// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 照片格式。 */
public enum PhotoFormat {
    /** RAW 格式 */
    PhotoFormat_RAW(0),
    /** JPEG 格式 */
    PhotoFormat_JPEG(1),
    /** DLT664 红外图像格式（R-JPEG，含红外热成像数据） */
    PhotoFormat_DLT664(16),
    /** 未知 */
    PhotoFormat_UNKNOWN(65535);

    private final int value;

    PhotoFormat(int value) {
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
    public static PhotoFormat fromValue(int value) {
        for (PhotoFormat e : PhotoFormat.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return PhotoFormat_UNKNOWN;
    }
}
