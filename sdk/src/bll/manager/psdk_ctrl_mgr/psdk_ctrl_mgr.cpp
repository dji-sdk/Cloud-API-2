// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "psdk_ctrl_mgr.h"

#include <utility>

#include "base/common/logger.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

PsdkCtrlMgr::PsdkCtrlMgr(std::string drone_sn, std::string gateway_sn, const std::shared_ptr<message_engine::IMessageEngine> &message_engine) : message_engine_(message_engine), drone_sn_(std::move(drone_sn)), gateway_sn_(std::move(gateway_sn)) {
    LOG_INFO("[PsdkCtrlMgr][%s] ctor", drone_sn_.c_str());
    initPsdkUiResourceListening();
}

PsdkCtrlMgr::~PsdkCtrlMgr() {
    LOG_INFO("[PsdkCtrlMgr][%s] dtor", drone_sn_.c_str());

    // 1. 先停止消息监听
    if (message_engine_ && message_engine_->getCloudMessageManager()) {
        if (psdk_ui_resource_listen_id_ >= 0) {
            message_engine_->getCloudMessageManager()->CancelObserveMessage(psdk_ui_resource_listen_id_);
            psdk_ui_resource_listen_id_ = -1;
        }
    }

    // 2. 清理所有PsdkAbs对象
    psdk_abs_map_.clear();
    psdk_abs_info_map_.clear();

    // 3. 回调容器会在析构函数中自动清理，无需手动调用clear
}

int PsdkCtrlMgr::listenDrcPsdkDeviceList(const std::function<void(PsdkDeviceList)> &callback) {
    auto listen_id = psdk_device_list_callback_container_.addCallback(callback);
    // callback(psdk_device_list_msg_); // 已经有 cached 的数据，CallbackContainer 内部直接回调
    LOG_INFO("[PsdkCtrlMgr][%s] listenDrcPsdkDeviceList listen_id: %d", drone_sn_.c_str(), listen_id);
    return listen_id;
}

void PsdkCtrlMgr::cancelListenDrcPsdkDeviceList(int listen_id) {
    LOG_INFO("[PsdkCtrlMgr][%s] cancelListenDrcPsdkDeviceList listen_id: %d", drone_sn_.c_str(), listen_id);
    psdk_device_list_callback_container_.removeCallback(listen_id);
}

void PsdkCtrlMgr::initPsdkUiResourceListening() {
    WeakDummy(weak_ptr);
    psdk_ui_resource_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcPsdkUiResourceNorthbound([this, weak_ptr](const std::shared_ptr<message_engine::DrcDrcPsdkUiResourceNorthbound>& msg)->void{
        WeakDummyReturn(weak_ptr);
        onReceivePsdkUiResource(msg);
    }, nullptr);
    if (psdk_ui_resource_listen_id_ < 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] initPsdkUiResourceListening ObserveDrcDrcPsdkUiResourceNorthbound failed, psdk_ui_resource_listen_id_ %d", drone_sn_.c_str(), psdk_ui_resource_listen_id_);
    }
}

void PsdkCtrlMgr::onReceivePsdkUiResource(const std::shared_ptr<message_engine::DrcDrcPsdkUiResourceNorthbound> &msg) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::Main()->Post([this, weak_ptr, msg]() {
        WeakDummyReturn(weak_ptr);
        LOG_DEBUG("[PsdkCtrlMgr][%s] onReceivePsdkUiResource, psdk_index: %d, psdk_ready: %d, object_key: %s", drone_sn_.c_str(), msg->msg.psdk_index, msg->msg.psdk_ready, msg->msg.object_key.c_str());
        if (psdk_abs_info_map_.count(msg->msg.psdk_index) == 0 && msg->msg.psdk_ready == true) {
            // 新设备接入
            LOG_INFO("[PsdkCtrlMgr][%s] onReceivePsdkUiResource, new ready coming psdk_index: %d, psdk_ready: %d, object_key: %s (empty: %d)", drone_sn_.c_str(), msg->msg.psdk_index, msg->msg.psdk_ready, msg->msg.object_key.c_str(), msg->msg.object_key.empty());
            psdk_abs_map_[msg->msg.psdk_index] = std::make_shared<PsdkAbs>(msg->msg.psdk_index, drone_sn_, gateway_sn_, message_engine_);
            psdk_abs_info_map_[msg->msg.psdk_index] = PsdkDeviceInfo{
                .psdk_index = msg->msg.psdk_index,
                .object_key = msg->msg.object_key
            };
        }

        if (psdk_abs_info_map_.count(msg->msg.psdk_index) > 0 && msg->msg.psdk_ready == false) {
            // 设备断开
            psdk_abs_map_.erase(msg->msg.psdk_index);
            psdk_abs_info_map_.erase(msg->msg.psdk_index);
        }

        psdk_device_list_msg_.psdk_device_list.clear();
        for (const auto& [psdk_index, conn_state]: psdk_abs_info_map_) {
            psdk_device_list_msg_.psdk_device_list.emplace_back(conn_state);
        }
        psdk_device_list_callback_container_.notifyCallbackIfChanged(psdk_device_list_msg_);
    });
}

int PsdkCtrlMgr::listenDrcPsdkStateSummaryInfo(int psdk_index, const std::function<void(PsdkStateSummaryInfo psdk_device_list)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] listenDrcPsdkStateSummaryInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return -1;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    return psdk_abs->listenDrcPsdkStateSummaryInfo(callback);
}

void PsdkCtrlMgr::cancelListenDrcPsdkStateSummaryInfo(int psdk_index, int listen_id) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] cancelListenDrcPsdkStateSummaryInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->cancelListenDrcPsdkStateSummaryInfo(listen_id);
}

int PsdkCtrlMgr::listenDrcPsdkStateSpeakerInfo(int psdk_index, const std::function<void(PsdkStateSpeakerInfo speaker_info)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] listenDrcPsdkStateSpeakerInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return -1;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    return psdk_abs->listenDrcPsdkStateSpeakerInfo(callback);
}

void PsdkCtrlMgr::cancelListenDrcPsdkStateSpeakerInfo(int psdk_index, int listen_id) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] cancelListenDrcPsdkStateSpeakerInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->cancelListenDrcPsdkStateSpeakerInfo(listen_id);
}

int PsdkCtrlMgr::listenDrcPsdkStateSearchlightInfo(int psdk_index, const std::function<void(PsdkStateSearchlightInfo searchlight_info)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] listenDrcPsdkStateSearchlightInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return -1;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    return psdk_abs->listenDrcPsdkStateSearchlightInfo(callback);
}

void PsdkCtrlMgr::cancelListenDrcPsdkStateSearchlightInfo(int psdk_index, int listen_id) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] cancelListenDrcPsdkStateSearchlightInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->cancelListenDrcPsdkStateSearchlightInfo(listen_id);
}

int PsdkCtrlMgr::listenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] listenDrcPsdkSpeakerPlayProgressInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return -1;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    return psdk_abs->listenDrcPsdkSpeakerPlayProgressInfo(callback);
}

void PsdkCtrlMgr::cancelListenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, int listen_id) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] cancelListenDrcPsdkSpeakerPlayProgressInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->cancelListenDrcPsdkSpeakerPlayProgressInfo(listen_id);
}

int PsdkCtrlMgr::listenDrcPsdkFloatingWindowTextInfo(int psdk_index, const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] listenDrcPsdkFloatingWindowTextInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return -1;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    return psdk_abs->listenDrcPsdkFloatingWindowTextInfo(callback);
}

void PsdkCtrlMgr::cancelListenDrcPsdkFloatingWindowTextInfo(int psdk_index, int listen_id) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] cancelListenDrcPsdkFloatingWindowTextInfo unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->cancelListenDrcPsdkFloatingWindowTextInfo(listen_id);
}

void PsdkCtrlMgr::DrcSpeakerAudioPlayStart(int psdk_index, const PsdkSpeakerAudioPlayInfo &audio_play_info, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerAudioPlayStart unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 开始音频喊话(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_AUDIO_PLAY_START, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerAudioPlayStart(audio_play_info, [callback](FlightHubError err) {
        // 埋点: 开始音频喊话; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_AUDIO_PLAY_START, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerTTSPlayStart(int psdk_index, const PsdkSpeakerTTSPlayInfo &tts_play_info, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerTTSPlayStart unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 开始 TTS 喊话(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    if (tts_play_info.name.empty()) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerTTSPlayStart name empty", drone_sn_.c_str());
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 开始 TTS 喊话(TTS 名称为空); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    if (tts_play_info.text.empty()) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerTTSPlayStart text empty", drone_sn_.c_str());
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 开始 TTS 喊话(TTS 文本为空); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    if (tts_play_info.md5.empty()) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerTTSPlayStart md5 empty", drone_sn_.c_str());
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 开始 TTS 喊话(TTS 文件 md5 为空); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }

        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerTTSPlayStart(tts_play_info, [callback](FlightHubError err) {
        // 埋点: 开始 TTS 喊话; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerRealTimeStart(int psdk_index, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerRealTimeStart unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 开始实时喊话(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_START, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerRealTimeStart([callback](FlightHubError err) {
        // 埋点: 开始实时喊话; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_START, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerRealTimeStop(int psdk_index, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerRealTimeStop unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 停止实时喊话(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_STOP, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerRealTimeStop([callback](FlightHubError err) {
        // 埋点: 停止实时喊话; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_STOP, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerReplay(int psdk_index, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerReplay unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 重播喊话(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REPLAY, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerReplay([callback](FlightHubError err) {
        // 埋点: 重播喊话; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REPLAY, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerPlayStop(int psdk_index, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerPlayStop unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 停止喊话播放(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_STOP, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerPlayStop([callback](FlightHubError err) {
        // 埋点: 停止喊话播放; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_STOP, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerPlayVolumeSet(int psdk_index, int volume, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerPlayVolumeSet unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置喊话器播放音量(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_VOLUME_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    if (volume < 0 || volume > 100) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerPlayVolumeSet invalid volume: %d", drone_sn_.c_str(), volume);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置喊话器播放音量(音量超出 0~100 范围); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_VOLUME_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerPlayVolumeSet(volume, [callback](FlightHubError err) {
        // 埋点: 设置喊话器播放音量; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_VOLUME_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerPlayModeSet(int psdk_index, int play_mode, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerPlayModeSet unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置喊话器播放模式(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_MODE_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    const int single = static_cast<int>(MegaphonePlayMode::SINGLE_PLAY);
    const int loop = static_cast<int>(MegaphonePlayMode::LOOP_PLAYBACK);
    if (play_mode != single && play_mode != loop) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerPlayModeSet invalid play_mode: %d", drone_sn_.c_str(), play_mode);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置喊话器播放模式(播放模式非法, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_MODE_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerPlayModeSet(play_mode, [callback](FlightHubError err) {
        // 埋点: 设置喊话器播放模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_MODE_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSpeakerTTSSet(int psdk_index, PsdkSpeakerTTSconfig tts_config, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcSpeakerTTSSet unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置 TTS 配置(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcSpeakerTTSSet(tts_config, [callback](FlightHubError err) {
        // 埋点: 设置 TTS 配置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcWidgetValueSet(int psdk_index, int index, int value, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcWidgetValueSet unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置 Widget 值(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_VALUE_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcWidgetValueSet(index, value, [callback](FlightHubError err) {
        // 埋点: 设置 Widget 值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_VALUE_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcWidgetInputBoxTextSet(int psdk_index, const std::string &value, const std::function<void(FlightHubError err)> &callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr][%s] DrcWidgetInputBoxTextSet unknown psdk_index: %d", drone_sn_.c_str(), psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置 Widget 输入框文本(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_INPUT_BOX_TEXT_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    auto psdk_abs = psdk_abs_map_[psdk_index];
    psdk_abs->DrcWidgetInputBoxTextSet(value, [callback](FlightHubError err) {
        // 埋点: 设置 Widget 输入框文本; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_INPUT_BOX_TEXT_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}


void PsdkCtrlMgr::DrcSearchlightModeSet(int psdk_index, int mode, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightModeSet unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置探照灯模式(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_MODE_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightModeSet(mode, [callback](FlightHubError err) {
        // 埋点: 设置探照灯模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_MODE_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSearchlightBrightnessSet(int psdk_index, int brightness, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightBrightnessSet unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置探照灯亮度(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_BRIGHTNESS_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightBrightnessSet(brightness, [callback](FlightHubError err) {
        // 埋点: 设置探照灯亮度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_BRIGHTNESS_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSearchlightCalibration(int psdk_index, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightCalibration unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 探照灯校准(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_CALIBRATION, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightCalibration([callback](FlightHubError err) {
        // 埋点: 探照灯校准; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_CALIBRATION, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSearchlightFineTuningSet(int psdk_index, int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightFineTuningSet unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 探照灯微调设置(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_FINE_TUNING_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightFineTuningSet(position, value, saved, [callback](FlightHubError err) {
        // 埋点: 探照灯微调设置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_FINE_TUNING_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSearchlightWideFieldModeSet(int psdk_index, int wide_field_mode, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightWideFieldModeSet unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置探照灯广域模式(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_WIDE_FIELD_MODE_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightWideFieldModeSet(wide_field_mode, [callback](FlightHubError err) {
        // 埋点: 设置探照灯广域模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_WIDE_FIELD_MODE_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSearchlightLightGimbalControlSet(int psdk_index, int light_gimbal_control, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightLightGimbalControlSet unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置探照灯云台控制(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_LIGHT_GIMBAL_CONTROL_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightLightGimbalControlSet(light_gimbal_control, [callback](FlightHubError err) {
        // 埋点: 设置探照灯云台控制; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_LIGHT_GIMBAL_CONTROL_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}

void PsdkCtrlMgr::DrcSearchlightTypeSet(int psdk_index, int type, const std::function<void(FlightHubError err)>& callback) {
    if (psdk_abs_map_.count(psdk_index) == 0) {
        LOG_ERROR("[PsdkCtrlMgr] DrcSearchlightTypeSet unknown psdk_index: %d", psdk_index);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 设置探照灯类型(psdk_index 无效, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_TYPE_SET, param_err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(param_err);
        }
        return;
    }
    psdk_abs_map_[psdk_index]->DrcSearchlightTypeSet(type, [callback](FlightHubError err) {
        // 埋点: 设置探照灯类型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_TYPE_SET, err, "[PsdkCtrlMgr]");
        if (callback) {
            callback(err);
        }
    });
}
