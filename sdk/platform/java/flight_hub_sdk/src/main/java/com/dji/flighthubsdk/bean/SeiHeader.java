// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** SEI 头部信息 */
public class SeiHeader {
    /** 协议版本号 */
    private int version = 1;
    /** 保留字段 */
    private int reserved;
    /** 数据大小（字节），编码时自动计算 */
    private int dataSize;
    /** 时间戳，单位：毫秒 */
    private long timestamp;

    /**
     * 构造 SeiHeader。
     */
    public SeiHeader() {
    }

/**
     * 构造 SeiHeader。
     *
     * @param version 协议版本号
     * @param reserved 保留字段
     * @param dataSize 数据大小（字节），编码时自动计算
     * @param timestamp 时间戳，单位：毫秒
     */
    public SeiHeader(int version, int reserved, int dataSize, long timestamp) {
        this.version = version;
        this.reserved = reserved;
        this.dataSize = dataSize;
        this.timestamp = timestamp;
    }

    /** 协议版本号 */
    public int getVersion() { return version; }
    /** @param version 协议版本号 */
    public void setVersion(int version) { this.version = version; }
    /** 保留字段 */
    public int getReserved() { return reserved; }
    /** @param reserved 保留字段 */
    public void setReserved(int reserved) { this.reserved = reserved; }
    /** 数据大小（字节），编码时自动计算 */
    public int getDataSize() { return dataSize; }
    /** @param dataSize 数据大小（字节），编码时自动计算 */
    public void setDataSize(int dataSize) { this.dataSize = dataSize; }
    /** 时间戳，单位：毫秒 */
    public long getTimestamp() { return timestamp; }
    /** @param timestamp 时间戳，单位：毫秒 */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiHeader that = (SeiHeader) o;
        return version == that.version
                && reserved == that.reserved
                && dataSize == that.dataSize
                && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, reserved, dataSize, timestamp);
    }

    @Override
    public String toString() {
        return "SeiHeader{"
                + "version=" + version
                + ", reserved=" + reserved
                + ", dataSize=" + dataSize
                + ", timestamp=" + timestamp
                + '}';
    }
}
