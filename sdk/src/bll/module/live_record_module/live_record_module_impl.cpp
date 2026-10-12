// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "live_record_module_impl.h"
#include "common/util/async_capture_protect.h"
#include "common/executors/executors.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

LiveRecordModuleImpl::LiveRecordModuleImpl(const LiveInfo& live_info)
    : live_info_(live_info) {
    LOG_INFO("[LiveRecordModuleImpl] constructor, sn: %s", live_info_.device_sn.c_str());
}

LiveRecordModuleImpl::~LiveRecordModuleImpl() {
    record_state_callbacks_.clear();
}

void LiveRecordModuleImpl::startLiveRecord(const std::string& time_zone, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(p);
    start_live_record(live_info_, "", time_zone, [this, p, callback](FlightHubError err) {
        WeakDummyReturn(p);
        drc_sdk::common::Executors::Main()->Post([callback, err]() {
            // 埋点: 开始录制当前直播流; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_LIVE_RECORD_START, err, "[LiveRecordModuleImpl]");
            if (callback) callback(err);
        });
    });
}

void LiveRecordModuleImpl::stopLiveRecord(const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(p);
    stop_live_record(live_info_, "", [this, p, callback](FlightHubError err) {
        WeakDummyReturn(p);
        drc_sdk::common::Executors::Main()->Post([callback, err]() {
            // 埋点: 停止录制当前直播流; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_LIVE_RECORD_STOP, err, "[LiveRecordModuleImpl]");
            if (callback) callback(err);
        });
    });
}

void LiveRecordModuleImpl::getDeviceLiveRecords(const std::string& device_sn, const std::function<void(FlightHubError err, const std::vector<LiveRecordItem>& records)>& callback) {
    WeakDummy(p);
    get_device_live_records(device_sn, [this, p, callback](const std::vector<LiveRecordItem>& records, FlightHubError err) {
        WeakDummyReturn(p);
        drc_sdk::common::Executors::Main()->Post([callback, records, err]() {
            // 埋点: 按设备 SN 查询录制记录列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_LIVE_RECORD_GET_DEVICE_LIVE_RECORDS, err, "[LiveRecordModuleImpl]");
            if (callback) callback(err, records);
        });
    });
}

int LiveRecordModuleImpl::listenRecordState(const std::function<void(const LiveRecordStateInfo& info)>& callback) {
    return record_state_callbacks_.addCallback(callback);
}

void LiveRecordModuleImpl::cancelListenRecordState(int listen_id) {
    record_state_callbacks_.removeCallback(listen_id);
}

void LiveRecordModuleImpl::notifyRecordState(const LiveRecordStateInfo& info) {
    record_state_callbacks_.notifyCallback(info);
}

}  // namespace flight_hub_sdk
