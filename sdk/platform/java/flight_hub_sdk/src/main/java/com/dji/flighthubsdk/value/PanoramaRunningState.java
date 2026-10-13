// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 全景拍照执行阶段。 */
public enum PanoramaRunningState {

    /** 空闲 */
    IDLE(0),

    /** 拍摄中 */
    RUNNING(1),

    /** 合成中 */
    SYNTHESIZING(2),

    /** 已完成 */
    FINISHED(3),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    PanoramaRunningState(int value) {
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
    public static PanoramaRunningState fromValue(int value) {
        for (PanoramaRunningState e : PanoramaRunningState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
