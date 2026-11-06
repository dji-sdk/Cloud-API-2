// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // AirSense 民航飞机告警上行 (topic_airsense_warning)
    // message_data 是对象 {"sn":..., "list":[{"icao":...}, ...]}，含外层设备 sn 与告警数组 list，直接解析即可。
    // 单条告警的解析视图: AirsenseWarningItem 是 notification_center_sdk_api 的公共 POD,
    // 未注册 AIGCJson(公共头刻意不引 AIGCJson.hpp), 直接放进被序列化的 vector 会让 AIGCJson
    // 落到「未知类型按 enum static_cast<int>」的兜底分支而编译失败。故在此定义带 AIGC_JSON_HELPER
    // 的本地镜像结构, 解析后经转换算子落回公共 POD——与本目录其它 *ItemMsg
    // (如 ControlRightsDeviceControlStatusItemMsg)完全一致。
    class AirsenseWarningItemMsg {
    public:
        std::string icao;
        int warning_level = 0;
        double latitude = 0.0;
        double longitude = 0.0;
        int altitude = 0;
        int altitude_type = 0;
        double heading = 0.0;
        int relative_altitude = 0;
        int vert_trend = 0;
        int distance = 0;

        AIGC_JSON_HELPER(icao, warning_level, latitude, longitude, altitude,
                         altitude_type, heading, relative_altitude, vert_trend, distance);

        explicit operator AirsenseWarningItem() const {
            AirsenseWarningItem item{};
            item.icao = icao;
            item.warning_level = warning_level;
            item.latitude = latitude;
            item.longitude = longitude;
            item.altitude = altitude;
            item.altitude_type = altitude_type;
            item.heading = heading;
            item.relative_altitude = relative_altitude;
            item.vert_trend = vert_trend;
            item.distance = distance;
            return item;
        }
    };

    class AirsenseWarningMsg {
    public:
        std::string sn;                            // 设备 SN，SDK 侧按 sn_ 分模块订阅，此处解析后丢弃不透出
        std::vector<AirsenseWarningItemMsg> list;  // 周边民航飞机告警数组

        AIGC_JSON_HELPER(sn, list);

        explicit operator AirsenseWarning() const {
            AirsenseWarning obj{};
            for (const auto &item : list) {
                obj.data.emplace_back(static_cast<AirsenseWarningItem>(item));
            }
            return obj;
        }
    };

    class PublishAirsenseWarningTopic : public PublishTopicWrapper {
    public:
        AirsenseWarningMsg msg;

        [[nodiscard]] AirsenseWarning getPodClass() const {
            return static_cast<AirsenseWarning>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAirsenseWarningTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
            message_topic = publish_msg->message_topic;
            message_data = publish_msg->message_data;
            need_replay = publish_msg->need_replay;
            message_id = publish_msg->message_id;
            message_type = publish_msg->message_type;
            timestamp = publish_msg->timestamp;
            version = publish_msg->version;
            device_sn = publish_msg->device_sn;
            subscribe_id = publish_msg->subscribe_id;

            // message_data 是对象 {"sn":..., "list":[...]}，直接用 AIGCJson 解析。
            std::string err;
            aigc::JsonHelper::JsonToObject(msg, publish_msg->message_data, {}, &err);
            if (!err.empty()) {
                LOG_ERROR("PublishAirsenseWarningTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
