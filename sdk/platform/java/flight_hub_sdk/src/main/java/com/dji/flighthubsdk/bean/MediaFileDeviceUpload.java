// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 监听通知。 */
public final class MediaFileDeviceUpload {
    /** 文件 id; */
    private long fileId;
    /** 飞行任务 id; */
    private String flightId;
    /** 设备 SN; */
    private String deviceSn;
    /** 文件对象 key; */
    private String objectKey;
    /** 文件大小(字节); */
    private long objectSize;
    private int fileType;
    public MediaFileDeviceUpload() {
    }

/**
     * 构造 MediaFileDeviceUpload。
     *
     * @param fileId 文件ID
     * @param flightId 飞行任务ID
     * @param deviceSn 设备序列号
     * @param objectKey 对象存储Key
     * @param objectSize 对象大小
     * @param fileType 文件类型（2: 图片, 3: 视频, -1: 未知）
     */
    public MediaFileDeviceUpload(
            long fileId, String flightId, String deviceSn, String objectKey, long objectSize, int fileType) {
        this.fileId = fileId;
        this.flightId = flightId != null ? flightId : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.objectKey = objectKey != null ? objectKey : "";
        this.objectSize = objectSize;
        this.fileType = fileType;
    }

    /** 文件 id; */
    public long getFileId() {
        return fileId;
    }

    /** @param fileId 文件 id; */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /** 飞行任务 id; */
    public String getFlightId() {
        return flightId;
    }

    /** @param flightId 飞行任务 id; */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 设备 SN; */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备 SN; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 文件对象 key; */
    public String getObjectKey() {
        return objectKey;
    }

    /** @param objectKey 文件对象 key; */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 文件大小(字节); */
    public long getObjectSize() {
        return objectSize;
    }

    /** @param objectSize 文件大小(字节); */
    public void setObjectSize(long objectSize) {
        this.objectSize = objectSize;
    }

    /**
     * 文件类型枚举值：
     * <ul>
     *   <li>2 - 图片（photo）</li>
     *   <li>3 - 视频（video）</li>
     *   <li>-1 - 未知/默认</li>
     * </ul>
     */
    public int getFileType() {
        return fileType;
    }

    /**
     * 文件类型枚举值：
     * <ul>
     *   <li>2 - 图片（photo）</li>
     *   <li>3 - 视频（video）</li>
     *   <li>-1 - 未知/默认</li>
     * </ul>
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFileDeviceUpload that = (MediaFileDeviceUpload) o;
        return fileId == that.fileId
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(objectKey, that.objectKey)
                && objectSize == that.objectSize
                && fileType == that.fileType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fileId,
                flightId,
                deviceSn,
                objectKey,
                objectSize,
                fileType);
    }

    @Override
    public String toString() {
        return "MediaFileDeviceUpload{"
                + "fileId=" + fileId
                + ", flightId='" + flightId + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", objectKey='" + objectKey + '\''
                + ", objectSize=" + objectSize
                + ", fileType=" + fileType
                + '}';
    }
}
