// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** MinIO STS 临时上传凭证 */
public class AIInsideCredentials {
    /** MinIO AccessKey ID */
    private String accessKeyId = "";
    /** MinIO SecretKey */
    private String accessKeySecret = "";
    /** 凭证过期时间（Unix 秒） */
    private long expire;
    /** STS SessionToken（当前实现为空字符串） */
    private String securityToken = "";

    public AIInsideCredentials() {}

    /** MinIO AccessKey ID */
    public String getAccessKeyId() { return accessKeyId; }

    /** @param v MinIO AccessKey ID */
    public void setAccessKeyId(String v) { this.accessKeyId = v != null ? v : ""; }

    /** MinIO SecretKey */
    public String getAccessKeySecret() { return accessKeySecret; }

    /** @param v MinIO SecretKey */
    public void setAccessKeySecret(String v) { this.accessKeySecret = v != null ? v : ""; }

    /** 凭证过期时间（Unix 秒） */
    public long getExpire() { return expire; }

    /** @param v 凭证过期时间（Unix 秒） */
    public void setExpire(long v) { this.expire = v; }

    /** STS SessionToken（当前实现为空字符串） */
    public String getSecurityToken() { return securityToken; }

    /** @param v STS SessionToken（当前实现为空字符串） */
    public void setSecurityToken(String v) { this.securityToken = v != null ? v : ""; }
}
