// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "wayline_manage_module_impl.h"
#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include "common/executors/executors.h"
#include <BusinessManager.h>

using namespace notification_center;

namespace flight_hub_sdk {

WaylineManageModuleImpl::WaylineManageModuleImpl()
    {
    LOG_INFO("[WaylineManageModuleImpl] constructor");
}

void WaylineManageModuleImpl::getWaylineList(const GetWaylineListParam& param, std::function<void(FlightHubError err, GetWaylineListResult result)> callback) {
    WeakDummy(weak_ptr);
    get_wayline_list(param, [this, weak_ptr, callback](const GetWaylineListResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取航线列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_GET_LIST, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getWaylineList: err_code: %d", (int)err.error_code);
                }
                GetWaylineListResult result;
                result.pagination.page = response.pagination.page;
                result.pagination.page_size = response.pagination.page_size;
                result.pagination.total = response.pagination.total;
                for (const auto& item : response.list) {
                    WaylineListItemResult item_result;
                    item_result.wayline_uuid = item.wayline_uuid;
                    item_result.wayline_types = item.wayline_types;
                    item_result.wayline_name = item.wayline_name;
                    item_result.size = item.size;
                    item_result.payload_types = item.payload_types;
                    item_result.payload_types_with_index = item.payload_types_with_index;
                    item_result.drone_type = item.drone_type;
                    item_result.updated_at = item.updated_at;
                    item_result.with_megaphone = item.with_megaphone;
                    item_result.with_search_light = item.with_search_light;
                    item_result.is_ai_wayline = item.is_ai_wayline;
                    result.list.push_back(item_result);
                }
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::getWaylineDetail(const std::string& wayline_uuid, std::function<void(FlightHubError err, GetWaylineDetailResult result)> callback) {
    WeakDummy(weak_ptr);
    get_wayline_detail(wayline_uuid, [this, weak_ptr, callback](const GetWaylineDetailResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取航线详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_GET_DETAIL, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getWaylineDetail: err_code: %d", (int)err.error_code);
                }
                GetWaylineDetailResult result;
                result.wayline_uuid         = response.wayline_uuid;
                result.md5                  = response.md5;
                result.wayline_name         = response.wayline_name;
                result.kmz_object_key       = response.kmz_object_key;
                result.url                  = response.url;
                result.suffix               = response.suffix;
                result.deleted_at           = response.deleted_at;
                result.updated_at           = response.updated_at;
                result.kmz_without_res_url  = response.kmz_without_res_url;
                result.target_area_file_url = response.target_area_file_url;
                result.business_file_url    = response.business_file_url;
                result.kmz_data.wayline_type                            = response.kmz_data.wayline_type;
                result.kmz_data.wayline_point_count                     = response.kmz_data.wayline_point_count;
                result.kmz_data.exit_on_rc_lost_type                    = response.kmz_data.exit_on_rc_lost_type;
                result.kmz_data.exit_on_rc_lost_behavior                = response.kmz_data.exit_on_rc_lost_behavior;
                result.kmz_data.start_record_action_count               = response.kmz_data.start_record_action_count;
                result.kmz_data.stop_record_action_count                = response.kmz_data.stop_record_action_count;
                result.kmz_data.ortho_camera_overlap_h                  = response.kmz_data.ortho_camera_overlap_h;
                result.kmz_data.ortho_camera_overlap_w                  = response.kmz_data.ortho_camera_overlap_w;
                result.kmz_data.inclined_camera_overlap_h               = response.kmz_data.inclined_camera_overlap_h;
                result.kmz_data.inclined_camera_overlap_w               = response.kmz_data.inclined_camera_overlap_w;
                result.kmz_data.supplement_prism_model_count            = response.kmz_data.supplement_prism_model_count;
                result.kmz_data.estimated_photo_count                   = response.kmz_data.estimated_photo_count;
                result.kmz_data.duration                                = response.kmz_data.duration;
                result.kmz_data.distance                                = response.kmz_data.distance;
                result.kmz_data.survey_area                             = response.kmz_data.survey_area;
                result.kmz_data.start_latitude                          = response.kmz_data.start_latitude;
                result.kmz_data.start_longitude                         = response.kmz_data.start_longitude;
                result.kmz_data.start_altitude                          = response.kmz_data.start_altitude;
                result.kmz_data.margin_distance                         = response.kmz_data.margin_distance;
                result.kmz_data.ortho_gsd                               = response.kmz_data.ortho_gsd;
                result.kmz_data.incline_gsd                             = response.kmz_data.incline_gsd;
                result.kmz_data.is_wayline_validity_check               = response.kmz_data.is_wayline_validity_check;
                result.kmz_data.has_oriented_action                     = response.kmz_data.has_oriented_action;
                result.kmz_data.has_accurate_action                     = response.kmz_data.has_accurate_action;
                result.kmz_data.is_surface_fly_on                       = response.kmz_data.is_surface_fly_on;
                result.kmz_data.is_elevation_optimization_on            = response.kmz_data.is_elevation_optimization_on;
                result.kmz_data.is_smart_oblique_on                     = response.kmz_data.is_smart_oblique_on;
                result.kmz_data.is_mapping_strip_use_template_altitude  = response.kmz_data.is_mapping_strip_use_template_altitude;
                result.kmz_data.is_with_megaphone                       = response.kmz_data.is_with_megaphone;
                result.kmz_data.is_with_search_light                    = response.kmz_data.is_with_search_light;
                result.kmz_data.is_ai_wayline                           = response.kmz_data.is_ai_wayline;
                result.kmz_data.drone_type                              = response.kmz_data.drone_type;
                result.kmz_data.target_detect_area_file_path            = response.kmz_data.target_detect_area_file_path;
                result.kmz_data.wayline_validity_check_codes            = response.kmz_data.wayline_validity_check_codes;
                result.kmz_data.elevation_file_names                    = response.kmz_data.elevation_file_names;
                result.kmz_data.payload_types                           = response.kmz_data.payload_types;
                result.kmz_data.payload_types_with_index                = response.kmz_data.payload_types_with_index;
                result.kmz_data.accurate_action_uuids                   = response.kmz_data.accurate_action_uuids;
                result.kmz_data.action_uuids                            = response.kmz_data.action_uuids;
                result.kmz_data.res_file_md5s                           = response.kmz_data.res_file_md5s;
                result.kmz_data.audio_file_md5s                         = response.kmz_data.audio_file_md5s;
                result.kmz_data.target_detect_action_uuids              = response.kmz_data.target_detect_action_uuids;
                result.kmz_data.ai_audio_file_md5s                      = response.kmz_data.ai_audio_file_md5s;
                result.kmz_data.action_uuid_to_md5_map                  = response.kmz_data.action_uuid_to_md5_map;
                result.kmz_data.audio_action_uuid_to_file_path_map      = response.kmz_data.audio_action_uuid_to_file_path_map;
                result.kmz_data.ai_audio_action_uuid_to_file_path_map   = response.kmz_data.ai_audio_action_uuid_to_file_path_map;
                for (const auto& p : response.kmz_data.survey_area_scope.points) {
                    WaylineDetailSurveyAreaScopePoint point;
                    point.lng = p.lng;
                    point.lat = p.lat;
                    point.alt = p.alt;
                    result.kmz_data.survey_area_scope.points.push_back(point);
                }
                result.ext_data.kmz_without_res_object_key   = response.ext_data.kmz_without_res_object_key;
                result.ext_data.kmz_without_res_md5          = response.ext_data.kmz_without_res_md5;
                result.ext_data.target_area_file_object_key  = response.ext_data.target_area_file_object_key;
                result.ext_data.business_file_object_key     = response.ext_data.business_file_object_key;
                result.ext_data.model_file_object_keys       = response.ext_data.model_file_object_keys;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::batchDeleteWaylines(const std::vector<std::string>& wayline_uuids,
    std::function<void(FlightHubError err, BatchDeleteWaylinesResult result)> callback) {
    WeakDummy(weak_ptr);
    post_batch_delete_waylines(wayline_uuids, [this, weak_ptr, callback](const BatchDeleteWaylinesResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 批量删除航线; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_BATCH_DELETE_WAYLINES, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("batchDeleteWaylines: err_code: %d, server_code: %d, server_message: %s", (int)err.error_code,
                        response.server_code, response.server_message.c_str());
                }
                BatchDeleteWaylinesResult result;
                result.server_code = response.server_code;
                result.server_message = response.server_message;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::updateWayline(const std::string& wayline_uuid, const std::string& wayline_name, std::function<void(FlightHubError err, UpdateWaylineResult result)> callback) {
    WeakDummy(weak_ptr);
    post_update_wayline(wayline_uuid, wayline_name, [this, weak_ptr, callback](const UpdateWaylineResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 更新航线; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_UPDATE, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("updateWayline: err_code: %d", (int)err.error_code);
                }
                UpdateWaylineResult result;
                result.status = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::submitWaylineInfo(const SubmitWaylineInfoParam& param, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    post_import_wayline_callback(param, [this, weak_ptr, callback](const SubmitWaylineInfoResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 提交航线文件信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_SUBMIT_INFO, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("submitWaylineInfo: err_code: %d", (int)err.error_code);
                }
                if (callback) {
                    callback(err);
                }
            });
        });
}

void WaylineManageModuleImpl::batchDownloadWaylines(const BatchDownloadWaylinesParam& param, std::function<void(FlightHubError err, BatchDownloadWaylinesResult result)> callback) {
    WeakDummy(weak_ptr);
    post_batch_download_waylines(param, [this, weak_ptr, callback](const BatchDownloadWaylinesResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 批量下载航线文件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_BATCH_DOWNLOAD_WAYLINES, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("batchDownloadWaylines: err_code: %d", (int)err.error_code);
                }
                BatchDownloadWaylinesResult result;
                for (const auto& item : response.list) {
                    BatchDownloadWaylinesItemResult item_result;
                    item_result.wayline_uuid = item.wayline_uuid;
                    item_result.wayline_name = item.wayline_name;
                    item_result.kmz_object_key = item.kmz_object_key;
                    item_result.url = item.url;
                    item_result.size = item.size;
                    result.list.push_back(item_result);
                }
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::duplicateWayline(const DuplicateWaylineParam& param, std::function<void(FlightHubError err, DuplicateWaylineResult result)> callback) {
    WeakDummy(weak_ptr);
    post_duplicate_wayline(param, [this, weak_ptr, callback](const DuplicateWaylineResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 创建副本航线; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_DUPLICATE, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("duplicateWayline: err_code: %d", (int)err.error_code);
                }
                DuplicateWaylineResult result;
                result.wayline_uuid = response.wayline_uuid;
                result.wayline_name = response.wayline_name;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::copyWayline(const CopyWaylineParam& param, std::function<void(FlightHubError err, CopyWaylineResult result)> callback) {
    WeakDummy(weak_ptr);
    post_copy_wayline(param, [this, weak_ptr, callback](const CopyWaylineResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 航线复制（去组织化，不需要 workspace_id）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_COPY, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("copyWayline: err_code: %d", (int)err.error_code);
                }
                CopyWaylineResult result;
                result.wayline_uuid = response.wayline_uuid;
                result.wayline_name = response.wayline_name;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::concatWayline(const ConcatWaylineParam& param, std::function<void(FlightHubError err, ConcatWaylineResult result)> callback) {
    WeakDummy(weak_ptr);
    post_concat_wayline(param, [this, weak_ptr, callback](const ConcatWaylineResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 航线拼接（去组织化，不需要 workspace_id）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_CONCAT, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("concatWayline: err_code: %d", (int)err.error_code);
                }
                ConcatWaylineResult result;
                result.wayline_uuid = response.wayline_uuid;
                // kmz_data 映射（字段名完全一致，直接赋值）
                result.kmz_data.wayline_type = response.kmz_data.wayline_type;
                result.kmz_data.wayline_point_count = response.kmz_data.wayline_point_count;
                result.kmz_data.exit_on_rc_lost_type = response.kmz_data.exit_on_rc_lost_type;
                result.kmz_data.exit_on_rc_lost_behavior = response.kmz_data.exit_on_rc_lost_behavior;
                result.kmz_data.start_record_action_count = response.kmz_data.start_record_action_count;
                result.kmz_data.stop_record_action_count = response.kmz_data.stop_record_action_count;
                result.kmz_data.ortho_camera_overlap_h = response.kmz_data.ortho_camera_overlap_h;
                result.kmz_data.ortho_camera_overlap_w = response.kmz_data.ortho_camera_overlap_w;
                result.kmz_data.inclined_camera_overlap_h = response.kmz_data.inclined_camera_overlap_h;
                result.kmz_data.inclined_camera_overlap_w = response.kmz_data.inclined_camera_overlap_w;
                result.kmz_data.supplement_prism_model_count = response.kmz_data.supplement_prism_model_count;
                result.kmz_data.estimated_photo_count = response.kmz_data.estimated_photo_count;
                result.kmz_data.duration = response.kmz_data.duration;
                result.kmz_data.distance = response.kmz_data.distance;
                result.kmz_data.survey_area = response.kmz_data.survey_area;
                result.kmz_data.start_latitude = response.kmz_data.start_latitude;
                result.kmz_data.start_longitude = response.kmz_data.start_longitude;
                result.kmz_data.start_altitude = response.kmz_data.start_altitude;
                result.kmz_data.margin_distance = response.kmz_data.margin_distance;
                result.kmz_data.ortho_gsd = response.kmz_data.ortho_gsd;
                result.kmz_data.incline_gsd = response.kmz_data.incline_gsd;
                result.kmz_data.is_wayline_validity_check = response.kmz_data.is_wayline_validity_check;
                result.kmz_data.has_oriented_action = response.kmz_data.has_oriented_action;
                result.kmz_data.has_accurate_action = response.kmz_data.has_accurate_action;
                result.kmz_data.is_surface_fly_on = response.kmz_data.is_surface_fly_on;
                result.kmz_data.is_elevation_optimization_on = response.kmz_data.is_elevation_optimization_on;
                result.kmz_data.is_smart_oblique_on = response.kmz_data.is_smart_oblique_on;
                result.kmz_data.is_mapping_strip_use_template_altitude = response.kmz_data.is_mapping_strip_use_template_altitude;
                result.kmz_data.is_with_megaphone = response.kmz_data.is_with_megaphone;
                result.kmz_data.is_with_search_light = response.kmz_data.is_with_search_light;
                result.kmz_data.is_ai_wayline = response.kmz_data.is_ai_wayline;
                result.kmz_data.drone_type = response.kmz_data.drone_type;
                result.kmz_data.target_detect_area_file_path = response.kmz_data.target_detect_area_file_path;
                result.kmz_data.wayline_validity_check_codes = response.kmz_data.wayline_validity_check_codes;
                result.kmz_data.elevation_file_names = response.kmz_data.elevation_file_names;
                result.kmz_data.payload_types = response.kmz_data.payload_types;
                result.kmz_data.payload_types_with_index = response.kmz_data.payload_types_with_index;
                result.kmz_data.accurate_action_uuids = response.kmz_data.accurate_action_uuids;
                result.kmz_data.action_uuids = response.kmz_data.action_uuids;
                result.kmz_data.res_file_md5s = response.kmz_data.res_file_md5s;
                result.kmz_data.audio_file_md5s = response.kmz_data.audio_file_md5s;
                result.kmz_data.target_detect_action_uuids = response.kmz_data.target_detect_action_uuids;
                result.kmz_data.ai_audio_file_md5s = response.kmz_data.ai_audio_file_md5s;
                result.kmz_data.action_uuid_to_md5_map = response.kmz_data.action_uuid_to_md5_map;
                result.kmz_data.audio_action_uuid_to_file_path_map = response.kmz_data.audio_action_uuid_to_file_path_map;
                result.kmz_data.ai_audio_action_uuid_to_file_path_map = response.kmz_data.ai_audio_action_uuid_to_file_path_map;
                for (const auto& p : response.kmz_data.survey_area_scope.points) {
                    WaylineDetailSurveyAreaScopePoint pt;
                    pt.lng = p.lng;
                    pt.lat = p.lat;
                    pt.alt = p.alt;
                    result.kmz_data.survey_area_scope.points.push_back(pt);
                }
                // ext_data 映射
                result.ext_data.kmz_without_res_object_key = response.ext_data.kmz_without_res_object_key;
                result.ext_data.kmz_without_res_md5 = response.ext_data.kmz_without_res_md5;
                result.ext_data.target_area_file_object_key = response.ext_data.target_area_file_object_key;
                result.ext_data.business_file_object_key = response.ext_data.business_file_object_key;
                result.ext_data.model_file_object_keys = response.ext_data.model_file_object_keys;
                result.ext_data.elevation_file_object_keys = response.ext_data.elevation_file_object_keys;
                result.ext_data.content_length = response.ext_data.content_length;
                for (const auto& m : response.ext_data.models) {
                    ConcatWaylineExtDataModel model;
                    model.uuid = m.uuid;
                    model.file_key = m.file_key;
                    model.boundary.latitude = m.boundary.latitude;
                    model.boundary.longitude = m.boundary.longitude;
                    model.boundary.altitude = m.boundary.altitude;
                    model.boundary.half_x = m.boundary.half_x;
                    model.boundary.half_y = m.boundary.half_y;
                    model.boundary.half_z = m.boundary.half_z;
                    model.boundary.transform = m.boundary.transform;
                    for (const auto& pt : m.bounding.points) {
                        ConcatWaylineExtDataModelBoundingPoint p;
                        p.lng = pt.lng;
                        p.lat = pt.lat;
                        model.bounding.points.push_back(p);
                    }
                    result.ext_data.models.push_back(model);
                }
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void WaylineManageModuleImpl::importKmz(const std::vector<std::string>& file_paths, std::function<void(FlightHubError err, ImportKmzResult result)> callback) {
    WeakDummy(weak_ptr);
    post_import_kmz(file_paths, [this, weak_ptr, callback](const ImportKmzResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 上传 KMZ 文件并导入航线; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_WAYLINE_IMPORT_KMZ, err, "[WaylineManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("importKmz: err_code: %d", (int)err.error_code);
                }
                ImportKmzResult result;
                result.total = response.total;
                for (const auto& item : response.list) {
                    ImportKmzItem kmz_item;
                    kmz_item.kmz_object_key = item.kmz_object_key;
                    kmz_item.wayline_name = item.wayline_name;
                    kmz_item.async_request_id = item.async_request_id;
                    result.list.push_back(kmz_item);
                }
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

int WaylineManageModuleImpl::listenImportWaylineEvent(const std::function<void(const ImportWaylineEventResult& result)>& callback) {
    LOG_INFO("listenImportWaylineEvent");
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenImportWaylineEvent(
        [this, weak_ptr, callback](const notification_center::ImportWaylineEvent& msg) -> void {
            WeakDummyReturn(weak_ptr);
            if (!callback) return;

            ImportWaylineEventResult result;
            result.async_request_id = msg.async_request_id;
            result.timestamp = msg.timestamp;
            result.kmz_object_key = msg.kmz_object_key;
            result.wayline_uuid = msg.wayline_uuid;
            result.code = msg.code;
            result.message = msg.message;

            result.kmz_data.wayline_type = msg.kmz_data.wayline_type;
            result.kmz_data.wayline_point_count = msg.kmz_data.wayline_point_count;
            result.kmz_data.exit_on_rc_lost_type = msg.kmz_data.exit_on_rc_lost_type;
            result.kmz_data.exit_on_rc_lost_behavior = msg.kmz_data.exit_on_rc_lost_behavior;
            result.kmz_data.start_record_action_count = msg.kmz_data.start_record_action_count;
            result.kmz_data.stop_record_action_count = msg.kmz_data.stop_record_action_count;
            result.kmz_data.ortho_camera_overlap_h = msg.kmz_data.ortho_camera_overlap_h;
            result.kmz_data.ortho_camera_overlap_w = msg.kmz_data.ortho_camera_overlap_w;
            result.kmz_data.inclined_camera_overlap_h = msg.kmz_data.inclined_camera_overlap_h;
            result.kmz_data.inclined_camera_overlap_w = msg.kmz_data.inclined_camera_overlap_w;
            result.kmz_data.supplement_prism_model_count = msg.kmz_data.supplement_prism_model_count;
            result.kmz_data.estimated_photo_count = msg.kmz_data.estimated_photo_count;
            result.kmz_data.duration = msg.kmz_data.duration;
            result.kmz_data.distance = msg.kmz_data.distance;
            result.kmz_data.survey_area = msg.kmz_data.survey_area;
            result.kmz_data.start_latitude = msg.kmz_data.start_latitude;
            result.kmz_data.start_longitude = msg.kmz_data.start_longitude;
            result.kmz_data.start_altitude = msg.kmz_data.start_altitude;
            result.kmz_data.margin_distance = msg.kmz_data.margin_distance;
            result.kmz_data.ortho_gsd = msg.kmz_data.ortho_gsd;
            result.kmz_data.incline_gsd = msg.kmz_data.incline_gsd;
            result.kmz_data.is_wayline_validity_check = msg.kmz_data.is_wayline_validity_check;
            result.kmz_data.has_oriented_action = msg.kmz_data.has_oriented_action;
            result.kmz_data.has_accurate_action = msg.kmz_data.has_accurate_action;
            result.kmz_data.is_surface_fly_on = msg.kmz_data.is_surface_fly_on;
            result.kmz_data.is_elevation_optimization_on = msg.kmz_data.is_elevation_optimization_on;
            result.kmz_data.is_smart_oblique_on = msg.kmz_data.is_smart_oblique_on;
            result.kmz_data.is_mapping_strip_use_template_altitude = msg.kmz_data.is_mapping_strip_use_template_altitude;
            result.kmz_data.is_with_megaphone = msg.kmz_data.is_with_megaphone;
            result.kmz_data.is_with_search_light = msg.kmz_data.is_with_search_light;
            result.kmz_data.is_ai_wayline = msg.kmz_data.is_ai_wayline;
            result.kmz_data.drone_type = msg.kmz_data.drone_type;
            result.kmz_data.target_detect_area_file_path = msg.kmz_data.target_detect_area_file_path;
            result.kmz_data.wayline_validity_check_codes = msg.kmz_data.wayline_validity_check_codes;
            result.kmz_data.elevation_file_names = msg.kmz_data.elevation_file_names;
            result.kmz_data.payload_types = msg.kmz_data.payload_types;
            result.kmz_data.payload_types_with_index = msg.kmz_data.payload_types_with_index;
            result.kmz_data.accurate_action_uuids = msg.kmz_data.accurate_action_uuids;
            result.kmz_data.action_uuids = msg.kmz_data.action_uuids;
            result.kmz_data.res_file_md5s = msg.kmz_data.res_file_md5s;
            result.kmz_data.audio_file_md5s = msg.kmz_data.audio_file_md5s;
            result.kmz_data.target_detect_action_uuids = msg.kmz_data.target_detect_action_uuids;
            result.kmz_data.ai_audio_file_md5s = msg.kmz_data.ai_audio_file_md5s;
            result.kmz_data.action_uuid_to_md5_map = msg.kmz_data.action_uuid_to_md5_map;
            result.kmz_data.audio_action_uuid_to_file_path_map = msg.kmz_data.audio_action_uuid_to_file_path_map;
            result.kmz_data.ai_audio_action_uuid_to_file_path_map = msg.kmz_data.ai_audio_action_uuid_to_file_path_map;

            for (const auto& point : msg.kmz_data.survey_area_scope.points) {
                ImportWaylineEventPoint p;
                p.lng = point.lng;
                p.lat = point.lat;
                p.alt = point.alt;
                result.kmz_data.survey_area_scope.points.push_back(p);
            }

            result.ext_data.kmz_without_res_object_key = msg.ext_data.kmz_without_res_object_key;
            result.ext_data.kmz_without_res_md5 = msg.ext_data.kmz_without_res_md5;
            result.ext_data.target_area_file_object_key = msg.ext_data.target_area_file_object_key;
            result.ext_data.business_file_object_key = msg.ext_data.business_file_object_key;
            result.ext_data.model_file_object_keys = msg.ext_data.model_file_object_keys;

            LOG_INFO("[WaylineManageModuleImpl] listenImportWaylineEvent, wayline_uuid: %s, code: %d",
                     msg.wayline_uuid.c_str(), msg.code);
            callback(result);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[WaylineManageModuleImpl] listenImportWaylineEvent error: %d", error_code);
            }
        },
        "", notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void WaylineManageModuleImpl::cancelListenImportWaylineEvent(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

void WaylineManageModuleImpl::getWaylineEditorSts(
    const GetWaylineEditorStsParams& params,
    std::function<void(FlightHubError err, const GetWaylineEditorStsResult& result)> callback) {
    if (!callback) { LOG_ERROR("getWaylineEditorSts(): callback is NULL !"); return; }
    if (params.object_key_prefix.empty()) {
        LOG_ERROR("getWaylineEditorSts(): object_key_prefix is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取航线编辑器 OSS STS 临时上传凭证(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_WAYLINE_GET_EDITOR_STS, param_err, "[WaylineManageModuleImpl]");
        callback(param_err, {});
        return;
    }
    WeakDummy(weak_ptr);

    GetWaylineEditorStsRequest request;
    request.object_key_prefix = params.object_key_prefix;
    request.expire = params.expire;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        get_wayline_editor_sts(request,
            [weak_ptr, callback](const GetWaylineEditorStsResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取航线编辑器 OSS STS 临时上传凭证; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_WAYLINE_GET_EDITOR_STS, err, "[WaylineManageModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        LOG_ERROR("getWaylineEditorSts(): err_code: %d", static_cast<int>(err.error_code));
                        callback(err, {});
                        return;
                    }
                    GetWaylineEditorStsResult result;
                    result.credentials.access_key_id     = data.credentials.access_key_id;
                    result.credentials.access_key_secret = data.credentials.access_key_secret;
                    result.credentials.expire            = data.credentials.expire;
                    result.credentials.security_token    = data.credentials.security_token;
                    result.credentials.platform          = data.credentials.platform;
                    result.object_key_prefix = data.object_key_prefix;
                    result.bucket     = data.bucket;
                    result.endpoint   = data.endpoint;
                    result.provider   = data.provider;
                    result.region     = data.region;
                    callback(err, result);
                });
            });
    });
}

}  // namespace flight_hub_sdk
