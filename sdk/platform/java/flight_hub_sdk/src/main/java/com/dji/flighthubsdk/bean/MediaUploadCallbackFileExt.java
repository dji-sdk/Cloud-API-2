// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传回调通知中的文件扩展信息。
 */
public final class MediaUploadCallbackFileExt {
    private String prjUuid;
    private String workspaceId;
    private String flightId;
    private String droneModelKey;
    private String payloadModelKey;
    private boolean isOriginal;
    private String userId;
    private String waylineId;
    private String tinnyFingerprint;
    private int fastReconstructionMediaType;
    private long mediaIndex;
    private String waylineActionUuid;
    private String taskName;
    private String sn;
    private int expectedFileCount;
    private int panoTotalCount;
    public MediaUploadCallbackFileExt() {
    }

/**
     * 构造 MediaUploadCallbackFileExt。
     *
     * @param prjUuid 项目 UUID
     * @param workspaceId 工作空间 ID
     * @param flightId 飞行任务 ID
     * @param droneModelKey 无人机型号 Key
     * @param payloadModelKey 负载型号 Key
     * @param isOriginal 是否为原始文件
     * @param userId 用户 ID
     * @param waylineId 航线 ID
     * @param tinnyFingerprint 微缩指纹
     * @param fastReconstructionMediaType 快速重建媒体类型
     * @param mediaIndex 媒体索引
     */
    public MediaUploadCallbackFileExt(
            String prjUuid,
            String workspaceId,
            String flightId,
            String droneModelKey,
            String payloadModelKey,
            boolean isOriginal,
            String userId,
            String waylineId,
            String tinnyFingerprint,
            int fastReconstructionMediaType,
            long mediaIndex) {
        this(prjUuid, workspaceId, flightId, droneModelKey, payloadModelKey, isOriginal, userId,
                waylineId, tinnyFingerprint, fastReconstructionMediaType, mediaIndex,
                "", "", "", 0, 0);
    }

    /**
     * 构造 MediaUploadCallbackFileExt（含新增字段）。
     *
     * @param prjUuid 项目 UUID
     * @param workspaceId 工作空间 ID
     * @param flightId 飞行任务 ID
     * @param droneModelKey 无人机型号 Key
     * @param payloadModelKey 负载型号 Key
     * @param isOriginal 是否为原始文件
     * @param userId 用户 ID
     * @param waylineId 航线 ID
     * @param tinnyFingerprint 微缩指纹
     * @param fastReconstructionMediaType 快速重建媒体类型
     * @param mediaIndex 媒体索引
     * @param waylineActionUuid 航线动作 UUID
     * @param taskName 任务名称
     * @param sn 设备序列号
     * @param expectedFileCount 预期文件总数
     * @param panoTotalCount 全景总数
     */
    public MediaUploadCallbackFileExt(
            String prjUuid,
            String workspaceId,
            String flightId,
            String droneModelKey,
            String payloadModelKey,
            boolean isOriginal,
            String userId,
            String waylineId,
            String tinnyFingerprint,
            int fastReconstructionMediaType,
            long mediaIndex,
            String waylineActionUuid,
            String taskName,
            String sn,
            int expectedFileCount,
            int panoTotalCount) {
        this.prjUuid = prjUuid != null ? prjUuid : "";
        this.workspaceId = workspaceId != null ? workspaceId : "";
        this.flightId = flightId != null ? flightId : "";
        this.droneModelKey = droneModelKey != null ? droneModelKey : "";
        this.payloadModelKey = payloadModelKey != null ? payloadModelKey : "";
        this.isOriginal = isOriginal;
        this.userId = userId != null ? userId : "";
        this.waylineId = waylineId != null ? waylineId : "";
        this.tinnyFingerprint = tinnyFingerprint != null ? tinnyFingerprint : "";
        this.fastReconstructionMediaType = fastReconstructionMediaType;
        this.mediaIndex = mediaIndex;
        this.waylineActionUuid = waylineActionUuid != null ? waylineActionUuid : "";
        this.taskName = taskName != null ? taskName : "";
        this.sn = sn != null ? sn : "";
        this.expectedFileCount = expectedFileCount;
        this.panoTotalCount = panoTotalCount;
    }

    /** 项目 UUID */
    public String getPrjUuid() { return prjUuid; }

    /** 工作空间 ID */
    public String getWorkspaceId() { return workspaceId; }

    /** 飞行任务 ID */
    public String getFlightId() { return flightId; }

    /** 无人机型号 Key */
    public String getDroneModelKey() { return droneModelKey; }

    /** 负载型号 Key */
    public String getPayloadModelKey() { return payloadModelKey; }

    /** 是否原始文件 */
    public boolean isOriginal() { return isOriginal; }

    /** 用户 ID */
    public String getUserId() { return userId; }

    /** 航线 ID */
    public String getWaylineId() { return waylineId; }

    /** 微缩指纹 */
    public String getTinnyFingerprint() { return tinnyFingerprint; }

    /** 快速重建媒体类型 */
    public int getFastReconstructionMediaType() { return fastReconstructionMediaType; }

    /** 媒体索引 */
    public long getMediaIndex() { return mediaIndex; }

    /** 项目 UUID */
    public void setPrjUuid(String prjUuid) {
        this.prjUuid = prjUuid;
    }

    /** 工作空间 ID */
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    /** 飞行任务 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 无人机型号 Key */
    public void setDroneModelKey(String droneModelKey) {
        this.droneModelKey = droneModelKey;
    }

    /** 负载型号 Key */
    public void setPayloadModelKey(String payloadModelKey) {
        this.payloadModelKey = payloadModelKey;
    }

    /** 是否原始文件 */
    public void setOriginal(boolean original) {
        this.isOriginal = original;
    }

    /** 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** 航线 ID */
    public void setWaylineId(String waylineId) {
        this.waylineId = waylineId;
    }

    /** 微缩指纹 */
    public void setTinnyFingerprint(String tinnyFingerprint) {
        this.tinnyFingerprint = tinnyFingerprint;
    }

    /** 快速重建媒体类型 */
    public void setFastReconstructionMediaType(int fastReconstructionMediaType) {
        this.fastReconstructionMediaType = fastReconstructionMediaType;
    }

    /** 媒体索引 */
    public void setMediaIndex(long mediaIndex) {
        this.mediaIndex = mediaIndex;
    }

    /** 航线动作 UUID */
    public String getWaylineActionUuid() { return waylineActionUuid; }

    /** 航线动作 UUID */
    public void setWaylineActionUuid(String waylineActionUuid) {
        this.waylineActionUuid = waylineActionUuid;
    }

    /** 任务名称 */
    public String getTaskName() { return taskName; }

    /** 任务名称 */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /** 设备序列号 */
    public String getSn() { return sn; }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 预期文件总数 */
    public int getExpectedFileCount() { return expectedFileCount; }

    /** 预期文件总数 */
    public void setExpectedFileCount(int expectedFileCount) {
        this.expectedFileCount = expectedFileCount;
    }

    /** 全景总数 */
    public int getPanoTotalCount() { return panoTotalCount; }

    /** 全景总数 */
    public void setPanoTotalCount(int panoTotalCount) {
        this.panoTotalCount = panoTotalCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadCallbackFileExt that = (MediaUploadCallbackFileExt) o;
        return isOriginal == that.isOriginal
                && fastReconstructionMediaType == that.fastReconstructionMediaType
                && mediaIndex == that.mediaIndex
                && expectedFileCount == that.expectedFileCount
                && panoTotalCount == that.panoTotalCount
                && Objects.equals(prjUuid, that.prjUuid)
                && Objects.equals(workspaceId, that.workspaceId)
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(droneModelKey, that.droneModelKey)
                && Objects.equals(payloadModelKey, that.payloadModelKey)
                && Objects.equals(userId, that.userId)
                && Objects.equals(waylineId, that.waylineId)
                && Objects.equals(tinnyFingerprint, that.tinnyFingerprint)
                && Objects.equals(waylineActionUuid, that.waylineActionUuid)
                && Objects.equals(taskName, that.taskName)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prjUuid, workspaceId, flightId, droneModelKey, payloadModelKey,
                isOriginal, userId, waylineId, tinnyFingerprint, fastReconstructionMediaType, mediaIndex,
                waylineActionUuid, taskName, sn, expectedFileCount, panoTotalCount);
    }

    @Override
    public String toString() {
        return "MediaUploadCallbackFileExt{"
                + "prjUuid='" + prjUuid + '\''
                + ", workspaceId='" + workspaceId + '\''
                + ", flightId='" + flightId + '\''
                + ", droneModelKey='" + droneModelKey + '\''
                + ", payloadModelKey='" + payloadModelKey + '\''
                + ", isOriginal=" + isOriginal
                + ", userId='" + userId + '\''
                + ", waylineId='" + waylineId + '\''
                + ", waylineActionUuid='" + waylineActionUuid + '\''
                + ", taskName='" + taskName + '\''
                + ", sn='" + sn + '\''
                + ", tinnyFingerprint='" + tinnyFingerprint + '\''
                + ", fastReconstructionMediaType=" + fastReconstructionMediaType
                + ", mediaIndex=" + mediaIndex
                + ", expectedFileCount=" + expectedFileCount
                + ", panoTotalCount=" + panoTotalCount
                + '}';
    }
}
