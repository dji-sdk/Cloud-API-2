// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** Terra Build 模型文件列表项 */
public final class TerraBuildModelFileItem {
    private int id;
    private String uuid;
    private String name;
    private int fileType;
    private int subFileType;
    private int state;
    private List<Integer> reconstructionType;
    private String thumbnailUrl;
    private String zipUrl;
    private String createdAt;
    private String updatedAt;
    public TerraBuildModelFileItem() {
    }

/**
     * 构造 TerraBuildModelFileItem。
     *
     * @param id ID
     * @param uuid 任务 UUID
     * @param name 任务名称过滤（模糊搜索）
     * @param fileType 文件类型。枚举："tif", "b3dm", "pnts"
     * @param subFileType 子文件类型
     * @param state 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点}
     * @param reconstructionType 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
     * @param thumbnailUrl 缩略图 URL
     * @param zipUrl 压缩包 URL
     * @param createdAt 创建时间（ISO 8601）
     * @param updatedAt 更新时间（ISO 8601）
     */
    public TerraBuildModelFileItem(
            int id, String uuid, String name, int fileType, int subFileType, int state,
            List<Integer> reconstructionType, String thumbnailUrl, String zipUrl,
            String createdAt, String updatedAt) {
        this.id = id;
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.fileType = fileType;
        this.subFileType = subFileType;
        this.state = state;
        this.reconstructionType = reconstructionType != null ? reconstructionType : Collections.emptyList();
        this.thumbnailUrl = thumbnailUrl != null ? thumbnailUrl : "";
        this.zipUrl = zipUrl != null ? zipUrl : "";
        this.createdAt = createdAt != null ? createdAt : "";
        this.updatedAt = updatedAt != null ? updatedAt : "";
    }

    /** ID */
    public int getId() { return id; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 任务名称过滤（模糊搜索） */
    public String getName() { return name; }
    /** 文件类型。枚举："tif", "b3dm", "pnts" */
    public int getFileType() { return fileType; }
    /** 子文件类型 */
    public int getSubFileType() { return subFileType; }
    /** 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点} */
    public int getState() { return state; }
    /** 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达 */
    public List<Integer> getReconstructionType() { return reconstructionType; }
    /** 缩略图 URL */
    public String getThumbnailUrl() { return thumbnailUrl; }
    /** 压缩包 URL */
    public String getZipUrl() { return zipUrl; }
    /** 创建时间（ISO 8601） */
    public String getCreatedAt() { return createdAt; }
    /** 更新时间（ISO 8601） */
    public String getUpdatedAt() { return updatedAt; }

    /** ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 任务名称过滤（模糊搜索） */
    public void setName(String name) {
        this.name = name;
    }

    /** 文件类型。枚举："tif", "b3dm", "pnts" */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    /** 子文件类型 */
    public void setSubFileType(int subFileType) {
        this.subFileType = subFileType;
    }

    /** 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点} */
    public void setState(int state) {
        this.state = state;
    }

    /** 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达 */
    public void setReconstructionType(List<Integer> reconstructionType) {
        this.reconstructionType = reconstructionType;
    }

    /** 缩略图 URL */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /** 压缩包 URL */
    public void setZipUrl(String zipUrl) {
        this.zipUrl = zipUrl;
    }

    /** 创建时间（ISO 8601） */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** 更新时间（ISO 8601） */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildModelFileItem that = (TerraBuildModelFileItem) o;
        return id == that.id
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && fileType == that.fileType
                && subFileType == that.subFileType
                && state == that.state
                && Objects.equals(reconstructionType, that.reconstructionType)
                && Objects.equals(thumbnailUrl, that.thumbnailUrl)
                && Objects.equals(zipUrl, that.zipUrl)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                uuid,
                name,
                fileType,
                subFileType,
                state,
                reconstructionType,
                thumbnailUrl,
                zipUrl,
                createdAt,
                updatedAt);
    }

    @Override
    public String toString() {
        return "TerraBuildModelFileItem{"
                + "id=" + id
                + ", uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", fileType=" + fileType
                + ", subFileType=" + subFileType
                + ", state=" + state
                + ", reconstructionType=" + reconstructionType
                + ", thumbnailUrl='" + thumbnailUrl + '\''
                + ", zipUrl='" + zipUrl + '\''
                + ", createdAt='" + createdAt + '\''
                + ", updatedAt='" + updatedAt + '\''
                + '}';
    }
}
