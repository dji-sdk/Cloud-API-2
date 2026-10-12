// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 飞机当前算法状态信息 */
public class AIInsideModelInfo {
    /** 飞机当前装载的算法签名 ID */
    private String signedId = "";
    /** 飞机当前装载的算法显示名称 */
    private String modelName = "";
    /** 飞机当前装载的算法签名时间戳（Unix 秒） */
    private long signedUtc;
    /** 飞机当前装载的算法过期时间戳（Unix 秒） */
    private long expiredUtc;
    /** 算法装载状态：latest/updatable/not_loaded/expired/not_supported */
    private String status = "";

    public AIInsideModelInfo() {}

    /** 飞机当前装载的算法签名 ID */
    public String getSignedId() { return signedId; }

    /** @param v 飞机当前装载的算法签名 ID */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }

    /** 飞机当前装载的算法显示名称 */
    public String getModelName() { return modelName; }

    /** @param v 飞机当前装载的算法显示名称 */
    public void setModelName(String v) { this.modelName = v != null ? v : ""; }

    /** 飞机当前装载的算法签名时间戳（Unix 秒） */
    public long getSignedUtc() { return signedUtc; }

    /** @param v 飞机当前装载的算法签名时间戳（Unix 秒） */
    public void setSignedUtc(long v) { this.signedUtc = v; }

    /** 飞机当前装载的算法过期时间戳（Unix 秒） */
    public long getExpiredUtc() { return expiredUtc; }

    /** @param v 飞机当前装载的算法过期时间戳（Unix 秒） */
    public void setExpiredUtc(long v) { this.expiredUtc = v; }

    /** 算法装载状态：latest/updatable/not_loaded/expired/not_supported */
    public String getStatus() { return status; }

    /** @param status 算法装载状态：latest/updatable/not_loaded/expired/not_supported */
    public void setStatus(String v) { this.status = v != null ? v : ""; }
}
