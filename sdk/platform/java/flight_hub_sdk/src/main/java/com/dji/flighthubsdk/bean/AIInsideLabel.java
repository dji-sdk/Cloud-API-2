// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 算法目标标签 */
public class AIInsideLabel {
    /** 标签类型枚举值（如 1=目标类型） */
    private int type;
    /** 标签名称（如 "fire"、"smoke"） */
    private String name = "";

    public AIInsideLabel() {}

    /** 标签类型枚举值（如 1=目标类型） */
    public int getType() { return type; }

    /** @param v 标签类型枚举值（如 1=目标类型） */
    public void setType(int v) { this.type = v; }

    /** 标签名称（如 "fire"、"smoke"） */
    public String getName() { return name; }

    /** @param v 标签名称（如 "fire"、"smoke"） */
    public void setName(String v) { this.name = v != null ? v : ""; }
}
