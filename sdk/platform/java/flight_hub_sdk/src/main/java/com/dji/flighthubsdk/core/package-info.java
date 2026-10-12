// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * SDK 运行时核心能力：通过 {@link com.dji.flighthubsdk.core.FlightHubSdkManager} 衔接底层实现，
 * 对外提供 {@link com.dji.flighthubsdk.core.Workspace}、{@link com.dji.flighthubsdk.core.Drone}、
 * {@link com.dji.flighthubsdk.core.Dock} 以及直播、航线、媒体等业务模块。
 *
 * <p>应用通常通过 {@link com.dji.flighthubsdk.FlightHubSDK} 获取本包中的实例；本包负责会话与设备对象的生命周期，
 * 并将底层数据转换为 {@link com.dji.flighthubsdk.bean} 中的类型后再暴露给调用方。</p>
 *
 * @see com.dji.flighthubsdk.FlightHubSDK
 * @see com.dji.flighthubsdk.bean
 */
package com.dji.flighthubsdk.core;
