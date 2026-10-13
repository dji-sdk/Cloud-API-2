// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <string>
#include <AIGCJson.hpp>
#include <vector>
#include <unordered_map>
#include <optional>
#include "response_message.h"

namespace flight_hub_sdk {

/// GetDeviceTopoRequest
struct DeviceTopoRequest {
    std::vector<std::string> sn_list;
    AIGC_JSON_HELPER(sn_list);
};

/// LiveStreamStartRequest
struct LiveStreamStartRequest {
    std::string sn;
    std::string camera;
    std::string video;
    std::string gateway_sn;
    AIGC_JSON_HELPER(sn, camera, video, gateway_sn);
};

/// LiveStreamStateUpdateRequest
struct LiveStreamStateUpdateRequest {
    std::string sn;
    std::string camera;
    std::string video;
    std::string uid;
    int state;
    AIGC_JSON_HELPER(sn, camera, video, uid, state);
};

/// LiveStreamStateUpdateTokenItem
struct LiveStreamStateUpdateTokenItem {
    std::string sn;
    std::string camera;
    std::string user_id;
    AIGC_JSON_HELPER(sn, camera, user_id);
};

/// LiveStreamStateUpdateTokenRequest
struct LiveStreamStateUpdateTokenRequest {
    std::vector<LiveStreamStateUpdateTokenItem> list;
    AIGC_JSON_HELPER(list);
};

/// ChangeDockCameraPositionRequest 切换机场舱内/舱外摄像头
struct ChangeDockCameraPositionRequest {
    std::string sn;
    std::string camera;
    std::string video;
    std::string gateway_sn;  // 可选，网关/机场 SN
    int camera_position;     // 0: 主/舱内, 1: 辅助1/舱外, 2: 辅助2
    AIGC_JSON_HELPER(sn, camera, video, gateway_sn, camera_position);
};

/// StartLiveRecordRequest / StopLiveRecordRequest — LiveRecordStartRequest / LiveRecordStopRequest
/// HTTP: POST /pf-live/api/v1/record/start|stop
struct StartLiveRecordRequest {
    // === 必选字段 — 始终序列化 ===
    std::string sn;      // 设备序列号（必填）
    std::string camera;  // 摄像头标识（必填）
    std::string video;   // 视频流标识（必填）

    AIGC_JSON_HELPER(sn, camera, video)

    // === 可选字段 — nullopt 时跳过 ===
    std::optional<std::string> gateway_sn;  // 网关 SN；空则不传，由服务端自动选择
    std::optional<std::string> time_zone;   // 时区；如 Asia/Shanghai（仅 start 使用）

    AIGC_JSON_HELPER_OPTIONAL_SKIP(gateway_sn, time_zone);
};

/// 获取在线观众数量 - 请求体（POST /pf-live/api/v1/stream/audience-count）
/// 注意：后端要求不传 camera_list，仅传 sn
struct AudienceCountRequestItem {
    std::string sn;
    AIGC_JSON_HELPER(sn);
};

struct AudienceCountGroupRequest {
    std::string level_1_group;
    std::string level_2_group;
    AIGC_JSON_HELPER(level_1_group, level_2_group);
};

struct AudienceCountRequest {
    std::vector<AudienceCountRequestItem> device_list;
    AudienceCountGroupRequest group;
    AIGC_JSON_HELPER(device_list, group);
};

/// OpenDataChannelRequest
/// 对应 POST /paas/api/v1/rt-channels/{sn}
/// Body 只含 provider（sn 走 URL 路径参数），drone_sn/gateway_sn 保留用于 SDK 内部逻辑
struct OpenDataChannelRequest {
    std::string drone_sn;      // SDK 内部使用（拼 URL 用）
    std::string gateway_sn;    // SDK 内部使用（不进 Body）
    std::string provider;      // 进 Body（可选，留空由服务端默认选择 provider）
    AIGC_JSON_HELPER(provider);   // Body 只序列化 provider
};

/// FlyToPointRequest
struct FlyToPointRequestPoints {
    double latitude;
    double longitude;
    double height;
    AIGC_JSON_HELPER(latitude, longitude, height);
};

struct FlyToPointRequest {
    std::string device_sn;
    FlyToPointRequestPoints start_point;
    std::vector<FlyToPointRequestPoints> points;
    int max_speed;
    bool need_verify_flight_control = false;
    AIGC_JSON_HELPER(device_sn, start_point, points, max_speed, need_verify_flight_control);
};

/// UpdateFlyToPointsRequest 更新飞向目标点
/// 对应 PUT /control/api/v1/manual/flight/fly-to-points
struct UpdateFlyToPointsRequest {
    std::string device_sn;                           // 设备序列号
    std::string fly_to_id;                           // 当前执行中的 FlyTo 任务 ID
    FlyToPointRequestPoints start_point;             // 起始点（当前飞机位置）
    std::vector<FlyToPointRequestPoints> points;     // 目标点数组（至少 1 个）
    int max_speed = 0;                               // 最大飞行速度 m/s
    AIGC_JSON_HELPER(device_sn, fly_to_id, start_point, points, max_speed);
};

/// CreateInFlightWaylineTaskRequest 创建空中下发航线任务
struct CreateInFlightWaylineTaskRequest {
    // === 必选字段 — 始终序列化 ===
    std::string drone_sn;      // 无人机 SN
    std::string gateway_sn;    // 网关(机场) SN
    std::string wayline_uuid;  // 航线 UUID
    int32_t rth_altitude = 0;  // 返航高度(米)，飞控验证要求≥2

    AIGC_JSON_HELPER(drone_sn, gateway_sn, wayline_uuid, rth_altitude)

    // === 可选字段 — nullopt 时跳过，不输出到 JSON ===
    std::optional<int32_t> wayline_precision_type;    // 航线精度类型
    std::optional<int32_t> rth_mode;                    // 返航高度模式
    std::optional<int32_t> out_of_control_action;       // 飞控失控动作: 0-返航 1-悬停 2-降落
    std::optional<int32_t> exit_wayline_when_rc_lost; // 航线失控动作: 0-继续执行 1-执行飞控失控动作
    std::optional<double> wayline_duration;             // 任务预估时长
    std::optional<double> wayline_length;               // 航线长度
    std::optional<std::string> task_name;              // 任务名称
    std::optional<bool> fast_reconstruction;           // 快速建模

    AIGC_JSON_HELPER_OPTIONAL_SKIP(wayline_precision_type, rth_mode, out_of_control_action,
                                     exit_wayline_when_rc_lost, wayline_duration, wayline_length,
                                     task_name, fast_reconstruction);
};

/// ControlInFlightWaylineTaskRequest 控制空中航线任务
struct ControlInFlightWaylineTaskRequest {
    std::string drone_sn;  // 无人机 SN
    int32_t action = 0;    // 控制动作: 1-暂停 2-恢复 3-取消
    AIGC_JSON_HELPER(drone_sn, action);
};

/// POIEnterRequest
struct POICenterPoint {
    double latitude;
    double longitude;
    double height;
    AIGC_JSON_HELPER(latitude, longitude, height);
};

struct POIEnterRequest {
    std::string device_sn;
    std::string payload_index;
    POICenterPoint poi_center_point;
    double speed;
    double circle_radius;
    bool need_verify_flight_control = false;
    AIGC_JSON_HELPER(device_sn, payload_index, poi_center_point, speed, circle_radius, need_verify_flight_control);
};

/// POIExitRequest
struct POIExitRequest {
    std::string device_sn;
    AIGC_JSON_HELPER(device_sn);
};

/// POISpeedUpdateRequest
struct POISpeedUpdateRequest {
    std::string device_sn;
    double speed;
    AIGC_JSON_HELPER(device_sn, speed);
};

/// GetPOITaskRequest
struct GetPOITaskRequest {
    std::string device_sn;
    AIGC_JSON_HELPER(device_sn);
};

/// AcquireCtrlAuthorityRequest
struct AcquireCtrlAuthorityRequest {
    std::string drone_sn;
    std::vector<std::string> control_keys;
    std::string gateway_sn;
    AIGC_JSON_HELPER(drone_sn, control_keys, gateway_sn);
};

/// ReleaseCtrlAuthorityRequest
struct ReleaseCtrlAuthorityRequest {
    std::string drone_sn;
    std::vector<std::string> control_keys;
    std::string gateway_sn;
    AIGC_JSON_HELPER(drone_sn, control_keys, gateway_sn);
};

struct NonRealtimeCmdBaseRequest {
    std::string sn;
    std::string type; // flight/payload
    std::string method;
    AIGC_JSON_HELPER(sn, type, method);
};

/// FlightTaskPauseRequest
struct FlightTaskPauseRequest : NonRealtimeCmdBaseRequest {
    FlightTaskPauseRequest() {
        type = "flight";
        method = "flighttask_pause";
    }
    AIGC_JSON_HELPER_BASE((NonRealtimeCmdBaseRequest *)this)
};

/// FlightTaskResumeRequest
struct FlightTaskResumeRequest : NonRealtimeCmdBaseRequest {
    FlightTaskResumeRequest() {
        type = "flight";
        method = "flighttask_recovery";
    }
    AIGC_JSON_HELPER_BASE((NonRealtimeCmdBaseRequest *)this)
};

/// ReturnHomeRequest
struct ReturnHomeRequest : NonRealtimeCmdBaseRequest {
    ReturnHomeRequest() {
        type = "flight";
        method = "return_home";
    }
    AIGC_JSON_HELPER_BASE((NonRealtimeCmdBaseRequest *)this)
};

/// ReturnHomeCancelRequest
struct ReturnHomeCancelRequest : NonRealtimeCmdBaseRequest {
    ReturnHomeCancelRequest() {
        type = "flight";
        method = "return_home_cancel";
    }
    AIGC_JSON_HELPER_BASE((NonRealtimeCmdBaseRequest *)this)
};

/// LookAtTargetRequest
struct LookAtTargetRequestData {
    std::string payload_index;
    bool locked;
    double longitude;
    double latitude;
    double height;
    AIGC_JSON_HELPER(payload_index, locked, longitude, latitude, height);
};
struct LookAtTargetRequest : NonRealtimeCmdBaseRequest {
    LookAtTargetRequest() {
        type = "payload";
        method = "camera_look_at";
    }
    LookAtTargetRequestData data;
    AIGC_JSON_HELPER(data);
    AIGC_JSON_HELPER_BASE((NonRealtimeCmdBaseRequest *)this)
};

/// Psdk
struct PsdkSpeakerResourceListRequest {
    std::string file_type;  // "audio" or "tts"
    AIGC_JSON_HELPER(file_type);
};

struct CreateSpeakerResourceRequest {
    std::string name;
    std::string file_type;  // "audio" or "tts"
    std::string object_key; // required if file_type is "audio"
    int64_t duration = 0;   // required if file_type is "audio" and create_scene is "record"
    std::string content;    // required if file_type is "tts"
    std::string create_scene; // "import" or "record"
    bool need_opus = false; // whether to convert to opus format
    int64_t size = 0;       // required if file_type is "audio"
    bool visible = false;   // required if file_type is "audio"
    AIGC_JSON_HELPER(name, file_type, object_key, duration, content, create_scene, need_opus, size, visible);
};

struct EditSpeakerResourceRequest {
    std::string id;     // 服务端期望 string 类型
    std::string name;       // required
    std::string file_type;  // "audio" or "tts"
    bool visible = false;   // required if file_type is "audio"
    std::string content;    // required if file_type is "tts"
    AIGC_JSON_HELPER(id, name, file_type, visible, content);
};

struct PsdkWidgetResourceRequest {
    std::vector<std::string> object_keys;  // required, dive, required
    AIGC_JSON_HELPER(object_keys);
};

/// DroneTakeoffRequest — CommandRequest / DroneTakeoffRequest（字段一致）
/// HTTP: POST /atc/api/v1/flight-tasks/command（SDK）或 /drone-take-off
struct DroneTakeoffRequest {
    // === 必选字段 — 始终序列化 ===
    std::string device_sn;                 // 设备序列号（必填）
    std::string takeoff_mode;              // 起飞模式 Takeoff / TakeoffWithFlyTo（必填）
    double security_takeoff_height = 0;    // 安全起飞高度（米），范围 8-1500（必填）
    double target_height = 0;              // 目标高度（米），范围 2-10000（必填）
    double target_latitude = 0;            // 目标纬度
    double target_longitude = 0;           // 目标经度
    int max_speed = 0;                     // 最大速度（米/秒）（必填）
    std::string out_of_control_action;     // 失控动作 Hover / ReturnHome / Continue（必填）
    int rth_altitude = 0;                  // 返航高度（米），范围 15-1500（必填）

    AIGC_JSON_HELPER(device_sn, takeoff_mode, security_takeoff_height, target_height, target_latitude,
                     target_longitude, max_speed, out_of_control_action, rth_altitude)

    // === 可选字段 — nullopt 时跳过 ===
    std::optional<int> rth_mode;                      // 返航模式：0-智能高度，1-设定高度
    std::optional<int> commander_flight_mode;         // 指点飞行模式：0-平稳，1-运动
    std::optional<double> commander_flight_height;    // 指点飞行高度（米），20-1500
    std::optional<std::string> cloud_to_cloud_id;     // 云云对接存储桶 ID
    std::optional<bool> need_verify_flight_control;   // 是否需要验证飞行控制权

    AIGC_JSON_HELPER_OPTIONAL_SKIP(rth_mode, commander_flight_mode, commander_flight_height,
                                   cloud_to_cloud_id, need_verify_flight_control);
};

/// DockSupplementLightRequest
struct DockSupplementLightRequest {
    std::string device_sn;
    AIGC_JSON_HELPER(device_sn);
};

/// DockAirConditionerRequest
struct DockAirConditionerRequestData {
    int mode;
    AIGC_JSON_HELPER(mode);
};

struct DockAirConditionerRequest {
    std::string device_sn;
    DockAirConditionerRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

/// DockCoverRequest
struct DockCoverRequestData {
    int mode;
    AIGC_JSON_HELPER(mode);
};

struct DockCoverRequest {
    std::string device_sn;
    DockCoverRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

/// GenerateTryFlightWaylineRequest (试飞航线生成)
struct TryFlightWaylinePointRequest {
    double lng;
    double lat;
    double alt;
    AIGC_JSON_HELPER(lng, lat, alt);
};

struct GenerateTryFlightWaylineRequest {
    std::string name;
    std::string device_type;
    TryFlightWaylinePointRequest target_point;
    TryFlightWaylinePointRequest observer_point;
    AIGC_JSON_HELPER(name, device_type, target_point, observer_point);
};

/// CreateTestFlightTaskRequest (试飞航线下发)
struct CreateTestFlightTaskRequest {
    std::string name;
    std::string wayline_uuid;
    std::string sn;
    std::string landing_dock_sn;
    int task_type = 1;  // 立即任务
    int out_of_control_action_in_flight = 1;
    int rth_mode = 1;
    int repeat_type = 0;
    std::string time_zone;
    std::string default_name;
    int wayline_precision_type = 1;
    double wayline_duration = 0.0;  // 航线预估执行时间，单位秒
    int rth_altitude = 100;  // 返航高度，单位米
    std::string resumable_status;
    std::string media_folder_id_paths;
    int device_type = 0;
    
    AIGC_JSON_HELPER(name, wayline_uuid, sn, landing_dock_sn, task_type, out_of_control_action_in_flight, 
                     rth_mode, repeat_type, time_zone, default_name, wayline_precision_type, wayline_duration,
                     rth_altitude, resumable_status, media_folder_id_paths, device_type);
};


/// GetMediaFileListRequest (媒体文件列表，query 参数)
struct GetMediaFileListRequest {
    int parent_id = 0;       // 0 或不传表示根目录
    int page = 1;
    int size = 30;
    bool get_sub_file = false;
    std::string sort_by;    // 如 "-create_at"
    AIGC_JSON_HELPER(parent_id, page, size, get_sub_file, sort_by);
};

/// DroneDownloadEphemerisRequestData 请求的 data 参数;
struct DroneDownloadEphemerisRequestData {
    std::string ephemeris_url;          // 星历 url;
    std::string ephemeris_file_name;    // 星历文件名;

    AIGC_JSON_HELPER(ephemeris_url, ephemeris_file_name);
};


/// DroneDownloadEphemerisRequest 请求参数;
struct DroneDownloadEphemerisRequest {
    std::string sn;                              // 网关 SN;
    std::string type = "flight";                 // 设备类型;
    std::string method = "ephemeris_download";   // 方法名;
    DroneDownloadEphemerisRequestData data;

    AIGC_JSON_HELPER(sn, type, method, data);
};


/// DroneInOutWaylineDetectRequest (开始/暂停)进离场任务 请求参数;
struct DroneInOutWaylineDetectRequestData {
    std::string task_id;            // 任务id;
    int task_cmd;                   // 0 开始勘探选址; 1 终止勘探选址; 2 停止离场点视觉勘探; 3 开始离场点视觉勘探;
    bool is_auto_visual_scout;      // 是否自动勘探;
    double scout_relative_height;   // 离场点相对起飞高度 自动勘探需填;
    double patrol_relative_height;  // 方兴航线执行相对起飞高度;

    AIGC_JSON_HELPER(task_id, task_cmd, is_auto_visual_scout, scout_relative_height, patrol_relative_height);
};

struct DroneInOutWaylineDetectRequest {
    std::string sn;
    std::string type = "flight";            // "flight", "payload", "psdk";
    std::string method = "visual_scout_cmd";

    DroneInOutWaylineDetectRequestData data;

    AIGC_JSON_HELPER(sn, type, method, data);
};


/// DroneInOutWaylineFlightSettingRequest 进离场航线飞行设置;
struct DroneInOutWaylineFlightSettingRequest {
    std::string sn;                // [必须参数], 设备序列号;
    double min_return_home_height;
    int lost_contact_action;       // 0 悬停; 1 返航; 2 自动降落;
    double max_height_limit;
    double max_distance_limit;


    AIGC_JSON_HELPER(sn, min_return_home_height, lost_contact_action, max_height_limit, max_distance_limit);
};


/// LiveQualitySettingRequest  直播画质设置;
struct LiveQualitySettingRequest {
    std::string sn;          // [必须参数], 设备序列号;
    std::string camera;      // [必须参数], 摄像头标识;
    std::string video;       // [必须参数], 视频流标识;
    std::string gateway_sn;  // [非必须  ], 网关设备序列号;
    int video_quality;       // [必须参数], 视频质量等级(0 自适应; 1 标清; 4 超清);


    AIGC_JSON_HELPER(sn, camera, video, gateway_sn, video_quality);
};

struct CommonRequestWithDeviceSn {
    std::string device_sn;
    AIGC_JSON_HELPER(device_sn);
};


struct CommonRequestWithDeviceSNs {
    std::vector<std::string> devices;

    // register class-members;
    AIGC_JSON_HELPER(devices);
};


struct CommonMediaServiceRequestWithFileIDs {
    std::vector<int64_t> file_ids;

    // register class-members;
    AIGC_JSON_HELPER(file_ids);
};


struct DockSilentModeRequestData {
    int silent_mode;
    AIGC_JSON_HELPER(silent_mode);
};

struct DockSilentModeRequest {
    std::string device_sn;
    DockSilentModeRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

// ---------- 航线任务 HTTP 请求体（/atc/api/v1/flight-tasks）----------
struct CreateFlightTaskBreakPointRequest {
    int index = 0;
    int state = 0;
    double progress = 0.0;
    int wayline_id = 0;
    int break_reason = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    double attitude_head = 0.0;
    AIGC_JSON_HELPER(index, state, progress, wayline_id, break_reason,
                     latitude, longitude, height, attitude_head);
};

struct CreateFlightTaskRequest {
    // === 必选字段 — 始终序列化 ===
    std::string name;
    std::string sn;
    int task_type = 1;
    std::string time_zone;
    std::string wayline_uuid;
    int out_of_control_action_in_flight = 1;
    double rth_altitude = 0;

    AIGC_JSON_HELPER(name, sn, task_type, time_zone, wayline_uuid,
                     out_of_control_action_in_flight, rth_altitude)

    // === 可选字段 — nullopt 时跳过，不输出到 JSON ===
    std::optional<int> rth_mode;
    std::optional<int64_t> begin_at;
    std::optional<int64_t> end_at;
    std::optional<int> device_type;
    std::optional<std::string> landing_dock_sn;
    std::optional<int> min_battery_capacity;
    std::optional<int> wayline_precision_type;
    std::optional<std::vector<int>> wayline_segment_indexes;
    std::optional<double> wayline_duration;
    std::optional<std::string> uuid;
    std::optional<std::vector<std::string>> tags;
    std::optional<bool> is_break_point_resume;
    std::optional<CreateFlightTaskBreakPointRequest> break_point;
    std::optional<int64_t> signal_type;
    std::optional<std::string> resumable_status;

    AIGC_JSON_HELPER_OPTIONAL_SKIP(rth_mode, begin_at, end_at, device_type,
                                   landing_dock_sn, min_battery_capacity,
                                   wayline_precision_type, wayline_segment_indexes,
                                   wayline_duration, uuid, tags,
                                   is_break_point_resume, break_point, signal_type,
                                   resumable_status)
};

struct UpdateFlightTaskStatusRequest {
    std::string task_uuid;
    int status = 0;
    AIGC_JSON_HELPER(task_uuid, status);
};

struct BatchUpdateFlightTaskStatusRequest {
    std::vector<std::string> task_uuids;
    int status = 0;
    AIGC_JSON_HELPER(task_uuids, status);
};

struct DeviceRecommendationPositionBody {
    double height = 0;
    double latitude = 0;
    double longitude = 0;
    AIGC_JSON_HELPER(height, latitude, longitude);
};

struct DeviceRecommendationRequest {
    std::vector<std::string> sn;
    std::vector<std::string> device_model_keys;
    int strategy_id = 1;
    std::string prj_uuid;
    std::string wayline_uuid;
    int wayline_height_type = 0;
    DeviceRecommendationPositionBody fly_to_position;
    DeviceRecommendationPositionBody wayline_start_position;
    DeviceRecommendationPositionBody wayline_end_position;
    AIGC_JSON_HELPER(sn, device_model_keys, strategy_id, prj_uuid, wayline_uuid, wayline_height_type,
                     fly_to_position, wayline_start_position, wayline_end_position);
};

struct UpdateTcasRequest {
    std::vector<std::string> center_sn;
    AIGC_JSON_HELPER(center_sn);
};

struct StartRoamingRequest {
    std::string drone_sn;
    std::string flight_id;
    int source_task_type = 1;
    AIGC_JSON_HELPER(drone_sn, flight_id, source_task_type);
};

struct SetRoamingStationSnRequest {
    std::string drone_sn;
    std::vector<std::string> station_sns;
    AIGC_JSON_HELPER(drone_sn, station_sns);
};

// 安全服务: signal-maps/search(信号地图查询) 请求参数;
struct SearchSignalMapsRequest {
    std::vector<std::string> device_sns;    // 设备 SN;
    std::vector<std::string> data_types;    // 待查询的数据类型, 比如 ["4g", "sdr"]; 4g信号; sdr信号;

    AIGC_JSON_HELPER(device_sns, data_types);
};


// 自定区域: 属性;
struct ContentsProperties {
    std::string color;             // 颜色, 颜色值用于方便在地图上面区分不同区域, 有效值比如 "#FF0000"
    int width = 0;                 // 宽度, 多边形/线状类型需要传入, 用于表示周边范围
    bool clampToGround = false;    // 服务端(旧协议设计上字段是 "clampToGround");

    // register class-members;
    AIGC_JSON_HELPER(color, width, clampToGround);
};


// 自定义飞行区域:Geometry 参数;
struct ContentsGeometry {
    // type: "Circle", 圆形: 限飞区和作业区支持;
    // type: "Polygon", 多边形: 限飞区和作业区支持;
    // type: "Polyline", 自定义(折线): 折线仅禁降区支持;  
    std::string type;
    std::vector<std::vector<std::vector<double>>> coordinates;  // [折线] [多边形] 顶点坐标;

    std::vector<double> center;  // 创建圆形区域的时候传入: 圆心坐标;
    double radius = 0;  // 半径, 创建圆形区域的时候传入;

    // register class-members;
    AIGC_JSON_HELPER(type, coordinates, center, radius);
};


// 自定义飞行区域: 飞行区内容 Contents;
struct FlightAreasContents {
    std::string        type = "Feature";    // 固定值 "Feature";
    ContentsProperties properties;
    ContentsGeometry   geometry;

    // register class-members;
    AIGC_JSON_HELPER(type, properties, geometry);
};


// 创建[自定义飞行区] 请求参数;
struct CreateCustomizedFlightAreasRequest {
    std::string         name;            // name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区";
    std::string         type;            // type, 飞行区类型, {"nfz" 限飞区; "dfence" 作业区; "noland" 禁降区}";
    FlightAreasContents content;         // content, 飞行区内容;
    std::string         created_by;      // created_by, 创建用户 ID;

    // register class-members;
    AIGC_JSON_HELPER(name, type, content, created_by);
};


// 更新[自定义飞行区] 请求参数;
struct UpdateCustomizedFlightAreasRequest {
    std::string         name;            // name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区";
    FlightAreasContents content;         // content, 飞行区内容;
    std::string         update_by;       // update_by, 更新用户 ID;

    // register class-members;
    AIGC_JSON_HELPER(name, content, update_by);
};


struct ModelItemInfo {
    std::string uuid;
    std::string object_key;
    std::string file_type;

    // register class-members;
    AIGC_JSON_HELPER(uuid, object_key, file_type);
};


// 更新离线地图模型;
struct UpdateOfflineMapsRequest {
    std::vector<ModelItemInfo> model_3d_items;
    std::vector<ModelItemInfo> model_2d_items;

    // register class-members;
    AIGC_JSON_HELPER(model_3d_items, model_2d_items);
};


struct SetOfflineMapsRequest {
    bool offline_map_enable;  // 离线地图开关;

    // register class-members;
    AIGC_JSON_HELPER(offline_map_enable);
};

// 导出飞行记录;
struct ExportFlightRecordsRequest {
    std::string content_type;                    // 导出类型, 比如 "summary", "details";
    std::vector<std::string> file_type;          // 文件类型, 比如 ["Excel"];
    std::string file_name;                       // 文件名;
    std::vector<std::string> flight_task_ids;    // 飞行任务 id 列表;
    std::vector<std::string> metrics;            // 指标列表;
    std::string time_zone;                       // 时区, 比如 "Asia/Shanghai";
    std::string locale;                          // 语言, 比如 "zh";

    // register class-members;
    AIGC_JSON_HELPER(content_type, file_type, file_name, flight_task_ids, metrics, time_zone, locale);
};


struct DroneSdrWorkModeRequestData {
    int link_workmode;
    AIGC_JSON_HELPER(link_workmode);
};

struct DroneSdrWorkModeRequest {
    std::string device_sn;
    DroneSdrWorkModeRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct CustomCalibrationData {
    std::string host;
    int port;
    std::string account;
    std::string password;
    std::string mount_point;
    int version;
    AIGC_JSON_HELPER(host, port, account, password, mount_point, version);
};

struct NetworkRtkCalibrationRequest {
    std::string device_sn;
    std::string device_module;
    int calibration_type;
    CustomCalibrationData custom_calibration_data;
    AIGC_JSON_HELPER(device_sn, device_module, calibration_type, custom_calibration_data);
    AIGC_JSON_HELPER_RENAME("device_sn", "module", "calibration_type", "custom_calibration_data");
};

struct ManualCalibrationData {
    double height;
    double latitude;
    double longitude;
    AIGC_JSON_HELPER(height, latitude, longitude);
};

struct ManualRtkCalibrationRequest {
    std::string device_sn;
    std::string device_module;
    int calibration_type;
    ManualCalibrationData manual_calibration_data;
    AIGC_JSON_HELPER(device_sn, device_module, calibration_type, manual_calibration_data);
    AIGC_JSON_HELPER_RENAME("device_sn", "module", "calibration_type", "manual_calibration_data");
};

struct DeviceLogListRequest {
    std::string device_sn;
    std::vector<int> module_list;
    AIGC_JSON_HELPER(device_sn, module_list);
};

struct DeviceLogReportLogItem {
    int boot_index = 0;
    int64_t start_time = 0;
    int64_t end_time = 0;
    int64_t size = 0;
    std::string md5;
    AIGC_JSON_HELPER(boot_index, start_time, end_time, size, md5);
};

struct DeviceLogReportLogListItem {
    std::string device_sn;
    std::vector<DeviceLogReportLogItem> list;
    std::string module;
    AIGC_JSON_HELPER(device_sn, list, module);
};

struct DeviceLogReportDomainItem {
    int64_t key = 0;
    std::string value;
    AIGC_JSON_HELPER(key, value);
};

struct DeviceLogReportContactInformation {
    std::string email;
    std::string phone;
    AIGC_JSON_HELPER(email, phone);
};

struct DeviceLogReportDeviceTopoItem {
    std::string device_model_key;
    std::string sn;
    AIGC_JSON_HELPER(device_model_key, sn);
};

struct DeviceLogReportDeviceTopo {
    std::vector<DeviceLogReportDeviceTopoItem> parents;
    std::vector<DeviceLogReportDeviceTopoItem> hosts;
    AIGC_JSON_HELPER(parents, hosts);
};

struct CreateDeviceLogReportRequest {
    std::string report_information;
    int64_t error_start_time = 0;
    DeviceLogReportDeviceTopo device_topo;
    DeviceLogReportContactInformation report_contact_information;
    std::vector<std::string> appendix_files;
    std::vector<DeviceLogReportLogListItem> log_list;
    std::vector<DeviceLogReportDomainItem> domains;
    bool is_crashed = false;
    AIGC_JSON_HELPER(report_information, error_start_time, device_topo, report_contact_information, appendix_files, log_list, domains, is_crashed);
};

struct LogReportOperationRequest {
    std::string report_cmd_method;
    std::string report_uuid;
    AIGC_JSON_HELPER(report_cmd_method, report_uuid);
};

struct LogReportAppendStsRequest {
    std::string method;
    AIGC_JSON_HELPER(method);
};

struct HistoryHmsDataRequest {
    std::vector<std::string> sn;
    int64_t create_time_begin = 0;
    int64_t create_time_end = 0;
    int page_size = 0;
    int page_num = 0;
    AIGC_JSON_HELPER(sn, create_time_begin, create_time_end, page_size, page_num);
};

struct UnBindDeviceRequest {
    std::vector<std::string> device_sns;
    AIGC_JSON_HELPER(device_sns);
};

struct ModifyDeviceNameItemRequest {
    std::string device_sn;
    std::string device_callsign;
    AIGC_JSON_HELPER(device_sn, device_callsign);
};

struct ModifyDeviceNameRequest {
    std::vector<ModifyDeviceNameItemRequest> devices;
    AIGC_JSON_HELPER(devices);
};

struct FirmwareUpgradeDeviceItemRequest {
    std::string sn;
    std::string device_model_key;
    int firmware_upgrade_type;
    std::string product_version;
    AIGC_JSON_HELPER(sn, device_model_key, firmware_upgrade_type, product_version);
};

struct FirmwareUpgradeRequest {
    std::vector<FirmwareUpgradeDeviceItemRequest> devices;
    AIGC_JSON_HELPER(devices);
};

struct FirmwareInfoRequest {
    std::string device_model_key;
    std::string version;
    std::string desc;
    std::string object_key;
    std::string md5;
    AIGC_JSON_HELPER(device_model_key, version, desc, object_key, md5);
};

struct CheckFirmwareStatusRequest {
    std::vector<std::string> device_sns;
    std::vector<std::string> status;
    AIGC_JSON_HELPER(device_sns, status);
};

struct QueryDeviceUnlockLicenseListRequest {
    std::string device_sn;
    int device_model_domain;
    AIGC_JSON_HELPER(device_sn, device_model_domain);
};

struct SwitchDeviceUnlockLicenseCmdDataRequest {
    std::string device_sn;
    int license_id = 0;
    bool enable = false;
    AIGC_JSON_HELPER(device_sn, license_id, enable);
};

struct SwitchDeviceUnlockLicenseRequest {
    std::string device_sn;
    SwitchDeviceUnlockLicenseCmdDataRequest device_cmd_data;
    AIGC_JSON_HELPER(device_sn, device_cmd_data);
};

struct CheckUnlockLicenseRequest {
    std::string file_key;
    std::string device_sn;
    AIGC_JSON_HELPER(file_key, device_sn);
};

struct ImportUnlockLicenseRequest {
    std::string device_sn;
    std::vector<std::string> cert_url;
    AIGC_JSON_HELPER(device_sn, cert_url);
};

struct SetDistanceLimitRequestData {
    int distance_limit_state = 0;
    int distance_limit_value = 0;
    AIGC_JSON_HELPER(distance_limit_state, distance_limit_value);
};

struct SetDistanceLimitRequest {
    std::string device_sn;
    SetDistanceLimitRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SetHeightLimitRequestData {
    int height_limit = 0;
    AIGC_JSON_HELPER(height_limit);
};

struct SetHeightLimitRequest {
    std::string device_sn;
    SetHeightLimitRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SetObstacleAvoidanceRequestData {
    int obstacle_avoidance_state = 0;
    AIGC_JSON_HELPER(obstacle_avoidance_state);
};

struct SetObstacleAvoidanceRequest {
    std::string device_sn;
    SetObstacleAvoidanceRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SetRemainingPowerForRthRequestData {
    int remaining_power_for_rth = 0;
    AIGC_JSON_HELPER(remaining_power_for_rth);
};

struct SetRemainingPowerForRthRequest {
    std::string device_sn;
    SetRemainingPowerForRthRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SetNightLightsStateRequestData {
    int night_lights_state = 0;
    AIGC_JSON_HELPER(night_lights_state);
};

struct SetNightLightsStateRequest {
    std::string device_sn;
    SetNightLightsStateRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct EsimOperatorSwitchRequestData {
    std::string imei;
    int operator_ = 0;
    std::string device_type;
    AIGC_JSON_HELPER(imei, operator_, device_type);
    AIGC_JSON_HELPER_RENAME("imei", "operator", "device_type");
};

struct EsimOperatorSwitchRequest {
    std::string device_sn;
    EsimOperatorSwitchRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SimSlotSwitchRequestData {
    std::string imei;
    int sim_slot = 0;
    std::string device_type;
    AIGC_JSON_HELPER(imei, sim_slot, device_type);
};

struct SimSlotSwitchRequest {
    std::string device_sn;
    SimSlotSwitchRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SetBatteryStoreModeRequestData {
    int battery_store_mode = 0;
    AIGC_JSON_HELPER(battery_store_mode);
};

struct SetBatteryStoreModeRequest {
    std::string device_sn;
    SetBatteryStoreModeRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct ManualBatterySwapRequestData {
    int slot_id = 0;
    AIGC_JSON_HELPER(slot_id);
};

struct ManualBatterySwapRequest {
    std::string device_sn;
    ManualBatterySwapRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct DonglePowerSwitchRequestData {
    int action = 0;
    AIGC_JSON_HELPER(action);
};

struct DonglePowerSwitchRequest {
    std::string device_sn;
    DonglePowerSwitchRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct LowPowerSwitchRequestData {
    int low_power_mode = 0;
    AIGC_JSON_HELPER(low_power_mode);
};

struct LowPowerSwitchRequest {
    std::string device_sn;
    LowPowerSwitchRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct SwitchAlarmLightStateRequestData {
    int alarm_state = 0;
    AIGC_JSON_HELPER(alarm_state);
};

struct SwitchAlarmLightStateRequest {
    std::string device_sn;
    SwitchAlarmLightStateRequestData data;
    AIGC_JSON_HELPER(device_sn, data);
};

struct BatchDeleteWaylinesRequest {
    std::vector<std::string> wayline_uuids;
    AIGC_JSON_HELPER(wayline_uuids);
};

struct UpdateWaylineRequest {
    std::string wayline_uuid;
    std::string wayline_name;
    AIGC_JSON_HELPER(wayline_uuid, wayline_name);
};

struct SubmitWaylineInfoRequest {
    std::string kmz_object_key;
    std::string wayline_name;
    std::string async_request_id;
    int64_t timestamp = 0;
    bool return_paas_data = false;
    AIGC_JSON_HELPER(kmz_object_key, wayline_name, async_request_id, timestamp, return_paas_data);
};

struct BatchDownloadWaylinesRequest {
    std::vector<std::string> wayline_uuids;
    AIGC_JSON_HELPER(wayline_uuids);
};

struct DuplicateWaylineRequest {
    std::string original_wayline_uuid;
    std::string target_wayline_name;
    std::string target_object_key_prefix;
    AIGC_JSON_HELPER(original_wayline_uuid, target_wayline_name, target_object_key_prefix);
};

struct CopyWaylineRequest {
    std::string original_wayline_uuid;
    std::string target_wayline_name;
    AIGC_JSON_HELPER(original_wayline_uuid, target_wayline_name);
};

struct ConcatWaylineRequest {
    std::string target_wayline_name;
    std::string kmz_object_key;
    std::vector<std::string> sub_wayline_uuids;
    std::unordered_map<std::string, std::string> editor_warnings;
    AIGC_JSON_HELPER(target_wayline_name, kmz_object_key, sub_wayline_uuids, editor_warnings);
};

// AI Inside 机载算法管理请求结构体（只含需 JSON 序列化的 Request，Param 在 api/message.h）
struct OperateAIInsideModelRequest {
    std::string operation_type;
    std::vector<std::string> drone_sn_list;
    AIGC_JSON_HELPER(operation_type, drone_sn_list);
};

struct CreateAIInsideAlgorithmRequest {
    std::string name;
    AIGC_JSON_HELPER(name);
};

struct FileUploadAIInsideAlgorithmRequest {
    std::string file_key;
    AIGC_JSON_HELPER(file_key);
};

struct DeleteAIInsideAlgorithmRequest {
    std::string signed_id;
    AIGC_JSON_HELPER(signed_id);
};


/// FlightSmartPlanModeSettings 飞行安全设置
///   smart_planning_mode      - 智能规划模式: 0=启用 / 1=关闭（与 commander_flight_mode、rth_mode 互斥）
///   sn                        - 网关 SN（后端要求 Body 内也带 sn，即使 URL 也带了 ?sn=xxx）
struct FlightSmartPlanModeSettings {
    int smart_planning_mode;
    std::string sn;
    AIGC_JSON_HELPER(smart_planning_mode, sn);
};

/// FlightSmartPlanModeRequest 智能规划模式请求
/// 对应 PUT /manage/api/v1/flight/configs/safe?sn=xxx
struct FlightSmartPlanModeRequest {
    std::string sn;                    // 网关 SN，同时会拼到 URL 的 ?sn=xxx 和 Body 里的 sn 字段
    FlightSmartPlanModeSettings settings;       // Body 序列化只用 settings（含 sn 和其余字段平铺）

    AIGC_JSON_HELPER(sn, settings);
};

/// FlightOutOfControlActionSettings 失控动作设置
struct FlightOutOfControlActionSettings {
    std::string out_of_control_action;   // 失控动作: "Hover" / "ReturnHome" / "Continue"
    std::string sn;                       // 网关 SN（后端 Body 要求）
    AIGC_JSON_HELPER(out_of_control_action, sn);
};

/// FlightOutOfControlActionRequest 失控动作请求
/// 对应 PUT /manage/api/v1/flight/configs/safe?sn=xxx
struct FlightOutOfControlActionRequest {
    std::string sn;
    FlightOutOfControlActionSettings settings;
    AIGC_JSON_HELPER(sn, settings);
};

/// FlightCommanderHeightSettings 飞行作业高度设置
struct FlightCommanderHeightSettings {
    int commander_flight_height = 0;   // 飞行作业高度（米），取值[0, 3000]
    std::string sn;                     // 网关 SN（后端 Body 要求）
    AIGC_JSON_HELPER(commander_flight_height, sn);
};

/// FlightCommanderHeightRequest 飞行作业高度请求
/// 对应 PUT /manage/api/v1/flight/configs/safe?sn=xxx
struct FlightCommanderHeightRequest {
    std::string sn;
    FlightCommanderHeightSettings settings;
    AIGC_JSON_HELPER(sn, settings);
};

/// FlightRthAltitudeSettings 返航高度设置
struct FlightRthAltitudeSettings {
    int rth_altitude = 0;   // 返航高度（米）
    std::string sn;         // 网关 SN（后端 Body 要求）
    AIGC_JSON_HELPER(rth_altitude, sn);
};

/// FlightRthAltitudeRequest 返航高度请求
/// 对应 PUT /manage/api/v1/flight/configs/safe?sn=xxx
struct FlightRthAltitudeRequest {
    std::string sn;
    FlightRthAltitudeSettings settings;
    AIGC_JSON_HELPER(sn, settings);
};

/// AirTransferEnableRequest 空中回传设置请求
/// 对应 PUT /control/api/v1/flight/configs/air-transfer
struct AirTransferEnableRequest {
    std::string device_sn;              // 设备序列号
    bool air_transfer_enable = false;   // 空中回传开关: true=开启, false=关闭
    AIGC_JSON_HELPER(device_sn, air_transfer_enable);
};


// (设备)报修管理模块: 获取设备报修配置请求参数;
struct ObtainRepairConfigRequest {
    std::vector<std::string> device_sn;    // 设备 SN;

    // register class-members;
    AIGC_JSON_HELPER(device_sn);
};


// (设备)报修管理模块: 创建报修订单 - 联系地址;
struct MaintenanceContactAddress {
    std::string country;
    std::string province;
    std::string city;
    std::string district;
    std::string detail;

    // register class-members;
    AIGC_JSON_HELPER(country, province, city, district, detail);
};


// (设备)报修管理模块: 创建报修订单 - 设备信息;
struct MaintenanceOrderDeviceItem {
    std::string device_model_key;
    std::string device_sn;
    std::string module;
    std::string nameplate_sn;
    int source_type = 0;             // 报修来源类型：0=定损 2=日志反馈 3=直接报修;
    std::string source_ref_id;

    // register class-members;
    AIGC_JSON_HELPER(device_model_key, device_sn, module, nameplate_sn, source_type, source_ref_id);
};

// (设备)报修管理模块: 创建报修订单 - 设备拓扑节点;
struct MaintenanceTopoNode {
    std::string sn;
    std::string device_model_key;
    bool online_status = false;

    // register class-members;
    AIGC_JSON_HELPER(sn, device_model_key, online_status);
};

// (设备)报修管理模块: 创建报修订单 - 设备拓扑;
struct MaintenanceDeviceTopo {
    std::vector<MaintenanceTopoNode> hosts;
    std::vector<MaintenanceTopoNode> parents;
    std::vector<MaintenanceTopoNode> nodes;

    // register class-members;
    AIGC_JSON_HELPER(hosts, parents, nodes);
};

// attachments;
struct MaintenanceOrderAttachments {
    std::string file_name;      // 文件名;
    std::string file_key;       // OSS 存储路径(object key);
    int64_t file_size = 0;      // 文件大小(字节);

    // register class-members;
    AIGC_JSON_HELPER(file_name, file_key, file_size);
};


// 创建报修订单 - 日志信息;
struct MaintenanceLogInfo {
    std::string device_sn;   // 设备 SN;
    std::string module;      // 设备模块;
    std::string object_key;  // 日志文件 OSS 路径;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, module, object_key);
};



// (设备)报修管理模块: 创建报修订单请求参数;
struct CreateMaintenanceOrdersRequest {
    std::string fault_occurrence_time;                      // 故障发生时间("2026-04-01 10:00:00");
    std::vector<MaintenanceOrderDeviceItem> device_list;    // 设备列表;
    std::string fault_description;                          // 故障描述;
    std::vector<MaintenanceOrderAttachments> attachments;   // 附件列表;
    std::string contact_person;                             // 联系人;
    std::string contact_phone;                              // 联系电话;
    std::string contact_email;                              // 联系邮箱;
    MaintenanceContactAddress contact_address;              // 联系地址;
    std::string expected_service_time;                      // 期望服务时间;
    std::string report_uuid;                                // 报告 UUID;
    MaintenanceDeviceTopo device_topo;                      // 设备拓扑;
    std::vector<MaintenanceLogInfo> log_list;               // 日志列表;

    // register class-members;
    AIGC_JSON_HELPER(
        fault_occurrence_time,
        device_list,
        fault_description,
        attachments,
        contact_person,
        contact_phone,
        contact_email,
        contact_address,
        expected_service_time,
        report_uuid,
        device_topo,
        log_list
    );
};


// (设备)报修管理模块: 分页查询报修订单列表请求参数;
struct GetMaintenanceOrdersListRequest {
    int page = 1;
    int page_size = 100;
    std::string order_number;
    std::string status;
    std::string initiated_from;
    std::string device_sn;
    std::vector<std::string> creator_ids;
    std::string key_word;
    std::string created_at_start;
    std::string created_at_end;
    std::string sort_column;
    std::string sort_type;
};


// === Terra Build 相关请求参数 ===

/// GetQualityReportRequest — 获取质量报告查询参数
struct GetQualityReportRequest {
    std::string language = "zh";                    // 报告语言: zh/en
    std::string measure_unit_system = "metric";     // 度量单位: metric/imperial
    AIGC_JSON_HELPER(language, measure_unit_system);
};

/// GetModelFileListRequest — 获取模型文件列表查询参数
struct GetModelFileListRequest {
    int page = 1;                                   // 页码
    int page_size = 20;                             // 每页数量（最大 100）
    AIGC_JSON_HELPER(page, page_size);
};

/// GetModelFileUrlRequest — 获取模型文件签名 URL 查询参数
struct GetModelFileUrlRequest {
    int type = 1;   // 1=原始文件, 2=ZIP 文件
    AIGC_JSON_HELPER(type);
};

/// PolygonPoint — predefine_area 中的多边形坐标点
struct PolygonPoint {
    double lat = 0.0;   // 纬度
    double lng = 0.0;   // 经度
    AIGC_JSON_HELPER(lat, lng);
};

/// PredefineArea — 兴趣区域（多边形）
struct PredefineArea {
    std::vector<PolygonPoint> polygon_points;   // 多边形坐标点列表
    AIGC_JSON_HELPER(polygon_points);
};

// ============================================================
// 创建重建任务 — 内部序列化用子结构体（带 AIGC_JSON_HELPER，与公开层 Recon* 类型隔离）
// ============================================================

/// GeoDesc — 地理坐标系描述（内部序列化层）
struct GeoDesc {
    std::optional<std::string> cs_type;
    std::optional<std::string> geo_cs;
    std::optional<std::string> geo_cs_wkt;
    std::optional<std::string> geoid_file_name;
    std::optional<std::vector<double>> offset;
    std::optional<std::string> override_vertical_cs;
    AIGC_JSON_HELPER(cs_type);
    AIGC_JSON_HELPER_OPTIONAL_SKIP(cs_type, geo_cs, geo_cs_wkt, geoid_file_name, offset, override_vertical_cs);
};

/// QualityReportSetting — 质量报告设置（内部序列化层）
struct QualityReportSetting {
    std::optional<std::string> language = "zh";
    std::optional<std::string> measure_unit_system = "metric";
    AIGC_JSON_HELPER(language);
    AIGC_JSON_HELPER_OPTIONAL_SKIP(language, measure_unit_system);
};

/// Parameter2D — 二维重建参数（内部序列化层）
struct Parameter2D {
    std::optional<std::string> SfM_method;
    std::optional<int> sfm_feature_quantity_level;
    std::optional<bool> use_image_position;
    std::optional<bool> generate_blocks_exchangge_xml;
    std::optional<GeoDesc> blocks_exchange_xml_geo_desc;
    std::optional<double> usable_memory_GB;
    std::optional<int> map_mode;
    std::optional<int> quality_level;
    std::optional<int> resolution_type_2d;
    std::optional<int> dom_tile_size;
    std::optional<GeoDesc> output_geo_desc;
    std::optional<double> resolution_gsd_2d;
    std::optional<bool> use_Wallis;
    std::optional<bool> use_Dehaze;
    std::optional<bool> use_gcj02;
    std::optional<bool> generate_maps_2d_tiff;
    std::optional<bool> generate_maps_2d_tiff_cog;
    std::optional<bool> use_smart_fill_hole;
    AIGC_JSON_HELPER(SfM_method)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(SfM_method, sfm_feature_quantity_level, use_image_position,
                     generate_blocks_exchangge_xml, blocks_exchange_xml_geo_desc,
                     usable_memory_GB, map_mode, quality_level, resolution_type_2d,
                     dom_tile_size, output_geo_desc, resolution_gsd_2d,
                     use_Wallis, use_Dehaze, use_gcj02,
                     generate_maps_2d_tiff, generate_maps_2d_tiff_cog, use_smart_fill_hole)
};

/// Parameter3D — 三维重建参数（内部序列化层）
struct Parameter3D {
    std::optional<std::string> SfM_method;
    std::optional<int> sfm_feature_quantity_level;
    std::optional<bool> use_image_position;
    std::optional<bool> generate_blocks_exchangge_xml;
    std::optional<GeoDesc> blocks_exchange_xml_geo_desc;
    std::optional<bool> output_pointcloud;
    std::optional<int> density_percent_level;
    std::optional<bool> generate_pnts;
    std::optional<bool> generate_las;
    std::optional<bool> generate_laz;
    std::optional<bool> generate_pcd;
    std::optional<bool> generate_point_s3mb;
    std::optional<bool> generate_point_ply;
    std::optional<bool> merge_point;
    std::optional<bool> output_ply_with_normals;
    std::optional<int> ply_with_normals_level;
    std::optional<bool> groud_extract;
    std::optional<int> scene_type;
    std::optional<double> max_size_building;
    std::optional<double> iter_dist;
    std::optional<double> iter_angle;
    std::optional<double> usable_memory_GB;
    std::optional<bool> stereo_mode;
    std::optional<int> global_min_depth;
    std::optional<bool> output_mesh;
    std::optional<bool> generate_ply;
    std::optional<bool> generate_obj;
    std::optional<bool> generate_fbx;
    std::optional<bool> generate_b3dm;
    std::optional<bool> generate_osgb;
    std::optional<bool> generate_s3mb;
    std::optional<bool> generate_i3s;
    std::optional<bool> water_refine;
    std::optional<bool> water_remove;
    std::optional<double> model_simplify;
    std::optional<bool> generate_dem;
    std::optional<double> dem_resolution;
    std::optional<bool> generate_contour;
    std::optional<double> contour_datum;
    std::optional<double> contour_interval;
    std::optional<double> contour_elevation_annotation_radius;
    std::optional<bool> use_contour_filter;
    std::optional<double> contour_filter_length;
    std::optional<bool> generate_grid_and_tin;
    std::optional<double> grid_interval;
    std::optional<bool> cut_AOI;
    std::optional<int> quality_level;
    std::optional<GeoDesc> output_geo_desc;
    AIGC_JSON_HELPER(SfM_method)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(SfM_method, sfm_feature_quantity_level, use_image_position,
                     generate_blocks_exchangge_xml, blocks_exchange_xml_geo_desc,
                     output_pointcloud, density_percent_level, generate_pnts,
                     generate_las, generate_laz, generate_pcd, generate_point_s3mb,
                     generate_point_ply, merge_point, output_ply_with_normals, ply_with_normals_level,
                     groud_extract, scene_type, max_size_building, iter_dist, iter_angle,
                     usable_memory_GB, stereo_mode, global_min_depth,
                     output_mesh, generate_ply, generate_obj, generate_fbx, generate_b3dm,
                     generate_osgb, generate_s3mb, generate_i3s,
                     water_refine, water_remove, model_simplify,
                     generate_dem, dem_resolution, generate_contour, contour_datum,
                     contour_interval, contour_elevation_annotation_radius,
                     use_contour_filter, contour_filter_length,
                     generate_grid_and_tin, grid_interval, cut_AOI, quality_level, output_geo_desc)
};

/// Parameter3DGS — 3DGS 重建参数（内部序列化层）
struct Parameter3DGS {
    std::optional<std::string> SfM_method;
    std::optional<int> sfm_feature_quantity_level;
    std::optional<bool> use_image_position;
    std::optional<bool> generate_blocks_exchangge_xml;
    std::optional<GeoDesc> blocks_exchange_xml_geo_desc;
    std::optional<std::string> global_data_type;
    std::optional<int> quality_level_3dgs;
    std::optional<bool> generate_terrags;
    std::optional<bool> generate_3dtiles;
    std::optional<bool> generate_3dgs_dom;
    std::optional<GeoDesc> output_geo_desc;
    AIGC_JSON_HELPER(SfM_method)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(SfM_method, sfm_feature_quantity_level, use_image_position,
                     generate_blocks_exchangge_xml, blocks_exchange_xml_geo_desc,
                     global_data_type, quality_level_3dgs, generate_terrags,
                     generate_3dtiles, generate_3dgs_dom, output_geo_desc)
};

/// ParameterLidar — 激光雷达重建参数（内部序列化层）
struct ParameterLidar {
    std::optional<bool> output_pointcloud;
    std::optional<int> density_percent_level;
    std::optional<bool> generate_pnts;
    std::optional<bool> generate_las;
    std::optional<bool> generate_laz;
    std::optional<bool> generate_pcd;
    std::optional<bool> generate_point_s3mb;
    std::optional<bool> generate_point_ply;
    std::optional<bool> merge_point;
    std::optional<bool> output_ply_with_normals;
    std::optional<int> ply_with_normals_level;
    std::optional<bool> groud_extract;
    std::optional<int> scene_type;
    std::optional<double> max_size_building;
    std::optional<double> iter_dist;
    std::optional<double> iter_angle;
    std::optional<double> usable_memory_GB;
    std::optional<bool> colorize_point;
    std::optional<bool> lpp_optimise;
    std::optional<int> refine_calibrate;
    std::optional<bool> smooth_point;
    std::optional<double> sample_distance;
    std::optional<double> output_height_offset;
    std::optional<int> quality_level;
    std::optional<bool> can_process_L1;
    std::optional<GeoDesc> output_geo_desc;
    std::optional<bool> output_mesh;
    std::optional<int> quality_level_mesh;
    std::optional<double> model_simplify;
    std::optional<bool> generate_obj;
    std::optional<bool> generate_fbx;
    std::optional<bool> generate_ply;
    std::optional<bool> generate_osgb;
    std::optional<bool> generate_b3dm;
    std::optional<bool> generate_i3s;
    std::optional<bool> generate_maps_2d;
    std::optional<bool> generate_maps_2d_tiff;
    std::optional<bool> generate_maps_2d_tiff_cog;
    std::optional<int> resolution_type_2d;
    std::optional<int> resolution_level_2d;
    std::optional<double> resolution_gsd_2d;
    std::optional<int> dom_tile_size;
    std::optional<bool> use_gcj02;
    std::optional<bool> generate_dem;
    std::optional<double> dem_resolution;
    std::optional<bool> generate_contour;
    std::optional<double> contour_datum;
    std::optional<double> contour_interval;
    std::optional<double> contour_elevation_annotation_radius;
    std::optional<bool> use_contour_filter;
    std::optional<double> contour_filter_length;
    std::optional<bool> generate_3dgs;
    std::optional<int> quality_level_3dgs;
    std::optional<bool> generate_3dtiles;
    AIGC_JSON_HELPER(output_pointcloud)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(output_pointcloud, density_percent_level, generate_pnts,
                     generate_las, generate_laz, generate_pcd, generate_point_s3mb,
                     generate_point_ply, merge_point, output_ply_with_normals, ply_with_normals_level,
                     groud_extract, scene_type, max_size_building, iter_dist, iter_angle,
                     usable_memory_GB, colorize_point, lpp_optimise, refine_calibrate,
                     smooth_point, sample_distance, output_height_offset,
                     quality_level, can_process_L1, output_geo_desc,
                     output_mesh, quality_level_mesh, model_simplify,
                     generate_obj, generate_fbx, generate_ply, generate_osgb,
                     generate_b3dm, generate_i3s, generate_maps_2d,
                     generate_maps_2d_tiff, generate_maps_2d_tiff_cog,
                     resolution_type_2d, resolution_level_2d, resolution_gsd_2d,
                     dom_tile_size, use_gcj02, generate_dem, dem_resolution,
                     generate_contour, contour_datum, contour_interval,
                     contour_elevation_annotation_radius, use_contour_filter, contour_filter_length,
                     generate_3dgs, quality_level_3dgs, generate_3dtiles)
};

/// CreateReconstructionRequest — 创建重建任务请求
/// 必填字段用 AIGC_JSON_HELPER（空数组保留，服务端可校验）
/// 可选字段用 AIGC_JSON_HELPER_OPTIONAL_SKIP（nullopt 时跳过，不输出到 JSON）
struct CreateReconstructionRequest {
    // 必填
    std::vector<int> reconstruction_type;
    std::string name;

    // 二选一
    std::vector<std::string> folder_uuids;
    std::vector<int> folder_ids;

    // 必填字段 — 正常序列化（空数组/空字符串保留，服务端可校验）
    AIGC_JSON_HELPER(reconstruction_type, name, folder_uuids, folder_ids)

    // 可选基础字段
    std::optional<double> simplified_factor;
    std::optional<PredefineArea> predefine_area;
    std::optional<std::vector<int>> generate_model_formats;
    std::optional<std::string> wkt;
    std::optional<int> quality_level;
    std::optional<int> quality_level_3dgs;
    std::optional<bool> is_shown_on_map;
    std::optional<bool> optimize_SfM;
    std::optional<int> custom_storage_parent_id;
    std::optional<QualityReportSetting> quality_report_setting;

    // 可选 Parameter 对象
    std::optional<Parameter2D> parameter_2d;
    std::optional<Parameter3D> parameter_3d;
    std::optional<Parameter3DGS> parameter_3dgs;
    std::optional<ParameterLidar> parameter_lidar;

    // 可选字段 — nullopt 时跳过，不输出到 JSON
    AIGC_JSON_HELPER_OPTIONAL_SKIP(simplified_factor, predefine_area, generate_model_formats, wkt,
                                   quality_level, quality_level_3dgs,
                                   is_shown_on_map, optimize_SfM, custom_storage_parent_id,
                                   quality_report_setting,
                                   parameter_2d, parameter_3d, parameter_3dgs, parameter_lidar)
};

/// RetryReconstructionRequest — 重试重建任务请求
struct RetryReconstructionRequest {
    std::string file_uuid;                          // 文件 UUID
    AIGC_JSON_HELPER(file_uuid);
};

/// StopReconstructionRequest — 停止重建任务请求
struct StopReconstructionRequest {
    std::string file_uuid;                          // 文件 UUID
    AIGC_JSON_HELPER(file_uuid);
};

/// DeleteModelFilesRequest — 批量删除模型文件请求
struct DeleteModelFilesRequest {
    std::vector<int> file_ids;                      // 文件 ID 列表
    AIGC_JSON_HELPER(file_ids);
};

/// CreatePanoramaRequest — 创建全景拼接任务请求
struct CreatePanoramaRequest {
    std::string name;                           // 任务名称，必填
    std::vector<std::string> folder_uuids;      // 文件夹 UUID 列表，必填
    std::string wkt;                            // 输出坐标系 WKT 字符串，可选
    std::optional<bool> is_shown_on_map;        // 是否在地图上展示，可选
    AIGC_JSON_HELPER(name, folder_uuids, wkt, is_shown_on_map);
};

// === Terra 模型管理新增请求结构 ===

/// TerraUploadCallbackRequest — 模型文件上传回调请求（POST /pf-terra/api/v1/models/files/upload-callback）
struct TerraUploadCallbackRequest {
    std::string object_key;                 // OSS 对象 key，必填
    std::string name;                       // 文件名（含后缀），必填
    std::optional<long long> parent_id;     // 父目录 ID（0 表示根目录）
    std::optional<std::string> drive_uuid;  // 存储盘 UUID
    std::optional<bool> show_on_map;        // 解压完成后是否自动加载到地图
    AIGC_JSON_HELPER(object_key, name)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(parent_id, drive_uuid, show_on_map)
};

/// TerraRenameModelRequest — 模型重命名请求（PUT /model/api/v1/files/{id}）
struct TerraRenameModelRequest {
    std::string name;                           // 新名称（不允许含 /）
    std::optional<bool> in_uniform_height;      // 等高模式
    std::optional<int> model_display_mode;      // 模型显示模式：1-普通 2-环绕 3-线框
    std::optional<long long> collected_at;      // 采集时间（毫秒时间戳）
    AIGC_JSON_HELPER(name)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(in_uniform_height, model_display_mode, collected_at)
};

/// TerraSelectFilter — 全选反选场景的筛选条件
struct TerraSelectFilter {
    std::optional<int> selected_parent_id;  // 选中范围的父目录 ID（uint32）
    std::vector<int> except_ids;            // 排除的文件 ID 列表
    std::string key;                        // 关键字模糊搜索
    AIGC_JSON_HELPER(except_ids, key)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(selected_parent_id)
};

/// TerraShowOnMapRequest — 批量加载/取消加载模型到地图（PUT /model/api/v1/files）
struct TerraShowOnMapRequest {
    std::vector<int> ids;                       // 指定文件 ID 列表（与 select_filter 二选一）
    std::optional<bool> show_on_map;            // true=加载，false=取消加载
    std::optional<bool> is_distributed;         // 是否已分发
    std::optional<TerraSelectFilter> select_filter; // 全选过滤条件
    AIGC_JSON_HELPER(ids)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(show_on_map, is_distributed, select_filter)
};

/// TerraSetLasWktRequest — 设置 LAS 坐标系请求（POST /pf-terra/api/v1/models/converters）
struct TerraSetLasWktRequest {
    std::string file_uuid;  // LAS 模型文件 UUID，必填
    std::string wkt;        // 坐标系描述（EPSG 代码或 WKT 字符串），必填
    AIGC_JSON_HELPER(file_uuid, wkt)
};

// === 元素管理（ElementService）内部请求结构体 ===

/// ElementPropertiesReq — 要素渲染属性（GeoJSON Feature Properties）
struct ElementPropertiesReq {
    std::string color;
    bool clamp_to_ground = false;
    bool is3d = false;
    bool is_transparent = false;
    int font_size = 0;
    int icon = 0;
    AIGC_JSON_HELPER(color, clamp_to_ground, is3d, is_transparent, font_size, icon)
};

/// ElementGeoPointReq — 地理坐标点 {lng, lat, alt}
struct ElementGeoPointReq {
    double lng = 0.0;
    double lat = 0.0;
    double alt = 0.0;
    AIGC_JSON_HELPER(lng, lat, alt)
};

/// ElementGeometryReq — 要素几何数据（GeoJSON Geometry）
struct ElementGeometryReq {
    std::string type;
    double radius = 0.0;
    std::string coordinates;     // raw JSON 透传（AIGC 序列化后需 unquote）
    std::string bread_crumbs;    // raw JSON 透传（AIGC 序列化后需 unquote）
    AIGC_JSON_HELPER(type, radius, coordinates, bread_crumbs)
};

/// ElementMediaImageReq — 媒体图片
struct ElementMediaImageReq {
    std::string object_key;
    int media_file_id = 0;
    std::string media_file_uuid;
    AIGC_JSON_HELPER(object_key, media_file_id, media_file_uuid)
};

/// ElementMediaResourcesReq — 要素绑定的媒体资源
struct ElementMediaResourcesReq {
    std::vector<ElementMediaImageReq> images;
    AIGC_JSON_HELPER(images)
};

/// ElementResourceContentReq — 要素几何内容，GeoJSON Feature 结构
struct ElementResourceContentReq {
    std::string type;                          // 固定传 "Feature"
    ElementPropertiesReq properties;           // 渲染属性
    ElementGeometryReq geometry;               // 几何数据
    ElementMediaResourcesReq media_resources;  // 绑定的媒体资源
    AIGC_JSON_HELPER(type, properties, geometry, media_resources)
};

/// ElementResourceItemReq — 要素资源（对齐 proto ElementResourceItem）
struct ElementResourceItemReq {
    ElementResourceContentReq content; // 几何内容（GeoJSON Feature）
    int type = 0;                      // 几何类型：0-点 1-线 2-面 7-圆
    std::string remark;                // 备注
    AIGC_JSON_HELPER(content, type, remark)
};

/// ElementResourceOrderItemReq — 要素排序项
struct ElementResourceOrderItemReq {
    std::string id;  // 要素 UUID
    int order = 0;   // 排序序号
    AIGC_JSON_HELPER(id, order)
};

/// ListElementHierarchyRequest — 查询元素层级列表（GET /pf-map/api/v1/elements/lists）
/// 所有字段均为查询参数，序列化仅用于 URL 拼接，故无需 AIGC_JSON_HELPER
struct ListElementHierarchyRequest {
    std::string group_id;                        // 指定图层 UUID（空=查 workspace 下全部）
    std::string pid;                             // 父目录 UUID（空=查根目录）
    std::optional<bool> is_distributed;          // true=仅返回已分发的图层
    std::optional<int> type;                     // 图层类型筛选
    std::optional<std::string> element_uuid;     // 定位特定元素
    bool exclude_copy = false;                   // true=排除复制中的图层
};

/// CreateElementRequest — 创建要素（POST /pf-map/api/v1/elements/create）
struct CreateElementRequest {
    std::string id;                    // 可选：自定义要素 UUID
    int source = 0;                    // 要素来源类型
    ElementResourceItemReq resource;   // 要素资源，必填
    std::string name;                  // 要素名称
    int element_source = 0;            // 埋点来源标记
    std::string desc;                  // 描述
    AIGC_JSON_HELPER(id, source, resource, name, element_source, desc)
};

/// UpdateElementRequest — 更新要素（PUT /pf-map/api/v1/elements/update/{id}）
struct UpdateElementRequest {
    std::optional<std::string> name;
    std::optional<ElementResourceContentReq> content;  // 几何数据（GeoJSON Feature），整体覆盖
    std::optional<int> display;
    std::optional<std::string> remark;
    std::optional<int> status;
    std::optional<int> elevation_load_status;
    std::optional<int> user_status;
    std::string target_layer_id;
    std::vector<ElementResourceOrderItemReq> orders;
    AIGC_JSON_HELPER(target_layer_id, orders)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(name, content, display, remark, status, elevation_load_status, user_status)
};

/// SetElementVisibilityRequest — 设置要素显示状态（PUT /pf-map/api/v1/elements/{id}）
struct SetElementVisibilityRequest {
    int display = 0;  // 显示状态：1-显示 0-隐藏
    AIGC_JSON_HELPER(display)
};

/// DeleteElementRequest — 删除要素（DELETE /pf-map/api/v1/elements/delete/{id}）
/// 请求体（DELETE 可能无 body，from_batch_delete 作为查询参数或 body 传递）
struct DeleteElementRequest {
    bool from_batch_delete = false;
    AIGC_JSON_HELPER(from_batch_delete)
};

// === 媒体上传相关请求结构 ===

/// GetMediaUploadStsData — 上传凭证请求中的附加数据
struct GetMediaUploadStsData {
    std::string sn;     // 设备序列号
    int module = 0;     // 业务模块标识
    AIGC_JSON_HELPER(sn, module);
};

/// GetMediaUploadStsRequest — 获取媒体上传 STS 凭证请求（POST /pf-media/inner/v1/media-files/sts）
struct GetMediaUploadStsRequest {
    std::string file_path;      // 上传目标路径前缀（OSS Object Key 前缀），必填
    int expire = 3600;          // 凭证有效期（秒），最大 3600
    GetMediaUploadStsData data; // 附加数据（sn/module）
    AIGC_JSON_HELPER(file_path, expire, data);
};


/// GetMediaFilesDownloadUrlListRequest — 获取媒体文件;
struct GetMediaFilesDownloadUrlListRequest {
    std::vector<std::string> object_keys;    // 存储对象key列表;

    AIGC_JSON_HELPER(object_keys);
};


/// UploadCallbackFileExt — 上传回调中的文件扩展信息
struct UploadCallbackFileExt {
    std::string wayline_id;
    std::string prj_uuid;                   // 关联项目 UUID
    std::string workspace_id;               // 工作空间 ID
    std::string flight_id;                  // 航线任务 ID
    std::string drone_model_key;            // 飞行器型号 Key
    std::string payload_model_key;          // 负载型号 Key
    bool is_original = true;                // 是否原片
    std::string user_id;                    // 上传用户 ID
    std::string tinny_fingerprint;          // 短指纹
    int fast_reconstruction_media_type = 0; // 快速重建媒体类型
    int64_t media_index = 0;               // 媒体索引
    AIGC_JSON_HELPER(wayline_id, prj_uuid, workspace_id, flight_id,
                     drone_model_key, payload_model_key, is_original,
                     user_id, tinny_fingerprint,
                     fast_reconstruction_media_type, media_index);
};

/// UploadCallbackFileShootPosition — 拍摄位置
struct UploadCallbackFileShootPosition {
    double lat = 0.0;
    double lng = 0.0;
    AIGC_JSON_HELPER(lat, lng);
};

/// UploadCallbackFileMetadata — 文件元数据（图像拍摄信息）
struct UploadCallbackFileMetadata {
    double absolute_altitude = 0.0;     // 绝对高度（米）
    double relative_altitude = 0.0;     // 相对高度（米）
    double gimbal_yaw_degree = 0.0;     // 云台偏航角
    std::string created_time;           // 拍摄时间
    UploadCallbackFileShootPosition shoot_position; // 拍摄位置
    AIGC_JSON_HELPER(absolute_altitude, relative_altitude, gimbal_yaw_degree,
                     created_time, shoot_position);
};

/// UploadCallbackFile — 上传回调中的文件信息
struct UploadCallbackFile {
    std::string object_key;                 // 文件在 OSS 上的完整 Key，必填
    std::string fingerprint;                // 文件指纹（MD5）
    std::string path;                       // 设备端文件路径
    std::string name;                       // 文件名，必填
    int type = 0;                           // 文件类型
    std::string cloud_to_cloud_id;          // 云端传输标识
    UploadCallbackFileExt ext;              // 扩展信息
    UploadCallbackFileMetadata metadata;    // 图像元数据
    std::string created_time;              // 文件创建时间
    std::string source;                    // 上传来源（web/device）
    AIGC_JSON_HELPER(object_key, fingerprint, path, name, type,
                     cloud_to_cloud_id, ext, metadata, created_time, source);
};

/// UploadCallbackFlightTask — 上传回调中的飞行任务信息
struct UploadCallbackFlightTask {
    int expected_file_count = 0;    // 预期文件总数
    int flight_type = 0;            // 飞行类型
    int uploaded_file_count = 0;    // 已上传文件数
    AIGC_JSON_HELPER(expected_file_count, flight_type, uploaded_file_count);
};

/// UploadCallbackData — 上传回调内层 data 字段
struct UploadCallbackData {
    std::string sn;                         // 设备序列号
    int result = 0;                         // 上传结果（0=成功）
    int progress = 0;                       // 上传进度
    UploadCallbackFile file;                // 文件信息
    UploadCallbackFlightTask flight_task;   // 飞行任务信息
    AIGC_JSON_HELPER(sn, result, progress, file, flight_task);
};

/// MediaUploadCallbackRequest — 媒体上传完成回调请求（POST /pf-media/api/v1/upload-callback）
struct MediaUploadCallbackRequest {
    std::string biz_code;       // 业务码（如 "file_upload_callback"）
    UploadCallbackData data;
    double time_stamp = 0.0;    // 时间戳
    std::string version;        // 版本号
    AIGC_JSON_HELPER(biz_code, data, time_stamp, version);
};

// ============================================================
// 中继站调试接口请求结构
// ============================================================

// ============================================================
// Geo Caging 请求结构
// ============================================================

/// Geo Caging 围栏信息
struct GeoCagingInfo {
    double fg_longitude = 0.0;
    double fg_latitude = 0.0;
    int fg_radius = 0;
    int fg_height = 0;
    double cv_longitude = 0.0;
    double cv_latitude = 0.0;
    int cv_radius = 0;
    int cv_height = 0;
    AIGC_JSON_HELPER(fg_longitude, fg_latitude, fg_radius, fg_height, cv_longitude, cv_latitude, cv_radius, cv_height);
};

/// Geo Caging 开关
struct GeoCagingSwitchState {
    int state = 0;  // 0=关闭, 1=开启
    AIGC_JSON_HELPER(state);
};

/// desired 包含围栏信息
struct GeoCagingDesiredInfo {
    GeoCagingInfo geo_caging_info;
    AIGC_JSON_HELPER(geo_caging_info);
};

/// desired 包含开关
struct GeoCagingDesiredSwitch {
    GeoCagingSwitchState geo_caging_switch;
    AIGC_JSON_HELPER(geo_caging_switch);
};

/// state 数组元素 - 围栏信息
struct GeoCagingStateInfoItem {
    GeoCagingDesiredInfo desired;
    AIGC_JSON_HELPER(desired);
};

/// state 数组元素 - 开关
struct GeoCagingStateSwitchItem {
    GeoCagingDesiredSwitch desired;
    AIGC_JSON_HELPER(desired);
};

/// 设置中继广播隐藏请求
struct SetRelayBroadcastHideRequest {
    std::string device_sn;
    bool is_broadcast_hide = false;
    AIGC_JSON_HELPER(device_sn, is_broadcast_hide);
};

/// 设置中继名称（SSID）请求
struct SetRelaySSIDRequest {
    std::string device_sn;
    std::string device_name;
    AIGC_JSON_HELPER(device_sn, device_name);
};

/// 设置中继密码请求
struct SetRelayPasswordRequest {
    std::string device_sn;
    std::string passwd;
    AIGC_JSON_HELPER(device_sn, passwd);
};

/// 设置图传搜索模式请求
struct SetRelaySearchModeRequest {
    std::string device_sn;
    int relay_search_mode = 0;  // 0=关闭搜索, 1=开启搜索
    AIGC_JSON_HELPER(device_sn, relay_search_mode);
};

/// 设置中继占用状态请求
struct SetRelayOccupiedStatusRequest {
    std::string device_sn;
    int occupied_status = 0;  // 0=禁止抢占, 1=允许抢占
    AIGC_JSON_HELPER(device_sn, occupied_status);
};

/// 中继重启请求（仅含 device_sn）
struct RelayRebootRequest {
    std::string device_sn;
    AIGC_JSON_HELPER(device_sn);
};

/// 中继断开连接请求（仅含 device_sn）
struct RelayDisconnectRequest {
    std::string device_sn;
    AIGC_JSON_HELPER(device_sn);
};

// 设备定损: 设备拓扑节点;
struct SelfDiagnosisTopoNode {
    std::string sn;
    std::string type;
    int domain = 0;
    std::string device_model;

    AIGC_JSON_HELPER(sn, type, domain, device_model);
};

// 设备定损: 设备拓扑信息;
struct SelfDiagnosisDeviceTopo {
    std::string index;
    std::vector<SelfDiagnosisTopoNode> hosts;
    std::vector<SelfDiagnosisTopoNode> parents;
    std::vector<SelfDiagnosisTopoNode> nodes;
    std::vector<SelfDiagnosisTopoNode> others;
    std::vector<SelfDiagnosisTopoNode> docks;

    AIGC_JSON_HELPER(index, hosts, parents, nodes, others, docks);
};

// 设备定损: 下发定损指令请求;
struct CreateSelfDiagnosisRequest {
    std::string device_sn;       // 设备 SN;
    int type = 0;                // 定损类型;
    int part = 0;                // 定损部位;
    int index = 0;               // 索引;
    int motor_action = 0;        // 电机动作;
    SelfDiagnosisDeviceTopo device_topo;  // 设备拓扑;

    AIGC_JSON_HELPER(device_sn, type, part, index, motor_action, device_topo);
};

// 定损操作(取消 / 删除) 请求;
struct SelfDiagnosisOperationRequest {
    std::string report_uuid;         // 定损报告 UUID;
    std::string report_cmd_method;   // 操作指令: "cancel" / "delete";

    AIGC_JSON_HELPER(report_uuid, report_cmd_method);
};

// PaaS ai-alert 告警配置 POST Request
struct PostAiAlertDeviceSettingRequest {
    std::string drone_sn;
    bool enable_target_alert = false;
    AiAlertAlarmInfoResponse alarm_info;
    AIGC_JSON_HELPER(drone_sn, enable_target_alert, alarm_info);
};

struct PostAiAlertAlgorithmSettingRequest {
    std::string drone_sn;
    int algorithm_type = 0;
    std::string algorithm_name;
    std::vector<AiAlertAlarmTargetResponse> alarm_targets;
    AIGC_JSON_HELPER(drone_sn, algorithm_type, algorithm_name, alarm_targets);
};

struct CreateFlashLedConfigRequest {
    std::string name;
    std::vector<FlashLedSegmentHttp> flash_segments;
    AIGC_JSON_HELPER(name, flash_segments);
};

struct UpdateFlashLedConfigRequest {
    std::string name;
    std::vector<FlashLedSegmentHttp> flash_segments;
    AIGC_JSON_HELPER(name, flash_segments);
};

// ===================== Live Sync Request Begin =====================

struct LiveSyncCreateRequest {
    std::string converter_name;
    std::string sn;
    std::string camera_index;
    std::string video_type;
    int video_quality = 0;
    std::string schema;
    LiveSyncSchemaOptionHttp schema_option;
    AIGC_JSON_HELPER(converter_name, sn, camera_index, video_type, video_quality,
                     schema, schema_option);
};

/// update 路径1：仅开关 enable，body = {"enable": bool}
struct LiveSyncEnableOnlyRequest {
    bool enable = false;
    AIGC_JSON_HELPER(enable);
};

/// update 路径2：按 set_* 标记只序列化被修改的字段，避免默认值污染
struct LiveSyncUpdateFullRequest {
    std::optional<bool>        enable;
    std::optional<std::string> converter_name;
    std::optional<std::string> sn;
    std::optional<std::string> camera_index;
    std::optional<std::string> video_type;
    std::optional<int>         video_quality;
    std::optional<std::string> schema;
    std::optional<LiveSyncSchemaOptionHttp> schema_option;
    AIGC_JSON_HELPER_ALL_OPTIONAL(enable, converter_name, sn, camera_index,
                                  video_type, video_quality, schema, schema_option);
};

// ===================== Live Sync Request End =====================

// === 航线 Editor STS 请求结构体 ===

/// GetWaylineEditorStsRequest — 获取航线编辑器 STS（POST /gateway/pf-wayline/editor/v1/sts）
struct GetWaylineEditorStsRequest {
    std::string object_key_prefix; // 对象存储路径前缀，必填
    long long expire = 3600;       // 凭证有效期（秒），默认 3600
    AIGC_JSON_HELPER(object_key_prefix, expire)
};

// === 图片备注相关请求结构体 ===

/// UploadGeoMediaFileRequest — 上传地理标注媒体资源（POST /pf-media/api/v1/geo-media-file）
struct UploadGeoMediaFileRequest {
    std::string name;                           // 文件名称，必填
    std::string object_key;                     // OSS 对象 key，必填
    std::optional<std::string> fingerprint;     // 文件指纹（md5），可选
    int media_source = 0;                       // 媒体来源，可选，默认 0
    AIGC_JSON_HELPER(name, object_key, media_source)
    AIGC_JSON_HELPER_OPTIONAL_SKIP(fingerprint)
};

// ===================== 云环境切换 Request Begin =====================

struct CloudEnvDeviceInfoRequest {
    std::string callsign;
    std::string device_model_key;
    std::string device_sn;
    AIGC_JSON_HELPER(callsign, device_model_key, device_sn);
};

struct CloudEnvDeviceListRequest {
    std::vector<CloudEnvDeviceInfoRequest> host;
    std::vector<CloudEnvDeviceInfoRequest> nodes;
    std::vector<CloudEnvDeviceInfoRequest> parents;
    AIGC_JSON_HELPER(host, nodes, parents);
};

struct CloudEnvPublicCloudConfigRequest {
    std::string bind_code;
    std::string org_id;
    std::string zone_id;
    AIGC_JSON_HELPER(bind_code, org_id, zone_id);
};

struct CloudEnvPrivateCloudConfigRequest {
    std::string bind_code;
    std::string org_id;
    std::string url;
    AIGC_JSON_HELPER(bind_code, org_id, url);
};

struct CloudEnvThirdCloudConfigRequest {
    std::string bind_code;
    std::string org_id;
    std::string mqtt_addr;
    std::string mqtt_password;
    std::string mqtt_username;
    AIGC_JSON_HELPER(bind_code, org_id, mqtt_addr, mqtt_password, mqtt_username);
};

struct CloudEnvTargetConfigRequest {
    int type = 0;
    CloudEnvPublicCloudConfigRequest public_cloud_config;
    CloudEnvPrivateCloudConfigRequest private_cloud_config;
    CloudEnvThirdCloudConfigRequest third_cloud_config;
    AIGC_JSON_HELPER(type, public_cloud_config, private_cloud_config, third_cloud_config);
};

/// CloudEnvironmentSwitchRequest — POST /gateway/device/api/v1/service/cloud-environment-switch 请求体
struct CloudEnvironmentSwitchRequest {
    std::vector<CloudEnvDeviceListRequest> devices_list;
    CloudEnvTargetConfigRequest target_config;
    AIGC_JSON_HELPER(devices_list, target_config);
};

struct DeviceAuthVerifyRequest {
    std::string sn;
    std::string secret;
    std::string nonce;
    AIGC_JSON_HELPER(sn, secret, nonce);
};

/// CloudEnvironmentSwitchCallbackRequest — POST /gateway/device/api/v1/service/cloud_environment_switch/callback 请求体
struct CloudEnvironmentSwitchCallbackRequest {
    int code = 0;
    std::string status;
    DeviceAuthVerifyRequest device_auth_verify;
    std::vector<CloudEnvDeviceListRequest> devices_list;
    CloudEnvTargetConfigRequest target_config;
    AIGC_JSON_HELPER(code, status, device_auth_verify, devices_list, target_config);
};

// ===================== 云环境切换 Request End =====================

// ========================== 数据埋点 ======================================

/// BeaconReportRequest — 上报埋点事件(POST /pf-beacon/ingest/inner/v1/report, 平台内部接口)
struct BeaconReportRequest {
    std::string type = "sdk_api";                          // 埋点类型："milestone" | "component" | "sdk_api" | "ai_tool", sdk 约定使用 "sdk_api";
    std::string event;                                     // 事件标识，如 "takeoff";
    long long timestamp = 0;                               // Unix 秒级时间戳;
    std::string license_id;                                // ISV license 标识;  预留字段, 暂无需 SDK 填写;
    std::unordered_map<std::string, std::string> payload;  // 可选, 扁平 KV，值为数值或字符串；为空时序列化成 "payload":{};

    AIGC_JSON_HELPER(type, event, timestamp, license_id, payload)
};


}  // namespace flight_hub_sdk
