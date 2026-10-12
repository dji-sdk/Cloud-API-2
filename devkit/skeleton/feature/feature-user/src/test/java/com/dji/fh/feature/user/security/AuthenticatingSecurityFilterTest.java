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

package com.dji.fh.feature.user.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dji.fh.kernel.spi.auth.AuthenticationException;
import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * AuthenticatingSecurityFilter 单元测试。
 *
 * @author DJI DevKit Team
 */
class AuthenticatingSecurityFilterTest {

    private AuthenticationProvider provider;
    private AuthenticatingSecurityFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        provider = mock(AuthenticationProvider.class);
        filter = new AuthenticatingSecurityFilter(provider);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void doFilter_authenticatedRequest_shouldSetContext() throws Exception {
        SecurityContext ctx = new SecurityContext() {
            @Override
            public String principalId() {
                return "user-1";
            }

            @Override
            public Set<String> roles() {
                return Set.of("user");
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of("userId", 1L);
            }
        };
        when(provider.authenticate(request)).thenReturn(Optional.of(ctx));

        AtomicReference<SecurityContext> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(SecurityContextHolder.current());

        filter.doFilter(request, response, chain);

        assertThat(captured.get()).isSameAs(ctx);
        assertThat(SecurityContextHolder.current().isAuthenticated()).isFalse();
    }

    @Test
    void doFilter_noAuthentication_shouldSetNoOpContext() throws Exception {
        when(provider.authenticate(request)).thenReturn(Optional.empty());

        AtomicReference<SecurityContext> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(SecurityContextHolder.current());

        filter.doFilter(request, response, chain);

        assertThat(captured.get().isAuthenticated()).isFalse();
        assertThat(captured.get().principalId()).isEqualTo("anonymous");
    }

    @Test
    void doFilter_providerThrowsException_shouldWrite401Json() throws Exception {
        when(provider.authenticate(request))
                .thenThrow(new AuthenticationException("token expired"));

        AtomicReference<SecurityContext> captured = new AtomicReference<>();
        FilterChain chain = mock(FilterChain.class);

        // mock response 的 getWriter() 默认返回 null，需桩出真实 Writer 以捕获输出
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, chain);

        // fail-closed：不执行链路，上下文不注入任何身份
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        assertThat(captured.get()).isNull();
        assertThat(SecurityContextHolder.current().isAuthenticated()).isFalse();

        // 直接写 401 JSON，且响应体不回显 JWT 解析细节
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertThat(body.toString())
                .contains("\"code\":\"invalid_token\"")
                .contains("无效或已过期的访问凭证，请重新登录")
                .doesNotContain("token expired");
    }

    @Test
    void doFilter_publicPath_shouldShortCircuitWithoutAuthentication() throws Exception {
        MockHttpServletRequest publicReq =
                new MockHttpServletRequest("POST", "/api/auth/login");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(publicReq, new MockHttpServletResponse(), chain);

        // 公开路径不触发 provider 鉴权解析，直接放行
        verify(provider, never()).authenticate(org.mockito.ArgumentMatchers.any());
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doFilter_nonPublicPath_withStaleToken_shouldStill401() throws Exception {
        when(provider.authenticate(request))
                .thenThrow(new AuthenticationException("token expired"));

        FilterChain chain = mock(FilterChain.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, chain);

        // 非公开路径携带失效 token 仍 fail-closed 401
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doFilter_shouldClearContextAfterChain() throws Exception {
        SecurityContext ctx = new SecurityContext() {
            @Override
            public String principalId() {
                return "user-2";
            }

            @Override
            public Set<String> roles() {
                return Set.of("admin");
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of();
            }
        };
        when(provider.authenticate(request)).thenReturn(Optional.of(ctx));

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.current().isAuthenticated()).isFalse();
    }

    @Test
    void doFilter_chainThrowsException_shouldStillClearContext() throws Exception {
        when(provider.authenticate(request)).thenReturn(Optional.empty());

        FilterChain chain = (req, res) -> {
            throw new RuntimeException("chain error");
        };

        try {
            filter.doFilter(request, response, chain);
        } catch (RuntimeException ignored) {
            // expected
        }

        assertThat(SecurityContextHolder.current().isAuthenticated()).isFalse();
    }
}
