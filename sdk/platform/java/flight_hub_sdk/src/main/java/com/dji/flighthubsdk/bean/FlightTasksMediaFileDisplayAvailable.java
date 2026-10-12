// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞行任务媒体文件可展示。 */
public final class FlightTasksMediaFileDisplayAvailable {

    private String uuid;
    private int idxParentId;
    private int fileType;
    private int subFileType;
    private int mediaFileId;
    private String flightId;
    private String sn;
    private int mediaSource;
    private String url;
    public FlightTasksMediaFileDisplayAvailable() {
    }

/**
     * 构造 FlightTasksMediaFileDisplayAvailable。
     *
     * @param uuid UUID
     * @param idxParentId 父级索引 ID
     * @param fileType 文件类型
     * @param subFileType 子文件类型
     * @param mediaFileId 媒体文件 ID
     * @param flightId 飞行 ID
     * @param sn 设备序列号
     * @param mediaSource 媒体来源
     * @param url URL 地址
     */
    public FlightTasksMediaFileDisplayAvailable(
            String uuid,
            int idxParentId,
            int fileType,
            int subFileType,
            int mediaFileId,
            String flightId,
            String sn,
            int mediaSource,
            String url) {
        this.uuid = uuid != null ? uuid : "";
        this.idxParentId = idxParentId;
        this.fileType = fileType;
        this.subFileType = subFileType;
        this.mediaFileId = mediaFileId;
        this.flightId = flightId != null ? flightId : "";
        this.sn = sn != null ? sn : "";
        this.mediaSource = mediaSource;
        this.url = url != null ? url : "";
    }

    /** UUID */
    public String getUuid() { return uuid; }
    /** 父级索引 ID */
    public int getIdxParentId() { return idxParentId; }
    /** 文件类型 */
    public int getFileType() { return fileType; }
    /** 子文件类型 */
    public int getSubFileType() { return subFileType; }
    /** 媒体文件 ID */
    public int getMediaFileId() { return mediaFileId; }
    /** 飞行 ID */
    public String getFlightId() { return flightId; }
    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 媒体来源 */
    public int getMediaSource() { return mediaSource; }
    /** URL 地址 */
    public String getUrl() { return url; }

    /** UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 父级索引 ID */
    public void setIdxParentId(int idxParentId) {
        this.idxParentId = idxParentId;
    }

    /** 文件类型 */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    /** 子文件类型 */
    public void setSubFileType(int subFileType) {
        this.subFileType = subFileType;
    }

    /** 媒体文件 ID */
    public void setMediaFileId(int mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    /** 飞行 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 媒体来源 */
    public void setMediaSource(int mediaSource) {
        this.mediaSource = mediaSource;
    }

    /** URL 地址 */
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
        FlightTasksMediaFileDisplayAvailable that = (FlightTasksMediaFileDisplayAvailable) o;
        return Objects.equals(uuid, that.uuid)
                && idxParentId == that.idxParentId
                && fileType == that.fileType
                && subFileType == that.subFileType
                && mediaFileId == that.mediaFileId
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(sn, that.sn)
                && mediaSource == that.mediaSource
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid,
                idxParentId,
                fileType,
                subFileType,
                mediaFileId,
                flightId,
                sn,
                mediaSource,
                url);
    }

    @Override
    public String toString() {
        return "FlightTasksMediaFileDisplayAvailable{"
                + "uuid='" + uuid + '\''
                + ", idxParentId=" + idxParentId
                + ", fileType=" + fileType
                + ", subFileType=" + subFileType
                + ", mediaFileId=" + mediaFileId
                + ", flightId='" + flightId + '\''
                + ", sn='" + sn + '\''
                + ", mediaSource=" + mediaSource
                + ", url='" + url + '\''
                + '}';
    }
}
