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

package com.dji.fh.feature.chat.mcp;

import com.dji.fh.feature.chat.controller.ChatSseEvent;
import com.dji.fh.feature.chat.tool.NavigationHolder;
import com.dji.fh.feature.chat.tool.ToolCallContext;
import com.dji.fh.feature.chat.tool.tools.AbstractChatTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * MCP 透传 Chat Tool — 将 chatbot 的 function calling 请求直接代理到 MCP Server。
 *
 * <p>每个实例对应 MCP Server 中的一个 Tool（通过 name 映射）。
 * 本类不包含任何业务逻辑，仅做 JSON 透传 + 错误处理。
 *
 * <p>优势：
 * <ul>
 *   <li>消除 skeleton 与 MCP Server 的代码重复</li>
 *   <li>新增 Tool 只需在 TOOL_MAPPINGS 添加一行配置</li>
 *   <li>MCP Server 的 mock 模式同时服务于 chatbot</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public class McpProxyChatTool extends AbstractChatTool {

    private static final Logger LOG = LoggerFactory.getLogger(McpProxyChatTool.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    /** 触发驾驶舱导航的 MCP 工具名。 */
    private static final String TOOL_TAKEOFF = "fh_takeoff";

    /** 驾驶舱路由路径。 */
    private static final String COCKPIT_PATH = "/cockpit";

    /** 无人机 SN 参数名。 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** 工具名 → 用户可见的操作描述（不暴露内部工具名）。 */
    private static final Map<String, String> FRIENDLY_NAMES = Map.ofEntries(
            Map.entry("fh_connect",           "连接 FlightHub"),
            Map.entry("fh_session_info",       "查询会话状态"),
            Map.entry("fh_device_list",        "查询设备列表"),
            Map.entry("fh_device_manage",      "查询设备详情"),
            Map.entry("fh_device_recommend",   "推荐设备"),
            Map.entry("fh_drone_snapshot",     "获取无人机状态"),
            Map.entry("fh_dock_snapshot",      "获取机场状态"),
            Map.entry("fh_osd_snapshot",       "获取飞行遥测数据"),
            Map.entry("fh_hms",                "查询设备健康告警"),
            Map.entry("fh_wayline_status",     "查询航线任务状态"),
            Map.entry("fh_flight_task",        "查询飞行任务"),
            Map.entry("fh_flight_area_list",   "查询飞行限制区域"),
            Map.entry("fh_workspace",          "查询航线库"),
            Map.entry("fh_media_list",         "查询媒体文件"),
            Map.entry("fh_media_download",     "获取媒体下载链接"),
            Map.entry("fh_firmware",           "查询固件信息"),
            Map.entry("fh_diagnosis",          "查询诊断报告"),
            Map.entry("fh_maintenance",        "查询维修工单"),
            Map.entry("fh_flight_record",      "导出飞行记录"),
            Map.entry("fh_log",                "查询设备日志"),
            Map.entry("fh_terra",              "查询三维重建任务"),
            Map.entry("fh_takeoff",            "执行一键起飞"),
            Map.entry("fh_flyto",              "控制无人机飞往目标点"),
            Map.entry("fh_return_home",        "控制无人机返航"),
            Map.entry("fh_wayline_execute",    "执行航线任务"),
            Map.entry("fh_wayline_control",    "控制航线任务"),
            Map.entry("fh_dock_control",       "控制机场舱盖"),
            Map.entry("fh_live_start",         "开启直播"),
            Map.entry("fh_camera",             "控制相机"),
            Map.entry("fh_media_record",       "控制直播录制"),
            Map.entry("fh_media_delete",       "删除媒体文件"),
            Map.entry("fh_poi",                "执行环绕飞行"),
            Map.entry("fh_control_acquire",    "获取飞行控制权"),
            Map.entry("fh_control_release",    "释放飞行控制权"),
            Map.entry("fh_drc_connect",        "建立 DRC 链路"),
            Map.entry("fh_drc_disconnect",     "断开 DRC 链路")
    );

    /** MCP Server 中的 tool name */
    private final String mcpToolName;

    /** Tool 描述（从 MCP Server tools/list 获取） */
    private final String description;

    /** Tool 输入 JSON Schema（从 MCP Server tools/list 获取） */
    private final String inputSchema;

    /** MCP Client 实例 */
    private final McpClient mcpClient;

    /**
     * 构造透传 Tool。
     *
     * @param mcpToolName MCP Tool 名称
     * @param description Tool 描述
     * @param inputSchema JSON Schema 字符串
     * @param mcpClient   MCP Client
     */
    public McpProxyChatTool(String mcpToolName, String description,
                            String inputSchema, McpClient mcpClient) {
        this.mcpToolName = mcpToolName;
        this.description = description;
        this.inputSchema = inputSchema;
        this.mcpClient = mcpClient;
    }

    /**
     * 返回 Tool 定义，供 Spring AI function calling 使用。
     *
     * @return ToolDefinition
     */
    @Override
    public ToolDefinition getToolDefinition() {
        return DefaultToolDefinition.builder()
                .name(mcpToolName)
                .description(description)
                .inputSchema(inputSchema)
                .build();
    }

    /**
     * 执行 Tool 调用 — 解析输入 JSON 为 Map，透传到 MCP Server。
     *
     * @param input Spring AI 传入的 JSON 字符串（函数参数）
     * @return MCP Server 返回的原始文本结果
     */
    @Override
    public String call(String input) {
        ToolCallContext.emit(ChatSseEvent.status(friendlyName(mcpToolName)));
        try {
            Map<String, Object> arguments = OBJECT_MAPPER.readValue(input, MAP_TYPE);
            String result = mcpClient.callTool(mcpToolName, arguments);
            offerNavigationIfTakeoff(arguments, result);
            return result;
        } catch (McpClientException e) {
            LOG.error("MCP 调用失败 tool={}", mcpToolName, e);
            return buildErrorResponse("mcp_error", "MCP Server 调用失败: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("透传 Tool 异常 tool={}: {}", mcpToolName, e.getMessage(), e);
            return buildErrorResponse("internal_error", "内部错误: " + e.getMessage());
        }
    }

    /**
     * 若当前工具为 fh_takeoff 且执行成功，则向 NavigationHolder 写入驾驶舱导航信号。
     *
     * @param arguments 工具调用参数（含 drone_sn）
     * @param result    工具调用返回的 JSON 字符串
     */
    private void offerNavigationIfTakeoff(Map<String, Object> arguments, String result) {
        if (!TOOL_TAKEOFF.equals(mcpToolName)) {
            return;
        }
        if (!isTakeoffSuccess(result)) {
            return;
        }
        Object droneSn = arguments.get(PARAM_DRONE_SN);
        if (droneSn == null) {
            return;
        }
        NavigationHolder.offer(COCKPIT_PATH, Map.of(PARAM_DRONE_SN, droneSn.toString()));
        LOG.debug("McpProxyChatTool：写入驾驶舱导航信号 drone_sn={}", droneSn);
    }

    /**
     * 判断 fh_takeoff 工具返回值是否表示成功。
     * 判断依据：顶层 JSON 包含 "data" 字段且不包含 "error" 字段。
     *
     * @param result MCP 工具返回的原始 JSON 字符串
     * @return {@code true} 表示成功
     */
    private boolean isTakeoffSuccess(String result) {
        if (result == null || result.isBlank()) {
            return false;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(result);
            return node.has("data") && !node.has("error");
        } catch (JsonProcessingException e) {
            LOG.debug("McpProxyChatTool：起飞结果 JSON 解析失败，视为未成功", e);
            return false;
        }
    }

    /**
     * 返回工具的用户可见友好名称。未在映射表中的工具名打印 WARN 并返回通用描述。
     *
     * @param toolName MCP 工具名
     * @return 中文友好名称
     */
    private static String friendlyName(String toolName) {
        String name = FRIENDLY_NAMES.get(toolName);
        if (name == null) {
            LOG.warn("McpProxyChatTool：工具 {} 未在 FRIENDLY_NAMES 中定义，返回通用描述", toolName);
            return "执行操作";
        }
        return name;
    }
}
