// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 全景瓦片 CDN Cookie 鉴权项。用于 {@link GetPanoTierTileUrlResult#getCookies()}。 */
public final class PanoTierCookie {

    private String name;
    private String value;
    private String path;
    /** Cookie 到期时间（Unix 秒）；后端签名有效期 24 小时。 */
    private long expiresUnixSec;

    public PanoTierCookie() {}

    public PanoTierCookie(String name, String value, String path, long expiresUnixSec) {
        this.name = name != null ? name : "";
        this.value = value != null ? value : "";
        this.path = path != null ? path : "";
        this.expiresUnixSec = expiresUnixSec;
    }

    /** Cookie 名（如 "tile-signature" 或 CloudFront-* 等） */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Cookie 值 */
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    /** Cookie 作用域（如 "/model/api/v1/tiles"） */
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    /** 到期时间戳（Unix 秒） */
    public long getExpiresUnixSec() { return expiresUnixSec; }
    public void setExpiresUnixSec(long expiresUnixSec) { this.expiresUnixSec = expiresUnixSec; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanoTierCookie that = (PanoTierCookie) o;
        return expiresUnixSec == that.expiresUnixSec
                && Objects.equals(name, that.name)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(name, value, expiresUnixSec); }

    @Override
    public String toString() {
        return "PanoTierCookie{name='" + name + "', path='" + path + "'}";
    }
}
