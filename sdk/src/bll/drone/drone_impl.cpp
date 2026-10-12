// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drone_impl.h"
#include "bll/module/flight_task_control_module/flight_task_control_module.h"
#include "bll/module/live_module/live_module_impl.h"
#include "bll/module/live_record_module/live_record_module_impl.h"
#include "bll/module/fly_to_point_module/fly_to_point_module_impl.h"
#include "bll/module/poi_module/poi_module_impl.h"
#include "bll/module/in_flight_wayline_module/in_flight_wayline_module_impl.h"
#include "bll/module/device_state_module/device_state_module_impl.h"
#include "bll/module/device_hms_module/device_hms_module_impl.h"
#include "bll/module/live_device_module/live_device_module_impl.h"
#include "bll/module/takeoff_module/takeoff_module_impl.h"
#include "bll/module/flight_control_module/flight_control_module_impl.h"
#include "bll/module/control_auth_module/control_auth_module_impl.h"
#include "bll/module/drc_module/drc_module_impl.h"
#include "bll/module/flight_settings_module/flight_settings_module_impl.h"
#include "bll/module/test_flight_module/test_flight_module_impl.h"
#include "bll/module/onboard_ai_module/onboard_ai_module_impl.h"
#include "bll/module/drone_osd_module/drone_osd_module_impl.h"
#include "bll/http_interface/http_interface.h"
#include "bll/manager/device_capability_mgr/device_capability_mgr.h"
#include "bll/manager/topo_registry/topo_registry.h"
#include "message_engine_factory.h"
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "bll/tool/flight_hub_utils.h"
#include <atomic>

using namespace flight_hub_sdk;
using namespace notification_center;

static std::atomic<int> s_drone_impl_instance_id{1};

DroneImpl::DroneImpl(
    DeviceTopo device_topo,
    const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge,
    const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge)
    : device_topo_(std::move(device_topo)),
      rtc_channel_manager_bridge_(rtc_channel_manager_bridge),
      mqtt_client_manager_bridge_(mqtt_client_manager_bridge) {
    if (device_topo_.sub_devices.empty()) {
        LOG_ERROR("DroneImpl: sub_devices is empty");
        return;
    }
    drone_sn_ = device_topo_.sub_devices[0].device_sn;
    drone_model_key_ = device_topo_.sub_devices[0].model_key;
    dock_sn_ = device_topo_.device.device_sn;
    instance_id_ = s_drone_impl_instance_id.fetch_add(1);
    log_tag_ = drone_sn_ + "#" + std::to_string(instance_id_);

    LOG_INFO("DroneImpl: instance_id=%d, log_tag=%s, dock_sn=%s, drone_sn=%s, drone_model_key=%s",
        instance_id_, log_tag_.c_str(), dock_sn_.c_str(), drone_sn_.c_str(), drone_model_key_.c_str()
    );

    /// init message engine
    message_engine_ = message_engine::MessageEngineFactory::createMessageEngine(drc_sdk::common::Executors::Main());
    if (!message_engine_) {
        LOG_ERROR("DroneImpl: createMessageEngine failed, Drone SN: %s, Dock SN: %s", 
                  log_tag_.c_str(), dock_sn_.c_str());
        LOG_ERROR("DroneImpl: 程序将调用abort()终止，这可能导致服务退出");
        LOG_ERROR("DroneImpl: 请检查日志文件以获取更多信息");
        abort();
    }
    if (rtc_channel_manager_bridge_) {
        message_engine_->setRtcChannelManager(rtc_channel_manager_bridge_);
    }
    if (mqtt_client_manager_bridge_) {
        message_engine_->setMqttClientManager(mqtt_client_manager_bridge_);
    }
    message_engine_->initialize();

    device_state_module_ = std::make_shared<DeviceStateModuleImpl>(DeviceKind::Drone, drone_sn_);
    device_hms_module_ = std::make_shared<DeviceHMSModuleImpl>(DeviceKind::Drone, drone_sn_);
    takeoff_module_ = std::make_shared<TakeoffModuleImpl>(drone_sn_, dock_sn_);
    fly_to_point_module_ = std::make_shared<FlyToPointModuleImpl>(message_engine_, drone_sn_, getGatewaySn());
    poi_module_ = std::make_shared<PoiModuleImpl>(message_engine_, drone_sn_, getGatewaySn(), drone_model_key_);
    in_flight_wayline_module_ = std::make_shared<InFlightWaylineModuleImpl>(getGatewaySn(), drone_sn_);
    flight_task_control_module_ = std::make_shared<FlightTaskControlModuleImpl>(message_engine_, drone_sn_, getGatewaySn());

    /// init manager
    initDrcLinkMgr();
    drc_module_ = std::make_shared<DrcModuleImpl>(drc_link_mgr_, log_tag_);
    initFlightCtrlMgr();
    initPayloadCtrlMgr();
    initPsdkCtrlMgr();
    initAICtrlMgr();

    /// init listen
    initListen();
    drone_osd_module_ = std::make_shared<DroneOsdModuleImpl>(
        drone_sn_, dock_sn_, log_tag_, flight_ctrl_mgr_, payload_ctrl_mgr_);
    initLiveRecordListener();
}


DroneImpl::~DroneImpl() {
    LOG_INFO("DroneImpl: ~DroneImpl");
    if (message_engine_) {
        message_engine_->destroy();
    }
    message_engine::MessageEngineFactory::destroyMessageEngine(message_engine_);
    for (const auto& [payload_index, live_module] : live_module_map_) {
        live_module->stop();
    }
    live_module_map_.clear();
    live_record_module_map_.clear();
    if (live_record_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(live_record_info_listen_id_);
        live_record_info_listen_id_ = -1;
    }
}


void DroneImpl::initDrcLinkMgr() {
    drc_link_mgr_ = std::make_shared<DrcLinkMgr>(drone_sn_, getGatewaySn(), message_engine_, instance_id_);
}

void DroneImpl::initFlightCtrlMgr() {
    flight_ctrl_mgr_ = std::make_shared<FlightCtrlMgr>(drone_sn_, getGatewaySn(), message_engine_, instance_id_);
}

void DroneImpl::initPayloadCtrlMgr() {
    payload_ctrl_mgr_ = std::make_shared<PayloadCtrlMgr>(drone_sn_, getGatewaySn(), message_engine_, instance_id_);
    
    // 注册回调，让 PayloadCtrlMgr 在状态更新后通知用户
    // 这样确保 SDK 内部状态更新先于用户回调，避免时序问题
}

void DroneImpl::initPsdkCtrlMgr() {
    psdk_ctrl_mgr_ = std::make_shared<PsdkCtrlMgr>(drone_sn_, getGatewaySn(), message_engine_);
}

void DroneImpl::initAICtrlMgr() {
    ai_ctrl_mgr_ = std::make_shared<AICtrlMgr>(drone_sn_, getGatewaySn(), message_engine_);
}

void DroneImpl::initListen() {
}

void DroneImpl::initLiveRecordListener() {
    WeakDummy(p);
    // 直播录制状态（topic_live_record_info）
    live_record_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenLiveRecordInfo(
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
                LOG_ERROR("[DroneImpl-%s] ListenLiveRecordInfo, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);
    if (live_record_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DroneImpl-%s] ListenLiveRecordInfo failed, listen_id: %d", log_tag_.c_str(), live_record_info_listen_id_);
    }
}


std::shared_ptr<ILiveModule> DroneImpl::getLiveStream(const LiveInfo& params) {
    std::lock_guard<std::mutex> lock(live_module_mutex_);
    const std::string key = params.payload_index + ":" + params.video_id;
    if (live_module_map_.count(key) > 0) {
        return live_module_map_[key];
    }
    auto live_module = std::make_shared<LiveModuleImpl>(params, message_engine_->getRtcChannelManager(), getGatewaySn());
    live_module_map_[key] = live_module;
    return live_module;
}

std::shared_ptr<ILiveRecordModule> DroneImpl::getLiveRecord(const LiveInfo& params) {
    std::lock_guard<std::mutex> lock(live_module_mutex_);
    std::string key = params.payload_index + ":" + params.video_id;
    if (live_record_module_map_.count(key) > 0) {
        return live_record_module_map_[key];
    }
    auto live_record_module = std::make_shared<LiveRecordModuleImpl>(params);
    live_record_module_map_[key] = live_record_module;
    return live_record_module;
}

std::shared_ptr<ILiveDeviceModule> DroneImpl::getLiveDeviceModule() {
    std::lock_guard<std::mutex> lock(live_device_module_mutex_);
    if (!live_device_module_) {
        live_device_module_ = std::make_shared<LiveDeviceModuleImpl>(DeviceKind::Drone, drone_sn_);
    }
    return live_device_module_;
}

std::shared_ptr<ITakeoffModule> DroneImpl::getTakeoffModule() {
    return takeoff_module_;
}

std::shared_ptr<IFlightControlModule> DroneImpl::getFlightControlModule() {
    std::lock_guard<std::mutex> lock(flight_control_module_mutex_);
    if (!flight_control_module_) {
        flight_control_module_ = std::make_shared<FlightControlModuleImpl>(
            drone_sn_, getGatewaySn(), message_engine_, flight_ctrl_mgr_);
    }
    return flight_control_module_;
}

std::shared_ptr<IControlAuthModule> DroneImpl::getControlAuthModule() {
    std::lock_guard<std::mutex> lock(control_auth_module_mutex_);
    if (!control_auth_module_) {
        control_auth_module_ = std::make_shared<ControlAuthModuleImpl>(flight_ctrl_mgr_, payload_ctrl_mgr_);
    }
    return control_auth_module_;
}

std::shared_ptr<IDrcModule> DroneImpl::getDrcModule() {
    return drc_module_;
}

std::shared_ptr<IFlightSettingsModule> DroneImpl::getFlightSettingsModule() {
    std::lock_guard<std::mutex> lock(flight_settings_module_mutex_);
    if (!flight_settings_module_) {
        // 传入 gateway_sn（网关 SN）：
        // 后端 /manage/api/v1/flight/configs/safe 接口的 ?sn=xxx 参数期望网关 SN，
        // 传统机场场景 gateway_sn = dock_sn；网联机型 gateway_sn = drone_sn。
        flight_settings_module_ = std::make_shared<FlightSettingsModuleImpl>(getGatewaySn());
    }
    return flight_settings_module_;
}

std::shared_ptr<ITestFlightModule> DroneImpl::getTestFlightModule() {
    std::lock_guard<std::mutex> lock(test_flight_module_mutex_);
    if (!test_flight_module_) {
        test_flight_module_ = std::make_shared<TestFlightModuleImpl>();
    }
    return test_flight_module_;
}

std::shared_ptr<IOnboardAiModule> DroneImpl::getOnboardAiModule() {
    std::lock_guard<std::mutex> lock(onboard_ai_module_mutex_);
    if (!onboard_ai_module_) {
        onboard_ai_module_ = std::make_shared<OnboardAiModuleImpl>(ai_ctrl_mgr_);
    }
    return onboard_ai_module_;
}


std::shared_ptr<IPsdkControlModule> DroneImpl::getPsdkControlModule() {
    std::lock_guard<std::mutex> lock(psdk_control_module_mutex_);
    if (!psdk_control_module_) {
        psdk_control_module_ = std::make_shared<PsdkControlModuleImpl>(psdk_ctrl_mgr_);
    }
    return psdk_control_module_;
}

std::shared_ptr<ICameraControlModule> DroneImpl::getCameraControlModule() {
    std::lock_guard<std::mutex> lock(camera_control_module_mutex_);
    if (!camera_control_module_) {
        camera_control_module_ = std::make_shared<CameraControlModuleImpl>(payload_ctrl_mgr_);
    }
    return camera_control_module_;
}
std::shared_ptr<IDroneOsdModule> DroneImpl::getDroneOsdModule() {
    return drone_osd_module_;
}

// 智能低光拍照
// 录像
// 框选变焦
// 变焦;
// 联动变焦;
// 负载控制: 重置云台(method: gimbal_reset);
// 看向目标
// (相机)负载控制: 长按画面拖动(method: camera_screen_drag);
// 云台拖拽: drc_camera_screen_drag
// 全景
// 照片分辨率设置
// 定时拍
// 双击AIM
// 切换相机模式
// 用 std::vector<int> 替换 std::vector<CameraMode>, 避免跨平台编译报错(比如 go 编译);
std::shared_ptr<IFlyToPointModule> DroneImpl::getFlyToPointModule() {
    return fly_to_point_module_;
}

std::shared_ptr<IPoiModule> DroneImpl::getPoiModule() {
    return poi_module_;
}

std::shared_ptr<IInFlightWaylineModule> DroneImpl::getInFlightWaylineModule() {
    return in_flight_wayline_module_;
}

std::shared_ptr<IDeviceStateModule> DroneImpl::getDeviceStateModule() {
    return device_state_module_;
}

std::shared_ptr<IDeviceHMSModule> DroneImpl::getDeviceHMSModule() {
    return device_hms_module_;
}

std::shared_ptr<IDeviceDebugModule> DroneImpl::getDeviceDebugModule() {
    std::lock_guard<std::mutex> lock(device_debug_module_mutex_);
    if (!device_debug_module_) {
        device_debug_module_ = std::make_shared<DeviceDebugModuleImpl>();
    }
    return device_debug_module_;
}

std::shared_ptr<IAIBoxModule> DroneImpl::getAIBoxModule() {
    std::lock_guard<std::mutex> lock(ai_box_module_mutex_);
    if (!ai_box_module_) {
        ai_box_module_ = std::make_shared<AIBoxModuleImpl>();
    }
    return ai_box_module_;
}

std::shared_ptr<IFlightTaskControlModule> DroneImpl::getFlightTaskControlModule() {
    return flight_task_control_module_;
}

namespace {
/// 网联飞机（如 EA340）无机场/基站作为网关，gateway_sn 使用飞机 SN。后端设备列表返回的机型 key。
const char* kNetworkDroneModelKey = "0-130-0";  // EA340，若后续有更多网联机型可改为 set 或列表判断
}  // namespace

std::string DroneImpl::getGatewaySn() {
    const auto resolved = TopoRegistry::getInstance().resolveGatewaySn(drone_sn_);
    if (resolved.has_value() && !resolved->empty()) {
        return *resolved;
    }
    if (!device_topo_.sub_devices.empty() && device_topo_.sub_devices[0].model_key == kNetworkDroneModelKey) {
        LOG_DEBUG("[DroneImpl] getGatewaySn: fallback 网联机型, sn=%s", drone_sn_.c_str());
        return drone_sn_;
    }
    return dock_sn_;
}

// 红外增益模式
