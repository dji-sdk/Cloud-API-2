// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "transport/i_transport.h"
#include "cloud_message_manager/message_scheduler/message_scheduler.h"
#include "cloud_message_manager/message_wrapper/message_v1.h"
#include "cloud_message_manager/message_wrapper/message_v2.h"
#include "cloud_message_manager/message_wrapper/message_v3.h"
#include "real_time_channel_wrapper.h"
#include "transport/transport_manager.h"
#include <memory>

namespace message_engine {

/**
 * ITransport 适配器：将 RealTimeChannelWrapper（JS 注入的 RTC）适配为 ITransport，
 * 使 DRC 消息能通过 TransportManager 路由到 Web 端。
 * 参考 Linux 下 RtcChannelManager::AddTransport 与 VolcRealTimeChannel 的实现。
 */
class RealTimeChannelTransportAdapter : public ITransport,
                                       public MessageSchedulerListener,
                                       public std::enable_shared_from_this<RealTimeChannelTransportAdapter> {
public:
    RealTimeChannelTransportAdapter(
        const std::shared_ptr<RealTimeChannelWrapper>& wrapper,
        const std::shared_ptr<TransportManager>& transport_manager,
        const std::shared_ptr<drc_sdk::Executor>& executor,
        const std::string& log_tag = "volc_rtc");

    ~RealTimeChannelTransportAdapter() override;

    /** 初始化：注册 listener，需在构造后由创建者调用 */
    void Init();

    MeErrorCode Send(const std::shared_ptr<MessageWrapper>& message) override;
    MeErrorCode Send(const std::shared_ptr<MessageWrapper>& message, const MeResponse& response,
                     const std::shared_ptr<drc_sdk::Executor>& user_define_executor) override;
    ListenIdCode Observe(const std::shared_ptr<ObserveCallback>& callback) override;
    void CancelObserve(const ListenIdCode& listen_id) override;

    void OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper>& message) override;

private:
    void OnSentV1Message(const std::shared_ptr<MessageV1>& message);
    void OnSentV2Message(const std::shared_ptr<MessageV2>& message);
    void OnSentV3Message(const std::shared_ptr<MessageV3>& message);

    void OnReceivedBinaryMessage(const std::string& source_user_id,
                                const std::shared_ptr<drc_sdk::ByteBuffer>& message);
    void OnReceivedStringMessage(const std::string& source_user_id, const std::string& message);

    std::shared_ptr<RealTimeChannelWrapper> wrapper_;
    std::shared_ptr<MessageScheduler> scheduler_;
    std::shared_ptr<TransportManager> transport_manager_;
    std::string log_tag_;
};

}  // namespace message_engine
