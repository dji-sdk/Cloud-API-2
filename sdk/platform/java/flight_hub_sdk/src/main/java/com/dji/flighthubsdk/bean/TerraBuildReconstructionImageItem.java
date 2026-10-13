// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Terra Build 重建任务图片列表项 */
public final class TerraBuildReconstructionImageItem {
    private String imageId;
    private String fileName;
    private String thumbnailImageUrl;
    private String originImageUrl;
    public TerraBuildReconstructionImageItem() {
    }

/**
     * 构造 TerraBuildReconstructionImageItem。
     *
     * @param imageId 图片ID
     * @param fileName 文件名
     * @param thumbnailImageUrl Thumbnail图片URL
     * @param originImageUrl Origin图片URL
     */
    public TerraBuildReconstructionImageItem(String imageId, String fileName, String thumbnailImageUrl, String originImageUrl) {
        this.imageId = imageId != null ? imageId : "";
        this.fileName = fileName != null ? fileName : "";
        this.thumbnailImageUrl = thumbnailImageUrl != null ? thumbnailImageUrl : "";
        this.originImageUrl = originImageUrl != null ? originImageUrl : "";
    }

    /** 图片ID */
    public String getImageId() { return imageId; }
    /** 文件名 */
    public String getFileName() { return fileName; }
    /** Thumbnail图片URL */
    public String getThumbnailImageUrl() { return thumbnailImageUrl; }
    /** Origin图片URL */
    public String getOriginImageUrl() { return originImageUrl; }

    /** 图片ID */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /** 文件名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** Thumbnail图片URL */
    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    /** Origin图片URL */
    public void setOriginImageUrl(String originImageUrl) {
        this.originImageUrl = originImageUrl;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildReconstructionImageItem that = (TerraBuildReconstructionImageItem) o;
        return Objects.equals(imageId, that.imageId) && Objects.equals(fileName, that.fileName)
                && Objects.equals(thumbnailImageUrl, that.thumbnailImageUrl) && Objects.equals(originImageUrl, that.originImageUrl);
    }
    @Override public int hashCode() { return Objects.hash(imageId, fileName, thumbnailImageUrl, originImageUrl); }

    @Override
    public String toString() {
        return "TerraBuildReconstructionImageItem{"
                + "imageId='" + imageId + '\''
                + ", fileName='" + fileName + '\''
                + ", thumbnailImageUrl='" + thumbnailImageUrl + '\''
                + ", originImageUrl='" + originImageUrl + '\''
                + '}';
    }
}
