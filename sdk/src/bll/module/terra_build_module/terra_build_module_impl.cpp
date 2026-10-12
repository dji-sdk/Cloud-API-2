// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "terra_build_module_impl.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include "BusinessManager.h"
#include <atomic>

namespace flight_hub_sdk {

static std::atomic<int> s_terra_build_instance_id{1};

TerraBuildModuleImpl::TerraBuildModuleImpl()
    {
    instance_id_ = s_terra_build_instance_id.fetch_add(1);
    log_tag_ = "TerraBuild#" + std::to_string(instance_id_);
    LOG_INFO("[%s] constructor, instance_id: %d",
             log_tag_.c_str(), instance_id_);
}

TerraBuildModuleImpl::~TerraBuildModuleImpl() {
    LOG_INFO("[%s] destructor invoked.", log_tag_.c_str());
}

// 1. 获取重建设置
void TerraBuildModuleImpl::getReconstructionSettings(
    const std::string& file_uuid,
    std::function<void(FlightHubError err, const TerraBuildSettingsResult& result)> callback) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_uuid, callback]() {
        WeakDummyReturn(weak_ptr);
        get_reconstruction_settings(file_uuid,
            [this, weak_ptr, callback](const ReconstructionSettingsResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取重建设置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_RECONSTRUCTION_SETTINGS, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildSettingsResult{});
                        return;
                    }
                    TerraBuildSettingsResult result;
                    result.state = data.state;
                    result.media_input_object_key = data.media_input_object_key;
                    result.sfm_output_object_prefix = data.sfm_output_object_prefix;
                    result.reconstruction_settings.name = data.reconstruction_settings.name;
                    result.reconstruction_settings.reconstruction_type = data.reconstruction_settings.reconstruction_type;
                    result.reconstruction_settings.simplified_factor = data.reconstruction_settings.simplified_factor;
                    result.reconstruction_settings.reconstruction_mode = data.reconstruction_settings.reconstruction_mode;
                    result.reconstruction_settings.generate_model_formats = data.reconstruction_settings.generate_model_formats;
                    result.reconstruction_settings.wkt = data.reconstruction_settings.wkt;
                    result.reconstruction_settings.quality_level = data.reconstruction_settings.quality_level;
                    result.reconstruction_settings.quality_level_3dgs = data.reconstruction_settings.quality_level_3dgs;
                    for (const auto& pt : data.reconstruction_settings.predefine_area.polygon_points) {
                        ReconstructionPolygonPoint p;
                        p.lat = pt.lat;
                        p.lng = pt.lng;
                        result.reconstruction_settings.predefine_area.polygon_points.push_back(p);
                    }
                    for (const auto& f : data.folder_list) {
                        TerraBuildFolderInfo info;
                        info.folder_id = f.folder_id;
                        info.folder_uuid = f.folder_uuid;
                        info.folder_name = f.folder_name;
                        result.folder_list.push_back(info);
                    }
                    if (callback) callback(err, result);
                });
            });
    });
}

// 2. 获取质量报告
void TerraBuildModuleImpl::getQualityReport(
    const std::string& file_uuid,
    const std::string& language,
    const std::string& measure_unit_system,
    std::function<void(FlightHubError err, const TerraBuildQualityReportResult& result)> callback) {
    WeakDummy(weak_ptr);
    GetQualityReportRequest request;
    request.language = language;
    request.measure_unit_system = measure_unit_system;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_uuid, request, callback]() {
        WeakDummyReturn(weak_ptr);
        get_quality_report(file_uuid, request,
            [this, weak_ptr, callback](const QualityReportResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取质量报告; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_QUALITY_REPORT, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildQualityReportResult{});
                        return;
                    }
                    TerraBuildQualityReportResult result;
                    result.quality_report_url = data.quality_report_url;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 3. 获取模型文件列表
void TerraBuildModuleImpl::getModelFileList(
    int page,
    int page_size,
    std::function<void(FlightHubError err, const TerraBuildModelFileListResult& result)> callback) {
    WeakDummy(weak_ptr);
    GetModelFileListRequest request;
    request.page = page;
    request.page_size = page_size;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        get_model_file_list(request,
            [this, weak_ptr, callback](const ModelFileListResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取模型文件列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_MODEL_FILE_LIST, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildModelFileListResult{});
                        return;
                    }
                    TerraBuildModelFileListResult result;
                    result.total = data.pagination.total;
                    result.page = data.pagination.page;
                    result.page_size = data.pagination.page_size;
                    for (const auto& item : data.list) {
                        TerraBuildModelFileItem fi;
                        fi.id = item.id;
                        fi.uuid = item.uuid;
                        fi.name = item.name;
                        fi.file_type = item.file_type;
                        fi.sub_file_type = item.sub_file_type;
                        fi.state = item.state;
                        fi.reconstruction_type = item.reconstruction_type;
                        fi.thumbnail_url = item.thumbnail_url;
                        fi.zip_url = item.zip_url;
                        fi.created_at = item.created_at;
                        fi.updated_at = item.updated_at;
                        result.list.push_back(fi);
                    }
                    if (callback) callback(err, result);
                });
            });
    });
}

// 4. 获取模型文件签名 URL
void TerraBuildModuleImpl::getModelFileUrl(
    int file_id,
    int type,
    std::function<void(FlightHubError err, const TerraBuildModelFileUrlResult& result)> callback) {
    WeakDummy(weak_ptr);
    GetModelFileUrlRequest request;
    request.type = type;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, request, callback]() {
        WeakDummyReturn(weak_ptr);
        get_model_file_url(file_id, request,
            [this, weak_ptr, callback](const ModelFileUrlResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取模型文件签名 URL; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_MODEL_FILE_URL, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildModelFileUrlResult{});
                        return;
                    }
                    TerraBuildModelFileUrlResult result;
                    result.url = data.url;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 5. 创建重建任务
void TerraBuildModuleImpl::createReconstruction(
    const CreateReconstructionParams& params,
    std::function<void(FlightHubError err, const TerraBuildCreateReconstructionResult& result)> callback) {
    WeakDummy(weak_ptr);

    CreateReconstructionRequest req;
    // 必填字段直接赋值
    req.folder_uuids = params.folder_uuids;
    req.folder_ids = params.folder_ids;
    req.reconstruction_type = params.reconstruction_type;
    req.name = params.name;
    // 可选字段：只有非空/非零/已设置时才赋值，保持未设置字段为 nullopt
    req.simplified_factor = params.simplified_factor;
    if (!params.generate_model_formats.empty())
        req.generate_model_formats = params.generate_model_formats;
    if (!params.wkt.empty())
        req.wkt = params.wkt;
    req.quality_level = params.quality_level;
    req.quality_level_3dgs = params.quality_level_3dgs;
    req.is_shown_on_map = params.is_shown_on_map;
    req.optimize_SfM = params.optimize_SfM;
    req.custom_storage_parent_id = params.custom_storage_parent_id;
    // predefine_area
    if (params.predefine_area.has_value() && !params.predefine_area->polygon_points.empty()) {
        PredefineArea area;
        for (const auto& pt : params.predefine_area->polygon_points) {
            PolygonPoint p; p.lat = pt.lat; p.lng = pt.lng;
            area.polygon_points.push_back(p);
        }
        req.predefine_area = area;
    }
    // quality_report_setting
    if (params.quality_report_setting.has_value()) {
        QualityReportSetting q;
        q.language = params.quality_report_setting->language;
        q.measure_unit_system = params.quality_report_setting->measure_unit_system;
        req.quality_report_setting = q;
    }

    auto copyGeoDesc = [](const ReconGeoDesc& src) -> std::optional<GeoDesc> {
        if (src.cs_type.empty() && src.geo_cs.empty() && src.geo_cs_wkt.empty()
            && src.geoid_file_name.empty() && src.offset.empty()
            && src.override_vertical_cs.empty()) {
            return std::nullopt;
        }
        GeoDesc dst;
        dst.cs_type = src.cs_type;
        dst.geo_cs = src.geo_cs;
        dst.geo_cs_wkt = src.geo_cs_wkt;
        dst.geoid_file_name = src.geoid_file_name;
        dst.offset = src.offset;
        dst.override_vertical_cs = src.override_vertical_cs;
        return dst;
    };

    // parameter_2d
    if (params.parameter_2d.has_value()) {
        const auto& s = *params.parameter_2d;
        Parameter2D p;
        p.SfM_method = s.SfM_method;
        p.sfm_feature_quantity_level = s.sfm_feature_quantity_level;
        p.use_image_position = s.use_image_position;
        p.generate_blocks_exchangge_xml = s.generate_blocks_exchangge_xml;
        p.blocks_exchange_xml_geo_desc = copyGeoDesc(s.blocks_exchange_xml_geo_desc);
        p.usable_memory_GB = s.usable_memory_GB;
        p.map_mode = s.map_mode; p.quality_level = s.quality_level;
        p.resolution_type_2d = s.resolution_type_2d; p.dom_tile_size = s.dom_tile_size;
        p.output_geo_desc = copyGeoDesc(s.output_geo_desc);
        p.resolution_gsd_2d = s.resolution_gsd_2d;
        p.use_Wallis = s.use_Wallis; p.use_Dehaze = s.use_Dehaze; p.use_gcj02 = s.use_gcj02;
        p.generate_maps_2d_tiff = s.generate_maps_2d_tiff;
        p.generate_maps_2d_tiff_cog = s.generate_maps_2d_tiff_cog;
        p.use_smart_fill_hole = s.use_smart_fill_hole;
        req.parameter_2d = p;
    }
    // parameter_3d
    if (params.parameter_3d.has_value()) {
        const auto& s = *params.parameter_3d;
        Parameter3D p;
        p.SfM_method = s.SfM_method;
        p.sfm_feature_quantity_level = s.sfm_feature_quantity_level;
        p.use_image_position = s.use_image_position;
        p.generate_blocks_exchangge_xml = s.generate_blocks_exchangge_xml;
        p.blocks_exchange_xml_geo_desc = copyGeoDesc(s.blocks_exchange_xml_geo_desc);
        p.output_pointcloud = s.output_pointcloud; p.density_percent_level = s.density_percent_level;
        p.generate_pnts = s.generate_pnts;
        p.generate_las = s.generate_las; p.generate_laz = s.generate_laz;
        p.generate_pcd = s.generate_pcd; p.generate_point_s3mb = s.generate_point_s3mb;
        p.generate_point_ply = s.generate_point_ply; p.merge_point = s.merge_point;
        p.output_ply_with_normals = s.output_ply_with_normals;
        p.ply_with_normals_level = s.ply_with_normals_level;
        p.groud_extract = s.groud_extract; p.scene_type = s.scene_type;
        p.max_size_building = s.max_size_building; p.iter_dist = s.iter_dist; p.iter_angle = s.iter_angle;
        p.usable_memory_GB = s.usable_memory_GB; p.stereo_mode = s.stereo_mode;
        p.global_min_depth = s.global_min_depth;
        p.output_mesh = s.output_mesh; p.generate_ply = s.generate_ply;
        p.generate_obj = s.generate_obj; p.generate_fbx = s.generate_fbx;
        p.generate_b3dm = s.generate_b3dm; p.generate_osgb = s.generate_osgb;
        p.generate_s3mb = s.generate_s3mb; p.generate_i3s = s.generate_i3s;
        p.water_refine = s.water_refine; p.water_remove = s.water_remove;
        p.model_simplify = s.model_simplify; p.generate_dem = s.generate_dem;
        p.dem_resolution = s.dem_resolution; p.generate_contour = s.generate_contour;
        p.contour_datum = s.contour_datum; p.contour_interval = s.contour_interval;
        p.contour_elevation_annotation_radius = s.contour_elevation_annotation_radius;
        p.use_contour_filter = s.use_contour_filter; p.contour_filter_length = s.contour_filter_length;
        p.generate_grid_and_tin = s.generate_grid_and_tin; p.grid_interval = s.grid_interval;
        p.cut_AOI = s.cut_AOI; p.quality_level = s.quality_level;
        p.output_geo_desc = copyGeoDesc(s.output_geo_desc);
        req.parameter_3d = p;
    }
    // parameter_3dgs
    if (params.parameter_3dgs.has_value()) {
        const auto& s = *params.parameter_3dgs;
        Parameter3DGS p;
        p.SfM_method = s.SfM_method;
        p.sfm_feature_quantity_level = s.sfm_feature_quantity_level;
        p.use_image_position = s.use_image_position;
        p.generate_blocks_exchangge_xml = s.generate_blocks_exchangge_xml;
        p.blocks_exchange_xml_geo_desc = copyGeoDesc(s.blocks_exchange_xml_geo_desc);
        p.global_data_type = s.global_data_type;
        p.quality_level_3dgs = s.quality_level_3dgs;
        p.generate_terrags = s.generate_terrags; p.generate_3dtiles = s.generate_3dtiles;
        p.generate_3dgs_dom = s.generate_3dgs_dom;
        p.output_geo_desc = copyGeoDesc(s.output_geo_desc);
        req.parameter_3dgs = p;
    }
    // parameter_lidar
    if (params.parameter_lidar.has_value()) {
        const auto& s = *params.parameter_lidar;
        ParameterLidar p;
        p.output_pointcloud = s.output_pointcloud; p.density_percent_level = s.density_percent_level;
        p.generate_pnts = s.generate_pnts;
        p.generate_las = s.generate_las; p.generate_laz = s.generate_laz;
        p.generate_pcd = s.generate_pcd; p.generate_point_s3mb = s.generate_point_s3mb;
        p.generate_point_ply = s.generate_point_ply; p.merge_point = s.merge_point;
        p.output_ply_with_normals = s.output_ply_with_normals;
        p.ply_with_normals_level = s.ply_with_normals_level;
        p.groud_extract = s.groud_extract; p.scene_type = s.scene_type;
        p.max_size_building = s.max_size_building; p.iter_dist = s.iter_dist; p.iter_angle = s.iter_angle;
        p.usable_memory_GB = s.usable_memory_GB; p.colorize_point = s.colorize_point;
        p.lpp_optimise = s.lpp_optimise; p.refine_calibrate = s.refine_calibrate;
        p.smooth_point = s.smooth_point; p.sample_distance = s.sample_distance;
        p.output_height_offset = s.output_height_offset; p.quality_level = s.quality_level;
        p.can_process_L1 = s.can_process_L1;
        p.output_geo_desc = copyGeoDesc(s.output_geo_desc);
        p.output_mesh = s.output_mesh; p.quality_level_mesh = s.quality_level_mesh;
        p.model_simplify = s.model_simplify;
        p.generate_obj = s.generate_obj; p.generate_fbx = s.generate_fbx;
        p.generate_ply = s.generate_ply; p.generate_osgb = s.generate_osgb;
        p.generate_b3dm = s.generate_b3dm; p.generate_i3s = s.generate_i3s;
        p.generate_maps_2d = s.generate_maps_2d;
        p.generate_maps_2d_tiff = s.generate_maps_2d_tiff;
        p.generate_maps_2d_tiff_cog = s.generate_maps_2d_tiff_cog;
        p.resolution_type_2d = s.resolution_type_2d; p.resolution_level_2d = s.resolution_level_2d;
        p.resolution_gsd_2d = s.resolution_gsd_2d; p.dom_tile_size = s.dom_tile_size;
        p.use_gcj02 = s.use_gcj02; p.generate_dem = s.generate_dem; p.dem_resolution = s.dem_resolution;
        p.generate_contour = s.generate_contour; p.contour_datum = s.contour_datum;
        p.contour_interval = s.contour_interval;
        p.contour_elevation_annotation_radius = s.contour_elevation_annotation_radius;
        p.use_contour_filter = s.use_contour_filter; p.contour_filter_length = s.contour_filter_length;
        p.generate_3dgs = s.generate_3dgs; p.quality_level_3dgs = s.quality_level_3dgs;
        p.generate_3dtiles = s.generate_3dtiles;
        req.parameter_lidar = p;
    }

    std::string body;
    std::string json_err;
    aigc::JsonHelper::ObjectToJson(req, body, &json_err);
    if (!json_err.empty()) {
        LOG_ERROR("createReconstruction(): build json error: %s", json_err.c_str());

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 创建重建任务(请求参数序列化失败, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_TERRA_CREATE_RECONSTRUCTION, param_err, "[TerraBuildModuleImpl]");

        if (callback) callback(param_err, TerraBuildCreateReconstructionResult{});
        return;
    }

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, body, callback]() {
        WeakDummyReturn(weak_ptr);
        create_reconstruction(body,
            [this, weak_ptr, callback](const CreateReconstructionResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 创建重建任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_CREATE_RECONSTRUCTION, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildCreateReconstructionResult{});
                        return;
                    }
                    TerraBuildCreateReconstructionResult result;
                    result.id = data.id;
                    result.uuid = data.uuid;
                    result.job_uuid = data.job_uuid;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 6. 重试重建任务
void TerraBuildModuleImpl::retryReconstruction(
    const std::string& file_uuid,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    RetryReconstructionRequest request;
    request.file_uuid = file_uuid;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        retry_reconstruction(request,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback]() {
                    // 埋点: 重试重建任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_RETRY_RECONSTRUCTION, err, "[TerraBuildModuleImpl]");

                    if (callback) callback(err);
                });
            });
    });
}

// 7. 停止重建任务
void TerraBuildModuleImpl::stopReconstruction(
    const std::string& file_uuid,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    StopReconstructionRequest request;
    request.file_uuid = file_uuid;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        stop_reconstruction(request,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback]() {
                    // 埋点: 停止重建任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_STOP_RECONSTRUCTION, err, "[TerraBuildModuleImpl]");

                    if (callback) callback(err);
                });
            });
    });
}

// 8. 获取空三结果
void TerraBuildModuleImpl::getSfmResult(
    const std::string& file_uuid,
    std::function<void(FlightHubError err, const TerraBuildSfmResult& result)> callback) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_uuid, callback]() {
        WeakDummyReturn(weak_ptr);
        get_sfm_result(file_uuid,
            [this, weak_ptr, callback](const SfmResultResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取空三结果; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_SFM_RESULT, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildSfmResult{});
                        return;
                    }
                    TerraBuildSfmResult result;
                    result.sfm_json_url = data.sfm_json_url;
                    result.sfm_report_json_url = data.sfm_report_json_url;
                    result.gcp_out_json_url = data.gcp_out_json_url;
                    result.re_projection_error_pic_url_list = data.re_projection_error_pic_url_list;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 9. 删除单个模型文件
void TerraBuildModuleImpl::deleteModelFile(
    int file_id,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, callback]() {
        WeakDummyReturn(weak_ptr);
        delete_model_file(file_id,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback]() {
                    // 埋点: 删除单个模型文件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_DELETE_MODEL_FILE, err, "[TerraBuildModuleImpl]");

                    if (callback) callback(err);
                });
            });
    });
}

// 10. 批量删除模型文件
void TerraBuildModuleImpl::batchDeleteModelFiles(
    const std::vector<int>& file_ids,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    DeleteModelFilesRequest request;
    request.file_ids = file_ids;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        batch_delete_model_files(request,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback]() {
                    // 埋点: 批量删除模型文件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_BATCH_DELETE_MODEL_FILES, err, "[TerraBuildModuleImpl]");

                    if (callback) callback(err);
                });
            });
    });
}

// 11. 创建全景拼接任务
void TerraBuildModuleImpl::createPanorama(
    const CreatePanoramaParams& params,
    std::function<void(FlightHubError err, const TerraBuildCreatePanoramaResult& result)> callback) {
    WeakDummy(weak_ptr);
    CreatePanoramaRequest request;
    request.name = params.name;
    request.folder_uuids = params.folder_uuids;
    request.wkt = params.wkt;
    request.is_shown_on_map = params.is_shown_on_map;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        create_panorama(request,
            [this, weak_ptr, callback](const CreatePanoramaResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 创建全景拼接任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_CREATE_PANORAMA, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildCreatePanoramaResult{});
                        return;
                    }
                    TerraBuildCreatePanoramaResult result;
                    result.id = data.id;
                    result.uuid = data.uuid;
                    result.job_uuid = data.job_uuid;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 12. 获取重建图像列表
void TerraBuildModuleImpl::getReconstructionImages(
    const std::string& file_uuid,
    std::function<void(FlightHubError err, const TerraBuildReconstructionImagesResult& result)> callback) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_uuid, callback]() {
        WeakDummyReturn(weak_ptr);
        get_reconstruction_images(file_uuid,
            [this, weak_ptr, callback](const ReconstructionImagesResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取重建图像列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_RECONSTRUCTION_IMAGES, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraBuildReconstructionImagesResult{});
                        return;
                    }
                    TerraBuildReconstructionImagesResult result;
                    result.total = data.total;
                    for (const auto& item : data.list) {
                        TerraBuildReconstructionImageItem img;
                        img.image_id = item.image_id;
                        img.file_name = item.file_name;
                        img.thumbnail_image_url = item.thumbnail_image_url;
                        img.origin_image_url = item.origin_image_url;
                        result.list.push_back(img);
                    }
                    if (callback) callback(err, result);
                });
            });
    });
}

// 15. 监听重建任务状态变更推送
int TerraBuildModuleImpl::listenReconstructionStatusChanged(
    const std::function<void(const TerraBuildReconstructionStatusChanged& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenReconstructionStatusChangedMessage(
            [weak_ptr, callback](const notification_center::ReconstructionStatusChanged& nc_msg) {
                WeakDummyReturn(weak_ptr);
                TerraBuildReconstructionStatusChanged msg;
                msg.workspace_id         = nc_msg.workspace_id;
                msg.id                   = nc_msg.id;
                msg.name                 = nc_msg.name;
                msg.state                = nc_msg.state;
                msg.uuid                 = nc_msg.uuid;
                msg.file_type            = nc_msg.file_type;
                msg.sub_file_type        = nc_msg.sub_file_type;
                msg.code                 = nc_msg.code;
                msg.size                 = nc_msg.size;
                msg.fast_reconstruction_id = nc_msg.fast_reconstruction_id;
                // job_info
                msg.job_info.type       = nc_msg.job_info.type;
                msg.job_info.uuid       = nc_msg.job_info.uuid;
                msg.job_info.job_status = nc_msg.job_info.job_status;
                msg.job_info.percent    = nc_msg.job_info.percent;
                msg.job_info.code       = nc_msg.job_info.code;
                // job_infos
                for (const auto& j : nc_msg.job_infos) {
                    TerraBuildJobInfo ji;
                    ji.type       = j.type;
                    ji.uuid       = j.uuid;
                    ji.job_status = j.job_status;
                    ji.percent    = j.percent;
                    ji.code       = j.code;
                    msg.job_infos.push_back(ji);
                }
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",   // device_sn 为空，推送按 workspace 广播
            notification_center::NotificationFrequency_OnChanged);
}

// 16. 取消监听重建任务状态变更推送
void TerraBuildModuleImpl::cancelListenReconstructionStatusChanged(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

// 17. 模型文件上传回调
void TerraBuildModuleImpl::uploadModelCallback(
    const TerraUploadCallbackParams& params,
    std::function<void(FlightHubError err, const TerraUploadCallbackResult& result)> callback) {
    WeakDummy(weak_ptr);
    TerraUploadCallbackRequest request;
    request.object_key  = params.object_key;
    request.name        = params.name;
    request.parent_id   = params.parent_id;
    request.drive_uuid  = params.drive_uuid;
    request.show_on_map = params.show_on_map;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        terra_upload_callback(request,
            [this, weak_ptr, callback](const TerraUploadCallbackResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 模型文件上传回调; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_UPLOAD_CALLBACK, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraUploadCallbackResult{});
                        return;
                    }
                    TerraUploadCallbackResult result;
                    result.id   = data.id;
                    result.uuid = data.uuid;
                    result.name = data.name;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 18. 模型重命名
void TerraBuildModuleImpl::renameModel(
    int file_id,
    const TerraRenameModelParams& params,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    TerraRenameModelRequest request;
    request.name               = params.name;
    request.in_uniform_height  = params.in_uniform_height;
    request.model_display_mode = params.model_display_mode;
    request.collected_at       = params.collected_at;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, request, callback]() {
        WeakDummyReturn(weak_ptr);
        terra_rename_model(file_id, request,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback]() {
                    // 埋点: 模型重命名; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_RENAME_MODEL, err, "[TerraBuildModuleImpl]");

                    if (callback) callback(err);
                });
            });
    });
}

// 19. 批量加载/取消加载模型到地图
void TerraBuildModuleImpl::showModelOnMap(
    const TerraShowOnMapParams& params,
    std::function<void(FlightHubError err, const TerraShowOnMapResult& result)> callback) {
    WeakDummy(weak_ptr);
    TerraShowOnMapRequest request;
    request.ids            = params.ids;
    request.show_on_map    = params.show_on_map;
    request.is_distributed = params.is_distributed;
    if (params.select_filter.has_value()) {
        TerraSelectFilter sf;
        sf.selected_parent_id = params.select_filter->selected_parent_id;
        sf.except_ids         = params.select_filter->except_ids;
        sf.key                = params.select_filter->key;
        request.select_filter = sf;
    }

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        terra_show_on_map(request,
            [this, weak_ptr, callback](const TerraShowOnMapResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 批量加载/取消加载模型到地图; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_SHOW_ON_MAP, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraShowOnMapResult{});
                        return;
                    }
                    TerraShowOnMapResult result;
                    result.ids = data.ids;
                    if (callback) callback(err, result);
                });
            });
    });
}

// 20. 监听模型解压进度推送
int TerraBuildModuleImpl::listenTerraUnzipProgress(
    const std::function<void(const TerraUnzipProgressMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenTerraUnzipProgress(
            [weak_ptr, callback](const notification_center::TerraUnzipProgress& nc_msg) {
                WeakDummyReturn(weak_ptr);
                TerraUnzipProgressMsg msg;
                msg.file_id = nc_msg.file_id;
                msg.uuid    = nc_msg.uuid;
                msg.name    = nc_msg.name;
                msg.percent = nc_msg.percent;
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

// 21. 取消监听模型解压进度推送
void TerraBuildModuleImpl::cancelListenTerraUnzipProgress(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

// 22. 监听模型状态变更推送
int TerraBuildModuleImpl::listenTerraModelStatusChanged(
    const std::function<void(const TerraModelStatusChangedMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenTerraModelStatusChanged(
            [weak_ptr, callback](const notification_center::TerraModelStatusChanged& nc_msg) {
                WeakDummyReturn(weak_ptr);
                TerraModelStatusChangedMsg msg;
                msg.file_id      = nc_msg.file_id;
                msg.uuid         = nc_msg.uuid;
                msg.name         = nc_msg.name;
                msg.state        = nc_msg.state;
                msg.file_type    = nc_msg.file_type;
                msg.sub_file_type = nc_msg.sub_file_type;
                msg.error_code   = nc_msg.error_code;
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

// 23. 取消监听模型状态变更推送
void TerraBuildModuleImpl::cancelListenTerraModelStatusChanged(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

// 24. 监听文件展示地图状态推送
int TerraBuildModuleImpl::listenTerraFileShowOnMap(
    const std::function<void(const TerraFileShowOnMapMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenTerraFileShowOnMap(
            [weak_ptr, callback](const notification_center::TerraFileShowOnMap& nc_msg) {
                WeakDummyReturn(weak_ptr);
                TerraFileShowOnMapMsg msg;
                for (const auto& item : nc_msg.list) {
                    TerraFileShowOnMapItem it;
                    it.id           = item.id;
                    it.name         = item.name;
                    it.file_type    = item.file_type;
                    it.sub_file_type = item.sub_file_type;
                    it.preview_url  = item.preview_url;
                    msg.list.push_back(it);
                }
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

// 25. 取消监听文件展示地图状态推送
void TerraBuildModuleImpl::cancelListenTerraFileShowOnMap(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

// 28. 监听模型隐藏地图推送 (topic_media_file_hide_on_map)
int TerraBuildModuleImpl::listenTerraMediaFileHideOnMap(
    const std::function<void(const TerraMediaFileHideOnMapMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenTerraMediaFileHideOnMap(
            [weak_ptr, callback](const notification_center::TerraMediaFileHideOnMap& nc_msg) {
                WeakDummyReturn(weak_ptr);
                TerraMediaFileHideOnMapMsg msg;
                msg.ids = nc_msg.ids;
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

void TerraBuildModuleImpl::cancelListenTerraMediaFileHideOnMap(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

// 29. 监听模型显示地图推送 (topic_media_file_show_on_map)
int TerraBuildModuleImpl::listenTerraMediaFileShowOnMap(
    const std::function<void(const TerraMediaFileShowOnMapMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenTerraMediaFileShowOnMap(
            [weak_ptr, callback](const notification_center::TerraMediaFileShowOnMap& nc_msg) {
                WeakDummyReturn(weak_ptr);
                TerraMediaFileShowOnMapMsg msg;
                for (const auto& nc_item : nc_msg.list) {
                    TerraMediaFileShowOnMapItem item;
                    item.name          = nc_item.name;
                    item.id            = nc_item.id;
                    item.shoot_lat     = nc_item.shoot_lat;
                    item.shoot_lng     = nc_item.shoot_lng;
                    item.shoot_abs_alt = nc_item.shoot_abs_alt;
                    item.shoot_rel_alt = nc_item.shoot_rel_alt;
                    item.photo_lat     = nc_item.photo_lat;
                    item.photo_lng     = nc_item.photo_lng;
                    item.photo_abs_alt = nc_item.photo_abs_alt;
                    item.photo_rel_alt = nc_item.photo_rel_alt;
                    item.preview_url   = nc_item.preview_url;
                    item.file_type     = nc_item.file_type;
                    item.sub_file_type = nc_item.sub_file_type;
                    msg.list.push_back(item);
                }
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

void TerraBuildModuleImpl::cancelListenTerraMediaFileShowOnMap(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

// 26. 获取模型文件详情
void TerraBuildModuleImpl::getModelDetail(
    int file_id,
    std::function<void(FlightHubError err, const TerraModelDetailResult& result)> callback) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, callback]() {
        WeakDummyReturn(weak_ptr);
        terra_get_model_detail(file_id,
            [this, weak_ptr, callback](const TerraModelDetailResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 获取模型文件详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_GET_MODEL_DETAIL, err, "[TerraBuildModuleImpl]");

                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, TerraModelDetailResult{});
                        return;
                    }
                    TerraModelDetailResult r;
                    r.id            = data.id;
                    r.uuid          = data.uuid;
                    r.name          = data.name;
                    r.file_type     = data.file_type;
                    r.sub_file_type = data.sub_file_type;
                    r.size          = data.size;
                    r.suffix        = data.suffix;
                    r.mod           = data.mod;
                    r.show_on_map   = data.show_on_map;
                    r.state         = data.state;
                    r.selected_wkt  = data.selected_wkt;
                    r.parent_id     = data.parent_id;
                    r.update_at     = data.update_at;
                    r.create_at     = data.create_at;
                    r.url           = data.url;
                    r.webp_url      = data.webp_url;
                    r.preview_url   = data.preview_url;
                    r.thumbnail_url = data.thumbnail_url;
                    r.object_key    = data.object_key;
                    r.index_file_key   = data.index_file_key;
                    r.file_prefix      = data.file_prefix;
                    r.webp_file_prefix = data.webp_file_prefix;
                    // boundary — 直接赋值（字段缺失时保持默认 0.0）
                    auto& b = data.boundary;
                    auto& rb = r.boundary;
                    rb.max_lat = b.max_lat;       rb.min_lat = b.min_lat;
                    rb.max_lng = b.max_lng;       rb.min_lng = b.min_lng;
                    rb.max_alt = b.max_alt;       rb.min_alt = b.min_alt;
                    rb.latitude = b.latitude;     rb.longitude = b.longitude;
                    rb.altitude = b.altitude;
                    rb.half_x = b.half_x;         rb.half_y = b.half_y;
                    rb.half_z = b.half_z;
                    rb.transform = b.transform;
                    rb.max_level = b.max_level;   rb.min_level = b.min_level;
                    rb.start_lat = b.start_lat;   rb.end_lat = b.end_lat;
                    rb.start_lng = b.start_lng;   rb.end_lng = b.end_lng;
                    // intensity_info
                    r.intensity_info.max_intensity = data.intensity_info.max_intensity;
                    r.intensity_info.min_intensity = data.intensity_info.min_intensity;
                    // max_return_number
                    r.max_return_number = data.max_return_number;
                    // bounding polygon
                    for (const auto& pt : data.bounding.points) {
                        r.bounding_points.push_back({pt.lng, pt.lat});
                    }
                    // ext
                    r.ext.device              = data.ext.device;
                    r.ext.duration            = data.ext.duration;
                    r.ext.photographer        = data.ext.photographer;
                    r.ext.resolution          = data.ext.resolution;
                    r.ext.height              = data.ext.height;
                    r.ext.width               = data.ext.width;
                    r.ext.zoom_factor         = data.ext.zoom_factor;
                    r.ext.task_name           = data.ext.task_name;
                    r.ext.shoot_position      = {data.ext.shoot_position.lng,  data.ext.shoot_position.lat,
                                                  data.ext.shoot_position.abs_alt, data.ext.shoot_position.rel_alt};
                    r.ext.photo_position      = {data.ext.photo_position.lng,  data.ext.photo_position.lat,
                                                  data.ext.photo_position.abs_alt, data.ext.photo_position.rel_alt};
                    r.ext.line_name           = data.ext.line_name;
                    r.ext.is_original         = data.ext.is_original;
                    r.ext.gimbal_pitch        = data.ext.gimbal_pitch;
                    r.ext.gimbal_yaw          = data.ext.gimbal_yaw;
                    r.ext.gimbal_yaw_degree   = data.ext.gimbal_yaw_degree;
                    r.ext.gimbal_roll         = data.ext.gimbal_roll;
                    r.ext.focal_length        = data.ext.focal_length;
                    r.ext.payload_model_key   = data.ext.payload_model_key;
                    r.ext.drone_model_key     = data.ext.drone_model_key;
                    r.ext.create_time         = data.ext.create_time;
                    r.ext.wayline_action_uuid = data.ext.wayline_action_uuid;
                    r.ext.wayline_uuid        = data.ext.wayline_uuid;
                    r.ext.origin_wayline_uuid = data.ext.origin_wayline_uuid;
                    r.ext.digital_zoom_ratio  = data.ext.digital_zoom_ratio;
                    r.ext.length_in35mm_film  = data.ext.length_in35mm_film;
                    r.file_tags = data.file_tags;
                    r.user_name = data.user_name;
                    if (callback) callback(err, r);
                });
            });
    });
}

// 27. 设置 LAS 模型坐标系并触发格式转换
void TerraBuildModuleImpl::setLasWkt(
    const TerraSetLasWktParams& params,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, params, callback]() {
        WeakDummyReturn(weak_ptr);
        terra_set_las_wkt(params.file_uuid, params.wkt,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback]() {
                    // 埋点: 设置 LAS 模型坐标系并触发格式转换; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_TERRA_SET_LAS_WKT, err, "[TerraBuildModuleImpl]");

                    if (callback) callback(err);
                });
            });
    });
}

}  // namespace flight_hub_sdk
