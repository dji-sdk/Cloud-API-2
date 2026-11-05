// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 全景拼接进度推送消息。
 *
 * <p>链路：云端媒体服务（PaaS 分流）→ 云端通知服务（topic: topic_pano_stitch_progress）
 * → NCSDK ListenPanoStitchProgress → {@link com.dji.flighthubsdk.FlightHubCallback.PanoStitchProgressCallback}。</p>
 *
 * <p>状态含义（与 C++ PanoStitchState 枚举数值一致）：</p>
 * <ul>
 *   <li>0 = 未处理</li>
 *   <li>1 = 等待资源</li>
 *   <li>2 = 处理中</li>
 *   <li>3 = 成功</li>
 *   <li>4 = 失败（可重试）</li>
 *   <li>5 = 取消（可重试）</li>
 *   <li>6 = 余额不足（可重试）</li>
 * </ul>
 */
public final class PanoStitchProgressMsg {

    /** 工作空间 UUID（ISV 可据此过滤） */
    private String workspaceId;
    /** 成片文件 ID */
    private long fileId;
    /** 成片文件名 */
    private String name;
    /** 拼接状态（0~6，见 PanoStitchState 枚举） */
    private int panoStitchState;
    /** 拼接进度 [0, 100] */
    private int panoStitchProgress;
    /** 错误码（0 = 无错误） */
    private int panoErrorCode;

    public PanoStitchProgressMsg() {
        this.workspaceId = "";
        this.name = "";
    }

    /** 工作空间 UUID */
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId != null ? workspaceId : ""; }

    /** 成片文件 ID */
    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId; }

    /** 成片文件名 */
    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** 拼接状态（0~6，与 PanoStitchState 枚举一致） */
    public int getPanoStitchState() { return panoStitchState; }
    public void setPanoStitchState(int panoStitchState) { this.panoStitchState = panoStitchState; }

    /** 拼接进度 [0, 100] */
    public int getPanoStitchProgress() { return panoStitchProgress; }
    public void setPanoStitchProgress(int panoStitchProgress) { this.panoStitchProgress = panoStitchProgress; }

    /** 错误码（0 = 无错误） */
    public int getPanoErrorCode() { return panoErrorCode; }
    public void setPanoErrorCode(int panoErrorCode) { this.panoErrorCode = panoErrorCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanoStitchProgressMsg that = (PanoStitchProgressMsg) o;
        return fileId == that.fileId
                && panoStitchState == that.panoStitchState
                && panoStitchProgress == that.panoStitchProgress;
    }

    @Override
    public int hashCode() { return Objects.hash(fileId, panoStitchState, panoStitchProgress); }

    @Override
    public String toString() {
        return "PanoStitchProgressMsg{fileId=" + fileId
                + ", state=" + panoStitchState
                + ", progress=" + panoStitchProgress
                + ", errorCode=" + panoErrorCode + '}';
    }
}
