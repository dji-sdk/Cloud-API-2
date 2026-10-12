// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 定损报告 - 查询单条定损报告详情结果 */
public final class GetSelfDiagnosisReportResults {
    /** 定损报告唯一标识（UUID）; */
    private String reportUuid;
    /** 发起定损操作的用户 ID; */
    private String reportUserId;
    /** 发起人在本组织中的呼号（昵称），由 organization_member 表查询得出; */
    private String reportUserOrganizationCallsign;
    /** 报告创建时间（Unix 秒时间戳）; */
    private long createTime;
    /** 整机 SN（铭牌 SN / 主控 SN）; */
    private String completeMachineSn;
    private String deviceSn;
    /** 设备型号标识; */
    private String deviceModelKey;
    /** 设备在本组织中的昵称 / 呼号，由 organization_device 表查询得出; */
    private String deviceOrganizationCallsign;
    /** 定损报告当前状态, {"pre_commit", "in_progress", "ok", "canceled", "timeout", "failed"}; */
    private String status;
    /** 定损结果码(由设备上报)。0 表示成功, 210098 表示超时，其它值为设备定义的错误码; */
    private int resultCode;
    /** 批次 ID，用于关联同一批次的多条报告，也用于向 DTCP 查询实时进度; */
    private String bid;
    /** 定损部件类型。0=整机，1=电池，10=单机业务定损; */
    private int part;
    /** 定损来源。0=FH2，1=NeptuneApp; */
    private int origin;
    private SelfDiagnosticResultData selfDiagnosticResult;
    private DiagnosisDeviceReportProgress deviceReportProgress;
    private String qrCodeContent;
    private String qrGuideText;
    public GetSelfDiagnosisReportResults() {
    }

/**
     * 构造 GetSelfDiagnosisReportResults。
     *
     * @param reportUuid 报告 UUID
     * @param reportUserId 报告用户 ID
     * @param reportUserOrganizationCallsign 报告用户组织呼号
     * @param createTime 创建时间
     * @param completeMachineSn 整机序列号
     * @param deviceSn 设备序列号
     * @param deviceModelKey 设备型号 Key
     * @param deviceOrganizationCallsign 设备组织呼号
     * @param status 状态
     * @param resultCode 结果码
     * @param bid 业务 ID
     * @param part 部件
     * @param origin 来源
     * @param selfDiagnosticResult 自诊断结果数据
     * @param deviceReportProgress 设备报告上传进度
     * @param qrCodeContent 二维码内容
     * @param qrGuideText 二维码引导文本
     */
    public GetSelfDiagnosisReportResults(
            String reportUuid, String reportUserId, String reportUserOrganizationCallsign,
            long createTime, String completeMachineSn, String deviceSn, String deviceModelKey,
            String deviceOrganizationCallsign, String status, int resultCode, String bid,
            int part, int origin, SelfDiagnosticResultData selfDiagnosticResult,
            DiagnosisDeviceReportProgress deviceReportProgress,
            String qrCodeContent, String qrGuideText) {
        this.reportUuid = reportUuid;
        this.reportUserId = reportUserId;
        this.reportUserOrganizationCallsign = reportUserOrganizationCallsign;
        this.createTime = createTime;
        this.completeMachineSn = completeMachineSn;
        this.deviceSn = deviceSn;
        this.deviceModelKey = deviceModelKey;
        this.deviceOrganizationCallsign = deviceOrganizationCallsign;
        this.status = status;
        this.resultCode = resultCode;
        this.bid = bid;
        this.part = part;
        this.origin = origin;
        this.selfDiagnosticResult = selfDiagnosticResult;
        this.deviceReportProgress = deviceReportProgress;
        this.qrCodeContent = qrCodeContent;
        this.qrGuideText = qrGuideText;
    }

    /** 定损报告唯一标识（UUID）; */
    public String getReportUuid() { return reportUuid; }
    /** 发起定损操作的用户 ID; */
    public String getReportUserId() { return reportUserId; }
    /** 发起人在本组织中的呼号（昵称），由 organization_member 表查询得出; */
    public String getReportUserOrganizationCallsign() { return reportUserOrganizationCallsign; }
    /** 报告创建时间（Unix 秒时间戳）; */
    public long getCreateTime() { return createTime; }
    /** 整机 SN（铭牌 SN / 主控 SN）; */
    public String getCompleteMachineSn() { return completeMachineSn; }
    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** 设备型号标识; */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** 设备在本组织中的昵称 / 呼号，由 organization_device 表查询得出; */
    public String getDeviceOrganizationCallsign() { return deviceOrganizationCallsign; }
    /** 定损报告当前状态, {"pre_commit", "in_progress", "ok", "canceled", "timeout", "failed"}; */
    public String getStatus() { return status; }
    /** 定损结果码(由设备上报)。0 表示成功, 210098 表示超时，其它值为设备定义的错误码; */
    public int getResultCode() { return resultCode; }
    /** 批次 ID，用于关联同一批次的多条报告，也用于向 DTCP 查询实时进度; */
    public String getBid() { return bid; }
    /** 定损部件类型。0=整机，1=电池，10=单机业务定损; */
    public int getPart() { return part; }
    /** 定损来源。0=FH2，1=NeptuneApp; */
    public int getOrigin() { return origin; }
    /** 自诊断结果数据 */
    public SelfDiagnosticResultData getSelfDiagnosticResult() { return selfDiagnosticResult; }
    /** 设备报告上传进度 */
    public DiagnosisDeviceReportProgress getDeviceReportProgress() { return deviceReportProgress; }
    /** 二维码内容 */
    public String getQrCodeContent() { return qrCodeContent; }
    /** 二维码引导文本 */
    public String getQrGuideText() { return qrGuideText; }

    /** @param reportUuid 定损报告唯一标识（UUID）; */
    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid;
    }

    /** @param reportUserId 发起定损操作的用户 ID; */
    public void setReportUserId(String reportUserId) {
        this.reportUserId = reportUserId;
    }

    /** @param reportUserOrganizationCallsign 发起人在本组织中的呼号（昵称），由 organization_member 表查询得出; */
    public void setReportUserOrganizationCallsign(String reportUserOrganizationCallsign) {
        this.reportUserOrganizationCallsign = reportUserOrganizationCallsign;
    }

    /** @param createTime 报告创建时间（Unix 秒时间戳）; */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /** @param completeMachineSn 整机 SN（铭牌 SN / 主控 SN）; */
    public void setCompleteMachineSn(String completeMachineSn) {
        this.completeMachineSn = completeMachineSn;
    }

    /** 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** @param deviceModelKey 设备型号标识; */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** @param deviceOrganizationCallsign 设备在本组织中的昵称 / 呼号，由 organization_device 表查询得出; */
    public void setDeviceOrganizationCallsign(String deviceOrganizationCallsign) {
        this.deviceOrganizationCallsign = deviceOrganizationCallsign;
    }

    /** @param status 定损报告当前状态, {"pre_commit", "in_progress", "ok", "canceled", "timeout", "failed"}; */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @param resultCode 定损结果码(由设备上报)。0 表示成功, 210098 表示超时，其它值为设备定义的错误码; */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /** @param bid 批次 ID，用于关联同一批次的多条报告，也用于向 DTCP 查询实时进度; */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** @param part 定损部件类型。0=整机，1=电池，10=单机业务定损; */
    public void setPart(int part) {
        this.part = part;
    }

    /** @param origin 定损来源。0=FH2，1=NeptuneApp; */
    public void setOrigin(int origin) {
        this.origin = origin;
    }

    /** 自诊断结果数据 */
    public void setSelfDiagnosticResult(SelfDiagnosticResultData selfDiagnosticResult) {
        this.selfDiagnosticResult = selfDiagnosticResult;
    }

    /** 设备报告上传进度 */
    public void setDeviceReportProgress(DiagnosisDeviceReportProgress deviceReportProgress) {
        this.deviceReportProgress = deviceReportProgress;
    }

    /** 二维码内容 */
    public void setQrCodeContent(String qrCodeContent) {
        this.qrCodeContent = qrCodeContent;
    }

    /** 二维码引导文本 */
    public void setQrGuideText(String qrGuideText) {
        this.qrGuideText = qrGuideText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetSelfDiagnosisReportResults that = (GetSelfDiagnosisReportResults) o;
        return Objects.equals(reportUuid, that.reportUuid)
                && Objects.equals(reportUserId, that.reportUserId)
                && Objects.equals(reportUserOrganizationCallsign, that.reportUserOrganizationCallsign)
                && createTime == that.createTime
                && Objects.equals(completeMachineSn, that.completeMachineSn)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(deviceOrganizationCallsign, that.deviceOrganizationCallsign)
                && Objects.equals(status, that.status)
                && resultCode == that.resultCode
                && Objects.equals(bid, that.bid)
                && part == that.part
                && origin == that.origin
                && Objects.equals(selfDiagnosticResult, that.selfDiagnosticResult)
                && Objects.equals(deviceReportProgress, that.deviceReportProgress)
                && Objects.equals(qrCodeContent, that.qrCodeContent)
                && Objects.equals(qrGuideText, that.qrGuideText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                reportUuid,
                reportUserId,
                reportUserOrganizationCallsign,
                createTime,
                completeMachineSn,
                deviceSn,
                deviceModelKey,
                deviceOrganizationCallsign,
                status,
                resultCode,
                bid,
                part,
                origin,
                selfDiagnosticResult,
                deviceReportProgress,
                qrCodeContent,
                qrGuideText);
    }

    @Override
    public String toString() {
        return "GetSelfDiagnosisReportResults{"
                + "reportUuid='" + reportUuid + '\''
                + ", reportUserId='" + reportUserId + '\''
                + ", reportUserOrganizationCallsign='" + reportUserOrganizationCallsign + '\''
                + ", createTime=" + createTime
                + ", completeMachineSn='" + completeMachineSn + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", deviceOrganizationCallsign='" + deviceOrganizationCallsign + '\''
                + ", status='" + status + '\''
                + ", resultCode=" + resultCode
                + ", bid='" + bid + '\''
                + ", part=" + part
                + ", origin=" + origin
                + ", selfDiagnosticResult=" + selfDiagnosticResult
                + ", deviceReportProgress=" + deviceReportProgress
                + ", qrCodeContent='" + qrCodeContent + '\''
                + ", qrGuideText='" + qrGuideText + '\''
                + '}';
    }
}
