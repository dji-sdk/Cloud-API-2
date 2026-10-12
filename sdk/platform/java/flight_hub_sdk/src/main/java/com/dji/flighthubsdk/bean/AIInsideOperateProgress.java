// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 算法操作进度（对齐现网 operate_ai_model） */
public class AIInsideOperateProgress {
    /** 当前操作指令类型（"load_ai_model" / "unload_ai_model"） */
    private String deviceCmdMethod = "";
    /** 操作状态字符串（如 "ok" / "failed"） */
    private String status = "";
    /** 嵌套进度 */
    private AIInsideOperateProgressDetail progress = new AIInsideOperateProgressDetail();
    /** 设备侧错误码（0=无错） */
    private int deviceCode;
    /** 操作创建时间（Unix ms） */
    private long createTime;

    public AIInsideOperateProgress() {}

    /** 当前操作指令类型（"load_ai_model" / "unload_ai_model"） */
    public String getDeviceCmdMethod() { return deviceCmdMethod; }
    /** @param deviceCmdMethod 当前操作指令类型（"load_ai_model" / "unload_ai_model"） */
    public void setDeviceCmdMethod(String v) { this.deviceCmdMethod = v != null ? v : ""; }

    /** 操作状态字符串（如 "ok" / "failed"） */
    public String getStatus() { return status; }
    /** @param status 操作状态字符串（如 "ok" / "failed"） */
    public void setStatus(String v) { this.status = v != null ? v : ""; }

    /** 嵌套进度 */
    public AIInsideOperateProgressDetail getProgress() { return progress; }
    /** @param progress 嵌套进度 */
    public void setProgress(AIInsideOperateProgressDetail v) {
        this.progress = v != null ? v : new AIInsideOperateProgressDetail();
    }

    /** 设备侧错误码（0=无错） */
    public int getDeviceCode() { return deviceCode; }
    /** @param deviceCode 设备侧错误码（0=无错） */
    public void setDeviceCode(int v) { this.deviceCode = v; }

    /** 操作创建时间（Unix ms） */
    public long getCreateTime() { return createTime; }
    /** @param createTime 操作创建时间（Unix ms） */
    public void setCreateTime(long v) { this.createTime = v; }
}
