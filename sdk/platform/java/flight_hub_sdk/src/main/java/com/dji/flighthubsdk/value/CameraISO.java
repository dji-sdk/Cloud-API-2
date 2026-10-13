// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 相机感光度（ISO）。 */
public enum CameraISO {

    /** 自动 */
    ISO_AUTO(0),

    /** ISO 50 */
    ISO_50(2),

    /** ISO 100 */
    ISO_100(3),

    /** ISO 200 */
    ISO_200(4),

    /** ISO 400 */
    ISO_400(5),

    /** ISO 800 */
    ISO_800(6),

    /** ISO 1600 */
    ISO_1600(7),

    /** ISO 3200 */
    ISO_3200(8),

    /** ISO 6400 */
    ISO_6400(9),

    /** ISO 12800 */
    ISO_12800(10),

    /** ISO 25600 */
    ISO_25600(11),

    /** ISO 51200 */
    ISO_51200(12),

    /** ISO 102400 */
    ISO_102400(13),

    /** ISO 19200 */
    ISO_19200(14),

    /** ISO 32000 */
    ISO_32000(15),

    /** ISO 9600 */
    ISO_9600(16),

    /** ISO 16000 */
    ISO_16000(17),

    /** ISO 204800 */
    ISO_204800(18),

    /** ISO 409600 */
    ISO_409600(19),

    /** ISO 819200 */
    ISO_819200(20),

    /** 固定（不可调） */
    ISO_FIXED(255),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    CameraISO(int value) {
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
    public static CameraISO fromValue(int value) {
        for (CameraISO e : CameraISO.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
