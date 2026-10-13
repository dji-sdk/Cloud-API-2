// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/psdk_control_module.h"
#include "bll/manager/psdk_ctrl_mgr/psdk_ctrl_mgr.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"
#include "notification_center/notification_center.h"
#include <functional>
#include <unordered_map>

namespace flight_hub_sdk {

class PsdkControlModuleImpl : public IPsdkControlModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit PsdkControlModuleImpl(std::shared_ptr<PsdkCtrlMgr> psdk_ctrl_mgr);
    ~PsdkControlModuleImpl() override = default;

    int listenDrcPsdkDeviceList(const std::function<void(const PsdkDeviceList &psdk_device_list)> &callback) override;
    void cancelListenDrcPsdkDeviceList(int listen_id) override;
    int listenDrcPsdkStateSummaryInfo(int psdk_index, const std::function<void(PsdkStateSummaryInfo psdk_device_list)> &callback) override;
    void cancelListenDrcPsdkStateSummaryInfo(int psdk_index, int listen_id) override;
    int listenDrcPsdkStateSpeakerInfo(int psdk_index, const std::function<void(PsdkStateSpeakerInfo speaker_info)> &callback) override;
    void cancelListenDrcPsdkStateSpeakerInfo(int psdk_index, int listen_id) override;
    int listenDrcPsdkStateSearchlightInfo(int psdk_index, const std::function<void(PsdkStateSearchlightInfo searchlight_info)> &callback) override;
    void cancelListenDrcPsdkStateSearchlightInfo(int psdk_index, int listen_id) override;
    void DrcSearchlightModeSet(int psdk_index, int mode, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSearchlightBrightnessSet(int psdk_index, int brightness, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSearchlightCalibration(int psdk_index, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSearchlightFineTuningSet(int psdk_index, int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSearchlightWideFieldModeSet(int psdk_index, int wide_field_mode, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSearchlightLightGimbalControlSet(int psdk_index, int light_gimbal_control, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSearchlightTypeSet(int psdk_index, int type, const std::function<void(FlightHubError err)>& callback) override;
    int listenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)> &callback) override;
    void cancelListenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, int listen_id) override;
    int listenDrcPsdkFloatingWindowTextInfo(int psdk_index, const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)> &callback) override;
    void cancelListenDrcPsdkFloatingWindowTextInfo(int psdk_index, int listen_id) override;
    void DrcSpeakerAudioPlayStart(int psdk_index, const PsdkSpeakerAudioPlayInfo& audio_play_info, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSpeakerTTSPlayStart(int psdk_index, const PsdkSpeakerTTSPlayInfo& tts_play_info, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSpeakerRealTimeStart(int psdk_index, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSpeakerRealTimeStop(int psdk_index, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSpeakerReplay(int psdk_index, const std::function<void(FlightHubError err)>& callback) override;
    void DrcSpeakerPlayStop(int psdk_index, const std::function<void(FlightHubError err)> &callback) override;
    void DrcSpeakerPlayVolumeSet(int psdk_index, int volume, const std::function<void(FlightHubError err)> &callback) override;
    void DrcSpeakerPlayModeSet(int psdk_index, int play_mode, const std::function<void(FlightHubError err)> &callback) override;
    void DrcSpeakerTTSSet(int psdk_index, const PsdkSpeakerTTSconfig &tts_config, const std::function<void(FlightHubError err)> &callback) override;
    void DrcWidgetValueSet(int psdk_index, int index, int value, const std::function<void(FlightHubError err)> &callback) override;
    void DrcWidgetInputBoxTextSet(int psdk_index, const std::string& value, const std::function<void(FlightHubError err)> &callback) override;
    int listenPsdkSpeakerAudioPlayStartProgress(const std::function<void(const PsdkSpeakerPlayStartProgress& msg)>& callback) override;
    void cancelListenPsdkSpeakerAudioPlayStartProgress(int listen_id) override;
    int listenPsdkSpeakerTtsPlayStartProgress(const std::function<void(const PsdkSpeakerPlayStartProgress& msg)>& callback) override;
    void cancelListenPsdkSpeakerTtsPlayStartProgress(int listen_id) override;

    int listenDronePsdkUiResource(const std::function<void(const DronePsdkUiResource& msg)>& callback) override;
    void cancelListenDronePsdkUiResource(int listen_id) override;

private:
    std::shared_ptr<PsdkCtrlMgr> psdk_ctrl_mgr_;

    // NC push 订阅：user_listen_id ↔ nc_listen_id 映射
    // 一个 module 允许多个 listen，故用 map；user_listen_id 单调自增
    std::unordered_map<int, int64_t> psdk_audio_play_progress_nc_listen_ids_;
    std::unordered_map<int, int64_t> psdk_tts_play_progress_nc_listen_ids_;
    std::unordered_map<int, int64_t> psdk_ui_resource_nc_listen_ids_;
    int psdk_audio_play_progress_next_id_ = 0;
    int psdk_tts_play_progress_next_id_ = 0;
    int psdk_ui_resource_next_id_ = 0;
};

} // namespace flight_hub_sdk
