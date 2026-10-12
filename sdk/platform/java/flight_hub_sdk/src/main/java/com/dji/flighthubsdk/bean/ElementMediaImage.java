// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素绑定的媒体图片（对齐 proto ElementMediaImage） */
public final class ElementMediaImage {

    private String objectKey = "";
    private int mediaFileId;
    private String mediaFileUuid = "";

    public ElementMediaImage() {
    }

    /**
     * 构造 ElementMediaImage。
     *
     * @param objectKey     OSS 对象 key
     * @param mediaFileId   媒体库文件 ID
     * @param mediaFileUuid 媒体库文件 UUID
     */
    public ElementMediaImage(String objectKey, int mediaFileId, String mediaFileUuid) {
        this.objectKey = objectKey != null ? objectKey : "";
        this.mediaFileId = mediaFileId;
        this.mediaFileUuid = mediaFileUuid != null ? mediaFileUuid : "";
    }

    /** OSS 对象 key */
    public String getObjectKey() { return objectKey; }

    /** OSS 对象 key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** 媒体库文件 ID */
    public int getMediaFileId() { return mediaFileId; }

    /** 媒体库文件 ID */
    public void setMediaFileId(int mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    /** 媒体库文件 UUID */
    public String getMediaFileUuid() { return mediaFileUuid; }

    /** 媒体库文件 UUID */
    public void setMediaFileUuid(String mediaFileUuid) {
        this.mediaFileUuid = mediaFileUuid != null ? mediaFileUuid : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementMediaImage that = (ElementMediaImage) o;
        return mediaFileId == that.mediaFileId
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(mediaFileUuid, that.mediaFileUuid);
    }

    @Override
    public int hashCode() { return Objects.hash(objectKey, mediaFileId, mediaFileUuid); }

    @Override
    public String toString() {
        return "ElementMediaImage{"
                + "objectKey='" + objectKey + '\''
                + ", mediaFileId=" + mediaFileId
                + ", mediaFileUuid='" + mediaFileUuid + '\''
                + '}';
    }
}
