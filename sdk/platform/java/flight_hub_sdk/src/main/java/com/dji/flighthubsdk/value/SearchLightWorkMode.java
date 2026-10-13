// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 探照灯工作模式。 */
public enum SearchLightWorkMode {
    /** 关闭 */
    CLOSE(0),
    /** 常亮 */
    OPEN(1),
    /** 爆闪 */
    STROBE(2),
    /** 快速爆闪 */
    QUICK_STROBE(3),
    /** 交替爆闪 */
    ALTERNATION_STROBE(4),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    SearchLightWorkMode(int value) {
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
    public static SearchLightWorkMode fromValue(int value) {
        for (SearchLightWorkMode e : SearchLightWorkMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
