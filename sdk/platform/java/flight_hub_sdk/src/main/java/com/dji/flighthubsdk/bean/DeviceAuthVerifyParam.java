// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设备一机一密鉴权信息。 */
public class DeviceAuthVerifyParam {
    private String sn = "";
    private String secret = "";
    private String nonce = "";

    public DeviceAuthVerifyParam() {
    }

    /** 设备经纬 SN */
    public String getSn() {
        return sn;
    }

    /** 设备经纬 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 设备密钥 */
    public String getSecret() {
        return secret;
    }

    /** 设备密钥 */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /** 随机数（防重放攻击） */
    public String getNonce() {
        return nonce;
    }

    /** 随机数（防重放攻击） */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceAuthVerifyParam that = (DeviceAuthVerifyParam) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(secret, that.secret)
                && Objects.equals(nonce, that.nonce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, secret, nonce);
    }

    @Override
    public String toString() {
        return "DeviceAuthVerifyParam{sn='" + sn + '\'' + ", secret='***'" + ", nonce='" + nonce + '\'' + '}';
    }
}
