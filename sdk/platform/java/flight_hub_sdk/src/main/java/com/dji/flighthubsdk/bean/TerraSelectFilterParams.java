// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 全选反选场景的筛选条件（加载/取消加载模型时使用）
 * 对应 proto SelectFilter.selected_parent_id (uint32 → int)
 */
public final class TerraSelectFilterParams {

    /** 选中范围的父目录 ID (uint32)；仅当 setSelectedParentId() 被调用后才会传递给服务端 */
    private int selectedParentId;
    /** 是否传 selectedParentId */
    private boolean selectedParentIdSet;
    /** 排除的文件 ID 列表 */
    private List<Integer> exceptIds = new ArrayList<>();
    /** 关键字模糊搜索 */
    private String key = "";
    /** 是否包含子文件 */
    private boolean getSubFile;
    /** 开始时间 */
    private long startTime;
    /** 结束时间 */
    private long endTime;
    /** 文件类型过滤 */
    private List<String> fileAllTypes = new ArrayList<>();
    /** 负载型号过滤 */
    private List<String> payloadModelKey = new ArrayList<>();
    /** 父目录 ID */
    private int parentId;
    /** 文件标签过滤 */
    private List<String> fileTags = new ArrayList<>();
    /** 分享状态（可选） */
    private Integer shareState;
    /** 是否已上图（可选） */
    private Boolean showOnMap;

    public TerraSelectFilterParams() {
    }

    public int getSelectedParentId() { return selectedParentId; }
    public boolean isSelectedParentIdSet() { return selectedParentIdSet; }
    /** 调用此方法后 selectedParentId 才会被序列化到请求中 */
    public void setSelectedParentId(int selectedParentId) {
        this.selectedParentId = selectedParentId;
        this.selectedParentIdSet = true;
    }

    public List<Integer> getExceptIds() { return exceptIds; }
    public void setExceptIds(List<Integer> exceptIds) { this.exceptIds = exceptIds != null ? exceptIds : new ArrayList<>(); }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key != null ? key : ""; }

    public boolean isGetSubFile() { return getSubFile; }
    public void setGetSubFile(boolean getSubFile) { this.getSubFile = getSubFile; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public List<String> getFileAllTypes() { return fileAllTypes; }
    public void setFileAllTypes(List<String> fileAllTypes) { this.fileAllTypes = fileAllTypes != null ? fileAllTypes : new ArrayList<>(); }

    public List<String> getPayloadModelKey() { return payloadModelKey; }
    public void setPayloadModelKey(List<String> payloadModelKey) { this.payloadModelKey = payloadModelKey != null ? payloadModelKey : new ArrayList<>(); }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public List<String> getFileTags() { return fileTags; }
    public void setFileTags(List<String> fileTags) { this.fileTags = fileTags != null ? fileTags : new ArrayList<>(); }

    public Integer getShareState() { return shareState; }
    public void setShareState(Integer shareState) { this.shareState = shareState; }

    public Boolean getShowOnMap() { return showOnMap; }
    public void setShowOnMap(Boolean showOnMap) { this.showOnMap = showOnMap; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraSelectFilterParams that = (TerraSelectFilterParams) o;
        return selectedParentId == that.selectedParentId && selectedParentIdSet == that.selectedParentIdSet
                && getSubFile == that.getSubFile
                && startTime == that.startTime && endTime == that.endTime
                && parentId == that.parentId
                && Objects.equals(exceptIds, that.exceptIds)
                && Objects.equals(key, that.key)
                && Objects.equals(fileAllTypes, that.fileAllTypes)
                && Objects.equals(payloadModelKey, that.payloadModelKey)
                && Objects.equals(fileTags, that.fileTags)
                && Objects.equals(shareState, that.shareState)
                && Objects.equals(showOnMap, that.showOnMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectedParentId, selectedParentIdSet, exceptIds, key,
                getSubFile, startTime, endTime, fileAllTypes, payloadModelKey,
                parentId, fileTags, shareState, showOnMap);
    }

    @Override
    public String toString() {
        return "TerraSelectFilterParams{"
                + "selectedParentId=" + (selectedParentIdSet ? selectedParentId : "unset")
                + ", exceptIds=" + exceptIds
                + ", key='" + key + '\''
                + ", getSubFile=" + getSubFile
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", fileAllTypes=" + fileAllTypes
                + ", payloadModelKey=" + payloadModelKey
                + ", parentId=" + parentId
                + ", fileTags=" + fileTags
                + ", shareState=" + shareState
                + ", showOnMap=" + showOnMap
                + '}';
    }
}
