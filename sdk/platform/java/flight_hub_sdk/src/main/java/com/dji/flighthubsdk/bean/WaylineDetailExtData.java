// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 航线详情扩展数据 */
public final class WaylineDetailExtData {

    private String kmzWithoutResObjectKey;
    private String kmzWithoutResMd5;
    private String targetAreaFileObjectKey;
    private String businessFileObjectKey;
    private List<String> modelFileObjectKeys = new ArrayList<>();
    private List<String> elevationFileObjectKeys = new ArrayList<>();
    private long contentLength;

    public WaylineDetailExtData() {
    }

    public WaylineDetailExtData(
            String kmzWithoutResObjectKey,
            String kmzWithoutResMd5,
            String targetAreaFileObjectKey,
            String businessFileObjectKey) {
        this.kmzWithoutResObjectKey = kmzWithoutResObjectKey != null ? kmzWithoutResObjectKey : "";
        this.kmzWithoutResMd5 = kmzWithoutResMd5 != null ? kmzWithoutResMd5 : "";
        this.targetAreaFileObjectKey = targetAreaFileObjectKey != null ? targetAreaFileObjectKey : "";
        this.businessFileObjectKey = businessFileObjectKey != null ? businessFileObjectKey : "";
    }

    /** 不含资源的 KMZ 对象 Key */
    public String getKmzWithoutResObjectKey() { return kmzWithoutResObjectKey; }

    /** 不含资源的 KMZ MD5 */
    public String getKmzWithoutResMd5() { return kmzWithoutResMd5; }

    /** 目标区域文件对象 Key */
    public String getTargetAreaFileObjectKey() { return targetAreaFileObjectKey; }

    /** 业务文件对象 Key */
    public String getBusinessFileObjectKey() { return businessFileObjectKey; }

    /** 贴近摄影航线关联模型索引文件存储桶路径列表 */
    public List<String> getModelFileObjectKeys() { return modelFileObjectKeys; }

    /** 高程文件存储桶路径列表 */
    public List<String> getElevationFileObjectKeys() { return elevationFileObjectKeys; }

    /** 航线文件大小（字节） */
    public long getContentLength() { return contentLength; }

    /** @param kmzWithoutResObjectKey 不含资源的 KMZ 对象 Key */
    public void setKmzWithoutResObjectKey(String kmzWithoutResObjectKey) {
        this.kmzWithoutResObjectKey = kmzWithoutResObjectKey;
    }

    /** @param kmzWithoutResMd5 不含资源的 KMZ MD5 */
    public void setKmzWithoutResMd5(String kmzWithoutResMd5) {
        this.kmzWithoutResMd5 = kmzWithoutResMd5;
    }

    /** @param targetAreaFileObjectKey 目标区域文件对象 Key */
    public void setTargetAreaFileObjectKey(String targetAreaFileObjectKey) {
        this.targetAreaFileObjectKey = targetAreaFileObjectKey;
    }

    /** @param businessFileObjectKey 业务文件对象 Key */
    public void setBusinessFileObjectKey(String businessFileObjectKey) {
        this.businessFileObjectKey = businessFileObjectKey;
    }

    /** @param modelFileObjectKeys 贴近摄影航线关联模型索引文件路径列表 */
    public void setModelFileObjectKeys(List<String> modelFileObjectKeys) {
        this.modelFileObjectKeys = modelFileObjectKeys != null ? modelFileObjectKeys : new ArrayList<>();
    }

    /** @param elevationFileObjectKeys 高程文件存储桶路径列表 */
    public void setElevationFileObjectKeys(List<String> elevationFileObjectKeys) {
        this.elevationFileObjectKeys = elevationFileObjectKeys != null ? elevationFileObjectKeys : new ArrayList<>();
    }

    /** @param contentLength 航线文件大小（字节） */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaylineDetailExtData that = (WaylineDetailExtData) o;
        return contentLength == that.contentLength
                && Objects.equals(kmzWithoutResObjectKey, that.kmzWithoutResObjectKey)
                && Objects.equals(kmzWithoutResMd5, that.kmzWithoutResMd5)
                && Objects.equals(targetAreaFileObjectKey, that.targetAreaFileObjectKey)
                && Objects.equals(businessFileObjectKey, that.businessFileObjectKey)
                && Objects.equals(modelFileObjectKeys, that.modelFileObjectKeys)
                && Objects.equals(elevationFileObjectKeys, that.elevationFileObjectKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kmzWithoutResObjectKey, kmzWithoutResMd5, targetAreaFileObjectKey,
                businessFileObjectKey, modelFileObjectKeys, elevationFileObjectKeys, contentLength);
    }

    @Override
    public String toString() {
        return "WaylineDetailExtData{"
                + "kmzWithoutResObjectKey='" + kmzWithoutResObjectKey + '\''
                + ", kmzWithoutResMd5='" + kmzWithoutResMd5 + '\''
                + ", targetAreaFileObjectKey='" + targetAreaFileObjectKey + '\''
                + ", businessFileObjectKey='" + businessFileObjectKey + '\''
                + ", modelFileObjectKeys=" + modelFileObjectKeys
                + ", elevationFileObjectKeys=" + elevationFileObjectKeys
                + ", contentLength=" + contentLength
                + '}';
    }
}
