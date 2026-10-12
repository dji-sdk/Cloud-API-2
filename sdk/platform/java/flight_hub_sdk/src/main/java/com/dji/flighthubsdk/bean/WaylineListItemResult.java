// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 航线列表项 */
public final class WaylineListItemResult {

    private String waylineUuid;
    private List<Integer> waylineTypes;
    private String waylineName;
    private int size;
    private List<String> payloadTypes;
    private List<String> payloadTypesWithIndex;
    private String droneType;
    private long updatedAt;
    private boolean withMegaphone;
    private boolean withSearchLight;
    private boolean aiWayline;
    public WaylineListItemResult() {
    }

/**
     * 构造 WaylineListItemResult。
     *
     * @param waylineUuid 航线UUID
     * @param waylineTypes 航线类型列表
     * @param waylineName 航线名称
     * @param size 文件大小
     * @param payloadTypes 负载类型列表
     * @param payloadTypesWithIndex 带索引的负载类型列表
     * @param droneType 飞行器类型
     * @param updatedAt 更新时间
     * @param withMegaphone 是否携带喊话器
     * @param withSearchLight 是否携带探照灯
     * @param aiWayline 是否AI航线
     */
    public WaylineListItemResult(
            String waylineUuid,
            List<Integer> waylineTypes,
            String waylineName,
            int size,
            List<String> payloadTypes,
            List<String> payloadTypesWithIndex,
            String droneType,
            long updatedAt,
            boolean withMegaphone,
            boolean withSearchLight,
            boolean aiWayline) {
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.waylineTypes = waylineTypes != null ? waylineTypes : Collections.emptyList();
        this.waylineName = waylineName != null ? waylineName : "";
        this.size = size;
        this.payloadTypes = payloadTypes != null ? payloadTypes : Collections.emptyList();
        this.payloadTypesWithIndex = payloadTypesWithIndex != null ? payloadTypesWithIndex : Collections.emptyList();
        this.droneType = droneType != null ? droneType : "";
        this.updatedAt = updatedAt;
        this.withMegaphone = withMegaphone;
        this.withSearchLight = withSearchLight;
        this.aiWayline = aiWayline;
    }

    /** 航线UUID */
    public String getWaylineUuid() { return waylineUuid; }
    /** 航线类型列表 */
    public List<Integer> getWaylineTypes() { return waylineTypes; }
    /** 航线名称 */
    public String getWaylineName() { return waylineName; }
    /** 文件大小 */
    public int getSize() { return size; }
    /** 负载类型列表 */
    public List<String> getPayloadTypes() { return payloadTypes; }
    /** 带索引的负载类型列表 */
    public List<String> getPayloadTypesWithIndex() { return payloadTypesWithIndex; }
    /** 飞行器类型 */
    public String getDroneType() { return droneType; }
    /** 更新时间 */
    public long getUpdatedAt() { return updatedAt; }
    /** 是否携带喊话器 */
    public boolean isWithMegaphone() { return withMegaphone; }
    /** 是否携带探照灯 */
    public boolean isWithSearchLight() { return withSearchLight; }
    /** 是否AI航线 */
    public boolean isAiWayline() { return aiWayline; }

    /** 航线UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** 航线类型列表 */
    public void setWaylineTypes(List<Integer> waylineTypes) {
        this.waylineTypes = waylineTypes;
    }

    /** 航线名称 */
    public void setWaylineName(String waylineName) {
        this.waylineName = waylineName;
    }

    /** 文件大小 */
    public void setSize(int size) {
        this.size = size;
    }

    /** 负载类型列表 */
    public void setPayloadTypes(List<String> payloadTypes) {
        this.payloadTypes = payloadTypes;
    }

    /** 带索引的负载类型列表 */
    public void setPayloadTypesWithIndex(List<String> payloadTypesWithIndex) {
        this.payloadTypesWithIndex = payloadTypesWithIndex;
    }

    /** 飞行器类型 */
    public void setDroneType(String droneType) {
        this.droneType = droneType;
    }

    /** 更新时间 */
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** 是否携带喊话器 */
    public void setWithMegaphone(boolean withMegaphone) {
        this.withMegaphone = withMegaphone;
    }

    /** 是否携带探照灯 */
    public void setWithSearchLight(boolean withSearchLight) {
        this.withSearchLight = withSearchLight;
    }

    /** 是否AI航线 */
    public void setAiWayline(boolean aiWayline) {
        this.aiWayline = aiWayline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineListItemResult that = (WaylineListItemResult) o;
        return Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(waylineTypes, that.waylineTypes)
                && Objects.equals(waylineName, that.waylineName)
                && size == that.size
                && Objects.equals(payloadTypes, that.payloadTypes)
                && Objects.equals(payloadTypesWithIndex, that.payloadTypesWithIndex)
                && Objects.equals(droneType, that.droneType)
                && updatedAt == that.updatedAt
                && withMegaphone == that.withMegaphone
                && withSearchLight == that.withSearchLight
                && aiWayline == that.aiWayline;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                waylineUuid,
                waylineTypes,
                waylineName,
                size,
                payloadTypes,
                payloadTypesWithIndex,
                droneType,
                updatedAt,
                withMegaphone,
                withSearchLight,
                aiWayline);
    }

    @Override
    public String toString() {
        return "WaylineListItemResult{"
                + "waylineUuid='" + waylineUuid + '\''
                + ", waylineTypes=" + waylineTypes
                + ", waylineName='" + waylineName + '\''
                + ", size=" + size
                + ", payloadTypes=" + payloadTypes
                + ", payloadTypesWithIndex=" + payloadTypesWithIndex
                + ", droneType='" + droneType + '\''
                + ", updatedAt=" + updatedAt
                + ", withMegaphone=" + withMegaphone
                + ", withSearchLight=" + withSearchLight
                + ", aiWayline=" + aiWayline
                + '}';
    }
}
