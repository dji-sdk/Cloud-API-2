// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备定损 - 拓扑节点参数 */
public class SelfDiagnosisTopoNodeParams {
    private String sn = "";
    private String type = "";
    private int domain;
    private String deviceModel = "";

    public SelfDiagnosisTopoNodeParams() {
    }


    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** @param sn 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn != null ? sn : "";
    }

    /** 类型 */
    public String getType() {
        return type;
    }

    /** @param type 类型 */
    public void setType(String type) {
        this.type = type != null ? type : "";
    }

    /** 领域 */
    public int getDomain() {
        return domain;
    }

    /** @param domain 领域 */
    public void setDomain(int domain) {
        this.domain = domain;
    }

    /** 设备型号 */
    public String getDeviceModel() {
        return deviceModel;
    }

    /** @param deviceModel 设备型号 */
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel != null ? deviceModel : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelfDiagnosisTopoNodeParams that = (SelfDiagnosisTopoNodeParams) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(type, that.type)
                && domain == that.domain
                && Objects.equals(deviceModel, that.deviceModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, type, domain, deviceModel);
    }

    @Override
    public String toString() {
        return "SelfDiagnosisTopoNodeParams{"
                + "sn='" + sn + '\''
                + ", type='" + type + '\''
                + ", domain=" + domain
                + ", deviceModel='" + deviceModel + '\''
                + '}';
    }
}
