// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.value;

/** 飞行器模式码。 */
public enum DroneModeCode {

    /** 0：待机 */
    STANDBY(0),
    /** 1：起飞准备 */
    TAKEOFF_PREPARATION(1),
    /** 2：起飞准备完毕 */
    READY_TO_TAKE_OFF(2),
    /** 3：手动飞行 */
    MANUAL_FLIGHT(3),
    /** 4：自动起飞 */
    AUTOMATIC_TAKEOFF(4),
    /** 5：航线飞行 */
    ROUTE_FLIGHT(5),
    /** 6：全景拍照 */
    PANORAMIC_PHOTO(6),
    /** 7：智能跟随 */
    SMART_FOLLOW(7),
    /** 8：ADS-B 躲避 */
    ADSB_AVOIDANCE(8),
    /** 9：自动返航 */
    RETURN_HOME_AUTOMATICALLY(9),
    /** 10：自动降落 */
    AUTOMATIC_LANDING(10),
    /** 11：强制降落 */
    FORCED_LANDING(11),
    /** 12：三桨叶降落 */
    LANDING_WITH_THREE_BLADES(12),
    /** 13：升级中 */
    UPGRADING(13),
    /** 14：未连接 */
    NOT_CONNECTED(14),
    /** 15：高级手动飞行，智能绕行避障 */
    APAS(15),
    /** 16：虚拟摇杆模式（与手动飞行在设备侧可能均显示为手动） */
    JOYSTICK(16),
    /** 17：指点飞行 */
    COMMAND_FLIGHT(17),
    /** 18：RTK 收敛中 */
    RTK_CONVERGING(18),
    /** 19：机场选址中 */
    EXPLORATION_AND_SITING(19),
    /** 20：POI 环绕 */
    POI(20),
    /** 21：离场航线 */
    DEPARTURE_POINT(21),
    /** 22：中继选址中 */
    RELAY_SITING(22),
    /** 状态异常或未在表中定义的取值 */
    UNKNOWN(65535);

    private final int value;

    DroneModeCode(int value) {
        this.value = value;
    }

    /** 整型取值。 */
    public int getValue() {
        return value;
    }

    /**
     * 由整型解析为枚举；未命中已知取值时返回 {@link #UNKNOWN}。
     *
     * @param value 与 {@code AircraftModeCode::mode_code} 一致的原始值
     */
    public static DroneModeCode fromValue(int value) {
        for (DroneModeCode m : DroneModeCode.values()) {
            if (m.value == value) {
                return m;
            }
        }
        return UNKNOWN;
    }
}
