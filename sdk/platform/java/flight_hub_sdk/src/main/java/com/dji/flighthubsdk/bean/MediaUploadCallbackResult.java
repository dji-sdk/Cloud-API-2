// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传回调通知的返回结果。
 */
public final class MediaUploadCallbackResult {
    private String objectKey;
    private String filename;
    private String flightId;
    private String uuid;
    private int id;
    private int mediaId;
    public MediaUploadCallbackResult() {
    }

/**
     * 构造 MediaUploadCallbackResult。
     *
     * @param objectKey 对象存储 Key
     * @param filename 文件名
     * @param flightId 飞行任务 ID
     * @param uuid 媒体 UUID
     * @param id 记录 ID
     * @param mediaId 媒体 ID
     */
    public MediaUploadCallbackResult(
            String objectKey,
            String filename,
            String flightId,
            String uuid,
            int id,
            int mediaId) {
        this.objectKey = objectKey != null ? objectKey : "";
        this.filename = filename != null ? filename : "";
        this.flightId = flightId != null ? flightId : "";
        this.uuid = uuid != null ? uuid : "";
        this.id = id;
        this.mediaId = mediaId;
    }

    /** 对象存储 Key */
    public String getObjectKey() { return objectKey; }

    /** 文件名 */
    public String getFilename() { return filename; }

    /** 飞行任务 ID */
    public String getFlightId() { return flightId; }

    /** 媒体 UUID */
    public String getUuid() { return uuid; }

    /** 记录 ID */
    public int getId() { return id; }

    /** 媒体 ID */
    public int getMediaId() { return mediaId; }

    /** 对象存储 Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 文件名 */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** 飞行任务 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 媒体 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 记录 ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 媒体 ID */
    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadCallbackResult that = (MediaUploadCallbackResult) o;
        return id == that.id
                && mediaId == that.mediaId
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(filename, that.filename)
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, filename, flightId, uuid, id, mediaId);
    }

    @Override
    public String toString() {
        return "MediaUploadCallbackResult{"
                + "objectKey='" + objectKey + '\''
                + ", filename='" + filename + '\''
                + ", flightId='" + flightId + '\''
                + ", uuid='" + uuid + '\''
                + ", id=" + id
                + ", mediaId=" + mediaId
                + '}';
    }
}
