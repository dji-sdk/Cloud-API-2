// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** PSDK UI 资源条目（psdk_ui_resource）。EA340 消息推送补全（Phase D）。 */
public final class PsdkUiResourceItem {

    private long psdkIndex;
    private long psdkReady;
    private String objectKey;

    public PsdkUiResourceItem() {
        this.objectKey = "";
    }

    /**
     * 构造 PsdkUiResourceItem。
     *
     * @param psdkIndex PSDK 索引
     * @param psdkReady PSDK 是否就绪
     * @param objectKey UI 资源文件的 object key
     */
    public PsdkUiResourceItem(long psdkIndex, long psdkReady, String objectKey) {
        this.psdkIndex = psdkIndex;
        this.psdkReady = psdkReady;
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** PSDK 索引 */
    public long getPsdkIndex() {
        return psdkIndex;
    }

    /** PSDK 索引 */
    public void setPsdkIndex(long psdkIndex) {
        this.psdkIndex = psdkIndex;
    }

    /** PSDK 是否就绪 */
    public long getPsdkReady() {
        return psdkReady;
    }

    /** PSDK 是否就绪 */
    public void setPsdkReady(long psdkReady) {
        this.psdkReady = psdkReady;
    }

    /** UI 资源文件的 object key */
    public String getObjectKey() {
        return objectKey;
    }

    /** UI 资源文件的 object key */
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
        PsdkUiResourceItem that = (PsdkUiResourceItem) o;
        return psdkIndex == that.psdkIndex
                && psdkReady == that.psdkReady
                && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psdkIndex, psdkReady, objectKey);
    }

    @Override
    public String toString() {
        return "PsdkUiResourceItem{psdkIndex=" + psdkIndex
                + ", psdkReady=" + psdkReady
                + ", objectKey='" + objectKey + "'}";
    }
}
