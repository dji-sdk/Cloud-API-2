// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_topic_util.h"

#include <base/common/logger.h>

namespace flight_hub_sdk {

namespace {

constexpr const char* kTopicPrefix = "thing/product/";
constexpr const char* kUpSuffix = "/drc/up";
constexpr const char* kDownSuffix = "/drc/down";

bool StartsWith(const std::string& value, const std::string& prefix) {
    return value.size() >= prefix.size() &&
           value.compare(0, prefix.size(), prefix) == 0;
}

bool EndsWith(const std::string& value, const std::string& suffix) {
    return value.size() >= suffix.size() &&
           value.compare(value.size() - suffix.size(), suffix.size(), suffix) == 0;
}

bool ExtractTopicSn(const std::string& topic, const char* suffix, std::string* out_sn) {
    if (!StartsWith(topic, kTopicPrefix) || !EndsWith(topic, suffix)) {
        return false;
    }
    const size_t prefix_len = std::char_traits<char>::length(kTopicPrefix);
    const size_t suffix_len = std::char_traits<char>::length(suffix);
    if (topic.size() <= prefix_len + suffix_len) {
        return false;
    }
    *out_sn = topic.substr(prefix_len, topic.size() - prefix_len - suffix_len);
    return !out_sn->empty();
}

}  // namespace

DrcMqttTopicInfo ResolveDrcMqttTopics(
    const OpenDataChannelResponseRtChannelConfig& config,
    const std::string& gateway_sn) {
    DrcMqttTopicInfo info;

    if (config.up_topic.empty() || config.down_topic.empty()) {
        if (gateway_sn.empty()) {
            LOG_ERROR("[DrcTopicUtil] up_topic/down_topic missing and gateway_sn empty");
            return info;
        }
        info.ok = true;
        info.from_backend_topics = false;
        info.topic_sn = gateway_sn;
        LOG_WARN("[DrcTopicUtil] up_topic/down_topic missing, fallback topic_sn to gateway_sn: %s",
                 gateway_sn.c_str());
        return info;
    }

    std::string up_sn;
    std::string down_sn;
    if (!ExtractTopicSn(config.up_topic, kUpSuffix, &up_sn) ||
        !ExtractTopicSn(config.down_topic, kDownSuffix, &down_sn)) {
        LOG_ERROR("[DrcTopicUtil] invalid drc topic format, up=%s down=%s",
                  config.up_topic.c_str(), config.down_topic.c_str());
        return info;
    }

    if (up_sn != down_sn) {
        LOG_ERROR("[DrcTopicUtil] topic_sn mismatch, up_sn=%s down_sn=%s",
                  up_sn.c_str(), down_sn.c_str());
        return info;
    }

    info.ok = true;
    info.from_backend_topics = true;
    info.up_topic = config.up_topic;
    info.down_topic = config.down_topic;
    info.topic_sn = up_sn;
    return info;
}

}  // namespace flight_hub_sdk
