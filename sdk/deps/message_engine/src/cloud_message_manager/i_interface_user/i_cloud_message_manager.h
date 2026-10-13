// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include <functional>
#include <base/async/executor.h>
#include "message_all.h"

namespace message_engine {

class ICloudMessageManager {
public:
    virtual ~ICloudMessageManager() = default;

    virtual void CancelObserveMessage(ListenIdCode listen_id) = 0;

public:
///------ 用户自定义接口声明 start ------
    /*
        V3 压缩杆量控制（二进制 MessageV3，脚本无法生成）
    */
    virtual MeErrorCode SendCompressedSticksControlSouthbound(const std::shared_ptr<CompressedSticksControlSouthbound>& msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
///------ 用户自定义接口声明 end ------
public:

    /*
        ai_box_app_file_export: AI盒子应用文件导出
    */
    virtual MeErrorCode SendServicesAiBoxAppFileExportSouthbound(const std::shared_ptr<ServicesAiBoxAppFileExportSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppFileExportNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyAiBoxAppFileExportNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppFileExportNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai_box_app_file_export_progress: AI盒子应用文件导出进度
    */
    virtual MeErrorCode SendEventsAiBoxAppFileExportProgressSouthbound(const std::shared_ptr<EventsAiBoxAppFileExportProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAiBoxAppFileExportProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyAiBoxAppFileExportProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyAiBoxAppFileExportProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai_box_app_list: AI盒子应用列表
    */
    virtual MeErrorCode SendServicesAiBoxAppListSouthbound(const std::shared_ptr<ServicesAiBoxAppListSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppListNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyAiBoxAppListNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppListNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai_box_app_list_infos: AI盒子应用列表信息
    */
    virtual MeErrorCode SendEventsAiBoxAppListInfosSouthbound(const std::shared_ptr<EventsAiBoxAppListInfosSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAiBoxAppListInfosNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyAiBoxAppListInfosNorthbound(const std::function<void(const std::shared_ptr<EventsReplyAiBoxAppListInfosNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai_box_app_operate: AI盒子应用操作
    */
    virtual MeErrorCode SendServicesAiBoxAppOperateSouthbound(const std::shared_ptr<ServicesAiBoxAppOperateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppOperateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyAiBoxAppOperateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppOperateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai_object_detection_alert: AI告警数据
    */
    virtual ListenIdCode ObserveEventsAiObjectDetectionAlertNorthbound(const std::function<void(const std::shared_ptr<EventsAiObjectDetectionAlertNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyAiObjectDetectionAlertSouthbound(const std::shared_ptr<EventsReplyAiObjectDetectionAlertSouthbound>& msg, const std::shared_ptr<EventsAiObjectDetectionAlertNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*

    */
    virtual MeErrorCode SendServicesAircraftPowerCtrlSouthbound(const std::shared_ptr<ServicesAircraftPowerCtrlSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAircraftPowerCtrlNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyAircraftPowerCtrlNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAircraftPowerCtrlNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        计算候选飞机到达目标点的时间和能耗
    */
    virtual MeErrorCode SendServicesCalculateCandidateRelayAircraftSouthbound(const std::shared_ptr<ServicesCalculateCandidateRelayAircraftSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCalculateCandidateRelayAircraftNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyCalculateCandidateRelayAircraftNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCalculateCandidateRelayAircraftNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        切换镜头变焦倍率
    */
    virtual MeErrorCode SendServicesCameraFocalLengthSetSouthbound(const std::shared_ptr<ServicesCameraFocalLengthSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCameraFocalLengthSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyCameraFocalLengthSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCameraFocalLengthSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机LookAt
    */
    virtual MeErrorCode SendServicesCameraLookAtSouthbound(const std::shared_ptr<ServicesCameraLookAtSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCameraLookAtNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyCameraLookAtNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCameraLookAtNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        candidate_aircrafts_calculate_results: 候选飞机计算结果
    */
    virtual ListenIdCode ObserveEventsCandidateAircraftsCalculateResultsNorthbound(const std::function<void(const std::shared_ptr<EventsCandidateAircraftsCalculateResultsNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyCandidateAircraftsCalculateResultsSouthbound(const std::shared_ptr<EventsReplyCandidateAircraftsCalculateResultsSouthbound>& msg, const std::shared_ptr<EventsCandidateAircraftsCalculateResultsNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        调试命令
    */
    virtual MeErrorCode SendServicesCccDebugCommandSouthbound(const std::shared_ptr<ServicesCccDebugCommandSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCccDebugCommandNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyCccDebugCommandNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCccDebugCommandNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        调试命令事件上报
    */
    virtual ListenIdCode ObserveEventsCccDebugEventNorthbound(const std::function<void(const std::shared_ptr<EventsCccDebugEventNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyCccDebugEventSouthbound(const std::shared_ptr<EventsReplyCccDebugEventSouthbound>& msg, const std::shared_ptr<EventsCccDebugEventNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        clear_sdr_topo_info: 清除写号
    */
    virtual MeErrorCode SendServicesClearSdrTopoInfoSouthbound(const std::shared_ptr<ServicesClearSdrTopoInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyClearSdrTopoInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyClearSdrTopoInfoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyClearSdrTopoInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        cleared_to_land: 云端主动请求设备执行降落
    */
    virtual MeErrorCode SendServicesClearedToLandSouthbound(const std::shared_ptr<ServicesClearedToLandSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyClearedToLandNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyClearedToLandNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyClearedToLandNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        cloud_environment_switch: 云环境切换
    */
    virtual MeErrorCode SendServicesCloudEnvironmentSwitchSouthbound(const std::shared_ptr<ServicesCloudEnvironmentSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCloudEnvironmentSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyCloudEnvironmentSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCloudEnvironmentSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        command_send: 调试V1消息
    */
    virtual MeErrorCode SendServicesCommandSendSouthbound(const std::shared_ptr<ServicesCommandSendSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCommandSendNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyCommandSendNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCommandSendNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        RTC链路延迟信息统计
    */
    virtual ListenIdCode ObserveDrcDelayInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDelayInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        返航误避障状态上报
    */
    virtual ListenIdCode ObserveEventsDeviceExitHomingNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsDeviceExitHomingNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyDeviceExitHomingNotifySouthbound(const std::shared_ptr<EventsReplyDeviceExitHomingNotifySouthbound>& msg, const std::shared_ptr<EventsDeviceExitHomingNotifyNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        device_format: 机场格式化
    */
    virtual MeErrorCode SendServicesDeviceFormatSouthbound(const std::shared_ptr<ServicesDeviceFormatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceFormatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDeviceFormatNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceFormatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        device_format: 机场格式化执行结果
    */
    virtual MeErrorCode SendEventsDeviceFormatSouthbound(const std::shared_ptr<EventsDeviceFormatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyDeviceFormatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyDeviceFormatNorthbound(const std::function<void(const std::shared_ptr<EventsReplyDeviceFormatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        开启设备端RTC链路V1转发
    */
    virtual MeErrorCode SendServicesDeviceV1ForwardStartSouthbound(const std::shared_ptr<ServicesDeviceV1ForwardStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceV1ForwardStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDeviceV1ForwardStartNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceV1ForwardStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        关闭设备端RTC链路V1转发
    */
    virtual MeErrorCode SendServicesDeviceV1ForwardStopSouthbound(const std::shared_ptr<ServicesDeviceV1ForwardStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceV1ForwardStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDeviceV1ForwardStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceV1ForwardStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        dongle_power_switch: dongle电源开关
    */
    virtual MeErrorCode SendServicesDonglePowerSwitchSouthbound(const std::shared_ptr<ServicesDonglePowerSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDonglePowerSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDonglePowerSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDonglePowerSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        dongle_power_switch: dongle电源开关切换进度
        北向 Topic: thing/product/uav/events/dongle_power_switch
        南向 Topic: thing/product/uav/events_reply/dongle_power_switch
    */
    virtual ListenIdCode ObserveEventsDonglePowerSwitchNorthbound(const std::function<void(const std::shared_ptr<EventsDonglePowerSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyDonglePowerSwitchSouthbound(const std::shared_ptr<EventsReplyDonglePowerSwitchSouthbound>& msg, const std::shared_ptr<EventsDonglePowerSwitchNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        打开 AI 主动告警: 按多路码流配置开启 AI 目标告警 (EA340)
        南向: thing/product/uav/drc/down/drc_ai_alarm_start
        北向: thing/product/uav/drc/up/drc_ai_alarm_start (回包: result)
    */
    virtual MeErrorCode SendDrcDrcAiAlarmStartSouthbound(const std::shared_ptr<DrcDrcAiAlarmStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiAlarmStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiAlarmStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiAlarmStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        AI 主动告警状态实时推送 (EA340)
        北向: thing/product/uav/drc/up/drc_ai_alarm_state_push
    */
    virtual ListenIdCode ObserveDrcDrcAiAlarmStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcAiAlarmStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        关闭 AI 主动告警 (EA340)
        南向: thing/product/uav/drc/down/drc_ai_alarm_stop
        北向: thing/product/uav/drc/up/drc_ai_alarm_stop (回包: result)
    */
    virtual MeErrorCode SendDrcDrcAiAlarmStopSouthbound(const std::shared_ptr<DrcDrcAiAlarmStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiAlarmStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiAlarmStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiAlarmStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置AI识别目标过滤列表
    */
    virtual MeErrorCode SendDrcDrcAiIdentifyFilterSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyFilterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiIdentifyFilterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置AI识别置信度模式
    */
    virtual MeErrorCode SendDrcDrcAiIdentifyScoreModeSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiIdentifyScoreModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        重置AI识别置信度
    */
    virtual MeErrorCode SendDrcDrcAiIdentifyScoreResetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiIdentifyScoreResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置AI识别置信度
    */
    virtual MeErrorCode SendDrcDrcAiIdentifyScoreSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiIdentifyScoreSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai 识别开关
    */
    virtual MeErrorCode SendDrcDrcAiIdentifySetSouthbound(const std::shared_ptr<DrcDrcAiIdentifySetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifySetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiIdentifySetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifySetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        Ai状态
        北向: thing/product/uav/drc/up/drc_ai_info_push
    */
    virtual ListenIdCode ObserveDrcDrcAiInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcAiInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai 模型选择，根据索引选择模型
    */
    virtual MeErrorCode SendDrcDrcAiModelSelectSouthbound(const std::shared_ptr<DrcDrcAiModelSelectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiModelSelectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiModelSelectNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiModelSelectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ai 模型配置, 配置码流绑定的AI模型
    */
    virtual MeErrorCode SendDrcDrcAiModelsConfigSouthbound(const std::shared_ptr<DrcDrcAiModelsConfigSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiModelsConfigNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiModelsConfigNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiModelsConfigNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        AI框选目标跟随确认
    */
    virtual MeErrorCode SendDrcDrcAiSpotlightZoomConfirmSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomConfirmSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomConfirmNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        AI框选目标跟随
    */
    virtual MeErrorCode SendDrcDrcAiSpotlightZoomSelectSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomSelectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomSelectNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        AI跟随开关设置
    */
    virtual MeErrorCode SendDrcDrcAiSpotlightZoomSetSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        停止目标跟随
    */
    virtual MeErrorCode SendDrcDrcAiSpotlightZoomStopSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        AI识别目标跟随
    */
    virtual MeErrorCode SendDrcDrcAiSpotlightZoomTrackSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomTrackSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAiSpotlightZoomTrackNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机电子除雾等级设置
    */
    virtual MeErrorCode SendDrcDrcAlgorithmDehazeLevelSetSouthbound(const std::shared_ptr<DrcDrcAlgorithmDehazeLevelSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAlgorithmDehazeLevelSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机电子除雾模式设置
    */
    virtual MeErrorCode SendDrcDrcAlgorithmDehazeModeSetSouthbound(const std::shared_ptr<DrcDrcAlgorithmDehazeModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAlgorithmDehazeModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        辅助影像模式设置
    */
    virtual MeErrorCode SendDrcDrcAssistantVideoModeSetSouthbound(const std::shared_ptr<DrcDrcAssistantVideoModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAssistantVideoModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAssistantVideoModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAssistantVideoModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        辅助影像开关设置
    */
    virtual MeErrorCode SendDrcDrcAssistantVideoSwitchSetSouthbound(const std::shared_ptr<DrcDrcAssistantVideoSwitchSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAssistantVideoSwitchSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcAssistantVideoSwitchSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAssistantVideoSwitchSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        电池信息
        北向: thing/product/uav/drc/up/drc_batteries_info_push
    */
    virtual ListenIdCode ObserveDrcDrcBatteriesInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcBatteriesInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        双击AIM
        南向: thing/product/uav/drc/down/drc_camera_aim
        北向: thing/product/uav/drc/up/drc_camera_aim
    */
    virtual MeErrorCode SendDrcDrcCameraAimSouthbound(const std::shared_ptr<DrcDrcCameraAimSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraAimNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraAimNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraAimNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机光圈值设置
    */
    virtual MeErrorCode SendDrcDrcCameraApertureValueSetSouthbound(const std::shared_ptr<DrcDrcCameraApertureValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraApertureValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraApertureValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraApertureValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机降噪等级设置
    */
    virtual MeErrorCode SendDrcDrcCameraDenoiseLevelSetSouthbound(const std::shared_ptr<DrcDrcCameraDenoiseLevelSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraDenoiseLevelSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraDenoiseLevelSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraDenoiseLevelSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机畸变校正设置
    */
    virtual MeErrorCode SendDrcDrcCameraDewarpingSetSouthbound(const std::shared_ptr<DrcDrcCameraDewarpingSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraDewarpingSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraDewarpingSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraDewarpingSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机曝光模式设置
    */
    virtual MeErrorCode SendDrcDrcCameraExposureModeSetSouthbound(const std::shared_ptr<DrcDrcCameraExposureModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraExposureModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraExposureModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraExposureModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机曝光设置
    */
    virtual MeErrorCode SendDrcDrcCameraExposureSetSouthbound(const std::shared_ptr<DrcDrcCameraExposureSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraExposureSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraExposureSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraExposureSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        切换镜头变焦倍率
    */
    virtual MeErrorCode SendDrcDrcCameraFocalLengthSetSouthbound(const std::shared_ptr<DrcDrcCameraFocalLengthSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocalLengthSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraFocalLengthSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocalLengthSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机对焦模式设置
    */
    virtual MeErrorCode SendDrcDrcCameraFocusModeSetSouthbound(const std::shared_ptr<DrcDrcCameraFocusModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocusModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraFocusModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocusModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机对焦值设置
    */
    virtual MeErrorCode SendDrcDrcCameraFocusValueSetSouthbound(const std::shared_ptr<DrcDrcCameraFocusValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocusValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraFocusValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocusValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机帧变焦
    */
    virtual MeErrorCode SendDrcDrcCameraFrameZoomSouthbound(const std::shared_ptr<DrcDrcCameraFrameZoomSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFrameZoomNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraFrameZoomNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFrameZoomNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机ISO设置
    */
    virtual MeErrorCode SendDrcDrcCameraIsoSetSouthbound(const std::shared_ptr<DrcDrcCameraIsoSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraIsoSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraIsoSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraIsoSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机LookAt
    */
    virtual MeErrorCode SendDrcDrcCameraLookAtSouthbound(const std::shared_ptr<DrcDrcCameraLookAtSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraLookAtNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraLookAtNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraLookAtNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机机械快门设置
    */
    virtual MeErrorCode SendDrcDrcCameraMechanicalShutterSetSouthbound(const std::shared_ptr<DrcDrcCameraMechanicalShutterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraMechanicalShutterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraMechanicalShutterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraMechanicalShutterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机模式切换
        南向: thing/product/uav/drc/down/drc_camera_mode_switch
        北向: thing/product/uav/drc/up/drc_camera_mode_switch
    */
    virtual MeErrorCode SendDrcDrcCameraModeSwitchSouthbound(const std::shared_ptr<DrcDrcCameraModeSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraModeSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraModeSwitchNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraModeSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        夜景模式设置
    */
    virtual MeErrorCode SendDrcDrcCameraNightModeSetSouthbound(const std::shared_ptr<DrcDrcCameraNightModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraNightModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraNightModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraNightModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        黑白夜视使能
    */
    virtual MeErrorCode SendDrcDrcCameraNightVisionEnableSouthbound(const std::shared_ptr<DrcDrcCameraNightVisionEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraNightVisionEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraNightVisionEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraNightVisionEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        drc模式下，相机信息推送，3Hz
        北向: thing/product/uav/drc/up/drc_camera_osd_info_push
    */
    virtual ListenIdCode ObserveDrcDrcCameraOsdInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraOsdInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机照片格式设置
    */
    virtual MeErrorCode SendDrcDrcCameraPhotoFormatSetSouthbound(const std::shared_ptr<DrcDrcCameraPhotoFormatSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoFormatSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraPhotoFormatSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoFormatSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机拍照状态推送
        北向: thing/product/uav/drc/up/drc_camera_photo_info_push
    */
    virtual ListenIdCode ObserveDrcDrcCameraPhotoInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraPhotoInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机照片尺寸设置
    */
    virtual MeErrorCode SendDrcDrcCameraPhotoSizeSetSouthbound(const std::shared_ptr<DrcDrcCameraPhotoSizeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraPhotoSizeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        停止拍照：停止全景拍照，停止低光智能
    */
    virtual MeErrorCode SendDrcDrcCameraPhotoStopSouthbound(const std::shared_ptr<DrcDrcCameraPhotoStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraPhotoStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        南向: thing/product/uav/drc/down/drc_camera_photo_take  数据结构: DrcDownDrcCameraPhotoTakeSouthbound   接口: ObserveDrcDownDrcCameraPhotoTakeSouthbound
        北向: thing/product/uav/drc/up/drc_camera_photo_take    数据结构: DrcUpDrcCameraPhotoTakeNorthbound     接口: SendDrcUpDrcCameraPhotoTakeNorthbound
    */
    virtual MeErrorCode SendDrcDrcCameraPhotoTakeSouthbound(const std::shared_ptr<DrcDrcCameraPhotoTakeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoTakeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraPhotoTakeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoTakeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机点对焦操作
    */
    virtual MeErrorCode SendDrcDrcCameraPointFocusActionSouthbound(const std::shared_ptr<DrcDrcCameraPointFocusActionSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPointFocusActionNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraPointFocusActionNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPointFocusActionNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机开始录像
        南向: thing/product/uav/drc/down/drc_camera_recording_start
        北向: thing/product/uav/drc/up/drc_camera_recording_start
    */
    virtual MeErrorCode SendDrcDrcCameraRecordingStartSouthbound(const std::shared_ptr<DrcDrcCameraRecordingStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraRecordingStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraRecordingStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraRecordingStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机停止录像
        南向: thing/product/uav/drc/down/drc_camera_recording_stop
        北向: thing/product/uav/drc/up/drc_camera_recording_stop
    */
    virtual MeErrorCode SendDrcDrcCameraRecordingStopSouthbound(const std::shared_ptr<DrcDrcCameraRecordingStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraRecordingStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraRecordingStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraRecordingStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        长按画面拖动（控制云台转动速度）
    */
    virtual MeErrorCode SendDrcDrcCameraScreenDragSouthbound(const std::shared_ptr<DrcDrcCameraScreenDragSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraScreenDragNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraScreenDragNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraScreenDragNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        分屏
        南向: thing/product/uav/drc/down/drc_camera_screen_split
        北向: thing/product/uav/drc/up/drc_camera_screen_split
    */
    virtual MeErrorCode SendDrcDrcCameraScreenSplitSouthbound(const std::shared_ptr<DrcDrcCameraScreenSplitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraScreenSplitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraScreenSplitNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraScreenSplitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        重置相机参数设置
        不可重置参数情况: 
        1、相机正在视频录制
        2、相机正在定时拍照
    */
    virtual MeErrorCode SendDrcDrcCameraSettingsResetSouthbound(const std::shared_ptr<DrcDrcCameraSettingsResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraSettingsResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraSettingsResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraSettingsResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机快门设置
    */
    virtual MeErrorCode SendDrcDrcCameraShutterSetSouthbound(const std::shared_ptr<DrcDrcCameraShutterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraShutterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraShutterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraShutterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机状态推送
        北向: thing/product/uav/drc/up/drc_camera_state_push
    */
    virtual ListenIdCode ObserveDrcDrcCameraStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置水印
    */
    virtual MeErrorCode SendDrcDrcCameraWatermarkSetSouthbound(const std::shared_ptr<DrcDrcCameraWatermarkSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraWatermarkSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCameraWatermarkSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraWatermarkSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        镜头去雾
    */
    virtual MeErrorCode SendDrcDrcCleanFogSouthbound(const std::shared_ptr<DrcDrcCleanFogSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCleanFogNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCleanFogNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCleanFogNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        倒计时动作设置
    */
    virtual MeErrorCode SendDrcDrcCountDownActionSetSouthbound(const std::shared_ptr<DrcDrcCountDownActionSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCountDownActionSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcCountDownActionSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCountDownActionSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        倒计时信息推送
        北向: thing/product/uav/drc/up/drc_count_down_info_push
    */
    virtual ListenIdCode ObserveDrcDrcCountDownInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCountDownInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备推送drc心跳，让前端保持drc连接
    */
    virtual ListenIdCode ObserveDrcDrcDeviceHeartBeatNorthbound(const std::function<void(const std::shared_ptr<DrcDrcDeviceHeartBeatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        飞机悬停时间设置
    */
    virtual MeErrorCode SendDrcDrcDroneHoverSetSouthbound(const std::shared_ptr<DrcDrcDroneHoverSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcDroneHoverSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcDroneHoverSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcDroneHoverSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        飞行器状态推送
        北向: thing/product/uav/drc/up/drc_drone_state_push
    */
    virtual ListenIdCode ObserveDrcDrcDroneStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcDroneStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        降落：紧急降落
    */
    virtual MeErrorCode SendDrcDrcEmergencyLandingSouthbound(const std::shared_ptr<DrcDrcEmergencyLandingSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcEmergencyLandingNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcEmergencyLandingNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcEmergencyLandingNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        爆闪灯设置
    */
    virtual MeErrorCode SendDrcDrcFlashLedSetSouthbound(const std::shared_ptr<DrcDrcFlashLedSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcFlashLedSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcFlashLedSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcFlashLedSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        指哪打哪参数设置
    */
    virtual MeErrorCode SendDrcDrcFollowMouseParameterSetSouthbound(const std::shared_ptr<DrcDrcFollowMouseParameterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcFollowMouseParameterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcFollowMouseParameterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcFollowMouseParameterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        降落：强制降落
    */
    virtual MeErrorCode SendDrcDrcForceLandingSouthbound(const std::shared_ptr<DrcDrcForceLandingSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcForceLandingNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcForceLandingNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcForceLandingNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云台快捷操作（回中、朝下等）
    */
    virtual MeErrorCode SendDrcDrcGimbalResetSouthbound(const std::shared_ptr<DrcDrcGimbalResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcGimbalResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcGimbalResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcGimbalResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        近红外补光使能
    */
    virtual MeErrorCode SendDrcDrcInfraredFillLightEnableSouthbound(const std::shared_ptr<DrcDrcInfraredFillLightEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcInfraredFillLightEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcInfraredFillLightEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcInfraredFillLightEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        订阅初始状态
    */
    virtual MeErrorCode SendDrcDrcInitialStateSubscribeSouthbound(const std::shared_ptr<DrcDrcInitialStateSubscribeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcInitialStateSubscribeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcInitialStateSubscribeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcInitialStateSubscribeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        定时拍照设置
    */
    virtual MeErrorCode SendDrcDrcIntervalPhotoSetSouthbound(const std::shared_ptr<DrcDrcIntervalPhotoSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIntervalPhotoSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcIntervalPhotoSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIntervalPhotoSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        红外测温区域设置
        南向: thing/product/uav/drc/down/drc_ir_metering_area_set
        北向: thing/product/uav/drc/up/drc_ir_metering_area_set
    */
    virtual MeErrorCode SendDrcDrcIrMeteringAreaSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringAreaSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringAreaSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcIrMeteringAreaSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringAreaSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        红外测温模式设置
        南向: thing/product/uav/drc/down/drc_ir_metering_mode_set
        北向: thing/product/uav/drc/up/drc_ir_metering_mode_set
    */
    virtual MeErrorCode SendDrcDrcIrMeteringModeSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcIrMeteringModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        红外测温点设置
        南向: thing/product/uav/drc/down/drc_ir_metering_point_set
        北向: thing/product/uav/drc/up/drc_ir_metering_point_set
    */
    virtual MeErrorCode SendDrcDrcIrMeteringPointSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringPointSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringPointSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcIrMeteringPointSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringPointSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯 亮度设置
    */
    virtual MeErrorCode SendDrcDrcLightBrightnessSetSouthbound(const std::shared_ptr<DrcDrcLightBrightnessSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightBrightnessSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightBrightnessSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightBrightnessSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯 云台校准
    */
    virtual MeErrorCode SendDrcDrcLightCalibrationSouthbound(const std::shared_ptr<DrcDrcLightCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightCalibrationNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯 云台微调
    */
    virtual MeErrorCode SendDrcDrcLightFineTuningSetSouthbound(const std::shared_ptr<DrcDrcLightFineTuningSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightFineTuningSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightFineTuningSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightFineTuningSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯 云台联动
    */
    virtual MeErrorCode SendDrcDrcLightLightGimbalControlSetSouthbound(const std::shared_ptr<DrcDrcLightLightGimbalControlSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightLightGimbalControlSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯 模式设置
    */
    virtual MeErrorCode SendDrcDrcLightModeSetSouthbound(const std::shared_ptr<DrcDrcLightModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯照明类型设置
    */
    virtual MeErrorCode SendDrcDrcLightTypeSetSouthbound(const std::shared_ptr<DrcDrcLightTypeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightTypeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightTypeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightTypeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 探照灯 广视野模式
    */
    virtual MeErrorCode SendDrcDrcLightWideFieldModeSetSouthbound(const std::shared_ptr<DrcDrcLightWideFieldModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLightWideFieldModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        联动变焦
        南向: thing/product/uav/drc/down/drc_linkage_zoom_set
        北向: thing/product/uav/drc/up/drc_linkage_zoom_set
    */
    virtual MeErrorCode SendDrcDrcLinkageZoomSetSouthbound(const std::shared_ptr<DrcDrcLinkageZoomSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLinkageZoomSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLinkageZoomSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLinkageZoomSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机镜头切换
    */
    virtual MeErrorCode SendDrcDrcLiveLensChangeSouthbound(const std::shared_ptr<DrcDrcLiveLensChangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLiveLensChangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcLiveLensChangeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLiveLensChangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机状态推送
        北向: thing/product/uav/drc/up/drc_live_video_info_push
    */
    virtual ListenIdCode ObserveDrcDrcLiveVideoInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcLiveVideoInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        进入drc模式
    */
    virtual MeErrorCode SendServicesDrcModeEnterSouthbound(const std::shared_ptr<ServicesDrcModeEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcModeEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDrcModeEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcModeEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        退出drc模式
    */
    virtual MeErrorCode SendServicesDrcModeExitSouthbound(const std::shared_ptr<ServicesDrcModeExitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcModeExitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDrcModeExitNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcModeExitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        夜航灯设置
    */
    virtual MeErrorCode SendDrcDrcNightLightsStateSetSouthbound(const std::shared_ptr<DrcDrcNightLightsStateSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcNightLightsStateSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcNightLightsStateSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcNightLightsStateSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        打开降落伞
    */
    virtual MeErrorCode SendDrcDrcOpenParachuteSouthbound(const std::shared_ptr<DrcDrcOpenParachuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcOpenParachuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcOpenParachuteNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcOpenParachuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        降落伞信息推送
    */
    virtual ListenIdCode ObserveDrcDrcParachuteInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcParachuteInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        照片存储设置
    */
    virtual MeErrorCode SendDrcDrcPhotoStorageSetSouthbound(const std::shared_ptr<DrcDrcPhotoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPhotoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcPhotoStorageSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPhotoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        预录制状态设置
    */
    virtual MeErrorCode SendDrcDrcPreRecordedStatusSetSouthbound(const std::shared_ptr<DrcDrcPreRecordedStatusSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPreRecordedStatusSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcPreRecordedStatusSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPreRecordedStatusSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        预录制时间设置
    */
    virtual MeErrorCode SendDrcDrcPreRecordedTimeSetSouthbound(const std::shared_ptr<DrcDrcPreRecordedTimeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPreRecordedTimeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcPreRecordedTimeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPreRecordedTimeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 浮窗文本推送
    */
    virtual ListenIdCode ObserveDrcDrcPsdkFloatingWindowTextNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkFloatingWindowTextNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 三方 PSDK 发送文本框内容
    */
    virtual MeErrorCode SendDrcDrcPsdkInputBoxTextSetSouthbound(const std::shared_ptr<DrcDrcPsdkInputBoxTextSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcPsdkInputBoxTextSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK状态上报
    */
    virtual ListenIdCode ObserveDrcDrcPsdkStateInfoNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkStateInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK状态上报，UI资源
    */
    virtual ListenIdCode ObserveDrcDrcPsdkUiResourceNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkUiResourceNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 设置三方 PSDK 控件值
    */
    virtual MeErrorCode SendDrcDrcPsdkWidgetValueSetSouthbound(const std::shared_ptr<DrcDrcPsdkWidgetValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcPsdkWidgetValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        测试rtc网络质量, 回复仅在is_need_ack为true时发送
    */
    virtual MeErrorCode SendDrcDrcRtcQualityTestSouthbound(const std::shared_ptr<DrcDrcRtcQualityTestSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcRtcQualityTestNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcRtcQualityTestNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcRtcQualityTestNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置普通档最大水平速度
    */
    virtual MeErrorCode SendDrcDrcSetFlyingMaxSpeedSouthbound(const std::shared_ptr<DrcDrcSetFlyingMaxSpeedSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetFlyingMaxSpeedNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetFlyingMaxSpeedNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetFlyingMaxSpeedNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置红外显示模式 - 纯红外画面/红外可见光画中画模式/红外可见光融合画面
    */
    virtual MeErrorCode SendDrcDrcSetIrDisplayModeSouthbound(const std::shared_ptr<DrcDrcSetIrDisplayModeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrDisplayModeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIrDisplayModeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrDisplayModeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置红外liveview增强模式-关闭/边缘增强/热源回帖
    */
    virtual MeErrorCode SendDrcDrcSetIrEnhanceModeSouthbound(const std::shared_ptr<DrcDrcSetIrEnhanceModeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrEnhanceModeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIrEnhanceModeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrEnhanceModeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置红外融合模式比例
    */
    virtual MeErrorCode SendDrcDrcSetIrMixRateSouthbound(const std::shared_ptr<DrcDrcSetIrMixRateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrMixRateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIrMixRateNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrMixRateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置温域聚焦功能画面着色效果类型
    */
    virtual MeErrorCode SendDrcDrcSetIsothermColorRenderingTypeSouthbound(const std::shared_ptr<DrcDrcSetIsothermColorRenderingTypeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIsothermColorRenderingTypeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置温域聚焦开关（等温线开关）
    */
    virtual MeErrorCode SendDrcDrcSetIsothermEnableSouthbound(const std::shared_ptr<DrcDrcSetIsothermEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIsothermEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        手动设置温域聚焦上下限温度
    */
    virtual MeErrorCode SendDrcDrcSetIsothermLowerUpperRangeSouthbound(const std::shared_ptr<DrcDrcSetIsothermLowerUpperRangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIsothermLowerUpperRangeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置温域聚焦多边形区域测温的区域顶点坐标
    */
    virtual MeErrorCode SendDrcDrcSetIsothermRenderingPolygonSouthbound(const std::shared_ptr<DrcDrcSetIsothermRenderingPolygonSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIsothermRenderingPolygonNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置按框选区域设置温域聚焦使能
    */
    virtual MeErrorCode SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound(const std::shared_ptr<DrcDrcSetIsothermRoiBasedRenderingEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 开始文件播放
    */
    virtual MeErrorCode SendDrcDrcSpeakerAudioPlayStartSouthbound(const std::shared_ptr<DrcDrcSpeakerAudioPlayStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerAudioPlayStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 模式设置
    */
    virtual MeErrorCode SendDrcDrcSpeakerPlayModeSetSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerPlayModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 音频播放进度上报
    */
    virtual ListenIdCode ObserveDrcDrcSpeakerPlayProgressNorthbound(const std::function<void(const std::shared_ptr<DrcDrcSpeakerPlayProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 停止播放
    */
    virtual MeErrorCode SendDrcDrcSpeakerPlayStopSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerPlayStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 音量设置
    */
    virtual MeErrorCode SendDrcDrcSpeakerPlayVolumeSetSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayVolumeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerPlayVolumeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 实时数据传输
    */
    virtual MeErrorCode SendDrcDrcSpeakerRealTimeDataTransmitSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeDataTransmitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerRealTimeDataTransmitNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 实时播放开始
    */
    virtual MeErrorCode SendDrcDrcSpeakerRealTimeStartSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerRealTimeStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 实时播放停止
    */
    virtual MeErrorCode SendDrcDrcSpeakerRealTimeStopSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerRealTimeStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 重播
    */
    virtual MeErrorCode SendDrcDrcSpeakerReplaySouthbound(const std::shared_ptr<DrcDrcSpeakerReplaySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerReplayNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerReplayNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerReplayNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 TTS播放开始
    */
    virtual MeErrorCode SendDrcDrcSpeakerTtsPlayStartSouthbound(const std::shared_ptr<DrcDrcSpeakerTtsPlayStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerTtsPlayStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 TTS设置
    */
    virtual MeErrorCode SendDrcDrcSpeakerTtsSetSouthbound(const std::shared_ptr<DrcDrcSpeakerTtsSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerTtsSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcSpeakerTtsSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerTtsSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        隐蔽模式设置
    */
    virtual MeErrorCode SendDrcDrcStealthStateSetSouthbound(const std::shared_ptr<DrcDrcStealthStateSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcStealthStateSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcStealthStateSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcStealthStateSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        调色盘
        南向: thing/product/uav/drc/down/drc_thermal_current_palette_set
        北向: thing/product/uav/drc/up/drc_thermal_current_palette_set
    */
    virtual MeErrorCode SendDrcDrcThermalCurrentPaletteSetSouthbound(const std::shared_ptr<DrcDrcThermalCurrentPaletteSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalCurrentPaletteSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcThermalCurrentPaletteSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalCurrentPaletteSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        红外增益模式
        南向: thing/product/uav/drc/down/drc_thermal_gain_mode_set
        北向: thing/product/uav/drc/up/drc_thermal_gain_mode_set
    */
    virtual MeErrorCode SendDrcDrcThermalGainModeSetSouthbound(const std::shared_ptr<DrcDrcThermalGainModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalGainModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcThermalGainModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalGainModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        红外超分辨率设置
    */
    virtual MeErrorCode SendDrcDrcThermalSuperResolutionEnableSouthbound(const std::shared_ptr<DrcDrcThermalSuperResolutionEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalSuperResolutionEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcThermalSuperResolutionEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalSuperResolutionEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        视频尺寸设置
    */
    virtual MeErrorCode SendDrcDrcVideoResolutionSetSouthbound(const std::shared_ptr<DrcDrcVideoResolutionSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcVideoResolutionSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcVideoResolutionSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcVideoResolutionSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        视频存储设置
    */
    virtual MeErrorCode SendDrcDrcVideoStorageSetSouthbound(const std::shared_ptr<DrcDrcVideoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcVideoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDrcVideoStorageSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcVideoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        PSDK 喊话器 实时喊话房间加入
    */
    virtual MeErrorCode SendServicesDrcVoiceEnterSouthbound(const std::shared_ptr<ServicesDrcVoiceEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcVoiceEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyDrcVoiceEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcVoiceEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        飞行器急停
    */
    virtual MeErrorCode SendDrcDroneEmergencyStopSouthbound(const std::shared_ptr<DrcDroneEmergencyStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDroneEmergencyStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyDroneEmergencyStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDroneEmergencyStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_ready: 任务准备结果
    */
    virtual MeErrorCode SendEventsEmergencyStopNotifySouthbound(const std::shared_ptr<EventsEmergencyStopNotifySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyEmergencyStopNotifyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyEmergencyStopNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyEmergencyStopNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        esim_operator_switch: eSim运营商切换进度
        北向 Topic: thing/product/uav/events/esim_operator_switch
        南向 Topic: thing/product/uav/events_reply/esim_operator_switch
    */
    virtual ListenIdCode ObserveEventsEsimOperatorSwitchNorthbound(const std::function<void(const std::shared_ptr<EventsEsimOperatorSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyEsimOperatorSwitchSouthbound(const std::shared_ptr<EventsReplyEsimOperatorSwitchSouthbound>& msg, const std::shared_ptr<EventsEsimOperatorSwitchNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        esim_operator_switch: eSim运营商切换
    */
    virtual MeErrorCode SendServicesEsimOperatorSwitchSouthbound(const std::shared_ptr<ServicesEsimOperatorSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyEsimOperatorSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyEsimOperatorSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyEsimOperatorSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        fileupload_list: 远程日志列表
    */
    virtual MeErrorCode SendServicesFileuploadListSouthbound(const std::shared_ptr<ServicesFileuploadListSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadListNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFileuploadListNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadListNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        fileupload_progress: 日志上传进度
    */
    virtual MeErrorCode SendEventsFileuploadProgressSouthbound(const std::shared_ptr<EventsFileuploadProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFileuploadProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyFileuploadProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFileuploadProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        fileupload_start: 远程日志上传
    */
    virtual MeErrorCode SendServicesFileuploadStartSouthbound(const std::shared_ptr<ServicesFileuploadStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFileuploadStartNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        fileupload_update: 远程日志更新
    */
    virtual MeErrorCode SendServicesFileuploadUpdateSouthbound(const std::shared_ptr<ServicesFileuploadUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFileuploadUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        自定义飞行区告警信息推送
    */
    virtual ListenIdCode ObserveEventsFlightAreasDroneLocationNorthbound(const std::function<void(const std::shared_ptr<EventsFlightAreasDroneLocationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyFlightAreasDroneLocationSouthbound(const std::shared_ptr<EventsReplyFlightAreasDroneLocationSouthbound>& msg, const std::shared_ptr<EventsFlightAreasDroneLocationNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备端请求获取flight_areas_get资源
        北向: thing/product/uav/requests/flight_areas_get
        南向: thing/product/uav/requests_reply/flight_areas_get
    */
    virtual ListenIdCode ObserveRequestsFlightAreasGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlightAreasGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyFlightAreasGetSouthbound(const std::shared_ptr<RequestsReplyFlightAreasGetSouthbound>& msg, const std::shared_ptr<RequestsFlightAreasGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flight_areas_sync_progress: 自定义飞行区同步进度
    */
    virtual MeErrorCode SendEventsFlightAreasSyncProgressSouthbound(const std::shared_ptr<EventsFlightAreasSyncProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlightAreasSyncProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyFlightAreasSyncProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlightAreasSyncProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flight_areas_update: 更新自定义飞行区
    */
    virtual MeErrorCode SendServicesFlightAreasUpdateSouthbound(const std::shared_ptr<ServicesFlightAreasUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlightAreasUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlightAreasUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlightAreasUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云端抢夺设备飞行控制权
    */
    virtual MeErrorCode SendServicesFlightAuthorityGrabSouthbound(const std::shared_ptr<ServicesFlightAuthorityGrabSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlightAuthorityGrabNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlightAuthorityGrabNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlightAuthorityGrabNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_execute: 执行计划航线
    */
    virtual MeErrorCode SendServicesFlighttaskExecuteSouthbound(const std::shared_ptr<ServicesFlighttaskExecuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskExecuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlighttaskExecuteNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskExecuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        暂停航线
    */
    virtual MeErrorCode SendServicesFlighttaskPauseSouthbound(const std::shared_ptr<ServicesFlighttaskPauseSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskPauseNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlighttaskPauseNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskPauseNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_prepare: 准备计划航线
    */
    virtual MeErrorCode SendServicesFlighttaskPrepareSouthbound(const std::shared_ptr<ServicesFlighttaskPrepareSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskPrepareNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlighttaskPrepareNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskPrepareNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_progress: 执行计划航线进度
    */
    virtual ListenIdCode ObserveEventsFlighttaskProgressNorthbound(const std::function<void(const std::shared_ptr<EventsFlighttaskProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyFlighttaskProgressSouthbound(const std::shared_ptr<EventsReplyFlighttaskProgressSouthbound>& msg, const std::shared_ptr<EventsFlighttaskProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备端请求获取目标设备的任务进度
    */
    virtual ListenIdCode ObserveRequestsFlighttaskProgressGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlighttaskProgressGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyFlighttaskProgressGetSouthbound(const std::shared_ptr<RequestsReplyFlighttaskProgressGetSouthbound>& msg, const std::shared_ptr<RequestsFlighttaskProgressGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_ready: 任务准备结果
    */
    virtual MeErrorCode SendEventsFlighttaskReadySouthbound(const std::shared_ptr<EventsFlighttaskReadySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlighttaskReadyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyFlighttaskReadyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlighttaskReadyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        恢复航线
    */
    virtual MeErrorCode SendServicesFlighttaskRecoverySouthbound(const std::shared_ptr<ServicesFlighttaskRecoverySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskRecoveryNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlighttaskRecoveryNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskRecoveryNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备端请求获取flighttask资源
        北向: thing/product/uav/requests/flighttask_resource_get
        南向: thing/product/uav/requests_reply/flighttask_resource_get
    */
    virtual ListenIdCode ObserveRequestsFlighttaskResourceGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlighttaskResourceGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyFlighttaskResourceGetSouthbound(const std::shared_ptr<RequestsReplyFlighttaskResourceGetSouthbound>& msg, const std::shared_ptr<RequestsFlighttaskResourceGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_stop: 多机场任务，A机场结束任务后，取消其余机场任务
    */
    virtual MeErrorCode SendServicesFlighttaskStopSouthbound(const std::shared_ptr<ServicesFlighttaskStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlighttaskStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flighttask_undo: 取消未执行任务
    */
    virtual MeErrorCode SendServicesFlighttaskUndoSouthbound(const std::shared_ptr<ServicesFlighttaskUndoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskUndoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlighttaskUndoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskUndoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        FlyTo: 飞向目标点
    */
    virtual MeErrorCode SendServicesFlyToPointSouthbound(const std::shared_ptr<ServicesFlyToPointSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlyToPointNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        FlyTo: flyto 执行结果事件通知
        北向 Topic: thing/product/uav/events/fly_to_point_progress
        南向 Topic: thing/product/uav/events_reply/fly_to_point_progress
    */
    virtual ListenIdCode ObserveEventsFlyToPointProgressNorthbound(const std::function<void(const std::shared_ptr<EventsFlyToPointProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyFlyToPointProgressSouthbound(const std::shared_ptr<EventsReplyFlyToPointProgressSouthbound>& msg, const std::shared_ptr<EventsFlyToPointProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        FlyTo: 结束 flyto 飞向目标点任务
    */
    virtual MeErrorCode SendServicesFlyToPointStopSouthbound(const std::shared_ptr<ServicesFlyToPointStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlyToPointStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        FlyTo: 更新 flyto 目标点
    */
    virtual MeErrorCode SendServicesFlyToPointUpdateSouthbound(const std::shared_ptr<ServicesFlyToPointUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlyToPointUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flysafe_database_upgrade: 限飞数据库升级进度
    */
    virtual MeErrorCode SendEventsFlysafeDatabaseUpgradeSouthbound(const std::shared_ptr<EventsFlysafeDatabaseUpgradeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyFlysafeDatabaseUpgradeNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        flysafe_database_upgrade: 限飞数据库升级
    */
    virtual MeErrorCode SendServicesFlysafeDatabaseUpgradeSouthbound(const std::shared_ptr<ServicesFlysafeDatabaseUpgradeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyFlysafeDatabaseUpgradeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云端推送drc心跳, 保持drc连接
    */
    virtual MeErrorCode SendDrcHeartBeatSouthbound(const std::shared_ptr<DrcHeartBeatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyHeartBeatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveDrcReplyHeartBeatNorthbound(const std::function<void(const std::shared_ptr<DrcReplyHeartBeatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        飞机HMS上报
    */
    virtual ListenIdCode ObserveEventsHmsNorthbound(const std::function<void(const std::shared_ptr<EventsHmsNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        飞行器HSI信息推送
        北向: thing/product/uav/drc/up/hsi_info_push
    */
    virtual ListenIdCode ObserveDrcHsiInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcHsiInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        北向: thing/product/uav/requests/in_flight_task_request
        南向: thing/product/uav/requests_reply/in_flight_task_request
    */
    virtual ListenIdCode ObserveRequestsInFlightTaskRequestNorthbound(const std::function<void(const std::shared_ptr<RequestsInFlightTaskRequestNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyInFlightTaskRequestSouthbound(const std::shared_ptr<RequestsReplyInFlightTaskRequestSouthbound>& msg, const std::shared_ptr<RequestsInFlightTaskRequestNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        in_flight_wayline_cancel: 空中航线取消
    */
    virtual MeErrorCode SendServicesInFlightWaylineCancelSouthbound(const std::shared_ptr<ServicesInFlightWaylineCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyInFlightWaylineCancelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        in_flight_wayline_deliver: 空中下发航线
    */
    virtual MeErrorCode SendServicesInFlightWaylineDeliverSouthbound(const std::shared_ptr<ServicesInFlightWaylineDeliverSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineDeliverNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyInFlightWaylineDeliverNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineDeliverNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        in_flight_wayline_progress: 空中航线进度
    */
    virtual ListenIdCode ObserveEventsInFlightWaylineProgressNorthbound(const std::function<void(const std::shared_ptr<EventsInFlightWaylineProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        in_flight_wayline_recover: 空中航线恢复
    */
    virtual MeErrorCode SendServicesInFlightWaylineRecoverSouthbound(const std::shared_ptr<ServicesInFlightWaylineRecoverSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineRecoverNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyInFlightWaylineRecoverNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineRecoverNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        in_flight_wayline_stop: 空中航线停止
    */
    virtual MeErrorCode SendServicesInFlightWaylineStopSouthbound(const std::shared_ptr<ServicesInFlightWaylineStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyInFlightWaylineStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        相机镜头切换
    */
    virtual MeErrorCode SendServicesLiveLensChangeSouthbound(const std::shared_ptr<ServicesLiveLensChangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveLensChangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyLiveLensChangeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveLensChangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设置直播清晰度
    */
    virtual MeErrorCode SendServicesLiveSetQualitySouthbound(const std::shared_ptr<ServicesLiveSetQualitySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveSetQualityNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyLiveSetQualityNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveSetQualityNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云端请求开启直播
        南向: thing/product/uav/services/live_start_push        数据结构: ServicesLiveStartPushSouthbound       接口: ObserveServicesLiveStartPushSouthbound
        北向: thing/product/uav/services_reply/live_start_push  数据结构: ServicesReplyLiveStartPushNorthbound  接口: SendServicesReplyLiveStartPushNorthbound
    */
    virtual MeErrorCode SendServicesLiveStartPushSouthbound(const std::shared_ptr<ServicesLiveStartPushSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveStartPushNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyLiveStartPushNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveStartPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云端请求停止直播
    */
    virtual MeErrorCode SendServicesLiveStopPushSouthbound(const std::shared_ptr<ServicesLiveStopPushSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveStopPushNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyLiveStopPushNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveStopPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        load_ai_box_app: 加载AI盒子应用
    */
    virtual MeErrorCode SendServicesLoadAiBoxAppSouthbound(const std::shared_ptr<ServicesLoadAiBoxAppSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLoadAiBoxAppNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyLoadAiBoxAppNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLoadAiBoxAppNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        load_ai_box_app_progress: 加载AI盒子应用进度
    */
    virtual MeErrorCode SendEventsLoadAiBoxAppProgressSouthbound(const std::shared_ptr<EventsLoadAiBoxAppProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyLoadAiBoxAppProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyLoadAiBoxAppProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyLoadAiBoxAppProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        load_ai_model: 加载AI模型
    */
    virtual MeErrorCode SendServicesLoadAiModelSouthbound(const std::shared_ptr<ServicesLoadAiModelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLoadAiModelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyLoadAiModelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLoadAiModelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        load_ai_model_progress: 加载AI模型进度
    */
    virtual MeErrorCode SendEventsLoadAiModelProgressSouthbound(const std::shared_ptr<EventsLoadAiModelProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyLoadAiModelProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyLoadAiModelProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyLoadAiModelProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        measure_sdr_signal_enter: 进入测量
    */
    virtual MeErrorCode SendServicesMeasureSdrSignalEnterSouthbound(const std::shared_ptr<ServicesMeasureSdrSignalEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyMeasureSdrSignalEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyMeasureSdrSignalEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyMeasureSdrSignalEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        obstacle_avoidance_notify: 避障结果通知
    */
    virtual MeErrorCode SendEventsObstacleAvoidanceNotifySouthbound(const std::shared_ptr<EventsObstacleAvoidanceNotifySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyObstacleAvoidanceNotifyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyObstacleAvoidanceNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyObstacleAvoidanceNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备端请求获取offline_map_get资源
        北向: thing/product/uav/requests/offline_map_get
        南向: thing/product/uav/requests_reply/offline_map_get
    */
    virtual ListenIdCode ObserveRequestsOfflineMapGetNorthbound(const std::function<void(const std::shared_ptr<RequestsOfflineMapGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyOfflineMapGetSouthbound(const std::shared_ptr<RequestsReplyOfflineMapGetSouthbound>& msg, const std::shared_ptr<RequestsOfflineMapGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备端请求获取offline_map_get_v2资源
        北向: thing/product/uav/requests/offline_map_get_v2
        南向: thing/product/uav/requests_reply/offline_map_get_v2
    */
    virtual ListenIdCode ObserveRequestsOfflineMapGetV2Northbound(const std::function<void(const std::shared_ptr<RequestsOfflineMapGetV2Northbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyOfflineMapGetV2Southbound(const std::shared_ptr<RequestsReplyOfflineMapGetV2Southbound>& msg, const std::shared_ptr<RequestsOfflineMapGetV2Northbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        offline_map_sync_progress: 离线地图同步进度
    */
    virtual MeErrorCode SendEventsOfflineMapSyncProgressSouthbound(const std::shared_ptr<EventsOfflineMapSyncProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyOfflineMapSyncProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyOfflineMapSyncProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyOfflineMapSyncProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        offline_map_update: 更新自定义飞行区
    */
    virtual MeErrorCode SendServicesOfflineMapUpdateSouthbound(const std::shared_ptr<ServicesOfflineMapUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyOfflineMapUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyOfflineMapUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyOfflineMapUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        打开降落伞
    */
    virtual MeErrorCode SendServicesOpenParachuteSouthbound(const std::shared_ptr<ServicesOpenParachuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyOpenParachuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyOpenParachuteNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyOpenParachuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        飞行器OSD信息
        北向: thing/product/uav/drc/up/osd_info_push
    */
    virtual ListenIdCode ObserveDrcOsdInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcOsdInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云端抢夺设备负载控制权
    */
    virtual MeErrorCode SendServicesPayloadAuthorityGrabSouthbound(const std::shared_ptr<ServicesPayloadAuthorityGrabSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyPayloadAuthorityGrabNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyPayloadAuthorityGrabNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyPayloadAuthorityGrabNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        照片存储设置
    */
    virtual MeErrorCode SendServicesPhotoStorageSetSouthbound(const std::shared_ptr<ServicesPhotoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyPhotoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyPhotoStorageSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyPhotoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        POI 会话状态与运行参数上报
        北向 Topic: thing/product/uav/events/poi_status_notify
        南向 Topic: thing/product/uav/events_reply/poi_status_notify
    */
    virtual ListenIdCode ObserveEventsPoiStatusNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsPoiStatusNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        relay_connect: 中继连接
    */
    virtual MeErrorCode SendServicesRelayConnectSouthbound(const std::shared_ptr<ServicesRelayConnectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayConnectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRelayConnectNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayConnectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        relay_disconnect: 中继断开
    */
    virtual MeErrorCode SendServicesRelayDisconnectSouthbound(const std::shared_ptr<ServicesRelayDisconnectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayDisconnectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRelayDisconnectNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayDisconnectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        relay_non_pair_attach_result: 中继非配对连接结果
    */
    virtual MeErrorCode SendEventsRelayNonPairAttachResultSouthbound(const std::shared_ptr<EventsRelayNonPairAttachResultSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRelayNonPairAttachResultNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyRelayNonPairAttachResultNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRelayNonPairAttachResultNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        relay_passwd_hash: 中继密码哈希
    */
    virtual MeErrorCode SendServicesRelayPasswdHashSouthbound(const std::shared_ptr<ServicesRelayPasswdHashSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayPasswdHashNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRelayPasswdHashNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayPasswdHashNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        relay_scan: 中继扫描
    */
    virtual MeErrorCode SendServicesRelayScanSouthbound(const std::shared_ptr<ServicesRelayScanSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayScanNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRelayScanNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayScanNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        relay_scan_result: 中继扫描结果
    */
    virtual MeErrorCode SendEventsRelayScanResultSouthbound(const std::shared_ptr<EventsRelayScanResultSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRelayScanResultNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyRelayScanResultNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRelayScanResultNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        release_terminal_control_area: 释放终端控制区域
    */
    virtual MeErrorCode SendEventsReleaseTerminalControlAreaSouthbound(const std::shared_ptr<EventsReleaseTerminalControlAreaSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyReleaseTerminalControlAreaNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyReleaseTerminalControlAreaNorthbound(const std::function<void(const std::shared_ptr<EventsReplyReleaseTerminalControlAreaNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        request_landing_permission: 请求降落许可
    */
    virtual ListenIdCode ObserveRequestsRequestLandingPermissionNorthbound(const std::function<void(const std::shared_ptr<RequestsRequestLandingPermissionNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyRequestLandingPermissionSouthbound(const std::shared_ptr<RequestsReplyRequestLandingPermissionSouthbound>& msg, const std::shared_ptr<RequestsRequestLandingPermissionNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        requests_ai_model_url: 请求AI模型URL
    */
    virtual ListenIdCode ObserveRequestsRequestsAiModelUrlNorthbound(const std::function<void(const std::shared_ptr<RequestsRequestsAiModelUrlNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyRequestsAiModelUrlSouthbound(const std::shared_ptr<RequestsReplyRequestsAiModelUrlSouthbound>& msg, const std::shared_ptr<RequestsRequestsAiModelUrlNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ReturnHome: ReturnHome 执行结果事件通知
    */
    virtual MeErrorCode SendServicesReturnHomeSouthbound(const std::shared_ptr<ServicesReturnHomeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnHomeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyReturnHomeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnHomeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        取消ReturnHome: 取消ReturnHome 执行结果事件通知
    */
    virtual MeErrorCode SendServicesReturnHomeCancelSouthbound(const std::shared_ptr<ServicesReturnHomeCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnHomeCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyReturnHomeCancelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnHomeCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        ReturnHome: ReturnHome 状态上报
    */
    virtual ListenIdCode ObserveEventsReturnHomeInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReturnHomeInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyReturnHomeInfoSouthbound(const std::shared_ptr<EventsReplyReturnHomeInfoSouthbound>& msg, const std::shared_ptr<EventsReturnHomeInfoNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        return_specific_home: 返航到指定home点
    */
    virtual MeErrorCode SendServicesReturnSpecificHomeSouthbound(const std::shared_ptr<ServicesReturnSpecificHomeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnSpecificHomeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyReturnSpecificHomeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnSpecificHomeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        rid_avoidance_enable: 启用RID避障
    */
    virtual MeErrorCode SendServicesRidAvoidanceEnableSouthbound(const std::shared_ptr<ServicesRidAvoidanceEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRidAvoidanceEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRidAvoidanceEnableNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRidAvoidanceEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        rid_warning: RID告警
    */
    virtual ListenIdCode ObserveEventsRidWarningNorthbound(const std::function<void(const std::shared_ptr<EventsRidWarningNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyRidWarningSouthbound(const std::shared_ptr<EventsReplyRidWarningSouthbound>& msg, const std::shared_ptr<EventsRidWarningNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        通知机场加入 rtc_agent 房间
    */
    virtual MeErrorCode SendServicesRtAgentChannelJoinSouthbound(const std::shared_ptr<ServicesRtAgentChannelJoinSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRtAgentChannelJoinNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRtAgentChannelJoinNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRtAgentChannelJoinNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        北向: thing/product/uav/requests/rt_agent_channel_open
        南向: thing/product/uav/requests_reply/rt_agent_channel_open
    */
    virtual ListenIdCode ObserveRequestsRtAgentChannelOpenNorthbound(const std::function<void(const std::shared_ptr<RequestsRtAgentChannelOpenNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyRtAgentChannelOpenSouthbound(const std::shared_ptr<RequestsReplyRtAgentChannelOpenSouthbound>& msg, const std::shared_ptr<RequestsRtAgentChannelOpenNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        北向: thing/product/uav/requests/rt_agent_channel_status_get
        南向: thing/product/uav/requests_reply/rt_agent_channel_status_get
        获取 rtc_agent 状态
    */
    virtual ListenIdCode ObserveRequestsRtAgentChannelStatusGetNorthbound(const std::function<void(const std::shared_ptr<RequestsRtAgentChannelStatusGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyRtAgentChannelStatusGetSouthbound(const std::shared_ptr<RequestsReplyRtAgentChannelStatusGetSouthbound>& msg, const std::shared_ptr<RequestsRtAgentChannelStatusGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        rtk_calibration: 一键标定
    */
    virtual MeErrorCode SendServicesRtkCalibrationSouthbound(const std::shared_ptr<ServicesRtkCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRtkCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyRtkCalibrationNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRtkCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        rtk_calibration: 一键标定执行结果
    */
    virtual MeErrorCode SendEventsRtkCalibrationSouthbound(const std::shared_ptr<EventsRtkCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRtkCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyRtkCalibrationNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRtkCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        sdr_debug_info: 任务准备结果
    */
    virtual MeErrorCode SendEventsSdrDebugInfoSouthbound(const std::shared_ptr<EventsSdrDebugInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySdrDebugInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplySdrDebugInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReplySdrDebugInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        sdr_debug_switch: SDR调试开关
    */
    virtual MeErrorCode SendServicesSdrDebugSwitchSouthbound(const std::shared_ptr<ServicesSdrDebugSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrDebugSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySdrDebugSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrDebugSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        sdr_link_set_config: 图传频段配置（包括切换工作频段、打开/关闭警用与民用频段）
    */
    virtual MeErrorCode SendServicesSdrLinkSetConfigSouthbound(const std::shared_ptr<ServicesSdrLinkSetConfigSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrLinkSetConfigNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySdrLinkSetConfigNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrLinkSetConfigNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        sdr_workmode_switch: 4G开关
    */
    virtual MeErrorCode SendServicesSdrWorkmodeSwitchSouthbound(const std::shared_ptr<ServicesSdrWorkmodeSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrWorkmodeSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySdrWorkmodeSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrWorkmodeSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        self_diagnostic: 一键定损
    */
    virtual MeErrorCode SendServicesSelfDiagnosticSouthbound(const std::shared_ptr<ServicesSelfDiagnosticSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySelfDiagnosticNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySelfDiagnosticNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySelfDiagnosticNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        self_diagnostic: 一键定损执行结果
    */
    virtual MeErrorCode SendEventsSelfDiagnosticSouthbound(const std::shared_ptr<EventsSelfDiagnosticSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySelfDiagnosticNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplySelfDiagnosticNorthbound(const std::function<void(const std::shared_ptr<EventsReplySelfDiagnosticNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        self_diagnostic_cancel: 一键标定取消
    */
    virtual MeErrorCode SendEventsSelfDiagnosticCancelSouthbound(const std::shared_ptr<EventsSelfDiagnosticCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySelfDiagnosticCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplySelfDiagnosticCancelNorthbound(const std::function<void(const std::shared_ptr<EventsReplySelfDiagnosticCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        set_relay_occupied_status: 设置中继占用状态
    */
    virtual MeErrorCode SendServicesSetRelayOccupiedStatusSouthbound(const std::shared_ptr<ServicesSetRelayOccupiedStatusSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySetRelayOccupiedStatusNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySetRelayOccupiedStatusNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySetRelayOccupiedStatusNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        sim_slot_switch: sim和esim切换
    */
    virtual MeErrorCode SendServicesSimSlotSwitchSouthbound(const std::shared_ptr<ServicesSimSlotSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySimSlotSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySimSlotSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySimSlotSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        spotlight_zoom_close: 关闭 Spotlight Zoom
    */
    virtual MeErrorCode SendServicesSpotlightZoomCloseSouthbound(const std::shared_ptr<ServicesSpotlightZoomCloseSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySpotlightZoomCloseNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySpotlightZoomCloseNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySpotlightZoomCloseNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        spotlight_zoom_open: 开启 Spotlight Zoom, 看向目标点
    */
    virtual MeErrorCode SendServicesSpotlightZoomOpenSouthbound(const std::shared_ptr<ServicesSpotlightZoomOpenSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySpotlightZoomOpenNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplySpotlightZoomOpenNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySpotlightZoomOpenNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备静态拓扑上报，目前无回包，没有southbound消息，应该需要和动态拓扑一样，有southbound消息
    */
    virtual ListenIdCode ObserveStatusStaticTopoNorthbound(const std::function<void(const std::shared_ptr<StatusStaticTopoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        station_measure_report: 基站测量上报
    */
    virtual ListenIdCode ObserveEventsStationMeasureReportNorthbound(const std::function<void(const std::shared_ptr<EventsStationMeasureReportNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyStationMeasureReportSouthbound(const std::shared_ptr<EventsReplyStationMeasureReportSouthbound>& msg, const std::shared_ptr<EventsStationMeasureReportNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        云端发生drc杆量控制
    */
    virtual MeErrorCode SendDrcStickControlSouthbound(const std::shared_ptr<DrcStickControlSouthbound>& msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        设备端请求获取flighttask资源
        北向: thing/product/uav/requests/storage_config_get
        南向: thing/product/uav/requests_reply/storage_config_get
    */
    virtual ListenIdCode ObserveRequestsStorageConfigGetNorthbound(const std::function<void(const std::shared_ptr<RequestsStorageConfigGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendRequestsReplyStorageConfigGetSouthbound(const std::shared_ptr<RequestsReplyStorageConfigGetSouthbound>& msg, const std::shared_ptr<RequestsStorageConfigGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        takeoff_to_point: 一键起飞
    */
    virtual MeErrorCode SendServicesTakeoffToPointSouthbound(const std::shared_ptr<ServicesTakeoffToPointSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyTakeoffToPointNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyTakeoffToPointNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyTakeoffToPointNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        OneTakeOff: 一键起飞 执行结果事件通知
        北向 Topic: thing/product/uav/events/takeoff_to_point_progress
        南向 Topic: thing/product/uav/events_reply/takeoff_to_point_progress
    */
    virtual ListenIdCode ObserveEventsTakeoffToPointProgressNorthbound(const std::function<void(const std::shared_ptr<EventsTakeoffToPointProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendEventsReplyTakeoffToPointProgressSouthbound(const std::shared_ptr<EventsReplyTakeoffToPointProgressSouthbound>& msg, const std::shared_ptr<EventsTakeoffToPointProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        thumbnail_upload_finish: 缩略图上传完成
    */
    virtual MeErrorCode SendEventsThumbnailUploadFinishSouthbound(const std::shared_ptr<EventsThumbnailUploadFinishSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyThumbnailUploadFinishNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyThumbnailUploadFinishNorthbound(const std::function<void(const std::shared_ptr<EventsReplyThumbnailUploadFinishNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        unload_ai_model: 卸载AI模型
    */
    virtual MeErrorCode SendServicesUnloadAiModelSouthbound(const std::shared_ptr<ServicesUnloadAiModelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnloadAiModelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyUnloadAiModelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnloadAiModelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        unload_ai_model_progress: 卸载AI模型进度
    */
    virtual MeErrorCode SendEventsUnloadAiModelProgressSouthbound(const std::shared_ptr<EventsUnloadAiModelProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyUnloadAiModelProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyUnloadAiModelProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyUnloadAiModelProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        unlock_license_switch: 解禁license开关
    */
    virtual MeErrorCode SendServicesUnlockLicenseSwitchSouthbound(const std::shared_ptr<ServicesUnlockLicenseSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnlockLicenseSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyUnlockLicenseSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnlockLicenseSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        unlock_license_update: 解禁license更新
    */
    virtual MeErrorCode SendServicesUnlockLicenseUpdateSouthbound(const std::shared_ptr<ServicesUnlockLicenseUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnlockLicenseUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyUnlockLicenseUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnlockLicenseUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        uom_fly_data_info: 无人机飞行数据信息
    */
    virtual MeErrorCode SendEventsUomFlyDataInfoSouthbound(const std::shared_ptr<EventsUomFlyDataInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyUomFlyDataInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveEventsReplyUomFlyDataInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReplyUomFlyDataInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        北向: thing/product/uav/status/update_topo        数据结构: StatusUpdateTopoNorthbound       接口: SendStatusUpdateTopoNorthbound
        南向: thing/product/uav/status_reply/update_topo  数据结构: StatusReplyUpdateTopoSouthbound  接口: ObserveStatusReplyUpdateTopoSouthbound
    */
    virtual ListenIdCode ObserveStatusUpdateTopoNorthbound(const std::function<void(const std::shared_ptr<StatusUpdateTopoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual MeErrorCode SendStatusReplyUpdateTopoSouthbound(const std::shared_ptr<StatusReplyUpdateTopoSouthbound>& msg, const std::shared_ptr<StatusUpdateTopoNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        upload_flighttask_media_prioritize: 媒体上传优先级
    */
    virtual MeErrorCode SendServicesUploadFlighttaskMediaPrioritizeSouthbound(const std::shared_ptr<ServicesUploadFlighttaskMediaPrioritizeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyUploadFlighttaskMediaPrioritizeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        视频存储设置
    */
    virtual MeErrorCode SendServicesVideoStorageSetSouthbound(const std::shared_ptr<ServicesVideoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyVideoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyVideoStorageSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyVideoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

    /*
        write_sdr_topo_info: 写号
    */
    virtual MeErrorCode SendServicesWriteSdrTopoInfoSouthbound(const std::shared_ptr<ServicesWriteSdrTopoInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyWriteSdrTopoInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;
    virtual ListenIdCode ObserveServicesReplyWriteSdrTopoInfoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyWriteSdrTopoInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr) = 0;

};
} // namespace message_engine