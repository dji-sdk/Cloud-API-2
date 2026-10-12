// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 获取在线观众数量 - 请求项（设备 SN + 摄像头列表） */
public class DeviceAudienceCountRequestItem {
    private String sn = "";
    private List<String> cameraList = new ArrayList<>();

    public DeviceAudienceCountRequestItem() {
    }


    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** @param sn 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn != null ? sn : "";
    }

    /** 相机列表 */
    public List<String> getCameraList() {
        return cameraList;
    }

    /** @param cameraList 相机列表 */
    public void setCameraList(List<String> cameraList) {
        this.cameraList = cameraList != null ? cameraList : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceAudienceCountRequestItem that = (DeviceAudienceCountRequestItem) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(cameraList, that.cameraList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, cameraList);
    }

    @Override
    public String toString() {
        return "DeviceAudienceCountRequestItem{sn='" + sn + '\'' + ", cameraList=" + cameraList + '}';
    }
}
