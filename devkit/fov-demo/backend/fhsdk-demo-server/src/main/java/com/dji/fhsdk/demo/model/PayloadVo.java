// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

public class PayloadVo {

    private String payloadIndex;
    private String deviceSn;
    private String videoId;

    public PayloadVo() {
    }

    public PayloadVo(String payloadIndex, String deviceSn, String videoId) {
        this.payloadIndex = payloadIndex;
        this.deviceSn = deviceSn;
        this.videoId = videoId;
    }

    public String getPayloadIndex() { return payloadIndex; }
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex; }
    public String getDeviceSn() { return deviceSn; }
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
}
