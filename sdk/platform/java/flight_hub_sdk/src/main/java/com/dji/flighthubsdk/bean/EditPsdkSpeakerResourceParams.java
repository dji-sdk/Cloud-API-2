// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 编辑 PSDK 喊话器资源请求参数 */
public class EditPsdkSpeakerResourceParams {
    /** 服务端期望 string 类型 */
    private String id = "";
    /** required */
    private String name = "";
    /** "audio" or "tts" */
    private String fileType = "";
    /** required if file_type is "audio" */
    private boolean visible = false;
    /** required if file_type is "tts" */
    private String content = "";

    public EditPsdkSpeakerResourceParams() {
    }


    /** 服务端期望 string 类型 */
    public String getId() { return id; }
    /** @param id 服务端期望 string 类型 */
    public void setId(String id) { this.id = id != null ? id : ""; }

    /** required */
    public String getName() { return name; }
    /** @param name required */
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** "audio" or "tts" */
    public String getFileType() { return fileType; }
    /** @param fileType "audio" or "tts" */
    public void setFileType(String fileType) { this.fileType = fileType != null ? fileType : ""; }

    /** required if file_type is "audio" */
    public boolean isVisible() { return visible; }
    /** @param visible required if file_type is "audio" */
    public void setVisible(boolean visible) { this.visible = visible; }

    /** required if file_type is "tts" */
    public String getContent() { return content; }
    /** @param content required if file_type is "tts" */
    public void setContent(String content) { this.content = content != null ? content : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EditPsdkSpeakerResourceParams that = (EditPsdkSpeakerResourceParams) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(fileType, that.fileType)
                && visible == that.visible
                && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                fileType,
                visible,
                content);
    }

    @Override
    public String toString() {
        return "EditPsdkSpeakerResourceParams{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", fileType='" + fileType + '\''
                + ", visible=" + visible
                + ", content='" + content + '\''
                + '}';
    }
}
