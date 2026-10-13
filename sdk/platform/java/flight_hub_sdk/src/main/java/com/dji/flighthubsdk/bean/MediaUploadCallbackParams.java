// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传回调通知的请求参数。
 */
public final class MediaUploadCallbackParams {
    private String sn;
    private int result;
    private int progress;
    private MediaUploadCallbackFile file;
    private int expectedFileCount;
    private int uploadedFileCount;
    private int flightType;
    private String bizCode;
    private String version;
    public MediaUploadCallbackParams() {
    }

/**
     * 构造 MediaUploadCallbackParams。
     *
     * @param sn 设备序列号
     * @param result 上传结果码
     * @param progress 上传进度（0-100）
     * @param file 文件信息
     * @param expectedFileCount 预期上传文件总数
     * @param uploadedFileCount 已上传文件数
     * @param flightType 飞行类型
     * @param bizCode 业务码
     * @param version 版本
     */
    public MediaUploadCallbackParams(
            String sn,
            int result,
            int progress,
            MediaUploadCallbackFile file,
            int expectedFileCount,
            int uploadedFileCount,
            int flightType,
            String bizCode,
            String version) {
        this.sn = sn != null ? sn : "";
        this.result = result;
        this.progress = progress;
        this.file = file;
        this.expectedFileCount = expectedFileCount;
        this.uploadedFileCount = uploadedFileCount;
        this.flightType = flightType;
        this.bizCode = bizCode != null ? bizCode : "";
        this.version = version != null ? version : "";
    }

    /** 设备序列号 */
    public String getSn() { return sn; }

    /** 上传结果码 */
    public int getResult() { return result; }

    /** 上传进度（0-100） */
    public int getProgress() { return progress; }

    /** 文件信息 */
    public MediaUploadCallbackFile getFile() { return file; }

    /** 预期上传文件总数 */
    public int getExpectedFileCount() { return expectedFileCount; }

    /** 已上传文件数 */
    public int getUploadedFileCount() { return uploadedFileCount; }

    /** 飞行类型 */
    public int getFlightType() { return flightType; }

    /** 业务码 */
    public String getBizCode() { return bizCode; }

    /** 版本 */
    public String getVersion() { return version; }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 上传结果码 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 上传进度（0-100） */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /** 文件信息 */
    public void setFile(MediaUploadCallbackFile file) {
        this.file = file;
    }

    /** 预期上传文件总数 */
    public void setExpectedFileCount(int expectedFileCount) {
        this.expectedFileCount = expectedFileCount;
    }

    /** 已上传文件数 */
    public void setUploadedFileCount(int uploadedFileCount) {
        this.uploadedFileCount = uploadedFileCount;
    }

    /** 飞行类型 */
    public void setFlightType(int flightType) {
        this.flightType = flightType;
    }

    /** 业务码 */
    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    /** 版本 */
    public void setVersion(String version) {
        this.version = version;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadCallbackParams that = (MediaUploadCallbackParams) o;
        return result == that.result
                && progress == that.progress
                && expectedFileCount == that.expectedFileCount
                && uploadedFileCount == that.uploadedFileCount
                && flightType == that.flightType
                && Objects.equals(sn, that.sn)
                && Objects.equals(file, that.file)
                && Objects.equals(bizCode, that.bizCode)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, result, progress, file, expectedFileCount, uploadedFileCount,
                flightType, bizCode, version);
    }

    @Override
    public String toString() {
        return "MediaUploadCallbackParams{"
                + "sn='" + sn + '\''
                + ", result=" + result
                + ", progress=" + progress
                + ", file=" + file
                + ", expectedFileCount=" + expectedFileCount
                + ", uploadedFileCount=" + uploadedFileCount
                + ", flightType=" + flightType
                + ", bizCode='" + bizCode + '\''
                + ", version='" + version + '\''
                + '}';
    }
}
