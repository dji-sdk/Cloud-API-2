// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 机场：DockPowerState;
    class DockPowerStateMsg {
    public:
        int electric_supply_voltage = 0;  // int, 市电电压, 单位：V;
        int working_voltage = 0;          // int, 工作电压(mV);
        int working_current = 0;          // int, 工作电流(mA);

        AIGC_JSON_HELPER(electric_supply_voltage, working_voltage, working_current);

        explicit operator DockPowerState() const {
            DockPowerState dock_power_state{};

            dock_power_state.electric_supply_voltage = electric_supply_voltage;
            dock_power_state.working_voltage = working_voltage;
            dock_power_state.working_current = working_current;

            return dock_power_state;
        }
    };


    class PublishDockPowerStateTopic: public PublishTopicWrapper {
    public:
        DockPowerStateMsg msg;

        [[nodiscard]] DockPowerState getPodClass() const {
            return static_cast<DockPowerState>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockPowerStateTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockPowerStateTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
