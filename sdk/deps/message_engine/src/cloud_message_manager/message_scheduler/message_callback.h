// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/async/executor.h"
#include "common/util/async_capture_protect.h"
#include "api/defines.h"
#include <atomic>
#include <list>
#include <mutex>
#include <unordered_map>

namespace message_engine {

using MeResponse = std::function<void(MeErrorCode error_code, std::shared_ptr<MessageWrapper>)>;
using MeObserveResults = std::function<void(std::shared_ptr<MessageWrapper>)>;

class SendCallback : public ::drc_sdk::common::AsyncCaptureProtect {
public:
    explicit SendCallback(MethodType method, std::shared_ptr<::drc_sdk::Executor> executor, std::string uuid, MeResponse response);

    void Call(const MeErrorCode& error_code, const std::shared_ptr<MessageWrapper>& message);

    [[nodiscard]] std::string GetUuid() const;

private:
    MethodType method_;
    std::shared_ptr<::drc_sdk::Executor> executor_;
    std::string uuid_;
    MeResponse response_;
    std::atomic<bool> called_{false};
};

class ObserveCallback : public ::drc_sdk::common::AsyncCaptureProtect {
public:
    explicit ObserveCallback(MethodType method, std::shared_ptr<::drc_sdk::Executor> executor, ListenIdCode listen_id, MeObserveResults observe_results);

    void Call(const std::shared_ptr<MessageWrapper>& message);

    [[nodiscard]] ListenIdCode GetListenId() const;
    [[nodiscard]] MethodType GetMethod() const;

private:
    MethodType method_;
    std::shared_ptr<::drc_sdk::Executor> executor_;
    ListenIdCode listen_id_ = 0;
    MeObserveResults observe_result_;
};

class SendCallbackManager {
public:
    void AddSendCallback(const std::shared_ptr<SendCallback>& callback);
    bool IsNeedCallback(const std::string& message_uuid);
    void RemoveSendCallback(const std::string& message_uuid);
    std::shared_ptr<SendCallback> GetSendCallback(const std::string& message_uuid);

private:
    std::mutex mutex_;
    std::unordered_map<std::string, std::shared_ptr<SendCallback>> send_callbacks_;
};

class ObserveCallbackManager {
public:
    void AddObserveCallback(const std::shared_ptr<ObserveCallback>& callback);
    void RemoveObserveCallback(ListenIdCode listen_id);

    std::list<std::shared_ptr<ObserveCallback>> GetObserveCallbacks(const MethodType& method);

private:
    std::mutex mutex_;
    std::unordered_map<std::string, std::list<std::shared_ptr<ObserveCallback>>> observe_callbacks_;
};

}  // namespace message_engine

