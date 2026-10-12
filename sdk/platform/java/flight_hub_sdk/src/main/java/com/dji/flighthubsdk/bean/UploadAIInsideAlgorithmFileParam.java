// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 一站式算法文件上传请求参数 */
public class UploadAIInsideAlgorithmFileParam {
    /** 本地算法 ZIP 文件的绝对路径（必填） */
    private String filePath = "";

    public UploadAIInsideAlgorithmFileParam() {}
    public UploadAIInsideAlgorithmFileParam(String filePath) {
        this.filePath = filePath != null ? filePath : "";
    }

    /** 本地算法 ZIP 文件的绝对路径（必填） */
    public String getFilePath() { return filePath; }

    /** @param v 本地算法 ZIP 文件的绝对路径（必填） */
    public void setFilePath(String v) { this.filePath = v != null ? v : ""; }
}
