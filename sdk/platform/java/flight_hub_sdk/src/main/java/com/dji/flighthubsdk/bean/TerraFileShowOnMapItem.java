// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 文件展示地图列表项
 */
public final class TerraFileShowOnMapItem {

    /** File 记录 ID */
    private long id;
    /** 文件名 */
    private String name;
    /** 文件类型 */
    private int fileType;
    /** 子文件类型 */
    private int subFileType;
    /** 预览图签名 URL */
    private String previewUrl;

    public TerraFileShowOnMapItem() {
    }

    public TerraFileShowOnMapItem(long id, String name, int fileType, int subFileType, String previewUrl) {
        this.id = id;
        this.name = name != null ? name : "";
        this.fileType = fileType;
        this.subFileType = subFileType;
        this.previewUrl = previewUrl != null ? previewUrl : "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFileType() { return fileType; }
    public void setFileType(int fileType) { this.fileType = fileType; }

    public int getSubFileType() { return subFileType; }
    public void setSubFileType(int subFileType) { this.subFileType = subFileType; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraFileShowOnMapItem that = (TerraFileShowOnMapItem) o;
        return id == that.id && fileType == that.fileType && subFileType == that.subFileType
                && Objects.equals(name, that.name) && Objects.equals(previewUrl, that.previewUrl);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name, fileType, subFileType, previewUrl); }

    @Override
    public String toString() {
        return "TerraFileShowOnMapItem{id=" + id + ", name='" + name
                + "', fileType=" + fileType + ", subFileType=" + subFileType + '}';
    }
}
