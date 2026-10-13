// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/**
 * 喊话器音频播放信息。
 */
public class PsdkSpeakerAudioPlayInfo {

    private String name = "";
    private String url = "";
    private String md5 = "";
    private String format = "pcm";

    public PsdkSpeakerAudioPlayInfo() {
    }


    /** 名称 */
    public String getName() {
        return name;
    }

    /** @param name 名称 */
    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    /** URL地址 */
    public String getUrl() {
        return url;
    }

    /** @param url URL地址 */
    public void setUrl(String url) {
        this.url = url != null ? url : "";
    }

    /** MD5校验值 */
    public String getMd5() {
        return md5;
    }

    /** @param md5 MD5校验值 */
    public void setMd5(String md5) {
        this.md5 = md5 != null ? md5 : "";
    }

    /** 音频格式 */
    public String getFormat() {
        return format;
    }

    /** @param format 音频格式 */
    public void setFormat(String format) {
        this.format = format != null ? format : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkSpeakerAudioPlayInfo that = (PsdkSpeakerAudioPlayInfo) o;
        return Objects.equals(name, that.name)
                && Objects.equals(url, that.url)
                && Objects.equals(md5, that.md5)
                && Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, md5, format);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerAudioPlayInfo{"
                + "name='" + name + '\''
                + ", url='" + url + '\''
                + ", md5='" + md5 + '\''
                + ", format='" + format + '\''
                + '}';
    }
}
