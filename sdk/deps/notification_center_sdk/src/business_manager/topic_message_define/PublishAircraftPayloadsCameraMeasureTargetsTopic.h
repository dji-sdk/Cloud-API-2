// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 飞机: 激光打点信息;
    class AircraftPayloadsCameraMeasureTargetMsg {
    public:
        std::string payload_index;
        double measure_target_altitude = 0.0;     // 海拔高度;
        double measure_target_longitude = 0.0;    // 经度;
        double measure_target_latitude = 0.0;     // 纬度;
        double measure_target_distance = 0.0;
        int measure_target_error_state = 0;

        AIGC_JSON_HELPER(payload_index, measure_target_altitude, measure_target_longitude,
            measure_target_latitude, measure_target_distance, measure_target_error_state);

        explicit operator AircraftPayloadsCameraMeasureTarget() const {
            AircraftPayloadsCameraMeasureTarget measure_target {};

            measure_target.payload_index = payload_index;
            measure_target.measure_target_altitude = measure_target_altitude;
            measure_target.measure_target_longitude = measure_target_longitude;
            measure_target.measure_target_latitude = measure_target_latitude;
            measure_target.measure_target_distance = measure_target_distance;
            measure_target.measure_target_error_state = measure_target_error_state;

            return measure_target;
        }
    };

    // 飞机: 激光打点信息列表;
    class AircraftPayloadsCameraMeasureTargetsMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        std::vector<AircraftPayloadsCameraMeasureTargetMsg> measure_target_list;

        AIGC_JSON_HELPER(measure_target_list);

        explicit operator AircraftPayloadsCameraMeasureTargets() const {
            AircraftPayloadsCameraMeasureTargets measure_targets_obj{};

            for (const auto & target : measure_target_list) {
                measure_targets_obj.measure_targets.emplace_back(static_cast<AircraftPayloadsCameraMeasureTarget>(target));
            }

            return measure_targets_obj;
        }
    };


    class PublishAircraftPayloadsCameraMeasureTargetsTopic : public PublishTopicWrapper {
    public:
        AircraftPayloadsCameraMeasureTargetsMsg msg;

        [[nodiscard]] AircraftPayloadsCameraMeasureTargets getPodClass() const {
            return static_cast<AircraftPayloadsCameraMeasureTargets>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftPayloadsCameraMeasureTargetsTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftPayloadsCameraMeasureTargetsTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
