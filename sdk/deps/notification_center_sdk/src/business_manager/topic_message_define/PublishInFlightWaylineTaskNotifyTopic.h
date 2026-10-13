// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"
#include <base/common/logger.h>

namespace notification_center {

class InFlightWaylineTaskProgressPercentMsg {
public:
    int percent = 0;

    AIGC_JSON_HELPER(percent);

    explicit operator InFlightWaylineTaskProgressPercent() const {
        InFlightWaylineTaskProgressPercent info{};
        info.percent = percent;
        return info;
    }
};

class InFlightWaylineTaskNotifyMsg {
public:
    std::string in_flight_wayline_id;
    InFlightWaylineTaskProgressPercentMsg progress;
    int status = 0;
    int result = 0;
    std::string gateway_sn;
    int way_point_index = 0;
    double wayline_duration = 0.0;
    double wayline_length = 0.0;
    std::string task_name;
    int64_t timestamp = 0;

    AIGC_JSON_HELPER(in_flight_wayline_id, progress, status, result, gateway_sn,
                     way_point_index, wayline_duration, wayline_length, task_name, timestamp);

    explicit operator InFlightWaylineTaskNotify() const {
        InFlightWaylineTaskNotify info{};
        info.in_flight_wayline_id = in_flight_wayline_id;
        info.progress = static_cast<InFlightWaylineTaskProgressPercent>(progress);
        info.status = status;
        info.result = result;
        info.gateway_sn = gateway_sn;
        info.way_point_index = way_point_index;
        info.wayline_duration = wayline_duration;
        info.wayline_length = wayline_length;
        info.task_name = task_name;
        info.timestamp = timestamp;
        return info;
    }
};

class PublishInFlightWaylineTaskNotifyTopic : public PublishTopicWrapper {
public:
    InFlightWaylineTaskNotifyMsg msg;

    [[nodiscard]] InFlightWaylineTaskNotify getPodClass() const {
        return static_cast<InFlightWaylineTaskNotify>(msg);
    }

    bool isValid() override { return is_valid_; }

    explicit PublishInFlightWaylineTaskNotifyTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
        message_topic = publish_msg->message_topic;
        message_data = publish_msg->message_data;
        need_replay = publish_msg->need_replay;
        message_id = publish_msg->message_id;
        message_type = publish_msg->message_type;
        timestamp = publish_msg->timestamp;
        version = publish_msg->version;
        device_sn = publish_msg->device_sn;
        subscribe_id = publish_msg->subscribe_id;

        std::string err;
        aigc::JsonHelper::JsonToObject(msg, publish_msg->message_data, {}, &err);
        if (!err.empty()) {
            LOG_ERROR("PublishInFlightWaylineTaskNotifyTopic ctor error, err: %s, json: %s",
                      err.c_str(), publish_msg->ToJsonString().c_str());
            is_valid_ = false;
        }
    }

    AIGC_JSON_HELPER(msg);
    AIGC_JSON_HELPER_BASE((PublishTopicWrapper *)this)

    std::string ToJsonString() override {
        std::string err, json_string;
        aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
        return json_string;
    }

    void FromJsonString(const std::string &json) override {
        std::string err;
        aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
    }

private:
    bool is_valid_ = true;
};

} // namespace notification_center
