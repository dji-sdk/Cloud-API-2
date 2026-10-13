// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 获取航线编辑器 STS 凭证结果 */
public final class GetWaylineEditorStsResult {

    /** STS 临时凭证 */
    private WaylineEditorStsCredentials credentials;
    /** 对象存储路径前缀（对应服务端 object_key_prefix） */
    private String objectKeyPrefix;
    /** 存储桶名称 */
    private String bucket;
    /** 存储服务端点 */
    private String endpoint;
    /** 存储提供商（minio/aws/ali 等） */
    private String provider;
    /** 存储区域 */
    private String region;

    public GetWaylineEditorStsResult() {
        this.credentials = new WaylineEditorStsCredentials();
        this.objectKeyPrefix = "";
        this.bucket = "";
        this.endpoint = "";
        this.provider = "";
        this.region = "";
    }

    /**
     * 构造 GetWaylineEditorStsResult。
     *
     * @param credentials     STS 临时凭证
     * @param objectKeyPrefix 对象存储路径前缀
     * @param bucket          存储桶名称
     * @param endpoint        存储服务端点
     * @param provider        存储提供商（minio/aws/ali 等）
     * @param region          存储区域
     */
    public GetWaylineEditorStsResult(WaylineEditorStsCredentials credentials,
            String objectKeyPrefix, String bucket, String endpoint,
            String provider, String region) {
        this.credentials = credentials != null ? credentials : new WaylineEditorStsCredentials();
        this.objectKeyPrefix = objectKeyPrefix != null ? objectKeyPrefix : "";
        this.bucket = bucket != null ? bucket : "";
        this.endpoint = endpoint != null ? endpoint : "";
        this.provider = provider != null ? provider : "";
        this.region = region != null ? region : "";
    }

    /** STS 临时凭证 */
    public WaylineEditorStsCredentials getCredentials() { return credentials; }

    /** STS 临时凭证 */
    public void setCredentials(WaylineEditorStsCredentials credentials) {
        this.credentials = credentials;
    }

    /** 对象存储路径前缀（对应服务端 object_key_prefix） */
    public String getObjectKeyPrefix() { return objectKeyPrefix; }

    /** @param objectKeyPrefix 对象存储路径前缀（对应服务端 object_key_prefix） */
    public void setObjectKeyPrefix(String objectKeyPrefix) {
        this.objectKeyPrefix = objectKeyPrefix != null ? objectKeyPrefix : "";
    }

    /**
     * 对象存储路径前缀。
     *
     * @deprecated 请使用 {@link #getObjectKeyPrefix()}
     */
    @Deprecated
    public String getObjectDir() { return objectKeyPrefix; }

    /**
     * 对象存储路径前缀。
     *
     * @deprecated 请使用 {@link #setObjectKeyPrefix(String)}
     */
    @Deprecated
    public void setObjectDir(String objectDir) {
        this.objectKeyPrefix = objectDir != null ? objectDir : "";
    }

    /** 存储桶名称 */
    public String getBucket() { return bucket; }

    /** 存储桶名称 */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /** 存储服务端点 */
    public String getEndpoint() { return endpoint; }

    /** 存储服务端点 */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /** 存储提供商（minio/aws/ali 等） */
    public String getProvider() { return provider; }

    /** @param provider 存储提供商（minio/aws/ali 等） */
    public void setProvider(String provider) {
        this.provider = provider != null ? provider : "";
    }

    /** 存储区域 */
    public String getRegion() { return region; }

    /** 存储区域 */
    public void setRegion(String region) {
        this.region = region != null ? region : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetWaylineEditorStsResult that = (GetWaylineEditorStsResult) o;
        return Objects.equals(credentials, that.credentials)
                && Objects.equals(objectKeyPrefix, that.objectKeyPrefix)
                && Objects.equals(bucket, that.bucket)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(provider, that.provider)
                && Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentials, objectKeyPrefix, bucket, endpoint, provider, region);
    }

    @Override
    public String toString() {
        return "GetWaylineEditorStsResult{"
                + "bucket='" + bucket + '\''
                + ", endpoint='" + endpoint + '\''
                + ", objectKeyPrefix='" + objectKeyPrefix + '\''
                + ", provider='" + provider + '\''
                + ", region='" + region + '\''
                + '}';
    }
}
