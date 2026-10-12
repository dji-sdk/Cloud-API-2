// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 单路载荷直播画面世界区域。
 * <p>矩形坐标见 {@link #getWorldRegion()}（{@link CameraLiveviewWorldRegion}）。</p>
 */
public final class LiveviewWorldRegion {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private CameraLiveviewWorldRegion worldRegion;
    public LiveviewWorldRegion() {
    }

/**
     * 构造 LiveviewWorldRegion。
     *
     * @param payloadIndex 负载索引
     * @param worldRegion 世界区域坐标
     */
    public LiveviewWorldRegion(String payloadIndex, CameraLiveviewWorldRegion worldRegion) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.worldRegion = worldRegion != null ? worldRegion : new CameraLiveviewWorldRegion(0, 0, 0, 0);
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** 世界区域坐标 */
    public CameraLiveviewWorldRegion getWorldRegion() { return worldRegion; }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 世界区域坐标 */
    public void setWorldRegion(CameraLiveviewWorldRegion worldRegion) {
        this.worldRegion = worldRegion;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveviewWorldRegion that = (LiveviewWorldRegion) o;
        return Objects.equals(payloadIndex, that.payloadIndex) && Objects.equals(worldRegion, that.worldRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, worldRegion);
    }

    @Override
    public String toString() {
        return "LiveviewWorldRegion{payloadIndex='" + payloadIndex + '\'' + ", worldRegion=" + worldRegion + '}';
    }
}
