// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/async/executor.h"
#include "api/defines.h"
#include "cloud_message_manager/message_scheduler/message_callback.h"

namespace message_engine {

/* ITransport: 对不同链路的抽象
 * 每一个mqtt_client、RealTimeChannel的实例，都是一个Transport
 * 抹平mqtt、rtc链路消息收发的差异
 */

class ITransport {
public:
    virtual ~ITransport() = default;

    // 直接发送，没有回包，或者不关心回复
    virtual MeErrorCode Send(const std::shared_ptr<MessageWrapper>& message) = 0;

    // 发送后等待对端回复，对端回复相同uuid的消息，才认为发送成功，否则按照超时处理
    virtual MeErrorCode Send(const std::shared_ptr<MessageWrapper>& message, const MeResponse& response, const std::shared_ptr<::drc_sdk::Executor>& user_define_executor) = 0;

    virtual ListenIdCode Observe(const std::shared_ptr<ObserveCallback>& callback) = 0;

    virtual void CancelObserve(const ListenIdCode& listen_id) = 0;
};

} // namespace message_engine