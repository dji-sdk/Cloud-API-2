// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "api/value.h"
#include "api/message.h"
#include <string>
#include "tool/callback_container.h"
#include <BusinessManager.h>

namespace flight_hub_sdk {

class CameraAbs : public drc_sdk::common::AsyncCaptureProtect {
public:
    CameraAbs(const std::string& payload_index, const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine, int drone_instance_id);
    ~CameraAbs() override;

    void acquirePayloadControlAuthority(const std::function<void(FlightHubError)>& callback);
    void releasePayloadControlAuthority(const std::function<void(FlightHubError)>& callback);
    int listenPayloadControlAuthority(const std::function<void(bool)>& callback);
    void cancelListenPayloadControlAuthority(int listen_id);

    // switch live lens
    void drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError err)>& callback);

    // photo
    void drcTakePhoto(const std::function<void(FlightHubError err)>& callback);

    // smart photo
    void drcSmartPhotoTakePhoto(const std::function<void(FlightHubError err)>& callback);

    // record
    void drcStartRecord(const std::function<void(FlightHubError err)>& callback);
    void drcStopRecord(const std::function<void(FlightHubError err)>& callback);

    // zoom at target
    void drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError err)>& callback);

    // set focal length;
    void drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError err)>& callback);

    // set linkage zoom;
    void drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError err)>& callback);

    // 负载控制: 重置云台(method: gimbal_reset);
    void drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError err)>& callback);

    // look at target
    void lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback);

    // 云台拖拽: drc_camera_screen_drag
    void drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError err)>& callback);

    // panorama
    void drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError err)>& callback);
    void drcStopPanorama(const std::function<void(FlightHubError err)>& callback);
    int listenDrcPanoramaStatus(const std::function<void(const PanoramaStatus& status)>& callback);
    void cancelListenDrcPanoramaStatus(int listen_id);

    // 照片分辨率设置
    void drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError err)>& callback);

    // 双击AIM
    void drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError err)>& callback);

    // 定时拍
    void drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError err)>& callback);
    void drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError err)>& callback);
    int listenDrcIntervalTakePhotoStatus(const std::function<void(const IntervalTakePhotoStatus& status)>& callback);
    void cancelListenDrcIntervalTakePhotoStatus(int listen_id);

    void drcStopTakePhoto(const std::function<void(FlightHubError err)>& callback);

    // switch camera mode
    void drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError err)>& callback);
    // 用 std::vector<int> 替换 std::vector<CameraMode>, 避免跨平台编译报错(比如 go 编译);
    int listenDrcCameraModeAvailableRange(const std::function<void(const std::vector<int>&)>& callback);
    void cancelListenDrcCameraModeAvailableRange(int listen_id);

    /// camera/payload osd info
    int listenDrcCameraState(const std::function<void(const CameraState& state)> callback);
    void cancelListenDrcCameraState(int listen_id);

    int listenDrcLiveviewWorldRegion(const std::function<void(const LiveviewWorldRegion& region)> callback);
    void cancelListenDrcLiveviewWorldRegion(int listen_id);

    int listenDrcCameraWideLensState(const std::function<void(const CameraWideLensState& state)> callback);
    void cancelListenDrcCameraWideLensState(int listen_id);

    int listenDrcCameraZoomLensState(const std::function<void(const CameraZoomLensState& state)> callback);
    void cancelListenDrcCameraZoomLensState(int listen_id);

    int listenDrcCameraMeasureTarget(const std::function<void(const CameraMeasureTarget& target)> callback);
    void cancelListenDrcCameraMeasureTarget(int listen_id);
    int listenDrcNightModeSettings(const std::function<void(const NightModeSettings& settings)> callback);
    void cancelListenDrcNightModeSettings(int listen_id);

    // ── B4 drc_camera_state_push 拆分 listen ──
    int listenDrcCameraWatermarkSettings(const std::function<void(const CameraWatermarkSettings&)>& callback);
    void cancelListenDrcCameraWatermarkSettings(int listen_id);
    int listenDrcCameraStorageInfo(const std::function<void(const CameraStorageInfo&)>& callback);
    void cancelListenDrcCameraStorageInfo(int listen_id);
    int listenDrcCameraIrPipState(const std::function<void(const CameraIrPipState&)>& callback);
    void cancelListenDrcCameraIrPipState(int listen_id);
    int listenDrcCameraIsothermPolygon(const std::function<void(const CameraIsothermPolygon&)>& callback);
    void cancelListenDrcCameraIsothermPolygon(int listen_id);
    int listenDrcCameraMediaStorage(const std::function<void(const CameraMediaStorage&)>& callback);
    void cancelListenDrcCameraMediaStorage(int listen_id);
    int listenDrcCameraAlgorithmDehazeInfo(const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback);
    void cancelListenDrcCameraAlgorithmDehazeInfo(int listen_id);
    int listenDrcCameraPreRecordedInfo(const std::function<void(const CameraPreRecordedInfo&)>& callback);
    void cancelListenDrcCameraPreRecordedInfo(int listen_id);
    int listenDrcCameraCapability(const std::function<void(const CameraCapability&)>& callback);
    void cancelListenDrcCameraCapability(int listen_id);
    int listenDrcCameraSettableCapability(const std::function<void(const CameraSettableCapability&)>& callback);
    void cancelListenDrcCameraSettableCapability(int listen_id);
    int listenDrcCameraVisibleCapability(const std::function<void(const CameraVisibleCapability&)>& callback);
    void cancelListenDrcCameraVisibleCapability(int listen_id);

    // ── B4.1 drc_camera_photo_info_push 全量透传 ──
    int listenDrcCameraPhotoInfo(const std::function<void(const CameraPhotoInfo&)>& callback);
    void cancelListenDrcCameraPhotoInfo(int listen_id);

    int listenDrcGimbalState(const std::function<void(const GimbalState& state)> callback);
    void cancelListenDrcGimbalState(int listen_id);

    int listenDrcLiveVideoInfo(const std::function<void(const LiveVideoInfo& info)>& callback);
    void cancelListenDrcLiveVideoInfo(int listen_id);

    // 红外增益模式
    int listenDrcThermalGainMode(const std::function<void(const ThermalGainModeInfo& info)>& callback);
    void cancelListenDrcThermalGainMode(int listen_id);
    void drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError err)>& callback);

    // 红外镜头完整状态（含 gain / palette / isotherm / zoom / split / FOV）
    int listenDrcCameraIrLensState(const std::function<void(const CameraIrLensState& state)>& callback);
    void cancelListenDrcCameraIrLensState(int listen_id);

    // 红外测温上行推送（与 setIrMetering* 成对）
    int listenDrcCameraIrThermometry(const std::function<void(const CameraIrThermometryInfo& info)>& callback);
    void cancelListenDrcCameraIrThermometry(int listen_id);

    // ── Camera Advanced Params ──
    void drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError err)>& callback);

    // ── Screen/Gimbal Advanced ──
    void drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError err)>& callback);

    // ── Thermal/IR ──
    void drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError err)>& callback);

    // ── Other Camera ──
    void drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError err)>& callback);
    void drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError err)>& callback);
    void drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError err)>& callback);
    void drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError err)>& callback);
    void drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError err)>& callback);

private:
    int instance_id_ = 0;
    std::string log_tag_;

    std::string payload_index_;

    void initPayloadAuthorityListening();
    void onReceivePayloadControlAuthority(const notification_center::ControlRightsCloudControlStatus& msg);

    /// camera status listening
    void initializeDeviceStatusListening();
    void onReceiveCameraDrcOsdInfo(const message_engine::DrcDrcCameraOsdInfoPushNorthbound& msg);
    void onReceiveCameraState(const message_engine::DrcDrcCameraStatePushNorthbound& msg);
    void onReceiveAircraftPayloadsList(const notification_center::AircraftPayloadsList& msg);
    void onReceiveLiveVideoInfoPush(const message_engine::DrcDrcLiveVideoInfoPushNorthbound& msg);
    void onReceiveDrcDroneStatePush(const message_engine::DrcDrcDroneStatePushNorthbound& msg);
    /// `drc_camera_photo_info_push`：全量 CameraPhotoInfo + Panorama/Interval 切片
    void onReceiveCameraPhotoInfoPush(const std::shared_ptr<message_engine::DrcDrcCameraPhotoInfoPushNorthbound>& msg);

    /// message engine
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// params
    std::string drone_sn_;
    std::string gateway_sn_;

    /// state & data cache
    message_engine::DrcDrcCameraOsdInfoPushNorthbound drc_camera_osd_msg_;
    message_engine::DrcDrcCameraStatePushNorthbound drc_camera_state_msg_;
    notification_center::AircraftPayloadsList aircraft_payloads_list_msg_;
    bool is_payload_control_authority_ = false;
    notification_center::ControlRightsCloudControlStatus payload_control_authority_msg_;
    message_engine::DrcDrcLiveVideoInfoPushNorthbound live_video_info_push_msg_;
    message_engine::DrcDrcDroneStatePushNorthbound drc_drone_state_push_msg_;
    message_engine::DrcDrcCameraPhotoInfoPushNorthbound panorama_status_msg_;

    /// callback container
    CallbackContainer<bool> payload_control_authority_callback_container_;
    CallbackContainer<CameraState> camera_state_callback_container_;
    CallbackContainer<LiveviewWorldRegion> liveview_world_region_callback_container_;
    CallbackContainer<CameraWideLensState> camera_wide_lens_state_callback_container_;
    CallbackContainer<CameraZoomLensState> camera_zoom_lens_state_callback_container_;
    CallbackContainer<CameraMeasureTarget> camera_measure_target_callback_container_;
    CallbackContainer<NightModeSettings> night_mode_settings_callback_container_;
    CallbackContainer<GimbalState> gimbal_state_callback_container_;
    CallbackContainer<LiveVideoInfo> live_video_info_callback_container_;
    CallbackContainer<ThermalGainModeInfo> thermal_gain_mode_callback_container_;
    CallbackContainer<CameraIrLensState> camera_ir_lens_state_callback_container_;
    CallbackContainer<CameraIrThermometryInfo> camera_ir_thermometry_callback_container_;
    // ── B4 State push 各子结构 ──
    CallbackContainer<CameraWatermarkSettings> camera_watermark_settings_callback_container_;
    CallbackContainer<CameraStorageInfo> camera_storage_info_callback_container_;
    CallbackContainer<CameraIrPipState> camera_ir_pip_state_callback_container_;
    CallbackContainer<CameraIsothermPolygon> camera_isotherm_polygon_callback_container_;
    CallbackContainer<CameraMediaStorage> camera_media_storage_callback_container_;
    CallbackContainer<CameraAlgorithmDehazeInfo> camera_algorithm_dehaze_info_callback_container_;
    CallbackContainer<CameraPreRecordedInfo> camera_pre_recorded_info_callback_container_;
    CallbackContainer<CameraCapability> camera_capability_callback_container_;
    CallbackContainer<CameraSettableCapability> camera_settable_capability_callback_container_;
    CallbackContainer<CameraVisibleCapability> camera_visible_capability_callback_container_;
    // ── B4.1 Photo info 全量 ──
    CallbackContainer<CameraPhotoInfo> camera_photo_info_callback_container_;
    CallbackContainer<PanoramaStatus> panorama_status_callback_container_;
    // 用 std::vector<int> 替换 std::vector<CameraMode>, 避免跨平台编译报错(比如 go 编译);
    CallbackContainer<std::vector<int>> camera_mode_available_range_callback_container_;
    CallbackContainer<IntervalTakePhotoStatus> interval_take_photo_status_callback_container_;

    // listen id
    int drc_camera_osd_listen_id_ = -1;
    int drc_camera_state_listen_id_ = -1;
    int aircraft_payloads_list_listen_id_ = -1;
    int payload_control_authority_listen_id_ = -1;
    int live_video_info_push_listen_id_ = -1;
    int drc_drone_state_push_listen_id_ = -1;
    /// `drc_camera_photo_info_push` 单一 Observe（PhotoInfo 全量 + Panorama/Interval 切片共用）
    int camera_photo_info_listen_id_ = -1;
};

}