// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cfloat>
#include <climits>
#include <vector>
#include <string>
#include <optional>
#include <AIGCJson.hpp>

namespace flight_hub_sdk {

struct CommonResponse {
    int code = -1;
    std::string message;
    std::optional<std::string> data;
    AIGC_JSON_HELPER(code, message, data);
};


// Common 分页信息;
struct CommonPaginationInfo {
    int page = 0;          // 当前页码;
    int page_size = 0;     // 分页大小;
    int total = 0;         // 总数量;

    // register class-members;
    AIGC_JSON_HELPER(page, page_size, total);
};




/// DeviceTopoResponse — 匹配 GET /device/api/v1/manager/properties 响应格式

struct DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValueDeviceCameraVideo {
    std::vector<std::string> switchable_video_types;
    std::string video_index;
    std::string video_type;
    AIGC_JSON_HELPER(switchable_video_types, video_index, video_type);
};

struct DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValueDeviceCamera {
    int available_video_number = 0;
    std::string camera_index;
    int coexist_video_number_max = 0;
    std::vector<DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValueDeviceCameraVideo> video_list;
    AIGC_JSON_HELPER(available_video_number, camera_index, coexist_video_number_max, video_list);
};

struct DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValueDevice {
    int available_video_number = 0;
    int coexist_video_number_max = 0;
    std::string sn;
    std::vector<DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValueDeviceCamera> camera_list;
    AIGC_JSON_HELPER(available_video_number, coexist_video_number_max, sn, camera_list);
};

struct DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValue {
    int available_video_number = 0;
    int coexist_video_number_max = 0;
    std::vector<DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValueDevice> device_list;
    AIGC_JSON_HELPER(available_video_number, coexist_video_number_max, device_list);
};

struct DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacity {
    DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacityValue value;
    AIGC_JSON_HELPER(value);
};

struct DeviceTopoResponseProperties {
    DeviceTopoResponseDeviceItemParentDeviceStateLiveCapacity live_capacity;
    AIGC_JSON_HELPER(live_capacity);
};

struct DeviceTopoResponseDeviceItem {
    std::string sn;
    bool online_status = false;
    DeviceTopoResponseProperties properties;
    AIGC_JSON_HELPER(sn, online_status, properties);
};

struct DeviceTopoResponse {
    std::vector<DeviceTopoResponseDeviceItem> list;
    AIGC_JSON_HELPER(list);
};

/// LiveStreamStartResponse
struct LiveStreamStartResponse {
    std::string expire_ts;
    std::string url_type;
    std::string url;
    std::string publish_uid;
    std::string provider_url;
    AIGC_JSON_HELPER(expire_ts, url_type, url, publish_uid, provider_url);
};

/// UpdateLiveTokenResponse
struct UpdateLiveTokenResponse {
    std::string provider;
    std::string app_id;
    std::string room_id;
    std::string user_id;
    std::string token;
    AIGC_JSON_HELPER(provider, app_id, room_id, user_id, token);
};

/// UpdateLiveTokenListResponse
struct UpdateLiveTokenListResponse {
    std::vector<UpdateLiveTokenResponse> list;
    AIGC_JSON_HELPER(list);
};

/// LiveRecordItemResponse — RecordTask（GET /pf-live/api/v1/record/device/{sn}）
struct LiveRecordItemResponse {
    std::string bid;         // 业务 ID
    std::string sn;          // 设备 SN
    int state = 0;           // 录制状态：0 成功/已结束，1 录制中，2 初始化中，3 失败
    std::string camera;      // 摄像头标识
    std::string created_by;  // 创建者
    int64_t duration = 0;    // 录制时长（秒）
    AIGC_JSON_HELPER(bid, sn, state, camera, created_by, duration);
};

/// GetDeviceLiveRecordsResponse — GetLiveRecordsBySNResponse（data: {"list":[...]}）
struct GetDeviceLiveRecordsResponse {
    std::vector<LiveRecordItemResponse> list;
    AIGC_JSON_HELPER(list);
};

/// 获取在线观众数量 - 响应（POST /pf-live/api/v1/stream/audience-count）
struct DeviceAudienceCountItemResponse {
    std::string sn;
    int count = 0;
    AIGC_JSON_HELPER(sn, count);
};

struct AudienceCountResponse {
    std::vector<DeviceAudienceCountItemResponse> device_list;
    AIGC_JSON_HELPER(device_list);
};

// ---------- 航线任务 HTTP 响应 data 解析 -----------
struct FlightTaskListPaginationResponse {
    int page = 1;
    int page_size = 10;
    int total = 0;
    AIGC_JSON_HELPER(page, page_size, total);
};

struct TaskDetailBreakPointResponse {
    int index = 0;
    int state = 0;
    double progress = 0.0;
    int wayline_id = 0;
    int break_reason = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    double attitude_head = 0.0;
    AIGC_JSON_HELPER(index, state, progress, wayline_id, break_reason, latitude, longitude, height, attitude_head);
};

struct TaskDetailObstacleResponse {
    int id = 0;
    int type = 0;
    int64_t timestamp = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    int wayline_id = 0;
    int waypoint_index = 0;
    AIGC_JSON_HELPER(id, type, timestamp, latitude, longitude, height, wayline_id, waypoint_index);
};

struct TaskDetailResponse {
    std::string uuid;
    std::string name;
    int task_type = 0;
    int status = 0;
    std::string sn;
    std::string landing_dock_sn;
    std::string wayline_uuid;
    std::string begin_at;
    std::string end_at;
    int out_of_control_action_in_flight = 0;
    double rth_altitude = 0;
    int rth_mode = 0;
    int min_battery_capacity = 0;
    int wayline_precision_type = 0;
    std::vector<int> wayline_segment_indexes;
    int current_waypoint_index = 0;
    int total_waypoints = 0;
    std::string time_zone;
    int device_type = 0;
    std::string bid;
    TaskDetailBreakPointResponse break_point;
    std::string completed_at;
    std::string created_at;
    std::string deleted_at;
    bool is_break_point_resume = false;
    int media_upload_status = 0;
    std::vector<TaskDetailObstacleResponse> obstacles;
    std::string run_at;
    std::vector<std::string> tags;
    std::string updated_at;
    double wayline_duration = 0;
    AIGC_JSON_HELPER(uuid, name, task_type, status, sn, landing_dock_sn, wayline_uuid, begin_at, end_at, out_of_control_action_in_flight, rth_altitude, rth_mode, min_battery_capacity, wayline_precision_type, wayline_segment_indexes, current_waypoint_index, total_waypoints, time_zone, device_type, bid, break_point, completed_at, created_at, deleted_at, is_break_point_resume, media_upload_status, obstacles, run_at, tags, updated_at, wayline_duration);
};

struct FlightTaskListDataResponse {
    std::vector<TaskDetailResponse> list;
    FlightTaskListPaginationResponse pagination;
    AIGC_JSON_HELPER(list, pagination);
};

// 一键起飞任务列表 - 响应项;
struct CommandTaskListItemResponse {
    std::string uuid;
    std::string sn;
    std::string drone_sn;
    int status = 0;
    int err_code = 0;
    std::string takeoff_mode;
    double security_takeoff_height = 0.0;
    int max_speed = 0;
    int rth_altitude = 0;
    std::string created_at;
    std::string user_id;

    AIGC_JSON_HELPER(uuid, sn, drone_sn, status, err_code, takeoff_mode,
                     security_takeoff_height, max_speed, rth_altitude, created_at, user_id);
};

// 一键起飞任务列表 - 响应;
struct GetCommandTaskListDataResponse {
    std::vector<CommandTaskListItemResponse> list;
    FlightTaskListPaginationResponse pagination;

    AIGC_JSON_HELPER(list, pagination);
};

// 飞行任务轨迹 - 轨迹点;
struct FlightTaskTrackPointResponse {
    long long timestamp = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(timestamp, latitude, longitude, height);
};

// 飞行任务轨迹 - track 对象;
struct FlightTaskTrackResponse {
    std::string track_id;
    std::string drone_sn;
    int flight_distance = 0;
    int flight_duration = 0;
    std::vector<FlightTaskTrackPointResponse> points;

    AIGC_JSON_HELPER(track_id, drone_sn, flight_distance, flight_duration, points);
};

// 飞行任务轨迹 - 响应;
struct GetFlightTaskTrackDataResponse {
    std::string uuid;
    std::string run_at;
    std::string completed_at;
    std::string name;
    int status = 0;
    std::string wayline_uuid;
    FlightTaskTrackResponse track;

    AIGC_JSON_HELPER(uuid, run_at, completed_at, name, status, wayline_uuid, track);
};

struct CreateFlightTaskResponse {
    std::string uuid;
    std::vector<int> wayline_validity_check_codes;
    AIGC_JSON_HELPER(uuid, wayline_validity_check_codes);
};

struct TCAInfoResponse {
    std::string center_sn;
    std::string data_json;  // 可扩展为具体字段
    AIGC_JSON_HELPER(center_sn);
};

struct GetTCAInfosDataResponse {
    std::vector<TCAInfoResponse> tcas;
    AIGC_JSON_HELPER(tcas);
};

/// OpenRtChannelResponse
struct OpenDataChannelResponseRtChannelConfig {
    // for mqtt_broker
    std::string addr;
    std::string client_id;
    std::string username;
    std::string password;
    std::string expire_time;
    std::string default_frequency;
    std::string method_frequency;
    // for volc_rtc
    std::string app_id;
    std::string room_id;
    std::string user_id;
    std::string token;
    // for drtc_rtc
    std::string provider_url;  // arbitUrl，仲裁服务 URL
    // for mqtt_broker DRC
    std::string up_topic;
    std::string down_topic;
    AIGC_JSON_HELPER(addr, client_id, username, password, expire_time, default_frequency, method_frequency, app_id, room_id, user_id, token, provider_url, up_topic, down_topic);
};

struct OpenDataChannelResponseRtChannel {
    std::string provider;
    OpenDataChannelResponseRtChannelConfig config;
    AIGC_JSON_HELPER(provider, config);
};

struct OpenDataChannelResponse {
    OpenDataChannelResponseRtChannel data_channel;
    AIGC_JSON_HELPER(data_channel);
};

/// FlyToPointResponse
struct FlyToPointResponse {
    std::string fly_to_id;
    AIGC_JSON_HELPER(fly_to_id);
};

/// CreateInFlightWaylineTaskResponse 创建空中下发航线任务
struct CreateInFlightWaylineTaskResponse {
    std::string flight_task_id;
    std::string in_flight_way_line_task_id;
    AIGC_JSON_HELPER(flight_task_id, in_flight_way_line_task_id);
};

/// InFlightTaskInFlightResponse 空中航线子任务详情
struct InFlightTaskInFlightResponse {
    std::string uuid;
    std::string user_id;
    int32_t status = 0;
    std::string wayline_id;
    int32_t err_code = 0;
    int32_t way_point_index = 0;
    int32_t progress_percent = 0;
    double wayline_duration = 0.0;
    double wayline_length = 0.0;
    std::string task_name;
    AIGC_JSON_HELPER(uuid, user_id, status, wayline_id, err_code, way_point_index, progress_percent,
                     wayline_duration, wayline_length, task_name);
};

/// GetActiveInFlightWaylineTaskResponse 查询活跃空中航线任务
struct GetActiveInFlightWaylineTaskResponse {
    std::string flight_id;
    int32_t flight_type = 0;
    std::string sn;
    InFlightTaskInFlightResponse in_flight_task;
    std::string flight_task_data;
    AIGC_JSON_HELPER(flight_id, flight_type, sn, in_flight_task, flight_task_data);
};

struct DeviceListResponsePaginationInfo {
    int page_num = 0;    // 当前页;
    int page_size = 0;   // 页数据条数;
    int total = 0;       // 总数据条数;
    AIGC_JSON_HELPER(page_num, page_size, total);
};

struct DeviceListResponseDeviceModel {
    std::string key;            // 设备sn
    std::string domain;         // 产品 domain;
    std::string type;           // 产品类型;
    std::string sub_type;       // 产品子类型;
    std::string name;           // 类型名称;
    std::string device_class;   // 类型;
    std::string develop_code;   // 开发代码;
    AIGC_JSON_HELPER(key, domain, type, sub_type, name, device_class, develop_code);
    AIGC_JSON_HELPER_RENAME("key", "domain", "type", "sub_type", "name", "class", "develop_code");
};

struct DeviceListResponseItem {
    std::string device_sn;              // 设备sn;
    std::string nickname;               // 设备昵称;
    std::string active_group_id;
    std::string complete_machine_sn;    // 设备铭牌sn;
    bool device_online_status = false;  // 设备是否在线;

    std::string device_model_key;       // 设备上云枚举值;
    std::string device_offline_time;    // 设备离线时间;

    DeviceListResponseDeviceModel device_model;


    AIGC_JSON_HELPER(device_sn, nickname, active_group_id, complete_machine_sn, device_online_status, device_model_key, device_offline_time, device_model);
};


struct DeviceListResponse {
    DeviceListResponsePaginationInfo pagination;
    std::vector<DeviceListResponseItem> list;
    AIGC_JSON_HELPER(pagination, list);
};


struct DeviceTopoListResponsePaginationInfo {
    int page_num = 0;    // 当前页;
    int page_size = 0;   // 页数据条数;
    int total = 0;       // 总数据条数;
    AIGC_JSON_HELPER(page_num, page_size, total);
};

struct DeviceTopoListResponseDeviceModel {
    std::string model_key;
    std::string name;
    std::string device_class;
    AIGC_JSON_HELPER(model_key, name, device_class);
    AIGC_JSON_HELPER_RENAME("model_key", "name", "class");
};

struct DeviceTopoListResponseSubDeviceItem {
    std::string device_sn;
    std::string nickname;
    bool online_status = false;
    DeviceTopoListResponseDeviceModel model_info;
    std::vector<std::string> parent_sns;

    AIGC_JSON_HELPER(device_sn, nickname, online_status, model_info, parent_sns);
};

struct DeviceTopoListResponseItem {
    std::string device_sn;
    std::string nickname;         // 设备昵称(来自 device_inside_callsign 字段);
    bool online_status = false;

    DeviceTopoListResponseDeviceModel model_info;
    std::vector<DeviceTopoListResponseSubDeviceItem> sub_devices;

    AIGC_JSON_HELPER(device_sn, nickname, online_status, model_info, sub_devices);
};

struct DeviceTopoListResponse {
    DeviceTopoListResponsePaginationInfo pagination;
    std::vector<DeviceTopoListResponseItem> list;
    AIGC_JSON_HELPER(pagination, list);
};

struct POITaskWaypoint {
    double latitude = 0;
    double longitude = 0;
    double height = 0;
    AIGC_JSON_HELPER(latitude, longitude, height);
};

struct POITaskResponse {
    std::string uuid;           // POI任务UUID（旧字段名: task_uuid）
    std::string device_sn;      // 设备SN（机场/遥控器）（旧字段名: gateway_sn）
    std::string sn;             // 飞行器SN（旧字段名: drone_sn）
    int status = INT_MIN;       ///< 任务状态（HTTP JSON 整型）；取值语义同 `flight_hub_sdk::POITaskStatus`，定义见 `api/value.h`。
    int err_code = INT_MIN;     // 默认 INT_MIN 为 无效值;
    POITaskWaypoint poi_center_point;  // POI中心点坐标（旧字段名: center_point）
    double speed = 0;           // 环绕速度（米/秒）
    double circle_radius = 0;   // 环绕半径（米）（旧字段名: radius）
    std::string payload_index;  // 负载索引

    AIGC_JSON_HELPER(uuid, device_sn, sn, status, err_code,
                     poi_center_point, speed, circle_radius, payload_index);
};

/// AcquireCtrlAuthorityResponse
struct AcquireCtrlAuthorityResponse {
    std::string drone_sn;
    AIGC_JSON_HELPER(drone_sn);
};

/// ReleaseCtrlAuthorityResponse
struct ReleaseCtrlAuthorityResponse {
    std::string drone_sn;
    AIGC_JSON_HELPER(drone_sn);
};

struct NonRealtimeCmdResponse {
    int code = 0;
    std::string message;
    AIGC_JSON_HELPER(code, message);
};

/// Psdk
struct SpeakerResourceResponse {
    uint32_t id = 0;
    std::string name;
    std::string file_type;
    std::string object_key;
    std::string object_url;
    std::string format;
    int64_t size = 0;
    int64_t duration = 0;
    std::string state;
    std::string state_message;
    std::string pcm_object_key;
    std::string pcm_object_url;
    int64_t pcm_size = 0;
    std::string pcm_md5_sum;
    std::string content;
    std::string md5_sum;
    int64_t created_at = 0;
    
    AIGC_JSON_HELPER(id, name, file_type, object_key, object_url, format, size, 
                     duration, state, state_message, pcm_object_key, pcm_object_url, 
                     pcm_size, pcm_md5_sum, content, md5_sum, created_at);
};

struct PsdkSpeakerResourceListResponse {
    std::vector<SpeakerResourceResponse> resources;
    AIGC_JSON_HELPER(resources);
    AIGC_JSON_HELPER_RENAME("list");
};

struct CreateSpeakerResourceResponse {
    uint32_t id = 0;
    std::string name;
    std::string file_type;
    std::string object_key;
    std::string object_url;
    std::string format;
    int64_t size = 0;
    int64_t duration = 0;
    std::string state;
    std::string state_message;
    std::string pcm_object_key;
    std::string pcm_object_url;
    int64_t pcm_size = 0;
    std::string pcm_md5_sum;
    std::string content;
    std::string md5_sum;
    int64_t created_at = 0;
    
    AIGC_JSON_HELPER(id, name, file_type, object_key, object_url, format, size, 
                     duration, state, state_message, pcm_object_key, pcm_object_url, 
                     pcm_size, pcm_md5_sum, content, md5_sum, created_at);
};

struct EditSpeakerResourceResponse {
    uint32_t id = 0;
    std::string name;
    std::string file_type;
    std::string object_key;
    std::string object_url;
    std::string format;
    int64_t size = 0;
    int64_t duration = 0;
    std::string state;
    std::string state_message;
    std::string pcm_object_key;
    std::string pcm_object_url;
    int64_t pcm_size = 0;
    std::string pcm_md5_sum;
    std::string content;
    std::string md5_sum;
    int64_t created_at = 0;
    
    AIGC_JSON_HELPER(id, name, file_type, object_key, object_url, format, size, 
                     duration, state, state_message, pcm_object_key, pcm_object_url, 
                     pcm_size, pcm_md5_sum, content, md5_sum, created_at);
};

struct STSCredentialResponse {
    std::string access_key_id;
    std::string access_key_secret;
    int expire = 0;
    std::string security_token;
    int platform = 0;  // 直播专用，后续再废弃

    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token, platform);
};

struct STSInfoResponse {
    std::string object_key_prefix;
    std::string bucket;
    std::string endpoint;
    std::string provider;
    std::string region;
    STSCredentialResponse credentials;

    AIGC_JSON_HELPER(object_key_prefix, bucket, endpoint, provider, region, credentials);
};

struct PsdkWidgetResourceResponse {
    std::string object_key;
    std::string url;
    
    AIGC_JSON_HELPER(object_key, url);
};

struct PsdkWidgetResourceListResponse {
    std::vector<PsdkWidgetResourceResponse> ui_resource_urls;
    
    AIGC_JSON_HELPER(ui_resource_urls);
};

/// DroneTakeoffResponse — CommandResponse / DroneTakeoffResponse
struct DroneTakeoffResponse {
    std::string flight_id;  // 飞行任务 ID
    std::string fly_to_id;  // 指点飞行任务 ID
    AIGC_JSON_HELPER(flight_id, fly_to_id);
};

/// GenerateTryFlightWaylineResponse (试飞航线生成响应，用于 JSON 解析)
struct GenerateTryFlightWaylineResponseData {
    std::string key;
    std::string name;
    std::string uuid;
    std::string request_id;
    AIGC_JSON_HELPER(key, name, uuid, request_id);
};

/// CreateTestFlightTaskResponseData (试飞航线下发响应)
struct CreateTestFlightTaskResponseTask {
    std::string uuid;
    int begin_at = 0;
    int end_at = 0;
    AIGC_JSON_HELPER(uuid, begin_at, end_at);
};

struct CreateTestFlightTaskResponseData {
    CreateTestFlightTaskResponseTask task;
    AIGC_JSON_HELPER(task);
};

/// Media file list API response（使用 int 避免 AIGCJson 对 int64_t 遇 null 解析失败）
struct MediaFileListItemResponse {
    int id = 0;
    std::string uuid;
    int file_type = 0;
    int sub_file_type = 0;
    std::string name;
    std::string suffix;
    int size = 0;
    bool has_child = false;
    bool has_position = false;
    std::string create_at;
    std::string update_at;
    bool show_on_map = false;
    std::string preview_url;
    std::string original_url;
    AIGC_JSON_HELPER(id, uuid, file_type, sub_file_type, name, suffix, size, has_child, has_position,
                     create_at, update_at, show_on_map, preview_url, original_url);
};

struct MediaFileListPaginationResponse {
    int page = 1;
    int page_size = 30;
    int total = 0;
    AIGC_JSON_HELPER(page, page_size, total);
};

struct MediaFileListAncestorResponse {
    std::string name;
    int id = 0;
    std::string uuid;
    int file_type = 0;
    int sub_file_type = 0;
    AIGC_JSON_HELPER(name, id, uuid, file_type, sub_file_type);
};

struct MediaFileListResponse {
    std::vector<MediaFileListItemResponse> list;
    MediaFileListPaginationResponse pagination;
    std::vector<MediaFileListAncestorResponse> ancestors;
    AIGC_JSON_HELPER(list, pagination, ancestors);
};

struct OpenDebugModeResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct SetDistanceLimitResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct SetHeightLimitResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct SetObstacleAvoidanceResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct SetRemainingPowerForRthResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct SetNightLightsStateResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct AlarmStateSwitchResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct EsimOperatorSwitchResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct SimSlotSwitchResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct SetBatteryStoreModeResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct ManualBatterySwapResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct ManualBatterySwapProgressResponse {
    std::string bid;
    std::string status;
    std::string data_json;  // 透传整段 data，非 JSON 反序列化字段
    AIGC_JSON_HELPER(bid, status);
};

struct DonglePowerSwitchResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct LowPowerSwitchResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct EsimOperatorSwitchProgressResponse {
    std::string bid;
    std::string status;
    std::string data_json;  // 透传整段 data，非 JSON 反序列化字段
    AIGC_JSON_HELPER(bid, status);
};

struct CloseDebugModeResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct OpenDockCoverResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct OpenDockCoverProgressResponse {
    std::string bid;
    std::string status;
    double create_time = 0;
    double update_time = 0;
    AIGC_JSON_HELPER(bid, status, create_time, update_time);
};

struct CloseDockCoverResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct CloseDockCoverProgressResponse {
    std::string bid;
    std::string status;
    double create_time = 0;
    double update_time = 0;
    AIGC_JSON_HELPER(bid, status, create_time, update_time);
};

struct DeviceRebootResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct DeviceRebootProgressResponse {
    std::string bid;
    std::string status;
    double create_time = 0;
    double update_time = 0;
    AIGC_JSON_HELPER(bid, status, create_time, update_time);
};

struct DeviceFormatResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct DeviceFormatProgressResponse {
    std::string bid;
    std::string status;
    double create_time = 0;
    double update_time = 0;
    AIGC_JSON_HELPER(bid, status, create_time, update_time);
};

struct DroneOpenResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct DroneCloseResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct OpenChargeResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct CloseChargeResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

// DroneStatus Start
struct DroneStatusBatteryItem {
    int capacity_percent = 0;
    std::string firmware_version;
    int high_voltage_storage_days = 0;
    int index = 0;
    int loop_times = 0;
    std::string sn;
    int sub_type = 0;
    double temperature = 0.0;
    int type = 0;
    int voltage = 0;
    AIGC_JSON_HELPER(capacity_percent, firmware_version, high_voltage_storage_days, index, loop_times, sn, sub_type, temperature, type, voltage);
};

struct DroneStatusBattery {
    std::vector<DroneStatusBatteryItem> batteries;
    int capacity_percent = 0;
    int landing_power = 0;
    int remain_flight_time = 0;
    int remain_job_time = 0;
    int return_home_power = 0;
    AIGC_JSON_HELPER(batteries, capacity_percent, landing_power, remain_flight_time, remain_job_time, return_home_power);
};

struct DroneStatusDistanceLimitStatus {
    int distance_limit = 0;
    int is_near_distance_limit = 0;
    int state = 0;
    AIGC_JSON_HELPER(distance_limit, is_near_distance_limit, state);
};

struct DroneStatusObstacleAvoidance {
    int downside = 0;
    int horizon = 0;
    int upside = 0;
    AIGC_JSON_HELPER(downside, horizon, upside);
};

struct DroneStatusStorage {
    int64_t total = 0;
    int64_t used = 0;
    AIGC_JSON_HELPER(total, used);
};

struct DroneStatusPositionState {
    int gps_number = 0;
    int is_fixed = 0;
    int quality = 0;
    int rtk_number = 0;
    AIGC_JSON_HELPER(gps_number, is_fixed, quality, rtk_number);
};

struct DroneStatusProperties {
    DroneStatusBattery battery;
    DroneStatusDistanceLimitStatus distance_limit_status;
    int height_limit = 0;
    double home_distance = 0.0;
    double home_latitude = 0.0;
    double home_longitude = 0.0;
    int horizontal_speed = 0;
    int low_battery_warning_threshold = 0;
    int mode_code = 0;
    int mode_code_reason = 0;
    int night_lights_state = 0;
    DroneStatusObstacleAvoidance obstacle_avoidance;
    DroneStatusPositionState position_state;
    int remaining_power_for_return_home = 0;
    int serious_low_battery_warning_threshold = 0;
    DroneStatusStorage storage;
    double total_flight_distance = 0.0;
    int total_flight_sorties = 0;
    double total_flight_time = 0.0;
    int vertical_speed = 0;
    AIGC_JSON_HELPER(
        battery, distance_limit_status, height_limit, home_distance, home_latitude, home_longitude,
        horizontal_speed, low_battery_warning_threshold, mode_code, mode_code_reason, night_lights_state,
        obstacle_avoidance, position_state, remaining_power_for_return_home, serious_low_battery_warning_threshold,
        storage, total_flight_distance, total_flight_sorties, total_flight_time, vertical_speed
    );
};

struct DroneStatusDeviceModel {
    std::string device_class;
    std::string domain;
    std::string key;
    std::string name;
    AIGC_JSON_HELPER(device_class, domain, key, name);
    AIGC_JSON_HELPER_RENAME("class", "domain", "key", "name");
};

struct DroneStatusItem {
    std::string sn;
    bool online_status = false;
    int64_t updated_time = 0;
    DroneStatusDeviceModel device_model;
    DroneStatusProperties properties;
    AIGC_JSON_HELPER(sn, online_status, updated_time, device_model, properties);
};

struct DroneStatusResponse {
    std::vector<DroneStatusItem> list;
    AIGC_JSON_HELPER(list);
};
// DroneStatus End

// DockStatus Start
struct DockStatusAirConditioner {
    int air_conditioner_state = 0;
    int switch_time = 0;
    AIGC_JSON_HELPER(air_conditioner_state, switch_time);
};

struct DockStatusAlternateLandPoint {
    double height = 0.0;
    int is_configured = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double safe_land_height = 0.0;
    AIGC_JSON_HELPER(height, is_configured, latitude, longitude, safe_land_height);
};

struct DockStatusBackupBattery {
    int switch_ = 0;
    double temperature = 0.0;
    int voltage = 0;
    AIGC_JSON_HELPER(switch_, temperature, voltage);
};

struct DockStatusNetworkState {
    int quality = 0;
    int rate = 0;
    int type = 0;
    AIGC_JSON_HELPER(quality, rate, type);
};

struct DockStatusPositionState {
    int gps_number = 0;
    int is_calibration = 0;
    int is_fixed = 0;
    int quality = 0;
    int rtk_number = 0;
    AIGC_JSON_HELPER(gps_number, is_calibration, is_fixed, quality, rtk_number);
};

struct DockStatusTiltAngle {
    int valid = 0;
    double value = 0.0;
    AIGC_JSON_HELPER(valid, value);
};

struct DockStatusProperties {
    int acc_time = 0;
    double acdc_power_input = 0.0;
    DockStatusAirConditioner air_conditioner;
    DockStatusAlternateLandPoint alternate_land_point;
    DockStatusBackupBattery backup_battery;
    int electric_supply_voltage = 0;
    double environment_temperature = 0.0;
    int humidity = 0;
    int job_number = 0;
    DockStatusNetworkState network_state;
    int poe_link_status = 0;
    double poe_power_output = 0;
    DockStatusPositionState position_state;
    int rainfall = 0;
    double temperature = 0.0;
    DockStatusTiltAngle tilt_angle;
    int working_voltage = 0;

    AIGC_JSON_HELPER(
        acc_time, acdc_power_input, air_conditioner, alternate_land_point, backup_battery,
        electric_supply_voltage, environment_temperature, humidity, job_number, network_state,
        poe_link_status, poe_power_output, position_state, rainfall, temperature, tilt_angle, working_voltage
    );
};

struct DockStatusDeviceModel {
    std::string device_class;
    std::string domain;
    std::string key;
    std::string name;
    AIGC_JSON_HELPER(device_class, domain, key, name);
    AIGC_JSON_HELPER_RENAME("class", "domain", "key", "name");
};

struct DockStatusItem {
    std::string sn;
    bool online_status = false;
    int64_t updated_time = 0;
    DockStatusDeviceModel device_model;
    DockStatusProperties properties;
    AIGC_JSON_HELPER(sn, online_status, updated_time, device_model, properties);
};

struct DockStatusResponse {
    std::vector<DockStatusItem> list;
    AIGC_JSON_HELPER(list);
};
// DockStatus End

struct DockSilentModeResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct DockAirConditionerResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};


/******************************************************
 * Rename members
 * eg:
 * struct Test
 * {
 *      string A;
 *      string B;
 *      AIGC_JSON_HELPER(A, B)
 *      AIGC_JSON_HELPER_RENAME("a", "b")
 * };         
 ******************************************************/
// 信号网格数据;
struct GridItem {
    std::string h3_index;        // Uber H3网格下标，等级为 11;
    int sdr_avg = 0;             // 平均sdr信号强度;
    int sdr_max = 0;             // 最大sdr信号强度;
    int sdr_min = 0;             // 最小sdr信号强度;

    int signal_4g_avg = 0;       // 平均4g信号强度, 如果没有插入4G模块, 则无此字段;
    int signal_4g_max = 0;       // 最大4g信号强度, 如果没有插入4G模块, 则无此字段;
    int signal_4g_min = 0;       // 最小4g信号强度, 如果没有插入4G模块, 则无此字段;

    // register class-members;
    AIGC_JSON_HELPER(h3_index, sdr_avg, sdr_max, sdr_min, signal_4g_avg, signal_4g_max, signal_4g_min);
    // rename class-members;
    AIGC_JSON_HELPER_RENAME("h3_index", "sdr_avg", "sdr_max", "sdr_min", "4g_avg", "4g_max", "4g_min");
};


struct DeviceSignalInfo {
    std::string device_sn;
    std::vector<GridItem> grid;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, grid);
};

struct SignalMapsResponse {
    std::vector<DeviceSignalInfo> list;

    // register class-members;
    AIGC_JSON_HELPER(list);
};


// 创建飞行区域返回值, 协议 response data 部分的数据;
struct CreateFlightAreasResponse {
    std::string id;
    std::string name;

    // register class-members;
    AIGC_JSON_HELPER(id, name);
};


// 自定义飞行区域: Geometry;
struct ContentsGeometryResponse {
    // type: "Circle", 圆形: 限飞区和作业区支持;
    // type: "Polygon", 多边形: 限飞区和作业区支持;
    // type: "Polyline", 自定义(折线): 折线仅禁降区支持;  
    std::string type;
    std::vector<std::vector<std::vector<double>>> coordinates;  // [折线] [多边形] 顶点坐标;

    std::vector<double> center;  // 创建圆形区域的时候传入： 圆心坐标;
    double radius = 0;           // 半径, 创建圆形区域的时候传入;

    // register class-members;
    AIGC_JSON_HELPER(type, coordinates, center, radius);
};


// 自定区域: 属性;
struct ContentsPropertiesResponse {
    std::string color;             // 颜色, 颜色值用于方便在地图上面区分不同区域, 有效值比如 "#FF0000"
    int width = 0;                 // 宽度, 多边形/线状类型需要传入, 用于表示周边范围
    bool clampToGround = false;    // 服务端(旧协议设计上字段是 "clampToGround");

    // register class-members;
    AIGC_JSON_HELPER(color, width, clampToGround);
};


struct FlightAreaInfoContentsResponse {
    std::string type;
    ContentsGeometryResponse geometry;
    ContentsPropertiesResponse properties;

    // register class-members;
    AIGC_JSON_HELPER(type, geometry, properties);
};


// list item;
struct FlightAreaInfo {
    std::string id;                  // 飞行区id;
    std::string name;                // 飞行区名称;
    std::string status;              // 飞行区状态;
    std::string type;                // 飞行区类型;
    FlightAreaInfoContentsResponse content;  // 飞行区内容;

    long created_time;               // 创建时间;
    std::string created_by;          // 创建用户 id;
    long updated_time;               // 更新时间;
    std::string updated_by;          // 更新用户 id;

    // register class-members;
    AIGC_JSON_HELPER(id, name, status, type, content, created_time, created_by, updated_time, updated_by);
};



// FlightAreaInfo 分页信息;
struct FlightAreaPaginationInfo {
    int page = 0;          // 当前页码;
    int page_size = 0;     // 分页大小;
    int total = 0;         // 总数量;

    // register class-members;
    AIGC_JSON_HELPER(page, page_size, total);
};


struct GetFlightAreasResponse {
    std::vector<FlightAreaInfo> list;
    FlightAreaPaginationInfo pagination;

    // register class-members;
    AIGC_JSON_HELPER(list, pagination);
};


struct UpdatedAreaContentsResponse {
    std::string type;
    ContentsGeometryResponse geometry;
    ContentsPropertiesResponse properties;

    // register class-members;
    AIGC_JSON_HELPER(type, geometry, properties);
};


struct UpdatedAreaResponse {
    std::string id;
    std::string name;
    std::string status;
    std::string type;

    UpdatedAreaContentsResponse content;  // 飞行区内容;

    long created_time = 0;
    std::string created_by;
    long updated_time = 0;
    std::string updated_by;

    // register class-members;
    AIGC_JSON_HELPER(id, name, status, type, content, created_time, created_by, updated_time, updated_by);
};


struct UpdateFlightAreasResponse {
    std::string id;
    UpdatedAreaResponse updated_area;

    // register class-members;
    AIGC_JSON_HELPER(id, updated_area);
};

struct DeviceSyncFlightAreasResults {
    std::string device_sn;    // 待同步设备的SN, 支持网关类型设备(机场1、2、3 以及 地面站);
    int code = -1;            // {0: 下发成功, 230012: 设备已离线, 230013: 不支持的设备, 230014: 指令下发失败, 230015: 不支持的地面站 };

    // register class-members;
    AIGC_JSON_HELPER(device_sn, code);
};


// 同步自定义飞行区;
struct SyncFlightAreasResponse {
    std::vector<DeviceSyncFlightAreasResults> results;

    // register class-members;
    AIGC_JSON_HELPER(results);
};


struct DeviceFlightAreasSyncStatusResponse {
    // 待同步设备的SN, 支持网关类型设备(机场1、2、3 以及 地面站);
    std::string sn;
    // 同步状态;
    std::string status;
    // 错误码;
    int reason = 0;


    // register class-members;
    AIGC_JSON_HELPER(sn, status, reason);
};


// 获取设备同步状态;
struct GetAreasSyncStatusResponse {
    std::vector<DeviceFlightAreasSyncStatusResponse> list;

    // register class-members;
    AIGC_JSON_HELPER(list);
};


struct OfflineMapsModelInfo {
    std::string uuid;

    // register class-members;
    AIGC_JSON_HELPER(uuid);
};

struct OfflineMapsModelInfoResponse {
    int id = 0;                // 离线地图模型ID;
    long updated_time = 0;     // 更新时间;
    std::vector<OfflineMapsModelInfo> models;  // 模型信息;
    int percent = 0;
    std::string status;
    int result = 0;

    // register class-members;
    AIGC_JSON_HELPER(id, updated_time, models, percent, status, result);
};


// 查询离线地图模型信息;
struct GetOfflineMapsModelInfoResponse {
    bool disable = false;  //  是否被禁用;
    OfflineMapsModelInfoResponse offline_map;

    // register class-members;
    AIGC_JSON_HELPER(disable, offline_map);
};


// 设备同步状态;
struct DeviceSyncStatus {
    // 待同步设备的SN;
    std::string sn;
    // 同步状态;
    std::string status;
    // 错误码;
    int reason = 0;


    // register class-members;
    AIGC_JSON_HELPER(sn, status, reason);
};

// 查询离线地图同步状态;
struct GetOfflineMapsSyncStatusResponse {
    std::vector<DeviceSyncStatus> list;

    // register class-members;
    AIGC_JSON_HELPER(list);
};


struct DeviceSyncInfo {
    std::string device_sn;   // 待同步设备的SN;
    int code = -1;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, code);
};


struct OfflineMapsSyncResponse {
    std::vector<DeviceSyncInfo> results;

    // register class-members;
    AIGC_JSON_HELPER(results);
};


// 位置信息;
struct PositionInfo {
    double lat = 0.0;                           // 纬度;
    double lng = 0.0;                           // 经度;
    double abs_alt = 0.0;                       // 绝对高度;
    double rel_alt = 0.0;                       // 相对高度;

    // register class-members;
    AIGC_JSON_HELPER(lat, lng, abs_alt, rel_alt);
};


// 媒体文件列表;
// 扩展数据(图片才会返回);
struct MediaFilesListItemExt {
    std::string create_time;                    // 创建时间;
    double flight_yaw = 0;                      // 飞行偏航角;
    double focal_length = 0.0;                  // 焦距;
    double gimbal_pitch = 0.0;                  // 云台俯仰角;
    double gimbal_roll = 0.0;                   // 云台横滚角;
    double gimbal_yaw = 0.0;                    // 云台偏航角;
    int width = 0;                              // 图片宽度;
    int height = 0;                             // 图片高度;
    std::string image_type;                     // 图片类型; { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera" }, 空字符串表示无效数据;
    PositionInfo photo_position;                // 图片位置信息;
    PositionInfo shoot_position;                // 飞机位置信息;
    double zoom_factor = INT_MIN;               // 焦距倍数, 服务端可能无返回值, 默认为 INT_MIN 无效值(一个很小的负数);

    // register class-members;
    AIGC_JSON_HELPER(
        create_time, flight_yaw, focal_length, gimbal_pitch, gimbal_roll, gimbal_yaw, width, height,
        image_type, photo_position, shoot_position, zoom_factor
    );
};


// 媒体文件列表 item 数据;
struct MediaFilesListItemInfo {
    int64_t file_id = -1;                      // 文件id(-1 表示默认无效的id);
    std::string flight_id;                     // 飞行记录id;
    std::string wayline_action_uuid;           // 航点动作id;
    int64_t size = 0;                          // 文件大小(字节);
    std::string object_key;                    // 文件存储对象key;
    std::string file_name;                     // 文件名;
    std::string suffix;                        // 文件后缀名;
    MediaFilesListItemExt ext;                 // 扩展数据(图片才会返回);
    std::string created_at;                    // 创建时间;
    std::string file_path;                     // 文件路径;
    std::string device_sn;                     // 设备 SN;
    std::string payload_type;                  // 负载类型;
    int type = 0;                              // 文件类型; 取值参见 MediaType 枚举; 媒体列表接口实际只会出现 0(未定义)/1(文件夹)/2(图片)/3(视频);

    // register class-members;
    AIGC_JSON_HELPER(
        file_id, flight_id, wayline_action_uuid, size, object_key, file_name, suffix, ext, created_at,
        file_path, device_sn, payload_type, type
    );
};


// 获取媒体文件列表 response 数据;
struct GetMediaFilesListResponse {
    std::vector<MediaFilesListItemInfo> list;  // 数据列表;
    int page_size = 0;                         // 页数据大小;
    int next_index = 0;                        // 下一页的起始索引, 如果是最后一页, 则返回-1(表示获取完毕);
    int total = 0;                             // 总记录数;

    // register class-members;
    AIGC_JSON_HELPER(list, page_size, next_index, total);
};


struct GetMediaFilesDownloadUrlResponse {
    std::string url;                           // 下载地址;

    // register class-members;
    AIGC_JSON_HELPER(url);
};


struct MediaFileDownloadData {
    std::string object_key;
    std::string url;

    // register class-members;
    AIGC_JSON_HELPER(object_key, url);
};

class GetMediaFilesDownloadUrlListResponse {
public:
    std::vector<MediaFileDownloadData> list;

    // register class-members;
    AIGC_JSON_HELPER(list);
};


 struct FlightRecordExportProgressInfoItem {
     int64_t id = -1;                     // 文件id;
     std::string export_id;               // 任务 uuid; 服务端字段名为 "uuid";
     std::string created_at;              // 创建时间;
     std::string content_type;            // 导出类型, 比如 "details";
     int status = -1;                     // 导出状态;
     int progress = -1;                   // 导出进度;
     int failed_reason_code = -1;         // 导出失败原因码;
     std::string object_key;              // 文件存储对象 key;
     std::string file_name;               // 文件名;
     std::string user_id;                 // 用户 id;
     std::string prj_uuid;                // 项目 uuid;
     std::vector<std::string> file_type;  // 文件类型, 比如 ["Excel"];
     std::string download_url;            // 下载地址;

     // register class-members;
     // 服务端实际返回字段：uuid(非export_id), 其余字段名一致;
     AIGC_JSON_HELPER(
         id, export_id, created_at, content_type, status, progress, failed_reason_code,
         object_key, file_name, user_id, prj_uuid, file_type, download_url
     );
     AIGC_JSON_HELPER_RENAME(
         "id", "uuid", "created_at", "content_type", "status", "progress", "failed_reason_code",
         "object_key", "file_name", "user_id", "prj_uuid", "file_type", "download_url"
     );
 };


 struct GetExportFlightRecordsProgressResponse {
     // 服务端返回扁平分页字段（page/page_size/total 与 data 同级），非嵌套 pagination 对象;
     // 用三个独立字段接收，再在 impl 层手动组装 results.pagination;
     int page = 0;
     int page_size = 0;
     int total = 0;
     std::vector<FlightRecordExportProgressInfoItem> list;  // 服务端字段名为 "list";
 
     // register class-members;
     AIGC_JSON_HELPER(page, page_size, total, list);
     AIGC_JSON_HELPER_RENAME("page", "page_size", "total", "list");
 };


struct ExportFlightRecordsResponse {
    std::string export_id;

    // register class-members;
    AIGC_JSON_HELPER(export_id);
};


struct DroneFormatResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct DroneSdrWorkModeResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct RtkCalibrationResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct StopNetworkSpeedTestResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct StartNetworkSpeedTestResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

struct NetworkSpeedTestDataResponse {
    double download_speed = 0;
    double upload_speed = 0;
    AIGC_JSON_HELPER(download_speed, upload_speed);
};

struct NetworkSpeedTestProgressResponse {
    std::string bid;
    std::string status;
    double create_time = 0;
    double update_time = 0;
    NetworkSpeedTestDataResponse speed_result;
    AIGC_JSON_HELPER(bid, status, create_time, update_time, speed_result);
};

struct DeviceLogListResponseLogItem {
    int boot_index = 0;
    int64_t start_time = 0;
    int64_t end_time = 0;
    int64_t size = 0;
    std::string md5;
    AIGC_JSON_HELPER(boot_index, start_time, end_time, size, md5);
};

struct DeviceLogListResponseFileItem {
    std::string device_sn;
    std::string module;
    int result = 0;
    std::vector<DeviceLogListResponseLogItem> list;
    std::string ObjectKey;
    int log_type = 0;
    int responsibility_log_type = 0;
    AIGC_JSON_HELPER(device_sn, module, result, list, ObjectKey, log_type, responsibility_log_type);
    // 云端返回 object_key，成员名为 ObjectKey
    AIGC_JSON_HELPER_RENAME("device_sn", "module", "result", "list", "object_key", "log_type", "responsibility_log_type");
};

struct DeviceLogListResponse {
    std::vector<DeviceLogListResponseFileItem> files;
    AIGC_JSON_HELPER(files);
    // 云端 data 字段为 list，对外 API 仍用 files
    AIGC_JSON_HELPER_RENAME("list");
};

struct CreateDeviceJobProgress {
    int percent = 0;
    int current_step = 0;
    std::string ext; // 可为 null，建议用 string 或 std::optional
    AIGC_JSON_HELPER(percent, current_step, ext);
};

struct CreateDeviceJobData {
    std::string bid;
    std::string sn;
    std::string biz_code;
    std::string status;
    int result = 0;
    CreateDeviceJobProgress progress;
    std::string service_identifier;
    AIGC_JSON_HELPER(bid, sn, biz_code, status, result, progress, service_identifier);
};

struct CreateDeviceLogReportResponse {
    std::string track_number;
    std::string report_uuid;
    std::string error_log_list; // 可为 null，建议用 string 或 std::optional
    CreateDeviceJobData creat_device_job_data;
    AIGC_JSON_HELPER(track_number, report_uuid, error_log_list, creat_device_job_data);
};

struct LogReportListResponsePagination {
    int page = 0;
    int page_size = 0;
    int total = 0;
    AIGC_JSON_HELPER(page, page_size, total);
};

struct LogReportListResponseAppendixFileItem {
    std::string file_name;
    std::string file_object_key;
    int64_t file_size = 0;
    AIGC_JSON_HELPER(file_name, file_object_key, file_size);
};

struct LogReportListResponseAppendixFiles {
    std::vector<LogReportListResponseAppendixFileItem> files;
    AIGC_JSON_HELPER(files);
};

struct LogReportListResponseDeviceTopoItem {
    std::string sn;
    std::string device_model_key;
    std::string device_organization_callsign;
    AIGC_JSON_HELPER(sn, device_model_key, device_organization_callsign);
};

struct LogReportListResponseDeviceTopo {
    std::vector<LogReportListResponseDeviceTopoItem> hosts;
    std::vector<LogReportListResponseDeviceTopoItem> parents;
    // nodes 可为 null，暂不定义
    AIGC_JSON_HELPER(hosts, parents);
};

struct LogReportListResponseContactInformation {
    std::string email;
    std::string phone;
    AIGC_JSON_HELPER(email, phone);
};

struct LogReportListResponseDeviceLogItemList {
    int boot_index = 0;
    int64_t start_time = 0;
    int64_t end_time = 0;
    int64_t size = 0;
    AIGC_JSON_HELPER(boot_index, start_time, end_time, size);
};

struct LogReportListResponseDeviceLog {
    std::string device_sn;
    std::string module;
    int result = 0;
    std::vector<LogReportListResponseDeviceLogItemList> list;
    std::string object_key;
    AIGC_JSON_HELPER(device_sn, module, result, list, object_key);
};

struct LogReportListResponseDeviceLogs {
    std::vector<LogReportListResponseDeviceLog> files;
    AIGC_JSON_HELPER(files);
};

struct LogReportListResponseDeviceUploadFilesItemProgress {
    int current_step = 0;
    int total_step = 0;
    int progress = 0;
    int64_t finish_time = 0;
    int upload_rate = 0;
    int result = 0;
    std::string status;
    AIGC_JSON_HELPER(current_step, total_step, progress, finish_time, upload_rate, result, status);
};

struct LogReportListResponseDeviceUploadFilesItem {
    std::string module;
    int64_t size = 0;
    std::string device_sn;
    std::string key;
    std::string fingerprint;
    LogReportListResponseDeviceUploadFilesItemProgress progress;
    std::string download_url;
    AIGC_JSON_HELPER(module, size, device_sn, key, fingerprint, progress, download_url);
};

struct LogReportListResponseDeviceUploadFiles {
    std::vector<LogReportListResponseDeviceUploadFilesItem> files;
    AIGC_JSON_HELPER(files);
};

struct LogReportListResponseItem {
    std::string report_uuid;
    int64_t report_time = 0;
    std::string report_user_id;
    std::string report_user_organization_callsign;
    LogReportListResponseDeviceTopo device_topo;
    std::string report_information;
    std::string track_number;
    int64_t error_start_time = 0;
    LogReportListResponseAppendixFiles appendix_files;
    LogReportListResponseDeviceLogs device_logs;
    LogReportListResponseDeviceUploadFiles device_upload_files;
    std::string status;
    LogReportListResponseContactInformation report_contact_information;
    std::string bid;
    // device_report_progress 可为 null，暂不定义
    std::string device_report_feedback;
    int read_status = 0;
    std::string url;
    AIGC_JSON_HELPER(
        report_uuid, report_time, report_user_id, report_user_organization_callsign, device_topo,
        report_information, track_number, error_start_time, appendix_files, device_logs,
        device_upload_files, status, report_contact_information, bid, device_report_feedback,
        read_status, url
    );
};

struct LogReportListResponse {
    LogReportListResponsePagination pagination;
    std::vector<LogReportListResponseItem> list;
    AIGC_JSON_HELPER(pagination, list);
};

struct LogReportAppendStsResponseCredentials {
    std::string access_key_id;
    std::string access_key_secret;
    int64_t expire = 0;
    std::string security_token;
    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token);
};

struct LogReportAppendStsResponse {
    LogReportAppendStsResponseCredentials credentials;
    std::string provider;
    std::string bucket;
    std::string region;
    std::string endpoint;
    std::string file_store_dir;
    std::string track_number;
    std::string report_uuid;
    AIGC_JSON_HELPER(credentials, provider, bucket, region, endpoint, file_store_dir, track_number, report_uuid);
};


struct HistoryHmsDataResponsePagination {
    int page_num = 0;
    int page_size = 0;
    int total = 0;
    AIGC_JSON_HELPER(page_num, page_size, total);
};

struct HistoryHmsDataResponseArgs {
    int alarm_info = 0;
    int component_index = 0;
    int64_t create_timestamp = 0;
    std::string device_id;
    int display_scene = 0;
    int ignore = 0;
    std::string sender_host_id;
    int sensor_index = 0;
    AIGC_JSON_HELPER(alarm_info, component_index, create_timestamp, 
                     device_id, display_scene, ignore, sender_host_id, 
                     sensor_index);
};

struct HistoryHmsDataResponseItem {
    std::string _id;
    std::string bid;
    std::string tid;
    int level = 0;
    int module = 0;
    std::string message;
    int status = 0;
    std::string code;
    int64_t device_data_create_time = 0;
    int64_t device_data_update_time = 0;
    int64_t create_time = 0;
    int64_t update_time = 0;
    HistoryHmsDataResponseArgs args;
    std::string device_type;
    std::string domain_type;
    int imminent = 0;
    int in_the_sky = 0;
    std::string joint_key;
    std::string status_key;
    std::string sn;
    // sub_hms_list 可为 null，暂不定义
    AIGC_JSON_HELPER(_id, bid, tid, level, module, message, 
                     status, code, device_data_create_time, 
                     device_data_update_time, create_time, 
                     update_time, args, device_type, domain_type, 
                     imminent, in_the_sky, joint_key, status_key, sn);
};

struct HistoryHmsDataResponse {
    HistoryHmsDataResponsePagination pagination;
    std::vector<HistoryHmsDataResponseItem> list;
    AIGC_JSON_HELPER(pagination, list);
};

/**
 * 后端真实数据示例: "model_info":{"key":"8-135-0","domain":"8","type":"135","sub_type":"0","name":"O4 Ground Station","class":"base_station","develop_code":"ea610"}
 */
struct ModelInfoResponse {
    std::string key;
    std::string domain;
    std::string type;
    std::string sub_type;
    std::string name;
    std::string device_class;
    std::string develop_code;

    AIGC_JSON_HELPER(key, domain, type, sub_type, name, device_class, develop_code);
    // 后端 JSON 字段名是 class，成员名为 device_class
    AIGC_JSON_HELPER_RENAME("key", "domain", "type", "sub_type", "name", "class", "develop_code");
};


struct SubDeviceInfoResponse {
    std::string device_sn;
    std::string device_callsign;
    ModelInfoResponse model_info;
    bool device_online_status = false;
    std::string device_firmware_version;
    int device_firmware_status = 0;
    int device_working_status = 0;
    std::string flysafe_database_version;
    std::vector<std::string> parent_sns;
    AIGC_JSON_HELPER(device_sn, device_callsign, model_info, device_online_status, device_firmware_version,
                     device_firmware_status, device_working_status, flysafe_database_version, 
                     parent_sns);
};

struct DeviceDetailResponse {
    std::string device_sn;
    std::string device_callsign;
    ModelInfoResponse model_info;
    int device_add_time = 0;
    bool device_online_status = false;
    std::string device_firmware_version;
    int device_firmware_status = 0;
    int device_working_status = 0;
    std::string complete_machine_sn;
    std::vector<SubDeviceInfoResponse> sub_devices;
    AIGC_JSON_HELPER(device_sn, device_callsign, model_info, 
                     device_add_time, device_online_status, 
                     device_firmware_version, device_firmware_status, 
                     device_working_status, complete_machine_sn, sub_devices);
};

struct FirmwareReleaseNoteReponse {
    std::string device_model_key;
    std::string lang;
    std::string product_version;
    std::string release_note;
    std::string released_time;
    AIGC_JSON_HELPER(device_model_key, lang, product_version, release_note, released_time);
};

struct FirmwareReleaseNoteListResponse {
    std::vector<FirmwareReleaseNoteReponse> list;
    AIGC_JSON_HELPER(list);
};

struct FirmwareUpgradeResponseDeviceItemResponse {
    int firmware_upgrade_type = 0;
    std::string product_version;
    std::string sn;
    AIGC_JSON_HELPER(firmware_upgrade_type, product_version, sn);
};

struct FirmwareUpgradeResponse {
    std::string bid;
    int device_code = 0;
    std::vector<FirmwareUpgradeResponseDeviceItemResponse> devices;
    std::string status;
    AIGC_JSON_HELPER(bid, device_code, devices, status);
};


struct FirmwareUpgradeProgressStepResponse {
    int current_step = 0;
    int percent = 0;
    AIGC_JSON_HELPER(current_step, percent);
};

struct FirmwareUpgradeProgressOTAResponse {
    FirmwareUpgradeProgressStepResponse progress;
    std::string status;
    AIGC_JSON_HELPER(progress, status);
};

struct FirmwareUpgradeAssociateDroneResponse {
    int firmware_status = 0;
    std::string sn;
    int working_status = 0;
    AIGC_JSON_HELPER(firmware_status, sn, working_status);
};

struct FirmwareUpgradeAssociateRelayResponse {
    int firmware_status = 0;
    std::string sn;
    AIGC_JSON_HELPER(firmware_status, sn);
};

struct FirmwareUpgradeProgressItemResponse {
    FirmwareUpgradeAssociateDroneResponse associate_drone;
    FirmwareUpgradeAssociateRelayResponse associate_relay;
    int device_code = 0;
    int firmware_status = 0;
    FirmwareUpgradeProgressOTAResponse ota;
    std::string sn;
    int working_status = 0;
    AIGC_JSON_HELPER(associate_drone, associate_relay, device_code, firmware_status, ota, sn, working_status);
};

struct FirmwareUpgradeProgressResponse {
    std::vector<FirmwareUpgradeProgressItemResponse> list;
    AIGC_JSON_HELPER(list);
};

struct FirmwareUpgradeStsResponseCredentials {
    std::string access_key_id;
    std::string access_key_secret;
    std::string security_token;
    int64_t expire = 0;
    AIGC_JSON_HELPER(access_key_id, access_key_secret, security_token, expire);
};

struct FirmwareUpgradeStsResponse {
    FirmwareUpgradeStsResponseCredentials credentials;
    std::string provider;
    std::string bucket;
    std::string region;
    std::string endpoint;
    std::string file_store_dir;
    AIGC_JSON_HELPER(credentials, provider, bucket, region, endpoint, file_store_dir);
};

struct FirmwareItemResponse {
    int id = 0;
    std::string created_at;
    std::string device_model_key;
    std::string status;
    std::string version;
    std::string desc;
    AIGC_JSON_HELPER(id, created_at, device_model_key, status, version, desc);
};

struct FirmwareListResponse {
    std::vector<FirmwareItemResponse> list;
    AIGC_JSON_HELPER(list);
};

struct FirmwareCompatibilityRulesResponse {
    std::string min_version;
    std::string max_version;
    AIGC_JSON_HELPER(min_version, max_version);
};

struct FirmwareCompatibilityConfigItemResponse {
    std::string device_model_key;
    std::vector<std::string> compatible_versions;
    FirmwareCompatibilityRulesResponse rules;
    AIGC_JSON_HELPER(device_model_key, compatible_versions, rules);
};

struct FirmwareCompatibilityConfigResponse {
    int code = -1;
    std::string message;
    std::vector<FirmwareCompatibilityConfigItemResponse> data;
    AIGC_JSON_HELPER(code, message, data);
};

struct FirmwareServiceProgressResponse {
    int percent = 0;
    int current_step = 0;
    AIGC_JSON_HELPER(percent, current_step);
};

struct FirmwareServiceDeviceResponse {
    std::string sn;
    int firmware_upgrade_type = 0;
    std::string product_version;
    AIGC_JSON_HELPER(sn, firmware_upgrade_type, product_version);
};

struct FirmwareServiceDataResponse {
    std::vector<FirmwareServiceDeviceResponse> devices;
    AIGC_JSON_HELPER(devices);
};

struct FirmwareServiceResponse {
    std::string bid;
    std::string status;
    int64_t create_time = 0;
    int64_t update_time = 0;
    int device_code = 0;
    FirmwareServiceProgressResponse progress;
    FirmwareServiceDataResponse data;
    AIGC_JSON_HELPER(bid, status, create_time, update_time, device_code, progress, data);
};

struct FirmwareStatusItemResponse {
    std::string sn;
    std::string firmware_version;
    int firmware_status = 0;
    std::string device_model_key;
    FirmwareServiceResponse services;
    AIGC_JSON_HELPER(sn, firmware_version, firmware_status, device_model_key, services);
};

struct FirmwareStatusListResponse {
    std::vector<FirmwareStatusItemResponse> list;
    AIGC_JSON_HELPER(list);
};


struct DeviceUnlockCommonFieldsResponse {
    int id = 0;
    std::string name;
    int type = 0;
    int group_id = 0;
    std::string user_id;
    std::string device_sn;
    int64_t begin_time = 0;
    int64_t end_time = 0;
    bool user_only = false;
    bool enabled = false;
    AIGC_JSON_HELPER(id, name, type, group_id, user_id, device_sn, begin_time, end_time, user_only, enabled);
};

struct DeviceUnlockAreaUnlockResponse {
    std::vector<int> area_ids; // 可为 null
    AIGC_JSON_HELPER(area_ids);
};

struct DeviceUnlockCircleUnlockResponse {
    double radius = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    AIGC_JSON_HELPER(radius, latitude, longitude, height);
};

struct DeviceUnlockCountryUnlockResponse {
    int country_number = 0;
    double height = 0.0;
    AIGC_JSON_HELPER(country_number, height);
};

struct DeviceUnlockHeightUnlockResponse {
    double height = 0.0;
    AIGC_JSON_HELPER(height);
};

struct DeviceUnlockPolygonPointResponse {
    double latitude = 0.0;
    double longitude = 0.0;
    AIGC_JSON_HELPER(latitude, longitude);
};

struct DeviceUnlockPolygonUnlockResponse {
    std::vector<DeviceUnlockPolygonPointResponse> points; // 可为 null
    AIGC_JSON_HELPER(points);
};

struct DeviceUnlockPowerUnlockResponse {
    int level = 0;
    AIGC_JSON_HELPER(level);
};

struct DeviceUnlockRidUnlockResponse {
    int level = 0;
    AIGC_JSON_HELPER(level);
};

struct DeviceUnlockLicenseItemResponse {
    DeviceUnlockCommonFieldsResponse common_fields;
    DeviceUnlockAreaUnlockResponse area_unlock;
    DeviceUnlockCircleUnlockResponse circle_unlock;
    DeviceUnlockCountryUnlockResponse country_unlock;
    DeviceUnlockHeightUnlockResponse height_unlock;
    DeviceUnlockPolygonUnlockResponse polygon_unlock;
    DeviceUnlockPowerUnlockResponse power_unlock;
    DeviceUnlockRidUnlockResponse rid_unlock;
    AIGC_JSON_HELPER(common_fields, area_unlock, circle_unlock, country_unlock, height_unlock, polygon_unlock, power_unlock, rid_unlock);
};

struct QueryDeviceUnlockLicenseListResponse {
    int result = 0;
    int device_model_domain = 0;
    bool consistence = false;
    std::vector<DeviceUnlockLicenseItemResponse> licenses;
    AIGC_JSON_HELPER(result, device_model_domain, consistence, licenses);
};

struct CheckUnlockLicenseResponseItemResponse {
    std::string file_name;
    std::string name;
    std::string type;
    int64_t begin_time = 0;
    int64_t end_time = 0;
    std::string url;
    AIGC_JSON_HELPER(file_name, name, type, begin_time, end_time, url);
};

struct CheckUnlockLicenseResponse {
    std::vector<std::string> err_file_name;
    std::vector<std::string> err_cert_mismatch;
    std::vector<CheckUnlockLicenseResponseItemResponse> list;
    AIGC_JSON_HELPER(err_file_name, err_cert_mismatch, list);
};

struct GetImportUnlockLicenseStsResponseCredentials {
    std::string access_key_id;
    std::string access_key_secret;
    int64_t expire = 0;
    std::string security_token;
    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token);
};

struct GetImportUnlockLicenseStsResponse {
    GetImportUnlockLicenseStsResponseCredentials credentials;
    std::string provider;
    std::string bucket;
    std::string region;
    std::string endpoint;
    std::string file_store_dir;
    AIGC_JSON_HELPER(credentials, provider, bucket, region, endpoint, file_store_dir);
};

struct WaylineListPaginationResponse {
    int page = 0;
    int page_size = 0;
    int total = 0;
    AIGC_JSON_HELPER(page, page_size, total);
};

struct WaylineListItemResponse {
    std::string wayline_uuid;
    std::vector<int> wayline_types;
    std::string wayline_name;
    int size = 0;
    std::vector<std::string> payload_types;
    std::vector<std::string> payload_types_with_index;
    std::string drone_type;
    int64_t updated_at = 0;
    bool with_megaphone = false;
    bool with_search_light = false;
    bool is_ai_wayline = false;
    AIGC_JSON_HELPER(wayline_uuid, wayline_types, wayline_name, size, payload_types, payload_types_with_index, drone_type, updated_at, with_megaphone, with_search_light, is_ai_wayline);
};

struct GetWaylineListResponse {
    WaylineListPaginationResponse pagination;
    std::vector<WaylineListItemResponse> list;
    AIGC_JSON_HELPER(pagination, list);
};

struct WaylineDetailSurveyAreaScopePointResponse {
    double lng = 0;
    double lat = 0;
    double alt = 0;
    AIGC_JSON_HELPER(lng, lat, alt);
};

struct WaylineDetailSurveyAreaScopeResponse {
    std::vector<WaylineDetailSurveyAreaScopePointResponse> points;
    AIGC_JSON_HELPER(points);
};

struct WaylineDetailKmzDataResponse {
    int wayline_type = 0;
    int wayline_point_count = 0;
    int exit_on_rc_lost_type = 0;
    int exit_on_rc_lost_behavior = 0;
    int start_record_action_count = 0;
    int stop_record_action_count = 0;
    int ortho_camera_overlap_h = 0;
    int ortho_camera_overlap_w = 0;
    int inclined_camera_overlap_h = 0;
    int inclined_camera_overlap_w = 0;
    int supplement_prism_model_count = 0;
    int estimated_photo_count = 0;
    double duration = 0;
    double distance = 0;
    double survey_area = 0;
    double start_latitude = 0;
    double start_longitude = 0;
    double start_altitude = 0;
    double margin_distance = 0;
    double ortho_gsd = 0;
    double incline_gsd = 0;
    bool is_wayline_validity_check = false;
    bool has_oriented_action = false;
    bool has_accurate_action = false;
    bool is_surface_fly_on = false;
    bool is_elevation_optimization_on = false;
    bool is_smart_oblique_on = false;
    bool is_mapping_strip_use_template_altitude = false;
    bool is_with_megaphone = false;
    bool is_with_search_light = false;
    bool is_ai_wayline = false;
    std::string drone_type;
    std::string target_detect_area_file_path;
    std::vector<int> wayline_validity_check_codes;
    std::vector<std::string> elevation_file_names;
    std::vector<std::string> payload_types;
    std::vector<std::string> payload_types_with_index;
    std::vector<std::string> accurate_action_uuids;
    std::vector<std::string> action_uuids;
    std::vector<std::string> res_file_md5s;
    std::vector<std::string> audio_file_md5s;
    std::vector<std::string> target_detect_action_uuids;
    std::vector<std::string> ai_audio_file_md5s;
    std::unordered_map<std::string, std::string> action_uuid_to_md5_map;
    std::unordered_map<std::string, std::string> audio_action_uuid_to_file_path_map;
    std::unordered_map<std::string, std::string> ai_audio_action_uuid_to_file_path_map;
    WaylineDetailSurveyAreaScopeResponse survey_area_scope;
    AIGC_JSON_HELPER(wayline_type, wayline_point_count, exit_on_rc_lost_type,
        exit_on_rc_lost_behavior, start_record_action_count, stop_record_action_count,
        ortho_camera_overlap_h, ortho_camera_overlap_w,
        inclined_camera_overlap_h, inclined_camera_overlap_w,
        supplement_prism_model_count, estimated_photo_count,
        duration, distance, survey_area,
        start_latitude, start_longitude, start_altitude,
        margin_distance, ortho_gsd, incline_gsd,
        is_wayline_validity_check, has_oriented_action, has_accurate_action,
        is_surface_fly_on, is_elevation_optimization_on, is_smart_oblique_on,
        is_mapping_strip_use_template_altitude,
        is_with_megaphone, is_with_search_light, is_ai_wayline,
        drone_type, target_detect_area_file_path,
        wayline_validity_check_codes, elevation_file_names,
        payload_types, payload_types_with_index,
        accurate_action_uuids, action_uuids,
        res_file_md5s, audio_file_md5s,
        target_detect_action_uuids, ai_audio_file_md5s,
        action_uuid_to_md5_map, audio_action_uuid_to_file_path_map,
        ai_audio_action_uuid_to_file_path_map, survey_area_scope);
};

struct WaylineDetailExtDataResponse {
    std::string kmz_without_res_object_key;
    std::string kmz_without_res_md5;
    std::string target_area_file_object_key;
    std::string business_file_object_key;
    std::vector<std::string> model_file_object_keys;
    AIGC_JSON_HELPER(kmz_without_res_object_key, kmz_without_res_md5,
        target_area_file_object_key, business_file_object_key,
        model_file_object_keys);
};

struct GetWaylineDetailResponse {
    std::string wayline_uuid;
    std::string md5;
    std::string wayline_name;
    std::string kmz_object_key;
    std::string url;
    std::string suffix;
    int64_t deleted_at = 0;
    int64_t updated_at = 0;
    std::string kmz_without_res_url;
    std::string target_area_file_url;
    std::string business_file_url;
    WaylineDetailKmzDataResponse kmz_data;
    WaylineDetailExtDataResponse ext_data;
    AIGC_JSON_HELPER(wayline_uuid, md5, wayline_name, kmz_object_key, url, suffix, deleted_at, updated_at, kmz_without_res_url, target_area_file_url, business_file_url, kmz_data, ext_data);
};

struct BatchDeleteWaylinesResponse {
    std::string status;
    /// FH2 通用响应中的 code/message（成功时为 0 与 message；失败时为业务码如 217010）
    int server_code = 0;
    std::string server_message;
    AIGC_JSON_HELPER(status);
};

struct UpdateWaylineResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct SubmitWaylineInfoResponse {
    std::string status;
    AIGC_JSON_HELPER(status);
};

struct BatchDownloadWaylinesItemResponse {
    std::string wayline_uuid;
    std::string wayline_name;
    std::string kmz_object_key;
    std::string url;
    int size = 0;
    AIGC_JSON_HELPER(wayline_uuid, wayline_name, kmz_object_key, url, size);
};

struct BatchDownloadWaylinesResponse {
    std::vector<BatchDownloadWaylinesItemResponse> list;
    AIGC_JSON_HELPER(list);
};

struct DuplicateWaylineResponse {
    std::string wayline_uuid;
    std::string wayline_name;
    AIGC_JSON_HELPER(wayline_uuid, wayline_name);
};

struct CopyWaylineResponse {
    std::string wayline_uuid;
    std::string wayline_name;
    AIGC_JSON_HELPER(wayline_uuid, wayline_name);
};

struct ConcatWaylineExtDataModelBoundaryResponse {
    double latitude = 0;
    double longitude = 0;
    double altitude = 0;
    double half_x = 0;
    double half_y = 0;
    double half_z = 0;
    std::vector<double> transform;
    AIGC_JSON_HELPER(latitude, longitude, altitude, half_x, half_y, half_z, transform);
};

struct ConcatWaylineExtDataModelBoundingPointResponse {
    double lng = 0;
    double lat = 0;
    AIGC_JSON_HELPER(lng, lat);
};

struct ConcatWaylineExtDataModelBoundingResponse {
    std::vector<ConcatWaylineExtDataModelBoundingPointResponse> points;
    AIGC_JSON_HELPER(points);
};

struct ConcatWaylineExtDataModelResponse {
    std::string uuid;
    std::string file_key;
    ConcatWaylineExtDataModelBoundaryResponse boundary;
    ConcatWaylineExtDataModelBoundingResponse bounding;
    AIGC_JSON_HELPER(uuid, file_key, boundary, bounding);
};

struct ConcatWaylineExtDataResponse {
    std::string kmz_without_res_object_key;
    std::string kmz_without_res_md5;
    std::string target_area_file_object_key;
    std::string business_file_object_key;
    std::vector<std::string> model_file_object_keys;
    std::vector<std::string> elevation_file_object_keys;
    int64_t content_length = 0;
    std::vector<ConcatWaylineExtDataModelResponse> models;
    AIGC_JSON_HELPER(kmz_without_res_object_key, kmz_without_res_md5, target_area_file_object_key,
                     business_file_object_key, model_file_object_keys, elevation_file_object_keys,
                     content_length, models);
};

struct ConcatWaylineResponse {
    std::string wayline_uuid;
    WaylineDetailKmzDataResponse kmz_data;
    ConcatWaylineExtDataResponse ext_data;
    AIGC_JSON_HELPER(wayline_uuid, kmz_data, ext_data);
};

struct ImportKmzItemResponse {
    std::string kmz_object_key;
    std::string wayline_name;
    std::string async_request_id;
    AIGC_JSON_HELPER(kmz_object_key, wayline_name, async_request_id);
};

struct ImportKmzResponse {
    std::vector<ImportKmzItemResponse> list;
    int total = 0;
    AIGC_JSON_HELPER(list, total);
};

// AI Inside 机载算法管理响应结构体
struct AIInsideLabelResponse {
    int type = 0;
    std::string name;
    AIGC_JSON_HELPER(type, name);
};

struct AIInsideModelParamResponse {
    double conf_threshold = 0;
    int nms_pre = 0;
    double iou_threshold = 0;
    AIGC_JSON_HELPER(conf_threshold, nms_pre, iou_threshold);
};

struct AIInsideModelItemResponse {
    std::string signed_name;
    std::string model_name;
    std::string signed_id;
    AIInsideModelParamResponse model_param;
    long long signed_utc = 0;
    long long expired_utc = 0;
    std::vector<AIInsideLabelResponse> labels;
    std::vector<std::string> sn_list;
    int signed_version = 0;
    std::vector<int> image_source;
    std::vector<int> digital_effect;
    std::vector<int> vl_resolution;
    AIGC_JSON_HELPER(signed_name, model_name, signed_id, model_param,
                     signed_utc, expired_utc, labels, sn_list,
                     signed_version, image_source, digital_effect, vl_resolution);
};

struct GetAIInsideModelsResponse {
    std::vector<AIInsideModelItemResponse> models;
    AIGC_JSON_HELPER(models);
};

struct AIInsideDeviceModelResponse {
    std::string device_model_key;  // JSON key: "key"
    std::string domain;
    std::string name;
    AIGC_JSON_HELPER(device_model_key, domain, name);
    AIGC_JSON_HELPER_RENAME("key", "domain", "name");
};

// 对齐现网 DeviceServiceStatus：status 为 string，progress 为嵌套对象
struct AIInsideOperateProgressDetailResponse {
    int percent = 0;
    int current_step = 0;
    AIGC_JSON_HELPER(percent, current_step);
};

struct AIInsideOperateProgressResponse {
    std::string device_cmd_method;
    std::string status;  // "ok" / "failed" / ...
    AIInsideOperateProgressDetailResponse progress;
    int device_code = 0;
    long long create_time = 0;
    AIGC_JSON_HELPER(device_cmd_method, status, progress, device_code, create_time);
};

struct AIInsideModelInfoResponse {
    std::string signed_id;
    std::string model_name;
    long long signed_utc = 0;
    long long expired_utc = 0;
    std::string status;
    AIGC_JSON_HELPER(signed_id, model_name, signed_utc, expired_utc, status);
};

struct AIInsideAssociateDroneResponse {
    std::string device_sn;
    AIInsideDeviceModelResponse device_model;
    bool device_online_status = false;
    int device_working_status = 0;
    AIInsideModelInfoResponse ai_model_info;
    AIGC_JSON_HELPER(device_sn, device_model, device_online_status,
                     device_working_status, ai_model_info);
};

struct AIInsideDeviceItemResponse {
    std::string device_sn;
    AIInsideDeviceModelResponse device_model;
    std::string complete_machine_sn;
    bool device_online_status = false;
    int device_working_status = 0;
    AIInsideOperateProgressResponse operate_ai_model;
    AIInsideAssociateDroneResponse associate_drone_device_info;
    AIGC_JSON_HELPER(device_sn, device_model, complete_machine_sn,
                     device_online_status, device_working_status,
                     operate_ai_model, associate_drone_device_info);
};

struct AIInsideDevicesPaginationResponse {
    int page = 0;
    int page_size = 0;
    int total = 0;
    AIGC_JSON_HELPER(page, page_size, total);
};

// 现网 GinPagy：data.pagination + data.list（非扁平 page/total）
struct GetAIInsideModelDevicesResponse {
    std::vector<AIInsideDeviceItemResponse> list;
    AIInsideDevicesPaginationResponse pagination;
    AIGC_JSON_HELPER(list, pagination);
};

struct GetAIInsideModelDevicesSummaryResponse {
    int total_devices = 0;
    int operable_devices = 0;
    AIGC_JSON_HELPER(total_devices, operable_devices);
};

struct AIInsideOperateDeviceResultResponse {
    std::string drone_sn;
    int code = 0;
    AIGC_JSON_HELPER(drone_sn, code);
};

struct OperateAIInsideModelResponse {
    std::vector<AIInsideOperateDeviceResultResponse> results;
    AIGC_JSON_HELPER(results);
};

struct AIInsideCredentialsResponse {
    std::string access_key_id;
    std::string access_key_secret;
    long long expire = 0;
    std::string security_token;
    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token);
};

struct CreateAIInsideAlgorithmResponse {
    AIInsideCredentialsResponse credentials;
    std::string provider;
    std::string bucket;
    std::string region;
    std::string endpoint;
    std::string file_store_dir;
    AIGC_JSON_HELPER(credentials, provider, bucket, region, endpoint, file_store_dir);
};

struct AIInsideLicenseResponse {
    std::vector<std::string> sn_list;
    long long started_utc = 0;
    long long expired_utc = 0;
    AIGC_JSON_HELPER(sn_list, started_utc, expired_utc);
};

struct AIInsideAlgorithmDetailResponse {
    int id = 0;
    std::string created_at;
    std::string updated_at;
    std::string signed_id;
    std::string signed_name;
    int signed_version = 0;
    long long signed_utc = 0;
    std::string model_uuid;
    std::string model_name;
    std::string model_type;
    std::vector<AIInsideLabelResponse> labels;
    AIInsideModelParamResponse model_param;
    AIInsideLicenseResponse license;
    std::vector<int> applicable_product;
    std::string file_key;
    std::vector<int> image_source;
    std::vector<int> digital_effect;
    std::vector<int> vl_resolution;
    AIGC_JSON_HELPER(id, created_at, updated_at, signed_id, signed_name,
                     signed_version, signed_utc, model_uuid, model_name,
                     model_type, labels, model_param, license,
                     applicable_product, file_key, image_source,
                     digital_effect, vl_resolution);
};

struct FileUploadAIInsideAlgorithmResponse {
    AIInsideAlgorithmDetailResponse detail;
    AIGC_JSON_HELPER(detail);
};

struct RecommendedDeviceResponse {
    std::string drone_sn;
    std::string gateway_sn;
    double distance = 0;
    bool matched = false;
    AIGC_JSON_HELPER(drone_sn, gateway_sn, distance, matched);
};

struct DeviceRecommendationResponse {
    std::vector<RecommendedDeviceResponse> devices;
    AIGC_JSON_HELPER(devices);
};

struct RoamingStationSnResultResponse {
    std::string station_sn;
    bool success = false;
    int fail_reason = 0;
    AIGC_JSON_HELPER(station_sn, success, fail_reason);
};

struct SetRoamingStationSnResponse {
    std::vector<RoamingStationSnResultResponse> results;
    AIGC_JSON_HELPER(results);
};

struct InFlightTaskTopoResponseItem {
    std::string drone_sn;
    std::string flight_id;
    std::string takeoff_sn;
    AIGC_JSON_HELPER(drone_sn, flight_id, takeoff_sn);
};

struct GetInFlightTaskToposResponse {
    std::vector<InFlightTaskTopoResponseItem> list;
    AIGC_JSON_HELPER(list);
};

/// DeviceShadowSyncField 设备影子同步字段状态
struct DeviceShadowSyncField {
    std::string field;      // 字段名，如 "rth_altitude", "out_of_control_action"
    bool synced = false;    // 是否已同步
    
    AIGC_JSON_HELPER(field, synced);
};

/// DeviceShadowSyncResult 设备影子同步结果
struct DeviceShadowSyncResult {
    std::string status;                             // 同步状态，如 "all_synced", "partial_synced", "failed"
    std::vector<DeviceShadowSyncField> results;     // 各字段同步结果
    
    AIGC_JSON_HELPER(status, results);
};

struct SmartPlanSetting {
    int smart_planning_mode = 0;                             // 智能规划模式，0:开启 1:关闭
    
    AIGC_JSON_HELPER(smart_planning_mode);
}; 


/// FlightSmartPlanModeResponse 飞行安全配置智能规划模式
struct FlightSmartPlanModeResponse {
    SmartPlanSetting settings;                    // 智能规划模式
    DeviceShadowSyncResult device_shadow_sync;      // 设备影子同步结果
    
    AIGC_JSON_HELPER(settings, device_shadow_sync);
};

/// FlightSmartPlanModeApiResponse 飞行安全配置智能规划模式API响应
struct FlightSmartPlanModeApiResponse {
    int code = -1;                                  // 响应码，0表示成功
    std::string message;                            // 响应消息
    FlightSmartPlanModeResponse data;                  // 响应数据
    
    AIGC_JSON_HELPER(code, message, data);
};

// ---- 失控动作 ----
struct OutOfControlActionSetting {
    std::string out_of_control_action;   // 失控动作: "Hover" / "ReturnHome" / "Continue"
    AIGC_JSON_HELPER(out_of_control_action);
};

struct FlightOutOfControlActionResponse {
    OutOfControlActionSetting settings;
    DeviceShadowSyncResult device_shadow_sync;
    AIGC_JSON_HELPER(settings, device_shadow_sync);
};

struct FlightOutOfControlActionApiResponse {
    int code = -1;
    std::string message;
    FlightOutOfControlActionResponse data;
    AIGC_JSON_HELPER(code, message, data);
};

// ---- 飞行作业高度 ----
struct TaskHeightSettings {
    int commander_flight_height = 0;   // 飞行作业高度（米）
    AIGC_JSON_HELPER(commander_flight_height);
};

struct FlightTaskHeightResponse {
    TaskHeightSettings settings;
    DeviceShadowSyncResult device_shadow_sync;
    AIGC_JSON_HELPER(settings, device_shadow_sync);
};

struct FlightTaskHeightApiResponse {
    int code = -1;
    std::string message;
    FlightTaskHeightResponse data;
    AIGC_JSON_HELPER(code, message, data);
};

// ---- 返航高度 ----
struct RthAltitudeSetting {
    int rth_altitude = 0;   // 返航高度（米）
    AIGC_JSON_HELPER(rth_altitude);
};

struct FlightRthAltitudeResponse {
    RthAltitudeSetting settings;
    DeviceShadowSyncResult device_shadow_sync;
    AIGC_JSON_HELPER(settings, device_shadow_sync);
};

struct FlightRthAltitudeApiResponse {
    int code = -1;
    std::string message;
    FlightRthAltitudeResponse data;
    AIGC_JSON_HELPER(code, message, data);
};

// ---- 空中回传设置 ----
// 接口已不再返回 data 字段（data 为 null），仅使用外层 code/message 判断结果


// 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object);
struct RepairGuidanceInfo {
    std::string redirect_url;  // 维修指导跳转链接，型号不支持时指向官网维修页;
    std::string description;   // 描述码：devices_repair_guidance_prefix(型号不支持), devices_repair_detail_repair_order_unfinished(有未完成订单);

    AIGC_JSON_HELPER(redirect_url, description);
};


// 获取设备报修配置;
struct DeviceRepairConfigInfo {
    std::string device_sn;                  // 设备 SN;
    bool support_repair = true;             // 是否支持在线报修;
    std::vector<int> allowed_source_types;  // 允许的报修来源类型：0=定损 2=日志反馈 3=直接报修;
    RepairGuidanceInfo repair_guidance;     // 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object);

    AIGC_JSON_HELPER(device_sn, support_repair, allowed_source_types, repair_guidance);
};


struct ObtainRepairConfigResponse {
    std::vector<DeviceRepairConfigInfo> list;

    // register class-members;
    AIGC_JSON_HELPER(list);
};


// 创建报修订单 - 生成的订单信息;
struct GeneratedOrderInfo {
    std::string order_number;           // 订单编号;
    int status = 0;                     // 订单状态;
    std::string ruiyun_order_number;    // 瑞云订单编号;

    // register class-members;
    AIGC_JSON_HELPER(order_number, status, ruiyun_order_number);
};

// 创建报修订单响应;
struct CreateMaintenanceOrdersResponse {
    bool success = false;
    std::vector<GeneratedOrderInfo> generated_orders;

    // register class-members;
    AIGC_JSON_HELPER(success, generated_orders);
};


// 查询单个报修订单详情 - 联系地址;
struct MaintenanceOrderContactAddressInfo {
    std::string country;
    std::string province;
    std::string city;
    std::string district;
    std::string detail;

    // register class-members;
    AIGC_JSON_HELPER(country, province, city, district, detail);
};

// 查询单个报修订单详情 - 状态变更历史;
struct MaintenanceOrderStatusHistoryItem {
    int from_status = 0;
    int to_status = 0;
    std::string change_time;
    std::string operator_id;
    std::string operator_name;
    std::string remark;

    // register class-members;
    AIGC_JSON_HELPER(from_status, to_status, change_time, operator_id, operator_name, remark);
    AIGC_JSON_HELPER_RENAME("from_status", "to_status", "change_time", "operator", "operator_name", "remark");
};


// 查询单个报修订单详情 -- attachments;
struct MaintenanceOrderDetailAttachmentsInfo {
    int64_t id = 0;
    std::string file_name;
    std::string object_key;  // OSS 存储路径;
    int64_t file_size = 0;   // 文件大小(字节);
    std::string upload_time;
    std::string created_at;

    // register class-members;
    AIGC_JSON_HELPER(id, file_name, object_key, file_size, upload_time, created_at);
};

struct MaintenanceOrderRepairInfoData {
    std::string assignee;
    std::string assignee_phone;
    std::string repair_start_time;

    // register class-members;
    AIGC_JSON_HELPER(assignee, assignee_phone, repair_start_time);
};

struct MaintenanceOrderLogReportData {
    std::string report_uuid;
    std::string track_number;
    std::string status;

    // register class-members;
    AIGC_JSON_HELPER(report_uuid, track_number, status);
};


// 查询单个报修订单详情响应;
struct MaintenanceOrderDetailResponse {
    std::string order_number;
    std::string organization_uuid;
    std::string creator_id;
    std::string creator_name;
    int source_type = 0;
    std::string source_type_text;
    int status = 0;
    std::string status_text;
    std::string fault_description;
    std::string fault_occurrence_time;
    std::string expected_service_time;
    std::string contact_person;
    std::string contact_phone;
    std::string contact_email;
    MaintenanceOrderContactAddressInfo contact_address_full;
    std::string created_at;
    std::string updated_at;
    std::string submission_time;
    std::string accepted_time;
    std::string completed_time;
    std::string ruiyun_order_number;
    std::string ruiyun_sync_status;
    std::string device_sn;
    std::string device_model_key;
    std::string module;
    std::string nameplate_sn;
    std::string diagnostic_id;
    std::string device_report_id;
    std::vector<MaintenanceOrderDetailAttachmentsInfo> attachments;
    std::vector<MaintenanceOrderStatusHistoryItem> status_history;
    MaintenanceOrderRepairInfoData repair_info;
    MaintenanceOrderLogReportData log_report;

    // register class-members;
    AIGC_JSON_HELPER(
        order_number,
        organization_uuid,
        creator_id,
        creator_name,
        source_type,
        source_type_text,
        status,
        status_text,
        fault_description,
        fault_occurrence_time,
        expected_service_time,
        contact_person,
        contact_phone,
        contact_email,
        contact_address_full,
        created_at,
        updated_at,
        submission_time,
        accepted_time,
        completed_time,
        ruiyun_order_number,
        ruiyun_sync_status,
        device_sn,
        device_model_key,
        module, nameplate_sn,
        diagnostic_id,
        device_report_id,
        attachments,
        status_history,
        repair_info,
        log_report
    );
};


struct MaintenanceOrdersListItemInfo {
    std::string order_number;
    std::string ruiyun_order_number;
    int status = 0;
    std::string status_text;
    int source_type = 0;
    std::string source_type_text;
    std::string creator_id;
    std::string creator_name;
    std::string contact_person;
    std::string contact_phone;
    std::string contact_email;
    MaintenanceOrderContactAddressInfo contact_address_full;
    std::string fault_description;
    std::string fault_occurrence_time;
    std::string expected_service_time;
    std::string created_at;
    std::string updated_at;
    std::string submission_time;
    std::string accepted_time;
    std::string completed_time;
    std::string device_sn;
    std::string device_model_key;
    std::string device_model_name;
    std::string nameplate_sn;
    std::string module;
    std::string device_organization_callsign;
    std::vector<MaintenanceOrderDetailAttachmentsInfo> attachments;
    std::vector<MaintenanceOrderStatusHistoryItem> status_history;
    MaintenanceOrderRepairInfoData repair_info;
    MaintenanceOrderLogReportData log_report;

    // register class-members;
    AIGC_JSON_HELPER(
        order_number,
        ruiyun_order_number,
        status,
        status_text,
        source_type,
        source_type_text,
        creator_id,
        creator_name,
        contact_person,
        contact_phone,
        contact_email,
        contact_address_full,
        fault_description,
        fault_occurrence_time,
        expected_service_time,
        created_at,
        updated_at,
        submission_time,
        accepted_time,
        completed_time,
        device_sn,
        device_model_key,
        device_model_name,
        nameplate_sn,
        module,
        device_organization_callsign,
        attachments,
        status_history,
        repair_info,
        log_report
    );
};


struct MaintenanceOrdersListResponse {
    CommonPaginationInfo pagination;
    std::vector<MaintenanceOrdersListItemInfo> list;

    // register class-members;
    AIGC_JSON_HELPER(pagination, list);
};


// === Terra Build 相关响应结构 ===

/// FolderInfo — 文件夹信息
struct FolderInfo {
    int folder_id = 0;
    std::string folder_uuid;
    std::string folder_name;
    AIGC_JSON_HELPER(folder_id, folder_uuid, folder_name);
};

struct PolygonPointItem {
    double lat = 0.0;   // 纬度
    double lng = 0.0;   // 经度
    AIGC_JSON_HELPER(lat, lng);
};

/// PredefineArea — 兴趣区域（多边形）
struct PredefineAreaList {
    std::vector<PolygonPointItem> polygon_points;   // 多边形坐标点列表
    AIGC_JSON_HELPER(polygon_points);
};

/// ReconstructionSettingsDetail — 重建参数（按需定义，未定义的字段自动忽略）
struct ReconstructionSettingsDetail {
    std::string name;
    std::vector<int> reconstruction_type;
    double simplified_factor = 0.0;
    int reconstruction_mode = 0;
    PredefineAreaList predefine_area;           // 兴趣区域（多边形坐标点）
    std::vector<int> generate_model_formats;
    std::string wkt;
    int quality_level = 0;
    int quality_level_3dgs = 0;
    AIGC_JSON_HELPER(name, reconstruction_type, simplified_factor,
                     reconstruction_mode, predefine_area, generate_model_formats,
                     wkt, quality_level, quality_level_3dgs);
};

/// ReconstructionSettingsResponseData — 获取重建设置响应
struct ReconstructionSettingsResponseData {
    int state = 0;
    ReconstructionSettingsDetail reconstruction_settings;
    std::string media_input_object_key;
    std::string sfm_output_object_prefix;
    std::vector<FolderInfo> folder_list;
    AIGC_JSON_HELPER(state, reconstruction_settings, media_input_object_key,
                     sfm_output_object_prefix, folder_list);
};

/// QualityReportResponseData — 获取质量报告响应
struct QualityReportResponseData {
    std::string quality_report_url;
    AIGC_JSON_HELPER(quality_report_url);
};

/// ModelFileItem — 模型文件列表中的单个项
struct ModelFileItem {
    int id = 0;
    std::string uuid;
    std::string name;
    int file_type = 0;                      // 文件大类型
    int sub_file_type = 0;                  // 文件子类型
    int state = 0;
    std::vector<int> reconstruction_type;   // 重建类型（数组）
    std::string thumbnail_url;
    std::string zip_url;                    // 打包下载 URL
    std::string created_at;
    std::string updated_at;
    AIGC_JSON_HELPER(id, uuid, name, file_type, sub_file_type, state,
                     reconstruction_type, thumbnail_url, zip_url,
                     created_at, updated_at);
};

/// ModelFileListPaginationInfo — 分页元信息
struct ModelFileListPaginationInfo {
    int page = 0;        // 当前页码
    int page_size = 0;   // 每页数量
    long long total = 0; // 总记录数
    AIGC_JSON_HELPER(page, page_size, total);
};

/// ModelFileListAncestorInfo — 面包屑导航单元
struct ModelFileListAncestorInfo {
    std::string name;
    int id = 0;
    int file_type = 0;
    int sub_file_type = 0;
    AIGC_JSON_HELPER(name, id, file_type, sub_file_type);
};

/// ModelFileListResponseData — 获取模型文件列表响应
struct ModelFileListResponseData {
    std::vector<ModelFileItem> list;
    ModelFileListPaginationInfo pagination;
    std::vector<ModelFileListAncestorInfo> ancestors;
    AIGC_JSON_HELPER(list, pagination, ancestors);
};

/// ModelFileUrlResponseData — 获取模型文件签名 URL 响应
struct ModelFileUrlResponseData {
    std::string url;
    AIGC_JSON_HELPER(url);
};

/// CreateReconstructionResponseData — 创建重建任务响应
struct CreateReconstructionResponseData {
    int id = 0;             // 任务 ID
    std::string uuid;       // 任务 UUID
    std::string job_uuid;   // 实例 UUID
    AIGC_JSON_HELPER(id, uuid, job_uuid);
};

/// SfmResultResponseData — 获取空三结果响应
struct SfmResultResponseData {
    std::string sfm_json_url;                              // 空三计算结果 JSON 文件签名 URL
    std::string sfm_report_json_url;                       // 空三质量报告 JSON 文件签名 URL（暂不支持）
    std::string gcp_out_json_url;                          // 控制点输出 JSON 文件签名 URL（暂不支持）
    std::vector<std::string> re_projection_error_pic_url_list; // 重投影误差图片签名 URL 列表
    AIGC_JSON_HELPER(sfm_json_url, sfm_report_json_url, gcp_out_json_url, re_projection_error_pic_url_list);
};

/// CreatePanoramaResponseData — 创建全景拼接任务响应
struct CreatePanoramaResponseData {
    int id = 0;             // 任务 ID
    std::string uuid;       // 任务 UUID
    std::string job_uuid;   // 实例 UUID
    AIGC_JSON_HELPER(id, uuid, job_uuid);
};

/// ReconstructionImageItem — 重建图像列表中的单个项
struct ReconstructionImageItem {
    std::string image_id;              // 图像 ID（文件名去掉扩展名）
    std::string file_name;             // 原始文件名
    std::string thumbnail_image_url;   // 缩略图签名 URL
    std::string origin_image_url;      // 原图签名 URL
    AIGC_JSON_HELPER(image_id, file_name, thumbnail_image_url, origin_image_url);
};

/// ReconstructionImagesResponseData — 获取重建图像列表响应
struct ReconstructionImagesResponseData {
    std::vector<ReconstructionImageItem> list;
    int total = 0;
    AIGC_JSON_HELPER(list, total);
};
// === Terra 模型管理新增响应结构 ===

/// TerraUploadCallbackResponseData — 模型上传回调响应
struct TerraUploadCallbackResponseData {
    long long id = 0;       // 新创建的 File 记录 ID
    std::string uuid;       // 新创建的 File 记录 UUID
    std::string name;       // 文件名（原样返回）
    AIGC_JSON_HELPER(id, uuid, name);
};

/// TerraShowOnMapResponseData — 批量加载/取消加载响应
struct TerraShowOnMapResponseData {
    std::vector<int> ids;   // 实际被操作的文件 ID 列表
    AIGC_JSON_HELPER(ids);
};

// ── GetModelDetail 内部解析结构体 ──────────────────────────────────────

struct TerraDetailBoundaryData {
    double max_lat = 0.0;    double min_lat = 0.0;
    double max_lng = 0.0;    double min_lng = 0.0;
    double max_alt = 0.0;    double min_alt = 0.0;
    double latitude = 0.0;   double longitude = 0.0;
    double altitude = 0.0;
    double half_x = 0.0;     double half_y = 0.0;
    double half_z = 0.0;
    std::vector<double> transform;
    int max_level = 0;       int min_level = 0;
    double start_lat = 0.0;  double end_lat = 0.0;
    double start_lng = 0.0;  double end_lng = 0.0;
    AIGC_JSON_HELPER(max_lat, min_lat, max_lng, min_lng, max_alt, min_alt,
                     latitude, longitude, altitude, half_x, half_y, half_z,
                     transform, max_level, min_level,
                     start_lat, end_lat, start_lng, end_lng)
};

struct TerraDetailIntensityData {
    double max_intensity = 0.0;
    double min_intensity = 0.0;
    AIGC_JSON_HELPER(max_intensity, min_intensity)
};

struct TerraDetailGeo2DPointData {
    double lng = 0.0;
    double lat = 0.0;
    AIGC_JSON_HELPER(lng, lat)
};

struct TerraDetailGeoPolygonData {
    std::vector<TerraDetailGeo2DPointData> points;
    AIGC_JSON_HELPER(points)
};

struct TerraDetailPositionData {
    double lng = 0.0;
    double lat = 0.0;
    double abs_alt = 0.0;
    double rel_alt = 0.0;
    AIGC_JSON_HELPER(lng, lat, abs_alt, rel_alt)
};

struct TerraDetailExtInfoData {
    std::string device;
    double duration = 0.0;
    std::string photographer;
    std::string resolution;
    int height = 0;
    int width = 0;
    double zoom_factor = 0.0;
    std::string task_name;
    TerraDetailPositionData shoot_position;
    TerraDetailPositionData photo_position;
    std::string line_name;
    bool is_original = false;
    double gimbal_pitch = 0.0;
    double gimbal_yaw = 0.0;
    double gimbal_yaw_degree = 0.0;
    double gimbal_roll = 0.0;
    double focal_length = 0.0;
    std::string payload_model_key;
    std::string drone_model_key;
    std::string create_time;
    std::string wayline_action_uuid;
    std::string wayline_uuid;
    std::string origin_wayline_uuid;
    double digital_zoom_ratio = 0.0;
    double length_in35mm_film = 0.0;
    AIGC_JSON_HELPER(device, duration, photographer, resolution, height, width,
                     zoom_factor, task_name, shoot_position, photo_position,
                     line_name, is_original, gimbal_pitch, gimbal_yaw,
                     gimbal_yaw_degree, gimbal_roll, focal_length,
                     payload_model_key, drone_model_key, create_time,
                     wayline_action_uuid, wayline_uuid, origin_wayline_uuid,
                     digital_zoom_ratio, length_in35mm_film)
};

struct TerraModelDetailResponseData {
    long long id = 0;
    std::string uuid;
    std::string name;
    int file_type = 0;
    int sub_file_type = 0;
    long long size = 0;
    std::string suffix;
    int mod = 0;
    bool show_on_map = false;
    int state = 0;
    std::string selected_wkt;
    long long parent_id = 0;
    long long update_at = 0;
    long long create_at = 0;
    std::string url;
    std::string webp_url;
    std::string preview_url;
    std::string thumbnail_url;
    std::string object_key;
    std::string index_file_key;
    std::string file_prefix;
    std::string webp_file_prefix;
    TerraDetailBoundaryData boundary;
    TerraDetailIntensityData intensity_info;
    double max_return_number = 0.0;
    TerraDetailGeoPolygonData bounding;
    TerraDetailExtInfoData ext;
    std::vector<std::string> file_tags;
    std::string user_name;
    AIGC_JSON_HELPER(id, uuid, name, file_type, sub_file_type, size, suffix,
                     mod, show_on_map, state, selected_wkt, parent_id,
                     update_at, create_at, url, webp_url, preview_url,
                     thumbnail_url, object_key, index_file_key, file_prefix,
                     webp_file_prefix, boundary, intensity_info,
                     max_return_number, bounding, ext, file_tags, user_name)
};

// === 元素管理（ElementService）内部解析结构体 ===

/// ElementGeoPointResp — 响应中的地理坐标点
struct ElementGeoPointResp {
    double lng = 0.0;
    double lat = 0.0;
    double alt = 0.0;
    AIGC_JSON_HELPER(lng, lat, alt)
};

/// ElementPropertiesResp — 响应中的要素渲染属性
struct ElementPropertiesResp {
    std::string color;
    bool clamp_to_ground = false;
    bool is3d = false;
    bool is_transparent = false;
    int font_size = 0;
    int icon = 0;
    AIGC_JSON_HELPER(color, clamp_to_ground, is3d, is_transparent, font_size, icon)
};

/// ElementGeometryResp — 响应中的要素几何数据
struct ElementGeometryResp {
    std::string type;
    double radius = 0.0;
    std::string coordinates;     // raw JSON，多态形状（Point/LineString/Polygon）
    std::string bread_crumbs;    // raw JSON
    AIGC_JSON_HELPER(type, radius, coordinates, bread_crumbs)
};

/// ElementMediaImageResp — 响应中的要素媒体图片
struct ElementMediaImageResp {
    std::string object_key;
    int media_file_id = 0;
    std::string media_file_uuid;
    AIGC_JSON_HELPER(object_key, media_file_id, media_file_uuid)
};

/// ElementMediaResourcesResp — 响应中的要素媒体资源
struct ElementMediaResourcesResp {
    std::vector<ElementMediaImageResp> images;
    AIGC_JSON_HELPER(images)
};

/// ElementResourceContentResp — 响应中的要素几何内容
struct ElementResourceContentResp {
    std::string type;
    ElementPropertiesResp properties;
    ElementGeometryResp geometry;
    ElementMediaResourcesResp media_resources;
    AIGC_JSON_HELPER(type, properties, geometry, media_resources)
};

/// ElementResourceDataResp — 响应中的要素资源体
struct ElementResourceDataResp {
    ElementResourceContentResp content;
    int type = 0;
    std::string user_name;
    std::string user_id;
    std::string remark;
    AIGC_JSON_HELPER(content, type, user_name, user_id, remark)
};

struct ElementCreateResponseData {
    std::string id;  // 新建要素 UUID
    AIGC_JSON_HELPER(id);
};

struct ElementUpdateResponseData {
    std::string id;  // 被更新的要素 UUID
    AIGC_JSON_HELPER(id);
};

/// ElementItemData — 要素节点内部解析结构体
struct ElementItemData {
    std::string id;
    std::string name;
    std::string desc;
    int source = 0;
    int order = 0;
    int status = 0;
    int display = 0;
    std::string resource;           // resource JSON 原样透传，impl 层按需解析
    long long update_time = 0;
    long long create_time = 0;
    int elevation_load_status = 0;
    bool notify = false;
    AIGC_JSON_HELPER(id, name, desc, source, order, status, display,
                     resource, update_time, create_time,
                     elevation_load_status, notify)
};

/// ElementGroupOutputData — 图层节点内部解析结构体（支持递归）
struct ElementGroupOutputData {
    std::string id;
    std::string name;
    int order = 0;
    bool is_distributed = false;
    int type = 0;
    bool is_lock = false;
    std::string tri_state;
    long long create_time = 0;
    std::string pid;                                    // 父图层 UUID，json key 为 "pid"
    std::vector<ElementItemData> elements;
    std::vector<ElementGroupOutputData> children;       // 子图层递归
    AIGC_JSON_HELPER(id, name, order, is_distributed, type, is_lock,
                     tri_state, create_time, pid, elements, children)
};

/// ElementListHierarchyResponseData — 获取图层树形列表响应
struct ElementListHierarchyResponseData {
    std::vector<ElementGroupOutputData> data;
    AIGC_JSON_HELPER(data)
};

struct ElementSetVisibilityResponseData {
    std::string id;  // 被操作的要素 UUID
    AIGC_JSON_HELPER(id);
};

struct ElementDeleteResponseData {
    std::string id;  // 被删除的要素 UUID
    AIGC_JSON_HELPER(id);
};

// === 媒体上传相关响应结构 ===

// === 航线 Editor STS 响应结构体 ===

/// WaylineEditorStsCredentialsData — 航线 Editor STS 临时凭证
struct WaylineEditorStsCredentialsData {
    std::string access_key_id;     // 临时 AccessKey ID
    std::string access_key_secret; // 临时 AccessKey Secret
    int expire = 0;                // 有效期（秒）
    std::string security_token;    // STS SecurityToken
    int platform = 0;              // 存储平台标识
    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token, platform)
};

/// GetWaylineEditorStsResponseData — 获取航线 Editor STS 凭证响应
struct GetWaylineEditorStsResponseData {
    WaylineEditorStsCredentialsData credentials; // STS 临时凭证
    std::string object_key_prefix;  // 对象存储路径前缀
    std::string bucket;             // 存储桶名称
    std::string endpoint;           // 存储服务端点
    std::string provider;           // 存储提供商（minio/aws/ali 等）
    std::string region;             // 存储区域
    AIGC_JSON_HELPER(credentials, object_key_prefix, bucket, endpoint, provider, region)
};

// === 图片备注相关响应结构体 ===

/// UploadGeoMediaFileResponseData — 上传地理标注媒体资源响应
struct UploadGeoMediaFileResponseData {
    int file_id = 0;        // 文件 ID（files 表主键）
    std::string file_uuid;  // 文件 UUID
    int media_file_id = 0;  // 媒体文件 ID（media_files 表主键）
    AIGC_JSON_HELPER(file_id, file_uuid, media_file_id)
};

/// MediaFileGeoPositionData — 媒体文件地理位置
struct MediaFileGeoPositionData {
    double lat = 0.0;     // 纬度
    double lng = 0.0;     // 经度
    double abs_alt = 0.0; // 绝对高度（米）
    AIGC_JSON_HELPER(lat, lng, abs_alt)
};

/// MediaFileDetailExtData — 媒体文件扩展信息
struct MediaFileDetailExtData {
    int width = 0;                            // 图片宽度（像素）
    int height = 0;                           // 图片高度（像素）
    MediaFileGeoPositionData photo_position;  // 照片 GPS 位置
    AIGC_JSON_HELPER(width, height, photo_position)
};

/// GetMediaFileDetailByIdResponseData — 根据 ID 获取媒体文件详情响应
struct GetMediaFileDetailByIdResponseData {
    std::string url;             // 原始文件下载 URL
    std::string thumbnail_url;   // 缩略图 URL
    std::string alert_image_url; // 报警图片 URL
    std::string name;            // 文件名
    std::string suffix;          // 文件后缀
    long long size = 0;          // 文件大小（字节）
    std::string create_at;       // 创建时间（ISO 字符串）
    MediaFileDetailExtData ext;  // 扩展信息
    AIGC_JSON_HELPER(url, thumbnail_url, alert_image_url, name, suffix, size, create_at, ext)
};

/// StsCredentials — STS 临时凭证
struct StsCredentials {
    std::string access_key_id;      // STS 临时 AccessKeyId
    std::string access_key_secret;  // STS 临时 AccessKeySecret
    int expire = 0;                 // 凭证有效期（秒）
    std::string security_token;     // STS SecurityToken
    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token);
};

/// GetMediaUploadStsResponseData — 获取媒体上传 STS 凭证响应
struct GetMediaUploadStsResponseData {
    std::string bucket;              // OSS 存储桶名称
    StsCredentials credentials;      // STS 临时凭证
    std::string object_key_prefix;   // 上传路径前缀
    std::string endpoint;            // OSS 访问端点
    std::string provider;            // 存储提供商（ali/aws/minio 等）
    std::string region;              // 存储区域
    AIGC_JSON_HELPER(bucket, credentials, object_key_prefix, endpoint, provider, region);
};

/// MediaUploadCallbackResponseData — 媒体上传完成回调响应
struct MediaUploadCallbackResponseData {
    std::string object_key;     // 文件 OSS Key
    std::string filename;       // 文件名
    std::string flight_id;      // 航线任务 ID
    std::string uuid;           // 文件 UUID（files 表）
    int id = 0;                 // 文件 ID（files 表主键）
    int media_id = 0;           // 媒体文件 ID（media_files 表主键）
    AIGC_JSON_HELPER(object_key, filename, flight_id, uuid, id, media_id);
};

// ============================================================
// 中继站调试接口响应结构
// ============================================================

// 注意：设置中继广播隐藏、SSID、密码、图传搜索模式 这 4 个接口
// 响应体无 data 字段，直接回调 FlightHubError，无需定义 Response 结构体

/// 设置中继占用状态响应（有 bid+status）
struct SetRelayOccupiedStatusResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

/// 中继重启响应（有 bid+status）
struct RelayRebootResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

/// 中继断开连接响应（有 bid+status）
struct RelayDisconnectResponse {
    std::string bid;
    std::string status;
    AIGC_JSON_HELPER(bid, status);
};

// 定损报告: 错误码信息;
struct DiagnosisErrorCodeItem {
    std::string error_code;
    std::string error_code_describe;
    std::string error_code_describe_en;
    int level = 0;

    // register class-members;
    AIGC_JSON_HELPER(error_code, error_code_describe, error_code_describe_en, level);
};

// 定损报告: 错误码分级列表;
struct DiagnosisErrorCodeList {
    std::vector<DiagnosisErrorCodeItem> notice;
    std::vector<DiagnosisErrorCodeItem> warning;
    std::vector<DiagnosisErrorCodeItem> alert;

    // register class-members;
    AIGC_JSON_HELPER(notice, warning, alert);
};

// 定损报告: 物料诊断结果;
struct DiagnosisMaterialItem {
    DiagnosisErrorCodeList error_code_list;
    std::string material_num;
    std::string material_name;
    std::string material_probability;
    int hms_total = 0;

    // register class-members;
    AIGC_JSON_HELPER(error_code_list, material_num, material_name, material_probability, hms_total);
};

// 定损报告: 自检诊断结果;
struct SelfDiagnosticResult {
    std::vector<DiagnosisMaterialItem> high;
    std::vector<DiagnosisMaterialItem> mid;
    std::vector<DiagnosisMaterialItem> low;

    // register class-members;
    AIGC_JSON_HELPER(high, mid, low);
};

// 定损报告: 设备上报进度 - 子设备;
struct DiagnosisDeviceProgressItem {
    std::string sn;
    std::string status;
    int percent = 0;

    // register class-members;
    AIGC_JSON_HELPER(sn, status, percent);
};

// 定损报告: 设备上报进度;
struct DiagnosisDeviceReportProgress {
    int result = 0;
    std::string sn;
    std::string status;
    int percent = 0;
    std::vector<DiagnosisDeviceProgressItem> all_device;

    // register class-members;
    AIGC_JSON_HELPER(result, sn, status, percent, all_device);
};

// 定损报告: 查询单条定损报告详情 响应;
struct GetSelfDiagnosisReportResponse {
    std::string report_uuid;
    std::string report_user_id;
    std::string report_user_organization_callsign;
    int64_t create_time = 0;
    std::string complete_machine_sn;
    std::string device_sn;
    std::string device_model_key;
    std::string device_organization_callsign;
    std::string status;
    int result_code = 0;
    std::string bid;
    int part = 0;
    int origin = 0;
    SelfDiagnosticResult self_diagnostic_result;
    DiagnosisDeviceReportProgress device_report_progress;
    std::string qr_code_content;
    std::string qr_guide_text;

    // register class-members;
    AIGC_JSON_HELPER(
        report_uuid,
        report_user_id,
        report_user_organization_callsign,
        create_time,
        complete_machine_sn,
        device_sn,
        device_model_key,
        device_organization_callsign,
        status,
        result_code,
        bid,
        part,
        origin,
        self_diagnostic_result,
        device_report_progress,
        qr_code_content,
        qr_guide_text
    );
};


struct GetSelfDiagnosisReportListResponse {
    CommonPaginationInfo pagination;
    std::vector<GetSelfDiagnosisReportResponse> list;

    // register class-members;
    AIGC_JSON_HELPER(pagination, list);
};

struct AIBoxInfo {
    int index = 0;
    int type = 0;
    std::string sn;
    std::string firmware_version;
    double storage_total = 0.0;
    double storage_used = 0.0;

    // register class-members;
    AIGC_JSON_HELPER(index, type, sn, firmware_version, storage_total, storage_used);
};

struct AIBoxInfoListResponse {
    std::vector<AIBoxInfo> ai_boxes;

    // register class-members;
    AIGC_JSON_HELPER(ai_boxes);
};

struct FetchFileDownloadUrlRequest {
    std::string device_sn;
    std::string box_sn;
    std::vector<std::string> object_keys;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, object_keys);
};

struct FileDownloadUrlListResponse {
    std::vector<std::string> list;

    // register class-members;
    AIGC_JSON_HELPER(list);
};

struct FetchAppListRequest {
    std::string device_sn;
    std::string box_sn;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn);
};

struct FetchAppListResponse {
    std::string bid;
    std::string status;

    // register class-members;
    AIGC_JSON_HELPER(bid, status);
};

struct FetchAppListProgressRequest {
    std::string device_sn;
    std::string box_sn;
    std::string bid;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, bid);
};

struct AIBoxAppInfo {
    std::string id;
    std::string name;
    std::string version;
    std::string description;
    std::string publisher;
    double app_size = 0.0;
    double data_size = 0.0;
    int type = 0;

    // register class-members;
    AIGC_JSON_HELPER(id, name, version, description, publisher, app_size, data_size, type);
};

struct FetchAppListProgressExt {
    std::string sn;
    std::vector<AIBoxAppInfo> ai_box_app_info;
    std::string error_reason;

    // register class-members;
    AIGC_JSON_HELPER(sn, ai_box_app_info, error_reason);
};

struct FetchAppListProgressResponse {
    std::string bid;
    std::string status;
    int percent = 0;
    int64_t create_time = 0;
    int64_t update_time = 0;
    int device_code = 0;
    FetchAppListProgressExt ext;

    // register class-members;
    AIGC_JSON_HELPER(bid, status, percent, create_time, update_time, device_code, ext);
};


struct ExportFileRequest {
    std::string device_sn;
    std::string box_sn;
    std::string app_id;
    int export_type = 0;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, app_id, export_type);
};

struct ExportFileResponse {
    std::string bid;
    std::string status;

    // register class-members;
    AIGC_JSON_HELPER(bid, status);
};

struct ExportFileProgressRequest {
    std::string device_sn;
    std::string box_sn;
    std::string bid;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, bid);
};

struct ExportFileProgressExt {
    std::string sn;
    int export_type = 0;
    std::string app_id;
    std::string object_key;

    // register class-members;
    AIGC_JSON_HELPER(sn, export_type, app_id, object_key);
};

struct ExportFileProgressResponse {
    std::string bid;
    std::string status;
    int percent = 0;
    int64_t create_time = 0;
    int64_t update_time = 0;
    int device_code = 0;
    ExportFileProgressExt ext;

    // register class-members;
    AIGC_JSON_HELPER(bid, status, percent, create_time, update_time, device_code, ext);
};

struct InstallAppRequest {
    std::string device_sn;
    std::string box_sn;
    std::string object_key;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, object_key);
};

struct InstallAppResponse {
    std::string bid;
    std::string status;

    // register class-members;
    AIGC_JSON_HELPER(bid, status);
};

struct InstallAppProgressRequest {
    std::string device_sn;
    std::string box_sn;
    std::string bid;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, bid);
};

struct InstallAppProgressExt {
    std::string sn;

    // register class-members;
    AIGC_JSON_HELPER(sn);
};

struct InstallAppProgressResponse {
    std::string bid;
    std::string status;
    int percent = 0;
    int64_t create_time = 0;
    int64_t update_time = 0;
    int device_code = 0;
    InstallAppProgressExt ext;

    // register class-members;
    AIGC_JSON_HELPER(bid, status, percent, create_time, update_time, device_code, ext);
};

struct OperateAppRequest {
    std::string device_sn;
    std::string box_sn;
    std::string app_id;
    std::string action;

    // register class-members;
    AIGC_JSON_HELPER(device_sn, box_sn, app_id, action);
};

struct OperateAppResponse {
    std::string bid;
    std::string status;

    // register class-members;
    AIGC_JSON_HELPER(bid, status);
};

struct GetUploadStsCredentials {
    std::string access_key_id;
    std::string access_key_secret;
    int64_t expire = 0;
    std::string security_token;

    // register class-members;
    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, security_token);
};

struct GetUploadStsResponse {
    GetUploadStsCredentials credentials;
    std::string provider;
    std::string bucket;
    std::string region;
    std::string endpoint;
    std::string object_key_prefix;

    // register class-members;
    AIGC_JSON_HELPER(credentials, provider, bucket, region, endpoint, object_key_prefix);
};







// PaaS ai-alert Response 嵌套类型（含 AIGC_JSON_HELPER，也被 request_message.h 复用）
struct AiAlertAlarmInfoResponse {
    std::vector<int> alarm_method;
    int alarm_interval = 0;
    AIGC_JSON_HELPER(alarm_method, alarm_interval);
};

struct AiAlertAlarmTargetResponse {
    int target_type = 0;
    int target_value = 0;
    bool use_min_threshold = false;
    bool use_max_threshold = false;
    int target_max_threshold = 0;
    int target_min_threshold = 0;
    AIGC_JSON_HELPER(target_type, target_value, use_min_threshold, use_max_threshold,
                     target_max_threshold, target_min_threshold);
};

struct AiAlertLocationResponse {
    double latitude = 0;
    double longitude = 0;
    double altitude = 0;
    AIGC_JSON_HELPER(latitude, longitude, altitude);
};

struct AiAlertTargetAlertInfoResponse {
    int target_type = 0;
    int target_value = 0;
    std::string label;
    AIGC_JSON_HELPER(target_type, target_value, label);
};

struct AiAlertFlightItemResponse {
    std::string flight_id;
    int count = 0;
    std::string flight_task_name;
    int status = 0;
    long long start_time = 0;
    AIGC_JSON_HELPER(flight_id, count, flight_task_name, status, start_time);
};

struct AiAlertRecordItemResponse {
    std::string alert_uuid;
    std::string flight_id;
    std::string drone_sn;
    std::string gateway_sn;
    int status = 0;
    int algorithm_source = 0;
    AiAlertLocationResponse location;
    std::vector<AiAlertTargetAlertInfoResponse> target_alert_infos;
    std::string thumbnail_url;
    std::vector<std::string> labels;
    long long timestamp = 0;
    std::string task_name;
    int media_index = 0;
    AIGC_JSON_HELPER(alert_uuid, flight_id, drone_sn, gateway_sn, status, algorithm_source,
                     location, target_alert_infos, thumbnail_url, labels, timestamp,
                     task_name, media_index);
};

// ---- POST /device-setting Response（成功只看 code=0，data 为空）----
struct PostAiAlertDeviceSettingResponse {};

// ---- GET /device-setting Response ----
struct GetAiAlertDeviceSettingResponse {
    std::string drone_sn;
    bool enable_target_alert = false;
    AiAlertAlarmInfoResponse alarm_info;
    AIGC_JSON_HELPER(drone_sn, enable_target_alert, alarm_info);
};

// ---- POST /algorithm-setting Response（成功只看 code=0，data 为空）----
struct PostAiAlertAlgorithmSettingResponse {};

// ---- GET /algorithm-setting Response ----
struct GetAiAlertAlgorithmSettingResponse {
    std::string drone_sn;
    int algorithm_type = 0;
    std::string algorithm_name;
    std::vector<AiAlertAlarmTargetResponse> alarm_targets;
    AIGC_JSON_HELPER(drone_sn, algorithm_type, algorithm_name, alarm_targets);
};

// ---- GET /flights Response ----
struct GetAiAlertFlightsResponse {
    std::vector<AiAlertFlightItemResponse> data;
    int total = 0;
    int page = 1;
    int page_size = 50;
    int page_count = 0;
    AIGC_JSON_HELPER(data, total, page, page_size, page_count);
};

// ---- GET /records Response ----
struct GetAiAlertRecordsResponse {
    std::unordered_map<std::string, std::vector<AiAlertRecordItemResponse>> data;
    int total = 0;
    int page = 1;
    int page_size = 50;
    int page_count = 0;
    AIGC_JSON_HELPER(data, total, page, page_size, page_count);
};

/// 爆闪灯模板片段（HTTP flash-configs）
struct FlashLedSegmentHttp {
    int color = 0;
    int count = 1;
    int on = 0;
    int off = 0;
    int interval = 0;
    AIGC_JSON_HELPER(color, count, on, off, interval);
};

/// 爆闪灯模板（HTTP 响应项）
struct FlashLedConfigHttpItem {
    std::string id;
    std::string name;
    std::vector<FlashLedSegmentHttp> flash_segments;
    AIGC_JSON_HELPER(id, name, flash_segments);
};

/// List 接口 data 为 JSON 数组（不能走 CommonResponse.data=string）
struct FlashLedConfigListRawResponse {
    int code = -1;
    std::string message;
    std::vector<FlashLedConfigHttpItem> data;
    AIGC_JSON_HELPER(code, message, data);
};

struct FlashLedConfigListHttpResponse {
    std::vector<FlashLedConfigHttpItem> configs;
};

struct CreateFlashLedConfigResponse {
    std::string id;
    AIGC_JSON_HELPER(id);
};

struct UpdateFlashLedConfigResponse {
    std::string id;
    std::string name;
    std::vector<FlashLedSegmentHttp> flash_segments;
    AIGC_JSON_HELPER(id, name, flash_segments);
};

// ===================== Live Sync Response Begin =====================

/// Http 版 schema_option（供 request/response 序列化使用）
struct LiveSyncSchemaOptionHttp {
    std::string server_id;
    std::string server_ip;
    std::string server_port;
    std::string device_password;
    std::string local_port;
    std::string device_id;
    std::string local_channel;
    std::string url;
    std::string provider;
    std::string volc_user_id;
    std::string rtsp_url;
    std::string username;
    std::string password;
    bool enable_ts = false;
    AIGC_JSON_HELPER(server_id, server_ip, server_port, device_password, local_port,
                     device_id, local_channel, url, provider, volc_user_id, rtsp_url,
                     username, password, enable_ts);
};

struct LiveSyncVideoCapacityHttp {
    std::string video_type;
    std::string video_index;
    AIGC_JSON_HELPER(video_type, video_index);
};

struct LiveSyncCameraCapacityHttp {
    std::string camera_index;
    std::vector<LiveSyncVideoCapacityHttp> videos_list;
    AIGC_JSON_HELPER(camera_index, videos_list);
};

struct LiveSyncItemHttp {
    std::string uuid;
    int status = 0;
    std::string org_uuid;
    std::string sn;
    std::string camera_index;
    std::string name;
    std::string schema;
    LiveSyncSchemaOptionHttp schema_option;
    std::string device_name;
    bool device_online = false;
    std::string device_model_key;
    bool is_airport = false;
    std::string video_type;
    int video_quality = 0;
    std::string region;
    bool enable = false;
    std::string err_msg;
    std::string device_type;
    AIGC_JSON_HELPER(uuid, status, org_uuid, sn, camera_index, name, schema, schema_option,
                     device_name, device_online, device_model_key, is_airport, video_type,
                     video_quality, region, enable, err_msg, device_type);
};

/// create 响应：LiveSyncItem 所有字段 + limit
struct LiveSyncCreateResponse {
    std::string uuid;
    int status = 0;
    std::string org_uuid;
    std::string sn;
    std::string camera_index;
    std::string name;
    std::string schema;
    LiveSyncSchemaOptionHttp schema_option;
    std::string device_name;
    bool device_online = false;
    std::string device_model_key;
    bool is_airport = false;
    std::string video_type;
    int video_quality = 0;
    std::string region;
    bool enable = false;
    std::string err_msg;
    std::string device_type;
    int limit = 0;   ///< 成功时 null→0；PaaS 超限走 HTTP 400，此字段不参与超限判断
    AIGC_JSON_HELPER(uuid, status, org_uuid, sn, camera_index, name, schema, schema_option,
                     device_name, device_online, device_model_key, is_airport, video_type,
                     video_quality, region, enable, err_msg, device_type, limit);
};

struct LiveSyncUpdateResponse {
    std::string uuid;
    int status = 0;
    std::string org_uuid;
    std::string sn;
    std::string camera_index;
    std::string name;
    std::string schema;
    LiveSyncSchemaOptionHttp schema_option;
    std::string device_name;
    bool device_online = false;
    std::string device_model_key;
    bool is_airport = false;
    std::string video_type;
    int video_quality = 0;
    std::string region;
    bool enable = false;
    std::string err_msg;
    std::string device_type;
    AIGC_JSON_HELPER(uuid, status, org_uuid, sn, camera_index, name, schema, schema_option,
                     device_name, device_online, device_model_key, is_airport, video_type,
                     video_quality, region, enable, err_msg, device_type);
};

struct LiveSyncListResponse {
    int page = 1;
    int size = 20;
    long long total = 0;
    std::vector<LiveSyncItemHttp> list;
    AIGC_JSON_HELPER(page, size, total, list);
};

struct LiveSyncDeviceV2Http {
    std::string sn;
    std::string name;
    std::string device_model_key;
    std::string device_type;
    std::vector<LiveSyncCameraCapacityHttp> camera_capacity;
    AIGC_JSON_HELPER(sn, name, device_model_key, device_type, camera_capacity);
};

struct LiveSyncDeviceItemV2Http {
    LiveSyncDeviceV2Http gateway;
    std::vector<LiveSyncDeviceV2Http> hosts;
    AIGC_JSON_HELPER(gateway, hosts);
};

struct LiveSyncDevicesV2Response {
    long long total = 0;
    std::vector<LiveSyncDeviceItemV2Http> list;
    AIGC_JSON_HELPER(total, list);
};

struct LiveSyncSummaryResponse {
    long long total = 0;
    long long enable_cnt = 0;
    AIGC_JSON_HELPER(total, enable_cnt);
};

// ===================== Live Sync Response End =====================

} // end of namespace flight_hub_sdk;
