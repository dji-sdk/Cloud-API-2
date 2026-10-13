// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 航线编辑器 STS 临时凭证 */
public final class WaylineEditorStsCredentials {

    /** 临时 AccessKey ID */
    private String accessKeyId;
    /** 临时 AccessKey Secret */
    private String accessKeySecret;
    /** 凭证有效期（秒） */
    private int expire;
    /** STS SecurityToken */
    private String securityToken;
    /** 存储平台标识 */
    private int platform;

    public WaylineEditorStsCredentials() {
    }

    /**
     * 构造 WaylineEditorStsCredentials。
     *
     * @param accessKeyId     临时 AccessKey ID
     * @param accessKeySecret 临时 AccessKey Secret
     * @param expire          有效期（秒）
     * @param securityToken   STS SecurityToken
     * @param platform        存储平台标识
     */
    public WaylineEditorStsCredentials(String accessKeyId, String accessKeySecret,
            int expire, String securityToken, int platform) {
        this.accessKeyId = accessKeyId != null ? accessKeyId : "";
        this.accessKeySecret = accessKeySecret != null ? accessKeySecret : "";
        this.expire = expire;
        this.securityToken = securityToken != null ? securityToken : "";
        this.platform = platform;
    }

    /** 临时 AccessKey ID */
    public String getAccessKeyId() { return accessKeyId; }

    /** 临时 AccessKey ID */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /** 临时 AccessKey Secret */
    public String getAccessKeySecret() { return accessKeySecret; }

    /** 临时 AccessKey Secret */
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    /** 凭证有效期（秒） */
    public int getExpire() { return expire; }

    /** @param expire 凭证有效期（秒） */
    public void setExpire(int expire) {
        this.expire = expire;
    }

    /** STS SecurityToken */
    public String getSecurityToken() { return securityToken; }

    /** STS SecurityToken */
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    /** 存储平台标识 */
    public int getPlatform() { return platform; }

    /** 存储平台标识 */
    public void setPlatform(int platform) {
        this.platform = platform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineEditorStsCredentials that = (WaylineEditorStsCredentials) o;
        return expire == that.expire
                && platform == that.platform
                && Objects.equals(accessKeyId, that.accessKeyId)
                && Objects.equals(accessKeySecret, that.accessKeySecret)
                && Objects.equals(securityToken, that.securityToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKeyId, accessKeySecret, expire, securityToken, platform);
    }

    @Override
    public String toString() {
        return "WaylineEditorStsCredentials{"
                + "accessKeyId='" + accessKeyId + '\''
                + ", expire=" + expire
                + ", platform=" + platform
                + '}';
    }
}
