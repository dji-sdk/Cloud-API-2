// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_flysafe_database_upgrade_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyFlysafeDatabaseUpgradeNorthbound::EventsReplyFlysafeDatabaseUpgradeNorthbound() {
    this->topic_ = "thing/product/gnd/events_reply/flysafe_database_upgrade";
    this->method_ = "flysafe_database_upgrade";
}

EventsReplyFlysafeDatabaseUpgradeNorthbound::EventsReplyFlysafeDatabaseUpgradeNorthbound(const std::shared_ptr<MessageV1>& message) : EventsReplyFlysafeDatabaseUpgradeNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyFlysafeDatabaseUpgradeNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string EventsReplyFlysafeDatabaseUpgradeNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyFlysafeDatabaseUpgradeNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyFlysafeDatabaseUpgradeNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyFlysafeDatabaseUpgradeNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}