// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>
#include <vector>

#include "message.h"

namespace flight_hub_sdk {

/// 固件管理模块接口，提供固件查询、升级、上传、发布及兼容性检查等功能。
class IFirmwareManageModule {
public:
    virtual ~IFirmwareManageModule() = default;

    /// @brief 获取最新固件发布说明
    /// @param device_model_keys 设备型号 key 列表
    /// @param language 语言
    /// @param callback 回调，返回固件发布说明列表与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/firmware-release-notes/latest
    /// @endinternal
    virtual void getLatestFirmwareReleaseNote(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(FlightHubError err, FirmwareReleaseNoteListResult release_note_list)> callback) = 0;

    /// @brief 触发固件升级
    /// @param params 升级参数
    /// @param callback 回调，返回升级结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/{uuid}/ota/upgrade
    /// @endinternal
    virtual void invokeFirmwareUpgrade(const FirmwareUpgradeParam& params, std::function<void(FlightHubError err, FirmwareUpgradeResult result)> callback) = 0;

    /// @brief 查询固件升级进度
    /// @param device_sn 设备序列号
    /// @param callback 回调，返回升级进度与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/ota/progress
    /// @endinternal
    virtual void queryFirmwareUpgradeProgress(const std::string& device_sn, std::function<void(FlightHubError err, FirmwareUpgradeProgressResult progress_list)> callback) = 0;

    /// @brief 上传固件文件
    /// @param file_path 本地文件路径
    /// @param callback 回调，返回上传结果与错误信息
    /// @internal
    /// 链路: SDK 内部 | 获取 STS 凭证后上传固件文件
    /// @endinternal
    virtual void uploadFirmwareFile(const std::string& file_path, std::function<void(FlightHubError err, UploadFileResults results)> callback) = 0;

    /// @brief 发布固件
     /// @param file_id 固件文件 ID
     /// @param callback 回调，返回错误信息
     /// @note 首次发布时后端会同步拆包并上传小包，接口可能耗时较长；SDK HTTP 超时为 90s
     /// @internal
    /// 链路: HTTP | PUT /device/api/v1/manager/firmwares/{id}
    /// @endinternal
    virtual void releaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 取消发布固件
    /// @param file_id 固件文件 ID
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | PUT /device/api/v1/manager/firmwares/{id}
    /// @endinternal
    virtual void unReleaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 删除固件
    /// @param file_id 固件文件 ID
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | DELETE /device/api/v1/manager/firmwares/{id}
    /// @endinternal
    virtual void deleteFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 查询固件列表
    /// @param page 页码
    /// @param page_size 每页数量
    /// @param callback 回调，返回固件列表与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/firmwares
    /// @endinternal
    virtual void queryFirmwareList(int page, int page_size, std::function<void(FlightHubError err, FirmwareListResult firmware_list)> callback) = 0;

    /// @brief 查询固件兼容性配置
    /// @param callback 回调，返回兼容性配置与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/firmware_compatible/config
    /// @endinternal
    virtual void queryFirmwareCompatibilityConfig(std::function<void(FlightHubError err, FirmwareCompatibilityConfigResult config)> callback) = 0;

    /// @brief 检查固件状态
    /// @param device_sns 设备序列号列表
    /// @param status 状态列表
    /// @param callback 回调，返回固件状态列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/ota/infos
    /// @endinternal
    virtual void checkFirmwareStatus(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(FlightHubError err, FirmwareStatusListResult status_list)> callback) = 0;

    /// @brief 提交固件信息
    /// @param firmware_info 固件信息参数
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/firmwares
    /// @endinternal
    virtual void submitFirmwareInfo(const FirmwareInfoParam& firmware_info, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 订阅固件升级进度
    /// @param device_sn 设备序列号
    /// @param callback 升级进度回调
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_ota_progress
    /// @endinternal
    virtual int listenDeviceOtaProgress(
        const std::string& device_sn,
        const std::function<void(const DeviceOtaProgress& msg)>& callback) = 0;

    /// @brief 取消订阅固件升级进度
    /// @param listen_id 监听 ID
    virtual void cancelListenDeviceOtaProgress(int listen_id) = 0;
};

}  // namespace flight_hub_sdk
