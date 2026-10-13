// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "dock_osd_module_impl.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;
using namespace notification_center;

DockOsdModuleImpl::DockOsdModuleImpl(const std::string& dock_sn)
    : dock_sn_(dock_sn) {
    WeakDummy(p);
    registerNotificationCenterListeners();
}

void DockOsdModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);
    // 初始化机场位置监听
    dock_location_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockLocation(
        [this, p](const notification_center::DockLocation& msg)-> void {
            WeakDummyReturn(p);
            DockLocation dock_location;
            dock_location.latitude = msg.latitude;
            dock_location.longitude = msg.longitude;
            dock_location.height = msg.height;
            dock_location.heading = msg.heading;
            
            LOG_DEBUG("[DockOsdModuleImpl] listenDockLocation, lat: %f, lon: %f, height: %f, heading: %f", 
                dock_location.latitude, dock_location.longitude, 
                dock_location.height, dock_location.heading);
            dock_location_callbacks_.notifyCallbackIfChanged(dock_location);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] listenDockLocation, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);
    if (dock_location_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl] ListenDockLocation, error: %d", dock_location_listen_id_);
    }
    // 初始化机场中的无人机监听
    drone_in_dock_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneInDock(
        [this, p](const notification_center::DroneInDock& msg)-> void {
            WeakDummyReturn(p);
            DroneInDock drone_in_dock;
            drone_in_dock.drone_in_dock = msg.drone_in_dock;
            
            LOG_DEBUG("[DockOsdModuleImpl] listenDroneInDock, drone_in_dock: %d", 
                drone_in_dock.drone_in_dock);
            drone_in_dock_callbacks_.notifyCallbackIfChanged(drone_in_dock);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] listenDroneInDock, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);
    if (drone_in_dock_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl] ListenDroneInDock, error: %d", drone_in_dock_listen_id_);
    }

    // 初始化: 监听机场舱盖状态;
    dock_cover_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockCoverState(
        [this, p](const notification_center::DockCoverState& msg)-> void {
            WeakDummyReturn(p);
            // 数据转换;
            flight_hub_sdk::DockCoverState dock_cover_state;
            dock_cover_state.cover_state = msg.cover_state;

            LOG_DEBUG("[DockOsdModuleImpl] listenDockCoverState, dock_cover_state: %d",  dock_cover_state.cover_state);
            dock_cover_state_callbacks_.notifyCallbackIfChanged(dock_cover_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] listenDockCoverState, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);

    if (dock_cover_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl] listenDockCoverState, error dock_cover_state_listen_id: %d", dock_cover_state_listen_id_);
    }

    // 初始化: 监听机场飞机充电状态;
    dock_drone_charge_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockDroneChargeState(
        [this, p](const notification_center::DockDroneChargeState& msg)-> void {
            WeakDummyReturn(p);
            // 数据转换;
            flight_hub_sdk::DockDroneChargeState dock_drone_charge_state;
            dock_drone_charge_state.state = msg.state;    // 0: 空闲, 1: 正在充电;
            dock_drone_charge_state.capacity_percent = msg.capacity_percent;

            LOG_DEBUG("[DockOsdModuleImpl] listenDockDroneChargeState, {state: %d, capacity_percent: %d}",
                dock_drone_charge_state.state, dock_drone_charge_state.capacity_percent
            );
            dock_drone_charge_state_callbacks_.notifyCallbackIfChanged(dock_drone_charge_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] listenDockDroneChargeState, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);

    if (dock_drone_charge_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl] listenDockDroneChargeState, error dock_drone_charge_state_listen_id: %d", dock_drone_charge_state_listen_id_);
    }

    // 初始化监听: 机场气象信息;
    dock_meteorological_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockMeteorologicalInfo(
        [this, p](const notification_center::DockMeteorologicalInfo& msg)-> void {
             WeakDummyReturn(p);
            // 数据转换;
            flight_hub_sdk::DockMeteorologicalInfo dock_meteorological_info;
            dock_meteorological_info.temperature = msg.temperature;                          // 舱内温度;
            dock_meteorological_info.environment_temperature = msg.environment_temperature;  // 环境温度;
            dock_meteorological_info.humidity = msg.humidity;          // 舱内湿度;
            dock_meteorological_info.wind_speed = msg.wind_speed;      // 风速;
            dock_meteorological_info.rainfall = msg.rainfall;          // 雨量;

            LOG_DEBUG("[DockOsdModuleImpl] listenDockMeteorologicalInfo, {temperature: %g, environment_temperature: %g, humidity: %d, wind_speed: %g, rainfall: %d}",
                dock_meteorological_info.temperature,
                dock_meteorological_info.environment_temperature,
                dock_meteorological_info.humidity,
                dock_meteorological_info.wind_speed,
                dock_meteorological_info.rainfall
            );
            dock_meteorological_info_callbacks_.notifyCallbackIfChanged(dock_meteorological_info);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] listenDockMeteorologicalInfo, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);

    if (dock_meteorological_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
            LOG_ERROR("[DockOsdModuleImpl] listenDockMeteorologicalInfo, error dock_meteorological_info_listen_id: %d", dock_meteorological_info_listen_id_);
    }


    // 初始化监听: 机场备用电池;
    dock_backup_battery_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockBackupBatteryInfo(
        [this, p](const notification_center::DockBackupBatteryInfo& msg)-> void {
            WeakDummyReturn(p);
            // 数据转换;
            flight_hub_sdk::DockBackupBatteryInfo dock_backup_battery_info;
            dock_backup_battery_info.switch_state = msg.switch_state;  // 备用电池开关(0: 备用电池关闭, 1: 备用电池开启);
            dock_backup_battery_info.voltage = msg.voltage;            // 备用电池电压(单位: 毫伏, mV);
            dock_backup_battery_info.temperature = msg.temperature;    // 备用电池温度(单位: 摄氏度);

            LOG_DEBUG("[DockOsdModuleImpl] ListenDockBackupBatteryInfo, {switch_state: %d, voltage: %d, temperature: %g}",
                dock_backup_battery_info.switch_state,
                dock_backup_battery_info.voltage,
                dock_backup_battery_info.temperature
            );
            dock_backup_battery_info_callbacks_.notifyCallbackIfChanged(dock_backup_battery_info);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] ListenDockBackupBatteryInfo, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);

    if (dock_backup_battery_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl] ListenDockBackupBatteryInfo, error dock_backup_battery_info_listen_id: %d", dock_backup_battery_info_listen_id_);
    }


    // 初始化监听: 机场保养维护信息;
    dock_maintain_status_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockMaintainStatusInfo(
        [this, p](const notification_center::DockMaintainStatusInfo& msg)-> void {
            WeakDummyReturn(p);
            // 数据转换;
            flight_hub_sdk::DockMaintainStatusInfo dock_maintain_status_info;
            flight_hub_sdk::DockMaintainStatus temp_status;

            for (const auto &it : msg.maintain_status_info) {
                temp_status.state = it.state;
                temp_status.last_maintain_type = it.last_maintain_type;
                temp_status.last_maintain_time = it.last_maintain_time;
                temp_status.last_maintain_work_sorties = it.last_maintain_work_sorties;

                dock_maintain_status_info.maintain_status_info.emplace_back(temp_status);
            }

            LOG_DEBUG("[DockOsdModuleImpl] listenDockMaintainStatusInfo size: %d", (int)dock_maintain_status_info.maintain_status_info.size());

            for (const auto &it : dock_maintain_status_info.maintain_status_info) {
                LOG_DEBUG("[DockOsdModuleImpl] DockMaintainStatus, {state: %d, last_maintain_type: %d, last_maintain_time: %lld, last_maintain_work_sorties: %d}",
                    it.state,
                    it.last_maintain_type,
                    it.last_maintain_time,
                    it.last_maintain_work_sorties
                );
            }

            dock_maintain_status_info_callbacks_.notifyCallbackIfChanged(dock_maintain_status_info);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl] listenDockMaintainStatusInfo, error: %d", error_code);
            }
        }, dock_sn_, notification_center::NotificationFrequency_OnChanged);

    if (dock_maintain_status_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl] listenDockMaintainStatusInfo, error dock_maintain_status_info_listen_id: %d", dock_maintain_status_info_listen_id_);
    }
    // 监听机场 mode code;
    dock_mode_code_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockModeCode(
        [this, p](const notification_center::DockModeCode& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockModeCode dock_mode_code{};
            dock_mode_code.mode_code = msg.mode_code;

            LOG_DEBUG("[DockImpl DockModeCode] ListenDockModeCode(), mode_code: %d", dock_mode_code.mode_code);

            // Notify ModeCode data;
            dock_mode_code_callbacks_.notifyCallbackIfChanged(dock_mode_code);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockModeCode] ListenDockModeCode, error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_mode_code_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockModeCode] ListenDockModeCode, error dock_mode_code_listen_id: %d", dock_mode_code_listen_id_);
    }

    // 监听飞行任务 step code;
    flight_task_step_code_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenFlightTaskStepCode(
        [this, p](const notification_center::FlightTaskStepCode& msg)-> void {
            WeakDummyReturn(p);

            flight_hub_sdk::FlightTaskStepCode flight_task_step_code{};
            flight_task_step_code.flighttask_step_code = msg.flighttask_step_code;

            LOG_DEBUG("[DockOsdModuleImpl FlightTaskStepCode] ListenFlightTaskStepCode(), flighttask_step_code: %d",
                      flight_task_step_code.flighttask_step_code);

            flight_task_step_code_callbacks_.notifyCallbackIfChanged(flight_task_step_code);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockOsdModuleImpl FlightTaskStepCode] ListenFlightTaskStepCode, error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (flight_task_step_code_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockOsdModuleImpl FlightTaskStepCode] ListenFlightTaskStepCode, error flight_task_step_code_listen_id: %d",
                  flight_task_step_code_listen_id_);
    }

    // 机场: 监听子设备信息列表(sub_device);
    dock_sub_device_info_list_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockSubDevice(
        [this, p](const notification_center::DockSubDeviceInfoList& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockSubDeviceInfoList dock_sub_device_info_list{};
            flight_hub_sdk::DockSubDeviceInfo     dock_sub_device_info{};

            LOG_DEBUG("[DockImpl DockSubDevice] ListenDockSubDevice(), DockSubDeviceInfoList size: %d", int(msg.sub_device_info_vec.size()));

            for (const auto& sub_device_info : msg.sub_device_info_vec) {
                dock_sub_device_info.device_sn = sub_device_info.device_sn;
                dock_sub_device_info.device_model_key = sub_device_info.device_model_key;
                dock_sub_device_info.device_online_status = sub_device_info.device_online_status;
                dock_sub_device_info.device_paired = sub_device_info.device_paired;

                dock_sub_device_info_list.sub_device_info_vec.emplace_back(dock_sub_device_info);
            }

            // Notify data;
            dock_sub_device_info_list_callbacks_.notifyCallbackIfChanged(dock_sub_device_info_list);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockSubDevice] ListenDockSubDevice(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_sub_device_info_list_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockSubDevice] ListenDockSubDevice, error dock_sub_device_info_list_listen_id: %d", dock_sub_device_info_list_listen_id_);
    }

    // 机场: 网络状态(network_state);
    dock_network_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockNetworkState(
        [this, p](const notification_center::DockNetworkState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockNetworkState dock_network_state{};

            dock_network_state.type = msg.type;
            dock_network_state.quality = msg.quality;
            dock_network_state.rate = msg.rate;

            LOG_DEBUG("[DockImpl DockNetworkState] ListenDockNetworkState(), {type: %d, quality: %d, rate: %d}",
                dock_network_state.type, dock_network_state.quality, dock_network_state.rate);

            // Notify data;
            dock_network_state_callbacks_.notifyCallbackIfChanged(dock_network_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockNetworkState] ListenDockNetworkState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_network_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockNetworkState] ListenDockNetworkState, error dock_network_state_listen_id: %d", dock_network_state_listen_id_);
    }


    // 机场: 媒体文件数量(media_file_detail);
    dock_media_file_detail_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockMediaFileDetail(
        [this, p](const notification_center::DockMediaFileDetail& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockMediaFileDetail dock_media_file_detail{};

            dock_media_file_detail.remain_upload = msg.remain_upload;

            LOG_DEBUG("[DockImpl DockMediaFileDetail] ListenDockMediaFileDetail(), { remain_upload: %d }", dock_media_file_detail.remain_upload);

            // Notify data;
            dock_media_file_detail_callbacks_.notifyCallbackIfChanged(dock_media_file_detail);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockMediaFileDetail] ListenDockMediaFileDetail(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_media_file_detail_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockMediaFileDetail] ListenDockMediaFileDetail, error dock_media_file_detail_listen_id: %d", dock_media_file_detail_listen_id_);
    }


     // 机场: wireless_link(wireless_link);
    dock_wireless_link_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockWirelessLink(
        [this, p](const notification_center::DockWirelessLink& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockWirelessLink dock_wireless_link{};
            dock_wireless_link.dongle_num = msg.dongle_num;              // int: 0,1,2 飞机上 dongle 数量;
            dock_wireless_link.link_state_4g = msg.link_state_4g;        // int: 4g 链路连接状态, 0-断开, 1-连接;
            dock_wireless_link.sdr_link_state = msg.sdr_link_state;      // int, "sdr链路状态", { 0: 断开, 1: 连接 };
            dock_wireless_link.link_workmode = msg.link_workmode;        // int, "链路工作模式", { 0: sdr only, 1: 4G融合 };
            dock_wireless_link.sdr_quality = msg.sdr_quality;            // int: 标识sdr信号强弱, 0~5 一共六档;
            dock_wireless_link.quality_4g = msg.quality_4g;              // int: 标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality;
            dock_wireless_link.uav_quality_4g = msg.uav_quality_4g;      // int: 标识飞机端4G信号强弱,0~5 一共六档;
            dock_wireless_link.gnd_quality_4g = msg.gnd_quality_4g;      // int: 标识地面端4G信号强弱,0~5 一共六档;
            dock_wireless_link.sdr_freq_band = msg.sdr_freq_band;        // float: sdr 频段;
            dock_wireless_link.freq_band_4g = msg.freq_band_4g;          // float: 4g 频段;


            LOG_DEBUG("[DockImpl DockWirelessLink] [device: %s] ListenDockWirelessLink(), DockWirelessLink, "
                "{\n"
                "    dongle_num: %d, \n"
                "    link_state_4g: %d, \n"
                "    sdr_link_state: %d, \n"
                "    link_workmode: %d, \n"
                "    sdr_quality: %d, \n"
                "    quality_4g: %d, \n"
                "    uav_quality_4g: %d, \n"
                "    gnd_quality_4g: %d, \n"
                "    sdr_freq_band: %f, \n"
                "    freq_band_4g: %f"
                "} \n",
                this->dock_sn_.c_str(),
                msg.dongle_num,
                msg.link_state_4g,
                msg.sdr_link_state,
                msg.link_workmode,
                msg.sdr_quality,
                msg.quality_4g,
                msg.uav_quality_4g,
                msg.gnd_quality_4g,
                msg.sdr_freq_band,
                msg.freq_band_4g
            );


            // Notify data;
            dock_wireless_link_callbacks_.notifyCallbackIfChanged(dock_wireless_link);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockWirelessLink] ListenDockWirelessLink(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_wireless_link_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockWirelessLink] ListenDockWirelessLink(), error dock_wireless_link_listen_id: %d", dock_wireless_link_listen_id_);
    }


    // 机场: 机场作业次数(job_number);
    dock_job_number_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockJobNumber(
        [this, p](const notification_center::DockJobNumber& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockJobNumber dock_job_number{};
            dock_job_number.job_number = msg.job_number;

            LOG_DEBUG("[DockImpl DockJobNumber] ListenDockJobNumber(), { job_number: %d }", dock_job_number.job_number);

            // Notify data;
            dock_job_number_callbacks_.notifyCallbackIfChanged(dock_job_number);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockJobNumber] ListenDockJobNumber(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_job_number_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockJobNumber] ListenDockJobNumber(), error dock_job_number_listen_id: %d", dock_job_number_listen_id_);
    }


    // 机场：DockPowerState；
    dock_power_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockPowerState(
        [this, p](const notification_center::DockPowerState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockPowerState dock_power_state{};

            dock_power_state.electric_supply_voltage = msg.electric_supply_voltage;
            dock_power_state.working_voltage = msg.working_voltage;
            dock_power_state.working_current = msg.working_current;


            LOG_DEBUG("[DockImpl DockPowerState] [device: %s] ListenDockPowerState(), DockPowerState: { electric_supply_voltage: %d, working_voltage: %d, working_current: %d }",
                    this->dock_sn_.c_str(),
                    msg.electric_supply_voltage,
                    msg.working_voltage,
                    msg.working_current
            );

            // Notify data;
            dock_power_state_callbacks_.notifyCallbackIfChanged(dock_power_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockPowerState] ListenDockPowerState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_power_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockPowerState] ListenDockPowerState(), error dock_power_state_listen_id: %d", dock_power_state_listen_id_);
    }


    // 机场: 存储容量(storage);
    dock_storage_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockStorageState(
        [this, p](const notification_center::DockStorageState &msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockStorageState dock_storage_state{};

            dock_storage_state.total = msg.total;
            dock_storage_state.used = msg.used;

            LOG_DEBUG("[DockImpl DockStorageState] ListenDockStorageState(), { total: %d, used: %d }",
                dock_storage_state.total,
                dock_storage_state.used
            );

            // Notify data;
            dock_storage_state_callbacks_.notifyCallbackIfChanged(dock_storage_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockStorageState] ListenDockStorageState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_storage_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockStorageState] ListenDockStorageState(), error dock_storage_state_listen_id: %d", dock_storage_state_listen_id_);
    }

    // 机场首次上电时间(first_power_on);
    dock_first_power_on_time_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockFirstPowerOnTime(
        [this, p](const notification_center::DockFirstPowerOnTime& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockFirstPowerOnTime dock_first_power_on_time{};

            dock_first_power_on_time.first_power_on = msg.first_power_on;
            LOG_DEBUG("[DockImpl DockFirstPowerOnTime] ListenDockFirstPowerOnTime(), { first_power_on: %s }",
                dock_first_power_on_time.first_power_on.c_str()
            );

            // Notify data;
            dock_first_power_on_time_callbacks_.notifyCallbackIfChanged(dock_first_power_on_time);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockFirstPowerOnTime] ListenDockFirstPowerOnTime(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_first_power_on_time_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockFirstPowerOnTime] ListenDockFirstPowerOnTime(), error dock_first_power_on_time_listen_id: %d", dock_first_power_on_time_listen_id_);
    }

    // 机场累计运行时长(acc_time);
    dock_acc_time_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockAccumulatedRunningTime(
        [this, p](const notification_center::DockAccTime& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockAccTime dock_acc_time{};

            dock_acc_time.acc_time = msg.acc_time;

            LOG_DEBUG("[DockImpl DockAccTime] ListenDockAccumulatedRunningTime(), { acc_time: %d }", dock_acc_time.acc_time);

            // Notify data;
            dock_acc_time_callbacks_.notifyCallbackIfChanged(dock_acc_time);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                 LOG_ERROR("[DockImpl DockAccTime] ListenDockAccumulatedRunningTime(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_acc_time_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockAccTime] ListenDockAccumulatedRunningTime(), error dock_acc_time_listen_id: %d", dock_acc_time_listen_id_);
    }

    // 备降点(alternate_land_point);
    dock_alternate_land_point_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockAlternateLandPoint(
        [this, p](const notification_center::DockAlternateLandPoint& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockAlternateLandPoint dock_alternate_land_point{};

            dock_alternate_land_point.is_configured = msg.is_configured;
            dock_alternate_land_point.longitude = msg.longitude;
            dock_alternate_land_point.latitude = msg.latitude;
            dock_alternate_land_point.safe_land_height = msg.safe_land_height;
            dock_alternate_land_point.height = msg.height;

            LOG_DEBUG("[DockImpl DockAlternateLandPoint] ListenDockAlternateLandPoint(), { is_configured: %d, longitude: %f, latitude: %f, safe_land_height: %f, height: %f }",
                dock_alternate_land_point.is_configured,
                dock_alternate_land_point.longitude,
                dock_alternate_land_point.latitude,
                dock_alternate_land_point.safe_land_height,
                dock_alternate_land_point.height
            );

            // Notify data;
            dock_alternate_land_point_callbacks_.notifyCallbackIfChanged(dock_alternate_land_point);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockAlternateLandPoint] ListenDockAlternateLandPoint(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_alternate_land_point_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockAccTime] ListenDockAccumulatedRunningTime(), error dock_alternate_land_point_listen_id: %d", dock_alternate_land_point_listen_id_);
    }

    // 搜星状态(position_state);
    dock_position_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockPositionState(
        [this, p](const notification_center::DockPositionState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockPositionState dock_position_state{};

            
            dock_position_state.is_calibration = msg.is_calibration;
            dock_position_state.is_fixed = msg.is_fixed;
            dock_position_state.quality = msg.quality;
            dock_position_state.gps_number = msg.gps_number;
            dock_position_state.rtk_number = msg.rtk_number;

            LOG_DEBUG("[DockImpl DockPositionState] ListenDockPositionState(), { is_calibration: %d, is_fixed: %d, quality: %d, gps_number: %d, rtk_number: %d }",
                dock_position_state.is_calibration,
                dock_position_state.is_fixed,
                dock_position_state.quality,
                dock_position_state.gps_number,
                dock_position_state.rtk_number
            );

            // Notify data;
            dock_position_state_callbacks_.notifyCallbackIfChanged(dock_position_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockPositionState] ListenDockPositionState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_position_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockPositionState] ListenDockPositionState(), error dock_position_state_listen_id: %d", dock_position_state_listen_id_);
    }


    // 机场激活时间(activation_time, unix时间戳 s);
    dock_activation_time_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockActivationTime(
        [this, p](const notification_center::DockActivationTime& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockActivationTime dock_activation_time{};
            dock_activation_time.activation_time = msg.activation_time;

            LOG_DEBUG("[DockImpl DockActivationTime] ListenDockActivationTime(), { activation_time: %d }", msg.activation_time);

            // Notify data;
            dock_activation_time_callbacks_.notifyCallbackIfChanged(dock_activation_time);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockActivationTime] ListenDockActivationTime(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_activation_time_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockActivationTime] ListenDockActivationTime(), error dock_activation_time_listen_id: %d", dock_activation_time_listen_id_);
    }

     // 电池(存储)模式(battery_store_mode);
     dock_battery_store_mode_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockBatteryStoreMode(
        [this, p](const notification_center::DockBatteryStoreMode& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockBatteryStoreMode dock_battery_store_mode{};
            dock_battery_store_mode.battery_store_mode = msg.battery_store_mode;  // battery_store_mode

            LOG_DEBUG("[DockImpl DockBatteryStoreMode] ListenDockBatteryStoreMode(), { battery_store_mode: %d }", msg.battery_store_mode);

            // Notify data;
            dock_battery_store_mode_callbacks_.notifyCallbackIfChanged(dock_battery_store_mode);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockBatteryStoreMode] ListenDockBatteryStoreMode(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
     );

     if (dock_battery_store_mode_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
         LOG_ERROR("[DockImpl DockBatteryStoreMode] ListenDockBatteryStoreMode(), error dock_battery_store_mode_listen_id: %d", dock_battery_store_mode_listen_id_);
     }

    // 机场声光报警状态(alarm_state);
    dock_alarm_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockAlarmState(
        [this, p](const notification_center::DockAlarmState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockAlarmState dock_alarm_state{};
            dock_alarm_state.alarm_state = msg.alarm_state;

            LOG_DEBUG("[DockImpl DockAlarmState] ListenDockAlarmState(), { alarm_state: %d }", msg.alarm_state);

            // Notify data;
            dock_alarm_state_callbacks_.notifyCallbackIfChanged(dock_alarm_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockAlarmState] ListenDockAlarmState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_alarm_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockAlarmState] ListenDockAlarmState(), error dock_alarm_state_listen_id: %d", dock_alarm_state_listen_id_);
    }


    // 机场: 飞行器电池保养信息(drone_battery_maintenance_info);
    // DockDroneBatteryMaintenanceInfo;
    dock_drone_battery_maintenance_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockDroneBatteryMaintenanceInfo(
        [this, p](const notification_center::DockDroneBatteryMaintenanceInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockDroneBatteryMaintenanceInfo dock_drone_battery_maintenance_info{};
            dock_drone_battery_maintenance_info.batteries.reserve(msg.batteries.size());

            for (const auto& bat : msg.batteries) {
                flight_hub_sdk::BatteryBasicInfo battery_basic_info{};
                battery_basic_info.index = bat.index;                         // 电池序号, 0:左电池; 1:右电池;
                battery_basic_info.capacity_percent = bat.capacity_percent;   // 电池剩余电量,[0,100];
                battery_basic_info.voltage = bat.voltage;                     // 电压(mV), 参考[0~28000mV];
                battery_basic_info.temperature = bat.temperature;             // 温度(摄氏度);
 
                dock_drone_battery_maintenance_info.batteries.emplace_back(battery_basic_info);
            }

            dock_drone_battery_maintenance_info.heat_state = msg.heat_state;
            dock_drone_battery_maintenance_info.maintenance_state = msg.maintenance_state;
            dock_drone_battery_maintenance_info.maintenance_time_left = msg.maintenance_time_left;

            LOG_DEBUG("[DockImpl DockDroneBatteryMaintenanceInfo] ListenDockDroneBatteryMaintenanceInfo(), "
                "msg data: { heat_state: %d, maintenance_state: %d, maintenance_time_left: %d, batteries count: %d }",
                msg.heat_state,
                msg.maintenance_state,
                msg.maintenance_time_left,
                static_cast<int>(msg.batteries.size())
            );

            // Notify data;
            dock_drone_battery_maintenance_info_callbacks_.notifyCallbackIfChanged(dock_drone_battery_maintenance_info);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockDroneBatteryMaintenanceInfo] ListenDockDroneBatteryMaintenanceInfo(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_drone_battery_maintenance_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockDroneBatteryMaintenanceInfo] ListenDockDroneBatteryMaintenanceInfo(), error dock_drone_battery_maintenance_info_listen_id: %d",
            dock_drone_battery_maintenance_info_listen_id_);
    }


    // 机场: 紧急停止按钮状态(emergency_stop_state);
    // DockEmergencyStopState;
    dock_emergency_stop_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockEmergencyStopState(
        [this, p](const notification_center::DockEmergencyStopState& msg)-> void {
            WeakDummyReturn(p);
            
            // 数据转换;
             flight_hub_sdk::DockEmergencyStopState dock_emergency_stop_state{};

             dock_emergency_stop_state.emergency_stop_state = msg.emergency_stop_state;

             LOG_DEBUG("[DockImpl DockEmergencyStopState] ListenDockEmergencyStopState(), { emergency_stop_state: %d }", msg.emergency_stop_state);

            dock_emergency_stop_state_callbacks_.notifyCallbackIfChanged(dock_emergency_stop_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockEmergencyStopState] ListenDockEmergencyStopState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_emergency_stop_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockEmergencyStopState] ListenDockEmergencyStopState(), error dock_emergency_stop_state_listen_id: %d",
            dock_emergency_stop_state_listen_id_);
    }


    // 机场静音模式(silent_mode);
    // DockSilentMode;
    dock_silent_mode_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockSilentMode(
        [this, p](const notification_center::DockSilentMode& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockSilentMode dock_silent_mode{};
            dock_silent_mode.silent_mode = msg.silent_mode;

            LOG_DEBUG("[DockImpl DockSilentMode] ListenDockSilentMode(), msg silent_mode: %d, { silent_mode: %d }",
                msg.silent_mode, dock_silent_mode.silent_mode
            );

            // Notify data;
            dock_silent_mode_callbacks_.notifyCallbackIfChanged(dock_silent_mode);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockSilentMode] ListenDockSilentMode(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_silent_mode_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockSilentMode] ListenDockSilentMode(), error dock_silent_mode_listen_id: %d",
            dock_silent_mode_listen_id_);
    }

    // 机场: 用户体验改善计划(user_experience_improvement);
    // DockUserExperienceImprovement;
    dock_user_experience_improvement_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockUserExperienceImprovement(
        [this, p](const notification_center::DockUserExperienceImprovement& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockUserExperienceImprovement dock_user_experience_improvement{};
            dock_user_experience_improvement.user_experience_improvement = msg.user_experience_improvement;

            LOG_DEBUG("[DockImpl DockUserExperienceImprovement] ListenDockUserExperienceImprovement(), msg data: { user_experience_improvement: %d }",
                msg.user_experience_improvement
            );

            // Notify data;
            dock_user_experience_improvement_callbacks_.notifyCallbackIfChanged(dock_user_experience_improvement);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockUserExperienceImprovement] ListenDockUserExperienceImprovement(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_user_experience_improvement_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockUserExperienceImprovement] ListenDockUserExperienceImprovement(), error dock_user_experience_improvement_listen_id: %d",
            dock_user_experience_improvement_listen_id_);
    }


    // DRC 链路的状态(drc_state);
    // DockDrcState;
    dock_drc_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockDrcState(
        [this, p](const notification_center::DockDrcState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockDrcState dock_drc_state{};
            dock_drc_state.drc_state = msg.drc_state;

            LOG_DEBUG("[DockImpl DockDrcState] ListenDockDrcState(), msg data: { drc_state: %d }", msg.drc_state);

            // Notify data;
            dock_drc_state_callbacks_.notifyCallbackIfChanged(dock_drc_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockDrcState] ListenDockDrcState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_drc_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockDrcState] ListenDockDrcState(), error dock_drc_state_listen_id: %d",
            dock_drc_state_listen_id_);
    }

    // 推杆状态(putter_state);
    // DockPutterState;
    dock_putter_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockPutterState(
        [this, p](const notification_center::DockPutterState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockPutterState dock_putter_state{};
            dock_putter_state.putter_state = msg.putter_state;

            LOG_DEBUG("[DockImpl DockPutterState] ListenDockPutterState(), msg data: { putter_state: %d }", msg.putter_state);

            // Notify data;
            dock_putter_state_callbacks_.notifyCallbackIfChanged(dock_putter_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockPutterState] ListenDockPutterState(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_putter_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockPutterState] ListenDockPutterState(), error dock_putter_state_listen_id: %d",
            dock_putter_state_listen_id_);
    }

    // 相对备降点(relative_alternate_land_point);
    // DockRelativeAlternateLandPoint;
    dock_relative_alternate_land_point_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockRelativeAlternateLandPoint(
        [this, p](const notification_center::DockRelativeAlternateLandPoint& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockRelativeAlternateLandPoint dock_relative_alternate_land_point{};

            dock_relative_alternate_land_point.longitude = msg.longitude;
            dock_relative_alternate_land_point.latitude = msg.latitude;
            dock_relative_alternate_land_point.safe_land_height = msg.safe_land_height;
            dock_relative_alternate_land_point.status = msg.status;

            LOG_DEBUG("[DockImpl DockRelativeAlternateLandPoint] ListenDockRelativeAlternateLandPoint(), "
                "msg data: { longitude: %f, latitude: %f, safe_land_height: %f, status: %d }",
                msg.longitude, msg.latitude, msg.safe_land_height, msg.status
            );

            // Notify data;
            dock_relative_alternate_land_point_callbacks_.notifyCallbackIfChanged(dock_relative_alternate_land_point);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockRelativeAlternateLandPoint] ListenDockRelativeAlternateLandPoint(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_relative_alternate_land_point_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockRelativeAlternateLandPoint] ListenDockRelativeAlternateLandPoint(), error dock_relative_alternate_land_point_listen_id: %d",
            dock_relative_alternate_land_point_listen_id_);
    }


    // 自收敛坐标(self_converge_coordinate);
    // DockSelfConvergeCoordinate;
    dock_self_converge_coordinate_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockSelfConvergeCoordinate(
        [this, p](const notification_center::DockSelfConvergeCoordinate& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockSelfConvergeCoordinate dock_self_converge_coordinate{};

            dock_self_converge_coordinate.height = msg.height;
            dock_self_converge_coordinate.longitude = msg.longitude;
            dock_self_converge_coordinate.latitude = msg.latitude;

            LOG_DEBUG("[DockImpl DockSelfConvergeCoordinate] ListenDockSelfConvergeCoordinate(), "
                "msg data: { height: %f, latitude: %f, longitude: %f }",
                msg.height, msg.latitude, msg.longitude
            );

            // Notify data;
            dock_self_converge_coordinate_callbacks_.notifyCallbackIfChanged(dock_self_converge_coordinate);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockSelfConvergeCoordinate] ListenDockSelfConvergeCoordinate(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_self_converge_coordinate_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockSelfConvergeCoordinate] ListenDockSelfConvergeCoordinate(), error dock_self_converge_coordinate_listen_id: %d",
            dock_self_converge_coordinate_listen_id_);
    }


    // 机场固件信息;
    // DockFirmwareInfo
    dock_firmware_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockFirmwareInfo(
        [this, p](const notification_center::DockFirmwareInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockFirmwareInfo dock_firmware_info{};

            dock_firmware_info.firmware_version = msg.firmware_version;    // 固件版本, such as "00.01.0439";
            dock_firmware_info.compatible_status = msg.compatible_status;  // 固件一致性, { 0: 一致, 1: 不一致 };
            dock_firmware_info.firmware_upgrade_status = msg.firmware_upgrade_status;   // 固件升级状态, { 0: 未升级, 1: 升级中 };

            LOG_DEBUG("[DockImpl DockFirmwareInfo] ListenDockFirmwareInfo(), "
                "msg data: { compatible_status: %d, firmware_upgrade_status: %d, firmware_version: %s }",
                msg.compatible_status,
                msg.firmware_upgrade_status,
                msg.firmware_version.c_str()
            );

            // Notify data;
            dock_firmware_info_callbacks_.notifyCallbackIfChanged(dock_firmware_info);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockFirmwareInfo] ListenDockFirmwareInfo(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_firmware_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockFirmwareInfo] ListenDockFirmwareInfo(), error dock_firmware_info_listen_id: %d",
            dock_firmware_info_listen_id_);
    }

    // 机场4G Dongle信息(dongle_infos);
    // DockDongleInfos;
    dock_dongle_infos_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockDongleInfos(
        [this, p](const notification_center::DockDongleInfos& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockDongleInfos dock_dongle_infos{};
            for (const auto& it : msg.dongle_infos) {
                flight_hub_sdk::DongleInfoItem item{};
                item.dongle_type = it.dongle_type;
                item.eid = it.eid;
                item.esim_activate_state = it.esim_activate_state;
                for (const auto& esim : it.esim_infos) {
                    flight_hub_sdk::EsimInfoItem esim_item{};
                    esim_item.enabled = esim.enabled;
                    esim_item.iccid = esim.iccid;
                    esim_item.telecom_operator = esim.telecom_operator;
                    item.esim_infos.emplace_back(esim_item);
                }
                item.imei = it.imei;
                item.sim_card_state = it.sim_card_state;
                item.sim_slot = it.sim_slot;
                item.sim_info.iccid = it.sim_info.iccid;
                item.sim_info.telecom_operator = it.sim_info.telecom_operator;
                item.telecom_operator = it.telecom_operator;
                dock_dongle_infos.dongle_infos.emplace_back(item);
            }

            LOG_DEBUG("[DockImpl DockDongleInfos] ListenDockDongleInfos(), dongle_infos size: %d",
                (int)dock_dongle_infos.dongle_infos.size());

            // Notify data;
            dock_dongle_infos_callbacks_.notifyCallbackIfChanged(dock_dongle_infos);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockDongleInfos] ListenDockDongleInfos(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_dongle_infos_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockDongleInfos] ListenDockDongleInfos(), error dock_dongle_infos_listen_id: %d",
            dock_dongle_infos_listen_id_);
    }

    // 机场空调工作模式(air_conditioner_mode);
    // DockAirConditionerMode;
    dock_air_conditioner_mode_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockAirConditionerMode(
        [this, p](const notification_center::DockAirConditionerMode& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::DockAirConditionerMode dock_air_conditioner_mode{};
            dock_air_conditioner_mode.air_conditioner_mode = msg.air_conditioner_mode;

            LOG_DEBUG("[DockImpl DockAirConditionerMode] ListenDockAirConditionerMode(), { air_conditioner_mode: %lld }",
                (long long)msg.air_conditioner_mode);

            // Notify data;
            dock_air_conditioner_mode_callbacks_.notifyCallbackIfChanged(dock_air_conditioner_mode);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl DockAirConditionerMode] ListenDockAirConditionerMode(), error: %d", error_code);
            }
        },
        dock_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    if (dock_air_conditioner_mode_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl DockAirConditionerMode] ListenDockAirConditionerMode(), error dock_air_conditioner_mode_listen_id: %d",
            dock_air_conditioner_mode_listen_id_);
    }
}

DockOsdModuleImpl::~DockOsdModuleImpl() {
    if (dock_location_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_location_listen_id_);
    }

    if (drone_in_dock_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_in_dock_listen_id_);
    }

    if (dock_cover_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_cover_state_listen_id_);
        dock_cover_state_listen_id_ = -1;
    }

    if (dock_drone_charge_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_drone_charge_state_listen_id_);
        dock_drone_charge_state_listen_id_ = -1;
    }

    if (dock_meteorological_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_meteorological_info_listen_id_);
        dock_meteorological_info_listen_id_ = -1;
    }

    if (dock_backup_battery_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_backup_battery_info_listen_id_);
        dock_backup_battery_info_listen_id_ = -1;
    }

    if (dock_maintain_status_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_maintain_status_info_listen_id_);
        dock_maintain_status_info_listen_id_ = -1;
    }
    if (dock_mode_code_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_mode_code_listen_id_);
        dock_mode_code_listen_id_ = -1;
    }

    if (flight_task_step_code_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(flight_task_step_code_listen_id_);
        flight_task_step_code_listen_id_ = -1;
    }

    if (dock_sub_device_info_list_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_sub_device_info_list_listen_id_);
        dock_sub_device_info_list_listen_id_ = -1;
    }

    if (dock_network_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_network_state_listen_id_);
        dock_network_state_listen_id_ = -1;
    }

    if (dock_media_file_detail_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_media_file_detail_listen_id_);
        dock_media_file_detail_listen_id_ = -1;
    }

    if (dock_wireless_link_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_wireless_link_listen_id_);
        dock_wireless_link_listen_id_ = -1;
    }

    if (dock_job_number_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_job_number_listen_id_);
        dock_job_number_listen_id_ = -1;
    }

    if (dock_power_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_power_state_listen_id_);
        dock_power_state_listen_id_ = -1;
    }

    if (dock_storage_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_storage_state_listen_id_);
        dock_storage_state_listen_id_ = -1;
    }
    
    if (dock_first_power_on_time_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_first_power_on_time_listen_id_);
        dock_first_power_on_time_listen_id_ = -1;
    }

    if (dock_acc_time_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_acc_time_listen_id_);
        dock_acc_time_listen_id_ = -1;
    }

    if (dock_alternate_land_point_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_alternate_land_point_listen_id_);
        dock_alternate_land_point_listen_id_ = -1;
    }

    if (dock_position_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_position_state_listen_id_);
        dock_position_state_listen_id_ = -1;
    }

    if (dock_activation_time_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_activation_time_listen_id_);
        dock_activation_time_listen_id_ = -1;
    }

    if (dock_battery_store_mode_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_battery_store_mode_listen_id_);
        dock_battery_store_mode_listen_id_ = -1;
    }

    if (dock_alarm_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_alarm_state_listen_id_);
        dock_alarm_state_listen_id_ = -1;
    }

    if (dock_drone_battery_maintenance_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_drone_battery_maintenance_info_listen_id_);
        dock_drone_battery_maintenance_info_listen_id_ = -1;
    }

    if (dock_emergency_stop_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_emergency_stop_state_listen_id_);
        dock_emergency_stop_state_listen_id_ = -1;
    }

    if (dock_silent_mode_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_silent_mode_listen_id_);
        dock_silent_mode_listen_id_ = -1;
    }
    
    if (dock_user_experience_improvement_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_user_experience_improvement_listen_id_);
        dock_user_experience_improvement_listen_id_ = -1;
    }

    if (dock_drc_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_drc_state_listen_id_);
        dock_drc_state_listen_id_ = -1;
    }
    
    if (dock_putter_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_putter_state_listen_id_);
        dock_putter_state_listen_id_ = -1;
    }

    if (dock_relative_alternate_land_point_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_relative_alternate_land_point_listen_id_);
        dock_relative_alternate_land_point_listen_id_ = -1;
    }

    if (dock_self_converge_coordinate_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_self_converge_coordinate_listen_id_);
        dock_self_converge_coordinate_listen_id_ = -1;
    }

    // 机场固件信息;
    // DockFirmwareInfo
    if (dock_firmware_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_firmware_info_listen_id_);
        dock_firmware_info_listen_id_ = -1;
    }

    // 机场4G Dongle信息;
    if (dock_dongle_infos_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_dongle_infos_listen_id_);
        dock_dongle_infos_listen_id_ = -1;
    }

    // 机场空调工作模式;
    if (dock_air_conditioner_mode_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_air_conditioner_mode_listen_id_);
        dock_air_conditioner_mode_listen_id_ = -1;
    }
}

int DockOsdModuleImpl::listenDockLocation(const std::function<void(const DockLocation& msg)>& callback) {
    return dock_location_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockLocation(int listen_id) {
    dock_location_callbacks_.removeCallback(listen_id);
}

int DockOsdModuleImpl::listenDroneInDock(const std::function<void(const DroneInDock& msg)>& callback) {
    return drone_in_dock_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDroneInDock(int listen_id) {
    drone_in_dock_callbacks_.removeCallback(listen_id);
}

// 监听机场舱盖状态;
int DockOsdModuleImpl::listenDockCoverState(const std::function<void(const DockCoverState &msg)> & callback) {
    return dock_cover_state_callbacks_.addCallback(callback);
}

// 取消监听机场舱盖状态;
void DockOsdModuleImpl::cancelListenDockCoverState(int listen_id) {
    dock_cover_state_callbacks_.removeCallback(listen_id);
}

// 监听机场飞机充电状态;
int DockOsdModuleImpl::listenDockDroneChargeState(const std::function<void(const DockDroneChargeState &msg)> & callback) {
    return dock_drone_charge_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockDroneChargeState(int listen_id) {
    dock_drone_charge_state_callbacks_.removeCallback(listen_id);
}

// 监听机场气象信息;
int DockOsdModuleImpl::listenDockMeteorologicalInfo(const std::function<void(const DockMeteorologicalInfo &msg)> & callback) {
    return dock_meteorological_info_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockMeteorologicalInfo(int listen_id) {
    dock_meteorological_info_callbacks_.removeCallback(listen_id);
}

// 监听机场气象信息;
int DockOsdModuleImpl::listenDockBackupBatteryInfo(const std::function<void(const DockBackupBatteryInfo &msg)> & callback) {
    return dock_backup_battery_info_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockBackupBatteryInfo(int listen_id) {
    dock_backup_battery_info_callbacks_.removeCallback(listen_id);
}

// 监听机场保养信息;
int DockOsdModuleImpl::listenDockMaintainStatusInfo(const std::function<void(const DockMaintainStatusInfo &msg)> & callback) {
    return dock_maintain_status_info_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockMaintainStatusInfo(int listen_id) {
    dock_maintain_status_info_callbacks_.removeCallback(listen_id);
}


// 机场 mode code;
int DockOsdModuleImpl::listenDockModeCode(const std::function<void(const DockModeCode &msg)>& callback) {
    return dock_mode_code_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockModeCode(int listen_id) {
    dock_mode_code_callbacks_.removeCallback(listen_id);
}

// 飞行任务 step code;
int DockOsdModuleImpl::listenFlightTaskStepCode(const std::function<void(const FlightTaskStepCode &msg)>& callback) {
    return flight_task_step_code_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenFlightTaskStepCode(int listen_id) {
    flight_task_step_code_callbacks_.removeCallback(listen_id);
}

// 机场: 子设备信息列表(sub_device);
int DockOsdModuleImpl::listenDockSubDevice(const std::function<void(const DockSubDeviceInfoList &msg)>& callback) {
    return dock_sub_device_info_list_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockSubDevice(int listen_id) {
    dock_sub_device_info_list_callbacks_.removeCallback(listen_id);
}


// 机场: 网络状态(network_state);
int DockOsdModuleImpl::listenDockNetworkState(const std::function<void(const DockNetworkState &msg)>& callback) {
    return dock_network_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockNetworkState(int listen_id) {
    dock_network_state_callbacks_.removeCallback(listen_id);
}


// 机场: 媒体文件数量(media_file_detail);
int DockOsdModuleImpl::listenDockMediaFileDetail(const std::function<void(const DockMediaFileDetail &msg)>& callback) {
    return dock_media_file_detail_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockMediaFileDetail(int listen_id) {
    dock_media_file_detail_callbacks_.removeCallback(listen_id);
}

// 机场: 图传质量(wireless_link);
int DockOsdModuleImpl::listenDockWirelessLink(const std::function<void(const DockWirelessLink &msg)>& callback) {
    return dock_wireless_link_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockWirelessLink(int listen_id) {
    dock_wireless_link_callbacks_.removeCallback(listen_id);
}


// 机场: 机场作业次数(job_number);
int DockOsdModuleImpl::listenDockJobNumber(const std::function<void(const DockJobNumber &msg)>& callback) {
    return dock_job_number_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockJobNumber(int listen_id) {
    dock_job_number_callbacks_.removeCallback(listen_id);
}


// 机场: DockPowerState;
int DockOsdModuleImpl::listenDockPowerState(const std::function<void(const DockPowerState &msg)>& callback) {
    return dock_power_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockPowerState(int listen_id) {
    dock_power_state_callbacks_.removeCallback(listen_id);
}

// 机场: 存储容量(storage);
int DockOsdModuleImpl::listenDockStorageState(const std::function<void(const DockStorageState &msg)>& callback) {
    return dock_storage_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockStorageState(int listen_id) {
    dock_storage_state_callbacks_.removeCallback(listen_id);
}

// 机场: 首次上电时间(first_power_on);
int DockOsdModuleImpl::listenDockFirstPowerOnTime(const std::function<void(const DockFirstPowerOnTime &msg)>& callback) {
    return dock_first_power_on_time_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockFirstPowerOnTime(int listen_id) {
    dock_first_power_on_time_callbacks_.removeCallback(listen_id);
}

// 机场累计运行时长(acc_time);
int DockOsdModuleImpl::listenDockAccumulatedRunningTime(const std::function<void(const DockAccTime &msg)>& callback) {
    return dock_acc_time_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockAccumulatedRunningTime(int listen_id) {
    dock_acc_time_callbacks_.removeCallback(listen_id);
}

// 机场: 备降点(alternate_land_point);
int DockOsdModuleImpl::listenDockAlternateLandPoint(const std::function<void(const DockAlternateLandPoint &msg)>& callback) {
    return dock_alternate_land_point_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockAlternateLandPoint(int listen_id) {
    dock_alternate_land_point_callbacks_.removeCallback(listen_id);
}

// 搜星状态(position_state);
int DockOsdModuleImpl::listenDockPositionState(const std::function<void(const DockPositionState &msg)>& callback) {
    return dock_position_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockPositionState(int listen_id) {
    dock_position_state_callbacks_.removeCallback(listen_id);
}

// 机场激活时间(activation_time, unix时间戳 s);
int DockOsdModuleImpl::listenDockActivationTime(const std::function<void(const DockActivationTime &msg)>& callback) {
    return dock_activation_time_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockActivationTime(int listen_id) {
    dock_activation_time_callbacks_.removeCallback(listen_id);
}

// 电池(存储)模式(battery_store_mode);
int DockOsdModuleImpl::listenDockBatteryStoreMode(const std::function<void(const DockBatteryStoreMode &msg)>& callback) {
    return dock_battery_store_mode_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockBatteryStoreMode(int listen_id) {
    dock_battery_store_mode_callbacks_.removeCallback(listen_id);
}


// 机场: 声光报警状态(alarm_state);
int DockOsdModuleImpl::listenDockAlarmState(const std::function<void(const DockAlarmState &msg)>& callback) {
    return dock_alarm_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockAlarmState(int listen_id) {
    dock_alarm_state_callbacks_.removeCallback(listen_id);
}


// 机场: 飞行器电池保养信息(drone_battery_maintenance_info);
// DockDroneBatteryMaintenanceInfo;
int DockOsdModuleImpl::listenDockDroneBatteryMaintenanceInfo(const std::function<void(const DockDroneBatteryMaintenanceInfo &msg)>& callback) {
    return dock_drone_battery_maintenance_info_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockDroneBatteryMaintenanceInfo(int listen_id) {
    dock_drone_battery_maintenance_info_callbacks_.removeCallback(listen_id);
}

// 紧急停止按钮状态(emergency_stop_state);
// DockEmergencyStopState;
int DockOsdModuleImpl::listenDockEmergencyStopState(const std::function<void(const DockEmergencyStopState &msg)>& callback) {
    return dock_emergency_stop_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockEmergencyStopState(int listen_id) {
    dock_emergency_stop_state_callbacks_.removeCallback(listen_id);
}


// 机场: 机场静音模式(silent_mode);
// DockSilentMode;
int DockOsdModuleImpl::listenDockSilentMode(const std::function<void(const DockSilentMode &msg)>& callback) {
    return dock_silent_mode_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockSilentMode(int listen_id) {
    dock_silent_mode_callbacks_.removeCallback(listen_id);
}

// 机场: 用户体验改善计划(user_experience_improvement);
// DockUserExperienceImprovement;
int DockOsdModuleImpl::listenDockUserExperienceImprovement(const std::function<void(const DockUserExperienceImprovement &msg)>& callback) {
    return dock_user_experience_improvement_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockUserExperienceImprovement(int listen_id) {
    dock_user_experience_improvement_callbacks_.removeCallback(listen_id);
}

// 机场: DRC 链路的状态(drc_state);
// DockDrcState;
int DockOsdModuleImpl::listenDockDrcState(const std::function<void(const DockDrcState &msg)>& callback) {
    return dock_drc_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockDrcState(int listen_id) {
    dock_drc_state_callbacks_.removeCallback(listen_id);
}


// 机场: 推杆状态(putter_state);
// DockPutterState;
int DockOsdModuleImpl::listenDockPutterState(const std::function<void(const DockPutterState &msg)>& callback) {
    return dock_putter_state_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockPutterState(int listen_id) {
    dock_putter_state_callbacks_.removeCallback(listen_id);
}

// 相对备降点(relative_alternate_land_point);
// DockRelativeAlternateLandPoint;
int DockOsdModuleImpl::listenDockRelativeAlternateLandPoint(const std::function<void(const DockRelativeAlternateLandPoint &msg)>& callback) {
    return dock_relative_alternate_land_point_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockRelativeAlternateLandPoint(int listen_id) {
    dock_relative_alternate_land_point_callbacks_.removeCallback(listen_id);
}

// 自收敛坐标(self_converge_coordinate);
// DockSelfConvergeCoordinate;
int DockOsdModuleImpl::listenDockSelfConvergeCoordinate(const std::function<void(const DockSelfConvergeCoordinate &msg)>& callback) {
    return dock_self_converge_coordinate_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockSelfConvergeCoordinate(int listen_id) {
    dock_self_converge_coordinate_callbacks_.removeCallback(listen_id);
}


// 机场固件信息;
// DockFirmwareInfo
int DockOsdModuleImpl::listenDockFirmwareInfo(const std::function<void(const DockFirmwareInfo &msg)>& callback) {
    return dock_firmware_info_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockFirmwareInfo(int listen_id) {
    dock_firmware_info_callbacks_.removeCallback(listen_id);
}

// 机场4G Dongle信息(dongle_infos);
// DockDongleInfos;
int DockOsdModuleImpl::listenDockDongleInfos(const std::function<void(const DockDongleInfos &msg)>& callback) {
    return dock_dongle_infos_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockDongleInfos(int listen_id) {
    dock_dongle_infos_callbacks_.removeCallback(listen_id);
}

// 机场空调工作模式(air_conditioner_mode);
// DockAirConditionerMode;
int DockOsdModuleImpl::listenDockAirConditionerMode(const std::function<void(const DockAirConditionerMode &msg)>& callback) {
    return dock_air_conditioner_mode_callbacks_.addCallback(callback);
}

void DockOsdModuleImpl::cancelListenDockAirConditionerMode(int listen_id) {
    dock_air_conditioner_mode_callbacks_.removeCallback(listen_id);
}

