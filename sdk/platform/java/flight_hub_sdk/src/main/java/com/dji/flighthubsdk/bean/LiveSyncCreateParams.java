// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 创建转码器请求参数 */
public class LiveSyncCreateParams {
    /** 转码器名称 */
    private String converterName = "";
    /** 直播源设备 SN，惯例填飞机（drone）SN；传网关 SN 后端不拦截但下游行为不保证 */
    private String sn = "";
    /** 摄像头索引 */
    private String cameraIndex = "";
    /** 视频流类型 */
    private String videoType = "";
    /** 视频质量 */
    private int videoQuality = 0;
    /** 推流协议：rtmp / gb28181 / rtsp */
    private String schema = "";
    /** 推流协议参数 */
    private LiveSyncSchemaOption schemaOption = new LiveSyncSchemaOption();

    public LiveSyncCreateParams() {}

    public String getConverterName() { return converterName; }
    public void setConverterName(String v) { this.converterName = v != null ? v : ""; }

    public String getSn() { return sn; }
    public void setSn(String v) { this.sn = v != null ? v : ""; }

    public String getCameraIndex() { return cameraIndex; }
    public void setCameraIndex(String v) { this.cameraIndex = v != null ? v : ""; }

    public String getVideoType() { return videoType; }
    public void setVideoType(String v) { this.videoType = v != null ? v : ""; }

    public int getVideoQuality() { return videoQuality; }
    public void setVideoQuality(int v) { this.videoQuality = v; }

    public String getSchema() { return schema; }
    public void setSchema(String v) { this.schema = v != null ? v : ""; }

    public LiveSyncSchemaOption getSchemaOption() { return schemaOption; }
    public void setSchemaOption(LiveSyncSchemaOption v) { this.schemaOption = v; }
}
