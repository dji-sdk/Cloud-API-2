// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "dock_impl.h"
#include "bll/module/live_module/live_module_impl.h"
#include "bll/module/live_record_module/live_record_module_impl.h"
#include "bll/module/rtk_calibration_module/rtk_calibration_module_impl.h"
#include "bll/module/network_test_module/network_test_module_impl.h"
#include "bll/module/device_state_module/device_state_module_impl.h"
#include "bll/module/device_hms_module/device_hms_module_impl.h"
#include "bll/module/live_device_module/live_device_module_impl.h"
#include "bll/module/deploy_module/deploy_module_impl.h"
#include "bll/module/dock_control_module/dock_control_module_impl.h"
#include "bll/module/dock_osd_module/dock_osd_module_impl.h"
#include "message_engine_factory.h"
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "bll/tool/flight_hub_utils.h"
#include "bll/manager/topo_registry/topo_registry.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
using namespace flight_hub_sdk;
using namespace notification_center;

DockImpl::DockImpl(
    const std::string& sn,
    const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge,
    const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge)
    : sn_(sn),
      rtc_channel_manager_bridge_(rtc_channel_manager_bridge),
      mqtt_client_manager_bridge_(mqtt_client_manager_bridge) {
    LOG_INFO("DockImpl: sn=%s", sn.c_str());
    // TODO: 初始化
    message_engine_ = message_engine::MessageEngineFactory::createMessageEngine(drc_sdk::common::Executors::Main());
    if (!message_engine_) {
        LOG_ERROR("DockImpl: createMessageEngine failed, SN: %s", sn.c_str());
        LOG_ERROR("DockImpl: 程序将调用abort()终止，这可能导致服务退出");
        LOG_ERROR("DockImpl: 请检查日志文件以获取更多信息");
        abort();
    }
    if (rtc_channel_manager_bridge_) {
        message_engine_->setRtcChannelManager(rtc_channel_manager_bridge_);
    }
    if (mqtt_client_manager_bridge_) {
        message_engine_->setMqttClientManager(mqtt_client_manager_bridge_);
    }
    message_engine_->initialize();

    device_state_module_ = std::make_shared<DeviceStateModuleImpl>(DeviceKind::Dock, sn_);
    device_hms_module_ = std::make_shared<DeviceHMSModuleImpl>(DeviceKind::Dock, sn_);

    dock_osd_module_ = std::make_shared<DockOsdModuleImpl>(sn_);

    WeakDummy(p);


    // eager 创建 DeployModule（含 6 路 NC 注册）
    deploy_module_ = std::make_shared<DeployModuleImpl>(sn_);

    // eager 创建 DockControlModule（含环控 NC 注册）
    dock_control_module_ = std::make_shared<DockControlModuleImpl>(sn_);

    // 监听直播录制状态（topic_live_record_info）
    dock_live_record_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenLiveRecordInfo(
        [this, p](const notification_center::LiveRecordInfo& msg) {
            WeakDummyReturn(p);
            LiveRecordStateInfo info;
            info.bid = msg.bid;
            info.sn = msg.sn;
            info.camera = msg.camera;
            info.video = msg.video;
            info.state = msg.state;
            info.created_by = msg.created_by;
            info.duration = msg.duration;
            info.create_time = msg.create_time;
            drc_sdk::common::Executors::Main()->Post([this, p, info]() {
                WeakDummyReturn(p);
                // 若 NC 按 device_sn 过滤则通常仅一条；若 NC 推送全量，则按 sn/camera/video 与各 module 绑定的 live_info 匹配后再通知，减少无关回调
                std::lock_guard<std::mutex> lock(live_module_mutex_);
                for (auto& kv : live_record_module_map_) {
                    if (!kv.second) continue;
                    auto* impl = dynamic_cast<LiveRecordModuleImpl*>(kv.second.get());
                    if (!impl) {
                        kv.second->notifyRecordState(info);
                        continue;
                    }
                    const LiveInfo& li = impl->getLiveInfo();
                    if (info.sn == li.device_sn && info.camera == li.payload_index && info.video == li.video_id) {
                        kv.second->notifyRecordState(info);
                    }
                }
            });
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockImpl] ListenLiveRecordInfo, error: %d", error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);
    if (dock_live_record_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockImpl] ListenLiveRecordInfo, error: %d", dock_live_record_info_listen_id_);
    }


}

DockImpl::~DockImpl() {
    LOG_INFO("DockImpl: ~DockImpl");

    for (const auto& [payload_index, live_module] : live_module_map_) {
        live_module->stop();
    }
    live_module_map_.clear();
    live_record_module_map_.clear();

    if (dock_live_record_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_live_record_info_listen_id_);
        dock_live_record_info_listen_id_ = -1;
    }

    // 机场固件信息;
    // DockFirmwareInfo
    if (message_engine_) {
        message_engine_->destroy();
    }
    message_engine::MessageEngineFactory::destroyMessageEngine(message_engine_);
}

std::shared_ptr<ILiveModule> DockImpl::getLiveStream(const LiveInfo& params) {
    std::lock_guard<std::mutex> lock(live_module_mutex_);
    const std::string key = params.payload_index + ":" + params.video_id;
    if (live_module_map_.count(key) > 0) {
        return live_module_map_[key];
    }
    const auto gateway_sn = TopoRegistry::getInstance().resolveGatewaySn(sn_).value_or(sn_);
    auto live_module = std::make_shared<LiveModuleImpl>(
        params, message_engine_->getRtcChannelManager(), gateway_sn);
    live_module_map_[key] = live_module;
    return live_module;
}

std::shared_ptr<ILiveRecordModule> DockImpl::getLiveRecord(const LiveInfo& params) {
    std::lock_guard<std::mutex> lock(live_module_mutex_);
    std::string key = params.payload_index + ":" + params.video_id;
    auto it = live_record_module_map_.find(key);
    if (it != live_record_module_map_.end()) {
        return it->second;
    }
    auto module = std::make_shared<LiveRecordModuleImpl>(params);
    live_record_module_map_[key] = module;
    return module;
}

std::shared_ptr<ILiveDeviceModule> DockImpl::getLiveDeviceModule() {
    if (!live_device_module_) {
        live_device_module_ = std::make_shared<LiveDeviceModuleImpl>(DeviceKind::Dock, sn_);
    }
    return live_device_module_;
}

std::shared_ptr<IDeployModule> DockImpl::getDeployModule() {
    return deploy_module_;
}


std::shared_ptr<IDockOsdModule> DockImpl::getDockOsdModule() {
    return dock_osd_module_;
}

std::shared_ptr<IDockControlModule> DockImpl::getDockControlModule() {
    return dock_control_module_;
}

std::shared_ptr<IRtkCalibrationModule> DockImpl::getRtkCalibrationModule() {
    if (!rtk_calibration_module_) {
        rtk_calibration_module_ = std::make_shared<RtkCalibrationModuleImpl>(sn_, "3");
    }
    return rtk_calibration_module_;
}

std::shared_ptr<INetworkTestModule> DockImpl::getNetworkTestModule() {
    if (!network_test_module_) {
        network_test_module_ = std::make_shared<NetworkTestModuleImpl>(sn_);
    }
    return network_test_module_;
}

std::shared_ptr<IDeviceDebugModule> DockImpl::getDeviceDebugModule() {
    if (!device_debug_module_) {
        device_debug_module_ = std::make_shared<DeviceDebugModuleImpl>();
    }
    return device_debug_module_;
}

std::shared_ptr<IDeviceStateModule> DockImpl::getDeviceStateModule() {
    return device_state_module_;
}

std::shared_ptr<IDeviceHMSModule> DockImpl::getDeviceHMSModule() {
    return device_hms_module_;
}

