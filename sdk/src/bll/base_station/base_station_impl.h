// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/base_station.h"
#include "i_message_engine.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"
#include <memory>
#include <mutex>
#include <string>
#include <unordered_map>

namespace flight_hub_sdk {

class BaseStationImpl : public IBaseStation {
public:
    BaseStationImpl(
        const std::string& sn,
        const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge = nullptr,
        const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge = nullptr);
    ~BaseStationImpl() override;

    const std::string& getSn() const override { return sn_; }

    std::shared_ptr<ILiveModule> getLiveStream(const LiveInfo& params) override;
    std::shared_ptr<ILiveDeviceModule> getLiveDeviceModule() override;

    std::shared_ptr<IRtkCalibrationModule> getRtkCalibrationModule() override;
    std::shared_ptr<INetworkTestModule> getNetworkTestModule() override;

private:
    std::string sn_;
    std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager_bridge_;
    std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager_bridge_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    std::unordered_map<std::string, std::shared_ptr<ILiveModule>> live_module_map_;
    std::shared_ptr<ILiveDeviceModule> live_device_module_;
    std::shared_ptr<IRtkCalibrationModule> rtk_calibration_module_;
    std::shared_ptr<INetworkTestModule> network_test_module_;
    std::mutex live_module_mutex_;
};

} // namespace flight_hub_sdk
