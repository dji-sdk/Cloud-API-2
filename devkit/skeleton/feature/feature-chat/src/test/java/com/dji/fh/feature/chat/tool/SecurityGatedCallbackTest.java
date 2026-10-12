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

package com.dji.fh.feature.chat.tool;

import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SecurityGatedCallback 单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>L0 READ_ONLY：直接委托给 delegate 执行</li>
 *   <li>L1 DEVICE_CONTROL：直接委托给 delegate 执行</li>
 *   <li>L2 FLIGHT_CONTROL：返回安全拦截错误信息（不抛异常）</li>
 *   <li>L3 EMERGENCY：返回安全拦截错误信息（不抛异常）</li>
 *   <li>getToolDefinition/getToolMetadata：委托给 delegate</li>
 * </ul>
 * </p>
 *
 * @author DJI DevKit Team
 */
@ExtendWith(MockitoExtension.class)
class SecurityGatedCallbackTest {

    private static final String TOOL_NAME = "fh_device_list";
    private static final String TOOL_DESCRIPTION = "List all drones";
    private static final String TOOL_INPUT_SCHEMA = "{}";
    private static final String TOOL_INPUT = "{\"drone_sn\":\"ABC123\"}";
    private static final String EXPECTED_RESULT = "{\"devices\":[]}";

    @Mock
    private ToolCallback delegate;

    /** 真实的 ToolDefinition，不依赖 mock，避免 setup 中产生 UnnecessaryStubbing。 */
    private ToolDefinition toolDefinition;

    @BeforeEach
    void setUp() {
        toolDefinition = ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(TOOL_DESCRIPTION)
                .inputSchema(TOOL_INPUT_SCHEMA)
                .build();
    }

    // -------------------------------------------------------------------------
    // L0 READ_ONLY: 直接放行
    // -------------------------------------------------------------------------

    @Test
    void call_shouldDelegate_whenReadOnly() {
        when(delegate.call(TOOL_INPUT)).thenReturn(EXPECTED_RESULT);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY);

        String result = callback.call(TOOL_INPUT);

        assertThat(result).isEqualTo(EXPECTED_RESULT);
        verify(delegate).call(TOOL_INPUT);
    }

    // -------------------------------------------------------------------------
    // L1 DEVICE_CONTROL: 需已认证 + 白名单角色
    // -------------------------------------------------------------------------

    @Test
    void call_shouldBlock_whenDeviceControlAndAnonymous() {
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.DEVICE_CONTROL);

        String result = callback.call(TOOL_INPUT);

        assertThat(result).contains("security_blocked");
        assertThat(result).contains("DEVICE_CONTROL");
        assertThat(result).contains("授权角色");
        verify(delegate, never()).call(TOOL_INPUT);
    }

    @Test
    void call_shouldBlock_whenDeviceControlAndRoleNotInWhitelist() {
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.DEVICE_CONTROL);
        SecurityContextHolder.set(new TestSecurityContext("user-1", Set.of("user")));
        try {
            String result = callback.call(TOOL_INPUT);
            assertThat(result).contains("security_blocked");
            assertThat(result).contains("授权角色");
            verify(delegate, never()).call(TOOL_INPUT);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    @Test
    void call_shouldDelegate_whenDeviceControlAndWhitelistRole() {
        when(delegate.call(TOOL_INPUT)).thenReturn(EXPECTED_RESULT);
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.DEVICE_CONTROL);
        SecurityContextHolder.set(new TestSecurityContext("admin-1", Set.of("admin")));
        try {
            String result = callback.call(TOOL_INPUT);
            assertThat(result).isEqualTo(EXPECTED_RESULT);
            verify(delegate).call(TOOL_INPUT);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    @Test
    void call_shouldDelegate_whenDeviceControlAndCustomWhitelistRole() {
        when(delegate.call(TOOL_INPUT)).thenReturn(EXPECTED_RESULT);
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(
                delegate, ChatSecurityLevel.DEVICE_CONTROL, Set.of("operator"));
        SecurityContextHolder.set(new TestSecurityContext("op-1", Set.of("operator")));
        try {
            String result = callback.call(TOOL_INPUT);
            assertThat(result).isEqualTo(EXPECTED_RESULT);
            verify(delegate).call(TOOL_INPUT);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    @Test
    void call_shouldBlockAll_whenDeviceControlAndEmptyWhitelist() {
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(
                delegate, ChatSecurityLevel.DEVICE_CONTROL, Set.of());
        SecurityContextHolder.set(new TestSecurityContext("admin-1", Set.of("admin")));
        try {
            String result = callback.call(TOOL_INPUT);
            assertThat(result).contains("security_blocked");
            verify(delegate, never()).call(TOOL_INPUT);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    // -------------------------------------------------------------------------
    // L2 FLIGHT_CONTROL: 返回错误信息（不委托给 delegate）
    // -------------------------------------------------------------------------

    @Test
    void call_shouldReturnSecurityError_whenFlightControl() {
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.FLIGHT_CONTROL);

        String result = callback.call(TOOL_INPUT);

        assertThat(result).contains("security_blocked");
        assertThat(result).contains("FLIGHT_CONTROL");
        assertThat(result).contains(TOOL_NAME);
        verify(delegate, never()).call(TOOL_INPUT);
    }

    // -------------------------------------------------------------------------
    // L3 EMERGENCY: 返回错误信息（不委托给 delegate）
    // -------------------------------------------------------------------------

    @Test
    void call_shouldReturnSecurityError_whenEmergency() {
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.EMERGENCY);

        String result = callback.call(TOOL_INPUT);

        assertThat(result).contains("security_blocked");
        assertThat(result).contains("EMERGENCY");
        assertThat(result).contains(TOOL_NAME);
        verify(delegate, never()).call(TOOL_INPUT);
    }

    // -------------------------------------------------------------------------
    // getToolDefinition / getToolMetadata: 委托给 delegate
    // -------------------------------------------------------------------------

    @Test
    void getToolDefinition_shouldDelegate() {
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY);

        ToolDefinition result = callback.getToolDefinition();

        assertThat(result.name()).isEqualTo(TOOL_NAME);
        assertThat(result.description()).isEqualTo(TOOL_DESCRIPTION);
        assertThat(result.inputSchema()).isEqualTo(TOOL_INPUT_SCHEMA);
        verify(delegate).getToolDefinition();
    }

    @Test
    void getToolMetadata_shouldDelegate() {
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY);

        callback.getToolMetadata();

        verify(delegate).getToolMetadata();
    }

    // -------------------------------------------------------------------------
    // L0 READ_ONLY TTL 缓存：命中时不重复调用 delegate
    // -------------------------------------------------------------------------

    @Test
    void call_shouldReturnCachedResult_whenReadOnlyAndCacheHit() {
        when(delegate.call(TOOL_INPUT)).thenReturn(EXPECTED_RESULT);
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY);

        // 第一次调用：填充缓存
        String first = callback.call(TOOL_INPUT);
        // 第二次调用：应命中缓存，不再调用 delegate
        String second = callback.call(TOOL_INPUT);

        assertThat(first).isEqualTo(EXPECTED_RESULT);
        assertThat(second).isEqualTo(EXPECTED_RESULT);
        // delegate 只被调用一次（第二次命中缓存）
        verify(delegate, org.mockito.Mockito.times(1)).call(TOOL_INPUT);
    }

    @Test
    void call_shouldNotCache_whenDeviceControlAndWhitelistRole() {
        when(delegate.call(TOOL_INPUT)).thenReturn(EXPECTED_RESULT);
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.DEVICE_CONTROL);
        SecurityContextHolder.set(new TestSecurityContext("admin-1", Set.of("admin")));
        try {
            callback.call(TOOL_INPUT);
            callback.call(TOOL_INPUT);
        } finally {
            SecurityContextHolder.clear();
        }

        // L1 不缓存，delegate 被调用两次
        verify(delegate, org.mockito.Mockito.times(2)).call(TOOL_INPUT);
    }

    // -------------------------------------------------------------------------
    // L0 READ_ONLY 缓存：错误响应不缓存，允许重试
    // -------------------------------------------------------------------------

    @Test
    void call_shouldNotCacheErrorResponse_whenReadOnly() {
        String errorResponse = "{\"status\":\"error\",\"code\":\"mcp_error\","
                + "\"message\":\"MCP Server 调用失败: JSON-RPC 响应超时\"}";
        when(delegate.call(TOOL_INPUT))
                .thenReturn(errorResponse)
                .thenReturn(EXPECTED_RESULT);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY);

        // 第一次调用返回错误 — 不应被缓存
        String first = callback.call(TOOL_INPUT);
        // 第二次调用应重新执行 delegate（非缓存命中）
        String second = callback.call(TOOL_INPUT);

        assertThat(first).contains("\"status\":\"error\"");
        assertThat(second).isEqualTo(EXPECTED_RESULT);
        // delegate 被调用两次（错误未缓存）
        verify(delegate, org.mockito.Mockito.times(2)).call(TOOL_INPUT);
    }

    @Test
    void call_shouldNotCacheJsonRpcError_whenReadOnly() {
        String jsonRpcError = "{\"error\": {\"code\": -32000, \"message\": \"timeout\"}}";
        when(delegate.call(TOOL_INPUT))
                .thenReturn(jsonRpcError)
                .thenReturn(EXPECTED_RESULT);
        SecurityGatedCallback callback = new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY);

        String first = callback.call(TOOL_INPUT);
        String second = callback.call(TOOL_INPUT);

        assertThat(first).contains("\"error\":");
        assertThat(second).isEqualTo(EXPECTED_RESULT);
        verify(delegate, org.mockito.Mockito.times(2)).call(TOOL_INPUT);
    }


    // -------------------------------------------------------------------------
    // getSecurityLevel()：返回正确等级
    // -------------------------------------------------------------------------

    @Test
    void getSecurityLevel_shouldReturnCorrectLevel() {
        assertThat(new SecurityGatedCallback(delegate, ChatSecurityLevel.READ_ONLY).getSecurityLevel())
                .isEqualTo(ChatSecurityLevel.READ_ONLY);
        assertThat(new SecurityGatedCallback(delegate, ChatSecurityLevel.DEVICE_CONTROL).getSecurityLevel())
                .isEqualTo(ChatSecurityLevel.DEVICE_CONTROL);
        assertThat(new SecurityGatedCallback(delegate, ChatSecurityLevel.FLIGHT_CONTROL).getSecurityLevel())
                .isEqualTo(ChatSecurityLevel.FLIGHT_CONTROL);
        assertThat(new SecurityGatedCallback(delegate, ChatSecurityLevel.EMERGENCY).getSecurityLevel())
                .isEqualTo(ChatSecurityLevel.EMERGENCY);
    }

    /**
     * 测试用 SecurityContext 最小实现。
     */
    private record TestSecurityContext(String principal, Set<String> roles)
            implements com.dji.fh.kernel.spi.auth.SecurityContext {
        @Override
        public String principalId() {
            return principal;
        }

        @Override
        public Set<String> roles() {
            return roles;
        }

        @Override
        public Map<String, Object> attributes() {
            return Map.of();
        }
    }

}
