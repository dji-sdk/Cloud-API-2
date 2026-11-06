// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "airsense_warning_module_impl.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "session/session_bm.h"

using namespace flight_hub_sdk;
using namespace notification_center;

namespace {

// 两个 using namespace 同时引入 flight_hub_sdk 与 notification_center 的同名 AirsenseWarning(Item),
// 匿名命名空间里的这个转换函数不在类作用域内, 非限定名会二义。SDK 侧目标类型显式限定 flight_hub_sdk::。
flight_hub_sdk::AirsenseWarning ConvertNcAirsenseWarningToSdk(const notification_center::AirsenseWarning& msg) {
    flight_hub_sdk::AirsenseWarning result;
    result.data.reserve(msg.data.size());
    for (const auto& item : msg.data) {
        flight_hub_sdk::AirsenseWarningItem dst;
        dst.icao = item.icao;
        dst.warning_level = item.warning_level;
        dst.latitude = item.latitude;
        dst.longitude = item.longitude;
        dst.altitude = item.altitude;
        dst.altitude_type = item.altitude_type;
        dst.heading = item.heading;
        dst.relative_altitude = item.relative_altitude;
        dst.vert_trend = item.vert_trend;
        dst.distance = item.distance;
        result.data.emplace_back(std::move(dst));
    }
    return result;
}

} // namespace

AirsenseWarningModuleImpl::AirsenseWarningModuleImpl(const std::string& drone_sn, const std::string& gateway_sn, int session_id)
    : drone_sn_(drone_sn), gateway_sn_(gateway_sn), session_id_(session_id) {
    registerNotificationCenterListener();
}

AirsenseWarningModuleImpl::~AirsenseWarningModuleImpl() {
    if (airsense_listen_id_ != -1) {
        auto bm = ResolveBusinessManager(session_id_);
        if (bm) {
            bm->CancelObserve(airsense_listen_id_);
        }
        airsense_listen_id_ = -1;
    }
}

void AirsenseWarningModuleImpl::registerNotificationCenterListener() {
    WeakDummy(p);

    auto on_msg = [this, p](const notification_center::AirsenseWarning& msg) -> void {
        WeakDummyReturn(p);
        auto sdk_data = ConvertNcAirsenseWarningToSdk(msg);
        LOG_DEBUG("[AirsenseWarningModule-%s] listenAirsenseWarning, count: %d",
            drone_sn_.c_str(), static_cast<int>(sdk_data.data.size()));
        airsense_callbacks_.notifyCallback(sdk_data);
    };

    auto on_err = [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
        WeakDummyReturn(p);
        if (error_code != NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
            LOG_ERROR("[AirsenseWarningModule-%s] listenAirsenseWarning, error: %d", drone_sn_.c_str(), error_code);
        }
    };

    // 该 topic 由 dock/gateway 上行推送(device_sn=gateway_sn), SDK 须按 gateway_sn 订阅才能收到;
    // 早期实现误用 drone_sn 订阅, 与服务端推送 SN 不匹配导致收不到消息。
    const std::string& subscribe_sn = !gateway_sn_.empty() ? gateway_sn_ : drone_sn_;
    auto bm = ResolveBusinessManager(session_id_);
    if (!bm) {
        LOG_ERROR("[AirsenseWarningModule-%s] registerNotificationCenterListener: BusinessManager not found, session_id=%d",
            drone_sn_.c_str(), session_id_);
        return;
    }
    airsense_listen_id_ = bm->ListenAirsenseWarning(on_msg, on_err, subscribe_sn, NotificationFrequency_Any);

    if (airsense_listen_id_ < NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[AirsenseWarningModule-%s] register airsense listener failed, error: %d", drone_sn_.c_str(), airsense_listen_id_);
    }
}

int AirsenseWarningModuleImpl::listenAirsenseWarning(
    const std::function<void(const AirsenseWarning& msg)>& callback) {
    return airsense_callbacks_.addCallback(callback);
}

void AirsenseWarningModuleImpl::cancelListenAirsenseWarning(int listen_id) {
    airsense_callbacks_.removeCallback(listen_id);
}
