// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 喊话器工作模式。 */
public enum MegaphoneWorkMode {
    /** TTS 模式 */
    TTS(0),
    /** 实时喊话模式（微信语音模式） */
    VOICE(1),
    /** TTS 模式（APP） */
    TTS_APP(2),
    /** 对讲机模式（暂不支持） */
    LIVE_VOICE(3),
    /** 实时喊话模式（边喊边播） */
    REAL_TIME(4),
    /** 边传边播模式 */
    PLAYING_IN_TRANSMISSION(5),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    MegaphoneWorkMode(int value) {
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
    public static MegaphoneWorkMode fromValue(int value) {
        for (MegaphoneWorkMode e : MegaphoneWorkMode.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
