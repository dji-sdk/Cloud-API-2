// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** STS 临时凭证（AccessKey、过期时间、SecurityToken） */
public final class STSCredential {
    private String accessKeyId;
    private String accessKeySecret;
    private long expire;
    private String securityToken;
    /** 直播专用，后续再废弃 */
    private int platform;
    /** 凭证绝对过期时间 */
    private String expiration;
    public STSCredential() {
    }

/**
     * 构造 STSCredential。
     *
     * @param accessKeyId AccessKeyId
     * @param accessKeySecret AccessKeySecret
     * @param expire 过期时间
     * @param securityToken 安全令牌
     * @param platform 直播专用，后续再废弃
     */
    public STSCredential(String accessKeyId, String accessKeySecret, long expire, String securityToken, int platform) {
        this(accessKeyId, accessKeySecret, expire, securityToken, platform, "");
    }

    /**
     * 构造 STSCredential。
     *
     * @param accessKeyId AccessKeyId
     * @param accessKeySecret AccessKeySecret
     * @param expire 过期时间
     * @param securityToken 安全令牌
     * @param platform 直播专用，后续再废弃
     * @param expiration 凭证绝对过期时间
     */
    public STSCredential(String accessKeyId, String accessKeySecret, long expire, String securityToken,
            int platform, String expiration) {
        this.accessKeyId = accessKeyId != null ? accessKeyId : "";
        this.accessKeySecret = accessKeySecret != null ? accessKeySecret : "";
        this.expire = expire;
        this.securityToken = securityToken != null ? securityToken : "";
        this.platform = platform;
        this.expiration = expiration != null ? expiration : "";
    }

    /** AccessKeyId */
    public String getAccessKeyId() { return accessKeyId; }
    /** AccessKeySecret */
    public String getAccessKeySecret() { return accessKeySecret; }
    /** 过期时间 */
    public long getExpire() { return expire; }
    /** 安全令牌 */
    public String getSecurityToken() { return securityToken; }
    /** 直播专用，后续再废弃 */
    public int getPlatform() { return platform; }

    /** AccessKeyId */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /** AccessKeySecret */
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    /** 过期时间 */
    public void setExpire(long expire) {
        this.expire = expire;
    }

    /** 安全令牌 */
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    /** 直播专用，后续再废弃 */
    public void setPlatform(int platform) {
        this.platform = platform;
    }

    /** 凭证绝对过期时间 */
    public String getExpiration() { return expiration; }

    /** 凭证绝对过期时间 */
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        STSCredential that = (STSCredential) o;
        return expire == that.expire
                && platform == that.platform
                && Objects.equals(accessKeyId, that.accessKeyId)
                && Objects.equals(accessKeySecret, that.accessKeySecret)
                && Objects.equals(securityToken, that.securityToken)
                && Objects.equals(expiration, that.expiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKeyId, accessKeySecret, expire, securityToken, platform, expiration);
    }

    @Override
    public String toString() {
        return "STSCredential{"
                + "accessKeyId='" + accessKeyId + '\''
                + ", accessKeySecret='" + accessKeySecret + '\''
                + ", expire=" + expire
                + ", securityToken='" + securityToken + '\''
                + ", platform=" + platform
                + ", expiration='" + expiration + '\''
                + '}';
    }
}
