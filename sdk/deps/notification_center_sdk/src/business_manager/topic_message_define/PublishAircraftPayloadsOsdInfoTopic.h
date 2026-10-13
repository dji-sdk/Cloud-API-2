// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 智能跟踪点信息(smart track 点信息);
    // SmartTrackPointInfo;
    class SmartTrackPointInfoMsg {
    public:
        double track_altitude = 0.0;     // 跟踪目标海拔高度; 单位：米;
        double track_latitude = 0.0;     // 目标纬度, [-90, 90]度;
        double track_longitude = 0.0;    // 目标经度, [-180, 180]度;
        int track_target_mode = 0;       // track 模式, {1: 正常跟踪, 2: 正常跟踪，可信度低, 3: 目标是GPS点，或目标丢失但有预测};

        AIGC_JSON_HELPER(track_altitude, track_latitude, track_longitude, track_target_mode);

        explicit operator SmartTrackPointInfo() const {
            SmartTrackPointInfo smart_track_point_info{};
            
            smart_track_point_info.track_altitude = track_altitude;
            smart_track_point_info.track_latitude = track_latitude;
            smart_track_point_info.track_longitude = track_longitude;
            smart_track_point_info.track_target_mode = track_target_mode;

            return smart_track_point_info;
        }
    };


    // 飞机负载信息;
    // AircraftPayloadsOsdInfo
    class AircraftPayloadsOsdInfoMsg {
    public:
        std::string payload_index = "";                        // 负载索引/负载编号,{type-subtype-gimbalindex}, such as "81-0-0";

        double gimbal_pitch = 0.0;                             // 云台俯仰轴角度, [-180, 180]度;
        double gimbal_roll = 0.0;                              // 云台横滚轴角度,[-180, 180]度;
        double gimbal_yaw = 0.0;                               // 云台朝向角度/云台偏航轴角度,[-180, 180]度;

        double measure_target_altitude = 0.0;                  // 激光测距目标海拔(单位: m);
        double measure_target_longitude = 0.0;                 // 激光测距目标经度,[-180,180]度;
        double measure_target_latitude = 0.0;                  // 激光测距目标纬度,[-90, 90]度;
        double measure_target_distance = 0.0;                  // 测距距离(单位: m);
        double measure_target_error_state = 0;                 // 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL;

        std::vector<SmartTrackPointInfoMsg> smart_track_points;   // smart track 点信息;

        double zoom_factor = 0.0;                              // 当前变焦倍率;

        int thermal_current_palette_style = 0;                 // 调色盘样式;
        int thermal_gain_mode = 0;                             // 增益模式;
        double thermal_global_temperature_min = 0.0;           // 全局画面中测量的最低温度(摄氏度);
        double thermal_global_temperature_max = 0.0;           // 全局画面中测量的最高温度(摄氏度);
        int thermal_isotherm_state = 0;                        // 是否开启等温线, {0: 关闭, 1: 开启};
        int thermal_isotherm_lower_limit = 0;                  // 测温区间下限(摄氏度), 仅启用等温线功能后有效;
        int thermal_isotherm_upper_limit = 0;                  // 测温区间上限(摄氏度), 仅启用等温线功能后有效;

        std::vector<int> thermal_supported_palette_styles;     // 设备支持的调色盘样式集合, 不同设备所支持的样式能力有所不同",

        std::string control_source ="";                        // 负载控制权;
        std::string firmware_version = "";                     // 负载固件版本;
        std::string sn = "";                                   // 负载sn;


        AIGC_JSON_HELPER(
            payload_index,
            gimbal_pitch, gimbal_roll, gimbal_yaw,
            measure_target_altitude, measure_target_longitude, measure_target_latitude, measure_target_distance, measure_target_error_state,
            smart_track_points,
            zoom_factor,
            thermal_current_palette_style, thermal_gain_mode, thermal_global_temperature_min, thermal_global_temperature_max,
            thermal_isotherm_state, thermal_isotherm_lower_limit, thermal_isotherm_upper_limit,
            thermal_supported_palette_styles,
            control_source, firmware_version, sn
        );


        explicit operator AircraftPayloadsOsdInfo() const {
            AircraftPayloadsOsdInfo res{};
            res.smart_track_points.reserve(smart_track_points.size());

            res.payload_index = payload_index;

            res.gimbal_pitch = gimbal_pitch;
            res.gimbal_roll = gimbal_roll;
            res.gimbal_yaw = gimbal_yaw;
            
            res.measure_target_altitude = measure_target_altitude;
            res.measure_target_longitude = measure_target_longitude;
            res.measure_target_latitude = measure_target_latitude;
            res.measure_target_distance = measure_target_distance;
            res.measure_target_error_state = measure_target_error_state;

            // smart track 点信息;
            for (const auto &smart_track_point_info_msg : smart_track_points) {
                res.smart_track_points.emplace_back(static_cast<SmartTrackPointInfo>(smart_track_point_info_msg));
            }

            res.zoom_factor = zoom_factor;
            res.thermal_current_palette_style = thermal_current_palette_style;
            res.thermal_gain_mode = thermal_gain_mode;
            res.thermal_global_temperature_min = thermal_global_temperature_min;
            res.thermal_global_temperature_max = thermal_global_temperature_max;
            res.thermal_isotherm_state = thermal_isotherm_state;
            res.thermal_isotherm_lower_limit = thermal_isotherm_lower_limit;
            res.thermal_isotherm_upper_limit = thermal_isotherm_upper_limit;

            res.thermal_supported_palette_styles = thermal_supported_palette_styles;
            
            res.control_source = control_source;
            res.firmware_version = firmware_version;
            res.sn = sn;

            return res;
        }
    };


    // 飞机负载信息列表;
    // AircraftPayloadsOsdInfoList
    class AircraftPayloadsOsdInfoListMsg {
    public:
        std::vector<AircraftPayloadsOsdInfoMsg> payloads_info_list;


        AIGC_JSON_HELPER(payloads_info_list);

        explicit operator AircraftPayloadsOsdInfoList() const {
            AircraftPayloadsOsdInfoList res{};
            res.payloads_info_list.reserve(payloads_info_list.size());

            for (const auto &payloads_info_msg : payloads_info_list) {
                res.payloads_info_list.emplace_back(static_cast<AircraftPayloadsOsdInfo>(payloads_info_msg));
            }

            return res;
        }
    };


    // topic 定义;
    class PublishAircraftPayloadsOsdInfoTopic: public PublishTopicWrapper {
    public:
        AircraftPayloadsOsdInfoListMsg msg;

        [[nodiscard]] AircraftPayloadsOsdInfoList getPodClass() const {
            return static_cast<AircraftPayloadsOsdInfoList>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftPayloadsOsdInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftPayloadsOsdInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
}  // notification_center;