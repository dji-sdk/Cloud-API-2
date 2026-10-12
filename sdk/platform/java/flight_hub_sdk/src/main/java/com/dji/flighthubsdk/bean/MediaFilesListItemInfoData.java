// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 媒体文件列表条目信息数据。 */
public final class MediaFilesListItemInfoData {
    /** 文件id; */
    private long fileId;
    /** 飞行记录id; */
    private String flightId;
    /** 航点动作id; */
    private String waylineActionUuid;
    /** 文件大小(字节); */
    private long size;
    /** 文件存储对象key; */
    private String objectKey;
    /** 文件名; */
    private String fileName;
    /** 文件后缀; */
    private String suffix;
    /** 扩展字段; 扩展数据(图片才会返回); */
    private MediaFilesListItemExtData ext;
    /** 创建时间; */
    private String createdAt;
    /** 文件路径; */
    private String filePath;
    /** 设备 SN */
    private String deviceSn;
    /** 负载类型 */
    private String payloadType;
    /** 文件类型; 取值参见 MediaType 枚举; 媒体列表接口实际只会出现 0(未定义)/1(文件夹)/2(图片)/3(视频) */
    private int type;
    public MediaFilesListItemInfoData() {
    }

/**
     * 构造 MediaFilesListItemInfoData。
     *
     * @param fileId 文件ID
     * @param flightId 飞行任务ID
     * @param waylineActionUuid 航线动作UUID
     * @param size 文件大小
     * @param objectKey 对象存储Key
     * @param fileName 文件名
     * @param suffix 文件后缀
     * @param ext 扩展数据
     * @param createdAt 创建时间
     * @param filePath 文件路径
     * @param deviceSn 设备 SN
     * @param payloadType 负载类型
     */
    public MediaFilesListItemInfoData(
            long fileId,
            String flightId,
            String waylineActionUuid,
            long size,
            String objectKey,
            String fileName,
            String suffix,
            MediaFilesListItemExtData ext,
            String createdAt,
            String filePath,
            String deviceSn,
            String payloadType,
            int type) {
        this.fileId = fileId;
        this.flightId = flightId != null ? flightId : "";
        this.waylineActionUuid = waylineActionUuid != null ? waylineActionUuid : "";
        this.size = size;
        this.objectKey = objectKey != null ? objectKey : "";
        this.fileName = fileName != null ? fileName : "";
        this.suffix = suffix != null ? suffix : "";
        this.ext = ext;
        this.createdAt = createdAt != null ? createdAt : "";
        this.filePath = filePath != null ? filePath : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.payloadType = payloadType != null ? payloadType : "";
        this.type = type;
    }

    /** 文件id; */
    public long getFileId() {
        return fileId;
    }

    /** @param fileId 文件id; */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /** 飞行记录id; */
    public String getFlightId() {
        return flightId;
    }

    /** @param flightId 飞行记录id; */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 航点动作id; */
    public String getWaylineActionUuid() {
        return waylineActionUuid;
    }

    /** @param waylineActionUuid 航点动作id; */
    public void setWaylineActionUuid(String waylineActionUuid) {
        this.waylineActionUuid = waylineActionUuid;
    }

    /** 文件大小(字节); */
    public long getSize() {
        return size;
    }

    /** @param size 文件大小(字节); */
    public void setSize(long size) {
        this.size = size;
    }

    /** 文件存储对象key; */
    public String getObjectKey() {
        return objectKey;
    }

    /** @param objectKey 文件存储对象key; */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 文件名; */
    public String getFileName() {
        return fileName;
    }

    /** @param fileName 文件名; */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** 文件后缀; */
    public String getSuffix() {
        return suffix;
    }

    /** @param suffix 文件后缀; */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /** 扩展字段; 扩展数据(图片才会返回); */
    public MediaFilesListItemExtData getExt() {
        return ext;
    }

    /** @param ext 扩展字段; 扩展数据(图片才会返回); */
    public void setExt(MediaFilesListItemExtData ext) {
        this.ext = ext;
    }

    /** 创建时间; */
    public String getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt 创建时间; */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** 文件路径; */
    public String getFilePath() {
        return filePath;
    }

    /** @param filePath 文件路径; */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /** 设备 SN */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备 SN */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 负载类型 */
    public String getPayloadType() {
        return payloadType;
    }

    /** 负载类型 */
    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    /** 文件类型; 取值参见 MediaType 枚举; 媒体列表接口实际只会出现 0(未定义)/1(文件夹)/2(图片)/3(视频) */
    public int getType() {
        return type;
    }

    /** 文件类型; 取值参见 MediaType 枚举; 媒体列表接口实际只会出现 0(未定义)/1(文件夹)/2(图片)/3(视频) */
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
        MediaFilesListItemInfoData that = (MediaFilesListItemInfoData) o;
        return fileId == that.fileId
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(waylineActionUuid, that.waylineActionUuid)
                && size == that.size
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(suffix, that.suffix)
                && Objects.equals(ext, that.ext)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(filePath, that.filePath)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(payloadType, that.payloadType)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fileId,
                flightId,
                waylineActionUuid,
                size,
                objectKey,
                fileName,
                suffix,
                ext,
                createdAt,
                filePath,
                deviceSn,
                payloadType,
                type);
    }

    @Override
    public String toString() {
        return "MediaFilesListItemInfoData{"
                + "fileId=" + fileId
                + ", flightId='" + flightId + '\''
                + ", waylineActionUuid='" + waylineActionUuid + '\''
                + ", size=" + size
                + ", objectKey='" + objectKey + '\''
                + ", fileName='" + fileName + '\''
                + ", suffix='" + suffix + '\''
                + ", ext=" + ext
                + ", createdAt='" + createdAt + '\''
                + ", filePath='" + filePath + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", payloadType='" + payloadType + '\''
                + ", type='" + type + '\''
                + '}';
    }
}
