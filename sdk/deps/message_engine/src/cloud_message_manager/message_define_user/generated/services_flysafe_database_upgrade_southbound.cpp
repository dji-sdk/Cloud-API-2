// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_flysafe_database_upgrade_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFlysafeDatabaseUpgradeSouthbound::ServicesFlysafeDatabaseUpgradeSouthbound() {
    this->topic_ = "thing/product/gnd/services/flysafe_database_upgrade";
    this->method_ = "flysafe_database_upgrade";
}

ServicesFlysafeDatabaseUpgradeSouthbound::ServicesFlysafeDatabaseUpgradeSouthbound(const ServicesFlysafeDatabaseUpgradeSouthboundMsg& msg) : ServicesFlysafeDatabaseUpgradeSouthbound() {
    this->msg = msg;
}

std::string ServicesFlysafeDatabaseUpgradeSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFlysafeDatabaseUpgradeSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFlysafeDatabaseUpgradeSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFlysafeDatabaseUpgradeSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}