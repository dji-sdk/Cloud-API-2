// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 无人机链路状态。 */
public enum DroneLinkState {

    /** 空闲 */
    IDLE(0),
    /** 连接中 */
    CONNECTING(1),
    /** 已连接 */
    CONNECTED(2),
    /** 已断开 */
    DISCONNECTED(3),
    /** 未知或未在表中定义的取值 */
    UNKNOWN(65535);

    private final int value;

    DroneLinkState(int value) {
        this.value = value;
    }

    /** 整型取值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 {@link #UNKNOWN}。
     *
     * @param value 与 {@code LinkStatus} 链路字段一致的原始值
     * @return 对应枚举常量，未知时 {@link #UNKNOWN}
     */
    public static DroneLinkState fromValue(int value) {
        for (DroneLinkState m : DroneLinkState.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
