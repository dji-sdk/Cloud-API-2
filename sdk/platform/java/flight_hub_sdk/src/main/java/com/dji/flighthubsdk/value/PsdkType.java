// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** PSDK 设备类型。 */
public enum PsdkType {
    /** 第三方设备 */
    UNOFFICIAL(4),
    /** 官方设备 */
    OFFICIAL(5),
    /** 妙算 3 */
    MANIFOLD3(8),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    PsdkType(int value) {
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
    public static PsdkType fromValue(int value) {
        for (PsdkType e : PsdkType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
