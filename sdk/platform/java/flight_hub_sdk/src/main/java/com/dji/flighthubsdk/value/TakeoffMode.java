// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 起飞模式。 */
public enum TakeoffMode {
    /** 起飞 */
    TAKEOFF(0),
    /** 起飞并飞向目标点 */
    TAKEOFF_WITH_FLY_TO(1),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    TakeoffMode(int value) {
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
    public static TakeoffMode fromValue(int value) {
        for (TakeoffMode e : TakeoffMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
