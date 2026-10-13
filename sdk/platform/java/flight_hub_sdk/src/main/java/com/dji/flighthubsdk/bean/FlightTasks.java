// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞行任务列表推送。 */
public final class FlightTasks {

    private List<FlightTask> flightTasks;
    public FlightTasks() {
    }

/**
     * 构造 FlightTasks。
     *
     * @param flightTasks 飞行任务列表
     */
    public FlightTasks(List<FlightTask> flightTasks) {
        this.flightTasks = flightTasks != null ? flightTasks : Collections.emptyList();
    }

    /** 飞行任务列表 */
    public List<FlightTask> getFlightTasks() {
        return flightTasks;
    }

    /** 飞行任务列表 */
    public void setFlightTasks(List<FlightTask> flightTasks) {
        this.flightTasks = flightTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTasks that = (FlightTasks) o;
        return Objects.equals(flightTasks, that.flightTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightTasks);
    }

    @Override
    public String toString() {
        return "FlightTasks{flightTasks=" + flightTasks + '}';
    }
}
