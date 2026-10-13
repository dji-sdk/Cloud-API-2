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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * fh_device_list — 查询 workspace 中的所有设备。
 * 支持按设备类型（drone/dock/airport/base_station/all）和在线状态过滤。
 *
 * <p>设备拓扑说明：SDK {@code getDeviceList()} 返回的顶层设备（{@link DeviceTopo#getDevice()}）
 * 真实类型只有 {@code airport}（机场柜）/ {@code base_station}（地面站）；
 * 挂载的子设备（{@link DeviceTopo#getSubDevices()}）真实类型为
 * {@code drone}/{@code dock}/{@code relay}。汇总统计和 {@code type} 过滤都需要把
 * 子设备摊平后才能覆盖到这些类型，详见 {@link #flattenDevices}。
 */
@Component
public class FhDeviceListTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDeviceListTool.class);

    /** {@code type} 过滤参数支持的取值枚举。 */
    private static final List<String> TYPE_FILTER_ENUM =
            List.of("drone", "dock", "airport", "base_station", "all");

    /** 表示不过滤类型的特殊取值。 */
    private static final String TYPE_FILTER_ALL = "all";

    private final SdkService sdkService;

    public FhDeviceListTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_device_list");
        def.put("description", "Query all devices in the current workspace. "
                + "Returns device SN, name, type, online status. "
                + "Top-level devices are gateways (airport/base_station); "
                + "drone/dock/relay are their sub-devices. "
                + "Supports filtering by device type (across all levels) and online status. "
                + "IMPORTANT: Drones housed inside airports/docks typically appear OFFLINE (powered down in storage) — "
                + "this is NORMAL and expected. "
                + "For dock/airport-based flight operations, only the airport (gateway) needs to be online. "
                + "Do NOT use drone.online status as a pre-flight blocker; check airport.online instead.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("type", Map.of(
                "type", "string",
                "enum", TYPE_FILTER_ENUM,
                "description", "Filter by device type, matched across top-level gateways and "
                        + "their sub-devices (default: all)",
                "default", TYPE_FILTER_ALL
        ));
        props.put("online_only", Map.of(
                "type", "boolean",
                "description", "Only return online devices (default: false)",
                "default", false
        ));
        schema.put("properties", props);
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 检查 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 解析过滤参数
        String typeFilter = args != null ? String.valueOf(args.getOrDefault("type", TYPE_FILTER_ALL))
                : TYPE_FILTER_ALL;
        boolean onlineOnly = args != null && Boolean.TRUE.equals(args.get("online_only"));

        // 获取 workspace
        Workspace workspace = sdkService.getWorkspace();

        // Mock 模式：返回模拟数据
        if (sdkService.isMockMode() || workspace == null) {
            return buildDeviceListResponse(mockDeviceTree(), typeFilter, onlineOnly, true);
        }

        try {
            List<Map<String, Object>> deviceTree = fetchDeviceTree(workspace);
            return buildDeviceListResponse(deviceTree, typeFilter, onlineOnly, false);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Device list request timed out. Check server connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_device_list error: {}", e.getMessage(), e);
            return errorResult("Internal error", e.getMessage());
        }
    }

    /**
     * 调用 SDK 异步接口获取设备拓扑，通过 bridge 同步等待并转换为树状 Map 结构。
     *
     * @param workspace 当前已连接的 workspace
     * @return 顶层设备列表（树状结构，顶层 item 可能带 "children" 字段）
     */
    private List<Map<String, Object>> fetchDeviceTree(Workspace workspace) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "getDeviceList failed: " + error.getErrorCode()));
                return;
            }
            future.complete(toDeviceTree(devices));
        });

        return SdkCallbackBridge.await(future);
    }

    /**
     * 把 SDK 返回的 {@link DeviceTopo} 列表转换为树状 Map 结构。
     *
     * @param topos SDK 原始设备拓扑列表
     * @return 顶层设备 Map 列表，挂载子设备的顶层 item 带 "children" 字段
     */
    private static List<Map<String, Object>> toDeviceTree(List<DeviceTopo> topos) {
        List<Map<String, Object>> deviceTree = new ArrayList<>();
        if (topos == null) {
            return deviceTree;
        }
        for (DeviceTopo topo : topos) {
            Map<String, Object> item = buildDeviceItem(topo.getDevice());

            List<Device> subDevices = topo.getSubDevices();
            if (subDevices != null && !subDevices.isEmpty()) {
                List<Map<String, Object>> children = new ArrayList<>();
                for (Device child : subDevices) {
                    children.add(buildDeviceItem(child));
                }
                item.put("children", children);
            }
            deviceTree.add(item);
        }
        return deviceTree;
    }

    /**
     * 把单个 SDK 设备对象转换为响应用的 Map（sn/name/type/online/model）。
     *
     * @param device SDK 设备对象（顶层网关或子设备均适用）
     * @return 设备信息 Map
     */
    private static Map<String, Object> buildDeviceItem(Device device) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sn", device.getDeviceSn());
        item.put("name", device.getNickname() != null && !device.getNickname().isEmpty()
                ? device.getNickname() : device.getDeviceName());
        item.put("type", device.getDeviceClass());
        item.put("online", device.isOnline());
        item.put("model", device.getModelKey());
        return item;
    }

    /**
     * 把树状设备结构摊平为单层列表，顶层网关设备（airport/base_station）和其挂载的
     * 子设备（drone/dock/relay）都作为独立条目出现在同一层，供聚合统计和 type 过滤使用。
     *
     * <p>顶层 item 摊平后仍保留原有的 "children" 字段（用于 {@link #buildDeviceListResponse}
     * 里的树状展示分支），子设备条目不带 "children"（子设备本身没有下一级）。
     *
     * @param deviceTree 树状设备列表（{@link #toDeviceTree} 或 mock 构造的结构）
     * @return 摊平后的设备列表，顺序为：每个顶层设备本身，紧跟其子设备（如有）
     */
    static List<Map<String, Object>> flattenDevices(List<Map<String, Object>> deviceTree) {
        List<Map<String, Object>> flat = new ArrayList<>();
        for (Map<String, Object> item : deviceTree) {
            flat.add(item);
            flat.addAll(extractChildren(item));
        }
        return flat;
    }

    /**
     * 提取顶层设备 item 里的子设备列表（"children" 字段），不存在时返回空列表。
     *
     * @param item 顶层设备 Map
     * @return 子设备 Map 列表（可能为空，不会为 null）
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractChildren(Map<String, Object> item) {
        Object children = item.get("children");
        if (children instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    /**
     * 按真实 type 值（大小写不敏感、忽略 "|" 或空格之后的后缀变体，如 "airport | 1"）
     * 分组统计在线数量。不写死具体类型枚举，遇到的每种真实 type 值都会统计，
     * 避免未来出现新设备类型时汇总再次漏统计。
     *
     * @param flatDevices {@link #flattenDevices} 摊平后的设备列表
     * @return type（标准化后小写）→ 在线数量
     */
    static Map<String, Long> aggregateOnlineCounts(List<Map<String, Object>> flatDevices) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Map<String, Object> device : flatDevices) {
            if (!Boolean.TRUE.equals(device.get("online"))) {
                continue;
            }
            String type = normalizeType(device.get("type"));
            counts.merge(type, 1L, Long::sum);
        }
        return counts;
    }

    /**
     * 按 type 过滤摊平后的设备列表，{@code "all"} 表示不过滤。
     * 过滤同样基于标准化后的 type 值比较，容忍大小写和 "airport | 1" 这类已知格式变体。
     *
     * @param flatDevices {@link #flattenDevices} 摊平后的设备列表
     * @param typeFilter  过滤类型（"drone"/"dock"/"airport"/"base_station"/"all"）
     * @return 过滤后的设备列表
     */
    static List<Map<String, Object>> filterFlatByType(List<Map<String, Object>> flatDevices, String typeFilter) {
        if (TYPE_FILTER_ALL.equalsIgnoreCase(typeFilter)) {
            return flatDevices;
        }
        String normalizedFilter = normalizeType(typeFilter);
        return flatDevices.stream()
                .filter(d -> normalizeType(d.get("type")).equals(normalizedFilter))
                .toList();
    }

    /**
     * 标准化设备 type 值：转小写、去除首尾空白，并截断 "|" 或空格之后的后缀
     * （兼容已知的 "airport | 1" 格式变体，根因暂未排查，此处只做兼容不做报错）。
     *
     * @param rawType 原始 type 值（可能为 null）
     * @return 标准化后的 type 字符串
     */
    private static String normalizeType(Object rawType) {
        String s = String.valueOf(rawType).trim().toLowerCase(Locale.ROOT);
        int cutAt = s.length();
        int pipeIdx = s.indexOf('|');
        if (pipeIdx >= 0) {
            cutAt = Math.min(cutAt, pipeIdx);
        }
        int spaceIdx = s.indexOf(' ');
        if (spaceIdx >= 0) {
            cutAt = Math.min(cutAt, spaceIdx);
        }
        return s.substring(0, cutAt);
    }

    /**
     * 根据树状设备数据构建 {@code fh_device_list} 的完整响应（过滤 + 聚合 + 摘要文案）。
     *
     * @param deviceTree 顶层设备树状列表（真实 SDK 或 mock 来源）
     * @param typeFilter type 过滤参数
     * @param onlineOnly 是否只保留在线设备
     * @param isMock     是否为 mock 数据（决定是否附加 "_mock" 标记和摘要前缀）
     * @return 完整工具响应 Map
     */
    private static Map<String, Object> buildDeviceListResponse(
            List<Map<String, Object>> deviceTree, String typeFilter, boolean onlineOnly, boolean isMock) {
        List<Map<String, Object>> flatDevices = flattenDevices(deviceTree);
        Map<String, Long> onlineCounts = aggregateOnlineCounts(flatDevices);

        long dronesOnline = onlineCounts.getOrDefault("drone", 0L);
        long docksOnline = onlineCounts.getOrDefault("dock", 0L);
        long airportsOnline = onlineCounts.getOrDefault("airport", 0L);
        long baseStationsOnline = onlineCounts.getOrDefault("base_station", 0L);

        // 无类型过滤（"all"）时保持树状结构（顶层 + children），与现有消费者兼容；
        // 有类型过滤时改为在摊平列表上过滤——这样才能覆盖只存在于子设备层的
        // drone/dock/relay，以及只存在于顶层的 airport/base_station。
        List<Map<String, Object>> candidates =
                TYPE_FILTER_ALL.equalsIgnoreCase(typeFilter) ? deviceTree : flatDevices;
        List<Map<String, Object>> filtered = filterFlatByType(candidates, typeFilter).stream()
                .filter(d -> !onlineOnly || Boolean.TRUE.equals(d.get("online")))
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("devices", filtered);
        data.put("total", deviceTree.size());
        data.put("filtered", filtered.size());
        data.put("drones_online", dronesOnline);
        data.put("docks_online", docksOnline);
        data.put("airports_online", airportsOnline);
        data.put("base_stations_online", baseStationsOnline);
        if (isMock) {
            data.put("_mock", true);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        String prefix = isMock ? "[MOCK] " : "";
        result.put("message", String.format(
                "%sFound %d top-level devices (%d shown after filter). "
                        + "%d airports online, %d base stations online, %d drones online, %d docks online.",
                prefix, deviceTree.size(), filtered.size(),
                airportsOnline, baseStationsOnline, dronesOnline, docksOnline));
        result.put("next_steps", List.of(
                "Call fh_drone_snapshot(drone_sn) for real-time drone status",
                "Call fh_dock_snapshot(dock_sn) for dock status"
        ));
        return result;
    }

    /**
     * 构造贴近真实拓扑的 mock 设备树：1 台在线 airport 挂 1 台在线 drone，
     * 1 台离线 base_station 挂 1 台在线 dock，用于本地开发/演示，
     * 不依赖真实 SDK 连接。
     *
     * @return mock 顶层设备树状列表
     */
    private static List<Map<String, Object>> mockDeviceTree() {
        Map<String, Object> airport = new LinkedHashMap<>();
        airport.put("sn", "AIRPORT-7CQDKR1020003V");
        airport.put("name", "Dock 2");
        airport.put("type", "airport");
        airport.put("online", true);
        airport.put("model", "dock-2");
        airport.put("children", List.of(mockChild(
                "DRONE-1581F6GKD241T00E40", "Matrice 3D", "drone", "matrice-3d", true)));

        Map<String, Object> baseStation = new LinkedHashMap<>();
        baseStation.put("sn", "BS-4LFDK100030182");
        baseStation.put("name", "地面站-001");
        baseStation.put("type", "base_station");
        baseStation.put("online", false);
        baseStation.put("model", "base-station-t");
        baseStation.put("children", List.of(mockChild(
                "DOCK-1581F6Q8D2400700B3", "ea340d", "dock", "ea340d", true)));

        return List.of(airport, baseStation);
    }

    /**
     * 构造 mock 子设备 Map。
     *
     * @param sn     子设备 SN
     * @param name   子设备名称
     * @param type   子设备类型
     * @param model  子设备型号
     * @param online 是否在线
     * @return 子设备 Map
     */
    private static Map<String, Object> mockChild(String sn, String name, String type, String model, boolean online) {
        Map<String, Object> child = new LinkedHashMap<>();
        child.put("sn", sn);
        child.put("name", name);
        child.put("type", type);
        child.put("online", online);
        child.put("model", model);
        return child;
    }
}
