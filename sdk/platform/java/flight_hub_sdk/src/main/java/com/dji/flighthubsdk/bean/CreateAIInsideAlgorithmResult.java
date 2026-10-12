// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 创建算法上传凭证结果 */
public class CreateAIInsideAlgorithmResult {
    /** MinIO 临时上传凭证 */
    private AIInsideCredentials credentials;
    /** 存储提供商标识（如 "minio"） */
    private String provider = "";
    /** 存储 Bucket 名称 */
    private String bucket = "";
    /** 存储区域 */
    private String region = "";
    /** 存储访问端点（含协议头，如 "https://minio.example.com"） */
    private String endpoint = "";
    /** 文件存储目录前缀，上传时使用此路径拼接文件名 */
    private String fileStoreDir = "";

    public CreateAIInsideAlgorithmResult() {}

    /** MinIO 临时上传凭证 */
    public AIInsideCredentials getCredentials() { return credentials; }

    /** @param v MinIO 临时上传凭证 */
    public void setCredentials(AIInsideCredentials v) { this.credentials = v; }

    /** 存储提供商标识（如 "minio"） */
    public String getProvider() { return provider; }

    /** @param v 存储提供商标识（如 "minio"） */
    public void setProvider(String v) { this.provider = v != null ? v : ""; }

    /** 存储 Bucket 名称 */
    public String getBucket() { return bucket; }

    /** @param v 存储 Bucket 名称 */
    public void setBucket(String v) { this.bucket = v != null ? v : ""; }

    /** 存储区域 */
    public String getRegion() { return region; }

    /** @param v 存储区域 */
    public void setRegion(String v) { this.region = v != null ? v : ""; }

    /** 存储访问端点（含协议头，如 "https://minio.example.com"） */
    public String getEndpoint() { return endpoint; }

    /** @param endpoint 存储访问端点（含协议头，如 "https://minio.example.com"） */
    public void setEndpoint(String v) { this.endpoint = v != null ? v : ""; }

    /** 文件存储目录前缀，上传时使用此路径拼接文件名 */
    public String getFileStoreDir() { return fileStoreDir; }

    /** @param v 文件存储目录前缀，上传时使用此路径拼接文件名 */
    public void setFileStoreDir(String v) { this.fileStoreDir = v != null ? v : ""; }
}
