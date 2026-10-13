// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "api/value.h"
#include "api/message.h"
#include <string>
#include "tool/callback_container.h"

namespace flight_hub_sdk {

// 对指定位置的 PSDK 的抽象, 绑定psdk_index
// 相同物理接口可能切换不同类型的 psdk 设备, 需要用psdk_type/psdk_sub_type区分
class PsdkAbs : public drc_sdk::common::AsyncCaptureProtect {
public:
    PsdkAbs(int psdk_index, std::string  drone_sn, std::string  gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine);
    ~PsdkAbs() override;

    /// psdk control
    void DrcSpeakerAudioPlayStart(const PsdkSpeakerAudioPlayInfo& audio_play_info, const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerTTSPlayStart(const PsdkSpeakerTTSPlayInfo& tts_play_info, const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerRealTimeStart(const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerRealTimeStop(const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerReplay(const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerPlayStop(const std::function<void(FlightHubError err)> &callback);
    void DrcSpeakerPlayVolumeSet(int volume, const std::function<void(FlightHubError err)> &callback);
    void DrcSpeakerPlayModeSet(int play_mode, const std::function<void(FlightHubError err)> &callback);
    void DrcSpeakerTTSSet(PsdkSpeakerTTSconfig tts_config, const std::function<void(FlightHubError err)> &callback);

    void DrcWidgetValueSet(int index, int value, const std::function<void(FlightHubError err)> &callback);
    void DrcWidgetInputBoxTextSet(const std::string& value, const std::function<void(FlightHubError err)> &callback);

    /// 官方探照灯控制
    void DrcSearchlightModeSet(int mode, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightBrightnessSet(int brightness, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightCalibration(const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightFineTuningSet(int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightWideFieldModeSet(int wide_field_mode, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightLightGimbalControlSet(int light_gimbal_control, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightTypeSet(int type, const std::function<void(FlightHubError err)>& callback);

    /// psdk osd info
    // psdk 连接状态 和 ui resource 资源监听
    int listenDrcPsdkStateSummaryInfo(const std::function<void(PsdkStateSummaryInfo psdk_device_list)>& callback);
    void cancelListenDrcPsdkStateSummaryInfo(int listen_id);

    // 官方喊话器状态
    int listenDrcPsdkStateSpeakerInfo(const std::function<void(PsdkStateSpeakerInfo speaker_info)>& callback);
    void cancelListenDrcPsdkStateSpeakerInfo(int listen_id);

    // 官方探照灯状态
    int listenDrcPsdkStateSearchlightInfo(const std::function<void(PsdkStateSearchlightInfo searchlight_info)>& callback);
    void cancelListenDrcPsdkStateSearchlightInfo(int listen_id);

    // 喊话器 音频播放进度
    int listenDrcPsdkSpeakerPlayProgressInfo(const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)>& callback);
    void cancelListenDrcPsdkSpeakerPlayProgressInfo(int listen_id);

    // 三方 PSDK - 浮窗文本推送
    int listenDrcPsdkFloatingWindowTextInfo(const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)>& callback);
    void cancelListenDrcPsdkFloatingWindowTextInfo(int listen_id);

private:
    int psdk_index_;

    /// listening
    void initPsdkStateInfoListening();
    void onReceivePsdkStateInfo(const std::shared_ptr<message_engine::DrcDrcPsdkStateInfoNorthbound>& msg);
    void initPsdkSpeakerPlayProgressListening();
    void onReceivePsdkSpeakerPlayProgress(const std::shared_ptr<message_engine::DrcDrcSpeakerPlayProgressNorthbound>& msg);
    void initPsdkFloatingWindowTextListening();
    void onReceivePsdkFloatingWindowText(const std::shared_ptr<message_engine::DrcDrcPsdkFloatingWindowTextNorthbound>& msg);

    void inferPsdkType(const std::shared_ptr<message_engine::DrcDrcPsdkStateInfoNorthbound>& msg);

    /// available calculate

    /// message engine
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// params
    std::string drone_sn_;
    std::string gateway_sn_;

    /// state & data cache
    std::shared_ptr<message_engine::DrcDrcPsdkStateInfoNorthbound> psdk_state_info_msg_;
    int psdk_type = static_cast<int>(PsdkType::UNKNOWN); // 区分是官方还是三方 PSDK 设备
    int psdk_sub_type = static_cast<int>(PsdkSubType::UNKNOWN); // 官方 PSDK 时, 用于区分具体是喊话器还是探照灯


    /// available cache

    /// callback container
    CallbackContainer<PsdkStateSummaryInfo> psdk_state_summary_info_callback_container_;
    CallbackContainer<PsdkStateSpeakerInfo> psdk_state_speaker_info_callback_container_;
    CallbackContainer<PsdkStateSearchlightInfo> psdk_state_searchlight_info_callback_container_;
    CallbackContainer<PsdkSpeakerPlayProgressInfo> psdk_speaker_play_progress_info_callback_container_;
    CallbackContainer<PsdkFloatingWindowTextInfo> psdk_floating_window_text_info_callback_container_;

    /// listen id
    int psdk_state_info_listen_id_ = -1;
    int psdk_speaker_play_progress_listen_id_ = -1;
    int psdk_floating_window_text_listen_id_ = -1;

};

}