// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 拍照结果状态（含全景）。 */
public enum CameraPhotoState {

    /** 拍摄中 */
    IN_PROGRESS(0),

    /** 拍摄成功 */
    OK(1),

    /** 拍摄失败 */
    FAILED(2),

    /** 已取消 */
    CANCELED(3),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    CameraPhotoState(int value) {
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
    public static CameraPhotoState fromValue(int value) {
        for (CameraPhotoState e : CameraPhotoState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
