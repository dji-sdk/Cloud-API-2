// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.OutOfControlAction;

import java.util.Objects;

/**
 * 舱外起飞参数。对应 <b>GroundTakeoffRequest</b>。
 * SDK 实际请求：{@code POST /atc/api/v1/flight-tasks/ground-take-off}；
 * {@code device_sn} 由 Module 注入网关 SN，调用方无需填写。
 */
public class GroundTakeoffParams {
    /** 自动起飞高度（米），范围 2-1500（必填） */
    private int autoTakeoffHeight;
    /** 失控动作（必填）。Hover/ReturnHome/Continue */
    private OutOfControlAction outOfControlAction = OutOfControlAction.RETURN_HOME;

    public GroundTakeoffParams() {
    }

    /**
     * @param autoTakeoffHeight 自动起飞高度（米），范围 2-1500（必填）
     * @param outOfControlAction 失控动作（必填）
     */
    public GroundTakeoffParams(int autoTakeoffHeight, OutOfControlAction outOfControlAction) {
        this.autoTakeoffHeight = autoTakeoffHeight;
        this.outOfControlAction = outOfControlAction != null ? outOfControlAction : OutOfControlAction.RETURN_HOME;
    }

    /** 自动起飞高度（米），范围 2-1500（必填） */
    public int getAutoTakeoffHeight() { return autoTakeoffHeight; }
    /** @param autoTakeoffHeight 自动起飞高度（米），范围 2-1500（必填） */
    public void setAutoTakeoffHeight(int autoTakeoffHeight) {
        this.autoTakeoffHeight = autoTakeoffHeight;
    }

    /** 失控动作（必填）。Hover/ReturnHome/Continue */
    public OutOfControlAction getOutOfControlAction() { return outOfControlAction; }
    /** @param outOfControlAction 失控动作（必填）。Hover/ReturnHome/Continue */
    public void setOutOfControlAction(OutOfControlAction outOfControlAction) {
        this.outOfControlAction = outOfControlAction != null ? outOfControlAction : OutOfControlAction.RETURN_HOME;
    }

    @Override
    public String toString() {
        return "GroundTakeoffParams{autoTakeoffHeight=" + autoTakeoffHeight
                + ", outOfControlAction=" + outOfControlAction + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroundTakeoffParams that = (GroundTakeoffParams) o;
        return autoTakeoffHeight == that.autoTakeoffHeight
                && outOfControlAction == that.outOfControlAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoTakeoffHeight, outOfControlAction);
    }
}
