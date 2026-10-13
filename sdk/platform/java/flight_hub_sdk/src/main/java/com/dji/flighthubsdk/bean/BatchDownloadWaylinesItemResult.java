// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 批量下载航线 - 单项结果 */
public final class BatchDownloadWaylinesItemResult {

    private String waylineUuid;
    private String waylineName;
    private String kmzObjectKey;
    private String url;
    private int size;
    public BatchDownloadWaylinesItemResult() {
    }

/**
     * 构造 BatchDownloadWaylinesItemResult。
     *
     * @param waylineUuid 航线 UUID
     * @param waylineName 航线名称
     * @param kmzObjectKey KMZ 文件对象存储 key
     * @param url 下载地址
     * @param size 文件大小
     */
    public BatchDownloadWaylinesItemResult(
            String waylineUuid, String waylineName, String kmzObjectKey, String url, int size) {
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.waylineName = waylineName != null ? waylineName : "";
        this.kmzObjectKey = kmzObjectKey != null ? kmzObjectKey : "";
        this.url = url != null ? url : "";
        this.size = size;
    }

    /** 航线 UUID */
    public String getWaylineUuid() {
        return waylineUuid;
    }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** 航线名称 */
    public String getWaylineName() {
        return waylineName;
    }

    /** 航线名称 */
    public void setWaylineName(String waylineName) {
        this.waylineName = waylineName;
    }

    /** KMZ 文件对象存储 key */
    public String getKmzObjectKey() {
        return kmzObjectKey;
    }

    /** KMZ 文件对象存储 key */
    public void setKmzObjectKey(String kmzObjectKey) {
        this.kmzObjectKey = kmzObjectKey;
    }

    /** 下载地址 */
    public String getUrl() {
        return url;
    }

    /** 下载地址 */
    public void setUrl(String url) {
        this.url = url;
    }

    /** 文件大小 */
    public int getSize() {
        return size;
    }

    /** 文件大小 */
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BatchDownloadWaylinesItemResult that = (BatchDownloadWaylinesItemResult) o;
        return Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(waylineName, that.waylineName)
                && Objects.equals(kmzObjectKey, that.kmzObjectKey)
                && Objects.equals(url, that.url)
                && size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                waylineUuid,
                waylineName,
                kmzObjectKey,
                url,
                size);
    }

    @Override
    public String toString() {
        return "BatchDownloadWaylinesItemResult{"
                + "waylineUuid='" + waylineUuid + '\''
                + ", waylineName='" + waylineName + '\''
                + ", kmzObjectKey='" + kmzObjectKey + '\''
                + ", url='" + url + '\''
                + ", size=" + size
                + '}';
    }
}
