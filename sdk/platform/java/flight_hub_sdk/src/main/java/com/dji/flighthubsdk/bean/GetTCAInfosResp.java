// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取TCAInfos响应。 */
public final class GetTCAInfosResp {
    /** TCAS 信息列表 */
    private List<TCAInfo> tcas;
    public GetTCAInfosResp() {
    }

/**
     * 构造 GetTCAInfosResp。
     *
     * @param tcas TCA 信息列表
     */
    public GetTCAInfosResp(List<TCAInfo> tcas) {
        this.tcas = tcas != null ? tcas : Collections.emptyList();
    }

    /** TCAS 信息列表 */
    public List<TCAInfo> getTcas() {
        return tcas;
    }

    /** @param tcas TCAS 信息列表 */
    public void setTcas(List<TCAInfo> tcas) {
        this.tcas = tcas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetTCAInfosResp that = (GetTCAInfosResp) o;
        return Objects.equals(tcas, that.tcas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tcas);
    }

    @Override
    public String toString() {
        return "GetTCAInfosResp{tcas=" + tcas + '}';
    }
}
