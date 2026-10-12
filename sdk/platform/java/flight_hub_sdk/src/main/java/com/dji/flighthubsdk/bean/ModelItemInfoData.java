// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 模型项信息数据（UUID、对象存储 key、文件类型） */
public class ModelItemInfoData {
    private String uuid = "";
    /** 对象存储 key; */
    private String objectKey = "";
    /** 文件类型。枚举："tif", "b3dm", "pnts"; */
    private String fileType = "";

    public ModelItemInfoData() {
    }


    /** UUID */
    public String getUuid() { return uuid; }
    /** @param uuid UUID */
    public void setUuid(String uuid) { this.uuid = uuid != null ? uuid : ""; }
    /** 对象存储 key; */
    public String getObjectKey() { return objectKey; }
    /** @param objectKey 对象存储 key; */
    public void setObjectKey(String objectKey) { this.objectKey = objectKey != null ? objectKey : ""; }
    /** 文件类型。枚举："tif", "b3dm", "pnts"; */
    public String getFileType() { return fileType; }
    /** @param fileType 文件类型。枚举："tif", "b3dm", "pnts"; */
    public void setFileType(String fileType) { this.fileType = fileType != null ? fileType : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelItemInfoData that = (ModelItemInfoData) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(fileType, that.fileType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, objectKey, fileType);
    }

    @Override
    public String toString() {
        return "ModelItemInfoData{uuid='" + uuid + '\'' + ", objectKey='" + objectKey + '\'' + ", fileType='" + fileType + '\'' + '}';
    }
}
