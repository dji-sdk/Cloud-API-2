// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场媒体文件数量。 */
public final class DockMediaFileDetail {

    /** int, "待上传数量"; */
    private int remainUpload;
    public DockMediaFileDetail() {
    }

/**
     * 构造 DockMediaFileDetail。
     *
     * @param remainUpload 剩余上传数
     */
    public DockMediaFileDetail(int remainUpload) {
        this.remainUpload = remainUpload;
    }

    /** int, "待上传数量"; */
    public int getRemainUpload() {
        return remainUpload;
    }

    /** @param remainUpload int, "待上传数量"; */
    public void setRemainUpload(int remainUpload) {
        this.remainUpload = remainUpload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockMediaFileDetail that = (DockMediaFileDetail) o;
        return remainUpload == that.remainUpload;
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainUpload);
    }

    @Override
    public String toString() {
        return "DockMediaFileDetail{remainUpload=" + remainUpload + '}';
    }
}
