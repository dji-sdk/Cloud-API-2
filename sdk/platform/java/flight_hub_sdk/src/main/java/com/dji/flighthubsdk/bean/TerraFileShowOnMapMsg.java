// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 文件展示地图状态 WS 推送消息
 * topic: topic_model_file_show_on_map
 */
public final class TerraFileShowOnMapMsg {

    /** 展示到地图的文件列表 */
    private List<TerraFileShowOnMapItem> list;

    public TerraFileShowOnMapMsg() {
        this.list = Collections.emptyList();
    }

    public TerraFileShowOnMapMsg(List<TerraFileShowOnMapItem> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    public List<TerraFileShowOnMapItem> getList() { return list; }
    public void setList(List<TerraFileShowOnMapItem> list) { this.list = list != null ? list : Collections.emptyList(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraFileShowOnMapMsg that = (TerraFileShowOnMapMsg) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() { return Objects.hash(list); }

    @Override
    public String toString() { return "TerraFileShowOnMapMsg{list=" + list + '}'; }
}
