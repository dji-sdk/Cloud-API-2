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

package com.dji.flighthub.mcp.tools.osd;

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.ai.AiSubscriptionBuffer;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * fh_osd_listen — 订阅 / 读取 Drone、Dock 的 OSD 实时数据。
 *
 * <p>安全等级：L0（READ_ONLY），前置条件：SESSION。
 * catalog 列出可订阅字段；read 一次性读取各字段首帧；
 * start / events / stop 持续订阅并增量拉取（数据写入 AiSubscriptionBuffer）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhOsdListenTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhOsdListenTool.class);

    private static final String TOOL_NAME = "fh_osd_listen";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_DEVICE_TYPE = "device_type";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_FIELDS = "fields";

    private static final String ACTION_CATALOG = "catalog";
    private static final String ACTION_READ = "read";
    private static final String ACTION_START = "start";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_STOP = "stop";

    private static final String TYPE_DRONE = "drone";
    private static final String TYPE_DOCK = "dock";

    private static final Set<String> VALID_ACTIONS =
            Set.of(ACTION_CATALOG, ACTION_READ, ACTION_START, ACTION_EVENTS, ACTION_STOP);
    private static final Set<String> VALID_TYPES = Set.of(TYPE_DRONE, TYPE_DOCK);

    private static final int DEFAULT_TIMEOUT_MS = 10_000;
    private static final int MAX_TIMEOUT_MS = 60_000;
    private static final String REGISTRY_PREFIX = "osd::";

    private static final AtomicInteger MOCK_ID_GEN = new AtomicInteger(19000);

    private final SdkService sdkService;
    private final AiSubscriptionBuffer buffer;

    public FhOsdListenTool(SessionManager sessionManager, SdkService sdkService,
                            AiSubscriptionBuffer buffer) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.buffer = buffer;
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
        def.put("description", "Read or subscribe to Drone / Dock OSD data. "
                + "catalog: list subscribable fields. "
                + "read: one-shot, wait for the first frame of each field. "
                + "start: subscribe continuously (data is buffered). "
                + "events: pull buffered data incrementally via since_seq. "
                + "stop: cancel subscriptions.");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string", "enum", List.of("catalog", "read", "start", "events", "stop"),
                "description", "Action to perform"));
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string", "description", "Device serial number (required except for catalog)"));
        props.put(PARAM_DEVICE_TYPE, Map.of(
                "type", "string", "enum", List.of(TYPE_DRONE, TYPE_DOCK),
                "description", "Device type, default drone. For catalog, omit to list both."));
        props.put(PARAM_FIELDS, Map.of(
                "type", "array", "items", Map.of("type", "string"),
                "description", "Field names (see catalog). Required for read/start; "
                        + "for events/stop defaults to all active subscriptions of the device."));
        props.put("timeout_ms", Map.of(
                "type", "integer",
                "description", "read: max wait for first frames in ms (default 10000, max 60000)"));
        props.put("since_seq", Map.of(
                "type", "integer",
                "description", "events: return events with _seq > since_seq (default 0)"));
        props.put("limit", Map.of(
                "type", "integer",
                "description", "events: max events per field (default 50)"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }
        String action = getStringArg(args, PARAM_ACTION, "");
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "Valid actions: catalog, read, start, events, stop");
        }
        String deviceType = getStringArg(args, PARAM_DEVICE_TYPE, null);
        if (deviceType != null && !VALID_TYPES.contains(deviceType)) {
            return errorResult("Invalid device_type: " + deviceType, "Valid device types: drone, dock");
        }
        if (ACTION_CATALOG.equals(action)) {
            return handleCatalog(deviceType);
        }
        String sn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (sn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide the target device serial number.");
        }
        String type = deviceType == null ? TYPE_DRONE : deviceType;
        return switch (action) {
            case ACTION_READ -> handleRead(sn, type, args);
            case ACTION_START -> handleStart(session, sn, type, args);
            case ACTION_EVENTS -> handleEvents(session, sn, args);
            case ACTION_STOP -> handleStop(session, sn, args);
            default -> errorResult("Unhandled action: " + action, "This should not happen.");
        };
    }

    // ==================== catalog ====================

    private Map<String, Object> handleCatalog(String deviceType) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (deviceType == null || TYPE_DRONE.equals(deviceType)) {
            data.put(TYPE_DRONE, describe(OsdFieldCatalog.DRONE));
        }
        if (deviceType == null || TYPE_DOCK.equals(deviceType)) {
            data.put(TYPE_DOCK, describe(OsdFieldCatalog.DOCK));
        }
        return successResult(data, "Pass field names via 'fields' to read / start.");
    }

    private List<Map<String, Object>> describe(OsdFieldCatalog catalog) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OsdFieldCatalog.Field field : catalog.all().values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", field.name());
            item.put("sdk_method", field.listen().getName());
            item.put("payload_type", field.payloadType().getSimpleName());
            list.add(item);
        }
        return list;
    }

    // ==================== read ====================

    private Map<String, Object> handleRead(String sn, String type, Map<String, Object> args) {
        OsdFieldCatalog catalog = OsdFieldCatalog.forDeviceType(type);
        List<String> fields = resolveRequiredFields(args, catalog);
        if (fields == null) {
            return fieldsErrorResult(args, catalog);
        }
        int timeoutMs = Math.max(1, Math.min(MAX_TIMEOUT_MS, getIntArg(args, "timeout_ms", DEFAULT_TIMEOUT_MS)));

        boolean mock = sdkService.isMockMode();
        Map<String, Object> snapshot = new ConcurrentHashMap<>();
        List<String> rejected = new ArrayList<>();
        if (mock) {
            for (String name : fields) {
                snapshot.put(name, mockPayload(catalog.get(name)));
            }
        } else {
            Object module = resolveModule(sn, type);
            if (module == null) {
                return errorResult("Device instance not found: " + sn,
                        "Use fh_device_list to check device status.");
            }
            try {
                collectFirstFrames(module, catalog, fields, timeoutMs, snapshot, rejected);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return errorResult("Interrupted while waiting for OSD data", "Retry the read.");
            }
        }

        Map<String, Object> received = new LinkedHashMap<>();
        List<String> timedOut = new ArrayList<>();
        for (String name : fields) {
            if (snapshot.containsKey(name)) {
                received.put(name, snapshot.get(name));
            } else if (!rejected.contains(name)) {
                timedOut.add(name);
            }
        }
        if (received.isEmpty()) {
            return errorResult("No OSD data received for " + fields
                            + " (timed_out=" + timedOut + ", rejected=" + rejected + ")",
                    "Check the device is online and pushing OSD; try a larger timeout_ms.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        if (mock) {
            data.put("_mock", true);
        }
        data.put("device_sn", sn);
        data.put("device_type", type);
        data.put("fields", received);
        data.put("timed_out", timedOut);
        data.put("rejected", rejected);
        return successResult(data, (mock ? "Mock: " : "") + "Received " + received.size() + "/" + fields.size()
                + " fields");
    }

    private void collectFirstFrames(Object module, OsdFieldCatalog catalog, List<String> fields, int timeoutMs,
                                    Map<String, Object> snapshot, List<String> rejected)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(fields.size());
        Map<String, Integer> listenIds = new LinkedHashMap<>();
        try {
            for (String name : fields) {
                OsdFieldCatalog.Field field = catalog.get(name);
                int listenId;
                try {
                    listenId = field.listen(module, payload -> {
                        if (snapshot.putIfAbsent(name, OsdBeanMapper.toEvent(payload)) == null) {
                            latch.countDown();
                        }
                    });
                } catch (RuntimeException e) {
                    log.warn("OSD listen failed: field={}, error={}", name, e.getMessage());
                    listenId = -1;
                }
                if (listenId < 0) {
                    rejected.add(name);
                    latch.countDown();
                } else {
                    listenIds.put(name, listenId);
                }
            }
            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } finally {
            listenIds.forEach((name, id) -> {
                try {
                    catalog.get(name).cancel(module, id);
                } catch (RuntimeException e) {
                    log.warn("OSD cancel failed: field={}, error={}", name, e.getMessage());
                }
            });
        }
    }

    // ==================== start ====================

    private Map<String, Object> handleStart(SessionContext session, String sn, String type,
                                            Map<String, Object> args) {
        OsdFieldCatalog catalog = OsdFieldCatalog.forDeviceType(type);
        List<String> fields = resolveRequiredFields(args, catalog);
        if (fields == null) {
            return fieldsErrorResult(args, catalog);
        }
        boolean mock = sdkService.isMockMode();
        Object module = null;
        if (!mock) {
            module = resolveModule(sn, type);
            if (module == null) {
                return errorResult("Device instance not found: " + sn,
                        "Use fh_device_list to check device status.");
            }
        }

        Map<String, Integer> listenIds = new LinkedHashMap<>();
        for (String name : fields) {
            int listenId;
            try {
                listenId = startField(module, session, sn, catalog.get(name), mock);
            } catch (RuntimeException e) {
                log.warn("OSD start failed: sn={}, field={}, error={}", sn, name, e.getMessage());
                listenId = -1;
            }
            if (listenId < 0) {
                listenIds.keySet().forEach(started -> cancelQuietly(session, registryKey(sn, started)));
                return errorResult("SDK refused subscription for field " + name,
                        "Check device state; no subscription was kept.");
            }
            listenIds.put(name, listenId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        if (mock) {
            data.put("_mock", true);
        }
        data.put("device_sn", sn);
        data.put("listen_ids", listenIds);
        return successResult(data, (mock ? "Mock: " : "") + "OSD subscriptions started for " + fields);
    }

    private int startField(Object module, SessionContext session, String sn,
                           OsdFieldCatalog.Field field, boolean mock) {
        String key = registryKey(sn, field.name());
        cancelQuietly(session, key);
        String scope = AiSubscriptionBuffer.scope(session, sn);
        String streamKey = streamKey(field.name());
        int listenId;
        if (mock) {
            buffer.append(scope, streamKey, withTimestamp(mockPayload(field)));
            listenId = MOCK_ID_GEN.incrementAndGet();
        } else {
            listenId = field.listen(module, payload ->
                    buffer.append(scope, streamKey, withTimestamp(OsdBeanMapper.toEvent(payload))));
        }
        if (listenId < 0) {
            return listenId;
        }
        session.registerListen(key, field.name() + ":" + sn, () -> {
            try {
                if (!mock) {
                    field.cancel(module, listenId);
                }
            } finally {
                buffer.remove(scope, streamKey);
            }
        });
        return listenId;
    }

    // ==================== events ====================

    private Map<String, Object> handleEvents(SessionContext session, String sn, Map<String, Object> args) {
        List<String> explicit = getStringListArg(args, PARAM_FIELDS);
        List<String> fields = (explicit == null || explicit.isEmpty()) ? activeFields(session, sn) : explicit;
        long sinceSeq = getLongArg(args, "since_seq", 0);
        int limit = getIntArg(args, "limit", 50);
        String scope = AiSubscriptionBuffer.scope(session, sn);

        Map<String, Object> streams = new LinkedHashMap<>();
        boolean truncated = false;
        for (String name : fields) {
            AiSubscriptionBuffer.ReadResult result = buffer.read(scope, streamKey(name), sinceSeq, limit);
            Map<String, Object> stream = new LinkedHashMap<>();
            stream.put("active", session.getListenRegistry().containsKey(registryKey(sn, name)));
            stream.put("events", result.events());
            stream.put("count", result.events().size());
            stream.put("dropped", result.dropped());
            stream.put("truncated", result.truncated());
            if (!result.events().isEmpty()) {
                stream.put("last_seq", result.events().get(result.events().size() - 1).get("_seq"));
            }
            truncated |= result.truncated();
            streams.put(name, stream);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", sn);
        data.put("streams", streams);
        String hint;
        if (fields.isEmpty()) {
            hint = "No active OSD subscriptions for this device. Call start first.";
        } else if (truncated) {
            hint = "Some events after since_seq were evicted from the buffer; poll more frequently.";
        } else {
            hint = "Use each stream's last_seq as since_seq in the next call for incremental reads.";
        }
        return successResult(data, hint);
    }

    // ==================== stop ====================

    private Map<String, Object> handleStop(SessionContext session, String sn, Map<String, Object> args) {
        List<String> explicit = getStringListArg(args, PARAM_FIELDS);
        List<String> fields = (explicit == null || explicit.isEmpty()) ? activeFields(session, sn) : explicit;
        List<String> stopped = new ArrayList<>();
        List<String> notActive = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String name : fields) {
            try {
                if (session.cancelListen(registryKey(sn, name))) {
                    stopped.add(name);
                } else {
                    notActive.add(name);
                }
            } catch (RuntimeException e) {
                log.warn("Failed to cancel OSD listen: sn={}, field={}, error={}", sn, name, e.getMessage());
                failed.add(name);
            }
        }
        if (!failed.isEmpty()) {
            return errorResult("Failed to cancel SDK subscription for " + failed
                            + (stopped.isEmpty() ? "" : " (stopped: " + stopped + ")"),
                    "The local registration was removed; check device connectivity.");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stopped", stopped);
        data.put("not_active", notActive);
        return successResult(data, stopped.isEmpty() ? "No active subscriptions to stop" : "Stopped: " + stopped);
    }

    // ==================== helpers ====================

    private Object resolveModule(String sn, String type) {
        if (TYPE_DOCK.equals(type)) {
            Dock dock = sdkService.getDock(sn);
            return dock == null ? null : dock.getDockOsdModule();
        }
        Drone drone = sdkService.getDrone(sn);
        return drone == null ? null : drone.getDroneOsdModule();
    }

    /**
     * 解析并校验 fields；缺失、为空或包含未知字段时返回 null。
     */
    private List<String> resolveRequiredFields(Map<String, Object> args, OsdFieldCatalog catalog) {
        List<String> requested = getStringListArg(args, PARAM_FIELDS);
        if (requested == null || requested.isEmpty()) {
            return null;
        }
        List<String> fields = new ArrayList<>(new LinkedHashSet<>(requested));
        for (String name : fields) {
            if (catalog.get(name) == null) {
                return null;
            }
        }
        return fields;
    }

    private Map<String, Object> fieldsErrorResult(Map<String, Object> args, OsdFieldCatalog catalog) {
        List<String> requested = getStringListArg(args, PARAM_FIELDS);
        if (requested == null || requested.isEmpty()) {
            return errorResult("Missing required parameter: fields",
                    "Provide field names; use action=catalog to list them.");
        }
        List<String> unknown = requested.stream().filter(name -> catalog.get(name) == null).toList();
        return errorResult("Unknown fields: " + unknown,
                "Use action=catalog to list valid field names.");
    }

    private List<String> activeFields(SessionContext session, String sn) {
        String prefix = REGISTRY_PREFIX + sn + "::";
        Set<String> names = new TreeSet<>();
        for (String key : session.getListenRegistry().keySet()) {
            if (key.startsWith(prefix)) {
                names.add(key.substring(prefix.length()));
            }
        }
        return new ArrayList<>(names);
    }

    private Map<String, Object> mockPayload(OsdFieldCatalog.Field field) {
        Map<String, Object> event = OsdBeanMapper.toEvent(field.newDefaultPayload());
        event.put("_mock", true);
        return event;
    }

    private static Map<String, Object> withTimestamp(Map<String, Object> event) {
        event.put("_ts", System.currentTimeMillis());
        return event;
    }

    private void cancelQuietly(SessionContext session, String key) {
        try {
            session.cancelListen(key);
        } catch (RuntimeException e) {
            log.warn("Failed to cancel listen: key={}, error={}", key, e.getMessage());
        }
    }

    private static String registryKey(String sn, String field) {
        return REGISTRY_PREFIX + sn + "::" + field;
    }

    private static String streamKey(String field) {
        return "osd:" + field;
    }
}
