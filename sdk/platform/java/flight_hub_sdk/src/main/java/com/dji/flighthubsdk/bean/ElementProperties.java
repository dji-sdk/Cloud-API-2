// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素渲染属性（GeoJSON Feature Properties，对齐 proto ElementProperties） */
public final class ElementProperties {

    private String color = "";
    private boolean clampToGround;
    private boolean is3d;
    private boolean isTransparent;
    private int fontSize;
    private int icon;

    public ElementProperties() {
    }

    /** 渲染颜色，HEX 格式（如 "#2D8CF0"） */
    public String getColor() { return color; }

    /** 渲染颜色，HEX 格式（如 "#2D8CF0"） */
    public void setColor(String color) {
        this.color = color != null ? color : "";
    }

    /** 是否贴地渲染 */
    public boolean isClampToGround() { return clampToGround; }

    /** 是否贴地渲染 */
    public void setClampToGround(boolean clampToGround) {
        this.clampToGround = clampToGround;
    }

    /** 是否 3D 要素 */
    public boolean isIs3d() { return is3d; }

    /** 是否 3D 要素 */
    public void setIs3d(boolean is3d) {
        this.is3d = is3d;
    }

    /** 要素是否透明渲染 */
    public boolean isTransparent() { return isTransparent; }

    /** 要素是否透明渲染 */
    public void setTransparent(boolean isTransparent) {
        this.isTransparent = isTransparent;
    }

    /** 字体大小 */
    public int getFontSize() { return fontSize; }

    /** 字体大小 */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /** 图标 ID */
    public int getIcon() { return icon; }

    /** 图标 ID */
    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementProperties that = (ElementProperties) o;
        return clampToGround == that.clampToGround && is3d == that.is3d
                && isTransparent == that.isTransparent && fontSize == that.fontSize
                && icon == that.icon && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() { return Objects.hash(color, clampToGround, is3d, isTransparent, fontSize, icon); }

    @Override
    public String toString() {
        return "ElementProperties{color='" + color + "', icon=" + icon + ", is3d=" + is3d + '}';
    }
}
