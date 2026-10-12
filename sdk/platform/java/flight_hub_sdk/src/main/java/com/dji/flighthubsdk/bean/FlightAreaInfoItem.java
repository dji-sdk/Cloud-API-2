// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞行区域信息条目。 */
public final class FlightAreaInfoItem {
    /** 飞行区id; */
    private String id;
    /** 飞行区名称; */
    private String name;
    /** 飞行区状态; */
    private String status;
    /** 飞行区类型; */
    private String type;
    /** 飞行区内容; */
    private FlightAreasContentData content;
    /** 创建时间; */
    private long createdTime;
    /** 创建用户 id; */
    private String createdBy;
    /** 更新时间; */
    private long updatedTime;
    /** 更新用户 id; */
    private String updatedBy;
    public FlightAreaInfoItem() {
    }

/**
     * 构造 FlightAreaInfoItem。
     *
     * @param id 区域 ID
     * @param name 区域名称
     * @param status 状态。枚举："disable"=已禁用, "enable"=已启用
     * @param type 飞行区类型。枚举："dfence"=作业区, "nfz"=限飞区, "noland"=禁降区
     * @param content 区域内容数据
     * @param createdTime 创建时间
     * @param createdBy 创建者
     * @param updatedTime 更新时间
     * @param updatedBy 更新者
     */
    public FlightAreaInfoItem(
            String id,
            String name,
            String status,
            String type,
            FlightAreasContentData content,
            long createdTime,
            String createdBy,
            long updatedTime,
            String updatedBy) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.status = status != null ? status : "";
        this.type = type != null ? type : "";
        this.content = content;
        this.createdTime = createdTime;
        this.createdBy = createdBy != null ? createdBy : "";
        this.updatedTime = updatedTime;
        this.updatedBy = updatedBy != null ? updatedBy : "";
    }

    /** 飞行区id; */
    public String getId() { return id; }
    /** 飞行区名称; */
    public String getName() { return name; }
    /** 飞行区状态; */
    public String getStatus() { return status; }
    /** 飞行区类型; */
    public String getType() { return type; }
    /** 飞行区内容; */
    public FlightAreasContentData getContent() { return content; }
    /** 创建时间; */
    public long getCreatedTime() { return createdTime; }
    /** 创建用户 id; */
    public String getCreatedBy() { return createdBy; }
    /** 更新时间; */
    public long getUpdatedTime() { return updatedTime; }
    /** 更新用户 id; */
    public String getUpdatedBy() { return updatedBy; }

    /** @param id 飞行区id; */
    public void setId(String id) {
        this.id = id;
    }

    /** @param name 飞行区名称; */
    public void setName(String name) {
        this.name = name;
    }

    /** @param status 飞行区状态; */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @param type 飞行区类型; */
    public void setType(String type) {
        this.type = type;
    }

    /** @param content 飞行区内容; */
    public void setContent(FlightAreasContentData content) {
        this.content = content;
    }

    /** @param createdTime 创建时间; */
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    /** @param createdBy 创建用户 id; */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** @param updatedTime 更新时间; */
    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    /** @param updatedBy 更新用户 id; */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightAreaInfoItem that = (FlightAreaInfoItem) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(status, that.status)
                && Objects.equals(type, that.type)
                && Objects.equals(content, that.content)
                && createdTime == that.createdTime
                && Objects.equals(createdBy, that.createdBy)
                && updatedTime == that.updatedTime
                && Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                status,
                type,
                content,
                createdTime,
                createdBy,
                updatedTime,
                updatedBy);
    }

    @Override
    public String toString() {
        return "FlightAreaInfoItem{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", status='" + status + '\''
                + ", type='" + type + '\''
                + ", content=" + content
                + ", createdTime=" + createdTime
                + ", createdBy='" + createdBy + '\''
                + ", updatedTime=" + updatedTime
                + ", updatedBy='" + updatedBy + '\''
                + '}';
    }
}
