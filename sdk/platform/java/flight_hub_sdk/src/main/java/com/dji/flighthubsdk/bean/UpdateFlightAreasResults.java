// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 更新飞行区域结果。 */
public final class UpdateFlightAreasResults {
    private String id;
    private UpdatedAreaData updatedArea;
    public UpdateFlightAreasResults() {
    }

/**
     * 构造 UpdateFlightAreasResults。
     *
     * @param id 区域ID
     * @param updatedArea 更新后的区域数据
     */
    public UpdateFlightAreasResults(String id, UpdatedAreaData updatedArea) {
        this.id = id != null ? id : "";
        this.updatedArea = updatedArea;
    }

    /** 区域ID */
    public String getId() { return id; }
    /** 更新后的区域数据 */
    public UpdatedAreaData getUpdatedArea() { return updatedArea; }

    /** 区域ID */
    public void setId(String id) {
        this.id = id;
    }

    /** 更新后的区域数据 */
    public void setUpdatedArea(UpdatedAreaData updatedArea) {
        this.updatedArea = updatedArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateFlightAreasResults that = (UpdateFlightAreasResults) o;
        return Objects.equals(id, that.id)
                && Objects.equals(updatedArea, that.updatedArea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, updatedArea);
    }

    @Override
    public String toString() {
        return "UpdateFlightAreasResults{id='" + id + '\'' + ", updatedArea=" + updatedArea + '}';
    }
}
