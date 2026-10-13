// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Terra Build 文件夹信息 */
public final class TerraBuildFolderInfo {
    private int folderId;
    private String folderUuid;
    private String folderName;
    public TerraBuildFolderInfo() {
    }

/**
     * 构造 TerraBuildFolderInfo。
     *
     * @param folderId 文件夹 ID
     * @param folderUuid 文件夹 UUID
     * @param folderName Folder名称
     */
    public TerraBuildFolderInfo(int folderId, String folderUuid, String folderName) {
        this.folderId = folderId;
        this.folderUuid = folderUuid != null ? folderUuid : "";
        this.folderName = folderName != null ? folderName : "";
    }

    /** 文件夹 ID */
    public int getFolderId() { return folderId; }
    /** 文件夹 UUID */
    public String getFolderUuid() { return folderUuid; }
    /** Folder名称 */
    public String getFolderName() { return folderName; }

    /** 文件夹 ID */
    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    /** 文件夹 UUID */
    public void setFolderUuid(String folderUuid) {
        this.folderUuid = folderUuid;
    }

    /** Folder名称 */
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildFolderInfo that = (TerraBuildFolderInfo) o;
        return folderId == that.folderId && Objects.equals(folderUuid, that.folderUuid) && Objects.equals(folderName, that.folderName);
    }
    @Override public int hashCode() { return Objects.hash(folderId, folderUuid, folderName); }

    @Override
    public String toString() {
        return "TerraBuildFolderInfo{folderId=" + folderId + ", folderUuid='" + folderUuid + '\'' + ", folderName='" + folderName + '\'' + '}';
    }
}
