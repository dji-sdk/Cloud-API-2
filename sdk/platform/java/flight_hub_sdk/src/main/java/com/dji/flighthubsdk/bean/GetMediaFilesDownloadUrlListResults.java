// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取媒体文件下载URL列表结果。 */
public final class GetMediaFilesDownloadUrlListResults {
    private List<MediaFileDownloadInfo> list;
    public GetMediaFilesDownloadUrlListResults() {
    }

/**
     * 构造 GetMediaFilesDownloadUrlListResults。
     *
     * @param list 下载信息列表
     */
    public GetMediaFilesDownloadUrlListResults(List<MediaFileDownloadInfo> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 下载信息列表 */
    public List<MediaFileDownloadInfo> getList() {
        return list;
    }

    /** 下载信息列表 */
    public void setList(List<MediaFileDownloadInfo> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetMediaFilesDownloadUrlListResults that = (GetMediaFilesDownloadUrlListResults) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "GetMediaFilesDownloadUrlListResults{list=" + list + '}';
    }
}
