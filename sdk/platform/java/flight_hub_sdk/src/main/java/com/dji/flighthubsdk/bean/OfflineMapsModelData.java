// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 离线地图模型数据。 */
public final class OfflineMapsModelData {
    /** 离线地图模型ID; */
    private int id;
    /** 更新时间; */
    private long updatedTime;
    /** 模型信息; */
    private List<OfflineMapsModelInfoData> models;
    /** 同步进度百分比; */
    private int percent;
    /** 状态。枚举："finish"=完成; */
    private String status;
    /** 结果码; */
    private int result;
    public OfflineMapsModelData() {
    }

/**
     * 构造 OfflineMapsModelData。
     *
     * @param id ID
     * @param updatedTime 更新时间
     * @param models 离线地图模型列表
     * @param percent 同步进度百分比
     * @param status 状态。枚举："finish"=完成
     * @param result 结果码
     */
    public OfflineMapsModelData(
            int id,
            long updatedTime,
            List<OfflineMapsModelInfoData> models,
            int percent,
            String status,
            int result) {
        this.id = id;
        this.updatedTime = updatedTime;
        this.models = models != null ? models : Collections.emptyList();
        this.percent = percent;
        this.status = status != null ? status : "";
        this.result = result;
    }

    /** 离线地图模型ID; */
    public int getId() {
        return id;
    }

    /** @param id 离线地图模型ID; */
    public void setId(int id) {
        this.id = id;
    }

    /** 更新时间; */
    public long getUpdatedTime() {
        return updatedTime;
    }

    /** @param updatedTime 更新时间; */
    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    /** 模型信息; */
    public List<OfflineMapsModelInfoData> getModels() {
        return models;
    }

    /** @param models 模型信息; */
    public void setModels(List<OfflineMapsModelInfoData> models) {
        this.models = models;
    }

    /** 同步进度百分比; */
    public int getPercent() {
        return percent;
    }

    /** @param percent 同步进度百分比; */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    /** 状态。枚举："finish"=完成; */
    public String getStatus() {
        return status;
    }

    /** @param status 状态。枚举："finish"=完成; */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 结果码; */
    public int getResult() {
        return result;
    }

    /** @param result 结果码; */
    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OfflineMapsModelData that = (OfflineMapsModelData) o;
        return id == that.id
                && updatedTime == that.updatedTime
                && Objects.equals(models, that.models)
                && percent == that.percent
                && Objects.equals(status, that.status)
                && result == that.result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                updatedTime,
                models,
                percent,
                status,
                result);
    }

    @Override
    public String toString() {
        return "OfflineMapsModelData{"
                + "id=" + id
                + ", updatedTime=" + updatedTime
                + ", models=" + models
                + ", percent=" + percent
                + ", status='" + status + '\''
                + ", result=" + result
                + '}';
    }
}
