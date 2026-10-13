// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 飞机控制码。 */
public final class AircraftControlCode {

    /** "0": "航线控制-执行态", "1": "指令控制-手动飞行", "2": "指令控制-自动飞行", "3": "航线控制-中断态" */
    private int controlMode;
    public AircraftControlCode() {
    }

/**
     * 构造 AircraftControlCode。
     *
     * @param controlMode 控制模式，取值：
     *                    <ul>
     *                      <li>0：航线控制-执行态</li>
     *                      <li>1：指令控制-手动飞行</li>
     *                      <li>2：指令控制-自动飞行</li>
     *                      <li>3：航线控制-中断态</li>
     *                    </ul>
     */
    public AircraftControlCode(int controlMode) {
        this.controlMode = controlMode;
    }

    /** "0": "航线控制-执行态", "1": "指令控制-手动飞行", "2": "指令控制-自动飞行", "3": "航线控制-中断态" */
    public int getControlMode() {
        return controlMode;
    }

    /** @param controlMode "0": "航线控制-执行态", "1": "指令控制-手动飞行", "2": "指令控制-自动飞行", "3": "航线控制-中断态" */
    public void setControlMode(int controlMode) {
        this.controlMode = controlMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftControlCode that = (AircraftControlCode) o;
        return controlMode == that.controlMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(controlMode);
    }

    @Override
    public String toString() {
        return "AircraftControlCode{controlMode=" + controlMode + '}';
    }
}
