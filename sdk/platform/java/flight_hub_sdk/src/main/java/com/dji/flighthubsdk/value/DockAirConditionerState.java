// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 机场空调状态。 */
public enum DockAirConditionerState {
    /** 空闲模式（无制冷、制热、除湿等） */
    DockAirConditionerState_IDLE(0),
    /** 制冷模式 */
    DockAirConditionerState_COOLING(1),
    /** 制热模式 */
    DockAirConditionerState_HEATING(2),
    /** 除湿模式 */
    DockAirConditionerState_DEHUMIDIFYING(3),
    /** 制冷退出模式 */
    DockAirConditionerState_COOLING_EXIT(4),
    /** 制热退出模式 */
    DockAirConditionerState_HEATING_EXIT(5),
    /** 除湿退出模式 */
    DockAirConditionerState_DEHUMIDIFYING_EXIT(6),
    /** 制冷准备模式 */
    DockAirConditionerState_COOLING_PREPARATION(7),
    /** 制热准备模式 */
    DockAirConditionerState_HEATING_PREPARATION(8),
    /** 除湿准备模式 */
    DockAirConditionerState_DEHUMIDIFYING_PREPARATION(9),
    /** 获取空调状态失败（MCU 空调器件异常） */
    DockAirConditionerState_INVALID(32767),
    /** 未知 */
    DockAirConditionerState_UNKNOWN(65535);

    private final int value;

    DockAirConditionerState(int value) {
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
    public static DockAirConditionerState fromValue(int value) {
        for (DockAirConditionerState e : DockAirConditionerState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return DockAirConditionerState_UNKNOWN;
    }
}
