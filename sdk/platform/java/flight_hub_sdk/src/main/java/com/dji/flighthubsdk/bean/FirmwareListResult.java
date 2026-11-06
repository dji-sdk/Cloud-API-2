// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 固件列表结果（含分页信息）。 */
public final class FirmwareListResult {

    /** 固件列表 */
    private List<FirmwareItemResult> list;
    /** 分页信息（当前页/每页数量/总数） */
    private CommonPaginationData pagination;

    public FirmwareListResult() {
    }

    /**
     * 构造 FirmwareListResult。
     *
     * @param list 固件列表
     * @param pagination 分页信息（当前页/每页数量/总数）
     */
    public FirmwareListResult(List<FirmwareItemResult> list, CommonPaginationData pagination) {
        this.list = list != null ? list : Collections.emptyList();
        this.pagination = pagination;
    }

    /** 固件列表 */
    public List<FirmwareItemResult> getList() {
        return list;
    }

    /** 固件列表 */
    public void setList(List<FirmwareItemResult> list) {
        this.list = list;
    }

    /** 分页信息（当前页/每页数量/总数） */
    public CommonPaginationData getPagination() {
        return pagination;
    }

    /** 分页信息（当前页/每页数量/总数） */
    public void setPagination(CommonPaginationData pagination) {
        this.pagination = pagination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareListResult that = (FirmwareListResult) o;
        return Objects.equals(list, that.list)
                && Objects.equals(pagination, that.pagination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, pagination);
    }

    @Override
    public String toString() {
        return "FirmwareListResult{list=" + list
                + ", pagination=" + pagination + '}';
    }
}
