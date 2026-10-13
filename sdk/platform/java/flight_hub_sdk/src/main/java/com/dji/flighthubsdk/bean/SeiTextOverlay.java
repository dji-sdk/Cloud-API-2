// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** SEI 文本叠加 */
public class SeiTextOverlay {
    /** 叠加项 ID */
    private int id;
    /** 文本位置 x，单位：像素 */
    private int x;
    /** 文本位置 y，单位：像素 */
    private int y;
    /** 字体大小 */
    private int fontSize = 12;
    /** 颜色红色分量（0-255） */
    private int colorR = 255;
    /** 颜色绿色分量（0-255） */
    private int colorG = 255;
    /** 颜色蓝色分量（0-255） */
    private int colorB = 255;
    /** 透明度（0-255，255 为完全不透明） */
    private int alpha = 255;
    /** 文本内容 */
    private String text = "";

    /**
     * 构造 SeiTextOverlay。
     */
    public SeiTextOverlay() {
    }

/**
     * 构造 SeiTextOverlay。
     *
     * @param id 叠加项 ID
     * @param x 文本位置 x，单位：像素
     * @param y 文本位置 y，单位：像素
     * @param fontSize 字体大小
     * @param colorR 颜色红色分量（0-255）
     * @param colorG 颜色绿色分量（0-255）
     * @param colorB 颜色蓝色分量（0-255）
     * @param alpha 透明度（0-255，255 为完全不透明）
     * @param text 文本内容
     */
    public SeiTextOverlay(int id, int x, int y, int fontSize, int colorR, int colorG, int colorB, int alpha, String text) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.alpha = alpha;
        this.text = text != null ? text : "";
    }

    /** 叠加项 ID */
    public int getId() { return id; }
    /** @param id 叠加项 ID */
    public void setId(int id) { this.id = id; }
    /** 文本位置 x，单位：像素 */
    public int getX() { return x; }
    /** @param x 文本位置 x，单位：像素 */
    public void setX(int x) { this.x = x; }
    /** 文本位置 y，单位：像素 */
    public int getY() { return y; }
    /** @param y 文本位置 y，单位：像素 */
    public void setY(int y) { this.y = y; }
    /** 字体大小 */
    public int getFontSize() { return fontSize; }
    /** @param fontSize 字体大小 */
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }
    /** 颜色红色分量（0-255） */
    public int getColorR() { return colorR; }
    /** @param colorR 颜色红色分量（0-255） */
    public void setColorR(int colorR) { this.colorR = colorR; }
    /** 颜色绿色分量（0-255） */
    public int getColorG() { return colorG; }
    /** @param colorG 颜色绿色分量（0-255） */
    public void setColorG(int colorG) { this.colorG = colorG; }
    /** 颜色蓝色分量（0-255） */
    public int getColorB() { return colorB; }
    /** @param colorB 颜色蓝色分量（0-255） */
    public void setColorB(int colorB) { this.colorB = colorB; }
    /** 透明度（0-255，255 为完全不透明） */
    public int getAlpha() { return alpha; }
    /** @param alpha 透明度（0-255，255 为完全不透明） */
    public void setAlpha(int alpha) { this.alpha = alpha; }
    /** 文本内容 */
    public String getText() { return text; }
    /** @param text 文本内容 */
    public void setText(String text) { this.text = text != null ? text : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiTextOverlay that = (SeiTextOverlay) o;
        return id == that.id
                && x == that.x
                && y == that.y
                && fontSize == that.fontSize
                && colorR == that.colorR
                && colorG == that.colorG
                && colorB == that.colorB
                && alpha == that.alpha
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                x,
                y,
                fontSize,
                colorR,
                colorG,
                colorB,
                alpha,
                text);
    }

    @Override
    public String toString() {
        return "SeiTextOverlay{"
                + "id=" + id
                + ", x=" + x
                + ", y=" + y
                + ", fontSize=" + fontSize
                + ", colorR=" + colorR
                + ", colorG=" + colorG
                + ", colorB=" + colorB
                + ", alpha=" + alpha
                + ", text='" + text + '\''
                + '}';
    }
}
