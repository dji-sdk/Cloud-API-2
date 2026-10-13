// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include <unordered_map>

namespace message_engine {

class ITransportManager {
public:
    virtual ~ITransportManager() = default;

    /// 配置method路由表
    virtual void ResetMethodRouterMap() = 0;                                                                // 清空设置
    virtual void AddMethodRouterMap(std::unordered_map<MethodType, TransportIdType> router_map) = 0;        // 增/改 - 批量
    virtual void AddMethodRouterMap(const MethodType& method, const TransportIdType& transport_id) = 0;     // 增/改 - 单条
    virtual void RemoveMethodRouterMap(const std::vector<MethodType>& method_list) = 0;                     // 删 - 批量
    virtual void RemoveMethodRouterMap(const MethodType& method) = 0;                                       // 删 - 单条

    virtual std::unordered_map<MethodType, TransportIdType> GetMethodRouterMap() = 0;
};

}  // namespace message_engine