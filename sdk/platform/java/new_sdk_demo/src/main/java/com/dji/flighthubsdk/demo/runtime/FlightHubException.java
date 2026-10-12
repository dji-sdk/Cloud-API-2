// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

/**
 * Demo 统一异常：把 SDK 的错误码 / 前置条件失败 / 超时等统一为可抛出的非受检运行时异常语义。
 *
 * <p>相比「打印一行 System.out 然后继续跑」, 这里让失败可以被 try/finally 正确捕获与清理。
 */
public class FlightHubException extends RuntimeException {

    public FlightHubException(String message) {
        super(message);
    }

    public FlightHubException(String message, Throwable cause) {
        super(message, cause);
    }
}
