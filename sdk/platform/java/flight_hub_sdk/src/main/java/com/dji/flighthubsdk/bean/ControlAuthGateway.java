// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 控制权网关信息。 */
public final class ControlAuthGateway {
    /** 网关 SN */
    private String sn = "";
    /** 网关 ID */
    private String id = "";
    /** 网关正在代理的控制权 key */
    private List<String> authControlKeys = new ArrayList<>();
    /** 网关类型 */
    private String deviceModelClass = "";

    public ControlAuthGateway() {
    }

    /** 网关 SN */
    public String getSn() { return sn; }
    /** @param sn 网关 SN */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }

    /** 网关 ID */
    public String getId() { return id; }
    /** @param id 网关 ID */
    public void setId(String id) { this.id = id != null ? id : ""; }

    /** 网关正在代理的控制权 key */
    public List<String> getAuthControlKeys() { return authControlKeys; }
    /** @param authControlKeys 网关正在代理的控制权 key */
    public void setAuthControlKeys(List<String> authControlKeys) {
        this.authControlKeys = authControlKeys != null ? authControlKeys : new ArrayList<>();
    }

    /** 网关类型 */
    public String getDeviceModelClass() { return deviceModelClass; }
    /** @param deviceModelClass 网关类型 */
    public void setDeviceModelClass(String deviceModelClass) {
        this.deviceModelClass = deviceModelClass != null ? deviceModelClass : "";
    }
}
