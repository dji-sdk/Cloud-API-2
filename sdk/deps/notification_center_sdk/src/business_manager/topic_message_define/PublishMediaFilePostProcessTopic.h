// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_media_file_post_process";
    // 设备媒体后处理完成通知;

    // PositionData;
    class PositionDataMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        double lat = 0.0;         // 纬度;
        double lng = 0.0;         // 经度;
        double abs_alt = 0.0;     // 高度;
        double rel_alt = 0.0;     // 相对高度;

        // register class members;
        AIGC_JSON_HELPER(lat, lng, abs_alt, rel_alt);


        // explicit type cast;
        explicit operator PositionData() const {
            PositionData res{};

            res.lat = lat;
            res.lng = lng;
            res.abs_alt = abs_alt;
            res.rel_alt = rel_alt;

            return res;
        }
    };


    // MediaFileVideoMetaData;
    class MediaFileVideoMetaDataMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        double duration = -1;           // 视频时长(单位：秒)，示例值：26.927;
        std::string encoder;            // 视频编码器，示例值：Lavf58.12.100;
        std::string format_long_name;   // 视频格式名称，示例值：QuickTime / MOV;
        std::string format_name;        // 示例值："mov,mp4,m4a,3gp,3g2,mj2";
        int height = -1;
        int width = -1;

        // register class members;
        AIGC_JSON_HELPER(duration, encoder, format_long_name, format_name, height, width);

        // explicit type cast;
        explicit operator MediaFileVideoMetaData() const {
            MediaFileVideoMetaData res{};

            res.duration = duration;
            res.encoder = encoder;
            res.format_long_name = format_long_name;
            res.format_name = format_name;
            res.height = height;
            res.width = width;

            return res;
        }
    };


    // MediaFileImageMetaData;
    class MediaFileImageMetaDataMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        std::string photo_create_time;
        double flight_yaw = -1;
        double focal_length = -1;
        double gimbal_pitch = -1;
        double gimbal_roll = -1;
        double gimbal_yaw = -1;
        int height = -1;
        int width = -1;
        double zoom_factor = -1;
        double digital_zoom_ratio = -1;
        double length_in_35mm_film = -1;  // 35mm胶片等效焦距，示例值：50;

        std::string image_type;           // 图片类型; {广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"Panorama"}
        std::string payload_type;
        std::string action_uuid;
        PositionDataMsg photo_position;
        PositionDataMsg shoot_position;

        // register class members;
        AIGC_JSON_HELPER(
            photo_create_time, flight_yaw, focal_length, gimbal_pitch, gimbal_roll, gimbal_yaw, height, width,
            zoom_factor, digital_zoom_ratio, length_in_35mm_film, image_type, payload_type, action_uuid, photo_position, shoot_position
        );


        // explicit type cast;
        explicit operator MediaFileImageMetaData() const {
            MediaFileImageMetaData res{};

            res.photo_create_time = photo_create_time;
            res.flight_yaw = flight_yaw;
            res.focal_length = focal_length;
            res.gimbal_pitch = gimbal_pitch;
            res.gimbal_roll = gimbal_roll;
            res.gimbal_yaw = gimbal_yaw;
            res.height = height;
            res.width = width;
            res.zoom_factor = zoom_factor;
            res.digital_zoom_ratio = digital_zoom_ratio;
            res.length_in_35mm_film = length_in_35mm_film;

            res.image_type = image_type;
            res.payload_type = payload_type;
            res.action_uuid = action_uuid;

            // use type cast to convert PositionDataMsg to PositionData;
            res.photo_position = static_cast<PositionData>(photo_position);
            res.shoot_position = static_cast<PositionData>(shoot_position);

            return res;
        }
    };


    // 设备媒体后处理完成通知;
    // MediaFilePostProcess;
    class MediaFilePostProcessMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        int64_t file_id = -1;                      // 文件 id;
        std::string flight_id;                     // 飞行任务 id;
        std::string device_sn;                     // 设备序列号 SN;
        std::string object_key;                    // 文件对象 key(媒体文件在对象存储中的唯一标识);
        int64_t object_size = 0;                   // 文件大小(字节);
        int file_type = -1;                        // 文件类型，2：图片、3：视频;
        std::string preview_object_key;            // 预览文件对象 key(媒体文件在对象存储中的唯一标识);
        MediaFileImageMetaDataMsg image_metadata;  // 图片元数据;
        MediaFileVideoMetaDataMsg video_metadata;  // 视频元数据;

        // register class members;
        AIGC_JSON_HELPER(file_id, flight_id, device_sn, object_key, object_size, file_type, preview_object_key, image_metadata, video_metadata);

        // explicit type cast;
        explicit operator MediaFilePostProcess() const {
            MediaFilePostProcess res{};

            res.file_id = file_id;
            res.flight_id = flight_id;
            res.device_sn = device_sn;
            res.object_key = object_key;
            res.object_size = object_size;
            res.file_type = file_type;
            res.preview_object_key = preview_object_key;

            // use type cast to convert MediaFileImageMetaDataMsg to MediaFileImageMetaData;
            res.image_metadata = static_cast<MediaFileImageMetaData>(image_metadata);
            // use type cast to convert MediaFileVideoMetaDataMsg to MediaFileVideoMetaData;
            res.video_metadata = static_cast<MediaFileVideoMetaData>(video_metadata);

            return res;
        }
    };


    // topic define;
    class PublishMediaFilePostProcessTopic: public PublishTopicWrapper {
    public:
        MediaFilePostProcessMsg msg;

        [[nodiscard]] MediaFilePostProcess getPodClass() const {
            return static_cast<MediaFilePostProcess>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishMediaFilePostProcessTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishMediaFilePostProcessTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

