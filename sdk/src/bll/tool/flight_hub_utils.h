// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <unordered_set>
#include <unordered_map>
#include "api/value.h"

namespace flight_hub_sdk {

static const std::unordered_set<DroneModeCode> drone_in_sky_mode_code_set_ = {
    DroneModeCode::DroneModeCode_ManualFlight,
    DroneModeCode::DroneModeCode_AutomaticTakeoff,
    DroneModeCode::DroneModeCode_RouteFlight,
    DroneModeCode::DroneModeCode_PanoramicPhoto,
    DroneModeCode::DroneModeCode_SmartFollow,
    DroneModeCode::DroneModeCode_ADSBAvoidance,
    DroneModeCode::DroneModeCode_ReturnHomeAutomatically,
    DroneModeCode::DroneModeCode_AutomaticLanding,
    DroneModeCode::DroneModeCode_ForcedLanding,
    DroneModeCode::DroneModeCode_LandingWithThreeBlades,
    DroneModeCode::DroneModeCode_APAS,
    DroneModeCode::DroneModeCode_Joystick,
    DroneModeCode::DroneModeCode_CommandFlight,
    DroneModeCode::DroneModeCode_POI,
    DroneModeCode::DroneModeCode_DeparturePoint,
    DroneModeCode::DroneModeCode_RTKConverging,
};

inline bool isDroneInSky(DroneModeCode mode_code) {
    return drone_in_sky_mode_code_set_.find(mode_code) != drone_in_sky_mode_code_set_.end();
}

inline std::string cameraLensTypeToString(CameraLensType camera_lens_type) {
    switch (camera_lens_type) {
        case CameraLensType::CameraLensType_NORMAL:
            return "normal";
        case CameraLensType::CameraLensType_WIDE:
            return "wide";
        case CameraLensType::CameraLensType_ZOOM:
            return "zoom";
        case CameraLensType::CameraLensType_IR:
            return "ir";
        case CameraLensType::CameraLensType_THERMAL:
            return "thermal";
        default:
            return "unknown";
    }
}

static const std::unordered_map<std::string, CameraLensType> camera_lens_string_to_enum_map_ = {
    {"normal", CameraLensType::CameraLensType_NORMAL},
    {"wide", CameraLensType::CameraLensType_WIDE},
    {"zoom", CameraLensType::CameraLensType_ZOOM},
    {"ir", CameraLensType::CameraLensType_IR},
    {"thermal", CameraLensType::CameraLensType_THERMAL},
};
inline CameraLensType cameraLensStringToEnum(const std::string& camera_lens_string) {
    if (camera_lens_string_to_enum_map_.find(camera_lens_string) != camera_lens_string_to_enum_map_.end()) {
        return camera_lens_string_to_enum_map_.at(camera_lens_string);
    } else {
        return CameraLensType::CameraLensType_UNKNOWN;
    }
}

static const std::unordered_map<std::string, CameraPhotoState> camera_photo_state_string_to_enum_map_ = {
    {"in_progress", CameraPhotoState::CameraPhotoState_IN_PROGRESS},
    {"ok", CameraPhotoState::CameraPhotoState_OK},
    {"failed", CameraPhotoState::CameraPhotoState_FAILED},
    {"canceled", CameraPhotoState::CameraPhotoState_CANCELED},
};
inline CameraPhotoState cameraPhotoStateStringToEnum(const std::string& camera_photo_state_string) {
    if (camera_photo_state_string_to_enum_map_.find(camera_photo_state_string) != camera_photo_state_string_to_enum_map_.end()) {
        return camera_photo_state_string_to_enum_map_.at(camera_photo_state_string);
    } else {
        return CameraPhotoState::CameraPhotoState_UNKNOWN;
    }
}
} // namespace flight_hub_sdk