// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "camera_control_module_impl.h"

#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;

CameraControlModuleImpl::CameraControlModuleImpl(std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr)
    : payload_ctrl_mgr_(std::move(payload_ctrl_mgr)) {
    LOG_INFO("[CameraControlModuleImpl] constructor");
}

void CameraControlModuleImpl::drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 切换直播镜头(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_LIVE_LENS, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSwitchLiveLens(params, callback);
}


void CameraControlModuleImpl::drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置照片存储参数(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_STORAGE_SETTINGS, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetPhotoStorageSettings(params, callback);
}


void CameraControlModuleImpl::drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置视频存储参数(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_STORAGE_SETTINGS, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetVideoStorageSettings(params, callback);
}


void CameraControlModuleImpl::drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置夜景模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_NIGHT_SCENE_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetNightSceneMode(params, callback);
}


void CameraControlModuleImpl::drcTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 拍照(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_TAKE_PHOTO, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcTakePhoto(payload_index, callback);
}


void CameraControlModuleImpl::drcSmartPhotoTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 智能低光拍照(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SMART_PHOTO_TAKE_PHOTO, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSmartPhotoTakePhoto(payload_index, callback);
}


void CameraControlModuleImpl::drcStartRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 开始录像(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_START_RECORD, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcStartRecord(payload_index, callback);
}


void CameraControlModuleImpl::drcStopRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止录像(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_RECORD, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcStopRecord(payload_index, callback);
}


void CameraControlModuleImpl::drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 框选变焦(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_ZOOM_AT_TARGET, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcZoomAtTarget(params, callback);
}


void CameraControlModuleImpl::drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置焦距(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_FOCAL_LENGTH, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetFocalLength(params, callback);
}


void CameraControlModuleImpl::drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置联动变焦(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_LINKAGE_ZOOM, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetLinkageZoom(params, callback);
}


void CameraControlModuleImpl::drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 重置云台(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_GIMBAL, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcResetGimbal(params, callback);
}


void CameraControlModuleImpl::lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 看向目标(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_LOOK_AT_TARGET, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->lookAtTarget(params, callback);
}


void CameraControlModuleImpl::drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 画面拖动控制云台转动速度(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SEND_CAMERA_SCREEN_DRAG, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSendCameraScreenDrag(params, callback);
}


void CameraControlModuleImpl::drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 开始全景拍摄(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_START_PANORAMA, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcStartPanorama(params, callback);
}


void CameraControlModuleImpl::drcStopPanorama(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止全景拍摄(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_PANORAMA, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcStopPanorama(payload_index, callback);
}


int CameraControlModuleImpl::listenDrcPanoramaStatus(const std::string& payload_index, const std::function<void(const PanoramaStatus& status)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcPanoramaStatus(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcPanoramaStatus(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcPanoramaStatus(payload_index, listen_id);
}


void CameraControlModuleImpl::drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置照片分辨率(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_SIZE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetPhotoSize(params, callback);
}


void CameraControlModuleImpl::drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置定时拍参数(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INTERVAL_TAKE_PHOTO, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIntervalTakePhoto(params, callback);
}


void CameraControlModuleImpl::drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 执行定时拍(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_INTERVAL_TAKE_PHOTO, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcIntervalTakePhoto(params, callback);
}


void CameraControlModuleImpl::drcStopTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 停止拍照(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_TAKE_PHOTO, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcStopTakePhoto(payload_index, callback);
}


int CameraControlModuleImpl::listenDrcIntervalTakePhotoStatus(const std::string& payload_index, const std::function<void(const IntervalTakePhotoStatus& status)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcIntervalTakePhotoStatus(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcIntervalTakePhotoStatus(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcIntervalTakePhotoStatus(payload_index, listen_id);
}


void CameraControlModuleImpl::drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 双击画面瞄准目标(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_AIM_AT_TARGET, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcCameraAimAtTarget(params, callback);
}


void CameraControlModuleImpl::drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 切换相机模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_CAMERA_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSwitchCameraMode(params, callback);
}


int CameraControlModuleImpl::listenDrcCameraModeAvailableRange(const std::string& payload_index, const std::function<void(const std::vector<int>&)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraModeAvailableRange(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcCameraModeAvailableRange(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraModeAvailableRange(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcCameraState(const std::string& payload_index, const std::function<void(const CameraState& state)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraState(payload_index,callback);
}


void CameraControlModuleImpl::cancelListenDrcCameraState(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraState(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcLiveviewWorldRegion(const std::string& payload_index, const std::function<void(const LiveviewWorldRegion& region)>& callback) {  
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcLiveviewWorldRegion(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcLiveviewWorldRegion(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");   
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcLiveviewWorldRegion(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcCameraWideLensState(const std::string& payload_index, const std::function<void(const CameraWideLensState& state)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraWideLensState(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcCameraWideLensState(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraWideLensState(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcCameraZoomLensState(const std::string& payload_index, const std::function<void(const CameraZoomLensState& state)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraZoomLensState(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcCameraZoomLensState(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraZoomLensState(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcCameraMeasureTarget(const std::string& payload_index, const std::function<void(const CameraMeasureTarget& target)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraMeasureTarget(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcCameraMeasureTarget(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraMeasureTarget(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcNightModeSettings(const std::string& payload_index, const std::function<void(const NightModeSettings& settings)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcNightModeSettings(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcNightModeSettings(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcNightModeSettings(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcGimbalState(const std::string& payload_index, const std::function<void(const GimbalState& state)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcGimbalState(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcGimbalState(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcGimbalState(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcLiveVideoInfo(const std::string& payload_index, const std::function<void(const LiveVideoInfo& info)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcLiveVideoInfo(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcLiveVideoInfo(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcLiveVideoInfo(payload_index, listen_id);
}


int CameraControlModuleImpl::listenDrcThermalGainMode(const std::string& payload_index, const std::function<void(const ThermalGainModeInfo& info)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcThermalGainMode(payload_index, callback);
}


void CameraControlModuleImpl::cancelListenDrcThermalGainMode(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcThermalGainMode(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraIrLensState(const std::string& payload_index, const std::function<void(const CameraIrLensState& state)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraIrLensState(payload_index, callback);
}

void CameraControlModuleImpl::cancelListenDrcCameraIrLensState(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraIrLensState(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraIrThermometry(const std::string& payload_index, const std::function<void(const CameraIrThermometryInfo& info)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    return payload_ctrl_mgr_->listenDrcCameraIrThermometry(payload_index, callback);
}

void CameraControlModuleImpl::cancelListenDrcCameraIrThermometry(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return;
    }
    payload_ctrl_mgr_->cancelListenDrcCameraIrThermometry(payload_index, listen_id);
}

// ── B4 drc_camera_state_push 拆分 ──
int CameraControlModuleImpl::listenDrcCameraWatermarkSettings(const std::string& payload_index, const std::function<void(const CameraWatermarkSettings&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraWatermarkSettings(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraWatermarkSettings(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraWatermarkSettings(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraStorageInfo(const std::string& payload_index, const std::function<void(const CameraStorageInfo&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraStorageInfo(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraStorageInfo(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraStorageInfo(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraIrPipState(const std::string& payload_index, const std::function<void(const CameraIrPipState&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraIrPipState(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraIrPipState(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraIrPipState(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraIsothermPolygon(const std::string& payload_index, const std::function<void(const CameraIsothermPolygon&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraIsothermPolygon(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraIsothermPolygon(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraIsothermPolygon(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraMediaStorage(const std::string& payload_index, const std::function<void(const CameraMediaStorage&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraMediaStorage(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraMediaStorage(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraMediaStorage(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraAlgorithmDehazeInfo(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraAlgorithmDehazeInfo(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraPreRecordedInfo(const std::string& payload_index, const std::function<void(const CameraPreRecordedInfo&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraPreRecordedInfo(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraPreRecordedInfo(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraPreRecordedInfo(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraCapability(const std::string& payload_index, const std::function<void(const CameraCapability&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraCapability(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraCapability(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraCapability(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraSettableCapability(const std::string& payload_index, const std::function<void(const CameraSettableCapability&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraSettableCapability(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraSettableCapability(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraSettableCapability(payload_index, listen_id);
}

int CameraControlModuleImpl::listenDrcCameraVisibleCapability(const std::string& payload_index, const std::function<void(const CameraVisibleCapability&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraVisibleCapability(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraVisibleCapability(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraVisibleCapability(payload_index, listen_id);
}

// ── B4.1 ──
int CameraControlModuleImpl::listenDrcCameraPhotoInfo(const std::string& payload_index, const std::function<void(const CameraPhotoInfo&)>& callback) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return -1; }
    return payload_ctrl_mgr_->listenDrcCameraPhotoInfo(payload_index, callback);
}
void CameraControlModuleImpl::cancelListenDrcCameraPhotoInfo(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) { LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null"); return; }
    payload_ctrl_mgr_->cancelListenDrcCameraPhotoInfo(payload_index, listen_id);
}


void CameraControlModuleImpl::drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外增益模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_GAIN_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetThermalGainMode(params, callback);
}


// ══════════════════════════════════════════════
// Camera Advanced Parameters (DRC)
// ══════════════════════════════════════════════

void CameraControlModuleImpl::drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机光圈值(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_APERTURE_VALUE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraApertureValue(params, callback);
}

void CameraControlModuleImpl::drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机曝光模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraExposureMode(params, callback);
}

void CameraControlModuleImpl::drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机曝光值(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_VALUE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraExposureValue(params, callback);
}

void CameraControlModuleImpl::drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机对焦模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraFocusMode(params, callback);
}

void CameraControlModuleImpl::drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机对焦值(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_VALUE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraFocusValue(params, callback);
}

void CameraControlModuleImpl::drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机 ISO(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_ISO, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraIso(params, callback);
}

void CameraControlModuleImpl::drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机机械快门(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_MECHANICAL_SHUTTER, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraMechanicalShutter(params, callback);
}

void CameraControlModuleImpl::drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 相机点对焦操作(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_POINT_FOCUS_ACTION, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcCameraPointFocusAction(params, callback);
}

void CameraControlModuleImpl::drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机快门速度(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SHUTTER, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraShutter(params, callback);
}

void CameraControlModuleImpl::drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机水印(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_WATERMARK, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraWatermark(params, callback);
}

void CameraControlModuleImpl::drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机畸变矫正(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DEWARPING, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraDewarping(params, callback);
}

// ══════════════════════════════════════════════
// Screen/Gimbal Advanced (DRC)
// ══════════════════════════════════════════════

void CameraControlModuleImpl::drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机分屏显示(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SCREEN_SPLIT, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraScreenSplit(params, callback);
}

void CameraControlModuleImpl::drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 镜头去雾(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_CLEAN_FOG, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcCleanFog(params, callback);
}

// ══════════════════════════════════════════════
// Thermal/IR (DRC)
// ══════════════════════════════════════════════

void CameraControlModuleImpl::drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置热成像调色板(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_CURRENT_PALETTE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetThermalCurrentPalette(params, callback);
}

void CameraControlModuleImpl::drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置热成像超分辨率(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_SUPER_RESOLUTION_ENABLE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetThermalSuperResolutionEnable(params, callback);
}

void CameraControlModuleImpl::drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外测温模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIrMeteringMode(params, callback);
}

void CameraControlModuleImpl::drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外测温点(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_POINT, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIrMeteringPoint(params, callback);
}

void CameraControlModuleImpl::drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外测温区域(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_AREA, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIrMeteringArea(params, callback);
}

void CameraControlModuleImpl::drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外显示模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_DISPLAY_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIrDisplayMode(params, callback);
}

void CameraControlModuleImpl::drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外增强模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_ENHANCE_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIrEnhanceMode(params, callback);
}

void CameraControlModuleImpl::drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置红外融合比例(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_MIX_RATE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIrMixRate(params, callback);
}

void CameraControlModuleImpl::drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线开关(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ENABLE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIsothermEnable(params, callback);
}

void CameraControlModuleImpl::drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线着色类型(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_COLOR_RENDERING_TYPE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIsothermColorRenderingType(params, callback);
}

void CameraControlModuleImpl::drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线温度上下限(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_LOWER_UPPER_RANGE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIsothermLowerUpperRange(params, callback);
}

void CameraControlModuleImpl::drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置等温线多边形渲染区域(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_RENDERING_POLYGON, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIsothermRenderingPolygon(params, callback);
}

void CameraControlModuleImpl::drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置按框选区域的等温线渲染使能(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ROI_BASED_RENDERING_ENABLE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetIsothermRoiBasedRenderingEnable(params, callback);
}

void CameraControlModuleImpl::drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置近红外补光灯使能(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INFRARED_FILL_LIGHT_ENABLE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetInfraredFillLightEnable(params, callback);
}

void CameraControlModuleImpl::drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置黑白夜视使能(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_NIGHT_VISION_ENABLE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraNightVisionEnable(params, callback);
}

void CameraControlModuleImpl::drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 重置相机设置(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_CAMERA_SETTINGS, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcResetCameraSettings(params, callback);
}

// ══════════════════════════════════════════════
// Other Camera DRC
// ══════════════════════════════════════════════

void CameraControlModuleImpl::drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机降噪等级(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DENOISE_LEVEL_SET, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraDenoiseLevelSet(params, callback);
}

void CameraControlModuleImpl::drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置视频分辨率(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_RESOLUTION, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetVideoResolution(params, callback);
}

void CameraControlModuleImpl::drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置照片格式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_PHOTO_FORMAT, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCameraPhotoFormat(params, callback);
}

void CameraControlModuleImpl::drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置预录制状态(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_STATUS, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetPreRecordedStatus(params, callback);
}

void CameraControlModuleImpl::drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置预录制时长(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_TIME, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetPreRecordedTime(params, callback);
}

void CameraControlModuleImpl::drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置倒计时动作(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_COUNT_DOWN_ACTION, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetCountDownAction(count_down_type, action_type, callback);
}

void CameraControlModuleImpl::drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置辅助画面模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetAssistantVideoMode(mode, callback);
}

void CameraControlModuleImpl::drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置辅助画面开关(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_SWITCH, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetAssistantVideoSwitch(sw, callback);
}

void CameraControlModuleImpl::drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机电子去雾模式(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_MODE, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetAlgorithmDehazeMode(params, callback);
}

void CameraControlModuleImpl::drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 设置相机电子去雾等级(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_LEVEL, state_err, "[CameraControlModuleImpl]");

        callback(state_err);
        return;
    }
    payload_ctrl_mgr_->drcSetAlgorithmDehazeLevel(params, callback);
}

int CameraControlModuleImpl::listenDroneCameraWatermarkSettings(const std::function<void(const DroneCameraWatermarkSettingsPush&)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[CameraControlModuleImpl] payload_ctrl_mgr_ is null");
        return -1;
    }
    WeakDummy(weak_ptr);
    int user_listen_id = drone_camera_watermark_settings_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneCameraWatermarkSettings(
        [this, weak_ptr, callback](const notification_center::DroneCameraWatermarkSettingsPush& msg) -> void {
            WeakDummyReturn(weak_ptr);
            DroneCameraWatermarkSettingsPush res{};
            res.global_enable = msg.global_enable;
            res.drone_type_enable = msg.drone_type_enable;
            res.drone_sn_enable = msg.drone_sn_enable;
            res.datetime_enable = msg.datetime_enable;
            res.gps_enable = msg.gps_enable;
            res.user_custom_string_enable = msg.user_custom_string_enable;
            res.user_custom_string = msg.user_custom_string;
            res.layout = msg.layout;

            LOG_INFO("[CameraControlModuleImpl] ListenDroneCameraWatermarkSettings: global_enable: %lld, layout: %lld",
                (long long)msg.global_enable, (long long)msg.layout);

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[CameraControlModuleImpl] ListenDroneCameraWatermarkSettings error: %d", error_code);
            }
        }, payload_ctrl_mgr_->getDroneSn(), notification_center::NotificationFrequency_OnChanged);

    drone_camera_watermark_settings_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[CameraControlModuleImpl] listenDroneCameraWatermarkSettings user_listen_id: %d, nc_listen_id: %lld",
        user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void CameraControlModuleImpl::cancelListenDroneCameraWatermarkSettings(int listen_id) {
    LOG_INFO("[CameraControlModuleImpl] cancelListenDroneCameraWatermarkSettings listen_id: %d", listen_id);
    auto it = drone_camera_watermark_settings_nc_listen_ids_.find(listen_id);
    if (it != drone_camera_watermark_settings_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        drone_camera_watermark_settings_nc_listen_ids_.erase(it);
    }
}

