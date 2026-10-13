// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 机场声光报警状态(alarm_state);
    // DockAlarmState;
    class DockAlarmStateMsg {
    public:
         int alarm_state = 0;    // int, 声光报警状态, {0: 声光报警关闭, 1: 声光报警开启};

        AIGC_JSON_HELPER(alarm_state);

        explicit operator DockAlarmState() const {
            DockAlarmState res{};

            res.alarm_state = alarm_state;
            return res;
        }
    };

    class PublishDockAlarmStateTopic: public PublishTopicWrapper {
    public:
        DockAlarmStateMsg msg;

        [[nodiscard]] DockAlarmState getPodClass() const {
            return static_cast<DockAlarmState>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockAlarmStateTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockAlarmStateTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
