// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ai_ctrl_mgr.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/request_message.h"
#include "http_interface/http_interface.h"
#include "api/flight_hub_sdk.h"
#include "notification_center/notification_center.h"

namespace flight_hub_sdk {

AICtrlMgr::AICtrlMgr(const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine)
    : drone_sn_(drone_sn), gateway_sn_(gateway_sn), message_engine_(message_engine) {
    initListening();
}

AICtrlMgr::~AICtrlMgr() {
    LOG_INFO("[AICtrlMgr][%s] dtor", drone_sn_.c_str());
    if (ai_alarm_state_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(ai_alarm_state_listen_id_);
        ai_alarm_state_listen_id_ = -1;
    }
}

void AICtrlMgr::initListening() {
    WeakDummy(p);
    ai_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcAiInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcAiInfoPushNorthbound>& msg)-> void {
            WeakDummyReturn(p);
            onReceiveAiInfo(msg);
        });
    LOG_INFO("[AICtrlMgr][%s] ObserveDrcDrcAiInfoPushNorthbound, ai_info_listen_id_ %d", drone_sn_.c_str(), ai_info_listen_id_);

    ai_alarm_state_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcAiAlarmStatePushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcAiAlarmStatePushNorthbound>& msg)-> void {
            WeakDummyReturn(p);
            onReceiveAiAlarmState(msg);
        });
    LOG_INFO("[AICtrlMgr][%s] ObserveDrcDrcAiAlarmStatePushNorthbound, ai_alarm_state_listen_id_ %d", drone_sn_.c_str(), ai_alarm_state_listen_id_);
}

void AICtrlMgr::onReceiveAiInfo(const std::shared_ptr<message_engine::DrcDrcAiInfoPushNorthbound>& msg) {
    LOG_INFO("[AICtrlMgr][%s] onReceiveAiInfo, msg: %s", drone_sn_.c_str(), msg->ToJsonString().c_str());
    flight_hub_sdk::AiDataInfo ai_data_info;
    ai_data_info.identify_on = msg->msg.identify_on;
    for (const auto& model: msg->msg.ai_model_list) {
        flight_hub_sdk::AiModelItem item;
        item.index = model.index;
        item.signed_name = model.signed_name;
        ai_data_info.ai_model_list.emplace_back(item);
    }
    ai_data_info.selected_ai_model.index = msg->msg.selected_ai_model.index;
    ai_data_info.selected_ai_model.score = msg->msg.selected_ai_model.score;
    ai_data_info.selected_ai_model.score_mode = msg->msg.selected_ai_model.score_mode;
    ai_data_info.selected_ai_model.image_source = msg->msg.selected_ai_model.image_source;
    ai_data_info.selected_ai_model.digital_effect = msg->msg.selected_ai_model.digital_effect;
    ai_data_info.selected_ai_model.filters = msg->msg.selected_ai_model.filters;
    for (const auto& label: msg->msg.selected_ai_model.labels) {
        flight_hub_sdk::AiModelLabel model_label;
        model_label.index = label.index;
        model_label.name = label.name;
        ai_data_info.selected_ai_model.labels.emplace_back(model_label);
    }
    for (const auto& cfg : msg->msg.ai_models_config) {
        flight_hub_sdk::AiModelsConfigSet item;
        item.stream_type = cfg.stream_type;
        item.models = cfg.models;
        ai_data_info.ai_models_config.emplace_back(item);
    }
    for (const auto& status : msg->msg.selected_ai_models_status) {
        flight_hub_sdk::AiModelsStatusItem item;
        item.index = status.index;
        item.light_type = status.light_type;
        item.score = status.score;
        item.score_mode = status.score_mode;
        item.filters = status.filters;
        ai_data_info.selected_ai_models_status.emplace_back(item);
    }
    for (const auto& info : msg->msg.ai_models_infos) {
        flight_hub_sdk::AiModelsInfoItem item;
        item.index = info.index;
        item.image_source = info.image_source;
        item.digital_effect = info.digital_effect;
        for (const auto& label : info.labels) {
            flight_hub_sdk::AiModelLabel model_label;
            model_label.index = label.index;
            model_label.name = label.name;
            item.labels.emplace_back(model_label);
        }
        ai_data_info.ai_models_infos.emplace_back(item);
    }
    ai_data_info_callback_container_.notifyCallbackIfChanged(ai_data_info);
}

int AICtrlMgr::listenAIDataInfo(const std::function<void(const AiDataInfo& state)> callback) {
    auto listen_id = ai_data_info_callback_container_.addCallback(callback);
    LOG_INFO("[AICtrlMgr][%s] listenAIDataInfo listen_id: %d", drone_sn_.c_str(), listen_id);
    return listen_id;
}

void AICtrlMgr::cancelListenAIDataInfo(int listen_id) {
    LOG_INFO("[AICtrlMgr][%s] cancelListenAIDataInfo listen_id: %d", drone_sn_.c_str(), listen_id);
    ai_data_info_callback_container_.removeCallback(listen_id);
}

void AICtrlMgr::AIIdentifySet(const AiIdentifySetParam& param, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] AIIdentifySet on: %d", drone_sn_.c_str(), param.on);
    auto msg = std::make_shared<message_engine::DrcDrcAiIdentifySetSouthbound>();
    msg->msg.on = param.on;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiIdentifySetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiIdentifySetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr AIIdentifySet callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置 AI 识别开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SET, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置 AI 识别开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SET, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置 AI 识别开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SET, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置 AI 识别开关(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SET, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::AIIdentifyFilterSet(const AiIdentifyFilterSetParam& param, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] AIIdentifyFilterSet index: %d, light_type: %d", drone_sn_.c_str(), param.index, param.light_type);
    auto msg = std::make_shared<message_engine::DrcDrcAiIdentifyFilterSetSouthbound>();
    msg->msg.index = param.index;
    msg->msg.light_type = param.light_type;
    msg->msg.filters = param.filters;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiIdentifyFilterSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr AIIdentifyFilterSet callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置 AI 识别过滤; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_FILTER_SET, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置 AI 识别过滤; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_FILTER_SET, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置 AI 识别过滤; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_FILTER_SET, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置 AI 识别过滤(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_FILTER_SET, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::AIIdentifyScoreSet(const AiIdentifyScoreSetParam& param, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] AIIdentifyScoreSet index: %d, light_type: %d, score: %d",
             drone_sn_.c_str(), param.index, param.light_type, param.score);
    auto msg = std::make_shared<message_engine::DrcDrcAiIdentifyScoreSetSouthbound>();
    msg->msg.index = param.index;
    msg->msg.light_type = param.light_type;
    msg->msg.score = param.score;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiIdentifyScoreSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr AIIdentifyScoreSet callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置 AI 识别分数阈值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_SET, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置 AI 识别分数阈值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_SET, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置 AI 识别分数阈值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_SET, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置 AI 识别分数阈值(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_SET, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::AIIdentifyScoreModeSet(const AiIdentifyScoreModeSetParam& param, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] AIIdentifyScoreModeSet index: %d, light_type: %d, score_mode: %d",
             drone_sn_.c_str(), param.index, param.light_type, param.score_mode);
    auto msg = std::make_shared<message_engine::DrcDrcAiIdentifyScoreModeSetSouthbound>();
    msg->msg.index = param.index;
    msg->msg.light_type = param.light_type;
    msg->msg.score_mode = param.score_mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiIdentifyScoreModeSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr AIIdentifyScoreModeSet callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置 AI 识别分数模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_MODE_SET, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置 AI 识别分数模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_MODE_SET, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置 AI 识别分数模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_MODE_SET, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置 AI 识别分数模式(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_MODE_SET, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::AIIdentifyScoreReset(const AiIdentifyScoreResetParam& param, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] AIIdentifyScoreReset index: %d, light_type: %d", drone_sn_.c_str(), param.index, param.light_type);
    auto msg = std::make_shared<message_engine::DrcDrcAiIdentifyScoreResetSouthbound>();
    msg->msg.index = param.index;
    msg->msg.light_type = param.light_type;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiIdentifyScoreResetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr AIIdentifyScoreReset callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 重置 AI 识别分数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_RESET, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 重置 AI 识别分数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_RESET, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 重置 AI 识别分数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_RESET, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 重置 AI 识别分数(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_RESET, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::AIModelSelect(const AiModelSelectParam& param, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] AIModelSelect, index: %d", drone_sn_.c_str(), param.index);
    auto msg = std::make_shared<message_engine::DrcDrcAiModelSelectSouthbound>();
    msg->msg.index = param.index;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiModelSelectSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiModelSelectNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr AIModelSelect callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 选择 AI 模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_MODEL_SELECT, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 选择 AI 模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_MODEL_SELECT, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 选择 AI 模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_MODEL_SELECT, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 选择 AI 模型(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_AI_MODEL_SELECT, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

// ── AI Spotlight Zoom (Tracking) ──

void AICtrlMgr::DrcAISpotlightZoomSet(int on, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAISpotlightZoomSet, on: %d", drone_sn_.c_str(), on);
    auto msg = std::make_shared<message_engine::DrcDrcAiSpotlightZoomSetSouthbound>();
    msg->msg.on = on;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiSpotlightZoomSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAISpotlightZoomSet callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: AI 跟随开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SET, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: AI 跟随开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SET, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: AI 跟随开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SET, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: AI 跟随开关(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SET, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::DrcAISpotlightZoomSelect(const AiSpotlightZoomSelectParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAISpotlightZoomSelect, center_x: %f, center_y: %f, width: %f, height: %f", drone_sn_.c_str(), params.center_x, params.center_y, params.width, params.height);
    auto msg = std::make_shared<message_engine::DrcDrcAiSpotlightZoomSelectSouthbound>();
    msg->msg.center_x = params.center_x;
    msg->msg.center_y = params.center_y;
    msg->msg.width = params.width;
    msg->msg.height = params.height;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiSpotlightZoomSelectSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAISpotlightZoomSelect callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: AI 框选目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SELECT, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: AI 框选目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SELECT, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: AI 框选目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SELECT, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: AI 框选目标跟随(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SELECT, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::DrcAISpotlightZoomConfirm(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAISpotlightZoomConfirm", drone_sn_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcAiSpotlightZoomConfirmSouthbound>();
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiSpotlightZoomConfirmSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAISpotlightZoomConfirm callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: AI 框选目标跟随确认; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_CONFIRM, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: AI 框选目标跟随确认; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_CONFIRM, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: AI 框选目标跟随确认; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_CONFIRM, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: AI 框选目标跟随确认(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_CONFIRM, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::DrcAISpotlightZoomStop(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAISpotlightZoomStop", drone_sn_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcAiSpotlightZoomStopSouthbound>();
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiSpotlightZoomStopSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAISpotlightZoomStop callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 停止 AI 目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_STOP, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 停止 AI 目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_STOP, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 停止 AI 目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_STOP, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 停止 AI 目标跟随(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_STOP, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::DrcAISpotlightZoomTrack(int target_index, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAISpotlightZoomTrack, target_index: %d", drone_sn_.c_str(), target_index);
    auto msg = std::make_shared<message_engine::DrcDrcAiSpotlightZoomTrackSouthbound>();
    msg->msg.target_index = target_index;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiSpotlightZoomTrackSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAISpotlightZoomTrack callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: AI 识别目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_TRACK, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: AI 识别目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_TRACK, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: AI 识别目标跟随; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_TRACK, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: AI 识别目标跟随(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_TRACK, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

// ── AI Alarm ──

void AICtrlMgr::DrcAIAlarmStart(const AiAlarmStartParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAIAlarmStart", drone_sn_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcAiAlarmStartSouthbound>();
    for (const auto& conf : params.stream_input_confs) {
        message_engine::DrcAiAlarmStreamInputConf me_conf;
        me_conf.stream_source = conf.stream_source;
        me_conf.model_idx = conf.model_idx;
        me_conf.algorithm_type = conf.algorithm_type;
        me_conf.alarm_interval = conf.alarm_interval;
        for (const auto& target : conf.alarm_targets) {
            message_engine::DrcAiAlarmTarget me_target;
            me_target.target_type = target.target_type;
            me_target.target_value = target.target_value;
            me_target.use_min_threshold = target.use_min_threshold;
            me_target.use_max_threshold = target.use_max_threshold;
            me_target.target_min_threshold = target.target_min_threshold;
            me_target.target_max_threshold = target.target_max_threshold;
            me_conf.alarm_targets.emplace_back(me_target);
        }
        msg->msg.stream_input_confs.emplace_back(me_conf);
    }
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiAlarmStartSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiAlarmStartNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAIAlarmStart callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 开启 AI 主动告警; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_START, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 开启 AI 主动告警; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_START, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 开启 AI 主动告警; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_START, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 开启 AI 主动告警(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_START, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::DrcAIAlarmStop(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAIAlarmStop", drone_sn_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcAiAlarmStopSouthbound>();
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiAlarmStopSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiAlarmStopNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAIAlarmStop callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 关闭 AI 主动告警; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_STOP, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 关闭 AI 主动告警; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_STOP, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 关闭 AI 主动告警; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_STOP, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 关闭 AI 主动告警(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_STOP, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

void AICtrlMgr::DrcAIModelsConfig(const AiModelsConfigParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[AICtrlMgr][%s] DrcAIModelsConfig", drone_sn_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcAiModelsConfigSouthbound>();
    for (const auto& config_set : params.ai_models_config) {
        message_engine::AiModelsConfigSet me_set;
        me_set.stream_type = config_set.stream_type;
        me_set.models = config_set.models;
        msg->msg.ai_models_config.emplace_back(me_set);
    }
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAiModelsConfigSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAiModelsConfigNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("AICtrlMgr DrcAIModelsConfig callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 配置 AI 模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_MODELS_CONFIG, drc_err, "[AICtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 配置 AI 模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_MODELS_CONFIG, reply_err, "[AICtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 配置 AI 模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_MODELS_CONFIG, success_err, "[AICtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 配置 AI 模型(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_ONBOARD_AI_DRC_AI_MODELS_CONFIG, link_err, "[AICtrlMgr]");

        callback(link_err);
    }
}

// ── AI Alarm State Push (Observe) ──

void AICtrlMgr::onReceiveAiAlarmState(const std::shared_ptr<message_engine::DrcDrcAiAlarmStatePushNorthbound>& msg) {
    LOG_INFO("[AICtrlMgr][%s] onReceiveAiAlarmState", drone_sn_.c_str());
    if (!msg) return;
    AiAlarmStateInfo info;
    info.alarm_on = msg->msg.alarm_on;
    for (const auto& conf : msg->msg.stream_input_confs) {
        AiAlarmStreamInputConf sdk_conf;
        sdk_conf.stream_source = conf.stream_source;
        sdk_conf.model_idx = conf.model_idx;
        sdk_conf.algorithm_type = conf.algorithm_type;
        sdk_conf.alarm_interval = conf.alarm_interval;
        for (const auto& target : conf.alarm_targets) {
            AiAlarmTarget sdk_target;
            sdk_target.target_type = target.target_type;
            sdk_target.target_value = target.target_value;
            sdk_target.use_min_threshold = target.use_min_threshold;
            sdk_target.use_max_threshold = target.use_max_threshold;
            sdk_target.target_min_threshold = target.target_min_threshold;
            sdk_target.target_max_threshold = target.target_max_threshold;
            sdk_conf.alarm_targets.emplace_back(sdk_target);
        }
        info.stream_input_confs.emplace_back(sdk_conf);
    }
    ai_alarm_state_callback_container_.notifyCallbackIfChanged(info);
}

int AICtrlMgr::listenDrcAIAlarmState(const std::function<void(const AiAlarmStateInfo& info)>& callback) {
    auto listen_id = ai_alarm_state_callback_container_.addCallback(callback);
    LOG_INFO("[AICtrlMgr][%s] listenDrcAIAlarmState listen_id: %d", drone_sn_.c_str(), listen_id);
    return listen_id;
}

void AICtrlMgr::cancelListenDrcAIAlarmState(int listen_id) {
    LOG_INFO("[AICtrlMgr][%s] cancelListenDrcAIAlarmState listen_id: %d", drone_sn_.c_str(), listen_id);
    ai_alarm_state_callback_container_.removeCallback(listen_id);
}

}  // namespace flight_hub_sdk