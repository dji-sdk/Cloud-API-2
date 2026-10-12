// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <functional>
#include <thread>
#include <vector>
#include <cstdint>
#include "api/message.h"
#include "beacon_event.h"
#include "response_message.h"
#include "request_message.h"
#include "http_runtime.h"

namespace flight_hub_sdk {
// event-tracking data report;
const std::string kBeaconReportAPI = "/gateway/pf-beacon/ingest/inner/v1/report";


const std::string kDeviceTopoListAPI = "/gateway/device/api/v1/manager/device/topologies";

const std::string kDevicePropertiesAPI = "/gateway/device/api/v1/manager/properties";
const std::string kLiveStreamStartAPI = "/gateway/pf-live/api/v1/stream/start";
const std::string kLiveStreamStopAPI  = "/gateway/pf-live/api/v1/stream/stop";
const std::string kLiveStreamStateUpdateAPI = "/gateway/pf-live/api/v1/stream/state";
const std::string kLiveStreamStateUpdateTokenAPI = "/gateway/pf-live/api/v1/live-channels/stream/update-token";
const std::string kLiveStreamChangeCameraAPI = "/gateway/pf-live/api/v1/camera/change";
const std::string kLiveRecordStartAPI = "/gateway/pf-live/api/v1/record/start";
const std::string kLiveRecordStopAPI = "/gateway/pf-live/api/v1/record/stop";
const std::string kLiveRecordDeviceAPI = "/gateway/pf-live/api/v1/record/device/{sn}";
const std::string kAudienceCountAPI = "/gateway/pf-live/api/v1/stream/audience-count";

const std::string kFlightTaskListAPI = "/gateway/atc/api/v1/flight-tasks";
const std::string kCommandTaskListAPI = "/gateway/atc/api/v1/command-tasks";
const std::string kFlightTaskTrackAPI = "/gateway/atc/api/v1/flight-tasks/track";
const std::string kFlightTaskCreateAPI = "/gateway/atc/api/v1/flight-tasks";
const std::string kFlightTaskDetailAPI = "/gateway/atc/api/v1/flight-tasks/{task_uuid}";
const std::string kFlightTaskEditAPI = "/gateway/atc/api/v1/flight-tasks/{task_uuid}";
const std::string kFlightTaskDeleteAPI = "/gateway/atc/api/v1/flight-tasks/{task_uuid}";
const std::string kFlightTaskStatusAPI = "/gateway/atc/api/v1/flight-tasks/{task_uuid}/status";
const std::string kFlightTaskBatchStatusAPI = "/gateway/atc/api/v1/flight-tasks/batch/status";
const std::string kFlightTaskDeviceRecommendationAPI = "/gateway/atc/api/v1/flight-tasks/device-recommendation";
const std::string kFlightTaskTcasAPI = "/gateway/atc/api/v1/flight-tasks/tcas";
const std::string kFlightTaskStartRoamingAPI = "/gateway/atc/api/v1/flight-tasks/start-roaming";
const std::string kFlightTaskInFlightTaskToposAPI = "/gateway/atc/api/v1/flight-tasks/in-flight-task-topos";
const std::string kFlightTaskRoamingBaseStationsAPI = "/gateway/atc/api/v1/flight-tasks/roaming/base-stations";
const std::string kOpenDataChannelAPI = "/gateway/paas/api/v1/rt-channels/{sn}";
const std::string kControlAuthorityAPI = "/gateway/drc/api/v1/cloud_controls";
const std::string kFlyToPointAPI = "/gateway/atc/api/v1/flight-tasks/fly-to-points";
const std::string kUpdateFlyToPointsAPI = "/gateway/atc/api/v1/flight-tasks/fly-to-points";   // 更新飞向目标点;
// 空中下发航线（drc.proto v3）
const std::string kCreateInFlightWaylineTaskAPI = "/gateway/drc/api/v3/flight-tasks/in-flight-way-line-task";
const std::string kControlInFlightWaylineTaskAPI = "/gateway/drc/api/v3/flight-wayline-tasks/commands";
const std::string kGetActiveInFlightWaylineTaskAPI = "/gateway/drc/api/v3/flight-tasks/in-flight";
const std::string kNonRealtimeCmdAPI = "/gateway/control/api/v1/command-bus/non-realtime-cmd";
const std::string kPsdkSpeakerResourceListAPI = "/gateway/pf-media/api/v2/speaker/resources";
const std::string kCreatePsdkSpeakerResourceAPI = "/gateway/pf-media/api/v2/speaker/resources";
const std::string kEditPsdkSpeakerResourceAPI = "/gateway/pf-media/api/v2/speaker/resources/{id}";
const std::string kDeletePsdkSpeakerResourceAPI = "/gateway/pf-media/api/v2/speaker/resources/{id}";
const std::string kGetSTSAPI = "/gateway/pf-media/api/v2/speaker/sts";
const std::string kGetPsdkWidgetResourceAPI = "/gateway/control/api/v1/manual/psdk/ui-resources/batch-get";
// PAAS flash-configs（drc.proto DrcDeviceControlService；无 project_id）
const std::string kFlashLedConfigListAPI = "/gateway/drc/api/v2/flash-configs";
const std::string kCreateFlashLedConfigAPI = "/gateway/drc/api/v2/flash-configs";
const std::string kUpdateFlashLedConfigAPI = "/gateway/drc/api/v2/flash-configs/{id}";
const std::string kDeleteFlashLedConfigAPI = "/gateway/drc/api/v2/flash-configs/{id}";
const std::string kDroneTakeoffAPI = "/gateway/atc/api/v1/flight-tasks/command";
// POI
const std::string kPOIEnterAPI = "/gateway/atc/api/v1/flight-tasks/poi-enter";
const std::string kPOIExitAPI = "/gateway/atc/api/v1/flight-tasks/poi-exit";
const std::string kPOISpeedUpdateAPI = "/gateway/atc/api/v1/flight-tasks/poi-speed";
const std::string kGetPOITaskAPI = "/gateway/atc/api/v1/flight-tasks/poi-task";

// 获取设备报修配置;
const std::string kMaintenanceOrdersRepairConfigAPI = "/gateway/device/api/v1/manager/maintenance-orders/repair-config";

// 创建报修订单;
// 分页查询报修订单列表;
const std::string kMaintenanceOrdersAPI= "/gateway/device/api/v1/manager/maintenance-orders";

// 查询单个报修订单详情;
const std::string kMaintenanceOrderDetailAPI = "/gateway/device/api/v1/manager/maintenance-orders/{orderNumber}";

// 设备定损: 下发定损指令;
const std::string kSelfDiagnosticAPI = "/gateway/device/api/v1/service/self-diagnostic";

// 设备定损: 定损操作(取消/删除);
const std::string kSelfDiagnosticOperationAPI = "/gateway/device/api/v1/manager/self-diagnostic/operation";

// 设备定损: 查询单条定损报告详情;
const std::string kSelfDiagnosticReportAPI = "/gateway/device/api/v1/manager/self-diagnostic/{report_uuid}";

// 设备定损: 查询定损报告列表;
const std::string kSelfDiagnosticReportListAPI = "/gateway/device/api/v1/manager/self-diagnostic";

// AI Box: 获取 AI Box 信息;
const std::string kAIBoxInfoAPI = "/gateway/device/api/v1/service/aibox/ai-box-info";

// AI Box: 获取文件下载 URL;
const std::string kAIBoxFileDownloadURLAPI = "/gateway/device/api/v1/service/aibox/file-download-url";

//  AI Box: 获取应用列表;
const std::string kAIBoxAppListAPI = "/gateway/device/api/v1/service/aibox/app-list";

// AI Box: 查询应用列表进度;
const std::string kAIBoxAppListProgressAPI = "/gateway/device/api/v1/service/aibox/app-list-progress";

// AI Box: 导出文件;
const std::string kAIBoxExportFileAPI = "/gateway/device/api/v1/service/aibox/export-file";

// AI Box: 查询文件导出进度;
const std::string kAIBoxExportFileProgressAPI = "/gateway/device/api/v1/service/aibox/export-file-progress";

// AI Box: 安装应用;
const std::string kAIBoxInstallAppAPI = "/gateway/device/api/v1/service/aibox/install-app";

// AI Box: 查询应用安装进度;
const std::string kAIBoxInstallAppProgressAPI = "/gateway/device/api/v1/service/aibox/install-app-progress";

// AI Box: 操作应用;
const std::string kAIBoxOperateAppAPI = "/gateway/device/api/v1/service/aibox/operate-app";

/// AI Box: 获取应用上传(STS凭证);
const std::string kAIBoxUploadAppStsAPI = "/gateway/device/api/v1/service/aibox/upload-app-sts";



// 飞机星历下载;
const std::string kDownloadEphemerisAPI = "/gateway/control/api/v1/manual/scout/ephemeris/url";


// 开始/暂停进离场任务;
const std::string kDetectTaskAPI = "/gateway/control/api/v1/manual/scout/task";

// 进离场航线飞行设置;
const std::string kFlightSettingAPI = "/gateway/control/api/v1/manual/scout/flight/setting";

// 直播画质设置;
const std::string kLiveSetQualityAPI = "/gateway/pf-live/api/v1/stream/set-quality";


// 试飞航线生成
const std::string kGenerateTryFlightWaylineAPI = "/gateway/wayline/api/v1/workspaces/{project_id}/try-fly-wayline";

// 试飞航线下发
const std::string kCreateTestFlightTaskAPI = "/gateway/task/api/v1/workspaces/{project_id}/flight-tasks/test-flight";

// 媒体文件列表
const std::string kMediaFileListAPI = "/gateway/media/api/v1/workspaces/{project_id}/files";

// Dock Supplement Light
const std::string kDockSupplementLightOpenAPI = "/gateway/device/api/v1/service/supplement-light-open";
const std::string kDockSupplementLightCloseAPI = "/gateway/device/api/v1/service/supplement-light-close";

// Dock Charge Control
const std::string kDockChargeOpenAPI = "/gateway/device/api/v1/service/charge-open";
const std::string kDockChargeCloseAPI = "/gateway/device/api/v1/service/charge-close";

// Dock Cover Control
const std::string kDockCoverOpenAPI = "/gateway/device/api/v1/service/cover-open";
const std::string kDockCoverCloseAPI = "/gateway/device/api/v1/service/cover-close";

// Dock Air Conditioner Control
const std::string kDockAirConditionerAPI = "/gateway/device/api/v1/service/air-conditioners";

const std::string kOpenDebugModeAPI = "/gateway/device/api/v1/service/debug-mode-open";
const std::string kCloseDebugModeAPI = "/gateway/device/api/v1/service/debug-mode-close";
const std::string kSetDistanceLimitAPI = "/gateway/device/api/v1/service/distance-limit";
const std::string kSetHeightLimitAPI = "/gateway/device/api/v1/service/height-limit";
const std::string kSetObstacleAvoidanceAPI = "/gateway/device/api/v1/service/obstacle-avoidance";
const std::string kSetRemainingPowerForRthAPI = "/gateway/device/api/v1/service/remaining-power-for-rth";
const std::string kSetNightLightsStateAPI = "/gateway/device/api/v1/service/night-lights-state";
const std::string kAlarmStateSwitchAPI = "/gateway/device/api/v1/service/alarm-state-switch";
const std::string kEsimOperatorSwitchAPI = "/gateway/device/api/v1/service/esim-operator-switch";
const std::string kSimSlotSwitchAPI = "/gateway/device/api/v1/service/sim-slot-switch";
const std::string kCloudEnvironmentSwitchAPI = "/gateway/device/api/v1/service/cloud-environment-switch";
const std::string kCloudEnvironmentSwitchCallbackAPI = "/gateway/device/api/v1/service/cloud_environment_switch/callback";
const std::string kBatteryStoreModeSwitchAPI = "/gateway/device/api/v1/service/battery-store-mode-switch";
const std::string kManualBatterySwapAPI = "/gateway/device/api/v1/service/manual-battery-swap";
const std::string kDonglePowerSwitchAPI = "/gateway/device/api/v1/service/dongle-power-switch";
const std::string kLowPowerSwitchAPI = "/gateway/device/api/v1/service/low-power-switch";
const std::string kOpenDockCoverAPI = "/gateway/device/api/v1/service/cover-open";
const std::string kCloseDockCoverAPI = "/gateway/device/api/v1/service/cover-close";
const std::string kDeviceRebootAPI = "/gateway/device/api/v1/service/device-reboot";
const std::string kDeviceFormatAPI = "/gateway/device/api/v1/service/device-format";
const std::string kDeviceStateAPI = "/gateway/device/api/v1/manager/device-properties";
const std::string kDroneOpenAPI = "/gateway/device/api/v1/service/drone-open";
const std::string kDroneCloseAPI = "/gateway/device/api/v1/service/drone-close";
const std::string kOpenChargeAPI = "/gateway/device/api/v1/service/charge-open";
const std::string kCloseChargeAPI = "/gateway/device/api/v1/service/charge-close";
const std::string kDockSilentModePI = "/gateway/device/api/v1/service/silent-mode";
const std::string kStartNetworkSpeedTestAPI = "/gateway/device/api/v1/service/network/speed-tests/start";
const std::string kStopNetworkSpeedTestAPI = "/gateway/device/api/v1/service/network/speed-tests";
const std::string kNetworkSpeedTestProgressAPI = "/gateway/device/api/v1/service/network/speed-tests";
const std::string kRtkCalibrationAPI = "/gateway/device/api/v1/service/rtk-calibration";
const std::string kDroneFormatAPI = "/gateway/device/api/v1/service/drone-format";
const std::string kDroneSdrWorkModeAPI = "/gateway/device/api/v1/service/sdr-workmode-switch";
const std::string kDeviceLogListAPI = "/gateway/device/api/v1/manager/reports/devices/logs";
const std::string kCreateDeviceLogReportAPI = "/gateway/device/api/v1/manager/reports";
const std::string kLogReportListAPI = "/gateway/device/api/v1/manager/reports";
const std::string kLogReportOperationAPI = "/gateway/device/api/v1/manager/reports/operation";
const std::string kLogReportAppendStsAPI = "/gateway/device/api/v1/manager/reports/sts";
// Geo Caging 接口
const std::string kGeoCagingEnableAPI = "/gateway/device/api/v1/service/geo-caging-enable";
// 中继站调试接口
const std::string kRelayBroadcastHideAPI = "/gateway/device/api/v1/service/relay-broadcast-hide";
const std::string kRelaySSIDAPI = "/gateway/device/api/v1/service/relay-ssid";
const std::string kRelayPasswordAPI = "/gateway/device/api/v1/service/relay-password";
const std::string kRelaySearchModeAPI = "/gateway/device/api/v1/service/relay-search-mode";
const std::string kRelayOccupiedStatusAPI = "/gateway/device/api/v1/service/relay-occupied-status";
const std::string kRelayRebootAPI = "/gateway/device/api/v1/service/relay-reboot";
const std::string kRelayDisconnectAPI = "/gateway/device/api/v1/service/relay-disconnect";
const std::string kQueryHistoryHmsAPI = "/gateway/device/api/v1/manager/hms";
const std::string kUnBindDeviceAPI = "/gateway/device/api/v1/manager/unbind-devices";
const std::string kGetDeviceDetailAPI = "/gateway/device/api/v1/manager/device/detail";
const std::string kModifyDeviceNameAPI = "/gateway/device/api/v1/manager/device/info";
const std::string kGetLatestFirmwareReleaseNoteAPI = "/gateway/device/api/v1/firmware-release-notes/latest";
const std::string kInvokeFirmwareUpgradeAPI = "/gateway/device/api/v1/manager/ota/upgrade";
const std::string kQueryFirmwareUpgradeProgressAPI = "/gateway/device/api/v1/manager/ota/progress";
const std::string kFirmwareUpgradeStsAPI = "/gateway/device/api/v1/manager/firmwares/security-tokens";
const std::string kUpdateFirmwareReleaseStatusAPI = "/gateway/device/api/v1/manager/firmwares/{id}"; // 发布、取消发布、删除
const std::string kQueryFirmwareListAPI = "/gateway/device/api/v1/manager/firmwares";
const std::string kGetFirmwareCompatibilityAPI = "/gateway/device/api/v1/firmware_compatible/config/{userType}";
const std::string kCheckFirmwareStatusAPI = "/gateway/device/api/v1/manager/ota/infos";
const std::string kSubmitFirmwareInfoAPI = "/gateway/device/api/v1/manager/firmwares";
const std::string kGetUnlockLicenseListAPI = "/gateway/device/api/v1/service/unlock-license/list";
const std::string kSwitchUnlockLicenseAPI = "/gateway/device/api/v1/service/unlock-license/switch";
const std::string kCheckUnlockLicenseAPI = "/gateway/device/api/v1/service/unlock-license/check";
const std::string kImportUnlockLicenseAPI = "/gateway/device/api/v1/service/unlock-license/unlock";
const std::string kGetImportUnlockLicenseStsAPI = "/gateway/device/api/v1/service/unlock-license/sts/private";
const std::string kClearDeviceUnlockLicenseAPI = "/gateway/device/api/v1/service/unlock-license/clear";
const std::string kGetWaylineListAPI = "/gateway/pf-wayline/api/v1/list";
const std::string kGetWaylineDetailAPI = "/gateway/pf-wayline/api/v1/details";
const std::string kBatchDeleteWaylinesAPI = "/gateway/pf-wayline/api/v1/batch-delete";
const std::string kUpdateWaylineAPI = "/gateway/pf-wayline/api/v1/update";
const std::string kSubmitWaylineInfoAPI = "/gateway/pf-wayline/api/v1/import-callback";
const std::string kBatchDownloadWaylinesAPI = "/gateway/pf-wayline/api/v1/batch-download";
const std::string kDuplicateWaylineAPI = "/gateway/pf-wayline/api/v1/duplicate";
const std::string kCopyWaylineAPI = "/gateway/pf-wayline/api/v1/copy";
const std::string kConcatWaylineAPI = "/gateway/pf-wayline/api/v1/concat";
const std::string kImportKmzAPI = "/gateway/pf-wayline/api/v1/import-kmz";
const std::string kGetWaylineEditorStsAPI = "/gateway/pf-wayline/editor/v1/sts"; // 航线编辑器 STS

// 安全服务相关 API;
// 特定 flight-area-id 常量;
const std::string kFlightAreaID = "{flight-area-id}";
/// [信号地图];
const std::string kSearchSignalMapsAPI = "/gateway/safety/api/v1/signal-maps/search";    // 信号地图查询;
/// [自定义飞行区];
const std::string kCreateFlightAreas = "/gateway/safety/api/v2/flight-areas";    // 创建 自定义飞行区;
const std::string kGetFlightAreas = "/gateway/safety/api/v2/flight-areas";       // 查询/获取 自定义飞行区;
// "/safety/api/v2/flight-areas/{flight-area-id}"
const std::string kUpdateFlightAreas = "/gateway/safety/api/v2/flight-areas/" + kFlightAreaID;     // 更新 自定义飞行区;
// "/safety/api/v2/flight-areas/{flight-area-id}"
const std::string kDeleteFlightAreas = "/gateway/safety/api/v2/flight-areas/" + kFlightAreaID;     // 删除 自定义飞行区;
const std::string kSyncFlightAreas = "/gateway/safety/api/v1/flight-areas/sync";                   // 同步自定义飞行区;
const std::string kFlightAreasSyncStatus = "/gateway/safety/api/v1/flight-areas/device-status";    // 获取设备同步状态;
const std::string kGetOfflineMaps = "/gateway/safety/api/v1/offline-maps";                         // 查询离线地图模型信息;
const std::string kUpdateOfflineMaps = "/gateway/safety/api/v1/offline-maps";                      // 更新离线地图模型;
const std::string kCancelOfflineMaps = "/gateway/safety/api/v1/offline-maps";                      // 取消离线地图生成;
const std::string kModifyOfflineMaps = "/gateway/safety/api/v1/offline-maps";                      // 修改离线地图(开启/关闭);
const std::string kSyncOfflineMaps = "/gateway/safety/api/v1/offline-maps/sync";                   // 离线地图同步;
const std::string kSyncOfflineMapsStatus = "/gateway/safety/api/v1/offline-maps/device-status";    // 查询离线地图同步状态;

// 媒体服务相关 API;
const std::string kGetMediaFileListAPI = "/gateway/pf-media/api/v1/media-files/list";              // 获取媒体服务文件列表;
const std::string kDeleteMediaFileAPI = "/gateway/pf-media/api/v1/media-files/batch-delete";       // 批量删除媒体文件;
const std::string kGetMediaFileUrlAPI = "/gateway/pf-media/api/v1/media-files/url";                // 获取媒体文件下载地址;

// ATC 空管服务相关 API;
const std::string kExportFlightRecordsAPI = "/gateway/atc/api/v1/flight-tasks/records/export";              // 导出飞行记录;
const std::string kGetExportFlightRecordsProgressAPI = "/gateway/atc/api/v1/flight-tasks/records/export";   // 查询导出进度;

// 飞行安全配置 API
const std::string kFlightSafeConfigAPI = "/gateway/manage/api/v1/flight/configs/safe";   // 飞行安全配置;
const std::string kAirTransferEnableAPI = "/gateway/device/api/v1/service/air-transfer-enable";   // 空中回传设置;

// Terra Build API（三维重建，不含 project_id）
const std::string kTerraBuildReconstructionSettingsAPI = "/gateway/pf-terra/api/v1/models/reconstruction/settings/{file_uuid}";
const std::string kTerraBuildQualityReportAPI = "/gateway/pf-terra/api/v1/models/reconstruction/{file_uuid}/quality-report";
const std::string kTerraBuildModelFileListAPI = "/gateway/pf-terra/api/v1/models/files";
const std::string kTerraBuildModelFileUrlAPI = "/gateway/pf-terra/api/v1/models/files/{file_id}/url";
const std::string kTerraBuildCreateReconstructionAPI = "/gateway/pf-terra/api/v1/models/reconstruction";
const std::string kTerraBuildRetryReconstructionAPI = "/gateway/pf-terra/api/v1/models/reconstruction/retry";
const std::string kTerraBuildStopReconstructionAPI = "/gateway/pf-terra/api/v1/models/reconstruction/stop";
const std::string kTerraBuildSfmResultAPI = "/gateway/pf-terra/api/v1/models/reconstruction/{file_uuid}/sfm-result";
const std::string kTerraBuildDeleteModelFileAPI = "/gateway/model/api/v1/files/{file_id}";
const std::string kTerraBuildBatchDeleteModelFilesAPI = "/gateway/model/api/v1/files";
const std::string kTerraBuildCreatePanoramaAPI = "/gateway/pf-terra/api/v1/models/panorama";
const std::string kTerraBuildReconstructionImagesAPI = "/gateway/pf-terra/api/v1/images/reconstruction/{file_uuid}";

// 元素管理（ElementService）接口
const std::string kElementCreateAPI           = "/gateway/pf-map/api/v1/elements/create";
const std::string kElementUpdateAPI           = "/gateway/pf-map/api/v1/elements/update/{id}";
const std::string kElementListHierarchyAPI    = "/gateway/pf-map/api/v1/elements/lists";
const std::string kElementSetVisibilityAPI    = "/gateway/pf-map/api/v1/elements/{id}";
const std::string kElementDeleteAPI           = "/gateway/pf-map/api/v1/elements/delete/{id}";
// Terra 模型管理新增接口
const std::string kTerraUploadCallbackAPI     = "/gateway/pf-terra/api/v1/models/files/upload-callback";
const std::string kTerraRenameModelAPI        = "/gateway/model/api/v1/files/{id}";
const std::string kTerraShowOnMapAPI          = "/gateway/model/api/v1/files";
const std::string kTerraGetModelDetailAPI     = "/gateway/pf-terra/api/v1/models/files/{file_id}";
const std::string kTerraSetLasWktAPI          = "/gateway/pf-terra/api/v1/models/converters";

// 媒体上传 API
const std::string kMediaUploadStsAPI      = "/gateway/pf-media/inner/v1/media-files/sts";
const std::string kMediaUploadCallbackAPI = "/gateway/pf-media/api/v1/upload-callback";
const std::string kUploadGeoMediaFileAPI  = "/gateway/pf-media/api/v1/geo-media-file";      // 图片备注上传回调
const std::string kGetMediaFileDetailAPI  = "/gateway/pf-media/api/v1/media-files/{file_id}"; // 轮询文件 URL

// AI Inside 机载算法管理 API
// ai-inside/* 四个接口：预期在 pf-live-service（与 SAAS/平台一致）；PAAS ES340 期间误开发到 MANAGE 服务，
// 保留 live/api/v1 前缀与 SAAS 现状对齐
// ai-inside-algorithm/* 三个接口：正常在 MANAGE 服务。
const std::string kGetAIInsideModelsAPI              = "/gateway/live/api/v1/manage/api/v1/ai-inside/models";
// 以下三个 API 路径含 {signed_id}，调用时须将占位符替换为实际 signed_id 值
const std::string kGetAIInsideModelDevicesAPI        = "/gateway/live/api/v1/manage/api/v1/ai-inside/models/{signed_id}/devices";
const std::string kGetAIInsideModelDevicesSummaryAPI = "/gateway/live/api/v1/manage/api/v1/ai-inside/models/{signed_id}/devices/summary";
const std::string kOperateAIInsideModelAPI           = "/gateway/live/api/v1/manage/api/v1/ai-inside/models/{signed_id}/operation";
const std::string kCreateAIInsideAlgorithmAPI        = "/gateway/manage/api/v1/ai-inside-algorithm/create";
const std::string kFileUploadAIInsideAlgorithmAPI    = "/gateway/manage/api/v1/ai-inside-algorithm/file-upload";
const std::string kDeleteAIInsideAlgorithmAPI        = "/gateway/manage/api/v1/ai-inside-algorithm/delete";

// PaaS ai-alert 告警配置与查询 API（/drc/paas/v2/ai-alert）
const std::string kAiAlertDeviceSettingAPI    = "/gateway/drc/paas/v2/ai-alert/device-setting";
const std::string kAiAlertAlgorithmSettingAPI = "/gateway/drc/paas/v2/ai-alert/algorithm-setting";
const std::string kAiAlertFlightsAPI          = "/gateway/drc/paas/v2/ai-alert/flights";
const std::string kAiAlertRecordsAPI          = "/gateway/drc/paas/v2/ai-alert/records";

// 直播转发 API（/manage/api/v1/paas/c2c/live-sync）
const std::string kLiveSyncBaseAPI      = "/gateway/manage/api/v1/paas/c2c/live-sync";
const std::string kLiveSyncByIdAPI      = "/gateway/manage/api/v1/paas/c2c/live-sync/{id}";
const std::string kLiveSyncListAPI      = "/gateway/manage/api/v1/paas/c2c/live-sync/list";
const std::string kLiveSyncDevicesV2API = "/gateway/manage/api/v1/paas/c2c/live-sync/devices-v2";
const std::string kLiveSyncSummaryAPI   = "/gateway/manage/api/v1/paas/c2c/live-sync/summary";


/// All the interface in this file are thread safe, you can call them in any thread
void get_device_list_by_filter(const std::string& gateway_model_key, int page_index, int page_size, uint32_t timeout_ms, const std::function<void(const DeviceTopoListResponse&, FlightHubError err)> callback);
void get_device_topo(const std::string& device_sn, const std::function<void(const DeviceTopoResponse&, FlightHubError err)> callback);
void start_live_stream(const LiveInfo& live_info, const std::function<void(const LiveStreamStartResponse&, FlightHubError err)> callback, const std::string& gateway_sn = "");
void stop_live_stream(const LiveInfo& live_info, const std::function<void(FlightHubError err)> callback, const std::string& gateway_sn = "");
void update_live_stream_state(const LiveInfo& live_info, int state, const std::function<void(FlightHubError err)> callback);
void update_live_stream_token(const LiveInfo& live_info, const std::string& user_id, const std::string& provider, const std::function<void(FlightHubError err, const std::string& token)>& callback);
void change_dock_camera_position(const LiveInfo& live_info, int camera_position, const std::string& gateway_sn, const std::function<void(FlightHubError err)>& callback);
void start_live_record(const LiveInfo& live_info, const std::string& gateway_sn, const std::string& time_zone, const std::function<void(FlightHubError err)>& callback);
void stop_live_record(const LiveInfo& live_info, const std::string& gateway_sn, const std::function<void(FlightHubError err)>& callback);
void get_device_live_records(const std::string& device_sn, const std::function<void(const std::vector<LiveRecordItem>& records, FlightHubError err)>& callback);
void post_audience_count(const std::vector<DeviceAudienceCountRequestItem>& device_list, const AudienceCountGroup* group, const std::function<void(const std::vector<DeviceAudienceCountItem>& result, FlightHubError err)>& callback);
// 航线任务 HTTP 层
void get_flight_task_list(const FlightTaskListParams& params, const std::function<void(FlightHubError err, const FlightTaskListResult& result)>& callback);
void get_command_task_list(const GetCommandTaskListParams& params, const std::function<void(FlightHubError err, const GetCommandTaskListResults& results)>& callback);
void get_flight_task_track(const GetFlightTaskTrackParams& params, const std::function<void(FlightHubError err, const GetFlightTaskTrackResults& results)>& callback);
void create_flight_task(const CreateTaskReq& req, const std::function<void(FlightHubError err, const CreateTaskResp& result)>& callback);
void get_flight_task_detail(const std::string& task_uuid, const std::function<void(FlightHubError err, const TaskDetail& result)>& callback);
void edit_flight_task(const std::string& task_uuid, const EditTaskReq& req, const std::function<void(FlightHubError err, const CreateTaskResp& result)>& callback);
void delete_flight_task(const std::string& task_uuid, const std::function<void(FlightHubError err)>& callback);
void update_flight_task_status(const std::string& task_uuid, int status, const std::function<void(FlightHubError err)>& callback);
void batch_update_flight_task_status(const BatchUpdateStatusReq& req, const std::function<void(FlightHubError err)>& callback);
void get_device_recommendation(const DeviceRecommendationReq& req, const std::function<void(FlightHubError err, const DeviceRecommendationResp& result)>& callback);
void get_device_tcas(const std::vector<std::string>& center_sn, const std::function<void(FlightHubError err, const GetTCAInfosResp& result)>& callback);
void update_device_tcas(const std::vector<std::string>& center_sn, const std::function<void(FlightHubError err)>& callback);
void start_roaming(const StartRoamingParams& params, const std::function<void(FlightHubError err)>& callback);
void set_roaming_station_sn(const SetRoamingStationSnParams& params,
                            const std::function<void(FlightHubError err, const SetRoamingStationSnResult& result)>& callback);
void get_in_flight_task_topos(const std::vector<std::string>& drone_sns,
                              const std::function<void(FlightHubError err, const GetInFlightTaskToposResult& result)>& callback);
void open_data_channel(OpenDataChannelRequest request, const std::string& sn_tag, const std::function<void(const OpenDataChannelResponse&, FlightHubError err)> callback);
void fly_to_point(FlyToPointRequest request, const std::function<void(const FlyToPointResponse& response, FlightHubError err)> callback);

// 空中下发航线
void create_in_flight_wayline_task(
    CreateInFlightWaylineTaskRequest request,
    const std::function<void(const CreateInFlightWaylineTaskResponse& response, FlightHubError err)>& callback);
void control_in_flight_wayline_task(
    ControlInFlightWaylineTaskRequest request,
    const std::function<void(FlightHubError err)>& callback);
void get_active_in_flight_wayline_task(
    const std::string& drone_sn,
    const std::function<void(const GetActiveInFlightWaylineTaskResponse& response, FlightHubError err)>& callback);
void poi_enter(POIEnterRequest request, const std::function<void(const POITaskResponse& response, FlightHubError err)> callback);
void poi_exit(POIExitRequest request, const std::function<void(FlightHubError err)> callback);
void poi_speed_update(POISpeedUpdateRequest request, const std::function<void(FlightHubError err)> callback);
void get_poi_task(GetPOITaskRequest request, const std::function<void(const POITaskResponse& response, FlightHubError err)> callback);
void acquire_ctrl_authority(AcquireCtrlAuthorityRequest request, const std::function<void(const AcquireCtrlAuthorityResponse& response, FlightHubError err)> callback);
void release_ctrl_authority(ReleaseCtrlAuthorityRequest request, const std::function<void(const ReleaseCtrlAuthorityResponse& response, FlightHubError err)> callback);

/// non-realtime-cmd
void look_at_target(const LookAtTargetRequest& request, const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback);
void flight_task_pause(const FlightTaskPauseRequest& request, const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback);
void flight_task_resume(const FlightTaskResumeRequest& request, const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback);
void return_home(const ReturnHomeRequest& request, const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback);
void return_home_cancel(const ReturnHomeCancelRequest& request, const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback);

/// psdk
void get_psdk_speaker_resource_list(const std::function<void(const PsdkSpeakerResourceListResponse& response, FlightHubError err)>& callback);
void create_psdk_speaker_resource(CreateSpeakerResourceRequest request, const std::function<void(const CreateSpeakerResourceResponse& response, FlightHubError err)>& callback);
void edit_psdk_speaker_resource(const std::string& resource_id, EditSpeakerResourceRequest request, const std::function<void(const EditSpeakerResourceResponse& response, FlightHubError err)>& callback);
void delete_psdk_speaker_resource(int resource_id, const std::function<void(FlightHubError err)>& callback);
void get_sts(const std::function<void(const STSInfo& response, FlightHubError err)>& callback);
void get_psdk_widget_resources(PsdkWidgetResourceRequest request, const std::function<void(const PsdkWidgetResourceListResponse& response, FlightHubError err)>& callback);

/// flash-configs（爆闪灯模板库，PAAS 无 project_id）
void get_flash_led_config_list(const std::function<void(const FlashLedConfigListHttpResponse& response, FlightHubError err)>& callback);
void create_flash_led_config(CreateFlashLedConfigRequest request, const std::function<void(const CreateFlashLedConfigResponse& response, FlightHubError err)>& callback);
void update_flash_led_config(const std::string& config_id, UpdateFlashLedConfigRequest request, const std::function<void(const UpdateFlashLedConfigResponse& response, FlightHubError err)>& callback);
void delete_flash_led_config(const std::string& config_id, const std::function<void(FlightHubError err)>& callback);

/// drone takeoff
void drone_takeoff(DroneTakeoffRequest request, const std::function<void(const DroneTakeoffResponse& response, FlightHubError err)>& callback);

/// generate try flight wayline (试飞航线生成)，路径参数为 project_id，由调用方传入
void generate_try_flight_wayline(const std::string& project_id, GenerateTryFlightWaylineRequest request, const std::function<void(const GenerateTryFlightWaylineResponseData& response, FlightHubError err)>& callback);

void create_test_flight_task(const std::string& project_id, CreateTestFlightTaskRequest request, const std::function<void(const CreateTestFlightTaskResponseData& response, FlightHubError err)>& callback);

/// 飞机星历下载;
void drone_download_ephemeris(DroneDownloadEphemerisRequest request, const std::function<void(FlightHubError err)>& callback);


/// drone_in_out_wayline_detect  (开始/暂停)进离场任务;
void drone_in_out_wayline_detect(DroneInOutWaylineDetectRequest request, const std::function<void(FlightHubError err)>& callback);

/// drone_in_out_wayline_flight_setting  进离场航线飞行设置;
void drone_in_out_wayline_flight_setting(DroneInOutWaylineFlightSettingRequest request, const std::function<void(FlightHubError err)>& callback);

/// get_scout_flight_setting  获取进离场航线飞行设置 (GET);
void get_scout_flight_setting(const std::string& sn, const std::function<void(const InOutWaylineFlightSettingParams& params, FlightHubError err)>& callback);

/// set_live_quality 直播画质设置;
void set_live_quality(LiveQualitySettingRequest request, const std::function<void(FlightHubError err)>& callback);


/// media file list
void get_media_file_list(const std::string& project_id, GetMediaFileListRequest request, const std::function<void(const MediaFileListResponse& response, FlightHubError err)>& callback);

/// dock supplement light
void set_dock_supplement_light_state(const std::string& device_sn, bool state, const std::function<void(FlightHubError err)>& callback);

/// dock charge control
void set_dock_charge_state(const std::string& device_sn, bool state, const std::function<void(FlightHubError err)>& callback);

/// dock cover control
void set_dock_cover_state(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback);

/// dock air conditioner control
void set_dock_air_conditioner_mode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback);

void post_open_debug_mode(const std::string& device_sn, const std::function<void(const OpenDebugModeResponse& data, FlightHubError err)>& callback);
void post_close_debug_mode(const std::string& device_sn, const std::function<void(const CloseDebugModeResponse& data, FlightHubError err)>& callback);
void post_set_distance_limit(const std::string& device_sn, int distance_limit_state, int distance_limit_value, const std::function<void(const SetDistanceLimitResponse& data, FlightHubError err)>& callback);void post_open_dock_cover(const std::string& device_sn, const std::function<void(const OpenDockCoverResponse& data, FlightHubError err)>& callback);
void post_set_height_limit(const std::string& device_sn, int height_limit, const std::function<void(const SetHeightLimitResponse& data, FlightHubError err)>& callback);
void post_set_obstacle_avoidance(const std::string& device_sn, int obstacle_avoidance_state, const std::function<void(const SetObstacleAvoidanceResponse& data, FlightHubError err)>& callback);
void post_set_remaining_power_for_rth(const std::string& device_sn, int remaining_power_for_rth, const std::function<void(const SetRemainingPowerForRthResponse& data, FlightHubError err)>& callback);
void post_set_night_lights_state(const std::string& device_sn, int night_lights_state, const std::function<void(const SetNightLightsStateResponse& data, FlightHubError err)>& callback);
void post_switch_alarm_state(const std::string& device_sn, int alarm_state, const std::function<void(const AlarmStateSwitchResponse& data, FlightHubError err)>& callback);
void post_esim_operator_switch(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, const std::function<void(const EsimOperatorSwitchResponse& data, FlightHubError err)>& callback);
void post_sim_slot_switch(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, const std::function<void(const SimSlotSwitchResponse& data, FlightHubError err)>& callback);
void post_cloud_environment_switch(CloudEnvironmentSwitchRequest& request, const std::function<void(FlightHubError err)>& callback);
void post_cloud_environment_switch_callback(CloudEnvironmentSwitchCallbackRequest& request, const std::function<void(FlightHubError err)>& callback);
void post_set_battery_store_mode(const std::string& device_sn, int battery_store_mode, const std::function<void(const SetBatteryStoreModeResponse& data, FlightHubError err)>& callback);
// 手动换电 / Dongle 电源 / 低功耗 / eSIM 运营商切换进度
void put_manual_battery_swap(const std::string& device_sn, int slot_id, const std::function<void(const ManualBatterySwapResponse& data, FlightHubError err)>& callback);
void get_manual_battery_swap(const std::string& device_sn, const std::function<void(const ManualBatterySwapProgressResponse& data, FlightHubError err)>& callback);
void post_dongle_power_switch(const std::string& device_sn, int power_state, const std::function<void(const DonglePowerSwitchResponse& data, FlightHubError err)>& callback);
void post_low_power_switch(const std::string& device_sn, int low_power_mode, const std::function<void(const LowPowerSwitchResponse& data, FlightHubError err)>& callback);
void get_esim_operator_switch(const std::string& device_sn, const std::function<void(const EsimOperatorSwitchProgressResponse& data, FlightHubError err)>& callback);
// Geo Caging 接口
void post_set_geo_caging_enable(const std::string& device_sn, int geo_caging_switch, const std::vector<GeoCagingInfo>& geo_caging_infos, const std::function<void(FlightHubError err)>& callback);
// 中继站调试接口
// 无 data 响应（broadcast_hide / ssid / password / search_mode）：callback 只有 FlightHubError
void post_relay_broadcast_hide(const std::string& device_sn, bool is_broadcast_hide, const std::function<void(FlightHubError err)>& callback);
void post_relay_ssid(const std::string& device_sn, const std::string& device_name, const std::function<void(FlightHubError err)>& callback);
void post_relay_password(const std::string& device_sn, const std::string& passwd, const std::function<void(FlightHubError err)>& callback);
void post_relay_search_mode(const std::string& device_sn, int relay_search_mode, const std::function<void(FlightHubError err)>& callback);
// 有 data 响应（occupied_status / reboot / disconnect）：callback 带 Response 结构体
void post_relay_occupied_status(const std::string& device_sn, int occupied_status, const std::function<void(const SetRelayOccupiedStatusResponse& data, FlightHubError err)>& callback);
void post_relay_reboot(const std::string& device_sn, const std::function<void(const RelayRebootResponse& data, FlightHubError err)>& callback);
void post_relay_disconnect(const std::string& device_sn, const std::function<void(const RelayDisconnectResponse& data, FlightHubError err)>& callback);
void get_open_dock_cover_progress(const std::string& device_sn, const std::function<void(const OpenDockCoverProgressResponse&, FlightHubError err)> callback);
void post_close_dock_cover(const std::string& device_sn, const std::function<void(const CloseDockCoverResponse& data, FlightHubError err)>& callback);
void get_close_dock_cover_progress(const std::string& device_sn, const std::function<void(const CloseDockCoverProgressResponse&, FlightHubError err)> callback);
void post_device_reboot(const std::string& device_sn, const std::function<void(const DeviceRebootResponse& data, FlightHubError err)>& callback);
void get_device_reboot_progress(const std::string& device_sn, const std::function<void(const DeviceRebootProgressResponse&, FlightHubError err)> callback);
void post_device_format(const std::string& device_sn, const std::function<void(const DeviceFormatResponse& data, FlightHubError err)>& callback);
void get_device_format_progress(const std::string& device_sn, const std::function<void(const DeviceFormatProgressResponse&, FlightHubError err)> callback);
void post_drone_open(const std::string& device_sn, const std::function<void(const DroneOpenResponse& data, FlightHubError err)>& callback);
void post_drone_close(const std::string& device_sn, const std::function<void(const DroneCloseResponse& data, FlightHubError err)>& callback);
void post_open_charge(const std::string& device_sn, const std::function<void(const OpenChargeResponse& data, FlightHubError err)>& callback);
void post_close_charge(const std::string& device_sn, const std::function<void(const CloseChargeResponse& data, FlightHubError err)>& callback);
void get_dock_status(const std::vector<std::string>& device_sns, const std::function<void(const DockStatusResponse& data, FlightHubError err)>& callback);
void get_drone_status(const std::vector<std::string>& device_sns, const std::function<void(const DroneStatusResponse& data, FlightHubError err)>& callback);
void post_dock_silent_mode(const std::string& device_sn, const int silent_mode, const std::function<void(const DockSilentModeResponse& data, FlightHubError err)>& callback);
void post_dock_air_conditioner_mode(const std::string& device_sn, const int mode, const std::function<void(const DockAirConditionerResponse& data, FlightHubError err)>& callback);

/// (设备)报修管理模块;
/// 获取设备报修配置;
void obtain_maintenance_orders_repair_config(
    ObtainRepairConfigRequest request,
    const std::function<void(const ObtainRepairConfigResponse& response, FlightHubError err)>& callback
);

/// (设备)报修管理模块;
/// 创建报修订单;
void create_maintenance_orders(
    CreateMaintenanceOrdersRequest request,
    const std::function<void(const CreateMaintenanceOrdersResponse& response, FlightHubError err)>& callback
);

/// (设备)报修管理模块;
/// 查询单个报修订单详情;
void get_maintenance_order_detail(
    const std::string &order_number,
    const std::function<void(const MaintenanceOrderDetailResponse& response, FlightHubError err)>& callback
);

/// (设备)报修管理模块;
/// 分页查询报修订单列表;
void get_maintenance_orders_list(
    const GetMaintenanceOrdersListRequest & request,
    const std::function<void(const MaintenanceOrdersListResponse& response, FlightHubError err)>& callback
);

/// 设备定损: 下发定损指令;
void post_self_diagnosis(
    CreateSelfDiagnosisRequest request,
    const std::function<void(FlightHubError err)>& callback
);

/// 定损操作(取消/删除);
void post_self_diagnosis_operation(
    SelfDiagnosisOperationRequest request,
    const std::function<void(FlightHubError err)>& callback
);


/// 设备定损: 查询单条定损报告详情;
void get_self_diagnosis_report(
    const std::string &report_uuid,
    const std::function<void(const GetSelfDiagnosisReportResponse& response, FlightHubError err)>& callback
);


/// 设备定损: 查询定损报告列表;
void get_self_diagnosis_report_lists(
    const GetSelfDiagnosisReportListParams & params,
    const std::function<void(const GetSelfDiagnosisReportListResponse& response, FlightHubError err)>& callback
);


/// AI Box: 获取 AI Box 信息;
void fetch_ai_box_info(
    const std::string & device_sn,
    const std::function<void(const AIBoxInfoListResponse& response, FlightHubError err)>& callback
);


/// AI Box: 获取文件下载 URL;
void fetch_file_download_url(
    FetchFileDownloadUrlRequest request,
    const std::function<void(const FileDownloadUrlListResponse& response, FlightHubError err)>& callback
);

///  AI Box: 获取应用列表;
void fetch_app_list(
    FetchAppListRequest request,
    const std::function<void(const FetchAppListResponse& response, FlightHubError err)>& callback
);


/// AI Box: 查询应用列表进度;
void fetch_app_list_progress(
    FetchAppListProgressRequest request,
    const std::function<void(const FetchAppListProgressResponse& response, FlightHubError err)>& callback
);


/// AI Box: 导出文件;
void export_file(
    ExportFileRequest request,
    const std::function<void(const ExportFileResponse& response, FlightHubError err)>& callback
);


/// AI Box: 查询文件导出进度;
void export_file_progress(
    ExportFileProgressRequest request,
    const std::function<void (const ExportFileProgressResponse &response, FlightHubError err)> &callback
);


/// AI Box: 安装应用;
void install_app(
    InstallAppRequest request,
    const std::function<void (const InstallAppResponse &response, FlightHubError err)> &callback
);


/// AI Box: 查询应用安装进度;
void install_app_progress(
    InstallAppProgressRequest request,
    const std::function<void (const InstallAppProgressResponse &response, FlightHubError err)> &callback
);


/// AI Box: 操作应用;
void operate_app(
    OperateAppRequest request,
    const std::function<void (const OperateAppResponse &response, FlightHubError err)> &callback
);


/// AI Box: 获取应用上传(STS凭证);
/// "/device/api/v1/service/aibox/upload-app-sts";
void get_upload_app_sts(
    const std::string &device_sn,
    const std::string &box_sn,
    const std::function<void(const GetUploadStsResponse& response, FlightHubError err)>& callback
);


/// Safety Services;
// [安全服务]: 信号地图, signal-maps/search, 信号地图查询;
void search_signal_maps(
    SearchSignalMapsRequest request,
    const std::function<void(const SignalMapsResponse& response, FlightHubError err)>& callback
);

// [自定义飞行区];
// 自定义飞行区: 创建[多边形、圆形] 限飞区(nfz) + 作业区(dfence);
// 自定义飞行区: 创建[折线形] 禁降区(noland);
void create_customized_flight_areas(
    CreateCustomizedFlightAreasRequest request,
    const std::function<void(const CreateFlightAreasResponse& response, FlightHubError err)>& callback
);

// [自定义飞行区];
// 自定义飞行区: 获取飞行区列表;
void get_customized_flight_areas(
    const std::string &name,             // 飞行区名称, 可选参数, 空值表示不使用该项过滤条件;
    const std::string &type,             // 飞行区类型, 可选参数, 空值表示不使用该项过滤条件, {"dfence": 自定义作业区; "nfz": 自定义限飞区; "noland": 自定义禁降区};
    const std::string &status,           // 飞行区状态, 可选参数, 空值表示不使用该项过滤条件, {"disable": 已禁用; "enable": 已启用};
    const std::vector<std::string> &ids,  // 飞行区id列表, 可选参数, 空值表示不使用该项过滤条件, 示例: ["48a1cd0b-346e-4a47-a1b6-0c335cbc2487"];
    int page_index,                      // 页码, 页下标(从1开始), 可选参数，传负数(表示不使用分页条件, 比如-1)则查询所有飞行区;
    int page_size,                       // 页大小(比如 100), 可选参数，传负数(表示不使用分页条件, 比如-1)则查询所有飞行区(page_index 和 page_size 一起使用);
    const std::function<void(const GetFlightAreasResponse& response, FlightHubError err)>& callback
);


// [自定义飞行区];
// 自定义飞行区: 更新飞行区;
void update_customized_flight_areas(
    const std::string &flight_area_id,    // 请求的 path 参数;
    UpdateCustomizedFlightAreasRequest request,
    const std::function<void(const UpdateFlightAreasResponse& response, FlightHubError err)>& callback
);


// [自定义飞行区];
// 自定义飞行区: 删除飞行区;
void delete_customized_flight_areas(
    const std::string &flight_area_id,    // 请求的 path 参数;
    const std::function<void(FlightHubError err)>& callback
);


// [自定义飞行区];
// 同步自定义飞行区;
void sync_customized_flight_areas(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
    const std::function<void(const SyncFlightAreasResponse& response, FlightHubError err)>& callback
);


// [自定义飞行区];
// 获取设备同步状态;
void get_flight_areas_sync_status(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
    const std::function<void(const GetAreasSyncStatusResponse& response, FlightHubError err)>& callback
);


// [地形障碍物];
// 查询离线地图模型信息;
void get_offline_maps_model_info(
    const std::function<void(const GetOfflineMapsModelInfoResponse& response, FlightHubError err)>& callback
);


// [地形障碍物];
// 创建/更新离线地图模型;
void update_offline_maps_model_info(
    UpdateOfflineMapsRequest request,
    const std::function<void(FlightHubError err)>& callback
);


// [地形障碍物];
// 取消离线地图生成;
void cancel_offline_maps_model_info(
    const std::function<void(FlightHubError err)>& callback
);


// [地形障碍物];
// 修改离线地图(开启/关闭);
void set_offline_maps(
    SetOfflineMapsRequest request,
    const std::function<void(FlightHubError err)>& callback
);


// [地形障碍物];
// 离线地图同步;
void sync_offline_maps(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN;
    const std::function<void(const OfflineMapsSyncResponse& response, FlightHubError err)>& callback
);


// [地形障碍物];
// 获取离线地图同步状态;
void get_offline_maps_sync_status(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN;
    const std::function<void(const GetOfflineMapsSyncStatusResponse& response, FlightHubError err)>& callback
);


/// Media Service
// [媒体服务]
// 获取媒体文件列表;
void get_media_files_list(
    const GetMediaFilesListParams &request_params,
    const std::function<void(const GetMediaFilesListResponse& response, FlightHubError err)>& callback
);


// [媒体服务]
// 批量删除媒体文件;
void delete_media_files(
    const std::vector<long long> &file_ids,    // 文件 id 列表;
    const std::function<void(FlightHubError err)>& callback
);


// [媒体服务]
// 获取媒体文件下载地址;
void get_media_files_download_url(
    long long file_id,    // 文件 id;
    const std::function<void(const GetMediaFilesDownloadUrlResponse& response, FlightHubError err)>& callback
);


// [媒体服务]
// 获取媒体文件下载地址;
void get_media_files_download_url(
    const std::vector<std::string> & object_keys,
    const std::function<void(const GetMediaFilesDownloadUrlListResponse& response, FlightHubError err)>& callback
);


// [ATC 空管服务]
// 导出飞行记录;
void export_flight_records(
    const std::string &user_id,                       // [必选参数] 用户 id, 比如 "1547828723498192896";
    const std::string &project_id,                    // [可选参数] 比如 "f111d837-7740-470b-9a44-e52fba516085", 默认传空字符串;
    ExportFlightRecordsRequest request,               // [必选参数] 请求 body 参数;
    const std::function<void(const ExportFlightRecordsResponse& response, FlightHubError err)>& callback // [必选参数] 回调函数;
);


// [ATC 空管服务]
// 查询导出进度;
void get_export_flight_records_progress(
    const std::string &project_id,                    // [可选参数] 比如 "f111d837-7740-470b-9a44-e52fba516085", 默认传空字符串;
    const GetExportFlightRecordsProgressParams &request_params,
    const std::function<void(const GetExportFlightRecordsProgressResponse& response, FlightHubError err)>& callback
);


void post_start_network_speed_test(const std::string& device_sn, const std::function<void(const StartNetworkSpeedTestResponse& data, FlightHubError err)>& callback);
void put_stop_network_speed_test(const std::string& device_sn, const std::function<void(const StopNetworkSpeedTestResponse& data, FlightHubError err)>& callback);
void get_network_speed_test_progress(const std::string& device_sn, const std::function<void(const NetworkSpeedTestProgressResponse& data, FlightHubError err)>& callback);
void post_rtk_calibration_by_network(NetworkRtkCalibrationRequest request, const std::function<void(const RtkCalibrationResponse& data, FlightHubError err)>& callback);
void post_rtk_calibration_by_manual(ManualRtkCalibrationRequest request, const std::function<void(const RtkCalibrationResponse& data, FlightHubError err)>& callback);
void post_drone_format(const std::string& device_sn, const std::function<void(const DroneFormatResponse& data, FlightHubError err)>& callback);
void post_drone_sdr_work_mode(const std::string& device_sn, const int work_mode, const std::function<void(const DroneSdrWorkModeResponse& data, FlightHubError err)>& callback);
void get_device_log_list(const std::string& device_sn, std::vector<int> module_list, const std::function<void(const DeviceLogListResponse& data, FlightHubError err)>& callback);
void post_create_device_log_report(CreateDeviceLogReportRequest& request, const std::function<void(const CreateDeviceLogReportResponse& data, FlightHubError err)>& callback);
void get_log_report_list(const std::string& device_sn, int page_index, int page_size, const std::function<void(const LogReportListResponse& data, FlightHubError err)>& callback);
void post_log_report_operation(const std::string& operation, const std::string& report_id, const std::function<void(FlightHubError err)>& callback);
void post_log_report_append_sts(const std::function<void(const LogReportAppendStsResponse& data, FlightHubError err)>& callback);
void post_history_hms(HistoryHmsDataRequest& request, const std::function<void(const HistoryHmsDataResponse& data, FlightHubError err)>& callback);
void post_unbind_device(UnBindDeviceRequest& device_sns, const std::function<void(FlightHubError err)>& callback);
void get_device_detail(const std::string& device_sn, const std::function<void(const DeviceDetailResponse& data, FlightHubError err)>& callback);
void put_modify_device_name(ModifyDeviceNameRequest& request, const std::function<void(FlightHubError err)>& callback);
void get_latest_firmware_release_note(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(const FirmwareReleaseNoteListResponse& data, FlightHubError err)> callback);
void post_invoke_firmware_upgrade(FirmwareUpgradeRequest& request, std::function<void(const FirmwareUpgradeResponse& data, FlightHubError err)> callback);
void get_query_firmware_upgrade_progress(const std::string& device_sn, std::function<void(const FirmwareUpgradeProgressResponse& data, FlightHubError err)> callback);
void post_firmware_upgrade_sts(std::function<void(const FirmwareUpgradeStsResponse& sts, FlightHubError err)> callback);
void put_release_firmware(const std::string& file_id, std::function<void(FlightHubError err)> callback);
void put_unrelease_firmware(const std::string& file_id, std::function<void(FlightHubError err)> callback);
void delete_delete_firmware(const std::string& file_id, std::function<void(FlightHubError err)> callback);
void get_query_firmware_list(int page, int page_size, std::function<void(const FirmwareListResponse& data, FlightHubError err)> callback);
void post_query_firmware_compatibility_config(std::function<void(const FirmwareCompatibilityConfigResponse& data, FlightHubError err)> callback);
void get_check_firmware_status(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(const FirmwareStatusListResponse& response, FlightHubError err)> callback);
void post_submit_firmware_info(FirmwareInfoRequest& request, std::function<void(FlightHubError err)> callback);
void get_unlock_license_list(QueryDeviceUnlockLicenseListRequest& request, std::function<void(const QueryDeviceUnlockLicenseListResponse& data, FlightHubError err)> callback);
void post_switch_unlock_license(SwitchDeviceUnlockLicenseRequest& request, std::function<void(FlightHubError err)> callback);
void post_check_unlock_license(CheckUnlockLicenseRequest& request, std::function<void(const CheckUnlockLicenseResponse& data, FlightHubError err)> callback);
void post_import_unlock_license(ImportUnlockLicenseRequest& request, std::function<void(FlightHubError err)> callback);
void post_import_unlock_license_sts(std::function<void(const GetImportUnlockLicenseStsResponse& data, FlightHubError err)> callback);
void post_clear_device_unlock_license(const std::string& device_sn, std::function<void(FlightHubError err)> callback);
void get_wayline_list(const GetWaylineListParam& param, const std::function<void(const GetWaylineListResponse& data, FlightHubError err)>& callback);
void get_wayline_detail(const std::string& wayline_uuid, const std::function<void(const GetWaylineDetailResponse& data, FlightHubError err)>& callback);
void post_batch_delete_waylines(const std::vector<std::string>& wayline_uuids, const std::function<void(const BatchDeleteWaylinesResponse& data, FlightHubError err)>& callback);
void post_update_wayline(const std::string& wayline_uuid, const std::string& wayline_name, const std::function<void(const UpdateWaylineResponse& data, FlightHubError err)>& callback);
void post_import_wayline_callback(const SubmitWaylineInfoParam& param, const std::function<void(const SubmitWaylineInfoResponse& data, FlightHubError err)>& callback);
void post_batch_download_waylines(const BatchDownloadWaylinesParam& param, const std::function<void(const BatchDownloadWaylinesResponse& data, FlightHubError err)>& callback);
void post_duplicate_wayline(const DuplicateWaylineParam& param, const std::function<void(const DuplicateWaylineResponse& data, FlightHubError err)>& callback);
void post_copy_wayline(const CopyWaylineParam& param, const std::function<void(const CopyWaylineResponse& data, FlightHubError err)>& callback);
void post_concat_wayline(const ConcatWaylineParam& param, const std::function<void(const ConcatWaylineResponse& data, FlightHubError err)>& callback);
void post_import_kmz(const std::vector<std::string>& file_paths, const std::function<void(const ImportKmzResponse& data, FlightHubError err)>& callback);

// AI Inside 机载算法管理 HTTP 函数声明
void get_ai_inside_models(const std::function<void(const GetAIInsideModelsResponse& data, FlightHubError err)>& callback);
// GET 接口无 body，直接接收 Param 拼接 URL
void get_ai_inside_model_devices(const GetAIInsideModelDevicesParam& param, const std::function<void(const GetAIInsideModelDevicesResponse& data, FlightHubError err)>& callback);
void get_ai_inside_model_devices_summary(const GetAIInsideModelDevicesSummaryParam& param, const std::function<void(const GetAIInsideModelDevicesSummaryResponse& data, FlightHubError err)>& callback);
// POST/DELETE 接口有 body，与项目其他接口保持一致：声明接收 Request 序列化结构体，signed_id 单独传入用于构造路径
void post_operate_ai_inside_model(const std::string& signed_id, OperateAIInsideModelRequest& request, const std::function<void(const OperateAIInsideModelResponse& data, FlightHubError err)>& callback);
void post_create_ai_inside_algorithm(CreateAIInsideAlgorithmRequest& request, const std::function<void(const CreateAIInsideAlgorithmResponse& data, FlightHubError err)>& callback);
void post_file_upload_ai_inside_algorithm(FileUploadAIInsideAlgorithmRequest& request, const std::function<void(const FileUploadAIInsideAlgorithmResponse& data, FlightHubError err)>& callback);
void delete_ai_inside_algorithm(DeleteAIInsideAlgorithmRequest& request, const std::function<void(FlightHubError err)>& callback);

// PaaS ai-alert 告警配置与查询 HTTP 函数声明
void post_ai_alert_device_setting(PostAiAlertDeviceSettingRequest& request, const std::function<void(const PostAiAlertDeviceSettingResponse& data, FlightHubError err)>& callback);
void get_ai_alert_device_setting(const GetAiAlertDeviceSettingParam& param, const std::function<void(const GetAiAlertDeviceSettingResponse& data, FlightHubError err)>& callback);
void post_ai_alert_algorithm_setting(PostAiAlertAlgorithmSettingRequest& request, const std::function<void(const PostAiAlertAlgorithmSettingResponse& data, FlightHubError err)>& callback);
void get_ai_alert_algorithm_setting(const GetAiAlertAlgorithmSettingParam& param, const std::function<void(const GetAiAlertAlgorithmSettingResponse& data, FlightHubError err)>& callback);
void get_ai_alert_flights(const GetAiAlertFlightsParam& param, const std::function<void(const GetAiAlertFlightsResponse& data, FlightHubError err)>& callback);
void get_ai_alert_records(const GetAiAlertRecordsParam& param, const std::function<void(const GetAiAlertRecordsResponse& data, FlightHubError err)>& callback);

// 直播转发
void post_live_sync_create(const LiveSyncCreateRequest& request, const std::function<void(const LiveSyncCreateResponse& data, FlightHubError err)>& callback);
void delete_live_sync(const std::string& id, const std::function<void(FlightHubError err)>& callback);
void put_live_sync_update(const std::string& id, const std::string& body, const std::function<void(const LiveSyncUpdateResponse& data, FlightHubError err)>& callback);
void get_live_sync_list(const LiveSyncListParams& params, const std::function<void(const LiveSyncListResponse& data, FlightHubError err)>& callback);
void get_live_sync_devices_v2(const std::function<void(const LiveSyncDevicesV2Response& data, FlightHubError err)>& callback);
void get_live_sync_summary(const std::function<void(const LiveSyncSummaryResponse& data, FlightHubError err)>& callback);


void set_flight_smart_plan_mode(
    
    FlightSmartPlanModeRequest request, 
    const std::function<void(const FlightSmartPlanModeApiResponse& response, FlightHubError err)>& callback
);

void set_flight_out_of_control_action(
    FlightOutOfControlActionRequest request,
    const std::function<void(const FlightOutOfControlActionApiResponse& response, FlightHubError err)>& callback
);
//飞行作业高
void set_flight_task_height(
    FlightCommanderHeightRequest request,
    const std::function<void(const FlightTaskHeightApiResponse& response, FlightHubError err)>& callback
);

void set_rth_altitude(
    FlightRthAltitudeRequest request,
    const std::function<void(const FlightRthAltitudeApiResponse& response, FlightHubError err)>& callback
);

void set_air_transfer_enable(
    AirTransferEnableRequest request,
    const std::function<void(FlightHubError err)>& callback
);

void update_fly_to_points(
    UpdateFlyToPointsRequest request,
    const std::function<void(FlightHubError err)>& callback
);

// === Terra Build GET 接口 ===

void get_reconstruction_settings(
    const std::string& file_uuid,
    const std::function<void(const ReconstructionSettingsResponseData& data, FlightHubError err)>& callback
);

void get_quality_report(
    const std::string& file_uuid,
    const GetQualityReportRequest& request,
    const std::function<void(const QualityReportResponseData& data, FlightHubError err)>& callback
);

void get_model_file_list(
    const GetModelFileListRequest& request,
    const std::function<void(const ModelFileListResponseData& data, FlightHubError err)>& callback
);

void get_model_file_url(
    int file_id,
    const GetModelFileUrlRequest& request,
    const std::function<void(const ModelFileUrlResponseData& data, FlightHubError err)>& callback
);

// === Terra Build POST/DELETE/GET 扩展接口 ===

void create_reconstruction(
    const std::string& body,
    const std::function<void(const CreateReconstructionResponseData& data, FlightHubError err)>& callback
);

void retry_reconstruction(
    const RetryReconstructionRequest& request,
    const std::function<void(FlightHubError err)>& callback
);

void stop_reconstruction(
    const StopReconstructionRequest& request,
    const std::function<void(FlightHubError err)>& callback
);

void get_sfm_result(
    const std::string& file_uuid,
    const std::function<void(const SfmResultResponseData& data, FlightHubError err)>& callback
);

void delete_model_file(
    int file_id,
    const std::function<void(FlightHubError err)>& callback
);

void batch_delete_model_files(
    const DeleteModelFilesRequest& request,
    const std::function<void(FlightHubError err)>& callback
);

void create_panorama(
    const CreatePanoramaRequest& request,
    const std::function<void(const CreatePanoramaResponseData& data, FlightHubError err)>& callback
);

void get_reconstruction_images(
    const std::string& file_uuid,
    const std::function<void(const ReconstructionImagesResponseData& data, FlightHubError err)>& callback
);

// 媒体上传相关接口（独立路径，不依赖 project_id）

/// Terra 模型管理新增接口

/// 模型文件上传回调（POST /pf-terra/api/v1/models/files/upload-callback）
void terra_upload_callback(
    const TerraUploadCallbackRequest& request,
    const std::function<void(const TerraUploadCallbackResponseData& data, FlightHubError err)>& callback
);

/// 模型重命名（PUT /model/api/v1/files/{id}）
void terra_rename_model(
    int file_id,
    const TerraRenameModelRequest& request,
    const std::function<void(FlightHubError err)>& callback
);

/// 批量加载/取消加载模型到地图（PUT /model/api/v1/files）
void terra_show_on_map(
    const TerraShowOnMapRequest& request,
    const std::function<void(const TerraShowOnMapResponseData& data, FlightHubError err)>& callback
);

/// 获取模型文件详情（GET /pf-terra/api/v1/models/files/{file_id}）
void terra_get_model_detail(
    int file_id,
    const std::function<void(const TerraModelDetailResponseData& data, FlightHubError err)>& callback
);

/// 设置 LAS 模型坐标系并触发格式转换（POST /pf-terra/api/v1/models/converters）
void terra_set_las_wkt(
    const std::string& file_uuid,
    const std::string& wkt,
    const std::function<void(FlightHubError err)>& callback
);

// 媒体上传相关接口（独立路径，不依赖 project_id）

/// 获取媒体上传 STS 凭证（POST /pf-media/inner/v1/media-files/sts）
void get_media_upload_sts(
    const GetMediaUploadStsRequest& request,
    const std::function<void(const GetMediaUploadStsResponseData& data, FlightHubError err)>& callback
);

/// 媒体上传完成回调（POST /pf-media/api/v1/upload-callback）
void media_upload_callback(
    const MediaUploadCallbackRequest& request,
    const std::function<void(const MediaUploadCallbackResponseData& data, FlightHubError err)>& callback
);

/// 上传地理标注媒体资源（POST /pf-media/api/v1/geo-media-file）
void upload_geo_media_file(
    const UploadGeoMediaFileRequest& request,
    const std::function<void(const UploadGeoMediaFileResponseData& data, FlightHubError err)>& callback
);

/// 根据文件 ID 获取媒体文件详情（GET /pf-media/api/v1/media-files/{file_id}）
void get_media_file_detail_by_id(
    int file_id,
    const std::function<void(const GetMediaFileDetailByIdResponseData& data, FlightHubError err)>& callback
);

/// 获取航线编辑器 STS 凭证（POST /gateway/pf-wayline/editor/v1/sts）
void get_wayline_editor_sts(
    const GetWaylineEditorStsRequest& request,
    const std::function<void(const GetWaylineEditorStsResponseData& data, FlightHubError err)>& callback
);

// ── 元素管理（ElementService）HTTP 接口 ──────────────────────────────────────

/// 创建要素（POST /pf-map/api/v1/elements/create）
void element_create(
    const CreateElementRequest& request,
    const std::function<void(const ElementCreateResponseData& data, FlightHubError err)>& callback
);

/// 更新要素（PUT /pf-map/api/v1/elements/update/{id}）
void element_update(
    const std::string& element_id,
    const UpdateElementRequest& request,
    const std::function<void(const ElementUpdateResponseData& data, FlightHubError err)>& callback
);

/// 查询元素层级列表（GET /pf-map/api/v1/elements/lists）
/// 查询元素层级列表（GET /pf-map/api/v1/elements/lists）
void element_list_hierarchy(
    const ListElementHierarchyRequest& request,
    const std::function<void(const ElementListHierarchyResponseData& data, FlightHubError err)>& callback
);

/// 设置要素显示状态（PUT /pf-map/api/v1/elements/{id}）
void element_set_visibility(
    const std::string& element_id,
    const SetElementVisibilityRequest& request,
    const std::function<void(const ElementSetVisibilityResponseData& data, FlightHubError err)>& callback
);

/// 删除要素（DELETE /pf-map/api/v1/elements/delete/{id}）
void element_delete(
    const std::string& element_id,
    bool from_batch_delete,
    const std::function<void(const ElementDeleteResponseData& data, FlightHubError err)>& callback
);

// ── 数据埋点（BeaconService）HTTP 接口 ──────────────────────────────────────

/// 上报埋点事件（POST /pf-beacon/ingest/inner/v1/report，平台内部接口）
/// 服务端只回 code/message，无 data；callback 可为空（纯埋点调用方通常不关心结果）
void report_beacon_event(
    const BeaconReportRequest& request,
    const std::function<void(FlightHubError err)>& callback
);

/// 埋点上报便捷封装：按统一口径填好 type/timestamp/payload（err 的全部字段）后异步上报。
/// 上报失败不影响主流程，故不关心结果，仅打印 log 观察。
/// @param event   事件标识，取 kBeaconReportEvent_* 常量，如 kBeaconReportEvent_TAKE_OFF
/// @param err     业务调用结果，全部字段转字符串写入 payload
/// @param log_tag 日志前缀，如 "[TakeoffModuleImpl]"；为空时不加前缀
void report_beacon_event_helper(
    std::string_view event, const FlightHubError& err, std::string_view log_tag = ""
);

}  // namespace flight_hub_sdk
