// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 航线导入事件扩展数据。 */
public final class ImportWaylineEventExtData {

    private static final ImportWaylineEventExtData EMPTY =
            new ImportWaylineEventExtData("", "", "", "", Collections.emptyList());

    private String kmzWithoutResObjectKey;
    private String kmzWithoutResMd5;
    private String targetAreaFileObjectKey;
    private String businessFileObjectKey;
    private List<String> modelFileObjectKeys;
    public ImportWaylineEventExtData() {
    }

/**
     * 构造 ImportWaylineEventExtData。
     *
     * @param kmzWithoutResObjectKey 不含资源的 KMZ 对象存储 Key
     * @param kmzWithoutResMd5 不含资源的 KMZ MD5
     * @param targetAreaFileObjectKey 目标区域文件对象存储 Key
     * @param businessFileObjectKey 业务文件对象存储 Key
     * @param modelFileObjectKeys 模型文件对象存储 Key 列表
     */
    public ImportWaylineEventExtData(
            String kmzWithoutResObjectKey,
            String kmzWithoutResMd5,
            String targetAreaFileObjectKey,
            String businessFileObjectKey,
            List<String> modelFileObjectKeys) {
        this.kmzWithoutResObjectKey = kmzWithoutResObjectKey != null ? kmzWithoutResObjectKey : "";
        this.kmzWithoutResMd5 = kmzWithoutResMd5 != null ? kmzWithoutResMd5 : "";
        this.targetAreaFileObjectKey = targetAreaFileObjectKey != null ? targetAreaFileObjectKey : "";
        this.businessFileObjectKey = businessFileObjectKey != null ? businessFileObjectKey : "";
        this.modelFileObjectKeys = modelFileObjectKeys != null ? modelFileObjectKeys : Collections.emptyList();
    }

    /** 不含资源的 KMZ 对象存储 Key */
    public String getKmzWithoutResObjectKey() { return kmzWithoutResObjectKey; }
    /** 不含资源的 KMZ MD5 */
    public String getKmzWithoutResMd5() { return kmzWithoutResMd5; }
    /** 目标区域文件对象存储 Key */
    public String getTargetAreaFileObjectKey() { return targetAreaFileObjectKey; }
    /** 业务文件对象存储 Key */
    public String getBusinessFileObjectKey() { return businessFileObjectKey; }
    /** 模型文件对象存储 Key 列表 */
    public List<String> getModelFileObjectKeys() { return modelFileObjectKeys; }

    /** 不含资源的 KMZ 对象存储 Key */
    public void setKmzWithoutResObjectKey(String kmzWithoutResObjectKey) {
        this.kmzWithoutResObjectKey = kmzWithoutResObjectKey;
    }

    /** 不含资源的 KMZ MD5 */
    public void setKmzWithoutResMd5(String kmzWithoutResMd5) {
        this.kmzWithoutResMd5 = kmzWithoutResMd5;
    }

    /** 目标区域文件对象存储 Key */
    public void setTargetAreaFileObjectKey(String targetAreaFileObjectKey) {
        this.targetAreaFileObjectKey = targetAreaFileObjectKey;
    }

    /** 业务文件对象存储 Key */
    public void setBusinessFileObjectKey(String businessFileObjectKey) {
        this.businessFileObjectKey = businessFileObjectKey;
    }

    /** 模型文件对象存储 Key 列表 */
    public void setModelFileObjectKeys(List<String> modelFileObjectKeys) {
        this.modelFileObjectKeys = modelFileObjectKeys;
    }


    /** 全默认值的空实例，与 {@link ImportWaylineEventResult} 非 null 约定一致。 */
    public static ImportWaylineEventExtData empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportWaylineEventExtData that = (ImportWaylineEventExtData) o;
        return Objects.equals(kmzWithoutResObjectKey, that.kmzWithoutResObjectKey)
                && Objects.equals(kmzWithoutResMd5, that.kmzWithoutResMd5)
                && Objects.equals(targetAreaFileObjectKey, that.targetAreaFileObjectKey)
                && Objects.equals(businessFileObjectKey, that.businessFileObjectKey)
                && Objects.equals(modelFileObjectKeys, that.modelFileObjectKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                kmzWithoutResObjectKey,
                kmzWithoutResMd5,
                targetAreaFileObjectKey,
                businessFileObjectKey,
                modelFileObjectKeys);
    }

    @Override
    public String toString() {
        return "ImportWaylineEventExtData{"
                + "kmzWithoutResObjectKey='" + kmzWithoutResObjectKey + '\''
                + ", kmzWithoutResMd5='" + kmzWithoutResMd5 + '\''
                + ", targetAreaFileObjectKey='" + targetAreaFileObjectKey + '\''
                + ", businessFileObjectKey='" + businessFileObjectKey + '\''
                + ", modelFileObjectKeys=" + modelFileObjectKeys
                + '}';
    }
}
