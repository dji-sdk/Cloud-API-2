// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// ESDK 控制模块接口，提供 ESDK（dock 扩展口）自定义数据透传能力。
class IEsdkControlModule {
public:
    virtual ~IEsdkControlModule() = default;

    /// @brief 订阅 ESDK 自定义数据透传（上行，ESDK→云端）
    /// @param callback 自定义数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_custom_data_transmission_from_esdk
    /// @endinternal
    virtual int listenEsdkCustomDataTransmissionFromEsdk(
        const std::function<void(const EsdkCustomDataTransmission& msg)>& callback) = 0;

    /// @brief 取消订阅 ESDK 自定义数据透传
    /// @param listen_id 监听 ID
    virtual void cancelListenEsdkCustomDataTransmissionFromEsdk(int listen_id) = 0;

    /// @brief 下发 ESDK 自定义数据透传（下行，云端→ESDK）
    /// @param value 数据内容（长度 < 256）
    /// @param callback 结果回调，err.success() 表示设备接受（result==0）
    ///
    /// @internal
    /// 链路: MQTT services | method: custom_data_transmission_to_esdk
    /// @endinternal
    virtual void sendEsdkCustomDataTransmissionToEsdk(
        const std::string& value,
        const std::function<void(FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk
