// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_cloud_message_manager.h"
#include <base/async/executor.h>
#include "transport/transport_manager.h"

namespace message_engine {
class CloudMessageManager : public ICloudMessageManager {
public:
    CloudMessageManager(const std::shared_ptr<TransportManager>& transport_manager);
    ~CloudMessageManager() override;

    void CancelObserveMessage(ListenIdCode listen_id) override;
private:
    std::shared_ptr<TransportManager> transport_manager_;
    // 根据原消息的uuid填充要发送的消息的uuid, 保证回包的uuid正确
    void BuildReplyMessage(const std::shared_ptr<MessageWrapper>& msg, const std::shared_ptr<MessageWrapper>& original_msg);

public:
///------ 用户自定义接口声明 start ------
    // /*
    //     北向: thing/product/uav/osd   数据结构: OsdNorthbound   接口: SendOsdNorthbound
    // */
    // MeErrorCode SendOsdNorthbound(const std::shared_ptr<OsdNorthbound>& msg, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;

    // /*
    //     设备状态按变上报
    // */
    // MeErrorCode SendStateNorthbound(const std::shared_ptr<StateNorthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<StateSouthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;
    // ListenIdCode ObserveStateSouthbound(const std::function<void(const std::shared_ptr<StateSouthbound>& msg)>& observe_callback, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;

    // /*
    //     设备属性设置
    // */
    // ListenIdCode ObserveSetPropertySouthbound(const std::function<void(const std::shared_ptr<SetPropertySouthbound>& msg)>& observe_callback, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;
    // MeErrorCode SendSetReplyPropertyNorthbound(const std::shared_ptr<SetReplyPropertyNorthbound>& msg, const std::shared_ptr<SetPropertySouthbound>& original_msg, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;

    // /*
    //     ADS-B 状态上报
    // */
    // MeErrorCode SendEventsAirsenseWarningNorthbound(const std::shared_ptr<EventsAirsenseWarningNorthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAirsenseWarningSouthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;
    // ListenIdCode ObserveEventsReplyAirsenseWarningSouthbound(const std::function<void(const std::shared_ptr<EventsReplyAirsenseWarningSouthbound>& msg)>& observe_callback, const std::shared_ptr<Executor>& user_define_executor = nullptr) override;
    /*
        V3 压缩杆量控制（二进制 MessageV3，脚本无法生成）
    */
    MeErrorCode SendCompressedSticksControlSouthbound(const std::shared_ptr<CompressedSticksControlSouthbound>& msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

///------ 用户自定义接口声明 end ------
public:

    /*
        ai_box_app_file_export: AI盒子应用文件导出
    */
    MeErrorCode SendServicesAiBoxAppFileExportSouthbound(const std::shared_ptr<ServicesAiBoxAppFileExportSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppFileExportNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyAiBoxAppFileExportNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppFileExportNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai_box_app_file_export_progress: AI盒子应用文件导出进度
    */
    MeErrorCode SendEventsAiBoxAppFileExportProgressSouthbound(const std::shared_ptr<EventsAiBoxAppFileExportProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAiBoxAppFileExportProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyAiBoxAppFileExportProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyAiBoxAppFileExportProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai_box_app_list: AI盒子应用列表
    */
    MeErrorCode SendServicesAiBoxAppListSouthbound(const std::shared_ptr<ServicesAiBoxAppListSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppListNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyAiBoxAppListNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppListNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai_box_app_list_infos: AI盒子应用列表信息
    */
    MeErrorCode SendEventsAiBoxAppListInfosSouthbound(const std::shared_ptr<EventsAiBoxAppListInfosSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAiBoxAppListInfosNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyAiBoxAppListInfosNorthbound(const std::function<void(const std::shared_ptr<EventsReplyAiBoxAppListInfosNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai_box_app_operate: AI盒子应用操作
    */
    MeErrorCode SendServicesAiBoxAppOperateSouthbound(const std::shared_ptr<ServicesAiBoxAppOperateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppOperateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyAiBoxAppOperateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppOperateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai_object_detection_alert: AI告警数据
    */
    ListenIdCode ObserveEventsAiObjectDetectionAlertNorthbound(const std::function<void(const std::shared_ptr<EventsAiObjectDetectionAlertNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyAiObjectDetectionAlertSouthbound(const std::shared_ptr<EventsReplyAiObjectDetectionAlertSouthbound>& msg, const std::shared_ptr<EventsAiObjectDetectionAlertNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*

    */
    MeErrorCode SendServicesAircraftPowerCtrlSouthbound(const std::shared_ptr<ServicesAircraftPowerCtrlSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAircraftPowerCtrlNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyAircraftPowerCtrlNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAircraftPowerCtrlNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        计算候选飞机到达目标点的时间和能耗
    */
    MeErrorCode SendServicesCalculateCandidateRelayAircraftSouthbound(const std::shared_ptr<ServicesCalculateCandidateRelayAircraftSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCalculateCandidateRelayAircraftNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyCalculateCandidateRelayAircraftNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCalculateCandidateRelayAircraftNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        切换镜头变焦倍率
    */
    MeErrorCode SendServicesCameraFocalLengthSetSouthbound(const std::shared_ptr<ServicesCameraFocalLengthSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCameraFocalLengthSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyCameraFocalLengthSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCameraFocalLengthSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机LookAt
    */
    MeErrorCode SendServicesCameraLookAtSouthbound(const std::shared_ptr<ServicesCameraLookAtSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCameraLookAtNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyCameraLookAtNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCameraLookAtNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        candidate_aircrafts_calculate_results: 候选飞机计算结果
    */
    ListenIdCode ObserveEventsCandidateAircraftsCalculateResultsNorthbound(const std::function<void(const std::shared_ptr<EventsCandidateAircraftsCalculateResultsNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyCandidateAircraftsCalculateResultsSouthbound(const std::shared_ptr<EventsReplyCandidateAircraftsCalculateResultsSouthbound>& msg, const std::shared_ptr<EventsCandidateAircraftsCalculateResultsNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        调试命令
    */
    MeErrorCode SendServicesCccDebugCommandSouthbound(const std::shared_ptr<ServicesCccDebugCommandSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCccDebugCommandNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyCccDebugCommandNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCccDebugCommandNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        调试命令事件上报
    */
    ListenIdCode ObserveEventsCccDebugEventNorthbound(const std::function<void(const std::shared_ptr<EventsCccDebugEventNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyCccDebugEventSouthbound(const std::shared_ptr<EventsReplyCccDebugEventSouthbound>& msg, const std::shared_ptr<EventsCccDebugEventNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        clear_sdr_topo_info: 清除写号
    */
    MeErrorCode SendServicesClearSdrTopoInfoSouthbound(const std::shared_ptr<ServicesClearSdrTopoInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyClearSdrTopoInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyClearSdrTopoInfoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyClearSdrTopoInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        cleared_to_land: 云端主动请求设备执行降落
    */
    MeErrorCode SendServicesClearedToLandSouthbound(const std::shared_ptr<ServicesClearedToLandSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyClearedToLandNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyClearedToLandNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyClearedToLandNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        cloud_environment_switch: 云环境切换
    */
    MeErrorCode SendServicesCloudEnvironmentSwitchSouthbound(const std::shared_ptr<ServicesCloudEnvironmentSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCloudEnvironmentSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyCloudEnvironmentSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCloudEnvironmentSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        command_send: 调试V1消息
    */
    MeErrorCode SendServicesCommandSendSouthbound(const std::shared_ptr<ServicesCommandSendSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCommandSendNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyCommandSendNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCommandSendNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        RTC链路延迟信息统计
    */
    ListenIdCode ObserveDrcDelayInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDelayInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        返航误避障状态上报
    */
    ListenIdCode ObserveEventsDeviceExitHomingNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsDeviceExitHomingNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyDeviceExitHomingNotifySouthbound(const std::shared_ptr<EventsReplyDeviceExitHomingNotifySouthbound>& msg, const std::shared_ptr<EventsDeviceExitHomingNotifyNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        device_format: 机场格式化
    */
    MeErrorCode SendServicesDeviceFormatSouthbound(const std::shared_ptr<ServicesDeviceFormatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceFormatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDeviceFormatNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceFormatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        device_format: 机场格式化执行结果
    */
    MeErrorCode SendEventsDeviceFormatSouthbound(const std::shared_ptr<EventsDeviceFormatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyDeviceFormatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyDeviceFormatNorthbound(const std::function<void(const std::shared_ptr<EventsReplyDeviceFormatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        开启设备端RTC链路V1转发
    */
    MeErrorCode SendServicesDeviceV1ForwardStartSouthbound(const std::shared_ptr<ServicesDeviceV1ForwardStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceV1ForwardStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDeviceV1ForwardStartNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceV1ForwardStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        关闭设备端RTC链路V1转发
    */
    MeErrorCode SendServicesDeviceV1ForwardStopSouthbound(const std::shared_ptr<ServicesDeviceV1ForwardStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceV1ForwardStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDeviceV1ForwardStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceV1ForwardStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        dongle_power_switch: dongle电源开关
    */
    MeErrorCode SendServicesDonglePowerSwitchSouthbound(const std::shared_ptr<ServicesDonglePowerSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDonglePowerSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDonglePowerSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDonglePowerSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        dongle_power_switch: dongle电源开关切换进度
        北向 Topic: thing/product/uav/events/dongle_power_switch
        南向 Topic: thing/product/uav/events_reply/dongle_power_switch
    */
    ListenIdCode ObserveEventsDonglePowerSwitchNorthbound(const std::function<void(const std::shared_ptr<EventsDonglePowerSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyDonglePowerSwitchSouthbound(const std::shared_ptr<EventsReplyDonglePowerSwitchSouthbound>& msg, const std::shared_ptr<EventsDonglePowerSwitchNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        打开 AI 主动告警: 按多路码流配置开启 AI 目标告警 (EA340)
        南向: thing/product/uav/drc/down/drc_ai_alarm_start
        北向: thing/product/uav/drc/up/drc_ai_alarm_start (回包: result)
    */
    MeErrorCode SendDrcDrcAiAlarmStartSouthbound(const std::shared_ptr<DrcDrcAiAlarmStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiAlarmStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiAlarmStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiAlarmStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        AI 主动告警状态实时推送 (EA340)
        北向: thing/product/uav/drc/up/drc_ai_alarm_state_push
    */
    ListenIdCode ObserveDrcDrcAiAlarmStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcAiAlarmStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        关闭 AI 主动告警 (EA340)
        南向: thing/product/uav/drc/down/drc_ai_alarm_stop
        北向: thing/product/uav/drc/up/drc_ai_alarm_stop (回包: result)
    */
    MeErrorCode SendDrcDrcAiAlarmStopSouthbound(const std::shared_ptr<DrcDrcAiAlarmStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiAlarmStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiAlarmStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiAlarmStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置AI识别目标过滤列表
    */
    MeErrorCode SendDrcDrcAiIdentifyFilterSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyFilterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiIdentifyFilterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置AI识别置信度模式
    */
    MeErrorCode SendDrcDrcAiIdentifyScoreModeSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiIdentifyScoreModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        重置AI识别置信度
    */
    MeErrorCode SendDrcDrcAiIdentifyScoreResetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiIdentifyScoreResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置AI识别置信度
    */
    MeErrorCode SendDrcDrcAiIdentifyScoreSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiIdentifyScoreSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai 识别开关
    */
    MeErrorCode SendDrcDrcAiIdentifySetSouthbound(const std::shared_ptr<DrcDrcAiIdentifySetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifySetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiIdentifySetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifySetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        Ai状态
        北向: thing/product/uav/drc/up/drc_ai_info_push
    */
    ListenIdCode ObserveDrcDrcAiInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcAiInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai 模型选择，根据索引选择模型
    */
    MeErrorCode SendDrcDrcAiModelSelectSouthbound(const std::shared_ptr<DrcDrcAiModelSelectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiModelSelectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiModelSelectNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiModelSelectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ai 模型配置, 配置码流绑定的AI模型
    */
    MeErrorCode SendDrcDrcAiModelsConfigSouthbound(const std::shared_ptr<DrcDrcAiModelsConfigSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiModelsConfigNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiModelsConfigNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiModelsConfigNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        AI框选目标跟随确认
    */
    MeErrorCode SendDrcDrcAiSpotlightZoomConfirmSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomConfirmSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomConfirmNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        AI框选目标跟随
    */
    MeErrorCode SendDrcDrcAiSpotlightZoomSelectSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomSelectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomSelectNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        AI跟随开关设置
    */
    MeErrorCode SendDrcDrcAiSpotlightZoomSetSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        停止目标跟随
    */
    MeErrorCode SendDrcDrcAiSpotlightZoomStopSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        AI识别目标跟随
    */
    MeErrorCode SendDrcDrcAiSpotlightZoomTrackSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomTrackSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomTrackNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机电子除雾等级设置
    */
    MeErrorCode SendDrcDrcAlgorithmDehazeLevelSetSouthbound(const std::shared_ptr<DrcDrcAlgorithmDehazeLevelSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAlgorithmDehazeLevelSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机电子除雾模式设置
    */
    MeErrorCode SendDrcDrcAlgorithmDehazeModeSetSouthbound(const std::shared_ptr<DrcDrcAlgorithmDehazeModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAlgorithmDehazeModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        辅助影像模式设置
    */
    MeErrorCode SendDrcDrcAssistantVideoModeSetSouthbound(const std::shared_ptr<DrcDrcAssistantVideoModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAssistantVideoModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAssistantVideoModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAssistantVideoModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        辅助影像开关设置
    */
    MeErrorCode SendDrcDrcAssistantVideoSwitchSetSouthbound(const std::shared_ptr<DrcDrcAssistantVideoSwitchSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAssistantVideoSwitchSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcAssistantVideoSwitchSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAssistantVideoSwitchSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        电池信息
        北向: thing/product/uav/drc/up/drc_batteries_info_push
    */
    ListenIdCode ObserveDrcDrcBatteriesInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcBatteriesInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        双击AIM
        南向: thing/product/uav/drc/down/drc_camera_aim
        北向: thing/product/uav/drc/up/drc_camera_aim
    */
    MeErrorCode SendDrcDrcCameraAimSouthbound(const std::shared_ptr<DrcDrcCameraAimSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraAimNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraAimNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraAimNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机光圈值设置
    */
    MeErrorCode SendDrcDrcCameraApertureValueSetSouthbound(const std::shared_ptr<DrcDrcCameraApertureValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraApertureValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraApertureValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraApertureValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机降噪等级设置
    */
    MeErrorCode SendDrcDrcCameraDenoiseLevelSetSouthbound(const std::shared_ptr<DrcDrcCameraDenoiseLevelSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraDenoiseLevelSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraDenoiseLevelSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraDenoiseLevelSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机畸变校正设置
    */
    MeErrorCode SendDrcDrcCameraDewarpingSetSouthbound(const std::shared_ptr<DrcDrcCameraDewarpingSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraDewarpingSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraDewarpingSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraDewarpingSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机曝光模式设置
    */
    MeErrorCode SendDrcDrcCameraExposureModeSetSouthbound(const std::shared_ptr<DrcDrcCameraExposureModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraExposureModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraExposureModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraExposureModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机曝光设置
    */
    MeErrorCode SendDrcDrcCameraExposureSetSouthbound(const std::shared_ptr<DrcDrcCameraExposureSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraExposureSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraExposureSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraExposureSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        切换镜头变焦倍率
    */
    MeErrorCode SendDrcDrcCameraFocalLengthSetSouthbound(const std::shared_ptr<DrcDrcCameraFocalLengthSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocalLengthSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraFocalLengthSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocalLengthSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机对焦模式设置
    */
    MeErrorCode SendDrcDrcCameraFocusModeSetSouthbound(const std::shared_ptr<DrcDrcCameraFocusModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocusModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraFocusModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocusModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机对焦值设置
    */
    MeErrorCode SendDrcDrcCameraFocusValueSetSouthbound(const std::shared_ptr<DrcDrcCameraFocusValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocusValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraFocusValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocusValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机帧变焦
    */
    MeErrorCode SendDrcDrcCameraFrameZoomSouthbound(const std::shared_ptr<DrcDrcCameraFrameZoomSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFrameZoomNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraFrameZoomNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFrameZoomNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机ISO设置
    */
    MeErrorCode SendDrcDrcCameraIsoSetSouthbound(const std::shared_ptr<DrcDrcCameraIsoSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraIsoSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraIsoSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraIsoSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机LookAt
    */
    MeErrorCode SendDrcDrcCameraLookAtSouthbound(const std::shared_ptr<DrcDrcCameraLookAtSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraLookAtNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraLookAtNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraLookAtNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机机械快门设置
    */
    MeErrorCode SendDrcDrcCameraMechanicalShutterSetSouthbound(const std::shared_ptr<DrcDrcCameraMechanicalShutterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraMechanicalShutterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraMechanicalShutterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraMechanicalShutterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机模式切换
        南向: thing/product/uav/drc/down/drc_camera_mode_switch
        北向: thing/product/uav/drc/up/drc_camera_mode_switch
    */
    MeErrorCode SendDrcDrcCameraModeSwitchSouthbound(const std::shared_ptr<DrcDrcCameraModeSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraModeSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraModeSwitchNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraModeSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        夜景模式设置
    */
    MeErrorCode SendDrcDrcCameraNightModeSetSouthbound(const std::shared_ptr<DrcDrcCameraNightModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraNightModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraNightModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraNightModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        黑白夜视使能
    */
    MeErrorCode SendDrcDrcCameraNightVisionEnableSouthbound(const std::shared_ptr<DrcDrcCameraNightVisionEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraNightVisionEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraNightVisionEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraNightVisionEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        drc模式下，相机信息推送，3Hz
        北向: thing/product/uav/drc/up/drc_camera_osd_info_push
    */
    ListenIdCode ObserveDrcDrcCameraOsdInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraOsdInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机照片格式设置
    */
    MeErrorCode SendDrcDrcCameraPhotoFormatSetSouthbound(const std::shared_ptr<DrcDrcCameraPhotoFormatSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoFormatSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraPhotoFormatSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoFormatSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机拍照状态推送
        北向: thing/product/uav/drc/up/drc_camera_photo_info_push
    */
    ListenIdCode ObserveDrcDrcCameraPhotoInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraPhotoInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机照片尺寸设置
    */
    MeErrorCode SendDrcDrcCameraPhotoSizeSetSouthbound(const std::shared_ptr<DrcDrcCameraPhotoSizeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraPhotoSizeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        停止拍照：停止全景拍照，停止低光智能
    */
    MeErrorCode SendDrcDrcCameraPhotoStopSouthbound(const std::shared_ptr<DrcDrcCameraPhotoStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraPhotoStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        南向: thing/product/uav/drc/down/drc_camera_photo_take  数据结构: DrcDownDrcCameraPhotoTakeSouthbound   接口: ObserveDrcDownDrcCameraPhotoTakeSouthbound
        北向: thing/product/uav/drc/up/drc_camera_photo_take    数据结构: DrcUpDrcCameraPhotoTakeNorthbound     接口: SendDrcUpDrcCameraPhotoTakeNorthbound
    */
    MeErrorCode SendDrcDrcCameraPhotoTakeSouthbound(const std::shared_ptr<DrcDrcCameraPhotoTakeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoTakeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraPhotoTakeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoTakeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机点对焦操作
    */
    MeErrorCode SendDrcDrcCameraPointFocusActionSouthbound(const std::shared_ptr<DrcDrcCameraPointFocusActionSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPointFocusActionNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraPointFocusActionNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPointFocusActionNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机开始录像
        南向: thing/product/uav/drc/down/drc_camera_recording_start
        北向: thing/product/uav/drc/up/drc_camera_recording_start
    */
    MeErrorCode SendDrcDrcCameraRecordingStartSouthbound(const std::shared_ptr<DrcDrcCameraRecordingStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraRecordingStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraRecordingStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraRecordingStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机停止录像
        南向: thing/product/uav/drc/down/drc_camera_recording_stop
        北向: thing/product/uav/drc/up/drc_camera_recording_stop
    */
    MeErrorCode SendDrcDrcCameraRecordingStopSouthbound(const std::shared_ptr<DrcDrcCameraRecordingStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraRecordingStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraRecordingStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraRecordingStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        长按画面拖动（控制云台转动速度）
    */
    MeErrorCode SendDrcDrcCameraScreenDragSouthbound(const std::shared_ptr<DrcDrcCameraScreenDragSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraScreenDragNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraScreenDragNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraScreenDragNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        分屏
        南向: thing/product/uav/drc/down/drc_camera_screen_split
        北向: thing/product/uav/drc/up/drc_camera_screen_split
    */
    MeErrorCode SendDrcDrcCameraScreenSplitSouthbound(const std::shared_ptr<DrcDrcCameraScreenSplitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraScreenSplitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraScreenSplitNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraScreenSplitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        重置相机参数设置
        不可重置参数情况: 
        1、相机正在视频录制
        2、相机正在定时拍照
    */
    MeErrorCode SendDrcDrcCameraSettingsResetSouthbound(const std::shared_ptr<DrcDrcCameraSettingsResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraSettingsResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraSettingsResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraSettingsResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机快门设置
    */
    MeErrorCode SendDrcDrcCameraShutterSetSouthbound(const std::shared_ptr<DrcDrcCameraShutterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraShutterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraShutterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraShutterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机状态推送
        北向: thing/product/uav/drc/up/drc_camera_state_push
    */
    ListenIdCode ObserveDrcDrcCameraStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置水印
    */
    MeErrorCode SendDrcDrcCameraWatermarkSetSouthbound(const std::shared_ptr<DrcDrcCameraWatermarkSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraWatermarkSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCameraWatermarkSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraWatermarkSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        镜头去雾
    */
    MeErrorCode SendDrcDrcCleanFogSouthbound(const std::shared_ptr<DrcDrcCleanFogSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCleanFogNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCleanFogNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCleanFogNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        倒计时动作设置
    */
    MeErrorCode SendDrcDrcCountDownActionSetSouthbound(const std::shared_ptr<DrcDrcCountDownActionSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCountDownActionSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcCountDownActionSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCountDownActionSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        倒计时信息推送
        北向: thing/product/uav/drc/up/drc_count_down_info_push
    */
    ListenIdCode ObserveDrcDrcCountDownInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCountDownInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备推送drc心跳，让前端保持drc连接
    */
    ListenIdCode ObserveDrcDrcDeviceHeartBeatNorthbound(const std::function<void(const std::shared_ptr<DrcDrcDeviceHeartBeatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        飞机悬停时间设置
    */
    MeErrorCode SendDrcDrcDroneHoverSetSouthbound(const std::shared_ptr<DrcDrcDroneHoverSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcDroneHoverSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcDroneHoverSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcDroneHoverSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        飞行器状态推送
        北向: thing/product/uav/drc/up/drc_drone_state_push
    */
    ListenIdCode ObserveDrcDrcDroneStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcDroneStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        降落：紧急降落
    */
    MeErrorCode SendDrcDrcEmergencyLandingSouthbound(const std::shared_ptr<DrcDrcEmergencyLandingSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcEmergencyLandingNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcEmergencyLandingNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcEmergencyLandingNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        爆闪灯设置
    */
    MeErrorCode SendDrcDrcFlashLedSetSouthbound(const std::shared_ptr<DrcDrcFlashLedSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcFlashLedSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcFlashLedSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcFlashLedSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        指哪打哪参数设置
    */
    MeErrorCode SendDrcDrcFollowMouseParameterSetSouthbound(const std::shared_ptr<DrcDrcFollowMouseParameterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcFollowMouseParameterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcFollowMouseParameterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcFollowMouseParameterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        降落：强制降落
    */
    MeErrorCode SendDrcDrcForceLandingSouthbound(const std::shared_ptr<DrcDrcForceLandingSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcForceLandingNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcForceLandingNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcForceLandingNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云台快捷操作（回中、朝下等）
    */
    MeErrorCode SendDrcDrcGimbalResetSouthbound(const std::shared_ptr<DrcDrcGimbalResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcGimbalResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcGimbalResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcGimbalResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        近红外补光使能
    */
    MeErrorCode SendDrcDrcInfraredFillLightEnableSouthbound(const std::shared_ptr<DrcDrcInfraredFillLightEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcInfraredFillLightEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcInfraredFillLightEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcInfraredFillLightEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        订阅初始状态
    */
    MeErrorCode SendDrcDrcInitialStateSubscribeSouthbound(const std::shared_ptr<DrcDrcInitialStateSubscribeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcInitialStateSubscribeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcInitialStateSubscribeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcInitialStateSubscribeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        定时拍照设置
    */
    MeErrorCode SendDrcDrcIntervalPhotoSetSouthbound(const std::shared_ptr<DrcDrcIntervalPhotoSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIntervalPhotoSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcIntervalPhotoSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIntervalPhotoSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        红外测温区域设置
        南向: thing/product/uav/drc/down/drc_ir_metering_area_set
        北向: thing/product/uav/drc/up/drc_ir_metering_area_set
    */
    MeErrorCode SendDrcDrcIrMeteringAreaSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringAreaSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringAreaSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcIrMeteringAreaSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringAreaSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        红外测温模式设置
        南向: thing/product/uav/drc/down/drc_ir_metering_mode_set
        北向: thing/product/uav/drc/up/drc_ir_metering_mode_set
    */
    MeErrorCode SendDrcDrcIrMeteringModeSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcIrMeteringModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        红外测温点设置
        南向: thing/product/uav/drc/down/drc_ir_metering_point_set
        北向: thing/product/uav/drc/up/drc_ir_metering_point_set
    */
    MeErrorCode SendDrcDrcIrMeteringPointSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringPointSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringPointSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcIrMeteringPointSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringPointSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯 亮度设置
    */
    MeErrorCode SendDrcDrcLightBrightnessSetSouthbound(const std::shared_ptr<DrcDrcLightBrightnessSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightBrightnessSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightBrightnessSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightBrightnessSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯 云台校准
    */
    MeErrorCode SendDrcDrcLightCalibrationSouthbound(const std::shared_ptr<DrcDrcLightCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightCalibrationNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯 云台微调
    */
    MeErrorCode SendDrcDrcLightFineTuningSetSouthbound(const std::shared_ptr<DrcDrcLightFineTuningSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightFineTuningSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightFineTuningSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightFineTuningSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯 云台联动
    */
    MeErrorCode SendDrcDrcLightLightGimbalControlSetSouthbound(const std::shared_ptr<DrcDrcLightLightGimbalControlSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightLightGimbalControlSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯 模式设置
    */
    MeErrorCode SendDrcDrcLightModeSetSouthbound(const std::shared_ptr<DrcDrcLightModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯照明类型设置
    */
    MeErrorCode SendDrcDrcLightTypeSetSouthbound(const std::shared_ptr<DrcDrcLightTypeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightTypeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightTypeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightTypeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 探照灯 广视野模式
    */
    MeErrorCode SendDrcDrcLightWideFieldModeSetSouthbound(const std::shared_ptr<DrcDrcLightWideFieldModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLightWideFieldModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        联动变焦
        南向: thing/product/uav/drc/down/drc_linkage_zoom_set
        北向: thing/product/uav/drc/up/drc_linkage_zoom_set
    */
    MeErrorCode SendDrcDrcLinkageZoomSetSouthbound(const std::shared_ptr<DrcDrcLinkageZoomSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLinkageZoomSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLinkageZoomSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLinkageZoomSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机镜头切换
    */
    MeErrorCode SendDrcDrcLiveLensChangeSouthbound(const std::shared_ptr<DrcDrcLiveLensChangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLiveLensChangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcLiveLensChangeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLiveLensChangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机状态推送
        北向: thing/product/uav/drc/up/drc_live_video_info_push
    */
    ListenIdCode ObserveDrcDrcLiveVideoInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcLiveVideoInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        进入drc模式
    */
    MeErrorCode SendServicesDrcModeEnterSouthbound(const std::shared_ptr<ServicesDrcModeEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcModeEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDrcModeEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcModeEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        退出drc模式
    */
    MeErrorCode SendServicesDrcModeExitSouthbound(const std::shared_ptr<ServicesDrcModeExitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcModeExitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDrcModeExitNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcModeExitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        夜航灯设置
    */
    MeErrorCode SendDrcDrcNightLightsStateSetSouthbound(const std::shared_ptr<DrcDrcNightLightsStateSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcNightLightsStateSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcNightLightsStateSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcNightLightsStateSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        打开降落伞
    */
    MeErrorCode SendDrcDrcOpenParachuteSouthbound(const std::shared_ptr<DrcDrcOpenParachuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcOpenParachuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcOpenParachuteNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcOpenParachuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        降落伞信息推送
    */
    ListenIdCode ObserveDrcDrcParachuteInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcParachuteInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        照片存储设置
    */
    MeErrorCode SendDrcDrcPhotoStorageSetSouthbound(const std::shared_ptr<DrcDrcPhotoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPhotoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcPhotoStorageSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPhotoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        预录制状态设置
    */
    MeErrorCode SendDrcDrcPreRecordedStatusSetSouthbound(const std::shared_ptr<DrcDrcPreRecordedStatusSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPreRecordedStatusSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcPreRecordedStatusSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPreRecordedStatusSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        预录制时间设置
    */
    MeErrorCode SendDrcDrcPreRecordedTimeSetSouthbound(const std::shared_ptr<DrcDrcPreRecordedTimeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPreRecordedTimeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcPreRecordedTimeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPreRecordedTimeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 浮窗文本推送
    */
    ListenIdCode ObserveDrcDrcPsdkFloatingWindowTextNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkFloatingWindowTextNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 三方 PSDK 发送文本框内容
    */
    MeErrorCode SendDrcDrcPsdkInputBoxTextSetSouthbound(const std::shared_ptr<DrcDrcPsdkInputBoxTextSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcPsdkInputBoxTextSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK状态上报
    */
    ListenIdCode ObserveDrcDrcPsdkStateInfoNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkStateInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK状态上报，UI资源
    */
    ListenIdCode ObserveDrcDrcPsdkUiResourceNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkUiResourceNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 设置三方 PSDK 控件值
    */
    MeErrorCode SendDrcDrcPsdkWidgetValueSetSouthbound(const std::shared_ptr<DrcDrcPsdkWidgetValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcPsdkWidgetValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        测试rtc网络质量, 回复仅在is_need_ack为true时发送
    */
    MeErrorCode SendDrcDrcRtcQualityTestSouthbound(const std::shared_ptr<DrcDrcRtcQualityTestSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcRtcQualityTestNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcRtcQualityTestNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcRtcQualityTestNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置普通档最大水平速度
    */
    MeErrorCode SendDrcDrcSetFlyingMaxSpeedSouthbound(const std::shared_ptr<DrcDrcSetFlyingMaxSpeedSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetFlyingMaxSpeedNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetFlyingMaxSpeedNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetFlyingMaxSpeedNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置红外显示模式 - 纯红外画面/红外可见光画中画模式/红外可见光融合画面
    */
    MeErrorCode SendDrcDrcSetIrDisplayModeSouthbound(const std::shared_ptr<DrcDrcSetIrDisplayModeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrDisplayModeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIrDisplayModeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrDisplayModeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置红外liveview增强模式-关闭/边缘增强/热源回帖
    */
    MeErrorCode SendDrcDrcSetIrEnhanceModeSouthbound(const std::shared_ptr<DrcDrcSetIrEnhanceModeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrEnhanceModeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIrEnhanceModeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrEnhanceModeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置红外融合模式比例
    */
    MeErrorCode SendDrcDrcSetIrMixRateSouthbound(const std::shared_ptr<DrcDrcSetIrMixRateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrMixRateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIrMixRateNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrMixRateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置温域聚焦功能画面着色效果类型
    */
    MeErrorCode SendDrcDrcSetIsothermColorRenderingTypeSouthbound(const std::shared_ptr<DrcDrcSetIsothermColorRenderingTypeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIsothermColorRenderingTypeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置温域聚焦开关（等温线开关）
    */
    MeErrorCode SendDrcDrcSetIsothermEnableSouthbound(const std::shared_ptr<DrcDrcSetIsothermEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIsothermEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        手动设置温域聚焦上下限温度
    */
    MeErrorCode SendDrcDrcSetIsothermLowerUpperRangeSouthbound(const std::shared_ptr<DrcDrcSetIsothermLowerUpperRangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIsothermLowerUpperRangeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置温域聚焦多边形区域测温的区域顶点坐标
    */
    MeErrorCode SendDrcDrcSetIsothermRenderingPolygonSouthbound(const std::shared_ptr<DrcDrcSetIsothermRenderingPolygonSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIsothermRenderingPolygonNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置按框选区域设置温域聚焦使能
    */
    MeErrorCode SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound(const std::shared_ptr<DrcDrcSetIsothermRoiBasedRenderingEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 开始文件播放
    */
    MeErrorCode SendDrcDrcSpeakerAudioPlayStartSouthbound(const std::shared_ptr<DrcDrcSpeakerAudioPlayStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerAudioPlayStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 模式设置
    */
    MeErrorCode SendDrcDrcSpeakerPlayModeSetSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerPlayModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 音频播放进度上报
    */
    ListenIdCode ObserveDrcDrcSpeakerPlayProgressNorthbound(const std::function<void(const std::shared_ptr<DrcDrcSpeakerPlayProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 停止播放
    */
    MeErrorCode SendDrcDrcSpeakerPlayStopSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerPlayStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 音量设置
    */
    MeErrorCode SendDrcDrcSpeakerPlayVolumeSetSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayVolumeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerPlayVolumeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 实时数据传输
    */
    MeErrorCode SendDrcDrcSpeakerRealTimeDataTransmitSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeDataTransmitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerRealTimeDataTransmitNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 实时播放开始
    */
    MeErrorCode SendDrcDrcSpeakerRealTimeStartSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerRealTimeStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 实时播放停止
    */
    MeErrorCode SendDrcDrcSpeakerRealTimeStopSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerRealTimeStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 重播
    */
    MeErrorCode SendDrcDrcSpeakerReplaySouthbound(const std::shared_ptr<DrcDrcSpeakerReplaySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerReplayNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerReplayNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerReplayNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 TTS播放开始
    */
    MeErrorCode SendDrcDrcSpeakerTtsPlayStartSouthbound(const std::shared_ptr<DrcDrcSpeakerTtsPlayStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerTtsPlayStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 TTS设置
    */
    MeErrorCode SendDrcDrcSpeakerTtsSetSouthbound(const std::shared_ptr<DrcDrcSpeakerTtsSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerTtsSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcSpeakerTtsSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerTtsSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        隐蔽模式设置
    */
    MeErrorCode SendDrcDrcStealthStateSetSouthbound(const std::shared_ptr<DrcDrcStealthStateSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcStealthStateSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcStealthStateSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcStealthStateSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        调色盘
        南向: thing/product/uav/drc/down/drc_thermal_current_palette_set
        北向: thing/product/uav/drc/up/drc_thermal_current_palette_set
    */
    MeErrorCode SendDrcDrcThermalCurrentPaletteSetSouthbound(const std::shared_ptr<DrcDrcThermalCurrentPaletteSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalCurrentPaletteSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcThermalCurrentPaletteSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalCurrentPaletteSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        红外增益模式
        南向: thing/product/uav/drc/down/drc_thermal_gain_mode_set
        北向: thing/product/uav/drc/up/drc_thermal_gain_mode_set
    */
    MeErrorCode SendDrcDrcThermalGainModeSetSouthbound(const std::shared_ptr<DrcDrcThermalGainModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalGainModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcThermalGainModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalGainModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        红外超分辨率设置
    */
    MeErrorCode SendDrcDrcThermalSuperResolutionEnableSouthbound(const std::shared_ptr<DrcDrcThermalSuperResolutionEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalSuperResolutionEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcThermalSuperResolutionEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalSuperResolutionEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        视频尺寸设置
    */
    MeErrorCode SendDrcDrcVideoResolutionSetSouthbound(const std::shared_ptr<DrcDrcVideoResolutionSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcVideoResolutionSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcVideoResolutionSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcVideoResolutionSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        视频存储设置
    */
    MeErrorCode SendDrcDrcVideoStorageSetSouthbound(const std::shared_ptr<DrcDrcVideoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcVideoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDrcVideoStorageSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcVideoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        PSDK 喊话器 实时喊话房间加入
    */
    MeErrorCode SendServicesDrcVoiceEnterSouthbound(const std::shared_ptr<ServicesDrcVoiceEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcVoiceEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyDrcVoiceEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcVoiceEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        飞行器急停
    */
    MeErrorCode SendDrcDroneEmergencyStopSouthbound(const std::shared_ptr<DrcDroneEmergencyStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDroneEmergencyStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyDroneEmergencyStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDroneEmergencyStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_ready: 任务准备结果
    */
    MeErrorCode SendEventsEmergencyStopNotifySouthbound(const std::shared_ptr<EventsEmergencyStopNotifySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyEmergencyStopNotifyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyEmergencyStopNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyEmergencyStopNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        esim_operator_switch: eSim运营商切换进度
        北向 Topic: thing/product/uav/events/esim_operator_switch
        南向 Topic: thing/product/uav/events_reply/esim_operator_switch
    */
    ListenIdCode ObserveEventsEsimOperatorSwitchNorthbound(const std::function<void(const std::shared_ptr<EventsEsimOperatorSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyEsimOperatorSwitchSouthbound(const std::shared_ptr<EventsReplyEsimOperatorSwitchSouthbound>& msg, const std::shared_ptr<EventsEsimOperatorSwitchNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        esim_operator_switch: eSim运营商切换
    */
    MeErrorCode SendServicesEsimOperatorSwitchSouthbound(const std::shared_ptr<ServicesEsimOperatorSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyEsimOperatorSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyEsimOperatorSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyEsimOperatorSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        fileupload_list: 远程日志列表
    */
    MeErrorCode SendServicesFileuploadListSouthbound(const std::shared_ptr<ServicesFileuploadListSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadListNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFileuploadListNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadListNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        fileupload_progress: 日志上传进度
    */
    MeErrorCode SendEventsFileuploadProgressSouthbound(const std::shared_ptr<EventsFileuploadProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFileuploadProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyFileuploadProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFileuploadProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        fileupload_start: 远程日志上传
    */
    MeErrorCode SendServicesFileuploadStartSouthbound(const std::shared_ptr<ServicesFileuploadStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFileuploadStartNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        fileupload_update: 远程日志更新
    */
    MeErrorCode SendServicesFileuploadUpdateSouthbound(const std::shared_ptr<ServicesFileuploadUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFileuploadUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        自定义飞行区告警信息推送
    */
    ListenIdCode ObserveEventsFlightAreasDroneLocationNorthbound(const std::function<void(const std::shared_ptr<EventsFlightAreasDroneLocationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyFlightAreasDroneLocationSouthbound(const std::shared_ptr<EventsReplyFlightAreasDroneLocationSouthbound>& msg, const std::shared_ptr<EventsFlightAreasDroneLocationNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备端请求获取flight_areas_get资源
        北向: thing/product/uav/requests/flight_areas_get
        南向: thing/product/uav/requests_reply/flight_areas_get
    */
    ListenIdCode ObserveRequestsFlightAreasGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlightAreasGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyFlightAreasGetSouthbound(const std::shared_ptr<RequestsReplyFlightAreasGetSouthbound>& msg, const std::shared_ptr<RequestsFlightAreasGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flight_areas_sync_progress: 自定义飞行区同步进度
    */
    MeErrorCode SendEventsFlightAreasSyncProgressSouthbound(const std::shared_ptr<EventsFlightAreasSyncProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlightAreasSyncProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyFlightAreasSyncProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlightAreasSyncProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flight_areas_update: 更新自定义飞行区
    */
    MeErrorCode SendServicesFlightAreasUpdateSouthbound(const std::shared_ptr<ServicesFlightAreasUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlightAreasUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlightAreasUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlightAreasUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云端抢夺设备飞行控制权
    */
    MeErrorCode SendServicesFlightAuthorityGrabSouthbound(const std::shared_ptr<ServicesFlightAuthorityGrabSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlightAuthorityGrabNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlightAuthorityGrabNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlightAuthorityGrabNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_execute: 执行计划航线
    */
    MeErrorCode SendServicesFlighttaskExecuteSouthbound(const std::shared_ptr<ServicesFlighttaskExecuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskExecuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlighttaskExecuteNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskExecuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        暂停航线
    */
    MeErrorCode SendServicesFlighttaskPauseSouthbound(const std::shared_ptr<ServicesFlighttaskPauseSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskPauseNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlighttaskPauseNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskPauseNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_prepare: 准备计划航线
    */
    MeErrorCode SendServicesFlighttaskPrepareSouthbound(const std::shared_ptr<ServicesFlighttaskPrepareSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskPrepareNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlighttaskPrepareNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskPrepareNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_progress: 执行计划航线进度
    */
    ListenIdCode ObserveEventsFlighttaskProgressNorthbound(const std::function<void(const std::shared_ptr<EventsFlighttaskProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyFlighttaskProgressSouthbound(const std::shared_ptr<EventsReplyFlighttaskProgressSouthbound>& msg, const std::shared_ptr<EventsFlighttaskProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备端请求获取目标设备的任务进度
    */
    ListenIdCode ObserveRequestsFlighttaskProgressGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlighttaskProgressGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyFlighttaskProgressGetSouthbound(const std::shared_ptr<RequestsReplyFlighttaskProgressGetSouthbound>& msg, const std::shared_ptr<RequestsFlighttaskProgressGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_ready: 任务准备结果
    */
    MeErrorCode SendEventsFlighttaskReadySouthbound(const std::shared_ptr<EventsFlighttaskReadySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlighttaskReadyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyFlighttaskReadyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlighttaskReadyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        恢复航线
    */
    MeErrorCode SendServicesFlighttaskRecoverySouthbound(const std::shared_ptr<ServicesFlighttaskRecoverySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskRecoveryNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlighttaskRecoveryNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskRecoveryNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备端请求获取flighttask资源
        北向: thing/product/uav/requests/flighttask_resource_get
        南向: thing/product/uav/requests_reply/flighttask_resource_get
    */
    ListenIdCode ObserveRequestsFlighttaskResourceGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlighttaskResourceGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyFlighttaskResourceGetSouthbound(const std::shared_ptr<RequestsReplyFlighttaskResourceGetSouthbound>& msg, const std::shared_ptr<RequestsFlighttaskResourceGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_stop: 多机场任务，A机场结束任务后，取消其余机场任务
    */
    MeErrorCode SendServicesFlighttaskStopSouthbound(const std::shared_ptr<ServicesFlighttaskStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlighttaskStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flighttask_undo: 取消未执行任务
    */
    MeErrorCode SendServicesFlighttaskUndoSouthbound(const std::shared_ptr<ServicesFlighttaskUndoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskUndoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlighttaskUndoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskUndoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        FlyTo: 飞向目标点
    */
    MeErrorCode SendServicesFlyToPointSouthbound(const std::shared_ptr<ServicesFlyToPointSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlyToPointNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        FlyTo: flyto 执行结果事件通知
        北向 Topic: thing/product/uav/events/fly_to_point_progress
        南向 Topic: thing/product/uav/events_reply/fly_to_point_progress
    */
    ListenIdCode ObserveEventsFlyToPointProgressNorthbound(const std::function<void(const std::shared_ptr<EventsFlyToPointProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyFlyToPointProgressSouthbound(const std::shared_ptr<EventsReplyFlyToPointProgressSouthbound>& msg, const std::shared_ptr<EventsFlyToPointProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        FlyTo: 结束 flyto 飞向目标点任务
    */
    MeErrorCode SendServicesFlyToPointStopSouthbound(const std::shared_ptr<ServicesFlyToPointStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlyToPointStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        FlyTo: 更新 flyto 目标点
    */
    MeErrorCode SendServicesFlyToPointUpdateSouthbound(const std::shared_ptr<ServicesFlyToPointUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlyToPointUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flysafe_database_upgrade: 限飞数据库升级进度
    */
    MeErrorCode SendEventsFlysafeDatabaseUpgradeSouthbound(const std::shared_ptr<EventsFlysafeDatabaseUpgradeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyFlysafeDatabaseUpgradeNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        flysafe_database_upgrade: 限飞数据库升级
    */
    MeErrorCode SendServicesFlysafeDatabaseUpgradeSouthbound(const std::shared_ptr<ServicesFlysafeDatabaseUpgradeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyFlysafeDatabaseUpgradeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云端推送drc心跳, 保持drc连接
    */
    MeErrorCode SendDrcHeartBeatSouthbound(const std::shared_ptr<DrcHeartBeatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyHeartBeatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveDrcReplyHeartBeatNorthbound(const std::function<void(const std::shared_ptr<DrcReplyHeartBeatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        飞机HMS上报
    */
    ListenIdCode ObserveEventsHmsNorthbound(const std::function<void(const std::shared_ptr<EventsHmsNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        飞行器HSI信息推送
        北向: thing/product/uav/drc/up/hsi_info_push
    */
    ListenIdCode ObserveDrcHsiInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcHsiInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        北向: thing/product/uav/requests/in_flight_task_request
        南向: thing/product/uav/requests_reply/in_flight_task_request
    */
    ListenIdCode ObserveRequestsInFlightTaskRequestNorthbound(const std::function<void(const std::shared_ptr<RequestsInFlightTaskRequestNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyInFlightTaskRequestSouthbound(const std::shared_ptr<RequestsReplyInFlightTaskRequestSouthbound>& msg, const std::shared_ptr<RequestsInFlightTaskRequestNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        in_flight_wayline_cancel: 空中航线取消
    */
    MeErrorCode SendServicesInFlightWaylineCancelSouthbound(const std::shared_ptr<ServicesInFlightWaylineCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyInFlightWaylineCancelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        in_flight_wayline_deliver: 空中下发航线
    */
    MeErrorCode SendServicesInFlightWaylineDeliverSouthbound(const std::shared_ptr<ServicesInFlightWaylineDeliverSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineDeliverNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyInFlightWaylineDeliverNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineDeliverNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        in_flight_wayline_progress: 空中航线进度
    */
    ListenIdCode ObserveEventsInFlightWaylineProgressNorthbound(const std::function<void(const std::shared_ptr<EventsInFlightWaylineProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        in_flight_wayline_recover: 空中航线恢复
    */
    MeErrorCode SendServicesInFlightWaylineRecoverSouthbound(const std::shared_ptr<ServicesInFlightWaylineRecoverSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineRecoverNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyInFlightWaylineRecoverNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineRecoverNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        in_flight_wayline_stop: 空中航线停止
    */
    MeErrorCode SendServicesInFlightWaylineStopSouthbound(const std::shared_ptr<ServicesInFlightWaylineStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyInFlightWaylineStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        相机镜头切换
    */
    MeErrorCode SendServicesLiveLensChangeSouthbound(const std::shared_ptr<ServicesLiveLensChangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveLensChangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyLiveLensChangeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveLensChangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设置直播清晰度
    */
    MeErrorCode SendServicesLiveSetQualitySouthbound(const std::shared_ptr<ServicesLiveSetQualitySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveSetQualityNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyLiveSetQualityNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveSetQualityNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云端请求开启直播
        南向: thing/product/uav/services/live_start_push        数据结构: ServicesLiveStartPushSouthbound       接口: ObserveServicesLiveStartPushSouthbound
        北向: thing/product/uav/services_reply/live_start_push  数据结构: ServicesReplyLiveStartPushNorthbound  接口: SendServicesReplyLiveStartPushNorthbound
    */
    MeErrorCode SendServicesLiveStartPushSouthbound(const std::shared_ptr<ServicesLiveStartPushSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveStartPushNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyLiveStartPushNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveStartPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云端请求停止直播
    */
    MeErrorCode SendServicesLiveStopPushSouthbound(const std::shared_ptr<ServicesLiveStopPushSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveStopPushNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyLiveStopPushNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveStopPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        load_ai_box_app: 加载AI盒子应用
    */
    MeErrorCode SendServicesLoadAiBoxAppSouthbound(const std::shared_ptr<ServicesLoadAiBoxAppSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLoadAiBoxAppNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyLoadAiBoxAppNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLoadAiBoxAppNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        load_ai_box_app_progress: 加载AI盒子应用进度
    */
    MeErrorCode SendEventsLoadAiBoxAppProgressSouthbound(const std::shared_ptr<EventsLoadAiBoxAppProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyLoadAiBoxAppProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyLoadAiBoxAppProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyLoadAiBoxAppProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        load_ai_model: 加载AI模型
    */
    MeErrorCode SendServicesLoadAiModelSouthbound(const std::shared_ptr<ServicesLoadAiModelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLoadAiModelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyLoadAiModelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLoadAiModelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        load_ai_model_progress: 加载AI模型进度
    */
    MeErrorCode SendEventsLoadAiModelProgressSouthbound(const std::shared_ptr<EventsLoadAiModelProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyLoadAiModelProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyLoadAiModelProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyLoadAiModelProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        measure_sdr_signal_enter: 进入测量
    */
    MeErrorCode SendServicesMeasureSdrSignalEnterSouthbound(const std::shared_ptr<ServicesMeasureSdrSignalEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyMeasureSdrSignalEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyMeasureSdrSignalEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyMeasureSdrSignalEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        obstacle_avoidance_notify: 避障结果通知
    */
    MeErrorCode SendEventsObstacleAvoidanceNotifySouthbound(const std::shared_ptr<EventsObstacleAvoidanceNotifySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyObstacleAvoidanceNotifyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyObstacleAvoidanceNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyObstacleAvoidanceNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备端请求获取offline_map_get资源
        北向: thing/product/uav/requests/offline_map_get
        南向: thing/product/uav/requests_reply/offline_map_get
    */
    ListenIdCode ObserveRequestsOfflineMapGetNorthbound(const std::function<void(const std::shared_ptr<RequestsOfflineMapGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyOfflineMapGetSouthbound(const std::shared_ptr<RequestsReplyOfflineMapGetSouthbound>& msg, const std::shared_ptr<RequestsOfflineMapGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备端请求获取offline_map_get_v2资源
        北向: thing/product/uav/requests/offline_map_get_v2
        南向: thing/product/uav/requests_reply/offline_map_get_v2
    */
    ListenIdCode ObserveRequestsOfflineMapGetV2Northbound(const std::function<void(const std::shared_ptr<RequestsOfflineMapGetV2Northbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyOfflineMapGetV2Southbound(const std::shared_ptr<RequestsReplyOfflineMapGetV2Southbound>& msg, const std::shared_ptr<RequestsOfflineMapGetV2Northbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        offline_map_sync_progress: 离线地图同步进度
    */
    MeErrorCode SendEventsOfflineMapSyncProgressSouthbound(const std::shared_ptr<EventsOfflineMapSyncProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyOfflineMapSyncProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyOfflineMapSyncProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyOfflineMapSyncProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        offline_map_update: 更新自定义飞行区
    */
    MeErrorCode SendServicesOfflineMapUpdateSouthbound(const std::shared_ptr<ServicesOfflineMapUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyOfflineMapUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyOfflineMapUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyOfflineMapUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        打开降落伞
    */
    MeErrorCode SendServicesOpenParachuteSouthbound(const std::shared_ptr<ServicesOpenParachuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyOpenParachuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyOpenParachuteNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyOpenParachuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        飞行器OSD信息
        北向: thing/product/uav/drc/up/osd_info_push
    */
    ListenIdCode ObserveDrcOsdInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcOsdInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云端抢夺设备负载控制权
    */
    MeErrorCode SendServicesPayloadAuthorityGrabSouthbound(const std::shared_ptr<ServicesPayloadAuthorityGrabSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyPayloadAuthorityGrabNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyPayloadAuthorityGrabNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyPayloadAuthorityGrabNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        照片存储设置
    */
    MeErrorCode SendServicesPhotoStorageSetSouthbound(const std::shared_ptr<ServicesPhotoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyPhotoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyPhotoStorageSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyPhotoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        POI 会话状态与运行参数上报
        北向 Topic: thing/product/uav/events/poi_status_notify
        南向 Topic: thing/product/uav/events_reply/poi_status_notify
    */
    ListenIdCode ObserveEventsPoiStatusNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsPoiStatusNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        relay_connect: 中继连接
    */
    MeErrorCode SendServicesRelayConnectSouthbound(const std::shared_ptr<ServicesRelayConnectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayConnectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRelayConnectNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayConnectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        relay_disconnect: 中继断开
    */
    MeErrorCode SendServicesRelayDisconnectSouthbound(const std::shared_ptr<ServicesRelayDisconnectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayDisconnectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRelayDisconnectNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayDisconnectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        relay_non_pair_attach_result: 中继非配对连接结果
    */
    MeErrorCode SendEventsRelayNonPairAttachResultSouthbound(const std::shared_ptr<EventsRelayNonPairAttachResultSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRelayNonPairAttachResultNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyRelayNonPairAttachResultNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRelayNonPairAttachResultNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        relay_passwd_hash: 中继密码哈希
    */
    MeErrorCode SendServicesRelayPasswdHashSouthbound(const std::shared_ptr<ServicesRelayPasswdHashSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayPasswdHashNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRelayPasswdHashNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayPasswdHashNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        relay_scan: 中继扫描
    */
    MeErrorCode SendServicesRelayScanSouthbound(const std::shared_ptr<ServicesRelayScanSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayScanNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRelayScanNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayScanNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        relay_scan_result: 中继扫描结果
    */
    MeErrorCode SendEventsRelayScanResultSouthbound(const std::shared_ptr<EventsRelayScanResultSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRelayScanResultNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyRelayScanResultNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRelayScanResultNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        release_terminal_control_area: 释放终端控制区域
    */
    MeErrorCode SendEventsReleaseTerminalControlAreaSouthbound(const std::shared_ptr<EventsReleaseTerminalControlAreaSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyReleaseTerminalControlAreaNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyReleaseTerminalControlAreaNorthbound(const std::function<void(const std::shared_ptr<EventsReplyReleaseTerminalControlAreaNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        request_landing_permission: 请求降落许可
    */
    ListenIdCode ObserveRequestsRequestLandingPermissionNorthbound(const std::function<void(const std::shared_ptr<RequestsRequestLandingPermissionNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyRequestLandingPermissionSouthbound(const std::shared_ptr<RequestsReplyRequestLandingPermissionSouthbound>& msg, const std::shared_ptr<RequestsRequestLandingPermissionNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        requests_ai_model_url: 请求AI模型URL
    */
    ListenIdCode ObserveRequestsRequestsAiModelUrlNorthbound(const std::function<void(const std::shared_ptr<RequestsRequestsAiModelUrlNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyRequestsAiModelUrlSouthbound(const std::shared_ptr<RequestsReplyRequestsAiModelUrlSouthbound>& msg, const std::shared_ptr<RequestsRequestsAiModelUrlNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ReturnHome: ReturnHome 执行结果事件通知
    */
    MeErrorCode SendServicesReturnHomeSouthbound(const std::shared_ptr<ServicesReturnHomeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnHomeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyReturnHomeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnHomeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        取消ReturnHome: 取消ReturnHome 执行结果事件通知
    */
    MeErrorCode SendServicesReturnHomeCancelSouthbound(const std::shared_ptr<ServicesReturnHomeCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnHomeCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyReturnHomeCancelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnHomeCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        ReturnHome: ReturnHome 状态上报
    */
    ListenIdCode ObserveEventsReturnHomeInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReturnHomeInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyReturnHomeInfoSouthbound(const std::shared_ptr<EventsReplyReturnHomeInfoSouthbound>& msg, const std::shared_ptr<EventsReturnHomeInfoNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        return_specific_home: 返航到指定home点
    */
    MeErrorCode SendServicesReturnSpecificHomeSouthbound(const std::shared_ptr<ServicesReturnSpecificHomeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnSpecificHomeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyReturnSpecificHomeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnSpecificHomeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        rid_avoidance_enable: 启用RID避障
    */
    MeErrorCode SendServicesRidAvoidanceEnableSouthbound(const std::shared_ptr<ServicesRidAvoidanceEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRidAvoidanceEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRidAvoidanceEnableNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRidAvoidanceEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        rid_warning: RID告警
    */
    ListenIdCode ObserveEventsRidWarningNorthbound(const std::function<void(const std::shared_ptr<EventsRidWarningNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyRidWarningSouthbound(const std::shared_ptr<EventsReplyRidWarningSouthbound>& msg, const std::shared_ptr<EventsRidWarningNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        通知机场加入 rtc_agent 房间
    */
    MeErrorCode SendServicesRtAgentChannelJoinSouthbound(const std::shared_ptr<ServicesRtAgentChannelJoinSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRtAgentChannelJoinNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRtAgentChannelJoinNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRtAgentChannelJoinNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        北向: thing/product/uav/requests/rt_agent_channel_open
        南向: thing/product/uav/requests_reply/rt_agent_channel_open
    */
    ListenIdCode ObserveRequestsRtAgentChannelOpenNorthbound(const std::function<void(const std::shared_ptr<RequestsRtAgentChannelOpenNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyRtAgentChannelOpenSouthbound(const std::shared_ptr<RequestsReplyRtAgentChannelOpenSouthbound>& msg, const std::shared_ptr<RequestsRtAgentChannelOpenNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        北向: thing/product/uav/requests/rt_agent_channel_status_get
        南向: thing/product/uav/requests_reply/rt_agent_channel_status_get
        获取 rtc_agent 状态
    */
    ListenIdCode ObserveRequestsRtAgentChannelStatusGetNorthbound(const std::function<void(const std::shared_ptr<RequestsRtAgentChannelStatusGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyRtAgentChannelStatusGetSouthbound(const std::shared_ptr<RequestsReplyRtAgentChannelStatusGetSouthbound>& msg, const std::shared_ptr<RequestsRtAgentChannelStatusGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        rtk_calibration: 一键标定
    */
    MeErrorCode SendServicesRtkCalibrationSouthbound(const std::shared_ptr<ServicesRtkCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRtkCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyRtkCalibrationNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRtkCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        rtk_calibration: 一键标定执行结果
    */
    MeErrorCode SendEventsRtkCalibrationSouthbound(const std::shared_ptr<EventsRtkCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRtkCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyRtkCalibrationNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRtkCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        sdr_debug_info: 任务准备结果
    */
    MeErrorCode SendEventsSdrDebugInfoSouthbound(const std::shared_ptr<EventsSdrDebugInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySdrDebugInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplySdrDebugInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReplySdrDebugInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        sdr_debug_switch: SDR调试开关
    */
    MeErrorCode SendServicesSdrDebugSwitchSouthbound(const std::shared_ptr<ServicesSdrDebugSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrDebugSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySdrDebugSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrDebugSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        sdr_link_set_config: 图传频段配置（包括切换工作频段、打开/关闭警用与民用频段）
    */
    MeErrorCode SendServicesSdrLinkSetConfigSouthbound(const std::shared_ptr<ServicesSdrLinkSetConfigSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrLinkSetConfigNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySdrLinkSetConfigNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrLinkSetConfigNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        sdr_workmode_switch: 4G开关
    */
    MeErrorCode SendServicesSdrWorkmodeSwitchSouthbound(const std::shared_ptr<ServicesSdrWorkmodeSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrWorkmodeSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySdrWorkmodeSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrWorkmodeSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        self_diagnostic: 一键定损
    */
    MeErrorCode SendServicesSelfDiagnosticSouthbound(const std::shared_ptr<ServicesSelfDiagnosticSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySelfDiagnosticNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySelfDiagnosticNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySelfDiagnosticNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        self_diagnostic: 一键定损执行结果
    */
    MeErrorCode SendEventsSelfDiagnosticSouthbound(const std::shared_ptr<EventsSelfDiagnosticSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySelfDiagnosticNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplySelfDiagnosticNorthbound(const std::function<void(const std::shared_ptr<EventsReplySelfDiagnosticNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        self_diagnostic_cancel: 一键标定取消
    */
    MeErrorCode SendEventsSelfDiagnosticCancelSouthbound(const std::shared_ptr<EventsSelfDiagnosticCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySelfDiagnosticCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplySelfDiagnosticCancelNorthbound(const std::function<void(const std::shared_ptr<EventsReplySelfDiagnosticCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        set_relay_occupied_status: 设置中继占用状态
    */
    MeErrorCode SendServicesSetRelayOccupiedStatusSouthbound(const std::shared_ptr<ServicesSetRelayOccupiedStatusSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySetRelayOccupiedStatusNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySetRelayOccupiedStatusNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySetRelayOccupiedStatusNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        sim_slot_switch: sim和esim切换
    */
    MeErrorCode SendServicesSimSlotSwitchSouthbound(const std::shared_ptr<ServicesSimSlotSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySimSlotSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySimSlotSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySimSlotSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        spotlight_zoom_close: 关闭 Spotlight Zoom
    */
    MeErrorCode SendServicesSpotlightZoomCloseSouthbound(const std::shared_ptr<ServicesSpotlightZoomCloseSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySpotlightZoomCloseNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySpotlightZoomCloseNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySpotlightZoomCloseNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        spotlight_zoom_open: 开启 Spotlight Zoom, 看向目标点
    */
    MeErrorCode SendServicesSpotlightZoomOpenSouthbound(const std::shared_ptr<ServicesSpotlightZoomOpenSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySpotlightZoomOpenNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplySpotlightZoomOpenNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySpotlightZoomOpenNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备静态拓扑上报，目前无回包，没有southbound消息，应该需要和动态拓扑一样，有southbound消息
    */
    ListenIdCode ObserveStatusStaticTopoNorthbound(const std::function<void(const std::shared_ptr<StatusStaticTopoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        station_measure_report: 基站测量上报
    */
    ListenIdCode ObserveEventsStationMeasureReportNorthbound(const std::function<void(const std::shared_ptr<EventsStationMeasureReportNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyStationMeasureReportSouthbound(const std::shared_ptr<EventsReplyStationMeasureReportSouthbound>& msg, const std::shared_ptr<EventsStationMeasureReportNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        云端发生drc杆量控制
    */
    MeErrorCode SendDrcStickControlSouthbound(const std::shared_ptr<DrcStickControlSouthbound>& msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        设备端请求获取flighttask资源
        北向: thing/product/uav/requests/storage_config_get
        南向: thing/product/uav/requests_reply/storage_config_get
    */
    ListenIdCode ObserveRequestsStorageConfigGetNorthbound(const std::function<void(const std::shared_ptr<RequestsStorageConfigGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendRequestsReplyStorageConfigGetSouthbound(const std::shared_ptr<RequestsReplyStorageConfigGetSouthbound>& msg, const std::shared_ptr<RequestsStorageConfigGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        takeoff_to_point: 一键起飞
    */
    MeErrorCode SendServicesTakeoffToPointSouthbound(const std::shared_ptr<ServicesTakeoffToPointSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyTakeoffToPointNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyTakeoffToPointNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyTakeoffToPointNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        OneTakeOff: 一键起飞 执行结果事件通知
        北向 Topic: thing/product/uav/events/takeoff_to_point_progress
        南向 Topic: thing/product/uav/events_reply/takeoff_to_point_progress
    */
    ListenIdCode ObserveEventsTakeoffToPointProgressNorthbound(const std::function<void(const std::shared_ptr<EventsTakeoffToPointProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendEventsReplyTakeoffToPointProgressSouthbound(const std::shared_ptr<EventsReplyTakeoffToPointProgressSouthbound>& msg, const std::shared_ptr<EventsTakeoffToPointProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        thumbnail_upload_finish: 缩略图上传完成
    */
    MeErrorCode SendEventsThumbnailUploadFinishSouthbound(const std::shared_ptr<EventsThumbnailUploadFinishSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyThumbnailUploadFinishNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyThumbnailUploadFinishNorthbound(const std::function<void(const std::shared_ptr<EventsReplyThumbnailUploadFinishNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        unload_ai_model: 卸载AI模型
    */
    MeErrorCode SendServicesUnloadAiModelSouthbound(const std::shared_ptr<ServicesUnloadAiModelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnloadAiModelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyUnloadAiModelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnloadAiModelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        unload_ai_model_progress: 卸载AI模型进度
    */
    MeErrorCode SendEventsUnloadAiModelProgressSouthbound(const std::shared_ptr<EventsUnloadAiModelProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyUnloadAiModelProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyUnloadAiModelProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyUnloadAiModelProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        unlock_license_switch: 解禁license开关
    */
    MeErrorCode SendServicesUnlockLicenseSwitchSouthbound(const std::shared_ptr<ServicesUnlockLicenseSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnlockLicenseSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyUnlockLicenseSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnlockLicenseSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        unlock_license_update: 解禁license更新
    */
    MeErrorCode SendServicesUnlockLicenseUpdateSouthbound(const std::shared_ptr<ServicesUnlockLicenseUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnlockLicenseUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyUnlockLicenseUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnlockLicenseUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        uom_fly_data_info: 无人机飞行数据信息
    */
    MeErrorCode SendEventsUomFlyDataInfoSouthbound(const std::shared_ptr<EventsUomFlyDataInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyUomFlyDataInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveEventsReplyUomFlyDataInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReplyUomFlyDataInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        北向: thing/product/uav/status/update_topo        数据结构: StatusUpdateTopoNorthbound       接口: SendStatusUpdateTopoNorthbound
        南向: thing/product/uav/status_reply/update_topo  数据结构: StatusReplyUpdateTopoSouthbound  接口: ObserveStatusReplyUpdateTopoSouthbound
    */
    ListenIdCode ObserveStatusUpdateTopoNorthbound(const std::function<void(const std::shared_ptr<StatusUpdateTopoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    MeErrorCode SendStatusReplyUpdateTopoSouthbound(const std::shared_ptr<StatusReplyUpdateTopoSouthbound>& msg, const std::shared_ptr<StatusUpdateTopoNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        upload_flighttask_media_prioritize: 媒体上传优先级
    */
    MeErrorCode SendServicesUploadFlighttaskMediaPrioritizeSouthbound(const std::shared_ptr<ServicesUploadFlighttaskMediaPrioritizeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyUploadFlighttaskMediaPrioritizeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        视频存储设置
    */
    MeErrorCode SendServicesVideoStorageSetSouthbound(const std::shared_ptr<ServicesVideoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyVideoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyVideoStorageSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyVideoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

    /*
        write_sdr_topo_info: 写号
    */
    MeErrorCode SendServicesWriteSdrTopoInfoSouthbound(const std::shared_ptr<ServicesWriteSdrTopoInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyWriteSdrTopoInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;
    ListenIdCode ObserveServicesReplyWriteSdrTopoInfoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyWriteSdrTopoInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) override;

};
} // namespace message_engine