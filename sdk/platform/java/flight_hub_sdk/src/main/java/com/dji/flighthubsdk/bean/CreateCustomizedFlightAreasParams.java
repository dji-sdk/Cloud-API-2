// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建自定义飞行区请求参数 */
public class CreateCustomizedFlightAreasParams {
    /** name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区"; */
    private String name = "";
    private String type = "";
    /** content, 飞行区内容; */
    private FlightAreasContentData content;
    /** created_by, 创建用户 ID; */
    private String createdBy = "";

    public CreateCustomizedFlightAreasParams() {
    }


    /** name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区"; */
    public String getName() { return name; }
    /** @param name name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区"; */
    public void setName(String name) { this.name = name != null ? name : ""; }
    /**
     * 飞行区类型枚举：
     * <ul>
     *   <li>"nfz" - 限飞区（最多 100 个）</li>
     *   <li>"dfence" - 作业区</li>
     *   <li>"noland" - 禁降区（最多 50 个）</li>
     * </ul>
     */
    public String getType() { return type; }
    /**
     * @param type 飞行区类型。枚举："nfz"=限飞区（最多 100 个）, "dfence"=作业区, "noland"=禁降区（最多 50 个）
     */
    public void setType(String type) { this.type = type != null ? type : ""; }
    /** content, 飞行区内容; */
    public FlightAreasContentData getContent() { return content; }
    /** @param content content, 飞行区内容; */
    public void setContent(FlightAreasContentData content) { this.content = content; }
    /** created_by, 创建用户 ID; */
    public String getCreatedBy() { return createdBy; }
    /** @param createdBy created_by, 创建用户 ID; */
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy != null ? createdBy : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateCustomizedFlightAreasParams that = (CreateCustomizedFlightAreasParams) o;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(content, that.content)
                && Objects.equals(createdBy, that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, content, createdBy);
    }

    @Override
    public String toString() {
        return "CreateCustomizedFlightAreasParams{"
                + "name='" + name + '\''
                + ", type='" + type + '\''
                + ", content=" + content
                + ", createdBy='" + createdBy + '\''
                + '}';
    }
}
