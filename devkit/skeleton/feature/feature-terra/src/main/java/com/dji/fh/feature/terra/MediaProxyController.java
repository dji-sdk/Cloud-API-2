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

import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 媒体资源代理端点 — 将内网 MinIO 预签名 URL 代理转发给浏览器。
 *
 * <p>SDK 返回的缩略图、预览图、影像和报告 URL 指向私有 MinIO 实例，
 * 浏览器无法直接访问。本 Controller 在后端代理转发，前端通过代理路径加载资源。
 *
 * <p>代理流程：
 * <ol>
 *   <li>优先从 {@link MediaUrlCache} 获取原始 URL（快速路径）</li>
 *   <li>缓存 miss 时 fallback 到 {@link TerraService} 重新获取（慢速路径）</li>
 *   <li>代理请求到 MinIO，流式返回响应</li>
 * </ol>
 *
 * @author DJI DevKit Team
 */
@RequireAuthenticated
@RestController
@RequestMapping("/api/v1/terra/media")
public class MediaProxyController {

    private static final Logger LOG = LoggerFactory.getLogger(MediaProxyController.class);

    /** 上游请求超时. */
    private static final Duration UPSTREAM_TIMEOUT = Duration.ofSeconds(30);

    /** 浏览器缓存时间（秒）. */
    private static final String CACHE_CONTROL_VALUE = "public, max-age=300";

    /** 文件扩展名 → Content-Type 映射. */
    private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
            ".webp", "image/webp",
            ".png", "image/png",
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".gif", "image/gif",
            ".pdf", "application/pdf",
            ".html", "text/html",
            ".json", "application/json"
    );

    /** 默认 Content-Type. */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MediaUrlCache cache;
    private final TerraService terraService;
    private final HttpClient httpClient;

    /**
     * 构造 MediaProxyController.
     *
     * @param cache        媒体 URL 缓存
     * @param terraService 业务服务
     */
    public MediaProxyController(final MediaUrlCache cache, final TerraService terraService) {
        this.cache = cache;
        this.terraService = terraService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(UPSTREAM_TIMEOUT)
                // 安全发布专项修复：跟随重定向可能使攻击者将请求转向内网任意主机
                // （预签名 URL 被 307 重定向到 file-storage-privatization 内部地址的场景已由 NORMAL
                // 覆盖不到——直接禁用重定向，仅代理原始响应，杜绝重定向 SSRF）
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * 测试用构造函数 — 允许注入自定义 HttpClient.
     *
     * @param cache        媒体 URL 缓存
     * @param terraService 业务服务
     * @param httpClient   自定义 HTTP 客户端
     */
    MediaProxyController(final MediaUrlCache cache,
                         final TerraService terraService,
                         final HttpClient httpClient) {
        this.cache = cache;
        this.terraService = terraService;
        this.httpClient = httpClient;
    }

    /**
     * 代理模型缩略图.
     *
     * @param fileId 模型文件 ID
     * @return 缩略图响应
     */
    @GetMapping("/thumb/{fileId}")
    public ResponseEntity<byte[]> proxyThumbnail(@PathVariable final long fileId) {
        String url = resolveUrl("thumb:" + fileId, () -> terraService.getModelThumbnailUrl(fileId));
        return proxyBinary(url, "缩略图", fileId);
    }

    /**
     * 代理模型预览图.
     *
     * @param fileId 模型文件 ID
     * @return 预览图响应
     */
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> proxyPreview(@PathVariable final long fileId) {
        String url = resolveUrl("preview:" + fileId, () -> terraService.getModelPreviewUrl(fileId));
        return proxyBinary(url, "预览图", fileId);
    }

    /**
     * 代理重建影像.
     *
     * @param taskId  任务 ID
     * @param imageId 影像 ID
     * @return 影像响应
     */
    @GetMapping("/image/{taskId}/{imageId}")
    public ResponseEntity<byte[]> proxyImage(
            @PathVariable final String taskId,
            @PathVariable final String imageId) {
        String cacheKey = "image:" + taskId + ":" + imageId;
        String url = resolveUrl(cacheKey, () -> terraService.getReconstructionImageUrl(taskId, imageId));
        return proxyBinary(url, "影像", taskId + "/" + imageId);
    }

    /**
     * 代理质量报告.
     *
     * @param taskId 任务 ID
     * @return 报告文件响应
     */
    @GetMapping("/report/{taskId}")
    public ResponseEntity<byte[]> proxyReport(@PathVariable final String taskId) {
        String url = resolveUrl("report:" + taskId, () -> terraService.getQualityReportFileUrl(taskId));
        return proxyBinary(url, "质量报告", taskId);
    }

    // ── 私有方法 ──

    /**
     * 解析原始 URL：先查缓存，miss 时走 fallback。
     */
    private String resolveUrl(String cacheKey, UrlSupplier fallback) {
        String url = cache.get(cacheKey);
        if (url != null) {
            return url;
        }
        try {
            url = fallback.get();
            if (url != null && !url.isBlank()) {
                cache.put(cacheKey, url);
            }
            return url;
        } catch (Exception e) {
            LOG.warn("媒体代理 fallback 获取 URL 失败：key={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 代理 HTTP 请求到上游 URL.
     */
    private ResponseEntity<byte[]> proxyBinary(String url, String label, Object id) {
        if (url == null || url.isBlank()) {
            LOG.warn("{}代理 URL 为空：id={}", label, id);
            return ResponseEntity.notFound().build();
        }

        try {
            HttpRequest upstreamReq = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(UPSTREAM_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<byte[]> upstreamResp = httpClient.send(
                    upstreamReq, HttpResponse.BodyHandlers.ofByteArray());

            if (upstreamResp.statusCode() >= HttpStatus.BAD_REQUEST.value()) {
                LOG.warn("{}代理上游返回 {}：id={}", label, upstreamResp.statusCode(), id);
                return ResponseEntity.status(upstreamResp.statusCode()).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, resolveContentType(url));
            headers.set(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE);

            return new ResponseEntity<>(upstreamResp.body(), headers, HttpStatus.OK);
        } catch (IOException e) {
            LOG.error("{}代理 IO 异常：id={}", label, id, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("{}代理被中断：id={}", label, id, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * 根据 URL 中的文件扩展名推断 Content-Type.
     */
    private static String resolveContentType(String url) {
        String path = URI.create(url).getPath();
        if (path == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx < 0) {
            return DEFAULT_CONTENT_TYPE;
        }
        String ext = path.substring(dotIdx).toLowerCase();
        // 去掉查询参数（预签名 URL 的扩展名后可能紧跟 ?X-Amz-...）
        int qIdx = ext.indexOf('?');
        if (qIdx > 0) {
            ext = ext.substring(0, qIdx);
        }
        return CONTENT_TYPE_MAP.getOrDefault(ext, DEFAULT_CONTENT_TYPE);
    }

    /**
     * URL 供应商函数接口（允许抛出异常）.
     */
    @FunctionalInterface
    interface UrlSupplier {
        /**
         * 获取 URL.
         *
         * @return URL 字符串
         * @throws Exception 获取失败时抛出
         */
        String get() throws Exception;
    }
}
