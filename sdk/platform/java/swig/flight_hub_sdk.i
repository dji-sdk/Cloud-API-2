/* flight_hub_sdk.i */

%module(directors="1") flighthubsdkmodule
#ifdef SWIGJAVA
%javaconst(1);
#endif

// Handle enums
#ifdef SWIGJAVA
%include "enums.swg"
#endif
// %include "enumtypeunsafe.swg"

#ifdef SWIGJAVA
%javaconst(1);
#endif

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
#include "ground_flight_module.h"
#include "dock_control_module.h"
#include "drc_module.h"
#include "flight_settings_module.h"
#include "test_flight_module.h"
#include "onboard_ai_module.h"
#include "psdk_control_module.h"
#include "esdk_control_module.h"
#include "flight_task_media_upload_module.h"
#include "airsense_warning_module.h"
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
#include "cascade_module.h"
#include "session.h"
#include "session_manager.h"
#include <memory>

using namespace flight_hub_sdk;
%}

// Standard library support
%include "std_string.i"
%include "std_shared_ptr.i"
%include "std_vector.i"
%include "std_unordered_map.i"
%include "stdint.i"
%include "std_map.i"


#ifdef SWIGJAVA
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

#endif

// Forward declare the enums and structs before template instantiation
// This ensures SWIG knows about these types when processing templates

#ifdef SWIGJAVA
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
%ignore flight_hub_sdk::GroundLandingCommand;

// SpeedMode：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.SpeedMode，不生成 generated 枚举。
%ignore flight_hub_sdk::SpeedMode;

// FlightRiskType：仅保留 C++（value.h）；Java 使用 com.dji.flighthubsdk.value.FlightRiskType，不生成 generated 枚举。
%ignore flight_hub_sdk::FlightRiskType;
%ignore flight_hub_sdk::DockCoverMode;
%ignore flight_hub_sdk::PhotoFormat;
%ignore flight_hub_sdk::VisualExplorationStage;
%ignore flight_hub_sdk::VisualExplorationErrorCode;
%ignore flight_hub_sdk::Stage;
%ignore flight_hub_sdk::AIIdentifyMode;
%ignore flight_hub_sdk::AIIdentifyScoreMode;

#endif

%include "value.h"
%include "message.h"
%shared_ptr(flight_hub_sdk::ICascadeModule)
// Cascade 模块：屏蔽 std::function<> 版本方法，用 %extend 里的 typed Callback 版本替代
// v1.1 契约：11 个接口（addChildNode / enableChildNodes / disableChildNodes /
// deleteChildNode / getReachableChildren / getParentNode / respondToParent /
// getNodeIndex / getConnectivity / getParentConnectivity / postToNode）
%ignore flight_hub_sdk::ICascadeModule::addChildNode;
%ignore flight_hub_sdk::ICascadeModule::enableChildNodes;
%ignore flight_hub_sdk::ICascadeModule::disableChildNodes;
%ignore flight_hub_sdk::ICascadeModule::deleteChildNode;
%ignore flight_hub_sdk::ICascadeModule::getReachableChildren;
%ignore flight_hub_sdk::ICascadeModule::getParentNode;
%ignore flight_hub_sdk::ICascadeModule::respondToParent;
%ignore flight_hub_sdk::ICascadeModule::getNodeIndex;
%ignore flight_hub_sdk::ICascadeModule::getConnectivity;
%ignore flight_hub_sdk::ICascadeModule::getParentConnectivity;
%ignore flight_hub_sdk::ICascadeModule::postToNode;
// 事件推送 listen/cancel：raw std::function 重载不可直接暴露，走 *Wrapper。
%ignore flight_hub_sdk::ICascadeModule::listenNodeStatusChanged;
%ignore flight_hub_sdk::ICascadeModule::cancelListenNodeStatusChanged;
%ignore flight_hub_sdk::ICascadeModule::listenNotifyPayload;
%ignore flight_hub_sdk::ICascadeModule::cancelListenNotifyPayload;

// v1.1 snake_case 结构体成员 → Java camelCase getter/setter
%rename("nodeId")           flight_hub_sdk::CascadeNodeInfo::node_id;
%rename("parentId")         flight_hub_sdk::CascadeNodeInfo::parent_id;
%rename("sourceAddr")       flight_hub_sdk::AddChildNodeRequest::source_addr;
%rename("childAddr")        flight_hub_sdk::AddChildNodeRequest::child_addr;
%rename("nodeId")           flight_hub_sdk::AddChildNodeResult::node_id;
%rename("grantId")          flight_hub_sdk::AddChildNodeResult::grant_id;
%rename("parentId")         flight_hub_sdk::AddChildNodeResult::parent_id;
%rename("executedBy")       flight_hub_sdk::AddChildNodeResult::executed_by;
%rename("okCount")          flight_hub_sdk::CascadeBatchOpResult::ok_count;
%rename("failCount")        flight_hub_sdk::CascadeBatchOpResult::fail_count;
%rename("nodeId")           flight_hub_sdk::CascadeChildrenQuery::node_id;
%rename("nodeId")           flight_hub_sdk::CascadeParentNodeInfo::node_id;
%rename("heartbeatPeriod")  flight_hub_sdk::CascadeParentNodeInfo::heartbeat_period;

// v1.1 §8 联通性检测：snake_case → Java camelCase
%rename("nodeId")           flight_hub_sdk::CascadeConnectivityItem::node_id;

// v1.1 §7 响应上级组网请求 + §10 上级联通性检测：snake_case → Java camelCase
%rename("nodeId")           flight_hub_sdk::CascadeParentRespondRequest::node_id;
%rename("parentId")         flight_hub_sdk::CascadeParentRespondRequest::parent_id;
%rename("nodeId")           flight_hub_sdk::CascadeParentConnectivity::node_id;

// v1.1 事件推送 topic_cascade_node_status：snake_case → Java camelCase
%rename("nodeId")                flight_hub_sdk::CascadeChildNodeStatus::node_id;
%rename("nodeId")                flight_hub_sdk::CascadeParentNodeStatus::node_id;
%rename("childNodeStatusList")   flight_hub_sdk::CascadeNodeStatusEvent::child_node_status_list;
%rename("parentNodeStatus")      flight_hub_sdk::CascadeNodeStatusEvent::parent_node_status;
%rename("changedAt")             flight_hub_sdk::CascadeNodeStatusEvent::changed_at;


// Instantiate templates for each class that will be held by a shared_ptr
%shared_ptr(flight_hub_sdk::IWorkspace);
%shared_ptr(flight_hub_sdk::ISessionManager);
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
%shared_ptr(flight_hub_sdk::IAirsenseWarningModule);
%shared_ptr(flight_hub_sdk::ILiveDeviceModule);
%shared_ptr(flight_hub_sdk::ITakeoffModule);
%shared_ptr(flight_hub_sdk::IGroundFlightModule);
%shared_ptr(flight_hub_sdk::IFlightControlModule);
%shared_ptr(flight_hub_sdk::IControlAuthModule);
%shared_ptr(flight_hub_sdk::IDrcModule);
%shared_ptr(flight_hub_sdk::IFlightSettingsModule);
%shared_ptr(flight_hub_sdk::ITestFlightModule);
%shared_ptr(flight_hub_sdk::IOnboardAiModule);
%shared_ptr(flight_hub_sdk::IPsdkControlModule);
%shared_ptr(flight_hub_sdk::IEsdkControlModule);
%shared_ptr(flight_hub_sdk::IFlightTaskMediaUploadModule);
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
%shared_ptr(flight_hub_sdk::IAutoRecordModule);
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
%template(AirsenseWarningItemVector) std::vector<flight_hub_sdk::AirsenseWarningItem>;
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
%template(WaylineCollisionEventVector) std::vector<flight_hub_sdk::WaylineCollisionEvent>;
%template(RecommendedDeviceVector) std::vector<flight_hub_sdk::RecommendedDevice>;
%template(StringVector) std::vector<std::string>;
%template(CascadeNodeInfoVector) std::vector<flight_hub_sdk::CascadeNodeInfo>;
%template(CascadeChildNodeStatusVector) std::vector<flight_hub_sdk::CascadeChildNodeStatus>;
%template(CascadeConnectivityItemVector) std::vector<flight_hub_sdk::CascadeConnectivityItem>;
%template(DockStatusItemResultVector) std::vector<flight_hub_sdk::DockStatusItemResult>;
%template(DroneStatusItemResultVector) std::vector<flight_hub_sdk::DroneStatusItemResult>;
%template(DeviceAudienceCountRequestItemVector) std::vector<flight_hub_sdk::DeviceAudienceCountRequestItem>;
%template(DeviceAudienceCountItemVector) std::vector<flight_hub_sdk::DeviceAudienceCountItem>;
%template(TaskDetailVector) std::vector<flight_hub_sdk::TaskDetail>;
%template(CommandTaskListItemDataVector) std::vector<flight_hub_sdk::CommandTaskListItemData>;
%template(FlightTrackPointVector) std::vector<flight_hub_sdk::FlightTrackPoint>;
%template(FlightRiskItemVector) std::vector<flight_hub_sdk::FlightRiskItem>;
%template(TCAInfoVector) std::vector<flight_hub_sdk::TCAInfo>;
%template(TCAZoneStatusVector) std::vector<flight_hub_sdk::TCAZoneStatus>;
%template(RoamingStationSnResultVector) std::vector<flight_hub_sdk::RoamingStationSnResult>;
%template(InFlightTaskTopoItemVector) std::vector<flight_hub_sdk::InFlightTaskTopoItem>;
%template(DroneStatusBatteryItemResultVector) std::vector<flight_hub_sdk::DroneStatusBatteryItemResult>;
%template(FlightTaskExceptionDetailVector) std::vector<flight_hub_sdk::FlightTaskExceptionDetail>;
%template(TaskExceptionItemVector) std::vector<flight_hub_sdk::TaskExceptionItem>;
%template(WaypointVector) std::vector<flight_hub_sdk::Waypoint>;
%template(MultiDockHomeInfoVector) std::vector<flight_hub_sdk::MultiDockHomeInfo>;
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
%template(ErrCertMismatchVector) std::vector<flight_hub_sdk::ErrCertMismatch>;
%template(ControlAuthControllerVector) std::vector<flight_hub_sdk::ControlAuthController>;
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
%template(StringStringMap) std::map<std::string, std::string>;
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
%template(FirmwareCompatibilityValidateResultVector) std::vector<flight_hub_sdk::FirmwareCompatibilityValidateResult>;
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
%template(AutoRecordItemVector) std::vector<flight_hub_sdk::AutoRecordItem>;
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
%template(AiAlertTriggerActionVector)    std::vector<flight_hub_sdk::AiAlertTriggerAction>;
%template(AiAlertRecordItemVector)       std::vector<flight_hub_sdk::AiAlertRecordItem>;
%template(AiAlertRecordItemVectorMap)    std::unordered_map<std::string, std::vector<flight_hub_sdk::AiAlertRecordItem>>;
%template(AiAlertNotifyTargetInfoVector) std::vector<flight_hub_sdk::AiAlertNotifyTargetInfo>;
%template(AiAlertNotifyRecordItemVector) std::vector<flight_hub_sdk::AiAlertNotifyRecordItem>;
%template(AiAlertNotifyFlightDataVector) std::vector<flight_hub_sdk::AiAlertNotifyFlightData>;

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
%rename(destroy) ~flight_hub_sdk::IAirsenseWarningModule;
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
%rename(destroy) ~flight_hub_sdk::IAutoRecordModule;
%rename(destroy) ~flight_hub_sdk::IWaylineManageModule;


// Define callback interfaces for Java;
#ifndef SWIGCSHARP
#define FHSDK_CALLBACK_DIRECTOR(Type) %feature("director") Type;
#endif

FHSDK_CALLBACK_DIRECTOR(HmsDataCallback)
FHSDK_CALLBACK_DIRECTOR(AirsenseWarningCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftPayloadsOsdInfoListCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftGearCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftHomeDistanceCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftPositionStateCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftControlSourceCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftLimitsInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftBatteryWarningThresholdsCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftFirmwareStatusCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftTotalFlightInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftActivationTimeCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftMaintainStatusInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftNightLightsStateCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftObstacleAvoidanceStateCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftStorageInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftTrackIdCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftComplianceInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftPayloadsCamerasListCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftRcLostActionCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftRthInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftHomeGeographicalInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftCurrentRthModeCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceListCallback)
FHSDK_CALLBACK_DIRECTOR(GetDeviceDetailInfoCallback)
FHSDK_CALLBACK_DIRECTOR(ModifyDeviceNameCallback)
FHSDK_CALLBACK_DIRECTOR(LiveAvailableRangeCallback)
FHSDK_CALLBACK_DIRECTOR(LiveStreamCallback)
FHSDK_CALLBACK_DIRECTOR(AiSeiInfoCallback)
FHSDK_CALLBACK_DIRECTOR(ArSeiInfoCallback)
FHSDK_CALLBACK_DIRECTOR(FovSeiInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AssistantSeiInfoCallback)
FHSDK_CALLBACK_DIRECTOR(EisSeiInfoCallback)
FHSDK_CALLBACK_DIRECTOR(ResultCallback)
FHSDK_CALLBACK_DIRECTOR(ResultCallbackWithError)
FHSDK_CALLBACK_DIRECTOR(GetDeviceLiveRecordsCallback)
FHSDK_CALLBACK_DIRECTOR(LiveRecordStateCallback)
FHSDK_CALLBACK_DIRECTOR(GetAudienceCountCallback)
FHSDK_CALLBACK_DIRECTOR(GetFlightTaskListCallback)
FHSDK_CALLBACK_DIRECTOR(GetCommandTaskListCallback)
FHSDK_CALLBACK_DIRECTOR(GetFlightTaskTrackCallback)
FHSDK_CALLBACK_DIRECTOR(GetFlightRisksCallback)
FHSDK_CALLBACK_DIRECTOR(CreateFlightTaskCallback)
FHSDK_CALLBACK_DIRECTOR(GetFlightTaskDetailCallback)
FHSDK_CALLBACK_DIRECTOR(GetDeviceRecommendationCallback)
FHSDK_CALLBACK_DIRECTOR(GetDeviceTcasCallback)
FHSDK_CALLBACK_DIRECTOR(FlyToPointStartCallback)
FHSDK_CALLBACK_DIRECTOR(FlyToPointStartResultCallback)
FHSDK_CALLBACK_DIRECTOR(FlyToDroneSelfPlanPathCallback)
FHSDK_CALLBACK_DIRECTOR(FlyToPointProgressCallback)
FHSDK_CALLBACK_DIRECTOR(PoiEnterCallback)
FHSDK_CALLBACK_DIRECTOR(PoiExitCallback)
FHSDK_CALLBACK_DIRECTOR(PoiUpdateSpeedCallback)
FHSDK_CALLBACK_DIRECTOR(PoiGetTaskCallback)
FHSDK_CALLBACK_DIRECTOR(AiDataInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftBatteryInfoCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftAttitudeCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftLocationCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftModeCodeCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftWindSpeedCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftTakeoffProgressCallback)
FHSDK_CALLBACK_DIRECTOR(TakeoffResultCallback)
FHSDK_CALLBACK_DIRECTOR(GroundTakeoffResultCallback)
FHSDK_CALLBACK_DIRECTOR(CreateInFlightWaylineTaskCallback)
FHSDK_CALLBACK_DIRECTOR(GetActiveInFlightWaylineTaskCallback)
FHSDK_CALLBACK_DIRECTOR(InFlightWaylineProgressCallback)
FHSDK_CALLBACK_DIRECTOR(FlightControlAuthorityCallback)
FHSDK_CALLBACK_DIRECTOR(PayloadControlAuthorityCallback)
FHSDK_CALLBACK_DIRECTOR(CameraStateCallback)
FHSDK_CALLBACK_DIRECTOR(LiveviewWorldRegionCallback)
FHSDK_CALLBACK_DIRECTOR(CameraWideLensStateCallback)
FHSDK_CALLBACK_DIRECTOR(CameraZoomLensStateCallback)
FHSDK_CALLBACK_DIRECTOR(CameraMeasureTargetCallback)
FHSDK_CALLBACK_DIRECTOR(NightModeSettingsCallback)
FHSDK_CALLBACK_DIRECTOR(DockSubDeviceInfoListCallback)
FHSDK_CALLBACK_DIRECTOR(DockNetworkStateCallback)
FHSDK_CALLBACK_DIRECTOR(GimbalStateCallback)
FHSDK_CALLBACK_DIRECTOR(DroneOnlineStatusCallback)
FHSDK_CALLBACK_DIRECTOR(LinkStatusCallback)
FHSDK_CALLBACK_DIRECTOR(PanoramaStatusCallback)
FHSDK_CALLBACK_DIRECTOR(IntervalTakePhotoStatusCallback)
FHSDK_CALLBACK_DIRECTOR(CameraModeAvailableRangeCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkDeviceListCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkStateSummaryInfoCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkStateSpeakerInfoCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkStateSearchlightInfoCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkSpeakerPlayProgressInfoCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkFloatingWindowTextInfoCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkSpeakerResourceListCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkSpeakerResourceCallback)
FHSDK_CALLBACK_DIRECTOR(STSInfoCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkWidgetResourceListCallback)

// 红外增益模式相关回调接口
FHSDK_CALLBACK_DIRECTOR(LiveVideoInfoCallback)
FHSDK_CALLBACK_DIRECTOR(ThermalGainModeInfoCallback)
FHSDK_CALLBACK_DIRECTOR(CameraIrLensStateCallback)
FHSDK_CALLBACK_DIRECTOR(CameraIrThermometryInfoCallback)
FHSDK_CALLBACK_DIRECTOR(CameraWatermarkSettingsCallback)
FHSDK_CALLBACK_DIRECTOR(CameraStorageInfoCallback)
FHSDK_CALLBACK_DIRECTOR(CameraIrPipStateCallback)
FHSDK_CALLBACK_DIRECTOR(CameraIsothermPolygonCallback)
FHSDK_CALLBACK_DIRECTOR(CameraMediaStorageCallback)
FHSDK_CALLBACK_DIRECTOR(CameraAlgorithmDehazeInfoCallback)
FHSDK_CALLBACK_DIRECTOR(CameraPreRecordedInfoCallback)
FHSDK_CALLBACK_DIRECTOR(CameraCapabilityCallback)
FHSDK_CALLBACK_DIRECTOR(CameraSettableCapabilityCallback)
FHSDK_CALLBACK_DIRECTOR(CameraVisibleCapabilityCallback)
FHSDK_CALLBACK_DIRECTOR(CameraPhotoInfoCallback)

// 飞行任务异常和设备变更相关回调接口
FHSDK_CALLBACK_DIRECTOR(FlightTaskExceptionCallback)
FHSDK_CALLBACK_DIRECTOR(WorkspaceDeviceChangeCallback)
FHSDK_CALLBACK_DIRECTOR(ImportWaylineEventCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceTopoChangeCallback)

FHSDK_CALLBACK_DIRECTOR(AircraftControlCodeCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftSpeedCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftPayloadsCameraLiveviewWorldRegionCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftPayloadsGimbalAttitudeCallback)
FHSDK_CALLBACK_DIRECTOR(AircraftPayloadsListCallback)
FHSDK_CALLBACK_DIRECTOR(ControlRightsCloudControlStatusCallback)
FHSDK_CALLBACK_DIRECTOR(ControlRightsDeviceControlStatusCallback)
FHSDK_CALLBACK_DIRECTOR(FlightTasksMediaFileDisplayAvailableCallback)
FHSDK_CALLBACK_DIRECTOR(CommandTaskEventCallback)
FHSDK_CALLBACK_DIRECTOR(FlyToTaskEventCallback)
FHSDK_CALLBACK_DIRECTOR(PoiTaskEventCallback)
FHSDK_CALLBACK_DIRECTOR(WaylineTaskEventCallback)
FHSDK_CALLBACK_DIRECTOR(ReturnHomeInfoEventCallback)
FHSDK_CALLBACK_DIRECTOR(TaskExceptionEventCallback)
FHSDK_CALLBACK_DIRECTOR(DockLocationCallback)
FHSDK_CALLBACK_DIRECTOR(DroneInDockCallback)
FHSDK_CALLBACK_DIRECTOR(DockCoverStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockMediaFileDetailCallback)
FHSDK_CALLBACK_DIRECTOR(DockWirelessLinkCallback)
FHSDK_CALLBACK_DIRECTOR(DockJobNumberCallback)
FHSDK_CALLBACK_DIRECTOR(FlightTaskStepCodeCallback)
FHSDK_CALLBACK_DIRECTOR(DockPowerStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockStorageStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockFirstPowerOnTimeCallback)
FHSDK_CALLBACK_DIRECTOR(DockAccTimeCallback)
FHSDK_CALLBACK_DIRECTOR(DockModeCodeCallback)
FHSDK_CALLBACK_DIRECTOR(DockDroneChargeStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockUserExperienceImprovementCallback)
FHSDK_CALLBACK_DIRECTOR(DockDrcStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockPutterStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockAlternateLandPointCallback)
FHSDK_CALLBACK_DIRECTOR(DockPositionStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockActivationTimeCallback)
FHSDK_CALLBACK_DIRECTOR(DockBatteryStoreModeCallback)
FHSDK_CALLBACK_DIRECTOR(DockAlarmStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockDroneBatteryMaintenanceInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DockEmergencyStopStateCallback)
FHSDK_CALLBACK_DIRECTOR(DockSilentModeCallback)
FHSDK_CALLBACK_DIRECTOR(DockRelativeAlternateLandPointCallback)
FHSDK_CALLBACK_DIRECTOR(DockSelfConvergeCoordinateCallback)
FHSDK_CALLBACK_DIRECTOR(DockFirmwareInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DockMeteorologicalInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DockBackupBatteryInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DockMaintainStatusInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DockOnlineStatusCallback)
FHSDK_CALLBACK_DIRECTOR(DockAirConditionerStatusInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DockSupplementLightStateCallback)
FHSDK_CALLBACK_DIRECTOR(PullStreamStartCallback)
FHSDK_CALLBACK_DIRECTOR(PullStreamDataCallback)
FHSDK_CALLBACK_DIRECTOR(PullStreamErrorCallback)
FHSDK_CALLBACK_DIRECTOR(StartNetworkTestCallback)
FHSDK_CALLBACK_DIRECTOR(StopNetworkTestCallback)
FHSDK_CALLBACK_DIRECTOR(GetNetworkTestProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeNetworkTestProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeCalibrationProgressCallback)
FHSDK_CALLBACK_DIRECTOR(RtkCalibrationCallback)
FHSDK_CALLBACK_DIRECTOR(SetDistanceLimitCallback)
FHSDK_CALLBACK_DIRECTOR(SetHeightLimitCallback)
FHSDK_CALLBACK_DIRECTOR(SetObstacleAvoidanceCallback)
FHSDK_CALLBACK_DIRECTOR(SetRemainingPowerForRthCallback)
FHSDK_CALLBACK_DIRECTOR(SetNightLightsStateCallback)
FHSDK_CALLBACK_DIRECTOR(SwitchAlarmStateCallback)
FHSDK_CALLBACK_DIRECTOR(EsimOperatorSwitchCallback)
FHSDK_CALLBACK_DIRECTOR(SimSlotSwitchCallback)
FHSDK_CALLBACK_DIRECTOR(SetBatteryStoreModeCallback)
FHSDK_CALLBACK_DIRECTOR(ManualBatterySwapCallback)
FHSDK_CALLBACK_DIRECTOR(ManualBatterySwapProgressCallback)
FHSDK_CALLBACK_DIRECTOR(DonglePowerSwitchCallback)
FHSDK_CALLBACK_DIRECTOR(LowPowerSwitchCallback)
FHSDK_CALLBACK_DIRECTOR(EsimOperatorSwitchProgressCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceCloudEnvironmentSwitchProgressCallback)
// 中继站调试接口回调
FHSDK_CALLBACK_DIRECTOR(SetRelayBroadcastHideCallback)
// Geo Caging 回调
FHSDK_CALLBACK_DIRECTOR(SetGeoCagingEnableCallback)
// Cascade 模块回调（v1.1，11 个）
FHSDK_CALLBACK_DIRECTOR(CascadeAddChildNodeCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeEnableChildNodesCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeDisableChildNodesCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeDeleteChildNodeCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeGetReachableChildrenCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeGetParentNodeCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeRespondToParentCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeGetNodeIndexCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeGetConnectivityCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeGetParentConnectivityCallback)
FHSDK_CALLBACK_DIRECTOR(CascadePostToNodeCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeNodeStatusEventCallback)
FHSDK_CALLBACK_DIRECTOR(CascadeNotifyEventCallback)
FHSDK_CALLBACK_DIRECTOR(SetRelaySSIDCallback)
FHSDK_CALLBACK_DIRECTOR(SetRelayPasswordCallback)
FHSDK_CALLBACK_DIRECTOR(SetRelaySearchModeCallback)
FHSDK_CALLBACK_DIRECTOR(SetRelayOccupiedStatusCallback)
FHSDK_CALLBACK_DIRECTOR(RelayRebootCallback)
FHSDK_CALLBACK_DIRECTOR(RelayDisconnectCallback)
FHSDK_CALLBACK_DIRECTOR(QueryHistoryHMSDataCallback)
FHSDK_CALLBACK_DIRECTOR(QueryDeviceLogListCallback)
FHSDK_CALLBACK_DIRECTOR(CreateDeviceLogReportCallback)
FHSDK_CALLBACK_DIRECTOR(LogReportListCallback)
FHSDK_CALLBACK_DIRECTOR(LogReportOperationCallback)
FHSDK_CALLBACK_DIRECTOR(UploadLogAppendFileCallback)

// 固件管理相关回调接口
FHSDK_CALLBACK_DIRECTOR(GetLatestFirmwareReleaseNoteCallback)
FHSDK_CALLBACK_DIRECTOR(InvokeFirmwareUpgradeCallback)
FHSDK_CALLBACK_DIRECTOR(QueryFirmwareUpgradeProgressCallback)
FHSDK_CALLBACK_DIRECTOR(UploadFirmwareFileCallback)
FHSDK_CALLBACK_DIRECTOR(ReleaseFirmwareCallback)
FHSDK_CALLBACK_DIRECTOR(UnReleaseFirmwareCallback)
FHSDK_CALLBACK_DIRECTOR(DeleteFirmwareCallback)
FHSDK_CALLBACK_DIRECTOR(QueryFirmwareListCallback)
FHSDK_CALLBACK_DIRECTOR(QueryFirmwareCompatibilityConfigCallback)
FHSDK_CALLBACK_DIRECTOR(CheckFirmwareStatusCallback)
FHSDK_CALLBACK_DIRECTOR(SubmitFirmwareInfoCallback)

// 解锁证书管理相关回调接口
FHSDK_CALLBACK_DIRECTOR(GetUnlockLicenseListCallback)
FHSDK_CALLBACK_DIRECTOR(SwitchUnlockLicenseCallback)
FHSDK_CALLBACK_DIRECTOR(CheckUnlockLicenseCallback)
FHSDK_CALLBACK_DIRECTOR(SubmitUnlockLicenseCallback)
FHSDK_CALLBACK_DIRECTOR(ClearDeviceUnlockLicenseCallback)
FHSDK_CALLBACK_DIRECTOR(UploadUnlockLicenseFileCallback)

// 航线管理相关回调接口
FHSDK_CALLBACK_DIRECTOR(GetWaylineListCallback)
FHSDK_CALLBACK_DIRECTOR(GetWaylineDetailCallback)
FHSDK_CALLBACK_DIRECTOR(BatchDeleteWaylinesCallback)
FHSDK_CALLBACK_DIRECTOR(UpdateWaylineCallback)
FHSDK_CALLBACK_DIRECTOR(SubmitWaylineInfoCallback)
FHSDK_CALLBACK_DIRECTOR(BatchDownloadWaylinesCallback)
FHSDK_CALLBACK_DIRECTOR(DuplicateWaylineCallback)
FHSDK_CALLBACK_DIRECTOR(CopyWaylineCallback)
FHSDK_CALLBACK_DIRECTOR(ConcatWaylineCallback)
FHSDK_CALLBACK_DIRECTOR(ImportKmzCallback)
// AI Inside 机载算法管理相关回调接口
FHSDK_CALLBACK_DIRECTOR(GetAIInsideModelsCallback)
FHSDK_CALLBACK_DIRECTOR(GetAIInsideModelDevicesCallback)
FHSDK_CALLBACK_DIRECTOR(GetAIInsideModelDevicesSummaryCallback)
FHSDK_CALLBACK_DIRECTOR(OperateAIInsideModelCallback)
FHSDK_CALLBACK_DIRECTOR(CreateAIInsideAlgorithmCallback)
FHSDK_CALLBACK_DIRECTOR(FileUploadAIInsideAlgorithmCallback)
FHSDK_CALLBACK_DIRECTOR(DeleteAIInsideAlgorithmCallback)
FHSDK_CALLBACK_DIRECTOR(UploadAlgorithmFileCallback)
// AI 告警管理相关回调接口
FHSDK_CALLBACK_DIRECTOR(PostAiAlertDeviceSettingCallback)
FHSDK_CALLBACK_DIRECTOR(GetAiAlertDeviceSettingCallback)
FHSDK_CALLBACK_DIRECTOR(PostAiAlertAlgorithmSettingCallback)
FHSDK_CALLBACK_DIRECTOR(GetAiAlertAlgorithmSettingCallback)
FHSDK_CALLBACK_DIRECTOR(GetAiAlertFlightsCallback)
FHSDK_CALLBACK_DIRECTOR(GetAiAlertRecordsCallback)
FHSDK_CALLBACK_DIRECTOR(AiAlertRecordsNotifyCallback)

// 直播转发回调
FHSDK_CALLBACK_DIRECTOR(CreateLiveSyncCallback)
FHSDK_CALLBACK_DIRECTOR(DeleteLiveSyncCallback)
FHSDK_CALLBACK_DIRECTOR(UpdateLiveSyncCallback)
FHSDK_CALLBACK_DIRECTOR(GetLiveSyncListCallback)
FHSDK_CALLBACK_DIRECTOR(GetLiveSyncDevicesV2Callback)
FHSDK_CALLBACK_DIRECTOR(GetLiveSyncSummaryCallback)
// 媒体服务模块相关回调接口
FHSDK_CALLBACK_DIRECTOR(GetMediaFilesListCallback)
FHSDK_CALLBACK_DIRECTOR(GetMediaFilesDownloadUrlCallback)
FHSDK_CALLBACK_DIRECTOR(GetMediaFilesDownloadUrlListCallback)
FHSDK_CALLBACK_DIRECTOR(MediaFilePostProcessCallback)
FHSDK_CALLBACK_DIRECTOR(MediaFileDeviceUploadCallback)
FHSDK_CALLBACK_DIRECTOR(GetMediaUploadStsCallback)
FHSDK_CALLBACK_DIRECTOR(MediaUploadCallbackCallback)
FHSDK_CALLBACK_DIRECTOR(UploadGeoMediaFileCallback)
FHSDK_CALLBACK_DIRECTOR(GetMediaFileDetailByIdCallback)
// 3X 超清全景拼接回调
FHSDK_CALLBACK_DIRECTOR(PanoStitchActionCallback)
FHSDK_CALLBACK_DIRECTOR(GetPanoTierTileUrlCallback)
FHSDK_CALLBACK_DIRECTOR(PanoStitchProgressCallback)
FHSDK_CALLBACK_DIRECTOR(GetWaylineEditorStsCallback)
// Terra Build 模块相关回调接口
FHSDK_CALLBACK_DIRECTOR(TerraBuildReconstructionSettingsCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildQualityReportCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildModelFileListCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildModelFileUrlCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildCreateReconstructionCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildSfmResultCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildCreatePanoramaCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildReconstructionImagesCallback)
FHSDK_CALLBACK_DIRECTOR(TerraBuildReconstructionStatusChangedCallback)
// Element 模块回调
FHSDK_CALLBACK_DIRECTOR(ElementCreateCallback)
FHSDK_CALLBACK_DIRECTOR(ElementUpdateCallback)
FHSDK_CALLBACK_DIRECTOR(ElementDeleteCallback)
FHSDK_CALLBACK_DIRECTOR(ElementCreateElementCallback)
FHSDK_CALLBACK_DIRECTOR(ElementUpdateElementCallback)
FHSDK_CALLBACK_DIRECTOR(ElementListHierarchyCallback)
FHSDK_CALLBACK_DIRECTOR(ElementSetVisibilityCallback)
FHSDK_CALLBACK_DIRECTOR(ElementDeleteElementCallback)
FHSDK_CALLBACK_DIRECTOR(TerraUploadCallbackCallback)
FHSDK_CALLBACK_DIRECTOR(TerraShowOnMapCallback)
FHSDK_CALLBACK_DIRECTOR(TerraDeleteModelFilesCallback)
FHSDK_CALLBACK_DIRECTOR(TerraUnzipProgressCallback)
FHSDK_CALLBACK_DIRECTOR(TerraModelStatusChangedCallback)
FHSDK_CALLBACK_DIRECTOR(TerraFileShowOnMapCallback)
FHSDK_CALLBACK_DIRECTOR(TerraMediaFileHideOnMapCallback)
FHSDK_CALLBACK_DIRECTOR(TerraMediaFileShowOnMapCallback)
FHSDK_CALLBACK_DIRECTOR(TerraGetModelDetailCallback)
FHSDK_CALLBACK_DIRECTOR(TerraSetLasWktCallback)

// 安全服务模块相关回调接口
FHSDK_CALLBACK_DIRECTOR(SearchSignalMapsCallback)
FHSDK_CALLBACK_DIRECTOR(CreateCustomizedFlightAreasCallback)
FHSDK_CALLBACK_DIRECTOR(GetCustomizedFlightAreasCallback)
FHSDK_CALLBACK_DIRECTOR(UpdateCustomizedFlightAreasCallback)
FHSDK_CALLBACK_DIRECTOR(SyncCustomizedFlightAreasCallback)
FHSDK_CALLBACK_DIRECTOR(GetFlightAreasSyncStatusCallback)
FHSDK_CALLBACK_DIRECTOR(GetFlightAreasUrlCallback)
FHSDK_CALLBACK_DIRECTOR(GetOfflineMapsModelInfoCallback)
FHSDK_CALLBACK_DIRECTOR(SyncOfflineMapsCallback)
FHSDK_CALLBACK_DIRECTOR(GetOfflineMapsSyncStatusCallback)

// 飞行任务记录模块相关回调接口
FHSDK_CALLBACK_DIRECTOR(ExportFlightRecordsCallback)
FHSDK_CALLBACK_DIRECTOR(GetExportFlightRecordsProgressCallback)

// 飞行器 HSI 信息（障碍物感知/避障传感器数据）
FHSDK_CALLBACK_DIRECTOR(HsiInfoCallback)

// 新增订阅进度回调接口
FHSDK_CALLBACK_DIRECTOR(DeviceOtaProgressCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceFileUploadProgressCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkSpeakerAudioPlayStartProgressCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkSpeakerTtsPlayStartProgressCallback)
FHSDK_CALLBACK_DIRECTOR(PsdkCustomDataTransmissionFromPsdkCallback)
FHSDK_CALLBACK_DIRECTOR(EsdkCustomDataTransmissionFromEsdkCallback)
FHSDK_CALLBACK_DIRECTOR(HighestPriorityUploadFlighttaskMediaCallback)
FHSDK_CALLBACK_DIRECTOR(FlightPoiProgressCallback)

// 飞行安全设置回调
FHSDK_CALLBACK_DIRECTOR(SetFlightSmartPlanModeCallback)
FHSDK_CALLBACK_DIRECTOR(SetOutOfControlActionCallback)
FHSDK_CALLBACK_DIRECTOR(SetFlightTaskHeightCallback)
FHSDK_CALLBACK_DIRECTOR(SetRthAltitudeCallback)
FHSDK_CALLBACK_DIRECTOR(SetAirTransferEnableCallback)
FHSDK_CALLBACK_DIRECTOR(GenerateTryFlightWaylineCallback)
FHSDK_CALLBACK_DIRECTOR(CreateTestFlightTaskCallback)

// 报修管理模块相关回调接口
FHSDK_CALLBACK_DIRECTOR(GetRepairConfigsCallback)
FHSDK_CALLBACK_DIRECTOR(CreateMaintenanceOrderCallback)
FHSDK_CALLBACK_DIRECTOR(GetMaintenanceOrderDetailCallback)
FHSDK_CALLBACK_DIRECTOR(GetMaintenanceOrdersListCallback)

// 诊断管理模块相关回调接口
FHSDK_CALLBACK_DIRECTOR(CreateSelfDiagnosisCallback)
FHSDK_CALLBACK_DIRECTOR(OperateSelfDiagnosisCallback)
FHSDK_CALLBACK_DIRECTOR(GetSelfDiagnosisReportCallback)
FHSDK_CALLBACK_DIRECTOR(GetSelfDiagnosisReportListCallback)

// AI Box 管理相关回调接口
FHSDK_CALLBACK_DIRECTOR(FetchAIBoxInfoCallback)
FHSDK_CALLBACK_DIRECTOR(FetchFileDownloadUrlCallback)
FHSDK_CALLBACK_DIRECTOR(FetchAppListCallback)
FHSDK_CALLBACK_DIRECTOR(FetchAppListProgressCallback)
FHSDK_CALLBACK_DIRECTOR(ExportFileCallback)
FHSDK_CALLBACK_DIRECTOR(ExportFileProgressCallback)
FHSDK_CALLBACK_DIRECTOR(UploadAIBoxFileCallback)
FHSDK_CALLBACK_DIRECTOR(InstallAppCallback)
FHSDK_CALLBACK_DIRECTOR(InstallAppProgressCallback)
FHSDK_CALLBACK_DIRECTOR(OperateAppCallback)

// DRC northbound push listener callbacks
FHSDK_CALLBACK_DIRECTOR(DrcOsdInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DrcBatteriesInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DrcParachuteInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DrcLandingMissionStateCallback)
FHSDK_CALLBACK_DIRECTOR(FlashLedStateInfoCallback)
FHSDK_CALLBACK_DIRECTOR(FlashLedConfigListCallback)
FHSDK_CALLBACK_DIRECTOR(FlashLedConfigCallback)
FHSDK_CALLBACK_DIRECTOR(AutoRecordConfigCallback)
FHSDK_CALLBACK_DIRECTOR(AiAlarmStateCallback)
FHSDK_CALLBACK_DIRECTOR(DrcDelayInfoCallback)
FHSDK_CALLBACK_DIRECTOR(DrcCountDownInfoCallback)

// EA340 消息推送补全（Phase D）新增回调接口;
FHSDK_CALLBACK_DIRECTOR(DockLiveStatusCallback)
FHSDK_CALLBACK_DIRECTOR(DockLiveCapacityCallback)
FHSDK_CALLBACK_DIRECTOR(DockDongleInfosCallback)
FHSDK_CALLBACK_DIRECTOR(DockAirConditionerModeCallback)
FHSDK_CALLBACK_DIRECTOR(DroneModeCodeReasonCallback)
FHSDK_CALLBACK_DIRECTOR(DroneDongleInfosCallback)
FHSDK_CALLBACK_DIRECTOR(DroneFlysafeDatabaseVersionCallback)
FHSDK_CALLBACK_DIRECTOR(DroneWpmzVersionCallback)
FHSDK_CALLBACK_DIRECTOR(DroneCommanderFlightModeCallback)
FHSDK_CALLBACK_DIRECTOR(DroneCurrentCommanderFlightModeCallback)
FHSDK_CALLBACK_DIRECTOR(DroneCommanderModeLostActionCallback)
FHSDK_CALLBACK_DIRECTOR(DroneOfflineMapEnableCallback)
FHSDK_CALLBACK_DIRECTOR(DronePsdkUiResourceCallback)
FHSDK_CALLBACK_DIRECTOR(DroneCameraWatermarkSettingsPushCallback)

#ifndef SWIGCSHARP
#define FHSDK_CALLBACK_PARAMETER(Type) Type* callback
#define FHSDK_SHARE_CALLBACK(Type) std::shared_ptr<Type> cb(callback)
#define FHSDK_DELETE_CALLBACK() delete callback
#endif

%inline %{
    // 前向声明：TerraModelDetailJavaResult 定义在 terra_build_swig 命名空间的另一个 inline 块中
    namespace terra_build_swig { struct TerraModelDetailJavaResult; }

    // Java callback interfaces
    class HmsDataCallback {
    public:
        virtual ~HmsDataCallback() {}
        virtual void onHmsData(const flight_hub_sdk::HmsData& hms_data) = 0;
    };

    class AirsenseWarningCallback {
    public:
        virtual ~AirsenseWarningCallback() {}
        virtual void onAirsenseWarning(const flight_hub_sdk::AirsenseWarning& msg) = 0;
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

    class ControlAuthResultCallback {
    public:
        virtual ~ControlAuthResultCallback() {}
        virtual void onResult(const flight_hub_sdk::FlightHubError& err, const flight_hub_sdk::ControlAuthResult& result) = 0;
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

    class FlyToPointStartResultCallback {
    public:
        virtual ~FlyToPointStartResultCallback() {}
        virtual void onFlyToPointStartResult(flight_hub_sdk::FlightHubError err,
                                             const flight_hub_sdk::FlyToPointStartResult& result) = 0;
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

    class GroundTakeoffResultCallback {
    public:
        virtual ~GroundTakeoffResultCallback() {}
        virtual void onResult(const flight_hub_sdk::FlightHubError& err, const flight_hub_sdk::GroundTakeoffResult& result) = 0;
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
    class GetFlightRisksCallback {
    public:
        virtual ~GetFlightRisksCallback() {}
        virtual void onGetFlightRisks(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightRisksResults& results) = 0;
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

    class ReturnHomeInfoEventCallback {
    public:
        virtual ~ReturnHomeInfoEventCallback() {}
        virtual void onReturnHomeInfoEvent(const flight_hub_sdk::ReturnHomeInfoEvent& event) = 0;
    };

    class TaskExceptionEventCallback {
    public:
        virtual ~TaskExceptionEventCallback() {}
        virtual void onTaskExceptionEvent(const flight_hub_sdk::TaskExceptionEvent& event) = 0;
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

    // 3X 超清全景拼接回调类（PaaS pano-stitch）
    class PanoStitchActionCallback {
    public:
        virtual ~PanoStitchActionCallback() {}
        virtual void onPanoStitchAction(flight_hub_sdk::FlightHubError err) = 0;
    };

    class GetPanoTierTileUrlCallback {
    public:
        virtual ~GetPanoTierTileUrlCallback() {}
        virtual void onGetPanoTierTileUrl(flight_hub_sdk::FlightHubError err,
                                         const flight_hub_sdk::GetPanoTierTileUrlResult& result) = 0;
    };

    class PanoStitchProgressCallback {
    public:
        virtual ~PanoStitchProgressCallback() {}
        virtual void onPanoStitchProgress(const flight_hub_sdk::PanoStitchProgressMsg& msg) = 0;
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

    class TerraDeleteModelFilesCallback {
    public:
        virtual ~TerraDeleteModelFilesCallback() {}
        virtual void onTerraDeleteModelFiles(flight_hub_sdk::FlightHubError err,
                                             const flight_hub_sdk::TerraDeleteModelFilesResult& result) = 0;
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

    class GetFlightAreasUrlCallback {
    public:
        virtual ~GetFlightAreasUrlCallback() {}
        virtual void onGetFlightAreasUrl(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightAreasUrlResults& result) = 0;
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

    class AiAlertRecordsNotifyCallback {
    public:
        virtual ~AiAlertRecordsNotifyCallback() {}
        virtual void onAiAlertRecordsNotify(flight_hub_sdk::AiAlertRecordsNotifyResult result) = 0;
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

    class PsdkCustomDataTransmissionFromPsdkCallback {
    public:
        virtual ~PsdkCustomDataTransmissionFromPsdkCallback() {}
        virtual void onPsdkCustomDataTransmissionFromPsdk(const flight_hub_sdk::PsdkCustomDataTransmission& msg) = 0;
    };

    class EsdkCustomDataTransmissionFromEsdkCallback {
    public:
        virtual ~EsdkCustomDataTransmissionFromEsdkCallback() {}
        virtual void onEsdkCustomDataTransmissionFromEsdk(const flight_hub_sdk::EsdkCustomDataTransmission& msg) = 0;
    };

    class HighestPriorityUploadFlighttaskMediaCallback {
    public:
        virtual ~HighestPriorityUploadFlighttaskMediaCallback() {}
        virtual void onHighestPriorityUploadFlighttaskMedia(const flight_hub_sdk::HighestPriorityUploadFlighttaskMedia& msg) = 0;
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

    // 注: 回调参数 DrcLandingMissionState 的舱盖百分比字段, 协议侧拼作 cover_precent
    //     (协议设计如此, 非笔误), 对外 API 已更正为 cover_percent, 故 SWIG 生成的 getter
    //     为 getCover_percent()。转换在 FlightCtrlMgr::onReceiveLandingMissionState 完成。
    //     landing_state 枚举: 0=初始空闲 1=等待参数同步 2=航向对齐 3=精准降落(有水平源)
    //     4=无源降落(无水平源, 继续下降) 5=悬停微调(有水平源) 6=纯悬停(无水平源)
    //     7=备降(有源) 8=备降(无源) 9=备降(用户 RC 接管)
    class DrcLandingMissionStateCallback {
    public:
        virtual ~DrcLandingMissionStateCallback() {}
        virtual void onDrcLandingMissionState(const flight_hub_sdk::DrcLandingMissionState& info) = 0;
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

    class AutoRecordConfigCallback {
    public:
        virtual ~AutoRecordConfigCallback() {}
        virtual void onAutoRecordConfig(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::AutoRecordConfig& config) = 0;
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

    // ===== Cascade 模块 typed Callback（v1.1，9 个）=====
    class CascadeAddChildNodeCallback {
    public:
        virtual ~CascadeAddChildNodeCallback() {}
        virtual void onCascadeAddChildNode(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::AddChildNodeResult& result) = 0;
    };

    class CascadeEnableChildNodesCallback {
    public:
        virtual ~CascadeEnableChildNodesCallback() {}
        virtual void onCascadeEnableChildNodes(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeBatchOpResult& result) = 0;
    };

    class CascadeDisableChildNodesCallback {
    public:
        virtual ~CascadeDisableChildNodesCallback() {}
        virtual void onCascadeDisableChildNodes(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeBatchOpResult& result) = 0;
    };

    class CascadeDeleteChildNodeCallback {
    public:
        virtual ~CascadeDeleteChildNodeCallback() {}
        virtual void onCascadeDeleteChildNode(flight_hub_sdk::FlightHubError err) = 0;
    };

    class CascadeGetReachableChildrenCallback {
    public:
        virtual ~CascadeGetReachableChildrenCallback() {}
        virtual void onCascadeGetReachableChildren(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeChildNodeListResult& result) = 0;
    };

    class CascadeGetParentNodeCallback {
    public:
        virtual ~CascadeGetParentNodeCallback() {}
        virtual void onCascadeGetParentNode(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeParentNodeInfo& info) = 0;
    };

    // v1.1 §7 响应上级组网请求：只回调 err
    class CascadeRespondToParentCallback {
    public:
        virtual ~CascadeRespondToParentCallback() {}
        virtual void onCascadeRespondToParent(flight_hub_sdk::FlightHubError err) = 0;
    };

    class CascadeGetNodeIndexCallback {
    public:
        virtual ~CascadeGetNodeIndexCallback() {}
        virtual void onCascadeGetNodeIndex(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeChildNodeListResult& result) = 0;
    };

    class CascadeGetConnectivityCallback {
    public:
        virtual ~CascadeGetConnectivityCallback() {}
        virtual void onCascadeGetConnectivity(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeConnectivityResult& result) = 0;
    };

    // v1.1 §10 上级平台联通性检测：单条结果
    class CascadeGetParentConnectivityCallback {
    public:
        virtual ~CascadeGetParentConnectivityCallback() {}
        virtual void onCascadeGetParentConnectivity(flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeParentConnectivity& result) = 0;
    };

    class CascadePostToNodeCallback {
    public:
        virtual ~CascadePostToNodeCallback() {}
        virtual void onCascadePostToNode(flight_hub_sdk::FlightHubError err) = 0;
    };

    // ===== Cascade 事件推送 typed Callback（topic_cascade_node_status）=====
    class CascadeNodeStatusEventCallback {
    public:
        virtual ~CascadeNodeStatusEventCallback() {}
        virtual void onCascadeNodeStatusEvent(const flight_hub_sdk::CascadeNodeStatusEvent& event) = 0;
    };

    // ===== Cascade 事件推送 typed Callback（topic_cascade_notify）=====
    class CascadeNotifyEventCallback {
    public:
        virtual ~CascadeNotifyEventCallback() {}
        virtual void onCascadeNotifyEvent(const flight_hub_sdk::CascadeNotifyEvent& event) = 0;
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
%ignore flight_hub_sdk::IPsdkControlModule::listenPsdkCustomDataTransmissionFromPsdk;
%ignore flight_hub_sdk::IPsdkControlModule::cancelListenPsdkCustomDataTransmissionFromPsdk;
%ignore flight_hub_sdk::IPsdkControlModule::sendPsdkCustomDataTransmissionToPsdk;
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

// ESDK相关接口忽略
%ignore flight_hub_sdk::IEsdkControlModule::listenEsdkCustomDataTransmissionFromEsdk;
%ignore flight_hub_sdk::IEsdkControlModule::cancelListenEsdkCustomDataTransmissionFromEsdk;
%ignore flight_hub_sdk::IEsdkControlModule::sendEsdkCustomDataTransmissionToEsdk;
%ignore flight_hub_sdk::IFlightTaskMediaUploadModule::uploadFlighttaskMediaPrioritize;
%ignore flight_hub_sdk::IFlightTaskMediaUploadModule::listenHighestPriorityUploadFlighttaskMedia;
%ignore flight_hub_sdk::IFlightTaskMediaUploadModule::cancelListenHighestPriorityUploadFlighttaskMedia;



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
%ignore flight_hub_sdk::IFlyToPointModule::startWithResult;
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
%ignore flight_hub_sdk::IFlightTaskControlModule::listenReturnHomeInfoEvent;
%ignore flight_hub_sdk::IFlightTaskControlModule::cancelListenReturnHomeInfoEvent;
%ignore flight_hub_sdk::IFlightTaskControlModule::listenReturnHomeInfoEventByDevice;
%ignore flight_hub_sdk::IFlightTaskControlModule::cancelListenReturnHomeInfoEventByDevice;
%ignore flight_hub_sdk::IFlightTaskControlModule::listenTaskExceptionEvent;
%ignore flight_hub_sdk::IFlightTaskControlModule::cancelListenTaskExceptionEvent;


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
%ignore flight_hub_sdk::IAirsenseWarningModule::listenAirsenseWarning;
%ignore flight_hub_sdk::IAirsenseWarningModule::cancelListenAirsenseWarning;
%ignore flight_hub_sdk::ILiveDeviceModule::listenLiveAvailableRange;
%ignore flight_hub_sdk::ILiveDeviceModule::cancelListenLiveAvailableRange;
%ignore flight_hub_sdk::ILiveDeviceModule::getAudienceCount;
// EA340 消息推送补全（Phase D）;
%ignore flight_hub_sdk::ILiveDeviceModule::listenDockLiveStatus;
%ignore flight_hub_sdk::ILiveDeviceModule::cancelListenDockLiveStatus;
%ignore flight_hub_sdk::ILiveDeviceModule::listenDockLiveCapacity;
%ignore flight_hub_sdk::ILiveDeviceModule::cancelListenDockLiveCapacity;
%ignore flight_hub_sdk::ITakeoffModule::takeoff;
%ignore flight_hub_sdk::IGroundFlightModule::groundTakeoff;
%ignore flight_hub_sdk::IGroundFlightModule::groundLanding;
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
%ignore flight_hub_sdk::IFlightControlModule::drcSetFlyingMaxSpeed;
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
%ignore flight_hub_sdk::IFlightControlModule::listenDrcLandingMissionState;
%ignore flight_hub_sdk::IFlightControlModule::cancelListenDrcLandingMissionState;

%ignore flight_hub_sdk::IControlAuthModule::acquireFlightControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::releaseFlightControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::acquireFlightControlAuthorityWithResult;
%ignore flight_hub_sdk::IControlAuthModule::releaseFlightControlAuthorityWithResult;
%ignore flight_hub_sdk::IControlAuthModule::listenFlightControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::acquirePayloadControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::releasePayloadControlAuthority;
%ignore flight_hub_sdk::IControlAuthModule::acquirePayloadControlAuthorityWithResult;
%ignore flight_hub_sdk::IControlAuthModule::releasePayloadControlAuthorityWithResult;
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
%ignore flight_hub_sdk::IPsdkResourceModule::getPsdkSpeakerResourceListWithFilter;
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

// 忽略IAutoRecordModule的原始方法
%ignore flight_hub_sdk::IAutoRecordModule::getAutoRecordConfig;
%ignore flight_hub_sdk::IAutoRecordModule::saveAutoRecordConfig;

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
%ignore flight_hub_sdk::IAIAlertModule::listenAiAlertRecords;
%ignore flight_hub_sdk::IAIAlertModule::cancelListenAiAlertRecords;
%ignore flight_hub_sdk::IAIAlertModule::deleteAiAlertRecord;

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
%ignore flight_hub_sdk::IMediaServiceModule::startPanoStitch;
%ignore flight_hub_sdk::IMediaServiceModule::stopPanoStitch;
%ignore flight_hub_sdk::IMediaServiceModule::retryPanoStitch;
%ignore flight_hub_sdk::IMediaServiceModule::startPanoTier;
%ignore flight_hub_sdk::IMediaServiceModule::getPanoTierTileUrl;
%ignore flight_hub_sdk::IMediaServiceModule::subscribePanoStitchProgress;
%ignore flight_hub_sdk::IMediaServiceModule::cancelListenPanoStitchProgress;

// 忽略ISafetyServiceModule的原始方法
%ignore flight_hub_sdk::ISafetyServiceModule::searchSignalMaps;
%ignore flight_hub_sdk::ISafetyServiceModule::createCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::getCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::updateCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::deleteCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::syncCustomizedFlightAreas;
%ignore flight_hub_sdk::ISafetyServiceModule::getFlightAreasSyncStatus;
%ignore flight_hub_sdk::ISafetyServiceModule::getFlightAreasUrl;
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
    void fetchAIBoxInfoWrapper(std::string const &device_sn,FHSDK_CALLBACK_PARAMETER(FetchAIBoxInfoCallback)) {
        $self->fetchAIBoxInfo(device_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::AIBoxInfoListResults& results) {
            if (callback) { callback->onFetchAIBoxInfo(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void fetchFileDownloadUrlWrapper(flight_hub_sdk::FetchFileDownloadUrlParams const &params,FHSDK_CALLBACK_PARAMETER(FetchFileDownloadUrlCallback)) {
        $self->fetchFileDownloadUrl(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FileDownloadUrlListResults& results) {
            if (callback) { callback->onFetchFileDownloadUrl(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void fetchAppListWrapper(flight_hub_sdk::FetchAppListParam const &param,FHSDK_CALLBACK_PARAMETER(FetchAppListCallback)) {
        $self->fetchAppList(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FetchAppListResults& results) {
            if (callback) { callback->onFetchAppList(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void fetchAppListProgressWrapper(flight_hub_sdk::FetchAppListProgressParam const &param,FHSDK_CALLBACK_PARAMETER(FetchAppListProgressCallback)) {
        $self->fetchAppListProgress(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FetchAppListProgressResults& results) {
            if (callback) { callback->onFetchAppListProgress(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void exportFileWrapper(flight_hub_sdk::ExportFileParam const &param,FHSDK_CALLBACK_PARAMETER(ExportFileCallback)) {
        $self->exportFile(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFileResults& results) {
            if (callback) { callback->onExportFile(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void exportFileProgressWrapper(flight_hub_sdk::ExportFileProgressParam const &param,FHSDK_CALLBACK_PARAMETER(ExportFileProgressCallback)) {
        $self->exportFileProgress(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFileProgressResults& results) {
            if (callback) { callback->onExportFileProgress(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void uploadAIBoxFileWrapper(flight_hub_sdk::UploadFileParam const &param,FHSDK_CALLBACK_PARAMETER(UploadAIBoxFileCallback)) {
        $self->uploadAIBoxFile(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UploadFileResults& results) {
            if (callback) { callback->onUploadAIBoxFile(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void installAppWrapper(flight_hub_sdk::InstallAppParam const &param,FHSDK_CALLBACK_PARAMETER(InstallAppCallback)) {
        $self->installApp(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::InstallAppResults& results) {
            if (callback) { callback->onInstallApp(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void installAppProgressWrapper(flight_hub_sdk::InstallAppProgressParam const &param,FHSDK_CALLBACK_PARAMETER(InstallAppProgressCallback)) {
        $self->installAppProgress(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::InstallAppProgressResults& results) {
            if (callback) { callback->onInstallAppProgress(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void operateAppWrapper(flight_hub_sdk::OperateAppParam const &param,FHSDK_CALLBACK_PARAMETER(OperateAppCallback)) {
        $self->operateApp(param, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OperateAppResults& results) {
            if (callback) { callback->onOperateApp(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%extend flight_hub_sdk::IDeviceManageModule {
    void getDeviceListWrapper(FHSDK_CALLBACK_PARAMETER(DeviceListCallback)) {
        $self->getDeviceList([callback](const std::vector<flight_hub_sdk::DeviceTopo>& devices, const flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onDeviceList(devices, err);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void getDeviceDetailInfoWrapper(std::string const &device_sn,FHSDK_CALLBACK_PARAMETER(GetDeviceDetailInfoCallback)) {
        $self->getDeviceDetailInfo(device_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceDetailInfo& device_detail) {
                if (callback) {
                    callback->onGetDeviceDetailInfo(err, device_detail);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void modifyDeviceNameWrapper(flight_hub_sdk::ModifyDeviceNameParam const &device_list,FHSDK_CALLBACK_PARAMETER(ModifyDeviceNameCallback)) {
        $self->modifyDeviceName(device_list, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onModifyDeviceName(err);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void unbindDeviceWrapper(std::vector<std::string> const &device_sns,FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->unbindDevice(device_sns, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenWorkspaceDeviceChangedWrapper(FHSDK_CALLBACK_PARAMETER(WorkspaceDeviceChangeCallback)) {
        FHSDK_SHARE_CALLBACK(WorkspaceDeviceChangeCallback);
        return $self->listenWorkspaceDeviceChanged([cb](const flight_hub_sdk::WorkspaceDeviceChange& change) {
            if (cb) cb->onWorkspaceDeviceChange(change);
        });
    }

    void cancelListenWorkspaceDeviceChangedWrapper(int listen_id) {
        $self->cancelListenWorkspaceDeviceChanged(listen_id);
    }

    int listenDeviceTopoChangedWrapper(FHSDK_CALLBACK_PARAMETER(DeviceTopoChangeCallback)) {
        FHSDK_SHARE_CALLBACK(DeviceTopoChangeCallback);
        return $self->listenDeviceTopoChanged([cb](const flight_hub_sdk::DeviceTopoChange& change) {
            if (cb) cb->onDeviceTopoChange(change);
        });
    }

    void cancelListenDeviceTopoChangedWrapper(int listen_id) {
        $self->cancelListenDeviceTopoChanged(listen_id);
    }

    void getDockDeviceStatusWrapper(std::vector<std::string> const &device_sns,FHSDK_CALLBACK_PARAMETER(DockDeviceStatusListCallback)) {
        $self->getDockDeviceStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DockDeviceStatusResultList& result) {
                if (callback) {
                    callback->onDockDeviceStatusList(err, result);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void getDroneDeviceStatusWrapper(std::vector<std::string> const &device_sns,FHSDK_CALLBACK_PARAMETER(DroneDeviceStatusListCallback)) {
        $self->getDroneDeviceStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DroneDeviceStatusResultList& result) {
                if (callback) {
                    callback->onDroneDeviceStatusList(err, result);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }
}

%extend flight_hub_sdk::IFirmwareManageModule {
    void getLatestFirmwareReleaseNoteWrapper(std::vector<std::string> const &device_model_keys,std::string const &language,FHSDK_CALLBACK_PARAMETER(GetLatestFirmwareReleaseNoteCallback)) {
        $self->getLatestFirmwareReleaseNote(device_model_keys, language, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareReleaseNoteListResult result) {
            if (callback) {
                callback->onGetLatestFirmwareReleaseNote(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void invokeFirmwareUpgradeWrapper(flight_hub_sdk::FirmwareUpgradeParam const &params,FHSDK_CALLBACK_PARAMETER(InvokeFirmwareUpgradeCallback)) {
        $self->invokeFirmwareUpgrade(params, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareUpgradeResult result) {
            if (callback) {
                callback->onInvokeFirmwareUpgrade(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void queryFirmwareUpgradeProgressWrapper(std::string const &device_sn,FHSDK_CALLBACK_PARAMETER(QueryFirmwareUpgradeProgressCallback)) {
        $self->queryFirmwareUpgradeProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareUpgradeProgressResult result) {
            if (callback) {
                callback->onQueryFirmwareUpgradeProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void uploadFirmwareFileWrapper(std::string const &file_path,FHSDK_CALLBACK_PARAMETER(UploadFirmwareFileCallback)) {
        $self->uploadFirmwareFile(file_path, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadFileResults results) {
            if (callback) {
                callback->onUploadFirmwareFile(err, results);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void releaseFirmwareWrapper(std::string const &file_id,FHSDK_CALLBACK_PARAMETER(ReleaseFirmwareCallback)) {
        $self->releaseFirmware(file_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onReleaseFirmware(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void unReleaseFirmwareWrapper(std::string const &file_id,FHSDK_CALLBACK_PARAMETER(UnReleaseFirmwareCallback)) {
        $self->unReleaseFirmware(file_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onUnReleaseFirmware(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void deleteFirmwareWrapper(std::string const &file_id,FHSDK_CALLBACK_PARAMETER(DeleteFirmwareCallback)) {
        $self->deleteFirmware(file_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onDeleteFirmware(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void queryFirmwareListWrapper(int page,int page_size,FHSDK_CALLBACK_PARAMETER(QueryFirmwareListCallback)) {
        $self->queryFirmwareList(page, page_size, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareListResult result) {
            if (callback) {
                callback->onQueryFirmwareList(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void queryFirmwareCompatibilityConfigWrapper(FHSDK_CALLBACK_PARAMETER(QueryFirmwareCompatibilityConfigCallback)) {
        $self->queryFirmwareCompatibilityConfig([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareCompatibilityConfigResult result) {
            if (callback) {
                callback->onQueryFirmwareCompatibilityConfig(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void checkFirmwareStatusWrapper(std::vector<std::string> const &device_sns,std::vector<std::string> const &status,FHSDK_CALLBACK_PARAMETER(CheckFirmwareStatusCallback)) {
        $self->checkFirmwareStatus(device_sns, status, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FirmwareStatusListResult result) {
            if (callback) {
                callback->onCheckFirmwareStatus(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void submitFirmwareInfoWrapper(flight_hub_sdk::FirmwareInfoParam const &firmware_info,FHSDK_CALLBACK_PARAMETER(SubmitFirmwareInfoCallback)) {
        $self->submitFirmwareInfo(firmware_info, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSubmitFirmwareInfo(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenDeviceOtaProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceOtaProgressCallback)) {
        FHSDK_SHARE_CALLBACK(DeviceOtaProgressCallback);
        return $self->listenDeviceOtaProgress(device_sn, [cb](const flight_hub_sdk::DeviceOtaProgress& msg) {
            if (cb) cb->onDeviceOtaProgress(msg);
        });
    }
    void cancelListenDeviceOtaProgressWrapper(int listen_id) {
        $self->cancelListenDeviceOtaProgress(listen_id);
    }
}

%extend flight_hub_sdk::IPsdkResourceModule {
    void getPsdkSpeakerResourceListWrapper(FHSDK_CALLBACK_PARAMETER(PsdkSpeakerResourceListCallback)) {
        $self->getPsdkSpeakerResourceList([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResourceList& psdk_speaker_resource_list) {
                if (callback) {
                    callback->onPsdkSpeakerResourceList(err, psdk_speaker_resource_list);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void getPsdkSpeakerResourceListWithFilterWrapper(const flight_hub_sdk::PsdkSpeakerResourceListParams& params, FHSDK_CALLBACK_PARAMETER(PsdkSpeakerResourceListCallback)) {
        $self->getPsdkSpeakerResourceListWithFilter(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResourceList& psdk_speaker_resource_list) {
                if (callback) {
                    callback->onPsdkSpeakerResourceList(err, psdk_speaker_resource_list);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void createPsdkSpeakerResourceWrapper(flight_hub_sdk::CreatePsdkSpeakerResourceParams const &params,FHSDK_CALLBACK_PARAMETER(PsdkSpeakerResourceCallback)) {
        $self->createPsdkSpeakerResource(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResource& psdk_speaker_resource) {
                if (callback) {
                    callback->onPsdkSpeakerResource(err, psdk_speaker_resource);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void editPsdkSpeakerResourceWrapper(flight_hub_sdk::EditPsdkSpeakerResourceParams const &params,FHSDK_CALLBACK_PARAMETER(PsdkSpeakerResourceCallback)) {
        $self->editPsdkSpeakerResource(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkSpeakerResource& psdk_speaker_resource) {
                if (callback) {
                    callback->onPsdkSpeakerResource(err, psdk_speaker_resource);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void deletePsdkSpeakerResourceWrapper(long long resource_id,FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->deletePsdkSpeakerResource(resource_id, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void getSTSWrapper(FHSDK_CALLBACK_PARAMETER(STSInfoCallback)) {
        $self->getSTS([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::STSInfo& sts_info) {
                if (callback) {
                    callback->onSTSInfo(err, sts_info);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void uploadSpeakerAudioFileWrapper(std::string const &file_path,flight_hub_sdk::STSInfo const &sts_info,FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->uploadSpeakerAudioFile(file_path, sts_info, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void getPsdkWidgetResourcesWrapper(flight_hub_sdk::GetPsdkWidgetResourceParams const &params,FHSDK_CALLBACK_PARAMETER(PsdkWidgetResourceListCallback)) {
        $self->getPsdkWidgetResources(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::PsdkWidgetResourceList& psdk_widget_resource_list) {
                if (callback) {
                    callback->onPsdkWidgetResourceList(err, psdk_widget_resource_list);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }
}

%extend flight_hub_sdk::IFlashLedModule {
    void getFlashLedConfigListWrapper(FHSDK_CALLBACK_PARAMETER(FlashLedConfigListCallback)) {
        $self->getFlashLedConfigList([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfigList& list) {
                if (callback) {
                    callback->onFlashLedConfigList(err, list);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void createFlashLedConfigWrapper(flight_hub_sdk::FlashLedConfigParams const &params, FHSDK_CALLBACK_PARAMETER(FlashLedConfigCallback)) {
        $self->createFlashLedConfig(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfig& config) {
                if (callback) {
                    callback->onFlashLedConfig(err, config);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void updateFlashLedConfigWrapper(std::string const &config_id, flight_hub_sdk::FlashLedConfigParams const &params, FHSDK_CALLBACK_PARAMETER(FlashLedConfigCallback)) {
        $self->updateFlashLedConfig(config_id, params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlashLedConfig& config) {
                if (callback) {
                    callback->onFlashLedConfig(err, config);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void deleteFlashLedConfigWrapper(std::string const &config_id, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->deleteFlashLedConfig(config_id, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }
}

%extend flight_hub_sdk::IAutoRecordModule {
    void getAutoRecordConfigWrapper(FHSDK_CALLBACK_PARAMETER(AutoRecordConfigCallback)) {
        $self->getAutoRecordConfig([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::AutoRecordConfig& config) {
                if (callback) {
                    callback->onAutoRecordConfig(err, config);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }

    void saveAutoRecordConfigWrapper(flight_hub_sdk::SaveAutoRecordConfigParams const &params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->saveAutoRecordConfig(params, [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) {
                    callback->onResult(err);
                    FHSDK_DELETE_CALLBACK();
                }
            });
    }
}

%extend flight_hub_sdk::IWaylineManageModule {
    void getWaylineListWrapper(flight_hub_sdk::GetWaylineListParam param,FHSDK_CALLBACK_PARAMETER(GetWaylineListCallback)) {
        $self->getWaylineList(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetWaylineListResult result) {
            if (callback) {
                callback->onGetWaylineList(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getWaylineDetailWrapper(std::string const &wayline_uuid,FHSDK_CALLBACK_PARAMETER(GetWaylineDetailCallback)) {
        $self->getWaylineDetail(wayline_uuid, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetWaylineDetailResult result) {
            if (callback) {
                callback->onGetWaylineDetail(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void batchDeleteWaylinesWrapper(std::vector<std::string> const &wayline_uuids,FHSDK_CALLBACK_PARAMETER(BatchDeleteWaylinesCallback)) {
        $self->batchDeleteWaylines(wayline_uuids, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::BatchDeleteWaylinesResult result) {
            if (callback) {
                callback->onBatchDeleteWaylines(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void updateWaylineWrapper(std::string const &wayline_uuid,std::string const &wayline_name,FHSDK_CALLBACK_PARAMETER(UpdateWaylineCallback)) {
        $self->updateWayline(wayline_uuid, wayline_name, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::UpdateWaylineResult result) {
            if (callback) {
                callback->onUpdateWayline(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void submitWaylineInfoWrapper(flight_hub_sdk::SubmitWaylineInfoParam param,FHSDK_CALLBACK_PARAMETER(SubmitWaylineInfoCallback)) {
        $self->submitWaylineInfo(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSubmitWaylineInfo(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void batchDownloadWaylinesWrapper(flight_hub_sdk::BatchDownloadWaylinesParam param,FHSDK_CALLBACK_PARAMETER(BatchDownloadWaylinesCallback)) {
        $self->batchDownloadWaylines(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::BatchDownloadWaylinesResult result) {
            if (callback) {
                callback->onBatchDownloadWaylines(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void duplicateWaylineWrapper(flight_hub_sdk::DuplicateWaylineParam param,FHSDK_CALLBACK_PARAMETER(DuplicateWaylineCallback)) {
        $self->duplicateWayline(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::DuplicateWaylineResult result) {
            if (callback) {
                callback->onDuplicateWayline(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void copyWaylineWrapper(flight_hub_sdk::CopyWaylineParam param, FHSDK_CALLBACK_PARAMETER(CopyWaylineCallback)) {
        $self->copyWayline(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CopyWaylineResult result) {
            if (callback) {
                callback->onCopyWayline(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void concatWaylineWrapper(flight_hub_sdk::ConcatWaylineParam param, FHSDK_CALLBACK_PARAMETER(ConcatWaylineCallback)) {
        $self->concatWayline(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::ConcatWaylineResult result) {
            if (callback) {
                callback->onConcatWayline(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void importKmzWrapper(std::vector<std::string> file_paths, FHSDK_CALLBACK_PARAMETER(ImportKmzCallback)) {
        $self->importKmz(file_paths, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::ImportKmzResult result) {
            if (callback) {
                callback->onImportKmz(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenImportWaylineEventWrapper(FHSDK_CALLBACK_PARAMETER(ImportWaylineEventCallback)) {
        FHSDK_SHARE_CALLBACK(ImportWaylineEventCallback);
        return $self->listenImportWaylineEvent([cb](const flight_hub_sdk::ImportWaylineEventResult& result) {
            if (cb) cb->onImportWaylineEvent(result);
        });
    }

    void cancelListenImportWaylineEventWrapper(int listen_id) {
        $self->cancelListenImportWaylineEvent(listen_id);
    }

    void getWaylineEditorStsWrapper(const flight_hub_sdk::GetWaylineEditorStsParams& params,
                                    FHSDK_CALLBACK_PARAMETER(GetWaylineEditorStsCallback)) {
        $self->getWaylineEditorSts(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetWaylineEditorStsResult& result) {
                if (callback) { callback->onGetWaylineEditorSts(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }
}

// AI Inside 机载算法管理 %extend Wrapper
%extend flight_hub_sdk::IAIInsideManageModule {
    void getAIInsideModelsWrapper(FHSDK_CALLBACK_PARAMETER(GetAIInsideModelsCallback)) {
        $self->getAIInsideModels([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelsResult result) {
            if (callback) { callback->onGetAIInsideModels(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getAIInsideModelDevicesWrapper(flight_hub_sdk::GetAIInsideModelDevicesParam param, FHSDK_CALLBACK_PARAMETER(GetAIInsideModelDevicesCallback)) {
        $self->getAIInsideModelDevices(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelDevicesResult result) {
            if (callback) { callback->onGetAIInsideModelDevices(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getAIInsideModelDevicesSummaryWrapper(flight_hub_sdk::GetAIInsideModelDevicesSummaryParam param, FHSDK_CALLBACK_PARAMETER(GetAIInsideModelDevicesSummaryCallback)) {
        $self->getAIInsideModelDevicesSummary(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAIInsideModelDevicesSummaryResult result) {
            if (callback) { callback->onGetAIInsideModelDevicesSummary(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void operateAIInsideModelWrapper(flight_hub_sdk::OperateAIInsideModelParam param, FHSDK_CALLBACK_PARAMETER(OperateAIInsideModelCallback)) {
        $self->operateAIInsideModel(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::OperateAIInsideModelResult result) {
            if (callback) { callback->onOperateAIInsideModel(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void createAIInsideAlgorithmWrapper(flight_hub_sdk::CreateAIInsideAlgorithmParam param, FHSDK_CALLBACK_PARAMETER(CreateAIInsideAlgorithmCallback)) {
        $self->createAIInsideAlgorithm(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CreateAIInsideAlgorithmResult result) {
            if (callback) { callback->onCreateAIInsideAlgorithm(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void fileUploadAIInsideAlgorithmWrapper(flight_hub_sdk::FileUploadAIInsideAlgorithmParam param, FHSDK_CALLBACK_PARAMETER(FileUploadAIInsideAlgorithmCallback)) {
        $self->fileUploadAIInsideAlgorithm(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::FileUploadAIInsideAlgorithmResult result) {
            if (callback) { callback->onFileUploadAIInsideAlgorithm(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void deleteAIInsideAlgorithmWrapper(flight_hub_sdk::DeleteAIInsideAlgorithmParam param, FHSDK_CALLBACK_PARAMETER(DeleteAIInsideAlgorithmCallback)) {
        $self->deleteAIInsideAlgorithm(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onDeleteAIInsideAlgorithm(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void uploadAlgorithmFileWrapper(flight_hub_sdk::UploadAIInsideAlgorithmFileParam param, FHSDK_CALLBACK_PARAMETER(UploadAlgorithmFileCallback)) {
        $self->uploadAlgorithmFile(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::UploadAIInsideAlgorithmFileResult result) {
            if (callback) { callback->onUploadAlgorithmFile(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%extend flight_hub_sdk::IAIAlertModule {
    void postDeviceSettingWrapper(flight_hub_sdk::PostAiAlertDeviceSettingParam param, FHSDK_CALLBACK_PARAMETER(PostAiAlertDeviceSettingCallback)) {
        $self->postDeviceSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::PostAiAlertDeviceSettingResult result) {
            if (callback) { callback->onPostAiAlertDeviceSetting(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getDeviceSettingWrapper(flight_hub_sdk::GetAiAlertDeviceSettingParam param, FHSDK_CALLBACK_PARAMETER(GetAiAlertDeviceSettingCallback)) {
        $self->getDeviceSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertDeviceSettingResult result) {
            if (callback) { callback->onGetAiAlertDeviceSetting(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void postAlgorithmSettingWrapper(flight_hub_sdk::PostAiAlertAlgorithmSettingParam param, FHSDK_CALLBACK_PARAMETER(PostAiAlertAlgorithmSettingCallback)) {
        $self->postAlgorithmSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::PostAiAlertAlgorithmSettingResult result) {
            if (callback) { callback->onPostAiAlertAlgorithmSetting(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getAlgorithmSettingWrapper(flight_hub_sdk::GetAiAlertAlgorithmSettingParam param, FHSDK_CALLBACK_PARAMETER(GetAiAlertAlgorithmSettingCallback)) {
        $self->getAlgorithmSetting(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertAlgorithmSettingResult result) {
            if (callback) { callback->onGetAiAlertAlgorithmSetting(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getAiAlertFlightsWrapper(flight_hub_sdk::GetAiAlertFlightsParam param, FHSDK_CALLBACK_PARAMETER(GetAiAlertFlightsCallback)) {
        $self->getAiAlertFlights(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertFlightsResult result) {
            if (callback) { callback->onGetAiAlertFlights(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getAiAlertRecordsWrapper(flight_hub_sdk::GetAiAlertRecordsParam param, FHSDK_CALLBACK_PARAMETER(GetAiAlertRecordsCallback)) {
        $self->getAiAlertRecords(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::GetAiAlertRecordsResult result) {
            if (callback) { callback->onGetAiAlertRecords(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    int listenAiAlertRecordsWrapper(std::string device_sn, FHSDK_CALLBACK_PARAMETER(AiAlertRecordsNotifyCallback)) {
        return $self->listenAiAlertRecords(device_sn, [callback](flight_hub_sdk::AiAlertRecordsNotifyResult result) {
            if (callback) { callback->onAiAlertRecordsNotify(result); }
        });
    }

    void cancelListenAiAlertRecordsWrapper(int listen_id) {
        $self->cancelListenAiAlertRecords(listen_id);
    }

    // 删除告警记录：复用通用回调 ResultCallbackWithError（后端响应为空 {}，无数据可回传）
    void deleteAiAlertRecordWrapper(const flight_hub_sdk::DeleteAiAlertRecordParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->deleteAiAlertRecord(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%extend flight_hub_sdk::ILiveSyncModule {
    void createLiveSyncWrapper(flight_hub_sdk::LiveSyncCreateParams param, FHSDK_CALLBACK_PARAMETER(CreateLiveSyncCallback)) {
        $self->createLiveSync(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncCreateResult result) {
            if (callback) { callback->onCreateLiveSync(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void deleteLiveSyncWrapper(std::string const& id, FHSDK_CALLBACK_PARAMETER(DeleteLiveSyncCallback)) {
        $self->deleteLiveSync(id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onDeleteLiveSync(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void updateLiveSyncWrapper(flight_hub_sdk::LiveSyncUpdateParams param, FHSDK_CALLBACK_PARAMETER(UpdateLiveSyncCallback)) {
        $self->updateLiveSync(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncItem result) {
            if (callback) { callback->onUpdateLiveSync(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getLiveSyncListWrapper(flight_hub_sdk::LiveSyncListParams param, FHSDK_CALLBACK_PARAMETER(GetLiveSyncListCallback)) {
        $self->getLiveSyncList(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncListResult result) {
            if (callback) { callback->onGetLiveSyncList(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getLiveSyncDevicesV2Wrapper(FHSDK_CALLBACK_PARAMETER(GetLiveSyncDevicesV2Callback)) {
        $self->getLiveSyncDevicesV2([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncDevicesV2Result result) {
            if (callback) { callback->onGetLiveSyncDevicesV2(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getLiveSyncSummaryWrapper(FHSDK_CALLBACK_PARAMETER(GetLiveSyncSummaryCallback)) {
        $self->getLiveSyncSummary([callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LiveSyncSummaryResult result) {
            if (callback) { callback->onGetLiveSyncSummary(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%extend flight_hub_sdk::IFlightTaskModule {
    void getFlightTaskListWrapper(const flight_hub_sdk::FlightTaskListParams& params, FHSDK_CALLBACK_PARAMETER(GetFlightTaskListCallback)) {
        $self->getFlightTaskList(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightTaskListResult& result) {
            if (callback) { callback->onGetFlightTaskList(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getCommandTaskListWrapper(const flight_hub_sdk::GetCommandTaskListParams& params, FHSDK_CALLBACK_PARAMETER(GetCommandTaskListCallback)) {
        $self->getCommandTaskList(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetCommandTaskListResults& results) {
            if (callback) { callback->onGetCommandTaskList(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getFlightTaskTrackWrapper(const flight_hub_sdk::GetFlightTaskTrackParams& params, FHSDK_CALLBACK_PARAMETER(GetFlightTaskTrackCallback)) {
        $self->getFlightTaskTrack(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightTaskTrackResults& results) {
            if (callback) { callback->onGetFlightTaskTrack(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getFlightRisksWrapper(const flight_hub_sdk::GetFlightRisksParams& params, FHSDK_CALLBACK_PARAMETER(GetFlightRisksCallback)) {
        $self->getFlightRisks(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightRisksResults& results) {
            if (callback) { callback->onGetFlightRisks(err, results); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void createFlightTaskWrapper(const flight_hub_sdk::CreateTaskReq& req, FHSDK_CALLBACK_PARAMETER(CreateFlightTaskCallback)) {
        $self->createFlightTask(req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateTaskResp& result) {
            if (callback) { callback->onCreateFlightTask(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getFlightTaskDetailWrapper(const std::string& task_uuid, FHSDK_CALLBACK_PARAMETER(GetFlightTaskDetailCallback)) {
        $self->getFlightTaskDetail(task_uuid, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TaskDetail& result) {
            if (callback) { callback->onGetFlightTaskDetail(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void editFlightTaskWrapper(const std::string& task_uuid, const flight_hub_sdk::EditTaskReq& req, FHSDK_CALLBACK_PARAMETER(CreateFlightTaskCallback)) {
        $self->editFlightTask(task_uuid, req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateTaskResp& result) {
            if (callback) { callback->onCreateFlightTask(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void deleteFlightTaskWrapper(const std::string& task_uuid, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->deleteFlightTask(task_uuid, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void updateFlightTaskStatusWrapper(const std::string& task_uuid, int status, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->updateFlightTaskStatus(task_uuid, status, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void batchUpdateFlightTaskStatusWrapper(const flight_hub_sdk::BatchUpdateStatusReq& req, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->batchUpdateFlightTaskStatus(req, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getDeviceRecommendationWrapper(const flight_hub_sdk::DeviceRecommendationReq& req, FHSDK_CALLBACK_PARAMETER(GetDeviceRecommendationCallback)) {
        $self->getDeviceRecommendation(req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRecommendationResp& result) {
            if (callback) { callback->onGetDeviceRecommendation(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getDeviceTcasWrapper(const std::vector<std::string>& center_sn, FHSDK_CALLBACK_PARAMETER(GetDeviceTcasCallback)) {
        $self->getDeviceTcas(center_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetTCAInfosResp& result) {
            if (callback) { callback->onGetDeviceTcas(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void updateDeviceTcasWrapper(const std::vector<std::string>& center_sn, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->updateDeviceTcas(center_sn, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void startRoamingWrapper(const flight_hub_sdk::StartRoamingParams& params, FHSDK_CALLBACK_PARAMETER(StartRoamingCallback)) {
        $self->startRoaming(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onStartRoaming(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void setRoamingStationSnWrapper(const flight_hub_sdk::SetRoamingStationSnParams& params,
                                    FHSDK_CALLBACK_PARAMETER(SetRoamingStationSnCallback)) {
        $self->setRoamingStationSn(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::SetRoamingStationSnResult& result) {
                if (callback) { callback->onSetRoamingStationSn(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }
    void getInFlightTaskToposWrapper(const std::vector<std::string>& drone_sns,
                                     FHSDK_CALLBACK_PARAMETER(GetInFlightTaskToposCallback)) {
        $self->getInFlightTaskTopos(drone_sns,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetInFlightTaskToposResult& result) {
                if (callback) { callback->onGetInFlightTaskTopos(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }
    int listenTaskTopoChangedWrapper(FHSDK_CALLBACK_PARAMETER(TaskTopoChangedCallback)) {
        FHSDK_SHARE_CALLBACK(TaskTopoChangedCallback);
        return $self->listenTaskTopoChanged(
            [cb](const flight_hub_sdk::TaskTopoChangedEvent& event) {
                if (cb) cb->onTaskTopoChanged(event);
            });
    }
    void cancelListenTaskTopoChangedWrapper(int listen_id) {
        $self->cancelListenTaskTopoChanged(listen_id);
    }
}

%extend flight_hub_sdk::ICameraControlModule {
    void switchLiveLensWrapper(const flight_hub_sdk::SwitchLiveLensParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSwitchLiveLens(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setPhotoStorageSettingsWrapper(const flight_hub_sdk::SetPhotoStorageSettingsParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetPhotoStorageSettings(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setVideoStorageSettingsWrapper(const flight_hub_sdk::SetVideoStorageSettingsParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetVideoStorageSettings(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setNightSceneModeWrapper(const flight_hub_sdk::SetNightSceneModeParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetNightSceneMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void resetGimbalWrapper(const flight_hub_sdk::GimbalResetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcResetGimbal(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void takePhotoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcTakePhoto(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void startRecordWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcStartRecord(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void stopRecordWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcStopRecord(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void zoomAtTargetWrapper(const flight_hub_sdk::ZoomAtTargetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcZoomAtTarget(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setFocalLengthWrapper(const flight_hub_sdk::CameraFocalLengthParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetFocalLength(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void lookAtTargetWrapper(const flight_hub_sdk::LookAtTargetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->lookAtTarget(params, [callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void startPanoramaWrapper(const flight_hub_sdk::PanoramaParam& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcStartPanorama(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void stopPanoramaWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcStopPanorama(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenPanoramaStatusWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(PanoramaStatusCallback)) {
        FHSDK_SHARE_CALLBACK(PanoramaStatusCallback);
        return $self->listenDrcPanoramaStatus(payload_index, [cb](const flight_hub_sdk::PanoramaStatus& status) {
            if (cb) cb->onPanoramaStatus(status);
        });
    }

    void switchCameraModeWrapper(const flight_hub_sdk::SwitchCameraModeParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSwitchCameraMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenCameraModeAvailableRangeWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraModeAvailableRangeCallback)) {
        FHSDK_SHARE_CALLBACK(CameraModeAvailableRangeCallback);
        return $self->listenDrcCameraModeAvailableRange(payload_index, [cb](const std::vector<int>& camera_modes) {
            if (cb) cb->onCameraModeAvailableRange(camera_modes);
        });
    }

    int listenCameraStateWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraStateCallback)) {
        FHSDK_SHARE_CALLBACK(CameraStateCallback);
        return $self->listenDrcCameraState(payload_index, [cb](const flight_hub_sdk::CameraState& state) {
            if (cb) cb->onCameraState(state);
        });
    }

    int listenLiveviewWorldRegionWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(LiveviewWorldRegionCallback)) {
        FHSDK_SHARE_CALLBACK(LiveviewWorldRegionCallback);
        return $self->listenDrcLiveviewWorldRegion(payload_index, [cb](const flight_hub_sdk::LiveviewWorldRegion& region) {
            if (cb) cb->onLiveviewWorldRegion(region);
        });
    }

    int listenCameraWideLensStateWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraWideLensStateCallback)) {
        FHSDK_SHARE_CALLBACK(CameraWideLensStateCallback);
        return $self->listenDrcCameraWideLensState(payload_index, [cb](const flight_hub_sdk::CameraWideLensState& state) {
            if (cb) cb->onCameraWideLensState(state);
        });
    }

    int listenCameraZoomLensStateWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraZoomLensStateCallback)) {
        FHSDK_SHARE_CALLBACK(CameraZoomLensStateCallback);
        return $self->listenDrcCameraZoomLensState(payload_index, [cb](const flight_hub_sdk::CameraZoomLensState& state) {
            if (cb) cb->onCameraZoomLensState(state);
        });
    }

    int listenCameraMeasureTargetWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraMeasureTargetCallback)) {
        FHSDK_SHARE_CALLBACK(CameraMeasureTargetCallback);
        return $self->listenDrcCameraMeasureTarget(payload_index, [cb](const flight_hub_sdk::CameraMeasureTarget& target) {
            if (cb) cb->onCameraMeasureTarget(target);
        });
    }

    int listenNightModeSettingsWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(NightModeSettingsCallback)) {
        FHSDK_SHARE_CALLBACK(NightModeSettingsCallback);
        return $self->listenDrcNightModeSettings(payload_index, [cb](const flight_hub_sdk::NightModeSettings& settings) {
            if (cb) cb->onNightModeSettings(settings);
        });
    }

    int listenGimbalStateWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(GimbalStateCallback)) {
        FHSDK_SHARE_CALLBACK(GimbalStateCallback);
        return $self->listenDrcGimbalState(payload_index, [cb](const flight_hub_sdk::GimbalState& state) {
            if (cb) cb->onGimbalState(state);
        });
    }

    int listenLiveVideoInfoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(LiveVideoInfoCallback)) {
        FHSDK_SHARE_CALLBACK(LiveVideoInfoCallback);
        return $self->listenDrcLiveVideoInfo(payload_index, [cb](const flight_hub_sdk::LiveVideoInfo& info) {
            if (cb) cb->onLiveVideoInfo(info);
        });
    }

    void cancelListenLiveVideoInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcLiveVideoInfo(payload_index, listen_id);
    }

    int listenThermalGainModeWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ThermalGainModeInfoCallback)) {
        FHSDK_SHARE_CALLBACK(ThermalGainModeInfoCallback);
        return $self->listenDrcThermalGainMode(payload_index, [cb](const flight_hub_sdk::ThermalGainModeInfo& info) {
            if (cb) cb->onThermalGainModeInfo(info);
        });
    }

    void cancelListenThermalGainModeWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcThermalGainMode(payload_index, listen_id);
    }

    int listenCameraIrLensStateWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraIrLensStateCallback)) {
        FHSDK_SHARE_CALLBACK(CameraIrLensStateCallback);
        return $self->listenDrcCameraIrLensState(payload_index, [cb](const flight_hub_sdk::CameraIrLensState& state) {
            if (cb) cb->onCameraIrLensState(state);
        });
    }

    void cancelListenCameraIrLensStateWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIrLensState(payload_index, listen_id);
    }

    int listenCameraIrThermometryWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraIrThermometryInfoCallback)) {
        FHSDK_SHARE_CALLBACK(CameraIrThermometryInfoCallback);
        return $self->listenDrcCameraIrThermometry(payload_index, [cb](const flight_hub_sdk::CameraIrThermometryInfo& info) {
            if (cb) cb->onCameraIrThermometryInfo(info);
        });
    }

    void cancelListenCameraIrThermometryWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIrThermometry(payload_index, listen_id);
    }

    // ── B4 State push 拆分 ──
    int listenCameraWatermarkSettingsWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraWatermarkSettingsCallback)) {
        FHSDK_SHARE_CALLBACK(CameraWatermarkSettingsCallback);
        return $self->listenDrcCameraWatermarkSettings(payload_index, [cb](const flight_hub_sdk::CameraWatermarkSettings& v) { if (cb) cb->onCameraWatermarkSettings(v); });
    }
    void cancelListenCameraWatermarkSettingsWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraWatermarkSettings(payload_index, listen_id);
    }

    int listenCameraStorageInfoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraStorageInfoCallback)) {
        FHSDK_SHARE_CALLBACK(CameraStorageInfoCallback);
        return $self->listenDrcCameraStorageInfo(payload_index, [cb](const flight_hub_sdk::CameraStorageInfo& v) { if (cb) cb->onCameraStorageInfo(v); });
    }
    void cancelListenCameraStorageInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraStorageInfo(payload_index, listen_id);
    }

    int listenCameraIrPipStateWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraIrPipStateCallback)) {
        FHSDK_SHARE_CALLBACK(CameraIrPipStateCallback);
        return $self->listenDrcCameraIrPipState(payload_index, [cb](const flight_hub_sdk::CameraIrPipState& v) { if (cb) cb->onCameraIrPipState(v); });
    }
    void cancelListenCameraIrPipStateWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIrPipState(payload_index, listen_id);
    }

    int listenCameraIsothermPolygonWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraIsothermPolygonCallback)) {
        FHSDK_SHARE_CALLBACK(CameraIsothermPolygonCallback);
        return $self->listenDrcCameraIsothermPolygon(payload_index, [cb](const flight_hub_sdk::CameraIsothermPolygon& v) { if (cb) cb->onCameraIsothermPolygon(v); });
    }
    void cancelListenCameraIsothermPolygonWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraIsothermPolygon(payload_index, listen_id);
    }

    int listenCameraMediaStorageWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraMediaStorageCallback)) {
        FHSDK_SHARE_CALLBACK(CameraMediaStorageCallback);
        return $self->listenDrcCameraMediaStorage(payload_index, [cb](const flight_hub_sdk::CameraMediaStorage& v) { if (cb) cb->onCameraMediaStorage(v); });
    }
    void cancelListenCameraMediaStorageWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraMediaStorage(payload_index, listen_id);
    }

    int listenCameraAlgorithmDehazeInfoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraAlgorithmDehazeInfoCallback)) {
        FHSDK_SHARE_CALLBACK(CameraAlgorithmDehazeInfoCallback);
        return $self->listenDrcCameraAlgorithmDehazeInfo(payload_index, [cb](const flight_hub_sdk::CameraAlgorithmDehazeInfo& v) { if (cb) cb->onCameraAlgorithmDehazeInfo(v); });
    }
    void cancelListenCameraAlgorithmDehazeInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraAlgorithmDehazeInfo(payload_index, listen_id);
    }

    int listenCameraPreRecordedInfoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraPreRecordedInfoCallback)) {
        FHSDK_SHARE_CALLBACK(CameraPreRecordedInfoCallback);
        return $self->listenDrcCameraPreRecordedInfo(payload_index, [cb](const flight_hub_sdk::CameraPreRecordedInfo& v) { if (cb) cb->onCameraPreRecordedInfo(v); });
    }
    void cancelListenCameraPreRecordedInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraPreRecordedInfo(payload_index, listen_id);
    }

    int listenCameraCapabilityWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraCapabilityCallback)) {
        FHSDK_SHARE_CALLBACK(CameraCapabilityCallback);
        return $self->listenDrcCameraCapability(payload_index, [cb](const flight_hub_sdk::CameraCapability& v) { if (cb) cb->onCameraCapability(v); });
    }
    void cancelListenCameraCapabilityWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraCapability(payload_index, listen_id);
    }

    int listenCameraSettableCapabilityWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraSettableCapabilityCallback)) {
        FHSDK_SHARE_CALLBACK(CameraSettableCapabilityCallback);
        return $self->listenDrcCameraSettableCapability(payload_index, [cb](const flight_hub_sdk::CameraSettableCapability& v) { if (cb) cb->onCameraSettableCapability(v); });
    }
    void cancelListenCameraSettableCapabilityWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraSettableCapability(payload_index, listen_id);
    }

    int listenCameraVisibleCapabilityWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraVisibleCapabilityCallback)) {
        FHSDK_SHARE_CALLBACK(CameraVisibleCapabilityCallback);
        return $self->listenDrcCameraVisibleCapability(payload_index, [cb](const flight_hub_sdk::CameraVisibleCapability& v) { if (cb) cb->onCameraVisibleCapability(v); });
    }
    void cancelListenCameraVisibleCapabilityWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraVisibleCapability(payload_index, listen_id);
    }

    int listenCameraPhotoInfoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(CameraPhotoInfoCallback)) {
        FHSDK_SHARE_CALLBACK(CameraPhotoInfoCallback);
        return $self->listenDrcCameraPhotoInfo(payload_index, [cb](const flight_hub_sdk::CameraPhotoInfo& v) { if (cb) cb->onCameraPhotoInfo(v); });
    }
    void cancelListenCameraPhotoInfoWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcCameraPhotoInfo(payload_index, listen_id);
    }

    void smartPhotoTakePhotoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSmartPhotoTakePhoto(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setLinkageZoomWrapper(const flight_hub_sdk::LinkageZoomParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetLinkageZoom(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void sendCameraScreenDragWrapper(const flight_hub_sdk::CameraScreenDragParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSendCameraScreenDrag(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setPhotoSizeWrapper(const flight_hub_sdk::PhotoSizeParam& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetPhotoSize(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIntervalTakePhotoWrapper(const flight_hub_sdk::IntervalTakePhotoSetParam& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIntervalTakePhoto(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void intervalTakePhotoWrapper(const flight_hub_sdk::IntervalTakePhotoParam& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcIntervalTakePhoto(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void stopTakePhotoWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcStopTakePhoto(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    int listenIntervalTakePhotoStatusWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(IntervalTakePhotoStatusCallback)) {
        FHSDK_SHARE_CALLBACK(IntervalTakePhotoStatusCallback);
        return $self->listenDrcIntervalTakePhotoStatus(payload_index, [cb](const flight_hub_sdk::IntervalTakePhotoStatus& status) {
            if (cb) cb->onIntervalTakePhotoStatus(status);
        });
    }

    void cancelListenIntervalTakePhotoStatusWrapper(const std::string& payload_index, int listen_id) {
        $self->cancelListenDrcIntervalTakePhotoStatus(payload_index, listen_id);
    }

    void cameraAimAtTargetWrapper(const flight_hub_sdk::CameraAimParam& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcCameraAimAtTarget(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setThermalGainModeWrapper(const flight_hub_sdk::ThermalGainModeSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetThermalGainMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // ══════════════════════════════════════════════
    // Camera Advanced Parameters Wrappers
    // ══════════════════════════════════════════════

    void setCameraApertureValueWrapper(const flight_hub_sdk::CameraApertureValueSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraApertureValue(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraExposureModeWrapper(const flight_hub_sdk::CameraExposureModeSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraExposureMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraExposureValueWrapper(const flight_hub_sdk::CameraExposureSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraExposureValue(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraFocusModeWrapper(const flight_hub_sdk::CameraFocusModeSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraFocusMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraFocusValueWrapper(const flight_hub_sdk::CameraFocusValueSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraFocusValue(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraIsoWrapper(const flight_hub_sdk::CameraIsoSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraIso(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraMechanicalShutterWrapper(const flight_hub_sdk::CameraMechanicalShutterSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraMechanicalShutter(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void cameraPointFocusActionWrapper(const flight_hub_sdk::CameraPointFocusActionParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcCameraPointFocusAction(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraShutterWrapper(const flight_hub_sdk::CameraShutterSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraShutter(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraWatermarkWrapper(const flight_hub_sdk::CameraWatermarkParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraWatermark(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraDewarpingWrapper(const flight_hub_sdk::CameraDewarpingSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraDewarping(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraScreenSplitWrapper(const flight_hub_sdk::CameraScreenSplitParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraScreenSplit(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void cleanFogWrapper(const flight_hub_sdk::CleanFogParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcCleanFog(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setThermalCurrentPaletteWrapper(const flight_hub_sdk::ThermalCurrentPaletteSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetThermalCurrentPalette(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setThermalSuperResolutionEnableWrapper(const flight_hub_sdk::ThermalSuperResolutionEnableParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetThermalSuperResolutionEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIrMeteringModeWrapper(const flight_hub_sdk::IrMeteringModeSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIrMeteringMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIrMeteringPointWrapper(const flight_hub_sdk::IrMeteringPointSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIrMeteringPoint(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIrMeteringAreaWrapper(const flight_hub_sdk::IrMeteringAreaSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIrMeteringArea(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIrDisplayModeWrapper(const flight_hub_sdk::SetIrDisplayModeParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIrDisplayMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIrEnhanceModeWrapper(const flight_hub_sdk::SetIrEnhanceModeParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIrEnhanceMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIrMixRateWrapper(const flight_hub_sdk::SetIrMixRateParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIrMixRate(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIsothermEnableWrapper(const flight_hub_sdk::SetIsothermEnableParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIsothermEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIsothermColorRenderingTypeWrapper(const flight_hub_sdk::SetIsothermColorRenderingTypeParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIsothermColorRenderingType(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIsothermLowerUpperRangeWrapper(const flight_hub_sdk::SetIsothermLowerUpperRangeParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIsothermLowerUpperRange(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIsothermRenderingPolygonWrapper(const flight_hub_sdk::SetIsothermRenderingPolygonParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIsothermRenderingPolygon(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setIsothermRoiBasedRenderingEnableWrapper(const flight_hub_sdk::SetIsothermRoiBasedRenderingEnableParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetIsothermRoiBasedRenderingEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setInfraredFillLightEnableWrapper(const flight_hub_sdk::InfraredFillLightEnableParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetInfraredFillLightEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraNightVisionEnableWrapper(const flight_hub_sdk::CameraNightVisionEnableParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraNightVisionEnable(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void resetCameraSettingsWrapper(const flight_hub_sdk::CameraSettingsResetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcResetCameraSettings(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraDenoiseLevelSetWrapper(const flight_hub_sdk::CameraDenoiseLevelSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraDenoiseLevelSet(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setVideoResolutionWrapper(const flight_hub_sdk::VideoResolutionSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetVideoResolution(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCameraPhotoFormatWrapper(const flight_hub_sdk::CameraPhotoFormatSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCameraPhotoFormat(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setPreRecordedStatusWrapper(const flight_hub_sdk::PreRecordedStatusSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetPreRecordedStatus(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setPreRecordedTimeWrapper(const flight_hub_sdk::PreRecordedTimeSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetPreRecordedTime(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setCountDownActionWrapper(int count_down_type, int action_type, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetCountDownAction(count_down_type, action_type, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setAssistantVideoModeWrapper(int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetAssistantVideoMode(mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setAssistantVideoSwitchWrapper(int sw, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetAssistantVideoSwitch(sw, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setAlgorithmDehazeModeWrapper(const flight_hub_sdk::AlgorithmDehazeModeSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetAlgorithmDehazeMode(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void setAlgorithmDehazeLevelWrapper(const flight_hub_sdk::AlgorithmDehazeLevelSetParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetAlgorithmDehazeLevel(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    // EA340 消息推送补全（Phase D）: 飞机相机水印设置推送(WebSocket);
    int listenDroneCameraWatermarkSettingsWrapper(FHSDK_CALLBACK_PARAMETER(DroneCameraWatermarkSettingsPushCallback)) {
        FHSDK_SHARE_CALLBACK(DroneCameraWatermarkSettingsPushCallback);
        return $self->listenDroneCameraWatermarkSettings([cb](const flight_hub_sdk::DroneCameraWatermarkSettingsPush& msg) {
            if (cb) cb->onDroneCameraWatermarkSettingsPush(msg);
        });
    }

    void cancelListenDroneCameraWatermarkSettingsWrapper(int listen_id) {
        $self->cancelListenDroneCameraWatermarkSettings(listen_id);
    }
}

%extend flight_hub_sdk::IDockControlModule {
    void changeDockCameraPositionWrapper(const flight_hub_sdk::LiveInfo& live_info, int camera_position, const std::string& gateway_sn, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->changeDockCameraPosition(live_info, camera_position, gateway_sn, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenDockAirConditionerStatusInfoWrapper(FHSDK_CALLBACK_PARAMETER(DockAirConditionerStatusInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DockAirConditionerStatusInfoCallback);
        return $self->listenDockAirConditionerStatusInfo([cb](const flight_hub_sdk::DockAirConditionerStatusInfo& status_info) {
            if (cb) cb->onDockAirConditionerStatusInfo(status_info);
        });
    }

    void cancelListenDockAirConditionerStatusInfoWrapper(int listen_id) {
        $self->cancelListenDockAirConditionerStatusInfo(listen_id);
    }

    int listenDockSupplementLightStateWrapper(FHSDK_CALLBACK_PARAMETER(DockSupplementLightStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockSupplementLightStateCallback);
        return $self->listenDockSupplementLightState([cb](const flight_hub_sdk::DockSupplementLightState& light_state) {
            if (cb) cb->onDockSupplementLightState(light_state);
        });
    }

    void cancelListenDockSupplementLightStateWrapper(int listen_id) {
        $self->cancelListenDockSupplementLightState(listen_id);
    }

    void setDockSupplementLightStateWrapper(bool state, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setDockSupplementLightState(state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDockChargeStateWrapper(bool state, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setDockChargeState(state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDockCoverStateWrapper(int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setDockCoverState(mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDockAirConditionerModeWrapper(int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setDockAirConditionerMode(mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%extend flight_hub_sdk::ILiveModule {
    void startWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->start([callback](flight_hub_sdk::FlightHubError result) {
            if (callback) {
                callback->onResult(result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void startWithURLWrapper(FHSDK_CALLBACK_PARAMETER(LiveStreamStartCallback)) {
        $self->startWithURL([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::LiveStreamStartResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void stopWithURLWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->stopWithURL([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int subscribeLiveStreamWrapper(FHSDK_CALLBACK_PARAMETER(LiveStreamCallback)) {
        FHSDK_SHARE_CALLBACK(LiveStreamCallback);
        return $self->subscribeLiveStream([cb](const std::vector<uint8_t>& data, const flight_hub_sdk::FrameInfo& frame_info) {
            std::vector<unsigned char> data_vector(data.begin(), data.end());
            if (cb) cb->onLiveStream(data_vector, frame_info);
        });
    }

    int subscribeAiSeiInfoWrapper(FHSDK_CALLBACK_PARAMETER(AiSeiInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AiSeiInfoCallback);
        return $self->subscribeAiSeiInfo([cb](const flight_hub_sdk::AiSeiInfo& ai_sei_info) {
            if (cb) cb->onAiSeiInfo(ai_sei_info);
        });
    }

    int subscribeArSeiInfoWrapper(FHSDK_CALLBACK_PARAMETER(ArSeiInfoCallback)) {
        FHSDK_SHARE_CALLBACK(ArSeiInfoCallback);
        return $self->subscribeArSeiInfo([cb](const flight_hub_sdk::ArSeiInfo& ar_sei_info) {
            if (cb) cb->onArSeiInfo(ar_sei_info);
        });
    }

    int subscribeFovSeiInfoWrapper(FHSDK_CALLBACK_PARAMETER(FovSeiInfoCallback)) {
        FHSDK_SHARE_CALLBACK(FovSeiInfoCallback);
        return $self->subscribeFovSeiInfo([cb](const flight_hub_sdk::FovSeiInfo& fov_sei_info) {
            if (cb) cb->onFovSeiInfo(fov_sei_info);
        });
    }

    int subscribeAssistantSeiInfoWrapper(FHSDK_CALLBACK_PARAMETER(AssistantSeiInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AssistantSeiInfoCallback);
        return $self->subscribeAssistantSeiInfo([cb](const flight_hub_sdk::AssistantSeiInfo& assistant_sei_info) {
            if (cb) cb->onAssistantSeiInfo(assistant_sei_info);
        });
    }

    int subscribeEisSeiInfoWrapper(FHSDK_CALLBACK_PARAMETER(EisSeiInfoCallback)) {
        FHSDK_SHARE_CALLBACK(EisSeiInfoCallback);
        return $self->subscribeEisSeiInfo([cb](const flight_hub_sdk::EisSeiInfo& eis_sei_info) {
            if (cb) cb->onEisSeiInfo(eis_sei_info);
        });
    }

    void setLiveQualityWrapper(const flight_hub_sdk::LiveQualitySettingParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setLiveQuality(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%ignore flight_hub_sdk::ILiveRecordModule::startLiveRecord;
%ignore flight_hub_sdk::ILiveRecordModule::stopLiveRecord;
%ignore flight_hub_sdk::ILiveRecordModule::getDeviceLiveRecords;
%ignore flight_hub_sdk::ILiveRecordModule::listenRecordState;
%ignore flight_hub_sdk::ILiveRecordModule::notifyRecordState;
%extend flight_hub_sdk::ILiveRecordModule {
    void startLiveRecordWrapper(const std::string& time_zone, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->startLiveRecord(time_zone, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void stopLiveRecordWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->stopLiveRecord([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void getDeviceLiveRecordsWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(GetDeviceLiveRecordsCallback)) {
        $self->getDeviceLiveRecords(device_sn, [callback](flight_hub_sdk::FlightHubError err, const std::vector<flight_hub_sdk::LiveRecordItem>& records) {
            if (callback) { callback->onResult(err, records); FHSDK_DELETE_CALLBACK(); }
        });
    }
    int listenRecordStateWrapper(FHSDK_CALLBACK_PARAMETER(LiveRecordStateCallback)) {
        FHSDK_SHARE_CALLBACK(LiveRecordStateCallback);
        return $self->listenRecordState([cb](const flight_hub_sdk::LiveRecordStateInfo& info) {
            if (cb) cb->onRecordState(info);
        });
    }
}

%extend flight_hub_sdk::IFlyToPointModule {
    void startWrapper(const flight_hub_sdk::FlyToPointParams& params, FHSDK_CALLBACK_PARAMETER(FlyToPointStartCallback)) {
        $self->start(params, [callback](flight_hub_sdk::FlightHubError err, const std::string& fly_to_id) {
            if (callback) {
                callback->onFlyToPointStart(err, fly_to_id);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void startWithResultWrapper(const flight_hub_sdk::FlyToPointParams& params, FHSDK_CALLBACK_PARAMETER(FlyToPointStartResultCallback)) {
        $self->startWithResult(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlyToPointStartResult& result) {
            if (callback) {
                callback->onFlyToPointStartResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void drcStopWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int subscribeFlyToDroneSelfPlanPathWrapper(FHSDK_CALLBACK_PARAMETER(FlyToDroneSelfPlanPathCallback)) {
        FHSDK_SHARE_CALLBACK(FlyToDroneSelfPlanPathCallback);
        return $self->subscribeFlyToDroneSelfPlanPath([cb](const flight_hub_sdk::FlyToDroneSelfPlanPath& fly_to_drone_self_plan_path) {
            if (cb) cb->onFlyToDroneSelfPlanPath(fly_to_drone_self_plan_path);
        });
    }

    int subscribeFlyToPointProgressWrapper(FHSDK_CALLBACK_PARAMETER(FlyToPointProgressCallback)) {
        FHSDK_SHARE_CALLBACK(FlyToPointProgressCallback);
        return $self->subscribeFlyToPointProgress([cb](const flight_hub_sdk::FlyToPointProgress& fly_to_point_progress) {
            if (cb) cb->onFlyToPointProgress(fly_to_point_progress);
        });
    }

    void updateTargetPointsWrapper(const flight_hub_sdk::FlyToUpdateTargetPointsParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->updateTargetPoints(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void unsubscribeFlyToDroneSelfPlanPathWrapper(int listen_id) {
        $self->unsubscribeFlyToDroneSelfPlanPath(listen_id);
    }

    void unsubscribeFlyToPointProgressWrapper(int listen_id) {
        $self->unsubscribeFlyToPointProgress(listen_id);
    }

    int listenFlyToTaskEventWrapper(FHSDK_CALLBACK_PARAMETER(FlyToTaskEventCallback)) {
        FHSDK_SHARE_CALLBACK(FlyToTaskEventCallback);
        return $self->listenFlyToTaskEvent([cb](const flight_hub_sdk::FlyToTaskEvent& event) {
            if (cb) cb->onFlyToTaskEvent(event);
        });
    }

    void cancelListenFlyToTaskEventWrapper(int listen_id) {
        $self->cancelListenFlyToTaskEvent(listen_id);
    }
}

%extend flight_hub_sdk::IPoiModule {
    void enterWrapper(const flight_hub_sdk::POIEnterParams& params, FHSDK_CALLBACK_PARAMETER(PoiEnterCallback)) {
        $self->enter(params, [callback](flight_hub_sdk::FlightHubError err, const std::string& task_uuid) {
            if (callback) {
                callback->onPoiEnter(err, task_uuid);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void exitWrapper(FHSDK_CALLBACK_PARAMETER(PoiExitCallback)) {
        $self->exit([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onPoiExit(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void updateSpeedWrapper(double speed, FHSDK_CALLBACK_PARAMETER(PoiUpdateSpeedCallback)) {
        $self->updateSpeed(speed, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onPoiUpdateSpeed(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getTaskWrapper(FHSDK_CALLBACK_PARAMETER(PoiGetTaskCallback)) {
        $self->getTask([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::POITaskInfo& task) {
            if (callback) {
                callback->onPoiGetTask(err, task);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenPoiTaskEventWrapper(FHSDK_CALLBACK_PARAMETER(PoiTaskEventCallback)) {
        FHSDK_SHARE_CALLBACK(PoiTaskEventCallback);
        return $self->listenPoiTaskEvent([cb](const flight_hub_sdk::PoiTaskEvent& event) {
            if (cb) cb->onPoiTaskEvent(event);
        });
    }

    void cancelListenPoiTaskEventWrapper(int listen_id) {
        $self->cancelListenPoiTaskEvent(listen_id);
    }

    int listenFlightPoiProgressWrapper(FHSDK_CALLBACK_PARAMETER(FlightPoiProgressCallback)) {
        FHSDK_SHARE_CALLBACK(FlightPoiProgressCallback);
        return $self->listenFlightPoiProgress([cb](const flight_hub_sdk::FlightPoiProgress& msg) {
            if (cb) cb->onFlightPoiProgress(msg);
        });
    }
    void cancelListenFlightPoiProgressWrapper(int listen_id) {
        $self->cancelListenFlightPoiProgress(listen_id);
    }

}

%extend flight_hub_sdk::IFlightTaskControlModule {
    int listenWaylineTaskEventWrapper(FHSDK_CALLBACK_PARAMETER(WaylineTaskEventCallback)) {
        FHSDK_SHARE_CALLBACK(WaylineTaskEventCallback);
        return $self->listenWaylineTaskEvent([cb](const flight_hub_sdk::WaylineTaskEvent& event) {
            if (cb) cb->onWaylineTaskEvent(event);
        });
    }

    void cancelListenWaylineTaskEventWrapper(int listen_id) {
        $self->cancelListenWaylineTaskEvent(listen_id);
    }

    int listenReturnHomeInfoEventWrapper(FHSDK_CALLBACK_PARAMETER(ReturnHomeInfoEventCallback)) {
        FHSDK_SHARE_CALLBACK(ReturnHomeInfoEventCallback);
        return $self->listenReturnHomeInfoEvent([cb](const flight_hub_sdk::ReturnHomeInfoEvent& event) {
            if (cb) cb->onReturnHomeInfoEvent(event);
        });
    }

    int listenReturnHomeInfoEventByDeviceWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(ReturnHomeInfoEventCallback)) {
        FHSDK_SHARE_CALLBACK(ReturnHomeInfoEventCallback);
        return $self->listenReturnHomeInfoEventByDevice(device_sn, [cb](const flight_hub_sdk::ReturnHomeInfoEvent& event) {
            if (cb) cb->onReturnHomeInfoEvent(event);
        });
    }

    void cancelListenReturnHomeInfoEventWrapper(int listen_id) {
        $self->cancelListenReturnHomeInfoEvent(listen_id);
    }

    void cancelListenReturnHomeInfoEventByDeviceWrapper(int listen_id) {
        $self->cancelListenReturnHomeInfoEventByDevice(listen_id);
    }

    int listenTaskExceptionEventWrapper(FHSDK_CALLBACK_PARAMETER(TaskExceptionEventCallback)) {
        FHSDK_SHARE_CALLBACK(TaskExceptionEventCallback);
        return $self->listenTaskExceptionEvent([cb](const flight_hub_sdk::TaskExceptionEvent& event) {
            if (cb) cb->onTaskExceptionEvent(event);
        });
    }

    void cancelListenTaskExceptionEventWrapper(int listen_id) {
        $self->cancelListenTaskExceptionEvent(listen_id);
    }

    void pauseWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->pause([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void resumeWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->resume([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
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
    void startWrapper(const std::string& stream_url, FHSDK_CALLBACK_PARAMETER(PullStreamStartCallback)) {
        $self->start(stream_url, [callback](flight_hub_sdk::FlightHubSdkErrorCode err_code) {
            if (callback) {
                callback->onPullStreamStart(err_code);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int subscribeStreamWrapper(FHSDK_CALLBACK_PARAMETER(PullStreamDataCallback)) {
        FHSDK_SHARE_CALLBACK(PullStreamDataCallback);
        return $self->subscribeStream([cb](const std::vector<uint8_t>& data, const flight_hub_sdk::FrameInfo& frame_info) {
            std::vector<unsigned char> data_vector(data.begin(), data.end());
            if (cb) cb->onPullStreamData(data_vector, frame_info);
        });
    }

    void setErrorCallbackWrapper(FHSDK_CALLBACK_PARAMETER(PullStreamErrorCallback)) {
        $self->setErrorCallback([callback](int error_code, const std::string& error_message) {
            if (callback) {
                callback->onPullStreamError(error_code, error_message);
            }
        });
    }
}

%extend flight_hub_sdk::IDeviceDebugModule {
    void openDebugModeWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(OpenDebugModeCallback)) {
        $self->openDebugMode(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDebugModeResult& result) {
            if (callback) {
                callback->onOpenDebugMode(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void closeDebugModeWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(CloseDebugModeCallback)) {
        $self->closeDebugMode(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDebugModeResult& result) {
            if (callback) {
                callback->onCloseDebugMode(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void openDockCoverWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(OpenDockCoverCallback)) {
        $self->openDockCover(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDockCoverResult& result) {
            if (callback) {
                callback->onOpenDockCover(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void openDockCoverProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(OpenDockCoverProgressCallback)) {
        $self->getOpenDockCoverProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDockCoverProgressResult& result) {
            if (callback) {
                callback->onOpenDockCoverProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void closeDockCoverWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(CloseDockCoverCallback)) {
        $self->closeDockCover(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDockCoverResult& result) {
            if (callback) {
                callback->onCloseDockCover(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void closeDockCoverProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(CloseDockCoverProgressCallback)) {
        $self->getCloseDockCoverProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDockCoverProgressResult& result) {
            if (callback) {
                callback->onCloseDockCoverProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void deviceRebootWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceRebootCallback)) {
        $self->deviceReboot(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceRebootResult& result) {
            if (callback) {
                callback->onDeviceReboot(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void deviceRebootProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceRebootProgressCallback)) {
        $self->getDeviceRebootProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceRebootProgressResult& result) {
            if (callback) {
                callback->onDeviceRebootProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void deviceFormatWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceFormatCallback)) {
        $self->deviceFormat(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceFormatResult& result) {
            if (callback) {
                callback->onDeviceFormat(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void deviceFormatProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceFormatProgressCallback)) {
        $self->getDeviceFormatProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, const DeviceFormatProgressResult& result) {
            if (callback) {
                callback->onDeviceFormatProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void openDroneWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(OpenDroneCallback)) {
        $self->openDrone(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenDroneResult& result) {
            if (callback) {
                callback->onOpenDrone(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void closeDroneWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(CloseDroneCallback)) {
        $self->closeDrone(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseDroneResult& result) {
            if (callback) {
                callback->onCloseDrone(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void openChargeWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(OpenChargeCallback)) {
        $self->openCharge(device_sn, [callback](flight_hub_sdk::FlightHubError err, const OpenChargeResult& result) {
            if (callback) {
                callback->onOpenCharge(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void closeChargeWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(CloseChargeCallback)) {
        $self->closeCharge(device_sn, [callback](flight_hub_sdk::FlightHubError err, const CloseChargeResult& result) {
            if (callback) {
                callback->onCloseCharge(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDockAirConditionerModeWrapper(const std::string& device_sn, int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setDockAirConditionerMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDockSilentModeWrapper(const std::string& device_sn, int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setDockSilentMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setObstacleAvoidanceWrapper(const std::string& device_sn, int obstacle_avoidance_state, FHSDK_CALLBACK_PARAMETER(SetObstacleAvoidanceCallback)) {
        $self->setObstacleAvoidance(device_sn, obstacle_avoidance_state, [callback](flight_hub_sdk::FlightHubError err, SetObstacleAvoidanceResult result) {
            if (callback) {
                callback->onSetObstacleAvoidance(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDistanceLimitWrapper(const std::string& device_sn, int distance_limit_state, int distance_limit_value, FHSDK_CALLBACK_PARAMETER(SetDistanceLimitCallback)) {
        $self->setDistanceLimit(device_sn, distance_limit_state, distance_limit_value, [callback](flight_hub_sdk::FlightHubError err, SetDistanceLimitResult result) {
            if (callback) {
                callback->onSetDistanceLimit(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setHeightLimitWrapper(const std::string& device_sn, int height_limit, FHSDK_CALLBACK_PARAMETER(SetHeightLimitCallback)) {
        $self->setHeightLimit(device_sn, height_limit, [callback](flight_hub_sdk::FlightHubError err, SetHeightLimitResult result) {
            if (callback) {
                callback->onSetHeightLimit(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setRemainingPowerForRthWrapper(const std::string& device_sn, int remaining_power_for_rth, FHSDK_CALLBACK_PARAMETER(SetRemainingPowerForRthCallback)) {
        $self->setRemainingPowerForRth(device_sn, remaining_power_for_rth, [callback](flight_hub_sdk::FlightHubError err, SetRemainingPowerForRthResult result) {
            if (callback) {
                callback->onSetRemainingPowerForRth(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setNightLightsStateWrapper(const std::string& device_sn, int night_lights_state, FHSDK_CALLBACK_PARAMETER(SetNightLightsStateCallback)) {
        $self->setNightLightsState(device_sn, night_lights_state, [callback](flight_hub_sdk::FlightHubError err, SetNightLightsStateResult result) {
            if (callback) {
                callback->onSetNightLightsState(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void switchAlarmLightStateWrapper(const std::string& device_sn, int alarm_state, FHSDK_CALLBACK_PARAMETER(SwitchAlarmStateCallback)) {
        $self->switchAlarmLightState(device_sn, alarm_state, [callback](flight_hub_sdk::FlightHubError err, SwitchAlarmStateResult result) {
            if (callback) {
                callback->onSwitchAlarmState(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void esimOperatorSwitchWrapper(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, FHSDK_CALLBACK_PARAMETER(EsimOperatorSwitchCallback)) {
        $self->esimOperatorSwitch(device_sn, imei, operator_type, device_type, [callback](flight_hub_sdk::FlightHubError err, EsimOperatorSwitchResult result) {
            if (callback) {
                callback->onEsimOperatorSwitch(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void simSlotSwitchWrapper(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, FHSDK_CALLBACK_PARAMETER(SimSlotSwitchCallback)) {
        $self->simSlotSwitch(device_sn, imei, sim_slot, device_type, [callback](flight_hub_sdk::FlightHubError err, SimSlotSwitchResult result) {
            if (callback) {
                callback->onSimSlotSwitch(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setBatteryStoreModeWrapper(const std::string& device_sn, int battery_store_mode, FHSDK_CALLBACK_PARAMETER(SetBatteryStoreModeCallback)) {
        $self->setBatteryStoreMode(device_sn, battery_store_mode, [callback](flight_hub_sdk::FlightHubError err, SetBatteryStoreModeResult result) {
            if (callback) {
                callback->onSetBatteryStoreMode(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void startManualBatterySwapWrapper(const std::string& device_sn, int slot_id, FHSDK_CALLBACK_PARAMETER(ManualBatterySwapCallback)) {
        $self->startManualBatterySwap(device_sn, slot_id, [callback](flight_hub_sdk::FlightHubError err, ManualBatterySwapResult result) {
            if (callback) {
                callback->onManualBatterySwap(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getManualBatterySwapProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(ManualBatterySwapProgressCallback)) {
        $self->getManualBatterySwapProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, ManualBatterySwapProgressResult result) {
            if (callback) {
                callback->onManualBatterySwapProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setDonglePowerSwitchWrapper(const std::string& device_sn, int power_state, FHSDK_CALLBACK_PARAMETER(DonglePowerSwitchCallback)) {
        $self->setDonglePowerSwitch(device_sn, power_state, [callback](flight_hub_sdk::FlightHubError err, DonglePowerSwitchResult result) {
            if (callback) {
                callback->onDonglePowerSwitch(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setLowPowerSwitchWrapper(const std::string& device_sn, int low_power_mode, FHSDK_CALLBACK_PARAMETER(LowPowerSwitchCallback)) {
        $self->setLowPowerSwitch(device_sn, low_power_mode, [callback](flight_hub_sdk::FlightHubError err, LowPowerSwitchResult result) {
            if (callback) {
                callback->onLowPowerSwitch(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getEsimOperatorSwitchProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(EsimOperatorSwitchProgressCallback)) {
        $self->getEsimOperatorSwitchProgress(device_sn, [callback](flight_hub_sdk::FlightHubError err, EsimOperatorSwitchProgressResult result) {
            if (callback) {
                callback->onEsimOperatorSwitchProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // Geo Caging wrapper
    void setGeoCagingEnableWrapper(const std::string& device_sn, int geo_caging_switch, const std::vector<flight_hub_sdk::GeoCagingInfoParam>& geo_caging_infos, FHSDK_CALLBACK_PARAMETER(SetGeoCagingEnableCallback)) {
        $self->setGeoCagingEnable(device_sn, geo_caging_switch, geo_caging_infos, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetGeoCagingEnable(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int subscribeDockCoverOpenProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDockCoverOpenProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDockCoverOpenProgressCallback);
        return $self->subscribeDockCoverOpenProgress(device_sn, [cb](flight_hub_sdk::OpenDockCoverResult result) {
            if (cb) cb->onSubscribeDockCoverOpenProgress(result);
        });
    }

    void unsubscribeDockCoverOpenProgressWrapper(int listen_id) {
        $self->unsubscribeDockCoverOpenProgress(listen_id);
    }

    int subscribeDockCoverCloseProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDockCoverCloseProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDockCoverCloseProgressCallback);
        return $self->subscribeDockCoverCloseProgress(device_sn, [cb](flight_hub_sdk::CloseDockCoverResult result) {
            if (cb) cb->onSubscribeDockCoverCloseProgress(result);
        });
    }

    void unsubscribeDockCoverCloseProgressWrapper(int listen_id) {
        $self->unsubscribeDockCoverCloseProgress(listen_id);
    }

    int subscribeDeviceRebootProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDeviceRebootProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDeviceRebootProgressCallback);
        return $self->subscribeDeviceRebootProgress(device_sn, [cb](flight_hub_sdk::DeviceRebootResult result) {
            if (cb) cb->onSubscribeDeviceRebootProgress(result);
        });
    }

    void unsubscribeDeviceRebootProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceRebootProgress(listen_id);
    }

    int subscribeDeviceFormatProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDeviceFormatProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDeviceFormatProgressCallback);
        return $self->subscribeDeviceFormatProgress(device_sn, [cb](flight_hub_sdk::DeviceFormatResult result) {
            if (cb) cb->onSubscribeDeviceFormatProgress(result);
        });
    }

    void unsubscribeDeviceFormatProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceFormatProgress(listen_id);
    }

    int subscribeDroneOpenProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDroneOpenProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDroneOpenProgressCallback);
        return $self->subscribeDroneOpenProgress(device_sn, [cb](flight_hub_sdk::OpenDroneResult result) {
            if (cb) cb->onSubscribeDroneOpenProgress(result);
        });
    }

    void unsubscribeDroneOpenProgressWrapper(int listen_id) {
        $self->unsubscribeDroneOpenProgress(listen_id);
    }

    int subscribeDroneCloseProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDroneCloseProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDroneCloseProgressCallback);
        return $self->subscribeDroneCloseProgress(device_sn, [cb](flight_hub_sdk::CloseDroneResult result) {
            if (cb) cb->onSubscribeDroneCloseProgress(result);
        });
    }

    void unsubscribeDroneCloseProgressWrapper(int listen_id) {
        $self->unsubscribeDroneCloseProgress(listen_id);
    }

    int subscribeDroneFormatProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDroneFormatProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDroneFormatProgressCallback);
        return $self->subscribeDroneFormatProgress(device_sn, [cb](flight_hub_sdk::DroneFormatResult result) {
            if (cb) cb->onSubscribeDroneFormatProgress(result);
        });
    }

    void unsubscribeDroneFormatProgressWrapper(int listen_id) {
        $self->unsubscribeDroneFormatProgress(listen_id);
    }

    int subscribeDroneChargeOpenProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDroneChargeOpenProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDroneChargeOpenProgressCallback);
        return $self->subscribeDroneChargeOpenProgress(device_sn, [cb](flight_hub_sdk::OpenChargeResult result) {
            if (cb) cb->onSubscribeDroneChargeOpenProgress(result);
        });
    }

    void unsubscribeDroneChargeOpenProgressWrapper(int listen_id) {
        $self->unsubscribeDroneChargeOpenProgress(listen_id);
    }

    int subscribeDroneChargeCloseProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDroneChargeCloseProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDroneChargeCloseProgressCallback);
        return $self->subscribeDroneChargeCloseProgress(device_sn, [cb](flight_hub_sdk::CloseChargeResult result) {
            if (cb) cb->onSubscribeDroneChargeCloseProgress(result);
        });
    }

    void unsubscribeDroneChargeCloseProgressWrapper(int listen_id) {
        $self->unsubscribeDroneChargeCloseProgress(listen_id);
    }

    int subscribeDeviceEsimOperatorSwitchProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDeviceEsimOperatorSwitchProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDeviceEsimOperatorSwitchProgressCallback);
        return $self->subscribeDeviceEsimOperatorSwitchProgress(device_sn, [cb](flight_hub_sdk::EsimOperatorSwitchResult result) {
            if (cb) cb->onSubscribeDeviceEsimOperatorSwitchProgress(result);
        });
    }

    void unsubscribeDeviceEsimOperatorSwitchProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceEsimOperatorSwitchProgress(listen_id);
    }

    int subscribeDeviceSimSlotSwitchProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(SubscribeDeviceSimSlotSwitchProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeDeviceSimSlotSwitchProgressCallback);
        return $self->subscribeDeviceSimSlotSwitchProgress(device_sn, [cb](flight_hub_sdk::SimSlotSwitchResult result) {
            if (cb) cb->onSubscribeDeviceSimSlotSwitchProgress(result);
        });
    }

    void unsubscribeDeviceSimSlotSwitchProgressWrapper(int listen_id) {
        $self->unsubscribeDeviceSimSlotSwitchProgress(listen_id);
    }

    void droneFormatWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DroneFormatCallback)) {
        $self->droneFormat(device_sn, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DroneFormatResult& result) {
            if (callback) {
                callback->onDroneFormat(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void droneSdrWorkModeWrapper(const std::string& device_sn, int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->droneSdrWorkMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // 云环境切换 wrapper
    void cloudEnvironmentSwitchWrapper(const flight_hub_sdk::CloudEnvironmentSwitchParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->cloudEnvironmentSwitch(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void cloudEnvironmentSwitchCallbackWrapper(const flight_hub_sdk::CloudEnvironmentSwitchCallbackParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->cloudEnvironmentSwitchCallback(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenDeviceCloudEnvironmentSwitchProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceCloudEnvironmentSwitchProgressCallback)) {
        FHSDK_SHARE_CALLBACK(DeviceCloudEnvironmentSwitchProgressCallback);
        return $self->listenDeviceCloudEnvironmentSwitchProgress(device_sn, [cb](flight_hub_sdk::CloudEnvironmentSwitchResult result) {
            if (cb) cb->onDeviceCloudEnvironmentSwitchProgress(result);
        });
    }

    void cancelListenDeviceCloudEnvironmentSwitchProgressWrapper(int listen_id) {
        $self->cancelListenDeviceCloudEnvironmentSwitchProgress(listen_id);
    }
}

%extend flight_hub_sdk::IRelayControlModule {
    void setRelayBroadcastHideWrapper(bool is_broadcast_hide, FHSDK_CALLBACK_PARAMETER(SetRelayBroadcastHideCallback)) {
        $self->setRelayBroadcastHide(is_broadcast_hide, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelayBroadcastHide(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setRelaySSIDWrapper(const std::string& device_name, FHSDK_CALLBACK_PARAMETER(SetRelaySSIDCallback)) {
        $self->setRelaySSID(device_name, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelaySSID(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setRelayPasswordWrapper(const std::string& passwd, FHSDK_CALLBACK_PARAMETER(SetRelayPasswordCallback)) {
        $self->setRelayPassword(passwd, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelayPassword(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setRelaySearchModeWrapper(int relay_search_mode, FHSDK_CALLBACK_PARAMETER(SetRelaySearchModeCallback)) {
        $self->setRelaySearchMode(relay_search_mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSetRelaySearchMode(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void setRelayOccupiedStatusWrapper(int occupied_status, FHSDK_CALLBACK_PARAMETER(SetRelayOccupiedStatusCallback)) {
        $self->setRelayOccupiedStatus(occupied_status, [callback](flight_hub_sdk::FlightHubError err, SetRelayOccupiedStatusResult result) {
            if (callback) {
                callback->onSetRelayOccupiedStatus(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void relayRebootWrapper(FHSDK_CALLBACK_PARAMETER(RelayRebootCallback)) {
        $self->relayReboot([callback](flight_hub_sdk::FlightHubError err, RelayRebootResult result) {
            if (callback) {
                callback->onRelayReboot(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void relayDisconnectWrapper(FHSDK_CALLBACK_PARAMETER(RelayDisconnectCallback)) {
        $self->relayDisconnect([callback](flight_hub_sdk::FlightHubError err, RelayDisconnectResult result) {
            if (callback) {
                callback->onRelayDisconnect(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%extend flight_hub_sdk::IRtkCalibrationModule {
    void calibrationByNetworkWrapper(const NetworkRtkCalibrationParams& params, FHSDK_CALLBACK_PARAMETER(RtkCalibrationCallback)) {
        $self->calibrationByNetwork(params, [callback](flight_hub_sdk::FlightHubError err, RtkCalibrationResult result) {
            if (callback) {
                callback->onRtkCalibration(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void calibrationByManualWrapper(const ManulRtkCalibrationParams& params, FHSDK_CALLBACK_PARAMETER(RtkCalibrationCallback)) {
        $self->calibrationByManual(params, [callback](flight_hub_sdk::FlightHubError err, RtkCalibrationResult result) {
            if (callback) {
                callback->onRtkCalibration(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int subscribeCalibrationProgressWrapper(FHSDK_CALLBACK_PARAMETER(SubscribeCalibrationProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeCalibrationProgressCallback);
        return $self->subscribeCalibrationProgress([cb](flight_hub_sdk::RtkCalibrationResult result) {
            if (cb) cb->onSubscribeCalibrationProgress(result);
        });
    }

    void unsubscribeCalibrationProgressWrapper(int listen_id) {
        $self->unsubscribeCalibrationProgress(listen_id);
    }
}

%extend flight_hub_sdk::IDeviceStateModule {
    int listenDockOnlineStatusWrapper(FHSDK_CALLBACK_PARAMETER(DockOnlineStatusCallback)) {
        FHSDK_SHARE_CALLBACK(DockOnlineStatusCallback);
        return $self->listenDockOnlineStatus([cb](const flight_hub_sdk::DockOnlineStatus& status) {
            if (cb) cb->onDockOnlineStatus(status);
        });
    }

    void cancelListenDockOnlineStatusWrapper(int listen_id) {
        $self->cancelListenDockOnlineStatus(listen_id);
    }

    int listenDroneOnlineStatusWrapper(FHSDK_CALLBACK_PARAMETER(DroneOnlineStatusCallback)) {
        FHSDK_SHARE_CALLBACK(DroneOnlineStatusCallback);
        return $self->listenDroneOnlineStatus([cb](const flight_hub_sdk::DroneOnlineStatus& status) {
            if (cb) cb->onDroneOnlineStatus(status);
        });
    }

    void cancelListenDroneOnlineStatusWrapper(int listen_id) {
        $self->cancelListenDroneOnlineStatus(listen_id);
    }
}

%extend flight_hub_sdk::IDeviceHMSModule {
    int listenDeviceHMSWrapper(FHSDK_CALLBACK_PARAMETER(HmsDataCallback)) {
        FHSDK_SHARE_CALLBACK(HmsDataCallback);
        return $self->listenDeviceHMS([cb](const flight_hub_sdk::HmsData& hms_data) {
            if (cb) cb->onHmsData(hms_data);
        });
    }

    void cancelListenDeviceHMSWrapper(int listen_id) {
        $self->cancelListenDeviceHMS(listen_id);
    }
}

%extend flight_hub_sdk::IAirsenseWarningModule {
    int listenAirsenseWarningWrapper(FHSDK_CALLBACK_PARAMETER(AirsenseWarningCallback)) {
        FHSDK_SHARE_CALLBACK(AirsenseWarningCallback);
        return $self->listenAirsenseWarning([cb](const flight_hub_sdk::AirsenseWarning& msg) {
            if (cb) cb->onAirsenseWarning(msg);
        });
    }

    void cancelListenAirsenseWarningWrapper(int listen_id) {
        $self->cancelListenAirsenseWarning(listen_id);
    }
}

%extend flight_hub_sdk::ILiveDeviceModule {
    int listenLiveAvailableRangeWrapper(FHSDK_CALLBACK_PARAMETER(LiveAvailableRangeCallback)) {
        FHSDK_SHARE_CALLBACK(LiveAvailableRangeCallback);
        return $self->listenLiveAvailableRange(
            [cb](const std::vector<flight_hub_sdk::LiveInfo>& live_infos) {
                if (cb) cb->onLiveAvailableRange(live_infos);
            });
    }

    void cancelListenLiveAvailableRangeWrapper(int listen_id) {
        $self->cancelListenLiveAvailableRange(listen_id);
    }

    void getAudienceCountWrapper(const flight_hub_sdk::AudienceCountGroup* group, FHSDK_CALLBACK_PARAMETER(GetAudienceCountCallback)) {
        $self->getAudienceCount(group, [callback](flight_hub_sdk::FlightHubError err, const std::vector<flight_hub_sdk::DeviceAudienceCountItem>& result) {
            if (callback) {
                callback->onGetAudienceCount(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // EA340 消息推送补全（Phase D）;
    int listenDockLiveStatusWrapper(FHSDK_CALLBACK_PARAMETER(DockLiveStatusCallback)) {
        FHSDK_SHARE_CALLBACK(DockLiveStatusCallback);
        return $self->listenDockLiveStatus([cb](const flight_hub_sdk::DockLiveStatus& msg) {
            if (cb) cb->onDockLiveStatus(msg);
        });
    }

    void cancelListenDockLiveStatusWrapper(int listen_id) {
        $self->cancelListenDockLiveStatus(listen_id);
    }

    int listenDockLiveCapacityWrapper(FHSDK_CALLBACK_PARAMETER(DockLiveCapacityCallback)) {
        FHSDK_SHARE_CALLBACK(DockLiveCapacityCallback);
        return $self->listenDockLiveCapacity([cb](const flight_hub_sdk::DockLiveCapacity& msg) {
            if (cb) cb->onDockLiveCapacity(msg);
        });
    }

    void cancelListenDockLiveCapacityWrapper(int listen_id) {
        $self->cancelListenDockLiveCapacity(listen_id);
    }
}

%extend flight_hub_sdk::IGroundFlightModule {
    void groundTakeoffWrapper(const flight_hub_sdk::GroundTakeoffParams& params, FHSDK_CALLBACK_PARAMETER(GroundTakeoffResultCallback)) {
        $self->groundTakeoff(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GroundTakeoffResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void groundLandingWrapper(int cmd, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->groundLanding(cmd, [callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%extend flight_hub_sdk::ITakeoffModule {
    void takeoffWrapper(const flight_hub_sdk::TakeoffParams& params, FHSDK_CALLBACK_PARAMETER(TakeoffResultCallback)) {
        $self->takeoff(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TakeoffResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenCommandTaskEventWrapper(FHSDK_CALLBACK_PARAMETER(CommandTaskEventCallback)) {
        FHSDK_SHARE_CALLBACK(CommandTaskEventCallback);
        return $self->listenCommandTaskEvent(
            [cb](const flight_hub_sdk::CommandTaskEvent& event) {
                if (cb) cb->onCommandTaskEvent(event);
            });
    }

    void cancelListenCommandTaskEventWrapper(int listen_id) {
        $self->cancelListenCommandTaskEvent(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDroneCommanderFlightModeWrapper(FHSDK_CALLBACK_PARAMETER(DroneCommanderFlightModeCallback)) {
        FHSDK_SHARE_CALLBACK(DroneCommanderFlightModeCallback);
        return $self->listenDroneCommanderFlightMode([cb](const flight_hub_sdk::DroneCommanderFlightMode& msg) {
            if (cb) cb->onDroneCommanderFlightMode(msg);
        });
    }

    void cancelListenDroneCommanderFlightModeWrapper(int listen_id) {
        $self->cancelListenDroneCommanderFlightMode(listen_id);
    }

    int listenDroneCurrentCommanderFlightModeWrapper(FHSDK_CALLBACK_PARAMETER(DroneCurrentCommanderFlightModeCallback)) {
        FHSDK_SHARE_CALLBACK(DroneCurrentCommanderFlightModeCallback);
        return $self->listenDroneCurrentCommanderFlightMode([cb](const flight_hub_sdk::DroneCurrentCommanderFlightMode& msg) {
            if (cb) cb->onDroneCurrentCommanderFlightMode(msg);
        });
    }

    void cancelListenDroneCurrentCommanderFlightModeWrapper(int listen_id) {
        $self->cancelListenDroneCurrentCommanderFlightMode(listen_id);
    }

    int listenDroneCommanderModeLostActionWrapper(FHSDK_CALLBACK_PARAMETER(DroneCommanderModeLostActionCallback)) {
        FHSDK_SHARE_CALLBACK(DroneCommanderModeLostActionCallback);
        return $self->listenDroneCommanderModeLostAction([cb](const flight_hub_sdk::DroneCommanderModeLostAction& msg) {
            if (cb) cb->onDroneCommanderModeLostAction(msg);
        });
    }

    void cancelListenDroneCommanderModeLostActionWrapper(int listen_id) {
        $self->cancelListenDroneCommanderModeLostAction(listen_id);
    }
}

%extend flight_hub_sdk::IInFlightWaylineModule {
    void createInFlightWaylineTaskWrapper(const flight_hub_sdk::CreateInFlightWaylineTaskParams& params, FHSDK_CALLBACK_PARAMETER(CreateInFlightWaylineTaskCallback)) {
        $self->createInFlightWaylineTask(params, [callback](const flight_hub_sdk::CreateInFlightWaylineTaskResult& result, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(result, err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void controlInFlightWaylineTaskWrapper(flight_hub_sdk::InFlightWaylineControlAction action, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->controlInFlightWaylineTask(action, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getActiveInFlightWaylineTaskWrapper(FHSDK_CALLBACK_PARAMETER(GetActiveInFlightWaylineTaskCallback)) {
        $self->getActiveInFlightWaylineTask([callback](const flight_hub_sdk::ActiveInFlightWaylineTaskInfo& info, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(info, err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenInFlightWaylineProgressWrapper(FHSDK_CALLBACK_PARAMETER(InFlightWaylineProgressCallback)) {
        FHSDK_SHARE_CALLBACK(InFlightWaylineProgressCallback);
        return $self->listenInFlightWaylineProgress([cb](const flight_hub_sdk::InFlightWaylineProgress& progress) {
            if (cb) cb->onInFlightWaylineProgress(progress);
        });
    }

    void cancelListenInFlightWaylineProgressWrapper(int listen_id) {
        $self->cancelListenInFlightWaylineProgress(listen_id);
    }
}

%extend flight_hub_sdk::IFlightControlModule {
    void drcEmergencyStopWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcEmergencyStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void drcEmergencyLandingWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcEmergencyLanding([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void drcForceLandingWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcForceLanding([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void returnHomeWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->returnHome([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void returnHomeToDockWrapper(const std::string& target_dock_sn, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->returnHome(target_dock_sn, [callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void returnHomeCancelWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->returnHomeCancel([callback](const flight_hub_sdk::FlightHubError& err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void drcSendStickControlWrapper(const flight_hub_sdk::StickControlData& data) {
        $self->drcSendStickControl(data);
    }

    // ══════════════════════════════════════════════
    // Flight Control Auxiliary Wrappers
    // ══════════════════════════════════════════════

    void drcSetDroneHoverWrapper(int duration, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetDroneHover(duration, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcSetFollowMouseParameterWrapper(int speed, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetFollowMouseParameter(speed, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcSetFlyingMaxSpeedWrapper(double speed, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetFlyingMaxSpeed(speed, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcSetNightLightsStateWrapper(int night_lights_state, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetNightLightsState(night_lights_state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcSetStealthStateWrapper(int stealth_state, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetStealthState(stealth_state, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcOpenParachuteWrapper(int force, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcOpenParachute(force, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcSetFlashLedWrapper(const flight_hub_sdk::FlashLedParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcSetFlashLed(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void drcCameraLookAtWrapper(const flight_hub_sdk::CameraLookAtParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcCameraLookAt(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    // ══════════════════════════════════════════════
    // Northbound Push Listener Wrappers
    // ══════════════════════════════════════════════

    int listenDrcOsdInfoWrapper(FHSDK_CALLBACK_PARAMETER(DrcOsdInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DrcOsdInfoCallback);
        return $self->listenDrcOsdInfo([cb](const flight_hub_sdk::DrcOsdInfo& info) {
            if (cb) cb->onDrcOsdInfo(info);
        });
    }

    void cancelListenDrcOsdInfoWrapper(int listen_id) {
        $self->cancelListenDrcOsdInfo(listen_id);
    }

    int listenDrcBatteriesInfoWrapper(FHSDK_CALLBACK_PARAMETER(DrcBatteriesInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DrcBatteriesInfoCallback);
        return $self->listenDrcBatteriesInfo([cb](const flight_hub_sdk::DrcBatteriesInfo& info) {
            if (cb) cb->onDrcBatteriesInfo(info);
        });
    }

    void cancelListenDrcBatteriesInfoWrapper(int listen_id) {
        $self->cancelListenDrcBatteriesInfo(listen_id);
    }

    int listenDrcParachuteInfoWrapper(FHSDK_CALLBACK_PARAMETER(DrcParachuteInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DrcParachuteInfoCallback);
        return $self->listenDrcParachuteInfo([cb](const flight_hub_sdk::DrcParachuteInfo& info) {
            if (cb) cb->onDrcParachuteInfo(info);
        });
    }

    void cancelListenDrcParachuteInfoWrapper(int listen_id) {
        $self->cancelListenDrcParachuteInfo(listen_id);
    }

    int listenDrcFlashLedWrapper(FHSDK_CALLBACK_PARAMETER(FlashLedStateInfoCallback)) {
        FHSDK_SHARE_CALLBACK(FlashLedStateInfoCallback);
        return $self->listenDrcFlashLed([cb](const flight_hub_sdk::FlashLedStateInfo& info) {
            if (cb) cb->onFlashLedStateInfo(info);
        });
    }

    void cancelListenDrcFlashLedWrapper(int listen_id) {
        $self->cancelListenDrcFlashLed(listen_id);
    }

    int listenDrcLandingMissionStateWrapper(FHSDK_CALLBACK_PARAMETER(DrcLandingMissionStateCallback)) {
        FHSDK_SHARE_CALLBACK(DrcLandingMissionStateCallback);
        return $self->listenDrcLandingMissionState([cb](const flight_hub_sdk::DrcLandingMissionState& info) {
            if (cb) cb->onDrcLandingMissionState(info);
        });
    }

    void cancelListenDrcLandingMissionStateWrapper(int listen_id) {
        $self->cancelListenDrcLandingMissionState(listen_id);
    }
}

%extend flight_hub_sdk::IControlAuthModule {
    void acquireFlightControlAuthorityWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->acquireFlightControlAuthority([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void releaseFlightControlAuthorityWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->releaseFlightControlAuthority([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenFlightControlAuthorityWrapper(FHSDK_CALLBACK_PARAMETER(FlightControlAuthorityCallback)) {
        FHSDK_SHARE_CALLBACK(FlightControlAuthorityCallback);
        return $self->listenFlightControlAuthority([cb](bool available) {
            if (cb) cb->onFlightControlAuthority(available);
        });
    }

    void acquirePayloadControlAuthorityWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->acquirePayloadControlAuthority(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void releasePayloadControlAuthorityWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->releasePayloadControlAuthority(payload_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void acquireFlightControlAuthorityWithResultWrapper(FHSDK_CALLBACK_PARAMETER(ControlAuthResultCallback)) {
        $self->acquireFlightControlAuthorityWithResult([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ControlAuthResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void releaseFlightControlAuthorityWithResultWrapper(FHSDK_CALLBACK_PARAMETER(ControlAuthResultCallback)) {
        $self->releaseFlightControlAuthorityWithResult([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ControlAuthResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void acquirePayloadControlAuthorityWithResultWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ControlAuthResultCallback)) {
        $self->acquirePayloadControlAuthorityWithResult(payload_index, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ControlAuthResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void releasePayloadControlAuthorityWithResultWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(ControlAuthResultCallback)) {
        $self->releasePayloadControlAuthorityWithResult(payload_index, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ControlAuthResult& result) {
            if (callback) {
                callback->onResult(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenPayloadControlAuthorityWrapper(const std::string& payload_index, FHSDK_CALLBACK_PARAMETER(PayloadControlAuthorityCallback)) {
        FHSDK_SHARE_CALLBACK(PayloadControlAuthorityCallback);
        return $self->listenPayloadControlAuthority(payload_index, [cb](bool available) {
            if (cb) cb->onPayloadControlAuthority(available);
        });
    }
}

%extend flight_hub_sdk::IDrcModule {
    int listenLinkStatusWrapper(FHSDK_CALLBACK_PARAMETER(LinkStatusCallback)) {
        FHSDK_SHARE_CALLBACK(LinkStatusCallback);
        return $self->listenLinkStatus([cb](const flight_hub_sdk::LinkStatus& status) {
            if (cb) cb->onLinkStatus(status);
        });
    }
    void cancelListenLinkStatusWrapper(int listen_id) {
        $self->cancelListenLinkStatus(listen_id);
    }
    void connectDrcLinkWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->connectDrcLink([callback](flight_hub_sdk::FlightHubSdkErrorCode err_code) {
            if (callback) {
                flight_hub_sdk::FlightHubError err = flight_hub_sdk::FlightHubError::from_sdk(err_code);
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
    void disconnectDrcLinkWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->disconnectDrcLink([callback](flight_hub_sdk::FlightHubSdkErrorCode err_code) {
            if (callback) {
                flight_hub_sdk::FlightHubError err = flight_hub_sdk::FlightHubError::from_sdk(err_code);
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // ══════════════════════════════════════════════
    // DRC Link Auxiliary Wrappers
    // ══════════════════════════════════════════════

    void drcRtcQualityTestWrapper(const flight_hub_sdk::RtcQualityTestParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->drcRtcQualityTest(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    int listenDrcDelayInfoWrapper(FHSDK_CALLBACK_PARAMETER(DrcDelayInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DrcDelayInfoCallback);
        return $self->listenDrcDelayInfo([cb](const flight_hub_sdk::DrcDelayInfo& info) {
            if (cb) cb->onDrcDelayInfo(info);
        });
    }

    void cancelListenDrcDelayInfoWrapper(int listen_id) {
        $self->cancelListenDrcDelayInfo(listen_id);
    }

    int listenDrcCountDownInfoWrapper(FHSDK_CALLBACK_PARAMETER(DrcCountDownInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DrcCountDownInfoCallback);
        return $self->listenDrcCountDownInfo([cb](const flight_hub_sdk::DrcCountDownInfo& info) {
            if (cb) cb->onDrcCountDownInfo(info);
        });
    }

    void cancelListenDrcCountDownInfoWrapper(int listen_id) {
        $self->cancelListenDrcCountDownInfo(listen_id);
    }
}

%extend flight_hub_sdk::ICascadeModule {
    void addChildNodeWrapper(const flight_hub_sdk::AddChildNodeRequest& req, FHSDK_CALLBACK_PARAMETER(CascadeAddChildNodeCallback)) {
        $self->addChildNode(req, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::AddChildNodeResult& result) {
            if (callback) { callback->onCascadeAddChildNode(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void enableChildNodesWrapper(const std::vector<std::string>& node_ids, FHSDK_CALLBACK_PARAMETER(CascadeEnableChildNodesCallback)) {
        $self->enableChildNodes(node_ids, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeBatchOpResult& result) {
            if (callback) { callback->onCascadeEnableChildNodes(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void disableChildNodesWrapper(const std::vector<std::string>& node_ids, FHSDK_CALLBACK_PARAMETER(CascadeDisableChildNodesCallback)) {
        $self->disableChildNodes(node_ids, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeBatchOpResult& result) {
            if (callback) { callback->onCascadeDisableChildNodes(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void deleteChildNodeWrapper(const std::string& node_id, FHSDK_CALLBACK_PARAMETER(CascadeDeleteChildNodeCallback)) {
        $self->deleteChildNode(node_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onCascadeDeleteChildNode(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getReachableChildrenWrapper(const flight_hub_sdk::CascadeChildrenQuery& query, FHSDK_CALLBACK_PARAMETER(CascadeGetReachableChildrenCallback)) {
        $self->getReachableChildren(query, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeChildNodeListResult& result) {
            if (callback) { callback->onCascadeGetReachableChildren(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getParentNodeWrapper(FHSDK_CALLBACK_PARAMETER(CascadeGetParentNodeCallback)) {
        $self->getParentNode([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeParentNodeInfo& info) {
            if (callback) { callback->onCascadeGetParentNode(err, info); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void respondToParentWrapper(const flight_hub_sdk::CascadeParentRespondRequest& req, FHSDK_CALLBACK_PARAMETER(CascadeRespondToParentCallback)) {
        $self->respondToParent(req, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onCascadeRespondToParent(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getNodeIndexWrapper(int32_t page, int32_t size, FHSDK_CALLBACK_PARAMETER(CascadeGetNodeIndexCallback)) {
        $self->getNodeIndex(page, size, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeChildNodeListResult& result) {
            if (callback) { callback->onCascadeGetNodeIndex(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getConnectivityWrapper(FHSDK_CALLBACK_PARAMETER(CascadeGetConnectivityCallback)) {
        $self->getConnectivity([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeConnectivityResult& result) {
            if (callback) { callback->onCascadeGetConnectivity(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void getParentConnectivityWrapper(FHSDK_CALLBACK_PARAMETER(CascadeGetParentConnectivityCallback)) {
        $self->getParentConnectivity([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CascadeParentConnectivity& result) {
            if (callback) { callback->onCascadeGetParentConnectivity(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void postToNodeWrapper(const std::string& dst_node_id, const std::string& body, FHSDK_CALLBACK_PARAMETER(CascadePostToNodeCallback)) {
        $self->postToNode(dst_node_id, body, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onCascadePostToNode(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    // 事件推送 topic_cascade_node_status（多订阅者，生命周期随 listen_id；不 delete callback）
    int listenNodeStatusChangedWrapper(FHSDK_CALLBACK_PARAMETER(CascadeNodeStatusEventCallback)) {
        FHSDK_SHARE_CALLBACK(CascadeNodeStatusEventCallback);
        return $self->listenNodeStatusChanged([cb](const flight_hub_sdk::CascadeNodeStatusEvent& event) {
            if (cb) cb->onCascadeNodeStatusEvent(event);
        });
    }

    void cancelListenNodeStatusChangedWrapper(int listen_id) {
        $self->cancelListenNodeStatusChanged(listen_id);
    }

    // 事件推送 topic_cascade_notify（多订阅者，生命周期随 listen_id；不 delete callback）
    int listenNotifyPayloadWrapper(FHSDK_CALLBACK_PARAMETER(CascadeNotifyEventCallback)) {
        FHSDK_SHARE_CALLBACK(CascadeNotifyEventCallback);
        return $self->listenNotifyPayload([cb](const flight_hub_sdk::CascadeNotifyEvent& event) {
            if (cb) cb->onCascadeNotifyEvent(event);
        });
    }

    void cancelListenNotifyPayloadWrapper(int listen_id) {
        $self->cancelListenNotifyPayload(listen_id);
    }
}

%extend flight_hub_sdk::IFlightSettingsModule {
    void setFlightSmartPlanModeWrapper(const std::string& device_sn, int mode, FHSDK_CALLBACK_PARAMETER(SetFlightSmartPlanModeCallback)) {
        $self->setFlightSmartPlanMode(device_sn, mode, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightSmartPlanModeResult& result) {
            if (callback) { callback->onSetFlightSmartPlanMode(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void setOutOfControlActionWrapper(const std::string& device_sn, const std::string& action, FHSDK_CALLBACK_PARAMETER(SetOutOfControlActionCallback)) {
        $self->setOutOfControlAction(device_sn, action, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightOutOfControlActionResult& result) {
            if (callback) { callback->onSetOutOfControlAction(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void setFlightTaskHeightWrapper(const std::string& device_sn, double height, FHSDK_CALLBACK_PARAMETER(SetFlightTaskHeightCallback)) {
        $self->setFlightTaskHeight(device_sn, height, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightCommanderHeightResult& result) {
            if (callback) { callback->onSetFlightTaskHeight(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void setRthAltitudeWrapper(const std::string& device_sn, int altitude, FHSDK_CALLBACK_PARAMETER(SetRthAltitudeCallback)) {
        $self->setRthAltitude(device_sn, altitude, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::FlightRthAltitudeResult& result) {
            if (callback) { callback->onSetRthAltitude(err, result); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void setAirTransferEnableWrapper(const std::string& device_sn, bool enabled, FHSDK_CALLBACK_PARAMETER(SetAirTransferEnableCallback)) {
        $self->setAirTransferEnable(device_sn, enabled, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onSetAirTransferEnable(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%extend flight_hub_sdk::ITestFlightModule {
    void generateTryFlightWaylineWrapper(const std::string& project_id, const flight_hub_sdk::GenerateTryFlightWaylineParams& params, FHSDK_CALLBACK_PARAMETER(GenerateTryFlightWaylineCallback)) {
        $self->generateTryFlightWayline(project_id, params, [callback](const flight_hub_sdk::GenerateTryFlightWaylineResponse& response, flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onGenerateTryFlightWayline(response, err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void createTestFlightTaskWrapper(const std::string& project_id, const flight_hub_sdk::CreateTestFlightTaskParams& params, FHSDK_CALLBACK_PARAMETER(CreateTestFlightTaskCallback)) {
        $self->createTestFlightTask(project_id, params, [callback](const flight_hub_sdk::CreateTestFlightTaskResponse& response, flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onCreateTestFlightTask(response, err); FHSDK_DELETE_CALLBACK(); }
        });
    }
}

%extend flight_hub_sdk::IDockOsdModule {
    int listenDockLocationWrapper(FHSDK_CALLBACK_PARAMETER(DockLocationCallback)) {
        FHSDK_SHARE_CALLBACK(DockLocationCallback);
        return $self->listenDockLocation([cb](const flight_hub_sdk::DockLocation& location) {
            if (cb) cb->onDockLocation(location);
        });
    }

    void cancelListenDockLocationWrapper(int listen_id) {
        $self->cancelListenDockLocation(listen_id);
    }

    int listenDroneInDockWrapper(FHSDK_CALLBACK_PARAMETER(DroneInDockCallback)) {
        FHSDK_SHARE_CALLBACK(DroneInDockCallback);
        return $self->listenDroneInDock([cb](const flight_hub_sdk::DroneInDock& drone_in_dock) {
            if (cb) cb->onDroneInDock(drone_in_dock);
        });
    }

    void cancelListenDroneInDockWrapper(int listen_id) {
        $self->cancelListenDroneInDock(listen_id);
    }

    // 监听机场 mode code;
    int listenDockModeCodeWrapper(FHSDK_CALLBACK_PARAMETER(DockModeCodeCallback)) {
        FHSDK_SHARE_CALLBACK(DockModeCodeCallback);
        return $self->listenDockModeCode([cb](const flight_hub_sdk::DockModeCode& dock_mode_code) {
            if (cb) cb->onDockModeCode(dock_mode_code);
        });
    }

    // 取消监听机场 mode code;
    void cancelListenDockModeCodeWrapper(int listen_id) {
        $self->cancelListenDockModeCode(listen_id);
    }

    // 监听飞行任务 step code;
    int listenFlightTaskStepCodeWrapper(FHSDK_CALLBACK_PARAMETER(FlightTaskStepCodeCallback)) {
        FHSDK_SHARE_CALLBACK(FlightTaskStepCodeCallback);
        return $self->listenFlightTaskStepCode([cb](const flight_hub_sdk::FlightTaskStepCode& flight_task_step_code) {
            if (cb) cb->onFlightTaskStepCode(flight_task_step_code);
        });
    }

    // 取消监听飞行任务 step code;
    void cancelListenFlightTaskStepCodeWrapper(int listen_id) {
        $self->cancelListenFlightTaskStepCode(listen_id);
    }

    // 机场: 子设备信息列表(sub_device);
    int listenDockSubDeviceWrapper(FHSDK_CALLBACK_PARAMETER(DockSubDeviceInfoListCallback)) {
        FHSDK_SHARE_CALLBACK(DockSubDeviceInfoListCallback);
        return $self->listenDockSubDevice([cb](const flight_hub_sdk::DockSubDeviceInfoList& dock_sub_device_info_list) {
            if (cb) cb->onDockSubDeviceInfoList(dock_sub_device_info_list);
        });
    }

    // 取消监听机场子设备信息列表(sub_device);
    void cancelListenDockSubDeviceWrapper(int listen_id) {
        $self->cancelListenDockSubDevice(listen_id);
    }

    // 监听机场网络状态(network_state);
    int listenDockNetworkStateWrapper(FHSDK_CALLBACK_PARAMETER(DockNetworkStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockNetworkStateCallback);
        return $self->listenDockNetworkState([cb](const flight_hub_sdk::DockNetworkState& dock_network_state) {
            if (cb) cb->onDockNetworkState(dock_network_state);
        });
    }

    // 取消监听机场网络状态(network_state);
    void cancelListenDockNetworkStateWrapper(int listen_id) {
        $self->cancelListenDockNetworkState(listen_id);
    }

    // 监听机场舱盖状态;
    int listenDockCoverStateWrapper(FHSDK_CALLBACK_PARAMETER(DockCoverStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockCoverStateCallback);
        return $self->listenDockCoverState([cb](const flight_hub_sdk::DockCoverState& dock_cover_state) {
            if (cb) cb->onDockCoverState(dock_cover_state);
        });
    }

    // 取消监听机场舱盖状态;
    void cancelListenDockCoverStateWrapper(int listen_id) {
        $self->cancelListenDockCoverState(listen_id);
    }

    // 监听: 机场媒体文件数量(media_file_detail);
    int listenDockMediaFileDetailWrapper(FHSDK_CALLBACK_PARAMETER(DockMediaFileDetailCallback)) {
        FHSDK_SHARE_CALLBACK(DockMediaFileDetailCallback);
        return $self->listenDockMediaFileDetail([cb](const flight_hub_sdk::DockMediaFileDetail& dock_media_file_detail) {
            if (cb) cb->onDockMediaFileDetail(dock_media_file_detail);
        });
    }

    // 取消监听: 机场媒体文件数量(media_file_detail);
    void cancelListenDockMediaFileDetailWrapper(int listen_id) {
        $self->cancelListenDockMediaFileDetail(listen_id);
    }


    // 机场: 图传质量(wireless_link);
    int listenDockWirelessLinkWrapper(FHSDK_CALLBACK_PARAMETER(DockWirelessLinkCallback)) {
        FHSDK_SHARE_CALLBACK(DockWirelessLinkCallback);
        return $self->listenDockWirelessLink([cb](const flight_hub_sdk::DockWirelessLink& dock_wireless_link) {
            if (cb) cb->onDockWirelessLink(dock_wireless_link);
        });
    }

    // 取消监听机场图传质量(wireless_link);
    void cancelListenDockWirelessLinkWrapper(int listen_id) {
        $self->cancelListenDockWirelessLink(listen_id);
    }


    // 机场: 机场作业次数(job_number);
    int listenDockJobNumberWrapper(FHSDK_CALLBACK_PARAMETER(DockJobNumberCallback)) {
        FHSDK_SHARE_CALLBACK(DockJobNumberCallback);
        return $self->listenDockJobNumber([cb](const flight_hub_sdk::DockJobNumber& dock_job_number) {
            if (cb) cb->onDockJobNumber(dock_job_number);
        });
    }

    // 取消监听机场作业次数(job_number);
    void cancelListenDockJobNumberWrapper(int listen_id) {
        $self->cancelListenDockJobNumber(listen_id);
    }


    // 机场：DockPowerState；
    int listenDockPowerStateWrapper(FHSDK_CALLBACK_PARAMETER(DockPowerStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockPowerStateCallback);
        return $self->listenDockPowerState([cb](const flight_hub_sdk::DockPowerState& dock_power_state) {
            if (cb) cb->onDockPowerState(dock_power_state);
        });
    }

    // 取消监听机场 DockPowerState;
    void cancelListenDockPowerStateWrapper(int listen_id) {
        $self->cancelListenDockPowerState(listen_id);
    }


     // 机场: 存储容量(storage);
    int listenDockStorageStateWrapper(FHSDK_CALLBACK_PARAMETER(DockStorageStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockStorageStateCallback);
        return $self->listenDockStorageState([cb](const flight_hub_sdk::DockStorageState& dock_storage_state) {
            if (cb) cb->onDockStorageState(dock_storage_state);
        });
    }

    // 取消监听机场存储容量(storage);
    void cancelListenDockStorageStateWrapper(int listen_id) {
        $self->cancelListenDockStorageState(listen_id);
    }


    // 机场: 首次上电时间(first_power_on);
    int listenDockFirstPowerOnTimeWrapper(FHSDK_CALLBACK_PARAMETER(DockFirstPowerOnTimeCallback)) {
        FHSDK_SHARE_CALLBACK(DockFirstPowerOnTimeCallback);
        return $self->listenDockFirstPowerOnTime([cb](const flight_hub_sdk::DockFirstPowerOnTime& first_power_on_time) {
            if (cb) cb->onDockFirstPowerOnTime(first_power_on_time);
        });
    }

    // 取消监听首次上电时间(first_power_on);
    void cancelListenDockFirstPowerOnTimeWrapper(int listen_id) {
        $self->cancelListenDockFirstPowerOnTime(listen_id);
    }


    // 监听机场累计运行时长(acc_time);
    int listenDockAccumulatedRunningTimeWrapper(FHSDK_CALLBACK_PARAMETER(DockAccTimeCallback)) {
        FHSDK_SHARE_CALLBACK(DockAccTimeCallback);
        return $self->listenDockAccumulatedRunningTime([cb](const flight_hub_sdk::DockAccTime& dock_acc_time) {
            if (cb) cb->onDockAccTime(dock_acc_time);
        });
    }

    // 取消监听机场累计运行时长(acc_time);
    void cancelListenDockAccumulatedRunningTimeWrapper(int listen_id) {
        $self->cancelListenDockAccumulatedRunningTime(listen_id);
    }


    // 监听机场备降点(alternate_land_point);
    int listenDockAlternateLandPointWrapper(FHSDK_CALLBACK_PARAMETER(DockAlternateLandPointCallback)) {
        FHSDK_SHARE_CALLBACK(DockAlternateLandPointCallback);
        return $self->listenDockAlternateLandPoint([cb](const flight_hub_sdk::DockAlternateLandPoint& dock_alternate_land_point) {
            if (cb) cb->onDockAlternateLandPoint(dock_alternate_land_point);
        });
    }

    // 取消监听机场备降点(alternate_land_point);
    void cancelListenDockAlternateLandPointWrapper(int listen_id) {
        $self->cancelListenDockAlternateLandPoint(listen_id);
    }


    // 监听搜星状态(position_state);
    int listenDockPositionStateWrapper(FHSDK_CALLBACK_PARAMETER(DockPositionStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockPositionStateCallback);
        return $self->listenDockPositionState([cb](const flight_hub_sdk::DockPositionState& dock_position_state) {
            if (cb) cb->onDockPositionState(dock_position_state);
        });
    }

    // 取消监听搜星状态(position_state);
    void cancelListenDockPositionStateWrapper(int listen_id) {
        $self->cancelListenDockPositionState(listen_id);
    }


    // 监听机场激活时间(activation_time, unix时间戳 s);
    int listenDockActivationTimeWrapper(FHSDK_CALLBACK_PARAMETER(DockActivationTimeCallback)) {
        FHSDK_SHARE_CALLBACK(DockActivationTimeCallback);
        return $self->listenDockActivationTime([cb](const flight_hub_sdk::DockActivationTime& dock_activateion_time) {
            if (cb) cb->onDockActivationTime(dock_activateion_time);
        });
    }

    // 取消监听机场激活时间(activation_time, unix时间戳 s);
    void cancelListenDockActivationTimeWrapper(int listen_id) {
        $self->cancelListenDockActivationTime(listen_id);
    }


    // 监听机场电池(存储)模式(battery_store_mode);
    int listenDockBatteryStoreModeWrapper(FHSDK_CALLBACK_PARAMETER(DockBatteryStoreModeCallback)) {
        FHSDK_SHARE_CALLBACK(DockBatteryStoreModeCallback);
        return $self->listenDockBatteryStoreMode([cb](const flight_hub_sdk::DockBatteryStoreMode& dock_battery_store_mode) {
            if (cb) cb->onDockBatteryStoreMode(dock_battery_store_mode);
        });
    }

    // 取消监听机场电池(存储)模式(battery_store_mode);
    void cancelListenDockBatteryStoreModeWrapper(int listen_id) {
        $self->cancelListenDockBatteryStoreMode(listen_id);
    }


    // 监听机场声光报警状态(alarm_state);
    int listenDockAlarmStateWrapper(FHSDK_CALLBACK_PARAMETER(DockAlarmStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockAlarmStateCallback);
        return $self->listenDockAlarmState([cb](const flight_hub_sdk::DockAlarmState& dock_alarm_state) {
            if (cb) cb->onDockAlarmState(dock_alarm_state);
        });
    }

    // 取消监听机场: 声光报警状态(alarm_state);
    void cancelListenDockAlarmStateWrapper(int listen_id) {
        $self->cancelListenDockAlarmState(listen_id);
    }


    // 监听机场: 飞行器电池保养信息(drone_battery_maintenance_info);
    int listenDockDroneBatteryMaintenanceInfoWrapper(FHSDK_CALLBACK_PARAMETER(DockDroneBatteryMaintenanceInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DockDroneBatteryMaintenanceInfoCallback);
        return $self->listenDockDroneBatteryMaintenanceInfo([cb](const flight_hub_sdk::DockDroneBatteryMaintenanceInfo& dock_drone_battery_maintenance_info) {
            if (cb) cb->onDockDroneBatteryMaintenanceInfo(dock_drone_battery_maintenance_info);
        });
    }

    // 取消监听机场: 飞行器电池保养信息(drone_battery_maintenance_info);
    void cancelListenDockDroneBatteryMaintenanceInfoWrapper(int listen_id) {
        $self->cancelListenDockDroneBatteryMaintenanceInfo(listen_id);
    }


    // 监听机场: 紧急停止按钮状态(emergency_stop_state);
    int listenDockEmergencyStopStateWrapper(FHSDK_CALLBACK_PARAMETER(DockEmergencyStopStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockEmergencyStopStateCallback);
        return $self->listenDockEmergencyStopState([cb](const flight_hub_sdk::DockEmergencyStopState& state) {
            if (cb) cb->onDockEmergencyStopState(state);
        });
    }

    // 取消监听机场: 紧急停止按钮状态(emergency_stop_state);
    void cancelListenDockEmergencyStopStateWrapper(int listen_id) {
        $self->cancelListenDockEmergencyStopState(listen_id);
    }


    // 监听机场: 静音模式(silent_mode);
    int listenDockSilentModeWrapper(FHSDK_CALLBACK_PARAMETER(DockSilentModeCallback)) {
        FHSDK_SHARE_CALLBACK(DockSilentModeCallback);
        return $self->listenDockSilentMode([cb](const flight_hub_sdk::DockSilentMode& mode) {
            if (cb) cb->onDockSilentMode(mode);
        });
    }


    // 取消监听机场: 静音模式(silent_mode);
    void cancelListenDockSilentModeWrapper(int listen_id) {
        $self->cancelListenDockSilentMode(listen_id);
    }


    // 监听机场: 用户体验改善计划(user_experience_improvement);
    int listenDockUserExperienceImprovementWrapper(FHSDK_CALLBACK_PARAMETER(DockUserExperienceImprovementCallback)) {
        FHSDK_SHARE_CALLBACK(DockUserExperienceImprovementCallback);
        return $self->listenDockUserExperienceImprovement([cb](const flight_hub_sdk::DockUserExperienceImprovement& user_experience_improvement) {
            if (cb) cb->onDockUserExperienceImprovement(user_experience_improvement);
        });
    }


    // 取消监听机场: 用户体验改善计划(user_experience_improvement);
    void cancelListenDockUserExperienceImprovementWrapper(int listen_id) {
        $self->cancelListenDockUserExperienceImprovement(listen_id);
    }


    // 监听机场: DRC 链路的状态(drc_state);
    int listenDockDrcStateWrapper(FHSDK_CALLBACK_PARAMETER(DockDrcStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockDrcStateCallback);
        return $self->listenDockDrcState([cb](const flight_hub_sdk::DockDrcState& state) {
            if (cb) cb->onDockDrcState(state);
        });
    }


    // 取消监听机场: DRC 链路的状态(drc_state);
    void cancelListenDockDrcStateWrapper(int listen_id) {
        $self->cancelListenDockDrcState(listen_id);
    }


    // 监听机场: 推杆状态(putter_state);
    int listenDockPutterStateWrapper(FHSDK_CALLBACK_PARAMETER(DockPutterStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockPutterStateCallback);
        return $self->listenDockPutterState([cb](const flight_hub_sdk::DockPutterState& state) {
            if (cb) cb->onDockPutterState(state);
        });
    }


    // 取消监听机场: 推杆状态(putter_state);
    void cancelListenDockPutterStateWrapper(int listen_id) {
        $self->cancelListenDockPutterState(listen_id);
    }


    // 监听机场: 相对备降点(relative_alternate_land_point);
    int listenDockRelativeAlternateLandPointWrapper(FHSDK_CALLBACK_PARAMETER(DockRelativeAlternateLandPointCallback)) {
        FHSDK_SHARE_CALLBACK(DockRelativeAlternateLandPointCallback);
        return $self->listenDockRelativeAlternateLandPoint([cb](const flight_hub_sdk::DockRelativeAlternateLandPoint& land_point) {
            if (cb) cb->onDockRelativeAlternateLandPoint(land_point);
        });
    }


    // 取消监听机场: 相对备降点(relative_alternate_land_point);
    void cancelListenDockRelativeAlternateLandPointWrapper(int listen_id) {
        $self->cancelListenDockRelativeAlternateLandPoint(listen_id);
    }


    // 监听机场: 自收敛坐标(self_converge_coordinate);
    int listenDockSelfConvergeCoordinateWrapper(FHSDK_CALLBACK_PARAMETER(DockSelfConvergeCoordinateCallback)) {
        FHSDK_SHARE_CALLBACK(DockSelfConvergeCoordinateCallback);
        return $self->listenDockSelfConvergeCoordinate([cb](const flight_hub_sdk::DockSelfConvergeCoordinate& coordinate) {
            if (cb) cb->onDockSelfConvergeCoordinate(coordinate);
        });
    }

    // 取消监听机场: 自收敛坐标(self_converge_coordinate);
    void cancelListenDockSelfConvergeCoordinateWrapper(int listen_id) {
        $self->cancelListenDockSelfConvergeCoordinate(listen_id);
    }

    // 机场固件信息;
    // DockFirmwareInfo
    int listenDockFirmwareInfoWrapper(FHSDK_CALLBACK_PARAMETER(DockFirmwareInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DockFirmwareInfoCallback);
        return $self->listenDockFirmwareInfo([cb](const flight_hub_sdk::DockFirmwareInfo& firmware_info) {
            if (cb) cb->onDockFirmwareInfo(firmware_info);
        });
    }


    void cancelListenDockFirmwareInfoWrapper(int listen_id) {
        $self->cancelListenDockFirmwareInfo(listen_id);
    }


    // 监听机场飞机充电状态;
    int listenDockDroneChargeStateWrapper(FHSDK_CALLBACK_PARAMETER(DockDroneChargeStateCallback)) {
        FHSDK_SHARE_CALLBACK(DockDroneChargeStateCallback);
        return $self->listenDockDroneChargeState([cb](const flight_hub_sdk::DockDroneChargeState& dock_drone_charge_state) {
            if (cb) cb->onDockDroneChargeState(dock_drone_charge_state);
        });
    }

    // 取消监听机场飞机充电状态;
    void cancelListenDockDroneChargeStateWrapper(int listen_id) {
        $self->cancelListenDockDroneChargeState(listen_id);
    }

    // 监听机场气象信息;
    int listenDockMeteorologicalInfoWrapper(FHSDK_CALLBACK_PARAMETER(DockMeteorologicalInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DockMeteorologicalInfoCallback);
        return $self->listenDockMeteorologicalInfo([cb](const flight_hub_sdk::DockMeteorologicalInfo& info) {
            if (cb) cb->onDockMeteorologicalInfo(info);
        });
    }

    // 取消监听机场气象信息;
    void cancelListenDockMeteorologicalInfoWrapper(int listen_id) {
        $self->cancelListenDockMeteorologicalInfo(listen_id);
    }


    // 监听机场备用电池信息;
    int listenDockBackupBatteryInfoWrapper(FHSDK_CALLBACK_PARAMETER(DockBackupBatteryInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DockBackupBatteryInfoCallback);
        return $self->listenDockBackupBatteryInfo([cb](const flight_hub_sdk::DockBackupBatteryInfo& info) {
            if (cb) cb->onDockBackupBatteryInfo(info);
        });
    }

    // 取消 监听机场备用电池信息;
    void cancelListenDockBackupBatteryInfoWrapper(int listen_id) {
        $self->cancelListenDockBackupBatteryInfo(listen_id);
    }


    // 监听机场维护保养信息;
    int listenDockMaintainStatusInfoWrapper(FHSDK_CALLBACK_PARAMETER(DockMaintainStatusInfoCallback)) {
        FHSDK_SHARE_CALLBACK(DockMaintainStatusInfoCallback);
        return $self->listenDockMaintainStatusInfo([cb](const flight_hub_sdk::DockMaintainStatusInfo& info) {
            if (cb) cb->onDockMaintainStatusInfo(info);
        });
    }

    // 取消监听机场维护保养信息;
    void cancelListenDockMaintainStatusInfoWrapper(int listen_id) {
        $self->cancelListenDockMaintainStatusInfo(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDockDongleInfosWrapper(FHSDK_CALLBACK_PARAMETER(DockDongleInfosCallback)) {
        FHSDK_SHARE_CALLBACK(DockDongleInfosCallback);
        return $self->listenDockDongleInfos([cb](const flight_hub_sdk::DockDongleInfos& msg) {
            if (cb) cb->onDockDongleInfos(msg);
        });
    }

    void cancelListenDockDongleInfosWrapper(int listen_id) {
        $self->cancelListenDockDongleInfos(listen_id);
    }

    int listenDockAirConditionerModeWrapper(FHSDK_CALLBACK_PARAMETER(DockAirConditionerModeCallback)) {
        FHSDK_SHARE_CALLBACK(DockAirConditionerModeCallback);
        return $self->listenDockAirConditionerMode([cb](const flight_hub_sdk::DockAirConditionerMode& msg) {
            if (cb) cb->onDockAirConditionerMode(msg);
        });
    }

    void cancelListenDockAirConditionerModeWrapper(int listen_id) {
        $self->cancelListenDockAirConditionerMode(listen_id);
    }
}

%extend flight_hub_sdk::IDroneOsdModule {
    int listenAircraftBatteryInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftBatteryInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftBatteryInfoCallback);
        return $self->listenAircraftBatteryInfo([cb](const flight_hub_sdk::AircraftBatteryInfo& battery_info) {
            if (cb) cb->onAircraftBatteryInfo(battery_info);
        });
    }

    int listenAircraftAttitudeWrapper(FHSDK_CALLBACK_PARAMETER(AircraftAttitudeCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftAttitudeCallback);
        return $self->listenAircraftAttitude([cb](const flight_hub_sdk::AircraftAttitude& attitude) {
            if (cb) cb->onAircraftAttitude(attitude);
        });
    }

    int listenAircraftLocationWrapper(FHSDK_CALLBACK_PARAMETER(AircraftLocationCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftLocationCallback);
        return $self->listenAircraftLocation([cb](const flight_hub_sdk::AircraftLocation& location) {
            if (cb) cb->onAircraftLocation(location);
        });
    }

    int listenAircraftModeCodeWrapper(FHSDK_CALLBACK_PARAMETER(AircraftModeCodeCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftModeCodeCallback);
        return $self->listenAircraftModeCode([cb](const flight_hub_sdk::AircraftModeCode& mode_code) {
            if (cb) cb->onAircraftModeCode(mode_code);
        });
    }

    int listenAircraftWindSpeedWrapper(FHSDK_CALLBACK_PARAMETER(AircraftWindSpeedCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftWindSpeedCallback);
        return $self->listenAircraftWindSpeed([cb](const flight_hub_sdk::AircraftWindSpeed& wind_speed) {
            if (cb) cb->onAircraftWindSpeed(wind_speed);
        });
    }

    int listenAircraftPayloadsOsdInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftPayloadsOsdInfoListCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftPayloadsOsdInfoListCallback);
        return $self->listenAircraftPayloadsOsdInfo([cb](const flight_hub_sdk::AircraftPayloadsOsdInfoList& data) {
            if (cb) cb->onAircraftPayloadsOsdInfoList(data);
        });
    }

    void cancelListenAircraftPayloadsOsdInfoWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsOsdInfo(listen_id);
    }


    // 飞机: 档位信息(gear);
    // AircraftGear;
    int listenAircraftGearWrapper(FHSDK_CALLBACK_PARAMETER(AircraftGearCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftGearCallback);
        return $self->listenAircraftGear([cb](const flight_hub_sdk::AircraftGear& data) {
            if (cb) cb->onAircraftGear(data);
        });
    }

    void cancelListenAircraftGearWrapper(int listen_id) {
        $self->cancelListenAircraftGear(listen_id);
    }


    // 飞机: 档位信息(gear);
    // AircraftGear;
    int listenAircraftHomeDistanceWrapper(FHSDK_CALLBACK_PARAMETER(AircraftHomeDistanceCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftHomeDistanceCallback);
        return $self->listenAircraftHomeDistance([cb](const flight_hub_sdk::AircraftHomeDistance& home_distance) {
            if (cb) cb->onAircraftHomeDistance(home_distance);
        });
    }

    void cancelListenAircraftHomeDistanceWrapper(int listen_id) {
        $self->cancelListenAircraftHomeDistance(listen_id);
    }


    // 飞机: 搜星状态(position_state);
    // AircraftPositionState;
    int listenAircraftPositionStateWrapper(FHSDK_CALLBACK_PARAMETER(AircraftPositionStateCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftPositionStateCallback);
        return $self->listenAircraftPositionState([cb](const flight_hub_sdk::AircraftPositionState& state) {
            if (cb) cb->onAircraftPositionState(state);
        });
    }

    void cancelListenAircraftPositionStateWrapper(int listen_id) {
        $self->cancelListenAircraftPositionState(listen_id);
    }


    // 飞机: 当前控制源(control_source);
    // AircraftControlSource;
    int listenAircraftControlSourceWrapper(FHSDK_CALLBACK_PARAMETER(AircraftControlSourceCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftControlSourceCallback);
        return $self->listenAircraftControlSource([cb](const flight_hub_sdk::AircraftControlSource& control_source) {
            if (cb) cb->onAircraftControlSource(control_source);
        });
    }

    void cancelListenAircraftControlSourceWrapper(int listen_id) {
        $self->cancelListenAircraftControlSource(listen_id);
    }


    // 飞机: 限制类信息状态;
    // AircraftLimitsInfo;
    int listenAircraftLimitsInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftLimitsInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftLimitsInfoCallback);
        return $self->listenAircraftLimitsInfo([cb](const flight_hub_sdk::AircraftLimitsInfo& info) {
            if (cb) cb->onAircraftLimitsInfo(info);
        });
    }

    void cancelListenAircraftLimitsInfoWrapper(int listen_id) {
        $self->cancelListenAircraftLimitsInfo(listen_id);
    }


    // 电量告警信息(用户设置的阈值);
    // AircraftBatteryWarningThresholds;
    int listenAircraftBatteryWarningThresholdsWrapper(FHSDK_CALLBACK_PARAMETER(AircraftBatteryWarningThresholdsCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftBatteryWarningThresholdsCallback);
        return $self->listenAircraftBatteryWarningThresholds([cb](const flight_hub_sdk::AircraftBatteryWarningThresholds& threshold) {
            if (cb) cb->onAircraftBatteryWarningThresholds(threshold);
        });
    }

    void cancelListenAircraftBatteryWarningThresholdsWrapper(int listen_id) {
        $self->cancelListenAircraftBatteryWarningThresholds(listen_id);
    }


    // 固件状态信息;
    // AircraftFirmwareStatus;
    int listenAircraftFirmwareStatusWrapper(FHSDK_CALLBACK_PARAMETER(AircraftFirmwareStatusCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftFirmwareStatusCallback);
        return $self->listenAircraftFirmwareStatus([cb](const flight_hub_sdk::AircraftFirmwareStatus& firmware_status) {
            if (cb) cb->onAircraftFirmwareStatus(firmware_status);
        });
    }

    void cancelListenAircraftFirmwareStatusWrapper(int listen_id) {
        $self->cancelListenAircraftFirmwareStatus(listen_id);
    }


    // 累计飞行信息(total_flight_info);
    // AircraftTotalFlightInfo;
    int listenAircraftTotalFlightInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftTotalFlightInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftTotalFlightInfoCallback);
        return $self->listenAircraftTotalFlightInfo([cb](const flight_hub_sdk::AircraftTotalFlightInfo& total_flight_info) {
            if (cb) cb->onAircraftTotalFlightInfo(total_flight_info);
        });
    }

    void cancelListenAircraftTotalFlightInfoWrapper(int listen_id) {
        $self->cancelListenAircraftTotalFlightInfo(listen_id);
    }


    // 飞行器激活时间(unix时间戳)(activation_time);
    // AircraftActivationTime;
    int listenAircraftActivationTimeWrapper(FHSDK_CALLBACK_PARAMETER(AircraftActivationTimeCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftActivationTimeCallback);
        return $self->listenAircraftActivationTime([cb](const flight_hub_sdk::AircraftActivationTime& activation_time) {
            if (cb) cb->onAircraftActivationTime(activation_time);
        });
    }

    void cancelListenAircraftActivationTimeWrapper(int listen_id) {
        $self->cancelListenAircraftActivationTime(listen_id);
    }


    // 飞机：保养信息(maintain_status);
    // AircraftMaintainStatusInfo;
    int listenAircraftMaintainStatusInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftMaintainStatusInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftMaintainStatusInfoCallback);
        return $self->listenAircraftMaintainStatusInfo([cb](const flight_hub_sdk::AircraftMaintainStatusInfo& info) {
            if (cb) cb->onAircraftMaintainStatusInfo(info);
        });
    }

    void cancelListenAircraftMaintainStatusInfoWrapper(int listen_id) {
        $self->cancelListenAircraftMaintainStatusInfo(listen_id);
    }

    // 飞行器夜航灯状态(night_lights_state);
    // AircraftNightLightsState;
    int listenAircraftNightLightsStateWrapper(FHSDK_CALLBACK_PARAMETER(AircraftNightLightsStateCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftNightLightsStateCallback);
        return $self->listenAircraftNightLightsState([cb](const flight_hub_sdk::AircraftNightLightsState& state) {
            if (cb) cb->onAircraftNightLightsState(state);
        });
    }

    void cancelListenAircraftNightLightsStateWrapper(int listen_id) {
        $self->cancelListenAircraftNightLightsState(listen_id);
    }

    // 飞行器避障状态(obstacle_avoidance);
    // AircraftObstacleAvoidanceState;
    int listenAircraftObstacleAvoidanceStateWrapper(FHSDK_CALLBACK_PARAMETER(AircraftObstacleAvoidanceStateCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftObstacleAvoidanceStateCallback);
        return $self->listenAircraftObstacleAvoidanceState([cb](const flight_hub_sdk::AircraftObstacleAvoidanceState& state) {
            if (cb) cb->onAircraftObstacleAvoidanceState(state);
        });
    }

    void cancelListenAircraftObstacleAvoidanceStateWrapper(int listen_id) {
        $self->cancelListenAircraftObstacleAvoidanceState(listen_id);
    }


    // 存储容量(storage);
    // AircraftStorageInfo;
    int listenAircraftStorageInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftStorageInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftStorageInfoCallback);
        return $self->listenAircraftStorageInfo([cb](const flight_hub_sdk::AircraftStorageInfo& info) {
            if (cb) cb->onAircraftStorageInfo(info);
        });
    }

    void cancelListenAircraftStorageInfoWrapper(int listen_id) {
        $self->cancelListenAircraftStorageInfo(listen_id);
    }


    // 航迹id(track_id);
    // AircraftTrackId;
    int listenAircraftTrackIdWrapper(FHSDK_CALLBACK_PARAMETER(AircraftTrackIdCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftTrackIdCallback);
        return $self->listenAircraftTrackId([cb](const flight_hub_sdk::AircraftTrackId& track_id) {
            if (cb) cb->onAircraftTrackId(track_id);
        });
    }

    void cancelListenAircraftTrackIdWrapper(int listen_id) {
        $self->cancelListenAircraftTrackId(listen_id);
    }


    // 合规相关信息(compliance information);
    // AircraftComplianceInfo;
    int listenAircraftComplianceInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftComplianceInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftComplianceInfoCallback);
        return $self->listenAircraftComplianceInfo([cb](const flight_hub_sdk::AircraftComplianceInfo& info) {
            if (cb) cb->onAircraftComplianceInfo(info);
        });
    }

    void cancelListenAircraftComplianceInfoWrapper(int listen_id) {
        $self->cancelListenAircraftComplianceInfo(listen_id);
    }

    // 飞行器相机信息(cameras);
    // AircraftPayloadsCamerasList;
    int listenAircraftPayloadsCamerasListWrapper(FHSDK_CALLBACK_PARAMETER(AircraftPayloadsCamerasListCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftPayloadsCamerasListCallback);
        return $self->listenAircraftPayloadsCamerasList([cb](const flight_hub_sdk::AircraftPayloadsCamerasList& cameras) {
            if (cb) cb->onAircraftPayloadsCamerasList(cameras);
        });
    }

    void cancelListenAircraftPayloadsCamerasListWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsCamerasList(listen_id);
    }

    // 遥控器失控动作(rc_lost_action);
    // AircraftRcLostAction;
    int listenAircraftRcLostActionWrapper(FHSDK_CALLBACK_PARAMETER(AircraftRcLostActionCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftRcLostActionCallback);
        return $self->listenAircraftRcLostAction([cb](const flight_hub_sdk::AircraftRcLostAction& action) {
            if (cb) cb->onAircraftRcLostAction(action);
        });
    }

    void cancelListenAircraftRcLostActionWrapper(int listen_id) {
        $self->cancelListenAircraftRcLostAction(listen_id);
    }

    // 返航信息(返航模式, 返航高度);
    // AircraftRthInfo;
    int listenAircraftRthInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftRthInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftRthInfoCallback);
        return $self->listenAircraftRthInfo([cb](const flight_hub_sdk::AircraftRthInfo& info) {
            if (cb) cb->onAircraftRthInfo(info);
        });
    }

    void cancelListenAircraftRthInfoWrapper(int listen_id) {
        $self->cancelListenAircraftRthInfo(listen_id);
    }


    // 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
    // AircraftHomeGeographicalInfo;
    int listenAircraftHomeGeographicalInfoWrapper(FHSDK_CALLBACK_PARAMETER(AircraftHomeGeographicalInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftHomeGeographicalInfoCallback);
        return $self->listenAircraftHomeGeographicalInfo([cb](const flight_hub_sdk::AircraftHomeGeographicalInfo& info) {
            if (cb) cb->onAircraftHomeGeographicalInfo(info);
        });
    }

    void cancelListenAircraftHomeGeographicalInfoWrapper(int listen_id) {
        $self->cancelListenAircraftHomeGeographicalInfo(listen_id);
    }

    // 返航高度模式当前值(current_rth_mode);
    // AircraftCurrentRthMode;
    int listenAircraftCurrentRthModeWrapper(FHSDK_CALLBACK_PARAMETER(AircraftCurrentRthModeCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftCurrentRthModeCallback);
        return $self->listenAircraftCurrentRthMode([cb](const flight_hub_sdk::AircraftCurrentRthMode& mode) {
            if (cb) cb->onAircraftCurrentRthMode(mode);
        });
    }

    void cancelListenAircraftCurrentRthModeWrapper(int listen_id) {
        $self->cancelListenAircraftCurrentRthMode(listen_id);
    }

    // 飞行任务异常相关wrapper方法
    int listenFlightTasksExceptionWrapper(FHSDK_CALLBACK_PARAMETER(FlightTaskExceptionCallback)) {
        FHSDK_SHARE_CALLBACK(FlightTaskExceptionCallback);
        return $self->listenFlightTasksException([cb](const flight_hub_sdk::FlightTaskException& exception) {
            if (cb) cb->onFlightTaskException(exception);
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
    int listenAircraftControlCodeWrapper(FHSDK_CALLBACK_PARAMETER(AircraftControlCodeCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftControlCodeCallback);
        return $self->listenAircraftControlCode([cb](const flight_hub_sdk::AircraftControlCode& control_code) {
            if (cb) cb->onAircraftControlCode(control_code);
        });
    }

    void cancelListenAircraftControlCodeWrapper(int listen_id) {
        $self->cancelListenAircraftControlCode(listen_id);
    }

    int listenAircraftSpeedWrapper(FHSDK_CALLBACK_PARAMETER(AircraftSpeedCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftSpeedCallback);
        return $self->listenAircraftSpeed([cb](const flight_hub_sdk::AircraftSpeed& speed) {
            if (cb) cb->onAircraftSpeed(speed);
        });
    }

    void cancelListenAircraftSpeedWrapper(int listen_id) {
        $self->cancelListenAircraftSpeed(listen_id);
    }

    int listenAircraftPayloadsCameraLiveviewWorldRegionWrapper(FHSDK_CALLBACK_PARAMETER(AircraftPayloadsCameraLiveviewWorldRegionCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftPayloadsCameraLiveviewWorldRegionCallback);
        return $self->listenAircraftPayloadsCameraLiveviewWorldRegion([cb](const flight_hub_sdk::AircraftPayloadsCameraLiveviewWorldRegion& region) {
            if (cb) cb->onAircraftPayloadsCameraLiveviewWorldRegion(region);
        });
    }

    void cancelListenAircraftPayloadsCameraLiveviewWorldRegionWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsCameraLiveviewWorldRegion(listen_id);
    }

    int listenAircraftPayloadsGimbalAttitudeWrapper(FHSDK_CALLBACK_PARAMETER(AircraftPayloadsGimbalAttitudeCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftPayloadsGimbalAttitudeCallback);
        return $self->listenAircraftPayloadsGimbalAttitude([cb](const flight_hub_sdk::AircraftPayloadsGimbalAttitude& attitude) {
            if (cb) cb->onAircraftPayloadsGimbalAttitude(attitude);
        });
    }

    void cancelListenAircraftPayloadsGimbalAttitudeWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsGimbalAttitude(listen_id);
    }

    int listenAircraftPayloadsListWrapper(FHSDK_CALLBACK_PARAMETER(AircraftPayloadsListCallback)) {
        FHSDK_SHARE_CALLBACK(AircraftPayloadsListCallback);
        return $self->listenAircraftPayloadsList([cb](const flight_hub_sdk::AircraftPayloadsList& payloads_list) {
            if (cb) cb->onAircraftPayloadsList(payloads_list);
        });
    }

    void cancelListenAircraftPayloadsListWrapper(int listen_id) {
        $self->cancelListenAircraftPayloadsList(listen_id);
    }

    int listenControlRightsCloudControlStatusWrapper(FHSDK_CALLBACK_PARAMETER(ControlRightsCloudControlStatusCallback)) {
        FHSDK_SHARE_CALLBACK(ControlRightsCloudControlStatusCallback);
        return $self->listenControlRightsCloudControlStatus([cb](const flight_hub_sdk::ControlRightsCloudControlStatus& status) {
            if (cb) cb->onControlRightsCloudControlStatus(status);
        });
    }

    void cancelListenControlRightsCloudControlStatusWrapper(int listen_id) {
        $self->cancelListenControlRightsCloudControlStatus(listen_id);
    }

    int listenControlRightsDeviceControlStatusWrapper(FHSDK_CALLBACK_PARAMETER(ControlRightsDeviceControlStatusCallback)) {
        FHSDK_SHARE_CALLBACK(ControlRightsDeviceControlStatusCallback);
        return $self->listenControlRightsDeviceControlStatus([cb](const flight_hub_sdk::ControlRightsDeviceControlStatus& status) {
            if (cb) cb->onControlRightsDeviceControlStatus(status);
        });
    }

    void cancelListenControlRightsDeviceControlStatusWrapper(int listen_id) {
        $self->cancelListenControlRightsDeviceControlStatus(listen_id);
    }

    int listenFlightTasksMediaFileDisplayAvailableWrapper(FHSDK_CALLBACK_PARAMETER(FlightTasksMediaFileDisplayAvailableCallback)) {
        FHSDK_SHARE_CALLBACK(FlightTasksMediaFileDisplayAvailableCallback);
        return $self->listenFlightTasksMediaFileDisplayAvailable([cb](const flight_hub_sdk::FlightTasksMediaFileDisplayAvailable& available) {
            if (cb) cb->onFlightTasksMediaFileDisplayAvailable(available);
        });
    }

    void cancelListenFlightTasksMediaFileDisplayAvailableWrapper(int listen_id) {
        $self->cancelListenFlightTasksMediaFileDisplayAvailable(listen_id);
    }

    // 飞行器 HSI 信息（障碍物感知/避障传感器数据）
    int listenHsiInfoWrapper(FHSDK_CALLBACK_PARAMETER(HsiInfoCallback)) {
        FHSDK_SHARE_CALLBACK(HsiInfoCallback);
        return $self->listenHsiInfo([cb](const flight_hub_sdk::HsiInfo& info) {
            if (cb) cb->onHsiInfo(info);
        });
    }

    void cancelListenHsiInfoWrapper(int listen_id) {
        $self->cancelListenHsiInfo(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDroneModeCodeReasonWrapper(FHSDK_CALLBACK_PARAMETER(DroneModeCodeReasonCallback)) {
        FHSDK_SHARE_CALLBACK(DroneModeCodeReasonCallback);
        return $self->listenDroneModeCodeReason([cb](const flight_hub_sdk::DroneModeCodeReason& msg) {
            if (cb) cb->onDroneModeCodeReason(msg);
        });
    }

    void cancelListenDroneModeCodeReasonWrapper(int listen_id) {
        $self->cancelListenDroneModeCodeReason(listen_id);
    }

    int listenDroneDongleInfosWrapper(FHSDK_CALLBACK_PARAMETER(DroneDongleInfosCallback)) {
        FHSDK_SHARE_CALLBACK(DroneDongleInfosCallback);
        return $self->listenDroneDongleInfos([cb](const flight_hub_sdk::DroneDongleInfos& msg) {
            if (cb) cb->onDroneDongleInfos(msg);
        });
    }

    void cancelListenDroneDongleInfosWrapper(int listen_id) {
        $self->cancelListenDroneDongleInfos(listen_id);
    }

    int listenDroneFlysafeDatabaseVersionWrapper(FHSDK_CALLBACK_PARAMETER(DroneFlysafeDatabaseVersionCallback)) {
        FHSDK_SHARE_CALLBACK(DroneFlysafeDatabaseVersionCallback);
        return $self->listenDroneFlysafeDatabaseVersion([cb](const flight_hub_sdk::DroneFlysafeDatabaseVersion& msg) {
            if (cb) cb->onDroneFlysafeDatabaseVersion(msg);
        });
    }

    void cancelListenDroneFlysafeDatabaseVersionWrapper(int listen_id) {
        $self->cancelListenDroneFlysafeDatabaseVersion(listen_id);
    }

    int listenDroneWpmzVersionWrapper(FHSDK_CALLBACK_PARAMETER(DroneWpmzVersionCallback)) {
        FHSDK_SHARE_CALLBACK(DroneWpmzVersionCallback);
        return $self->listenDroneWpmzVersion([cb](const flight_hub_sdk::DroneWpmzVersion& msg) {
            if (cb) cb->onDroneWpmzVersion(msg);
        });
    }

    void cancelListenDroneWpmzVersionWrapper(int listen_id) {
        $self->cancelListenDroneWpmzVersion(listen_id);
    }

}

%extend flight_hub_sdk::IPsdkControlModule {
    int listenPsdkDeviceListWrapper(FHSDK_CALLBACK_PARAMETER(PsdkDeviceListCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkDeviceListCallback);
        return $self->listenDrcPsdkDeviceList([cb](const flight_hub_sdk::PsdkDeviceList& psdk_device_list) {
            if (cb) cb->onPsdkDeviceList(psdk_device_list);
        });
    }

    void cancelListenPsdkDeviceListWrapper(int listen_id) {
        $self->cancelListenDrcPsdkDeviceList(listen_id);
    }

    int listenPsdkStateSummaryInfoWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(PsdkStateSummaryInfoCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkStateSummaryInfoCallback);
        return $self->listenDrcPsdkStateSummaryInfo(psdk_index, [cb](flight_hub_sdk::PsdkStateSummaryInfo psdk_state_summary_info) {
            if (cb) cb->onPsdkStateSummaryInfo(psdk_state_summary_info);
        });
    }

    void cancelListenPsdkStateSummaryInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkStateSummaryInfo(psdk_index, listen_id);
    }

    int listenPsdkStateSpeakerInfoWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(PsdkStateSpeakerInfoCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkStateSpeakerInfoCallback);
        return $self->listenDrcPsdkStateSpeakerInfo(psdk_index, [cb](flight_hub_sdk::PsdkStateSpeakerInfo psdk_state_speaker_info) {
            if (cb) cb->onPsdkStateSpeakerInfo(psdk_state_speaker_info);
        });
    }

    void cancelListenPsdkStateSpeakerInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkStateSpeakerInfo(psdk_index, listen_id);
    }

    int listenPsdkStateSearchlightInfoWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(PsdkStateSearchlightInfoCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkStateSearchlightInfoCallback);
        return $self->listenDrcPsdkStateSearchlightInfo(psdk_index, [cb](flight_hub_sdk::PsdkStateSearchlightInfo psdk_state_searchlight_info) {
            if (cb) cb->onPsdkStateSearchlightInfo(psdk_state_searchlight_info);
        });
    }

    void cancelListenPsdkStateSearchlightInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkStateSearchlightInfo(psdk_index, listen_id);
    }

    int listenPsdkSpeakerPlayProgressInfoWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(PsdkSpeakerPlayProgressInfoCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkSpeakerPlayProgressInfoCallback);
        return $self->listenDrcPsdkSpeakerPlayProgressInfo(psdk_index, [cb](flight_hub_sdk::PsdkSpeakerPlayProgressInfo psdk_speaker_play_progress_info) {
            if (cb) cb->onPsdkSpeakerPlayProgressInfo(psdk_speaker_play_progress_info);
        });
    }

    void cancelListenPsdkSpeakerPlayProgressInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkSpeakerPlayProgressInfo(psdk_index, listen_id);
    }

    int listenPsdkFloatingWindowTextInfoWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(PsdkFloatingWindowTextInfoCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkFloatingWindowTextInfoCallback);
        return $self->listenDrcPsdkFloatingWindowTextInfo(psdk_index, [cb](flight_hub_sdk::PsdkFloatingWindowTextInfo psdk_floating_window_text_info) {
            if (cb) cb->onPsdkFloatingWindowTextInfo(psdk_floating_window_text_info);
        });
    }

    void cancelListenPsdkFloatingWindowTextInfoWrapper(int psdk_index, int listen_id) {
        $self->cancelListenDrcPsdkFloatingWindowTextInfo(psdk_index, listen_id);
    }

    void speakerAudioPlayStartWrapper(int psdk_index, const flight_hub_sdk::PsdkSpeakerAudioPlayInfo& audio_play_info, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerAudioPlayStart(psdk_index, audio_play_info, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerTTSPlayStartWrapper(int psdk_index, const flight_hub_sdk::PsdkSpeakerTTSPlayInfo& tts_play_info, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerTTSPlayStart(psdk_index, tts_play_info, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerReplayWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerReplay(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerPlayStopWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerPlayStop(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerRealTimeStartWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerRealTimeStart(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerRealTimeStopWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerRealTimeStop(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerPlayVolumeSetWrapper(int psdk_index, int volume, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerPlayVolumeSet(psdk_index, volume, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerPlayModeSetWrapper(int psdk_index, int play_mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerPlayModeSet(psdk_index, play_mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void speakerTTSSetWrapper(int psdk_index, flight_hub_sdk::PsdkSpeakerTTSconfig tts_config, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSpeakerTTSSet(psdk_index, tts_config, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void widgetValueSetWrapper(int psdk_index, int index, int value, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcWidgetValueSet(psdk_index, index, value, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void widgetInputBoxTextSetWrapper(int psdk_index, const std::string& value, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcWidgetInputBoxTextSet(psdk_index, value, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightModeSetWrapper(int psdk_index, int mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightModeSet(psdk_index, mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightBrightnessSetWrapper(int psdk_index, int brightness, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightBrightnessSet(psdk_index, brightness, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightCalibrationWrapper(int psdk_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightCalibration(psdk_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightFineTuningSetWrapper(int psdk_index, int position, double value, bool saved, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightFineTuningSet(psdk_index, position, value, saved, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightWideFieldModeSetWrapper(int psdk_index, int wide_field_mode, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightWideFieldModeSet(psdk_index, wide_field_mode, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightLightGimbalControlSetWrapper(int psdk_index, int light_gimbal_control, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightLightGimbalControlSet(psdk_index, light_gimbal_control, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void searchlightTypeSetWrapper(int psdk_index, int type, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcSearchlightTypeSet(psdk_index, type, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenPsdkSpeakerAudioPlayStartProgressWrapper(FHSDK_CALLBACK_PARAMETER(PsdkSpeakerAudioPlayStartProgressCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkSpeakerAudioPlayStartProgressCallback);
        return $self->listenPsdkSpeakerAudioPlayStartProgress([cb](const flight_hub_sdk::PsdkSpeakerPlayStartProgress& msg) {
            if (cb) cb->onPsdkSpeakerAudioPlayStartProgress(msg);
        });
    }
    void cancelListenPsdkSpeakerAudioPlayStartProgressWrapper(int listen_id) {
        $self->cancelListenPsdkSpeakerAudioPlayStartProgress(listen_id);
    }
    int listenPsdkSpeakerTtsPlayStartProgressWrapper(FHSDK_CALLBACK_PARAMETER(PsdkSpeakerTtsPlayStartProgressCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkSpeakerTtsPlayStartProgressCallback);
        return $self->listenPsdkSpeakerTtsPlayStartProgress([cb](const flight_hub_sdk::PsdkSpeakerPlayStartProgress& msg) {
            if (cb) cb->onPsdkSpeakerTtsPlayStartProgress(msg);
        });
    }
    void cancelListenPsdkSpeakerTtsPlayStartProgressWrapper(int listen_id) {
        $self->cancelListenPsdkSpeakerTtsPlayStartProgress(listen_id);
    }

    // EA340 消息推送补全（Phase D）;
    int listenDronePsdkUiResourceWrapper(FHSDK_CALLBACK_PARAMETER(DronePsdkUiResourceCallback)) {
        FHSDK_SHARE_CALLBACK(DronePsdkUiResourceCallback);
        return $self->listenDronePsdkUiResource([cb](const flight_hub_sdk::DronePsdkUiResource& msg) {
            if (cb) cb->onDronePsdkUiResource(msg);
        });
    }

    void cancelListenDronePsdkUiResourceWrapper(int listen_id) {
        $self->cancelListenDronePsdkUiResource(listen_id);
    }

    int listenPsdkCustomDataTransmissionFromPsdkWrapper(FHSDK_CALLBACK_PARAMETER(PsdkCustomDataTransmissionFromPsdkCallback)) {
        FHSDK_SHARE_CALLBACK(PsdkCustomDataTransmissionFromPsdkCallback);
        return $self->listenPsdkCustomDataTransmissionFromPsdk([cb](const flight_hub_sdk::PsdkCustomDataTransmission& msg) {
            if (cb) cb->onPsdkCustomDataTransmissionFromPsdk(msg);
        });
    }
    void cancelListenPsdkCustomDataTransmissionFromPsdkWrapper(int listen_id) {
        $self->cancelListenPsdkCustomDataTransmissionFromPsdk(listen_id);
    }
    void sendPsdkCustomDataTransmissionToPsdkWrapper(const std::string& value, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->sendPsdkCustomDataTransmissionToPsdk(value, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

}

%extend flight_hub_sdk::IEsdkControlModule {
    int listenEsdkCustomDataTransmissionFromEsdkWrapper(FHSDK_CALLBACK_PARAMETER(EsdkCustomDataTransmissionFromEsdkCallback)) {
        FHSDK_SHARE_CALLBACK(EsdkCustomDataTransmissionFromEsdkCallback);
        return $self->listenEsdkCustomDataTransmissionFromEsdk([cb](const flight_hub_sdk::EsdkCustomDataTransmission& msg) {
            if (cb) cb->onEsdkCustomDataTransmissionFromEsdk(msg);
        });
    }
    void cancelListenEsdkCustomDataTransmissionFromEsdkWrapper(int listen_id) {
        $self->cancelListenEsdkCustomDataTransmissionFromEsdk(listen_id);
    }
    void sendEsdkCustomDataTransmissionToEsdkWrapper(const std::string& value, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->sendEsdkCustomDataTransmissionToEsdk(value, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%extend flight_hub_sdk::IFlightTaskMediaUploadModule {
    void uploadFlighttaskMediaPrioritizeWrapper(const std::string& flight_id, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->uploadFlighttaskMediaPrioritize(flight_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
    int listenHighestPriorityUploadFlighttaskMediaWrapper(FHSDK_CALLBACK_PARAMETER(HighestPriorityUploadFlighttaskMediaCallback)) {
        FHSDK_SHARE_CALLBACK(HighestPriorityUploadFlighttaskMediaCallback);
        return $self->listenHighestPriorityUploadFlighttaskMedia([cb](const flight_hub_sdk::HighestPriorityUploadFlighttaskMedia& msg) {
            if (cb) cb->onHighestPriorityUploadFlighttaskMedia(msg);
        });
    }
    void cancelListenHighestPriorityUploadFlighttaskMediaWrapper(int listen_id) {
        $self->cancelListenHighestPriorityUploadFlighttaskMedia(listen_id);
    }
}

%extend flight_hub_sdk::IOnboardAiModule {
    int listenAIDataInfoWrapper(FHSDK_CALLBACK_PARAMETER(AiDataInfoCallback)) {
        FHSDK_SHARE_CALLBACK(AiDataInfoCallback);
        return $self->listenAIDataInfo([cb](const flight_hub_sdk::AiDataInfo& info) {
            if (cb) cb->onAiDataInfo(info);
        });
    }
    void cancelListenAIDataInfoWrapper(int listen_id) {
        $self->cancelListenAIDataInfo(listen_id);
    }
    void AIIdentifySetWrapper(const flight_hub_sdk::AiIdentifySetParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->AIIdentifySet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void AIIdentifyFilterSetWrapper(const flight_hub_sdk::AiIdentifyFilterSetParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->AIIdentifyFilterSet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void AIIdentifyScoreSetWrapper(const flight_hub_sdk::AiIdentifyScoreSetParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->AIIdentifyScoreSet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void AIIdentifyScoreModeSetWrapper(const flight_hub_sdk::AiIdentifyScoreModeSetParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->AIIdentifyScoreModeSet(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void AIIdentifyScoreResetWrapper(const flight_hub_sdk::AiIdentifyScoreResetParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->AIIdentifyScoreReset(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }
    void AIModelSelectWrapper(const flight_hub_sdk::AiModelSelectParam& param, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->AIModelSelect(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    // ══════════════════════════════════════════════
    // AI Spotlight Zoom (Tracking) Wrappers
    // ══════════════════════════════════════════════

    void DrcAISpotlightZoomSetWrapper(int on, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAISpotlightZoomSet(on, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void DrcAISpotlightZoomSelectWrapper(const flight_hub_sdk::AiSpotlightZoomSelectParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAISpotlightZoomSelect(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void DrcAISpotlightZoomConfirmWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAISpotlightZoomConfirm([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void DrcAISpotlightZoomStopWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAISpotlightZoomStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void DrcAISpotlightZoomTrackWrapper(int target_index, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAISpotlightZoomTrack(target_index, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    // ══════════════════════════════════════════════
    // AI Alarm Wrappers
    // ══════════════════════════════════════════════

    void DrcAIAlarmStartWrapper(const flight_hub_sdk::AiAlarmStartParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAIAlarmStart(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void DrcAIAlarmStopWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAIAlarmStop([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    void DrcAIModelsConfigWrapper(const flight_hub_sdk::AiModelsConfigParams& params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->DrcAIModelsConfig(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
        });
    }

    int listenDrcAIAlarmStateWrapper(FHSDK_CALLBACK_PARAMETER(AiAlarmStateCallback)) {
        FHSDK_SHARE_CALLBACK(AiAlarmStateCallback);
        return $self->listenDrcAIAlarmState([cb](const flight_hub_sdk::AiAlarmStateInfo& info) {
            if (cb) cb->onAiAlarmState(info);
        });
    }

    void cancelListenDrcAIAlarmStateWrapper(int listen_id) {
        $self->cancelListenDrcAIAlarmState(listen_id);
    }
}

%extend flight_hub_sdk::INetworkTestModule {
    void startNetworkTestWrapper(FHSDK_CALLBACK_PARAMETER(StartNetworkTestCallback)) {
        $self->startNetworkTest([callback](flight_hub_sdk::FlightHubError err, NetworkTestProgressResult result) {
            if (callback) {
                callback->onStartNetworkTest(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void stopNetworkTestWrapper(FHSDK_CALLBACK_PARAMETER(StopNetworkTestCallback)) {
        $self->stopNetworkTest([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onStopNetworkTest(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getNetworkTestProgressWrapper(FHSDK_CALLBACK_PARAMETER(GetNetworkTestProgressCallback)) {
        $self->getNetworkTestProgress([callback](flight_hub_sdk::FlightHubError err, NetworkTestProgressResult result) {
            if (callback) {
                callback->onGetNetworkTestProgress(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int subscribeNetworkTestProgressWrapper(FHSDK_CALLBACK_PARAMETER(SubscribeNetworkTestProgressCallback)) {
        FHSDK_SHARE_CALLBACK(SubscribeNetworkTestProgressCallback);
        return $self->subscribeNetworkTestProgress([cb](flight_hub_sdk::NetworkTestProgressResult result) {
            if (cb) cb->onSubscribeNetworkTestProgress(result);
        });
    }

    void unsubscribeNetworkTestProgressWrapper(int listen_id) {
        $self->unsubscribeNetworkTestProgress(listen_id);
    }
}

%extend flight_hub_sdk::IHMSModule {
    void QueryHistoryHMSDataWrapper(const flight_hub_sdk::HistoryHmsDataParam& params, FHSDK_CALLBACK_PARAMETER(QueryHistoryHMSDataCallback)) {
        $self->QueryHistoryHMSData(params, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::HistoryHmsDataResult result) {
            if (callback) {
                callback->onQueryHistoryHMSData(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%extend flight_hub_sdk::ILogManagementModule {
    void QueryDeviceLogListWrapper(const std::string& device_sn, const std::vector<int>& module_list, FHSDK_CALLBACK_PARAMETER(QueryDeviceLogListCallback)) {
        $self->QueryDeviceLogList(device_sn, module_list, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::DeviceLogListResult result) {
            if (callback) {
                callback->onQueryDeviceLogList(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void CreateDeviceLogReportWrapper(const flight_hub_sdk::CreateDeviceLogReportRequestParam& param, FHSDK_CALLBACK_PARAMETER(CreateDeviceLogReportCallback)) {
        $self->CreateDeviceLogReport(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CreateDeviceLogReportResult result) {
            if (callback) {
                callback->onCreateDeviceLogReport(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void LogReportListWrapper(const std::string& device_sn, int page, int page_size, FHSDK_CALLBACK_PARAMETER(LogReportListCallback)) {
        $self->LogReportList(device_sn, page, page_size, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::LogReportListResult result) {
            if (callback) {
                callback->onLogReportList(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void LogReportOperationWrapper(const std::string& operation, const std::string& report_id, FHSDK_CALLBACK_PARAMETER(LogReportOperationCallback)) {
        $self->LogReportOperation(operation, report_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onLogReportOperation(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void UploadLogAppendFileWrapper(const std::string& file_path, FHSDK_CALLBACK_PARAMETER(UploadLogAppendFileCallback)) {
        $self->UploadLogAppendFile(file_path, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onUploadLogAppendFile(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    int listenDeviceFileUploadProgressWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(DeviceFileUploadProgressCallback)) {
        FHSDK_SHARE_CALLBACK(DeviceFileUploadProgressCallback);
        return $self->listenDeviceFileUploadProgress(device_sn, [cb](const flight_hub_sdk::DeviceFileUploadProgress& msg) {
            if (cb) cb->onDeviceFileUploadProgress(msg);
        });
    }
    void cancelListenDeviceFileUploadProgressWrapper(int listen_id) {
        $self->cancelListenDeviceFileUploadProgress(listen_id);
    }
}

%extend flight_hub_sdk::IMediaServiceModule {
    // [媒体服务] 获取媒体文件列表;
    void getMediaFilesListWrapper(const flight_hub_sdk::GetMediaFilesListParams& request_params, FHSDK_CALLBACK_PARAMETER(GetMediaFilesListCallback)) {
        $self->getMediaFilesList(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesListResults& results) {
            if (callback) {
                callback->onGetMediaFilesList(err, results);
                // FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [媒体服务] 批量删除媒体文件;
    void deleteMediaFilesWrapper(const std::vector<long long>& file_ids, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->deleteMediaFiles(file_ids, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                // FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [媒体服务] 获取媒体文件下载地址;
    void getMediaFilesDownloadURLWrapper(long long file_id, FHSDK_CALLBACK_PARAMETER(GetMediaFilesDownloadUrlCallback)) {
        $self->getMediaFilesDownloadURL(file_id, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesDownloadUrlResults& results) {
            if (callback) {
                callback->onGetMediaFilesDownloadUrl(err, results);
                // FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [媒体服务] 获取媒体文件批量下载地址 (通过 object_keys);
    void getMediaFilesDownloadURLListWrapper(const std::vector<std::string>& object_keys, FHSDK_CALLBACK_PARAMETER(GetMediaFilesDownloadUrlListCallback)) {
        $self->getMediaFilesDownloadURL(object_keys, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetMediaFilesDownloadUrlListResults& results) {
            if (callback) {
                callback->onGetMediaFilesDownloadUrlList(err, results);
                // FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // 监听: 设备媒体后处理完成通知;
    int listenMediaFilePostProcessMessageWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(MediaFilePostProcessCallback)) {
        FHSDK_SHARE_CALLBACK(MediaFilePostProcessCallback);
        return $self->listenMediaFilePostProcessMessage(device_sn, [cb](const flight_hub_sdk::MediaFilePostProcess& msg) {
            if (cb) cb->onMediaFilePostProcess(msg);
        });
    }

    // 取消监听: 设备媒体后处理完成通知;
    void cancelListenMediaFilePostProcessMessageWrapper(int listen_id) {
        $self->cancelListenMediaFilePostProcessMessage(listen_id);
    }

    // 监听: 设备媒体回传通知;
    int listenMediaFileDeviceUploadMessageWrapper(const std::string& device_sn, FHSDK_CALLBACK_PARAMETER(MediaFileDeviceUploadCallback)) {
        FHSDK_SHARE_CALLBACK(MediaFileDeviceUploadCallback);
        return $self->listenMediaFileDeviceUploadMessage(device_sn, [cb](const flight_hub_sdk::MediaFileDeviceUpload& msg) {
            if (cb) cb->onMediaFileDeviceUpload(msg);
        });
    }

    // 取消监听: 设备媒体回传通知;
    void cancelListenMediaFileDeviceUploadMessageWrapper(int listen_id) {
        $self->cancelListenMediaFileDeviceUploadMessage(listen_id);
    }

    // [媒体上传] 获取 OSS STS 临时上传凭证
    void getMediaUploadStsWrapper(const flight_hub_sdk::GetMediaUploadStsParams& params,
                                  FHSDK_CALLBACK_PARAMETER(GetMediaUploadStsCallback)) {
        $self->getMediaUploadSts(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetMediaUploadStsResult& result) {
                if (callback) { callback->onGetMediaUploadSts(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [媒体上传] 文件上传完成后通知服务端
    void mediaUploadCallbackWrapper(const flight_hub_sdk::MediaUploadCallbackParams& params,
                                    FHSDK_CALLBACK_PARAMETER(MediaUploadCallbackCallback)) {
        $self->mediaUploadCallback(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::MediaUploadCallbackResult& result) {
                if (callback) { callback->onMediaUploadCallback(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    void uploadGeoMediaFileWrapper(const flight_hub_sdk::UploadGeoMediaFileParams& params,
                                   FHSDK_CALLBACK_PARAMETER(UploadGeoMediaFileCallback)) {
        $self->uploadGeoMediaFile(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::UploadGeoMediaFileResult& result) {
                if (callback) { callback->onUploadGeoMediaFile(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    void getMediaFileDetailByIdWrapper(int file_id,
                                       FHSDK_CALLBACK_PARAMETER(GetMediaFileDetailByIdCallback)) {
        $self->getMediaFileDetailById(file_id,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetMediaFileDetailByIdResult& result) {
                if (callback) { callback->onGetMediaFileDetailById(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // ── 3X 超清全景拼接 Wrapper 方法 ──────────────────────────────────────
    void startPanoStitchWrapper(long long file_id, FHSDK_CALLBACK_PARAMETER(PanoStitchActionCallback)) {
        $self->startPanoStitch(file_id,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onPanoStitchAction(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    void stopPanoStitchWrapper(long long file_id, FHSDK_CALLBACK_PARAMETER(PanoStitchActionCallback)) {
        $self->stopPanoStitch(file_id,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onPanoStitchAction(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    void retryPanoStitchWrapper(long long file_id, FHSDK_CALLBACK_PARAMETER(PanoStitchActionCallback)) {
        $self->retryPanoStitch(file_id,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onPanoStitchAction(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    void startPanoTierWrapper(long long file_id, FHSDK_CALLBACK_PARAMETER(PanoStitchActionCallback)) {
        $self->startPanoTier(file_id,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onPanoStitchAction(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    void getPanoTierTileUrlWrapper(long long file_id, FHSDK_CALLBACK_PARAMETER(GetPanoTierTileUrlCallback)) {
        $self->getPanoTierTileUrl(file_id,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::GetPanoTierTileUrlResult& result) {
                if (callback) { callback->onGetPanoTierTileUrl(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

     int subscribePanoStitchProgressWrapper(FHSDK_CALLBACK_PARAMETER(PanoStitchProgressCallback)) {
         FHSDK_SHARE_CALLBACK(PanoStitchProgressCallback);
         return $self->subscribePanoStitchProgress(
             [cb](const flight_hub_sdk::PanoStitchProgressMsg& msg) {
                 if (cb) cb->onPanoStitchProgress(msg);
             });
     }

    void cancelListenPanoStitchProgressWrapper(int listen_id) {
        $self->cancelListenPanoStitchProgress(listen_id);
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
%ignore flight_hub_sdk::ITerraBuildModule::deleteModelFileWithResult;
%ignore flight_hub_sdk::ITerraBuildModule::batchDeleteModelFiles;
%ignore flight_hub_sdk::ITerraBuildModule::batchDeleteModelFilesWithResult;
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
                                          FHSDK_CALLBACK_PARAMETER(TerraBuildReconstructionSettingsCallback)) {
        $self->getReconstructionSettings(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildSettingsResult& result) {
                if (callback) { callback->onGetReconstructionSettings(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 获取质量报告
    void getQualityReportWrapper(const std::string& file_uuid,
                                 const std::string& language,
                                 const std::string& measure_unit_system,
                                 FHSDK_CALLBACK_PARAMETER(TerraBuildQualityReportCallback)) {
        $self->getQualityReport(file_uuid, language, measure_unit_system,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildQualityReportResult& result) {
                if (callback) { callback->onGetQualityReport(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 获取模型文件列表
    void getModelFileListWrapper(int page, int page_size,
                                 FHSDK_CALLBACK_PARAMETER(TerraBuildModelFileListCallback)) {
        $self->getModelFileList(page, page_size,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildModelFileListResult& result) {
                if (callback) { callback->onGetModelFileList(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 获取模型文件签名 URL
    void getModelFileUrlWrapper(int file_id, int type,
                                FHSDK_CALLBACK_PARAMETER(TerraBuildModelFileUrlCallback)) {
        $self->getModelFileUrl(file_id, type,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildModelFileUrlResult& result) {
                if (callback) { callback->onGetModelFileUrl(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }
    // [Terra Build] 重试重建任务
    void retryReconstructionWrapper(const std::string& file_uuid, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->retryReconstruction(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 停止重建任务
    void stopReconstructionWrapper(const std::string& file_uuid, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->stopReconstruction(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 获取空三结果
    void getSfmResultWrapper(const std::string& file_uuid,
                             FHSDK_CALLBACK_PARAMETER(TerraBuildSfmResultCallback)) {
        $self->getSfmResult(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildSfmResult& result) {
                if (callback) { callback->onGetSfmResult(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 创建重建任务（Java 参数对象方式，无需构建 JSON）
    void createReconstructionWithParamsWrapper(const terra_build_swig::CreateReconstructionJavaParams& jp,
                                               FHSDK_CALLBACK_PARAMETER(TerraBuildCreateReconstructionCallback)) {
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
                if (callback) { callback->onCreateReconstruction(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 删除单个模型文件
    void deleteModelFileWrapper(int file_id, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->deleteModelFile(file_id,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 批量删除模型文件
    void batchDeleteModelFilesWrapper(const std::vector<int>& file_ids, FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        $self->batchDeleteModelFiles(file_ids,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 删除单个模型文件（返回 lock_files）
    void deleteModelFileWithResultWrapper(int file_id, FHSDK_CALLBACK_PARAMETER(TerraDeleteModelFilesCallback)) {
        $self->deleteModelFileWithResult(file_id,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TerraDeleteModelFilesResult& result) {
                if (callback) { callback->onTerraDeleteModelFiles(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 批量删除模型文件（返回 lock_files）
    void batchDeleteModelFilesWithResultWrapper(const std::vector<int>& file_ids, FHSDK_CALLBACK_PARAMETER(TerraDeleteModelFilesCallback)) {
        $self->batchDeleteModelFilesWithResult(file_ids,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::TerraDeleteModelFilesResult& result) {
                if (callback) { callback->onTerraDeleteModelFiles(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 创建全景拼接任务
    void createPanoramaWrapper(const std::string& name,
                               const std::vector<std::string>& folder_uuids,
                               const std::string& wkt,
                               bool is_shown_on_map,
                               FHSDK_CALLBACK_PARAMETER(TerraBuildCreatePanoramaCallback)) {
        flight_hub_sdk::CreatePanoramaParams params;
        params.name = name;
        params.folder_uuids = folder_uuids;
        if (!wkt.empty()) params.wkt = wkt;
        if (is_shown_on_map) params.is_shown_on_map = true;
        $self->createPanorama(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildCreatePanoramaResult& result) {
                if (callback) { callback->onCreatePanorama(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 获取重建图像列表
    void getReconstructionImagesWrapper(const std::string& file_uuid,
                                        FHSDK_CALLBACK_PARAMETER(TerraBuildReconstructionImagesCallback)) {
        $self->getReconstructionImages(file_uuid,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraBuildReconstructionImagesResult& result) {
                if (callback) { callback->onGetReconstructionImages(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra Build] 监听重建任务状态变更推送
    int listenReconstructionStatusChangedWrapper(FHSDK_CALLBACK_PARAMETER(TerraBuildReconstructionStatusChangedCallback)) {
        FHSDK_SHARE_CALLBACK(TerraBuildReconstructionStatusChangedCallback);
        return $self->listenReconstructionStatusChanged(
            [cb](const flight_hub_sdk::TerraBuildReconstructionStatusChanged& msg) {
                if (cb) cb->onReconstructionStatusChanged(msg);
            });
    }

    // [Terra Build] 取消监听重建任务状态变更推送
    void cancelListenReconstructionStatusChangedWrapper(int listen_id) {
        $self->cancelListenReconstructionStatusChanged(listen_id);
    }

    // [Terra 模型管理] 模型文件上传回调
    void uploadModelCallbackWrapper(const terra_build_swig::TerraUploadCallbackJavaParams& jp,
                                    FHSDK_CALLBACK_PARAMETER(TerraUploadCallbackCallback)) {
        flight_hub_sdk::TerraUploadCallbackParams params;
        params.object_key = jp.object_key;
        params.name = jp.name;
        if (jp.parent_id_set) params.parent_id = jp.parent_id;
        if (jp.drive_uuid_set) params.drive_uuid = jp.drive_uuid;
        if (jp.show_on_map_set) params.show_on_map = jp.show_on_map;
        $self->uploadModelCallback(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraUploadCallbackResult& result) {
                if (callback) { callback->onTerraUploadCallback(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra 模型管理] 模型重命名
    void renameModelWrapper(int file_id,
                            const terra_build_swig::TerraRenameModelJavaParams& jp,
                            FHSDK_CALLBACK_PARAMETER(ResultCallback)) {
        flight_hub_sdk::TerraRenameModelParams params;
        params.name = jp.name;
        if (jp.in_uniform_height_set) params.in_uniform_height = jp.in_uniform_height;
        if (jp.model_display_mode_set) params.model_display_mode = jp.model_display_mode;
        if (jp.collected_at_set) params.collected_at = jp.collected_at;
        $self->renameModel(file_id, params,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onResult(err); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra 模型管理] 批量加载/取消加载模型到地图
    void showModelOnMapWrapper(const terra_build_swig::TerraShowOnMapJavaParams& jp,
                               FHSDK_CALLBACK_PARAMETER(TerraShowOnMapCallback)) {
        flight_hub_sdk::TerraShowOnMapParams params;
        params.ids = jp.ids;
        if (jp.show_on_map_set) params.show_on_map = jp.show_on_map;
        if (jp.is_distributed_set) params.is_distributed = jp.is_distributed;
        params.restore = jp.restore;
        if (jp.is_displayed_set) params.is_displayed = jp.is_displayed;
        if (jp.in_uniform_height_set) params.in_uniform_height = jp.in_uniform_height;
        if (jp.model_display_mod_set) params.model_display_mod = jp.model_display_mod;
        if (jp.select_filter_set) {
            flight_hub_sdk::TerraSelectFilterParams sf;
            if (jp.select_filter.selected_parent_id_set) sf.selected_parent_id = jp.select_filter.selected_parent_id;
            sf.except_ids = jp.select_filter.except_ids;
            sf.key = jp.select_filter.key;
            sf.get_sub_file = jp.select_filter.get_sub_file;
            sf.start_time = jp.select_filter.start_time;
            sf.end_time = jp.select_filter.end_time;
            sf.file_all_types = jp.select_filter.file_all_types;
            sf.payload_model_key = jp.select_filter.payload_model_key;
            sf.parent_id = jp.select_filter.parent_id;
            sf.file_tags = jp.select_filter.file_tags;
            if (jp.select_filter.share_state_set) sf.share_state = jp.select_filter.share_state;
            if (jp.select_filter.show_on_map_set) sf.show_on_map = jp.select_filter.show_on_map;
            params.select_filter = sf;
        }
        $self->showModelOnMap(params,
            [callback](flight_hub_sdk::FlightHubError err,
                       const flight_hub_sdk::TerraShowOnMapResult& result) {
                if (callback) { callback->onTerraShowOnMap(err, result); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // [Terra 模型管理] 监听模型解压进度推送
    int listenTerraUnzipProgressWrapper(FHSDK_CALLBACK_PARAMETER(TerraUnzipProgressCallback)) {
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
    int listenTerraModelStatusChangedWrapper(FHSDK_CALLBACK_PARAMETER(TerraModelStatusChangedCallback)) {
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
    int listenTerraFileShowOnMapWrapper(FHSDK_CALLBACK_PARAMETER(TerraFileShowOnMapCallback)) {
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
    int listenTerraMediaFileHideOnMapWrapper(FHSDK_CALLBACK_PARAMETER(TerraMediaFileHideOnMapCallback)) {
        return $self->listenTerraMediaFileHideOnMap(
            [callback](const flight_hub_sdk::TerraMediaFileHideOnMapMsg& msg) {
                if (callback) callback->onTerraMediaFileHideOnMap(msg);
            });
    }

    void cancelListenTerraMediaFileHideOnMapWrapper(int listen_id) {
        $self->cancelListenTerraMediaFileHideOnMap(listen_id);
    }

    // [Terra 模型管理] 监听模型显示地图推送 (topic_media_file_show_on_map)
    int listenTerraMediaFileShowOnMapWrapper(FHSDK_CALLBACK_PARAMETER(TerraMediaFileShowOnMapCallback)) {
        return $self->listenTerraMediaFileShowOnMap(
            [callback](const flight_hub_sdk::TerraMediaFileShowOnMapMsg& msg) {
                if (callback) callback->onTerraMediaFileShowOnMap(msg);
            });
    }

    void cancelListenTerraMediaFileShowOnMapWrapper(int listen_id) {
        $self->cancelListenTerraMediaFileShowOnMap(listen_id);
    }

    // [Terra 模型管理] 获取模型文件详情
    void getModelDetailWrapper(int file_id, FHSDK_CALLBACK_PARAMETER(TerraGetModelDetailCallback)) {
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
                FHSDK_DELETE_CALLBACK();
            });
    }

    // [Terra 模型管理] 设置 LAS 坐标系并触发格式转换
    void setLasWktWrapper(const terra_build_swig::TerraSetLasWktJavaParams& jp,
                          FHSDK_CALLBACK_PARAMETER(TerraSetLasWktCallback)) {
        flight_hub_sdk::TerraSetLasWktParams params;
        params.file_uuid = jp.file_uuid;
        params.wkt       = jp.wkt;
        $self->setLasWkt(params,
            [callback](flight_hub_sdk::FlightHubError err) {
                if (callback) { callback->onSetLasWkt(err); FHSDK_DELETE_CALLBACK(); }
            });
    }
}

// ── IElementModule wrapper methods ──────────────────────────────────────────
%extend flight_hub_sdk::IElementModule {
    // 创建要素（CreateElementParams 无 std::optional，直接传入）
    void createElementWrapper(
            const flight_hub_sdk::CreateElementParams& params,
            FHSDK_CALLBACK_PARAMETER(ElementCreateElementCallback)) {
        $self->createElement(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateElementResult& r) {
                if (callback) { callback->onCreateElement(err, r); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // 更新要素（UpdateElementParams 含 std::optional，需通过 JavaParams 中转）
    void updateElementWrapper(
            const terra_build_swig::ElementUpdateJavaParams& jp,
            FHSDK_CALLBACK_PARAMETER(ElementUpdateElementCallback)) {
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
            content.properties.icon_url        = jp.content.properties.icon_url;
            content.properties.width           = jp.content.properties.width;
            content.properties.start_cap       = jp.content.properties.start_cap;
            content.properties.end_cap         = jp.content.properties.end_cap;
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
                if (callback) { callback->onUpdateElement(err, r); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // 查询元素层级列表（ListElementHierarchyParams 含 std::optional，需通过 JavaParams 中转）
    void listElementHierarchyWrapper(
            const terra_build_swig::ListElementHierarchyJavaParams& jp,
            FHSDK_CALLBACK_PARAMETER(ElementListHierarchyCallback)) {
        flight_hub_sdk::ListElementHierarchyParams params;
        params.group_id     = jp.group_id;
        params.pid          = jp.pid;
        if (jp.is_distributed_set) params.is_distributed = jp.is_distributed;
        if (jp.type_set)           params.type           = jp.type;
        if (jp.element_uuid_set)   params.element_uuid   = jp.element_uuid;
        params.exclude_copy = jp.exclude_copy;
        $self->listElementHierarchy(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ListElementHierarchyResult& r) {
                if (callback) { callback->onListElementHierarchy(err, r); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // 设置要素显示状态（SetElementVisibilityParams 无 std::optional，直接传入）
    void setElementVisibilityWrapper(
            const flight_hub_sdk::SetElementVisibilityParams& params,
            FHSDK_CALLBACK_PARAMETER(ElementSetVisibilityCallback)) {
        $self->setElementVisibility(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SetElementVisibilityResult& r) {
                if (callback) { callback->onSetElementVisibility(err, r); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // 删除要素（DeleteElementParams 无 std::optional，直接传入）
    void deleteElementWrapper(
            const flight_hub_sdk::DeleteElementParams& params,
            FHSDK_CALLBACK_PARAMETER(ElementDeleteElementCallback)) {
        $self->deleteElement(params,
            [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeleteElementResult& r) {
                if (callback) { callback->onDeleteElement(err, r); FHSDK_DELETE_CALLBACK(); }
            });
    }

    // 监听要素创建事件
    int listenElementCreateWrapper(FHSDK_CALLBACK_PARAMETER(ElementCreateCallback)) {
        return $self->listenElementCreate(
            [callback](const flight_hub_sdk::ElementCreateMsg& msg) {
                if (callback) callback->onElementCreate(msg);
            });
    }
    void cancelListenElementCreateWrapper(int listen_id) {
        $self->cancelListenElementCreate(listen_id);
    }

    // 监听要素更新事件
    int listenElementUpdateWrapper(FHSDK_CALLBACK_PARAMETER(ElementUpdateCallback)) {
        return $self->listenElementUpdate(
            [callback](const flight_hub_sdk::ElementUpdateMsg& msg) {
                if (callback) callback->onElementUpdate(msg);
            });
    }
    void cancelListenElementUpdateWrapper(int listen_id) {
        $self->cancelListenElementUpdate(listen_id);
    }

    // 监听要素删除事件
    int listenElementDeleteWrapper(FHSDK_CALLBACK_PARAMETER(ElementDeleteCallback)) {
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
    void searchSignalMapsWrapper(const std::vector<std::string>& device_sns, const std::vector<std::string>& data_types, FHSDK_CALLBACK_PARAMETER(SearchSignalMapsCallback)) {
        $self->searchSignalMaps(device_sns, data_types, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SignalMapsResults& result) {
            if (callback) {
                callback->onSearchSignalMaps(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区域] 创建自定义飞行区域;
    void createCustomizedFlightAreasWrapper(const flight_hub_sdk::CreateCustomizedFlightAreasParams& request_params, FHSDK_CALLBACK_PARAMETER(CreateCustomizedFlightAreasCallback)) {
        $self->createCustomizedFlightAreas(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateFlightAreasResults& result) {
            if (callback) {
                callback->onCreateCustomizedFlightAreas(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区域] 获取自定义飞行区域;
    void getCustomizedFlightAreasWrapper(const flight_hub_sdk::GetCustomizedFlightAreasParams& request_params, FHSDK_CALLBACK_PARAMETER(GetCustomizedFlightAreasCallback)) {
        $self->getCustomizedFlightAreas(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightAreasResults& result) {
            if (callback) {
                callback->onGetCustomizedFlightAreas(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区域] 更新自定义飞行区域;
    void updateCustomizedFlightAreasWrapper(const flight_hub_sdk::UpdateCustomizedFlightAreasParams& request_params, FHSDK_CALLBACK_PARAMETER(UpdateCustomizedFlightAreasCallback)) {
        $self->updateCustomizedFlightAreas(request_params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UpdateFlightAreasResults& result) {
            if (callback) {
                callback->onUpdateCustomizedFlightAreas(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区域] 删除自定义飞行区域;
    void deleteCustomizedFlightAreasWrapper(const std::string& flight_area_id, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->deleteCustomizedFlightAreas(flight_area_id, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区域] 同步自定义飞行区域;
    void syncCustomizedFlightAreasWrapper(const std::vector<std::string>& device_sns, FHSDK_CALLBACK_PARAMETER(SyncCustomizedFlightAreasCallback)) {
        $self->syncCustomizedFlightAreas(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::SyncFlightAreasResults& result) {
            if (callback) {
                callback->onSyncCustomizedFlightAreas(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区域] 获取自定义飞行区域同步状态;
    void getFlightAreasSyncStatusWrapper(const std::vector<std::string>& device_sns, FHSDK_CALLBACK_PARAMETER(GetFlightAreasSyncStatusCallback)) {
        $self->getFlightAreasSyncStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetAreasSyncStatusResults& result) {
            if (callback) {
                callback->onGetFlightAreasSyncStatus(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [自定义飞行区] 获取飞行区文件下载地址;
    void getFlightAreasUrlWrapper(FHSDK_CALLBACK_PARAMETER(GetFlightAreasUrlCallback)) {
        $self->getFlightAreasUrl([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetFlightAreasUrlResults& result) {
            if (callback) {
                callback->onGetFlightAreasUrl(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [地形障碍物] 查询离线地图模型信息;
    void getOfflineMapsModelInfoWrapper(FHSDK_CALLBACK_PARAMETER(GetOfflineMapsModelInfoCallback)) {
        $self->getOfflineMapsModelInfo([callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetOfflineMapsModelInfoResults& result) {
            if (callback) {
                callback->onGetOfflineMapsModelInfo(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [地形障碍物] 创建/更新离线地图模型;
    void updateOfflineMapsModelInfoWrapper(const flight_hub_sdk::UpdateOfflineMapsParams& request_params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->updateOfflineMapsModelInfo(request_params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [地形障碍物] 取消离线地图生成;
    void cancelOfflineMapsModelInfoWrapper(FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->cancelOfflineMapsModelInfo([callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [地形障碍物] 修改离线地图(开启/关闭);
    void setOfflineMapsWrapper(const flight_hub_sdk::SetOfflineMapsParams& request_params, FHSDK_CALLBACK_PARAMETER(ResultCallbackWithError)) {
        $self->setOfflineMaps(request_params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onResult(err);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [地形障碍物] 离线地图同步;
    void syncOfflineMapsWrapper(const std::vector<std::string>& device_sns, FHSDK_CALLBACK_PARAMETER(SyncOfflineMapsCallback)) {
        $self->syncOfflineMaps(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::OfflineMapsSyncResults& result) {
            if (callback) {
                callback->onSyncOfflineMaps(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [地形障碍物] 获取设备同步状态;
    void getOfflineMapsSyncStatusWrapper(const std::vector<std::string>& device_sns, FHSDK_CALLBACK_PARAMETER(GetOfflineMapsSyncStatusCallback)) {
        $self->getOfflineMapsSyncStatus(device_sns, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetOfflineMapsSyncStatusResults& result) {
            if (callback) {
                callback->onGetOfflineMapsSyncStatus(err, result);
                //FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void getUnlockLicenseListWrapper(flight_hub_sdk::QueryDeviceUnlockLicenseListParam const &param,FHSDK_CALLBACK_PARAMETER(GetUnlockLicenseListCallback)) {
        $self->getUnlockLicenseList(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::QueryDeviceUnlockLicenseListResult result) {
            if (callback) {
                callback->onGetUnlockLicenseList(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void switchUnlockLicenseWrapper(flight_hub_sdk::SwitchUnlockLicenseParam const &param,FHSDK_CALLBACK_PARAMETER(SwitchUnlockLicenseCallback)) {
        $self->switchUnlockLicense(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSwitchUnlockLicense(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void checkUnlockLicenseWrapper(flight_hub_sdk::CheckUnlockLicenseParam const &param,FHSDK_CALLBACK_PARAMETER(CheckUnlockLicenseCallback)) {
        $self->checkUnlockLicense(param, [callback](flight_hub_sdk::FlightHubError err, flight_hub_sdk::CheckUnlockLicenseResult result) {
            if (callback) {
                callback->onCheckUnlockLicense(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void submitUnlockLicenseWrapper(flight_hub_sdk::ImportUnlockLicenseParam const &param,FHSDK_CALLBACK_PARAMETER(SubmitUnlockLicenseCallback)) {
        $self->submitUnlockLicense(param, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onSubmitUnlockLicense(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void clearDeviceUnlockLicenseWrapper(std::string const &device_sn,FHSDK_CALLBACK_PARAMETER(ClearDeviceUnlockLicenseCallback)) {
        $self->clearDeviceUnlockLicense(device_sn, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onClearDeviceUnlockLicense(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    void uploadUnlockLicenseFileWrapper(std::string const &file_path,FHSDK_CALLBACK_PARAMETER(UploadUnlockLicenseFileCallback)) {
        $self->uploadUnlockLicenseFile(file_path, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::UploadUnlockLicenseFileResult& result) {
            if (callback) {
                callback->onUploadUnlockLicenseFile(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // EA340 消息推送补全（Phase D）: 飞行器离线地图开关, 需要 drone_sn 筛选订阅目标;
    int listenDroneOfflineMapEnableWrapper(std::string const &drone_sn, FHSDK_CALLBACK_PARAMETER(DroneOfflineMapEnableCallback)) {
        FHSDK_SHARE_CALLBACK(DroneOfflineMapEnableCallback);
        return $self->listenDroneOfflineMapEnable(drone_sn, [cb](const flight_hub_sdk::DroneOfflineMapEnable& msg) {
            if (cb) cb->onDroneOfflineMapEnable(msg);
        });
    }

    void cancelListenDroneOfflineMapEnableWrapper(int listen_id) {
        $self->cancelListenDroneOfflineMapEnable(listen_id);
    }
}

%extend flight_hub_sdk::IFlightTaskRecordModule {
    // [ATC 空管服务] 导出飞行记录;
    void exportFlightRecordsWrapper(const std::string& user_id, const flight_hub_sdk::ExportFlightRecordsRequestParams& params, FHSDK_CALLBACK_PARAMETER(ExportFlightRecordsCallback)) {
        $self->exportFlightRecords(user_id, params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::ExportFlightRecordsResults& results) {
            if (callback) {
                callback->onExportFlightRecords(err, results);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [ATC 空管服务] 查询导出进度;
    void getExportFlightRecordsProgressWrapper(const flight_hub_sdk::GetExportFlightRecordsProgressParams& params, FHSDK_CALLBACK_PARAMETER(GetExportFlightRecordsProgressCallback)) {
        $self->getExportFlightRecordsProgress(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::GetExportFlightRecordsProgressResults& results) {
            if (callback) {
                callback->onGetExportFlightRecordsProgress(err, results);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%extend flight_hub_sdk::IMaintenanceManagementModule {
    // [报修管理] 获取设备报修配置;
    void getRepairConfigsWrapper(const flight_hub_sdk::GetDeviceRepairConfigParams& params, FHSDK_CALLBACK_PARAMETER(GetRepairConfigsCallback)) {
        $self->getRepairConfigs(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::DeviceRepairConfigResults& result) {
            if (callback) {
                callback->onGetRepairConfigs(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [报修管理] 创建报修订单;
    void createMaintenanceOrderWrapper(const flight_hub_sdk::CreateMaintenanceOrderParams& params, FHSDK_CALLBACK_PARAMETER(CreateMaintenanceOrderCallback)) {
        $self->createMaintenanceOrder(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::CreateMaintenanceOrderResults& result) {
            if (callback) {
                callback->onCreateMaintenanceOrder(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [报修管理] 查询单个报修订单详情;
    void getMaintenanceOrderDetailWrapper(const std::string& order_number, FHSDK_CALLBACK_PARAMETER(GetMaintenanceOrderDetailCallback)) {
        $self->getMaintenanceOrderDetail(order_number, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::MaintenanceOrderDetailResults& result) {
            if (callback) {
                callback->onGetMaintenanceOrderDetail(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [报修管理] 分页查询报修订单列表;
    void getMaintenanceOrdersListWrapper(const flight_hub_sdk::GetMaintenanceOrdersListParams& params, FHSDK_CALLBACK_PARAMETER(GetMaintenanceOrdersListCallback)) {
        $self->getMaintenanceOrdersList(params, [callback](flight_hub_sdk::FlightHubError err, const flight_hub_sdk::MaintenanceOrdersListResults& result) {
            if (callback) {
                callback->onGetMaintenanceOrdersList(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }
}

%include "maintenance_management_module.h"

%extend flight_hub_sdk::IDiagnosisManagementModule {
    // [定损管理] 下发定损指令;
    void createSelfDiagnosisWrapper(const flight_hub_sdk::CreateSelfDiagnosisParams& params, FHSDK_CALLBACK_PARAMETER(CreateSelfDiagnosisCallback)) {
        $self->createSelfDiagnosis(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onCreateSelfDiagnosis(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [定损管理] 定损操作(取消/删除);
    void operateSelfDiagnosisWrapper(const flight_hub_sdk::OperateSelfDiagnosisParams& params, FHSDK_CALLBACK_PARAMETER(OperateSelfDiagnosisCallback)) {
        $self->operateSelfDiagnosis(params, [callback](flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onOperateSelfDiagnosis(err);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [定损管理] 查询单条定损报告详情;
    void getSelfDiagnosisReportWrapper(const std::string& report_uuid, FHSDK_CALLBACK_PARAMETER(GetSelfDiagnosisReportCallback)) {
        $self->getSelfDiagnosisReport(report_uuid, [callback](const flight_hub_sdk::GetSelfDiagnosisReportResults& result, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onGetSelfDiagnosisReport(err, result);
                FHSDK_DELETE_CALLBACK();
            }
        });
    }

    // [定损管理] 查询定损报告列表;
    void getSelfDiagnosisReportListWrapper(const flight_hub_sdk::GetSelfDiagnosisReportListParams& params, FHSDK_CALLBACK_PARAMETER(GetSelfDiagnosisReportListCallback)) {
        $self->getSelfDiagnosisReportList(params, [callback](const flight_hub_sdk::GetSelfDiagnosisReportListResults& result, flight_hub_sdk::FlightHubError err) {
            if (callback) {
                callback->onGetSelfDiagnosisReportList(err, result);
                FHSDK_DELETE_CALLBACK();
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
%include "ground_flight_module.h"
%include "in_flight_wayline_module.h"
%include "flight_control_module.h"
%include "control_auth_module.h"
%include "drc_module.h"
%include "flight_settings_module.h"
%include "test_flight_module.h"
%include "onboard_ai_module.h"
%include "psdk_control_module.h"
%include "esdk_control_module.h"
%include "flight_task_media_upload_module.h"
%include "camera_control_module.h"
%include "drone_osd_module.h"
%include "dock_osd_module.h"
%include "ai_box_module.h"
%include "drone.h"
%include "device_state_module.h"
%include "device_hms_module.h"
%include "airsense_warning_module.h"
%include "live_device_module.h"
%include "dock_control_module.h"
%include "dock.h"
%include "remote_controller.h"
%include "base_station.h"
%include "relay.h"
%include "relay_control_module.h"
%include "rtk_calibration_module.h"
%include "network_test_module.h"
%include "device_debug_module.h"
%include "cascade_module.h"
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
    bool get_sub_file = false;
    long long start_time = 0;
    long long end_time = 0;
    std::vector<std::string> file_all_types;
    std::vector<std::string> payload_model_key;
    int parent_id = 0;
    std::vector<std::string> file_tags;
    int share_state = 0; bool share_state_set = false;
    bool show_on_map = false; bool show_on_map_set = false;
};

/// Terra 加载/取消加载模型 Java 友好参数结构体
struct TerraShowOnMapJavaParams {
    std::vector<int> ids;
    bool show_on_map = false;       bool show_on_map_set = false;
    bool is_distributed = false;    bool is_distributed_set = false;
    bool restore = false;
    bool is_displayed = false;      bool is_displayed_set = false;
    bool in_uniform_height = false; bool in_uniform_height_set = false;
    int model_display_mod = 0;      bool model_display_mod_set = false;
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
    std::string icon_url;
    int width = 0;
    int start_cap = 0;
    int end_cap = 0;
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
%include "auto_record_module.h"
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
%include "session.h"
%include "session_manager.h"
%template(SessionInfoVector) std::vector<flight_hub_sdk::SessionInfo>;
%include "flight_hub_sdk.h"
