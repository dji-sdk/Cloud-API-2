// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 电池(存储)模式(battery_store_mode);
    // DockBatteryStoreMode;
    class DockBatteryStoreModeMsg {
    public:
        int battery_store_mode = 0;    // int, 电池(存储)模式, {1: 电池计划存储策略, 2: 电池应急存储策略};

        AIGC_JSON_HELPER(battery_store_mode);

        explicit operator DockBatteryStoreMode() const {
            DockBatteryStoreMode res{};
            res.battery_store_mode = battery_store_mode;

            return res;
        }
    };


    class PublishDockBatteryStoreModeTopic: public PublishTopicWrapper {
    public:
        DockBatteryStoreModeMsg msg;

        [[nodiscard]] DockBatteryStoreMode getPodClass() const {
            return static_cast<DockBatteryStoreMode>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockBatteryStoreModeTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockBatteryStoreModeTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

