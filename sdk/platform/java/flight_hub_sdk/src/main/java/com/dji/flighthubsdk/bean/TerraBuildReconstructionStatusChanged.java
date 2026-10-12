// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** Terra Build 重建状态变更通知 */
public final class TerraBuildReconstructionStatusChanged {
    private String workspaceId;
    private long id;
    private String name;
    private int state;
    private String uuid;
    private int fileType;
    private int subFileType;
    private int code;
    private long size;
    private TerraBuildJobInfo jobInfo;
    private List<TerraBuildJobInfo> jobInfos;
    private int fastReconstructionId;
    public TerraBuildReconstructionStatusChanged() {
    }

/**
     * 构造 TerraBuildReconstructionStatusChanged。
     *
     * @param workspaceId 工作空间 ID
     * @param id ID
     * @param name 任务名称过滤（模糊搜索）
     * @param state 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点}
     * @param uuid 任务 UUID
     * @param fileType 文件类型。枚举："tif", "b3dm", "pnts"
     * @param subFileType 子文件类型
     * @param code 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时
     * @param size 文件大小
     * @param jobInfo 作业信息
     * @param jobInfos 作业Infos
     * @param fastReconstructionId 快速重建ID
     */
    public TerraBuildReconstructionStatusChanged(
            String workspaceId, long id, String name, int state, String uuid,
            int fileType, int subFileType, int code, long size,
            TerraBuildJobInfo jobInfo, List<TerraBuildJobInfo> jobInfos,
            int fastReconstructionId) {
        this.workspaceId = workspaceId != null ? workspaceId : "";
        this.id = id;
        this.name = name != null ? name : "";
        this.state = state;
        this.uuid = uuid != null ? uuid : "";
        this.fileType = fileType;
        this.subFileType = subFileType;
        this.code = code;
        this.size = size;
        this.jobInfo = jobInfo;
        this.jobInfos = jobInfos != null ? jobInfos : Collections.emptyList();
        this.fastReconstructionId = fastReconstructionId;
    }

    /** 工作空间 ID */
    public String getWorkspaceId() { return workspaceId; }
    /** ID */
    public long getId() { return id; }
    /** 任务名称过滤（模糊搜索） */
    public String getName() { return name; }
    /** 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点} */
    public int getState() { return state; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 文件类型。枚举："tif", "b3dm", "pnts" */
    public int getFileType() { return fileType; }
    /** 子文件类型 */
    public int getSubFileType() { return subFileType; }
    /** 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时 */
    public int getCode() { return code; }
    /** 文件大小 */
    public long getSize() { return size; }
    /** 作业信息 */
    public TerraBuildJobInfo getJobInfo() { return jobInfo; }
    /** 作业Infos */
    public List<TerraBuildJobInfo> getJobInfos() { return jobInfos; }
    /** 快速重建ID */
    public int getFastReconstructionId() { return fastReconstructionId; }

    /** 工作空间 ID */
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    /** ID */
    public void setId(long id) {
        this.id = id;
    }

    /** 任务名称过滤（模糊搜索） */
    public void setName(String name) {
        this.name = name;
    }

    /** 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点} */
    public void setState(int state) {
        this.state = state;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 文件类型。枚举："tif", "b3dm", "pnts" */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    /** 子文件类型 */
    public void setSubFileType(int subFileType) {
        this.subFileType = subFileType;
    }

    /** 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时 */
    public void setCode(int code) {
        this.code = code;
    }

    /** 文件大小 */
    public void setSize(long size) {
        this.size = size;
    }

    /** 作业信息 */
    public void setJobInfo(TerraBuildJobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    /** 作业Infos */
    public void setJobInfos(List<TerraBuildJobInfo> jobInfos) {
        this.jobInfos = jobInfos;
    }

    /** 快速重建ID */
    public void setFastReconstructionId(int fastReconstructionId) {
        this.fastReconstructionId = fastReconstructionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildReconstructionStatusChanged that = (TerraBuildReconstructionStatusChanged) o;
        return Objects.equals(workspaceId, that.workspaceId)
                && id == that.id
                && Objects.equals(name, that.name)
                && state == that.state
                && Objects.equals(uuid, that.uuid)
                && fileType == that.fileType
                && subFileType == that.subFileType
                && code == that.code
                && size == that.size
                && Objects.equals(jobInfo, that.jobInfo)
                && Objects.equals(jobInfos, that.jobInfos)
                && fastReconstructionId == that.fastReconstructionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                workspaceId,
                id,
                name,
                state,
                uuid,
                fileType,
                subFileType,
                code,
                size,
                jobInfo,
                jobInfos,
                fastReconstructionId);
    }

    @Override
    public String toString() {
        return "TerraBuildReconstructionStatusChanged{"
                + "workspaceId='" + workspaceId + '\''
                + ", id=" + id
                + ", name='" + name + '\''
                + ", state=" + state
                + ", uuid='" + uuid + '\''
                + ", fileType=" + fileType
                + ", subFileType=" + subFileType
                + ", code=" + code
                + ", size=" + size
                + ", jobInfo=" + jobInfo
                + ", jobInfos=" + jobInfos
                + ", fastReconstructionId=" + fastReconstructionId
                + '}';
    }
}
