// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/camera_control_module.h"
#include "bll/manager/payload_ctrl_mgr/payload_ctrl_mgr.h"
#include "common/util/async_capture_protect.h"
#include <unordered_map>

namespace flight_hub_sdk {

class CameraControlModuleImpl : public ICameraControlModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit CameraControlModuleImpl(std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr);
    ~CameraControlModuleImpl() override = default;

    void drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    void drcSmartPhotoTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    void drcStartRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    void drcStopRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    void drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError)>& callback) override;
    void drcStopPanorama(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    int listenDrcPanoramaStatus(const std::string& payload_index, const std::function<void(const PanoramaStatus& status)>& callback) override;
    void cancelListenDrcPanoramaStatus(const std::string& payload_index, int listen_id) override;
    void drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError)>& callback) override;
    void drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError)>& callback) override;
    void drcStopTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    int listenDrcIntervalTakePhotoStatus(const std::string& payload_index, const std::function<void(const IntervalTakePhotoStatus& status)>& callback) override;
    void cancelListenDrcIntervalTakePhotoStatus(const std::string& payload_index, int listen_id) override;
    void drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError)>& callback) override;
    int listenDrcCameraModeAvailableRange(const std::string& payload_index, const std::function<void(const std::vector<int>&)>& callback) override;
    void cancelListenDrcCameraModeAvailableRange(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraState(const std::string& payload_index, const std::function<void(const CameraState& state)>& callback) override;
    void cancelListenDrcCameraState(const std::string& payload_index, int listen_id) override;
    int listenDrcLiveviewWorldRegion(const std::string& payload_index, const std::function<void(const LiveviewWorldRegion& region)>& callback) override;
    void cancelListenDrcLiveviewWorldRegion(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraWideLensState(const std::string& payload_index, const std::function<void(const CameraWideLensState& state)>& callback) override;
    void cancelListenDrcCameraWideLensState(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraZoomLensState(const std::string& payload_index, const std::function<void(const CameraZoomLensState& state)>& callback) override;
    void cancelListenDrcCameraZoomLensState(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraMeasureTarget(const std::string& payload_index, const std::function<void(const CameraMeasureTarget& target)>& callback) override;
    void cancelListenDrcCameraMeasureTarget(const std::string& payload_index, int listen_id) override;
    int listenDrcNightModeSettings(const std::string& payload_index, const std::function<void(const NightModeSettings& settings)>& callback) override;
    void cancelListenDrcNightModeSettings(const std::string& payload_index, int listen_id) override;
    int listenDrcGimbalState(const std::string& payload_index, const std::function<void(const GimbalState& state)>& callback) override;
    void cancelListenDrcGimbalState(const std::string& payload_index, int listen_id) override;
    int listenDrcLiveVideoInfo(const std::string& payload_index, const std::function<void(const LiveVideoInfo& info)>& callback) override;
    void cancelListenDrcLiveVideoInfo(const std::string& payload_index, int listen_id) override;
    int listenDrcThermalGainMode(const std::string& payload_index, const std::function<void(const ThermalGainModeInfo& info)>& callback) override;
    void cancelListenDrcThermalGainMode(const std::string& payload_index, int listen_id) override;

    int listenDrcCameraIrLensState(const std::string& payload_index, const std::function<void(const CameraIrLensState& state)>& callback) override;
    void cancelListenDrcCameraIrLensState(const std::string& payload_index, int listen_id) override;

    int listenDrcCameraIrThermometry(const std::string& payload_index, const std::function<void(const CameraIrThermometryInfo& info)>& callback) override;
    void cancelListenDrcCameraIrThermometry(const std::string& payload_index, int listen_id) override;

    // ── B4 State push 拆分 ──
    int listenDrcCameraWatermarkSettings(const std::string& payload_index, const std::function<void(const CameraWatermarkSettings&)>& callback) override;
    void cancelListenDrcCameraWatermarkSettings(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraStorageInfo(const std::string& payload_index, const std::function<void(const CameraStorageInfo&)>& callback) override;
    void cancelListenDrcCameraStorageInfo(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraIrPipState(const std::string& payload_index, const std::function<void(const CameraIrPipState&)>& callback) override;
    void cancelListenDrcCameraIrPipState(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraIsothermPolygon(const std::string& payload_index, const std::function<void(const CameraIsothermPolygon&)>& callback) override;
    void cancelListenDrcCameraIsothermPolygon(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraMediaStorage(const std::string& payload_index, const std::function<void(const CameraMediaStorage&)>& callback) override;
    void cancelListenDrcCameraMediaStorage(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback) override;
    void cancelListenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraPreRecordedInfo(const std::string& payload_index, const std::function<void(const CameraPreRecordedInfo&)>& callback) override;
    void cancelListenDrcCameraPreRecordedInfo(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraCapability(const std::string& payload_index, const std::function<void(const CameraCapability&)>& callback) override;
    void cancelListenDrcCameraCapability(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraSettableCapability(const std::string& payload_index, const std::function<void(const CameraSettableCapability&)>& callback) override;
    void cancelListenDrcCameraSettableCapability(const std::string& payload_index, int listen_id) override;
    int listenDrcCameraVisibleCapability(const std::string& payload_index, const std::function<void(const CameraVisibleCapability&)>& callback) override;
    void cancelListenDrcCameraVisibleCapability(const std::string& payload_index, int listen_id) override;

    // ── B4.1 Photo info 全量 ──
    int listenDrcCameraPhotoInfo(const std::string& payload_index, const std::function<void(const CameraPhotoInfo&)>& callback) override;
    void cancelListenDrcCameraPhotoInfo(const std::string& payload_index, int listen_id) override;
    void drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError)>& callback) override;

    // ── Camera Advanced Parameters (DRC) ──
    void drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError)>& callback) override;

    // ── Screen/Gimbal Advanced (DRC) ──
    void drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError)>& callback) override;

    // ── Thermal/IR (DRC) ──
    void drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError)>& callback) override;

    // ── Other Camera DRC ──
    void drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError)>& callback) override;
    void drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError)>& callback) override;
    void drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError)>& callback) override;
    void drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError)>& callback) override;
    void drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError)>& callback) override;

    int listenDroneCameraWatermarkSettings(const std::function<void(const DroneCameraWatermarkSettingsPush&)>& callback) override;
    void cancelListenDroneCameraWatermarkSettings(int listen_id) override;

private:
    std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr_;

    // NC push 订阅：user_listen_id ↔ nc_listen_id 映射（本模块无独立 sn_ 成员，借助 payload_ctrl_mgr_->getDroneSn()）
    std::unordered_map<int, int64_t> drone_camera_watermark_settings_nc_listen_ids_;
    int drone_camera_watermark_settings_next_id_ = 0;
};

} // namespace flight_hub_sdk
