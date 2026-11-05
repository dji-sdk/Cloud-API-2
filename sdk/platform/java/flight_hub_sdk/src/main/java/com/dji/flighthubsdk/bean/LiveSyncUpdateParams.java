// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/**
 * 更新转码器请求参数。
 *
 * <p>更新接口采用“标记字段”模型：只有调用了对应的
 * {@code setSetXxx(true)} 的字段才会写入后端；未标记的字段不会出现在本次更新中。
 *
 * <p><b>enable=false 必须配合 setSetEnable(true)</b>：
 * <pre>{@code
 * LiveSyncUpdateParams p = new LiveSyncUpdateParams();
 * p.setId("converter-id");
 * p.setSetEnable(true);
 * p.setEnable(false);
 * liveSyncModule.updateLiveSync(p, callback);
 * }</pre>
 *
 * 如果只调用 {@code setEnable(false)} 而不调用 {@code setSetEnable(true)}，
 * 后端会忽略 enable 字段，转码器不会被停用。
 *
 * <p><b>停用后再删除</b>：
 * <pre>{@code
 * // 1. 先停用
 * LiveSyncUpdateParams p = new LiveSyncUpdateParams();
 * p.setId("converter-id");
 * p.setSetEnable(true);
 * p.setEnable(false);
 * liveSyncModule.updateLiveSync(p, callback);
 *
 * // 2. 停用成功后删除
 * liveSyncModule.deleteLiveSync("converter-id", callback);
 * }</pre>
 *
 * <p>{@code id} 为必填。其他字段同理：要修改某个字段时，
 * 先调用对应的 {@code setSetXxx(true)}，再设置值。
 */
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
    /** 视频质量等级：0=自适应 1=标清 4=超清 */
    private int videoQuality = 0;

    /** 创建更新参数。 */
    public LiveSyncUpdateParams() {}

    /** 获取转码器 ID。 */
    public String getId() { return id; }

    /**
     * 设置转码器 ID。
     *
     * @param v 转码器 ID（必填）
     */
    public void setId(String v) { this.id = v != null ? v : ""; }

    /** 本次更新是否携带 enable 字段。停用转码器时必须为 true。 */
    public boolean isSetEnable() { return setEnable; }

    /**
     * 设置本次更新是否携带 enable 字段。
     *
     * <p>停用转码器时必须设为 {@code true}，否则 {@link #setEnable(boolean)}
     * 传入的 {@code false} 会被后端忽略。
     *
     * @param v true=本次更新写入 enable；false=不写入（默认）
     */
    public void setSetEnable(boolean v) { this.setEnable = v; }

    /** enable 字段值；仅当 setSetEnable(true) 时生效。 */
    public boolean isEnable() { return enable; }

    /**
     * 设置 enable 字段值。
     *
     * <p>必须配合 {@link #setSetEnable(boolean)} 使用：先调用
     * {@code setSetEnable(true)}，再调用本方法设置 {@code false} 才能停用。
     *
     * @param v true=启用，false=停用；需先调用 setSetEnable(true)
     */
    public void setEnable(boolean v) { this.enable = v; }

    /** 本次更新是否携带 schema 字段。 */
    public boolean isSetSchema() { return setSchema; }

    /**
     * 设置本次更新是否携带 schema 字段。
     *
     * @param v true=本次更新写入 schema；false=不写入（默认）
     */
    public void setSetSchema(boolean v) { this.setSchema = v; }

    /** 推流协议：rtmp / gb28181 / rtsp；仅当 setSetSchema(true) 时生效。 */
    public String getSchema() { return schema; }

    /**
     * 设置推流协议。
     *
     * @param v 推流协议：rtmp / gb28181 / rtsp；仅当 setSetSchema(true) 时生效
     */
    public void setSchema(String v) { this.schema = v != null ? v : ""; }

    /** 本次更新是否携带 schemaOption 字段。 */
    public boolean isSetSchemaOption() { return setSchemaOption; }

    /**
     * 设置本次更新是否携带 schemaOption 字段。
     *
     * @param v true=本次更新写入 schemaOption；false=不写入（默认）
     */
    public void setSetSchemaOption(boolean v) { this.setSchemaOption = v; }

    /** 推流协议参数；仅当 setSetSchemaOption(true) 时生效。 */
    public LiveSyncSchemaOption getSchemaOption() { return schemaOption; }

    /**
     * 设置推流协议参数。
     *
     * @param v 推流协议参数；仅当 setSetSchemaOption(true) 时生效
     */
    public void setSchemaOption(LiveSyncSchemaOption v) { this.schemaOption = v; }

    /** 本次更新是否携带 converterName 字段。 */
    public boolean isSetConverterName() { return setConverterName; }

    /**
     * 设置本次更新是否携带 converterName 字段。
     *
     * @param v true=本次更新写入 converterName；false=不写入（默认）
     */
    public void setSetConverterName(boolean v) { this.setConverterName = v; }

    /** 转码器名称；仅当 setSetConverterName(true) 时生效。 */
    public String getConverterName() { return converterName; }

    /**
     * 设置转码器名称。
     *
     * @param v 转码器名称；仅当 setSetConverterName(true) 时生效
     */
    public void setConverterName(String v) { this.converterName = v != null ? v : ""; }

    /** 本次更新是否携带 sn 字段。 */
    public boolean isSetSn() { return setSn; }

    /**
     * 设置本次更新是否携带 sn 字段。
     *
     * @param v true=本次更新写入 sn；false=不写入（默认）
     */
    public void setSetSn(boolean v) { this.setSn = v; }

    /** 直播源设备 SN；仅当 setSetSn(true) 时生效。 */
    public String getSn() { return sn; }

    /**
     * 设置直播源设备 SN。
     *
     * @param v 直播源设备 SN；仅当 setSetSn(true) 时生效
     */
    public void setSn(String v) { this.sn = v != null ? v : ""; }

    /** 本次更新是否携带 cameraIndex 字段。 */
    public boolean isSetCameraIndex() { return setCameraIndex; }

    /**
     * 设置本次更新是否携带 cameraIndex 字段。
     *
     * @param v true=本次更新写入 cameraIndex；false=不写入（默认）
     */
    public void setSetCameraIndex(boolean v) { this.setCameraIndex = v; }

    /** 摄像头索引；仅当 setSetCameraIndex(true) 时生效。 */
    public String getCameraIndex() { return cameraIndex; }

    /**
     * 设置摄像头索引。
     *
     * @param v 摄像头索引；仅当 setSetCameraIndex(true) 时生效
     */
    public void setCameraIndex(String v) { this.cameraIndex = v != null ? v : ""; }

    /** 本次更新是否携带 videoType 字段。 */
    public boolean isSetVideoType() { return setVideoType; }

    /**
     * 设置本次更新是否携带 videoType 字段。
     *
     * @param v true=本次更新写入 videoType；false=不写入（默认）
     */
    public void setSetVideoType(boolean v) { this.setVideoType = v; }

    /** 视频流类型；仅当 setSetVideoType(true) 时生效。 */
    public String getVideoType() { return videoType; }

    /**
     * 设置视频流类型。
     *
     * @param v 视频流类型；仅当 setSetVideoType(true) 时生效
     */
    public void setVideoType(String v) { this.videoType = v != null ? v : ""; }

    /** 本次更新是否携带 videoQuality 字段。 */
    public boolean isSetVideoQuality() { return setVideoQuality; }

    /**
     * 设置本次更新是否携带 videoQuality 字段。
     *
     * @param v true=本次更新写入 videoQuality；false=不写入（默认）
     */
    public void setSetVideoQuality(boolean v) { this.setVideoQuality = v; }

    /** 视频质量等级：0=自适应 1=标清 4=超清；仅当 setSetVideoQuality(true) 时生效。 */
    public int getVideoQuality() { return videoQuality; }

    /**
     * 设置视频质量等级。
     *
     * @param v 视频质量等级：0=自适应 1=标清 4=超清；仅当 setSetVideoQuality(true) 时生效
     */
    public void setVideoQuality(int v) { this.videoQuality = v; }
}
