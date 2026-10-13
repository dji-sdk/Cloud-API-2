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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.FlightHubCallback.ResultCallback;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DrcModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DRC 自动重试机制单元测试。
 * 覆盖 executeWithDrcRetry 的核心路径：
 * <ul>
 *   <li>首次成功 → 直接返回</li>
 *   <li>首次 SdkException + 重连成功 + 重试成功 → 透明返回</li>
 *   <li>首次 SdkException + 重连成功 + 重试失败 → 返回重试错误</li>
 *   <li>首次 SdkException + 重连失败 → 返回重连错误</li>
 *   <li>首次 SdkTimeoutException → 不重试，直接返回超时错误</li>
 * </ul>
 */
class DrcRetryTest {

    private DrcRetryTestTool tool;
    private SessionContext session;
    private SdkService sdkService;
    private Drone mockDrone;
    private DrcModule mockDrcModule;

    @BeforeEach
    void setUp() {
        SessionManager mockSessionManager = mock(SessionManager.class);
        sdkService = mock(SdkService.class);
        mockDrone = mock(Drone.class);
        mockDrcModule = mock(DrcModule.class);

        when(sdkService.getDrone("DRONE-001")).thenReturn(mockDrone);
        when(mockDrone.getDrcModule()).thenReturn(mockDrcModule);

        tool = new DrcRetryTestTool(mockSessionManager, sdkService);
        session = new SessionContext("test-session", "ws-uuid", "test-workspace", 3);
        session.setActiveControlDevice("DRONE-001");
        session.setActiveDrcDevice("DRONE-001");
    }

    @Test
    void executeWithDrcRetry_shouldReturnDirectly_whenFirstCallSucceeds() {
        Map<String, Object> expected = Map.of("success", true, "data", "ok");

        Map<String, Object> result = tool.callRetry(session, "DRONE-001", sdkService, () -> expected);

        assertEquals(expected, result);
    }

    @Test
    void executeWithDrcRetry_shouldReconnectAndRetry_whenFirstCallThrowsSdkException() {
        AtomicInteger callCount = new AtomicInteger(0);
        Map<String, Object> successResult = Map.of("success", true, "data", "retried");

        // DRC 断开成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).disconnectDrcLink(any(ResultCallback.class));

        // DRC 重连成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).connectDrcLink(any(ResultCallback.class));

        Map<String, Object> result = tool.callRetry(session, "DRONE-001", sdkService, () -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new SdkCallbackBridge.SdkException(500, "DRC operation failed");
            }
            return successResult;
        });

        assertEquals(2, callCount.get());
        assertEquals(successResult, result);
    }

    @Test
    void executeWithDrcRetry_shouldReturnError_whenReconnectFails() {
        // DRC 断开成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).disconnectDrcLink(any(ResultCallback.class));

        // DRC 重连失败
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(false);
            when(error.getServerCode()).thenReturn(4001);
            when(error.getServerMessage()).thenReturn("DRC connection refused");
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).connectDrcLink(any(ResultCallback.class));

        AtomicInteger callCount = new AtomicInteger(0);
        Map<String, Object> result = tool.callRetry(session, "DRONE-001", sdkService, () -> {
            callCount.incrementAndGet();
            throw new SdkCallbackBridge.SdkException(500, "DRC operation failed");
        });

        assertEquals(1, callCount.get());
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("error_message").toString().contains("DRC"));
    }

    @Test
    void executeWithDrcRetry_shouldRethrowSdkException_whenRetryAlsoFails() {
        // DRC 断开成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).disconnectDrcLink(any(ResultCallback.class));

        // DRC 重连成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).connectDrcLink(any(ResultCallback.class));

        AtomicInteger callCount = new AtomicInteger(0);
        SdkCallbackBridge.SdkException thrown = assertThrows(
                SdkCallbackBridge.SdkException.class,
                () -> tool.callRetry(session, "DRONE-001", sdkService, () -> {
                    callCount.incrementAndGet();
                    throw new SdkCallbackBridge.SdkException(500, "Persistent DRC error");
                }));

        assertEquals(2, callCount.get());
        assertEquals(500, thrown.getErrorCode());
        assertTrue(thrown.getMessage().contains("Persistent DRC error"));
    }

    @Test
    void executeWithDrcRetry_shouldRethrowTimeout_whenTimeoutException() {
        AtomicInteger callCount = new AtomicInteger(0);
        SdkCallbackBridge.SdkTimeoutException thrown = assertThrows(
                SdkCallbackBridge.SdkTimeoutException.class,
                () -> tool.callRetry(session, "DRONE-001", sdkService, () -> {
                    callCount.incrementAndGet();
                    throw new SdkCallbackBridge.SdkTimeoutException("SDK callback timeout after 15s");
                }));

        assertEquals(1, callCount.get());
        assertTrue(thrown.getMessage().contains("timeout"));
    }

    @Test
    void executeWithDrcRetry_shouldClearDrcState_duringReconnect() {
        // DRC 断开成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).disconnectDrcLink(any(ResultCallback.class));

        // DRC 重连成功
        doAnswer(inv -> {
            ResultCallback cb = inv.getArgument(0);
            com.dji.flighthubsdk.bean.FlightHubError error = mock(com.dji.flighthubsdk.bean.FlightHubError.class);
            when(error.isSuccess()).thenReturn(true);
            cb.onResult(error);
            return null;
        }).when(mockDrcModule).connectDrcLink(any(ResultCallback.class));

        AtomicInteger callCount = new AtomicInteger(0);
        tool.callRetry(session, "DRONE-001", sdkService, () -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new SdkCallbackBridge.SdkException(500, "DRC error");
            }
            return Map.of("success", true);
        });

        // 重连后 session 应恢复 DRC 状态
        assertTrue(session.hasDrcLinkTo("DRONE-001"));
    }

    /**
     * 暴露 executeWithDrcRetry 方法用于测试的子类。
     */
    private static class DrcRetryTestTool extends AbstractMcpTool {

        private final SdkService sdkService;

        DrcRetryTestTool(SessionManager sessionManager, SdkService sdkService) {
            super(sessionManager);
            this.sdkService = sdkService;
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "drc_retry_test");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return Map.of();
        }

        Map<String, Object> callRetry(SessionContext session, String droneSn,
                                       SdkService sdkService,
                                       java.util.function.Supplier<Map<String, Object>> operation) {
            return executeWithDrcRetry(session, droneSn, sdkService, operation);
        }
    }
}
