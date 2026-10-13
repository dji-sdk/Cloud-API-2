// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>
#include <vector>

#include "message.h"

namespace flight_hub_sdk {

/// 航线管理模块接口，提供航线增删改查、批量下载及导入事件监听等功能。
class IWaylineManageModule {
public:
    virtual ~IWaylineManageModule() = default;

    /// @brief 获取航线列表
    /// @param param 查询参数
    /// @param callback 回调，返回航线列表与错误信息
    /// @internal
    /// 链路: HTTP | GET /pf-wayline/api/v1/list
    /// @endinternal
    virtual void getWaylineList(const GetWaylineListParam& param, std::function<void(FlightHubError err, GetWaylineListResult result)> callback) = 0;

    /// @brief 获取航线详情
    /// @param wayline_uuid 航线 UUID
    /// @param callback 回调，返回航线详情与错误信息
    /// @internal
    /// 链路: HTTP | GET /pf-wayline/api/v1/details
    /// @endinternal
    virtual void getWaylineDetail(const std::string& wayline_uuid, std::function<void(FlightHubError err, GetWaylineDetailResult result)> callback) = 0;

    /// @brief 批量删除航线
    /// @param wayline_uuids 航线 UUID 列表
    /// @param callback 回调，返回删除结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/batch-delete
    /// @endinternal
    virtual void batchDeleteWaylines(const std::vector<std::string>& wayline_uuids,
        std::function<void(FlightHubError err, BatchDeleteWaylinesResult result)> callback) = 0;

    /// @brief 更新航线
    /// @param wayline_uuid 航线 UUID
    /// @param wayline_name 新航线名称
    /// @param callback 回调，返回更新结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/update
    /// @endinternal
    virtual void updateWayline(const std::string& wayline_uuid, const std::string& wayline_name, std::function<void(FlightHubError err, UpdateWaylineResult result)> callback) = 0;

    /// @brief 提交航线文件信息
    /// @param param 提交参数
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/import-callback
    /// @endinternal
    virtual void submitWaylineInfo(const SubmitWaylineInfoParam& param, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 批量下载航线文件
    /// @param param 下载参数
    /// @param callback 回调，返回下载结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/batch-download
    /// @endinternal
    virtual void batchDownloadWaylines(const BatchDownloadWaylinesParam& param, std::function<void(FlightHubError err, BatchDownloadWaylinesResult result)> callback) = 0;

    /// @brief 创建副本航线
    /// @param param 参数
    /// @param callback 回调，返回副本航线结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/duplicate
    /// @endinternal
    virtual void duplicateWayline(const DuplicateWaylineParam& param, std::function<void(FlightHubError err, DuplicateWaylineResult result)> callback) = 0;

    /// @brief 航线复制（去组织化，不需要 workspace_id）
    /// @param param 复制参数（原始航线 UUID + 目标航线名称）
    /// @param callback 回调，err 为 SUCCESS 时 result 有效
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/copy
    /// @endinternal
    virtual void copyWayline(const CopyWaylineParam& param, std::function<void(FlightHubError err, CopyWaylineResult result)> callback) = 0;

    /// @brief 航线拼接（去组织化，不需要 workspace_id）
    /// @param param 拼接参数（目标名称 + kmz_object_key + 子航线UUID列表）
    /// @param callback 回调，err 为 SUCCESS 时 result 有效（含 kmz_data 和 ext_data）
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/concat
    /// @endinternal
    virtual void concatWayline(const ConcatWaylineParam& param, std::function<void(FlightHubError err, ConcatWaylineResult result)> callback) = 0;

    /// @brief 上传 KMZ 文件并导入航线
    /// @param file_paths 本地 KMZ 文件路径列表（仅接受 .kmz 后缀）
    /// @param callback 回调，err 为 SUCCESS 时 result 有效
    /// @internal
    /// 链路: HTTP | POST /pf-wayline/api/v1/import-kmz (multipart/form-data)
    /// @endinternal
    virtual void importKmz(const std::vector<std::string>& file_paths, std::function<void(FlightHubError err, ImportKmzResult result)> callback) = 0;

    /// @brief 监听航线导入事件
    /// @param callback 航线导入事件回调
    /// @return 监听 ID，用于取消订阅
    /// @internal
    /// 链路: WebSocket | topic: topic_import_wayline_event
    /// @endinternal
    virtual int listenImportWaylineEvent(const std::function<void(const ImportWaylineEventResult& result)>& callback) = 0;

    /// @brief 取消订阅航线导入事件
    /// @param listen_id 监听 ID
    virtual void cancelListenImportWaylineEvent(int listen_id) = 0;

    /// @brief 获取航线编辑器 OSS STS 临时上传凭证
    /// @param params 请求参数（object_key_prefix 必填）
    /// @param callback 结果回调，成功时返回 STS 凭证信息
    /// @internal
    /// 链路: HTTP | POST /gateway/pf-wayline/editor/v1/sts
    /// @endinternal
    virtual void getWaylineEditorSts(
        const GetWaylineEditorStsParams& params,
        std::function<void(FlightHubError err, const GetWaylineEditorStsResult& result)> callback) = 0;
};

}  // namespace flight_hub_sdk
