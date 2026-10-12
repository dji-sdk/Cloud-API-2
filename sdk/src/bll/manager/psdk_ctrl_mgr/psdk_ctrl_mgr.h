// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "api/value.h"
#include "api/message.h"
#include <string>
#include "tool/callback_container.h"
#include "psdk_abs.h"

namespace flight_hub_sdk {

class PsdkCtrlMgr : public drc_sdk::common::AsyncCaptureProtect {
public:
    PsdkCtrlMgr(std::string  drone_sn, std::string  gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine);
    ~PsdkCtrlMgr() override;

    /// psdk osd info
    // psdk 连接状态 和 ui resource 资源监听
    int listenDrcPsdkDeviceList(const std::function<void(PsdkDeviceList psdk_device_list)>& callback);
    void cancelListenDrcPsdkDeviceList(int listen_id);

    int listenDrcPsdkStateSummaryInfo(int psdk_index, const std::function<void(PsdkStateSummaryInfo psdk_device_list)>& callback);
    void cancelListenDrcPsdkStateSummaryInfo(int psdk_index, int listen_id);

    int listenDrcPsdkStateSpeakerInfo(int psdk_index, const std::function<void(PsdkStateSpeakerInfo speaker_info)>& callback);
    void cancelListenDrcPsdkStateSpeakerInfo(int psdk_index, int listen_id);

    int listenDrcPsdkStateSearchlightInfo(int psdk_index, const std::function<void(PsdkStateSearchlightInfo searchlight_info)>& callback);
    void cancelListenDrcPsdkStateSearchlightInfo(int psdk_index, int listen_id);

    int listenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)>& callback);
    void cancelListenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, int listen_id);

    int listenDrcPsdkFloatingWindowTextInfo(int psdk_index, const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)>& callback);
    void cancelListenDrcPsdkFloatingWindowTextInfo(int psdk_index, int listen_id);

    /// psdk control
    void DrcSpeakerAudioPlayStart(int psdk_index, const PsdkSpeakerAudioPlayInfo& audio_play_info, const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerTTSPlayStart(int psdk_index, const PsdkSpeakerTTSPlayInfo& tts_play_info, const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerRealTimeStart(int psdk_index, const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerRealTimeStop(int psdk_index, const std::function<void(FlightHubError err)>& callback);
    void DrcSpeakerReplay(int psdk_index, const std::function<void(FlightHubError err)>& callback); // 喊话器重新播放
    void DrcSpeakerPlayStop(int psdk_index, const std::function<void(FlightHubError err)>& callback); // 喊话器停止播放
    void DrcSpeakerPlayVolumeSet(int psdk_index, int volume, const std::function<void(FlightHubError err)> &callback); // 喊话器播放音量设置
    void DrcSpeakerPlayModeSet(int psdk_index, int play_mode, const std::function<void(FlightHubError err)> &callback); // 喊话器播放模式设置 - 单次/循环
    void DrcSpeakerTTSSet(int psdk_index, PsdkSpeakerTTSconfig tts_config, const std::function<void(FlightHubError err)> &callback); // 喊话器TTS播放设置

    void DrcWidgetValueSet(int psdk_index, int index, int value, const std::function<void(FlightHubError err)> &callback);
    void DrcWidgetInputBoxTextSet(int psdk_index, const std::string& value, const std::function<void(FlightHubError err)> &callback);

    /// 官方探照灯控制
    void DrcSearchlightModeSet(int psdk_index, int mode, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightBrightnessSet(int psdk_index, int brightness, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightCalibration(int psdk_index, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightFineTuningSet(int psdk_index, int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightWideFieldModeSet(int psdk_index, int wide_field_mode, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightLightGimbalControlSet(int psdk_index, int light_gimbal_control, const std::function<void(FlightHubError err)>& callback);
    void DrcSearchlightTypeSet(int psdk_index, int type, const std::function<void(FlightHubError err)>& callback);

    /// SN accessors（供 module impl 订阅 NC / DRC 时构造 topic）
    const std::string& getDroneSn() const { return drone_sn_; }
    const std::string& getGatewaySn() const { return gateway_sn_; }

private:
    /// psdk status listening
    void initPsdkUiResourceListening();
    void onReceivePsdkUiResource(const std::shared_ptr<message_engine::DrcDrcPsdkUiResourceNorthbound>& msg);

    /// message engine
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// params
    std::string drone_sn_;
    std::string gateway_sn_;

    /// listen id
    int psdk_ui_resource_listen_id_ = -1;

    /// state & data cache
    PsdkDeviceList psdk_device_list_msg_;

    /// callback container
    CallbackContainer<PsdkDeviceList> psdk_device_list_callback_container_;

    /// psdk abs
    std::unordered_map<int, std::shared_ptr<PsdkAbs>> psdk_abs_map_; // psdk_index to PsdkAbs 保存已经 ready 的设备
    std::unordered_map<int, PsdkDeviceInfo> psdk_abs_info_map_; // psdk_index to PsdkAbs

};

}  // namespace flight_hub_sdk