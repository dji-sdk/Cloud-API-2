// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 获取媒体上传 STS 凭证的返回结果。
 */
public final class GetMediaUploadStsResult {
    private String bucket;
    private MediaUploadStsCredentials credentials;
    private String objectKeyPrefix;
    private String endpoint;
    private String provider;
    private String region;
    public GetMediaUploadStsResult() {
    }

/**
     * 构造 GetMediaUploadStsResult。
     *
     * @param bucket 存储桶
     * @param credentials STS 临时凭证
     * @param objectKeyPrefix 上传路径前缀
     * @param endpoint 访问端点
     * @param provider 存储提供商
     * @param region 区域
     */
    public GetMediaUploadStsResult(
            String bucket,
            MediaUploadStsCredentials credentials,
            String objectKeyPrefix,
            String endpoint,
            String provider,
            String region) {
        this.bucket = bucket != null ? bucket : "";
        this.credentials = credentials;
        this.objectKeyPrefix = objectKeyPrefix != null ? objectKeyPrefix : "";
        this.endpoint = endpoint != null ? endpoint : "";
        this.provider = provider != null ? provider : "";
        this.region = region != null ? region : "";
    }

    /** 存储桶 */
    public String getBucket() { return bucket; }

    /** STS 临时凭证 */
    public MediaUploadStsCredentials getCredentials() { return credentials; }

    /** 上传路径前缀 */
    public String getObjectKeyPrefix() { return objectKeyPrefix; }

    /** 访问端点 */
    public String getEndpoint() { return endpoint; }

    /** 存储提供商 */
    public String getProvider() { return provider; }

    /** 区域 */
    public String getRegion() { return region; }

    /** 存储桶 */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /** STS 临时凭证 */
    public void setCredentials(MediaUploadStsCredentials credentials) {
        this.credentials = credentials;
    }

    /** 上传路径前缀 */
    public void setObjectKeyPrefix(String objectKeyPrefix) {
        this.objectKeyPrefix = objectKeyPrefix;
    }

    /** 访问端点 */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /** 存储提供商 */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /** 区域 */
    public void setRegion(String region) {
        this.region = region;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetMediaUploadStsResult that = (GetMediaUploadStsResult) o;
        return Objects.equals(bucket, that.bucket)
                && Objects.equals(credentials, that.credentials)
                && Objects.equals(objectKeyPrefix, that.objectKeyPrefix)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(provider, that.provider)
                && Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucket, credentials, objectKeyPrefix, endpoint, provider, region);
    }

    @Override
    public String toString() {
        return "GetMediaUploadStsResult{"
                + "bucket='" + bucket + '\''
                + ", credentials=" + credentials
                + ", objectKeyPrefix='" + objectKeyPrefix + '\''
                + ", endpoint='" + endpoint + '\''
                + ", provider='" + provider + '\''
                + ", region='" + region + '\''
                + '}';
    }
}
