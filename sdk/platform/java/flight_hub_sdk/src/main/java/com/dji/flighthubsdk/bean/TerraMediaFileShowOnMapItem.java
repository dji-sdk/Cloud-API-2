// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 上图模型文件摘要（topic_media_file_show_on_map 列表项）
 */
public final class TerraMediaFileShowOnMapItem {

    /** 文件名 */
    private String name = "";
    /** 文件 ID（uint32） */
    private int id;
    /** 拍摄位置纬度 */
    private double shootLat;
    /** 拍摄位置经度 */
    private double shootLng;
    /** 拍摄位置绝对高度（米） */
    private double shootAbsAlt;
    /** 拍摄位置相对高度（米） */
    private double shootRelAlt;
    /** 照片位置纬度 */
    private double photoLat;
    /** 照片位置经度 */
    private double photoLng;
    /** 照片位置绝对高度（米） */
    private double photoAbsAlt;
    /** 照片位置相对高度（米） */
    private double photoRelAlt;
    /** 预览图 URL */
    private String previewUrl = "";
    /** 文件大类型（int32） */
    private int fileType;
    /** 文件子类型（uint32） */
    private int subFileType;

    public TerraMediaFileShowOnMapItem() {}

    public String getName() { return name; } public void setName(String v) { name = v != null ? v : ""; }
    public int getId() { return id; } public void setId(int v) { id = v; }
    public double getShootLat() { return shootLat; } public void setShootLat(double v) { shootLat = v; }
    public double getShootLng() { return shootLng; } public void setShootLng(double v) { shootLng = v; }
    public double getShootAbsAlt() { return shootAbsAlt; } public void setShootAbsAlt(double v) { shootAbsAlt = v; }
    public double getShootRelAlt() { return shootRelAlt; } public void setShootRelAlt(double v) { shootRelAlt = v; }
    public double getPhotoLat() { return photoLat; } public void setPhotoLat(double v) { photoLat = v; }
    public double getPhotoLng() { return photoLng; } public void setPhotoLng(double v) { photoLng = v; }
    public double getPhotoAbsAlt() { return photoAbsAlt; } public void setPhotoAbsAlt(double v) { photoAbsAlt = v; }
    public double getPhotoRelAlt() { return photoRelAlt; } public void setPhotoRelAlt(double v) { photoRelAlt = v; }
    public String getPreviewUrl() { return previewUrl; } public void setPreviewUrl(String v) { previewUrl = v != null ? v : ""; }
    public int getFileType() { return fileType; } public void setFileType(int v) { fileType = v; }
    public int getSubFileType() { return subFileType; } public void setSubFileType(int v) { subFileType = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraMediaFileShowOnMapItem that = (TerraMediaFileShowOnMapItem) o;
        return id == that.id
            && fileType == that.fileType
            && subFileType == that.subFileType
            && Double.compare(that.shootLat,    shootLat)    == 0
            && Double.compare(that.shootLng,    shootLng)    == 0
            && Double.compare(that.shootAbsAlt, shootAbsAlt) == 0
            && Double.compare(that.shootRelAlt, shootRelAlt) == 0
            && Double.compare(that.photoLat,    photoLat)    == 0
            && Double.compare(that.photoLng,    photoLng)    == 0
            && Double.compare(that.photoAbsAlt, photoAbsAlt) == 0
            && Double.compare(that.photoRelAlt, photoRelAlt) == 0
            && Objects.equals(name, that.name)
            && Objects.equals(previewUrl, that.previewUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fileType, subFileType, previewUrl,
            shootLat, shootLng, shootAbsAlt, shootRelAlt,
            photoLat, photoLng, photoAbsAlt, photoRelAlt);
    }
    @Override public String toString() {
        return "TerraMediaFileShowOnMapItem{id=" + id + ", name='" + name + "', fileType=" + fileType + '}';
    }
}
