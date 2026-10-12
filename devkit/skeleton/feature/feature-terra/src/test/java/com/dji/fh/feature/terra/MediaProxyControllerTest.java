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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MediaProxyController 单元测试 — 覆盖缩略图/预览图/影像/报告代理端点。
 *
 * @author DJI DevKit Team
 */
class MediaProxyControllerTest {

    private MockMvc mockMvc;
    private HttpClient mockHttpClient;
    private MediaUrlCache cache;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        cache = new MediaUrlCache();
        TerraService service = new MockTerraService();
        MediaProxyController controller = new MediaProxyController(cache, service, mockHttpClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ── 缩略图代理 ──

    @Test
    void proxyThumbnail_shouldReturn200_whenCacheHit() throws Exception {
        byte[] imageBytes = "fake-webp".getBytes(StandardCharsets.UTF_8);
        cache.put("thumb:42", "http://minio/thumb.webp");
        mockUpstream(200, imageBytes);

        mockMvc.perform(get("/api/v1/terra/media/thumb/42"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/webp"))
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    void proxyThumbnail_shouldFallback_whenCacheMiss() throws Exception {
        byte[] imageBytes = "fake-webp".getBytes(StandardCharsets.UTF_8);
        mockUpstream(200, imageBytes);

        mockMvc.perform(get("/api/v1/terra/media/thumb/42"))
                .andExpect(status().isOk());
    }

    @Test
    void proxyThumbnail_shouldReturn502_whenUpstreamIOError() throws Exception {
        cache.put("thumb:42", "http://minio/thumb.webp");
        doThrow(new IOException("connection refused"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        mockMvc.perform(get("/api/v1/terra/media/thumb/42"))
                .andExpect(status().isBadGateway());
    }

    // ── 预览图代理 ──

    @Test
    void proxyPreview_shouldReturn200() throws Exception {
        byte[] imageBytes = "fake-png".getBytes(StandardCharsets.UTF_8);
        cache.put("preview:42", "http://minio/preview.png");
        mockUpstream(200, imageBytes);

        mockMvc.perform(get("/api/v1/terra/media/preview/42"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
    }

    // ── 影像代理 ──

    @Test
    void proxyImage_shouldReturn200() throws Exception {
        byte[] imageBytes = "fake-jpg".getBytes(StandardCharsets.UTF_8);
        cache.put("image:task-1:img-5", "http://minio/image.jpg");
        mockUpstream(200, imageBytes);

        mockMvc.perform(get("/api/v1/terra/media/image/task-1/img-5"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void proxyImage_shouldFallback_whenCacheMiss() throws Exception {
        byte[] imageBytes = "fake-jpg".getBytes(StandardCharsets.UTF_8);
        mockUpstream(200, imageBytes);

        mockMvc.perform(get("/api/v1/terra/media/image/task-1/img-5"))
                .andExpect(status().isOk());
    }

    // ── 质量报告代理 ──

    @Test
    void proxyReport_shouldReturn200() throws Exception {
        byte[] reportBytes = "<html>report</html>".getBytes(StandardCharsets.UTF_8);
        cache.put("report:task-1", "http://minio/report.html");
        mockUpstream(200, reportBytes);

        mockMvc.perform(get("/api/v1/terra/media/report/task-1"))
                .andExpect(status().isOk());
    }

    // ── 上游错误透传 ──

    @Test
    void proxyThumbnail_shouldForwardUpstreamError() throws Exception {
        cache.put("thumb:42", "http://minio/thumb.webp");
        mockUpstream(404, new byte[0]);

        mockMvc.perform(get("/api/v1/terra/media/thumb/42"))
                .andExpect(status().isNotFound());
    }

    // ── 帮助方法 ──

    @SuppressWarnings("unchecked")
    private void mockUpstream(int statusCode, byte[] body)
            throws IOException, InterruptedException {
        HttpResponse<byte[]> mockResp = mock(HttpResponse.class);
        when(mockResp.statusCode()).thenReturn(statusCode);
        when(mockResp.body()).thenReturn(body);
        doReturn(mockResp).when(mockHttpClient).send(any(HttpRequest.class), any());
    }
}
