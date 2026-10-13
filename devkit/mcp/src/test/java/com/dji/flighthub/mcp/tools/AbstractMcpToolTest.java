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
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
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
 * AbstractMcpTool 公共方法单元测试。
 */
class AbstractMcpToolTest {

    /** 被测 Tool 实例（使用内部具体子类实现） */
    private TestMcpTool tool;

    /** 测试用 SessionContext */
    private SessionContext session;

    @BeforeEach
    void setUp() {
        SessionManager mockSessionManager = mock(SessionManager.class);
        tool = new TestMcpTool(mockSessionManager);
        session = new SessionContext("test-session", "ws-uuid", "test-workspace", 3);
    }

    /**
     * AbstractMcpTool 的具体测试子类，只实现抽象方法占位。
     */
    private static class TestMcpTool extends AbstractMcpTool {
        TestMcpTool(SessionManager sessionManager) {
            super(sessionManager);
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "test_tool");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return Map.of();
        }
    }

    // ==================== errorResult ====================

    @Test
    void errorResult_shouldReturnCorrectStructure() {
        Map<String, Object> result = AbstractMcpTool.errorResult("test error", "try again");

        assertEquals(false, result.get("success"));
        assertEquals("test error", result.get("error_message"));
        assertEquals("try again", result.get("suggestion"));
        assertEquals(3, result.size());
    }

    // ==================== successResult ====================

    @Test
    void successResult_shouldReturnCorrectStructure() {
        Map<String, Object> data = Map.of("key", "value");
        Map<String, Object> result = AbstractMcpTool.successResult(data, "ok");

        assertEquals(true, result.get("success"));
        assertEquals(data, result.get("data"));
        assertEquals("ok", result.get("message"));
        assertEquals(3, result.size());
    }

    // ==================== getStringArg ====================

    @Test
    void getStringArg_shouldReturnValueWhenPresent() {
        Map<String, Object> args = Map.of("name", "hello");
        assertEquals("hello", AbstractMcpTool.getStringArg(args, "name", "default"));
    }

    @Test
    void getStringArg_shouldReturnFallbackWhenMissing() {
        Map<String, Object> args = Map.of("other", "val");
        assertEquals("default", AbstractMcpTool.getStringArg(args, "name", "default"));
    }

    @Test
    void getStringArg_shouldReturnFallbackWhenBlank() {
        Map<String, Object> args = Map.of("name", "   ");
        assertEquals("default", AbstractMcpTool.getStringArg(args, "name", "default"));
    }

    @Test
    void getStringArg_shouldReturnFallbackWhenNull() {
        assertEquals("default", AbstractMcpTool.getStringArg(null, "name", "default"));
    }

    // ==================== getIntArg ====================

    @Test
    void getIntArg_shouldReturnNumberValue() {
        Map<String, Object> args = Map.of("port", 8080);
        assertEquals(8080, AbstractMcpTool.getIntArg(args, "port", 443));
    }

    @Test
    void getIntArg_shouldParseStringValue() {
        Map<String, Object> args = Map.of("port", "9090");
        assertEquals(9090, AbstractMcpTool.getIntArg(args, "port", 443));
    }

    @Test
    void getIntArg_shouldReturnFallbackForInvalidString() {
        Map<String, Object> args = Map.of("port", "not_a_number");
        assertEquals(443, AbstractMcpTool.getIntArg(args, "port", 443));
    }

    @Test
    void getIntArg_shouldReturnFallbackWhenMissing() {
        Map<String, Object> args = Map.of("other", 1);
        assertEquals(443, AbstractMcpTool.getIntArg(args, "port", 443));
    }

    // ==================== getBooleanArg ====================

    @Test
    void getBooleanArg_shouldReturnTrueWhenTrue() {
        Map<String, Object> args = Map.of("confirm", true);
        assertTrue(AbstractMcpTool.getBooleanArg(args, "confirm"));
    }

    @Test
    void getBooleanArg_shouldReturnFalseWhenFalse() {
        Map<String, Object> args = Map.of("confirm", false);
        assertFalse(AbstractMcpTool.getBooleanArg(args, "confirm"));
    }

    @Test
    void getBooleanArg_shouldReturnFalseWhenMissing() {
        Map<String, Object> args = Map.of("other", true);
        assertFalse(AbstractMcpTool.getBooleanArg(args, "confirm"));
    }

    @Test
    void getBooleanArg_shouldReturnFalseForNullArgs() {
        assertFalse(AbstractMcpTool.getBooleanArg(null, "confirm"));
    }

    // ==================== requireControlAuthority ====================

    @Test
    void requireControlAuthority_shouldReturnNullWhenGranted() {
        session.setActiveControlDevice("DRONE-001");
        Map<String, Object> result = tool.requireControlAuthority(session, "DRONE-001");
        assertNull(result);
    }

    @Test
    void requireControlAuthority_shouldReturnErrorWhenNotGranted() {
        Map<String, Object> result = tool.requireControlAuthority(session, "DRONE-001");
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertTrue(result.get("suggestion").toString().contains("fh_control_acquire"));
    }

    // ==================== requireDrcLink ====================

    @Test
    void requireDrcLink_shouldReturnNullWhenConnected() {
        session.setActiveDrcDevice("DRONE-001");
        Map<String, Object> result = tool.requireDrcLink(session, "DRONE-001");
        assertNull(result);
    }

    @Test
    void requireDrcLink_shouldReturnErrorWhenNotConnected() {
        Map<String, Object> result = tool.requireDrcLink(session, "DRONE-001");
        assertNotNull(result);
        assertTrue(result.get("suggestion").toString().contains("fh_drc_connect"));
    }

    // ==================== requireConfirm ====================

    @Test
    void requireConfirm_shouldReturnNullWhenConfirmed() {
        Map<String, Object> result = tool.requireConfirm(Map.of("confirm", true), "takeoff", "DRONE-001");
        assertNull(result);
    }

    @Test
    void requireConfirm_shouldReturnErrorWhenNotConfirmed() {
        Map<String, Object> result = tool.requireConfirm(Map.of(), "takeoff", "DRONE-001");
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    // ==================== getDoubleArg ====================

    @Test
    void getDoubleArg_shouldParseNumber() {
        assertEquals(22.5, AbstractMcpTool.getDoubleArg(Map.of("lat", 22.5), "lat", 0.0));
    }

    @Test
    void getDoubleArg_shouldReturnFallback() {
        assertEquals(0.0, AbstractMcpTool.getDoubleArg(Map.of(), "lat", 0.0));
    }

    // ==================== toDouble ====================

    @Test
    void toDouble_shouldParseNumber() {
        assertEquals(3.14, AbstractMcpTool.toDouble(3.14, 0.0));
    }

    @Test
    void toDouble_shouldParseStringWithWhitespace() {
        assertEquals(22.5, AbstractMcpTool.toDouble("  22.5  ", 0.0));
    }

    @Test
    void toDouble_shouldReturnFallbackForNull() {
        assertEquals(-1.0, AbstractMcpTool.toDouble(null, -1.0));
    }

    @Test
    void toDouble_shouldReturnFallbackForBlankString() {
        assertEquals(-1.0, AbstractMcpTool.toDouble("   ", -1.0));
    }

    @Test
    void toDouble_shouldReturnFallbackForInvalidString() {
        assertEquals(0.0, AbstractMcpTool.toDouble("abc", 0.0));
    }

    // ==================== requiresHumanConfirmation ====================

    @Test
    void requiresHumanConfirmation_shouldReturnFalseByDefault() {
        assertFalse(tool.requiresHumanConfirmation());
    }

    // ==================== getLongArg ====================

    @Test
    void getLongArg_shouldReturnLongValue() {
        Map<String, Object> args = Map.of("ts", 1700000000L);
        assertEquals(1700000000L, AbstractMcpTool.getLongArg(args, "ts", 0L));
    }

    @Test
    void getLongArg_shouldParseStringValue() {
        Map<String, Object> args = Map.of("ts", "1700000000");
        assertEquals(1700000000L, AbstractMcpTool.getLongArg(args, "ts", 0L));
    }

    @Test
    void getLongArg_shouldReturnFallbackForInvalidString() {
        Map<String, Object> args = Map.of("ts", "not_a_number");
        assertEquals(-1L, AbstractMcpTool.getLongArg(args, "ts", -1L));
    }

    @Test
    void getLongArg_shouldReturnFallbackWhenMissing() {
        assertEquals(99L, AbstractMcpTool.getLongArg(Map.of(), "ts", 99L));
    }

    @Test
    void getLongArg_shouldReturnFallbackForNullArgs() {
        assertEquals(0L, AbstractMcpTool.getLongArg(null, "ts", 0L));
    }

    // ==================== getStringListArg ====================

    @Test
    void getStringListArg_shouldReturnListOfStrings() {
        Map<String, Object> args = Map.of("sns", List.of("SN-001", "SN-002"));
        List<String> result = AbstractMcpTool.getStringListArg(args, "sns");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("SN-001"));
    }

    @Test
    void getStringListArg_shouldReturnNullWhenMissing() {
        assertNull(AbstractMcpTool.getStringListArg(Map.of(), "sns"));
    }

    @Test
    void getStringListArg_shouldFilterBlanks() {
        Map<String, Object> args = Map.of("sns", List.of("SN-001", "", "   ", "SN-002"));
        List<String> result = AbstractMcpTool.getStringListArg(args, "sns");
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getStringListArg_shouldReturnNullForNullArgs() {
        assertNull(AbstractMcpTool.getStringListArg(null, "sns"));
    }

    // ==================== parseStringList ====================

    @Test
    void parseStringList_shouldReturnListFromJavaList() {
        Map<String, Object> args = Map.of("items", List.of("a", "b", "c"));
        List<String> result = AbstractMcpTool.parseStringList(args, "items");
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void parseStringList_shouldReturnNullWhenKeyMissing() {
        assertNull(AbstractMcpTool.parseStringList(Map.of(), "items"));
    }

    @Test
    void parseStringList_shouldReturnNullWhenNotAList() {
        Map<String, Object> args = Map.of("items", "not_a_list");
        assertNull(AbstractMcpTool.parseStringList(args, "items"));
    }

    @Test
    void parseStringList_shouldReturnNullWhenAllBlanks() {
        Map<String, Object> args = Map.of("items", List.of("  ", ""));
        assertNull(AbstractMcpTool.parseStringList(args, "items"));
    }

    @Test
    void parseStringList_shouldReturnNullForEmptyList() {
        Map<String, Object> args = Map.of("items", List.of());
        assertNull(AbstractMcpTool.parseStringList(args, "items"));
    }

    // ==================== enforceSecurityLevel ====================

    @Test
    void enforceSecurityLevel_shouldReturnNullWhenAllowed() {
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(3); // allows CRITICAL
        SessionManager realManager = new SessionManager(props);
        realManager.createSession("uuid", "ws");
        TestMcpTool realTool = new TestMcpTool(realManager);

        assertNull(realTool.enforceSecurityLevel(SecurityLevel.FLIGHT));
    }

    @Test
    void enforceSecurityLevel_shouldReturnErrorWhenDenied() {
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(0); // only READ_ONLY
        SessionManager realManager = new SessionManager(props);
        realManager.createSession("uuid", "ws");
        TestMcpTool realTool = new TestMcpTool(realManager);

        Map<String, Object> result = realTool.enforceSecurityLevel(SecurityLevel.FLIGHT);
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void enforceSecurityLevel_shouldReturnErrorWhenNoSession() {
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(3);
        SessionManager realManager = new SessionManager(props);
        // no session created
        TestMcpTool realTool = new TestMcpTool(realManager);

        Map<String, Object> result = realTool.enforceSecurityLevel(SecurityLevel.READ_ONLY);
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ==================== isDryRun ====================

    @Test
    void isDryRun_shouldReturnTrueWhenSet() {
        assertTrue(AbstractMcpTool.isDryRun(Map.of("dry_run", true)));
    }

    @Test
    void isDryRun_shouldReturnFalseWhenAbsent() {
        assertFalse(AbstractMcpTool.isDryRun(Map.of()));
    }

    @Test
    void isDryRun_shouldReturnFalseWhenFalse() {
        assertFalse(AbstractMcpTool.isDryRun(Map.of("dry_run", false)));
    }

    // ==================== dryRunResult ====================

    @Test
    @SuppressWarnings("unchecked")
    void dryRunResult_shouldReturnCorrectStructure() {
        Map<String, Object> preview = Map.of("tool", "fh_takeoff", "altitude", 20);
        Map<String, Object> result = AbstractMcpTool.dryRunResult("Would takeoff", preview);

        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        assertNotNull(result.get("description"));
        assertNotNull(result.get("would_execute"));
        assertNotNull(result.get("message"));
    }

    // ==================== buildDryRunPreview ====================

    @Test
    @SuppressWarnings("unchecked")
    void buildDryRunPreview_shouldReturnCorrectStructure() {
        Map<String, Object> params = Map.of("altitude", 30);
        Map<String, Object> preview = AbstractMcpTool.buildDryRunPreview(
                "fh_takeoff", "takeoff", "DRONE-001",
                params, List.of("SESSION", "CONTROL_AUTHORITY"),
                SecurityLevel.FLIGHT, true
        );

        assertEquals("fh_takeoff", preview.get("tool"));
        assertEquals("takeoff", preview.get("action"));
        assertEquals("DRONE-001", preview.get("target"));
        assertEquals(params, preview.get("parameters"));
        assertEquals("FLIGHT", preview.get("security_level"));
        assertEquals(true, preview.get("requires_confirm"));
    }

    // ==================== validateGpsCoordinates ====================

    @Test
    void validateGpsCoordinates_shouldReturnNullForValid() {
        assertNull(tool.validateGpsCoordinates(22.5, 113.5, 100.0));
    }

    @Test
    void validateGpsCoordinates_shouldReturnErrorForInvalidLat() {
        Map<String, Object> result = tool.validateGpsCoordinates(100.0, 113.5, 100.0);
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("latitude"));
    }

    @Test
    void validateGpsCoordinates_shouldReturnErrorForInvalidLng() {
        Map<String, Object> result = tool.validateGpsCoordinates(22.5, 200.0, 100.0);
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("longitude"));
    }

    @Test
    void validateGpsCoordinates_shouldReturnErrorForInvalidHeight() {
        Map<String, Object> result = tool.validateGpsCoordinates(22.5, 113.5, 1.0);
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("height"));
    }

    @Test
    void validateGpsCoordinates_shouldReturnErrorForNegativeLat() {
        Map<String, Object> result = tool.validateGpsCoordinates(-100.0, 113.5, 100.0);
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("latitude"));
    }

    @Test
    void validateGpsCoordinates_shouldReturnErrorForTooHighHeight() {
        Map<String, Object> result = tool.validateGpsCoordinates(22.5, 113.5, 15000.0);
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("height"));
    }

    // ==================== noSessionError ====================

    @Test
    void noSessionError_shouldReturnCorrectError() {
        Map<String, Object> result = tool.noSessionError();
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
        assertTrue(result.get("suggestion").toString().contains("fh_connect"));
    }

    // ==================== haversineDistance ====================

    @Test
    void haversineDistance_shouldReturnZeroForSamePoint() {
        double distance = AbstractMcpTool.haversineDistance(22.5431, 113.9465, 22.5431, 113.9465);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void haversineDistance_shouldCalculateKnownDistance() {
        // 深圳宝安机场 → 深圳市中心（约 25km）
        double distance = AbstractMcpTool.haversineDistance(
                22.6394, 113.8145, 22.5431, 113.9465);
        assertTrue(distance > 15_000 && distance < 25_000,
                "深圳宝安机场到市中心距离应在 15~25km 之间，实际: " + distance);
    }

    @Test
    void haversineDistance_shouldReturnSmallDistanceForNearbyPoints() {
        // 同一建筑物内两点（约 100 米差异）
        double distance = AbstractMcpTool.haversineDistance(
                22.5431, 113.9465, 22.5440, 113.9465);
        assertTrue(distance > 50 && distance < 200,
                "100m 级别的近距离计算应准确，实际: " + distance);
    }

    @Test
    void haversineDistance_shouldHandleCrossEquator() {
        double distance = AbstractMcpTool.haversineDistance(
                1.0, 103.0, -1.0, 103.0);
        assertTrue(distance > 200_000 && distance < 250_000,
                "跨赤道 2 度距离约 222km，实际: " + distance);
    }
}
