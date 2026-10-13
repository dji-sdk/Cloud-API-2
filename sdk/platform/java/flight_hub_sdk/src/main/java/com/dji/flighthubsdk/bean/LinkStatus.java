// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.DroneLinkState;

import java.util.Objects;
/**
 * 飞机链路状态聚合。
 */
public final class LinkStatus {

    /** 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    private DroneLinkState ctrlLinkStatus;
    /** 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    private DroneLinkState dataLinkStatus;
    /** 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    private DroneLinkState liveLinkStatus;
    public LinkStatus() {
    }

/**
     * 构造 LinkStatus。
     *
     * @param ctrlLinkStatus 控制链路状态
     * @param dataLinkStatus 数据链路状态
     * @param liveLinkStatus 直播链路状态
     */
    public LinkStatus(DroneLinkState ctrlLinkStatus, DroneLinkState dataLinkStatus, DroneLinkState liveLinkStatus) {
        this.ctrlLinkStatus = ctrlLinkStatus != null ? ctrlLinkStatus : DroneLinkState.UNKNOWN;
        this.dataLinkStatus = dataLinkStatus != null ? dataLinkStatus : DroneLinkState.UNKNOWN;
        this.liveLinkStatus = liveLinkStatus != null ? liveLinkStatus : DroneLinkState.UNKNOWN;
    }

    /** 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    public DroneLinkState getCtrlLinkStatus() { return ctrlLinkStatus; }
    /** 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    public DroneLinkState getDataLinkStatus() { return dataLinkStatus; }
    /** 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    public DroneLinkState getLiveLinkStatus() { return liveLinkStatus; }

    /** @param ctrlLinkStatus 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    public void setCtrlLinkStatus(DroneLinkState ctrlLinkStatus) {
        this.ctrlLinkStatus = ctrlLinkStatus;
    }

    /** @param dataLinkStatus 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    public void setDataLinkStatus(DroneLinkState dataLinkStatus) {
        this.dataLinkStatus = dataLinkStatus;
    }

    /** @param liveLinkStatus 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`） */
    public void setLiveLinkStatus(DroneLinkState liveLinkStatus) {
        this.liveLinkStatus = liveLinkStatus;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkStatus linkStatus = (LinkStatus) o;
        return ctrlLinkStatus == linkStatus.ctrlLinkStatus
                && dataLinkStatus == linkStatus.dataLinkStatus
                && liveLinkStatus == linkStatus.liveLinkStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ctrlLinkStatus, dataLinkStatus, liveLinkStatus);
    }

    @Override
    public String toString() {
        return "LinkStatus{ctrl=" + ctrlLinkStatus + ", data=" + dataLinkStatus + ", live=" + liveLinkStatus + '}';
    }
}
