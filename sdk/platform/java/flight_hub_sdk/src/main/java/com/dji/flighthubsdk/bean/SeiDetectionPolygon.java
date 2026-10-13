// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** SEI 多边形检测区域 */
public class SeiDetectionPolygon {
    /** 目标 ID */
    private int id;
    /** 目标类型 */
    private int type;
    /** 目标状态 */
    private int state;
    /** 置信度（0-100） */
    private int confidence;
    /** 多边形顶点列表 */
    private List<SeiPoint2D> points;

    /**
     * 构造 SeiDetectionPolygon。
     */
    public SeiDetectionPolygon() {
        this.points = new ArrayList<>();
    }

    /**
     * 构造 SeiDetectionPolygon。
     *
     * @param id 目标 ID
     * @param type 目标类型
     * @param state 目标状态
     * @param confidence 置信度（0-100）
     * @param points 多边形顶点列表
     */
    public SeiDetectionPolygon(int id, int type, int state, int confidence, List<SeiPoint2D> points) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.confidence = confidence;
        this.points = points != null ? points : new ArrayList<>();
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
    /** 置信度（0-100） */
    public int getConfidence() { return confidence; }
    /** @param confidence 置信度（0-100） */
    public void setConfidence(int confidence) { this.confidence = confidence; }
    /** 多边形顶点列表 */
    public List<SeiPoint2D> getPoints() { return points; }
    /** @param points 多边形顶点列表 */
    public void setPoints(List<SeiPoint2D> points) { this.points = points != null ? points : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiDetectionPolygon that = (SeiDetectionPolygon) o;
        return id == that.id
                && type == that.type
                && state == that.state
                && confidence == that.confidence
                && Objects.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                type,
                state,
                confidence,
                points);
    }

    @Override
    public String toString() {
        return "SeiDetectionPolygon{"
                + "id=" + id
                + ", type=" + type
                + ", state=" + state
                + ", confidence=" + confidence
                + ", points=" + points
                + '}';
    }
}
