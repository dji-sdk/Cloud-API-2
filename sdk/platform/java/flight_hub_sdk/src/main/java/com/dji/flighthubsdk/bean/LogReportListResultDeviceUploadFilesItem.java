// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 日志报告设备上传文件项。 */
public final class LogReportListResultDeviceUploadFilesItem {
    /** 模块编号 */
    private String module;
    /** 文件大小（字节） */
    private long size;
    /** 设备序列号 */
    private String deviceSn;
    /** 文件 Key */
    private String key;
    /** 文件指纹 */
    private String fingerprint;
    /** 上传进度 */
    private LogReportListResultDeviceUploadFilesItemProgress progress;
    /** 下载 URL */
    private String downloadUrl;
    public LogReportListResultDeviceUploadFilesItem() {
    }

    public LogReportListResultDeviceUploadFilesItem(String module, long size, String deviceSn,
            String key, String fingerprint, LogReportListResultDeviceUploadFilesItemProgress progress,
            String downloadUrl) {
        this.module = module != null ? module : "";
        this.size = size;
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.key = key != null ? key : "";
        this.fingerprint = fingerprint != null ? fingerprint : "";
        this.progress = progress;
        this.downloadUrl = downloadUrl != null ? downloadUrl : "";
    }

    /** 模块编号 */
    public String getModule() {
        return module;
    }

    /** @param module 模块编号 */
    public void setModule(String module) {
        this.module = module;
    }

    /** 文件大小（字节） */
    public long getSize() {
        return size;
    }

    /** @param size 文件大小（字节） */
    public void setSize(long size) {
        this.size = size;
    }

    /** 设备序列号 */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 文件 Key */
    public String getKey() {
        return key;
    }

    /** @param key 文件 Key */
    public void setKey(String key) {
        this.key = key;
    }

    /** 文件指纹 */
    public String getFingerprint() {
        return fingerprint;
    }

    /** @param fingerprint 文件指纹 */
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /** 上传进度 */
    public LogReportListResultDeviceUploadFilesItemProgress getProgress() {
        return progress;
    }

    /** @param progress 上传进度 */
    public void setProgress(LogReportListResultDeviceUploadFilesItemProgress progress) {
        this.progress = progress;
    }

    /** 下载 URL */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /** @param downloadUrl 下载 URL */
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
        LogReportListResultDeviceUploadFilesItem that = (LogReportListResultDeviceUploadFilesItem) o;
        return size == that.size
                && Objects.equals(module, that.module)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(key, that.key)
                && Objects.equals(fingerprint, that.fingerprint)
                && Objects.equals(progress, that.progress)
                && Objects.equals(downloadUrl, that.downloadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, size, deviceSn, key, fingerprint, progress, downloadUrl);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceUploadFilesItem{module='" + module + '\'' + ", size=" + size + ", deviceSn='" + deviceSn + '\'' + ", key='" + key + '\'' + ", fingerprint='" + fingerprint + '\'' + ", progress=" + progress + ", downloadUrl='" + downloadUrl + '\'' + '}';
    }
}
