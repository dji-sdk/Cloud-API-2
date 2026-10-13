// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 上传地理标注媒体资源结果 */
public final class UploadGeoMediaFileResult {

    /** 文件 ID（files 表主键），可用于轮询 URL */
    private int fileId;
    /** 文件 UUID */
    private String fileUuid;
    /** 媒体文件 ID（media_files 表主键），可用于批量删除 */
    private int mediaFileId;

    public UploadGeoMediaFileResult() {
    }

    /**
     * 构造 UploadGeoMediaFileResult。
     *
     * @param fileId      文件 ID（files 表主键），可用于轮询 URL
     * @param fileUuid    文件 UUID
     * @param mediaFileId 媒体文件 ID（media_files 表主键），可用于批量删除
     */
    public UploadGeoMediaFileResult(int fileId, String fileUuid, int mediaFileId) {
        this.fileId = fileId;
        this.fileUuid = fileUuid != null ? fileUuid : "";
        this.mediaFileId = mediaFileId;
    }

    /** 文件 ID（files 表主键），可用于轮询 URL */
    public int getFileId() { return fileId; }

    /** 文件 ID（files 表主键），可用于轮询 URL */
    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    /** 文件 UUID */
    public String getFileUuid() { return fileUuid; }

    /** 文件 UUID */
    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    /** 媒体文件 ID（media_files 表主键），可用于批量删除 */
    public int getMediaFileId() { return mediaFileId; }

    /** 媒体文件 ID（media_files 表主键），可用于批量删除 */
    public void setMediaFileId(int mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadGeoMediaFileResult that = (UploadGeoMediaFileResult) o;
        return fileId == that.fileId
                && mediaFileId == that.mediaFileId
                && Objects.equals(fileUuid, that.fileUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, fileUuid, mediaFileId);
    }

    @Override
    public String toString() {
        return "UploadGeoMediaFileResult{"
                + "fileId=" + fileId
                + ", fileUuid='" + fileUuid + '\''
                + ", mediaFileId=" + mediaFileId
                + '}';
    }
}
