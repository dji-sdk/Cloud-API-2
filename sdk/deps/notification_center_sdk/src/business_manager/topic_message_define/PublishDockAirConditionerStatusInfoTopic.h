// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class DockAirConditionerStatusInfoMsg {
    public:
        int air_conditioner_state = 0;
        int switch_time = 0;

        AIGC_JSON_HELPER(air_conditioner_state, switch_time);

        explicit operator DockAirConditionerStatusInfo() const {
            DockAirConditionerStatusInfo dock_air_conditioner_status_info{};
            dock_air_conditioner_status_info.air_conditioner_state = air_conditioner_state;
            dock_air_conditioner_status_info.switch_time = switch_time;
            return dock_air_conditioner_status_info;
        }
    };

    class PublishDockAirConditionerStatusInfoTopic : public PublishTopicWrapper {
    public:
        DockAirConditionerStatusInfoMsg msg;

        [[nodiscard]] DockAirConditionerStatusInfo getPodClass() const {
            return static_cast<DockAirConditionerStatusInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockAirConditionerStatusInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockAirConditionerStatusInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

