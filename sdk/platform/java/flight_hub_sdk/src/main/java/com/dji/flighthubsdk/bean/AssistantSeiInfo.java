// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Assistant SEI 信息。 */
public final class AssistantSeiInfo {

    private boolean autoDirection;
    /** 朝向值 */
    private int direction;
    public AssistantSeiInfo() {
    }

/**
     * 构造 AssistantSeiInfo。
     *
     * @param autoDirection 是否自动方向
     * @param direction 方向
     */
    public AssistantSeiInfo(boolean autoDirection, int direction) {
        this.autoDirection = autoDirection;
        this.direction = direction;
    }

    /** 是否自动方向 */
    public boolean isAutoDirection() {
        return autoDirection;
    }

    /** 是否自动方向 */
    public void setAutoDirection(boolean autoDirection) {
        this.autoDirection = autoDirection;
    }

    /** 朝向值 */
    public int getDirection() {
        return direction;
    }

    /** @param direction 朝向值 */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssistantSeiInfo that = (AssistantSeiInfo) o;
        return autoDirection == that.autoDirection && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoDirection, direction);
    }

    @Override
    public String toString() {
        return "AssistantSeiInfo{autoDirection=" + autoDirection + ", direction=" + direction + '}';
    }
}
