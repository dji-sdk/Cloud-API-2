// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "api/value.h"
#include "api/message.h"
#include <string>
#include "tool/callback_container.h"
#include "BusinessManager.h"

namespace flight_hub_sdk {

class AICtrlMgr : public drc_sdk::common::AsyncCaptureProtect {
public:
    AICtrlMgr(const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine);
    ~AICtrlMgr() override;
    void AIIdentifySet(const AiIdentifySetParam& param, const std::function<void(FlightHubError)>& callback);
    void AIIdentifyFilterSet(const AiIdentifyFilterSetParam& param, const std::function<void(FlightHubError)>& callback);
    void AIIdentifyScoreSet(const AiIdentifyScoreSetParam& param, const std::function<void(FlightHubError)>& callback);
    void AIIdentifyScoreModeSet(const AiIdentifyScoreModeSetParam& param, const std::function<void(FlightHubError)>& callback);
    void AIIdentifyScoreReset(const AiIdentifyScoreResetParam& param, const std::function<void(FlightHubError)>& callback);
    void AIModelSelect(const AiModelSelectParam& param, const std::function<void(FlightHubError)>& callback);

    // ── AI Spotlight Zoom (Tracking) ──
    void DrcAISpotlightZoomSet(int on, const std::function<void(FlightHubError err)>& callback);
    void DrcAISpotlightZoomSelect(const AiSpotlightZoomSelectParams& params, const std::function<void(FlightHubError err)>& callback);
    void DrcAISpotlightZoomConfirm(const std::function<void(FlightHubError err)>& callback);
    void DrcAISpotlightZoomStop(const std::function<void(FlightHubError err)>& callback);
    void DrcAISpotlightZoomTrack(int target_index, const std::function<void(FlightHubError err)>& callback);

    // ── AI Alarm ──
    void DrcAIAlarmStart(const AiAlarmStartParams& params, const std::function<void(FlightHubError err)>& callback);
    void DrcAIAlarmStop(const std::function<void(FlightHubError err)>& callback);
    void DrcAIModelsConfig(const AiModelsConfigParams& params, const std::function<void(FlightHubError err)>& callback);

    // ── AI Alarm State Push (Observe) ──
    int listenDrcAIAlarmState(const std::function<void(const AiAlarmStateInfo& info)>& callback);
    void cancelListenDrcAIAlarmState(int listen_id);

    int listenAIDataInfo(const std::function<void(const AiDataInfo& state)> callback);
    void cancelListenAIDataInfo(int listen_id);
private:
    void initListening();
    void onReceiveAiInfo(const std::shared_ptr<message_engine::DrcDrcAiInfoPushNorthbound>& msg);
    void onReceiveAiAlarmState(const std::shared_ptr<message_engine::DrcDrcAiAlarmStatePushNorthbound>& msg);
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// params
    std::string drone_sn_;
    std::string gateway_sn_;

    /// callback container
    CallbackContainer<AiDataInfo> ai_data_info_callback_container_;
    CallbackContainer<AiAlarmStateInfo> ai_alarm_state_callback_container_;

    // listen id
    int ai_info_listen_id_ = -1;
    int ai_alarm_state_listen_id_ = -1;
};

}  // namespace flight_hub_sdk