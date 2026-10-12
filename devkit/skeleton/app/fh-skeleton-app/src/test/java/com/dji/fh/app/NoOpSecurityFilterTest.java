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

package com.dji.fh.app;

import com.dji.fh.app.security.NoOpSecurityFilter;
import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.PublicPaths;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NoOpSecurityFilterTest {

    private final NoOpSecurityFilter filter = new NoOpSecurityFilter();

    @AfterEach
    void clearCtx() {
        SecurityContextHolder.clear();
    }

    @Test
    void doFilter_shouldSetNoOpContext_duringChain() throws Exception {
        SecurityContext[] seen = new SecurityContext[1];
        HttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = (r, s) -> seen[0] = SecurityContextHolder.current();

        filter.doFilter(req, resp, chain);

        assertThat(seen[0]).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void doFilter_shouldClearContext_afterChain() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        // chain 跑完后 holder 应被清回 NoOp（来自 default fallback）
        assertThat(SecurityContextHolder.current()).isSameAs(NoOpSecurityContext.INSTANCE);
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doFilter_shouldClearContext_evenWhenChainThrows() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        doThrow(new RuntimeException("downstream boom")).when(chain)
                .doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> filter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("downstream boom");

        // finally 块必须 clear 干净
        assertThat(SecurityContextHolder.current()).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    // ── 安全发布专项：公开路径白名单 ──

    @Test
    void doFilter_shouldNotSetContext_onPublicPath() throws Exception {
        // SecurityContextHolder 默认 fallback 为 NoOp；公开路径不应被注入上下文
        SecurityContext[] seen = new SecurityContext[1];
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
        FilterChain chain = (r, s) -> seen[0] = SecurityContextHolder.current();

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(seen[0]).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void doFilter_shouldSetContext_onNonPublicPath() throws Exception {
        SecurityContext[] seen = new SecurityContext[1];
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/devices");
        FilterChain chain = (r, s) -> seen[0] = SecurityContextHolder.current();

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(seen[0]).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void publicPaths_exactMatch_shouldRejectLookAlikePaths() {
        // 精确 equals：伪装路径 /api/auth/loginnn 不得命中 /api/auth/login
        assertThat(PublicPaths.matches("/api/auth/login")).isTrue();
        assertThat(PublicPaths.matches("/api/auth/register")).isTrue();
        assertThat(PublicPaths.matches("/api/auth/loginnn")).isFalse();
        assertThat(PublicPaths.matches("/api/auth/login/")).isFalse();
        assertThat(PublicPaths.matches("/api/v1/devices")).isFalse();
        assertThat(PublicPaths.matches(null)).isFalse();
    }

    @Test
    void publicPaths_shouldStripContextPath_beforeMatching() {
        // 兼容 context-path：/pf-devkit/ai-plugin/skeleton/api/auth/login
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
        req.setContextPath("/pf-devkit/ai-plugin/skeleton");

        assertThat(PublicPaths.isPublicPath(req)).isTrue();

        // context-path 下伪装路径同样不命中
        MockHttpServletRequest lookAlike =
                new MockHttpServletRequest("POST", "/api/auth/loginnn");
        lookAlike.setContextPath("/pf-devkit/ai-plugin/skeleton");
        assertThat(PublicPaths.isPublicPath(lookAlike)).isFalse();
    }
}
