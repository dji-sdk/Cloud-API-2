// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * AirSense 民航飞机告警数据。
 */
public final class AirsenseWarning {

    /** 周边民航飞机告警数组（可能为空）。 */
    private List<AirsenseWarningItem> data;

    public AirsenseWarning() {
    }

    /**
     * 构造 AirsenseWarning。
     *
     * @param data 民航飞机告警数组
     */
    public AirsenseWarning(List<AirsenseWarningItem> data) {
        this.data = data != null ? data : Collections.emptyList();
    }

    /** 周边民航飞机告警数组 */
    public List<AirsenseWarningItem> getData() {
        return data;
    }

    /** 周边民航飞机告警数组 */
    public void setData(List<AirsenseWarningItem> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AirsenseWarning that = (AirsenseWarning) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "AirsenseWarning{data=" + data + '}';
    }
}
