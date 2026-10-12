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

package com.dji.fh.feature.dashboard.spi.sdk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SDK 异步回调 → 同步桥接工具。
 *
 * <p>将 SDK 回调模式转为 {@link CompletableFuture}，统一超时和异常处理。
 *
 * @author DJI DevKit Team
 */
final class SdkBridge {

    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    private SdkBridge() {
    }

    /**
     * 等待 future 完成，超时或异常时抛出 {@link SdkQueryException}。
     *
     * @param future SDK 回调填充的 future
     * @param <T>    结果类型
     * @return SDK 返回结果
     * @throws SdkQueryException 超时或 SDK 错误
     */
    static <T> T await(CompletableFuture<T> future) {
        try {
            return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new SdkQueryException("SDK 查询超时（" + DEFAULT_TIMEOUT_SECONDS + "s）");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new SdkQueryException(
                    cause != null ? cause.getMessage() : "SDK 回调异常");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SdkQueryException("SDK 查询被中断");
        }
    }

    /**
     * SDK 查询异常（非受检）。
     */
    static class SdkQueryException extends RuntimeException {

        SdkQueryException(String message) {
            super(message);
        }
    }
}
