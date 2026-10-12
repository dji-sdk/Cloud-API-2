// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 获取 PSDK 小组件资源请求参数 */
public class GetPsdkWidgetResourceParams {
    /** required, dive, required */
    private List<String> objectKeys = new ArrayList<>();

    public GetPsdkWidgetResourceParams() {
    }


    /** required, dive, required */
    public List<String> getObjectKeys() {
        return objectKeys;
    }

    /** @param objectKeys required, dive, required */
    public void setObjectKeys(List<String> objectKeys) {
        this.objectKeys = objectKeys != null ? objectKeys : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetPsdkWidgetResourceParams that = (GetPsdkWidgetResourceParams) o;
        return Objects.equals(objectKeys, that.objectKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKeys);
    }

    @Override
    public String toString() {
        return "GetPsdkWidgetResourceParams{objectKeys=" + objectKeys + '}';
    }
}
