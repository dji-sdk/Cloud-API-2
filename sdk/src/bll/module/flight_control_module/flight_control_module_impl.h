// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_control_module.h"
#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "bll/manager/flight_ctrl_mgr/flight_ctrl_mgr.h"

namespace flight_hub_sdk {

class FlightControlModuleImpl : public IFlightControlModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    FlightControlModuleImpl(
        const std::string& drone_sn,
        const std::string& gateway_sn,
        std::shared_ptr<message_engine::IMessageEngine> message_engine,
        std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr);
    ~FlightControlModuleImpl() override = default;

    void drcEmergencyStop(const std::function<void(FlightHubError err)>& callback) override;

    void drcEmergencyLanding(const std::function<void(FlightHubError err)>& callback) override;
    void drcForceLanding(const std::function<void(FlightHubError err)>& callback) override;

    void returnHome(const std::function<void(FlightHubError err)>& callback) override;

    void returnHomeCancel(const std::function<void(FlightHubError err)>& callback) override;

    void drcSendStickControl(const StickControlData& data) override;

    // ── Flight Control Auxiliary (DRC) ──
    void drcSetDroneHover(int duration, const std::function<void(FlightHubError)>& callback) override;
    void drcSetFollowMouseParameter(int speed, const std::function<void(FlightHubError)>& callback) override;
    void drcSetNightLightsState(int night_lights_state, const std::function<void(FlightHubError)>& callback) override;
    void drcSetStealthState(int stealth_state, const std::function<void(FlightHubError)>& callback) override;
    void drcOpenParachute(int force, const std::function<void(FlightHubError)>& callback) override;
    void drcSetFlashLed(const FlashLedParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcCameraLookAt(const CameraLookAtParams& params, const std::function<void(FlightHubError)>& callback) override;

    // ── Northbound Push Listeners (DRC) ──
    int listenDrcOsdInfo(const std::function<void(const DrcOsdInfo&)>& callback) override;
    void cancelListenDrcOsdInfo(int listen_id) override;
    int listenDrcBatteriesInfo(const std::function<void(const DrcBatteriesInfo&)>& callback) override;
    void cancelListenDrcBatteriesInfo(int listen_id) override;
    int listenDrcParachuteInfo(const std::function<void(const DrcParachuteInfo&)>& callback) override;
    void cancelListenDrcParachuteInfo(int listen_id) override;
    int listenDrcFlashLed(const std::function<void(const FlashLedStateInfo&)>& callback) override;
    void cancelListenDrcFlashLed(int listen_id) override;

private:
    bool isDrtcRealTimeLinkActive() const;

    std::string drone_sn_;
    std::string gateway_sn_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr_;
};

} // namespace flight_hub_sdk
