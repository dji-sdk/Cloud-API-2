// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 喊话器 TTS 音色。 */
public enum MegaphoneTTSVoiceType {

    /** 男声 */
    MAN(0),
    /** 女声 */
    WOMAN(1),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    MegaphoneTTSVoiceType(int value) {
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
    public static MegaphoneTTSVoiceType fromValue(int value) {
        for (MegaphoneTTSVoiceType t : MegaphoneTTSVoiceType.values()) {
            if (t.value == value) {
                return t;
            }
        }
        return UNKNOWN;
    }
}
