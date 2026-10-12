// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 设置 LAS 模型坐标系并触发格式转换请求参数
 * 对应接口: POST /pf-terra/api/v1/models/converters
 * <p>
 * 仅适用于 subFileType = FormatLas(7) 的模型文件。
 * 上传 LAS 后若 lambda 探测到坐标系缺失（file_wkt_state != ContainsWkt），
 * 模型停在 ModelFileStateSelectCord(4) 状态，前端弹窗让用户选坐标系后调用此接口，
 * 服务端写入 selected_wkt 并触发 Las2Pnts 格式转换任务。
 */
public final class TerraSetLasWktParams {

    /** LAS 模型文件 UUID，必填 */
    private String fileUuid = "";

    /**
     * 坐标系描述，必填。
     * 支持 EPSG 代码（如 "EPSG:4326"）或完整 WKT 字符串，
     * 会作为 EpsgCode 传给 lambda-las2pnts。
     */
    private String wkt = "";

    public TerraSetLasWktParams() {}

    /**
     * @param fileUuid LAS 模型文件 UUID，必填
     * @param wkt      坐标系描述（EPSG 代码或 WKT 字符串），必填
     */
    public TerraSetLasWktParams(String fileUuid, String wkt) {
        this.fileUuid = fileUuid != null ? fileUuid : "";
        this.wkt = wkt != null ? wkt : "";
    }

    public String getFileUuid() { return fileUuid; }
    public void setFileUuid(String v) { fileUuid = v != null ? v : ""; }

    public String getWkt() { return wkt; }
    public void setWkt(String v) { wkt = v != null ? v : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraSetLasWktParams that = (TerraSetLasWktParams) o;
        return Objects.equals(fileUuid, that.fileUuid) && Objects.equals(wkt, that.wkt);
    }

    @Override
    public int hashCode() { return Objects.hash(fileUuid, wkt); }

    @Override
    public String toString() {
        return "TerraSetLasWktParams{fileUuid='" + fileUuid + "', wkt='" + wkt + "'}";
    }
}
