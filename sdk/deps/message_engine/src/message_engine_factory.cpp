// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "api/message_engine_factory.h"
#include "message_engine_impl.h"

using namespace message_engine;

std::shared_ptr<IMessageEngine> MessageEngineFactory::createMessageEngine(const std::shared_ptr<::drc_sdk::Executor> &working_executor) {
    return std::make_shared<MessageEngineImpl>(working_executor);
}

void MessageEngineFactory::destroyMessageEngine(const std::shared_ptr<IMessageEngine>& messageEngine) {
    messageEngine->destroy();
}