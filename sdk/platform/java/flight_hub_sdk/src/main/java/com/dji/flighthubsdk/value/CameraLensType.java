// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 直播/负载镜头类型。 */
public enum CameraLensType {

    /** 默认镜头 */
    NORMAL(0),

    /** 广角镜头 */
    WIDE(1),

    /** 变焦镜头 */
    ZOOM(2),

    /** 红外镜头 */
    IR(3),

    /** 热成像镜头 */
    THERMAL(4),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    CameraLensType(int value) {
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
    public static CameraLensType fromValue(int value) {
        for (CameraLensType e : CameraLensType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
