// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.fhsdk;

import com.dji.flighthubsdk.bean.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FHSDK Bean → 前端 JSON 字段映射（与 osd store / useOsdTable 列名对齐）。
 */
public final class TelemetryMapper {

    private TelemetryMapper() {}

    public static Map<String, Object> location(AircraftLocation loc) {
        return Map.of(
                "latitude", loc.getLatitude(),
                "longitude", loc.getLongitude(),
                "height", loc.getHeight(),
                "elevation", loc.getElevation()
        );
    }

    public static Map<String, Object> attitude(AircraftAttitude att) {
        return Map.of(
                "attitudeHead", att.getAttitudeHead(),
                "attitudePitch", att.getAttitudePitch(),
                "attitudeRoll", att.getAttitudeRoll()
        );
    }

    public static Map<String, Object> speed(AircraftSpeed spd) {
        return Map.of(
                "horizontalSpeed", spd.getHorizontalSpeed(),
                "verticalSpeed", spd.getVerticalSpeed()
        );
    }

    public static Map<String, Object> battery(AircraftBatteryInfo bat) {
        return Map.of(
                "capacityPercent", bat.getCapacityPercent(),
                "remainFlightTime", bat.getRemainFlightTime(),
                "returnHomePower", bat.getReturnHomePower(),
                "landingPower", bat.getLandingPower()
        );
    }

    public static Map<String, Object> wind(AircraftWindSpeed wind) {
        return Map.of(
                "windSpeed", wind.getWindSpeed(),
                "windDirection", wind.getWindDirection()
        );
    }

    public static Map<String, Object> dockLocation(DockLocation loc) {
        return Map.of(
                "latitude", loc.getLatitude(),
                "longitude", loc.getLongitude(),
                "height", loc.getHeight(),
                "heading", loc.getHeading()
        );
    }

    public static Map<String, Object> gimbalAttitude(AircraftPayloadsGimbalAttitude gimbal) {
        List<Map<String, Object>> payloads = new ArrayList<>();
        if (gimbal.getPayloadsGimbalAttitude() != null) {
            for (AircraftPayloadGimbalAttitude p : gimbal.getPayloadsGimbalAttitude()) {
                Map<String, Object> item = new HashMap<>();
                item.put("payloadIndex", nullSafe(p.getPayloadIndex()));
                item.put("gimbalPitch", p.getGimbalPitch());
                item.put("gimbalRoll", p.getGimbalRoll());
                item.put("gimbalYaw", p.getGimbalYaw());
                payloads.add(item);
            }
        }
        return Map.of("payloads", payloads);
    }

    public static Map<String, Object> measureTarget(CameraMeasureTarget t) {
        return Map.of(
                "payloadIndex", nullSafe(t.getPayloadIndex()),
                "latitude", t.getLatitude(),
                "longitude", t.getLongitude(),
                "altitude", t.getAltitude(),
                "distance", t.getDistance(),
                "laserMeasureState", t.getLaserMeasureState()
        );
    }

    public static Map<String, Object> measureTargetFromOsd(AircraftPayloadsOsdInfo osd) {
        return Map.of(
                "payloadIndex", nullSafe(osd.getPayloadIndex()),
                "latitude", osd.getMeasureTargetLatitude(),
                "longitude", osd.getMeasureTargetLongitude(),
                "altitude", osd.getMeasureTargetAltitude(),
                "distance", osd.getMeasureTargetDistance(),
                "laserMeasureState", (int) osd.getMeasureTargetErrorState()
        );
    }

    public static Map<String, Object> sei(ArSeiInfo ar, FovSeiInfo fov) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        if (ar != null) {
            map.put("cameraYaw", ar.getCameraYaw());
            map.put("cameraPitch", ar.getCameraPitch());
            map.put("cameraRoll", ar.getCameraRoll());
            map.put("latitude", ar.getLatitude());
            map.put("longitude", ar.getLongitude());
            map.put("altitude", ar.getAltitude());
        }
        if (fov != null) {
            map.put("fovHorizontal", fov.getFovHorizontal());
            map.put("fovVertical", fov.getFovVertical());
            map.put("curActualFocalLength", fov.getCurActualFocalLength());
        }
        return map;
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }
}
