// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 信息结果 */
public final class AIBoxInfoResult {
    private int index;
    private int type;
    private String sn;
    private String firmwareVersion;
    private double storageTotal;
    private double storageUsed;
    public AIBoxInfoResult() {
    }

/**
     * 构造 AIBoxInfoResult。
     *
     * @param index 索引
     * @param type 类型
     * @param sn 序列号
     * @param firmwareVersion 固件版本
     * @param storageTotal 总存储容量
     * @param storageUsed 已使用存储容量
     */
    public AIBoxInfoResult(int index, int type, String sn, String firmwareVersion, double storageTotal, double storageUsed) {
        this.index = index;
        this.type = type;
        this.sn = sn != null ? sn : "";
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.storageTotal = storageTotal;
        this.storageUsed = storageUsed;
    }

    /** 索引 */
    public int getIndex() { return index; }
    /** 类型 */
    public int getType() { return type; }
    /** 序列号 */
    public String getSn() { return sn; }
    /** 固件版本 */
    public String getFirmwareVersion() { return firmwareVersion; }
    /** 总存储容量 */
    public double getStorageTotal() { return storageTotal; }
    /** 已使用存储容量 */
    public double getStorageUsed() { return storageUsed; }

    /** 索引 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** 类型 */
    public void setType(int type) {
        this.type = type;
    }

    /** 序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 固件版本 */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /** 总存储容量 */
    public void setStorageTotal(double storageTotal) {
        this.storageTotal = storageTotal;
    }

    /** 已使用存储容量 */
    public void setStorageUsed(double storageUsed) {
        this.storageUsed = storageUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxInfoResult that = (AIBoxInfoResult) o;
        return index == that.index
                && type == that.type
                && Objects.equals(sn, that.sn)
                && Objects.equals(firmwareVersion, that.firmwareVersion)
                && Double.compare(that.storageTotal, storageTotal) == 0
                && Double.compare(that.storageUsed, storageUsed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                index,
                type,
                sn,
                firmwareVersion,
                storageTotal,
                storageUsed);
    }

    @Override
    public String toString() {
        return "AIBoxInfoResult{"
                + "index=" + index
                + ", type=" + type
                + ", sn='" + sn + '\''
                + ", firmwareVersion='" + firmwareVersion + '\''
                + ", storageTotal=" + storageTotal
                + ", storageUsed=" + storageUsed
                + '}';
    }
}
