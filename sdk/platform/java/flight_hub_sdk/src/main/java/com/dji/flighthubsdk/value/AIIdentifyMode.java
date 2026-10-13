// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 机载 AI 识别模式。 */
public enum AIIdentifyMode {
    /** 关闭识别 */
    DISABLE(0),
    /** 可见光识别 */
    VISIBLE_LIGHT(1),
    /** 红外光识别 */
    INFRARED_LIGHT(2),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    AIIdentifyMode(int value) {
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
    public static AIIdentifyMode fromValue(int value) {
        for (AIIdentifyMode e : AIIdentifyMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
