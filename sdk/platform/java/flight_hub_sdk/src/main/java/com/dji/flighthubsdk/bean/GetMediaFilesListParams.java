// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 获取媒体文件列表请求参数 */
public class GetMediaFilesListParams {
    /** [可选参数] 飞行航线 ID, 传空字符串("")表示不指定具体的航线 ID; */
    private String flightId = "";
    /** [必须参数] 下一页请求的起始 index(第一次查询传入 0, 请求的响应结果返回会返回下一页起始索引); */
    private int startIndex = 0;
    /** [必须参数] 每页大小(比如可以传 100, 默认可传 100, 最大500); */
    private int pageSize = 100;
    /** [可选参数] file_types, 文件枚举类型("2": 图片, "3": 视频), 比如: ["2", "3"]; */
    private List<String> fileTypes = new ArrayList<>();
    /** [可选参数] 设备 sn(机场 Dock SN，非飞机 Drone SN), 传空字符串表示不指定具体的 设备; 媒体文件由机场上传至云端, 服务端 media_files.sn 存的是机场 SN 并做等值匹配, 传飞机 SN 查不到数据; */
    private String deviceSn = "";
    /** [可选参数] 开始时间, 时间戳: 秒, 传 -1 表示不指定该参数; */
    private long startCreateTime = -1L;
    /** [可选参数] 结束时间, 时间戳: 秒, 传 -1 表示不指定该参数; */
    private long endCreateTime = -1L;

    public GetMediaFilesListParams() {
    }


    /** [可选参数] 飞行航线 ID, 传空字符串("")表示不指定具体的航线 ID; */
    public String getFlightId() { return flightId; }
    /** @param flightId [可选参数] 飞行航线 ID, 传空字符串("")表示不指定具体的航线 ID; */
    public void setFlightId(String flightId) { this.flightId = flightId != null ? flightId : ""; }
    /** [必须参数] 下一页请求的起始 index(第一次查询传入 0, 请求的响应结果返回会返回下一页起始索引); */
    public int getStartIndex() { return startIndex; }
    /** @param startIndex [必须参数] 下一页请求的起始 index(第一次查询传入 0, 请求的响应结果返回会返回下一页起始索引); */
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
    /** [必须参数] 每页大小(比如可以传 100, 默认可传 100, 最大500); */
    public int getPageSize() { return pageSize; }
    /** @param pageSize [必须参数] 每页大小(比如可以传 100, 默认可传 100, 最大500); */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    /** [可选参数] file_types, 文件枚举类型("2": 图片, "3": 视频), 比如: ["2", "3"]; */
    public List<String> getFileTypes() { return fileTypes; }
    /** @param fileTypes [可选参数] file_types, 文件枚举类型("2": 图片, "3": 视频), 比如: ["2", "3"]; */
    public void setFileTypes(List<String> fileTypes) { this.fileTypes = fileTypes != null ? fileTypes : new ArrayList<>(); }
    /** [可选参数] 设备 sn(机场 Dock SN，非飞机 Drone SN), 传空字符串表示不指定具体的 设备; 媒体文件由机场上传至云端, 服务端 media_files.sn 存的是机场 SN 并做等值匹配, 传飞机 SN 查不到数据; */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn [可选参数] 设备 sn(机场 Dock SN，非飞机 Drone SN), 传空字符串表示不指定具体的 设备; 媒体文件由机场上传至云端, 服务端 media_files.sn 存的是机场 SN 并做等值匹配, 传飞机 SN 查不到数据; */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }
    /** [可选参数] 开始时间, 时间戳: 秒, 传 -1 表示不指定该参数; */
    public long getStartCreateTime() { return startCreateTime; }
    /** @param startCreateTime [可选参数] 开始时间, 时间戳: 秒, 传 -1 表示不指定该参数; */
    public void setStartCreateTime(long startCreateTime) { this.startCreateTime = startCreateTime; }
    /** [可选参数] 结束时间, 时间戳: 秒, 传 -1 表示不指定该参数; */
    public long getEndCreateTime() { return endCreateTime; }
    /** @param endCreateTime [可选参数] 结束时间, 时间戳: 秒, 传 -1 表示不指定该参数; */
    public void setEndCreateTime(long endCreateTime) { this.endCreateTime = endCreateTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetMediaFilesListParams that = (GetMediaFilesListParams) o;
        return Objects.equals(flightId, that.flightId)
                && startIndex == that.startIndex
                && pageSize == that.pageSize
                && Objects.equals(fileTypes, that.fileTypes)
                && Objects.equals(deviceSn, that.deviceSn)
                && startCreateTime == that.startCreateTime
                && endCreateTime == that.endCreateTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                flightId,
                startIndex,
                pageSize,
                fileTypes,
                deviceSn,
                startCreateTime,
                endCreateTime);
    }

    @Override
    public String toString() {
        return "GetMediaFilesListParams{"
                + "flightId='" + flightId + '\''
                + ", startIndex=" + startIndex
                + ", pageSize=" + pageSize
                + ", fileTypes=" + fileTypes
                + ", deviceSn='" + deviceSn + '\''
                + ", startCreateTime=" + startCreateTime
                + ", endCreateTime=" + endCreateTime
                + '}';
    }
}
