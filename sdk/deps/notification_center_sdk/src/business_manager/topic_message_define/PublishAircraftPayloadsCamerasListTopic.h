// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 红外测温点相关信息;
    // IrMeteringPoint;
    class IrMeteringPointMsg {
    public:
        double temperature = 0.0;    // double, 温度;
        double x = 0.0;              // double, x坐标(0.0~1.0);
        double y = 0.0;              // double, y坐标(0.0~1.0);


        AIGC_JSON_HELPER(temperature, x, y);

        explicit operator IrMeteringPoint() const {
            IrMeteringPoint res{};

            res.temperature = temperature;
            res.x = x;
            res.y = y;

            return res;
        }
    };

    // Liveview 世界区域信息;
    // LiveviewWorldRegion;
    class LiveviewWorldRegionMsg {
    public:
        double bottom = 0.0;
        double left = 0.0;
        double right = 0.0;
        double top = 0.0;

 
        AIGC_JSON_HELPER(bottom, left, right, top);

        explicit operator LiveviewWorldRegion() const {
            LiveviewWorldRegion res{};

            res.bottom = bottom;
            res.left = left;
            res.right = right;
            res.top = top;

            return res;
        }
    };


    // 飞机相机负载信息;
    // AircraftPayloadCameraInfo;
    class AircraftPayloadCameraInfoMsg {
    public:
        std::string payload_index = "";    // "payload_index": "81-0-0";
        int camera_mode = 0;               // 相机模式, {0: 拍照, 1: 录像, 2: 低光智能, 3: 全景拍照, -1: 不支持的模式};
        int ir_metering_mode = 0;
        IrMeteringPointMsg ir_metering_point;
        double ir_zoom_factor = 0;
        LiveviewWorldRegionMsg liveview_world_region;
        int photo_state = 0;
        std::vector<std::string> photo_storage_settings;
        int record_time = 0;         // 视频录制时长, 单位：秒(s);
        int recording_state = 0;
        int remain_photo_num = 0;    // 剩余拍照张数;
        int remain_record_duration = 0;
        bool screen_split_enable = false;

        int wide_calibrate_farthest_focus_value = 0;
        int wide_calibrate_nearest_focus_value = 0;
        int wide_exposure_mode = 0;
        int wide_exposure_value = 0;
        int wide_focus_mode = 0;
        int wide_focus_state = 0;
        int wide_focus_value = 0;
        int wide_iso = 0;
        int wide_max_focus_value = 0;
        int wide_min_focus_value = 0;
        int wide_shutter_speed = 0;

        int zoom_calibrate_farthest_focus_value = 0;
        int zoom_calibrate_nearest_focus_value = 0;
        int zoom_exposure_mode = 0;
        int zoom_exposure_value = 0;
        double zoom_factor = 0;
        int zoom_focus_mode = 0;
        int zoom_focus_state = 0;
        int zoom_focus_value = 0;
        int zoom_iso = 0;
        int zoom_max_focus_value = 0;
        int zoom_min_focus_value = 0;
        int zoom_shutter_speed = 0;


        AIGC_JSON_HELPER(
            payload_index,
            camera_mode,
            ir_metering_mode,
            ir_metering_point,
            ir_zoom_factor,
            liveview_world_region,
            photo_state,
            photo_storage_settings,
            record_time,
            recording_state,
            remain_photo_num,
            remain_record_duration,
            screen_split_enable,
            wide_calibrate_farthest_focus_value,
            wide_calibrate_nearest_focus_value,
            wide_exposure_mode,
            wide_exposure_value,
            wide_focus_mode,
            wide_focus_state,
            wide_focus_value,
            wide_iso,
            wide_max_focus_value,
            wide_min_focus_value,
            wide_shutter_speed,
            zoom_calibrate_farthest_focus_value,
            zoom_calibrate_nearest_focus_value,
            zoom_exposure_mode,
            zoom_exposure_value,
            zoom_factor,
            zoom_focus_mode,
            zoom_focus_state,
            zoom_focus_value,
            zoom_iso,
            zoom_max_focus_value,
            zoom_min_focus_value,
            zoom_shutter_speed
        );


        explicit operator AircraftPayloadCameraInfo() const {
            AircraftPayloadCameraInfo res{};

            res.payload_index = payload_index;
            res.camera_mode = camera_mode;
            res.ir_metering_mode = ir_metering_mode;
            res.ir_metering_point = static_cast<IrMeteringPoint>(ir_metering_point);
            res.ir_zoom_factor = ir_zoom_factor;
            res.liveview_world_region = static_cast<LiveviewWorldRegion>(liveview_world_region);
            res.photo_state = photo_state;
            res.photo_storage_settings = photo_storage_settings;
            res.record_time = record_time;
            res.recording_state = recording_state;
            res.remain_photo_num = remain_photo_num;
            res.remain_record_duration = remain_record_duration;
            res.screen_split_enable = screen_split_enable;
            res.wide_calibrate_farthest_focus_value = wide_calibrate_farthest_focus_value;
            res.wide_calibrate_nearest_focus_value = wide_calibrate_nearest_focus_value;
            res.wide_exposure_mode = wide_exposure_mode;
            res.wide_exposure_value = wide_exposure_value;
            res.wide_focus_mode = wide_focus_mode;
            res.wide_focus_state = wide_focus_state;
            res.wide_focus_value = wide_focus_value;
            res.wide_iso = wide_iso;
            res.wide_max_focus_value = wide_max_focus_value;
            res.wide_min_focus_value = wide_min_focus_value;
            res.wide_shutter_speed = wide_shutter_speed;
            res.zoom_calibrate_farthest_focus_value = zoom_calibrate_farthest_focus_value;
            res.zoom_calibrate_nearest_focus_value = zoom_calibrate_nearest_focus_value;
            res.zoom_exposure_mode = zoom_exposure_mode;
            res.zoom_exposure_value = zoom_exposure_value;
            res.zoom_factor = zoom_factor;
            res.zoom_focus_mode = zoom_focus_mode;
            res.zoom_focus_state = zoom_focus_state;
            res.zoom_focus_value = zoom_focus_value;
            res.zoom_iso = zoom_iso;
            res.zoom_max_focus_value = zoom_max_focus_value;
            res.zoom_min_focus_value = zoom_min_focus_value;
            res.zoom_shutter_speed = zoom_shutter_speed;

            return res;
        }
    };


    // 飞机相机负载信息列表;
    // AircraftPayloadsCamerasList;
    class AircraftPayloadCameraListMsg {
    public:
        std::vector<AircraftPayloadCameraInfoMsg> cameras_list;


        AIGC_JSON_HELPER(cameras_list);


        explicit operator AircraftPayloadsCamerasList() const {
            AircraftPayloadsCamerasList res{};
            res.cameras_list.reserve(cameras_list.size());

            for (const auto &item : cameras_list) {
                res.cameras_list.emplace_back(static_cast<AircraftPayloadCameraInfo>(item));
            }


            return res;
        }
    };



    // topic definition;
    class PublishAircraftPayloadsCamerasListTopic: public PublishTopicWrapper {
    public:
        AircraftPayloadCameraListMsg msg;

        [[nodiscard]] AircraftPayloadsCamerasList getPodClass() const {
            return static_cast<AircraftPayloadsCamerasList>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftPayloadsCamerasListTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftPayloadsCamerasListTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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


