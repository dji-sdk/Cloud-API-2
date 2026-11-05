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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.AudienceCountGroup;
import com.dji.flighthubsdk.bean.DeviceAudienceCountItem;
import com.dji.flighthubsdk.bean.DockLiveCapacity;
import com.dji.flighthubsdk.bean.DockLiveStatus;
import com.dji.flighthubsdk.bean.LiveCapacityCamera;
import com.dji.flighthubsdk.bean.LiveCapacityDevice;
import com.dji.flighthubsdk.bean.LiveCapacityVideo;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveStatusItem;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * fh_live_device — 查询设备可用直播流、观看人数、机场直播状态与容量。
 *
 * <p>安全等级：L0（READ_ONLY），只读查询。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhLiveDeviceTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhLiveDeviceTool.class);

    private static final String TOOL_NAME = "fh_live_device";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_LEVEL_1_GROUP = "level_1_group";
    private static final String PARAM_LEVEL_2_GROUP = "level_2_group";

    private static final String ACTION_AVAILABLE_RANGE = "available_range";
    private static final String ACTION_DOCK_LIVE_STATUS = "dock_live_status";
    private static final String ACTION_DOCK_LIVE_CAPACITY = "dock_live_capacity";
    private static final String ACTION_AUDIENCE_COUNT = "audience_count";

    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_AVAILABLE_RANGE, ACTION_DOCK_LIVE_STATUS, ACTION_DOCK_LIVE_CAPACITY, ACTION_AUDIENCE_COUNT);

    private static final List<String> VALID_ACTIONS_SORTED = List.of(
            "audience_count", "available_range", "dock_live_capacity", "dock_live_status");

    private static final int LISTEN_TIMEOUT_MS = 10_000;

    private final SdkService sdkService;

    public FhLiveDeviceTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 声明工具级静态安全等级：READ_ONLY。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description", "Query device live stream capabilities: "
                + "available_range returns all available video streams (LiveInfo with video_id) for starting multi-stream; "
                + "dock_live_status returns current live status per video on a dock; "
                + "dock_live_capacity returns available/max concurrent stream counts; "
                + "audience_count returns live audience count grouped by level_1_group/level_2_group.");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Target device serial number (drone or dock)"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Query type: available_range / dock_live_status / dock_live_capacity / audience_count"
        ));
        props.put(PARAM_LEVEL_1_GROUP, Map.of(
                "type", "string",
                "description", "Optional first-level grouping key for audience_count (e.g. region or channel). Only used by action=audience_count."
        ));
        props.put(PARAM_LEVEL_2_GROUP, Map.of(
                "type", "string",
                "description", "Optional second-level grouping key for audience_count. Only used by action=audience_count."
        ));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device_sn. Use fh_device_list to get available device SNs.");
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        if (sdkService.isMockMode()) {
            return dispatchMock(deviceSn, action);
        }

        return executeWithRealSdk(deviceSn, action, args);
    }

    // ==================== mock ====================

    private Map<String, Object> dispatchMock(String deviceSn, String action) {
        log.info("[MOCK] fh_live_device action={} on device={}", action, deviceSn);

        return switch (action) {
            case ACTION_AVAILABLE_RANGE -> {
                List<Map<String, Object>> streams = new ArrayList<>();
                Map<String, Object> s1 = new LinkedHashMap<>();
                s1.put("device_sn", deviceSn);
                s1.put("payload_index", "81-0-0");
                s1.put("video_id", deviceSn + "/81-0-0/normal-0");
                streams.add(s1);
                Map<String, Object> s2 = new LinkedHashMap<>();
                s2.put("device_sn", deviceSn);
                s2.put("payload_index", "81-0-0");
                s2.put("video_id", deviceSn + "/81-0-0/wide-0");
                streams.add(s2);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("streams", streams);
                data.put("count", streams.size());
                data.put("_mock", true);
                yield successResult(data, "Found " + streams.size() + " available streams for " + deviceSn);
            }
            case ACTION_DOCK_LIVE_STATUS -> {
                List<Map<String, Object>> statuses = new ArrayList<>();
                Map<String, Object> st = new LinkedHashMap<>();
                st.put("video_id", deviceSn + "/81-0-0/normal-0");
                st.put("video_type", "normal-0");
                st.put("video_quality", 0L);
                st.put("status", 0L);
                st.put("error_status", 0L);
                statuses.add(st);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("live_status", statuses);
                data.put("_mock", true);
                yield successResult(data, "Dock live status for " + deviceSn);
            }
            case ACTION_DOCK_LIVE_CAPACITY -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("available_video_number", 3L);
                data.put("coexist_video_number_max", 3L);
                data.put("devices", List.of());
                data.put("_mock", true);
                yield successResult(data, "Dock live capacity for " + deviceSn);
            }
            case ACTION_AUDIENCE_COUNT -> {
                List<Map<String, Object>> items = new ArrayList<>();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("sn", deviceSn);
                item.put("count", 12);
                items.add(item);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("items", items);
                data.put("_mock", true);
                yield successResult(data, "Audience count for " + deviceSn);
            }
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    // ==================== real SDK ====================

    @Generated
    private Map<String, Object> executeWithRealSdk(String deviceSn, String action, Map<String, Object> args) {
        try {
            LiveDeviceModule module = resolveModule(deviceSn);
            if (module == null) {
                return errorResult("LiveDeviceModule unavailable for device: " + deviceSn,
                        "Ensure device is online. Use fh_device_list to check.");
            }

            return switch (action) {
                case ACTION_AVAILABLE_RANGE -> handleAvailableRange(module, deviceSn);
                case ACTION_DOCK_LIVE_STATUS -> handleDockLiveStatus(module, deviceSn);
                case ACTION_DOCK_LIVE_CAPACITY -> handleDockLiveCapacity(module, deviceSn);
                case ACTION_AUDIENCE_COUNT -> handleAudienceCount(module, deviceSn, args);
                default -> errorResult("Unhandled action: " + action, "Report this bug.");
            };
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_live_device timeout for device {} action {}: {}",
                    deviceSn, action, e.getMessage(), e);
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Operation timed out. Check device connectivity.");
        } catch (Exception e) {
            log.error("fh_live_device error for device {}: {}", deviceSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check device connectivity and try again.");
        }
    }

    private LiveDeviceModule resolveModule(String deviceSn) {
        Drone drone = sdkService.getDrone(deviceSn);
        if (drone != null) {
            return drone.getLiveDeviceModule();
        }
        Dock dock = sdkService.getDock(deviceSn);
        if (dock != null) {
            return dock.getLiveDeviceModule();
        }
        return null;
    }

    private Map<String, Object> handleAvailableRange(LiveDeviceModule module, String deviceSn) {
        AtomicReference<List<LiveInfo>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = module.listenLiveAvailableRange(infoList -> {
            ref.set(infoList);
            latch.countDown();
        });

        if (listenId < 0) {
            return errorResult("Failed to listen for available streams on device: " + deviceSn,
                    "SDK returned negative listen ID. Check device state.");
        }

        try {
            boolean received = latch.await(LISTEN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!received) {
                module.cancelListenLiveAvailableRange(listenId);
                return errorResult("Timeout waiting for available stream data from " + deviceSn,
                        "Device may not be broadcasting. Check device connectivity.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            module.cancelListenLiveAvailableRange(listenId);
            return errorResult("Interrupted while waiting for stream data", "Retry the query.");
        } finally {
            module.cancelListenLiveAvailableRange(listenId);
        }

        List<LiveInfo> infoList = ref.get();
        List<Map<String, Object>> streams = new ArrayList<>();
        if (infoList != null) {
            for (LiveInfo info : infoList) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("device_sn", info.getDeviceSn());
                item.put("payload_index", info.getPayloadIndex());
                item.put("video_id", info.getVideoId());
                if (info.getSwitchableLensTypeSwigValues() != null) {
                    item.put("switchable_lens_types", info.getSwitchableLensTypeSwigValues());
                }
                streams.add(item);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("streams", streams);
        data.put("count", streams.size());

        log.info("[REAL] Available range for device={}: {} streams", deviceSn, streams.size());
        return successResult(data,
                "Found " + streams.size() + " available streams for " + deviceSn
                        + ". Use video_id with fh_live_start to start a specific stream.");
    }

    private Map<String, Object> handleDockLiveStatus(LiveDeviceModule module, String deviceSn) {
        AtomicReference<DockLiveStatus> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = module.listenDockLiveStatus(status -> {
            ref.set(status);
            latch.countDown();
        });

        if (listenId < 0) {
            return errorResult("Failed to listen for dock live status on device: " + deviceSn,
                    "SDK returned negative listen ID. Check device state.");
        }

        try {
            boolean received = latch.await(LISTEN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!received) {
                module.cancelListenDockLiveStatus(listenId);
                return errorResult("Timeout waiting for dock live status from " + deviceSn,
                        "Device may not be broadcasting. Check device connectivity.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            module.cancelListenDockLiveStatus(listenId);
            return errorResult("Interrupted while waiting for dock live status", "Retry the query.");
        } finally {
            module.cancelListenDockLiveStatus(listenId);
        }

        DockLiveStatus status = ref.get();
        List<Map<String, Object>> statuses = new ArrayList<>();
        if (status != null && status.getLiveStatus() != null) {
            for (LiveStatusItem item : status.getLiveStatus()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("video_id", item.getVideoId());
                m.put("video_type", item.getVideoType());
                m.put("video_quality", item.getVideoQuality());
                m.put("status", item.getStatus());
                m.put("error_status", item.getErrorStatus());
                statuses.add(m);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("live_status", statuses);

        log.info("[REAL] Dock live status for device={}: {} items", deviceSn, statuses.size());
        return successResult(data, "Dock live status for " + deviceSn + ": " + statuses.size() + " streams");
    }

    private Map<String, Object> handleDockLiveCapacity(LiveDeviceModule module, String deviceSn) {
        AtomicReference<DockLiveCapacity> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = module.listenDockLiveCapacity(capacity -> {
            ref.set(capacity);
            latch.countDown();
        });

        if (listenId < 0) {
            return errorResult("Failed to listen for dock live capacity on device: " + deviceSn,
                    "SDK returned negative listen ID. Check device state.");
        }

        try {
            boolean received = latch.await(LISTEN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!received) {
                module.cancelListenDockLiveCapacity(listenId);
                return errorResult("Timeout waiting for dock live capacity from " + deviceSn,
                        "Device may not be broadcasting. Check device connectivity.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            module.cancelListenDockLiveCapacity(listenId);
            return errorResult("Interrupted while waiting for dock live capacity", "Retry the query.");
        } finally {
            module.cancelListenDockLiveCapacity(listenId);
        }

        DockLiveCapacity capacity = ref.get();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("available_video_number", capacity != null ? capacity.getAvailableVideoNumber() : 0L);
        data.put("coexist_video_number_max", capacity != null ? capacity.getCoexistVideoNumberMax() : 0L);

        List<Map<String, Object>> devices = new ArrayList<>();
        if (capacity != null && capacity.getDeviceList() != null) {
            for (LiveCapacityDevice dev : capacity.getDeviceList()) {
                Map<String, Object> dm = new LinkedHashMap<>();
                dm.put("sn", dev.getSn());
                dm.put("available_video_number", dev.getAvailableVideoNumber());
                dm.put("coexist_video_number_max", dev.getCoexistVideoNumberMax());
                List<Map<String, Object>> cameras = new ArrayList<>();
                if (dev.getCameraList() != null) {
                    for (LiveCapacityCamera cam : dev.getCameraList()) {
                        Map<String, Object> cm = new LinkedHashMap<>();
                        cm.put("camera_index", cam.getCameraIndex());
                        cm.put("available_video_number", cam.getAvailableVideoNumber());
                        cm.put("coexist_video_number_max", cam.getCoexistVideoNumberMax());
                        List<Map<String, Object>> videos = new ArrayList<>();
                        if (cam.getVideoList() != null) {
                            for (LiveCapacityVideo vid : cam.getVideoList()) {
                                Map<String, Object> vm = new LinkedHashMap<>();
                                vm.put("video_index", vid.getVideoIndex());
                                vm.put("video_type", vid.getVideoType());
                                vm.put("switchable_video_types", vid.getSwitchableVideoTypes());
                                videos.add(vm);
                            }
                        }
                        cm.put("video_list", videos);
                        cameras.add(cm);
                    }
                }
                dm.put("camera_list", cameras);
                devices.add(dm);
            }
        }
        data.put("devices", devices);

        log.info("[REAL] Dock live capacity for device={}: available={}, max={}",
                deviceSn,
                capacity != null ? capacity.getAvailableVideoNumber() : 0,
                capacity != null ? capacity.getCoexistVideoNumberMax() : 0);
        return successResult(data, "Dock live capacity for " + deviceSn);
    }

    private Map<String, Object> handleAudienceCount(LiveDeviceModule module, String deviceSn, Map<String, Object> args) {
        AudienceCountGroup group = new AudienceCountGroup();
        group.setLevel1Group(getStringArg(args, PARAM_LEVEL_1_GROUP, null));
        group.setLevel2Group(getStringArg(args, PARAM_LEVEL_2_GROUP, null));

        CompletableFuture<List<DeviceAudienceCountItem>> future = new CompletableFuture<>();
        module.getAudienceCount(group, (error, result) -> {
            if (error.isSuccess()) {
                future.complete(result);
            } else {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
            }
        });
        List<DeviceAudienceCountItem> result = SdkCallbackBridge.await(future);

        List<Map<String, Object>> items = new ArrayList<>();
        if (result != null) {
            for (DeviceAudienceCountItem item : result) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("sn", item.getSn());
                m.put("count", item.getCount());
                items.add(m);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("items", items);

        log.info("[REAL] Audience count for device={}: {} item(s)", deviceSn, items.size());
        return successResult(data, "Audience count for " + deviceSn + ": " + items.size() + " item(s)");
    }
}
