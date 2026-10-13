// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 创建报修订单请求参数 */
public class CreateMaintenanceOrderParams {
    /** 故障发生时间 fault_occurrence_time; */
    private String faultOccurrenceTime = "";
    /** 设备列表; */
    private List<MaintenanceOrderDeviceItemParams> deviceList = new ArrayList<>();
    /** 故障描述; */
    private String faultDescription = "";
    /** 附件列表; */
    private List<MaintenanceOrderAttachmentsParams> attachments = new ArrayList<>();
    /** 联系人; */
    private String contactPerson = "";
    /** 联系电话; */
    private String contactPhone = "";
    /** 联系邮箱; */
    private String contactEmail = "";
    /** 联系地址; */
    private MaintenanceOrderContactAddressParams contactAddress;
    /** 期望服务时间; */
    private String expectedServiceTime = "";
    /** 报告 UUID(关联的日志报告 UUID(日志反馈报修时传入)); */
    private String reportUuid = "";
    /** 设备拓扑; */
    private MaintenanceOrderDeviceTopoParams deviceTopo;
    /** 日志列表; */
    private List<MaintenanceOrderLogInfoParams> logList = new ArrayList<>();

    public CreateMaintenanceOrderParams() {
    }


    /** 故障发生时间 fault_occurrence_time; */
    public String getFaultOccurrenceTime() {
        return faultOccurrenceTime;
    }

    /** @param faultOccurrenceTime 故障发生时间 fault_occurrence_time; */
    public void setFaultOccurrenceTime(String faultOccurrenceTime) {
        this.faultOccurrenceTime = faultOccurrenceTime != null ? faultOccurrenceTime : "";
    }

    /** 设备列表; */
    public List<MaintenanceOrderDeviceItemParams> getDeviceList() {
        return deviceList;
    }

    /** @param deviceList 设备列表; */
    public void setDeviceList(List<MaintenanceOrderDeviceItemParams> deviceList) {
        this.deviceList = deviceList != null ? deviceList : new ArrayList<>();
    }

    /** 故障描述; */
    public String getFaultDescription() {
        return faultDescription;
    }

    /** @param faultDescription 故障描述; */
    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription != null ? faultDescription : "";
    }

    /** 附件列表; */
    public List<MaintenanceOrderAttachmentsParams> getAttachments() {
        return attachments;
    }

    /** @param attachments 附件列表; */
    public void setAttachments(List<MaintenanceOrderAttachmentsParams> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    /** 联系人; */
    public String getContactPerson() {
        return contactPerson;
    }

    /** @param contactPerson 联系人; */
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson != null ? contactPerson : "";
    }

    /** 联系电话; */
    public String getContactPhone() {
        return contactPhone;
    }

    /** @param contactPhone 联系电话; */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone != null ? contactPhone : "";
    }

    /** 联系邮箱; */
    public String getContactEmail() {
        return contactEmail;
    }

    /** @param contactEmail 联系邮箱; */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail != null ? contactEmail : "";
    }

    /** 联系地址; */
    public MaintenanceOrderContactAddressParams getContactAddress() {
        return contactAddress;
    }

    /** @param contactAddress 联系地址; */
    public void setContactAddress(MaintenanceOrderContactAddressParams contactAddress) {
        this.contactAddress = contactAddress;
    }

    /** 期望服务时间; */
    public String getExpectedServiceTime() {
        return expectedServiceTime;
    }

    /** @param expectedServiceTime 期望服务时间; */
    public void setExpectedServiceTime(String expectedServiceTime) {
        this.expectedServiceTime = expectedServiceTime != null ? expectedServiceTime : "";
    }

    /** 报告 UUID(关联的日志报告 UUID(日志反馈报修时传入)); */
    public String getReportUuid() {
        return reportUuid;
    }

    /** @param reportUuid 报告 UUID(关联的日志报告 UUID(日志反馈报修时传入)); */
    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid != null ? reportUuid : "";
    }

    /** 设备拓扑; */
    public MaintenanceOrderDeviceTopoParams getDeviceTopo() {
        return deviceTopo;
    }

    /** @param deviceTopo 设备拓扑; */
    public void setDeviceTopo(MaintenanceOrderDeviceTopoParams deviceTopo) {
        this.deviceTopo = deviceTopo;
    }

    /** 日志列表; */
    public List<MaintenanceOrderLogInfoParams> getLogList() {
        return logList;
    }

    /** @param logList 日志列表; */
    public void setLogList(List<MaintenanceOrderLogInfoParams> logList) {
        this.logList = logList != null ? logList : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateMaintenanceOrderParams that = (CreateMaintenanceOrderParams) o;
        return Objects.equals(faultOccurrenceTime, that.faultOccurrenceTime)
                && Objects.equals(deviceList, that.deviceList)
                && Objects.equals(faultDescription, that.faultDescription)
                && Objects.equals(attachments, that.attachments)
                && Objects.equals(contactPerson, that.contactPerson)
                && Objects.equals(contactPhone, that.contactPhone)
                && Objects.equals(contactEmail, that.contactEmail)
                && Objects.equals(contactAddress, that.contactAddress)
                && Objects.equals(expectedServiceTime, that.expectedServiceTime)
                && Objects.equals(reportUuid, that.reportUuid)
                && Objects.equals(deviceTopo, that.deviceTopo)
                && Objects.equals(logList, that.logList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                faultOccurrenceTime,
                deviceList,
                faultDescription,
                attachments,
                contactPerson,
                contactPhone,
                contactEmail,
                contactAddress,
                expectedServiceTime,
                reportUuid,
                deviceTopo,
                logList);
    }

    @Override
    public String toString() {
        return "CreateMaintenanceOrderParams{"
                + "faultOccurrenceTime='" + faultOccurrenceTime + '\''
                + ", deviceList=" + deviceList
                + ", faultDescription='" + faultDescription + '\''
                + ", attachments=" + attachments
                + ", contactPerson='" + contactPerson + '\''
                + ", contactPhone='" + contactPhone + '\''
                + ", contactEmail='" + contactEmail + '\''
                + ", contactAddress=" + contactAddress
                + ", expectedServiceTime='" + expectedServiceTime + '\''
                + ", reportUuid='" + reportUuid + '\''
                + ", deviceTopo=" + deviceTopo
                + ", logList=" + logList
                + '}';
    }
}
