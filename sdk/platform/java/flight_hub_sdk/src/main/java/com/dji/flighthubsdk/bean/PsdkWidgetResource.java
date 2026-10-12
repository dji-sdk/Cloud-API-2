// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** PSDK 小组件资源 */
public final class PsdkWidgetResource {
    private String objectKey;
    private String url;
    public PsdkWidgetResource() {
    }

/**
     * 构造 PsdkWidgetResource。
     *
     * @param objectKey 对象存储Key
     * @param url URL地址
     */
    public PsdkWidgetResource(String objectKey, String url) {
        this.objectKey = objectKey != null ? objectKey : "";
        this.url = url != null ? url : "";
    }

    /** 对象存储Key */
    public String getObjectKey() { return objectKey; }
    /** URL地址 */
    public String getUrl() { return url; }

    /** 对象存储Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** URL地址 */
    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsdkWidgetResource that = (PsdkWidgetResource) o;
        return Objects.equals(objectKey, that.objectKey) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, url);
    }

    @Override
    public String toString() {
        return "PsdkWidgetResource{objectKey='" + objectKey + '\'' + ", url='" + url + '\'' + '}';
    }
}
