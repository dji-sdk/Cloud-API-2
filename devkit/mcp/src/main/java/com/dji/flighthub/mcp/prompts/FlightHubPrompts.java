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

package com.dji.flighthub.mcp.prompts;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FlightHub 业务流程 Prompt 定义 — 5 大最佳实践流程模板。
 *
 * <p>每个 Prompt 为 LLM Agent 提供一个完整的业务流程编排指南，
 * 包含：调用顺序、前置条件、错误处理分支、资源清理要求。
 *
 * <p>Prompt 内容使用英文（消费方为 LLM），遵循 MCP 协议 prompts/get 规范。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FlightHubPrompts {

    /**
     * 获取所有 prompt 定义。
     *
     * @return 5 个业务流 prompt 定义列表
     */
    public List<PromptDefinition> getDefinitions() {
        return List.of(
                buildQuickstartPrompt(),
                buildFlyDronePrompt(),
                buildRunWaylinePrompt(),
                buildCheckDevicePrompt(),
                buildPostFlightPrompt()
        );
    }

    // ==================== Prompt 1: Quickstart ====================

    private PromptDefinition buildQuickstartPrompt() {
        return new PromptDefinition(
                "quickstart",
                "SDK initialization workflow: connect to FlightHub, establish session, and list available devices. "
                        + "Start here if this is your first interaction.",
                List.of(),
                args -> List.of(PromptDefinition.Message.userText(QUICKSTART_TEMPLATE))
        );
    }

    private static final String QUICKSTART_TEMPLATE = """
            ## FlightHub Quickstart Guide

            Follow these steps to initialize the SDK and discover available devices.

            ### Step 1: Connect to FlightHub
            ```
            fh_connect(workspace_uuid="<your-workspace-uuid>")
            ```
            - This initializes the SDK and establishes a session
            - **MUST** be called before any other tool
            - On success: returns `session_id` and workspace info
            - On failure: check error_message for connectivity issues

            ### Step 2: Verify Session
            ```
            fh_session_info()
            ```
            - Confirms your session is active
            - Shows workspace name, max security level, and active resources

            ### Step 3: List Devices
            ```
            fh_device_list()
            ```
            - Returns all drones and docks in the workspace
            - Each device has a `device_sn` (serial number) used in subsequent calls
            - Note device `online_status`: only online devices accept commands

            ### Step 4: Get Device Details (optional)
            ```
            fh_drone_snapshot(drone_sn="<sn>")
            fh_dock_snapshot(dock_sn="<sn>")
            ```
            - Provides real-time telemetry: position, altitude, battery, signal strength

            ### Error Handling
            - If `fh_connect` fails: verify workspace_uuid and network connectivity
            - If devices show offline: they may be powered down or out of range
            - Always check `error_code == 0` before using response data

            ### Next Steps
            After initialization, you can:
            - **Fly a drone manually**: use the `fly-drone` prompt
            - **Run an automated wayline mission**: use the `run-wayline` prompt
            - **Check device health**: use the `check-device` prompt
            """;

    // ==================== Prompt 2: Fly Drone (DRC) ====================

    private PromptDefinition buildFlyDronePrompt() {
        return new PromptDefinition(
                "fly-drone",
                "Real-time drone flight control (DRC) workflow: acquire authority, connect DRC link, "
                        + "takeoff, fly to target, and safely return. Requires human confirmation for critical steps.",
                List.of(new PromptDefinition.Argument("drone_sn", "Target drone serial number", true)),
                args -> {
                    String droneSn = args.getOrDefault("drone_sn", "<drone_sn>");
                    return List.of(PromptDefinition.Message.userText(
                            FLY_DRONE_TEMPLATE.replace("{drone_sn}", droneSn)));
                }
        );
    }

    private static final String FLY_DRONE_TEMPLATE = """
            ## Real-Time Drone Flight Control (DRC)

            Target drone: `{drone_sn}`

            ### Prerequisites
            - Session established via `fh_connect`
            - Drone is online (verify with `fh_drone_snapshot`)
            - No other operator holds flight control authority

            ### Step 1: Acquire Flight Control Authority
            ```
            fh_control_acquire(drone_sn="{drone_sn}")
            ```
            - ⚠️ Requires human confirmation (L1 operation)
            - If error_code ≠ 0: another operator may hold authority
            - **CRITICAL**: You MUST release authority when done (Step 7)

            ### Step 2: Establish DRC Link
            ```
            fh_drc_connect(drone_sn="{drone_sn}")
            ```
            - ⚠️ Requires human confirmation (L1 operation)
            - Creates low-latency real-time control channel
            - Verify link_status in response

            ### Step 3: Takeoff
            ```
            fh_takeoff(drone_sn="{drone_sn}", altitude=20)
            ```
            - ⚠️ Requires human confirmation (L2 operation)
            - Default safe altitude: 20m
            - Wait for takeoff completion before next command

            ### Step 4: Fly to Target (choose one)

            **Option A — Coordinate flight:**
            ```
            fh_flyto(drone_sn="{drone_sn}", latitude=22.5431, longitude=113.9478, altitude=50)
            ```

            **Option B — Point of Interest orbit:**
            ```
            fh_poi(drone_sn="{drone_sn}", latitude=22.5431, longitude=113.9478, altitude=50, radius=30)
            ```

            **Option C — Manual stick control (advanced):**
            ```
            fh_stick_move(drone_sn="{drone_sn}", pitch=0.5, roll=0.0, yaw=0.0, throttle=0.3)
            ```

            ### Step 5: Monitor Position
            ```
            fh_osd_snapshot(drone_sn="{drone_sn}")
            ```
            - Check latitude, longitude, altitude, battery_percent
            - If battery < 30%: initiate return home immediately

            ### Step 6: Return Home
            ```
            fh_return_home(drone_sn="{drone_sn}")
            ```
            - ⚠️ Requires human confirmation
            - Drone returns to home point and lands automatically

            ### Step 7: Cleanup (ALWAYS execute, even on errors)
            ```
            fh_drc_disconnect(drone_sn="{drone_sn}")
            fh_control_release(drone_sn="{drone_sn}")
            ```
            - **ALWAYS** release control authority in a finally block
            - Failing to release blocks all other operators

            ### Emergency
            ```
            fh_emergency_stop(drone_sn="{drone_sn}")
            ```
            - ⚠️ L3 — IMMEDIATE motor stop, drone will fall
            - Use ONLY when collision is imminent and no other option exists

            ### Error Handling
            - Any step failure: log error, skip to Step 7 (cleanup)
            - DRC link lost: authority is still held, retry connect or release
            - Low battery warning: immediately execute return_home
            """;

    // ==================== Prompt 3: Run Wayline ====================

    private PromptDefinition buildRunWaylinePrompt() {
        return new PromptDefinition(
                "run-wayline",
                "Automated wayline mission workflow: upload route, create task, execute on dock, "
                        + "and monitor progress until completion.",
                List.of(new PromptDefinition.Argument("dock_sn", "Target dock serial number", true)),
                args -> {
                    String dockSn = args.getOrDefault("dock_sn", "<dock_sn>");
                    return List.of(PromptDefinition.Message.userText(
                            RUN_WAYLINE_TEMPLATE.replace("{dock_sn}", dockSn)));
                }
        );
    }

    private static final String RUN_WAYLINE_TEMPLATE = """
            ## Automated Wayline Mission

            Target dock: `{dock_sn}`

            ### Prerequisites
            - Session established via `fh_connect`
            - Dock is online (verify with `fh_dock_snapshot`)
            - Wayline file prepared (KMZ format)

            ### Step 1: Upload Wayline
            ```
            fh_wayline_upload(file_path="/path/to/route.kmz", wayline_name="Survey Mission 1")
            ```
            - ⚠️ Requires human confirmation (L1 operation)
            - Returns `wayline_id` on success
            - Save this ID for task creation

            ### Step 2: Create Flight Task
            ```
            fh_flight_task(action="create", dock_sn="{dock_sn}", wayline_id="<wayline_id>",
                           task_name="Auto Survey", execute_time="immediate")
            ```
            - Binds wayline to a specific dock
            - `execute_time`: "immediate" or ISO-8601 timestamp for scheduled execution

            ### Step 3: Execute Task
            ```
            fh_wayline_execute(dock_sn="{dock_sn}", task_id="<task_id>")
            ```
            - ⚠️ Requires human confirmation (L2 operation)
            - Dock opens cover → drone powers on → takes off → follows wayline

            ### Step 4: Monitor Progress
            ```
            fh_wayline_status(dock_sn="{dock_sn}")
            ```
            - Poll every 10-30 seconds until `status == "completed"` or `status == "failed"`
            - Shows: current_waypoint, progress_percent, estimated_remaining_time

            ### Step 5: Pause/Resume (if needed)
            ```
            fh_wayline_control(dock_sn="{dock_sn}", action="pause")
            fh_wayline_control(dock_sn="{dock_sn}", action="resume")
            ```
            - ⚠️ Requires human confirmation

            ### Step 6: Review Results
            ```
            fh_media_list(device_sn="{dock_sn}")
            fh_flight_record(device_sn="{dock_sn}")
            ```
            - Check captured media (photos/video) after mission completes
            - Review flight record for any anomalies

            ### Error Handling
            - Upload failure: check file format (must be DJI KMZ)
            - Task execution failure: check dock status and weather conditions
            - If drone doesn't return: check `fh_wayline_status` for error details
            - Dock cover not opening: use `fh_dock_control(dock_sn, action="cover_open")`
            """;

    // ==================== Prompt 4: Check Device ====================

    private PromptDefinition buildCheckDevicePrompt() {
        return new PromptDefinition(
                "check-device",
                "Device health check workflow: query status, get telemetry snapshot, "
                        + "review HMS alerts, and check firmware version.",
                List.of(new PromptDefinition.Argument("device_sn", "Device serial number (drone or dock)", true)),
                args -> {
                    String deviceSn = args.getOrDefault("device_sn", "<device_sn>");
                    return List.of(PromptDefinition.Message.userText(
                            CHECK_DEVICE_TEMPLATE.replace("{device_sn}", deviceSn)));
                }
        );
    }

    private static final String CHECK_DEVICE_TEMPLATE = """
            ## Device Health Check

            Target device: `{device_sn}`

            ### Step 1: Get Real-Time Snapshot
            ```
            fh_drone_snapshot(drone_sn="{device_sn}")
            ```
            or for docks:
            ```
            fh_dock_snapshot(dock_sn="{device_sn}")
            ```
            - Shows: position, altitude, battery, signal, temperature
            - If offline: device may be powered down

            ### Step 2: Check HMS Alerts
            ```
            fh_hms(device_sn="{device_sn}", action="list")
            ```
            - HMS = Health Management System
            - Shows active warnings/errors (battery degradation, sensor issues, etc.)
            - Severity levels: notice / caution / warning / error
            - Critical errors: do NOT fly until resolved

            ### Step 3: Check Firmware
            ```
            fh_firmware(device_sn="{device_sn}", action="status")
            ```
            - Shows current version and available updates
            - If update available: consider `fh_firmware(action="upgrade")` during maintenance window

            ### Step 4: Review Flight Records (optional)
            ```
            fh_flight_record(device_sn="{device_sn}")
            ```
            - Historical flights: duration, distance, anomalies
            - Useful for maintenance scheduling

            ### Step 5: Run Diagnosis (if issues suspected)
            ```
            fh_diagnosis(device_sn="{device_sn}", action="start")
            ```
            - Runs self-test on device hardware
            - Check results with `fh_diagnosis(action="result")`

            ### Summary Checklist
            Report to the user:
            - [ ] Online status
            - [ ] Battery level (warn if < 30%)
            - [ ] Active HMS alerts (list severity)
            - [ ] Firmware up to date?
            - [ ] Recent flight hours
            """;

    // ==================== Prompt 5: Post-Flight ====================

    private PromptDefinition buildPostFlightPrompt() {
        return new PromptDefinition(
                "post-flight",
                "Post-flight data processing workflow: retrieve media files, download selected items, "
                        + "initiate 3D reconstruction, and archive flight records.",
                List.of(new PromptDefinition.Argument("drone_sn", "Drone serial number that completed the flight", true)),
                args -> {
                    String droneSn = args.getOrDefault("drone_sn", "<drone_sn>");
                    return List.of(PromptDefinition.Message.userText(
                            POST_FLIGHT_TEMPLATE.replace("{drone_sn}", droneSn)));
                }
        );
    }

    private static final String POST_FLIGHT_TEMPLATE = """
            ## Post-Flight Data Processing

            Source drone: `{drone_sn}`

            ### Step 1: List Captured Media
            ```
            fh_media_list(device_sn="{drone_sn}")
            ```
            - Returns photos and videos from the latest flight
            - Each item has: media_id, file_name, size, capture_time, type (photo/video)

            ### Step 2: Download Selected Media
            ```
            fh_media_download(device_sn="{drone_sn}", media_id="<id>", save_path="/output/")
            ```
            - Download specific files for processing
            - For bulk download: iterate over media_list results

            ### Step 3: Initiate 3D Reconstruction (optional)
            ```
            fh_terra(action="create", device_sn="{drone_sn}", task_name="Site Survey 3D Model")
            ```
            - Creates a Terra reconstruction task from captured survey photos
            - Processing takes 10-60 minutes depending on photo count

            ### Step 4: Monitor Terra Progress
            ```
            fh_terra(action="status", task_id="<terra_task_id>")
            ```
            - Poll until status == "completed"
            - On completion: model download URL is provided

            ### Step 5: Archive Flight Record
            ```
            fh_flight_record(device_sn="{drone_sn}")
            ```
            - Captures: flight path, duration, max altitude, distance
            - Useful for compliance reporting and maintenance tracking

            ### Step 6: Cleanup Media (optional)
            ```
            fh_media_delete(device_sn="{drone_sn}", media_ids=["<id1>", "<id2>"])
            ```
            - ⚠️ Requires human confirmation
            - Free device storage after confirmed download
            - Only delete after verifying downloads are intact

            ### Best Practices
            - Always download before deleting
            - Tag media with mission name for traceability
            - Schedule Terra processing during off-peak hours
            - Retain flight records for at least 90 days (compliance)
            """;
}
