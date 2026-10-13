// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 查询自定义飞行区列表请求参数 */
public class GetCustomizedFlightAreasParams {
    /** 飞行区名称过滤 */
    private String name = "";
    /** 飞行区类型过滤。枚举："dfence"=作业区, "nfz"=限飞区, "noland"=禁降区 */
    private String type = "";
    /** 状态过滤。枚举："disable"=已禁用, "enable"=已启用 */
    private String status = "";
    /** 飞行区 ID 过滤 */
    private List<String> ids = new ArrayList<>();
    /** 页码，从 1 开始，不传查全部 */
    private int pageIndex;
    /** 每页数量 */
    private int pageSize;

    public GetCustomizedFlightAreasParams() {
    }


    /** 飞行区名称过滤 */
    public String getName() { return name; }
    /** @param name 飞行区名称过滤 */
    public void setName(String name) { this.name = name != null ? name : ""; }
    /** 飞行区类型过滤。枚举："dfence"=作业区, "nfz"=限飞区, "noland"=禁降区 */
    public String getType() { return type; }
    /** @param type 飞行区类型过滤。枚举："dfence"=作业区, "nfz"=限飞区, "noland"=禁降区 */
    public void setType(String type) { this.type = type != null ? type : ""; }
    /** 状态过滤。枚举："disable"=已禁用, "enable"=已启用 */
    public String getStatus() { return status; }
    /** @param status 状态过滤。枚举："disable"=已禁用, "enable"=已启用 */
    public void setStatus(String status) { this.status = status != null ? status : ""; }
    /** 飞行区 ID 过滤 */
    public List<String> getIds() { return ids; }
    /** @param ids 飞行区 ID 过滤 */
    public void setIds(List<String> ids) { this.ids = ids != null ? ids : new ArrayList<>(); }
    /** 页码，从 1 开始，不传查全部 */
    public int getPageIndex() { return pageIndex; }
    /** @param pageIndex 页码，从 1 开始，不传查全部 */
    public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }
    /** 每页数量 */
    public int getPageSize() { return pageSize; }
    /** @param pageSize 每页数量 */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetCustomizedFlightAreasParams that = (GetCustomizedFlightAreasParams) o;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(status, that.status)
                && Objects.equals(ids, that.ids)
                && pageIndex == that.pageIndex
                && pageSize == that.pageSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                type,
                status,
                ids,
                pageIndex,
                pageSize);
    }

    @Override
    public String toString() {
        return "GetCustomizedFlightAreasParams{"
                + "name='" + name + '\''
                + ", type='" + type + '\''
                + ", status='" + status + '\''
                + ", ids=" + ids
                + ", pageIndex=" + pageIndex
                + ", pageSize=" + pageSize
                + '}';
    }
}
