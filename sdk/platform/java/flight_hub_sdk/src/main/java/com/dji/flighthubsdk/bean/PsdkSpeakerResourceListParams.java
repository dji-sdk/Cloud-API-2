// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 获取 PSDK 喊话器资源列表的过滤参数 */
public final class PsdkSpeakerResourceListParams {
    /** 文件类型："audio" 或 "tts"，空=不过滤 */
    private String fileType = "";
    /** 文件 MD5，空=不过滤 */
    private String md5Sum = "";

    public PsdkSpeakerResourceListParams() {
    }

    /** @param fileType 文件类型："audio" 或 "tts" */
    public String getFileType() { return fileType; }

    /** @param fileType 文件类型："audio" 或 "tts" */
    public void setFileType(String fileType) { this.fileType = fileType != null ? fileType : ""; }

    /** 文件 MD5 */
    public String getMd5Sum() { return md5Sum; }

    /** 文件 MD5 */
    public void setMd5Sum(String md5Sum) { this.md5Sum = md5Sum != null ? md5Sum : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsdkSpeakerResourceListParams that = (PsdkSpeakerResourceListParams) o;
        return Objects.equals(fileType, that.fileType) && Objects.equals(md5Sum, that.md5Sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, md5Sum);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerResourceListParams{fileType='" + fileType + "', md5Sum='" + md5Sum + "'}";
    }
}
