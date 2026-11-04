// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 单个控制权条目。 */
public final class ControlAuthController {
    /** 控制 Key，如 flight、payload-5-3-0 */
    private String key = "";
    /** 控制类型，如 flight、payload */
    private String type = "";
    /** 控制权版本 */
    private long version;
    /** 控制网关 */
    private ControlAuthGateway gateway;
    /** 控制用户 */
    private ControlAuthUser user;
    /** 扩展信息 */
    private ControlAuthSpec spec;

    public ControlAuthController() {
    }

    /** 控制 Key */
    public String getKey() { return key; }
    /** @param key 控制 Key */
    public void setKey(String key) { this.key = key != null ? key : ""; }

    /** 控制类型 */
    public String getType() { return type; }
    /** @param type 控制类型 */
    public void setType(String type) { this.type = type != null ? type : ""; }

    /** 控制权版本 */
    public long getVersion() { return version; }
    /** @param version 控制权版本 */
    public void setVersion(long version) { this.version = version; }

    /** 控制网关 */
    public ControlAuthGateway getGateway() { return gateway; }
    /** @param gateway 控制网关 */
    public void setGateway(ControlAuthGateway gateway) { this.gateway = gateway; }

    /** 控制用户 */
    public ControlAuthUser getUser() { return user; }
    /** @param user 控制用户 */
    public void setUser(ControlAuthUser user) { this.user = user; }

    /** 扩展信息 */
    public ControlAuthSpec getSpec() { return spec; }
    /** @param spec 扩展信息 */
    public void setSpec(ControlAuthSpec spec) { this.spec = spec; }
}
