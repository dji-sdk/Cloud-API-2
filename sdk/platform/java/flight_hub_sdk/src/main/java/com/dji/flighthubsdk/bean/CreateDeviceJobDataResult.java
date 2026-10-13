// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建设备作业数据结果。 */
public final class CreateDeviceJobDataResult {
    /** 业务标识(bid) */
    private String bid;
    /** 设备 SN */
    private String sn;
    /** 业务编码 */
    private String bizCode;
    /** 任务状态 */
    private String status;
    /** 结果码 */
    private int result;
    /** 进度信息 */
    private CreateDeviceJobProgressResult progress;
    /** 服务标识 */
    private String serviceIdentifier;
    public CreateDeviceJobDataResult() {
    }

/**
     * 构造 CreateDeviceJobDataResult。
     *
     * @param bid 业务标识
     * @param sn 设备序列号
     * @param bizCode 业务编码
     * @param status 状态
     * @param result 结果
     * @param progress 进度
     * @param serviceIdentifier 服务标识
     */
    public CreateDeviceJobDataResult(
            String bid,
            String sn,
            String bizCode,
            String status,
            int result,
            CreateDeviceJobProgressResult progress,
            String serviceIdentifier) {
        this.bid = bid != null ? bid : "";
        this.sn = sn != null ? sn : "";
        this.bizCode = bizCode != null ? bizCode : "";
        this.status = status != null ? status : "";
        this.result = result;
        this.progress = progress;
        this.serviceIdentifier = serviceIdentifier != null ? serviceIdentifier : "";
    }

    /** 业务标识(bid) */
    public String getBid() {
        return bid;
    }

    /** @param bid 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 设备 SN */
    public String getSn() {
        return sn;
    }

    /** @param sn 设备 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 业务编码 */
    public String getBizCode() {
        return bizCode;
    }

    /** 业务编码 */
    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    /** 任务状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 任务状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 结果码 */
    public int getResult() {
        return result;
    }

    /** @param result 结果码 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 进度信息 */
    public CreateDeviceJobProgressResult getProgress() {
        return progress;
    }

    /** @param progress 进度信息 */
    public void setProgress(CreateDeviceJobProgressResult progress) {
        this.progress = progress;
    }

    /** 服务标识 */
    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    /** 服务标识 */
    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateDeviceJobDataResult that = (CreateDeviceJobDataResult) o;
        return Objects.equals(bid, that.bid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(bizCode, that.bizCode)
                && Objects.equals(status, that.status)
                && result == that.result
                && Objects.equals(progress, that.progress)
                && Objects.equals(serviceIdentifier, that.serviceIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                bid,
                sn,
                bizCode,
                status,
                result,
                progress,
                serviceIdentifier);
    }

    @Override
    public String toString() {
        return "CreateDeviceJobDataResult{"
                + "bid='" + bid + '\''
                + ", sn='" + sn + '\''
                + ", bizCode='" + bizCode + '\''
                + ", status='" + status + '\''
                + ", result=" + result
                + ", progress=" + progress
                + ", serviceIdentifier='" + serviceIdentifier + '\''
                + '}';
    }
}
