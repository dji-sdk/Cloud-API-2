// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 喊话器播放模式。 */
public enum MegaphonePlayMode {

    /** 单次播放 */
    SINGLE_PLAY(0),
    /** 循环播放（单曲） */
    LOOP_PLAYBACK(1),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    MegaphonePlayMode(int value) {
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
    public static MegaphonePlayMode fromValue(int value) {
        for (MegaphonePlayMode m : MegaphonePlayMode.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
