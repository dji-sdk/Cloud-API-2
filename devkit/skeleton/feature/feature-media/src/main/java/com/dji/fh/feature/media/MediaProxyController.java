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

package com.dji.fh.feature.media;

import com.dji.fh.kernel.spi.auth.RequireAuthenticated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * 媒体内容代理端点 — 将内网对象存储预签名 URL 流式转发给浏览器。
 *
 * <p>背景：SDK 返回的媒体下载地址指向对象存储 {@code :30802} 的预签名 URL，
 * 其 host 是内网 DNS 名或内网 IP，客户端网段不可达 —— 表现为「列表/缩略图正常（走 FH2 网关）、
 * 点开后黑屏（直连 :30802 超时）」。本端点由后端代取，前端只与本服务通信。
 *
 * <p>衍生问题：本端点代取时同样要和对象存储的 {@code :30802} 建 TLS 连接 —— 私有化部署该
 * 端口常用自签名证书，JVM 默认信任库不认得，会在证书校验阶段失败
 * （{@code SSLHandshakeException: PKIX path building failed}），表现与上面相同（黑屏/加载失败），
 * 但原因不是网络不可达而是证书不受信。配置 {@code fh.skeleton.feature.media.proxy-trusted-cert-path}
 * 指向该证书文件即可追加信任，见 {@link MediaProxySslContextFactory}。
 *
 * <p>与 {@code com.dji.fh.feature.terra.MediaProxyController} 的关键差异：
 * <ul>
 *   <li><b>流式</b>而非全量缓冲 —— 视频动辄数十 MB，{@code ofByteArray()} 会 OOM 且无法 seek；</li>
 *   <li><b>透传 Range</b> —— 上游返回 {@code 206} 时本端点同样回 {@code 206} + {@code Content-Range}，
 *       否则 {@code <video>} 无法拖动进度条；</li>
 *   <li>附带 {@code ?download=true} 时回 {@code Content-Disposition: attachment}。</li>
 * </ul>
 *
 * <p>鉴权：{@code <img>} / {@code <video>} 由浏览器原生加载，无法自定义 {@code Authorization} 头。
 * 本端点保持 {@link RequireAuthenticated}，由 {@code JwtAuthenticationProvider} 的
 * {@code ?token=} query 回退接管（与 {@code useTerra.ts} 的 EventSource 同一先例），
 * 避免把媒体内容开口成匿名可读。
 *
 * @author DJI DevKit Team
 */
@RequireAuthenticated
@RestController
@RequestMapping("/api/v1/media")
public class MediaProxyController {

    private static final Logger LOG = LoggerFactory.getLogger(MediaProxyController.class);

    /** 上游连接与响应头超时（不覆盖响应体读取时长）. */
    private static final Duration UPSTREAM_TIMEOUT = Duration.ofSeconds(30);

    /** 浏览器缓存时长 —— 媒体内容属登录用户私有，不能用 public 以免被共享代理缓存. */
    private static final String CACHE_CONTROL_VALUE = "private, max-age=300";

    /** 上游未给 Content-Type 时按扩展名推断的映射. */
    private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
            ".mp4", "video/mp4",
            ".mov", "video/quicktime",
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".webp", "image/webp"
    );

    /** 默认 Content-Type. */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /** 无法从 URL 解析出文件名时的兜底前缀. */
    private static final String FALLBACK_FILE_NAME_PREFIX = "media-";

    /** 缓存键前缀. */
    private static final String CACHE_KEY_PREFIX = "content:";

    /** 缓存 TTL 取 SDK 声明有效期的百分比，留出余量避免缓存到已过期的 URL. */
    private static final int CACHE_TTL_SAFETY_PERCENT = 90;

    /** Accept-Ranges 头取值 —— 上游支持字节分段. */
    private static final String BYTES_RANGE_UNIT = "bytes";

    /** 重定向状态码下界（3xx 段起点）. */
    private static final int REDIRECT_STATUS_MIN = 300;

    /** 重定向状态码上界（3xx 段终点，含）. */
    private static final int REDIRECT_STATUS_MAX = 399;

    private final MediaService mediaService;
    private final MediaUrlCache cache;
    private final HttpClient httpClient;

    /**
     * 构造 MediaProxyController.
     *
     * @param mediaService 媒体业务服务
     * @param cache        预签名 URL 缓存
     * @param props        媒体配置属性（含代理回源的受信证书路径）
     */
    public MediaProxyController(final MediaService mediaService, final MediaUrlCache cache,
                                final MediaProperties props) {
        this.mediaService = mediaService;
        this.cache = cache;
        this.httpClient = buildHttpClient(props);
    }

    /**
     * 构造回源用的 HttpClient。
     *
     * <p>私有化部署的对象存储常用自签名证书，JVM 默认信任库不认得，直连会在证书校验阶段
     * 失败。{@code props.proxyTrustedCertPath()} 配置时追加信任该证书，不配置则保持
     * JDK 默认信任链不变（不是 trust-all）。
     *
     * @param props 媒体配置属性
     * @return 回源 HttpClient
     */
    private static HttpClient buildHttpClient(final MediaProperties props) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(UPSTREAM_TIMEOUT)
                // 安全发布专项修复：跟随重定向可能被利用将请求转向内网任意主机，
                // 故一律不跟随，只把原始响应透传给前端。
                .followRedirects(HttpClient.Redirect.NEVER);
        SSLContext sslContext = MediaProxySslContextFactory.createSslContext(props.proxyTrustedCertPath());
        if (sslContext != null) {
            builder.sslContext(sslContext);
        }
        return builder.build();
    }

    /**
     * 测试用构造函数 — 允许注入自定义 HttpClient.
     *
     * @param mediaService 媒体业务服务
     * @param cache        预签名 URL 缓存
     * @param httpClient   自定义 HTTP 客户端
     */
    MediaProxyController(final MediaService mediaService,
                         final MediaUrlCache cache,
                         final HttpClient httpClient) {
        this.mediaService = mediaService;
        this.cache = cache;
        this.httpClient = httpClient;
    }

    /**
     * 代理媒体文件内容，支持 Range 分段请求。
     *
     * @param fileId   文件 ID
     * @param range    浏览器下发的 {@code Range} 请求头（可为空）
     * @param download 为 {@code true} 时以附件形式返回，触发浏览器下载
     * @return 媒体内容响应；URL 解析失败 404，上游不可达 502，请求中断 503
     */
    @GetMapping("/{fileId}/content")
    public ResponseEntity<InputStreamResource> streamContent(
            @PathVariable("fileId") final long fileId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) final String range,
            @RequestParam(name = "download", defaultValue = "false") final boolean download) {
        String url = resolveUrl(fileId);
        if (url == null || url.isBlank()) {
            LOG.warn("媒体代理 URL 为空：fileId={}", fileId);
            return ResponseEntity.notFound().build();
        }
        return proxyStream(url, range, download, fileId);
    }

    // ── 私有方法 ──

    /**
     * 解析预签名 URL：先查缓存，miss 时回源 SDK 并写入缓存。
     *
     * @param fileId 文件 ID
     * @return 预签名 URL；解析失败时返回 null
     */
    private String resolveUrl(final long fileId) {
        String cacheKey = CACHE_KEY_PREFIX + fileId;
        String url = cache.get(cacheKey);
        if (url != null) {
            return url;
        }
        try {
            DownloadUrlResponse resp = mediaService.getDownloadUrl(fileId);
            url = resp == null ? null : resp.downloadUrl();
            if (url != null && !url.isBlank()) {
                cache.put(cacheKey, url, resolveTtlSeconds(resp.expiresIn()));
            }
            return url;
        } catch (RuntimeException e) {
            LOG.warn("媒体代理回源获取 URL 失败：fileId={}", fileId, e);
            return null;
        }
    }

    /**
     * 计算缓存 TTL —— 取 SDK 声明有效期的九成，避免缓存到已过期的 URL。
     *
     * <p>注意有效期口径：这里用的是 SDK 在 {@link DownloadUrlResponse#expiresIn()} 里声明的值，
     * <b>不是</b>预签名 URL 上 {@code X-Amz-Expires} 的实际值，两者并不相等
     * （实测 URL 上为 86400，而 SDK 固定声明 3600）。不去解析 URL 上的参数是刻意的 ——
     * 那是平台侧的产物，形态随时可能变，解析它等于把本模块耦合到一个不受本仓控制的契约上。
     * 当前偏差方向是安全的：TTL 短于真实有效期，最坏只是提前回源一次，绝不会缓存到过期 URL。
     *
     * @param expiresIn SDK 声明的有效期（秒）
     * @return 缓存秒数；无法确定时返回 0，由缓存实现按「不缓存」处理
     */
    private static long resolveTtlSeconds(final int expiresIn) {
        if (expiresIn <= 0) {
            return 0;
        }
        return expiresIn * CACHE_TTL_SAFETY_PERCENT / 100;
    }

    /**
     * 流式代理上游媒体内容，透传 Range 与分段响应头。
     *
     * @param url      上游预签名 URL
     * @param range    原始 Range 请求头
     * @param download 是否以附件形式返回
     * @param fileId   文件 ID（仅用于日志）
     * @return 代理响应
     */
    private ResponseEntity<InputStreamResource> proxyStream(final String url,
                                                            final String range,
                                                            final boolean download,
                                                            final long fileId) {
        try {
            HttpResponse<InputStream> upstreamResp =
                    httpClient.send(buildUpstreamRequest(url, range), HttpResponse.BodyHandlers.ofInputStream());

            int statusCode = upstreamResp.statusCode();
            if (isRedirect(statusCode)) {
                // 不跟随重定向是安全修复 —— 跟随可被利用把请求转向内网任意主机。
                // 但 3xx 原样透传同样无意义：Location 不在透传白名单里，浏览器收到的会是一个
                // 没有落点的重定向，表现为无提示的失败。故显式收敛成 502，让前端拿到明确的服务端错误。
                LOG.warn("媒体代理上游返回重定向 {}，按安全策略不跟随：fileId={}", statusCode, fileId);
                closeQuietly(upstreamResp.body());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }
            if (statusCode >= HttpStatus.BAD_REQUEST.value()) {
                LOG.warn("媒体代理上游返回 {}：fileId={}", statusCode, fileId);
                closeQuietly(upstreamResp.body());
                return ResponseEntity.status(statusCode).build();
            }

            HttpHeaders headers = buildDownstreamHeaders(upstreamResp, url, download, fileId);
            HttpStatus status = HttpStatus.resolve(statusCode);
            return new ResponseEntity<>(
                    new InputStreamResource(upstreamResp.body()),
                    headers,
                    status == null ? HttpStatus.OK : status);
        } catch (IOException e) {
            LOG.error("媒体代理 IO 异常：fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("媒体代理被中断：fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * 判断上游状态码是否属于 3xx 重定向段。
     *
     * @param statusCode 上游状态码
     * @return true 表示重定向
     */
    private static boolean isRedirect(final int statusCode) {
        return statusCode >= REDIRECT_STATUS_MIN && statusCode <= REDIRECT_STATUS_MAX;
    }

    /**
     * 构造上游请求，按需透传 Range 头。
     *
     * @param url   上游预签名 URL
     * @param range 原始 Range 请求头，可为空
     * @return 上游请求
     */
    private static HttpRequest buildUpstreamRequest(final String url, final String range) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(UPSTREAM_TIMEOUT)
                .GET();
        if (range != null && !range.isBlank()) {
            builder.header(HttpHeaders.RANGE, range);
        }
        return builder.build();
    }

    /**
     * 构造回传前端的响应头 —— 只透传与分段播放相关的头，其余一律不复制。
     *
     * @param upstreamResp 上游响应
     * @param url          上游 URL（Content-Type / 文件名兜底用）
     * @param download     是否以附件形式返回
     * @param fileId       文件 ID（文件名兜底用）
     * @return 回传响应头
     */
    private static HttpHeaders buildDownstreamHeaders(final HttpResponse<InputStream> upstreamResp,
                                                      final String url,
                                                      final boolean download,
                                                      final long fileId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, resolveContentType(upstreamResp, url));
        headers.set(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE);
        // 始终声明支持分段，否则 <video> 不会尝试带 Range 拖动
        headers.set(HttpHeaders.ACCEPT_RANGES, BYTES_RANGE_UNIT);
        copyHeader(upstreamResp, HttpHeaders.CONTENT_RANGE, headers);
        copyHeader(upstreamResp, HttpHeaders.CONTENT_LENGTH, headers);
        if (download) {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, buildAttachmentDisposition(url, fileId));
        }
        return headers;
    }

    /**
     * 复制上游响应头（存在时才复制）.
     *
     * @param upstreamResp 上游响应
     * @param name         头名称
     * @param target       目标响应头
     */
    private static void copyHeader(final HttpResponse<InputStream> upstreamResp,
                                   final String name,
                                   final HttpHeaders target) {
        upstreamResp.headers().firstValue(name).ifPresent(value -> target.set(name, value));
    }

    /**
     * 推断 Content-Type：优先用上游声明的（可信），缺失时按 URL 扩展名兜底。
     *
     * <p>注意文件名存在双后缀缺陷（如 {@code ..._V.mp4mp4}），仅靠扩展名会退化成
     * {@code application/octet-stream}，故必须以服务端响应头为准。
     *
     * @param upstreamResp 上游响应
     * @param url          上游 URL
     * @return Content-Type
     */
    private static String resolveContentType(final HttpResponse<InputStream> upstreamResp, final String url) {
        Optional<String> upstreamType = upstreamResp.headers().firstValue(HttpHeaders.CONTENT_TYPE);
        if (upstreamType.isPresent() && !upstreamType.get().isBlank()) {
            return upstreamType.get();
        }
        String path = URI.create(url).getPath();
        if (path == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx < 0) {
            return DEFAULT_CONTENT_TYPE;
        }
        return CONTENT_TYPE_MAP.getOrDefault(path.substring(dotIdx).toLowerCase(), DEFAULT_CONTENT_TYPE);
    }

    /**
     * 构造附件下载的 Content-Disposition，文件名取上游 URL 的末段。
     *
     * <p>用 Spring 的 {@link ContentDisposition} 而非手工拼串 —— 它会做 RFC 5987 编码，
     * 避免非 ASCII 文件名或引号、换行等字符污染响应头。
     *
     * @param url    上游 URL
     * @param fileId 文件 ID（解析不出文件名时兜底）
     * @return Content-Disposition 头值
     */
    private static String buildAttachmentDisposition(final String url, final long fileId) {
        return ContentDisposition.attachment()
                .filename(resolveFileName(url, fileId), StandardCharsets.UTF_8)
                .build()
                .toString();
    }

    /**
     * 从预签名 URL 的末段解析文件名。
     *
     * @param url    上游 URL
     * @param fileId 文件 ID
     * @return 文件名；无法解析时返回 {@code media-{fileId}}
     */
    private static String resolveFileName(final String url, final long fileId) {
        String fallback = FALLBACK_FILE_NAME_PREFIX + fileId;
        String path = URI.create(url).getPath();
        if (path == null) {
            return fallback;
        }
        int slashIdx = path.lastIndexOf('/');
        String name = slashIdx < 0 ? path : path.substring(slashIdx + 1);
        return name.isBlank() ? fallback : name;
    }

    /**
     * 关闭流，忽略关闭时的异常 —— 仅用于丢弃上游错误响应体.
     *
     * @param stream 待关闭的流，可为 null
     */
    private static void closeQuietly(final InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
            LOG.debug("关闭上游响应流失败", e);
        }
    }
}
