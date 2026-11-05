// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "esdk_control_module_impl.h"

#include <BusinessManager.h>
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "session/session_bm.h"
#include "services_custom_data_transmission_to_esdk_southbound.h"
#include "services_reply_custom_data_transmission_to_esdk_northbound.h"

using namespace flight_hub_sdk;

EsdkControlModuleImpl::EsdkControlModuleImpl(
    const std::string& drone_sn,
    const std::string& gateway_sn,
    std::shared_ptr<message_engine::IMessageEngine> message_engine,
    int session_id)
    : drone_sn_(drone_sn),
      gateway_sn_(gateway_sn),
      message_engine_(std::move(message_engine)),
      session_id_(session_id) {
    LOG_INFO("[EsdkControlModuleImpl] constructor, drone_sn: %s, gateway_sn: %s",
        drone_sn_.c_str(), gateway_sn_.c_str());
}

EsdkControlModuleImpl::~EsdkControlModuleImpl() {
    std::unordered_map<int, int64_t> pending;
    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        pending.swap(esdk_custom_data_from_esdk_nc_listen_ids_);
    }
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        for (const auto& kv : pending) {
            bm->CancelObserve(kv.second);
        }
    }
    LOG_INFO("[EsdkControlModuleImpl] destructor, cancel %d listen(s)", (int)pending.size());
}

int EsdkControlModuleImpl::listenEsdkCustomDataTransmissionFromEsdk(const std::function<void(const EsdkCustomDataTransmission& msg)>& callback) {
    // 该 topic 由 dock/gateway 上行推送(device_sn=gateway_sn), SDK 须按 gateway_sn 订阅才能收到;
    // 早期实现误用 drone_sn_ 订阅, 与服务端推送 SN 不匹配导致收不到消息。
    const std::string& subscribe_sn = !gateway_sn_.empty() ? gateway_sn_ : drone_sn_;
    if (subscribe_sn.empty()) {
        LOG_ERROR("EsdkControlModuleImpl: subscribe_sn is empty (drone_sn=%s, gateway_sn=%s)",
            drone_sn_.c_str(), gateway_sn_.c_str());
        return -1;
    }
    WeakDummy(weak_ptr);
    int user_listen_id;
    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        user_listen_id = esdk_custom_data_from_esdk_next_id_++;
    }

    auto bm = ResolveBusinessManager(session_id_);
    if (!bm) {
        LOG_ERROR("[EsdkControlModuleImpl] listenEsdkCustomDataTransmissionFromEsdk: BusinessManager not found, session_id=%d", session_id_);
        return -1;
    }
    auto nc_listen_id = bm->ListenEsdkCustomDataTransmissionFromEsdk(
        [this, weak_ptr, callback](const notification_center::EsdkCustomDataTransmission& msg) -> void {
            WeakDummyReturn(weak_ptr);
            EsdkCustomDataTransmission res{};
            res.value = msg.value;

            LOG_INFO("[EsdkControlModuleImpl] ListenEsdkCustomDataTransmissionFromEsdk: value size: %d",
                (int)res.value.size());

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[EsdkControlModuleImpl] ListenEsdkCustomDataTransmissionFromEsdk error: %d", error_code);
            }
        }, subscribe_sn, notification_center::NotificationFrequency_Any);

    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        esdk_custom_data_from_esdk_nc_listen_ids_[user_listen_id] = nc_listen_id;
    }
    LOG_INFO("[EsdkControlModuleImpl] listenEsdkCustomDataTransmissionFromEsdk user_listen_id: %d, nc_listen_id: %lld",
        user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void EsdkControlModuleImpl::cancelListenEsdkCustomDataTransmissionFromEsdk(int listen_id) {
    LOG_INFO("[EsdkControlModuleImpl] cancelListenEsdkCustomDataTransmissionFromEsdk listen_id: %d", listen_id);
    int64_t nc_listen_id = 0;
    {
        std::lock_guard<std::mutex> lock(listen_mutex_);
        auto it = esdk_custom_data_from_esdk_nc_listen_ids_.find(listen_id);
        if (it == esdk_custom_data_from_esdk_nc_listen_ids_.end()) {
            return;
        }
        nc_listen_id = it->second;
        esdk_custom_data_from_esdk_nc_listen_ids_.erase(it);
    }
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        bm->CancelObserve(nc_listen_id);
    }
}

void EsdkControlModuleImpl::sendEsdkCustomDataTransmissionToEsdk(
    const std::string& value,
    const std::function<void(FlightHubError err)>& callback) {
    // DJI 文档约束：数据内容长度 < 256
    if (value.size() >= 256) {
        LOG_ERROR("EsdkControlModuleImpl: sendEsdkCustomDataTransmissionToEsdk invalid value length: %d", (int)value.size());
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        report_beacon_event_helper(kBeaconReportEvent_ESDK_CTRL_SEND_CUSTOM_DATA_TO_ESDK, param_err, session_id_, "[EsdkControlModuleImpl]");
        if (callback) {
            drc_sdk::common::Executors::Main()->Post([callback, param_err]() -> void { callback(param_err); });
        }
        return;
    }
    if (!message_engine_) {
        LOG_ERROR("EsdkControlModuleImpl: message_engine_ is not initialized");
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);
        report_beacon_event_helper(kBeaconReportEvent_ESDK_CTRL_SEND_CUSTOM_DATA_TO_ESDK, state_err, session_id_, "[EsdkControlModuleImpl]");
        if (callback) {
            drc_sdk::common::Executors::Main()->Post([callback, state_err]() -> void { callback(state_err); });
        }
        return;
    }

    WeakDummy(weak_ptr);
    auto msg = std::make_shared<message_engine::ServicesCustomDataTransmissionToEsdkSouthbound>();
    msg->msg.value = value;

    auto err = message_engine_->getCloudMessageManager()->SendServicesCustomDataTransmissionToEsdkSouthbound(
        msg, [this, weak_ptr, callback](int error_code, const std::shared_ptr<message_engine::ServicesReplyCustomDataTransmissionToEsdkNorthbound>& reply) {
            WeakDummyReturn(weak_ptr);
            FlightHubError result;
            if (error_code == message_engine::MeErrorCode::ME_NO_ERROR && reply && reply->msg.result == 0) {
                result = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);
            } else {
                LOG_ERROR("[EsdkControlModuleImpl] sendEsdkCustomDataTransmissionToEsdk failed, error_code: %d, result: %d",
                    error_code, reply ? reply->msg.result : -1);
                // 设备端返回码透出到 device_code, 便于 ISV 定位设备拒绝原因
                result = FlightHubError::from_drc(error_code, reply ? reply->msg.result : -1);
            }
            report_beacon_event_helper(kBeaconReportEvent_ESDK_CTRL_SEND_CUSTOM_DATA_TO_ESDK, result, session_id_, "[EsdkControlModuleImpl]");
            if (callback) {
                drc_sdk::common::Executors::Main()->Post([callback, result]() -> void { callback(result); });
            }
        });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        LOG_ERROR("[EsdkControlModuleImpl] sendEsdkCustomDataTransmissionToEsdk link error: %lld", (long long)err);
        auto link_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK);
        report_beacon_event_helper(kBeaconReportEvent_ESDK_CTRL_SEND_CUSTOM_DATA_TO_ESDK, link_err, session_id_, "[EsdkControlModuleImpl]");
        if (callback) {
            drc_sdk::common::Executors::Main()->Post([callback, link_err]() -> void { callback(link_err); });
        }
    }
}
