// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 自定义 SEI 数据项类型。 */
public enum SeiDataType {
    /** 自定义数据（对应 SeiItem::custom_data） */
    CUSTOM_DATA(1),
    /** 目标检测框（对应 SeiItem::detection_box） */
    DETECTION_BOX(2),
    /** 多边形检测区域（对应 SeiItem::detection_polygon） */
    DETECTION_POLYGON(3),
    /** 文本叠加（对应 SeiItem::text_overlay） */
    TEXT_OVERLAY(4),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    SeiDataType(int value) {
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
    public static SeiDataType fromValue(int value) {
        for (SeiDataType e : SeiDataType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
