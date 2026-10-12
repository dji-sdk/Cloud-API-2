// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 机场网络状态(network_state);
    class DockNetworkStateMsg {
    public:
        int type = -1;     // int, "网络类型", { 1: 4G, 2: 以太网 };  默认无效值: -1;
        int quality = -1;  // int, "网络质量(当网络类型为4G时才具有网络质量信息)", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1;
        int rate = 0;      // int, "网络速率(单位KB/秒)"; 默认值: 0;

        AIGC_JSON_HELPER(type, quality, rate);

        explicit operator DockNetworkState() const {
            DockNetworkState network_state{};

            network_state.type = type;
            network_state.quality = quality;
            network_state.rate = rate;

            return network_state;
        }
    };


    class PublishDockNetworkStateTopic : public PublishTopicWrapper {
    public:
        DockNetworkStateMsg msg;

        [[nodiscard]] DockNetworkState getPodClass() const {
            return static_cast<DockNetworkState>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockNetworkStateTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockNetworkStateTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
