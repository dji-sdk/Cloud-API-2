// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/device_debug_module.h"
#include "tool/callback_container.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class DeviceDebugModuleImpl : public IDeviceDebugModule, public drc_sdk::common::AsyncCaptureProtect{
public:
    explicit DeviceDebugModuleImpl();
    ~DeviceDebugModuleImpl();

    void openDebugMode(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDebugModeResult& result)>& callback) override;
    void closeDebugMode(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDebugModeResult& result)>& callback) override;
    void openDockCover(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDockCoverResult& result)>& callback) override;
    void getOpenDockCoverProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDockCoverProgressResult& result)> callback) override;
    int subscribeDockCoverOpenProgress(const std::string& device_sn, const std::function<void(OpenDockCoverResult result)>& callback) override;
    void unsubscribeDockCoverOpenProgress(int listen_id) override;
    void closeDockCover(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDockCoverResult& result)>& callback) override;
    void getCloseDockCoverProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDockCoverProgressResult& result)> callback) override;
    int subscribeDockCoverCloseProgress(const std::string& device_sn, const std::function<void(CloseDockCoverResult result)>& callback) override;
    void unsubscribeDockCoverCloseProgress(int listen_id) override;
    void deviceReboot(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceRebootResult& result)>& callback) override;
    int subscribeDeviceRebootProgress(const std::string& device_sn, const std::function<void(DeviceRebootResult result)>& callback) override;
    void unsubscribeDeviceRebootProgress(int listen_id) override;
    void getDeviceRebootProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceRebootProgressResult& result)> callback) override;
    void deviceFormat(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceFormatResult& result)>& callback) override;
    void getDeviceFormatProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceFormatProgressResult& result)> callback) override;
    int subscribeDeviceFormatProgress(const std::string& device_sn, const std::function<void(DeviceFormatResult result)>& callback) override;
    void unsubscribeDeviceFormatProgress(int listen_id) override;
    void openDrone(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDroneResult& data)>& callback) override;
    int subscribeDroneOpenProgress(const std::string& device_sn, const std::function<void(OpenDroneResult result)>& callback) override;
    void unsubscribeDroneOpenProgress(int listen_id) override;
    void closeDrone(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDroneResult& data)>& callback) override;
    int subscribeDroneCloseProgress(const std::string& device_sn, const std::function<void(CloseDroneResult result)>& callback) override;
    void unsubscribeDroneCloseProgress(int listen_id) override;
    int subscribeDroneFormatProgress(const std::string& device_sn, const std::function<void(DroneFormatResult result)>& callback) override;
    void unsubscribeDroneFormatProgress(int listen_id) override;
    void droneFormat(const std::string& device_sn, const std::function<void(FlightHubError err, const DroneFormatResult& data)>& callback) override;
    void droneSdrWorkMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) override;
    void openCharge(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenChargeResult& data)>& callback) override;
    int subscribeDroneChargeOpenProgress(const std::string& device_sn, const std::function<void(OpenChargeResult result)>& callback) override;
    void unsubscribeDroneChargeOpenProgress(int listen_id) override;
    void closeCharge(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseChargeResult& data)>& callback) override;
    int subscribeDroneChargeCloseProgress(const std::string& device_sn, const std::function<void(CloseChargeResult result)>& callback) override;
    void unsubscribeDroneChargeCloseProgress(int listen_id) override;
    void setDockAirConditionerMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) override;
    void setDockSilentMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) override;
    void setDistanceLimit(const std::string& device_sn, int distance_limit_state, int distance_limit_value, const std::function<void(FlightHubError err, SetDistanceLimitResult result)>& callback) override;
    void setHeightLimit(const std::string& device_sn, int height_limit, const std::function<void(FlightHubError err, SetHeightLimitResult result)>& callback) override;
    void setObstacleAvoidance(const std::string& device_sn, int obstacle_avoidance_state, const std::function<void(FlightHubError err, SetObstacleAvoidanceResult result)>& callback) override;
    void setRemainingPowerForRth(const std::string& device_sn, int remaining_power_for_rth, const std::function<void(FlightHubError err, SetRemainingPowerForRthResult result)>& callback) override;
    void setNightLightsState(const std::string& device_sn, int night_lights_state, const std::function<void(FlightHubError err, SetNightLightsStateResult result)>& callback) override;
    void switchAlarmLightState(const std::string& device_sn, int alarm_state, const std::function<void(FlightHubError err, SwitchAlarmStateResult result)>& callback) override;
    void esimOperatorSwitch(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, const std::function<void(FlightHubError err, EsimOperatorSwitchResult result)>& callback) override;
    int subscribeDeviceEsimOperatorSwitchProgress(const std::string& device_sn, const std::function<void(EsimOperatorSwitchResult result)>& callback) override;
    void unsubscribeDeviceEsimOperatorSwitchProgress(int listen_id) override;
    void simSlotSwitch(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, const std::function<void(FlightHubError err, SimSlotSwitchResult result)>& callback) override;
    int subscribeDeviceSimSlotSwitchProgress(const std::string& device_sn, const std::function<void(SimSlotSwitchResult result)>& callback) override;
    void unsubscribeDeviceSimSlotSwitchProgress(int listen_id) override;
    void setBatteryStoreMode(const std::string& device_sn, int battery_store_mode, const std::function<void(FlightHubError err, SetBatteryStoreModeResult result)>& callback) override;
    void startManualBatterySwap(const std::string& device_sn, int slot_id, const std::function<void(FlightHubError err, ManualBatterySwapResult result)>& callback) override;
    void getManualBatterySwapProgress(const std::string& device_sn, const std::function<void(FlightHubError err, ManualBatterySwapProgressResult result)>& callback) override;
    void setDonglePowerSwitch(const std::string& device_sn, int power_state, const std::function<void(FlightHubError err, DonglePowerSwitchResult result)>& callback) override;
    void setLowPowerSwitch(const std::string& device_sn, int low_power_mode, const std::function<void(FlightHubError err, LowPowerSwitchResult result)>& callback) override;
    void getEsimOperatorSwitchProgress(const std::string& device_sn, const std::function<void(FlightHubError err, EsimOperatorSwitchProgressResult result)>& callback) override;
    void setGeoCagingEnable(const std::string& device_sn, int geo_caging_switch, const std::vector<GeoCagingInfoParam>& geo_caging_infos, const std::function<void(FlightHubError err)>& callback) override;
    void cloudEnvironmentSwitch(const CloudEnvironmentSwitchParam& param, const std::function<void(FlightHubError err)>& callback) override;
    void cloudEnvironmentSwitchCallback(const CloudEnvironmentSwitchCallbackParam& param, const std::function<void(FlightHubError err)>& callback) override;
    int listenDeviceCloudEnvironmentSwitchProgress(const std::string& device_sn, const std::function<void(CloudEnvironmentSwitchResult result)>& callback) override;
    void cancelListenDeviceCloudEnvironmentSwitchProgress(int listen_id) override;

private:
};

} // namespace flight_hub_sdk