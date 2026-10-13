// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 空中航线控制动作 */
public enum InFlightWaylineControlAction {
    /** 暂停 */
    PAUSE(1),
    /** 恢复 */
    RESUME(2),
    /** 取消 */
    CANCEL(3),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    InFlightWaylineControlAction(int value) {
        this.value = value;
    }

    /** 获取枚举对应的整型值 */
    public int getValue() {
        return value;
    }

    /** 由整型解析为枚举；未命中已知取值时返回 UNKNOWN */
    public static InFlightWaylineControlAction fromValue(int value) {
        for (InFlightWaylineControlAction e : InFlightWaylineControlAction.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
