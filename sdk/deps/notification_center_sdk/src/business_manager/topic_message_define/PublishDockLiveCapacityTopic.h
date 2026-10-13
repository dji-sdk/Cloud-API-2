// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 直播能力视频信息;
    class LiveCapacityVideoMsg {
    public:
        std::string video_index;
        std::string video_type;
        std::vector<std::string> switchable_video_types;

        AIGC_JSON_HELPER(video_index, video_type, switchable_video_types);

        explicit operator LiveCapacityVideo() const {
            LiveCapacityVideo obj{};
            obj.video_index = video_index;
            obj.video_type = video_type;
            obj.switchable_video_types = switchable_video_types;
            return obj;
        }
    };

    // 直播能力相机信息;
    class LiveCapacityCameraMsg {
    public:
        std::string camera_index;
        int64_t available_video_number = 0;
        int64_t coexist_video_number_max = 0;
        std::vector<LiveCapacityVideoMsg> video_list;

        AIGC_JSON_HELPER(camera_index, available_video_number, coexist_video_number_max, video_list);

        explicit operator LiveCapacityCamera() const {
            LiveCapacityCamera obj{};
            obj.camera_index = camera_index;
            obj.available_video_number = available_video_number;
            obj.coexist_video_number_max = coexist_video_number_max;
            for (const auto &item : video_list) {
                obj.video_list.push_back(static_cast<LiveCapacityVideo>(item));
            }
            return obj;
        }
    };

    // 直播能力设备信息;
    class LiveCapacityDeviceMsg {
    public:
        std::string sn;
        int64_t available_video_number = 0;
        int64_t coexist_video_number_max = 0;
        std::vector<LiveCapacityCameraMsg> camera_list;

        AIGC_JSON_HELPER(sn, available_video_number, coexist_video_number_max, camera_list);

        explicit operator LiveCapacityDevice() const {
            LiveCapacityDevice obj{};
            obj.sn = sn;
            obj.available_video_number = available_video_number;
            obj.coexist_video_number_max = coexist_video_number_max;
            for (const auto &item : camera_list) {
                obj.camera_list.push_back(static_cast<LiveCapacityCamera>(item));
            }
            return obj;
        }
    };

    // 机场直播能力(topic_dock_live_capacity);
    class DockLiveCapacityMsg {
    public:
        int64_t available_video_number = 0;
        int64_t coexist_video_number_max = 0;
        std::vector<LiveCapacityDeviceMsg> device_list;

        AIGC_JSON_HELPER(available_video_number, coexist_video_number_max, device_list);

        explicit operator DockLiveCapacity() const {
            DockLiveCapacity obj{};
            obj.available_video_number = available_video_number;
            obj.coexist_video_number_max = coexist_video_number_max;
            for (const auto &item : device_list) {
                obj.device_list.push_back(static_cast<LiveCapacityDevice>(item));
            }
            return obj;
        }
    };

    class PublishDockLiveCapacityTopic : public PublishTopicWrapper {
    public:
        DockLiveCapacityMsg msg;

        [[nodiscard]] DockLiveCapacity getPodClass() const {
            return static_cast<DockLiveCapacity>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockLiveCapacityTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockLiveCapacityTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
