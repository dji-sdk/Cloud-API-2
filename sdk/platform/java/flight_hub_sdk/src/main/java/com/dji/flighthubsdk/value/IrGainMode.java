// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 红外增益模式。 */
public enum IrGainMode {

    /** 自动 */
    Auto(0),
    /** 低增益 */
    LowGain(1),
    /** 高增益 */
    HighGain(2),
    /** 超清模式 */
    UltraHD(3),
    UNKNOWN(0xFFFF);

    private final int value;

    IrGainMode(int value) {
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
    public static IrGainMode fromValue(int value) {
        for (IrGainMode e : IrGainMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
