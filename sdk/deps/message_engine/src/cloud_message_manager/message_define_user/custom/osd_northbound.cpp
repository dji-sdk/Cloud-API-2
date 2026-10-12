// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "osd_northbound.h"

#include <base/common/logger.h>

namespace message_engine {
/// OsdNorthboundMsg to json
json11::Json OsdNorthboundMsgToJson(const OsdNorthboundMsg& osd) {
    json11::Json::object osd_json;
    OsdSingleDataToJson(osd_json, osd);
    PayloadToJson(osd_json, osd);
    BatteryToJson(osd_json, osd);
    PositionStateToJson(osd_json, osd);
    StorageToJson(osd_json, osd);
    DistanceLimitStatusToJson(osd_json, osd);
    MaintainStatusToJson(osd_json, osd);
    FtsInfoToJson(osd_json, osd);
    ObstacleAvoidanceStatusToJson(osd_json, osd);
    CameraStatusToJson(osd_json, osd);
    return osd_json;
}

OsdNorthbound::OsdNorthbound() {
    this->topic_ = "thing/product/" + std::string(TopicUserType) + "/osd";
    this->method_ = "osd";
}

OsdNorthbound::OsdNorthbound(const OsdNorthboundMsg& msg) : OsdNorthbound() {
    this->msg = msg;
}

std::string OsdNorthbound::ToJsonString() {
    json11::Json json = OsdNorthboundMsgToJson(msg);
    std::string json_string = json.dump();
    return json_string;
}

drc_sdk::ByteBuffer OsdNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("OsdNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}

void OsdSingleDataToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    json_obj["activation_time"] = osd.activation_time;
    json_obj["best_link_gateway"] = osd.best_link_gateway;
    json_obj["country"] = osd.country;
    json_obj["height_limit"] = osd.height_limit;
    json_obj["is_near_height_limit"] = osd.is_near_height_limit;
    json_obj["is_near_area_limit"] = osd.is_near_area_limit;
    json_obj["night_lights_state"] = osd.night_lights_state;
    json_obj["rc_lost_action"] = osd.rc_lost_action;
    json_obj["rid_state"] = osd.rid_state;
    json_obj["rth_altitude"] = osd.rth_altitude;
    json_obj["mode_code"] = osd.mode_code;
    json_obj["gear"] = osd.gear;
    json_obj["horizontal_speed"] = osd.aircraft_horizontal_speed;
    json_obj["vertical_speed"] = osd.aircraft_vertical_speed;
    json_obj["longitude"] = osd.aircraft_location_longitude;
    json_obj["latitude"] = osd.aircraft_location_latitude;
    json_obj["height"] = osd.aircraft_absolute_height;
    json_obj["elevation"] = osd.aircraft_relative_height;
    json_obj["attitude_pitch"] = osd.aircraft_attitude_pitch;
    json_obj["attitude_roll"] = osd.aircraft_attitude_roll;
    json_obj["attitude_head"] = osd.aircraft_attitude_yaw;
    json_obj["home_longitude"] = osd.aircraft_home_longitude;
    json_obj["home_latitude"] = osd.aircraft_home_latitude;
    json_obj["home_distance"] = osd.aircraft_home_distance;
    json_obj["wind_speed"] = osd.wind_info_speed;
    json_obj["wind_direction"] = osd.wind_info_direction;
    json_obj["total_flight_time"] = osd.total_flight_time;
    json_obj["total_flight_distance"] = osd.total_flight_distance;
    json_obj["total_flight_sorties"] = osd.total_flight_sorties;
    json_obj["track_id"] = osd.track_id;
    json_obj["firmware_version"] = osd.aircraft_firmware_version;
}

// 负载状态
void PayloadToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    auto payload_index_to_info = osd.payload_index_to_info;
    // 数据合法性检查
    for (auto iter = payload_index_to_info.begin(); iter != payload_index_to_info.end();) {
        if (iter->second.payload_index.empty()) {
            LOG_ERROR("[OsdNorthboundMsg_to_json] Payload_to_json pre-check payload_index_to_info_ payload_index empty, index:%d", iter->first);
            iter = payload_index_to_info.erase(iter);
        } else {
            iter++;
        }
    }

    for (auto iter : payload_index_to_info) {
        if (iter.second.payload_index.empty()) {
            LOG_ERROR("[OsdNorthboundMsg_to_json] Payload_to_json check payload_index empty, index:%d", iter.first);
            break;
        }
        auto info = json11::Json::object{
                            {"payload_index", iter.second.payload_index},
                            {"gimbal_pitch", iter.second.gimbal_pitch},
                            {"gimbal_roll", iter.second.gimbal_roll},
                            {"gimbal_yaw", iter.second.gimbal_yaw},
                        };
        if (iter.second.laser_on) {
            // 激光 开启
            info["measure_target_longitude"] = iter.second.measure_target_longitude;
            info["measure_target_latitude"] = iter.second.measure_target_latitude;
            info["measure_target_altitude"] = iter.second.measure_target_altitude;
            info["measure_target_distance"] = iter.second.measure_target_distance;
            info["measure_target_error_state"] = iter.second.measure_target_error_state;
        }
        if (iter.second.track_on) {
            // track 开启
            json11::Json::array points;
            for (auto point_info : iter.second.track_info_list) {
                points.emplace_back(json11::Json::object{
                    {"track_target_mode", static_cast<int>(point_info.track_target_mode)},
                    {"track_longitude", point_info.track_longitude},
                    {"track_latitude", point_info.track_latitude},
                    {"track_altitude", point_info.track_altitude},
                });
            }
            info["smart_track_point"] = points;
        }
        // thermal
        info["thermal_supported_palette_styles"] = iter.second.thermal_supported_palette_styles;
        info["thermal_current_palette_style"] = iter.second.thermal_current_palette_style;
        info["thermal_gain_mode"] = iter.second.thermal_gain_mode;
        info["thermal_isotherm_state"] = iter.second.thermal_isotherm_state;
        info["thermal_isotherm_upper_limit"] = iter.second.thermal_isotherm_upper_limit;
        info["thermal_isotherm_lower_limit"] = iter.second.thermal_isotherm_lower_limit;
        info["thermal_global_temperature_min"] = iter.second.thermal_global_temperature_min;
        info["thermal_global_temperature_max"] = iter.second.thermal_global_temperature_max;
        info["zoom_factor"] = iter.second.zoom_factor;

        json_obj[iter.second.payload_index] = info;
    }
}

// 飞机电池信息
void BatteryToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    std::vector<json11::Json::object> batteries_list;
    int capacity_percent = osd.capacity_percent;
    for (auto iter : osd.battery_index_to_info) {
        if (!iter.second.existed) {
            continue;
        }
        batteries_list.emplace_back(json11::Json::object{
            {"index", iter.second.index},
            {"capacity_percent", iter.second.capacity_percent},
            {"sn", iter.second.sn},
            {"type", iter.second.type},
            {"sub_type", iter.second.sub_type},
            {"firmware_version", iter.second.firmware_version},
            {"loop_times", iter.second.loop_times},
            {"voltage", iter.second.voltage},
            {"temperature", iter.second.temperature},
            {"high_voltage_storage_days", iter.second.high_voltage_storage_days},
            {"soh_percent", iter.second.soh_percent},
        });
        capacity_percent = iter.second.capacity_percent;  // 暂不支持电池总电量，先用其中一块电池的电量
    }
    auto battery_summary_info = json11::Json::object{
                        {"capacity_percent", capacity_percent},
                        {"remain_flight_time", osd.remain_flight_time},
                        {"return_home_power", osd.return_home_power},
                        {"landing_power", osd.landing_power},
                        {"batteries", batteries_list},
                    };

    json_obj["battery"] = battery_summary_info;
}

// 搜星状态
void PositionStateToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    auto position_state_info = json11::Json::object{
                        {"is_fixed", osd.is_fixed}, {"quality", osd.quality}, {"gps_number", osd.gps_number}, {"rtk_number", osd.rtk_number},
                    };
    json_obj["position_state"] = position_state_info;

}

// 存储容量
void StorageToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    auto storage_info = json11::Json::object{
                        {"total", osd.storage_total},
                        {"used", osd.storage_used},
                    };
    json_obj["storage"] = storage_info;
}

// 限远状态
void DistanceLimitStatusToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    auto distance_limit_status = json11::Json::object{
                            {"distance_limit", osd.distance_limit_status.distance_limit},
                            {"is_near_distance_limit", osd.distance_limit_status.is_near_distance_limit},
                            {"state", osd.distance_limit_status.state},
                        };
    json_obj["distance_limit_status"] = distance_limit_status;

}

// 保养状态
void MaintainStatusToJson(json11::Json::object& json_obj, const OsdNorthboundMsg& osd) {
    std::vector<json11::Json::object> maintain_status_info_list;
    for (const auto& iter : osd.maintain_status) {
        auto maintain_status_info = json11::Json::object{
                                    {"last_maintain_flight_sorties", iter.last_maintain_flight_sorties},
                                    {"last_maintain_flight_time", iter.last_maintain_flight_time},
                                    {"last_maintain_time", iter.last_maintain_time},
                                    {"last_maintain_type", iter.last_maintain_type},
                                    {"state", iter.state},
                                    };
        maintain_status_info_list.emplace_back(maintain_status_info);
    }
    auto maintain_status_array = json11::Json::object{{"maintain_status_array", maintain_status_info_list}};
    json_obj["maintain_status"] = maintain_status_array;

}

// 飞行终止系统状态
void FtsInfoToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    auto fts_info = json11::Json::object{
                                {"fts_firmware_version", osd.fts_info.fts_firmware_version},
                                {"fts_sn", osd.fts_info.fts_sn},
                                {"fts_status", osd.fts_info.fts_status},
                                {"fts_timestamp", osd.fts_info.fts_timestamp},
                                {"fts_valid_duration", osd.fts_info.fts_valid_duration},
                                {"fts_verification_code", osd.fts_info.fts_verification_code},
                            };
    json_obj["fts_info"] = fts_info;
}

// 避障状态
void ObstacleAvoidanceStatusToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd) {
    auto obstacle_avoidance = json11::Json::object{
                                    {"downside", osd.obstacle_avoidance.downside},
                                    {"horizon", osd.obstacle_avoidance.horizon},
                                    {"upside", osd.obstacle_avoidance.upside},
                                };
    json_obj["obstacle_avoidance"] = obstacle_avoidance;

}

// 相机状态
void CameraStatusToJson(json11::Json::object& json_obj, const OsdNorthboundMsg& osd) {
    std::vector<json11::Json::object> camera_status_list;
    for (const auto& iter : osd.cameras) {
        auto liveview_world_region = json11::Json::object{
            {"bottom", iter.second.liveview_world_region.bottom},
            {"left", iter.second.liveview_world_region.left},
            {"right", iter.second.liveview_world_region.right},
            {"top", iter.second.liveview_world_region.top},
        };

        auto camera_status = json11::Json::object{
            {"camera_mode", iter.second.camera_mode},
            {"photo_state", iter.second.photo_state},
            {"photo_storage_settings", iter.second.photo_storage_settings},
            {"recording_state", iter.second.recording_state},
            {"video_storage_settings", iter.second.video_storage_settings},
            {"record_time", iter.second.record_time},
            {"payload_index", iter.second.payload_index},
            {"remain_photo_num", iter.second.remain_photo_num},
            {"remain_record_duration", iter.second.remain_record_duration},
            {"screen_split_enable", iter.second.screen_split_enable},
            {"wide_calibrate_farthest_focus_value", iter.second.wide_calibrate_farthest_focus_value},
            {"wide_calibrate_nearest_focus_value", iter.second.wide_calibrate_nearest_focus_value},
            {"wide_exposure_mode", iter.second.wide_exposure_mode},
            {"wide_exposure_value", iter.second.wide_exposure_value},
            {"wide_focus_mode", iter.second.wide_focus_mode},
            {"wide_focus_state", iter.second.wide_focus_state},
            {"wide_focus_value", iter.second.wide_focus_value},
            {"wide_iso", iter.second.wide_iso},
            {"wide_max_focus_value", iter.second.wide_max_focus_value},
            {"wide_min_focus_value", iter.second.wide_min_focus_value},
            {"wide_shutter_speed", iter.second.wide_shutter_speed},
            {"zoom_calibrate_farthest_focus_value", iter.second.zoom_calibrate_farthest_focus_value},
            {"zoom_calibrate_nearest_focus_value", iter.second.zoom_calibrate_nearest_focus_value},
            {"zoom_exposure_mode", iter.second.zoom_exposure_mode},
            {"zoom_exposure_value", iter.second.zoom_exposure_value},
            {"zoom_factor", iter.second.zoom_factor},
            {"zoom_focus_mode", iter.second.zoom_focus_mode},
            {"zoom_focus_state", iter.second.zoom_focus_state},
            {"zoom_focus_value", iter.second.zoom_focus_value},
            {"zoom_iso", iter.second.zoom_iso},
            {"zoom_max_focus_value", iter.second.zoom_max_focus_value},
            {"zoom_min_focus_value", iter.second.zoom_min_focus_value},
            {"zoom_shutter_speed", iter.second.zoom_shutter_speed},
            {"ir_metering_mode", iter.second.ir_metering_mode},
            {"ir_zoom_factor", iter.second.ir_zoom_factor},
            {"liveview_world_region", liveview_world_region},
        };
        // ir_metering_mode
        if (iter.second.ir_metering_mode == 1) { // point
            auto ir_metering_point = json11::Json::object{
                    {"temperature", iter.second.ir_metering_point.temperature},
                    {"x", iter.second.ir_metering_point.x},
                    {"y", iter.second.ir_metering_point.y},
                };
            camera_status["ir_metering_point"] = ir_metering_point;
        } else if (iter.second.ir_metering_mode == 2) { // area
            auto ir_metering_area = json11::Json::object{
                    {"x", iter.second.ir_metering_area.x},
                    {"y", iter.second.ir_metering_area.y},
                    {"height", iter.second.ir_metering_area.height},
                    {"width", iter.second.ir_metering_area.width},
                    {"aver_temperature", iter.second.ir_metering_area.aver_temperature},
                    {"max_temperature_point", json11::Json::object{
                        {"temperature", iter.second.ir_metering_area.max_temperature_point.temperature},
                        {"x", iter.second.ir_metering_area.max_temperature_point.x},
                        {"y", iter.second.ir_metering_area.max_temperature_point.y},
                     }},
                    {"min_temperature_point", json11::Json::object{
                        {"temperature", iter.second.ir_metering_area.min_temperature_point.temperature},
                        {"x", iter.second.ir_metering_area.min_temperature_point.x},
                        {"y", iter.second.ir_metering_area.min_temperature_point.y},
                     }},
                };
            camera_status["ir_metering_area"] = ir_metering_area;
        }
        camera_status_list.emplace_back(camera_status);
    }
    json_obj["cameras"] = camera_status_list;

}

}