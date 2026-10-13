// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 光圈值。 */
public enum CameraAperture {

    /** 自动光圈 */
    F_AUTO(0),

    /** f/1.6 */
    F1DOT6(160),

    /** f/1.7 */
    F1DOT7(170),

    /** f/1.8 */
    F1DOT8(180),

    /** f/2 */
    F2(200),

    /** f/2.2 */
    F2DOT2(220),

    /** f/2.4 */
    F2DOT4(240),

    /** f/2.5 */
    F2DOT5(250),

    /** f/2.8 */
    F2DOT8(280),

    /** f/3.2 */
    F3DOT2(320),

    /** f/3.4 */
    F3DOT4(340),

    /** f/3.5 */
    F3DOT5(350),

    /** f/4 */
    F4(400),

    /** f/4.4 */
    F4DOT4(440),

    /** f/4.5 */
    F4DOT5(450),

    /** f/4.8 */
    F4DOT8(480),

    /** f/5 */
    F5(500),

    /** f/5.6 */
    F5DOT6(560),

    /** f/6.3 */
    F6DOT3(630),

    /** f/6.8 */
    F6DOT8(680),

    /** f/7.1 */
    F7DOT1(710),

    /** f/8 */
    F8(800),

    /** f/9 */
    F9(900),

    /** f/9.5 */
    F9DOT5(950),

    /** f/9.6 */
    F9DOT6(960),

    /** f/10 */
    F10(1000),

    /** f/11 */
    F11(1100),

    /** f/13 */
    F13(1300),

    /** f/14 */
    F14(1400),

    /** f/16 */
    F16(1600),

    /** f/18 */
    F18(1800),

    /** f/19 */
    F19(1900),

    /** f/20 */
    F20(2000),

    /** f/22 */
    F22(2200),

    /** f/27 */
    F27(2700),

    /** f/32 */
    F32(3200),

    /** 未知 */
    UNKNOWN(0xFFFF),
    ;

    private final int value;

    CameraAperture(int value) {
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
    public static CameraAperture fromValue(int value) {
        for (CameraAperture e : CameraAperture.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
