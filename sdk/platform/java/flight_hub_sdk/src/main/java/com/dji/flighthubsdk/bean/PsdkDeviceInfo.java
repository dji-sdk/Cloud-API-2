// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** PSDK 设备条目。 */
public final class PsdkDeviceInfo {

    private int psdkIndex;
    private String objectKey;
    public PsdkDeviceInfo() {
    }

/**
     * 构造 PsdkDeviceInfo。
     *
     * @param psdkIndex PSDK索引
     * @param objectKey 对象存储Key
     */
    public PsdkDeviceInfo(int psdkIndex, String objectKey) {
        this.psdkIndex = psdkIndex;
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** PSDK索引 */
    public int getPsdkIndex() {
        return psdkIndex;
    }

    /** PSDK索引 */
    public void setPsdkIndex(int psdkIndex) {
        this.psdkIndex = psdkIndex;
    }

    /** 对象存储Key */
    public String getObjectKey() {
        return objectKey;
    }

    /** 对象存储Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkDeviceInfo that = (PsdkDeviceInfo) o;
        return psdkIndex == that.psdkIndex && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psdkIndex, objectKey);
    }

    @Override
    public String toString() {
        return "PsdkDeviceInfo{psdkIndex=" + psdkIndex + ", objectKey='" + objectKey + '\'' + '}';
    }
}
