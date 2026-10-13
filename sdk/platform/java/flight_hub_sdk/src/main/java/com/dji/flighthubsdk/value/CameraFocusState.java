// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 相机对焦状态。 */
public enum CameraFocusState {

    /** 空闲 */
    IDLE(0),

    /** 对焦中 */
    FOCUSING(1),

    /** 对焦成功 */
    SUCCESSFUL(2),

    /** 对焦失败 */
    FAILED(3),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    CameraFocusState(int value) {
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
    public static CameraFocusState fromValue(int value) {
        for (CameraFocusState e : CameraFocusState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
