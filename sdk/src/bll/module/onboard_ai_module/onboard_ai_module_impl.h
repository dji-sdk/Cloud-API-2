// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/onboard_ai_module.h"
#include "bll/manager/ai_ctrl_mgr/ai_ctrl_mgr.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class OnboardAiModuleImpl : public IOnboardAiModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit OnboardAiModuleImpl(std::shared_ptr<AICtrlMgr> ai_ctrl_mgr);
    ~OnboardAiModuleImpl() override = default;

    int listenAIDataInfo(const std::function<void(const AiDataInfo& state)>& callback) override;
    void cancelListenAIDataInfo(int listen_id) override;

    void AIIdentifySet(const AiIdentifySetParam& param, const std::function<void(FlightHubError)>& callback) override;
    void AIIdentifyFilterSet(const AiIdentifyFilterSetParam& param, const std::function<void(FlightHubError)>& callback) override;
    void AIIdentifyScoreSet(const AiIdentifyScoreSetParam& param, const std::function<void(FlightHubError)>& callback) override;
    void AIIdentifyScoreModeSet(const AiIdentifyScoreModeSetParam& param, const std::function<void(FlightHubError)>& callback) override;
    void AIIdentifyScoreReset(const AiIdentifyScoreResetParam& param, const std::function<void(FlightHubError)>& callback) override;
    void AIModelSelect(const AiModelSelectParam& param, const std::function<void(FlightHubError)>& callback) override;

    // ── AI Spotlight Zoom (Tracking) DRC ──
    void DrcAISpotlightZoomSet(int on, const std::function<void(FlightHubError)>& callback) override;
    void DrcAISpotlightZoomSelect(const AiSpotlightZoomSelectParams& params, const std::function<void(FlightHubError)>& callback) override;
    void DrcAISpotlightZoomConfirm(const std::function<void(FlightHubError)>& callback) override;
    void DrcAISpotlightZoomStop(const std::function<void(FlightHubError)>& callback) override;
    void DrcAISpotlightZoomTrack(int target_index, const std::function<void(FlightHubError)>& callback) override;

    // ── AI Alarm DRC ──
    void DrcAIAlarmStart(const AiAlarmStartParams& params, const std::function<void(FlightHubError)>& callback) override;
    void DrcAIAlarmStop(const std::function<void(FlightHubError)>& callback) override;
    void DrcAIModelsConfig(const AiModelsConfigParams& params, const std::function<void(FlightHubError)>& callback) override;
    int listenDrcAIAlarmState(const std::function<void(const AiAlarmStateInfo&)>& callback) override;
    void cancelListenDrcAIAlarmState(int listen_id) override;

private:
    std::shared_ptr<AICtrlMgr> ai_ctrl_mgr_;
};

} // namespace flight_hub_sdk
