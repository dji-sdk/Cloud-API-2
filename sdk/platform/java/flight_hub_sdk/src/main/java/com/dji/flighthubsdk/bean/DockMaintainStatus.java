// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 保养状态条目。 */
public final class DockMaintainStatus {

    /** 保养状态(0: 未保养, 1: 保养中); */
    private int state;
    /** 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养); */
    private int lastMaintainType;
    /** 上一次保养时间, utc 秒时间戳(单位: 秒); // TODO: long long 类型 AIGC 解析出错; */
    private long lastMaintainTime;
    /** 上一次保养时飞行架次, [0, 2147483647]; */
    private int lastMaintainWorkSorties;
    public DockMaintainStatus() {
    }

/**
     * 构造 DockMaintainStatus。
     *
     * @param state 状态
     * @param lastMaintainType 上次保养类型
     * @param lastMaintainTime 上次保养时间
     * @param lastMaintainWorkSorties 上次保养后工作架次
     */
    public DockMaintainStatus(
            int state,
            int lastMaintainType,
            long lastMaintainTime,
            int lastMaintainWorkSorties) {
        this.state = state;
        this.lastMaintainType = lastMaintainType;
        this.lastMaintainTime = lastMaintainTime;
        this.lastMaintainWorkSorties = lastMaintainWorkSorties;
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

    /** 上一次保养时飞行架次, [0, 2147483647]; */
    public int getLastMaintainWorkSorties() {
        return lastMaintainWorkSorties;
    }

    /** @param lastMaintainWorkSorties 上一次保养时飞行架次, [0, 2147483647]; */
    public void setLastMaintainWorkSorties(int lastMaintainWorkSorties) {
        this.lastMaintainWorkSorties = lastMaintainWorkSorties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockMaintainStatus that = (DockMaintainStatus) o;
        return state == that.state
                && lastMaintainType == that.lastMaintainType
                && lastMaintainTime == that.lastMaintainTime
                && lastMaintainWorkSorties == that.lastMaintainWorkSorties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, lastMaintainType, lastMaintainTime, lastMaintainWorkSorties);
    }

    @Override
    public String toString() {
        return "DockMaintainStatus{state=" + state
                + ", lastMaintainType=" + lastMaintainType
                + ", lastMaintainTime=" + lastMaintainTime
                + ", lastMaintainWorkSorties=" + lastMaintainWorkSorties + '}';
    }
}
