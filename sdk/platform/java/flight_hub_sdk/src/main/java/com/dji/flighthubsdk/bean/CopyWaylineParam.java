// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;

/**
 * 航线复制请求参数（去组织化，不含 workspace_id）。
 * <p>
 * 与 {@link DuplicateWaylineParam} 的区别：
 * <ul>
 *   <li>copyWayline 为去组织化接口，不需要 workspace_id</li>
 *   <li>copyWayline 不需要 targetObjectKeyPrefix 参数</li>
 *   <li>两者后端路径不同（/copy vs /duplicate）</li>
 * </ul>
 * </p>
 */
public class CopyWaylineParam {
    /** 原始航线 UUID（必填） */
    private String originalWaylineUuid = "";
    /** 目标航线名称（必填） */
    private String targetWaylineName = "";

    public CopyWaylineParam() {
    }

    /** 原始航线 UUID（必填） */
    public String getOriginalWaylineUuid() {
        return originalWaylineUuid;
    }

    /** @param originalWaylineUuid 原始航线 UUID（必填） */
    public void setOriginalWaylineUuid(String originalWaylineUuid) {
        this.originalWaylineUuid = originalWaylineUuid != null ? originalWaylineUuid : "";
    }

    /** 目标航线名称（必填） */
    public String getTargetWaylineName() {
        return targetWaylineName;
    }

    /** @param targetWaylineName 目标航线名称（必填） */
    public void setTargetWaylineName(String targetWaylineName) {
        this.targetWaylineName = targetWaylineName != null ? targetWaylineName : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopyWaylineParam that = (CopyWaylineParam) o;
        return Objects.equals(originalWaylineUuid, that.originalWaylineUuid)
                && Objects.equals(targetWaylineName, that.targetWaylineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalWaylineUuid, targetWaylineName);
    }

    @Override
    public String toString() {
        return "CopyWaylineParam{originalWaylineUuid='" + originalWaylineUuid + '\'' + ", targetWaylineName='" + targetWaylineName + '\'' + '}';
    }
}
