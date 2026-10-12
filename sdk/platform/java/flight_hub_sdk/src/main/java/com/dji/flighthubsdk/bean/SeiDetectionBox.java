// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** SEI 目标检测框 */
public class SeiDetectionBox {
    /** 目标 ID */
    private int id;
    /** 目标类型 */
    private int type;
    /** 目标状态 */
    private int state;
    /** 检测框左上角 x 坐标，单位：像素 */
    private int x;
    /** 检测框左上角 y 坐标，单位：像素 */
    private int y;
    /** 检测框宽度，单位：像素 */
    private int width;
    /** 检测框高度，单位：像素 */
    private int height;
    /** 置信度（0-100） */
    private int confidence;

    /**
     * 构造 SeiDetectionBox。
     */
    public SeiDetectionBox() {
    }

/**
     * 构造 SeiDetectionBox。
     *
     * @param id 目标 ID
     * @param type 目标类型
     * @param state 目标状态
     * @param x 检测框左上角 x 坐标，单位：像素
     * @param y 检测框左上角 y 坐标，单位：像素
     * @param width 检测框宽度，单位：像素
     * @param height 检测框高度，单位：像素
     * @param confidence 置信度（0-100）
     */
    public SeiDetectionBox(int id, int type, int state, int x, int y, int width, int height, int confidence) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.confidence = confidence;
    }

    /** 目标 ID */
    public int getId() { return id; }
    /** @param id 目标 ID */
    public void setId(int id) { this.id = id; }
    /** 目标类型 */
    public int getType() { return type; }
    /** @param type 目标类型 */
    public void setType(int type) { this.type = type; }
    /** 目标状态 */
    public int getState() { return state; }
    /** @param state 目标状态 */
    public void setState(int state) { this.state = state; }
    /** 检测框左上角 x 坐标，单位：像素 */
    public int getX() { return x; }
    /** @param x 检测框左上角 x 坐标，单位：像素 */
    public void setX(int x) { this.x = x; }
    /** 检测框左上角 y 坐标，单位：像素 */
    public int getY() { return y; }
    /** @param y 检测框左上角 y 坐标，单位：像素 */
    public void setY(int y) { this.y = y; }
    /** 检测框宽度，单位：像素 */
    public int getWidth() { return width; }
    /** @param width 检测框宽度，单位：像素 */
    public void setWidth(int width) { this.width = width; }
    /** 检测框高度，单位：像素 */
    public int getHeight() { return height; }
    /** @param height 检测框高度，单位：像素 */
    public void setHeight(int height) { this.height = height; }
    /** 置信度（0-100） */
    public int getConfidence() { return confidence; }
    /** @param confidence 置信度（0-100） */
    public void setConfidence(int confidence) { this.confidence = confidence; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiDetectionBox that = (SeiDetectionBox) o;
        return id == that.id
                && type == that.type
                && state == that.state
                && x == that.x
                && y == that.y
                && width == that.width
                && height == that.height
                && confidence == that.confidence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                type,
                state,
                x,
                y,
                width,
                height,
                confidence);
    }

    @Override
    public String toString() {
        return "SeiDetectionBox{"
                + "id=" + id
                + ", type=" + type
                + ", state=" + state
                + ", x=" + x
                + ", y=" + y
                + ", width=" + width
                + ", height=" + height
                + ", confidence=" + confidence
                + '}';
    }
}
