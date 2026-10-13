// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk;

import com.dji.flighthubsdk.bean.AircraftActivationTime;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftBatteryWarningThresholds;
import com.dji.flighthubsdk.bean.AircraftComplianceInfo;
import com.dji.flighthubsdk.bean.AircraftControlCode;
import com.dji.flighthubsdk.bean.AircraftControlSource;
import com.dji.flighthubsdk.bean.AircraftCurrentRthMode;
import com.dji.flighthubsdk.bean.AircraftFirmwareStatus;
import com.dji.flighthubsdk.bean.AircraftGear;
import com.dji.flighthubsdk.bean.AircraftHomeDistance;
import com.dji.flighthubsdk.bean.AircraftHomeGeographicalInfo;
import com.dji.flighthubsdk.bean.AircraftLimitsInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.AircraftMaintainStatusInfo;
import com.dji.flighthubsdk.bean.AircraftModeCode;
import com.dji.flighthubsdk.bean.AircraftPayloadsCamerasList;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfoList;
import com.dji.flighthubsdk.bean.AircraftNightLightsState;
import com.dji.flighthubsdk.bean.AircraftObstacleAvoidanceState;
import com.dji.flighthubsdk.bean.AircraftPositionState;
import com.dji.flighthubsdk.bean.AircraftRcLostAction;
import com.dji.flighthubsdk.bean.AircraftRthInfo;
import com.dji.flighthubsdk.bean.AircraftSpeed;
import com.dji.flighthubsdk.bean.AircraftStorageInfo;
import com.dji.flighthubsdk.bean.AircraftTakeoffProgress;
import com.dji.flighthubsdk.bean.AircraftTotalFlightInfo;
import com.dji.flighthubsdk.bean.AircraftTrackId;
import com.dji.flighthubsdk.bean.AircraftWindSpeed;
import com.dji.flighthubsdk.bean.AiSeiInfo;
import com.dji.flighthubsdk.bean.ArSeiInfo;
import com.dji.flighthubsdk.bean.AssistantSeiInfo;
import com.dji.flighthubsdk.bean.EisSeiInfo;
import com.dji.flighthubsdk.bean.FovSeiInfo;
import com.dji.flighthubsdk.bean.FrameInfo;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.CameraMeasureTarget;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.NightModeSettings;
import com.dji.flighthubsdk.bean.CameraWideLensState;
import com.dji.flighthubsdk.bean.CameraZoomLensState;
import com.dji.flighthubsdk.bean.DeviceAudienceCountItem;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResult;
import com.dji.flighthubsdk.bean.DockStatusItemResult;
import com.dji.flighthubsdk.bean.DroneStatusItemResult;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityConfigItemResult;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResult;
import com.dji.flighthubsdk.bean.FirmwareItemResult;
import com.dji.flighthubsdk.bean.FirmwareReleaseNoteResult;
import com.dji.flighthubsdk.bean.FirmwareStatusItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressItem;
import com.dji.flighthubsdk.bean.DeviceOtaProgress;
import com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListResult;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.DuplicateWaylineResult;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.BatchDeleteWaylinesResult;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.UploadUnlockLicenseFileResult;
import com.dji.flighthubsdk.bean.UpdateWaylineResult;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import com.dji.flighthubsdk.bean.DeviceDetailInfo;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.DeviceTopoChange;
import com.dji.flighthubsdk.bean.DockActivationTime;
import com.dji.flighthubsdk.bean.DockAccTime;
import com.dji.flighthubsdk.bean.DockAirConditionerStatusInfo;
import com.dji.flighthubsdk.bean.DockAlarmState;
import com.dji.flighthubsdk.bean.DockAlternateLandPoint;
import com.dji.flighthubsdk.bean.DockBackupBatteryInfo;
import com.dji.flighthubsdk.bean.DockBatteryStoreMode;
import com.dji.flighthubsdk.bean.DockCoverState;
import com.dji.flighthubsdk.bean.DockDrcState;
import com.dji.flighthubsdk.bean.DockDroneBatteryMaintenanceInfo;
import com.dji.flighthubsdk.bean.DockDroneChargeState;
import com.dji.flighthubsdk.bean.DockEmergencyStopState;
import com.dji.flighthubsdk.bean.DockFirstPowerOnTime;
import com.dji.flighthubsdk.bean.DockFirmwareInfo;
import com.dji.flighthubsdk.bean.DockJobNumber;
import com.dji.flighthubsdk.bean.DockLocation;
import com.dji.flighthubsdk.bean.DockMaintainStatusInfo;
import com.dji.flighthubsdk.bean.DockMediaFileDetail;
import com.dji.flighthubsdk.bean.DockMeteorologicalInfo;
import com.dji.flighthubsdk.bean.DockModeCode;
import com.dji.flighthubsdk.bean.DockNetworkState;
import com.dji.flighthubsdk.bean.DockOnlineStatus;
import com.dji.flighthubsdk.bean.DockPositionState;
import com.dji.flighthubsdk.bean.DockPowerState;
import com.dji.flighthubsdk.bean.DockPutterState;
import com.dji.flighthubsdk.bean.DockRelativeAlternateLandPoint;
import com.dji.flighthubsdk.bean.DockSelfConvergeCoordinate;
import com.dji.flighthubsdk.bean.DockSilentMode;
import com.dji.flighthubsdk.bean.DockStorageState;
import com.dji.flighthubsdk.bean.DockSubDeviceInfoList;
import com.dji.flighthubsdk.bean.DockSupplementLightState;
import com.dji.flighthubsdk.bean.DockUserExperienceImprovement;
import com.dji.flighthubsdk.bean.DockWirelessLink;
import com.dji.flighthubsdk.bean.DroneInDock;
import com.dji.flighthubsdk.bean.FlightTaskStepCode;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlyToDroneSelfPlanPath;
import com.dji.flighthubsdk.bean.FlyToPointProgress;
import com.dji.flighthubsdk.bean.GimbalState;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveRecordItem;
import com.dji.flighthubsdk.bean.LiveRecordStateInfo;
import com.dji.flighthubsdk.bean.LiveStreamStartResult;
import com.dji.flighthubsdk.bean.LiveVideoInfo;
import com.dji.flighthubsdk.bean.LiveviewWorldRegion;
import com.dji.flighthubsdk.bean.IntervalTakePhotoStatus;
import com.dji.flighthubsdk.bean.PanoramaStatus;
import com.dji.flighthubsdk.bean.PoiStatusEvent;
import com.dji.flighthubsdk.bean.POITaskInfo;
import com.dji.flighthubsdk.bean.ThermalGainModeInfo;
import com.dji.flighthubsdk.bean.PsdkDeviceList;
import com.dji.flighthubsdk.bean.PsdkFloatingWindowTextInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerPlayProgressInfo;
import com.dji.flighthubsdk.bean.PsdkStateSearchlightInfo;
import com.dji.flighthubsdk.bean.PsdkStateSpeakerInfo;
import com.dji.flighthubsdk.bean.PsdkStateSummaryInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerResource;
import com.dji.flighthubsdk.bean.PsdkSpeakerResourceList;
import com.dji.flighthubsdk.bean.PsdkWidgetResourceList;
import com.dji.flighthubsdk.bean.STSInfo;
import com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingResult;
import com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingResult;
import com.dji.flighthubsdk.bean.GetAiAlertFlightsResult;
import com.dji.flighthubsdk.bean.GetAiAlertRecordsResult;
import com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingResult;
import com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingResult;
import com.dji.flighthubsdk.value.CameraMode;

public class FlightHubCallback {
    /**
     * 设备列表回调接口
     */
    @FunctionalInterface
    public interface DeviceListCallback {
        /**
         * @param devices 设备拓扑列表
         * @param error 错误信息
         */
        void onResult(java.util.List<DeviceTopo> devices, FlightHubError error);
    }

    /** 获取设备详情回调 */
    public interface GetDeviceDetailInfoCallback {
        /**
         * @param error 错误信息
         * @param deviceDetail 设备详情
         */
        void onResult(FlightHubError error, DeviceDetailInfo deviceDetail);
    }

    /** 修改设备名称回调 */
    @FunctionalInterface
    public interface ModifyDeviceNameCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /**
     * 结果回调接口
     */
    @FunctionalInterface
    public interface ResultCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /**
     * 结果回调接口（含分层错误码）
     */
    @FunctionalInterface
    public interface ResultCallbackWithError {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 一键起飞回调 */
    @FunctionalInterface
    public interface TakeoffCallback {
        /**
         * @param error 错误信息
         * @param result 起飞结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TakeoffResult result);
    }

    /** 创建空中航线任务回调 */
    @FunctionalInterface
    public interface CreateInFlightWaylineTaskCallback {
        /**
         * @param result 创建结果
         * @param error 错误信息
         */
        void onResult(com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult result, FlightHubError error);
    }

    /** 查询活跃空中航线任务回调 */
    @FunctionalInterface
    public interface GetActiveInFlightWaylineTaskCallback {
        /**
         * @param info 活跃任务信息
         * @param error 错误信息
         */
        void onResult(com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo info, FlightHubError error);
    }

    /** 空中航线任务进度推送回调 */
    @FunctionalInterface
    public interface InFlightWaylineProgressCallback {
        /**
         * @param progress 进度信息
         */
        void onResult(com.dji.flighthubsdk.bean.InFlightWaylineProgress progress);
    }

    /** 控制空中航线任务回调 */
    @FunctionalInterface
    public interface InFlightWaylineControlCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /**
     * 获取设备直播录制记录列表回调
     */
    public interface GetDeviceLiveRecordsCallback {
        /**
         * @param error 错误信息
         * @param records 录制记录列表
         */
        void onResult(FlightHubError error, java.util.List<LiveRecordItem> records);
    }

    /**
     * 直播录制状态推送回调（NCSDK topic_live_record_info）
     */
    public interface LiveRecordStateCallback {
        void onRecordState(LiveRecordStateInfo info);
    }

    /**
     * Available回调接口
     */
    @FunctionalInterface
    public interface AvailableCallback {
        /**
         * @param available 是否可用
         */
        void onResult(boolean available);
    }

    /**
     * LiveAvailableRange回调接口
     */
    @FunctionalInterface
    public interface LiveAvailableRangeCallback {
        /**
         * @param liveInfos 直播信息列表
         */
        void onResult(java.util.List<LiveInfo> liveInfos);
    }

    /**
     * 直播流数据回调；第二参数为对外 {@link com.dji.flighthubsdk.bean.FrameInfo}（非 SWIG）。
     */
    @FunctionalInterface
    public interface LiveStreamCallback {
        /**
         * @param data 直播流原始数据
         * @param frameInfo 帧信息
         */
        void onResult(byte[] data, FrameInfo frameInfo);
    }

    /**
     * 直播拉流地址回调；用于 {@link LiveModule#startWithURL} 接口，返回后端拉流地址供用户自行播放。
     */
    @FunctionalInterface
    public interface LiveStreamStartCallback {
        /**
         * @param error 错误信息
         * @param result 拉流地址信息，失败时为 null
         */
        void onResult(FlightHubError error, LiveStreamStartResult result);
    }

    /**
     * AI SEI 信息回调；参数为对外 {@link com.dji.flighthubsdk.bean.AiSeiInfo}（含 {@link com.dji.flighthubsdk.bean.AiDetectionTarget} 列表，非 SWIG 类型）。
     */
    @FunctionalInterface
    public interface AiSeiInfoCallback {
        /**
         * @param info AI SEI 信息
         */
        void onResult(AiSeiInfo info);
    }

    /** AR SEI；参数为 {@code bean.ArSeiInfo}。 */
    @FunctionalInterface
    public interface ArSeiInfoCallback {
        /**
         * @param info AR SEI 信息
         */
        void onResult(ArSeiInfo info);
    }

    /**
     * FOV SEI 回调；参数为 {@link com.dji.flighthubsdk.bean.FovSeiInfo}（画幅 μm、焦距整型÷1000 为 mm）。
     */
    @FunctionalInterface
    public interface FovSeiInfoCallback {
        /**
         * @param info FOV SEI 信息
         */
        void onResult(FovSeiInfo info);
    }

    /** Assistant SEI；参数为 {@code bean.AssistantSeiInfo}。 */
    @FunctionalInterface
    public interface AssistantSeiInfoCallback {
        /**
         * @param info Assistant SEI 信息
         */
        void onResult(AssistantSeiInfo info);
    }

    /** EIS SEI；参数为 {@code bean.EisSeiInfo}。 */
    @FunctionalInterface
    public interface EisSeiInfoCallback {
        /**
         * @param info EIS SEI 信息
         */
        void onResult(EisSeiInfo info);
    }

    /**
     * FlyToPointStart回调接口
     */
    @FunctionalInterface
    public interface FlyToPointStartCallback {
        /**
         * @param error 错误信息
         * @param fly_to_id 任务 ID
         */
        void onResult(FlightHubError error, String fly_to_id);
    }

    /**
     * FlyToDroneSelfPlanPath回调接口
     */
    @FunctionalInterface
    public interface FlyToDroneSelfPlanPathCallback {
        /**
         * @param fly_to_drone_self_plan_path 无人机自主规划路径
         */
        void onResult(FlyToDroneSelfPlanPath fly_to_drone_self_plan_path);
    }

    /**
     * FlyToPointProgress回调接口
     */
    @FunctionalInterface
    public interface FlyToPointProgressCallback {
        /**
         * @param fly_to_point_progress 指点飞行进度
         */
        void onResult(FlyToPointProgress fly_to_point_progress);
    }

    /**
     * PoiEnter回调接口
     */
    @FunctionalInterface
    public interface PoiEnterCallback {
        /**
         * @param error 错误信息
         * @param task_uuid 任务 ID
         */
        void onResult(FlightHubError error, String task_uuid);
    }

    /**
     * PoiGetTask回调接口
     */
    @FunctionalInterface
    public interface PoiGetTaskCallback {
        /**
         * @param error 错误信息
         * @param task POI 任务信息
         */
        void onResult(FlightHubError error, POITaskInfo task);
    }

    /**
     * AiDataInfo 回调；参数为 generated {@code AiDataInfo}。
     */
    @FunctionalInterface
    public interface AiDataInfoCallback {
        /**
         * @param aiDataInfo 机载 AI 数据信息
         */
        void onResult(com.dji.flighthubsdk.generated.AiDataInfo aiDataInfo);
    }

    /**
     * AircraftBatteryInfo回调接口
     */
    @FunctionalInterface
    public interface AircraftBatteryInfoCallback {
        /**
         * @param aircraftBatteryInfo 飞行器电池信息
         */
        void onResult(AircraftBatteryInfo aircraftBatteryInfo);
    }

    /**
     * AircraftAttitude 回调；参数为 {@code bean.AircraftAttitude}。
     */
    @FunctionalInterface
    public interface AircraftAttitudeCallback {
        /**
         * @param aircraftAttitude 飞行器姿态信息
         */
        void onResult(AircraftAttitude aircraftAttitude);
    }

    /**
     * AircraftLocation 回调；参数为 {@code bean.AircraftLocation}。
     */
    @FunctionalInterface
    public interface AircraftLocationCallback {
        /**
         * @param aircraftLocation 飞行器位置信息
         */
        void onResult(AircraftLocation aircraftLocation);
    }

    /**
     * AircraftModeCode 回调；参数为 {@code bean.AircraftModeCode}（含 {@link com.dji.flighthubsdk.value.DroneModeCode}）。
     */
    @FunctionalInterface
    public interface AircraftModeCodeCallback {
        /**
         * @param aircraftModeCode 飞行器模式码
         */
        void onResult(AircraftModeCode aircraftModeCode);
    }

    /**
     * AircraftWindSpeed 回调；参数为 {@code bean.AircraftWindSpeed}。
     */
    @FunctionalInterface
    public interface AircraftWindSpeedCallback {
        /**
         * @param aircraftWindSpeed 飞行器风速信息
         */
        void onResult(AircraftWindSpeed aircraftWindSpeed);
    }

    /**
     * AircraftTakeoffProgress 回调；参数为 {@code bean.AircraftTakeoffProgress}。
     */
    @FunctionalInterface
    public interface AircraftTakeoffProgressCallback {
        /**
         * @param aircraftTakeoffProgress 飞行器起飞进度
         */
        void onResult(AircraftTakeoffProgress aircraftTakeoffProgress);
    }

    /**
     * FlightControlAuthority回调接口
     */
    @FunctionalInterface
    public interface FlightControlAuthorityCallback {
        /**
         * @param state 状态值
         */
        void onResult(boolean state);
    }

    /**
     * PayloadControlAuthority回调接口
     */
    @FunctionalInterface
    public interface PayloadControlAuthorityCallback {
        /**
         * @param state 状态值
         */
        void onResult(boolean state);
    }

    /**
     * CameraState 回调接口；参数为 {@code bean.CameraState}（非 SWIG 生成类型）。
     */
    @FunctionalInterface
    public interface CameraStateCallback {
        /**
         * @param cameraState 相机状态
         */
        void onResult(CameraState cameraState);
    }

    /**
     * 单路直播世界区域回调；参数为 {@code bean.LiveviewWorldRegion}（含 {@code getWorldRegion()} 矩形）。
     */
    @FunctionalInterface
    public interface LiveviewWorldRegionCallback {
        /**
         * @param liveviewWorldRegion 直播世界区域
         */
        void onResult(LiveviewWorldRegion liveviewWorldRegion);
    }

    /**
     * CameraWideLensState 回调接口；参数为 {@code bean.CameraWideLensState}。
     */
    @FunctionalInterface
    public interface CameraWideLensStateCallback {
        /**
         * @param cameraWideLensState 广角镜头状态
         */
        void onResult(CameraWideLensState cameraWideLensState);
    }

    /**
     * CameraZoomLensState 回调接口；参数为 {@code bean.CameraZoomLensState}。
     */
    @FunctionalInterface
    public interface CameraZoomLensStateCallback {
        /**
         * @param cameraZoomLensState 变焦镜头状态
         */
        void onResult(CameraZoomLensState cameraZoomLensState);
    }

    /**
     * 相机测量目标回调；参数为 {@code bean.CameraMeasureTarget}。
     */
    @FunctionalInterface
    public interface CameraMeasureTargetCallback {
        /**
         * @param cameraMeasureTarget 相机测量目标
         */
        void onResult(CameraMeasureTarget cameraMeasureTarget);
    }

    /**
     * 夜景模式状态回调；参数为 {@code bean.NightModeSettings}。
     */
    @FunctionalInterface
    public interface NightModeSettingsCallback {
        /**
         * @param nightModeSettings 夜景模式设置
         */
        void onResult(NightModeSettings nightModeSettings);
    }
    
    /**
     * GimbalState 回调接口；参数为 {@code bean.GimbalState}。
     */
    @FunctionalInterface
    public interface GimbalStateCallback {
        /**
         * @param gimbalState 云台状态
         */
        void onResult(GimbalState gimbalState);
    }

    /**
     * 定时拍照状态回调；参数为 {@code bean.IntervalTakePhotoStatus}。
     */
    @FunctionalInterface
    public interface IntervalTakePhotoStatusCallback {
        /**
         * @param status 定时拍照状态
         */
        void onResult(IntervalTakePhotoStatus status);
    }

    /**
     * 全景状态回调；参数为 {@code bean.PanoramaStatus}。
     */
    @FunctionalInterface
    public interface PanoramaStatusCallback {
        /**
         * @param panoramaStatus 全景状态
         */
        void onResult(PanoramaStatus panoramaStatus);
    }
    
    /**
     * 相机模式可用范围回调接口
     */
    @FunctionalInterface
    public interface CameraModeAvailableRangeCallback {
        /**
         * @param camera_modes 相机模式列表
         */
        void onResult(java.util.List<com.dji.flighthubsdk.value.CameraMode> camera_modes);
    }

    /** 飞机链路状态；参数为 {@code bean.LinkStatus}（含 {@link com.dji.flighthubsdk.value.DroneLinkState}）。 */
    @FunctionalInterface
    public interface LinkStatusCallback {
        /**
         * @param status 链路状态
         */
        void onResult(LinkStatus status);
    }

    /**
     * 无人机在线状态监听回调；参数为对外 {@link com.dji.flighthubsdk.bean.DroneOnlineStatus}（非 SWIG 生成类型）。
     */
    @FunctionalInterface
    public interface DroneOnlineStatusCallback {
        /**
         * @param status 无人机在线状态
         */
        void onResult(DroneOnlineStatus status);
    }

    /**
     * 飞机负载 OSD 信息列表回调；参数为 {@code bean.AircraftPayloadsOsdInfoList}（非 SWIG 类型）。
     */
    @FunctionalInterface
    public interface AircraftPayloadsOsdInfoListCallback {
        /**
         * @param payloads_osd_info_list 负载 OSD 信息列表
         */
        void onResult(AircraftPayloadsOsdInfoList payloads_osd_info_list);
    }

    /**
     * 飞机档位信息；参数为 {@code bean.AircraftGear}。
     */
    @FunctionalInterface
    public interface AircraftGearCallback {
        /**
         * @param gear 飞行器档位信息
         */
        void onResult(AircraftGear gear);
    }

    /**
     * Home 点距离；参数为 {@code bean.AircraftHomeDistance}。
     */
    @FunctionalInterface
    public interface AircraftHomeDistanceCallback {
        /**
         * @param homeDistance Home 点距离信息
         */
        void onResult(AircraftHomeDistance homeDistance);
    }

    /**
     * 搜星状态；参数为 {@code bean.AircraftPositionState}。
     */
    @FunctionalInterface
    public interface AircraftPositionStateCallback {
        /**
         * @param positionState 飞行器定位状态
         */
        void onResult(AircraftPositionState positionState);
    }

    /**
     * 当前控制源；参数为 {@code bean.AircraftControlSource}。
     */
    @FunctionalInterface
    public interface AircraftControlSourceCallback {
        /**
         * @param controlSource 飞行器控制来源
         */
        void onResult(AircraftControlSource controlSource);
    }

    /**
     * 限制类信息；参数为 {@code bean.AircraftLimitsInfo}。
     */
    @FunctionalInterface
    public interface AircraftLimitsInfoCallback {
        /**
         * @param limitsInfo 飞行器限制信息
         */
        void onResult(AircraftLimitsInfo limitsInfo);
    }

    /**
     * 飞机电量告警信息(用户设置的阈值), AircraftBatteryWarningThresholds 回调接口
     */
    @FunctionalInterface
    public interface AircraftBatteryWarningThresholdsCallback {
        /**
         * @param thresholds 电量告警阈值
         */
        void onResult(AircraftBatteryWarningThresholds thresholds);
    }

    /**
     * 固件状态信息；参数为 {@code bean.AircraftFirmwareStatus}。
     */
    @FunctionalInterface
    public interface AircraftFirmwareStatusCallback {
        /**
         * @param firmwareStatus 飞行器固件状态
         */
        void onResult(AircraftFirmwareStatus firmwareStatus);
    }

    /**
     * 机场固件信息, DockFirmwareInfo 回调接口
     */
    @FunctionalInterface
    public interface DockFirmwareInfoCallback {
        /**
         * @param firmware_info 机场固件信息
         */
        void onResult(DockFirmwareInfo firmware_info);
    }

    /**
     * 返航高度模式当前值(current_rth_mode), AircraftCurrentRthMode 回调接口
     */
    @FunctionalInterface
    public interface AircraftCurrentRthModeCallback {
        /**
         * @param rthMode 返航高度模式
         */
        void onResult(AircraftCurrentRthMode rthMode);
    }

    /**
     * 累计飞行信息；参数为 {@code bean.AircraftTotalFlightInfo}。
     */
    @FunctionalInterface
    public interface AircraftTotalFlightInfoCallback {
        /**
         * @param totalFlightInfo 累计飞行信息
         */
        void onResult(AircraftTotalFlightInfo totalFlightInfo);
    }

    /**
     * 飞行器激活时间；参数为 {@code bean.AircraftActivationTime}。
     */
    @FunctionalInterface
    public interface AircraftActivationTimeCallback {
        /**
         * @param activationTime 飞行器激活时间
         */
        void onResult(AircraftActivationTime activationTime);
    }

    /**
     * 保养信息列表；参数为 {@code bean.AircraftMaintainStatusInfo}。
     */
    @FunctionalInterface
    public interface AircraftMaintainStatusInfoCallback {
        /**
         * @param maintainStatusInfo 飞行器保养状态信息
         */
        void onResult(AircraftMaintainStatusInfo maintainStatusInfo);
    }

    /**
     * 夜航灯状态；参数为 {@code bean.AircraftNightLightsState}。
     */
    @FunctionalInterface
    public interface AircraftNightLightsStateCallback {
        /**
         * @param nightLightsState 夜航灯状态
         */
        void onResult(AircraftNightLightsState nightLightsState);
    }

    /**
     * 避障状态；参数为 {@code bean.AircraftObstacleAvoidanceState}。
     */
    @FunctionalInterface
    public interface AircraftObstacleAvoidanceStateCallback {
        /**
         * @param obstacleAvoidance 避障状态
         */
        void onResult(AircraftObstacleAvoidanceState obstacleAvoidance);
    }

    /**
     * 存储容量；参数为 {@code bean.AircraftStorageInfo}。
     */
    @FunctionalInterface
    public interface AircraftStorageInfoCallback {
        /**
         * @param storageInfo 飞行器存储信息
         */
        void onResult(AircraftStorageInfo storageInfo);
    }

    /**
     * 航迹 ID；参数为 {@code bean.AircraftTrackId}。
     */
    @FunctionalInterface
    public interface AircraftTrackIdCallback {
        /**
         * @param trackId 航迹 ID
         */
        void onResult(AircraftTrackId trackId);
    }

    /**
     * 合规信息；参数为 {@code bean.AircraftComplianceInfo}。
     */
    @FunctionalInterface
    public interface AircraftComplianceInfoCallback {
        /**
         * @param info 合规信息
         */
        void onResult(AircraftComplianceInfo info);
    }

    /**
     * 飞行器相机信息(cameras)回调；参数为 {@code bean.AircraftPayloadsCamerasList}。
     */
    @FunctionalInterface
    public interface AircraftPayloadsCamerasListCallback {
        /**
         * @param cameras 负载相机列表
         */
        void onResult(AircraftPayloadsCamerasList cameras);
    }

    /**
     * 遥控器失控动作(rc_lost_action), AircraftRcLostAction 回调接口
     */
    @FunctionalInterface
    public interface AircraftRcLostActionCallback {
        /**
         * @param action 遥控器失控动作
         */
        void onResult(AircraftRcLostAction action);
    }

    /**
     * 返航信息；参数为 {@code bean.AircraftRthInfo}。
     */
    @FunctionalInterface
    public interface AircraftRthInfoCallback {
        /**
         * @param rthInfo 返航信息
         */
        void onResult(AircraftRthInfo rthInfo);
    }

    /**
     * Home 点地理坐标；参数为 {@code bean.AircraftHomeGeographicalInfo}。
     */
    @FunctionalInterface
    public interface AircraftHomeGeographicalInfoCallback {
        /**
         * @param homeGeographicalInfo Home 点地理坐标
         */
        void onResult(AircraftHomeGeographicalInfo homeGeographicalInfo);
    }

    /**
     * 设备 HMS 回调；参数为 {@code bean.HmsData}。
     */
    @FunctionalInterface
    public interface DeviceHmsCallback {
        void onResult(HmsData hms_data);
    }

    /**
     * @deprecated 使用 {@link DeviceHmsCallback}
     */
    @Deprecated
    public interface AircraftHmsCallback extends DeviceHmsCallback {
    }

    /**
     * @deprecated 使用 {@link DeviceHmsCallback}
     */
    @Deprecated
    public interface DockHmsCallback extends DeviceHmsCallback {
    }

    /**
     * DockOnlineStatusCallback 回调接口；参数为 {@code bean.DockOnlineStatus}。
     */
    @FunctionalInterface
    public interface DockOnlineStatusCallback {
        /**
         * @param status 机场在线状态
         */
        void onResult(DockOnlineStatus status);
    }

    /**
     * DockAirConditionerStatusInfoCallback回调接口
     */
    @FunctionalInterface
    public interface DockAirConditionerStatusInfoCallback {
        /**
         * @param status_info 机场空调状态信息
         */
        void onResult(DockAirConditionerStatusInfo status_info);
    }

    /**
     * DockSupplementLightStateCallback回调接口
     */
    @FunctionalInterface
    public interface DockSupplementLightStateCallback {
        /**
         * @param light_state 机场补光灯状态
         */
        void onResult(DockSupplementLightState light_state);
    }

    // ==================== PSDK相关回调接口 ====================

    /**
     * PsdkDeviceListCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkDeviceListCallback {
        /**
         * @param psdk_device_list PSDK 设备列表
         */
        void onResult(PsdkDeviceList psdk_device_list);
    }

    /**
     * PsdkStateSummaryInfoCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkStateSummaryInfoCallback {
        /**
         * @param psdk_state_summary_info PSDK 状态摘要
         */
        void onResult(PsdkStateSummaryInfo psdk_state_summary_info);
    }

    /**
     * PsdkStateSpeakerInfoCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkStateSpeakerInfoCallback {
        /**
         * @param psdk_state_speaker_info PSDK 喊话器状态
         */
        void onResult(PsdkStateSpeakerInfo psdk_state_speaker_info);
    }

    /**
     * PsdkStateSearchlightInfoCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkStateSearchlightInfoCallback {
        /**
         * @param psdk_state_searchlight_info PSDK 探照灯状态
         */
        void onResult(PsdkStateSearchlightInfo psdk_state_searchlight_info);
    }

    /**
     * PsdkSpeakerPlayProgressInfoCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkSpeakerPlayProgressInfoCallback {
        /**
         * @param psdk_speaker_play_progress_info PSDK 喊话器播放进度
         */
        void onResult(PsdkSpeakerPlayProgressInfo psdk_speaker_play_progress_info);
    }

    /**
     * PsdkFloatingWindowTextInfoCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkFloatingWindowTextInfoCallback {
        /**
         * @param psdk_floating_window_text_info PSDK 浮窗文本信息
         */
        void onResult(PsdkFloatingWindowTextInfo psdk_floating_window_text_info);
    }

    /**
     * PsdkSpeakerResourceListCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkSpeakerResourceListCallback {
        /**
         * @param error 错误信息
         * @param resourceList PSDK 喊话器资源列表
         */
        void onResult(FlightHubError error, PsdkSpeakerResourceList resourceList);
    }

    /**
     * PsdkSpeakerResourceCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkSpeakerResourceCallback {
        /**
         * @param error 错误信息
         * @param resource PSDK 喊话器资源
         */
        void onResult(FlightHubError error, PsdkSpeakerResource resource);
    }

    /**
     * STSInfoCallback回调接口
     */
    @FunctionalInterface
    public interface STSInfoCallback {
        /**
         * @param error 错误信息
         * @param stsInfo STS 信息
         */
        void onResult(FlightHubError error, STSInfo stsInfo);
    }

    /**
     * PsdkWidgetResourceListCallback回调接口
     */
    @FunctionalInterface
    public interface PsdkWidgetResourceListCallback {
        /**
         * @param error 错误信息
         * @param resourceList PSDK 控件资源列表
         */
        void onResult(FlightHubError error, PsdkWidgetResourceList resourceList);
    }

    // ==================== 红外增益模式相关回调接口 ====================

    /**
     * LiveVideoInfoCallback回调接口
     */
    @FunctionalInterface
    public interface LiveVideoInfoCallback {
        /**
         * @param info 直播视频信息
         */
        void onResult(LiveVideoInfo info);
    }

    /**
     * ThermalGainModeInfoCallback回调接口
     */
    @FunctionalInterface
    public interface ThermalGainModeInfoCallback {
        /**
         * @param info 红外增益模式信息
         */
        void onResult(ThermalGainModeInfo info);
    }

    /**
     * CameraIrLensStateCallback 回调接口（DRC 上行 ir_lense 全量）
     */
    @FunctionalInterface
    public interface CameraIrLensStateCallback {
        /**
         * @param state 红外镜头状态
         */
        void onResult(com.dji.flighthubsdk.bean.CameraIrLensState state);
    }

    /**
     * CameraIrThermometryInfoCallback 回调接口（DRC 上行 ir_thermometry 全量）
     */
    @FunctionalInterface
    public interface CameraIrThermometryInfoCallback {
        /**
         * @param info 红外测温信息
         */
        void onResult(com.dji.flighthubsdk.bean.CameraIrThermometryInfo info);
    }

    // ── B4 drc_camera_state_push 拆分回调 ──
    @FunctionalInterface public interface CameraWatermarkSettingsCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraWatermarkSettings v);
    }
    @FunctionalInterface public interface CameraStorageInfoCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraStorageInfo v);
    }
    @FunctionalInterface public interface CameraIrPipStateCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraIrPipState v);
    }
    @FunctionalInterface public interface CameraIsothermPolygonCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraIsothermPolygon v);
    }
    @FunctionalInterface public interface CameraMediaStorageCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraMediaStorage v);
    }
    @FunctionalInterface public interface CameraAlgorithmDehazeInfoCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraAlgorithmDehazeInfo v);
    }
    @FunctionalInterface public interface CameraPreRecordedInfoCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraPreRecordedInfo v);
    }
    @FunctionalInterface public interface CameraCapabilityCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraCapability v);
    }
    @FunctionalInterface public interface CameraSettableCapabilityCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraSettableCapability v);
    }
    @FunctionalInterface public interface CameraVisibleCapabilityCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraVisibleCapability v);
    }

    // ── B4.1 drc_camera_photo_info_push 全量透传回调 ──
    @FunctionalInterface public interface CameraPhotoInfoCallback {
        void onResult(com.dji.flighthubsdk.bean.CameraPhotoInfo v);
    }

    // ==================== 飞行任务异常和设备变更相关回调接口 ====================

    /**
     * FlightTaskExceptionCallback回调接口
     */
    @FunctionalInterface
    public interface FlightTaskExceptionCallback {
        /**
         * @param exception 飞行任务异常信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightTaskException exception);
    }

    /**
     * WorkspaceDeviceChangeCallback回调接口
     */
    @FunctionalInterface
    public interface WorkspaceDeviceChangeCallback {
        /**
         * @param change 工作空间设备变更
         */
        void onResult(WorkspaceDeviceChange change);
    }

    /**
     * ImportWaylineEventCallback回调接口
     */
    @FunctionalInterface
    public interface ImportWaylineEventCallback {
        /**
         * @param result 航线导入事件结果
         */
        void onResult(ImportWaylineEventResult result);
    }

    /**
     * DeviceTopoChangeCallback回调接口
     */
    @FunctionalInterface
    public interface DeviceTopoChangeCallback {
        /**
         * @param change 设备拓扑变更
         */
        void onResult(DeviceTopoChange change);
    }

    /**
     * 飞机控制码回调接口
     */
    @FunctionalInterface
    public interface AircraftControlCodeCallback {
        /**
         * @param controlCode 飞行器控制码
         */
        void onResult(AircraftControlCode controlCode);
    }

    /**
     * 飞机速度回调接口；参数为 {@code bean.AircraftSpeed}。
     */
    @FunctionalInterface
    public interface AircraftSpeedCallback {
        /**
         * @param speed 飞行器速度信息
         */
        void onResult(AircraftSpeed speed);
    }

    /**
     * 载荷相机实时视图世界区域回调接口；参数为 {@code bean.AircraftPayloadsCameraLiveviewWorldRegion}。
     */
    @FunctionalInterface
    public interface AircraftPayloadsCameraLiveviewWorldRegionCallback {
        /**
         * @param region 负载相机实时视图世界区域
         */
        void onResult(com.dji.flighthubsdk.bean.AircraftPayloadsCameraLiveviewWorldRegion region);
    }

    /**
     * 载荷云台姿态回调接口；参数为 {@code bean.AircraftPayloadsGimbalAttitude}。
     */
    @FunctionalInterface
    public interface AircraftPayloadsGimbalAttitudeCallback {
        /**
         * @param attitude 负载云台姿态
         */
        void onResult(com.dji.flighthubsdk.bean.AircraftPayloadsGimbalAttitude attitude);
    }

    /**
     * 载荷列表回调接口；参数为 {@code bean.AircraftPayloadsList}。
     */
    @FunctionalInterface
    public interface AircraftPayloadsListCallback {
        /**
         * @param payloadsList 负载列表
         */
        void onResult(com.dji.flighthubsdk.bean.AircraftPayloadsList payloadsList);
    }

    /**
     * 控制权限云控制状态回调接口
     */
    @FunctionalInterface
    public interface ControlRightsCloudControlStatusCallback {
        /**
         * @param status 云控制权限状态
         */
        void onResult(com.dji.flighthubsdk.bean.ControlRightsCloudControlStatus status);
    }

    /**
     * 控制权限设备控制状态回调接口
     */
    @FunctionalInterface
    public interface ControlRightsDeviceControlStatusCallback {
        /**
         * @param status 设备控制权限状态
         */
        void onResult(com.dji.flighthubsdk.bean.ControlRightsDeviceControlStatus status);
    }

    /**
     * 飞行任务媒体文件显示可用回调接口
     */
    @FunctionalInterface
    public interface FlightTasksMediaFileDisplayAvailableCallback {
        /**
         * @param available 媒体文件展示可用信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightTasksMediaFileDisplayAvailable available);
    }

    /**
     * 指令任务事件回调接口
     */
    @FunctionalInterface
    public interface CommandTaskEventCallback {
        /**
         * @param event 指令任务事件
         */
        void onResult(com.dji.flighthubsdk.bean.CommandTaskEvent event);
    }

    /**
     * 指点飞行任务事件回调接口
     */
    @FunctionalInterface
    public interface FlyToTaskEventCallback {
        /**
         * @param event 指点飞行任务事件
         */
        void onResult(com.dji.flighthubsdk.bean.FlyToTaskEvent event);
    }

    /**
     * POI 任务事件回调接口
     */
    @FunctionalInterface
    public interface PoiTaskEventCallback {
        /**
         * @param event POI 任务事件
         */
        void onResult(com.dji.flighthubsdk.bean.PoiTaskEvent event);
    }

    /**
     * 航线任务事件回调接口
     */
    @FunctionalInterface
    public interface WaylineTaskEventCallback {
        /**
         * @param event 航线任务事件
         */
        void onResult(com.dji.flighthubsdk.bean.WaylineTaskEvent event);
    }

    /**
     * 机场位置回调接口
     */
    @FunctionalInterface
    public interface DockLocationCallback {
        /**
         * @param location 机场位置信息
         */
        void onResult(DockLocation location);
    }

    /**
     * 机场中的无人机回调接口
     */
    @FunctionalInterface
    public interface DroneInDockCallback {
        /**
         * @param drone_in_dock 机场内无人机信息
         */
        void onResult(DroneInDock drone_in_dock);
    }

    /**
     * 机场舱盖状态回调接口
     */
    @FunctionalInterface
    public interface DockCoverStateCallback {
        /**
         * @param dock_cover_state 机场舱盖状态
         */
        void onResult(DockCoverState dock_cover_state);
    }

    /**
     * 机场 DockModeCode 状态回调接口
     */
    @FunctionalInterface
    public interface DockModeCodeCallback {
        /**
         * @param dock_mode_code 机场模式码
         */
        void onResult(DockModeCode dock_mode_code);
    }

    /**
     * 机场子设备信息列表(sub_device)回调接口;
     */
    @FunctionalInterface
    public interface DockSubDeviceInfoListCallback {
        /**
         * @param dock_sub_device_info_list 子设备信息列表
         */
        void onResult(DockSubDeviceInfoList dock_sub_device_info_list);
    }

    /**
     * 机场网络状态(network_state)回调接口;
     */
    @FunctionalInterface
    public interface DockNetworkStateCallback {
        /**
         * @param dock_network_state 机场网络状态
         */
        void onResult(DockNetworkState dock_network_state);
    }

    /**
     * 机场媒体文件数量(media_file_detail)回调接口;
     */
    @FunctionalInterface
    public interface DockMediaFileDetailCallback {
        /**
         * @param dock_media_file_detail 媒体文件详情
         */
        void onResult(DockMediaFileDetail dock_media_file_detail);
    }

    /**
     * 机场图传质量(wireless_link)回调接口;
     */
    @FunctionalInterface
    public interface DockWirelessLinkCallback {
        /**
         * @param dock_wireless_link 图传质量信息
         */
        void onResult(DockWirelessLink dock_wireless_link);
    }

    /**
     * 机场作业次数(job_number)回调接口;
     */
    @FunctionalInterface
    public interface DockJobNumberCallback {
        /**
         * @param dock_job_number 作业次数信息
         */
        void onResult(DockJobNumber dock_job_number);
    }

    /**
     * 飞行任务步骤码(flighttask_step_code)回调接口;
     */
    @FunctionalInterface
    public interface FlightTaskStepCodeCallback {
        /**
         * @param flight_task_step_code 飞行任务步骤码
         */
        void onResult(FlightTaskStepCode flight_task_step_code);
    }

    /**
     * 机场：DockPowerState 回调接口;
     */
    @FunctionalInterface
    public interface DockPowerStateCallback {
        /**
         * @param dock_power_state 机场电源状态
         */
        void onResult(DockPowerState dock_power_state);
    }

    /**
     * DockStorageState 回调接口;
     */
    @FunctionalInterface
    public interface DockStorageStateCallback {
        /**
         * @param dock_storage_state 机场存储状态
         */
        void onResult(DockStorageState dock_storage_state);
    }

    /**
     * 机场首次上电时间(first_power_on) 回调接口;
     */
    @FunctionalInterface
    public interface DockFirstPowerOnTimeCallback {
        /**
         * @param dock_first_power_on_time 首次上电时间
         */
        void onResult(DockFirstPowerOnTime dock_first_power_on_time);
    }

    /**
     * 机场累计运行时长(acc_time) 回调接口;
     */
    @FunctionalInterface
    public interface DockAccTimeCallback {
        /**
         * @param dock_acc_time 累计运行时长
         */
        void onResult(DockAccTime dock_acc_time);
    }

    /**
     * 备降点(alternate_land_point) 回调接口;
     */
    @FunctionalInterface
    public interface DockAlternateLandPointCallback {
        /**
         * @param landPoint 备降点信息
         */
        void onResult(DockAlternateLandPoint landPoint);
    }

    /**
     * 搜星状态(position_state) 回调接口;
     */
    @FunctionalInterface
    public interface DockPositionStateCallback {
        /**
         * @param dock_position_state 机场定位状态
         */
        void onResult(DockPositionState dock_position_state);
    }

    /**
     * 机场激活时间(activation_time) 回调接口;
     */
    @FunctionalInterface
    public interface DockActivationTimeCallback {
        /**
         * @param dock_activation_time 机场激活时间
         */
        void onResult(DockActivationTime dock_activation_time);
    }

    /**
     *  电池(存储)模式(battery_store_mode) 回调接口;
     */
    @FunctionalInterface
    public interface DockBatteryStoreModeCallback {
        /**
         * @param dock_battery_store_mode 电池存储模式
         */
        void onResult(DockBatteryStoreMode dock_battery_store_mode);
    }

    /**
     *  机场声光报警状态(alarm_state) 回调接口;
     */
    @FunctionalInterface
    public interface DockAlarmStateCallback {
        /**
         * @param dock_alarm_state 声光报警状态
         */
        void onResult(DockAlarmState dock_alarm_state);
    }

    /**
     * 机场: 飞行器电池保养信息(drone_battery_maintenance_info) 回调接口;
     * DockDroneBatteryMaintenanceInfo;
     */
    @FunctionalInterface
    public interface DockDroneBatteryMaintenanceInfoCallback {
        /**
         * @param dock_drone_battery_maintenance_info 无人机电池保养信息
         */
        void onResult(DockDroneBatteryMaintenanceInfo dock_drone_battery_maintenance_info);
    }

    /**
     * 机场: 紧急停止按钮状态(emergency_stop_state) 回调接口;
     * DockEmergencyStopState;
     */
    @FunctionalInterface
    public interface DockEmergencyStopStateCallback {
        /**
         * @param state 紧急停止状态
         */
        void onResult(DockEmergencyStopState state);
    }

    /**
     * 机场: 机场静音模式(silent_mode) 回调接口;
     * DockSilentMode;
     */
    @FunctionalInterface
    public interface DockSilentModeCallback {
        /**
         * @param state 静音模式状态
         */
        void onResult(DockSilentMode state);
    }

    /**
     * 机场: 用户体验改善计划(user_experience_improvement);
     * DockUserExperienceImprovement;
     */
    @FunctionalInterface
    public interface DockUserExperienceImprovementCallback {
        /**
         * @param state 用户体验改善计划
         */
        void onResult(DockUserExperienceImprovement state);
    }

    /**
     * 机场: DRC 链路的状态(drc_state);
     * DockDrcState;
     */
    @FunctionalInterface
    public interface DockDrcStateCallback {
        /**
         * @param state DRC 链路状态
         */
        void onResult(DockDrcState state);
    }

    /**
     * 机场: 推杆状态(putter_state);
     * DockPutterState;
     */
    @FunctionalInterface
    public interface DockPutterStateCallback {
        /**
         * @param state 推杆状态
         */
        void onResult(DockPutterState state);
    }

    /**
     * 机场: 相对备降点(relative_alternate_land_point);
     * DockRelativeAlternateLandPoint;
     */
    @FunctionalInterface
    public interface DockRelativeAlternateLandPointCallback {
        /**
         * @param land_point 相对备降点
         */
        void onResult(DockRelativeAlternateLandPoint land_point);
    }

    /**
     * 机场: 自收敛坐标(self_converge_coordinate);
     * DockSelfConvergeCoordinate;
     */
    @FunctionalInterface
    public interface DockSelfConvergeCoordinateCallback {
        /**
         * @param land_point 自收敛坐标
         */
        void onResult(DockSelfConvergeCoordinate land_point);
    }

    /**
     * 机场无人机充电状态回调接口
     */
    @FunctionalInterface
    public interface DockDroneChargeStateCallback {
        /**
         * @param dock_drone_charge_state 无人机充电状态
         */
        void onResult(DockDroneChargeState dock_drone_charge_state);
    }

    /**
     * 机场气象信息回调接口
     */
    @FunctionalInterface
    public interface DockMeteorologicalInfoCallback {
        /**
         * @param dock_meteorological_info 气象信息
         */
        void onResult(DockMeteorologicalInfo dock_meteorological_info);
    }

    /**
     * 机场备用电池信息回调接口
     */
    @FunctionalInterface
    public interface DockBackupBatteryInfoCallback {
        /**
         * @param dock_backup_battery_info 备用电池信息
         */
        void onResult(DockBackupBatteryInfo dock_backup_battery_info);
    }

    /**
     * 机场保养维护信息回调接口
     */
    @FunctionalInterface
    public interface DockMaintainStatusInfoCallback {
        /**
         * @param dock_maintain_status_info 保养维护信息
         */
        void onResult(DockMaintainStatusInfo dock_maintain_status_info);
    }

    /** 打开调试模式回调 */
    @FunctionalInterface
    public interface OpenDebugModeCallback {
        /**
         * @param error 错误信息
         * @param result 开启调试模式结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDebugModeResult result);
    }

    /** 关闭调试模式回调 */
    @FunctionalInterface
    public interface CloseDebugModeCallback {
        /**
         * @param error 错误信息
         * @param result 关闭调试模式结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDebugModeResult result);
    }

    /** 打开机场舱盖回调 */
    @FunctionalInterface
    public interface OpenDockCoverCallback {
        /**
         * @param error 错误信息
         * @param result 打开舱盖结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDockCoverResult result);
    }

    /** 打开机场舱盖进度回调 */
    @FunctionalInterface
    public interface OpenDockCoverProgressCallback {
        /**
         * @param error 错误信息
         * @param result 打开舱盖进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDockCoverProgressResult result);
    }

    /** 关闭机场舱盖回调 */
    @FunctionalInterface
    public interface CloseDockCoverCallback {
        /**
         * @param error 错误信息
         * @param result 关闭舱盖结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDockCoverResult result);
    }

    /** 关闭机场舱盖进度回调 */
    @FunctionalInterface
    public interface CloseDockCoverProgressCallback {
        /**
         * @param error 错误信息
         * @param result 关闭舱盖进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDockCoverProgressResult result);
    }

    /** 设备重启回调 */
    @FunctionalInterface
    public interface DeviceRebootCallback {
        /**
         * @param error 错误信息
         * @param result 设备重启结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceRebootResult result);
    }

    /** 设备重启进度回调 */
    @FunctionalInterface
    public interface DeviceRebootProgressCallback {
        /**
         * @param error 错误信息
         * @param result 设备重启进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceRebootProgressResult result);
    }

    /** 设备格式化回调 */
    @FunctionalInterface
    public interface DeviceFormatCallback {
        /**
         * @param error 错误信息
         * @param result 设备格式化结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceFormatResult result);
    }

    /** 设备格式化进度回调 */
    @FunctionalInterface
    public interface DeviceFormatProgressCallback {
        /**
         * @param error 错误信息
         * @param result 设备格式化进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceFormatProgressResult result);
    }

    /** 开启无人机回调 */
    @FunctionalInterface
    public interface OpenDroneCallback {
        /**
         * @param error 错误信息
         * @param result 开启无人机结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDroneResult result);
    }

    /** 关闭无人机回调 */
    @FunctionalInterface
    public interface CloseDroneCallback {
        /**
         * @param error 错误信息
         * @param result 关闭无人机结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDroneResult result);
    }

    /** 开启充电回调 */
    @FunctionalInterface
    public interface OpenChargeCallback {
        /**
         * @param error 错误信息
         * @param result 开启充电结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenChargeResult result);
    }

    /** 关闭充电回调 */
    @FunctionalInterface
    public interface CloseChargeCallback {
        /**
         * @param error 错误信息
         * @param result 关闭充电结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseChargeResult result);
    }

    /** 机场设备状态列表回调 */
    @FunctionalInterface
    public interface DockDeviceStatusListCallback {
        /**
         * @param error 错误信息
         * @param items 机场设备状态列表
         */
        void onResult(FlightHubError error, java.util.List<DockStatusItemResult> items);
    }

    /** 无人机设备状态列表回调 */
    @FunctionalInterface
    public interface DroneDeviceStatusListCallback {
        /**
         * @param error 错误信息
         * @param items 无人机设备状态列表
         */
        void onResult(FlightHubError error, java.util.List<DroneStatusItemResult> items);
    }

    /** 获取直播观众数量回调 */
    @FunctionalInterface
    public interface GetAudienceCountCallback {
        /**
         * @param error 错误信息
         * @param items 设备观众数量列表
         */
        void onResult(FlightHubError error, java.util.List<DeviceAudienceCountItem> items);
    }

    /** 航线任务列表回调 */
    @FunctionalInterface
    public interface GetFlightTaskListCallback {
        /**
         * @param error 错误信息
         * @param result 航线任务列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlightTaskListResult result);
    }

    /** 一键起飞任务列表回调 */
    @FunctionalInterface
    public interface GetCommandTaskListCallback {
        /**
         * @param error 错误信息
         * @param results 一键起飞任务列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetCommandTaskListResults results);
    }

    /** 查询飞行任务轨迹回调 */
    @FunctionalInterface
    public interface GetFlightTaskTrackCallback {
        /**
         * @param error 错误信息
         * @param results 飞行任务轨迹结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetFlightTaskTrackResults results);
    }

    /** 创建/编辑航线任务回调 */
    @FunctionalInterface
    public interface CreateFlightTaskCallback {
        /**
         * @param error 错误信息
         * @param result 创建任务响应
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateTaskResp result);
    }

    /** 航线任务详情回调 */
    @FunctionalInterface
    public interface GetFlightTaskDetailCallback {
        /**
         * @param error 错误信息
         * @param result 任务详情
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TaskDetail result);
    }

    /** 设备推荐回调 */
    @FunctionalInterface
    public interface GetDeviceRecommendationCallback {
        /**
         * @param error 错误信息
         * @param result 设备推荐响应
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceRecommendationResp result);
    }

    /** 设备 TCA 回调 */
    @FunctionalInterface
    public interface GetDeviceTcasCallback {
        /**
         * @param error 错误信息
         * @param result 设备 TCA 信息响应
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetTCAInfosResp result);
    }

    /** 开始漫游回调。 */
    @FunctionalInterface
    public interface StartRoamingCallback {
        void onResult(FlightHubError error);
    }

    /** 设置漫游基站白名单回调。 */
    @FunctionalInterface
    public interface SetRoamingStationSnCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetRoamingStationSnResult result);
    }

    /** 查询飞行中任务拓扑回调。 */
    @FunctionalInterface
    public interface GetInFlightTaskToposCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetInFlightTaskToposResult result);
    }

    /** 漫游任务拓扑变更推送回调。 */
    @FunctionalInterface
    public interface TaskTopoChangedCallback {
        void onResult(com.dji.flighthubsdk.bean.TaskTopoChangedEvent event);
    }

    // ==================== 媒体服务模块相关回调接口 ====================

    /** 获取媒体文件列表回调 */
    @FunctionalInterface
    public interface GetMediaFilesListCallback {
        /**
         * @param error 错误信息
         * @param results 媒体文件列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesListResults results);
    }

    /** 获取媒体文件下载链接回调 */
    @FunctionalInterface
    public interface GetMediaFilesDownloadUrlCallback {
        /**
         * @param error 错误信息
         * @param results 媒体文件下载链接结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults results);
    }

    /** 获取媒体文件批量下载链接回调 */
    @FunctionalInterface
    public interface GetMediaFilesDownloadUrlListCallback {
        /**
         * @param error 错误信息
         * @param results 媒体文件批量下载链接结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults results);
    }

    /** 媒体文件后处理事件回调 */
    @FunctionalInterface
    public interface MediaFilePostProcessCallback {
        /**
         * @param msg 媒体文件后处理消息
         */
        void onResult(com.dji.flighthubsdk.bean.MediaFilePostProcess msg);
    }

    /** 媒体文件设备上传事件回调 */
    @FunctionalInterface
    public interface MediaFileDeviceUploadCallback {
        /**
         * @param msg 媒体文件设备上传消息
         */
        void onResult(com.dji.flighthubsdk.bean.MediaFileDeviceUpload msg);
    }

    /** 获取媒体上传 STS 凭证回调 */
    @FunctionalInterface
    public interface GetMediaUploadStsCallback {
        /**
         * @param error 错误信息
         * @param result 媒体上传 STS 凭证结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaUploadStsResult result);
    }

    /** 媒体上传通知回调 */
    @FunctionalInterface
    public interface MediaUploadCallbackCallback {
        /**
         * @param error 错误信息
         * @param result 媒体上传通知结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.MediaUploadCallbackResult result);
    }

    /** 上传地理标注媒体资源（图片备注）回调 */
    @FunctionalInterface
    public interface UploadGeoMediaFileCallback {
        /**
         * @param error  错误信息
         * @param result 上传结果（file_id/file_uuid/media_file_id）
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UploadGeoMediaFileResult result);
    }

    /** 根据文件 ID 获取媒体文件详情（轮询文件 URL）回调 */
    @FunctionalInterface
    public interface GetMediaFileDetailByIdCallback {
        /**
         * @param error  错误信息
         * @param result 媒体文件详情（url/thumbnail_url 等）
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFileDetailByIdResult result);
    }

    /** 获取航线编辑器 STS 临时上传凭证回调 */
    @FunctionalInterface
    public interface GetWaylineEditorStsCallback {
        /**
         * @param error  错误信息
         * @param result STS 凭证（credentials/bucket/endpoint 等）
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetWaylineEditorStsResult result);
    }

    // ==================== Terra Build 模块相关回调接口 ====================

    /** 获取重建设置回调 */
    @FunctionalInterface
    public interface TerraBuildGetReconstructionSettingsCallback {
        /**
         * @param error 错误信息
         * @param result 重建设置结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildSettingsResult result);
    }

    /** 获取质量报告回调 */
    @FunctionalInterface
    public interface TerraBuildGetQualityReportCallback {
        /**
         * @param error 错误信息
         * @param result 质量报告结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildQualityReportResult result);
    }

    /** 获取模型文件列表回调 */
    @FunctionalInterface
    public interface TerraBuildGetModelFileListCallback {
        /**
         * @param error 错误信息
         * @param result 模型文件列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildModelFileListResult result);
    }

    /** 获取模型文件下载 URL 回调 */
    @FunctionalInterface
    public interface TerraBuildGetModelFileUrlCallback {
        /**
         * @param error 错误信息
         * @param result 模型文件下载 URL 结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildModelFileUrlResult result);
    }

    /** 创建重建任务回调 */
    @FunctionalInterface
    public interface TerraBuildCreateReconstructionCallback {
        /**
         * @param error 错误信息
         * @param result 创建重建任务结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionResult result);
    }

    /** 创建全景图回调 */
    @FunctionalInterface
    public interface TerraBuildCreatePanoramaCallback {
        /**
         * @param error 错误信息
         * @param result 创建全景图结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildCreatePanoramaResult result);
    }

    /** 获取 SFM 结果回调 */
    @FunctionalInterface
    public interface TerraBuildGetSfmResultCallback {
        /**
         * @param error 错误信息
         * @param result SFM 结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildSfmResult result);
    }

    /** 获取重建任务图片列表回调 */
    @FunctionalInterface
    public interface TerraBuildGetReconstructionImagesCallback {
        /**
         * @param error 错误信息
         * @param result 重建任务图片列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraBuildReconstructionImagesResult result);
    }

    /** 重建状态变更监听回调 */
    @FunctionalInterface
    public interface TerraBuildReconstructionStatusChangedCallback {
        /**
         * @param msg 重建状态变更消息
         */
        void onResult(com.dji.flighthubsdk.bean.TerraBuildReconstructionStatusChanged msg);
    }

    /** 模型上传回调接口回调 */
    @FunctionalInterface
    public interface TerraUploadCallbackCallback {
        /**
         * @param error  错误信息
         * @param result 上传回调结果（file id/uuid/name）
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraUploadCallbackResult result);
    }

    /** 模型重命名回调 */
    @FunctionalInterface
    public interface TerraRenameModelCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 加载/取消加载模型到地图回调 */
    @FunctionalInterface
    public interface TerraShowOnMapCallback {
        /**
         * @param error  错误信息
         * @param result 实际操作的文件 ID 列表
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraShowOnMapResult result);
    }

    /** 模型解压进度 WS 推送监听回调 (topic_reconstruction_unzip_progress) */
    @FunctionalInterface
    public interface TerraUnzipProgressCallback {
        /**
         * @param msg 解压进度消息
         */
        void onResult(com.dji.flighthubsdk.bean.TerraUnzipProgressMsg msg);
    }

    /** 模型状态变更 WS 推送监听回调 (topic_model_status_changed) */
    @FunctionalInterface
    public interface TerraModelStatusChangedCallback {
        /**
         * @param msg 模型状态变更消息
         */
        void onResult(com.dji.flighthubsdk.bean.TerraModelStatusChangedMsg msg);
    }

    /** 文件展示地图 WS 推送监听回调 (topic_model_file_show_on_map) */
    @FunctionalInterface
    public interface TerraFileShowOnMapCallback {
        /**
         * @param msg 文件展示地图消息
         */
        void onResult(com.dji.flighthubsdk.bean.TerraFileShowOnMapMsg msg);
    }

    /** 模型隐藏地图 WS 推送监听回调 (topic_media_file_hide_on_map) */
    @FunctionalInterface
    public interface TerraMediaFileHideOnMapCallback {
        /**
         * @param msg 隐藏地图消息（含被隐藏的文件 ID 列表）
         */
        void onResult(com.dji.flighthubsdk.bean.TerraMediaFileHideOnMapMsg msg);
    }

    /** 模型显示地图 WS 推送监听回调 (topic_media_file_show_on_map) */
    @FunctionalInterface
    public interface TerraMediaFileShowOnMapCallback {
        /**
         * @param msg 显示地图消息（含上图文件列表）
         */
        void onResult(com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapMsg msg);
    }

    /** 获取模型文件详情回调 */
    @FunctionalInterface
    public interface TerraGetModelDetailCallback {
        /**
         * @param error  错误信息
         * @param result 模型文件详情
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TerraModelDetailResult result);
    }

    /** 设置 LAS 坐标系并触发格式转换回调 */
    @FunctionalInterface
    public interface TerraSetLasWktCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    // ==================== 安全服务模块相关回调接口 ====================

    /** 搜索信号地图回调 */
    @FunctionalInterface
    public interface SearchSignalMapsCallback {
        /**
         * @param error 错误信息
         * @param result 信号地图搜索结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SignalMapsResults result);
    }

    /** 创建自定义飞行区域回调 */
    @FunctionalInterface
    public interface CreateCustomizedFlightAreasCallback {
        /**
         * @param error 错误信息
         * @param result 创建飞行区域结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateFlightAreasResults result);
    }

    /** 获取自定义飞行区域回调 */
    @FunctionalInterface
    public interface GetCustomizedFlightAreasCallback {
        /**
         * @param error 错误信息
         * @param result 飞行区域结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetFlightAreasResults result);
    }

    /** 更新自定义飞行区域回调 */
    @FunctionalInterface
    public interface UpdateCustomizedFlightAreasCallback {
        /**
         * @param error 错误信息
         * @param result 更新飞行区域结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UpdateFlightAreasResults result);
    }

    /** 同步自定义飞行区域到设备回调 */
    @FunctionalInterface
    public interface SyncCustomizedFlightAreasCallback {
        /**
         * @param error 错误信息
         * @param result 同步飞行区域结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SyncFlightAreasResults result);
    }

    /** 获取飞行区域同步状态回调 */
    @FunctionalInterface
    public interface GetFlightAreasSyncStatusCallback {
        /**
         * @param error 错误信息
         * @param result 飞行区域同步状态结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetAreasSyncStatusResults result);
    }

    /** 获取离线地图模型信息回调 */
    @FunctionalInterface
    public interface GetOfflineMapsModelInfoCallback {
        /**
         * @param error 错误信息
         * @param result 离线地图模型信息结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults result);
    }

    /** 同步离线地图到设备回调 */
    @FunctionalInterface
    public interface SyncOfflineMapsCallback {
        /**
         * @param error 错误信息
         * @param result 离线地图同步结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OfflineMapsSyncResults result);
    }

    /** 获取离线地图同步状态回调 */
    @FunctionalInterface
    public interface GetOfflineMapsSyncStatusCallback {
        /**
         * @param error 错误信息
         * @param result 离线地图同步状态结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetOfflineMapsSyncStatusResults result);
    }

    // ==================== 飞行任务记录模块相关回调接口 ====================

    /** 导出飞行记录回调 */
    @FunctionalInterface
    public interface ExportFlightRecordsCallback {
        /**
         * @param error 错误信息
         * @param results 导出飞行记录结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ExportFlightRecordsResults results);
    }

    /** 获取导出飞行记录进度回调 */
    @FunctionalInterface
    public interface GetExportFlightRecordsProgressCallback {
        /**
         * @param error 错误信息
         * @param results 导出飞行记录进度结果
         */
        void onResult(
                FlightHubError error, com.dji.flighthubsdk.bean.GetExportFlightRecordsProgressResults results);
    }

    /** 启动网络测试回调 */
    @FunctionalInterface
    public interface StartNetworkTestCallback {
        /**
         * @param error 错误信息
         * @param result 网络测试进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.NetworkTestProgressResult result);
    }

    /** 停止网络测试回调 */
    @FunctionalInterface
    public interface StopNetworkTestCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 获取网络测试进度回调 */
    @FunctionalInterface
    public interface GetNetworkTestProgressCallback {
        /**
         * @param error 错误信息
         * @param result 网络测试进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.NetworkTestProgressResult result);
    }

    /** 订阅网络测速进度推送回调 */
    public interface SubscribeNetworkTestProgressCallback {
        /**
         * @param result 网络测试进度结果
         */
        void onResult(com.dji.flighthubsdk.bean.NetworkTestProgressResult result);
    }

    /** RTK 标定回调 */
    @FunctionalInterface
    public interface RtkCalibrationCallback {
        /**
         * @param error 错误信息
         * @param result RTK 标定结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.RtkCalibrationResult result);
    }

    /** 设置限远回调 */
    @FunctionalInterface
    public interface SetDistanceLimitCallback {
        /**
         * @param error 错误信息
         * @param result 设置限远结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetDistanceLimitResult result);
    }

    /** 设置限高回调 */
    @FunctionalInterface
    public interface SetHeightLimitCallback {
        /**
         * @param error 错误信息
         * @param result 设置限高结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetHeightLimitResult result);
    }

    /** 设置避障回调 */
    @FunctionalInterface
    public interface SetObstacleAvoidanceCallback {
        /**
         * @param error 错误信息
         * @param result 设置避障结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetObstacleAvoidanceResult result);
    }

    /** 设置返航电量回调 */
    @FunctionalInterface
    public interface SetRemainingPowerForRthCallback {
        /**
         * @param error 错误信息
         * @param result 设置返航电量结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetRemainingPowerForRthResult result);
    }

    /** 设置夜航灯状态回调 */
    @FunctionalInterface
    public interface SetNightLightsStateCallback {
        /**
         * @param error 错误信息
         * @param result 设置夜航灯结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetNightLightsStateResult result);
    }

    /** 切换声光报警状态回调 */
    @FunctionalInterface
    public interface SwitchAlarmLightStateCallback {
        /**
         * @param error 错误信息
         * @param result 声光报警切换结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SwitchAlarmStateResult result);
    }

    /** eSIM 运营商切换回调 */
    @FunctionalInterface
    public interface EsimOperatorSwitchCallback {
        /**
         * @param error 错误信息
         * @param result eSIM 运营商切换结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.EsimOperatorSwitchResult result);
    }

    /** SIM 卡槽切换回调 */
    @FunctionalInterface
    public interface SimSlotSwitchCallback {
        /**
         * @param error 错误信息
         * @param result SIM 卡槽切换结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SimSlotSwitchResult result);
    }

    /** 设置电池存储模式回调 */
    @FunctionalInterface
    public interface SetBatteryStoreModeCallback {
        /**
         * @param error 错误信息
         * @param result 设置电池存储模式结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetBatteryStoreModeResult result);
    }

    /** 发起手动换电回调 */
    @FunctionalInterface
    public interface ManualBatterySwapCallback {
        /**
         * @param error 错误信息
         * @param result 换电发起结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ManualBatterySwapResult result);
    }

    /** 查询手动换电进度回调 */
    @FunctionalInterface
    public interface ManualBatterySwapProgressCallback {
        /**
         * @param error 错误信息
         * @param result 换电进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ManualBatterySwapProgressResult result);
    }

    /** Dongle 电源开关回调 */
    @FunctionalInterface
    public interface DonglePowerSwitchCallback {
        /**
         * @param error 错误信息
         * @param result Dongle 电源开关结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DonglePowerSwitchResult result);
    }

    /** 机场低功耗开关回调 */
    @FunctionalInterface
    public interface LowPowerSwitchCallback {
        /**
         * @param error 错误信息
         * @param result 低功耗开关结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LowPowerSwitchResult result);
    }

    /** eSIM 运营商切换进度回调 */
    @FunctionalInterface
    public interface EsimOperatorSwitchProgressCallback {
        /**
         * @param error 错误信息
         * @param result eSIM 运营商切换进度结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.EsimOperatorSwitchProgressResult result);
    }

    /** 设置中继站广播隐藏回调 */
    @FunctionalInterface
    public interface SetRelayBroadcastHideCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 设置 Geo Caging 开关回调 */
    @FunctionalInterface
    public interface SetGeoCagingEnableCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 设置中继站 SSID 回调 */
    @FunctionalInterface
    public interface SetRelaySSIDCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 设置中继站密码回调 */
    @FunctionalInterface
    public interface SetRelayPasswordCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 设置中继站搜索模式回调 */
    @FunctionalInterface
    public interface SetRelaySearchModeCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 设置中继站占用状态回调, 中继站调试接口(有 data：bid+status); */
    @FunctionalInterface
    public interface SetRelayOccupiedStatusCallback {
        /**
         * @param error 错误信息
         * @param result 设置中继站占用状态结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SetRelayOccupiedStatusResult result);
    }

    /** 中继站重启回调 */
    @FunctionalInterface
    public interface RelayRebootCallback {
        /**
         * @param error 错误信息
         * @param result 中继站重启结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.RelayRebootResult result);
    }

    /** 中继站断连回调 */
    @FunctionalInterface
    public interface RelayDisconnectCallback {
        /**
         * @param error 错误信息
         * @param result 中继站断连结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.RelayDisconnectResult result);
    }

    /** 查询历史 HMS 数据回调 */
    @FunctionalInterface
    public interface QueryHistoryHMSDataCallback {
        /**
         * @param error 错误信息
         * @param result 历史 HMS 数据结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.HistoryHmsDataResult result);
    }

    /** 查询设备日志列表回调 */
    @FunctionalInterface
    public interface QueryDeviceLogListCallback {
        /**
         * @param error 错误信息
         * @param result 设备日志列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceLogListResult result);
    }

    /** 创建设备日志上报任务回调 */
    @FunctionalInterface
    public interface CreateDeviceLogReportCallback {
        /**
         * @param error 错误信息
         * @param result 创建设备日志上报结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateDeviceLogReportResult result);
    }

    /** 日志上报列表回调 */
    @FunctionalInterface
    public interface LogReportListCallback {
        /**
         * @param error 错误信息
         * @param result 日志上报列表结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LogReportListResult result);
    }

    /** 日志上报操作回调 */
    @FunctionalInterface
    public interface LogReportOperationCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 上传日志附件回调 */
    @FunctionalInterface
    public interface UploadLogAppendFileCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    // ==================== 固件管理相关回调接口 ====================

    /** 获取最新固件更新说明回调 */
    @FunctionalInterface
    public interface GetLatestFirmwareReleaseNoteCallback {
        /**
         * @param error 错误信息
         * @param notes 固件更新说明列表
         */
        void onResult(FlightHubError error, java.util.List<FirmwareReleaseNoteResult> notes);
    }

    /** 发起固件升级回调 */
    @FunctionalInterface
    public interface InvokeFirmwareUpgradeCallback {
        /**
         * @param error 错误信息
         * @param result 固件升级结果
         */
        void onResult(FlightHubError error, FirmwareUpgradeResult result);
    }

    /** 查询固件升级进度回调 */
    @FunctionalInterface
    public interface QueryFirmwareUpgradeProgressCallback {
        /**
         * @param error 错误信息
         * @param items 固件升级进度列表
         */
        void onResult(FlightHubError error, java.util.List<FirmwareUpgradeProgressItem> items);
    }

    /** 上传固件文件回调 */
    @FunctionalInterface
    public interface UploadFirmwareFileCallback {
        /**
         * @param error 错误信息
         * @param result 文件上传结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UploadFileResult result);
    }

    /** 发布固件回调 */
    @FunctionalInterface
    public interface ReleaseFirmwareCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 撤销发布固件回调 */
    @FunctionalInterface
    public interface UnReleaseFirmwareCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 删除固件回调 */
    @FunctionalInterface
    public interface DeleteFirmwareCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 查询固件列表回调 */
    @FunctionalInterface
    public interface QueryFirmwareListCallback {
        /**
         * @param error 错误信息
         * @param items 固件列表
         */
        void onResult(FlightHubError error, java.util.List<FirmwareItemResult> items);
    }

    /** 查询固件兼容性配置回调 */
    @FunctionalInterface
    public interface QueryFirmwareCompatibilityConfigCallback {
        /**
         * @param error 错误信息
         * @param items 固件兼容性配置列表
         */
        void onResult(FlightHubError error, java.util.List<FirmwareCompatibilityConfigItemResult> items);
    }

    /** 检查固件状态回调 */
    @FunctionalInterface
    public interface CheckFirmwareStatusCallback {
        /**
         * @param error 错误信息
         * @param items 固件状态列表
         */
        void onResult(FlightHubError error, java.util.List<FirmwareStatusItem> items);
    }

    /** 提交固件信息回调 */
    @FunctionalInterface
    public interface SubmitFirmwareInfoCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    // ==================== 解锁证书管理相关回调接口 ====================

    /** 获取解锁证书列表回调 */
    public interface GetUnlockLicenseListCallback {
        /**
         * @param error 错误信息
         * @param result 设备解锁证书列表结果
         */
        void onResult(FlightHubError error, QueryDeviceUnlockLicenseListResult result);
    }

    /** 切换解锁证书回调 */
    public interface SwitchUnlockLicenseCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 校验解锁证书回调 */
    public interface CheckUnlockLicenseCallback {
        /**
         * @param error 错误信息
         * @param result 校验解锁证书结果
         */
        void onResult(FlightHubError error, CheckUnlockLicenseResult result);
    }

    /** 提交解锁证书回调 */
    public interface SubmitUnlockLicenseCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 清除设备解锁证书回调 */
    public interface ClearDeviceUnlockLicenseCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 上传解锁证书回调 */
    public interface UploadUnlockLicenseFileCallback {
        /**
         * @param error 错误码
         * @param result 成功时包含 file_store_dir、object_key 等，用于后续 checkUnlockLicense(file_key) 与 submitUnlockLicense(cert_url)
         */
        void onResult(FlightHubError error, UploadUnlockLicenseFileResult result);
    }

    // ==================== 航线管理相关回调接口 ====================

    /** 获取航线列表回调 */
    public interface GetWaylineListCallback {
        /**
         * @param error 错误信息
         * @param result 航线列表结果
         */
        void onResult(FlightHubError error, GetWaylineListResult result);
    }

    /** 获取航线详情回调 */
    public interface GetWaylineDetailCallback {
        /**
         * @param error 错误信息
         * @param result 航线详情结果
         */
        void onResult(FlightHubError error, GetWaylineDetailResult result);
    }

    /** 批量删除航线回调 */
    public interface BatchDeleteWaylinesCallback {
        /**
         * @param result FH2 通用响应信息（server_code / server_message），后续字段可扩展
         */
        void onResult(FlightHubError error, BatchDeleteWaylinesResult result);
    }

    /** 更新航线回调 */
    public interface UpdateWaylineCallback {
        /**
         * @param error 错误信息
         * @param result 更新航线结果
         */
        void onResult(FlightHubError error, UpdateWaylineResult result);
    }

    /** 提交航线信息回调 */
    public interface SubmitWaylineInfoCallback {
        /**
         * @param error 错误信息
         */
        void onResult(FlightHubError error);
    }

    /** 批量下载航线回调 */
    public interface BatchDownloadWaylinesCallback {
        /**
         * @param error 错误信息
         * @param items 批量下载航线结果列表
         */
        void onResult(FlightHubError error, java.util.List<BatchDownloadWaylinesItemResult> items);
    }

    /** 复制航线回调 */
    public interface DuplicateWaylineCallback {
        /**
         * @param error 错误信息
         * @param result 复制航线结果
         */
        void onResult(FlightHubError error, DuplicateWaylineResult result);
    }

    /** 航线复制回调（去组织化） */
    public interface CopyWaylineCallback {
        /**
         * @param error 错误信息
         * @param result 航线复制结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CopyWaylineResult result);
    }

    /** 航线拼接回调（去组织化） */
    public interface ConcatWaylineCallback {
        /**
         * @param error 错误信息
         * @param result 航线拼接结果
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ConcatWaylineResult result);
    }

    /** 上传 KMZ 并导入航线回调 */
    public interface ImportKmzCallback {
        /**
         * @param error 错误信息
         * @param result 导入结果（含每个文件的导入状态）
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ImportKmzResult result);
    }

    /** 订阅机场舱盖打开进度回调 */
    public interface SubscribeDockCoverOpenProgressCallback {
        /**
         * @param result 打开舱盖结果
         */
        void onResult(com.dji.flighthubsdk.bean.OpenDockCoverResult result);
    }

    /** 订阅机场舱盖关闭进度回调 */
    public interface SubscribeDockCoverCloseProgressCallback {
        /**
         * @param result 关闭舱盖结果
         */
        void onResult(com.dji.flighthubsdk.bean.CloseDockCoverResult result);
    }

    /** 订阅设备重启进度回调 */
    public interface SubscribeDeviceRebootProgressCallback {
        /**
         * @param result 设备重启结果
         */
        void onResult(com.dji.flighthubsdk.bean.DeviceRebootResult result);
    }

    /** 订阅 RTK 标定进度回调 */
    public interface SubscribeCalibrationProgressCallback {
        /**
         * @param result RTK 标定结果
         */
        void onResult(com.dji.flighthubsdk.bean.RtkCalibrationResult result);
    }

    /** 订阅设备格式化进度回调 */
    public interface SubscribeDeviceFormatProgressCallback {
        /**
         * @param result 设备格式化结果
         */
        void onResult(com.dji.flighthubsdk.bean.DeviceFormatResult result);
    }

    /** 订阅无人机开启进度回调 */
    public interface SubscribeDroneOpenProgressCallback {
        /**
         * @param result 开启无人机结果
         */
        void onResult(com.dji.flighthubsdk.bean.OpenDroneResult result);
    }

    /** 订阅无人机关闭进度回调 */
    public interface SubscribeDroneCloseProgressCallback {
        /**
         * @param result 关闭无人机结果
         */
        void onResult(com.dji.flighthubsdk.bean.CloseDroneResult result);
    }

    /** 订阅无人机格式化进度回调 */
    public interface SubscribeDroneFormatProgressCallback {
        /**
         * @param result 无人机格式化结果
         */
        void onResult(com.dji.flighthubsdk.bean.DroneFormatResult result);
    }

    /** 订阅无人机充电开启进度回调 */
    public interface SubscribeDroneChargeOpenProgressCallback {
        /**
         * @param result 开启充电结果
         */
        void onResult(com.dji.flighthubsdk.bean.OpenChargeResult result);
    }

    /** 订阅无人机充电关闭进度回调 */
    public interface SubscribeDroneChargeCloseProgressCallback {
        /**
         * @param result 关闭充电结果
         */
        void onResult(com.dji.flighthubsdk.bean.CloseChargeResult result);
    }

    /** 订阅 eSIM 运营商切换进度回调 */
    public interface SubscribeDeviceEsimOperatorSwitchProgressCallback {
        /**
         * @param result eSIM 运营商切换结果
         */
        void onResult(com.dji.flighthubsdk.bean.EsimOperatorSwitchResult result);
    }

    /** 订阅 SIM 卡槽切换进度回调 */
    public interface SubscribeDeviceSimSlotSwitchProgressCallback {
        /**
         * @param result SIM 卡槽切换结果
         */
        void onResult(com.dji.flighthubsdk.bean.SimSlotSwitchResult result);
    }

    /** 无人机格式化回调 */
    public interface DroneFormatCallback {
        /**
         * @param error 错误信息
         * @param result 无人机格式化结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.DroneFormatResult result);
    }

    /**
     * 飞行器 HSI 信息（障碍物感知/避障传感器数据）；参数为 {@code bean.HsiInfo}。
     */
    @FunctionalInterface
    public interface HsiInfoCallback {
        /**
         * @param hsiInfo HSI 障碍物感知信息
         */
        void onResult(com.dji.flighthubsdk.bean.HsiInfo hsiInfo);
    }

    /** 设置智能规划模式回调 */
    @FunctionalInterface
    public interface SetFlightSmartPlanModeCallback {
        /**
         * @param error 错误信息
         * @param result 智能规划模式结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FlightSmartPlanModeResult result);
    }

    /** 设置失控动作回调 */
    @FunctionalInterface
    public interface SetOutOfControlActionCallback {
        /**
         * @param error 错误信息
         * @param result 失控动作结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FlightOutOfControlActionResult result);
    }

    /** 设置飞行任务高度回调 */
    @FunctionalInterface
    public interface SetFlightTaskHeightCallback {
        /**
         * @param error 错误信息
         * @param result 飞行任务高度结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FlightCommanderHeightResult result);
    }

    /** 设置返航高度回调 */
    @FunctionalInterface
    public interface SetRthAltitudeCallback {
        /**
         * @param error 错误信息
         * @param result 返航高度结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FlightRthAltitudeResult result);
    }

    /** 设置空中传输使能回调 */
    @FunctionalInterface
    public interface SetAirTransferEnableCallback {
        /**
         * @param error 错误信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error);
    }

    /** 试飞航线生成回调 */
    @FunctionalInterface
    public interface GenerateTryFlightWaylineCallback {
        void onResult(com.dji.flighthubsdk.generated.GenerateTryFlightWaylineResponse response,
                      com.dji.flighthubsdk.bean.FlightHubError error);
    }

    /** 试飞任务下发回调 */
    @FunctionalInterface
    public interface CreateTestFlightTaskCallback {
        void onResult(com.dji.flighthubsdk.generated.CreateTestFlightTaskResponse response,
                      com.dji.flighthubsdk.bean.FlightHubError error);
    }

    // ==================== 报修管理模块 ====================

    /** 获取报修配置回调 */
    @FunctionalInterface
    public interface GetRepairConfigsCallback {
        /**
         * @param error 错误信息
         * @param result 报修配置结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.DeviceRepairConfigResults result);
    }

    /** 创建维修工单回调 */
    @FunctionalInterface
    public interface CreateMaintenanceOrderCallback {
        /**
         * @param error 错误信息
         * @param result 创建维修工单结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.CreateMaintenanceOrderResults result);
    }

    /** 获取维修工单详情回调 */
    @FunctionalInterface
    public interface GetMaintenanceOrderDetailCallback {
        /**
         * @param error 错误信息
         * @param result 维修工单详情结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.MaintenanceOrderDetailResults result);
    }

    /** 获取维修工单列表回调 */
    @FunctionalInterface
    public interface GetMaintenanceOrdersListCallback {
        /**
         * @param error 错误信息
         * @param result 维修工单列表结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.MaintenanceOrdersListResults result);
    }

    // ==================== 诊断管理模块 ====================

    /** 创建自检诊断回调 */
    @FunctionalInterface
    public interface CreateSelfDiagnosisCallback {
        /**
         * @param error 错误信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error);
    }

    /** 操作自检诊断回调 */
    @FunctionalInterface
    public interface OperateSelfDiagnosisCallback {
        /**
         * @param error 错误信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error);
    }

    /** 获取自检诊断报告回调 */
    @FunctionalInterface
    public interface GetSelfDiagnosisReportCallback {
        /**
         * @param error 错误信息
         * @param result 自检诊断报告结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults result);
    }

    /** 获取自检诊断报告列表回调 */
    @FunctionalInterface
    public interface GetSelfDiagnosisReportListCallback {
        /**
         * @param error 错误信息
         * @param result 自检诊断报告列表结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults result);
    }

    // ==================== AI Box 管理模块 ====================

    /** 获取 AI Box 信息回调 */
    @FunctionalInterface
    public interface FetchAIBoxInfoCallback {
        /**
         * @param error 错误信息
         * @param result AI Box 信息列表结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.AIBoxInfoListResult result);
    }

    /** 获取文件下载链接回调 */
    @FunctionalInterface
    public interface FetchFileDownloadUrlCallback {
        /**
         * @param error 错误信息
         * @param result 文件下载链接列表结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FileDownloadUrlListResult result);
    }

    /** 获取应用列表回调 */
    @FunctionalInterface
    public interface FetchAppListCallback {
        /**
         * @param error 错误信息
         * @param result 应用列表结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FetchAppListResult result);
    }

    /** 获取应用列表进度回调 */
    @FunctionalInterface
    public interface FetchAppListProgressCallback {
        /**
         * @param error 错误信息
         * @param result 应用列表获取进度结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FetchAppListProgressResult result);
    }

    /** 导出文件回调 */
    @FunctionalInterface
    public interface ExportFileCallback {
        /**
         * @param error 错误信息
         * @param result 导出文件结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.ExportFileResult result);
    }

    /** 导出文件进度回调 */
    @FunctionalInterface
    public interface ExportFileProgressCallback {
        /**
         * @param error 错误信息
         * @param result 导出文件进度结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.ExportFileProgressResult result);
    }

    /** 上传 AI Box 文件回调 */
    @FunctionalInterface
    public interface UploadAIBoxFileCallback {
        /**
         * @param error 错误信息
         * @param result 文件上传结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.UploadFileResult result);
    }

    /** 安装应用回调 */
    @FunctionalInterface
    public interface InstallAppCallback {
        /**
         * @param error 错误信息
         * @param result 安装应用结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.InstallAppResult result);
    }

    /** 安装应用进度回调 */
    @FunctionalInterface
    public interface InstallAppProgressCallback {
        /**
         * @param error 错误信息
         * @param result 安装应用进度结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.InstallAppProgressResult result);
    }

    /** 操作应用回调 */
    @FunctionalInterface
    public interface OperateAppCallback {
        /**
         * @param error 错误信息
         * @param result 操作应用结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.OperateAppResult result);
    }

    public interface DockDeparturePointCallback {
        void onResult(com.dji.flighthubsdk.generated.DockDeparturePoint msg);
    }

    public interface AircraftVisualScoutStatusCallback {
        void onResult(com.dji.flighthubsdk.generated.AircraftVisualScoutStatus msg);
    }

    public interface AircraftVisualScoutAbnormalInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.AircraftVisualScoutAbnormalInfo msg);
    }

    public interface AircraftVisualScoutObstacleInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.AircraftVisualScoutObstacleInfo msg);
    }

    public interface EphemerisDownloadStateCallback {
        void onResult(com.dji.flighthubsdk.generated.EphemerisDownloadState msg);
    }

    public interface EphemerisInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.EphemerisInfo msg);
    }

    public interface InOutWaylineFlightSettingCallback {
        void onResult(com.dji.flighthubsdk.generated.InOutWaylineFlightSettingParams params, com.dji.flighthubsdk.bean.FlightHubError error);
    }

    // ==================== AI Inside 机载算法管理回调接口（PAAS 纯血场景） ====================

    /** 获取机载算法列表回调 */
    public interface GetAIInsideModelsCallback {
        /**
         * @param error 错误信息
         * @param result 算法列表结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.GetAIInsideModelsResult result);
    }

    /** 获取算法绑定设备详情回调 */
    public interface GetAIInsideModelDevicesCallback {
        /**
         * @param error 错误信息
         * @param result 设备详情分页结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.GetAIInsideModelDevicesResult result);
    }

    /** 获取可操作设备数量统计回调 */
    public interface GetAIInsideModelDevicesSummaryCallback {
        /**
         * @param error 错误信息
         * @param result 可操作设备数量统计结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryResult result);
    }

    /** 批量下发算法操作指令回调 */
    public interface OperateAIInsideModelCallback {
        /**
         * @param error 错误信息
         * @param result 各设备操作结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.OperateAIInsideModelResult result);
    }

    /** 创建算法上传凭证回调 */
    public interface CreateAIInsideAlgorithmCallback {
        /**
         * @param error 错误信息
         * @param result 上传凭证和存储路径信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmResult result);
    }

    /** 处理已上传算法文件回调 */
    public interface FileUploadAIInsideAlgorithmCallback {
        /**
         * @param error 错误信息
         * @param result 算法入库详情结果
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmResult result);
    }

    /** 删除算法回调 */
    public interface DeleteAIInsideAlgorithmCallback {
        /**
         * @param error 错误信息
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error);
    }

    /** 一站式算法文件上传回调（内部自动完成获取凭证→上传→入库三步） */
    public interface UploadAlgorithmFileCallback {
        /**
         * @param error 错误信息
         * @param result 上传结果，含 object_key 和完整算法入库详情
         */
        void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                      com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileResult result);
    }

    // ─────────────────────────────────────────────────────────
    // AI 告警管理（/drc/paas/v2/ai-alert/）
    // ─────────────────────────────────────────────────────────

    /** 写入设备级告警配置回调 */
    public interface PostAiAlertDeviceSettingCallback {
        /**
         * @param error 错误信息
         * @param result 操作结果（空载体）
         */
        void onResult(FlightHubError error,
                      PostAiAlertDeviceSettingResult result);
    }

    /** 读取设备级告警配置回调 */
    public interface GetAiAlertDeviceSettingCallback {
        /**
         * @param error 错误信息
         * @param result 设备告警配置详情
         */
        void onResult(FlightHubError error,
                      GetAiAlertDeviceSettingResult result);
    }

    /** 写入算法级阈值配置回调 */
    public interface PostAiAlertAlgorithmSettingCallback {
        /**
         * @param error 错误信息
         * @param result 操作结果（空载体）
         */
        void onResult(FlightHubError error,
                      PostAiAlertAlgorithmSettingResult result);
    }

    /** 读取算法级阈值配置回调 */
    public interface GetAiAlertAlgorithmSettingCallback {
        /**
         * @param error 错误信息
         * @param result 算法阈值配置详情
         */
        void onResult(FlightHubError error,
                      GetAiAlertAlgorithmSettingResult result);
    }

    /** 查询告警架次统计回调 */
    public interface GetAiAlertFlightsCallback {
        /**
         * @param error 错误信息
         * @param result 告警架次分页列表
         */
        void onResult(FlightHubError error,
                      GetAiAlertFlightsResult result);
    }

    /** 查询告警明细记录回调 */
    public interface GetAiAlertRecordsCallback {
        /**
         * @param error 错误信息
         * @param result 告警明细（按 flight_id 分组）及分页信息
         */
        void onResult(FlightHubError error,
                      GetAiAlertRecordsResult result);
    }

    // ─────────────────────────────────────────────────────────
    // 固件升级进度 (topic_device_ota_progress)
    // ─────────────────────────────────────────────────────────
    @FunctionalInterface
    public interface DeviceOtaProgressCallback {
        void onResult(DeviceOtaProgress msg);
    }

    // ─────────────────────────────────────────────────────────
    // 设备异常报告上传进度 (topic_device_fileupload_progress)
    // ─────────────────────────────────────────────────────────
    @FunctionalInterface
    public interface DeviceFileUploadProgressCallback {
        void onResult(com.dji.flighthubsdk.generated.DeviceFileUploadProgress msg);
    }

    // ─────────────────────────────────────────────────────────
    // PSDK喊话器音频播放进度 (topic_psdk_speaker_audio_play_start_progress)
    // ─────────────────────────────────────────────────────────
    @FunctionalInterface
    public interface PsdkSpeakerAudioPlayStartProgressCallback {
        void onResult(com.dji.flighthubsdk.generated.PsdkSpeakerPlayStartProgress msg);
    }

    // ─────────────────────────────────────────────────────────
    // PSDK喊话器TTS播放进度 (topic_psdk_speaker_tts_play_start_progress)
    // ─────────────────────────────────────────────────────────
    @FunctionalInterface
    public interface PsdkSpeakerTtsPlayStartProgressCallback {
        void onResult(com.dji.flighthubsdk.generated.PsdkSpeakerPlayStartProgress msg);
    }

    // ─────────────────────────────────────────────────────────
    // POI飞行进度通知 (topic_task_flight_poi_progress)
    // ─────────────────────────────────────────────────────────
    @FunctionalInterface
    public interface FlightPoiProgressCallback {
        void onResult(com.dji.flighthubsdk.generated.FlightPoiProgress msg);
    }

    // ==================== DRC 推送数据回调接口 ====================

    /** DRC OSD 信息回调 */
    @FunctionalInterface
    public interface DrcOsdInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.DrcOsdInfo info);
    }

    /** DRC 电池信息回调 */
    @FunctionalInterface
    public interface DrcBatteriesInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.DrcBatteriesInfo info);
    }

    /** DRC 降落伞信息回调 */
    @FunctionalInterface
    public interface DrcParachuteInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.DrcParachuteInfo info);
    }

    /** 爆闪灯状态回调 */
    @FunctionalInterface
    public interface FlashLedStateInfoCallback {
        /**
         * @param info 爆闪灯状态
         */
        void onResult(com.dji.flighthubsdk.bean.FlashLedStateInfo info);
    }

    /**
     * FlashLedConfigListCallback 回调接口
     */
    @FunctionalInterface
    public interface FlashLedConfigListCallback {
        /**
         * @param error 错误信息
         * @param list 爆闪灯模板列表
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlashLedConfigList list);
    }

    /**
     * FlashLedConfigCallback 回调接口
     */
    @FunctionalInterface
    public interface FlashLedConfigCallback {
        /**
         * @param error 错误信息
         * @param config 爆闪灯模板
         */
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlashLedConfig config);
    }

    /** AI 告警状态回调 */
    @FunctionalInterface
    public interface AiAlarmStateCallback {
        void onResult(com.dji.flighthubsdk.generated.AiAlarmStateInfo info);
    }

    /** DRC 延时信息回调 */
    @FunctionalInterface
    public interface DrcDelayInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.DrcDelayInfo info);
    }

    /** DRC 倒计时信息回调 */
    @FunctionalInterface
    public interface DrcCountDownInfoCallback {
        void onResult(com.dji.flighthubsdk.generated.DrcCountDownInfo info);
    }

    // ── ElementModule HTTP 回调 ──────────────────────────────────────────────

    /** 创建要素回调 */
    @FunctionalInterface
    public interface ElementCreateElementCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ElementCreateResult result);
    }
    /** 更新要素回调 */
    @FunctionalInterface
    public interface ElementUpdateElementCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ElementUpdateResult result);
    }
    /** 查询元素层级列表回调 */
    @FunctionalInterface
    public interface ElementListHierarchyCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ElementListHierarchyResult result);
    }
    /** 设置要素显示状态回调 */
    @FunctionalInterface
    public interface ElementSetVisibilityCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ElementSetVisibilityResult result);
    }
    /** 删除要素回调 */
    @FunctionalInterface
    public interface ElementDeleteElementCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ElementDeleteResult result);
    }

    // ── ElementModule WS 回调 ────────────────────────────────────────────────

    /** 要素创建推送回调 (topic_element_create) */
    @FunctionalInterface
    public interface ElementCreateCallback {
        void onResult(com.dji.flighthubsdk.bean.ElementCreateMsg msg);
    }
    /** 要素更新推送回调 (topic_element_update) */
    @FunctionalInterface
    public interface ElementUpdateCallback {
        void onResult(com.dji.flighthubsdk.bean.ElementUpdateMsg msg);
    }
    /** 要素删除推送回调 (topic_element_delete) */
    @FunctionalInterface
    public interface ElementDeleteCallback {
        void onResult(com.dji.flighthubsdk.bean.ElementDeleteMsg msg);
    }

     // ===== 直播转发 =====

    public interface CreateLiveSyncCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LiveSyncCreateResult result);
    }

    public interface DeleteLiveSyncCallback {
        void onResult(FlightHubError error);
    }

    public interface UpdateLiveSyncCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LiveSyncItem result);
    }

    public interface GetLiveSyncListCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LiveSyncListResult result);
    }

    public interface GetLiveSyncDevicesV2Callback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LiveSyncDevicesV2Result result);
    }

    public interface GetLiveSyncSummaryCallback {
        void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LiveSyncSummaryResult result);
    }

    // ===================== EA340 消息推送补全（Phase D）回调接口 Start =====================

    /** 机场直播状态(live_status)回调接口 */
    public interface DockLiveStatusCallback {
        /**
         * @param dockLiveStatus 机场直播状态
         */
        void onResult(com.dji.flighthubsdk.bean.DockLiveStatus dockLiveStatus);
    }

    /** 机场直播能力(live_capacity)回调接口 */
    public interface DockLiveCapacityCallback {
        /**
         * @param dockLiveCapacity 机场直播能力
         */
        void onResult(com.dji.flighthubsdk.bean.DockLiveCapacity dockLiveCapacity);
    }

    /** 机场4G Dongle信息(dongle_infos)回调接口 */
    public interface DockDongleInfosCallback {
        /**
         * @param dockDongleInfos 机场4G Dongle信息
         */
        void onResult(com.dji.flighthubsdk.bean.DockDongleInfos dockDongleInfos);
    }

    /** 机场空调工作模式(air_conditioner_mode)回调接口 */
    public interface DockAirConditionerModeCallback {
        /**
         * @param dockAirConditionerMode 机场空调工作模式
         */
        void onResult(com.dji.flighthubsdk.bean.DockAirConditionerMode dockAirConditionerMode);
    }

    /** 飞行器进入当前状态原因(mode_code_reason)回调接口 */
    public interface DroneModeCodeReasonCallback {
        /**
         * @param droneModeCodeReason 飞行器进入当前状态原因
         */
        void onResult(com.dji.flighthubsdk.bean.DroneModeCodeReason droneModeCodeReason);
    }

    /** 飞机4G Dongle信息(dongle_infos)回调接口 */
    public interface DroneDongleInfosCallback {
        /**
         * @param droneDongleInfos 飞机4G Dongle信息
         */
        void onResult(com.dji.flighthubsdk.bean.DroneDongleInfos droneDongleInfos);
    }

    /** 限飞数据库版本(flysafe_database_version)回调接口 */
    public interface DroneFlysafeDatabaseVersionCallback {
        /**
         * @param droneFlysafeDatabaseVersion 限飞数据库版本
         */
        void onResult(com.dji.flighthubsdk.bean.DroneFlysafeDatabaseVersion droneFlysafeDatabaseVersion);
    }

    /** 航线解析库版本(wpmz_version)回调接口 */
    public interface DroneWpmzVersionCallback {
        /**
         * @param droneWpmzVersion 航线解析库版本
         */
        void onResult(com.dji.flighthubsdk.bean.DroneWpmzVersion droneWpmzVersion);
    }

    /** 指点飞行模式设置值(commander_flight_mode)回调接口 */
    public interface DroneCommanderFlightModeCallback {
        /**
         * @param droneCommanderFlightMode 指点飞行模式设置值
         */
        void onResult(com.dji.flighthubsdk.bean.DroneCommanderFlightMode droneCommanderFlightMode);
    }

    /** 指点飞行模式当前值(current_commander_flight_mode)回调接口 */
    public interface DroneCurrentCommanderFlightModeCallback {
        /**
         * @param droneCurrentCommanderFlightMode 指点飞行模式当前值
         */
        void onResult(com.dji.flighthubsdk.bean.DroneCurrentCommanderFlightMode droneCurrentCommanderFlightMode);
    }

    /** 指点飞行失控动作(commander_mode_lost_action)回调接口 */
    public interface DroneCommanderModeLostActionCallback {
        /**
         * @param droneCommanderModeLostAction 指点飞行失控动作
         */
        void onResult(com.dji.flighthubsdk.bean.DroneCommanderModeLostAction droneCommanderModeLostAction);
    }

    /** 飞行器离线地图开关(offline_map_enable)回调接口 */
    public interface DroneOfflineMapEnableCallback {
        /**
         * @param droneOfflineMapEnable 飞行器离线地图开关
         */
        void onResult(com.dji.flighthubsdk.bean.DroneOfflineMapEnable droneOfflineMapEnable);
    }

    /** PSDK UI 资源(psdk_ui_resource)回调接口 */
    public interface DronePsdkUiResourceCallback {
        /**
         * @param dronePsdkUiResource PSDK UI 资源
         */
        void onResult(com.dji.flighthubsdk.bean.DronePsdkUiResource dronePsdkUiResource);
    }

    /**
     * 飞机相机水印设置推送(WebSocket topic_drone_camera_watermark_settings)回调接口。
     * 注意：与 DRC 侧 listenDrcCameraWatermarkSettings 使用的 CameraWatermarkSettings 不同，不要混用。
     */
    public interface DroneCameraWatermarkSettingsPushCallback {
        /**
         * @param droneCameraWatermarkSettingsPush 飞机相机水印设置
         */
        void onResult(com.dji.flighthubsdk.bean.DroneCameraWatermarkSettingsPush droneCameraWatermarkSettingsPush);
    }

    // ===================== EA340 消息推送补全（Phase D）回调接口 End =====================

    // ===================== 云环境切换 回调接口 Begin =====================
    /**
     * 云环境切换进度回调接口（topic_cloud_environment_switch）。
     */
    public interface DeviceCloudEnvironmentSwitchProgressCallback {
        /**
         * @param result 云环境切换结果
         */
        void onResult(com.dji.flighthubsdk.bean.CloudEnvironmentSwitchResult result);
    }
    // ===================== 云环境切换 回调接口 End =====================
}
