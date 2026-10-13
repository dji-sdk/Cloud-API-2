// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素资源体（对齐 proto ElementResourceData） */
public final class ElementResourceData {

    private ElementResourceContent content;
    private int type;
    private String userName = "";
    private String userId = "";
    private String remark = "";

    public ElementResourceData() {
    }

    /** 几何内容（GeoJSON Feature） */
    public ElementResourceContent getContent() { return content; }

    /** 几何内容（GeoJSON Feature） */
    public void setContent(ElementResourceContent content) {
        this.content = content;
    }

    /** 几何类型：0=点 1=线 2=面 7=圆 */
    public int getType() { return type; }

    /** 几何类型：0=点 1=线 2=面 7=圆 */
    public void setType(int type) {
        this.type = type;
    }

    /** 创建者昵称 */
    public String getUserName() { return userName; }

    /** 创建者昵称 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 创建者 UID */
    public String getUserId() { return userId; }

    /** 创建者 UID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** 备注 */
    public String getRemark() { return remark; }

    /** 备注 */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementResourceData that = (ElementResourceData) o;
        return type == that.type
                && Objects.equals(content, that.content)
                && Objects.equals(userName, that.userName)
                && Objects.equals(userId, that.userId)
                && Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, type, userName, userId, remark);
    }

    @Override
    public String toString() {
        return "ElementResourceData{"
                + "type=" + type
                + ", userName='" + userName + '\''
                + ", userId='" + userId + '\''
                + ", remark='" + remark + '\''
                + ", content=" + content
                + '}';
    }
}
