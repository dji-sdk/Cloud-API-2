// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** Terra Build 获取重建任务图片列表的返回结果 */
public final class TerraBuildReconstructionImagesResult {
    private List<TerraBuildReconstructionImageItem> list;
    private int total;
    public TerraBuildReconstructionImagesResult() {
    }

/**
     * 构造 TerraBuildReconstructionImagesResult。
     *
     * @param list HMS 数据项列表
     * @param total 总容量(KB)
     */
    public TerraBuildReconstructionImagesResult(List<TerraBuildReconstructionImageItem> list, int total) {
        this.list = list != null ? list : Collections.emptyList();
        this.total = total;
    }

    /** HMS 数据项列表 */
    public List<TerraBuildReconstructionImageItem> getList() { return list; }
    /** 总容量(KB) */
    public int getTotal() { return total; }

    /** HMS 数据项列表 */
    public void setList(List<TerraBuildReconstructionImageItem> list) {
        this.list = list;
    }

    /** 总容量(KB) */
    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildReconstructionImagesResult that = (TerraBuildReconstructionImagesResult) o;
        return Objects.equals(list, that.list)
                && total == that.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, total);
    }

    @Override
    public String toString() {
        return "TerraBuildReconstructionImagesResult{list=" + list + ", total=" + total + '}';
    }
}
