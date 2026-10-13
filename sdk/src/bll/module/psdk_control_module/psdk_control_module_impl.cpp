// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "psdk_control_module_impl.h"

#include <BusinessManager.h>
#include <base/common/logger.h>
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

PsdkControlModuleImpl::PsdkControlModuleImpl(std::shared_ptr<PsdkCtrlMgr> psdk_ctrl_mgr)
    : psdk_ctrl_mgr_(std::move(psdk_ctrl_mgr)) {
    LOG_INFO("[PsdkControlModuleImpl] constructor");
}

int PsdkControlModuleImpl::listenDrcPsdkDeviceList(const std::function<void(const PsdkDeviceList& psdk_device_list)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    return psdk_ctrl_mgr_->listenDrcPsdkDeviceList(callback);
}


void PsdkControlModuleImpl::cancelListenDrcPsdkDeviceList(int listen_id) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return;
    }
    psdk_ctrl_mgr_->cancelListenDrcPsdkDeviceList(listen_id);
}


int PsdkControlModuleImpl::listenDrcPsdkStateSummaryInfo(int psdk_index, const std::function<void(PsdkStateSummaryInfo psdk_device_list)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    return psdk_ctrl_mgr_->listenDrcPsdkStateSummaryInfo(psdk_index, callback);
}


void PsdkControlModuleImpl::cancelListenDrcPsdkStateSummaryInfo(int psdk_index, int listen_id) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return;
    }
    psdk_ctrl_mgr_->cancelListenDrcPsdkStateSummaryInfo(psdk_index, listen_id);
}


int PsdkControlModuleImpl::listenDrcPsdkStateSpeakerInfo(int psdk_index, const std::function<void(PsdkStateSpeakerInfo speaker_info)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    return psdk_ctrl_mgr_->listenDrcPsdkStateSpeakerInfo(psdk_index, callback);
}


void PsdkControlModuleImpl::cancelListenDrcPsdkStateSpeakerInfo(int psdk_index, int listen_id) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return;
    }
    psdk_ctrl_mgr_->cancelListenDrcPsdkStateSpeakerInfo(psdk_index, listen_id);
}


int PsdkControlModuleImpl::listenDrcPsdkStateSearchlightInfo(int psdk_index, const std::function<void(PsdkStateSearchlightInfo searchlight_info)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    return psdk_ctrl_mgr_->listenDrcPsdkStateSearchlightInfo(psdk_index, callback);
}


void PsdkControlModuleImpl::cancelListenDrcPsdkStateSearchlightInfo(int psdk_index, int listen_id) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return;
    }
    psdk_ctrl_mgr_->cancelListenDrcPsdkStateSearchlightInfo(psdk_index, listen_id);
}


void PsdkControlModuleImpl::DrcSearchlightModeSet(int psdk_index, int mode, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置探照灯模式(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_MODE_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightModeSet(psdk_index, mode, callback);
}


void PsdkControlModuleImpl::DrcSearchlightBrightnessSet(int psdk_index, int brightness, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置探照灯亮度(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_BRIGHTNESS_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightBrightnessSet(psdk_index, brightness, callback);
}


void PsdkControlModuleImpl::DrcSearchlightCalibration(int psdk_index, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 探照灯校准(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_CALIBRATION, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightCalibration(psdk_index, callback);
}


void PsdkControlModuleImpl::DrcSearchlightFineTuningSet(int psdk_index, int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 探照灯微调设置(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_FINE_TUNING_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightFineTuningSet(psdk_index, position, value, saved, callback);
}


void PsdkControlModuleImpl::DrcSearchlightWideFieldModeSet(int psdk_index, int wide_field_mode, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置探照灯广域模式(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_WIDE_FIELD_MODE_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightWideFieldModeSet(psdk_index, wide_field_mode, callback);
}


void PsdkControlModuleImpl::DrcSearchlightLightGimbalControlSet(int psdk_index, int light_gimbal_control, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置探照灯云台控制(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_LIGHT_GIMBAL_CONTROL_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightLightGimbalControlSet(psdk_index, light_gimbal_control, callback);
}


void PsdkControlModuleImpl::DrcSearchlightTypeSet(int psdk_index, int type, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置探照灯类型(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_TYPE_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSearchlightTypeSet(psdk_index, type, callback);
}


int PsdkControlModuleImpl::listenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    return psdk_ctrl_mgr_->listenDrcPsdkSpeakerPlayProgressInfo(psdk_index, callback);
}


void PsdkControlModuleImpl::cancelListenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, int listen_id) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return;
    }
    psdk_ctrl_mgr_->cancelListenDrcPsdkSpeakerPlayProgressInfo(psdk_index, listen_id);
}


int PsdkControlModuleImpl::listenDrcPsdkFloatingWindowTextInfo(int psdk_index, const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    return psdk_ctrl_mgr_->listenDrcPsdkFloatingWindowTextInfo(psdk_index, callback);
}


void PsdkControlModuleImpl::cancelListenDrcPsdkFloatingWindowTextInfo(int psdk_index, int listen_id) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return;
    }
    psdk_ctrl_mgr_->cancelListenDrcPsdkFloatingWindowTextInfo(psdk_index, listen_id);
}


void PsdkControlModuleImpl::DrcSpeakerAudioPlayStart(int psdk_index, const PsdkSpeakerAudioPlayInfo& audio_play_info, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 开始音频喊话(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_AUDIO_PLAY_START, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerAudioPlayStart(psdk_index, audio_play_info, callback);
}


void PsdkControlModuleImpl::DrcSpeakerTTSPlayStart(int psdk_index, const PsdkSpeakerTTSPlayInfo& tts_play_info, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 开始 TTS 喊话(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerTTSPlayStart(psdk_index, tts_play_info, callback);
}


void PsdkControlModuleImpl::DrcSpeakerRealTimeStart(int psdk_index, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 开始实时喊话(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_START, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerRealTimeStart(psdk_index, callback);
}


void PsdkControlModuleImpl::DrcSpeakerRealTimeStop(int psdk_index, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 停止实时喊话(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_STOP, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerRealTimeStop(psdk_index, callback);
}


void PsdkControlModuleImpl::DrcSpeakerReplay(int psdk_index, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 重播喊话(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REPLAY, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerReplay(psdk_index, callback);
}


void PsdkControlModuleImpl::DrcSpeakerPlayStop(int psdk_index, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 停止喊话播放(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_STOP, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerPlayStop(psdk_index, callback);
}


void PsdkControlModuleImpl::DrcSpeakerPlayVolumeSet(int psdk_index, int volume, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置喊话器播放音量(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_VOLUME_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerPlayVolumeSet(psdk_index, volume, callback);
}


void PsdkControlModuleImpl::DrcSpeakerPlayModeSet(int psdk_index, int play_mode, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置喊话器播放模式(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_MODE_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerPlayModeSet(psdk_index, play_mode, callback);
}


void PsdkControlModuleImpl::DrcSpeakerTTSSet(int psdk_index, const PsdkSpeakerTTSconfig& tts_config, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置 TTS 配置(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcSpeakerTTSSet(psdk_index, tts_config, callback);
}


void PsdkControlModuleImpl::DrcWidgetValueSet(int psdk_index, int index, int value, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置 Widget 值(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_VALUE_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcWidgetValueSet(psdk_index, index, value, callback);
}


void PsdkControlModuleImpl::DrcWidgetInputBoxTextSet(int psdk_index, const std::string& value, const std::function<void(FlightHubError err)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        // 埋点: 设置 Widget 输入框文本(PSDK 控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_INPUT_BOX_TEXT_SET, state_err, "[PsdkControlModuleImpl]");
        callback(state_err);
        return;
    }
    psdk_ctrl_mgr_->DrcWidgetInputBoxTextSet(psdk_index, value, callback);
}


int PsdkControlModuleImpl::listenPsdkSpeakerAudioPlayStartProgress(const std::function<void(const PsdkSpeakerPlayStartProgress& msg)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    WeakDummy(weak_ptr);
    int user_listen_id = psdk_audio_play_progress_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenPsdkSpeakerAudioPlayProgress(
        [this, weak_ptr, callback](const notification_center::PsdkSpeakerPlayProgress& msg) -> void {
            WeakDummyReturn(weak_ptr);
            PsdkSpeakerPlayStartProgress res{};
            res.gateway = msg.gateway;
            res.sn = msg.sn;
            res.result = msg.result;
            res.output.md5 = msg.output.md5;
            res.output.psdk_index = msg.output.psdk_index;
            res.output.status = msg.output.status;
            res.output.progress.percent = msg.output.progress.percent;
            res.output.progress.step_key = msg.output.progress.step_key;

            LOG_INFO("[PsdkControlModuleImpl] ListenPsdkSpeakerAudioPlayProgress: sn: %s, psdk_index: %d, percent: %d, status: %s",
                msg.sn.c_str(), msg.output.psdk_index, msg.output.progress.percent, msg.output.status.c_str());

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[PsdkControlModuleImpl] ListenPsdkSpeakerAudioPlayProgress error: %d", error_code);
            }
        }, psdk_ctrl_mgr_->getDroneSn(), notification_center::NotificationFrequency_Any);

    psdk_audio_play_progress_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[PsdkControlModuleImpl] listenPsdkSpeakerAudioPlayStartProgress user_listen_id: %d, nc_listen_id: %lld",
        user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void PsdkControlModuleImpl::cancelListenPsdkSpeakerAudioPlayStartProgress(int listen_id) {
    LOG_INFO("[PsdkControlModuleImpl] cancelListenPsdkSpeakerAudioPlayStartProgress listen_id: %d", listen_id);
    auto it = psdk_audio_play_progress_nc_listen_ids_.find(listen_id);
    if (it != psdk_audio_play_progress_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        psdk_audio_play_progress_nc_listen_ids_.erase(it);
    }
}

int PsdkControlModuleImpl::listenPsdkSpeakerTtsPlayStartProgress(const std::function<void(const PsdkSpeakerPlayStartProgress& msg)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    WeakDummy(weak_ptr);
    int user_listen_id = psdk_tts_play_progress_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenPsdkSpeakerTtsPlayProgress(
        [this, weak_ptr, callback](const notification_center::PsdkSpeakerPlayProgress& msg) -> void {
            WeakDummyReturn(weak_ptr);
            PsdkSpeakerPlayStartProgress res{};
            res.gateway = msg.gateway;
            res.sn = msg.sn;
            res.result = msg.result;
            res.output.md5 = msg.output.md5;
            res.output.psdk_index = msg.output.psdk_index;
            res.output.status = msg.output.status;
            res.output.progress.percent = msg.output.progress.percent;
            res.output.progress.step_key = msg.output.progress.step_key;

            LOG_INFO("[PsdkControlModuleImpl] ListenPsdkSpeakerTtsPlayProgress: sn: %s, psdk_index: %d, percent: %d, status: %s",
                msg.sn.c_str(), msg.output.psdk_index, msg.output.progress.percent, msg.output.status.c_str());

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[PsdkControlModuleImpl] ListenPsdkSpeakerTtsPlayProgress error: %d", error_code);
            }
        }, psdk_ctrl_mgr_->getDroneSn(), notification_center::NotificationFrequency_Any);

    psdk_tts_play_progress_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[PsdkControlModuleImpl] listenPsdkSpeakerTtsPlayStartProgress user_listen_id: %d, nc_listen_id: %lld",
        user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void PsdkControlModuleImpl::cancelListenPsdkSpeakerTtsPlayStartProgress(int listen_id) {
    LOG_INFO("[PsdkControlModuleImpl] cancelListenPsdkSpeakerTtsPlayStartProgress listen_id: %d", listen_id);
    auto it = psdk_tts_play_progress_nc_listen_ids_.find(listen_id);
    if (it != psdk_tts_play_progress_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        psdk_tts_play_progress_nc_listen_ids_.erase(it);
    }
}

int PsdkControlModuleImpl::listenDronePsdkUiResource(const std::function<void(const DronePsdkUiResource& msg)>& callback) {
    if (!psdk_ctrl_mgr_) {
        LOG_ERROR("PsdkControlModuleImpl: psdk_ctrl_mgr_ is not initialized");
        return -1;
    }
    WeakDummy(weak_ptr);
    int user_listen_id = psdk_ui_resource_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDronePsdkUiResource(
        [this, weak_ptr, callback](const notification_center::DronePsdkUiResource& msg) -> void {
            WeakDummyReturn(weak_ptr);
            DronePsdkUiResource res{};
            for (const auto& it : msg.psdk_ui_resource) {
                PsdkUiResourceItem item{};
                item.psdk_index = it.psdk_index;
                item.psdk_ready = it.psdk_ready;
                item.object_key = it.object_key;
                res.psdk_ui_resource.emplace_back(item);
            }

            LOG_INFO("[PsdkControlModuleImpl] ListenDronePsdkUiResource: psdk_ui_resource size: %d",
                (int)res.psdk_ui_resource.size());

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[PsdkControlModuleImpl] ListenDronePsdkUiResource error: %d", error_code);
            }
        }, psdk_ctrl_mgr_->getDroneSn(), notification_center::NotificationFrequency_OnChanged);

    psdk_ui_resource_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[PsdkControlModuleImpl] listenDronePsdkUiResource user_listen_id: %d, nc_listen_id: %lld",
        user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void PsdkControlModuleImpl::cancelListenDronePsdkUiResource(int listen_id) {
    LOG_INFO("[PsdkControlModuleImpl] cancelListenDronePsdkUiResource listen_id: %d", listen_id);
    auto it = psdk_ui_resource_nc_listen_ids_.find(listen_id);
    if (it != psdk_ui_resource_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        psdk_ui_resource_nc_listen_ids_.erase(it);
    }
}


