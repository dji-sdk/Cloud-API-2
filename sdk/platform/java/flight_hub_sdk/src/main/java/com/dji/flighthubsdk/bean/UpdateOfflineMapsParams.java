// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 更新离线地图模型请求参数 */
public class UpdateOfflineMapsParams {
    private List<ModelItemInfoData> model3dItems = new ArrayList<>();
    private List<ModelItemInfoData> model2dItems = new ArrayList<>();

    public UpdateOfflineMapsParams() {
    }


    /** 3D模型项列表 */
    public List<ModelItemInfoData> getModel3dItems() { return model3dItems; }
    /** @param model3dItems 3D模型项列表 */
    public void setModel3dItems(List<ModelItemInfoData> model3dItems) {
        this.model3dItems = model3dItems != null ? model3dItems : new ArrayList<>();
    }

    /** 2D模型项列表 */
    public List<ModelItemInfoData> getModel2dItems() { return model2dItems; }
    /** @param model2dItems 2D模型项列表 */
    public void setModel2dItems(List<ModelItemInfoData> model2dItems) {
        this.model2dItems = model2dItems != null ? model2dItems : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateOfflineMapsParams that = (UpdateOfflineMapsParams) o;
        return Objects.equals(model3dItems, that.model3dItems)
                && Objects.equals(model2dItems, that.model2dItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model3dItems, model2dItems);
    }

    @Override
    public String toString() {
        return "UpdateOfflineMapsParams{model3dItems=" + model3dItems + ", model2dItems=" + model2dItems + '}';
    }
}
