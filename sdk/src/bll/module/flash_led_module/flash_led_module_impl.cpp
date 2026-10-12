// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flash_led_module_impl.h"

#include <base/common/logger.h>
#include "http_interface/http_interface.h"

namespace flight_hub_sdk {

FlashLedModuleImpl::FlashLedModuleImpl() {
    LOG_INFO("[FlashLedModuleImpl] constructor");
}

void FlashLedModuleImpl::getFlashLedConfigList(
    std::function<void(FlightHubError err, const FlashLedConfigList& list)> callback) {
    LOG_INFO("[FlashLedModuleImpl] getFlashLedConfigList");
    WeakDummy(weak_ptr);
    get_flash_led_config_list([this, weak_ptr, callback](const FlashLedConfigListHttpResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 获取爆闪灯模板列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLASH_LED_GET_CONFIG_LIST, err, "[FlashLedModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        FlashLedConfigList list;
        for (const auto& item : response.configs) {
            FlashLedConfig cfg;
            cfg.id = item.id;
            cfg.name = item.name;
            for (const auto& seg : item.flash_segments) {
                FlashLedSegment s;
                s.color = seg.color;
                s.count = seg.count;
                s.on = seg.on;
                s.off = seg.off;
                s.interval = seg.interval;
                cfg.flash_segments.push_back(s);
            }
            list.configs.push_back(cfg);
        }
        LOG_INFO("[FlashLedModuleImpl] getFlashLedConfigList size=%zu", list.configs.size());
        callback(FlightHubError::success(), list);
    });
}

void FlashLedModuleImpl::createFlashLedConfig(
    const FlashLedConfigParams& params,
    std::function<void(FlightHubError err, const FlashLedConfig& config)> callback) {
    LOG_INFO("[FlashLedModuleImpl] createFlashLedConfig name=%s", params.name.c_str());
    WeakDummy(weak_ptr);
    CreateFlashLedConfigRequest request;
    request.name = params.name;
    for (const auto& seg : params.flash_segments) {
        FlashLedSegmentHttp s;
        s.color = seg.color;
        s.count = seg.count;
        s.on = seg.on;
        s.off = seg.off;
        s.interval = seg.interval;
        request.flash_segments.push_back(s);
    }
    create_flash_led_config(request, [this, weak_ptr, callback, params](const CreateFlashLedConfigResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 创建爆闪灯模板; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLASH_LED_CREATE_CONFIG, err, "[FlashLedModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        FlashLedConfig cfg;
        cfg.id = response.id;
        cfg.name = params.name;
        cfg.flash_segments = params.flash_segments;
        LOG_INFO("[FlashLedModuleImpl] createFlashLedConfig ok id=%s", cfg.id.c_str());
        callback(FlightHubError::success(), cfg);
    });
}

void FlashLedModuleImpl::updateFlashLedConfig(
    const std::string& config_id,
    const FlashLedConfigParams& params,
    std::function<void(FlightHubError err, const FlashLedConfig& config)> callback) {
    LOG_INFO("[FlashLedModuleImpl] updateFlashLedConfig id=%s", config_id.c_str());
    WeakDummy(weak_ptr);
    UpdateFlashLedConfigRequest request;
    request.name = params.name;
    for (const auto& seg : params.flash_segments) {
        FlashLedSegmentHttp s;
        s.color = seg.color;
        s.count = seg.count;
        s.on = seg.on;
        s.off = seg.off;
        s.interval = seg.interval;
        request.flash_segments.push_back(s);
    }
    update_flash_led_config(config_id, request, [this, weak_ptr, callback, config_id](const UpdateFlashLedConfigResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 更新爆闪灯模板; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLASH_LED_UPDATE_CONFIG, err, "[FlashLedModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        FlashLedConfig cfg;
        cfg.id = response.id.empty() ? config_id : response.id;
        cfg.name = response.name;
        for (const auto& seg : response.flash_segments) {
            FlashLedSegment s;
            s.color = seg.color;
            s.count = seg.count;
            s.on = seg.on;
            s.off = seg.off;
            s.interval = seg.interval;
            cfg.flash_segments.push_back(s);
        }
        LOG_INFO("[FlashLedModuleImpl] updateFlashLedConfig ok id=%s", cfg.id.c_str());
        callback(FlightHubError::success(), cfg);
    });
}

void FlashLedModuleImpl::deleteFlashLedConfig(
    const std::string& config_id,
    std::function<void(FlightHubError err)> callback) {
    LOG_INFO("[FlashLedModuleImpl] deleteFlashLedConfig id=%s", config_id.c_str());
    WeakDummy(weak_ptr);
    delete_flash_led_config(config_id, [this, weak_ptr, callback](const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 删除爆闪灯模板; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLASH_LED_DELETE_CONFIG, err, "[FlashLedModuleImpl]");
        callback(err);
    });
}

}  // namespace flight_hub_sdk
