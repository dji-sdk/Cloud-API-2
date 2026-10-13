// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 曝光补偿（EV）。 */
public enum CameraExposureCompensation {

    /** -5.0EV */
    NEG_5P0EV(1),
    /** -4.7EV */
    NEG_4P7EV(2),
    /** -4.3EV */
    NEG_4P3EV(3),
    /** -4.0EV */
    NEG_4P0EV(4),
    /** -3.7EV */
    NEG_3P7EV(5),
    /** -3.3EV */
    NEG_3P3EV(6),
    /** -3.0EV */
    NEG_3P0EV(7),
    /** -2.7EV */
    NEG_2P7EV(8),
    /** -2.3EV */
    NEG_2P3EV(9),
    /** -2.0EV */
    NEG_2P0EV(10),
    /** -1.7EV */
    NEG_1P7EV(11),
    /** -1.3EV */
    NEG_1P3EV(12),
    /** -1.0EV */
    NEG_1P0EV(13),
    /** -0.7EV */
    NEG_0P7EV(14),
    /** -0.3EV */
    NEG_0P3EV(15),
    /** 0EV（无补偿） */
    NEG_0EV(16),
    /** +0.3EV */
    POS_0P3EV(17),
    /** +0.7EV */
    POS_0P7EV(18),
    /** +1.0EV */
    POS_1P0EV(19),
    /** +1.3EV */
    POS_1P3EV(20),
    /** +1.7EV */
    POS_1P7EV(21),
    /** +2.0EV */
    POS_2P0EV(22),
    /** +2.3EV */
    POS_2P3EV(23),
    /** +2.7EV */
    POS_2P7EV(24),
    /** +3.0EV */
    POS_3P0EV(25),
    /** +3.3EV */
    POS_3P3EV(26),
    /** +3.7EV */
    POS_3P7EV(27),
    /** +4.0EV */
    POS_4P0EV(28),
    /** +4.3EV */
    POS_4P3EV(29),
    /** +4.7EV */
    POS_4P7EV(30),
    /** +5.0EV */
    POS_5P0EV(31),
    /** 固定（不可调） */
    FIXED(255),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    CameraExposureCompensation(int value) {
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
    public static CameraExposureCompensation fromValue(int value) {
        for (CameraExposureCompensation m : CameraExposureCompensation.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
