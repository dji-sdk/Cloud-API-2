// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞行记录导出进度条目数据。 */
public final class FlightRecordExportProgressItemData {
    /** 文件id; */
    private long id;
    /** 任务 uuid; */
    private String exportId;
    /** 创建时间; */
    private String createdAt;
    /** 导出类型, 比如 "details"; */
    private String contentType;
    /** 导出状态; */
    private int status;
    /** 导出进度; */
    private int progress;
    /** 导出失败原因码; */
    private int failedReasonCode;
    /** 文件存储对象 key; */
    private String objectKey;
    /** 文件名; */
    private String fileName;
    /** 用户 id; */
    private String userId;
    /** 项目 uuid; */
    private String prjUuid;
    /** 文件类型, 比如 ["Excel"]; */
    private List<String> fileType;
    /** 下载地址; */
    private String downloadUrl;
    public FlightRecordExportProgressItemData() {
    }

/**
     * 构造 FlightRecordExportProgressItemData。
     *
     * @param id ID
     * @param exportId 导出任务 ID
     * @param createdAt 创建时间
     * @param contentType 内容类型
     * @param status 状态
     * @param progress 进度
     * @param failedReasonCode 失败原因码
     * @param objectKey 对象存储 Key
     * @param fileName 文件名
     * @param userId 用户 ID
     * @param prjUuid 项目 UUID
     * @param fileType 文件类型列表
     * @param downloadUrl 下载链接
     */
    public FlightRecordExportProgressItemData(
            long id,
            String exportId,
            String createdAt,
            String contentType,
            int status,
            int progress,
            int failedReasonCode,
            String objectKey,
            String fileName,
            String userId,
            String prjUuid,
            List<String> fileType,
            String downloadUrl) {
        this.id = id;
        this.exportId = exportId != null ? exportId : "";
        this.createdAt = createdAt != null ? createdAt : "";
        this.contentType = contentType != null ? contentType : "";
        this.status = status;
        this.progress = progress;
        this.failedReasonCode = failedReasonCode;
        this.objectKey = objectKey != null ? objectKey : "";
        this.fileName = fileName != null ? fileName : "";
        this.userId = userId != null ? userId : "";
        this.prjUuid = prjUuid != null ? prjUuid : "";
        this.fileType = fileType != null ? fileType : Collections.emptyList();
        this.downloadUrl = downloadUrl != null ? downloadUrl : "";
    }

    /** 文件id; */
    public long getId() { return id; }
    /** 任务 uuid; */
    public String getExportId() { return exportId; }
    /** 创建时间; */
    public String getCreatedAt() { return createdAt; }
    /** 导出类型, 比如 "details"; */
    public String getContentType() { return contentType; }
    /** 导出状态; */
    public int getStatus() { return status; }
    /** 导出进度; */
    public int getProgress() { return progress; }
    /** 导出失败原因码; */
    public int getFailedReasonCode() { return failedReasonCode; }
    /** 文件存储对象 key; */
    public String getObjectKey() { return objectKey; }
    /** 文件名; */
    public String getFileName() { return fileName; }
    /** 用户 id; */
    public String getUserId() { return userId; }
    /** 项目 uuid; */
    public String getPrjUuid() { return prjUuid; }
    /** 文件类型, 比如 ["Excel"]; */
    public List<String> getFileType() { return fileType; }
    /** 下载地址; */
    public String getDownloadUrl() { return downloadUrl; }

    /** @param id 文件id; */
    public void setId(long id) {
        this.id = id;
    }

    /** @param exportId 任务 uuid; */
    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    /** @param createdAt 创建时间; */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** @param contentType 导出类型, 比如 "details"; */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /** @param status 导出状态; */
    public void setStatus(int status) {
        this.status = status;
    }

    /** @param progress 导出进度; */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /** @param failedReasonCode 导出失败原因码; */
    public void setFailedReasonCode(int failedReasonCode) {
        this.failedReasonCode = failedReasonCode;
    }

    /** @param objectKey 文件存储对象 key; */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** @param fileName 文件名; */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** @param userId 用户 id; */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @param prjUuid 项目 uuid; */
    public void setPrjUuid(String prjUuid) {
        this.prjUuid = prjUuid;
    }

    /** @param fileType 文件类型, 比如 ["Excel"]; */
    public void setFileType(List<String> fileType) {
        this.fileType = fileType;
    }

    /** @param downloadUrl 下载地址; */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightRecordExportProgressItemData that = (FlightRecordExportProgressItemData) o;
        return id == that.id
                && Objects.equals(exportId, that.exportId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(contentType, that.contentType)
                && status == that.status
                && progress == that.progress
                && failedReasonCode == that.failedReasonCode
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(userId, that.userId)
                && Objects.equals(prjUuid, that.prjUuid)
                && Objects.equals(fileType, that.fileType)
                && Objects.equals(downloadUrl, that.downloadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                exportId,
                createdAt,
                contentType,
                status,
                progress,
                failedReasonCode,
                objectKey,
                fileName,
                userId,
                prjUuid,
                fileType,
                downloadUrl);
    }

    @Override
    public String toString() {
        return "FlightRecordExportProgressItemData{"
                + "id=" + id
                + ", exportId='" + exportId + '\''
                + ", createdAt='" + createdAt + '\''
                + ", contentType='" + contentType + '\''
                + ", status=" + status
                + ", progress=" + progress
                + ", failedReasonCode=" + failedReasonCode
                + ", objectKey='" + objectKey + '\''
                + ", fileName='" + fileName + '\''
                + ", userId='" + userId + '\''
                + ", prjUuid='" + prjUuid + '\''
                + ", fileType=" + fileType
                + ", downloadUrl='" + downloadUrl + '\''
                + '}';
    }
}
