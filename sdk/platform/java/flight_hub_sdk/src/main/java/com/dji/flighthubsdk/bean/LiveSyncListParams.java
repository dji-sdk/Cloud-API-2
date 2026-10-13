// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 分页查询转码器列表请求参数 */
public class LiveSyncListParams {
    /** 页码（从 1 开始） */
    private int page = 1;
    /** 每页数量 */
    private int size = 20;
    /** 是否按 sn 过滤 */
    private boolean setSn = false;
    /** 设备序列号过滤 */
    private String sn = "";
    /** 是否按 schema 过滤 */
    private boolean setSchema = false;
    /** 推流协议过滤：rtmp / gb28181 / rtsp */
    private String schema = "";
    /** 是否按 enable 状态过滤 */
    private boolean setEnable = false;
    /** 启用状态过滤 */
    private boolean enable = false;

    public LiveSyncListParams() {}

    public int getPage() { return page; }
    public void setPage(int v) { this.page = v; }

    public int getSize() { return size; }
    public void setSize(int v) { this.size = v; }

    public boolean isSetSn() { return setSn; }
    public void setSetSn(boolean v) { this.setSn = v; }

    public String getSn() { return sn; }
    public void setSn(String v) { this.sn = v != null ? v : ""; }

    public boolean isSetSchema() { return setSchema; }
    public void setSetSchema(boolean v) { this.setSchema = v; }

    public String getSchema() { return schema; }
    public void setSchema(String v) { this.schema = v != null ? v : ""; }

    public boolean isSetEnable() { return setEnable; }
    public void setSetEnable(boolean v) { this.setEnable = v; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean v) { this.enable = v; }
}
