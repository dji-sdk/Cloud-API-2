/* flight_hub_sdk.i */

%module(directors="1") flighthubsdkmodule
%javaconst(1);

// Handle enums
%include "enums.swg"
// %include "enumtypeunsafe.swg"

%javaconst(1);

// Enable C++11 features and directors for cross-language polymorphism
%feature("c++11");
%feature("director");

%{
#include "flight_hub_sdk.h"
#include "workspace.h"
#include "element.h"
#include "drone.h"
#include "dock.h"
#include "remote_controller.h"
#include "base_station.h"
#include "relay.h"
#include "relay_control_module.h"
#include "rtk_calibration_module.h"
#include "network_test_module.h"
#include "device_debug_module.h"
#include "ai_box_module.h"
#include "device_state_module.h"
#include "device_hms_module.h"
#include "live_device_module.h"
#include "takeoff_module.h"
#include "deploy_module.h"
#include "dock_control_module.h"
#include "drc_module.h"
#include "flight_settings_module.h"
#include "test_flight_module.h"
#include "onboard_ai_module.h"
#include "psdk_control_module.h"
#include "camera_control_module.h"
#include "drone_osd_module.h"
#include "dock_osd_module.h"
#include "live_stream.h"
#include "live_record_module.h"
#include "flight_task_module.h"
#include "fly_to_point.h"
#include "poi_module.h"
#include "custom_sei.h"
#include "stream.h"
#include "message.h"
#include "value.h"
#include "maintenance_management_module.h"
#include "diagnose_management_module.h"
#include <memory>

using namespace flight_hub_sdk;
%}

// Standard library support
%include "std_string.i"
%include "std_shared_ptr.i"
%include "std_vector.i"
%include "std_unordered_map.i"


// 类型映射：将C++的long类型映射到Java的long类型
%typemap(jstype) long "long"
%typemap(jtype) long "long"
%typemap(jni) long "jlong"
%typemap(in) long %{ $1 = (long)$input; %}
%typemap(out) long %{ $result = (jlong)$1; %}
%typemap(argin) long %{ $1 = (long)$input; %}
%typemap(argout) long %{ $input = (jlong)$1; %}
%typemap(freearg) long ""
%typemap(directorout) long %{ $result = (long)$input; %}
%typemap(directorin) long %{ $input = (jlong)$1; %}

// 对于long*类型的映射
%typemap(jstype) long* "long[]"
%typemap(jtype) long* "long[]"
%typemap(jni) long* "jlongArray"
%typemap(in) long* %{ $1 = (long*)JCALL2(GetLongArrayElements, jenv, $input, 0); %}
%typemap(out) long* %{ $result = 0; %}
%typemap(argin) long* %{ $1 = (long*)JCALL2(GetLongArrayElements, jenv, $input, 0); %}
%typemap(argout) long* %{ JCALL3(ReleaseLongArrayElements, jenv, $input, (jlong*)$1, 0); %}
%typemap(freearg) long* %{ JCALL3(ReleaseLongArrayElements, jenv, $input, (jlong*)$1, 0); %}
%typemap(directorout) long* %{ $result = (long*)$input; %}
%typemap(directorin) long* %{ $input = (jlong*)$1; %}

// Forward declare the enums and structs before template instantiation
// This ensures SWIG knows about these types when processing templates

// POITaskStatus：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.POITaskStatus，不生成 generated 枚举。
%ignore flight_hub_sdk::POITaskStatus;
// DroneLinkState：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.DroneLinkState，不生成 generated 枚举。
%ignore flight_hub_sdk::DroneLinkState;
// DroneModeCode：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.DroneModeCode，不生成 generated 枚举。
%ignore flight_hub_sdk::DroneModeCode;
// CameraMode：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.CameraMode，不生成 generated 枚举。
%ignore flight_hub_sdk::CameraMode;
// CameraExposureMode：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.CameraExposureMode。
%ignore flight_hub_sdk::CameraExposureMode;
// CameraExposureCompensation / CameraFocusMode：Java 使用 value 包手写枚举。
%ignore flight_hub_sdk::CameraExposureCompensation;
%ignore flight_hub_sdk::CameraFocusMode;
%ignore flight_hub_sdk::CameraFocusState;
%ignore flight_hub_sdk::CameraISO;
%ignore flight_hub_sdk::CameraShutterSpeed;
%ignore flight_hub_sdk::CameraAperture;
%ignore flight_hub_sdk::CameraLaserMeasureState;
%ignore flight_hub_sdk::CameraLensType;
%ignore flight_hub_sdk::IrGainMode;
%ignore flight_hub_sdk::PanoramaRunningState;
%ignore flight_hub_sdk::CameraPhotoState;
%ignore flight_hub_sdk::RecordState;
%ignore flight_hub_sdk::NightSceneMode;
%ignore flight_hub_sdk::PsdkType;
%ignore flight_hub_sdk::PsdkSubType;
%ignore flight_hub_sdk::MegaphoneWorkMode;
%ignore flight_hub_sdk::MegaphonePlayMode;
%ignore flight_hub_sdk::PSDKMegaphoneSystemState;
%ignore flight_hub_sdk::SearchLightWorkMode;
%ignore flight_hub_sdk::DockAirConditionerState;
%ignore flight_hub_sdk::SeiDataType;
%ignore flight_hub_sdk::DockCameraPosition;
%ignore flight_hub_sdk::MegaphoneTTSVoiceType;
%ignore flight_hub_sdk::MegaphoneTTSLanguageType;
%ignore flight_hub_sdk::OutOfControlAction;
%ignore flight_hub_sdk::TakeoffMode;
%ignore flight_hub_sdk::DockCoverMode;
%ignore flight_hub_sdk::PhotoFormat;
%ignore flight_hub_sdk::VisualExplorationStage;
%ignore flight_hub_sdk::VisualExplorationErrorCode;
%ignore flight_hub_sdk::Stage;
%ignore flight_hub_sdk::AIIdentifyMode;
%ignore flight_hub_sdk::AIIdentifyScoreMode;

%include "value.h"
%include "message.h"


// Instantiate templates for each class that will be held by a shared_ptr
%shared_ptr(flight_hub_sdk::IWorkspace);
%shared_ptr(flight_hub_sdk::IDrone);
%shared_ptr(flight_hub_sdk::IDock);
%shared_ptr(flight_hub_sdk::IRemoteController);
%shared_ptr(flight_hub_sdk::IBaseStation);
%shared_ptr(flight_hub_sdk::IRelay);
%shared_ptr(flight_hub_sdk::IRelayControlModule);
%shared_ptr(flight_hub_sdk::IRtkCalibrationModule);
%shared_ptr(flight_hub_sdk::INetworkTestModule);
%shared_ptr(flight_hub_sdk::IDeviceStateModule);
%shared_ptr(flight_hub_sdk::IDeviceHMSModule);
%shared_ptr(flight_hub_sdk::ILiveDeviceModule);
%shared_ptr(flight_hub_sdk::ITakeoffModule);
%shared_ptr(flight_hub_sdk::IFlightControlModule);
%shared_ptr(flight_hub_sdk::IControlAuthModule);
%shared_ptr(flight_hub_sdk::IDeployModule);
%shared_ptr(flight_hub_sdk::IDrcModule);
%shared_ptr(flight_hub_sdk::IFlightSettingsModule);
%shared_ptr(flight_hub_sdk::ITestFlightModule);
%shared_ptr(flight_hub_sdk::IOnboardAiModule);
%shared_ptr(flight_hub_sdk::IPsdkControlModule);
%shared_ptr(flight_hub_sdk::ICameraControlModule);
%shared_ptr(flight_hub_sdk::IDroneOsdModule);
%shared_ptr(flight_hub_sdk::IDockOsdModule);
%shared_ptr(flight_hub_sdk::IDockControlModule);
%shared_ptr(flight_hub_sdk::ILiveModule);
%shared_ptr(flight_hub_sdk::ILiveRecordModule);
%shared_ptr(flight_hub_sdk::IFlyToPointModule);
%shared_ptr(flight_hub_sdk::IInFlightWaylineModule);
%shared_ptr(flight_hub_sdk::IPoiModule);
%shared_ptr(flight_hub_sdk::IFlightTaskControlModule);
%shared_ptr(flight_hub_sdk::IStream);
%shared_ptr(flight_hub_sdk::ICustomSEIModule);
%shared_ptr(flight_hub_sdk::IPullStreamModule);
%shared_ptr(flight_hub_sdk::IDeviceDebugModule);
%shared_ptr(flight_hub_sdk::IAIBoxModule);
%shared_ptr(flight_hub_sdk::IFlightTaskModule);
%shared_ptr(flight_hub_sdk::IMediaServiceModule);
%shared_ptr(flight_hub_sdk::ITerraBuildModule);
%shared_ptr(flight_hub_sdk::ISafetyServiceModule);
%shared_ptr(flight_hub_sdk::IFlightTaskRecordModule);
%shared_ptr(flight_hub_sdk::IHMSModule);
%shared_ptr(flight_hub_sdk::ILogManagementModule);
%shared_ptr(flight_hub_sdk::IMaintenanceManagementModule);
%shared_ptr(flight_hub_sdk::IDiagnosisManagementModule);
%shared_ptr(flight_hub_sdk::IDeviceManageModule);
%shared_ptr(flight_hub_sdk::IFirmwareManageModule);
%shared_ptr(flight_hub_sdk::IPsdkResourceModule);
%shared_ptr(flight_hub_sdk::IFlashLedModule);
%shared_ptr(flight_hub_sdk::IWaylineManageModule);
%shared_ptr(flight_hub_sdk::IAIInsideManageModule);
%shared_ptr(flight_hub_sdk::IElementModule);
%shared_ptr(flight_hub_sdk::IAIAlertModule);
%shared_ptr(flight_hub_sdk::ILiveSyncModule);

// GetDeviceRepairConfigParams 含引用成员, 禁止 SWIG 生成默认构造;
// %nodefaultctor flight_hub_sdk::GetDeviceRepairConfigParams;

// Instantiate vector templates we need;
%template(HmsDataBasicsVector) std::vector<flight_hub_sdk::HmsDataBasics>;
%template(HmsDataItemVector) std::vector<flight_hub_sdk::HmsDataItem>;
%template(AircraftPayloadCameraInfoVector) std::vector<flight_hub_sdk::AircraftPayloadCameraInfo>;
%template(SmartTrackPointInfoVector) std::vector<flight_hub_sdk::SmartTrackPointInfo>;
%template(AircraftPayloadsOsdInfoVector) std::vector<flight_hub_sdk::AircraftPayloadsOsdInfo>;
%template(BatteryInfoVector) std::vector<flight_hub_sdk::BatteryInfo>;
%template(DeviceVector) std::vector<flight_hub_sdk::Device>;
%template(DeviceTopoVector) std::vector<flight_hub_sdk::DeviceTopo>;
%template(LiveInfoVector) std::vector<flight_hub_sdk::LiveInfo>;
%template(LiveRecordItemVector) std::vector<flight_hub_sdk::LiveRecordItem>;
%template(AiDetectionTargetVector) std::vector<flight_hub_sdk::AiDetectionTarget>;
%template(ByteVector) std::vector<unsigned char>;
%template(FlyToDroneSelfPlanPathPointVector) std::vector<flight_hub_sdk::FlyToDroneSelfPlanPathPoint>;
%template(PsdkDeviceInfoVector) std::vector<flight_hub_sdk::PsdkDeviceInfo>;
%template(PsdkSpeakerResourceVector) std::vector<flight_hub_sdk::PsdkSpeakerResource>;
%template(PsdkWidgetResourceVector) std::vector<flight_hub_sdk::PsdkWidgetResource>;
%template(RecommendedDeviceVector) std::vector<flight_hub_sdk::RecommendedDevice>;
%template(StringVector) std::vector<std::string>;
%template(DockStatusItemResultVector) std::vector<flight_hub_sdk::DockStatusItemResult>;
%template(DroneStatusItemResultVector) std::vector<flight_hub_sdk::DroneStatusItemResult>;
%template(DeviceAudienceCountRequestItemVector) std::vector<flight_hub_sdk::DeviceAudienceCountRequestItem>;
%template(DeviceAudienceCountItemVector) std::vector<flight_hub_sdk::DeviceAudienceCountItem>;
%template(TaskDetailVector) std::vector<flight_hub_sdk::TaskDetail>;
%template(CommandTaskListItemDataVector) std::vector<flight_hub_sdk::CommandTaskListItemData>;
%template(FlightTrackPointVector) std::vector<flight_hub_sdk::FlightTrackPoint>;
%template(TCAInfoVector) std::vector<flight_hub_sdk::TCAInfo>;
%template(RoamingStationSnResultVector) std::vector<flight_hub_sdk::RoamingStationSnResult>;
%template(InFlightTaskTopoItemVector) std::vector<flight_hub_sdk::InFlightTaskTopoItem>;
%template(DroneStatusBatteryItemResultVector) std::vector<flight_hub_sdk::DroneStatusBatteryItemResult>;
%template(FlightTaskExceptionDetailVector) std::vector<flight_hub_sdk::FlightTaskExceptionDetail>;
%template(DeviceChangeVector) std::vector<flight_hub_sdk::DeviceChange>;
%template(DockMaintainStatusVector) std::vector<flight_hub_sdk::DockMaintainStatus>;
%template(AircraftMaintainStatusVector) std::vector<flight_hub_sdk::AircraftMaintainStatus>;
%template(DockSubDeviceInfoVector) std::vector<flight_hub_sdk::DockSubDeviceInfo>;
%template(BatteryBasicInfoVector) std::vector<flight_hub_sdk::BatteryBasicInfo>;
%template(IntVector) std::vector<int>;
// B4 State push
%template(CameraSettableEventVector) std::vector<flight_hub_sdk::CameraSettableEvent>;
%template(CameraVisibleCapabilityItemVector) std::vector<flight_hub_sdk::CameraVisibleCapabilityItem>;
%template(CameraIsothermPolygonPointVector) std::vector<flight_hub_sdk::CameraIsothermPolygonPoint>;
// Terra Build 相关 vector 模板
%template(TerraBuildFolderInfoVector) std::vector<flight_hub_sdk::TerraBuildFolderInfo>;
%template(TerraBuildModelFileItemVector) std::vector<flight_hub_sdk::TerraBuildModelFileItem>;
%template(TerraBuildReconstructionImageItemVector) std::vector<flight_hub_sdk::TerraBuildReconstructionImageItem>;
%template(ReconstructionPolygonPointVector) std::vector<flight_hub_sdk::ReconstructionPolygonPoint>;
%template(TerraBuildJobInfoVector) std::vector<flight_hub_sdk::TerraBuildJobInfo>;
%template(TerraFileShowOnMapItemVector) std::vector<flight_hub_sdk::TerraFileShowOnMapItem>;
%template(TerraMediaFileShowOnMapItemVector) std::vector<flight_hub_sdk::TerraMediaFileShowOnMapItem>;
%template(TerraModelGeo2DPointVector) std::vector<flight_hub_sdk::TerraModelGeo2DPoint>;
%template(SeiItemVector) std::vector<flight_hub_sdk::SeiItem>;
%template(SeiPoint2DVector) std::vector<flight_hub_sdk::SeiPoint2D>;
%template(TasksMediaStatusVector) std::vector<flight_hub_sdk::TasksMediaStatus>;
%template(FlightTaskVector) std::vector<flight_hub_sdk::FlightTask>;
%template(ControlRightsCloudControlStatusItemVector) std::vector<flight_hub_sdk::ControlRightsCloudControlStatusItem>;
%template(ControlRightsDeviceControlStatusItemVector) std::vector<flight_hub_sdk::ControlRightsDeviceControlStatusItem>;
%template(DeviceLogListResultFileItemVector) std::vector<flight_hub_sdk::DeviceLogListResultFileItem>;
%template(DeviceLogListResultLogItemVector) std::vector<flight_hub_sdk::DeviceLogListResultLogItem>;
%template(DeviceLogReportDeviceTopoItemParamVector) std::vector<flight_hub_sdk::DeviceLogReportDeviceTopoItemParam>;
%template(DeviceLogReportDomainItemParamVector) std::vector<flight_hub_sdk::DeviceLogReportDomainItemParam>;
%template(DeviceLogReportLogItemParamVector) std::vector<flight_hub_sdk::DeviceLogReportLogItemParam>;
%template(DeviceLogReportLogListItemParamVector) std::vector<flight_hub_sdk::DeviceLogReportLogListItemParam>;
%template(HistoryHmsDataResultItemVector) std::vector<flight_hub_sdk::HistoryHmsDataResultItem>;
%template(LogReportListResultAppendixFileItemVector) std::vector<flight_hub_sdk::LogReportListResultAppendixFileItem>;
%template(LogReportListResultDeviceLogItemListVector) std::vector<flight_hub_sdk::LogReportListResultDeviceLogItemList>;
%template(LogReportListResultDeviceLogVector) std::vector<flight_hub_sdk::LogReportListResultDeviceLog>;
%template(LogReportListResultDeviceUploadFilesItemVector) std::vector<flight_hub_sdk::LogReportListResultDeviceUploadFilesItem>;
%template(LogReportListResultDeviceTopoItemVector) std::vector<flight_hub_sdk::LogReportListResultDeviceTopoItem>;
%template(LogReportListResultItemVector) std::vector<flight_hub_sdk::LogReportListResultItem>;
%template(ModifyDeviceNameItemVector) std::vector<flight_hub_sdk::ModifyDeviceNameItem>;
%template(SubDeviceInfoVector) std::vector<flight_hub_sdk::SubDeviceInfo>;
%template(HeaderKeyValueVector) std::vector<flight_hub_sdk::HeaderKeyValue>;
// EA340 消息推送补全（Phase D）新增 vector 模板;
%template(LiveStatusItemVector) std::vector<flight_hub_sdk::LiveStatusItem>;
%template(LiveCapacityVideoVector) std::vector<flight_hub_sdk::LiveCapacityVideo>;
%template(LiveCapacityCameraVector) std::vector<flight_hub_sdk::LiveCapacityCamera>;
%template(LiveCapacityDeviceVector) std::vector<flight_hub_sdk::LiveCapacityDevice>;
%template(EsimInfoItemVector) std::vector<flight_hub_sdk::EsimInfoItem>;
%template(DongleInfoItemVector) std::vector<flight_hub_sdk::DongleInfoItem>;
%template(PsdkUiResourceItemVector) std::vector<flight_hub_sdk::PsdkUiResourceItem>;
%template(AircraftPayloadGimbalAttitudeMap) std::unordered_map<std::string, flight_hub_sdk::AircraftPayloadGimbalAttitude>;
%template(AircraftPayloadGimbalAttitudeVector) std::vector<flight_hub_sdk::AircraftPayloadGimbalAttitude>;
%template(CameraLiveviewWorldRegionMap) std::unordered_map<std::string, flight_hub_sdk::LiveviewWorldRegion>;
%template(StringStringUnorderedMap) std::unordered_map<std::string, std::string>;
%template(ImportWaylineEventPointVector) std::vector<flight_hub_sdk::ImportWaylineEventPoint>;
%template(MediaFilesListItemInfoDataVector) std::vector<flight_hub_sdk::MediaFilesListItemInfoData>;
%template(MediaFileListItemVector) std::vector<flight_hub_sdk::MediaFileListItem>;
%template(MediaFileListAncestorVector) std::vector<flight_hub_sdk::MediaFileListAncestor>;
%template(MediaFileDownloadInfoVector) std::vector<flight_hub_sdk::MediaFileDownloadInfo>;
%template(LongLongVector) std::vector<long long>;
%template(FloatVector) std::vector<float>;
%template(DoubleVector) std::vector<double>;
%template(DoubleVectorVector) std::vector<std::vector<double>>;
%template(DoubleVectorVectorVector) std::vector<std::vector<std::vector<double>>>;

// 报修管理相关 vector 模板
%template(DeviceRepairConfigVector) std::vector<flight_hub_sdk::DeviceRepairConfig>;
%template(MaintenanceOrderDeviceItemParamsVector) std::vector<flight_hub_sdk::MaintenanceOrderDeviceItemParams>;
%template(MaintenanceOrderAttachmentsParamsVector) std::vector<flight_hub_sdk::MaintenanceOrderAttachmentsParams>;
%template(MaintenanceOrderLogInfoParamsVector) std::vector<flight_hub_sdk::MaintenanceOrderLogInfoParams>;
%template(MaintenanceOrderTopoNodeParamsVector) std::vector<flight_hub_sdk::MaintenanceOrderTopoNodeParams>;
%template(GeneratedOrderInfoResultVector) std::vector<flight_hub_sdk::GeneratedOrderInfoResult>;
%template(MaintenanceOrderDetailAttachmentsVector) std::vector<flight_hub_sdk::MaintenanceOrderDetailAttachments>;
%template(MaintenanceOrderStatusHistoryVector) std::vector<flight_hub_sdk::MaintenanceOrderStatusHistory>;
%template(MaintenanceOrdersListItemVector) std::vector<flight_hub_sdk::MaintenanceOrdersListItem>;
%template(DeviceSignalItemVector) std::vector<flight_hub_sdk::DeviceSignalItem>;
%template(GridSignalItemVector) std::vector<flight_hub_sdk::GridSignalItem>;
%template(FlightAreaInfoItemVector) std::vector<flight_hub_sdk::FlightAreaInfoItem>;
%template(DeviceSyncResultsVector) std::vector<flight_hub_sdk::DeviceSyncResults>;
%template(DeviceSyncStatusResultsVector) std::vector<flight_hub_sdk::DeviceSyncStatusResults>;
%template(ModelItemInfoDataVector) std::vector<flight_hub_sdk::ModelItemInfoData>;
%template(OfflineMapsModelInfoDataVector) std::vector<flight_hub_sdk::OfflineMapsModelInfoData>;
%template(FlightRecordExportProgressItemDataVector) std::vector<flight_hub_sdk::FlightRecordExportProgressItemData>;

// 固件管理相关 vector 模板
%template(FirmwareReleaseNoteResultVector) std::vector<flight_hub_sdk::FirmwareReleaseNoteResult>;
%template(FirmwareUpgradeDeviceItemVector) std::vector<flight_hub_sdk::FirmwareUpgradeDeviceItem>;
%template(FirmwareUpgradeResponseDeviceItemResultVector) std::vector<flight_hub_sdk::FirmwareUpgradeResponseDeviceItemResult>;
%template(FirmwareUpgradeProgressItemVector) std::vector<flight_hub_sdk::FirmwareUpgradeProgressItem>;
%template(FirmwareItemResultVector) std::vector<flight_hub_sdk::FirmwareItemResult>;
%template(FirmwareCompatibilityConfigItemResultVector) std::vector<flight_hub_sdk::FirmwareCompatibilityConfigItemResult>;
%template(FirmwareStatusItemVector) std::vector<flight_hub_sdk::FirmwareStatusItem>;
%template(FirmwareServiceDeviceVector) std::vector<flight_hub_sdk::FirmwareServiceDevice>;

// 解锁证书管理相关 vector 模板
%template(CheckUnlockLicenseResultItemVector) std::vector<flight_hub_sdk::CheckUnlockLicenseResultItem>;
%template(DeviceUnlockLicenseItemVector) std::vector<flight_hub_sdk::DeviceUnlockLicenseItem>;
%template(DeviceUnlockPolygonPointVector) std::vector<flight_hub_sdk::DeviceUnlockPolygonPoint>;
%template(IsothermPolygonPointVector) std::vector<flight_hub_sdk::IsothermPolygonPoint>;

// DRC 高级参数相关 vector 模板（AI 告警 / AI 模型配置 / 爆闪灯）
%template(AiAlarmTargetVector) std::vector<flight_hub_sdk::AiAlarmTarget>;
%template(AiAlarmStreamInputConfVector) std::vector<flight_hub_sdk::AiAlarmStreamInputConf>;
%template(AiModelsConfigSetVector) std::vector<flight_hub_sdk::AiModelsConfigSet>;
%template(AiModelsStatusItemVector) std::vector<flight_hub_sdk::AiModelsStatusItem>;
%template(AiModelsInfoItemVector) std::vector<flight_hub_sdk::AiModelsInfoItem>;
%template(FlashLedPatternInfoVector) std::vector<flight_hub_sdk::FlashLedPatternInfo>;
%template(FlashLedSegmentVector) std::vector<flight_hub_sdk::FlashLedSegment>;
%template(FlashLedConfigVector) std::vector<flight_hub_sdk::FlashLedConfig>;
// Geo Caging
%template(GeoCagingInfoParamVector) std::vector<flight_hub_sdk::GeoCagingInfoParam>;

// 云环境切换相关 vector 模板
%template(CloudEnvDeviceInfoVector) std::vector<flight_hub_sdk::CloudEnvDeviceInfo>;
%template(CloudEnvDeviceListVector) std::vector<flight_hub_sdk::CloudEnvDeviceList>;


// 航线管理相关 vector 模板
%template(WaylineListItemResultVector) std::vector<flight_hub_sdk::WaylineListItemResult>;
%template(BatchDownloadWaylinesItemResultVector) std::vector<flight_hub_sdk::BatchDownloadWaylinesItemResult>;
%template(WaylineDetailSurveyAreaScopePointVector) std::vector<flight_hub_sdk::WaylineDetailSurveyAreaScopePoint>;

// 飞行安全设置相关 vector 模板
%template(FlightSafeSyncFieldVector) std::vector<flight_hub_sdk::FlightSafeSyncField>;
%template(FlyToTargetPointVector) std::vector<flight_hub_sdk::FlyToTargetPoint>;

// AI Box 管理相关 vector 模板
%template(AIBoxInfoResultsVector) std::vector<flight_hub_sdk::AIBoxInfoResults>;
%template(AIBoxAppInfoResultsVector) std::vector<flight_hub_sdk::AIBoxAppInfoResults>;

// 诊断管理模块相关 vector 模板
%template(GetSelfDiagnosisReportResultsVector) std::vector<flight_hub_sdk::GetSelfDiagnosisReportResults>;
%template(DiagnosisMaterialItemResultVector) std::vector<flight_hub_sdk::DiagnosisMaterialItemResult>;
%template(DiagnosisErrorCodeItemResultVector) std::vector<flight_hub_sdk::DiagnosisErrorCodeItemResult>;
%template(DiagnosisDeviceProgressItemResultVector) std::vector<flight_hub_sdk::DiagnosisDeviceProgressItemResult>;
%template(SelfDiagnosisTopoNodeParamsVector) std::vector<flight_hub_sdk::SelfDiagnosisTopoNodeParams>;
%template(AiModelItemVector) std::vector<flight_hub_sdk::AiModelItem>;
%template(AiModelLabelVector) std::vector<flight_hub_sdk::AiModelLabel>;

// AI Inside 机载算法管理相关 vector 模板
%template(AIInsideModelItemVector)           std::vector<flight_hub_sdk::AIInsideModelItem>;
%template(AIInsideLabelVector)               std::vector<flight_hub_sdk::AIInsideLabel>;
%template(AIInsideDeviceItemVector)          std::vector<flight_hub_sdk::AIInsideDeviceItem>;
%template(AIInsideOperateDeviceResultVector) std::vector<flight_hub_sdk::AIInsideOperateDeviceResult>;

// AI 告警管理相关 vector / map 模板
%template(AiAlertAlarmTargetVector)      std::vector<flight_hub_sdk::AiAlertAlarmTarget>;
%template(AiAlertFlightItemVector)       std::vector<flight_hub_sdk::AiAlertFlightItem>;
%template(AiAlertTargetAlertInfoVector)  std::vector<flight_hub_sdk::AiAlertTargetAlertInfo>;
%template(AiAlertRecordItemVector)       std::vector<flight_hub_sdk::AiAlertRecordItem>;
%template(AiAlertRecordItemVectorMap)    std::unordered_map<std::string, std::vector<flight_hub_sdk::AiAlertRecordItem>>;

// 直播转发相关 vector 模板
%template(LiveSyncVideoCapacityVector)   std::vector<flight_hub_sdk::LiveSyncVideoCapacity>;
%template(LiveSyncCameraCapacityVector)  std::vector<flight_hub_sdk::LiveSyncCameraCapacity>;
%template(LiveSyncItemVector)            std::vector<flight_hub_sdk::LiveSyncItem>;
%template(LiveSyncDeviceV2Vector)        std::vector<flight_hub_sdk::LiveSyncDeviceV2>;
%template(LiveSyncDeviceItemV2Vector)    std::vector<flight_hub_sdk::LiveSyncDeviceItemV2>;

// ATC 任务事件相关 vector 模板
%template(FlyToWayPointVector) std::vector<flight_hub_sdk::FlyToWayPoint>;
%template(WaylineObstacleVector) std::vector<flight_hub_sdk::WaylineObstacle>;
%template(ImportKmzItemVector) std::vector<flight_hub_sdk::ImportKmzItem>;
%template(ConcatWaylineExtDataModelVector) std::vector<flight_hub_sdk::ConcatWaylineExtDataModel>;
%template(ConcatWaylineExtDataModelBoundingPointVector) std::vector<flight_hub_sdk::ConcatWaylineExtDataModelBoundingPoint>;


// Rename C++ entities for better Java API
%rename(destroy) ~flight_hub_sdk::IWorkspace;
%rename(destroy) ~flight_hub_sdk::IDrone;
%rename(destroy) ~flight_hub_sdk::IDock;
%rename(destroy) ~flight_hub_sdk::IRemoteController;
%rename(destroy) ~flight_hub_sdk::IBaseStation;
%rename(destroy) ~flight_hub_sdk::IRelay;
%rename(destroy) ~flight_hub_sdk::IRelayControlModule;
%rename(destroy) ~flight_hub_sdk::IRtkCalibrationModule;
%rename(destroy) ~flight_hub_sdk::INetworkTestModule;
%rename(destroy) ~flight_hub_sdk::IDeviceStateModule;
%rename(destroy) ~flight_hub_sdk::IDeviceHMSModule;
%rename(destroy) ~flight_hub_sdk::ILiveDeviceModule;
%rename(destroy) ~flight_hub_sdk::ILiveModule;
%rename(destroy) ~flight_hub_sdk::ILiveRecordModule;
%rename(destroy) ~flight_hub_sdk::IFlyToPointModule;
%rename(destroy) ~flight_hub_sdk::IFlightTaskControlModule;
%rename(destroy) ~flight_hub_sdk::IStream;
%rename(destroy) ~flight_hub_sdk::ICustomSEIModule;
%rename(destroy) ~flight_hub_sdk::IPullStreamModule;
%rename(destroy) ~flight_hub_sdk::IDeviceDebugModule;
%rename(destroy) ~flight_hub_sdk::IAIBoxModule;
%rename(destroy) ~flight_hub_sdk::IMediaServiceModule;
%rename(destroy) ~flight_hub_sdk::ISafetyServiceModule;
%rename(destroy) ~flight_hub_sdk::IFlightTaskRecordModule;
%rename(destroy) ~flight_hub_sdk::IHMSModule;
%rename(destroy) ~flight_hub_sdk::ILogManagementModule;
%rename(destroy) ~flight_hub_sdk::IMaintenanceManagementModule;
%rename(destroy) ~flight_hub_sdk::IDiagnosisManagementModule;
%rename(destroy) ~flight_hub_sdk::IDeviceManageModule;
%rename(destroy) ~flight_hub_sdk::IFirmwareManageModule;
%rename(destroy) ~flight_hub_sdk::IPsdkResourceModule;
%rename(destroy) ~flight_hub_sdk::IFlashLedModule;
%rename(destroy) ~flight_hub_sdk::IWaylineManageModule;


// Define callback interfaces for Java;
%feature("director") HmsDataCallback;
%feature("director") AircraftPayloadsOsdInfoListCallback;
%feature("director") AircraftGearCallback;
%feature("director") AircraftHomeDistanceCallback;
%feature("director") AircraftPositionStateCallback;
%feature("director") AircraftControlSourceCallback;
%feature("director") AircraftLimitsInfoCallback;
%feature("director") AircraftBatteryWarningThresholdsCallback;
%feature("director") AircraftFirmwareStatusCallback;
%feature("director") AircraftTotalFlightInfoCallback;
%feature("director") AircraftActivationTimeCallback;
%feature("director") AircraftMaintainStatusInfoCallback;
%feature("director") AircraftNightLightsStateCallback;
%feature("director") AircraftObstacleAvoidanceStateCallback;
%feature("director") AircraftStorageInfoCallback;
%feature("director") AircraftTrackIdCallback;
%feature("director") AircraftComplianceInfoCallback;
%feature("director") AircraftPayloadsCamerasListCallback;
%feature("director") AircraftRcLostActionCallback;
%feature("director") AircraftRthInfoCallback;
%feature("director") AircraftHomeGeographicalInfoCallback;
%feature("director") AircraftCurrentRthModeCallback;
%feature("director") DeviceListCallback;
%feature("director") GetDeviceDetailInfoCallback;
%feature("director") ModifyDeviceNameCallback;
%feature("director") LiveAvailableRangeCallback;
%feature("director") LiveStreamCallback;
%feature("director") AiSeiInfoCallback;
%feature("director") ArSeiInfoCallback;
%feature("director") FovSeiInfoCallback;
%feature("director") AssistantSeiInfoCallback;
%feature("director") EisSeiInfoCallback;
%feature("director") ResultCallback;
%feature("director") ResultCallbackWithError;
%feature("director") GetDeviceLiveRecordsCallback;
%feature("director") LiveRecordStateCallback;
%feature("director") GetAudienceCountCallback;
%feature("director") GetFlightTaskListCallback;
%feature("director") GetCommandTaskListCallback;
%feature("director") GetFlightTaskTrackCallback;
%feature("director") CreateFlightTaskCallback;
%feature("director") GetFlightTaskDetailCallback;
%feature("director") GetDeviceRecommendationCallback;
%feature("director") GetDeviceTcasCallback;
%feature("director") FlyToPointStartCallback;
%feature("director") FlyToDroneSelfPlanPathCallback;
%feature("director") FlyToPointProgressCallback;
%feature("director") PoiEnterCallback;
%feature("director") PoiExitCallback;
%feature("director") PoiUpdateSpeedCallback;
%feature("director") PoiGetTaskCallback;
%feature("director") AiDataInfoCallback;
%feature("director") AircraftBatteryInfoCallback;
%feature("director") AircraftAttitudeCallback;
%feature("director") AircraftLocationCallback;
%feature("director") AircraftModeCodeCallback;
%feature("director") AircraftWindSpeedCallback;
%feature("director") AircraftTakeoffProgressCallback;
%feature("director") TakeoffResultCallback;
%feature("director") CreateInFlightWaylineTaskCallback;
%feature("director") GetActiveInFlightWaylineTaskCallback;
%feature("director") InFlightWaylineProgressCallback;
%feature("director") FlightControlAuthorityCallback;
%feature("director") PayloadControlAuthorityCallback;
%feature("director") CameraStateCallback;
%feature("director") LiveviewWorldRegionCallback;
%feature("director") CameraWideLensStateCallback;
%feature("director") CameraZoomLensStateCallback;
%feature("director") CameraMeasureTargetCallback;
%feature("director") NightModeSettingsCallback;
%feature("director") DockSubDeviceInfoListCallback;
%feature("director") DockNetworkStateCallback;
%feature("director") GimbalStateCallback;
%feature("director") DroneOnlineStatusCallback;
%feature("director") LinkStatusCallback;
%feature("director") PanoramaStatusCallback;
%feature("director") IntervalTakePhotoStatusCallback;
%feature("director") CameraModeAvailableRangeCallback;
%feature("director") PsdkDeviceListCallback;
%feature("director") PsdkStateSummaryInfoCallback;
%feature("director") PsdkStateSpeakerInfoCallback;
%feature("director") PsdkStateSearchlightInfoCallback;
%feature("director") PsdkSpeakerPlayProgressInfoCallback;
%feature("director") PsdkFloatingWindowTextInfoCallback;
%feature("director") PsdkSpeakerResourceListCallback;
%feature("director") PsdkSpeakerResourceCallback;
%feature("director") STSInfoCallback;
%feature("director") PsdkWidgetResourceListCallback;

// 红外增益模式相关回调接口
%feature("director") LiveVideoInfoCallback;
%feature("director") ThermalGainModeInfoCallback;
%feature("director") CameraIrLensStateCallback;
%feature("director") CameraIrThermometryInfoCallback;
%feature("director") CameraWatermarkSettingsCallback;
%feature("director") CameraStorageInfoCallback;
%feature("director") CameraIrPipStateCallback;
%feature("director") CameraIsothermPolygonCallback;
%feature("director") CameraMediaStorageCallback;
%feature("director") CameraAlgorithmDehazeInfoCallback;
%feature("director") CameraPreRecordedInfoCallback;
%feature("director") CameraCapabilityCallback;
%feature("director") CameraSettableCapabilityCallback;
%feature("director") CameraVisibleCapabilityCallback;
%feature("director") CameraPhotoInfoCallback;

// 飞行任务异常和设备变更相关回调接口
%feature("director") FlightTaskExceptionCallback;
%feature("director") WorkspaceDeviceChangeCallback;
%feature("director") ImportWaylineEventCallback;
%feature("director") DeviceTopoChangeCallback;

%feature("director") AircraftControlCodeCallback;
%feature("director") AircraftSpeedCallback;
%feature("director") AircraftPayloadsCameraLiveviewWorldRegionCallback;
%feature("director") AircraftPayloadsGimbalAttitudeCallback;
%feature("director") AircraftPayloadsListCallback;
%feature("director") ControlRightsCloudControlStatusCallback;
%feature("director") ControlRightsDeviceControlStatusCallback;
%feature("director") FlightTasksMediaFileDisplayAvailableCallback;
%feature("director") CommandTaskEventCallback;
%feature("director") FlyToTaskEventCallback;
%feature("director") PoiTaskEventCallback;
%feature("director") WaylineTaskEventCallback;
%feature("director") DockLocationCallback;
%feature("director") DroneInDockCallback;
%feature("director") DockCoverStateCallback;
%feature("director") DockMediaFileDetailCallback;
%feature("director") DockWirelessLinkCallback;
%feature("director") DockJobNumberCallback;
%feature("director") FlightTaskStepCodeCallback;
%feature("director") DockPowerStateCallback;
%feature("director") DockStorageStateCallback;
%feature("director") DockFirstPowerOnTimeCallback;
%feature("director") DockAccTimeCallback;
%feature("director") DockModeCodeCallback;
%feature("director") DockDroneChargeStateCallback;
%feature("director") DockUserExperienceImprovementCallback;
%feature("director") DockDrcStateCallback;
%feature("director") DockPutterStateCallback;
%feature("director") DockAlternateLandPointCallback;
%feature("director") DockPositionStateCallback;
%feature("director") DockActivationTimeCallback;
%feature("director") DockBatteryStoreModeCallback;
%feature("director") DockAlarmStateCallback;
%feature("director") DockDroneBatteryMaintenanceInfoCallback;
%feature("director") DockEmergencyStopStateCallback;
%feature("director") DockSilentModeCallback;
%feature("director") DockRelativeAlternateLandPointCallback;
%feature("director") DockSelfConvergeCoordinateCallback;
%feature("director") DockFirmwareInfoCallback;
%feature("director") DockMeteorologicalInfoCallback;
%feature("director") DockBackupBatteryInfoCallback;
%feature("director") DockMaintainStatusInfoCallback;
%feature("director") DockOnlineStatusCallback;
%feature("director") DockAirConditionerStatusInfoCallback;
%feature("director") DockSupplementLightStateCallback;
%feature("director") PullStreamStartCallback;
%feature("director") PullStreamDataCallback;
%feature("director") PullStreamErrorCallback;
%feature("director") StartNetworkTestCallback;
%feature("director") StopNetworkTestCallback;
%feature("director") GetNetworkTestProgressCallback;
%feature("director") SubscribeNetworkTestProgressCallback;
%feature("director") SubscribeCalibrationProgressCallback;
%feature("director") RtkCalibrationCallback;
%feature("director") SetDistanceLimitCallback;
%feature("director") SetHeightLimitCallback;
%feature("director") SetObstacleAvoidanceCallback;
%feature("director") SetRemainingPowerForRthCallback;
%feature("director") SetNightLightsStateCallback;
%feature("director") SwitchAlarmStateCallback;
%feature("director") EsimOperatorSwitchCallback;
%feature("director") SimSlotSwitchCallback;
%feature("director") SetBatteryStoreModeCallback;
%feature("director") ManualBatterySwapCallback;
%feature("director") ManualBatterySwapProgressCallback;
%feature("director") DonglePowerSwitchCallback;
%feature("director") LowPowerSwitchCallback;
%feature("director") EsimOperatorSwitchProgressCallback;
%feature("director") DeviceCloudEnvironmentSwitchProgressCallback;
// 中继站调试接口回调
%feature("director") SetRelayBroadcastHideCallback;
// Geo Caging 回调
%feature("director") SetGeoCagingEnableCallback;
%feature("director") SetRelaySSIDCallback;
%feature("director") SetRelayPasswordCallback;
%feature("director") SetRelaySearchModeCallback;
%feature("director") SetRelayOccupiedStatusCallback;
%feature("director") RelayRebootCallback;
%feature("director") RelayDisconnectCallback;
%feature("director") QueryHistoryHMSDataCallback;
%feature("director") QueryDeviceLogListCallback;
%feature("director") CreateDeviceLogReportCallback;
%feature("director") LogReportListCallback;
%feature("director") LogReportOperationCallback;
%feature("director") UploadLogAppendFileCallback;

// 固件管理相关回调接口
%feature("director") GetLatestFirmwareReleaseNoteCallback;
%feature("director") InvokeFirmwareUpgradeCallback;
%feature("director") QueryFirmwareUpgradeProgressCallback;
%feature("director") UploadFirmwareFileCallback;
%feature("director") ReleaseFirmwareCallback;
%feature("director") UnReleaseFirmwareCallback;
%feature("director") DeleteFirmwareCallback;
%feature("director") QueryFirmwareListCallback;
%feature("director") QueryFirmwareCompatibilityConfigCallback;
%feature("director") CheckFirmwareStatusCallback;
%feature("director") SubmitFirmwareInfoCallback;

// 解锁证书管理相关回调接口
%feature("director") GetUnlockLicenseListCallback;
%feature("director") SwitchUnlockLicenseCallback;
%feature("director") CheckUnlockLicenseCallback;
%feature("director") SubmitUnlockLicenseCallback;
%feature("director") ClearDeviceUnlockLicenseCallback;
%feature("director") UploadUnlockLicenseFileCallback;

// 航线管理相关回调接口
%feature("director") GetWaylineListCallback;
%feature("director") GetWaylineDetailCallback;
%feature("director") BatchDeleteWaylinesCallback;
%feature("director") UpdateWaylineCallback;
%feature("director") SubmitWaylineInfoCallback;
%feature("director") BatchDownloadWaylinesCallback;
%feature("director") DuplicateWaylineCallback;
%feature("director") CopyWaylineCallback;
%feature("director") ConcatWaylineCallback;
%feature("director") ImportKmzCallback;
// AI Inside 机载算法管理相关回调接口
%feature("director") GetAIInsideModelsCallback;
%feature("director") GetAIInsideModelDevicesCallback;
%feature("director") GetAIInsideModelDevicesSummaryCallback;
%feature("director") OperateAIInsideModelCallback;
%feature("director") CreateAIInsideAlgorithmCallback;
%feature("director") FileUploadAIInsideAlgorithmCallback;
%feature("director") DeleteAIInsideAlgorithmCallback;
%feature("director") UploadAlgorithmFileCallback;
// AI 告警管理相关回调接口
%feature("director") PostAiAlertDeviceSettingCallback;
%feature("director") GetAiAlertDeviceSettingCallback;
%feature("director") PostAiAlertAlgorithmSettingCallback;
%feature("director") GetAiAlertAlgorithmSettingCallback;
%feature("director") GetAiAlertFlightsCallback;
%feature("director") GetAiAlertRecordsCallback;

// 直播转发回调
%feature("director") CreateLiveSyncCallback;
%feature("director") DeleteLiveSyncCallback;
%feature("director") UpdateLiveSyncCallback;
%feature("director") GetLiveSyncListCallback;
%feature("director") GetLiveSyncDevicesV2Callback;
%feature("director") GetLiveSyncSummaryCallback;
// 媒体服务模块相关回调接口
%feature("director") GetMediaFilesListCallback;
%feature("director") GetMediaFilesDownloadUrlCallback;
%feature("director") GetMediaFilesDownloadUrlListCallback;
%feature("director") MediaFilePostProcessCallback;
%feature("director") MediaFileDeviceUploadCallback;
%feature("director") GetMediaUploadStsCallback;
%feature("director") MediaUploadCallbackCallback;
%feature("director") UploadGeoMediaFileCallback;
%feature("director") GetMediaFileDetailByIdCallback;
%feature("director") GetWaylineEditorStsCallback;
// Terra Build 模块相关回调接口
%feature("director") TerraBuildReconstructionSettingsCallback;
%feature("director") TerraBuildQualityReportCallback;
%feature("director") TerraBuildModelFileListCallback;
%feature("director") TerraBuildModelFileUrlCallback;
%feature("director") TerraBuildCreateReconstructionCallback;
%feature("director") TerraBuildSfmResultCallback;
%feature("director") TerraBuildCreatePanoramaCallback;
%feature("director") TerraBuildReconstructionImagesCallback;
%feature("director") TerraBuildReconstructionStatusChangedCallback;
// Element 模块回调
%feature("director") ElementCreateCallback;
%feature("director") ElementUpdateCallback;
%feature("director") ElementDeleteCallback;
%feature("director") ElementCreateElementCallback;
%feature("director") ElementUpdateElementCallback;
%feature("director") ElementListHierarchyCallback;
%feature("director") ElementSetVisibilityCallback;
%feature("director") ElementDeleteElementCallback;
%feature("director") TerraUploadCallbackCallback;
%feature("director") TerraShowOnMapCallback;
%feature("director") TerraUnzipProgressCallback;
%feature("director") TerraModelStatusChangedCallback;
%feature("director") TerraFileShowOnMapCallback;
%feature("director") TerraMediaFileHideOnMapCallback;
%feature("director") TerraMediaFileShowOnMapCallback;
%feature("director") TerraGetModelDetailCallback;
%feature("director") TerraSetLasWktCallback;

// 安全服务模块相关回调接口
%feature("director") SearchSignalMapsCallback;
%feature("director") CreateCustomizedFlightAreasCallback;
%feature("director") GetCustomizedFlightAreasCallback;
%feature("director") UpdateCustomizedFlightAreasCallback;
%feature("director") SyncCustomizedFlightAreasCallback;
%feature("director") GetFlightAreasSyncStatusCallback;
%feature("director") GetOfflineMapsModelInfoCallback;
%feature("director") SyncOfflineMapsCallback;
%feature("director") GetOfflineMapsSyncStatusCallback;

// 飞行任务记录模块相关回调接口
%feature("director") ExportFlightRecordsCallback;
%feature("director") GetExportFlightRecordsProgressCallback;

// 飞行器 HSI 信息（障碍物感知/避障传感器数据）
%feature("director") HsiInfoCallback;

// 新增订阅进度回调接口
%feature("director") DeviceOtaProgressCallback;
%feature("director") DeviceFileUploadProgressCallback;
%feature("director") PsdkSpeakerAudioPlayStartProgressCallback;
%feature("director") PsdkSpeakerTtsPlayStartProgressCallback;
%feature("director") FlightPoiProgressCallback;

// 飞行安全设置回调
%feature("director") SetFlightSmartPlanModeCallback;
%feature("director") SetOutOfControlActionCallback;
%feature("director") SetFlightTaskHeightCallback;
%feature("director") SetRthAltitudeCallback;
%feature("director") SetAirTransferEnableCallback;
%feature("director") GenerateTryFlightWaylineCallback;
%feature("director") CreateTestFlightTaskCallback;

// 报修管理模块相关回调接口
%feature("director") GetRepairConfigsCallback;
%feature("director") CreateMaintenanceOrderCallback;
%feature("director") GetMaintenanceOrderDetailCallback;
%feature("director") GetMaintenanceOrdersListCallback;

// 诊断管理模块相关回调接口
%feature("director") CreateSelfDiagnosisCallback;
%feature("director") OperateSelfDiagnosisCallback;
%feature("director") GetSelfDiagnosisReportCallback;
%feature("director") GetSelfDiagnosisReportListCallback;

// AI Box 管理相关回调接口
%feature("director") FetchAIBoxInfoCallback;
%feature("director") FetchFileDownloadUrlCallback;
%feature("director") FetchAppListCallback;
%feature("director") FetchAppListProgressCallback;
%feature("director") ExportFileCallback;
%feature("director") ExportFileProgressCallback;
%feature("director") UploadAIBoxFileCallback;
%feature("director") InstallAppCallback;
%feature("director") InstallAppProgressCallback;
%feature("director") OperateAppCallback;

// DRC northbound push listener callbacks
%feature("director") DrcOsdInfoCallback;
%feature("director") DrcBatteriesInfoCallback;
%feature("director") DrcParachuteInfoCallback;
%feature("director") FlashLedStateInfoCallback;
%feature("director") FlashLedConfigListCallback;
%feature("director") FlashLedConfigCallback;
%feature("director") AiAlarmStateCallback;
%feature("director") DrcDelayInfoCallback;
%feature("director") DrcCountDownInfoCallback;

// EA340 消息推送补全（Phase D）新增回调接口;
%feature("director") DockLiveStatusCallback;
%feature("director") DockLiveCapacityCallback;
%feature("director") DockDongleInfosCallback;
%feature("director") DockAirConditionerModeCallback;
%feature("director") DroneModeCodeReasonCallback;
%feature("director") DroneDongleInfosCallback;
%feature("director") DroneFlysafeDatabaseVersionCallback;
%feature("director") DroneWpmzVersionCallback;
%feature("director") DroneCommanderFlightModeCallback;
%feature("director") DroneCurrentCommanderFlightModeCallback;
%feature("director") DroneCommanderModeLostActionCallback;
%feature("director") DroneOfflineMapEnableCallback;
%feature("director") DronePsdkUiResourceCallback;
%feature("director") DroneCameraWatermarkSettingsPushCallback;

%inline %{
    // 前向声明：TerraModelDetailJavaResult 定义在 terra_build_swig 命名空间的另一个 inline 块中
    namespace terra_build_swig { struct TerraModelDetailJavaResult; }

    // Java callback interfaces
    class HmsDataCallback {
    public:
        virtual ~HmsDataCallback() {}
        virtual void onHmsData(const flight_hub_sdk::HmsData& hms_data) = 0;
    };

    class AircraftPayloadsOsdInfoListCallback {
    public:
        virtual ~AircraftPayloadsOsdInfoListCallback() {}
        virtual void onAircraftPayloadsOsdInfoList(const flight_hub_sdk::AircraftPayloadsOsdInfoList &payloads_list) = 0;
    };

    class AircraftGearCallback {
    public:
        virtual ~AircraftGearCallback() {}
        virtual void onAircraftGear(const flight_hub_sdk::AircraftGear& gear) = 0;
    };

    class AircraftHomeDistanceCallback {
    public:
        virtual ~AircraftHomeDistanceCallback() {}
        virtual void onAircraftHomeDistance(const flight_hub_sdk::AircraftHomeDistance& home_distance) = 0;
    };

    class AircraftPositionStateCallback {
    public:
        virtual ~AircraftPositionStateCallback() {}
        virtual void onAircraftPositionState(const flight_hub_sdk::AircraftPositionState& state) = 0;
    };

    class AircraftControlSourceCallback {
    public:
        virtual ~AircraftControlSourceCallback() {}
        virtual void onAircraftControlSource(const flight_hub_sdk::AircraftControlSource& control_source) = 0;
    };

    class AircraftLimitsInfoCallback {
    public:
        virtual ~AircraftLimitsInfoCallback() {}
        virtual void onAircraftLimitsInfo(const flight_hub_sdk::AircraftLimitsInfo& info) = 0;
    };

    class AircraftBatteryWarningThresholdsCallback {
    public:
        virtual ~AircraftBatteryWarningThresholdsCallback() {}
        virtual void onAircraftBatteryWarningThresholds(const flight_hub_sdk::AircraftBatteryWarningThresholds& threshold) = 0;
    };

    class AircraftFirmwareStatusCallback {
    public:
        virtual ~AircraftFirmwareStatusCallback() {}
        virtual void onAircraftFirmwareStatus(const flight_hub_sdk::AircraftFirmwareStatus& status) = 0;
    };

    class AircraftTotalFlightInfoCallback {
    public:
        virtual ~AircraftTotalFlightInfoCallback() {}
        virtual void onAircraftTotalFlightInfo(const flight_hub_sdk::AircraftTotalFlightInfo& info) = 0;
    };

    class AircraftActivationTimeCallback {
    public:
        virtual ~AircraftActivationTimeCallback() {}
        virtual void onAircraftActivationTime(const flight_hub_sdk::AircraftActivationTime& mode) = 0;
    };

    class AircraftMaintainStatusInfoCallback {
    public:
        virtual ~AircraftMaintainStatusInfoCallback() {}
        virtual void onAircraftMaintainStatusInfo(const flight_hub_sdk::AircraftMaintainStatusInfo& info) = 0;
    };

    class AircraftNightLightsStateCallback {
    public:
        virtual ~AircraftNightLightsStateCallback() {}
        virtual void onAircraftNightLightsState(const flight_hub_sdk::AircraftNightLightsState& status) = 0;
    };

    class AircraftObstacleAvoidanceStateCallback {
    public:
        virtual ~AircraftObstacleAvoidanceStateCallback() {}
        virtual void onAircraftObstacleAvoidanceState(const flight_hub_sdk::AircraftObstacleAvoidanceState& state) = 0;
    };

    class AircraftStorageInfoCallback {
    public:
        virtual ~AircraftStorageInfoCallback() {}
        virtual void onAircraftStorageInfo(const flight_hub_sdk::AircraftStorageInfo& status) = 0;
    };

    class AircraftTrackIdCallback {
    public:
        virtual ~AircraftTrackIdCallback() {}
        virtual void onAircraftTrackId(const flight_hub_sdk::AircraftTrackId& track_id) = 0;
    };

    class AircraftComplianceInfoCallback {
    public:
        virtual ~AircraftComplianceInfoCallback() {}
        virtual void onAircraftComplianceInfo(const flight_hub_sdk::AircraftComplianceInfo& info) = 0;
    };

    class AircraftPayloadsCamerasListCallback {
    public:
        virtual ~AircraftPayloadsCamerasListCallback() {}
        virtual void onAircraftPayloadsCamerasList(const flight_hub_sdk::AircraftPayloadsCamerasList& status) = 0;
    };

    class AircraftRcLostActionCallback {
    public:
        virtual ~AircraftRcLostActionCallback() {}
        virtual void onAircraftRcLostAction(const flight_hub_sdk::AircraftRcLostAction& action) = 0;
    };

    class AircraftRthInfoCallback {
    public:
        virtual ~AircraftRthInfoCallback() {}
        virtual void onAircraftRthInfo(const flight_hub_sdk::AircraftRthInfo& info) = 0;
    };

    class AircraftHomeGeographicalInfoCallback {
    public:
        virtual ~AircraftHomeGeographicalInfoCallback() {}
        virtual void onAircraftHomeGeographicalInfo(const flight_hub_sdk::AircraftHomeGeographicalInfo& info) = 0;
    };

    class AircraftCurrentRthModeCallback {
    public:
        virtual ~AircraftCurrentRthModeCallback() {}
        virtual void onAircraftCurrentRthMode(const flight_hub_sdk::AircraftCurrentRthMode& mode) = 0;
    };

    class DeviceListCallback {
    public:
        virtual ~DeviceListCallback() {}
        virtual void onDeviceList(const std::vector<flight_hub_sdk::DeviceTopo>& devices, flight_hub_sdk::FlightHubError err) = 0;
    };

    class GetDeviceDetailInfoCallback {
    public:
        virtual ~GetDeviceDetailInfoCallback() {}
        virtual void onGetDeviceDetailInfo(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceDetailInfo& device_detail) = 0;
    };

    class ModifyDeviceNameCallback {
    public:
        virtual ~ModifyDeviceNameCallback() {}
        virtual void onModifyDeviceName(flight_hub_sdk::FlightHubError err) = 0;
    };

    class LiveAvailableRangeCallback {
    public:
        virtual ~LiveAvailableRangeCallback() {}
        virtual void onLiveAvailableRange(const std::vector<flight_hub_sdk::LiveInfo>& live_infos) = 0;
    };

    class LiveStreamCallback {
    public:
        virtual ~LiveStreamCallback() {}
        virtual void onLiveStream(const std::vector<unsigned char>& data, const flight_hub_sdk::FrameInfo& frame_info) = 0;
    };

    class AiSeiInfoCallback {
    public:
        virtual ~AiSeiInfoCallback() {}
        virtual void onAiSeiInfo(const flight_hub_sdk::AiSeiInfo& ai_sei_info) = 0;
    };

    class ArSeiInfoCallback {
    public:
        virtual ~ArSeiInfoCallback() {}
        virtual void onArSeiInfo(const flight_hub_sdk::ArSeiInfo& ar_sei_info) = 0;
    };

    class FovSeiInfoCallback {
    public:
        virtual ~FovSeiInfoCallback() {}
        virtual void onFovSeiInfo(const flight_hub_sdk::FovSeiInfo& fov_sei_info) = 0;
    };

    class AssistantSeiInfoCallback {
    public:
        virtual ~AssistantSeiInfoCallback() {}
        virtual void onAssistantSeiInfo(const flight_hub_sdk::AssistantSeiInfo& assistant_sei_info) = 0;
    };

    class EisSeiInfoCallback {
    public:
        virtual ~EisSeiInfoCallback() {}
        virtual void onEisSeiInfo(const flight_hub_sdk::EisSeiInfo& eis_sei_info) = 0;
    };

    class ResultCallback {
    public:
        virtual ~ResultCallback() {}
        virtual void onResult(flight_hub_sdk::FlightHubError err) = 0;
    };

    class ResultCallbackWithError {
    public:
        virtual ~ResultCallbackWithError() {}
        virtual void onResult(const flight_hub_sdk::FlightHubError& err) = 0;
    };

    class LiveStreamStartCallback {
    public:
        virtual ~LiveStreamStartCallback() {}
        virtual void onResult(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::LiveStreamStartResult& result) = 0;
    };

    class GetDeviceLiveRecordsCallback {
    public:
        virtual ~GetDeviceLiveRecordsCallback() {}
        virtual void onResult(flight_hub_sdk::FlightHubError err, const std::vector<flight_hub_sdk::LiveRecordItem>& records) = 0;
    };

    class LiveRecordStateCallback {
    public:
        virtual ~LiveRecordStateCallback() {}
        virtual void onRecordState(const flight_hub_sdk::LiveRecordStateInfo& info) = 0;
    };

    class FlyToPointStartCallback {
    public:
        virtual ~FlyToPointStartCallback() {}
        virtual void onFlyToPointStart(flight_hub_sdk::FlightHubError err, const std::string& fly_to_id) = 0;
    };

    class FlyToDroneSelfPlanPathCallback {
    public:
        virtual ~FlyToDroneSelfPlanPathCallback() {}
        virtual void onFlyToDroneSelfPlanPath(const flight_hub_sdk::FlyToDroneSelfPlanPath& fly_to_drone_self_plan_path) = 0;
    };

    class FlyToPointProgressCallback {
    public:
        virtual ~FlyToPointProgressCallback() {}
        virtual void onFlyToPointProgress(const flight_hub_sdk::FlyToPointProgress& fly_to_point_progress) = 0;
    };

    class PoiEnterCallback {
    public:
        virtual ~PoiEnterCallback() {}
        virtual void onPoiEnter(flight_hub_sdk::FlightHubError err, const std::string& task_uuid) = 0;
    };

    class PoiExitCallback {
    public:
        virtual ~PoiExitCallback() {}
        virtual void onPoiExit(flight_hub_sdk::FlightHubError err) = 0;
    };

    class PoiUpdateSpeedCallback {
    public:
        virtual ~PoiUpdateSpeedCallback() {}
        virtual void onPoiUpdateSpeed(flight_hub_sdk::FlightHubError err) = 0;
    };

    class PoiGetTaskCallback {
    public:
        virtual ~PoiGetTaskCallback() {}
        virtual void onPoiGetTask(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::POITaskInfo& task) = 0;
    };

    class AiDataInfoCallback {
    public:
        virtual ~AiDataInfoCallback() {}
        virtual void onAiDataInfo(const flight_hub_sdk::AiDataInfo& info) = 0;
    };

    class AircraftBatteryInfoCallback {
    public:
        virtual ~AircraftBatteryInfoCallback() {}
        virtual void onAircraftBatteryInfo(const flight_hub_sdk::AircraftBatteryInfo& battery_info) = 0;
    };

    class AircraftAttitudeCallback {
    public:
        virtual ~AircraftAttitudeCallback() {}
        virtual void onAircraftAttitude(const flight_hub_sdk::AircraftAttitude& attitude) = 0;
    };

    class AircraftLocationCallback {
    public:
        virtual ~AircraftLocationCallback() {}
        virtual void onAircraftLocation(const flight_hub_sdk::AircraftLocation& location) = 0;
    };

    class AircraftModeCodeCallback {
    public:
        virtual ~AircraftModeCodeCallback() {}
        virtual void onAircraftModeCode(const flight_hub_sdk::AircraftModeCode& mode_code) = 0;
    };

    class AircraftWindSpeedCallback {
    public: 
        virtual ~AircraftWindSpeedCallback() {}
        virtual void onAircraftWindSpeed(const flight_hub_sdk::AircraftWindSpeed& wind_speed) = 0;
    };

    class AircraftTakeoffProgressCallback {
    public:
        virtual ~AircraftTakeoffProgressCallback() {}
        virtual void onAircraftTakeoffProgress(const flight_hub_sdk::AircraftTakeoffProgress& takeoff_progress) = 0;
    };

    class TakeoffResultCallback {
    public:
        virtual ~TakeoffResultCallback() {}
        virtual void onResult(const flight_hub_sdk::FlightHubError& err, const flight_hub_sdk::TakeoffResult& result) = 0;
    };

    class CreateInFlightWaylineTaskCallback {
    public:
        virtual ~CreateInFlightWaylineTaskCallback() {}
        virtual void onResult(const flight_hub_sdk::CreateInFlightWaylineTaskResult& result, const flight_hub_sdk::FlightHubError& err) = 0;
    };

    class GetActiveInFlightWaylineTaskCallback {
    public:
        virtual ~GetActiveInFlightWaylineTaskCallback() {}
        virtual void onResult(const flight_hub_sdk::ActiveInFlightWaylineTaskInfo& info, const flight_hub_sdk::FlightHubError& err) = 0;
    };

    class InFlightWaylineProgressCallback {
    public:
        virtual ~InFlightWaylineProgressCallback() {}
        virtual void onInFlightWaylineProgress(const flight_hub_sdk::InFlightWaylineProgress& progress) = 0;
    };

    class FlightControlAuthorityCallback {
    public:
        virtual ~FlightControlAuthorityCallback() {}
        virtual void onFlightControlAuthority(bool available) = 0;
    };
    
    class PayloadControlAuthorityCallback {
    public:
        virtual ~PayloadControlAuthorityCallback() {}
        virtual void onPayloadControlAuthority(bool available) = 0;
    };

    class CameraStateCallback {
    public:
        virtual ~CameraStateCallback() {}
        virtual void onCameraState(const flight_hub_sdk::CameraState& state) = 0;
    };

    class LiveviewWorldRegionCallback {
    public:
        virtual ~LiveviewWorldRegionCallback() {}
        virtual void onLiveviewWorldRegion(const flight_hub_sdk::LiveviewWorldRegion& region) = 0;
    };

    class CameraWideLensStateCallback {
    public:
        virtual ~CameraWideLensStateCallback() {}
        virtual void onCameraWideLensState(const flight_hub_sdk::CameraWideLensState& state) = 0;
    };

    class CameraZoomLensStateCallback {
    public:
        virtual ~CameraZoomLensStateCallback() {}
        virtual void onCameraZoomLensState(const flight_hub_sdk::CameraZoomLensState& state) = 0;
    };

    class CameraMeasureTargetCallback {
    public:
        virtual ~CameraMeasureTargetCallback() {}
        virtual void onCameraMeasureTarget(const flight_hub_sdk::CameraMeasureTarget& target) = 0;
    };

    class NightModeSettingsCallback {
    public:
        virtual ~NightModeSettingsCallback() {}
        virtual void onNightModeSettings(const flight_hub_sdk::NightModeSettings& settings) = 0;
    };

    class DockSubDeviceInfoListCallback {
    public:
        virtual ~DockSubDeviceInfoListCallback() {}
        virtual void onDockSubDeviceInfoList(const flight_hub_sdk::DockSubDeviceInfoList &sub_device_info_list) = 0;
    };
    
    class DockNetworkStateCallback {
    public:
        virtual ~DockNetworkStateCallback() {}
        virtual void onDockNetworkState(const flight_hub_sdk::DockNetworkState& dock_network_state) = 0;
    };

    class GimbalStateCallback {
    public:
        virtual ~GimbalStateCallback() {}
        virtual void onGimbalState(const flight_hub_sdk::GimbalState& state) = 0;
    };

    class DroneOnlineStatusCallback {

    public:
        virtual ~DroneOnlineStatusCallback() {}
        virtual void onDroneOnlineStatus(const flight_hub_sdk::DroneOnlineStatus& status) = 0;
    };

    class LinkStatusCallback {

    public:
        virtual ~LinkStatusCallback() {}
        virtual void onLinkStatus(const flight_hub_sdk::LinkStatus& status) = 0;
    };


    class IntervalTakePhotoStatusCallback {
    public:
        virtual ~IntervalTakePhotoStatusCallback() {}
        virtual void onIntervalTakePhotoStatus(const flight_hub_sdk::IntervalTakePhotoStatus& status) = 0;
    };

    class PanoramaStatusCallback {
    public:
        virtual ~PanoramaStatusCallback() {}
        virtual void onPanoramaStatus(const flight_hub_sdk::PanoramaStatus& status) = 0;
    };

    class CameraModeAvailableRangeCallback {
    public:
        virtual ~CameraModeAvailableRangeCallback() {}
        virtual void onCameraModeAvailableRange(const std::vector<int>& camera_modes) = 0;
    };

    // PSDK相关回调接口
    class PsdkDeviceListCallback {
    public:
        virtual ~PsdkDeviceListCallback() {}
        virtual void onPsdkDeviceList(const flight_hub_sdk::PsdkDeviceList& psdk_device_list) = 0;
    };

    class PsdkStateSummaryInfoCallback {
    public:
        virtual ~PsdkStateSummaryInfoCallback() {}
        virtual void onPsdkStateSummaryInfo(const flight_hub_sdk::PsdkStateSummaryInfo& psdk_state_summary_info) = 0;
    };

    class PsdkStateSpeakerInfoCallback {
    public:
        virtual ~PsdkStateSpeakerInfoCallback() {}
        virtual void onPsdkStateSpeakerInfo(const flight_hub_sdk::PsdkStateSpeakerInfo& psdk_state_speaker_info) = 0;
    };

    class PsdkStateSearchlightInfoCallback {
    public:
        virtual ~PsdkStateSearchlightInfoCallback() {}
        virtual void onPsdkStateSearchlightInfo(const flight_hub_sdk::PsdkStateSearchlightInfo& psdk_state_searchlight_info) = 0;
    };

    class PsdkSpeakerPlayProgressInfoCallback {
    public:
        virtual ~PsdkSpeakerPlayProgressInfoCallback() {}
        virtual void onPsdkSpeakerPlayProgressInfo(const flight_hub_sdk::PsdkSpeakerPlayProgressInfo& psdk_speaker_play_progress_info) = 0;
    };

    class PsdkFloatingWindowTextInfoCallback {
    public:
        virtual ~PsdkFloatingWindowTextInfoCallback() {}
        virtual void onPsdkFloatingWindowTextInfo(const flight_hub_sdk::PsdkFloatingWindowTextInfo& psdk_floating_window_text_info) = 0;
    };

    // PSDK资源相关回调接口
    class PsdkSpeakerResourceListCallback {
    public:
        virtual ~PsdkSpeakerResourceListCallback() {}
        virtual void onPsdkSpeakerResourceList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResourceList& psdk_speaker_resource_list) = 0;
    };

    class PsdkSpeakerResourceCallback {
    public:
        virtual ~PsdkSpeakerResourceCallback() {}
        virtual void onPsdkSpeakerResource(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResource& psdk_speaker_resource) = 0;
    };

    class STSInfoCallback {
    public:
        virtual ~STSInfoCallback() {}
        virtual void onSTSInfo(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::STSInfo& sts_info) = 0;
    };

    class PsdkWidgetResourceListCallback {
    public:
        virtual ~PsdkWidgetResourceListCallback() {}
        virtual void onPsdkWidgetResourceList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkWidgetResourceList& psdk_widget_resource_list) = 0;
    };

    class DockDeviceStatusListCallback {
    public:
        virtual ~DockDeviceStatusListCallback() {}
        virtual void onDockDeviceStatusList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DockDeviceStatusResultList& result) = 0;
    };

    class DroneDeviceStatusListCallback {
    public:
        virtual ~DroneDeviceStatusListCallback() {}
        virtual void onDroneDeviceStatusList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DroneDeviceStatusResultList& result) = 0;
    };

    class GetAudienceCountCallback {
    public:
        virtual ~GetAudienceCountCallback() {}
        virtual void onGetAudienceCount(flight_hub_sdk::FlightHubError err, const std::vector<flight_hub_sdk::DeviceAudienceCountItem>& result) = 0;
    };

    // 航线任务模块回调接口
    class GetFlightTaskListCallback {
    public:
        virtual ~GetFlightTaskListCallback() {}
        virtual void onGetFlightTaskList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightTaskListResult& result) = 0;
    };
    class GetCommandTaskListCallback {
    public:
        virtual ~GetCommandTaskListCallback() {}
        virtual void onGetCommandTaskList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetCommandTaskListResults& results) = 0;
    };
    class GetFlightTaskTrackCallback {
    public:
        virtual ~GetFlightTaskTrackCallback() {}
        virtual void onGetFlightTaskTrack(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightTaskTrackResults& results) = 0;
    };
    class CreateFlightTaskCallback {
    public:
        virtual ~CreateFlightTaskCallback() {}
        virtual void onCreateFlightTask(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateTaskResp& result) = 0;
    };
    class GetFlightTaskDetailCallback {
    public:
        virtual ~GetFlightTaskDetailCallback() {}
        virtual void onGetFlightTaskDetail(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TaskDetail& result) = 0;
    };
    class GetDeviceRecommendationCallback {
    public:
        virtual ~GetDeviceRecommendationCallback() {}
        virtual void onGetDeviceRecommendation(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRecommendationResp& result) = 0;
    };
    class GetDeviceTcasCallback {
    public:
        virtual ~GetDeviceTcasCallback() {}
        virtual void onGetDeviceTcas(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetTCAInfosResp& result) = 0;
    };
    class StartRoamingCallback {
    public:
        virtual ~StartRoamingCallback() {}
        virtual void onStartRoaming(flight_hub_sdk::FlightHubError err) = 0;
    };
    class SetRoamingStationSnCallback {
    public:
        virtual ~SetRoamingStationSnCallback() {}
        virtual void onSetRoamingStationSn(flight_hub_sdk::FlightHubError err,
                                            const flight_hub_sdk::SetRoamingStationSnResult& result) = 0;
    };
    class GetInFlightTaskToposCallback {
    public:
        virtual ~GetInFlightTaskToposCallback() {}
        virtual void onGetInFlightTaskTopos(flight_hub_sdk::FlightHubError err,
                                             const flight_hub_sdk::GetInFlightTaskToposResult& result) = 0;
    };
    class TaskTopoChangedCallback {
    public:
        virtual ~TaskTopoChangedCallback() {}
        virtual void onTaskTopoChanged(const flight_hub_sdk::TaskTopoChangedEvent& event) = 0;
    };

    // 红外增益模式相关回调接口
    class LiveVideoInfoCallback {
    public:
        virtual ~LiveVideoInfoCallback() {}
        virtual void onLiveVideoInfo(const flight_hub_sdk::LiveVideoInfo& info) = 0;
    };

    class ThermalGainModeInfoCallback {
    public:
        virtual ~ThermalGainModeInfoCallback() {}
        virtual void onThermalGainModeInfo(const flight_hub_sdk::ThermalGainModeInfo& info) = 0;
    };

    class CameraIrLensStateCallback {
    public:
        virtual ~CameraIrLensStateCallback() {}
        virtual void onCameraIrLensState(const flight_hub_sdk::CameraIrLensState& state) = 0;
    };

    class CameraIrThermometryInfoCallback {
    public:
        virtual ~CameraIrThermometryInfoCallback() {}
        virtual void onCameraIrThermometryInfo(const flight_hub_sdk::CameraIrThermometryInfo& info) = 0;
    };

    class CameraWatermarkSettingsCallback {
    public:
        virtual ~CameraWatermarkSettingsCallback() {}
        virtual void onCameraWatermarkSettings(const flight_hub_sdk::CameraWatermarkSettings& v) = 0;
    };
    class CameraStorageInfoCallback {
    public:
        virtual ~CameraStorageInfoCallback() {}
        virtual void onCameraStorageInfo(const flight_hub_sdk::CameraStorageInfo& v) = 0;
    };
    class CameraIrPipStateCallback {
    public:
        virtual ~CameraIrPipStateCallback() {}
        virtual void onCameraIrPipState(const flight_hub_sdk::CameraIrPipState& v) = 0;
    };
    class CameraIsothermPolygonCallback {
    public:
        virtual ~CameraIsothermPolygonCallback() {}
        virtual void onCameraIsothermPolygon(const flight_hub_sdk::CameraIsothermPolygon& v) = 0;
    };
    class CameraMediaStorageCallback {
    public:
        virtual ~CameraMediaStorageCallback() {}
        virtual void onCameraMediaStorage(const flight_hub_sdk::CameraMediaStorage& v) = 0;
    };
    class CameraAlgorithmDehazeInfoCallback {
    public:
        virtual ~CameraAlgorithmDehazeInfoCallback() {}
        virtual void onCameraAlgorithmDehazeInfo(const flight_hub_sdk::CameraAlgorithmDehazeInfo& v) = 0;
    };
    class CameraPreRecordedInfoCallback {
    public:
        virtual ~CameraPreRecordedInfoCallback() {}
        virtual void onCameraPreRecordedInfo(const flight_hub_sdk::CameraPreRecordedInfo& v) = 0;
    };
    class CameraCapabilityCallback {
    public:
        virtual ~CameraCapabilityCallback() {}
        virtual void onCameraCapability(const flight_hub_sdk::CameraCapability& v) = 0;
    };
    class CameraSettableCapabilityCallback {
    public:
        virtual ~CameraSettableCapabilityCallback() {}
        virtual void onCameraSettableCapability(const flight_hub_sdk::CameraSettableCapability& v) = 0;
    };
    class CameraVisibleCapabilityCallback {
    public:
        virtual ~CameraVisibleCapabilityCallback() {}
        virtual void onCameraVisibleCapability(const flight_hub_sdk::CameraVisibleCapability& v) = 0;
    };
    class CameraPhotoInfoCallback {
    public:
        virtual ~CameraPhotoInfoCallback() {}
        virtual void onCameraPhotoInfo(const flight_hub_sdk::CameraPhotoInfo& v) = 0;
    };

    // 飞行任务异常和设备变更相关回调接口
    class FlightTaskExceptionCallback {
    public:
        virtual ~FlightTaskExceptionCallback() {}
        virtual void onFlightTaskException(const flight_hub_sdk::FlightTaskException& exception) = 0;
    };

    class WorkspaceDeviceChangeCallback {
    public:
        virtual ~WorkspaceDeviceChangeCallback() {}
        virtual void onWorkspaceDeviceChange(const flight_hub_sdk::WorkspaceDeviceChange& change) = 0;
    };

    class ImportWaylineEventCallback {
    public:
        virtual ~ImportWaylineEventCallback() {}
        virtual void onImportWaylineEvent(const flight_hub_sdk::ImportWaylineEventResult& result) = 0;
    };

    class DeviceTopoChangeCallback {
    public:
        virtual ~DeviceTopoChangeCallback() {}
        virtual void onDeviceTopoChange(const flight_hub_sdk::DeviceTopoChange& change) = 0;
    };

    // 新增的BusinessManager接口回调类
    class AircraftControlCodeCallback {
    public:
        virtual ~AircraftControlCodeCallback() {}
        virtual void onAircraftControlCode(const flight_hub_sdk::AircraftControlCode& control_code) = 0;
    };

    class AircraftSpeedCallback {
    public:
        virtual ~AircraftSpeedCallback() {}
        virtual void onAircraftSpeed(const flight_hub_sdk::AircraftSpeed& speed) = 0;
    };

    class AircraftPayloadsCameraLiveviewWorldRegionCallback {
    public:
        virtual ~AircraftPayloadsCameraLiveviewWorldRegionCallback() {}
        virtual void onAircraftPayloadsCameraLiveviewWorldRegion(const flight_hub_sdk::AircraftPayloadsCameraLiveviewWorldRegion& region) = 0;
    };

    class AircraftPayloadsGimbalAttitudeCallback {
    public:
        virtual ~AircraftPayloadsGimbalAttitudeCallback() {}
        virtual void onAircraftPayloadsGimbalAttitude(const flight_hub_sdk::AircraftPayloadsGimbalAttitude& attitude) = 0;
    };

    class AircraftPayloadsListCallback {
    public:
        virtual ~AircraftPayloadsListCallback() {}
        virtual void onAircraftPayloadsList(const flight_hub_sdk::AircraftPayloadsList& payloads_list) = 0;
    };

    class ControlRightsCloudControlStatusCallback {
    public:
        virtual ~ControlRightsCloudControlStatusCallback() {}
        virtual void onControlRightsCloudControlStatus(const flight_hub_sdk::ControlRightsCloudControlStatus& status) = 0;
    };

    class ControlRightsDeviceControlStatusCallback {
    public:
        virtual ~ControlRightsDeviceControlStatusCallback() {}
        virtual void onControlRightsDeviceControlStatus(const flight_hub_sdk::ControlRightsDeviceControlStatus& status) = 0;
    };

    class FlightTasksMediaFileDisplayAvailableCallback {
    public:
        virtual ~FlightTasksMediaFileDisplayAvailableCallback() {}
        virtual void onFlightTasksMediaFileDisplayAvailable(const flight_hub_sdk::FlightTasksMediaFileDisplayAvailable& available) = 0;
    };

    class CommandTaskEventCallback {
    public:
        virtual ~CommandTaskEventCallback() {}
        virtual void onCommandTaskEvent(const flight_hub_sdk::CommandTaskEvent& event) = 0;
    };

    class FlyToTaskEventCallback {
    public:
        virtual ~FlyToTaskEventCallback() {}
        virtual void onFlyToTaskEvent(const flight_hub_sdk::FlyToTaskEvent& event) = 0;
    };

    class PoiTaskEventCallback {
    public:
        virtual ~PoiTaskEventCallback() {}
        virtual void onPoiTaskEvent(const flight_hub_sdk::PoiTaskEvent& event) = 0;
    };

    class WaylineTaskEventCallback {
    public:
        virtual ~WaylineTaskEventCallback() {}
        virtual void onWaylineTaskEvent(const flight_hub_sdk::WaylineTaskEvent& event) = 0;
    };

    class DockLocationCallback {
    public:
        virtual ~DockLocationCallback() {}
        virtual void onDockLocation(const flight_hub_sdk::DockLocation& location) = 0;
    };

    class DroneInDockCallback {
    public:
        virtual ~DroneInDockCallback() {}
        virtual void onDroneInDock(const flight_hub_sdk::DroneInDock& drone_in_dock) = 0;
    };

    class DockCoverStateCallback {
    public:
        virtual ~DockCoverStateCallback() {}
        virtual void onDockCoverState(const flight_hub_sdk::DockCoverState & info) = 0;
    };

    class DockMediaFileDetailCallback {
    public:
        virtual ~DockMediaFileDetailCallback() {}
        virtual void onDockMediaFileDetail(const flight_hub_sdk::DockMediaFileDetail& detail) = 0;
    };


    class DockWirelessLinkCallback {
    public:
        virtual ~DockWirelessLinkCallback() {}
        virtual void onDockWirelessLink(const flight_hub_sdk::DockWirelessLink& link) = 0;
    };
    
    class DockJobNumberCallback {
    public:
        virtual ~DockJobNumberCallback() {}
        virtual void onDockJobNumber(const flight_hub_sdk::DockJobNumber& job_number) = 0;
    };

    class FlightTaskStepCodeCallback {
    public:
        virtual ~FlightTaskStepCodeCallback() {}
        virtual void onFlightTaskStepCode(const flight_hub_sdk::FlightTaskStepCode& code) = 0;
    };

    class DockPowerStateCallback {
    public:
        virtual ~DockPowerStateCallback() {}
        virtual void onDockPowerState(const flight_hub_sdk::DockPowerState& power_state) = 0;
    };

    class DockStorageStateCallback {
    public:
        virtual ~DockStorageStateCallback() {}
        virtual void onDockStorageState(const flight_hub_sdk::DockStorageState& dock_storage_state) = 0;
    };

    class DockFirstPowerOnTimeCallback {
    public:
        virtual ~DockFirstPowerOnTimeCallback() {}
        virtual void onDockFirstPowerOnTime(const flight_hub_sdk::DockFirstPowerOnTime& first_power_on_time) = 0;
    };

    class DockAccTimeCallback {
    public:
        virtual ~DockAccTimeCallback() {}
        virtual void onDockAccTime(const flight_hub_sdk::DockAccTime& dock_acc_time) = 0;
    };

    class DockModeCodeCallback {
    public:
        virtual ~DockModeCodeCallback() {}
        virtual void onDockModeCode(const flight_hub_sdk::DockModeCode& code) = 0;
    };

    class DockDroneChargeStateCallback {
    public:
        virtual ~DockDroneChargeStateCallback() {}
        virtual void onDockDroneChargeState(const flight_hub_sdk::DockDroneChargeState & state) = 0;
    };

    class DockUserExperienceImprovementCallback {
    public:
        virtual ~DockUserExperienceImprovementCallback() {}
        virtual void onDockUserExperienceImprovement(const flight_hub_sdk::DockUserExperienceImprovement & state) = 0;
    };

    class DockDrcStateCallback {
    public:
        virtual ~DockDrcStateCallback() {}
        virtual void onDockDrcState(const flight_hub_sdk::DockDrcState & state) = 0;
    };

    class DockPutterStateCallback {
    public:
        virtual ~DockPutterStateCallback() {}
        virtual void onDockPutterState(const flight_hub_sdk::DockPutterState & state) = 0;
    };

    class DockAlternateLandPointCallback {
    public:
        virtual ~DockAlternateLandPointCallback() {}
        virtual void onDockAlternateLandPoint(const flight_hub_sdk::DockAlternateLandPoint& dock_alternate_land_point) = 0;
    };

    class DockPositionStateCallback {
    public:
        virtual ~DockPositionStateCallback() {}
        virtual void onDockPositionState(const flight_hub_sdk::DockPositionState& dock_position_state) = 0;
    };

    class DockActivationTimeCallback {
    public:
        virtual ~DockActivationTimeCallback() {}
        virtual void onDockActivationTime(const flight_hub_sdk::DockActivationTime& dock_activateion_time) = 0;
    };

    class DockBatteryStoreModeCallback {
    public:
        virtual ~DockBatteryStoreModeCallback() {}
        virtual void onDockBatteryStoreMode(const flight_hub_sdk::DockBatteryStoreMode& dock_battery_store_mode) = 0;
    };

    class DockAlarmStateCallback {
    public:
        virtual ~DockAlarmStateCallback() {}
        virtual void onDockAlarmState(const flight_hub_sdk::DockAlarmState& dock_alarm_state) = 0;
    };

    class DockDroneBatteryMaintenanceInfoCallback {
    public:
        virtual ~DockDroneBatteryMaintenanceInfoCallback() {}
        virtual void onDockDroneBatteryMaintenanceInfo(const flight_hub_sdk::DockDroneBatteryMaintenanceInfo& dock_drone_battery_maintenance_info) = 0;
    };

    class DockEmergencyStopStateCallback {
    public:
        virtual ~DockEmergencyStopStateCallback() {}
        virtual void onDockEmergencyStopState(const flight_hub_sdk::DockEmergencyStopState& state) = 0;
    };

    class DockSilentModeCallback {
    public:
        virtual ~DockSilentModeCallback() {}
        virtual void onDockSilentMode(const flight_hub_sdk::DockSilentMode& dock_silent_mode) = 0;
    };

    class DockRelativeAlternateLandPointCallback {
    public:
        virtual ~DockRelativeAlternateLandPointCallback() {}
        virtual void onDockRelativeAlternateLandPoint(const flight_hub_sdk::DockRelativeAlternateLandPoint& land_point) = 0;
    };

    class DockSelfConvergeCoordinateCallback {
    public:
        virtual ~DockSelfConvergeCoordinateCallback() {}
        virtual void onDockSelfConvergeCoordinate(const flight_hub_sdk::DockSelfConvergeCoordinate& coordinate) = 0;
    };

    class DockFirmwareInfoCallback {
    public:
        virtual ~DockFirmwareInfoCallback() {}
        virtual void onDockFirmwareInfo(const flight_hub_sdk::DockFirmwareInfo& info) = 0;
    };

    class DockMeteorologicalInfoCallback {
    public:
        virtual ~DockMeteorologicalInfoCallback() {}
        virtual void onDockMeteorologicalInfo(const flight_hub_sdk::DockMeteorologicalInfo& dock_meteorological_info) = 0;
    };

    class DockBackupBatteryInfoCallback {
    public:
        virtual ~DockBackupBatteryInfoCallback() {}
        virtual void onDockBackupBatteryInfo(const flight_hub_sdk::DockBackupBatteryInfo& dock_backup_battery_info) = 0;
    };

    class DockMaintainStatusInfoCallback {
    public:
        virtual ~DockMaintainStatusInfoCallback() {}
        virtual void onDockMaintainStatusInfo(const flight_hub_sdk::DockMaintainStatusInfo& status) = 0;
    };

    class DockOnlineStatusCallback {
    public:
        virtual ~DockOnlineStatusCallback() {}
        virtual void onDockOnlineStatus(const flight_hub_sdk::DockOnlineStatus& status) = 0;
    };

    class DockAirConditionerStatusInfoCallback {
    public:
        virtual ~DockAirConditionerStatusInfoCallback() {}
        virtual void onDockAirConditionerStatusInfo(const flight_hub_sdk::DockAirConditionerStatusInfo& status_info) = 0;
    };

    class DockSupplementLightStateCallback {
    public:
        virtual ~DockSupplementLightStateCallback() {}
        virtual void onDockSupplementLightState(const flight_hub_sdk::DockSupplementLightState& light_state) = 0;
    };

    // 拉流相关回调类
    class PullStreamStartCallback {
    public:
        virtual ~PullStreamStartCallback() {}
        virtual void onPullStreamStart(flight_hub_sdk::FlightHubSdkErrorCode err_code) = 0;
    };

    class PullStreamDataCallback {
    public:
        virtual ~PullStreamDataCallback() {}
        virtual void onPullStreamData(const std::vector<unsigned char>& data, const flight_hub_sdk::FrameInfo& frame_info) = 0;
    };

    class PullStreamErrorCallback {
    public:
        virtual ~PullStreamErrorCallback() {}
        virtual void onPullStreamError(int error_code, const std::string& error_message) = 0;
    };

    class OpenDebugModeCallback {
    public:
        virtual ~OpenDebugModeCallback() {}
        virtual void onOpenDebugMode(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OpenDebugModeResult& result) = 0;
    };

    class CloseDebugModeCallback {
    public:
        virtual ~CloseDebugModeCallback() {}
        virtual void onCloseDebugMode(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CloseDebugModeResult& result) = 0;
    };

    class OpenDockCoverCallback {
    public:
        virtual ~OpenDockCoverCallback() {}
        virtual void onOpenDockCover(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OpenDockCoverResult& result) = 0;
    };

    class OpenDockCoverProgressCallback {
    public:
        virtual ~OpenDockCoverProgressCallback() {}
        virtual void onOpenDockCoverProgress(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OpenDockCoverProgressResult& result) = 0;
    };

    class SubscribeDockCoverOpenProgressCallback {
    public:
        virtual ~SubscribeDockCoverOpenProgressCallback() {}
        virtual void onSubscribeDockCoverOpenProgress(flight_hub_sdk::OpenDockCoverResult result) = 0;
    };

    class SubscribeDockCoverCloseProgressCallback {
    public:
        virtual ~SubscribeDockCoverCloseProgressCallback() {}
        virtual void onSubscribeDockCoverCloseProgress(flight_hub_sdk::CloseDockCoverResult result) = 0;
    };

    class SubscribeDeviceRebootProgressCallback {
    public:
        virtual ~SubscribeDeviceRebootProgressCallback() {}
        virtual void onSubscribeDeviceRebootProgress(flight_hub_sdk::DeviceRebootResult result) = 0;
    };

    class SubscribeCalibrationProgressCallback {
    public:
        virtual ~SubscribeCalibrationProgressCallback() {}
        virtual void onSubscribeCalibrationProgress(flight_hub_sdk::RtkCalibrationResult result) = 0;
    };

    class SubscribeDeviceFormatProgressCallback {
    public:
        virtual ~SubscribeDeviceFormatProgressCallback() {}
        virtual void onSubscribeDeviceFormatProgress(flight_hub_sdk::DeviceFormatResult result) = 0;
    };

    class SubscribeDroneOpenProgressCallback {
    public:
        virtual ~SubscribeDroneOpenProgressCallback() {}
        virtual void onSubscribeDroneOpenProgress(flight_hub_sdk::OpenDroneResult result) = 0;
    };

    class SubscribeDroneCloseProgressCallback {
    public:
        virtual ~SubscribeDroneCloseProgressCallback() {}
        virtual void onSubscribeDroneCloseProgress(flight_hub_sdk::CloseDroneResult result) = 0;
    };

    class SubscribeDroneFormatProgressCallback {
    public:
        virtual ~SubscribeDroneFormatProgressCallback() {}
        virtual void onSubscribeDroneFormatProgress(flight_hub_sdk::DroneFormatResult result) = 0;
    };

    class SubscribeNetworkTestProgressCallback {
    public:
        virtual ~SubscribeNetworkTestProgressCallback() {}
        virtual void onSubscribeNetworkTestProgress(flight_hub_sdk::NetworkTestProgressResult result) = 0;
    };

    class SubscribeDroneChargeOpenProgressCallback {
    public:
        virtual ~SubscribeDroneChargeOpenProgressCallback() {}
        virtual void onSubscribeDroneChargeOpenProgress(flight_hub_sdk::OpenChargeResult result) = 0;
    };

    class SubscribeDroneChargeCloseProgressCallback {
    public:
        virtual ~SubscribeDroneChargeCloseProgressCallback() {}
        virtual void onSubscribeDroneChargeCloseProgress(flight_hub_sdk::CloseChargeResult result) = 0;
    };

    class SubscribeDeviceEsimOperatorSwitchProgressCallback {
    public:
        virtual ~SubscribeDeviceEsimOperatorSwitchProgressCallback() {}
        virtual void onSubscribeDeviceEsimOperatorSwitchProgress(flight_hub_sdk::EsimOperatorSwitchResult result) = 0;
    };

    class SubscribeDeviceSimSlotSwitchProgressCallback {
    public:
        virtual ~SubscribeDeviceSimSlotSwitchProgressCallback() {}
        virtual void onSubscribeDeviceSimSlotSwitchProgress(flight_hub_sdk::SimSlotSwitchResult result) = 0;
    };

    class DeviceCloudEnvironmentSwitchProgressCallback {
    public:
        virtual ~DeviceCloudEnvironmentSwitchProgressCallback() {}
        virtual void onDeviceCloudEnvironmentSwitchProgress(flight_hub_sdk::CloudEnvironmentSwitchResult result) = 0;
    };


    class CloseDockCoverCallback {
    public:
        virtual ~CloseDockCoverCallback() {}
        virtual void onCloseDockCover(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CloseDockCoverResult& result) = 0;
    };

    class CloseDockCoverProgressCallback {
    public:
        virtual ~CloseDockCoverProgressCallback() {}
        virtual void onCloseDockCoverProgress(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CloseDockCoverProgressResult& result) = 0;
    };

    class DeviceRebootCallback {
    public:
        virtual ~DeviceRebootCallback() {}
        virtual void onDeviceReboot(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRebootResult& result) = 0;
    };

    class DeviceRebootProgressCallback {
    public:
        virtual ~DeviceRebootProgressCallback() {}
        virtual void onDeviceRebootProgress(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRebootProgressResult& result) = 0;
    };

    class DeviceFormatCallback {
    public:
        virtual ~DeviceFormatCallback() {}
        virtual void onDeviceFormat(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceFormatResult& result) = 0;
    };

    class DeviceFormatProgressCallback {
    public:
        virtual ~DeviceFormatProgressCallback() {}
        virtual void onDeviceFormatProgress(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceFormatProgressResult& result) = 0;
    };

    class OpenDroneCallback {
    public:
        virtual ~OpenDroneCallback() {}
        virtual void onOpenDrone(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OpenDroneResult& result) = 0;
    };

    class CloseDroneCallback {
    public:
        virtual ~CloseDroneCallback() {}
        virtual void onCloseDrone(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CloseDroneResult& result) = 0;
    };

    class OpenChargeCallback {
    public:
        virtual ~OpenChargeCallback() {}
        virtual void onOpenCharge(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OpenChargeResult& result) = 0;
    };

    class CloseChargeCallback {
    public:
        virtual ~CloseChargeCallback() {}
        virtual void onCloseCharge(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CloseChargeResult& result) = 0;
    };

    class DroneFormatCallback {
    public:
        virtual ~DroneFormatCallback() {}
        virtual void onDroneFormat(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DroneFormatResult& result) = 0;
    };

    // 媒体服务模块相关回调类
    class GetMediaFilesListCallback {
    public:
        virtual ~GetMediaFilesListCallback() {}
        virtual void onGetMediaFilesList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesListResults& results) = 0;
    };

    class GetMediaFilesDownloadUrlCallback {
    public:
        virtual ~GetMediaFilesDownloadUrlCallback() {}
        virtual void onGetMediaFilesDownloadUrl(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesDownloadUrlResults& results) = 0;
    };

    class GetMediaFilesDownloadUrlListCallback {
    public:
        virtual ~GetMediaFilesDownloadUrlListCallback() {}
        virtual void onGetMediaFilesDownloadUrlList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesDownloadUrlListResults& results) = 0;
    };

    class MediaFilePostProcessCallback {
    public:
        virtual ~MediaFilePostProcessCallback() {}
        virtual void onMediaFilePostProcess(const flight_hub_sdk::MediaFilePostProcess& msg) = 0;
    };

    class MediaFileDeviceUploadCallback {
    public:
        virtual ~MediaFileDeviceUploadCallback() {}
        virtual void onMediaFileDeviceUpload(const flight_hub_sdk::MediaFileDeviceUpload& msg) = 0;
    };

    class GetMediaUploadStsCallback {
    public:
        virtual ~GetMediaUploadStsCallback() {}
        virtual void onGetMediaUploadSts(flight_hub_sdk::FlightHubError err,
                                         const flight_hub_sdk::GetMediaUploadStsResult& result) = 0;
    };

    class MediaUploadCallbackCallback {
    public:
        virtual ~MediaUploadCallbackCallback() {}
        virtual void onMediaUploadCallback(flight_hub_sdk::FlightHubError err,
                                           const flight_hub_sdk::MediaUploadCallbackResult& result) = 0;
    };

    class UploadGeoMediaFileCallback {
    public:
        virtual ~UploadGeoMediaFileCallback() {}
        virtual void onUploadGeoMediaFile(flight_hub_sdk::FlightHubError err,
                                          const flight_hub_sdk::UploadGeoMediaFileResult& result) = 0;
    };

    class GetMediaFileDetailByIdCallback {
    public:
        virtual ~GetMediaFileDetailByIdCallback() {}
        virtual void onGetMediaFileDetailById(flight_hub_sdk::FlightHubError err,
                                              const flight_hub_sdk::GetMediaFileDetailByIdResult& result) = 0;
    };

    class GetWaylineEditorStsCallback {
    public:
        virtual ~GetWaylineEditorStsCallback() {}
        virtual void onGetWaylineEditorSts(flight_hub_sdk::FlightHubError err,
                                           const flight_hub_sdk::GetWaylineEditorStsResult& result) = 0;
    };

    // Terra Build 模块相关回调类
    class TerraBuildReconstructionSettingsCallback {
    public:
        virtual ~TerraBuildReconstructionSettingsCallback() {}
        virtual void onGetReconstructionSettings(flight_hub_sdk::FlightHubError err,
                                                 const flight_hub_sdk::TerraBuildSettingsResult& result) = 0;
    };
    class TerraBuildQualityReportCallback {
    public:
        virtual ~TerraBuildQualityReportCallback() {}
        virtual void onGetQualityReport(flight_hub_sdk::FlightHubError err,
                                        const flight_hub_sdk::TerraBuildQualityReportResult& result) = 0;
    };
    class TerraBuildModelFileListCallback {
    public:
        virtual ~TerraBuildModelFileListCallback() {}
        virtual void onGetModelFileList(flight_hub_sdk::FlightHubError err,
                                        const flight_hub_sdk::TerraBuildModelFileListResult& result) = 0;
    };
    class TerraBuildModelFileUrlCallback {
    public:
        virtual ~TerraBuildModelFileUrlCallback() {}
        virtual void onGetModelFileUrl(flight_hub_sdk::FlightHubError err,
                                       const flight_hub_sdk::TerraBuildModelFileUrlResult& result) = 0;
    };
    class TerraBuildCreateReconstructionCallback {
    public:
        virtual ~TerraBuildCreateReconstructionCallback() {}
        virtual void onCreateReconstruction(flight_hub_sdk::FlightHubError err,
                                            const flight_hub_sdk::TerraBuildCreateReconstructionResult& result) = 0;
    };
    class TerraBuildSfmResultCallback {
    public:
        virtual ~TerraBuildSfmResultCallback() {}
        virtual void onGetSfmResult(flight_hub_sdk::FlightHubError err,
                                    const flight_hub_sdk::TerraBuildSfmResult& result) = 0;
    };
    class TerraBuildCreatePanoramaCallback {
    public:
        virtual ~TerraBuildCreatePanoramaCallback() {}
        virtual void onCreatePanorama(flight_hub_sdk::FlightHubError err,
                                      const flight_hub_sdk::TerraBuildCreatePanoramaResult& result) = 0;
    };
    class TerraBuildReconstructionImagesCallback {
    public:
        virtual ~TerraBuildReconstructionImagesCallback() {}
        virtual void onGetReconstructionImages(flight_hub_sdk::FlightHubError err,
                                               const flight_hub_sdk::TerraBuildReconstructionImagesResult& result) = 0;
    };
    class TerraBuildReconstructionStatusChangedCallback {
    public:
        virtual ~TerraBuildReconstructionStatusChangedCallback() {}
        virtual void onReconstructionStatusChanged(const flight_hub_sdk::TerraBuildReconstructionStatusChanged& msg) = 0;
    };

    // ── Element 模块 WS 回调类 ─────────────────────────────────────────────
    class ElementCreateCallback {
    public:
        virtual ~ElementCreateCallback() {}
        virtual void onElementCreate(const flight_hub_sdk::ElementCreateMsg& msg) = 0;
    };
    class ElementUpdateCallback {
    public:
        virtual ~ElementUpdateCallback() {}
        virtual void onElementUpdate(const flight_hub_sdk::ElementUpdateMsg& msg) = 0;
    };
    class ElementDeleteCallback {
    public:
        virtual ~ElementDeleteCallback() {}
        virtual void onElementDelete(const flight_hub_sdk::ElementDeleteMsg& msg) = 0;
    };
    // ── Element 模块 HTTP 回调类 ───────────────────────────────────────────
    class ElementCreateElementCallback {
    public:
        virtual ~ElementCreateElementCallback() {}
        virtual void onCreateElement(flight_hub_sdk::FlightHubError err,
                                     const flight_hub_sdk::CreateElementResult& result) = 0;
    };
    class ElementUpdateElementCallback {
    public:
        virtual ~ElementUpdateElementCallback() {}
        virtual void onUpdateElement(flight_hub_sdk::FlightHubError err,
                                     const flight_hub_sdk::UpdateElementResult& result) = 0;
    };
    class ElementListHierarchyCallback {
    public:
        virtual ~ElementListHierarchyCallback() {}
        virtual void onListElementHierarchy(flight_hub_sdk::FlightHubError err,
                                            const flight_hub_sdk::ListElementHierarchyResult& result) = 0;
    };
    class ElementSetVisibilityCallback {
    public:
        virtual ~ElementSetVisibilityCallback() {}
        virtual void onSetElementVisibility(flight_hub_sdk::FlightHubError err,
                                            const flight_hub_sdk::SetElementVisibilityResult& result) = 0;
    };
    class ElementDeleteElementCallback {
    public:
        virtual ~ElementDeleteElementCallback() {}
        virtual void onDeleteElement(flight_hub_sdk::FlightHubError err,
                                     const flight_hub_sdk::DeleteElementResult& result) = 0;
    };
    // Terra 模型管理新增回调类
    class TerraUploadCallbackCallback {
    public:
        virtual ~TerraUploadCallbackCallback() {}
        virtual void onTerraUploadCallback(flight_hub_sdk::FlightHubError err,
                                           const flight_hub_sdk::TerraUploadCallbackResult& result) = 0;
    };

    class TerraShowOnMapCallback {
    public:
        virtual ~TerraShowOnMapCallback() {}
        virtual void onTerraShowOnMap(flight_hub_sdk::FlightHubError err,
                                      const flight_hub_sdk::TerraShowOnMapResult& result) = 0;
    };

    class TerraUnzipProgressCallback {
    public:
        virtual ~TerraUnzipProgressCallback() {}
        virtual void onTerraUnzipProgress(const flight_hub_sdk::TerraUnzipProgressMsg& msg) = 0;
    };

    class TerraModelStatusChangedCallback {
    public:
        virtual ~TerraModelStatusChangedCallback() {}
        virtual void onTerraModelStatusChanged(const flight_hub_sdk::TerraModelStatusChangedMsg& msg) = 0;
    };

    class TerraFileShowOnMapCallback {
    public:
        virtual ~TerraFileShowOnMapCallback() {}
        virtual void onTerraFileShowOnMap(const flight_hub_sdk::TerraFileShowOnMapMsg& msg) = 0;
    };

    class TerraMediaFileHideOnMapCallback {
    public:
        virtual ~TerraMediaFileHideOnMapCallback() {}
        virtual void onTerraMediaFileHideOnMap(const flight_hub_sdk::TerraMediaFileHideOnMapMsg& msg) = 0;
    };

    class TerraMediaFileShowOnMapCallback {
    public:
        virtual ~TerraMediaFileShowOnMapCallback() {}
        virtual void onTerraMediaFileShowOnMap(const flight_hub_sdk::TerraMediaFileShowOnMapMsg& msg) = 0;
    };

    // GetModelDetail / SetLasWkt 回调类
    class TerraGetModelDetailCallback {
    public:
        virtual ~TerraGetModelDetailCallback() {}
        // 所有字段通过 Java 友好的 TerraModelDetailJavaResult 结构体传递
        virtual void onGetModelDetail(flight_hub_sdk::FlightHubError err,
                                      const terra_build_swig::TerraModelDetailJavaResult& result) = 0;
    };

    class TerraSetLasWktCallback {
    public:
        virtual ~TerraSetLasWktCallback() {}
        virtual void onSetLasWkt(flight_hub_sdk::FlightHubError err) = 0;
    };

    // 安全服务模块相关回调类
    class SearchSignalMapsCallback {
    public:
        virtual ~SearchSignalMapsCallback() {}
        virtual void onSearchSignalMaps(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SignalMapsResults& result) = 0;
    };

    class CreateCustomizedFlightAreasCallback {
    public:
        virtual ~CreateCustomizedFlightAreasCallback() {}
        virtual void onCreateCustomizedFlightAreas(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateFlightAreasResults& result) = 0;
    };

    class GetCustomizedFlightAreasCallback {
    public:
        virtual ~GetCustomizedFlightAreasCallback() {}
        virtual void onGetCustomizedFlightAreas(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightAreasResults& result) = 0;
    };

    class UpdateCustomizedFlightAreasCallback {
    public:
        virtual ~UpdateCustomizedFlightAreasCallback() {}
        virtual void onUpdateCustomizedFlightAreas(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UpdateFlightAreasResults& result) = 0;
    };

    class SyncCustomizedFlightAreasCallback {
    public:
        virtual ~SyncCustomizedFlightAreasCallback() {}
        virtual void onSyncCustomizedFlightAreas(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SyncFlightAreasResults& result) = 0;
    };

    class GetFlightAreasSyncStatusCallback {
    public:
        virtual ~GetFlightAreasSyncStatusCallback() {}
        virtual void onGetFlightAreasSyncStatus(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetAreasSyncStatusResults& result) = 0;
    };

    class GetOfflineMapsModelInfoCallback {
    public:
        virtual ~GetOfflineMapsModelInfoCallback() {}
        virtual void onGetOfflineMapsModelInfo(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetOfflineMapsModelInfoResults& result) = 0;
    };

    class SyncOfflineMapsCallback {
    public:
        virtual ~SyncOfflineMapsCallback() {}
        virtual void onSyncOfflineMaps(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OfflineMapsSyncResults& result) = 0;
    };

    class GetOfflineMapsSyncStatusCallback {
    public:
        virtual ~GetOfflineMapsSyncStatusCallback() {}
        virtual void onGetOfflineMapsSyncStatus(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetOfflineMapsSyncStatusResults& result) = 0;
    };

    // 飞行任务记录模块相关回调接口
    class ExportFlightRecordsCallback {
    public:
        virtual ~ExportFlightRecordsCallback() {}
        virtual void onExportFlightRecords(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFlightRecordsResults& results) = 0;
    };

    class GetExportFlightRecordsProgressCallback {
    public:
        virtual ~GetExportFlightRecordsProgressCallback() {}
        virtual void onGetExportFlightRecordsProgress(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetExportFlightRecordsProgressResults& results) = 0;
    };

    class StartNetworkTestCallback {
    public:
        virtual ~StartNetworkTestCallback() {}
        virtual void onStartNetworkTest(flight_hub_sdk::FlightHubError err, flight_hub_sdk::NetworkTestProgressResult result) = 0;
    };

    class StopNetworkTestCallback {
    public:
        virtual ~StopNetworkTestCallback() {}
        virtual void onStopNetworkTest(flight_hub_sdk::FlightHubError err) = 0;
    };

    class GetNetworkTestProgressCallback {
    public:
        virtual ~GetNetworkTestProgressCallback() {}
        virtual void onGetNetworkTestProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::NetworkTestProgressResult result) = 0;
    };

    class RtkCalibrationCallback {
    public:
        virtual ~RtkCalibrationCallback() {}
        virtual void onRtkCalibration(flight_hub_sdk::FlightHubError err, flight_hub_sdk::RtkCalibrationResult result) = 0;
    };

    class SetObstacleAvoidanceCallback {
    public:
        virtual ~SetObstacleAvoidanceCallback() {}
        virtual void onSetObstacleAvoidance(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetObstacleAvoidanceResult result) = 0;
    };

    class SetDistanceLimitCallback {
    public:
        virtual ~SetDistanceLimitCallback() {}
        virtual void onSetDistanceLimit(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetDistanceLimitResult result) = 0;
    };

    class SetHeightLimitCallback {
    public:
        virtual ~SetHeightLimitCallback() {}
        virtual void onSetHeightLimit(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetHeightLimitResult result) = 0;
    };

    class SetRemainingPowerForRthCallback {
    public:
        virtual ~SetRemainingPowerForRthCallback() {}
        virtual void onSetRemainingPowerForRth(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetRemainingPowerForRthResult result) = 0;
    };

    class SetNightLightsStateCallback {
    public:
        virtual ~SetNightLightsStateCallback() {}
        virtual void onSetNightLightsState(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetNightLightsStateResult result) = 0;
    };

    class SwitchAlarmStateCallback {
    public:
        virtual ~SwitchAlarmStateCallback() {}
        virtual void onSwitchAlarmState(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SwitchAlarmStateResult result) = 0;
    };

    class EsimOperatorSwitchCallback {
    public:
        virtual ~EsimOperatorSwitchCallback() {}
        virtual void onEsimOperatorSwitch(flight_hub_sdk::FlightHubError err, flight_hub_sdk::EsimOperatorSwitchResult result) = 0;
    };

    class SimSlotSwitchCallback {
    public:
        virtual ~SimSlotSwitchCallback() {}
        virtual void onSimSlotSwitch(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SimSlotSwitchResult result) = 0;
    };

    class SetBatteryStoreModeCallback {
    public:
        virtual ~SetBatteryStoreModeCallback() {}
        virtual void onSetBatteryStoreMode(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetBatteryStoreModeResult result) = 0;
    };

    class ManualBatterySwapCallback {
    public:
        virtual ~ManualBatterySwapCallback() {}
        virtual void onManualBatterySwap(flight_hub_sdk::FlightHubError err, flight_hub_sdk::ManualBatterySwapResult result) = 0;
    };

    class ManualBatterySwapProgressCallback {
    public:
        virtual ~ManualBatterySwapProgressCallback() {}
        virtual void onManualBatterySwapProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::ManualBatterySwapProgressResult result) = 0;
    };

    class DonglePowerSwitchCallback {
    public:
        virtual ~DonglePowerSwitchCallback() {}
        virtual void onDonglePowerSwitch(flight_hub_sdk::FlightHubError err, flight_hub_sdk::DonglePowerSwitchResult result) = 0;
    };

    class LowPowerSwitchCallback {
    public:
        virtual ~LowPowerSwitchCallback() {}
        virtual void onLowPowerSwitch(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LowPowerSwitchResult result) = 0;
    };

    class EsimOperatorSwitchProgressCallback {
    public:
        virtual ~EsimOperatorSwitchProgressCallback() {}
        virtual void onEsimOperatorSwitchProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::EsimOperatorSwitchProgressResult result) = 0;
    };

    // 中继站调试接口回调类
    // 无 data 响应（只传 err_code）
    class SetRelayBroadcastHideCallback {
    public:
        virtual ~SetRelayBroadcastHideCallback() {}
        virtual void onSetRelayBroadcastHide(flight_hub_sdk::FlightHubError err) = 0;
    };

    // Geo Caging 回调类
    class SetGeoCagingEnableCallback {
    public:
        virtual ~SetGeoCagingEnableCallback() {}
        virtual void onSetGeoCagingEnable(flight_hub_sdk::FlightHubError err) = 0;
    };

    class SetRelaySSIDCallback {
    public:
        virtual ~SetRelaySSIDCallback() {}
        virtual void onSetRelaySSID(flight_hub_sdk::FlightHubError err) = 0;
    };

    class SetRelayPasswordCallback {
    public:
        virtual ~SetRelayPasswordCallback() {}
        virtual void onSetRelayPassword(flight_hub_sdk::FlightHubError err) = 0;
    };

    class SetRelaySearchModeCallback {
    public:
        virtual ~SetRelaySearchModeCallback() {}
        virtual void onSetRelaySearchMode(flight_hub_sdk::FlightHubError err) = 0;
    };

    // 有 data 响应（传 err_code + result）
    class SetRelayOccupiedStatusCallback {
    public:
        virtual ~SetRelayOccupiedStatusCallback() {}
        virtual void onSetRelayOccupiedStatus(flight_hub_sdk::FlightHubError err, flight_hub_sdk::SetRelayOccupiedStatusResult result) = 0;
    };

    class RelayRebootCallback {
    public:
        virtual ~RelayRebootCallback() {}
        virtual void onRelayReboot(flight_hub_sdk::FlightHubError err, flight_hub_sdk::RelayRebootResult result) = 0;
    };

    class RelayDisconnectCallback {
    public:
        virtual ~RelayDisconnectCallback() {}
        virtual void onRelayDisconnect(flight_hub_sdk::FlightHubError err, flight_hub_sdk::RelayDisconnectResult result) = 0;
    };

    class QueryHistoryHMSDataCallback {
    public:
        virtual ~QueryHistoryHMSDataCallback() {}
        virtual void onQueryHistoryHMSData(flight_hub_sdk::FlightHubError err, flight_hub_sdk::HistoryHmsDataResult result) = 0;
    };

    class QueryDeviceLogListCallback {
    public:
        virtual ~QueryDeviceLogListCallback() {}
        virtual void onQueryDeviceLogList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::DeviceLogListResult result) = 0;
    };

    class CreateDeviceLogReportCallback {
    public:
        virtual ~CreateDeviceLogReportCallback() {}
        virtual void onCreateDeviceLogReport(flight_hub_sdk::FlightHubError err, flight_hub_sdk::CreateDeviceLogReportResult result) = 0;
    };

    class LogReportListCallback {
    public:
        virtual ~LogReportListCallback() {}
        virtual void onLogReportList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LogReportListResult result) = 0;
    };

    class LogReportOperationCallback {
    public:
        virtual ~LogReportOperationCallback() {}
        virtual void onLogReportOperation(flight_hub_sdk::FlightHubError err) = 0;
    };

    class UploadLogAppendFileCallback {
    public:
        virtual ~UploadLogAppendFileCallback() {}
        virtual void onUploadLogAppendFile(flight_hub_sdk::FlightHubError err) = 0;
    };

    // 固件管理相关回调接口
    class GetLatestFirmwareReleaseNoteCallback {
    public:
        virtual ~GetLatestFirmwareReleaseNoteCallback() {}
        virtual void onGetLatestFirmwareReleaseNote(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareReleaseNoteListResult result) = 0;
    };

    class InvokeFirmwareUpgradeCallback {
    public:
        virtual ~InvokeFirmwareUpgradeCallback() {}
        virtual void onInvokeFirmwareUpgrade(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareUpgradeResult result) = 0;
    };

    class QueryFirmwareUpgradeProgressCallback {
    public:
        virtual ~QueryFirmwareUpgradeProgressCallback() {}
        virtual void onQueryFirmwareUpgradeProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareUpgradeProgressResult result) = 0;
    };

    class UploadFirmwareFileCallback {
    public:
        virtual ~UploadFirmwareFileCallback() {}
        virtual void onUploadFirmwareFile(flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadFileResults results) = 0;
    };

    class ReleaseFirmwareCallback {
    public:
        virtual ~ReleaseFirmwareCallback() {}
        virtual void onReleaseFirmware(flight_hub_sdk::FlightHubError err) = 0;
    };

    class UnReleaseFirmwareCallback {
    public:
        virtual ~UnReleaseFirmwareCallback() {}
        virtual void onUnReleaseFirmware(flight_hub_sdk::FlightHubError err) = 0;
    };

    class DeleteFirmwareCallback {
    public:
        virtual ~DeleteFirmwareCallback() {}
        virtual void onDeleteFirmware(flight_hub_sdk::FlightHubError err) = 0;
    };

    class QueryFirmwareListCallback {
    public:
        virtual ~QueryFirmwareListCallback() {}
        virtual void onQueryFirmwareList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareListResult result) = 0;
    };

    class QueryFirmwareCompatibilityConfigCallback {
    public:
        virtual ~QueryFirmwareCompatibilityConfigCallback() {}
        virtual void onQueryFirmwareCompatibilityConfig(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareCompatibilityConfigResult result) = 0;
    };

    class CheckFirmwareStatusCallback {
    public:
        virtual ~CheckFirmwareStatusCallback() {}
        virtual void onCheckFirmwareStatus(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareStatusListResult result) = 0;
    };

    class SubmitFirmwareInfoCallback {
    public:
        virtual ~SubmitFirmwareInfoCallback() {}
        virtual void onSubmitFirmwareInfo(flight_hub_sdk::FlightHubError err) = 0;
    };

    // 解锁证书管理相关回调接口
    class GetUnlockLicenseListCallback {
    public:
        virtual ~GetUnlockLicenseListCallback() {}
        virtual void onGetUnlockLicenseList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::QueryDeviceUnlockLicenseListResult result) = 0;
    };

    class SwitchUnlockLicenseCallback {
    public:
        virtual ~SwitchUnlockLicenseCallback() {}
        virtual void onSwitchUnlockLicense(flight_hub_sdk::FlightHubError err) = 0;
    };

    class CheckUnlockLicenseCallback {
    public:
        virtual ~CheckUnlockLicenseCallback() {}
        virtual void onCheckUnlockLicense(flight_hub_sdk::FlightHubError err, flight_hub_sdk::CheckUnlockLicenseResult result) = 0;
    };

    class SubmitUnlockLicenseCallback {
    public:
        virtual ~SubmitUnlockLicenseCallback() {}
        virtual void onSubmitUnlockLicense(flight_hub_sdk::FlightHubError err) = 0;
    };

    class ClearDeviceUnlockLicenseCallback {
    public:
        virtual ~ClearDeviceUnlockLicenseCallback() {}
        virtual void onClearDeviceUnlockLicense(flight_hub_sdk::FlightHubError err) = 0;
    };

    class UploadUnlockLicenseFileCallback {
    public:
        virtual ~UploadUnlockLicenseFileCallback() {}
        virtual void onUploadUnlockLicenseFile(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UploadUnlockLicenseFileResult& result) = 0;
    };

    // 航线管理相关回调接口
    class GetWaylineListCallback {
    public:
        virtual ~GetWaylineListCallback() {}
        virtual void onGetWaylineList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetWaylineListResult result) = 0;
    };

    class GetWaylineDetailCallback {
    public:
        virtual ~GetWaylineDetailCallback() {}
        virtual void onGetWaylineDetail(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetWaylineDetailResult result) = 0;
    };

    class BatchDeleteWaylinesCallback {
    public:
        virtual ~BatchDeleteWaylinesCallback() {}
        virtual void onBatchDeleteWaylines(flight_hub_sdk::FlightHubError err, flight_hub_sdk::BatchDeleteWaylinesResult result) = 0;
    };

    class UpdateWaylineCallback {
    public:
        virtual ~UpdateWaylineCallback() {}
        virtual void onUpdateWayline(flight_hub_sdk::FlightHubError err, flight_hub_sdk::UpdateWaylineResult result) = 0;
    };

    class SubmitWaylineInfoCallback {
    public:
        virtual ~SubmitWaylineInfoCallback() {}
        virtual void onSubmitWaylineInfo(flight_hub_sdk::FlightHubError err) = 0;
    };

    class BatchDownloadWaylinesCallback {
    public:
        virtual ~BatchDownloadWaylinesCallback() {}
        virtual void onBatchDownloadWaylines(flight_hub_sdk::FlightHubError err, flight_hub_sdk::BatchDownloadWaylinesResult result) = 0;
    };

    class DuplicateWaylineCallback {
    public:
        virtual ~DuplicateWaylineCallback() {}
        virtual void onDuplicateWayline(flight_hub_sdk::FlightHubError err, flight_hub_sdk::DuplicateWaylineResult result) = 0;
    };

    class CopyWaylineCallback {
    public:
        virtual ~CopyWaylineCallback() {}
        virtual void onCopyWayline(flight_hub_sdk::FlightHubError err, flight_hub_sdk::CopyWaylineResult result) = 0;
    };

    class ConcatWaylineCallback {
    public:
        virtual ~ConcatWaylineCallback() {}
        virtual void onConcatWayline(flight_hub_sdk::FlightHubError err, flight_hub_sdk::ConcatWaylineResult result) = 0;
    };

    class ImportKmzCallback {
    public:
        virtual ~ImportKmzCallback() {}
        virtual void onImportKmz(flight_hub_sdk::FlightHubError err, flight_hub_sdk::ImportKmzResult result) = 0;
    };

    // AI Inside 机载算法管理回调接口
    class GetAIInsideModelsCallback {
    public:
        virtual ~GetAIInsideModelsCallback() {}
        virtual void onGetAIInsideModels(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelsResult result) = 0;
    };

    class GetAIInsideModelDevicesCallback {
    public:
        virtual ~GetAIInsideModelDevicesCallback() {}
        virtual void onGetAIInsideModelDevices(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelDevicesResult result) = 0;
    };

    class GetAIInsideModelDevicesSummaryCallback {
    public:
        virtual ~GetAIInsideModelDevicesSummaryCallback() {}
        virtual void onGetAIInsideModelDevicesSummary(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelDevicesSummaryResult result) = 0;
    };

    class OperateAIInsideModelCallback {
    public:
        virtual ~OperateAIInsideModelCallback() {}
        virtual void onOperateAIInsideModel(flight_hub_sdk::FlightHubError err, flight_hub_sdk::OperateAIInsideModelResult result) = 0;
    };

    class CreateAIInsideAlgorithmCallback {
    public:
        virtual ~CreateAIInsideAlgorithmCallback() {}
        virtual void onCreateAIInsideAlgorithm(flight_hub_sdk::FlightHubError err, flight_hub_sdk::CreateAIInsideAlgorithmResult result) = 0;
    };

    class FileUploadAIInsideAlgorithmCallback {
    public:
        virtual ~FileUploadAIInsideAlgorithmCallback() {}
        virtual void onFileUploadAIInsideAlgorithm(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FileUploadAIInsideAlgorithmResult result) = 0;
    };

    class DeleteAIInsideAlgorithmCallback {
    public:
        virtual ~DeleteAIInsideAlgorithmCallback() {}
        virtual void onDeleteAIInsideAlgorithm(flight_hub_sdk::FlightHubError err) = 0;
    };

    class UploadAlgorithmFileCallback {
    public:
        virtual ~UploadAlgorithmFileCallback() {}
        virtual void onUploadAlgorithmFile(flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadAIInsideAlgorithmFileResult result) = 0;
    };

    // AI 告警管理回调接口
    class PostAiAlertDeviceSettingCallback {
    public:
        virtual ~PostAiAlertDeviceSettingCallback() {}
        virtual void onPostAiAlertDeviceSetting(flight_hub_sdk::FlightHubError err, flight_hub_sdk::PostAiAlertDeviceSettingResult result) = 0;
    };

    class GetAiAlertDeviceSettingCallback {
    public:
        virtual ~GetAiAlertDeviceSettingCallback() {}
        virtual void onGetAiAlertDeviceSetting(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertDeviceSettingResult result) = 0;
    };

    class PostAiAlertAlgorithmSettingCallback {
    public:
        virtual ~PostAiAlertAlgorithmSettingCallback() {}
        virtual void onPostAiAlertAlgorithmSetting(flight_hub_sdk::FlightHubError err, flight_hub_sdk::PostAiAlertAlgorithmSettingResult result) = 0;
    };

    class GetAiAlertAlgorithmSettingCallback {
    public:
        virtual ~GetAiAlertAlgorithmSettingCallback() {}
        virtual void onGetAiAlertAlgorithmSetting(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertAlgorithmSettingResult result) = 0;
    };

    class GetAiAlertFlightsCallback {
    public:
        virtual ~GetAiAlertFlightsCallback() {}
        virtual void onGetAiAlertFlights(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertFlightsResult result) = 0;
    };

    class GetAiAlertRecordsCallback {
    public:
        virtual ~GetAiAlertRecordsCallback() {}
        virtual void onGetAiAlertRecords(flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertRecordsResult result) = 0;
    };

    // 直播转发回调
    class CreateLiveSyncCallback {
    public:
        virtual ~CreateLiveSyncCallback() {}
        virtual void onCreateLiveSync(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncCreateResult result) = 0;
    };

    class DeleteLiveSyncCallback {
    public:
        virtual ~DeleteLiveSyncCallback() {}
        virtual void onDeleteLiveSync(flight_hub_sdk::FlightHubError err) = 0;
    };

    class UpdateLiveSyncCallback {
    public:
        virtual ~UpdateLiveSyncCallback() {}
        virtual void onUpdateLiveSync(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncItem result) = 0;
    };

    class GetLiveSyncListCallback {
    public:
        virtual ~GetLiveSyncListCallback() {}
        virtual void onGetLiveSyncList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncListResult result) = 0;
    };

    class GetLiveSyncDevicesV2Callback {
    public:
        virtual ~GetLiveSyncDevicesV2Callback() {}
        virtual void onGetLiveSyncDevicesV2(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncDevicesV2Result result) = 0;
    };

    class GetLiveSyncSummaryCallback {
    public:
        virtual ~GetLiveSyncSummaryCallback() {}
        virtual void onGetLiveSyncSummary(flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncSummaryResult result) = 0;
    };

    // 飞行器 HSI 信息（障碍物感知/避障传感器数据）
    class HsiInfoCallback {
    public:
        virtual ~HsiInfoCallback() {}
        virtual void onHsiInfo(const flight_hub_sdk::HsiInfo& info) = 0;
    };

    class SetFlightSmartPlanModeCallback {
    public:
        virtual ~SetFlightSmartPlanModeCallback() {}
        virtual void onSetFlightSmartPlanMode(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightSmartPlanModeResult& result) = 0;
    };

    class SetOutOfControlActionCallback {
    public:
        virtual ~SetOutOfControlActionCallback() {}
        virtual void onSetOutOfControlAction(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightOutOfControlActionResult& result) = 0;
    };

    class SetFlightTaskHeightCallback {
    public:
        virtual ~SetFlightTaskHeightCallback() {}
        virtual void onSetFlightTaskHeight(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightCommanderHeightResult& result) = 0;
    };

    class SetRthAltitudeCallback {
    public:
        virtual ~SetRthAltitudeCallback() {}
        virtual void onSetRthAltitude(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightRthAltitudeResult& result) = 0;
    };

    class SetAirTransferEnableCallback {
    public:
        virtual ~SetAirTransferEnableCallback() {}
        virtual void onSetAirTransferEnable(flight_hub_sdk::FlightHubError err) = 0;
    };

    class GenerateTryFlightWaylineCallback {
    public:
        virtual ~GenerateTryFlightWaylineCallback() {}
        virtual void onGenerateTryFlightWayline(const flight_hub_sdk::GenerateTryFlightWaylineResponse& response, flight_hub_sdk::FlightHubError err) = 0;
    };

    class CreateTestFlightTaskCallback {
    public:
        virtual ~CreateTestFlightTaskCallback() {}
        virtual void onCreateTestFlightTask(const flight_hub_sdk::CreateTestFlightTaskResponse& response, flight_hub_sdk::FlightHubError err) = 0;
    };

    // 报修管理模块相关回调类
    class GetRepairConfigsCallback {
    public:
        virtual ~GetRepairConfigsCallback() {}
        virtual void onGetRepairConfigs(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRepairConfigResults& result) = 0;
    };

    class CreateMaintenanceOrderCallback {
    public:
        virtual ~CreateMaintenanceOrderCallback() {}
        virtual void onCreateMaintenanceOrder(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateMaintenanceOrderResults& result) = 0;
    };

    class GetMaintenanceOrderDetailCallback {
    public:
        virtual ~GetMaintenanceOrderDetailCallback() {}
        virtual void onGetMaintenanceOrderDetail(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::MaintenanceOrderDetailResults& result) = 0;
    };

    class GetMaintenanceOrdersListCallback {
    public:
        virtual ~GetMaintenanceOrdersListCallback() {}
        virtual void onGetMaintenanceOrdersList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::MaintenanceOrdersListResults& result) = 0;
    };

    // 诊断管理模块相关回调类
    class CreateSelfDiagnosisCallback {
    public:
        virtual ~CreateSelfDiagnosisCallback() {}
        virtual void onCreateSelfDiagnosis(flight_hub_sdk::FlightHubError err) = 0;
    };

    class OperateSelfDiagnosisCallback {
    public:
        virtual ~OperateSelfDiagnosisCallback() {}
        virtual void onOperateSelfDiagnosis(flight_hub_sdk::FlightHubError err) = 0;
    };

    class GetSelfDiagnosisReportCallback {
    public:
        virtual ~GetSelfDiagnosisReportCallback() {}
        virtual void onGetSelfDiagnosisReport(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetSelfDiagnosisReportResults& result) = 0;
    };

    class GetSelfDiagnosisReportListCallback {
    public:
        virtual ~GetSelfDiagnosisReportListCallback() {}
        virtual void onGetSelfDiagnosisReportList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetSelfDiagnosisReportListResults& result) = 0;
    };

    // AI Box 管理相关回调接口
    class FetchAIBoxInfoCallback {
    public:
        virtual ~FetchAIBoxInfoCallback() {}
        virtual void onFetchAIBoxInfo(flight_hub_sdk::FlightHubError err, flight_hub_sdk::AIBoxInfoListResults results) = 0;
    };

    class FetchFileDownloadUrlCallback {
    public:
        virtual ~FetchFileDownloadUrlCallback() {}
        virtual void onFetchFileDownloadUrl(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FileDownloadUrlListResults results) = 0;
    };

    class FetchAppListCallback {
    public:
        virtual ~FetchAppListCallback() {}
        virtual void onFetchAppList(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FetchAppListResults results) = 0;
    };

    class FetchAppListProgressCallback {
    public:
        virtual ~FetchAppListProgressCallback() {}
        virtual void onFetchAppListProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::FetchAppListProgressResults results) = 0;
    };

    class ExportFileCallback {
    public:
        virtual ~ExportFileCallback() {}
        virtual void onExportFile(flight_hub_sdk::FlightHubError err, flight_hub_sdk::ExportFileResults results) = 0;
    };

    class ExportFileProgressCallback {
    public:
        virtual ~ExportFileProgressCallback() {}
        virtual void onExportFileProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::ExportFileProgressResults results) = 0;
    };

    class UploadAIBoxFileCallback {
    public:
        virtual ~UploadAIBoxFileCallback() {}
        virtual void onUploadAIBoxFile(flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadFileResults results) = 0;
    };

    class InstallAppCallback {
    public:
        virtual ~InstallAppCallback() {}
        virtual void onInstallApp(flight_hub_sdk::FlightHubError err, flight_hub_sdk::InstallAppResults results) = 0;
    };

    class InstallAppProgressCallback {
    public:
        virtual ~InstallAppProgressCallback() {}
        virtual void onInstallAppProgress(flight_hub_sdk::FlightHubError err, flight_hub_sdk::InstallAppProgressResults results) = 0;
    };

    class OperateAppCallback {
    public:
        virtual ~OperateAppCallback() {}
        virtual void onOperateApp(flight_hub_sdk::FlightHubError err, flight_hub_sdk::OperateAppResults results) = 0;
    };

    class DockDeparturePointCallback {
    public:
        virtual ~DockDeparturePointCallback() {}
        virtual void onDockDeparturePoint(const flight_hub_sdk::DockDeparturePoint& msg) = 0;
    };

    class AircraftVisualScoutStatusCallback {
    public:
        virtual ~AircraftVisualScoutStatusCallback() {}
        virtual void onAircraftVisualScoutStatus(const flight_hub_sdk::AircraftVisualScoutStatus& msg) = 0;
    };

    class AircraftVisualScoutAbnormalInfoCallback {
    public:
        virtual ~AircraftVisualScoutAbnormalInfoCallback() {}
        virtual void onAircraftVisualScoutAbnormalInfo(const flight_hub_sdk::AircraftVisualScoutAbnormalInfo& msg) = 0;
    };

    class AircraftVisualScoutObstacleInfoCallback {
    public:
        virtual ~AircraftVisualScoutObstacleInfoCallback() {}
        virtual void onAircraftVisualScoutObstacleInfo(const flight_hub_sdk::AircraftVisualScoutObstacleInfo& msg) = 0;
    };

    class EphemerisDownloadStateCallback {
    public:
        virtual ~EphemerisDownloadStateCallback() {}
        virtual void onEphemerisDownloadState(const flight_hub_sdk::EphemerisDownloadState& msg) = 0;
    };

    class EphemerisInfoCallback {
    public:
        virtual ~EphemerisInfoCallback() {}
        virtual void onEphemerisInfo(const flight_hub_sdk::EphemerisInfo& msg) = 0;
    };

    class InOutWaylineFlightSettingResultCallback {
    public:
        virtual ~InOutWaylineFlightSettingResultCallback() {}
        virtual void onResult(const flight_hub_sdk::InOutWaylineFlightSettingParams& params, flight_hub_sdk::FlightHubError err) = 0;
    };

    class DeviceOtaProgressCallback {
    public:
        virtual ~DeviceOtaProgressCallback() {}
        virtual void onDeviceOtaProgress(const flight_hub_sdk::DeviceOtaProgress& msg) = 0;
    };

    class DeviceFileUploadProgressCallback {
    public:
        virtual ~DeviceFileUploadProgressCallback() {}
        virtual void onDeviceFileUploadProgress(const flight_hub_sdk::DeviceFileUploadProgress& msg) = 0;
    };

    class PsdkSpeakerAudioPlayStartProgressCallback {
    public:
        virtual ~PsdkSpeakerAudioPlayStartProgressCallback() {}
        virtual void onPsdkSpeakerAudioPlayStartProgress(const flight_hub_sdk::PsdkSpeakerPlayStartProgress& msg) = 0;
    };

    class PsdkSpeakerTtsPlayStartProgressCallback {
    public:
        virtual ~PsdkSpeakerTtsPlayStartProgressCallback() {}
        virtual void onPsdkSpeakerTtsPlayStartProgress(const flight_hub_sdk::PsdkSpeakerPlayStartProgress& msg) = 0;
    };

    class FlightPoiProgressCallback {
    public:
        virtual ~FlightPoiProgressCallback() {}
        virtual void onFlightPoiProgress(const flight_hub_sdk::FlightPoiProgress& msg) = 0;
    };

    class DrcOsdInfoCallback {
    public:
        virtual ~DrcOsdInfoCallback() {}
        virtual void onDrcOsdInfo(const flight_hub_sdk::DrcOsdInfo& info) = 0;
    };

    class DrcBatteriesInfoCallback {
    public:
        virtual ~DrcBatteriesInfoCallback() {}
        virtual void onDrcBatteriesInfo(const flight_hub_sdk::DrcBatteriesInfo& info) = 0;
    };

    class DrcParachuteInfoCallback {
    public:
        virtual ~DrcParachuteInfoCallback() {}
        virtual void onDrcParachuteInfo(const flight_hub_sdk::DrcParachuteInfo& info) = 0;
    };

    class FlashLedStateInfoCallback {
    public:
        virtual ~FlashLedStateInfoCallback() {}
        virtual void onFlashLedStateInfo(const flight_hub_sdk::FlashLedStateInfo& info) = 0;
    };

    class FlashLedConfigListCallback {
    public:
        virtual ~FlashLedConfigListCallback() {}
        virtual void onFlashLedConfigList(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfigList& list) = 0;
    };

    class FlashLedConfigCallback {
    public:
        virtual ~FlashLedConfigCallback() {}
        virtual void onFlashLedConfig(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfig& config) = 0;
    };

    class AiAlarmStateCallback {
    public:
        virtual ~AiAlarmStateCallback() {}
        virtual void onAiAlarmState(const flight_hub_sdk::AiAlarmStateInfo& info) = 0;
    };

    class DrcDelayInfoCallback {
    public:
        virtual ~DrcDelayInfoCallback() {}
        virtual void onDrcDelayInfo(const flight_hub_sdk::DrcDelayInfo& info) = 0;
    };


    class DrcCountDownInfoCallback {
    public:
        virtual ~DrcCountDownInfoCallback() {}
        virtual void onDrcCountDownInfo(const flight_hub_sdk::DrcCountDownInfo& info) = 0;
    };

    // EA340 消息推送补全（Phase D）新增回调接口;
    // 注意: 与 C++ 侧的 std::function<void(const Xxx&)> 回调签名保持一致, 均不携带 sn 参数
    // （sn 仅作为部分 listen 方法的入参用于筛选订阅目标, 详见 SafetyServiceModule::listenDroneOfflineMapEnable）;
    class DockLiveStatusCallback {
    public:
        virtual ~DockLiveStatusCallback() {}
        virtual void onDockLiveStatus(const flight_hub_sdk::DockLiveStatus& status) = 0;
    };

    class DockLiveCapacityCallback {
    public:
        virtual ~DockLiveCapacityCallback() {}
        virtual void onDockLiveCapacity(const flight_hub_sdk::DockLiveCapacity& capacity) = 0;
    };

    class DockDongleInfosCallback {
    public:
        virtual ~DockDongleInfosCallback() {}
        virtual void onDockDongleInfos(const flight_hub_sdk::DockDongleInfos& infos) = 0;
    };

    class DockAirConditionerModeCallback {
    public:
        virtual ~DockAirConditionerModeCallback() {}
        virtual void onDockAirConditionerMode(const flight_hub_sdk::DockAirConditionerMode& mode) = 0;
    };

    class DroneModeCodeReasonCallback {
    public:
        virtual ~DroneModeCodeReasonCallback() {}
        virtual void onDroneModeCodeReason(const flight_hub_sdk::DroneModeCodeReason& reason) = 0;
    };

    class DroneDongleInfosCallback {
    public:
        virtual ~DroneDongleInfosCallback() {}
        virtual void onDroneDongleInfos(const flight_hub_sdk::DroneDongleInfos& infos) = 0;
    };

    class DroneFlysafeDatabaseVersionCallback {
    public:
        virtual ~DroneFlysafeDatabaseVersionCallback() {}
        virtual void onDroneFlysafeDatabaseVersion(const flight_hub_sdk::DroneFlysafeDatabaseVersion& version) = 0;
    };

    class DroneWpmzVersionCallback {
    public:
        virtual ~DroneWpmzVersionCallback() {}
        virtual void onDroneWpmzVersion(const flight_hub_sdk::DroneWpmzVersion& version) = 0;
    };

    class DroneCommanderFlightModeCallback {
    public:
        virtual ~DroneCommanderFlightModeCallback() {}
        virtual void onDroneCommanderFlightMode(const flight_hub_sdk::DroneCommanderFlightMode& mode) = 0;
    };

    class DroneCurrentCommanderFlightModeCallback {
    public:
        virtual ~DroneCurrentCommanderFlightModeCallback() {}
        virtual void onDroneCurrentCommanderFlightMode(const flight_hub_sdk::DroneCurrentCommanderFlightMode& mode) = 0;
    };

    class DroneCommanderModeLostActionCallback {
    public:
        virtual ~DroneCommanderModeLostActionCallback() {}
        virtual void onDroneCommanderModeLostAction(const flight_hub_sdk::DroneCommanderModeLostAction& action) = 0;
    };

    class DroneOfflineMapEnableCallback {
    public:
        virtual ~DroneOfflineMapEnableCallback() {}
        virtual void onDroneOfflineMapEnable(const flight_hub_sdk::DroneOfflineMapEnable& enable) = 0;
    };

    class DronePsdkUiResourceCallback {
    public:
        virtual ~DronePsdkUiResourceCallback() {}
        virtual void onDronePsdkUiResource(const flight_hub_sdk::DronePsdkUiResource& resource) = 0;
    };

    class DroneCameraWatermarkSettingsPushCallback {
    public:
        virtual ~DroneCameraWatermarkSettingsPushCallback() {}
        virtual void onDroneCameraWatermarkSettingsPush(const flight_hub_sdk::DroneCameraWatermarkSettingsPush& settings) = 0;
    };
%}


%ignore flight_hub_sdk::ICameraControlModule::drcTakePhoto;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraState;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcLiveviewWorldRegion;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraWideLensState;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraZoomLensState;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraMeasureTarget;
%ignore flight_hub_sdk::ICameraControlModule::drcSetNightSceneMode;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcNightModeSettings;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcGimbalState;
%ignore flight_hub_sdk::ICameraControlModule::drcStartRecord;
%ignore flight_hub_sdk::ICameraControlModule::drcStopRecord;
%ignore flight_hub_sdk::ICameraControlModule::drcZoomAtTarget;
%ignore flight_hub_sdk::ICameraControlModule::drcSetFocalLength;
%ignore flight_hub_sdk::ICameraControlModule::lookAtTarget;
%ignore flight_hub_sdk::ICameraControlModule::drcStartPanorama;
%ignore flight_hub_sdk::ICameraControlModule::drcStopPanorama;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcPanoramaStatus;
%ignore flight_hub_sdk::ICameraControlModule::drcSwitchCameraMode;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraModeAvailableRange;
%ignore flight_hub_sdk::ICameraControlModule::drcSwitchLiveLens;


%ignore flight_hub_sdk::ICameraControlModule::drcSmartPhotoTakePhoto;
%ignore flight_hub_sdk::ICameraControlModule::drcSetLinkageZoom;
%ignore flight_hub_sdk::ICameraControlModule::drcSendCameraScreenDrag;
%ignore flight_hub_sdk::ICameraControlModule::drcSetPhotoSize;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIntervalTakePhoto;
%ignore flight_hub_sdk::ICameraControlModule::drcIntervalTakePhoto;
%ignore flight_hub_sdk::ICameraControlModule::drcStopTakePhoto;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcIntervalTakePhotoStatus;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcIntervalTakePhotoStatus;
%ignore flight_hub_sdk::ICameraControlModule::drcCameraAimAtTarget;

// 红外增益模式相关接口忽略
%ignore flight_hub_sdk::ICameraControlModule::listenDrcLiveVideoInfo;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcLiveVideoInfo;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcThermalGainMode;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcThermalGainMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetThermalGainMode;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraIrLensState;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraIrLensState;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraIrThermometry;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraIrThermometry;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraWatermarkSettings;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraWatermarkSettings;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraStorageInfo;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraStorageInfo;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraIrPipState;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraIrPipState;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraIsothermPolygon;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraIsothermPolygon;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraMediaStorage;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraMediaStorage;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraAlgorithmDehazeInfo;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraAlgorithmDehazeInfo;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraPreRecordedInfo;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraPreRecordedInfo;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraCapability;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraCapability;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraSettableCapability;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraSettableCapability;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraVisibleCapability;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraVisibleCapability;
%ignore flight_hub_sdk::ICameraControlModule::listenDrcCameraPhotoInfo;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDrcCameraPhotoInfo;

%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraApertureValue;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraExposureMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraExposureValue;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraFocusMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraFocusValue;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraIso;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraMechanicalShutter;
%ignore flight_hub_sdk::ICameraControlModule::drcCameraPointFocusAction;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraShutter;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraWatermark;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraDewarping;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraScreenSplit;
%ignore flight_hub_sdk::ICameraControlModule::drcCleanFog;
%ignore flight_hub_sdk::ICameraControlModule::drcSetThermalCurrentPalette;
%ignore flight_hub_sdk::ICameraControlModule::drcSetThermalSuperResolutionEnable;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIrMeteringMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIrMeteringPoint;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIrMeteringArea;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIrDisplayMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIrEnhanceMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIrMixRate;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIsothermEnable;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIsothermColorRenderingType;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIsothermLowerUpperRange;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIsothermRenderingPolygon;
%ignore flight_hub_sdk::ICameraControlModule::drcSetIsothermRoiBasedRenderingEnable;
%ignore flight_hub_sdk::ICameraControlModule::drcSetInfraredFillLightEnable;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraNightVisionEnable;
%ignore flight_hub_sdk::ICameraControlModule::drcResetCameraSettings;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraDenoiseLevelSet;
%ignore flight_hub_sdk::ICameraControlModule::drcSetVideoResolution;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCameraPhotoFormat;
%ignore flight_hub_sdk::ICameraControlModule::drcSetPreRecordedStatus;
%ignore flight_hub_sdk::ICameraControlModule::drcSetPreRecordedTime;
%ignore flight_hub_sdk::ICameraControlModule::drcSetCountDownAction;
%ignore flight_hub_sdk::ICameraControlModule::drcSetAssistantVideoMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetAssistantVideoSwitch;
%ignore flight_hub_sdk::ICameraControlModule::drcSetAlgorithmDehazeMode;
%ignore flight_hub_sdk::ICameraControlModule::drcSetAlgorithmDehazeLevel;
// EA340 消息推送补全（Phase D）: 飞机相机水印设置推送(WebSocket), 与 DRC 的 listenDrcCameraWatermarkSettings 不同, 不要混淆;
%ignore flight_hub_sdk::ICameraControlModule::listenDroneCameraWatermarkSettings;
%ignore flight_hub_sdk::ICameraControlModule::cancelListenDroneCameraWatermarkSettings;

// PSDK相关接口忽略
%ignore flight_hub_sdk::IPsdkControlModule::listenDrcPsdkDeviceList;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDrcPsdkDeviceList;
%ignore flight_hub_sdk::IPsdkControlModule::listenDrcPsdkStateSummaryInfo;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDrcPsdkStateSummaryInfo;
%ignore flight_hub_sdk::IPsdkControlModule::listenDrcPsdkStateSpeakerInfo;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDrcPsdkStateSpeakerInfo;
%ignore flight_hub_sdk::IPsdkControlModule::listenDrcPsdkStateSearchlightInfo;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDrcPsdkStateSearchlightInfo;
%ignore flight_hub_sdk::IPsdkControlModule::listenDrcPsdkSpeakerPlayProgressInfo;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDrcPsdkSpeakerPlayProgressInfo;
%ignore flight_hub_sdk::IPsdkControlModule::listenDrcPsdkFloatingWindowTextInfo;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDrcPsdkFloatingWindowTextInfo;
%ignore flight_hub_sdk::IPsdkControlModule::listenPsdkSpeakerAudioPlayStartProgress;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenPsdkSpeakerAudioPlayStartProgress;
%ignore flight_hub_sdk::IPsdkControlModule::listenPsdkSpeakerTtsPlayStartProgress;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenPsdkSpeakerTtsPlayStartProgress;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerAudioPlayStart;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerTTSPlayStart;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerRealTimeStop;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerRealTimeStart;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerReplay;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerPlayStop;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerPlayVolumeSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerPlayModeSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSpeakerTTSSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcWidgetValueSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcWidgetInputBoxTextSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightModeSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightBrightnessSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightCalibration;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightFineTuningSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightWideFieldModeSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightLightGimbalControlSet;
%ignore flight_hub_sdk::IPsdkControlModule::DrcSearchlightTypeSet;
// EA340 消息推送补全（Phase D）;
%ignore flight_hub_sdk::IPsdkControlModule::listenDronePsdkUiResource;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenDronePsdkUiResource;



%ignore flight_hub_sdk::ILiveModule::subscribeLiveStream;
%ignore flight_hub_sdk::ILiveModule::subscribeAiSeiInfo;
%ignore flight_hub_sdk::ILiveModule::subscribeArSeiInfo;
%ignore flight_hub_sdk::ILiveModule::subscribeFovSeiInfo;
%ignore flight_hub_sdk::ILiveModule::subscribeAssistantSeiInfo;
%ignore flight_hub_sdk::ILiveModule::subscribeEisSeiInfo;
%ignore flight_hub_sdk::ILiveModule::setLiveQuality;
%ignore flight_hub_sdk::ILiveModule::startWithURL;
%ignore flight_hub_sdk::ILiveModule::stopWithURL;

%ignore flight_hub_sdk::IFlyToPointModule::start;
%ignore flight_hub_sdk::IFlyToPointModule::drcStop;
%ignore flight_hub_sdk::IFlyToPointModule::subscribeFlyToDroneSelfPlanPath;
%ignore flight_hub_sdk::IFlyToPointModule::subscribeFlyToPointProgress;
%ignore flight_hub_sdk::IFlyToPointModule::updateTargetPoints;
%ignore flight_hub_sdk::IFlyToPointModule::listenFlyToTaskEvent;
%ignore flight_hub_sdk::IFlyToPointModule::cancelListenFlyToTaskEvent;

%ignore flight_hub_sdk::IPoiModule::enter;
%ignore flight_hub_sdk::IPoiModule::exit;
%ignore flight_hub_sdk::IPoiModule::updateSpeed;
%ignore flight_hub_sdk::IPoiModule::getTask;
%ignore flight_hub_sdk::IPoiModule::listenPoiTaskEvent;
%ignore flight_hub_sdk::IPoiModule::cancelListenPoiTaskEvent;
%ignore flight_hub_sdk::IPoiModule::listenFlightPoiProgress;
%ignore flight_hub_sdk::IPoiModule::cancelListenFlightPoiProgress;

%ignore flight_hub_sdk::IFlightTaskControlModule::pause;
%ignore flight_hub_sdk::IFlightTaskControlModule::resume;
%ignore flight_hub_sdk::IFlightTaskControlModule::listenWaylineTaskEvent;
%ignore flight_hub_sdk::IFlightTaskControlModule::cancelListenWaylineTaskEvent;


%ignore flight_hub_sdk::IFlightTaskModule::getFlightTaskList;
%ignore flight_hub_sdk::IFlightTaskModule::getCommandTaskList;
%ignore flight_hub_sdk::IFlightTaskModule::getFlightTaskTrack;
%ignore flight_hub_sdk::IFlightTaskModule::createFlightTask;
%ignore flight_hub_sdk::IFlightTaskModule::getFlightTaskDetail;
%ignore flight_hub_sdk::IFlightTaskModule::editFlightTask;
%ignore flight_hub_sdk::IFlightTaskModule::deleteFlightTask;
%ignore flight_hub_sdk::IFlightTaskModule::updateFlightTaskStatus;
%ignore flight_hub_sdk::IFlightTaskModule::batchUpdateFlightTaskStatus;
%ignore flight_hub_sdk::IFlightTaskModule::getDeviceRecommendation;
%ignore flight_hub_sdk::IFlightTaskModule::getDeviceTcas;
%ignore flight_hub_sdk::IFlightTaskModule::updateDeviceTcas;
%ignore flight_hub_sdk::IFlightTaskModule::startRoaming;
%ignore flight_hub_sdk::IFlightTaskModule::setRoamingStationSn;
%ignore flight_hub_sdk::IFlightTaskModule::getInFlightTaskTopos;
%ignore flight_hub_sdk::IFlightTaskModule::listenTaskTopoChanged;
%ignore flight_hub_sdk::IFlightTaskModule::cancelListenTaskTopoChanged;

// 提前声明 IFlightTaskModule，以便后续 %extend 能正确生成 Java 类
%include "flight_task_module.h"


// AI Box 管理相关原始方法忽略
%ignore flight_hub_sdk::IAIBoxModule::fetchAIBoxInfo;
%ignore flight_hub_sdk::IAIBoxModule::fetchFileDownloadUrl;
%ignore flight_hub_sdk::IAIBoxModule::fetchAppList;
%ignore flight_hub_sdk::IAIBoxModule::fetchAppListProgress;
%ignore flight_hub_sdk::IAIBoxModule::exportFile;
%ignore flight_hub_sdk::IAIBoxModule::exportFileProgress;
%ignore flight_hub_sdk::IAIBoxModule::uploadAIBoxFile;
%ignore flight_hub_sdk::IAIBoxModule::installApp;
%ignore flight_hub_sdk::IAIBoxModule::installAppProgress;
%ignore flight_hub_sdk::IAIBoxModule::operateApp;

%ignore flight_hub_sdk::ICustomSEIModule::SimpleDataTest;
%ignore flight_hub_sdk::ICustomSEIModule::CustomSEIEncode;
%ignore flight_hub_sdk::ICustomSEIModule::CustomSEIDecode;

// 忽略IPullStreamModule的原始方法
%ignore flight_hub_sdk::IPullStreamModule::start;
%ignore flight_hub_sdk::IPullStreamModule::subscribeStream;
%ignore flight_hub_sdk::IPullStreamModule::setErrorCallback;
// 忽略IDeviceDebugModule的原始方法
%ignore flight_hub_sdk::IDeviceDebugModule::openDebugMode;
%ignore flight_hub_sdk::IDeviceDebugModule::closeDebugMode;
%ignore flight_hub_sdk::IDeviceDebugModule::openDockCover;
%ignore flight_hub_sdk::IDeviceDebugModule::getOpenDockCoverProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::closeDockCover;
%ignore flight_hub_sdk::IDeviceDebugModule::getCloseDockCoverProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::deviceReboot;
%ignore flight_hub_sdk::IDeviceDebugModule::getDeviceRebootProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::deviceFormat;
%ignore flight_hub_sdk::IDeviceDebugModule::getDeviceFormatProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::openDrone;
%ignore flight_hub_sdk::IDeviceDebugModule::closeDrone;
%ignore flight_hub_sdk::IDeviceDebugModule::openCharge;
%ignore flight_hub_sdk::IDeviceDebugModule::closeCharge;
%ignore flight_hub_sdk::IDeviceDebugModule::setDockAirConditionerMode;
%ignore flight_hub_sdk::IDeviceDebugModule::setDockSilentMode;
%ignore flight_hub_sdk::IDeviceDebugModule::startNetworkTest;
%ignore flight_hub_sdk::IDeviceDebugModule::stopNetworkTest;
%ignore flight_hub_sdk::IDeviceDebugModule::getNetworkTestProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDeviceNetworkSpeedTestProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDeviceNetworkSpeedTestProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::setObstacleAvoidance;
%ignore flight_hub_sdk::IDeviceDebugModule::setDistanceLimit;
%ignore flight_hub_sdk::IDeviceDebugModule::setHeightLimit;
%ignore flight_hub_sdk::IDeviceDebugModule::setRemainingPowerForRth;
%ignore flight_hub_sdk::IDeviceDebugModule::setNightLightsState;
%ignore flight_hub_sdk::IDeviceDebugModule::switchAlarmLightState;
%ignore flight_hub_sdk::IDeviceDebugModule::esimOperatorSwitch;
%ignore flight_hub_sdk::IDeviceDebugModule::simSlotSwitch;
%ignore flight_hub_sdk::IDeviceDebugModule::setBatteryStoreMode;
%ignore flight_hub_sdk::IDeviceDebugModule::startManualBatterySwap;
%ignore flight_hub_sdk::IDeviceDebugModule::getManualBatterySwapProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::setDonglePowerSwitch;
%ignore flight_hub_sdk::IDeviceDebugModule::setLowPowerSwitch;
%ignore flight_hub_sdk::IDeviceDebugModule::getEsimOperatorSwitchProgress;
// Geo Caging ignore
%ignore flight_hub_sdk::IDeviceDebugModule::setGeoCagingEnable;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDockCoverOpenProgress;
// 云环境切换 ignore
%ignore flight_hub_sdk::IDeviceDebugModule::cloudEnvironmentSwitch;
%ignore flight_hub_sdk::IDeviceDebugModule::cloudEnvironmentSwitchCallback;
%ignore flight_hub_sdk::IDeviceDebugModule::listenDeviceCloudEnvironmentSwitchProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::cancelListenDeviceCloudEnvironmentSwitchProgress;

// IRelayControlModule ignore
%ignore flight_hub_sdk::IRelayControlModule::setRelayBroadcastHide;
%ignore flight_hub_sdk::IRelayControlModule::setRelaySSID;
%ignore flight_hub_sdk::IRelayControlModule::setRelayPassword;
%ignore flight_hub_sdk::IRelayControlModule::setRelaySearchMode;
%ignore flight_hub_sdk::IRelayControlModule::setRelayOccupiedStatus;
%ignore flight_hub_sdk::IRelayControlModule::relayReboot;
%ignore flight_hub_sdk::IRelayControlModule::relayDisconnect;
// IRtkCalibrationModule ignore
%ignore flight_hub_sdk::IRtkCalibrationModule::calibrationByNetwork;
%ignore flight_hub_sdk::IRtkCalibrationModule::calibrationByManual;
%ignore flight_hub_sdk::IRtkCalibrationModule::subscribeCalibrationProgress;
%ignore flight_hub_sdk::IRtkCalibrationModule::unsubscribeCalibrationProgress;
%ignore flight_hub_sdk::INetworkTestModule::startNetworkTest;
%ignore flight_hub_sdk::INetworkTestModule::stopNetworkTest;
%ignore flight_hub_sdk::INetworkTestModule::getNetworkTestProgress;
%ignore flight_hub_sdk::INetworkTestModule::subscribeNetworkTestProgress;
%ignore flight_hub_sdk::INetworkTestModule::unsubscribeNetworkTestProgress;
%ignore flight_hub_sdk::IDeviceStateModule::listenDeviceTopoChanged;
%ignore flight_hub_sdk::IDeviceStateModule::cancelListenDeviceTopoChanged;
%ignore flight_hub_sdk::IDeviceStateModule::listenDockOnlineStatus;
%ignore flight_hub_sdk::IDeviceStateModule::cancelListenDockOnlineStatus;
%ignore flight_hub_sdk::IDeviceStateModule::listenDroneOnlineStatus;
%ignore flight_hub_sdk::IDeviceStateModule::cancelListenDroneOnlineStatus;
%ignore flight_hub_sdk::IDeviceHMSModule::listenDeviceHMS;
%ignore flight_hub_sdk::IDeviceHMSModule::cancelListenDeviceHMS;
%ignore flight_hub_sdk::ILiveDeviceModule::listenLiveAvailableRange;
%ignore flight_hub_sdk::ILiveDeviceModule::cancelListenLiveAvailableRange;
%ignore flight_hub_sdk::ILiveDeviceModule::getAudienceCount;
// EA340 消息推送补全（Phase D）;
%ignore flight_hub_sdk::ILiveDeviceModule::listenDockLiveStatus;
%ignore flight_hub_sdk::ILiveDeviceModule::cancelListenDockLiveStatus;
%ignore flight_hub_sdk::ILiveDeviceModule::listenDockLiveCapacity;
%ignore flight_hub_sdk::ILiveDeviceModule::cancelListenDockLiveCapacity;
%ignore flight_hub_sdk::ITakeoffModule::takeoff;
%ignore flight_hub_sdk::ITakeoffModule::listenCommandTaskEvent;
%ignore flight_hub_sdk::ITakeoffModule::cancelListenCommandTaskEvent;
%ignore flight_hub_sdk::ITakeoffModule::listenDroneCommanderFlightMode;
%ignore flight_hub_sdk::ITakeoffModule::cancelListenDroneCommanderFlightMode;
%ignore flight_hub_sdk::ITakeoffModule::listenDroneCurrentCommanderFlightMode;
%ignore flight_hub_sdk::ITakeoffModule::cancelListenDroneCurrentCommanderFlightMode;
%ignore flight_hub_sdk::ITakeoffModule::listenDroneCommanderModeLostAction;
%ignore flight_hub_sdk::ITakeoffModule::cancelListenDroneCommanderModeLostAction;

%ignore flight_hub_sdk::IInFlightWaylineModule::createInFlightWaylineTask;
%ignore flight_hub_sdk::IInFlightWaylineModule::controlInFlightWaylineTask;
%ignore flight_hub_sdk::IInFlightWaylineModule::getActiveInFlightWaylineTask;
%ignore flight_hub_sdk::IInFlightWaylineModule::listenInFlightWaylineProgress;
%ignore flight_hub_sdk::IInFlightWaylineModule::cancelListenInFlightWaylineProgress;

%ignore flight_hub_sdk::IFlightControlModule::drcEmergencyStop;
%ignore flight_hub_sdk::IFlightControlModule::drcEmergencyLanding;
%ignore flight_hub_sdk::IFlightControlModule::drcForceLanding;
%ignore flight_hub_sdk::IFlightControlModule::returnHome;
%ignore flight_hub_sdk::IFlightControlModule::returnHomeCancel;
%ignore flight_hub_sdk::IFlightControlModule::drcSendStickControl;
%ignore flight_hub_sdk::IFlightControlModule::drcSetDroneHover;
%ignore flight_hub_sdk::IFlightControlModule::drcSetFollowMouseParameter;
%ignore flight_hub_sdk::IFlightControlModule::drcSetNightLightsState;
%ignore flight_hub_sdk::IFlightControlModule::drcSetStealthState;
%ignore flight_hub_sdk::IFlightControlModule::drcOpenParachute;
%ignore flight_hub_sdk::IFlightControlModule::drcSetFlashLed;
%ignore flight_hub_sdk::IFlightControlModule::drcCameraLookAt;
%ignore flight_hub_sdk::IFlightControlModule::listenDrcOsdInfo;
%ignore flight_hub_sdk::IFlightControlModule::cancelListenDrcOsdInfo;
%ignore flight_hub_sdk::IFlightControlModule::listenDrcBatteriesInfo;
%ignore flight_hub_sdk::IFlightControlModule::cancelListenDrcBatteriesInfo;
%ignore flight_hub_sdk::IFlightControlModule::listenDrcParachuteInfo;
%ignore flight_hub_sdk::IFlightControlModule::cancelListenDrcParachuteInfo;
%ignore flight_hub_sdk::IFlightControlModule::listenDrcFlashLed;
%ignore flight_hub_sdk::IFlightControlModule::cancelListenDrcFlashLed;

%ignore flight_hub_sdk::IControlAuthModule::acquireFlightControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::releaseFlightControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::listenFlightControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::acquirePayloadControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::releasePayloadControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::listenPayloadControlAuthority;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDockCoverOpenProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDockCoverCloseProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDockCoverCloseProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDeviceRebootProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDeviceRebootProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDeviceFormatProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDeviceFormatProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDroneOpenProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDroneOpenProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDroneCloseProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDroneCloseProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDroneFormatProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDroneFormatProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDroneChargeOpenProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDroneChargeOpenProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDroneChargeCloseProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDroneChargeCloseProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDeviceEsimOperatorSwitchProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDeviceEsimOperatorSwitchProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::subscribeDeviceSimSlotSwitchProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::unsubscribeDeviceSimSlotSwitchProgress;
%ignore flight_hub_sdk::IDeviceDebugModule::droneFormat;
%ignore flight_hub_sdk::IDeviceDebugModule::droneSdrWorkMode;

// 忽略IHMSModule的原始方法
%ignore flight_hub_sdk::IHMSModule::QueryHistoryHMSData;


// 忽略IDeviceManageModule的原始方法
%ignore flight_hub_sdk::IDeviceManageModule::getDeviceList;
%ignore flight_hub_sdk::IDeviceManageModule::getDeviceDetailInfo;
%ignore flight_hub_sdk::IDeviceManageModule::modifyDeviceName;
%ignore flight_hub_sdk::IDeviceManageModule::listenWorkspaceDeviceChanged;
%ignore flight_hub_sdk::IDeviceManageModule::cancelListenWorkspaceDeviceChanged;
%ignore flight_hub_sdk::IDeviceManageModule::unbindDevice;
%ignore flight_hub_sdk::IDeviceManageModule::getDockDeviceStatus;
%ignore flight_hub_sdk::IDeviceManageModule::getDroneDeviceStatus;

// 忽略IFirmwareManageModule的原始方法
%ignore flight_hub_sdk::IFirmwareManageModule::listenDeviceOtaProgress;
%ignore flight_hub_sdk::IFirmwareManageModule::cancelListenDeviceOtaProgress;
%ignore flight_hub_sdk::IFirmwareManageModule::getLatestFirmwareReleaseNote;
%ignore flight_hub_sdk::IFirmwareManageModule::invokeFirmwareUpgrade;
%ignore flight_hub_sdk::IFirmwareManageModule::queryFirmwareUpgradeProgress;
%ignore flight_hub_sdk::IFirmwareManageModule::uploadFirmwareFile;
%ignore flight_hub_sdk::IFirmwareManageModule::releaseFirmware;
%ignore flight_hub_sdk::IFirmwareManageModule::unReleaseFirmware;
%ignore flight_hub_sdk::IFirmwareManageModule::deleteFirmware;
%ignore flight_hub_sdk::IFirmwareManageModule::queryFirmwareList;
%ignore flight_hub_sdk::IFirmwareManageModule::queryFirmwareCompatibilityConfig;
%ignore flight_hub_sdk::IFirmwareManageModule::checkFirmwareStatus;
%ignore flight_hub_sdk::IFirmwareManageModule::submitFirmwareInfo;

// 忽略IPsdkResourceModule的原始方法
%ignore flight_hub_sdk::IPsdkResourceModule::getPsdkSpeakerResourceList;
%ignore flight_hub_sdk::IPsdkResourceModule::createPsdkSpeakerResource;
%ignore flight_hub_sdk::IPsdkResourceModule::editPsdkSpeakerResource;
%ignore flight_hub_sdk::IPsdkResourceModule::deletePsdkSpeakerResource;
%ignore flight_hub_sdk::IPsdkResourceModule::getSTS;
%ignore flight_hub_sdk::IPsdkResourceModule::getPsdkWidgetResources;
%ignore flight_hub_sdk::IPsdkResourceModule::uploadSpeakerAudioFile;

// 忽略IFlashLedModule的原始方法
%ignore flight_hub_sdk::IFlashLedModule::getFlashLedConfigList;
%ignore flight_hub_sdk::IFlashLedModule::createFlashLedConfig;
%ignore flight_hub_sdk::IFlashLedModule::updateFlashLedConfig;
%ignore flight_hub_sdk::IFlashLedModule::deleteFlashLedConfig;

// 忽略IWaylineManageModule的原始方法
%ignore flight_hub_sdk::IWaylineManageModule::getWaylineList;
%ignore flight_hub_sdk::IWaylineManageModule::getWaylineDetail;
%ignore flight_hub_sdk::IWaylineManageModule::batchDeleteWaylines;
%ignore flight_hub_sdk::IWaylineManageModule::updateWayline;
%ignore flight_hub_sdk::IWaylineManageModule::submitWaylineInfo;
%ignore flight_hub_sdk::IWaylineManageModule::batchDownloadWaylines;
%ignore flight_hub_sdk::IWaylineManageModule::duplicateWayline;
%ignore flight_hub_sdk::IWaylineManageModule::copyWayline;
%ignore flight_hub_sdk::IWaylineManageModule::concatWayline;
%ignore flight_hub_sdk::IWaylineManageModule::importKmz;
%ignore flight_hub_sdk::IWaylineManageModule::listenImportWaylineEvent;
%ignore flight_hub_sdk::IWaylineManageModule::cancelListenImportWaylineEvent;
%ignore flight_hub_sdk::IWaylineManageModule::getWaylineEditorSts;

// 忽略 IAIInsideManageModule 原始方法，由 Wrapper 代替
%ignore flight_hub_sdk::IAIInsideManageModule::getAIInsideModels;
%ignore flight_hub_sdk::IAIInsideManageModule::getAIInsideModelDevices;
%ignore flight_hub_sdk::IAIInsideManageModule::getAIInsideModelDevicesSummary;
%ignore flight_hub_sdk::IAIInsideManageModule::operateAIInsideModel;
%ignore flight_hub_sdk::IAIInsideManageModule::createAIInsideAlgorithm;
%ignore flight_hub_sdk::IAIInsideManageModule::fileUploadAIInsideAlgorithm;
%ignore flight_hub_sdk::IAIInsideManageModule::deleteAIInsideAlgorithm;
%ignore flight_hub_sdk::IAIInsideManageModule::uploadAlgorithmFile;

// 忽略 IAIAlertModule 原始方法，由 Wrapper 代替
%ignore flight_hub_sdk::IAIAlertModule::postDeviceSetting;
%ignore flight_hub_sdk::IAIAlertModule::getDeviceSetting;
%ignore flight_hub_sdk::IAIAlertModule::postAlgorithmSetting;
%ignore flight_hub_sdk::IAIAlertModule::getAlgorithmSetting;
%ignore flight_hub_sdk::IAIAlertModule::getAiAlertFlights;
%ignore flight_hub_sdk::IAIAlertModule::getAiAlertRecords;

// 忽略 ILiveSyncModule 原始方法，由 Wrapper 代替
%ignore flight_hub_sdk::ILiveSyncModule::createLiveSync;
%ignore flight_hub_sdk::ILiveSyncModule::deleteLiveSync;
%ignore flight_hub_sdk::ILiveSyncModule::updateLiveSync;
%ignore flight_hub_sdk::ILiveSyncModule::getLiveSyncList;
%ignore flight_hub_sdk::ILiveSyncModule::getLiveSyncDevicesV2;
%ignore flight_hub_sdk::ILiveSyncModule::getLiveSyncSummary;

// 忽略ILogManagementModule的原始方法
%ignore flight_hub_sdk::ILogManagementModule::listenDeviceFileUploadProgress;
%ignore flight_hub_sdk::ILogManagementModule::cancelListenDeviceFileUploadProgress;
%ignore flight_hub_sdk::ILogManagementModule::QueryDeviceLogList;
%ignore flight_hub_sdk::ILogManagementModule::CreateDeviceLogReport;
%ignore flight_hub_sdk::ILogManagementModule::LogReportList;
%ignore flight_hub_sdk::ILogManagementModule::LogReportOperation;
%ignore flight_hub_sdk::ILogManagementModule::UploadLogAppendFile;

// 忽略IMediaServiceModule的原始方法
%ignore flight_hub_sdk::IMediaServiceModule::getMediaFilesList;
%ignore flight_hub_sdk::IMediaServiceModule::deleteMediaFiles;
%ignore flight_hub_sdk::IMediaServiceModule::getMediaFilesDownloadURL;
%ignore flight_hub_sdk::IMediaServiceModule::listenMediaFilePostProcessMessage;
%ignore flight_hub_sdk::IMediaServiceModule::cancelListenMediaFilePostProcessMessage;
%ignore flight_hub_sdk::IMediaServiceModule::listenMediaFileDeviceUploadMessage;
%ignore flight_hub_sdk::IMediaServiceModule::cancelListenMediaFileDeviceUploadMessage;
%ignore flight_hub_sdk::IMediaServiceModule::getMediaUploadSts;
%ignore flight_hub_sdk::IMediaServiceModule::mediaUploadCallback;
%ignore flight_hub_sdk::IMediaServiceModule::uploadGeoMediaFile;
%ignore flight_hub_sdk::IMediaServiceModule::getMediaFileDetailById;

// 忽略ISafetyServiceModule的原始方法
%ignore flight_hub_sdk::ISafetyServiceModule::searchSignalMaps;
%ignore flight_hub_sdk::ISafetyServiceModule::createCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::getCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::updateCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::deleteCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::syncCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::getFlightAreasSyncStatus;
%ignore flight_hub_sdk::ISafetyServiceModule::getOfflineMapsModelInfo;
%ignore flight_hub_sdk::ISafetyServiceModule::updateOfflineMapsModelInfo;
%ignore flight_hub_sdk::ISafetyServiceModule::cancelOfflineMapsModelInfo;
%ignore flight_hub_sdk::ISafetyServiceModule::setOfflineMaps;
%ignore flight_hub_sdk::ISafetyServiceModule::syncOfflineMaps;
%ignore flight_hub_sdk::ISafetyServiceModule::getOfflineMapsSyncStatus;
%ignore flight_hub_sdk::ISafetyServiceModule::getUnlockLicenseList;
%ignore flight_hub_sdk::ISafetyServiceModule::switchUnlockLicense;
%ignore flight_hub_sdk::ISafetyServiceModule::checkUnlockLicense;
%ignore flight_hub_sdk::ISafetyServiceModule::submitUnlockLicense;
%ignore flight_hub_sdk::ISafetyServiceModule::clearDeviceUnlockLicense;
%ignore flight_hub_sdk::ISafetyServiceModule::uploadUnlockLicenseFile;
// EA340 消息推送补全（Phase D）: 飞行器离线地图开关(offline_map_enable), 需要 drone_sn;
%ignore flight_hub_sdk::ISafetyServiceModule::listenDroneOfflineMapEnable;
%ignore flight_hub_sdk::ISafetyServiceModule::cancelListenDroneOfflineMapEnable;

// 忽略IMaintenanceManagementModule的原始方法
%ignore flight_hub_sdk::IMaintenanceManagementModule::getRepairConfigs;
%ignore flight_hub_sdk::IMaintenanceManagementModule::createMaintenanceOrder;
%ignore flight_hub_sdk::IMaintenanceManagementModule::getMaintenanceOrderDetail;
%ignore flight_hub_sdk::IMaintenanceManagementModule::getMaintenanceOrdersList;

// 忽略IDiagnosisManagementModule的原始方法
%ignore flight_hub_sdk::IDiagnosisManagementModule::createSelfDiagnosis;
%ignore flight_hub_sdk::IDiagnosisManagementModule::operateSelfDiagnosis;
%ignore flight_hub_sdk::IDiagnosisManagementModule::getSelfDiagnosisReport;
%ignore flight_hub_sdk::IDiagnosisManagementModule::getSelfDiagnosisReportList;

// IFlightTaskRecordModule: ignore original methods (replaced by wrapper methods)
%ignore flight_hub_sdk::IFlightTaskRecordModule::exportFlightRecords;
%ignore flight_hub_sdk::IFlightTaskRecordModule::getExportFlightRecordsProgress;




// ATC 任务事件

%ignore flight_hub_sdk::IDeployModule::listenDockDeparturePoint;
%ignore flight_hub_sdk::IDeployModule::cancelListenDockDeparturePoint;
%ignore flight_hub_sdk::IDeployModule::listenAircraftVisualScoutStatus;
%ignore flight_hub_sdk::IDeployModule::cancelListenAircraftVisualScoutStatus;
%ignore flight_hub_sdk::IDeployModule::listenAircraftVisualScoutAbnormalInfo;
%ignore flight_hub_sdk::IDeployModule::cancelListenAircraftVisualScoutAbnormalInfo;
%ignore flight_hub_sdk::IDeployModule::listenAircraftVisualScoutObstacleInfo;
%ignore flight_hub_sdk::IDeployModule::cancelListenAircraftVisualScoutObstacleInfo;
%ignore flight_hub_sdk::IDeployModule::listenAircraftEphemerisDownloadState;
%ignore flight_hub_sdk::IDeployModule::cancelListenAircraftEphemerisDownloadState;
%ignore flight_hub_sdk::IDeployModule::listenAircraftEphemerisInfo;
%ignore flight_hub_sdk::IDeployModule::cancelListenAircraftEphemerisInfo;
%ignore flight_hub_sdk::IDeployModule::downloadEphemeris;
%ignore flight_hub_sdk::IDeployModule::executeInOutWaylineDetect;
%ignore flight_hub_sdk::IDeployModule::setInOutWaylineFlight;
%ignore flight_hub_sdk::IDeployModule::getInOutWaylineFlightSetting;

%ignore flight_hub_sdk::IDrcModule::listenLinkStatus;
%ignore flight_hub_sdk::IDrcModule::cancelListenLinkStatus;
%ignore flight_hub_sdk::IDrcModule::connectDrcLink;
%ignore flight_hub_sdk::IDrcModule::disconnectDrcLink;
%ignore flight_hub_sdk::IDrcModule::drcRtcQualityTest;
%ignore flight_hub_sdk::IDrcModule::listenDrcDelayInfo;
%ignore flight_hub_sdk::IDrcModule::cancelListenDrcDelayInfo;
%ignore flight_hub_sdk::IDrcModule::listenDrcCountDownInfo;
%ignore flight_hub_sdk::IDrcModule::cancelListenDrcCountDownInfo;
%ignore flight_hub_sdk::IFlightSettingsModule::setFlightSmartPlanMode;
%ignore flight_hub_sdk::IFlightSettingsModule::setOutOfControlAction;
%ignore flight_hub_sdk::IFlightSettingsModule::setFlightTaskHeight;
%ignore flight_hub_sdk::IFlightSettingsModule::setRthAltitude;
%ignore flight_hub_sdk::IFlightSettingsModule::setAirTransferEnable;
%ignore flight_hub_sdk::ITestFlightModule::generateTryFlightWayline;
%ignore flight_hub_sdk::ITestFlightModule::createTestFlightTask;
%ignore flight_hub_sdk::IOnboardAiModule::listenAIDataInfo;
%ignore flight_hub_sdk::IOnboardAiModule::cancelListenAIDataInfo;
%ignore flight_hub_sdk::IOnboardAiModule::AIIdentifySet;
%ignore flight_hub_sdk::IOnboardAiModule::AIIdentifyFilterSet;
%ignore flight_hub_sdk::IOnboardAiModule::AIIdentifyScoreSet;
%ignore flight_hub_sdk::IOnboardAiModule::AIIdentifyScoreModeSet;
%ignore flight_hub_sdk::IOnboardAiModule::AIIdentifyScoreReset;
%ignore flight_hub_sdk::IOnboardAiModule::AIModelSelect;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAISpotlightZoomSet;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAISpotlightZoomSelect;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAISpotlightZoomConfirm;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAISpotlightZoomStop;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAISpotlightZoomTrack;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAIAlarmStart;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAIAlarmStop;
%ignore flight_hub_sdk::IOnboardAiModule::DrcAIModelsConfig;
%ignore flight_hub_sdk::IOnboardAiModule::listenDrcAIAlarmState;
%ignore flight_hub_sdk::IOnboardAiModule::cancelListenDrcAIAlarmState;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftBatteryInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftBatteryInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftAttitude;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftAttitude;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftLocation;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftLocation;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftModeCode;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftModeCode;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftWindSpeed;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftWindSpeed;
%ignore flight_hub_sdk::IDroneOsdModule::listenFlightTasksException;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenFlightTasksException;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftControlCode;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftControlCode;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftSpeed;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftSpeed;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftPayloadsCameraLiveviewWorldRegion;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftPayloadsCameraLiveviewWorldRegion;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftPayloadsGimbalAttitude;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftPayloadsGimbalAttitude;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftPayloadsList;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftPayloadsList;
%ignore flight_hub_sdk::IDroneOsdModule::listenControlRightsCloudControlStatus;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenControlRightsCloudControlStatus;
%ignore flight_hub_sdk::IDroneOsdModule::listenControlRightsDeviceControlStatus;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenControlRightsDeviceControlStatus;
%ignore flight_hub_sdk::IDroneOsdModule::listenFlightTasksMediaFileDisplayAvailable;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenFlightTasksMediaFileDisplayAvailable;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftPayloadsOsdInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftPayloadsOsdInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftGear;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftGear;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftHomeDistance;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftHomeDistance;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftPositionState;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftPositionState;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftControlSource;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftControlSource;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftLimitsInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftLimitsInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftBatteryWarningThresholds;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftBatteryWarningThresholds;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftFirmwareStatus;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftFirmwareStatus;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftTotalFlightInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftTotalFlightInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftActivationTime;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftActivationTime;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftMaintainStatusInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftMaintainStatusInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftNightLightsState;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftNightLightsState;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftObstacleAvoidanceState;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftObstacleAvoidanceState;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftStorageInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftStorageInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftTrackId;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftTrackId;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftComplianceInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftComplianceInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftPayloadsCamerasList;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftPayloadsCamerasList;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftRcLostAction;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftRcLostAction;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftRthInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftRthInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftHomeGeographicalInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftHomeGeographicalInfo;
%ignore flight_hub_sdk::IDroneOsdModule::listenAircraftCurrentRthMode;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenAircraftCurrentRthMode;
%ignore flight_hub_sdk::IDroneOsdModule::listenHsiInfo;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenHsiInfo;
// EA340 消息推送补全（Phase D）;
%ignore flight_hub_sdk::IDroneOsdModule::listenDroneModeCodeReason;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenDroneModeCodeReason;
%ignore flight_hub_sdk::IDroneOsdModule::listenDroneDongleInfos;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenDroneDongleInfos;
%ignore flight_hub_sdk::IDroneOsdModule::listenDroneFlysafeDatabaseVersion;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenDroneFlysafeDatabaseVersion;
%ignore flight_hub_sdk::IDroneOsdModule::listenDroneWpmzVersion;
%ignore flight_hub_sdk::IDroneOsdModule::cancelListenDroneWpmzVersion;
%ignore flight_hub_sdk::IDockOsdModule::listenDockLocation;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockLocation;
%ignore flight_hub_sdk::IDockOsdModule::listenDroneInDock;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDroneInDock;
%ignore flight_hub_sdk::IDockOsdModule::listenDockCoverState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockCoverState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockModeCode;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockModeCode;
%ignore flight_hub_sdk::IDockOsdModule::listenFlightTaskStepCode;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenFlightTaskStepCode;
%ignore flight_hub_sdk::IDockOsdModule::listenDockMediaFileDetail;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockMediaFileDetail;
%ignore flight_hub_sdk::IDockOsdModule::listenDockWirelessLink;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockWirelessLink;
%ignore flight_hub_sdk::IDockOsdModule::listenDockJobNumber;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockJobNumber;
%ignore flight_hub_sdk::IDockOsdModule::listenDockPowerState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockPowerState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockStorageState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockStorageState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockFirstPowerOnTime;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockFirstPowerOnTime;
%ignore flight_hub_sdk::IDockOsdModule::listenDockAccumulatedRunningTime;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockAccumulatedRunningTime;
%ignore flight_hub_sdk::IDockOsdModule::listenDockAlternateLandPoint;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockAlternateLandPoint;
%ignore flight_hub_sdk::IDockOsdModule::listenDockPositionState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockPositionState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockActivationTime;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockActivationTime;
%ignore flight_hub_sdk::IDockOsdModule::listenDockBatteryStoreMode;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockBatteryStoreMode;
%ignore flight_hub_sdk::IDockOsdModule::listenDockAlarmState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockAlarmState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockDroneBatteryMaintenanceInfo;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockDroneBatteryMaintenanceInfo;
%ignore flight_hub_sdk::IDockOsdModule::listenDockEmergencyStopState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockEmergencyStopState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockSilentMode;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockSilentMode;
%ignore flight_hub_sdk::IDockOsdModule::listenDockUserExperienceImprovement;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockUserExperienceImprovement;
%ignore flight_hub_sdk::IDockOsdModule::listenDockDrcState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockDrcState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockPutterState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockPutterState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockRelativeAlternateLandPoint;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockRelativeAlternateLandPoint;
%ignore flight_hub_sdk::IDockOsdModule::listenDockSelfConvergeCoordinate;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockSelfConvergeCoordinate;
%ignore flight_hub_sdk::IDockOsdModule::listenDockFirmwareInfo;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockFirmwareInfo;
%ignore flight_hub_sdk::IDockOsdModule::listenDockSubDevice;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockSubDevice;
%ignore flight_hub_sdk::IDockOsdModule::listenDockNetworkState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockNetworkState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockDroneChargeState;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockDroneChargeState;
%ignore flight_hub_sdk::IDockOsdModule::listenDockMeteorologicalInfo;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockMeteorologicalInfo;
%ignore flight_hub_sdk::IDockOsdModule::listenDockBackupBatteryInfo;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockBackupBatteryInfo;
%ignore flight_hub_sdk::IDockOsdModule::listenDockMaintainStatusInfo;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockMaintainStatusInfo;
// EA340 消息推送补全（Phase D）;
%ignore flight_hub_sdk::IDockOsdModule::listenDockDongleInfos;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockDongleInfos;
%ignore flight_hub_sdk::IDockOsdModule::listenDockAirConditionerMode;
%ignore flight_hub_sdk::IDockOsdModule::cancelListenDockAirConditionerMode;

%ignore flight_hub_sdk::IDockControlModule::changeDockCameraPosition;
%ignore flight_hub_sdk::IDockControlModule::listenDockAirConditionerStatusInfo;
%ignore flight_hub_sdk::IDockControlModule::cancelListenDockAirConditionerStatusInfo;
%ignore flight_hub_sdk::IDockControlModule::listenDockSupplementLightState;
%ignore flight_hub_sdk::IDockControlModule::cancelListenDockSupplementLightState;
%ignore flight_hub_sdk::IDockControlModule::setDockSupplementLightState;
%ignore flight_hub_sdk::IDockControlModule::setDockChargeState;
%ignore flight_hub_sdk::IDockControlModule::setDockCoverState;
%ignore flight_hub_sdk::IDockControlModule::setDockAirConditionerMode;

// Extend classes to add wrapper methods with callback interfaces
%extend flight_hub_sdk::IAIBoxModule {
    void fetchAIBoxInfoWrapper(std::string const &device_sn,FetchAIBoxInfoCallback *callback) {
        $self->fetchAIBoxInfo(device_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::AIBoxInfoListResults& results) {
            if (callback) { callback->onFetchAIBoxInfo(err, results); delete callback; }
        });
    }

    void fetchFileDownloadUrlWrapper(flight_hub_sdk::FetchFileDownloadUrlParams const &params,FetchFileDownloadUrlCallback *callback) {
        $self->fetchFileDownloadUrl(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FileDownloadUrlListResults& results) {
            if (callback) { callback->onFetchFileDownloadUrl(err, results); delete callback; }
        });
    }

    void fetchAppListWrapper(flight_hub_sdk::FetchAppListParam const &param,FetchAppListCallback *callback) {
        $self->fetchAppList(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FetchAppListResults& results) {
            if (callback) { callback->onFetchAppList(err, results); delete callback; }
        });
    }

    void fetchAppListProgressWrapper(flight_hub_sdk::FetchAppListProgressParam const &param,FetchAppListProgressCallback *callback) {
        $self->fetchAppListProgress(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FetchAppListProgressResults& results) {
            if (callback) { callback->onFetchAppListProgress(err, results); delete callback; }
        });
    }

    void exportFileWrapper(flight_hub_sdk::ExportFileParam const &param,ExportFileCallback *callback) {
        $self->exportFile(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFileResults& results) {
            if (callback) { callback->onExportFile(err, results); delete callback; }
        });
    }

    void exportFileProgressWrapper(flight_hub_sdk::ExportFileProgressParam const &param,ExportFileProgressCallback *callback) {
        $self->exportFileProgress(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFileProgressResults& results) {
            if (callback) { callback->onExportFileProgress(err, results); delete callback; }
        });
    }

    void uploadAIBoxFileWrapper(flight_hub_sdk::UploadFileParam const &param,UploadAIBoxFileCallback *callback) {
        $self->uploadAIBoxFile(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UploadFileResults& results) {
            if (callback) { callback->onUploadAIBoxFile(err, results); delete callback; }
        });
    }

    void installAppWrapper(flight_hub_sdk::InstallAppParam const &param,InstallAppCallback *callback) {
        $self->installApp(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::InstallAppResults& results) {
            if (callback) { callback->onInstallApp(err, results); delete callback; }
        });
    }

    void installAppProgressWrapper(flight_hub_sdk::InstallAppProgressParam const &param,InstallAppProgressCallback *callback) {
        $self->installAppProgress(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::InstallAppProgressResults& results) {
            if (callback) { callback->onInstallAppProgress(err, results); delete callback; }
        });
    }

    void operateAppWrapper(flight_hub_sdk::OperateAppParam const &param,OperateAppCallback *callback) {
        $self->operateApp(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OperateAppResults& results) {
            if (callback) { callback->onOperateApp(err, results); delete callback; }
        });
    }
}

%extend flight_hub_sdk::IDeviceManageModule {
    void getDeviceListWrapper(DeviceListCallback *callback) {
        $self->getDeviceList([callback](const std::vector<flight_hub_sdk::DeviceTopo>& devices, const flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onDeviceList(devices, err);
                    delete callback;
                }
            });
    }

    void getDeviceDetailInfoWrapper(std::string const &device_sn,GetDeviceDetailInfoCallback *callback) {
        $self->getDeviceDetailInfo(device_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceDetailInfo& device_detail) {
                if (callback) {
                    callback->onGetDeviceDetailInfo(err, device_detail);
                    delete callback;
                }
            });
    }

    void modifyDeviceNameWrapper(flight_hub_sdk::ModifyDeviceNameParam const &device_list,ModifyDeviceNameCallback *callback) {
        $self->modifyDeviceName(device_list, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onModifyDeviceName(err);
                    delete callback;
                }
            });
    }

    void unbindDeviceWrapper(std::vector<std::string> const &device_sns,ResultCallbackWithError *callback) {
        $self->unbindDevice(device_sns, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenWorkspaceDeviceChangedWrapper(WorkspaceDeviceChangeCallback *callback) {
        std::shared_ptr<WorkspaceDeviceChangeCallback> cb(callback);
        return $self->listenWorkspaceDeviceChanged([cb](const flight_hub_sdk::WorkspaceDeviceChange& change) {
            cb->onWorkspaceDeviceChange(change);
        });
    }

    void cancelListenWorkspaceDeviceChangedWrapper(int listen_id) {
        $self->cancelListenWorkspaceDeviceChanged(listen_id);
    }

    int listenDeviceTopoChangedWrapper(DeviceTopoChangeCallback *callback) {
        std::shared_ptr<DeviceTopoChangeCallback> cb(callback);
        return $self->listenDeviceTopoChanged([cb](const flight_hub_sdk::DeviceTopoChange& change) {
            cb->onDeviceTopoChange(change);
        });
    }

    void cancelListenDeviceTopoChangedWrapper(int listen_id) {
        $self->cancelListenDeviceTopoChanged(listen_id);
    }

    void getDockDeviceStatusWrapper(std::vector<std::string> const &device_sns,DockDeviceStatusListCallback *callback) {
        $self->getDockDeviceStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DockDeviceStatusResultList& result) {
                if (callback) {
                    callback->onDockDeviceStatusList(err, result);
                    delete callback;
                }
            });
    }

    void getDroneDeviceStatusWrapper(std::vector<std::string> const &device_sns,DroneDeviceStatusListCallback *callback) {
        $self->getDroneDeviceStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DroneDeviceStatusResultList& result) {
                if (callback) {
                    callback->onDroneDeviceStatusList(err, result);
                    delete callback;
                }
            });
    }
}

%extend flight_hub_sdk::IFirmwareManageModule {
    void getLatestFirmwareReleaseNoteWrapper(std::vector<std::string> const &device_model_keys,std::string const &language,GetLatestFirmwareReleaseNoteCallback *callback) {
        $self->getLatestFirmwareReleaseNote(device_model_keys, language, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareReleaseNoteListResult result) {
            if (callback) {
                callback->onGetLatestFirmwareReleaseNote(err, result);
                delete callback;
            }
        });
    }

    void invokeFirmwareUpgradeWrapper(flight_hub_sdk::FirmwareUpgradeParam const &params,InvokeFirmwareUpgradeCallback *callback) {
        $self->invokeFirmwareUpgrade(params, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareUpgradeResult result) {
            if (callback) {
                callback->onInvokeFirmwareUpgrade(err, result);
                delete callback;
            }
        });
    }

    void queryFirmwareUpgradeProgressWrapper(std::string const &device_sn,QueryFirmwareUpgradeProgressCallback *callback) {
        $self->queryFirmwareUpgradeProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareUpgradeProgressResult result) {
            if (callback) {
                callback->onQueryFirmwareUpgradeProgress(err, result);
                delete callback;
            }
        });
    }

    void uploadFirmwareFileWrapper(std::string const &file_path,UploadFirmwareFileCallback *callback) {
        $self->uploadFirmwareFile(file_path, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadFileResults results) {
            if (callback) {
                callback->onUploadFirmwareFile(err, results);
                delete callback;
            }
        });
    }

    void releaseFirmwareWrapper(std::string const &file_id,ReleaseFirmwareCallback *callback) {
        $self->releaseFirmware(file_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onReleaseFirmware(err);
                delete callback;
            }
        });
    }

    void unReleaseFirmwareWrapper(std::string const &file_id,UnReleaseFirmwareCallback *callback) {
        $self->unReleaseFirmware(file_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onUnReleaseFirmware(err);
                delete callback;
            }
        });
    }

    void deleteFirmwareWrapper(std::string const &file_id,DeleteFirmwareCallback *callback) {
        $self->deleteFirmware(file_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onDeleteFirmware(err);
                delete callback;
            }
        });
    }

    void queryFirmwareListWrapper(int page,int page_size,QueryFirmwareListCallback *callback) {
        $self->queryFirmwareList(page, page_size, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareListResult result) {
            if (callback) {
                callback->onQueryFirmwareList(err, result);
                delete callback;
            }
        });
    }

    void queryFirmwareCompatibilityConfigWrapper(QueryFirmwareCompatibilityConfigCallback *callback) {
        $self->queryFirmwareCompatibilityConfig([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareCompatibilityConfigResult result) {
            if (callback) {
                callback->onQueryFirmwareCompatibilityConfig(err, result);
                delete callback;
            }
        });
    }

    void checkFirmwareStatusWrapper(std::vector<std::string> const &device_sns,std::vector<std::string> const &status,CheckFirmwareStatusCallback *callback) {
        $self->checkFirmwareStatus(device_sns, status, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareStatusListResult result) {
            if (callback) {
                callback->onCheckFirmwareStatus(err, result);
                delete callback;
            }
        });
    }

    void submitFirmwareInfoWrapper(flight_hub_sdk::FirmwareInfoParam const &firmware_info,SubmitFirmwareInfoCallback *callback) {
        $self->submitFirmwareInfo(firmware_info, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSubmitFirmwareInfo(err);
                delete callback;
            }
        });
    }

    int listenDeviceOtaProgressWrapper(const std::string& device_sn, DeviceOtaProgressCallback* callback) {
        std::shared_ptr<DeviceOtaProgressCallback> cb(callback);
        return $self->listenDeviceOtaProgress(device_sn, [cb](const flight_hub_sdk::DeviceOtaProgress& msg) {
            cb->onDeviceOtaProgress(msg);
        });
    }
    void cancelListenDeviceOtaProgressWrapper(int listen_id) {
        $self->cancelListenDeviceOtaProgress(listen_id);
    }
}

%extend flight_hub_sdk::IPsdkResourceModule {
    void getPsdkSpeakerResourceListWrapper(PsdkSpeakerResourceListCallback *callback) {
        $self->getPsdkSpeakerResourceList([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResourceList& psdk_speaker_resource_list) {
                if (callback) {
                    callback->onPsdkSpeakerResourceList(err, psdk_speaker_resource_list);
                    delete callback;
                }
            });
    }

    void createPsdkSpeakerResourceWrapper(flight_hub_sdk::CreatePsdkSpeakerResourceParams const &params,PsdkSpeakerResourceCallback *callback) {
        $self->createPsdkSpeakerResource(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResource& psdk_speaker_resource) {
                if (callback) {
                    callback->onPsdkSpeakerResource(err, psdk_speaker_resource);
                    delete callback;
                }
            });
    }

    void editPsdkSpeakerResourceWrapper(flight_hub_sdk::EditPsdkSpeakerResourceParams const &params,PsdkSpeakerResourceCallback *callback) {
        $self->editPsdkSpeakerResource(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResource& psdk_speaker_resource) {
                if (callback) {
                    callback->onPsdkSpeakerResource(err, psdk_speaker_resource);
                    delete callback;
                }
            });
    }

    void deletePsdkSpeakerResourceWrapper(int resource_id,ResultCallbackWithError *callback) {
        $self->deletePsdkSpeakerResource(resource_id, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    delete callback;
                }
            });
    }

    void getSTSWrapper(STSInfoCallback *callback) {
        $self->getSTS([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::STSInfo& sts_info) {
                if (callback) {
                    callback->onSTSInfo(err, sts_info);
                    delete callback;
                }
            });
    }

    void uploadSpeakerAudioFileWrapper(std::string const &file_path,flight_hub_sdk::STSInfo const &sts_info,ResultCallbackWithError *callback) {
        $self->uploadSpeakerAudioFile(file_path, sts_info, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    delete callback;
                }
            });
    }

    void getPsdkWidgetResourcesWrapper(flight_hub_sdk::GetPsdkWidgetResourceParams const &params,PsdkWidgetResourceListCallback *callback) {
        $self->getPsdkWidgetResources(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkWidgetResourceList& psdk_widget_resource_list) {
                if (callback) {
                    callback->onPsdkWidgetResourceList(err, psdk_widget_resource_list);
                    delete callback;
                }
            });
    }
}

%extend flight_hub_sdk::IFlashLedModule {
    void getFlashLedConfigListWrapper(FlashLedConfigListCallback *callback) {
        $self->getFlashLedConfigList([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfigList& list) {
                if (callback) {
                    callback->onFlashLedConfigList(err, list);
                    delete callback;
                }
            });
    }

    void createFlashLedConfigWrapper(flight_hub_sdk::FlashLedConfigParams const &params, FlashLedConfigCallback *callback) {
        $self->createFlashLedConfig(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfig& config) {
                if (callback) {
                    callback->onFlashLedConfig(err, config);
                    delete callback;
                }
            });
    }

    void updateFlashLedConfigWrapper(std::string const &config_id, flight_hub_sdk::FlashLedConfigParams const &params, FlashLedConfigCallback *callback) {
        $self->updateFlashLedConfig(config_id, params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfig& config) {
                if (callback) {
                    callback->onFlashLedConfig(err, config);
                    delete callback;
                }
            });
    }

    void deleteFlashLedConfigWrapper(std::string const &config_id, ResultCallbackWithError *callback) {
        $self->deleteFlashLedConfig(config_id, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    delete callback;
                }
            });
    }
}

%extend flight_hub_sdk::IWaylineManageModule {
    void getWaylineListWrapper(flight_hub_sdk::GetWaylineListParam param,GetWaylineListCallback *callback) {
        $self->getWaylineList(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetWaylineListResult result) {
            if (callback) {
                callback->onGetWaylineList(err, result);
                delete callback;
            }
        });
    }

    void getWaylineDetailWrapper(std::string const &wayline_uuid,GetWaylineDetailCallback *callback) {
        $self->getWaylineDetail(wayline_uuid, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetWaylineDetailResult result) {
            if (callback) {
                callback->onGetWaylineDetail(err, result);
                delete callback;
            }
        });
    }

    void batchDeleteWaylinesWrapper(std::vector<std::string> const &wayline_uuids,BatchDeleteWaylinesCallback *callback) {
        $self->batchDeleteWaylines(wayline_uuids, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::BatchDeleteWaylinesResult result) {
            if (callback) {
                callback->onBatchDeleteWaylines(err, result);
                delete callback;
            }
        });
    }

    void updateWaylineWrapper(std::string const &wayline_uuid,std::string const &wayline_name,UpdateWaylineCallback *callback) {
        $self->updateWayline(wayline_uuid, wayline_name, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::UpdateWaylineResult result) {
            if (callback) {
                callback->onUpdateWayline(err, result);
                delete callback;
            }
        });
    }

    void submitWaylineInfoWrapper(flight_hub_sdk::SubmitWaylineInfoParam param,SubmitWaylineInfoCallback *callback) {
        $self->submitWaylineInfo(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSubmitWaylineInfo(err);
                delete callback;
            }
        });
    }

    void batchDownloadWaylinesWrapper(flight_hub_sdk::BatchDownloadWaylinesParam param,BatchDownloadWaylinesCallback *callback) {
        $self->batchDownloadWaylines(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::BatchDownloadWaylinesResult result) {
            if (callback) {
                callback->onBatchDownloadWaylines(err, result);
                delete callback;
            }
        });
    }

    void duplicateWaylineWrapper(flight_hub_sdk::DuplicateWaylineParam param,DuplicateWaylineCallback *callback) {
        $self->duplicateWayline(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::DuplicateWaylineResult result) {
            if (callback) {
                callback->onDuplicateWayline(err, result);
                delete callback;
            }
        });
    }

    void copyWaylineWrapper(flight_hub_sdk::CopyWaylineParam param, CopyWaylineCallback *callback) {
        $self->copyWayline(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CopyWaylineResult result) {
            if (callback) {
                callback->onCopyWayline(err, result);
                delete callback;
            }
        });
    }

    void concatWaylineWrapper(flight_hub_sdk::ConcatWaylineParam param, ConcatWaylineCallback *callback) {
        $self->concatWayline(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::ConcatWaylineResult result) {
            if (callback) {
                callback->onConcatWayline(err, result);
                delete callback;
            }
        });
    }

    void importKmzWrapper(std::vector<std::string> file_paths, ImportKmzCallback *callback) {
        $self->importKmz(file_paths, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::ImportKmzResult result) {
            if (callback) {
                callback->onImportKmz(err, result);
                delete callback;
            }
        });
    }

    int listenImportWaylineEventWrapper(ImportWaylineEventCallback *callback) {
        std::shared_ptr<ImportWaylineEventCallback> cb(callback);
        return $self->listenImportWaylineEvent([cb](const flight_hub_sdk::ImportWaylineEventResult& result) {
            cb->onImportWaylineEvent(result);
        });
    }

    void cancelListenImportWaylineEventWrapper(int listen_id) {
        $self->cancelListenImportWaylineEvent(listen_id);
    }

    void getWaylineEditorStsWrapper(const flight_hub_sdk::GetWaylineEditorStsParams& params,
                                    GetWaylineEditorStsCallback* callback) {
        $self->getWaylineEditorSts(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetWaylineEditorStsResult& result) {
                if (callback) { callback->onGetWaylineEditorSts(err, result); delete callback; }
            });
    }
}

// AI Inside 机载算法管理 %extend Wrapper
%extend flight_hub_sdk::IAIInsideManageModule {
    void getAIInsideModelsWrapper(GetAIInsideModelsCallback* callback) {
        $self->getAIInsideModels([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelsResult result) {
            if (callback) { callback->onGetAIInsideModels(err, result); delete callback; }
        });
    }

    void getAIInsideModelDevicesWrapper(flight_hub_sdk::GetAIInsideModelDevicesParam param, GetAIInsideModelDevicesCallback* callback) {
        $self->getAIInsideModelDevices(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelDevicesResult result) {
            if (callback) { callback->onGetAIInsideModelDevices(err, result); delete callback; }
        });
    }

    void getAIInsideModelDevicesSummaryWrapper(flight_hub_sdk::GetAIInsideModelDevicesSummaryParam param, GetAIInsideModelDevicesSummaryCallback* callback) {
        $self->getAIInsideModelDevicesSummary(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelDevicesSummaryResult result) {
            if (callback) { callback->onGetAIInsideModelDevicesSummary(err, result); delete callback; }
        });
    }

    void operateAIInsideModelWrapper(flight_hub_sdk::OperateAIInsideModelParam param, OperateAIInsideModelCallback* callback) {
        $self->operateAIInsideModel(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::OperateAIInsideModelResult result) {
            if (callback) { callback->onOperateAIInsideModel(err, result); delete callback; }
        });
    }

    void createAIInsideAlgorithmWrapper(flight_hub_sdk::CreateAIInsideAlgorithmParam param, CreateAIInsideAlgorithmCallback* callback) {
        $self->createAIInsideAlgorithm(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CreateAIInsideAlgorithmResult result) {
            if (callback) { callback->onCreateAIInsideAlgorithm(err, result); delete callback; }
        });
    }

    void fileUploadAIInsideAlgorithmWrapper(flight_hub_sdk::FileUploadAIInsideAlgorithmParam param, FileUploadAIInsideAlgorithmCallback* callback) {
        $self->fileUploadAIInsideAlgorithm(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FileUploadAIInsideAlgorithmResult result) {
            if (callback) { callback->onFileUploadAIInsideAlgorithm(err, result); delete callback; }
        });
    }

    void deleteAIInsideAlgorithmWrapper(flight_hub_sdk::DeleteAIInsideAlgorithmParam param, DeleteAIInsideAlgorithmCallback* callback) {
        $self->deleteAIInsideAlgorithm(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onDeleteAIInsideAlgorithm(err); delete callback; }
        });
    }

    void uploadAlgorithmFileWrapper(flight_hub_sdk::UploadAIInsideAlgorithmFileParam param, UploadAlgorithmFileCallback* callback) {
        $self->uploadAlgorithmFile(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadAIInsideAlgorithmFileResult result) {
            if (callback) { callback->onUploadAlgorithmFile(err, result); delete callback; }
        });
    }
}

%extend flight_hub_sdk::IAIAlertModule {
    void postDeviceSettingWrapper(flight_hub_sdk::PostAiAlertDeviceSettingParam param, PostAiAlertDeviceSettingCallback* callback) {
        $self->postDeviceSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::PostAiAlertDeviceSettingResult result) {
            if (callback) { callback->onPostAiAlertDeviceSetting(err, result); delete callback; }
        });
    }

    void getDeviceSettingWrapper(flight_hub_sdk::GetAiAlertDeviceSettingParam param, GetAiAlertDeviceSettingCallback* callback) {
        $self->getDeviceSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertDeviceSettingResult result) {
            if (callback) { callback->onGetAiAlertDeviceSetting(err, result); delete callback; }
        });
    }

    void postAlgorithmSettingWrapper(flight_hub_sdk::PostAiAlertAlgorithmSettingParam param, PostAiAlertAlgorithmSettingCallback* callback) {
        $self->postAlgorithmSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::PostAiAlertAlgorithmSettingResult result) {
            if (callback) { callback->onPostAiAlertAlgorithmSetting(err, result); delete callback; }
        });
    }

    void getAlgorithmSettingWrapper(flight_hub_sdk::GetAiAlertAlgorithmSettingParam param, GetAiAlertAlgorithmSettingCallback* callback) {
        $self->getAlgorithmSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertAlgorithmSettingResult result) {
            if (callback) { callback->onGetAiAlertAlgorithmSetting(err, result); delete callback; }
        });
    }

    void getAiAlertFlightsWrapper(flight_hub_sdk::GetAiAlertFlightsParam param, GetAiAlertFlightsCallback* callback) {
        $self->getAiAlertFlights(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertFlightsResult result) {
            if (callback) { callback->onGetAiAlertFlights(err, result); delete callback; }
        });
    }

    void getAiAlertRecordsWrapper(flight_hub_sdk::GetAiAlertRecordsParam param, GetAiAlertRecordsCallback* callback) {
        $self->getAiAlertRecords(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertRecordsResult result) {
            if (callback) { callback->onGetAiAlertRecords(err, result); delete callback; }
        });
    }
}

%extend flight_hub_sdk::ILiveSyncModule {
    void createLiveSyncWrapper(flight_hub_sdk::LiveSyncCreateParams param, CreateLiveSyncCallback* callback) {
        $self->createLiveSync(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncCreateResult result) {
            if (callback) { callback->onCreateLiveSync(err, result); delete callback; }
        });
    }

    void deleteLiveSyncWrapper(std::string const& id, DeleteLiveSyncCallback* callback) {
        $self->deleteLiveSync(id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onDeleteLiveSync(err); delete callback; }
        });
    }

    void updateLiveSyncWrapper(flight_hub_sdk::LiveSyncUpdateParams param, UpdateLiveSyncCallback* callback) {
        $self->updateLiveSync(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncItem result) {
            if (callback) { callback->onUpdateLiveSync(err, result); delete callback; }
        });
    }

    void getLiveSyncListWrapper(flight_hub_sdk::LiveSyncListParams param, GetLiveSyncListCallback* callback) {
        $self->getLiveSyncList(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncListResult result) {
            if (callback) { callback->onGetLiveSyncList(err, result); delete callback; }
        });
    }

    void getLiveSyncDevicesV2Wrapper(GetLiveSyncDevicesV2Callback* callback) {
        $self->getLiveSyncDevicesV2([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncDevicesV2Result result) {
            if (callback) { callback->onGetLiveSyncDevicesV2(err, result); delete callback; }
        });
    }

    void getLiveSyncSummaryWrapper(GetLiveSyncSummaryCallback* callback) {
        $self->getLiveSyncSummary([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncSummaryResult result) {
            if (callback) { callback->onGetLiveSyncSummary(err, result); delete callback; }
        });
    }
}

%extend flight_hub_sdk::IFlightTaskModule {
    void getFlightTaskListWrapper(const flight_hub_sdk::FlightTaskListParams& params, GetFlightTaskListCallback* callback) {
        $self->getFlightTaskList(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightTaskListResult& result) {
            if (callback) { callback->onGetFlightTaskList(err, result); delete callback; }
        });
    }
    void getCommandTaskListWrapper(const flight_hub_sdk::GetCommandTaskListParams& params, GetCommandTaskListCallback* callback) {
        $self->getCommandTaskList(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetCommandTaskListResults& results) {
            if (callback) { callback->onGetCommandTaskList(err, results); delete callback; }
        });
    }
    void getFlightTaskTrackWrapper(const flight_hub_sdk::GetFlightTaskTrackParams& params, GetFlightTaskTrackCallback* callback) {
        $self->getFlightTaskTrack(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightTaskTrackResults& results) {
            if (callback) { callback->onGetFlightTaskTrack(err, results); delete callback; }
        });
    }
    void createFlightTaskWrapper(const flight_hub_sdk::CreateTaskReq& req, CreateFlightTaskCallback* callback) {
        $self->createFlightTask(req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateTaskResp& result) {
            if (callback) { callback->onCreateFlightTask(err, result); delete callback; }
        });
    }
    void getFlightTaskDetailWrapper(const std::string& task_uuid, GetFlightTaskDetailCallback* callback) {
        $self->getFlightTaskDetail(task_uuid, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TaskDetail& result) {
            if (callback) { callback->onGetFlightTaskDetail(err, result); delete callback; }
        });
    }
    void editFlightTaskWrapper(const std::string& task_uuid, const flight_hub_sdk::EditTaskReq& req, CreateFlightTaskCallback* callback) {
        $self->editFlightTask(task_uuid, req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateTaskResp& result) {
            if (callback) { callback->onCreateFlightTask(err, result); delete callback; }
        });
    }
    void deleteFlightTaskWrapper(const std::string& task_uuid, ResultCallbackWithError* callback) {
        $self->deleteFlightTask(task_uuid, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void updateFlightTaskStatusWrapper(const std::string& task_uuid, int status, ResultCallbackWithError* callback) {
        $self->updateFlightTaskStatus(task_uuid, status, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void batchUpdateFlightTaskStatusWrapper(const flight_hub_sdk::BatchUpdateStatusReq& req, ResultCallbackWithError* callback) {
        $self->batchUpdateFlightTaskStatus(req, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void getDeviceRecommendationWrapper(const flight_hub_sdk::DeviceRecommendationReq& req, GetDeviceRecommendationCallback* callback) {
        $self->getDeviceRecommendation(req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRecommendationResp& result) {
            if (callback) { callback->onGetDeviceRecommendation(err, result); delete callback; }
        });
    }
    void getDeviceTcasWrapper(const std::vector<std::string>& center_sn, GetDeviceTcasCallback* callback) {
        $self->getDeviceTcas(center_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetTCAInfosResp& result) {
            if (callback) { callback->onGetDeviceTcas(err, result); delete callback; }
        });
    }
    void updateDeviceTcasWrapper(const std::vector<std::string>& center_sn, ResultCallbackWithError* callback) {
        $self->updateDeviceTcas(center_sn, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void startRoamingWrapper(const flight_hub_sdk::StartRoamingParams& params, StartRoamingCallback* callback) {
        $self->startRoaming(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onStartRoaming(err); delete callback; }
        });
    }
    void setRoamingStationSnWrapper(const flight_hub_sdk::SetRoamingStationSnParams& params,
                                    SetRoamingStationSnCallback* callback) {
        $self->setRoamingStationSn(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::SetRoamingStationSnResult& result) {
                if (callback) { callback->onSetRoamingStationSn(err, result); delete callback; }
            });
    }
    void getInFlightTaskToposWrapper(const std::vector<std::string>& drone_sns,
                                     GetInFlightTaskToposCallback* callback) {
        $self->getInFlightTaskTopos(drone_sns,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetInFlightTaskToposResult& result) {
                if (callback) { callback->onGetInFlightTaskTopos(err, result); delete callback; }
            });
    }
    int listenTaskTopoChangedWrapper(TaskTopoChangedCallback* callback) {
        std::shared_ptr<TaskTopoChangedCallback> cb(callback);
        return $self->listenTaskTopoChanged(
            [cb](const flight_hub_sdk::TaskTopoChangedEvent& event) {
                cb->onTaskTopoChanged(event);
            });
    }
    void cancelListenTaskTopoChangedWrapper(int listen_id) {
        $self->cancelListenTaskTopoChanged(listen_id);
    }
}

%extend flight_hub_sdk::ICameraControlModule {
    void switchLiveLensWrapper(const flight_hub_sdk::SwitchLiveLensParams& params, ResultCallbackWithError* callback) {
        $self->drcSwitchLiveLens(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setPhotoStorageSettingsWrapper(const flight_hub_sdk::SetPhotoStorageSettingsParams& params, ResultCallbackWithError* callback) {
        $self->drcSetPhotoStorageSettings(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setVideoStorageSettingsWrapper(const flight_hub_sdk::SetVideoStorageSettingsParams& params, ResultCallbackWithError* callback) {
        $self->drcSetVideoStorageSettings(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setNightSceneModeWrapper(const flight_hub_sdk::SetNightSceneModeParams& params, ResultCallbackWithError* callback) {
        $self->drcSetNightSceneMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void resetGimbalWrapper(const flight_hub_sdk::GimbalResetParams& params, ResultCallbackWithError* callback) {
        $self->drcResetGimbal(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void takePhotoWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->drcTakePhoto(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void startRecordWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->drcStartRecord(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void stopRecordWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->drcStopRecord(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void zoomAtTargetWrapper(const flight_hub_sdk::ZoomAtTargetParams& params, ResultCallbackWithError* callback) {
        $self->drcZoomAtTarget(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setFocalLengthWrapper(const flight_hub_sdk::CameraFocalLengthParams& params, ResultCallbackWithError* callback) {
        $self->drcSetFocalLength(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void lookAtTargetWrapper(const flight_hub_sdk::LookAtTargetParams& params, ResultCallbackWithError* callback) {
        $self->lookAtTarget(params, [callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void startPanoramaWrapper(const flight_hub_sdk::PanoramaParam& params, ResultCallbackWithError* callback) {
        $self->drcStartPanorama(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void stopPanoramaWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->drcStopPanorama(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenPanoramaStatusWrapper(const std::string& payload_index, PanoramaStatusCallback* callback) {
        std::shared_ptr<PanoramaStatusCallback> cb(callback);
        return $self->listenDrcPanoramaStatus(payload_index, [cb](const flight_hub_sdk::PanoramaStatus& status) {
            cb->onPanoramaStatus(status);
        });
    }

    void switchCameraModeWrapper(const flight_hub_sdk::SwitchCameraModeParams& params, ResultCallbackWithError* callback) {
        $self->drcSwitchCameraMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenCameraModeAvailableRangeWrapper(const std::string& payload_index, CameraModeAvailableRangeCallback* callback) {
        std::shared_ptr<CameraModeAvailableRangeCallback> cb(callback);
        return $self->listenDrcCameraModeAvailableRange(payload_index, [cb](const std::vector<int>& camera_modes) {
            cb->onCameraModeAvailableRange(camera_modes);
        });
    }

    int listenCameraStateWrapper(const std::string& payload_index, CameraStateCallback* callback) {
        std::shared_ptr<CameraStateCallback> cb(callback);
        return $self->listenDrcCameraState(payload_index, [cb](const flight_hub_sdk::CameraState& state) {
            cb->onCameraState(state);
        });
    }

    int listenLiveviewWorldRegionWrapper(const std::string& payload_index, LiveviewWorldRegionCallback* callback) {
        std::shared_ptr<LiveviewWorldRegionCallback> cb(callback);
        return $self->listenDrcLiveviewWorldRegion(payload_index, [cb](const flight_hub_sdk::LiveviewWorldRegion& region) {
            cb->onLiveviewWorldRegion(region);
        });
    }

    int listenCameraWideLensStateWrapper(const std::string& payload_index, CameraWideLensStateCallback* callback) {
        std::shared_ptr<CameraWideLensStateCallback> cb(callback);
        return $self->listenDrcCameraWideLensState(payload_index, [cb](const flight_hub_sdk::CameraWideLensState& state) {
            cb->onCameraWideLensState(state);
        });
    }

    int listenCameraZoomLensStateWrapper(const std::string& payload_index, CameraZoomLensStateCallback* callback) {
        std::shared_ptr<CameraZoomLensStateCallback> cb(callback);
        return $self->listenDrcCameraZoomLensState(payload_index, [cb](const flight_hub_sdk::CameraZoomLensState& state) {
            cb->onCameraZoomLensState(state);
        });
    }

    int listenCameraMeasureTargetWrapper(const std::string& payload_index, CameraMeasureTargetCallback* callback) {
        std::shared_ptr<CameraMeasureTargetCallback> cb(callback);
        return $self->listenDrcCameraMeasureTarget(payload_index, [cb](const flight_hub_sdk::CameraMeasureTarget& target) {
            cb->onCameraMeasureTarget(target);
        });
    }

    int listenNightModeSettingsWrapper(const std::string& payload_index, NightModeSettingsCallback* callback) {
        std::shared_ptr<NightModeSettingsCallback> cb(callback);
        return $self->listenDrcNightModeSettings(payload_index, [cb](const flight_hub_sdk::NightModeSettings& settings) {
            cb->onNightModeSettings(settings);
        });
    }

    int listenGimbalStateWrapper(const std::string& payload_index, GimbalStateCallback* callback) {
        std::shared_ptr<GimbalStateCallback> cb(callback);
        return $self->listenDrcGimbalState(payload_index, [cb](const flight_hub_sdk::GimbalState& state) {
            cb->onGimbalState(state);
        });
    }

    int listenLiveVideoInfoWrapper(const std::string& payload_index, LiveVideoInfoCallback* callback) {
        std::shared_ptr<LiveVideoInfoCallback> cb(callback);
        return $self->listenDrcLiveVideoInfo(payload_index, [cb](const flight_hub_sdk::LiveVideoInfo& info) {
            cb->onLiveVideoInfo(info);
        });
    }

    void cancelListenLiveVideoInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcLiveVideoInfo(payload_index, listen_id);
    }

    int listenThermalGainModeWrapper(const std::string& payload_index, ThermalGainModeInfoCallback* callback) {
        std::shared_ptr<ThermalGainModeInfoCallback> cb(callback);
        return $self->listenDrcThermalGainMode(payload_index, [cb](const flight_hub_sdk::ThermalGainModeInfo& info) {
            cb->onThermalGainModeInfo(info);
        });
    }

    void cancelListenThermalGainModeWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcThermalGainMode(payload_index, listen_id);
    }

    int listenCameraIrLensStateWrapper(const std::string& payload_index, CameraIrLensStateCallback* callback) {
        std::shared_ptr<CameraIrLensStateCallback> cb(callback);
        return $self->listenDrcCameraIrLensState(payload_index, [cb](const flight_hub_sdk::CameraIrLensState& state) {
            cb->onCameraIrLensState(state);
        });
    }

    void cancelListenCameraIrLensStateWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIrLensState(payload_index, listen_id);
    }

    int listenCameraIrThermometryWrapper(const std::string& payload_index, CameraIrThermometryInfoCallback* callback) {
        std::shared_ptr<CameraIrThermometryInfoCallback> cb(callback);
        return $self->listenDrcCameraIrThermometry(payload_index, [cb](const flight_hub_sdk::CameraIrThermometryInfo& info) {
            cb->onCameraIrThermometryInfo(info);
        });
    }

    void cancelListenCameraIrThermometryWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIrThermometry(payload_index, listen_id);
    }

    // ── B4 State push 拆分 ──
    int listenCameraWatermarkSettingsWrapper(const std::string& payload_index, CameraWatermarkSettingsCallback* callback) {
        std::shared_ptr<CameraWatermarkSettingsCallback> cb(callback);
        return $self->listenDrcCameraWatermarkSettings(payload_index, [cb](const flight_hub_sdk::CameraWatermarkSettings& v) { cb->onCameraWatermarkSettings(v); });
    }
    void cancelListenCameraWatermarkSettingsWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraWatermarkSettings(payload_index, listen_id);
    }

    int listenCameraStorageInfoWrapper(const std::string& payload_index, CameraStorageInfoCallback* callback) {
        std::shared_ptr<CameraStorageInfoCallback> cb(callback);
        return $self->listenDrcCameraStorageInfo(payload_index, [cb](const flight_hub_sdk::CameraStorageInfo& v) { cb->onCameraStorageInfo(v); });
    }
    void cancelListenCameraStorageInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraStorageInfo(payload_index, listen_id);
    }

    int listenCameraIrPipStateWrapper(const std::string& payload_index, CameraIrPipStateCallback* callback) {
        std::shared_ptr<CameraIrPipStateCallback> cb(callback);
        return $self->listenDrcCameraIrPipState(payload_index, [cb](const flight_hub_sdk::CameraIrPipState& v) { cb->onCameraIrPipState(v); });
    }
    void cancelListenCameraIrPipStateWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIrPipState(payload_index, listen_id);
    }

    int listenCameraIsothermPolygonWrapper(const std::string& payload_index, CameraIsothermPolygonCallback* callback) {
        std::shared_ptr<CameraIsothermPolygonCallback> cb(callback);
        return $self->listenDrcCameraIsothermPolygon(payload_index, [cb](const flight_hub_sdk::CameraIsothermPolygon& v) { cb->onCameraIsothermPolygon(v); });
    }
    void cancelListenCameraIsothermPolygonWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIsothermPolygon(payload_index, listen_id);
    }

    int listenCameraMediaStorageWrapper(const std::string& payload_index, CameraMediaStorageCallback* callback) {
        std::shared_ptr<CameraMediaStorageCallback> cb(callback);
        return $self->listenDrcCameraMediaStorage(payload_index, [cb](const flight_hub_sdk::CameraMediaStorage& v) { cb->onCameraMediaStorage(v); });
    }
    void cancelListenCameraMediaStorageWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraMediaStorage(payload_index, listen_id);
    }

    int listenCameraAlgorithmDehazeInfoWrapper(const std::string& payload_index, CameraAlgorithmDehazeInfoCallback* callback) {
        std::shared_ptr<CameraAlgorithmDehazeInfoCallback> cb(callback);
        return $self->listenDrcCameraAlgorithmDehazeInfo(payload_index, [cb](const flight_hub_sdk::CameraAlgorithmDehazeInfo& v) { cb->onCameraAlgorithmDehazeInfo(v); });
    }
    void cancelListenCameraAlgorithmDehazeInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraAlgorithmDehazeInfo(payload_index, listen_id);
    }

    int listenCameraPreRecordedInfoWrapper(const std::string& payload_index, CameraPreRecordedInfoCallback* callback) {
        std::shared_ptr<CameraPreRecordedInfoCallback> cb(callback);
        return $self->listenDrcCameraPreRecordedInfo(payload_index, [cb](const flight_hub_sdk::CameraPreRecordedInfo& v) { cb->onCameraPreRecordedInfo(v); });
    }
    void cancelListenCameraPreRecordedInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraPreRecordedInfo(payload_index, listen_id);
    }

    int listenCameraCapabilityWrapper(const std::string& payload_index, CameraCapabilityCallback* callback) {
        std::shared_ptr<CameraCapabilityCallback> cb(callback);
        return $self->listenDrcCameraCapability(payload_index, [cb](const flight_hub_sdk::CameraCapability& v) { cb->onCameraCapability(v); });
    }
    void cancelListenCameraCapabilityWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraCapability(payload_index, listen_id);
    }

    int listenCameraSettableCapabilityWrapper(const std::string& payload_index, CameraSettableCapabilityCallback* callback) {
        std::shared_ptr<CameraSettableCapabilityCallback> cb(callback);
        return $self->listenDrcCameraSettableCapability(payload_index, [cb](const flight_hub_sdk::CameraSettableCapability& v) { cb->onCameraSettableCapability(v); });
    }
    void cancelListenCameraSettableCapabilityWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraSettableCapability(payload_index, listen_id);
    }

    int listenCameraVisibleCapabilityWrapper(const std::string& payload_index, CameraVisibleCapabilityCallback* callback) {
        std::shared_ptr<CameraVisibleCapabilityCallback> cb(callback);
        return $self->listenDrcCameraVisibleCapability(payload_index, [cb](const flight_hub_sdk::CameraVisibleCapability& v) { cb->onCameraVisibleCapability(v); });
    }
    void cancelListenCameraVisibleCapabilityWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraVisibleCapability(payload_index, listen_id);
    }

    int listenCameraPhotoInfoWrapper(const std::string& payload_index, CameraPhotoInfoCallback* callback) {
        std::shared_ptr<CameraPhotoInfoCallback> cb(callback);
        return $self->listenDrcCameraPhotoInfo(payload_index, [cb](const flight_hub_sdk::CameraPhotoInfo& v) { cb->onCameraPhotoInfo(v); });
    }
    void cancelListenCameraPhotoInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraPhotoInfo(payload_index, listen_id);
    }

    void smartPhotoTakePhotoWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->drcSmartPhotoTakePhoto(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setLinkageZoomWrapper(const flight_hub_sdk::LinkageZoomParams& params, ResultCallbackWithError* callback) {
        $self->drcSetLinkageZoom(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void sendCameraScreenDragWrapper(const flight_hub_sdk::CameraScreenDragParams& params, ResultCallbackWithError* callback) {
        $self->drcSendCameraScreenDrag(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setPhotoSizeWrapper(const flight_hub_sdk::PhotoSizeParam& params, ResultCallbackWithError* callback) {
        $self->drcSetPhotoSize(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIntervalTakePhotoWrapper(const flight_hub_sdk::IntervalTakePhotoSetParam& params, ResultCallbackWithError* callback) {
        $self->drcSetIntervalTakePhoto(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void intervalTakePhotoWrapper(const flight_hub_sdk::IntervalTakePhotoParam& params, ResultCallbackWithError* callback) {
        $self->drcIntervalTakePhoto(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void stopTakePhotoWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->drcStopTakePhoto(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    int listenIntervalTakePhotoStatusWrapper(const std::string& payload_index, IntervalTakePhotoStatusCallback* callback) {
        std::shared_ptr<IntervalTakePhotoStatusCallback> cb(callback);
        return $self->listenDrcIntervalTakePhotoStatus(payload_index, [cb](const flight_hub_sdk::IntervalTakePhotoStatus& status) {
            cb->onIntervalTakePhotoStatus(status);
        });
    }

    void cancelListenIntervalTakePhotoStatusWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcIntervalTakePhotoStatus(payload_index, listen_id);
    }

    void cameraAimAtTargetWrapper(const flight_hub_sdk::CameraAimParam& params, ResultCallbackWithError* callback) {
        $self->drcCameraAimAtTarget(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setThermalGainModeWrapper(const flight_hub_sdk::ThermalGainModeSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetThermalGainMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    // ══════════════════════════════════════════════
    // Camera Advanced Parameters Wrappers
    // ══════════════════════════════════════════════

    void setCameraApertureValueWrapper(const flight_hub_sdk::CameraApertureValueSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraApertureValue(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraExposureModeWrapper(const flight_hub_sdk::CameraExposureModeSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraExposureMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraExposureValueWrapper(const flight_hub_sdk::CameraExposureSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraExposureValue(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraFocusModeWrapper(const flight_hub_sdk::CameraFocusModeSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraFocusMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraFocusValueWrapper(const flight_hub_sdk::CameraFocusValueSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraFocusValue(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraIsoWrapper(const flight_hub_sdk::CameraIsoSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraIso(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraMechanicalShutterWrapper(const flight_hub_sdk::CameraMechanicalShutterSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraMechanicalShutter(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void cameraPointFocusActionWrapper(const flight_hub_sdk::CameraPointFocusActionParams& params, ResultCallbackWithError* callback) {
        $self->drcCameraPointFocusAction(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraShutterWrapper(const flight_hub_sdk::CameraShutterSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraShutter(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraWatermarkWrapper(const flight_hub_sdk::CameraWatermarkParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraWatermark(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraDewarpingWrapper(const flight_hub_sdk::CameraDewarpingSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraDewarping(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraScreenSplitWrapper(const flight_hub_sdk::CameraScreenSplitParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraScreenSplit(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void cleanFogWrapper(const flight_hub_sdk::CleanFogParams& params, ResultCallbackWithError* callback) {
        $self->drcCleanFog(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setThermalCurrentPaletteWrapper(const flight_hub_sdk::ThermalCurrentPaletteSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetThermalCurrentPalette(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setThermalSuperResolutionEnableWrapper(const flight_hub_sdk::ThermalSuperResolutionEnableParams& params, ResultCallbackWithError* callback) {
        $self->drcSetThermalSuperResolutionEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIrMeteringModeWrapper(const flight_hub_sdk::IrMeteringModeSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIrMeteringMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIrMeteringPointWrapper(const flight_hub_sdk::IrMeteringPointSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIrMeteringPoint(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIrMeteringAreaWrapper(const flight_hub_sdk::IrMeteringAreaSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIrMeteringArea(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIrDisplayModeWrapper(const flight_hub_sdk::SetIrDisplayModeParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIrDisplayMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIrEnhanceModeWrapper(const flight_hub_sdk::SetIrEnhanceModeParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIrEnhanceMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIrMixRateWrapper(const flight_hub_sdk::SetIrMixRateParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIrMixRate(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIsothermEnableWrapper(const flight_hub_sdk::SetIsothermEnableParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIsothermEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIsothermColorRenderingTypeWrapper(const flight_hub_sdk::SetIsothermColorRenderingTypeParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIsothermColorRenderingType(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIsothermLowerUpperRangeWrapper(const flight_hub_sdk::SetIsothermLowerUpperRangeParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIsothermLowerUpperRange(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIsothermRenderingPolygonWrapper(const flight_hub_sdk::SetIsothermRenderingPolygonParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIsothermRenderingPolygon(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setIsothermRoiBasedRenderingEnableWrapper(const flight_hub_sdk::SetIsothermRoiBasedRenderingEnableParams& params, ResultCallbackWithError* callback) {
        $self->drcSetIsothermRoiBasedRenderingEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setInfraredFillLightEnableWrapper(const flight_hub_sdk::InfraredFillLightEnableParams& params, ResultCallbackWithError* callback) {
        $self->drcSetInfraredFillLightEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraNightVisionEnableWrapper(const flight_hub_sdk::CameraNightVisionEnableParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraNightVisionEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void resetCameraSettingsWrapper(const flight_hub_sdk::CameraSettingsResetParams& params, ResultCallbackWithError* callback) {
        $self->drcResetCameraSettings(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraDenoiseLevelSetWrapper(const flight_hub_sdk::CameraDenoiseLevelSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraDenoiseLevelSet(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setVideoResolutionWrapper(const flight_hub_sdk::VideoResolutionSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetVideoResolution(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCameraPhotoFormatWrapper(const flight_hub_sdk::CameraPhotoFormatSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetCameraPhotoFormat(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setPreRecordedStatusWrapper(const flight_hub_sdk::PreRecordedStatusSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetPreRecordedStatus(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setPreRecordedTimeWrapper(const flight_hub_sdk::PreRecordedTimeSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetPreRecordedTime(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setCountDownActionWrapper(int count_down_type, int action_type, ResultCallbackWithError* callback) {
        $self->drcSetCountDownAction(count_down_type, action_type, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setAssistantVideoModeWrapper(int mode, ResultCallbackWithError* callback) {
        $self->drcSetAssistantVideoMode(mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setAssistantVideoSwitchWrapper(int sw, ResultCallbackWithError* callback) {
        $self->drcSetAssistantVideoSwitch(sw, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setAlgorithmDehazeModeWrapper(const flight_hub_sdk::AlgorithmDehazeModeSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetAlgorithmDehazeMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setAlgorithmDehazeLevelWrapper(const flight_hub_sdk::AlgorithmDehazeLevelSetParams& params, ResultCallbackWithError* callback) {
        $self->drcSetAlgorithmDehazeLevel(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    // EA340 消息推送补全（Phase D）: 飞机相机水印设置推送(WebSocket);
    int listenDroneCameraWatermarkSettingsWrapper(DroneCameraWatermarkSettingsPushCallback* callback) {
        std::shared_ptr<DroneCameraWatermarkSettingsPushCallback> cb(callback);
        return $self->listenDroneCameraWatermarkSettings([cb](const flight_hub_sdk::DroneCameraWatermarkSettingsPush& msg) {
            cb->onDroneCameraWatermarkSettingsPush(msg);
        });
    }

    void cancelListenDroneCameraWatermarkSettingsWrapper(int listen_id) {
        $self->cancelListenDroneCameraWatermarkSettings(listen_id);
    }
}

%extend flight_hub_sdk::IDockControlModule {
    void changeDockCameraPositionWrapper(const flight_hub_sdk::LiveInfo& live_info, int camera_position, const std::string& gateway_sn, ResultCallbackWithError* callback) {
        $self->changeDockCameraPosition(live_info, camera_position, gateway_sn, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenDockAirConditionerStatusInfoWrapper(DockAirConditionerStatusInfoCallback* callback) {
        std::shared_ptr<DockAirConditionerStatusInfoCallback> cb(callback);
        return $self->listenDockAirConditionerStatusInfo([cb](const flight_hub_sdk::DockAirConditionerStatusInfo& status_info) {
            cb->onDockAirConditionerStatusInfo(status_info);
        });
    }

    void cancelListenDockAirConditionerStatusInfoWrapper(int listen_id) {
        $self->cancelListenDockAirConditionerStatusInfo(listen_id);
    }

    int listenDockSupplementLightStateWrapper(DockSupplementLightStateCallback* callback) {
        std::shared_ptr<DockSupplementLightStateCallback> cb(callback);
        return $self->listenDockSupplementLightState([cb](const flight_hub_sdk::DockSupplementLightState& light_state) {
            cb->onDockSupplementLightState(light_state);
        });
    }

    void cancelListenDockSupplementLightStateWrapper(int listen_id) {
        $self->cancelListenDockSupplementLightState(listen_id);
    }

    void setDockSupplementLightStateWrapper(bool state, ResultCallbackWithError* callback) {
        $self->setDockSupplementLightState(state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setDockChargeStateWrapper(bool state, ResultCallbackWithError* callback) {
        $self->setDockChargeState(state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setDockCoverStateWrapper(int mode, ResultCallbackWithError* callback) {
        $self->setDockCoverState(mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setDockAirConditionerModeWrapper(int mode, ResultCallbackWithError* callback) {
        $self->setDockAirConditionerMode(mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }
}

%extend flight_hub_sdk::IDeployModule {
    int listenDockDeparturePointWrapper(DockDeparturePointCallback* callback) {
        std::shared_ptr<DockDeparturePointCallback> cb(callback);
        return $self->listenDockDeparturePoint([cb](const flight_hub_sdk::DockDeparturePoint& msg) {
            cb->onDockDeparturePoint(msg);
        });
    }
    void cancelListenDockDeparturePointWrapper(int listen_id) {
        $self->cancelListenDockDeparturePoint(listen_id);
    }

    int listenAircraftVisualScoutStatusWrapper(AircraftVisualScoutStatusCallback* callback) {
        std::shared_ptr<AircraftVisualScoutStatusCallback> cb(callback);
        return $self->listenAircraftVisualScoutStatus([cb](const flight_hub_sdk::AircraftVisualScoutStatus& msg) {
            cb->onAircraftVisualScoutStatus(msg);
        });
    }
    void cancelListenAircraftVisualScoutStatusWrapper(int listen_id) {
        $self->cancelListenAircraftVisualScoutStatus(listen_id);
    }

    int listenAircraftVisualScoutAbnormalInfoWrapper(AircraftVisualScoutAbnormalInfoCallback* callback) {
        std::shared_ptr<AircraftVisualScoutAbnormalInfoCallback> cb(callback);
        return $self->listenAircraftVisualScoutAbnormalInfo([cb](const flight_hub_sdk::AircraftVisualScoutAbnormalInfo& msg) {
            cb->onAircraftVisualScoutAbnormalInfo(msg);
        });
    }
    void cancelListenAircraftVisualScoutAbnormalInfoWrapper(int listen_id) {
        $self->cancelListenAircraftVisualScoutAbnormalInfo(listen_id);
    }

    int listenAircraftVisualScoutObstacleInfoWrapper(AircraftVisualScoutObstacleInfoCallback* callback) {
        std::shared_ptr<AircraftVisualScoutObstacleInfoCallback> cb(callback);
        return $self->listenAircraftVisualScoutObstacleInfo([cb](const flight_hub_sdk::AircraftVisualScoutObstacleInfo& msg) {
            cb->onAircraftVisualScoutObstacleInfo(msg);
        });
    }
    void cancelListenAircraftVisualScoutObstacleInfoWrapper(int listen_id) {
        $self->cancelListenAircraftVisualScoutObstacleInfo(listen_id);
    }

    int listenAircraftEphemerisDownloadStateWrapper(EphemerisDownloadStateCallback* callback) {
        std::shared_ptr<EphemerisDownloadStateCallback> cb(callback);
        return $self->listenAircraftEphemerisDownloadState([cb](const flight_hub_sdk::EphemerisDownloadState& msg) {
            cb->onEphemerisDownloadState(msg);
        });
    }
    void cancelListenAircraftEphemerisDownloadStateWrapper(int listen_id) {
        $self->cancelListenAircraftEphemerisDownloadState(listen_id);
    }

    int listenAircraftEphemerisInfoWrapper(EphemerisInfoCallback* callback) {
        std::shared_ptr<EphemerisInfoCallback> cb(callback);
        return $self->listenAircraftEphemerisInfo([cb](const flight_hub_sdk::EphemerisInfo& msg) {
            cb->onEphemerisInfo(msg);
        });
    }
    void cancelListenAircraftEphemerisInfoWrapper(int listen_id) {
        $self->cancelListenAircraftEphemerisInfo(listen_id);
    }

    void downloadEphemerisWrapper(const flight_hub_sdk::DownloadEphemerisParams& params, ResultCallbackWithError* callback) {
        $self->downloadEphemeris(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void executeInOutWaylineDetectWrapper(const flight_hub_sdk::InOutWaylineDetectParams& params, ResultCallbackWithError* callback) {
        $self->executeInOutWaylineDetect(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void setInOutWaylineFlightWrapper(const flight_hub_sdk::InOutWaylineFlightSettingParams& params, ResultCallbackWithError* callback) {
        $self->setInOutWaylineFlight(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void getInOutWaylineFlightSettingWrapper(InOutWaylineFlightSettingResultCallback* callback) {
        $self->getInOutWaylineFlightSetting([callback](const flight_hub_sdk::InOutWaylineFlightSettingParams& params, flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(params, err); delete callback; }
        });
    }
}

%extend flight_hub_sdk::ILiveModule {
    void startWrapper(ResultCallbackWithError* callback) {
        $self->start([callback](flight_hub_sdk::FlightHubError result) {
            if (callback) {
                callback->onResult(result);
                delete callback;
            }
        });
    }

    void startWithURLWrapper(LiveStreamStartCallback* callback) {
        $self->startWithURL([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::LiveStreamStartResult& result) {
            if (callback) {
                callback->onResult(err, result);
                delete callback;
            }
        });
    }

    void stopWithURLWrapper(ResultCallbackWithError* callback) {
        $self->stopWithURL([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int subscribeLiveStreamWrapper(LiveStreamCallback* callback) {
        std::shared_ptr<LiveStreamCallback> cb(callback);
        return $self->subscribeLiveStream([cb](const std::vector<uint8_t>& data, const flight_hub_sdk::FrameInfo& frame_info) {
            std::vector<unsigned char> data_vector(data.begin(), data.end());
            cb->onLiveStream(data_vector, frame_info);
        });
    }

    int subscribeAiSeiInfoWrapper(AiSeiInfoCallback* callback) {
        std::shared_ptr<AiSeiInfoCallback> cb(callback);
        return $self->subscribeAiSeiInfo([cb](const flight_hub_sdk::AiSeiInfo& ai_sei_info) {
            cb->onAiSeiInfo(ai_sei_info);
        });
    }

    int subscribeArSeiInfoWrapper(ArSeiInfoCallback* callback) {
        std::shared_ptr<ArSeiInfoCallback> cb(callback);
        return $self->subscribeArSeiInfo([cb](const flight_hub_sdk::ArSeiInfo& ar_sei_info) {
            cb->onArSeiInfo(ar_sei_info);
        });
    }

    int subscribeFovSeiInfoWrapper(FovSeiInfoCallback* callback) {
        std::shared_ptr<FovSeiInfoCallback> cb(callback);
        return $self->subscribeFovSeiInfo([cb](const flight_hub_sdk::FovSeiInfo& fov_sei_info) {
            cb->onFovSeiInfo(fov_sei_info);
        });
    }

    int subscribeAssistantSeiInfoWrapper(AssistantSeiInfoCallback* callback) {
        std::shared_ptr<AssistantSeiInfoCallback> cb(callback);
        return $self->subscribeAssistantSeiInfo([cb](const flight_hub_sdk::AssistantSeiInfo& assistant_sei_info) {
            cb->onAssistantSeiInfo(assistant_sei_info);
        });
    }

    int subscribeEisSeiInfoWrapper(EisSeiInfoCallback* callback) {
        std::shared_ptr<EisSeiInfoCallback> cb(callback);
        return $self->subscribeEisSeiInfo([cb](const flight_hub_sdk::EisSeiInfo& eis_sei_info) {
            cb->onEisSeiInfo(eis_sei_info);
        });
    }

    void setLiveQualityWrapper(const flight_hub_sdk::LiveQualitySettingParams& params, ResultCallbackWithError* callback) {
        $self->setLiveQuality(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
}

%ignore flight_hub_sdk::ILiveRecordModule::startLiveRecord;
%ignore flight_hub_sdk::ILiveRecordModule::stopLiveRecord;
%ignore flight_hub_sdk::ILiveRecordModule::getDeviceLiveRecords;
%ignore flight_hub_sdk::ILiveRecordModule::listenRecordState;
%ignore flight_hub_sdk::ILiveRecordModule::notifyRecordState;
%extend flight_hub_sdk::ILiveRecordModule {
    void startLiveRecordWrapper(const std::string& time_zone, ResultCallbackWithError* callback) {
        $self->startLiveRecord(time_zone, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void stopLiveRecordWrapper(ResultCallbackWithError* callback) {
        $self->stopLiveRecord([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void getDeviceLiveRecordsWrapper(const std::string& device_sn, GetDeviceLiveRecordsCallback* callback) {
        $self->getDeviceLiveRecords(device_sn, [callback](flight_hub_sdk::FlightHubError err, const std::vector<flight_hub_sdk::LiveRecordItem>& records) {
            if (callback) { callback->onResult(err, records); delete callback; }
        });
    }
    int listenRecordStateWrapper(LiveRecordStateCallback* callback) {
        std::shared_ptr<LiveRecordStateCallback> cb(callback);
        return $self->listenRecordState([cb](const flight_hub_sdk::LiveRecordStateInfo& info) {
            cb->onRecordState(info);
        });
    }
}

%extend flight_hub_sdk::IFlyToPointModule {
    void startWrapper(const flight_hub_sdk::FlyToPointParams& params, FlyToPointStartCallback* callback) {
        $self->start(params, [callback](flight_hub_sdk::FlightHubError err, const std::string& fly_to_id) {
            if (callback) {
                callback->onFlyToPointStart(err, fly_to_id);
                delete callback;
            }
        });
    }

    void drcStopWrapper(ResultCallbackWithError* callback) {
        $self->drcStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int subscribeFlyToDroneSelfPlanPathWrapper(FlyToDroneSelfPlanPathCallback* callback) {
        std::shared_ptr<FlyToDroneSelfPlanPathCallback> cb(callback);
        return $self->subscribeFlyToDroneSelfPlanPath([cb](const flight_hub_sdk::FlyToDroneSelfPlanPath& fly_to_drone_self_plan_path) {
            cb->onFlyToDroneSelfPlanPath(fly_to_drone_self_plan_path);
        });
    }

    int subscribeFlyToPointProgressWrapper(FlyToPointProgressCallback* callback) {
        std::shared_ptr<FlyToPointProgressCallback> cb(callback);
        return $self->subscribeFlyToPointProgress([cb](const flight_hub_sdk::FlyToPointProgress& fly_to_point_progress) {
            cb->onFlyToPointProgress(fly_to_point_progress);
        });
    }

    void updateTargetPointsWrapper(const flight_hub_sdk::FlyToUpdateTargetPointsParams& params, ResultCallbackWithError* callback) {
        $self->updateTargetPoints(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void unsubscribeFlyToDroneSelfPlanPathWrapper(int listen_id) {
        $self->unsubscribeFlyToDroneSelfPlanPath(listen_id);
    }

    void unsubscribeFlyToPointProgressWrapper(int listen_id) {
        $self->unsubscribeFlyToPointProgress(listen_id);
    }

    int listenFlyToTaskEventWrapper(FlyToTaskEventCallback* callback) {
        std::shared_ptr<FlyToTaskEventCallback> cb(callback);
        return $self->listenFlyToTaskEvent([cb](const flight_hub_sdk::FlyToTaskEvent& event) {
            cb->onFlyToTaskEvent(event);
        });
    }

    void cancelListenFlyToTaskEventWrapper(int listen_id) {
        $self->cancelListenFlyToTaskEvent(listen_id);
    }
}

%extend flight_hub_sdk::IPoiModule {
    void enterWrapper(const flight_hub_sdk::POIEnterParams& params, PoiEnterCallback* callback) {
        $self->enter(params, [callback](flight_hub_sdk::FlightHubError err, const std::string& task_uuid) {
            if (callback) {
                callback->onPoiEnter(err, task_uuid);
                delete callback;
            }
        });
    }

    void exitWrapper(PoiExitCallback* callback) {
        $self->exit([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onPoiExit(err);
                delete callback;
            }
        });
    }

    void updateSpeedWrapper(double speed, PoiUpdateSpeedCallback* callback) {
        $self->updateSpeed(speed, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onPoiUpdateSpeed(err);
                delete callback;
            }
        });
    }

    void getTaskWrapper(PoiGetTaskCallback* callback) {
        $self->getTask([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::POITaskInfo& task) {
            if (callback) {
                callback->onPoiGetTask(err, task);
                delete callback;
            }
        });
    }

    int listenPoiTaskEventWrapper(PoiTaskEventCallback* callback) {
        std::shared_ptr<PoiTaskEventCallback> cb(callback);
        return $self->listenPoiTaskEvent([cb](const flight_hub_sdk::PoiTaskEvent& event) {
            cb->onPoiTaskEvent(event);
        });
    }

    void cancelListenPoiTaskEventWrapper(int listen_id) {
        $self->cancelListenPoiTaskEvent(listen_id);
    }

    int listenFlightPoiProgressWrapper(FlightPoiProgressCallback* callback) {
        std::shared_ptr<FlightPoiProgressCallback> cb(callback);
        return $self->listenFlightPoiProgress([cb](const flight_hub_sdk::FlightPoiProgress& msg) {
            cb->onFlightPoiProgress(msg);
        });
    }
    void cancelListenFlightPoiProgressWrapper(int listen_id) {
        $self->cancelListenFlightPoiProgress(listen_id);
    }

}

%extend flight_hub_sdk::IFlightTaskControlModule {
    int listenWaylineTaskEventWrapper(WaylineTaskEventCallback* callback) {
        std::shared_ptr<WaylineTaskEventCallback> cb(callback);
        return $self->listenWaylineTaskEvent([cb](const flight_hub_sdk::WaylineTaskEvent& event) {
            cb->onWaylineTaskEvent(event);
        });
    }

    void cancelListenWaylineTaskEventWrapper(int listen_id) {
        $self->cancelListenWaylineTaskEvent(listen_id);
    }

    void pauseWrapper(ResultCallbackWithError* callback) {
        $self->pause([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void resumeWrapper(ResultCallbackWithError* callback) {
        $self->resume([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }
}

%extend flight_hub_sdk::ICustomSEIModule {
    bool CustomSEIEncodeWrapper(const std::vector<int>& frame_data, 
                               const std::vector<flight_hub_sdk::SeiItem>& custom_sei_data,
                               std::vector<int>& output_frame_data) {
        return $self->CustomSEIEncode(frame_data, custom_sei_data, output_frame_data);
    }
    
    bool CustomSEIDecodeWrapper(const std::vector<int>& frame_data,
                               flight_hub_sdk::SeiHeader& output_header,
                               std::vector<flight_hub_sdk::SeiItem>& output_sei_data) {
        return $self->CustomSEIDecode(frame_data, output_header, output_sei_data);
    }
}

%extend flight_hub_sdk::IPullStreamModule {
    void startWrapper(const std::string& stream_url, PullStreamStartCallback* callback) {
        $self->start(stream_url, [callback](flight_hub_sdk::FlightHubSdkErrorCode err_code) {
            if (callback) {
                callback->onPullStreamStart(err_code);
                delete callback;
            }
        });
    }

    int subscribeStreamWrapper(PullStreamDataCallback* callback) {
        std::shared_ptr<PullStreamDataCallback> cb(callback);
        return $self->subscribeStream([cb](const std::vector<uint8_t>& data, const flight_hub_sdk::FrameInfo& frame_info) {
            std::vector<unsigned char> data_vector(data.begin(), data.end());
            cb->onPullStreamData(data_vector, frame_info);
        });
    }

    void setErrorCallbackWrapper(PullStreamErrorCallback* callback) {
        $self->setErrorCallback([callback](int error_code, const std::string& error_message) {
            if (callback) {
                callback->onPullStreamError(error_code, error_message);
            }
        });
    }
}

%extend flight_hub_sdk::IDeviceDebugModule {
    void openDebugModeWrapper(const std::string& device_sn, OpenDebugModeCallback* callback) {
        $self->openDebugMode(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDebugModeResult& result) {
            if (callback) {
                callback->onOpenDebugMode(err, result);
                delete callback;
            }
        });
    }

    void closeDebugModeWrapper(const std::string& device_sn, CloseDebugModeCallback* callback) {
        $self->closeDebugMode(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDebugModeResult& result) {
            if (callback) {
                callback->onCloseDebugMode(err, result);
                delete callback;
            }
        });
    }

    void openDockCoverWrapper(const std::string& device_sn, OpenDockCoverCallback* callback) {
        $self->openDockCover(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDockCoverResult& result) {
            if (callback) {
                callback->onOpenDockCover(err, result);
                delete callback;
            }
        });
    }

    void openDockCoverProgressWrapper(const std::string& device_sn, OpenDockCoverProgressCallback* callback) {
        $self->getOpenDockCoverProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDockCoverProgressResult& result) {
            if (callback) {
                callback->onOpenDockCoverProgress(err, result);
                delete callback;
            }
        });
    }

    void closeDockCoverWrapper(const std::string& device_sn, CloseDockCoverCallback* callback) {
        $self->closeDockCover(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDockCoverResult& result) {
            if (callback) {
                callback->onCloseDockCover(err, result);
                delete callback;
            }
        });
    }

    void closeDockCoverProgressWrapper(const std::string& device_sn, CloseDockCoverProgressCallback* callback) {
        $self->getCloseDockCoverProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDockCoverProgressResult& result) {
            if (callback) {
                callback->onCloseDockCoverProgress(err, result);
                delete callback;
            }
        });
    }

    void deviceRebootWrapper(const std::string& device_sn, DeviceRebootCallback* callback) {
        $self->deviceReboot(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceRebootResult& result) {
            if (callback) {
                callback->onDeviceReboot(err, result);
                delete callback;
            }
        });
    }

    void deviceRebootProgressWrapper(const std::string& device_sn, DeviceRebootProgressCallback* callback) {
        $self->getDeviceRebootProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceRebootProgressResult& result) {
            if (callback) {
                callback->onDeviceRebootProgress(err, result);
                delete callback;
            }
        });
    }

    void deviceFormatWrapper(const std::string& device_sn, DeviceFormatCallback* callback) {
        $self->deviceFormat(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceFormatResult& result) {
            if (callback) {
                callback->onDeviceFormat(err, result);
                delete callback;
            }
        });
    }

    void deviceFormatProgressWrapper(const std::string& device_sn, DeviceFormatProgressCallback* callback) {
        $self->getDeviceFormatProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceFormatProgressResult& result) {
            if (callback) {
                callback->onDeviceFormatProgress(err, result);
                delete callback;
            }
        });
    }

    void openDroneWrapper(const std::string& device_sn, OpenDroneCallback* callback) {
        $self->openDrone(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDroneResult& result) {
            if (callback) {
                callback->onOpenDrone(err, result);
                delete callback;
            }
        });
    }

    void closeDroneWrapper(const std::string& device_sn, CloseDroneCallback* callback) {
        $self->closeDrone(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDroneResult& result) {
            if (callback) {
                callback->onCloseDrone(err, result);
                delete callback;
            }
        });
    }

    void openChargeWrapper(const std::string& device_sn, OpenChargeCallback* callback) {
        $self->openCharge(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenChargeResult& result) {
            if (callback) {
                callback->onOpenCharge(err, result);
                delete callback;
            }
        });
    }

    void closeChargeWrapper(const std::string& device_sn, CloseChargeCallback* callback) {
        $self->closeCharge(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseChargeResult& result) {
            if (callback) {
                callback->onCloseCharge(err, result);
                delete callback;
            }
        });
    }

    void setDockAirConditionerModeWrapper(const std::string& device_sn, int mode, ResultCallbackWithError* callback) {
        $self->setDockAirConditionerMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setDockSilentModeWrapper(const std::string& device_sn, int mode, ResultCallbackWithError* callback) {
        $self->setDockSilentMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void setObstacleAvoidanceWrapper(const std::string& device_sn, int obstacle_avoidance_state, SetObstacleAvoidanceCallback* callback) {
        $self->setObstacleAvoidance(device_sn, obstacle_avoidance_state, [callback](flight_hub_sdk::FlightHubError err, SetObstacleAvoidanceResult result) {
            if (callback) {
                callback->onSetObstacleAvoidance(err, result);
                delete callback;
            }
        });
    }

    void setDistanceLimitWrapper(const std::string& device_sn, int distance_limit_state, int distance_limit_value, SetDistanceLimitCallback* callback) {
        $self->setDistanceLimit(device_sn, distance_limit_state, distance_limit_value, [callback](flight_hub_sdk::FlightHubError err, SetDistanceLimitResult result) {
            if (callback) {
                callback->onSetDistanceLimit(err, result);
                delete callback;
            }
        });
    }

    void setHeightLimitWrapper(const std::string& device_sn, int height_limit, SetHeightLimitCallback* callback) {
        $self->setHeightLimit(device_sn, height_limit, [callback](flight_hub_sdk::FlightHubError err, SetHeightLimitResult result) {
            if (callback) {
                callback->onSetHeightLimit(err, result);
                delete callback;
            }
        });
    }

    void setRemainingPowerForRthWrapper(const std::string& device_sn, int remaining_power_for_rth, SetRemainingPowerForRthCallback* callback) {
        $self->setRemainingPowerForRth(device_sn, remaining_power_for_rth, [callback](flight_hub_sdk::FlightHubError err, SetRemainingPowerForRthResult result) {
            if (callback) {
                callback->onSetRemainingPowerForRth(err, result);
                delete callback;
            }
        });
    }

    void setNightLightsStateWrapper(const std::string& device_sn, int night_lights_state, SetNightLightsStateCallback* callback) {
        $self->setNightLightsState(device_sn, night_lights_state, [callback](flight_hub_sdk::FlightHubError err, SetNightLightsStateResult result) {
            if (callback) {
                callback->onSetNightLightsState(err, result);
                delete callback;
            }
        });
    }

    void switchAlarmLightStateWrapper(const std::string& device_sn, int alarm_state, SwitchAlarmStateCallback* callback) {
        $self->switchAlarmLightState(device_sn, alarm_state, [callback](flight_hub_sdk::FlightHubError err, SwitchAlarmStateResult result) {
            if (callback) {
                callback->onSwitchAlarmState(err, result);
                delete callback;
            }
        });
    }

    void esimOperatorSwitchWrapper(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, EsimOperatorSwitchCallback* callback) {
        $self->esimOperatorSwitch(device_sn, imei, operator_type, device_type, [callback](flight_hub_sdk::FlightHubError err, EsimOperatorSwitchResult result) {
            if (callback) {
                callback->onEsimOperatorSwitch(err, result);
                delete callback;
            }
        });
    }

    void simSlotSwitchWrapper(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, SimSlotSwitchCallback* callback) {
        $self->simSlotSwitch(device_sn, imei, sim_slot, device_type, [callback](flight_hub_sdk::FlightHubError err, SimSlotSwitchResult result) {
            if (callback) {
                callback->onSimSlotSwitch(err, result);
                delete callback;
            }
        });
    }

    void setBatteryStoreModeWrapper(const std::string& device_sn, int battery_store_mode, SetBatteryStoreModeCallback* callback) {
        $self->setBatteryStoreMode(device_sn, battery_store_mode, [callback](flight_hub_sdk::FlightHubError err, SetBatteryStoreModeResult result) {
            if (callback) {
                callback->onSetBatteryStoreMode(err, result);
                delete callback;
            }
        });
    }

    void startManualBatterySwapWrapper(const std::string& device_sn, int slot_id, ManualBatterySwapCallback* callback) {
        $self->startManualBatterySwap(device_sn, slot_id, [callback](flight_hub_sdk::FlightHubError err, ManualBatterySwapResult result) {
            if (callback) {
                callback->onManualBatterySwap(err, result);
                delete callback;
            }
        });
    }

    void getManualBatterySwapProgressWrapper(const std::string& device_sn, ManualBatterySwapProgressCallback* callback) {
        $self->getManualBatterySwapProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, ManualBatterySwapProgressResult result) {
            if (callback) {
                callback->onManualBatterySwapProgress(err, result);
                delete callback;
            }
        });
    }

    void setDonglePowerSwitchWrapper(const std::string& device_sn, int power_state, DonglePowerSwitchCallback* callback) {
        $self->setDonglePowerSwitch(device_sn, power_state, [callback](flight_hub_sdk::FlightHubError err, DonglePowerSwitchResult result) {
            if (callback) {
                callback->onDonglePowerSwitch(err, result);
                delete callback;
            }
        });
    }

    void setLowPowerSwitchWrapper(const std::string& device_sn, int low_power_mode, LowPowerSwitchCallback* callback) {
        $self->setLowPowerSwitch(device_sn, low_power_mode, [callback](flight_hub_sdk::FlightHubError err, LowPowerSwitchResult result) {
            if (callback) {
                callback->onLowPowerSwitch(err, result);
                delete callback;
            }
        });
    }

    void getEsimOperatorSwitchProgressWrapper(const std::string& device_sn, EsimOperatorSwitchProgressCallback* callback) {
        $self->getEsimOperatorSwitchProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, EsimOperatorSwitchProgressResult result) {
            if (callback) {
                callback->onEsimOperatorSwitchProgress(err, result);
                delete callback;
            }
        });
    }

    // Geo Caging wrapper
    void setGeoCagingEnableWrapper(const std::string& device_sn, int geo_caging_switch, const std::vector<flight_hub_sdk::GeoCagingInfoParam>& geo_caging_infos, SetGeoCagingEnableCallback* callback) {
        $self->setGeoCagingEnable(device_sn, geo_caging_switch, geo_caging_infos, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetGeoCagingEnable(err);
                delete callback;
            }
        });
    }

    int subscribeDockCoverOpenProgressWrapper(const std::string& device_sn, SubscribeDockCoverOpenProgressCallback* callback) {
        std::shared_ptr<SubscribeDockCoverOpenProgressCallback> cb(callback);
        return $self->subscribeDockCoverOpenProgress(device_sn, [cb](flight_hub_sdk::OpenDockCoverResult result) {
            cb->onSubscribeDockCoverOpenProgress(result);
        });
    }

    void unsubscribeDockCoverOpenProgressWrapper(int listen_id) {
        $self->unsubscribeDockCoverOpenProgress(listen_id);
    }

    int subscribeDockCoverCloseProgressWrapper(const std::string& device_sn, SubscribeDockCoverCloseProgressCallback* callback) {
        std::shared_ptr<SubscribeDockCoverCloseProgressCallback> cb(callback);
        return $self->subscribeDockCoverCloseProgress(device_sn, [cb](flight_hub_sdk::CloseDockCoverResult result) {
            cb->onSubscribeDockCoverCloseProgress(result);
        });
    }

    void unsubscribeDockCoverCloseProgressWrapper(int listen_id) {
        $self->unsubscribeDockCoverCloseProgress(listen_id);
    }

    int subscribeDeviceRebootProgressWrapper(const std::string& device_sn, SubscribeDeviceRebootProgressCallback* callback) {
        std::shared_ptr<SubscribeDeviceRebootProgressCallback> cb(callback);
        return $self->subscribeDeviceRebootProgress(device_sn, [cb](flight_hub_sdk::DeviceRebootResult result) {
            cb->onSubscribeDeviceRebootProgress(result);
        });
    }

    void unsubscribeDeviceRebootProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceRebootProgress(listen_id);
    }

    int subscribeDeviceFormatProgressWrapper(const std::string& device_sn, SubscribeDeviceFormatProgressCallback* callback) {
        std::shared_ptr<SubscribeDeviceFormatProgressCallback> cb(callback);
        return $self->subscribeDeviceFormatProgress(device_sn, [cb](flight_hub_sdk::DeviceFormatResult result) {
            cb->onSubscribeDeviceFormatProgress(result);
        });
    }

    void unsubscribeDeviceFormatProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceFormatProgress(listen_id);
    }

    int subscribeDroneOpenProgressWrapper(const std::string& device_sn, SubscribeDroneOpenProgressCallback* callback) {
        std::shared_ptr<SubscribeDroneOpenProgressCallback> cb(callback);
        return $self->subscribeDroneOpenProgress(device_sn, [cb](flight_hub_sdk::OpenDroneResult result) {
            cb->onSubscribeDroneOpenProgress(result);
        });
    }

    void unsubscribeDroneOpenProgressWrapper(int listen_id) {
        $self->unsubscribeDroneOpenProgress(listen_id);
    }

    int subscribeDroneCloseProgressWrapper(const std::string& device_sn, SubscribeDroneCloseProgressCallback* callback) {
        std::shared_ptr<SubscribeDroneCloseProgressCallback> cb(callback);
        return $self->subscribeDroneCloseProgress(device_sn, [cb](flight_hub_sdk::CloseDroneResult result) {
            cb->onSubscribeDroneCloseProgress(result);
        });
    }

    void unsubscribeDroneCloseProgressWrapper(int listen_id) {
        $self->unsubscribeDroneCloseProgress(listen_id);
    }

    int subscribeDroneFormatProgressWrapper(const std::string& device_sn, SubscribeDroneFormatProgressCallback* callback) {
        std::shared_ptr<SubscribeDroneFormatProgressCallback> cb(callback);
        return $self->subscribeDroneFormatProgress(device_sn, [cb](flight_hub_sdk::DroneFormatResult result) {
            cb->onSubscribeDroneFormatProgress(result);
        });
    }

    void unsubscribeDroneFormatProgressWrapper(int listen_id) {
        $self->unsubscribeDroneFormatProgress(listen_id);
    }

    int subscribeDroneChargeOpenProgressWrapper(const std::string& device_sn, SubscribeDroneChargeOpenProgressCallback* callback) {
        std::shared_ptr<SubscribeDroneChargeOpenProgressCallback> cb(callback);
        return $self->subscribeDroneChargeOpenProgress(device_sn, [cb](flight_hub_sdk::OpenChargeResult result) {
            cb->onSubscribeDroneChargeOpenProgress(result);
        });
    }

    void unsubscribeDroneChargeOpenProgressWrapper(int listen_id) {
        $self->unsubscribeDroneChargeOpenProgress(listen_id);
    }

    int subscribeDroneChargeCloseProgressWrapper(const std::string& device_sn, SubscribeDroneChargeCloseProgressCallback* callback) {
        std::shared_ptr<SubscribeDroneChargeCloseProgressCallback> cb(callback);
        return $self->subscribeDroneChargeCloseProgress(device_sn, [cb](flight_hub_sdk::CloseChargeResult result) {
            cb->onSubscribeDroneChargeCloseProgress(result);
        });
    }

    void unsubscribeDroneChargeCloseProgressWrapper(int listen_id) {
        $self->unsubscribeDroneChargeCloseProgress(listen_id);
    }

    int subscribeDeviceEsimOperatorSwitchProgressWrapper(const std::string& device_sn, SubscribeDeviceEsimOperatorSwitchProgressCallback* callback) {
        std::shared_ptr<SubscribeDeviceEsimOperatorSwitchProgressCallback> cb(callback);
        return $self->subscribeDeviceEsimOperatorSwitchProgress(device_sn, [cb](flight_hub_sdk::EsimOperatorSwitchResult result) {
            cb->onSubscribeDeviceEsimOperatorSwitchProgress(result);
        });
    }

    void unsubscribeDeviceEsimOperatorSwitchProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceEsimOperatorSwitchProgress(listen_id);
    }

    int subscribeDeviceSimSlotSwitchProgressWrapper(const std::string& device_sn, SubscribeDeviceSimSlotSwitchProgressCallback* callback) {
        std::shared_ptr<SubscribeDeviceSimSlotSwitchProgressCallback> cb(callback);
        return $self->subscribeDeviceSimSlotSwitchProgress(device_sn, [cb](flight_hub_sdk::SimSlotSwitchResult result) {
            cb->onSubscribeDeviceSimSlotSwitchProgress(result);
        });
    }

    void unsubscribeDeviceSimSlotSwitchProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceSimSlotSwitchProgress(listen_id);
    }

    void droneFormatWrapper(const std::string& device_sn, DroneFormatCallback* callback) {
        $self->droneFormat(device_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DroneFormatResult& result) {
            if (callback) {
                callback->onDroneFormat(err, result);
                delete callback;
            }
        });
    }

    void droneSdrWorkModeWrapper(const std::string& device_sn, int mode, ResultCallbackWithError* callback) {
        $self->droneSdrWorkMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    // 云环境切换 wrapper
    void cloudEnvironmentSwitchWrapper(const flight_hub_sdk::CloudEnvironmentSwitchParam& param, ResultCallbackWithError* callback) {
        $self->cloudEnvironmentSwitch(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void cloudEnvironmentSwitchCallbackWrapper(const flight_hub_sdk::CloudEnvironmentSwitchCallbackParam& param, ResultCallbackWithError* callback) {
        $self->cloudEnvironmentSwitchCallback(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenDeviceCloudEnvironmentSwitchProgressWrapper(const std::string& device_sn, DeviceCloudEnvironmentSwitchProgressCallback* callback) {
        std::shared_ptr<DeviceCloudEnvironmentSwitchProgressCallback> cb(callback);
        return $self->listenDeviceCloudEnvironmentSwitchProgress(device_sn, [cb](flight_hub_sdk::CloudEnvironmentSwitchResult result) {
            cb->onDeviceCloudEnvironmentSwitchProgress(result);
        });
    }

    void cancelListenDeviceCloudEnvironmentSwitchProgressWrapper(int listen_id) {
        $self->cancelListenDeviceCloudEnvironmentSwitchProgress(listen_id);
    }
}

%extend flight_hub_sdk::IRelayControlModule {
    void setRelayBroadcastHideWrapper(bool is_broadcast_hide, SetRelayBroadcastHideCallback* callback) {
        $self->setRelayBroadcastHide(is_broadcast_hide, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelayBroadcastHide(err);
                delete callback;
            }
        });
    }

    void setRelaySSIDWrapper(const std::string& device_name, SetRelaySSIDCallback* callback) {
        $self->setRelaySSID(device_name, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelaySSID(err);
                delete callback;
            }
        });
    }

    void setRelayPasswordWrapper(const std::string& passwd, SetRelayPasswordCallback* callback) {
        $self->setRelayPassword(passwd, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelayPassword(err);
                delete callback;
            }
        });
    }

    void setRelaySearchModeWrapper(int relay_search_mode, SetRelaySearchModeCallback* callback) {
        $self->setRelaySearchMode(relay_search_mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelaySearchMode(err);
                delete callback;
            }
        });
    }

    void setRelayOccupiedStatusWrapper(int occupied_status, SetRelayOccupiedStatusCallback* callback) {
        $self->setRelayOccupiedStatus(occupied_status, [callback](flight_hub_sdk::FlightHubError err, SetRelayOccupiedStatusResult result) {
            if (callback) {
                callback->onSetRelayOccupiedStatus(err, result);
                delete callback;
            }
        });
    }

    void relayRebootWrapper(RelayRebootCallback* callback) {
        $self->relayReboot([callback](flight_hub_sdk::FlightHubError err, RelayRebootResult result) {
            if (callback) {
                callback->onRelayReboot(err, result);
                delete callback;
            }
        });
    }

    void relayDisconnectWrapper(RelayDisconnectCallback* callback) {
        $self->relayDisconnect([callback](flight_hub_sdk::FlightHubError err, RelayDisconnectResult result) {
            if (callback) {
                callback->onRelayDisconnect(err, result);
                delete callback;
            }
        });
    }
}

%extend flight_hub_sdk::IRtkCalibrationModule {
    void calibrationByNetworkWrapper(const NetworkRtkCalibrationParams& params, RtkCalibrationCallback* callback) {
        $self->calibrationByNetwork(params, [callback](flight_hub_sdk::FlightHubError err, RtkCalibrationResult result) {
            if (callback) {
                callback->onRtkCalibration(err, result);
                delete callback;
            }
        });
    }

    void calibrationByManualWrapper(const ManulRtkCalibrationParams& params, RtkCalibrationCallback* callback) {
        $self->calibrationByManual(params, [callback](flight_hub_sdk::FlightHubError err, RtkCalibrationResult result) {
            if (callback) {
                callback->onRtkCalibration(err, result);
                delete callback;
            }
        });
    }

    int subscribeCalibrationProgressWrapper(SubscribeCalibrationProgressCallback* callback) {
        std::shared_ptr<SubscribeCalibrationProgressCallback> cb(callback);
        return $self->subscribeCalibrationProgress([cb](flight_hub_sdk::RtkCalibrationResult result) {
            cb->onSubscribeCalibrationProgress(result);
        });
    }

    void unsubscribeCalibrationProgressWrapper(int listen_id) {
        $self->unsubscribeCalibrationProgress(listen_id);
    }
}

%extend flight_hub_sdk::IDeviceStateModule {
    int listenDockOnlineStatusWrapper(DockOnlineStatusCallback* callback) {
        std::shared_ptr<DockOnlineStatusCallback> cb(callback);
        return $self->listenDockOnlineStatus([cb](const flight_hub_sdk::DockOnlineStatus& status) {
            cb->onDockOnlineStatus(status);
        });
    }

    void cancelListenDockOnlineStatusWrapper(int listen_id) {
        $self->cancelListenDockOnlineStatus(listen_id);
    }

    int listenDroneOnlineStatusWrapper(DroneOnlineStatusCallback* callback) {
        std::shared_ptr<DroneOnlineStatusCallback> cb(callback);
        return $self->listenDroneOnlineStatus([cb](const flight_hub_sdk::DroneOnlineStatus& status) {
            cb->onDroneOnlineStatus(status);
        });
    }

    void cancelListenDroneOnlineStatusWrapper(int listen_id) {
        $self->cancelListenDroneOnlineStatus(listen_id);
    }
}

%extend flight_hub_sdk::IDeviceHMSModule {
    int listenDeviceHMSWrapper(HmsDataCallback* callback) {
        std::shared_ptr<HmsDataCallback> cb(callback);
        return $self->listenDeviceHMS([cb](const flight_hub_sdk::HmsData& hms_data) {
            cb->onHmsData(hms_data);
        });
    }

    void cancelListenDeviceHMSWrapper(int listen_id) {
        $self->cancelListenDeviceHMS(listen_id);
    }
}

%extend flight_hub_sdk::ILiveDeviceModule {
    int listenLiveAvailableRangeWrapper(LiveAvailableRangeCallback* callback) {
        std::shared_ptr<LiveAvailableRangeCallback> cb(callback);
        return $self->listenLiveAvailableRange(
            [cb](const std::vector<flight_hub_sdk::LiveInfo>& live_infos) {
                cb->onLiveAvailableRange(live_infos);
            });
    }

    void cancelListenLiveAvailableRangeWrapper(int listen_id) {
        $self->cancelListenLiveAvailableRange(listen_id);
    }

    void getAudienceCountWrapper(const flight_hub_sdk::AudienceCountGroup* group, GetAudienceCountCallback* callback) {
        $self->getAudienceCount(group, [callback](flight_hub_sdk::FlightHubError err, const std::vector<flight_hub_sdk::DeviceAudienceCountItem>& result) {
            if (callback) {
                callback->onGetAudienceCount(err, result);
                delete callback;
            }
        });
    }

    // EA340 消息推送补全（Phase D）;
    int listenDockLiveStatusWrapper(DockLiveStatusCallback* callback) {
        std::shared_ptr<DockLiveStatusCallback> cb(callback);
        return $self->listenDockLiveStatus([cb](const flight_hub_sdk::DockLiveStatus& msg) {
            cb->onDockLiveStatus(msg);
        });
    }

    void cancelListenDockLiveStatusWrapper(int listen_id) {
        $self->cancelListenDockLiveStatus(listen_id);
    }

    int listenDockLiveCapacityWrapper(DockLiveCapacityCallback* callback) {
        std::shared_ptr<DockLiveCapacityCallback> cb(callback);
        return $self->listenDockLiveCapacity([cb](const flight_hub_sdk::DockLiveCapacity& msg) {
            cb->onDockLiveCapacity(msg);
        });
    }

    void cancelListenDockLiveCapacityWrapper(int listen_id) {
        $self->cancelListenDockLiveCapacity(listen_id);
    }
}

%extend flight_hub_sdk::ITakeoffModule {
    void takeoffWrapper(const flight_hub_sdk::TakeoffParams& params, TakeoffResultCallback* callback) {
        $self->takeoff(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TakeoffResult& result) {
            if (callback) {
                callback->onResult(err, result);
                delete callback;
            }
        });
    }

    int listenCommandTaskEventWrapper(CommandTaskEventCallback* callback) {
        std::shared_ptr<CommandTaskEventCallback> cb(callback);
        return $self->listenCommandTaskEvent(
            [cb](const flight_hub_sdk::CommandTaskEvent& event) {
                cb->onCommandTaskEvent(event);
            });
    }

    void cancelListenCommandTaskEventWrapper(int listen_id) {
        $self->cancelListenCommandTaskEvent(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDroneCommanderFlightModeWrapper(DroneCommanderFlightModeCallback* callback) {
        std::shared_ptr<DroneCommanderFlightModeCallback> cb(callback);
        return $self->listenDroneCommanderFlightMode([cb](const flight_hub_sdk::DroneCommanderFlightMode& msg) {
            cb->onDroneCommanderFlightMode(msg);
        });
    }

    void cancelListenDroneCommanderFlightModeWrapper(int listen_id) {
        $self->cancelListenDroneCommanderFlightMode(listen_id);
    }

    int listenDroneCurrentCommanderFlightModeWrapper(DroneCurrentCommanderFlightModeCallback* callback) {
        std::shared_ptr<DroneCurrentCommanderFlightModeCallback> cb(callback);
        return $self->listenDroneCurrentCommanderFlightMode([cb](const flight_hub_sdk::DroneCurrentCommanderFlightMode& msg) {
            cb->onDroneCurrentCommanderFlightMode(msg);
        });
    }

    void cancelListenDroneCurrentCommanderFlightModeWrapper(int listen_id) {
        $self->cancelListenDroneCurrentCommanderFlightMode(listen_id);
    }

    int listenDroneCommanderModeLostActionWrapper(DroneCommanderModeLostActionCallback* callback) {
        std::shared_ptr<DroneCommanderModeLostActionCallback> cb(callback);
        return $self->listenDroneCommanderModeLostAction([cb](const flight_hub_sdk::DroneCommanderModeLostAction& msg) {
            cb->onDroneCommanderModeLostAction(msg);
        });
    }

    void cancelListenDroneCommanderModeLostActionWrapper(int listen_id) {
        $self->cancelListenDroneCommanderModeLostAction(listen_id);
    }
}

%extend flight_hub_sdk::IInFlightWaylineModule {
    void createInFlightWaylineTaskWrapper(const flight_hub_sdk::CreateInFlightWaylineTaskParams& params, CreateInFlightWaylineTaskCallback* callback) {
        $self->createInFlightWaylineTask(params, [callback](const flight_hub_sdk::CreateInFlightWaylineTaskResult& result, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(result, err);
                delete callback;
            }
        });
    }

    void controlInFlightWaylineTaskWrapper(flight_hub_sdk::InFlightWaylineControlAction action, ResultCallbackWithError* callback) {
        $self->controlInFlightWaylineTask(action, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void getActiveInFlightWaylineTaskWrapper(GetActiveInFlightWaylineTaskCallback* callback) {
        $self->getActiveInFlightWaylineTask([callback](const flight_hub_sdk::ActiveInFlightWaylineTaskInfo& info, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(info, err);
                delete callback;
            }
        });
    }

    int listenInFlightWaylineProgressWrapper(InFlightWaylineProgressCallback* callback) {
        std::shared_ptr<InFlightWaylineProgressCallback> cb(callback);
        return $self->listenInFlightWaylineProgress([cb](const flight_hub_sdk::InFlightWaylineProgress& progress) {
            cb->onInFlightWaylineProgress(progress);
        });
    }

    void cancelListenInFlightWaylineProgressWrapper(int listen_id) {
        $self->cancelListenInFlightWaylineProgress(listen_id);
    }
}

%extend flight_hub_sdk::IFlightControlModule {
    void drcEmergencyStopWrapper(ResultCallbackWithError* callback) {
        $self->drcEmergencyStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void drcEmergencyLandingWrapper(ResultCallbackWithError* callback) {
        $self->drcEmergencyLanding([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void drcForceLandingWrapper(ResultCallbackWithError* callback) {
        $self->drcForceLanding([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void returnHomeWrapper(ResultCallbackWithError* callback) {
        $self->returnHome([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void returnHomeCancelWrapper(ResultCallbackWithError* callback) {
        $self->returnHomeCancel([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void drcSendStickControlWrapper(const flight_hub_sdk::StickControlData& data) {
        $self->drcSendStickControl(data);
    }

    // ══════════════════════════════════════════════
    // Flight Control Auxiliary Wrappers
    // ══════════════════════════════════════════════

    void drcSetDroneHoverWrapper(int duration, ResultCallbackWithError* callback) {
        $self->drcSetDroneHover(duration, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void drcSetFollowMouseParameterWrapper(int speed, ResultCallbackWithError* callback) {
        $self->drcSetFollowMouseParameter(speed, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void drcSetNightLightsStateWrapper(int night_lights_state, ResultCallbackWithError* callback) {
        $self->drcSetNightLightsState(night_lights_state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void drcSetStealthStateWrapper(int stealth_state, ResultCallbackWithError* callback) {
        $self->drcSetStealthState(stealth_state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void drcOpenParachuteWrapper(int force, ResultCallbackWithError* callback) {
        $self->drcOpenParachute(force, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void drcSetFlashLedWrapper(const flight_hub_sdk::FlashLedParams& params, ResultCallbackWithError* callback) {
        $self->drcSetFlashLed(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void drcCameraLookAtWrapper(const flight_hub_sdk::CameraLookAtParams& params, ResultCallbackWithError* callback) {
        $self->drcCameraLookAt(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    // ══════════════════════════════════════════════
    // Northbound Push Listener Wrappers
    // ══════════════════════════════════════════════

    int listenDrcOsdInfoWrapper(DrcOsdInfoCallback* callback) {
        std::shared_ptr<DrcOsdInfoCallback> cb(callback);
        return $self->listenDrcOsdInfo([cb](const flight_hub_sdk::DrcOsdInfo& info) {
            cb->onDrcOsdInfo(info);
        });
    }

    void cancelListenDrcOsdInfoWrapper(int listen_id) {
        $self->cancelListenDrcOsdInfo(listen_id);
    }

    int listenDrcBatteriesInfoWrapper(DrcBatteriesInfoCallback* callback) {
        std::shared_ptr<DrcBatteriesInfoCallback> cb(callback);
        return $self->listenDrcBatteriesInfo([cb](const flight_hub_sdk::DrcBatteriesInfo& info) {
            cb->onDrcBatteriesInfo(info);
        });
    }

    void cancelListenDrcBatteriesInfoWrapper(int listen_id) {
        $self->cancelListenDrcBatteriesInfo(listen_id);
    }

    int listenDrcParachuteInfoWrapper(DrcParachuteInfoCallback* callback) {
        std::shared_ptr<DrcParachuteInfoCallback> cb(callback);
        return $self->listenDrcParachuteInfo([cb](const flight_hub_sdk::DrcParachuteInfo& info) {
            cb->onDrcParachuteInfo(info);
        });
    }

    void cancelListenDrcParachuteInfoWrapper(int listen_id) {
        $self->cancelListenDrcParachuteInfo(listen_id);
    }

    int listenDrcFlashLedWrapper(FlashLedStateInfoCallback* callback) {
        std::shared_ptr<FlashLedStateInfoCallback> cb(callback);
        return $self->listenDrcFlashLed([cb](const flight_hub_sdk::FlashLedStateInfo& info) {
            cb->onFlashLedStateInfo(info);
        });
    }

    void cancelListenDrcFlashLedWrapper(int listen_id) {
        $self->cancelListenDrcFlashLed(listen_id);
    }
}

%extend flight_hub_sdk::IControlAuthModule {
    void acquireFlightControlAuthorityWrapper(ResultCallbackWithError* callback) {
        $self->acquireFlightControlAuthority([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void releaseFlightControlAuthorityWrapper(ResultCallbackWithError* callback) {
        $self->releaseFlightControlAuthority([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenFlightControlAuthorityWrapper(FlightControlAuthorityCallback* callback) {
        std::shared_ptr<FlightControlAuthorityCallback> cb(callback);
        return $self->listenFlightControlAuthority([cb](bool available) {
            cb->onFlightControlAuthority(available);
        });
    }

    void acquirePayloadControlAuthorityWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->acquirePayloadControlAuthority(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void releasePayloadControlAuthorityWrapper(const std::string& payload_index, ResultCallbackWithError* callback) {
        $self->releasePayloadControlAuthority(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenPayloadControlAuthorityWrapper(const std::string& payload_index, PayloadControlAuthorityCallback* callback) {
        std::shared_ptr<PayloadControlAuthorityCallback> cb(callback);
        return $self->listenPayloadControlAuthority(payload_index, [cb](bool available) {
            cb->onPayloadControlAuthority(available);
        });
    }
}

%extend flight_hub_sdk::IDrcModule {
    int listenLinkStatusWrapper(LinkStatusCallback* callback) {
        std::shared_ptr<LinkStatusCallback> cb(callback);
        return $self->listenLinkStatus([cb](const flight_hub_sdk::LinkStatus& status) {
            cb->onLinkStatus(status);
        });
    }
    void cancelListenLinkStatusWrapper(int listen_id) {
        $self->cancelListenLinkStatus(listen_id);
    }
    void connectDrcLinkWrapper(ResultCallback* callback) {
        $self->connectDrcLink([callback](flight_hub_sdk::FlightHubSdkErrorCode err_code) {
            if (callback) {
                flight_hub_sdk::FlightHubError err = flight_hub_sdk::FlightHubError::from_sdk(err_code);
                callback->onResult(err);
                delete callback;
            }
        });
    }
    void disconnectDrcLinkWrapper(ResultCallback* callback) {
        $self->disconnectDrcLink([callback](flight_hub_sdk::FlightHubSdkErrorCode err_code) {
            if (callback) {
                flight_hub_sdk::FlightHubError err = flight_hub_sdk::FlightHubError::from_sdk(err_code);
                callback->onResult(err);
                delete callback;
            }
        });
    }

    // ══════════════════════════════════════════════
    // DRC Link Auxiliary Wrappers
    // ══════════════════════════════════════════════

    void drcRtcQualityTestWrapper(const flight_hub_sdk::RtcQualityTestParams& params, ResultCallbackWithError* callback) {
        $self->drcRtcQualityTest(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    int listenDrcDelayInfoWrapper(DrcDelayInfoCallback* callback) {
        std::shared_ptr<DrcDelayInfoCallback> cb(callback);
        return $self->listenDrcDelayInfo([cb](const flight_hub_sdk::DrcDelayInfo& info) {
            cb->onDrcDelayInfo(info);
        });
    }

    void cancelListenDrcDelayInfoWrapper(int listen_id) {
        $self->cancelListenDrcDelayInfo(listen_id);
    }

    int listenDrcCountDownInfoWrapper(DrcCountDownInfoCallback* callback) {
        std::shared_ptr<DrcCountDownInfoCallback> cb(callback);
        return $self->listenDrcCountDownInfo([cb](const flight_hub_sdk::DrcCountDownInfo& info) {
            cb->onDrcCountDownInfo(info);
        });
    }

    void cancelListenDrcCountDownInfoWrapper(int listen_id) {
        $self->cancelListenDrcCountDownInfo(listen_id);
    }
}

%extend flight_hub_sdk::IFlightSettingsModule {
    void setFlightSmartPlanModeWrapper(const std::string& device_sn, int mode, SetFlightSmartPlanModeCallback* callback) {
        $self->setFlightSmartPlanMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightSmartPlanModeResult& result) {
            if (callback) { callback->onSetFlightSmartPlanMode(err, result); delete callback; }
        });
    }
    void setOutOfControlActionWrapper(const std::string& device_sn, const std::string& action, SetOutOfControlActionCallback* callback) {
        $self->setOutOfControlAction(device_sn, action, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightOutOfControlActionResult& result) {
            if (callback) { callback->onSetOutOfControlAction(err, result); delete callback; }
        });
    }
    void setFlightTaskHeightWrapper(const std::string& device_sn, int height, SetFlightTaskHeightCallback* callback) {
        $self->setFlightTaskHeight(device_sn, height, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightCommanderHeightResult& result) {
            if (callback) { callback->onSetFlightTaskHeight(err, result); delete callback; }
        });
    }
    void setRthAltitudeWrapper(const std::string& device_sn, int altitude, SetRthAltitudeCallback* callback) {
        $self->setRthAltitude(device_sn, altitude, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightRthAltitudeResult& result) {
            if (callback) { callback->onSetRthAltitude(err, result); delete callback; }
        });
    }
    void setAirTransferEnableWrapper(const std::string& device_sn, bool enabled, SetAirTransferEnableCallback* callback) {
        $self->setAirTransferEnable(device_sn, enabled, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onSetAirTransferEnable(err); delete callback; }
        });
    }
}

%extend flight_hub_sdk::ITestFlightModule {
    void generateTryFlightWaylineWrapper(const std::string& project_id, const flight_hub_sdk::GenerateTryFlightWaylineParams& params, GenerateTryFlightWaylineCallback* callback) {
        $self->generateTryFlightWayline(project_id, params, [callback](const flight_hub_sdk::GenerateTryFlightWaylineResponse& response, flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onGenerateTryFlightWayline(response, err); delete callback; }
        });
    }
    void createTestFlightTaskWrapper(const std::string& project_id, const flight_hub_sdk::CreateTestFlightTaskParams& params, CreateTestFlightTaskCallback* callback) {
        $self->createTestFlightTask(project_id, params, [callback](const flight_hub_sdk::CreateTestFlightTaskResponse& response, flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onCreateTestFlightTask(response, err); delete callback; }
        });
    }
}

%extend flight_hub_sdk::IDockOsdModule {
    int listenDockLocationWrapper(DockLocationCallback* callback) {
        std::shared_ptr<DockLocationCallback> cb(callback);
        return $self->listenDockLocation([cb](const flight_hub_sdk::DockLocation& location) {
            cb->onDockLocation(location);
        });
    }

    void cancelListenDockLocationWrapper(int listen_id) {
        $self->cancelListenDockLocation(listen_id);
    }

    int listenDroneInDockWrapper(DroneInDockCallback* callback) {
        std::shared_ptr<DroneInDockCallback> cb(callback);
        return $self->listenDroneInDock([cb](const flight_hub_sdk::DroneInDock& drone_in_dock) {
            cb->onDroneInDock(drone_in_dock);
        });
    }

    void cancelListenDroneInDockWrapper(int listen_id) {
        $self->cancelListenDroneInDock(listen_id);
    }

    // 监听机场 mode code;
    int listenDockModeCodeWrapper(DockModeCodeCallback* callback) {
        std::shared_ptr<DockModeCodeCallback> cb(callback);
        return $self->listenDockModeCode([cb](const flight_hub_sdk::DockModeCode& dock_mode_code) {
            cb->onDockModeCode(dock_mode_code);
        });
    }

    // 取消监听机场 mode code;
    void cancelListenDockModeCodeWrapper(int listen_id) {
        $self->cancelListenDockModeCode(listen_id);
    }

    // 监听飞行任务 step code;
    int listenFlightTaskStepCodeWrapper(FlightTaskStepCodeCallback* callback) {
        std::shared_ptr<FlightTaskStepCodeCallback> cb(callback);
        return $self->listenFlightTaskStepCode([cb](const flight_hub_sdk::FlightTaskStepCode& flight_task_step_code) {
            cb->onFlightTaskStepCode(flight_task_step_code);
        });
    }

    // 取消监听飞行任务 step code;
    void cancelListenFlightTaskStepCodeWrapper(int listen_id) {
        $self->cancelListenFlightTaskStepCode(listen_id);
    }

    // 机场: 子设备信息列表(sub_device);
    int listenDockSubDeviceWrapper(DockSubDeviceInfoListCallback* callback) {
        std::shared_ptr<DockSubDeviceInfoListCallback> cb(callback);
        return $self->listenDockSubDevice([cb](const flight_hub_sdk::DockSubDeviceInfoList& dock_sub_device_info_list) {
            cb->onDockSubDeviceInfoList(dock_sub_device_info_list);
        });
    }

    // 取消监听机场子设备信息列表(sub_device);
    void cancelListenDockSubDeviceWrapper(int listen_id) {
        $self->cancelListenDockSubDevice(listen_id);
    }

    // 监听机场网络状态(network_state);
    int listenDockNetworkStateWrapper(DockNetworkStateCallback* callback) {
        std::shared_ptr<DockNetworkStateCallback> cb(callback);
        return $self->listenDockNetworkState([cb](const flight_hub_sdk::DockNetworkState& dock_network_state) {
            cb->onDockNetworkState(dock_network_state);
        });
    }

    // 取消监听机场网络状态(network_state);
    void cancelListenDockNetworkStateWrapper(int listen_id) {
        $self->cancelListenDockNetworkState(listen_id);
    }

    // 监听机场舱盖状态;
    int listenDockCoverStateWrapper(DockCoverStateCallback* callback) {
        std::shared_ptr<DockCoverStateCallback> cb(callback);
        return $self->listenDockCoverState([cb](const flight_hub_sdk::DockCoverState& dock_cover_state) {
            cb->onDockCoverState(dock_cover_state);
        });
    }

    // 取消监听机场舱盖状态;
    void cancelListenDockCoverStateWrapper(int listen_id) {
        $self->cancelListenDockCoverState(listen_id);
    }

    // 监听: 机场媒体文件数量(media_file_detail);
    int listenDockMediaFileDetailWrapper(DockMediaFileDetailCallback* callback) {
        std::shared_ptr<DockMediaFileDetailCallback> cb(callback);
        return $self->listenDockMediaFileDetail([cb](const flight_hub_sdk::DockMediaFileDetail& dock_media_file_detail) {
            cb->onDockMediaFileDetail(dock_media_file_detail);
        });
    }

    // 取消监听: 机场媒体文件数量(media_file_detail);
    void cancelListenDockMediaFileDetailWrapper(int listen_id) {
        $self->cancelListenDockMediaFileDetail(listen_id);
    }


    // 机场: 图传质量(wireless_link);
    int listenDockWirelessLinkWrapper(DockWirelessLinkCallback* callback) {
        std::shared_ptr<DockWirelessLinkCallback> cb(callback);
        return $self->listenDockWirelessLink([cb](const flight_hub_sdk::DockWirelessLink& dock_wireless_link) {
            cb->onDockWirelessLink(dock_wireless_link);
        });
    }

    // 取消监听机场图传质量(wireless_link);
    void cancelListenDockWirelessLinkWrapper(int listen_id) {
        $self->cancelListenDockWirelessLink(listen_id);
    }


    // 机场: 机场作业次数(job_number);
    int listenDockJobNumberWrapper(DockJobNumberCallback* callback) {
        std::shared_ptr<DockJobNumberCallback> cb(callback);
        return $self->listenDockJobNumber([cb](const flight_hub_sdk::DockJobNumber& dock_job_number) {
            cb->onDockJobNumber(dock_job_number);
        });
    }

    // 取消监听机场作业次数(job_number);
    void cancelListenDockJobNumberWrapper(int listen_id) {
        $self->cancelListenDockJobNumber(listen_id);
    }


    // 机场：DockPowerState；
    int listenDockPowerStateWrapper(DockPowerStateCallback* callback) {
        std::shared_ptr<DockPowerStateCallback> cb(callback);
        return $self->listenDockPowerState([cb](const flight_hub_sdk::DockPowerState& dock_power_state) {
            cb->onDockPowerState(dock_power_state);
        });
    }

    // 取消监听机场 DockPowerState;
    void cancelListenDockPowerStateWrapper(int listen_id) {
        $self->cancelListenDockPowerState(listen_id);
    }


     // 机场: 存储容量(storage);
    int listenDockStorageStateWrapper(DockStorageStateCallback* callback) {
        std::shared_ptr<DockStorageStateCallback> cb(callback);
        return $self->listenDockStorageState([cb](const flight_hub_sdk::DockStorageState& dock_storage_state) {
            cb->onDockStorageState(dock_storage_state);
        });
    }

    // 取消监听机场存储容量(storage);
    void cancelListenDockStorageStateWrapper(int listen_id) {
        $self->cancelListenDockStorageState(listen_id);
    }


    // 机场: 首次上电时间(first_power_on);
    int listenDockFirstPowerOnTimeWrapper(DockFirstPowerOnTimeCallback* callback) {
        std::shared_ptr<DockFirstPowerOnTimeCallback> cb(callback);
        return $self->listenDockFirstPowerOnTime([cb](const flight_hub_sdk::DockFirstPowerOnTime& first_power_on_time) {
            cb->onDockFirstPowerOnTime(first_power_on_time);
        });
    }

    // 取消监听首次上电时间(first_power_on);
    void cancelListenDockFirstPowerOnTimeWrapper(int listen_id) {
        $self->cancelListenDockFirstPowerOnTime(listen_id);
    }


    // 监听机场累计运行时长(acc_time);
    int listenDockAccumulatedRunningTimeWrapper(DockAccTimeCallback* callback) {
        std::shared_ptr<DockAccTimeCallback> cb(callback);
        return $self->listenDockAccumulatedRunningTime([cb](const flight_hub_sdk::DockAccTime& dock_acc_time) {
            cb->onDockAccTime(dock_acc_time);
        });
    }

    // 取消监听机场累计运行时长(acc_time);
    void cancelListenDockAccumulatedRunningTimeWrapper(int listen_id) {
        $self->cancelListenDockAccumulatedRunningTime(listen_id);
    }


    // 监听机场备降点(alternate_land_point);
    int listenDockAlternateLandPointWrapper(DockAlternateLandPointCallback* callback) {
        std::shared_ptr<DockAlternateLandPointCallback> cb(callback);
        return $self->listenDockAlternateLandPoint([cb](const flight_hub_sdk::DockAlternateLandPoint& dock_alternate_land_point) {
            cb->onDockAlternateLandPoint(dock_alternate_land_point);
        });
    }

    // 取消监听机场备降点(alternate_land_point);
    void cancelListenDockAlternateLandPointWrapper(int listen_id) {
        $self->cancelListenDockAlternateLandPoint(listen_id);
    }


    // 监听搜星状态(position_state);
    int listenDockPositionStateWrapper(DockPositionStateCallback* callback) {
        std::shared_ptr<DockPositionStateCallback> cb(callback);
        return $self->listenDockPositionState([cb](const flight_hub_sdk::DockPositionState& dock_position_state) {
            cb->onDockPositionState(dock_position_state);
        });
    }

    // 取消监听搜星状态(position_state);
    void cancelListenDockPositionStateWrapper(int listen_id) {
        $self->cancelListenDockPositionState(listen_id);
    }


    // 监听机场激活时间(activation_time, unix时间戳 s);
    int listenDockActivationTimeWrapper(DockActivationTimeCallback* callback) {
        std::shared_ptr<DockActivationTimeCallback> cb(callback);
        return $self->listenDockActivationTime([cb](const flight_hub_sdk::DockActivationTime& dock_activateion_time) {
            cb->onDockActivationTime(dock_activateion_time);
        });
    }

    // 取消监听机场激活时间(activation_time, unix时间戳 s);
    void cancelListenDockActivationTimeWrapper(int listen_id) {
        $self->cancelListenDockActivationTime(listen_id);
    }


    // 监听机场电池(存储)模式(battery_store_mode);
    int listenDockBatteryStoreModeWrapper(DockBatteryStoreModeCallback* callback) {
        std::shared_ptr<DockBatteryStoreModeCallback> cb(callback);
        return $self->listenDockBatteryStoreMode([cb](const flight_hub_sdk::DockBatteryStoreMode& dock_battery_store_mode) {
            cb->onDockBatteryStoreMode(dock_battery_store_mode);
        });
    }

    // 取消监听机场电池(存储)模式(battery_store_mode);
    void cancelListenDockBatteryStoreModeWrapper(int listen_id) {
        $self->cancelListenDockBatteryStoreMode(listen_id);
    }


    // 监听机场声光报警状态(alarm_state);
    int listenDockAlarmStateWrapper(DockAlarmStateCallback* callback) {
        std::shared_ptr<DockAlarmStateCallback> cb(callback);
        return $self->listenDockAlarmState([cb](const flight_hub_sdk::DockAlarmState& dock_alarm_state) {
            cb->onDockAlarmState(dock_alarm_state);
        });
    }

    // 取消监听机场: 声光报警状态(alarm_state);
    void cancelListenDockAlarmStateWrapper(int listen_id) {
        $self->cancelListenDockAlarmState(listen_id);
    }


    // 监听机场: 飞行器电池保养信息(drone_battery_maintenance_info);
    int listenDockDroneBatteryMaintenanceInfoWrapper(DockDroneBatteryMaintenanceInfoCallback* callback) {
        std::shared_ptr<DockDroneBatteryMaintenanceInfoCallback> cb(callback);
        return $self->listenDockDroneBatteryMaintenanceInfo([cb](const flight_hub_sdk::DockDroneBatteryMaintenanceInfo& dock_drone_battery_maintenance_info) {
            cb->onDockDroneBatteryMaintenanceInfo(dock_drone_battery_maintenance_info);
        });
    }

    // 取消监听机场: 飞行器电池保养信息(drone_battery_maintenance_info);
    void cancelListenDockDroneBatteryMaintenanceInfoWrapper(int listen_id) {
        $self->cancelListenDockDroneBatteryMaintenanceInfo(listen_id);
    }


    // 监听机场: 紧急停止按钮状态(emergency_stop_state);
    int listenDockEmergencyStopStateWrapper(DockEmergencyStopStateCallback* callback) {
        std::shared_ptr<DockEmergencyStopStateCallback> cb(callback);
        return $self->listenDockEmergencyStopState([cb](const flight_hub_sdk::DockEmergencyStopState& state) {
            cb->onDockEmergencyStopState(state);
        });
    }

    // 取消监听机场: 紧急停止按钮状态(emergency_stop_state);
    void cancelListenDockEmergencyStopStateWrapper(int listen_id) {
        $self->cancelListenDockEmergencyStopState(listen_id);
    }


    // 监听机场: 静音模式(silent_mode);
    int listenDockSilentModeWrapper(DockSilentModeCallback* callback) {
        std::shared_ptr<DockSilentModeCallback> cb(callback);
        return $self->listenDockSilentMode([cb](const flight_hub_sdk::DockSilentMode& mode) {
            cb->onDockSilentMode(mode);
        });
    }


    // 取消监听机场: 静音模式(silent_mode);
    void cancelListenDockSilentModeWrapper(int listen_id) {
        $self->cancelListenDockSilentMode(listen_id);
    }


    // 监听机场: 用户体验改善计划(user_experience_improvement);
    int listenDockUserExperienceImprovementWrapper(DockUserExperienceImprovementCallback* callback) {
        std::shared_ptr<DockUserExperienceImprovementCallback> cb(callback);
        return $self->listenDockUserExperienceImprovement([cb](const flight_hub_sdk::DockUserExperienceImprovement& user_experience_improvement) {
            cb->onDockUserExperienceImprovement(user_experience_improvement);
        });
    }


    // 取消监听机场: 用户体验改善计划(user_experience_improvement);
    void cancelListenDockUserExperienceImprovementWrapper(int listen_id) {
        $self->cancelListenDockUserExperienceImprovement(listen_id);
    }


    // 监听机场: DRC 链路的状态(drc_state);
    int listenDockDrcStateWrapper(DockDrcStateCallback* callback) {
        std::shared_ptr<DockDrcStateCallback> cb(callback);
        return $self->listenDockDrcState([cb](const flight_hub_sdk::DockDrcState& state) {
            cb->onDockDrcState(state);
        });
    }


    // 取消监听机场: DRC 链路的状态(drc_state);
    void cancelListenDockDrcStateWrapper(int listen_id) {
        $self->cancelListenDockDrcState(listen_id);
    }


    // 监听机场: 推杆状态(putter_state);
    int listenDockPutterStateWrapper(DockPutterStateCallback* callback) {
        std::shared_ptr<DockPutterStateCallback> cb(callback);
        return $self->listenDockPutterState([cb](const flight_hub_sdk::DockPutterState& state) {
            cb->onDockPutterState(state);
        });
    }


    // 取消监听机场: 推杆状态(putter_state);
    void cancelListenDockPutterStateWrapper(int listen_id) {
        $self->cancelListenDockPutterState(listen_id);
    }


    // 监听机场: 相对备降点(relative_alternate_land_point);
    int listenDockRelativeAlternateLandPointWrapper(DockRelativeAlternateLandPointCallback* callback) {
        std::shared_ptr<DockRelativeAlternateLandPointCallback> cb(callback);
        return $self->listenDockRelativeAlternateLandPoint([cb](const flight_hub_sdk::DockRelativeAlternateLandPoint& land_point) {
            cb->onDockRelativeAlternateLandPoint(land_point);
        });
    }


    // 取消监听机场: 相对备降点(relative_alternate_land_point);
    void cancelListenDockRelativeAlternateLandPointWrapper(int listen_id) {
        $self->cancelListenDockRelativeAlternateLandPoint(listen_id);
    }


    // 监听机场: 自收敛坐标(self_converge_coordinate);
    int listenDockSelfConvergeCoordinateWrapper(DockSelfConvergeCoordinateCallback* callback) {
        std::shared_ptr<DockSelfConvergeCoordinateCallback> cb(callback);
        return $self->listenDockSelfConvergeCoordinate([cb](const flight_hub_sdk::DockSelfConvergeCoordinate& coordinate) {
            cb->onDockSelfConvergeCoordinate(coordinate);
        });
    }

    // 取消监听机场: 自收敛坐标(self_converge_coordinate);
    void cancelListenDockSelfConvergeCoordinateWrapper(int listen_id) {
        $self->cancelListenDockSelfConvergeCoordinate(listen_id);
    }

    // 机场固件信息;
    // DockFirmwareInfo
    int listenDockFirmwareInfoWrapper(DockFirmwareInfoCallback* callback) {
        std::shared_ptr<DockFirmwareInfoCallback> cb(callback);
        return $self->listenDockFirmwareInfo([cb](const flight_hub_sdk::DockFirmwareInfo& firmware_info) {
            cb->onDockFirmwareInfo(firmware_info);
        });
    }


    void cancelListenDockFirmwareInfoWrapper(int listen_id) {
        $self->cancelListenDockFirmwareInfo(listen_id);
    }


    // 监听机场飞机充电状态;
    int listenDockDroneChargeStateWrapper(DockDroneChargeStateCallback *callback) {
        std::shared_ptr<DockDroneChargeStateCallback> cb(callback);
        return $self->listenDockDroneChargeState([cb](const flight_hub_sdk::DockDroneChargeState& dock_drone_charge_state) {
            cb->onDockDroneChargeState(dock_drone_charge_state);
        });
    }

    // 取消监听机场飞机充电状态;
    void cancelListenDockDroneChargeStateWrapper(int listen_id) {
        $self->cancelListenDockDroneChargeState(listen_id);
    }

    // 监听机场气象信息;
    int listenDockMeteorologicalInfoWrapper(DockMeteorologicalInfoCallback *callback) {
        std::shared_ptr<DockMeteorologicalInfoCallback> cb(callback);
        return $self->listenDockMeteorologicalInfo([cb](const flight_hub_sdk::DockMeteorologicalInfo& info) {
            cb->onDockMeteorologicalInfo(info);
        });
    }

    // 取消监听机场气象信息;
    void cancelListenDockMeteorologicalInfoWrapper(int listen_id) {
        $self->cancelListenDockMeteorologicalInfo(listen_id);
    }


    // 监听机场备用电池信息;
    int listenDockBackupBatteryInfoWrapper(DockBackupBatteryInfoCallback *callback) {
        std::shared_ptr<DockBackupBatteryInfoCallback> cb(callback);
        return $self->listenDockBackupBatteryInfo([cb](const flight_hub_sdk::DockBackupBatteryInfo& info) {
            cb->onDockBackupBatteryInfo(info);
        });
    }

    // 取消 监听机场备用电池信息;
    void cancelListenDockBackupBatteryInfoWrapper(int listen_id) {
        $self->cancelListenDockBackupBatteryInfo(listen_id);
    }


    // 监听机场维护保养信息;
    int listenDockMaintainStatusInfoWrapper(DockMaintainStatusInfoCallback *callback) {
        std::shared_ptr<DockMaintainStatusInfoCallback> cb(callback);
        return $self->listenDockMaintainStatusInfo([cb](const flight_hub_sdk::DockMaintainStatusInfo& info) {
            cb->onDockMaintainStatusInfo(info);
        });
    }

    // 取消监听机场维护保养信息;
    void cancelListenDockMaintainStatusInfoWrapper(int listen_id) {
        $self->cancelListenDockMaintainStatusInfo(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDockDongleInfosWrapper(DockDongleInfosCallback* callback) {
        std::shared_ptr<DockDongleInfosCallback> cb(callback);
        return $self->listenDockDongleInfos([cb](const flight_hub_sdk::DockDongleInfos& msg) {
            cb->onDockDongleInfos(msg);
        });
    }

    void cancelListenDockDongleInfosWrapper(int listen_id) {
        $self->cancelListenDockDongleInfos(listen_id);
    }

    int listenDockAirConditionerModeWrapper(DockAirConditionerModeCallback* callback) {
        std::shared_ptr<DockAirConditionerModeCallback> cb(callback);
        return $self->listenDockAirConditionerMode([cb](const flight_hub_sdk::DockAirConditionerMode& msg) {
            cb->onDockAirConditionerMode(msg);
        });
    }

    void cancelListenDockAirConditionerModeWrapper(int listen_id) {
        $self->cancelListenDockAirConditionerMode(listen_id);
    }
}

%extend flight_hub_sdk::IDroneOsdModule {
    int listenAircraftBatteryInfoWrapper(AircraftBatteryInfoCallback* callback) {
        std::shared_ptr<AircraftBatteryInfoCallback> cb(callback);
        return $self->listenAircraftBatteryInfo([cb](const flight_hub_sdk::AircraftBatteryInfo& battery_info) {
            cb->onAircraftBatteryInfo(battery_info);
        });
    }

    int listenAircraftAttitudeWrapper(AircraftAttitudeCallback* callback) {
        std::shared_ptr<AircraftAttitudeCallback> cb(callback);
        return $self->listenAircraftAttitude([cb](const flight_hub_sdk::AircraftAttitude& attitude) {
            cb->onAircraftAttitude(attitude);
        });
    }

    int listenAircraftLocationWrapper(AircraftLocationCallback* callback) {
        std::shared_ptr<AircraftLocationCallback> cb(callback);
        return $self->listenAircraftLocation([cb](const flight_hub_sdk::AircraftLocation& location) {
            cb->onAircraftLocation(location);
        });
    }

    int listenAircraftModeCodeWrapper(AircraftModeCodeCallback* callback) {
        std::shared_ptr<AircraftModeCodeCallback> cb(callback);
        return $self->listenAircraftModeCode([cb](const flight_hub_sdk::AircraftModeCode& mode_code) {
            cb->onAircraftModeCode(mode_code);
        });
    }

    int listenAircraftWindSpeedWrapper(AircraftWindSpeedCallback* callback) {
        std::shared_ptr<AircraftWindSpeedCallback> cb(callback);
        return $self->listenAircraftWindSpeed([cb](const flight_hub_sdk::AircraftWindSpeed& wind_speed) {
            cb->onAircraftWindSpeed(wind_speed);
        });
    }

    int listenAircraftPayloadsOsdInfoWrapper(AircraftPayloadsOsdInfoListCallback* callback) {
        std::shared_ptr<AircraftPayloadsOsdInfoListCallback> cb(callback);
        return $self->listenAircraftPayloadsOsdInfo([cb](const flight_hub_sdk::AircraftPayloadsOsdInfoList& data) {
            cb->onAircraftPayloadsOsdInfoList(data);
        });
    }

    void cancelListenAircraftPayloadsOsdInfoWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsOsdInfo(listen_id);
    }


    // 飞机: 档位信息(gear);
    // AircraftGear;
    int listenAircraftGearWrapper(AircraftGearCallback* callback) {
        std::shared_ptr<AircraftGearCallback> cb(callback);
        return $self->listenAircraftGear([cb](const flight_hub_sdk::AircraftGear& data) {
            cb->onAircraftGear(data);
        });
    }

    void cancelListenAircraftGearWrapper(int listen_id) {
        $self->cancelListenAircraftGear(listen_id);
    }


    // 飞机: 档位信息(gear);
    // AircraftGear;
    int listenAircraftHomeDistanceWrapper(AircraftHomeDistanceCallback* callback) {
        std::shared_ptr<AircraftHomeDistanceCallback> cb(callback);
        return $self->listenAircraftHomeDistance([cb](const flight_hub_sdk::AircraftHomeDistance& home_distance) {
            cb->onAircraftHomeDistance(home_distance);
        });
    }

    void cancelListenAircraftHomeDistanceWrapper(int listen_id) {
        $self->cancelListenAircraftHomeDistance(listen_id);
    }


    // 飞机: 搜星状态(position_state);
    // AircraftPositionState;
    int listenAircraftPositionStateWrapper(AircraftPositionStateCallback* callback) {
        std::shared_ptr<AircraftPositionStateCallback> cb(callback);
        return $self->listenAircraftPositionState([cb](const flight_hub_sdk::AircraftPositionState& state) {
            cb->onAircraftPositionState(state);
        });
    }

    void cancelListenAircraftPositionStateWrapper(int listen_id) {
        $self->cancelListenAircraftPositionState(listen_id);
    }


    // 飞机: 当前控制源(control_source);
    // AircraftControlSource;
    int listenAircraftControlSourceWrapper(AircraftControlSourceCallback* callback) {
        std::shared_ptr<AircraftControlSourceCallback> cb(callback);
        return $self->listenAircraftControlSource([cb](const flight_hub_sdk::AircraftControlSource& control_source) {
            cb->onAircraftControlSource(control_source);
        });
    }

    void cancelListenAircraftControlSourceWrapper(int listen_id) {
        $self->cancelListenAircraftControlSource(listen_id);
    }


    // 飞机: 限制类信息状态;
    // AircraftLimitsInfo;
    int listenAircraftLimitsInfoWrapper(AircraftLimitsInfoCallback* callback) {
        std::shared_ptr<AircraftLimitsInfoCallback> cb(callback);
        return $self->listenAircraftLimitsInfo([cb](const flight_hub_sdk::AircraftLimitsInfo& info) {
            cb->onAircraftLimitsInfo(info);
        });
    }

    void cancelListenAircraftLimitsInfoWrapper(int listen_id) {
        $self->cancelListenAircraftLimitsInfo(listen_id);
    }


    // 电量告警信息(用户设置的阈值);
    // AircraftBatteryWarningThresholds;
    int listenAircraftBatteryWarningThresholdsWrapper(AircraftBatteryWarningThresholdsCallback* callback) {
        std::shared_ptr<AircraftBatteryWarningThresholdsCallback> cb(callback);
        return $self->listenAircraftBatteryWarningThresholds([cb](const flight_hub_sdk::AircraftBatteryWarningThresholds& threshold) {
            cb->onAircraftBatteryWarningThresholds(threshold);
        });
    }

    void cancelListenAircraftBatteryWarningThresholdsWrapper(int listen_id) {
        $self->cancelListenAircraftBatteryWarningThresholds(listen_id);
    }


    // 固件状态信息;
    // AircraftFirmwareStatus;
    int listenAircraftFirmwareStatusWrapper(AircraftFirmwareStatusCallback* callback) {
        std::shared_ptr<AircraftFirmwareStatusCallback> cb(callback);
        return $self->listenAircraftFirmwareStatus([cb](const flight_hub_sdk::AircraftFirmwareStatus& firmware_status) {
            cb->onAircraftFirmwareStatus(firmware_status);
        });
    }

    void cancelListenAircraftFirmwareStatusWrapper(int listen_id) {
        $self->cancelListenAircraftFirmwareStatus(listen_id);
    }


    // 累计飞行信息(total_flight_info);
    // AircraftTotalFlightInfo;
    int listenAircraftTotalFlightInfoWrapper(AircraftTotalFlightInfoCallback* callback) {
        std::shared_ptr<AircraftTotalFlightInfoCallback> cb(callback);
        return $self->listenAircraftTotalFlightInfo([cb](const flight_hub_sdk::AircraftTotalFlightInfo& total_flight_info) {
            cb->onAircraftTotalFlightInfo(total_flight_info);
        });
    }

    void cancelListenAircraftTotalFlightInfoWrapper(int listen_id) {
        $self->cancelListenAircraftTotalFlightInfo(listen_id);
    }


    // 飞行器激活时间(unix时间戳)(activation_time);
    // AircraftActivationTime;
    int listenAircraftActivationTimeWrapper(AircraftActivationTimeCallback* callback) {
        std::shared_ptr<AircraftActivationTimeCallback> cb(callback);
        return $self->listenAircraftActivationTime([cb](const flight_hub_sdk::AircraftActivationTime& activation_time) {
            cb->onAircraftActivationTime(activation_time);
        });
    }

    void cancelListenAircraftActivationTimeWrapper(int listen_id) {
        $self->cancelListenAircraftActivationTime(listen_id);
    }


    // 飞机：保养信息(maintain_status);
    // AircraftMaintainStatusInfo;
    int listenAircraftMaintainStatusInfoWrapper(AircraftMaintainStatusInfoCallback* callback) {
        std::shared_ptr<AircraftMaintainStatusInfoCallback> cb(callback);
        return $self->listenAircraftMaintainStatusInfo([cb](const flight_hub_sdk::AircraftMaintainStatusInfo& info) {
            cb->onAircraftMaintainStatusInfo(info);
        });
    }

    void cancelListenAircraftMaintainStatusInfoWrapper(int listen_id) {
        $self->cancelListenAircraftMaintainStatusInfo(listen_id);
    }

    // 飞行器夜航灯状态(night_lights_state);
    // AircraftNightLightsState;
    int listenAircraftNightLightsStateWrapper(AircraftNightLightsStateCallback* callback) {
        std::shared_ptr<AircraftNightLightsStateCallback> cb(callback);
        return $self->listenAircraftNightLightsState([cb](const flight_hub_sdk::AircraftNightLightsState& state) {
            cb->onAircraftNightLightsState(state);
        });
    }

    void cancelListenAircraftNightLightsStateWrapper(int listen_id) {
        $self->cancelListenAircraftNightLightsState(listen_id);
    }

    // 飞行器避障状态(obstacle_avoidance);
    // AircraftObstacleAvoidanceState;
    int listenAircraftObstacleAvoidanceStateWrapper(AircraftObstacleAvoidanceStateCallback* callback) {
        std::shared_ptr<AircraftObstacleAvoidanceStateCallback> cb(callback);
        return $self->listenAircraftObstacleAvoidanceState([cb](const flight_hub_sdk::AircraftObstacleAvoidanceState& state) {
            cb->onAircraftObstacleAvoidanceState(state);
        });
    }

    void cancelListenAircraftObstacleAvoidanceStateWrapper(int listen_id) {
        $self->cancelListenAircraftObstacleAvoidanceState(listen_id);
    }


    // 存储容量(storage);
    // AircraftStorageInfo;
    int listenAircraftStorageInfoWrapper(AircraftStorageInfoCallback* callback) {
        std::shared_ptr<AircraftStorageInfoCallback> cb(callback);
        return $self->listenAircraftStorageInfo([cb](const flight_hub_sdk::AircraftStorageInfo& info) {
            cb->onAircraftStorageInfo(info);
        });
    }

    void cancelListenAircraftStorageInfoWrapper(int listen_id) {
        $self->cancelListenAircraftStorageInfo(listen_id);
    }


    // 航迹id(track_id);
    // AircraftTrackId;
    int listenAircraftTrackIdWrapper(AircraftTrackIdCallback* callback) {
        std::shared_ptr<AircraftTrackIdCallback> cb(callback);
        return $self->listenAircraftTrackId([cb](const flight_hub_sdk::AircraftTrackId& track_id) {
            cb->onAircraftTrackId(track_id);
        });
    }

    void cancelListenAircraftTrackIdWrapper(int listen_id) {
        $self->cancelListenAircraftTrackId(listen_id);
    }


    // 合规相关信息(compliance information);
    // AircraftComplianceInfo;
    int listenAircraftComplianceInfoWrapper(AircraftComplianceInfoCallback* callback) {
        std::shared_ptr<AircraftComplianceInfoCallback> cb(callback);
        return $self->listenAircraftComplianceInfo([cb](const flight_hub_sdk::AircraftComplianceInfo& info) {
            cb->onAircraftComplianceInfo(info);
        });
    }

    void cancelListenAircraftComplianceInfoWrapper(int listen_id) {
        $self->cancelListenAircraftComplianceInfo(listen_id);
    }

    // 飞行器相机信息(cameras);
    // AircraftPayloadsCamerasList;
    int listenAircraftPayloadsCamerasListWrapper(AircraftPayloadsCamerasListCallback* callback) {
        std::shared_ptr<AircraftPayloadsCamerasListCallback> cb(callback);
        return $self->listenAircraftPayloadsCamerasList([cb](const flight_hub_sdk::AircraftPayloadsCamerasList& cameras) {
            cb->onAircraftPayloadsCamerasList(cameras);
        });
    }

    void cancelListenAircraftPayloadsCamerasListWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsCamerasList(listen_id);
    }

    // 遥控器失控动作(rc_lost_action);
    // AircraftRcLostAction;
    int listenAircraftRcLostActionWrapper(AircraftRcLostActionCallback* callback) {
        std::shared_ptr<AircraftRcLostActionCallback> cb(callback);
        return $self->listenAircraftRcLostAction([cb](const flight_hub_sdk::AircraftRcLostAction& action) {
            cb->onAircraftRcLostAction(action);
        });
    }

    void cancelListenAircraftRcLostActionWrapper(int listen_id) {
        $self->cancelListenAircraftRcLostAction(listen_id);
    }

    // 返航信息(返航模式, 返航高度);
    // AircraftRthInfo;
    int listenAircraftRthInfoWrapper(AircraftRthInfoCallback* callback) {
        std::shared_ptr<AircraftRthInfoCallback> cb(callback);
        return $self->listenAircraftRthInfo([cb](const flight_hub_sdk::AircraftRthInfo& info) {
            cb->onAircraftRthInfo(info);
        });
    }

    void cancelListenAircraftRthInfoWrapper(int listen_id) {
        $self->cancelListenAircraftRthInfo(listen_id);
    }


    // 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
    // AircraftHomeGeographicalInfo;
    int listenAircraftHomeGeographicalInfoWrapper(AircraftHomeGeographicalInfoCallback* callback) {
        std::shared_ptr<AircraftHomeGeographicalInfoCallback> cb(callback);
        return $self->listenAircraftHomeGeographicalInfo([cb](const flight_hub_sdk::AircraftHomeGeographicalInfo& info) {
            cb->onAircraftHomeGeographicalInfo(info);
        });
    }

    void cancelListenAircraftHomeGeographicalInfoWrapper(int listen_id) {
        $self->cancelListenAircraftHomeGeographicalInfo(listen_id);
    }

    // 返航高度模式当前值(current_rth_mode);
    // AircraftCurrentRthMode;
    int listenAircraftCurrentRthModeWrapper(AircraftCurrentRthModeCallback* callback) {
        std::shared_ptr<AircraftCurrentRthModeCallback> cb(callback);
        return $self->listenAircraftCurrentRthMode([cb](const flight_hub_sdk::AircraftCurrentRthMode& mode) {
            cb->onAircraftCurrentRthMode(mode);
        });
    }

    void cancelListenAircraftCurrentRthModeWrapper(int listen_id) {
        $self->cancelListenAircraftCurrentRthMode(listen_id);
    }

    // 飞行任务异常相关wrapper方法
    int listenFlightTasksExceptionWrapper(FlightTaskExceptionCallback* callback) {
        std::shared_ptr<FlightTaskExceptionCallback> cb(callback);
        return $self->listenFlightTasksException([cb](const flight_hub_sdk::FlightTaskException& exception) {
            cb->onFlightTaskException(exception);
        });
    }


    void cancelListenAircraftBatteryInfoWrapper(int listen_id) {
        $self->cancelListenAircraftBatteryInfo(listen_id);
    }

    void cancelListenAircraftAttitudeWrapper(int listen_id) {
        $self->cancelListenAircraftAttitude(listen_id);
    }

    void cancelListenAircraftLocationWrapper(int listen_id) {
        $self->cancelListenAircraftLocation(listen_id);
    }

    void cancelListenAircraftModeCodeWrapper(int listen_id) {
        $self->cancelListenAircraftModeCode(listen_id);
    }

    void cancelListenAircraftWindSpeedWrapper(int listen_id) {
        $self->cancelListenAircraftWindSpeed(listen_id);
    }

    void cancelListenFlightTasksExceptionWrapper(int listen_id) {
        $self->cancelListenFlightTasksException(listen_id);
    }

    // 新增的BusinessManager接口包装方法
    int listenAircraftControlCodeWrapper(AircraftControlCodeCallback* callback) {
        std::shared_ptr<AircraftControlCodeCallback> cb(callback);
        return $self->listenAircraftControlCode([cb](const flight_hub_sdk::AircraftControlCode& control_code) {
            cb->onAircraftControlCode(control_code);
        });
    }

    void cancelListenAircraftControlCodeWrapper(int listen_id) {
        $self->cancelListenAircraftControlCode(listen_id);
    }

    int listenAircraftSpeedWrapper(AircraftSpeedCallback* callback) {
        std::shared_ptr<AircraftSpeedCallback> cb(callback);
        return $self->listenAircraftSpeed([cb](const flight_hub_sdk::AircraftSpeed& speed) {
            cb->onAircraftSpeed(speed);
        });
    }

    void cancelListenAircraftSpeedWrapper(int listen_id) {
        $self->cancelListenAircraftSpeed(listen_id);
    }

    int listenAircraftPayloadsCameraLiveviewWorldRegionWrapper(AircraftPayloadsCameraLiveviewWorldRegionCallback* callback) {
        std::shared_ptr<AircraftPayloadsCameraLiveviewWorldRegionCallback> cb(callback);
        return $self->listenAircraftPayloadsCameraLiveviewWorldRegion([cb](const flight_hub_sdk::AircraftPayloadsCameraLiveviewWorldRegion& region) {
            cb->onAircraftPayloadsCameraLiveviewWorldRegion(region);
        });
    }

    void cancelListenAircraftPayloadsCameraLiveviewWorldRegionWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsCameraLiveviewWorldRegion(listen_id);
    }

    int listenAircraftPayloadsGimbalAttitudeWrapper(AircraftPayloadsGimbalAttitudeCallback* callback) {
        std::shared_ptr<AircraftPayloadsGimbalAttitudeCallback> cb(callback);
        return $self->listenAircraftPayloadsGimbalAttitude([cb](const flight_hub_sdk::AircraftPayloadsGimbalAttitude& attitude) {
            cb->onAircraftPayloadsGimbalAttitude(attitude);
        });
    }

    void cancelListenAircraftPayloadsGimbalAttitudeWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsGimbalAttitude(listen_id);
    }

    int listenAircraftPayloadsListWrapper(AircraftPayloadsListCallback* callback) {
        std::shared_ptr<AircraftPayloadsListCallback> cb(callback);
        return $self->listenAircraftPayloadsList([cb](const flight_hub_sdk::AircraftPayloadsList& payloads_list) {
            cb->onAircraftPayloadsList(payloads_list);
        });
    }

    void cancelListenAircraftPayloadsListWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsList(listen_id);
    }

    int listenControlRightsCloudControlStatusWrapper(ControlRightsCloudControlStatusCallback* callback) {
        std::shared_ptr<ControlRightsCloudControlStatusCallback> cb(callback);
        return $self->listenControlRightsCloudControlStatus([cb](const flight_hub_sdk::ControlRightsCloudControlStatus& status) {
            cb->onControlRightsCloudControlStatus(status);
        });
    }

    void cancelListenControlRightsCloudControlStatusWrapper(int listen_id) {
        $self->cancelListenControlRightsCloudControlStatus(listen_id);
    }

    int listenControlRightsDeviceControlStatusWrapper(ControlRightsDeviceControlStatusCallback* callback) {
        std::shared_ptr<ControlRightsDeviceControlStatusCallback> cb(callback);
        return $self->listenControlRightsDeviceControlStatus([cb](const flight_hub_sdk::ControlRightsDeviceControlStatus& status) {
            cb->onControlRightsDeviceControlStatus(status);
        });
    }

    void cancelListenControlRightsDeviceControlStatusWrapper(int listen_id) {
        $self->cancelListenControlRightsDeviceControlStatus(listen_id);
    }

    int listenFlightTasksMediaFileDisplayAvailableWrapper(FlightTasksMediaFileDisplayAvailableCallback* callback) {
        std::shared_ptr<FlightTasksMediaFileDisplayAvailableCallback> cb(callback);
        return $self->listenFlightTasksMediaFileDisplayAvailable([cb](const flight_hub_sdk::FlightTasksMediaFileDisplayAvailable& available) {
            cb->onFlightTasksMediaFileDisplayAvailable(available);
        });
    }

    void cancelListenFlightTasksMediaFileDisplayAvailableWrapper(int listen_id) {
        $self->cancelListenFlightTasksMediaFileDisplayAvailable(listen_id);
    }

    // 飞行器 HSI 信息（障碍物感知/避障传感器数据）
    int listenHsiInfoWrapper(HsiInfoCallback* callback) {
        std::shared_ptr<HsiInfoCallback> cb(callback);
        return $self->listenHsiInfo([cb](const flight_hub_sdk::HsiInfo& info) {
            cb->onHsiInfo(info);
        });
    }

    void cancelListenHsiInfoWrapper(int listen_id) {
        $self->cancelListenHsiInfo(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDroneModeCodeReasonWrapper(DroneModeCodeReasonCallback* callback) {
        std::shared_ptr<DroneModeCodeReasonCallback> cb(callback);
        return $self->listenDroneModeCodeReason([cb](const flight_hub_sdk::DroneModeCodeReason& msg) {
            cb->onDroneModeCodeReason(msg);
        });
    }

    void cancelListenDroneModeCodeReasonWrapper(int listen_id) {
        $self->cancelListenDroneModeCodeReason(listen_id);
    }

    int listenDroneDongleInfosWrapper(DroneDongleInfosCallback* callback) {
        std::shared_ptr<DroneDongleInfosCallback> cb(callback);
        return $self->listenDroneDongleInfos([cb](const flight_hub_sdk::DroneDongleInfos& msg) {
            cb->onDroneDongleInfos(msg);
        });
    }

    void cancelListenDroneDongleInfosWrapper(int listen_id) {
        $self->cancelListenDroneDongleInfos(listen_id);
    }

    int listenDroneFlysafeDatabaseVersionWrapper(DroneFlysafeDatabaseVersionCallback* callback) {
        std::shared_ptr<DroneFlysafeDatabaseVersionCallback> cb(callback);
        return $self->listenDroneFlysafeDatabaseVersion([cb](const flight_hub_sdk::DroneFlysafeDatabaseVersion& msg) {
            cb->onDroneFlysafeDatabaseVersion(msg);
        });
    }

    void cancelListenDroneFlysafeDatabaseVersionWrapper(int listen_id) {
        $self->cancelListenDroneFlysafeDatabaseVersion(listen_id);
    }

    int listenDroneWpmzVersionWrapper(DroneWpmzVersionCallback* callback) {
        std::shared_ptr<DroneWpmzVersionCallback> cb(callback);
        return $self->listenDroneWpmzVersion([cb](const flight_hub_sdk::DroneWpmzVersion& msg) {
            cb->onDroneWpmzVersion(msg);
        });
    }

    void cancelListenDroneWpmzVersionWrapper(int listen_id) {
        $self->cancelListenDroneWpmzVersion(listen_id);
    }

}

%extend flight_hub_sdk::IPsdkControlModule {
    int listenPsdkDeviceListWrapper(PsdkDeviceListCallback* callback) {
        std::shared_ptr<PsdkDeviceListCallback> cb(callback);
        return $self->listenDrcPsdkDeviceList([cb](const flight_hub_sdk::PsdkDeviceList& psdk_device_list) {
            cb->onPsdkDeviceList(psdk_device_list);
        });
    }

    void cancelListenPsdkDeviceListWrapper(int listen_id) {
        $self->cancelListenDrcPsdkDeviceList(listen_id);
    }

    int listenPsdkStateSummaryInfoWrapper(int psdk_index, PsdkStateSummaryInfoCallback* callback) {
        std::shared_ptr<PsdkStateSummaryInfoCallback> cb(callback);
        return $self->listenDrcPsdkStateSummaryInfo(psdk_index, [cb](flight_hub_sdk::PsdkStateSummaryInfo psdk_state_summary_info) {
            cb->onPsdkStateSummaryInfo(psdk_state_summary_info);
        });
    }

    void cancelListenPsdkStateSummaryInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkStateSummaryInfo(psdk_index, listen_id);
    }

    int listenPsdkStateSpeakerInfoWrapper(int psdk_index, PsdkStateSpeakerInfoCallback* callback) {
        std::shared_ptr<PsdkStateSpeakerInfoCallback> cb(callback);
        return $self->listenDrcPsdkStateSpeakerInfo(psdk_index, [cb](flight_hub_sdk::PsdkStateSpeakerInfo psdk_state_speaker_info) {
            cb->onPsdkStateSpeakerInfo(psdk_state_speaker_info);
        });
    }

    void cancelListenPsdkStateSpeakerInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkStateSpeakerInfo(psdk_index, listen_id);
    }

    int listenPsdkStateSearchlightInfoWrapper(int psdk_index, PsdkStateSearchlightInfoCallback* callback) {
        std::shared_ptr<PsdkStateSearchlightInfoCallback> cb(callback);
        return $self->listenDrcPsdkStateSearchlightInfo(psdk_index, [cb](flight_hub_sdk::PsdkStateSearchlightInfo psdk_state_searchlight_info) {
            cb->onPsdkStateSearchlightInfo(psdk_state_searchlight_info);
        });
    }

    void cancelListenPsdkStateSearchlightInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkStateSearchlightInfo(psdk_index, listen_id);
    }

    int listenPsdkSpeakerPlayProgressInfoWrapper(int psdk_index, PsdkSpeakerPlayProgressInfoCallback* callback) {
        std::shared_ptr<PsdkSpeakerPlayProgressInfoCallback> cb(callback);
        return $self->listenDrcPsdkSpeakerPlayProgressInfo(psdk_index, [cb](flight_hub_sdk::PsdkSpeakerPlayProgressInfo psdk_speaker_play_progress_info) {
            cb->onPsdkSpeakerPlayProgressInfo(psdk_speaker_play_progress_info);
        });
    }

    void cancelListenPsdkSpeakerPlayProgressInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkSpeakerPlayProgressInfo(psdk_index, listen_id);
    }

    int listenPsdkFloatingWindowTextInfoWrapper(int psdk_index, PsdkFloatingWindowTextInfoCallback* callback) {
        std::shared_ptr<PsdkFloatingWindowTextInfoCallback> cb(callback);
        return $self->listenDrcPsdkFloatingWindowTextInfo(psdk_index, [cb](flight_hub_sdk::PsdkFloatingWindowTextInfo psdk_floating_window_text_info) {
            cb->onPsdkFloatingWindowTextInfo(psdk_floating_window_text_info);
        });
    }

    void cancelListenPsdkFloatingWindowTextInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkFloatingWindowTextInfo(psdk_index, listen_id);
    }

    void speakerAudioPlayStartWrapper(int psdk_index, const flight_hub_sdk::PsdkSpeakerAudioPlayInfo& audio_play_info, ResultCallbackWithError* callback) {
        $self->DrcSpeakerAudioPlayStart(psdk_index, audio_play_info, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerTTSPlayStartWrapper(int psdk_index, const flight_hub_sdk::PsdkSpeakerTTSPlayInfo& tts_play_info, ResultCallbackWithError* callback) {
        $self->DrcSpeakerTTSPlayStart(psdk_index, tts_play_info, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerReplayWrapper(int psdk_index, ResultCallbackWithError* callback) {
        $self->DrcSpeakerReplay(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerPlayStopWrapper(int psdk_index, ResultCallbackWithError* callback) {
        $self->DrcSpeakerPlayStop(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerRealTimeStartWrapper(int psdk_index, ResultCallbackWithError* callback) {
        $self->DrcSpeakerRealTimeStart(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerRealTimeStopWrapper(int psdk_index, ResultCallbackWithError* callback) {
        $self->DrcSpeakerRealTimeStop(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerPlayVolumeSetWrapper(int psdk_index, int volume, ResultCallbackWithError* callback) {
        $self->DrcSpeakerPlayVolumeSet(psdk_index, volume, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerPlayModeSetWrapper(int psdk_index, int play_mode, ResultCallbackWithError* callback) {
        $self->DrcSpeakerPlayModeSet(psdk_index, play_mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void speakerTTSSetWrapper(int psdk_index, flight_hub_sdk::PsdkSpeakerTTSconfig tts_config, ResultCallbackWithError* callback) {
        $self->DrcSpeakerTTSSet(psdk_index, tts_config, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void widgetValueSetWrapper(int psdk_index, int index, int value, ResultCallbackWithError* callback) {
        $self->DrcWidgetValueSet(psdk_index, index, value, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void widgetInputBoxTextSetWrapper(int psdk_index, const std::string& value, ResultCallbackWithError* callback) {
        $self->DrcWidgetInputBoxTextSet(psdk_index, value, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightModeSetWrapper(int psdk_index, int mode, ResultCallbackWithError* callback) {
        $self->DrcSearchlightModeSet(psdk_index, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightBrightnessSetWrapper(int psdk_index, int brightness, ResultCallbackWithError* callback) {
        $self->DrcSearchlightBrightnessSet(psdk_index, brightness, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightCalibrationWrapper(int psdk_index, ResultCallbackWithError* callback) {
        $self->DrcSearchlightCalibration(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightFineTuningSetWrapper(int psdk_index, int position, double value, bool saved, ResultCallbackWithError* callback) {
        $self->DrcSearchlightFineTuningSet(psdk_index, position, value, saved, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightWideFieldModeSetWrapper(int psdk_index, int wide_field_mode, ResultCallbackWithError* callback) {
        $self->DrcSearchlightWideFieldModeSet(psdk_index, wide_field_mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightLightGimbalControlSetWrapper(int psdk_index, int light_gimbal_control, ResultCallbackWithError* callback) {
        $self->DrcSearchlightLightGimbalControlSet(psdk_index, light_gimbal_control, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    void searchlightTypeSetWrapper(int psdk_index, int type, ResultCallbackWithError* callback) {
        $self->DrcSearchlightTypeSet(psdk_index, type, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                delete callback;
            }
        });
    }

    int listenPsdkSpeakerAudioPlayStartProgressWrapper(PsdkSpeakerAudioPlayStartProgressCallback* callback) {
        std::shared_ptr<PsdkSpeakerAudioPlayStartProgressCallback> cb(callback);
        return $self->listenPsdkSpeakerAudioPlayStartProgress([cb](const flight_hub_sdk::PsdkSpeakerPlayStartProgress& msg) {
            cb->onPsdkSpeakerAudioPlayStartProgress(msg);
        });
    }
    void cancelListenPsdkSpeakerAudioPlayStartProgressWrapper(int listen_id) {
        $self->cancelListenPsdkSpeakerAudioPlayStartProgress(listen_id);
    }
    int listenPsdkSpeakerTtsPlayStartProgressWrapper(PsdkSpeakerTtsPlayStartProgressCallback* callback) {
        std::shared_ptr<PsdkSpeakerTtsPlayStartProgressCallback> cb(callback);
        return $self->listenPsdkSpeakerTtsPlayStartProgress([cb](const flight_hub_sdk::PsdkSpeakerPlayStartProgress& msg) {
            cb->onPsdkSpeakerTtsPlayStartProgress(msg);
        });
    }
    void cancelListenPsdkSpeakerTtsPlayStartProgressWrapper(int listen_id) {
        $self->cancelListenPsdkSpeakerTtsPlayStartProgress(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDronePsdkUiResourceWrapper(DronePsdkUiResourceCallback* callback) {
        std::shared_ptr<DronePsdkUiResourceCallback> cb(callback);
        return $self->listenDronePsdkUiResource([cb](const flight_hub_sdk::DronePsdkUiResource& msg) {
            cb->onDronePsdkUiResource(msg);
        });
    }

    void cancelListenDronePsdkUiResourceWrapper(int listen_id) {
        $self->cancelListenDronePsdkUiResource(listen_id);
    }

}

%extend flight_hub_sdk::IOnboardAiModule {
    int listenAIDataInfoWrapper(AiDataInfoCallback* callback) {
        std::shared_ptr<AiDataInfoCallback> cb(callback);
        return $self->listenAIDataInfo([cb](const flight_hub_sdk::AiDataInfo& info) {
            cb->onAiDataInfo(info);
        });
    }
    void cancelListenAIDataInfoWrapper(int listen_id) {
        $self->cancelListenAIDataInfo(listen_id);
    }
    void AIIdentifySetWrapper(const flight_hub_sdk::AiIdentifySetParam& param, ResultCallback* callback) {
        $self->AIIdentifySet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void AIIdentifyFilterSetWrapper(const flight_hub_sdk::AiIdentifyFilterSetParam& param, ResultCallback* callback) {
        $self->AIIdentifyFilterSet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void AIIdentifyScoreSetWrapper(const flight_hub_sdk::AiIdentifyScoreSetParam& param, ResultCallback* callback) {
        $self->AIIdentifyScoreSet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void AIIdentifyScoreModeSetWrapper(const flight_hub_sdk::AiIdentifyScoreModeSetParam& param, ResultCallback* callback) {
        $self->AIIdentifyScoreModeSet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void AIIdentifyScoreResetWrapper(const flight_hub_sdk::AiIdentifyScoreResetParam& param, ResultCallback* callback) {
        $self->AIIdentifyScoreReset(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }
    void AIModelSelectWrapper(const flight_hub_sdk::AiModelSelectParam& param, ResultCallback* callback) {
        $self->AIModelSelect(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    // ══════════════════════════════════════════════
    // AI Spotlight Zoom (Tracking) Wrappers
    // ══════════════════════════════════════════════

    void DrcAISpotlightZoomSetWrapper(int on, ResultCallbackWithError* callback) {
        $self->DrcAISpotlightZoomSet(on, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void DrcAISpotlightZoomSelectWrapper(const flight_hub_sdk::AiSpotlightZoomSelectParams& params, ResultCallbackWithError* callback) {
        $self->DrcAISpotlightZoomSelect(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void DrcAISpotlightZoomConfirmWrapper(ResultCallbackWithError* callback) {
        $self->DrcAISpotlightZoomConfirm([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void DrcAISpotlightZoomStopWrapper(ResultCallbackWithError* callback) {
        $self->DrcAISpotlightZoomStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void DrcAISpotlightZoomTrackWrapper(int target_index, ResultCallbackWithError* callback) {
        $self->DrcAISpotlightZoomTrack(target_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    // ══════════════════════════════════════════════
    // AI Alarm Wrappers
    // ══════════════════════════════════════════════

    void DrcAIAlarmStartWrapper(const flight_hub_sdk::AiAlarmStartParams& params, ResultCallbackWithError* callback) {
        $self->DrcAIAlarmStart(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void DrcAIAlarmStopWrapper(ResultCallbackWithError* callback) {
        $self->DrcAIAlarmStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    void DrcAIModelsConfigWrapper(const flight_hub_sdk::AiModelsConfigParams& params, ResultCallbackWithError* callback) {
        $self->DrcAIModelsConfig(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); delete callback; }
        });
    }

    int listenDrcAIAlarmStateWrapper(AiAlarmStateCallback* callback) {
        std::shared_ptr<AiAlarmStateCallback> cb(callback);
        return $self->listenDrcAIAlarmState([cb](const flight_hub_sdk::AiAlarmStateInfo& info) {
            cb->onAiAlarmState(info);
        });
    }

    void cancelListenDrcAIAlarmStateWrapper(int listen_id) {
        $self->cancelListenDrcAIAlarmState(listen_id);
    }
}

%extend flight_hub_sdk::INetworkTestModule {
    void startNetworkTestWrapper(StartNetworkTestCallback* callback) {
        $self->startNetworkTest([callback](flight_hub_sdk::FlightHubError err, NetworkTestProgressResult result) {
            if (callback) {
                callback->onStartNetworkTest(err, result);
                delete callback;
            }
        });
    }

    void stopNetworkTestWrapper(StopNetworkTestCallback* callback) {
        $self->stopNetworkTest([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onStopNetworkTest(err);
                delete callback;
            }
        });
    }

    void getNetworkTestProgressWrapper(GetNetworkTestProgressCallback* callback) {
        $self->getNetworkTestProgress([callback](flight_hub_sdk::FlightHubError err, NetworkTestProgressResult result) {
            if (callback) {
                callback->onGetNetworkTestProgress(err, result);
                delete callback;
            }
        });
    }

    int subscribeNetworkTestProgressWrapper(SubscribeNetworkTestProgressCallback* callback) {
        std::shared_ptr<SubscribeNetworkTestProgressCallback> cb(callback);
        return $self->subscribeNetworkTestProgress([cb](flight_hub_sdk::NetworkTestProgressResult result) {
            cb->onSubscribeNetworkTestProgress(result);
        });
    }

    void unsubscribeNetworkTestProgressWrapper(int listen_id) {
        $self->unsubscribeNetworkTestProgress(listen_id);
    }
}

%extend flight_hub_sdk::IHMSModule {
    void QueryHistoryHMSDataWrapper(const flight_hub_sdk::HistoryHmsDataParam& params, QueryHistoryHMSDataCallback* callback) {
        $self->QueryHistoryHMSData(params, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::HistoryHmsDataResult result) {
            if (callback) {
                callback->onQueryHistoryHMSData(err, result);
                delete callback;
            }
        });
    }
}

%extend flight_hub_sdk::ILogManagementModule {
    void QueryDeviceLogListWrapper(const std::string& device_sn, const std::vector<int>& module_list, QueryDeviceLogListCallback* callback) {
        $self->QueryDeviceLogList(device_sn, module_list, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::DeviceLogListResult result) {
            if (callback) {
                callback->onQueryDeviceLogList(err, result);
                delete callback;
            }
        });
    }

    void CreateDeviceLogReportWrapper(const flight_hub_sdk::CreateDeviceLogReportRequestParam& param, CreateDeviceLogReportCallback* callback) {
        $self->CreateDeviceLogReport(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CreateDeviceLogReportResult result) {
            if (callback) {
                callback->onCreateDeviceLogReport(err, result);
                delete callback;
            }
        });
    }

    void LogReportListWrapper(const std::string& device_sn, int page, int page_size, LogReportListCallback* callback) {
        $self->LogReportList(device_sn, page, page_size, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LogReportListResult result) {
            if (callback) {
                callback->onLogReportList(err, result);
                delete callback;
            }
        });
    }

    void LogReportOperationWrapper(const std::string& operation, const std::string& report_id, LogReportOperationCallback* callback) {
        $self->LogReportOperation(operation, report_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onLogReportOperation(err);
                delete callback;
            }
        });
    }

    void UploadLogAppendFileWrapper(const std::string& file_path, UploadLogAppendFileCallback* callback) {
        $self->UploadLogAppendFile(file_path, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onUploadLogAppendFile(err);
                delete callback;
            }
        });
    }

    int listenDeviceFileUploadProgressWrapper(const std::string& device_sn, DeviceFileUploadProgressCallback* callback) {
        std::shared_ptr<DeviceFileUploadProgressCallback> cb(callback);
        return $self->listenDeviceFileUploadProgress(device_sn, [cb](const flight_hub_sdk::DeviceFileUploadProgress& msg) {
            cb->onDeviceFileUploadProgress(msg);
        });
    }
    void cancelListenDeviceFileUploadProgressWrapper(int listen_id) {
        $self->cancelListenDeviceFileUploadProgress(listen_id);
    }
}

%extend flight_hub_sdk::IMediaServiceModule {
    // [媒体服务] 获取媒体文件列表;
    void getMediaFilesListWrapper(const flight_hub_sdk::GetMediaFilesListParams& request_params, GetMediaFilesListCallback* callback) {
        $self->getMediaFilesList(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesListResults& results) {
            if (callback) {
                callback->onGetMediaFilesList(err, results);
                // delete callback;
            }
        });
    }

    // [媒体服务] 批量删除媒体文件;
    void deleteMediaFilesWrapper(const std::vector<long long>& file_ids, ResultCallbackWithError* callback) {
        $self->deleteMediaFiles(file_ids, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                // delete callback;
            }
        });
    }

    // [媒体服务] 获取媒体文件下载地址;
    void getMediaFilesDownloadURLWrapper(long long file_id, GetMediaFilesDownloadUrlCallback* callback) {
        $self->getMediaFilesDownloadURL(file_id, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesDownloadUrlResults& results) {
            if (callback) {
                callback->onGetMediaFilesDownloadUrl(err, results);
                // delete callback;
            }
        });
    }

    // [媒体服务] 获取媒体文件批量下载地址 (通过 object_keys);
    void getMediaFilesDownloadURLListWrapper(const std::vector<std::string>& object_keys, GetMediaFilesDownloadUrlListCallback* callback) {
        $self->getMediaFilesDownloadURL(object_keys, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesDownloadUrlListResults& results) {
            if (callback) {
                callback->onGetMediaFilesDownloadUrlList(err, results);
                // delete callback;
            }
        });
    }

    // 监听: 设备媒体后处理完成通知;
    int listenMediaFilePostProcessMessageWrapper(const std::string& device_sn, MediaFilePostProcessCallback* callback) {
        std::shared_ptr<MediaFilePostProcessCallback> cb(callback);
        return $self->listenMediaFilePostProcessMessage(device_sn, [cb](const flight_hub_sdk::MediaFilePostProcess& msg) {
            cb->onMediaFilePostProcess(msg);
        });
    }

    // 取消监听: 设备媒体后处理完成通知;
    void cancelListenMediaFilePostProcessMessageWrapper(int listen_id) {
        $self->cancelListenMediaFilePostProcessMessage(listen_id);
    }

    // 监听: 设备媒体回传通知;
    int listenMediaFileDeviceUploadMessageWrapper(const std::string& device_sn, MediaFileDeviceUploadCallback* callback) {
        std::shared_ptr<MediaFileDeviceUploadCallback> cb(callback);
        return $self->listenMediaFileDeviceUploadMessage(device_sn, [cb](const flight_hub_sdk::MediaFileDeviceUpload& msg) {
            cb->onMediaFileDeviceUpload(msg);
        });
    }

    // 取消监听: 设备媒体回传通知;
    void cancelListenMediaFileDeviceUploadMessageWrapper(int listen_id) {
        $self->cancelListenMediaFileDeviceUploadMessage(listen_id);
    }

    // [媒体上传] 获取 OSS STS 临时上传凭证
    void getMediaUploadStsWrapper(const flight_hub_sdk::GetMediaUploadStsParams& params,
                                  GetMediaUploadStsCallback* callback) {
        $self->getMediaUploadSts(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetMediaUploadStsResult& result) {
                if (callback) { callback->onGetMediaUploadSts(err, result); delete callback; }
            });
    }

    // [媒体上传] 文件上传完成后通知服务端
    void mediaUploadCallbackWrapper(const flight_hub_sdk::MediaUploadCallbackParams& params,
                                    MediaUploadCallbackCallback* callback) {
        $self->mediaUploadCallback(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::MediaUploadCallbackResult& result) {
                if (callback) { callback->onMediaUploadCallback(err, result); delete callback; }
            });
    }

    void uploadGeoMediaFileWrapper(const flight_hub_sdk::UploadGeoMediaFileParams& params,
                                   UploadGeoMediaFileCallback* callback) {
        $self->uploadGeoMediaFile(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::UploadGeoMediaFileResult& result) {
                if (callback) { callback->onUploadGeoMediaFile(err, result); delete callback; }
            });
    }

    void getMediaFileDetailByIdWrapper(int file_id,
                                       GetMediaFileDetailByIdCallback* callback) {
        $self->getMediaFileDetailById(file_id,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetMediaFileDetailByIdResult& result) {
                if (callback) { callback->onGetMediaFileDetailById(err, result); delete callback; }
            });
    }
}

// ============================================================
// ITerraBuildModule — SWIG %ignore + %extend 胶水层
// ============================================================
%ignore flight_hub_sdk::ITerraBuildModule::getReconstructionSettings;
%ignore flight_hub_sdk::ITerraBuildModule::getQualityReport;
%ignore flight_hub_sdk::ITerraBuildModule::getModelFileList;
%ignore flight_hub_sdk::ITerraBuildModule::getModelFileUrl;
%ignore flight_hub_sdk::ITerraBuildModule::createReconstruction;
%ignore flight_hub_sdk::ITerraBuildModule::retryReconstruction;
%ignore flight_hub_sdk::ITerraBuildModule::stopReconstruction;
%ignore flight_hub_sdk::ITerraBuildModule::getSfmResult;
%ignore flight_hub_sdk::ITerraBuildModule::deleteModelFile;
%ignore flight_hub_sdk::ITerraBuildModule::batchDeleteModelFiles;
%ignore flight_hub_sdk::ITerraBuildModule::createPanorama;
%ignore flight_hub_sdk::ITerraBuildModule::getReconstructionImages;
%ignore flight_hub_sdk::ITerraBuildModule::listenReconstructionStatusChanged;
%ignore flight_hub_sdk::ITerraBuildModule::cancelListenReconstructionStatusChanged;
%ignore flight_hub_sdk::ITerraBuildModule::uploadModelCallback;
%ignore flight_hub_sdk::ITerraBuildModule::renameModel;
%ignore flight_hub_sdk::ITerraBuildModule::showModelOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::listenTerraUnzipProgress;
%ignore flight_hub_sdk::ITerraBuildModule::cancelListenTerraUnzipProgress;
%ignore flight_hub_sdk::ITerraBuildModule::listenTerraModelStatusChanged;
%ignore flight_hub_sdk::ITerraBuildModule::cancelListenTerraModelStatusChanged;
%ignore flight_hub_sdk::ITerraBuildModule::listenTerraFileShowOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::cancelListenTerraFileShowOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::listenTerraMediaFileHideOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::cancelListenTerraMediaFileHideOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::listenTerraMediaFileShowOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::cancelListenTerraMediaFileShowOnMap;
%ignore flight_hub_sdk::ITerraBuildModule::getModelDetail;
%ignore flight_hub_sdk::ITerraBuildModule::setLasWkt;
// optional 字段结构体 SWIG 无法直接处理，通过 JSON 字符串透传
%ignore flight_hub_sdk::TerraModelDetailResult;
%ignore flight_hub_sdk::TerraModelBoundaryInfo;
%ignore flight_hub_sdk::TerraModelIntensityInfo;
%ignore flight_hub_sdk::TerraModelExtInfo;
%ignore flight_hub_sdk::TerraSetLasWktParams;
%ignore flight_hub_sdk::CreateReconstructionParams;

// IElementModule — 所有含 std::function/std::optional 的方法通过 wrapper 暴露
%ignore flight_hub_sdk::IElementModule::createElement;
%ignore flight_hub_sdk::IElementModule::updateElement;
%ignore flight_hub_sdk::IElementModule::listElementHierarchy;
%ignore flight_hub_sdk::IElementModule::setElementVisibility;
%ignore flight_hub_sdk::IElementModule::deleteElement;
%ignore flight_hub_sdk::IElementModule::listenElementCreate;
%ignore flight_hub_sdk::IElementModule::cancelListenElementCreate;
%ignore flight_hub_sdk::IElementModule::listenElementUpdate;
%ignore flight_hub_sdk::IElementModule::cancelListenElementUpdate;
%ignore flight_hub_sdk::IElementModule::listenElementDelete;
%ignore flight_hub_sdk::IElementModule::cancelListenElementDelete;
// optional 字段的结构体 SWIG 无法直接处理（CreateElementParams 无 optional，不需要 ignore）
%ignore flight_hub_sdk::UpdateElementParams;
%ignore flight_hub_sdk::ListElementHierarchyParams;
%ignore flight_hub_sdk::ReconParameter2D;
%ignore flight_hub_sdk::ReconParameter3D;
%ignore flight_hub_sdk::ReconParameter3DGS;
%ignore flight_hub_sdk::ReconParameterLidar;
%ignore flight_hub_sdk::ReconGeoDesc;
%ignore flight_hub_sdk::ReconQualityReportSetting;

%extend flight_hub_sdk::ITerraBuildModule {

    // [Terra Build] 获取重建设置
    void getReconstructionSettingsWrapper(const std::string& file_uuid,
                                          TerraBuildReconstructionSettingsCallback* callback) {
        $self->getReconstructionSettings(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildSettingsResult& result) {
                if (callback) { callback->onGetReconstructionSettings(err, result); delete callback; }
            });
    }

    // [Terra Build] 获取质量报告
    void getQualityReportWrapper(const std::string& file_uuid,
                                 const std::string& language,
                                 const std::string& measure_unit_system,
                                 TerraBuildQualityReportCallback* callback) {
        $self->getQualityReport(file_uuid, language, measure_unit_system,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildQualityReportResult& result) {
                if (callback) { callback->onGetQualityReport(err, result); delete callback; }
            });
    }

    // [Terra Build] 获取模型文件列表
    void getModelFileListWrapper(int page, int page_size,
                                 TerraBuildModelFileListCallback* callback) {
        $self->getModelFileList(page, page_size,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildModelFileListResult& result) {
                if (callback) { callback->onGetModelFileList(err, result); delete callback; }
            });
    }

    // [Terra Build] 获取模型文件签名 URL
    void getModelFileUrlWrapper(int file_id, int type,
                                TerraBuildModelFileUrlCallback* callback) {
        $self->getModelFileUrl(file_id, type,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildModelFileUrlResult& result) {
                if (callback) { callback->onGetModelFileUrl(err, result); delete callback; }
            });
    }
    // [Terra Build] 重试重建任务
    void retryReconstructionWrapper(const std::string& file_uuid, ResultCallback* callback) {
        $self->retryReconstruction(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); delete callback; }
            });
    }

    // [Terra Build] 停止重建任务
    void stopReconstructionWrapper(const std::string& file_uuid, ResultCallback* callback) {
        $self->stopReconstruction(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); delete callback; }
            });
    }

    // [Terra Build] 获取空三结果
    void getSfmResultWrapper(const std::string& file_uuid,
                             TerraBuildSfmResultCallback* callback) {
        $self->getSfmResult(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildSfmResult& result) {
                if (callback) { callback->onGetSfmResult(err, result); delete callback; }
            });
    }

    // [Terra Build] 创建重建任务（Java 参数对象方式，无需构建 JSON）
    void createReconstructionWithParamsWrapper(const terra_build_swig::CreateReconstructionJavaParams& jp,
                                               TerraBuildCreateReconstructionCallback* callback) {
        flight_hub_sdk::CreateReconstructionParams params;
        // 必填
        params.reconstruction_type = jp.reconstruction_type;
        params.name = jp.name;
        params.folder_uuids = jp.folder_uuids;
        params.folder_ids = jp.folder_ids;
        // 可选基础字段
        if (jp.simplified_factor_set) params.simplified_factor = jp.simplified_factor;
        if (!jp.wkt.empty()) params.wkt = jp.wkt;
        if (jp.quality_level_set) params.quality_level = jp.quality_level;
        if (jp.quality_level_3dgs_set) params.quality_level_3dgs = jp.quality_level_3dgs;
        if (jp.is_shown_on_map_set) params.is_shown_on_map = jp.is_shown_on_map;
        if (jp.optimize_SfM_set) params.optimize_SfM = jp.optimize_SfM;
        if (jp.custom_storage_parent_id_set) params.custom_storage_parent_id = jp.custom_storage_parent_id;
        if (!jp.generate_model_formats.empty()) params.generate_model_formats = jp.generate_model_formats;
        // predefine_area
        if (jp.use_predefine_area && !jp.predefine_area.polygon_points.empty()) {
            params.predefine_area = jp.predefine_area;
        }
        if (!jp.quality_report_language.empty()) {
            flight_hub_sdk::ReconQualityReportSetting qrs;
            qrs.language = jp.quality_report_language;
            qrs.measure_unit_system = jp.quality_report_measure_unit_system.empty()
                ? "metric" : jp.quality_report_measure_unit_system;
            params.quality_report_setting = qrs;
        }

        // GeoDesc 辅助 lambda
        auto toGeo = [](const terra_build_swig::GeoDescParams& g) -> flight_hub_sdk::ReconGeoDesc {
            flight_hub_sdk::ReconGeoDesc r;
            r.cs_type = g.cs_type; r.geo_cs = g.geo_cs;
            r.geo_cs_wkt = g.geo_cs_wkt; r.geoid_file_name = g.geoid_file_name;
            r.offset = g.offset; r.override_vertical_cs = g.override_vertical_cs;
            return r;
        };

        // Parameter2D
        if (jp.use_parameter_2d) {
            const auto& s = jp.parameter_2d;
            flight_hub_sdk::ReconParameter2D p;
            if (!s.SfM_method.empty()) p.SfM_method = s.SfM_method;
            if (s.sfm_feature_quantity_level_set) p.sfm_feature_quantity_level = s.sfm_feature_quantity_level;
            if (s.use_image_position_set) p.use_image_position = s.use_image_position;
            if (s.generate_blocks_exchangge_xml_set) p.generate_blocks_exchangge_xml = s.generate_blocks_exchangge_xml;
            if (!s.blocks_exchange_xml_geo_desc.cs_type.empty()) p.blocks_exchange_xml_geo_desc = toGeo(s.blocks_exchange_xml_geo_desc);
            if (!s.output_geo_desc.cs_type.empty()) p.output_geo_desc = toGeo(s.output_geo_desc);
            if (s.usable_memory_GB_set) p.usable_memory_GB = s.usable_memory_GB;
            if (s.map_mode_set) p.map_mode = s.map_mode;
            if (s.quality_level_set) p.quality_level = s.quality_level;
            if (s.resolution_type_2d_set) p.resolution_type_2d = s.resolution_type_2d;
            if (s.dom_tile_size_set) p.dom_tile_size = s.dom_tile_size;
            if (s.resolution_gsd_2d_set) p.resolution_gsd_2d = s.resolution_gsd_2d;
            if (s.use_Wallis_set) p.use_Wallis = s.use_Wallis;
            if (s.use_Dehaze_set) p.use_Dehaze = s.use_Dehaze;
            if (s.use_gcj02_set) p.use_gcj02 = s.use_gcj02;
            if (s.generate_maps_2d_tiff_set) p.generate_maps_2d_tiff = s.generate_maps_2d_tiff;
            if (s.generate_maps_2d_tiff_cog_set) p.generate_maps_2d_tiff_cog = s.generate_maps_2d_tiff_cog;
            if (s.use_smart_fill_hole_set) p.use_smart_fill_hole = s.use_smart_fill_hole;
            params.parameter_2d = p;
        }

        // Parameter3D
        if (jp.use_parameter_3d) {
            const auto& s = jp.parameter_3d;
            flight_hub_sdk::ReconParameter3D p;
            if (!s.SfM_method.empty()) p.SfM_method = s.SfM_method;
            if (s.sfm_feature_quantity_level_set) p.sfm_feature_quantity_level = s.sfm_feature_quantity_level;
            if (s.use_image_position_set) p.use_image_position = s.use_image_position;
            if (s.generate_blocks_exchangge_xml_set) p.generate_blocks_exchangge_xml = s.generate_blocks_exchangge_xml;
            if (!s.blocks_exchange_xml_geo_desc.cs_type.empty()) p.blocks_exchange_xml_geo_desc = toGeo(s.blocks_exchange_xml_geo_desc);
            if (!s.output_geo_desc.cs_type.empty()) p.output_geo_desc = toGeo(s.output_geo_desc);
            if (s.output_pointcloud_set) p.output_pointcloud = s.output_pointcloud;
            if (s.density_percent_level_set) p.density_percent_level = s.density_percent_level;
            if (s.generate_pnts_set) p.generate_pnts = s.generate_pnts;
            if (s.generate_las_set) p.generate_las = s.generate_las;
            if (s.generate_laz_set) p.generate_laz = s.generate_laz;
            if (s.generate_pcd_set) p.generate_pcd = s.generate_pcd;
            if (s.generate_point_s3mb_set) p.generate_point_s3mb = s.generate_point_s3mb;
            if (s.generate_point_ply_set) p.generate_point_ply = s.generate_point_ply;
            if (s.merge_point_set) p.merge_point = s.merge_point;
            if (s.output_ply_with_normals_set) p.output_ply_with_normals = s.output_ply_with_normals;
            if (s.ply_with_normals_level_set) p.ply_with_normals_level = s.ply_with_normals_level;
            if (s.groud_extract_set) p.groud_extract = s.groud_extract;
            if (s.scene_type_set) p.scene_type = s.scene_type;
            if (s.max_size_building_set) p.max_size_building = s.max_size_building;
            if (s.iter_dist_set) p.iter_dist = s.iter_dist;
            if (s.iter_angle_set) p.iter_angle = s.iter_angle;
            if (s.usable_memory_GB_set) p.usable_memory_GB = s.usable_memory_GB;
            if (s.stereo_mode_set) p.stereo_mode = s.stereo_mode;
            if (s.global_min_depth_set) p.global_min_depth = s.global_min_depth;
            if (s.output_mesh_set) p.output_mesh = s.output_mesh;
            if (s.generate_ply_set) p.generate_ply = s.generate_ply;
            if (s.generate_obj_set) p.generate_obj = s.generate_obj;
            if (s.generate_fbx_set) p.generate_fbx = s.generate_fbx;
            if (s.generate_b3dm_set) p.generate_b3dm = s.generate_b3dm;
            if (s.generate_osgb_set) p.generate_osgb = s.generate_osgb;
            if (s.generate_s3mb_set) p.generate_s3mb = s.generate_s3mb;
            if (s.generate_i3s_set) p.generate_i3s = s.generate_i3s;
            if (s.water_refine_set) p.water_refine = s.water_refine;
            if (s.water_remove_set) p.water_remove = s.water_remove;
            if (s.model_simplify_set) p.model_simplify = s.model_simplify;
            if (s.generate_dem_set) p.generate_dem = s.generate_dem;
            if (s.dem_resolution_set) p.dem_resolution = s.dem_resolution;
            if (s.generate_contour_set) p.generate_contour = s.generate_contour;
            if (s.contour_interval_set) p.contour_interval = s.contour_interval;
            if (s.contour_datum_set) p.contour_datum = s.contour_datum;
            if (s.contour_elevation_annotation_radius_set) p.contour_elevation_annotation_radius = s.contour_elevation_annotation_radius;
            if (s.use_contour_filter_set) p.use_contour_filter = s.use_contour_filter;
            if (s.contour_filter_length_set) p.contour_filter_length = s.contour_filter_length;
            if (s.generate_grid_and_tin_set) p.generate_grid_and_tin = s.generate_grid_and_tin;
            if (s.grid_interval_set) p.grid_interval = s.grid_interval;
            if (s.cut_AOI_set) p.cut_AOI = s.cut_AOI;
            if (s.quality_level_set) p.quality_level = s.quality_level;
            params.parameter_3d = p;
        }

        // Parameter3DGS
        if (jp.use_parameter_3dgs) {
            const auto& s = jp.parameter_3dgs;
            flight_hub_sdk::ReconParameter3DGS p;
            if (!s.SfM_method.empty()) p.SfM_method = s.SfM_method;
            if (s.sfm_feature_quantity_level_set) p.sfm_feature_quantity_level = s.sfm_feature_quantity_level;
            if (s.use_image_position_set) p.use_image_position = s.use_image_position;
            if (s.generate_blocks_exchangge_xml_set) p.generate_blocks_exchangge_xml = s.generate_blocks_exchangge_xml;
            if (!s.blocks_exchange_xml_geo_desc.cs_type.empty()) p.blocks_exchange_xml_geo_desc = toGeo(s.blocks_exchange_xml_geo_desc);
            if (!s.output_geo_desc.cs_type.empty()) p.output_geo_desc = toGeo(s.output_geo_desc);
            if (!s.global_data_type.empty()) p.global_data_type = s.global_data_type;
            if (s.quality_level_3dgs_set) p.quality_level_3dgs = s.quality_level_3dgs;
            if (s.generate_terrags_set) p.generate_terrags = s.generate_terrags;
            if (s.generate_3dtiles_set) p.generate_3dtiles = s.generate_3dtiles;
            if (s.generate_3dgs_dom_set) p.generate_3dgs_dom = s.generate_3dgs_dom;
            params.parameter_3dgs = p;
        }

        // ParameterLidar
        if (jp.use_parameter_lidar) {
            const auto& s = jp.parameter_lidar;
            flight_hub_sdk::ReconParameterLidar p;
            if (s.output_pointcloud_set) p.output_pointcloud = s.output_pointcloud;
            if (s.density_percent_level_set) p.density_percent_level = s.density_percent_level;
            if (s.generate_pnts_set) p.generate_pnts = s.generate_pnts;
            if (s.generate_las_set) p.generate_las = s.generate_las;
            if (s.generate_laz_set) p.generate_laz = s.generate_laz;
            if (s.generate_pcd_set) p.generate_pcd = s.generate_pcd;
            if (s.generate_point_s3mb_set) p.generate_point_s3mb = s.generate_point_s3mb;
            if (s.generate_point_ply_set) p.generate_point_ply = s.generate_point_ply;
            if (s.merge_point_set) p.merge_point = s.merge_point;
            if (s.output_ply_with_normals_set) p.output_ply_with_normals = s.output_ply_with_normals;
            if (s.ply_with_normals_level_set) p.ply_with_normals_level = s.ply_with_normals_level;
            if (s.groud_extract_set) p.groud_extract = s.groud_extract;
            if (s.scene_type_set) p.scene_type = s.scene_type;
            if (s.max_size_building_set) p.max_size_building = s.max_size_building;
            if (s.iter_dist_set) p.iter_dist = s.iter_dist;
            if (s.iter_angle_set) p.iter_angle = s.iter_angle;
            if (s.usable_memory_GB_set) p.usable_memory_GB = s.usable_memory_GB;
            if (s.colorize_point_set) p.colorize_point = s.colorize_point;
            if (s.lpp_optimise_set) p.lpp_optimise = s.lpp_optimise;
            if (s.refine_calibrate_set) p.refine_calibrate = s.refine_calibrate;
            if (s.smooth_point_set) p.smooth_point = s.smooth_point;
            if (s.sample_distance_set) p.sample_distance = s.sample_distance;
            if (s.output_height_offset_set) p.output_height_offset = s.output_height_offset;
            if (s.quality_level_set) p.quality_level = s.quality_level;
            if (s.can_process_L1_set) p.can_process_L1 = s.can_process_L1;
            if (!s.output_geo_desc.cs_type.empty()) p.output_geo_desc = toGeo(s.output_geo_desc);
            if (s.output_mesh_set) p.output_mesh = s.output_mesh;
            if (s.quality_level_mesh_set) p.quality_level_mesh = s.quality_level_mesh;
            if (s.model_simplify_set) p.model_simplify = s.model_simplify;
            if (s.generate_obj_set) p.generate_obj = s.generate_obj;
            if (s.generate_fbx_set) p.generate_fbx = s.generate_fbx;
            if (s.generate_ply_set) p.generate_ply = s.generate_ply;
            if (s.generate_osgb_set) p.generate_osgb = s.generate_osgb;
            if (s.generate_b3dm_set) p.generate_b3dm = s.generate_b3dm;
            if (s.generate_i3s_set) p.generate_i3s = s.generate_i3s;
            if (s.generate_maps_2d_set) p.generate_maps_2d = s.generate_maps_2d;
            if (s.generate_maps_2d_tiff_set) p.generate_maps_2d_tiff = s.generate_maps_2d_tiff;
            if (s.generate_maps_2d_tiff_cog_set) p.generate_maps_2d_tiff_cog = s.generate_maps_2d_tiff_cog;
            if (s.resolution_type_2d_set) p.resolution_type_2d = s.resolution_type_2d;
            if (s.resolution_level_2d_set) p.resolution_level_2d = s.resolution_level_2d;
            if (s.resolution_gsd_2d_set) p.resolution_gsd_2d = s.resolution_gsd_2d;
            if (s.dom_tile_size_set) p.dom_tile_size = s.dom_tile_size;
            if (s.use_gcj02_set) p.use_gcj02 = s.use_gcj02;
            if (s.generate_dem_set) p.generate_dem = s.generate_dem;
            if (s.dem_resolution_set) p.dem_resolution = s.dem_resolution;
            if (s.generate_contour_set) p.generate_contour = s.generate_contour;
            if (s.contour_interval_set) p.contour_interval = s.contour_interval;
            if (s.contour_datum_set) p.contour_datum = s.contour_datum;
            if (s.contour_elevation_annotation_radius_set) p.contour_elevation_annotation_radius = s.contour_elevation_annotation_radius;
            if (s.use_contour_filter_set) p.use_contour_filter = s.use_contour_filter;
            if (s.contour_filter_length_set) p.contour_filter_length = s.contour_filter_length;
            if (s.generate_3dgs_set) p.generate_3dgs = s.generate_3dgs;
            if (s.quality_level_3dgs_set) p.quality_level_3dgs = s.quality_level_3dgs;
            if (s.generate_3dtiles_set) p.generate_3dtiles = s.generate_3dtiles;
            params.parameter_lidar = p;
        }

        $self->createReconstruction(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildCreateReconstructionResult& result) {
                if (callback) { callback->onCreateReconstruction(err, result); delete callback; }
            });
    }

    // [Terra Build] 删除单个模型文件
    void deleteModelFileWrapper(int file_id, ResultCallback* callback) {
        $self->deleteModelFile(file_id,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); delete callback; }
            });
    }

    // [Terra Build] 批量删除模型文件
    void batchDeleteModelFilesWrapper(const std::vector<int>& file_ids, ResultCallback* callback) {
        $self->batchDeleteModelFiles(file_ids,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); delete callback; }
            });
    }

    // [Terra Build] 创建全景拼接任务
    void createPanoramaWrapper(const std::string& name,
                               const std::vector<std::string>& folder_uuids,
                               const std::string& wkt,
                               bool is_shown_on_map,
                               TerraBuildCreatePanoramaCallback* callback) {
        flight_hub_sdk::CreatePanoramaParams params;
        params.name = name;
        params.folder_uuids = folder_uuids;
        if (!wkt.empty()) params.wkt = wkt;
        if (is_shown_on_map) params.is_shown_on_map = true;
        $self->createPanorama(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildCreatePanoramaResult& result) {
                if (callback) { callback->onCreatePanorama(err, result); delete callback; }
            });
    }

    // [Terra Build] 获取重建图像列表
    void getReconstructionImagesWrapper(const std::string& file_uuid,
                                        TerraBuildReconstructionImagesCallback* callback) {
        $self->getReconstructionImages(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildReconstructionImagesResult& result) {
                if (callback) { callback->onGetReconstructionImages(err, result); delete callback; }
            });
    }

    // [Terra Build] 监听重建任务状态变更推送
    int listenReconstructionStatusChangedWrapper(TerraBuildReconstructionStatusChangedCallback* callback) {
        std::shared_ptr<TerraBuildReconstructionStatusChangedCallback> cb(callback);
        return $self->listenReconstructionStatusChanged(
            [cb](const flight_hub_sdk::TerraBuildReconstructionStatusChanged& msg) {
                cb->onReconstructionStatusChanged(msg);
            });
    }

    // [Terra Build] 取消监听重建任务状态变更推送
    void cancelListenReconstructionStatusChangedWrapper(int listen_id) {
        $self->cancelListenReconstructionStatusChanged(listen_id);
    }

    // [Terra 模型管理] 模型文件上传回调
    void uploadModelCallbackWrapper(const terra_build_swig::TerraUploadCallbackJavaParams& jp,
                                    TerraUploadCallbackCallback* callback) {
        flight_hub_sdk::TerraUploadCallbackParams params;
        params.object_key = jp.object_key;
        params.name = jp.name;
        if (jp.parent_id_set) params.parent_id = jp.parent_id;
        if (jp.drive_uuid_set) params.drive_uuid = jp.drive_uuid;
        if (jp.show_on_map_set) params.show_on_map = jp.show_on_map;
        $self->uploadModelCallback(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraUploadCallbackResult& result) {
                if (callback) { callback->onTerraUploadCallback(err, result); delete callback; }
            });
    }

    // [Terra 模型管理] 模型重命名
    void renameModelWrapper(int file_id,
                            const terra_build_swig::TerraRenameModelJavaParams& jp,
                            ResultCallback* callback) {
        flight_hub_sdk::TerraRenameModelParams params;
        params.name = jp.name;
        if (jp.in_uniform_height_set) params.in_uniform_height = jp.in_uniform_height;
        if (jp.model_display_mode_set) params.model_display_mode = jp.model_display_mode;
        if (jp.collected_at_set) params.collected_at = jp.collected_at;
        $self->renameModel(file_id, params,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); delete callback; }
            });
    }

    // [Terra 模型管理] 批量加载/取消加载模型到地图
    void showModelOnMapWrapper(const terra_build_swig::TerraShowOnMapJavaParams& jp,
                               TerraShowOnMapCallback* callback) {
        flight_hub_sdk::TerraShowOnMapParams params;
        params.ids = jp.ids;
        if (jp.show_on_map_set) params.show_on_map = jp.show_on_map;
        if (jp.is_distributed_set) params.is_distributed = jp.is_distributed;
        if (jp.select_filter_set) {
            flight_hub_sdk::TerraSelectFilterParams sf;
            if (jp.select_filter.selected_parent_id_set) sf.selected_parent_id = jp.select_filter.selected_parent_id;
            sf.except_ids = jp.select_filter.except_ids;
            sf.key = jp.select_filter.key;
            params.select_filter = sf;
        }
        $self->showModelOnMap(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraShowOnMapResult& result) {
                if (callback) { callback->onTerraShowOnMap(err, result); delete callback; }
            });
    }

    // [Terra 模型管理] 监听模型解压进度推送
    int listenTerraUnzipProgressWrapper(TerraUnzipProgressCallback* callback) {
        return $self->listenTerraUnzipProgress(
            [callback](const flight_hub_sdk::TerraUnzipProgressMsg& msg) {
                if (callback) callback->onTerraUnzipProgress(msg);
            });
    }

    // [Terra 模型管理] 取消监听模型解压进度推送
    void cancelListenTerraUnzipProgressWrapper(int listen_id) {
        $self->cancelListenTerraUnzipProgress(listen_id);
    }

    // [Terra 模型管理] 监听模型状态变更推送
    int listenTerraModelStatusChangedWrapper(TerraModelStatusChangedCallback* callback) {
        return $self->listenTerraModelStatusChanged(
            [callback](const flight_hub_sdk::TerraModelStatusChangedMsg& msg) {
                if (callback) callback->onTerraModelStatusChanged(msg);
            });
    }

    // [Terra 模型管理] 取消监听模型状态变更推送
    void cancelListenTerraModelStatusChangedWrapper(int listen_id) {
        $self->cancelListenTerraModelStatusChanged(listen_id);
    }

    // [Terra 模型管理] 监听文件展示地图状态推送
    int listenTerraFileShowOnMapWrapper(TerraFileShowOnMapCallback* callback) {
        return $self->listenTerraFileShowOnMap(
            [callback](const flight_hub_sdk::TerraFileShowOnMapMsg& msg) {
                if (callback) callback->onTerraFileShowOnMap(msg);
            });
    }

    // [Terra 模型管理] 取消监听文件展示地图状态推送
    void cancelListenTerraFileShowOnMapWrapper(int listen_id) {
        $self->cancelListenTerraFileShowOnMap(listen_id);
    }

    // [Terra 模型管理] 监听模型隐藏地图推送 (topic_media_file_hide_on_map)
    int listenTerraMediaFileHideOnMapWrapper(TerraMediaFileHideOnMapCallback* callback) {
        return $self->listenTerraMediaFileHideOnMap(
            [callback](const flight_hub_sdk::TerraMediaFileHideOnMapMsg& msg) {
                if (callback) callback->onTerraMediaFileHideOnMap(msg);
            });
    }

    void cancelListenTerraMediaFileHideOnMapWrapper(int listen_id) {
        $self->cancelListenTerraMediaFileHideOnMap(listen_id);
    }

    // [Terra 模型管理] 监听模型显示地图推送 (topic_media_file_show_on_map)
    int listenTerraMediaFileShowOnMapWrapper(TerraMediaFileShowOnMapCallback* callback) {
        return $self->listenTerraMediaFileShowOnMap(
            [callback](const flight_hub_sdk::TerraMediaFileShowOnMapMsg& msg) {
                if (callback) callback->onTerraMediaFileShowOnMap(msg);
            });
    }

    void cancelListenTerraMediaFileShowOnMapWrapper(int listen_id) {
        $self->cancelListenTerraMediaFileShowOnMap(listen_id);
    }

    // [Terra 模型管理] 获取模型文件详情
    void getModelDetailWrapper(int file_id, TerraGetModelDetailCallback* callback) {
        $self->getModelDetail(file_id,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraModelDetailResult& r) {
                if (!callback) return;
                terra_build_swig::TerraModelDetailJavaResult jr;
                jr.id            = r.id;          jr.uuid          = r.uuid;
                jr.name          = r.name;         jr.file_type     = r.file_type;
                jr.sub_file_type = r.sub_file_type; jr.size          = r.size;
                jr.suffix        = r.suffix;       jr.mod           = r.mod;
                jr.show_on_map   = r.show_on_map;  jr.state         = r.state;
                jr.selected_wkt  = r.selected_wkt; jr.parent_id     = r.parent_id;
                jr.update_at     = r.update_at;    jr.create_at     = r.create_at;
                jr.url           = r.url;          jr.webp_url      = r.webp_url;
                jr.preview_url   = r.preview_url;  jr.thumbnail_url = r.thumbnail_url;
                jr.object_key    = r.object_key;   jr.index_file_key = r.index_file_key;
                jr.file_prefix   = r.file_prefix;  jr.webp_file_prefix = r.webp_file_prefix;
                // 边界：直接赋值
                jr.max_lat = r.boundary.max_lat;   jr.min_lat = r.boundary.min_lat;
                jr.max_lng = r.boundary.max_lng;   jr.min_lng = r.boundary.min_lng;
                jr.max_alt = r.boundary.max_alt;   jr.min_alt = r.boundary.min_alt;
                jr.latitude = r.boundary.latitude; jr.longitude = r.boundary.longitude;
                jr.altitude = r.boundary.altitude;
                jr.half_x = r.boundary.half_x;     jr.half_y = r.boundary.half_y;
                jr.half_z = r.boundary.half_z;
                jr.max_level = r.boundary.max_level; jr.min_level = r.boundary.min_level;
                jr.start_lat = r.boundary.start_lat; jr.end_lat = r.boundary.end_lat;
                jr.start_lng = r.boundary.start_lng; jr.end_lng = r.boundary.end_lng;
                jr.transform = r.boundary.transform;
                jr.max_intensity = r.intensity_info.max_intensity;
                jr.min_intensity = r.intensity_info.min_intensity;
                jr.max_return_number = r.max_return_number;
                jr.bounding_points = r.bounding_points;
                jr.device = r.ext.device; jr.duration = r.ext.duration;
                jr.photographer = r.ext.photographer; jr.resolution = r.ext.resolution;
                jr.ext_height = r.ext.height; jr.ext_width = r.ext.width;
                jr.zoom_factor = r.ext.zoom_factor; jr.task_name = r.ext.task_name;
                jr.shoot_lng = r.ext.shoot_position.lng; jr.shoot_lat = r.ext.shoot_position.lat;
                jr.shoot_abs_alt = r.ext.shoot_position.abs_alt; jr.shoot_rel_alt = r.ext.shoot_position.rel_alt;
                jr.photo_lng = r.ext.photo_position.lng; jr.photo_lat = r.ext.photo_position.lat;
                jr.photo_abs_alt = r.ext.photo_position.abs_alt; jr.photo_rel_alt = r.ext.photo_position.rel_alt;
                jr.line_name = r.ext.line_name; jr.is_original = r.ext.is_original;
                jr.gimbal_pitch = r.ext.gimbal_pitch; jr.gimbal_yaw = r.ext.gimbal_yaw;
                jr.gimbal_yaw_degree = r.ext.gimbal_yaw_degree; jr.gimbal_roll = r.ext.gimbal_roll;
                jr.focal_length = r.ext.focal_length;
                jr.payload_model_key = r.ext.payload_model_key; jr.drone_model_key = r.ext.drone_model_key;
                jr.create_time = r.ext.create_time; jr.wayline_action_uuid = r.ext.wayline_action_uuid;
                jr.wayline_uuid = r.ext.wayline_uuid; jr.origin_wayline_uuid = r.ext.origin_wayline_uuid;
                jr.digital_zoom_ratio = r.ext.digital_zoom_ratio; jr.length_in35mm_film = r.ext.length_in35mm_film;
                jr.file_tags = r.file_tags; jr.user_name = r.user_name;
                callback->onGetModelDetail(err, jr);
                delete callback;
            });
    }

    // [Terra 模型管理] 设置 LAS 坐标系并触发格式转换
    void setLasWktWrapper(const terra_build_swig::TerraSetLasWktJavaParams& jp,
                          TerraSetLasWktCallback* callback) {
        flight_hub_sdk::TerraSetLasWktParams params;
        params.file_uuid = jp.file_uuid;
        params.wkt       = jp.wkt;
        $self->setLasWkt(params,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onSetLasWkt(err); delete callback; }
            });
    }
}

// ── IElementModule wrapper methods ──────────────────────────────────────────
%extend flight_hub_sdk::IElementModule {
    // 创建要素（CreateElementParams 无 std::optional，直接传入）
    void createElementWrapper(
            const flight_hub_sdk::CreateElementParams& params,
            ElementCreateElementCallback* callback) {
        $self->createElement(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateElementResult& r) {
                if (callback) { callback->onCreateElement(err, r); delete callback; }
            });
    }

    // 更新要素（UpdateElementParams 含 std::optional，需通过 JavaParams 中转）
    void updateElementWrapper(
            const terra_build_swig::ElementUpdateJavaParams& jp,
            ElementUpdateElementCallback* callback) {
        flight_hub_sdk::UpdateElementParams params;
        params.id = jp.id;
        if (jp.name_set) params.name = jp.name;
        if (jp.content_set) {
            flight_hub_sdk::ElementResourceContent content;
            content.type = jp.content.type;
            content.properties.color           = jp.content.properties.color;
            content.properties.clamp_to_ground = jp.content.properties.clamp_to_ground;
            content.properties.is3d            = jp.content.properties.is3d;
            content.properties.is_transparent  = jp.content.properties.is_transparent;
            content.properties.font_size       = jp.content.properties.font_size;
            content.properties.icon            = jp.content.properties.icon;
            content.geometry.type              = jp.content.geometry.type;
            content.geometry.radius            = jp.content.geometry.radius;
            content.geometry.coordinates       = jp.content.geometry.coordinates;
            content.geometry.bread_crumbs      = jp.content.geometry.bread_crumbs;
            for (const auto& img : jp.content.images) {
                flight_hub_sdk::ElementMediaImage mi;
                mi.object_key = img.object_key; mi.media_file_id = img.media_file_id; mi.media_file_uuid = img.media_file_uuid;
                content.media_resources.images.push_back(mi);
            }
            params.content = content;
        }
        if (jp.display_set) params.display = jp.display;
        if (jp.remark_set)  params.remark  = jp.remark;
        if (jp.status_set)  params.status  = jp.status;
        if (jp.elevation_load_status_set) params.elevation_load_status = jp.elevation_load_status;
        if (jp.user_status_set) params.user_status = jp.user_status;
        params.target_layer_id = jp.target_layer_id;
        for (const auto& item : jp.orders) {
            params.orders.push_back(item);
        }
        $self->updateElement(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UpdateElementResult& r) {
                if (callback) { callback->onUpdateElement(err, r); delete callback; }
            });
    }

    // 查询元素层级列表（ListElementHierarchyParams 含 std::optional，需通过 JavaParams 中转）
    void listElementHierarchyWrapper(
            const terra_build_swig::ListElementHierarchyJavaParams& jp,
            ElementListHierarchyCallback* callback) {
        flight_hub_sdk::ListElementHierarchyParams params;
        params.group_id     = jp.group_id;
        params.pid          = jp.pid;
        if (jp.is_distributed_set) params.is_distributed = jp.is_distributed;
        if (jp.type_set)           params.type           = jp.type;
        if (jp.element_uuid_set)   params.element_uuid   = jp.element_uuid;
        params.exclude_copy = jp.exclude_copy;
        $self->listElementHierarchy(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ListElementHierarchyResult& r) {
                if (callback) { callback->onListElementHierarchy(err, r); delete callback; }
            });
    }

    // 设置要素显示状态（SetElementVisibilityParams 无 std::optional，直接传入）
    void setElementVisibilityWrapper(
            const flight_hub_sdk::SetElementVisibilityParams& params,
            ElementSetVisibilityCallback* callback) {
        $self->setElementVisibility(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SetElementVisibilityResult& r) {
                if (callback) { callback->onSetElementVisibility(err, r); delete callback; }
            });
    }

    // 删除要素（DeleteElementParams 无 std::optional，直接传入）
    void deleteElementWrapper(
            const flight_hub_sdk::DeleteElementParams& params,
            ElementDeleteElementCallback* callback) {
        $self->deleteElement(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeleteElementResult& r) {
                if (callback) { callback->onDeleteElement(err, r); delete callback; }
            });
    }

    // 监听要素创建事件
    int listenElementCreateWrapper(ElementCreateCallback* callback) {
        return $self->listenElementCreate(
            [callback](const flight_hub_sdk::ElementCreateMsg& msg) {
                if (callback) callback->onElementCreate(msg);
            });
    }
    void cancelListenElementCreateWrapper(int listen_id) {
        $self->cancelListenElementCreate(listen_id);
    }

    // 监听要素更新事件
    int listenElementUpdateWrapper(ElementUpdateCallback* callback) {
        return $self->listenElementUpdate(
            [callback](const flight_hub_sdk::ElementUpdateMsg& msg) {
                if (callback) callback->onElementUpdate(msg);
            });
    }
    void cancelListenElementUpdateWrapper(int listen_id) {
        $self->cancelListenElementUpdate(listen_id);
    }

    // 监听要素删除事件
    int listenElementDeleteWrapper(ElementDeleteCallback* callback) {
        return $self->listenElementDelete(
            [callback](const flight_hub_sdk::ElementDeleteMsg& msg) {
                if (callback) callback->onElementDelete(msg);
            });
    }
    void cancelListenElementDeleteWrapper(int listen_id) {
        $self->cancelListenElementDelete(listen_id);
    }
}

%extend flight_hub_sdk::ISafetyServiceModule {
    // [信号地图] 获取信号地图;
    void searchSignalMapsWrapper(const std::vector<std::string>& device_sns, const std::vector<std::string>& data_types, SearchSignalMapsCallback* callback) {
        $self->searchSignalMaps(device_sns, data_types, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SignalMapsResults& result) {
            if (callback) {
                callback->onSearchSignalMaps(err, result);
                //delete callback;
            }
        });
    }

    // [自定义飞行区域] 创建自定义飞行区域;
    void createCustomizedFlightAreasWrapper(const flight_hub_sdk::CreateCustomizedFlightAreasParams& request_params, CreateCustomizedFlightAreasCallback* callback) {
        $self->createCustomizedFlightAreas(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateFlightAreasResults& result) {
            if (callback) {
                callback->onCreateCustomizedFlightAreas(err, result);
                //delete callback;
            }
        });
    }

    // [自定义飞行区域] 获取自定义飞行区域;
    void getCustomizedFlightAreasWrapper(const flight_hub_sdk::GetCustomizedFlightAreasParams& request_params, GetCustomizedFlightAreasCallback* callback) {
        $self->getCustomizedFlightAreas(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightAreasResults& result) {
            if (callback) {
                callback->onGetCustomizedFlightAreas(err, result);
                //delete callback;
            }
        });
    }

    // [自定义飞行区域] 更新自定义飞行区域;
    void updateCustomizedFlightAreasWrapper(const flight_hub_sdk::UpdateCustomizedFlightAreasParams& request_params, UpdateCustomizedFlightAreasCallback* callback) {
        $self->updateCustomizedFlightAreas(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UpdateFlightAreasResults& result) {
            if (callback) {
                callback->onUpdateCustomizedFlightAreas(err, result);
                //delete callback;
            }
        });
    }

    // [自定义飞行区域] 删除自定义飞行区域;
    void deleteCustomizedFlightAreasWrapper(const std::string& flight_area_id, ResultCallbackWithError* callback) {
        $self->deleteCustomizedFlightAreas(flight_area_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //delete callback;
            }
        });
    }

    // [自定义飞行区域] 同步自定义飞行区域;
    void syncCustomizedFlightAreasWrapper(const std::vector<std::string>& device_sns, SyncCustomizedFlightAreasCallback* callback) {
        $self->syncCustomizedFlightAreas(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SyncFlightAreasResults& result) {
            if (callback) {
                callback->onSyncCustomizedFlightAreas(err, result);
                //delete callback;
            }
        });
    }

    // [自定义飞行区域] 获取自定义飞行区域同步状态;
    void getFlightAreasSyncStatusWrapper(const std::vector<std::string>& device_sns, GetFlightAreasSyncStatusCallback* callback) {
        $self->getFlightAreasSyncStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetAreasSyncStatusResults& result) {
            if (callback) {
                callback->onGetFlightAreasSyncStatus(err, result);
                //delete callback;
            }
        });
    }

    // [地形障碍物] 查询离线地图模型信息;
    void getOfflineMapsModelInfoWrapper(GetOfflineMapsModelInfoCallback* callback) {
        $self->getOfflineMapsModelInfo([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetOfflineMapsModelInfoResults& result) {
            if (callback) {
                callback->onGetOfflineMapsModelInfo(err, result);
                //delete callback;
            }
        });
    }

    // [地形障碍物] 创建/更新离线地图模型;
    void updateOfflineMapsModelInfoWrapper(const flight_hub_sdk::UpdateOfflineMapsParams& request_params, ResultCallbackWithError* callback) {
        $self->updateOfflineMapsModelInfo(request_params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //delete callback;
            }
        });
    }

    // [地形障碍物] 取消离线地图生成;
    void cancelOfflineMapsModelInfoWrapper(ResultCallbackWithError* callback) {
        $self->cancelOfflineMapsModelInfo([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //delete callback;
            }
        });
    }

    // [地形障碍物] 修改离线地图(开启/关闭);
    void setOfflineMapsWrapper(const flight_hub_sdk::SetOfflineMapsParams& request_params, ResultCallbackWithError* callback) {
        $self->setOfflineMaps(request_params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //delete callback;
            }
        });
    }

    // [地形障碍物] 离线地图同步;
    void syncOfflineMapsWrapper(const std::vector<std::string>& device_sns, SyncOfflineMapsCallback* callback) {
        $self->syncOfflineMaps(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OfflineMapsSyncResults& result) {
            if (callback) {
                callback->onSyncOfflineMaps(err, result);
                //delete callback;
            }
        });
    }

    // [地形障碍物] 获取设备同步状态;
    void getOfflineMapsSyncStatusWrapper(const std::vector<std::string>& device_sns, GetOfflineMapsSyncStatusCallback* callback) {
        $self->getOfflineMapsSyncStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetOfflineMapsSyncStatusResults& result) {
            if (callback) {
                callback->onGetOfflineMapsSyncStatus(err, result);
                //delete callback;
            }
        });
    }

    void getUnlockLicenseListWrapper(flight_hub_sdk::QueryDeviceUnlockLicenseListParam const &param,GetUnlockLicenseListCallback *callback) {
        $self->getUnlockLicenseList(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::QueryDeviceUnlockLicenseListResult result) {
            if (callback) {
                callback->onGetUnlockLicenseList(err, result);
                delete callback;
            }
        });
    }

    void switchUnlockLicenseWrapper(flight_hub_sdk::SwitchUnlockLicenseParam const &param,SwitchUnlockLicenseCallback *callback) {
        $self->switchUnlockLicense(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSwitchUnlockLicense(err);
                delete callback;
            }
        });
    }

    void checkUnlockLicenseWrapper(flight_hub_sdk::CheckUnlockLicenseParam const &param,CheckUnlockLicenseCallback *callback) {
        $self->checkUnlockLicense(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CheckUnlockLicenseResult result) {
            if (callback) {
                callback->onCheckUnlockLicense(err, result);
                delete callback;
            }
        });
    }

    void submitUnlockLicenseWrapper(flight_hub_sdk::ImportUnlockLicenseParam const &param,SubmitUnlockLicenseCallback *callback) {
        $self->submitUnlockLicense(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSubmitUnlockLicense(err);
                delete callback;
            }
        });
    }

    void clearDeviceUnlockLicenseWrapper(std::string const &device_sn,ClearDeviceUnlockLicenseCallback *callback) {
        $self->clearDeviceUnlockLicense(device_sn, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onClearDeviceUnlockLicense(err);
                delete callback;
            }
        });
    }

    void uploadUnlockLicenseFileWrapper(std::string const &file_path,UploadUnlockLicenseFileCallback *callback) {
        $self->uploadUnlockLicenseFile(file_path, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UploadUnlockLicenseFileResult& result) {
            if (callback) {
                callback->onUploadUnlockLicenseFile(err, result);
                delete callback;
            }
        });
    }

    // EA340 消息推送补全（Phase D）: 飞行器离线地图开关, 需要 drone_sn 筛选订阅目标;
    int listenDroneOfflineMapEnableWrapper(std::string const &drone_sn, DroneOfflineMapEnableCallback* callback) {
        std::shared_ptr<DroneOfflineMapEnableCallback> cb(callback);
        return $self->listenDroneOfflineMapEnable(drone_sn, [cb](const flight_hub_sdk::DroneOfflineMapEnable& msg) {
            cb->onDroneOfflineMapEnable(msg);
        });
    }

    void cancelListenDroneOfflineMapEnableWrapper(int listen_id) {
        $self->cancelListenDroneOfflineMapEnable(listen_id);
    }
}

%extend flight_hub_sdk::IFlightTaskRecordModule {
    // [ATC 空管服务] 导出飞行记录;
    void exportFlightRecordsWrapper(const std::string& user_id, const flight_hub_sdk::ExportFlightRecordsRequestParams& params, ExportFlightRecordsCallback* callback) {
        $self->exportFlightRecords(user_id, params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFlightRecordsResults& results) {
            if (callback) {
                callback->onExportFlightRecords(err, results);
                delete callback;
            }
        });
    }

    // [ATC 空管服务] 查询导出进度;
    void getExportFlightRecordsProgressWrapper(const flight_hub_sdk::GetExportFlightRecordsProgressParams& params, GetExportFlightRecordsProgressCallback* callback) {
        $self->getExportFlightRecordsProgress(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetExportFlightRecordsProgressResults& results) {
            if (callback) {
                callback->onGetExportFlightRecordsProgress(err, results);
                delete callback;
            }
        });
    }
}

%extend flight_hub_sdk::IMaintenanceManagementModule {
    // [报修管理] 获取设备报修配置;
    void getRepairConfigsWrapper(const flight_hub_sdk::GetDeviceRepairConfigParams& params, GetRepairConfigsCallback* callback) {
        $self->getRepairConfigs(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRepairConfigResults& result) {
            if (callback) {
                callback->onGetRepairConfigs(err, result);
                delete callback;
            }
        });
    }

    // [报修管理] 创建报修订单;
    void createMaintenanceOrderWrapper(const flight_hub_sdk::CreateMaintenanceOrderParams& params, CreateMaintenanceOrderCallback* callback) {
        $self->createMaintenanceOrder(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateMaintenanceOrderResults& result) {
            if (callback) {
                callback->onCreateMaintenanceOrder(err, result);
                delete callback;
            }
        });
    }

    // [报修管理] 查询单个报修订单详情;
    void getMaintenanceOrderDetailWrapper(const std::string& order_number, GetMaintenanceOrderDetailCallback* callback) {
        $self->getMaintenanceOrderDetail(order_number, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::MaintenanceOrderDetailResults& result) {
            if (callback) {
                callback->onGetMaintenanceOrderDetail(err, result);
                delete callback;
            }
        });
    }

    // [报修管理] 分页查询报修订单列表;
    void getMaintenanceOrdersListWrapper(const flight_hub_sdk::GetMaintenanceOrdersListParams& params, GetMaintenanceOrdersListCallback* callback) {
        $self->getMaintenanceOrdersList(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::MaintenanceOrdersListResults& result) {
            if (callback) {
                callback->onGetMaintenanceOrdersList(err, result);
                delete callback;
            }
        });
    }
}

%include "maintenance_management_module.h"

%extend flight_hub_sdk::IDiagnosisManagementModule {
    // [定损管理] 下发定损指令;
    void createSelfDiagnosisWrapper(const flight_hub_sdk::CreateSelfDiagnosisParams& params, CreateSelfDiagnosisCallback* callback) {
        $self->createSelfDiagnosis(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onCreateSelfDiagnosis(err);
                delete callback;
            }
        });
    }

    // [定损管理] 定损操作(取消/删除);
    void operateSelfDiagnosisWrapper(const flight_hub_sdk::OperateSelfDiagnosisParams& params, OperateSelfDiagnosisCallback* callback) {
        $self->operateSelfDiagnosis(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onOperateSelfDiagnosis(err);
                delete callback;
            }
        });
    }

    // [定损管理] 查询单条定损报告详情;
    void getSelfDiagnosisReportWrapper(const std::string& report_uuid, GetSelfDiagnosisReportCallback* callback) {
        $self->getSelfDiagnosisReport(report_uuid, [callback](const flight_hub_sdk::GetSelfDiagnosisReportResults& result, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onGetSelfDiagnosisReport(err, result);
                delete callback;
            }
        });
    }

    // [定损管理] 查询定损报告列表;
    void getSelfDiagnosisReportListWrapper(const flight_hub_sdk::GetSelfDiagnosisReportListParams& params, GetSelfDiagnosisReportListCallback* callback) {
        $self->getSelfDiagnosisReportList(params, [callback](const flight_hub_sdk::GetSelfDiagnosisReportListResults& result, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onGetSelfDiagnosisReportList(err, result);
                delete callback;
            }
        });
    }
}

%include "diagnose_management_module.h"
%include "custom_sei.h"
%include "pull_stream.h"
%include "stream.h"
%include "live_stream.h"
%include "live_record_module.h"
%include "fly_to_point.h"
%include "poi_module.h"
%include "flight_task_control.h"
%include "takeoff_module.h"
%include "in_flight_wayline_module.h"
%include "flight_control_module.h"
%include "control_auth_module.h"
%include "drc_module.h"
%include "flight_settings_module.h"
%include "test_flight_module.h"
%include "onboard_ai_module.h"
%include "psdk_control_module.h"
%include "camera_control_module.h"
%include "drone_osd_module.h"
%include "dock_osd_module.h"
%include "ai_box_module.h"
%include "drone.h"
%include "device_state_module.h"
%include "device_hms_module.h"
%include "live_device_module.h"
%include "deploy_module.h"
%include "dock_control_module.h"
%include "dock.h"
%include "remote_controller.h"
%include "base_station.h"
%include "relay.h"
%include "relay_control_module.h"
%include "rtk_calibration_module.h"
%include "network_test_module.h"
%include "device_debug_module.h"
%include "media_service_module.h"

// ─────────────────────────────────────────────────────────────────────────────
// Terra Build Java 友好参数结构体（无 std::optional，供 Java 层直接 new 赋值）
// bool xxx_set 标志：true 表示该字段有效、需要发送；false 表示不传
// ─────────────────────────────────────────────────────────────────────────────
%inline %{
namespace terra_build_swig {

struct GeoDescParams {
    std::string cs_type;
    std::string geo_cs;
    std::string geo_cs_wkt;
    std::string geoid_file_name;
    std::vector<double> offset;
    std::string override_vertical_cs;
};

struct Parameter2DParams {
    std::string SfM_method;
    int sfm_feature_quantity_level = 0;
    bool sfm_feature_quantity_level_set = false;
    bool use_image_position = false;
    bool use_image_position_set = false;
    bool generate_blocks_exchangge_xml = false;
    bool generate_blocks_exchangge_xml_set = false;
    GeoDescParams blocks_exchange_xml_geo_desc;
    GeoDescParams output_geo_desc;
    double usable_memory_GB = 0.0;
    bool usable_memory_GB_set = false;
    int map_mode = 0;
    bool map_mode_set = false;
    int quality_level = 0;
    bool quality_level_set = false;
    int resolution_type_2d = 0;
    bool resolution_type_2d_set = false;
    int dom_tile_size = 0;
    bool dom_tile_size_set = false;
    double resolution_gsd_2d = 0.0;
    bool resolution_gsd_2d_set = false;
    bool use_Wallis = false;
    bool use_Wallis_set = false;
    bool use_Dehaze = false;
    bool use_Dehaze_set = false;
    bool use_gcj02 = false;
    bool use_gcj02_set = false;
    bool generate_maps_2d_tiff = false;
    bool generate_maps_2d_tiff_set = false;
    bool generate_maps_2d_tiff_cog = false;
    bool generate_maps_2d_tiff_cog_set = false;
    bool use_smart_fill_hole = false;
    bool use_smart_fill_hole_set = false;
};

struct Parameter3DParams {
    std::string SfM_method;
    int sfm_feature_quantity_level = 0;
    bool sfm_feature_quantity_level_set = false;
    bool use_image_position = false;
    bool use_image_position_set = false;
    bool generate_blocks_exchangge_xml = false;
    bool generate_blocks_exchangge_xml_set = false;
    GeoDescParams blocks_exchange_xml_geo_desc;
    GeoDescParams output_geo_desc;
    bool output_pointcloud = false;
    bool output_pointcloud_set = false;
    int density_percent_level = 0;
    bool density_percent_level_set = false;
    bool generate_pnts = false; bool generate_pnts_set = false;
    bool generate_las = false; bool generate_las_set = false;
    bool generate_laz = false; bool generate_laz_set = false;
    bool generate_pcd = false; bool generate_pcd_set = false;
    bool generate_point_s3mb = false; bool generate_point_s3mb_set = false;
    bool generate_point_ply = false; bool generate_point_ply_set = false;
    bool merge_point = false; bool merge_point_set = false;
    bool output_ply_with_normals = false; bool output_ply_with_normals_set = false;
    int ply_with_normals_level = 0; bool ply_with_normals_level_set = false;
    bool groud_extract = false; bool groud_extract_set = false;
    int scene_type = 0; bool scene_type_set = false;
    double max_size_building = 0.0; bool max_size_building_set = false;
    double iter_dist = 0.0; bool iter_dist_set = false;
    double iter_angle = 0.0; bool iter_angle_set = false;
    double usable_memory_GB = 0.0; bool usable_memory_GB_set = false;
    bool stereo_mode = false; bool stereo_mode_set = false;
    int global_min_depth = 0; bool global_min_depth_set = false;
    bool output_mesh = false; bool output_mesh_set = false;
    bool generate_ply = false; bool generate_ply_set = false;
    bool generate_obj = false; bool generate_obj_set = false;
    bool generate_fbx = false; bool generate_fbx_set = false;
    bool generate_b3dm = false; bool generate_b3dm_set = false;
    bool generate_osgb = false; bool generate_osgb_set = false;
    bool generate_s3mb = false; bool generate_s3mb_set = false;
    bool generate_i3s = false; bool generate_i3s_set = false;
    bool water_refine = false; bool water_refine_set = false;
    bool water_remove = false; bool water_remove_set = false;
    double model_simplify = 0.0; bool model_simplify_set = false;
    bool generate_dem = false; bool generate_dem_set = false;
    double dem_resolution = 0.0; bool dem_resolution_set = false;
    bool generate_contour = false; bool generate_contour_set = false;
    double contour_interval = 0.0; bool contour_interval_set = false;
    double contour_datum = 0.0; bool contour_datum_set = false;
    double contour_elevation_annotation_radius = 0.0;
    bool contour_elevation_annotation_radius_set = false;
    bool use_contour_filter = false; bool use_contour_filter_set = false;
    double contour_filter_length = 0.0; bool contour_filter_length_set = false;
    bool generate_grid_and_tin = false; bool generate_grid_and_tin_set = false;
    double grid_interval = 0.0; bool grid_interval_set = false;
    bool cut_AOI = false; bool cut_AOI_set = false;
    int quality_level = 0; bool quality_level_set = false;
};

struct Parameter3DGSParams {
    std::string SfM_method;
    int sfm_feature_quantity_level = 0; bool sfm_feature_quantity_level_set = false;
    bool use_image_position = false; bool use_image_position_set = false;
    bool generate_blocks_exchangge_xml = false; bool generate_blocks_exchangge_xml_set = false;
    GeoDescParams blocks_exchange_xml_geo_desc;
    GeoDescParams output_geo_desc;
    std::string global_data_type;
    int quality_level_3dgs = 0; bool quality_level_3dgs_set = false;
    bool generate_terrags = false; bool generate_terrags_set = false;
    bool generate_3dtiles = false; bool generate_3dtiles_set = false;
    bool generate_3dgs_dom = false; bool generate_3dgs_dom_set = false;
};

struct ParameterLidarParams {
    bool output_pointcloud = false; bool output_pointcloud_set = false;
    int density_percent_level = 0; bool density_percent_level_set = false;
    bool generate_pnts = false; bool generate_pnts_set = false;
    bool generate_las = false; bool generate_las_set = false;
    bool generate_laz = false; bool generate_laz_set = false;
    bool generate_pcd = false; bool generate_pcd_set = false;
    bool generate_point_s3mb = false; bool generate_point_s3mb_set = false;
    bool generate_point_ply = false; bool generate_point_ply_set = false;
    bool merge_point = false; bool merge_point_set = false;
    bool output_ply_with_normals = false; bool output_ply_with_normals_set = false;
    int ply_with_normals_level = 0; bool ply_with_normals_level_set = false;
    bool groud_extract = false; bool groud_extract_set = false;
    int scene_type = 0; bool scene_type_set = false;
    double max_size_building = 0.0; bool max_size_building_set = false;
    double iter_dist = 0.0; bool iter_dist_set = false;
    double iter_angle = 0.0; bool iter_angle_set = false;
    double usable_memory_GB = 0.0; bool usable_memory_GB_set = false;
    bool colorize_point = false; bool colorize_point_set = false;
    bool lpp_optimise = false; bool lpp_optimise_set = false;
    int refine_calibrate = 0; bool refine_calibrate_set = false;
    bool smooth_point = false; bool smooth_point_set = false;
    double sample_distance = 0.0; bool sample_distance_set = false;
    double output_height_offset = 0.0; bool output_height_offset_set = false;
    int quality_level = 0; bool quality_level_set = false;
    bool can_process_L1 = false; bool can_process_L1_set = false;
    GeoDescParams output_geo_desc;
    bool output_mesh = false; bool output_mesh_set = false;
    int quality_level_mesh = 0; bool quality_level_mesh_set = false;
    double model_simplify = 0.0; bool model_simplify_set = false;
    bool generate_obj = false; bool generate_obj_set = false;
    bool generate_fbx = false; bool generate_fbx_set = false;
    bool generate_ply = false; bool generate_ply_set = false;
    bool generate_osgb = false; bool generate_osgb_set = false;
    bool generate_b3dm = false; bool generate_b3dm_set = false;
    bool generate_i3s = false; bool generate_i3s_set = false;
    bool generate_maps_2d = false; bool generate_maps_2d_set = false;
    bool generate_maps_2d_tiff = false; bool generate_maps_2d_tiff_set = false;
    bool generate_maps_2d_tiff_cog = false; bool generate_maps_2d_tiff_cog_set = false;
    int resolution_type_2d = 0; bool resolution_type_2d_set = false;
    int resolution_level_2d = 0; bool resolution_level_2d_set = false;
    double resolution_gsd_2d = 0.0; bool resolution_gsd_2d_set = false;
    int dom_tile_size = 0; bool dom_tile_size_set = false;
    bool use_gcj02 = false; bool use_gcj02_set = false;
    bool generate_dem = false; bool generate_dem_set = false;
    double dem_resolution = 0.0; bool dem_resolution_set = false;
    bool generate_contour = false; bool generate_contour_set = false;
    double contour_interval = 0.0; bool contour_interval_set = false;
    double contour_datum = 0.0; bool contour_datum_set = false;
    double contour_elevation_annotation_radius = 0.0;
    bool contour_elevation_annotation_radius_set = false;
    bool use_contour_filter = false; bool use_contour_filter_set = false;
    double contour_filter_length = 0.0; bool contour_filter_length_set = false;
    bool generate_3dgs = false; bool generate_3dgs_set = false;
    int quality_level_3dgs = 0; bool quality_level_3dgs_set = false;
    bool generate_3dtiles = false; bool generate_3dtiles_set = false;
};

/// 顶层 Java 友好参数结构体
struct CreateReconstructionJavaParams {
    // 必填
    std::vector<int> reconstruction_type;
    std::string name;
    std::vector<std::string> folder_uuids;
    std::vector<int> folder_ids;
    // 可选
    double simplified_factor = 0.0;       bool simplified_factor_set = false;
    std::string wkt;
    int quality_level = 0;                bool quality_level_set = false;
    int quality_level_3dgs = 0;           bool quality_level_3dgs_set = false;
    bool is_shown_on_map = false;         bool is_shown_on_map_set = false;
    bool optimize_SfM = false;            bool optimize_SfM_set = false;
    int custom_storage_parent_id = 0;     bool custom_storage_parent_id_set = false;
    std::vector<int> generate_model_formats;
    std::string quality_report_language;
    std::string quality_report_measure_unit_system;
    // 兴趣区域（含 polygon_points）
    flight_hub_sdk::ReconPredefineArea predefine_area;
    bool use_predefine_area = false;
    // Parameter 对象
    bool use_parameter_2d = false;        Parameter2DParams parameter_2d;
    bool use_parameter_3d = false;        Parameter3DParams parameter_3d;
    bool use_parameter_3dgs = false;      Parameter3DGSParams parameter_3dgs;
     bool use_parameter_lidar = false;     ParameterLidarParams parameter_lidar;
};

/// Terra 模型上传回调 Java 友好参数结构体
struct TerraUploadCallbackJavaParams {
    std::string object_key;
    std::string name;
    long long parent_id = 0;    bool parent_id_set = false;
    std::string drive_uuid;     bool drive_uuid_set = false;
    bool show_on_map = false;   bool show_on_map_set = false;
};

/// Terra 模型重命名 Java 友好参数结构体
struct TerraRenameModelJavaParams {
    std::string name;
    bool in_uniform_height = false;   bool in_uniform_height_set = false;
    int model_display_mode = 0;       bool model_display_mode_set = false;
    long long collected_at = 0;       bool collected_at_set = false;
};

/// Terra 全选反选筛选条件 Java 友好参数结构体
struct TerraSelectFilterJavaParams {
    int selected_parent_id = 0; bool selected_parent_id_set = false;
    std::vector<int> except_ids;
    std::string key;
};

/// Terra 加载/取消加载模型 Java 友好参数结构体
struct TerraShowOnMapJavaParams {
    std::vector<int> ids;
    bool show_on_map = false;       bool show_on_map_set = false;
    bool is_distributed = false;    bool is_distributed_set = false;
    TerraSelectFilterJavaParams select_filter;
    bool select_filter_set = false;
};

/// Terra 模型文件详情 Java 友好结果结构体（展平所有嵌套可选字段）
struct TerraModelDetailJavaResult {
    // 基础字段
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
    // 边界（3D + 2D，字段缺失时服务端返回默认值 0.0）
    double max_lat = 0;    double min_lat = 0;
    double max_lng = 0;    double min_lng = 0;
    double max_alt = 0;    double min_alt = 0;
    double latitude = 0;   double longitude = 0;
    double altitude = 0;
    double half_x = 0;     double half_y = 0;
    double half_z = 0;
    std::vector<double> transform;
    int max_level = 0;     int min_level = 0;
    double start_lat = 0;  double end_lat = 0;
    double start_lng = 0;  double end_lng = 0;
    // 强度信息
    double max_intensity = 0;
    double min_intensity = 0;
    // Lidar 最大回波
    double max_return_number = 0;
    // 地理边界多边形
    std::vector<flight_hub_sdk::TerraModelGeo2DPoint> bounding_points;
    // 媒体扩展信息
    std::string device;
    double duration = 0;
    std::string photographer;
    std::string resolution;
    int ext_height = 0;
    int ext_width = 0;
    double zoom_factor = 0;
    std::string task_name;
    double shoot_lng = 0; double shoot_lat = 0; double shoot_abs_alt = 0; double shoot_rel_alt = 0;
    double photo_lng = 0; double photo_lat = 0; double photo_abs_alt = 0; double photo_rel_alt = 0;
    std::string line_name;
    bool is_original = false;
    double gimbal_pitch = 0;
    double gimbal_yaw = 0;
    double gimbal_yaw_degree = 0;
    double gimbal_roll = 0;
    double focal_length = 0;
    std::string payload_model_key;
    std::string drone_model_key;
    std::string create_time;
    std::string wayline_action_uuid;
    std::string wayline_uuid;
    std::string origin_wayline_uuid;
    double digital_zoom_ratio = 0;
    double length_in35mm_film = 0;
    // 标签与用户
    std::vector<std::string> file_tags;
    std::string user_name;
};

/// Element 查询层级列表 Java 友好参数结构体
struct ListElementHierarchyJavaParams {
    std::string group_id;
    std::string pid;
    bool is_distributed = false;   bool is_distributed_set = false;
    int type = 0;                  bool type_set = false;
    std::string element_uuid;      bool element_uuid_set = false;
    bool exclude_copy = false;
};

/// Element 地理坐标点 Java 参数结构体
struct ElementGeoPointJavaParams {
    double lng = 0.0;
    double lat = 0.0;
    double alt = 0.0;
};

/// Element 渲染属性 Java 参数结构体
struct ElementPropertiesJavaParams {
    std::string color;
    bool clamp_to_ground = false;
    bool is3d = false;
    bool is_transparent = false;
    int font_size = 0;
    int icon = 0;
};

/// Element 媒体图片 Java 参数结构体
struct ElementMediaImageJavaParams {
    std::string object_key;
    int media_file_id = 0;
    std::string media_file_uuid;
};

/// Element 几何数据 Java 参数结构体
struct ElementGeometryJavaParams {
    std::string type;
    double radius = 0.0;
    std::string coordinates;     // raw JSON 透传
    std::string bread_crumbs;    // raw JSON 透传
};

/// Element 几何内容（GeoJSON Feature）Java 参数结构体
struct ElementResourceContentJavaParams {
    std::string type;   // 固定传 "Feature"
    ElementPropertiesJavaParams properties;
    ElementGeometryJavaParams geometry;
    std::vector<ElementMediaImageJavaParams> images;  // media_resources.images
};

/// Element 更新要素 Java 友好参数结构体
struct ElementUpdateJavaParams {
    std::string id;
    std::string name;                           bool name_set = false;
    ElementResourceContentJavaParams content;   bool content_set = false;
    int display = 0;                            bool display_set = false;
    std::string remark;                         bool remark_set = false;
    int status = 0;                             bool status_set = false;
    int elevation_load_status = 0;              bool elevation_load_status_set = false;
    int user_status = 0;                        bool user_status_set = false;
    std::string target_layer_id;
    std::vector<flight_hub_sdk::ElementResourceOrderItem> orders;
};

/// Terra LAS 坐标系设置 Java 参数结构体
struct TerraSetLasWktJavaParams {
    std::string file_uuid;
    std::string wkt;
};

} // namespace terra_build_swig
%}

%include "terra_build.h"
%include "safety_service_module.h"
%include "flight_task_record_module.h"
%include "log_management_module.h"
%include "device_manage_module.h"
%include "firmware_manage_module.h"
%include "psdk_resource_module.h"
%include "flash_led_module.h"
%include "wayline_manage_module.h"
%include "ai_inside_manage_module.h"
%include "ai_alert_module.h"
%include "live_sync_module.h"
%include "hms_module.h"
%include "element.h"
// Element 模块结构体向量（必须在 %include "element.h" 之后声明）
%template(ElementItemInfoVector) std::vector<flight_hub_sdk::ElementItemInfo>;
%template(ElementGroupInfoVector) std::vector<flight_hub_sdk::ElementGroupInfo>;
%template(ElementGeoPointVector) std::vector<flight_hub_sdk::ElementGeoPoint>;
%template(ElementMediaImageVector) std::vector<flight_hub_sdk::ElementMediaImage>;
%template(ElementResourceOrderItemVector) std::vector<flight_hub_sdk::ElementResourceOrderItem>;
%template(ElementGeoPointJavaParamsVector) std::vector<terra_build_swig::ElementGeoPointJavaParams>;
%template(ElementMediaImageJavaParamsVector) std::vector<terra_build_swig::ElementMediaImageJavaParams>;
%include "workspace.h"
%include "flight_hub_sdk.h"
