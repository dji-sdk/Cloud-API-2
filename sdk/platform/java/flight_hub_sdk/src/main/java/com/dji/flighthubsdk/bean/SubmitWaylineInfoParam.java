// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.HashMap;
import java.util.Map;

import java.util.Objects;
/** 提交航线信息请求参数 */
public class SubmitWaylineInfoParam {
    private String kmzObjectKey = "";
    private String waylineName = "";
    private String asyncRequestId = "";
    private long timestamp = 0L;
    private Map<String, String> transparent = new HashMap<>();

    public SubmitWaylineInfoParam() {
    }


    /** KMZ对象存储Key */
    public String getKmzObjectKey() { return kmzObjectKey; }
    /** @param kmzObjectKey KMZ对象存储Key */
    public void setKmzObjectKey(String kmzObjectKey) { this.kmzObjectKey = kmzObjectKey != null ? kmzObjectKey : ""; }

    /** 航线名称 */
    public String getWaylineName() { return waylineName; }
    /** @param waylineName 航线名称 */
    public void setWaylineName(String waylineName) { this.waylineName = waylineName != null ? waylineName : ""; }

    /** 异步请求ID */
    public String getAsyncRequestId() { return asyncRequestId; }
    /** @param asyncRequestId 异步请求ID */
    public void setAsyncRequestId(String asyncRequestId) { this.asyncRequestId = asyncRequestId != null ? asyncRequestId : ""; }

    /** 时间戳 */
    public long getTimestamp() { return timestamp; }
    /** @param timestamp 时间戳 */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /** 透传数据 */
    public Map<String, String> getTransparent() { return transparent; }
    /** @param transparent 透传数据 */
    public void setTransparent(Map<String, String> transparent) { this.transparent = transparent != null ? transparent : new HashMap<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubmitWaylineInfoParam that = (SubmitWaylineInfoParam) o;
        return Objects.equals(kmzObjectKey, that.kmzObjectKey)
                && Objects.equals(waylineName, that.waylineName)
                && Objects.equals(asyncRequestId, that.asyncRequestId)
                && timestamp == that.timestamp
                && Objects.equals(transparent, that.transparent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                kmzObjectKey,
                waylineName,
                asyncRequestId,
                timestamp,
                transparent);
    }

    @Override
    public String toString() {
        return "SubmitWaylineInfoParam{"
                + "kmzObjectKey='" + kmzObjectKey + '\''
                + ", waylineName='" + waylineName + '\''
                + ", asyncRequestId='" + asyncRequestId + '\''
                + ", timestamp=" + timestamp
                + ", transparent=" + transparent
                + '}';
    }
}
