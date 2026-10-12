// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "device_hms_module_impl.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;
using namespace notification_center;

namespace {

void FillHmsBasicsFromNc(const notification_center::HmsDataBasics& src, flight_hub_sdk::HmsDataBasics& dst) {
    dst.args.component_index = src.args.component_index;
    dst.args.sensor_index = src.args.sensor_index;
    dst.args.device_id = src.args.device_id;
    dst.args.sender_host_id = src.args.sender_host_id;
    dst.args.ignore = src.args.ignore;
    dst.args.alarm_info = src.args.alarm_info;
    dst.args.create_timestamp = src.args.create_timestamp;
    dst.args.display_scene = src.args.display_scene;
    dst.level = src.level;
    dst.module_id = src.module_id;
    dst.code = src.code;
    dst.device_type = src.device_type;
    dst.domain_type = src.domain_type;
    dst.in_the_sky = src.in_the_sky;
    dst.imminent = src.imminent;
    dst.status_key = src.status_key;
    dst.joint_key = src.joint_key;
    dst.id = src.id;
    dst.parent_id = src.parent_id;
}

flight_hub_sdk::HmsData ConvertNcHmsDataToSdk(const notification_center::HmsData& msg) {
    flight_hub_sdk::HmsData hms_data;
    for (const auto& hms_data_item : msg.hms_data_items) {
        flight_hub_sdk::HmsDataItem item;
        FillHmsBasicsFromNc(hms_data_item.basics, item.basics);
        item.sub_hms_data.clear();
        for (const auto& sub_hms_data_basics : hms_data_item.sub_hms_data) {
            flight_hub_sdk::HmsDataBasics sub_basics;
            FillHmsBasicsFromNc(sub_hms_data_basics, sub_basics);
            item.sub_hms_data.emplace_back(sub_basics);
        }
        hms_data.hms_data_items.emplace_back(item);
    }
    return hms_data;
}

} // namespace

DeviceHMSModuleImpl::DeviceHMSModuleImpl(DeviceKind kind, const std::string& sn)
    : kind_(kind), sn_(sn) {
    registerNotificationCenterListener();
}

DeviceHMSModuleImpl::~DeviceHMSModuleImpl() {
    if (hms_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(hms_listen_id_);
        hms_listen_id_ = -1;
    }
}

void DeviceHMSModuleImpl::registerNotificationCenterListener() {
    WeakDummy(p);

    auto on_msg = [this, p](const notification_center::HmsData& msg) -> void {
        WeakDummyReturn(p);
        auto hms_data = ConvertNcHmsDataToSdk(msg);
        LOG_DEBUG("[DeviceHMSModule-%s] listenDeviceHMS, hms_data count: %d",
            sn_.c_str(), static_cast<int>(hms_data.hms_data_items.size()));
        hms_data_callbacks_.notifyCallbackIfChanged(hms_data);
    };

    auto on_err = [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
        WeakDummyReturn(p);
        if (error_code != NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
            LOG_ERROR("[DeviceHMSModule-%s] listenDeviceHMS, error: %d", sn_.c_str(), error_code);
        }
    };

    auto bm = NotificationCenter::getInstance()->getBusinessManager();
    if (kind_ == DeviceKind::Dock) {
        hms_listen_id_ = bm->ListenDockHMS(on_msg, on_err, sn_, NotificationFrequency_OnChanged);
    } else {
        hms_listen_id_ = bm->ListenAircraftHMS(on_msg, on_err, sn_, NotificationFrequency_OnChanged);
    }

    if (hms_listen_id_ < NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DeviceHMSModule-%s] register HMS listener failed, error: %d", sn_.c_str(), hms_listen_id_);
    }
}

int DeviceHMSModuleImpl::listenDeviceHMS(
    const std::function<void(const HmsData& msg)>& callback) {
    return hms_data_callbacks_.addCallback(callback);
}

void DeviceHMSModuleImpl::cancelListenDeviceHMS(int listen_id) {
    hms_data_callbacks_.removeCallback(listen_id);
}
