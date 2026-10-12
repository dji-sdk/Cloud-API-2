// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 飞机 PSDK UI 资源推送（psdk_ui_resource）。EA340 消息推送补全（Phase D）。 */
public final class DronePsdkUiResource {

    private List<PsdkUiResourceItem> psdkUiResource;

    public DronePsdkUiResource() {
        this.psdkUiResource = Collections.emptyList();
    }

    /**
     * 构造 DronePsdkUiResource。
     *
     * @param psdkUiResource PSDK UI 资源列表
     */
    public DronePsdkUiResource(List<PsdkUiResourceItem> psdkUiResource) {
        this.psdkUiResource = psdkUiResource != null ? psdkUiResource : Collections.emptyList();
    }

    /** PSDK UI 资源列表 */
    public List<PsdkUiResourceItem> getPsdkUiResource() {
        return psdkUiResource;
    }

    /** PSDK UI 资源列表 */
    public void setPsdkUiResource(List<PsdkUiResourceItem> psdkUiResource) {
        this.psdkUiResource = psdkUiResource != null ? psdkUiResource : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DronePsdkUiResource that = (DronePsdkUiResource) o;
        return Objects.equals(psdkUiResource, that.psdkUiResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psdkUiResource);
    }

    @Override
    public String toString() {
        return "DronePsdkUiResource{psdkUiResource=" + psdkUiResource + '}';
    }
}
