// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** HTTP flash-configs 配置项 */
public class FlashLedConfig {
    private String id = "";
    private String name = "";
    private List<FlashLedSegment> flashSegments = new ArrayList<>();

    public FlashLedConfig() {
    }

    /** 模板 UUID */
    public String getId() { return id; }
    /** @param id 模板 UUID */
    public void setId(String id) { this.id = id != null ? id : ""; }
    /** 配置名称 */
    public String getName() { return name; }
    /** @param name 配置名称 */
    public void setName(String name) { this.name = name != null ? name : ""; }
    /** 爆闪片段列表 */
    public List<FlashLedSegment> getFlashSegments() { return flashSegments; }
    /** @param flashSegments 爆闪片段列表 */
    public void setFlashSegments(List<FlashLedSegment> flashSegments) {
        this.flashSegments = flashSegments != null ? flashSegments : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlashLedConfig that = (FlashLedConfig) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(flashSegments, that.flashSegments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, flashSegments);
    }

    @Override
    public String toString() {
        return "FlashLedConfig{id='" + id + '\'' + ", name='" + name + '\''
                + ", flashSegments=" + flashSegments + '}';
    }
}
