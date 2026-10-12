// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 批量下载航线请求参数 */
public class BatchDownloadWaylinesParam {
    private List<String> waylineUuids = new ArrayList<>();

    public BatchDownloadWaylinesParam() {
    }


    /** 航线 UUID 列表 */
    public List<String> getWaylineUuids() {
        return waylineUuids;
    }

    /** @param waylineUuids 航线 UUID 列表 */
    public void setWaylineUuids(List<String> waylineUuids) {
        this.waylineUuids = waylineUuids != null ? waylineUuids : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BatchDownloadWaylinesParam that = (BatchDownloadWaylinesParam) o;
        return Objects.equals(waylineUuids, that.waylineUuids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waylineUuids);
    }

    @Override
    public String toString() {
        return "BatchDownloadWaylinesParam{waylineUuids=" + waylineUuids + '}';
    }
}
