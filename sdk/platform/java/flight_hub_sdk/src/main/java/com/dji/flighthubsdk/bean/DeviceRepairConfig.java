// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;

import java.util.Objects;
/** 设备报修配置信息 */
public final class DeviceRepairConfig {
    /** 设备 SN; */
    private String deviceSn;
    /** 是否支持在线报修; */
    private boolean supportRepair;
    /** 允许的报修来源类型：0=定损 2=日志反馈 3=直接报修; */
    private List<Integer> allowedSourceTypes;
    /** 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object); */
    private RepairGuidance repairGuidance;
    public DeviceRepairConfig() {
    }

/**
     * 构造 DeviceRepairConfig。
     *
     * @param deviceSn 设备 SN
     * @param supportRepair 是否支持在线报修
     * @param allowedSourceTypes 允许的报修来源类型：0=定损 2=日志反馈 3=直接报修
     * @param repairGuidance 不支持报修时返回的维修指导
     */
    public DeviceRepairConfig(String deviceSn, boolean supportRepair, List<Integer> allowedSourceTypes, RepairGuidance repairGuidance) {
        this.deviceSn = deviceSn;
        this.supportRepair = supportRepair;
        this.allowedSourceTypes = allowedSourceTypes;
        this.repairGuidance = repairGuidance;
    }

    /** 设备 SN; */
    public String getDeviceSn() { return deviceSn; }
    /** 是否支持在线报修; */
    public boolean isSupportRepair() { return supportRepair; }
    /** 允许的报修来源类型：0=定损 2=日志反馈 3=直接报修; */
    public List<Integer> getAllowedSourceTypes() { return allowedSourceTypes; }
    /** 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object); */
    public RepairGuidance getRepairGuidance() { return repairGuidance; }

    /** @param deviceSn 设备 SN; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** @param supportRepair 是否支持在线报修; */
    public void setSupportRepair(boolean supportRepair) {
        this.supportRepair = supportRepair;
    }

    /** @param allowedSourceTypes 允许的报修来源类型：0=定损 2=日志反馈 3=直接报修; */
    public void setAllowedSourceTypes(List<Integer> allowedSourceTypes) {
        this.allowedSourceTypes = allowedSourceTypes;
    }

    /** @param repairGuidance 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object); */
    public void setRepairGuidance(RepairGuidance repairGuidance) {
        this.repairGuidance = repairGuidance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceRepairConfig that = (DeviceRepairConfig) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && supportRepair == that.supportRepair
                && Objects.equals(allowedSourceTypes, that.allowedSourceTypes)
                && Objects.equals(repairGuidance, that.repairGuidance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, supportRepair, allowedSourceTypes, repairGuidance);
    }

    @Override
    public String toString() {
        return "DeviceRepairConfig{"
                + "deviceSn='" + deviceSn + '\''
                + ", supportRepair=" + supportRepair
                + ", allowedSourceTypes=" + allowedSourceTypes
                + ", repairGuidance=" + repairGuidance
                + '}';
    }
}
