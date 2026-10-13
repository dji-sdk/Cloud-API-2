// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 飞行器电池保养信息(drone_battery_maintenance_info);
    // 机场飞行器 电池基础信息;
    class BatteryBasicInfoMsg {
    public:
        int index = 0;                   // int, 电池序号, 0:左电池; 1:右电池；
        int capacity_percent = 0;        // int, 电池剩余电量,[0,100];
        int voltage = 0;                 // int, 电压(mV), 参考[0~28000mV];
        double temperature = 0.0;        // double, 温度(摄氏度);


        AIGC_JSON_HELPER(index, capacity_percent, voltage, temperature);

        explicit operator BatteryBasicInfo() const {
            BatteryBasicInfo res{};

            res.index = index;
            res.capacity_percent = capacity_percent;
            res.voltage = voltage;
            res.temperature = temperature;

            return res;
        }
    };


    // 飞行器电池保养信息(drone_battery_maintenance_info);
    // [含] 机场飞行器 电池基础信息;
    class DockDroneBatteryMaintenanceInfoMsg {
    public:
        int maintenance_state = 0;                     // int, 保养状态, 0: 无需保养, 1: 待保养, 2: 正在保养;
        int maintenance_time_left = 0;                 // int, 电池保养剩余时间(小时);
        int heat_state = 0;                            // int, 电池加热保温状态, 0: 电池未开启加热或保温, 1: 电池在加热中, 2: 电池在保温中;

        std::vector<BatteryBasicInfoMsg> batteries;    // std::vector<BatteryBasicInfo>, 电池基础信息;


        AIGC_JSON_HELPER(maintenance_state, maintenance_time_left, heat_state, batteries);

        explicit operator DockDroneBatteryMaintenanceInfo() const {
            DockDroneBatteryMaintenanceInfo res{};
            
            res.maintenance_state = maintenance_state;
            res.maintenance_time_left = maintenance_time_left;
            res.heat_state = heat_state;
            res.batteries.clear();


            for (const auto & bat_info : batteries) {
                res.batteries.emplace_back(static_cast<BatteryBasicInfo>(bat_info));
            }

            return res;
        }
    };


    // topic define for publish;
    class PublishDockDroneBatteryMaintenanceInfoTopic: public PublishTopicWrapper {
    public:
        DockDroneBatteryMaintenanceInfoMsg msg;

        [[nodiscard]] DockDroneBatteryMaintenanceInfo getPodClass() const {
            return static_cast<DockDroneBatteryMaintenanceInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockDroneBatteryMaintenanceInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockDroneBatteryMaintenanceInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
