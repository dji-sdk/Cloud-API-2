// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 设备调试模块接口，提供机场/飞行器的开关机、舱盖、充电、格式化、重启、网络测试、RTK 标定、中继站调试等功能。
class IDeviceDebugModule {
public:
    virtual ~IDeviceDebugModule() = default;

    /// @brief 打开调试模式
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/debug-mode-open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和打开调试模式结果
    virtual void openDebugMode(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDebugModeResult& result)>& callback) = 0;

    /// @brief 关闭调试模式
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/debug-mode-close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和关闭调试模式结果
    virtual void closeDebugMode(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDebugModeResult& result)>& callback) = 0;

    /// @brief 打开机场舱盖
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/cover-open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和打开舱盖结果
    virtual void openDockCover(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDockCoverResult& result)>& callback) = 0;

    /// @brief 获取打开机场舱盖进度
    ///
    /// @internal
    /// 链路: HTTP | HTTP GET /device/api/v1/service/cover-open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和打开舱盖进度结果
    virtual void getOpenDockCoverProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDockCoverProgressResult& result)> callback) = 0;

    /// @brief 订阅机场舱盖打开进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_cover_open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回打开舱盖结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDockCoverOpenProgress(const std::string& device_sn, const std::function<void(OpenDockCoverResult result)>& callback) = 0;

    /// @brief 取消订阅机场舱盖打开进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDockCoverOpenProgress(int listen_id) = 0;

    /// @brief 关闭机场舱盖
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/cover-close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和关闭舱盖结果
    virtual void closeDockCover(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDockCoverResult& result)>& callback) = 0;

    /// @brief 获取关闭机场舱盖进度
    ///
    /// @internal
    /// 链路: HTTP | HTTP GET /device/api/v1/service/cover-close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和关闭舱盖进度结果
    virtual void getCloseDockCoverProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDockCoverProgressResult& result)> callback) = 0;

    /// @brief 订阅机场舱盖关闭进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_cover_close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回关闭舱盖结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDockCoverCloseProgress(const std::string& device_sn, const std::function<void(CloseDockCoverResult result)>& callback) = 0;

    /// @brief 取消订阅机场舱盖关闭进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDockCoverCloseProgress(int listen_id) = 0;

    /// @brief 设备重启
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/device-reboot
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和设备重启结果
    virtual void deviceReboot(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceRebootResult& result)>& callback) = 0;

    /// @brief 订阅设备重启进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_reboot
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回设备重启结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDeviceRebootProgress(const std::string& device_sn, const std::function<void(DeviceRebootResult result)>& callback) = 0;

    /// @brief 取消订阅设备重启进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDeviceRebootProgress(int listen_id) = 0;

    /// @brief 获取设备重启进度
    ///
    /// @internal
    /// 链路: HTTP | HTTP GET /device/api/v1/service/device-reboot
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和设备重启进度结果
    virtual void getDeviceRebootProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceRebootProgressResult& result)> callback) = 0;

    /// @brief 设备格式化
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/device-format
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和设备格式化结果
    virtual void deviceFormat(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceFormatResult& result)>& callback) = 0;

    /// @brief 获取设备格式化进度
    ///
    /// @internal
    /// 链路: HTTP | HTTP GET /device/api/v1/service/device-format
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和设备格式化进度结果
    virtual void getDeviceFormatProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceFormatProgressResult& result)> callback) = 0;

    /// @brief 订阅设备格式化进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_format
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回设备格式化结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDeviceFormatProgress(const std::string& device_sn, const std::function<void(DeviceFormatResult result)>& callback) = 0;

    /// @brief 取消订阅设备格式化进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDeviceFormatProgress(int listen_id) = 0;

    /// @brief 飞行器开机
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/drone-open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和飞行器开机结果
    virtual void openDrone(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDroneResult& data)>& callback) = 0;

    /// @brief 订阅飞行器开机进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回飞行器开机结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDroneOpenProgress(const std::string& device_sn, const std::function<void(OpenDroneResult result)>& callback) = 0;

    /// @brief 取消订阅飞行器开机进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDroneOpenProgress(int listen_id) = 0;

    /// @brief 飞行器关机
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/drone-close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和飞行器关机结果
    virtual void closeDrone(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDroneResult& data)>& callback) = 0;

    /// @brief 订阅飞行器关机进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回飞行器关机结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDroneCloseProgress(const std::string& device_sn, const std::function<void(CloseDroneResult result)>& callback) = 0;

    /// @brief 取消订阅飞行器关机进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDroneCloseProgress(int listen_id) = 0;

    /// @brief 订阅飞行器格式化进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_format
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回飞行器格式化结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDroneFormatProgress(const std::string& device_sn, const std::function<void(DroneFormatResult result)>& callback) = 0;

    /// @brief 取消订阅飞行器格式化进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDroneFormatProgress(int listen_id) = 0;

    /// @brief 飞行器格式化（过程操作）
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/drone-format
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和飞行器格式化结果
    virtual void droneFormat(const std::string& device_sn, const std::function<void(FlightHubError err, const DroneFormatResult& data)>& callback) = 0;

    /// @brief 切换飞行器 SDR 工作模式（即时操作）
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/sdr-workmode-switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param mode SDR 工作模式
    /// @param callback 结果回调，返回错误信息
    virtual void droneSdrWorkMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 打开充电
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/charge-open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和打开充电结果
    virtual void openCharge(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenChargeResult& data)>& callback) = 0;

    /// @brief 订阅打开充电进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_charge_open
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回打开充电结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDroneChargeOpenProgress(const std::string& device_sn, const std::function<void(OpenChargeResult result)>& callback) = 0;

    /// @brief 取消订阅打开充电进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDroneChargeOpenProgress(int listen_id) = 0;

    /// @brief 关闭充电
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/charge-close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，返回错误信息和关闭充电结果
    virtual void closeCharge(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseChargeResult& data)>& callback) = 0;

    /// @brief 订阅关闭充电进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_charge_close
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回关闭充电结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDroneChargeCloseProgress(const std::string& device_sn, const std::function<void(CloseChargeResult result)>& callback) = 0;

    /// @brief 取消订阅关闭充电进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDroneChargeCloseProgress(int listen_id) = 0;

    /// @brief 设置机场空调模式
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/air-conditioners
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param mode 空调模式。枚举 DockAirConditionerState（value.h）：0=空闲, 1=制冷, 2=制热, 3=除湿
    /// @param callback 结果回调，返回错误信息
    virtual void setDockAirConditionerMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置机场静音模式
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/silent-mode
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param mode 静音模式
    /// @param callback 结果回调，返回错误信息
    virtual void setDockSilentMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置限远距离
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/distance-limit
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param distance_limit_state 限远状态
    /// @param distance_limit_value 限远距离值
    /// @param callback 结果回调，返回错误信息和设置限远结果
    virtual void setDistanceLimit(const std::string& device_sn, int distance_limit_state, int distance_limit_value, const std::function<void(FlightHubError err, SetDistanceLimitResult result)>& callback) = 0;

    /// @brief 设置限高
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/height-limit
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param height_limit 限高值
    /// @param callback 结果回调，返回错误信息和设置限高结果
    virtual void setHeightLimit(const std::string& device_sn, int height_limit, const std::function<void(FlightHubError err, SetHeightLimitResult result)>& callback) = 0;

    /// @brief 设置避障状态
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/obstacle-avoidance
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param obstacle_avoidance_state 避障状态
    /// @param callback 结果回调，返回错误信息和设置避障结果
    virtual void setObstacleAvoidance(const std::string& device_sn, int obstacle_avoidance_state, const std::function<void(FlightHubError err, SetObstacleAvoidanceResult result)>& callback) = 0;

    /// @brief 设置返航剩余电量
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/remaining-power-for-rth
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param remaining_power_for_rth 返航剩余电量百分比
    /// @param callback 结果回调，返回错误信息和设置返航电量结果
    virtual void setRemainingPowerForRth(const std::string& device_sn, int remaining_power_for_rth, const std::function<void(FlightHubError err, SetRemainingPowerForRthResult result)>& callback) = 0;

    /// @brief 设置夜航灯状态
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/night-lights-state
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param night_lights_state 夜航灯状态
    /// @param callback 结果回调，返回错误信息和设置夜航灯结果
    virtual void setNightLightsState(const std::string& device_sn, int night_lights_state, const std::function<void(FlightHubError err, SetNightLightsStateResult result)>& callback) = 0;

    /// @brief 切换报警灯状态
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/alarm-state-switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param alarm_state 报警灯状态
    /// @param callback 结果回调，返回错误信息和切换报警灯结果
    virtual void switchAlarmLightState(const std::string& device_sn, int alarm_state, const std::function<void(FlightHubError err, SwitchAlarmStateResult result)>& callback) = 0;

    /// @brief eSIM 运营商切换
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/esim-operator-switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param imei 设备 IMEI
    /// @param operator_type 运营商类型
    /// @param device_type 设备类型
    /// @param callback 结果回调，返回错误信息和 eSIM 运营商切换结果
    virtual void esimOperatorSwitch(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, const std::function<void(FlightHubError err, EsimOperatorSwitchResult result)>& callback) = 0;

    /// @brief 订阅 eSIM 运营商切换进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_esim_operator_switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回 eSIM 运营商切换结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDeviceEsimOperatorSwitchProgress(const std::string& device_sn, const std::function<void(EsimOperatorSwitchResult result)>& callback) = 0;

    /// @brief 取消订阅 eSIM 运营商切换进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDeviceEsimOperatorSwitchProgress(int listen_id) = 0;

    /// @brief SIM 卡槽切换
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/sim-slot-switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param imei 设备 IMEI
    /// @param sim_slot SIM 卡槽
    /// @param device_type 设备类型
    /// @param callback 结果回调，返回错误信息和 SIM 卡槽切换结果
    virtual void simSlotSwitch(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, const std::function<void(FlightHubError err, SimSlotSwitchResult result)>& callback) = 0;

    /// @brief 订阅 SIM 卡槽切换进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_sim_slot_switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回 SIM 卡槽切换结果
    /// @return 监听 ID，用于取消订阅
    virtual int subscribeDeviceSimSlotSwitchProgress(const std::string& device_sn, const std::function<void(SimSlotSwitchResult result)>& callback) = 0;

    /// @brief 取消订阅 SIM 卡槽切换进度
    /// @param listen_id 监听 ID
    virtual void unsubscribeDeviceSimSlotSwitchProgress(int listen_id) = 0;

    /// @brief 设置电池存储模式
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/battery-store-mode-switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param battery_store_mode 电池存储模式
    /// @param callback 结果回调，返回错误信息和设置电池存储模式结果
    virtual void setBatteryStoreMode(const std::string& device_sn, int battery_store_mode, const std::function<void(FlightHubError err, SetBatteryStoreModeResult result)>& callback) = 0;

    /// @brief 发起手动换电
    ///
    /// @internal
    /// 链路: HTTP | HTTP PUT /device/api/v1/service/manual-battery-swap
    /// @endinternal
    /// @param device_sn 设备序列号（机场 SN）
    /// @param slot_id 目标电池卡槽 ID
    /// @param callback 结果回调，返回错误信息和换电发起结果
    virtual void startManualBatterySwap(const std::string& device_sn, int slot_id, const std::function<void(FlightHubError err, ManualBatterySwapResult result)>& callback) = 0;

    /// @brief 查询手动换电进度
    ///
    /// @internal
    /// 链路: HTTP | HTTP GET /device/api/v1/service/manual-battery-swap?sn={device_sn}
    /// @endinternal
    /// @param device_sn 设备序列号（机场 SN，query 键为 sn）
    /// @param callback 结果回调，返回错误信息和换电进度结果
    virtual void getManualBatterySwapProgress(const std::string& device_sn, const std::function<void(FlightHubError err, ManualBatterySwapProgressResult result)>& callback) = 0;

    /// @brief Dongle 电源开关
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/dongle-power-switch
    /// 请求体: {"device_sn":"...","data":{"action":0/1}}
    /// 注意: device_sn 为飞行器 SN（非机场 SN），飞机需已安装 Dongle
    /// @endinternal
    /// @param device_sn 飞行器序列号（非机场 SN）
    /// @param power_state 电源状态，0=关，1=开（序列化为 JSON 字段 action）
    /// @param callback 结果回调，返回错误信息和 Dongle 电源开关结果
    virtual void setDonglePowerSwitch(const std::string& device_sn, int power_state, const std::function<void(FlightHubError err, DonglePowerSwitchResult result)>& callback) = 0;

    /// @brief 机场低功耗开关
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/low-power-switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param low_power_mode 低功耗模式，0=关，1=开
    /// @param callback 结果回调，返回错误信息和低功耗开关结果
    virtual void setLowPowerSwitch(const std::string& device_sn, int low_power_mode, const std::function<void(FlightHubError err, LowPowerSwitchResult result)>& callback) = 0;

    /// @brief 查询 eSIM 运营商切换进度
    ///
    /// @internal
    /// 链路: HTTP | HTTP GET /device/api/v1/service/esim-operator-switch?device_sn={device_sn}
    /// @endinternal
    /// @param device_sn 设备序列号（query 键为 sn）
    /// @param callback 结果回调，返回错误信息和切换进度
    virtual void getEsimOperatorSwitchProgress(const std::string& device_sn, const std::function<void(FlightHubError err, EsimOperatorSwitchProgressResult result)>& callback) = 0;

    /// @brief 设置 Geo Caging（地理围栏）开关及围栏参数
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/geo-caging-enable
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param geo_caging_switch Geo Caging 开关状态，1=开启，0=关闭
    /// @param geo_caging_infos 围栏信息列表，每个元素包含飞行围栏和自定义围栏参数
    /// @param callback 结果回调，返回错误信息
    virtual void setGeoCagingEnable(const std::string& device_sn, int geo_caging_switch, const std::vector<GeoCagingInfoParam>& geo_caging_infos, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 云环境切换下发通知
    ///
    /// 设备完成云环境切换后调用此接口通知服务端，携带切换结果及设备一机一密鉴权信息。
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/cloud-environment-switch
    /// @endinternal
    /// @param param 设备列表 + 目标云环境配置
    /// @param callback 结果回调，返回错误信息
    virtual void cloudEnvironmentSwitch(const CloudEnvironmentSwitchParam& param, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 云环境切换回调通知
    ///
    /// 设备侧执行云环境切换后调用此接口上报切换结果（含一机一密鉴权信息），支持司空2 公有云、司空2 私有云、
    /// 三方云(API1)、三方云(API2)之间互切。
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/cloud_environment_switch/callback
    /// @endinternal
    /// @param param 切换结果、鉴权信息、设备列表 + 目标云环境配置
    /// @param callback 结果回调，返回错误信息
    virtual void cloudEnvironmentSwitchCallback(const CloudEnvironmentSwitchCallbackParam& param, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 订阅云环境切换进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_cloud_environment_switch
    /// @endinternal
    /// @param device_sn 设备序列号
    /// @param callback 进度回调，返回云环境切换结果
    /// @return 监听 ID，用于取消订阅
    virtual int listenDeviceCloudEnvironmentSwitchProgress(const std::string& device_sn, const std::function<void(CloudEnvironmentSwitchResult result)>& callback) = 0;

    /// @brief 取消订阅云环境切换进度
    /// @param listen_id 监听 ID
    virtual void cancelListenDeviceCloudEnvironmentSwitchProgress(int listen_id) = 0;
};

}
