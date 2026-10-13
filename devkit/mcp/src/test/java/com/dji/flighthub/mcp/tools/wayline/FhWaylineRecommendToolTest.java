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

package com.dji.flighthub.mcp.tools.wayline;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * FhWaylineRecommendTool 单元测试。
 * 覆盖 definition、session 校验、必填参数校验、Mock 过滤/排序、距离阈值、类型过滤、错误分支。
 *
 * @author DJI FlightHub SDK Team
 */
class FhWaylineRecommendToolTest {

    private static final String DOCK_SN = "AIRPORT-7CQDKR1020003V";

    private SessionManager sessionManager;
    private FhWaylineRecommendTool tool;

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
        tool = new FhWaylineRecommendTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema_whenInvoked() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_wayline_recommend", def.get("name"));
        assertNotNull(def.get("description"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertNotNull(props.get("dock_sn"));
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("dock_sn", DOCK_SN));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(new HashMap<>());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: dock_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterAndSort_whenMockDefault() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("dock_sn", DOCK_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DOCK_SN, data.get("dock_sn"));
        assertEquals("0-67-0", data.get("drone_model_key"));
        assertEquals(10.0, data.get("max_distance_km"));

        List<Map<String, Object>> recommended = (List<Map<String, Object>>) data.get("recommended");
        assertEquals(1, recommended.size());
        assertEquals("wl-A", recommended.get(0).get("wayline_uuid"));
        assertNotNull(recommended.get(0).get("distance_km"));

        List<Map<String, Object>> excluded = (List<Map<String, Object>>) data.get("excluded");
        assertEquals(2, excluded.size());
        // 排除原因文案：一个距离、一个机型
        String reasons = excluded.get(0).get("exclude_reason") + " | "
                + excluded.get(1).get("exclude_reason");
        assertTrue(reasons.contains("exceeds max_distance_km"));
        assertTrue(reasons.contains("does not match dock drone"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldExcludeMatchedWayline_whenMaxDistanceShrunk() {
        sessionManager.createSession("uuid", "ws");

        // 把距离阈值压到 1km，wl-A（~3km）也会被排除
        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "max_distance_km", 1.0
        ));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> recommended = (List<Map<String, Object>>) data.get("recommended");
        List<Map<String, Object>> excluded = (List<Map<String, Object>>) data.get("excluded");
        assertTrue(recommended.isEmpty());
        // wl-A、wl-B 都因距离被排除，wl-C 因机型被排除
        assertEquals(3, excluded.size());
        boolean aExcluded = excluded.stream()
                .anyMatch(e -> "wl-A".equals(e.get("wayline_uuid"))
                        && e.get("exclude_reason").toString().contains("exceeds max_distance_km"));
        assertTrue(aExcluded);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterByWaylineTypes_whenProvided() {
        sessionManager.createSession("uuid", "ws");

        // wl-A、wl-B 是 type 0；wl-C 是 type 1。传 [1] 则只剩 wl-C 候选，但 wl-C 机型不匹配 → 全 excluded
        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "wayline_types", List.of(1)
        ));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> recommended = (List<Map<String, Object>>) data.get("recommended");
        List<Map<String, Object>> excluded = (List<Map<String, Object>>) data.get("excluded");
        assertTrue(recommended.isEmpty());
        // wl-A、wl-B 因类型被排除；wl-C 因机型被排除
        assertEquals(3, excluded.size());
        boolean aTypeExcluded = excluded.stream()
                .anyMatch(e -> "wl-A".equals(e.get("wayline_uuid"))
                        && e.get("exclude_reason").toString().contains("no intersection"));
        assertTrue(aTypeExcluded);
    }

    @Test
    void execute_shouldReturnError_whenMaxDistanceIsNaN() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "max_distance_km", Double.NaN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid max_distance_km"));
    }

    @Test
    void execute_shouldReturnError_whenMaxDistanceIsInfinity() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "max_distance_km", Double.POSITIVE_INFINITY
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid max_distance_km"));
    }

    @Test
    void execute_shouldReturnError_whenWaylineTypesEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "wayline_types", List.of()
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("empty list"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotEmitPlaceholderUuid_whenRecommendedEmpty() {
        sessionManager.createSession("uuid", "ws");

        // 距离阈值压到 1km：wl-A/wl-B 因距离排除，wl-C 因机型排除 → recommended 为空
        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "max_distance_km", 1.0
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> recommended = (List<Map<String, Object>>) data.get("recommended");
        assertTrue(recommended.isEmpty());

        // next_steps 不应包含字面占位符 <wayline_uuid>，且不应指向 fh_flight_task
        List<Map<String, Object>> nextSteps = (List<Map<String, Object>>) result.get("next_steps");
        assertNotNull(nextSteps);
        assertFalse(nextSteps.isEmpty());
        String nextStepsStr = nextSteps.toString();
        assertFalse(nextStepsStr.contains("<wayline_uuid>"),
                "next_steps must not carry placeholder uuid: " + nextStepsStr);
        assertFalse(nextStepsStr.contains("fh_flight_task"),
                "empty-recommend must not route to flight task: " + nextStepsStr);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldIncludeNameInNextSteps_whenRecommendedNonEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("dock_sn", DOCK_SN));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> recommended = (List<Map<String, Object>>) data.get("recommended");
        assertEquals(1, recommended.size());

        List<Map<String, Object>> nextSteps = (List<Map<String, Object>>) result.get("next_steps");
        assertNotNull(nextSteps);
        Map<String, Object> paramsHint = (Map<String, Object>) nextSteps.get(0).get("params_hint");
        // 必须包含 name 字段，否则 fh_flight_task(create) 会因缺 name 报错
        assertNotNull(paramsHint.get("name"),
                "next_steps params_hint must include 'name' for fh_flight_task create");
    }

    @Test
    void execute_shouldReturnError_whenRealPathSdkNotInitialized() {
        // isMockMode=false 且 workspace=null（未 initialize）→ real 路径应在入口返回明确错误，不进入 SDK 调用
        // 用 Mockito mock SdkService 精确控制 isMockMode=false、getWorkspace=null
        SdkService sdkService = mock(SdkService.class);
        when(sdkService.isMockMode()).thenReturn(false);
        when(sdkService.getWorkspace()).thenReturn(null);

        FhWaylineRecommendTool realTool = new FhWaylineRecommendTool(sessionManager, sdkService);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = realTool.execute(Map.of("dock_sn", DOCK_SN));

        assertEquals(false, result.get("success"));
        assertNotNull(result.get("error_message"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }
}
