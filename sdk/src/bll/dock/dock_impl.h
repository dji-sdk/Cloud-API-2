// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/dock.h"
#include "api/deploy_module.h"
#include "api/dock_control_module.h"
#include "bll/module/dock_osd_module/dock_osd_module_impl.h"
#include "bll/module/device_debug_module/device_debug_module_impl.h"
#include "i_message_engine.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"
#include "common/util/async_capture_protect.h"
#include "base/async/timer.h"
#include <mutex>
#include <string>
#include <memory>
#include <unordered_map>

namespace flight_hub_sdk {

class DockImpl : public IDock, public drc_sdk::common::AsyncCaptureProtect {
public:
    DockImpl(
        const std::string& sn,
        const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge = nullptr,
        const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge = nullptr);
    ~DockImpl() override;

    std::shared_ptr<ILiveModule> getLiveStream(const LiveInfo& params) override;
    std::shared_ptr<ILiveRecordModule> getLiveRecord(const LiveInfo& params) override;

    std::shared_ptr<ILiveDeviceModule> getLiveDeviceModule() override;
    std::shared_ptr<IDeployModule> getDeployModule() override;
    std::shared_ptr<IDockControlModule> getDockControlModule() override;
    std::shared_ptr<IDockOsdModule> getDockOsdModule() override;

    std::shared_ptr<IRtkCalibrationModule> getRtkCalibrationModule() override;
    std::shared_ptr<INetworkTestModule> getNetworkTestModule() override;
    std::shared_ptr<IDeviceDebugModule> getDeviceDebugModule() override;
    std::shared_ptr<IDeviceStateModule> getDeviceStateModule() override;
    std::shared_ptr<IDeviceHMSModule> getDeviceHMSModule() override;

private:
    std::string sn_;
    std::unordered_map<std::string, std::shared_ptr<ILiveModule>> live_module_map_;
    std::unordered_map<std::string, std::shared_ptr<ILiveRecordModule>> live_record_module_map_;
    std::shared_ptr<IDeployModule> deploy_module_;
    std::shared_ptr<IDockControlModule> dock_control_module_;
    std::shared_ptr<IDockOsdModule> dock_osd_module_;

    // message engine
    std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager_bridge_;
    std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager_bridge_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    std::shared_ptr<ILiveDeviceModule> live_device_module_;
    int dock_live_record_info_listen_id_ = -1;
    std::shared_ptr<IRtkCalibrationModule> rtk_calibration_module_;
    std::shared_ptr<INetworkTestModule> network_test_module_;
    std::shared_ptr<IDeviceDebugModule> device_debug_module_;
    std::shared_ptr<IDeviceStateModule> device_state_module_;
    std::shared_ptr<IDeviceHMSModule> device_hms_module_;
    std::mutex live_module_mutex_;
};

} // namespace flight_hub_sdk
