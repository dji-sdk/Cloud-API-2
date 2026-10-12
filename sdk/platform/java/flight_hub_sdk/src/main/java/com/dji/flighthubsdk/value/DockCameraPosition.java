// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 机场摄像头位置（舱内/舱外）。 */
public enum DockCameraPosition {
    /** 舱内 / 主摄像头 */
    INSIDE(0),
    /** 舱外 / 辅助摄像头 1 */
    OUTSIDE(1),
    /** 辅助摄像头 2 */
    AUX2(2),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    DockCameraPosition(int value) {
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
    public static DockCameraPosition fromValue(int value) {
        for (DockCameraPosition e : DockCameraPosition.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
