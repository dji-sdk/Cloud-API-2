// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传回调通知中的文件信息。
 */
public final class MediaUploadCallbackFile {
    private String objectKey;
    private String fingerprint;
    private String name;
    private int type;
    private String cloudToCloudId;
    private MediaUploadCallbackFileExt ext;
    private MediaUploadCallbackMetadata metadata;
    private String createdTime;
    private String source;
    public MediaUploadCallbackFile() {
    }

/**
     * 构造 MediaUploadCallbackFile。
     *
     * @param objectKey 对象存储 Key
     * @param fingerprint 文件指纹
     * @param name 文件名
     * @param type 文件类型
     * @param cloudToCloudId 云到云 ID
     * @param ext 文件扩展信息
     * @param metadata 文件元数据
     * @param createdTime 创建时间
     * @param source 来源
     */
    public MediaUploadCallbackFile(
            String objectKey,
            String fingerprint,
            String name,
            int type,
            String cloudToCloudId,
            MediaUploadCallbackFileExt ext,
            MediaUploadCallbackMetadata metadata,
            String createdTime,
            String source) {
        this.objectKey = objectKey != null ? objectKey : "";
        this.fingerprint = fingerprint != null ? fingerprint : "";
        this.name = name != null ? name : "";
        this.type = type;
        this.cloudToCloudId = cloudToCloudId != null ? cloudToCloudId : "";
        this.ext = ext;
        this.metadata = metadata;
        this.createdTime = createdTime != null ? createdTime : "";
        this.source = source != null ? source : "";
    }

    /** 对象存储 Key */
    public String getObjectKey() { return objectKey; }

    /** 文件指纹 */
    public String getFingerprint() { return fingerprint; }

    /** 文件名 */
    public String getName() { return name; }

    /** 文件类型 */
    public int getType() { return type; }

    /** 云到云 ID */
    public String getCloudToCloudId() { return cloudToCloudId; }

    /** 文件扩展信息 */
    public MediaUploadCallbackFileExt getExt() { return ext; }

    /** 文件元数据 */
    public MediaUploadCallbackMetadata getMetadata() { return metadata; }

    /** 创建时间 */
    public String getCreatedTime() { return createdTime; }

    /** 来源 */
    public String getSource() { return source; }

    /** 对象存储 Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 文件指纹 */
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /** 文件名 */
    public void setName(String name) {
        this.name = name;
    }

    /** 文件类型 */
    public void setType(int type) {
        this.type = type;
    }

    /** 云到云 ID */
    public void setCloudToCloudId(String cloudToCloudId) {
        this.cloudToCloudId = cloudToCloudId;
    }

    /** 文件扩展信息 */
    public void setExt(MediaUploadCallbackFileExt ext) {
        this.ext = ext;
    }

    /** 文件元数据 */
    public void setMetadata(MediaUploadCallbackMetadata metadata) {
        this.metadata = metadata;
    }

    /** 创建时间 */
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    /** 来源 */
    public void setSource(String source) {
        this.source = source;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadCallbackFile that = (MediaUploadCallbackFile) o;
        return type == that.type
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(fingerprint, that.fingerprint)
                && Objects.equals(name, that.name)
                && Objects.equals(cloudToCloudId, that.cloudToCloudId)
                && Objects.equals(ext, that.ext)
                && Objects.equals(metadata, that.metadata)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, fingerprint, name, type, cloudToCloudId, ext, metadata, createdTime, source);
    }

    @Override
    public String toString() {
        return "MediaUploadCallbackFile{"
                + "objectKey='" + objectKey + '\''
                + ", fingerprint='" + fingerprint + '\''
                + ", name='" + name + '\''
                + ", type=" + type
                + ", cloudToCloudId='" + cloudToCloudId + '\''
                + ", ext=" + ext
                + ", metadata=" + metadata
                + ", createdTime='" + createdTime + '\''
                + ", source='" + source + '\''
                + '}';
    }
}
