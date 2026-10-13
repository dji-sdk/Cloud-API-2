// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 基础照片存储格式能力。 */
public final class CameraPhotoStorageFormat {
    private int photoStorageFormatNum;
    private List<String> photoStorageFormats;

    public CameraPhotoStorageFormat() {}
    public CameraPhotoStorageFormat(int photoStorageFormatNum, List<String> photoStorageFormats) {
        this.photoStorageFormatNum = photoStorageFormatNum;
        this.photoStorageFormats = photoStorageFormats;
    }
    public int getPhotoStorageFormatNum() { return photoStorageFormatNum; }
    public void setPhotoStorageFormatNum(int v) { this.photoStorageFormatNum = v; }
    public List<String> getPhotoStorageFormats() { return photoStorageFormats; }
    public void setPhotoStorageFormats(List<String> v) { this.photoStorageFormats = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraPhotoStorageFormat)) return false;
        CameraPhotoStorageFormat that = (CameraPhotoStorageFormat) o;
        return photoStorageFormatNum == that.photoStorageFormatNum
                && Objects.equals(photoStorageFormats, that.photoStorageFormats);
    }
    @Override public int hashCode() { return Objects.hash(photoStorageFormatNum, photoStorageFormats); }
    @Override public String toString() {
        return "CameraPhotoStorageFormat{num=" + photoStorageFormatNum + ", formats=" + photoStorageFormats + '}';
    }
}
