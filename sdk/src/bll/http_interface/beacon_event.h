// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string_view>

// SDK 数据埋点(BeaconService)事件标识常量。
//
// 仅供 SDK 内部实现使用, 不属于对外 API, 故不放在公开头文件 api/value.h 中
// (放在那里会被 SWIG 生成一批无人使用的 Java getter, 且每个包含 value.h 的
//  编译单元都会各存一份 std::string 副本并在加载期做静态初始化)。
// 用 inline constexpr std::string_view: 全工程单一实体, 零静态初始化、零堆分配。

namespace flight_hub_sdk {

inline constexpr std::string_view kBeaconReportType_SDK_API = "sdk_api";

inline constexpr std::string_view kBeaconReportEvent_TAKE_OFF = "take_off";

inline constexpr std::string_view kBeaconReportEvent_RETURN_HOME = "return_home";

inline constexpr std::string_view kBeaconReportEvent_RETURN_HOME_CANCEL = "return_home_cancel";

inline constexpr std::string_view kBeaconReportEvent_DRC_FORCE_LANDING = "drc_force_landing";

inline constexpr std::string_view kBeaconReportEvent_DRC_EMERGENCY_LANDING = "drc_emergency_landing";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_PAUSE = "flight_task_pause";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_RESUME = "flight_task_resume";

inline constexpr std::string_view kBeaconReportEvent_FLY_TO_START = "fly_to_start";

inline constexpr std::string_view kBeaconReportEvent_FLY_TO_UPDATE_TARGET_POINTS = "fly_to_update_target_points";

inline constexpr std::string_view kBeaconReportEvent_FLY_TO_DRC_STOP = "fly_to_drc_stop";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_CREATE = "flight_task_create";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_EDIT = "flight_task_edit";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_DELETE = "flight_task_delete";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_UPDATE_STATUS = "flight_task_update_status";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_BATCH_UPDATE_STATUS = "flight_task_batch_update_status";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_GET_LIST = "flight_task_get_list";

inline constexpr std::string_view kBeaconReportEvent_COMMAND_TASK_GET_LIST = "command_task_get_list";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_GET_TRACK = "flight_task_get_track";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_GET_DETAIL = "flight_task_get_detail";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION = "flight_task_get_device_recommendation";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_TCAS = "flight_task_get_device_tcas";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_UPDATE_DEVICE_TCAS = "flight_task_update_device_tcas";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_START_ROAMING = "flight_task_start_roaming";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS = "flight_task_roaming_base_stations";

inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_GET_IN_FLIGHT_TASK_TOPOS = "flight_task_get_in_flight_task_topos";

inline constexpr std::string_view kBeaconReportEvent_START_LIVE_STREAM = "start_live_stream";

inline constexpr std::string_view kBeaconReportEvent_SET_LIVE_QUALITY = "set_live_quality";

inline constexpr std::string_view kBeaconReportEvent_GET_AUDIENCE_COUNT = "get_audience_count";

inline constexpr std::string_view kBeaconReportEvent_QUERY_HISTORY_HMS = "query_history_hms";

inline constexpr std::string_view kBeaconReportEvent_GET_DEVICE_LOG_LIST = "get_device_log_list";

inline constexpr std::string_view kBeaconReportEvent_CREATE_DEVICE_LOG_REPORT = "create_device_log_report";

inline constexpr std::string_view kBeaconReportEvent_GET_LOG_REPORT_LIST = "get_log_report_list";

inline constexpr std::string_view kBeaconReportEvent_POST_LOG_REPORT_OPERATION = "post_log_report_operation";

inline constexpr std::string_view kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE = "upload_log_append_file";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_RECONSTRUCTION_SETTINGS = "terra_get_reconstruction_settings";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_QUALITY_REPORT = "terra_get_quality_report";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_MODEL_FILE_LIST = "terra_get_model_file_list";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_MODEL_FILE_URL = "terra_get_model_file_url";

inline constexpr std::string_view kBeaconReportEvent_TERRA_CREATE_RECONSTRUCTION = "terra_create_reconstruction";

inline constexpr std::string_view kBeaconReportEvent_TERRA_RETRY_RECONSTRUCTION = "terra_retry_reconstruction";

inline constexpr std::string_view kBeaconReportEvent_TERRA_STOP_RECONSTRUCTION = "terra_stop_reconstruction";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_SFM_RESULT = "terra_get_sfm_result";

inline constexpr std::string_view kBeaconReportEvent_TERRA_DELETE_MODEL_FILE = "terra_delete_model_file";

inline constexpr std::string_view kBeaconReportEvent_TERRA_BATCH_DELETE_MODEL_FILES = "terra_batch_delete_model_files";

inline constexpr std::string_view kBeaconReportEvent_TERRA_CREATE_PANORAMA = "terra_create_panorama";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_RECONSTRUCTION_IMAGES = "terra_get_reconstruction_images";

inline constexpr std::string_view kBeaconReportEvent_TERRA_UPLOAD_CALLBACK = "terra_upload_callback";

inline constexpr std::string_view kBeaconReportEvent_TERRA_RENAME_MODEL = "terra_rename_model";

inline constexpr std::string_view kBeaconReportEvent_TERRA_SHOW_ON_MAP = "terra_show_on_map";

inline constexpr std::string_view kBeaconReportEvent_TERRA_GET_MODEL_DETAIL = "terra_get_model_detail";

inline constexpr std::string_view kBeaconReportEvent_TERRA_SET_LAS_WKT = "terra_set_las_wkt";

inline constexpr std::string_view kBeaconReportEvent_FETCH_AI_BOX_INFO = "fetch_ai_box_info";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_FETCH_FILE_DOWNLOAD_URL = "ai_box_fetch_file_download_url";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_FETCH_APP_LIST = "ai_box_fetch_app_list";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_FETCH_APP_LIST_PROGRESS = "ai_box_fetch_app_list_progress";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_EXPORT_FILE = "ai_box_export_file";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_EXPORT_FILE_PROGRESS = "ai_box_export_file_progress";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_INSTALL_APP = "ai_box_install_app";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_UPLOAD_FILE = "ai_box_upload_file";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_INSTALL_APP_PROGRESS = "ai_box_install_app_progress";

inline constexpr std::string_view kBeaconReportEvent_AI_BOX_OPERATE_APP = "ai_box_operate_app";


// ═══════════════════════════════════════════════════════════════════════════
// 以下为按「含 FlightHubError err 回调参数的业务接口必须埋点」补齐的事件。
// 命名规则: kBeaconReportEvent_<模块前缀>_<接口名 SNAKE_CASE>, 取值为其小写形式。
// ═══════════════════════════════════════════════════════════════════════════

// ── 飞行控制 (IFlightControlModule) ──
/// 紧急停桨
inline constexpr std::string_view kBeaconReportEvent_DRC_EMERGENCY_STOP = "drc_emergency_stop";

// ── 机场控制 (IDockControlModule) ──
/// 切换机场舱内/舱外摄像头
inline constexpr std::string_view kBeaconReportEvent_DOCK_CTRL_CHANGE_DOCK_CAMERA_POSITION = "dock_ctrl_change_dock_camera_position";
/// 设置机场补光灯开关
inline constexpr std::string_view kBeaconReportEvent_DOCK_CTRL_SET_DOCK_SUPPLEMENT_LIGHT_STATE = "dock_ctrl_set_dock_supplement_light_state";
/// 设置机场充电开关
inline constexpr std::string_view kBeaconReportEvent_DOCK_CTRL_SET_DOCK_CHARGE_STATE = "dock_ctrl_set_dock_charge_state";
/// 设置机场舱盖开关
inline constexpr std::string_view kBeaconReportEvent_DOCK_CTRL_SET_DOCK_COVER_STATE = "dock_ctrl_set_dock_cover_state";
/// 设置机场空调模式
inline constexpr std::string_view kBeaconReportEvent_DOCK_CTRL_SET_DOCK_AIR_CONDITIONER_MODE = "dock_ctrl_set_dock_air_conditioner_mode";

// ── 设备调试 (IDeviceDebugModule) ──
/// 打开调试模式
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_OPEN_DEBUG_MODE = "device_debug_open_debug_mode";
/// 关闭调试模式
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_CLOSE_DEBUG_MODE = "device_debug_close_debug_mode";
/// 打开机场舱盖
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_OPEN_DOCK_COVER = "device_debug_open_dock_cover";
/// 获取打开机场舱盖进度
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_GET_OPEN_DOCK_COVER_PROGRESS = "device_debug_get_open_dock_cover_progress";
/// 关闭机场舱盖
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_CLOSE_DOCK_COVER = "device_debug_close_dock_cover";
/// 获取关闭机场舱盖进度
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_GET_CLOSE_DOCK_COVER_PROGRESS = "device_debug_get_close_dock_cover_progress";
/// 设备重启
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_DEVICE_REBOOT = "device_debug_device_reboot";
/// 获取设备重启进度
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_GET_DEVICE_REBOOT_PROGRESS = "device_debug_get_device_reboot_progress";
/// 设备格式化
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_DEVICE_FORMAT = "device_debug_device_format";
/// 获取设备格式化进度
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_GET_DEVICE_FORMAT_PROGRESS = "device_debug_get_device_format_progress";
/// 飞行器开机
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_OPEN_DRONE = "device_debug_open_drone";
/// 飞行器关机
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_CLOSE_DRONE = "device_debug_close_drone";
/// 飞行器格式化（过程操作）
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_DRONE_FORMAT = "device_debug_drone_format";
/// 切换飞行器 SDR 工作模式（即时操作）
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_DRONE_SDR_WORK_MODE = "device_debug_drone_sdr_work_mode";
/// 打开充电
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_OPEN_CHARGE = "device_debug_open_charge";
/// 关闭充电
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_CLOSE_CHARGE = "device_debug_close_charge";
/// 设置机场空调模式
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_DOCK_AIR_CONDITIONER_MODE = "device_debug_set_dock_air_conditioner_mode";
/// 设置机场静音模式
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_DOCK_SILENT_MODE = "device_debug_set_dock_silent_mode";
/// 设置限远距离
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_DISTANCE_LIMIT = "device_debug_set_distance_limit";
/// 设置限高
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_HEIGHT_LIMIT = "device_debug_set_height_limit";
/// 设置避障状态
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_OBSTACLE_AVOIDANCE = "device_debug_set_obstacle_avoidance";
/// 设置返航剩余电量
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_REMAINING_POWER_FOR_RTH = "device_debug_set_remaining_power_for_rth";
/// 设置夜航灯状态
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_NIGHT_LIGHTS_STATE = "device_debug_set_night_lights_state";
/// 切换报警灯状态
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SWITCH_ALARM_LIGHT_STATE = "device_debug_switch_alarm_light_state";
/// eSIM 运营商切换
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_ESIM_OPERATOR_SWITCH = "device_debug_esim_operator_switch";
/// SIM 卡槽切换
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SIM_SLOT_SWITCH = "device_debug_sim_slot_switch";
/// 设置电池存储模式
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_BATTERY_STORE_MODE = "device_debug_set_battery_store_mode";
/// 发起手动换电
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_START_MANUAL_BATTERY_SWAP = "device_debug_start_manual_battery_swap";
/// 查询手动换电进度
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_GET_MANUAL_BATTERY_SWAP_PROGRESS = "device_debug_get_manual_battery_swap_progress";
/// Dongle 电源开关
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_DONGLE_POWER_SWITCH = "device_debug_set_dongle_power_switch";
/// 机场低功耗开关
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_LOW_POWER_SWITCH = "device_debug_set_low_power_switch";
/// 查询 eSIM 运营商切换进度
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_GET_ESIM_OPERATOR_SWITCH_PROGRESS = "device_debug_get_esim_operator_switch_progress";
/// 设置 Geo Caging（地理围栏）开关及围栏参数
inline constexpr std::string_view kBeaconReportEvent_DEVICE_DEBUG_SET_GEO_CAGING_ENABLE = "device_debug_set_geo_caging_enable";

// ── 设备管理 (IDeviceManageModule) ──
/// 获取设备列表（含拓扑关系）
inline constexpr std::string_view kBeaconReportEvent_DEVICE_MANAGE_GET_DEVICE_LIST = "device_manage_get_device_list";
/// 获取设备详情
inline constexpr std::string_view kBeaconReportEvent_DEVICE_MANAGE_GET_DEVICE_DETAIL_INFO = "device_manage_get_device_detail_info";
/// 修改设备名称
inline constexpr std::string_view kBeaconReportEvent_DEVICE_MANAGE_MODIFY_DEVICE_NAME = "device_manage_modify_device_name";
/// 解绑设备
inline constexpr std::string_view kBeaconReportEvent_DEVICE_MANAGE_UNBIND_DEVICE = "device_manage_unbind_device";
/// 获取机场设备状态
inline constexpr std::string_view kBeaconReportEvent_DEVICE_MANAGE_GET_DOCK_DEVICE_STATUS = "device_manage_get_dock_device_status";
/// 获取飞机设备状态
inline constexpr std::string_view kBeaconReportEvent_DEVICE_MANAGE_GET_DRONE_DEVICE_STATUS = "device_manage_get_drone_device_status";

// ── 飞行设置 (IFlightSettingsModule) ──
/// 设置智能规划模式
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_SETTINGS_SET_FLIGHT_SMART_PLAN_MODE = "flight_settings_set_flight_smart_plan_mode";
/// 设置失控动作
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_SETTINGS_SET_OUT_OF_CONTROL_ACTION = "flight_settings_set_out_of_control_action";
/// 设置飞行任务高度
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_SETTINGS_SET_FLIGHT_TASK_HEIGHT = "flight_settings_set_flight_task_height";
/// 设置返航高度
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_SETTINGS_SET_RTH_ALTITUDE = "flight_settings_set_rth_altitude";
/// 设置空中回传开关
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_SETTINGS_SET_AIR_TRANSFER_ENABLE = "flight_settings_set_air_transfer_enable";

// ── POI 环绕 (IPoiModule) ──
/// 进入 POI 环绕模式
inline constexpr std::string_view kBeaconReportEvent_POI_ENTER = "poi_enter";
/// 退出 POI 环绕模式
inline constexpr std::string_view kBeaconReportEvent_POI_EXIT = "poi_exit";
/// 更新 POI 环绕速度
inline constexpr std::string_view kBeaconReportEvent_POI_UPDATE_SPEED = "poi_update_speed";
/// 查询当前 POI 任务信息
inline constexpr std::string_view kBeaconReportEvent_POI_GET_TASK = "poi_get_task";

// ── 空中航线 (IInFlightWaylineModule) ──
/// 创建空中航线任务（自动注入当前设备的 drone_sn / gateway_sn）
inline constexpr std::string_view kBeaconReportEvent_IN_FLIGHT_WAYLINE_CREATE_TASK = "in_flight_wayline_create_task";
/// 控制空中航线任务（暂停 / 恢复 / 取消）
inline constexpr std::string_view kBeaconReportEvent_IN_FLIGHT_WAYLINE_CONTROL_TASK = "in_flight_wayline_control_task";
/// 查询当前设备活跃中的空中航线任务
inline constexpr std::string_view kBeaconReportEvent_IN_FLIGHT_WAYLINE_GET_ACTIVE_TASK = "in_flight_wayline_get_active_task";

// ── 试飞 (ITestFlightModule) ──
/// 生成试飞航线
inline constexpr std::string_view kBeaconReportEvent_TEST_FLIGHT_GENERATE_TRY_FLIGHT_WAYLINE = "test_flight_generate_try_flight_wayline";
/// 创建试飞任务
inline constexpr std::string_view kBeaconReportEvent_TEST_FLIGHT_CREATE_TASK = "test_flight_create_task";

// ── 部署/进离场 (IDeployModule) ──
/// 下载星历数据
inline constexpr std::string_view kBeaconReportEvent_DEPLOY_DOWNLOAD_EPHEMERIS = "deploy_download_ephemeris";
/// 执行进离场航线侦察任务（开始/暂停）
inline constexpr std::string_view kBeaconReportEvent_DEPLOY_EXECUTE_IN_OUT_WAYLINE_DETECT = "deploy_execute_in_out_wayline_detect";
/// 设置进离场航线飞行参数
inline constexpr std::string_view kBeaconReportEvent_DEPLOY_SET_IN_OUT_WAYLINE_FLIGHT = "deploy_set_in_out_wayline_flight";
/// 获取进离场航线飞行设置
inline constexpr std::string_view kBeaconReportEvent_DEPLOY_GET_IN_OUT_WAYLINE_FLIGHT_SETTING = "deploy_get_in_out_wayline_flight_setting";

// ── RTK 标定 (IRtkCalibrationModule) ──
/// RTK 标定（网络模式）
inline constexpr std::string_view kBeaconReportEvent_RTK_CALIB_BY_NETWORK = "rtk_calib_by_network";
/// RTK 标定（手动模式）
inline constexpr std::string_view kBeaconReportEvent_RTK_CALIB_BY_MANUAL = "rtk_calib_by_manual";

// ── 网络测速 (INetworkTestModule) ──
/// 开始网络测速
inline constexpr std::string_view kBeaconReportEvent_NETWORK_TEST_START = "network_test_start";
/// 停止网络测速
inline constexpr std::string_view kBeaconReportEvent_NETWORK_TEST_STOP = "network_test_stop";
/// 获取网络测速进度
inline constexpr std::string_view kBeaconReportEvent_NETWORK_TEST_GET_PROGRESS = "network_test_get_progress";

// ── 中继站控制 (IRelayControlModule) ──
/// 设置中继站广播隐藏状态
inline constexpr std::string_view kBeaconReportEvent_RELAY_SET_BROADCAST_HIDE = "relay_set_broadcast_hide";
/// 设置中继站 SSID
inline constexpr std::string_view kBeaconReportEvent_RELAY_SET_SSID = "relay_set_ssid";
/// 设置中继站密码
inline constexpr std::string_view kBeaconReportEvent_RELAY_SET_PASSWORD = "relay_set_password";
/// 设置中继站搜索模式
inline constexpr std::string_view kBeaconReportEvent_RELAY_SET_SEARCH_MODE = "relay_set_search_mode";
/// 设置中继站占用状态
inline constexpr std::string_view kBeaconReportEvent_RELAY_SET_OCCUPIED_STATUS = "relay_set_occupied_status";
/// 中继站重启
inline constexpr std::string_view kBeaconReportEvent_RELAY_REBOOT = "relay_reboot";
/// 中继站断开连接
inline constexpr std::string_view kBeaconReportEvent_RELAY_DISCONNECT = "relay_disconnect";

// ── PSDK 负载控制 (IPsdkControlModule) ──
/// 设置探照灯模式
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_MODE_SET = "psdk_ctrl_drc_searchlight_mode_set";
/// 设置探照灯亮度
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_BRIGHTNESS_SET = "psdk_ctrl_drc_searchlight_brightness_set";
/// 探照灯校准
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_CALIBRATION = "psdk_ctrl_drc_searchlight_calibration";
/// 探照灯微调设置
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_FINE_TUNING_SET = "psdk_ctrl_drc_searchlight_fine_tuning_set";
/// 设置探照灯广域模式
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_WIDE_FIELD_MODE_SET = "psdk_ctrl_drc_searchlight_wide_field_mode_set";
/// 设置探照灯云台控制
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_LIGHT_GIMBAL_CONTROL_SET = "psdk_ctrl_drc_searchlight_light_gimbal_control_set";
/// 设置探照灯类型
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SEARCHLIGHT_TYPE_SET = "psdk_ctrl_drc_searchlight_type_set";
/// 开始音频喊话
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_AUDIO_PLAY_START = "psdk_ctrl_drc_speaker_audio_play_start";
/// 开始 TTS 喊话
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_PLAY_START = "psdk_ctrl_drc_speaker_tts_play_start";
/// 开始实时喊话
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_START = "psdk_ctrl_drc_speaker_real_time_start";
/// 停止实时喊话
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REAL_TIME_STOP = "psdk_ctrl_drc_speaker_real_time_stop";
/// 重播喊话
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_REPLAY = "psdk_ctrl_drc_speaker_replay";
/// 停止喊话播放
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_STOP = "psdk_ctrl_drc_speaker_play_stop";
/// 设置喊话器播放音量
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_VOLUME_SET = "psdk_ctrl_drc_speaker_play_volume_set";
/// 设置喊话器播放模式
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_PLAY_MODE_SET = "psdk_ctrl_drc_speaker_play_mode_set";
/// 设置 TTS 配置
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_SPEAKER_TTS_SET = "psdk_ctrl_drc_speaker_tts_set";
/// 设置 Widget 值
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_VALUE_SET = "psdk_ctrl_drc_widget_value_set";
/// 设置 Widget 输入框文本
inline constexpr std::string_view kBeaconReportEvent_PSDK_CTRL_DRC_WIDGET_INPUT_BOX_TEXT_SET = "psdk_ctrl_drc_widget_input_box_text_set";

// ── PSDK 资源 (IPsdkResourceModule) ──
/// 获取 PSDK 喊话器资源列表
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_GET_PSDK_SPEAKER_RESOURCE_LIST = "psdk_res_get_psdk_speaker_resource_list";
/// 创建 PSDK 喊话器资源
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_CREATE_PSDK_SPEAKER_RESOURCE = "psdk_res_create_psdk_speaker_resource";
/// 编辑 PSDK 喊话器资源
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_EDIT_PSDK_SPEAKER_RESOURCE = "psdk_res_edit_psdk_speaker_resource";
/// 删除 PSDK 喊话器资源
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_DELETE_PSDK_SPEAKER_RESOURCE = "psdk_res_delete_psdk_speaker_resource";
/// 获取 STS 临时凭证
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_GET_STS = "psdk_res_get_sts";
/// 批量获取 PSDK 控件资源
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_GET_PSDK_WIDGET_RESOURCES = "psdk_res_get_psdk_widget_resources";
/// 上传喊话器音频文件
inline constexpr std::string_view kBeaconReportEvent_PSDK_RES_UPLOAD_SPEAKER_AUDIO_FILE = "psdk_res_upload_speaker_audio_file";

// ── 爆闪灯 (IFlashLedModule) ──
/// 获取爆闪灯模板列表
inline constexpr std::string_view kBeaconReportEvent_FLASH_LED_GET_CONFIG_LIST = "flash_led_get_config_list";
/// 创建爆闪灯模板
inline constexpr std::string_view kBeaconReportEvent_FLASH_LED_CREATE_CONFIG = "flash_led_create_config";
/// 更新爆闪灯模板
inline constexpr std::string_view kBeaconReportEvent_FLASH_LED_UPDATE_CONFIG = "flash_led_update_config";
/// 删除爆闪灯模板
inline constexpr std::string_view kBeaconReportEvent_FLASH_LED_DELETE_CONFIG = "flash_led_delete_config";

// ── 航线管理 (IWaylineManageModule) ──
/// 获取航线列表
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_GET_LIST = "wayline_get_list";
/// 获取航线详情
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_GET_DETAIL = "wayline_get_detail";
/// 批量删除航线
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_BATCH_DELETE_WAYLINES = "wayline_batch_delete_waylines";
/// 更新航线
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_UPDATE = "wayline_update";
/// 提交航线文件信息
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_SUBMIT_INFO = "wayline_submit_info";
/// 批量下载航线文件
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_BATCH_DOWNLOAD_WAYLINES = "wayline_batch_download_waylines";
/// 创建副本航线
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_DUPLICATE = "wayline_duplicate";
/// 航线复制（去组织化，不需要 workspace_id）
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_COPY = "wayline_copy";
/// 航线拼接（去组织化，不需要 workspace_id）
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_CONCAT = "wayline_concat";
/// 上传 KMZ 文件并导入航线
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_IMPORT_KMZ = "wayline_import_kmz";
/// 获取航线编辑器 OSS STS 临时上传凭证
inline constexpr std::string_view kBeaconReportEvent_WAYLINE_GET_EDITOR_STS = "wayline_get_editor_sts";

// ── 固件管理 (IFirmwareManageModule) ──
/// 获取最新固件发布说明
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_GET_LATEST_RELEASE_NOTE = "firmware_get_latest_release_note";
/// 触发固件升级
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_INVOKE_UPGRADE = "firmware_invoke_upgrade";
/// 查询固件升级进度
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_QUERY_UPGRADE_PROGRESS = "firmware_query_upgrade_progress";
/// 上传固件文件
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_UPLOAD_FILE = "firmware_upload_file";
/// 发布固件
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_RELEASE = "firmware_release";
/// 取消发布固件
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_UNRELEASE = "firmware_unrelease";
/// 删除固件
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_DELETE = "firmware_delete";
/// 查询固件列表
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_QUERY_LIST = "firmware_query_list";
/// 查询固件兼容性配置
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_QUERY_COMPATIBILITY_CONFIG = "firmware_query_compatibility_config";
/// 检查固件状态
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_CHECK_STATUS = "firmware_check_status";
/// 提交固件信息
inline constexpr std::string_view kBeaconReportEvent_FIRMWARE_SUBMIT_INFO = "firmware_submit_info";

// ── 飞行安全 (ISafetyServiceModule) ──
/// 获取信号地图
inline constexpr std::string_view kBeaconReportEvent_SAFETY_SEARCH_SIGNAL_MAPS = "safety_search_signal_maps";
/// 创建自定义飞行区域
inline constexpr std::string_view kBeaconReportEvent_SAFETY_CREATE_CUSTOMIZED_FLIGHT_AREAS = "safety_create_customized_flight_areas";
/// 获取自定义飞行区域
inline constexpr std::string_view kBeaconReportEvent_SAFETY_GET_CUSTOMIZED_FLIGHT_AREAS = "safety_get_customized_flight_areas";
/// 更新自定义飞行区域
inline constexpr std::string_view kBeaconReportEvent_SAFETY_UPDATE_CUSTOMIZED_FLIGHT_AREAS = "safety_update_customized_flight_areas";
/// 删除自定义飞行区域
inline constexpr std::string_view kBeaconReportEvent_SAFETY_DELETE_CUSTOMIZED_FLIGHT_AREAS = "safety_delete_customized_flight_areas";
/// 同步自定义飞行区域
inline constexpr std::string_view kBeaconReportEvent_SAFETY_SYNC_CUSTOMIZED_FLIGHT_AREAS = "safety_sync_customized_flight_areas";
/// 获取自定义飞行区域同步状态
inline constexpr std::string_view kBeaconReportEvent_SAFETY_GET_FLIGHT_AREAS_SYNC_STATUS = "safety_get_flight_areas_sync_status";
/// 查询离线地图模型信息
inline constexpr std::string_view kBeaconReportEvent_SAFETY_GET_OFFLINE_MAPS_MODEL_INFO = "safety_get_offline_maps_model_info";
/// 创建/更新离线地图模型
inline constexpr std::string_view kBeaconReportEvent_SAFETY_UPDATE_OFFLINE_MAPS_MODEL_INFO = "safety_update_offline_maps_model_info";
/// 取消离线地图生成
inline constexpr std::string_view kBeaconReportEvent_SAFETY_CANCEL_OFFLINE_MAPS_MODEL_INFO = "safety_cancel_offline_maps_model_info";
/// 修改离线地图（开启/关闭）
inline constexpr std::string_view kBeaconReportEvent_SAFETY_SET_OFFLINE_MAPS = "safety_set_offline_maps";
/// 离线地图同步
inline constexpr std::string_view kBeaconReportEvent_SAFETY_SYNC_OFFLINE_MAPS = "safety_sync_offline_maps";
/// 获取设备同步状态
inline constexpr std::string_view kBeaconReportEvent_SAFETY_GET_OFFLINE_MAPS_SYNC_STATUS = "safety_get_offline_maps_sync_status";
/// 获取解锁证书列表
inline constexpr std::string_view kBeaconReportEvent_SAFETY_GET_UNLOCK_LICENSE_LIST = "safety_get_unlock_license_list";
/// 切换解锁证书状态
inline constexpr std::string_view kBeaconReportEvent_SAFETY_SWITCH_UNLOCK_LICENSE = "safety_switch_unlock_license";
/// 检查解锁证书
inline constexpr std::string_view kBeaconReportEvent_SAFETY_CHECK_UNLOCK_LICENSE = "safety_check_unlock_license";
/// 提交解锁证书
inline constexpr std::string_view kBeaconReportEvent_SAFETY_SUBMIT_UNLOCK_LICENSE = "safety_submit_unlock_license";
/// 清除设备解锁证书
inline constexpr std::string_view kBeaconReportEvent_SAFETY_CLEAR_DEVICE_UNLOCK_LICENSE = "safety_clear_device_unlock_license";
/// 上传解锁证书文件
inline constexpr std::string_view kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE = "safety_upload_unlock_license_file";

// ── 要素/图层 (IElementModule) ──
/// 创建要素
inline constexpr std::string_view kBeaconReportEvent_ELEMENT_CREATE = "element_create";
/// 更新要素属性或移动要素到目标图层
inline constexpr std::string_view kBeaconReportEvent_ELEMENT_UPDATE = "element_update";
/// 查询元素层级列表（目录树 JSON 透传）
inline constexpr std::string_view kBeaconReportEvent_ELEMENT_LIST_HIERARCHY = "element_list_hierarchy";
/// 设置要素显示/隐藏状态（PaaS 专用简化接口）
inline constexpr std::string_view kBeaconReportEvent_ELEMENT_SET_VISIBILITY = "element_set_visibility";
/// 删除要素（仅限基础几何类型：点/线/面/圆）
inline constexpr std::string_view kBeaconReportEvent_ELEMENT_DELETE = "element_delete";

// ── 媒体服务 (IMediaServiceModule) ──
/// 获取媒体文件列表
inline constexpr std::string_view kBeaconReportEvent_MEDIA_GET_FILES_LIST = "media_get_files_list";
/// 批量删除媒体文件
inline constexpr std::string_view kBeaconReportEvent_MEDIA_DELETE_FILES = "media_delete_files";
/// 获取媒体文件下载地址（按 file_id）
inline constexpr std::string_view kBeaconReportEvent_MEDIA_GET_FILES_DOWNLOAD_URL_BY_FILE_ID = "media_get_files_download_url_by_file_id";
/// 获取媒体文件下载地址（按 object_keys）
inline constexpr std::string_view kBeaconReportEvent_MEDIA_GET_FILES_DOWNLOAD_URL_BY_OBJECT_KEYS = "media_get_files_download_url_by_object_keys";
/// 获取 OSS STS 临时上传凭证
inline constexpr std::string_view kBeaconReportEvent_MEDIA_GET_UPLOAD_STS = "media_get_upload_sts";
/// 媒体文件上传完成后通知服务端
inline constexpr std::string_view kBeaconReportEvent_MEDIA_UPLOAD_CALLBACK = "media_upload_callback";
/// 上传地理标注媒体资源（图片备注上传回调）
inline constexpr std::string_view kBeaconReportEvent_MEDIA_UPLOAD_GEO_FILE = "media_upload_geo_file";
/// 根据文件 ID 获取媒体文件详情（轮询文件 URL）
inline constexpr std::string_view kBeaconReportEvent_MEDIA_GET_FILE_DETAIL_BY_ID = "media_get_file_detail_by_id";

// ── 转码器 (ILiveSyncModule) ──
/// 创建转码器
inline constexpr std::string_view kBeaconReportEvent_LIVE_SYNC_CREATE = "live_sync_create";
/// 删除转码器
inline constexpr std::string_view kBeaconReportEvent_LIVE_SYNC_DELETE = "live_sync_delete";
/// 更新转码器（仅 enable 或全量改配置）
inline constexpr std::string_view kBeaconReportEvent_LIVE_SYNC_UPDATE = "live_sync_update";
/// 分页查询转码器列表
inline constexpr std::string_view kBeaconReportEvent_LIVE_SYNC_GET_LIST = "live_sync_get_list";
/// 获取可用设备树（V2，含基站/Dock）
inline constexpr std::string_view kBeaconReportEvent_LIVE_SYNC_GET_DEVICES_V2 = "live_sync_get_devices_v2";
/// 获取转码器总数与启用数汇总
inline constexpr std::string_view kBeaconReportEvent_LIVE_SYNC_GET_SUMMARY = "live_sync_get_summary";

// ── 直播录制 (ILiveRecordModule) ──
/// 开始录制当前直播流
inline constexpr std::string_view kBeaconReportEvent_LIVE_RECORD_START = "live_record_start";
/// 停止录制当前直播流
inline constexpr std::string_view kBeaconReportEvent_LIVE_RECORD_STOP = "live_record_stop";
/// 按设备 SN 查询录制记录列表
inline constexpr std::string_view kBeaconReportEvent_LIVE_RECORD_GET_DEVICE_LIVE_RECORDS = "live_record_get_device_live_records";

// ── AI 告警 (IAIAlertModule) ──
/// 写设备级告警配置
inline constexpr std::string_view kBeaconReportEvent_AI_ALERT_POST_DEVICE_SETTING = "ai_alert_post_device_setting";
/// 读设备级告警配置
inline constexpr std::string_view kBeaconReportEvent_AI_ALERT_GET_DEVICE_SETTING = "ai_alert_get_device_setting";
/// 写算法级告警配置（唯一键 drone_sn + algorithm_type，Upsert）
inline constexpr std::string_view kBeaconReportEvent_AI_ALERT_POST_ALGORITHM_SETTING = "ai_alert_post_algorithm_setting";
/// 读算法级告警配置
inline constexpr std::string_view kBeaconReportEvent_AI_ALERT_GET_ALGORITHM_SETTING = "ai_alert_get_algorithm_setting";
/// 查询架次告警统计（先拿 flight_id，再调 getAiAlertRecords）
inline constexpr std::string_view kBeaconReportEvent_AI_ALERT_GET_FLIGHTS = "ai_alert_get_flights";
/// 查询告警明细列表（按 flight_id 分组，含 thumbnail_url）
inline constexpr std::string_view kBeaconReportEvent_AI_ALERT_GET_RECORDS = "ai_alert_get_records";

// ── 机载算法管理 (IAIInsideManageModule) ──
/// 获取组织下机载算法列表
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_GET_MODELS = "ai_inside_get_models";
/// 获取指定算法绑定的设备详情（分页）
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_GET_MODEL_DEVICES = "ai_inside_get_model_devices";
/// 获取指定算法在指定操作类型下的可操作设备数量统计
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_GET_MODEL_DEVICES_SUMMARY = "ai_inside_get_model_devices_summary";
/// 批量下发 Load / Update / Unload 操作指令
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_OPERATE_MODEL = "ai_inside_operate_model";
/// 创建算法包上传凭证（私有化）
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_CREATE_ALGORITHM = "ai_inside_create_algorithm";
/// 处理已上传的算法文件
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_FILE_UPLOAD_ALGORITHM = "ai_inside_file_upload_algorithm";
/// 删除指定算法
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_DELETE_ALGORITHM = "ai_inside_delete_algorithm";
/// 一站式算法文件上传（私有化）
inline constexpr std::string_view kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE = "ai_inside_upload_algorithm_file";

// ── 自检定损 (IDiagnoseManagementModule) ──
/// 下发设备自检定损指令
inline constexpr std::string_view kBeaconReportEvent_DIAGNOSE_CREATE_SELF_DIAGNOSIS = "diagnose_create_self_diagnosis";
/// 定损操作（取消/删除）
inline constexpr std::string_view kBeaconReportEvent_DIAGNOSE_OPERATE_SELF_DIAGNOSIS = "diagnose_operate_self_diagnosis";
/// 查询单条定损报告详情
inline constexpr std::string_view kBeaconReportEvent_DIAGNOSE_GET_SELF_DIAGNOSIS_REPORT = "diagnose_get_self_diagnosis_report";
/// 查询定损报告列表
inline constexpr std::string_view kBeaconReportEvent_DIAGNOSE_GET_SELF_DIAGNOSIS_REPORT_LIST = "diagnose_get_self_diagnosis_report_list";

// ── 报修维保 (IMaintenanceManagementModule) ──
/// 获取设备报修配置
inline constexpr std::string_view kBeaconReportEvent_MAINTENANCE_GET_REPAIR_CONFIGS = "maintenance_get_repair_configs";
/// 创建报修订单
inline constexpr std::string_view kBeaconReportEvent_MAINTENANCE_CREATE_ORDER = "maintenance_create_order";
/// 查询单个报修订单详情
inline constexpr std::string_view kBeaconReportEvent_MAINTENANCE_GET_ORDER_DETAIL = "maintenance_get_order_detail";
/// 分页查询报修订单列表
inline constexpr std::string_view kBeaconReportEvent_MAINTENANCE_GET_ORDERS_LIST = "maintenance_get_orders_list";

// ── 飞行记录 (IFlightTaskRecordModule) ──
/// 导出飞行记录
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_RECORD_EXPORT_FLIGHT_RECORDS = "flight_task_record_export_flight_records";
/// 查询导出进度
inline constexpr std::string_view kBeaconReportEvent_FLIGHT_TASK_RECORD_GET_EXPORT_FLIGHT_RECORDS_PROGRESS = "flight_task_record_get_export_flight_records_progress";

// ── 相机负载控制 (ICameraControlModule) ──
/// 切换直播镜头
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_LIVE_LENS = "camera_ctrl_drc_switch_live_lens";
/// 设置照片存储参数
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_STORAGE_SETTINGS = "camera_ctrl_drc_set_photo_storage_settings";
/// 设置视频存储参数
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_STORAGE_SETTINGS = "camera_ctrl_drc_set_video_storage_settings";
/// 设置夜景模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_NIGHT_SCENE_MODE = "camera_ctrl_drc_set_night_scene_mode";
/// 拍照
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_TAKE_PHOTO = "camera_ctrl_drc_take_photo";
/// 智能低光拍照
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SMART_PHOTO_TAKE_PHOTO = "camera_ctrl_drc_smart_photo_take_photo";
/// 开始录像
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_START_RECORD = "camera_ctrl_drc_start_record";
/// 停止录像
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_RECORD = "camera_ctrl_drc_stop_record";
/// 框选变焦
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_ZOOM_AT_TARGET = "camera_ctrl_drc_zoom_at_target";
/// 设置焦距
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_FOCAL_LENGTH = "camera_ctrl_drc_set_focal_length";
/// 设置联动变焦
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_LINKAGE_ZOOM = "camera_ctrl_drc_set_linkage_zoom";
/// 重置云台
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_GIMBAL = "camera_ctrl_drc_reset_gimbal";
/// 看向目标
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_LOOK_AT_TARGET = "camera_ctrl_look_at_target";
/// 画面拖动控制云台转动速度
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SEND_CAMERA_SCREEN_DRAG = "camera_ctrl_drc_send_camera_screen_drag";
/// 开始全景拍摄
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_START_PANORAMA = "camera_ctrl_drc_start_panorama";
/// 停止全景拍摄
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_PANORAMA = "camera_ctrl_drc_stop_panorama";
/// 设置照片分辨率
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PHOTO_SIZE = "camera_ctrl_drc_set_photo_size";
/// 设置定时拍参数
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INTERVAL_TAKE_PHOTO = "camera_ctrl_drc_set_interval_take_photo";
/// 执行定时拍
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_INTERVAL_TAKE_PHOTO = "camera_ctrl_drc_interval_take_photo";
/// 停止拍照
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_STOP_TAKE_PHOTO = "camera_ctrl_drc_stop_take_photo";
/// 双击画面瞄准目标
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_AIM_AT_TARGET = "camera_ctrl_drc_camera_aim_at_target";
/// 切换相机模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SWITCH_CAMERA_MODE = "camera_ctrl_drc_switch_camera_mode";
/// 设置红外增益模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_GAIN_MODE = "camera_ctrl_drc_set_thermal_gain_mode";
/// 设置相机光圈值
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_APERTURE_VALUE = "camera_ctrl_drc_set_camera_aperture_value";
/// 设置相机曝光模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_MODE = "camera_ctrl_drc_set_camera_exposure_mode";
/// 设置相机曝光值
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_EXPOSURE_VALUE = "camera_ctrl_drc_set_camera_exposure_value";
/// 设置相机对焦模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_MODE = "camera_ctrl_drc_set_camera_focus_mode";
/// 设置相机对焦值
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_FOCUS_VALUE = "camera_ctrl_drc_set_camera_focus_value";
/// 设置相机 ISO
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_ISO = "camera_ctrl_drc_set_camera_iso";
/// 设置相机机械快门
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_MECHANICAL_SHUTTER = "camera_ctrl_drc_set_camera_mechanical_shutter";
/// 相机点对焦操作
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_CAMERA_POINT_FOCUS_ACTION = "camera_ctrl_drc_camera_point_focus_action";
/// 设置相机快门速度
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SHUTTER = "camera_ctrl_drc_set_camera_shutter";
/// 设置相机水印
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_WATERMARK = "camera_ctrl_drc_set_camera_watermark";
/// 设置相机畸变矫正
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DEWARPING = "camera_ctrl_drc_set_camera_dewarping";
/// 设置相机分屏显示
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_SCREEN_SPLIT = "camera_ctrl_drc_set_camera_screen_split";
/// 镜头去雾
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_CLEAN_FOG = "camera_ctrl_drc_clean_fog";
/// 设置热成像调色板
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_CURRENT_PALETTE = "camera_ctrl_drc_set_thermal_current_palette";
/// 设置热成像超分辨率
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_THERMAL_SUPER_RESOLUTION_ENABLE = "camera_ctrl_drc_set_thermal_super_resolution_enable";
/// 设置红外测温模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_MODE = "camera_ctrl_drc_set_ir_metering_mode";
/// 设置红外测温点
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_POINT = "camera_ctrl_drc_set_ir_metering_point";
/// 设置红外测温区域
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_METERING_AREA = "camera_ctrl_drc_set_ir_metering_area";
/// 设置红外显示模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_DISPLAY_MODE = "camera_ctrl_drc_set_ir_display_mode";
/// 设置红外增强模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_ENHANCE_MODE = "camera_ctrl_drc_set_ir_enhance_mode";
/// 设置红外融合比例
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_IR_MIX_RATE = "camera_ctrl_drc_set_ir_mix_rate";
/// 设置等温线开关
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ENABLE = "camera_ctrl_drc_set_isotherm_enable";
/// 设置等温线着色类型
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_COLOR_RENDERING_TYPE = "camera_ctrl_drc_set_isotherm_color_rendering_type";
/// 设置等温线温度上下限
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_LOWER_UPPER_RANGE = "camera_ctrl_drc_set_isotherm_lower_upper_range";
/// 设置等温线多边形渲染区域
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_RENDERING_POLYGON = "camera_ctrl_drc_set_isotherm_rendering_polygon";
/// 设置按框选区域的等温线渲染使能
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ISOTHERM_ROI_BASED_RENDERING_ENABLE = "camera_ctrl_drc_set_isotherm_roi_based_rendering_enable";
/// 设置近红外补光灯使能
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_INFRARED_FILL_LIGHT_ENABLE = "camera_ctrl_drc_set_infrared_fill_light_enable";
/// 设置黑白夜视使能
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_NIGHT_VISION_ENABLE = "camera_ctrl_drc_set_camera_night_vision_enable";
/// 重置相机设置
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_RESET_CAMERA_SETTINGS = "camera_ctrl_drc_reset_camera_settings";
/// 设置相机降噪等级
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_DENOISE_LEVEL_SET = "camera_ctrl_drc_set_camera_denoise_level_set";
/// 设置视频分辨率
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_VIDEO_RESOLUTION = "camera_ctrl_drc_set_video_resolution";
/// 设置照片格式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_CAMERA_PHOTO_FORMAT = "camera_ctrl_drc_set_camera_photo_format";
/// 设置预录制状态
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_STATUS = "camera_ctrl_drc_set_pre_recorded_status";
/// 设置预录制时长
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_PRE_RECORDED_TIME = "camera_ctrl_drc_set_pre_recorded_time";
/// 设置倒计时动作
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_COUNT_DOWN_ACTION = "camera_ctrl_drc_set_count_down_action";
/// 设置辅助画面模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_MODE = "camera_ctrl_drc_set_assistant_video_mode";
/// 设置辅助画面开关
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ASSISTANT_VIDEO_SWITCH = "camera_ctrl_drc_set_assistant_video_switch";
/// 设置相机电子去雾模式
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_MODE = "camera_ctrl_drc_set_algorithm_dehaze_mode";
/// 设置相机电子去雾等级
inline constexpr std::string_view kBeaconReportEvent_CAMERA_CTRL_DRC_SET_ALGORITHM_DEHAZE_LEVEL = "camera_ctrl_drc_set_algorithm_dehaze_level";

// ── 飞行控制 (IFlightControlModule) — DRC 辅助指令 ──
/// 设置飞机悬停时间
inline constexpr std::string_view kBeaconReportEvent_DRC_SET_DRONE_HOVER = "drc_set_drone_hover";
/// 设置指哪打哪参数
inline constexpr std::string_view kBeaconReportEvent_DRC_SET_FOLLOW_MOUSE_PARAMETER = "drc_set_follow_mouse_parameter";
/// 设置夜航灯状态
inline constexpr std::string_view kBeaconReportEvent_DRC_SET_NIGHT_LIGHTS_STATE = "drc_set_night_lights_state";
/// 设置隐身模式
inline constexpr std::string_view kBeaconReportEvent_DRC_SET_STEALTH_STATE = "drc_set_stealth_state";
/// 开伞
inline constexpr std::string_view kBeaconReportEvent_DRC_OPEN_PARACHUTE = "drc_open_parachute";
/// 设置爆闪灯
inline constexpr std::string_view kBeaconReportEvent_DRC_SET_FLASH_LED = "drc_set_flash_led";
/// 相机 LookAt
inline constexpr std::string_view kBeaconReportEvent_DRC_CAMERA_LOOK_AT = "drc_camera_look_at";

// ── 机载 AI (IOnboardAiModule) ──
/// 设置 AI 识别开关
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SET = "onboard_ai_ai_identify_set";
/// 设置 AI 识别过滤
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_FILTER_SET = "onboard_ai_ai_identify_filter_set";
/// 设置 AI 识别分数阈值
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_SET = "onboard_ai_ai_identify_score_set";
/// 设置 AI 识别分数模式
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_MODE_SET = "onboard_ai_ai_identify_score_mode_set";
/// 重置 AI 识别分数
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_AI_IDENTIFY_SCORE_RESET = "onboard_ai_ai_identify_score_reset";
/// 选择 AI 模型
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_AI_MODEL_SELECT = "onboard_ai_ai_model_select";
/// AI 跟随开关
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SET = "onboard_ai_drc_ai_spotlight_zoom_set";
/// AI 框选目标跟随
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_SELECT = "onboard_ai_drc_ai_spotlight_zoom_select";
/// AI 框选目标跟随确认
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_CONFIRM = "onboard_ai_drc_ai_spotlight_zoom_confirm";
/// 停止 AI 目标跟随
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_STOP = "onboard_ai_drc_ai_spotlight_zoom_stop";
/// AI 识别目标跟随
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_SPOTLIGHT_ZOOM_TRACK = "onboard_ai_drc_ai_spotlight_zoom_track";
/// 开启 AI 主动告警
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_START = "onboard_ai_drc_ai_alarm_start";
/// 关闭 AI 主动告警
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_ALARM_STOP = "onboard_ai_drc_ai_alarm_stop";
/// 配置 AI 模型
inline constexpr std::string_view kBeaconReportEvent_ONBOARD_AI_DRC_AI_MODELS_CONFIG = "onboard_ai_drc_ai_models_config";

// ── 控制权 (IControlAuthModule) ──
/// 抢夺飞行控制权
inline constexpr std::string_view kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_FLIGHT_CONTROL_AUTHORITY = "control_auth_acquire_flight_control_authority";
/// 释放飞行控制权
inline constexpr std::string_view kBeaconReportEvent_CONTROL_AUTH_RELEASE_FLIGHT_CONTROL_AUTHORITY = "control_auth_release_flight_control_authority";
/// 抢夺负载控制权
inline constexpr std::string_view kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_PAYLOAD_CONTROL_AUTHORITY = "control_auth_acquire_payload_control_authority";
/// 释放负载控制权
inline constexpr std::string_view kBeaconReportEvent_CONTROL_AUTH_RELEASE_PAYLOAD_CONTROL_AUTHORITY = "control_auth_release_payload_control_authority";

// ── DRC 链路 (IDrcModule) ──
/// 连接 DRC 链路
inline constexpr std::string_view kBeaconReportEvent_DRC_CONNECT_LINK = "drc_connect_link";
/// 断开 DRC 链路
inline constexpr std::string_view kBeaconReportEvent_DRC_DISCONNECT_LINK = "drc_disconnect_link";
/// RTC 网络质量测试
inline constexpr std::string_view kBeaconReportEvent_DRC_RTC_QUALITY_TEST = "drc_rtc_quality_test";

// ── 拉流 (IPullStreamModule) ──
/// 开始拉流
inline constexpr std::string_view kBeaconReportEvent_PULL_STREAM_START = "pull_stream_start";

}  // namespace flight_hub_sdk
