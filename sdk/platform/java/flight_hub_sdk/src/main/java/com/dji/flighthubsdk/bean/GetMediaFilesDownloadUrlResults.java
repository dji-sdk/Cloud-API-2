// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 获取媒体文件下载URL结果。 */
public final class GetMediaFilesDownloadUrlResults {
    /** 下载地址; */
    private String url;
    public GetMediaFilesDownloadUrlResults() {
    }

/**
     * 构造 GetMediaFilesDownloadUrlResults。
     *
     * @param url 下载 URL
     */
    public GetMediaFilesDownloadUrlResults(String url) {
        this.url = url != null ? url : "";
    }

    /** 下载地址; */
    public String getUrl() {
        return url;
    }

    /** @param url 下载地址; */
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
        GetMediaFilesDownloadUrlResults that = (GetMediaFilesDownloadUrlResults) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "GetMediaFilesDownloadUrlResults{url='" + url + '\'' + '}';
    }
}
