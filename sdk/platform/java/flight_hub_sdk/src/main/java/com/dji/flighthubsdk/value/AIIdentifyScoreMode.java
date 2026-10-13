// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 机载 AI 识别置信度模式。 */
public enum AIIdentifyScoreMode {
    /** 无效值 */
    MODE_INVALID(0),
    /** 计数模式 */
    MODE_COUNT(1),
    /** 搜救模式 */
    MODE_RESCUE(2),
    /** 用户自定义模式 */
    MODE_USER(3),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    AIIdentifyScoreMode(int value) {
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
    public static AIIdentifyScoreMode fromValue(int value) {
        for (AIIdentifyScoreMode e : AIIdentifyScoreMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
