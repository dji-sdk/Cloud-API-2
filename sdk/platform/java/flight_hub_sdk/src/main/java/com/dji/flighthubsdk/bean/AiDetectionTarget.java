// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** AI 检测目标。 */
public final class AiDetectionTarget {

    /** 目标 ID */
    private int targetId;
    /** 目标类型 */
    private int targetType;
    /** 目标状态 */
    private int targetState;
    /** 目标中心 x 坐标 */
    private int targetCx;
    /** 目标中心 y 坐标 */
    private int targetCy;
    /** 目标宽度 */
    private int targetW;
    /** 目标高度 */
    private int targetH;
    /** 目标距离 */
    private long targetDistance;
    public AiDetectionTarget() {
    }

/**
     * 构造 AiDetectionTarget。
     *
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param targetState 目标状态
     * @param targetCx 目标中心X坐标
     * @param targetCy 目标中心Y坐标
     * @param targetW 目标宽度
     * @param targetH 目标高度
     * @param targetDistance 目标距离
     */
    public AiDetectionTarget(
            int targetId,
            int targetType,
            int targetState,
            int targetCx,
            int targetCy,
            int targetW,
            int targetH,
            long targetDistance) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.targetState = targetState;
        this.targetCx = targetCx;
        this.targetCy = targetCy;
        this.targetW = targetW;
        this.targetH = targetH;
        this.targetDistance = targetDistance;
    }

    /** 目标 ID */
    public int getTargetId() {
        return targetId;
    }

    /** @param targetId 目标 ID */
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    /** 目标类型 */
    public int getTargetType() {
        return targetType;
    }

    /** 目标类型 */
    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    /** 目标状态 */
    public int getTargetState() {
        return targetState;
    }

    /** 目标状态 */
    public void setTargetState(int targetState) {
        this.targetState = targetState;
    }

    /** 目标中心 x 坐标 */
    public int getTargetCx() {
        return targetCx;
    }

    /** @param targetCx 目标中心 x 坐标 */
    public void setTargetCx(int targetCx) {
        this.targetCx = targetCx;
    }

    /** 目标中心 y 坐标 */
    public int getTargetCy() {
        return targetCy;
    }

    /** @param targetCy 目标中心 y 坐标 */
    public void setTargetCy(int targetCy) {
        this.targetCy = targetCy;
    }

    /** 目标宽度 */
    public int getTargetW() {
        return targetW;
    }

    /** 目标宽度 */
    public void setTargetW(int targetW) {
        this.targetW = targetW;
    }

    /** 目标高度 */
    public int getTargetH() {
        return targetH;
    }

    /** 目标高度 */
    public void setTargetH(int targetH) {
        this.targetH = targetH;
    }

    /** 目标距离 */
    public long getTargetDistance() {
        return targetDistance;
    }

    /** 目标距离 */
    public void setTargetDistance(long targetDistance) {
        this.targetDistance = targetDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiDetectionTarget that = (AiDetectionTarget) o;
        return targetId == that.targetId
                && targetType == that.targetType
                && targetState == that.targetState
                && targetCx == that.targetCx
                && targetCy == that.targetCy
                && targetW == that.targetW
                && targetH == that.targetH
                && targetDistance == that.targetDistance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                targetId,
                targetType,
                targetState,
                targetCx,
                targetCy,
                targetW,
                targetH,
                targetDistance);
    }

    @Override
    public String toString() {
        return "AiDetectionTarget{"
                + "targetId=" + targetId
                + ", targetType=" + targetType
                + ", targetState=" + targetState
                + ", targetCx=" + targetCx
                + ", targetCy=" + targetCy
                + ", targetW=" + targetW
                + ", targetH=" + targetH
                + ", targetDistance=" + targetDistance
                + '}';
    }
}
