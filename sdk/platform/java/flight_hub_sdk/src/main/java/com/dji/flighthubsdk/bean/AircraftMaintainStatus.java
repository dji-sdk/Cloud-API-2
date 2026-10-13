// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 单条保养状态。 */
public final class AircraftMaintainStatus {

    /** 保养状态(0: 未保养, 1: 保养中); */
    private int state;
    /** 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养); */
    private int lastMaintainType;
    /** 上一次保养时间, utc 秒时间戳(单位: 秒); // TODO: long long 类型 AIGC 解析出错; */
    private long lastMaintainTime;
    /** 上一次保养时飞行航时(单位: h); */
    private int lastMaintainFlightTime;
    /** 上一次保养时飞行架次; */
    private int lastMaintainFlightSorties;
    public AircraftMaintainStatus() {
    }

/**
     * 构造 AircraftMaintainStatus。
     *
     * @param state 保养状态
     * @param lastMaintainType 上次保养类型
     * @param lastMaintainTime 上次保养时间(UTC秒时间戳)
     * @param lastMaintainFlightTime 上次保养时飞行航时(h)
     * @param lastMaintainFlightSorties 上次保养时飞行架次
     */
    public AircraftMaintainStatus(
            int state,
            int lastMaintainType,
            long lastMaintainTime,
            int lastMaintainFlightTime,
            int lastMaintainFlightSorties) {
        this.state = state;
        this.lastMaintainType = lastMaintainType;
        this.lastMaintainTime = lastMaintainTime;
        this.lastMaintainFlightTime = lastMaintainFlightTime;
        this.lastMaintainFlightSorties = lastMaintainFlightSorties;
    }

    /** 保养状态(0: 未保养, 1: 保养中); */
    public int getState() {
        return state;
    }

    /** @param state 保养状态(0: 未保养, 1: 保养中); */
    public void setState(int state) {
        this.state = state;
    }

    /** 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养); */
    public int getLastMaintainType() {
        return lastMaintainType;
    }

    /** @param lastMaintainType 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养); */
    public void setLastMaintainType(int lastMaintainType) {
        this.lastMaintainType = lastMaintainType;
    }

    /** 上一次保养时间, utc 秒时间戳(单位: 秒); // TODO: long long 类型 AIGC 解析出错; */
    public long getLastMaintainTime() {
        return lastMaintainTime;
    }

    /** @param lastMaintainTime 上一次保养时间, utc 秒时间戳(单位: 秒); // TODO: long long 类型 AIGC 解析出错; */
    public void setLastMaintainTime(long lastMaintainTime) {
        this.lastMaintainTime = lastMaintainTime;
    }

    /** 上一次保养时飞行航时(单位: h); */
    public int getLastMaintainFlightTime() {
        return lastMaintainFlightTime;
    }

    /** @param lastMaintainFlightTime 上一次保养时飞行航时(单位: h); */
    public void setLastMaintainFlightTime(int lastMaintainFlightTime) {
        this.lastMaintainFlightTime = lastMaintainFlightTime;
    }

    /** 上一次保养时飞行架次; */
    public int getLastMaintainFlightSorties() {
        return lastMaintainFlightSorties;
    }

    /** @param lastMaintainFlightSorties 上一次保养时飞行架次; */
    public void setLastMaintainFlightSorties(int lastMaintainFlightSorties) {
        this.lastMaintainFlightSorties = lastMaintainFlightSorties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftMaintainStatus that = (AircraftMaintainStatus) o;
        return state == that.state
                && lastMaintainType == that.lastMaintainType
                && lastMaintainTime == that.lastMaintainTime
                && lastMaintainFlightTime == that.lastMaintainFlightTime
                && lastMaintainFlightSorties == that.lastMaintainFlightSorties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                state, lastMaintainType, lastMaintainTime, lastMaintainFlightTime, lastMaintainFlightSorties);
    }

    @Override
    public String toString() {
        return "AircraftMaintainStatus{state=" + state + ", lastMaintainType=" + lastMaintainType + '}';
    }
}
