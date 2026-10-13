// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;

import java.util.Objects;
/** 查询单个报修订单详情返回结果 */
public final class MaintenanceOrderDetailResults {
    /** 报修订单号，格式：MO_{8位随机ID}{型号key}{毫秒时间戳}; */
    private String orderNumber;
    /** 所属组织 UUID; */
    private String organizationUuid;
    /** 创建人用户 ID（UUID）; */
    private String creatorId;
    /** 创建人昵称; */
    private String creatorName;
    /** 报修来源类型码：0=定损 2=日志反馈 3=直接报修; */
    private int sourceType;
    /** 报修来源类型文本（中文）; */
    private String sourceTypeText;
    /** 订单状态码：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消; */
    private int status;
    /** 订单状态文本（中文）; */
    private String statusText;
    /** 故障现象描述; */
    private String faultDescription;
    /** 故障发生时间（RFC3339）; */
    private String faultOccurrenceTime;
    /** 期望上门服务时间; */
    private String expectedServiceTime;
    /** 联系人姓名; */
    private String contactPerson;
    /** 联系人电话; */
    private String contactPhone;
    /** 联系人邮箱; */
    private String contactEmail;
    /** 联系地址; */
    private MaintenanceOrderContactAddress contactAddressFull;
    /** 订单创建时间; */
    private String createdAt;
    /** 订单最后更新时间; */
    private String updatedAt;
    /** 提交时间; */
    private String submissionTime;
    /** 受理时间; */
    private String acceptedTime;
    /** 完成时间; */
    private String completedTime;
    /** 瑞云工单号; */
    private String ruiyunOrderNumber;
    /** 瑞云同步状态：pending（待同步）/ synced（已同步）/ failed（同步失败）; */
    private String ruiyunSyncStatus;
    /** 设备激活 SN; */
    private String deviceSn;
    /** 设备型号 key; */
    private String deviceModelKey;
    /** 设备机型领域（如 dock、drone）; */
    private String module;
    /** 设备铭牌 SN（整机序列号）; */
    private String nameplateSn;
    /** 关联定损报告 UUID，无则为空字符串; */
    private String diagnosticId;
    /** 关联日志报告 UUID，无则为空字符串; */
    private String deviceReportId;
    /** 附件信息; */
    private List<MaintenanceOrderAttachment> attachments;
    /** 订单状态变更历史，无历史时为空数组; */
    private List<MaintenanceOrderStatusHistoryItem> statusHistory;
    /** 维修人员信息，已指派维修人员时返回; */
    private MaintenanceOrderRepairInfo repairInfo;
    /** 关联日志报告信息，日志反馈渠道且存在 device_report_id 时返回; */
    private MaintenanceOrderLogReport logReport;
    public MaintenanceOrderDetailResults() {
    }

/**
     * 构造 MaintenanceOrderDetailResults。
     *
     * @param orderNumber 工单编号
     * @param organizationUuid 组织UUID
     * @param creatorId 创建者ID
     * @param creatorName 创建者名称
     * @param sourceType 报修来源类型
     * @param sourceTypeText 报修来源类型文本
     * @param status 工单状态
     * @param statusText 工单状态文本
     * @param faultDescription 故障描述
     * @param faultOccurrenceTime 故障发生时间
     * @param expectedServiceTime 期望服务时间
     * @param contactPerson 联系人
     * @param contactPhone 联系电话
     * @param contactEmail 联系邮箱
     * @param contactAddressFull 联系地址详情
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @param submissionTime 提交时间
     * @param acceptedTime 受理时间
     * @param completedTime 完成时间
     * @param ruiyunOrderNumber 瑞云工单编号
     * @param ruiyunSyncStatus 瑞云同步状态
     * @param deviceSn 设备序列号
     * @param deviceModelKey 设备型号Key
     * @param module 模块
     * @param nameplateSn 铭牌序列号
     * @param diagnosticId 诊断ID
     * @param deviceReportId 设备报告ID
     * @param attachments 附件列表
     * @param statusHistory 状态历史记录
     * @param repairInfo 维修信息
     * @param logReport 日志报告
     */
    public MaintenanceOrderDetailResults(
            String orderNumber, String organizationUuid, String creatorId, String creatorName,
            int sourceType, String sourceTypeText, int status, String statusText,
            String faultDescription, String faultOccurrenceTime, String expectedServiceTime,
            String contactPerson, String contactPhone, String contactEmail,
            MaintenanceOrderContactAddress contactAddressFull,
            String createdAt, String updatedAt, String submissionTime, String acceptedTime, String completedTime,
            String ruiyunOrderNumber, String ruiyunSyncStatus,
            String deviceSn, String deviceModelKey, String module, String nameplateSn,
            String diagnosticId, String deviceReportId,
            List<MaintenanceOrderAttachment> attachments,
            List<MaintenanceOrderStatusHistoryItem> statusHistory,
            MaintenanceOrderRepairInfo repairInfo,
            MaintenanceOrderLogReport logReport) {
        this.orderNumber = orderNumber;
        this.organizationUuid = organizationUuid;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.sourceType = sourceType;
        this.sourceTypeText = sourceTypeText;
        this.status = status;
        this.statusText = statusText;
        this.faultDescription = faultDescription;
        this.faultOccurrenceTime = faultOccurrenceTime;
        this.expectedServiceTime = expectedServiceTime;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.contactAddressFull = contactAddressFull;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submissionTime = submissionTime;
        this.acceptedTime = acceptedTime;
        this.completedTime = completedTime;
        this.ruiyunOrderNumber = ruiyunOrderNumber;
        this.ruiyunSyncStatus = ruiyunSyncStatus;
        this.deviceSn = deviceSn;
        this.deviceModelKey = deviceModelKey;
        this.module = module;
        this.nameplateSn = nameplateSn;
        this.diagnosticId = diagnosticId;
        this.deviceReportId = deviceReportId;
        this.attachments = attachments;
        this.statusHistory = statusHistory;
        this.repairInfo = repairInfo;
        this.logReport = logReport;
    }

    /** 报修订单号，格式：MO_{8位随机ID}{型号key}{毫秒时间戳}; */
    public String getOrderNumber() { return orderNumber; }
    /** 所属组织 UUID; */
    public String getOrganizationUuid() { return organizationUuid; }
    /** 创建人用户 ID（UUID）; */
    public String getCreatorId() { return creatorId; }
    /** 创建人昵称; */
    public String getCreatorName() { return creatorName; }
    /** 报修来源类型码：0=定损 2=日志反馈 3=直接报修; */
    public int getSourceType() { return sourceType; }
    /** 报修来源类型文本（中文）; */
    public String getSourceTypeText() { return sourceTypeText; }
    /** 订单状态码：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消; */
    public int getStatus() { return status; }
    /** 订单状态文本（中文）; */
    public String getStatusText() { return statusText; }
    /** 故障现象描述; */
    public String getFaultDescription() { return faultDescription; }
    /** 故障发生时间（RFC3339）; */
    public String getFaultOccurrenceTime() { return faultOccurrenceTime; }
    /** 期望上门服务时间; */
    public String getExpectedServiceTime() { return expectedServiceTime; }
    /** 联系人姓名; */
    public String getContactPerson() { return contactPerson; }
    /** 联系人电话; */
    public String getContactPhone() { return contactPhone; }
    /** 联系人邮箱; */
    public String getContactEmail() { return contactEmail; }
    /** 联系地址; */
    public MaintenanceOrderContactAddress getContactAddressFull() { return contactAddressFull; }
    /** 订单创建时间; */
    public String getCreatedAt() { return createdAt; }
    /** 订单最后更新时间; */
    public String getUpdatedAt() { return updatedAt; }
    /** 提交时间; */
    public String getSubmissionTime() { return submissionTime; }
    /** 受理时间; */
    public String getAcceptedTime() { return acceptedTime; }
    /** 完成时间; */
    public String getCompletedTime() { return completedTime; }
    /** 瑞云工单号; */
    public String getRuiyunOrderNumber() { return ruiyunOrderNumber; }
    /** 瑞云同步状态：pending（待同步）/ synced（已同步）/ failed（同步失败）; */
    public String getRuiyunSyncStatus() { return ruiyunSyncStatus; }
    /** 设备激活 SN; */
    public String getDeviceSn() { return deviceSn; }
    /** 设备型号 key; */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** 设备机型领域（如 dock、drone）; */
    public String getModule() { return module; }
    /** 设备铭牌 SN（整机序列号）; */
    public String getNameplateSn() { return nameplateSn; }
    /** 关联定损报告 UUID，无则为空字符串; */
    public String getDiagnosticId() { return diagnosticId; }
    /** 关联日志报告 UUID，无则为空字符串; */
    public String getDeviceReportId() { return deviceReportId; }
    /** 附件信息; */
    public List<MaintenanceOrderAttachment> getAttachments() { return attachments; }
    /** 订单状态变更历史，无历史时为空数组; */
    public List<MaintenanceOrderStatusHistoryItem> getStatusHistory() { return statusHistory; }
    /** 维修人员信息，已指派维修人员时返回; */
    public MaintenanceOrderRepairInfo getRepairInfo() { return repairInfo; }
    /** 关联日志报告信息，日志反馈渠道且存在 device_report_id 时返回; */
    public MaintenanceOrderLogReport getLogReport() { return logReport; }

    /** @param orderNumber 报修订单号，格式：MO_{8位随机ID}{型号key}{毫秒时间戳}; */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /** @param organizationUuid 所属组织 UUID; */
    public void setOrganizationUuid(String organizationUuid) {
        this.organizationUuid = organizationUuid;
    }

    /** @param creatorId 创建人用户 ID（UUID）; */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /** @param creatorName 创建人昵称; */
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    /** @param sourceType 报修来源类型码：0=定损 2=日志反馈 3=直接报修; */
    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    /** @param sourceTypeText 报修来源类型文本（中文）; */
    public void setSourceTypeText(String sourceTypeText) {
        this.sourceTypeText = sourceTypeText;
    }

    /** @param status 订单状态码：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消; */
    public void setStatus(int status) {
        this.status = status;
    }

    /** @param statusText 订单状态文本（中文）; */
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    /** @param faultDescription 故障现象描述; */
    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    /** @param faultOccurrenceTime 故障发生时间（RFC3339）; */
    public void setFaultOccurrenceTime(String faultOccurrenceTime) {
        this.faultOccurrenceTime = faultOccurrenceTime;
    }

    /** @param expectedServiceTime 期望上门服务时间; */
    public void setExpectedServiceTime(String expectedServiceTime) {
        this.expectedServiceTime = expectedServiceTime;
    }

    /** @param contactPerson 联系人姓名; */
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    /** @param contactPhone 联系人电话; */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /** @param contactEmail 联系人邮箱; */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /** @param contactAddressFull 联系地址; */
    public void setContactAddressFull(MaintenanceOrderContactAddress contactAddressFull) {
        this.contactAddressFull = contactAddressFull;
    }

    /** @param createdAt 订单创建时间; */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** @param updatedAt 订单最后更新时间; */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** @param submissionTime 提交时间; */
    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    /** @param acceptedTime 受理时间; */
    public void setAcceptedTime(String acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    /** @param completedTime 完成时间; */
    public void setCompletedTime(String completedTime) {
        this.completedTime = completedTime;
    }

    /** @param ruiyunOrderNumber 瑞云工单号; */
    public void setRuiyunOrderNumber(String ruiyunOrderNumber) {
        this.ruiyunOrderNumber = ruiyunOrderNumber;
    }

    /** @param ruiyunSyncStatus 瑞云同步状态：pending（待同步）/ synced（已同步）/ failed（同步失败）; */
    public void setRuiyunSyncStatus(String ruiyunSyncStatus) {
        this.ruiyunSyncStatus = ruiyunSyncStatus;
    }

    /** @param deviceSn 设备激活 SN; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** @param deviceModelKey 设备型号 key; */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** @param module 设备机型领域（如 dock、drone）; */
    public void setModule(String module) {
        this.module = module;
    }

    /** @param nameplateSn 设备铭牌 SN（整机序列号）; */
    public void setNameplateSn(String nameplateSn) {
        this.nameplateSn = nameplateSn;
    }

    /** @param diagnosticId 关联定损报告 UUID，无则为空字符串; */
    public void setDiagnosticId(String diagnosticId) {
        this.diagnosticId = diagnosticId;
    }

    /** @param deviceReportId 关联日志报告 UUID，无则为空字符串; */
    public void setDeviceReportId(String deviceReportId) {
        this.deviceReportId = deviceReportId;
    }

    /** @param attachments 附件信息; */
    public void setAttachments(List<MaintenanceOrderAttachment> attachments) {
        this.attachments = attachments;
    }

    /** @param statusHistory 订单状态变更历史，无历史时为空数组; */
    public void setStatusHistory(List<MaintenanceOrderStatusHistoryItem> statusHistory) {
        this.statusHistory = statusHistory;
    }

    /** @param repairInfo 维修人员信息，已指派维修人员时返回; */
    public void setRepairInfo(MaintenanceOrderRepairInfo repairInfo) {
        this.repairInfo = repairInfo;
    }

    /** @param logReport 关联日志报告信息，日志反馈渠道且存在 device_report_id 时返回; */
    public void setLogReport(MaintenanceOrderLogReport logReport) {
        this.logReport = logReport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderDetailResults that = (MaintenanceOrderDetailResults) o;
        return Objects.equals(orderNumber, that.orderNumber)
                && Objects.equals(organizationUuid, that.organizationUuid)
                && Objects.equals(creatorId, that.creatorId)
                && Objects.equals(creatorName, that.creatorName)
                && sourceType == that.sourceType
                && Objects.equals(sourceTypeText, that.sourceTypeText)
                && status == that.status
                && Objects.equals(statusText, that.statusText)
                && Objects.equals(faultDescription, that.faultDescription)
                && Objects.equals(faultOccurrenceTime, that.faultOccurrenceTime)
                && Objects.equals(expectedServiceTime, that.expectedServiceTime)
                && Objects.equals(contactPerson, that.contactPerson)
                && Objects.equals(contactPhone, that.contactPhone)
                && Objects.equals(contactEmail, that.contactEmail)
                && Objects.equals(contactAddressFull, that.contactAddressFull)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(submissionTime, that.submissionTime)
                && Objects.equals(acceptedTime, that.acceptedTime)
                && Objects.equals(completedTime, that.completedTime)
                && Objects.equals(ruiyunOrderNumber, that.ruiyunOrderNumber)
                && Objects.equals(ruiyunSyncStatus, that.ruiyunSyncStatus)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(module, that.module)
                && Objects.equals(nameplateSn, that.nameplateSn)
                && Objects.equals(diagnosticId, that.diagnosticId)
                && Objects.equals(deviceReportId, that.deviceReportId)
                && Objects.equals(attachments, that.attachments)
                && Objects.equals(statusHistory, that.statusHistory)
                && Objects.equals(repairInfo, that.repairInfo)
                && Objects.equals(logReport, that.logReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                orderNumber,
                organizationUuid,
                creatorId,
                creatorName,
                sourceType,
                sourceTypeText,
                status,
                statusText,
                faultDescription,
                faultOccurrenceTime,
                expectedServiceTime,
                contactPerson,
                contactPhone,
                contactEmail,
                contactAddressFull,
                createdAt,
                updatedAt,
                submissionTime,
                acceptedTime,
                completedTime,
                ruiyunOrderNumber,
                ruiyunSyncStatus,
                deviceSn,
                deviceModelKey,
                module,
                nameplateSn,
                diagnosticId,
                deviceReportId,
                attachments,
                statusHistory,
                repairInfo,
                logReport);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderDetailResults{"
                + "orderNumber='" + orderNumber + '\''
                + ", organizationUuid='" + organizationUuid + '\''
                + ", creatorId='" + creatorId + '\''
                + ", creatorName='" + creatorName + '\''
                + ", sourceType=" + sourceType
                + ", sourceTypeText='" + sourceTypeText + '\''
                + ", status=" + status
                + ", statusText='" + statusText + '\''
                + ", faultDescription='" + faultDescription + '\''
                + ", faultOccurrenceTime='" + faultOccurrenceTime + '\''
                + ", expectedServiceTime='" + expectedServiceTime + '\''
                + ", contactPerson='" + contactPerson + '\''
                + ", contactPhone='" + contactPhone + '\''
                + ", contactEmail='" + contactEmail + '\''
                + ", contactAddressFull=" + contactAddressFull
                + ", createdAt='" + createdAt + '\''
                + ", updatedAt='" + updatedAt + '\''
                + ", submissionTime='" + submissionTime + '\''
                + ", acceptedTime='" + acceptedTime + '\''
                + ", completedTime='" + completedTime + '\''
                + ", ruiyunOrderNumber='" + ruiyunOrderNumber + '\''
                + ", ruiyunSyncStatus='" + ruiyunSyncStatus + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", module='" + module + '\''
                + ", nameplateSn='" + nameplateSn + '\''
                + ", diagnosticId='" + diagnosticId + '\''
                + ", deviceReportId='" + deviceReportId + '\''
                + ", attachments=" + attachments
                + ", statusHistory=" + statusHistory
                + ", repairInfo=" + repairInfo
                + ", logReport=" + logReport
                + '}';
    }
}
