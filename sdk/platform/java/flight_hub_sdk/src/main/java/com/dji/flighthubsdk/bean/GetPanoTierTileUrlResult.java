// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 获取全景瓦片 CDN URL 结果。
 *
 * <p>仅当 {@code state == 3 (PanoTierState_SUCCESS)} 时，
 * {@code baseUrl} / {@code queryParams} 有效。</p>
 *
 * <p>PaaS 通道鉴权方式：将 {@code queryParams} 拼到瓦片 URL 后（推荐）。</p>
 *
 * <p><b>注意</b>：{@code cookies} 字段在 PaaS 通道下<b>不填充</b>。
 * 后端通过 HTTP {@code Set-Cookie} 响应头下发 Cookie，SDK 不解析响应头；
 * 浏览器场景由 HTTP client 自动携带，Native/Java 场景请改用 {@code queryParams}
 * （Key/Value 与 cookies 同源）。</p>
 */
public final class GetPanoTierTileUrlResult {

    /** 瓦片切片状态，取值与 C++ PanoTierState 枚举一致（0~4） */
    private int state;
    /** 瓦片基础 URL（CDN 域名 + prefix，指向 vtour 目录） */
    private String baseUrl;
    /** CDN Cookie 鉴权列表（PaaS 通道不填充；保留供未来场景扩展） */
    private List<PanoTierCookie> cookies;
    /** URL Query 鉴权参数（PaaS 推荐；Key/Value 与 cookies 同源） */
    private Map<String, String> queryParams;

    public GetPanoTierTileUrlResult() {
        this.baseUrl = "";
        this.cookies = new ArrayList<>();
        this.queryParams = new HashMap<>();
    }

    /** 瓦片切片状态（3 = 成功，此时其余字段有效） */
    public int getState() { return state; }
    public void setState(int state) { this.state = state; }

    /** 瓦片基础 URL */
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl != null ? baseUrl : ""; }

    /** CDN Cookie 鉴权列表（PaaS 通道不填充） */
    public List<PanoTierCookie> getCookies() { return cookies; }
    public void setCookies(List<PanoTierCookie> cookies) {
        this.cookies = cookies != null ? cookies : new ArrayList<>();
    }

    /** URL Query 鉴权参数 */
    public Map<String, String> getQueryParams() { return queryParams; }
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPanoTierTileUrlResult that = (GetPanoTierTileUrlResult) o;
        return state == that.state && Objects.equals(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() { return Objects.hash(state, baseUrl); }

    @Override
    public String toString() {
        return "GetPanoTierTileUrlResult{state=" + state
                + ", baseUrl='" + baseUrl + '\''
                + ", queryParamsSize=" + (queryParams != null ? queryParams.size() : 0)
                + '}';
    }
}
