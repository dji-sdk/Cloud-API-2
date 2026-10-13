// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/drone.h"
#include "api/drc_module.h"
#include "api/flight_settings_module.h"
#include "api/test_flight_module.h"
#include "api/onboard_ai_module.h"
#include "bll/module/onboard_ai_module/onboard_ai_module_impl.h"
#include "bll/module/psdk_control_module/psdk_control_module_impl.h"
#include "bll/module/camera_control_module/camera_control_module_impl.h"
#include "bll/module/drone_osd_module/drone_osd_module_impl.h"
#include "bll/module/device_debug_module/device_debug_module_impl.h"
#include "bll/module/ai_box_module/ai_box_module_impl.h"
#include "api/ai_box_module.h"
#include "i_message_engine.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"
#include "common/util/async_capture_protect.h"
#include "base/async/timer.h"
#include "manager/drc_link_mgr/drc_link_mgr.h"
#include "manager/flight_ctrl_mgr/flight_ctrl_mgr.h"
#include "manager/payload_ctrl_mgr/payload_ctrl_mgr.h"
#include "manager/psdk_ctrl_mgr/psdk_ctrl_mgr.h"
#include "manager/ai_ctrl_mgr/ai_ctrl_mgr.h"
#include <mutex>

namespace flight_hub_sdk {

class DroneImpl : public IDrone, public drc_sdk::common::AsyncCaptureProtect {

public:
    DroneImpl(
        DeviceTopo device_topo,
        const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge = nullptr,
        const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge = nullptr);
    ~DroneImpl() override;

    /// live stream
    std::shared_ptr<ILiveModule> getLiveStream(const LiveInfo& params) override;
    std::shared_ptr<ILiveRecordModule> getLiveRecord(const LiveInfo& params) override;
    std::shared_ptr<ILiveDeviceModule> getLiveDeviceModule() override;
    std::shared_ptr<ITakeoffModule> getTakeoffModule() override;
    std::shared_ptr<IFlightControlModule> getFlightControlModule() override;
    std::shared_ptr<IControlAuthModule> getControlAuthModule() override;
    std::shared_ptr<IDrcModule> getDrcModule() override;
    std::shared_ptr<IFlightSettingsModule> getFlightSettingsModule() override;
    std::shared_ptr<ITestFlightModule> getTestFlightModule() override;
    std::shared_ptr<IOnboardAiModule> getOnboardAiModule() override;
    std::shared_ptr<IPsdkControlModule> getPsdkControlModule() override;
    std::shared_ptr<ICameraControlModule> getCameraControlModule() override;
    std::shared_ptr<IDroneOsdModule> getDroneOsdModule() override;
    std::shared_ptr<IFlyToPointModule> getFlyToPointModule() override;
    std::shared_ptr<IFlightTaskControlModule> getFlightTaskControlModule() override;
    std::shared_ptr<IPoiModule> getPoiModule() override;
    std::shared_ptr<IInFlightWaylineModule> getInFlightWaylineModule() override;
    std::shared_ptr<IDeviceStateModule> getDeviceStateModule() override;
    std::shared_ptr<IDeviceHMSModule> getDeviceHMSModule() override;
    std::shared_ptr<IDeviceDebugModule> getDeviceDebugModule() override;
    std::shared_ptr<IAIBoxModule> getAIBoxModule() override;

private:
    void initDrcLinkMgr();
    void initFlightCtrlMgr();
    void initPayloadCtrlMgr();
    void initPsdkCtrlMgr();
    void initAICtrlMgr();

    void initListen();

    void initLiveRecordListener();

    std::string getGatewaySn();

    /// params
    DeviceTopo device_topo_;
    std::string drone_sn_;
    std::string drone_model_key_;
    std::string dock_sn_;
    int instance_id_ = 0;
    std::string log_tag_;

    /// message engine
    std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager_bridge_;
    std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager_bridge_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// manager
    std::shared_ptr<DrcLinkMgr> drc_link_mgr_;
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr_;
    std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr_;
    std::shared_ptr<PsdkCtrlMgr> psdk_ctrl_mgr_;
    std::shared_ptr<AICtrlMgr> ai_ctrl_mgr_;

    /// module
    std::shared_ptr<IFlightTaskControlModule> flight_task_control_module_;
    std::shared_ptr<IFlyToPointModule> fly_to_point_module_;
    std::shared_ptr<IPoiModule> poi_module_;
    std::shared_ptr<IInFlightWaylineModule> in_flight_wayline_module_;
    std::shared_ptr<ITakeoffModule> takeoff_module_;
    std::shared_ptr<IFlightControlModule> flight_control_module_;
    std::shared_ptr<IControlAuthModule> control_auth_module_;
    std::shared_ptr<IDrcModule> drc_module_;
    std::shared_ptr<IFlightSettingsModule> flight_settings_module_;
    std::shared_ptr<ITestFlightModule> test_flight_module_;
    std::shared_ptr<IOnboardAiModule> onboard_ai_module_;
    std::shared_ptr<IPsdkControlModule> psdk_control_module_;
    std::shared_ptr<ICameraControlModule> camera_control_module_;
    std::shared_ptr<IDroneOsdModule> drone_osd_module_;
    std::shared_ptr<IDeviceStateModule> device_state_module_;
    std::shared_ptr<IDeviceHMSModule> device_hms_module_;
    std::shared_ptr<IDeviceDebugModule> device_debug_module_;
    std::shared_ptr<IAIBoxModule> ai_box_module_;
    std::unordered_map<std::string, std::shared_ptr<ILiveModule>> live_module_map_;
    std::unordered_map<std::string, std::shared_ptr<ILiveRecordModule>> live_record_module_map_;

    /// state & data cache
    message_engine::DrcDrcCameraOsdInfoPushNorthbound camera_info_msg_;

    /// timer
    std::shared_ptr<ILiveDeviceModule> live_device_module_;

    std::mutex flight_control_module_mutex_;
    std::mutex control_auth_module_mutex_;
    std::mutex flight_settings_module_mutex_;
    std::mutex test_flight_module_mutex_;
    std::mutex onboard_ai_module_mutex_;
    std::mutex psdk_control_module_mutex_;
    std::mutex camera_control_module_mutex_;
    std::mutex device_debug_module_mutex_;
    std::mutex ai_box_module_mutex_;
    std::mutex live_device_module_mutex_;
    std::mutex live_module_mutex_;

    int live_record_info_listen_id_ = -1;
};

} // namespace flight_hub_sdk
