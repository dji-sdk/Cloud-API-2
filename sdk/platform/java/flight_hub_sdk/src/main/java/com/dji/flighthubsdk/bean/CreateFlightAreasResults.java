// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建飞行区域结果。 */
public final class CreateFlightAreasResults {
    private String id;
    private String name;
    public CreateFlightAreasResults() {
    }

/**
     * 构造 CreateFlightAreasResults。
     *
     * @param id 标识
     * @param name 名称
     */
    public CreateFlightAreasResults(String id, String name) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
    }

    /** 标识 */
    public String getId() {
        return id;
    }

    /** 标识 */
    public void setId(String id) {
        this.id = id;
    }

    /** 名称 */
    public String getName() {
        return name;
    }

    /** 名称 */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateFlightAreasResults that = (CreateFlightAreasResults) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "CreateFlightAreasResults{id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
