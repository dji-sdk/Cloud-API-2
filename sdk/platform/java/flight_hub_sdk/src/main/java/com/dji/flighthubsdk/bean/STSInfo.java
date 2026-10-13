// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** STS 信息，包含对象存储前缀、Bucket、Endpoint、Provider、Region 及临时凭证 */
public final class STSInfo {
    private String objectKeyPrefix;
    private String bucket;
    private String endpoint;
    private String provider;
    private String region;
    private STSCredential credentials;
    public STSInfo() {
    }

/**
     * 构造 STSInfo。
     *
     * @param objectKeyPrefix 上传路径前缀
     * @param bucket 存储桶
     * @param endpoint 访问端点
     * @param provider 存储提供商
     * @param region 区域
     * @param credentials STS临时凭证
     */
    public STSInfo(
            String objectKeyPrefix,
            String bucket,
            String endpoint,
            String provider,
            String region,
            STSCredential credentials) {
        this.objectKeyPrefix = objectKeyPrefix != null ? objectKeyPrefix : "";
        this.bucket = bucket != null ? bucket : "";
        this.endpoint = endpoint != null ? endpoint : "";
        this.provider = provider != null ? provider : "";
        this.region = region != null ? region : "";
        this.credentials = credentials != null ? credentials : new STSCredential("", "", 0, "", 0);
    }

    /** 上传路径前缀 */
    public String getObjectKeyPrefix() { return objectKeyPrefix; }
    /** 存储桶 */
    public String getBucket() { return bucket; }
    /** 访问端点 */
    public String getEndpoint() { return endpoint; }
    /** 存储提供商 */
    public String getProvider() { return provider; }
    /** 区域 */
    public String getRegion() { return region; }
    /** STS临时凭证 */
    public STSCredential getCredentials() { return credentials; }

    /** 上传路径前缀 */
    public void setObjectKeyPrefix(String objectKeyPrefix) {
        this.objectKeyPrefix = objectKeyPrefix;
    }

    /** 存储桶 */
    public void setBucket(String bucket) {
        this.bucket = bucket;
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

    /** STS临时凭证 */
    public void setCredentials(STSCredential credentials) {
        this.credentials = credentials;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        STSInfo that = (STSInfo) o;
        return Objects.equals(objectKeyPrefix, that.objectKeyPrefix)
                && Objects.equals(bucket, that.bucket)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(provider, that.provider)
                && Objects.equals(region, that.region)
                && Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKeyPrefix, bucket, endpoint, provider, region, credentials);
    }

    @Override
    public String toString() {
        return "STSInfo{"
                + "objectKeyPrefix='" + objectKeyPrefix + '\''
                + ", bucket='" + bucket + '\''
                + ", endpoint='" + endpoint + '\''
                + ", provider='" + provider + '\''
                + ", region='" + region + '\''
                + ", credentials=" + credentials
                + '}';
    }
}
