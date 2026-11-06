// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/esdk_control_module.h"
#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "notification_center/notification_center.h"
#include <functional>
#include <unordered_map>
#include <mutex>

namespace flight_hub_sdk {

class EsdkControlModuleImpl : public IEsdkControlModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    EsdkControlModuleImpl(
        const std::string& drone_sn,
        const std::string& gateway_sn,
        std::shared_ptr<message_engine::IMessageEngine> message_engine,
        int session_id);
    ~EsdkControlModuleImpl() override;

    int listenEsdkCustomDataTransmissionFromEsdk(
        const std::function<void(const EsdkCustomDataTransmission& msg)>& callback) override;
    void cancelListenEsdkCustomDataTransmissionFromEsdk(int listen_id) override;

    void sendEsdkCustomDataTransmissionToEsdk(
        const std::string& value,
        const std::function<void(FlightHubError err)>& callback) override;

private:
    std::string drone_sn_;
    std::string gateway_sn_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;
    int session_id_;

    // from_esdk 监听映射：user_listen_id ↔ nc_listen_id
    std::unordered_map<int, int64_t> esdk_custom_data_from_esdk_nc_listen_ids_;
    int esdk_custom_data_from_esdk_next_id_ = 0;
    std::mutex listen_mutex_;
};

} // namespace flight_hub_sdk
