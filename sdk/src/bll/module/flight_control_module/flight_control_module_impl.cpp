// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_control_module_impl.h"

#include <i_real_time_channel.h>
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

FlightControlModuleImpl::FlightControlModuleImpl(
    const std::string& drone_sn,
    const std::string& gateway_sn,
    std::shared_ptr<message_engine::IMessageEngine> message_engine,
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr)
    :
      drone_sn_(drone_sn),
      gateway_sn_(gateway_sn),
      message_engine_(std::move(message_engine)),
      flight_ctrl_mgr_(std::move(flight_ctrl_mgr)) {
    LOG_INFO("[FlightControlModuleImpl] constructor, drone_sn: %s, gateway_sn: %s",
        drone_sn_.c_str(), gateway_sn_.c_str());
}

void FlightControlModuleImpl::drcEmergencyStop(const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDroneEmergencyStopSouthbound>();
    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDroneEmergencyStopSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDroneEmergencyStopNorthbound>& msg) {
        WeakDummyReturn(weak_ptr);
        if (error_code == message_engine::MeErrorCode::ME_NO_ERROR && msg->msg.result == 0) {
            auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);

            // 埋点: 紧急停桨; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_EMERGENCY_STOP, result, "[FlightControlModuleImpl]");

            callback(result);
            return;
        }

        auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 紧急停桨; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_EMERGENCY_STOP, result, "[FlightControlModuleImpl]");

        callback(result);
    });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK);

        // 埋点: 紧急停桨(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_EMERGENCY_STOP, link_err, "[FlightControlModuleImpl]");

        callback(link_err);
    }
}

void FlightControlModuleImpl::drcEmergencyLanding(const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcEmergencyLandingSouthbound>();
    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcEmergencyLandingSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcEmergencyLandingNorthbound>& reply) {
        WeakDummyReturn(weak_ptr);
        if (error_code == message_engine::MeErrorCode::ME_NO_ERROR && reply && reply->msg.result == 0) {
            auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);

            // 埋点: 紧急降落; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_EMERGENCY_LANDING, result, "[FlightControlModuleImpl]");

            callback(result);
            return;
        }

        auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 紧急降落; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_EMERGENCY_LANDING, result, "[FlightControlModuleImpl]");

        callback(result);
    });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK);

        // 埋点: 紧急降落(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_EMERGENCY_LANDING, link_err, "[FlightControlModuleImpl]");

        callback(link_err);
    }
}

void FlightControlModuleImpl::drcForceLanding(const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcForceLandingSouthbound>();
    WeakDummy(weak_ptr);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcForceLandingSouthbound(msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcForceLandingNorthbound>& reply) {
        WeakDummyReturn(weak_ptr);
        if (error_code == message_engine::MeErrorCode::ME_NO_ERROR && reply && reply->msg.result == 0) {
            auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);

            // 埋点: 强制降落; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_FORCE_LANDING, result, "[FlightControlModuleImpl]");

            callback(result);
            return;
        }
        auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 强制降落; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_FORCE_LANDING, result, "[FlightControlModuleImpl]");

        callback(result);
    });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK);

        // 埋点: 强制降落(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_FORCE_LANDING, link_err, "[FlightControlModuleImpl]");

        callback(link_err);
    }
}

void FlightControlModuleImpl::returnHome(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightControlModuleImpl][%s] returnHome", gateway_sn_.c_str());

    ReturnHomeRequest request;
    request.sn = gateway_sn_;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        return_home(request, [this, weak_ptr, callback](const NonRealtimeCmdResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                LOG_INFO("[FlightControlModuleImpl] returnHome, response: %d %s", response.code, response.message.c_str());

                // 埋点: 一键返航; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_RETURN_HOME, err, "[FlightControlModuleImpl]");

                callback(err);
            });
        });
    });
}

void FlightControlModuleImpl::returnHomeCancel(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightControlModuleImpl][%s] returnHomeCancel", gateway_sn_.c_str());

    ReturnHomeCancelRequest request;
    request.sn = gateway_sn_;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        return_home_cancel(request, [this, weak_ptr, callback](const NonRealtimeCmdResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                LOG_INFO("[FlightControlModuleImpl] returnHomeCancel, response: %d %s", response.code, response.message.c_str());

                // 埋点: 取消一键返航; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_RETURN_HOME_CANCEL, err, "[FlightControlModuleImpl]");

                callback(err);
            });
        });
    });
}

bool FlightControlModuleImpl::isDrtcRealTimeLinkActive() const {
    if (!message_engine_) {
        return false;
    }
    auto rtc_mgr = message_engine_->getRtcChannelManager();
    if (!rtc_mgr) {
        return false;
    }
    for (const auto& weak_ch : rtc_mgr->GetRealTimeChannels()) {
        auto ch = weak_ch.lock();
        if (!ch) {
            continue;
        }
        if (ch->GetChannelInfo().provider_type_ == message_engine::ChannelProviderType::DRTC) {
            return true;
        }
    }
    return false;
}

void FlightControlModuleImpl::drcSendStickControl(const StickControlData& data) {
    LOG_DEBUG("[FlightControlModuleImpl][%s] drcSendStickControl", gateway_sn_.c_str());

    if (isDrtcRealTimeLinkActive()) {
        message_engine::CompressedSticksControlSouthboundMsg msg;
        msg.roll = static_cast<uint16_t>(data.roll);
        msg.pitch = static_cast<uint16_t>(data.pitch);
        msg.throttle = static_cast<uint16_t>(data.throttle);
        msg.yaw = static_cast<uint16_t>(data.yaw);
        msg.gimbal_pitch = static_cast<uint16_t>(data.gimbal_pitch);

        auto message = std::make_shared<message_engine::CompressedSticksControlSouthbound>(msg);
        auto err = message_engine_->getCloudMessageManager()->SendCompressedSticksControlSouthbound(
            message, drc_sdk::common::Executors::Main());
        if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
            LOG_ERROR("[FlightControlModuleImpl][%s] SendCompressedSticksControlSouthbound failed, err=%d",
                      gateway_sn_.c_str(), static_cast<int>(err));
        }
        return;
    }

    message_engine::DrcStickControlSouthboundMsg msg;
    msg.roll = data.roll;
    msg.pitch = data.pitch;
    msg.throttle = data.throttle;
    msg.yaw = data.yaw;
    msg.gimbal_pitch = data.gimbal_pitch;

    auto message = std::make_shared<message_engine::DrcStickControlSouthbound>(msg);
    message_engine_->getCloudMessageManager()->SendDrcStickControlSouthbound(
        message, drc_sdk::common::Executors::Main());
}

// ── Flight Control Auxiliary (DRC) ──

void FlightControlModuleImpl::drcSetDroneHover(int duration, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置飞机悬停时间(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_DRONE_HOVER, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcSetDroneHover(duration, callback);
}

void FlightControlModuleImpl::drcSetFollowMouseParameter(int speed, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置指哪打哪参数(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FOLLOW_MOUSE_PARAMETER, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcSetFollowMouseParameter(speed, callback);
}

void FlightControlModuleImpl::drcSetNightLightsState(int night_lights_state, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置夜航灯状态(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_NIGHT_LIGHTS_STATE, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcSetNightLightsState(night_lights_state, callback);
}

void FlightControlModuleImpl::drcSetStealthState(int stealth_state, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置隐身模式(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_STEALTH_STATE, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcSetStealthState(stealth_state, callback);
}

void FlightControlModuleImpl::drcOpenParachute(int force, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 开伞(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_OPEN_PARACHUTE, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcOpenParachute(force, callback);
}

void FlightControlModuleImpl::drcSetFlashLed(const FlashLedParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置爆闪灯(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FLASH_LED, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcSetFlashLed(params, callback);
}

void FlightControlModuleImpl::drcCameraLookAt(const CameraLookAtParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 相机 LookAt(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_CAMERA_LOOK_AT, state_err, "[FlightControlModuleImpl]");

        callback(state_err);
        return;
    }
    flight_ctrl_mgr_->drcCameraLookAt(params, callback);
}

// ── Northbound Push Listeners (DRC) ──

int FlightControlModuleImpl::listenDrcOsdInfo(const std::function<void(const DrcOsdInfo&)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return -1;
    }
    return flight_ctrl_mgr_->listenDrcOsdInfo(callback);
}

void FlightControlModuleImpl::cancelListenDrcOsdInfo(int listen_id) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return;
    }
    flight_ctrl_mgr_->cancelListenDrcOsdInfo(listen_id);
}

int FlightControlModuleImpl::listenDrcBatteriesInfo(const std::function<void(const DrcBatteriesInfo&)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return -1;
    }
    return flight_ctrl_mgr_->listenDrcBatteriesInfo(callback);
}

void FlightControlModuleImpl::cancelListenDrcBatteriesInfo(int listen_id) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return;
    }
    flight_ctrl_mgr_->cancelListenDrcBatteriesInfo(listen_id);
}

int FlightControlModuleImpl::listenDrcParachuteInfo(const std::function<void(const DrcParachuteInfo&)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return -1;
    }
    return flight_ctrl_mgr_->listenDrcParachuteInfo(callback);
}

void FlightControlModuleImpl::cancelListenDrcParachuteInfo(int listen_id) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return;
    }
    flight_ctrl_mgr_->cancelListenDrcParachuteInfo(listen_id);
}

int FlightControlModuleImpl::listenDrcFlashLed(const std::function<void(const FlashLedStateInfo&)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return -1;
    }
    return flight_ctrl_mgr_->listenDrcFlashLed(callback);
}

void FlightControlModuleImpl::cancelListenDrcFlashLed(int listen_id) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[FlightControlModuleImpl] flight_ctrl_mgr_ is null");
        return;
    }
    flight_ctrl_mgr_->cancelListenDrcFlashLed(listen_id);
}
