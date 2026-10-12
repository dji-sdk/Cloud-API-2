// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** PSDK 子类型。 */
public enum PsdkSubType {
    /** 喊话器 AS1 */
    SPEAKER(0),
    /** 探照灯 AL1 */
    SEARCHLIGHT(1),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    PsdkSubType(int value) {
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
    public static PsdkSubType fromValue(int value) {
        for (PsdkSubType e : PsdkSubType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
