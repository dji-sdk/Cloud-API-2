// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/**
 * 速度模式。当前 EA230 / EA340 支持，后续机型可能扩展；SDK 不做机型校验，由服务端与设备侧判定。
 *
 * <p>与 {@code TakeoffParams.commanderFlightMode}（指点飞行风格：平稳/运动）无关，是两个独立字段。
 */
public enum SpeedMode {
    /** 低速 */
    LOW(0),
    /** 高速 */
    HIGH(1),
    /** 未设置：不下发该字段，设备保持当前模式 */
    UNKNOWN(65535);

    private final int value;

    SpeedMode(int value) {
        this.value = value;
    }

    /** 获取枚举对应的整型值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 UNKNOWN。
     *
     * @param value 整型原始值
     * @return 对应枚举常量
     */
    public static SpeedMode fromValue(int value) {
        for (SpeedMode e : SpeedMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
