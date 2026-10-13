// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "psdk_abs.h"

#include "base/common/logger.h"

using namespace flight_hub_sdk;

constexpr auto kPsdkWidgetNameAS1 = "Speaker";
constexpr auto kPsdkWidgetNameAL1 = "Searchlight";

PsdkAbs::PsdkAbs(int psdk_index, std::string drone_sn, std::string gateway_sn, const std::shared_ptr<message_engine::IMessageEngine> &message_engine) : psdk_index_(psdk_index), message_engine_(message_engine), drone_sn_(std::move(drone_sn)), gateway_sn_(std::move(gateway_sn)) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] ctor");
    initPsdkStateInfoListening();
    initPsdkSpeakerPlayProgressListening();
    initPsdkFloatingWindowTextListening();
}

PsdkAbs::~PsdkAbs() {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] dtor");
    
    // 1. 先停止所有消息监听，防止新的回调
    if (message_engine_ && message_engine_->getCloudMessageManager()) {
        if (psdk_state_info_listen_id_ >= 0) {
            message_engine_->getCloudMessageManager()->CancelObserveMessage(psdk_state_info_listen_id_);
            psdk_state_info_listen_id_ = -1;
        }
        if (psdk_speaker_play_progress_listen_id_ >= 0) {
            message_engine_->getCloudMessageManager()->CancelObserveMessage(psdk_speaker_play_progress_listen_id_);
            psdk_speaker_play_progress_listen_id_ = -1;
        }
        if (psdk_floating_window_text_listen_id_ >= 0) {
            message_engine_->getCloudMessageManager()->CancelObserveMessage(psdk_floating_window_text_listen_id_);
            psdk_floating_window_text_listen_id_ = -1;
        }
    }    
}

int PsdkAbs::listenDrcPsdkStateSummaryInfo(const std::function<void(PsdkStateSummaryInfo psdk_device_list)> &callback) {
    auto listen_id = psdk_state_summary_info_callback_container_.addCallback(callback);
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] listenDrcPsdkStateSummaryInfo listen_id: %d", listen_id);
    return listen_id;
}

void PsdkAbs::cancelListenDrcPsdkStateSummaryInfo(int listen_id) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] cancelListenDrcPsdkStateSummaryInfo listen_id: %d", listen_id);
    psdk_state_summary_info_callback_container_.removeCallback(listen_id);
}

int PsdkAbs::listenDrcPsdkStateSpeakerInfo(const std::function<void(PsdkStateSpeakerInfo speaker_info)> &callback) {
    auto listen_id = psdk_state_speaker_info_callback_container_.addCallback(callback);
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] listenDrcPsdkStateSpeakerInfo listen_id: %d", listen_id);
    return listen_id;
}

void PsdkAbs::cancelListenDrcPsdkStateSpeakerInfo(int listen_id) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] cancelListenDrcPsdkStateSpeakerInfo listen_id: %d", listen_id);
    psdk_state_speaker_info_callback_container_.removeCallback(listen_id);
}

int PsdkAbs::listenDrcPsdkStateSearchlightInfo(const std::function<void(PsdkStateSearchlightInfo searchlight_info)> &callback) {
    auto listen_id = psdk_state_searchlight_info_callback_container_.addCallback(callback);
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] listenDrcPsdkStateSearchlightInfo listen_id: %d", listen_id);
    return listen_id;
}

void PsdkAbs::cancelListenDrcPsdkStateSearchlightInfo(int listen_id) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] cancelListenDrcPsdkStateSearchlightInfo listen_id: %d", listen_id);
    psdk_state_searchlight_info_callback_container_.removeCallback(listen_id);
}

int PsdkAbs::listenDrcPsdkSpeakerPlayProgressInfo(const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)> &callback) {
    auto listen_id = psdk_speaker_play_progress_info_callback_container_.addCallback(callback);
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] listenDrcPsdkSpeakerPlayProgressInfo listen_id: %d", listen_id);
    return listen_id;
}

void PsdkAbs::cancelListenDrcPsdkSpeakerPlayProgressInfo(int listen_id) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] cancelListenDrcPsdkSpeakerPlayProgressInfo listen_id: %d", listen_id);
    psdk_speaker_play_progress_info_callback_container_.removeCallback(listen_id);
}

int PsdkAbs::listenDrcPsdkFloatingWindowTextInfo(const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)> &callback) {
    auto listen_id = psdk_floating_window_text_info_callback_container_.addCallback(callback);
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] listenDrcPsdkFloatingWindowTextInfo listen_id: %d", listen_id);
    return listen_id;
}

void PsdkAbs::cancelListenDrcPsdkFloatingWindowTextInfo(int listen_id) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] cancelListenDrcPsdkFloatingWindowTextInfo listen_id: %d", listen_id);
    psdk_floating_window_text_info_callback_container_.removeCallback(listen_id);
}

void PsdkAbs::initPsdkStateInfoListening() {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] initPsdkStateInfoListening");

    WeakDummy(weak_ptr);
    psdk_state_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcPsdkStateInfoNorthbound([this, weak_ptr](const std::shared_ptr<message_engine::DrcDrcPsdkStateInfoNorthbound>& msg)->void{
        WeakDummyReturn(weak_ptr);
        if (msg == nullptr) {
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] initPsdkStateInfoListening ObserveDrcDrcPsdkStateInfoNorthbound msg == nullptr");
            return;
        }
        if (msg->msg.psdk_index != psdk_index_) {
            // 不是自己的消息
            return;
        }
        onReceivePsdkStateInfo(msg);
    }, nullptr);
    if (psdk_state_info_listen_id_ < 0) {
        LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] initPsdkStateInfoListening ObserveDrcDrcPsdkStateInfoNorthbound failed, psdk_state_info_listen_id_ %d", psdk_state_info_listen_id_);
    }
}

void PsdkAbs::onReceivePsdkStateInfo(const std::shared_ptr<message_engine::DrcDrcPsdkStateInfoNorthbound> &msg) {
    psdk_state_info_msg_ = msg;
    LOG_DEBUG("[PsdkCtrlMgr][PsdkAbs] onReceivePsdkStateInfo: %s", psdk_state_info_msg_->ToJsonString().c_str());

    inferPsdkType(msg); // 推断 psdk 类型

    PsdkStateSummaryInfo summary_info{
        .psdk_index = psdk_state_info_msg_->msg.psdk_index,
        .psdk_type = psdk_type,
        .psdk_sub_type = psdk_sub_type,
        .psdk_name = psdk_state_info_msg_->msg.psdk_name,
        .psdk_sn = psdk_state_info_msg_->msg.psdk_sn,
        .psdk_version = psdk_state_info_msg_->msg.psdk_version,
        .psdk_lib_version = psdk_state_info_msg_->msg.psdk_lib_version,
        .psdk_restrict_state = psdk_state_info_msg_->msg.psdk_restrict_state
    };
    psdk_state_summary_info_callback_container_.notifyCallbackIfChanged(summary_info);

    if (psdk_sub_type == static_cast<int>(PsdkSubType::SPEAKER)) {
        PsdkStateSpeakerInfo speaker_info{
            .work_mode = msg->msg.speaker.work_mode,
            .play_mode = msg->msg.speaker.play_mode,
            .system_state = msg->msg.speaker.system_state,
            .play_volume = msg->msg.speaker.play_volume,
            .play_file_name = msg->msg.speaker.play_file_name,
            .play_file_md5 = msg->msg.speaker.play_file_md5,
            .tts_volume = msg->msg.speaker.tts_volume,
            .tts_type = msg->msg.speaker.tts_type,
            .tts_language = msg->msg.speaker.tts_language,
            .tts_speed = msg->msg.speaker.tts_speed,
        };
        psdk_state_speaker_info_callback_container_.notifyCallbackIfChanged(speaker_info);
    }

    if (psdk_sub_type == static_cast<int>(PsdkSubType::SEARCHLIGHT)) {
        PsdkStateSearchlightInfo searchlight_info{
            .work_mode = msg->msg.light.work_mode,
            .brightness = msg->msg.light.brightness,
            .calibration_status = msg->msg.light.calibration_status,
            .calibration_progress = msg->msg.light.calibration_progress,
            .left_value = msg->msg.light.left_value,
            .right_value = msg->msg.light.right_value,
            .wide_field_mode = msg->msg.light.wide_field_mode ? 1 : 0,
            .light_gimbal_control = msg->msg.light.light_gimbal_control ? 1 : 0,
            .lighting_type = msg->msg.light.lighting_type,
        };
        psdk_state_searchlight_info_callback_container_.notifyCallbackIfChanged(searchlight_info);
    }
}

void PsdkAbs::inferPsdkType(const std::shared_ptr<message_engine::DrcDrcPsdkStateInfoNorthbound> &msg) {
    psdk_type = static_cast<int>(PsdkType::UNKNOWN);
    if (msg->msg.psdk_type == static_cast<int>(PsdkType::UNOFFICIAL)) {
        psdk_type = static_cast<int>(PsdkType::UNOFFICIAL);
    }
    if (msg->msg.psdk_type == static_cast<int>(PsdkType::OFFICIAL)) {
        psdk_type = static_cast<int>(PsdkType::OFFICIAL);
    }
    if (msg->msg.psdk_type == static_cast<int>(PsdkType::MANIFOLD3)) {
        psdk_type = static_cast<int>(PsdkType::MANIFOLD3);
    }

    if (psdk_type == static_cast<int>(PsdkType::OFFICIAL)) {
        // 进一步判断是喊话器还是探照灯
        if (msg->msg.psdk_name == kPsdkWidgetNameAS1) {
            psdk_sub_type = static_cast<int>(PsdkSubType::SPEAKER);
        }
        if (msg->msg.psdk_name == kPsdkWidgetNameAL1) {
            psdk_sub_type = static_cast<int>(PsdkSubType::SEARCHLIGHT);
        }
    }
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] inferPsdkType, psdk_type: %d, psdk_sub_type: %d", psdk_type, psdk_sub_type);
}

void PsdkAbs::initPsdkSpeakerPlayProgressListening() {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] initPsdkSpeakerPlayProgressListening");

    WeakDummy(weak_ptr);
    psdk_speaker_play_progress_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcSpeakerPlayProgressNorthbound([this, weak_ptr](const std::shared_ptr<message_engine::DrcDrcSpeakerPlayProgressNorthbound>& msg)->void{
        WeakDummyReturn(weak_ptr);
        if (msg == nullptr) {
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] initPsdkSpeakerPlayProgressListening ObserveDrcDrcSpeakerPlayProgressNorthbound msg == nullptr");
            return;
        }
        if (msg->msg.psdk_index != psdk_index_) {
            // 不是自己的消息
            return;
        }
        onReceivePsdkSpeakerPlayProgress(msg);
    }, nullptr);
    if (psdk_speaker_play_progress_listen_id_ < 0) {
        LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] initPsdkSpeakerPlayProgressListening ObserveDrcDrcSpeakerPlayProgressNorthbound failed, psdk_speaker_play_progress_listen_id_ %d", psdk_speaker_play_progress_listen_id_);
    }
}

void PsdkAbs::onReceivePsdkSpeakerPlayProgress(const std::shared_ptr<message_engine::DrcDrcSpeakerPlayProgressNorthbound> &msg) {
    LOG_DEBUG("[PsdkCtrlMgr][PsdkAbs] onReceivePsdkSpeakerPlayProgress: %s", msg->ToJsonString().c_str());

    PsdkSpeakerPlayProgressInfo play_progress_info{
        .psdk_index = msg->msg.psdk_index,
        .result = msg->msg.result,
        .status = msg->msg.status,
        .step_key = msg->msg.progress.step_key,
        .percent = msg->msg.progress.percent,
        .md5 = msg->msg.md5
    };
    psdk_speaker_play_progress_info_callback_container_.notifyCallbackIfChanged(play_progress_info);
}

void PsdkAbs::initPsdkFloatingWindowTextListening() {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] initPsdkFloatingWindowTextListening");

    WeakDummy(weak_ptr);
    psdk_floating_window_text_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcPsdkFloatingWindowTextNorthbound([this, weak_ptr](const std::shared_ptr<message_engine::DrcDrcPsdkFloatingWindowTextNorthbound>& msg)->void{
        WeakDummyReturn(weak_ptr);
        if (msg == nullptr) {
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] initPsdkFloatingWindowTextListening ObserveDrcDrcPsdkFloatingWindowTextNorthbound msg == nullptr");
            return;
        }
        if (msg->msg.psdk_index != psdk_index_) {
            // 不是自己的消息
            return;
        }
        onReceivePsdkFloatingWindowText(msg);
    }, nullptr);
    if (psdk_floating_window_text_listen_id_ < 0) {
        LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] initPsdkFloatingWindowTextListening ObserveDrcDrcPsdkFloatingWindowTextNorthbound failed, psdk_floating_window_text_listen_id_ %d", psdk_floating_window_text_listen_id_);
    }
}

void PsdkAbs::onReceivePsdkFloatingWindowText(const std::shared_ptr<message_engine::DrcDrcPsdkFloatingWindowTextNorthbound> &msg) {
    LOG_DEBUG("[PsdkCtrlMgr][PsdkAbs] onReceivePsdkFloatingWindowText: %s", msg->ToJsonString().c_str());

    PsdkFloatingWindowTextInfo floating_window_text_info{
        .psdk_index = msg->msg.psdk_index,
        .floating_window_text = msg->msg.floating_window_text
    };
    psdk_floating_window_text_info_callback_container_.notifyCallbackIfChanged(floating_window_text_info);
}

void PsdkAbs::DrcSpeakerAudioPlayStart(const PsdkSpeakerAudioPlayInfo &audio_play_info, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerAudioPlayStart, name: %s, md5: %s, format: %s, url: %s", audio_play_info.name.c_str(), audio_play_info.md5.c_str(), audio_play_info.format.c_str(), audio_play_info.url.c_str());

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerAudioPlayStartSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.file.name = audio_play_info.name;
    msg->msg.file.md5 = audio_play_info.md5;
    msg->msg.file.format = audio_play_info.format;
    msg->msg.file.url = audio_play_info.url;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerAudioPlayStartSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerAudioPlayStart SendDrcDrcSpeakerAudioPlayStartSouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerAudioPlayStart SendDrcDrcSpeakerAudioPlayStartSouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerTTSPlayStart(const PsdkSpeakerTTSPlayInfo& tts_play_info, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerTTSPlayStart, name: %s, text: %s, md5: %s", tts_play_info.name.c_str(), tts_play_info.text.c_str(), tts_play_info.md5.c_str());

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerTtsPlayStartSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.tts.name = tts_play_info.name;
    msg->msg.tts.text = tts_play_info.text;
    msg->msg.tts.md5 = tts_play_info.md5;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerTtsPlayStartSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerTTSPlayStart SendDrcDrcSpeakerTtsPlayStartSouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerTTSPlayStart SendDrcDrcSpeakerTtsPlayStartSouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerRealTimeStart(const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerRealTimeStart");

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerRealTimeStartSouthbound>();
    msg->msg.psdk_index = psdk_index_;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerRealTimeStartSouthbound(
        msg,
        [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)->void {
            WeakDummyReturn(weak_ptr);
            FlightHubError err;
            if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
                err = FlightHubError::from_drc(error_code);
                LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerRealTimeStart SendDrcDrcSpeakerRealTimeStartSouthbound error: %d", error_code);
            } else if (msg && msg->msg.result != 0) {
                err = FlightHubError::from_drc(0, msg->msg.result);
                LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerRealTimeStart SendDrcDrcSpeakerRealTimeStartSouthbound error from device: %d", msg->msg.result);
            }
            if (callback) {
                callback(err);
            }
        },
        nullptr
    );
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerRealTimeStop(const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerRealTimeStop");

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerRealTimeStopSouthbound>();
    msg->msg.psdk_index = psdk_index_;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerRealTimeStopSouthbound(
        msg,
        [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)->void {
            WeakDummyReturn(weak_ptr);
            FlightHubError err;
            if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
                err = FlightHubError::from_drc(error_code);
                LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerRealTimeStop SendDrcDrcSpeakerRealTimeStopSouthbound error: %d", error_code);
            } else if (msg && msg->msg.result != 0) {
                err = FlightHubError::from_drc(0, msg->msg.result);
                LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerRealTimeStop SendDrcDrcSpeakerRealTimeStopSouthbound error from device: %d", msg->msg.result);
            }
            if (callback) {
                callback(err);
            }
        },
        nullptr
    );
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerReplay(const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerReplay");

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerReplaySouthbound>();
    msg->msg.psdk_index = psdk_index_;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerReplaySouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerReplayNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerReplay SendDrcDrcSpeakerReplaySouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerReplay SendDrcDrcSpeakerReplaySouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerPlayStop(const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayStop");

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerPlayStopSouthbound>();
    msg->msg.psdk_index = psdk_index_;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerPlayStopSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerPlayStopNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayStop SendDrcDrcSpeakerPlayStopSouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayStop SendDrcDrcSpeakerPlayStopSouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerPlayVolumeSet(int volume, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayVolumeSet");

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerPlayVolumeSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.play_volume = volume;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerPlayVolumeSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayVolumeSet SendDrcDrcSpeakerPlayStopSouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayVolumeSet SendDrcDrcSpeakerPlayStopSouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerPlayModeSet(int play_mode, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayModeSet: %d", play_mode);

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerPlayModeSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.play_mode = play_mode;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerPlayModeSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayModeSet SendDrcDrcSpeakerPlayModeSetSouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerPlayModeSet SendDrcDrcSpeakerPlayModeSetSouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSpeakerTTSSet(PsdkSpeakerTTSconfig tts_config, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerTTSSet: volume: %d, type: %d, language: %d, speed: %d", tts_config.volume, tts_config.type, tts_config.language, tts_config.speed);

    auto msg = std::make_shared<message_engine::DrcDrcSpeakerTtsSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.volume = tts_config.volume;
    msg->msg.type = tts_config.type;
    msg->msg.language = tts_config.language;
    msg->msg.speed = tts_config.speed;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSpeakerTtsSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSpeakerTtsSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerTTSSet SendDrcDrcSpeakerTtsSetSouthbound error: %d", error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs] DrcSpeakerTTSSet SendDrcDrcSpeakerTtsSetSouthbound error from device: %d", msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}


void PsdkAbs::DrcWidgetValueSet(int index, int value, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcWidgetValueSet: index: %d, value: %d", psdk_index_, index, value);

    auto msg = std::make_shared<message_engine::DrcDrcPsdkWidgetValueSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.index = index;
    msg->msg.value = value;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcPsdkWidgetValueSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcWidgetValueSet SendDrcDrcPsdkWidgetValueSetSouthbound error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcWidgetValueSet SendDrcDrcPsdkWidgetValueSetSouthbound error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcWidgetInputBoxTextSet(const std::string& value, const std::function<void(FlightHubError err)> &callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcWidgetValueSet: value: %s", psdk_index_, value.c_str());

    auto msg = std::make_shared<message_engine::DrcDrcPsdkInputBoxTextSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.value = value;
    msg->send_interval_millisecond = 10000; // 10s超时，设备需要 9s 超时再回包，一定要等到回包，才知道消息是否送到且执行完毕

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcPsdkInputBoxTextSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcWidgetValueSet SendDrcDrcPsdkWidgetValueSetSouthbound error: %d", psdk_index_, error_code);
        }
        // 设备永远返回超时错误，不需要 care
        // else if (msg && msg->msg.result != 0) {
        //     err = FlightHubError::from_drc(0, msg->msg.result);
        //     LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcWidgetValueSet SendDrcDrcPsdkWidgetValueSetSouthbound error from device: %d", psdk_index_, msg->msg.result);
        // }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightModeSet(int mode, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightModeSet: mode: %d", psdk_index_, mode);

    auto msg = std::make_shared<message_engine::DrcDrcLightModeSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.group = 0;
    msg->msg.mode = mode;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightModeSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightModeSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightModeSet error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightModeSet error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightBrightnessSet(int brightness, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightBrightnessSet: brightness: %d", psdk_index_, brightness);

    auto msg = std::make_shared<message_engine::DrcDrcLightBrightnessSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.group = 0;
    msg->msg.brightness = brightness;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightBrightnessSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightBrightnessSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightBrightnessSet error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightBrightnessSet error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightCalibration(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightCalibration", psdk_index_);

    auto msg = std::make_shared<message_engine::DrcDrcLightCalibrationSouthbound>();
    msg->msg.psdk_index = psdk_index_;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightCalibrationSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightCalibrationNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightCalibration error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightCalibration error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightFineTuningSet(int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightFineTuningSet: position: %d, value: %f, saved: %d", psdk_index_, position, value, saved);

    auto msg = std::make_shared<message_engine::DrcDrcLightFineTuningSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.position = position;
    msg->msg.value = value;
    msg->msg.saved = saved;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightFineTuningSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightFineTuningSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightFineTuningSet error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightFineTuningSet error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightWideFieldModeSet(int wide_field_mode, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightWideFieldModeSet: wide_field_mode: %d", psdk_index_, wide_field_mode);

    auto msg = std::make_shared<message_engine::DrcDrcLightWideFieldModeSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.group = 0;
    msg->msg.wide_field_mode = wide_field_mode;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightWideFieldModeSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightWideFieldModeSet error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightWideFieldModeSet error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightLightGimbalControlSet(int light_gimbal_control, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightLightGimbalControlSet: light_gimbal_control: %d", psdk_index_, light_gimbal_control);

    auto msg = std::make_shared<message_engine::DrcDrcLightLightGimbalControlSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.group = 0;
    msg->msg.light_gimbal_control = light_gimbal_control;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightLightGimbalControlSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightLightGimbalControlSet error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightLightGimbalControlSet error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

void PsdkAbs::DrcSearchlightTypeSet(int type, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightTypeSet: type: %d", psdk_index_, type);

    auto msg = std::make_shared<message_engine::DrcDrcLightTypeSetSouthbound>();
    msg->msg.psdk_index = psdk_index_;
    msg->msg.group = 0;
    msg->msg.type = type;

    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLightTypeSetSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLightTypeSetNorthbound>& msg)->void {
        WeakDummyReturn(weak_ptr);
        FlightHubError err;
        if (error_code != message_engine::MeErrorCode::ME_NO_ERROR) {
            err = FlightHubError::from_drc(error_code);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightTypeSet error: %d", psdk_index_, error_code);
        } else if (msg && msg->msg.result != 0) {
            err = FlightHubError::from_drc(0, msg->msg.result);
            LOG_ERROR("[PsdkCtrlMgr][PsdkAbs][%d] DrcSearchlightTypeSet error from device: %d", psdk_index_, msg->msg.result);
        }
        if (callback) {
            callback(err);
        }
    }, nullptr);
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}
