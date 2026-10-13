// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * 面向使用方的数据模型与参数类型：设备与机场状态、航线与任务、媒体与日志等结构的 Java Bean，
 * 以及各类请求参数、分页结果与错误信息（如 {@link com.dji.flighthubsdk.bean.FlightHubError}）。
 *
 * <p>本包类型由 {@link com.dji.flighthubsdk.FlightHubSDK} 与 {@link com.dji.flighthubsdk.core} 各模块在对外 API 中返回或接收，
 * 语义与 SDK 协议及云端约定一致，便于在 Java 工程中阅读、传递与序列化。</p>
 *
 * @see com.dji.flighthubsdk.FlightHubSDK
 * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
 */
package com.dji.flighthubsdk.bean;
