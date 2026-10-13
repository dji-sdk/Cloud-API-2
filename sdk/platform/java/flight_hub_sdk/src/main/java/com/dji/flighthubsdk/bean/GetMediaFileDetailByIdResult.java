// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 根据文件 ID 获取媒体文件详情结果 */
public final class GetMediaFileDetailByIdResult {

    /** 原始文件下载 URL */
    private String url;
    /** 缩略图 URL */
    private String thumbnailUrl;
    /** 报警图片 URL */
    private String alertImageUrl;
    /** 文件名 */
    private String name;
    /** 文件后缀 */
    private String suffix;
    /** 文件大小（字节） */
    private long size;
    /** 创建时间（ISO 字符串） */
    private String createAt;
    private int extWidth;
    private int extHeight;
    private MediaFileGeoPosition photoPosition;

    public GetMediaFileDetailByIdResult() {
        this.photoPosition = new MediaFileGeoPosition();
    }

    /**
     * 构造 GetMediaFileDetailByIdResult。
     *
     * @param url           原始文件下载 URL
     * @param thumbnailUrl  缩略图 URL
     * @param name          文件名
     * @param suffix        文件后缀
     * @param size          文件大小（字节）
     * @param createAt      创建时间（ISO 字符串）
     */
    public GetMediaFileDetailByIdResult(String url, String thumbnailUrl, String name,
            String suffix, long size, String createAt) {
        this.url = url != null ? url : "";
        this.thumbnailUrl = thumbnailUrl != null ? thumbnailUrl : "";
        this.alertImageUrl = "";
        this.name = name != null ? name : "";
        this.suffix = suffix != null ? suffix : "";
        this.size = size;
        this.createAt = createAt != null ? createAt : "";
        this.photoPosition = new MediaFileGeoPosition();
    }

    /** 原始文件下载 URL */
    public String getUrl() { return url; }

    /** 原始文件下载 URL */
    public void setUrl(String url) {
        this.url = url;
    }

    /** 缩略图 URL */
    public String getThumbnailUrl() { return thumbnailUrl; }

    /** 缩略图 URL */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /** 报警图片 URL */
    public String getAlertImageUrl() { return alertImageUrl; }

    /** 报警图片 URL */
    public void setAlertImageUrl(String alertImageUrl) {
        this.alertImageUrl = alertImageUrl;
    }

    /** 文件名 */
    public String getName() { return name; }

    /** 文件名 */
    public void setName(String name) {
        this.name = name;
    }

    /** 文件后缀 */
    public String getSuffix() { return suffix; }

    /** 文件后缀 */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /** 文件大小（字节） */
    public long getSize() { return size; }

    /** 文件大小（字节） */
    public void setSize(long size) {
        this.size = size;
    }

    /** 创建时间（ISO 字符串） */
    public String getCreateAt() { return createAt; }

    /** 创建时间（ISO 字符串） */
    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    /** 图片宽度（像素） */
    public int getExtWidth() { return extWidth; }

    /** 图片宽度（像素） */
    public void setExtWidth(int extWidth) {
        this.extWidth = extWidth;
    }

    /** 图片高度（像素） */
    public int getExtHeight() { return extHeight; }

    /** 图片高度（像素） */
    public void setExtHeight(int extHeight) {
        this.extHeight = extHeight;
    }

    /** 照片 GPS 位置 */
    public MediaFileGeoPosition getPhotoPosition() { return photoPosition; }

    /** 照片 GPS 位置 */
    public void setPhotoPosition(MediaFileGeoPosition photoPosition) {
        this.photoPosition = photoPosition != null ? photoPosition : new MediaFileGeoPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetMediaFileDetailByIdResult that = (GetMediaFileDetailByIdResult) o;
        return size == that.size
                && Objects.equals(url, that.url)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, size);
    }

    @Override
    public String toString() {
        return "GetMediaFileDetailByIdResult{"
                + "name='" + name + '\''
                + ", suffix='" + suffix + '\''
                + ", size=" + size
                + ", url='" + (url != null ? url.substring(0, Math.min(50, url.length())) : "") + "...'"
                + '}';
    }
}
