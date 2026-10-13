// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 删除算法请求参数 */
public class DeleteAIInsideAlgorithmParam {
    /** 要删除的算法签名 ID（必填） */
    private String signedId = "";

    public DeleteAIInsideAlgorithmParam() {}

    public DeleteAIInsideAlgorithmParam(String signedId) {
        this.signedId = signedId != null ? signedId : "";
    }

    /** 要删除的算法签名 ID（必填） */
    public String getSignedId() { return signedId; }

    /** @param v 要删除的算法签名 ID（必填） */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }
}
