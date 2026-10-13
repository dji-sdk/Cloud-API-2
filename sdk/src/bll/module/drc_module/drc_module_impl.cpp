// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_module_impl.h"

#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

DrcModuleImpl::DrcModuleImpl(std::shared_ptr<DrcLinkMgr> drc_link_mgr, const std::string& log_tag)
    : drc_link_mgr_(std::move(drc_link_mgr)), log_tag_(log_tag) {
    LOG_INFO("[DrcModuleImpl] constructor, log_tag: %s", log_tag_.c_str());
    registerDrcLinkStateListener();
}

DrcModuleImpl::~DrcModuleImpl() {
    LOG_INFO("[DrcModuleImpl] destructor, log_tag: %s", log_tag_.c_str());
    if (ctrl_link_status_listen_id_ != -1 && drc_link_mgr_) {
        drc_link_mgr_->cancelListenDrcLinkState(ctrl_link_status_listen_id_);
    }
}

void DrcModuleImpl::registerDrcLinkStateListener() {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] registerDrcLinkStateListener: drc_link_mgr_ is null");
        return;
    }
    WeakDummy(p);
    ctrl_link_status_listen_id_ = drc_link_mgr_->listenDrcLinkState([this, p](const DroneLinkState& status) -> void {
        WeakDummyReturn(p);
        drc_sdk::common::Executors::Main()->Post([this, p, status]() {
            WeakDummyReturn(p);
            LOG_INFO("[DrcModuleImpl-%s] control link status changed: %d (0-IDLE, 1-CONNECTING, 2-CONNECTED, 3-DISCONNECTED, 0xFFFF-UNKNOWN)",
                log_tag_.c_str(), static_cast<int>(status));
            LinkStatus link_status;
            link_status.ctrl_link_status = static_cast<int>(status);
            link_status_callbacks_.notifyCallbackIfChanged(link_status);
        });
    });
}

int DrcModuleImpl::listenLinkStatus(const std::function<void(const LinkStatus& status)>& callback) {
    return link_status_callbacks_.addCallback(callback);
}

void DrcModuleImpl::cancelListenLinkStatus(int listen_id) {
    link_status_callbacks_.removeCallback(listen_id);
}

void DrcModuleImpl::connectDrcLink(const std::function<void(FlightHubSdkErrorCode)>& callback) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] connectDrcLink: drc_link_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 连接 DRC 链路(DRC 链路管理器未初始化, 未建链); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_CONNECT_LINK, state_err, "[DrcModuleImpl]");

        if (callback) {
            callback(state_err.error_code);
        }
        return;
    }
    drc_link_mgr_->connectDrcLink(callback);
}

void DrcModuleImpl::disconnectDrcLink(const std::function<void(FlightHubSdkErrorCode)>& callback) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] disconnectDrcLink: drc_link_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 断开 DRC 链路(DRC 链路管理器未初始化, 未执行断链); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_DISCONNECT_LINK, state_err, "[DrcModuleImpl]");

        if (callback) {
            callback(state_err.error_code);
        }
        return;
    }
    drc_link_mgr_->onUserDisconnect();
    drc_link_mgr_->disconnectDrcLink(callback);
}

// ── DRC Link Auxiliary ──

void DrcModuleImpl::drcRtcQualityTest(const RtcQualityTestParams& params, const std::function<void(FlightHubError)>& callback) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] drcRtcQualityTest: drc_link_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: RTC 网络质量测试(DRC 链路管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_RTC_QUALITY_TEST, state_err, "[DrcModuleImpl]");

        if (callback) {
            callback(state_err);
        }
        return;
    }
    drc_link_mgr_->drcRtcQualityTest(params, callback);
}

int DrcModuleImpl::listenDrcDelayInfo(const std::function<void(const DrcDelayInfo&)>& callback) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] listenDrcDelayInfo: drc_link_mgr_ is not initialized");
        return -1;
    }
    return drc_link_mgr_->listenDrcDelayInfo(callback);
}

void DrcModuleImpl::cancelListenDrcDelayInfo(int listen_id) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] cancelListenDrcDelayInfo: drc_link_mgr_ is not initialized");
        return;
    }
    drc_link_mgr_->cancelListenDrcDelayInfo(listen_id);
}

int DrcModuleImpl::listenDrcCountDownInfo(const std::function<void(const DrcCountDownInfo&)>& callback) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] listenDrcCountDownInfo: drc_link_mgr_ is not initialized");
        return -1;
    }
    return drc_link_mgr_->listenDrcCountDownInfo(callback);
}

void DrcModuleImpl::cancelListenDrcCountDownInfo(int listen_id) {
    if (!drc_link_mgr_) {
        LOG_ERROR("[DrcModuleImpl] cancelListenDrcCountDownInfo: drc_link_mgr_ is not initialized");
        return;
    }
    drc_link_mgr_->cancelListenDrcCountDownInfo(listen_id);
}
