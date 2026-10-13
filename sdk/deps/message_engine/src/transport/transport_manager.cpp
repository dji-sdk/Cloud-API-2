// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "transport_manager.h"

#include <base/common/logger.h>
#include <common/executors/executors.h>

using namespace message_engine;

TransportManager::TransportManager(const std::shared_ptr<drc_sdk::Executor> &executor) : working_executor_(executor) {
    LOG_WARN("Message Engine startup [TransportManager] ctor");
    observe_callback_manager_ = std::make_shared<ObserveCallbackManager>();
}

TransportManager::~TransportManager() {
    LOG_WARN("Message Engine shutdown [TransportManager] dtor");
}

void TransportManager::AddTransport(const TransportIdType& transport_id, const std::shared_ptr<ITransport>& transport) {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] AddTransport, transport_id: %s", transport_id.c_str());
    if (transport_map_.contains(transport_id)) {
        LOG_WARN("[TransportManager] AddTransport, duplicate add, transport_id: %s", transport_id.c_str());
    }
    transport_map_[transport_id] = transport;
}

void TransportManager::RemoveTransport(const std::string& transport_id) {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] RemoveTransport, transport_id: %s", transport_id.c_str());
    if (!transport_map_.contains(transport_id)) {
        LOG_WARN("[TransportManager] RemoveTransport, not found this transport_id: %s", transport_id.c_str());
        return;
    }
    transport_map_.erase(transport_id);
}

void TransportManager::ResetMethodRouterMap() {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] ResetMethodRouterMap");
    method_router_map_.clear();
}

void TransportManager::AddMethodRouterMap(std::unordered_map<MethodType, TransportIdType> router_map) {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] AddMethodRouterMap batch");
    for (auto& [method, new_transport_id] : router_map) {
        if (method_router_map_.contains(method) && method_router_map_[method] != new_transport_id) {
            // 更新 method 使用的 transport id
            method_router_map_[method] = new_transport_id;
            LOG_DEBUG("[TransportManager] %s -> %s update", method.c_str(), new_transport_id.c_str());
        } else {
            // 新建映射关系
            method_router_map_[method] = new_transport_id;
            LOG_DEBUG("[TransportManager] %s -> %s new", method.c_str(), new_transport_id.c_str());
        }
    }
}

void TransportManager::AddMethodRouterMap(const MethodType& method, const TransportIdType& transport_id) {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] AddMethodRouterMap single, method: %s, transport_id: %s", method.c_str(), transport_id.c_str());
    method_router_map_[method] = transport_id;
}

void TransportManager::RemoveMethodRouterMap(const std::vector<MethodType>& method_list) {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] RemoveMethodRouterMap batch");
    for (const auto& method : method_list) {
        method_router_map_.erase(method);
    }
}

void TransportManager::RemoveMethodRouterMap(const MethodType& method) {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] RemoveMethodRouterMap single");
    method_router_map_.erase(method);
}

std::unordered_map<MethodType, TransportIdType> TransportManager::GetMethodRouterMap() {
    std::lock_guard lock(mutex_);
    LOG_INFO("[TransportManager] GetMethodRouterMap");
    return method_router_map_;
}

std::shared_ptr<ITransport> TransportManager::GetTransportByMethod(const MethodType& method) {
    std::lock_guard lock(mutex_);
    if (!method_router_map_.contains(method)) {
        LOG_WARN("[TransportManager] GetTransportByMethod, not found this method: %s", method.c_str());
        return nullptr;
    }

    const auto transport_id = method_router_map_.at(method);
    if (!transport_map_.contains(transport_id)) {
        LOG_ERROR("[TransportManager] GetTransportByMethod, not found this transport_id: %s", transport_id.c_str());
        return nullptr;
    }
    const auto transport = transport_map_.at(transport_id);
    return transport.lock();
}

void TransportManager::CancelObserveMessage(ListenIdCode listen_id) {
    if (observe_callback_manager_ == nullptr) {
        LOG_ERROR("[TransportManager] CancelObserveMessage, observe_callback_manager_ is nullptr");
        return;
    }
    observe_callback_manager_->RemoveObserveCallback(listen_id);
}

ListenIdCode TransportManager::Observe(MethodType method, const MeObserveResults& observe_results, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    auto listen_id = GetListenID();
    auto executor = user_define_executor;
    if (executor == nullptr) {
        executor = working_executor_;
    }
    auto observe_callback = std::make_shared<ObserveCallback>(method, executor, listen_id, observe_results);
    observe_callback_manager_->AddObserveCallback(observe_callback);
    return listen_id;
}

std::shared_ptr<ObserveCallbackManager> TransportManager::GetObserveCallbackManager() {
    return observe_callback_manager_;
}