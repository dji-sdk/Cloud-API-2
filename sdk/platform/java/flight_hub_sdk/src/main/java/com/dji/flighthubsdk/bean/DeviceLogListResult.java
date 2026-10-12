// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 设备日志列表结果。 */
public final class DeviceLogListResult {
    /** files */
    private List<DeviceLogListResultFileItem> files;
    public DeviceLogListResult() {
    }

/**
     * 构造 DeviceLogListResult。
     *
     * @param files files
     */
    public DeviceLogListResult(List<DeviceLogListResultFileItem> files) {
        this.files = files != null ? files : Collections.emptyList();
    }

    /** files */
    public List<DeviceLogListResultFileItem> getFiles() {
        return files;
    }

    /** files */
    public void setFiles(List<DeviceLogListResultFileItem> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogListResult that = (DeviceLogListResult) o;
        return Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        return "DeviceLogListResult{files=" + files + '}';
    }
}
