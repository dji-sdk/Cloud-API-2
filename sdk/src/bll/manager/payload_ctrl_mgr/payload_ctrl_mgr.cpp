// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "payload_ctrl_mgr.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/request_message.h"
#include "http_interface/http_interface.h"
#include "api/flight_hub_sdk.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <common/executors/executors.h>
#include "tool/flight_hub_utils.h"

namespace flight_hub_sdk {

PayloadCtrlMgr::PayloadCtrlMgr(const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine, int drone_instance_id)
    : drone_sn_(drone_sn), gateway_sn_(gateway_sn), message_engine_(message_engine) {
    instance_id_ = drone_instance_id;
    log_tag_ = drone_sn_ + "#" + std::to_string(instance_id_);
    LOG_INFO("[PayloadCtrlMgr] constructor, instance_id: %d, drone_sn: %s", instance_id_, drone_sn_.c_str());
    initializeDeviceStatusListening();
}

PayloadCtrlMgr::~PayloadCtrlMgr() {
    LOG_INFO("[PayloadCtrlMgr][%s] destructor", log_tag_.c_str());
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_payloads_list_listen_id_);
    camera_abs_map_.clear();
}

void PayloadCtrlMgr::initializeDeviceStatusListening() {
    WeakDummy(weak_ptr);
    aircraft_payloads_list_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPayloadsList(
        [this, weak_ptr](const notification_center::AircraftPayloadsList& msg)-> void {
            WeakDummyReturn(weak_ptr);
            onReceiveAircraftPayloadsList(msg);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(weak_ptr);
            LOG_INFO("[PayloadCtrlMgr][%s] ListenAircraftPayloadsList, listen result: %d", log_tag_.c_str(), error_code);
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);
    if (aircraft_payloads_list_listen_id_ < 0) {
        LOG_ERROR("[PayloadCtrlMgr][%s] ListenAircraftPayloadsList failed, aircraft_payloads_list_listen_id_ %d", log_tag_.c_str(), aircraft_payloads_list_listen_id_);
    }

    // Important log, 后续的操控动作，内部检查 PAYLOAD_INDEX 是否与当前设备一致;
    LOG_INFO("[PayloadCtrlMgr][%s] initializeDeviceStatusListening() completed, aircraft_payloads_list_listen_id: %d", log_tag_.c_str(), aircraft_payloads_list_listen_id_);
}

void PayloadCtrlMgr::onReceiveAircraftPayloadsList(const notification_center::AircraftPayloadsList& msg) {
    // 检查是否与上次消息相同，避免重复处理
    if (aircraft_payloads_list_msg_ == msg) {
        return;
    }
    
    // 如果收到空的payloads_list，记录日志
    if (msg.payloads_list.empty()) {
        LOG_WARN("[PayloadCtrlMgr][%s] onReceiveAircraftPayloadsList, received empty payloads_list, will clear all CameraAbs", log_tag_.c_str());
    }
    
    // 1. 先同步更新内部状态（使用事务性更新模式，避免中间状态暴露）
    // 在临时容器中构建新状态
    std::unordered_map<std::string, std::shared_ptr<CameraAbs>> new_camera_abs_map;
    for (const auto& payload_index : msg.payloads_list) {
        if (camera_abs_map_.find(payload_index) != camera_abs_map_.end()) {
            // 复用现有的 CameraAbs 对象
            new_camera_abs_map[payload_index] = camera_abs_map_[payload_index];
        } else {
            // 创建新的 CameraAbs 对象
            new_camera_abs_map[payload_index] = std::make_shared<CameraAbs>(payload_index, drone_sn_, gateway_sn_, message_engine_, instance_id_);
            LOG_INFO("[PayloadCtrlMgr][%s] Create CameraAbs, payload_index: %s", log_tag_.c_str(), payload_index.c_str());
        }
    }
    
    // 原子性替换（swap 操作很快，避免中间状态暴露）
    camera_abs_map_.swap(new_camera_abs_map);
    aircraft_payloads_list_msg_ = msg;

    LOG_INFO("[PayloadCtrlMgr][%s] onReceiveAircraftPayloadsList completed, payloads_list size: %d", log_tag_.c_str(), msg.payloads_list.size());

    // 2. 内部状态已更新完成，再 Post 任务到 main 线程通知用户
    // 这样确保用户回调时，内部状态已经就绪
    if (aircraft_payloads_list_user_callback_) {
        WeakDummy(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([this, msg, weak_ptr]() {
            WeakDummyReturn(weak_ptr);
            // 转换数据格式
            AircraftPayloadsList payloads_list;
            payloads_list.payloads_list = msg.payloads_list;
            // 通知用户（此时内部状态已更新）
            aircraft_payloads_list_user_callback_(payloads_list);
        });
    }
}

void PayloadCtrlMgr::setAircraftPayloadsListCallback(std::function<void(const AircraftPayloadsList&)> callback) {
    aircraft_payloads_list_user_callback_ = callback;
}

void PayloadCtrlMgr::acquirePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] acquirePayloadControlAuthority", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] acquirePayloadControlAuthority, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 抢夺负载控制权(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_PAYLOAD_CONTROL_AUTHORITY, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->acquirePayloadControlAuthority([callback](FlightHubError err) {
        // 埋点: 抢夺负载控制权; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_PAYLOAD_CONTROL_AUTHORITY, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::releasePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] releasePayloadControlAuthority", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] releasePayloadControlAuthority, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 释放负载控制权(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_RELEASE_PAYLOAD_CONTROL_AUTHORITY, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->releasePayloadControlAuthority([callback](FlightHubError err) {
        // 埋点: 释放负载控制权; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_RELEASE_PAYLOAD_CONTROL_AUTHORITY, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

int PayloadCtrlMgr::listenPayloadControlAuthority(const std::string& payload_index, const std::function<void(bool)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenPayloadControlAuthority, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenPayloadControlAuthority(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenPayloadControlAuthority listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenPayloadControlAuthority(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenPayloadControlAuthority listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenPayloadControlAuthority, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenPayloadControlAuthority(listen_id);
}

void PayloadCtrlMgr::drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcSwitchLiveLens, payload_index: %s, lens_type: %s", log_tag_.c_str(), params.payload_index.c_str(), cameraLensTypeToString(static_cast<CameraLensType>(params.lens_type)).c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSwitchLiveLens, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 切换直播镜头(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_LIVE_LENS, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSwitchLiveLens(params, [callback](FlightHubError err) {
        // 埋点: 切换直播镜头; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_LIVE_LENS, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("PayloadCtrlMgr: drcSetPhotoStorageSettings, payload_index: %s", params.payload_index.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: drcSetPhotoStorageSettings, payload_index: %s not found", params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置照片存储参数(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_STORAGE_SETTINGS, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetPhotoStorageSettings(params, [callback](FlightHubError err) {
        // 埋点: 设置照片存储参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_STORAGE_SETTINGS, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("PayloadCtrlMgr: drcSetVideoStorageSettings, payload_index: %s", params.payload_index.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: drcSetVideoStorageSettings, payload_index: %s not found", params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置视频存储参数(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_STORAGE_SETTINGS, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetVideoStorageSettings(params, [callback](FlightHubError err) {
        // 埋点: 设置视频存储参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_STORAGE_SETTINGS, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("PayloadCtrlMgr: drcSetNightSceneMode, payload_index: %s", params.payload_index.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: drcSetNightSceneMode, payload_index: %s not found", params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置夜景模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_NIGHT_SCENE_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetNightSceneMode(params, [callback](FlightHubError err) {
        // 埋点: 设置夜景模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_NIGHT_SCENE_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcTakePhoto, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 拍照(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_TAKE_PHOTO, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->drcTakePhoto([callback](FlightHubError err) {
        // 埋点: 拍照; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_TAKE_PHOTO, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// smart photo
void PayloadCtrlMgr::drcSmartPhotoTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: drcSmartPhotoTakePhoto, payload_index: %s not found", payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 智能低光拍照(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SMART_PHOTO_TAKE_PHOTO, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->drcSmartPhotoTakePhoto([callback](FlightHubError err) {
        // 埋点: 智能低光拍照; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SMART_PHOTO_TAKE_PHOTO, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

int PayloadCtrlMgr::listenDrcCameraState(const std::string& payload_index, const std::function<void(const CameraState& state)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(CameraState());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraState(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraState listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraState(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraState listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcCameraState(listen_id);
}

int PayloadCtrlMgr::listenDrcLiveviewWorldRegion(const std::string& payload_index, const std::function<void(const LiveviewWorldRegion& region)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcLiveviewWorldRegion, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(LiveviewWorldRegion());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcLiveviewWorldRegion(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcLiveviewWorldRegion listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcLiveviewWorldRegion(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcLiveviewWorldRegion listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcLiveviewWorldRegion, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcLiveviewWorldRegion(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraWideLensState(const std::string& payload_index, const std::function<void(const CameraWideLensState& state)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraWideLensState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(CameraWideLensState());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraWideLensState(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraWideLensState listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraWideLensState(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraWideLensState listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraWideLensState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcCameraWideLensState(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraZoomLensState(const std::string& payload_index, const std::function<void(const CameraZoomLensState& state)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraZoomLensState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(CameraZoomLensState());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraZoomLensState(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraZoomLensState listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraZoomLensState(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraZoomLensState listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraZoomLensState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcCameraZoomLensState(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraMeasureTarget(const std::string& payload_index, const std::function<void(const CameraMeasureTarget& target)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraMeasureTarget, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(CameraMeasureTarget());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraMeasureTarget(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraMeasureTarget listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraMeasureTarget(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraMeasureTarget listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraMeasureTarget, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcCameraMeasureTarget(listen_id);
}

int PayloadCtrlMgr::listenDrcNightModeSettings(const std::string& payload_index, const std::function<void(const NightModeSettings& settings)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: listenDrcNightModeSettings, payload_index: %s not found", payload_index.c_str());
        callback(NightModeSettings());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcNightModeSettings(callback);
    LOG_INFO("PayloadCtrlMgr: listenDrcNightModeSettings listen_id: %d", listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcNightModeSettings(const std::string& payload_index, int listen_id) {
    LOG_INFO("PayloadCtrlMgr: cancelListenDrcNightModeSettings listen_id: %d", listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: cancelListenDrcNightModeSettings, payload_index: %s not found", payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcNightModeSettings(listen_id);
}

// ── B4 drc_camera_state_push 拆分 listen ──
// 每个 pair 结构一致：找 camera_abs → forward。为了可读，逐个展开，不用宏。

int PayloadCtrlMgr::listenDrcCameraWatermarkSettings(const std::string& payload_index, const std::function<void(const CameraWatermarkSettings&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraWatermarkSettings, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraWatermarkSettings(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraWatermarkSettings(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraWatermarkSettings, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraWatermarkSettings(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraStorageInfo(const std::string& payload_index, const std::function<void(const CameraStorageInfo&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraStorageInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraStorageInfo(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraStorageInfo(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraStorageInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraStorageInfo(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraIrPipState(const std::string& payload_index, const std::function<void(const CameraIrPipState&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraIrPipState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraIrPipState(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraIrPipState(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraIrPipState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraIrPipState(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraIsothermPolygon(const std::string& payload_index, const std::function<void(const CameraIsothermPolygon&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraIsothermPolygon, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraIsothermPolygon(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraIsothermPolygon(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraIsothermPolygon, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraIsothermPolygon(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraMediaStorage(const std::string& payload_index, const std::function<void(const CameraMediaStorage&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraMediaStorage, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraMediaStorage(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraMediaStorage(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraMediaStorage, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraMediaStorage(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraAlgorithmDehazeInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraAlgorithmDehazeInfo(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraAlgorithmDehazeInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraAlgorithmDehazeInfo(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraPreRecordedInfo(const std::string& payload_index, const std::function<void(const CameraPreRecordedInfo&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraPreRecordedInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraPreRecordedInfo(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraPreRecordedInfo(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraPreRecordedInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraPreRecordedInfo(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraCapability(const std::string& payload_index, const std::function<void(const CameraCapability&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraCapability, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraCapability(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraCapability(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraCapability, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraCapability(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraSettableCapability(const std::string& payload_index, const std::function<void(const CameraSettableCapability&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraSettableCapability, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraSettableCapability(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraSettableCapability(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraSettableCapability, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraSettableCapability(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraVisibleCapability(const std::string& payload_index, const std::function<void(const CameraVisibleCapability&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraVisibleCapability, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraVisibleCapability(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraVisibleCapability(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraVisibleCapability, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraVisibleCapability(listen_id);
}

// ── B4.1 ──
int PayloadCtrlMgr::listenDrcCameraPhotoInfo(const std::string& payload_index, const std::function<void(const CameraPhotoInfo&)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraPhotoInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    return camera_abs_map_[payload_index]->listenDrcCameraPhotoInfo(callback);
}
void PayloadCtrlMgr::cancelListenDrcCameraPhotoInfo(const std::string& payload_index, int listen_id) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraPhotoInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    camera_abs_map_[payload_index]->cancelListenDrcCameraPhotoInfo(listen_id);
}

int PayloadCtrlMgr::listenDrcGimbalState(const std::string& payload_index, const std::function<void(const GimbalState& state)> callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcGimbalState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcGimbalState(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcGimbalState listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcGimbalState(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcGimbalState listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcGimbalState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcGimbalState(listen_id);
}

int PayloadCtrlMgr::listenDrcLiveVideoInfo(const std::string& payload_index, const std::function<void(const LiveVideoInfo& info)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcLiveVideoInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(LiveVideoInfo());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcLiveVideoInfo(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcLiveVideoInfo listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcLiveVideoInfo(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcLiveVideoInfo listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcLiveVideoInfo, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcLiveVideoInfo(listen_id);
}

// 红外增益模式
int PayloadCtrlMgr::listenDrcThermalGainMode(const std::string& payload_index, const std::function<void(const ThermalGainModeInfo& info)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcThermalGainMode, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(ThermalGainModeInfo());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcThermalGainMode(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcThermalGainMode listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcThermalGainMode(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcThermalGainMode listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcThermalGainMode, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcThermalGainMode(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraIrLensState(const std::string& payload_index, const std::function<void(const CameraIrLensState& state)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraIrLensState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(CameraIrLensState());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraIrLensState(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraIrLensState listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraIrLensState(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraIrLensState listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraIrLensState, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcCameraIrLensState(listen_id);
}

int PayloadCtrlMgr::listenDrcCameraIrThermometry(const std::string& payload_index, const std::function<void(const CameraIrThermometryInfo& info)>& callback) {
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraIrThermometry, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(CameraIrThermometryInfo());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraIrThermometry(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraIrThermometry listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraIrThermometry(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraIrThermometry listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcCameraIrThermometry, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcCameraIrThermometry(listen_id);
}

void PayloadCtrlMgr::drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcSetThermalGainMode", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetThermalGainMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外增益模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_GAIN_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetThermalGainMode(params, [callback](FlightHubError err) {
        // 埋点: 设置红外增益模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_GAIN_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// record
void PayloadCtrlMgr::drcStartRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcStartRecord", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcStartRecord, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 开始录像(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_START_RECORD, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->drcStartRecord([callback](FlightHubError err) {
        // 埋点: 开始录像; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_START_RECORD, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcStopRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcStopRecord", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcStopRecord, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止录像(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_RECORD, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->drcStopRecord([callback](FlightHubError err) {
        // 埋点: 停止录像; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_RECORD, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// zoom at target
void PayloadCtrlMgr::drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcZoomAtTarget", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcZoomAtTarget, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 框选变焦(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_ZOOM_AT_TARGET, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcZoomAtTarget(params, [callback](FlightHubError err) {
        // 埋点: 框选变焦; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_ZOOM_AT_TARGET, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}


// set focal length;
void PayloadCtrlMgr::drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcSetFocalLength", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetFocalLength, payload_index: %s NOT found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置焦距(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_FOCAL_LENGTH, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetFocalLength(params, [callback](FlightHubError err) {
        // 埋点: 设置焦距; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_FOCAL_LENGTH, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}


// set linkage zoom;
void PayloadCtrlMgr::drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("PayloadCtrlMgr: drcSetLinkageZoom");
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: drcSetLinkageZoom, payload_index: %s NOT found", params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置联动变焦(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_LINKAGE_ZOOM, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetLinkageZoom(params, [callback](FlightHubError err) {
        // 埋点: 设置联动变焦; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_LINKAGE_ZOOM, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}


// 负载控制: 重置云台(method: gimbal_reset);
void PayloadCtrlMgr::drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcResetGimbal", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcResetGimbal, payload_index: %s NOT found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 重置云台(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_GIMBAL, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }

    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcResetGimbal(params, [callback](FlightHubError err) {
        // 埋点: 重置云台; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_GIMBAL, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}


// look at target
void PayloadCtrlMgr::lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] lookAtTarget", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] lookAtTarget, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 看向目标(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_LOOK_AT_TARGET, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->lookAtTarget(params, [callback](FlightHubError err) {
        // 埋点: 看向目标; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_LOOK_AT_TARGET, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcSendCameraScreenDrag payload_index: %s", log_tag_.c_str(), params.payload_index.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSendCameraScreenDrag, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 画面拖动控制云台转动速度(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SEND_CAMERA_SCREEN_DRAG, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSendCameraScreenDrag(params, [callback](FlightHubError err) {
        // 埋点: 画面拖动控制云台转动速度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SEND_CAMERA_SCREEN_DRAG, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// switch camera mode
void PayloadCtrlMgr::drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcSwitchCameraMode", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSwitchCameraMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 切换相机模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_CAMERA_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSwitchCameraMode(params, [callback](FlightHubError err) {
        // 埋点: 切换相机模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_CAMERA_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

int PayloadCtrlMgr::listenDrcCameraModeAvailableRange(const std::string& payload_index, const std::function<void(const std::vector<int>&)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraModeAvailableRange", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcCameraModeAvailableRange, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcCameraModeAvailableRange(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcCameraModeAvailableRange listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcCameraModeAvailableRange(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcCameraModeAvailableRange listen_id: %d", log_tag_.c_str(), listen_id);
}


// panorama
void PayloadCtrlMgr::drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcStartPanorama", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcStartPanorama, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 开始全景拍摄(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_START_PANORAMA, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcStartPanorama(params, [callback](FlightHubError err) {
        // 埋点: 开始全景拍摄; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_START_PANORAMA, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcStopPanorama(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcStopPanorama", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcStopPanorama, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止全景拍摄(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_PANORAMA, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->drcStopPanorama([callback](FlightHubError err) {
        // 埋点: 停止全景拍摄; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_PANORAMA, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

int PayloadCtrlMgr::listenDrcPanoramaStatus(const std::string& payload_index, const std::function<void(const PanoramaStatus& status)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcPanoramaStatus", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcPanoramaStatus, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        callback(PanoramaStatus());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcPanoramaStatus(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcPanoramaStatus listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcPanoramaStatus(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcPanoramaStatus listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcPanoramaStatus, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcPanoramaStatus(listen_id);
}

void PayloadCtrlMgr::drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("PayloadCtrlMgr: drcSetPhotoSize");
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("PayloadCtrlMgr: drcSetPhotoSize, payload_index: %s not found", params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置照片分辨率(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_SIZE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetPhotoSize(params, [callback](FlightHubError err) {
        // 埋点: 设置照片分辨率; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_SIZE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcSetIntervalTakePhoto", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIntervalTakePhoto, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置定时拍参数(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INTERVAL_TAKE_PHOTO, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcSetIntervalTakePhoto(params, [callback](FlightHubError err) {
        // 埋点: 设置定时拍参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INTERVAL_TAKE_PHOTO, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcIntervalTakePhoto", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcIntervalTakePhoto, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 执行定时拍(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_INTERVAL_TAKE_PHOTO, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcIntervalTakePhoto(params, [callback](FlightHubError err) {
        // 埋点: 执行定时拍; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_INTERVAL_TAKE_PHOTO, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcStopTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcStopTakePhoto", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcStopTakePhoto, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止拍照(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_TAKE_PHOTO, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->drcStopTakePhoto([callback](FlightHubError err) {
        // 埋点: 停止拍照; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_TAKE_PHOTO, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

int PayloadCtrlMgr::listenDrcIntervalTakePhotoStatus(const std::string& payload_index, const std::function<void(const IntervalTakePhotoStatus& status)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcIntervalTakePhotoStatus", log_tag_.c_str());
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] listenDrcIntervalTakePhotoStatus, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return -1;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    auto listen_id = camera_abs->listenDrcIntervalTakePhotoStatus(callback);
    LOG_INFO("[PayloadCtrlMgr][%s] listenDrcIntervalTakePhotoStatus listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void PayloadCtrlMgr::cancelListenDrcIntervalTakePhotoStatus(const std::string& payload_index, int listen_id) {
    LOG_INFO("[PayloadCtrlMgr][%s] cancelListenDrcIntervalTakePhotoStatus listen_id: %d", log_tag_.c_str(), listen_id);
    if (camera_abs_map_.find(payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] cancelListenDrcIntervalTakePhotoStatus, payload_index: %s not found", log_tag_.c_str(), payload_index.c_str());
        return;
    }
    auto camera_abs = camera_abs_map_[payload_index];
    camera_abs->cancelListenDrcIntervalTakePhotoStatus(listen_id);
}

void PayloadCtrlMgr::drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[PayloadCtrlMgr][%s] drcCameraAimAtTarget", log_tag_.c_str());
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcCameraAimAtTarget, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 双击画面瞄准目标(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_AIM_AT_TARGET, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    auto camera_abs = camera_abs_map_[params.payload_index];
    camera_abs->drcCameraAimAtTarget(params, [callback](FlightHubError err) {
        // 埋点: 双击画面瞄准目标; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_AIM_AT_TARGET, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// ══════════════════════════════════════════════
// Camera Advanced Params (DRC)
// ══════════════════════════════════════════════

void PayloadCtrlMgr::drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraApertureValue, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机光圈值(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_APERTURE_VALUE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraApertureValue(params, [callback](FlightHubError err) {
        // 埋点: 设置相机光圈值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_APERTURE_VALUE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraExposureMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机曝光模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraExposureMode(params, [callback](FlightHubError err) {
        // 埋点: 设置相机曝光模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraExposureValue, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机曝光值(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_VALUE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraExposureValue(params, [callback](FlightHubError err) {
        // 埋点: 设置相机曝光值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_VALUE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraFocusMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机对焦模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraFocusMode(params, [callback](FlightHubError err) {
        // 埋点: 设置相机对焦模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraFocusValue, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机对焦值(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_VALUE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraFocusValue(params, [callback](FlightHubError err) {
        // 埋点: 设置相机对焦值; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_VALUE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraIso, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机 ISO(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_ISO, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraIso(params, [callback](FlightHubError err) {
        // 埋点: 设置相机 ISO; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_ISO, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraMechanicalShutter, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机机械快门(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_MECHANICAL_SHUTTER, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraMechanicalShutter(params, [callback](FlightHubError err) {
        // 埋点: 设置相机机械快门; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_MECHANICAL_SHUTTER, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcCameraPointFocusAction, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 相机点对焦操作(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_POINT_FOCUS_ACTION, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcCameraPointFocusAction(params, [callback](FlightHubError err) {
        // 埋点: 相机点对焦操作; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_POINT_FOCUS_ACTION, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraShutter, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机快门速度(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SHUTTER, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraShutter(params, [callback](FlightHubError err) {
        // 埋点: 设置相机快门速度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SHUTTER, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraWatermark, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机水印(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_WATERMARK, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraWatermark(params, [callback](FlightHubError err) {
        // 埋点: 设置相机水印; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_WATERMARK, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraDewarping, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机畸变矫正(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DEWARPING, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraDewarping(params, [callback](FlightHubError err) {
        // 埋点: 设置相机畸变矫正; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DEWARPING, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// ── Screen/Gimbal Advanced (DRC) ──

void PayloadCtrlMgr::drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraScreenSplit, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机分屏显示(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SCREEN_SPLIT, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraScreenSplit(params, [callback](FlightHubError err) {
        // 埋点: 设置相机分屏显示; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SCREEN_SPLIT, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcCleanFog, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 镜头去雾(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CLEAN_FOG, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcCleanFog(params, [callback](FlightHubError err) {
        // 埋点: 镜头去雾; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CLEAN_FOG, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// ── Thermal/IR (DRC) ──

void PayloadCtrlMgr::drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetThermalCurrentPalette, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置热成像调色板(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_CURRENT_PALETTE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetThermalCurrentPalette(params, [callback](FlightHubError err) {
        // 埋点: 设置热成像调色板; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_CURRENT_PALETTE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetThermalSuperResolutionEnable, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置热成像超分辨率(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_SUPER_RESOLUTION_ENABLE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetThermalSuperResolutionEnable(params, [callback](FlightHubError err) {
        // 埋点: 设置热成像超分辨率; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_SUPER_RESOLUTION_ENABLE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIrMeteringMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外测温模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIrMeteringMode(params, [callback](FlightHubError err) {
        // 埋点: 设置红外测温模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIrMeteringPoint, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外测温点(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_POINT, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIrMeteringPoint(params, [callback](FlightHubError err) {
        // 埋点: 设置红外测温点; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_POINT, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIrMeteringArea, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外测温区域(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_AREA, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIrMeteringArea(params, [callback](FlightHubError err) {
        // 埋点: 设置红外测温区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_AREA, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIrDisplayMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外显示模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_DISPLAY_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIrDisplayMode(params, [callback](FlightHubError err) {
        // 埋点: 设置红外显示模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_DISPLAY_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIrEnhanceMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外增强模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_ENHANCE_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIrEnhanceMode(params, [callback](FlightHubError err) {
        // 埋点: 设置红外增强模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_ENHANCE_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIrMixRate, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外融合比例(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_MIX_RATE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIrMixRate(params, [callback](FlightHubError err) {
        // 埋点: 设置红外融合比例; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_MIX_RATE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIsothermEnable, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线开关(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ENABLE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIsothermEnable(params, [callback](FlightHubError err) {
        // 埋点: 设置等温线开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ENABLE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIsothermColorRenderingType, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线着色类型(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_COLOR_RENDERING_TYPE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIsothermColorRenderingType(params, [callback](FlightHubError err) {
        // 埋点: 设置等温线着色类型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_COLOR_RENDERING_TYPE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIsothermLowerUpperRange, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线温度上下限(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_LOWER_UPPER_RANGE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIsothermLowerUpperRange(params, [callback](FlightHubError err) {
        // 埋点: 设置等温线温度上下限; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_LOWER_UPPER_RANGE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIsothermRenderingPolygon, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线多边形渲染区域(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_RENDERING_POLYGON, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIsothermRenderingPolygon(params, [callback](FlightHubError err) {
        // 埋点: 设置等温线多边形渲染区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_RENDERING_POLYGON, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetIsothermRoiBasedRenderingEnable, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置按框选区域的等温线渲染使能(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ROI_BASED_RENDERING_ENABLE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetIsothermRoiBasedRenderingEnable(params, [callback](FlightHubError err) {
        // 埋点: 设置按框选区域的等温线渲染使能; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ROI_BASED_RENDERING_ENABLE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetInfraredFillLightEnable, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置近红外补光灯使能(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INFRARED_FILL_LIGHT_ENABLE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetInfraredFillLightEnable(params, [callback](FlightHubError err) {
        // 埋点: 设置近红外补光灯使能; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INFRARED_FILL_LIGHT_ENABLE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraNightVisionEnable, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置黑白夜视使能(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_NIGHT_VISION_ENABLE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraNightVisionEnable(params, [callback](FlightHubError err) {
        // 埋点: 设置黑白夜视使能; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_NIGHT_VISION_ENABLE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcResetCameraSettings, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 重置相机设置(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_CAMERA_SETTINGS, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcResetCameraSettings(params, [callback](FlightHubError err) {
        // 埋点: 重置相机设置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_CAMERA_SETTINGS, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

// ── Other Camera DRC ──

void PayloadCtrlMgr::drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraDenoiseLevelSet, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机降噪等级(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DENOISE_LEVEL_SET, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraDenoiseLevelSet(params, [callback](FlightHubError err) {
        // 埋点: 设置相机降噪等级; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DENOISE_LEVEL_SET, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetVideoResolution, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置视频分辨率(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_RESOLUTION, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetVideoResolution(params, [callback](FlightHubError err) {
        // 埋点: 设置视频分辨率; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_RESOLUTION, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCameraPhotoFormat, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置照片格式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_PHOTO_FORMAT, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetCameraPhotoFormat(params, [callback](FlightHubError err) {
        // 埋点: 设置照片格式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_PHOTO_FORMAT, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetPreRecordedStatus, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置预录制状态(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_STATUS, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetPreRecordedStatus(params, [callback](FlightHubError err) {
        // 埋点: 设置预录制状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_STATUS, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetPreRecordedTime, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置预录制时长(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_TIME, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetPreRecordedTime(params, [callback](FlightHubError err) {
        // 埋点: 设置预录制时长; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_TIME, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.empty()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetCountDownAction, no camera available", log_tag_.c_str());
        auto no_camera_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置倒计时动作(无可用相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_COUNT_DOWN_ACTION, no_camera_err, "[PayloadCtrlMgr]");

        callback(no_camera_err);
        return;
    }
    camera_abs_map_.begin()->second->drcSetCountDownAction(count_down_type, action_type, [callback](FlightHubError err) {
        // 埋点: 设置倒计时动作; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_COUNT_DOWN_ACTION, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.empty()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetAssistantVideoMode, no camera available", log_tag_.c_str());
        auto no_camera_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置辅助画面模式(无可用相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_MODE, no_camera_err, "[PayloadCtrlMgr]");

        callback(no_camera_err);
        return;
    }
    camera_abs_map_.begin()->second->drcSetAssistantVideoMode(mode, [callback](FlightHubError err) {
        // 埋点: 设置辅助画面模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.empty()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetAssistantVideoSwitch, no camera available", log_tag_.c_str());
        auto no_camera_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置辅助画面开关(无可用相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_SWITCH, no_camera_err, "[PayloadCtrlMgr]");

        callback(no_camera_err);
        return;
    }
    camera_abs_map_.begin()->second->drcSetAssistantVideoSwitch(sw, [callback](FlightHubError err) {
        // 埋点: 设置辅助画面开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_SWITCH, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetAlgorithmDehazeMode, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机电子去雾模式(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_MODE, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetAlgorithmDehazeMode(params, [callback](FlightHubError err) {
        // 埋点: 设置相机电子去雾模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_MODE, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

void PayloadCtrlMgr::drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (camera_abs_map_.find(params.payload_index) == camera_abs_map_.end()) {
        LOG_ERROR("[PayloadCtrlMgr][%s] drcSetAlgorithmDehazeLevel, payload_index: %s not found", log_tag_.c_str(), params.payload_index.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机电子去雾等级(payload_index 无对应相机, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_LEVEL, not_found_err, "[PayloadCtrlMgr]");

        callback(not_found_err);
        return;
    }
    camera_abs_map_[params.payload_index]->drcSetAlgorithmDehazeLevel(params, [callback](FlightHubError err) {
        // 埋点: 设置相机电子去雾等级; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_LEVEL, err, "[PayloadCtrlMgr]");

        callback(err);
    });
}

}