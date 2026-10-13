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
import com.dji.flighthubsdk.core.Dock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fh_dock_snapshot — 获取机场实时状态快照。
 * 订阅机场遥测通道（位置、舱盖、电源、存储、无线链路），
 * 从每个通道采集一个数据点后取消订阅。
 */
@Component
public class FhDockSnapshotTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDockSnapshotTool.class);

    private final SdkService sdkService;

    public FhDockSnapshotTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_dock_snapshot");
        def.put("description", "Get real-time dock status snapshot: location, cover state, power, storage, "
                + "wireless link quality, and drone-in-dock status. "
                + "Use this to obtain dock GPS coordinates when the user refers to a named location "
                + "(e.g. 'the dock', 'the airport') instead of providing raw lat/lng.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "dock_sn", Map.of(
                        "type", "string",
                        "description", "Target dock SN (serial number)"
                )
        ));
        schema.put("required", List.of("dock_sn"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String dockSn = (String) args.get("dock_sn");
        if (dockSn == null || dockSn.isBlank()) {
            return errorResult("Missing dock_sn", "Provide the dock serial number.");
        }

        Dock dock = sdkService.getDock(dockSn);

        if (sdkService.isMockMode() || dock == null) {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("dock_sn", dockSn);
            snapshot.put("latitude", 22.5435);
            snapshot.put("longitude", 113.9470);
            snapshot.put("height", 25.0);
            snapshot.put("heading", 90.0);
            snapshot.put("cover_state", 0);
            snapshot.put("cover_state_desc", "closed");
            snapshot.put("drone_in_dock", 1);
            snapshot.put("_mock", true);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", snapshot);
            result.put("message", String.format("[MOCK] Dock %s: cover=closed, drone=in-dock, location=22.54N,113.95E", dockSn));
            result.put("next_steps", List.of("Call fh_dock_control(dock_sn, action) to control dock"));
            return result;
        }

        try {
            // ConcurrentHashMap：4 个 SDK 回调在不同线程上并发写入
            Map<String, Object> snapshot = new ConcurrentHashMap<>();
            snapshot.put("dock_sn", dockSn);

            CountDownLatch latch = new CountDownLatch(4);

            // 位置
            int locId = dock.getDockOsdModule().listenDockLocation(location -> {
                snapshot.put("latitude", location.getLatitude());
                snapshot.put("longitude", location.getLongitude());
                snapshot.put("height", location.getHeight());
                snapshot.put("heading", location.getHeading());
                latch.countDown();
            });

            // 舱盖状态
            int coverId = dock.getDockOsdModule().listenDockCoverState(coverState -> {
                snapshot.put("cover_state", coverState.getCoverState());
                latch.countDown();
            });

            // 电源状态
            int powerId = dock.getDockOsdModule().listenDockPowerState(powerState -> {
                snapshot.put("power_state", powerState.toString());
                latch.countDown();
            });

            // 飞机在舱状态
            int droneId = dock.getDockOsdModule().listenDroneInDock(droneInDock -> {
                snapshot.put("drone_in_dock", droneInDock.getDroneInDock());
                latch.countDown();
            });

            boolean allReceived;
            try {
                allReceived = latch.await(10, TimeUnit.SECONDS);
            } finally {
                // 无论成功或超时/异常，必须取消所有订阅
                if (locId >= 0) {
                    dock.getDockOsdModule().cancelListenDockLocation(locId);
                }
                if (coverId >= 0) {
                    dock.getDockOsdModule().cancelListenDockCoverState(coverId);
                }
                if (powerId >= 0) {
                    dock.getDockOsdModule().cancelListenDockPowerState(powerId);
                }
                if (droneId >= 0) {
                    dock.getDockOsdModule().cancelListenDroneInDock(droneId);
                }
            }

            if (!allReceived) {
                snapshot.put("_warning", "Some telemetry channels did not respond within 10s.");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", snapshot);
            result.put("message", String.format("Dock %s snapshot collected.", dockSn));
            result.put("next_steps", List.of(
                    "Call fh_dock_control(dock_sn, action) to control dock",
                    "Call fh_device_manage(action='hms_current', device_sn='" + dockSn + "') for health alerts"
            ));
            return result;

        } catch (Exception e) {
            log.error("fh_dock_snapshot error for {}: {}", dockSn, e.getMessage(), e);
            return errorResult("Snapshot failed", e.getMessage());
        }
    }
}
