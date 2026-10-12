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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link MediaProxyController} 单元测试。
 *
 * <p>不依赖真实网络：注入 mock 的 {@link HttpClient}，覆盖 Range 透传、缓存命中、
 * 错误分支（404 / 3xx 收敛为 502 / 502 / 503）与下载落盘头。
 *
 * @author DJI DevKit Team
 */
class MediaProxyControllerTest {

    /** 测试用 fileId. */
    private static final long FILE_ID = 12345L;

    /** 测试用预签名 URL —— 模拟内网对象存储 :30802 的地址形态. */
    private static final String PRESIGNED_URL =
            "http://storage.internal:30802/file-storage-privatization/a/b/DJI_0001_V.mp4?X-Amz-Expires=3600";

    private static final String CONTENT_TYPE_MP4 = "video/mp4";

    /** 视频总长度，用于构造 Content-Range. */
    private static final long TOTAL_LENGTH = 33080057L;

    private HttpClient mockHttpClient;
    private StubMediaService mediaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        mediaService = new StubMediaService();
        MediaProxyController controller =
                new MediaProxyController(mediaService, new MediaUrlCache(), mockHttpClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("带 Range 请求时应回 206 并透传 Content-Range 与 Accept-Ranges")
    void streamContent_shouldReturn206_whenRangeProvided() throws Exception {
        mockUpstream(206, mp4Bytes(), Map.of(
                HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4,
                HttpHeaders.CONTENT_RANGE, "bytes 0-1023/" + TOTAL_LENGTH));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID)
                        .header(HttpHeaders.RANGE, "bytes=0-1023"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-1023/" + TOTAL_LENGTH))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4))
                .andExpect(content().bytes(mp4Bytes()));
    }

    @Test
    @DisplayName("Range 头应原样转发给上游")
    void streamContent_shouldForwardRangeUpstream() throws Exception {
        mockUpstream(206, mp4Bytes(), Map.of(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID)
                        .header(HttpHeaders.RANGE, "bytes=2048-4095"))
                .andExpect(status().isPartialContent());

        HttpRequest sent = captureUpstreamRequest();
        Assertions.assertEquals("bytes=2048-4095",
                sent.headers().firstValue(HttpHeaders.RANGE).orElse(null),
                "Range 头必须透传给上游，否则无法分段取流");
    }

    @Test
    @DisplayName("无 Range 且上游回 200 时，本端点应回 200 且带 Accept-Ranges")
    void streamContent_shouldReturn200_whenNoRange() throws Exception {
        mockUpstream(200, mp4Bytes(), Map.of(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andExpect(content().bytes(mp4Bytes()));
    }

    @Test
    @DisplayName("二次请求应命中缓存，不再回源 SDK")
    void streamContent_shouldHitCache_onSecondRequest() throws Exception {
        mockUpstream(200, mp4Bytes(), Map.of(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID)).andExpect(status().isOk());

        Assertions.assertEquals(1, mediaService.getDownloadUrlInvocations,
                "同一 fileId 的预签名 URL 应被缓存，避免每个 Range 请求都回源 SDK");
    }

    @Test
    @DisplayName("缓存 TTL 取自 SDK 声明的 expiresIn，而非 URL 上的 X-Amz-Expires")
    void streamContent_shouldDeriveTtlFromSdkExpiresIn_notFromPresignedUrl() throws Exception {
        // URL 上写着 X-Amz-Expires=3600（真实环境为 86400），但 SDK 声明有效期为 0
        mediaService.expiresIn = 0;
        mockUpstream(200, mp4Bytes(), Map.of(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID)).andExpect(status().isOk());

        Assertions.assertEquals(2, mediaService.getDownloadUrlInvocations,
                "有效期只能信 SDK 声明的 expiresIn；若改去解析 URL 上的 X-Amz-Expires，"
                        + "本仓就被绑定到平台侧随时可变的 URL 形态上");
    }

    @Test
    @DisplayName("?download=true 时应回 Content-Disposition: attachment")
    void streamContent_shouldSetAttachment_whenDownloadTrue() throws Exception {
        mockUpstream(200, mp4Bytes(), Map.of(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID).param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        Matchers.startsWith("attachment;")));
    }

    @Test
    @DisplayName("非下载模式不应带 Content-Disposition")
    void streamContent_shouldNotSetAttachment_whenDownloadFalse() throws Exception {
        mockUpstream(200, mp4Bytes(), Map.of(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @DisplayName("SDK 返回空 URL 时应回 404")
    void streamContent_shouldReturn404_whenUrlBlank() throws Exception {
        mediaService.downloadUrl = "";

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("回源 SDK 抛异常时应回 404 而非 500")
    void streamContent_shouldReturn404_whenSdkThrows() throws Exception {
        mediaService.throwOnGet = true;

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("上游返回 404 时应原样透传状态码")
    void streamContent_shouldForwardStatus_whenUpstreamNotFound() throws Exception {
        mockUpstream(404, new byte[0], Map.of());

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("上游返回 3xx 时应收敛为 502，而非透传一个没有 Location 的重定向")
    void streamContent_shouldReturn502_whenUpstreamRedirects() throws Exception {
        CloseTrackingInputStream body = new CloseTrackingInputStream();
        mockUpstreamRaw(302, body,
                Map.of(HttpHeaders.LOCATION, "http://storage.internal:30802/another-object"));

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isBadGateway())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));

        Assertions.assertTrue(body.isClosed(), "被丢弃的上游重定向响应体必须关闭");
    }

    @Test
    @DisplayName("上游不可达时应回 502")
    void streamContent_shouldReturn502_whenUpstreamUnreachable() throws Exception {
        doThrow(new IOException("connection refused"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("请求被中断时应回 503 并恢复中断标志")
    void streamContent_shouldReturn503_whenInterrupted() throws Exception {
        doThrow(new InterruptedException("interrupted"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        try {
            mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                    .andExpect(status().isServiceUnavailable());

            Assertions.assertTrue(Thread.interrupted(),
                    "中断标志必须被恢复，否则会污染线程池后续任务");
        } finally {
            // 兜底清除，避免污染同一线程上的后续用例
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("上游未给 Content-Type 时应按 URL 扩展名兜底")
    void streamContent_shouldFallbackContentType_byExtension() throws Exception {
        mockUpstream(200, mp4Bytes(), Map.of());

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MP4));
    }

    @Test
    @DisplayName("上游错误响应体应被关闭，避免连接泄漏")
    void streamContent_shouldCloseUpstreamBody_whenStatusError() throws Exception {
        CloseTrackingInputStream body = new CloseTrackingInputStream();
        mockUpstreamRaw(404, body, Map.of());

        mockMvc.perform(get("/api/v1/media/{fileId}/content", FILE_ID))
                .andExpect(status().isNotFound());

        Assertions.assertTrue(body.isClosed(), "上游错误响应体必须关闭");
    }

    // ── 测试替身与辅助方法 ──

    private static byte[] mp4Bytes() {
        return "ftypisom-mock-body".getBytes();
    }

    /**
     * mock 上游响应（字节数组体）。
     *
     * @param statusCode 状态码
     * @param body       响应体
     * @param headers    响应头
     */
    private void mockUpstream(final int statusCode,
                              final byte[] body,
                              final Map<String, String> headers) throws Exception {
        mockUpstreamRaw(statusCode, new ByteArrayInputStream(body), headers);
    }

    /**
     * mock 上游响应（任意 InputStream 体）。
     *
     * @param statusCode 状态码
     * @param body       响应体流
     * @param headers    响应头
     */
    @SuppressWarnings("unchecked")
    private void mockUpstreamRaw(final int statusCode,
                                 final InputStream body,
                                 final Map<String, String> headers) throws Exception {
        // 先建好 headers 替身再进入 HttpResponse 的 stubbing —— Mockito 不允许嵌套 stubbing
        java.net.http.HttpHeaders mockHeaders = mockJdkHeaders(headers);

        HttpResponse<InputStream> mockResp = mock(HttpResponse.class);
        when(mockResp.statusCode()).thenReturn(statusCode);
        when(mockResp.body()).thenReturn(body);
        when(mockResp.headers()).thenReturn(mockHeaders);
        doReturn(mockResp).when(mockHttpClient).send(any(HttpRequest.class), any());
    }

    /**
     * 构造 JDK {@link java.net.http.HttpHeaders} 的测试替身（该类无公开构造函数）。
     *
     * <p>注意与 Spring 的 {@link HttpHeaders} 同名，此处显式使用全限定名区分。
     *
     * @param headers 头名到头值的映射（大小写不敏感）
     * @return HttpHeaders 测试替身
     */
    @SuppressWarnings("unchecked")
    private static java.net.http.HttpHeaders mockJdkHeaders(final Map<String, String> headers) {
        java.net.http.HttpHeaders mockHeaders = mock(java.net.http.HttpHeaders.class);
        when(mockHeaders.firstValue(any(String.class)))
                .thenAnswer(invocation -> {
                    String name = invocation.getArgument(0);
                    return headers.entrySet().stream()
                            .filter(e -> e.getKey().equalsIgnoreCase(name))
                            .map(Map.Entry::getValue)
                            .findFirst();
                });
        return mockHeaders;
    }

    /**
     * 取出 controller 实际发往上游的请求。
     *
     * @return 捕获到的上游请求
     */
    private HttpRequest captureUpstreamRequest() throws Exception {
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(1)).send(captor.capture(), any());
        return captor.getValue();
    }

    /**
     * 可追踪关闭状态的输入流，用于断言上游错误响应体被释放。
     */
    private static final class CloseTrackingInputStream extends InputStream {

        private boolean closed;

        boolean isClosed() {
            return closed;
        }

        @Override
        public int read() {
            return -1;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    /**
     * 返回可控下载 URL 的 MediaService 测试替身。
     */
    private static final class StubMediaService extends MediaService {

        private String downloadUrl = PRESIGNED_URL;
        private boolean throwOnGet;
        private int getDownloadUrlInvocations;
        private int expiresIn = DEFAULT_EXPIRES_IN;

        StubMediaService() {
            super(new MediaProperties(50, 500, true, ""));
        }

        @Override
        public DownloadUrlResponse getDownloadUrl(final long fileId) {
            getDownloadUrlInvocations++;
            if (throwOnGet) {
                throw new IllegalStateException("SDK 调用失败");
            }
            return new DownloadUrlResponse(fileId, downloadUrl, expiresIn);
        }

        /** SDK 声明的 URL 有效期（秒）—— 需大于 0 才会进入缓存. */
        private static final int DEFAULT_EXPIRES_IN = 3600;
    }
}
