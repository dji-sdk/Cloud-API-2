// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 模型隐藏地图 WS 推送消息
 * topic: topic_media_file_hide_on_map
 * 由 Delete/BatchDelete 触发
 */
public final class TerraMediaFileHideOnMapMsg {

    /** 被隐藏的模型文件 ID 列表（uint32） */
    private List<Integer> ids;

    public TerraMediaFileHideOnMapMsg() { this.ids = Collections.emptyList(); }

    public TerraMediaFileHideOnMapMsg(List<Integer> ids) {
        this.ids = ids != null ? ids : Collections.emptyList();
    }

    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids != null ? ids : Collections.emptyList(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(ids, ((TerraMediaFileHideOnMapMsg) o).ids);
    }

    @Override public int hashCode() { return Objects.hash(ids); }
    @Override public String toString() { return "TerraMediaFileHideOnMapMsg{ids=" + ids + '}'; }
}
