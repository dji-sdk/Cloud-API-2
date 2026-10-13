// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** AI 模型配置参数 */
public class AiModelsConfigParams {
    /** AI 模型配置列表 */
    private List<AiModelsConfigSet> aiModelsConfig = new ArrayList<>();

    public AiModelsConfigParams() {
    }

    /** AI 模型配置列表 */
    public List<AiModelsConfigSet> getAiModelsConfig() { return aiModelsConfig; }
    /** @param aiModelsConfig AI 模型配置列表 */
    public void setAiModelsConfig(List<AiModelsConfigSet> aiModelsConfig) {
        this.aiModelsConfig = aiModelsConfig != null ? aiModelsConfig : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiModelsConfigParams that = (AiModelsConfigParams) o;
        return Objects.equals(aiModelsConfig, that.aiModelsConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aiModelsConfig);
    }
}
