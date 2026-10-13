// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "safety_service_module_impl.h"
#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <base/common/logger.h>
#include <vector>

namespace flight_hub_sdk {

SafetyServiceModuleImpl::SafetyServiceModuleImpl()
    :
      fly_safe_mgr_() {
    LOG_INFO("[SafetyServiceModuleImpl] constructor");
}

SafetyServiceModuleImpl::~SafetyServiceModuleImpl() {}


void SafetyServiceModuleImpl::searchSignalMaps(
    const std::vector<std::string> &device_sns, const std::vector<std::string> &data_types,
    const std::function<void(FlightHubError err, const SignalMapsResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("searchSignalMaps(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Safety Services;
    // [安全服务]: 信号地图, signal-maps/search, 信号地图查询;
    SearchSignalMapsRequest request;
    request.device_sns = device_sns;
    request.data_types = data_types;

    // Send request;
    search_signal_maps(request, [weak_ptr, callback](const SignalMapsResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取信号地图; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_SEARCH_SIGNAL_MAPS, err, "[SafetyServiceModuleImpl]");
            // 检查 err;
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("searchSignalMaps(): err: %d", (int)err.error_code);
                callback(err, {});

                return;
            }

            // When succeed, transfer the response to the callback;
            SignalMapsResults results;
            results.lists.reserve(response.list.size());
            
            for (const auto &device_signal_info : response.list) {
                DeviceSignalItem device_signal_item;
                device_signal_item.grids.reserve(device_signal_info.grid.size());

                device_signal_item.device_sn = device_signal_info.device_sn;
                for (const auto &grid_signal_info : device_signal_info.grid) {
                    GridSignalItem grid_signal_item;
                    grid_signal_item.h3_index = grid_signal_info.h3_index;
                    grid_signal_item.sdr_avg = grid_signal_info.sdr_avg;
                    grid_signal_item.sdr_max = grid_signal_info.sdr_max;
                    grid_signal_item.sdr_min = grid_signal_info.sdr_min;
                    grid_signal_item.signal_4g_avg = grid_signal_info.signal_4g_avg;
                    grid_signal_item.signal_4g_max = grid_signal_info.signal_4g_max;
                    grid_signal_item.signal_4g_min = grid_signal_info.signal_4g_min;

                    // 一般来说, 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
                    device_signal_item.grids.emplace_back(std::move(grid_signal_item));
                }

                // 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
                results.lists.emplace_back(std::move(device_signal_item));
            }

            // When succeed, transfer the response to the callback;
            callback(err, results);
        });
    });
}


void SafetyServiceModuleImpl::createCustomizedFlightAreas(
    const CreateCustomizedFlightAreasParams &request_params,
    const std::function<void(FlightHubError err, const CreateFlightAreasResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("createCustomizedFlightAreas(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    CreateCustomizedFlightAreasRequest request;
    request.name = request_params.name;
    request.type = request_params.type;
    request.created_by = request_params.created_by;

    // Flight Areas Contents, content 飞行区内容;
    request.content.type = request_params.content.type;

    request.content.properties.color = request_params.content.properties.color;
    request.content.properties.width = request_params.content.properties.width;
    request.content.properties.clampToGround = request_params.content.properties.clampToGround;
    
    request.content.geometry.type = request_params.content.geometry.type;
    request.content.geometry.coordinates = request_params.content.geometry.coordinates;
    request.content.geometry.center = request_params.content.geometry.center;
    request.content.geometry.radius = request_params.content.geometry.radius;

    // Send request;
    create_customized_flight_areas(
        request,
        [weak_ptr, callback](const CreateFlightAreasResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 创建自定义飞行区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_CREATE_CUSTOMIZED_FLIGHT_AREAS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("createCustomizedFlightAreas(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                CreateFlightAreasResults results;
                // 根据 resp 组装创建飞行区域返回值;
                results.id = response.id;
                results.name = response.name;

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}


void SafetyServiceModuleImpl::getCustomizedFlightAreas(
    const GetCustomizedFlightAreasParams &request_params,
    const std::function<void(FlightHubError err, const GetFlightAreasResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getCustomizedFlightAreas(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    get_customized_flight_areas(
        request_params.name,
        request_params.type,
        request_params.status,
        request_params.ids,
        request_params.page_index,
        request_params.page_size,
        [weak_ptr, callback](const GetFlightAreasResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取自定义飞行区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_CUSTOMIZED_FLIGHT_AREAS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getCustomizedFlightAreas(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // When succeed, transfer the response to the callback;
                GetFlightAreasResults results;
                // 根据 resp 组装获取飞行区域返回值;
                // #1 填充分页信息;
                results.pagination.page = response.pagination.page;
                results.pagination.page_size = response.pagination.page_size;
                results.pagination.total = response.pagination.total;

                // #2 填充飞行区域信息;
                results.list.reserve(response.list.size());

                for (const auto& item : response.list) {
                    FlightAreaInfoItem flight_area_info_item;
                    flight_area_info_item.id = item.id;
                    flight_area_info_item.name = item.name;
                    flight_area_info_item.status = item.status;
                    flight_area_info_item.type = item.type;

                    // contents;
                    flight_area_info_item.content.type = item.content.type;

                    // contents: properties;
                    flight_area_info_item.content.properties.color = item.content.properties.color;
                    flight_area_info_item.content.properties.width = item.content.properties.width;
                    flight_area_info_item.content.properties.clampToGround = item.content.properties.clampToGround;

                    // contents: geometry;
                    flight_area_info_item.content.geometry.type = item.content.geometry.type;
                    flight_area_info_item.content.geometry.coordinates = item.content.geometry.coordinates;
                    flight_area_info_item.content.geometry.center = item.content.geometry.center;
                    flight_area_info_item.content.geometry.radius = item.content.geometry.radius;

                    flight_area_info_item.created_time = item.created_time;
                    flight_area_info_item.created_by = item.created_by;
                    flight_area_info_item.updated_time = item.updated_time;
                    flight_area_info_item.updated_by = item.updated_by;

                    // 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
                    results.list.emplace_back(std::move(flight_area_info_item));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}


// 修改 [自定义飞行区];
void SafetyServiceModuleImpl::updateCustomizedFlightAreas(
    const UpdateCustomizedFlightAreasParams &request_params,
    const std::function<void(FlightHubError err, const UpdateFlightAreasResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("updateCustomizedFlightAreas(): callback is NULL !");
        return;
    }

    // [必须参数]检查;
    if (request_params.flight_area_id.empty()) {
        LOG_ERROR("updateCustomizedFlightAreas(): flight_area_id is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 更新自定义飞行区域(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPDATE_CUSTOMIZED_FLIGHT_AREAS, param_err, "[SafetyServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    UpdateCustomizedFlightAreasRequest request;
    request.name = request_params.name;
    request.update_by = request_params.update_by;

    request.content.type = request_params.content.type;

    // properties;
    request.content.properties.color = request_params.content.properties.color;
    request.content.properties.width = request_params.content.properties.width;
    request.content.properties.clampToGround = request_params.content.properties.clampToGround;

    // geometry;
    request.content.geometry.type = request_params.content.geometry.type;
    request.content.geometry.coordinates = request_params.content.geometry.coordinates;
    request.content.geometry.center = request_params.content.geometry.center;
    request.content.geometry.radius = request_params.content.geometry.radius;


    // Send request;
    update_customized_flight_areas(
        request_params.flight_area_id,
        request,
        [weak_ptr, callback](const UpdateFlightAreasResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 更新自定义飞行区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPDATE_CUSTOMIZED_FLIGHT_AREAS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("updateCustomizedFlightAreas(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // When succeed, transfer the response to the callback;
                UpdateFlightAreasResults results;
                results.id = response.id;

                results.updated_area.id = response.updated_area.id;
                results.updated_area.name = response.updated_area.name;
                results.updated_area.status = response.updated_area.status;
                results.updated_area.type = response.updated_area.type;

                // contents;
                results.updated_area.content.type = response.updated_area.content.type;
                // contents: properties;
                results.updated_area.content.properties.color  = response.updated_area.content.properties.color;
                results.updated_area.content.properties.width  = response.updated_area.content.properties.width;
                results.updated_area.content.properties.clampToGround = response.updated_area.content.properties.clampToGround;
                // contents: geometry;
                results.updated_area.content.geometry.type = response.updated_area.content.geometry.type;
                results.updated_area.content.geometry.coordinates = response.updated_area.content.geometry.coordinates;
                results.updated_area.content.geometry.center = response.updated_area.content.geometry.center;
                results.updated_area.content.geometry.radius = response.updated_area.content.geometry.radius;

                results.updated_area.created_time = response.updated_area.created_time;
                results.updated_area.created_by = response.updated_area.created_by;
                results.updated_area.updated_time = response.updated_area.updated_time;
                results.updated_area.updated_by = response.updated_area.updated_by;

                callback(err, results);
            });
        }
    );
}


// 删除 [自定义飞行区];
void SafetyServiceModuleImpl::deleteCustomizedFlightAreas(
    const std::string &flight_area_id,
    const std::function<void(FlightHubError err)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("deleteCustomizedFlightAreas(): callback is NULL !");
        return;
    }

    // [必须参数] 检测;
    if (flight_area_id.empty()) {
        LOG_ERROR("deleteCustomizedFlightAreas(): param(flight_area_id) is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 删除自定义飞行区域(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_DELETE_CUSTOMIZED_FLIGHT_AREAS, param_err, "[SafetyServiceModuleImpl]");
        callback(param_err);
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    delete_customized_flight_areas(
        flight_area_id,
        [weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 删除自定义飞行区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_DELETE_CUSTOMIZED_FLIGHT_AREAS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("deleteCustomizedFlightAreas(): err: %d", (int)err.error_code);
                }

                // 统一返回 err;
                callback(err);
            });
        }
    );
}


// 同步 [自定义飞行区];
void SafetyServiceModuleImpl::syncCustomizedFlightAreas(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
    const std::function<void(FlightHubError err, const SyncFlightAreasResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("syncCustomizedFlightAreas(): callback is NULL !");
        return;
    }

    // [必须参数] 检测;
    if (device_sns.empty()) {
        LOG_ERROR("syncCustomizedFlightAreas(): param(device_sns) is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 同步自定义飞行区域(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_SYNC_CUSTOMIZED_FLIGHT_AREAS, param_err, "[SafetyServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);


    // Send request;
    sync_customized_flight_areas(
        device_sns,
        [weak_ptr, callback](const SyncFlightAreasResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 同步自定义飞行区域; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_SYNC_CUSTOMIZED_FLIGHT_AREAS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("syncCustomizedFlightAreas(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                SyncFlightAreasResults syncs;
                syncs.results.reserve(response.results.size());

                for (const auto &item : response.results) {
                    DeviceSyncResults item_result;
                    item_result.device_sn = item.device_sn;
                    item_result.code = item.code;

                    syncs.results.emplace_back(std::move(item_result));
                }

                // When succeed, transfer the response to the callback;
                callback(err, syncs);
            });
        }
    );
}


// 获取 [自定义飞行区] 同步状态;
void SafetyServiceModuleImpl::getFlightAreasSyncStatus(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
    const std::function<void(FlightHubError err, const GetAreasSyncStatusResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getFlightAreasSyncStatus(): callback is NULL !");
        return;
    }

    // [必须参数] 检测;
    if (device_sns.empty()) {
        LOG_ERROR("getFlightAreasSyncStatus(): param(device_sns) is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取自定义飞行区域同步状态(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_FLIGHT_AREAS_SYNC_STATUS, param_err, "[SafetyServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    get_flight_areas_sync_status(
        device_sns,
        [weak_ptr, callback](const GetAreasSyncStatusResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取自定义飞行区域同步状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_FLIGHT_AREAS_SYNC_STATUS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getFlightAreasSyncStatus(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                GetAreasSyncStatusResults results;
                results.list.reserve(response.list.size());

                for (const auto &item : response.list) {
                    DeviceSyncStatusResults item_result;
                    item_result.sn = item.sn;
                    item_result.status = item.status;
                    item_result.reason = item.reason;

                    results.list.emplace_back(std::move(item_result));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}


// [地形障碍物];
// 查询离线地图模型信息;
void SafetyServiceModuleImpl::getOfflineMapsModelInfo(
    const std::function<void(FlightHubError err, const GetOfflineMapsModelInfoResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getOfflineMapsModelInfo(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);


    // Send request;
    get_offline_maps_model_info(
        [weak_ptr, callback](const GetOfflineMapsModelInfoResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 查询离线地图模型信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_OFFLINE_MAPS_MODEL_INFO, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getOfflineMapsModelInfo(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                GetOfflineMapsModelInfoResults results;
                results.offline_map.models.reserve(response.offline_map.models.size());

                results.disable = response.disable;

                results.offline_map.id = response.offline_map.id;
                results.offline_map.updated_time = response.offline_map.updated_time;
                results.offline_map.percent = response.offline_map.percent;
                results.offline_map.status = response.offline_map.status;
                results.offline_map.result = response.offline_map.result;

                for (const auto& model_item: response.offline_map.models) {
                    OfflineMapsModelInfoData model;
                    model.uuid = model_item.uuid;

                    // 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
                    // 结构体中包含非 trivially-movable 的自定义类型时，需要手写 move 构造函数;
                    results.offline_map.models.emplace_back(std::move(model));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}


// [地形障碍物];
// 创建/更新离线地图模型;
void SafetyServiceModuleImpl::updateOfflineMapsModelInfo(
    const UpdateOfflineMapsParams &request_params,
    const std::function<void(FlightHubError err)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("updateOfflineMapsModelInfo(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    UpdateOfflineMapsRequest request;

    // 消除重复逻辑;
    auto fill_model_items = [](const auto& src_items, auto& dst_items) {
        dst_items.reserve(src_items.size());
        for (const auto& item : src_items) {
            ModelItemInfo data;
            data.uuid = item.uuid;
            data.object_key = item.object_key;
            data.file_type = item.file_type;

            // 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
            dst_items.emplace_back(std::move(data));
        }
    };

    // 填充 request 数据;
    fill_model_items(request_params.model_3d_items, request.model_3d_items);
    fill_model_items(request_params.model_2d_items, request.model_2d_items);

    // Send request;
    update_offline_maps_model_info(
        request,
        [weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 创建/更新离线地图模型; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPDATE_OFFLINE_MAPS_MODEL_INFO, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("updateOfflineMapsModelInfo(): err: %d", (int)err.error_code);
                }

                // 统一返回 err;
                callback(err);
            });
        }
    );
}


// [地形障碍物];
// 取消离线地图生成;
void SafetyServiceModuleImpl::cancelOfflineMapsModelInfo(
    const std::function<void(FlightHubError err)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("cancelOfflineMapsModelInfo(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    cancel_offline_maps_model_info(
        [weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 取消离线地图生成; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_CANCEL_OFFLINE_MAPS_MODEL_INFO, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("cancelOfflineMapsModelInfo(): err: %d", (int)err.error_code);
                }

                // 统一返回 err;
                callback(err);
            });
        }
    );
}


// [地形障碍物];
// 修改离线地图(开启/关闭);
void SafetyServiceModuleImpl::setOfflineMaps(
    const SetOfflineMapsParams &request_params,
    const std::function<void(FlightHubError err)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("setOfflineMaps(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    SetOfflineMapsRequest request;
    request.offline_map_enable = request_params.offline_map_enable;

    // Send request;
    set_offline_maps(
        request,
        [weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 修改离线地图（开启/关闭）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_SET_OFFLINE_MAPS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("setOfflineMaps(): err: %d", (int)err.error_code);
                }

                // 统一返回 err;
                callback(err);
            });
        }
    );
}


// [地形障碍物];
// 离线地图同步;
void SafetyServiceModuleImpl::syncOfflineMaps(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
    const std::function<void(FlightHubError err, const OfflineMapsSyncResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("syncOfflineMaps(): callback is NULL !");
        return;
    }

    // [必须参数] 检查;
    if (device_sns.empty()) {
        LOG_ERROR("syncOfflineMaps(): param(device_sns) is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 离线地图同步(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_SYNC_OFFLINE_MAPS, param_err, "[SafetyServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    sync_offline_maps(
        device_sns,
        [weak_ptr, callback](const OfflineMapsSyncResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 离线地图同步; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_SYNC_OFFLINE_MAPS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("syncOfflineMaps(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // 组装 results;
                OfflineMapsSyncResults syncs;
                syncs.results.reserve(response.results.size());
                
                for (const auto &item : response.results) {
                    DeviceSyncResults result;
                    result.device_sn = item.device_sn;
                    result.code = item.code;

                    // 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
                    // 结构体中包含非 trivially-movable 的自定义类型时，需要手写 move 构造函数;
                    syncs.results.emplace_back(std::move(result));
                }

                // When succeed, transfer the response to the callback;
                callback(err, syncs);
            });
        }
    );
}


// [地形障碍物];
// 获取设备同步状态;
void SafetyServiceModuleImpl::getOfflineMapsSyncStatus(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN;
    const std::function<void(FlightHubError err, const GetOfflineMapsSyncStatusResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getOfflineMapsSyncStatus(): callback is NULL !");
        return;
    }

    // [必须参数] 检查;
    if (device_sns.empty()) {
        LOG_ERROR("getOfflineMapsSyncStatus(): param(device_sns) is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取设备同步状态(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_OFFLINE_MAPS_SYNC_STATUS, param_err, "[SafetyServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    get_offline_maps_sync_status(
        device_sns,
        [weak_ptr, callback](const GetOfflineMapsSyncStatusResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取设备同步状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_OFFLINE_MAPS_SYNC_STATUS, err, "[SafetyServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getOfflineMapsSyncStatus(): err: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // 组装 results;
                GetOfflineMapsSyncStatusResults results;
                results.list.reserve(response.list.size());

                for (const auto &item : response.list) {
                    DeviceSyncStatusResults item_result;
                    item_result.sn = item.sn;
                    item_result.status = item.status;
                    item_result.reason = item.reason;

                    results.list.emplace_back(std::move(item_result));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}

void SafetyServiceModuleImpl::getUnlockLicenseList(const QueryDeviceUnlockLicenseListParam& param, std::function<void(FlightHubError err, QueryDeviceUnlockLicenseListResult result)> callback) {
    fly_safe_mgr_.getUnlockLicenseList(param, std::move(callback));
}

void SafetyServiceModuleImpl::switchUnlockLicense(const SwitchUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) {
    fly_safe_mgr_.switchUnlockLicense(param, std::move(callback));
}

void SafetyServiceModuleImpl::checkUnlockLicense(const CheckUnlockLicenseParam& param, std::function<void(FlightHubError err, CheckUnlockLicenseResult result)> callback) {
    fly_safe_mgr_.checkUnlockLicense(param, std::move(callback));
}

void SafetyServiceModuleImpl::submitUnlockLicense(const ImportUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) {
    fly_safe_mgr_.submitUnlockLicense(param, std::move(callback));
}

void SafetyServiceModuleImpl::clearDeviceUnlockLicense(const std::string& device_sn, std::function<void(FlightHubError err)> callback) {
    fly_safe_mgr_.clearDeviceUnlockLicense(device_sn, std::move(callback));
}

void SafetyServiceModuleImpl::uploadUnlockLicenseFile(const std::string& file_path, std::function<void(FlightHubError err, const UploadUnlockLicenseFileResult& result)> callback) {
    fly_safe_mgr_.uploadUnlockLicenseFile(file_path, std::move(callback));
}

int SafetyServiceModuleImpl::listenDroneOfflineMapEnable(
    const std::string& drone_sn,
    const std::function<void(const DroneOfflineMapEnable& msg)>& callback) {
    WeakDummy(weak_ptr);
    int user_listen_id = offline_map_enable_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneOfflineMapEnable(
        [this, weak_ptr, callback](const notification_center::DroneOfflineMapEnable& msg) -> void {
            WeakDummyReturn(weak_ptr);
            DroneOfflineMapEnable res{};
            res.offline_map_enable = msg.offline_map_enable;
            LOG_INFO("[SafetyServiceModuleImpl] ListenDroneOfflineMapEnable: offline_map_enable: %d", msg.offline_map_enable);
            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[SafetyServiceModuleImpl] ListenDroneOfflineMapEnable, error: %d", error_code);
            }
        }, drone_sn, notification_center::NotificationFrequency_OnChanged);

    offline_map_enable_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[SafetyServiceModuleImpl] listenDroneOfflineMapEnable listen_id: %d, nc_listen_id: %lld", user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void SafetyServiceModuleImpl::cancelListenDroneOfflineMapEnable(int listen_id) {
    LOG_INFO("[SafetyServiceModuleImpl] cancelListenDroneOfflineMapEnable listen_id: %d", listen_id);
    auto it = offline_map_enable_nc_listen_ids_.find(listen_id);
    if (it != offline_map_enable_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        offline_map_enable_nc_listen_ids_.erase(it);
    }
}

}  // namespace flight_hub_sdk