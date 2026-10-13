// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 已更新区域数据。 */
public final class UpdatedAreaData {
    private String id;
    private String name;
    private String status;
    private String type;
    /** 飞行区内容; */
    private FlightAreasContentData content;
    private long createdTime;
    private String createdBy;
    private long updatedTime;
    private String updatedBy;
    public UpdatedAreaData() {
    }

/**
     * 构造 UpdatedAreaData。
     *
     * @param id ID
     * @param name 名称
     * @param status 状态
     * @param type 类型
     * @param content 区域内容数据
     * @param createdTime 创建时间
     * @param createdBy 创建者
     * @param updatedTime 更新时间
     * @param updatedBy 更新者
     */
    public UpdatedAreaData(
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

    /** ID */
    public String getId() { return id; }
    /** 名称 */
    public String getName() { return name; }
    /** 状态 */
    public String getStatus() { return status; }
    /** 类型 */
    public String getType() { return type; }
    /** 飞行区内容; */
    public FlightAreasContentData getContent() { return content; }
    /** 创建时间 */
    public long getCreatedTime() { return createdTime; }
    /** 创建者 */
    public String getCreatedBy() { return createdBy; }
    /** 更新时间 */
    public long getUpdatedTime() { return updatedTime; }
    /** 更新者 */
    public String getUpdatedBy() { return updatedBy; }

    /** ID */
    public void setId(String id) {
        this.id = id;
    }

    /** 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @param content 飞行区内容; */
    public void setContent(FlightAreasContentData content) {
        this.content = content;
    }

    /** 创建时间 */
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    /** 创建者 */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** 更新时间 */
    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    /** 更新者 */
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
        UpdatedAreaData that = (UpdatedAreaData) o;
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
        return "UpdatedAreaData{"
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
