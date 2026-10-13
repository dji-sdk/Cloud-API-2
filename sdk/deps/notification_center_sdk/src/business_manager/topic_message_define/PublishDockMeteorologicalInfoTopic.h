// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 机场气象信息;
    class DockMeteorologicalInfoMsg {
    public:
        double temperature = 0.0;                   // 舱内温度;
        double environment_temperature = 0.0;       // 环境温度;
        int humidity = 0;                           // 舱内湿度;
        double wind_speed = 0.0;                    // 风速;
        int rainfall = 0;                           // 雨量;

        AIGC_JSON_HELPER(temperature, environment_temperature, humidity, wind_speed, rainfall);

        explicit operator DockMeteorologicalInfo() const {
            DockMeteorologicalInfo dock_meteorological_info{};

            dock_meteorological_info.temperature = temperature;
            dock_meteorological_info.environment_temperature = environment_temperature;
            dock_meteorological_info.humidity = humidity;
            dock_meteorological_info.wind_speed = wind_speed;
            dock_meteorological_info.rainfall = rainfall;

            return dock_meteorological_info;
        }
    };


    class PublishDockMeteorologicalInfoTopic : public PublishTopicWrapper {
    public:
        DockMeteorologicalInfoMsg msg;

        [[nodiscard]] DockMeteorologicalInfo getPodClass() const {
            return static_cast<DockMeteorologicalInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockMeteorologicalInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockMeteorologicalInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
