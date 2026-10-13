// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * PSDK 喊话器资源。
 */
public final class PsdkSpeakerResource {

    private int id;
    private String name;
    /** "audio" 或 "tts" */
    private String fileType;
    private String objectKey;
    private String objectUrl;
    private String format;
    private long size;
    private long duration;
    private String state;
    private String stateMessage;
    private String pcmObjectKey;
    private String pcmObjectUrl;
    private long pcmSize;
    private String pcmMd5Sum;
    private String content;
    private String md5Sum;
    private long createdAt;
    /** Opus 格式对象 key */
    private String opusObjectKey;
    /** Opus 文件大小 */
    private long opusSize;
    /** Opus 文件 MD5 */
    private String opusMd5Sum;
    public PsdkSpeakerResource() {
    }

/**
     * 构造 PsdkSpeakerResource。
     */
    public PsdkSpeakerResource(
            int id,
            String name,
            String fileType,
            String objectKey,
            String objectUrl,
            String format,
            long size,
            long duration,
            String state,
            String stateMessage,
            String pcmObjectKey,
            String pcmObjectUrl,
            long pcmSize,
            String pcmMd5Sum,
            String content,
            String md5Sum,
            long createdAt,
            String opusObjectKey,
            long opusSize,
            String opusMd5Sum) {
        this.id = id;
        this.name = name != null ? name : "";
        this.fileType = fileType != null ? fileType : "";
        this.objectKey = objectKey != null ? objectKey : "";
        this.objectUrl = objectUrl != null ? objectUrl : "";
        this.format = format != null ? format : "";
        this.size = size;
        this.duration = duration;
        this.state = state != null ? state : "";
        this.stateMessage = stateMessage != null ? stateMessage : "";
        this.pcmObjectKey = pcmObjectKey != null ? pcmObjectKey : "";
        this.pcmObjectUrl = pcmObjectUrl != null ? pcmObjectUrl : "";
        this.pcmSize = pcmSize;
        this.pcmMd5Sum = pcmMd5Sum != null ? pcmMd5Sum : "";
        this.content = content != null ? content : "";
        this.md5Sum = md5Sum != null ? md5Sum : "";
        this.createdAt = createdAt;
        this.opusObjectKey = opusObjectKey != null ? opusObjectKey : "";
        this.opusSize = opusSize;
        this.opusMd5Sum = opusMd5Sum != null ? opusMd5Sum : "";
    }

    /** 资源ID */
    public int getId() { return id; }
    /** 名称 */
    public String getName() { return name; }
    /** "audio" 或 "tts" */
    public String getFileType() { return fileType; }
    /** 对象存储Key */
    public String getObjectKey() { return objectKey; }
    /** 对象URL */
    public String getObjectUrl() { return objectUrl; }
    /** 格式 */
    public String getFormat() { return format; }
    /** 文件大小 */
    public long getSize() { return size; }
    /** 时长 */
    public long getDuration() { return duration; }
    /** 状态 */
    public String getState() { return state; }
    /** 状态消息 */
    public String getStateMessage() { return stateMessage; }
    /** PCM对象存储Key */
    public String getPcmObjectKey() { return pcmObjectKey; }
    /** PCM对象URL */
    public String getPcmObjectUrl() { return pcmObjectUrl; }
    /** PCM文件大小 */
    public long getPcmSize() { return pcmSize; }
    /** PCM文件MD5 */
    public String getPcmMd5Sum() { return pcmMd5Sum; }
    /** 内容 */
    public String getContent() { return content; }
    /** MD5校验值 */
    public String getMd5Sum() { return md5Sum; }
    /** 创建时间 */
    public long getCreatedAt() { return createdAt; }
    /** Opus 格式对象 key */
    public String getOpusObjectKey() { return opusObjectKey; }
    /** Opus 文件大小 */
    public long getOpusSize() { return opusSize; }
    /** Opus 文件 MD5 */
    public String getOpusMd5Sum() { return opusMd5Sum; }

    /** 资源ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @param fileType "audio" 或 "tts" */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /** 对象存储Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 对象URL */
    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
    }

    /** 格式 */
    public void setFormat(String format) {
        this.format = format;
    }

    /** 文件大小 */
    public void setSize(long size) {
        this.size = size;
    }

    /** 时长 */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /** 状态 */
    public void setState(String state) {
        this.state = state;
    }

    /** 状态消息 */
    public void setStateMessage(String stateMessage) {
        this.stateMessage = stateMessage;
    }

    /** PCM对象存储Key */
    public void setPcmObjectKey(String pcmObjectKey) {
        this.pcmObjectKey = pcmObjectKey;
    }

    /** PCM对象URL */
    public void setPcmObjectUrl(String pcmObjectUrl) {
        this.pcmObjectUrl = pcmObjectUrl;
    }

    /** PCM文件大小 */
    public void setPcmSize(long pcmSize) {
        this.pcmSize = pcmSize;
    }

    /** PCM文件MD5 */
    public void setPcmMd5Sum(String pcmMd5Sum) {
        this.pcmMd5Sum = pcmMd5Sum;
    }

    /** 内容 */
    public void setContent(String content) {
        this.content = content;
    }

    /** MD5校验值 */
    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    /** 创建时间 */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /** Opus 格式对象 key */
    public void setOpusObjectKey(String opusObjectKey) {
        this.opusObjectKey = opusObjectKey;
    }

    /** Opus 文件大小 */
    public void setOpusSize(long opusSize) {
        this.opusSize = opusSize;
    }

    /** Opus 文件 MD5 */
    public void setOpusMd5Sum(String opusMd5Sum) {
        this.opusMd5Sum = opusMd5Sum;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsdkSpeakerResource that = (PsdkSpeakerResource) o;
        return id == that.id
                && size == that.size
                && duration == that.duration
                && pcmSize == that.pcmSize
                && createdAt == that.createdAt
                && opusSize == that.opusSize
                && Objects.equals(name, that.name)
                && Objects.equals(fileType, that.fileType)
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(objectUrl, that.objectUrl)
                && Objects.equals(format, that.format)
                && Objects.equals(state, that.state)
                && Objects.equals(stateMessage, that.stateMessage)
                && Objects.equals(pcmObjectKey, that.pcmObjectKey)
                && Objects.equals(pcmObjectUrl, that.pcmObjectUrl)
                && Objects.equals(pcmMd5Sum, that.pcmMd5Sum)
                && Objects.equals(content, that.content)
                && Objects.equals(md5Sum, that.md5Sum)
                && Objects.equals(opusObjectKey, that.opusObjectKey)
                && Objects.equals(opusMd5Sum, that.opusMd5Sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fileType, objectKey, objectUrl, format, size, duration, state, stateMessage,
                pcmObjectKey, pcmObjectUrl, pcmSize, pcmMd5Sum, content, md5Sum, createdAt,
                opusObjectKey, opusSize, opusMd5Sum);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerResource{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", fileType='" + fileType + '\''
                + ", objectKey='" + objectKey + '\''
                + ", objectUrl='" + objectUrl + '\''
                + ", format='" + format + '\''
                + ", size=" + size
                + ", duration=" + duration
                + ", state='" + state + '\''
                + ", stateMessage='" + stateMessage + '\''
                + ", pcmObjectKey='" + pcmObjectKey + '\''
                + ", pcmObjectUrl='" + pcmObjectUrl + '\''
                + ", pcmSize=" + pcmSize
                + ", pcmMd5Sum='" + pcmMd5Sum + '\''
                + ", content='" + content + '\''
                + ", md5Sum='" + md5Sum + '\''
                + ", createdAt=" + createdAt
                + ", opusObjectKey='" + opusObjectKey + '\''
                + ", opusSize=" + opusSize
                + ", opusMd5Sum='" + opusMd5Sum + '\''
                + '}';
    }
}
