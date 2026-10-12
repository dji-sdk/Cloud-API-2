// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备同步状态结果。 */
public final class DeviceSyncStatusResults {
    private String sn;
    private String status;
    private int reason;
    public DeviceSyncStatusResults() {
    }

/**
     * 构造 DeviceSyncStatusResults。
     *
     * @param sn 设备序列号
     * @param status 同步状态。枚举："waiting"=等待同步, "synchronizing"=同步中, "synchronized"=已同步, "fail"=同步失败
     * @param reason 失败原因码。枚举：0=无错误(成功), 1=解析云端返回的文件信息失败, 2=获取飞行器端文件信息失败, 3=从云端下载文件失败, 4=链路翻转失败, 5=传输文件失败, 6=disable失败, 7=自定义飞行区删除失败, 8=飞行器端加载作业区域数据失败, 9=enable失败, 10=机场增强图传无法关闭，作业区域数据同步失败, 11=飞行器开机失败，无法同步作业区域数据, 12=checksum校验失败, 13=同步异常超时, 255=未知错误(机场侧快速起飞，等不急司空返回结果，主动上报255)
     */
    public DeviceSyncStatusResults(String sn, String status, int reason) {
        this.sn = sn != null ? sn : "";
        this.status = status != null ? status : "";
        this.reason = reason;
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 同步状态。枚举："waiting"=等待同步, "synchronizing"=同步中, "synchronized"=已同步, "fail"=同步失败 */
    public String getStatus() {
        return status;
    }

    /** 同步状态。枚举："waiting"=等待同步, "synchronizing"=同步中, "synchronized"=已同步, "fail"=同步失败 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 失败原因码
     *  枚举
     *    0=无错误(成功),
     *    1=解析云端返回的文件信息失败,
     *    2=获取飞行器端文件信息失败,
     *    3=从云端下载文件失败,
     *    4=链路翻转失败,
     *    5=传输文件失败,
     *    6=disable失败,
     *    7=自定义飞行区删除失败,
     *    8=飞行器端加载作业区域数据失败,
     *    9=enable失败,
     *    10=机场增强图传无法关闭，作业区域数据同步失败,
     *    11=飞行器开机失败，无法同步作业区域数据,
     *    12=checksum校验失败,
     *    13=同步异常超时,
     *    255=未知错误(机场侧快速起飞，等不急司空返回结果，主动上报255)
     */
    public int getReason() {
        return reason;
    }

    /** 失败原因码
     *  枚举
     *    0=无错误(成功),
     *    1=解析云端返回的文件信息失败,
     *    2=获取飞行器端文件信息失败,
     *    3=从云端下载文件失败,
     *    4=链路翻转失败,
     *    5=传输文件失败,
     *    6=disable失败,
     *    7=自定义飞行区删除失败,
     *    8=飞行器端加载作业区域数据失败,
     *    9=enable失败,
     *    10=机场增强图传无法关闭，作业区域数据同步失败,
     *    11=飞行器开机失败，无法同步作业区域数据,
     *    12=checksum校验失败,
     *    13=同步异常超时,
     *    255=未知错误(机场侧快速起飞，等不急司空返回结果，主动上报255)
     */
    public void setReason(int reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceSyncStatusResults that = (DeviceSyncStatusResults) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(status, that.status)
                && reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, status, reason);
    }

    @Override
    public String toString() {
        return "DeviceSyncStatusResults{sn='" + sn + '\'' + ", status='" + status + '\'' + ", reason=" + reason + '}';
    }
}
