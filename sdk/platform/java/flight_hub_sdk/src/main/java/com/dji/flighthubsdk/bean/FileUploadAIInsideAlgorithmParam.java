// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 处理已上传文件请求参数 */
public class FileUploadAIInsideAlgorithmParam {
    /** 上传完成后的 MinIO 对象 Key（由 createAIInsideAlgorithm 接口返回的路径拼接文件名）（必填） */
    private String fileKey = "";

    public FileUploadAIInsideAlgorithmParam() {}

    public FileUploadAIInsideAlgorithmParam(String fileKey) {
        this.fileKey = fileKey != null ? fileKey : "";
    }

    /** 上传完成后的 MinIO 对象 Key（由 createAIInsideAlgorithm 接口返回的路径拼接文件名）（必填） */
    public String getFileKey() { return fileKey; }

    /** @param v 上传完成后的 MinIO 对象 Key（由 createAIInsideAlgorithm 接口返回的路径拼接文件名）（必填） */
    public void setFileKey(String v) { this.fileKey = v != null ? v : ""; }
}
