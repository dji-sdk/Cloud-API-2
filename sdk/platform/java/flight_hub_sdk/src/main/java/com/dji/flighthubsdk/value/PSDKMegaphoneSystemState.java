// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 喊话器系统状态。 */
public enum PSDKMegaphoneSystemState {
    /** 空闲中 */
    IDLE(0),
    /** 传输中 */
    IN_TRANSMISSION(1),
    /** 播放中 */
    PLAYING(2),
    /** 异常中 */
    IN_EXCEPTION(3),
    /** 文本转换中（仅用于 TTS） */
    TTS_IN_CONVERSION(4),
    /** 边传边播中 */
    PLAYING_IN_TRANSMISSION(5),
    /** 下载中（云端到机场） */
    DOWNLOADING(99),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    PSDKMegaphoneSystemState(int value) {
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
    public static PSDKMegaphoneSystemState fromValue(int value) {
        for (PSDKMegaphoneSystemState e : PSDKMegaphoneSystemState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
