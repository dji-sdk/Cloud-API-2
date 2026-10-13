// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 监听通知。 */
public final class MediaFilePostProcess {
    /** 文件 id; */
    private long fileId;
    /** 飞行任务 id; */
    private String flightId;
    /** 设备 SN; */
    private String deviceSn;
    /** 文件对象 key(媒体文件在对象存储中的唯一标识); */
    private String objectKey;
    /** 文件大小(字节); */
    private long objectSize;
    /** 文件类型, { 2: 图片; 3: 视频 }; 可根据 file_type 判断文件类型, 解析和使用后续的 image_metadata 或 video_metadata; */
    private int fileType;
    /** 预览文件对象 key(媒体文件在对象存储中的唯一标识); */
    private String previewObjectKey;
    /** 图片媒体元数据, 包含拍摄相关详细信息; */
    private MediaFileImageMetaData imageMetadata;
    /** 视频媒体元数据, 包含视频相关详细信息; */
    private MediaFileVideoMetaData videoMetadata;
    public MediaFilePostProcess() {
    }

/**
     * 构造 MediaFilePostProcess。
     *
     * @param fileId 文件ID
     * @param flightId 飞行任务ID
     * @param deviceSn 设备序列号
     * @param objectKey 对象存储Key
     * @param objectSize 对象大小
     * @param fileType 文件类型
     * @param previewObjectKey 预览文件对象Key
     * @param imageMetadata 图片元数据
     * @param videoMetadata 视频元数据
     */
    public MediaFilePostProcess(
            long fileId,
            String flightId,
            String deviceSn,
            String objectKey,
            long objectSize,
            int fileType,
            String previewObjectKey,
            MediaFileImageMetaData imageMetadata,
            MediaFileVideoMetaData videoMetadata) {
        this.fileId = fileId;
        this.flightId = flightId != null ? flightId : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.objectKey = objectKey != null ? objectKey : "";
        this.objectSize = objectSize;
        this.fileType = fileType;
        this.previewObjectKey = previewObjectKey != null ? previewObjectKey : "";
        this.imageMetadata = imageMetadata;
        this.videoMetadata = videoMetadata;
    }

    /** 文件 id; */
    public long getFileId() {
        return fileId;
    }

    /** @param fileId 文件 id; */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /** 飞行任务 id; */
    public String getFlightId() {
        return flightId;
    }

    /** @param flightId 飞行任务 id; */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 设备 SN; */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备 SN; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 文件对象 key(媒体文件在对象存储中的唯一标识); */
    public String getObjectKey() {
        return objectKey;
    }

    /** @param objectKey 文件对象 key(媒体文件在对象存储中的唯一标识); */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 文件大小(字节); */
    public long getObjectSize() {
        return objectSize;
    }

    /** @param objectSize 文件大小(字节); */
    public void setObjectSize(long objectSize) {
        this.objectSize = objectSize;
    }

    /** 文件类型, { 2: 图片; 3: 视频 }; 可根据 file_type 判断文件类型, 解析和使用后续的 image_metadata 或 video_metadata; */
    public int getFileType() {
        return fileType;
    }

    /** @param fileType 文件类型, { 2: 图片; 3: 视频 }; 可根据 file_type 判断文件类型, 解析和使用后续的 image_metadata 或 video_metadata; */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    /** 预览文件对象 key(媒体文件在对象存储中的唯一标识); */
    public String getPreviewObjectKey() {
        return previewObjectKey;
    }

    /** @param previewObjectKey 预览文件对象 key(媒体文件在对象存储中的唯一标识); */
    public void setPreviewObjectKey(String previewObjectKey) {
        this.previewObjectKey = previewObjectKey;
    }

    /** 图片媒体元数据, 包含拍摄相关详细信息; */
    public MediaFileImageMetaData getImageMetadata() {
        return imageMetadata;
    }

    /** @param imageMetadata 图片媒体元数据, 包含拍摄相关详细信息; */
    public void setImageMetadata(MediaFileImageMetaData imageMetadata) {
        this.imageMetadata = imageMetadata;
    }

    /** 视频媒体元数据, 包含视频相关详细信息; */
    public MediaFileVideoMetaData getVideoMetadata() {
        return videoMetadata;
    }

    /** @param videoMetadata 视频媒体元数据, 包含视频相关详细信息; */
    public void setVideoMetadata(MediaFileVideoMetaData videoMetadata) {
        this.videoMetadata = videoMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFilePostProcess that = (MediaFilePostProcess) o;
        return fileId == that.fileId
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(objectKey, that.objectKey)
                && objectSize == that.objectSize
                && fileType == that.fileType
                && Objects.equals(previewObjectKey, that.previewObjectKey)
                && Objects.equals(imageMetadata, that.imageMetadata)
                && Objects.equals(videoMetadata, that.videoMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fileId,
                flightId,
                deviceSn,
                objectKey,
                objectSize,
                fileType,
                previewObjectKey,
                imageMetadata,
                videoMetadata);
    }

    @Override
    public String toString() {
        return "MediaFilePostProcess{"
                + "fileId=" + fileId
                + ", flightId='" + flightId + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", objectKey='" + objectKey + '\''
                + ", objectSize=" + objectSize
                + ", fileType=" + fileType
                + ", previewObjectKey='" + previewObjectKey + '\''
                + ", imageMetadata=" + imageMetadata
                + ", videoMetadata=" + videoMetadata
                + '}';
    }
}
