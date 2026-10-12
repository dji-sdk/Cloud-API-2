// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 文件下载链接列表结果 */
public final class FileDownloadUrlListResult {
    private List<String> list;
    public FileDownloadUrlListResult() {
    }

/**
     * 构造 FileDownloadUrlListResult。
     *
     * @param list 下载链接列表
     */
    public FileDownloadUrlListResult(List<String> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 下载链接列表 */
    public List<String> getList() { return list; }

    /** 下载链接列表 */
    public void setList(List<String> list) {
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
        FileDownloadUrlListResult that = (FileDownloadUrlListResult) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "FileDownloadUrlListResult{list=" + list + '}';
    }
}
