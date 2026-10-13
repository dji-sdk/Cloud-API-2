// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建报修订单 - 附件参数 */
public class MaintenanceOrderAttachmentsParams {
    /** 文件名; */
    private String fileName = "";
    /** OSS 存储路径(object key); */
    private String fileKey = "";
    /** 文件大小(字节); */
    private long fileSize;

    public MaintenanceOrderAttachmentsParams() {
    }


    /** 文件名; */
    public String getFileName() {
        return fileName;
    }

    /** @param fileName 文件名; */
    public void setFileName(String fileName) {
        this.fileName = fileName != null ? fileName : "";
    }

    /** OSS 存储路径(object key); */
    public String getFileKey() {
        return fileKey;
    }

    /** @param fileKey OSS 存储路径(object key); */
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey != null ? fileKey : "";
    }

    /** 文件大小(字节); */
    public long getFileSize() {
        return fileSize;
    }

    /** @param fileSize 文件大小(字节); */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderAttachmentsParams that = (MaintenanceOrderAttachmentsParams) o;
        return Objects.equals(fileName, that.fileName)
                && Objects.equals(fileKey, that.fileKey)
                && fileSize == that.fileSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileKey, fileSize);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderAttachmentsParams{fileName='" + fileName + '\'' + ", fileKey='" + fileKey + '\'' + ", fileSize=" + fileSize + '}';
    }
}
