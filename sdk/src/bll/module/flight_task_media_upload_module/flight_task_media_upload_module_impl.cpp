// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_task_media_upload_module_impl.h"

#include <BusinessManager.h>
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "session/session_bm.h"
#include "services_upload_flighttask_media_prioritize_southbound.h"
#include "services_reply_upload_flighttask_media_prioritize_northbound.h"

using namespace flight_hub_sdk;

FlightTaskMediaUploadModuleImpl::FlightTaskMediaUploadModuleImpl(
    const std::string& drone_sn,
    const std::string& gateway_sn,
    std::shared_ptr<message_engine::IMessageEngine> message_engine,
    int session_id)
    : drone_sn_(drone_sn),
      gateway_sn_(gateway_sn),
      message_engine_(std::move(message_engine)),
      session_id_(session_id) {
    LOG_INFO("[FlightTaskMediaUploadModuleImpl] constructor, drone_sn: %s, gateway_sn: %s",
        drone_sn_.c_str(), gateway_sn_.c_str());
}

FlightTaskMediaUploadModuleImpl::~FlightTaskMediaUploadModuleImpl() {
    std::unordered_map<int, int64_t> pending;
    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        pending.swap(highest_priority_nc_listen_ids_);
    }
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        for (const auto& kv : pending) {
            bm->CancelObserve(kv.second);
        }
    }
    LOG_INFO("[FlightTaskMediaUploadModuleImpl] destructor, cancel %d listen(s)", (int)pending.size());
}

int FlightTaskMediaUploadModuleImpl::listenHighestPriorityUploadFlighttaskMedia(
    const std::function<void(const HighestPriorityUploadFlighttaskMedia& msg)>& callback) {
    // 该 topic 由 dock/gateway 上行推送(device_sn=gateway_sn), SDK 须按 gateway_sn 订阅才能收到;
    // 早期实现误用 drone_sn_ 订阅, 与服务端推送 SN 不匹配导致收不到消息。
    const std::string& subscribe_sn = !gateway_sn_.empty() ? gateway_sn_ : drone_sn_;
    if (subscribe_sn.empty()) {
        LOG_ERROR("FlightTaskMediaUploadModuleImpl: subscribe_sn is empty (drone_sn=%s, gateway_sn=%s)",
            drone_sn_.c_str(), gateway_sn_.c_str());
        return -1;
    }
    WeakDummy(weak_ptr);
    int user_listen_id;
    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        user_listen_id = highest_priority_next_id_++;
    }

    auto bm = ResolveBusinessManager(session_id_);
    if (!bm) {
        LOG_ERROR("[FlightTaskMediaUploadModuleImpl] listenHighestPriorityUploadFlighttaskMedia: BusinessManager not found, session_id=%d", session_id_);
        return -1;
    }
    auto nc_listen_id = bm->ListenHighestPriorityUploadFlighttaskMedia(
        [this, weak_ptr, callback](const notification_center::HighestPriorityUploadFlighttaskMedia& msg) -> void {
            WeakDummyReturn(weak_ptr);
            HighestPriorityUploadFlighttaskMedia res{};
            res.flight_id = msg.flight_id;

            LOG_INFO("[FlightTaskMediaUploadModuleImpl] ListenHighestPriorityUploadFlighttaskMedia: flight_id: %s",
                res.flight_id.c_str());

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FlightTaskMediaUploadModuleImpl] ListenHighestPriorityUploadFlighttaskMedia error: %d", error_code);
            }
        }, subscribe_sn, notification_center::NotificationFrequency_Any);

    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        highest_priority_nc_listen_ids_[user_listen_id] = nc_listen_id;
    }
    LOG_INFO("[FlightTaskMediaUploadModuleImpl] listenHighestPriorityUploadFlighttaskMedia user_listen_id: %d, nc_listen_id: %lld",
        user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void FlightTaskMediaUploadModuleImpl::cancelListenHighestPriorityUploadFlighttaskMedia(int listen_id) {
    LOG_INFO("[FlightTaskMediaUploadModuleImpl] cancelListenHighestPriorityUploadFlighttaskMedia listen_id: %d", listen_id);
    int64_t nc_listen_id = 0;
    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        auto it = highest_priority_nc_listen_ids_.find(listen_id);
        if (it == highest_priority_nc_listen_ids_.end()) {
            return;
        }
        nc_listen_id = it->second;
        highest_priority_nc_listen_ids_.erase(it);
    }
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        bm->CancelObserve(nc_listen_id);
    }
}

void FlightTaskMediaUploadModuleImpl::uploadFlighttaskMediaPrioritize(
    const std::string& flight_id,
    const std::function<void(FlightHubError err)>& callback) {
    if (flight_id.empty()) {
        LOG_ERROR("FlightTaskMediaUploadModuleImpl: uploadFlighttaskMediaPrioritize invalid empty flight_id");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        report_beacon_event_helper(kBeaconReportEvent_FLIGHTTASK_MEDIA_PRIORITIZE_UPLOAD, param_err, session_id_, "[FlightTaskMediaUploadModuleImpl]");
        if (callback) {
            drc_sdk::common::Executors::Main()->Post([callback, param_err]() -> void { callback(param_err); });
        }
        return;
    }
    if (!message_engine_) {
        LOG_ERROR("FlightTaskMediaUploadModuleImpl: message_engine_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        report_beacon_event_helper(kBeaconReportEvent_FLIGHTTASK_MEDIA_PRIORITIZE_UPLOAD, state_err, session_id_, "[FlightTaskMediaUploadModuleImpl]");
        if (callback) {
            drc_sdk::common::Executors::Main()->Post([callback, state_err]() -> void { callback(state_err); });
        }
        return;
    }

    WeakDummy(weak_ptr);
    auto msg = std::make_shared<message_engine::ServicesUploadFlighttaskMediaPrioritizeSouthbound>();
    msg->msg.flight_id = flight_id;

    auto err = message_engine_->getCloudMessageManager()->SendServicesUploadFlighttaskMediaPrioritizeSouthbound(
        msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& reply) {
            WeakDummyReturn(weak_ptr);
            FlightHubError result;
            if (error_code == message_engine::MeErrorCode::ME_NO_ERROR && reply && reply->msg.result == 0) {
                result = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);
            } else {
                LOG_ERROR("[FlightTaskMediaUploadModuleImpl] uploadFlighttaskMediaPrioritize failed, error_code: %d, result: %d",
                    error_code, reply ? reply->msg.result : -1);
                // 设备端返回码透出到 device_code, 便于 ISV 定位设备拒绝原因
                result = FlightHubError::from_drc(error_code, reply ? reply->msg.result : -1);
            }
            report_beacon_event_helper(kBeaconReportEvent_FLIGHTTASK_MEDIA_PRIORITIZE_UPLOAD, result, session_id_, "[FlightTaskMediaUploadModuleImpl]");
            if (callback) {
                drc_sdk::common::Executors::Main()->Post([callback, result]() -> void { callback(result); });
            }
        });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        LOG_ERROR("[FlightTaskMediaUploadModuleImpl] uploadFlighttaskMediaPrioritize link error: %lld", (long long)err);
        auto link_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK);
        report_beacon_event_helper(kBeaconReportEvent_FLIGHTTASK_MEDIA_PRIORITIZE_UPLOAD, link_err, session_id_, "[FlightTaskMediaUploadModuleImpl]");
        if (callback) {
            drc_sdk::common::Executors::Main()->Post([callback, link_err]() -> void { callback(link_err); });
        }
    }
}