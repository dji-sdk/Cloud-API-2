// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 模型文件上传回调请求参数
 * 对应接口: POST /pf-terra/api/v1/models/files/upload-callback
 */
public final class TerraUploadCallbackParams {

    /** OSS 对象 key，必填 */
    private String objectKey;
    /** 文件名（含后缀），必填 */
    private String name;
    /** 父目录 ID；仅当 setParentId() 被调用后（parentIdSet==true）才会传递给服务端 */
    private long parentId;
    /** 是否传 parentId */
    private boolean parentIdSet;
    /** 存储盘 UUID（可选，null 表示不传） */
    private String driveUuid;
    /** 解压完成后是否自动加载到地图（可选，null 表示不传） */
    private Boolean showOnMap;

    public TerraUploadCallbackParams() {
    }

    /**
     * @param objectKey  OSS 对象 key，必填
     * @param name       文件名（含后缀），必填
     */
    public TerraUploadCallbackParams(String objectKey, String name) {
        this.objectKey = objectKey;
        this.name = name;
    }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getParentId() { return parentId; }
    public boolean isParentIdSet() { return parentIdSet; }
    /** 调用此方法后 parentId 才会被序列化到请求中 */
    public void setParentId(long parentId) { this.parentId = parentId; this.parentIdSet = true; }

    public String getDriveUuid() { return driveUuid; }
    public void setDriveUuid(String driveUuid) { this.driveUuid = driveUuid; }

    public Boolean getShowOnMap() { return showOnMap; }
    public void setShowOnMap(Boolean showOnMap) { this.showOnMap = showOnMap; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraUploadCallbackParams that = (TerraUploadCallbackParams) o;
        return parentId == that.parentId && parentIdSet == that.parentIdSet
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(name, that.name)
                && Objects.equals(driveUuid, that.driveUuid)
                && Objects.equals(showOnMap, that.showOnMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, name, parentId, parentIdSet, driveUuid, showOnMap);
    }

    @Override
    public String toString() {
        return "TerraUploadCallbackParams{"
                + "objectKey='" + objectKey + '\''
                + ", name='" + name + '\''
                + ", parentId=" + (parentIdSet ? parentId : "unset")
                + ", driveUuid='" + driveUuid + '\''
                + ", showOnMap=" + showOnMap
                + '}';
    }
}
