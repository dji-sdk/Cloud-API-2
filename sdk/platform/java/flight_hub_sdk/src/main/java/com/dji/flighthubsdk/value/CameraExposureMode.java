// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 相机曝光模式。 */
public enum CameraExposureMode {

    /** 自动曝光 */
    PROGRAM(1),
    /** 快门优先 */
    SHUTTER_PRIORITY(2),
    /** 光圈优先 */
    APERTURE_PRIORITY(3),
    /** 手动曝光 */
    MANUAL(4),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    CameraExposureMode(int value) {
        this.value = value;
    }

    /** 获取枚举对应的整型值。 */
    public int getValue() {
        return value;
    }

    /** 未命中已知取值时返回 {@link #UNKNOWN}。 */
    public static CameraExposureMode fromValue(int value) {
        for (CameraExposureMode m : CameraExposureMode.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
