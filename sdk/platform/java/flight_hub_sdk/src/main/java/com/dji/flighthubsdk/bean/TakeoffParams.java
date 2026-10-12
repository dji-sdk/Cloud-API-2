// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.OutOfControlAction;
import com.dji.flighthubsdk.value.TakeoffMode;

import java.util.Objects;

/**
 * 指令飞行 / 一键起飞参数。
 * 对应 <b>CommandRequest</b>（与 DroneTakeoffRequest 字段一致）。
 * SDK 实际请求：{@code POST /atc/api/v1/flight-tasks/command}；{@code device_sn} 由 Module 注入，调用方无需填写。
 * 可选数值字段默认 <b>-1</b>、字符串默认空：表示未设置，不写入 JSON。
 */
public class TakeoffParams {
    // --- 必填 ---
    /** 安全起飞高度（米），范围 8-1500（必填） */
    private double securityTakeoffHeight;
    /** 最大速度（米/秒）（必填） */
    private int maxSpeed;
    /** 失控动作（必填）。Hover/ReturnHome/Continue。参见 `OutOfControlAction`（`value.h`） */
    private OutOfControlAction outOfControlAction = OutOfControlAction.RETURN_HOME;
    /** 返航高度（米），范围 15-1500（必填） */
    private int rthAltitude;
    /** 目标高度（米），范围 2-10000（必填） */
    private double targetHeight;
    /** 起飞模式（必填）。Takeoff/TakeoffWithFlyTo。参见 `TakeoffMode`（`value.h`） */
    private TakeoffMode takeoffMode = TakeoffMode.TAKEOFF;

    // --- 坐标；随请求体下发 ---
    /** 目标纬度 */
    private double targetLatitude;
    /** 目标经度 */
    private double targetLongitude;

    // --- 可选（未设置则不传）---
    /** 指点飞行模式：0-平稳模式，1-运动模式。-1 表示未设置 */
    private int commanderFlightMode = -1;
    /** 指点飞行高度（米），范围 20-1500。-1 表示未设置 */
    private double commanderFlightHeight = -1;
    /** 返航模式：0-智能高度，1-设定高度。-1 表示未设置 */
    private int rthMode = -1;
    /** 云云对接存储桶 ID。空串表示未设置 */
    private String cloudToCloudId = "";
    /** 是否需要验证飞行控制权。-1 未设置；0=false；1=true */
    private int needVerifyFlightControl = -1;

    public TakeoffParams() {
    }

    /** 安全起飞高度（米），范围 8-1500（必填） */
    public double getSecurityTakeoffHeight() { return securityTakeoffHeight; }
    /** @param securityTakeoffHeight 安全起飞高度（米），范围 8-1500（必填） */
    public void setSecurityTakeoffHeight(double securityTakeoffHeight) {
        this.securityTakeoffHeight = securityTakeoffHeight;
    }

    /** 最大速度（米/秒）（必填） */
    public int getMaxSpeed() { return maxSpeed; }
    /** @param maxSpeed 最大速度（米/秒）（必填） */
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }

    /** 失控动作（必填）。Hover/ReturnHome/Continue。参见 `OutOfControlAction`（`value.h`） */
    public OutOfControlAction getOutOfControlAction() { return outOfControlAction; }
    /** @param outOfControlAction 失控动作（必填）。Hover/ReturnHome/Continue。参见 `OutOfControlAction`（`value.h`） */
    public void setOutOfControlAction(OutOfControlAction outOfControlAction) {
        this.outOfControlAction = outOfControlAction != null ? outOfControlAction : OutOfControlAction.RETURN_HOME;
    }

    /** 返航高度（米），范围 15-1500（必填） */
    public int getRthAltitude() { return rthAltitude; }
    /** @param rthAltitude 返航高度（米），范围 15-1500（必填） */
    public void setRthAltitude(int rthAltitude) { this.rthAltitude = rthAltitude; }

    /** 目标高度（米），范围 2-10000（必填） */
    public double getTargetHeight() { return targetHeight; }
    /** @param targetHeight 目标高度（米），范围 2-10000（必填） */
    public void setTargetHeight(double targetHeight) { this.targetHeight = targetHeight; }

    /** 目标纬度 */
    public double getTargetLatitude() { return targetLatitude; }
    /** @param targetLatitude 目标纬度 */
    public void setTargetLatitude(double targetLatitude) { this.targetLatitude = targetLatitude; }

    /** 目标经度 */
    public double getTargetLongitude() { return targetLongitude; }
    /** @param targetLongitude 目标经度 */
    public void setTargetLongitude(double targetLongitude) { this.targetLongitude = targetLongitude; }

    /** 起飞模式（必填）。Takeoff/TakeoffWithFlyTo。参见 `TakeoffMode`（`value.h`） */
    public TakeoffMode getTakeoffMode() { return takeoffMode; }
    /** @param takeoffMode 起飞模式（必填）。Takeoff/TakeoffWithFlyTo。参见 `TakeoffMode`（`value.h`） */
    public void setTakeoffMode(TakeoffMode takeoffMode) {
        this.takeoffMode = takeoffMode != null ? takeoffMode : TakeoffMode.TAKEOFF;
    }

    /** 指点飞行模式：0-平稳模式，1-运动模式。-1 表示未设置 */
    public int getCommanderFlightMode() { return commanderFlightMode; }
    /** @param commanderFlightMode 指点飞行模式：0-平稳模式，1-运动模式。-1 表示未设置 */
    public void setCommanderFlightMode(int commanderFlightMode) {
        this.commanderFlightMode = commanderFlightMode;
    }

    /** 指点飞行高度（米），范围 20-1500。-1 表示未设置 */
    public double getCommanderFlightHeight() { return commanderFlightHeight; }
    /** @param commanderFlightHeight 指点飞行高度（米），范围 20-1500。-1 表示未设置 */
    public void setCommanderFlightHeight(double commanderFlightHeight) {
        this.commanderFlightHeight = commanderFlightHeight;
    }

    /** 返航模式：0-智能高度，1-设定高度。-1 表示未设置 */
    public int getRthMode() { return rthMode; }
    /** @param rthMode 返航模式：0-智能高度，1-设定高度。-1 表示未设置 */
    public void setRthMode(int rthMode) { this.rthMode = rthMode; }

    /** 云云对接存储桶 ID。空串表示未设置 */
    public String getCloudToCloudId() { return cloudToCloudId; }
    /** @param cloudToCloudId 云云对接存储桶 ID。空串表示未设置 */
    public void setCloudToCloudId(String cloudToCloudId) {
        this.cloudToCloudId = cloudToCloudId != null ? cloudToCloudId : "";
    }

    /** 是否需要验证飞行控制权。-1 未设置；0=false；1=true */
    public int getNeedVerifyFlightControl() { return needVerifyFlightControl; }
    /** @param needVerifyFlightControl 是否需要验证飞行控制权。-1 未设置；0=false；1=true */
    public void setNeedVerifyFlightControl(int needVerifyFlightControl) {
        this.needVerifyFlightControl = needVerifyFlightControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TakeoffParams that = (TakeoffParams) o;
        return Double.compare(that.securityTakeoffHeight, securityTakeoffHeight) == 0
                && maxSpeed == that.maxSpeed
                && Objects.equals(outOfControlAction, that.outOfControlAction)
                && rthAltitude == that.rthAltitude
                && Double.compare(that.targetHeight, targetHeight) == 0
                && Double.compare(that.targetLatitude, targetLatitude) == 0
                && Double.compare(that.targetLongitude, targetLongitude) == 0
                && Objects.equals(takeoffMode, that.takeoffMode)
                && commanderFlightMode == that.commanderFlightMode
                && Double.compare(that.commanderFlightHeight, commanderFlightHeight) == 0
                && rthMode == that.rthMode
                && Objects.equals(cloudToCloudId, that.cloudToCloudId)
                && needVerifyFlightControl == that.needVerifyFlightControl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                securityTakeoffHeight,
                maxSpeed,
                outOfControlAction,
                rthAltitude,
                targetHeight,
                targetLatitude,
                targetLongitude,
                takeoffMode,
                commanderFlightMode,
                commanderFlightHeight,
                rthMode,
                cloudToCloudId,
                needVerifyFlightControl);
    }

    @Override
    public String toString() {
        return "TakeoffParams{"
                + "securityTakeoffHeight=" + securityTakeoffHeight
                + ", maxSpeed=" + maxSpeed
                + ", outOfControlAction=" + outOfControlAction
                + ", rthAltitude=" + rthAltitude
                + ", targetHeight=" + targetHeight
                + ", targetLatitude=" + targetLatitude
                + ", targetLongitude=" + targetLongitude
                + ", takeoffMode=" + takeoffMode
                + ", commanderFlightMode=" + commanderFlightMode
                + ", commanderFlightHeight=" + commanderFlightHeight
                + ", rthMode=" + rthMode
                + ", cloudToCloudId='" + cloudToCloudId + '\''
                + ", needVerifyFlightControl=" + needVerifyFlightControl
                + '}';
    }
}
