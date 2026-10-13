// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <string>
#include <vector>

namespace flight_hub_sdk {

/// 相机控制模块接口，提供镜头切换、拍照/录像、云台控制、相机参数设置等功能。
class ICameraControlModule {
public:
    virtual ~ICameraControlModule() = default;

    /// @brief 切换直播镜头
    /// @param params 切换镜头参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_live_lens_change
    /// @endinternal
    virtual void drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置照片存储参数
    /// @param params 照片存储设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_photo_storage_set
    /// @endinternal
    virtual void drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置视频存储参数
    /// @param params 视频存储设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_video_storage_set
    /// @endinternal
    virtual void drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置夜景模式
    /// @param params 夜景模式参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_night_mode_set
    /// @endinternal
    virtual void drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 拍照
    /// @param payload_index 负载索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_take
    /// @endinternal
    virtual void drcTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 智能低光拍照
    /// @param payload_index 负载索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_take (smart mode)
    /// @endinternal
    virtual void drcSmartPhotoTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 开始录像
    /// @param payload_index 负载索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_recording_start
    /// @endinternal
    virtual void drcStartRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 停止录像
    /// @param payload_index 负载索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_recording_stop
    /// @endinternal
    virtual void drcStopRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 框选变焦
    /// @param params 框选变焦参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_frame_zoom
    /// @endinternal
    virtual void drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置焦距
    /// @param params 焦距参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_focal_length_set
    /// @endinternal
    virtual void drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置联动变焦
    /// @param params 联动变焦参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_linkage_zoom_set
    /// @endinternal
    virtual void drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    // ──────────────────────────────────────────────
    //  云台控制
    // ──────────────────────────────────────────────

    /// @brief 重置云台
    /// @param params 云台重置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_gimbal_reset
    /// @endinternal
    virtual void drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 看向目标
    /// @param params 看向目标参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: look_at_target)
    /// @endinternal
    virtual void lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 画面拖动控制云台转动速度
    /// @param params 云台拖拽参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_screen_drag
    /// @endinternal
    virtual void drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 开始全景拍摄
    /// @param params 全景参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_take (panorama mode; 与拍照/定时拍复用同一 method，靠 camera_mode 区分)
    /// @endinternal
    virtual void drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 停止全景拍摄
    /// @param payload_index 负载索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_stop (与 drcStopTakePhoto 复用同一 method)
    /// @endinternal
    virtual void drcStopPanorama(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅全景拍摄状态
    /// @param payload_index 负载索引
    /// @param callback 全景状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | DRC 全景状态回调（`drc_camera_photo_info_push` filter: `camera_mode = PANO_PHOTO`）
    /// @endinternal
    virtual int listenDrcPanoramaStatus(const std::string& payload_index, const std::function<void(const PanoramaStatus& status)>& callback) = 0;

    /// @brief 取消订阅全景拍摄状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPanoramaStatus(const std::string& payload_index, int listen_id) = 0;

    // ──────────────────────────────────────────────
    //  照片分辨率设置
    // ──────────────────────────────────────────────

    /// @brief 设置照片分辨率
    /// @param params 照片分辨率参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_size_set
    /// @endinternal
    virtual void drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置定时拍参数
    /// @param params 定时拍设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_interval_photo_set
    /// @endinternal
    virtual void drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 执行定时拍
    /// @param params 定时拍参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_take (interval mode; 与拍照/全景复用同一 method)
    /// @endinternal
    virtual void drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅定时拍状态
    /// @param payload_index 负载索引
    /// @param callback 定时拍状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | DRC 定时拍状态回调（`drc_camera_photo_info_push` filter: `camera_mode = INTERVAL_PHOTO`）
    /// @endinternal
    virtual int listenDrcIntervalTakePhotoStatus(const std::string& payload_index, const std::function<void(const IntervalTakePhotoStatus& status)>& callback) = 0;

    /// @brief 取消订阅定时拍状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcIntervalTakePhotoStatus(const std::string& payload_index, int listen_id) = 0;

    // ──────────────────────────────────────────────
    //  停止拍照
    // ──────────────────────────────────────────────

    /// @brief 停止拍照
    /// @param payload_index 负载索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_stop
    /// @endinternal
    virtual void drcStopTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 双击画面瞄准目标
    /// @param params 瞄准参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_aim
    /// @endinternal
    virtual void drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 切换相机模式
    /// @param params 切换相机模式参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_mode_switch
    /// @endinternal
    virtual void drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅可用相机模式列表
    /// @param payload_index 负载索引
    /// @param callback 可用相机模式列表回调（用 std::vector<int> 替换 std::vector<CameraMode>，避免跨平台编译报错）
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | 硬编码可用相机模式列表（PHOTO / VIDEO / SMART_PHOTO / PANO_PHOTO 等），非 DRC push
    /// @endinternal
    virtual int listenDrcCameraModeAvailableRange(const std::string& payload_index, const std::function<void(const std::vector<int>&)>& callback) = 0;

    /// @brief 取消订阅可用相机模式列表
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraModeAvailableRange(const std::string& payload_index, int listen_id) = 0;

    // ──────────────────────────────────────────────
    //  相机/负载 OSD 状态
    // ──────────────────────────────────────────────

    /// @brief 订阅相机状态
    /// @param payload_index 负载索引
    /// @param callback 相机状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state)
    /// @endinternal
    virtual int listenDrcCameraState(const std::string& payload_index, const std::function<void(const CameraState& state)>& callback) = 0;

    /// @brief 取消订阅相机状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraState(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅实时视图世界区域
    /// @param payload_index 负载索引
    /// @param callback 实时视图区域回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: liveview.liveview_world_region)
    /// @endinternal
    virtual int listenDrcLiveviewWorldRegion(const std::string& payload_index, const std::function<void(const LiveviewWorldRegion& region)>& callback) = 0;

    /// @brief 取消订阅实时视图世界区域
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcLiveviewWorldRegion(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅广角镜头状态
    /// @param payload_index 负载索引
    /// @param callback 广角镜头状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: wide_lense, 含 wide_fov_info)
    /// @endinternal
    virtual int listenDrcCameraWideLensState(const std::string& payload_index, const std::function<void(const CameraWideLensState& state)>& callback) = 0;

    /// @brief 取消订阅广角镜头状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraWideLensState(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅变焦镜头状态
    /// @param payload_index 负载索引
    /// @param callback 变焦镜头状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: zoom_lense, 含 zoom_fov_info)
    /// @endinternal
    virtual int listenDrcCameraZoomLensState(const std::string& payload_index, const std::function<void(const CameraZoomLensState& state)>& callback) = 0;

    /// @brief 取消订阅变焦镜头状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraZoomLensState(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅测量目标数据
    /// @param payload_index 负载索引
    /// @param callback 测量目标回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: measure_target)
    /// @endinternal
    virtual int listenDrcCameraMeasureTarget(const std::string& payload_index, const std::function<void(const CameraMeasureTarget& target)>& callback) = 0;

    /// @brief 取消订阅测量目标数据
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraMeasureTarget(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅夜景模式设置
    /// @param payload_index 负载索引
    /// @param callback 夜景模式设置回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.night_mode_settings)
    /// @endinternal
    virtual int listenDrcNightModeSettings(const std::string& payload_index, const std::function<void(const NightModeSettings& settings)>& callback) = 0;

    /// @brief 取消订阅夜景模式设置
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcNightModeSettings(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅云台状态
    /// @param payload_index 负载索引
    /// @param callback 云台状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: gimbal_pitch / gimbal_roll / gimbal_yaw)
    /// @endinternal
    virtual int listenDrcGimbalState(const std::string& payload_index, const std::function<void(const GimbalState& state)>& callback) = 0;

    /// @brief 取消订阅云台状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcGimbalState(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅直播视频信息
    /// @param payload_index 负载索引
    /// @param callback 视频信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_live_video_info_push
    /// @endinternal
    virtual int listenDrcLiveVideoInfo(const std::string& payload_index, const std::function<void(const LiveVideoInfo& info)>& callback) = 0;

    /// @brief 取消订阅直播视频信息
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcLiveVideoInfo(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅红外增益模式
    /// @param payload_index 负载索引
    /// @param callback 红外增益模式回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: ir_lense.thermal_gain_mode; `listenDrcCameraIrLensState` 的子集，兼容保留)
    /// @endinternal
    virtual int listenDrcThermalGainMode(const std::string& payload_index, const std::function<void(const ThermalGainModeInfo& info)>& callback) = 0;

    /// @brief 取消订阅红外增益模式
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcThermalGainMode(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅红外镜头完整状态
    /// @param payload_index 负载索引
    /// @param callback 红外镜头状态回调（含 gain / palette / isotherm / zoom / split / FOV）
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: ir_lense)
    /// @endinternal
    virtual int listenDrcCameraIrLensState(const std::string& payload_index, const std::function<void(const CameraIrLensState& state)>& callback) = 0;

    /// @brief 取消订阅红外镜头完整状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraIrLensState(const std::string& payload_index, int listen_id) = 0;

    /// @brief 订阅红外测温上行推送（与 drcSetIrMeteringMode / drcSetIrMeteringPoint / drcSetIrMeteringArea 成对）
    /// @param payload_index 负载索引
    /// @param callback 红外测温回调（模式 / 点测温 / 区域测温含极值点）
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_osd_info_push (field: ir_thermometry)
    /// @endinternal
    virtual int listenDrcCameraIrThermometry(const std::string& payload_index, const std::function<void(const CameraIrThermometryInfo& info)>& callback) = 0;

    /// @brief 取消订阅红外测温上行推送
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCameraIrThermometry(const std::string& payload_index, int listen_id) = 0;

    // ── B4 drc_camera_state_push 拆分 listen ──
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.camera_watermark_settings)
    /// @endinternal
    virtual int listenDrcCameraWatermarkSettings(const std::string& payload_index, const std::function<void(const CameraWatermarkSettings&)>& callback) = 0;
    virtual void cancelListenDrcCameraWatermarkSettings(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.storage)
    /// @endinternal
    virtual int listenDrcCameraStorageInfo(const std::string& payload_index, const std::function<void(const CameraStorageInfo&)>& callback) = 0;
    virtual void cancelListenDrcCameraStorageInfo(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.ir_pip_state)
    /// @endinternal
    virtual int listenDrcCameraIrPipState(const std::string& payload_index, const std::function<void(const CameraIrPipState&)>& callback) = 0;
    virtual void cancelListenDrcCameraIrPipState(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.isotherm_rendering_polygon)
    /// @endinternal
    virtual int listenDrcCameraIsothermPolygon(const std::string& payload_index, const std::function<void(const CameraIsothermPolygon&)>& callback) = 0;
    virtual void cancelListenDrcCameraIsothermPolygon(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: media_storage)
    /// @endinternal
    virtual int listenDrcCameraMediaStorage(const std::string& payload_index, const std::function<void(const CameraMediaStorage&)>& callback) = 0;
    virtual void cancelListenDrcCameraMediaStorage(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: algorithm_dehaze_info)
    /// @endinternal
    virtual int listenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback) = 0;
    virtual void cancelListenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: pre_recorded_info)
    /// @endinternal
    virtual int listenDrcCameraPreRecordedInfo(const std::string& payload_index, const std::function<void(const CameraPreRecordedInfo&)>& callback) = 0;
    virtual void cancelListenDrcCameraPreRecordedInfo(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_capability)
    /// @endinternal
    virtual int listenDrcCameraCapability(const std::string& payload_index, const std::function<void(const CameraCapability&)>& callback) = 0;
    virtual void cancelListenDrcCameraCapability(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_settable_capability)
    /// @endinternal
    virtual int listenDrcCameraSettableCapability(const std::string& payload_index, const std::function<void(const CameraSettableCapability&)>& callback) = 0;
    virtual void cancelListenDrcCameraSettableCapability(const std::string& payload_index, int listen_id) = 0;

    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_state_push (field: camera_visible_capability)
    /// @endinternal
    virtual int listenDrcCameraVisibleCapability(const std::string& payload_index, const std::function<void(const CameraVisibleCapability&)>& callback) = 0;
    virtual void cancelListenDrcCameraVisibleCapability(const std::string& payload_index, int listen_id) = 0;

    // ── B4.1 drc_camera_photo_info_push 全量透传 ──
    /// @note ME 推送无 payload_index；回调里 `CameraPhotoInfo::payload_index` 见结构体注释（可能为空）。
    /// @internal
    /// 链路: DRC Northbound | method: drc_camera_photo_info_push
    /// @endinternal
    virtual int listenDrcCameraPhotoInfo(const std::string& payload_index, const std::function<void(const CameraPhotoInfo&)>& callback) = 0;
    virtual void cancelListenDrcCameraPhotoInfo(const std::string& payload_index, int listen_id) = 0;

    /// @brief 设置红外增益模式
    /// @param params 红外增益模式参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_thermal_gain_mode_set
    /// @endinternal
    virtual void drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // Camera Advanced Parameters (DRC)
    // ══════════════════════════════════════════════

    /// @brief 设置相机光圈值
    /// @param params 光圈值设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_aperture_value_set
    /// @endinternal
    virtual void drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机曝光模式
    /// @param params 曝光模式设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_exposure_mode_set
    /// @endinternal
    virtual void drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机曝光值
    /// @param params 曝光值设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_exposure_set
    /// @endinternal
    virtual void drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机对焦模式
    /// @param params 对焦模式设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_focus_mode_set
    /// @endinternal
    virtual void drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机对焦值
    /// @param params 对焦值设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_focus_value_set
    /// @endinternal
    virtual void drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机 ISO
    /// @param params ISO 设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_iso_set
    /// @endinternal
    virtual void drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机机械快门
    /// @param params 机械快门设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_mechanical_shutter_set
    /// @endinternal
    virtual void drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 相机点对焦操作
    /// @param params 点对焦参数（含负载索引、镜头类型及对焦坐标）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_point_focus_action
    /// @endinternal
    virtual void drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机快门速度
    /// @param params 快门速度设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_shutter_set
    /// @endinternal
    virtual void drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机水印
    /// @param params 水印设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_watermark_set
    /// @endinternal
    virtual void drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机畸变矫正
    /// @param params 畸变矫正设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_dewarping_set
    /// @endinternal
    virtual void drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // Screen/Gimbal Advanced (DRC)
    // ══════════════════════════════════════════════

    /// @brief 设置相机分屏显示
    /// @param params 分屏设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_screen_split
    /// @endinternal
    virtual void drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 镜头去雾
    /// @param params 去雾参数（含负载索引）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_clean_fog
    /// @endinternal
    virtual void drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // Thermal/IR (DRC)
    // ══════════════════════════════════════════════

    /// @brief 设置热成像调色板
    /// @param params 调色板设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_thermal_current_palette_set
    /// @endinternal
    virtual void drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置热成像超分辨率
    /// @param params 超分辨率使能参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_thermal_super_resolution_enable
    /// @endinternal
    virtual void drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置红外测温模式
    /// @param params 红外测温模式参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ir_metering_mode_set
    /// @endinternal
    virtual void drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置红外测温点
    /// @param params 红外测温点参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ir_metering_point_set
    /// @endinternal
    virtual void drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置红外测温区域
    /// @param params 红外测温区域参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ir_metering_area_set
    /// @endinternal
    virtual void drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置红外显示模式
    /// @param params 红外显示模式参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_ir_display_mode
    /// @endinternal
    virtual void drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置红外增强模式
    /// @param params 红外增强模式参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_ir_enhance_mode
    /// @endinternal
    virtual void drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置红外融合比例
    /// @param params 红外混合比例参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_ir_mix_rate
    /// @endinternal
    virtual void drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置等温线开关
    /// @param params 等温线使能参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_isotherm_enable
    /// @endinternal
    virtual void drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置等温线着色类型
    /// @param params 等温线着色类型参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_isotherm_color_rendering_type
    /// @endinternal
    virtual void drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置等温线温度上下限
    /// @param params 等温线温度范围参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_isotherm_lower_upper_range
    /// @endinternal
    virtual void drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置等温线多边形渲染区域
    /// @param params 等温线多边形区域顶点参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_isotherm_rendering_polygon
    /// @endinternal
    virtual void drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置按框选区域的等温线渲染使能
    /// @param params 等温线 ROI 渲染使能参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_isotherm_roi_based_rendering_enable
    /// @endinternal
    virtual void drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置近红外补光灯使能
    /// @param params 红外补光灯使能参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_infrared_fill_light_enable
    /// @endinternal
    virtual void drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置黑白夜视使能
    /// @param params 夜视使能参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_night_vision_enable
    /// @endinternal
    virtual void drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 重置相机设置
    /// @param params 重置参数（含负载索引）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_settings_reset
    /// @endinternal
    virtual void drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // Other Camera DRC
    // ══════════════════════════════════════════════

    /// @brief 设置相机降噪等级
    /// @param params 降噪等级设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_denoise_level_set
    /// @endinternal
    virtual void drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置视频分辨率
    /// @param params 视频分辨率设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_video_resolution_set
    /// @endinternal
    virtual void drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置照片格式
    /// @param params 照片格式设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_photo_format_set
    /// @endinternal
    virtual void drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置预录制状态
    /// @param params 预录制状态设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_pre_recorded_status_set
    /// @endinternal
    virtual void drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置预录制时长
    /// @param params 预录制时长设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_pre_recorded_time_set
    /// @endinternal
    virtual void drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置倒计时动作
    /// @param count_down_type 倒计时类型
    /// @param action_type 动作类型
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_count_down_action_set
    /// @endinternal
    virtual void drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置辅助画面模式
    /// @param mode 模式（0=OFF 1=FRONT 2=BACK 3=LEFT 4=RIGHT 5=AUTO）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_assistant_video_mode_set
    /// @endinternal
    virtual void drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置辅助画面开关
    /// @param sw 开关（0=关 1=开）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_assistant_video_switch_set
    /// @endinternal
    virtual void drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机电子去雾模式
    /// @param params 去雾模式设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_algorithm_dehaze_mode_set
    /// @endinternal
    virtual void drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置相机电子去雾等级
    /// @param params 去雾等级设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_algorithm_dehaze_level_set
    /// @endinternal
    virtual void drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅飞机相机水印设置（WebSocket 推送，与 DRC listenDrcCameraWatermarkSettings 相互独立）
    /// @param callback 水印设置回调（`DroneCameraWatermarkSettingsPush`，字段与旧 DRC `CameraWatermarkSettings` 不同，不要混用）
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_camera_watermark_settings
    /// @endinternal
    virtual int listenDroneCameraWatermarkSettings(const std::function<void(const DroneCameraWatermarkSettingsPush&)>& callback) = 0;

    /// @brief 取消订阅飞机相机水印设置（WebSocket 推送）
    /// @param listen_id 监听 ID
    virtual void cancelListenDroneCameraWatermarkSettings(int listen_id) = 0;

};

} // namespace flight_hub_sdk
