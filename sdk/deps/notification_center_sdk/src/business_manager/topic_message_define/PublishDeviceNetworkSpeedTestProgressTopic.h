// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class DeviceNetworkSpeedTestSpeedResultMsg {
    public:
        double upload_speed = 0.0;
        double download_speed = 0.0;

        AIGC_JSON_HELPER(upload_speed, download_speed);

        explicit operator DeviceNetworkSpeedTestSpeedResult() const {
            DeviceNetworkSpeedTestSpeedResult obj{};
            obj.upload_speed = upload_speed;
            obj.download_speed = download_speed;
            return obj;
        }
    };

    class DeviceNetworkSpeedTestProgressMsg {
    public:
        std::string bid;
        int64_t timestamp = 0;
        int result = 0;
        std::string status;
        DeviceNetworkSpeedTestSpeedResultMsg speed_result;

        AIGC_JSON_HELPER(bid, timestamp, result, status, speed_result);

        explicit operator DeviceNetworkSpeedTestProgress() const {
            DeviceNetworkSpeedTestProgress obj{};
            obj.bid = bid;
            obj.timestamp = timestamp;
            obj.result = result;
            obj.status = status;
            obj.speed_result = static_cast<DeviceNetworkSpeedTestSpeedResult>(speed_result);
            return obj;
        }
    };

    class PublishDeviceNetworkSpeedTestProgressTopic : public PublishTopicWrapper {
    public:
        DeviceNetworkSpeedTestProgressMsg msg;

        [[nodiscard]] DeviceNetworkSpeedTestProgress getPodClass() const {
            return static_cast<DeviceNetworkSpeedTestProgress>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDeviceNetworkSpeedTestProgressTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDeviceNetworkSpeedTestProgressTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
