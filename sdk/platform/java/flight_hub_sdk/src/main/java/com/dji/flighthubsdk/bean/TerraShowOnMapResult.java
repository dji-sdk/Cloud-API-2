// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 批量加载/取消加载模型到地图响应结果
 */
public final class TerraShowOnMapResult {

    /** 实际被操作的文件 ID 列表 */
    private List<Integer> ids;

    public TerraShowOnMapResult() {
        this.ids = Collections.emptyList();
    }

    public TerraShowOnMapResult(List<Integer> ids) {
        this.ids = ids != null ? ids : Collections.emptyList();
    }

    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids != null ? ids : Collections.emptyList(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraShowOnMapResult that = (TerraShowOnMapResult) o;
        return Objects.equals(ids, that.ids);
    }

    @Override
    public int hashCode() { return Objects.hash(ids); }

    @Override
    public String toString() { return "TerraShowOnMapResult{ids=" + ids + '}'; }
}
