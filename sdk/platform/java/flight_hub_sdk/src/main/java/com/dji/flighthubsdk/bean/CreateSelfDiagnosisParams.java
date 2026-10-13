// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备定损 - 下发定损指令请求参数 */
public class CreateSelfDiagnosisParams {
    /** 设备 SN; */
    private String deviceSn = "";
    /** 定损类型; */
    private int type;
    /** 定损部位; */
    private int part;
    /** 索引; */
    private int index;
    /** 电机动作; */
    private int motorAction;
    /** 设备拓扑; */
    private SelfDiagnosisDeviceTopoParams deviceTopo;

    public CreateSelfDiagnosisParams() {
    }


    /** 设备 SN; */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备 SN; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 定损类型; */
    public int getType() {
        return type;
    }

    /** @param type 定损类型; */
    public void setType(int type) {
        this.type = type;
    }

    /** 定损部位; */
    public int getPart() {
        return part;
    }

    /** @param part 定损部位; */
    public void setPart(int part) {
        this.part = part;
    }

    /** 索引; */
    public int getIndex() {
        return index;
    }

    /** @param index 索引; */
    public void setIndex(int index) {
        this.index = index;
    }

    /** 电机动作; */
    public int getMotorAction() {
        return motorAction;
    }

    /** @param motorAction 电机动作; */
    public void setMotorAction(int motorAction) {
        this.motorAction = motorAction;
    }

    /** 设备拓扑; */
    public SelfDiagnosisDeviceTopoParams getDeviceTopo() {
        return deviceTopo;
    }

    /** @param deviceTopo 设备拓扑; */
    public void setDeviceTopo(SelfDiagnosisDeviceTopoParams deviceTopo) {
        this.deviceTopo = deviceTopo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateSelfDiagnosisParams that = (CreateSelfDiagnosisParams) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && type == that.type
                && part == that.part
                && index == that.index
                && motorAction == that.motorAction
                && Objects.equals(deviceTopo, that.deviceTopo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceSn,
                type,
                part,
                index,
                motorAction,
                deviceTopo);
    }

    @Override
    public String toString() {
        return "CreateSelfDiagnosisParams{"
                + "deviceSn='" + deviceSn + '\''
                + ", type=" + type
                + ", part=" + part
                + ", index=" + index
                + ", motorAction=" + motorAction
                + ", deviceTopo=" + deviceTopo
                + '}';
    }
}
