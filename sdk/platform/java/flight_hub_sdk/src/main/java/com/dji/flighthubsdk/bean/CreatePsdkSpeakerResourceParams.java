// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建 PSDK 喊话器资源请求参数 */
public class CreatePsdkSpeakerResourceParams {
    private String name = "";
    /** "audio" or "tts" */
    private String fileType = "";
    /** required if file_type is "audio" */
    private String objectKey = "";
    /** required if file_type is "audio" and create_scene is "record" */
    private long duration = 0L;
    /** required if file_type is "tts" */
    private String content = "";
    /** "import" or "record" */
    private String createScene = "";
    /** whether to convert to opus format */
    private boolean needOpus = false;
    /** 文件大小 */
    private long size = 0L;
    /** 是否可见 */
    private boolean visible = false;

    public CreatePsdkSpeakerResourceParams() {
    }


    /** 资源名称 */
    public String getName() { return name; }
    /** @param name 资源名称 */
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** "audio" or "tts" */
    public String getFileType() { return fileType; }
    /** @param fileType "audio" or "tts" */
    public void setFileType(String fileType) { this.fileType = fileType != null ? fileType : ""; }

    /** required if file_type is "audio" */
    public String getObjectKey() { return objectKey; }
    /** @param objectKey required if file_type is "audio" */
    public void setObjectKey(String objectKey) { this.objectKey = objectKey != null ? objectKey : ""; }

    /** required if file_type is "audio" and create_scene is "record" */
    public long getDuration() { return duration; }
    /** @param duration required if file_type is "audio" and create_scene is "record" */
    public void setDuration(long duration) { this.duration = duration; }

    /** required if file_type is "tts" */
    public String getContent() { return content; }
    /** @param content required if file_type is "tts" */
    public void setContent(String content) { this.content = content != null ? content : ""; }

    /** "import" or "record" */
    public String getCreateScene() { return createScene; }
    /** @param createScene "import" or "record" */
    public void setCreateScene(String createScene) { this.createScene = createScene != null ? createScene : ""; }

    /** whether to convert to opus format */
    public boolean isNeedOpus() { return needOpus; }
    /** @param needOpus whether to convert to opus format */
    public void setNeedOpus(boolean needOpus) { this.needOpus = needOpus; }

    /** 文件大小 */
    public long getSize() { return size; }
    /** @param size 文件大小 */
    public void setSize(long size) { this.size = size; }

    /** 是否可见 */
    public boolean isVisible() { return visible; }
    /** @param visible 是否可见 */
    public void setVisible(boolean visible) { this.visible = visible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreatePsdkSpeakerResourceParams that = (CreatePsdkSpeakerResourceParams) o;
        return Objects.equals(name, that.name)
                && Objects.equals(fileType, that.fileType)
                && Objects.equals(objectKey, that.objectKey)
                && duration == that.duration
                && Objects.equals(content, that.content)
                && Objects.equals(createScene, that.createScene)
                && needOpus == that.needOpus
                && size == that.size
                && visible == that.visible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                fileType,
                objectKey,
                duration,
                content,
                createScene,
                needOpus,
                size,
                visible);
    }

    @Override
    public String toString() {
        return "CreatePsdkSpeakerResourceParams{"
                + "name='" + name + '\''
                + ", fileType='" + fileType + '\''
                + ", objectKey='" + objectKey + '\''
                + ", duration=" + duration
                + ", content='" + content + '\''
                + ", createScene='" + createScene + '\''
                + ", needOpus=" + needOpus
                + ", size=" + size
                + ", visible=" + visible
                + '}';
    }
}
