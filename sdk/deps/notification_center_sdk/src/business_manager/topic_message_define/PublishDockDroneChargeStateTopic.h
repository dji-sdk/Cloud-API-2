// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 机场飞机充电状态;
    class DockDroneChargeStateMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        int state = 0;
        int capacity_percent = 0;

        AIGC_JSON_HELPER(state, capacity_percent);

        explicit operator DockDroneChargeState() const {
            DockDroneChargeState dock_drone_charge_state{};

            dock_drone_charge_state.state = state;
            dock_drone_charge_state.capacity_percent = capacity_percent;

            return dock_drone_charge_state;
        }
    };


    class PublishDockDroneChargeStateTopic: public PublishTopicWrapper {
    public:
        DockDroneChargeStateMsg msg;
        [[nodiscard]] DockDroneChargeState getPodClass() const {
            return static_cast<DockDroneChargeState>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockDroneChargeStateTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockDroneChargeStateTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
