// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 内容属性数据。 */
public final class ContentsPropertyData {
    /** 颜色, 颜色值用于方便在地图上面区分不同区域, 有效值比如 "#FF0000" */
    private String color;
    private int width;
    /** 服务端(旧协议设计上字段是 "clampToGround"); */
    private boolean clampToGround;
    public ContentsPropertyData() {
    }

/**
     * 构造 ContentsPropertyData。
     *
     * @param color 颜色，格式如 "#FF0000"
     * @param width 缓冲宽度（米），仅折线(Polyline)类型有效，取值范围 [10, 100]，默认 10
     * @param clampToGround 是否贴地
     */
    public ContentsPropertyData(String color, int width, boolean clampToGround) {
        this.color = color != null ? color : "";
        this.width = width;
        this.clampToGround = clampToGround;
    }

    /** 颜色, 颜色值用于方便在地图上面区分不同区域, 有效值比如 "#FF0000" */
    public String getColor() {
        return color;
    }

    /** @param color 颜色, 颜色值用于方便在地图上面区分不同区域, 有效值比如 "#FF0000" */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 缓冲宽度（米），仅折线（Polyline）类型飞行区域有效。
     * <p>取值范围：10 ~ 100，默认值 10。</p>
     * <p>含义：折线两侧各扩展 width/2 米形成面状区域。</p>
     */
    public int getWidth() {
        return width;
    }

    /**
     * 缓冲宽度（米），仅折线（Polyline）类型飞行区域有效。
     * <p>取值范围：10 ~ 100，默认值 10。</p>
     * <p>含义：折线两侧各扩展 width/2 米形成面状区域。</p>
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /** 服务端(旧协议设计上字段是 "clampToGround"); */
    public boolean isClampToGround() {
        return clampToGround;
    }

    /** @param clampToGround 服务端(旧协议设计上字段是 "clampToGround"); */
    public void setClampToGround(boolean clampToGround) {
        this.clampToGround = clampToGround;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContentsPropertyData that = (ContentsPropertyData) o;
        return Objects.equals(color, that.color)
                && width == that.width
                && clampToGround == that.clampToGround;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, width, clampToGround);
    }

    @Override
    public String toString() {
        return "ContentsPropertyData{color='" + color + '\'' + ", width=" + width + ", clampToGround=" + clampToGround + '}';
    }
}
