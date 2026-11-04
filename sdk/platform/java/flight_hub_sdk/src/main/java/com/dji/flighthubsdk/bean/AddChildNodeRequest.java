// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 新增子节点请求（对齐 v1.0 §1）。 */
public final class AddChildNodeRequest {
    private String sourceAddr;
    private String childAddr;
    private String name;
    private String code;
    private String token;

    public AddChildNodeRequest() {
        this("", "", "", "", "");
    }

    /**
     * 构造 AddChildNodeRequest。
     *
     * @param sourceAddr 上级节点对子节点暴露的接入地址（host:port）
     * @param childAddr  子节点接入地址（host:port）
     * @param name       子节点名称
     * @param code       子节点编码
     * @param token      配网 token
     */
    public AddChildNodeRequest(String sourceAddr, String childAddr, String name, String code, String token) {
        this.sourceAddr = sourceAddr != null ? sourceAddr : "";
        this.childAddr = childAddr != null ? childAddr : "";
        this.name = name != null ? name : "";
        this.code = code != null ? code : "";
        this.token = token != null ? token : "";
    }

    /** 上级节点对子节点暴露的接入地址 */
    public String getSourceAddr() { return sourceAddr; }
    /** 上级节点对子节点暴露的接入地址 */
    public void setSourceAddr(String sourceAddr) { this.sourceAddr = sourceAddr; }

    /** 子节点接入地址 */
    public String getChildAddr() { return childAddr; }
    /** 子节点接入地址 */
    public void setChildAddr(String childAddr) { this.childAddr = childAddr; }

    /** 子节点名称 */
    public String getName() { return name; }
    /** 子节点名称 */
    public void setName(String name) { this.name = name; }

    /** 子节点编码 */
    public String getCode() { return code; }
    /** 子节点编码 */
    public void setCode(String code) { this.code = code; }

    /** 配网 token */
    public String getToken() { return token; }
    /** 配网 token */
    public void setToken(String token) { this.token = token; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddChildNodeRequest that = (AddChildNodeRequest) o;
        return Objects.equals(sourceAddr, that.sourceAddr) && Objects.equals(childAddr, that.childAddr)
                && Objects.equals(name, that.name) && Objects.equals(code, that.code)
                && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() { return Objects.hash(sourceAddr, childAddr, name, code, token); }

    @Override
    public String toString() {
        return "AddChildNodeRequest{sourceAddr='" + sourceAddr + "', childAddr='" + childAddr
                + "', name='" + name + "', code='" + code + "', token='***'}";
    }
}
