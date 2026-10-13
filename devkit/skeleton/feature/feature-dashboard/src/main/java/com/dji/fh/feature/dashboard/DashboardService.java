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

package com.dji.fh.feature.dashboard;

import com.dji.fh.feature.dashboard.dto.CountStatsDto;
import com.dji.fh.feature.dashboard.dto.DashboardStatsDto;
import com.dji.fh.feature.dashboard.dto.DataResultStatsDto;
import com.dji.fh.feature.dashboard.dto.DeviceMarkerDto;
import com.dji.fh.feature.dashboard.dto.DockStatsDto;
import com.dji.fh.feature.dashboard.dto.DroneStatsDto;
import com.dji.fh.feature.dashboard.dto.ExecutionStatsDto;
import com.dji.fh.feature.dashboard.dto.FlightStatsDto;
import com.dji.fh.feature.dashboard.dto.RelayStatsDto;
import com.dji.fh.feature.dashboard.dto.TaskStatsDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.TaskSummary;
import com.dji.fh.feature.dashboard.spi.MediaCountService;
import com.dji.fh.feature.dashboard.spi.ModelCountService;
import com.dji.fh.feature.dashboard.spi.PersonnelCountService;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.feature.device.DeviceState;
import com.dji.fh.feature.device.DeviceStatusService;
import com.dji.fh.kernel.spi.event.DeviceType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 看板数据聚合服务。
 *
 * <p>从多个 SPI 聚合数据；SDK 模式下通过 {@code SdkDashboardAutoConfiguration}
 * 注入真实 SDK 实现，mock 模式下使用默认 mock 实现。
 *
 * <p>SDK 尚无对应接口的字段（中继站、报告、飞行时长/里程）标记为 mock。
 *
 * @author DJI DevKit Team
 */
public class DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardService.class);

    private static final int MOCK_RELAY_TOTAL = 10;
    private static final int MOCK_REPORT_COUNT = 15;

    private final DeviceStatusService deviceService;
    private final FlightTaskQueryService taskQueryService;
    private final WaylineQueryService waylineQueryService;
    private final MediaCountService mediaCountService;
    private final ModelCountService modelCountService;
    private final PersonnelCountService personnelCountService;

    /**
     * 构造函数。
     *
     * @param deviceService         设备状态服务
     * @param taskQueryService      飞行任务查询服务
     * @param waylineQueryService   航线查询服务
     * @param mediaCountService     媒体文件计数服务
     * @param modelCountService     模型文件计数服务
     * @param personnelCountService 人员计数服务
     */
    public DashboardService(DeviceStatusService deviceService,
                            FlightTaskQueryService taskQueryService,
                            WaylineQueryService waylineQueryService,
                            MediaCountService mediaCountService,
                            ModelCountService modelCountService,
                            PersonnelCountService personnelCountService) {
        this.deviceService = deviceService;
        this.taskQueryService = taskQueryService;
        this.waylineQueryService = waylineQueryService;
        this.mediaCountService = mediaCountService;
        this.modelCountService = modelCountService;
        this.personnelCountService = personnelCountService;
    }

    /**
     * 获取看板聚合统计数据。
     *
     * @param range 时间范围（week / month / recent_month / half_year / year）
     * @return 聚合 DTO
     */
    public DashboardStatsDto getStats(String range) {
        List<DeviceState> allDevices = deviceService.listAll();
        List<TaskSummary> tasks = queryTasksSafely(range);
        return new DashboardStatsDto(
                aggregateDocks(allDevices),
                aggregateDrones(allDevices),
                mockRelays(),
                buildCounts(),
                buildDataResults(range),
                aggregateTasks(tasks, range),
                aggregateExecution(tasks, range),
                aggregateFlight(tasks, range),
                buildDeviceMarkers(allDevices)
        );
    }

    private List<TaskSummary> queryTasksSafely(String range) {
        try {
            return taskQueryService.listTasks(range);
        } catch (Exception e) {
            LOG.warn("查询飞行任务失败，降级为空列表: {}", e.getMessage());
            return List.of();
        }
    }

    // ==================== 设备统计（真实数据）====================

    /**
     * 机场状态汇总。
     *
     * <p>字段映射为 {@code (total, idle, working, debugging, offline)}，
     * 其中 {@code idle} 取在线机场数、{@code working} 与 {@code debugging} 恒为 0 ——
     * 本服务不消费 {@code FlightTaskProgressEvent}，无从判断在线机场正在执行作业还是调试，
     * 因此一律计入「空闲」并保证四项之和恒等于 {@code total}。
     *
     * <p>数据源与 {@code GET /api/v1/devices} 完全同源（同一 {@link DeviceStatusService} 实例，
     * 见 {@link #getStats}），所以机场的在线判定若有误，两个出口会同时出错 ——
     * 现场现象为 {@code /devices} 返回 {@code online: true}，而看板显示
     * 「空闲 1（100.0%）、离线 0」，与之矛盾。
     *
     * @param allDevices 设备状态全量列表
     * @return 机场状态汇总，四项计数之和恒等于 total
     */
    private DockStatsDto aggregateDocks(List<DeviceState> allDevices) {
        List<DeviceState> docks = allDevices.stream()
                .filter(d -> d.deviceType() == DeviceType.DOCK)
                .toList();
        int total = docks.size();
        int online = (int) docks.stream().filter(DeviceState::online).count();
        return new DockStatsDto(total, online, 0, 0, total - online);
    }

    private DroneStatsDto aggregateDrones(List<DeviceState> allDevices) {
        List<DeviceState> drones = allDevices.stream()
                .filter(d -> d.deviceType() == DeviceType.DRONE)
                .toList();
        int total = drones.size();
        int online = (int) drones.stream().filter(DeviceState::online).count();
        return new DroneStatsDto(total, online, total - online);
    }

    private RelayStatsDto mockRelays() {
        return new RelayStatsDto(MOCK_RELAY_TOTAL, 0, MOCK_RELAY_TOTAL, true);
    }

    // ==================== 计数指标 ====================

    private CountStatsDto buildCounts() {
        int waylines = countWaylinesSafely();
        int personnel = countPersonnelSafely();
        boolean personnelIsMock = (personnel == 0);
        return new CountStatsDto(
                waylines,
                new CountStatsDto.MockableInt(personnel, personnelIsMock)
        );
    }

    private int countWaylinesSafely() {
        try {
            return waylineQueryService.countWaylines();
        } catch (Exception e) {
            LOG.warn("查询航线数失败，降级为 0: {}", e.getMessage());
            return 0;
        }
    }

    private int countPersonnelSafely() {
        try {
            return personnelCountService.countPersonnel();
        } catch (Exception e) {
            LOG.warn("查询人员数失败，降级为 0: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== 数据成果 ====================

    private DataResultStatsDto buildDataResults(String range) {
        int mediaTotalCount = queryMediaCountSafely();
        int modelTotalCount = queryModelCountSafely();

        int trendPoints = Math.min(toDays(range), 7);
        List<Integer> reportTrend = buildMockTrend(trendPoints);

        return new DataResultStatsDto(
                new DataResultStatsDto.TrendItem(mediaTotalCount, List.of()),
                new DataResultStatsDto.TrendItem(modelTotalCount, List.of()),
                new DataResultStatsDto.MockTrendItem(MOCK_REPORT_COUNT, reportTrend, true)
        );
    }

    private int queryMediaCountSafely() {
        try {
            return mediaCountService.countMediaFiles();
        } catch (Exception e) {
            LOG.warn("查询媒体文件总数失败，降级为 0: {}", e.getMessage());
            return 0;
        }
    }

    private int queryModelCountSafely() {
        try {
            return modelCountService.countModels();
        } catch (Exception e) {
            LOG.warn("查询模型总数失败，降级为 0: {}", e.getMessage());
            return 0;
        }
    }

    private List<Integer> buildMockTrend(int points) {
        List<Integer> trend = new ArrayList<>();
        long seed = LocalDate.now().toEpochDay();
        Random rng = new Random(seed);
        for (int i = 0; i < points; i++) {
            trend.add(rng.nextInt(5, 50));
        }
        return trend;
    }

    // ==================== 任务分布（真实数据）====================

    private TaskStatsDto aggregateTasks(List<TaskSummary> tasks, String range) {
        int days = toDays(range);
        Map<String, int[]> dailyMap = initDailyMap(days);

        for (TaskSummary task : tasks) {
            int[] counts = dailyMap.get(task.createDate());
            if (counts == null) {
                continue;
            }
            if (task.taskType() == 2) {
                counts[0]++;
            } else {
                counts[1]++;
            }
        }

        List<TaskStatsDto.DailyEntry> daily = dailyMap.entrySet().stream()
                .map(e -> new TaskStatsDto.DailyEntry(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();

        return new TaskStatsDto(daily);
    }

    // ==================== 执行结果（真实数据）====================

    private ExecutionStatsDto aggregateExecution(List<TaskSummary> tasks, String range) {
        int days = toDays(range);
        Map<String, int[]> dailyMap = initDailyMap(days);

        for (TaskSummary task : tasks) {
            int[] counts = dailyMap.get(task.createDate());
            if (counts == null) {
                continue;
            }
            switch (task.status()) {
                case "SUCCESS" -> counts[0]++;
                case "PAUSED", "CANCELLED" -> counts[1]++;
                case "FAILED" -> counts[2]++;
                default -> { /* CREATED / PREPARING / QUEUED / EXECUTING 不计入已完结统计 */ }
            }
        }

        List<ExecutionStatsDto.DailyEntry> daily = dailyMap.entrySet().stream()
                .map(e -> new ExecutionStatsDto.DailyEntry(
                        e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .toList();

        return new ExecutionStatsDto(daily);
    }

    // ==================== 飞行数据（count+duration 真实，km 待核实）====================

    /** 一分钟的秒数，用于时长换算。 */
    private static final double SECONDS_PER_MINUTE = 60.0;

    /** 一公里的米数，用于里程换算。 */
    private static final double METERS_PER_KILOMETER = 1000.0;

    /** 时长/里程保留 2 位小数的换算因子。 */
    private static final double TWO_DECIMAL_SCALE = 100.0;

    /**
     * 汇总飞行数据。
     *
     * <p>时长先按秒累加、最后统一换算为分钟，避免逐日换算成小时后再求和导致
     * 短航次（不足 6 分钟）被舍入抹平。里程通过逐任务并发查询轨迹获取
     * （{@code SdkFlightTaskQueryService.fetchTaskDistances}），已在任务查询阶段完成。
     *
     * @param tasks 任务列表
     * @param range 统计区间
     * @return 飞行数据统计
     */
    private FlightStatsDto aggregateFlight(List<TaskSummary> tasks, String range) {
        int days = toDays(range);
        Map<String, int[]> dailyCountMap = initDailyMap(days);
        Map<String, Double> dailySecondsMap = new LinkedHashMap<>();
        dailyCountMap.keySet().forEach(k -> dailySecondsMap.put(k, 0.0));
        Map<String, Double> dailyMetersMap = new LinkedHashMap<>();
        dailyCountMap.keySet().forEach(k -> dailyMetersMap.put(k, 0.0));
        int totalCount = 0;
        double totalSeconds = 0.0;
        double totalMeters = 0.0;

        for (TaskSummary task : tasks) {
            if (!"SUCCESS".equals(task.status())) {
                continue;
            }
            int[] counts = dailyCountMap.get(task.createDate());
            if (counts != null) {
                counts[0]++;
                totalCount++;
                totalSeconds += task.durationSeconds();
                dailySecondsMap.merge(task.createDate(), task.durationSeconds(), Double::sum);
                totalMeters += task.distanceMeters();
                dailyMetersMap.merge(task.createDate(), task.distanceMeters(), Double::sum);
            }
        }

        List<FlightStatsDto.DailyEntry> daily = dailyCountMap.entrySet().stream()
                .map(e -> new FlightStatsDto.DailyEntry(
                        e.getKey(),
                        e.getValue()[0],
                        toMinutes(dailySecondsMap.getOrDefault(e.getKey(), 0.0)),
                        toKilometers(dailyMetersMap.getOrDefault(e.getKey(), 0.0))))
                .toList();

        return new FlightStatsDto(
                totalCount,
                toMinutes(totalSeconds),
                new FlightStatsDto.MockableDouble(toKilometers(totalMeters), false),
                daily
        );
    }

    /**
     * 将秒换算为分钟，保留 2 位小数。
     *
     * @param seconds 秒数
     * @return 分钟数
     */
    private static double toMinutes(double seconds) {
        return Math.round(seconds / SECONDS_PER_MINUTE * TWO_DECIMAL_SCALE) / TWO_DECIMAL_SCALE;
    }

    /**
     * 将米换算为公里，保留 2 位小数。
     *
     * @param meters 米数
     * @return 公里数
     */
    private static double toKilometers(double meters) {
        return Math.round(meters / METERS_PER_KILOMETER * TWO_DECIMAL_SCALE) / TWO_DECIMAL_SCALE;
    }

    // ==================== 设备地图标记（真实数据）====================

    private List<DeviceMarkerDto> buildDeviceMarkers(List<DeviceState> allDevices) {
        List<DeviceMarkerDto> markers = new ArrayList<>();
        for (DeviceState device : allDevices) {
            if (!device.online()) {
                continue;
            }
            double lat = 0;
            double lng = 0;
            if (device.lastOsd() != null) {
                lat = extractDouble(device.lastOsd().attributes(), "lat", "latitude");
                lng = extractDouble(device.lastOsd().attributes(), "lng", "longitude");
            }
            String type = device.deviceType() == DeviceType.DOCK ? "dock" : "drone";
            markers.add(new DeviceMarkerDto(device.sn(), type, device.name(), "online", lat, lng));
        }
        return markers;
    }

    // ==================== 工具方法 ====================

    /**
     * 从 OSD attributes 中提取 double 值，依次尝试 primaryKey 和 fallbackKey。
     * 兼容真实 SDK（"lat"/"lng"）和第三方数据源（"latitude"/"longitude"）。
     */
    private static double extractDouble(Map<String, Object> attrs, String primaryKey, String fallbackKey) {
        Object val = attrs.get(primaryKey);
        if (val == null) {
            val = attrs.get(fallbackKey);
        }
        return val instanceof Number n ? n.doubleValue() : 0.0;
    }

    private Map<String, int[]> initDailyMap(int days) {
        LocalDate today = LocalDate.now();
        Map<String, int[]> map = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            map.put(today.minusDays(i).toString(), new int[3]);
        }
        return map;
    }

    private int toDays(String range) {
        return switch (range) {
            case "week", "recent_week" -> 7;
            case "month", "recent_month" -> 30;
            case "half_year" -> 180;
            case "year" -> 365;
            default -> 30;
        };
    }
}
