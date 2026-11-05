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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.elicit.ElicitResult;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FhLiveStartTool 单元测试。
 * 覆盖 session 校验、device_sn 校验、action 合法性校验、confirm 校验及 mock 模式各 action 分支。
 * 注意：本工具使用 device_sn（兼容无人机与机场），无需飞行控制权（L1 安全等级）。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhLiveStartToolTest {

    private static final String DEVICE_SN = "DRONE-LIVE-001";

    private SessionManager sessionManager;
    private FhLiveStartTool liveTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        liveTool = new FhLiveStartTool(sessionManager, sdkService,
                new ListenChannelSupport(new SubscriptionBuffer()));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action: invalid_action"));
    }

    @Test
    void execute_shouldReturnError_whenConfirmMissing() {
        // session 存在，参数合法，但未提供 confirm=true
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", false
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStartStream_whenMockMode() {
        // 验证 action=start 返回 streaming=true 且包含 stream_url
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals(true, data.get("streaming"));
        assertNotNull(data.get("stream_url"));
        assertTrue(data.get("stream_url").toString().contains(DEVICE_SN));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopStream_whenStopAction() {
        // 验证 action=stop 返回 streaming=false
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "stop",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals(false, data.get("streaming"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSetQuality_whenQualityAction() {
        // 验证 action=quality 返回指定画质
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "quality",
                "quality", "hd",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals("hd", data.get("quality"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidQuality() {
        // quality="4k" 不在允许列表中，应返回错误（I5 fix）
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "quality",
                "quality", "4k",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid quality: 4k"));
    }

    // ==================== Elicitation 人工确认测试 ====================

    @Test
    void execute_shouldExecute_whenElicitationAccepted() {
        // client 支持 elicitation 且真人 ACCEPT → 即使 confirm=false 也应执行
        sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", false
        ));

        assertEquals(true, result.get("success"), "真人接受后应执行，不依赖 confirm 参数");
    }

    @Test
    void execute_shouldBlock_whenElicitationDeclined() {
        // client 支持 elicitation 但真人 DECLINE → 即使 confirm=true 也必须拦截
        sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"), "真人拒绝后必须拦截");
        assertTrue(result.get("error_message").toString().contains("declined"));
    }

    @Test
    void execute_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = liveTool.getToolDefinition();

        assertEquals("fh_live_start", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    // ── switch_source 测试 ──

    @Test
    void execute_shouldReturnError_whenSwitchSourceMissingPayloadIndex() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "switch_source",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("payload_index"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSwitchSource_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "switch_source",
                "confirm", true,
                "payload_index", "53-0-0"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals("53-0-0", data.get("payload_index"));
        assertEquals(true, data.get("switched"));
        assertEquals(true, data.get("streaming"));
        assertNotNull(data.get("stream_url"));
        assertEquals(true, data.get("_mock"));
    }

    // ── video_id 多路拉流测试 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldIncludeVideoId_inMockStartResponse() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true,
                "video_id", DEVICE_SN + "/81-0-0/wide-0"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DEVICE_SN + "/81-0-0/wide-0", data.get("video_id"));
        assertTrue(data.get("stream_url").toString().contains("/81-0-0/wide-0"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUseDefaultVideoId_whenNotProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DEVICE_SN + "/81-0-0", data.get("video_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUseVideoIdWithPayloadIndex() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true,
                "payload_index", "53-0-0"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DEVICE_SN + "/53-0-0", data.get("video_id"));
    }

    // ── action enum 扩展测试 ──

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldIncludeSubscriptionActions() {
        Map<String, Object> def = liveTool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertTrue(enumValues.contains("subscribe_frames"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));

        // confirm 不在 required 列表中（只有 device_sn/action 是必填项）
        assertEquals(List.of("device_sn", "action"), schema.get("required"));
    }

    // ── subscribe_frames / events / listen_stop ──

    @Test
    void execute_shouldReturnError_whenSubscribeFramesHasNoSession() {
        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "subscribe_frames"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSubscribeFrames_inMockMode_withoutConfirm() {
        // 不传 confirm，不应被要求确认（READ_ONLY 订阅动作）
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "subscribe_frames"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("subscribe_frames", data.get("action"));
        assertEquals("frame_metadata", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsCalledWithNoActiveSubscription() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReadEvents_whenChannelActive() {
        sessionManager.createSession("uuid", "ws");
        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events",
                "channels", List.of("frame_metadata")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get("frame_metadata");
        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertTrue(events.get(0).containsKey("is_key_frame"));
        assertTrue(events.get(0).containsKey("frame_size"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterBySinceSeq_whenEventsCursorAdvanced() {
        sessionManager.createSession("uuid", "ws");
        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));
        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));

        Map<String, Object> first = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events",
                "channels", List.of("frame_metadata"),
                "limit", 1
        ));
        Map<String, Object> firstStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) first.get("data")).get("streams"))
                        .get("frame_metadata");
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> second = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events",
                "channels", List.of("frame_metadata"),
                "since_seq", lastSeq
        ));
        Map<String, Object> secondStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) second.get("data")).get("streams"))
                        .get("frame_metadata");
        List<Map<String, Object>> secondEvents = (List<Map<String, Object>>) secondStream.get("events");

        assertEquals(1, secondEvents.size());
        assertTrue((long) secondEvents.get(0).get("_seq") > lastSeq);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopExplicitChannel_whenChannelsGiven() {
        sessionManager.createSession("uuid", "ws");
        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_stop",
                "channels", List.of("frame_metadata")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_stop", data.get("action"));
        assertEquals(List.of("frame_metadata"), data.get("stopped"));
        assertTrue(((List<?>) data.get("failed")).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAllChannels_whenChannelsOmitted() {
        sessionManager.createSession("uuid", "ws");
        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_stop"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of("frame_metadata"), data.get("stopped"));

        Map<String, Object> after = liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    void execute_shouldNotLeakRegistration_whenSubscribeFramesRepeated() {
        // 重复订阅同一设备的 frame_metadata channel，注册表 key 相同（namespace::device_sn::channel）
        // 不应产生多条注册；listen_stop 一次即可全部清理，且 not_active 应为空。
        sessionManager.createSession("uuid", "ws");

        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));
        liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe_frames"));

        Map<String, Object> stop = liveTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_stop"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) stop.get("data");

        assertEquals(List.of("frame_metadata"), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
    }

    // ── 回归验证：4 个原始控制 action 的 confirm 门控未被削弱 ──

    @Test
    void execute_shouldStillRequireConfirm_forOriginalControlActions() {
        // subscribe_frames/events/listen_stop 绕过 confirm 不应影响 start/stop/quality/switch_source
        sessionManager.createSession("uuid", "ws");

        for (String action : List.of("start", "stop", "quality", "switch_source")) {
            Map<String, Object> args = action.equals("switch_source")
                    ? Map.of("device_sn", DEVICE_SN, "action", action, "confirm", false, "payload_index", "53-0-0")
                    : Map.of("device_sn", DEVICE_SN, "action", action, "confirm", false);

            Map<String, Object> result = liveTool.execute(args);

            assertEquals(false, result.get("success"),
                    "action=" + action + " should still require confirm=true");
            assertTrue(result.get("error_message").toString().contains("Confirmation required"),
                    "action=" + action + " unexpected error: " + result.get("error_message"));
        }
    }
}
