// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include "i_mqtt_client_manager.h"
#include <memory>

namespace message_engine {

class IMqttClientBridge;

/**
 * @brief MQTT客户端管理器桥接接口
 * 
 * 用于跨语言互操作的MQTT客户端管理器接口
 */
class IMqttClientManagerBridge {
public:
    virtual ~IMqttClientManagerBridge() = default;

    /**
     * @brief 添加MQTT客户端
     * @param params MQTT客户端参数
     * @return 操作结果
     */
    virtual MeErrorCode AddMqttClient(const TransportIdType& transport_id, const AddMqttClientParams& params) = 0;

    /**
     * @brief 移除MQTT客户端
     * @param transport_id 传输层ID
     * @return 操作结果
     */
    virtual MeErrorCode RemoveMqttClient(TransportIdType transport_id) = 0;

    /**
     * @brief 获取MQTT客户端桥接实例
     * @param transport_id 传输层ID
     * @return MQTT客户端桥接实例的弱引用
     */
    virtual std::weak_ptr<IMqttClientBridge> GetMqttClient(TransportIdType transport_id) = 0;
};

} // namespace message_engine 