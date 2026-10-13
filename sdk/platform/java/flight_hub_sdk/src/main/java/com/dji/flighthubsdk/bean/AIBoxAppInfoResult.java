// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 应用信息 */
public final class AIBoxAppInfoResult {
    private String id;
    private String name;
    private String version;
    private String description;
    private String publisher;
    private double appSize;
    private double dataSize;
    private int type;
    public AIBoxAppInfoResult() {
    }

/**
     * 构造 AIBoxAppInfoResult。
     *
     * @param id 应用ID
     * @param name 应用名称
     * @param version 应用版本
     * @param description 应用描述
     * @param publisher 发布者
     * @param appSize 应用大小
     * @param dataSize 数据大小
     * @param type 应用类型
     */
    public AIBoxAppInfoResult(String id, String name, String version, String description, String publisher, double appSize, double dataSize, int type) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.version = version != null ? version : "";
        this.description = description != null ? description : "";
        this.publisher = publisher != null ? publisher : "";
        this.appSize = appSize;
        this.dataSize = dataSize;
        this.type = type;
    }

    /** 应用ID */
    public String getId() { return id; }
    /** 应用名称 */
    public String getName() { return name; }
    /** 应用版本 */
    public String getVersion() { return version; }
    /** 应用描述 */
    public String getDescription() { return description; }
    /** 发布者 */
    public String getPublisher() { return publisher; }
    /** 应用大小 */
    public double getAppSize() { return appSize; }
    /** 数据大小 */
    public double getDataSize() { return dataSize; }
    /** 应用类型 */
    public int getType() { return type; }

    /** 应用ID */
    public void setId(String id) {
        this.id = id;
    }

    /** 应用名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 应用版本 */
    public void setVersion(String version) {
        this.version = version;
    }

    /** 应用描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** 发布者 */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /** 应用大小 */
    public void setAppSize(double appSize) {
        this.appSize = appSize;
    }

    /** 数据大小 */
    public void setDataSize(double dataSize) {
        this.dataSize = dataSize;
    }

    /** 应用类型 */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxAppInfoResult that = (AIBoxAppInfoResult) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(version, that.version)
                && Objects.equals(description, that.description)
                && Objects.equals(publisher, that.publisher)
                && Double.compare(that.appSize, appSize) == 0
                && Double.compare(that.dataSize, dataSize) == 0
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                version,
                description,
                publisher,
                appSize,
                dataSize,
                type);
    }

    @Override
    public String toString() {
        return "AIBoxAppInfoResult{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", version='" + version + '\''
                + ", description='" + description + '\''
                + ", publisher='" + publisher + '\''
                + ", appSize=" + appSize
                + ", dataSize=" + dataSize
                + ", type=" + type
                + '}';
    }
}
