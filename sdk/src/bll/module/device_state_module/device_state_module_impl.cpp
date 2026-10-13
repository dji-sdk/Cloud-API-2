// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "device_state_module_impl.h"
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;
using namespace notification_center;

DeviceStateModuleImpl::DeviceStateModuleImpl(DeviceKind kind, const std::string& sn)
    : kind_(kind), sn_(sn) {
    registerNotificationCenterListeners();
}

DeviceStateModuleImpl::~DeviceStateModuleImpl() {
    if (dock_online_status_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_online_status_listen_id_);
        dock_online_status_listen_id_ = -1;
    }
    if (drone_online_status_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_online_status_listen_id_);
        drone_online_status_listen_id_ = -1;
    }
}

void DeviceStateModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);

    if (kind_ == DeviceKind::Dock) {
        dock_online_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceOnlineStatus(
            [this, p](const notification_center::DeviceOnlineStatus& msg) -> void {
                WeakDummyReturn(p);
                DockOnlineStatus online_status;
                online_status.is_online = msg.is_online;
                LOG_INFO("[DeviceStateModule-%s] listenDockOnlineStatus, is_online: %d", sn_.c_str(), online_status.is_online);
                dock_online_status_callbacks_.notifyCallbackIfChanged(online_status);
            },
            [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
                WeakDummyReturn(p);
                if (error_code != NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                    LOG_ERROR("[DeviceStateModule-%s] listenDockOnlineStatus, error: %d", sn_.c_str(), error_code);
                }
            },
            sn_, NotificationFrequency_Any);
        if (dock_online_status_listen_id_ < NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
            LOG_ERROR("[DeviceStateModule-%s] ListenDockOnlineStatus, error: %d", sn_.c_str(), dock_online_status_listen_id_);
        }
    } else if (kind_ == DeviceKind::Drone) {
        drone_online_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceOnlineStatus(
            [this, p](const notification_center::DeviceOnlineStatus& msg) -> void {
                WeakDummyReturn(p);
                drc_sdk::common::Executors::Main()->Post([this, p, msg]() {
                    WeakDummyReturn(p);
                    DroneOnlineStatus status;
                    status.is_online = msg.is_online;
                    LOG_INFO("[DeviceStateModule-%s] listenDroneOnlineStatus, is_online: %d", sn_.c_str(), msg.is_online);
                    drone_online_status_callbacks_.notifyCallbackIfChanged(status);
                });
            },
            [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
                WeakDummyReturn(p);
                if (error_code != NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                    LOG_ERROR("[DeviceStateModule-%s] listenDroneOnlineStatus, error: %d", sn_.c_str(), error_code);
                }
            },
            sn_, NotificationFrequency_Any);
        if (drone_online_status_listen_id_ < NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
            LOG_ERROR("[DeviceStateModule-%s] ListenDroneOnlineStatus, error: %d", sn_.c_str(), drone_online_status_listen_id_);
        }
    }
}

int DeviceStateModuleImpl::listenDockOnlineStatus(
    const std::function<void(const DockOnlineStatus& status)>& callback) {
    if (kind_ != DeviceKind::Dock) {
        LOG_ERROR("[DeviceStateModule-%s] listenDockOnlineStatus called on non-Dock module", sn_.c_str());
        return -1;
    }
    return dock_online_status_callbacks_.addCallback(callback);
}

void DeviceStateModuleImpl::cancelListenDockOnlineStatus(int listen_id) {
    dock_online_status_callbacks_.removeCallback(listen_id);
}

int DeviceStateModuleImpl::listenDroneOnlineStatus(
    const std::function<void(const DroneOnlineStatus& status)>& callback) {
    if (kind_ != DeviceKind::Drone) {
        LOG_ERROR("[DeviceStateModule-%s] listenDroneOnlineStatus called on non-Drone module", sn_.c_str());
        return -1;
    }
    return drone_online_status_callbacks_.addCallback(callback);
}

void DeviceStateModuleImpl::cancelListenDroneOnlineStatus(int listen_id) {
    drone_online_status_callbacks_.removeCallback(listen_id);
}
