// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

public class StreamUrlResponse {

    private String whepUrl;
    private String streamKey;
    private String payloadIndex;

    public StreamUrlResponse() {}

    public StreamUrlResponse(String whepUrl, String streamKey, String payloadIndex) {
        this.whepUrl = whepUrl;
        this.streamKey = streamKey;
        this.payloadIndex = payloadIndex;
    }

    public String getWhepUrl() { return whepUrl; }
    public void setWhepUrl(String whepUrl) { this.whepUrl = whepUrl; }
    public String getStreamKey() { return streamKey; }
    public void setStreamKey(String streamKey) { this.streamKey = streamKey; }
    public String getPayloadIndex() { return payloadIndex; }
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex; }
}
