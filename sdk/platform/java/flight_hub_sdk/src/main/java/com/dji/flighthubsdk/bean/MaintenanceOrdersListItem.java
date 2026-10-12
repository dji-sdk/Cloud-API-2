// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;

import java.util.Objects;
/** 报修订单列表项 */
public final class MaintenanceOrdersListItem {
    private String orderNumber;
    private String ruiyunOrderNumber;
    private int status;
    private String statusText;
    private int sourceType;
    private String sourceTypeText;
    private String creatorId;
    private String creatorName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private MaintenanceOrderContactAddress contactAddressFull;
    private String faultDescription;
    private String faultOccurrenceTime;
    private String expectedServiceTime;
    private String createdAt;
    private String updatedAt;
    private String submissionTime;
    private String acceptedTime;
    private String completedTime;
    private String deviceSn;
    private String deviceModelKey;
    private String deviceModelName;
    private String nameplateSn;
    private String module;
    private String deviceOrganizationCallsign;
    private List<MaintenanceOrderAttachment> attachments;
    private List<MaintenanceOrderStatusHistoryItem> statusHistory;
    private MaintenanceOrderRepairInfo repairInfo;
    private MaintenanceOrderLogReport logReport;
    public MaintenanceOrdersListItem() {
    }

/**
     * 构造 MaintenanceOrdersListItem。
     *
     * @param orderNumber 工单编号
     * @param ruiyunOrderNumber 瑞云工单编号
     * @param status 工单状态
     * @param statusText 工单状态文本
     * @param sourceType 报修来源类型
     * @param sourceTypeText 报修来源类型文本
     * @param creatorId 创建者ID
     * @param creatorName 创建者名称
     * @param contactPerson 联系人
     * @param contactPhone 联系电话
     * @param contactEmail 联系邮箱
     * @param contactAddressFull 联系地址详情
     * @param faultDescription 故障描述
     * @param faultOccurrenceTime 故障发生时间
     * @param expectedServiceTime 期望服务时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @param submissionTime 提交时间
     * @param acceptedTime 受理时间
     * @param completedTime 完成时间
     * @param deviceSn 设备序列号
     * @param deviceModelKey 设备型号Key
     * @param deviceModelName 设备型号名称
     * @param nameplateSn 铭牌序列号
     * @param module 模块
     * @param deviceOrganizationCallsign 设备组织呼号
     * @param attachments 附件列表
     * @param statusHistory 状态历史记录
     * @param repairInfo 维修信息
     * @param logReport 日志报告
     */
    public MaintenanceOrdersListItem(
            String orderNumber, String ruiyunOrderNumber, int status, String statusText,
            int sourceType, String sourceTypeText, String creatorId, String creatorName,
            String contactPerson, String contactPhone, String contactEmail,
            MaintenanceOrderContactAddress contactAddressFull,
            String faultDescription, String faultOccurrenceTime, String expectedServiceTime,
            String createdAt, String updatedAt, String submissionTime, String acceptedTime, String completedTime,
            String deviceSn, String deviceModelKey, String deviceModelName, String nameplateSn,
            String module, String deviceOrganizationCallsign,
            List<MaintenanceOrderAttachment> attachments,
            List<MaintenanceOrderStatusHistoryItem> statusHistory,
            MaintenanceOrderRepairInfo repairInfo,
            MaintenanceOrderLogReport logReport) {
        this.orderNumber = orderNumber;
        this.ruiyunOrderNumber = ruiyunOrderNumber;
        this.status = status;
        this.statusText = statusText;
        this.sourceType = sourceType;
        this.sourceTypeText = sourceTypeText;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.contactAddressFull = contactAddressFull;
        this.faultDescription = faultDescription;
        this.faultOccurrenceTime = faultOccurrenceTime;
        this.expectedServiceTime = expectedServiceTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submissionTime = submissionTime;
        this.acceptedTime = acceptedTime;
        this.completedTime = completedTime;
        this.deviceSn = deviceSn;
        this.deviceModelKey = deviceModelKey;
        this.deviceModelName = deviceModelName;
        this.nameplateSn = nameplateSn;
        this.module = module;
        this.deviceOrganizationCallsign = deviceOrganizationCallsign;
        this.attachments = attachments;
        this.statusHistory = statusHistory;
        this.repairInfo = repairInfo;
        this.logReport = logReport;
    }

    /** 工单编号 */
    public String getOrderNumber() { return orderNumber; }
    /** 瑞云工单编号 */
    public String getRuiyunOrderNumber() { return ruiyunOrderNumber; }
    /** 工单状态 */
    public int getStatus() { return status; }
    /** 工单状态文本 */
    public String getStatusText() { return statusText; }
    /** 报修来源类型 */
    public int getSourceType() { return sourceType; }
    /** 报修来源类型文本 */
    public String getSourceTypeText() { return sourceTypeText; }
    /** 创建者ID */
    public String getCreatorId() { return creatorId; }
    /** 创建者名称 */
    public String getCreatorName() { return creatorName; }
    /** 联系人 */
    public String getContactPerson() { return contactPerson; }
    /** 联系电话 */
    public String getContactPhone() { return contactPhone; }
    /** 联系邮箱 */
    public String getContactEmail() { return contactEmail; }
    /** 联系地址详情 */
    public MaintenanceOrderContactAddress getContactAddressFull() { return contactAddressFull; }
    /** 故障描述 */
    public String getFaultDescription() { return faultDescription; }
    /** 故障发生时间 */
    public String getFaultOccurrenceTime() { return faultOccurrenceTime; }
    /** 期望服务时间 */
    public String getExpectedServiceTime() { return expectedServiceTime; }
    /** 创建时间 */
    public String getCreatedAt() { return createdAt; }
    /** 更新时间 */
    public String getUpdatedAt() { return updatedAt; }
    /** 提交时间 */
    public String getSubmissionTime() { return submissionTime; }
    /** 受理时间 */
    public String getAcceptedTime() { return acceptedTime; }
    /** 完成时间 */
    public String getCompletedTime() { return completedTime; }
    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** 设备型号Key */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** 设备型号名称 */
    public String getDeviceModelName() { return deviceModelName; }
    /** 铭牌序列号 */
    public String getNameplateSn() { return nameplateSn; }
    /** 模块 */
    public String getModule() { return module; }
    /** 设备组织呼号 */
    public String getDeviceOrganizationCallsign() { return deviceOrganizationCallsign; }
    /** 附件列表 */
    public List<MaintenanceOrderAttachment> getAttachments() { return attachments; }
    /** 状态历史记录 */
    public List<MaintenanceOrderStatusHistoryItem> getStatusHistory() { return statusHistory; }
    /** 维修信息 */
    public MaintenanceOrderRepairInfo getRepairInfo() { return repairInfo; }
    /** 日志报告 */
    public MaintenanceOrderLogReport getLogReport() { return logReport; }

    /** 工单编号 */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /** 瑞云工单编号 */
    public void setRuiyunOrderNumber(String ruiyunOrderNumber) {
        this.ruiyunOrderNumber = ruiyunOrderNumber;
    }

    /** 工单状态 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 工单状态文本 */
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    /** 报修来源类型 */
    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    /** 报修来源类型文本 */
    public void setSourceTypeText(String sourceTypeText) {
        this.sourceTypeText = sourceTypeText;
    }

    /** 创建者ID */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /** 创建者名称 */
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    /** 联系人 */
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    /** 联系电话 */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /** 联系邮箱 */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /** 联系地址详情 */
    public void setContactAddressFull(MaintenanceOrderContactAddress contactAddressFull) {
        this.contactAddressFull = contactAddressFull;
    }

    /** 故障描述 */
    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    /** 故障发生时间 */
    public void setFaultOccurrenceTime(String faultOccurrenceTime) {
        this.faultOccurrenceTime = faultOccurrenceTime;
    }

    /** 期望服务时间 */
    public void setExpectedServiceTime(String expectedServiceTime) {
        this.expectedServiceTime = expectedServiceTime;
    }

    /** 创建时间 */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** 更新时间 */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** 提交时间 */
    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    /** 受理时间 */
    public void setAcceptedTime(String acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    /** 完成时间 */
    public void setCompletedTime(String completedTime) {
        this.completedTime = completedTime;
    }

    /** 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 设备型号Key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 设备型号名称 */
    public void setDeviceModelName(String deviceModelName) {
        this.deviceModelName = deviceModelName;
    }

    /** 铭牌序列号 */
    public void setNameplateSn(String nameplateSn) {
        this.nameplateSn = nameplateSn;
    }

    /** 模块 */
    public void setModule(String module) {
        this.module = module;
    }

    /** 设备组织呼号 */
    public void setDeviceOrganizationCallsign(String deviceOrganizationCallsign) {
        this.deviceOrganizationCallsign = deviceOrganizationCallsign;
    }

    /** 附件列表 */
    public void setAttachments(List<MaintenanceOrderAttachment> attachments) {
        this.attachments = attachments;
    }

    /** 状态历史记录 */
    public void setStatusHistory(List<MaintenanceOrderStatusHistoryItem> statusHistory) {
        this.statusHistory = statusHistory;
    }

    /** 维修信息 */
    public void setRepairInfo(MaintenanceOrderRepairInfo repairInfo) {
        this.repairInfo = repairInfo;
    }

    /** 日志报告 */
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
        MaintenanceOrdersListItem that = (MaintenanceOrdersListItem) o;
        return Objects.equals(orderNumber, that.orderNumber)
                && Objects.equals(ruiyunOrderNumber, that.ruiyunOrderNumber)
                && status == that.status
                && Objects.equals(statusText, that.statusText)
                && sourceType == that.sourceType
                && Objects.equals(sourceTypeText, that.sourceTypeText)
                && Objects.equals(creatorId, that.creatorId)
                && Objects.equals(creatorName, that.creatorName)
                && Objects.equals(contactPerson, that.contactPerson)
                && Objects.equals(contactPhone, that.contactPhone)
                && Objects.equals(contactEmail, that.contactEmail)
                && Objects.equals(contactAddressFull, that.contactAddressFull)
                && Objects.equals(faultDescription, that.faultDescription)
                && Objects.equals(faultOccurrenceTime, that.faultOccurrenceTime)
                && Objects.equals(expectedServiceTime, that.expectedServiceTime)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(submissionTime, that.submissionTime)
                && Objects.equals(acceptedTime, that.acceptedTime)
                && Objects.equals(completedTime, that.completedTime)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(deviceModelName, that.deviceModelName)
                && Objects.equals(nameplateSn, that.nameplateSn)
                && Objects.equals(module, that.module)
                && Objects.equals(deviceOrganizationCallsign, that.deviceOrganizationCallsign)
                && Objects.equals(attachments, that.attachments)
                && Objects.equals(statusHistory, that.statusHistory)
                && Objects.equals(repairInfo, that.repairInfo)
                && Objects.equals(logReport, that.logReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                orderNumber,
                ruiyunOrderNumber,
                status,
                statusText,
                sourceType,
                sourceTypeText,
                creatorId,
                creatorName,
                contactPerson,
                contactPhone,
                contactEmail,
                contactAddressFull,
                faultDescription,
                faultOccurrenceTime,
                expectedServiceTime,
                createdAt,
                updatedAt,
                submissionTime,
                acceptedTime,
                completedTime,
                deviceSn,
                deviceModelKey,
                deviceModelName,
                nameplateSn,
                module,
                deviceOrganizationCallsign,
                attachments,
                statusHistory,
                repairInfo,
                logReport);
    }

    @Override
    public String toString() {
        return "MaintenanceOrdersListItem{"
                + "orderNumber='" + orderNumber + '\''
                + ", ruiyunOrderNumber='" + ruiyunOrderNumber + '\''
                + ", status=" + status
                + ", statusText='" + statusText + '\''
                + ", sourceType=" + sourceType
                + ", sourceTypeText='" + sourceTypeText + '\''
                + ", creatorId='" + creatorId + '\''
                + ", creatorName='" + creatorName + '\''
                + ", contactPerson='" + contactPerson + '\''
                + ", contactPhone='" + contactPhone + '\''
                + ", contactEmail='" + contactEmail + '\''
                + ", contactAddressFull=" + contactAddressFull
                + ", faultDescription='" + faultDescription + '\''
                + ", faultOccurrenceTime='" + faultOccurrenceTime + '\''
                + ", expectedServiceTime='" + expectedServiceTime + '\''
                + ", createdAt='" + createdAt + '\''
                + ", updatedAt='" + updatedAt + '\''
                + ", submissionTime='" + submissionTime + '\''
                + ", acceptedTime='" + acceptedTime + '\''
                + ", completedTime='" + completedTime + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", deviceModelName='" + deviceModelName + '\''
                + ", nameplateSn='" + nameplateSn + '\''
                + ", module='" + module + '\''
                + ", deviceOrganizationCallsign='" + deviceOrganizationCallsign + '\''
                + ", attachments=" + attachments
                + ", statusHistory=" + statusHistory
                + ", repairInfo=" + repairInfo
                + ", logReport=" + logReport
                + '}';
    }
}
