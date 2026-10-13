// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 直播画质设置参数。 */
public class LiveQualitySettingParams {

    /** 设备序列号 */
    private String sn = "";
    /** 摄像头标识 */
    private String camera = "";
    /** 视频流标识 */
    private String video = "";
    /** 视频质量等级：0=自适应 1=标清 4=超清 */
    private int videoQuality;

    public LiveQualitySettingParams() {
    }


    /** 设备序列号 */
    public String getSn() { return sn; }
    /** @param sn 设备序列号 */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }
    /** 摄像头标识 */
    public String getCamera() { return camera; }
    /** @param camera 摄像头标识 */
    public void setCamera(String camera) { this.camera = camera != null ? camera : ""; }
    /** 视频流标识 */
    public String getVideo() { return video; }
    /** @param video 视频流标识 */
    public void setVideo(String video) { this.video = video != null ? video : ""; }
    /** 视频质量等级：0=自适应 1=标清 4=超清 */
    public int getVideoQuality() { return videoQuality; }
    /** @param videoQuality 视频质量等级：0=自适应 1=标清 4=超清 */
    public void setVideoQuality(int videoQuality) { this.videoQuality = videoQuality; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveQualitySettingParams that = (LiveQualitySettingParams) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(camera, that.camera)
                && Objects.equals(video, that.video)
                && videoQuality == that.videoQuality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, camera, video, videoQuality);
    }

    @Override
    public String toString() {
        return "LiveQualitySettingParams{"
                + "sn='" + sn + '\''
                + ", camera='" + camera + '\''
                + ", video='" + video + '\''
                + ", videoQuality=" + videoQuality
                + '}';
    }
}
