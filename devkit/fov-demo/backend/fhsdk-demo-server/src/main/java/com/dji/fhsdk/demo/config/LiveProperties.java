// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "live")
public class LiveProperties {

    /**
     * ffmpeg 输入裸码流格式：{@code auto}（按 Annex-B NAL 自动识别）、{@code h264}、{@code hevc}/{@code h265}。
     * EA340 多为 HEVC，其它机型多为 H264；推荐保持 auto。
     */
    private String inputFormat = "auto";
    /**
     * 仅当输入为 HEVC 时生效：转成 H264 再 WHIP（浏览器/Oryx 更稳；转码会丢掉码流内 SEI，
     * 本 Demo 的 SEI 走 SDK 回调不受影响）。H264 输入始终 copy，忽略本开关。
     */
    private boolean transcodeToH264 = true;

    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
    public boolean isTranscodeToH264() { return transcodeToH264; }
    public void setTranscodeToH264(boolean transcodeToH264) { this.transcodeToH264 = transcodeToH264; }
}
