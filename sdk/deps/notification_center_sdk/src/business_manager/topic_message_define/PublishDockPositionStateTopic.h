// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 搜星状态(position_state);
    class DockPositionStateMsg {
    public:
        int is_calibration = 0;     // int, 是否标定, 0:未标定,1:已标定; 只有基站才有是否标定;
        int is_fixed = 0;           // int, 是否收敛, 0:未开始, 1:收敛中, 2:收敛成功, 3: 收敛失败;
        int quality = 0;            // int, 搜星档位, 1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档;
        int gps_number = 0;         // int, GPS 搜星数量;
        int rtk_number = 0;         // int, RTK 搜星数量;

        AIGC_JSON_HELPER(is_calibration, is_fixed, quality, gps_number, rtk_number);

        explicit operator DockPositionState() const {
            DockPositionState dock_position_state{};

            dock_position_state.is_calibration = is_calibration;
            dock_position_state.is_fixed = is_fixed;
            dock_position_state.quality = quality;
            dock_position_state.gps_number = gps_number;
            dock_position_state.rtk_number = rtk_number;

            return dock_position_state;
        }
    };


    class PublishDockPositionStateTopic: public PublishTopicWrapper {
    public:
        DockPositionStateMsg msg;

        [[nodiscard]] DockPositionState getPodClass() const {
            return static_cast<DockPositionState>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockPositionStateTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockPositionStateTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
