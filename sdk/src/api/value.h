// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>

namespace flight_hub_sdk {

/// SDK 客户端错误码。通过 `FlightHubError::error_code` 返回，表示 SDK 本地校验或执行阶段的错误，
/// 与服务端错误码（`FlightHubError::server_code`）独立。
enum class FlightHubSdkErrorCode {
    SUCCESS = 0,                    ///< 成功
    ERROR_INIT_FAILED = -1,         ///< 初始化失败。如直播通道建立失败、拉流初始化失败
    ERROR_ALREADY_INITIALIZED = -2, ///< 重复初始化。SDK 已 initialize，再次调用 initialize/setWorkspace/setUser 时返回
    ERROR_NOT_INITIALIZED = -3,     ///< 未初始化。SDK 尚未 initialize 即调用业务接口时返回
    ERROR_INVALID_PARAM = -4,       ///< 参数校验失败。必填参数为空、格式不合法等
    ERROR_OPERATION_FAILED = -5,    ///< 操作失败。HTTP 请求异常、DRC 指令执行异常等通用失败，具体原因见 server_code / server_message
    ERROR_INVALID_STATE = -6,       ///< 状态不允许。如载荷管理器未就绪、DRC 通道未建立时调用设备控制接口
    FILE_NOT_FOUND = -7,            ///< 文件未找到。上传航线/日志/解禁证书时本地文件不存在
    FILE_TOO_LARGE = -8,            ///< 文件超限。上传文件大小超过允许上限
    UNSUPPORTED_PROVIDER = -9,      ///< 不支持的存储提供商。上传文件时对象存储类型不在支持范围内
    UPLOAD_FAILED = -10,            ///< 上传失败。文件已找到但上传过程出错
    WAYLINE_ID_NOT_FOUND = -11,     ///< 航线 ID 未找到。HTTP 响应中未携带预期的航线 ID
    ERROR_DRC_LINK = -12,           ///< DRC 链路异常。DRC 通道不存在或数据传输失败，常见于指点飞行、设备控制、PSDK 控制等场景
    UNKNOWN = 0xFFFF,               ///< 未知错误
};

/// SDK 统一错误结构体。所有异步回调均通过此结构返回错误信息。
/// 错误来源分三层：SDK 本地校验（error_code）、服务端 HTTP 响应（http_code + server_code）、设备端指令结果（device_code）。
struct FlightHubError {
    FlightHubSdkErrorCode error_code = FlightHubSdkErrorCode::SUCCESS; ///< SDK 客户端错误码。SUCCESS 表示无 SDK 层错误
    int http_code = 0;      ///< HTTP 状态码。仅 HTTP 请求场景填充，如 200/400/401/500 等
    int server_code = 0;    ///< 服务端业务错误码。HTTP 响应 JSON 中的 code 字段，0 表示成功
    int device_code = 0;    ///< 设备端返回码。DRC 指令 等设备操控的结果码，0 表示成功
    std::string server_message; ///< 服务端错误描述。HTTP 响应 JSON 中的 message 字段

    std::string to_string() const {
        std::string msg;
        msg += "{";

        // 组装内容;
        msg += ("error_code: " + std::to_string(static_cast<int>(this->error_code)));
        msg += (", http_code: " + std::to_string(this->http_code));
        msg += (", server_code: " + std::to_string(this->server_code));
        msg += (", device_code: " + std::to_string(this->device_code));
        msg += (std::string(", server_message: ") + "\"" + this->server_message + "\"");

        msg += "}";

        return msg;
    }


    static FlightHubError success() { return {}; }

    static FlightHubError from_sdk(FlightHubSdkErrorCode c) {
        FlightHubError e; e.error_code = c; return e;
    }

    static FlightHubError from_http(int http_code, int server_code = 0,
                                    const std::string& msg = "") {
        FlightHubError e;
        // server_code==0 且 http_code 在 2xx 范围视为成功
        e.error_code = (server_code == 0 && http_code >= 200 && http_code < 300)
            ? FlightHubSdkErrorCode::SUCCESS
            : FlightHubSdkErrorCode::ERROR_OPERATION_FAILED;
        e.http_code = http_code;
        e.server_code = server_code;
        e.server_message = msg;
        return e;
    }

    static FlightHubError from_server(int server_code, const std::string& msg) {
        FlightHubError e;
        e.error_code = FlightHubSdkErrorCode::ERROR_OPERATION_FAILED;
        e.server_code = server_code;
        e.server_message = msg;
        return e;
    }

    static std::string me_error_to_string(int me_error_code) {
        switch (me_error_code) {
            case 0: return "";
            case -301: return "drc_timeout";
            case -201: return "transport_not_found";
            case -202: return "transport_error";
            case -401: return "connect_error";
            default: return "me_error:" + std::to_string(me_error_code);
        }
    }

    static FlightHubError from_drc(int me_error_code, int device_result = 0) {
        FlightHubError e;
        e.error_code = (me_error_code == 0 && device_result == 0)
            ? FlightHubSdkErrorCode::SUCCESS
            : FlightHubSdkErrorCode::ERROR_OPERATION_FAILED;
        e.device_code = device_result;
        e.server_message = me_error_to_string(me_error_code);
        return e;
    }
};

/// 无人机链路状态。对外 `LinkStatus` 中 `ctrl_link_status` / `data_link_status` / `live_link_status` 统一以 `int` 传递，
/// 数值须与本枚举底层整型一致；需要强类型时在业务侧 `static_cast<DroneLinkState>`。
enum class DroneLinkState {
    DroneLinkState_IDLE = 0,           ///< 空闲
    DroneLinkState_CONNECTING = 1,     ///< 连接中
    DroneLinkState_CONNECTED = 2,      ///< 已连接
    DroneLinkState_DISCONNECTED = 3,   ///< 已断开
    DroneLinkState_UNKNOWN = 0xFFFF,   ///< 未知
};

/// 飞行器模式码。对外 `AircraftModeCode::mode_code` 以 `int` 传递，数值须与本枚举一致。
enum class DroneModeCode {
    DroneModeCode_Standby = 0,                  ///< 待机
    DroneModeCode_TakeoffPreparation = 1,       ///< 起飞准备
    DroneModeCode_ReadyToTakeOff = 2,           ///< 起飞准备完毕
    DroneModeCode_ManualFlight = 3,             ///< 手动飞行
    DroneModeCode_AutomaticTakeoff = 4,         ///< 自动起飞
    DroneModeCode_RouteFlight = 5,              ///< 航线飞行
    DroneModeCode_PanoramicPhoto = 6,           ///< 全景拍照
    DroneModeCode_SmartFollow = 7,              ///< 智能跟随
    DroneModeCode_ADSBAvoidance = 8,            ///< ADS-B 躲避
    DroneModeCode_ReturnHomeAutomatically = 9,  ///< 自动返航
    DroneModeCode_AutomaticLanding = 10,        ///< 自动降落
    DroneModeCode_ForcedLanding = 11,           ///< 强制降落
    DroneModeCode_LandingWithThreeBlades = 12,  ///< 三桨叶降落
    DroneModeCode_Upgrading = 13,               ///< 升级中
    DroneModeCode_NotConnected = 14,            ///< 未连接
    DroneModeCode_APAS = 15,                    ///< 高级手动飞行（智能绕行避障）
    DroneModeCode_Joystick = 16,                ///< 虚拟摇杆模式（前端显示为手动飞行）
    DroneModeCode_CommandFlight = 17,           ///< 指点飞行
    DroneModeCode_RTKConverging = 18,           ///< RTK 收敛中
    DroneModeCode_ExplorationAndSiting = 19,    ///< 机场选址中
    DroneModeCode_POI = 20,                     ///< POI 环绕
    DroneModeCode_DeparturePoint = 21,          ///< 离场航线
    DroneModeCode_RelaySiting = 22,             ///< 中继选址中
    DroneModeCode_UNKNOWN = 65535,              ///< 状态异常
};

/// 相机模式。对外 `CameraState::camera_mode`、`SwitchCameraModeParams::camera_mode` 等以 `int` 传递，数值须与本枚举一致。
enum class CameraMode {
    CameraMode_PHOTO = 0,          ///< 拍照模式
    CameraMode_VIDEO = 1,          ///< 录像模式
    CameraMode_SMART_PHOTO = 2,    ///< 智能低光拍照
    CameraMode_PANO_PHOTO = 3,     ///< 全景拍照
    CameraMode_INTERVAL_PHOTO = 4, ///< 定时拍照
    CameraMode_UNKNOWN = 0xFFFF,   ///< 未知
};

/// 拍照文件格式。`CameraState::photo_format` 以 `int` 传递，数值须与本枚举一致。
enum class PhotoFormat {
    PhotoFormat_RAW = 0,           ///< RAW 格式
    PhotoFormat_JPEG = 1,          ///< JPEG 格式
    PhotoFormat_DLT664 = 16,       ///< DLT664 红外图像格式（R-JPEG，含红外热成像数据）
    PhotoFormat_UNKNOWN = 0xFFFF,  ///< 未知
};

/// 相机曝光模式。载荷镜头状态中 `wide_exposure_mode` / `zoom_exposure_mode` 等以 `int` 传递，数值须与本枚举一致。
enum class CameraExposureMode {
    CameraExposureMode_PROGRAM = 1,           ///< 自动曝光
    CameraExposureMode_SHUTTER_PRIORITY = 2,  ///< 快门优先
    CameraExposureMode_APERTURE_PRIORITY = 3, ///< 光圈优先
    CameraExposureMode_MANUAL = 4,            ///< 手动曝光
    CameraExposureMode_UNKNOWN = 0xFFFF,      ///< 未知
};

/// 对焦模式。`wide_focus_mode` / `zoom_focus_mode` 以 `int` 传递，数值须与本枚举一致。
enum class CameraFocusMode {
    CameraFocusMode_MANUAL = 0,    ///< MF 手动对焦
    CameraFocusMode_AF = 1,        ///< AF 单次自动对焦
    CameraFocusMode_AFC = 2,       ///< AFC 连续自动对焦
    CameraFocusMode_FINE_TUNE = 3, ///< MF 微调（配合 mf_value 使用）
    CameraFocusMode_UNKNOWN = 0xFFFF, ///< 未知
};

/// 对焦状态。`CameraWideLensState` / `CameraZoomLensState` 中 `wide_focus_state` / `zoom_focus_state` 以 `int` 传递，数值须与本枚举一致。
enum class CameraFocusState {
    CameraFocusState_IDLE = 0,       ///< 空闲
    CameraFocusState_FOCUSING = 1,   ///< 对焦中
    CameraFocusState_SUCCESSFUL = 2, ///< 对焦成功
    CameraFocusState_FAILED = 3,     ///< 对焦失败
    CameraFocusState_UNKNOWN = 0xFFFF, ///< 未知
};

/// 快门速度。`CameraWideLensState` / `CameraZoomLensState` 中 `wide_shutter_speed` / `zoom_shutter_speed` 以 `int` 传递，数值须与本枚举一致。
/// 命名规则：`1_BY_N` 表示 1/N 秒，`DOT` 表示小数点（如 `1DOT3` = 1.3秒），无 `BY` 的整数表示 N 秒。
enum class CameraShutterSpeed {
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_8000 = 0,   ///< 1/8000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_6400 = 1,   ///< 1/6400s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_6000 = 2,   ///< 1/6000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_5000 = 3,   ///< 1/5000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_4000 = 4,   ///< 1/4000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_3200 = 5,   ///< 1/3200s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_3000 = 6,   ///< 1/3000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_2500 = 7,   ///< 1/2500s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_2000 = 8,   ///< 1/2000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_1600 = 9,   ///< 1/1600s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_1500 = 10,  ///< 1/1500s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_1250 = 11,  ///< 1/1250s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_1000 = 12,  ///< 1/1000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_800 = 13,   ///< 1/800s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_725 = 14,   ///< 1/725s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_640 = 15,   ///< 1/640s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_500 = 16,   ///< 1/500s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_400 = 17,   ///< 1/400s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_350 = 18,   ///< 1/350s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_320 = 19,   ///< 1/320s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_250 = 20,   ///< 1/250s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_240 = 21,   ///< 1/240s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_200 = 22,   ///< 1/200s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_180 = 23,   ///< 1/180s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_160 = 24,   ///< 1/160s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_125 = 25,   ///< 1/125s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_120 = 26,   ///< 1/120s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_100 = 27,   ///< 1/100s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_90 = 28,    ///< 1/90s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_80 = 29,    ///< 1/80s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_60 = 30,    ///< 1/60s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_50 = 31,    ///< 1/50s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_40 = 32,    ///< 1/40s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_30 = 33,    ///< 1/30s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_25 = 34,    ///< 1/25s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_20 = 35,    ///< 1/20s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_15 = 36,    ///< 1/15s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_12DOT5 = 37, ///< 1/12.5s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_10 = 38,    ///< 1/10s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_8 = 39,     ///< 1/8s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_6DOT25 = 40, ///< 1/6.25s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_5 = 41,     ///< 1/5s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_4 = 42,     ///< 1/4s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_3 = 43,     ///< 1/3s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_2DOT5 = 44, ///< 1/2.5s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_2 = 45,     ///< 1/2s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_1DOT67 = 46, ///< 1/1.67s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_1DOT25 = 47, ///< 1/1.25s
    CameraShutterSpeed_SHUTTER_SPEED_1 = 48,           ///< 1s
    CameraShutterSpeed_SHUTTER_SPEED_1DOT3 = 49,      ///< 1.3s
    CameraShutterSpeed_SHUTTER_SPEED_1DOT6 = 50,      ///< 1.6s
    CameraShutterSpeed_SHUTTER_SPEED_2 = 51,           ///< 2s
    CameraShutterSpeed_SHUTTER_SPEED_2DOT5 = 52,      ///< 2.5s
    CameraShutterSpeed_SHUTTER_SPEED_3 = 53,           ///< 3s
    CameraShutterSpeed_SHUTTER_SPEED_3DOT2 = 54,      ///< 3.2s
    CameraShutterSpeed_SHUTTER_SPEED_4 = 55,           ///< 4s
    CameraShutterSpeed_SHUTTER_SPEED_5 = 56,           ///< 5s
    CameraShutterSpeed_SHUTTER_SPEED_6 = 57,           ///< 6s
    CameraShutterSpeed_SHUTTER_SPEED_7 = 58,           ///< 7s
    CameraShutterSpeed_SHUTTER_SPEED_8 = 59,           ///< 8s
    CameraShutterSpeed_SHUTTER_SPEED_9 = 60,           ///< 9s
    CameraShutterSpeed_SHUTTER_SPEED_10 = 61,          ///< 10s
    CameraShutterSpeed_SHUTTER_SPEED_11 = 62,          ///< 11s
    CameraShutterSpeed_SHUTTER_SPEED_13 = 63,          ///< 13s
    CameraShutterSpeed_SHUTTER_SPEED_15 = 64,          ///< 15s
    CameraShutterSpeed_SHUTTER_SPEED_16 = 65,          ///< 16s
    CameraShutterSpeed_SHUTTER_SPEED_20 = 66,          ///< 20s
    CameraShutterSpeed_SHUTTER_SPEED_23 = 67,          ///< 23s
    CameraShutterSpeed_SHUTTER_SPEED_25 = 68,          ///< 25s
    CameraShutterSpeed_SHUTTER_SPEED_30 = 69,          ///< 30s
    CameraShutterSpeed_SHUTTER_SPEED_40 = 70,          ///< 40s
    CameraShutterSpeed_SHUTTER_SPEED_50 = 71,          ///< 50s
    CameraShutterSpeed_SHUTTER_SPEED_60 = 72,          ///< 60s
    CameraShutterSpeed_SHUTTER_SPEED_80 = 73,          ///< 80s
    CameraShutterSpeed_SHUTTER_SPEED_100 = 74,         ///< 100s
    CameraShutterSpeed_SHUTTER_SPEED_120 = 75,         ///< 120s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_20000 = 76,  ///< 1/20000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_16000 = 77,  ///< 1/16000s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_12800 = 78,  ///< 1/12800s
    CameraShutterSpeed_SHUTTER_SPEED_1_BY_10000 = 79,  ///< 1/10000s
    CameraShutterSpeed_SHUTTER_SPEED_AUTO = 65534,     ///< 自动快门
    CameraShutterSpeed_UNKNOWN = 0xFFFF,               ///< 未知
};

/// 曝光补偿 EV。`CameraWideLensState` / `CameraZoomLensState` 中 `wide_exposure_value` / `zoom_exposure_value` 以 `int` 传递。
/// 命名规则：`NEG` 表示负补偿，`POS` 表示正补偿，`P` 表示小数点（如 `NEG_2P3EV` = -2.3EV）。
enum class CameraExposureCompensation {
    CameraExposureCompensation_NEG_5P0EV = 1,  ///< -5.0EV
    CameraExposureCompensation_NEG_4P7EV = 2,  ///< -4.7EV
    CameraExposureCompensation_NEG_4P3EV = 3,  ///< -4.3EV
    CameraExposureCompensation_NEG_4P0EV = 4,  ///< -4.0EV
    CameraExposureCompensation_NEG_3P7EV = 5,  ///< -3.7EV
    CameraExposureCompensation_NEG_3P3EV = 6,  ///< -3.3EV
    CameraExposureCompensation_NEG_3P0EV = 7,  ///< -3.0EV
    CameraExposureCompensation_NEG_2P7EV = 8,  ///< -2.7EV
    CameraExposureCompensation_NEG_2P3EV = 9,  ///< -2.3EV
    CameraExposureCompensation_NEG_2P0EV = 10, ///< -2.0EV
    CameraExposureCompensation_NEG_1P7EV = 11, ///< -1.7EV
    CameraExposureCompensation_NEG_1P3EV = 12, ///< -1.3EV
    CameraExposureCompensation_NEG_1P0EV = 13, ///< -1.0EV
    CameraExposureCompensation_NEG_0P7EV = 14, ///< -0.7EV
    CameraExposureCompensation_NEG_0P3EV = 15, ///< -0.3EV
    CameraExposureCompensation_NEG_0EV = 16,   ///< 0EV（无补偿）
    CameraExposureCompensation_POS_0P3EV = 17, ///< +0.3EV
    CameraExposureCompensation_POS_0P7EV = 18, ///< +0.7EV
    CameraExposureCompensation_POS_1P0EV = 19, ///< +1.0EV
    CameraExposureCompensation_POS_1P3EV = 20, ///< +1.3EV
    CameraExposureCompensation_POS_1P7EV = 21, ///< +1.7EV
    CameraExposureCompensation_POS_2P0EV = 22, ///< +2.0EV
    CameraExposureCompensation_POS_2P3EV = 23, ///< +2.3EV
    CameraExposureCompensation_POS_2P7EV = 24, ///< +2.7EV
    CameraExposureCompensation_POS_3P0EV = 25, ///< +3.0EV
    CameraExposureCompensation_POS_3P3EV = 26, ///< +3.3EV
    CameraExposureCompensation_POS_3P7EV = 27, ///< +3.7EV
    CameraExposureCompensation_POS_4P0EV = 28, ///< +4.0EV
    CameraExposureCompensation_POS_4P3EV = 29, ///< +4.3EV
    CameraExposureCompensation_POS_4P7EV = 30, ///< +4.7EV
    CameraExposureCompensation_POS_5P0EV = 31, ///< +5.0EV
    CameraExposureCompensation_FIXED = 255,    ///< 固定（不可调）
    CameraExposureCompensation_UNKNOWN = 0xFFFF, ///< 未知
};


/// ISO。`CameraWideLensState` / `CameraZoomLensState` 中 `wide_iso` / `zoom_iso` 以 `int` 传递，数值须与本枚举一致。
enum class CameraISO {
    CameraISO_ISO_AUTO = 0,     ///< 自动
    CameraISO_ISO_50 = 2,       ///< ISO 50
    CameraISO_ISO_100 = 3,      ///< ISO 100
    CameraISO_ISO_200 = 4,      ///< ISO 200
    CameraISO_ISO_400 = 5,      ///< ISO 400
    CameraISO_ISO_800 = 6,      ///< ISO 800
    CameraISO_ISO_1600 = 7,     ///< ISO 1600
    CameraISO_ISO_3200 = 8,     ///< ISO 3200
    CameraISO_ISO_6400 = 9,     ///< ISO 6400
    CameraISO_ISO_12800 = 10,   ///< ISO 12800
    CameraISO_ISO_25600 = 11,   ///< ISO 25600
    CameraISO_ISO_51200 = 12,   ///< ISO 51200
    CameraISO_ISO_102400 = 13,  ///< ISO 102400
    CameraISO_ISO_19200 = 14,   ///< ISO 19200
    CameraISO_ISO_32000 = 15,   ///< ISO 32000
    CameraISO_ISO_9600 = 16,    ///< ISO 9600
    CameraISO_ISO_16000 = 17,   ///< ISO 16000
    CameraISO_ISO_204800 = 18,  ///< ISO 204800
    CameraISO_ISO_409600 = 19,  ///< ISO 409600
    CameraISO_ISO_819200 = 20,  ///< ISO 819200
    CameraISO_ISO_FIXED = 255,  ///< 固定（不可调）
    CameraISO_UNKNOWN = 0xFFFF, ///< 未知
};


/// 光圈。`CameraWideLensState` / `CameraZoomLensState` 中 `wide_aperture_value` / `zoom_aperture_value` 以 `int` 传递，数值须与本枚举一致。
/// 命名规则：`DOT` 表示小数点（如 `F2DOT8` = f/2.8），枚举整型值 = 光圈数 × 100。
enum class CameraAperture {
    CameraAperture_F_AUTO = 0,     ///< 自动光圈
    CameraAperture_F1DOT6 = 160,   ///< f/1.6
    CameraAperture_F1DOT7 = 170,   ///< f/1.7
    CameraAperture_F1DOT8 = 180,   ///< f/1.8
    CameraAperture_F2 = 200,       ///< f/2
    CameraAperture_F2DOT2 = 220,   ///< f/2.2
    CameraAperture_F2DOT4 = 240,   ///< f/2.4
    CameraAperture_F2DOT5 = 250,   ///< f/2.5
    CameraAperture_F2DOT8 = 280,   ///< f/2.8
    CameraAperture_F3DOT2 = 320,   ///< f/3.2
    CameraAperture_F3DOT4 = 340,   ///< f/3.4
    CameraAperture_F3DOT5 = 350,   ///< f/3.5
    CameraAperture_F4 = 400,       ///< f/4
    CameraAperture_F4DOT4 = 440,   ///< f/4.4
    CameraAperture_F4DOT5 = 450,   ///< f/4.5
    CameraAperture_F4DOT8 = 480,   ///< f/4.8
    CameraAperture_F5 = 500,       ///< f/5
    CameraAperture_F5DOT6 = 560,   ///< f/5.6
    CameraAperture_F6DOT3 = 630,   ///< f/6.3
    CameraAperture_F6DOT8 = 680,   ///< f/6.8
    CameraAperture_F7DOT1 = 710,   ///< f/7.1
    CameraAperture_F8 = 800,       ///< f/8
    CameraAperture_F9 = 900,       ///< f/9
    CameraAperture_F9DOT5 = 950,   ///< f/9.5
    CameraAperture_F9DOT6 = 960,   ///< f/9.6
    CameraAperture_F10 = 1000,     ///< f/10
    CameraAperture_F11 = 1100,     ///< f/11
    CameraAperture_F13 = 1300,     ///< f/13
    CameraAperture_F14 = 1400,     ///< f/14
    CameraAperture_F16 = 1600,     ///< f/16
    CameraAperture_F18 = 1800,     ///< f/18
    CameraAperture_F19 = 1900,     ///< f/19
    CameraAperture_F20 = 2000,     ///< f/20
    CameraAperture_F22 = 2200,     ///< f/22
    CameraAperture_F27 = 2700,     ///< f/27
    CameraAperture_F32 = 3200,     ///< f/32
    CameraAperture_UNKNOWN = 0xFFFF, ///< 未知
};

/// 激光测距状态。`CameraMeasureTarget::laser_measure_state` 以 `int` 传递，数值须与本枚举一致。
enum class CameraLaserMeasureState {
    CameraLaserMeasureState_NORMAL = 0,      ///< 正常测距
    CameraLaserMeasureState_TOO_CLOSE = 1,   ///< 目标过近
    CameraLaserMeasureState_TOO_FAR = 2,     ///< 目标过远
    CameraLaserMeasureState_NO_SIGNAL = 3,   ///< 无信号
    CameraLaserMeasureState_UNKNOWN = 0xFFFF, ///< 未知
};

/// 镜头类型。`LiveInfo` / `LiveVideoInfo` 及切换镜头相关参数中的镜头类型以 `int` 传递，数值须与本枚举一致。
enum class CameraLensType {
    CameraLensType_NORMAL = 0,     ///< 默认镜头
    CameraLensType_WIDE = 1,       ///< 广角镜头
    CameraLensType_ZOOM = 2,       ///< 变焦镜头
    CameraLensType_IR = 3,         ///< 红外镜头
    CameraLensType_THERMAL = 4,    ///< 热成像镜头
    CameraLensType_UNKNOWN = 0xFFFF, ///< 未知
};

/// 红外增益模式。`ThermalGainModeInfo` / `ThermalGainModeSetParams::thermal_gain_mode` 以 `int` 传递，数值须与本枚举一致。
enum class IrGainMode {
    Auto = 0,      ///< 自动
    LowGain = 1,   ///< 低增益
    HighGain = 2,  ///< 高增益
    UltraHD = 3,   ///< 超清模式
    UNKNOWN = 0xFFFF, ///< 未知
};

/// 全景执行阶段。`PanoramaStatus::current_step` 以 `int` 传递，数值须与本枚举一致。
enum class PanoramaRunningState {
    PanoramaRunningState_IDLE = 0,          ///< 空闲
    PanoramaRunningState_RUNNING = 1,       ///< 拍摄中
    PanoramaRunningState_SYNTHESIZING = 2,  ///< 合成中
    PanoramaRunningState_FINISHED = 3,      ///< 已完成
    PanoramaRunningState_UNKNOWN = 0xFFFF   ///< 未知
};

/// 拍照结果状态。`PanoramaStatus::status` 以 `int` 传递，数值须与本枚举一致。
enum class CameraPhotoState {
    CameraPhotoState_IN_PROGRESS = 0, ///< 拍摄中
    CameraPhotoState_OK = 1,          ///< 拍摄成功
    CameraPhotoState_FAILED = 2,      ///< 拍摄失败
    CameraPhotoState_CANCELED = 3,    ///< 已取消
    CameraPhotoState_UNKNOWN = 0xFFFF, ///< 未知
};

/// 探照灯工作模式。`SearchLightWorkModeSetParams::work_mode` 以 `int` 传递，数值须与本枚举一致。
enum class SearchLightWorkMode {
    CLOSE = 0,              ///< 关闭
    OPEN = 1,               ///< 常亮
    STROBE = 2,             ///< 爆闪
    QUICK_STROBE = 3,       ///< 快速爆闪
    ALTERNATION_STROBE = 4, ///< 交替爆闪
    UNKNOWN = 0xFFFF,       ///< 未知
};

/// 喊话器 TTS 语音类型。
enum class MegaphoneTTSVoiceType {
    MAN = 0,         ///< 男声
    WOMAN = 1,       ///< 女声
    UNKNOWN = 0xFFFF, ///< 未知
};

/// 喊话器 TTS 语言类型。
enum class MegaphoneTTSLanguageType {
    CHINESE = 0,     ///< 中文
    ENGLISH = 1,     ///< 英文
    UNKNOWN = 0xFFFF, ///< 未知
};

/// 喊话器工作模式。
enum class MegaphoneWorkMode {
    TTS = 0,                    ///< TTS 模式
    VOICE = 1,                  ///< 实时喊话模式（微信语音模式）
    TTS_APP = 2,                ///< TTS 模式（APP）
    LIVE_VOICE = 3,             ///< 对讲机模式（暂不支持）
    REAL_TIME = 4,              ///< 实时喊话模式（边喊边播）
    PLAYING_IN_TRANSMISSION = 5, ///< 边传边播模式
    UNKNOWN = 0xFFFF,           ///< 未知
};

/// 喊话器播放模式。
enum class MegaphonePlayMode {
    SINGLE_PLAY = 0,   ///< 单次播放
    LOOP_PLAYBACK = 1, ///< 循环播放（单曲）
    UNKNOWN = 0xFFFF,  ///< 未知
};

/// PSDK 喊话器系统状态。
enum class PSDKMegaphoneSystemState {
    IDLE = 0,                     ///< 空闲中
    IN_TRANSMISSION = 1,          ///< 传输中
    PLAYING = 2,                  ///< 播放中
    IN_EXCEPTION = 3,             ///< 异常中
    TTS_IN_CONVERSION = 4,        ///< 文本转换中（仅用于 TTS）
    PLAYING_IN_TRANSMISSION = 5,  ///< 边传边播中
    DOWNLOADING = 99,             ///< 下载中（云端到机场）
    UNKNOWN = 0xFFFF,             ///< 未知
};

/// PSDK 设备类型。
enum class PsdkType {
    UNOFFICIAL = 4,  ///< 第三方设备
    OFFICIAL = 5,    ///< 官方设备
    MANIFOLD3 = 8,   ///< 妙算 3
    UNKNOWN = 0xFFFF, ///< 未知
};

/// PSDK 设备子类型。
enum class PsdkSubType {
    SPEAKER = 0,      ///< 喊话器 AS1
    SEARCHLIGHT = 1,  ///< 探照灯 AL1
    UNKNOWN = 0xFFFF, ///< 未知
};

/// 失控动作。
enum class OutOfControlAction {
    HOVER = 0,        ///< 悬停
    RETURN_HOME = 1,  ///< 返航
    CONTINUE = 2,     ///< 继续
    UNKNOWN = 0xFFFF, ///< 未知
};

/// 起飞模式。
enum class TakeoffMode {
    TAKEOFF = 0,              ///< 起飞
    TAKEOFF_WITH_FLY_TO = 1,  ///< 起飞并飞向目标点
    UNKNOWN = 0xFFFF,         ///< 未知
};

/// 机场空调状态。
enum class DockAirConditionerState {
    DockAirConditionerState_IDLE = 0,                       ///< 空闲模式（无制冷、制热、除湿等）
    DockAirConditionerState_COOLING = 1,                    ///< 制冷模式
    DockAirConditionerState_HEATING = 2,                    ///< 制热模式
    DockAirConditionerState_DEHUMIDIFYING = 3,              ///< 除湿模式
    DockAirConditionerState_COOLING_EXIT = 4,               ///< 制冷退出模式
    DockAirConditionerState_HEATING_EXIT = 5,               ///< 制热退出模式
    DockAirConditionerState_DEHUMIDIFYING_EXIT = 6,         ///< 除湿退出模式
    DockAirConditionerState_COOLING_PREPARATION = 7,        ///< 制冷准备模式
    DockAirConditionerState_HEATING_PREPARATION = 8,        ///< 制热准备模式
    DockAirConditionerState_DEHUMIDIFYING_PREPARATION = 9,  ///< 除湿准备模式

    DockAirConditionerState_INVALID = 0x7FFF,               ///< 获取空调状态失败（MCU 空调器件异常）
    DockAirConditionerState_UNKNOWN = 0xFFFF,               ///< 未知
};

/// 机场舱盖操作模式。
enum class DockCoverMode {
    DockCoverMode_OPEN = 0,         ///< 开启舱盖
    DockCoverMode_CLOSE = 1,        ///< 关闭舱盖
    DockCoverMode_FORCE_CLOSE = 2,  ///< 强制关闭舱盖
    DockCoverMode_UNKNOWN = 0xFFFF, ///< 未知
};

/// 设备种类（Dock / Drone / BaseStation）。module impl 构造注入；跨语言场景以 `int` 传递，数值须与本枚举一致。
enum class DeviceKind : int {
    Dock = 0,
    Drone = 1,
    BaseStation = 2,
};

/// 机场摄像头位置（舱内/舱外切换）。`IDock::changeDockCameraPosition` 等 API 以 `int` 传递时数值须与本枚举一致。
enum class DockCameraPosition : int {
    INSIDE = 0,       ///< 舱内 / 主摄像头
    OUTSIDE = 1,      ///< 舱外 / 辅助摄像头 1
    AUX2 = 2,         ///< 辅助摄像头 2
    UNKNOWN = 0xFFFF, ///< 未知
};

/// 直播录制状态（NCSDK topic_live_record_info 推送）。`LiveRecordStateInfo::state` 以 `int` 传递，数值须与本枚举一致。
enum class RecordState : int {
    RecordSuccess = 0,       ///< 录制成功
    Recording = 1,           ///< 录制中
    RecordInitializing = 2,  ///< 初始化中
    RecordFailed = 3,        ///< 录制失败
    RecordUnknown = 0xFFFF,  ///< 未知
};

/// 夜景模式。`SetNightSceneModeParams::mode`、`NightModeSettings::night_mode` 以 `int` 传递，数值须与本枚举一致。
enum class NightSceneMode {
    CLOSE = 0, ///< 关闭
    OPEN = 1,  ///< 开启
    AUTO = 2,  ///< 自动
};

/// SEI 数据项类型（SDK 内部定义）。`SeiItem::type` 以 `int` 传递，数值须与本枚举一致。
/// 用于直播流中嵌入辅助信息，每种类型对应 `SeiItem` 中不同的 union 字段。
enum class SeiDataType {
    CUSTOM_DATA = 0x01,        ///< 自定义数据（对应 SeiItem::custom_data）
    DETECTION_BOX = 0x02,      ///< 目标检测框（对应 SeiItem::detection_box）
    DETECTION_POLYGON = 0x03,  ///< 多边形检测区域（对应 SeiItem::detection_polygon）
    TEXT_OVERLAY = 0x04,       ///< 文本叠加（对应 SeiItem::text_overlay）
    UNKNOWN = 0xFFFF,          ///< 未知
};

/// 踏勘选址执行阶段。
enum class VisualExplorationStage{
    IDLE = 0,             ///< 空闲
    TAKE_OFF = 1,         ///< 起飞
    MANUAL = 2,           ///< 手动控制
    SCOUT_TRANSFER = 3,   ///< 转移至离场点
    SCOUT = 4,            ///< 离场点踏勘
    PATROL_TRANSFER = 5,  ///< 转移至方形航线起点
    PATROL = 6,           ///< 执行方形航线
    GO_HOME_TRANSFER = 7, ///< 转移至返航起点
    GO_HOME = 8,          ///< 返航
    GENERATE = 9,         ///< 生成进离场航线及离线地图数据中
    DOWNLOAD_FINISH = 10, ///< 进离场航线和离线地图下载成功
    UNKNOWN = 0xFFFF,     ///< 未知
};


/// 踏勘选址任务异常错误码（task_visual_scout_abnormal_info）。
enum class VisualExplorationErrorCode {
    CMD_SUCCESS = 0,                        ///< 命令操作成功（包含：开始、终止任务）
    UNKNOW_TASK_CMD = 1,                    ///< 未知指令
    APP_REGISTER_CALLBACK_ERROR = 2,        ///< 注册回调失败
    ABSTRACT_DATD_ERROR = 3,                ///< 抽象层数据异常
    DRONE_IS_NOT_IN_AIR = 4,                ///< 飞机不在空中
    PARAM_READ_ERROR = 5,                   ///< 参数读取异常
    EPHEMERIS_NOT_EXIT = 6,                 ///< 星历不存在
    WIND_SPD_BIG = 7,                       ///< 风速过大
    USER_REQUEST_PAUSE = 8,                 ///< 用户暂停任务
    USER_CHANGE_MODE = 9,                   ///< 用户切换档位
    RUNNING_AT_NIGHT = 10,                  ///< 环境光过暗，无法执行踏勘选址任务
    HEIGHT_LIMIT = 11,                      ///< 靠近限高
    BOUNDARY_LIMIT = 12,                    ///< 靠近禁飞区边界 30m
    OA_LIMIT = 13,                          ///< OA 避障
    REG_DBUS_EROOR = 14,                    ///< dbus 订阅异常
    RC_LOST_VE = 15,                        ///< 失控（EA230 开始新增离场点版本）
    START_FAIL_TASK_NUM_FULL = 16,          ///< 任务列表已满
    START_FAIL_FOR_TASK_IS_RUNNING = 17,    ///< 开始失败，当前任务正在执行
    START_FAIL_FOR_HEADING_LEVEL = 18,      ///< 开始失败，heading_level 没有收敛到 6
    START_FAIL_FOR_OSDK_MO_DULE_TYPE = 19,  ///< 开始失败，没有安装 RTK
    START_FAIL_FOR_GNSS_INIT_FAIL = 20,     ///< 开始失败，GNSS 初始化失败
    START_FAIL_FOR_GNSS_LEVEL_LOW = 21,     ///< 开始失败，GPS 信号弱
    START_FAIL_FOR_POS_INVALID = 22,        ///< 开始失败，飞机定位异常
    START_FAIL_FOR_MAP_INVALID = 23,        ///< 开始失败，地图异常
    STOP_FAIL_FOR_TASK_IS_NOT_RUNING = 32,  ///< 终止失败，当前任务没有执行
    STOP_FAIL_FOR_UNKNOW_TASK_CMD = 33,     ///< 终止失败，未知指令 / ID 号不匹配
    STOP_SKY_PARSING_SUCCESS = 34,          ///< 终止天空分析或搜星任务成功
    STOP_GNSS_ACCESS_SUCCESS = 35,          ///< 终止搜星任务成功
    STOP_GNSS_ACCESS_FAIL = 36,             ///< 终止搜星任务失败
    EPHEMERIS_UPLOAD_SUCCESS = 64,          ///< 踏勘任务获取星历成功
    EPHEMERIS_GET_FAILED = 65,              ///< 踏勘任务获取星历失败
    EPHEMERIS_UNZIP_FAILED = 66,            ///< 踏勘任务解压星历失败
    ROTATE_TIMEOUT = 80,                    ///< 旋转超时
    ACCLUSION_HIGH = 81,                    ///< 四周遮挡严重
    GET_SCALE_TIMEOUT = 82,                 ///< 获取影响因子超时
    FOUND_OBSTACLE = 83,                    ///< 发现障碍物
    PARSING_FAIL = 84,                      ///< 获取天空图失败
    GET_SCALE_ERROR = 85,                   ///< 获取星轨结果失败
    TOP_TASK_FOR_GNSS_FAILE = 98,           ///< 搜星失败导致任务退出
    GNSS_DATA_UNAVAILABLE = 99,             ///< 选址无搜星数据
    SPP_LOW_POS_RATE = 100,                 ///< 单点定位可用率低
    SPP_LOW_POS_ACCURACY = 101,             ///< 单点定位内符合精度低
    SPP_POS_SAT_INSUFFICIENT = 102,         ///< 单点定位参与定位卫星少
    SPP_POS_HIGH_PDOP = 103,                ///< 单点定位 PDOP 高
    HEADING_LOW_FIXRATE = 104,              ///< RTK 定向固定率低
    HEADING_SWITCH_TO_FLOAT = 105,          ///< RTK 定向切浮点解次数多
    HEADING_LOW_ACCURACY = 112,             ///< RTK 定向精度低（包括航向和俯仰）
    HEADING_SAT_INSUFFICIENT = 113,         ///< RTK 定向参与定位卫星少
    RTK_POS_NO_VALID_DATA = 114,            ///< RTK 定位结果无可用数据
    RTK_POS_TOO_MANY_ERROR_PERIODS = 115,   ///< RTK 定位结果出现多类异常
    NO_EPHEMERIS = 116,                     ///< 无可用事后星历
    EPHEMERIS_NOT_VALID = 117,              ///< 星历文件无效
    OBS_SAT_SNR_LOW = 118,                  ///< 观测数据卫星 SNR 低
    OBS_SAT_FREQUENT_CYCLE_SLIP = 119,      ///< 观测数据周跳多
    OBS_SAT_NUM_INSUFFICIENT = 120,         ///< 观测数据卫星少
    OBS_MIN_SAT_NUM_LESS = 121,             ///< 时段内最少搜星少于 16 颗（开始测试 3 分钟后）
    RTK_POS_TYPE_INVALID = 122,             ///< RTK 单点定位不可用
    GNSS_INIT_ERROR = 123,                  ///< 搜星初始化异常
    GNSS_GET_RESULT_ERROR = 124,            ///< 搜星结果获取异常
    GNSS_TIMEOUT = 125,                     ///< 等待搜星结果超时
    TRANSFER_HEIGHT_ERROR = 128,            ///< 爬升高度参数出错
    TRANSFER_HEIGHT_LIMIT = 129,            ///< 爬升到最高高度（60m）找不到离场点
    TRANSFER_TIMEOUT = 130,                 ///< 爬升超时
    TRANSFER_GNSS_TIME_OUT = 131,           ///< 爬升等待 GNSS 搜星结果超时
    ASSESS_RESULT_UNAVAILABLE_VE = 153,     ///< 踏勘任务被内部或外部终止
    MISSION_SWITCH_ERROR_VE = 155,          ///< mission 异常切换
    DEPARTURE_FAIL_VE = 160,                ///< 离场航线执行失败
    DEPARTURE_INIT_FAIL_VE = 161,           ///< 离场航线初始化失败
    DEPARTURE_INVALID_VE = 162,             ///< 离场航线不可用
    DEPARTURE_TARGET_NOT_REACHABLE_VE = 163, ///< 离场点不可达（可能存在禁飞区或障碍物）
    DEPARTURE_PLANNING_FAIL_VE = 164,       ///< 离场航线规划失败
    DEPARTURE_POS_VERT_ERROR_VE = 165,      ///< 离场点垂直位置错误
    DEPARTURE_POS_HORIZ_ERROR_VE = 166,     ///< 离场点水平位置错误
    DEPARTURE_RC_MODE_ERROR_VE = 167,       ///< 非 P 档不能执行离场航线
    RADIUS_LIMIT_VE = 176,                  ///< 靠近限远
    VFENCE_LIMIT_VE = 177,                  ///< 靠近自定义飞行区
    HOME_NOT_SET_VE = 178,                  ///< Home 点未刷新
    LOW_LIMIT_VE = 179,                     ///< 限低
    ATTI_MODE_VE = 180,                     ///< 姿态模式
    UNKNOWN = 0xFFFF,                       ///< 未知
};


/// 星历下载/上传状态。
enum class Stage {
    IDLE = 0,                ///< 空闲
    DOWNLOADING = 1,         ///< 下载中
    DOWNLOAD_SUCCESSED = 2,  ///< 下载成功
    DOWNLOAD_FAILED = 3,     ///< 下载失败
    UPLOADING = 4,           ///< 上传中
    UPLOAD_SUCCESSED = 5,    ///< 上传成功
    UPLOAD_FAILED = 6,       ///< 上传失败
    UNKNOWN = 0xFFFF,        ///< 未知
};

/// POI 任务状态。HTTP 层 `POITaskResponse::status` 与对外 `POITaskInfo::status` 统一以 `int` 传递，
/// 数值须与本枚举底层整型一致；需要强类型时在业务侧 `static_cast<POITaskStatus>`。
enum class POITaskStatus {
    Waiting = 0,           ///< 待开始
    StartingFailure = 1,   ///< 启动失败
    Executing = 2,         ///< 执行中（飞行）
    Paused = 3,            ///< 已暂停
    Terminated = 4,        ///< 终止
    Success = 5,           ///< 成功
    Suspended = 6,         ///< 挂起
    Timeout = 7,           ///< 超时
    PartiallyDone = 8,     ///< 部分执行状态
    Preparing = 9,         ///< 准备中
    QueueForTakeoff = 10,  ///< 起飞排队中
    UNKNOWN = 0xFFFF,      ///< 未知
};

/// AI 识别模式。
enum class AIIdentifyMode{
	DISABLE = 0,          ///< 关闭识别
	VISIBLE_LIGHT = 1,    ///< 可见光识别
	INFRARED_LIGHT = 2,   ///< 红外光识别
	UNKNOWN = 0xFFFF,     ///< 未知
};

/// AI 识别评分模式。
enum class AIIdentifyScoreMode{
	MODE_INVALID = 0,     ///< 无效值
	MODE_COUNT = 1,       ///< 计数模式
	MODE_RESCUE = 2,      ///< 搜救模式
	MODE_USER = 3,        ///< 用户自定义模式
	UNKNOWN = 0xFFFF,     ///< 未知
};

/// 媒体文件类型（后端 file_type 枚举，多接口共用）。
/// 不同接口实际返回的取值范围不同，具体参见各接口字段注释。
enum class MediaType : int {
    NoType      = 0,  ///< 未定义类型
    Dir         = 1,  ///< 文件夹
    Image       = 2,  ///< 图片
    Video       = 3,  ///< 视频
    Model       = 4,  ///< 模型（未知/不支持格式的默认值）
    WayLine     = 5,  ///< 航线文件
    TaskWayline = 6,  ///< 任务航线
    Zip         = 7,  ///< 压缩包
    Model2D     = 8,  ///< 2D 模型
    Model3D     = 9,  ///< 3D 模型
    CustomCloud = 10, ///< 用户自定义云媒体
    ModelGatherDir = 11, ///< 后重建中间状态文件夹
    PPK         = 12, ///< PPK 文件
    XMP         = 20, ///< XMP 文件
    OpenModel   = 21, ///< 开放模型
    OpenMedia   = 22, ///< 开放媒体
};

} // namespace flight_hub_sdk
