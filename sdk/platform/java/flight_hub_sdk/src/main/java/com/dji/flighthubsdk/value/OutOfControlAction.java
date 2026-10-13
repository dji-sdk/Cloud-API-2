// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 失控动作。 */
public enum OutOfControlAction {
    /** 悬停 */
    HOVER(0),
    /** 返航 */
    RETURN_HOME(1),
    /** 继续 */
    CONTINUE(2),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    OutOfControlAction(int value) {
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
    public static OutOfControlAction fromValue(int value) {
        for (OutOfControlAction e : OutOfControlAction.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
