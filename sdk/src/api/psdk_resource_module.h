// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>

#include "message.h"

namespace flight_hub_sdk {

/// PSDK 资源模块接口，提供喊话器资源、控件资源及音频上传等功能。
class IPsdkResourceModule {
public:
    virtual ~IPsdkResourceModule() = default;

    /// @brief 获取 PSDK 喊话器资源列表
    /// @param callback 回调，返回喊话器资源列表与错误信息
    /// @internal
    /// 链路: HTTP | GET /pf-media/api/v2/speaker/resources
    /// @endinternal
    virtual void getPsdkSpeakerResourceList(std::function<void(FlightHubError err, const PsdkSpeakerResourceList& psdk_speaker_resource_list)> callback) = 0;

    /// @brief 创建 PSDK 喊话器资源
    /// @param params 创建参数
    /// @param callback 回调，返回创建后的资源与错误信息
    /// @internal
    /// 链路: HTTP | POST /pf-media/api/v2/speaker/resources
    /// @endinternal
    virtual void createPsdkSpeakerResource(const CreatePsdkSpeakerResourceParams& params, std::function<void(FlightHubError err, const PsdkSpeakerResource& psdk_speaker_resource)> callback) = 0;

    /// @brief 编辑 PSDK 喊话器资源
    /// @param params 编辑参数
    /// @param callback 回调，返回更新后的资源与错误信息
    /// @internal
    /// 链路: HTTP | PUT /pf-media/api/v2/speaker/resources/{id}
    /// @endinternal
    virtual void editPsdkSpeakerResource(const EditPsdkSpeakerResourceParams& params, std::function<void(FlightHubError err, const PsdkSpeakerResource& psdk_speaker_resource)> callback) = 0;

    /// @brief 删除 PSDK 喊话器资源
    /// @param resource_id 资源 ID
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | DELETE /pf-media/api/v2/speaker/resources/{id}
    /// @endinternal
    virtual void deletePsdkSpeakerResource(int resource_id, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 获取 STS 临时凭证
    /// @param callback 回调，返回 STS 凭证信息与错误信息
    /// @internal
    /// 链路: HTTP | GET /pf-media/api/v2/speaker/sts
    /// @endinternal
    virtual void getSTS(std::function<void(FlightHubError err, const STSInfo& sts_response)> callback) = 0;

    /// @brief 批量获取 PSDK 控件资源
    /// @param params 获取参数
    /// @param callback 回调，返回控件资源列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/manual/psdk/ui-resources/batch-get
    /// @endinternal
    virtual void getPsdkWidgetResources(const GetPsdkWidgetResourceParams& params, std::function<void(FlightHubError err, const PsdkWidgetResourceList& psdk_widget_resource_list)> callback) = 0;

    /// @brief 上传喊话器音频文件
    /// @param file_path 本地文件路径
    /// @param sts_info STS 凭证信息
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: SDK 内部 | 获取 STS 凭证后上传至对象存储
    /// @endinternal
    virtual void uploadSpeakerAudioFile(const std::string& file_path, const STSInfo& sts_info, std::function<void(FlightHubError err)> callback) = 0;
};

}  // namespace flight_hub_sdk
