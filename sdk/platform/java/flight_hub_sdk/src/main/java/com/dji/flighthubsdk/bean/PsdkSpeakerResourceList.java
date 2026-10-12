// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** PSDK 喊话器资源列表 */
public final class PsdkSpeakerResourceList {
    private List<PsdkSpeakerResource> resources;
    public PsdkSpeakerResourceList() {
    }

/**
     * 构造 PsdkSpeakerResourceList。
     *
     * @param resources 资源列表
     */
    public PsdkSpeakerResourceList(List<PsdkSpeakerResource> resources) {
        this.resources = resources != null ? resources : Collections.emptyList();
    }

    /** 资源列表 */
    public List<PsdkSpeakerResource> getResources() {
        return resources;
    }

    /** 资源列表 */
    public void setResources(List<PsdkSpeakerResource> resources) {
        this.resources = resources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsdkSpeakerResourceList that = (PsdkSpeakerResourceList) o;
        return Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resources);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerResourceList{resources=" + resources + '}';
    }
}
