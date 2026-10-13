// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 机场舱盖模式。 */
public enum DockCoverMode {

    /** 开启舱盖 */
    OPEN(0),
    /** 关闭舱盖 */
    CLOSE(1),
    /** 强制关闭舱盖 */
    FORCE_CLOSE(2),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    DockCoverMode(int value) {
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
    public static DockCoverMode fromValue(int value) {
        for (DockCoverMode m : DockCoverMode.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
