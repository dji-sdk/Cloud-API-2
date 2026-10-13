/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.fh.feature.terra;

import com.dji.fh.feature.terra.dto.ModelUrlDto;
import com.dji.fh.feature.terra.dto.TileStorageInfo;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * 3D Tiles 瓦片代理端点。
 *
 * <p>Cesium 加载 tileset.json 后会按相对路径请求子瓦片（.b3dm/.glb），
 * 直接指向私有 MinIO 桶会因缺少签名而 403。
 * 本 Controller 在后端代理转发，前端无感知。
 *
 * @author DJI DevKit Team
 */
@RequireAuthenticated
@RestController
@RequestMapping("/api/v1/terra/tiles")
public class TileProxyController {

    private static final Logger LOG = LoggerFactory.getLogger(TileProxyController.class);

    /** 瓦片代理上游请求超时. */
    private static final Duration UPSTREAM_TIMEOUT = Duration.ofSeconds(30);

    /** 下载代理上游超时（模型 ZIP 可达数百 MB，需更长超时）. */
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(5);

    /** 文件扩展名 → Content-Type 映射. */
    private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
            ".json", "application/json",
            ".b3dm", "application/octet-stream",
            ".glb", "model/gltf-binary",
            ".gltf", "model/gltf+json",
            ".cmpt", "application/octet-stream",
            ".pnts", "application/octet-stream",
            ".i3dm", "application/octet-stream",
            ".png", "image/png",
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg"
    );

    /** 默认 Content-Type. */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final TerraService terraService;
    private final HttpClient httpClient;

    /**
     * 构造 TileProxyController.
     *
     * @param terraService 业务服务
     */
    public TileProxyController(final TerraService terraService) {
        this.terraService = terraService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(UPSTREAM_TIMEOUT)
                // 安全发布专项修复：禁止跟随重定向，避免请求被转向内网任意主机。
                // storageBaseUrl 来自 SDK getModelDetail，本已限定 file-storage-privatization 桶；
                // 但上一层代理（如网关）可能下发 3xx 到外网，跟随后构成重定向 SSRF。
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * 测试用构造函数 — 允许注入自定义 HttpClient.
     *
     * @param terraService 业务服务
     * @param httpClient   自定义 HTTP 客户端
     */
    TileProxyController(final TerraService terraService, final HttpClient httpClient) {
        this.terraService = terraService;
        this.httpClient = httpClient;
    }

    /**
     * 代理转发瓦片请求到 MinIO 存储.
     *
     * <p>URL 格式：{@code /api/v1/terra/tiles/{fileId}/tileset.json}
     * 或子瓦片 {@code /api/v1/terra/tiles/{fileId}/path/to/tile.b3dm}
     *
     * <p>安全发布专项修复：{@code subPath} 必须先经
     * {@link #sanitizeSubPath} 归一化并拒绝任何 ".." 段穿越，再拼接上游对象 URL。
     * 否则攻击者可用 {@code ../} 跳出文件名前缀，读取同桶任意对象。
     *
     * @param fileId  模型文件 ID
     * @param request 原始请求（用于提取子路径）
     * @return 上游响应体
     */
    @GetMapping("/{fileId}/**")
    public ResponseEntity<byte[]> proxyTile(
            @PathVariable final long fileId,
            final HttpServletRequest request) {
        String fullPath = (String) request.getAttribute(
                "org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping");
        if (fullPath == null) {
            fullPath = request.getRequestURI();
        }

        String prefix = "/api/v1/terra/tiles/" + fileId + "/";
        String subPath = fullPath.startsWith(prefix)
                ? fullPath.substring(prefix.length())
                : "";

        // 安全发布专项修复：路径穿越防护
        subPath = sanitizeSubPath(subPath);
        if (subPath == null) {
            LOG.warn("瓦片代理拒绝非法路径：fileId={}, rawSubPath={}", fileId,
                    request.getRequestURI());
            return ResponseEntity.badRequest().build();
        }

        TileStorageInfo info = terraService.getTileStorageInfo(fileId);
        String objectUrl = info.storageBaseUrl() + info.filePrefix() + subPath;

        try {
            HttpRequest upstreamReq = HttpRequest.newBuilder()
                    .uri(URI.create(objectUrl))
                    .timeout(UPSTREAM_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<byte[]> upstreamResp = httpClient.send(
                    upstreamReq, HttpResponse.BodyHandlers.ofByteArray());

            if (upstreamResp.statusCode() >= HttpStatus.BAD_REQUEST.value()) {
                LOG.warn("瓦片代理上游返回 {}，fileId={}, subPath={}", upstreamResp.statusCode(), fileId, subPath);
                return ResponseEntity.status(upstreamResp.statusCode()).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, resolveContentType(subPath));
            headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=86400");

            return new ResponseEntity<>(upstreamResp.body(), headers, HttpStatus.OK);
        } catch (IOException e) {
            LOG.error("瓦片代理 IO 异常，fileId={}, subPath={}", fileId, subPath, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("瓦片代理被中断，fileId={}, subPath={}", fileId, subPath, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * 模型文件下载代理 — 流式转发 MinIO 预签名 URL 内容到浏览器。
     *
     * <p>SDK 返回的预签名 URL 指向内网 MinIO，浏览器无法直接访问。
     * 本端点在后端流式代理转发，前端通过 {@code window.open} 触发下载。
     *
     * <p>Spring MVC 路径匹配中 {@code /{fileId}/download} 优先于 {@code /{fileId}/**}。
     *
     * @param fileId 模型文件 ID
     * @return 流式响应（ZIP 文件）
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadModel(
            @PathVariable final long fileId) {
        ModelUrlDto urlDto = terraService.getModelUrl(fileId);
        if (urlDto == null || urlDto.url() == null || urlDto.url().isBlank()) {
            LOG.warn("下载代理获取 URL 失败：fileId={}", fileId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            HttpRequest upstreamReq = HttpRequest.newBuilder()
                    .uri(URI.create(urlDto.url()))
                    .timeout(DOWNLOAD_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<InputStream> upstreamResp = httpClient.send(
                    upstreamReq, HttpResponse.BodyHandlers.ofInputStream());

            if (upstreamResp.statusCode() >= HttpStatus.BAD_REQUEST.value()) {
                InputStream errBody = upstreamResp.body();
                if (errBody != null) {
                    errBody.close();
                }
                LOG.warn("下载代理上游返回 {}，fileId={}", upstreamResp.statusCode(), fileId);
                return ResponseEntity.status(upstreamResp.statusCode()).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "application/zip");
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"model-" + fileId + ".zip\"");
            upstreamResp.headers().firstValue("Content-Length")
                    .ifPresent(len -> headers.set(HttpHeaders.CONTENT_LENGTH, len));

            return new ResponseEntity<>(
                    new InputStreamResource(upstreamResp.body()), headers, HttpStatus.OK);
        } catch (IOException e) {
            LOG.error("下载代理 IO 异常，fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("下载代理被中断，fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * 根据文件扩展名推断 Content-Type.
     *
     * @param path 文件路径
     * @return MIME 类型
     */
    private static String resolveContentType(final String path) {
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx < 0) {
            return DEFAULT_CONTENT_TYPE;
        }
        String ext = path.substring(dotIdx).toLowerCase();
        return CONTENT_TYPE_MAP.getOrDefault(ext, DEFAULT_CONTENT_TYPE);
    }

    /**
     * 清洗并校验瓦片子路径，阻断路径穿越。
     *
     * <p>规则：
     * <ol>
     *   <li>空路径直接拒绝（tileset.json 至少 1 个文件名段）</li>
     *   <li>URL 解码后逐段拆分，任何一段为 ""、"." 或 ".." 一律拒绝——".." 可穿越
     *       {@code filePrefix} 前缀读取同桶任意对象，属 SSRF/对象枚举高危向量</li>
     *   <li>规范化（合并 "./"）后必须仍与原始路径一致，拒绝 {@code %2e%2e} 等编码绕过</li>
     * </ol>
     *
     * @param rawSubPath 控制器提取的原始子路径（可能含 ".." / URL 编码）
     * @return 清洗后的安全子路径；非法时返回 null
     */
    private static String sanitizeSubPath(final String rawSubPath) {
        if (rawSubPath == null || rawSubPath.isEmpty() || rawSubPath.isBlank()) {
            return null;
        }
        final String decoded;
        try {
            decoded = decodeUpToTwice(rawSubPath);
        } catch (IllegalArgumentException e) {
            LOG.warn("瓦片代理拒绝非法 URL 编码路径：{}", rawSubPath);
            return null;
        }
        // 解码后第一遍逐段校验：任何纯 "" / "." / ".." 段直接拒绝（明文 ".." 路径穿越）
        for (String segment : decoded.split("/")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                return null;
            }
            if ("..".equals(segment)) {
                return null;
            }
        }
        // 禁止路径分隔符混入（Windows 反斜杠 / %5c 不可作为目录分隔符注入 MinIO 对象键）
        if (decoded.indexOf('\\') >= 0) {
            return null;
        }
        // 规范化必须保持"会话段落后"（prefix 适当 + 至少一个文件名段），且不得逃逸到 prefix 之外
        final Path path = Path.of(decoded);
        if (path.isAbsolute()) {
            return null;
        }
        final String normalized = path.normalize().toString();
        // 拒绝含目录分隔符或穿越残余的结果：最短合法对象为单文件名（如 tileset.json）
        if (normalized.isEmpty() || normalized.startsWith("..") || normalized.endsWith("..")) {
            return null;
        }
        for (String segment : normalized.split("/")) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                return null;
            }
        }
        return normalized;
    }

    /**
     * 对子路径做最多两轮 URL 解码，归一化"双编码"绕过。
     *
     * <p>Servlet 层已对请求路径解码一层（因此 {@code pathWithinHandlerMapping} 中的
     * {@code %252e} 保留为 {@code %2e}），而 Tomcat/Undertow 的 {@code requestUri} 可能
     * 已完整解码。为统一起见：解码 ≤2 次，使 {@code %252e→%2e→.}、{@code %255c→%5c→\\}
     * 均落入归一化校验，消除各容器解码差异。
     *
     * @param s 原始子路径
     * @return 充分解码后的路径
     */
    private static String decodeUpToTwice(final String s) {
        String current = s;
        for (int i = 0; i < 2; i++) {
            String next = java.net.URLDecoder.decode(current,
                    java.nio.charset.StandardCharsets.UTF_8);
            if (next.equals(current)) {
                break;
            }
            current = next;
        }
        return current;
    }
}
