// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 更新自定义飞行区请求参数 */
public class UpdateCustomizedFlightAreasParams {
    private String flightAreaId = "";
    /** name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区"; */
    private String name = "";
    /** content, 飞行区内容; */
    private FlightAreasContentData content;
    /** status, 飞行区状态: enable / disable（可选, 空表示不更新） */
    private String status = "";
    /** type, 飞行区类型: dfence / nfz / noland（可选, 空表示不更新） */
    private String type = "";
    /**
     * update_by, 更新用户 ID;
     * @deprecated 后端 updated_by 以登录态为准, 请求不再上送该字段, 设置无效(保留仅为兼容)。
     */
    @Deprecated
    private String updateBy = "";

    public UpdateCustomizedFlightAreasParams() {
    }


    /** 飞行区域ID */
    public String getFlightAreaId() { return flightAreaId; }
    /** @param flightAreaId 飞行区域ID */
    public void setFlightAreaId(String flightAreaId) { this.flightAreaId = flightAreaId != null ? flightAreaId : ""; }
    /** name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区"; */
    public String getName() { return name; }
    /** @param name name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区"; */
    public void setName(String name) { this.name = name != null ? name : ""; }
    /** content, 飞行区内容; */
    public FlightAreasContentData getContent() { return content; }
    /** @param content content, 飞行区内容; */
    public void setContent(FlightAreasContentData content) { this.content = content; }
    /** status, 飞行区状态: enable / disable（可选, 空表示不更新） */
    public String getStatus() { return status; }
    /** @param status status, 飞行区状态: enable / disable（可选, 空表示不更新） */
    public void setStatus(String status) { this.status = status != null ? status : ""; }
    /** type, 飞行区类型: dfence / nfz / noland（可选, 空表示不更新） */
    public String getType() { return type; }
    /** @param type type, 飞行区类型: dfence / nfz / noland（可选, 空表示不更新） */
    public void setType(String type) { this.type = type != null ? type : ""; }
    /**
     * update_by, 更新用户 ID;
     * @deprecated 后端 updated_by 以登录态为准, 请求不再上送该字段, 设置无效。
     */
    @Deprecated
    public String getUpdateBy() { return updateBy; }
    /**
     * @param updateBy update_by, 更新用户 ID;
     * @deprecated 后端 updated_by 以登录态为准, 请求不再上送该字段, 设置无效。
     */
    @Deprecated
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy != null ? updateBy : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateCustomizedFlightAreasParams that = (UpdateCustomizedFlightAreasParams) o;
        return Objects.equals(flightAreaId, that.flightAreaId)
                && Objects.equals(name, that.name)
                && Objects.equals(content, that.content)
                && Objects.equals(status, that.status)
                && Objects.equals(type, that.type)
                && Objects.equals(updateBy, that.updateBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightAreaId, name, content, status, type, updateBy);
    }

    @Override
    public String toString() {
        return "UpdateCustomizedFlightAreasParams{"
                + "flightAreaId='" + flightAreaId + '\''
                + ", name='" + name + '\''
                + ", content=" + content
                + ", status='" + status + '\''
                + ", type='" + type + '\''
                + ", updateBy='" + updateBy + '\''
                + '}';
    }
}
