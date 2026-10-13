// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 单条直播转发记录 */
public class LiveSyncItem {
    /** 转码器唯一 ID */
    private String uuid = "";
    /** 状态：1=空闲 2=推流中 3=失败 */
    private int status = 0;
    /** 工作区 UUID（PaaS 固定为默认 org，永不为空，透传即可） */
    private String orgUuid = "";
    /** 直播源设备 SN，惯例填飞机（drone）SN；传网关 SN 后端不拦截但下游行为不保证 */
    private String sn = "";
    /** 摄像头索引 */
    private String cameraIndex = "";
    /** 转码器名称 */
    private String name = "";
    /** 推流协议：rtmp / gb28181 / rtsp */
    private String schema = "";
    /** 推流协议参数 */
    private LiveSyncSchemaOption schemaOption = new LiveSyncSchemaOption();
    /** 设备名称 */
    private String deviceName = "";
    /** 设备是否在线 */
    private boolean deviceOnline = false;
    /** 设备型号 key */
    private String deviceModelKey = "";
    /** 是否为机场设备 */
    private boolean isAirport = false;
    /** 视频流类型 */
    private String videoType = "";
    /** 视频质量 */
    private int videoQuality = 0;
    /** 区域 */
    private String region = "";
    /** 是否启用 */
    private boolean enable = false;
    /** 推流失败时的错误信息 */
    private String errMsg = "";
    /** 设备类型 */
    private String deviceType = "";

    public LiveSyncItem() {}

    public String getUuid() { return uuid; }
    public void setUuid(String v) { this.uuid = v != null ? v : ""; }

    public int getStatus() { return status; }
    public void setStatus(int v) { this.status = v; }

    public String getOrgUuid() { return orgUuid; }
    public void setOrgUuid(String v) { this.orgUuid = v != null ? v : ""; }

    public String getSn() { return sn; }
    public void setSn(String v) { this.sn = v != null ? v : ""; }

    public String getCameraIndex() { return cameraIndex; }
    public void setCameraIndex(String v) { this.cameraIndex = v != null ? v : ""; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v != null ? v : ""; }

    public String getSchema() { return schema; }
    public void setSchema(String v) { this.schema = v != null ? v : ""; }

    public LiveSyncSchemaOption getSchemaOption() { return schemaOption; }
    public void setSchemaOption(LiveSyncSchemaOption v) { this.schemaOption = v; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String v) { this.deviceName = v != null ? v : ""; }

    public boolean isDeviceOnline() { return deviceOnline; }
    public void setDeviceOnline(boolean v) { this.deviceOnline = v; }

    public String getDeviceModelKey() { return deviceModelKey; }
    public void setDeviceModelKey(String v) { this.deviceModelKey = v != null ? v : ""; }

    public boolean isAirport() { return isAirport; }
    public void setAirport(boolean v) { this.isAirport = v; }

    public String getVideoType() { return videoType; }
    public void setVideoType(String v) { this.videoType = v != null ? v : ""; }

    public int getVideoQuality() { return videoQuality; }
    public void setVideoQuality(int v) { this.videoQuality = v; }

    public String getRegion() { return region; }
    public void setRegion(String v) { this.region = v != null ? v : ""; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean v) { this.enable = v; }

    public String getErrMsg() { return errMsg; }
    public void setErrMsg(String v) { this.errMsg = v != null ? v : ""; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String v) { this.deviceType = v != null ? v : ""; }
}
