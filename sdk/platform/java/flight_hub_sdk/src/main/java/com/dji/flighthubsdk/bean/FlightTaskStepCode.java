// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场任务步骤码。 */
public final class FlightTaskStepCode {

    private int flighttaskStepCode;
    public FlightTaskStepCode() {
    }

/**
     * 构造 FlightTaskStepCode。
     *
     * @param flighttaskStepCode 飞行任务步骤码: 0=作业准备中, 1=飞行作业中, 2=作业后状态恢复, 3=自定义飞行区更新中, 4=地形障碍物更新中, 5=任务空闲, 256=未知状态
     */
    public FlightTaskStepCode(int flighttaskStepCode) {
        this.flighttaskStepCode = flighttaskStepCode;
    }

    /** 飞行任务步骤码: 0=作业准备中, 1=飞行作业中, 2=作业后状态恢复, 3=自定义飞行区更新中, 4=地形障碍物更新中, 5=任务空闲, 256=未知状态 */
    public int getFlighttaskStepCode() {
        return flighttaskStepCode;
    }

    /** 飞行任务步骤码: 0=作业准备中, 1=飞行作业中, 2=作业后状态恢复, 3=自定义飞行区更新中, 4=地形障碍物更新中, 5=任务空闲, 256=未知状态 */
    public void setFlighttaskStepCode(int flighttaskStepCode) {
        this.flighttaskStepCode = flighttaskStepCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTaskStepCode that = (FlightTaskStepCode) o;
        return flighttaskStepCode == that.flighttaskStepCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flighttaskStepCode);
    }

    @Override
    public String toString() {
        return "FlightTaskStepCode{flighttaskStepCode=" + flighttaskStepCode + '}';
    }
}
