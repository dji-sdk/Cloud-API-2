// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/**
 * 航线导入事件结果。
 * <p>{@link #getKmzData()} 与 {@link #getExtData()} 永不为 {@code null}；无数据时为 {@link ImportWaylineEventKmzData#empty()} /
 * {@link ImportWaylineEventExtData#empty()}。</p>
 */
public final class ImportWaylineEventResult {

    private String asyncRequestId;
    private long timestamp;
    private String kmzObjectKey;
    private String waylineUuid;
    private int code;
    private String message;
    private ImportWaylineEventKmzData kmzData;
    private ImportWaylineEventExtData extData;
    public ImportWaylineEventResult() {
    }

/**
     * 构造 ImportWaylineEventResult。
     *
     * @param asyncRequestId 异步请求 ID
     * @param timestamp 时间戳
     * @param kmzObjectKey KMZ 对象存储 Key
     * @param waylineUuid 航线 UUID
     * @param code 结果码
     * @param message 结果消息
     * @param kmzData KMZ 解析数据
     * @param extData 扩展数据
     */
    public ImportWaylineEventResult(
            String asyncRequestId,
            long timestamp,
            String kmzObjectKey,
            String waylineUuid,
            int code,
            String message,
            ImportWaylineEventKmzData kmzData,
            ImportWaylineEventExtData extData) {
        this.asyncRequestId = asyncRequestId != null ? asyncRequestId : "";
        this.timestamp = timestamp;
        this.kmzObjectKey = kmzObjectKey != null ? kmzObjectKey : "";
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.code = code;
        this.message = message != null ? message : "";
        this.kmzData = kmzData != null ? kmzData : ImportWaylineEventKmzData.empty();
        this.extData = extData != null ? extData : ImportWaylineEventExtData.empty();
    }

    /** 异步请求 ID */
    public String getAsyncRequestId() { return asyncRequestId; }
    /** 时间戳 */
    public long getTimestamp() { return timestamp; }
    /** KMZ 对象存储 Key */
    public String getKmzObjectKey() { return kmzObjectKey; }
    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }
    /** 结果码 */
    public int getCode() { return code; }
    /** 结果消息 */
    public String getMessage() { return message; }
    /** KMZ 解析数据 */
    public ImportWaylineEventKmzData getKmzData() { return kmzData; }
    /** 扩展数据 */
    public ImportWaylineEventExtData getExtData() { return extData; }

    /** 异步请求 ID */
    public void setAsyncRequestId(String asyncRequestId) {
        this.asyncRequestId = asyncRequestId;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** KMZ 对象存储 Key */
    public void setKmzObjectKey(String kmzObjectKey) {
        this.kmzObjectKey = kmzObjectKey;
    }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** 结果码 */
    public void setCode(int code) {
        this.code = code;
    }

    /** 结果消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** KMZ 解析数据 */
    public void setKmzData(ImportWaylineEventKmzData kmzData) {
        this.kmzData = kmzData;
    }

    /** 扩展数据 */
    public void setExtData(ImportWaylineEventExtData extData) {
        this.extData = extData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportWaylineEventResult that = (ImportWaylineEventResult) o;
        return Objects.equals(asyncRequestId, that.asyncRequestId)
                && timestamp == that.timestamp
                && Objects.equals(kmzObjectKey, that.kmzObjectKey)
                && Objects.equals(waylineUuid, that.waylineUuid)
                && code == that.code
                && Objects.equals(message, that.message)
                && Objects.equals(kmzData, that.kmzData)
                && Objects.equals(extData, that.extData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                asyncRequestId,
                timestamp,
                kmzObjectKey,
                waylineUuid,
                code,
                message,
                kmzData,
                extData);
    }

    @Override
    public String toString() {
        return "ImportWaylineEventResult{"
                + "asyncRequestId='" + asyncRequestId + '\''
                + ", timestamp=" + timestamp
                + ", kmzObjectKey='" + kmzObjectKey + '\''
                + ", waylineUuid='" + waylineUuid + '\''
                + ", code=" + code
                + ", message='" + message + '\''
                + ", kmzData=" + kmzData
                + ", extData=" + extData
                + '}';
    }
}
