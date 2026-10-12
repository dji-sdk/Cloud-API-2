// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <base/async/executor.h>
#include <base/common/macros.h>
#include <mqtt_client.h>
#include <unordered_map>
#include "api/i_mqtt_client_manager.h"
#include "transport/transport_manager.h"

namespace message_engine {

// Transport管理类，负责管理mqtt client生成的Transport实例生命周期
class MqttClientManager : public IMqttClientManager {
public:
    MqttClientManager(const std::shared_ptr<drc_sdk::Executor>& executor, const std::shared_ptr<TransportManager>& transport_manager);
    ~MqttClientManager();

    MeErrorCode AddMqttClient(const TransportIdType& transport_id, const AddMqttClientParams& params) override;
    MeErrorCode RemoveMqttClient(const TransportIdType& transport_id) override;

    std::weak_ptr<IMqttClient> GetMqttClient(const TransportIdType& transport_id) override;
    bool IsMqttClientExisted(const TransportIdType& transport_id) override;

private:
    std::shared_ptr<drc_sdk::Executor> executor_;
    std::shared_ptr<TransportManager> transport_manager_;
    std::unordered_map<TransportIdType, std::shared_ptr<MqttClient>> mp_transport_id_to_mqtt_client_;
};


}  // namespace message_engine