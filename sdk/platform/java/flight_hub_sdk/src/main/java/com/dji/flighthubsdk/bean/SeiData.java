// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** SEI 自定义数据（透传字节流） */
public class SeiData {
    /** 自定义字节数据 */
    private List<Integer> data;

    /**
     * 构造 SeiData。
     */
    public SeiData() {
        this.data = new ArrayList<>();
    }

    /**
     * 构造 SeiData。
     *
     * @param data 自定义字节数据
     */
    public SeiData(List<Integer> data) {
        this.data = data != null ? data : new ArrayList<>();
    }

    /** 自定义字节数据 */
    public List<Integer> getData() { return data; }
    /** @param data 自定义字节数据 */
    public void setData(List<Integer> data) { this.data = data != null ? data : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiData that = (SeiData) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "SeiData{data=" + data + '}';
    }
}
