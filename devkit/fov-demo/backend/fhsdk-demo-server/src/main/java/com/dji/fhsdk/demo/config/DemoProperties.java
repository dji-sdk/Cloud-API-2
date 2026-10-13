// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private int stickDefaultIntervalMs = 50;
    /** 飞机直播 payload_index，见 application.yml demo.drone-payload-index */
    private String dronePayloadIndex;

    public int getStickDefaultIntervalMs() { return stickDefaultIntervalMs; }
    public void setStickDefaultIntervalMs(int stickDefaultIntervalMs) { this.stickDefaultIntervalMs = stickDefaultIntervalMs; }
    public String getDronePayloadIndex() { return dronePayloadIndex; }
    public void setDronePayloadIndex(String dronePayloadIndex) { this.dronePayloadIndex = dronePayloadIndex; }
}
