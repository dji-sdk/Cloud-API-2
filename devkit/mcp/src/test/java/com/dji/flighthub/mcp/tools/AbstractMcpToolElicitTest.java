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

package com.dji.flighthub.mcp.tools;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.elicit.ElicitResult;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AbstractMcpTool 人工确认（elicitation）相关方法单元测试。
 * 通过 ThreadLocal 绑定伪 ClientBridge，覆盖 accept/decline/cancel/timeout 四种语义，
 * 以及无 bridge / client 不支持时回退 confirm 参数校验的行为。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class AbstractMcpToolElicitTest {

    private static final String ACTION = "TEST_ACTION";
    private static final String TARGET = "DRONE-X";
    private static final String MESSAGE = "Approve test action?";

    /** 最小具体 tool，仅为测试基类受保护方法 */
    private static class TestTool extends AbstractMcpTool {
        TestTool(SessionManager sessionManager) {
            super(sessionManager);
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "test_tool");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }

        Map<String, Object> callRequireHumanConfirm(Map<String, Object> args) {
            return requireHumanConfirm(args, ACTION, TARGET, MESSAGE);
        }

        boolean callElicitationAvailable() {
            return elicitationAvailable();
        }

        Map<String, Object> callRequireInput(Map<String, Object> schema) {
            return requireInput(MESSAGE, schema);
        }
    }

    private TestTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        tool = new TestTool(new SessionManager(properties));
    }

    @Test
    void requireHumanConfirm_shouldPass_whenElicitationAccepted() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.accept(null)));

        Map<String, Object> error = tool.callRequireHumanConfirm(Map.of());

        assertNull(error, "ACCEPT 应放行（返回 null）");
    }

    @Test
    void requireHumanConfirm_shouldReturnError_whenElicitationDeclined() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.decline()));

        Map<String, Object> error = tool.callRequireHumanConfirm(Map.of());

        assertNotNull(error);
        assertEquals(false, error.get("success"));
        assertTrue(error.get("error_message").toString().contains("declined"));
    }

    @Test
    void requireHumanConfirm_shouldReturnError_whenElicitationCancelled() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.cancel()));

        Map<String, Object> error = tool.callRequireHumanConfirm(Map.of());

        assertNotNull(error);
        assertTrue(error.get("error_message").toString().contains("cancelled"));
    }

    @Test
    void requireHumanConfirm_shouldReturnError_whenElicitationTimedOut() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.timeout()));

        Map<String, Object> error = tool.callRequireHumanConfirm(Map.of());

        assertNotNull(error);
        assertTrue(error.get("error_message").toString().contains("timed out"));
    }

    @Test
    void requireHumanConfirm_shouldFallbackToConfirmParam_whenNoBridge() {
        // 未绑定 bridge（如单元测试直接调 execute）→ 回退 confirm 参数校验
        Map<String, Object> error = tool.callRequireHumanConfirm(Map.of("confirm", true));
        assertNull(error, "confirm=true 回退分支应放行");

        Map<String, Object> denied = tool.callRequireHumanConfirm(Map.of("confirm", false));
        assertNotNull(denied);
        assertTrue(denied.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void requireHumanConfirm_shouldAllowConfirmTrue_whenRemoteClientLacksElicitation() {
        // 远程 bridge（SSE）但 client 未声明 elicitation → confirm=true 时应放行（Chatbot 场景）
        ClientBridgeHolder.set(fakeBridge(false, true, ElicitResult.accept(null)));

        // confirm=true 应放行（依赖 system prompt 引导 AI 先 dry_run 再确认）
        Map<String, Object> passResult = tool.callRequireHumanConfirm(Map.of("confirm", true));
        assertNull(passResult, "SSE 远程模式下 confirm=true 应放行");

        // confirm 未传或 false 时应拒绝并提示使用 dry_run + confirm 流程
        Map<String, Object> denied = tool.callRequireHumanConfirm(Map.of("confirm", false));
        assertNotNull(denied, "SSE 远程模式下 confirm=false 应拒绝");
        assertTrue(denied.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void requireHumanConfirm_shouldFallbackToConfirm_whenLocalClientLacksElicitation() {
        // 本地 bridge（stdio）且 client 未声明 elicitation → 允许 fallback 到 confirm 参数
        ClientBridgeHolder.set(fakeBridge(false, false, ElicitResult.accept(null)));

        // confirm=true 时应放行
        Map<String, Object> passResult = tool.callRequireHumanConfirm(Map.of("confirm", true));
        assertNull(passResult, "stdio 本地模式下 confirm=true 应放行");

        // confirm=false 时应拒绝（但错误信息是 Confirmation required，不是 elicitation）
        Map<String, Object> denied = tool.callRequireHumanConfirm(Map.of("confirm", false));
        assertNotNull(denied, "stdio 本地模式下 confirm=false 应拒绝");
        assertTrue(denied.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void elicitationAvailable_shouldReflectBridgeAndCapability() {
        assertFalse(tool.callElicitationAvailable(), "无 bridge 时不可用");

        ClientBridgeHolder.set(fakeBridge(false, ElicitResult.accept(null)));
        assertFalse(tool.callElicitationAvailable(), "client 不支持时不可用");

        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.accept(null)));
        assertTrue(tool.callElicitationAvailable(), "bridge + 能力齐备时可用");
    }

    @Test
    void requireInput_shouldReturnContent_whenAccepted() {
        Map<String, Object> filled = Map.of("latitude", 22.5, "longitude", 113.9, "height", 50.0);
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.accept(filled)));

        Map<String, Object> content = tool.callRequireInput(Map.of("type", "object"));

        assertNotNull(content, "ACCEPT 应返回真人填写的 content");
        assertEquals(22.5, content.get("latitude"));
        assertEquals(113.9, content.get("longitude"));
        assertEquals(50.0, content.get("height"));
    }

    @Test
    void requireInput_shouldReturnNull_whenDeclined() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.decline()));
        assertNull(tool.callRequireInput(Map.of("type", "object")), "DECLINE 应返回 null");
    }

    @Test
    void requireInput_shouldReturnNull_whenCancelled() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.cancel()));
        assertNull(tool.callRequireInput(Map.of("type", "object")), "CANCEL 应返回 null");
    }

    @Test
    void requireInput_shouldReturnNull_whenTimedOut() {
        ClientBridgeHolder.set(fakeBridge(true, ElicitResult.timeout()));
        assertNull(tool.callRequireInput(Map.of("type", "object")), "TIMEOUT 应返回 null");
    }

    @Test
    void requireInput_shouldReturnNull_whenNoBridge() {
        assertNull(tool.callRequireInput(Map.of("type", "object")), "无 bridge 应返回 null");
    }

    @Test
    void requireInput_shouldReturnNull_whenClientLacksElicitation() {
        ClientBridgeHolder.set(fakeBridge(false, ElicitResult.accept(Map.of("latitude", 1.0))));
        assertNull(tool.callRequireInput(Map.of("type", "object")),
                "client 不支持 elicitation 应返回 null");
    }
}
