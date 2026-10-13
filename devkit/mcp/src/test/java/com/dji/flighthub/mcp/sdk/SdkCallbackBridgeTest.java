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

package com.dji.flighthub.mcp.sdk;

import com.dji.flighthubsdk.FlightHubCallback.ResultCallback;
import com.dji.flighthubsdk.FlightHubCallback.ResultCallbackWithError;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SdkCallbackBridge 单元测试。
 * 覆盖：await 成功/超时/SdkException/通用异常，awaitVoid/awaitVoidRc 成功/错误路径，
 * serverMessage 有无两种错误分支，以及异常类构造器。
 *
 * @author DJI FlightHub SDK Team
 */
class SdkCallbackBridgeTest {

    // ==================== await(future, timeout) ====================

    @Test
    void await_shouldReturnResult_whenFutureCompletesNormally() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("ok");

        String result = SdkCallbackBridge.await(future, 5);

        assertEquals("ok", result);
    }

    @Test
    void await_shouldThrowSdkTimeoutException_whenFutureTimesOut() {
        CompletableFuture<String> future = new CompletableFuture<>(); // 永不完成

        assertThrows(SdkCallbackBridge.SdkTimeoutException.class,
                () -> SdkCallbackBridge.await(future, 0)); // 0s 超时立即触发
    }

    @Test
    void await_shouldRethrowSdkException_whenCauseIsSdkException() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new SdkCallbackBridge.SdkException(1001, "business error"));

        SdkCallbackBridge.SdkException ex = assertThrows(SdkCallbackBridge.SdkException.class,
                () -> SdkCallbackBridge.await(future, 5));

        assertEquals(1001, ex.getErrorCode());
        assertEquals("business error", ex.getMessage());
    }

    @Test
    void await_shouldWrapAsGenericSdkException_whenCauseIsOtherException() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("unexpected cause"));

        SdkCallbackBridge.SdkException ex = assertThrows(SdkCallbackBridge.SdkException.class,
                () -> SdkCallbackBridge.await(future, 5));

        assertEquals(-1, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("unexpected cause"));
    }

    // ==================== await(future) — 使用默认超时 ====================

    @Test
    void await_defaultTimeout_shouldReturnResult() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(42);

        int result = SdkCallbackBridge.await(future);

        assertEquals(42, result);
    }

    // ==================== awaitVoid(sdkCall) — ResultCallbackWithError ====================

    @Test
    void awaitVoid_shouldComplete_whenCallbackReportsSuccess() {
        // 构造成功的 FlightHubError（errorCode = SUCCESS）
        FlightHubError success = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);

        assertDoesNotThrow(() ->
                SdkCallbackBridge.awaitVoid((ResultCallbackWithError cb) -> cb.onResult(success))
        );
    }

    @Test
    void awaitVoid_shouldThrowSdkException_withServerMessage_whenCallbackReportsError() {
        // FlightHubError(sdkErrorCode, serverCode, errorCode, serverMessage)
        // getErrorCode() returns the 3rd param (errorCode = 1001)
        FlightHubError error = new FlightHubError(
                FlightHubSdkErrorCode.ERROR_OPERATION_FAILED, 500, 1001, "Server is unavailable");

        SdkCallbackBridge.SdkException ex = assertThrows(SdkCallbackBridge.SdkException.class,
                () -> SdkCallbackBridge.awaitVoid((ResultCallbackWithError cb) -> cb.onResult(error))
        );

        assertEquals(1001, ex.getErrorCode());
        assertEquals("Server is unavailable", ex.getMessage());
    }

    @Test
    void awaitVoid_shouldThrowSdkException_withFallbackMessage_whenServerMessageBlank() {
        // serverMessage 为空字符串时回退到 errorCode/serverCode 格式
        // getErrorCode() returns 3rd param (errorCode = 2001)
        FlightHubError error = new FlightHubError(
                FlightHubSdkErrorCode.ERROR_OPERATION_FAILED, 404, 2001, "");

        SdkCallbackBridge.SdkException ex = assertThrows(SdkCallbackBridge.SdkException.class,
                () -> SdkCallbackBridge.awaitVoid((ResultCallbackWithError cb) -> cb.onResult(error))
        );

        assertEquals(2001, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("errorCode=") || ex.getMessage().contains("server="),
                "Fallback message should contain error code info, got: " + ex.getMessage());
    }

    // ==================== awaitVoid(sdkCall, timeout) — 显式超时 ====================

    @Test
    void awaitVoid_withExplicitTimeout_shouldComplete_whenCallbackReportsSuccess() {
        FlightHubError success = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);

        assertDoesNotThrow(() ->
                SdkCallbackBridge.awaitVoid(
                        (ResultCallbackWithError cb) -> cb.onResult(success),
                        5
                )
        );
    }

    // ==================== awaitVoidRc(sdkCall) — ResultCallback ====================

    @Test
    void awaitVoidRc_shouldComplete_whenCallbackReportsSuccess() {
        FlightHubError success = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);

        assertDoesNotThrow(() ->
                SdkCallbackBridge.awaitVoidRc((ResultCallback cb) -> cb.onResult(success))
        );
    }

    @Test
    void awaitVoidRc_shouldThrowSdkException_withServerMessage_whenCallbackReportsError() {
        // getErrorCode() returns 3rd param (errorCode = 3001)
        FlightHubError error = new FlightHubError(
                FlightHubSdkErrorCode.ERROR_OPERATION_FAILED, 503, 3001, "Service down");

        SdkCallbackBridge.SdkException ex = assertThrows(SdkCallbackBridge.SdkException.class,
                () -> SdkCallbackBridge.awaitVoidRc((ResultCallback cb) -> cb.onResult(error))
        );

        assertEquals(3001, ex.getErrorCode());
        assertEquals("Service down", ex.getMessage());
    }

    @Test
    void awaitVoidRc_shouldThrowSdkException_withFallbackMessage_whenServerMessageBlank() {
        // getErrorCode() returns 3rd param (errorCode = 9999)
        FlightHubError error = new FlightHubError(
                FlightHubSdkErrorCode.ERROR_OPERATION_FAILED, 0, 9999, "");

        SdkCallbackBridge.SdkException ex = assertThrows(SdkCallbackBridge.SdkException.class,
                () -> SdkCallbackBridge.awaitVoidRc((ResultCallback cb) -> cb.onResult(error))
        );

        assertEquals(9999, ex.getErrorCode());
        assertNotNull(ex.getMessage());
    }

    // ==================== awaitVoidRc(sdkCall, timeout) — 显式超时 ====================

    @Test
    void awaitVoidRc_withExplicitTimeout_shouldComplete_whenSuccess() {
        FlightHubError success = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);

        assertDoesNotThrow(() ->
                SdkCallbackBridge.awaitVoidRc(
                        (ResultCallback cb) -> cb.onResult(success),
                        5
                )
        );
    }

    // ==================== SdkException ====================

    @Test
    void sdkException_shouldStoreErrorCode() {
        SdkCallbackBridge.SdkException ex = new SdkCallbackBridge.SdkException(42, "error msg");

        assertEquals(42, ex.getErrorCode());
        assertEquals("error msg", ex.getMessage());
    }

    @Test
    void sdkException_shouldExtendRuntimeException() {
        SdkCallbackBridge.SdkException ex = new SdkCallbackBridge.SdkException(0, "test");

        assertTrue(ex instanceof RuntimeException);
    }

    // ==================== SdkTimeoutException ====================

    @Test
    void sdkTimeoutException_shouldStoreMessage() {
        SdkCallbackBridge.SdkTimeoutException ex =
                new SdkCallbackBridge.SdkTimeoutException("SDK callback timeout after 15s");

        assertEquals("SDK callback timeout after 15s", ex.getMessage());
    }

    @Test
    void sdkTimeoutException_shouldExtendRuntimeException() {
        SdkCallbackBridge.SdkTimeoutException ex =
                new SdkCallbackBridge.SdkTimeoutException("timeout");

        assertTrue(ex instanceof RuntimeException);
    }
}
