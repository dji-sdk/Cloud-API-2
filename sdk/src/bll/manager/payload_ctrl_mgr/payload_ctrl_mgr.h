// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "api/value.h"
#include "api/message.h"
#include <string>
#include "tool/callback_container.h"
#include "camera_abs.h"

namespace flight_hub_sdk {

class PayloadCtrlMgr : public drc_sdk::common::AsyncCaptureProtect {
public:
    PayloadCtrlMgr(const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine, int drone_instance_id);
    ~PayloadCtrlMgr() override;

    /// SN accessors（供 module impl 订阅 NC 时构造 topic）
    const std::string& getDroneSn() const { return drone_sn_; }
    const std::string& getGatewaySn() const { return gateway_sn_; }

    void acquirePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);
    void releasePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);
    int listenPayloadControlAuthority(const std::string& payload_index, const std::function<void(bool)>& callback);
    void cancelListenPayloadControlAuthority(const std::string& payload_index, int listen_id);

    // switch live lens
    void drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError)>& callback);

    // photo
    void drcTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);

    // smart photo
    void drcSmartPhotoTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);

    // record
    void drcStartRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);
    void drcStopRecord(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);

    // zoom at target
    void drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError)>& callback);

    // set focal length;
    void drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError)>& callback);

    // set linkage zoom;
    void drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError)>& callback);

    // 负载控制: 重置云台(method: gimbal_reset);
    void drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError)>& callback);

    // look at target
    void lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback);

    // 云台拖拽: drc_camera_screen_drag
    void drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError)>& callback);

    // panorama
    void drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError)>& callback);
    void drcStopPanorama(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);
    int listenDrcPanoramaStatus(const std::string& payload_index, const std::function<void(const PanoramaStatus& status)>& callback);
    void cancelListenDrcPanoramaStatus(const std::string& payload_index, int listen_id);

    // 照片分辨率设置
    void drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError)>& callback);

    // 定时拍
    void drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError)>& callback);
    void drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError)>& callback);
    int listenDrcIntervalTakePhotoStatus(const std::string& payload_index, const std::function<void(const IntervalTakePhotoStatus& status)>& callback);
    void cancelListenDrcIntervalTakePhotoStatus(const std::string& payload_index, int listen_id);

    void drcStopTakePhoto(const std::string& payload_index, const std::function<void(FlightHubError)>& callback);
    // 双击AIM
    void drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError)>& callback);

    // switch camera mode
    void drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError)>& callback);
    // 用 std::vector<int> 替换 std::vector<CameraMode>, 避免跨平台编译报错(比如 go 编译);
    int listenDrcCameraModeAvailableRange(const std::string& payload_index, const std::function<void(const std::vector<int>&)>& callback);
    void cancelListenDrcCameraModeAvailableRange(const std::string& payload_index, int listen_id);

    /// camera/payload osd info
    int listenDrcCameraState(const std::string& payload_index, const std::function<void(const CameraState& state)> callback);
    void cancelListenDrcCameraState(const std::string& payload_index, int listen_id);

    int listenDrcLiveviewWorldRegion(const std::string& payload_index, const std::function<void(const LiveviewWorldRegion& region)> callback);
    void cancelListenDrcLiveviewWorldRegion(const std::string& payload_index, int listen_id);

    int listenDrcCameraWideLensState(const std::string& payload_index, const std::function<void(const CameraWideLensState& state)> callback);
    void cancelListenDrcCameraWideLensState(const std::string& payload_index, int listen_id);

    int listenDrcCameraZoomLensState(const std::string& payload_index, const std::function<void(const CameraZoomLensState& state)> callback);
    void cancelListenDrcCameraZoomLensState(const std::string& payload_index, int listen_id);

    int listenDrcCameraMeasureTarget(const std::string& payload_index, const std::function<void(const CameraMeasureTarget& target)> callback);
    void cancelListenDrcCameraMeasureTarget(const std::string& payload_index, int listen_id);
    int listenDrcNightModeSettings(const std::string& payload_index, const std::function<void(const NightModeSettings& settings)> callback);
    void cancelListenDrcNightModeSettings(const std::string& payload_index, int listen_id);

    int listenDrcGimbalState(const std::string& payload_index, const std::function<void(const GimbalState& state)> callback);
    void cancelListenDrcGimbalState(const std::string& payload_index, int listen_id);

    int listenDrcLiveVideoInfo(const std::string& payload_index, const std::function<void(const LiveVideoInfo& info)>& callback);
    void cancelListenDrcLiveVideoInfo(const std::string& payload_index, int listen_id);

    // 红外增益模式
    int listenDrcThermalGainMode(const std::string& payload_index, const std::function<void(const ThermalGainModeInfo& info)>& callback);
    void cancelListenDrcThermalGainMode(const std::string& payload_index, int listen_id);
    void drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError)>& callback);

    int listenDrcCameraIrLensState(const std::string& payload_index, const std::function<void(const CameraIrLensState& state)>& callback);
    void cancelListenDrcCameraIrLensState(const std::string& payload_index, int listen_id);

    int listenDrcCameraIrThermometry(const std::string& payload_index, const std::function<void(const CameraIrThermometryInfo& info)>& callback);
    void cancelListenDrcCameraIrThermometry(const std::string& payload_index, int listen_id);

    // ── B4 drc_camera_state_push 拆分 listen ──
    int listenDrcCameraWatermarkSettings(const std::string& payload_index, const std::function<void(const CameraWatermarkSettings&)>& callback);
    void cancelListenDrcCameraWatermarkSettings(const std::string& payload_index, int listen_id);
    int listenDrcCameraStorageInfo(const std::string& payload_index, const std::function<void(const CameraStorageInfo&)>& callback);
    void cancelListenDrcCameraStorageInfo(const std::string& payload_index, int listen_id);
    int listenDrcCameraIrPipState(const std::string& payload_index, const std::function<void(const CameraIrPipState&)>& callback);
    void cancelListenDrcCameraIrPipState(const std::string& payload_index, int listen_id);
    int listenDrcCameraIsothermPolygon(const std::string& payload_index, const std::function<void(const CameraIsothermPolygon&)>& callback);
    void cancelListenDrcCameraIsothermPolygon(const std::string& payload_index, int listen_id);
    int listenDrcCameraMediaStorage(const std::string& payload_index, const std::function<void(const CameraMediaStorage&)>& callback);
    void cancelListenDrcCameraMediaStorage(const std::string& payload_index, int listen_id);
    int listenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback);
    void cancelListenDrcCameraAlgorithmDehazeInfo(const std::string& payload_index, int listen_id);
    int listenDrcCameraPreRecordedInfo(const std::string& payload_index, const std::function<void(const CameraPreRecordedInfo&)>& callback);
    void cancelListenDrcCameraPreRecordedInfo(const std::string& payload_index, int listen_id);
    int listenDrcCameraCapability(const std::string& payload_index, const std::function<void(const CameraCapability&)>& callback);
    void cancelListenDrcCameraCapability(const std::string& payload_index, int listen_id);
    int listenDrcCameraSettableCapability(const std::string& payload_index, const std::function<void(const CameraSettableCapability&)>& callback);
    void cancelListenDrcCameraSettableCapability(const std::string& payload_index, int listen_id);
    int listenDrcCameraVisibleCapability(const std::string& payload_index, const std::function<void(const CameraVisibleCapability&)>& callback);
    void cancelListenDrcCameraVisibleCapability(const std::string& payload_index, int listen_id);

    // ── B4.1 drc_camera_photo_info_push 全量 ──
    int listenDrcCameraPhotoInfo(const std::string& payload_index, const std::function<void(const CameraPhotoInfo&)>& callback);
    void cancelListenDrcCameraPhotoInfo(const std::string& payload_index, int listen_id);

    // ── Camera Advanced Params (DRC) ──
    void drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError)>& callback);

    // ── Screen/Gimbal Advanced (DRC) ──
    void drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError)>& callback);
    void drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError)>& callback);

    // ── Thermal/IR (DRC) ──
    void drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError)>& callback);
    void drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError)>& callback);

    // ── Other Camera DRC ──
    void drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError)>& callback);
    void drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError)>& callback);
    void drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError)>& callback);
    void drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError)>& callback);
    void drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError)>& callback);

    // 设置用户回调接口（由 DroneImpl 调用，用于在状态更新后通知用户）
    void setAircraftPayloadsListCallback(std::function<void(const AircraftPayloadsList&)> callback);

private:
    int instance_id_ = 0;
    std::string log_tag_;

    /// camera status listening
    void initializeDeviceStatusListening();
    void onReceiveAircraftPayloadsList(const notification_center::AircraftPayloadsList& msg);
    
    /// message engine
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    /// params
    std::string drone_sn_;
    std::string gateway_sn_;

    // listen id
    int aircraft_payloads_list_listen_id_ = -1;

    /// state & data cache
    notification_center::AircraftPayloadsList aircraft_payloads_list_msg_;

    /// camera abs
    std::unordered_map<std::string, std::shared_ptr<CameraAbs>> camera_abs_map_;

    /// 用户回调（由 DroneImpl 设置，用于在状态更新后通知用户）
    std::function<void(const AircraftPayloadsList&)> aircraft_payloads_list_user_callback_;

};

}  // namespace flight_hub_sdk