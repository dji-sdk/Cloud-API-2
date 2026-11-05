// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置视频分辨率参数 */
public class VideoResolutionSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型）整型码，取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    private int cameraLensType = 0;
    /** 视频分辨率。枚举（VideoResolutionType，Cloud API/Pilot/DRC 统一）：0=640x480, 1=640x512(红外相机), 2=1280x720(720p), 3=1920x960, 4=1920x1080(1080p), 5=2048x1080(2K Cinema), 6=2704x1520(2.7K), 7=2720x1530(2.7K), 8=3840x1572(超宽), 9=3840x2160(4K UHD), 10=4096x2160(4K Cinema C4K), 11=4608x2160, 12=4608x2592, 13=5280x2160(5.2K超宽), 14=5280x2972(5.2K), 15=5760x3240(5.8K), 16=6016x3200(6K), 17=MAX(传感器最大分辨率), 18=NO_SSD_VIDEO(SSD未设置), 0xFFFF=UNKNOWN */
    private int videoResolution = 0;

    public VideoResolutionSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型）整型码，取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型）整型码，取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 视频分辨率。枚举（VideoResolutionType，Cloud API/Pilot/DRC 统一）：0=640x480, 1=640x512(红外相机), 2=1280x720(720p), 3=1920x960, 4=1920x1080(1080p), 5=2048x1080(2K Cinema), 6=2704x1520(2.7K), 7=2720x1530(2.7K), 8=3840x1572(超宽), 9=3840x2160(4K UHD), 10=4096x2160(4K Cinema C4K), 11=4608x2160, 12=4608x2592, 13=5280x2160(5.2K超宽), 14=5280x2972(5.2K), 15=5760x3240(5.8K), 16=6016x3200(6K), 17=MAX(传感器最大分辨率), 18=NO_SSD_VIDEO(SSD未设置), 0xFFFF=UNKNOWN */
    public int getVideoResolution() { return videoResolution; }
    /** @param videoResolution 视频分辨率。枚举（VideoResolutionType，Cloud API/Pilot/DRC 统一）：0=640x480, 1=640x512(红外相机), 2=1280x720(720p), 3=1920x960, 4=1920x1080(1080p), 5=2048x1080(2K Cinema), 6=2704x1520(2.7K), 7=2720x1530(2.7K), 8=3840x1572(超宽), 9=3840x2160(4K UHD), 10=4096x2160(4K Cinema C4K), 11=4608x2160, 12=4608x2592, 13=5280x2160(5.2K超宽), 14=5280x2972(5.2K), 15=5760x3240(5.8K), 16=6016x3200(6K), 17=MAX(传感器最大分辨率), 18=NO_SSD_VIDEO(SSD未设置), 0xFFFF=UNKNOWN */
    public void setVideoResolution(int videoResolution) { this.videoResolution = videoResolution; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VideoResolutionSetParams that = (VideoResolutionSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && videoResolution == that.videoResolution;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, videoResolution);
    }

    @Override
    public String toString() {
        return "VideoResolutionSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", videoResolution=" + videoResolution + '}';
    }
}
