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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TileProxyController 单元测试 — 覆盖瓦片代理和下载代理端点。
 *
 * @author DJI DevKit Team
 */
class TileProxyControllerTest {

    private MockMvc mockMvc;
    private HttpClient mockHttpClient;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws IOException, InterruptedException {
        mockHttpClient = mock(HttpClient.class);
        TerraService service = new MockTerraService();
        TileProxyController controller = new TileProxyController(service, mockHttpClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ── 瓦片代理 ──

    @Test
    void proxyTile_shouldReturn200_whenUpstreamSucceeds() throws Exception {
        byte[] tileContent = "{\"asset\":{\"version\":\"1.0\"}}".getBytes(StandardCharsets.UTF_8);
        mockUpstream(200, tileContent);

        mockMvc.perform(get("/api/v1/terra/tiles/1001/tileset.json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(content().bytes(tileContent));
    }

    @Test
    void proxyTile_shouldReturn502_whenUpstreamIOException() throws Exception {
        doThrow(new IOException("connection refused"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        mockMvc.perform(get("/api/v1/terra/tiles/1001/tileset.json"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void proxyTile_shouldReturnBadRequest_whenNoSubPath() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tiles/1001/"))
                .andExpect(status().isBadRequest());
    }

    // ── 安全发布专项：路径穿越防护 ──

    @Test
    void proxyTile_shouldRejectTraversal_whenSubPathContainsDotDot() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tiles/1001/../../secrets.yaml"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proxyTile_shouldRejectTraversal_whenSubPathContainsEncodedDotDot() throws Exception {
        // MockMvc 会对 %2f 做一层解码（path attr = 编码值），controller 再解码第二层后才进入 sanitize
        mockMvc.perform(get("/api/v1/terra/tiles/1001/..%2f..%2fsecrets.yaml"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proxyTile_shouldRejectAbsolutePath() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tiles/1001/%2fetc%2fpasswd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void proxyTile_shouldStillServeNormalSubpath() throws Exception {
        byte[] tileContent = "x".getBytes(StandardCharsets.UTF_8);
        mockUpstream(200, tileContent);

        mockMvc.perform(get("/api/v1/terra/tiles/1001/project/model/tileset.json"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(tileContent));
    }

    @Test
    void proxyTile_shouldForwardUpstreamError() throws Exception {
        mockUpstream(404, new byte[0]);

        mockMvc.perform(get("/api/v1/terra/tiles/1001/some/path.b3dm"))
                .andExpect(status().isNotFound());
    }

    // ── 下载代理 ──

    @Test
    @SuppressWarnings("unchecked")
    void downloadModel_shouldReturn200WithZipHeaders() throws Exception {
        byte[] zipContent = "PK-fake-zip-content".getBytes(StandardCharsets.UTF_8);
        HttpResponse<java.io.InputStream> mockResp = mock(HttpResponse.class);
        when(mockResp.statusCode()).thenReturn(200);
        when(mockResp.body()).thenReturn(new ByteArrayInputStream(zipContent));
        HttpHeaders mockHeaders = HttpHeaders.of(
                Map.of("Content-Length", java.util.List.of(String.valueOf(zipContent.length))),
                (a, b) -> true);
        when(mockResp.headers()).thenReturn(mockHeaders);
        doReturn(mockResp).when(mockHttpClient).send(any(HttpRequest.class), any());

        mockMvc.perform(get("/api/v1/terra/tiles/1001/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"model-1001.zip\""))
                .andExpect(content().bytes(zipContent));
    }

    @Test
    @SuppressWarnings("unchecked")
    void downloadModel_shouldReturn502_whenUpstreamFails() throws Exception {
        doThrow(new IOException("timeout"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        mockMvc.perform(get("/api/v1/terra/tiles/1001/download"))
                .andExpect(status().isBadGateway());
    }

    @Test
    @SuppressWarnings("unchecked")
    void downloadModel_shouldForwardUpstream404() throws Exception {
        HttpResponse<java.io.InputStream> mockResp = mock(HttpResponse.class);
        when(mockResp.statusCode()).thenReturn(404);
        doReturn(mockResp).when(mockHttpClient).send(any(HttpRequest.class), any());

        mockMvc.perform(get("/api/v1/terra/tiles/1001/download"))
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
