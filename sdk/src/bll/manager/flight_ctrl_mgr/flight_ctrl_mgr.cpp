// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_ctrl_mgr.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/request_message.h"
#include "http_interface/http_interface.h"
#include "api/flight_hub_sdk.h"
#include "notification_center/notification_center.h"

namespace flight_hub_sdk {

FlightCtrlMgr::FlightCtrlMgr(const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine, int drone_instance_id)
    : drone_sn_(drone_sn), gateway_sn_(gateway_sn), message_engine_(message_engine) {
    instance_id_ = drone_instance_id;
    log_tag_ = drone_sn_ + "#" + std::to_string(instance_id_);
    LOG_INFO("[FlightCtrlMgr] constructor, instance_id: %d, drone_sn: %s", instance_id_, drone_sn_.c_str());
    initFlightControlAuthorityListening();
    initHsiInfoListening();
    initOsdInfoListening();
    initBatteriesInfoListening();
    initParachuteInfoListening();
    initFlashLedListening();
}

FlightCtrlMgr::~FlightCtrlMgr() {
    LOG_INFO("[FlightCtrlMgr][%s] dtor", log_tag_.c_str());
    if (flight_control_authority_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(flight_control_authority_listen_id_);
        flight_control_authority_listen_id_ = -1;
    }
    if (hsi_info_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(hsi_info_listen_id_);
        hsi_info_listen_id_ = -1;
    }
    if (osd_info_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(osd_info_listen_id_);
        osd_info_listen_id_ = -1;
    }
    if (batteries_info_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(batteries_info_listen_id_);
        batteries_info_listen_id_ = -1;
    }
    if (parachute_info_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(parachute_info_listen_id_);
        parachute_info_listen_id_ = -1;
    }
    if (flash_led_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(flash_led_listen_id_);
        flash_led_listen_id_ = -1;
    }
}

void FlightCtrlMgr::initFlightControlAuthorityListening() {
    WeakDummy(weak_ptr);
    flight_control_authority_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenControlRightsCloudControlStatus(
        [this, weak_ptr](const notification_center::ControlRightsCloudControlStatus& msg) -> void {
            WeakDummyReturn(weak_ptr);
            onReceiveFlightControlAuthority(msg);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            LOG_INFO("[FlightCtrlMgr][%s] ListenControlRightsCloudControlStatus, listen result: %d", log_tag_.c_str(), error_code);
        },
        drone_sn_, notification_center::NotificationFrequency_OnChanged);
    if (flight_control_authority_listen_id_ < 0) {
        LOG_ERROR("[FlightCtrlMgr][%s] ListenControlRightsCloudControlStatus failed, flight_control_authority_listen_id_ %d",
            log_tag_.c_str(), flight_control_authority_listen_id_);
    }
}

void FlightCtrlMgr::onReceiveFlightControlAuthority(const notification_center::ControlRightsCloudControlStatus& msg) {
    flight_control_authority_msg_ = msg;
    // TODO: 当前后端方案使得 SDK 没有身份标识，先默认有飞行控制权
    flight_control_authority_callback_container_.notifyCallback(true);
    LOG_INFO("[FlightCtrlMgr][%s] ListenControlRightsCloudControlStatus, flight_control_authority: 1",
        log_tag_.c_str());
}

void FlightCtrlMgr::acquireFlightControlAuthority(const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] acquireFlightControlAuthority", log_tag_.c_str());
    AcquireCtrlAuthorityRequest request;
    request.drone_sn = drone_sn_;
    request.control_keys = {"flight"};
    request.gateway_sn = gateway_sn_;
    WeakDummy(p);
    acquire_ctrl_authority(request, [callback, p, this](const AcquireCtrlAuthorityResponse& response, FlightHubError err) {
        WeakDummyReturn(p);

        // 埋点: 抢夺飞行控制权; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_FLIGHT_CONTROL_AUTHORITY, err, "[FlightCtrlMgr]");

        LOG_INFO("[FlightCtrlMgr][%s] acquireFlightControlAuthority, err_code: %d", log_tag_.c_str(), static_cast<int>(err.error_code));
        if (callback) {
            callback(err);
        }
    });
}

void FlightCtrlMgr::releaseFlightControlAuthority(const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] releaseFlightControlAuthority", log_tag_.c_str());
    ReleaseCtrlAuthorityRequest request;
    request.drone_sn = drone_sn_;
    request.control_keys = {"flight"};
    request.gateway_sn = gateway_sn_;
    WeakDummy(p);
    release_ctrl_authority(request, [callback, p, this](const ReleaseCtrlAuthorityResponse& response, FlightHubError err) {
        WeakDummyReturn(p);

        // 埋点: 释放飞行控制权; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_RELEASE_FLIGHT_CONTROL_AUTHORITY, err, "[FlightCtrlMgr]");

        LOG_INFO("[FlightCtrlMgr][%s] releaseFlightControlAuthority, err_code: %d", log_tag_.c_str(), static_cast<int>(err.error_code));
        if (callback) {
            callback(err);
        }
    });
}

int FlightCtrlMgr::listenFlightControlAuthority(const std::function<void(bool)>& callback) {
    auto listen_id = flight_control_authority_callback_container_.addCallback(callback);
    LOG_INFO("[FlightCtrlMgr][%s] listenFlightControlAuthority listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void FlightCtrlMgr::cancelListenFlightControlAuthority(int listen_id) {
    LOG_INFO("[FlightCtrlMgr][%s] cancelListenFlightControlAuthority listen_id: %d", log_tag_.c_str(), listen_id);
    flight_control_authority_callback_container_.removeCallback(listen_id);
}

// HSI 信息监听
void FlightCtrlMgr::initHsiInfoListening() {
    WeakDummy(p);
    hsi_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcHsiInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcHsiInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            if (!msg) return;
            onReceiveHsiInfo(*msg);
        }
    );
    if (hsi_info_listen_id_ < 0) {
        LOG_ERROR("FlightCtrlMgr: ObserveDrcHsiInfoPushNorthbound failed, hsi_info_listen_id_ %d", hsi_info_listen_id_);
    }
    LOG_INFO("FlightCtrlMgr: ObserveDrcHsiInfoPushNorthbound, hsi_info_listen_id_ %d", hsi_info_listen_id_);
}

void FlightCtrlMgr::onReceiveHsiInfo(const message_engine::DrcHsiInfoPushNorthbound& msg) {
    HsiInfo hsi_info;
    hsi_info.around_distances = msg.msg.around_distances;
    hsi_info.back_enable = msg.msg.back_enable;
    hsi_info.back_work = msg.msg.back_work;
    hsi_info.down_distance = msg.msg.down_distance;
    hsi_info.down_enable = msg.msg.down_enable;
    hsi_info.down_work = msg.msg.down_work;
    hsi_info.front_enable = msg.msg.front_enable;
    hsi_info.front_work = msg.msg.front_work;
    hsi_info.horizontal_enable = msg.msg.horizontal_enable;
    hsi_info.horizontal_work = msg.msg.horizontal_work;
    hsi_info.left_enable = msg.msg.left_enable;
    hsi_info.left_work = msg.msg.left_work;
    hsi_info.right_enable = msg.msg.right_enable;
    hsi_info.right_work = msg.msg.right_work;
    hsi_info.up_distance = msg.msg.up_distance;
    hsi_info.up_enable = msg.msg.up_enable;
    hsi_info.up_work = msg.msg.up_work;
    hsi_info.vertical_enable = msg.msg.vertical_enable;
    hsi_info.vertical_work = msg.msg.vertical_work;
    hsi_info_callback_container_.notifyCallbackIfChanged(hsi_info);
}

int FlightCtrlMgr::listenHsiInfo(const std::function<void(const HsiInfo&)>& callback) {
    auto listen_id = hsi_info_callback_container_.addCallback(callback);
    LOG_INFO("FlightCtrlMgr: listenHsiInfo listen_id: %d", listen_id);
    return listen_id;
}

void FlightCtrlMgr::cancelListenHsiInfo(int listen_id) {
    LOG_INFO("FlightCtrlMgr: cancelListenHsiInfo listen_id: %d", listen_id);
    hsi_info_callback_container_.removeCallback(listen_id);
}

// ── Flight Control Auxiliary ──

void FlightCtrlMgr::drcSetDroneHover(int duration, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcSetDroneHover, duration: %d", log_tag_.c_str(), duration);
    auto msg = std::make_shared<message_engine::DrcDrcDroneHoverSetSouthbound>();
    msg->msg.duration = duration;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcDroneHoverSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcDroneHoverSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcSetDroneHover callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置飞机悬停时间; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_DRONE_HOVER, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置飞机悬停时间; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_DRONE_HOVER, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置飞机悬停时间; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_DRONE_HOVER, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置飞机悬停时间(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_DRONE_HOVER, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

void FlightCtrlMgr::drcSetFollowMouseParameter(int speed, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcSetFollowMouseParameter, speed: %d", log_tag_.c_str(), speed);
    auto msg = std::make_shared<message_engine::DrcDrcFollowMouseParameterSetSouthbound>();
    msg->msg.speed = speed;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcFollowMouseParameterSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcFollowMouseParameterSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcSetFollowMouseParameter callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置指哪打哪参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FOLLOW_MOUSE_PARAMETER, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置指哪打哪参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FOLLOW_MOUSE_PARAMETER, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置指哪打哪参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FOLLOW_MOUSE_PARAMETER, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置指哪打哪参数(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FOLLOW_MOUSE_PARAMETER, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

void FlightCtrlMgr::drcSetNightLightsState(int night_lights_state, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcSetNightLightsState, state: %d", log_tag_.c_str(), night_lights_state);
    auto msg = std::make_shared<message_engine::DrcDrcNightLightsStateSetSouthbound>();
    msg->msg.night_lights_state = night_lights_state;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcNightLightsStateSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcNightLightsStateSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcSetNightLightsState callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置夜航灯状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_NIGHT_LIGHTS_STATE, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置夜航灯状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_NIGHT_LIGHTS_STATE, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置夜航灯状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_NIGHT_LIGHTS_STATE, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置夜航灯状态(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_NIGHT_LIGHTS_STATE, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

void FlightCtrlMgr::drcSetStealthState(int stealth_state, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcSetStealthState, state: %d", log_tag_.c_str(), stealth_state);
    auto msg = std::make_shared<message_engine::DrcDrcStealthStateSetSouthbound>();
    msg->msg.stealth_state = stealth_state;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcStealthStateSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcStealthStateSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcSetStealthState callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置隐身模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_STEALTH_STATE, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置隐身模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_STEALTH_STATE, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置隐身模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_STEALTH_STATE, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置隐身模式(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_STEALTH_STATE, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

void FlightCtrlMgr::drcOpenParachute(int force, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcOpenParachute, force: %d", log_tag_.c_str(), force);
    auto msg = std::make_shared<message_engine::DrcDrcOpenParachuteSouthbound>();
    msg->msg.force = force;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcOpenParachuteSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcOpenParachuteNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcOpenParachute callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 开伞; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_OPEN_PARACHUTE, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 开伞; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_OPEN_PARACHUTE, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 开伞; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_OPEN_PARACHUTE, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 开伞(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_OPEN_PARACHUTE, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

void FlightCtrlMgr::drcSetFlashLed(const FlashLedParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcSetFlashLed, on: %d", log_tag_.c_str(), params.on);
    auto msg = std::make_shared<message_engine::DrcDrcFlashLedSetSouthbound>();
    msg->msg.on = params.on;
    msg->msg.pattern_uuid = params.pattern_uuid;
    msg->msg.pattern_name = params.pattern_name;
    for (const auto& pattern : params.patterns) {
        message_engine::PatternInfo me_pattern;
        me_pattern.color = pattern.color;
        me_pattern.period = pattern.period;
        me_pattern.brightness = pattern.brightness;
        msg->msg.patterns.emplace_back(me_pattern);
    }
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcFlashLedSetSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcFlashLedSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcSetFlashLed callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 设置爆闪灯; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FLASH_LED, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 设置爆闪灯; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FLASH_LED, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 设置爆闪灯; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FLASH_LED, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 设置爆闪灯(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_SET_FLASH_LED, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

void FlightCtrlMgr::drcCameraLookAt(const CameraLookAtParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[FlightCtrlMgr][%s] drcCameraLookAt, payload_index: %s, locked: %d", log_tag_.c_str(), params.payload_index.c_str(), params.locked);
    auto msg = std::make_shared<message_engine::DrcDrcCameraLookAtSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.locked = params.locked;
    msg->msg.height = params.height;
    msg->msg.latitude = params.latitude;
    msg->msg.longitude = params.longitude;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraLookAtSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraLookAtNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("FlightCtrlMgr drcCameraLookAt callback, error_code: %d, result: %d", error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: 相机 LookAt; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_CAMERA_LOOK_AT, drc_err, "[FlightCtrlMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: 相机 LookAt; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_CAMERA_LOOK_AT, reply_err, "[FlightCtrlMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: 相机 LookAt; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_CAMERA_LOOK_AT, success_err, "[FlightCtrlMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: 相机 LookAt(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_CAMERA_LOOK_AT, link_err, "[FlightCtrlMgr]");

        callback(link_err);
    }
}

// ── Northbound Observes ──

void FlightCtrlMgr::initOsdInfoListening() {
    WeakDummy(p);
    osd_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcOsdInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcOsdInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveOsdInfo(msg);
        }
    );
    if (osd_info_listen_id_ < 0) {
        LOG_ERROR("[FlightCtrlMgr][%s] ObserveDrcOsdInfoPushNorthbound failed, osd_info_listen_id_ %d", log_tag_.c_str(), osd_info_listen_id_);
    }
    LOG_INFO("[FlightCtrlMgr][%s] ObserveDrcOsdInfoPushNorthbound, osd_info_listen_id_ %d", log_tag_.c_str(), osd_info_listen_id_);
}

void FlightCtrlMgr::onReceiveOsdInfo(const std::shared_ptr<message_engine::DrcOsdInfoPushNorthbound>& msg) {
    if (!msg) return;
    DrcOsdInfo info;
    info.attitude_head = msg->msg.attitude_head;
    info.height = msg->msg.height;
    info.latitude = msg->msg.latitude;
    info.longitude = msg->msg.longitude;
    info.speed_x = msg->msg.speed_x;
    info.speed_y = msg->msg.speed_y;
    info.speed_z = msg->msg.speed_z;
    info.ultrasonic_height = msg->msg.ultrasonic_height;
    info.horizontal_speed = msg->msg.horizontal_speed;
    info.vertical_speed = msg->msg.vertical_speed;
    info.home_distance = msg->msg.home_distance;
    info.wind_direction = msg->msg.wind_direction;
    info.wind_speed = msg->msg.wind_speed;
    info.elevation = msg->msg.elevation;
    info.gimbal_pitch = msg->msg.gimbal_pitch;
    info.gimbal_yaw = msg->msg.gimbal_yaw;
    info.gimbal_roll = msg->msg.gimbal_roll;
    osd_info_callback_container_.notifyCallbackIfChanged(info);
}

int FlightCtrlMgr::listenDrcOsdInfo(const std::function<void(const DrcOsdInfo& info)>& callback) {
    auto listen_id = osd_info_callback_container_.addCallback(callback);
    LOG_INFO("[FlightCtrlMgr][%s] listenDrcOsdInfo listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void FlightCtrlMgr::cancelListenDrcOsdInfo(int listen_id) {
    LOG_INFO("[FlightCtrlMgr][%s] cancelListenDrcOsdInfo listen_id: %d", log_tag_.c_str(), listen_id);
    osd_info_callback_container_.removeCallback(listen_id);
}

void FlightCtrlMgr::initBatteriesInfoListening() {
    WeakDummy(p);
    batteries_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcBatteriesInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcBatteriesInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveBatteriesInfo(msg);
        }
    );
    if (batteries_info_listen_id_ < 0) {
        LOG_ERROR("[FlightCtrlMgr][%s] ObserveDrcDrcBatteriesInfoPushNorthbound failed, batteries_info_listen_id_ %d", log_tag_.c_str(), batteries_info_listen_id_);
    }
    LOG_INFO("[FlightCtrlMgr][%s] ObserveDrcDrcBatteriesInfoPushNorthbound, batteries_info_listen_id_ %d", log_tag_.c_str(), batteries_info_listen_id_);
}

void FlightCtrlMgr::onReceiveBatteriesInfo(const std::shared_ptr<message_engine::DrcDrcBatteriesInfoPushNorthbound>& msg) {
    if (!msg) return;
    DrcBatteriesInfo info;
    info.capacity_percent = msg->msg.capacity_percent;
    info.landing_power = msg->msg.landing_power;
    info.remain_flight_time = msg->msg.remain_flight_time;
    info.return_home_power = msg->msg.return_home_power;
    info.remain_job_time = msg->msg.remain_job_time;
    info.low_battery_warning_threshold = msg->msg.low_battery_warning_threshold;
    info.serious_low_battery_warning_threshold = msg->msg.serious_low_battery_warning_threshold;
    for (const auto& battery : msg->msg.batteries) {
        DrcBatteryItem item;
        item.capacity_percent = battery.capacity_percent;
        item.firmware_version = battery.firmware_version;
        item.high_voltage_storage_days = battery.high_voltage_storage_days;
        item.index = battery.index;
        item.loop_times = battery.loop_times;
        item.sn = battery.sn;
        item.temperature = battery.temperature;
        item.voltage = battery.voltage;
        info.batteries.emplace_back(item);
    }
    batteries_info_callback_container_.notifyCallbackIfChanged(info);
}

int FlightCtrlMgr::listenDrcBatteriesInfo(const std::function<void(const DrcBatteriesInfo& info)>& callback) {
    auto listen_id = batteries_info_callback_container_.addCallback(callback);
    LOG_INFO("[FlightCtrlMgr][%s] listenDrcBatteriesInfo listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void FlightCtrlMgr::cancelListenDrcBatteriesInfo(int listen_id) {
    LOG_INFO("[FlightCtrlMgr][%s] cancelListenDrcBatteriesInfo listen_id: %d", log_tag_.c_str(), listen_id);
    batteries_info_callback_container_.removeCallback(listen_id);
}

void FlightCtrlMgr::initParachuteInfoListening() {
    WeakDummy(p);
    parachute_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcParachuteInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcParachuteInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveParachuteInfo(msg);
        }
    );
    if (parachute_info_listen_id_ < 0) {
        LOG_ERROR("[FlightCtrlMgr][%s] ObserveDrcDrcParachuteInfoPushNorthbound failed, parachute_info_listen_id_ %d", log_tag_.c_str(), parachute_info_listen_id_);
    }
    LOG_INFO("[FlightCtrlMgr][%s] ObserveDrcDrcParachuteInfoPushNorthbound, parachute_info_listen_id_ %d", log_tag_.c_str(), parachute_info_listen_id_);
}

void FlightCtrlMgr::onReceiveParachuteInfo(const std::shared_ptr<message_engine::DrcDrcParachuteInfoPushNorthbound>& msg) {
    if (!msg) return;
    DrcParachuteInfo info;
    info.parachute_firmware_version = msg->msg.parachute_firmware_version;
    info.parachute_is_fired = msg->msg.parachute_is_fired;
    info.parachute_on_fire_status = msg->msg.parachute_on_fire_status;
    info.parachute_type = msg->msg.parachute_type;
    info.parachute_sn = msg->msg.parachute_sn;
    info.parachute_is_abnormal = msg->msg.parachute_is_abnormal;
    parachute_info_callback_container_.notifyCallbackIfChanged(info);
}

int FlightCtrlMgr::listenDrcParachuteInfo(const std::function<void(const DrcParachuteInfo& info)>& callback) {
    auto listen_id = parachute_info_callback_container_.addCallback(callback);
    LOG_INFO("[FlightCtrlMgr][%s] listenDrcParachuteInfo listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void FlightCtrlMgr::cancelListenDrcParachuteInfo(int listen_id) {
    LOG_INFO("[FlightCtrlMgr][%s] cancelListenDrcParachuteInfo listen_id: %d", log_tag_.c_str(), listen_id);
    parachute_info_callback_container_.removeCallback(listen_id);
}

void FlightCtrlMgr::initFlashLedListening() {
    WeakDummy(p);
    flash_led_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcDroneStatePushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcDroneStatePushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveFlashLedState(msg);
        }
    );
    if (flash_led_listen_id_ < 0) {
        LOG_ERROR("[FlightCtrlMgr][%s] ObserveDrcDrcDroneStatePushNorthbound failed, flash_led_listen_id_ %d",
            log_tag_.c_str(), flash_led_listen_id_);
    }
    LOG_INFO("[FlightCtrlMgr][%s] ObserveDrcDrcDroneStatePushNorthbound, flash_led_listen_id_ %d",
        log_tag_.c_str(), flash_led_listen_id_);
}

void FlightCtrlMgr::onReceiveFlashLedState(const std::shared_ptr<message_engine::DrcDrcDroneStatePushNorthbound>& msg) {
    if (!msg) return;
    FlashLedStateInfo info;
    info.on = msg->msg.flash_led.on;
    info.pattern_uuid = msg->msg.flash_led.pattern_uuid;
    info.pattern_name = msg->msg.flash_led.pattern_name;
    for (const auto& p : msg->msg.flash_led.patterns) {
        FlashLedPatternInfo pattern;
        pattern.color = p.color;
        pattern.period = p.period;
        pattern.brightness = p.brightness;
        info.patterns.emplace_back(pattern);
    }
    flash_led_callback_container_.notifyCallbackIfChanged(info);
}

int FlightCtrlMgr::listenDrcFlashLed(const std::function<void(const FlashLedStateInfo& info)>& callback) {
    auto listen_id = flash_led_callback_container_.addCallback(callback);
    LOG_INFO("[FlightCtrlMgr][%s] listenDrcFlashLed listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void FlightCtrlMgr::cancelListenDrcFlashLed(int listen_id) {
    LOG_INFO("[FlightCtrlMgr][%s] cancelListenDrcFlashLed listen_id: %d", log_tag_.c_str(), listen_id);
    flash_led_callback_container_.removeCallback(listen_id);
}

}  // namespace flight_hub_sdk