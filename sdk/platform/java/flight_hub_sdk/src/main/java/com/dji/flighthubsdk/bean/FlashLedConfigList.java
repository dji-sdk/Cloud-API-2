// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** HTTP flash-configs 配置列表 */
public final class FlashLedConfigList {
    private List<FlashLedConfig> configs = new ArrayList<>();

    public FlashLedConfigList() {
    }

    /**
     * 构造 FlashLedConfigList。
     *
     * @param configs 配置列表
     */
    public FlashLedConfigList(List<FlashLedConfig> configs) {
        this.configs = configs != null ? configs : Collections.emptyList();
    }

    /** 配置列表 */
    public List<FlashLedConfig> getConfigs() {
        return configs;
    }

    /** 配置列表 */
    public void setConfigs(List<FlashLedConfig> configs) {
        this.configs = configs != null ? configs : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlashLedConfigList that = (FlashLedConfigList) o;
        return Objects.equals(configs, that.configs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configs);
    }

    @Override
    public String toString() {
        return "FlashLedConfigList{configs=" + configs + '}';
    }
}
