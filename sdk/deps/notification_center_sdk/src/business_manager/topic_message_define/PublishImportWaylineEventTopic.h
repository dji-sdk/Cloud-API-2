// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"
#include <base/common/logger.h>
#include <unordered_map>

namespace notification_center {

class PointsItemMsg {
public:
    double lng = 0.0;
    double lat = 0.0;
    double alt = 0.0;

    AIGC_JSON_HELPER(lng, lat, alt);

    explicit operator PointsItem() const {
        PointsItem info{};
        info.lng = lng;
        info.lat = lat;
        info.alt = alt;
        return info;
    }
};

class SurveyAreaScopeMsg {
public:
    std::vector<PointsItemMsg> points;

    AIGC_JSON_HELPER(points);

    explicit operator SurveyAreaScope() const {
        SurveyAreaScope info{};
        for (const auto& item : points) {
            info.points.push_back(static_cast<PointsItem>(item));
        }
        return info;
    }
};

class KmzDataMsg {
public:
    int wayline_type = 0;
    int wayline_point_count = 0;
    int exit_on_rc_lost_type = 0;
    int exit_on_rc_lost_behavior = 0;
    int start_record_action_count = 0;
    int stop_record_action_count = 0;
    int ortho_camera_overlap_h = 0;
    int ortho_camera_overlap_w = 0;
    int inclined_camera_overlap_h = 0;
    int inclined_camera_overlap_w = 0;
    int supplement_prism_model_count = 0;
    int estimated_photo_count = 0;
    double duration = 0.0;
    double distance = 0.0;
    double survey_area = 0;
    double start_latitude = 0.0;
    double start_longitude = 0.0;
    double start_altitude = 0.0;
    double margin_distance = 0;
    double ortho_gsd = 0;
    double incline_gsd = 0;
    bool is_wayline_validity_check = false;
    bool has_oriented_action = false;
    bool has_accurate_action = false;
    bool is_surface_fly_on = false;
    bool is_elevation_optimization_on = false;
    bool is_smart_oblique_on = false;
    bool is_mapping_strip_use_template_altitude = false;
    bool is_with_megaphone = false;
    bool is_with_search_light = false;
    bool is_ai_wayline = false;
    std::string drone_type;
    std::string target_detect_area_file_path;
    std::vector<int> wayline_validity_check_codes;
    std::vector<std::string> elevation_file_names;
    std::vector<std::string> payload_types;
    std::vector<std::string> payload_types_with_index;
    std::vector<std::string> accurate_action_uuids;
    std::vector<std::string> action_uuids;
    std::vector<std::string> res_file_md5s;
    std::vector<std::string> audio_file_md5s;
    std::vector<std::string> target_detect_action_uuids;
    std::vector<std::string> ai_audio_file_md5s;
    std::unordered_map<std::string, std::string> action_uuid_to_md5_map;
    std::unordered_map<std::string, std::string> audio_action_uuid_to_file_path_map;
    std::unordered_map<std::string, std::string> ai_audio_action_uuid_to_file_path_map;
    SurveyAreaScopeMsg survey_area_scope;

    AIGC_JSON_HELPER(wayline_type, wayline_point_count, exit_on_rc_lost_type,
        exit_on_rc_lost_behavior, start_record_action_count, stop_record_action_count,
        ortho_camera_overlap_h, ortho_camera_overlap_w,
        inclined_camera_overlap_h, inclined_camera_overlap_w,
        supplement_prism_model_count, estimated_photo_count,
        duration, distance, survey_area,
        start_latitude, start_longitude, start_altitude,
        margin_distance, ortho_gsd, incline_gsd,
        is_wayline_validity_check, has_oriented_action, has_accurate_action,
        is_surface_fly_on, is_elevation_optimization_on, is_smart_oblique_on,
        is_mapping_strip_use_template_altitude,
        is_with_megaphone, is_with_search_light, is_ai_wayline,
        drone_type, target_detect_area_file_path,
        wayline_validity_check_codes, elevation_file_names,
        payload_types, payload_types_with_index,
        accurate_action_uuids, action_uuids,
        res_file_md5s, audio_file_md5s,
        target_detect_action_uuids, ai_audio_file_md5s,
        action_uuid_to_md5_map, audio_action_uuid_to_file_path_map,
        ai_audio_action_uuid_to_file_path_map, survey_area_scope);

    explicit operator KmzData() const {
        KmzData info{};
        info.wayline_type = wayline_type;
        info.wayline_point_count = wayline_point_count;
        info.exit_on_rc_lost_type = exit_on_rc_lost_type;
        info.exit_on_rc_lost_behavior = exit_on_rc_lost_behavior;
        info.start_record_action_count = start_record_action_count;
        info.stop_record_action_count = stop_record_action_count;
        info.ortho_camera_overlap_h = ortho_camera_overlap_h;
        info.ortho_camera_overlap_w = ortho_camera_overlap_w;
        info.inclined_camera_overlap_h = inclined_camera_overlap_h;
        info.inclined_camera_overlap_w = inclined_camera_overlap_w;
        info.supplement_prism_model_count = supplement_prism_model_count;
        info.estimated_photo_count = estimated_photo_count;
        info.duration = duration;
        info.distance = distance;
        info.survey_area = survey_area;
        info.start_latitude = start_latitude;
        info.start_longitude = start_longitude;
        info.start_altitude = start_altitude;
        info.margin_distance = margin_distance;
        info.ortho_gsd = ortho_gsd;
        info.incline_gsd = incline_gsd;
        info.is_wayline_validity_check = is_wayline_validity_check;
        info.has_oriented_action = has_oriented_action;
        info.has_accurate_action = has_accurate_action;
        info.is_surface_fly_on = is_surface_fly_on;
        info.is_elevation_optimization_on = is_elevation_optimization_on;
        info.is_smart_oblique_on = is_smart_oblique_on;
        info.is_mapping_strip_use_template_altitude = is_mapping_strip_use_template_altitude;
        info.is_with_megaphone = is_with_megaphone;
        info.is_with_search_light = is_with_search_light;
        info.is_ai_wayline = is_ai_wayline;
        info.drone_type = drone_type;
        info.target_detect_area_file_path = target_detect_area_file_path;
        info.wayline_validity_check_codes = wayline_validity_check_codes;
        info.elevation_file_names = elevation_file_names;
        info.payload_types = payload_types;
        info.payload_types_with_index = payload_types_with_index;
        info.accurate_action_uuids = accurate_action_uuids;
        info.action_uuids = action_uuids;
        info.res_file_md5s = res_file_md5s;
        info.audio_file_md5s = audio_file_md5s;
        info.target_detect_action_uuids = target_detect_action_uuids;
        info.ai_audio_file_md5s = ai_audio_file_md5s;
        info.action_uuid_to_md5_map = action_uuid_to_md5_map;
        info.audio_action_uuid_to_file_path_map = audio_action_uuid_to_file_path_map;
        info.ai_audio_action_uuid_to_file_path_map = ai_audio_action_uuid_to_file_path_map;
        info.survey_area_scope = static_cast<SurveyAreaScope>(survey_area_scope);
        return info;
    }
};

class ExtDataMsg {
public:
    std::string kmz_without_res_object_key;
    std::string kmz_without_res_md5;
    std::string target_area_file_object_key;
    std::string business_file_object_key;
    std::vector<std::string> model_file_object_keys;

    AIGC_JSON_HELPER(kmz_without_res_object_key, kmz_without_res_md5,
        target_area_file_object_key, business_file_object_key,
        model_file_object_keys);

    explicit operator ExtData() const {
        ExtData info{};
        info.kmz_without_res_object_key = kmz_without_res_object_key;
        info.kmz_without_res_md5 = kmz_without_res_md5;
        info.target_area_file_object_key = target_area_file_object_key;
        info.business_file_object_key = business_file_object_key;
        info.model_file_object_keys = model_file_object_keys;
        return info;
    }
};

class ImportWaylineEventMsg {
public:
    std::string async_request_id;
    long long timestamp = 0;
    std::string kmz_object_key;
    std::string wayline_uuid;
    int code = 0;
    std::string message;
    KmzDataMsg kmz_data;
    ExtDataMsg ext_data;

    AIGC_JSON_HELPER(async_request_id, timestamp, kmz_object_key, wayline_uuid, code, message, kmz_data, ext_data);

    explicit operator ImportWaylineEvent() const {
        ImportWaylineEvent info{};
        info.async_request_id = async_request_id;
        info.timestamp = timestamp;
        info.kmz_object_key = kmz_object_key;
        info.wayline_uuid = wayline_uuid;
        info.code = code;
        info.message = message;
        info.kmz_data = static_cast<KmzData>(kmz_data);
        info.ext_data = static_cast<ExtData>(ext_data);
        return info;
    }
};

class PublishImportWaylineEventTopic : public PublishTopicWrapper {
public:
    ImportWaylineEventMsg msg;

    [[nodiscard]] ImportWaylineEvent getPodClass() const {
        return static_cast<ImportWaylineEvent>(msg);
    }

    bool isValid() override { return is_valid_; }

    explicit PublishImportWaylineEventTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
        message_topic = publish_msg->message_topic;
        message_data  = publish_msg->message_data;
        need_replay   = publish_msg->need_replay;
        message_id    = publish_msg->message_id;
        message_type  = publish_msg->message_type;
        timestamp     = publish_msg->timestamp;
        version       = publish_msg->version;
        device_sn     = publish_msg->device_sn;
        subscribe_id  = publish_msg->subscribe_id;

        std::string err;
        aigc::JsonHelper::JsonToObject(msg, publish_msg->message_data, {}, &err);
        if (!err.empty()) {
            LOG_ERROR("PublishImportWaylineEventTopic ctor error, err: %s, json: %s",
                      err.c_str(), publish_msg->ToJsonString().c_str());
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

} // namespace notification_center
