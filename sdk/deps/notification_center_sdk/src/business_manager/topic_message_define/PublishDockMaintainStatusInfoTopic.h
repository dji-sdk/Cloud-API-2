// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 机场保养信息;
    class DockMaintainStatusMsg {
    public:
        int state = 0;                         // 保养状态(0: 未保养, 1: 保养中);
        int last_maintain_type = 0;            // 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养);
        long last_maintain_time = 0;           // 上一次保养时间, utc 秒时间戳(单位: 秒); 类型与历史 JSON 对齐
        int last_maintain_work_sorties = 0;    // 上一次保养时飞行架次, [0, 2147483647];

        AIGC_JSON_HELPER(state, last_maintain_type, last_maintain_time, last_maintain_work_sorties);

        explicit operator DockMaintainStatus() const {
            DockMaintainStatus dock_maintain_status{};

            dock_maintain_status.state = state;
            dock_maintain_status.last_maintain_type = last_maintain_type;
            dock_maintain_status.last_maintain_time = last_maintain_time;
            dock_maintain_status.last_maintain_work_sorties = last_maintain_work_sorties;

            return dock_maintain_status;
        }
    };

    // 机场保养信息列表;
    class DockMaintainStatusInfoMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        std::vector<DockMaintainStatusMsg> maintain_status_array;

        AIGC_JSON_HELPER(maintain_status_array);

        explicit operator DockMaintainStatusInfo() const {
            DockMaintainStatusInfo dock_maintain_status_info_obj{};

            for (const auto &info : maintain_status_array) {
                dock_maintain_status_info_obj.maintain_status_info.emplace_back(static_cast<DockMaintainStatus>(info));
            }

            return dock_maintain_status_info_obj;
        }
    };

    class PublishDockMaintainStatusInfoTopic: public PublishTopicWrapper {
    public:
        DockMaintainStatusInfoMsg msg;

        [[nodiscard]] DockMaintainStatusInfo getPodClass() const {
            return static_cast<DockMaintainStatusInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockMaintainStatusInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockMaintainStatusInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
