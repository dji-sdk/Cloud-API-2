// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 模型显示地图 WS 推送消息
 * topic: topic_media_file_show_on_map（来自 file-storage BatchShowOnMap）
 */
public final class TerraMediaFileShowOnMapMsg {

    /** 上图文件列表 */
    private List<TerraMediaFileShowOnMapItem> list;

    public TerraMediaFileShowOnMapMsg() { this.list = Collections.emptyList(); }

    public TerraMediaFileShowOnMapMsg(List<TerraMediaFileShowOnMapItem> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    public List<TerraMediaFileShowOnMapItem> getList() { return list; }
    public void setList(List<TerraMediaFileShowOnMapItem> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(list, ((TerraMediaFileShowOnMapMsg) o).list);
    }
    @Override public int hashCode() { return Objects.hash(list); }
    @Override public String toString() { return "TerraMediaFileShowOnMapMsg{list=" + list + '}'; }
}
