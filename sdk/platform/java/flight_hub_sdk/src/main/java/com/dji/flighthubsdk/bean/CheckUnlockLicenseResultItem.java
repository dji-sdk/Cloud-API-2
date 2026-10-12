// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 校验解锁证书 - 单项结果 */
public final class CheckUnlockLicenseResultItem {

    private String fileName;
    private String name;
    private String type;
    private long beginTime;
    private long endTime;
    private String url;
    public CheckUnlockLicenseResultItem() {
    }

/**
     * 构造 CheckUnlockLicenseResultItem。
     *
     * @param fileName 文件名
     * @param name 许可证名称
     * @param type 许可证类型
     * @param beginTime 有效期开始时间
     * @param endTime 有效期结束时间
     * @param url 文件下载地址
     */
    public CheckUnlockLicenseResultItem(
            String fileName, String name, String type, long beginTime, long endTime, String url) {
        this.fileName = fileName != null ? fileName : "";
        this.name = name != null ? name : "";
        this.type = type != null ? type : "";
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.url = url != null ? url : "";
    }

    /** 文件名 */
    public String getFileName() {
        return fileName;
    }

    /** 文件名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** 许可证名称 */
    public String getName() {
        return name;
    }

    /** 许可证名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 许可证类型 */
    public String getType() {
        return type;
    }

    /** 许可证类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** 有效期开始时间 */
    public long getBeginTime() {
        return beginTime;
    }

    /** 有效期开始时间 */
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    /** 有效期结束时间 */
    public long getEndTime() {
        return endTime;
    }

    /** 有效期结束时间 */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /** 文件下载地址 */
    public String getUrl() {
        return url;
    }

    /** 文件下载地址 */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckUnlockLicenseResultItem that = (CheckUnlockLicenseResultItem) o;
        return Objects.equals(fileName, that.fileName)
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && beginTime == that.beginTime
                && endTime == that.endTime
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fileName,
                name,
                type,
                beginTime,
                endTime,
                url);
    }

    @Override
    public String toString() {
        return "CheckUnlockLicenseResultItem{"
                + "fileName='" + fileName + '\''
                + ", name='" + name + '\''
                + ", type='" + type + '\''
                + ", beginTime=" + beginTime
                + ", endTime=" + endTime
                + ", url='" + url + '\''
                + '}';
    }
}
