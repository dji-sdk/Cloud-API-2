// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 飞机 4G Dongle 信息推送（dongle_infos）。EA340 消息推送补全（Phase D）。 */
public final class DroneDongleInfos {

    private List<DongleInfoItem> dongleInfos;

    public DroneDongleInfos() {
        this.dongleInfos = Collections.emptyList();
    }

    /**
     * 构造 DroneDongleInfos。
     *
     * @param dongleInfos Dongle 信息列表
     */
    public DroneDongleInfos(List<DongleInfoItem> dongleInfos) {
        this.dongleInfos = dongleInfos != null ? dongleInfos : Collections.emptyList();
    }

    /** Dongle 信息列表 */
    public List<DongleInfoItem> getDongleInfos() {
        return dongleInfos;
    }

    /** Dongle 信息列表 */
    public void setDongleInfos(List<DongleInfoItem> dongleInfos) {
        this.dongleInfos = dongleInfos != null ? dongleInfos : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneDongleInfos that = (DroneDongleInfos) o;
        return Objects.equals(dongleInfos, that.dongleInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dongleInfos);
    }

    @Override
    public String toString() {
        return "DroneDongleInfos{dongleInfos=" + dongleInfos + '}';
    }
}
