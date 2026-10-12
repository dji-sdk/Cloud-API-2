// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 处理已上传文件结果 */
public class FileUploadAIInsideAlgorithmResult {
    /** 算法入库后的完整详情 */
    private AIInsideAlgorithmDetail detail;

    public FileUploadAIInsideAlgorithmResult() {}

    /** 算法入库后的完整详情 */
    public AIInsideAlgorithmDetail getDetail() { return detail; }

    /** @param v 算法入库后的完整详情 */
    public void setDetail(AIInsideAlgorithmDetail v) { this.detail = v; }
}
