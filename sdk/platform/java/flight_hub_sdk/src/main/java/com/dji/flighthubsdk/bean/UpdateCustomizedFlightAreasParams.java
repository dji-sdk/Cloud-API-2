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
    /** update_by, 更新用户 ID; */
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
    /** update_by, 更新用户 ID; */
    public String getUpdateBy() { return updateBy; }
    /** @param updateBy update_by, 更新用户 ID; */
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
                && Objects.equals(updateBy, that.updateBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightAreaId, name, content, updateBy);
    }

    @Override
    public String toString() {
        return "UpdateCustomizedFlightAreasParams{"
                + "flightAreaId='" + flightAreaId + '\''
                + ", name='" + name + '\''
                + ", content=" + content
                + ", updateBy='" + updateBy + '\''
                + '}';
    }
}
