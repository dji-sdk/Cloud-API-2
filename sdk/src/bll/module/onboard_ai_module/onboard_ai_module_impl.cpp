// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "onboard_ai_module_impl.h"

#include <base/common/logger.h>
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

OnboardAiModuleImpl::OnboardAiModuleImpl(std::shared_ptr<AICtrlMgr> ai_ctrl_mgr)
    : ai_ctrl_mgr_(std::move(ai_ctrl_mgr)) {
    LOG_INFO("[OnboardAiModuleImpl] constructor");
}

int OnboardAiModuleImpl::listenAIDataInfo(const std::function<void(const AiDataInfo& state)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] listenAIDataInfo: ai_ctrl_mgr_ is null");
        return -1;
    }
    return ai_ctrl_mgr_->listenAIDataInfo(callback);
}

void OnboardAiModuleImpl::cancelListenAIDataInfo(int listen_id) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] cancelListenAIDataInfo: ai_ctrl_mgr_ is null");
        return;
    }
    ai_ctrl_mgr_->cancelListenAIDataInfo(listen_id);
}

void OnboardAiModuleImpl::AIIdentifySet(const AiIdentifySetParam& param, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] AIIdentifySet: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置 AI 识别开关(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SET, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->AIIdentifySet(param, callback);
}

void OnboardAiModuleImpl::AIIdentifyFilterSet(const AiIdentifyFilterSetParam& param, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] AIIdentifyFilterSet: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置 AI 识别过滤(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_FILTER_SET, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->AIIdentifyFilterSet(param, callback);
}

void OnboardAiModuleImpl::AIIdentifyScoreSet(const AiIdentifyScoreSetParam& param, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] AIIdentifyScoreSet: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置 AI 识别分数阈值(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_SET, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->AIIdentifyScoreSet(param, callback);
}

void OnboardAiModuleImpl::AIIdentifyScoreModeSet(const AiIdentifyScoreModeSetParam& param, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] AIIdentifyScoreModeSet: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置 AI 识别分数模式(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_MODE_SET, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->AIIdentifyScoreModeSet(param, callback);
}

void OnboardAiModuleImpl::AIIdentifyScoreReset(const AiIdentifyScoreResetParam& param, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] AIIdentifyScoreReset: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 重置 AI 识别分数(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_RESET, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->AIIdentifyScoreReset(param, callback);
}

void OnboardAiModuleImpl::AIModelSelect(const AiModelSelectParam& param, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] AIModelSelect: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 选择 AI 模型(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_MODEL_SELECT, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->AIModelSelect(param, callback);
}

// ── AI Spotlight Zoom (Tracking) DRC ──

void OnboardAiModuleImpl::DrcAISpotlightZoomSet(int on, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAISpotlightZoomSet: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: AI 跟随开关(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SET, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAISpotlightZoomSet(on, callback);
}

void OnboardAiModuleImpl::DrcAISpotlightZoomSelect(const AiSpotlightZoomSelectParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAISpotlightZoomSelect: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: AI 框选目标跟随(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SELECT, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAISpotlightZoomSelect(params, callback);
}

void OnboardAiModuleImpl::DrcAISpotlightZoomConfirm(const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAISpotlightZoomConfirm: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: AI 框选目标跟随确认(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_CONFIRM, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAISpotlightZoomConfirm(callback);
}

void OnboardAiModuleImpl::DrcAISpotlightZoomStop(const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAISpotlightZoomStop: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止 AI 目标跟随(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_STOP, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAISpotlightZoomStop(callback);
}

void OnboardAiModuleImpl::DrcAISpotlightZoomTrack(int target_index, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAISpotlightZoomTrack: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: AI 识别目标跟随(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_TRACK, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAISpotlightZoomTrack(target_index, callback);
}

// ── AI Alarm DRC ──

void OnboardAiModuleImpl::DrcAIAlarmStart(const AiAlarmStartParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAIAlarmStart: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 开启 AI 主动告警(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_START, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAIAlarmStart(params, callback);
}

void OnboardAiModuleImpl::DrcAIAlarmStop(const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAIAlarmStop: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 关闭 AI 主动告警(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_STOP, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAIAlarmStop(callback);
}

void OnboardAiModuleImpl::DrcAIModelsConfig(const AiModelsConfigParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] DrcAIModelsConfig: ai_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 配置 AI 模型(机载 AI 管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_MODELS_CONFIG, state_err, "[OnboardAiModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    ai_ctrl_mgr_->DrcAIModelsConfig(params, callback);
}

int OnboardAiModuleImpl::listenDrcAIAlarmState(const std::function<void(const AiAlarmStateInfo&)>& callback) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] listenDrcAIAlarmState: ai_ctrl_mgr_ is null");
        return -1;
    }
    return ai_ctrl_mgr_->listenDrcAIAlarmState(callback);
}

void OnboardAiModuleImpl::cancelListenDrcAIAlarmState(int listen_id) {
    if (!ai_ctrl_mgr_) {
        LOG_ERROR("[OnboardAiModuleImpl] cancelListenDrcAIAlarmState: ai_ctrl_mgr_ is null");
        return;
    }
    ai_ctrl_mgr_->cancelListenDrcAIAlarmState(listen_id);
}
