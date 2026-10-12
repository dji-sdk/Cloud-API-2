// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 创建算法上传凭证请求参数 */
public class CreateAIInsideAlgorithmParam {
    /** 上传的文件名（如 "fire_detection_v2.zip"）（必填） */
    private String name = "";

    public CreateAIInsideAlgorithmParam() {}

    public CreateAIInsideAlgorithmParam(String name) {
        this.name = name != null ? name : "";
    }

    /** 上传的文件名（如 "fire_detection_v2.zip"）（必填） */
    public String getName() { return name; }

    /** @param name 上传的文件名（如 "fire_detection_v2.zip"）（必填） */
    public void setName(String v) { this.name = v != null ? v : ""; }
}
