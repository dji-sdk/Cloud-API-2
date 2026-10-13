// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 一站式算法文件上传结果 */
public class UploadAIInsideAlgorithmFileResult {
    /** MinIO 对象存储路径（上传后生成） */
    private String objectKey = "";
    /** MinIO 对象访问 URL */
    private String objectUrl = "";
    /** 文件 ETag（上传后生成） */
    private String etag = "";
    /** 算法入库后的完整详情（由 file-upload 接口返回） */
    private AIInsideAlgorithmDetail detail;

    public UploadAIInsideAlgorithmFileResult() {}

    /** MinIO 对象存储路径（上传后生成） */
    public String getObjectKey() { return objectKey; }

    /** @param v MinIO 对象存储路径（上传后生成） */
    public void setObjectKey(String v) { this.objectKey = v != null ? v : ""; }

    /** MinIO 对象访问 URL */
    public String getObjectUrl() { return objectUrl; }

    /** @param v MinIO 对象访问 URL */
    public void setObjectUrl(String v) { this.objectUrl = v != null ? v : ""; }

    /** 文件 ETag（上传后生成） */
    public String getEtag() { return etag; }

    /** @param v 文件 ETag（上传后生成） */
    public void setEtag(String v) { this.etag = v != null ? v : ""; }

    /** 算法入库后的完整详情（由 file-upload 接口返回） */
    public AIInsideAlgorithmDetail getDetail() { return detail; }

    /** @param v 算法入库后的完整详情（由 file-upload 接口返回） */
    public void setDetail(AIInsideAlgorithmDetail v) { this.detail = v; }
}
