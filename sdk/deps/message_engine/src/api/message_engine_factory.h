// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_message_engine.h"
#include <base/async/executor.h>
#include <memory>

namespace message_engine {

class MessageEngineFactory {
public:
    static std::shared_ptr<IMessageEngine> createMessageEngine(const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    static void destroyMessageEngine(const std::shared_ptr<IMessageEngine>& messageEngine);

private:
    MessageEngineFactory() = default;
    ~MessageEngineFactory()= default;
    MessageEngineFactory(const MessageEngineFactory&&) = delete;
    MessageEngineFactory(const MessageEngineFactory&) = delete;
    void operator=(const MessageEngineFactory&&) = delete;  
    void operator=(const MessageEngineFactory&) = delete;
};

} // namespace message_engine