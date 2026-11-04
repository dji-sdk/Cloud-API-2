// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/**
 * 飞行安全风险类型。
 *
 * <p>绕行只在开始点产生一条 {@link #OBSTACLE_BYPASS}，结束点不单独成为一条风险。
 */
public enum FlightRiskType {
    /** 未指定 */
    UNSPECIFIED(0),
    /** 避障刹停 */
    OBSTACLE_BRAKE(1),
    /** 避障绕行 */
    OBSTACLE_BYPASS(2),
    /** 碰撞炸机 */
    COLLISION(3),
    /** 未知 */
    UNKNOWN(65535);

    private final int value;

    FlightRiskType(int value) {
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
    public static FlightRiskType fromValue(int value) {
        for (FlightRiskType e : FlightRiskType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
