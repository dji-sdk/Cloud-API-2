// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 飞行器电池保养信息。 */
public final class DockDroneBatteryMaintenanceInfo {

    /** int, 保养状态, 0: 无需保养, 1: 待保养, 2: 正在保养; */
    private int maintenanceState;
    /** int, 电池保养剩余时间(小时); */
    private int maintenanceTimeLeft;
    /** int, 电池加热保温状态, 0: 电池未开启加热或保温, 1: 电池在加热中, 2: 电池在保温中; */
    private int heatState;
    /** std::vector<BatteryBasicInfo>, 电池基础信息; */
    private List<BatteryBasicInfo> batteries;
    public DockDroneBatteryMaintenanceInfo() {
    }

/**
     * 构造 DockDroneBatteryMaintenanceInfo。
     *
     * @param maintenanceState 保养状态（0:无需保养, 1:待保养, 2:正在保养）
     * @param maintenanceTimeLeft 电池保养剩余时间(小时)
     * @param heatState 电池加热保温状态（0:未加热保温, 1:加热中, 2:保温中）
     * @param batteries 电池基础信息列表
     */
    public DockDroneBatteryMaintenanceInfo(
            int maintenanceState,
            int maintenanceTimeLeft,
            int heatState,
            List<BatteryBasicInfo> batteries) {
        this.maintenanceState = maintenanceState;
        this.maintenanceTimeLeft = maintenanceTimeLeft;
        this.heatState = heatState;
        this.batteries = batteries != null ? batteries : Collections.emptyList();
    }

    /** int, 保养状态, 0: 无需保养, 1: 待保养, 2: 正在保养; */
    public int getMaintenanceState() {
        return maintenanceState;
    }

    /** @param maintenanceState int, 保养状态, 0: 无需保养, 1: 待保养, 2: 正在保养; */
    public void setMaintenanceState(int maintenanceState) {
        this.maintenanceState = maintenanceState;
    }

    /** int, 电池保养剩余时间(小时); */
    public int getMaintenanceTimeLeft() {
        return maintenanceTimeLeft;
    }

    /** @param maintenanceTimeLeft int, 电池保养剩余时间(小时); */
    public void setMaintenanceTimeLeft(int maintenanceTimeLeft) {
        this.maintenanceTimeLeft = maintenanceTimeLeft;
    }

    /** int, 电池加热保温状态, 0: 电池未开启加热或保温, 1: 电池在加热中, 2: 电池在保温中; */
    public int getHeatState() {
        return heatState;
    }

    /** @param heatState int, 电池加热保温状态, 0: 电池未开启加热或保温, 1: 电池在加热中, 2: 电池在保温中; */
    public void setHeatState(int heatState) {
        this.heatState = heatState;
    }

    /** std::vector<BatteryBasicInfo>, 电池基础信息; */
    public List<BatteryBasicInfo> getBatteries() {
        return batteries;
    }

    /** @param batteries std::vector<BatteryBasicInfo>, 电池基础信息; */
    public void setBatteries(List<BatteryBasicInfo> batteries) {
        this.batteries = batteries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockDroneBatteryMaintenanceInfo that = (DockDroneBatteryMaintenanceInfo) o;
        return maintenanceState == that.maintenanceState
                && maintenanceTimeLeft == that.maintenanceTimeLeft
                && heatState == that.heatState
                && Objects.equals(batteries, that.batteries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maintenanceState, maintenanceTimeLeft, heatState, batteries);
    }

    @Override
    public String toString() {
        return "DockDroneBatteryMaintenanceInfo{maintenanceState=" + maintenanceState
                + ", maintenanceTimeLeft=" + maintenanceTimeLeft
                + ", heatState=" + heatState
                + ", batteries=" + batteries + '}';
    }
}
