// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 单个 KMZ 文件导入结果条目 */
public class ImportKmzItem {
    /** KMZ 文件在对象存储中的路径，格式：wayline/import/<filename>.kmz */
    private String kmzObjectKey;
    /** 航线名称（默认为文件名去掉 .kmz 扩展名） */
    private String waylineName;
    /** 异步请求 ID（UUID 格式），可用于后续查询导入状态 */
    private String asyncRequestId;

    public ImportKmzItem() {}

    /** KMZ 文件在对象存储中的路径，格式：wayline/import/<filename>.kmz */
    public String getKmzObjectKey() { return kmzObjectKey; }

    /** @param kmzObjectKey KMZ 文件在对象存储中的路径，格式：wayline/import/<filename>.kmz */
    public void setKmzObjectKey(String kmzObjectKey) { this.kmzObjectKey = kmzObjectKey; }

    /** 航线名称（默认为文件名去掉 .kmz 扩展名） */
    public String getWaylineName() { return waylineName; }

    /** @param waylineName 航线名称（默认为文件名去掉 .kmz 扩展名） */
    public void setWaylineName(String waylineName) { this.waylineName = waylineName; }

    /** 异步请求 ID（UUID 格式），可用于后续查询导入状态 */
    public String getAsyncRequestId() { return asyncRequestId; }

    /** @param asyncRequestId 异步请求 ID（UUID 格式），可用于后续查询导入状态 */
    public void setAsyncRequestId(String asyncRequestId) { this.asyncRequestId = asyncRequestId; }

    @Override
    public String toString() {
        return "ImportKmzItem{kmzObjectKey='" + kmzObjectKey +
               "', waylineName='" + waylineName +
               "', asyncRequestId='" + asyncRequestId + "'}";
    }
}
