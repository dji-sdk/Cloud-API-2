// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 日志报告列表结果附件文件条目。 */
public final class LogReportListResultAppendixFileItem {
    /** 文件名 */
    private String fileName;
    /** 文件对象存储 Key */
    private String fileObjectKey;
    /** 文件大小，单位：字节 */
    private long fileSize;
    public LogReportListResultAppendixFileItem() {
    }

/**
     * 构造 LogReportListResultAppendixFileItem。
     *
     * @param fileName 文件名
     * @param fileObjectKey 文件对象存储 Key
     * @param fileSize 文件大小(字节)
     */
    public LogReportListResultAppendixFileItem(String fileName, String fileObjectKey, long fileSize) {
        this.fileName = fileName != null ? fileName : "";
        this.fileObjectKey = fileObjectKey != null ? fileObjectKey : "";
        this.fileSize = fileSize;
    }

    /** 文件名 */
    public String getFileName() {
        return fileName;
    }

    /** 文件名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** 文件对象存储 Key */
    public String getFileObjectKey() {
        return fileObjectKey;
    }

    /** 文件对象存储 Key */
    public void setFileObjectKey(String fileObjectKey) {
        this.fileObjectKey = fileObjectKey;
    }

    /** 文件大小，单位：字节 */
    public long getFileSize() {
        return fileSize;
    }

    /** @param fileSize 文件大小，单位：字节 */
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
        LogReportListResultAppendixFileItem that = (LogReportListResultAppendixFileItem) o;
        return Objects.equals(fileName, that.fileName)
                && Objects.equals(fileObjectKey, that.fileObjectKey)
                && fileSize == that.fileSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileObjectKey, fileSize);
    }

    @Override
    public String toString() {
        return "LogReportListResultAppendixFileItem{fileName='" + fileName + '\'' + ", fileObjectKey='" + fileObjectKey + '\'' + ", fileSize=" + fileSize + '}';
    }
}
