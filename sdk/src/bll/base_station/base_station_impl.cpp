// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "base_station_impl.h"
#include "bll/module/live_module/live_module_impl.h"
#include "bll/module/live_device_module/live_device_module_impl.h"
#include "bll/module/rtk_calibration_module/rtk_calibration_module_impl.h"
#include "bll/module/network_test_module/network_test_module_impl.h"
#include "bll/manager/topo_registry/topo_registry.h"
#include "message_engine_factory.h"
#include "common/executors/executors.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

BaseStationImpl::BaseStationImpl(
    const std::string& sn,
    const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge,
    const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge)
    : sn_(sn),
      rtc_channel_manager_bridge_(rtc_channel_manager_bridge),
      mqtt_client_manager_bridge_(mqtt_client_manager_bridge) {
    LOG_INFO("BaseStationImpl: sn=%s", sn_.c_str());
    message_engine_ = message_engine::MessageEngineFactory::createMessageEngine(drc_sdk::common::Executors::Main());
    if (!message_engine_) {
        LOG_ERROR("BaseStationImpl: createMessageEngine failed, SN: %s", sn_.c_str());
        abort();
    }
    if (rtc_channel_manager_bridge_) {
        message_engine_->setRtcChannelManager(rtc_channel_manager_bridge_);
    }
    if (mqtt_client_manager_bridge_) {
        message_engine_->setMqttClientManager(mqtt_client_manager_bridge_);
    }
    message_engine_->initialize();
}

BaseStationImpl::~BaseStationImpl() {
    LOG_INFO("BaseStationImpl: ~BaseStationImpl sn=%s", sn_.c_str());
    {
        std::lock_guard<std::mutex> lock(live_module_mutex_);
        for (const auto& [key, live_module] : live_module_map_) {
            if (live_module) {
                live_module->stop();
            }
        }
        live_module_map_.clear();
    }
    if (message_engine_) {
        message_engine_->destroy();
    }
    message_engine::MessageEngineFactory::destroyMessageEngine(message_engine_);
}

std::shared_ptr<ILiveModule> BaseStationImpl::getLiveStream(const LiveInfo& params) {
    std::lock_guard<std::mutex> lock(live_module_mutex_);
    const std::string key = params.payload_index + ":" + params.video_id;
    if (live_module_map_.count(key) > 0) {
        return live_module_map_[key];
    }
    const auto gateway_sn = TopoRegistry::getInstance().resolveGatewaySn(sn_).value_or(sn_);
    auto live_module = std::make_shared<LiveModuleImpl>(
        params, message_engine_->getRtcChannelManager(), gateway_sn);
    live_module_map_[key] = live_module;
    return live_module;
}

std::shared_ptr<ILiveDeviceModule> BaseStationImpl::getLiveDeviceModule() {
    if (!live_device_module_) {
        live_device_module_ = std::make_shared<LiveDeviceModuleImpl>(DeviceKind::BaseStation, sn_);
    }
    return live_device_module_;
}

std::shared_ptr<IRtkCalibrationModule> BaseStationImpl::getRtkCalibrationModule() {
    if (!rtk_calibration_module_) {
        rtk_calibration_module_ = std::make_shared<RtkCalibrationModuleImpl>(sn_, "8");
    }
    return rtk_calibration_module_;
}

std::shared_ptr<INetworkTestModule> BaseStationImpl::getNetworkTestModule() {
    if (!network_test_module_) {
        network_test_module_ = std::make_shared<NetworkTestModuleImpl>(sn_);
    }
    return network_test_module_;
}

} // namespace flight_hub_sdk
