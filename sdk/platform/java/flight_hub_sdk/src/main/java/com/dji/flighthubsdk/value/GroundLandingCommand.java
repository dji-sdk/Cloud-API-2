// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 舱外降落指令。 */
public enum GroundLandingCommand {
    /** 降落至机场（机场自动开舱盖 → 无人机降落 → 关舱盖） */
    LAND_TO_DOCK(0),
    /** 取消降落（飞机悬停） */
    CANCEL(1),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    GroundLandingCommand(int value) {
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
    public static GroundLandingCommand fromValue(int value) {
        for (GroundLandingCommand e : GroundLandingCommand.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
