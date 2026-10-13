// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 更新转码器请求参数；通过 set* 标记实际要修改的字段，未标记字段不写入 */
public class LiveSyncUpdateParams {
    /** 转码器 ID（必填） */
    private String id = "";
    /** 是否设置 enable 字段 */
    private boolean setEnable = false;
    /** 是否启用 */
    private boolean enable = false;
    /** 是否设置 schema 字段 */
    private boolean setSchema = false;
    /** 推流协议：rtmp / gb28181 / rtsp */
    private String schema = "";
    /** 是否设置 schemaOption 字段 */
    private boolean setSchemaOption = false;
    /** 推流协议参数 */
    private LiveSyncSchemaOption schemaOption = new LiveSyncSchemaOption();
    /** 是否设置 converterName 字段 */
    private boolean setConverterName = false;
    /** 转码器名称 */
    private String converterName = "";
    /** 是否设置 sn 字段 */
    private boolean setSn = false;
    /** 直播源设备 SN，惯例填飞机（drone）SN；传网关 SN 后端不拦截但下游行为不保证 */
    private String sn = "";
    /** 是否设置 cameraIndex 字段 */
    private boolean setCameraIndex = false;
    /** 摄像头索引 */
    private String cameraIndex = "";
    /** 是否设置 videoType 字段 */
    private boolean setVideoType = false;
    /** 视频流类型 */
    private String videoType = "";
    /** 是否设置 videoQuality 字段 */
    private boolean setVideoQuality = false;
    /** 视频质量 */
    private int videoQuality = 0;

    public LiveSyncUpdateParams() {}

    public String getId() { return id; }
    public void setId(String v) { this.id = v != null ? v : ""; }

    public boolean isSetEnable() { return setEnable; }
    public void setSetEnable(boolean v) { this.setEnable = v; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean v) { this.enable = v; }

    public boolean isSetSchema() { return setSchema; }
    public void setSetSchema(boolean v) { this.setSchema = v; }

    public String getSchema() { return schema; }
    public void setSchema(String v) { this.schema = v != null ? v : ""; }

    public boolean isSetSchemaOption() { return setSchemaOption; }
    public void setSetSchemaOption(boolean v) { this.setSchemaOption = v; }

    public LiveSyncSchemaOption getSchemaOption() { return schemaOption; }
    public void setSchemaOption(LiveSyncSchemaOption v) { this.schemaOption = v; }

    public boolean isSetConverterName() { return setConverterName; }
    public void setSetConverterName(boolean v) { this.setConverterName = v; }

    public String getConverterName() { return converterName; }
    public void setConverterName(String v) { this.converterName = v != null ? v : ""; }

    public boolean isSetSn() { return setSn; }
    public void setSetSn(boolean v) { this.setSn = v; }

    public String getSn() { return sn; }
    public void setSn(String v) { this.sn = v != null ? v : ""; }

    public boolean isSetCameraIndex() { return setCameraIndex; }
    public void setSetCameraIndex(boolean v) { this.setCameraIndex = v; }

    public String getCameraIndex() { return cameraIndex; }
    public void setCameraIndex(String v) { this.cameraIndex = v != null ? v : ""; }

    public boolean isSetVideoType() { return setVideoType; }
    public void setSetVideoType(boolean v) { this.setVideoType = v; }

    public String getVideoType() { return videoType; }
    public void setVideoType(String v) { this.videoType = v != null ? v : ""; }

    public boolean isSetVideoQuality() { return setVideoQuality; }
    public void setSetVideoQuality(boolean v) { this.setVideoQuality = v; }

    public int getVideoQuality() { return videoQuality; }
    public void setVideoQuality(int v) { this.videoQuality = v; }
}
