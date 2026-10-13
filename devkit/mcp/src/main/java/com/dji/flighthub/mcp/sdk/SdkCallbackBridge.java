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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * 将 SDK 异步 callback 模式桥接为同步结果，供 MCP tool 使用。
 *
 * <p>SDK 模式：workspace.getDeviceList(callback) → callback.onDeviceList(devices, error)
 * <p>MCP 需要：Map result = getDeviceList() → 立即返回
 *
 * <p>用法示例：
 * <pre>
 * var future = new CompletableFuture&lt;DeviceListResult&gt;();
 * workspace.getDeviceList((devices, error) -> {
 *     if (error.getErrorCode() == 0) future.complete(devices);
 *     else future.completeExceptionally(new SdkException(error));
 * });
 * DeviceListResult result = SdkCallbackBridge.await(future, 10);
 * </pre>
 */
public class SdkCallbackBridge {

    /** SDK callback 默认超时时间（秒） */
    public static final int DEFAULT_TIMEOUT = 15;

    /**
     * 等待 CompletableFuture 完成，带超时控制。
     *
     * @param future 要等待的 future
     * @param timeoutSeconds 最大等待时间（秒）
     * @param <T> 返回值类型
     * @return future 的结果
     * @throws SdkTimeoutException 超时时抛出
     * @throws SdkException SDK 返回错误时抛出
     */
    public static <T> T await(CompletableFuture<T> future, int timeoutSeconds) {
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new SdkTimeoutException("SDK callback timeout after " + timeoutSeconds + "s");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof SdkException) {
                throw (SdkException) cause;
            }
            throw new SdkException(-1, "SDK call failed: " + e.getMessage());
        }
    }

    /**
     * 使用默认超时等待。
     */
    public static <T> T await(CompletableFuture<T> future) {
        return await(future, DEFAULT_TIMEOUT);
    }

    /**
     * 执行带 void 回调的 SDK 方法并同步等待结果。
     * 消除 "创建 future → 注册回调 → await" 的重复模板。
     *
     * <p>用法示例：
     * <pre>
     * SdkCallbackBridge.awaitVoid(cb -> drone.acquireFlightControlAuthority(cb));
     * </pre>
     *
     * @param sdkCall 接受一个 ResultCallbackWithError 回调的 SDK 方法引用
     * @throws SdkTimeoutException 超时时抛出
     * @throws SdkException SDK 返回错误时抛出
     */
    public static void awaitVoid(Consumer<ResultCallbackWithError> sdkCall) {
        awaitVoid(sdkCall, DEFAULT_TIMEOUT);
    }

    /**
     * 执行带 void 回调的 SDK 方法并同步等待结果（指定超时）。
     *
     * @param sdkCall        接受一个 ResultCallbackWithError 回调的 SDK 方法引用
     * @param timeoutSeconds 最大等待时间（秒）
     * @throws SdkTimeoutException 超时时抛出
     * @throws SdkException SDK 返回错误时抛出
     */
    public static void awaitVoid(Consumer<ResultCallbackWithError> sdkCall, int timeoutSeconds) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sdkCall.accept(error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                String message = (error.getServerMessage() != null && !error.getServerMessage().isBlank())
                        ? error.getServerMessage()
                        : "errorCode=" + error.getErrorCode() + ", server=" + error.getServerCode();
                future.completeExceptionally(new SdkException(error.getServerCode(), message));
            }
        });
        await(future, timeoutSeconds);
    }

    /**
     * 执行接受 ResultCallback 的 SDK 方法并同步等待结果（如 connectDrcLink、disconnectDrcLink）。
     *
     * @param sdkCall 接受一个 ResultCallback 回调的 SDK 方法引用
     * @throws SdkTimeoutException 超时时抛出
     * @throws SdkException SDK 返回错误时抛出
     */
    public static void awaitVoidRc(Consumer<ResultCallback> sdkCall) {
        awaitVoidRc(sdkCall, DEFAULT_TIMEOUT);
    }

    /**
     * 执行接受 ResultCallback 的 SDK 方法并同步等待结果（指定超时）。
     *
     * @param sdkCall        接受一个 ResultCallback 回调的 SDK 方法引用
     * @param timeoutSeconds 最大等待时间（秒）
     * @throws SdkTimeoutException 超时时抛出
     * @throws SdkException SDK 返回错误时抛出
     */
    public static void awaitVoidRc(Consumer<ResultCallback> sdkCall, int timeoutSeconds) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sdkCall.accept(error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                String message = (error.getServerMessage() != null && !error.getServerMessage().isBlank())
                        ? error.getServerMessage()
                        : "errorCode=" + error.getErrorCode() + ", server=" + error.getServerCode();
                future.completeExceptionally(new SdkException(error.getServerCode(), message));
            }
        });
        await(future, timeoutSeconds);
    }

    /**
     * SDK 业务错误（error_code != 0）。
     */
    public static class SdkException extends RuntimeException {
        private final int errorCode;

        public SdkException(int errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    /**
     * SDK callback 在超时时间内未触发。
     */
    public static class SdkTimeoutException extends RuntimeException {
        public SdkTimeoutException(String message) {
            super(message);
        }
    }
}
