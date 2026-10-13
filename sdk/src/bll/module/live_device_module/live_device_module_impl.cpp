// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "live_device_module_impl.h"
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "bll/tool/flight_hub_utils.h"
#include "bll/manager/topo_registry/topo_registry.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;

LiveDeviceModuleImpl::LiveDeviceModuleImpl(DeviceKind kind, const std::string& sn)
    : kind_(kind), sn_(sn) {
    registerNotificationCenterListeners();
}

LiveDeviceModuleImpl::~LiveDeviceModuleImpl() {
    get_live_available_range_timer_.Cancel();

    if (dock_live_status_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_live_status_listen_id_);
        dock_live_status_listen_id_ = -1;
    }
    if (dock_live_capacity_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_live_capacity_listen_id_);
        dock_live_capacity_listen_id_ = -1;
    }
}

void LiveDeviceModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);
    // 机场直播状态(live_status);
    dock_live_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockLiveStatus(
        [this, p](const notification_center::DockLiveStatus& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::DockLiveStatus dock_live_status{};
            for (const auto& it : msg.live_status) {
                flight_hub_sdk::LiveStatusItem item{};
                item.video_id = it.video_id;
                item.video_type = it.video_type;
                item.video_quality = it.video_quality;
                item.status = it.status;
                item.error_status = it.error_status;
                dock_live_status.live_status.emplace_back(item);
            }
            LOG_DEBUG("[LiveDeviceModuleImpl] ListenDockLiveStatus, sn: %s, live_status size: %d",
                sn_.c_str(), (int)dock_live_status.live_status.size());
            dock_live_status_callbacks_.notifyCallbackIfChanged(dock_live_status);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[LiveDeviceModuleImpl] ListenDockLiveStatus, error: %d", error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);

    // 机场直播能力(live_capacity, WS 推送; 与 listenLiveAvailableRange 5s 轮询并存, 互不影响);
    dock_live_capacity_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockLiveCapacity(
        [this, p](const notification_center::DockLiveCapacity& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::DockLiveCapacity dock_live_capacity{};
            dock_live_capacity.available_video_number = msg.available_video_number;
            dock_live_capacity.coexist_video_number_max = msg.coexist_video_number_max;
            for (const auto& device_item : msg.device_list) {
                flight_hub_sdk::LiveCapacityDevice device{};
                device.sn = device_item.sn;
                device.available_video_number = device_item.available_video_number;
                device.coexist_video_number_max = device_item.coexist_video_number_max;
                for (const auto& camera_item : device_item.camera_list) {
                    flight_hub_sdk::LiveCapacityCamera camera{};
                    camera.camera_index = camera_item.camera_index;
                    camera.available_video_number = camera_item.available_video_number;
                    camera.coexist_video_number_max = camera_item.coexist_video_number_max;
                    for (const auto& video_item : camera_item.video_list) {
                        flight_hub_sdk::LiveCapacityVideo video{};
                        video.video_index = video_item.video_index;
                        video.video_type = video_item.video_type;
                        video.switchable_video_types = video_item.switchable_video_types;
                        camera.video_list.emplace_back(video);
                    }
                    device.camera_list.emplace_back(camera);
                }
                dock_live_capacity.device_list.emplace_back(device);
            }
            LOG_DEBUG("[LiveDeviceModuleImpl] ListenDockLiveCapacity, sn: %s, device_list size: %d",
                sn_.c_str(), (int)dock_live_capacity.device_list.size());
            dock_live_capacity_callbacks_.notifyCallbackIfChanged(dock_live_capacity);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[LiveDeviceModuleImpl] ListenDockLiveCapacity, error: %d", error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);
}

int LiveDeviceModuleImpl::listenLiveAvailableRange(
    const std::function<void(const std::vector<LiveInfo>&)>& callback) {
    if (kind_ == DeviceKind::Dock) {
        LOG_INFO("LiveDeviceModuleImpl: listenLiveAvailableRange (Dock), sn=%s", sn_.c_str());
    }
    int listen_id = live_info_list_callbacks_.addCallback(callback);
    WeakDummy(p);
    get_live_available_range_timer_.Cancel();
    drc_sdk::common::Executors::IO()->Post([this, p]() {
        WeakDummyReturn(p);
        getLiveAvailableRange();
    });
    get_live_available_range_timer_.Start(drc_sdk::common::Executors::IO(), [this, p]() {
        WeakDummyReturn(p);
        getLiveAvailableRange();
    }, std::chrono::seconds(5), true);
    return listen_id;
}

void LiveDeviceModuleImpl::getLiveAvailableRange() {
    const auto gateway_sn = TopoRegistry::getInstance().resolveGatewaySn(sn_).value_or(sn_);
    if (gateway_sn != sn_) {
        LOG_INFO("LiveDeviceModuleImpl: getLiveAvailableRange, device_sn=%s, gateway_sn=%s",
            sn_.c_str(), gateway_sn.c_str());
    }

    WeakDummy(p);
    get_device_topo(gateway_sn, [this, p](const DeviceTopoResponse& response, FlightHubError err) {
        WeakDummyReturn(p);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("LiveDeviceModuleImpl: getLiveAvailableRange failed: %d, sn=%s",
                static_cast<int>(err.error_code), sn_.c_str());
            return;
        }

        if (kind_ == DeviceKind::Drone || kind_ == DeviceKind::Dock) {
            LOG_INFO("LiveDeviceModuleImpl: getLiveAvailableRange received response, list.size()=%zu, sn=%s",
                response.list.size(), sn_.c_str());
        }

        do {
            if (response.list.empty()) {
                LOG_ERROR("GetLiveAvailableRange: list is empty");
                break;
            }

            const auto& live_capacity = response.list[0].properties.live_capacity;
            if (live_capacity.value.device_list.empty()) {
                if (kind_ == DeviceKind::Drone) {
                    LOG_ERROR("GetLiveAvailableRange: live_capacity.device_list is empty, sn=%s",
                        response.list[0].sn.c_str());
                } else {
                    LOG_ERROR("GetLiveAvailableRange: live_capacity.device_list is empty");
                }
                break;
            }

            if (kind_ == DeviceKind::Drone || kind_ == DeviceKind::Dock || kind_ == DeviceKind::BaseStation) {
                LOG_INFO("GetLiveAvailableRange: gateway_sn=%s, device_sn=%s, device_list.size()=%zu",
                    response.list[0].sn.c_str(), sn_.c_str(), live_capacity.value.device_list.size());
            }

            std::vector<LiveInfo> live_info_list;
            for (const auto& device_item : live_capacity.value.device_list) {
                const auto& device_sn = device_item.sn;

                // Dock / BaseStation：只收本 SN 下 camera（基站三方摄挂在基站 SN 条目）
                if (kind_ == DeviceKind::Dock || kind_ == DeviceKind::BaseStation) {
                    if (device_sn != sn_) {
                        continue;
                    }
                } else if (device_sn != sn_ && device_sn != response.list[0].sn) {
                    continue;
                }

                if (kind_ == DeviceKind::Drone) {
                    LOG_INFO("GetLiveAvailableRange: found matching device_sn=%s, camera_list.size()=%zu",
                        device_sn.c_str(), device_item.camera_list.size());
                }

                for (const auto& camera_item : device_item.camera_list) {
                    if (kind_ == DeviceKind::Drone) {
                        LOG_INFO("GetLiveAvailableRange: processing camera_index=%s, video_list.size()=%zu",
                            camera_item.camera_index.c_str(), camera_item.video_list.size());
                    }
                    for (const auto& video_item : camera_item.video_list) {
                        LiveInfo live_info;
                        live_info.device_sn = device_sn;
                        live_info.payload_index = camera_item.camera_index;
                        live_info.video_id = video_item.video_index;
                        for (const auto& lens_type : video_item.switchable_video_types) {
                            live_info.switchable_lens_types.push_back(
                                static_cast<int>(cameraLensStringToEnum(lens_type)));
                        }
                        live_info_list.push_back(live_info);
                        LOG_INFO("GetLiveAvailableRange: device_sn: %s, payload_index: %s, video_id: %s",
                            live_info.device_sn.c_str(),
                            live_info.payload_index.c_str(),
                            live_info.video_id.c_str());
                    }
                }
            }

            if (kind_ == DeviceKind::Drone || kind_ == DeviceKind::Dock || kind_ == DeviceKind::BaseStation) {
                LOG_INFO("GetLiveAvailableRange: final live_info_list.size()=%zu, sn=%s",
                    live_info_list.size(), sn_.c_str());
            }

            live_info_list_ = live_info_list;
            live_info_list_callbacks_.notifyCallbackIfChanged(live_info_list);
            return;
        } while (false);
    });
}

void LiveDeviceModuleImpl::cancelListenLiveAvailableRange(int listen_id) {
    get_live_available_range_timer_.Cancel();
    live_info_list_callbacks_.removeCallback(listen_id);
}

void LiveDeviceModuleImpl::getAudienceCount(
    const AudienceCountGroup* group,
    const std::function<void(FlightHubError err, const std::vector<DeviceAudienceCountItem>& result)>& callback) {
    std::vector<DeviceAudienceCountRequestItem> device_list;
    DeviceAudienceCountRequestItem item;
    item.sn = sn_;
    device_list.push_back(item);
    post_audience_count(device_list, group, [callback](const std::vector<DeviceAudienceCountItem>& result, FlightHubError err) {
        // 埋点: 查询在线观众数量; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_GET_AUDIENCE_COUNT, err, "[LiveDeviceModuleImpl]");

        if (callback) {
            callback(err, result);
        }
    });
}

int LiveDeviceModuleImpl::listenDockLiveStatus(const std::function<void(const DockLiveStatus& msg)>& callback) {
    return dock_live_status_callbacks_.addCallback(callback);
}

void LiveDeviceModuleImpl::cancelListenDockLiveStatus(int listen_id) {
    dock_live_status_callbacks_.removeCallback(listen_id);
}

int LiveDeviceModuleImpl::listenDockLiveCapacity(const std::function<void(const DockLiveCapacity& msg)>& callback) {
    return dock_live_capacity_callbacks_.addCallback(callback);
}

void LiveDeviceModuleImpl::cancelListenDockLiveCapacity(int listen_id) {
    dock_live_capacity_callbacks_.removeCallback(listen_id);
}
