// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** PSDK 小组件资源列表 */
public final class PsdkWidgetResourceList {
    private List<PsdkWidgetResource> uiResourceUrls;
    public PsdkWidgetResourceList() {
    }

/**
     * 构造 PsdkWidgetResourceList。
     *
     * @param uiResourceUrls UI资源URL列表
     */
    public PsdkWidgetResourceList(List<PsdkWidgetResource> uiResourceUrls) {
        this.uiResourceUrls = uiResourceUrls != null ? uiResourceUrls : Collections.emptyList();
    }

    /** UI资源URL列表 */
    public List<PsdkWidgetResource> getUiResourceUrls() {
        return uiResourceUrls;
    }

    /** UI资源URL列表 */
    public void setUiResourceUrls(List<PsdkWidgetResource> uiResourceUrls) {
        this.uiResourceUrls = uiResourceUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsdkWidgetResourceList that = (PsdkWidgetResourceList) o;
        return Objects.equals(uiResourceUrls, that.uiResourceUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uiResourceUrls);
    }

    @Override
    public String toString() {
        return "PsdkWidgetResourceList{uiResourceUrls=" + uiResourceUrls + '}';
    }
}
