// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 媒体文件下载信息。 */
public final class MediaFileDownloadInfo {
    private String objectKey;
    private String url;
    public MediaFileDownloadInfo() {
    }

/**
     * 构造 MediaFileDownloadInfo。
     *
     * @param objectKey 存储对象Key
     * @param url 下载 URL
     */
    public MediaFileDownloadInfo(String objectKey, String url) {
        this.objectKey = objectKey != null ? objectKey : "";
        this.url = url != null ? url : "";
    }

    /** 存储对象Key */
    public String getObjectKey() {
        return objectKey;
    }

    /** 存储对象Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 下载 URL */
    public String getUrl() {
        return url;
    }

    /** 下载 URL */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFileDownloadInfo that = (MediaFileDownloadInfo) o;
        return Objects.equals(objectKey, that.objectKey)
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, url);
    }

    @Override
    public String toString() {
        return "MediaFileDownloadInfo{objectKey='" + objectKey + '\'' + ", url='" + url + '\'' + '}';
    }
}
