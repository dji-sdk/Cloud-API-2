// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 相机对焦模式。 */
public enum CameraFocusMode {

    /** MF 手动对焦 */
    MANUAL(0),
    /** AF 单次自动对焦 */
    AF(1),
    /** AFC 连续自动对焦 */
    AFC(2),
    /** MF 微调（配合 mf_value 使用） */
    FINE_TUNE(3),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    CameraFocusMode(int value) {
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
    public static CameraFocusMode fromValue(int value) {
        for (CameraFocusMode m : CameraFocusMode.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
