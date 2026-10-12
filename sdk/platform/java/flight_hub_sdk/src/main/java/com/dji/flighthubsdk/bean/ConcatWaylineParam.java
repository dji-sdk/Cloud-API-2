// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 航线拼接请求参数（去组织化，不含 workspace_id） */
public class ConcatWaylineParam {
    /** 目标航线名称（必填） */
    private String targetWaylineName = "";
    /** KMZ 文件对象存储路径（必填） */
    private String kmzObjectKey = "";
    /** 子航线 UUID 列表，按拼接顺序（必填） */
    private List<String> subWaylineUuids = new ArrayList<>();
    private Map<String, String> editorWarnings = new HashMap<>();

    public ConcatWaylineParam() {
    }

    /** 目标航线名称（必填） */
    public String getTargetWaylineName() { return targetWaylineName; }

    /** @param targetWaylineName 目标航线名称（必填） */
    public void setTargetWaylineName(String targetWaylineName) {
        this.targetWaylineName = targetWaylineName != null ? targetWaylineName : "";
    }

    /** KMZ 文件对象存储路径（必填） */
    public String getKmzObjectKey() { return kmzObjectKey; }

    /** @param kmzObjectKey KMZ 文件对象存储路径（必填） */
    public void setKmzObjectKey(String kmzObjectKey) {
        this.kmzObjectKey = kmzObjectKey != null ? kmzObjectKey : "";
    }

    /** 子航线 UUID 列表，按拼接顺序（必填） */
    public List<String> getSubWaylineUuids() { return subWaylineUuids; }

    /** @param subWaylineUuids 子航线 UUID 列表，按拼接顺序（必填） */
    public void setSubWaylineUuids(List<String> subWaylineUuids) {
        this.subWaylineUuids = subWaylineUuids != null ? subWaylineUuids : new ArrayList<>();
    }

    /** 编辑器警告信息（可选，key-value 形式） */
    public Map<String, String> getEditorWarnings() { return editorWarnings; }

    /** @param editorWarnings 编辑器警告信息（可选，key-value 形式） */
    public void setEditorWarnings(Map<String, String> editorWarnings) {
        this.editorWarnings = editorWarnings != null ? editorWarnings : new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcatWaylineParam that = (ConcatWaylineParam) o;
        return Objects.equals(targetWaylineName, that.targetWaylineName)
                && Objects.equals(kmzObjectKey, that.kmzObjectKey)
                && Objects.equals(subWaylineUuids, that.subWaylineUuids)
                && Objects.equals(editorWarnings, that.editorWarnings);
    }

    @Override
    public int hashCode() { return Objects.hash(targetWaylineName, kmzObjectKey, subWaylineUuids, editorWarnings); }

    @Override
    public String toString() {
        return "ConcatWaylineParam{targetWaylineName='" + targetWaylineName + '\''
                + ", kmzObjectKey='" + kmzObjectKey + '\''
                + ", subWaylineUuids=" + subWaylineUuids
                + ", editorWarnings=" + editorWarnings + '}';
    }
}
