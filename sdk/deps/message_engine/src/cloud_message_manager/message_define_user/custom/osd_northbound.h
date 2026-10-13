// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>
#include <json11.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
/// aircraft osd
// 上云物模型中飞行器状态 mode_code 字段枚举
enum AircraftModeCode {
    STANDBY =             0,  // 待机
    ENGINE_START =        1,  // 起飞准备（刚开始起桨，尚未进入平稳怠速阶段）
    IDLING =              2,  // 怠速（起飞准备完成，飞机在地面怠速转桨，原地待命）
    FLY_BY_MANUAL =       3,  // 手动飞行
    AUTO_TAKE_OFF =       4,  // 自动起飞（App上一键起飞或执行航线任务的起飞动作）
    AIRYWAY =             5,  // 航线飞行
    PANORAMA =            6,  // 全景拍照
    SMART_TRACK =         7,  // 智能跟随
    ADS_B =               8,  // ADS-B 强制接管后进行躲避
    AUTO_GO_HOME =        9,  // 自动返航
    AUTO_LANDING =        10, // 自动降落
    FORCE_LANDING =       11, // 强制降落（严重低电量）
    CRASH_LANDING =       12, // 迫降（三桨降落之内的飞机发生异常的情况下紧急着陆）
    UPGRANDING =          13, // 升级中
    DISCONNECTED =        14, // 未连接飞行器
    APAS =                15, // 高级辅助飞行/APAS
    JOYSTICK =            16, // 遥杆模式
    COMMANDER =           17, // 指令飞行
    QUICKTAKEOFF =        18, // 快速起飞辅助模式(空中RTK收敛模式)
    VISUAL_EXPLORATION =  19, // 视觉踏勘选址模式
    POI_CIRCLE =          20, // POI环绕
    DEPARTURE_WAYLINE =   21, // 离场航线
};

struct TrackInfo {
    int track_target_mode = 0;
    double track_longitude = 0;
    double track_latitude = 0;
    double track_altitude = 0;
};

struct PayloadInfo {
    double gimbal_pitch = 0.0;
    double gimbal_roll = 0.0;
    double gimbal_yaw = 0.0;
    double gimbal_yaw_amend_value = 0.0;
    std::string payload_index;
    // 激光
    bool laser_on = false;
    double measure_target_altitude = 0.0;
    double measure_target_longitude = 0.0;
    double measure_target_latitude = 0.0;
    double measure_target_distance = 0.0;
    int measure_target_error_state = 0.0;
    // track
    bool track_on = false;
    std::vector<TrackInfo> track_info_list;
    // thermal
    std::vector<int> thermal_supported_palette_styles{};
    int thermal_current_palette_style = 0;
    int thermal_gain_mode = 0;
    int thermal_isotherm_state = 0;
    int thermal_isotherm_upper_limit = 0;
    int thermal_isotherm_lower_limit = 0;
    double thermal_global_temperature_min = 0.0;
    double thermal_global_temperature_max = 0.0;
    double zoom_factor = 0.0;
};

struct BatteryInfo {
    bool existed = false;
    int index = 0;
    int capacity_percent = 0;
    std::string sn;
    int type = 0;
    int sub_type = 0;
    std::string firmware_version;
    int loop_times = 0;
    int voltage = 0;     // 单位mV
    double temperature = 0;
    int high_voltage_storage_days = 0;
    int soh_percent = 0; //电池健康度
};

struct DistanceLimitStatus {
    int distance_limit;
    int is_near_distance_limit;
    int state;
};

enum class MaintainType {
    AIRCRAFT_BASIC_MAINTAIN = 1,
    AIRCRAFT_ROUTINE_MAINTAIN = 2,
    AIRCRAFT_DEEP_MAINTAIN = 3,
    UNKNOWN = 0xFFFF,
};

struct MaintainStatus {
    int last_maintain_flight_sorties = 0;
    int last_maintain_flight_time = 0;
    double last_maintain_time = 0.0;
    int last_maintain_type = 0;
    int state = 0;
};

struct FtsInfo {
    std::string fts_firmware_version;
    std::string fts_sn;
    int fts_status = 0;
    double fts_timestamp = 0.0;
    int fts_valid_duration = 0;
    std::string fts_verification_code;
};

struct ObstacleAvoidanceStatus {
    int downside = 0;
    int horizon = 0;
    int upside = 0;
};

struct IrMeteringPoint {
    double temperature = 0.0;
    double x = 0.0;
    double y = 0.0;
};

struct IrMeteringArea {
    double x = 0.0;
    double y = 0.0;
    double width = 0.0;
    double height = 0.0;
    double aver_temperature = 0.0;
    IrMeteringPoint min_temperature_point;
    IrMeteringPoint max_temperature_point;
};

struct LiveviewWorldRegion {
    double bottom = 0.0;
    double left = 0.0;
    double right = 0.0;
    double top = 0.0;
};

struct CameraStatus {
    int camera_mode = 0;
    int photo_state = 0;
    std::vector<std::string> photo_storage_settings;
    int recording_state = 0;
    std::vector<std::string> video_storage_settings;
    int record_time = 0;
    std::string payload_index;
    int remain_photo_num = 0;
    int remain_record_duration = 0;
    int screen_split_enable = 0;
    int wide_calibrate_farthest_focus_value = 0;
    int wide_calibrate_nearest_focus_value = 0;
    int wide_exposure_mode = 0;
    int wide_exposure_value = 0;
    int wide_focus_mode = 0;
    int wide_focus_state = 0;
    int wide_focus_value = 0;
    int wide_iso = 0;
    int wide_max_focus_value = 0;
    int wide_min_focus_value = 0;
    int wide_shutter_speed = 0;
    int zoom_calibrate_farthest_focus_value = 0;
    int zoom_calibrate_nearest_focus_value = 0;
    int zoom_exposure_mode = 0;
    int zoom_exposure_value = 0;
    int zoom_factor = 0;
    int zoom_focus_mode = 0;
    int zoom_focus_state = 0;
    int zoom_focus_value = 0;
    int zoom_iso = 0;
    int zoom_max_focus_value = 0;
    int zoom_min_focus_value = 0;
    int zoom_shutter_speed = 0;
    int ir_metering_mode = 0;
    IrMeteringPoint ir_metering_point;
    IrMeteringArea ir_metering_area;
    int ir_zoom_factor = 0;
    LiveviewWorldRegion liveview_world_region;
};

struct OsdNorthboundMsg {
    /// MODE_CODE = 0 飞行器模式状态
    AircraftModeCode mode_code = AircraftModeCode::STANDBY;
    /// PAYLOAD = 1 负载状态
    std::map<int, PayloadInfo> payload_index_to_info;
    /// GEAR = 2 飞行器模式状态
    int gear = 0;
    /// BATTERY = 3 飞机电池信息
    int battery_total_num = 0;
    std::map<int, BatteryInfo> battery_index_to_info;
    int capacity_percent = 0;
    int remain_flight_time = 0;
    int return_home_power = 0;
    int landing_power = 0;
    /// AIRCRAFT_SPEED = 4 飞机速度
    double aircraft_vertical_speed = 0;
    double aircraft_horizontal_speed = 0;
    /// AIRCRAFT_LOCATION = 5 飞机经纬度
    double aircraft_location_longitude = 0;
    double aircraft_location_latitude = 0;
    double aircraft_location_altitude = 0;
    /// AIRCRAFT_ABSOLUTE_HEIGHT = 6 飞行器绝对高度
    double rel_height = 0; // 气压计计算出来的相对起飞点的抬升高度
    double take_off_height = 0;
    double aircraft_absolute_height = 0;
    /// AIRCRAFT_RELATIVE_HEIGHT = 7 飞行器相对起飞点高度
    double aircraft_relative_height = 0;
    /// AIRCRAFT_ATTITUDE = 8 飞机姿态
    double aircraft_attitude_pitch = 0;
    double aircraft_attitude_roll = 0;
    double aircraft_attitude_yaw = 0;
    /// AIRCRAFT_HOME_LOCATION = 9 飞机Home点经纬度
    double aircraft_home_longitude = 0;
    double aircraft_home_latitude = 0;
    /// AIRCRAFT_HOME_DISTANCE = 10 飞机Home点距离
    double aircraft_home_distance = 0;
    /// WIND_INFO = 11 风速信息
    int wind_info_speed = 0;
    int wind_info_direction = 0;
    /// POSITION_STATE = 12 搜星状态
    int is_fixed = 0;
    int quality = 0;
    int gps_number = 0;
    int rtk_number = 0;
    /// TOTAL_FLIGHT_TIME = 13 飞行器总飞行时间
    double total_flight_time = 0;
    /// TOTAL_FLIGHT_DISTANCE = 14 飞行器总飞行里程
    double total_flight_distance = 0;
    /// TOTAL_FLIGHT_SORTIES = 15 飞行器总飞行次数
    int total_flight_sorties = 0;
    /// STORAGE = 16 存储容量
    int storage_total = 0;
    int storage_used = 0;
    /// TRACK_ID = 17 架次ID
    std::string track_id;
    /// AIRCRAFT_FIRMWARE_VERSION = 19 飞机固件版本
    std::string aircraft_firmware_version;
    /// 激活时间
    double activation_time;
    /// 信号最佳的机场SN
    std::string best_link_gateway;
    /// 国家码
    std::string country;
    /// 限远
    DistanceLimitStatus distance_limit_status;
    /// 限高
    int height_limit = 0;
    int is_near_height_limit = 0;
    /// 限飞
    int is_near_area_limit = 0;
    /// 保养信息
    std::vector<MaintainStatus> maintain_status;
    /// 夜航灯
    int night_lights_state = 0;
    /// 失联行为
    int rc_lost_action = 0;
    /// RID
    bool rid_state = false;
    /// 返航高度
    int rth_altitude = 0;
    /// FTS
    FtsInfo fts_info;
    /// 避障
    ObstacleAvoidanceStatus obstacle_avoidance;
    /// 相机
    std::map<int, CameraStatus> cameras;

};
// OsdNorthboundMsg to json
json11::Json OsdNorthboundMsgToJson(const OsdNorthboundMsg& osd);
// Osd SingleData To Json
void OsdSingleDataToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 负载状态
void PayloadToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 飞机电池信息
void BatteryToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 搜星状态
void PositionStateToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 存储容量
void StorageToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 限远状态
void DistanceLimitStatusToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 保养状态
void MaintainStatusToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 飞行终止系统状态
void FtsInfoToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 避障状态
void ObstacleAvoidanceStatusToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
// 相机状态
void CameraStatusToJson(json11::Json::object &json_obj, const OsdNorthboundMsg& osd);
///------ 用户自定义数据结构 end ------

class OsdNorthbound final : public MessageV1 {
public:
    OsdNorthbound();
    explicit OsdNorthbound(const OsdNorthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    OsdNorthboundMsg msg;
};

}