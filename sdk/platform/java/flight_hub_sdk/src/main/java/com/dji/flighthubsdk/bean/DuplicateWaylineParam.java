// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 复制航线请求参数 */
public class DuplicateWaylineParam {
    private String originalWaylineUuid = "";
    private String targetWaylineName = "";
    private String targetObjectKeyPrefix = "";

    public DuplicateWaylineParam() {
    }


    /** 原航线 UUID */
    public String getOriginalWaylineUuid() {
        return originalWaylineUuid;
    }

    /** @param originalWaylineUuid 原航线 UUID */
    public void setOriginalWaylineUuid(String originalWaylineUuid) {
        this.originalWaylineUuid = originalWaylineUuid != null ? originalWaylineUuid : "";
    }

    /** 目标航线名称 */
    public String getTargetWaylineName() {
        return targetWaylineName;
    }

    /** @param targetWaylineName 目标航线名称 */
    public void setTargetWaylineName(String targetWaylineName) {
        this.targetWaylineName = targetWaylineName != null ? targetWaylineName : "";
    }

    /** 目标对象存储 Key 前缀 */
    public String getTargetObjectKeyPrefix() {
        return targetObjectKeyPrefix;
    }

    /** @param targetObjectKeyPrefix 目标对象存储 Key 前缀 */
    public void setTargetObjectKeyPrefix(String targetObjectKeyPrefix) {
        this.targetObjectKeyPrefix = targetObjectKeyPrefix != null ? targetObjectKeyPrefix : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DuplicateWaylineParam that = (DuplicateWaylineParam) o;
        return Objects.equals(originalWaylineUuid, that.originalWaylineUuid)
                && Objects.equals(targetWaylineName, that.targetWaylineName)
                && Objects.equals(targetObjectKeyPrefix, that.targetObjectKeyPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalWaylineUuid, targetWaylineName, targetObjectKeyPrefix);
    }

    @Override
    public String toString() {
        return "DuplicateWaylineParam{originalWaylineUuid='" + originalWaylineUuid + '\'' + ", targetWaylineName='" + targetWaylineName + '\'' + ", targetObjectKeyPrefix='" + targetObjectKeyPrefix + '\'' + '}';
    }
}
