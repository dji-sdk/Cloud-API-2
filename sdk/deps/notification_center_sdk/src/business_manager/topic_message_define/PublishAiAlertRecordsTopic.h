// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class AiAlertLocationMsg {
    public:
        double latitude = 0.0;
        double longitude = 0.0;
        double altitude = 0.0;

        AIGC_JSON_HELPER(latitude, longitude, altitude);

        explicit operator AiAlertRecordLocation() const {
            AiAlertRecordLocation res{};
            res.latitude = latitude;
            res.longitude = longitude;
            res.altitude = altitude;
            return res;
        }
    };

    class AiAlertTargetInfoMsg {
    public:
        int target_type = 0;
        double target_value = 0.0;
        bool use_min_threshold = false;
        bool use_max_threshold = false;
        double target_max_threshold = 0.0;
        double target_min_threshold = 0.0;
        std::string label;

        AIGC_JSON_HELPER(target_type, target_value, use_min_threshold, use_max_threshold,
                         target_max_threshold, target_min_threshold, label);

        explicit operator AiAlertRecordTargetInfo() const {
            AiAlertRecordTargetInfo res{};
            res.target_type = target_type;
            res.target_value = target_value;
            res.use_min_threshold = use_min_threshold;
            res.use_max_threshold = use_max_threshold;
            res.target_max_threshold = target_max_threshold;
            res.target_min_threshold = target_min_threshold;
            res.label = label;
            return res;
        }
    };

    class AiAlertRecordMsg {
    public:
        std::string alert_uuid;
        std::string flight_id;
        std::string drone_sn;
        std::string gateway_sn;
        int status = 0;
        int algorithm_source = 0;
        AiAlertLocationMsg location;
        int64_t file_id = 0;
        int64_t media_index = 0;
        std::string task_name;
        std::vector<AiAlertTargetInfoMsg> target_alert_infos;
        int64_t timestamp = 0;
        std::string thumbnail_url;
        std::vector<std::string> labels;

        AIGC_JSON_HELPER(alert_uuid, flight_id, drone_sn, gateway_sn, status,
                         algorithm_source, location, file_id, media_index, task_name,
                         target_alert_infos, timestamp, thumbnail_url, labels);

        explicit operator AiAlertRecordNotifyItem() const {
            AiAlertRecordNotifyItem res{};
            res.alert_uuid = alert_uuid;
            res.flight_id = flight_id;
            res.drone_sn = drone_sn;
            res.gateway_sn = gateway_sn;
            res.status = status;
            res.algorithm_source = algorithm_source;
            res.location = static_cast<AiAlertRecordLocation>(location);
            res.file_id = file_id;
            res.media_index = media_index;
            res.task_name = task_name;
            for (const auto &info : target_alert_infos) {
                res.target_alert_infos.emplace_back(static_cast<AiAlertRecordTargetInfo>(info));
            }
            res.timestamp = timestamp;
            res.thumbnail_url = thumbnail_url;
            res.labels = labels;
            return res;
        }
    };

    class AiAlertFlightDataMsg {
    public:
        std::string flight_id;
        std::string flight_task_name;
        std::vector<AiAlertRecordMsg> records;

        AIGC_JSON_HELPER(flight_id, flight_task_name, records);

        explicit operator AiAlertFlightNotifyData() const {
            AiAlertFlightNotifyData res{};
            res.flight_id = flight_id;
            res.flight_task_name = flight_task_name;
            for (const auto &r : records) {
                res.records.emplace_back(static_cast<AiAlertRecordNotifyItem>(r));
            }
            return res;
        }
    };

    class AiAlertRecordsNotifyMsg {
    public:
        std::vector<AiAlertFlightDataMsg> data;

        AIGC_JSON_HELPER(data);

        explicit operator AiAlertRecordsNotification() const {
            AiAlertRecordsNotification res{};
            for (const auto &d : data) {
                res.data.emplace_back(static_cast<AiAlertFlightNotifyData>(d));
            }
            return res;
        }
    };

    class PublishAiAlertRecordsTopic : public PublishTopicWrapper {
    public:
        AiAlertRecordsNotifyMsg msg;

        [[nodiscard]] AiAlertRecordsNotification getPodClass() const {
            return static_cast<AiAlertRecordsNotification>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAiAlertRecordsTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAiAlertRecordsTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
