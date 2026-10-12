// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 获取文件下载链接请求参数 */
public class FetchFileDownloadUrlParam {
    private String deviceSn = "";
    private String boxSn = "";
    private List<String> objectKeys = new ArrayList<>();

    public FetchFileDownloadUrlParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }

    /** 盒子序列号 */
    public String getBoxSn() { return boxSn; }
    /** @param boxSn 盒子序列号 */
    public void setBoxSn(String boxSn) { this.boxSn = boxSn != null ? boxSn : ""; }

    /** 对象存储 Key 列表 */
    public List<String> getObjectKeys() { return objectKeys; }
    /** @param objectKeys 对象存储 Key 列表 */
    public void setObjectKeys(List<String> objectKeys) { this.objectKeys = objectKeys != null ? objectKeys : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FetchFileDownloadUrlParam that = (FetchFileDownloadUrlParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(boxSn, that.boxSn)
                && Objects.equals(objectKeys, that.objectKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, boxSn, objectKeys);
    }

    @Override
    public String toString() {
        return "FetchFileDownloadUrlParam{deviceSn='" + deviceSn + '\'' + ", boxSn='" + boxSn + '\'' + ", objectKeys=" + objectKeys + '}';
    }
}
