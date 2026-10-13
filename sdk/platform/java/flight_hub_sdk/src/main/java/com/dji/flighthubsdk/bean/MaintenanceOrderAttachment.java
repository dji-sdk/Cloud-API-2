// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 报修订单详情 - 附件信息 */
public final class MaintenanceOrderAttachment {
    private long id;
    private String fileName;
    private String objectKey;
    private long fileSize;
    private String uploadTime;
    private String createdAt;
    public MaintenanceOrderAttachment() {
    }

/**
     * 构造 MaintenanceOrderAttachment。
     *
     * @param id 附件ID
     * @param fileName 文件名
     * @param objectKey OSS 存储路径
     * @param fileSize 文件大小（字节）
     * @param uploadTime 上传时间
     * @param createdAt 创建时间
     */
    public MaintenanceOrderAttachment(long id, String fileName, String objectKey, long fileSize, String uploadTime, String createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.objectKey = objectKey;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
        this.createdAt = createdAt;
    }

    /** 附件ID */
    public long getId() { return id; }
    /** 文件名 */
    public String getFileName() { return fileName; }
    /** OSS 存储路径 */
    public String getObjectKey() { return objectKey; }
    /** 文件大小（字节） */
    public long getFileSize() { return fileSize; }
    /** 上传时间 */
    public String getUploadTime() { return uploadTime; }
    /** 创建时间 */
    public String getCreatedAt() { return createdAt; }

    /** 附件ID */
    public void setId(long id) {
        this.id = id;
    }

    /** 文件名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** OSS 存储路径 */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 文件大小（字节） */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /** 上传时间 */
    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    /** 创建时间 */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderAttachment that = (MaintenanceOrderAttachment) o;
        return id == that.id
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(objectKey, that.objectKey)
                && fileSize == that.fileSize
                && Objects.equals(uploadTime, that.uploadTime)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                fileName,
                objectKey,
                fileSize,
                uploadTime,
                createdAt);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderAttachment{"
                + "id=" + id
                + ", fileName='" + fileName + '\''
                + ", objectKey='" + objectKey + '\''
                + ", fileSize=" + fileSize
                + ", uploadTime='" + uploadTime + '\''
                + ", createdAt='" + createdAt + '\''
                + '}';
    }
}
