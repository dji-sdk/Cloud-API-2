// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_transport.h"
#include "api/defines.h"
#include "cloud_message_manager/message_scheduler/message_callback.h"
#include "base/common/macros.h"
#include "base/async/executor.h"
#include <atomic>
#include <unordered_map>
#include <vector>
#include <mutex>
#include "api/i_transport_manager.h"

namespace message_engine {

// 生成ListenId
static ListenIdCode GetListenID() {
    static std::atomic<ListenIdCode> listen_id_{0};
    return ++listen_id_;
}

/* TransportManager: 管理创建出的多个Transport，并对消息进行路由选择
 * 缓存所有用户Observe的Callback，在Transport断联重连后，自动恢复用户的observe
 * 上层会创建多种 transport（MQTT client、DRTC / WebRTC realtime channel），统一存入 TransportManager
 * 并由上层配置 method 路由：每一条 method 走哪条 transport
 */
class TransportManager : public ITransportManager {
public:
    TransportManager(const std::shared_ptr<drc_sdk::Executor> &executor);
    ~TransportManager() override;

    /// 管理 transport
    void AddTransport(const TransportIdType& transport_id, const std::shared_ptr<ITransport>& transport);
    void RemoveTransport(const std::string& transport_id);

    /// ITransportManager: 配置 method 路由表
    void ResetMethodRouterMap() override;                                                                // 清空设置
    void AddMethodRouterMap(std::unordered_map<MethodType, TransportIdType> router_map) override;        // 增/改 - 批量
    void AddMethodRouterMap(const MethodType& method, const TransportIdType& transport_id) override;     // 增/改 - 单条
    void RemoveMethodRouterMap(const std::vector<MethodType>& method_list) override;                     // 删 - 批量
    void RemoveMethodRouterMap(const MethodType& method) override;                                       // 删 - 单条
    std::unordered_map<MethodType, TransportIdType> GetMethodRouterMap() override;

    /// 匹配 method 和 transport, CloudMessageManager使用
    std::shared_ptr<ITransport> GetTransportByMethod(const MethodType& method);

    /// 获取ObserveCallbackManager
    std::shared_ptr<ObserveCallbackManager> GetObserveCallbackManager();

    /// 缓存监听的callback
public:
    ListenIdCode Observe(MethodType method, const MeObserveResults& observe_results, const std::shared_ptr<drc_sdk::Executor>& user_define_executor);
    void CancelObserveMessage(ListenIdCode listen_id);

private:
    std::shared_ptr<drc_sdk::Executor> working_executor_;
    std::shared_ptr<ObserveCallbackManager> observe_callback_manager_;
    
    std::mutex mutex_;
    std::unordered_map<TransportIdType, std::weak_ptr<ITransport>> transport_map_; // transport id 和 transport 对象之间映射表
    std::unordered_map<MethodType, TransportIdType> method_router_map_; // 记录每一条method所使用的transport
};

}  // namespace message_engine