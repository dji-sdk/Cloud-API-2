// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 媒体文件视频元数据。 */
public final class MediaFileVideoMetaData {
    /** 视频时长(单位: 秒), 示例值: 26.927, 负数表示无效值; */
    private double duration;
    /** 视频编码器, 示例值：Lavf58.12.100, 空字符串表示无效值; */
    private String encoder;
    /** 视频格式名称, 示例值：QuickTime / MOV, 空字符串表示无效值; */
    private String formatLongName;
    /** 示例值: "mov,mp4,m4a,3gp,3g2,mj2", 空字符串表示无效值; */
    private String formatName;
    /** 视频高度(单位: 像素), 示例值：1080, 负数表示无效值; */
    private int height;
    /** 视频宽度(单位: 像素), 示例值：1920, 负数表示无效值; */
    private int width;
    public MediaFileVideoMetaData() {
    }

/**
     * 构造 MediaFileVideoMetaData。
     *
     * @param duration 视频时长
     * @param encoder 编码器
     * @param formatLongName 格式全称
     * @param formatName 格式名称
     * @param height 视频高度
     * @param width 视频宽度
     */
    public MediaFileVideoMetaData(
            double duration,
            String encoder,
            String formatLongName,
            String formatName,
            int height,
            int width) {
        this.duration = duration;
        this.encoder = encoder != null ? encoder : "";
        this.formatLongName = formatLongName != null ? formatLongName : "";
        this.formatName = formatName != null ? formatName : "";
        this.height = height;
        this.width = width;
    }

    /** 视频时长(单位: 秒), 示例值: 26.927, 负数表示无效值; */
    public double getDuration() {
        return duration;
    }

    /** @param duration 视频时长(单位: 秒), 示例值: 26.927, 负数表示无效值; */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /** 视频编码器, 示例值：Lavf58.12.100, 空字符串表示无效值; */
    public String getEncoder() {
        return encoder;
    }

    /** @param encoder 视频编码器, 示例值：Lavf58.12.100, 空字符串表示无效值; */
    public void setEncoder(String encoder) {
        this.encoder = encoder;
    }

    /** 视频格式名称, 示例值：QuickTime / MOV, 空字符串表示无效值; */
    public String getFormatLongName() {
        return formatLongName;
    }

    /** @param formatLongName 视频格式名称, 示例值：QuickTime / MOV, 空字符串表示无效值; */
    public void setFormatLongName(String formatLongName) {
        this.formatLongName = formatLongName;
    }

    /** 示例值: "mov,mp4,m4a,3gp,3g2,mj2", 空字符串表示无效值; */
    public String getFormatName() {
        return formatName;
    }

    /** @param formatName 示例值: "mov,mp4,m4a,3gp,3g2,mj2", 空字符串表示无效值; */
    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    /** 视频高度(单位: 像素), 示例值：1080, 负数表示无效值; */
    public int getHeight() {
        return height;
    }

    /** @param height 视频高度(单位: 像素), 示例值：1080, 负数表示无效值; */
    public void setHeight(int height) {
        this.height = height;
    }

    /** 视频宽度(单位: 像素), 示例值：1920, 负数表示无效值; */
    public int getWidth() {
        return width;
    }

    /** @param width 视频宽度(单位: 像素), 示例值：1920, 负数表示无效值; */
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFileVideoMetaData that = (MediaFileVideoMetaData) o;
        return Double.compare(that.duration, duration) == 0
                && Objects.equals(encoder, that.encoder)
                && Objects.equals(formatLongName, that.formatLongName)
                && Objects.equals(formatName, that.formatName)
                && height == that.height
                && width == that.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                duration,
                encoder,
                formatLongName,
                formatName,
                height,
                width);
    }

    @Override
    public String toString() {
        return "MediaFileVideoMetaData{"
                + "duration=" + duration
                + ", encoder='" + encoder + '\''
                + ", formatLongName='" + formatLongName + '\''
                + ", formatName='" + formatName + '\''
                + ", height=" + height
                + ", width=" + width
                + '}';
    }
}
