// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include <AIGCJson.hpp>
#include "api/message.h"

namespace flight_hub_sdk {

struct JsonFormattedSeiInfoCameraFov {
    int32_t image_width = 0;
    int32_t image_height = 0;
    int32_t image_ratio = 0;
    int32_t lens_focal_length = 0;
    AIGC_JSON_HELPER(image_width, image_height, image_ratio, lens_focal_length);
};

struct JsonFormattedSeiInfo {
    std::string timestamp = "";
    uint64_t time = 0;
    int32_t cam_yaw = 0;
    int32_t cam_pitch = 0;
    int32_t cam_roll = 0;
    int32_t gnd_alt = 0;
    int32_t gnd_lat = 0;
    int32_t gnd_lng = 0;
    std::vector<int32_t> camera_pos_low;
    std::vector<int32_t> camera_pos_high;
    JsonFormattedSeiInfoCameraFov cam_fov;
    int32_t liveview_source_cam = 0;
    int32_t liveview_display_mode = 0;
    int32_t liveview_pip_para_set = 0;  
    AIGC_JSON_HELPER(timestamp, time, cam_yaw, cam_pitch, cam_roll, gnd_alt, gnd_lat, gnd_lng, camera_pos_low, camera_pos_high, cam_fov, liveview_source_cam, liveview_display_mode, liveview_pip_para_set);
};

class JsonFormattedSeiParser {
public:
    JsonFormattedSeiParser(const std::string& sei_buffer);
    ~JsonFormattedSeiParser();

    bool parse();

    bool is_fov_sei_info_valid() const;
    FovSeiInfo get_camera_fov_sei_info() const;

    bool is_ar_sei_info_valid() const;
    ArSeiInfo get_ar_sei_info() const;
private:
    std::string sei_buffer_;
    JsonFormattedSeiInfo json_formatted_sei_info_;
};
}

/* Formatted SEI
* {
*     "timestamp": "13:41:43.565",
*     "time": 1753710103565,
*     "cam_yaw": 833763924,
*     "cam_pitch": -6147,
*     "cam_roll": 901038009,
*     "gnd_alt": -272054,
*     "gnd_lat": 0,
*     "gnd_lng": 0,
*     "camera_pos_low": [
*         18,
*         65513,
*         55626
*     ],
*     "camera_pos_high": [
*         0,
*         65535,
*         65531
*     ],
*     "cam_fov": {
*         "image_width": 9652,
*         "image_height": 7239,
*         "image_ratio": 0,
*         "lens_focal_length": 6720
*     },
*     "liveview_source_cam": 1,
*     "liveview_display_mode": 1,
*     "liveview_pip_para_set": 9
* }
*/