// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 电池信息
    class BatteryInfoMsg {
    public:
        int capacity_percent = 0;
        std::string firmware_version;
        int high_voltage_storage_days = 0;
        int index = 0;
        int loop_times = 0;
        std::string sn;
        int sub_type = 0;
        double temperature = 0.0;
        int type = 0;
        int voltage = 0;

        AIGC_JSON_HELPER(capacity_percent, firmware_version, high_voltage_storage_days,
                         index, loop_times, sn, sub_type, temperature, type, voltage);

        explicit operator BatteryInfo() const {
            BatteryInfo battery_info{};
            battery_info.capacity_percent = capacity_percent;
            battery_info.firmware_version = firmware_version;
            battery_info.high_voltage_storage_days = high_voltage_storage_days;
            battery_info.index = index;
            battery_info.loop_times = loop_times;
            battery_info.sn = sn;
            battery_info.sub_type = sub_type;
            battery_info.temperature = temperature;
            battery_info.type = type;
            battery_info.voltage = voltage;
            return battery_info;
        }
    };

    // 电池组信息
    class AircraftBatteryInfoMsg {
    public:
        std::vector<BatteryInfoMsg> batteries;
        int capacity_percent = 0;
        int landing_power = 0;
        int remain_flight_time = 0;
        int return_home_power = 0;

        AIGC_JSON_HELPER(batteries, capacity_percent, landing_power, remain_flight_time, return_home_power);

        explicit operator AircraftBatteryInfo() const {
            AircraftBatteryInfo aircraft_battery_info{};
            for (const auto & batterie : batteries) {
                aircraft_battery_info.batteries.emplace_back(static_cast<BatteryInfo>(batterie));
            }
            aircraft_battery_info.capacity_percent = capacity_percent;
            aircraft_battery_info.landing_power = landing_power;
            aircraft_battery_info.remain_flight_time = remain_flight_time;
            aircraft_battery_info.return_home_power = return_home_power;
            return aircraft_battery_info;
        }
    };

    class PublishAircraftBatteryInfoTopic : public PublishTopicWrapper {
    public:
        AircraftBatteryInfoMsg msg;

        [[nodiscard]] AircraftBatteryInfo getPodClass() const {
            return static_cast<AircraftBatteryInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftBatteryInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftBatteryInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
