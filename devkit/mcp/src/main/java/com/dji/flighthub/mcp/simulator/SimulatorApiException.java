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

package com.dji.flighthub.mcp.simulator;

/**
 * 封装 pf-device-simulator-service HTTP API 调用失败信息。
 *
 * <p>{@code httpStatus} 为 0 表示连接级失败（不可达/超时），非 0 表示服务端返回的 HTTP 状态码。
 *
 * @author DJI FlightHub SDK Team
 */
public class SimulatorApiException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int httpStatus;

    /**
     * 构造模拟器 API 异常。
     *
     * @param httpStatus HTTP 状态码，0 表示连接级失败
     * @param message    错误信息，包含请求 path
     */
    public SimulatorApiException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * 构造模拟器 API 异常（保留原始异常链）。
     *
     * @param httpStatus HTTP 状态码，0 表示连接级失败
     * @param message    错误信息，包含请求 path
     * @param cause      原始异常（如网络连接失败的底层异常）
     */
    public SimulatorApiException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * 获取 HTTP 状态码。
     *
     * @return HTTP 状态码，0 表示连接级失败
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
