// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    /// 失控动作枚举
    enum class OutOfControlAction {
        HOVER = 0,        ///< 悬停
        RETURN_HOME = 1,  ///< 返航
        CONTINUE = 2,     ///< 继续
        UNKNOWN = 0xFFFF, ///< 未知
    };

    class CommandTaskDataMsg {
    public:
        int id = 0;
        std::string created_at;
        std::string uuid;
        std::string prj_uuid;
        std::string sn;
        std::string drone_sn;
        std::string track_id;
        std::string user_id;
        int status = 0;
        int err_code = 0;
        std::string takeoff_mode;
        double security_takeoff_height = 0.0;
        int max_speed = 0;
        std::string out_of_control_action;
        int rth_altitude = 0;
        int commander_flight_mode = 0;
        double commander_flight_height = 0.0;
        int rth_mode = 0;
        std::string cloud_to_cloud_id;

        AIGC_JSON_HELPER(id, created_at, uuid, prj_uuid, sn, drone_sn, track_id, user_id,
            status, err_code, takeoff_mode, security_takeoff_height, max_speed,
            out_of_control_action, rth_altitude, commander_flight_mode,
            commander_flight_height, rth_mode, cloud_to_cloud_id);

        explicit operator CommandTaskData() const {
            CommandTaskData data{};
            data.id = id;
            data.created_at = created_at;
            data.uuid = uuid;
            data.prj_uuid = prj_uuid;
            data.sn = sn;
            data.drone_sn = drone_sn;
            data.track_id = track_id;
            data.user_id = user_id;
            data.status = status;
            data.err_code = err_code;
            data.takeoff_mode = takeoff_mode;
            data.security_takeoff_height = security_takeoff_height;
            data.max_speed = max_speed;
            // 将后端返回的字符串转换为 int 枚举值
            if (out_of_control_action == "Hover") {
                data.out_of_control_action = static_cast<int>(OutOfControlAction::HOVER);
            } else if (out_of_control_action == "ReturnHome") {
                data.out_of_control_action = static_cast<int>(OutOfControlAction::RETURN_HOME);
            } else if (out_of_control_action == "Continue") {
                data.out_of_control_action = static_cast<int>(OutOfControlAction::CONTINUE);
            } else {
                data.out_of_control_action = static_cast<int>(OutOfControlAction::RETURN_HOME);
            }
            data.rth_altitude = rth_altitude;
            data.commander_flight_mode = commander_flight_mode;
            data.commander_flight_height = commander_flight_height;
            data.rth_mode = rth_mode;
            data.cloud_to_cloud_id = cloud_to_cloud_id;
            return data;
        }
    };

    class CommandTaskEventMsg {
    public:
        std::string event_id;
        std::string action;
        std::string uuid;
        int64_t timestamp = 0;
        CommandTaskDataMsg task_data;

        AIGC_JSON_HELPER(event_id, action, uuid, timestamp, task_data);

        explicit operator CommandTaskEvent() const {
            CommandTaskEvent event{};
            event.event_id = event_id;
            event.action = action;
            event.uuid = uuid;
            event.timestamp = timestamp;
            event.task_data = static_cast<CommandTaskData>(task_data);
            return event;
        }
    };

    class PublishCommandTaskEventTopic : public PublishTopicWrapper {
    public:
        CommandTaskEventMsg msg;

        [[nodiscard]] CommandTaskEvent getPodClass() const {
            return static_cast<CommandTaskEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishCommandTaskEventTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishCommandTaskEventTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
