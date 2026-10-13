// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class FlightTaskExceptionDetailMsg {
    public:
        int code = 0;
        std::string message;
        uint32_t task_id = 0;
        std::string happen_at; // 时间字符串，如 "2025-09-17T04:01:00.972477409Z"
        std::string sn;

        AIGC_JSON_HELPER(code, message, task_id, happen_at, sn);

        explicit operator FlightTaskExceptionDetail() const {
            FlightTaskExceptionDetail detail{};
            detail.code = code;
            detail.message = message;
            detail.task_id = task_id;
            detail.happen_at = happen_at;
            detail.sn = sn;
            return detail;
        }
    };

    class FlightTaskExceptionMsg {
    public:
        std::string uuid;
        std::string name;
        std::string sn;
        int task_type = 0;
        int status = 0;
        int progress = 0;
        std::vector<FlightTaskExceptionDetailMsg> exceptions;
        int64_t timestamp = 0;

        AIGC_JSON_HELPER(uuid, name, sn, task_type, status, progress, exceptions, timestamp);

        explicit operator FlightTaskException() const {
            FlightTaskException exception{};
            exception.uuid = uuid;
            exception.name = name;
            exception.sn = sn;
            exception.task_type = task_type;
            exception.status = status;
            exception.progress = progress;
            exception.timestamp = timestamp;
            
            for (const auto & detail : exceptions) {
                exception.exceptions.emplace_back(static_cast<FlightTaskExceptionDetail>(detail));
            }
            
            return exception;
        }
    };

    class PublishFlightTaskExceptionTopic : public PublishTopicWrapper {
    public:
        FlightTaskExceptionMsg msg;

        [[nodiscard]] FlightTaskException getPodClass() const {
            return static_cast<FlightTaskException>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishFlightTaskExceptionTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishFlightTaskExceptionTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
}