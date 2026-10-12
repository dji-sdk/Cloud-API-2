// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/**
 * 喊话器 TTS 播放信息。
 */
public class PsdkSpeakerTTSPlayInfo {

    private String name = "";
    private String text = "";
    private String md5 = "";

    public PsdkSpeakerTTSPlayInfo() {
    }


    /** 名称 */
    public String getName() {
        return name;
    }

    /** @param name 名称 */
    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    /** 文本内容 */
    public String getText() {
        return text;
    }

    /** @param text 文本内容 */
    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    /** MD5校验值 */
    public String getMd5() {
        return md5;
    }

    /** @param md5 MD5校验值 */
    public void setMd5(String md5) {
        this.md5 = md5 != null ? md5 : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkSpeakerTTSPlayInfo that = (PsdkSpeakerTTSPlayInfo) o;
        return Objects.equals(name, that.name)
                && Objects.equals(text, that.text)
                && Objects.equals(md5, that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, text, md5);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerTTSPlayInfo{name='" + name + '\'' + ", text='" + text + '\'' + ", md5='" + md5 + '\'' + '}';
    }
}
