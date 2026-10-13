// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** AI 模型配置项 */
public class AiModelsConfigSet {
    /** 码流类型, 0: 融合流, 1: 广角, 2: 变焦, 3: 红外 */
    private int streamType = 0;
    /** 模型索引列表, DJI官方: 0, 三方: other */
    private List<Integer> models = new ArrayList<>();

    public AiModelsConfigSet() {
    }

    /** 码流类型, 0: 融合流, 1: 广角, 2: 变焦, 3: 红外 */
    public int getStreamType() { return streamType; }
    /** @param streamType 码流类型, 0: 融合流, 1: 广角, 2: 变焦, 3: 红外 */
    public void setStreamType(int streamType) { this.streamType = streamType; }
    /** 模型索引列表, DJI官方: 0, 三方: other */
    public List<Integer> getModels() { return models; }
    /** @param models 模型索引列表, DJI官方: 0, 三方: other */
    public void setModels(List<Integer> models) { this.models = models != null ? models : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiModelsConfigSet that = (AiModelsConfigSet) o;
        return streamType == that.streamType && Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamType, models);
    }
}
