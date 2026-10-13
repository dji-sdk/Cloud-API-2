// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 航线详情 - 测区范围 */
public final class WaylineDetailSurveyAreaScope {

    private List<WaylineDetailSurveyAreaScopePoint> points;
    public WaylineDetailSurveyAreaScope() {
    }

/**
     * 构造 WaylineDetailSurveyAreaScope。
     *
     * @param points 坐标点列表
     */
    public WaylineDetailSurveyAreaScope(List<WaylineDetailSurveyAreaScopePoint> points) {
        this.points = points != null ? points : Collections.emptyList();
    }

    /** 坐标点列表 */
    public List<WaylineDetailSurveyAreaScopePoint> getPoints() {
        return points;
    }

    /** 坐标点列表 */
    public void setPoints(List<WaylineDetailSurveyAreaScopePoint> points) {
        this.points = points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineDetailSurveyAreaScope that = (WaylineDetailSurveyAreaScope) o;
        return Objects.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return "WaylineDetailSurveyAreaScope{points=" + points + '}';
    }
}
