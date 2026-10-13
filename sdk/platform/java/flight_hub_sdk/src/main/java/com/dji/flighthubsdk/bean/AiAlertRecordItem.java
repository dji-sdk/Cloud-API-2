// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** AI 告警明细条目 */
public class AiAlertRecordItem {
    /** 告警唯一 ID */
    private String alertUuid = "";
    /** 所属架次 ID */
    private String flightId = "";
    /** 无人机序列号 */
    private String droneSn = "";
    /** 遥控器序列号 */
    private String gatewaySn = "";
    /** 0=待处理 1=处理中 2=已完成 3=云云对接 4=失败 5=超时 */
    private int status;
    /** 算法来源 */
    private int algorithmSource;
    /** 告警发生时的位置信息 */
    private AiAlertLocation location;
    /** 各目标类型的识别结果列表 */
    private List<AiAlertTargetAlertInfo> targetAlertInfos = new ArrayList<>();
    /** status=1/2/3 时有值，24h CDN 鉴权 URL */
    private String thumbnailUrl = "";
    /** 目标标签列表 */
    private List<String> labels = new ArrayList<>();
    /** 毫秒时间戳 */
    private long timestamp;
    /** 飞行任务名称 */
    private String taskName = "";
    /** 媒体文件索引 */
    private int mediaIndex;

    public AiAlertRecordItem() {}

    /** 告警唯一 ID */
    public String getAlertUuid() { return alertUuid; }
    /** @param alertUuid 告警唯一 ID */
    public void setAlertUuid(String v) { this.alertUuid = v != null ? v : ""; }

    /** 所属架次 ID */
    public String getFlightId() { return flightId; }
    /** @param flightId 所属架次 ID */
    public void setFlightId(String v) { this.flightId = v != null ? v : ""; }

    /** 无人机序列号 */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 无人机序列号 */
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    /** 遥控器序列号 */
    public String getGatewaySn() { return gatewaySn; }
    /** @param gatewaySn 遥控器序列号 */
    public void setGatewaySn(String v) { this.gatewaySn = v != null ? v : ""; }

    /** 0=待处理 1=处理中 2=已完成 3=云云对接 4=失败 5=超时 */
    public int getStatus() { return status; }
    /** @param status 0=待处理 1=处理中 2=已完成 3=云云对接 4=失败 5=超时 */
    public void setStatus(int v) { this.status = v; }

    /** 算法来源 */
    public int getAlgorithmSource() { return algorithmSource; }
    /** @param algorithmSource 算法来源 */
    public void setAlgorithmSource(int v) { this.algorithmSource = v; }

    /** 告警发生时的位置信息 */
    public AiAlertLocation getLocation() { return location; }
    /** @param location 告警发生时的位置信息 */
    public void setLocation(AiAlertLocation v) { this.location = v; }

    /** 各目标类型的识别结果列表 */
    public List<AiAlertTargetAlertInfo> getTargetAlertInfos() { return targetAlertInfos; }
    /** @param targetAlertInfos 各目标类型的识别结果列表 */
    public void setTargetAlertInfos(List<AiAlertTargetAlertInfo> v) { this.targetAlertInfos = v != null ? v : new ArrayList<>(); }

    /** status=1/2/3 时有值，24h CDN 鉴权 URL */
    public String getThumbnailUrl() { return thumbnailUrl; }
    /** @param thumbnailUrl status=1/2/3 时有值，24h CDN 鉴权 URL */
    public void setThumbnailUrl(String v) { this.thumbnailUrl = v != null ? v : ""; }

    /** 目标标签列表 */
    public List<String> getLabels() { return labels; }
    /** @param labels 目标标签列表 */
    public void setLabels(List<String> v) { this.labels = v != null ? v : new ArrayList<>(); }

    /** 毫秒时间戳 */
    public long getTimestamp() { return timestamp; }
    /** @param timestamp 毫秒时间戳 */
    public void setTimestamp(long v) { this.timestamp = v; }

    /** 飞行任务名称 */
    public String getTaskName() { return taskName; }
    /** @param taskName 飞行任务名称 */
    public void setTaskName(String v) { this.taskName = v != null ? v : ""; }

    /** 媒体文件索引 */
    public int getMediaIndex() { return mediaIndex; }
    /** @param mediaIndex 媒体文件索引 */
    public void setMediaIndex(int v) { this.mediaIndex = v; }
}
