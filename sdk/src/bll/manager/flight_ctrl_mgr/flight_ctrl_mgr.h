// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "api/value.h"
#include "api/message.h"
#include <string>
#include <functional>
#include "tool/callback_container.h"
#include <BusinessManager.h>

namespace flight_hub_sdk {

class FlightCtrlMgr : public drc_sdk::common::AsyncCaptureProtect {
public:
    FlightCtrlMgr(const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine, int drone_instance_id);
    ~FlightCtrlMgr() override;

    // acquire/release 发 HTTP（手飞云控）；listen 订阅 NC topic_drone_cloud_control_status
    void acquireFlightControlAuthority(const std::function<void(FlightHubError)>& callback);
    void releaseFlightControlAuthority(const std::function<void(FlightHubError)>& callback);
    int listenFlightControlAuthority(const std::function<void(bool)>& callback);
    void cancelListenFlightControlAuthority(int listen_id);

    // HSI 信息监听
    int listenHsiInfo(const std::function<void(const HsiInfo&)>& callback);
    void cancelListenHsiInfo(int listen_id);

    // ── Flight Control Auxiliary ──
    void drcSetDroneHover(int duration, const std::function<void(FlightHubError err)>& callback);
    void drcSetFollowMouseParameter(int speed, const std::function<void(FlightHubError err)>& callback);
    void drcSetNightLightsState(int night_lights_state, const std::function<void(FlightHubError err)>& callback);
    void drcSetStealthState(int stealth_state, const std::function<void(FlightHubError err)>& callback);
    void drcOpenParachute(int force, const std::function<void(FlightHubError err)>& callback);
    void drcSetFlashLed(const FlashLedParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcCameraLookAt(const CameraLookAtParams& params, const std::function<void(FlightHubError err)>& callback);

    // ── Northbound Observes ──
    int listenDrcOsdInfo(const std::function<void(const DrcOsdInfo& info)>& callback);
    void cancelListenDrcOsdInfo(int listen_id);
    int listenDrcBatteriesInfo(const std::function<void(const DrcBatteriesInfo& info)>& callback);
    void cancelListenDrcBatteriesInfo(int listen_id);
    int listenDrcParachuteInfo(const std::function<void(const DrcParachuteInfo& info)>& callback);
    void cancelListenDrcParachuteInfo(int listen_id);
    int listenDrcFlashLed(const std::function<void(const FlashLedStateInfo& info)>& callback);
    void cancelListenDrcFlashLed(int listen_id);

private:
    int instance_id_ = 0;
    std::string log_tag_;

    void initFlightControlAuthorityListening();
    void onReceiveFlightControlAuthority(const notification_center::ControlRightsCloudControlStatus& msg);

    // HSI 信息
    void initHsiInfoListening();
    void onReceiveHsiInfo(const message_engine::DrcHsiInfoPushNorthbound& msg);

    // OSD Info
    void initOsdInfoListening();
    void onReceiveOsdInfo(const std::shared_ptr<message_engine::DrcOsdInfoPushNorthbound>& msg);

    // Batteries Info
    void initBatteriesInfoListening();
    void onReceiveBatteriesInfo(const std::shared_ptr<message_engine::DrcDrcBatteriesInfoPushNorthbound>& msg);

    // Parachute Info
    void initParachuteInfoListening();
    void onReceiveParachuteInfo(const std::shared_ptr<message_engine::DrcDrcParachuteInfoPushNorthbound>& msg);

    // Flash LED state (from drc_drone_state_push)
    void initFlashLedListening();
    void onReceiveFlashLedState(const std::shared_ptr<message_engine::DrcDrcDroneStatePushNorthbound>& msg);

    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// params
    std::string drone_sn_;
    std::string gateway_sn_;

    notification_center::ControlRightsCloudControlStatus flight_control_authority_msg_;

    /// callback container
    CallbackContainer<bool> flight_control_authority_callback_container_;
    CallbackContainer<HsiInfo> hsi_info_callback_container_;
    CallbackContainer<DrcOsdInfo> osd_info_callback_container_;
    CallbackContainer<DrcBatteriesInfo> batteries_info_callback_container_;
    CallbackContainer<DrcParachuteInfo> parachute_info_callback_container_;
    CallbackContainer<FlashLedStateInfo> flash_led_callback_container_;

    // listen id
    int flight_control_authority_listen_id_ = -1;
    int hsi_info_listen_id_ = -1;
    int osd_info_listen_id_ = -1;
    int batteries_info_listen_id_ = -1;
    int parachute_info_listen_id_ = -1;
    int flash_led_listen_id_ = -1;
};

}  // namespace flight_hub_sdk