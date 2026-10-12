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

package com.dji.flighthub.mcp.security;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.OperatorTokenAuthenticator;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.drc.FhDrcConnectTool;
import com.dji.flighthub.mcp.tools.drc.FhEmergencyStopTool;
import com.dji.flighthub.mcp.tools.drc.FhTakeoffTool;
import com.dji.flighthub.mcp.tools.firmware.FhFirmwareTool;
import com.dji.flighthub.mcp.tools.log.FhLogTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityLevel 强制拦截机制端到端测试。
 *
 * <p>验证核心语义：
 * <ul>
 *   <li>session.maxSecurityLevel >= 工具/操作要求等级 → 允许（交由后续逻辑处理）</li>
 *   <li>session.maxSecurityLevel < 工具/操作要求等级 → 硬拒绝（confirm=true 也无法绕过）</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
class SecurityLevelEnforcementTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    // ==================== 工具级拦截（统一等级工具） ====================

    @Nested
    @DisplayName("工具级拦截 - FhDrcConnectTool (L0)")
    class DrcConnectL0Test {

        @Test
        @DisplayName("maxLevel=3: L0 工具允许执行（后续因缺控制权而失败，但不被安全等级拦截）")
        void execute_shouldPassSecurityCheck_whenMaxLevelSufficient() {
            FhDrcConnectTool tool = createDrcConnectTool(3);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);

            Map<String, Object> result = tool.execute(args);

            // 不应被安全等级拦截（可能因其他原因失败，如缺控制权）
            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"),
                        "Should NOT be rejected by security level when maxLevel=3");
            }
        }

        @Test
        @DisplayName("maxLevel=0: L0 工具允许执行（READ_ONLY 等级无限制）")
        void execute_shouldPassSecurityCheck_whenMaxLevelZero() {
            FhDrcConnectTool tool = createDrcConnectTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);

            Map<String, Object> result = tool.execute(args);

            // L0 工具不应被安全等级拦截
            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"),
                        "L0 tool should NOT be rejected by security level even when maxLevel=0");
            }
        }
    }

    @Nested
    @DisplayName("工具级拦截 - FhTakeoffTool (L2)")
    class TakeoffL2Test {

        @Test
        @DisplayName("maxLevel=3: L2 工具允许执行")
        void execute_shouldPassSecurityCheck_whenMaxLevel3() {
            FhTakeoffTool tool = createTakeoffTool(3);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);
            args.put("confirm", true);

            Map<String, Object> result = tool.execute(args);

            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"));
            }
        }

        @Test
        @DisplayName("maxLevel=1: L2 工具硬拒绝")
        void execute_shouldDeny_whenMaxLevel1() {
            FhTakeoffTool tool = createTakeoffTool(1);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);
            args.put("confirm", true);

            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("insufficient security level"));
            assertTrue(result.get("suggestion").toString().contains("L2"));
            assertTrue(result.get("suggestion").toString().contains("L1"));
        }

        @Test
        @DisplayName("maxLevel=2: L2 工具允许执行（边界值）")
        void execute_shouldPass_whenMaxLevelExactly2() {
            FhTakeoffTool tool = createTakeoffTool(2);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);
            args.put("confirm", true);

            Map<String, Object> result = tool.execute(args);

            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"),
                        "L2 tool should pass when maxLevel=2 (exact match)");
            }
        }
    }

    @Nested
    @DisplayName("工具级拦截 - FhEmergencyStopTool (L3)")
    class EmergencyStopL3Test {

        @Test
        @DisplayName("maxLevel=3: L3 工具允许执行")
        void execute_shouldPass_whenMaxLevel3() {
            FhEmergencyStopTool tool = createEmergencyStopTool(3);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);
            args.put("confirm", true);
            args.put("reason", "test");

            Map<String, Object> result = tool.execute(args);

            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"));
            }
        }

        @Test
        @DisplayName("maxLevel=2: L3 工具硬拒绝")
        void execute_shouldDeny_whenMaxLevel2() {
            FhEmergencyStopTool tool = createEmergencyStopTool(2);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);
            args.put("confirm", true);
            args.put("reason", "test");

            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("insufficient security level"));
            assertTrue(result.get("suggestion").toString().contains("L3"));
            assertTrue(result.get("suggestion").toString().contains("L2"));
        }

        @Test
        @DisplayName("maxLevel=0: L3 工具硬拒绝")
        void execute_shouldDeny_whenMaxLevel0() {
            FhEmergencyStopTool tool = createEmergencyStopTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("drone_sn", DRONE_SN);
            args.put("confirm", true);
            args.put("reason", "test");

            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("insufficient security level"));
        }
    }

    // ==================== Action 级拦截（混合等级工具） ====================

    @Nested
    @DisplayName("Action 级拦截 - FhFirmwareTool (L0/L1/L2)")
    class FirmwareMixedTest {

        @Test
        @DisplayName("maxLevel=0: L0 读操作允许执行")
        void executeCheckStatus_shouldPass_whenMaxLevel0() {
            FhFirmwareTool tool = createFirmwareTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "check_status");
            args.put("device_sns", List.of(DRONE_SN));

            Map<String, Object> result = tool.execute(args);

            // L0 操作应该通过（可能因 mock 或缺参数失败，但不是安全等级拦截）
            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"),
                        "L0 action should pass regardless of maxLevel");
            }
        }

        @Test
        @DisplayName("maxLevel=0: L1 upgrade 操作硬拒绝")
        void executeUpgrade_shouldDeny_whenMaxLevel0() {
            FhFirmwareTool tool = createFirmwareTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "upgrade");
            args.put("device_sns", List.of(DRONE_SN));
            args.put("confirm", true);

            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("insufficient security level"));
            assertTrue(result.get("suggestion").toString().contains("L1"));
        }

        @Test
        @DisplayName("maxLevel=1: L1 upgrade 操作允许（需 confirm）")
        void executeUpgrade_shouldPassSecurityCheck_whenMaxLevel1() {
            FhFirmwareTool tool = createFirmwareTool(1);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "upgrade");
            args.put("device_sns", List.of(DRONE_SN));
            // 不传 confirm，验证通过安全等级但被 confirm 门控拦截
            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            // 应该是 confirm 拦截，不是安全等级拦截
            assertTrue(result.get("error_message").toString().contains("Confirmation required"),
                    "Should be blocked by confirm gate, not security level");
        }

        @Test
        @DisplayName("maxLevel=1: L2 delete 操作硬拒绝")
        void executeDelete_shouldDeny_whenMaxLevel1() {
            FhFirmwareTool tool = createFirmwareTool(1);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "delete");
            args.put("file_id", "fw_001");
            args.put("confirm", true);

            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("insufficient security level"));
            assertTrue(result.get("suggestion").toString().contains("L2"));
        }

        @Test
        @DisplayName("maxLevel=2: L2 delete 操作允许（需 confirm）")
        void executeDelete_shouldPassSecurityCheck_whenMaxLevel2() {
            FhFirmwareTool tool = createFirmwareTool(2);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "delete");
            args.put("file_id", "fw_001");
            // 不传 confirm
            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("Confirmation required"),
                    "Should be blocked by confirm gate, not security level");
        }
    }

    @Nested
    @DisplayName("Action 级拦截 - FhLogTool (L0/L1)")
    class LogMixedTest {

        @Test
        @DisplayName("maxLevel=0: L0 query 操作允许执行")
        void executeQuery_shouldPass_whenMaxLevel0() {
            FhLogTool tool = createLogTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "query");
            args.put("device_sn", DRONE_SN);

            Map<String, Object> result = tool.execute(args);

            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"));
            }
        }

        @Test
        @DisplayName("maxLevel=0: L1 delete 操作硬拒绝")
        void executeDeleteOperate_shouldDeny_whenMaxLevel0() {
            FhLogTool tool = createLogTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "operate");
            args.put("report_id", "log_001");
            args.put("operation", "delete");
            args.put("confirm", true);

            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("insufficient security level"));
        }

        @Test
        @DisplayName("maxLevel=1: L1 delete 操作允许（需 confirm）")
        void executeDeleteOperate_shouldPassSecurityCheck_whenMaxLevel1() {
            FhLogTool tool = createLogTool(1);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "operate");
            args.put("report_id", "log_001");
            args.put("operation", "delete");
            // 不传 confirm
            Map<String, Object> result = tool.execute(args);

            assertEquals(false, result.get("success"));
            assertTrue(result.get("error_message").toString().contains("Confirmation required"),
                    "Should be blocked by confirm gate, not security level");
        }

        @Test
        @DisplayName("maxLevel=0: L0 download 操作允许执行")
        void executeDownloadOperate_shouldPass_whenMaxLevel0() {
            FhLogTool tool = createLogTool(0);

            Map<String, Object> args = new HashMap<>();
            args.put("action", "operate");
            args.put("report_id", "log_001");
            args.put("operation", "download");

            Map<String, Object> result = tool.execute(args);

            // download 是 L0，应该允许执行
            if (result.get("success").equals(false)) {
                String error = result.get("error_message").toString();
                assertFalse(error.contains("insufficient security level"));
            }
        }
    }

    // ==================== getSecurityLevel() 声明验证 ====================

    @Nested
    @DisplayName("getSecurityLevel() 声明正确性")
    class SecurityLevelDeclarationTest {

        @Test
        void getSecurityLevel_shouldReturnReadOnly_whenDrcConnectTool() {
            FhDrcConnectTool tool = createDrcConnectTool(3);
            assertEquals(SecurityLevel.READ_ONLY, tool.getSecurityLevel());
        }

        @Test
        void getSecurityLevel_shouldReturnFlight_whenTakeoffTool() {
            FhTakeoffTool tool = createTakeoffTool(3);
            assertEquals(SecurityLevel.FLIGHT, tool.getSecurityLevel());
        }

        @Test
        void getSecurityLevel_shouldReturnCritical_whenEmergencyStopTool() {
            FhEmergencyStopTool tool = createEmergencyStopTool(3);
            assertEquals(SecurityLevel.CRITICAL, tool.getSecurityLevel());
        }

        @Test
        void getSecurityLevel_shouldReturnReadOnly_whenFirmwareToolMixed() {
            FhFirmwareTool tool = createFirmwareTool(3);
            assertEquals(SecurityLevel.READ_ONLY, tool.getSecurityLevel());
        }

        @Test
        void getSecurityLevel_shouldReturnReadOnly_whenLogToolMixed() {
            FhLogTool tool = createLogTool(3);
            assertEquals(SecurityLevel.READ_ONLY, tool.getSecurityLevel());
        }
    }

    // ==================== SecurityLevel 枚举自身测试 ====================

    @Nested
    @DisplayName("SecurityLevel.isAllowedBy() 谓词")
    class SecurityLevelPredicateTest {

        @Test
        void isAllowedBy_shouldReturnTrue_whenLevelIsL0() {
            assertTrue(SecurityLevel.READ_ONLY.isAllowedBy(0));
            assertTrue(SecurityLevel.READ_ONLY.isAllowedBy(1));
            assertTrue(SecurityLevel.READ_ONLY.isAllowedBy(3));
        }

        @Test
        void isAllowedBy_shouldRequireL1Plus_whenLevelIsL1() {
            assertFalse(SecurityLevel.REVERSIBLE.isAllowedBy(0));
            assertTrue(SecurityLevel.REVERSIBLE.isAllowedBy(1));
            assertTrue(SecurityLevel.REVERSIBLE.isAllowedBy(2));
            assertTrue(SecurityLevel.REVERSIBLE.isAllowedBy(3));
        }

        @Test
        void isAllowedBy_shouldRequireL2Plus_whenLevelIsL2() {
            assertFalse(SecurityLevel.FLIGHT.isAllowedBy(0));
            assertFalse(SecurityLevel.FLIGHT.isAllowedBy(1));
            assertTrue(SecurityLevel.FLIGHT.isAllowedBy(2));
            assertTrue(SecurityLevel.FLIGHT.isAllowedBy(3));
        }

        @Test
        void isAllowedBy_shouldRequireL3Only_whenLevelIsL3() {
            assertFalse(SecurityLevel.CRITICAL.isAllowedBy(0));
            assertFalse(SecurityLevel.CRITICAL.isAllowedBy(1));
            assertFalse(SecurityLevel.CRITICAL.isAllowedBy(2));
            assertTrue(SecurityLevel.CRITICAL.isAllowedBy(3));
        }

        @Test
        @DisplayName("labelFor(int) 应返回正确的等级标签")
        void labelFor_shouldReturnCorrectLabel() {
            assertEquals("L0", SecurityLevel.labelFor(0));
            assertEquals("L1", SecurityLevel.labelFor(1));
            assertEquals("L2", SecurityLevel.labelFor(2));
            assertEquals("L3", SecurityLevel.labelFor(3));
        }

        @Test
        @DisplayName("labelFor(int) 未知等级应返回兜底格式")
        void labelFor_shouldReturnFallback_whenLevelUnknown() {
            assertEquals("L99", SecurityLevel.labelFor(99));
            assertEquals("L-1", SecurityLevel.labelFor(-1));
        }
    }

    // ==================== 工厂方法 ====================

    private FhDrcConnectTool createDrcConnectTool(int maxLevel) {
        SessionManager sm = createSessionManager(maxLevel);
        sm.createSession("uuid", "ws");
        return new FhDrcConnectTool(sm, new SdkService());
    }

    private FhTakeoffTool createTakeoffTool(int maxLevel) {
        SessionManager sm = createSessionManager(maxLevel);
        sm.createSession("uuid", "ws");
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(maxLevel);
        return new FhTakeoffTool(sm, new SdkService(), props);
    }

    private FhEmergencyStopTool createEmergencyStopTool(int maxLevel) {
        SessionManager sm = createSessionManager(maxLevel);
        sm.createSession("uuid", "ws");
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(maxLevel);
        return new FhEmergencyStopTool(sm, new SdkService(),
                new OperatorTokenAuthenticator(props));
    }

    private FhFirmwareTool createFirmwareTool(int maxLevel) {
        SessionManager sm = createSessionManager(maxLevel);
        sm.createSession("uuid", "ws");
        return new FhFirmwareTool(sm, new SdkService());
    }

    private FhLogTool createLogTool(int maxLevel) {
        SessionManager sm = createSessionManager(maxLevel);
        sm.createSession("uuid", "ws");
        return new FhLogTool(sm, new SdkService());
    }

    private SessionManager createSessionManager(int maxLevel) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(maxLevel);
        return new SessionManager(properties);
    }
}
