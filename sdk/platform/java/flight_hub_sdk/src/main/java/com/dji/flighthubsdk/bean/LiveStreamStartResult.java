// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 直播拉流地址信息。由后端 POST /pf-live/api/v2/stream/start 返回，供用户自行拉流播放。
 */
public final class LiveStreamStartResult {

    /** 观众端拉流凭证过期时间戳（Unix 秒）。agora 为签发时间+60s（短期窗口，JWT 自身 exp 另有 365 天），volc 为签发时间+30min，drtc 恒为 0（过期时间编码在 url 的 query 参数中） */
    private String expireTs;
    /** 直播流提供方类型。取值："agora"（WebRTC/WHEP）、"drtc"、"volc" */
    private String urlType;
    /** 拉流/加入频道的连接地址。agora 为 WHEP 地址（HTTP URL，含 token），drtc/volc 为 URL-encoded query string（含 app_id/room_id/token 等） */
    private String url;
    /** 推流端（设备端）在 RTC 频道中的用户标识。agora 为数字（如 "50000"），drtc 为设备 SN，volc 固定为 "device" */
    private String publishUid;
    /** DRTC 仲裁地址，仅 url_type="drtc" 时返回；其他 provider 为空 */
    private String providerUrl;

    public LiveStreamStartResult() {
    }

    /**
     * 构造 LiveStreamStartResult。
     *
     * @param expireTs 观众端拉流凭证过期时间戳（Unix 秒）
     * @param urlType 直播流提供方类型（"agora"/"drtc"/"volc"/"srs"）
     * @param url 拉流/加入频道的连接地址
     * @param publishUid 推流端（设备端）在 RTC 频道中的用户标识
     * @param providerUrl DRTC 仲裁地址，仅 url_type="drtc" 时返回
     */
    public LiveStreamStartResult(
            String expireTs,
            String urlType,
            String url,
            String publishUid,
            String providerUrl) {
        this.expireTs = expireTs != null ? expireTs : "";
        this.urlType = urlType != null ? urlType : "";
        this.url = url != null ? url : "";
        this.publishUid = publishUid != null ? publishUid : "";
        this.providerUrl = providerUrl != null ? providerUrl : "";
    }

    /** 观众端拉流凭证过期时间戳（Unix 秒）。agora 为签发时间+60s（短期窗口，JWT 自身 exp 另有 365 天），volc 为签发时间+30min，drtc 恒为 0（过期时间编码在 url 的 query 参数中） */
    public String getExpireTs() { return expireTs; }
    /** 直播流提供方类型。取值："agora"（WebRTC/WHEP）、"drtc"、"volc" */
    public String getUrlType() { return urlType; }
    /** 拉流/加入频道的连接地址。agora 为 WHEP 地址（HTTP URL，含 token），drtc/volc 为 URL-encoded query string（含 app_id/room_id/token 等） */
    public String getUrl() { return url; }
    /** 推流端（设备端）在 RTC 频道中的用户标识。agora 为数字（如 "50000"），drtc 为设备 SN，volc 固定为 "device" */
    public String getPublishUid() { return publishUid; }
    /** DRTC 仲裁地址，仅 url_type="drtc" 时返回；其他 provider 为空 */
    public String getProviderUrl() { return providerUrl; }

    /** @param expireTs 观众端拉流凭证过期时间戳（Unix 秒）。agora 为签发时间+60s（短期窗口，JWT 自身 exp 另有 365 天），volc 为签发时间+30min，drtc 恒为 0（过期时间编码在 url 的 query 参数中） */
    public void setExpireTs(String expireTs) {
        this.expireTs = expireTs;
    }

    /** @param urlType 直播流提供方类型。取值："agora"（WebRTC/WHEP）、"drtc"、"volc" */
    public void setUrlType(String urlType) {
        this.urlType = urlType;
    }

    /** @param url 拉流/加入频道的连接地址。agora 为 WHEP 地址（HTTP URL，含 token），drtc/volc 为 URL-encoded query string（含 app_id/room_id/token 等） */
    public void setUrl(String url) {
        this.url = url;
    }

    /** @param publishUid 推流端（设备端）在 RTC 频道中的用户标识。agora 为数字（如 "50000"），drtc 为设备 SN，volc 固定为 "device" */
    public void setPublishUid(String publishUid) {
        this.publishUid = publishUid;
    }

    /** @param providerUrl DRTC 仲裁地址，仅 url_type="drtc" 时返回；其他 provider 为空 */
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveStreamStartResult that = (LiveStreamStartResult) o;
        return Objects.equals(expireTs, that.expireTs)
                && Objects.equals(urlType, that.urlType)
                && Objects.equals(url, that.url)
                && Objects.equals(publishUid, that.publishUid)
                && Objects.equals(providerUrl, that.providerUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expireTs, urlType, url, publishUid, providerUrl);
    }

    @Override
    public String toString() {
        return "LiveStreamStartResult{"
                + "expireTs='" + expireTs + '\''
                + ", urlType='" + urlType + '\''
                + ", url='" + url + '\''
                + ", publishUid='" + publishUid + '\''
                + ", providerUrl='" + providerUrl + '\''
                + '}';
    }
}
