// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 告警推送 — 单条告警记录。
 *
 * <p>对应 WS topic {@code alert_records.alert_records_notify} 推送中
 * {@code records} 数组元素。
 */
public class AiAlertNotifyRecordItem {

    /** 告警唯一 ID */
    private String alertUuid = "";
    /** 所属架次 ID */
    private String flightId = "";
    /** 无人机序列号 */
    private String droneSn = "";
    /** 网关/机场序列号 */
    private String gatewaySn = "";
    /** 0=待处理 1=处理中 2=已完成 3=云云对接 4=失败 5=超时 */
    private int status;
    /** 算法来源 */
    private int algorithmSource;
    /** 告警发生时的位置信息 */
    private AiAlertLocation location;
    /** 文件 ID */
    private long fileId;
    /** 媒体文件索引 */
    private long mediaIndex;
    /** 飞行任务名称 */
    private String taskName = "";
    /** 各目标类型的识别结果列表 */
    private List<AiAlertNotifyTargetInfo> targetAlertInfos = new ArrayList<>();
    /** 毫秒时间戳 */
    private long timestamp;
    /** 缩略图 URL（24h CDN 鉴权） */
    private String thumbnailUrl = "";
    /** 目标标签列表 */
    private List<String> labels = new ArrayList<>();

    public AiAlertNotifyRecordItem() {}

    /** @return 告警唯一 ID */
    public String getAlertUuid() { return alertUuid; }
    /** @param v 告警唯一 ID */
    public void setAlertUuid(String v) { this.alertUuid = v != null ? v : ""; }

    /** @return 所属架次 ID */
    public String getFlightId() { return flightId; }
    /** @param v 所属架次 ID */
    public void setFlightId(String v) { this.flightId = v != null ? v : ""; }

    /** @return 无人机序列号 */
    public String getDroneSn() { return droneSn; }
    /** @param v 无人机序列号 */
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    /** @return 网关/机场序列号 */
    public String getGatewaySn() { return gatewaySn; }
    /** @param v 网关/机场序列号 */
    public void setGatewaySn(String v) { this.gatewaySn = v != null ? v : ""; }

    /** @return 告警状态（0=待处理 1=处理中 2=已完成 3=云云对接 4=失败 5=超时） */
    public int getStatus() { return status; }
    /** @param v 告警状态 */
    public void setStatus(int v) { this.status = v; }

    /** @return 算法来源 */
    public int getAlgorithmSource() { return algorithmSource; }
    /** @param v 算法来源 */
    public void setAlgorithmSource(int v) { this.algorithmSource = v; }

    /** @return 告警发生时的位置信息 */
    public AiAlertLocation getLocation() { return location; }
    /** @param v 告警发生时的位置信息 */
    public void setLocation(AiAlertLocation v) { this.location = v; }

    /** @return 文件 ID */
    public long getFileId() { return fileId; }
    /** @param v 文件 ID */
    public void setFileId(long v) { this.fileId = v; }

    /** @return 媒体文件索引 */
    public long getMediaIndex() { return mediaIndex; }
    /** @param v 媒体文件索引 */
    public void setMediaIndex(long v) { this.mediaIndex = v; }

    /** @return 飞行任务名称 */
    public String getTaskName() { return taskName; }
    /** @param v 飞行任务名称 */
    public void setTaskName(String v) { this.taskName = v != null ? v : ""; }

    /** @return 各目标类型的识别结果列表 */
    public List<AiAlertNotifyTargetInfo> getTargetAlertInfos() { return targetAlertInfos; }
    /** @param v 各目标类型的识别结果列表 */
    public void setTargetAlertInfos(List<AiAlertNotifyTargetInfo> v) { this.targetAlertInfos = v != null ? v : new ArrayList<>(); }

    /** @return 毫秒时间戳 */
    public long getTimestamp() { return timestamp; }
    /** @param v 毫秒时间戳 */
    public void setTimestamp(long v) { this.timestamp = v; }

    /** @return 缩略图 URL（24h CDN 鉴权） */
    public String getThumbnailUrl() { return thumbnailUrl; }
    /** @param v 缩略图 URL */
    public void setThumbnailUrl(String v) { this.thumbnailUrl = v != null ? v : ""; }

    /** @return 目标标签列表 */
    public List<String> getLabels() { return labels; }
    /** @param v 目标标签列表 */
    public void setLabels(List<String> v) { this.labels = v != null ? v : new ArrayList<>(); }
}
