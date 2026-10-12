/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fh_drone_snapshot — 获取无人机实时状态快照。
 * 订阅多个遥测通道，从每个通道采集一个数据点后取消订阅。
 * 一次性返回位置、电量、姿态、速度、飞行模式等信息。
 */
@Component
public class FhDroneSnapshotTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDroneSnapshotTool.class);

    private final SdkService sdkService;

    public FhDroneSnapshotTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_drone_snapshot");
        def.put("description", "Get real-time drone status snapshot: position, battery, attitude, speed, flight mode. "
                + "Subscribes briefly to telemetry channels and returns current values. "
                + "For richer in-flight telemetry, prefer fh_osd_snapshot.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "drone_sn", Map.of(
                        "type", "string",
                        "description", "Target drone SN (serial number)"
                )
        ));
        schema.put("required", List.of("drone_sn"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String droneSn = (String) args.get("drone_sn");
        if (droneSn == null || droneSn.isBlank()) {
            return errorResult("Missing drone_sn", "Provide the drone serial number.");
        }

        Drone drone = sdkService.getDrone(droneSn);

        if (sdkService.isMockMode() || drone == null) {
            // Mock 模式：返回模拟快照数据
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("drone_sn", droneSn);
            snapshot.put("latitude", 22.5431);
            snapshot.put("longitude", 113.9465);
            snapshot.put("height", 120.5);
            snapshot.put("battery_percent", 87);
            snapshot.put("pitch", -2.3);
            snapshot.put("roll", 0.5);
            snapshot.put("yaw", 145.2);
            snapshot.put("horizontal_speed", 0.0);
            snapshot.put("vertical_speed", 0.0);
            snapshot.put("_mock", true);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", snapshot);
            result.put("message", String.format("[MOCK] Drone %s: battery=87%%, alt=120.5m, location=22.54N,113.95E", droneSn));
            result.put("next_steps", List.of("Call fh_control_acquire(drone_sn) to take control"));
            return result;
        }

        try {
            // ConcurrentHashMap：4 个 SDK 回调在不同线程上并发写入
            Map<String, Object> snapshot = new ConcurrentHashMap<>();
            snapshot.put("drone_sn", droneSn);

            // 使用一次性订阅模式采集遥测数据
            CountDownLatch latch = new CountDownLatch(4); // 位置、电量、姿态、速度

            // 位置
            int locId = drone.getDroneOsdModule().listenAircraftLocation(location -> {
                snapshot.put("longitude", location.getLongitude());
                snapshot.put("latitude", location.getLatitude());
                snapshot.put("height", location.getHeight());
                snapshot.put("elevation", location.getElevation());
                latch.countDown();
            });

            // 电量
            int batId = drone.getDroneOsdModule().listenAircraftBatteryInfo(battery -> {
                snapshot.put("battery_percent", battery.getCapacityPercent());
                snapshot.put("battery_landing_power", battery.getLandingPower());
                latch.countDown();
            });

            // 姿态
            int attId = drone.getDroneOsdModule().listenAircraftAttitude(attitude -> {
                snapshot.put("pitch", attitude.getAttitudePitch());
                snapshot.put("roll", attitude.getAttitudeRoll());
                snapshot.put("yaw", attitude.getAttitudeHead());
                latch.countDown();
            });

            // 速度
            int spdId = drone.getDroneOsdModule().listenAircraftSpeed(speed -> {
                snapshot.put("horizontal_speed", speed.getHorizontalSpeed());
                snapshot.put("vertical_speed", speed.getVerticalSpeed());
                latch.countDown();
            });

            // 等待所有通道返回数据（最多 10 秒）
            boolean allReceived;
            try {
                allReceived = latch.await(10, TimeUnit.SECONDS);
            } finally {
                // 无论成功或超时/异常，必须取消所有订阅
                if (locId >= 0) {
                    drone.getDroneOsdModule().cancelListenAircraftLocation(locId);
                }
                if (batId >= 0) {
                    drone.getDroneOsdModule().cancelListenAircraftBatteryInfo(batId);
                }
                if (attId >= 0) {
                    drone.getDroneOsdModule().cancelListenAircraftAttitude(attId);
                }
                if (spdId >= 0) {
                    drone.getDroneOsdModule().cancelListenAircraftSpeed(spdId);
                }
            }

            if (!allReceived) {
                snapshot.put("_warning", "Some telemetry channels did not respond within 10s. Data may be partial.");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", snapshot);
            result.put("message", String.format("Drone %s snapshot: battery=%s%%, alt=%sm",
                    droneSn,
                    snapshot.getOrDefault("battery_percent", "N/A"),
                    snapshot.getOrDefault("altitude", "N/A")));
            result.put("next_steps", List.of(
                    "Call fh_control_acquire(drone_sn) to take control",
                    "Call fh_device_manage(action='hms_current', device_sn='" + droneSn + "') for health alerts"
            ));
            return result;

        } catch (Exception e) {
            log.error("fh_drone_snapshot error for {}: {}", droneSn, e.getMessage(), e);
            return errorResult("Snapshot failed", e.getMessage());
        }
    }
}
