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

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fh_osd_snapshot — 获取无人机实时 OSD 遥测数据快照。
 * 返回位置、姿态、电量、速度、飞行模式、GPS 信号、风速等丰富信息，
 * 专为飞行过程中的实时监控设计，数据粒度优于 fh_drone_snapshot。
 *
 * <p>安全等级：L0（只读，无需 confirm）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhOsdSnapshotTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhOsdSnapshotTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_osd_snapshot";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** Mock 纬度（固定值，保证测试可断言） */
    private static final double MOCK_LATITUDE = 22.5431;

    /** Mock 经度（固定值，保证测试可断言） */
    private static final double MOCK_LONGITUDE = 113.9465;

    /** Mock 相对高度（m） */
    private static final double MOCK_HEIGHT = 50.0;

    /** Mock 海拔高度（m） */
    private static final double MOCK_ELEVATION = 85.0;

    /** Mock 电量百分比 */
    private static final int MOCK_BATTERY_PERCENT = 82;

    /** Mock 俯仰角（度） */
    private static final double MOCK_PITCH = -2.1;

    /** Mock 横滚角（度） */
    private static final double MOCK_ROLL = 0.3;

    /** Mock 偏航角（度） */
    private static final double MOCK_YAW = 145.0;

    /** Mock 水平飞行速度（m/s，飞行中） */
    private static final double MOCK_HORIZONTAL_SPEED_FLYING = 5.2;

    /** Mock 水平飞行速度（m/s，静止） */
    private static final double MOCK_HORIZONTAL_SPEED_IDLE = 0.0;

    /** Mock 上升速度（m/s，起飞中） */
    private static final double MOCK_VERTICAL_SPEED_TAKING_OFF = 2.0;

    /** Mock 垂直速度（m/s，非起飞状态） */
    private static final double MOCK_VERTICAL_SPEED_DEFAULT = 0.0;

    /** Mock GPS 卫星数 */
    private static final int MOCK_GPS_SATELLITES = 18;

    /** Mock 与返航点距离（m） */
    private static final double MOCK_HOME_DISTANCE = 235.0;

    /** Mock 估算风速（m/s） */
    private static final double MOCK_WIND_SPEED_EST = 3.5;

    /** 垂直速度阈值（m/s）：绝对值低于此值视为已停止上升/下降 */
    private static final double VERTICAL_SPEED_THRESHOLD = 0.3;

    /** 水平速度阈值（m/s）：低于此值视为无水平位移 */
    private static final double HORIZONTAL_SPEED_THRESHOLD = 0.5;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhOsdSnapshotTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 返回本工具的 MCP 协议定义，包含名称、描述和 inputSchema。
     *
     * @return MCP 工具定义 Map
     */
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Get real-time OSD telemetry data: position, attitude, battery, speed, flight mode, "
                        + "GPS signal, wind. Richer than fh_drone_snapshot — designed for in-flight monitoring.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行 OSD 实时遥测快照操作。
     * 前置条件：session 有效。安全等级 L0（只读）。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 2. 校验 drone_sn 参数不为空
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 3. Mock 模式：返回确定性遥测数据
        if (sdkService.isMockMode()) {
            return buildMockOsdSnapshot(droneSn, session);
        }

        // 4. 真实 SDK 调用（生产路径，待接入）
        return executeWithRealSdk(droneSn, session);
    }

    /**
     * 构建 Mock 模式下的 OSD 遥测快照。
     * 所有值均为固定常量，保证单元测试可断言。
     *
     * @param droneSn 目标无人机 SN
     * @param session 当前 session 上下文（用于阶段修正）
     * @return 遥测数据响应
     */
    private Map<String, Object> buildMockOsdSnapshot(String droneSn, SessionContext session) {
        FlightPhase phase = session.getFlightPhase();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("latitude", MOCK_LATITUDE);
        data.put("longitude", MOCK_LONGITUDE);
        data.put("height", MOCK_HEIGHT);
        data.put("elevation", MOCK_ELEVATION);
        data.put("battery_percent", MOCK_BATTERY_PERCENT);
        data.put("pitch", MOCK_PITCH);
        data.put("roll", MOCK_ROLL);
        data.put("yaw", MOCK_YAW);
        data.put("horizontal_speed", phase == FlightPhase.IDLE ? MOCK_HORIZONTAL_SPEED_IDLE : MOCK_HORIZONTAL_SPEED_FLYING);
        data.put("vertical_speed", phase == FlightPhase.TAKING_OFF ? MOCK_VERTICAL_SPEED_TAKING_OFF : MOCK_VERTICAL_SPEED_DEFAULT);
        data.put("flight_mode", phase.name().toLowerCase());
        data.put("gps_satellites", MOCK_GPS_SATELLITES);
        data.put("home_distance", MOCK_HOME_DISTANCE);
        data.put("wind_speed_est", MOCK_WIND_SPEED_EST);
        data.put("flight_phase", phase.name());
        data.put("_mock", true);

        // 阶段自动修正：TAKING_OFF → HOVERING（速度接近零表示已完成起飞）
        double hSpeed = ((Number) data.get("horizontal_speed")).doubleValue();
        double vSpeed = ((Number) data.get("vertical_speed")).doubleValue();
        FlightPhase correctedPhase = correctFlightPhase(session, hSpeed, vSpeed);
        data.put("flight_phase", correctedPhase.name());

        String message = String.format("OSD %s: battery=%d%%, height=%.1fm, phase=%s",
                droneSn, MOCK_BATTERY_PERCENT, MOCK_HEIGHT, correctedPhase.name());

        log.info("[MOCK] fh_osd_snapshot for drone={} phase={}", droneSn, correctedPhase);
        return successResult(data, message);
    }

    /** OSD 数据采集超时时间（秒） */
    private static final int OSD_TIMEOUT_SECONDS = 5;

    /** OSD 数据通道数量（位置、电量、姿态、速度、飞行模式） */
    private static final int OSD_CHANNEL_COUNT = 5;

    /**
     * 真实 SDK 模式下获取 OSD 遥测数据。
     * 使用一次性订阅模式（listen → 等待首帧回调 → cancel）采集多通道遥测数据。
     * 各通道独立订阅，使用 CountDownLatch 等待所有通道返回或超时。
     *
     * @param droneSn 目标无人机 SN
     * @param session 当前 session 上下文
     * @return 遥测数据响应
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(String droneSn, SessionContext session) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            DroneOsdModule osdModule = drone.getDroneOsdModule();
            if (osdModule == null) {
                return errorResult("DroneOsdModule unavailable for: " + droneSn,
                        "Drone does not support OSD telemetry. Check device capabilities.");
            }

            // ConcurrentHashMap：5 个 SDK 回调在不同线程上并发写入
            Map<String, Object> snapshot = new ConcurrentHashMap<>();
            snapshot.put("drone_sn", droneSn);

            // 使用 CountDownLatch 等待所有 OSD 通道返回首帧数据
            CountDownLatch latch = new CountDownLatch(OSD_CHANNEL_COUNT);

            // 订阅位置信息
            int locId = osdModule.listenAircraftLocation(location -> {
                snapshot.put("latitude", location.getLatitude());
                snapshot.put("longitude", location.getLongitude());
                snapshot.put("height", location.getHeight());
                snapshot.put("elevation", location.getElevation());
                latch.countDown();
            });

            // 订阅电池信息
            int batId = osdModule.listenAircraftBatteryInfo(battery -> {
                snapshot.put("battery_percent", battery.getCapacityPercent());
                snapshot.put("battery_landing_power", battery.getLandingPower());
                latch.countDown();
            });

            // 订阅姿态信息
            int attId = osdModule.listenAircraftAttitude(attitude -> {
                snapshot.put("pitch", attitude.getAttitudePitch());
                snapshot.put("roll", attitude.getAttitudeRoll());
                snapshot.put("yaw", attitude.getAttitudeHead());
                latch.countDown();
            });

            // 订阅速度信息
            int spdId = osdModule.listenAircraftSpeed(speed -> {
                snapshot.put("horizontal_speed", speed.getHorizontalSpeed());
                snapshot.put("vertical_speed", speed.getVerticalSpeed());
                latch.countDown();
            });

            // 订阅飞行模式
            int modeId = osdModule.listenAircraftModeCode(modeCode -> {
                snapshot.put("mode_code", modeCode.getModeCode().name());
                latch.countDown();
            });

            // 等待所有通道返回或超时
            boolean allReceived;
            try {
                allReceived = latch.await(OSD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } finally {
                // 无论成功或超时，必须取消所有订阅
                if (locId >= 0) {
                    osdModule.cancelListenAircraftLocation(locId);
                }
                if (batId >= 0) {
                    osdModule.cancelListenAircraftBatteryInfo(batId);
                }
                if (attId >= 0) {
                    osdModule.cancelListenAircraftAttitude(attId);
                }
                if (spdId >= 0) {
                    osdModule.cancelListenAircraftSpeed(spdId);
                }
                if (modeId >= 0) {
                    osdModule.cancelListenAircraftModeCode(modeId);
                }
            }

            // 补充 session 信息
            snapshot.put("flight_phase", session.getFlightPhase().name());

            // 阶段自动修正：TAKING_OFF → HOVERING
            Object hSpeedObj = snapshot.get("horizontal_speed");
            Object vSpeedObj = snapshot.get("vertical_speed");
            if (hSpeedObj instanceof Number && vSpeedObj instanceof Number) {
                FlightPhase correctedPhase = correctFlightPhase(
                        session,
                        ((Number) hSpeedObj).doubleValue(),
                        ((Number) vSpeedObj).doubleValue());
                snapshot.put("flight_phase", correctedPhase.name());
            }

            if (!allReceived) {
                snapshot.put("_warning", "Some OSD channels did not respond within "
                        + OSD_TIMEOUT_SECONDS + "s. Data may be partial.");
                log.warn("[REAL] fh_osd_snapshot for drone={}: partial data (timeout)", droneSn);
            }

            String message = String.format("OSD %s: battery=%s%%, height=%sm, phase=%s",
                    droneSn,
                    snapshot.getOrDefault("battery_percent", "N/A"),
                    snapshot.getOrDefault("height", "N/A"),
                    session.getFlightPhase().name());

            log.info("[REAL] fh_osd_snapshot for drone={}: channels_received={}",
                    droneSn, OSD_CHANNEL_COUNT - latch.getCount());
            return successResult(snapshot, message);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("fh_osd_snapshot interrupted for drone {}", droneSn, e);
            return errorResult("OSD snapshot interrupted",
                    "Operation was interrupted. Try again.");
        } catch (Exception e) {
            log.error("fh_osd_snapshot error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 根据遥测数据修正飞行阶段。
     * 当 session 阶段为 TAKING_OFF 但速度接近零时，判定无人机已完成起飞进入悬停，
     * 自动修正为 HOVERING 并更新 session 状态。
     *
     * @param session         当前 session 上下文
     * @param horizontalSpeed 水平速度（m/s）
     * @param verticalSpeed   垂直速度（m/s）
     * @return 修正后的飞行阶段
     */
    FlightPhase correctFlightPhase(
            SessionContext session, double horizontalSpeed, double verticalSpeed) {
        FlightPhase currentPhase = session.getFlightPhase();
        if (currentPhase == FlightPhase.TAKING_OFF
                && Math.abs(verticalSpeed) < VERTICAL_SPEED_THRESHOLD
                && Math.abs(horizontalSpeed) < HORIZONTAL_SPEED_THRESHOLD) {
            session.setFlightPhase(FlightPhase.HOVERING);
            log.info("飞行阶段自动修正: TAKING_OFF → HOVERING（vertical_speed={}, horizontal_speed={}）",
                    verticalSpeed, horizontalSpeed);
            return FlightPhase.HOVERING;
        }
        return currentPhase;
    }
}
