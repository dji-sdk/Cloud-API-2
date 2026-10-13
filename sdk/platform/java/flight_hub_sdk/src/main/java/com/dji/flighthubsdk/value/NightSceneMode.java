// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 夜景模式。 */
public enum NightSceneMode {
    /** 关闭 */
    CLOSE(0),
    /** 开启 */
    OPEN(1),
    /** 自动 */
    AUTO(2),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    NightSceneMode(int value) {
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
    public static NightSceneMode fromValue(int value) {
        for (NightSceneMode e : NightSceneMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
