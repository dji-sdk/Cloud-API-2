// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设置障碍物避障结果。 */
public final class SetObstacleAvoidanceResult {
    private String message;
    public SetObstacleAvoidanceResult() {
    }

/**
     * 构造 SetObstacleAvoidanceResult。
     *
     * @param message 消息
     */
    public SetObstacleAvoidanceResult(String message) {
        this.message = message != null ? message : "";
    }

    /** 消息 */
    public String getMessage() { return message; }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetObstacleAvoidanceResult that = (SetObstacleAvoidanceResult) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "SetObstacleAvoidanceResult{message='" + message + '\'' + '}';
    }
}
