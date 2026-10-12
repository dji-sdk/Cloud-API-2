// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传 STS 临时凭证（AccessKey、过期时间、SecurityToken）。
 */
public final class MediaUploadStsCredentials {
    private String accessKeyId;
    private String accessKeySecret;
    private int expire;
    private String securityToken;
    public MediaUploadStsCredentials() {
    }

/**
     * 构造 MediaUploadStsCredentials。
     *
     * @param accessKeyId AccessKeyId
     * @param accessKeySecret AccessKeySecret
     * @param expire 凭证有效期（秒）
     * @param securityToken 安全令牌
     */
    public MediaUploadStsCredentials(
            String accessKeyId,
            String accessKeySecret,
            int expire,
            String securityToken) {
        this.accessKeyId = accessKeyId != null ? accessKeyId : "";
        this.accessKeySecret = accessKeySecret != null ? accessKeySecret : "";
        this.expire = expire;
        this.securityToken = securityToken != null ? securityToken : "";
    }

    /** AccessKeyId */
    public String getAccessKeyId() { return accessKeyId; }

    /** AccessKeySecret */
    public String getAccessKeySecret() { return accessKeySecret; }

    /** 凭证有效期（秒） */
    public int getExpire() { return expire; }

    /** 安全令牌 */
    public String getSecurityToken() { return securityToken; }

    /** AccessKeyId */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /** AccessKeySecret */
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    /** 凭证有效期（秒） */
    public void setExpire(int expire) {
        this.expire = expire;
    }

    /** 安全令牌 */
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadStsCredentials that = (MediaUploadStsCredentials) o;
        return expire == that.expire
                && Objects.equals(accessKeyId, that.accessKeyId)
                && Objects.equals(accessKeySecret, that.accessKeySecret)
                && Objects.equals(securityToken, that.securityToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKeyId, accessKeySecret, expire, securityToken);
    }

    @Override
    public String toString() {
        return "MediaUploadStsCredentials{"
                + "accessKeyId='" + accessKeyId + '\''
                + ", accessKeySecret='" + accessKeySecret + '\''
                + ", expire=" + expire
                + ", securityToken='" + securityToken + '\''
                + '}';
    }
}
