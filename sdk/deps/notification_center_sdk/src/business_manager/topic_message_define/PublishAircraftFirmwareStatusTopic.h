// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 固件状态信息;
    // AircraftFirmwareStatus;
    class AircraftFirmwareStatusMsg {
    public:
        std::string firmware_version = "";     // 固件版本, such as "00.01.0439";
        int compatible_status = 0;             // 固件一致性, {"0: 一致, 1: 不一致"};
        int firmware_upgrade_status = 0;       // 固件升级状态, {"0: 未升级, 1: 升级中"};

        AIGC_JSON_HELPER(firmware_version, compatible_status, firmware_upgrade_status);

        explicit operator AircraftFirmwareStatus() const {
            AircraftFirmwareStatus res{};

            res.firmware_version = firmware_version;
            res.compatible_status = compatible_status;
            res.firmware_upgrade_status = firmware_upgrade_status;

            return res;
        }
    };


    // topic define;
    class PublishAircraftFirmwareStatusTopic: public PublishTopicWrapper {
    public:
        AircraftFirmwareStatusMsg msg;

        [[nodiscard]] AircraftFirmwareStatus getPodClass() const {
            return static_cast<AircraftFirmwareStatus>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftFirmwareStatusTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftFirmwareStatusTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

