// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 激光测距状态。 */
public enum CameraLaserMeasureState {

    /** 正常测距 */
    NORMAL(0),

    /** 目标过近 */
    TOO_CLOSE(1),

    /** 目标过远 */
    TOO_FAR(2),

    /** 无信号 */
    NO_SIGNAL(3),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    CameraLaserMeasureState(int value) {
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
    public static CameraLaserMeasureState fromValue(int value) {
        for (CameraLaserMeasureState e : CameraLaserMeasureState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
