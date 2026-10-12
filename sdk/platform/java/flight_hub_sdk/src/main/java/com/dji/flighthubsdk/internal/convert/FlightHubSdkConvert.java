// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.internal.convert;

import com.dji.flighthubsdk.bean.AircraftActivationTime;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadCameraInfo;
import com.dji.flighthubsdk.bean.AircraftPayloadsCamerasList;
import com.dji.flighthubsdk.bean.AircraftPayloadGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadLiveviewWorldRegionEntry;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfo;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfoList;
import com.dji.flighthubsdk.bean.AircraftPayloadsCameraLiveviewWorldRegion;
import com.dji.flighthubsdk.bean.AircraftPayloadsGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadsList;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftBatteryWarningThresholds;
import com.dji.flighthubsdk.bean.AircraftComplianceInfo;
import com.dji.flighthubsdk.bean.AircraftControlCode;
import com.dji.flighthubsdk.bean.AircraftControlSource;
import com.dji.flighthubsdk.bean.AircraftCurrentRthMode;
import com.dji.flighthubsdk.bean.AircraftFirmwareStatus;
import com.dji.flighthubsdk.bean.AircraftGear;
import com.dji.flighthubsdk.bean.AircraftHomeGeographicalInfo;
import com.dji.flighthubsdk.bean.AircraftHomeDistance;
import com.dji.flighthubsdk.bean.AircraftLimitsInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.AircraftMaintainStatus;
import com.dji.flighthubsdk.bean.AircraftMaintainStatusInfo;
import com.dji.flighthubsdk.bean.AircraftModeCode;
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
import com.dji.flighthubsdk.bean.BatteryBasicInfo;
import com.dji.flighthubsdk.bean.BatteryInfo;
import com.dji.flighthubsdk.bean.AiDetectionTarget;
import com.dji.flighthubsdk.bean.AiSeiInfo;
import com.dji.flighthubsdk.bean.ArSeiInfo;
import com.dji.flighthubsdk.bean.AssistantSeiInfo;
import com.dji.flighthubsdk.bean.CamCropPos;
import com.dji.flighthubsdk.bean.CameraIrMeteringPoint;
import com.dji.flighthubsdk.bean.CameraIrMeteringArea;
import com.dji.flighthubsdk.bean.CameraIrThermometryInfo;
import com.dji.flighthubsdk.bean.CameraIrLensState;
import com.dji.flighthubsdk.bean.CameraLensFovInfo;
import com.dji.flighthubsdk.bean.CameraLiveviewWorldRegion;
import com.dji.flighthubsdk.bean.CameraMeasureTarget;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.CameraWideLensState;
import com.dji.flighthubsdk.bean.CameraZoomLensState;
import com.dji.flighthubsdk.bean.CloseChargeResult;
import com.dji.flighthubsdk.bean.CloseDebugModeResult;
import com.dji.flighthubsdk.bean.CloseDockCoverProgressResult;
import com.dji.flighthubsdk.bean.CloseDockCoverResult;
import com.dji.flighthubsdk.bean.CloseDroneResult;
import com.dji.flighthubsdk.bean.DeviceFormatProgressResult;
import com.dji.flighthubsdk.bean.DeviceFormatResult;
import com.dji.flighthubsdk.bean.DeviceOtaProgress;
import com.dji.flighthubsdk.bean.DeviceOtaProgressInfo;
import com.dji.flighthubsdk.bean.DeviceRebootProgressResult;
import com.dji.flighthubsdk.bean.DeviceRebootResult;
import com.dji.flighthubsdk.bean.RtkCalibrationResult;
import com.dji.flighthubsdk.bean.DroneFormatResult;
import com.dji.flighthubsdk.bean.EsimOperatorSwitchResult;
import com.dji.flighthubsdk.bean.GimbalState;
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
import com.dji.flighthubsdk.bean.DockMaintainStatus;
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
import com.dji.flighthubsdk.bean.DockSubDeviceInfo;
import com.dji.flighthubsdk.bean.DockSubDeviceInfoList;
import com.dji.flighthubsdk.bean.DockSupplementLightState;
import com.dji.flighthubsdk.bean.DockUserExperienceImprovement;
import com.dji.flighthubsdk.bean.DockWirelessLink;
import com.dji.flighthubsdk.bean.DroneInDock;
import com.dji.flighthubsdk.bean.ControlRightsCloudControlStatus;
import com.dji.flighthubsdk.bean.ControlRightsCloudControlStatusItem;
import com.dji.flighthubsdk.bean.ControlRightsDeviceControlStatus;
import com.dji.flighthubsdk.bean.ControlRightsDeviceControlStatusItem;
import com.dji.flighthubsdk.bean.FlightTask;
import com.dji.flighthubsdk.bean.FlightTaskEventBreakPointResume;
import com.dji.flighthubsdk.bean.FlightTaskEventContinuousSubTask;
import com.dji.flighthubsdk.bean.FlightTaskException;
import com.dji.flighthubsdk.bean.FlightTaskExceptionDetail;
import com.dji.flighthubsdk.bean.FlightTaskStepCode;
import com.dji.flighthubsdk.bean.FlightTasks;
import com.dji.flighthubsdk.bean.FlightTasksMediaFileDisplayAvailable;
import com.dji.flighthubsdk.bean.FlightTasksMediaStatusChanged;
import com.dji.flighthubsdk.bean.FlyToDroneSelfPlanPath;
import com.dji.flighthubsdk.bean.FlyToDroneSelfPlanPathPoint;
import com.dji.flighthubsdk.bean.FlyToPointProgress;
import com.dji.flighthubsdk.bean.PsdkDeviceInfo;
import com.dji.flighthubsdk.bean.PsdkDeviceList;
import com.dji.flighthubsdk.bean.PsdkFloatingWindowTextInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerPlayProgressInfo;
import com.dji.flighthubsdk.bean.PsdkStateSearchlightInfo;
import com.dji.flighthubsdk.bean.PsdkStateSpeakerInfo;
import com.dji.flighthubsdk.bean.PsdkStateSummaryInfo;
import com.dji.flighthubsdk.bean.TasksMediaStatus;
import com.dji.flighthubsdk.bean.NetworkTestProgressResult;
import com.dji.flighthubsdk.bean.OpenChargeResult;
import com.dji.flighthubsdk.bean.OpenDebugModeResult;
import com.dji.flighthubsdk.bean.OpenDockCoverProgressResult;
import com.dji.flighthubsdk.bean.OpenDockCoverResult;
import com.dji.flighthubsdk.bean.OpenDroneResult;
import com.dji.flighthubsdk.bean.SetBatteryStoreModeResult;
import com.dji.flighthubsdk.bean.ManualBatterySwapResult;
import com.dji.flighthubsdk.bean.ManualBatterySwapProgressResult;
import com.dji.flighthubsdk.bean.DonglePowerSwitchResult;
import com.dji.flighthubsdk.bean.LowPowerSwitchResult;
import com.dji.flighthubsdk.bean.EsimOperatorSwitchProgressResult;
import com.dji.flighthubsdk.bean.SetDistanceLimitResult;
import com.dji.flighthubsdk.bean.SetHeightLimitResult;
import com.dji.flighthubsdk.bean.SetNightLightsStateResult;
import com.dji.flighthubsdk.bean.SetObstacleAvoidanceResult;
import com.dji.flighthubsdk.bean.SetRemainingPowerForRthResult;
import com.dji.flighthubsdk.bean.SimSlotSwitchResult;
import com.dji.flighthubsdk.bean.SwitchAlarmStateResult;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveRecordItem;
import com.dji.flighthubsdk.bean.LiveRecordStateInfo;
import com.dji.flighthubsdk.bean.LiveVideoInfo;
import com.dji.flighthubsdk.bean.LiveviewWorldRegion;
import com.dji.flighthubsdk.bean.PanoramaStatus;
import com.dji.flighthubsdk.bean.PoiStatusEvent;
import com.dji.flighthubsdk.bean.POITaskInfo;
import com.dji.flighthubsdk.bean.ThermalGainModeInfo;
import com.dji.flighthubsdk.bean.Waypoint;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceAudienceCountItem;
import com.dji.flighthubsdk.bean.DeviceChange;
import com.dji.flighthubsdk.bean.DeviceStatusModel;
import com.dji.flighthubsdk.bean.DockStatusItemResult;
import com.dji.flighthubsdk.bean.DockStatusAirConditionerResult;
import com.dji.flighthubsdk.bean.DockStatusAlternateLandPointResult;
import com.dji.flighthubsdk.bean.DockStatusBackupBatteryResult;
import com.dji.flighthubsdk.bean.DockStatusNetworkStateResult;
import com.dji.flighthubsdk.bean.DockStatusPositionStateResult;
import com.dji.flighthubsdk.bean.DockStatusPropertiesResult;
import com.dji.flighthubsdk.bean.DockStatusTiltAngleResult;
import com.dji.flighthubsdk.bean.DroneStatusItemResult;
import com.dji.flighthubsdk.bean.DroneStatusBatteryResult;
import com.dji.flighthubsdk.bean.DroneStatusBatteryItemResult;
import com.dji.flighthubsdk.bean.DroneStatusDistanceLimitStatusResult;
import com.dji.flighthubsdk.bean.DroneStatusObstacleAvoidanceResult;
import com.dji.flighthubsdk.bean.DroneStatusPositionStateResult;
import com.dji.flighthubsdk.bean.DroneStatusPropertiesResult;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResult;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResultItem;
import com.dji.flighthubsdk.bean.DeviceUnlockLicenseItem;
import com.dji.flighthubsdk.bean.DroneStatusStorageResult;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityConfigItemResult;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityRulesResult;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResult;
import com.dji.flighthubsdk.bean.FirmwareItemResult;
import com.dji.flighthubsdk.bean.FirmwareReleaseNoteResult;
import com.dji.flighthubsdk.bean.FirmwareServiceData;
import com.dji.flighthubsdk.bean.FirmwareServiceDevice;
import com.dji.flighthubsdk.bean.FirmwareService;
import com.dji.flighthubsdk.bean.FirmwareServiceProgress;
import com.dji.flighthubsdk.bean.FirmwareStatusItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeAssociateDrone;
import com.dji.flighthubsdk.bean.FirmwareUpgradeAssociateRelay;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResponseDeviceItemResult;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressOTA;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressStep;
import com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListResult;
import com.dji.flighthubsdk.bean.DeviceUnlockAreaUnlock;
import com.dji.flighthubsdk.bean.DeviceUnlockCircleUnlock;
import com.dji.flighthubsdk.bean.DeviceUnlockCountryUnlock;
import com.dji.flighthubsdk.bean.DeviceUnlockHeightUnlock;
import com.dji.flighthubsdk.bean.DeviceUnlockLicenseCommonFields;
import com.dji.flighthubsdk.bean.DeviceUnlockPolygonPoint;
import com.dji.flighthubsdk.bean.DeviceUnlockPolygonUnlock;
import com.dji.flighthubsdk.bean.DeviceUnlockPowerUnlock;
import com.dji.flighthubsdk.bean.DeviceUnlockRidUnlock;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.WaylineDetailExtData;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineDetailSurveyAreaScopePoint;
import com.dji.flighthubsdk.bean.WaylineDetailSurveyAreaScope;
import com.dji.flighthubsdk.bean.DuplicateWaylineResult;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.bean.WaylineListPaginationResult;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.BatchDeleteWaylinesResult;
import com.dji.flighthubsdk.bean.ImportWaylineEventExtData;
import com.dji.flighthubsdk.bean.ImportWaylineEventKmzData;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.UploadUnlockLicenseFileResult;
import com.dji.flighthubsdk.bean.UpdateWaylineResult;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import com.dji.flighthubsdk.bean.DeviceDetailInfo;
import com.dji.flighthubsdk.bean.ModelInfo;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.DeviceTopoChange;
import com.dji.flighthubsdk.bean.PsdkSpeakerResource;
import com.dji.flighthubsdk.bean.PsdkSpeakerResourceList;
import com.dji.flighthubsdk.bean.PsdkWidgetResource;
import com.dji.flighthubsdk.bean.PsdkWidgetResourceList;
import com.dji.flighthubsdk.bean.STSCredential;
import com.dji.flighthubsdk.bean.STSInfo;
import com.dji.flighthubsdk.bean.SubDeviceInfo;
import com.dji.flighthubsdk.bean.SmartTrackPointInfo;
import com.dji.flighthubsdk.bean.EisSeiInfo;
import com.dji.flighthubsdk.bean.FovSeiInfo;
import com.dji.flighthubsdk.bean.FrameInfo;
import com.dji.flighthubsdk.bean.HmsArgs;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataItem;
import com.dji.flighthubsdk.bean.ContentsGeometryData;
import com.dji.flighthubsdk.bean.ContentsPropertyData;
import com.dji.flighthubsdk.bean.CreateDeviceJobDataResult;
import com.dji.flighthubsdk.bean.CreateDeviceJobProgressResult;
import com.dji.flighthubsdk.bean.CreateDeviceLogReportResult;
import com.dji.flighthubsdk.bean.CreateFlightAreasResults;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.DeviceLogListResultFileItem;
import com.dji.flighthubsdk.bean.DeviceLogListResultLogItem;
import com.dji.flighthubsdk.bean.DeviceLogListResult;
import com.dji.flighthubsdk.bean.DeviceRecommendationResp;
import com.dji.flighthubsdk.bean.RecommendedDevice;
import com.dji.flighthubsdk.bean.DeviceSignalItem;
import com.dji.flighthubsdk.bean.DeviceSyncResults;
import com.dji.flighthubsdk.bean.DeviceSyncStatusResults;
import com.dji.flighthubsdk.bean.TCAInfo;
import com.dji.flighthubsdk.bean.GetTCAInfosResp;
import com.dji.flighthubsdk.bean.ExportFlightRecordsResults;
import com.dji.flighthubsdk.bean.FlightAreaInfoItem;
import com.dji.flighthubsdk.bean.FlightAreaPagination;
import com.dji.flighthubsdk.bean.FlightAreasContentData;
import com.dji.flighthubsdk.bean.FlightRecordExportProgressItemData;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.bean.WaylineBreakPoint;
import com.dji.flighthubsdk.bean.WaylineObstacle;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.CommandTaskListItemData;
import com.dji.flighthubsdk.bean.GetCommandTaskListParams;
import com.dji.flighthubsdk.bean.GetCommandTaskListResults;
import com.dji.flighthubsdk.bean.FlightTrackPoint;
import com.dji.flighthubsdk.bean.FlightTrackInfo;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackParams;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackResults;
import com.dji.flighthubsdk.bean.GetAreasSyncStatusResults;
import com.dji.flighthubsdk.bean.GetExportFlightRecordsProgressResults;
import com.dji.flighthubsdk.bean.GetFlightAreasResults;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.GetMediaUploadStsParams;
import com.dji.flighthubsdk.bean.GetMediaUploadStsResult;
import com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults;
import com.dji.flighthubsdk.bean.GetOfflineMapsSyncStatusResults;
import com.dji.flighthubsdk.bean.GridSignalItem;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultPagination;
import com.dji.flighthubsdk.bean.HistoryHmsDataResult;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultArgs;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultItem;
import com.dji.flighthubsdk.bean.LogReportListResultAppendixFileItem;
import com.dji.flighthubsdk.bean.LogReportListResultAppendixFiles;
import com.dji.flighthubsdk.bean.LogReportListResultContactInformation;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceLog;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceLogItemList;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceLogs;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceTopo;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceTopoItem;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceUploadFiles;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceUploadFilesItem;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceUploadFilesItemProgress;
import com.dji.flighthubsdk.bean.LogReportListResultItem;
import com.dji.flighthubsdk.bean.LogReportListResultPagination;
import com.dji.flighthubsdk.bean.LogReportListResult;
import com.dji.flighthubsdk.bean.MediaFileDeviceUpload;
import com.dji.flighthubsdk.bean.MediaFileImageMetaData;
import com.dji.flighthubsdk.bean.MediaFilePostProcess;
import com.dji.flighthubsdk.bean.MediaFileVideoMetaData;
import com.dji.flighthubsdk.bean.MediaFilesListItemExtData;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.bean.MediaUploadCallbackFile;
import com.dji.flighthubsdk.bean.MediaUploadCallbackFileExt;
import com.dji.flighthubsdk.bean.MediaUploadCallbackMetadata;
import com.dji.flighthubsdk.bean.MediaUploadCallbackParams;
import com.dji.flighthubsdk.bean.MediaUploadCallbackResult;
import com.dji.flighthubsdk.bean.MediaUploadCallbackShootPosition;
import com.dji.flighthubsdk.bean.MediaUploadStsCredentials;
import com.dji.flighthubsdk.bean.TerraBuildCreatePanoramaResult;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionResult;
import com.dji.flighthubsdk.bean.TerraBuildFolderInfo;
import com.dji.flighthubsdk.bean.TerraBuildJobInfo;
import com.dji.flighthubsdk.bean.TerraBuildModelFileItem;
import com.dji.flighthubsdk.bean.TerraBuildModelFileListResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileUrlResult;
import com.dji.flighthubsdk.bean.TerraBuildQualityReportResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionImageItem;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionImagesResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionStatusChanged;
import com.dji.flighthubsdk.bean.TerraBuildSettingsDetail;
import com.dji.flighthubsdk.bean.TerraBuildSettingsResult;
import com.dji.flighthubsdk.bean.TerraBuildSfmResult;
import com.dji.flighthubsdk.bean.PositionData;
import com.dji.flighthubsdk.bean.OfflineMapsModelData;
import com.dji.flighthubsdk.bean.OfflineMapsModelInfoData;
import com.dji.flighthubsdk.bean.OfflineMapsSyncResults;
import com.dji.flighthubsdk.bean.CommonPaginationData;
import com.dji.flighthubsdk.bean.SignalMapsResults;
import com.dji.flighthubsdk.bean.SyncFlightAreasResults;
import com.dji.flighthubsdk.bean.PaginationInfo;
import com.dji.flighthubsdk.bean.UpdateFlightAreasResults;
import com.dji.flighthubsdk.bean.UpdatedAreaData;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSconfig;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSPlayInfo;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.bean.CreateMaintenanceOrderResults;
import com.dji.flighthubsdk.bean.DeviceRepairConfig;
import com.dji.flighthubsdk.bean.DeviceRepairConfigResults;
import com.dji.flighthubsdk.bean.GeneratedOrderInfo;
import com.dji.flighthubsdk.bean.MaintenanceOrderAttachment;
import com.dji.flighthubsdk.bean.MaintenanceOrderContactAddress;
import com.dji.flighthubsdk.bean.MaintenanceOrderDetailResults;
import com.dji.flighthubsdk.bean.MaintenanceOrderLogReport;
import com.dji.flighthubsdk.bean.MaintenanceOrderRepairInfo;
import com.dji.flighthubsdk.bean.MaintenanceOrderStatusHistoryItem;
import com.dji.flighthubsdk.bean.MaintenanceOrdersListItem;
import com.dji.flighthubsdk.bean.MaintenanceOrdersListResults;
import com.dji.flighthubsdk.bean.RepairGuidance;
import com.dji.flighthubsdk.bean.AiAlertAlarmInfo;
import com.dji.flighthubsdk.bean.AiAlertAlarmTarget;
import com.dji.flighthubsdk.bean.AiAlertFlightItem;
import com.dji.flighthubsdk.bean.AiAlertLocation;
import com.dji.flighthubsdk.bean.AiAlertRecordItem;
import com.dji.flighthubsdk.bean.AiAlertTargetAlertInfo;
import com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingParam;
import com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingResult;
import com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingParam;
import com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingResult;
import com.dji.flighthubsdk.bean.GetAiAlertFlightsParam;
import com.dji.flighthubsdk.bean.GetAiAlertFlightsResult;
import com.dji.flighthubsdk.bean.GetAiAlertRecordsParam;
import com.dji.flighthubsdk.bean.GetAiAlertRecordsResult;
import com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingParam;
import com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingResult;
import com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingParam;
import com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingResult;
import com.dji.flighthubsdk.bean.LiveSyncCameraCapacity;
import com.dji.flighthubsdk.bean.LiveSyncCreateParams;
import com.dji.flighthubsdk.bean.LiveSyncCreateResult;
import com.dji.flighthubsdk.bean.LiveSyncDeviceItemV2;
import com.dji.flighthubsdk.bean.LiveSyncDeviceV2;
import com.dji.flighthubsdk.bean.LiveSyncDevicesV2Result;
import com.dji.flighthubsdk.bean.LiveSyncItem;
import com.dji.flighthubsdk.bean.LiveSyncListParams;
import com.dji.flighthubsdk.bean.LiveSyncListResult;
import com.dji.flighthubsdk.bean.LiveSyncSchemaOption;
import com.dji.flighthubsdk.bean.LiveSyncSummaryResult;
import com.dji.flighthubsdk.bean.LiveSyncUpdateParams;
import com.dji.flighthubsdk.bean.LiveSyncVideoCapacity;
import com.dji.flighthubsdk.value.CameraExposureCompensation;
import com.dji.flighthubsdk.value.CameraExposureMode;
import com.dji.flighthubsdk.value.CameraFocusMode;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.value.DockAirConditionerState;
import com.dji.flighthubsdk.value.DroneLinkState;
import com.dji.flighthubsdk.value.DroneModeCode;
import com.dji.flighthubsdk.value.IrGainMode;
import com.dji.flighthubsdk.value.MegaphoneTTSLanguageType;
import com.dji.flighthubsdk.value.MegaphoneTTSVoiceType;
import com.dji.flighthubsdk.value.NightSceneMode;
import com.dji.flighthubsdk.value.OutOfControlAction;
import com.dji.flighthubsdk.value.POITaskStatus;
import com.dji.flighthubsdk.value.PsdkSubType;
import com.dji.flighthubsdk.value.PsdkType;
import com.dji.flighthubsdk.value.SearchLightWorkMode;
import com.dji.flighthubsdk.value.TakeoffMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SDK 内部：SWIG / native 生成类型 ↔ 对外 Bean 的转换。
 * <p>对齐 Go {@code wrapper/convert.go} / {@code live_module.go} 中的拷贝逻辑；
 * 业务与 Demo 不应直接依赖本类。</p>
 */
public final class FlightHubSdkConvert {

    private FlightHubSdkConvert() {
    }

    // ── FlightHubError: generated → bean ──

    public static com.dji.flighthubsdk.bean.FlightHubError toFlightHubError(com.dji.flighthubsdk.generated.FlightHubError err) {
        if (err == null) {
            return new com.dji.flighthubsdk.bean.FlightHubError(
                com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED);
        }
        return new com.dji.flighthubsdk.bean.FlightHubError(
            err.getError_code(),
            err.getHttp_code(),
            err.getServer_code(),
            err.getDevice_code(),
            err.getServer_message());
    }

    /**
     * 在 JNI 回调内立即调用：从 SWIG 对象拷贝 primitive/值到 Bean，不向外传递 native 引用。
     * @param nativeStatus 参数
     */
    public static DroneOnlineStatus toDroneOnlineStatus(com.dji.flighthubsdk.generated.DroneOnlineStatus nativeStatus) {
        if (nativeStatus == null) {
            return new DroneOnlineStatus(false);
        }
        return new DroneOnlineStatus(nativeStatus.getIs_online());
    }

    /**
     * 若将来需要把 Bean 传回 C++（例如构造请求体），使用此方法生成 SWIG 对象。
     */
    public static com.dji.flighthubsdk.generated.DroneOnlineStatus toNativeDroneOnlineStatus(DroneOnlineStatus bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.DroneOnlineStatus n = new com.dji.flighthubsdk.generated.DroneOnlineStatus();
        n.setIs_online(bean.isOnline());
        return n;
    }

    public static DockOnlineStatus toDockOnlineStatus(com.dji.flighthubsdk.generated.DockOnlineStatus nativeStatus) {
        if (nativeStatus == null) {
            return new DockOnlineStatus(false);
        }
        return new DockOnlineStatus(nativeStatus.getIs_online());
    }

    public static com.dji.flighthubsdk.generated.DockOnlineStatus toNativeDockOnlineStatus(DockOnlineStatus bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.DockOnlineStatus n = new com.dji.flighthubsdk.generated.DockOnlineStatus();
        n.setIs_online(bean.isOnline());
        return n;
    }

    // --- Dock 监听 DTO（JNI 边界立即拷贝，不向外传递 SWIG 代理）---

    public static DockLocation toDockLocation(com.dji.flighthubsdk.generated.DockLocation n) {
        if (n == null) {
            return new DockLocation(0, 0, 0, 0);
        }
        return new DockLocation(n.getHeading(), n.getHeight(), n.getLatitude(), n.getLongitude());
    }

    public static DroneInDock toDroneInDock(com.dji.flighthubsdk.generated.DroneInDock n) {
        if (n == null) {
            return new DroneInDock(0);
        }
        return new DroneInDock(n.getDrone_in_dock());
    }

    public static DockCoverState toDockCoverState(com.dji.flighthubsdk.generated.DockCoverState n) {
        if (n == null) {
            return new DockCoverState(0);
        }
        return new DockCoverState(n.getCover_state());
    }

    public static DockModeCode toDockModeCode(com.dji.flighthubsdk.generated.DockModeCode n) {
        if (n == null) {
            return new DockModeCode(0);
        }
        return new DockModeCode(n.getMode_code());
    }

    public static FlightTaskStepCode toFlightTaskStepCode(com.dji.flighthubsdk.generated.FlightTaskStepCode n) {
        if (n == null) {
            return new FlightTaskStepCode(0);
        }
        return new FlightTaskStepCode(n.getFlighttask_step_code());
    }

    public static DockSubDeviceInfo toDockSubDeviceInfo(com.dji.flighthubsdk.generated.DockSubDeviceInfo n) {
        if (n == null) {
            return new DockSubDeviceInfo("", "", 0, 0);
        }
        return new DockSubDeviceInfo(
                n.getDevice_sn(),
                n.getDevice_model_key(),
                n.getDevice_online_status(),
                n.getDevice_paired());
    }

    public static DockSubDeviceInfoList toDockSubDeviceInfoList(com.dji.flighthubsdk.generated.DockSubDeviceInfoList n) {
        if (n == null) {
            return new DockSubDeviceInfoList(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.DockSubDeviceInfoVector v = n.getSub_device_info_vec();
        if (v == null || v.isEmpty()) {
            return new DockSubDeviceInfoList(Collections.emptyList());
        }
        int sz = v.size();
        List<DockSubDeviceInfo> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.DockSubDeviceInfo e = v.get(i);
            if (e != null) {
                out.add(toDockSubDeviceInfo(e));
            }
        }
        return new DockSubDeviceInfoList(Collections.unmodifiableList(out));
    }

    public static DockNetworkState toDockNetworkState(com.dji.flighthubsdk.generated.DockNetworkState n) {
        if (n == null) {
            return new DockNetworkState(0, 0, 0);
        }
        return new DockNetworkState(n.getType(), n.getQuality(), n.getRate());
    }

    public static DockMediaFileDetail toDockMediaFileDetail(com.dji.flighthubsdk.generated.DockMediaFileDetail n) {
        if (n == null) {
            return new DockMediaFileDetail(0);
        }
        return new DockMediaFileDetail(n.getRemain_upload());
    }

    public static DockWirelessLink toDockWirelessLink(com.dji.flighthubsdk.generated.DockWirelessLink n) {
        if (n == null) {
            return new DockWirelessLink(0, 0, 0, 0, 0, 0, 0, 0, 0f, 0f);
        }
        return new DockWirelessLink(
                n.getDongle_num(),
                n.getLink_state_4g(),
                n.getSdr_link_state(),
                n.getLink_workmode(),
                n.getSdr_quality(),
                n.getQuality_4g(),
                n.getUav_quality_4g(),
                n.getGnd_quality_4g(),
                n.getSdr_freq_band(),
                n.getFreq_band_4g());
    }

    public static DockJobNumber toDockJobNumber(com.dji.flighthubsdk.generated.DockJobNumber n) {
        if (n == null) {
            return new DockJobNumber(0);
        }
        return new DockJobNumber(n.getJob_number());
    }

    public static DockPowerState toDockPowerState(com.dji.flighthubsdk.generated.DockPowerState n) {
        if (n == null) {
            return new DockPowerState(0, 0, 0);
        }
        return new DockPowerState(
                n.getElectric_supply_voltage(), n.getWorking_voltage(), n.getWorking_current());
    }

    public static DockStorageState toDockStorageState(com.dji.flighthubsdk.generated.DockStorageState n) {
        if (n == null) {
            return new DockStorageState(0, 0);
        }
        return new DockStorageState(n.getTotal(), n.getUsed());
    }

    public static DockFirstPowerOnTime toDockFirstPowerOnTime(com.dji.flighthubsdk.generated.DockFirstPowerOnTime n) {
        if (n == null) {
            return new DockFirstPowerOnTime("");
        }
        return new DockFirstPowerOnTime(n.getFirst_power_on());
    }

    public static DockAccTime toDockAccTime(com.dji.flighthubsdk.generated.DockAccTime n) {
        if (n == null) {
            return new DockAccTime(0);
        }
        return new DockAccTime(n.getAcc_time());
    }

    public static DockAlternateLandPoint toDockAlternateLandPoint(com.dji.flighthubsdk.generated.DockAlternateLandPoint n) {
        if (n == null) {
            return new DockAlternateLandPoint(0, 0, 0, 0, 0);
        }
        return new DockAlternateLandPoint(
                n.getIs_configured(),
                n.getLongitude(),
                n.getLatitude(),
                n.getSafe_land_height(),
                n.getHeight());
    }

    public static DockPositionState toDockPositionState(com.dji.flighthubsdk.generated.DockPositionState n) {
        if (n == null) {
            return new DockPositionState(0, 0, 0, 0, 0);
        }
        return new DockPositionState(
                n.getIs_calibration(),
                n.getIs_fixed(),
                n.getQuality(),
                n.getGps_number(),
                n.getRtk_number());
    }

    public static DockActivationTime toDockActivationTime(com.dji.flighthubsdk.generated.DockActivationTime n) {
        if (n == null) {
            return new DockActivationTime(0);
        }
        return new DockActivationTime(n.getActivation_time());
    }

    public static DockBatteryStoreMode toDockBatteryStoreMode(com.dji.flighthubsdk.generated.DockBatteryStoreMode n) {
        if (n == null) {
            return new DockBatteryStoreMode(0);
        }
        return new DockBatteryStoreMode(n.getBattery_store_mode());
    }

    public static DockAlarmState toDockAlarmState(com.dji.flighthubsdk.generated.DockAlarmState n) {
        if (n == null) {
            return new DockAlarmState(0);
        }
        return new DockAlarmState(n.getAlarm_state());
    }

    public static BatteryBasicInfo toBatteryBasicInfo(com.dji.flighthubsdk.generated.BatteryBasicInfo n) {
        if (n == null) {
            return new BatteryBasicInfo(0, 0, 0, 0);
        }
        return new BatteryBasicInfo(
                n.getIndex(), n.getCapacity_percent(), n.getVoltage(), n.getTemperature());
    }

    public static List<BatteryBasicInfo> toBatteryBasicInfoList(
            com.dji.flighthubsdk.generated.BatteryBasicInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        int sz = v.size();
        List<BatteryBasicInfo> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.BatteryBasicInfo e = v.get(i);
            if (e != null) {
                out.add(toBatteryBasicInfo(e));
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static DockDroneBatteryMaintenanceInfo toDockDroneBatteryMaintenanceInfo(
            com.dji.flighthubsdk.generated.DockDroneBatteryMaintenanceInfo n) {
        if (n == null) {
            return new DockDroneBatteryMaintenanceInfo(0, 0, 0, Collections.emptyList());
        }
        return new DockDroneBatteryMaintenanceInfo(
                n.getMaintenance_state(),
                n.getMaintenance_time_left(),
                n.getHeat_state(),
                toBatteryBasicInfoList(n.getBatteries()));
    }

    public static DockEmergencyStopState toDockEmergencyStopState(com.dji.flighthubsdk.generated.DockEmergencyStopState n) {
        if (n == null) {
            return new DockEmergencyStopState(0);
        }
        return new DockEmergencyStopState(n.getEmergency_stop_state());
    }

    public static DockSilentMode toDockSilentMode(com.dji.flighthubsdk.generated.DockSilentMode n) {
        if (n == null) {
            return new DockSilentMode(0);
        }
        return new DockSilentMode(n.getSilent_mode());
    }

    public static DockUserExperienceImprovement toDockUserExperienceImprovement(
            com.dji.flighthubsdk.generated.DockUserExperienceImprovement n) {
        if (n == null) {
            return new DockUserExperienceImprovement(0);
        }
        return new DockUserExperienceImprovement(n.getUser_experience_improvement());
    }

    public static DockDrcState toDockDrcState(com.dji.flighthubsdk.generated.DockDrcState n) {
        if (n == null) {
            return new DockDrcState(0);
        }
        return new DockDrcState(n.getDrc_state());
    }

    public static DockPutterState toDockPutterState(com.dji.flighthubsdk.generated.DockPutterState n) {
        if (n == null) {
            return new DockPutterState(0);
        }
        return new DockPutterState(n.getPutter_state());
    }

    public static DockRelativeAlternateLandPoint toDockRelativeAlternateLandPoint(
            com.dji.flighthubsdk.generated.DockRelativeAlternateLandPoint n) {
        if (n == null) {
            return new DockRelativeAlternateLandPoint(0, 0, 0, 0);
        }
        return new DockRelativeAlternateLandPoint(
                n.getLongitude(), n.getLatitude(), n.getSafe_land_height(), n.getStatus());
    }

    public static DockSelfConvergeCoordinate toDockSelfConvergeCoordinate(
            com.dji.flighthubsdk.generated.DockSelfConvergeCoordinate n) {
        if (n == null) {
            return new DockSelfConvergeCoordinate(0, 0, 0);
        }
        return new DockSelfConvergeCoordinate(n.getHeight(), n.getLongitude(), n.getLatitude());
    }

    public static DockDroneChargeState toDockDroneChargeState(com.dji.flighthubsdk.generated.DockDroneChargeState n) {
        if (n == null) {
            return new DockDroneChargeState(0, 0);
        }
        return new DockDroneChargeState(n.getState(), n.getCapacity_percent());
    }

    public static DockMeteorologicalInfo toDockMeteorologicalInfo(com.dji.flighthubsdk.generated.DockMeteorologicalInfo n) {
        if (n == null) {
            return new DockMeteorologicalInfo(0, 0, 0, 0, 0);
        }
        return new DockMeteorologicalInfo(
                n.getTemperature(),
                n.getEnvironment_temperature(),
                n.getHumidity(),
                n.getWind_speed(),
                n.getRainfall());
    }

    public static DockBackupBatteryInfo toDockBackupBatteryInfo(com.dji.flighthubsdk.generated.DockBackupBatteryInfo n) {
        if (n == null) {
            return new DockBackupBatteryInfo(0, 0, 0);
        }
        return new DockBackupBatteryInfo(n.getSwitch_state(), n.getVoltage(), n.getTemperature());
    }

    public static DockMaintainStatus toDockMaintainStatus(com.dji.flighthubsdk.generated.DockMaintainStatus n) {
        if (n == null) {
            return new DockMaintainStatus(0, 0, 0L, 0);
        }
        return new DockMaintainStatus(
                n.getState(),
                n.getLast_maintain_type(),
                n.getLast_maintain_time(),
                n.getLast_maintain_work_sorties());
    }

    public static DockMaintainStatusInfo toDockMaintainStatusInfo(com.dji.flighthubsdk.generated.DockMaintainStatusInfo n) {
        if (n == null) {
            return new DockMaintainStatusInfo(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.DockMaintainStatusVector v = n.getMaintain_status_info();
        if (v == null || v.isEmpty()) {
            return new DockMaintainStatusInfo(Collections.emptyList());
        }
        int sz = v.size();
        List<DockMaintainStatus> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.DockMaintainStatus e = v.get(i);
            if (e != null) {
                out.add(toDockMaintainStatus(e));
            }
        }
        return new DockMaintainStatusInfo(Collections.unmodifiableList(out));
    }

    public static DockFirmwareInfo toDockFirmwareInfo(com.dji.flighthubsdk.generated.DockFirmwareInfo n) {
        if (n == null) {
            return new DockFirmwareInfo("", 0, 0);
        }
        return new DockFirmwareInfo(
                n.getFirmware_version(), n.getCompatible_status(), n.getFirmware_upgrade_status());
    }

    public static DockAirConditionerStatusInfo toDockAirConditionerStatusInfo(
            com.dji.flighthubsdk.generated.DockAirConditionerStatusInfo n) {
        if (n == null) {
            return new DockAirConditionerStatusInfo(DockAirConditionerState.fromValue(0), 0);
        }
        return new DockAirConditionerStatusInfo(
                DockAirConditionerState.fromValue(n.getAir_conditioner_state()),
                n.getSwitch_time());
    }

    public static DockSupplementLightState toDockSupplementLightState(
            com.dji.flighthubsdk.generated.DockSupplementLightState n) {
        if (n == null) {
            return new DockSupplementLightState(0);
        }
        return new DockSupplementLightState(n.getSupplement_light_state());
    }

    // --- FlyTo（JNI 边界立即拷贝）---

    public static FlyToDroneSelfPlanPathPoint toFlyToDroneSelfPlanPathPoint(
            com.dji.flighthubsdk.generated.FlyToDroneSelfPlanPathPoint n) {
        if (n == null) {
            return new FlyToDroneSelfPlanPathPoint(0, 0, 0);
        }
        return new FlyToDroneSelfPlanPathPoint(n.getLatitude(), n.getLongitude(), n.getHeight());
    }

    public static FlyToDroneSelfPlanPath toFlyToDroneSelfPlanPath(
            com.dji.flighthubsdk.generated.FlyToDroneSelfPlanPath n) {
        if (n == null) {
            return new FlyToDroneSelfPlanPath("", "", 0, Collections.emptyList(), 0, 0);
        }
        com.dji.flighthubsdk.generated.FlyToDroneSelfPlanPathPointVector v = n.getPlanned_path_points();
        List<FlyToDroneSelfPlanPathPoint> points;
        if (v == null || v.isEmpty()) {
            points = Collections.emptyList();
        } else {
            int sz = v.size();
            List<FlyToDroneSelfPlanPathPoint> out = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                com.dji.flighthubsdk.generated.FlyToDroneSelfPlanPathPoint p = v.get(i);
                if (p != null) {
                    out.add(toFlyToDroneSelfPlanPathPoint(p));
                }
            }
            points = Collections.unmodifiableList(out);
        }
        return new FlyToDroneSelfPlanPath(
                n.getFly_to_id(),
                n.getSn(),
                n.getTimestamp(),
                points,
                n.getRemaining_distance(),
                n.getRemaining_time());
    }

    public static FlyToPointProgress toFlyToPointProgress(com.dji.flighthubsdk.generated.FlyToPointProgress n) {
        if (n == null) {
            return new FlyToPointProgress("", "", 0, "", "", 0, "", "", 0, 0);
        }
        return new FlyToPointProgress(
                n.getBid(),
                n.getTid(),
                n.getTimestamp(),
                n.getFly_to_id(),
                n.getFlight_id(),
                n.getTask_seq(),
                n.getSn(),
                n.getStatus(),
                n.getResult(),
                n.getWaypoint_index());
    }

    public static DeviceOtaProgressInfo toDeviceOtaProgressInfo(
            com.dji.flighthubsdk.generated.DeviceOtaProgressInfo n) {
        if (n == null) {
            return new DeviceOtaProgressInfo(0, 0, "");
        }
        return new DeviceOtaProgressInfo(n.getPercent(), n.getCurrent_step(), safeString(n.getExt()));
    }

    public static DeviceOtaProgress toDeviceOtaProgress(com.dji.flighthubsdk.generated.DeviceOtaProgress n) {
        if (n == null) {
            return new DeviceOtaProgress("", 0, "", 0, toDeviceOtaProgressInfo(null));
        }
        return new DeviceOtaProgress(
                safeString(n.getBid()),
                n.getTimestamp(),
                safeString(n.getStatus()),
                n.getResult(),
                toDeviceOtaProgressInfo(n.getProgress()));
    }

    // --- PSDK 监听 DTO（JNI 边界立即拷贝）---

    public static PsdkDeviceInfo toPsdkDeviceInfo(com.dji.flighthubsdk.generated.PsdkDeviceInfo n) {
        if (n == null) {
            return new PsdkDeviceInfo(0, "");
        }
        return new PsdkDeviceInfo(n.getPsdk_index(), n.getObject_key());
    }

    public static PsdkDeviceList toPsdkDeviceList(com.dji.flighthubsdk.generated.PsdkDeviceList n) {
        if (n == null) {
            return new PsdkDeviceList(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.PsdkDeviceInfoVector v = n.getPsdk_device_list();
        if (v == null || v.isEmpty()) {
            return new PsdkDeviceList(Collections.emptyList());
        }
        int sz = v.size();
        List<PsdkDeviceInfo> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.PsdkDeviceInfo e = v.get(i);
            if (e != null) {
                out.add(toPsdkDeviceInfo(e));
            }
        }
        return new PsdkDeviceList(Collections.unmodifiableList(out));
    }

    public static PsdkStateSummaryInfo toPsdkStateSummaryInfo(com.dji.flighthubsdk.generated.PsdkStateSummaryInfo n) {
        if (n == null) {
            return new PsdkStateSummaryInfo(0, PsdkType.UNKNOWN, PsdkSubType.UNKNOWN, "", "", "", "", -1);
        }
        return new PsdkStateSummaryInfo(
                n.getPsdk_index(),
                PsdkType.fromValue(n.getPsdk_type()),
                PsdkSubType.fromValue(n.getPsdk_sub_type()),
                n.getPsdk_name(),
                n.getPsdk_sn(),
                n.getPsdk_version(),
                n.getPsdk_lib_version(),
                n.getPsdk_restrict_state());
    }

    public static PsdkStateSpeakerInfo toPsdkStateSpeakerInfo(com.dji.flighthubsdk.generated.PsdkStateSpeakerInfo n) {
        if (n == null) {
            return new PsdkStateSpeakerInfo(0, 0, 0, 0, "", "", 0, 0, 0, 50);
        }
        return new PsdkStateSpeakerInfo(
                n.getWork_mode(),
                n.getPlay_mode(),
                n.getSystem_state(),
                n.getPlay_volume(),
                n.getPlay_file_name(),
                n.getPlay_file_md5(),
                n.getTts_volume(),
                n.getTts_type(),
                n.getTts_language(),
                n.getTts_speed());
    }

    public static PsdkStateSearchlightInfo toPsdkStateSearchlightInfo(
            com.dji.flighthubsdk.generated.PsdkStateSearchlightInfo n) {
        if (n == null) {
            return new PsdkStateSearchlightInfo(
                    SearchLightWorkMode.fromValue(0),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0);
        }
        return new PsdkStateSearchlightInfo(
                SearchLightWorkMode.fromValue(n.getWork_mode()),
                n.getBrightness(),
                n.getCalibration_status(),
                n.getCalibration_progress(),
                n.getLeft_value(),
                n.getRight_value(),
                n.getWide_field_mode(),
                n.getLight_gimbal_control(),
                n.getLighting_type());
    }

    public static PsdkSpeakerPlayProgressInfo toPsdkSpeakerPlayProgressInfo(
            com.dji.flighthubsdk.generated.PsdkSpeakerPlayProgressInfo n) {
        if (n == null) {
            return new PsdkSpeakerPlayProgressInfo(0, 0, "", "", 0, "");
        }
        return new PsdkSpeakerPlayProgressInfo(
                n.getPsdk_index(),
                n.getResult(),
                n.getStatus(),
                n.getStep_key(),
                n.getPercent(),
                n.getMd5());
    }

    public static PsdkFloatingWindowTextInfo toPsdkFloatingWindowTextInfo(
            com.dji.flighthubsdk.generated.PsdkFloatingWindowTextInfo n) {
        if (n == null) {
            return new PsdkFloatingWindowTextInfo(0, "");
        }
        return new PsdkFloatingWindowTextInfo(n.getPsdk_index(), n.getFloating_window_text());
    }

    // --- 飞行任务 / 控制权限监听 DTO（JNI 边界立即拷贝）---

    public static FlightTaskExceptionDetail toFlightTaskExceptionDetail(
            com.dji.flighthubsdk.generated.FlightTaskExceptionDetail n) {
        if (n == null) {
            return new FlightTaskExceptionDetail(0, "", 0, "", "");
        }
        return new FlightTaskExceptionDetail(
                n.getCode(),
                safeString(n.getMessage()),
                n.getTask_id(),
                safeString(n.getHappen_at()),
                safeString(n.getSn()));
    }

    public static FlightTaskException toFlightTaskException(com.dji.flighthubsdk.generated.FlightTaskException n) {
        if (n == null) {
            return new FlightTaskException("", "", "", 0, 0, 0, Collections.emptyList(), 0L);
        }
        com.dji.flighthubsdk.generated.FlightTaskExceptionDetailVector v = n.getExceptions();
        List<FlightTaskExceptionDetail> details;
        if (v == null || v.isEmpty()) {
            details = Collections.emptyList();
        } else {
            int sz = v.size();
            List<FlightTaskExceptionDetail> out = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                com.dji.flighthubsdk.generated.FlightTaskExceptionDetail e = v.get(i);
                if (e != null) {
                    out.add(toFlightTaskExceptionDetail(e));
                }
            }
            details = Collections.unmodifiableList(out);
        }
        return new FlightTaskException(
                safeString(n.getUuid()),
                safeString(n.getName()),
                safeString(n.getSn()),
                n.getTask_type(),
                n.getStatus(),
                n.getProgress(),
                details,
                n.getTimestamp());
    }

    public static FlightTaskEventBreakPointResume toFlightTaskEventBreakPointResume(
            com.dji.flighthubsdk.generated.FlightTaskEventBreakPointResume n) {
        if (n == null) {
            return new FlightTaskEventBreakPointResume("", "", "", "", "");
        }
        return new FlightTaskEventBreakPointResume(
                safeString(n.getTask_uuid()),
                safeString(n.getParent_uuid()),
                safeString(n.getGroup_uuid()),
                safeString(n.getSn()),
                safeString(n.getLanding_dock_sn()));
    }

    public static FlightTaskEventContinuousSubTask toFlightTaskEventContinuousSubTask(
            com.dji.flighthubsdk.generated.FlightTaskEventContinuousSubTask n) {
        if (n == null) {
            return new FlightTaskEventContinuousSubTask("", "", "", "", "");
        }
        return new FlightTaskEventContinuousSubTask(
                safeString(n.getTask_uuid()),
                safeString(n.getParent_uuid()),
                safeString(n.getGroup_uuid()),
                safeString(n.getSn()),
                safeString(n.getLanding_dock_sn()));
    }

    public static TasksMediaStatus toTasksMediaStatus(com.dji.flighthubsdk.generated.TasksMediaStatus n) {
        if (n == null) {
            return new TasksMediaStatus("", 0);
        }
        return new TasksMediaStatus(safeString(n.getTask_uuid()), n.getMedia_status());
    }

    public static FlightTasksMediaStatusChanged toFlightTasksMediaStatusChanged(
            com.dji.flighthubsdk.generated.FlightTasksMediaStatusChanged n) {
        if (n == null) {
            return new FlightTasksMediaStatusChanged(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.TasksMediaStatusVector v = n.getTask_media();
        if (v == null || v.isEmpty()) {
            return new FlightTasksMediaStatusChanged(Collections.emptyList());
        }
        int sz = v.size();
        List<TasksMediaStatus> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.TasksMediaStatus e = v.get(i);
            if (e != null) {
                out.add(toTasksMediaStatus(e));
            }
        }
        return new FlightTasksMediaStatusChanged(Collections.unmodifiableList(out));
    }

    public static FlightTask toFlightTask(com.dji.flighthubsdk.generated.FlightTask n) {
        if (n == null) {
            return new FlightTask("", "", 0, 0, 0, "", "", 0L, 0L, 0, 0, 0L, "", "", "");
        }
        return new FlightTask(
                safeString(n.getUuid()),
                safeString(n.getName()),
                n.getTask_type(),
                n.getStatus(),
                n.getProgress(),
                safeString(n.getSn()),
                safeString(n.getFolder_info()),
                n.getRun_at(),
                n.getComplete_at(),
                n.getTotal_waypoints(),
                n.getCurrent_waypoint_index(),
                n.getProgress_version(),
                safeString(n.getResumable_status()),
                safeString(n.getObstacle_avoidance_notify()),
                safeString(n.getWayline_uuid()));
    }

    public static FlightTasks toFlightTasks(com.dji.flighthubsdk.generated.FlightTasks n) {
        if (n == null) {
            return new FlightTasks(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.FlightTaskVector v = n.getFlight_tasks();
        if (v == null || v.isEmpty()) {
            return new FlightTasks(Collections.emptyList());
        }
        int sz = v.size();
        List<FlightTask> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.FlightTask e = v.get(i);
            if (e != null) {
                out.add(toFlightTask(e));
            }
        }
        return new FlightTasks(Collections.unmodifiableList(out));
    }

    public static FlightTasksMediaFileDisplayAvailable toFlightTasksMediaFileDisplayAvailable(
            com.dji.flighthubsdk.generated.FlightTasksMediaFileDisplayAvailable n) {
        if (n == null) {
            return new FlightTasksMediaFileDisplayAvailable("", 0, 0, 0, 0, "", "", 0, "");
        }
        return new FlightTasksMediaFileDisplayAvailable(
                safeString(n.getUuid()),
                n.getIdx_parent_id(),
                n.getFile_type(),
                n.getSub_file_type(),
                n.getMedia_file_id(),
                safeString(n.getFlight_id()),
                safeString(n.getSn()),
                n.getMedia_source(),
                safeString(n.getUrl()));
    }

    public static ControlRightsCloudControlStatusItem toControlRightsCloudControlStatusItem(
            com.dji.flighthubsdk.generated.ControlRightsCloudControlStatusItem n) {
        if (n == null) {
            return new ControlRightsCloudControlStatusItem("", "", "", false, 0);
        }
        return new ControlRightsCloudControlStatusItem(
                safeString(n.getType()),
                safeString(n.getKey()),
                safeString(n.getUser_id()),
                n.getIs_locked(),
                n.getVersion());
    }

    public static ControlRightsCloudControlStatus toControlRightsCloudControlStatus(
            com.dji.flighthubsdk.generated.ControlRightsCloudControlStatus n) {
        if (n == null) {
            return new ControlRightsCloudControlStatus("", "", Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.ControlRightsCloudControlStatusItemVector v = n.getControls();
        List<ControlRightsCloudControlStatusItem> items;
        if (v == null || v.isEmpty()) {
            items = Collections.emptyList();
        } else {
            int sz = v.size();
            List<ControlRightsCloudControlStatusItem> out = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                com.dji.flighthubsdk.generated.ControlRightsCloudControlStatusItem e = v.get(i);
                if (e != null) {
                    out.add(toControlRightsCloudControlStatusItem(e));
                }
            }
            items = Collections.unmodifiableList(out);
        }
        return new ControlRightsCloudControlStatus(safeString(n.getSn()), safeString(n.getReason()), items);
    }

    public static ControlRightsDeviceControlStatusItem toControlRightsDeviceControlStatusItem(
            com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatusItem n) {
        if (n == null) {
            return new ControlRightsDeviceControlStatusItem("", "", "", 0);
        }
        return new ControlRightsDeviceControlStatusItem(
                safeString(n.getType()),
                safeString(n.getKey()),
                safeString(n.getOwner()),
                n.getVersion());
    }

    public static ControlRightsDeviceControlStatus toControlRightsDeviceControlStatus(
            com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatus n) {
        if (n == null) {
            return new ControlRightsDeviceControlStatus("", "", Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatusItemVector v = n.getControls();
        List<ControlRightsDeviceControlStatusItem> items;
        if (v == null || v.isEmpty()) {
            items = Collections.emptyList();
        } else {
            int sz = v.size();
            List<ControlRightsDeviceControlStatusItem> out = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatusItem e = v.get(i);
                if (e != null) {
                    out.add(toControlRightsDeviceControlStatusItem(e));
                }
            }
            items = Collections.unmodifiableList(out);
        }
        return new ControlRightsDeviceControlStatus(safeString(n.getSn()), safeString(n.getReason()), items);
    }

    /**
     * 拷贝单个 AI 检测目标（{@code vector::get(i)} 返回的 SWIG 代理勿长期持有）。
     * @param nativeTarget 参数
     */
    public static AiDetectionTarget toAiDetectionTarget(com.dji.flighthubsdk.generated.AiDetectionTarget nativeTarget) {
        if (nativeTarget == null) {
            return null;
        }
        return new AiDetectionTarget(
                nativeTarget.getTarget_id(),
                nativeTarget.getTarget_type(),
                nativeTarget.getTarget_state(),
                nativeTarget.getTarget_cx(),
                nativeTarget.getTarget_cy(),
                nativeTarget.getTarget_w(),
                nativeTarget.getTarget_h(),
                nativeTarget.getTarget_distance());
    }

    /**
     * 将 {@link com.dji.flighthubsdk.generated.AiDetectionTargetVector} 转为普通 {@link List}（深拷贝元素值）。
     */
    public static List<AiDetectionTarget> toAiDetectionTargetList(
            com.dji.flighthubsdk.generated.AiDetectionTargetVector nativeVector) {
        if (nativeVector == null || nativeVector.isEmpty()) {
            return Collections.emptyList();
        }
        int n = nativeVector.size();
        List<AiDetectionTarget> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            com.dji.flighthubsdk.generated.AiDetectionTarget t = nativeVector.get(i);
            if (t != null) {
                out.add(toAiDetectionTarget(t));
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * 拷贝 AI SEI 信息（含 targets 列表），对齐 Go {@code aiSeiInfoCallback.OnAiSeiInfo}。
     * @param nativeInfo 参数
     */
    public static AiSeiInfo toAiSeiInfo(com.dji.flighthubsdk.generated.AiSeiInfo nativeInfo) {
        if (nativeInfo == null) {
            return new AiSeiInfo(Collections.emptyList());
        }
        return new AiSeiInfo(toAiDetectionTargetList(nativeInfo.getTargets()));
    }

    /**
     * 若将来需要把 Bean 写回 native（例如构造 SEI），可由此组装 SWIG 对象。
     */
    public static com.dji.flighthubsdk.generated.AiDetectionTargetVector toNativeAiDetectionTargetVector(
            List<AiDetectionTarget> beans) {
        com.dji.flighthubsdk.generated.AiDetectionTargetVector v =
                new com.dji.flighthubsdk.generated.AiDetectionTargetVector();
        if (beans == null) {
            return v;
        }
        for (AiDetectionTarget b : beans) {
            if (b != null) {
                v.add(toNativeAiDetectionTarget(b));
            }
        }
        return v;
    }

    public static com.dji.flighthubsdk.generated.AiDetectionTarget toNativeAiDetectionTarget(AiDetectionTarget bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AiDetectionTarget n =
                new com.dji.flighthubsdk.generated.AiDetectionTarget();
        n.setTarget_id(bean.getTargetId());
        n.setTarget_type(bean.getTargetType());
        n.setTarget_state(bean.getTargetState());
        n.setTarget_cx(bean.getTargetCx());
        n.setTarget_cy(bean.getTargetCy());
        n.setTarget_w(bean.getTargetW());
        n.setTarget_h(bean.getTargetH());
        n.setTarget_distance(bean.getTargetDistance());
        return n;
    }

    public static com.dji.flighthubsdk.generated.AiSeiInfo toNativeAiSeiInfo(AiSeiInfo bean) {
        com.dji.flighthubsdk.generated.AiSeiInfo n = new com.dji.flighthubsdk.generated.AiSeiInfo();
        if (bean != null) {
            n.setTargets(toNativeAiDetectionTargetVector(bean.getTargets()));
        }
        return n;
    }

    // --- 阶段 2：Drone 链路状态 ---

    public static LinkStatus toLinkStatus(com.dji.flighthubsdk.generated.LinkStatus nativeStatus) {
        if (nativeStatus == null) {
            return new LinkStatus(DroneLinkState.UNKNOWN, DroneLinkState.UNKNOWN, DroneLinkState.UNKNOWN);
        }
        return new LinkStatus(
                DroneLinkState.fromValue(nativeStatus.getCtrl_link_status()),
                DroneLinkState.fromValue(nativeStatus.getData_link_status()),
                DroneLinkState.fromValue(nativeStatus.getLive_link_status()));
    }

    public static com.dji.flighthubsdk.generated.LinkStatus toNativeLinkStatus(LinkStatus bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.LinkStatus n = new com.dji.flighthubsdk.generated.LinkStatus();
        n.setCtrl_link_status(toNativeDroneLinkStateInt(bean.getCtrlLinkStatus()));
        n.setData_link_status(toNativeDroneLinkStateInt(bean.getDataLinkStatus()));
        n.setLive_link_status(toNativeDroneLinkStateInt(bean.getLiveLinkStatus()));
        return n;
    }

    // --- 阶段 2.2：Drone OSD 轻量监听（姿态 / 位置 / 模式 / 风速 / 起飞进度）---

    public static AircraftAttitude toAircraftAttitude(com.dji.flighthubsdk.generated.AircraftAttitude nativeAttitude) {
        if (nativeAttitude == null) {
            return new AircraftAttitude(0.0, 0.0, 0.0);
        }
        return new AircraftAttitude(
                nativeAttitude.getAttitude_head(),
                nativeAttitude.getAttitude_pitch(),
                nativeAttitude.getAttitude_roll());
    }

    public static com.dji.flighthubsdk.generated.AircraftAttitude toNativeAircraftAttitude(AircraftAttitude bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftAttitude n = new com.dji.flighthubsdk.generated.AircraftAttitude();
        n.setAttitude_head(bean.getAttitudeHead());
        n.setAttitude_pitch(bean.getAttitudePitch());
        n.setAttitude_roll(bean.getAttitudeRoll());
        return n;
    }

    public static AircraftLocation toAircraftLocation(com.dji.flighthubsdk.generated.AircraftLocation nativeLocation) {
        if (nativeLocation == null) {
            return new AircraftLocation(0.0, 0.0, 0.0, 0.0);
        }
        return new AircraftLocation(
                nativeLocation.getHeight(),
                nativeLocation.getElevation(),
                nativeLocation.getLongitude(),
                nativeLocation.getLatitude());
    }

    public static com.dji.flighthubsdk.generated.AircraftLocation toNativeAircraftLocation(AircraftLocation bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftLocation n = new com.dji.flighthubsdk.generated.AircraftLocation();
        n.setHeight(bean.getHeight());
        n.setElevation(bean.getElevation());
        n.setLongitude(bean.getLongitude());
        n.setLatitude(bean.getLatitude());
        return n;
    }

    /** native {@code int} → 对外 {@link CameraMode}（与 {@code value.h} 一致）。 */
    public static CameraMode toValueCameraMode(int v) {
        return CameraMode.fromValue(v);
    }

    /** 对外枚举 → native {@code CameraState::camera_mode} / {@code SwitchCameraModeParams::camera_mode} 整型。 */
    public static int toNativeCameraModeInt(CameraMode mode) {
        if (mode == null) {
            return CameraMode.UNKNOWN.getValue();
        }
        return mode.getValue();
    }

    /** native {@code int} → {@link CameraExposureMode}。 */
    public static CameraExposureMode toValueCameraExposureMode(int v) {
        return CameraExposureMode.fromValue(v);
    }

    /** → native 曝光模式整型。 */
    public static int toNativeCameraExposureModeInt(CameraExposureMode mode) {
        if (mode == null) {
            return CameraExposureMode.UNKNOWN.getValue();
        }
        return mode.getValue();
    }

    public static CameraExposureCompensation toValueCameraExposureCompensation(int v) {
        return CameraExposureCompensation.fromValue(v);
    }

    public static int toNativeCameraExposureCompensationInt(CameraExposureCompensation v) {
        if (v == null) {
            return CameraExposureCompensation.UNKNOWN.getValue();
        }
        return v.getValue();
    }

    public static CameraFocusMode toValueCameraFocusMode(int v) {
        return CameraFocusMode.fromValue(v);
    }

    public static int toNativeCameraFocusModeInt(CameraFocusMode v) {
        if (v == null) {
            return CameraFocusMode.UNKNOWN.getValue();
        }
        return v.getValue();
    }

    public static AircraftModeCode toAircraftModeCode(com.dji.flighthubsdk.generated.AircraftModeCode nativeMode) {
        if (nativeMode == null) {
            return new AircraftModeCode(DroneModeCode.UNKNOWN);
        }
        return new AircraftModeCode(DroneModeCode.fromValue(nativeMode.getMode_code()));
    }

    public static com.dji.flighthubsdk.generated.AircraftModeCode toNativeAircraftModeCode(AircraftModeCode bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftModeCode n = new com.dji.flighthubsdk.generated.AircraftModeCode();
        n.setMode_code(toNativeDroneModeCodeInt(bean.getModeCode()));
        return n;
    }

    /** native {@code int} → 对外 {@link DroneModeCode}（与 {@code value.h} 一致）。 */
    public static DroneModeCode toValueDroneModeCode(int v) {
        return DroneModeCode.fromValue(v);
    }

    /** 对外枚举 → native {@code AircraftModeCode::mode_code} 整型。 */
    public static int toNativeDroneModeCodeInt(DroneModeCode mode) {
        if (mode == null) {
            return DroneModeCode.UNKNOWN.getValue();
        }
        return mode.getValue();
    }

    public static AircraftWindSpeed toAircraftWindSpeed(com.dji.flighthubsdk.generated.AircraftWindSpeed nativeWind) {
        if (nativeWind == null) {
            return new AircraftWindSpeed(0, 0.0);
        }
        return new AircraftWindSpeed(nativeWind.getWind_direction(), nativeWind.getWind_speed());
    }

    public static com.dji.flighthubsdk.generated.AircraftWindSpeed toNativeAircraftWindSpeed(AircraftWindSpeed bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftWindSpeed n = new com.dji.flighthubsdk.generated.AircraftWindSpeed();
        n.setWind_direction(bean.getWindDirection());
        n.setWind_speed(bean.getWindSpeed());
        return n;
    }

    public static AircraftTakeoffProgress toAircraftTakeoffProgress(
            com.dji.flighthubsdk.generated.AircraftTakeoffProgress nativeProgress) {
        if (nativeProgress == null) {
            return new AircraftTakeoffProgress("", "", "", "", "", "", 0, 0, "", 0L);
        }
        return new AircraftTakeoffProgress(
                safeString(nativeProgress.getBid()),
                safeString(nativeProgress.getTid()),
                safeString(nativeProgress.getFlight_id()),
                safeString(nativeProgress.getTrack_id()),
                safeString(nativeProgress.getSn()),
                safeString(nativeProgress.getStatus()),
                nativeProgress.getResult(),
                nativeProgress.getWaypoint_index(),
                safeString(nativeProgress.getTakeoff_mode()),
                nativeProgress.getTimestamp());
    }

    public static com.dji.flighthubsdk.generated.AircraftTakeoffProgress toNativeAircraftTakeoffProgress(
            AircraftTakeoffProgress bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftTakeoffProgress n =
                new com.dji.flighthubsdk.generated.AircraftTakeoffProgress();
        n.setBid(bean.getBid());
        n.setTid(bean.getTid());
        n.setFlight_id(bean.getFlightId());
        n.setTrack_id(bean.getTrackId());
        n.setSn(bean.getSn());
        n.setStatus(bean.getStatus());
        n.setResult(bean.getResult());
        n.setWaypoint_index(bean.getWaypointIndex());
        n.setTakeoff_mode(bean.getTakeoffMode());
        n.setTimestamp(bean.getTimestamp());
        return n;
    }

    /** 档位 / Home 距离 / 搜星状态 / 控制源 / 限制信息（阶段 2.2 续） */

    public static AircraftGear toAircraftGear(com.dji.flighthubsdk.generated.AircraftGear nativeGear) {
        if (nativeGear == null) {
            return new AircraftGear(0);
        }
        return new AircraftGear(nativeGear.getGear());
    }

    public static com.dji.flighthubsdk.generated.AircraftGear toNativeAircraftGear(AircraftGear bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftGear n = new com.dji.flighthubsdk.generated.AircraftGear();
        n.setGear(bean.getGear());
        return n;
    }

    public static AircraftHomeDistance toAircraftHomeDistance(
            com.dji.flighthubsdk.generated.AircraftHomeDistance nativeDistance) {
        if (nativeDistance == null) {
            return new AircraftHomeDistance(0.0);
        }
        return new AircraftHomeDistance(nativeDistance.getHome_distance());
    }

    public static com.dji.flighthubsdk.generated.AircraftHomeDistance toNativeAircraftHomeDistance(
            AircraftHomeDistance bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftHomeDistance n =
                new com.dji.flighthubsdk.generated.AircraftHomeDistance();
        n.setHome_distance(bean.getHomeDistance());
        return n;
    }

    public static AircraftPositionState toAircraftPositionState(
            com.dji.flighthubsdk.generated.AircraftPositionState nativeState) {
        if (nativeState == null) {
            return new AircraftPositionState(0, 0, 0, 0);
        }
        return new AircraftPositionState(
                nativeState.getIs_fixed(),
                nativeState.getQuality(),
                nativeState.getGps_number(),
                nativeState.getRtk_number());
    }

    public static com.dji.flighthubsdk.generated.AircraftPositionState toNativeAircraftPositionState(
            AircraftPositionState bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftPositionState n =
                new com.dji.flighthubsdk.generated.AircraftPositionState();
        n.setIs_fixed(bean.getIsFixed());
        n.setQuality(bean.getQuality());
        n.setGps_number(bean.getGpsNumber());
        n.setRtk_number(bean.getRtkNumber());
        return n;
    }

    public static AircraftControlSource toAircraftControlSource(
            com.dji.flighthubsdk.generated.AircraftControlSource nativeSource) {
        if (nativeSource == null) {
            return new AircraftControlSource("");
        }
        return new AircraftControlSource(safeString(nativeSource.getControl_source()));
    }

    public static com.dji.flighthubsdk.generated.AircraftControlSource toNativeAircraftControlSource(
            AircraftControlSource bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftControlSource n =
                new com.dji.flighthubsdk.generated.AircraftControlSource();
        n.setControl_source(bean.getControlSource());
        return n;
    }

    public static AircraftLimitsInfo toAircraftLimitsInfo(com.dji.flighthubsdk.generated.AircraftLimitsInfo nativeInfo) {
        if (nativeInfo == null) {
            return new AircraftLimitsInfo(0, 0, 0, 0, 0, 0);
        }
        return new AircraftLimitsInfo(
                nativeInfo.getHeight_limit(),
                nativeInfo.getIs_near_height_limit(),
                nativeInfo.getIs_near_area_limit(),
                nativeInfo.getDistance_limit_state(),
                nativeInfo.getDistance_limit(),
                nativeInfo.getIs_near_distance_limit());
    }

    public static com.dji.flighthubsdk.generated.AircraftLimitsInfo toNativeAircraftLimitsInfo(AircraftLimitsInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftLimitsInfo n =
                new com.dji.flighthubsdk.generated.AircraftLimitsInfo();
        n.setHeight_limit(bean.getHeightLimit());
        n.setIs_near_height_limit(bean.getIsNearHeightLimit());
        n.setIs_near_area_limit(bean.getIsNearAreaLimit());
        n.setDistance_limit_state(bean.getDistanceLimitState());
        n.setDistance_limit(bean.getDistanceLimit());
        n.setIs_near_distance_limit(bean.getIsNearDistanceLimit());
        return n;
    }

    // --- 阶段 2.2 续：阈值 / 固件 / RTH / 统计 / 保养列表 / 避障 / 电池 / 控制码 / 速度 ---

    public static AircraftBatteryWarningThresholds toAircraftBatteryWarningThresholds(
            com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholds n) {
        if (n == null) {
            return new AircraftBatteryWarningThresholds(0, 0);
        }
        return new AircraftBatteryWarningThresholds(
                n.getLow_battery_warning_threshold(),
                n.getSerious_low_battery_warning_threshold());
    }

    public static com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholds toNativeAircraftBatteryWarningThresholds(
            AircraftBatteryWarningThresholds bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholds n =
                new com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholds();
        n.setLow_battery_warning_threshold(bean.getLowBatteryWarningThreshold());
        n.setSerious_low_battery_warning_threshold(bean.getSeriousLowBatteryWarningThreshold());
        return n;
    }

    public static AircraftFirmwareStatus toAircraftFirmwareStatus(com.dji.flighthubsdk.generated.AircraftFirmwareStatus n) {
        if (n == null) {
            return new AircraftFirmwareStatus("", 0, 0);
        }
        return new AircraftFirmwareStatus(
                safeString(n.getFirmware_version()),
                n.getCompatible_status(),
                n.getFirmware_upgrade_status());
    }

    public static com.dji.flighthubsdk.generated.AircraftFirmwareStatus toNativeAircraftFirmwareStatus(
            AircraftFirmwareStatus bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftFirmwareStatus n =
                new com.dji.flighthubsdk.generated.AircraftFirmwareStatus();
        n.setFirmware_version(bean.getFirmwareVersion());
        n.setCompatible_status(bean.getCompatibleStatus());
        n.setFirmware_upgrade_status(bean.getFirmwareUpgradeStatus());
        return n;
    }

    public static AircraftCurrentRthMode toAircraftCurrentRthMode(com.dji.flighthubsdk.generated.AircraftCurrentRthMode n) {
        if (n == null) {
            return new AircraftCurrentRthMode(0);
        }
        return new AircraftCurrentRthMode(n.getCurrent_rth_mode());
    }

    public static com.dji.flighthubsdk.generated.AircraftCurrentRthMode toNativeAircraftCurrentRthMode(
            AircraftCurrentRthMode bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftCurrentRthMode n =
                new com.dji.flighthubsdk.generated.AircraftCurrentRthMode();
        n.setCurrent_rth_mode(bean.getCurrentRthMode());
        return n;
    }

    public static AircraftTotalFlightInfo toAircraftTotalFlightInfo(
            com.dji.flighthubsdk.generated.AircraftTotalFlightInfo n) {
        if (n == null) {
            return new AircraftTotalFlightInfo(0.0, 0.0, 0);
        }
        return new AircraftTotalFlightInfo(
                n.getTotal_flight_time(), n.getTotal_flight_distance(), n.getTotal_flight_sorties());
    }

    public static com.dji.flighthubsdk.generated.AircraftTotalFlightInfo toNativeAircraftTotalFlightInfo(
            AircraftTotalFlightInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftTotalFlightInfo n =
                new com.dji.flighthubsdk.generated.AircraftTotalFlightInfo();
        n.setTotal_flight_time(bean.getTotalFlightTime());
        n.setTotal_flight_distance(bean.getTotalFlightDistance());
        n.setTotal_flight_sorties(bean.getTotalFlightSorties());
        return n;
    }

    public static AircraftActivationTime toAircraftActivationTime(com.dji.flighthubsdk.generated.AircraftActivationTime n) {
        if (n == null) {
            return new AircraftActivationTime(0L);
        }
        return new AircraftActivationTime(n.getActivation_time());
    }

    public static com.dji.flighthubsdk.generated.AircraftActivationTime toNativeAircraftActivationTime(
            AircraftActivationTime bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftActivationTime n =
                new com.dji.flighthubsdk.generated.AircraftActivationTime();
        n.setActivation_time(bean.getActivationTime());
        return n;
    }

    public static AircraftNightLightsState toAircraftNightLightsState(
            com.dji.flighthubsdk.generated.AircraftNightLightsState n) {
        if (n == null) {
            return new AircraftNightLightsState(0);
        }
        return new AircraftNightLightsState(n.getNight_lights_state());
    }

    public static com.dji.flighthubsdk.generated.AircraftNightLightsState toNativeAircraftNightLightsState(
            AircraftNightLightsState bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftNightLightsState n =
                new com.dji.flighthubsdk.generated.AircraftNightLightsState();
        n.setNight_lights_state(bean.getNightLightsState());
        return n;
    }

    public static AircraftObstacleAvoidanceState toAircraftObstacleAvoidanceState(
            com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceState n) {
        if (n == null) {
            return new AircraftObstacleAvoidanceState(0, 0, 0);
        }
        return new AircraftObstacleAvoidanceState(n.getHorizon(), n.getUpside(), n.getDownside());
    }

    public static com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceState toNativeAircraftObstacleAvoidanceState(
            AircraftObstacleAvoidanceState bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceState n =
                new com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceState();
        n.setHorizon(bean.getHorizon());
        n.setUpside(bean.getUpside());
        n.setDownside(bean.getDownside());
        return n;
    }

    public static AircraftStorageInfo toAircraftStorageInfo(com.dji.flighthubsdk.generated.AircraftStorageInfo n) {
        if (n == null) {
            return new AircraftStorageInfo(0, 0);
        }
        return new AircraftStorageInfo(n.getTotal(), n.getUsed());
    }

    public static com.dji.flighthubsdk.generated.AircraftStorageInfo toNativeAircraftStorageInfo(AircraftStorageInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftStorageInfo n = new com.dji.flighthubsdk.generated.AircraftStorageInfo();
        n.setTotal(bean.getTotal());
        n.setUsed(bean.getUsed());
        return n;
    }

    public static AircraftTrackId toAircraftTrackId(com.dji.flighthubsdk.generated.AircraftTrackId n) {
        if (n == null) {
            return new AircraftTrackId("");
        }
        return new AircraftTrackId(safeString(n.getTrack_id()));
    }

    public static com.dji.flighthubsdk.generated.AircraftTrackId toNativeAircraftTrackId(AircraftTrackId bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftTrackId n = new com.dji.flighthubsdk.generated.AircraftTrackId();
        n.setTrack_id(bean.getTrackId());
        return n;
    }

    public static AircraftComplianceInfo toAircraftComplianceInfo(com.dji.flighthubsdk.generated.AircraftComplianceInfo n) {
        if (n == null) {
            return new AircraftComplianceInfo(0, "");
        }
        return new AircraftComplianceInfo(n.getRid_state(), safeString(n.getCountry()));
    }

    public static com.dji.flighthubsdk.generated.AircraftComplianceInfo toNativeAircraftComplianceInfo(
            AircraftComplianceInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftComplianceInfo n =
                new com.dji.flighthubsdk.generated.AircraftComplianceInfo();
        n.setRid_state(bean.getRidState());
        n.setCountry(bean.getCountry());
        return n;
    }

    public static AircraftRcLostAction toAircraftRcLostAction(com.dji.flighthubsdk.generated.AircraftRcLostAction n) {
        if (n == null) {
            return new AircraftRcLostAction(0);
        }
        return new AircraftRcLostAction(n.getRc_lost_action());
    }

    public static com.dji.flighthubsdk.generated.AircraftRcLostAction toNativeAircraftRcLostAction(
            AircraftRcLostAction bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftRcLostAction n =
                new com.dji.flighthubsdk.generated.AircraftRcLostAction();
        n.setRc_lost_action(bean.getRcLostAction());
        return n;
    }

    public static AircraftRthInfo toAircraftRthInfo(com.dji.flighthubsdk.generated.AircraftRthInfo n) {
        if (n == null) {
            return new AircraftRthInfo(0, 0);
        }
        return new AircraftRthInfo(n.getRth_mode(), n.getRth_altitude());
    }

    public static com.dji.flighthubsdk.generated.AircraftRthInfo toNativeAircraftRthInfo(AircraftRthInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftRthInfo n = new com.dji.flighthubsdk.generated.AircraftRthInfo();
        n.setRth_mode(bean.getRthMode());
        n.setRth_altitude(bean.getRthAltitude());
        return n;
    }

    public static AircraftHomeGeographicalInfo toAircraftHomeGeographicalInfo(
            com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfo n) {
        if (n == null) {
            return new AircraftHomeGeographicalInfo(0.0, 0.0);
        }
        return new AircraftHomeGeographicalInfo(n.getHome_longitude(), n.getHome_latitude());
    }

    public static com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfo toNativeAircraftHomeGeographicalInfo(
            AircraftHomeGeographicalInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfo n =
                new com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfo();
        n.setHome_longitude(bean.getHomeLongitude());
        n.setHome_latitude(bean.getHomeLatitude());
        return n;
    }

    public static BatteryInfo toBatteryInfo(com.dji.flighthubsdk.generated.BatteryInfo n) {
        if (n == null) {
            return null;
        }
        return new BatteryInfo(
                n.getCapacity_percent(),
                safeString(n.getFirmware_version()),
                n.getHigh_voltage_storage_days(),
                n.getIndex(),
                n.getLoop_times(),
                safeString(n.getSn()),
                n.getSub_type(),
                n.getTemperature(),
                n.getType(),
                n.getVoltage());
    }

    public static com.dji.flighthubsdk.generated.BatteryInfo toNativeBatteryInfo(BatteryInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.BatteryInfo n = new com.dji.flighthubsdk.generated.BatteryInfo();
        n.setCapacity_percent(bean.getCapacityPercent());
        n.setFirmware_version(bean.getFirmwareVersion());
        n.setHigh_voltage_storage_days(bean.getHighVoltageStorageDays());
        n.setIndex(bean.getIndex());
        n.setLoop_times(bean.getLoopTimes());
        n.setSn(bean.getSn());
        n.setSub_type(bean.getSubType());
        n.setTemperature(bean.getTemperature());
        n.setType(bean.getType());
        n.setVoltage(bean.getVoltage());
        return n;
    }

    public static List<BatteryInfo> toBatteryInfoList(com.dji.flighthubsdk.generated.BatteryInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        int sz = v.size();
        List<BatteryInfo> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.BatteryInfo e = v.get(i);
            if (e != null) {
                out.add(toBatteryInfo(e));
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.generated.BatteryInfoVector toNativeBatteryInfoVector(List<BatteryInfo> beans) {
        com.dji.flighthubsdk.generated.BatteryInfoVector v = new com.dji.flighthubsdk.generated.BatteryInfoVector();
        if (beans == null) {
            return v;
        }
        for (BatteryInfo b : beans) {
            if (b != null) {
                v.add(toNativeBatteryInfo(b));
            }
        }
        return v;
    }

    public static AircraftBatteryInfo toAircraftBatteryInfo(com.dji.flighthubsdk.generated.AircraftBatteryInfo n) {
        if (n == null) {
            return new AircraftBatteryInfo(0, 0, 0, 0, Collections.emptyList());
        }
        return new AircraftBatteryInfo(
                n.getCapacity_percent(),
                n.getLanding_power(),
                n.getRemain_flight_time(),
                n.getReturn_home_power(),
                toBatteryInfoList(n.getBatteries()));
    }

    public static com.dji.flighthubsdk.generated.AircraftBatteryInfo toNativeAircraftBatteryInfo(AircraftBatteryInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftBatteryInfo n =
                new com.dji.flighthubsdk.generated.AircraftBatteryInfo();
        n.setCapacity_percent(bean.getCapacityPercent());
        n.setLanding_power(bean.getLandingPower());
        n.setRemain_flight_time(bean.getRemainFlightTime());
        n.setReturn_home_power(bean.getReturnHomePower());
        n.setBatteries(toNativeBatteryInfoVector(bean.getBatteries()));
        return n;
    }

    public static AircraftMaintainStatus toAircraftMaintainStatus(com.dji.flighthubsdk.generated.AircraftMaintainStatus n) {
        if (n == null) {
            return null;
        }
        return new AircraftMaintainStatus(
                n.getState(),
                n.getLast_maintain_type(),
                n.getLast_maintain_time(),
                n.getLast_maintain_flight_time(),
                n.getLast_maintain_flight_sorties());
    }

    public static com.dji.flighthubsdk.generated.AircraftMaintainStatus toNativeAircraftMaintainStatus(
            AircraftMaintainStatus bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftMaintainStatus n =
                new com.dji.flighthubsdk.generated.AircraftMaintainStatus();
        n.setState(bean.getState());
        n.setLast_maintain_type(bean.getLastMaintainType());
        n.setLast_maintain_time(bean.getLastMaintainTime());
        n.setLast_maintain_flight_time(bean.getLastMaintainFlightTime());
        n.setLast_maintain_flight_sorties(bean.getLastMaintainFlightSorties());
        return n;
    }

    public static List<AircraftMaintainStatus> toAircraftMaintainStatusList(
            com.dji.flighthubsdk.generated.AircraftMaintainStatusVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        int sz = v.size();
        List<AircraftMaintainStatus> out = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            com.dji.flighthubsdk.generated.AircraftMaintainStatus e = v.get(i);
            if (e != null) {
                out.add(toAircraftMaintainStatus(e));
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.generated.AircraftMaintainStatusVector toNativeAircraftMaintainStatusVector(
            List<AircraftMaintainStatus> beans) {
        com.dji.flighthubsdk.generated.AircraftMaintainStatusVector v =
                new com.dji.flighthubsdk.generated.AircraftMaintainStatusVector();
        if (beans == null) {
            return v;
        }
        for (AircraftMaintainStatus b : beans) {
            if (b != null) {
                v.add(toNativeAircraftMaintainStatus(b));
            }
        }
        return v;
    }

    public static AircraftMaintainStatusInfo toAircraftMaintainStatusInfo(
            com.dji.flighthubsdk.generated.AircraftMaintainStatusInfo n) {
        if (n == null) {
            return new AircraftMaintainStatusInfo(Collections.emptyList());
        }
        return new AircraftMaintainStatusInfo(toAircraftMaintainStatusList(n.getMaintain_status_info()));
    }

    public static com.dji.flighthubsdk.generated.AircraftMaintainStatusInfo toNativeAircraftMaintainStatusInfo(
            AircraftMaintainStatusInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftMaintainStatusInfo n =
                new com.dji.flighthubsdk.generated.AircraftMaintainStatusInfo();
        n.setMaintain_status_info(toNativeAircraftMaintainStatusVector(bean.getMaintainStatuses()));
        return n;
    }

    public static AircraftControlCode toAircraftControlCode(com.dji.flighthubsdk.generated.AircraftControlCode n) {
        if (n == null) {
            return new AircraftControlCode(0);
        }
        return new AircraftControlCode(n.getControl_mode());
    }

    public static com.dji.flighthubsdk.generated.AircraftControlCode toNativeAircraftControlCode(AircraftControlCode bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftControlCode n = new com.dji.flighthubsdk.generated.AircraftControlCode();
        n.setControl_mode(bean.getControlMode());
        return n;
    }

    public static AircraftSpeed toAircraftSpeed(com.dji.flighthubsdk.generated.AircraftSpeed n) {
        if (n == null) {
            return new AircraftSpeed(0.0, 0.0);
        }
        return new AircraftSpeed(n.getHorizontal_speed(), n.getVertical_speed());
    }

    public static com.dji.flighthubsdk.generated.AircraftSpeed toNativeAircraftSpeed(AircraftSpeed bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AircraftSpeed n = new com.dji.flighthubsdk.generated.AircraftSpeed();
        n.setHorizontal_speed(bean.getHorizontalSpeed());
        n.setVertical_speed(bean.getVerticalSpeed());
        return n;
    }

    private static String safeString(String s) {
        return s != null ? s : "";
    }

    // --- 阶段 1：直播帧与 SEI（LiveModule / PullStreamModule）---

    public static FrameInfo toFrameInfo(com.dji.flighthubsdk.generated.FrameInfo nativeInfo) {
        if (nativeInfo == null) {
            return new FrameInfo(false);
        }
        return new FrameInfo(nativeInfo.getIs_key_frame());
    }

    public static com.dji.flighthubsdk.generated.FrameInfo toNativeFrameInfo(FrameInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.FrameInfo n = new com.dji.flighthubsdk.generated.FrameInfo();
        n.setIs_key_frame(bean.isKeyFrame());
        return n;
    }

    public static CamCropPos toCamCropPos(com.dji.flighthubsdk.generated.CamCropPos nativePos) {
        if (nativePos == null) {
            return new CamCropPos(0, 0);
        }
        return new CamCropPos(nativePos.getX(), nativePos.getY());
    }

    public static com.dji.flighthubsdk.generated.CamCropPos toNativeCamCropPos(CamCropPos bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.CamCropPos n = new com.dji.flighthubsdk.generated.CamCropPos();
        n.setX(bean.getX());
        n.setY(bean.getY());
        return n;
    }

    public static ArSeiInfo toArSeiInfo(com.dji.flighthubsdk.generated.ArSeiInfo nativeInfo) {
        if (nativeInfo == null) {
            return new ArSeiInfo(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        return new ArSeiInfo(
                nativeInfo.getCamera_yaw(),
                nativeInfo.getCamera_pitch(),
                nativeInfo.getCamera_roll(),
                nativeInfo.getCamera_pos_x(),
                nativeInfo.getCamera_pos_y(),
                nativeInfo.getCamera_pos_z(),
                nativeInfo.getLatitude(),
                nativeInfo.getLongitude(),
                nativeInfo.getGnd_origin_z(),
                nativeInfo.getAltitude());
    }

    public static com.dji.flighthubsdk.generated.ArSeiInfo toNativeArSeiInfo(ArSeiInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.ArSeiInfo n = new com.dji.flighthubsdk.generated.ArSeiInfo();
        n.setCamera_yaw(bean.getCameraYaw());
        n.setCamera_pitch(bean.getCameraPitch());
        n.setCamera_roll(bean.getCameraRoll());
        n.setCamera_pos_x(bean.getCameraPosX());
        n.setCamera_pos_y(bean.getCameraPosY());
        n.setCamera_pos_z(bean.getCameraPosZ());
        n.setLatitude(bean.getLatitude());
        n.setLongitude(bean.getLongitude());
        n.setGnd_origin_z(bean.getGndOriginZ());
        n.setAltitude(bean.getAltitude());
        return n;
    }

    public static FovSeiInfo toFovSeiInfo(com.dji.flighthubsdk.generated.FovSeiInfo nativeInfo) {
        if (nativeInfo == null) {
            return new FovSeiInfo(0L, 0L, 0L);
        }
        return new FovSeiInfo(
                nativeInfo.getFov_horizontal(),
                nativeInfo.getFov_vertical(),
                nativeInfo.getCur_actual_focal_length());
    }

    public static com.dji.flighthubsdk.generated.FovSeiInfo toNativeFovSeiInfo(FovSeiInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.FovSeiInfo n = new com.dji.flighthubsdk.generated.FovSeiInfo();
        n.setFov_horizontal(bean.getFovHorizontal());
        n.setFov_vertical(bean.getFovVertical());
        n.setCur_actual_focal_length(bean.getCurActualFocalLength());
        return n;
    }

    public static AssistantSeiInfo toAssistantSeiInfo(com.dji.flighthubsdk.generated.AssistantSeiInfo nativeInfo) {
        if (nativeInfo == null) {
            return new AssistantSeiInfo(false, 0);
        }
        return new AssistantSeiInfo(nativeInfo.getIs_auto_direction(), nativeInfo.getDirection());
    }

    public static com.dji.flighthubsdk.generated.AssistantSeiInfo toNativeAssistantSeiInfo(AssistantSeiInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AssistantSeiInfo n = new com.dji.flighthubsdk.generated.AssistantSeiInfo();
        n.setIs_auto_direction(bean.isAutoDirection());
        n.setDirection(bean.getDirection());
        return n;
    }

    public static EisSeiInfo toEisSeiInfo(com.dji.flighthubsdk.generated.EisSeiInfo nativeInfo) {
        if (nativeInfo == null) {
            return new EisSeiInfo(0, new CamCropPos(0, 0), new CamCropPos(0, 0), new CamCropPos(0, 0), new CamCropPos(0, 0));
        }
        return new EisSeiInfo(
                nativeInfo.getVersion(),
                toCamCropPos(nativeInfo.getLeft_top()),
                toCamCropPos(nativeInfo.getRight_top()),
                toCamCropPos(nativeInfo.getRight_bottom()),
                toCamCropPos(nativeInfo.getLeft_bottom()));
    }

    public static com.dji.flighthubsdk.generated.EisSeiInfo toNativeEisSeiInfo(EisSeiInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.EisSeiInfo n = new com.dji.flighthubsdk.generated.EisSeiInfo();
        n.setVersion(bean.getVersion());
        n.setLeft_top(toNativeCamCropPos(bean.getLeftTop()));
        n.setRight_top(toNativeCamCropPos(bean.getRightTop()));
        n.setRight_bottom(toNativeCamCropPos(bean.getRightBottom()));
        n.setLeft_bottom(toNativeCamCropPos(bean.getLeftBottom()));
        return n;
    }

    // --- 阶段 4：HMS、负载 OSD 列表、相机列表 ---

    private static List<Integer> copyIntVector(com.dji.flighthubsdk.generated.IntVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(v);
    }

    private static List<String> copyStringVector(com.dji.flighthubsdk.generated.StringVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(v);
    }

    private static Map<String, String> copyStringStringUnorderedMap(
            com.dji.flighthubsdk.generated.StringStringUnorderedMap m) {
        if (m == null || m.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(m));
    }

    private static com.dji.flighthubsdk.generated.StringStringUnorderedMap toNativeStringStringUnorderedMap(
            Map<String, String> m) {
        com.dji.flighthubsdk.generated.StringStringUnorderedMap nativeMap = new com.dji.flighthubsdk.generated.StringStringUnorderedMap();
        if (m != null) {
            nativeMap.putAll(m);
        }
        return nativeMap;
    }

    public static HmsArgs toHmsArgs(com.dji.flighthubsdk.generated.HmsArgs n) {
        if (n == null) {
            return new HmsArgs(0, 0, "", "", 0, 0, 0L, 0);
        }
        return new HmsArgs(
                n.getComponent_index(),
                n.getSensor_index(),
                safeString(n.getDevice_id()),
                safeString(n.getSender_host_id()),
                n.getIgnore(),
                n.getAlarm_info(),
                n.getCreate_timestamp(),
                n.getDisplay_scene());
    }

    public static HmsDataBasics toHmsBasics(com.dji.flighthubsdk.generated.HmsDataBasics n) {
        if (n == null) {
            return new HmsDataBasics(
                    0,
                    0,
                    "",
                    "",
                    "",
                    0,
                    0,
                    toHmsArgs(null),
                    "",
                    "",
                    0,
                    0);
        }
        return new HmsDataBasics(
                n.getLevel(),
                n.getModule_id(),
                safeString(n.getCode()),
                safeString(n.getDevice_type()),
                safeString(n.getDomain_type()),
                n.getIn_the_sky(),
                n.getImminent(),
                toHmsArgs(n.getArgs()),
                safeString(n.getStatus_key()),
                safeString(n.getJoint_key()),
                n.getId(),
                n.getParent_id());
    }

    public static List<HmsDataBasics> toHmsDataBasicsList(com.dji.flighthubsdk.generated.HmsDataBasicsVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<HmsDataBasics> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toHmsBasics(v.get(i)));
        }
        return out;
    }

    public static HmsDataItem toHmsDataItem(com.dji.flighthubsdk.generated.HmsDataItem n) {
        if (n == null) {
            return new HmsDataItem(null, Collections.emptyList());
        }
        return new HmsDataItem(toHmsBasics(n.getBasics()), toHmsDataBasicsList(n.getSub_hms_data()));
    }

    public static List<HmsDataItem> toHmsDataItemList(com.dji.flighthubsdk.generated.HmsDataItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<HmsDataItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toHmsDataItem(v.get(i)));
        }
        return out;
    }

    /** 从 native {@code HmsData} 深拷贝为对外 {@link HmsData}。 */
    public static HmsData toHmsData(com.dji.flighthubsdk.generated.HmsData n) {
        if (n == null) {
            return new HmsData(Collections.emptyList());
        }
        return new HmsData(toHmsDataItemList(n.getHms_data_items()));
    }

    public static SmartTrackPointInfo toSmartTrackPointInfo(com.dji.flighthubsdk.generated.SmartTrackPointInfo n) {
        if (n == null) {
            return new SmartTrackPointInfo(0, 0, 0, 0);
        }
        return new SmartTrackPointInfo(
                n.getTrack_altitude(),
                n.getTrack_latitude(),
                n.getTrack_longitude(),
                n.getTrack_target_mode());
    }

    public static List<SmartTrackPointInfo> toSmartTrackPointInfoList(
            com.dji.flighthubsdk.generated.SmartTrackPointInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<SmartTrackPointInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toSmartTrackPointInfo(v.get(i)));
        }
        return out;
    }

    public static AircraftPayloadsOsdInfo toAircraftPayloadsOsdInfo(
            com.dji.flighthubsdk.generated.AircraftPayloadsOsdInfo n) {
        if (n == null) {
            return new AircraftPayloadsOsdInfo(
                    "",
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    Collections.emptyList(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    Collections.emptyList(),
                    "",
                    "",
                    "");
        }
        return new AircraftPayloadsOsdInfo(
                safeString(n.getPayload_index()),
                n.getGimbal_pitch(),
                n.getGimbal_roll(),
                n.getGimbal_yaw(),
                n.getMeasure_target_altitude(),
                n.getMeasure_target_longitude(),
                n.getMeasure_target_latitude(),
                n.getMeasure_target_distance(),
                n.getMeasure_target_error_state(),
                toSmartTrackPointInfoList(n.getSmart_track_points()),
                n.getZoom_factor(),
                n.getThermal_current_palette_style(),
                n.getThermal_gain_mode(),
                n.getThermal_global_temperature_min(),
                n.getThermal_global_temperature_max(),
                n.getThermal_isotherm_state(),
                n.getThermal_isotherm_lower_limit(),
                n.getThermal_isotherm_upper_limit(),
                copyIntVector(n.getThermal_supported_palette_styles()),
                safeString(n.getControl_source()),
                safeString(n.getFirmware_version()),
                safeString(n.getSn()));
    }

    public static List<AircraftPayloadsOsdInfo> toAircraftPayloadsOsdInfoListFromVector(
            com.dji.flighthubsdk.generated.AircraftPayloadsOsdInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<AircraftPayloadsOsdInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toAircraftPayloadsOsdInfo(v.get(i)));
        }
        return out;
    }

    public static AircraftPayloadsOsdInfoList toAircraftPayloadsOsdInfoList(
            com.dji.flighthubsdk.generated.AircraftPayloadsOsdInfoList n) {
        if (n == null) {
            return new AircraftPayloadsOsdInfoList(Collections.emptyList());
        }
        return new AircraftPayloadsOsdInfoList(toAircraftPayloadsOsdInfoListFromVector(n.getPayloads_info_list()));
    }

    public static CameraIrMeteringPoint toCameraIrMeteringPoint(
            com.dji.flighthubsdk.generated.CameraIrMeteringPoint n) {
        if (n == null) {
            return null;
        }
        return new CameraIrMeteringPoint(n.getTemperature(), n.getX(), n.getY());
    }

    public static CameraLiveviewWorldRegion toCameraLiveviewWorldRegion(
            com.dji.flighthubsdk.generated.CameraLiveviewWorldRegion n) {
        if (n == null) {
            return null;
        }
        return new CameraLiveviewWorldRegion(
                n.getBottom(), n.getLeft(), n.getRight(), n.getTop());
    }

    public static AircraftPayloadCameraInfo toAircraftPayloadCameraInfo(
            com.dji.flighthubsdk.generated.AircraftPayloadCameraInfo n) {
        if (n == null) {
            return null;
        }
        return new AircraftPayloadCameraInfo(
                safeString(n.getPayload_index()),
                n.getCamera_mode(),
                n.getIr_metering_mode(),
                toCameraIrMeteringPoint(n.getIr_metering_point()),
                n.getIr_zoom_factor(),
                toCameraLiveviewWorldRegion(n.getLiveview_world_region()),
                n.getPhoto_state(),
                copyStringVector(n.getPhoto_storage_settings()),
                n.getRecord_time(),
                n.getRecording_state(),
                n.getRemain_photo_num(),
                n.getRemain_record_duration(),
                n.getScreen_split_enable(),
                n.getWide_calibrate_farthest_focus_value(),
                n.getWide_calibrate_nearest_focus_value(),
                n.getWide_exposure_mode(),
                n.getWide_exposure_value(),
                n.getWide_focus_mode(),
                n.getWide_focus_state(),
                n.getWide_focus_value(),
                n.getWide_iso(),
                n.getWide_max_focus_value(),
                n.getWide_min_focus_value(),
                n.getWide_shutter_speed(),
                n.getZoom_calibrate_farthest_focus_value(),
                n.getZoom_calibrate_nearest_focus_value(),
                n.getZoom_exposure_mode(),
                n.getZoom_exposure_value(),
                n.getZoom_factor(),
                n.getZoom_focus_mode(),
                n.getZoom_focus_state(),
                n.getZoom_focus_value(),
                n.getZoom_iso(),
                n.getZoom_max_focus_value(),
                n.getZoom_min_focus_value(),
                n.getZoom_shutter_speed());
    }

    public static List<AircraftPayloadCameraInfo> toAircraftPayloadCameraInfoList(
            com.dji.flighthubsdk.generated.AircraftPayloadCameraInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<AircraftPayloadCameraInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            AircraftPayloadCameraInfo one = toAircraftPayloadCameraInfo(v.get(i));
            if (one != null) {
                out.add(one);
            }
        }
        return out;
    }

    public static AircraftPayloadsCamerasList toAircraftPayloadsCamerasList(
            com.dji.flighthubsdk.generated.AircraftPayloadsCamerasList n) {
        if (n == null) {
            return new AircraftPayloadsCamerasList(Collections.emptyList());
        }
        return new AircraftPayloadsCamerasList(toAircraftPayloadCameraInfoList(n.getCameras_list()));
    }

    // --- 阶段 4：载荷列表、直播世界区域、云台姿态 ---

    /** 从 native {@link com.dji.flighthubsdk.generated.LiveviewWorldRegion} 拷贝为对外区域 Bean。 */
    public static CameraLiveviewWorldRegion toCameraLiveviewWorldRegion(
            com.dji.flighthubsdk.generated.LiveviewWorldRegion n) {
        if (n == null) {
            return null;
        }
        return new CameraLiveviewWorldRegion(
                n.getBottom(), n.getLeft(), n.getRight(), n.getTop());
    }

    /**
     * 单路 {@code listenLiveviewWorldRegion}：载荷索引 + 世界区域矩形。
     * @param n 参数
     */
    public static LiveviewWorldRegion toLiveviewWorldRegion(com.dji.flighthubsdk.generated.LiveviewWorldRegion n) {
        if (n == null) {
            return new LiveviewWorldRegion("", new CameraLiveviewWorldRegion(0, 0, 0, 0));
        }
        CameraLiveviewWorldRegion rect = toCameraLiveviewWorldRegion(n);
        return new LiveviewWorldRegion(safeString(n.getPayload_index()), rect);
    }

    public static CameraMeasureTarget toCameraMeasureTarget(com.dji.flighthubsdk.generated.CameraMeasureTarget n) {
        if (n == null) {
            return new CameraMeasureTarget("", 0, 0, 0, 0, 0);
        }
        return new CameraMeasureTarget(
                safeString(n.getPayload_index()),
                n.getAltitude(),
                n.getDistance(),
                n.getLatitude(),
                n.getLongitude(),
                n.getLaser_measure_state());
    }

    public static AircraftPayloadsList toAircraftPayloadsList(
            com.dji.flighthubsdk.generated.AircraftPayloadsList n) {
        if (n == null) {
            return new AircraftPayloadsList(Collections.emptyList());
        }
        return new AircraftPayloadsList(copyStringVector(n.getPayloads_list()));
    }

    public static AircraftPayloadsCameraLiveviewWorldRegion toAircraftPayloadsCameraLiveviewWorldRegion(
            com.dji.flighthubsdk.generated.AircraftPayloadsCameraLiveviewWorldRegion n) {
        if (n == null) {
            return new AircraftPayloadsCameraLiveviewWorldRegion(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.CameraLiveviewWorldRegionMap map = n.getPayloads_list();
        if (map == null || map.isEmpty()) {
            return new AircraftPayloadsCameraLiveviewWorldRegion(Collections.emptyList());
        }
        List<AircraftPayloadLiveviewWorldRegionEntry> out = new ArrayList<>(map.size());
        for (Map.Entry<String, com.dji.flighthubsdk.generated.LiveviewWorldRegion> e : map.entrySet()) {
            String key = safeString(e.getKey());
            CameraLiveviewWorldRegion region = toCameraLiveviewWorldRegion(e.getValue());
            if (region != null) {
                out.add(new AircraftPayloadLiveviewWorldRegionEntry(key, region));
            }
        }
        return new AircraftPayloadsCameraLiveviewWorldRegion(out);
    }

    public static AircraftPayloadGimbalAttitude toAircraftPayloadGimbalAttitude(
            com.dji.flighthubsdk.generated.AircraftPayloadGimbalAttitude n) {
        if (n == null) {
            return new AircraftPayloadGimbalAttitude("", 0, 0, 0);
        }
        return new AircraftPayloadGimbalAttitude(
                safeString(n.getPayload_index()),
                n.getGimbal_pitch(),
                n.getGimbal_roll(),
                n.getGimbal_yaw());
    }

    public static AircraftPayloadsGimbalAttitude toAircraftPayloadsGimbalAttitude(
            com.dji.flighthubsdk.generated.AircraftPayloadsGimbalAttitude n) {
        if (n == null) {
            return new AircraftPayloadsGimbalAttitude(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.AircraftPayloadGimbalAttitudeVector v = n.getPayloads_gimbal_attitude();
        if (v == null || v.isEmpty()) {
            return new AircraftPayloadsGimbalAttitude(Collections.emptyList());
        }
        List<AircraftPayloadGimbalAttitude> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toAircraftPayloadGimbalAttitude(v.get(i)));
        }
        return new AircraftPayloadsGimbalAttitude(out);
    }

    // --- 阶段 4.1：相机 / 云台监听（单路 payload） ---

    public static GimbalState toGimbalState(com.dji.flighthubsdk.generated.GimbalState n) {
        if (n == null) {
            return new GimbalState("", 0, 0, 0);
        }
        return new GimbalState(safeString(n.getPayload_index()), n.getYaw(), n.getPitch(), n.getRoll());
    }

    public static CameraState toCameraState(com.dji.flighthubsdk.generated.CameraState n) {
        if (n == null) {
            return new CameraState("", 0, 0, false, 0, false, 0, 0);
        }
        int modeVal = n.getCamera_mode();
        CameraState s = new CameraState(
                safeString(n.getPayload_index()),
                modeVal,
                n.getRecord_time(),
                n.getRecording_state(),
                n.getRemain_record_duration(),
                n.getPhoto_state(),
                n.getRemain_photo_num(),
                n.getInterval_photo_interval());
        // B4 扩展字段
        s.setVideoResolution(n.getVideo_resolution());
        s.setLinkageZoomState(n.getLinkage_zoom_state());
        s.setPhotoSize(n.getPhoto_size());
        s.setVideoResolutionWide(n.getVideo_resolution_wide());
        s.setVideoResolutionZoom(n.getVideo_resolution_zoom());
        s.setPhotoSizeWide(n.getPhoto_size_wide());
        s.setPhotoSizeZoom(n.getPhoto_size_zoom());
        s.setPhotoFormat(n.getPhoto_format());
        s.setThermalTempExceedRangeValid(n.getThermal_temp_exceed_range_valid());
        s.setThermalSuperResolutionEnable(n.getThermal_super_resolution_enable());
        s.setIsothermEnable(n.getIsotherm_enable());
        s.setIsothermRoiBasedRenderingEnable(n.getIsotherm_roi_based_rendering_enable());
        s.setIsothermColorRenderingType(n.getIsotherm_color_rendering_type());
        s.setIsothermLowerThreshold(n.getIsotherm_lower_threshold());
        s.setIsothermUpperThreshold(n.getIsotherm_upper_threshold());
        return s;
    }

    public static CameraWideLensState toCameraWideLensState(com.dji.flighthubsdk.generated.CameraWideLensState n) {
        if (n == null) {
            return new CameraWideLensState(
                    "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, 0);
        }
        CameraWideLensState bean = new CameraWideLensState(
                safeString(n.getPayload_index()),
                n.getWide_calibrate_farthest_focus_value(),
                n.getWide_calibrate_nearest_focus_value(),
                n.getWide_exposure_mode(),
                n.getWide_exposure_value(),
                n.getWide_focus_mode(),
                n.getWide_focus_state(),
                n.getWide_focus_value(),
                n.getWide_iso(),
                n.getWide_max_focus_value(),
                n.getWide_min_focus_value(),
                n.getWide_shutter_speed(),
                n.getWide_dewarping_state(),
                n.getWide_mechanical_shutter_state(),
                n.getWide_aperture_value());
        bean.setWideFovInfo(toCameraLensFovInfo(n.getWide_fov_info()));
        return bean;
    }

    public static CameraZoomLensState toCameraZoomLensState(com.dji.flighthubsdk.generated.CameraZoomLensState n) {
        if (n == null) {
            return new CameraZoomLensState("", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        CameraZoomLensState bean = new CameraZoomLensState(
                safeString(n.getPayload_index()),
                n.getZoom_calibrate_farthest_focus_value(),
                n.getZoom_calibrate_nearest_focus_value(),
                n.getZoom_exposure_mode(),
                n.getZoom_exposure_value(),
                n.getZoom_factor(),
                n.getZoom_focus_mode(),
                n.getZoom_focus_state(),
                n.getZoom_focus_value(),
                n.getZoom_iso(),
                n.getZoom_max_focus_value(),
                n.getZoom_min_focus_value(),
                n.getZoom_shutter_speed(),
                n.getZoom_aperture_value());
        bean.setZoomFovInfo(toCameraLensFovInfo(n.getZoom_fov_info()));
        return bean;
    }

    public static CameraLensFovInfo toCameraLensFovInfo(com.dji.flighthubsdk.generated.CameraLensFovInfo n) {
        if (n == null) {
            return null;
        }
        return new CameraLensFovInfo(
                safeString(n.getPayload_index()),
                n.getWidth(),
                n.getHeight(),
                n.getImage_ratio(),
                n.getFocal_length());
    }

    public static CameraIrMeteringArea toCameraIrMeteringArea(com.dji.flighthubsdk.generated.CameraIrMeteringArea n) {
        if (n == null) {
            return null;
        }
        return new CameraIrMeteringArea(
                n.getAver_temperature(),
                n.getHeight(),
                n.getWidth(),
                n.getX(),
                n.getY(),
                toCameraIrMeteringPoint(n.getMax_temperature_point()),
                toCameraIrMeteringPoint(n.getMin_temperature_point()));
    }

    public static CameraIrThermometryInfo toCameraIrThermometryInfo(com.dji.flighthubsdk.generated.CameraIrThermometryInfo n) {
        if (n == null) {
            return new CameraIrThermometryInfo("", 0, null, null);
        }
        return new CameraIrThermometryInfo(
                safeString(n.getPayload_index()),
                n.getIr_metering_mode(),
                toCameraIrMeteringPoint(n.getIr_metering_point()),
                toCameraIrMeteringArea(n.getIr_metering_area()));
    }

    public static CameraIrLensState toCameraIrLensState(com.dji.flighthubsdk.generated.CameraIrLensState n) {
        if (n == null) {
            return new CameraIrLensState("", false, 0, 0, 0, 0.0, 0.0, 0, 0, 0, null);
        }
        return new CameraIrLensState(
                safeString(n.getPayload_index()),
                n.getScreen_split_enable(),
                n.getIr_zoom_factor(),
                n.getThermal_current_palette_style(),
                n.getThermal_gain_mode(),
                n.getThermal_global_temperature_max(),
                n.getThermal_global_temperature_min(),
                n.getThermal_isotherm_lower_limit(),
                n.getThermal_isotherm_state(),
                n.getThermal_isotherm_upper_limit(),
                toCameraLensFovInfo(n.getIr_fov_info()));
    }

    public static PanoramaStatus toPanoramaStatus(com.dji.flighthubsdk.generated.PanoramaStatus n) {
        if (n == null) {
            return new PanoramaStatus(0, 0, 0);
        }
        return new PanoramaStatus(
                n.getCurrent_step(),
                n.getPercent(),
                n.getStatus());
    }

    public static Waypoint toWaypoint(com.dji.flighthubsdk.generated.Waypoint n) {
        if (n == null) {
            return new Waypoint(0, 0, 0);
        }
        return new Waypoint(n.getLatitude(), n.getLongitude(), n.getHeight());
    }

    public static PoiStatusEvent toPoiStatusEvent(com.dji.flighthubsdk.generated.PoiStatusEvent n) {
        if (n == null) {
            return new PoiStatusEvent(
                    "", "", new Waypoint(0, 0, 0), 0, 0, 0, 0, "", 0, 0, "");
        }
        return new PoiStatusEvent(
                safeString(n.getDevice_sn()),
                safeString(n.getDrone_sn()),
                toWaypoint(n.getCenter_point()),
                n.getCircle_radius(),
                n.getCircle_speed(),
                n.getMax_circle_speed(),
                n.getReason(),
                safeString(n.getStatus()),
                n.getTimestamp(),
                n.getExecute_duration(),
                safeString(n.getPayload_index()));
    }

    public static POITaskInfo toPOITaskInfo(com.dji.flighthubsdk.generated.POITaskInfo n) {
        if (n == null) {
            return null;
        }
        return new POITaskInfo(
                safeString(n.getUuid()),
                safeString(n.getDevice_sn()),
                safeString(n.getSn()),
                POITaskStatus.fromValue(n.getStatus()),
                n.getErr_code(),
                toWaypoint(n.getPoi_center_point()),
                n.getSpeed(),
                n.getCircle_radius(),
                safeString(n.getPayload_index()));
    }

    /** native {@code int} 状态 → 对外 {@link POITaskStatus}（与 {@code value.h} 一致）。 */
    public static POITaskStatus toValuePOITaskStatus(int status) {
        return POITaskStatus.fromValue(status);
    }

    /** 对外枚举 → native {@code POITaskInfo::status} 整型。 */
    public static int toNativePOITaskStatusInt(POITaskStatus mode) {
        if (mode == null) {
            return POITaskStatus.UNKNOWN.getValue();
        }
        return mode.getValue();
    }

    /** native {@code int} → 对外 {@link DroneLinkState}（与 {@code value.h} 一致）。 */
    public static DroneLinkState toValueDroneLinkState(int v) {
        return DroneLinkState.fromValue(v);
    }

    /** 对外枚举 → native {@code LinkStatus} 链路字段整型。 */
    public static int toNativeDroneLinkStateInt(DroneLinkState mode) {
        if (mode == null) {
            return DroneLinkState.UNKNOWN.getValue();
        }
        return mode.getValue();
    }

    public static LiveRecordItem toLiveRecordItem(com.dji.flighthubsdk.generated.LiveRecordItem n) {
        if (n == null) {
            return new LiveRecordItem("", "", 0, "", "", 0L);
        }
        return new LiveRecordItem(
                safeString(n.getBid()),
                safeString(n.getSn()),
                n.getState(),
                safeString(n.getCamera()),
                safeString(n.getCreated_by()),
                n.getDuration());
    }

    public static List<LiveRecordItem> toLiveRecordItemList(com.dji.flighthubsdk.generated.LiveRecordItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<LiveRecordItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLiveRecordItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static LiveRecordStateInfo toLiveRecordStateInfo(com.dji.flighthubsdk.generated.LiveRecordStateInfo n) {
        if (n == null) {
            return new LiveRecordStateInfo("", "", "", "", 0, "", 0, 0L);
        }
        return new LiveRecordStateInfo(
                safeString(n.getBid()),
                safeString(n.getSn()),
                safeString(n.getCamera()),
                safeString(n.getVideo()),
                n.getState(),
                safeString(n.getCreated_by()),
                n.getDuration(),
                n.getCreate_time());
    }

    public static LiveVideoInfo toLiveVideoInfo(com.dji.flighthubsdk.generated.LiveVideoInfo n) {
        if (n == null) {
            return new LiveVideoInfo("", "", 0, Collections.emptyList());
        }
        List<Integer> switchable = copyIntVector(n.getSwitchable_video_types());
        return new LiveVideoInfo(
                safeString(n.getPayload_index()),
                safeString(n.getVideo_index()),
                n.getVideo_type(),
                switchable);
    }

    public static ThermalGainModeInfo toThermalGainModeInfo(com.dji.flighthubsdk.generated.ThermalGainModeInfo n) {
        if (n == null) {
            return new ThermalGainModeInfo("", 0);
        }
        return new ThermalGainModeInfo(
                safeString(n.getPayload_index()),
                n.getThermal_gain_mode());
    }

    public static LiveInfo toLiveInfo(com.dji.flighthubsdk.generated.LiveInfo n) {
        if (n == null) {
            return new LiveInfo("", "", "", Collections.emptyList());
        }
        return new LiveInfo(
                safeString(n.getDevice_sn()),
                safeString(n.getPayload_index()),
                safeString(n.getVideo_id()),
                copyIntVector(n.getSwitchable_lens_types()));
    }

    public static List<LiveInfo> toLiveInfoList(com.dji.flighthubsdk.generated.LiveInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<LiveInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLiveInfo(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * 供 {@code getLiveStream} / {@code getLiveRecord} / {@code changeDockCameraPosition} 等入参使用。
     */
    public static com.dji.flighthubsdk.generated.LiveInfo toNativeLiveInfo(LiveInfo bean) {
        if (bean == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.LiveInfo n = new com.dji.flighthubsdk.generated.LiveInfo();
        n.setDevice_sn(safeString(bean.getDeviceSn()));
        n.setPayload_index(safeString(bean.getPayloadIndex()));
        n.setVideo_id(safeString(bean.getVideoId()));
        List<Integer> lens = bean.getSwitchableLensTypeSwigValues();
        if (lens != null && !lens.isEmpty()) {
            n.setSwitchable_lens_types(toNativeIntVector(lens));
        }
        return n;
    }

    public static Device toDevice(com.dji.flighthubsdk.generated.Device n) {
        if (n == null) {
            return new Device("", "", "", "", "", false, Collections.emptyList());
        }
        return new Device(
                safeString(n.getDevice_sn()),
                safeString(n.getDevice_class()),
                safeString(n.getDevice_name()),
                safeString(n.getNickname()),
                safeString(n.getModel_key()),
                n.getIs_online(),
                copyStringVector(n.getParent_sns()));
    }

    public static List<Device> toDeviceList(com.dji.flighthubsdk.generated.DeviceVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<Device> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDevice(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DeviceTopo toDeviceTopo(com.dji.flighthubsdk.generated.DeviceTopo n) {
        if (n == null) {
            return new DeviceTopo(null, Collections.emptyList());
        }
        return new DeviceTopo(toDevice(n.getDevice()), toDeviceList(n.getSub_devices()));
    }

    public static List<DeviceTopo> toDeviceTopoList(
            com.dji.flighthubsdk.generated.DeviceTopoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceTopo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceTopo(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DeviceTopoChange toDeviceTopoChange(
            com.dji.flighthubsdk.generated.DeviceTopoChange n) {
        if (n == null) {
            return new DeviceTopoChange("", Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.StringVector sv = n.getDevices();
        List<String> sns;
        if (sv == null || sv.isEmpty()) {
            sns = Collections.emptyList();
        } else {
            sns = new ArrayList<>(sv.size());
            for (int i = 0; i < sv.size(); i++) {
                sns.add(safeString(sv.get(i)));
            }
            sns = Collections.unmodifiableList(sns);
        }
        return new DeviceTopoChange(safeString(n.getGateway_sn()), sns);
    }

    public static ModelInfo toModelInfo(com.dji.flighthubsdk.generated.ModelInfo n) {
        if (n == null) {
            return new ModelInfo("", "", "", "", "", "", "");
        }
        return new ModelInfo(
                safeString(n.getKey()),
                safeString(n.getDomain()),
                safeString(n.getType()),
                safeString(n.getSub_type()),
                safeString(n.getName()),
                safeString(n.getDevice_class()),
                safeString(n.getDevelop_code()));
    }

    public static SubDeviceInfo toSubDeviceInfo(com.dji.flighthubsdk.generated.SubDeviceInfo n) {
        if (n == null) {
            return new SubDeviceInfo("", null, false, "", 0, 0, "", Collections.emptyList());
        }
        SubDeviceInfo result = new SubDeviceInfo(
                safeString(n.getDevice_sn()),
                toModelInfo(n.getModel_info()),
                n.getDevice_online_status(),
                safeString(n.getDevice_firmware_version()),
                n.getDevice_firmware_status(),
                n.getDevice_working_status(),
                safeString(n.getFlysafe_database_version()),
                copyStringVector(n.getParent_sns()));
        result.setNickname(safeString(n.getNickname()));
        return result;
    }

    public static List<SubDeviceInfo> toSubDeviceInfoList(
            com.dji.flighthubsdk.generated.SubDeviceInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<SubDeviceInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toSubDeviceInfo(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DeviceDetailInfo toDeviceDetailInfo(
            com.dji.flighthubsdk.generated.DeviceDetailInfo n) {
        if (n == null) {
            return new DeviceDetailInfo("", "", null, 0, false, "", 0, 0, "", null);
        }
        return new DeviceDetailInfo(
                safeString(n.getDevice_sn()),
                safeString(n.getNickname()),
                toModelInfo(n.getModel_info()),
                n.getDevice_add_time(),
                n.getDevice_online_status(),
                safeString(n.getDevice_firmware_version()),
                n.getDevice_firmware_status(),
                n.getDevice_working_status(),
                safeString(n.getComplete_machine_sn()),
                toSubDeviceInfoList(n.getSub_devices()));
    }

    public static DeviceChange toDeviceChange(
            com.dji.flighthubsdk.generated.DeviceChange n) {
        if (n == null) {
            return new DeviceChange("", Collections.emptyList());
        }
        return new DeviceChange(safeString(n.getChange_type()), copyStringVector(n.getChange_devices()));
    }

    public static List<DeviceChange> toDeviceChangeList(
            com.dji.flighthubsdk.generated.DeviceChangeVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceChange> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceChange(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static WorkspaceDeviceChange toWorkspaceDeviceChange(
            com.dji.flighthubsdk.generated.WorkspaceDeviceChange n) {
        if (n == null) {
            return new WorkspaceDeviceChange(Collections.emptyList());
        }
        return new WorkspaceDeviceChange(toDeviceChangeList(n.getDevice_changes()));
    }

    public static STSCredential toSTSCredential(com.dji.flighthubsdk.generated.STSCredential n) {
        if (n == null) {
            return new STSCredential("", "", 0, "", 0);
        }
        return new STSCredential(
                safeString(n.getAccess_key_id()),
                safeString(n.getAccess_key_secret()),
                n.getExpire(),
                safeString(n.getSecurity_token()),
                n.getPlatform());
    }

    public static STSInfo toSTSInfo(com.dji.flighthubsdk.generated.STSInfo n) {
        if (n == null) {
            return new STSInfo("", "", "", "", "", null);
        }
        return new STSInfo(
                safeString(n.getObject_key_prefix()),
                safeString(n.getBucket()),
                safeString(n.getEndpoint()),
                safeString(n.getProvider()),
                safeString(n.getRegion()),
                toSTSCredential(n.getCredentials()));
    }

    public static com.dji.flighthubsdk.generated.STSCredential toNativeSTSCredential(STSCredential bean) {
        com.dji.flighthubsdk.generated.STSCredential n = new com.dji.flighthubsdk.generated.STSCredential();
        if (bean == null) {
            return n;
        }
        n.setAccess_key_id(safeString(bean.getAccessKeyId()));
        n.setAccess_key_secret(safeString(bean.getAccessKeySecret()));
        n.setExpire(bean.getExpire());
        n.setSecurity_token(safeString(bean.getSecurityToken()));
        n.setPlatform(bean.getPlatform());
        return n;
    }

    /**
     * 构造 JNI 用 {@code STSInfo}；调用方应在用完后 {@link com.dji.flighthubsdk.generated.STSInfo#delete()} 释放（若不再持有引用）。
     */
    public static com.dji.flighthubsdk.generated.STSInfo toNativeSTSInfo(STSInfo bean) {
        if (bean == null) {
            return new com.dji.flighthubsdk.generated.STSInfo();
        }
        com.dji.flighthubsdk.generated.STSInfo n = new com.dji.flighthubsdk.generated.STSInfo();
        n.setObject_key_prefix(safeString(bean.getObjectKeyPrefix()));
        n.setBucket(safeString(bean.getBucket()));
        n.setEndpoint(safeString(bean.getEndpoint()));
        n.setProvider(safeString(bean.getProvider()));
        n.setRegion(safeString(bean.getRegion()));
        n.setCredentials(toNativeSTSCredential(bean.getCredentials()));
        return n;
    }

    public static PsdkSpeakerResource toPsdkSpeakerResource(
            com.dji.flighthubsdk.generated.PsdkSpeakerResource n) {
        if (n == null) {
            return new PsdkSpeakerResource(
                    0,
                    "",
                    "",
                    "",
                    "",
                    "",
                    0L,
                    0L,
                    "",
                    "",
                    "",
                    "",
                    0L,
                    "",
                    "",
                    "",
                    0L,
                    "",
                    0L,
                    "");
        }
        return new PsdkSpeakerResource(
                n.getId(),
                safeString(n.getName()),
                safeString(n.getFile_type()),
                safeString(n.getObject_key()),
                safeString(n.getObject_url()),
                safeString(n.getFormat()),
                n.getSize(),
                n.getDuration(),
                safeString(n.getState()),
                safeString(n.getState_message()),
                safeString(n.getPcm_object_key()),
                safeString(n.getPcm_object_url()),
                n.getPcm_size(),
                safeString(n.getPcm_md5_sum()),
                safeString(n.getContent()),
                safeString(n.getMd5_sum()),
                n.getCreated_at(),
                safeString(n.getOpus_object_key()),
                n.getOpus_size(),
                safeString(n.getOpus_md5_sum()));
    }

    public static List<PsdkSpeakerResource> toPsdkSpeakerResourceList(
            com.dji.flighthubsdk.generated.PsdkSpeakerResourceVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<PsdkSpeakerResource> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toPsdkSpeakerResource(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static PsdkSpeakerResourceList toPsdkSpeakerResourceList(
            com.dji.flighthubsdk.generated.PsdkSpeakerResourceList n) {
        if (n == null) {
            return new PsdkSpeakerResourceList(Collections.emptyList());
        }
        return new PsdkSpeakerResourceList(toPsdkSpeakerResourceList(n.getResources()));
    }

    public static PsdkWidgetResource toPsdkWidgetResource(
            com.dji.flighthubsdk.generated.PsdkWidgetResource n) {
        if (n == null) {
            return new PsdkWidgetResource("", "");
        }
        return new PsdkWidgetResource(safeString(n.getObject_key()), safeString(n.getUrl()));
    }

    public static List<PsdkWidgetResource> toPsdkWidgetResourceList(
            com.dji.flighthubsdk.generated.PsdkWidgetResourceVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<PsdkWidgetResource> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toPsdkWidgetResource(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static PsdkWidgetResourceList toPsdkWidgetResourceList(
            com.dji.flighthubsdk.generated.PsdkWidgetResourceList n) {
        if (n == null) {
            return new PsdkWidgetResourceList(Collections.emptyList());
        }
        return new PsdkWidgetResourceList(toPsdkWidgetResourceList(n.getUi_resource_urls()));
    }

    public static DeviceAudienceCountItem toDeviceAudienceCountItem(
            com.dji.flighthubsdk.generated.DeviceAudienceCountItem n) {
        if (n == null) {
            return new DeviceAudienceCountItem("", 0);
        }
        return new DeviceAudienceCountItem(safeString(n.getSn()), n.getCount());
    }

    public static List<DeviceAudienceCountItem> toDeviceAudienceCountItemList(
            com.dji.flighthubsdk.generated.DeviceAudienceCountItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceAudienceCountItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceAudienceCountItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DeviceStatusModel toDeviceStatusModel(
            com.dji.flighthubsdk.generated.DockStatusDeviceModelResult n) {
        if (n == null) {
            return new DeviceStatusModel("", "", "");
        }
        return new DeviceStatusModel(
                safeString(n.getDevice_class()), safeString(n.getKey()), safeString(n.getName()));
    }

    public static DeviceStatusModel toDeviceStatusModel(
            com.dji.flighthubsdk.generated.DroneStatusDeviceModelResult n) {
        if (n == null) {
            return new DeviceStatusModel("", "", "");
        }
        return new DeviceStatusModel(
                safeString(n.getDevice_class()), safeString(n.getKey()), safeString(n.getName()));
    }

    public static DockStatusAirConditionerResult toDockStatusAirConditionerResult(
            com.dji.flighthubsdk.generated.DockStatusAirConditionerResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusAirConditionerResult(n.getAir_conditioner_state(), n.getSwitch_time());
    }

    public static DockStatusAlternateLandPointResult toDockStatusAlternateLandPointResult(
            com.dji.flighthubsdk.generated.DockStatusAlternateLandPointResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusAlternateLandPointResult(
                n.getHeight(),
                n.getIs_configured(),
                n.getLatitude(),
                n.getLongitude(),
                n.getSafe_land_height());
    }

    public static DockStatusBackupBatteryResult toDockStatusBackupBatteryResult(
            com.dji.flighthubsdk.generated.DockStatusBackupBatteryResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusBackupBatteryResult(n.getSwitch_state(), n.getTemperature(), n.getVoltage());
    }

    public static DockStatusNetworkStateResult toDockStatusNetworkStateResult(
            com.dji.flighthubsdk.generated.DockStatusNetworkStateResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusNetworkStateResult(n.getQuality(), n.getRate(), n.getType());
    }

    public static DockStatusPositionStateResult toDockStatusPositionStateResult(
            com.dji.flighthubsdk.generated.DockStatusPositionStateResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusPositionStateResult(
                n.getGps_number(), n.getIs_calibration(), n.getIs_fixed(), n.getQuality(), n.getRtk_number());
    }

    public static DockStatusTiltAngleResult toDockStatusTiltAngleResult(
            com.dji.flighthubsdk.generated.DockStatusTiltAngleResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusTiltAngleResult(n.getValid(), n.getValue());
    }

    public static DockStatusPropertiesResult toDockStatusPropertiesResult(
            com.dji.flighthubsdk.generated.DockStatusPropertiesResult n) {
        if (n == null) {
            return null;
        }
        return new DockStatusPropertiesResult(
                n.getAcc_time(),
                n.getAcdc_power_input(),
                n.getElectric_supply_voltage(),
                n.getEnvironment_temperature(),
                n.getHumidity(),
                n.getJob_number(),
                n.getPoe_link_status(),
                n.getPoe_power_output(),
                n.getRainfall(),
                n.getTemperature(),
                n.getWorking_voltage(),
                toDockStatusAirConditionerResult(n.getAir_conditioner()),
                toDockStatusAlternateLandPointResult(n.getAlternate_land_point()),
                toDockStatusBackupBatteryResult(n.getBackup_battery()),
                toDockStatusNetworkStateResult(n.getNetwork_state()),
                toDockStatusPositionStateResult(n.getPosition_state()),
                toDockStatusTiltAngleResult(n.getTilt_angle()));
    }

    public static DockStatusItemResult toDockStatusItemResult(
            com.dji.flighthubsdk.generated.DockStatusItemResult n) {
        if (n == null) {
            return new DockStatusItemResult("", false, 0L, new DeviceStatusModel("", "", ""), null);
        }
        return new DockStatusItemResult(
                safeString(n.getSn()),
                n.getOnline_status(),
                n.getUpdated_time(),
                toDeviceStatusModel(n.getDevice_model()),
                toDockStatusPropertiesResult(n.getProperties()));
    }

    public static List<DockStatusItemResult> toDockStatusItemResultList(
            com.dji.flighthubsdk.generated.DockDeviceStatusResultList r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.DockStatusItemResultVector v = r.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DockStatusItemResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDockStatusItemResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DroneStatusBatteryItemResult toDroneStatusBatteryItemResult(
            com.dji.flighthubsdk.generated.DroneStatusBatteryItemResult n) {
        if (n == null) {
            return new DroneStatusBatteryItemResult(
                    0, "", 0, 0, 0, "", 0, 0.0, 0, 0);
        }
        return new DroneStatusBatteryItemResult(
                n.getCapacity_percent(),
                safeString(n.getFirmware_version()),
                n.getHigh_voltage_storage_days(),
                n.getIndex(),
                n.getLoop_times(),
                safeString(n.getSn()),
                n.getSub_type(),
                n.getTemperature(),
                n.getType(),
                n.getVoltage());
    }

    public static List<DroneStatusBatteryItemResult> toDroneStatusBatteryItemList(
            com.dji.flighthubsdk.generated.DroneStatusBatteryItemResultVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DroneStatusBatteryItemResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDroneStatusBatteryItemResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DroneStatusBatteryResult toDroneStatusBatteryResult(
            com.dji.flighthubsdk.generated.DroneStatusBatteryResult n) {
        if (n == null) {
            return null;
        }
        return new DroneStatusBatteryResult(
                toDroneStatusBatteryItemList(n.getBatteries()),
                n.getCapacity_percent(),
                n.getLanding_power(),
                n.getRemain_flight_time(),
                n.getRemain_job_time(),
                n.getReturn_home_power());
    }

    public static DroneStatusDistanceLimitStatusResult toDroneStatusDistanceLimitStatusResult(
            com.dji.flighthubsdk.generated.DroneStatusDistanceLimitStatusResult n) {
        if (n == null) {
            return null;
        }
        return new DroneStatusDistanceLimitStatusResult(
                n.getDistance_limit(), n.getIs_near_distance_limit(), n.getState());
    }

    public static DroneStatusObstacleAvoidanceResult toDroneStatusObstacleAvoidanceResult(
            com.dji.flighthubsdk.generated.DroneStatusObstacleAvoidanceResult n) {
        if (n == null) {
            return null;
        }
        return new DroneStatusObstacleAvoidanceResult(n.getDownside(), n.getHorizon(), n.getUpside());
    }

    public static DroneStatusPositionStateResult toDroneStatusPositionStateResult(
            com.dji.flighthubsdk.generated.DroneStatusPositionStateResult n) {
        if (n == null) {
            return null;
        }
        return new DroneStatusPositionStateResult(
                n.getGps_number(), n.getIs_fixed(), n.getQuality(), n.getRtk_number());
    }

    public static DroneStatusStorageResult toDroneStatusStorageResult(
            com.dji.flighthubsdk.generated.DroneStatusStorageResult n) {
        if (n == null) {
            return null;
        }
        return new DroneStatusStorageResult(n.getTotal(), n.getUsed());
    }

    public static DroneStatusPropertiesResult toDroneStatusPropertiesResult(
            com.dji.flighthubsdk.generated.DroneStatusPropertiesResult n) {
        if (n == null) {
            return null;
        }
        return new DroneStatusPropertiesResult(
                n.getHeight_limit(),
                n.getHome_distance(),
                n.getHome_latitude(),
                n.getHome_longitude(),
                n.getHorizontal_speed(),
                n.getLow_battery_warning_threshold(),
                n.getMode_code(),
                n.getMode_code_reason(),
                n.getNight_lights_state(),
                n.getRemaining_power_for_return_home(),
                n.getSerious_low_battery_warning_threshold(),
                n.getTotal_flight_distance(),
                n.getTotal_flight_sorties(),
                n.getTotal_flight_time(),
                n.getVertical_speed(),
                toDroneStatusBatteryResult(n.getBattery()),
                toDroneStatusDistanceLimitStatusResult(n.getDistance_limit_status()),
                toDroneStatusObstacleAvoidanceResult(n.getObstacle_avoidance()),
                toDroneStatusPositionStateResult(n.getPosition_state()),
                toDroneStatusStorageResult(n.getStorage()));
    }

    public static DroneStatusItemResult toDroneStatusItemResult(
            com.dji.flighthubsdk.generated.DroneStatusItemResult n) {
        if (n == null) {
            return new DroneStatusItemResult("", false, 0L, new DeviceStatusModel("", "", ""), null);
        }
        return new DroneStatusItemResult(
                safeString(n.getSn()),
                n.getOnline_status(),
                n.getUpdated_time(),
                toDeviceStatusModel(n.getDevice_model()),
                toDroneStatusPropertiesResult(n.getProperties()));
    }

    public static List<DroneStatusItemResult> toDroneStatusItemResultList(
            com.dji.flighthubsdk.generated.DroneDeviceStatusResultList r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.DroneStatusItemResultVector v = r.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DroneStatusItemResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDroneStatusItemResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    // --- Workspace：固件 / FlySafe / 解锁证书 / 航线 ---

    public static FirmwareReleaseNoteResult toFirmwareReleaseNoteResult(
            com.dji.flighthubsdk.generated.FirmwareReleaseNoteResult n) {
        if (n == null) {
            return new FirmwareReleaseNoteResult("", "", "", "", "");
        }
        return new FirmwareReleaseNoteResult(
                safeString(n.getDevice_model_key()),
                safeString(n.getLang()),
                safeString(n.getProduct_version()),
                safeString(n.getRelease_note()),
                safeString(n.getReleased_time()));
    }

    public static List<FirmwareReleaseNoteResult> toFirmwareReleaseNoteResultList(
            com.dji.flighthubsdk.generated.FirmwareReleaseNoteListResult r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.FirmwareReleaseNoteResultVector v = r.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<FirmwareReleaseNoteResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFirmwareReleaseNoteResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static FirmwareUpgradeResponseDeviceItemResult toFirmwareUpgradeResponseDeviceItemResult(
            com.dji.flighthubsdk.generated.FirmwareUpgradeResponseDeviceItemResult n) {
        if (n == null) {
            return new FirmwareUpgradeResponseDeviceItemResult(0, "", "");
        }
        return new FirmwareUpgradeResponseDeviceItemResult(
                n.getFirmware_upgrade_type(), safeString(n.getProduct_version()), safeString(n.getSn()));
    }

    public static FirmwareUpgradeResult toFirmwareUpgradeResult(
            com.dji.flighthubsdk.generated.FirmwareUpgradeResult n) {
        if (n == null) {
            return new FirmwareUpgradeResult(0, "", Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.FirmwareUpgradeResponseDeviceItemResultVector v = n.getDevices();
        List<FirmwareUpgradeResponseDeviceItemResult> devices = Collections.emptyList();
        if (v != null && !v.isEmpty()) {
            devices = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                devices.add(toFirmwareUpgradeResponseDeviceItemResult(v.get(i)));
            }
            devices = Collections.unmodifiableList(devices);
        }
        return new FirmwareUpgradeResult(n.getDevice_code(), safeString(n.getStatus()), devices);
    }

    public static FirmwareUpgradeProgressStep toFirmwareUpgradeProgressStep(
            com.dji.flighthubsdk.generated.FirmwareUpgradeProgressStep n) {
        if (n == null) {
            return null;
        }
        return new FirmwareUpgradeProgressStep(n.getCurrent_step(), n.getPercent());
    }

    public static FirmwareUpgradeProgressOTA toFirmwareUpgradeProgressOTA(
            com.dji.flighthubsdk.generated.FirmwareUpgradeProgressOTA n) {
        if (n == null) {
            return null;
        }
        return new FirmwareUpgradeProgressOTA(
                safeString(n.getStatus()), toFirmwareUpgradeProgressStep(n.getProgress()));
    }

    public static FirmwareUpgradeAssociateDrone toFirmwareUpgradeAssociateDrone(
            com.dji.flighthubsdk.generated.FirmwareUpgradeAssociateDrone n) {
        if (n == null) {
            return null;
        }
        return new FirmwareUpgradeAssociateDrone(
                n.getFirmware_status(), safeString(n.getSn()), n.getWorking_status());
    }

    public static FirmwareUpgradeAssociateRelay toFirmwareUpgradeAssociateRelay(
            com.dji.flighthubsdk.generated.FirmwareUpgradeAssociateRelay n) {
        if (n == null) {
            return null;
        }
        return new FirmwareUpgradeAssociateRelay(n.getFirmware_status(), safeString(n.getSn()));
    }

    public static FirmwareUpgradeProgressItem toFirmwareUpgradeProgressItem(
            com.dji.flighthubsdk.generated.FirmwareUpgradeProgressItem n) {
        if (n == null) {
            return new FirmwareUpgradeProgressItem(
                    null, null, 0, 0, null, "", 0);
        }
        return new FirmwareUpgradeProgressItem(
                toFirmwareUpgradeAssociateDrone(n.getAssociate_drone()),
                toFirmwareUpgradeAssociateRelay(n.getAssociate_relay()),
                n.getDevice_code(),
                n.getFirmware_status(),
                toFirmwareUpgradeProgressOTA(n.getOta()),
                safeString(n.getSn()),
                n.getWorking_status());
    }

    public static List<FirmwareUpgradeProgressItem> toFirmwareUpgradeProgressItemList(
            com.dji.flighthubsdk.generated.FirmwareUpgradeProgressResult r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.FirmwareUpgradeProgressItemVector v = r.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<FirmwareUpgradeProgressItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFirmwareUpgradeProgressItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static FirmwareItemResult toFirmwareItemResult(
            com.dji.flighthubsdk.generated.FirmwareItemResult n) {
        if (n == null) {
            return new FirmwareItemResult(0, "", "", "", "", "");
        }
        return new FirmwareItemResult(
                n.getId(),
                safeString(n.getCreated_at()),
                safeString(n.getDevice_model_key()),
                safeString(n.getStatus()),
                safeString(n.getVersion()),
                safeString(n.getDesc()));
    }

    public static List<FirmwareItemResult> toFirmwareItemResultList(
            com.dji.flighthubsdk.generated.FirmwareListResult r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.FirmwareItemResultVector v = r.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<FirmwareItemResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFirmwareItemResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static FirmwareCompatibilityRulesResult toFirmwareCompatibilityRulesResult(
            com.dji.flighthubsdk.generated.FirmwareCompatibilityRulesResult n) {
        if (n == null) {
            return null;
        }
        return new FirmwareCompatibilityRulesResult(safeString(n.getMin_version()), safeString(n.getMax_version()));
    }

    public static FirmwareCompatibilityConfigItemResult toFirmwareCompatibilityConfigItemResult(
            com.dji.flighthubsdk.generated.FirmwareCompatibilityConfigItemResult n) {
        if (n == null) {
            return new FirmwareCompatibilityConfigItemResult("", Collections.emptyList(), null);
        }
        List<String> cv = Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getCompatible_versions())));
        return new FirmwareCompatibilityConfigItemResult(
                safeString(n.getDevice_model_key()), cv, toFirmwareCompatibilityRulesResult(n.getRules()));
    }

    public static List<FirmwareCompatibilityConfigItemResult> toFirmwareCompatibilityConfigItemResultList(
            com.dji.flighthubsdk.generated.FirmwareCompatibilityConfigResult r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.FirmwareCompatibilityConfigItemResultVector v = r.getData();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<FirmwareCompatibilityConfigItemResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFirmwareCompatibilityConfigItemResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static FirmwareServiceProgress toFirmwareServiceProgress(
            com.dji.flighthubsdk.generated.FirmwareServiceProgress n) {
        if (n == null) {
            return null;
        }
        return new FirmwareServiceProgress(n.getPercent(), n.getCurrent_step());
    }

    public static FirmwareServiceDevice toFirmwareServiceDevice(
            com.dji.flighthubsdk.generated.FirmwareServiceDevice n) {
        if (n == null) {
            return new FirmwareServiceDevice("", 0, "");
        }
        return new FirmwareServiceDevice(
                safeString(n.getSn()), n.getFirmware_upgrade_type(), safeString(n.getProduct_version()));
    }

    public static FirmwareServiceData toFirmwareServiceData(
            com.dji.flighthubsdk.generated.FirmwareServiceData n) {
        if (n == null) {
            return new FirmwareServiceData(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.FirmwareServiceDeviceVector v = n.getDevices();
        if (v == null || v.isEmpty()) {
            return new FirmwareServiceData(Collections.emptyList());
        }
        List<FirmwareServiceDevice> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFirmwareServiceDevice(v.get(i)));
        }
        return new FirmwareServiceData(Collections.unmodifiableList(out));
    }

    public static FirmwareService toFirmwareService(
            com.dji.flighthubsdk.generated.FirmwareService n) {
        if (n == null) {
            return null;
        }
        return new FirmwareService(
                safeString(n.getBid()),
                safeString(n.getStatus()),
                n.getCreate_time(),
                n.getUpdate_time(),
                n.getDevice_code(),
                toFirmwareServiceProgress(n.getProgress()),
                toFirmwareServiceData(n.getData()));
    }

    public static FirmwareStatusItem toFirmwareStatusItem(
            com.dji.flighthubsdk.generated.FirmwareStatusItem n) {
        if (n == null) {
            return new FirmwareStatusItem("", "", 0, "", null);
        }
        return new FirmwareStatusItem(
                safeString(n.getSn()),
                safeString(n.getFirmware_version()),
                n.getFirmware_status(),
                safeString(n.getDevice_model_key()),
                toFirmwareService(n.getServices()));
    }

    public static List<FirmwareStatusItem> toFirmwareStatusItemList(
            com.dji.flighthubsdk.generated.FirmwareStatusListResult r) {
        if (r == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.FirmwareStatusItemVector v = r.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<FirmwareStatusItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFirmwareStatusItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DeviceUnlockLicenseCommonFields toDeviceUnlockLicenseCommonFields(
            com.dji.flighthubsdk.generated.DeviceUnlockLicenseCommonFields n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockLicenseCommonFields(
                n.getId(),
                safeString(n.getName()),
                n.getType(),
                n.getGroup_id(),
                safeString(n.getUser_id()),
                safeString(n.getDevice_sn()),
                n.getBegin_time(),
                n.getEnd_time(),
                n.getUser_only(),
                n.getEnabled());
    }

    public static DeviceUnlockAreaUnlock toDeviceUnlockAreaUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockAreaUnlock n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockAreaUnlock(Collections.unmodifiableList(new ArrayList<>(copyIntVector(n.getArea_ids()))));
    }

    public static DeviceUnlockCircleUnlock toDeviceUnlockCircleUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockCircleUnlock n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockCircleUnlock(
                n.getRadius(), n.getLatitude(), n.getLongitude(), n.getHeight());
    }

    public static DeviceUnlockCountryUnlock toDeviceUnlockCountryUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockCountryUnlock n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockCountryUnlock(n.getCountry_number(), n.getHeight());
    }

    public static DeviceUnlockHeightUnlock toDeviceUnlockHeightUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockHeightUnlock n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockHeightUnlock(n.getHeight());
    }

    public static DeviceUnlockPolygonPoint toDeviceUnlockPolygonPoint(
            com.dji.flighthubsdk.generated.DeviceUnlockPolygonPoint n) {
        if (n == null) {
            return new DeviceUnlockPolygonPoint(0, 0);
        }
        return new DeviceUnlockPolygonPoint(n.getLatitude(), n.getLongitude());
    }

    public static DeviceUnlockPolygonUnlock toDeviceUnlockPolygonUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockPolygonUnlock n) {
        if (n == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.DeviceUnlockPolygonPointVector v = n.getPoints();
        if (v == null || v.isEmpty()) {
            return new DeviceUnlockPolygonUnlock(Collections.emptyList());
        }
        List<DeviceUnlockPolygonPoint> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceUnlockPolygonPoint(v.get(i)));
        }
        return new DeviceUnlockPolygonUnlock(Collections.unmodifiableList(out));
    }

    public static DeviceUnlockPowerUnlock toDeviceUnlockPowerUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockPowerUnlock n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockPowerUnlock(n.getLevel());
    }

    public static DeviceUnlockRidUnlock toDeviceUnlockRidUnlock(
            com.dji.flighthubsdk.generated.DeviceUnlockRidUnlock n) {
        if (n == null) {
            return null;
        }
        return new DeviceUnlockRidUnlock(n.getLevel());
    }

    public static DeviceUnlockLicenseItem toDeviceUnlockLicenseItem(
            com.dji.flighthubsdk.generated.DeviceUnlockLicenseItem n) {
        if (n == null) {
            return new DeviceUnlockLicenseItem(
                    null, null, null, null, null, null, null, null);
        }
        return new DeviceUnlockLicenseItem(
                toDeviceUnlockLicenseCommonFields(n.getCommon_fields()),
                toDeviceUnlockAreaUnlock(n.getArea_unlock()),
                toDeviceUnlockCircleUnlock(n.getCircle_unlock()),
                toDeviceUnlockCountryUnlock(n.getCountry_unlock()),
                toDeviceUnlockHeightUnlock(n.getHeight_unlock()),
                toDeviceUnlockPolygonUnlock(n.getPolygon_unlock()),
                toDeviceUnlockPowerUnlock(n.getPower_unlock()),
                toDeviceUnlockRidUnlock(n.getRid_unlock()));
    }

    public static List<DeviceUnlockLicenseItem> toDeviceUnlockLicenseItemList(
            com.dji.flighthubsdk.generated.DeviceUnlockLicenseItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceUnlockLicenseItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceUnlockLicenseItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static QueryDeviceUnlockLicenseListResult toQueryDeviceUnlockLicenseListResult(
            com.dji.flighthubsdk.generated.QueryDeviceUnlockLicenseListResult n) {
        if (n == null) {
            return new QueryDeviceUnlockLicenseListResult(0, 0, false, Collections.emptyList());
        }
        return new QueryDeviceUnlockLicenseListResult(
                n.getResult(),
                n.getDevice_model_domain(),
                n.getConsistence(),
                toDeviceUnlockLicenseItemList(n.getLicenses()));
    }

    public static CheckUnlockLicenseResultItem toCheckUnlockLicenseResultItem(
            com.dji.flighthubsdk.generated.CheckUnlockLicenseResultItem n) {
        if (n == null) {
            return new CheckUnlockLicenseResultItem("", "", "", 0L, 0L, "");
        }
        return new CheckUnlockLicenseResultItem(
                safeString(n.getFile_name()),
                safeString(n.getName()),
                safeString(n.getType()),
                n.getBegin_time(),
                n.getEnd_time(),
                safeString(n.getUrl()));
    }

    public static CheckUnlockLicenseResult toCheckUnlockLicenseResult(
            com.dji.flighthubsdk.generated.CheckUnlockLicenseResult n) {
        if (n == null) {
            return new CheckUnlockLicenseResult(
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
        List<String> ef = Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getErr_file_name())));
        List<String> em = Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getErr_cert_mismatch())));
        com.dji.flighthubsdk.generated.CheckUnlockLicenseResultItemVector v = n.getList();
        List<CheckUnlockLicenseResultItem> items = Collections.emptyList();
        if (v != null && !v.isEmpty()) {
            items = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                items.add(toCheckUnlockLicenseResultItem(v.get(i)));
            }
            items = Collections.unmodifiableList(items);
        }
        return new CheckUnlockLicenseResult(ef, em, items);
    }

    public static WaylineListPaginationResult toWaylineListPaginationResult(
            com.dji.flighthubsdk.generated.WaylineListPaginationResult n) {
        if (n == null) {
            return new WaylineListPaginationResult(0, 0, 0);
        }
        return new WaylineListPaginationResult(n.getPage(), n.getPage_size(), n.getTotal());
    }

    public static WaylineListItemResult toWaylineListItemResult(
            com.dji.flighthubsdk.generated.WaylineListItemResult n) {
        if (n == null) {
            return new WaylineListItemResult(
                    "",
                    Collections.emptyList(),
                    "",
                    0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "",
                    0L,
                    false,
                    false,
                    false);
        }
        return new WaylineListItemResult(
                safeString(n.getWayline_uuid()),
                Collections.unmodifiableList(new ArrayList<>(copyIntVector(n.getWayline_types()))),
                safeString(n.getWayline_name()),
                n.getSize(),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getPayload_types()))),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getPayload_types_with_index()))),
                safeString(n.getDrone_type()),
                n.getUpdated_at(),
                n.getWith_megaphone(),
                n.getWith_search_light(),
                n.getIs_ai_wayline());
    }

    public static GetWaylineListResult toGetWaylineListResult(
            com.dji.flighthubsdk.generated.GetWaylineListResult n) {
        if (n == null) {
            return new GetWaylineListResult(
                    new WaylineListPaginationResult(0, 0, 0), Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.WaylineListItemResultVector v = n.getList();
        List<WaylineListItemResult> items = Collections.emptyList();
        if (v != null && !v.isEmpty()) {
            items = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                items.add(toWaylineListItemResult(v.get(i)));
            }
            items = Collections.unmodifiableList(items);
        }
        return new GetWaylineListResult(toWaylineListPaginationResult(n.getPagination()), items);
    }

    public static WaylineDetailSurveyAreaScopePoint toWaylineDetailSurveyAreaScopePoint(
            com.dji.flighthubsdk.generated.WaylineDetailSurveyAreaScopePoint n) {
        if (n == null) {
            return new WaylineDetailSurveyAreaScopePoint(0, 0, 0);
        }
        return new WaylineDetailSurveyAreaScopePoint(n.getLng(), n.getLat(), n.getAlt());
    }

    public static WaylineDetailSurveyAreaScope toWaylineDetailSurveyAreaScope(
            com.dji.flighthubsdk.generated.WaylineDetailSurveyAreaScope n) {
        if (n == null) {
            return new WaylineDetailSurveyAreaScope(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.WaylineDetailSurveyAreaScopePointVector v = n.getPoints();
        if (v == null || v.isEmpty()) {
            return new WaylineDetailSurveyAreaScope(Collections.emptyList());
        }
        List<WaylineDetailSurveyAreaScopePoint> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toWaylineDetailSurveyAreaScopePoint(v.get(i)));
        }
        return new WaylineDetailSurveyAreaScope(Collections.unmodifiableList(out));
    }

    public static WaylineDetailKmzData toWaylineDetailKmzData(
            com.dji.flighthubsdk.generated.WaylineDetailKmzData n) {
        if (n == null) {
            return null;
        }
        return new WaylineDetailKmzData(
                n.getWayline_type(),
                n.getWayline_point_count(),
                n.getExit_on_rc_lost_type(),
                n.getExit_on_rc_lost_behavior(),
                n.getStart_record_action_count(),
                n.getStop_record_action_count(),
                n.getOrtho_camera_overlap_h(),
                n.getOrtho_camera_overlap_w(),
                n.getInclined_camera_overlap_h(),
                n.getInclined_camera_overlap_w(),
                n.getSupplement_prism_model_count(),
                n.getEstimated_photo_count(),
                n.getDuration(),
                n.getDistance(),
                n.getSurvey_area(),
                n.getStart_latitude(),
                n.getStart_longitude(),
                n.getStart_altitude(),
                n.getMargin_distance(),
                n.getOrtho_gsd(),
                n.getIncline_gsd(),
                n.getIs_wayline_validity_check(),
                n.getHas_oriented_action(),
                n.getHas_accurate_action(),
                n.getIs_surface_fly_on(),
                n.getIs_elevation_optimization_on(),
                n.getIs_smart_oblique_on(),
                n.getIs_mapping_strip_use_template_altitude(),
                n.getIs_with_megaphone(),
                n.getIs_with_search_light(),
                n.getIs_ai_wayline(),
                safeString(n.getDrone_type()),
                safeString(n.getTarget_detect_area_file_path()),
                Collections.unmodifiableList(new ArrayList<>(copyIntVector(n.getWayline_validity_check_codes()))),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getElevation_file_names()))),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getPayload_types()))),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getPayload_types_with_index()))),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getAccurate_action_uuids()))),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getAction_uuids()))),
                toWaylineDetailSurveyAreaScope(n.getSurvey_area_scope()));
    }

    public static WaylineDetailExtData toWaylineDetailExtData(
            com.dji.flighthubsdk.generated.WaylineDetailExtData n) {
        if (n == null) {
            return null;
        }
        return new WaylineDetailExtData(
                safeString(n.getKmz_without_res_object_key()),
                safeString(n.getKmz_without_res_md5()),
                safeString(n.getTarget_area_file_object_key()),
                safeString(n.getBusiness_file_object_key()));
    }

    public static GetWaylineDetailResult toGetWaylineDetailResult(
            com.dji.flighthubsdk.generated.GetWaylineDetailResult n) {
        if (n == null) {
            return null;
        }
        return new GetWaylineDetailResult(
                safeString(n.getWayline_uuid()),
                safeString(n.getMd5()),
                safeString(n.getWayline_name()),
                safeString(n.getKmz_object_key()),
                safeString(n.getUrl()),
                safeString(n.getSuffix()),
                n.getDeleted_at(),
                n.getUpdated_at(),
                safeString(n.getKmz_without_res_url()),
                safeString(n.getTarget_area_file_url()),
                safeString(n.getBusiness_file_url()),
                toWaylineDetailKmzData(n.getKmz_data()),
                toWaylineDetailExtData(n.getExt_data()),
                copyStringStringUnorderedMap(n.getEditor_warnings()),
                copyStringStringUnorderedMap(n.getExecute_warnings()));
    }

    private static WaylineDetailSurveyAreaScope toImportWaylineEventSurveyAreaScope(
            com.dji.flighthubsdk.generated.ImportWaylineEventSurveyAreaScope n) {
        if (n == null) {
            return new WaylineDetailSurveyAreaScope(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.ImportWaylineEventPointVector v = n.getPoints();
        if (v == null || v.isEmpty()) {
            return new WaylineDetailSurveyAreaScope(Collections.emptyList());
        }
        List<WaylineDetailSurveyAreaScopePoint> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            com.dji.flighthubsdk.generated.ImportWaylineEventPoint p = v.get(i);
            out.add(new WaylineDetailSurveyAreaScopePoint(p.getLng(), p.getLat(), p.getAlt()));
        }
        return new WaylineDetailSurveyAreaScope(Collections.unmodifiableList(out));
    }

    public static ImportWaylineEventKmzData toImportWaylineEventKmzData(
            com.dji.flighthubsdk.generated.ImportWaylineEventKmzData n) {
        if (n == null) {
            return ImportWaylineEventKmzData.empty();
        }
        return new ImportWaylineEventKmzData(
                n.getWayline_type(),
                n.getWayline_point_count(),
                n.getExit_on_rc_lost_type(),
                n.getExit_on_rc_lost_behavior(),
                n.getStart_record_action_count(),
                n.getStop_record_action_count(),
                n.getOrtho_camera_overlap_h(),
                n.getOrtho_camera_overlap_w(),
                n.getInclined_camera_overlap_h(),
                n.getInclined_camera_overlap_w(),
                n.getSupplement_prism_model_count(),
                n.getEstimated_photo_count(),
                n.getDuration(),
                n.getDistance(),
                n.getSurvey_area(),
                n.getStart_latitude(),
                n.getStart_longitude(),
                n.getStart_altitude(),
                n.getMargin_distance(),
                n.getOrtho_gsd(),
                n.getIncline_gsd(),
                n.getIs_wayline_validity_check(),
                n.getHas_oriented_action(),
                n.getHas_accurate_action(),
                n.getIs_surface_fly_on(),
                n.getIs_elevation_optimization_on(),
                n.getIs_smart_oblique_on(),
                n.getIs_mapping_strip_use_template_altitude(),
                n.getIs_with_megaphone(),
                n.getIs_with_search_light(),
                n.getIs_ai_wayline(),
                safeString(n.getDrone_type()),
                safeString(n.getTarget_detect_area_file_path()),
                Collections.unmodifiableList(copyIntVector(n.getWayline_validity_check_codes())),
                Collections.unmodifiableList(copyStringVector(n.getElevation_file_names())),
                Collections.unmodifiableList(copyStringVector(n.getPayload_types())),
                Collections.unmodifiableList(copyStringVector(n.getPayload_types_with_index())),
                Collections.unmodifiableList(copyStringVector(n.getAccurate_action_uuids())),
                Collections.unmodifiableList(copyStringVector(n.getAction_uuids())),
                toImportWaylineEventSurveyAreaScope(n.getSurvey_area_scope()),
                Collections.unmodifiableList(copyStringVector(n.getRes_file_md5s())),
                Collections.unmodifiableList(copyStringVector(n.getAudio_file_md5s())),
                Collections.unmodifiableList(copyStringVector(n.getTarget_detect_action_uuids())),
                Collections.unmodifiableList(copyStringVector(n.getAi_audio_file_md5s())),
                copyStringStringUnorderedMap(n.getAction_uuid_to_md5_map()),
                copyStringStringUnorderedMap(n.getAudio_action_uuid_to_file_path_map()),
                copyStringStringUnorderedMap(n.getAi_audio_action_uuid_to_file_path_map()));
    }

    public static ImportWaylineEventExtData toImportWaylineEventExtData(
            com.dji.flighthubsdk.generated.ImportWaylineEventExtData n) {
        if (n == null) {
            return ImportWaylineEventExtData.empty();
        }
        return new ImportWaylineEventExtData(
                safeString(n.getKmz_without_res_object_key()),
                safeString(n.getKmz_without_res_md5()),
                safeString(n.getTarget_area_file_object_key()),
                safeString(n.getBusiness_file_object_key()),
                copyStringVector(n.getModel_file_object_keys()));
    }

    public static ImportWaylineEventResult toImportWaylineEventResult(
            com.dji.flighthubsdk.generated.ImportWaylineEventResult n) {
        if (n == null) {
            return new ImportWaylineEventResult(
                    "",
                    0L,
                    "",
                    "",
                    0,
                    "",
                    ImportWaylineEventKmzData.empty(),
                    ImportWaylineEventExtData.empty());
        }
        return new ImportWaylineEventResult(
                safeString(n.getAsync_request_id()),
                n.getTimestamp(),
                safeString(n.getKmz_object_key()),
                safeString(n.getWayline_uuid()),
                n.getCode(),
                safeString(n.getMessage()),
                toImportWaylineEventKmzData(n.getKmz_data()),
                toImportWaylineEventExtData(n.getExt_data()));
    }

    public static UploadUnlockLicenseFileResult toUploadUnlockLicenseFileResult(
            com.dji.flighthubsdk.generated.UploadUnlockLicenseFileResult n) {
        if (n == null) {
            return new UploadUnlockLicenseFileResult("");
        }
        return new UploadUnlockLicenseFileResult(safeString(n.getFile_store_dir()));
    }

    public static BatchDeleteWaylinesResult toBatchDeleteWaylinesResult(
            com.dji.flighthubsdk.generated.BatchDeleteWaylinesResult n) {
        if (n == null) {
            return new BatchDeleteWaylinesResult(0, "");
        }
        return new BatchDeleteWaylinesResult(n.getServer_code(), safeString(n.getServer_message()));
    }

    public static UpdateWaylineResult toUpdateWaylineResult(
            com.dji.flighthubsdk.generated.UpdateWaylineResult n) {
        if (n == null) {
            return new UpdateWaylineResult("");
        }
        return new UpdateWaylineResult(safeString(n.getStatus()));
    }

    public static BatchDownloadWaylinesItemResult toBatchDownloadWaylinesItemResult(
            com.dji.flighthubsdk.generated.BatchDownloadWaylinesItemResult n) {
        if (n == null) {
            return new BatchDownloadWaylinesItemResult("", "", "", "", 0);
        }
        return new BatchDownloadWaylinesItemResult(
                safeString(n.getWayline_uuid()),
                safeString(n.getWayline_name()),
                safeString(n.getKmz_object_key()),
                safeString(n.getUrl()),
                n.getSize());
    }

    public static List<BatchDownloadWaylinesItemResult> toBatchDownloadWaylinesItemResultList(
            com.dji.flighthubsdk.generated.BatchDownloadWaylinesResult n) {
        if (n == null) {
            return Collections.emptyList();
        }
        com.dji.flighthubsdk.generated.BatchDownloadWaylinesItemResultVector v = n.getList();
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<BatchDownloadWaylinesItemResult> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toBatchDownloadWaylinesItemResult(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static DuplicateWaylineResult toDuplicateWaylineResult(
            com.dji.flighthubsdk.generated.DuplicateWaylineResult n) {
        if (n == null) {
            return new DuplicateWaylineResult("", "");
        }
        return new DuplicateWaylineResult(
                safeString(n.getWayline_uuid()), safeString(n.getWayline_name()));
    }

    // ---------- DeviceDebug 模块：简单 *Result → bean（类名与 generated 对齐）----------

    public static OpenDebugModeResult toOpenDebugModeResult(com.dji.flighthubsdk.generated.OpenDebugModeResult n) {
        if (n == null) {
            return new OpenDebugModeResult("", "");
        }
        return new OpenDebugModeResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static CloseDebugModeResult toCloseDebugModeResult(com.dji.flighthubsdk.generated.CloseDebugModeResult n) {
        if (n == null) {
            return new CloseDebugModeResult("", "");
        }
        return new CloseDebugModeResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static OpenDockCoverResult toOpenDockCoverResult(com.dji.flighthubsdk.generated.OpenDockCoverResult n) {
        if (n == null) {
            return new OpenDockCoverResult("", "");
        }
        return new OpenDockCoverResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static OpenDockCoverProgressResult toOpenDockCoverProgressResult(
            com.dji.flighthubsdk.generated.OpenDockCoverProgressResult n) {
        if (n == null) {
            return new OpenDockCoverProgressResult("", "", 0, 0);
        }
        return new OpenDockCoverProgressResult(
                safeString(n.getMessage()),
                safeString(n.getBid()),
                n.getCreate_time(),
                n.getUpdate_time());
    }

    public static CloseDockCoverResult toCloseDockCoverResult(com.dji.flighthubsdk.generated.CloseDockCoverResult n) {
        if (n == null) {
            return new CloseDockCoverResult("", "");
        }
        return new CloseDockCoverResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static CloseDockCoverProgressResult toCloseDockCoverProgressResult(
            com.dji.flighthubsdk.generated.CloseDockCoverProgressResult n) {
        if (n == null) {
            return new CloseDockCoverProgressResult("", "", 0, 0);
        }
        return new CloseDockCoverProgressResult(
                safeString(n.getMessage()),
                safeString(n.getBid()),
                n.getCreate_time(),
                n.getUpdate_time());
    }

    public static DeviceRebootResult toDeviceRebootResult(com.dji.flighthubsdk.generated.DeviceRebootResult n) {
        if (n == null) {
            return new DeviceRebootResult("", "");
        }
        return new DeviceRebootResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static DeviceRebootProgressResult toDeviceRebootProgressResult(
            com.dji.flighthubsdk.generated.DeviceRebootProgressResult n) {
        if (n == null) {
            return new DeviceRebootProgressResult("", "", 0, 0);
        }
        return new DeviceRebootProgressResult(
                safeString(n.getMessage()),
                safeString(n.getBid()),
                n.getCreate_time(),
                n.getUpdate_time());
    }

    public static DeviceFormatResult toDeviceFormatResult(com.dji.flighthubsdk.generated.DeviceFormatResult n) {
        if (n == null) {
            return new DeviceFormatResult("", "");
        }
        return new DeviceFormatResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static DeviceFormatProgressResult toDeviceFormatProgressResult(
            com.dji.flighthubsdk.generated.DeviceFormatProgressResult n) {
        if (n == null) {
            return new DeviceFormatProgressResult("", "", 0, 0);
        }
        return new DeviceFormatProgressResult(
                safeString(n.getMessage()),
                safeString(n.getBid()),
                n.getCreate_time(),
                n.getUpdate_time());
    }

    public static OpenDroneResult toOpenDroneResult(com.dji.flighthubsdk.generated.OpenDroneResult n) {
        if (n == null) {
            return new OpenDroneResult("", "");
        }
        return new OpenDroneResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static CloseDroneResult toCloseDroneResult(com.dji.flighthubsdk.generated.CloseDroneResult n) {
        if (n == null) {
            return new CloseDroneResult("", "");
        }
        return new CloseDroneResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static OpenChargeResult toOpenChargeResult(com.dji.flighthubsdk.generated.OpenChargeResult n) {
        if (n == null) {
            return new OpenChargeResult("", "");
        }
        return new OpenChargeResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static CloseChargeResult toCloseChargeResult(com.dji.flighthubsdk.generated.CloseChargeResult n) {
        if (n == null) {
            return new CloseChargeResult("", "");
        }
        return new CloseChargeResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static NetworkTestProgressResult toNetworkTestProgressResult(
            com.dji.flighthubsdk.generated.NetworkTestProgressResult n) {
        if (n == null) {
            return new NetworkTestProgressResult("", "", 0, 0, 0, 0);
        }
        return new NetworkTestProgressResult(
                safeString(n.getMessage()),
                safeString(n.getBid()),
                n.getCreate_time(),
                n.getUpdate_time(),
                n.getDownload_speed(),
                n.getUpload_speed());
    }

    public static RtkCalibrationResult toRtkCalibrationResult(
            com.dji.flighthubsdk.generated.RtkCalibrationResult n) {
        if (n == null) {
            return new RtkCalibrationResult("", "");
        }
        return new RtkCalibrationResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static SetDistanceLimitResult toSetDistanceLimitResult(
            com.dji.flighthubsdk.generated.SetDistanceLimitResult n) {
        if (n == null) {
            return new SetDistanceLimitResult("");
        }
        return new SetDistanceLimitResult(safeString(n.getMessage()));
    }

    public static SetHeightLimitResult toSetHeightLimitResult(com.dji.flighthubsdk.generated.SetHeightLimitResult n) {
        if (n == null) {
            return new SetHeightLimitResult("");
        }
        return new SetHeightLimitResult(safeString(n.getMessage()));
    }

    public static SetObstacleAvoidanceResult toSetObstacleAvoidanceResult(
            com.dji.flighthubsdk.generated.SetObstacleAvoidanceResult n) {
        if (n == null) {
            return new SetObstacleAvoidanceResult("");
        }
        return new SetObstacleAvoidanceResult(safeString(n.getMessage()));
    }

    public static SetRemainingPowerForRthResult toSetRemainingPowerForRthResult(
            com.dji.flighthubsdk.generated.SetRemainingPowerForRthResult n) {
        if (n == null) {
            return new SetRemainingPowerForRthResult("");
        }
        return new SetRemainingPowerForRthResult(safeString(n.getMessage()));
    }

    public static SetNightLightsStateResult toSetNightLightsStateResult(
            com.dji.flighthubsdk.generated.SetNightLightsStateResult n) {
        if (n == null) {
            return new SetNightLightsStateResult("");
        }
        return new SetNightLightsStateResult(safeString(n.getMessage()));
    }

    public static SwitchAlarmStateResult toSwitchAlarmStateResult(
            com.dji.flighthubsdk.generated.SwitchAlarmStateResult n) {
        if (n == null) {
            return new SwitchAlarmStateResult("");
        }
        return new SwitchAlarmStateResult(safeString(n.getMessage()));
    }

    public static EsimOperatorSwitchResult toEsimOperatorSwitchResult(
            com.dji.flighthubsdk.generated.EsimOperatorSwitchResult n) {
        if (n == null) {
            return new EsimOperatorSwitchResult("", "");
        }
        return new EsimOperatorSwitchResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static SimSlotSwitchResult toSimSlotSwitchResult(com.dji.flighthubsdk.generated.SimSlotSwitchResult n) {
        if (n == null) {
            return new SimSlotSwitchResult("", "");
        }
        return new SimSlotSwitchResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static SetBatteryStoreModeResult toSetBatteryStoreModeResult(
            com.dji.flighthubsdk.generated.SetBatteryStoreModeResult n) {
        if (n == null) {
            return new SetBatteryStoreModeResult("", "");
        }
        return new SetBatteryStoreModeResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static ManualBatterySwapResult toManualBatterySwapResult(
            com.dji.flighthubsdk.generated.ManualBatterySwapResult n) {
        if (n == null) {
            return new ManualBatterySwapResult("", "");
        }
        return new ManualBatterySwapResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static ManualBatterySwapProgressResult toManualBatterySwapProgressResult(
            com.dji.flighthubsdk.generated.ManualBatterySwapProgressResult n) {
        if (n == null) {
            return new ManualBatterySwapProgressResult("", "", "");
        }
        return new ManualBatterySwapProgressResult(safeString(n.getMessage()), safeString(n.getBid()), safeString(n.getData_json()));
    }

    public static DonglePowerSwitchResult toDonglePowerSwitchResult(
            com.dji.flighthubsdk.generated.DonglePowerSwitchResult n) {
        if (n == null) {
            return new DonglePowerSwitchResult("", "");
        }
        return new DonglePowerSwitchResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static LowPowerSwitchResult toLowPowerSwitchResult(
            com.dji.flighthubsdk.generated.LowPowerSwitchResult n) {
        if (n == null) {
            return new LowPowerSwitchResult("", "");
        }
        return new LowPowerSwitchResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    public static EsimOperatorSwitchProgressResult toEsimOperatorSwitchProgressResult(
            com.dji.flighthubsdk.generated.EsimOperatorSwitchProgressResult n) {
        if (n == null) {
            return new EsimOperatorSwitchProgressResult("", "", "");
        }
        return new EsimOperatorSwitchProgressResult(safeString(n.getMessage()), safeString(n.getBid()), safeString(n.getData_json()));
    }

    // 中继站调试接口（有 data：bid+status）
    public static com.dji.flighthubsdk.bean.SetRelayOccupiedStatusResult toSetRelayOccupiedStatusResult(
            com.dji.flighthubsdk.generated.SetRelayOccupiedStatusResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.SetRelayOccupiedStatusResult("", "");
        }
        return new com.dji.flighthubsdk.bean.SetRelayOccupiedStatusResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static com.dji.flighthubsdk.bean.RelayRebootResult toRelayRebootResult(
            com.dji.flighthubsdk.generated.RelayRebootResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.RelayRebootResult("", "");
        }
        return new com.dji.flighthubsdk.bean.RelayRebootResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static com.dji.flighthubsdk.bean.RelayDisconnectResult toRelayDisconnectResult(
            com.dji.flighthubsdk.generated.RelayDisconnectResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.RelayDisconnectResult("", "");
        }
        return new com.dji.flighthubsdk.bean.RelayDisconnectResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static DroneFormatResult toDroneFormatResult(com.dji.flighthubsdk.generated.DroneFormatResult n) {
        if (n == null) {
            return new DroneFormatResult("", "");
        }
        return new DroneFormatResult(safeString(n.getMessage()), safeString(n.getBid()));
    }

    // ---------- 媒体 / 安全 / 航线任务 / 飞行记录 / 日志 HMS（异步回调边界拷贝）----------

    private static List<Double> copyDoubleVector(com.dji.flighthubsdk.generated.DoubleVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(v));
    }

    private static List<List<List<Double>>> copyDoubleVectorVectorVector(
            com.dji.flighthubsdk.generated.DoubleVectorVectorVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<List<Double>>> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            com.dji.flighthubsdk.generated.DoubleVectorVector row = v.get(i);
            List<List<Double>> mid = new ArrayList<>();
            if (row != null) {
                for (int j = 0; j < row.size(); j++) {
                    mid.add(copyDoubleVector(row.get(j)));
                }
            }
            out.add(Collections.unmodifiableList(mid));
        }
        return Collections.unmodifiableList(out);
    }

    public static PositionData toPositionData(com.dji.flighthubsdk.generated.PositionData p) {
        if (p == null) {
            return new PositionData(0, 0, 0, 0);
        }
        return new PositionData(p.getLat(), p.getLng(), p.getAbs_alt(), p.getRel_alt());
    }

    public static MediaFileImageMetaData toMediaFileImageMetaData(
            com.dji.flighthubsdk.generated.MediaFileImageMetaData n) {
        if (n == null) {
            return null;
        }
        return new MediaFileImageMetaData(
                safeString(n.getPhoto_create_time()),
                n.getFlight_yaw(),
                n.getFocal_length(),
                n.getGimbal_pitch(),
                n.getGimbal_roll(),
                n.getGimbal_yaw(),
                n.getHeight(),
                n.getWidth(),
                n.getZoom_factor(),
                n.getDigital_zoom_ratio(),
                n.getLength_in_35mm_film(),
                safeString(n.getImage_type()),
                safeString(n.getPayload_type()),
                safeString(n.getAction_uuid()),
                toPositionData(n.getPhoto_position()),
                toPositionData(n.getShoot_position()));
    }

    public static MediaFileVideoMetaData toMediaFileVideoMetaData(
            com.dji.flighthubsdk.generated.MediaFileVideoMetaData n) {
        if (n == null) {
            return null;
        }
        return new MediaFileVideoMetaData(
                n.getDuration(),
                safeString(n.getEncoder()),
                safeString(n.getFormat_long_name()),
                safeString(n.getFormat_name()),
                n.getHeight(),
                n.getWidth());
    }

    public static MediaFilesListItemExtData toMediaFilesListItemExtData(
            com.dji.flighthubsdk.generated.MediaFilesListItemExtData n) {
        if (n == null) {
            return null;
        }
        return new MediaFilesListItemExtData(
                safeString(n.getCreate_time()),
                n.getFlight_yaw(),
                n.getFocal_length(),
                n.getGimbal_pitch(),
                n.getGimbal_roll(),
                n.getGimbal_yaw(),
                n.getWidth(),
                n.getHeight(),
                safeString(n.getImage_type()),
                toPositionData(n.getPhoto_position()),
                toPositionData(n.getShoot_position()),
                n.getZoom_factor());
    }

    public static MediaFilesListItemInfoData toMediaFilesListItemInfoData(
            com.dji.flighthubsdk.generated.MediaFilesListItemInfoData n) {
        if (n == null) {
            return null;
        }
        return new MediaFilesListItemInfoData(
                n.getFile_id(),
                safeString(n.getFlight_id()),
                safeString(n.getWayline_action_uuid()),
                n.getSize(),
                safeString(n.getObject_key()),
                safeString(n.getFile_name()),
                safeString(n.getSuffix()),
                toMediaFilesListItemExtData(n.getExt()),
                safeString(n.getCreated_at()),
                safeString(n.getFile_path()),
                safeString(n.getDevice_sn()),
                safeString(n.getPayload_type()),
                n.getType());
    }

    public static GetMediaFilesListResults toGetMediaFilesListResults(
            com.dji.flighthubsdk.generated.GetMediaFilesListResults n) {
        if (n == null) {
            return new GetMediaFilesListResults(Collections.emptyList(), 0, 0, 0);
        }
        com.dji.flighthubsdk.generated.MediaFilesListItemInfoDataVector v = n.getList();
        List<MediaFilesListItemInfoData> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toMediaFilesListItemInfoData(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new GetMediaFilesListResults(list, n.getPage_size(), n.getNext_index(), n.getTotal());
    }

    public static GetMediaFilesDownloadUrlResults toGetMediaFilesDownloadUrlResults(
            com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlResults n) {
        if (n == null) {
            return new GetMediaFilesDownloadUrlResults("");
        }
        return new GetMediaFilesDownloadUrlResults(safeString(n.getUrl()));
    }

    public static com.dji.flighthubsdk.bean.MediaFileDownloadInfo toMediaFileDownloadInfo(
            com.dji.flighthubsdk.generated.MediaFileDownloadInfo n) {
        if (n == null) {
            return null;
        }
        return new com.dji.flighthubsdk.bean.MediaFileDownloadInfo(
                safeString(n.getObject_key()),
                safeString(n.getUrl()));
    }

    public static com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults toGetMediaFilesDownloadUrlListResults(
            com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlListResults n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.MediaFileDownloadInfoVector v = n.getList();
        List<com.dji.flighthubsdk.bean.MediaFileDownloadInfo> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toMediaFileDownloadInfo(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults(list);
    }

    public static MediaFilePostProcess toMediaFilePostProcess(
            com.dji.flighthubsdk.generated.MediaFilePostProcess n) {
        if (n == null) {
            return null;
        }
        return new MediaFilePostProcess(
                n.getFile_id(),
                safeString(n.getFlight_id()),
                safeString(n.getDevice_sn()),
                safeString(n.getObject_key()),
                n.getObject_size(),
                n.getFile_type(),
                safeString(n.getPreview_object_key()),
                toMediaFileImageMetaData(n.getImage_metadata()),
                toMediaFileVideoMetaData(n.getVideo_metadata()));
    }

    public static MediaFileDeviceUpload toMediaFileDeviceUpload(
            com.dji.flighthubsdk.generated.MediaFileDeviceUpload n) {
        if (n == null) {
            return null;
        }
        return new MediaFileDeviceUpload(
                n.getFile_id(),
                safeString(n.getFlight_id()),
                safeString(n.getDevice_sn()),
                safeString(n.getObject_key()),
                n.getObject_size(),
                n.getFile_type());
    }

    public static MediaUploadStsCredentials toMediaUploadStsCredentials(
            com.dji.flighthubsdk.generated.MediaUploadStsCredentials n) {
        if (n == null) {
            return new MediaUploadStsCredentials("", "", 0, "");
        }
        return new MediaUploadStsCredentials(
                safeString(n.getAccess_key_id()),
                safeString(n.getAccess_key_secret()),
                n.getExpire(),
                safeString(n.getSecurity_token()));
    }

    public static GetMediaUploadStsResult toGetMediaUploadStsResult(
            com.dji.flighthubsdk.generated.GetMediaUploadStsResult n) {
        if (n == null) {
            return new GetMediaUploadStsResult("", null, "", "", "", "");
        }
        return new GetMediaUploadStsResult(
                safeString(n.getBucket()),
                toMediaUploadStsCredentials(n.getCredentials()),
                safeString(n.getObject_key_prefix()),
                safeString(n.getEndpoint()),
                safeString(n.getProvider()),
                safeString(n.getRegion()));
    }

    public static com.dji.flighthubsdk.generated.GetMediaUploadStsParams toNativeGetMediaUploadStsParams(
            GetMediaUploadStsParams params) {
        com.dji.flighthubsdk.generated.GetMediaUploadStsParams n =
                new com.dji.flighthubsdk.generated.GetMediaUploadStsParams();
        if (params == null) {
            return n;
        }
        n.setFile_path(params.getFilePath() != null ? params.getFilePath() : "");
        n.setExpire(params.getExpire());
        n.setSn(params.getSn() != null ? params.getSn() : "");
        n.setModule(params.getModule());
        return n;
    }

    public static MediaUploadCallbackShootPosition toMediaUploadCallbackShootPosition(
            com.dji.flighthubsdk.generated.MediaUploadCallbackShootPosition n) {
        if (n == null) {
            return new MediaUploadCallbackShootPosition(0, 0);
        }
        return new MediaUploadCallbackShootPosition(n.getLat(), n.getLng());
    }

    public static MediaUploadCallbackMetadata toMediaUploadCallbackMetadata(
            com.dji.flighthubsdk.generated.MediaUploadCallbackMetadata n) {
        if (n == null) {
            return null;
        }
        return new MediaUploadCallbackMetadata(
                n.getAbsolute_altitude(),
                n.getRelative_altitude(),
                n.getGimbal_yaw_degree(),
                safeString(n.getCreated_time()),
                toMediaUploadCallbackShootPosition(n.getShoot_position()));
    }

    public static MediaUploadCallbackFileExt toMediaUploadCallbackFileExt(
            com.dji.flighthubsdk.generated.MediaUploadCallbackFileExt n) {
        if (n == null) {
            return null;
        }
        return new MediaUploadCallbackFileExt(
                safeString(n.getPrj_uuid()),
                safeString(n.getWorkspace_id()),
                safeString(n.getFlight_id()),
                safeString(n.getDrone_model_key()),
                safeString(n.getPayload_model_key()),
                n.getIs_original(),
                safeString(n.getUser_id()),
                safeString(n.getWayline_id()),
                safeString(n.getTinny_fingerprint()),
                n.getFast_reconstruction_media_type(),
                n.getMedia_index());
    }

    public static MediaUploadCallbackFile toMediaUploadCallbackFile(
            com.dji.flighthubsdk.generated.MediaUploadCallbackFile n) {
        if (n == null) {
            return null;
        }
        return new MediaUploadCallbackFile(
                safeString(n.getObject_key()),
                safeString(n.getFingerprint()),
                safeString(n.getName()),
                n.getType(),
                safeString(n.getCloud_to_cloud_id()),
                toMediaUploadCallbackFileExt(n.getExt()),
                toMediaUploadCallbackMetadata(n.getMetadata()),
                safeString(n.getCreated_time()),
                safeString(n.getSource()));
    }

    public static MediaUploadCallbackResult toMediaUploadCallbackResult(
            com.dji.flighthubsdk.generated.MediaUploadCallbackResult n) {
        if (n == null) {
            return null;
        }
        return new MediaUploadCallbackResult(
                safeString(n.getObject_key()),
                safeString(n.getFilename()),
                safeString(n.getFlight_id()),
                safeString(n.getUuid()),
                n.getId(),
                n.getMedia_id());
    }

    public static com.dji.flighthubsdk.generated.MediaUploadCallbackParams toNativeMediaUploadCallbackParams(
            MediaUploadCallbackParams params) {
        com.dji.flighthubsdk.generated.MediaUploadCallbackParams n =
                new com.dji.flighthubsdk.generated.MediaUploadCallbackParams();
        if (params == null) {
            return n;
        }
        n.setSn(params.getSn() != null ? params.getSn() : "");
        n.setResult(params.getResult());
        n.setProgress(params.getProgress());
        n.setExpected_file_count(params.getExpectedFileCount());
        n.setUploaded_file_count(params.getUploadedFileCount());
        n.setFlight_type(params.getFlightType());
        n.setBiz_code(params.getBizCode() != null ? params.getBizCode() : "");
        n.setVersion(params.getVersion() != null ? params.getVersion() : "");
        if (params.getFile() != null) {
            MediaUploadCallbackFile file = params.getFile();
            com.dji.flighthubsdk.generated.MediaUploadCallbackFile nFile =
                    new com.dji.flighthubsdk.generated.MediaUploadCallbackFile();
            nFile.setObject_key(file.getObjectKey() != null ? file.getObjectKey() : "");
            nFile.setFingerprint(file.getFingerprint() != null ? file.getFingerprint() : "");
            nFile.setName(file.getName() != null ? file.getName() : "");
            nFile.setType(file.getType());
            nFile.setCloud_to_cloud_id(file.getCloudToCloudId() != null ? file.getCloudToCloudId() : "");
            nFile.setCreated_time(file.getCreatedTime() != null ? file.getCreatedTime() : "");
            nFile.setSource(file.getSource() != null ? file.getSource() : "");
            n.setFile(nFile);
        }
        return n;
    }

    public static GridSignalItem toGridSignalItem(com.dji.flighthubsdk.generated.GridSignalItem n) {
        if (n == null) {
            return new GridSignalItem("", 0, 0, 0, 0, 0, 0);
        }
        return new GridSignalItem(
                safeString(n.getH3_index()),
                n.getSdr_avg(),
                n.getSdr_max(),
                n.getSdr_min(),
                n.getSignal_4g_avg(),
                n.getSignal_4g_max(),
                n.getSignal_4g_min());
    }

    public static DeviceSignalItem toDeviceSignalItem(com.dji.flighthubsdk.generated.DeviceSignalItem n) {
        if (n == null) {
            return new DeviceSignalItem("", Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.GridSignalItemVector gv = n.getGrids();
        List<GridSignalItem> grids;
        if (gv == null || gv.isEmpty()) {
            grids = Collections.emptyList();
        } else {
            grids = new ArrayList<>(gv.size());
            for (int i = 0; i < gv.size(); i++) {
                grids.add(toGridSignalItem(gv.get(i)));
            }
            grids = Collections.unmodifiableList(grids);
        }
        return new DeviceSignalItem(safeString(n.getDevice_sn()), grids);
    }

    public static SignalMapsResults toSignalMapsResults(com.dji.flighthubsdk.generated.SignalMapsResults n) {
        if (n == null) {
            return new SignalMapsResults(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.DeviceSignalItemVector v = n.getLists();
        if (v == null || v.isEmpty()) {
            return new SignalMapsResults(Collections.emptyList());
        }
        List<DeviceSignalItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceSignalItem(v.get(i)));
        }
        return new SignalMapsResults(Collections.unmodifiableList(out));
    }

    public static ContentsPropertyData toContentsPropertyData(
            com.dji.flighthubsdk.generated.ContentsPropertyData n) {
        if (n == null) {
            return new ContentsPropertyData("", 0, false);
        }
        return new ContentsPropertyData(safeString(n.getColor()), n.getWidth(), n.getClampToGround());
    }

    public static ContentsGeometryData toContentsGeometryData(
            com.dji.flighthubsdk.generated.ContentsGeometryData n) {
        if (n == null) {
            return new ContentsGeometryData("", Collections.emptyList(), Collections.emptyList(), 0);
        }
        return new ContentsGeometryData(
                safeString(n.getType()),
                copyDoubleVectorVectorVector(n.getCoordinates()),
                copyDoubleVector(n.getCenter()),
                n.getRadius());
    }

    public static FlightAreasContentData toFlightAreasContentData(
            com.dji.flighthubsdk.generated.FlightAreasContentData n) {
        if (n == null) {
            return new FlightAreasContentData("", null, null);
        }
        return new FlightAreasContentData(
                safeString(n.getType()),
                toContentsPropertyData(n.getProperties()),
                toContentsGeometryData(n.getGeometry()));
    }

    public static FlightAreaInfoItem toFlightAreaInfoItem(com.dji.flighthubsdk.generated.FlightAreaInfoItem n) {
        if (n == null) {
            return new FlightAreaInfoItem("", "", "", "", null, 0, "", 0, "");
        }
        return new FlightAreaInfoItem(
                safeString(n.getId()),
                safeString(n.getName()),
                safeString(n.getStatus()),
                safeString(n.getType()),
                toFlightAreasContentData(n.getContent()),
                n.getCreated_time(),
                safeString(n.getCreated_by()),
                n.getUpdated_time(),
                safeString(n.getUpdated_by()));
    }

    public static FlightAreaPagination toFlightAreaPagination(
            com.dji.flighthubsdk.generated.FlightAreaPagination n) {
        if (n == null) {
            return new FlightAreaPagination(0, 0, 0);
        }
        return new FlightAreaPagination(n.getPage(), n.getPage_size(), n.getTotal());
    }

    public static GetFlightAreasResults toGetFlightAreasResults(
            com.dji.flighthubsdk.generated.GetFlightAreasResults n) {
        if (n == null) {
            return new GetFlightAreasResults(Collections.emptyList(), new FlightAreaPagination(0, 0, 0));
        }
        com.dji.flighthubsdk.generated.FlightAreaInfoItemVector v = n.getList();
        List<FlightAreaInfoItem> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toFlightAreaInfoItem(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new GetFlightAreasResults(list, toFlightAreaPagination(n.getPagination()));
    }

    public static CreateFlightAreasResults toCreateFlightAreasResults(
            com.dji.flighthubsdk.generated.CreateFlightAreasResults n) {
        if (n == null) {
            return new CreateFlightAreasResults("", "");
        }
        return new CreateFlightAreasResults(safeString(n.getId()), safeString(n.getName()));
    }

    public static UpdatedAreaData toUpdatedAreaData(com.dji.flighthubsdk.generated.UpdatedAreaData n) {
        if (n == null) {
            return new UpdatedAreaData("", "", "", "", null, 0, "", 0, "");
        }
        return new UpdatedAreaData(
                safeString(n.getId()),
                safeString(n.getName()),
                safeString(n.getStatus()),
                safeString(n.getType()),
                toFlightAreasContentData(n.getContent()),
                n.getCreated_time(),
                safeString(n.getCreated_by()),
                n.getUpdated_time(),
                safeString(n.getUpdated_by()));
    }

    public static UpdateFlightAreasResults toUpdateFlightAreasResults(
            com.dji.flighthubsdk.generated.UpdateFlightAreasResults n) {
        if (n == null) {
            return new UpdateFlightAreasResults("", null);
        }
        return new UpdateFlightAreasResults(safeString(n.getId()), toUpdatedAreaData(n.getUpdated_area()));
    }

    public static DeviceSyncResults toDeviceSyncResults(com.dji.flighthubsdk.generated.DeviceSyncResults n) {
        if (n == null) {
            return new DeviceSyncResults("", 0);
        }
        return new DeviceSyncResults(safeString(n.getDevice_sn()), n.getCode());
    }

    public static List<DeviceSyncResults> toDeviceSyncResultsList(
            com.dji.flighthubsdk.generated.DeviceSyncResultsVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceSyncResults> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceSyncResults(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static SyncFlightAreasResults toSyncFlightAreasResults(
            com.dji.flighthubsdk.generated.SyncFlightAreasResults n) {
        if (n == null) {
            return new SyncFlightAreasResults(Collections.emptyList());
        }
        return new SyncFlightAreasResults(toDeviceSyncResultsList(n.getResults()));
    }

    public static DeviceSyncStatusResults toDeviceSyncStatusResults(
            com.dji.flighthubsdk.generated.DeviceSyncStatusResults n) {
        if (n == null) {
            return new DeviceSyncStatusResults("", "", 0);
        }
        return new DeviceSyncStatusResults(safeString(n.getSn()), safeString(n.getStatus()), n.getReason());
    }

    public static List<DeviceSyncStatusResults> toDeviceSyncStatusResultsList(
            com.dji.flighthubsdk.generated.DeviceSyncStatusResultsVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceSyncStatusResults> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceSyncStatusResults(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static GetAreasSyncStatusResults toGetAreasSyncStatusResults(
            com.dji.flighthubsdk.generated.GetAreasSyncStatusResults n) {
        if (n == null) {
            return new GetAreasSyncStatusResults(Collections.emptyList());
        }
        return new GetAreasSyncStatusResults(toDeviceSyncStatusResultsList(n.getList()));
    }

    public static OfflineMapsSyncResults toOfflineMapsSyncResults(
            com.dji.flighthubsdk.generated.OfflineMapsSyncResults n) {
        if (n == null) {
            return new OfflineMapsSyncResults(Collections.emptyList());
        }
        return new OfflineMapsSyncResults(toDeviceSyncResultsList(n.getResults()));
    }

    public static GetOfflineMapsSyncStatusResults toGetOfflineMapsSyncStatusResults(
            com.dji.flighthubsdk.generated.GetOfflineMapsSyncStatusResults n) {
        if (n == null) {
            return new GetOfflineMapsSyncStatusResults(Collections.emptyList());
        }
        return new GetOfflineMapsSyncStatusResults(toDeviceSyncStatusResultsList(n.getList()));
    }

    public static OfflineMapsModelInfoData toOfflineMapsModelInfoData(
            com.dji.flighthubsdk.generated.OfflineMapsModelInfoData n) {
        if (n == null) {
            return new OfflineMapsModelInfoData("");
        }
        return new OfflineMapsModelInfoData(safeString(n.getUuid()));
    }

    public static OfflineMapsModelData toOfflineMapsModelData(
            com.dji.flighthubsdk.generated.OfflineMapsModelData n) {
        if (n == null) {
            return new OfflineMapsModelData(-1, 0, Collections.emptyList(), 0, "", 0);
        }
        com.dji.flighthubsdk.generated.OfflineMapsModelInfoDataVector mv = n.getModels();
        List<OfflineMapsModelInfoData> models;
        if (mv == null || mv.isEmpty()) {
            models = Collections.emptyList();
        } else {
            models = new ArrayList<>(mv.size());
            for (int i = 0; i < mv.size(); i++) {
                models.add(toOfflineMapsModelInfoData(mv.get(i)));
            }
            models = Collections.unmodifiableList(models);
        }
        return new OfflineMapsModelData(
                n.getId(),
                n.getUpdated_time(),
                models,
                n.getPercent(),
                safeString(n.getStatus()),
                n.getResult());
    }

    public static GetOfflineMapsModelInfoResults toGetOfflineMapsModelInfoResults(
            com.dji.flighthubsdk.generated.GetOfflineMapsModelInfoResults n) {
        if (n == null) {
            return new GetOfflineMapsModelInfoResults(false, null);
        }
        return new GetOfflineMapsModelInfoResults(n.getDisable(), toOfflineMapsModelData(n.getOffline_map()));
    }

    public static CommonPaginationData toCommonPaginationData(
            com.dji.flighthubsdk.generated.CommonPaginationData n) {
        if (n == null) {
            return new CommonPaginationData(0, 0, 0);
        }
        return new CommonPaginationData(n.getPage(), n.getPage_size(), n.getTotal());
    }

    public static FlightRecordExportProgressItemData toFlightRecordExportProgressItemData(
            com.dji.flighthubsdk.generated.FlightRecordExportProgressItemData n) {
        if (n == null) {
            return new FlightRecordExportProgressItemData(
                    -1, "", "", "", -1, -1, -1, "", "", "", "", Collections.emptyList(), "");
        }
        return new FlightRecordExportProgressItemData(
                n.getId(),
                safeString(n.getExport_id()),
                safeString(n.getCreated_at()),
                safeString(n.getContent_type()),
                n.getStatus(),
                n.getProgress(),
                n.getFailed_reason_code(),
                safeString(n.getObject_key()),
                safeString(n.getFile_name()),
                safeString(n.getUser_id()),
                safeString(n.getPrj_uuid()),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getFile_type()))),
                safeString(n.getDownload_url()));
    }

    public static ExportFlightRecordsResults toExportFlightRecordsResults(
            com.dji.flighthubsdk.generated.ExportFlightRecordsResults n) {
        if (n == null) {
            return new ExportFlightRecordsResults("");
        }
        return new ExportFlightRecordsResults(safeString(n.getExport_id()));
    }

    public static GetExportFlightRecordsProgressResults toGetExportFlightRecordsProgressResults(
            com.dji.flighthubsdk.generated.GetExportFlightRecordsProgressResults n) {
        if (n == null) {
            return new GetExportFlightRecordsProgressResults(
                    new CommonPaginationData(0, 0, 0), Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.FlightRecordExportProgressItemDataVector v = n.getList();
        List<FlightRecordExportProgressItemData> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toFlightRecordExportProgressItemData(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new GetExportFlightRecordsProgressResults(toCommonPaginationData(n.getPagination()), list);
    }

    public static PaginationInfo toTaskListPaginationInfo(
            com.dji.flighthubsdk.generated.PaginationInfo n) {
        if (n == null) {
            return new PaginationInfo(1, 10, 0);
        }
        return new PaginationInfo(n.getPage(), n.getPage_size(), n.getTotal());
    }

    public static TaskDetail toTaskDetail(com.dji.flighthubsdk.generated.TaskDetail n) {
        if (n == null) {
            return new TaskDetail(
                    "", "", 0, 0, "", "", "", "", "", 0, 0, 0, 0, 0, Collections.emptyList(), 0, 0, "", 0,
                    "", null, "", "", "", false, 0, Collections.emptyList(), "", Collections.emptyList(), "", 0);
        }
        List<WaylineObstacle> obstacles;
        com.dji.flighthubsdk.generated.WaylineObstacleVector obsVec = n.getObstacles();
        if (obsVec == null || obsVec.isEmpty()) {
            obstacles = Collections.emptyList();
        } else {
            obstacles = new ArrayList<>(obsVec.size());
            for (int i = 0; i < obsVec.size(); i++) {
                obstacles.add(toWaylineObstacle(obsVec.get(i)));
            }
            obstacles = Collections.unmodifiableList(obstacles);
        }
        return new TaskDetail(
                safeString(n.getUuid()),
                safeString(n.getName()),
                n.getTask_type(),
                n.getStatus(),
                safeString(n.getSn()),
                safeString(n.getLanding_dock_sn()),
                safeString(n.getWayline_uuid()),
                safeString(n.getBegin_at()),
                safeString(n.getEnd_at()),
                n.getOut_of_control_action_in_flight(),
                n.getRth_altitude(),
                n.getRth_mode(),
                n.getMin_battery_capacity(),
                n.getWayline_precision_type(),
                Collections.unmodifiableList(new ArrayList<>(copyIntVector(n.getWayline_segment_indexes()))),
                n.getCurrent_waypoint_index(),
                n.getTotal_waypoints(),
                safeString(n.getTime_zone()),
                n.getDevice_type(),
                safeString(n.getBid()),
                toWaylineBreakPoint(n.getBreak_point()),
                safeString(n.getCompleted_at()),
                safeString(n.getCreated_at()),
                safeString(n.getDeleted_at()),
                n.getIs_break_point_resume(),
                n.getMedia_upload_status(),
                obstacles,
                safeString(n.getRun_at()),
                Collections.unmodifiableList(new ArrayList<>(copyStringVector(n.getTags()))),
                safeString(n.getUpdated_at()),
                n.getWayline_duration());
    }

    public static FlightTaskListResult toFlightTaskListResult(com.dji.flighthubsdk.generated.FlightTaskListResult n) {
        if (n == null) {
            return new FlightTaskListResult(Collections.emptyList(), new PaginationInfo(0, 0, 0));
        }
        com.dji.flighthubsdk.generated.TaskDetailVector v = n.getList();
        List<TaskDetail> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toTaskDetail(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new FlightTaskListResult(list, toTaskListPaginationInfo(n.getPagination()));
    }

    public static GetCommandTaskListResults toGetCommandTaskListResults(com.dji.flighthubsdk.generated.GetCommandTaskListResults n) {
        if (n == null) {
            return new GetCommandTaskListResults(Collections.emptyList(), new PaginationInfo(0, 0, 0));
        }
        com.dji.flighthubsdk.generated.CommandTaskListItemDataVector v = n.getList();
        List<CommandTaskListItemData> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                com.dji.flighthubsdk.generated.CommandTaskListItemData item = v.get(i);
                list.add(new CommandTaskListItemData(
                        safeString(item.getUuid()),
                        safeString(item.getSn()),
                        safeString(item.getDrone_sn()),
                        item.getStatus(),
                        item.getErr_code(),
                        safeString(item.getTakeoff_mode()),
                        item.getSecurity_takeoff_height(),
                        item.getMax_speed(),
                        item.getRth_altitude(),
                        safeString(item.getCreated_at()),
                        safeString(item.getUser_id())));
            }
            list = Collections.unmodifiableList(list);
        }
        return new GetCommandTaskListResults(list, toTaskListPaginationInfo(n.getPagination()));
    }

    public static GetFlightTaskTrackResults toGetFlightTaskTrackResults(com.dji.flighthubsdk.generated.GetFlightTaskTrackResults n) {
        if (n == null) {
            return new GetFlightTaskTrackResults("", "", "", "", 0, "", null);
        }
        FlightTrackInfo trackInfo = null;
        com.dji.flighthubsdk.generated.FlightTrackInfo nTrack = n.getTrack();
        if (nTrack != null) {
            com.dji.flighthubsdk.generated.FlightTrackPointVector pv = nTrack.getPoints();
            java.util.List<FlightTrackPoint> points;
            if (pv == null || pv.isEmpty()) {
                points = Collections.emptyList();
            } else {
                points = new ArrayList<>(pv.size());
                for (int i = 0; i < pv.size(); i++) {
                    com.dji.flighthubsdk.generated.FlightTrackPoint p = pv.get(i);
                    points.add(new FlightTrackPoint(
                            p.getTimestamp(),
                            p.getLatitude(),
                            p.getLongitude(),
                            p.getHeight()));
                }
                points = Collections.unmodifiableList(points);
            }
            trackInfo = new FlightTrackInfo(
                    safeString(nTrack.getTrack_id()),
                    safeString(nTrack.getDrone_sn()),
                    nTrack.getFlight_distance(),
                    nTrack.getFlight_duration(),
                    points);
        }
        return new GetFlightTaskTrackResults(
                safeString(n.getUuid()),
                safeString(n.getRun_at()),
                safeString(n.getCompleted_at()),
                safeString(n.getName()),
                n.getStatus(),
                safeString(n.getWayline_uuid()),
                trackInfo);
    }

    public static CreateTaskResp toCreateTaskResp(
            com.dji.flighthubsdk.generated.CreateTaskResp n) {
        if (n == null) {
            return new CreateTaskResp("", Collections.emptyList());
        }
        return new CreateTaskResp(
                safeString(n.getUuid()),
                Collections.unmodifiableList(new ArrayList<>(copyIntVector(n.getWayline_validity_check_codes()))));
    }

    public static DeviceRecommendationResp toDeviceRecommendationResp(
            com.dji.flighthubsdk.generated.DeviceRecommendationResp n) {
        if (n == null) {
            return new DeviceRecommendationResp(new java.util.ArrayList<>());
        }
        java.util.List<RecommendedDevice> devices = new java.util.ArrayList<>();
        if (n.getDevices() != null) {
            for (int i = 0; i < n.getDevices().size(); i++) {
                com.dji.flighthubsdk.generated.RecommendedDevice d = n.getDevices().get(i);
                devices.add(new RecommendedDevice(
                        d.getDrone_sn(),
                        d.getGateway_sn(),
                        d.getDistance(),
                        d.getMatched()
                ));
            }
        }
        return new DeviceRecommendationResp(devices);
    }

    public static TCAInfo toTCAInfo(com.dji.flighthubsdk.generated.TCAInfo n) {
        if (n == null) {
            return new TCAInfo("", "");
        }
        return new TCAInfo(safeString(n.getCenter_sn()), safeString(n.getData_json()));
    }

    public static GetTCAInfosResp toGetTCAInfosResp(com.dji.flighthubsdk.generated.GetTCAInfosResp n) {
        if (n == null) {
            return new GetTCAInfosResp(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.TCAInfoVector v = n.getTcas();
        if (v == null || v.isEmpty()) {
            return new GetTCAInfosResp(Collections.emptyList());
        }
        List<TCAInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toTCAInfo(v.get(i)));
        }
        return new GetTCAInfosResp(Collections.unmodifiableList(out));
    }

    public static HistoryHmsDataResultArgs toHistoryHmsDataResultArgs(
            com.dji.flighthubsdk.generated.HistoryHmsDataResultArgs n) {
        if (n == null) {
            return new HistoryHmsDataResultArgs(0, 0, 0L, "", 0, 0, "", 0);
        }
        return new HistoryHmsDataResultArgs(
                n.getAlarm_info(),
                n.getComponent_index(),
                n.getCreate_timestamp(),
                safeString(n.getDevice_id()),
                n.getDisplay_scene(),
                n.getIgnore(),
                safeString(n.getSender_host_id()),
                n.getSensor_index());
    }

    public static HistoryHmsDataResultItem toHistoryHmsDataResultItem(
            com.dji.flighthubsdk.generated.HistoryHmsDataResultItem n) {
        if (n == null) {
            return new HistoryHmsDataResultItem(
                    "", "", "", 0, 0, "", 0, "", 0L, 0L, 0L, 0L, null, "", "", 0, 0, "", "", "");
        }
        return new HistoryHmsDataResultItem(
                safeString(n.get_id()),
                safeString(n.getBid()),
                safeString(n.getTid()),
                n.getLevel(),
                n.getModule(),
                safeString(n.getMessage()),
                n.getStatus(),
                safeString(n.getCode()),
                n.getDevice_data_create_time(),
                n.getDevice_data_update_time(),
                n.getCreate_time(),
                n.getUpdate_time(),
                toHistoryHmsDataResultArgs(n.getArgs()),
                safeString(n.getDevice_type()),
                safeString(n.getDomain_type()),
                n.getImminent(),
                n.getIn_the_sky(),
                safeString(n.getJoint_key()),
                safeString(n.getStatus_key()),
                safeString(n.getSn()));
    }

    public static HistoryHmsDataResultPagination toHistoryHmsDataResultPagination(
            com.dji.flighthubsdk.generated.HistoryHmsDataResultPagination n) {
        if (n == null) {
            return new HistoryHmsDataResultPagination(0, 0, 0);
        }
        return new HistoryHmsDataResultPagination(n.getPage_num(), n.getPage_size(), n.getTotal());
    }

    public static HistoryHmsDataResult toHistoryHmsDataResult(
            com.dji.flighthubsdk.generated.HistoryHmsDataResult n) {
        if (n == null) {
            return new HistoryHmsDataResult(new HistoryHmsDataResultPagination(0, 0, 0), Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.HistoryHmsDataResultItemVector v = n.getList();
        List<HistoryHmsDataResultItem> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toHistoryHmsDataResultItem(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new HistoryHmsDataResult(toHistoryHmsDataResultPagination(n.getPagination()), list);
    }

    public static DeviceLogListResultLogItem toDeviceLogListResultLogItem(
            com.dji.flighthubsdk.generated.DeviceLogListResultLogItem n) {
        if (n == null) {
            return new DeviceLogListResultLogItem(0, 0L, 0L, 0L, "");
        }
        return new DeviceLogListResultLogItem(
                n.getBoot_index(), n.getStart_time(), n.getEnd_time(), n.getSize(), safeString(n.getMd5()));
    }

    public static DeviceLogListResultFileItem toDeviceLogListResultFileItem(
            com.dji.flighthubsdk.generated.DeviceLogListResultFileItem n) {
        if (n == null) {
            return new DeviceLogListResultFileItem("", "", 0, Collections.emptyList(), "", 0, 0);
        }
        com.dji.flighthubsdk.generated.DeviceLogListResultLogItemVector lv = n.getList();
        List<DeviceLogListResultLogItem> logs;
        if (lv == null || lv.isEmpty()) {
            logs = Collections.emptyList();
        } else {
            logs = new ArrayList<>(lv.size());
            for (int i = 0; i < lv.size(); i++) {
                logs.add(toDeviceLogListResultLogItem(lv.get(i)));
            }
            logs = Collections.unmodifiableList(logs);
        }
        return new DeviceLogListResultFileItem(
                safeString(n.getDevice_sn()),
                safeString(n.getModule()),
                n.getResult(),
                logs,
                safeString(n.getObjectKey()),
                n.getLog_type(),
                n.getResponsibility_log_type());
    }

    public static DeviceLogListResult toDeviceLogListResult(
            com.dji.flighthubsdk.generated.DeviceLogListResult n) {
        if (n == null) {
            return new DeviceLogListResult(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.DeviceLogListResultFileItemVector v = n.getFiles();
        if (v == null || v.isEmpty()) {
            return new DeviceLogListResult(Collections.emptyList());
        }
        List<DeviceLogListResultFileItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceLogListResultFileItem(v.get(i)));
        }
        return new DeviceLogListResult(Collections.unmodifiableList(out));
    }

    public static CreateDeviceJobProgressResult toCreateDeviceJobProgressResult(
            com.dji.flighthubsdk.generated.CreateDeviceJobProgressResult n) {
        if (n == null) {
            return new CreateDeviceJobProgressResult(0, 0, "");
        }
        return new CreateDeviceJobProgressResult(n.getPercent(), n.getCurrent_step(), safeString(n.getExt()));
    }

    public static CreateDeviceJobDataResult toCreateDeviceJobDataResult(
            com.dji.flighthubsdk.generated.CreateDeviceJobDataResult n) {
        if (n == null) {
            return new CreateDeviceJobDataResult("", "", "", "", 0, null, "");
        }
        return new CreateDeviceJobDataResult(
                safeString(n.getBid()),
                safeString(n.getSn()),
                safeString(n.getBiz_code()),
                safeString(n.getStatus()),
                n.getResult(),
                toCreateDeviceJobProgressResult(n.getProgress()),
                safeString(n.getService_identifier()));
    }

    public static CreateDeviceLogReportResult toCreateDeviceLogReportResult(
            com.dji.flighthubsdk.generated.CreateDeviceLogReportResult n) {
        if (n == null) {
            return new CreateDeviceLogReportResult("", "", "", null);
        }
        return new CreateDeviceLogReportResult(
                safeString(n.getTrack_number()),
                safeString(n.getReport_uuid()),
                safeString(n.getError_log_list()),
                toCreateDeviceJobDataResult(n.getCreat_device_job_data()));
    }

    public static LogReportListResultDeviceTopoItem toLogReportListResultDeviceTopoItem(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceTopoItem n) {
        if (n == null) {
            return new LogReportListResultDeviceTopoItem("", "", "");
        }
        return new LogReportListResultDeviceTopoItem(
                safeString(n.getSn()),
                safeString(n.getDevice_model_key()),
                safeString(n.getDevice_organization_callsign()));
    }

    public static LogReportListResultDeviceTopo toLogReportListResultDeviceTopo(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceTopo n) {
        if (n == null) {
            return new LogReportListResultDeviceTopo(Collections.emptyList(), Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.LogReportListResultDeviceTopoItemVector hv = n.getHosts();
        com.dji.flighthubsdk.generated.LogReportListResultDeviceTopoItemVector pv = n.getParents();
        List<LogReportListResultDeviceTopoItem> hosts = mapTopoItems(hv);
        List<LogReportListResultDeviceTopoItem> parents = mapTopoItems(pv);
        return new LogReportListResultDeviceTopo(hosts, parents);
    }

    private static List<LogReportListResultDeviceTopoItem> mapTopoItems(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceTopoItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<LogReportListResultDeviceTopoItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLogReportListResultDeviceTopoItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static LogReportListResultAppendixFileItem toLogReportListResultAppendixFileItem(
            com.dji.flighthubsdk.generated.LogReportListResultAppendixFileItem n) {
        if (n == null) {
            return new LogReportListResultAppendixFileItem("", "", 0L);
        }
        return new LogReportListResultAppendixFileItem(
                safeString(n.getFile_name()), safeString(n.getFile_object_key()), n.getFile_size());
    }

    public static LogReportListResultAppendixFiles toLogReportListResultAppendixFiles(
            com.dji.flighthubsdk.generated.LogReportListResultAppendixFiles n) {
        if (n == null) {
            return new LogReportListResultAppendixFiles(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.LogReportListResultAppendixFileItemVector v = n.getFiles();
        if (v == null || v.isEmpty()) {
            return new LogReportListResultAppendixFiles(Collections.emptyList());
        }
        List<LogReportListResultAppendixFileItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLogReportListResultAppendixFileItem(v.get(i)));
        }
        return new LogReportListResultAppendixFiles(Collections.unmodifiableList(out));
    }

    public static LogReportListResultContactInformation toLogReportListResultContactInformation(
            com.dji.flighthubsdk.generated.LogReportListResultContactInformation n) {
        if (n == null) {
            return new LogReportListResultContactInformation("", "");
        }
        return new LogReportListResultContactInformation(safeString(n.getEmail()), safeString(n.getPhone()));
    }

    public static LogReportListResultPagination toLogReportListResultPagination(
            com.dji.flighthubsdk.generated.LogReportListResultPagination n) {
        if (n == null) {
            return new LogReportListResultPagination(0, 0, 0);
        }
        return new LogReportListResultPagination(n.getPage(), n.getPage_size(), n.getTotal());
    }

    public static LogReportListResultDeviceLogItemList toLogReportListResultDeviceLogItemList(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceLogItemList n) {
        if (n == null) {
            return new LogReportListResultDeviceLogItemList(0, 0L, 0L, 0L);
        }
        return new LogReportListResultDeviceLogItemList(
                n.getBoot_index(), n.getStart_time(), n.getEnd_time(), n.getSize());
    }

    public static LogReportListResultDeviceLog toLogReportListResultDeviceLog(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceLog n) {
        if (n == null) {
            return new LogReportListResultDeviceLog("", "", 0, Collections.emptyList(), "");
        }
        com.dji.flighthubsdk.generated.LogReportListResultDeviceLogItemListVector v = n.getList();
        List<LogReportListResultDeviceLogItemList> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toLogReportListResultDeviceLogItemList(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new LogReportListResultDeviceLog(
                safeString(n.getDevice_sn()),
                safeString(n.getModule()),
                n.getResult(),
                list,
                safeString(n.getObject_key()));
    }

    public static LogReportListResultDeviceLogs toLogReportListResultDeviceLogs(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceLogs n) {
        if (n == null) {
            return new LogReportListResultDeviceLogs(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.LogReportListResultDeviceLogVector v = n.getFiles();
        if (v == null || v.isEmpty()) {
            return new LogReportListResultDeviceLogs(Collections.emptyList());
        }
        List<LogReportListResultDeviceLog> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLogReportListResultDeviceLog(v.get(i)));
        }
        return new LogReportListResultDeviceLogs(Collections.unmodifiableList(out));
    }

    public static LogReportListResultDeviceUploadFilesItemProgress toLogReportListResultDeviceUploadFilesItemProgress(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceUploadFilesItemProgress n) {
        if (n == null) {
            return new LogReportListResultDeviceUploadFilesItemProgress(0, 0, 0, 0L, 0, 0, "");
        }
        return new LogReportListResultDeviceUploadFilesItemProgress(
                n.getCurrent_step(),
                n.getTotal_step(),
                n.getProgress(),
                n.getFinish_time(),
                n.getUpload_rate(),
                n.getResult(),
                safeString(n.getStatus()));
    }

    public static LogReportListResultDeviceUploadFilesItem toLogReportListResultDeviceUploadFilesItem(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceUploadFilesItem n) {
        if (n == null) {
            return new LogReportListResultDeviceUploadFilesItem("", 0L, "", "", "", null, "");
        }
        return new LogReportListResultDeviceUploadFilesItem(
                safeString(n.getModule()),
                n.getSize(),
                safeString(n.getDevice_sn()),
                safeString(n.getKey()),
                safeString(n.getFingerprint()),
                toLogReportListResultDeviceUploadFilesItemProgress(n.getProgress()),
                safeString(n.getDownload_url()));
    }

    public static LogReportListResultDeviceUploadFiles toLogReportListResultDeviceUploadFiles(
            com.dji.flighthubsdk.generated.LogReportListResultDeviceUploadFiles n) {
        if (n == null) {
            return new LogReportListResultDeviceUploadFiles(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.LogReportListResultDeviceUploadFilesItemVector v = n.getFiles();
        if (v == null || v.isEmpty()) {
            return new LogReportListResultDeviceUploadFiles(Collections.emptyList());
        }
        List<LogReportListResultDeviceUploadFilesItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLogReportListResultDeviceUploadFilesItem(v.get(i)));
        }
        return new LogReportListResultDeviceUploadFiles(Collections.unmodifiableList(out));
    }

    public static LogReportListResultItem toLogReportListResultItem(com.dji.flighthubsdk.generated.LogReportListResultItem n) {
        if (n == null) {
            return new LogReportListResultItem(
                    "", 0L, "", "", null, "", "", 0L, null, null, null, "", null, "", "", 0, "");
        }
        return new LogReportListResultItem(
                safeString(n.getReport_uuid()),
                n.getReport_time(),
                safeString(n.getReport_user_id()),
                safeString(n.getReport_user_organization_callsign()),
                toLogReportListResultDeviceTopo(n.getDevice_topo()),
                safeString(n.getReport_information()),
                safeString(n.getTrack_number()),
                n.getError_start_time(),
                toLogReportListResultAppendixFiles(n.getAppendix_files()),
                toLogReportListResultDeviceLogs(n.getDevice_logs()),
                toLogReportListResultDeviceUploadFiles(n.getDevice_upload_files()),
                safeString(n.getStatus()),
                toLogReportListResultContactInformation(n.getReport_contact_information()),
                safeString(n.getBid()),
                safeString(n.getDevice_report_feedback()),
                n.getRead_status(),
                safeString(n.getUrl()));
    }

    public static LogReportListResult toLogReportListResult(
            com.dji.flighthubsdk.generated.LogReportListResult n) {
        if (n == null) {
            return new LogReportListResult(new LogReportListResultPagination(0, 0, 0), Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.LogReportListResultItemVector v = n.getList();
        List<LogReportListResultItem> list;
        if (v == null || v.isEmpty()) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                list.add(toLogReportListResultItem(v.get(i)));
            }
            list = Collections.unmodifiableList(list);
        }
        return new LogReportListResult(toLogReportListResultPagination(n.getPagination()), list);
    }

    private static com.dji.flighthubsdk.generated.StringVector listToStringVector(List<String> values) {
        com.dji.flighthubsdk.generated.StringVector vector = new com.dji.flighthubsdk.generated.StringVector();
        if (values == null) {
            return vector;
        }
        for (String value : values) {
            vector.add(value != null ? value : "");
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.StringVector toNativeStringVector(List<String> values) {
        return listToStringVector(values);
    }

    public static com.dji.flighthubsdk.generated.IntVector toNativeIntVector(List<Integer> values) {
        com.dji.flighthubsdk.generated.IntVector vector = new com.dji.flighthubsdk.generated.IntVector();
        if (values == null) {
            return vector;
        }
        for (Integer value : values) {
            vector.add(value != null ? value : 0);
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.LongLongVector toNativeLongLongVector(List<Long> values) {
        com.dji.flighthubsdk.generated.LongLongVector vector = new com.dji.flighthubsdk.generated.LongLongVector();
        if (values == null) {
            return vector;
        }
        for (Long value : values) {
            vector.add(value != null ? value : 0L);
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.TakeoffParams toNativeTakeoffParams(
            com.dji.flighthubsdk.bean.TakeoffParams param) {
        com.dji.flighthubsdk.generated.TakeoffParams nativeParam = new com.dji.flighthubsdk.generated.TakeoffParams();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setSecurity_takeoff_height(param.getSecurityTakeoffHeight());
        nativeParam.setMax_speed(param.getMaxSpeed());
        nativeParam.setOut_of_control_action(
                param.getOutOfControlAction() != null
                        ? param.getOutOfControlAction().getValue()
                        : OutOfControlAction.UNKNOWN.getValue());
        nativeParam.setRth_altitude(param.getRthAltitude());
        nativeParam.setTarget_height(param.getTargetHeight());
        nativeParam.setTarget_latitude(param.getTargetLatitude());
        nativeParam.setTarget_longitude(param.getTargetLongitude());
        nativeParam.setTakeoff_mode(
                param.getTakeoffMode() != null
                        ? param.getTakeoffMode().getValue()
                        : TakeoffMode.UNKNOWN.getValue());
        nativeParam.setCommander_flight_mode(param.getCommanderFlightMode());
        nativeParam.setCommander_flight_height(param.getCommanderFlightHeight());
        nativeParam.setRth_mode(param.getRthMode());
        nativeParam.setCloud_to_cloud_id(
                param.getCloudToCloudId() != null ? param.getCloudToCloudId() : "");
        nativeParam.setNeed_verify_flight_control(param.getNeedVerifyFlightControl());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.ThermalGainModeSetParams toNativeThermalGainModeSetParams(
            com.dji.flighthubsdk.bean.ThermalGainModeSetParams param) {
        com.dji.flighthubsdk.generated.ThermalGainModeSetParams nativeParam =
                new com.dji.flighthubsdk.generated.ThermalGainModeSetParams();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setPayload_index(param.getPayloadIndex());
        nativeParam.setThermal_gain_mode(
                param.getThermalGainMode() != null ? param.getThermalGainMode().getValue() : IrGainMode.Auto.getValue());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.BatchUpdateStatusReq toNativeBatchUpdateStatusReq(
            com.dji.flighthubsdk.bean.BatchUpdateStatusReq req) {
        com.dji.flighthubsdk.generated.BatchUpdateStatusReq nativeReq =
                new com.dji.flighthubsdk.generated.BatchUpdateStatusReq();
        if (req == null) {
            return nativeReq;
        }
        nativeReq.setTask_uuids(toNativeStringVector(req.getTaskUuids()));
        nativeReq.setStatus(req.getStatus());
        return nativeReq;
    }

    public static com.dji.flighthubsdk.generated.DeviceRecommendationReq toNativeDeviceRecommendationReq(
            com.dji.flighthubsdk.bean.DeviceRecommendationReq req) {
        com.dji.flighthubsdk.generated.DeviceRecommendationReq nativeReq =
                new com.dji.flighthubsdk.generated.DeviceRecommendationReq();
        if (req == null) {
            return nativeReq;
        }
        nativeReq.setSn(toNativeStringVector(req.getSn()));
        nativeReq.setStrategy_id(req.getStrategyId());
        if (req.isHasFlyToPosition()) {
            com.dji.flighthubsdk.generated.DeviceRecommendationPosition flyToPosition =
                    new com.dji.flighthubsdk.generated.DeviceRecommendationPosition();
            flyToPosition.setLatitude(req.getFlyToLatitude());
            flyToPosition.setLongitude(req.getFlyToLongitude());
            flyToPosition.setHeight(req.getFlyToHeight());
            nativeReq.setFly_to_position(flyToPosition);
        }
        return nativeReq;
    }

    public static com.dji.flighthubsdk.generated.ModelItemInfoData toNativeModelItemInfoData(
            com.dji.flighthubsdk.bean.ModelItemInfoData item) {
        com.dji.flighthubsdk.generated.ModelItemInfoData nativeItem =
                new com.dji.flighthubsdk.generated.ModelItemInfoData();
        if (item == null) {
            return nativeItem;
        }
        nativeItem.setUuid(item.getUuid());
        nativeItem.setObject_key(item.getObjectKey());
        nativeItem.setFile_type(item.getFileType());
        return nativeItem;
    }

    public static com.dji.flighthubsdk.generated.ModelItemInfoDataVector toNativeModelItemInfoDataVector(
            List<com.dji.flighthubsdk.bean.ModelItemInfoData> items) {
        com.dji.flighthubsdk.generated.ModelItemInfoDataVector vector =
                new com.dji.flighthubsdk.generated.ModelItemInfoDataVector();
        if (items == null) {
            return vector;
        }
        for (com.dji.flighthubsdk.bean.ModelItemInfoData item : items) {
            vector.add(toNativeModelItemInfoData(item));
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.UpdateOfflineMapsParams toNativeUpdateOfflineMapsParams(
            com.dji.flighthubsdk.bean.UpdateOfflineMapsParams params) {
        com.dji.flighthubsdk.generated.UpdateOfflineMapsParams nativeParams =
                new com.dji.flighthubsdk.generated.UpdateOfflineMapsParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setModel_3d_items(toNativeModelItemInfoDataVector(params.getModel3dItems()));
        nativeParams.setModel_2d_items(toNativeModelItemInfoDataVector(params.getModel2dItems()));
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SetOfflineMapsParams toNativeSetOfflineMapsParams(
            com.dji.flighthubsdk.bean.SetOfflineMapsParams params) {
        com.dji.flighthubsdk.generated.SetOfflineMapsParams nativeParams =
                new com.dji.flighthubsdk.generated.SetOfflineMapsParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setOffline_map_enable(params.isOfflineMapEnable());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.FlightTaskListParams toNativeFlightTaskListParams(
            com.dji.flighthubsdk.bean.FlightTaskListParams params) {
        com.dji.flighthubsdk.generated.FlightTaskListParams nativeParams =
                new com.dji.flighthubsdk.generated.FlightTaskListParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPage(params.getPage());
        nativeParams.setPage_size(params.getPageSize());
        nativeParams.setSn(toNativeStringVector(params.getSn()));
        nativeParams.setName(params.getName());
        nativeParams.setStatus(toNativeIntVector(params.getStatus()));
        nativeParams.setTask_type(params.getTaskType());
        nativeParams.setMedia_upload_type(params.getMediaUploadType());
        if (params.getBeginAt() != null) nativeParams.setBegin_at(String.valueOf(params.getBeginAt()));
        if (params.getEndAt() != null) nativeParams.setEnd_at(String.valueOf(params.getEndAt()));
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.GetCommandTaskListParams toNativeGetCommandTaskListParams(
            com.dji.flighthubsdk.bean.GetCommandTaskListParams params) {
        com.dji.flighthubsdk.generated.GetCommandTaskListParams nativeParams =
                new com.dji.flighthubsdk.generated.GetCommandTaskListParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPage(params.getPage());
        nativeParams.setPage_size(params.getPageSize());
        nativeParams.setSn(params.getSn());
        nativeParams.setDrone_sn(params.getDroneSn());
        nativeParams.setBegin_at(params.getBeginAt());
        nativeParams.setEnd_at(params.getEndAt());
        nativeParams.setStatus(toNativeIntVector(params.getStatus()));
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.GetFlightTaskTrackParams toNativeGetFlightTaskTrackParams(
            com.dji.flighthubsdk.bean.GetFlightTaskTrackParams params) {
        com.dji.flighthubsdk.generated.GetFlightTaskTrackParams nativeParams =
                new com.dji.flighthubsdk.generated.GetFlightTaskTrackParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setTask_uuid(params.getTaskUuid());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.CreateTaskReq toNativeCreateTaskReq(
            com.dji.flighthubsdk.bean.CreateTaskReq req) {
        com.dji.flighthubsdk.generated.CreateTaskReq nativeReq =
                new com.dji.flighthubsdk.generated.CreateTaskReq();
        if (req == null) {
            return nativeReq;
        }
        nativeReq.setName(req.getName());
        nativeReq.setSn(req.getSn());
        nativeReq.setTask_type(req.getTaskType());
        nativeReq.setTime_zone(req.getTimeZone());
        nativeReq.setWayline_uuid(req.getWaylineUuid());
        nativeReq.setOut_of_control_action_in_flight(req.getOutOfControlActionInFlight());
        nativeReq.setRth_altitude(req.getRthAltitude());
        nativeReq.setRth_mode(req.getRthMode());
        if (req.getBeginAt() != null) {
            nativeReq.setBegin_at(req.getBeginAt());
        }
        if (req.getEndAt() != null) {
            nativeReq.setEnd_at(req.getEndAt());
        }
        nativeReq.setDevice_type(req.getDeviceType());
        nativeReq.setLanding_dock_sn(req.getLandingDockSn());
        nativeReq.setMin_battery_capacity(req.getMinBatteryCapacity());
        nativeReq.setWayline_precision_type(req.getWaylinePrecisionType());
        nativeReq.setWayline_segment_indexes(toNativeIntVector(req.getWaylineSegmentIndexes()));
        nativeReq.setWayline_duration(req.getWaylineDuration());
        nativeReq.setUuid(req.getUuid());
        nativeReq.setTags(toNativeStringVector(req.getTags()));
        nativeReq.setIs_break_point_resume(req.isBreakPointResume());
        if (req.getBreakPoint() != null) {
            nativeReq.setBreak_point(toNativeWaylineBreakPoint(req.getBreakPoint()));
        }
        nativeReq.setSignal_type(req.getSignalType());
        if (req.getResumableStatus() != null && !req.getResumableStatus().isEmpty()) {
            nativeReq.setResumable_status(req.getResumableStatus());
        }
        return nativeReq;
    }

    public static com.dji.flighthubsdk.generated.StartRoamingParams toNativeStartRoamingParams(
            com.dji.flighthubsdk.bean.StartRoamingParams params) {
        com.dji.flighthubsdk.generated.StartRoamingParams nativeParams =
                new com.dji.flighthubsdk.generated.StartRoamingParams();
        if (params != null) {
            nativeParams.setDrone_sn(params.getDroneSn());
            nativeParams.setFlight_id(params.getFlightId());
            nativeParams.setSource_task_type(params.getSourceTaskType());
        }
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SetRoamingStationSnParams toNativeSetRoamingStationSnParams(
            com.dji.flighthubsdk.bean.SetRoamingStationSnParams params) {
        com.dji.flighthubsdk.generated.SetRoamingStationSnParams nativeParams =
                new com.dji.flighthubsdk.generated.SetRoamingStationSnParams();
        if (params != null) {
            nativeParams.setDrone_sn(params.getDroneSn());
            nativeParams.setStation_sns(toNativeStringVector(params.getStationSns()));
        }
        return nativeParams;
    }

    public static com.dji.flighthubsdk.bean.SetRoamingStationSnResult toSetRoamingStationSnResult(
            com.dji.flighthubsdk.generated.SetRoamingStationSnResult nativeResult) {
        java.util.List<com.dji.flighthubsdk.bean.RoamingStationSnResult> results =
                new java.util.ArrayList<>();
        if (nativeResult != null && nativeResult.getResults() != null) {
            com.dji.flighthubsdk.generated.RoamingStationSnResultVector nativeResults =
                    nativeResult.getResults();
            for (int i = 0; i < nativeResults.size(); i++) {
                com.dji.flighthubsdk.generated.RoamingStationSnResult item = nativeResults.get(i);
                results.add(new com.dji.flighthubsdk.bean.RoamingStationSnResult(
                        safeString(item.getStation_sn()), item.getSuccess(),
                        item.getFail_reason().swigValue()));
            }
        }
        return new com.dji.flighthubsdk.bean.SetRoamingStationSnResult(results);
    }

    public static com.dji.flighthubsdk.bean.GetInFlightTaskToposResult toGetInFlightTaskToposResult(
            com.dji.flighthubsdk.generated.GetInFlightTaskToposResult nativeResult) {
        java.util.List<com.dji.flighthubsdk.bean.InFlightTaskTopoItem> list =
                new java.util.ArrayList<>();
        if (nativeResult != null && nativeResult.getList() != null) {
            com.dji.flighthubsdk.generated.InFlightTaskTopoItemVector nativeList =
                    nativeResult.getList();
            for (int i = 0; i < nativeList.size(); i++) {
                com.dji.flighthubsdk.generated.InFlightTaskTopoItem item = nativeList.get(i);
                list.add(new com.dji.flighthubsdk.bean.InFlightTaskTopoItem(
                        safeString(item.getDrone_sn()), safeString(item.getFlight_id()),
                        safeString(item.getTakeoff_sn())));
            }
        }
        return new com.dji.flighthubsdk.bean.GetInFlightTaskToposResult(list);
    }

    public static com.dji.flighthubsdk.bean.TaskTopoChangedEvent toTaskTopoChangedEvent(
            com.dji.flighthubsdk.generated.TaskTopoChangedEvent event) {
        if (event == null) {
            return null;
        }
        return new com.dji.flighthubsdk.bean.TaskTopoChangedEvent(
                safeString(event.getDrone_sn()), safeString(event.getFlight_id()),
                safeString(event.getTakeoff_sn()));
    }

    public static com.dji.flighthubsdk.generated.EditTaskReq toNativeEditTaskReq(
            com.dji.flighthubsdk.bean.EditTaskReq req) {
        com.dji.flighthubsdk.generated.EditTaskReq nativeReq =
                new com.dji.flighthubsdk.generated.EditTaskReq();
        if (req == null) {
            return nativeReq;
        }
        nativeReq.setUuid(req.getUuid());
        nativeReq.setName(req.getName());
        nativeReq.setTask_type(req.getTaskType());
        nativeReq.setTime_zone(req.getTimeZone());
        nativeReq.setSn(req.getSn());
        nativeReq.setLanding_dock_sn(req.getLandingDockSn());
        nativeReq.setWayline_uuid(req.getWaylineUuid());
        nativeReq.setWayline_segment_indexes(toNativeIntVector(req.getWaylineSegmentIndexes()));
        nativeReq.setWayline_precision_type(req.getWaylinePrecisionType());
        nativeReq.setOut_of_control_action_in_flight(req.getOutOfControlActionInFlight());
        nativeReq.setRth_altitude(req.getRthAltitude());
        nativeReq.setRth_mode(req.getRthMode());
        if (req.getBeginAt() != null) {
            nativeReq.setBegin_at(req.getBeginAt());
        }
        if (req.getEndAt() != null) {
            nativeReq.setEnd_at(req.getEndAt());
        }
        nativeReq.setMin_battery_capacity(req.getMinBatteryCapacity());
        nativeReq.setIs_break_point_resume(req.isBreakPointResume());
        nativeReq.setTags(toNativeStringVector(req.getTags()));
        if (req.getBreakPoint() != null) {
            nativeReq.setBreak_point(toNativeWaylineBreakPoint(req.getBreakPoint()));
        }
        return nativeReq;
    }

    private static com.dji.flighthubsdk.generated.WaylineBreakPoint toNativeWaylineBreakPoint(
            com.dji.flighthubsdk.bean.WaylineBreakPoint bp) {
        com.dji.flighthubsdk.generated.WaylineBreakPoint n =
                new com.dji.flighthubsdk.generated.WaylineBreakPoint();
        if (bp == null) {
            return n;
        }
        n.setIndex(bp.getIndex());
        n.setState(bp.getState());
        n.setProgress(bp.getProgress());
        n.setWayline_id(bp.getWaylineId());
        n.setBreak_reason(bp.getBreakReason());
        n.setLatitude(bp.getLatitude());
        n.setLongitude(bp.getLongitude());
        n.setHeight(bp.getHeight());
        n.setAttitude_head(bp.getAttitudeHead());
        return n;
    }

    private static com.dji.flighthubsdk.generated.DoubleVector toNativeDoubleVector(List<Double> values) {
        com.dji.flighthubsdk.generated.DoubleVector vector = new com.dji.flighthubsdk.generated.DoubleVector();
        if (values == null) {
            return vector;
        }
        for (Double value : values) {
            vector.add(value != null ? value : 0.0);
        }
        return vector;
    }

    private static com.dji.flighthubsdk.generated.DoubleVectorVectorVector toNativeDoubleVectorVectorVector(
            List<List<List<Double>>> values) {
        com.dji.flighthubsdk.generated.DoubleVectorVectorVector out =
                new com.dji.flighthubsdk.generated.DoubleVectorVectorVector();
        if (values == null) {
            return out;
        }
        for (List<List<Double>> l2 : values) {
            com.dji.flighthubsdk.generated.DoubleVectorVector mid =
                    new com.dji.flighthubsdk.generated.DoubleVectorVector();
            if (l2 != null) {
                for (List<Double> l1 : l2) {
                    mid.add(toNativeDoubleVector(l1));
                }
            }
            out.add(mid);
        }
        return out;
    }

    public static com.dji.flighthubsdk.generated.ContentsPropertyData toNativeContentsPropertyData(
            com.dji.flighthubsdk.bean.ContentsPropertyData data) {
        com.dji.flighthubsdk.generated.ContentsPropertyData nativeData =
                new com.dji.flighthubsdk.generated.ContentsPropertyData();
        if (data == null) {
            return nativeData;
        }
        nativeData.setColor(data.getColor());
        nativeData.setWidth(data.getWidth());
        nativeData.setClampToGround(data.isClampToGround());
        return nativeData;
    }

    public static com.dji.flighthubsdk.generated.ContentsGeometryData toNativeContentsGeometryData(
            com.dji.flighthubsdk.bean.ContentsGeometryData data) {
        com.dji.flighthubsdk.generated.ContentsGeometryData nativeData =
                new com.dji.flighthubsdk.generated.ContentsGeometryData();
        if (data == null) {
            return nativeData;
        }
        nativeData.setType(data.getType());
        nativeData.setCoordinates(toNativeDoubleVectorVectorVector(data.getCoordinates()));
        nativeData.setCenter(toNativeDoubleVector(data.getCenter()));
        nativeData.setRadius(data.getRadius());
        return nativeData;
    }

    public static com.dji.flighthubsdk.generated.FlightAreasContentData toNativeFlightAreasContentData(
            com.dji.flighthubsdk.bean.FlightAreasContentData data) {
        com.dji.flighthubsdk.generated.FlightAreasContentData nativeData =
                new com.dji.flighthubsdk.generated.FlightAreasContentData();
        if (data == null) {
            return nativeData;
        }
        nativeData.setType(data.getType());
        nativeData.setProperties(toNativeContentsPropertyData(data.getProperties()));
        nativeData.setGeometry(toNativeContentsGeometryData(data.getGeometry()));
        return nativeData;
    }

    public static com.dji.flighthubsdk.generated.CreateCustomizedFlightAreasParams toNativeCreateCustomizedFlightAreasParams(
            com.dji.flighthubsdk.bean.CreateCustomizedFlightAreasParams params) {
        com.dji.flighthubsdk.generated.CreateCustomizedFlightAreasParams nativeParams =
                new com.dji.flighthubsdk.generated.CreateCustomizedFlightAreasParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setName(params.getName());
        nativeParams.setType(params.getType());
        nativeParams.setContent(toNativeFlightAreasContentData(params.getContent()));
        nativeParams.setCreated_by(params.getCreatedBy());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.GetCustomizedFlightAreasParams toNativeGetCustomizedFlightAreasParams(
            com.dji.flighthubsdk.bean.GetCustomizedFlightAreasParams params) {
        com.dji.flighthubsdk.generated.GetCustomizedFlightAreasParams nativeParams =
                new com.dji.flighthubsdk.generated.GetCustomizedFlightAreasParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setName(params.getName());
        nativeParams.setType(params.getType());
        nativeParams.setStatus(params.getStatus());
        nativeParams.setIds(toNativeStringVector(params.getIds()));
        nativeParams.setPage_index(params.getPageIndex());
        nativeParams.setPage_size(params.getPageSize());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.UpdateCustomizedFlightAreasParams toNativeUpdateCustomizedFlightAreasParams(
            com.dji.flighthubsdk.bean.UpdateCustomizedFlightAreasParams params) {
        com.dji.flighthubsdk.generated.UpdateCustomizedFlightAreasParams nativeParams =
                new com.dji.flighthubsdk.generated.UpdateCustomizedFlightAreasParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setFlight_area_id(params.getFlightAreaId());
        nativeParams.setName(params.getName());
        nativeParams.setContent(toNativeFlightAreasContentData(params.getContent()));
        nativeParams.setUpdate_by(params.getUpdateBy());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.Waypoint toNativeWaypoint(
            com.dji.flighthubsdk.bean.Waypoint waypoint) {
        com.dji.flighthubsdk.generated.Waypoint nativeWaypoint = new com.dji.flighthubsdk.generated.Waypoint();
        if (waypoint == null) {
            return nativeWaypoint;
        }
        nativeWaypoint.setLatitude(waypoint.getLatitude());
        nativeWaypoint.setLongitude(waypoint.getLongitude());
        nativeWaypoint.setHeight(waypoint.getHeight());
        return nativeWaypoint;
    }

    public static com.dji.flighthubsdk.generated.POIEnterParams toNativePOIEnterParams(
            com.dji.flighthubsdk.bean.POIEnterParams params) {
        com.dji.flighthubsdk.generated.POIEnterParams nativeParams =
                new com.dji.flighthubsdk.generated.POIEnterParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(params.getPayloadIndex());
        nativeParams.setPoi_center_point(toNativeWaypoint(params.getPoiCenterPoint()));
        nativeParams.setSpeed(params.getSpeed());
        nativeParams.setCircle_radius(params.getCircleRadius());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.GetMediaFilesListParams toNativeGetMediaFilesListParams(
            com.dji.flighthubsdk.bean.GetMediaFilesListParams params) {
        com.dji.flighthubsdk.generated.GetMediaFilesListParams nativeParams =
                new com.dji.flighthubsdk.generated.GetMediaFilesListParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setFlight_id(params.getFlightId());
        nativeParams.setStart_index(params.getStartIndex());
        nativeParams.setPage_size(params.getPageSize());
        nativeParams.setFile_types(toNativeStringVector(params.getFileTypes()));
        nativeParams.setDevice_sn(params.getDeviceSn());
        nativeParams.setStart_create_time(params.getStartCreateTime());
        nativeParams.setEnd_create_time(params.getEndCreateTime());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.FlyToPointParams toNativeFlyToPointParams(
            com.dji.flighthubsdk.bean.FlyToPointParams params) {
        com.dji.flighthubsdk.generated.FlyToPointParams nativeParams =
                new com.dji.flighthubsdk.generated.FlyToPointParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setLatitude(params.getLatitude());
        nativeParams.setLongitude(params.getLongitude());
        nativeParams.setHeight(params.getHeight());
        nativeParams.setMax_speed(params.getMaxSpeed());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.NetworkRtkCalibrationParams toNativeNetworkRtkCalibrationParams(
            com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams params) {
        com.dji.flighthubsdk.generated.NetworkRtkCalibrationParams nativeParams =
                new com.dji.flighthubsdk.generated.NetworkRtkCalibrationParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setHost(params.getHost());
        nativeParams.setPort(params.getPort());
        nativeParams.setAccount(params.getAccount());
        nativeParams.setPassword(params.getPassword());
        nativeParams.setMount_point(params.getMountPoint());
        nativeParams.setVersion(params.getVersion());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.ManulRtkCalibrationParams toNativeManulRtkCalibrationParams(
            com.dji.flighthubsdk.bean.ManulRtkCalibrationParams params) {
        com.dji.flighthubsdk.generated.ManulRtkCalibrationParams nativeParams =
                new com.dji.flighthubsdk.generated.ManulRtkCalibrationParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setLongitude(params.getLongitude());
        nativeParams.setLatitude(params.getLatitude());
        nativeParams.setHeight(params.getHeight());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SwitchLiveLensParams toNativeSwitchLiveLensParams(
            com.dji.flighthubsdk.bean.SwitchLiveLensParams params) {
        com.dji.flighthubsdk.generated.SwitchLiveLensParams nativeParams =
                new com.dji.flighthubsdk.generated.SwitchLiveLensParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(params.getPayloadIndex());
        nativeParams.setLens_type(
                params.getLensType() != null
                        ? params.getLensType().getValue()
                        : com.dji.flighthubsdk.value.CameraLensType.UNKNOWN.getValue());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SetPhotoStorageSettingsParams toNativeSetPhotoStorageSettingsParams(
            com.dji.flighthubsdk.bean.SetPhotoStorageSettingsParams params) {
        com.dji.flighthubsdk.generated.SetPhotoStorageSettingsParams nativeParams =
                new com.dji.flighthubsdk.generated.SetPhotoStorageSettingsParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(safeString(params.getPayloadIndex()));
        nativeParams.setPhoto_storage_settings(toNativeStringVector(params.getPhotoStorageSettings()));
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SetVideoStorageSettingsParams toNativeSetVideoStorageSettingsParams(
            com.dji.flighthubsdk.bean.SetVideoStorageSettingsParams params) {
        com.dji.flighthubsdk.generated.SetVideoStorageSettingsParams nativeParams =
                new com.dji.flighthubsdk.generated.SetVideoStorageSettingsParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(safeString(params.getPayloadIndex()));
        nativeParams.setVideo_storage_settings(toNativeStringVector(params.getVideoStorageSettings()));
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SetNightSceneModeParams toNativeSetNightSceneModeParams(
            com.dji.flighthubsdk.bean.SetNightSceneModeParams params) {
        com.dji.flighthubsdk.generated.SetNightSceneModeParams nativeParams =
                new com.dji.flighthubsdk.generated.SetNightSceneModeParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(safeString(params.getPayloadIndex()));
        NightSceneMode mode = params.getMode();
        nativeParams.setMode(mode != null ? mode.getValue() : NightSceneMode.AUTO.getValue());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.bean.NightModeSettings toNightModeSettings(
            com.dji.flighthubsdk.generated.NightModeSettings n) {
        com.dji.flighthubsdk.bean.NightModeSettings b = new com.dji.flighthubsdk.bean.NightModeSettings();
        if (n == null) {
            return b;
        }
        b.setPayloadIndex(safeString(n.getPayload_index()));
        b.setNightMode(NightSceneMode.fromValue(n.getNight_mode()));
        b.setWorking(n.getIs_working());
        b.setDenoiseLevel(n.getDenoise_level());
        b.setNightVisionEnable(n.getNight_vision_enable());
        b.setInfraredFillLightEnable(n.getInfrared_fill_light_enable());
        b.setNightSceneModeSuggestion(n.getNight_scene_mode_suggestion());
        return b;
    }

    // ── B4 drc_camera_state_push 拆分转换 ──
    public static com.dji.flighthubsdk.bean.CameraWatermarkSettings toCameraWatermarkSettings(
            com.dji.flighthubsdk.generated.CameraWatermarkSettings n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraWatermarkSettings();
        return new com.dji.flighthubsdk.bean.CameraWatermarkSettings(
                safeString(n.getPayload_index()),
                n.getGlobal_enable(), n.getDrone_type_enable(), n.getManufacturer_enable(),
                n.getDrone_sn_enable(), n.getDatetime_enable(), n.getGps_enable(),
                n.getUser_custom_string_enable(), safeString(n.getUser_custom_string()),
                n.getLayout());
    }

    public static com.dji.flighthubsdk.bean.CameraStorageInfo toCameraStorageInfo(
            com.dji.flighthubsdk.generated.CameraStorageInfo n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraStorageInfo();
        return new com.dji.flighthubsdk.bean.CameraStorageInfo(
                safeString(n.getPayload_index()), n.getTotal(), n.getUsed());
    }

    public static com.dji.flighthubsdk.bean.CameraIrMeterRegion toCameraIrMeterRegion(
            com.dji.flighthubsdk.generated.CameraIrMeterRegion n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraIrMeterRegion();
        return new com.dji.flighthubsdk.bean.CameraIrMeterRegion(
                n.getTop_left_x(), n.getTop_left_y(),
                n.getBottom_right_x(), n.getBottom_right_y());
    }

    public static com.dji.flighthubsdk.bean.CameraIrPipState toCameraIrPipState(
            com.dji.flighthubsdk.generated.CameraIrPipState n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraIrPipState();
        return new com.dji.flighthubsdk.bean.CameraIrPipState(
                safeString(n.getPayload_index()),
                n.getDisplay_mode(), n.getEnhance_mode(), n.getIr_mix_rate(),
                toCameraIrMeterRegion(n.getIr_meter_region()));
    }

    public static com.dji.flighthubsdk.bean.CameraIsothermPolygonPoint toCameraIsothermPolygonPoint(
            com.dji.flighthubsdk.generated.CameraIsothermPolygonPoint n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraIsothermPolygonPoint();
        return new com.dji.flighthubsdk.bean.CameraIsothermPolygonPoint(n.getX(), n.getY());
    }

    public static com.dji.flighthubsdk.bean.CameraIsothermPolygon toCameraIsothermPolygon(
            com.dji.flighthubsdk.generated.CameraIsothermPolygon n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraIsothermPolygon();
        java.util.List<com.dji.flighthubsdk.bean.CameraIsothermPolygonPoint> pts = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.CameraIsothermPolygonPointVector v = n.getPoints();
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                pts.add(toCameraIsothermPolygonPoint(v.get(i)));
            }
        }
        return new com.dji.flighthubsdk.bean.CameraIsothermPolygon(safeString(n.getPayload_index()), pts);
    }

    public static com.dji.flighthubsdk.bean.CameraMediaStorage toCameraMediaStorage(
            com.dji.flighthubsdk.generated.CameraMediaStorage n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraMediaStorage();
        java.util.List<String> photos = new java.util.ArrayList<>();
        java.util.List<String> videos = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.StringVector p = n.getPhoto_storage_settings();
        if (p != null) { for (int i = 0; i < p.size(); i++) photos.add(p.get(i)); }
        com.dji.flighthubsdk.generated.StringVector vv = n.getVideo_storage_settings();
        if (vv != null) { for (int i = 0; i < vv.size(); i++) videos.add(vv.get(i)); }
        return new com.dji.flighthubsdk.bean.CameraMediaStorage(safeString(n.getPayload_index()), photos, videos);
    }

    public static com.dji.flighthubsdk.bean.CameraAlgorithmDehazeInfo toCameraAlgorithmDehazeInfo(
            com.dji.flighthubsdk.generated.CameraAlgorithmDehazeInfo n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraAlgorithmDehazeInfo();
        return new com.dji.flighthubsdk.bean.CameraAlgorithmDehazeInfo(
                safeString(n.getPayload_index()), n.getMode(), n.getLevel(), n.getIs_working());
    }

    public static com.dji.flighthubsdk.bean.CameraPreRecordedInfo toCameraPreRecordedInfo(
            com.dji.flighthubsdk.generated.CameraPreRecordedInfo n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraPreRecordedInfo();
        return new com.dji.flighthubsdk.bean.CameraPreRecordedInfo(
                safeString(n.getPayload_index()), n.getStatus(), n.getPre_recorded_time());
    }

    public static com.dji.flighthubsdk.bean.CameraPhotoStorageFormat toCameraPhotoStorageFormat(
            com.dji.flighthubsdk.generated.CameraPhotoStorageFormat n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraPhotoStorageFormat();
        java.util.List<String> fmts = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.StringVector v = n.getPhoto_storage_formats();
        if (v != null) { for (int i = 0; i < v.size(); i++) fmts.add(v.get(i)); }
        return new com.dji.flighthubsdk.bean.CameraPhotoStorageFormat(n.getPhoto_storage_format_num(), fmts);
    }

    public static com.dji.flighthubsdk.bean.CameraCapability toCameraCapability(
            com.dji.flighthubsdk.generated.CameraCapability n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraCapability();
        java.util.List<Integer> zoom = new java.util.ArrayList<>();
        java.util.List<Integer> ir = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.IntVector zv = n.getZoom_factor_range();
        if (zv != null) { for (int i = 0; i < zv.size(); i++) zoom.add(zv.get(i)); }
        com.dji.flighthubsdk.generated.IntVector iv = n.getIr_factor_range();
        if (iv != null) { for (int i = 0; i < iv.size(); i++) ir.add(iv.get(i)); }
        return new com.dji.flighthubsdk.bean.CameraCapability(
                safeString(n.getPayload_index()), zoom, ir,
                toCameraPhotoStorageFormat(n.getBase_photo_storage_format()));
    }

    public static com.dji.flighthubsdk.bean.CameraSettableEvent toCameraSettableEvent(
            com.dji.flighthubsdk.generated.CameraSettableEvent n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraSettableEvent();
        return new com.dji.flighthubsdk.bean.CameraSettableEvent(n.getEvent_id(), n.getSettable());
    }

    public static com.dji.flighthubsdk.bean.CameraSettableCapability toCameraSettableCapability(
            com.dji.flighthubsdk.generated.CameraSettableCapability n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraSettableCapability();
        java.util.List<com.dji.flighthubsdk.bean.CameraSettableEvent> events = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.CameraSettableEventVector v = n.getEvents();
        if (v != null) { for (int i = 0; i < v.size(); i++) events.add(toCameraSettableEvent(v.get(i))); }
        return new com.dji.flighthubsdk.bean.CameraSettableCapability(safeString(n.getPayload_index()), events);
    }

    public static com.dji.flighthubsdk.bean.CameraVisibleCapabilityItem toCameraVisibleCapabilityItem(
            com.dji.flighthubsdk.generated.CameraVisibleCapabilityItem n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraVisibleCapabilityItem();
        return new com.dji.flighthubsdk.bean.CameraVisibleCapabilityItem(n.getEvent_id(), n.getVisible());
    }

    public static com.dji.flighthubsdk.bean.CameraVisibleCapability toCameraVisibleCapability(
            com.dji.flighthubsdk.generated.CameraVisibleCapability n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraVisibleCapability();
        java.util.List<com.dji.flighthubsdk.bean.CameraVisibleCapabilityItem> events = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.CameraVisibleCapabilityItemVector v = n.getEvents();
        if (v != null) { for (int i = 0; i < v.size(); i++) events.add(toCameraVisibleCapabilityItem(v.get(i))); }
        return new com.dji.flighthubsdk.bean.CameraVisibleCapability(
                safeString(n.getPayload_index()), n.getCamera_mode(), events);
    }

    // ── B4.1 photo info ──
    public static com.dji.flighthubsdk.bean.CameraPhotoInfo toCameraPhotoInfo(
            com.dji.flighthubsdk.generated.CameraPhotoInfo n) {
        if (n == null) return new com.dji.flighthubsdk.bean.CameraPhotoInfo();
        return new com.dji.flighthubsdk.bean.CameraPhotoInfo(
                safeString(n.getPayload_index()), n.getCamera_mode(),
                n.getCurrent_step(), n.getPercent(), n.getCountdown_time(),
                n.getResult(), safeString(n.getStatus()));
    }


    public static com.dji.flighthubsdk.generated.LinkageZoomParams toNativeLinkageZoomParams(
            com.dji.flighthubsdk.bean.LinkageZoomParams params) {
        com.dji.flighthubsdk.generated.LinkageZoomParams n = new com.dji.flighthubsdk.generated.LinkageZoomParams();
        if (params == null) return n;
        n.setPayload_index(safeString(params.getPayloadIndex()));
        n.setState(params.getState());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraScreenDragParams toNativeCameraScreenDragParams(
            com.dji.flighthubsdk.bean.CameraScreenDragParams params) {
        com.dji.flighthubsdk.generated.CameraScreenDragParams n = new com.dji.flighthubsdk.generated.CameraScreenDragParams();
        if (params == null) return n;
        n.setPayload_index(safeString(params.getPayloadIndex()));
        n.setLocked(params.isLocked());
        n.setPitch_speed(params.getPitchSpeed());
        n.setYaw_speed(params.getYawSpeed());
        return n;
    }

    public static com.dji.flighthubsdk.generated.PhotoSizeParam toNativePhotoSizeParam(
            com.dji.flighthubsdk.bean.PhotoSizeParam params) {
        com.dji.flighthubsdk.generated.PhotoSizeParam n = new com.dji.flighthubsdk.generated.PhotoSizeParam();
        if (params == null) return n;
        n.setPayload_index(safeString(params.getPayloadIndex()));
        n.setPhoto_size(params.getPhotoSize());
        n.setCamera_type(safeString(params.getCameraType()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.IntervalTakePhotoSetParam toNativeIntervalTakePhotoSetParam(
            com.dji.flighthubsdk.bean.IntervalTakePhotoSetParam params) {
        com.dji.flighthubsdk.generated.IntervalTakePhotoSetParam n = new com.dji.flighthubsdk.generated.IntervalTakePhotoSetParam();
        if (params == null) return n;
        n.setPayload_index(safeString(params.getPayloadIndex()));
        n.setInterval(params.getInterval());
        return n;
    }

    public static com.dji.flighthubsdk.generated.IntervalTakePhotoParam toNativeIntervalTakePhotoParam(
            com.dji.flighthubsdk.bean.IntervalTakePhotoParam params) {
        com.dji.flighthubsdk.generated.IntervalTakePhotoParam n = new com.dji.flighthubsdk.generated.IntervalTakePhotoParam();
        if (params == null) return n;
        n.setPayload_index(safeString(params.getPayloadIndex()));
        return n;
    }

    public static com.dji.flighthubsdk.bean.IntervalTakePhotoStatus toIntervalTakePhotoStatus(
            com.dji.flighthubsdk.generated.IntervalTakePhotoStatus n) {
        com.dji.flighthubsdk.bean.IntervalTakePhotoStatus b = new com.dji.flighthubsdk.bean.IntervalTakePhotoStatus();
        if (n == null) return b;
        b.setCountdownTime(n.getCountdown_time());
        return b;
    }

    public static com.dji.flighthubsdk.generated.CameraAimParam toNativeCameraAimParam(
            com.dji.flighthubsdk.bean.CameraAimParam params) {
        com.dji.flighthubsdk.generated.CameraAimParam n = new com.dji.flighthubsdk.generated.CameraAimParam();
        if (params == null) return n;
        n.setCamera_type(safeString(params.getCameraType()));
        n.setLocked(params.isLocked());
        n.setPayload_index(safeString(params.getPayloadIndex()));
        n.setX(params.getX());
        n.setY(params.getY());
        return n;
    }

    public static com.dji.flighthubsdk.generated.PanoramaParam toNativePanoramaParam(
            com.dji.flighthubsdk.bean.PanoramaParam params) {
        com.dji.flighthubsdk.generated.PanoramaParam n = new com.dji.flighthubsdk.generated.PanoramaParam();
        if (params == null) {
            return n;
        }
        n.setPayload_index(safeString(params.getPayloadIndex()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.LiveQualitySettingParams toNativeLiveQualitySettingParams(
            com.dji.flighthubsdk.bean.LiveQualitySettingParams params) {
        com.dji.flighthubsdk.generated.LiveQualitySettingParams n =
                new com.dji.flighthubsdk.generated.LiveQualitySettingParams();
        if (params == null) {
            return n;
        }
        n.setSn(safeString(params.getSn()));
        n.setCamera(safeString(params.getCamera()));
        n.setVideo(safeString(params.getVideo()));
        n.setVideo_quality(params.getVideoQuality());
        return n;
    }

    public static com.dji.flighthubsdk.generated.FlightHubSdkConfig toNativeFlightHubSdkConfig(
            com.dji.flighthubsdk.bean.FlightHubSdkConfig bean) {
        com.dji.flighthubsdk.generated.FlightHubSdkConfig n =
                new com.dji.flighthubsdk.generated.FlightHubSdkConfig();
        if (bean == null) {
            return n;
        }
        n.setLog_level(safeString(bean.getLogLevel()));
        n.setLog_to_console(bean.isLogToConsole());
        n.setShow_version_info(bean.isShowVersionInfo());
        n.setLog_file_path(safeString(bean.getLogFilePath()));
        n.setLog_max_file_size(bean.getLogMaxFileSize());
        n.setLog_max_files(bean.getLogMaxFiles());
        n.setAddress(safeString(bean.getAddress()));
        n.setPort(safeString(bean.getPort()));
        n.setNotification_server_address(safeString(bean.getNotificationServerAddress()));
        n.setDrtc_config_path(safeString(bean.getDrtcConfigPath()));
        n.setVerify_ssl(bean.isVerifySsl());
        n.setSsl_ca_path(safeString(bean.getSslCaPath()));
        return n;
    }

    public static com.dji.flighthubsdk.bean.FlightHubSdkConfig toFlightHubSdkConfigBean(
            com.dji.flighthubsdk.generated.FlightHubSdkConfig n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightHubSdkConfig();
        }
        com.dji.flighthubsdk.bean.FlightHubSdkConfig b = new com.dji.flighthubsdk.bean.FlightHubSdkConfig();
        b.setLogLevel(safeString(n.getLog_level()));
        b.setLogToConsole(n.getLog_to_console());
        b.setShowVersionInfo(n.getShow_version_info());
        b.setLogFilePath(safeString(n.getLog_file_path()));
        b.setLogMaxFileSize(n.getLog_max_file_size());
        b.setLogMaxFiles(n.getLog_max_files());
        b.setAddress(safeString(n.getAddress()));
        b.setPort(safeString(n.getPort()));
        b.setNotificationServerAddress(safeString(n.getNotification_server_address()));
        b.setDrtcConfigPath(safeString(n.getDrtc_config_path()));
        b.setVerifySsl(n.getVerify_ssl());
        b.setSslCaPath(safeString(n.getSsl_ca_path()));
        return b;
    }

    // [removed] toWorkspaceSessionInfoBean — 去 SaaS 后无 session 概念

    public static com.dji.flighthubsdk.generated.ZoomAtTargetParams toNativeZoomAtTargetParams(
            com.dji.flighthubsdk.bean.ZoomAtTargetParams params) {
        com.dji.flighthubsdk.generated.ZoomAtTargetParams nativeParams =
                new com.dji.flighthubsdk.generated.ZoomAtTargetParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(params.getPayloadIndex());
        nativeParams.setCamera_lens_type(
                params.getCameraLensType() != null
                        ? params.getCameraLensType().getValue()
                        : com.dji.flighthubsdk.value.CameraLensType.UNKNOWN.getValue());
        nativeParams.setX(params.getX());
        nativeParams.setY(params.getY());
        nativeParams.setWidth(params.getWidth());
        nativeParams.setHeight(params.getHeight());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.CameraFocalLengthParams toNativeCameraFocalLengthParams(
            com.dji.flighthubsdk.bean.CameraFocalLengthParams params) {
        com.dji.flighthubsdk.generated.CameraFocalLengthParams nativeParams =
                new com.dji.flighthubsdk.generated.CameraFocalLengthParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(params.getPayloadIndex());
        nativeParams.setCamera_type(
                params.getCameraType() != null
                        ? params.getCameraType().getValue()
                        : com.dji.flighthubsdk.value.CameraLensType.UNKNOWN.getValue());
        nativeParams.setZoom_factor(params.getZoomFactor());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.SwitchCameraModeParams toNativeSwitchCameraModeParams(
            com.dji.flighthubsdk.bean.SwitchCameraModeParams params) {
        com.dji.flighthubsdk.generated.SwitchCameraModeParams nativeParams =
                new com.dji.flighthubsdk.generated.SwitchCameraModeParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(params.getPayloadIndex());
        nativeParams.setCamera_mode(
                params.getCameraMode() != null
                        ? toNativeCameraModeInt(params.getCameraMode())
                        : toNativeCameraModeInt(com.dji.flighthubsdk.value.CameraMode.UNKNOWN));
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.LookAtTargetParams toNativeLookAtTargetParams(
            com.dji.flighthubsdk.bean.LookAtTargetParams params) {
        com.dji.flighthubsdk.generated.LookAtTargetParams nativeParams =
                new com.dji.flighthubsdk.generated.LookAtTargetParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setPayload_index(params.getPayloadIndex());
        nativeParams.setLatitude(params.getLatitude());
        nativeParams.setLongitude(params.getLongitude());
        nativeParams.setHeight(params.getHeight());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.ExportFlightRecordsRequestParams toNativeExportFlightRecordsRequestParams(
            com.dji.flighthubsdk.bean.ExportFlightRecordsRequestParams params) {
        com.dji.flighthubsdk.generated.ExportFlightRecordsRequestParams nativeParams =
                new com.dji.flighthubsdk.generated.ExportFlightRecordsRequestParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setContent_type(params.getContentType());
        nativeParams.setFile_type(toNativeStringVector(params.getFileType()));
        nativeParams.setFile_name(params.getFileName());
        nativeParams.setFlight_task_ids(toNativeStringVector(params.getFlightTaskIds()));
        nativeParams.setMetrics(toNativeStringVector(params.getMetrics()));
        nativeParams.setTime_zone(params.getTimeZone());
        nativeParams.setLocale(params.getLocale());
        return nativeParams;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoItemParam toNativeDeviceLogReportDeviceTopoItemParam(
            com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam item) {
        com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoItemParam nativeItem =
                new com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoItemParam();
        if (item == null) {
            return nativeItem;
        }
        nativeItem.setDevice_model_key(item.getDeviceModelKey());
        nativeItem.setSn(item.getSn());
        return nativeItem;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoItemParamVector toNativeDeviceLogReportDeviceTopoItemParamVector(
            List<com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam> items) {
        com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoItemParamVector vector =
                new com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoItemParamVector();
        if (items == null) {
            return vector;
        }
        for (com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam item : items) {
            vector.add(toNativeDeviceLogReportDeviceTopoItemParam(item));
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoParam toNativeDeviceLogReportDeviceTopoParam(
            com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoParam param) {
        com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoParam nativeParam =
                new com.dji.flighthubsdk.generated.DeviceLogReportDeviceTopoParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setParents(toNativeDeviceLogReportDeviceTopoItemParamVector(param.getParents()));
        nativeParam.setHosts(toNativeDeviceLogReportDeviceTopoItemParamVector(param.getHosts()));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportContactInformationParam toNativeDeviceLogReportContactInformationParam(
            com.dji.flighthubsdk.bean.DeviceLogReportContactInformationParam param) {
        com.dji.flighthubsdk.generated.DeviceLogReportContactInformationParam nativeParam =
                new com.dji.flighthubsdk.generated.DeviceLogReportContactInformationParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setEmail(param.getEmail());
        nativeParam.setPhone(param.getPhone());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportLogItemParam toNativeDeviceLogReportLogItemParam(
            com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam item) {
        com.dji.flighthubsdk.generated.DeviceLogReportLogItemParam nativeItem =
                new com.dji.flighthubsdk.generated.DeviceLogReportLogItemParam();
        if (item == null) {
            return nativeItem;
        }
        nativeItem.setBoot_index(item.getBootIndex());
        nativeItem.setStart_time(item.getStartTime());
        nativeItem.setEnd_time(item.getEndTime());
        nativeItem.setSize(item.getSize());
        nativeItem.setMd5(item.getMd5());
        return nativeItem;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportLogItemParamVector toNativeDeviceLogReportLogItemParamVector(
            List<com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam> items) {
        com.dji.flighthubsdk.generated.DeviceLogReportLogItemParamVector vector =
                new com.dji.flighthubsdk.generated.DeviceLogReportLogItemParamVector();
        if (items == null) {
            return vector;
        }
        for (com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam item : items) {
            vector.add(toNativeDeviceLogReportLogItemParam(item));
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportLogListItemParam toNativeDeviceLogReportLogListItemParam(
            com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam item) {
        com.dji.flighthubsdk.generated.DeviceLogReportLogListItemParam nativeItem =
                new com.dji.flighthubsdk.generated.DeviceLogReportLogListItemParam();
        if (item == null) {
            return nativeItem;
        }
        nativeItem.setDevice_sn(item.getDeviceSn());
        nativeItem.setList(toNativeDeviceLogReportLogItemParamVector(item.getList()));
        nativeItem.setModule(item.getModule());
        return nativeItem;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportLogListItemParamVector toNativeDeviceLogReportLogListItemParamVector(
            List<com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam> items) {
        com.dji.flighthubsdk.generated.DeviceLogReportLogListItemParamVector vector =
                new com.dji.flighthubsdk.generated.DeviceLogReportLogListItemParamVector();
        if (items == null) {
            return vector;
        }
        for (com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam item : items) {
            vector.add(toNativeDeviceLogReportLogListItemParam(item));
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportDomainItemParam toNativeDeviceLogReportDomainItemParam(
            com.dji.flighthubsdk.bean.DeviceLogReportDomainItemParam item) {
        com.dji.flighthubsdk.generated.DeviceLogReportDomainItemParam nativeItem =
                new com.dji.flighthubsdk.generated.DeviceLogReportDomainItemParam();
        if (item == null) {
            return nativeItem;
        }
        nativeItem.setKey(item.getKey());
        nativeItem.setValue(item.getValue());
        return nativeItem;
    }

    public static com.dji.flighthubsdk.generated.DeviceLogReportDomainItemParamVector toNativeDeviceLogReportDomainItemParamVector(
            List<com.dji.flighthubsdk.bean.DeviceLogReportDomainItemParam> items) {
        com.dji.flighthubsdk.generated.DeviceLogReportDomainItemParamVector vector =
                new com.dji.flighthubsdk.generated.DeviceLogReportDomainItemParamVector();
        if (items == null) {
            return vector;
        }
        for (com.dji.flighthubsdk.bean.DeviceLogReportDomainItemParam item : items) {
            vector.add(toNativeDeviceLogReportDomainItemParam(item));
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.CreateDeviceLogReportRequestParam toNativeCreateDeviceLogReportRequestParam(
            com.dji.flighthubsdk.bean.CreateDeviceLogReportRequestParam param) {
        com.dji.flighthubsdk.generated.CreateDeviceLogReportRequestParam nativeParam =
                new com.dji.flighthubsdk.generated.CreateDeviceLogReportRequestParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setReport_information(param.getReportInformation());
        nativeParam.setError_start_time(param.getErrorStartTime());
        nativeParam.setDevice_topo(toNativeDeviceLogReportDeviceTopoParam(param.getDeviceTopo()));
        nativeParam.setReport_contact_information(
                toNativeDeviceLogReportContactInformationParam(param.getReportContactInformation()));
        nativeParam.setAppendix_files(toNativeStringVector(param.getAppendixFiles()));
        nativeParam.setLog_list(toNativeDeviceLogReportLogListItemParamVector(param.getLogList()));
        nativeParam.setDomains(toNativeDeviceLogReportDomainItemParamVector(param.getDomains()));
        nativeParam.setIs_crashed(param.isCrashed());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.DeviceAudienceCountRequestItemVector toNativeDeviceAudienceCountRequestItemVector(
            List<com.dji.flighthubsdk.bean.DeviceAudienceCountRequestItem> items) {
        com.dji.flighthubsdk.generated.DeviceAudienceCountRequestItemVector vector =
                new com.dji.flighthubsdk.generated.DeviceAudienceCountRequestItemVector();
        if (items == null) {
            return vector;
        }
        for (com.dji.flighthubsdk.bean.DeviceAudienceCountRequestItem item : items) {
            if (item == null) {
                continue;
            }
            com.dji.flighthubsdk.generated.DeviceAudienceCountRequestItem nativeItem =
                    new com.dji.flighthubsdk.generated.DeviceAudienceCountRequestItem();
            nativeItem.setSn(item.getSn());
            nativeItem.setCamera_list(listToStringVector(item.getCameraList()));
            vector.add(nativeItem);
        }
        return vector;
    }

    public static com.dji.flighthubsdk.generated.AudienceCountGroup toNativeAudienceCountGroup(
            com.dji.flighthubsdk.bean.AudienceCountGroup group) {
        if (group == null) {
            return null;
        }
        com.dji.flighthubsdk.generated.AudienceCountGroup nativeGroup =
                new com.dji.flighthubsdk.generated.AudienceCountGroup();
        nativeGroup.setLevel_1_group(group.getLevel1Group());
        nativeGroup.setLevel_2_group(group.getLevel2Group());
        return nativeGroup;
    }

    public static com.dji.flighthubsdk.generated.FirmwareUpgradeParam toNativeFirmwareUpgradeParam(
            com.dji.flighthubsdk.bean.FirmwareUpgradeParam param) {
        com.dji.flighthubsdk.generated.FirmwareUpgradeParam nativeParam = new com.dji.flighthubsdk.generated.FirmwareUpgradeParam();
        com.dji.flighthubsdk.generated.FirmwareUpgradeDeviceItemVector devices = new com.dji.flighthubsdk.generated.FirmwareUpgradeDeviceItemVector();
        if (param != null && param.getDevices() != null) {
            for (com.dji.flighthubsdk.bean.FirmwareUpgradeDeviceItem item : param.getDevices()) {
                if (item == null) {
                    continue;
                }
                com.dji.flighthubsdk.generated.FirmwareUpgradeDeviceItem nativeItem = new com.dji.flighthubsdk.generated.FirmwareUpgradeDeviceItem();
                nativeItem.setSn(item.getSn());
                nativeItem.setDevice_model_key(item.getDeviceModelKey());
                nativeItem.setFirmware_upgrade_type(item.getFirmwareUpgradeType());
                nativeItem.setProduct_version(item.getProductVersion());
                devices.add(nativeItem);
            }
        }
        nativeParam.setDevices(devices);
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.FirmwareInfoParam toNativeFirmwareInfoParam(
            com.dji.flighthubsdk.bean.FirmwareInfoParam param) {
        com.dji.flighthubsdk.generated.FirmwareInfoParam nativeParam = new com.dji.flighthubsdk.generated.FirmwareInfoParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setDevice_model_key(param.getDeviceModelKey());
        nativeParam.setVersion(param.getVersion());
        nativeParam.setDesc(param.getDesc());
        nativeParam.setObject_key(param.getObjectKey());
        nativeParam.setMd5(param.getMd5());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.QueryDeviceUnlockLicenseListParam toNativeQueryDeviceUnlockLicenseListParam(
            com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListParam param) {
        com.dji.flighthubsdk.generated.QueryDeviceUnlockLicenseListParam nativeParam = new com.dji.flighthubsdk.generated.QueryDeviceUnlockLicenseListParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setDevice_sn(param.getDeviceSn());
        nativeParam.setDevice_model_domain(param.getDeviceModelDomain());
        nativeParam.setLicense_type(param.getLicenseType());
        nativeParam.setLicense_id(param.getLicenseId());
        nativeParam.setDetail(param.isDetail());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.SwitchUnlockLicenseParam toNativeSwitchUnlockLicenseParam(
            com.dji.flighthubsdk.bean.SwitchUnlockLicenseParam param) {
        com.dji.flighthubsdk.generated.SwitchUnlockLicenseParam nativeParam = new com.dji.flighthubsdk.generated.SwitchUnlockLicenseParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setDevice_sn(param.getDeviceSn());
        nativeParam.setLicense_id(param.getLicenseId());
        nativeParam.setEnable(param.isEnable());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.CheckUnlockLicenseParam toNativeCheckUnlockLicenseParam(
            com.dji.flighthubsdk.bean.CheckUnlockLicenseParam param) {
        com.dji.flighthubsdk.generated.CheckUnlockLicenseParam nativeParam = new com.dji.flighthubsdk.generated.CheckUnlockLicenseParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setFile_key(param.getFileKey());
        nativeParam.setDevice_sn(param.getDeviceSn());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.HistoryHmsDataParam toNativeHistoryHmsDataParam(
            com.dji.flighthubsdk.bean.HistoryHmsDataParam param) {
        com.dji.flighthubsdk.generated.HistoryHmsDataParam nativeParam = new com.dji.flighthubsdk.generated.HistoryHmsDataParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setSn_list(toNativeStringVector(param.getSnList()));
        nativeParam.setCreate_time_begin(param.getCreateTimeBegin());
        nativeParam.setCreate_time_end(param.getCreateTimeEnd());
        nativeParam.setPage_size(param.getPageSize());
        nativeParam.setPage_num(param.getPageNum());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.ImportUnlockLicenseParam toNativeImportUnlockLicenseParam(
            com.dji.flighthubsdk.bean.ImportUnlockLicenseParam param) {
        com.dji.flighthubsdk.generated.ImportUnlockLicenseParam nativeParam = new com.dji.flighthubsdk.generated.ImportUnlockLicenseParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setDevice_sn(param.getDeviceSn());
        nativeParam.setCert_url(toNativeStringVector(Collections.singletonList(param.getCertUrl())));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.GetWaylineListParam toNativeGetWaylineListParam(
            com.dji.flighthubsdk.bean.GetWaylineListParam param) {
        com.dji.flighthubsdk.generated.GetWaylineListParam nativeParam = new com.dji.flighthubsdk.generated.GetWaylineListParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setPage(param.getPage());
        nativeParam.setPage_size(param.getPageSize());
        nativeParam.setStart_timestamp(param.getStartTimestamp());
        nativeParam.setEnd_timestamp(param.getEndTimestamp());
        nativeParam.setDrone_types(listToStringVector(param.getDroneTypes()));
        nativeParam.setWayline_uuids(listToStringVector(param.getWaylineUuids()));
        nativeParam.setPayload_types(listToStringVector(param.getPayloadTypes()));
        nativeParam.setWayline_types(listToStringVector(param.getWaylineTypes()));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.SubmitWaylineInfoParam toNativeSubmitWaylineInfoParam(
            com.dji.flighthubsdk.bean.SubmitWaylineInfoParam param) {
        com.dji.flighthubsdk.generated.SubmitWaylineInfoParam nativeParam = new com.dji.flighthubsdk.generated.SubmitWaylineInfoParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setKmz_object_key(param.getKmzObjectKey());
        nativeParam.setWayline_name(param.getWaylineName());
        nativeParam.setAsync_request_id(param.getAsyncRequestId());
        nativeParam.setTimestamp(param.getTimestamp());
        nativeParam.setTransparent(toNativeStringStringUnorderedMap(param.getTransparent()));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.BatchDownloadWaylinesParam toNativeBatchDownloadWaylinesParam(
            com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam param) {
        com.dji.flighthubsdk.generated.BatchDownloadWaylinesParam nativeParam = new com.dji.flighthubsdk.generated.BatchDownloadWaylinesParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setWayline_uuids(listToStringVector(param.getWaylineUuids()));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.DuplicateWaylineParam toNativeDuplicateWaylineParam(
            com.dji.flighthubsdk.bean.DuplicateWaylineParam param) {
        com.dji.flighthubsdk.generated.DuplicateWaylineParam nativeParam = new com.dji.flighthubsdk.generated.DuplicateWaylineParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setOriginal_wayline_uuid(param.getOriginalWaylineUuid());
        nativeParam.setTarget_wayline_name(param.getTargetWaylineName());
        nativeParam.setTarget_object_key_prefix(param.getTargetObjectKeyPrefix());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.ModifyDeviceNameParam toNativeModifyDeviceNameParam(
            com.dji.flighthubsdk.bean.ModifyDeviceNameParam param) {
        com.dji.flighthubsdk.generated.ModifyDeviceNameParam nativeParam = new com.dji.flighthubsdk.generated.ModifyDeviceNameParam();
        com.dji.flighthubsdk.generated.ModifyDeviceNameItemVector vector = new com.dji.flighthubsdk.generated.ModifyDeviceNameItemVector();
        if (param != null && param.getDeviceList() != null) {
            for (com.dji.flighthubsdk.bean.ModifyDeviceNameItem item : param.getDeviceList()) {
                if (item == null) {
                    continue;
                }
                com.dji.flighthubsdk.generated.ModifyDeviceNameItem nativeItem = new com.dji.flighthubsdk.generated.ModifyDeviceNameItem();
                nativeItem.setDevice_sn(item.getDeviceSn());
                nativeItem.setDevice_callsign(item.getDeviceCallsign());
                vector.add(nativeItem);
            }
        }
        nativeParam.setDevice_list(vector);
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.CreatePsdkSpeakerResourceParams toNativeCreatePsdkSpeakerResourceParams(
            com.dji.flighthubsdk.bean.CreatePsdkSpeakerResourceParams param) {
        com.dji.flighthubsdk.generated.CreatePsdkSpeakerResourceParams nativeParam = new com.dji.flighthubsdk.generated.CreatePsdkSpeakerResourceParams();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setName(param.getName());
        nativeParam.setFile_type(param.getFileType());
        nativeParam.setObject_key(param.getObjectKey());
        nativeParam.setDuration(param.getDuration());
        nativeParam.setContent(param.getContent());
        nativeParam.setCreate_scene(param.getCreateScene());
        nativeParam.setNeed_opus(param.isNeedOpus());
        nativeParam.setSize(param.getSize());
        nativeParam.setVisible(param.isVisible());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.EditPsdkSpeakerResourceParams toNativeEditPsdkSpeakerResourceParams(
            com.dji.flighthubsdk.bean.EditPsdkSpeakerResourceParams param) {
        com.dji.flighthubsdk.generated.EditPsdkSpeakerResourceParams nativeParam = new com.dji.flighthubsdk.generated.EditPsdkSpeakerResourceParams();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setId(param.getId());
        nativeParam.setName(param.getName());
        nativeParam.setFile_type(param.getFileType());
        nativeParam.setVisible(param.isVisible());
        nativeParam.setContent(param.getContent());
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.GetPsdkWidgetResourceParams toNativeGetPsdkWidgetResourceParams(
            com.dji.flighthubsdk.bean.GetPsdkWidgetResourceParams param) {
        com.dji.flighthubsdk.generated.GetPsdkWidgetResourceParams nativeParam = new com.dji.flighthubsdk.generated.GetPsdkWidgetResourceParams();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setObject_keys(listToStringVector(param.getObjectKeys()));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.StickControlData toNativeStickControlData(StickControlData bean) {
        com.dji.flighthubsdk.generated.StickControlData n = new com.dji.flighthubsdk.generated.StickControlData();
        if (bean == null) {
            return n;
        }
        n.setRoll(bean.getRoll());
        n.setPitch(bean.getPitch());
        n.setThrottle(bean.getThrottle());
        n.setYaw(bean.getYaw());
        n.setGimbal_pitch(bean.getGimbalPitch());
        return n;
    }

    public static com.dji.flighthubsdk.generated.PsdkSpeakerAudioPlayInfo toNativePsdkSpeakerAudioPlayInfo(
            PsdkSpeakerAudioPlayInfo bean) {
        com.dji.flighthubsdk.generated.PsdkSpeakerAudioPlayInfo n =
                new com.dji.flighthubsdk.generated.PsdkSpeakerAudioPlayInfo();
        if (bean == null) {
            return n;
        }
        if (bean.getName() != null) {
            n.setName(bean.getName());
        }
        if (bean.getUrl() != null) {
            n.setUrl(bean.getUrl());
        }
        if (bean.getMd5() != null) {
            n.setMd5(bean.getMd5());
        }
        if (bean.getFormat() != null) {
            n.setFormat(bean.getFormat());
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.PsdkSpeakerTTSPlayInfo toNativePsdkSpeakerTTSPlayInfo(
            PsdkSpeakerTTSPlayInfo bean) {
        com.dji.flighthubsdk.generated.PsdkSpeakerTTSPlayInfo n =
                new com.dji.flighthubsdk.generated.PsdkSpeakerTTSPlayInfo();
        if (bean == null) {
            return n;
        }
        if (bean.getName() != null) {
            n.setName(bean.getName());
        }
        if (bean.getText() != null) {
            n.setText(bean.getText());
        }
        if (bean.getMd5() != null) {
            n.setMd5(bean.getMd5());
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.PsdkSpeakerTTSconfig toNativePsdkSpeakerTTSconfig(
            PsdkSpeakerTTSconfig bean) {
        com.dji.flighthubsdk.generated.PsdkSpeakerTTSconfig n =
                new com.dji.flighthubsdk.generated.PsdkSpeakerTTSconfig();
        if (bean == null) {
            return n;
        }
        n.setVolume(bean.getVolume());
        n.setSpeed(bean.getSpeed());
        if (bean.getType() != null) {
            n.setType(bean.getType().getValue());
        }
        if (bean.getLanguage() != null) {
            n.setLanguage(bean.getLanguage().getValue());
        }
        return n;
    }

    // ==================== HsiInfo ====================

    public static com.dji.flighthubsdk.bean.HsiInfo toHsiInfo(com.dji.flighthubsdk.generated.HsiInfo n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.HsiInfo(
                    java.util.Collections.emptyList(),
                    false, false,
                    0, false, false,
                    false, false,
                    false, false,
                    false, false,
                    false, false,
                    0, false, false,
                    false, false);
        }
        return new com.dji.flighthubsdk.bean.HsiInfo(
                copyIntVector(n.getAround_distances()),
                n.getBack_enable(), n.getBack_work(),
                n.getDown_distance(), n.getDown_enable(), n.getDown_work(),
                n.getFront_enable(), n.getFront_work(),
                n.getHorizontal_enable(), n.getHorizontal_work(),
                n.getLeft_enable(), n.getLeft_work(),
                n.getRight_enable(), n.getRight_work(),
                n.getUp_distance(), n.getUp_enable(), n.getUp_work(),
                n.getVertical_enable(), n.getVertical_work());
    }

    // ==================== Flight Safety Settings ====================

    private static com.dji.flighthubsdk.bean.FlightSafeSyncField toFlightSafeSyncField(
            com.dji.flighthubsdk.generated.FlightSafeSyncField n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightSafeSyncField("", false);
        }
        return new com.dji.flighthubsdk.bean.FlightSafeSyncField(
                safeString(n.getField()), n.getSynced());
    }

    private static com.dji.flighthubsdk.bean.FlightSafeSyncResult toFlightSafeSyncResult(
            com.dji.flighthubsdk.generated.FlightSafeSyncResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightSafeSyncResult("", java.util.Collections.emptyList());
        }
        java.util.List<com.dji.flighthubsdk.bean.FlightSafeSyncField> fields = new java.util.ArrayList<>();
        if (n.getResults() != null) {
            for (int i = 0; i < n.getResults().size(); i++) {
                fields.add(toFlightSafeSyncField(n.getResults().get(i)));
            }
        }
        return new com.dji.flighthubsdk.bean.FlightSafeSyncResult(safeString(n.getStatus()), fields);
    }

    public static com.dji.flighthubsdk.bean.FlightSmartPlanModeResult toFlightSmartPlanModeResult(
            com.dji.flighthubsdk.generated.FlightSmartPlanModeResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightSmartPlanModeResult(
                    0, new com.dji.flighthubsdk.bean.FlightSafeSyncResult("", java.util.Collections.emptyList()));
        }
        int mode = n.getSettings() != null ? n.getSettings().getSmart_planning_mode() : 0;
        return new com.dji.flighthubsdk.bean.FlightSmartPlanModeResult(
                mode, toFlightSafeSyncResult(n.getDevice_shadow_sync()));
    }

    public static com.dji.flighthubsdk.bean.FlightOutOfControlActionResult toFlightOutOfControlActionResult(
            com.dji.flighthubsdk.generated.FlightOutOfControlActionResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightOutOfControlActionResult(
                    "", new com.dji.flighthubsdk.bean.FlightSafeSyncResult("", java.util.Collections.emptyList()));
        }
        String action = n.getSettings() != null ? safeString(n.getSettings().getOut_of_control_action()) : "";
        return new com.dji.flighthubsdk.bean.FlightOutOfControlActionResult(
                action, toFlightSafeSyncResult(n.getDevice_shadow_sync()));
    }

    public static com.dji.flighthubsdk.bean.FlightCommanderHeightResult toFlightCommanderHeightResult(
            com.dji.flighthubsdk.generated.FlightCommanderHeightResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightCommanderHeightResult(
                    0, new com.dji.flighthubsdk.bean.FlightSafeSyncResult("", java.util.Collections.emptyList()));
        }
        int height = n.getSettings() != null ? n.getSettings().getCommander_flight_height() : 0;
        return new com.dji.flighthubsdk.bean.FlightCommanderHeightResult(
                height, toFlightSafeSyncResult(n.getDevice_shadow_sync()));
    }

    public static com.dji.flighthubsdk.bean.FlightRthAltitudeResult toFlightRthAltitudeResult(
            com.dji.flighthubsdk.generated.FlightRthAltitudeResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlightRthAltitudeResult(
                    0, new com.dji.flighthubsdk.bean.FlightSafeSyncResult("", java.util.Collections.emptyList()));
        }
        int alt = n.getSettings() != null ? n.getSettings().getRth_altitude() : 0;
        return new com.dji.flighthubsdk.bean.FlightRthAltitudeResult(
                alt, toFlightSafeSyncResult(n.getDevice_shadow_sync()));
    }

    // toAirTransferEnableResult 已废弃：接口不再返回 data 字段

    // ==================== Geo Caging ====================

    public static com.dji.flighthubsdk.generated.GeoCagingInfoParamVector toNativeGeoCagingInfoParamVector(
            java.util.List<com.dji.flighthubsdk.bean.GeoCagingInfoParam> list) {
        com.dji.flighthubsdk.generated.GeoCagingInfoParamVector nativeVector =
                new com.dji.flighthubsdk.generated.GeoCagingInfoParamVector();
        if (list == null) {
            return nativeVector;
        }
        for (com.dji.flighthubsdk.bean.GeoCagingInfoParam p : list) {
            com.dji.flighthubsdk.generated.GeoCagingInfoParam np =
                    new com.dji.flighthubsdk.generated.GeoCagingInfoParam();
            np.setFg_longitude(p.getFgLongitude());
            np.setFg_latitude(p.getFgLatitude());
            np.setFg_radius(p.getFgRadius());
            np.setFg_height(p.getFgHeight());
            np.setCv_longitude(p.getCvLongitude());
            np.setCv_latitude(p.getCvLatitude());
            np.setCv_radius(p.getCvRadius());
            np.setCv_height(p.getCvHeight());
            nativeVector.add(np);
        }
        return nativeVector;
    }

    // ==================== FlyTo Update Target Points ====================

    public static com.dji.flighthubsdk.generated.FlyToUpdateTargetPointsParams toNativeFlyToUpdateTargetPointsParams(
            com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams params) {
        com.dji.flighthubsdk.generated.FlyToUpdateTargetPointsParams nativeParams =
                new com.dji.flighthubsdk.generated.FlyToUpdateTargetPointsParams();
        if (params == null) {
            return nativeParams;
        }
        nativeParams.setFly_to_id(safeString(params.getFlyToId()));
        nativeParams.setMax_speed(params.getMaxSpeed());
        if (params.getStartPoint() != null) {
            com.dji.flighthubsdk.generated.FlyToTargetPoint sp = new com.dji.flighthubsdk.generated.FlyToTargetPoint();
            sp.setLatitude(params.getStartPoint().getLatitude());
            sp.setLongitude(params.getStartPoint().getLongitude());
            sp.setHeight(params.getStartPoint().getHeight());
            nativeParams.setStart_point(sp);
        }
        if (params.getPoints() != null) {
            com.dji.flighthubsdk.generated.FlyToTargetPointVector nativePoints =
                    new com.dji.flighthubsdk.generated.FlyToTargetPointVector();
            for (com.dji.flighthubsdk.bean.FlyToTargetPoint p : params.getPoints()) {
                com.dji.flighthubsdk.generated.FlyToTargetPoint np = new com.dji.flighthubsdk.generated.FlyToTargetPoint();
                np.setLatitude(p.getLatitude());
                np.setLongitude(p.getLongitude());
                np.setHeight(p.getHeight());
                nativePoints.add(np);
            }
            nativeParams.setPoints(nativePoints);
        }
        return nativeParams;
    }

    // ==================== 报修管理模块转换 ====================

    public static DeviceRepairConfigResults toDeviceRepairConfigResults(com.dji.flighthubsdk.generated.DeviceRepairConfigResults n) {
        if (n == null) {
            return new DeviceRepairConfigResults(Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.DeviceRepairConfigVector v = n.getData();
        if (v == null || v.isEmpty()) {
            return new DeviceRepairConfigResults(Collections.emptyList());
        }
        List<DeviceRepairConfig> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDeviceRepairConfig(v.get(i)));
        }
        return new DeviceRepairConfigResults(Collections.unmodifiableList(out));
    }

    public static DeviceRepairConfig toDeviceRepairConfig(com.dji.flighthubsdk.generated.DeviceRepairConfig n) {
        if (n == null) return new DeviceRepairConfig("", true, Collections.emptyList(), new RepairGuidance("", ""));
        List<Integer> sourceTypes = new ArrayList<>();
        com.dji.flighthubsdk.generated.IntVector sv = n.getAllowed_source_types();
        if (sv != null) {
            for (int i = 0; i < sv.size(); i++) {
                sourceTypes.add(sv.get(i));
            }
        }
        RepairGuidance guidance = new RepairGuidance(
            n.getRepair_guidance().getRedirect_url(),
            n.getRepair_guidance().getDescription()
        );
        return new DeviceRepairConfig(n.getDevice_sn(), n.getSupport_repair(), sourceTypes, guidance);
    }

    public static CreateMaintenanceOrderResults toCreateMaintenanceOrderResults(com.dji.flighthubsdk.generated.CreateMaintenanceOrderResults n) {
        if (n == null) {
            return new CreateMaintenanceOrderResults(false, Collections.emptyList());
        }
        com.dji.flighthubsdk.generated.GeneratedOrderInfoResultVector v = n.getGenerated_orders();
        List<GeneratedOrderInfo> out = new ArrayList<>();
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                com.dji.flighthubsdk.generated.GeneratedOrderInfoResult item = v.get(i);
                out.add(new GeneratedOrderInfo(item.getOrder_number(), item.getStatus(), item.getRuiyun_order_number()));
            }
        }
        return new CreateMaintenanceOrderResults(n.getSuccess(), Collections.unmodifiableList(out));
    }

    private static MaintenanceOrderContactAddress toMaintenanceOrderContactAddress(com.dji.flighthubsdk.generated.MaintenanceOrderFullContactAddress n) {
        if (n == null) return new MaintenanceOrderContactAddress("", "", "", "", "");
        return new MaintenanceOrderContactAddress(n.getCountry(), n.getProvince(), n.getCity(), n.getDistrict(), n.getDetail());
    }

    private static MaintenanceOrderRepairInfo toMaintenanceOrderRepairInfo(com.dji.flighthubsdk.generated.MaintenanceOrderRepairInfo n) {
        if (n == null) return new MaintenanceOrderRepairInfo("", "", "");
        return new MaintenanceOrderRepairInfo(n.getAssignee(), n.getAssignee_phone(), n.getRepair_start_time());
    }

    private static MaintenanceOrderLogReport toMaintenanceOrderLogReport(com.dji.flighthubsdk.generated.MaintenanceOrderLogReport n) {
        if (n == null) return new MaintenanceOrderLogReport("", "", "");
        return new MaintenanceOrderLogReport(n.getReport_uuid(), n.getTrack_number(), n.getStatus());
    }

    private static List<MaintenanceOrderAttachment> toMaintenanceOrderAttachments(com.dji.flighthubsdk.generated.MaintenanceOrderDetailAttachmentsVector v) {
        if (v == null || v.isEmpty()) return Collections.emptyList();
        List<MaintenanceOrderAttachment> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            com.dji.flighthubsdk.generated.MaintenanceOrderDetailAttachments item = v.get(i);
            out.add(new MaintenanceOrderAttachment(item.getId(), item.getFile_name(), item.getObject_key(), item.getFile_size(), item.getUpload_time(), item.getCreated_at()));
        }
        return Collections.unmodifiableList(out);
    }

    private static List<MaintenanceOrderStatusHistoryItem> toMaintenanceOrderStatusHistory(com.dji.flighthubsdk.generated.MaintenanceOrderStatusHistoryVector v) {
        if (v == null || v.isEmpty()) return Collections.emptyList();
        List<MaintenanceOrderStatusHistoryItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            com.dji.flighthubsdk.generated.MaintenanceOrderStatusHistory item = v.get(i);
            out.add(new MaintenanceOrderStatusHistoryItem(item.getFrom_status(), item.getTo_status(), item.getChange_time(), item.getOperator_id(), item.getOperator_name(), item.getRemark()));
        }
        return Collections.unmodifiableList(out);
    }

    public static MaintenanceOrderDetailResults toMaintenanceOrderDetailResults(com.dji.flighthubsdk.generated.MaintenanceOrderDetailResults n) {
        if (n == null) return null;
        return new MaintenanceOrderDetailResults(
            n.getOrder_number(), n.getOrganization_uuid(), n.getCreator_id(), n.getCreator_name(),
            n.getSource_type(), n.getSource_type_text(), n.getStatus(), n.getStatus_text(),
            n.getFault_description(), n.getFault_occurrence_time(), n.getExpected_service_time(),
            n.getContact_person(), n.getContact_phone(), n.getContact_email(),
            toMaintenanceOrderContactAddress(n.getContact_address_full()),
            n.getCreated_at(), n.getUpdated_at(), n.getSubmission_time(), n.getAccepted_time(), n.getCompleted_time(),
            n.getRuiyun_order_number(), n.getRuiyun_sync_status(),
            n.getDevice_sn(), n.getDevice_model_key(), n.getModule(), n.getNameplate_sn(),
            n.getDiagnostic_id(), n.getDevice_report_id(),
            toMaintenanceOrderAttachments(n.getAttachments()),
            toMaintenanceOrderStatusHistory(n.getStatus_history()),
            toMaintenanceOrderRepairInfo(n.getRepair_info()),
            toMaintenanceOrderLogReport(n.getLog_report())
        );
    }

    public static MaintenanceOrdersListResults toMaintenanceOrdersListResults(com.dji.flighthubsdk.generated.MaintenanceOrdersListResults n) {
        if (n == null) return new MaintenanceOrdersListResults(new CommonPaginationData(0, 0, 0), Collections.emptyList());
        com.dji.flighthubsdk.generated.CommonPaginationData pg = n.getPagination();
        CommonPaginationData pagination = new CommonPaginationData(pg.getPage(), pg.getPage_size(), pg.getTotal());
        com.dji.flighthubsdk.generated.MaintenanceOrdersListItemVector v = n.getList();
        List<MaintenanceOrdersListItem> items = new ArrayList<>();
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                items.add(toMaintenanceOrdersListItem(v.get(i)));
            }
        }
        return new MaintenanceOrdersListResults(pagination, Collections.unmodifiableList(items));
    }

    private static MaintenanceOrdersListItem toMaintenanceOrdersListItem(com.dji.flighthubsdk.generated.MaintenanceOrdersListItem n) {
        if (n == null) return null;
        return new MaintenanceOrdersListItem(
            n.getOrder_number(), n.getRuiyun_order_number(), n.getStatus(), n.getStatus_text(),
            n.getSource_type(), n.getSource_type_text(), n.getCreator_id(), n.getCreator_name(),
            n.getContact_person(), n.getContact_phone(), n.getContact_email(),
            toMaintenanceOrderContactAddress(n.getContact_address_full()),
            n.getFault_description(), n.getFault_occurrence_time(), n.getExpected_service_time(),
            n.getCreated_at(), n.getUpdated_at(), n.getSubmission_time(), n.getAccepted_time(), n.getCompleted_time(),
            n.getDevice_sn(), n.getDevice_model_key(), n.getDevice_model_name(), n.getNameplate_sn(),
            n.getModule(), n.getDevice_organization_callsign(),
            toMaintenanceOrderAttachments(n.getAttachments()),
            toMaintenanceOrderStatusHistory(n.getStatus_history()),
            toMaintenanceOrderRepairInfo(n.getRepair_info()),
            toMaintenanceOrderLogReport(n.getLog_report())
        );
    }

    // ==================== 诊断管理模块 转换 ====================
    // TODO: SWIG 生成后启用以下代码;
    // 依赖 SWIG 生成的类型: DiagnosisErrorCodeItemResult, DiagnosisErrorCodeItemResultVector,
    // DiagnosisErrorCodeListResult, DiagnosisMaterialItemResult, DiagnosisMaterialItemResultVector,
    // SelfDiagnosticResultData, DiagnosisDeviceProgressItemResult, DiagnosisDeviceProgressItemResultVector,
    // DiagnosisDeviceReportProgressResult, GetSelfDiagnosisReportResults, GetSelfDiagnosisReportListResults,
    // GetSelfDiagnosisReportResultsVector

    private static com.dji.flighthubsdk.bean.DiagnosisErrorCodeItem toDiagnosisErrorCodeItem(com.dji.flighthubsdk.generated.DiagnosisErrorCodeItemResult n) {
        if (n == null) return null;
        return new com.dji.flighthubsdk.bean.DiagnosisErrorCodeItem(
            n.getError_code(), n.getError_code_describe(), n.getError_code_describe_en(), n.getLevel()
        );
    }

    private static List<com.dji.flighthubsdk.bean.DiagnosisErrorCodeItem> toDiagnosisErrorCodeItemList(com.dji.flighthubsdk.generated.DiagnosisErrorCodeItemResultVector v) {
        if (v == null || v.isEmpty()) return Collections.emptyList();
        List<com.dji.flighthubsdk.bean.DiagnosisErrorCodeItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDiagnosisErrorCodeItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    private static com.dji.flighthubsdk.bean.DiagnosisErrorCodeList toDiagnosisErrorCodeList(com.dji.flighthubsdk.generated.DiagnosisErrorCodeListResult n) {
        if (n == null) return new com.dji.flighthubsdk.bean.DiagnosisErrorCodeList(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return new com.dji.flighthubsdk.bean.DiagnosisErrorCodeList(
            toDiagnosisErrorCodeItemList(n.getNotice()),
            toDiagnosisErrorCodeItemList(n.getWarning()),
            toDiagnosisErrorCodeItemList(n.getAlert())
        );
    }

    private static com.dji.flighthubsdk.bean.DiagnosisMaterialItem toDiagnosisMaterialItem(com.dji.flighthubsdk.generated.DiagnosisMaterialItemResult n) {
        if (n == null) return null;
        return new com.dji.flighthubsdk.bean.DiagnosisMaterialItem(
            toDiagnosisErrorCodeList(n.getError_code_list()),
            n.getMaterial_num(), n.getMaterial_name(), n.getMaterial_probability(), n.getHms_total()
        );
    }

    private static List<com.dji.flighthubsdk.bean.DiagnosisMaterialItem> toDiagnosisMaterialItemList(com.dji.flighthubsdk.generated.DiagnosisMaterialItemResultVector v) {
        if (v == null || v.isEmpty()) return Collections.emptyList();
        List<com.dji.flighthubsdk.bean.DiagnosisMaterialItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDiagnosisMaterialItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    private static com.dji.flighthubsdk.bean.SelfDiagnosticResultData toSelfDiagnosticResultData(com.dji.flighthubsdk.generated.SelfDiagnosticResultData n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SelfDiagnosticResultData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return new com.dji.flighthubsdk.bean.SelfDiagnosticResultData(
            toDiagnosisMaterialItemList(n.getHigh()),
            toDiagnosisMaterialItemList(n.getMid()),
            toDiagnosisMaterialItemList(n.getLow())
        );
    }

    private static com.dji.flighthubsdk.bean.DiagnosisDeviceProgressItem toDiagnosisDeviceProgressItem(com.dji.flighthubsdk.generated.DiagnosisDeviceProgressItemResult n) {
        if (n == null) return null;
        return new com.dji.flighthubsdk.bean.DiagnosisDeviceProgressItem(n.getSn(), n.getStatus(), n.getPercent());
    }

    private static com.dji.flighthubsdk.bean.DiagnosisDeviceReportProgress toDiagnosisDeviceReportProgress(com.dji.flighthubsdk.generated.DiagnosisDeviceReportProgressResult n) {
        if (n == null) return new com.dji.flighthubsdk.bean.DiagnosisDeviceReportProgress(0, "", "", 0, Collections.emptyList());
        List<com.dji.flighthubsdk.bean.DiagnosisDeviceProgressItem> allDevice = Collections.emptyList();
        com.dji.flighthubsdk.generated.DiagnosisDeviceProgressItemResultVector v = n.getAll_device();
        if (v != null && !v.isEmpty()) {
            allDevice = new ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) {
                allDevice.add(toDiagnosisDeviceProgressItem(v.get(i)));
            }
            allDevice = Collections.unmodifiableList(allDevice);
        }
        return new com.dji.flighthubsdk.bean.DiagnosisDeviceReportProgress(
            n.getResult(), n.getSn(), n.getStatus(), n.getPercent(), allDevice
        );
    }

    public static com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults toGetSelfDiagnosisReportResults(com.dji.flighthubsdk.generated.GetSelfDiagnosisReportResults n) {
        if (n == null) return null;
        return new com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults(
            n.getReport_uuid(), n.getReport_user_id(), n.getReport_user_organization_callsign(),
            n.getCreate_time(), n.getComplete_machine_sn(), n.getDevice_sn(), n.getDevice_model_key(),
            n.getDevice_organization_callsign(), n.getStatus(), n.getResult_code(), n.getBid(),
            n.getPart(), n.getOrigin(),
            toSelfDiagnosticResultData(n.getSelf_diagnostic_result()),
            toDiagnosisDeviceReportProgress(n.getDevice_report_progress()),
            n.getQr_code_content(), n.getQr_guide_text()
        );
    }

    public static com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults toGetSelfDiagnosisReportListResults(com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults(new com.dji.flighthubsdk.bean.CommonPaginationData(0, 0, 0), Collections.emptyList());
        com.dji.flighthubsdk.generated.CommonPaginationData pg = n.getPagination();
        com.dji.flighthubsdk.bean.CommonPaginationData pagination = new com.dji.flighthubsdk.bean.CommonPaginationData(pg.getPage(), pg.getPage_size(), pg.getTotal());
        com.dji.flighthubsdk.generated.GetSelfDiagnosisReportResultsVector v = n.getList();
        List<com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults> items = new ArrayList<>();
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                items.add(toGetSelfDiagnosisReportResults(v.get(i)));
            }
        }
        return new com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults(pagination, Collections.unmodifiableList(items));
    }

    // ==================== AI Box 管理相关转换方法 ====================

    public static com.dji.flighthubsdk.bean.AIBoxInfoResult toAIBoxInfoResult(com.dji.flighthubsdk.generated.AIBoxInfoResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.AIBoxInfoResult(0, 0, "", "", 0.0, 0.0);
        return new com.dji.flighthubsdk.bean.AIBoxInfoResult(
            n.getIndex(), n.getType(), safeString(n.getSn()),
            safeString(n.getFirmware_version()), n.getStorage_total(), n.getStorage_used());
    }

    public static com.dji.flighthubsdk.bean.AIBoxInfoListResult toAIBoxInfoListResult(com.dji.flighthubsdk.generated.AIBoxInfoListResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.AIBoxInfoListResult(java.util.Collections.emptyList());
        com.dji.flighthubsdk.generated.AIBoxInfoResultsVector v = n.getAi_boxes();
        if (v == null || v.isEmpty()) return new com.dji.flighthubsdk.bean.AIBoxInfoListResult(java.util.Collections.emptyList());
        java.util.List<com.dji.flighthubsdk.bean.AIBoxInfoResult> list = new java.util.ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) { list.add(toAIBoxInfoResult(v.get(i))); }
        return new com.dji.flighthubsdk.bean.AIBoxInfoListResult(java.util.Collections.unmodifiableList(list));
    }

    public static com.dji.flighthubsdk.bean.FileDownloadUrlListResult toFileDownloadUrlListResult(com.dji.flighthubsdk.generated.FileDownloadUrlListResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.FileDownloadUrlListResult(java.util.Collections.emptyList());
        com.dji.flighthubsdk.generated.StringVector v = n.getList();
        if (v == null || v.isEmpty()) return new com.dji.flighthubsdk.bean.FileDownloadUrlListResult(java.util.Collections.emptyList());
        java.util.List<String> list = new java.util.ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) { list.add(safeString(v.get(i))); }
        return new com.dji.flighthubsdk.bean.FileDownloadUrlListResult(java.util.Collections.unmodifiableList(list));
    }

    public static com.dji.flighthubsdk.bean.FetchAppListResult toFetchAppListResult(com.dji.flighthubsdk.generated.FetchAppListResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.FetchAppListResult("", "");
        return new com.dji.flighthubsdk.bean.FetchAppListResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static com.dji.flighthubsdk.bean.AIBoxAppInfoResult toAIBoxAppInfoResult(com.dji.flighthubsdk.generated.AIBoxAppInfoResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.AIBoxAppInfoResult("", "", "", "", "", 0.0, 0.0, 0);
        return new com.dji.flighthubsdk.bean.AIBoxAppInfoResult(
            safeString(n.getId()), safeString(n.getName()), safeString(n.getVersion()),
            safeString(n.getDescription()), safeString(n.getPublisher()),
            n.getApp_size(), n.getData_size(), n.getType());
    }

    public static com.dji.flighthubsdk.bean.FetchAppListProgressExtResult toFetchAppListProgressExtResult(com.dji.flighthubsdk.generated.FetchAppListProgressExtResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.FetchAppListProgressExtResult("", java.util.Collections.emptyList(), "");
        com.dji.flighthubsdk.generated.AIBoxAppInfoResultsVector v = n.getAi_box_app_info();
        java.util.List<com.dji.flighthubsdk.bean.AIBoxAppInfoResult> appInfoList = java.util.Collections.emptyList();
        if (v != null && !v.isEmpty()) {
            appInfoList = new java.util.ArrayList<>(v.size());
            for (int i = 0; i < v.size(); i++) { appInfoList.add(toAIBoxAppInfoResult(v.get(i))); }
            appInfoList = java.util.Collections.unmodifiableList(appInfoList);
        }
        return new com.dji.flighthubsdk.bean.FetchAppListProgressExtResult(safeString(n.getSn()), appInfoList, safeString(n.getError_reason()));
    }

    public static com.dji.flighthubsdk.bean.FetchAppListProgressResult toFetchAppListProgressResult(com.dji.flighthubsdk.generated.FetchAppListProgressResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.FetchAppListProgressResult("", "", 0, 0L, 0L, 0, null);
        return new com.dji.flighthubsdk.bean.FetchAppListProgressResult(
            safeString(n.getBid()), safeString(n.getStatus()), n.getPercent(),
            n.getCreate_time(), n.getUpdate_time(), n.getDevice_code(),
            toFetchAppListProgressExtResult(n.getExt()));
    }

    public static com.dji.flighthubsdk.bean.ExportFileResult toExportFileResult(com.dji.flighthubsdk.generated.ExportFileResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.ExportFileResult("", "");
        return new com.dji.flighthubsdk.bean.ExportFileResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static com.dji.flighthubsdk.bean.ExportFileProgressExtResult toExportFileProgressExtResult(com.dji.flighthubsdk.generated.ExportFileProgressExtResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.ExportFileProgressExtResult("", 0, "", "");
        return new com.dji.flighthubsdk.bean.ExportFileProgressExtResult(
            safeString(n.getSn()), n.getExport_type(), safeString(n.getApp_id()), safeString(n.getObject_key()));
    }

    public static com.dji.flighthubsdk.bean.ExportFileProgressResult toExportFileProgressResult(com.dji.flighthubsdk.generated.ExportFileProgressResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.ExportFileProgressResult("", "", 0, 0L, 0L, 0, null);
        return new com.dji.flighthubsdk.bean.ExportFileProgressResult(
            safeString(n.getBid()), safeString(n.getStatus()), n.getPercent(),
            n.getCreate_time(), n.getUpdate_time(), n.getDevice_code(),
            toExportFileProgressExtResult(n.getExt()));
    }

    public static com.dji.flighthubsdk.bean.UploadFileResult toUploadFileResult(com.dji.flighthubsdk.generated.UploadFileResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.UploadFileResult("", "", "");
        return new com.dji.flighthubsdk.bean.UploadFileResult(
            safeString(n.getObject_url()), safeString(n.getEtag()), safeString(n.getObject_key()));
    }

    public static com.dji.flighthubsdk.bean.InstallAppResult toInstallAppResult(com.dji.flighthubsdk.generated.InstallAppResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.InstallAppResult("", "");
        return new com.dji.flighthubsdk.bean.InstallAppResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static com.dji.flighthubsdk.bean.InstallAppProgressExtResult toInstallAppProgressExtResult(com.dji.flighthubsdk.generated.InstallAppProgressExtResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.InstallAppProgressExtResult("");
        return new com.dji.flighthubsdk.bean.InstallAppProgressExtResult(safeString(n.getSn()));
    }

    public static com.dji.flighthubsdk.bean.InstallAppProgressResult toInstallAppProgressResult(com.dji.flighthubsdk.generated.InstallAppProgressResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.InstallAppProgressResult("", "", 0, 0L, 0L, 0, null);
        return new com.dji.flighthubsdk.bean.InstallAppProgressResult(
            safeString(n.getBid()), safeString(n.getStatus()), n.getPercent(),
            n.getCreate_time(), n.getUpdate_time(), n.getDevice_code(),
            toInstallAppProgressExtResult(n.getExt()));
    }

    public static com.dji.flighthubsdk.bean.OperateAppResult toOperateAppResult(com.dji.flighthubsdk.generated.OperateAppResults n) {
        if (n == null) return new com.dji.flighthubsdk.bean.OperateAppResult("", "");
        return new com.dji.flighthubsdk.bean.OperateAppResult(safeString(n.getBid()), safeString(n.getStatus()));
    }

    public static com.dji.flighthubsdk.generated.FetchFileDownloadUrlParams toNativeFetchFileDownloadUrlParam(com.dji.flighthubsdk.bean.FetchFileDownloadUrlParam param) {
        com.dji.flighthubsdk.generated.FetchFileDownloadUrlParams n = new com.dji.flighthubsdk.generated.FetchFileDownloadUrlParams();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        com.dji.flighthubsdk.generated.StringVector keys = new com.dji.flighthubsdk.generated.StringVector();
        if (param.getObjectKeys() != null) { for (String k : param.getObjectKeys()) { keys.add(k != null ? k : ""); } }
        n.setObject_keys(keys);
        return n;
    }

    public static com.dji.flighthubsdk.generated.FetchAppListParam toNativeFetchAppListParam(com.dji.flighthubsdk.bean.AIBoxFetchAppListParam param) {
        com.dji.flighthubsdk.generated.FetchAppListParam n = new com.dji.flighthubsdk.generated.FetchAppListParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.FetchAppListProgressParam toNativeFetchAppListProgressParam(com.dji.flighthubsdk.bean.AIBoxFetchAppListProgressParam param) {
        com.dji.flighthubsdk.generated.FetchAppListProgressParam n = new com.dji.flighthubsdk.generated.FetchAppListProgressParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        n.setBid(param.getBid() != null ? param.getBid() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.ExportFileParam toNativeExportFileParam(com.dji.flighthubsdk.bean.AIBoxExportFileParam param) {
        com.dji.flighthubsdk.generated.ExportFileParam n = new com.dji.flighthubsdk.generated.ExportFileParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        n.setApp_id(param.getAppId() != null ? param.getAppId() : "");
        n.setExport_type(param.getExportType());
        return n;
    }

    public static com.dji.flighthubsdk.generated.ExportFileProgressParam toNativeExportFileProgressParam(com.dji.flighthubsdk.bean.AIBoxExportFileProgressParam param) {
        com.dji.flighthubsdk.generated.ExportFileProgressParam n = new com.dji.flighthubsdk.generated.ExportFileProgressParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        n.setBid(param.getBid() != null ? param.getBid() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.InstallAppParam toNativeInstallAppParam(com.dji.flighthubsdk.bean.AIBoxInstallAppParam param) {
        com.dji.flighthubsdk.generated.InstallAppParam n = new com.dji.flighthubsdk.generated.InstallAppParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        n.setObject_key(param.getObjectKey() != null ? param.getObjectKey() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.InstallAppProgressParam toNativeInstallAppProgressParam(com.dji.flighthubsdk.bean.AIBoxInstallAppProgressParam param) {
        com.dji.flighthubsdk.generated.InstallAppProgressParam n = new com.dji.flighthubsdk.generated.InstallAppProgressParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        n.setBid(param.getBid() != null ? param.getBid() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.OperateAppParam toNativeOperateAppParam(com.dji.flighthubsdk.bean.AIBoxOperateAppParam param) {
        com.dji.flighthubsdk.generated.OperateAppParam n = new com.dji.flighthubsdk.generated.OperateAppParam();
        if (param == null) return n;
        n.setDevice_sn(param.getDeviceSn() != null ? param.getDeviceSn() : "");
        n.setBox_sn(param.getBoxSn() != null ? param.getBoxSn() : "");
        n.setApp_id(param.getAppId() != null ? param.getAppId() : "");
        n.setAction(param.getAction() != null ? param.getAction() : "");
        return n;
    }

    // ── Diagnosis Management ──

    public static com.dji.flighthubsdk.generated.OperateSelfDiagnosisParams toNativeOperateSelfDiagnosisParams(com.dji.flighthubsdk.bean.OperateSelfDiagnosisParams p) {
        com.dji.flighthubsdk.generated.OperateSelfDiagnosisParams n = new com.dji.flighthubsdk.generated.OperateSelfDiagnosisParams();
        if (p == null) return n;
        n.setReport_uuid(p.getReportUuid() != null ? p.getReportUuid() : "");
        n.setReport_cmd_method(p.getReportCmdMethod() != null ? p.getReportCmdMethod() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListParams toNativeGetSelfDiagnosisReportListParams(com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListParams p) {
        com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListParams n = new com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListParams();
        if (p == null) return n;
        n.setPage(p.getPage());
        n.setPage_size(p.getPageSize());
        n.setStatus(toNativeStringVector(p.getStatus()));
        n.setDevice_sn(p.getDeviceSn() != null ? p.getDeviceSn() : "");
        n.setReport_time_begin(p.getReportTimeBegin() != null ? p.getReportTimeBegin() : "");
        n.setReport_time_end(p.getReportTimeEnd() != null ? p.getReportTimeEnd() : "");
        n.setPart(p.getPart());
        n.setOrigin(p.getOrigin());
        n.setQuery(p.getQuery() != null ? p.getQuery() : "");
        n.setReport_user(toNativeStringVector(p.getReportUser()));
        n.setSort_by(p.getSortBy() != null ? p.getSortBy() : "");
        n.setSort_type(p.getSortType() != null ? p.getSortType() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.CreateSelfDiagnosisParams toNativeCreateSelfDiagnosisParams(com.dji.flighthubsdk.bean.CreateSelfDiagnosisParams p) {
        com.dji.flighthubsdk.generated.CreateSelfDiagnosisParams n = new com.dji.flighthubsdk.generated.CreateSelfDiagnosisParams();
        if (p == null) return n;
        n.setDevice_sn(p.getDeviceSn() != null ? p.getDeviceSn() : "");
        n.setType(p.getType());
        n.setPart(p.getPart());
        n.setIndex(p.getIndex());
        n.setMotor_action(p.getMotorAction());
        if (p.getDeviceTopo() != null) {
            n.setDevice_topo(toNativeSelfDiagnosisDeviceTopoParams(p.getDeviceTopo()));
        }
        return n;
    }

    private static com.dji.flighthubsdk.generated.SelfDiagnosisDeviceTopoParams toNativeSelfDiagnosisDeviceTopoParams(com.dji.flighthubsdk.bean.SelfDiagnosisDeviceTopoParams p) {
        com.dji.flighthubsdk.generated.SelfDiagnosisDeviceTopoParams n = new com.dji.flighthubsdk.generated.SelfDiagnosisDeviceTopoParams();
        if (p == null) return n;
        n.setIndex(p.getIndex() != null ? p.getIndex() : "");
        n.setHosts(toNativeSelfDiagnosisTopoNodeVector(p.getHosts()));
        n.setParents(toNativeSelfDiagnosisTopoNodeVector(p.getParents()));
        n.setNodes(toNativeSelfDiagnosisTopoNodeVector(p.getNodes()));
        n.setOthers(toNativeSelfDiagnosisTopoNodeVector(p.getOthers()));
        n.setDocks(toNativeSelfDiagnosisTopoNodeVector(p.getDocks()));
        return n;
    }

    private static com.dji.flighthubsdk.generated.SelfDiagnosisTopoNodeParamsVector toNativeSelfDiagnosisTopoNodeVector(java.util.List<com.dji.flighthubsdk.bean.SelfDiagnosisTopoNodeParams> list) {
        com.dji.flighthubsdk.generated.SelfDiagnosisTopoNodeParamsVector v = new com.dji.flighthubsdk.generated.SelfDiagnosisTopoNodeParamsVector();
        if (list == null) return v;
        for (com.dji.flighthubsdk.bean.SelfDiagnosisTopoNodeParams item : list) {
            com.dji.flighthubsdk.generated.SelfDiagnosisTopoNodeParams n = new com.dji.flighthubsdk.generated.SelfDiagnosisTopoNodeParams();
            n.setSn(item.getSn() != null ? item.getSn() : "");
            n.setType(item.getType() != null ? item.getType() : "");
            n.setDomain(item.getDomain());
            n.setDevice_model(item.getDeviceModel() != null ? item.getDeviceModel() : "");
            v.add(n);
        }
        return v;
    }

    // ── Maintenance Management ──

    public static com.dji.flighthubsdk.generated.GetDeviceRepairConfigParams toNativeGetDeviceRepairConfigParams(com.dji.flighthubsdk.bean.GetDeviceRepairConfigParams p) {
        com.dji.flighthubsdk.generated.GetDeviceRepairConfigParams n = new com.dji.flighthubsdk.generated.GetDeviceRepairConfigParams();
        if (p == null) return n;
        n.setDevice_sns(toNativeStringVector(p.getDeviceSns()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.GetMaintenanceOrdersListParams toNativeGetMaintenanceOrdersListParams(com.dji.flighthubsdk.bean.GetMaintenanceOrdersListParams p) {
        com.dji.flighthubsdk.generated.GetMaintenanceOrdersListParams n = new com.dji.flighthubsdk.generated.GetMaintenanceOrdersListParams();
        if (p == null) return n;
        n.setPage(p.getPage());
        n.setPage_size(p.getPageSize());
        n.setOrder_number(p.getOrderNumber() != null ? p.getOrderNumber() : "");
        n.setStatus(p.getStatus() != null ? p.getStatus() : "");
        n.setInitiated_from(p.getInitiatedFrom() != null ? p.getInitiatedFrom() : "");
        n.setDevice_sn(p.getDeviceSn() != null ? p.getDeviceSn() : "");
        n.setCreator_ids(toNativeStringVector(p.getCreatorIds()));
        n.setKey_word(p.getKeyWord() != null ? p.getKeyWord() : "");
        n.setCreated_at_start(p.getCreatedAtStart() != null ? p.getCreatedAtStart() : "");
        n.setCreated_at_end(p.getCreatedAtEnd() != null ? p.getCreatedAtEnd() : "");
        n.setSort_column(p.getSortColumn() != null ? p.getSortColumn() : "");
        n.setSort_type(p.getSortType() != null ? p.getSortType() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.CreateMaintenanceOrderParams toNativeCreateMaintenanceOrderParams(com.dji.flighthubsdk.bean.CreateMaintenanceOrderParams p) {
        com.dji.flighthubsdk.generated.CreateMaintenanceOrderParams n = new com.dji.flighthubsdk.generated.CreateMaintenanceOrderParams();
        if (p == null) return n;
        n.setFault_occurrence_time(p.getFaultOccurrenceTime() != null ? p.getFaultOccurrenceTime() : "");
        n.setDevice_list(toNativeMaintenanceOrderDeviceItemVector(p.getDeviceList()));
        n.setFault_description(p.getFaultDescription() != null ? p.getFaultDescription() : "");
        n.setAttachments(toNativeMaintenanceOrderAttachmentsVector(p.getAttachments()));
        n.setContact_person(p.getContactPerson() != null ? p.getContactPerson() : "");
        n.setContact_phone(p.getContactPhone() != null ? p.getContactPhone() : "");
        n.setContact_email(p.getContactEmail() != null ? p.getContactEmail() : "");
        if (p.getContactAddress() != null) {
            n.setContact_address(toNativeMaintenanceOrderContactAddressParams(p.getContactAddress()));
        }
        n.setExpected_service_time(p.getExpectedServiceTime() != null ? p.getExpectedServiceTime() : "");
        n.setReport_uuid(p.getReportUuid() != null ? p.getReportUuid() : "");
        if (p.getDeviceTopo() != null) {
            n.setDevice_topo(toNativeMaintenanceOrderDeviceTopoParams(p.getDeviceTopo()));
        }
        n.setLog_list(toNativeMaintenanceOrderLogInfoVector(p.getLogList()));
        return n;
    }

    private static com.dji.flighthubsdk.generated.MaintenanceOrderContactAddressParams toNativeMaintenanceOrderContactAddressParams(com.dji.flighthubsdk.bean.MaintenanceOrderContactAddressParams p) {
        com.dji.flighthubsdk.generated.MaintenanceOrderContactAddressParams n = new com.dji.flighthubsdk.generated.MaintenanceOrderContactAddressParams();
        if (p == null) return n;
        n.setCountry(p.getCountry() != null ? p.getCountry() : "");
        n.setProvince(p.getProvince() != null ? p.getProvince() : "");
        n.setCity(p.getCity() != null ? p.getCity() : "");
        n.setDistrict(p.getDistrict() != null ? p.getDistrict() : "");
        n.setDetail(p.getDetail() != null ? p.getDetail() : "");
        return n;
    }

    private static com.dji.flighthubsdk.generated.MaintenanceOrderDeviceTopoParams toNativeMaintenanceOrderDeviceTopoParams(com.dji.flighthubsdk.bean.MaintenanceOrderDeviceTopoParams p) {
        com.dji.flighthubsdk.generated.MaintenanceOrderDeviceTopoParams n = new com.dji.flighthubsdk.generated.MaintenanceOrderDeviceTopoParams();
        if (p == null) return n;
        n.setHosts(toNativeMaintenanceOrderTopoNodeVector(p.getHosts()));
        n.setParents(toNativeMaintenanceOrderTopoNodeVector(p.getParents()));
        n.setNodes(toNativeMaintenanceOrderTopoNodeVector(p.getNodes()));
        return n;
    }

    private static com.dji.flighthubsdk.generated.MaintenanceOrderTopoNodeParamsVector toNativeMaintenanceOrderTopoNodeVector(java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams> list) {
        com.dji.flighthubsdk.generated.MaintenanceOrderTopoNodeParamsVector v = new com.dji.flighthubsdk.generated.MaintenanceOrderTopoNodeParamsVector();
        if (list == null) return v;
        for (com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams item : list) {
            com.dji.flighthubsdk.generated.MaintenanceOrderTopoNodeParams n = new com.dji.flighthubsdk.generated.MaintenanceOrderTopoNodeParams();
            n.setSn(item.getSn() != null ? item.getSn() : "");
            n.setDevice_model_key(item.getDeviceModelKey() != null ? item.getDeviceModelKey() : "");
            n.setOnline_status(item.isOnlineStatus());
            v.add(n);
        }
        return v;
    }

    private static com.dji.flighthubsdk.generated.MaintenanceOrderDeviceItemParamsVector toNativeMaintenanceOrderDeviceItemVector(java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams> list) {
        com.dji.flighthubsdk.generated.MaintenanceOrderDeviceItemParamsVector v = new com.dji.flighthubsdk.generated.MaintenanceOrderDeviceItemParamsVector();
        if (list == null) return v;
        for (com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams item : list) {
            com.dji.flighthubsdk.generated.MaintenanceOrderDeviceItemParams n = new com.dji.flighthubsdk.generated.MaintenanceOrderDeviceItemParams();
            n.setDevice_model_key(item.getDeviceModelKey() != null ? item.getDeviceModelKey() : "");
            n.setDevice_sn(item.getDeviceSn() != null ? item.getDeviceSn() : "");
            n.setModule(item.getModule() != null ? item.getModule() : "");
            n.setNameplate_sn(item.getNameplateSn() != null ? item.getNameplateSn() : "");
            n.setSource_type(item.getSourceType());
            n.setSource_ref_id(item.getSourceRefId() != null ? item.getSourceRefId() : "");
            v.add(n);
        }
        return v;
    }

    private static com.dji.flighthubsdk.generated.MaintenanceOrderAttachmentsParamsVector toNativeMaintenanceOrderAttachmentsVector(java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderAttachmentsParams> list) {
        com.dji.flighthubsdk.generated.MaintenanceOrderAttachmentsParamsVector v = new com.dji.flighthubsdk.generated.MaintenanceOrderAttachmentsParamsVector();
        if (list == null) return v;
        for (com.dji.flighthubsdk.bean.MaintenanceOrderAttachmentsParams item : list) {
            com.dji.flighthubsdk.generated.MaintenanceOrderAttachmentsParams n = new com.dji.flighthubsdk.generated.MaintenanceOrderAttachmentsParams();
            n.setFile_name(item.getFileName() != null ? item.getFileName() : "");
            n.setFile_key(item.getFileKey() != null ? item.getFileKey() : "");
            n.setFile_size(item.getFileSize());
            v.add(n);
        }
        return v;
    }

    private static com.dji.flighthubsdk.generated.MaintenanceOrderLogInfoParamsVector toNativeMaintenanceOrderLogInfoVector(java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderLogInfoParams> list) {
        com.dji.flighthubsdk.generated.MaintenanceOrderLogInfoParamsVector v = new com.dji.flighthubsdk.generated.MaintenanceOrderLogInfoParamsVector();
        if (list == null) return v;
        for (com.dji.flighthubsdk.bean.MaintenanceOrderLogInfoParams item : list) {
            com.dji.flighthubsdk.generated.MaintenanceOrderLogInfoParams n = new com.dji.flighthubsdk.generated.MaintenanceOrderLogInfoParams();
            n.setDevice_sn(item.getDeviceSn() != null ? item.getDeviceSn() : "");
            n.setModule(item.getModule() != null ? item.getModule() : "");
            n.setObject_key(item.getObjectKey() != null ? item.getObjectKey() : "");
            v.add(n);
        }
        return v;
    }

    // ==================== SEI 转换 ====================

    public static com.dji.flighthubsdk.bean.SeiHeader toSeiHeader(com.dji.flighthubsdk.generated.SeiHeader n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiHeader();
        return new com.dji.flighthubsdk.bean.SeiHeader(
                n.getVersion(), n.getReserved(), n.getData_size(), n.getTimestamp());
    }

    public static com.dji.flighthubsdk.bean.SeiData toSeiData(com.dji.flighthubsdk.generated.SeiData n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiData();
        com.dji.flighthubsdk.generated.IntVector v = n.getData();
        java.util.List<Integer> list = new java.util.ArrayList<>();
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                list.add(v.get(i));
            }
        }
        return new com.dji.flighthubsdk.bean.SeiData(list);
    }

    public static com.dji.flighthubsdk.bean.SeiPoint2D toSeiPoint2D(com.dji.flighthubsdk.generated.SeiPoint2D n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiPoint2D();
        return new com.dji.flighthubsdk.bean.SeiPoint2D(n.getX(), n.getY());
    }

    public static com.dji.flighthubsdk.bean.SeiDetectionBox toSeiDetectionBox(com.dji.flighthubsdk.generated.SeiDetectionBox n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiDetectionBox();
        return new com.dji.flighthubsdk.bean.SeiDetectionBox(
                n.getId(), n.getType(), n.getState(),
                n.getX(), n.getY(), n.getWidth(), n.getHeight(),
                n.getConfidence());
    }

    public static com.dji.flighthubsdk.bean.SeiDetectionPolygon toSeiDetectionPolygon(com.dji.flighthubsdk.generated.SeiDetectionPolygon n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiDetectionPolygon();
        com.dji.flighthubsdk.generated.SeiPoint2DVector pv = n.getPoints();
        java.util.List<com.dji.flighthubsdk.bean.SeiPoint2D> points = new java.util.ArrayList<>();
        if (pv != null) {
            for (int i = 0; i < pv.size(); i++) {
                points.add(toSeiPoint2D(pv.get(i)));
            }
        }
        return new com.dji.flighthubsdk.bean.SeiDetectionPolygon(
                n.getId(), n.getType(), n.getState(), n.getConfidence(), points);
    }

    public static com.dji.flighthubsdk.bean.SeiTextOverlay toSeiTextOverlay(com.dji.flighthubsdk.generated.SeiTextOverlay n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiTextOverlay();
        return new com.dji.flighthubsdk.bean.SeiTextOverlay(
                n.getId(), n.getX(), n.getY(), n.getFont_size(),
                n.getColor_r(), n.getColor_g(), n.getColor_b(),
                n.getAlpha(), n.getText());
    }

    public static com.dji.flighthubsdk.bean.SeiItem toSeiItem(com.dji.flighthubsdk.generated.SeiItem n) {
        if (n == null) return new com.dji.flighthubsdk.bean.SeiItem();
        com.dji.flighthubsdk.value.SeiDataType type = com.dji.flighthubsdk.value.SeiDataType.fromValue(n.getType());
        switch (type) {
            case CUSTOM_DATA:
                return com.dji.flighthubsdk.bean.SeiItem.ofCustomData(toSeiData(n.getCustom_data()));
            case DETECTION_BOX:
                return com.dji.flighthubsdk.bean.SeiItem.ofDetectionBox(toSeiDetectionBox(n.getDetection_box()));
            case DETECTION_POLYGON:
                return com.dji.flighthubsdk.bean.SeiItem.ofDetectionPolygon(toSeiDetectionPolygon(n.getDetection_polygon()));
            case TEXT_OVERLAY:
                return com.dji.flighthubsdk.bean.SeiItem.ofTextOverlay(toSeiTextOverlay(n.getText_overlay()));
            default:
                com.dji.flighthubsdk.bean.SeiItem item = new com.dji.flighthubsdk.bean.SeiItem();
                item.setDataType(com.dji.flighthubsdk.value.SeiDataType.UNKNOWN);
                return item;
        }
    }

    public static java.util.List<com.dji.flighthubsdk.bean.SeiItem> toSeiItemList(com.dji.flighthubsdk.generated.SeiItemVector nv) {
        if (nv == null || nv.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<com.dji.flighthubsdk.bean.SeiItem> out = new java.util.ArrayList<>((int) nv.size());
        for (int i = 0; i < nv.size(); i++) {
            out.add(toSeiItem(nv.get(i)));
        }
        return out;
    }

    // ---- bean→native（encode 方向） ----

    public static com.dji.flighthubsdk.generated.SeiData toNativeSeiData(com.dji.flighthubsdk.bean.SeiData bean) {
        com.dji.flighthubsdk.generated.SeiData n = new com.dji.flighthubsdk.generated.SeiData();
        if (bean != null && bean.getData() != null) {
            com.dji.flighthubsdk.generated.IntVector v = new com.dji.flighthubsdk.generated.IntVector();
            for (Integer val : bean.getData()) {
                v.add(val);
            }
            n.setData(v);
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.SeiPoint2D toNativeSeiPoint2D(com.dji.flighthubsdk.bean.SeiPoint2D bean) {
        com.dji.flighthubsdk.generated.SeiPoint2D n = new com.dji.flighthubsdk.generated.SeiPoint2D();
        if (bean != null) {
            n.setX(bean.getX());
            n.setY(bean.getY());
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.SeiDetectionBox toNativeSeiDetectionBox(com.dji.flighthubsdk.bean.SeiDetectionBox bean) {
        com.dji.flighthubsdk.generated.SeiDetectionBox n = new com.dji.flighthubsdk.generated.SeiDetectionBox();
        if (bean != null) {
            n.setId(bean.getId());
            n.setType(bean.getType());
            n.setState(bean.getState());
            n.setX(bean.getX());
            n.setY(bean.getY());
            n.setWidth(bean.getWidth());
            n.setHeight(bean.getHeight());
            n.setConfidence(bean.getConfidence());
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.SeiDetectionPolygon toNativeSeiDetectionPolygon(com.dji.flighthubsdk.bean.SeiDetectionPolygon bean) {
        com.dji.flighthubsdk.generated.SeiDetectionPolygon n = new com.dji.flighthubsdk.generated.SeiDetectionPolygon();
        if (bean != null) {
            n.setId(bean.getId());
            n.setType(bean.getType());
            n.setState(bean.getState());
            n.setConfidence(bean.getConfidence());
            if (bean.getPoints() != null) {
                com.dji.flighthubsdk.generated.SeiPoint2DVector pv = new com.dji.flighthubsdk.generated.SeiPoint2DVector();
                for (com.dji.flighthubsdk.bean.SeiPoint2D p : bean.getPoints()) {
                    pv.add(toNativeSeiPoint2D(p));
                }
                n.setPoints(pv);
            }
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.SeiTextOverlay toNativeSeiTextOverlay(com.dji.flighthubsdk.bean.SeiTextOverlay bean) {
        com.dji.flighthubsdk.generated.SeiTextOverlay n = new com.dji.flighthubsdk.generated.SeiTextOverlay();
        if (bean != null) {
            n.setId(bean.getId());
            n.setX(bean.getX());
            n.setY(bean.getY());
            n.setFont_size(bean.getFontSize());
            n.setColor_r(bean.getColorR());
            n.setColor_g(bean.getColorG());
            n.setColor_b(bean.getColorB());
            n.setAlpha(bean.getAlpha());
            n.setText(bean.getText() != null ? bean.getText() : "");
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.SeiItem toNativeSeiItem(com.dji.flighthubsdk.bean.SeiItem bean) {
        if (bean == null) return new com.dji.flighthubsdk.generated.SeiItem(new com.dji.flighthubsdk.generated.SeiData());
        com.dji.flighthubsdk.value.SeiDataType type = bean.getDataType();
        if (type == null) type = com.dji.flighthubsdk.value.SeiDataType.UNKNOWN;
        switch (type) {
            case CUSTOM_DATA:
                return new com.dji.flighthubsdk.generated.SeiItem(toNativeSeiData(bean.getCustomData()));
            case DETECTION_BOX:
                return new com.dji.flighthubsdk.generated.SeiItem(toNativeSeiDetectionBox(bean.getDetectionBox()));
            case DETECTION_POLYGON:
                return new com.dji.flighthubsdk.generated.SeiItem(toNativeSeiDetectionPolygon(bean.getDetectionPolygon()));
            case TEXT_OVERLAY:
                return new com.dji.flighthubsdk.generated.SeiItem(toNativeSeiTextOverlay(bean.getTextOverlay()));
            default:
                return new com.dji.flighthubsdk.generated.SeiItem(new com.dji.flighthubsdk.generated.SeiData());
        }
    }

    public static com.dji.flighthubsdk.generated.SeiItemVector toNativeSeiItemVector(java.util.List<com.dji.flighthubsdk.bean.SeiItem> list) {
        com.dji.flighthubsdk.generated.SeiItemVector v = new com.dji.flighthubsdk.generated.SeiItemVector();
        if (list != null) {
            for (com.dji.flighthubsdk.bean.SeiItem item : list) {
                v.add(toNativeSeiItem(item));
            }
        }
        return v;
    }
    // ========== TerraBuild 转换方法 ==========

    public static TerraBuildFolderInfo toTerraBuildFolderInfo(com.dji.flighthubsdk.generated.TerraBuildFolderInfo n) {
        if (n == null) return null;
        return new TerraBuildFolderInfo(n.getFolder_id(), safeString(n.getFolder_uuid()), safeString(n.getFolder_name()));
    }

    private static java.util.List<TerraBuildFolderInfo> toTerraBuildFolderInfoList(com.dji.flighthubsdk.generated.TerraBuildFolderInfoVector v) {
        if (v == null || v.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<TerraBuildFolderInfo> list = new java.util.ArrayList<>();
        for (int i = 0; i < v.size(); i++) list.add(toTerraBuildFolderInfo(v.get(i)));
        return list;
    }

    private static java.util.List<Integer> toIntList(com.dji.flighthubsdk.generated.IntVector v) {
        if (v == null || v.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < v.size(); i++) list.add(v.get(i));
        return list;
    }

    private static java.util.List<double[]> toPolygonPointList(com.dji.flighthubsdk.generated.ReconstructionPolygonPointVector v) {
        if (v == null || v.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<double[]> list = new java.util.ArrayList<>();
        for (int i = 0; i < v.size(); i++) {
            com.dji.flighthubsdk.generated.ReconstructionPolygonPoint p = v.get(i);
            if (p != null) list.add(new double[]{p.getLat(), p.getLng()});
        }
        return list;
    }

    private static com.dji.flighthubsdk.bean.TerraBuildPredefineArea toTerraBuildPredefineArea(
            com.dji.flighthubsdk.generated.ReconPredefineArea n) {
        if (n == null) return null;
        java.util.List<double[]> pts = toPolygonPointList(n.getPolygon_points());
        return new com.dji.flighthubsdk.bean.TerraBuildPredefineArea(pts);
    }

    public static TerraBuildSettingsDetail toTerraBuildSettingsDetail(com.dji.flighthubsdk.generated.TerraBuildSettingsDetail n) {
        if (n == null) return null;
        return new TerraBuildSettingsDetail(
                safeString(n.getName()),
                toIntList(n.getReconstruction_type()),
                n.getSimplified_factor(),
                n.getReconstruction_mode(),
                toTerraBuildPredefineArea(n.getPredefine_area()),
                toIntList(n.getGenerate_model_formats()),
                safeString(n.getWkt()),
                n.getQuality_level(),
                n.getQuality_level_3dgs());
    }

    public static TerraBuildSettingsResult toTerraBuildSettingsResult(com.dji.flighthubsdk.generated.TerraBuildSettingsResult n) {
        if (n == null) return new TerraBuildSettingsResult(0, null, "", "", java.util.Collections.emptyList());
        return new TerraBuildSettingsResult(
                n.getState(),
                toTerraBuildSettingsDetail(n.getReconstruction_settings()),
                safeString(n.getMedia_input_object_key()),
                safeString(n.getSfm_output_object_prefix()),
                toTerraBuildFolderInfoList(n.getFolder_list()));
    }

    public static TerraBuildQualityReportResult toTerraBuildQualityReportResult(com.dji.flighthubsdk.generated.TerraBuildQualityReportResult n) {
        if (n == null) return new TerraBuildQualityReportResult("");
        return new TerraBuildQualityReportResult(safeString(n.getQuality_report_url()));
    }

    public static TerraBuildModelFileItem toTerraBuildModelFileItem(com.dji.flighthubsdk.generated.TerraBuildModelFileItem n) {
        if (n == null) return null;
        return new TerraBuildModelFileItem(
                n.getId(),
                safeString(n.getUuid()),
                safeString(n.getName()),
                n.getFile_type(),
                n.getSub_file_type(),
                n.getState(),
                toIntList(n.getReconstruction_type()),
                safeString(n.getThumbnail_url()),
                safeString(n.getZip_url()),
                safeString(n.getCreated_at()),
                safeString(n.getUpdated_at()));
    }

    public static TerraBuildModelFileListResult toTerraBuildModelFileListResult(com.dji.flighthubsdk.generated.TerraBuildModelFileListResult n) {
        if (n == null) return new TerraBuildModelFileListResult(java.util.Collections.emptyList(), 0, 0, 0);
        com.dji.flighthubsdk.generated.TerraBuildModelFileItemVector v = n.getList();
        java.util.List<TerraBuildModelFileItem> list = new java.util.ArrayList<>();
        if (v != null) for (int i = 0; i < v.size(); i++) list.add(toTerraBuildModelFileItem(v.get(i)));
        return new TerraBuildModelFileListResult(list, n.getTotal(), n.getPage(), n.getPage_size());
    }

    public static TerraBuildModelFileUrlResult toTerraBuildModelFileUrlResult(com.dji.flighthubsdk.generated.TerraBuildModelFileUrlResult n) {
        if (n == null) return new TerraBuildModelFileUrlResult("");
        return new TerraBuildModelFileUrlResult(safeString(n.getUrl()));
    }

    public static TerraBuildSfmResult toTerraBuildSfmResult(com.dji.flighthubsdk.generated.TerraBuildSfmResult n) {
        if (n == null) return new TerraBuildSfmResult("", "", "", java.util.Collections.emptyList());
        com.dji.flighthubsdk.generated.StringVector sv = n.getRe_projection_error_pic_url_list();
        java.util.List<String> urls = new java.util.ArrayList<>();
        if (sv != null) for (int i = 0; i < sv.size(); i++) urls.add(sv.get(i));
        return new TerraBuildSfmResult(
                safeString(n.getSfm_json_url()),
                safeString(n.getSfm_report_json_url()),
                safeString(n.getGcp_out_json_url()),
                urls);
    }

    public static TerraBuildCreateReconstructionResult toTerraBuildCreateReconstructionResult(com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionResult n) {
        if (n == null) return new TerraBuildCreateReconstructionResult(0, "", "");
        return new TerraBuildCreateReconstructionResult(n.getId(), safeString(n.getUuid()), safeString(n.getJob_uuid()));
    }

    public static TerraBuildCreatePanoramaResult toTerraBuildCreatePanoramaResult(com.dji.flighthubsdk.generated.TerraBuildCreatePanoramaResult n) {
        if (n == null) return new TerraBuildCreatePanoramaResult(0, "", "");
        return new TerraBuildCreatePanoramaResult(n.getId(), safeString(n.getUuid()), safeString(n.getJob_uuid()));
    }

    public static TerraBuildReconstructionImageItem toTerraBuildReconstructionImageItem(com.dji.flighthubsdk.generated.TerraBuildReconstructionImageItem n) {
        if (n == null) return null;
        return new TerraBuildReconstructionImageItem(
                safeString(n.getImage_id()),
                safeString(n.getFile_name()),
                safeString(n.getThumbnail_image_url()),
                safeString(n.getOrigin_image_url()));
    }

    public static TerraBuildReconstructionImagesResult toTerraBuildReconstructionImagesResult(com.dji.flighthubsdk.generated.TerraBuildReconstructionImagesResult n) {
        if (n == null) return new TerraBuildReconstructionImagesResult(java.util.Collections.emptyList(), 0);
        com.dji.flighthubsdk.generated.TerraBuildReconstructionImageItemVector v = n.getList();
        java.util.List<TerraBuildReconstructionImageItem> list = new java.util.ArrayList<>();
        if (v != null) for (int i = 0; i < v.size(); i++) list.add(toTerraBuildReconstructionImageItem(v.get(i)));
        return new TerraBuildReconstructionImagesResult(list, n.getTotal());
    }

    public static TerraBuildJobInfo toTerraBuildJobInfo(com.dji.flighthubsdk.generated.TerraBuildJobInfo n) {
        if (n == null) return null;
        return new TerraBuildJobInfo(n.getType(), safeString(n.getUuid()), n.getJob_status(), n.getPercent(), n.getCode());
    }

    public static TerraBuildReconstructionStatusChanged toTerraBuildReconstructionStatusChanged(com.dji.flighthubsdk.generated.TerraBuildReconstructionStatusChanged n) {
        if (n == null) return null;
        com.dji.flighthubsdk.generated.TerraBuildJobInfoVector jv = n.getJob_infos();
        java.util.List<TerraBuildJobInfo> jobInfos = new java.util.ArrayList<>();
        if (jv != null) for (int i = 0; i < jv.size(); i++) jobInfos.add(toTerraBuildJobInfo(jv.get(i)));
        return new TerraBuildReconstructionStatusChanged(
                safeString(n.getWorkspace_id()),
                n.getId(),
                safeString(n.getName()),
                n.getState(),
                safeString(n.getUuid()),
                n.getFile_type(),
                n.getSub_file_type(),
                n.getCode(),
                n.getSize(),
                toTerraBuildJobInfo(n.getJob_info()),
                jobInfos,
                n.getFast_reconstruction_id());
    }

    public static com.dji.flighthubsdk.generated.CreateReconstructionJavaParams toNativeCreateReconstructionParams(TerraBuildCreateReconstructionParams params) {
        com.dji.flighthubsdk.generated.CreateReconstructionJavaParams n = new com.dji.flighthubsdk.generated.CreateReconstructionJavaParams();
        if (params == null) return n;
        n.setName(params.getName() != null ? params.getName() : "");
        n.setWkt(params.getWkt() != null ? params.getWkt() : "");
        n.setSimplified_factor(params.getSimplifiedFactor());
        n.setSimplified_factor_set(params.isSimplifiedFactorSet());
        n.setQuality_level(params.getQualityLevel());
        n.setQuality_level_set(params.isQualityLevelSet());
        n.setQuality_level_3dgs(params.getQualityLevel3dgs());
        n.setQuality_level_3dgs_set(params.isQualityLevel3dgsSet());
        n.setIs_shown_on_map(params.isShownOnMap());
        n.setIs_shown_on_map_set(params.isShownOnMapSet());
        n.setOptimize_SfM(params.isOptimizeSfM());
        n.setOptimize_SfM_set(params.isOptimizeSfMSet());
        n.setCustom_storage_parent_id(params.getCustomStorageParentId());
        n.setCustom_storage_parent_id_set(params.isCustomStorageParentIdSet());
        n.setQuality_report_language(params.getQualityReportLanguage() != null ? params.getQualityReportLanguage() : "");
        n.setQuality_report_measure_unit_system(params.getQualityReportMeasureUnitSystem() != null ? params.getQualityReportMeasureUnitSystem() : "");
        // reconstruction_type
        com.dji.flighthubsdk.generated.IntVector rtv = new com.dji.flighthubsdk.generated.IntVector();
        if (params.getReconstructionType() != null) for (int v : params.getReconstructionType()) rtv.add(v);
        n.setReconstruction_type(rtv);
        // folder_uuids
        com.dji.flighthubsdk.generated.StringVector fuuids = new com.dji.flighthubsdk.generated.StringVector();
        if (params.getFolderUuids() != null) for (String s : params.getFolderUuids()) fuuids.add(s);
        n.setFolder_uuids(fuuids);
        // folder_ids
        com.dji.flighthubsdk.generated.IntVector fids = new com.dji.flighthubsdk.generated.IntVector();
        if (params.getFolderIds() != null) for (int v : params.getFolderIds()) fids.add(v);
        n.setFolder_ids(fids);
        // generate_model_formats
        com.dji.flighthubsdk.generated.IntVector gmf = new com.dji.flighthubsdk.generated.IntVector();
        if (params.getGenerateModelFormats() != null) for (int v : params.getGenerateModelFormats()) gmf.add(v);
        n.setGenerate_model_formats(gmf);
        // predefine_area
        if (params.getPredefineArea() != null && params.getPredefineArea().getPolygonPoints() != null
                && !params.getPredefineArea().getPolygonPoints().isEmpty()) {
            com.dji.flighthubsdk.generated.ReconPredefineArea npa =
                    new com.dji.flighthubsdk.generated.ReconPredefineArea();
            com.dji.flighthubsdk.generated.ReconstructionPolygonPointVector pav =
                    new com.dji.flighthubsdk.generated.ReconstructionPolygonPointVector();
            for (double[] pt : params.getPredefineArea().getPolygonPoints()) {
                com.dji.flighthubsdk.generated.ReconstructionPolygonPoint p =
                        new com.dji.flighthubsdk.generated.ReconstructionPolygonPoint();
                if (pt.length >= 2) { p.setLat(pt[0]); p.setLng(pt[1]); }
                pav.add(p);
            }
            npa.setPolygon_points(pav);
            n.setPredefine_area(npa);
            n.setUse_predefine_area(true);
        }
        // parameter_2d
        if (params.getParameter2d() != null) {
            n.setParameter_2d(toNativeParameter2DParams(params.getParameter2d()));
            n.setUse_parameter_2d(true);
        }
        // parameter_3d
        if (params.getParameter3d() != null) {
            n.setParameter_3d(toNativeParameter3DParams(params.getParameter3d()));
            n.setUse_parameter_3d(true);
        }
        // parameter_3dgs
        if (params.getParameter3dgs() != null) {
            n.setParameter_3dgs(toNativeParameter3DGSParams(params.getParameter3dgs()));
            n.setUse_parameter_3dgs(true);
        }
        // parameter_lidar
        if (params.getParameterLidar() != null) {
            n.setParameter_lidar(toNativeParameterLidarParams(params.getParameterLidar()));
            n.setUse_parameter_lidar(true);
        }
        return n;
    }

    // ─── Terra Build Sub-Parameter Converters ────────────────

    public static com.dji.flighthubsdk.generated.GeoDescParams toNativeGeoDescParams(
            com.dji.flighthubsdk.bean.TerraBuildGeoDesc p) {
        com.dji.flighthubsdk.generated.GeoDescParams n = new com.dji.flighthubsdk.generated.GeoDescParams();
        if (p == null) return n;
        n.setCs_type(p.getCsType() != null ? p.getCsType() : "");
        n.setGeo_cs(p.getGeoCs() != null ? p.getGeoCs() : "");
        n.setGeo_cs_wkt(p.getGeoCsWkt() != null ? p.getGeoCsWkt() : "");
        n.setGeoid_file_name(p.getGeoidFileName() != null ? p.getGeoidFileName() : "");
        com.dji.flighthubsdk.generated.DoubleVector ov = new com.dji.flighthubsdk.generated.DoubleVector();
        if (p.getOffset() != null) for (double v : p.getOffset()) ov.add(v);
        n.setOffset(ov);
        n.setOverride_vertical_cs(p.getOverrideVerticalCs() != null ? p.getOverrideVerticalCs() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.Parameter2DParams toNativeParameter2DParams(
            com.dji.flighthubsdk.bean.TerraBuildParameter2D p) {
        com.dji.flighthubsdk.generated.Parameter2DParams n = new com.dji.flighthubsdk.generated.Parameter2DParams();
        if (p == null) return n;
        n.setSfM_method(p.getSfmMethod() != null ? p.getSfmMethod() : "");
        n.setSfm_feature_quantity_level(p.getSfmFeatureQuantityLevel());
        n.setSfm_feature_quantity_level_set(p.isSfmFeatureQuantityLevelSet());
        n.setUse_image_position(p.isUseImagePosition());
        n.setUse_image_position_set(p.isUseImagePositionSet());
        n.setGenerate_blocks_exchangge_xml(p.isGenerateBlocksExchangeXml());
        n.setGenerate_blocks_exchangge_xml_set(p.isGenerateBlocksExchangeXmlSet());
        if (p.getBlocksExchangeXmlGeoDesc() != null)
            n.setBlocks_exchange_xml_geo_desc(toNativeGeoDescParams(p.getBlocksExchangeXmlGeoDesc()));
        if (p.getOutputGeoDesc() != null)
            n.setOutput_geo_desc(toNativeGeoDescParams(p.getOutputGeoDesc()));
        n.setUsable_memory_GB(p.getUsableMemoryGb());
        n.setUsable_memory_GB_set(p.isUsableMemoryGbSet());
        n.setMap_mode(p.getMapMode());
        n.setMap_mode_set(p.isMapModeSet());
        n.setQuality_level(p.getQualityLevel());
        n.setQuality_level_set(p.isQualityLevelSet());
        n.setResolution_type_2d(p.getResolutionType2d());
        n.setResolution_type_2d_set(p.isResolutionType2dSet());
        n.setDom_tile_size(p.getDomTileSize());
        n.setDom_tile_size_set(p.isDomTileSizeSet());
        n.setResolution_gsd_2d(p.getResolutionGsd2d());
        n.setResolution_gsd_2d_set(p.isResolutionGsd2dSet());
        n.setUse_Wallis(p.isUseWallis());
        n.setUse_Wallis_set(p.isUseWallisSet());
        n.setUse_Dehaze(p.isUseDehaze());
        n.setUse_Dehaze_set(p.isUseDehazeSet());
        n.setUse_gcj02(p.isUseGcj02());
        n.setUse_gcj02_set(p.isUseGcj02Set());
        n.setGenerate_maps_2d_tiff(p.isGenerateMaps2dTiff());
        n.setGenerate_maps_2d_tiff_set(p.isGenerateMaps2dTiffSet());
        n.setGenerate_maps_2d_tiff_cog(p.isGenerateMaps2dTiffCog());
        n.setGenerate_maps_2d_tiff_cog_set(p.isGenerateMaps2dTiffCogSet());
        n.setUse_smart_fill_hole(p.isUseSmartFillHole());
        n.setUse_smart_fill_hole_set(p.isUseSmartFillHoleSet());
        return n;
    }

    public static com.dji.flighthubsdk.generated.Parameter3DParams toNativeParameter3DParams(
            com.dji.flighthubsdk.bean.TerraBuildParameter3D p) {
        com.dji.flighthubsdk.generated.Parameter3DParams n = new com.dji.flighthubsdk.generated.Parameter3DParams();
        if (p == null) return n;
        n.setSfM_method(p.getSfmMethod() != null ? p.getSfmMethod() : "");
        n.setSfm_feature_quantity_level(p.getSfmFeatureQuantityLevel());
        n.setSfm_feature_quantity_level_set(p.isSfmFeatureQuantityLevelSet());
        n.setUse_image_position(p.isUseImagePosition());
        n.setUse_image_position_set(p.isUseImagePositionSet());
        n.setGenerate_blocks_exchangge_xml(p.isGenerateBlocksExchangeXml());
        n.setGenerate_blocks_exchangge_xml_set(p.isGenerateBlocksExchangeXmlSet());
        if (p.getBlocksExchangeXmlGeoDesc() != null)
            n.setBlocks_exchange_xml_geo_desc(toNativeGeoDescParams(p.getBlocksExchangeXmlGeoDesc()));
        if (p.getOutputGeoDesc() != null)
            n.setOutput_geo_desc(toNativeGeoDescParams(p.getOutputGeoDesc()));
        n.setOutput_pointcloud(p.isOutputPointcloud());
        n.setOutput_pointcloud_set(p.isOutputPointcloudSet());
        n.setDensity_percent_level(p.getDensityPercentLevel());
        n.setDensity_percent_level_set(p.isDensityPercentLevelSet());
        n.setGenerate_pnts(p.isGeneratePnts());           n.setGenerate_pnts_set(p.isGeneratePntsSet());
        n.setGenerate_las(p.isGenerateLas());             n.setGenerate_las_set(p.isGenerateLasSet());
        n.setGenerate_laz(p.isGenerateLaz());             n.setGenerate_laz_set(p.isGenerateLazSet());
        n.setGenerate_pcd(p.isGeneratePcd());             n.setGenerate_pcd_set(p.isGeneratePcdSet());
        n.setGenerate_point_s3mb(p.isGeneratePointS3mb()); n.setGenerate_point_s3mb_set(p.isGeneratePointS3mbSet());
        n.setGenerate_point_ply(p.isGeneratePointPly());  n.setGenerate_point_ply_set(p.isGeneratePointPlySet());
        n.setMerge_point(p.isMergePoint());               n.setMerge_point_set(p.isMergePointSet());
        n.setOutput_ply_with_normals(p.isOutputPlyWithNormals());
        n.setOutput_ply_with_normals_set(p.isOutputPlyWithNormalsSet());
        n.setPly_with_normals_level(p.getPlyWithNormalsLevel());
        n.setPly_with_normals_level_set(p.isPlyWithNormalsLevelSet());
        n.setGroud_extract(p.isGroundExtract());          n.setGroud_extract_set(p.isGroundExtractSet());
        n.setScene_type(p.getSceneType());                n.setScene_type_set(p.isSceneTypeSet());
        n.setMax_size_building(p.getMaxSizeBuilding());   n.setMax_size_building_set(p.isMaxSizeBuildingSet());
        n.setIter_dist(p.getIterDist());                  n.setIter_dist_set(p.isIterDistSet());
        n.setIter_angle(p.getIterAngle());                n.setIter_angle_set(p.isIterAngleSet());
        n.setUsable_memory_GB(p.getUsableMemoryGb());     n.setUsable_memory_GB_set(p.isUsableMemoryGbSet());
        n.setStereo_mode(p.isStereoMode());               n.setStereo_mode_set(p.isStereoModeSet());
        n.setGlobal_min_depth(p.getGlobalMinDepth());     n.setGlobal_min_depth_set(p.isGlobalMinDepthSet());
        n.setOutput_mesh(p.isOutputMesh());               n.setOutput_mesh_set(p.isOutputMeshSet());
        n.setGenerate_ply(p.isGeneratePly());             n.setGenerate_ply_set(p.isGeneratePlySet());
        n.setGenerate_obj(p.isGenerateObj());             n.setGenerate_obj_set(p.isGenerateObjSet());
        n.setGenerate_fbx(p.isGenerateFbx());             n.setGenerate_fbx_set(p.isGenerateFbxSet());
        n.setGenerate_b3dm(p.isGenerateB3dm());           n.setGenerate_b3dm_set(p.isGenerateB3dmSet());
        n.setGenerate_osgb(p.isGenerateOsgb());           n.setGenerate_osgb_set(p.isGenerateOsgbSet());
        n.setGenerate_s3mb(p.isGenerateS3mb());           n.setGenerate_s3mb_set(p.isGenerateS3mbSet());
        n.setGenerate_i3s(p.isGenerateI3s());             n.setGenerate_i3s_set(p.isGenerateI3sSet());
        n.setWater_refine(p.isWaterRefine());             n.setWater_refine_set(p.isWaterRefineSet());
        n.setWater_remove(p.isWaterRemove());             n.setWater_remove_set(p.isWaterRemoveSet());
        n.setModel_simplify(p.getModelSimplify());        n.setModel_simplify_set(p.isModelSimplifySet());
        n.setGenerate_dem(p.isGenerateDem());             n.setGenerate_dem_set(p.isGenerateDemSet());
        n.setDem_resolution(p.getDemResolution());        n.setDem_resolution_set(p.isDemResolutionSet());
        n.setGenerate_contour(p.isGenerateContour());     n.setGenerate_contour_set(p.isGenerateContourSet());
        n.setContour_interval(p.getContourInterval());    n.setContour_interval_set(p.isContourIntervalSet());
        n.setContour_datum(p.getContourDatum());          n.setContour_datum_set(p.isContourDatumSet());
        n.setContour_elevation_annotation_radius(p.getContourElevationAnnotationRadius());
        n.setContour_elevation_annotation_radius_set(p.isContourElevationAnnotationRadiusSet());
        n.setUse_contour_filter(p.isUseContourFilter());  n.setUse_contour_filter_set(p.isUseContourFilterSet());
        n.setContour_filter_length(p.getContourFilterLength());
        n.setContour_filter_length_set(p.isContourFilterLengthSet());
        n.setGenerate_grid_and_tin(p.isGenerateGridAndTin());
        n.setGenerate_grid_and_tin_set(p.isGenerateGridAndTinSet());
        n.setGrid_interval(p.getGridInterval());          n.setGrid_interval_set(p.isGridIntervalSet());
        n.setCut_AOI(p.isCutAoi());                       n.setCut_AOI_set(p.isCutAoiSet());
        n.setQuality_level(p.getQualityLevel());          n.setQuality_level_set(p.isQualityLevelSet());
        return n;
    }

    public static com.dji.flighthubsdk.generated.Parameter3DGSParams toNativeParameter3DGSParams(
            com.dji.flighthubsdk.bean.TerraBuildParameter3DGS p) {
        com.dji.flighthubsdk.generated.Parameter3DGSParams n = new com.dji.flighthubsdk.generated.Parameter3DGSParams();
        if (p == null) return n;
        n.setSfM_method(p.getSfmMethod() != null ? p.getSfmMethod() : "");
        n.setSfm_feature_quantity_level(p.getSfmFeatureQuantityLevel());
        n.setSfm_feature_quantity_level_set(p.isSfmFeatureQuantityLevelSet());
        n.setUse_image_position(p.isUseImagePosition());
        n.setUse_image_position_set(p.isUseImagePositionSet());
        n.setGenerate_blocks_exchangge_xml(p.isGenerateBlocksExchangeXml());
        n.setGenerate_blocks_exchangge_xml_set(p.isGenerateBlocksExchangeXmlSet());
        if (p.getBlocksExchangeXmlGeoDesc() != null)
            n.setBlocks_exchange_xml_geo_desc(toNativeGeoDescParams(p.getBlocksExchangeXmlGeoDesc()));
        if (p.getOutputGeoDesc() != null)
            n.setOutput_geo_desc(toNativeGeoDescParams(p.getOutputGeoDesc()));
        n.setGlobal_data_type(p.getGlobalDataType() != null ? p.getGlobalDataType() : "");
        n.setQuality_level_3dgs(p.getQualityLevel3dgs());
        n.setQuality_level_3dgs_set(p.isQualityLevel3dgsSet());
        n.setGenerate_terrags(p.isGenerateTerrags());     n.setGenerate_terrags_set(p.isGenerateTerragsSet());
        n.setGenerate_3dtiles(p.isGenerate3dtiles());     n.setGenerate_3dtiles_set(p.isGenerate3dtilesSet());
        n.setGenerate_3dgs_dom(p.isGenerate3dgsDom());    n.setGenerate_3dgs_dom_set(p.isGenerate3dgsDomSet());
        return n;
    }

    public static com.dji.flighthubsdk.generated.ParameterLidarParams toNativeParameterLidarParams(
            com.dji.flighthubsdk.bean.TerraBuildParameterLidar p) {
        com.dji.flighthubsdk.generated.ParameterLidarParams n = new com.dji.flighthubsdk.generated.ParameterLidarParams();
        if (p == null) return n;
        n.setOutput_pointcloud(p.isOutputPointcloud());   n.setOutput_pointcloud_set(p.isOutputPointcloudSet());
        n.setDensity_percent_level(p.getDensityPercentLevel());
        n.setDensity_percent_level_set(p.isDensityPercentLevelSet());
        n.setGenerate_pnts(p.isGeneratePnts());           n.setGenerate_pnts_set(p.isGeneratePntsSet());
        n.setGenerate_las(p.isGenerateLas());             n.setGenerate_las_set(p.isGenerateLasSet());
        n.setGenerate_laz(p.isGenerateLaz());             n.setGenerate_laz_set(p.isGenerateLazSet());
        n.setGenerate_pcd(p.isGeneratePcd());             n.setGenerate_pcd_set(p.isGeneratePcdSet());
        n.setGenerate_point_s3mb(p.isGeneratePointS3mb()); n.setGenerate_point_s3mb_set(p.isGeneratePointS3mbSet());
        n.setGenerate_point_ply(p.isGeneratePointPly());  n.setGenerate_point_ply_set(p.isGeneratePointPlySet());
        n.setMerge_point(p.isMergePoint());               n.setMerge_point_set(p.isMergePointSet());
        n.setOutput_ply_with_normals(p.isOutputPlyWithNormals()); n.setOutput_ply_with_normals_set(p.isOutputPlyWithNormalsSet());
        n.setPly_with_normals_level(p.getPlyWithNormalsLevel()); n.setPly_with_normals_level_set(p.isPlyWithNormalsLevelSet());
        n.setGroud_extract(p.isGroudExtract());           n.setGroud_extract_set(p.isGroudExtractSet());
        n.setScene_type(p.getSceneType());                n.setScene_type_set(p.isSceneTypeSet());
        n.setMax_size_building(p.getMaxSizeBuilding());   n.setMax_size_building_set(p.isMaxSizeBuildingSet());
        n.setIter_dist(p.getIterDist());                  n.setIter_dist_set(p.isIterDistSet());
        n.setIter_angle(p.getIterAngle());                n.setIter_angle_set(p.isIterAngleSet());
        n.setUsable_memory_GB(p.getUsableMemoryGb());     n.setUsable_memory_GB_set(p.isUsableMemoryGbSet());
        n.setQuality_level(p.getQualityLevel());          n.setQuality_level_set(p.isQualityLevelSet());
        n.setSample_distance(p.getSampleDistance());      n.setSample_distance_set(p.isSampleDistanceSet());
        n.setOutput_height_offset(p.getOutputHeightOffset());
        n.setOutput_height_offset_set(p.isOutputHeightOffsetSet());
        n.setRefine_calibrate(p.getRefineCalibrate());    n.setRefine_calibrate_set(p.isRefineCalibrateSet());
        n.setLpp_optimise(p.isLppOptimise());             n.setLpp_optimise_set(p.isLppOptimiseSet());
        n.setSmooth_point(p.isSmoothPoint());             n.setSmooth_point_set(p.isSmoothPointSet());
        n.setColorize_point(p.isColorizePoint());         n.setColorize_point_set(p.isColorizePointSet());
        n.setCan_process_L1(p.isCanProcessL1());          n.setCan_process_L1_set(p.isCanProcessL1Set());
        if (p.getOutputGeoDesc() != null)
            n.setOutput_geo_desc(toNativeGeoDescParams(p.getOutputGeoDesc()));
        n.setOutput_mesh(p.isOutputMesh());               n.setOutput_mesh_set(p.isOutputMeshSet());
        n.setQuality_level_mesh(p.getQualityLevelMesh()); n.setQuality_level_mesh_set(p.isQualityLevelMeshSet());
        n.setModel_simplify(p.getModelSimplify());        n.setModel_simplify_set(p.isModelSimplifySet());
        n.setGenerate_obj(p.isGenerateObj());             n.setGenerate_obj_set(p.isGenerateObjSet());
        n.setGenerate_fbx(p.isGenerateFbx());             n.setGenerate_fbx_set(p.isGenerateFbxSet());
        n.setGenerate_ply(p.isGeneratePly());             n.setGenerate_ply_set(p.isGeneratePlySet());
        n.setGenerate_osgb(p.isGenerateOsgb());           n.setGenerate_osgb_set(p.isGenerateOsgbSet());
        n.setGenerate_b3dm(p.isGenerateB3dm());           n.setGenerate_b3dm_set(p.isGenerateB3dmSet());
        n.setGenerate_i3s(p.isGenerateI3s());             n.setGenerate_i3s_set(p.isGenerateI3sSet());
        n.setGenerate_maps_2d(p.isGenerateMaps2d());     n.setGenerate_maps_2d_set(p.isGenerateMaps2dSet());
        n.setGenerate_maps_2d_tiff(p.isGenerateMaps2dTiff());
        n.setGenerate_maps_2d_tiff_set(p.isGenerateMaps2dTiffSet());
        n.setGenerate_maps_2d_tiff_cog(p.isGenerateMaps2dTiffCog());
        n.setGenerate_maps_2d_tiff_cog_set(p.isGenerateMaps2dTiffCogSet());
        n.setResolution_type_2d(p.getResolutionType2d()); n.setResolution_type_2d_set(p.isResolutionType2dSet());
        n.setResolution_level_2d(p.getResolutionLevel2d()); n.setResolution_level_2d_set(p.isResolutionLevel2dSet());
        n.setResolution_gsd_2d(p.getResolutionGsd2d());  n.setResolution_gsd_2d_set(p.isResolutionGsd2dSet());
        n.setDom_tile_size(p.getDomTileSize());           n.setDom_tile_size_set(p.isDomTileSizeSet());
        n.setUse_gcj02(p.isUseGcj02());                  n.setUse_gcj02_set(p.isUseGcj02Set());
        n.setGenerate_dem(p.isGenerateDem());             n.setGenerate_dem_set(p.isGenerateDemSet());
        n.setDem_resolution(p.getDemResolution());        n.setDem_resolution_set(p.isDemResolutionSet());
        n.setGenerate_contour(p.isGenerateContour());     n.setGenerate_contour_set(p.isGenerateContourSet());
        n.setContour_interval(p.getContourInterval());    n.setContour_interval_set(p.isContourIntervalSet());
        n.setContour_datum(p.getContourDatum());          n.setContour_datum_set(p.isContourDatumSet());
        n.setContour_elevation_annotation_radius(p.getContourElevationAnnotationRadius());
        n.setContour_elevation_annotation_radius_set(p.isContourElevationAnnotationRadiusSet());
        n.setUse_contour_filter(p.isUseContourFilter());  n.setUse_contour_filter_set(p.isUseContourFilterSet());
        n.setContour_filter_length(p.getContourFilterLength());
        n.setContour_filter_length_set(p.isContourFilterLengthSet());
        n.setGenerate_3dgs(p.isGenerate3dgs());           n.setGenerate_3dgs_set(p.isGenerate3dgsSet());
        n.setQuality_level_3dgs(p.getQualityLevel3dgs()); n.setQuality_level_3dgs_set(p.isQualityLevel3dgsSet());
        n.setGenerate_3dtiles(p.isGenerate3dtiles());     n.setGenerate_3dtiles_set(p.isGenerate3dtilesSet());
        return n;
    }

    // ─── ATC Task Events ────────────────────────────────────

    public static com.dji.flighthubsdk.bean.CommandTaskData toCommandTaskData(
            com.dji.flighthubsdk.generated.CommandTaskData n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.CommandTaskData(
                    0, "", "", "", "", "", "", "", 0, 0, "", 0.0, 0,
                    com.dji.flighthubsdk.value.OutOfControlAction.UNKNOWN, 0, 0, 0.0, 0, "");
        }
        return new com.dji.flighthubsdk.bean.CommandTaskData(
                n.getId(), safeString(n.getCreated_at()), safeString(n.getUuid()),
                safeString(n.getPrj_uuid()), safeString(n.getSn()), safeString(n.getDrone_sn()),
                safeString(n.getTrack_id()), safeString(n.getUser_id()),
                n.getStatus(), n.getErr_code(), safeString(n.getTakeoff_mode()),
                n.getSecurity_takeoff_height(), n.getMax_speed(),
                com.dji.flighthubsdk.value.OutOfControlAction.fromValue(n.getOut_of_control_action()),
                n.getRth_altitude(), n.getCommander_flight_mode(),
                n.getCommander_flight_height(), n.getRth_mode(),
                safeString(n.getCloud_to_cloud_id()));
    }

    public static com.dji.flighthubsdk.bean.CommandTaskEvent toCommandTaskEvent(
            com.dji.flighthubsdk.generated.CommandTaskEvent n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.CommandTaskEvent("", "", "", 0,
                    toCommandTaskData(null));
        }
        return new com.dji.flighthubsdk.bean.CommandTaskEvent(
                safeString(n.getEvent_id()), safeString(n.getAction()),
                safeString(n.getUuid()), n.getTimestamp(),
                toCommandTaskData(n.getTask_data()));
    }

    public static com.dji.flighthubsdk.bean.FlyToWayPoint toFlyToWayPoint(
            com.dji.flighthubsdk.generated.FlyToWayPoint n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlyToWayPoint(0.0, 0.0, 0.0);
        }
        return new com.dji.flighthubsdk.bean.FlyToWayPoint(
                n.getLatitude(), n.getLongitude(), n.getHeight());
    }

    public static com.dji.flighthubsdk.bean.FlyToTaskData toFlyToTaskData(
            com.dji.flighthubsdk.generated.FlyToTaskData n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlyToTaskData(
                    0, "", "", "", "", "", 0, "", 0, 0, 0, 0,
                    java.util.Collections.emptyList(), java.util.Collections.emptyList(), 0.0, 0.0);
        }
        java.util.List<com.dji.flighthubsdk.bean.FlyToWayPoint> wayPoints = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.FlyToWayPointVector wpVec = n.getWay_points();
        if (wpVec != null && !wpVec.isEmpty()) {
            for (int i = 0; i < wpVec.size(); i++) {
                wayPoints.add(toFlyToWayPoint(wpVec.get(i)));
            }
        }
        java.util.List<com.dji.flighthubsdk.bean.FlyToWayPoint> plannedPaths = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.FlyToWayPointVector ppVec = n.getPlanned_path_points();
        if (ppVec != null && !ppVec.isEmpty()) {
            for (int i = 0; i < ppVec.size(); i++) {
                plannedPaths.add(toFlyToWayPoint(ppVec.get(i)));
            }
        }
        return new com.dji.flighthubsdk.bean.FlyToTaskData(
                n.getId(), safeString(n.getUuid()), safeString(n.getPrj_uuid()),
                safeString(n.getSn()), safeString(n.getDrone_sn()), safeString(n.getUser_id()),
                n.getSeq_no(), safeString(n.getFlight_task_uuid()), n.getFlight_task_type(),
                n.getStatus(), n.getErr_code(), n.getWay_point_index(),
                wayPoints, plannedPaths, n.getRemaining_distance(), n.getRemaining_time());
    }

    public static com.dji.flighthubsdk.bean.FlyToTaskEvent toFlyToTaskEvent(
            com.dji.flighthubsdk.generated.FlyToTaskEvent n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlyToTaskEvent("", "", "", 0,
                    toFlyToTaskData(null));
        }
        return new com.dji.flighthubsdk.bean.FlyToTaskEvent(
                safeString(n.getEvent_id()), safeString(n.getAction()),
                safeString(n.getUuid()), n.getTimestamp(),
                toFlyToTaskData(n.getTask_data()));
    }

    public static com.dji.flighthubsdk.bean.PoiCenterPoint toPoiCenterPoint(
            com.dji.flighthubsdk.generated.PoiCenterPoint n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.PoiCenterPoint(0.0, 0.0, 0.0);
        }
        return new com.dji.flighthubsdk.bean.PoiCenterPoint(
                n.getLatitude(), n.getLongitude(), n.getHeight());
    }

    public static com.dji.flighthubsdk.bean.PoiTaskData toPoiTaskData(
            com.dji.flighthubsdk.generated.PoiTaskData n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.PoiTaskData(
                    "", "", "", toPoiCenterPoint(null), 0.0, 0.0, "", 0, 0);
        }
        return new com.dji.flighthubsdk.bean.PoiTaskData(
                safeString(n.getUuid()), safeString(n.getSn()), safeString(n.getDevice_sn()),
                toPoiCenterPoint(n.getPoi_center_point()),
                n.getSpeed(), n.getCircle_radius(), safeString(n.getPayload_index()),
                n.getStatus(), n.getErr_code());
    }

    public static com.dji.flighthubsdk.bean.PoiTaskEvent toPoiTaskEvent(
            com.dji.flighthubsdk.generated.PoiTaskEvent n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.PoiTaskEvent("", "", "", 0,
                    toPoiTaskData(null));
        }
        return new com.dji.flighthubsdk.bean.PoiTaskEvent(
                safeString(n.getEvent_id()), safeString(n.getAction()),
                safeString(n.getUuid()), n.getTimestamp(),
                toPoiTaskData(n.getTask_data()));
    }

    public static com.dji.flighthubsdk.bean.WaylineBreakPoint toWaylineBreakPoint(
            com.dji.flighthubsdk.generated.WaylineBreakPoint n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.WaylineBreakPoint(0, 0, 0.0, 0, 0, 0.0, 0.0, 0.0, 0.0);
        }
        return new com.dji.flighthubsdk.bean.WaylineBreakPoint(
                n.getIndex(), n.getState(), n.getProgress(), n.getWayline_id(),
                n.getBreak_reason(), n.getLatitude(), n.getLongitude(),
                n.getHeight(), n.getAttitude_head());
    }

    public static com.dji.flighthubsdk.bean.WaylineObstacle toWaylineObstacle(
            com.dji.flighthubsdk.generated.WaylineObstacle n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.WaylineObstacle(0, 0, 0, 0.0, 0.0, 0.0, 0, 0);
        }
        return new com.dji.flighthubsdk.bean.WaylineObstacle(
                n.getId(), n.getType(), n.getTimestamp(), n.getLatitude(),
                n.getLongitude(), n.getHeight(), n.getWayline_id(), n.getWaypoint_index());
    }

    public static com.dji.flighthubsdk.bean.WaylineTaskData toWaylineTaskData(
            com.dji.flighthubsdk.generated.WaylineTaskData n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.WaylineTaskData(
                    "", "", 0, 0, "", "", "", 0, 0.0,
                    java.util.Collections.emptyList(), 0, 0, "", "", "", "", "",
                    0, 0, 0, 0, java.util.Collections.emptyList(), "",
                    toWaylineBreakPoint(null), false, 0, false, 0,
                    java.util.Collections.emptyList(), "", "", "");
        }
        java.util.List<Integer> segIndexes = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.IntVector segVec = n.getWayline_segment_indexes();
        if (segVec != null && !segVec.isEmpty()) {
            for (int i = 0; i < segVec.size(); i++) {
                segIndexes.add(segVec.get(i));
            }
        }
        java.util.List<String> tags = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.StringVector tagVec = n.getTags();
        if (tagVec != null && !tagVec.isEmpty()) {
            for (int i = 0; i < tagVec.size(); i++) {
                tags.add(safeString(tagVec.get(i)));
            }
        }
        java.util.List<com.dji.flighthubsdk.bean.WaylineObstacle> obstacles = new java.util.ArrayList<>();
        com.dji.flighthubsdk.generated.WaylineObstacleVector obsVec = n.getObstacles();
        if (obsVec != null && !obsVec.isEmpty()) {
            for (int i = 0; i < obsVec.size(); i++) {
                obstacles.add(toWaylineObstacle(obsVec.get(i)));
            }
        }
        return new com.dji.flighthubsdk.bean.WaylineTaskData(
                safeString(n.getName()), safeString(n.getUuid()), n.getTask_type(),
                n.getStatus(), safeString(n.getSn()), safeString(n.getLanding_dock_sn()),
                safeString(n.getWayline_uuid()), n.getWayline_precision_type(),
                n.getWayline_duration(), segIndexes,
                n.getTotal_waypoints(), n.getCurrent_waypoint_index(),
                safeString(n.getBid()), safeString(n.getBegin_at()), safeString(n.getEnd_at()),
                safeString(n.getRun_at()), safeString(n.getCompleted_at()),
                n.getOut_of_control_action_in_flight(), n.getRth_altitude(), n.getRth_mode(),
                n.getMin_battery_capacity(), tags, safeString(n.getCloud_to_cloud_id()),
                toWaylineBreakPoint(n.getBreak_point()), n.getIs_break_point_resume(),
                n.getMedia_upload_status(), n.getFast_reconstruction(), n.getDevice_type(),
                obstacles, safeString(n.getCreated_at()), safeString(n.getUpdated_at()),
                safeString(n.getDeleted_at()));
    }

    public static com.dji.flighthubsdk.bean.WaylineTaskEvent toWaylineTaskEvent(
            com.dji.flighthubsdk.generated.WaylineTaskEvent n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.WaylineTaskEvent("", "", "", 0,
                    toWaylineTaskData(null));
        }
        return new com.dji.flighthubsdk.bean.WaylineTaskEvent(
                safeString(n.getEvent_id()), safeString(n.getAction()),
                safeString(n.getUuid()), n.getTimestamp(),
                toWaylineTaskData(n.getTask_data()));
    }

    public static com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskParams toNativeCreateInFlightWaylineTaskParams(
            com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskParams param) {
        com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskParams nativeParam =
                new com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskParams();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setWayline_uuid(param.getWaylineUuid());
        nativeParam.setRth_altitude(param.getRthAltitude());
        nativeParam.setWayline_precision_type(param.getWaylinePrecisionType());
        nativeParam.setRth_mode(param.getRthMode());
        nativeParam.setOut_of_control_action(param.getOutOfControlAction());
        nativeParam.setExit_wayline_when_rc_lost(param.getExitWaylineWhenRcLost());
        nativeParam.setWayline_duration(param.getWaylineDuration());
        nativeParam.setWayline_length(param.getWaylineLength());
        nativeParam.setTask_name(param.getTaskName());
        if (param.getFastReconstruction() != null) {
            nativeParam.setFast_reconstruction(param.getFastReconstruction() ? 1 : 0);
        }
        return nativeParam;
    }

    public static com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult toCreateInFlightWaylineTaskResult(
            com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult();
        }
        return new com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult(
                safeString(n.getFlight_task_id()), safeString(n.getIn_flight_wayline_task_id()));
    }

    public static com.dji.flighthubsdk.bean.InFlightWaylineTaskDetail toInFlightWaylineTaskDetail(
            com.dji.flighthubsdk.generated.InFlightWaylineTaskDetail n) {
        com.dji.flighthubsdk.bean.InFlightWaylineTaskDetail detail = new com.dji.flighthubsdk.bean.InFlightWaylineTaskDetail();
        if (n == null) {
            return detail;
        }
        detail.setUuid(safeString(n.getUuid()));
        detail.setUserId(safeString(n.getUser_id()));
        detail.setStatus(n.getStatus());
        detail.setWaylineId(safeString(n.getWayline_id()));
        detail.setErrCode(n.getErr_code());
        detail.setWayPointIndex(n.getWay_point_index());
        detail.setProgressPercent(n.getProgress_percent());
        detail.setWaylineDuration(n.getWayline_duration());
        detail.setWaylineLength(n.getWayline_length());
        detail.setTaskName(safeString(n.getTask_name()));
        return detail;
    }

    public static com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo toActiveInFlightWaylineTaskInfo(
            com.dji.flighthubsdk.generated.ActiveInFlightWaylineTaskInfo n) {
        com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo info = new com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo();
        if (n == null) {
            return info;
        }
        info.setFlightId(safeString(n.getFlight_id()));
        info.setFlightType(n.getFlight_type());
        info.setSn(safeString(n.getSn()));
        info.setInFlightTask(toInFlightWaylineTaskDetail(n.getIn_flight_task()));
        info.setFlightTaskData(safeString(n.getFlight_task_data()));
        return info;
    }

    public static com.dji.flighthubsdk.bean.InFlightWaylineProgress toInFlightWaylineProgress(
            com.dji.flighthubsdk.generated.InFlightWaylineProgress n) {
        com.dji.flighthubsdk.bean.InFlightWaylineProgress progress = new com.dji.flighthubsdk.bean.InFlightWaylineProgress();
        if (n == null) {
            return progress;
        }
        progress.setInFlightWaylineId(safeString(n.getIn_flight_wayline_id()));
        progress.setPercent(n.getPercent());
        progress.setStatus(n.getStatus());
        progress.setResult(n.getResult());
        progress.setGatewaySn(safeString(n.getGateway_sn()));
        progress.setWayPointIndex(n.getWay_point_index());
        progress.setWaylineDuration(n.getWayline_duration());
        progress.setWaylineLength(n.getWayline_length());
        progress.setTaskName(safeString(n.getTask_name()));
        progress.setTimestamp(n.getTimestamp());
        return progress;
    }

    public static com.dji.flighthubsdk.generated.InFlightWaylineControlAction toNativeInFlightWaylineControlAction(
            com.dji.flighthubsdk.value.InFlightWaylineControlAction action) {
        if (action == null) {
            return com.dji.flighthubsdk.generated.InFlightWaylineControlAction.Pause;
        }
        return com.dji.flighthubsdk.generated.InFlightWaylineControlAction.swigToEnum(action.getValue());
    }

    // =====================================================================
    // AI Inside 机载算法管理转换方法
    // =====================================================================

    public static com.dji.flighthubsdk.generated.GetAIInsideModelDevicesParam toNativeGetAIInsideModelDevicesParam(
            com.dji.flighthubsdk.bean.GetAIInsideModelDevicesParam param) {
        com.dji.flighthubsdk.generated.GetAIInsideModelDevicesParam n = new com.dji.flighthubsdk.generated.GetAIInsideModelDevicesParam();
        if (param == null) { return n; }
        n.setSigned_id(safeString(param.getSignedId()));
        n.setPage(param.getPage());
        n.setPage_size(param.getPageSize());
        if (param.getDroneSn() != null && !param.getDroneSn().isEmpty()) {
            com.dji.flighthubsdk.generated.StringVector v = new com.dji.flighthubsdk.generated.StringVector();
            for (String sn : param.getDroneSn()) { v.add(sn); }
            n.setDrone_sn(v);
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryParam toNativeGetAIInsideModelDevicesSummaryParam(
            com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryParam param) {
        com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryParam n = new com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryParam();
        if (param == null) { return n; }
        n.setSigned_id(safeString(param.getSignedId()));
        n.setOperation_type(safeString(param.getOperationType()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.OperateAIInsideModelParam toNativeOperateAIInsideModelParam(
            com.dji.flighthubsdk.bean.OperateAIInsideModelParam param) {
        com.dji.flighthubsdk.generated.OperateAIInsideModelParam n = new com.dji.flighthubsdk.generated.OperateAIInsideModelParam();
        if (param == null) { return n; }
        n.setSigned_id(safeString(param.getSignedId()));
        n.setOperation_type(safeString(param.getOperationType()));
        if (param.getDroneSNList() != null && !param.getDroneSNList().isEmpty()) {
            com.dji.flighthubsdk.generated.StringVector v = new com.dji.flighthubsdk.generated.StringVector();
            for (String sn : param.getDroneSNList()) { v.add(sn); }
            n.setDrone_sn_list(v);
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmParam toNativeCreateAIInsideAlgorithmParam(
            com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmParam param) {
        com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmParam n = new com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmParam();
        if (param == null) { return n; }
        n.setName(safeString(param.getName()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmParam toNativeFileUploadAIInsideAlgorithmParam(
            com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmParam param) {
        com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmParam n = new com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmParam();
        if (param == null) { return n; }
        n.setFile_key(safeString(param.getFileKey()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.DeleteAIInsideAlgorithmParam toNativeDeleteAIInsideAlgorithmParam(
            com.dji.flighthubsdk.bean.DeleteAIInsideAlgorithmParam param) {
        com.dji.flighthubsdk.generated.DeleteAIInsideAlgorithmParam n = new com.dji.flighthubsdk.generated.DeleteAIInsideAlgorithmParam();
        if (param == null) { return n; }
        n.setSigned_id(safeString(param.getSignedId()));
        return n;
    }

    private static com.dji.flighthubsdk.bean.AIInsideLabel toAIInsideLabel(com.dji.flighthubsdk.generated.AIInsideLabel n) {
        com.dji.flighthubsdk.bean.AIInsideLabel b = new com.dji.flighthubsdk.bean.AIInsideLabel();
        if (n == null) { return b; }
        b.setType(n.getType());
        b.setName(safeString(n.getName()));
        return b;
    }

    private static com.dji.flighthubsdk.bean.AIInsideModelParam toAIInsideModelParam(com.dji.flighthubsdk.generated.AIInsideModelParam n) {
        com.dji.flighthubsdk.bean.AIInsideModelParam b = new com.dji.flighthubsdk.bean.AIInsideModelParam();
        if (n == null) { return b; }
        b.setConfThreshold(n.getConf_threshold());
        b.setNmsPre(n.getNms_pre());
        b.setIouThreshold(n.getIou_threshold());
        return b;
    }

    private static com.dji.flighthubsdk.bean.AIInsideLicense toAIInsideLicense(com.dji.flighthubsdk.generated.AIInsideLicense n) {
        com.dji.flighthubsdk.bean.AIInsideLicense b = new com.dji.flighthubsdk.bean.AIInsideLicense();
        if (n == null) { return b; }
        b.setSnList(new java.util.ArrayList<>(copyStringVector(n.getSn_list())));
        b.setStartedUtc(n.getStarted_utc());
        b.setExpiredUtc(n.getExpired_utc());
        return b;
    }

    private static java.util.List<com.dji.flighthubsdk.bean.AIInsideLabel> toAIInsideLabelList(com.dji.flighthubsdk.generated.AIInsideLabelVector vec) {
        java.util.List<com.dji.flighthubsdk.bean.AIInsideLabel> list = new java.util.ArrayList<>();
        if (vec == null) { return list; }
        for (int i = 0; i < vec.size(); i++) { list.add(toAIInsideLabel(vec.get(i))); }
        return list;
    }

    private static com.dji.flighthubsdk.bean.AIInsideModelItem toAIInsideModelItem(com.dji.flighthubsdk.generated.AIInsideModelItem n) {
        com.dji.flighthubsdk.bean.AIInsideModelItem b = new com.dji.flighthubsdk.bean.AIInsideModelItem();
        if (n == null) { return b; }
        b.setSignedId(safeString(n.getSigned_id()));
        b.setSignedName(safeString(n.getSigned_name()));
        b.setModelName(safeString(n.getModel_name()));
        b.setSignedVersion(n.getSigned_version());
        b.setSignedUtc(n.getSigned_utc());
        b.setExpiredUtc(n.getExpired_utc());
        b.setModelParam(toAIInsideModelParam(n.getModel_param()));
        b.setSnList(new java.util.ArrayList<>(copyStringVector(n.getSn_list())));
        b.setImageSource(new java.util.ArrayList<>(copyIntVector(n.getImage_source())));
        b.setDigitalEffect(new java.util.ArrayList<>(copyIntVector(n.getDigital_effect())));
        b.setVlResolution(new java.util.ArrayList<>(copyIntVector(n.getVl_resolution())));
        b.setLabels(toAIInsideLabelList(n.getLabels()));
        return b;
    }

    public static com.dji.flighthubsdk.bean.GetAIInsideModelsResult toGetAIInsideModelsResult(
            com.dji.flighthubsdk.generated.GetAIInsideModelsResult n) {
        com.dji.flighthubsdk.bean.GetAIInsideModelsResult b = new com.dji.flighthubsdk.bean.GetAIInsideModelsResult();
        if (n == null) { return b; }
        com.dji.flighthubsdk.generated.AIInsideModelItemVector vec = n.getModels();
        if (vec != null) {
            java.util.List<com.dji.flighthubsdk.bean.AIInsideModelItem> list = new java.util.ArrayList<>();
            for (int i = 0; i < vec.size(); i++) { list.add(toAIInsideModelItem(vec.get(i))); }
            b.setModels(list);
        }
        return b;
    }

    private static com.dji.flighthubsdk.bean.AIInsideDeviceModel toAIInsideDeviceModel(com.dji.flighthubsdk.generated.AIInsideDeviceModel n) {
        com.dji.flighthubsdk.bean.AIInsideDeviceModel b = new com.dji.flighthubsdk.bean.AIInsideDeviceModel();
        if (n == null) { return b; }
        b.setDeviceModelKey(safeString(n.getDevice_model_key()));
        b.setDomain(safeString(n.getDomain()));
        b.setName(safeString(n.getName()));
        return b;
    }

    private static com.dji.flighthubsdk.bean.AIInsideDeviceItem toAIInsideDeviceItem(com.dji.flighthubsdk.generated.AIInsideDeviceItem n) {
        com.dji.flighthubsdk.bean.AIInsideDeviceItem b = new com.dji.flighthubsdk.bean.AIInsideDeviceItem();
        if (n == null) { return b; }
        b.setDeviceSn(safeString(n.getDevice_sn()));
        b.setCompleteMachineSn(safeString(n.getComplete_machine_sn()));
        b.setDeviceOnlineStatus(n.getDevice_online_status());
        b.setDeviceWorkingStatus(n.getDevice_working_status());
        b.setDeviceModel(toAIInsideDeviceModel(n.getDevice_model()));
        com.dji.flighthubsdk.bean.AIInsideOperateProgress op = new com.dji.flighthubsdk.bean.AIInsideOperateProgress();
        com.dji.flighthubsdk.generated.AIInsideOperateProgress nop = n.getOperate_ai_model();
        if (nop != null) {
            op.setDeviceCmdMethod(safeString(nop.getDevice_cmd_method()));
            op.setStatus(safeString(nop.getStatus()));
            com.dji.flighthubsdk.bean.AIInsideOperateProgressDetail detail =
                    new com.dji.flighthubsdk.bean.AIInsideOperateProgressDetail();
            com.dji.flighthubsdk.generated.AIInsideOperateProgressDetail ndetail = nop.getProgress();
            if (ndetail != null) {
                detail.setPercent(ndetail.getPercent());
                detail.setCurrentStep(ndetail.getCurrent_step());
            }
            op.setProgress(detail);
            op.setDeviceCode(nop.getDevice_code());
            op.setCreateTime(nop.getCreate_time());
        }
        b.setOperateAiModel(op);
        com.dji.flighthubsdk.generated.AIInsideAssociateDrone nd = n.getAssociate_drone_device_info();
        com.dji.flighthubsdk.bean.AIInsideAssociateDrone drone = new com.dji.flighthubsdk.bean.AIInsideAssociateDrone();
        if (nd != null) {
            drone.setDeviceSn(safeString(nd.getDevice_sn()));
            drone.setDeviceOnlineStatus(nd.getDevice_online_status());
            drone.setDeviceWorkingStatus(nd.getDevice_working_status());
            drone.setDeviceModel(toAIInsideDeviceModel(nd.getDevice_model()));
            com.dji.flighthubsdk.bean.AIInsideModelInfo info = new com.dji.flighthubsdk.bean.AIInsideModelInfo();
            com.dji.flighthubsdk.generated.AIInsideModelInfo ninfo = nd.getAi_model_info();
            if (ninfo != null) { info.setSignedId(safeString(ninfo.getSigned_id())); info.setModelName(safeString(ninfo.getModel_name())); info.setSignedUtc(ninfo.getSigned_utc()); info.setExpiredUtc(ninfo.getExpired_utc()); info.setStatus(safeString(ninfo.getStatus())); }
            drone.setAiModelInfo(info);
        }
        b.setAssociateDroneDeviceInfo(drone);
        return b;
    }

    public static com.dji.flighthubsdk.bean.GetAIInsideModelDevicesResult toGetAIInsideModelDevicesResult(
            com.dji.flighthubsdk.generated.GetAIInsideModelDevicesResult n) {
        com.dji.flighthubsdk.bean.GetAIInsideModelDevicesResult b = new com.dji.flighthubsdk.bean.GetAIInsideModelDevicesResult();
        if (n == null) { return b; }
        b.setTotal(n.getTotal()); b.setPage(n.getPage()); b.setPageSize(n.getPage_size());
        com.dji.flighthubsdk.generated.AIInsideDeviceItemVector vec = n.getList();
        if (vec != null) {
            java.util.List<com.dji.flighthubsdk.bean.AIInsideDeviceItem> list = new java.util.ArrayList<>();
            for (int i = 0; i < vec.size(); i++) { list.add(toAIInsideDeviceItem(vec.get(i))); }
            b.setList(list);
        }
        return b;
    }

    public static com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryResult toGetAIInsideModelDevicesSummaryResult(
            com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryResult n) {
        if (n == null) { return new com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryResult(); }
        return new com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryResult(n.getTotal_devices(), n.getOperable_devices());
    }

    public static com.dji.flighthubsdk.bean.OperateAIInsideModelResult toOperateAIInsideModelResult(
            com.dji.flighthubsdk.generated.OperateAIInsideModelResult n) {
        com.dji.flighthubsdk.bean.OperateAIInsideModelResult b = new com.dji.flighthubsdk.bean.OperateAIInsideModelResult();
        if (n == null) { return b; }
        com.dji.flighthubsdk.generated.AIInsideOperateDeviceResultVector vec = n.getResults();
        if (vec != null) {
            java.util.List<com.dji.flighthubsdk.bean.AIInsideOperateDeviceResult> list = new java.util.ArrayList<>();
            for (int i = 0; i < vec.size(); i++) {
                com.dji.flighthubsdk.bean.AIInsideOperateDeviceResult r = new com.dji.flighthubsdk.bean.AIInsideOperateDeviceResult();
                com.dji.flighthubsdk.generated.AIInsideOperateDeviceResult nr = vec.get(i);
                if (nr != null) { r.setDroneSn(safeString(nr.getDrone_sn())); r.setCode(nr.getCode()); }
                list.add(r);
            }
            b.setResults(list);
        }
        return b;
    }

    public static com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmResult toCreateAIInsideAlgorithmResult(
            com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmResult n) {
        com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmResult b = new com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmResult();
        if (n == null) { return b; }
        b.setProvider(safeString(n.getProvider())); b.setBucket(safeString(n.getBucket()));
        b.setRegion(safeString(n.getRegion())); b.setEndpoint(safeString(n.getEndpoint()));
        b.setFileStoreDir(safeString(n.getFile_store_dir()));
        com.dji.flighthubsdk.generated.AIInsideCredentials nc = n.getCredentials();
        if (nc != null) {
            com.dji.flighthubsdk.bean.AIInsideCredentials bc = new com.dji.flighthubsdk.bean.AIInsideCredentials();
            bc.setAccessKeyId(safeString(nc.getAccess_key_id())); bc.setAccessKeySecret(safeString(nc.getAccess_key_secret()));
            bc.setExpire(nc.getExpire()); bc.setSecurityToken(safeString(nc.getSecurity_token()));
            b.setCredentials(bc);
        }
        return b;
    }

    public static com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmResult toFileUploadAIInsideAlgorithmResult(
            com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmResult n) {
        com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmResult b = new com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmResult();
        if (n == null) { return b; }
        com.dji.flighthubsdk.generated.AIInsideAlgorithmDetail nd = n.getDetail();
        if (nd != null) {
            com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail d = new com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail();
            d.setId(nd.getId()); d.setCreatedAt(safeString(nd.getCreated_at())); d.setUpdatedAt(safeString(nd.getUpdated_at()));
            d.setSignedId(safeString(nd.getSigned_id())); d.setSignedName(safeString(nd.getSigned_name()));
            d.setSignedVersion(nd.getSigned_version()); d.setSignedUtc(nd.getSigned_utc());
            d.setModelUuid(safeString(nd.getModel_uuid())); d.setModelName(safeString(nd.getModel_name()));
            d.setModelType(safeString(nd.getModel_type())); d.setFileKey(safeString(nd.getFile_key()));
            d.setApplicableProduct(new java.util.ArrayList<>(copyIntVector(nd.getApplicable_product())));
            d.setImageSource(new java.util.ArrayList<>(copyIntVector(nd.getImage_source()))); d.setDigitalEffect(new java.util.ArrayList<>(copyIntVector(nd.getDigital_effect())));
            d.setVlResolution(new java.util.ArrayList<>(copyIntVector(nd.getVl_resolution())));
            d.setModelParam(toAIInsideModelParam(nd.getModel_param()));
            d.setLicense(toAIInsideLicense(nd.getLicense()));
            d.setLabels(toAIInsideLabelList(nd.getLabels()));
            b.setDetail(d);
        }
        return b;
    }

    public static com.dji.flighthubsdk.generated.UploadAIInsideAlgorithmFileParam toNativeUploadAlgorithmFileParam(
            com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileParam param) {
        com.dji.flighthubsdk.generated.UploadAIInsideAlgorithmFileParam n =
                new com.dji.flighthubsdk.generated.UploadAIInsideAlgorithmFileParam();
        if (param == null) { return n; }
        n.setFile_path(safeString(param.getFilePath()));
        return n;
    }

    public static com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileResult toUploadAlgorithmFileResult(
            com.dji.flighthubsdk.generated.UploadAIInsideAlgorithmFileResult n) {
        com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileResult b =
                new com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileResult();
        if (n == null) { return b; }
        b.setObjectKey(safeString(n.getObject_key()));
        b.setObjectUrl(safeString(n.getObject_url()));
        b.setEtag(safeString(n.getEtag()));
        // detail 字段复用已有的 AIInsideAlgorithmDetail 转换逻辑
        com.dji.flighthubsdk.generated.AIInsideAlgorithmDetail nd = n.getDetail();
        if (nd != null) {
            com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail d = new com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail();
            d.setId(nd.getId()); d.setCreatedAt(safeString(nd.getCreated_at())); d.setUpdatedAt(safeString(nd.getUpdated_at()));
            d.setSignedId(safeString(nd.getSigned_id())); d.setSignedName(safeString(nd.getSigned_name()));
            d.setSignedVersion(nd.getSigned_version()); d.setSignedUtc(nd.getSigned_utc());
            d.setModelUuid(safeString(nd.getModel_uuid())); d.setModelName(safeString(nd.getModel_name()));
            d.setModelType(safeString(nd.getModel_type())); d.setFileKey(safeString(nd.getFile_key()));
            d.setApplicableProduct(new java.util.ArrayList<>(copyIntVector(nd.getApplicable_product())));
            d.setImageSource(new java.util.ArrayList<>(copyIntVector(nd.getImage_source())));
            d.setDigitalEffect(new java.util.ArrayList<>(copyIntVector(nd.getDigital_effect())));
            d.setVlResolution(new java.util.ArrayList<>(copyIntVector(nd.getVl_resolution())));
            d.setModelParam(toAIInsideModelParam(nd.getModel_param()));
            d.setLicense(toAIInsideLicense(nd.getLicense()));
            d.setLabels(toAIInsideLabelList(nd.getLabels()));
            b.setDetail(d);
        }
        return b;
    }

    // -------------------------------------------------------------------------
    // DRC parameter toNative conversion methods
    // -------------------------------------------------------------------------

    public static com.dji.flighthubsdk.generated.CameraApertureValueSetParams toNativeCameraApertureValueSetParams(
            com.dji.flighthubsdk.bean.CameraApertureValueSetParams bean) {
        com.dji.flighthubsdk.generated.CameraApertureValueSetParams n =
                new com.dji.flighthubsdk.generated.CameraApertureValueSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setAperture_value(bean.getApertureValue());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraExposureModeSetParams toNativeCameraExposureModeSetParams(
            com.dji.flighthubsdk.bean.CameraExposureModeSetParams bean) {
        com.dji.flighthubsdk.generated.CameraExposureModeSetParams n =
                new com.dji.flighthubsdk.generated.CameraExposureModeSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setExposure_mode(bean.getExposureMode());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraExposureSetParams toNativeCameraExposureSetParams(
            com.dji.flighthubsdk.bean.CameraExposureSetParams bean) {
        com.dji.flighthubsdk.generated.CameraExposureSetParams n =
                new com.dji.flighthubsdk.generated.CameraExposureSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setExposure_value(bean.getExposureValue());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraFocusModeSetParams toNativeCameraFocusModeSetParams(
            com.dji.flighthubsdk.bean.CameraFocusModeSetParams bean) {
        com.dji.flighthubsdk.generated.CameraFocusModeSetParams n =
                new com.dji.flighthubsdk.generated.CameraFocusModeSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setFocus_mode(bean.getFocusMode());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraFocusValueSetParams toNativeCameraFocusValueSetParams(
            com.dji.flighthubsdk.bean.CameraFocusValueSetParams bean) {
        com.dji.flighthubsdk.generated.CameraFocusValueSetParams n =
                new com.dji.flighthubsdk.generated.CameraFocusValueSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setFocus_value(bean.getFocusValue());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraIsoSetParams toNativeCameraIsoSetParams(
            com.dji.flighthubsdk.bean.CameraIsoSetParams bean) {
        com.dji.flighthubsdk.generated.CameraIsoSetParams n =
                new com.dji.flighthubsdk.generated.CameraIsoSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setIso_value(bean.getIsoValue());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraMechanicalShutterSetParams toNativeCameraMechanicalShutterSetParams(
            com.dji.flighthubsdk.bean.CameraMechanicalShutterSetParams bean) {
        com.dji.flighthubsdk.generated.CameraMechanicalShutterSetParams n =
                new com.dji.flighthubsdk.generated.CameraMechanicalShutterSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setState(bean.getState());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraPointFocusActionParams toNativeCameraPointFocusActionParams(
            com.dji.flighthubsdk.bean.CameraPointFocusActionParams bean) {
        com.dji.flighthubsdk.generated.CameraPointFocusActionParams n =
                new com.dji.flighthubsdk.generated.CameraPointFocusActionParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setX(bean.getX());
        n.setY(bean.getY());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraShutterSetParams toNativeCameraShutterSetParams(
            com.dji.flighthubsdk.bean.CameraShutterSetParams bean) {
        com.dji.flighthubsdk.generated.CameraShutterSetParams n =
                new com.dji.flighthubsdk.generated.CameraShutterSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setShutter_value(bean.getShutterValue());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraWatermarkParams toNativeCameraWatermarkParams(
            com.dji.flighthubsdk.bean.CameraWatermarkParams bean) {
        com.dji.flighthubsdk.generated.CameraWatermarkParams n =
                new com.dji.flighthubsdk.generated.CameraWatermarkParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setGlobal_enable(bean.getGlobalEnable());
        n.setDrone_type_enable(bean.getDroneTypeEnable());
        n.setManufacturer_enable(bean.getManufacturerEnable());
        n.setDrone_sn_enable(bean.getDroneSNEnable());
        n.setDatetime_enable(bean.getDatetimeEnable());
        n.setGps_enable(bean.getGpsEnable());
        n.setUser_custom_string_enable(bean.getUserCustomStringEnable());
        n.setUser_custom_string(bean.getUserCustomString() != null ? bean.getUserCustomString() : "");
        n.setLayout(bean.getLayout());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraDewarpingSetParams toNativeCameraDewarpingSetParams(
            com.dji.flighthubsdk.bean.CameraDewarpingSetParams bean) {
        com.dji.flighthubsdk.generated.CameraDewarpingSetParams n =
                new com.dji.flighthubsdk.generated.CameraDewarpingSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setDewarping_state(bean.getDewarpingState());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraScreenSplitParams toNativeCameraScreenSplitParams(
            com.dji.flighthubsdk.bean.CameraScreenSplitParams bean) {
        com.dji.flighthubsdk.generated.CameraScreenSplitParams n =
                new com.dji.flighthubsdk.generated.CameraScreenSplitParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnable(bean.isEnable());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CleanFogParams toNativeCleanFogParams(
            com.dji.flighthubsdk.bean.CleanFogParams bean) {
        com.dji.flighthubsdk.generated.CleanFogParams n =
                new com.dji.flighthubsdk.generated.CleanFogParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.ThermalCurrentPaletteSetParams toNativeThermalCurrentPaletteSetParams(
            com.dji.flighthubsdk.bean.ThermalCurrentPaletteSetParams bean) {
        com.dji.flighthubsdk.generated.ThermalCurrentPaletteSetParams n =
                new com.dji.flighthubsdk.generated.ThermalCurrentPaletteSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setPalette_style(bean.getPaletteStyle());
        return n;
    }

    public static com.dji.flighthubsdk.generated.ThermalSuperResolutionEnableParams toNativeThermalSuperResolutionEnableParams(
            com.dji.flighthubsdk.bean.ThermalSuperResolutionEnableParams bean) {
        com.dji.flighthubsdk.generated.ThermalSuperResolutionEnableParams n =
                new com.dji.flighthubsdk.generated.ThermalSuperResolutionEnableParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnable(bean.getEnable());
        return n;
    }

    public static com.dji.flighthubsdk.generated.IrMeteringModeSetParams toNativeIrMeteringModeSetParams(
            com.dji.flighthubsdk.bean.IrMeteringModeSetParams bean) {
        com.dji.flighthubsdk.generated.IrMeteringModeSetParams n =
                new com.dji.flighthubsdk.generated.IrMeteringModeSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setMode(bean.getMode());
        return n;
    }

    public static com.dji.flighthubsdk.generated.IrMeteringPointSetParams toNativeIrMeteringPointSetParams(
            com.dji.flighthubsdk.bean.IrMeteringPointSetParams bean) {
        com.dji.flighthubsdk.generated.IrMeteringPointSetParams n =
                new com.dji.flighthubsdk.generated.IrMeteringPointSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setX(bean.getX());
        n.setY(bean.getY());
        return n;
    }

    public static com.dji.flighthubsdk.generated.IrMeteringAreaSetParams toNativeIrMeteringAreaSetParams(
            com.dji.flighthubsdk.bean.IrMeteringAreaSetParams bean) {
        com.dji.flighthubsdk.generated.IrMeteringAreaSetParams n =
                new com.dji.flighthubsdk.generated.IrMeteringAreaSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setX(bean.getX());
        n.setY(bean.getY());
        n.setWidth(bean.getWidth());
        n.setHeight(bean.getHeight());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIrDisplayModeParams toNativeSetIrDisplayModeParams(
            com.dji.flighthubsdk.bean.SetIrDisplayModeParams bean) {
        com.dji.flighthubsdk.generated.SetIrDisplayModeParams n =
                new com.dji.flighthubsdk.generated.SetIrDisplayModeParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setDisplay_mode(bean.getDisplayMode());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIrEnhanceModeParams toNativeSetIrEnhanceModeParams(
            com.dji.flighthubsdk.bean.SetIrEnhanceModeParams bean) {
        com.dji.flighthubsdk.generated.SetIrEnhanceModeParams n =
                new com.dji.flighthubsdk.generated.SetIrEnhanceModeParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnhance_mode(bean.getEnhanceMode());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIrMixRateParams toNativeSetIrMixRateParams(
            com.dji.flighthubsdk.bean.SetIrMixRateParams bean) {
        com.dji.flighthubsdk.generated.SetIrMixRateParams n =
                new com.dji.flighthubsdk.generated.SetIrMixRateParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setIr_mix_rate(bean.getIrMixRate());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIsothermEnableParams toNativeSetIsothermEnableParams(
            com.dji.flighthubsdk.bean.SetIsothermEnableParams bean) {
        com.dji.flighthubsdk.generated.SetIsothermEnableParams n =
                new com.dji.flighthubsdk.generated.SetIsothermEnableParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnable(bean.getEnable());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIsothermColorRenderingTypeParams toNativeSetIsothermColorRenderingTypeParams(
            com.dji.flighthubsdk.bean.SetIsothermColorRenderingTypeParams bean) {
        com.dji.flighthubsdk.generated.SetIsothermColorRenderingTypeParams n =
                new com.dji.flighthubsdk.generated.SetIsothermColorRenderingTypeParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setType(bean.getType());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIsothermLowerUpperRangeParams toNativeSetIsothermLowerUpperRangeParams(
            com.dji.flighthubsdk.bean.SetIsothermLowerUpperRangeParams bean) {
        com.dji.flighthubsdk.generated.SetIsothermLowerUpperRangeParams n =
                new com.dji.flighthubsdk.generated.SetIsothermLowerUpperRangeParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setLower(bean.getLower());
        n.setUpper(bean.getUpper());
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIsothermRenderingPolygonParams toNativeSetIsothermRenderingPolygonParams(
            com.dji.flighthubsdk.bean.SetIsothermRenderingPolygonParams bean) {
        com.dji.flighthubsdk.generated.SetIsothermRenderingPolygonParams n =
                new com.dji.flighthubsdk.generated.SetIsothermRenderingPolygonParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        // Note: the polygon field uses an opaque SWIG vector type
        // (SWIGTYPE_p_std__vectorT_flight_hub_sdk__IsothermPolygonPoint_t) that has no
        // exposed Java-level add/set methods. Population of the polygon field must be
        // handled at the JNI level or deferred until a proper vector wrapper is generated.
        java.util.List<com.dji.flighthubsdk.bean.IsothermPolygonPoint> points = bean.getPolygon();
        if (points != null && !points.isEmpty()) {
            com.dji.flighthubsdk.generated.IsothermPolygonPointVector vec =
                    new com.dji.flighthubsdk.generated.IsothermPolygonPointVector();
            for (com.dji.flighthubsdk.bean.IsothermPolygonPoint p : points) {
                if (p != null) {
                    com.dji.flighthubsdk.generated.IsothermPolygonPoint np =
                            new com.dji.flighthubsdk.generated.IsothermPolygonPoint();
                    np.setX(p.getX());
                    np.setY(p.getY());
                    vec.add(np);
                }
            }
            n.setPolygon(vec);
        }
        return n;
    }

    public static com.dji.flighthubsdk.generated.SetIsothermRoiBasedRenderingEnableParams toNativeSetIsothermRoiBasedRenderingEnableParams(
            com.dji.flighthubsdk.bean.SetIsothermRoiBasedRenderingEnableParams bean) {
        com.dji.flighthubsdk.generated.SetIsothermRoiBasedRenderingEnableParams n =
                new com.dji.flighthubsdk.generated.SetIsothermRoiBasedRenderingEnableParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnable(bean.getEnable());
        return n;
    }

    public static com.dji.flighthubsdk.generated.InfraredFillLightEnableParams toNativeInfraredFillLightEnableParams(
            com.dji.flighthubsdk.bean.InfraredFillLightEnableParams bean) {
        com.dji.flighthubsdk.generated.InfraredFillLightEnableParams n =
                new com.dji.flighthubsdk.generated.InfraredFillLightEnableParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnable(bean.isEnable());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraNightVisionEnableParams toNativeCameraNightVisionEnableParams(
            com.dji.flighthubsdk.bean.CameraNightVisionEnableParams bean) {
        com.dji.flighthubsdk.generated.CameraNightVisionEnableParams n =
                new com.dji.flighthubsdk.generated.CameraNightVisionEnableParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setEnable(bean.isEnable());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraSettingsResetParams toNativeCameraSettingsResetParams(
            com.dji.flighthubsdk.bean.CameraSettingsResetParams bean) {
        com.dji.flighthubsdk.generated.CameraSettingsResetParams n =
                new com.dji.flighthubsdk.generated.CameraSettingsResetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraDenoiseLevelSetParams toNativeCameraDenoiseLevelSetParams(
            com.dji.flighthubsdk.bean.CameraDenoiseLevelSetParams bean) {
        com.dji.flighthubsdk.generated.CameraDenoiseLevelSetParams n =
                new com.dji.flighthubsdk.generated.CameraDenoiseLevelSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setLevel(bean.getLevel());
        return n;
    }

    public static com.dji.flighthubsdk.generated.VideoResolutionSetParams toNativeVideoResolutionSetParams(
            com.dji.flighthubsdk.bean.VideoResolutionSetParams bean) {
        com.dji.flighthubsdk.generated.VideoResolutionSetParams n =
                new com.dji.flighthubsdk.generated.VideoResolutionSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setCamera_lens_type(bean.getCameraLensType());
        n.setVideo_resolution(bean.getVideoResolution());
        return n;
    }

    public static com.dji.flighthubsdk.generated.CameraPhotoFormatSetParams toNativeCameraPhotoFormatSetParams(
            com.dji.flighthubsdk.bean.CameraPhotoFormatSetParams bean) {
        com.dji.flighthubsdk.generated.CameraPhotoFormatSetParams n =
                new com.dji.flighthubsdk.generated.CameraPhotoFormatSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setPhoto_format(bean.getPhotoFormat());
        return n;
    }

    public static com.dji.flighthubsdk.generated.PreRecordedStatusSetParams toNativePreRecordedStatusSetParams(
            com.dji.flighthubsdk.bean.PreRecordedStatusSetParams bean) {
        com.dji.flighthubsdk.generated.PreRecordedStatusSetParams n =
                new com.dji.flighthubsdk.generated.PreRecordedStatusSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setStatus(bean.getStatus());
        return n;
    }

    public static com.dji.flighthubsdk.generated.PreRecordedTimeSetParams toNativePreRecordedTimeSetParams(
            com.dji.flighthubsdk.bean.PreRecordedTimeSetParams bean) {
        com.dji.flighthubsdk.generated.PreRecordedTimeSetParams n =
                new com.dji.flighthubsdk.generated.PreRecordedTimeSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setPre_recorded_time(bean.getPreRecordedTime());
        return n;
    }

    public static com.dji.flighthubsdk.generated.AlgorithmDehazeModeSetParams toNativeAlgorithmDehazeModeSetParams(
            com.dji.flighthubsdk.bean.AlgorithmDehazeModeSetParams bean) {
        com.dji.flighthubsdk.generated.AlgorithmDehazeModeSetParams n =
                new com.dji.flighthubsdk.generated.AlgorithmDehazeModeSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setMode(bean.getMode());
        return n;
    }

    public static com.dji.flighthubsdk.generated.AlgorithmDehazeLevelSetParams toNativeAlgorithmDehazeLevelSetParams(
            com.dji.flighthubsdk.bean.AlgorithmDehazeLevelSetParams bean) {
        com.dji.flighthubsdk.generated.AlgorithmDehazeLevelSetParams n =
                new com.dji.flighthubsdk.generated.AlgorithmDehazeLevelSetParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setLevel(bean.getLevel());
        return n;
    }

    public static com.dji.flighthubsdk.generated.AiSpotlightZoomSelectParams toNativeAiSpotlightZoomSelectParams(
            com.dji.flighthubsdk.bean.AiSpotlightZoomSelectParams bean) {
        com.dji.flighthubsdk.generated.AiSpotlightZoomSelectParams n =
                new com.dji.flighthubsdk.generated.AiSpotlightZoomSelectParams();
        if (bean == null) return n;
        n.setCenter_x(bean.getCenterX());
        n.setCenter_y(bean.getCenterY());
        n.setWidth(bean.getWidth());
        n.setHeight(bean.getHeight());
        return n;
    }

    // -------------------------------------------------------------------------
    // DRC 高级参数（含嵌套 vector）— 需 SWIG regen 后可用
    // 依赖 flight_hub_sdk.i 中新增的 %template：
    //   AiAlarmTargetVector / AiAlarmStreamInputConfVector /
    //   AiModelsConfigSetVector / FlashLedPatternInfoVector
    // -------------------------------------------------------------------------

    public static com.dji.flighthubsdk.generated.AiAlarmTarget toNativeAiAlarmTarget(
            com.dji.flighthubsdk.bean.AiAlarmTarget bean) {
        com.dji.flighthubsdk.generated.AiAlarmTarget n =
                new com.dji.flighthubsdk.generated.AiAlarmTarget();
        if (bean == null) return n;
        n.setTarget_type(bean.getTargetType());
        n.setTarget_value(bean.getTargetValue());
        n.setUse_min_threshold(bean.isUseMinThreshold());
        n.setUse_max_threshold(bean.isUseMaxThreshold());
        n.setTarget_min_threshold(bean.getTargetMinThreshold());
        n.setTarget_max_threshold(bean.getTargetMaxThreshold());
        return n;
    }

    public static com.dji.flighthubsdk.generated.AiAlarmTargetVector toNativeAiAlarmTargetVector(
            List<com.dji.flighthubsdk.bean.AiAlarmTarget> beans) {
        com.dji.flighthubsdk.generated.AiAlarmTargetVector v =
                new com.dji.flighthubsdk.generated.AiAlarmTargetVector();
        if (beans == null) return v;
        for (com.dji.flighthubsdk.bean.AiAlarmTarget b : beans) {
            v.add(toNativeAiAlarmTarget(b));
        }
        return v;
    }

    public static com.dji.flighthubsdk.generated.AiAlarmStreamInputConf toNativeAiAlarmStreamInputConf(
            com.dji.flighthubsdk.bean.AiAlarmStreamInputConf bean) {
        com.dji.flighthubsdk.generated.AiAlarmStreamInputConf n =
                new com.dji.flighthubsdk.generated.AiAlarmStreamInputConf();
        if (bean == null) return n;
        n.setStream_source(bean.getStreamSource());
        n.setModel_idx(bean.getModelIdx());
        n.setAlgorithm_type(bean.getAlgorithmType());
        n.setAlarm_interval(bean.getAlarmInterval());
        n.setAlarm_targets(toNativeAiAlarmTargetVector(bean.getAlarmTargets()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.AiAlarmStreamInputConfVector toNativeAiAlarmStreamInputConfVector(
            List<com.dji.flighthubsdk.bean.AiAlarmStreamInputConf> beans) {
        com.dji.flighthubsdk.generated.AiAlarmStreamInputConfVector v =
                new com.dji.flighthubsdk.generated.AiAlarmStreamInputConfVector();
        if (beans == null) return v;
        for (com.dji.flighthubsdk.bean.AiAlarmStreamInputConf b : beans) {
            v.add(toNativeAiAlarmStreamInputConf(b));
        }
        return v;
    }

    public static com.dji.flighthubsdk.generated.AiAlarmStartParams toNativeAiAlarmStartParams(
            com.dji.flighthubsdk.bean.AiAlarmStartParams bean) {
        com.dji.flighthubsdk.generated.AiAlarmStartParams n =
                new com.dji.flighthubsdk.generated.AiAlarmStartParams();
        if (bean == null) return n;
        n.setStream_input_confs(toNativeAiAlarmStreamInputConfVector(bean.getStreamInputConfs()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.AiModelsConfigSet toNativeAiModelsConfigSet(
            com.dji.flighthubsdk.bean.AiModelsConfigSet bean) {
        com.dji.flighthubsdk.generated.AiModelsConfigSet n =
                new com.dji.flighthubsdk.generated.AiModelsConfigSet();
        if (bean == null) return n;
        n.setStream_type(bean.getStreamType());
        com.dji.flighthubsdk.generated.IntVector iv = new com.dji.flighthubsdk.generated.IntVector();
        if (bean.getModels() != null) {
            for (Integer i : bean.getModels()) {
                iv.add(i != null ? i : 0);
            }
        }
        n.setModels(iv);
        return n;
    }

    public static com.dji.flighthubsdk.generated.AiModelsConfigSetVector toNativeAiModelsConfigSetVector(
            List<com.dji.flighthubsdk.bean.AiModelsConfigSet> beans) {
        com.dji.flighthubsdk.generated.AiModelsConfigSetVector v =
                new com.dji.flighthubsdk.generated.AiModelsConfigSetVector();
        if (beans == null) return v;
        for (com.dji.flighthubsdk.bean.AiModelsConfigSet b : beans) {
            v.add(toNativeAiModelsConfigSet(b));
        }
        return v;
    }

    public static com.dji.flighthubsdk.generated.AiModelsConfigParams toNativeAiModelsConfigParams(
            com.dji.flighthubsdk.bean.AiModelsConfigParams bean) {
        com.dji.flighthubsdk.generated.AiModelsConfigParams n =
                new com.dji.flighthubsdk.generated.AiModelsConfigParams();
        if (bean == null) return n;
        n.setAi_models_config(toNativeAiModelsConfigSetVector(bean.getAiModelsConfig()));
        return n;
    }

    public static com.dji.flighthubsdk.generated.FlashLedPatternInfo toNativeFlashLedPatternInfo(
            com.dji.flighthubsdk.bean.FlashLedPatternInfo bean) {
        com.dji.flighthubsdk.generated.FlashLedPatternInfo n =
                new com.dji.flighthubsdk.generated.FlashLedPatternInfo();
        if (bean == null) return n;
        n.setColor(bean.getColor());
        n.setPeriod(bean.getPeriod());
        n.setBrightness(bean.getBrightness());
        return n;
    }

    public static com.dji.flighthubsdk.generated.FlashLedPatternInfoVector toNativeFlashLedPatternInfoVector(
            List<com.dji.flighthubsdk.bean.FlashLedPatternInfo> beans) {
        com.dji.flighthubsdk.generated.FlashLedPatternInfoVector v =
                new com.dji.flighthubsdk.generated.FlashLedPatternInfoVector();
        if (beans == null) return v;
        for (com.dji.flighthubsdk.bean.FlashLedPatternInfo b : beans) {
            v.add(toNativeFlashLedPatternInfo(b));
        }
        return v;
    }

    public static com.dji.flighthubsdk.generated.FlashLedParams toNativeFlashLedParams(
            com.dji.flighthubsdk.bean.FlashLedParams bean) {
        com.dji.flighthubsdk.generated.FlashLedParams n =
                new com.dji.flighthubsdk.generated.FlashLedParams();
        if (bean == null) return n;
        n.setOn(bean.getOn());
        n.setPattern_uuid(bean.getPatternUuid() != null ? bean.getPatternUuid() : "");
        n.setPattern_name(bean.getPatternName() != null ? bean.getPatternName() : "");
        n.setPatterns(toNativeFlashLedPatternInfoVector(bean.getPatterns()));
        return n;
    }

    public static com.dji.flighthubsdk.bean.FlashLedPatternInfo toFlashLedPatternInfo(
            com.dji.flighthubsdk.generated.FlashLedPatternInfo n) {
        com.dji.flighthubsdk.bean.FlashLedPatternInfo bean =
                new com.dji.flighthubsdk.bean.FlashLedPatternInfo();
        if (n == null) return bean;
        bean.setColor(n.getColor());
        bean.setPeriod(n.getPeriod());
        bean.setBrightness(n.getBrightness());
        return bean;
    }

    public static List<com.dji.flighthubsdk.bean.FlashLedPatternInfo> toFlashLedPatternInfoList(
            com.dji.flighthubsdk.generated.FlashLedPatternInfoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.FlashLedPatternInfo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFlashLedPatternInfo(v.get(i)));
        }
        return out;
    }

    public static com.dji.flighthubsdk.bean.FlashLedStateInfo toFlashLedStateInfo(
            com.dji.flighthubsdk.generated.FlashLedStateInfo n) {
        com.dji.flighthubsdk.bean.FlashLedStateInfo bean =
                new com.dji.flighthubsdk.bean.FlashLedStateInfo();
        if (n == null) return bean;
        bean.setOn(n.getOn());
        bean.setPatternUuid(safeString(n.getPattern_uuid()));
        bean.setPatternName(safeString(n.getPattern_name()));
        bean.setPatterns(toFlashLedPatternInfoList(n.getPatterns()));
        return bean;
    }

    public static com.dji.flighthubsdk.generated.FlashLedSegment toNativeFlashLedSegment(
            com.dji.flighthubsdk.bean.FlashLedSegment bean) {
        com.dji.flighthubsdk.generated.FlashLedSegment n =
                new com.dji.flighthubsdk.generated.FlashLedSegment();
        if (bean == null) return n;
        n.setColor(bean.getColor());
        n.setCount(bean.getCount());
        n.setOn(bean.getOn());
        n.setOff(bean.getOff());
        n.setInterval(bean.getInterval());
        return n;
    }

    public static com.dji.flighthubsdk.generated.FlashLedSegmentVector toNativeFlashLedSegmentVector(
            List<com.dji.flighthubsdk.bean.FlashLedSegment> beans) {
        com.dji.flighthubsdk.generated.FlashLedSegmentVector v =
                new com.dji.flighthubsdk.generated.FlashLedSegmentVector();
        if (beans == null) return v;
        for (com.dji.flighthubsdk.bean.FlashLedSegment b : beans) {
            v.add(toNativeFlashLedSegment(b));
        }
        return v;
    }

    public static com.dji.flighthubsdk.bean.FlashLedSegment toFlashLedSegment(
            com.dji.flighthubsdk.generated.FlashLedSegment n) {
        com.dji.flighthubsdk.bean.FlashLedSegment bean =
                new com.dji.flighthubsdk.bean.FlashLedSegment();
        if (n == null) return bean;
        bean.setColor(n.getColor());
        bean.setCount(n.getCount());
        bean.setOn(n.getOn());
        bean.setOff(n.getOff());
        bean.setInterval(n.getInterval());
        return bean;
    }

    public static List<com.dji.flighthubsdk.bean.FlashLedSegment> toFlashLedSegmentList(
            com.dji.flighthubsdk.generated.FlashLedSegmentVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.FlashLedSegment> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFlashLedSegment(v.get(i)));
        }
        return out;
    }

    public static com.dji.flighthubsdk.generated.FlashLedConfigParams toNativeFlashLedConfigParams(
            com.dji.flighthubsdk.bean.FlashLedConfigParams bean) {
        com.dji.flighthubsdk.generated.FlashLedConfigParams n =
                new com.dji.flighthubsdk.generated.FlashLedConfigParams();
        if (bean == null) return n;
        n.setName(bean.getName() != null ? bean.getName() : "");
        n.setFlash_segments(toNativeFlashLedSegmentVector(bean.getFlashSegments()));
        return n;
    }

    public static com.dji.flighthubsdk.bean.FlashLedConfig toFlashLedConfig(
            com.dji.flighthubsdk.generated.FlashLedConfig n) {
        com.dji.flighthubsdk.bean.FlashLedConfig bean =
                new com.dji.flighthubsdk.bean.FlashLedConfig();
        if (n == null) return bean;
        bean.setId(safeString(n.getId()));
        bean.setName(safeString(n.getName()));
        bean.setFlashSegments(toFlashLedSegmentList(n.getFlash_segments()));
        return bean;
    }

    public static List<com.dji.flighthubsdk.bean.FlashLedConfig> toFlashLedConfigList(
            com.dji.flighthubsdk.generated.FlashLedConfigVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.FlashLedConfig> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toFlashLedConfig(v.get(i)));
        }
        return out;
    }

    public static com.dji.flighthubsdk.bean.FlashLedConfigList toFlashLedConfigList(
            com.dji.flighthubsdk.generated.FlashLedConfigList n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.FlashLedConfigList(Collections.emptyList());
        }
        return new com.dji.flighthubsdk.bean.FlashLedConfigList(toFlashLedConfigList(n.getConfigs()));
    }

    public static com.dji.flighthubsdk.generated.CameraLookAtParams toNativeCameraLookAtParams(
            com.dji.flighthubsdk.bean.CameraLookAtParams bean) {
        com.dji.flighthubsdk.generated.CameraLookAtParams n =
                 new com.dji.flighthubsdk.generated.CameraLookAtParams();
        if (bean == null) return n;
        n.setPayload_index(bean.getPayloadIndex() != null ? bean.getPayloadIndex() : "");
        n.setLocked(bean.isLocked());
        n.setHeight(bean.getHeight());
        n.setLatitude(bean.getLatitude());
        n.setLongitude(bean.getLongitude());
        return n;
    }

    // AI 告警管理转换方法
    private static com.dji.flighthubsdk.generated.AiAlertAlarmInfo toNativeAiAlertAlarmInfo(
            AiAlertAlarmInfo bean) {
        com.dji.flighthubsdk.generated.AiAlertAlarmInfo n = new com.dji.flighthubsdk.generated.AiAlertAlarmInfo();
        if (bean == null) { return n; }
        n.setAlarm_interval(bean.getAlarmInterval());
        if (bean.getAlarmMethod() != null && !bean.getAlarmMethod().isEmpty()) {
            com.dji.flighthubsdk.generated.IntVector v = new com.dji.flighthubsdk.generated.IntVector();
            for (Integer m : bean.getAlarmMethod()) { v.add(m); }
            n.setAlarm_method(v);
        }
        return n;
    }

    private static AiAlertAlarmInfo toAiAlertAlarmInfo(
            com.dji.flighthubsdk.generated.AiAlertAlarmInfo n) {
        AiAlertAlarmInfo b = new AiAlertAlarmInfo();
        if (n == null) { return b; }
        b.setAlarmInterval(n.getAlarm_interval());
        b.setAlarmMethod(new ArrayList<>(copyIntVector(n.getAlarm_method())));
        return b;
    }

    private static com.dji.flighthubsdk.generated.AiAlertAlarmTarget toNativeAiAlertAlarmTarget(
            AiAlertAlarmTarget bean) {
        com.dji.flighthubsdk.generated.AiAlertAlarmTarget n = new com.dji.flighthubsdk.generated.AiAlertAlarmTarget();
        if (bean == null) { return n; }
        n.setTarget_type(bean.getTargetType());
        n.setTarget_value(bean.getTargetValue());
        n.setUse_min_threshold(bean.isUseMinThreshold());
        n.setUse_max_threshold(bean.isUseMaxThreshold());
        n.setTarget_max_threshold(bean.getTargetMaxThreshold());
        n.setTarget_min_threshold(bean.getTargetMinThreshold());
        return n;
    }

    private static AiAlertAlarmTarget toAiAlertAlarmTarget(
            com.dji.flighthubsdk.generated.AiAlertAlarmTarget n) {
        AiAlertAlarmTarget b = new AiAlertAlarmTarget();
        if (n == null) { return b; }
        b.setTargetType(n.getTarget_type());
        b.setTargetValue(n.getTarget_value());
        b.setUseMinThreshold(n.getUse_min_threshold());
        b.setUseMaxThreshold(n.getUse_max_threshold());
        b.setTargetMaxThreshold(n.getTarget_max_threshold());
        b.setTargetMinThreshold(n.getTarget_min_threshold());
        return b;
    }

    private static AiAlertLocation toAiAlertLocation(
            com.dji.flighthubsdk.generated.AiAlertLocation n) {
        AiAlertLocation b = new AiAlertLocation();
        if (n == null) { return b; }
        b.setLatitude(n.getLatitude());
        b.setLongitude(n.getLongitude());
        b.setAltitude(n.getAltitude());
        return b;
    }

    private static AiAlertTargetAlertInfo toAiAlertTargetAlertInfo(
            com.dji.flighthubsdk.generated.AiAlertTargetAlertInfo n) {
        AiAlertTargetAlertInfo b = new AiAlertTargetAlertInfo();
        if (n == null) { return b; }
        b.setTargetType(n.getTarget_type());
        b.setTargetValue(n.getTarget_value());
        b.setLabel(safeString(n.getLabel()));
        return b;
    }

    private static AiAlertFlightItem toAiAlertFlightItem(
            com.dji.flighthubsdk.generated.AiAlertFlightItem n) {
        AiAlertFlightItem b = new AiAlertFlightItem();
        if (n == null) { return b; }
        b.setFlightId(safeString(n.getFlight_id()));
        b.setCount(n.getCount());
        b.setFlightTaskName(safeString(n.getFlight_task_name()));
        b.setStatus(n.getStatus());
        b.setStartTime(n.getStart_time());
        return b;
    }

    private static AiAlertRecordItem toAiAlertRecordItem(
            com.dji.flighthubsdk.generated.AiAlertRecordItem n) {
        AiAlertRecordItem b = new AiAlertRecordItem();
        if (n == null) { return b; }
        b.setAlertUuid(safeString(n.getAlert_uuid()));
        b.setFlightId(safeString(n.getFlight_id()));
        b.setDroneSn(safeString(n.getDrone_sn()));
        b.setGatewaySn(safeString(n.getGateway_sn()));
        b.setStatus(n.getStatus());
        b.setAlgorithmSource(n.getAlgorithm_source());
        b.setLocation(toAiAlertLocation(n.getLocation()));
        b.setThumbnailUrl(safeString(n.getThumbnail_url()));
        b.setTimestamp(n.getTimestamp());
        b.setTaskName(safeString(n.getTask_name()));
        b.setMediaIndex(n.getMedia_index());
        com.dji.flighthubsdk.generated.AiAlertTargetAlertInfoVector infos = n.getTarget_alert_infos();
        if (infos != null) {
            List<AiAlertTargetAlertInfo> list = new ArrayList<>();
            for (int i = 0; i < infos.size(); i++) { list.add(toAiAlertTargetAlertInfo(infos.get(i))); }
            b.setTargetAlertInfos(list);
        }
        b.setLabels(new ArrayList<>(copyStringVector(n.getLabels())));
        return b;
    }

    public static com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingParam toNativePostAiAlertDeviceSettingParam(
            PostAiAlertDeviceSettingParam param) {
        com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingParam n = new com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingParam();
        if (param == null) { return n; }
        n.setDrone_sn(safeString(param.getDroneSn()));
        n.setEnable_target_alert(param.isEnableTargetAlert());
        if (param.getAlarmInfo() != null) {
            n.setAlarm_info(toNativeAiAlertAlarmInfo(param.getAlarmInfo()));
        }
        return n;
    }

    public static PostAiAlertDeviceSettingResult toPostAiAlertDeviceSettingResult(
            com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingResult n) {
        return new PostAiAlertDeviceSettingResult();
    }

    public static com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingParam toNativeGetAiAlertDeviceSettingParam(
            GetAiAlertDeviceSettingParam param) {
        com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingParam n = new com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingParam();
        if (param == null) { return n; }
        n.setDrone_sn(safeString(param.getDroneSn()));
        return n;
    }

    public static GetAiAlertDeviceSettingResult toGetAiAlertDeviceSettingResult(
            com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingResult n) {
        GetAiAlertDeviceSettingResult b = new GetAiAlertDeviceSettingResult();
        if (n == null) { return b; }
        b.setDroneSn(safeString(n.getDrone_sn()));
        b.setEnableTargetAlert(n.getEnable_target_alert());
        b.setAlarmInfo(toAiAlertAlarmInfo(n.getAlarm_info()));
        return b;
    }

    public static com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingParam toNativePostAiAlertAlgorithmSettingParam(
            PostAiAlertAlgorithmSettingParam param) {
        com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingParam n = new com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingParam();
        if (param == null) { return n; }
        n.setDrone_sn(safeString(param.getDroneSn()));
        n.setAlgorithm_type(param.getAlgorithmType());
        n.setAlgorithm_name(safeString(param.getAlgorithmName()));
        if (param.getAlarmTargets() != null && !param.getAlarmTargets().isEmpty()) {
            com.dji.flighthubsdk.generated.AiAlertAlarmTargetVector v = new com.dji.flighthubsdk.generated.AiAlertAlarmTargetVector();
            for (AiAlertAlarmTarget t : param.getAlarmTargets()) { v.add(toNativeAiAlertAlarmTarget(t)); }
            n.setAlarm_targets(v);
        }
        return n;
    }

    public static PostAiAlertAlgorithmSettingResult toPostAiAlertAlgorithmSettingResult(
            com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingResult n) {
        return new PostAiAlertAlgorithmSettingResult();
    }

    public static com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingParam toNativeGetAiAlertAlgorithmSettingParam(
            GetAiAlertAlgorithmSettingParam param) {
        com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingParam n = new com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingParam();
        if (param == null) { return n; }
        n.setDrone_sn(safeString(param.getDroneSn()));
        n.setAlgorithm_type(param.getAlgorithmType());
        return n;
    }

    public static GetAiAlertAlgorithmSettingResult toGetAiAlertAlgorithmSettingResult(
            com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingResult n) {
        GetAiAlertAlgorithmSettingResult b = new GetAiAlertAlgorithmSettingResult();
        if (n == null) { return b; }
        b.setDroneSn(safeString(n.getDrone_sn()));
        b.setAlgorithmType(n.getAlgorithm_type());
        b.setAlgorithmName(safeString(n.getAlgorithm_name()));
        com.dji.flighthubsdk.generated.AiAlertAlarmTargetVector vec = n.getAlarm_targets();
        if (vec != null) {
            List<AiAlertAlarmTarget> list = new ArrayList<>();
            for (int i = 0; i < vec.size(); i++) { list.add(toAiAlertAlarmTarget(vec.get(i))); }
            b.setAlarmTargets(list);
        }
        return b;
    }

    public static com.dji.flighthubsdk.generated.GetAiAlertFlightsParam toNativeGetAiAlertFlightsParam(
            GetAiAlertFlightsParam param) {
        com.dji.flighthubsdk.generated.GetAiAlertFlightsParam n = new com.dji.flighthubsdk.generated.GetAiAlertFlightsParam();
        if (param == null) { return n; }
        n.setDrone_sn(safeString(param.getDroneSn()));
        n.setBegin_at(param.getBeginAt());
        n.setEnd_at(param.getEndAt());
        n.setPage(param.getPage());
        n.setPage_size(param.getPageSize());
        if (param.getAlgorithmSources() != null && !param.getAlgorithmSources().isEmpty()) {
            com.dji.flighthubsdk.generated.IntVector v = new com.dji.flighthubsdk.generated.IntVector();
            for (Integer s : param.getAlgorithmSources()) { v.add(s); }
            n.setAlgorithm_sources(v);
        }
        return n;
    }

    public static GetAiAlertFlightsResult toGetAiAlertFlightsResult(
            com.dji.flighthubsdk.generated.GetAiAlertFlightsResult n) {
        GetAiAlertFlightsResult b = new GetAiAlertFlightsResult();
        if (n == null) { return b; }
        b.setTotal(n.getTotal());
        b.setPage(n.getPage());
        b.setPageSize(n.getPage_size());
        b.setPageCount(n.getPage_count());
        com.dji.flighthubsdk.generated.AiAlertFlightItemVector vec = n.getData();
        if (vec != null) {
            List<AiAlertFlightItem> list = new ArrayList<>();
            for (int i = 0; i < vec.size(); i++) { list.add(toAiAlertFlightItem(vec.get(i))); }
            b.setData(list);
        }
        return b;
    }

    public static com.dji.flighthubsdk.generated.GetAiAlertRecordsParam toNativeGetAiAlertRecordsParam(
            GetAiAlertRecordsParam param) {
        com.dji.flighthubsdk.generated.GetAiAlertRecordsParam n = new com.dji.flighthubsdk.generated.GetAiAlertRecordsParam();
        if (param == null) { return n; }
        n.setBegin_at(param.getBeginAt());
        n.setEnd_at(param.getEndAt());
        n.setPage(param.getPage());
        n.setPage_size(param.getPageSize());
        if (param.getFlightId() != null && !param.getFlightId().isEmpty()) {
            com.dji.flighthubsdk.generated.StringVector v = new com.dji.flighthubsdk.generated.StringVector();
            for (String s : param.getFlightId()) { v.add(s); }
            n.setFlight_id(v);
        }
        if (param.getDroneSn() != null && !param.getDroneSn().isEmpty()) {
            com.dji.flighthubsdk.generated.StringVector v = new com.dji.flighthubsdk.generated.StringVector();
            for (String s : param.getDroneSn()) { v.add(s); }
            n.setDrone_sn(v);
        }
        if (param.getTargetType() != null && !param.getTargetType().isEmpty()) {
            com.dji.flighthubsdk.generated.IntVector v = new com.dji.flighthubsdk.generated.IntVector();
            for (Integer t : param.getTargetType()) { v.add(t); }
            n.setTarget_type(v);
        }
        if (param.getAlgorithmSources() != null && !param.getAlgorithmSources().isEmpty()) {
            com.dji.flighthubsdk.generated.IntVector v = new com.dji.flighthubsdk.generated.IntVector();
            for (Integer s : param.getAlgorithmSources()) { v.add(s); }
            n.setAlgorithm_sources(v);
        }
        return n;
    }

    public static GetAiAlertRecordsResult toGetAiAlertRecordsResult(
            com.dji.flighthubsdk.generated.GetAiAlertRecordsResult n) {
        GetAiAlertRecordsResult b = new GetAiAlertRecordsResult();
        if (n == null) { return b; }
        b.setTotal(n.getTotal());
        b.setPage(n.getPage());
        b.setPageSize(n.getPage_size());
        b.setPageCount(n.getPage_count());
        com.dji.flighthubsdk.generated.AiAlertRecordItemVectorMap nMap = n.getData();
        if (nMap != null) {
            Map<String, List<AiAlertRecordItem>> bMap = new HashMap<>();
            for (Map.Entry<String, com.dji.flighthubsdk.generated.AiAlertRecordItemVector> entry : nMap.entrySet()) {
                com.dji.flighthubsdk.generated.AiAlertRecordItemVector vec = entry.getValue();
                List<AiAlertRecordItem> list = new ArrayList<>();
                if (vec != null) {
                    for (int j = 0; j < vec.size(); j++) { list.add(toAiAlertRecordItem(vec.get(j))); }
                }
                bMap.put(entry.getKey(), list);
            }
            b.setData(bMap);
        }
        return b;
    }

    // ===== Live Sync 转换方法 =====

    private static LiveSyncSchemaOption toLiveSyncSchemaOption(
            com.dji.flighthubsdk.generated.LiveSyncSchemaOption n) {
        LiveSyncSchemaOption b = new LiveSyncSchemaOption();
        if (n == null) return b;
        b.setServerId(n.getServer_id());
        b.setServerIp(n.getServer_ip());
        b.setServerPort(n.getServer_port());
        b.setDevicePassword(n.getDevice_password());
        b.setLocalPort(n.getLocal_port());
        b.setDeviceId(n.getDevice_id());
        b.setLocalChannel(n.getLocal_channel());
        b.setUrl(n.getUrl());
        b.setProvider(n.getProvider());
        b.setVolcUserId(n.getVolc_user_id());
        b.setRtspUrl(n.getRtsp_url());
        b.setUsername(n.getUsername());
        b.setPassword(n.getPassword());
        b.setEnableTs(n.getEnable_ts());
        return b;
    }

    private static com.dji.flighthubsdk.generated.LiveSyncSchemaOption toNativeLiveSyncSchemaOption(
            LiveSyncSchemaOption b) {
        com.dji.flighthubsdk.generated.LiveSyncSchemaOption n =
                new com.dji.flighthubsdk.generated.LiveSyncSchemaOption();
        if (b == null) return n;
        n.setServer_id(b.getServerId());
        n.setServer_ip(b.getServerIp());
        n.setServer_port(b.getServerPort());
        n.setDevice_password(b.getDevicePassword());
        n.setLocal_port(b.getLocalPort());
        n.setDevice_id(b.getDeviceId());
        n.setLocal_channel(b.getLocalChannel());
        n.setUrl(b.getUrl());
        n.setProvider(b.getProvider());
        n.setVolc_user_id(b.getVolcUserId());
        n.setRtsp_url(b.getRtspUrl());
        n.setUsername(b.getUsername());
        n.setPassword(b.getPassword());
        n.setEnable_ts(b.isEnableTs());
        return n;
    }

    public static LiveSyncItem toLiveSyncItem(com.dji.flighthubsdk.generated.LiveSyncItem n) {
        LiveSyncItem b = new LiveSyncItem();
        if (n == null) return b;
        b.setUuid(n.getUuid());
        b.setStatus(n.getStatus());
        b.setOrgUuid(n.getOrg_uuid());
        b.setSn(n.getSn());
        b.setCameraIndex(n.getCamera_index());
        b.setName(n.getName());
        b.setSchema(n.getSchema());
        b.setSchemaOption(toLiveSyncSchemaOption(n.getSchema_option()));
        b.setDeviceName(n.getDevice_name());
        b.setDeviceOnline(n.getDevice_online());
        b.setDeviceModelKey(n.getDevice_model_key());
        b.setAirport(n.getIs_airport());
        b.setVideoType(n.getVideo_type());
        b.setVideoQuality(n.getVideo_quality());
        b.setRegion(n.getRegion());
        b.setEnable(n.getEnable());
        b.setErrMsg(n.getErr_msg());
        b.setDeviceType(n.getDevice_type());
        return b;
    }

    public static com.dji.flighthubsdk.generated.LiveSyncCreateParams toNativeLiveSyncCreateParams(
            LiveSyncCreateParams b) {
        com.dji.flighthubsdk.generated.LiveSyncCreateParams n =
                new com.dji.flighthubsdk.generated.LiveSyncCreateParams();
        if (b == null) return n;
        n.setConverter_name(b.getConverterName());
        n.setSn(b.getSn());
        n.setCamera_index(b.getCameraIndex());
        n.setVideo_type(b.getVideoType());
        n.setVideo_quality(b.getVideoQuality());
        n.setSchema(b.getSchema());
        n.setSchema_option(toNativeLiveSyncSchemaOption(b.getSchemaOption()));
        return n;
    }

    public static LiveSyncCreateResult toLiveSyncCreateResult(
            com.dji.flighthubsdk.generated.LiveSyncCreateResult n) {
        LiveSyncCreateResult b = new LiveSyncCreateResult();
        if (n == null) return b;
        b.setItem(toLiveSyncItem(n.getItem()));
        b.setLimit(n.getLimit());
        b.setHasLimit(n.getHas_limit());
        return b;
    }

    public static com.dji.flighthubsdk.generated.LiveSyncUpdateParams toNativeLiveSyncUpdateParams(
            LiveSyncUpdateParams b) {
        com.dji.flighthubsdk.generated.LiveSyncUpdateParams n =
                new com.dji.flighthubsdk.generated.LiveSyncUpdateParams();
        if (b == null) return n;
        n.setId(b.getId());
        n.setSet_enable(b.isSetEnable());
        n.setEnable(b.isEnable());
        n.setSet_schema(b.isSetSchema());
        n.setSchema(b.getSchema());
        n.setSet_schema_option(b.isSetSchemaOption());
        n.setSchema_option(toNativeLiveSyncSchemaOption(b.getSchemaOption()));
        n.setSet_converter_name(b.isSetConverterName());
        n.setConverter_name(b.getConverterName());
        n.setSet_sn(b.isSetSn());
        n.setSn(b.getSn());
        n.setSet_camera_index(b.isSetCameraIndex());
        n.setCamera_index(b.getCameraIndex());
        n.setSet_video_type(b.isSetVideoType());
        n.setVideo_type(b.getVideoType());
        n.setSet_video_quality(b.isSetVideoQuality());
        n.setVideo_quality(b.getVideoQuality());
        return n;
    }

    public static com.dji.flighthubsdk.generated.LiveSyncListParams toNativeLiveSyncListParams(
            LiveSyncListParams b) {
        com.dji.flighthubsdk.generated.LiveSyncListParams n =
                new com.dji.flighthubsdk.generated.LiveSyncListParams();
        if (b == null) return n;
        n.setPage(b.getPage());
        n.setSize(b.getSize());
        n.setSet_sn(b.isSetSn());
        n.setSn(b.getSn());
        n.setSet_schema(b.isSetSchema());
        n.setSchema(b.getSchema());
        n.setSet_enable(b.isSetEnable());
        n.setEnable(b.isEnable());
        return n;
    }

    public static LiveSyncListResult toLiveSyncListResult(
            com.dji.flighthubsdk.generated.LiveSyncListResult n) {
        LiveSyncListResult b = new LiveSyncListResult();
        if (n == null) return b;
        b.setPage(n.getPage());
        b.setSize(n.getSize());
        b.setTotal(n.getTotal());
        List<LiveSyncItem> list = new ArrayList<>();
        com.dji.flighthubsdk.generated.LiveSyncItemVector v = n.getList();
        if (v != null) {
            for (int i = 0; i < v.size(); i++) { list.add(toLiveSyncItem(v.get(i))); }
        }
        b.setList(list);
        return b;
    }

    private static LiveSyncCameraCapacity toLiveSyncCameraCapacity(
            com.dji.flighthubsdk.generated.LiveSyncCameraCapacity n) {
        LiveSyncCameraCapacity b = new LiveSyncCameraCapacity();
        if (n == null) return b;
        b.setCameraIndex(n.getCamera_index());
        List<LiveSyncVideoCapacity> vlist = new ArrayList<>();
        com.dji.flighthubsdk.generated.LiveSyncVideoCapacityVector vv = n.getVideos_list();
        if (vv != null) {
            for (int i = 0; i < vv.size(); i++) {
                LiveSyncVideoCapacity vc = new LiveSyncVideoCapacity();
                vc.setVideoType(vv.get(i).getVideo_type());
                vc.setVideoIndex(vv.get(i).getVideo_index());
                vlist.add(vc);
            }
        }
        b.setVideosList(vlist);
        return b;
    }

    private static LiveSyncDeviceV2 toLiveSyncDeviceV2(
            com.dji.flighthubsdk.generated.LiveSyncDeviceV2 n) {
        LiveSyncDeviceV2 b = new LiveSyncDeviceV2();
        if (n == null) return b;
        b.setSn(n.getSn());
        b.setName(n.getName());
        b.setDeviceModelKey(n.getDevice_model_key());
        b.setDeviceType(n.getDevice_type());
        List<LiveSyncCameraCapacity> caps = new ArrayList<>();
        com.dji.flighthubsdk.generated.LiveSyncCameraCapacityVector cv = n.getCamera_capacity();
        if (cv != null) {
            for (int i = 0; i < cv.size(); i++) { caps.add(toLiveSyncCameraCapacity(cv.get(i))); }
        }
        b.setCameraCapacity(caps);
        return b;
    }

    public static LiveSyncDevicesV2Result toLiveSyncDevicesV2Result(
            com.dji.flighthubsdk.generated.LiveSyncDevicesV2Result n) {
        LiveSyncDevicesV2Result b = new LiveSyncDevicesV2Result();
        if (n == null) return b;
        b.setTotal(n.getTotal());
        List<LiveSyncDeviceItemV2> items = new ArrayList<>();
        com.dji.flighthubsdk.generated.LiveSyncDeviceItemV2Vector iv = n.getList();
        if (iv != null) {
            for (int i = 0; i < iv.size(); i++) {
                LiveSyncDeviceItemV2 di = new LiveSyncDeviceItemV2();
                di.setGateway(toLiveSyncDeviceV2(iv.get(i).getGateway()));
                List<LiveSyncDeviceV2> hosts = new ArrayList<>();
                com.dji.flighthubsdk.generated.LiveSyncDeviceV2Vector hv = iv.get(i).getHosts();
                if (hv != null) {
                    for (int j = 0; j < hv.size(); j++) { hosts.add(toLiveSyncDeviceV2(hv.get(j))); }
                }
                di.setHosts(hosts);
                items.add(di);
            }
        }
        b.setList(items);
        return b;
    }

    public static LiveSyncSummaryResult toLiveSyncSummaryResult(
            com.dji.flighthubsdk.generated.LiveSyncSummaryResult n) {
        LiveSyncSummaryResult b = new LiveSyncSummaryResult();
        if (n == null) return b;
        b.setTotal(n.getTotal());
        b.setEnableCnt(n.getEnable_cnt());
        return b;
    }
    // ========== Terra 模型管理新增转换方法 ==========

    /** 模型上传回调响应转换 */
    public static com.dji.flighthubsdk.bean.TerraUploadCallbackResult toTerraUploadCallbackResult(
            com.dji.flighthubsdk.generated.TerraUploadCallbackResult n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraUploadCallbackResult();
        return new com.dji.flighthubsdk.bean.TerraUploadCallbackResult(
                n.getId(), safeString(n.getUuid()), safeString(n.getName()));
    }

    /** 批量加载/取消加载响应转换 */
    public static com.dji.flighthubsdk.bean.TerraShowOnMapResult toTerraShowOnMapResult(
            com.dji.flighthubsdk.generated.TerraShowOnMapResult n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraShowOnMapResult();
        com.dji.flighthubsdk.generated.IntVector iv = n.getIds();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        if (iv != null) for (int i = 0; i < iv.size(); i++) ids.add(iv.get(i));
        return new com.dji.flighthubsdk.bean.TerraShowOnMapResult(ids);
    }

    /** 解压进度 WS 消息转换 */
    public static com.dji.flighthubsdk.bean.TerraUnzipProgressMsg toTerraUnzipProgressMsg(
            com.dji.flighthubsdk.generated.TerraUnzipProgressMsg n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraUnzipProgressMsg();
        return new com.dji.flighthubsdk.bean.TerraUnzipProgressMsg(
                n.getFile_id(), safeString(n.getUuid()), safeString(n.getName()), n.getPercent());
    }

    /** 模型状态变更 WS 消息转换 */
    public static com.dji.flighthubsdk.bean.TerraModelStatusChangedMsg toTerraModelStatusChangedMsg(
            com.dji.flighthubsdk.generated.TerraModelStatusChangedMsg n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraModelStatusChangedMsg();
        return new com.dji.flighthubsdk.bean.TerraModelStatusChangedMsg(
                n.getFile_id(), safeString(n.getUuid()), safeString(n.getName()),
                n.getState(), n.getFile_type(), n.getSub_file_type(), n.getError_code());
    }

    /** 文件展示地图 WS 消息转换 */
    public static com.dji.flighthubsdk.bean.TerraFileShowOnMapMsg toTerraFileShowOnMapMsg(
            com.dji.flighthubsdk.generated.TerraFileShowOnMapMsg n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraFileShowOnMapMsg();
        com.dji.flighthubsdk.generated.TerraFileShowOnMapItemVector iv = n.getList();
        java.util.List<com.dji.flighthubsdk.bean.TerraFileShowOnMapItem> list = new java.util.ArrayList<>();
        if (iv != null) {
            for (int i = 0; i < iv.size(); i++) {
                com.dji.flighthubsdk.generated.TerraFileShowOnMapItem item = iv.get(i);
                list.add(new com.dji.flighthubsdk.bean.TerraFileShowOnMapItem(
                        item.getId(), safeString(item.getName()),
                        item.getFile_type(), item.getSub_file_type(), safeString(item.getPreview_url())));
            }
        }
        return new com.dji.flighthubsdk.bean.TerraFileShowOnMapMsg(list);
    }

    /** TerraMediaFileHideOnMapMsg 转换 */
    public static com.dji.flighthubsdk.bean.TerraMediaFileHideOnMapMsg toTerraMediaFileHideOnMapMsg(
            com.dji.flighthubsdk.generated.TerraMediaFileHideOnMapMsg n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraMediaFileHideOnMapMsg();
        com.dji.flighthubsdk.generated.IntVector iv = n.getIds();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        if (iv != null) for (int i = 0; i < iv.size(); i++) ids.add(iv.get(i));
        return new com.dji.flighthubsdk.bean.TerraMediaFileHideOnMapMsg(ids);
    }

    /** TerraMediaFileShowOnMapMsg 转换 */
    public static com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapMsg toTerraMediaFileShowOnMapMsg(
            com.dji.flighthubsdk.generated.TerraMediaFileShowOnMapMsg n) {
        if (n == null) return new com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapMsg();
        com.dji.flighthubsdk.generated.TerraMediaFileShowOnMapItemVector iv = n.getList();
        java.util.List<com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapItem> list = new java.util.ArrayList<>();
        if (iv != null) {
            for (int i = 0; i < iv.size(); i++) {
                com.dji.flighthubsdk.generated.TerraMediaFileShowOnMapItem item = iv.get(i);
                com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapItem bean =
                        new com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapItem();
                bean.setName(safeString(item.getName()));
                bean.setId(item.getId());
                bean.setShootLat(item.getShoot_lat());
                bean.setShootLng(item.getShoot_lng());
                bean.setShootAbsAlt(item.getShoot_abs_alt());
                bean.setShootRelAlt(item.getShoot_rel_alt());
                bean.setPhotoLat(item.getPhoto_lat());
                bean.setPhotoLng(item.getPhoto_lng());
                bean.setPhotoAbsAlt(item.getPhoto_abs_alt());
                bean.setPhotoRelAlt(item.getPhoto_rel_alt());
                bean.setPreviewUrl(safeString(item.getPreview_url()));
                bean.setFileType(item.getFile_type());
                bean.setSubFileType(item.getSub_file_type());
                list.add(bean);
            }
        }
        return new com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapMsg(list);
    }

    /** TerraUploadCallbackParams → native TerraUploadCallbackJavaParams */
    public static com.dji.flighthubsdk.generated.TerraUploadCallbackJavaParams toNativeTerraUploadCallbackParams(
            com.dji.flighthubsdk.bean.TerraUploadCallbackParams params) {
        com.dji.flighthubsdk.generated.TerraUploadCallbackJavaParams n =
                new com.dji.flighthubsdk.generated.TerraUploadCallbackJavaParams();
        if (params == null) return n;
        n.setObject_key(params.getObjectKey() != null ? params.getObjectKey() : "");
        n.setName(params.getName() != null ? params.getName() : "");
        if (params.isParentIdSet()) {
            n.setParent_id(params.getParentId());
            n.setParent_id_set(true);
        }
        if (params.getDriveUuid() != null) {
            n.setDrive_uuid(params.getDriveUuid());
            n.setDrive_uuid_set(true);
        }
        if (params.getShowOnMap() != null) {
            n.setShow_on_map(params.getShowOnMap());
            n.setShow_on_map_set(true);
        }
        return n;
    }

    /** TerraRenameModelParams → native TerraRenameModelJavaParams */
    public static com.dji.flighthubsdk.generated.TerraRenameModelJavaParams toNativeTerraRenameModelParams(
            com.dji.flighthubsdk.bean.TerraRenameModelParams params) {
        com.dji.flighthubsdk.generated.TerraRenameModelJavaParams n =
                new com.dji.flighthubsdk.generated.TerraRenameModelJavaParams();
        if (params == null) return n;
        n.setName(params.getName() != null ? params.getName() : "");
        if (params.getInUniformHeight() != null) {
            n.setIn_uniform_height(params.getInUniformHeight());
            n.setIn_uniform_height_set(true);
        }
        if (params.getModelDisplayMode() != null) {
            n.setModel_display_mode(params.getModelDisplayMode());
            n.setModel_display_mode_set(true);
        }
        if (params.isCollectedAtSet()) {
            n.setCollected_at(params.getCollectedAt());
            n.setCollected_at_set(true);
        }
        return n;
    }

    /** TerraShowOnMapParams → native TerraShowOnMapJavaParams */
    public static com.dji.flighthubsdk.generated.TerraShowOnMapJavaParams toNativeTerraShowOnMapParams(
            com.dji.flighthubsdk.bean.TerraShowOnMapParams params) {
        com.dji.flighthubsdk.generated.TerraShowOnMapJavaParams n =
                new com.dji.flighthubsdk.generated.TerraShowOnMapJavaParams();
        if (params == null) return n;
        com.dji.flighthubsdk.generated.IntVector ids = new com.dji.flighthubsdk.generated.IntVector();
        if (params.getIds() != null) for (int id : params.getIds()) ids.add(id);
        n.setIds(ids);
        if (params.getShowOnMap() != null) {
            n.setShow_on_map(params.getShowOnMap());
            n.setShow_on_map_set(true);
        }
        if (params.getIsDistributed() != null) {
            n.setIs_distributed(params.getIsDistributed());
            n.setIs_distributed_set(true);
        }
        if (params.getSelectFilter() != null) {
            com.dji.flighthubsdk.generated.TerraSelectFilterJavaParams sf =
                    new com.dji.flighthubsdk.generated.TerraSelectFilterJavaParams();
            com.dji.flighthubsdk.bean.TerraSelectFilterParams fp = params.getSelectFilter();
            if (fp.isSelectedParentIdSet()) {
                sf.setSelected_parent_id(fp.getSelectedParentId());
                sf.setSelected_parent_id_set(true);
            }
            com.dji.flighthubsdk.generated.IntVector excIds = new com.dji.flighthubsdk.generated.IntVector();
            if (fp.getExceptIds() != null) for (int id : fp.getExceptIds()) excIds.add(id);
            sf.setExcept_ids(excIds);
            sf.setKey(fp.getKey() != null ? fp.getKey() : "");
            n.setSelect_filter(sf);
            n.setSelect_filter_set(true);
        }
        return n;
    }

    // ========== GetModelDetail / SetLasWkt 转换方法 ==========

    /** 获取模型文件详情响应转换 */
    public static com.dji.flighthubsdk.bean.TerraModelDetailResult toTerraModelDetailResult(
            com.dji.flighthubsdk.generated.TerraModelDetailJavaResult n) {
        com.dji.flighthubsdk.bean.TerraModelDetailResult r = new com.dji.flighthubsdk.bean.TerraModelDetailResult();
        if (n == null) return r;
        r.setId(n.getId());                       r.setUuid(safeString(n.getUuid()));
        r.setName(safeString(n.getName()));        r.setFileType(n.getFile_type());
        r.setSubFileType(n.getSub_file_type());    r.setSize(n.getSize());
        r.setSuffix(safeString(n.getSuffix()));    r.setMod(n.getMod());
        r.setShowOnMap(n.getShow_on_map());         r.setState(n.getState());
        r.setSelectedWkt(safeString(n.getSelected_wkt())); r.setParentId(n.getParent_id());
        r.setUpdateAt(n.getUpdate_at());           r.setCreateAt(n.getCreate_at());
        r.setUrl(safeString(n.getUrl()));          r.setWebpUrl(safeString(n.getWebp_url()));
        r.setPreviewUrl(safeString(n.getPreview_url())); r.setThumbnailUrl(safeString(n.getThumbnail_url()));
        r.setObjectKey(safeString(n.getObject_key())); r.setIndexFileKey(safeString(n.getIndex_file_key()));
        r.setFilePrefix(safeString(n.getFile_prefix())); r.setWebpFilePrefix(safeString(n.getWebp_file_prefix()));
        // 边界字段（直接赋值；字段缺失时服务端返回 0.0，无法区分）
        r.setMaxLat(n.getMax_lat());     r.setMinLat(n.getMin_lat());
        r.setMaxLng(n.getMax_lng());     r.setMinLng(n.getMin_lng());
        r.setMaxAlt(n.getMax_alt());     r.setMinAlt(n.getMin_alt());
        r.setLatitude(n.getLatitude());  r.setLongitude(n.getLongitude());
        r.setAltitude(n.getAltitude());
        r.setHalfX(n.getHalf_x());       r.setHalfY(n.getHalf_y());
        r.setHalfZ(n.getHalf_z());
        r.setMaxLevel(n.getMax_level()); r.setMinLevel(n.getMin_level());
        r.setStartLat(n.getStart_lat()); r.setEndLat(n.getEnd_lat());
        r.setStartLng(n.getStart_lng()); r.setEndLng(n.getEnd_lng());
        // transform 矩阵
        com.dji.flighthubsdk.generated.DoubleVector tv = n.getTransform();
        if (tv != null) {
            java.util.List<Double> tf = new java.util.ArrayList<>();
            for (int i = 0; i < tv.size(); i++) tf.add(tv.get(i));
            r.setTransform(tf);
        }
        // 强度
        r.setMaxIntensity(n.getMax_intensity());
        r.setMinIntensity(n.getMin_intensity());
        // max_return_number
        r.setMaxReturnNumber(n.getMax_return_number());
        // 地理边界多边形
        com.dji.flighthubsdk.generated.TerraModelGeo2DPointVector pv = n.getBounding_points();
        if (pv != null) {
            java.util.List<com.dji.flighthubsdk.bean.TerraGeo2DPoint> pts = new java.util.ArrayList<>();
            for (int i = 0; i < pv.size(); i++) {
                com.dji.flighthubsdk.generated.TerraModelGeo2DPoint p = pv.get(i);
                pts.add(new com.dji.flighthubsdk.bean.TerraGeo2DPoint(p.getLng(), p.getLat()));
            }
            r.setBoundingPoints(pts);
        }
        // 媒体扩展
        r.setDevice(safeString(n.getDevice()));            r.setDuration(n.getDuration());
        r.setPhotographer(safeString(n.getPhotographer())); r.setResolution(safeString(n.getResolution()));
        r.setExtHeight(n.getExt_height());                 r.setExtWidth(n.getExt_width());
        r.setZoomFactor(n.getZoom_factor());               r.setTaskName(safeString(n.getTask_name()));
        r.setShootLng(n.getShoot_lng());   r.setShootLat(n.getShoot_lat());
        r.setShootAbsAlt(n.getShoot_abs_alt()); r.setShootRelAlt(n.getShoot_rel_alt());
        r.setPhotoLng(n.getPhoto_lng());   r.setPhotoLat(n.getPhoto_lat());
        r.setPhotoAbsAlt(n.getPhoto_abs_alt()); r.setPhotoRelAlt(n.getPhoto_rel_alt());
        r.setLineName(safeString(n.getLine_name()));   r.setOriginal(n.getIs_original());
        r.setGimbalPitch(n.getGimbal_pitch());         r.setGimbalYaw(n.getGimbal_yaw());
        r.setGimbalYawDegree(n.getGimbal_yaw_degree()); r.setGimbalRoll(n.getGimbal_roll());
        r.setFocalLength(n.getFocal_length());
        r.setPayloadModelKey(safeString(n.getPayload_model_key()));
        r.setDroneModelKey(safeString(n.getDrone_model_key()));
        r.setCreateTime(safeString(n.getCreate_time()));
        r.setWaylineActionUuid(safeString(n.getWayline_action_uuid()));
        r.setWaylineUuid(safeString(n.getWayline_uuid()));
        r.setOriginWaylineUuid(safeString(n.getOrigin_wayline_uuid()));
        r.setDigitalZoomRatio(n.getDigital_zoom_ratio());
        r.setLengthIn35mmFilm(n.getLength_in35mm_film());
        // 标签
        com.dji.flighthubsdk.generated.StringVector ftv = n.getFile_tags();
        if (ftv != null) {
            java.util.List<String> tags = new java.util.ArrayList<>();
            for (int i = 0; i < ftv.size(); i++) tags.add(safeString(ftv.get(i)));
            r.setFileTags(tags);
        }
        r.setUserName(safeString(n.getUser_name()));
        return r;
    }

    /** TerraSetLasWktParams → native TerraSetLasWktJavaParams */
    public static com.dji.flighthubsdk.generated.TerraSetLasWktJavaParams toNativeTerraSetLasWktParams(
            com.dji.flighthubsdk.bean.TerraSetLasWktParams params) {
        com.dji.flighthubsdk.generated.TerraSetLasWktJavaParams n =
                new com.dji.flighthubsdk.generated.TerraSetLasWktJavaParams();
        if (params == null) return n;
        n.setFile_uuid(params.getFileUuid() != null ? params.getFileUuid() : "");
        n.setWkt(params.getWkt() != null ? params.getWkt() : "");
        return n;
    }
    
    // ========== ElementModule 转换方法 ==========

    public static com.dji.flighthubsdk.bean.ElementCreateResult toElementCreateResult(
            com.dji.flighthubsdk.generated.CreateElementResult n) {
        com.dji.flighthubsdk.bean.ElementCreateResult r = new com.dji.flighthubsdk.bean.ElementCreateResult();
        if (n != null) r.setId(safeString(n.getId()));
        return r;
    }

    public static com.dji.flighthubsdk.bean.ElementUpdateResult toElementUpdateResult(
            com.dji.flighthubsdk.generated.UpdateElementResult n) {
        com.dji.flighthubsdk.bean.ElementUpdateResult r = new com.dji.flighthubsdk.bean.ElementUpdateResult();
        if (n != null) r.setId(safeString(n.getId()));
        return r;
    }

    public static com.dji.flighthubsdk.bean.ElementListHierarchyResult toElementListHierarchyResult(
            com.dji.flighthubsdk.generated.ListElementHierarchyResult n) {
        com.dji.flighthubsdk.bean.ElementListHierarchyResult r = new com.dji.flighthubsdk.bean.ElementListHierarchyResult();
        if (n == null) return r;
        com.dji.flighthubsdk.generated.ElementGroupInfoVector v = n.getList();
        if (v != null) {
            java.util.List<com.dji.flighthubsdk.bean.ElementGroupOutput> list = new java.util.ArrayList<>();
            for (int i = 0; i < v.size(); i++) list.add(toElementGroupOutput(v.get(i)));
            r.setList(list);
        }
        return r;
    }

    private static com.dji.flighthubsdk.bean.ElementGroupOutput toElementGroupOutput(
            com.dji.flighthubsdk.generated.ElementGroupInfo n) {
        com.dji.flighthubsdk.bean.ElementGroupOutput g = new com.dji.flighthubsdk.bean.ElementGroupOutput();
        if (n == null) return g;
        g.setId(safeString(n.getId()));
        g.setName(safeString(n.getName()));
        g.setOrder(n.getOrder());
        g.setDistributed(n.getIs_distributed());
        g.setType(n.getType());
        g.setLock(n.getIs_lock());
        g.setTriState(safeString(n.getTri_state()));
        g.setCreateTime(n.getCreate_time());
        g.setParentId(safeString(n.getPid()));
        // elements
        com.dji.flighthubsdk.generated.ElementItemInfoVector ev = n.getElements();
        if (ev != null) {
            java.util.List<com.dji.flighthubsdk.bean.ElementItem> items = new java.util.ArrayList<>();
            for (int i = 0; i < ev.size(); i++) items.add(toElementItem(ev.get(i)));
            g.setElements(items);
        }
        // children（递归）
        com.dji.flighthubsdk.generated.ElementGroupInfoVector cv = n.getChildren();
        if (cv != null) {
            java.util.List<com.dji.flighthubsdk.bean.ElementGroupOutput> children = new java.util.ArrayList<>();
            for (int i = 0; i < cv.size(); i++) children.add(toElementGroupOutput(cv.get(i)));
            g.setChildren(children);
        }
        return g;
    }

    private static com.dji.flighthubsdk.bean.ElementItem toElementItem(
            com.dji.flighthubsdk.generated.ElementItemInfo n) {
        com.dji.flighthubsdk.bean.ElementItem item = new com.dji.flighthubsdk.bean.ElementItem();
        if (n == null) return item;
        item.setId(safeString(n.getId()));
        item.setName(safeString(n.getName()));
        item.setDesc(safeString(n.getDesc()));
        item.setSource(n.getSource());
        item.setOrder(n.getOrder());
        item.setStatus(n.getStatus());
        item.setDisplay(n.getDisplay());
        item.setResource(toElementResourceData(n.getResource()));
        item.setUpdateTime(n.getUpdate_time());
        item.setCreateTime(n.getCreate_time());
        item.setElevationLoadStatus(n.getElevation_load_status());
        item.setNotify(n.getNotify());
        return item;
    }

    private static com.dji.flighthubsdk.bean.ElementResourceData toElementResourceData(
            com.dji.flighthubsdk.generated.ElementResourceData n) {
        com.dji.flighthubsdk.bean.ElementResourceData d = new com.dji.flighthubsdk.bean.ElementResourceData();
        if (n == null) return d;
        d.setType(n.getType());
        d.setUserName(safeString(n.getUser_name()));
        d.setUserId(safeString(n.getUser_id()));
        d.setRemark(safeString(n.getRemark()));
        // content
        com.dji.flighthubsdk.generated.ElementResourceContent nc = n.getContent();
        if (nc != null) {
            com.dji.flighthubsdk.bean.ElementResourceContent content = new com.dji.flighthubsdk.bean.ElementResourceContent();
            content.setType(safeString(nc.getType()));
            // properties
            com.dji.flighthubsdk.generated.ElementProperties np = nc.getProperties();
            if (np != null) {
                com.dji.flighthubsdk.bean.ElementProperties props = new com.dji.flighthubsdk.bean.ElementProperties();
                props.setColor(safeString(np.getColor()));
                props.setClampToGround(np.getClamp_to_ground());
                props.setIs3d(np.getIs3d());
                props.setTransparent(np.getIs_transparent());
                props.setFontSize(np.getFont_size());
                props.setIcon(np.getIcon());
                content.setProperties(props);
            }
            // geometry
            com.dji.flighthubsdk.generated.ElementGeometry ng = nc.getGeometry();
            if (ng != null) {
                com.dji.flighthubsdk.bean.ElementGeometry geom = new com.dji.flighthubsdk.bean.ElementGeometry();
                geom.setType(safeString(ng.getType()));
                geom.setRadius(ng.getRadius());
                geom.setCoordinates(safeString(ng.getCoordinates()));
                geom.setBreadCrumbs(safeString(ng.getBread_crumbs()));
                content.setGeometry(geom);
            }
            // media_resources
            com.dji.flighthubsdk.generated.ElementMediaResources nmr = nc.getMedia_resources();
            if (nmr != null) {
                com.dji.flighthubsdk.generated.ElementMediaImageVector imgs = nmr.getImages();
                if (imgs != null && imgs.size() > 0) {
                    com.dji.flighthubsdk.bean.ElementMediaResources mr = new com.dji.flighthubsdk.bean.ElementMediaResources();
                    java.util.List<com.dji.flighthubsdk.bean.ElementMediaImage> imgList = new java.util.ArrayList<>();
                    for (int i = 0; i < imgs.size(); i++) {
                        com.dji.flighthubsdk.generated.ElementMediaImage ni = imgs.get(i);
                        com.dji.flighthubsdk.bean.ElementMediaImage img = new com.dji.flighthubsdk.bean.ElementMediaImage();
                        img.setObjectKey(safeString(ni.getObject_key()));
                        img.setMediaFileId(ni.getMedia_file_id());
                        img.setMediaFileUuid(safeString(ni.getMedia_file_uuid()));
                        imgList.add(img);
                    }
                    mr.setImages(imgList);
                    content.setMediaResources(mr);
                }
            }
            d.setContent(content);
        }
        return d;
    }

    public static com.dji.flighthubsdk.bean.ElementSetVisibilityResult toElementSetVisibilityResult(
            com.dji.flighthubsdk.generated.SetElementVisibilityResult n) {
        com.dji.flighthubsdk.bean.ElementSetVisibilityResult r = new com.dji.flighthubsdk.bean.ElementSetVisibilityResult();
        if (n != null) r.setId(safeString(n.getId()));
        return r;
    }

    public static com.dji.flighthubsdk.bean.ElementDeleteResult toElementDeleteResult(
            com.dji.flighthubsdk.generated.DeleteElementResult n) {
        com.dji.flighthubsdk.bean.ElementDeleteResult r = new com.dji.flighthubsdk.bean.ElementDeleteResult();
        if (n != null) r.setId(safeString(n.getId()));
        return r;
    }

    public static com.dji.flighthubsdk.bean.ElementCreateMsg toElementCreateMsg(
            com.dji.flighthubsdk.generated.ElementCreateMsg n) {
        com.dji.flighthubsdk.bean.ElementCreateMsg r = new com.dji.flighthubsdk.bean.ElementCreateMsg();
        if (n == null) return r;
        r.setId(safeString(n.getId())); r.setGroupId(safeString(n.getGroup_id()));
        r.setName(safeString(n.getName())); r.setDisplay(n.getDisplay());
        return r;
    }

    public static com.dji.flighthubsdk.bean.ElementUpdateMsg toElementUpdateMsg(
            com.dji.flighthubsdk.generated.ElementUpdateMsg n) {
        com.dji.flighthubsdk.bean.ElementUpdateMsg r = new com.dji.flighthubsdk.bean.ElementUpdateMsg();
        if (n == null) return r;
        r.setId(safeString(n.getId())); r.setGroupId(safeString(n.getGroup_id()));
        r.setName(safeString(n.getName())); r.setDisplay(n.getDisplay());
        return r;
    }

    public static com.dji.flighthubsdk.bean.ElementDeleteMsg toElementDeleteMsg(
            com.dji.flighthubsdk.generated.ElementDeleteMsg n) {
        com.dji.flighthubsdk.bean.ElementDeleteMsg r = new com.dji.flighthubsdk.bean.ElementDeleteMsg();
        if (n == null) return r;
        r.setId(safeString(n.getId())); r.setGroupId(safeString(n.getGroup_id()));
        return r;
    }

    // ── Element toNative 转换方法 ──────────────────────────────────────────

    /** 将 ElementResourceContent bean 转为 SWIG native 对象（用于 CreateElementParams 直传） */
    private static com.dji.flighthubsdk.generated.ElementResourceContent toNativeElementResourceContent(
            com.dji.flighthubsdk.bean.ElementResourceContent c) {
        com.dji.flighthubsdk.generated.ElementResourceContent nc =
                new com.dji.flighthubsdk.generated.ElementResourceContent();
        if (c == null) return nc;
        nc.setType(c.getType() != null ? c.getType() : "Feature");
        if (c.getProperties() != null) {
            nc.getProperties().setColor(c.getProperties().getColor() != null ? c.getProperties().getColor() : "");
            nc.getProperties().setClamp_to_ground(c.getProperties().isClampToGround());
            nc.getProperties().setIs3d(c.getProperties().isIs3d());
            nc.getProperties().setIs_transparent(c.getProperties().isTransparent());
            nc.getProperties().setFont_size(c.getProperties().getFontSize());
            nc.getProperties().setIcon(c.getProperties().getIcon());
        }
        if (c.getGeometry() != null) {
            nc.getGeometry().setType(c.getGeometry().getType() != null ? c.getGeometry().getType() : "");
            nc.getGeometry().setRadius(c.getGeometry().getRadius());
            nc.getGeometry().setCoordinates(c.getGeometry().getCoordinates() != null ? c.getGeometry().getCoordinates() : "");
            nc.getGeometry().setBread_crumbs(c.getGeometry().getBreadCrumbs() != null ? c.getGeometry().getBreadCrumbs() : "");
        }
        if (c.getMediaResources() != null && c.getMediaResources().getImages() != null) {
            com.dji.flighthubsdk.generated.ElementMediaImageVector imgs =
                    new com.dji.flighthubsdk.generated.ElementMediaImageVector();
            for (com.dji.flighthubsdk.bean.ElementMediaImage img : c.getMediaResources().getImages()) {
                com.dji.flighthubsdk.generated.ElementMediaImage ni =
                        new com.dji.flighthubsdk.generated.ElementMediaImage();
                ni.setObject_key(img.getObjectKey() != null ? img.getObjectKey() : "");
                ni.setMedia_file_id(img.getMediaFileId());
                ni.setMedia_file_uuid(img.getMediaFileUuid() != null ? img.getMediaFileUuid() : "");
                imgs.add(ni);
            }
            nc.getMedia_resources().setImages(imgs);
        }
        return nc;
    }

    /** 将 ElementResourceContent bean 转为 JavaParams（用于 updateElement 的 JavaParams 中转） */
    private static com.dji.flighthubsdk.generated.ElementResourceContentJavaParams toNativeElementResourceContentJavaParams(
            com.dji.flighthubsdk.bean.ElementResourceContent c) {
        com.dji.flighthubsdk.generated.ElementResourceContentJavaParams nc =
                new com.dji.flighthubsdk.generated.ElementResourceContentJavaParams();
        if (c == null) return nc;
        nc.setType(c.getType() != null ? c.getType() : "Feature");
        if (c.getProperties() != null) {
            nc.getProperties().setColor(c.getProperties().getColor() != null ? c.getProperties().getColor() : "");
            nc.getProperties().setClamp_to_ground(c.getProperties().isClampToGround());
            nc.getProperties().setIs3d(c.getProperties().isIs3d());
            nc.getProperties().setIs_transparent(c.getProperties().isTransparent());
            nc.getProperties().setFont_size(c.getProperties().getFontSize());
            nc.getProperties().setIcon(c.getProperties().getIcon());
        }
        if (c.getGeometry() != null) {
            nc.getGeometry().setType(c.getGeometry().getType() != null ? c.getGeometry().getType() : "");
            nc.getGeometry().setRadius(c.getGeometry().getRadius());
            nc.getGeometry().setCoordinates(c.getGeometry().getCoordinates() != null ? c.getGeometry().getCoordinates() : "");
            nc.getGeometry().setBread_crumbs(c.getGeometry().getBreadCrumbs() != null ? c.getGeometry().getBreadCrumbs() : "");
        }
        if (c.getMediaResources() != null && c.getMediaResources().getImages() != null) {
            com.dji.flighthubsdk.generated.ElementMediaImageJavaParamsVector imgs =
                    new com.dji.flighthubsdk.generated.ElementMediaImageJavaParamsVector();
            for (com.dji.flighthubsdk.bean.ElementMediaImage img : c.getMediaResources().getImages()) {
                com.dji.flighthubsdk.generated.ElementMediaImageJavaParams ni =
                        new com.dji.flighthubsdk.generated.ElementMediaImageJavaParams();
                ni.setObject_key(img.getObjectKey() != null ? img.getObjectKey() : "");
                ni.setMedia_file_id(img.getMediaFileId());
                ni.setMedia_file_uuid(img.getMediaFileUuid() != null ? img.getMediaFileUuid() : "");
                imgs.add(ni);
            }
            nc.setImages(imgs);
        }
        return nc;
    }

    /** 创建要素参数 bean → native CreateElementParams（直传，无 JavaParams 中间层） */
    public static com.dji.flighthubsdk.generated.CreateElementParams toNativeCreateElementParams(
            com.dji.flighthubsdk.bean.ElementCreateParams params) {
        com.dji.flighthubsdk.generated.CreateElementParams n = new com.dji.flighthubsdk.generated.CreateElementParams();
        if (params == null) return n;
        n.setId(params.getId() != null ? params.getId() : "");
        n.setSource(params.getSource());
        if (params.getResourceContent() != null) {
            n.getResource().setContent(toNativeElementResourceContent(params.getResourceContent()));
        }
        n.getResource().setType(params.getResourceType());
        n.getResource().setRemark(params.getResourceRemark() != null ? params.getResourceRemark() : "");
        n.setName(params.getName() != null ? params.getName() : "");
        n.setElement_source(params.getElementSource());
        n.setDesc(params.getDesc() != null ? params.getDesc() : "");
        return n;
    }

    /** 更新要素参数 bean → native JavaParams（UpdateElementParams 含 std::optional，需 JavaParams 中转） */
    public static com.dji.flighthubsdk.generated.ElementUpdateJavaParams toNativeElementUpdateParams(
            com.dji.flighthubsdk.bean.ElementUpdateParams params) {
        com.dji.flighthubsdk.generated.ElementUpdateJavaParams n = new com.dji.flighthubsdk.generated.ElementUpdateJavaParams();
        if (params == null) return n;
        n.setId(params.getId() != null ? params.getId() : "");
        if (params.getName() != null) {
            n.setName(params.getName());
            n.setName_set(true);
        }
        if (params.getContent() != null) {
            n.setContent(toNativeElementResourceContentJavaParams(params.getContent()));
            n.setContent_set(true);
        }
        if (params.getDisplay() != null) {
            n.setDisplay(params.getDisplay());
            n.setDisplay_set(true);
        }
        if (params.getRemark() != null) {
            n.setRemark(params.getRemark());
            n.setRemark_set(true);
        }
        if (params.getStatus() != null) {
            n.setStatus(params.getStatus());
            n.setStatus_set(true);
        }
        if (params.getElevationLoadStatus() != null) {
            n.setElevation_load_status(params.getElevationLoadStatus());
            n.setElevation_load_status_set(true);
        }
        if (params.getUserStatus() != null) {
            n.setUser_status(params.getUserStatus());
            n.setUser_status_set(true);
        }
        n.setTarget_layer_id(params.getTargetLayerId() != null ? params.getTargetLayerId() : "");
        if (params.getOrders() != null && !params.getOrders().isEmpty()) {
            com.dji.flighthubsdk.generated.ElementResourceOrderItemVector nativeOrders =
                    new com.dji.flighthubsdk.generated.ElementResourceOrderItemVector();
            for (com.dji.flighthubsdk.bean.ElementResourceOrderItem item : params.getOrders()) {
                com.dji.flighthubsdk.generated.ElementResourceOrderItem ni =
                        new com.dji.flighthubsdk.generated.ElementResourceOrderItem();
                ni.setId(item.getId() != null ? item.getId() : "");
                ni.setOrder(item.getOrder());
                nativeOrders.add(ni);
            }
            n.setOrders(nativeOrders);
        }
        return n;
    }

    /** 查询元素层级列表参数 bean → native JavaParams（ListElementHierarchyParams 含 std::optional，需 JavaParams 中转） */
    public static com.dji.flighthubsdk.generated.ListElementHierarchyJavaParams toNativeListElementHierarchyParams(
            com.dji.flighthubsdk.bean.ElementListHierarchyParams params) {
        com.dji.flighthubsdk.generated.ListElementHierarchyJavaParams n = new com.dji.flighthubsdk.generated.ListElementHierarchyJavaParams();
        if (params == null) return n;
        n.setGroup_id(params.getGroupId() != null ? params.getGroupId() : "");
        n.setPid(params.getPid() != null ? params.getPid() : "");
        if (params.getIsDistributed() != null) {
            n.setIs_distributed(params.getIsDistributed());
            n.setIs_distributed_set(true);
        }
        if (params.getType() != null) {
            n.setType(params.getType());
            n.setType_set(true);
        }
        if (params.getElementUuid() != null) {
            n.setElement_uuid(params.getElementUuid());
            n.setElement_uuid_set(true);
        }
        n.setExclude_copy(params.isExcludeCopy());
        return n;
    }

    /** 设置要素显示状态参数 bean → native SetElementVisibilityParams（直传，无 std::optional） */
    public static com.dji.flighthubsdk.generated.SetElementVisibilityParams toNativeSetElementVisibilityParams(
            com.dji.flighthubsdk.bean.ElementSetVisibilityParams params) {
        com.dji.flighthubsdk.generated.SetElementVisibilityParams n = new com.dji.flighthubsdk.generated.SetElementVisibilityParams();
        if (params == null) return n;
        n.setId(params.getId() != null ? params.getId() : "");
        n.setDisplay(params.getDisplay());
        return n;
    }

    /** 删除要素参数 bean → native DeleteElementParams（直传，无 std::optional） */
    public static com.dji.flighthubsdk.generated.DeleteElementParams toNativeDeleteElementParams(
            com.dji.flighthubsdk.bean.ElementDeleteParams params) {
        com.dji.flighthubsdk.generated.DeleteElementParams n = new com.dji.flighthubsdk.generated.DeleteElementParams();
        if (params == null) return n;
        n.setId(params.getId() != null ? params.getId() : "");
        n.setFrom_batch_delete(params.isFromBatchDelete());
        return n;
    }

    // ========== 新增接口转换方法 ==========

    /** 上传地理标注媒体资源结果转换 */
    public static com.dji.flighthubsdk.bean.UploadGeoMediaFileResult toUploadGeoMediaFileResult(
            com.dji.flighthubsdk.generated.UploadGeoMediaFileResult n) {
        com.dji.flighthubsdk.bean.UploadGeoMediaFileResult r = new com.dji.flighthubsdk.bean.UploadGeoMediaFileResult();
        if (n == null) return r;
        r.setFileId(n.getFile_id());
        r.setFileUuid(safeString(n.getFile_uuid()));
        r.setMediaFileId(n.getMedia_file_id());
        return r;
    }

    /** 根据 ID 获取媒体文件详情结果转换 */
    public static com.dji.flighthubsdk.bean.GetMediaFileDetailByIdResult toGetMediaFileDetailByIdResult(
            com.dji.flighthubsdk.generated.GetMediaFileDetailByIdResult n) {
        com.dji.flighthubsdk.bean.GetMediaFileDetailByIdResult r = new com.dji.flighthubsdk.bean.GetMediaFileDetailByIdResult();
        if (n == null) return r;
        r.setUrl(safeString(n.getUrl()));
        r.setThumbnailUrl(safeString(n.getThumbnail_url()));
        r.setAlertImageUrl(safeString(n.getAlert_image_url()));
        r.setName(safeString(n.getName()));
        r.setSuffix(safeString(n.getSuffix()));
        r.setSize(n.getSize());
        r.setCreateAt(safeString(n.getCreate_at()));
        r.setExtWidth(n.getExt().getWidth());
        r.setExtHeight(n.getExt().getHeight());
        com.dji.flighthubsdk.bean.MediaFileGeoPosition pos = new com.dji.flighthubsdk.bean.MediaFileGeoPosition(
                n.getExt().getPhoto_position().getLat(),
                n.getExt().getPhoto_position().getLng(),
                n.getExt().getPhoto_position().getAbs_alt());
        r.setPhotoPosition(pos);
        return r;
    }

    /** 获取航线编辑器 STS 结果转换 */
    public static com.dji.flighthubsdk.bean.GetWaylineEditorStsResult toGetWaylineEditorStsResult(
            com.dji.flighthubsdk.generated.GetWaylineEditorStsResult n) {
        com.dji.flighthubsdk.bean.GetWaylineEditorStsResult r = new com.dji.flighthubsdk.bean.GetWaylineEditorStsResult();
        if (n == null) return r;
        com.dji.flighthubsdk.bean.WaylineEditorStsCredentials creds =
                new com.dji.flighthubsdk.bean.WaylineEditorStsCredentials(
                        safeString(n.getCredentials().getAccess_key_id()),
                        safeString(n.getCredentials().getAccess_key_secret()),
                        n.getCredentials().getExpire(),
                        safeString(n.getCredentials().getSecurity_token()),
                        n.getCredentials().getPlatform());
        r.setCredentials(creds);
        r.setObjectKeyPrefix(safeString(n.getObject_key_prefix()));
        r.setBucket(safeString(n.getBucket()));
        r.setEndpoint(safeString(n.getEndpoint()));
        r.setProvider(safeString(n.getProvider()));
        r.setRegion(safeString(n.getRegion()));
        return r;
    }

    // ===================== EA340 消息推送补全（Phase D）转换方法 Start =====================

    public static com.dji.flighthubsdk.bean.LiveStatusItem toLiveStatusItem(
            com.dji.flighthubsdk.generated.LiveStatusItem n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.LiveStatusItem();
        }
        return new com.dji.flighthubsdk.bean.LiveStatusItem(
                safeString(n.getVideo_id()),
                safeString(n.getVideo_type()),
                n.getVideo_quality(),
                n.getStatus(),
                n.getError_status());
    }

    public static List<com.dji.flighthubsdk.bean.LiveStatusItem> toLiveStatusItemList(
            com.dji.flighthubsdk.generated.LiveStatusItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.LiveStatusItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLiveStatusItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.DockLiveStatus toDockLiveStatus(
            com.dji.flighthubsdk.generated.DockLiveStatus n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DockLiveStatus();
        }
        return new com.dji.flighthubsdk.bean.DockLiveStatus(toLiveStatusItemList(n.getLive_status()));
    }

    public static com.dji.flighthubsdk.bean.LiveCapacityVideo toLiveCapacityVideo(
            com.dji.flighthubsdk.generated.LiveCapacityVideo n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.LiveCapacityVideo();
        }
        return new com.dji.flighthubsdk.bean.LiveCapacityVideo(
                safeString(n.getVideo_index()),
                safeString(n.getVideo_type()),
                copyStringVector(n.getSwitchable_video_types()));
    }

    public static List<com.dji.flighthubsdk.bean.LiveCapacityVideo> toLiveCapacityVideoList(
            com.dji.flighthubsdk.generated.LiveCapacityVideoVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.LiveCapacityVideo> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLiveCapacityVideo(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.LiveCapacityCamera toLiveCapacityCamera(
            com.dji.flighthubsdk.generated.LiveCapacityCamera n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.LiveCapacityCamera();
        }
        return new com.dji.flighthubsdk.bean.LiveCapacityCamera(
                safeString(n.getCamera_index()),
                n.getAvailable_video_number(),
                n.getCoexist_video_number_max(),
                toLiveCapacityVideoList(n.getVideo_list()));
    }

    public static List<com.dji.flighthubsdk.bean.LiveCapacityCamera> toLiveCapacityCameraList(
            com.dji.flighthubsdk.generated.LiveCapacityCameraVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.LiveCapacityCamera> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLiveCapacityCamera(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.LiveCapacityDevice toLiveCapacityDevice(
            com.dji.flighthubsdk.generated.LiveCapacityDevice n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.LiveCapacityDevice();
        }
        return new com.dji.flighthubsdk.bean.LiveCapacityDevice(
                safeString(n.getSn()),
                n.getAvailable_video_number(),
                n.getCoexist_video_number_max(),
                toLiveCapacityCameraList(n.getCamera_list()));
    }

    public static List<com.dji.flighthubsdk.bean.LiveCapacityDevice> toLiveCapacityDeviceList(
            com.dji.flighthubsdk.generated.LiveCapacityDeviceVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.LiveCapacityDevice> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toLiveCapacityDevice(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.DockLiveCapacity toDockLiveCapacity(
            com.dji.flighthubsdk.generated.DockLiveCapacity n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DockLiveCapacity();
        }
        return new com.dji.flighthubsdk.bean.DockLiveCapacity(
                n.getAvailable_video_number(),
                n.getCoexist_video_number_max(),
                toLiveCapacityDeviceList(n.getDevice_list()));
    }

    public static com.dji.flighthubsdk.bean.EsimInfoItem toEsimInfoItem(
            com.dji.flighthubsdk.generated.EsimInfoItem n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.EsimInfoItem();
        }
        return new com.dji.flighthubsdk.bean.EsimInfoItem(n.getEnabled(), safeString(n.getIccid()), n.getTelecom_operator());
    }

    public static List<com.dji.flighthubsdk.bean.EsimInfoItem> toEsimInfoItemList(
            com.dji.flighthubsdk.generated.EsimInfoItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.EsimInfoItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toEsimInfoItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.SimInfoItem toSimInfoItem(
            com.dji.flighthubsdk.generated.SimInfoItem n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.SimInfoItem();
        }
        return new com.dji.flighthubsdk.bean.SimInfoItem(safeString(n.getIccid()), n.getTelecom_operator());
    }

    public static com.dji.flighthubsdk.bean.DongleInfoItem toDongleInfoItem(
            com.dji.flighthubsdk.generated.DongleInfoItem n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DongleInfoItem();
        }
        return new com.dji.flighthubsdk.bean.DongleInfoItem(
                n.getDongle_type(),
                safeString(n.getEid()),
                n.getEsim_activate_state(),
                toEsimInfoItemList(n.getEsim_infos()),
                safeString(n.getImei()),
                n.getSim_card_state(),
                n.getSim_slot(),
                toSimInfoItem(n.getSim_info()),
                n.getTelecom_operator());
    }

    public static List<com.dji.flighthubsdk.bean.DongleInfoItem> toDongleInfoItemList(
            com.dji.flighthubsdk.generated.DongleInfoItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.DongleInfoItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toDongleInfoItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.DockDongleInfos toDockDongleInfos(
            com.dji.flighthubsdk.generated.DockDongleInfos n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DockDongleInfos();
        }
        return new com.dji.flighthubsdk.bean.DockDongleInfos(toDongleInfoItemList(n.getDongle_infos()));
    }

    public static com.dji.flighthubsdk.bean.DroneDongleInfos toDroneDongleInfos(
            com.dji.flighthubsdk.generated.DroneDongleInfos n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneDongleInfos();
        }
        return new com.dji.flighthubsdk.bean.DroneDongleInfos(toDongleInfoItemList(n.getDongle_infos()));
    }

    public static com.dji.flighthubsdk.bean.DockAirConditionerMode toDockAirConditionerMode(
            com.dji.flighthubsdk.generated.DockAirConditionerMode n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DockAirConditionerMode(0);
        }
        return new com.dji.flighthubsdk.bean.DockAirConditionerMode(n.getAir_conditioner_mode());
    }

    public static com.dji.flighthubsdk.bean.DroneFlysafeDatabaseVersion toDroneFlysafeDatabaseVersion(
            com.dji.flighthubsdk.generated.DroneFlysafeDatabaseVersion n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneFlysafeDatabaseVersion();
        }
        return new com.dji.flighthubsdk.bean.DroneFlysafeDatabaseVersion(safeString(n.getFlysafe_database_version()));
    }

    public static com.dji.flighthubsdk.bean.PsdkUiResourceItem toPsdkUiResourceItem(
            com.dji.flighthubsdk.generated.PsdkUiResourceItem n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.PsdkUiResourceItem();
        }
        return new com.dji.flighthubsdk.bean.PsdkUiResourceItem(
                n.getPsdk_index(), n.getPsdk_ready(), safeString(n.getObject_key()));
    }

    public static List<com.dji.flighthubsdk.bean.PsdkUiResourceItem> toPsdkUiResourceItemList(
            com.dji.flighthubsdk.generated.PsdkUiResourceItemVector v) {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.dji.flighthubsdk.bean.PsdkUiResourceItem> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            out.add(toPsdkUiResourceItem(v.get(i)));
        }
        return Collections.unmodifiableList(out);
    }

    public static com.dji.flighthubsdk.bean.DronePsdkUiResource toDronePsdkUiResource(
            com.dji.flighthubsdk.generated.DronePsdkUiResource n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DronePsdkUiResource();
        }
        return new com.dji.flighthubsdk.bean.DronePsdkUiResource(toPsdkUiResourceItemList(n.getPsdk_ui_resource()));
    }

    public static com.dji.flighthubsdk.bean.DroneModeCodeReason toDroneModeCodeReason(
            com.dji.flighthubsdk.generated.DroneModeCodeReason n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneModeCodeReason(0);
        }
        return new com.dji.flighthubsdk.bean.DroneModeCodeReason(n.getMode_code_reason());
    }

    public static com.dji.flighthubsdk.bean.DroneCommanderFlightMode toDroneCommanderFlightMode(
            com.dji.flighthubsdk.generated.DroneCommanderFlightMode n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneCommanderFlightMode(0);
        }
        return new com.dji.flighthubsdk.bean.DroneCommanderFlightMode(n.getCommander_flight_mode());
    }

    public static com.dji.flighthubsdk.bean.DroneCurrentCommanderFlightMode toDroneCurrentCommanderFlightMode(
            com.dji.flighthubsdk.generated.DroneCurrentCommanderFlightMode n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneCurrentCommanderFlightMode(0);
        }
        return new com.dji.flighthubsdk.bean.DroneCurrentCommanderFlightMode(n.getCurrent_commander_flight_mode());
    }

    public static com.dji.flighthubsdk.bean.DroneCommanderModeLostAction toDroneCommanderModeLostAction(
            com.dji.flighthubsdk.generated.DroneCommanderModeLostAction n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneCommanderModeLostAction(0);
        }
        return new com.dji.flighthubsdk.bean.DroneCommanderModeLostAction(n.getCommander_mode_lost_action());
    }

    public static com.dji.flighthubsdk.bean.DroneWpmzVersion toDroneWpmzVersion(
            com.dji.flighthubsdk.generated.DroneWpmzVersion n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneWpmzVersion();
        }
        return new com.dji.flighthubsdk.bean.DroneWpmzVersion(safeString(n.getWpmz_version()));
    }

    public static com.dji.flighthubsdk.bean.DroneOfflineMapEnable toDroneOfflineMapEnable(
            com.dji.flighthubsdk.generated.DroneOfflineMapEnable n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneOfflineMapEnable(false);
        }
        return new com.dji.flighthubsdk.bean.DroneOfflineMapEnable(n.getOffline_map_enable());
    }

    public static com.dji.flighthubsdk.bean.DroneCameraWatermarkSettingsPush toDroneCameraWatermarkSettingsPush(
            com.dji.flighthubsdk.generated.DroneCameraWatermarkSettingsPush n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.DroneCameraWatermarkSettingsPush();
        }
        return new com.dji.flighthubsdk.bean.DroneCameraWatermarkSettingsPush(
                n.getGlobal_enable(),
                n.getDrone_type_enable(),
                n.getDrone_sn_enable(),
                n.getDatetime_enable(),
                n.getGps_enable(),
                n.getUser_custom_string_enable(),
                safeString(n.getUser_custom_string()),
                n.getLayout());
    }

    // ===================== EA340 消息推送补全（Phase D）转换方法 End =====================

    // ===================== 云环境切换 转换方法 Begin =====================

    private static com.dji.flighthubsdk.generated.CloudEnvDeviceInfo toNativeCloudEnvDeviceInfo(
            com.dji.flighthubsdk.bean.CloudEnvDeviceInfo info) {
        com.dji.flighthubsdk.generated.CloudEnvDeviceInfo nativeInfo = new com.dji.flighthubsdk.generated.CloudEnvDeviceInfo();
        if (info == null) {
            return nativeInfo;
        }
        nativeInfo.setCallsign(info.getCallsign());
        nativeInfo.setDevice_model_key(info.getDeviceModelKey());
        nativeInfo.setDevice_sn(info.getDeviceSn());
        return nativeInfo;
    }

    private static com.dji.flighthubsdk.generated.CloudEnvDeviceList toNativeCloudEnvDeviceList(
            com.dji.flighthubsdk.bean.CloudEnvDeviceList list) {
        com.dji.flighthubsdk.generated.CloudEnvDeviceList nativeList = new com.dji.flighthubsdk.generated.CloudEnvDeviceList();
        if (list == null) {
            return nativeList;
        }
        com.dji.flighthubsdk.generated.CloudEnvDeviceInfoVector host = new com.dji.flighthubsdk.generated.CloudEnvDeviceInfoVector();
        if (list.getHost() != null) {
            for (com.dji.flighthubsdk.bean.CloudEnvDeviceInfo item : list.getHost()) {
                if (item != null) {
                    host.add(toNativeCloudEnvDeviceInfo(item));
                }
            }
        }
        nativeList.setHost(host);

        com.dji.flighthubsdk.generated.CloudEnvDeviceInfoVector nodes = new com.dji.flighthubsdk.generated.CloudEnvDeviceInfoVector();
        if (list.getNodes() != null) {
            for (com.dji.flighthubsdk.bean.CloudEnvDeviceInfo item : list.getNodes()) {
                if (item != null) {
                    nodes.add(toNativeCloudEnvDeviceInfo(item));
                }
            }
        }
        nativeList.setNodes(nodes);

        com.dji.flighthubsdk.generated.CloudEnvDeviceInfoVector parents = new com.dji.flighthubsdk.generated.CloudEnvDeviceInfoVector();
        if (list.getParents() != null) {
            for (com.dji.flighthubsdk.bean.CloudEnvDeviceInfo item : list.getParents()) {
                if (item != null) {
                    parents.add(toNativeCloudEnvDeviceInfo(item));
                }
            }
        }
        nativeList.setParents(parents);
        return nativeList;
    }

    private static com.dji.flighthubsdk.generated.CloudEnvTargetConfig toNativeCloudEnvTargetConfig(
            com.dji.flighthubsdk.bean.CloudEnvTargetConfig config) {
        com.dji.flighthubsdk.generated.CloudEnvTargetConfig nativeConfig = new com.dji.flighthubsdk.generated.CloudEnvTargetConfig();
        if (config == null) {
            return nativeConfig;
        }
        nativeConfig.setType(config.getType());

        com.dji.flighthubsdk.generated.CloudEnvPublicCloudConfig publicCfg = new com.dji.flighthubsdk.generated.CloudEnvPublicCloudConfig();
        if (config.getPublicCloudConfig() != null) {
            publicCfg.setBind_code(config.getPublicCloudConfig().getBindCode());
            publicCfg.setOrg_id(config.getPublicCloudConfig().getOrgId());
            publicCfg.setZone_id(config.getPublicCloudConfig().getZoneId());
        }
        nativeConfig.setPublic_cloud_config(publicCfg);

        com.dji.flighthubsdk.generated.CloudEnvPrivateCloudConfig privateCfg = new com.dji.flighthubsdk.generated.CloudEnvPrivateCloudConfig();
        if (config.getPrivateCloudConfig() != null) {
            privateCfg.setBind_code(config.getPrivateCloudConfig().getBindCode());
            privateCfg.setOrg_id(config.getPrivateCloudConfig().getOrgId());
            privateCfg.setUrl(config.getPrivateCloudConfig().getUrl());
        }
        nativeConfig.setPrivate_cloud_config(privateCfg);

        com.dji.flighthubsdk.generated.CloudEnvThirdCloudConfig thirdCfg = new com.dji.flighthubsdk.generated.CloudEnvThirdCloudConfig();
        if (config.getThirdCloudConfig() != null) {
            thirdCfg.setBind_code(config.getThirdCloudConfig().getBindCode());
            thirdCfg.setOrg_id(config.getThirdCloudConfig().getOrgId());
            thirdCfg.setMqtt_addr(config.getThirdCloudConfig().getMqttAddr());
            thirdCfg.setMqtt_password(config.getThirdCloudConfig().getMqttPassword());
            thirdCfg.setMqtt_username(config.getThirdCloudConfig().getMqttUsername());
        }
        nativeConfig.setThird_cloud_config(thirdCfg);
        return nativeConfig;
    }

    public static com.dji.flighthubsdk.generated.CloudEnvironmentSwitchParam toNativeCloudEnvironmentSwitchParam(
            com.dji.flighthubsdk.bean.CloudEnvironmentSwitchParam param) {
        com.dji.flighthubsdk.generated.CloudEnvironmentSwitchParam nativeParam = new com.dji.flighthubsdk.generated.CloudEnvironmentSwitchParam();
        com.dji.flighthubsdk.generated.CloudEnvDeviceListVector vector = new com.dji.flighthubsdk.generated.CloudEnvDeviceListVector();
        if (param != null && param.getDevicesList() != null) {
            for (com.dji.flighthubsdk.bean.CloudEnvDeviceList item : param.getDevicesList()) {
                if (item != null) {
                    vector.add(toNativeCloudEnvDeviceList(item));
                }
            }
        }
        nativeParam.setDevices_list(vector);
        nativeParam.setTarget_config(toNativeCloudEnvTargetConfig(param != null ? param.getTargetConfig() : null));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.generated.CloudEnvironmentSwitchCallbackParam toNativeCloudEnvironmentSwitchCallbackParam(
            com.dji.flighthubsdk.bean.CloudEnvironmentSwitchCallbackParam param) {
        com.dji.flighthubsdk.generated.CloudEnvironmentSwitchCallbackParam nativeParam = new com.dji.flighthubsdk.generated.CloudEnvironmentSwitchCallbackParam();
        if (param == null) {
            return nativeParam;
        }
        nativeParam.setCode(param.getCode());
        nativeParam.setStatus(param.getStatus());

        com.dji.flighthubsdk.generated.DeviceAuthVerifyParam authVerify = new com.dji.flighthubsdk.generated.DeviceAuthVerifyParam();
        if (param.getDeviceAuthVerify() != null) {
            authVerify.setSn(param.getDeviceAuthVerify().getSn());
            authVerify.setSecret(param.getDeviceAuthVerify().getSecret());
            authVerify.setNonce(param.getDeviceAuthVerify().getNonce());
        }
        nativeParam.setDevice_auth_verify(authVerify);

        com.dji.flighthubsdk.generated.CloudEnvDeviceListVector vector = new com.dji.flighthubsdk.generated.CloudEnvDeviceListVector();
        if (param.getDevicesList() != null) {
            for (com.dji.flighthubsdk.bean.CloudEnvDeviceList item : param.getDevicesList()) {
                if (item != null) {
                    vector.add(toNativeCloudEnvDeviceList(item));
                }
            }
        }
        nativeParam.setDevices_list(vector);
        nativeParam.setTarget_config(toNativeCloudEnvTargetConfig(param.getTargetConfig()));
        return nativeParam;
    }

    public static com.dji.flighthubsdk.bean.CloudEnvironmentSwitchResult toCloudEnvironmentSwitchResult(
            com.dji.flighthubsdk.generated.CloudEnvironmentSwitchResult n) {
        if (n == null) {
            return new com.dji.flighthubsdk.bean.CloudEnvironmentSwitchResult("", "", 0);
        }
        return new com.dji.flighthubsdk.bean.CloudEnvironmentSwitchResult(safeString(n.getMessage()), safeString(n.getBid()), n.getProgress());
    }

    // ===================== 云环境切换 转换方法 End =====================
}
