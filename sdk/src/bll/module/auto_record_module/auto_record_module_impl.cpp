// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "auto_record_module_impl.h"

#include <base/common/logger.h>
#include "http_interface/http_interface.h"

namespace flight_hub_sdk {

AutoRecordModuleImpl::AutoRecordModuleImpl(int session_id) : session_id_(session_id) {
    LOG_INFO("[AutoRecordModuleImpl] constructor, session_id: %d", session_id_);
}

void AutoRecordModuleImpl::getAutoRecordConfig(
    std::function<void(FlightHubError err, const AutoRecordConfig& config)> callback) {
    LOG_INFO("[AutoRecordModuleImpl] getAutoRecordConfig");
    int sid = session_id_;
    WeakDummy(weak_ptr);
    get_auto_record_config([this, weak_ptr, callback](const GetAutoRecordConfigHttpResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        // data_required=false: 配置不存在时 response 各字段为零值, 这里原样拷贝即可(非错误)。
        AutoRecordConfig config;
        config.id = response.id;
        config.project_id = response.project_id;
        config.created_at = response.created_at;
        config.updated_at = response.updated_at;
        config.auto_cleaning_time = response.auto_cleaning_time;
        config.disabled_auto_clean = response.disabled_auto_clean;
        config.disable_dock_recording = response.disable_dock_recording;
        config.disable_drone_recording = response.disable_drone_recording;
        config.disable_base_station_recording = response.disable_base_station_recording;
        config.disable_rtmp_recording = response.disable_rtmp_recording;
        for (const auto& item : response.recording_items) {
            AutoRecordItem it;
            it.sn = item.sn;
            it.camera_index = item.camera_index;
            it.camera_sn = item.camera_sn;
            it.recording_strategy = item.recording_strategy;
            config.recording_items.push_back(it);
        }
        LOG_INFO("[AutoRecordModuleImpl] getAutoRecordConfig ok id=%lld items=%zu",
            static_cast<long long>(config.id), config.recording_items.size());
        callback(FlightHubError::success(), config);
    }, sid);
}

void AutoRecordModuleImpl::saveAutoRecordConfig(
    const SaveAutoRecordConfigParams& params,
    std::function<void(FlightHubError err)> callback) {
    LOG_INFO("[AutoRecordModuleImpl] saveAutoRecordConfig auto_cleaning_time=%d items=%zu",
        params.auto_cleaning_time, params.recording_items.size());
    int sid = session_id_;
    WeakDummy(weak_ptr);
    SaveAutoRecordConfigRequest request;
    request.auto_cleaning_time = params.auto_cleaning_time;
    request.disable_dock_recording = params.disable_dock_recording;
    request.disable_drone_recording = params.disable_drone_recording;
    request.disable_base_station_recording = params.disable_base_station_recording;
    request.disable_rtmp_recording = params.disable_rtmp_recording;
    for (const auto& item : params.recording_items) {
        AutoRecordItemRequest it;
        it.sn = item.sn;
        it.camera_index = item.camera_index;
        it.camera_sn = item.camera_sn;
        request.recording_items.push_back(it);
    }
    save_auto_record_config(request, [this, weak_ptr, callback](const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        callback(err);
    }, sid);
}

}  // namespace flight_hub_sdk
