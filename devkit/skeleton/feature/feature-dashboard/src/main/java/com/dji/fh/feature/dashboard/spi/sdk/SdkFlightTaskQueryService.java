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

package com.dji.fh.feature.dashboard.spi.sdk;

import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import com.dji.fh.kernel.spi.util.DateTimeUtils;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackParams;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackResults;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.Workspace;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 飞行任务查询真实 SDK 实现。
 *
 * <p>通过 {@code FlightTaskModule.getFlightTaskList()} 分页查询任务，
 * 自动获取机场 SN 作为过滤条件（SDK 要求至少传入 1 个 SN 才能返回数据）。
 * 由于 {@code task_type} 无法表达"不筛选"，需按类型扇出查询后合并，详见 {@link #QUERY_TASK_TYPES}。
 *
 * @author DJI DevKit Team
 */
public class SdkFlightTaskQueryService implements FlightTaskQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkFlightTaskQueryService.class);
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 20;

    /** 可派发飞行任务的网关设备类别（大小写不敏感匹配） */
    private static final Set<String> GATEWAY_DEVICE_CLASSES = Set.of("DOCK", "AIRPORT", "BASE_STATION");

    /** 机场 SN 缓存有效期（毫秒），避免每次分页请求都同步阻塞查设备列表 */
    private static final long AIRPORT_SN_CACHE_TTL_MS = 30_000L;

    /** 逐任务轨迹查询的固定线程池大小（SDK 回调需在专用线程执行） */
    private static final int TRACK_QUERY_THREADS = 8;

    /** 逐任务轨迹查询的固定线程池 */
    private static final ExecutorService TRACK_QUERY_EXECUTOR =
            Executors.newFixedThreadPool(TRACK_QUERY_THREADS, r -> {
                Thread t = new Thread(r, "flight-track-query");
                t.setDaemon(true);
                return t;
            });

    /** 并发轨迹查询整体超时（秒），超时后已完成的返回，未完成的任务里程按 0 计 */
    private static final long TRACK_QUERY_TIMEOUT_SECONDS = 10;

    /**
     * 列表查询需要逐一扇出的 task_type 取值。
     *
     * <p>SDK 的 {@code FlightTaskListParams.taskType} 是原始 {@code int}，无法从请求中省略，
     * 未显式赋值也会以 0 序列化；而平台把 0 当作具体类型来过滤（只返回即时任务），
     * 并非协议文档所述的"不筛选"。因此要拿到全量任务，只能对每种类型分别查询后合并。
     *
     * <p>取值对齐平台 TaskType 枚举：0=未知, 1=即时, 2=重复定时（父任务，仅存重复规则）,
     * 3=单次定时, 4=子任务（重复定时的单次执行）, 5=连续执行, 6=连续执行子任务。
     * 4/6 是重复任务的每次实际执行记录，列表需展示，故一并扇出。
     */
    private static final List<Integer> QUERY_TASK_TYPES = List.of(0, 1, 2, 3, 4, 5, 6);

    /**
     * SDK 任务状态码 → 展示字符串映射。
     *
     * <p>参照协议任务状态枚举（message.h:1401, value.h:694-702）：
     * 0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止,
     * 5=成功, 6=已挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中。
     * 6 为协议允许下发暂停类状态，前端统一呈现"已暂停"；
     * 7/8 属于终态失败；9（PREPARING）/ 10（QUEUED）是执行前置阶段，
     * 平台会在 begin_at 之前数小时就置为 9，若与 2 一同呈现为"执行中"
     * 会让用户以为无人机已经在飞，故独立成两个状态。
     */
    private static final Map<Integer, String> STATUS_MAP = Map.ofEntries(
            Map.entry(0, "CREATED"),
            Map.entry(1, "FAILED"),
            Map.entry(2, "EXECUTING"),
            Map.entry(3, "PAUSED"),
            Map.entry(4, "CANCELLED"),
            Map.entry(5, "SUCCESS"),
            Map.entry(6, "PAUSED"),
            Map.entry(7, "FAILED"),
            Map.entry(8, "FAILED"),
            Map.entry(9, "PREPARING"),
            Map.entry(10, "QUEUED")
    );

    private final DeviceQueryService deviceQueryService;
    private final WaylineQueryService waylineQueryService;
    private final AtomicReference<CachedSns> cachedAirportSns = new AtomicReference<>();

    /**
     * 构造函数。
     *
     * @param deviceQueryService  设备查询服务，用于获取机场 SN 及设备名
     * @param waylineQueryService 航线查询服务，用于按 UUID 反查航线名
     */
    public SdkFlightTaskQueryService(DeviceQueryService deviceQueryService,
                                     WaylineQueryService waylineQueryService) {
        this.deviceQueryService = deviceQueryService;
        this.waylineQueryService = waylineQueryService;
    }

    @Override
    public List<TaskSummary> listTasks(String range) {
        FlightTaskModule ftm = requireFlightTaskModule();
        if (ftm == null) {
            return List.of();
        }

        List<String> sns = getAirportSns();
        if (sns.isEmpty()) {
            LOG.info("无机场设备，跳过任务查询");
            return List.of();
        }

        int days = toDays(range);
        LocalDate cutoff = LocalDate.now().minusDays(days);

        List<TaskDetail> allTasks = fetchAllTasks(ftm, sns);
        Map<String, Double> taskDistances = Map.of();
        List<String> successIds = allTasks.stream()
                .filter(d -> "SUCCESS".equals(STATUS_MAP.getOrDefault(d.getStatus(), "UNKNOWN")))
                .map(TaskDetail::getUuid)
                .filter(uuid -> uuid != null && !uuid.isBlank())
                .toList();
        if (!successIds.isEmpty()) {
            taskDistances = fetchTaskDistances(ftm, successIds);
        }

        List<TaskSummary> result = new ArrayList<>();
        for (TaskDetail detail : allTasks) {
            long epochSeconds = DateTimeUtils.toEpochSeconds(detail.getCreatedAt());
            if (epochSeconds == 0) {
                continue;
            }
            LocalDate taskDate = Instant.ofEpochSecond(epochSeconds)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (taskDate.isBefore(cutoff)) {
                continue;
            }
            result.add(new TaskSummary(
                    detail.getUuid(),
                    STATUS_MAP.getOrDefault(detail.getStatus(), "UNKNOWN"),
                    detail.getTaskType(),
                    taskDate.toString(),
                    detail.getWaylineDuration(),
                    taskDistances.getOrDefault(detail.getUuid(), 0.0)
            ));
        }

        LOG.info("SDK 任务查询完成：range={}, total={}", range, result.size());
        return result;
    }

    /**
     * 获取 FlightTaskModule；SDK 未初始化时返回 null。
     *
     * <p>package-private 便于单元测试覆写注入 mock。
     *
     * @return FlightTaskModule 实例，不可用时返回 null
     */
    FlightTaskModule requireFlightTaskModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            LOG.warn("Workspace 不可用，SDK 未初始化");
            return null;
        }
        FlightTaskModule ftm = ws.getFlightTaskModule();
        if (ftm == null) {
            LOG.warn("FlightTaskModule 不可用");
        }
        return ftm;
    }

    /**
     * 拉取全部任务：对每种 task_type 分别翻页查询后按 uuid 去重，再按创建时间倒序排列。
     *
     * <p>单一类型查询失败不影响其余类型，避免个别不被平台接受的枚举值导致整个列表为空。
     *
     * @param ftm FlightTaskModule 实例
     * @param sns 机场 SN 列表
     * @return 去重排序后的任务列表
     */
    private List<TaskDetail> fetchAllTasks(FlightTaskModule ftm, List<String> sns) {
        Map<String, TaskDetail> merged = new LinkedHashMap<>();
        for (int taskType : QUERY_TASK_TYPES) {
            try {
                collectPagesOfType(ftm, sns, taskType, merged);
            } catch (SdkBridge.SdkQueryException e) {
                LOG.warn("按 taskType={} 查询任务失败，跳过该类型：{}", taskType, e.getMessage());
            }
        }

        List<TaskDetail> tasks = new ArrayList<>(merged.values());
        tasks.sort(Comparator.comparingLong(
                (TaskDetail d) -> DateTimeUtils.toEpochSeconds(d.getCreatedAt())).reversed());
        return tasks;
    }

    /**
     * 翻页拉取指定 task_type 的全部任务，按 uuid 去重后写入 merged。
     *
     * <p>翻页上限为 {@value #MAX_PAGES} 页（即 {@value #PAGE_SIZE} × {@value #MAX_PAGES} 条）：
     * 触顶时记录告警而非静默截断，否则被丢弃的任务在冲突校验中会表现为「不冲突」，
     * 属于会漏判的降级路径，必须可观测。
     */
    private void collectPagesOfType(FlightTaskModule ftm, List<String> sns, int taskType,
                                    Map<String, TaskDetail> merged) {
        int page = 1;
        while (page <= MAX_PAGES) {
            FlightTaskListResult result = fetchPage(ftm, page, PAGE_SIZE, sns, taskType);
            if (result == null || result.getList() == null || result.getList().isEmpty()) {
                return;
            }
            for (TaskDetail detail : result.getList()) {
                if (detail.getUuid() != null) {
                    merged.putIfAbsent(detail.getUuid(), detail);
                }
            }

            int total = result.getPagination() != null ? result.getPagination().getTotal() : 0;
            if (page * PAGE_SIZE >= total) {
                return;
            }
            if (page == MAX_PAGES) {
                LOG.warn("任务列表翻页达到上限，结果被截断：taskType={}, maxPages={}, pageSize={}, "
                                + "total={}，本次拉取不完整，冲突校验结论可能漏判",
                        taskType, MAX_PAGES, PAGE_SIZE, total);
                return;
            }
            page++;
        }
    }

    private FlightTaskListResult fetchPage(FlightTaskModule ftm, int page, int pageSize,
                                              List<String> sns, int taskType) {
        FlightTaskListParams params = new FlightTaskListParams();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setTaskType(taskType);
        if (sns != null && !sns.isEmpty()) {
            params.setSn(sns);
        }

        CompletableFuture<FlightTaskListResult> future = new CompletableFuture<>();
        ftm.getFlightTaskList(params, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getFlightTaskList 失败: " + error.getServerMessage()));
                return;
            }
            future.complete(result);
        });

        return SdkBridge.await(future);
    }

    /**
     * 获取所有机场设备的 SN 列表。
     *
     * <p>不按在线状态过滤：机场离线只代表当前无法派发新任务，
     * 其历史任务与已排期的定时任务仍应出现在列表中。
     *
     * @return 机场 SN 列表，无设备时返回空列表
     */
    private List<String> getAirportSns() {
        CachedSns cached = cachedAirportSns.get();
        if (cached != null && !cached.isExpired()) {
            return cached.sns;
        }

        List<DeviceSnapshot> devices = deviceQueryService.listDevices();
        List<String> sns = devices.stream()
                .filter(d -> isGatewayDevice(d.deviceClass()))
                .map(DeviceSnapshot::sn)
                .toList();
        cachedAirportSns.set(new CachedSns(sns, System.currentTimeMillis()));
        return sns;
    }

    /**
     * 判断设备是否为可派发任务的网关类型（大小写不敏感）。
     */
    private static boolean isGatewayDevice(String deviceClass) {
        return deviceClass != null && GATEWAY_DEVICE_CLASSES.contains(deviceClass.toUpperCase());
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

    /**
     * 查询一批任务的飞行距离（米）。
     *
     * <p>SDK 无按任务列表批量返回距离的接口，需逐任务调 {@code getFlightTaskTrack()}。
     * 用固定大小线程池并发扇出，避免串行时顺序等待造成看板长时间阻塞。
     * 单个任务失败不影响其余任务（各自降级为 0），聚合时 SUCCESS 任务会把这些 0
     * 计入总里程，导致统计偏低；当前策略为「保证响应及时优先、轻微低估可接受」。
     * 轨迹仅 SUCCESS 任务可用，故仅对 SUCCESS 任务查询。缺失/失败记录 debug 日志。
     *
     * @param ftm        FlightTaskModule 实例
     * @param successIds 需查询轨迹的 SUCCESS 任务 UUID 列表
     * @return 按任务 UUID 映射的飞行距离（米）
     */
    private Map<String, Double> fetchTaskDistances(FlightTaskModule ftm, List<String> successIds) {
        if (successIds.isEmpty()) {
            return Map.of();
        }
        Map<String, Double> distances = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = successIds.stream()
                .map(id -> CompletableFuture.runAsync(() ->
                        distances.put(id, fetchTaskDistanceMeters(ftm, id)), TRACK_QUERY_EXECUTOR))
                .toList();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(TRACK_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException e) {
            // join() 不抛 InterruptedException；allOf/join 把超时和子任务异常包装为 CompletionException
            LOG.warn("批量轨迹查询有任务失败/超时（{}），已完成的里程照常计入", e.getCause() != null
                    ? e.getCause().getMessage() : e.getMessage());
        }
        return distances;
    }

    /**
     * 查询单个任务的飞行距离（米）。
     *
     * <p>通过 {@code getFlightTaskTrack()} 获取轨迹中的 {@code flightDistance}。
     * 查询失败或轨迹缺失时返回 0（不影响聚合主流程），记录 debug 日志。
     *
     * @param ftm    FlightTaskModule 实例
     * @param taskId 任务 UUID
     * @return 飞行距离（米），不可用时为 0
     */
    private double fetchTaskDistanceMeters(FlightTaskModule ftm, String taskId) {
        if (taskId == null || taskId.isBlank()) {
            return 0.0;
        }
        GetFlightTaskTrackParams params = new GetFlightTaskTrackParams();
        params.setTaskUuid(taskId);

        CompletableFuture<GetFlightTaskTrackResults> future = new CompletableFuture<>();
        ftm.getFlightTaskTrack(params, (error, results) -> {
            if (error != null && !error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getFlightTaskTrack 失败: " + error.getServerMessage()));
                return;
            }
            if (results == null) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getFlightTaskTrack 返回空结果"));
                return;
            }
            future.complete(results);
        });

        try {
            GetFlightTaskTrackResults results = SdkBridge.await(future);
            if (results == null || results.getTrack() == null) {
                return 0.0;
            }
            double distance = results.getTrack().getFlightDistance();
            return distance > 0 ? distance : 0.0;
        } catch (SdkBridge.SdkQueryException e) {
            LOG.debug("任务 {} 轨迹查询失败，里程降级为 0: {}", taskId, e.getMessage());
            return 0.0;
        } catch (RuntimeException e) {
            LOG.warn("任务 {} 轨迹查询异常，里程降级为 0: {}", taskId, e.getMessage());
            return 0.0;
        }
    }

    @Override
    public FlightTaskPage listTasksPaged(String status, int page, int pageSize) {
        FlightTaskModule ftm = requireFlightTaskModule();
        if (ftm == null) {
            return new FlightTaskPage(List.of(), 0, false);
        }

        List<String> sns = getAirportSns();
        if (sns.isEmpty()) {
            LOG.info("无机场设备，返回空任务列表");
            return new FlightTaskPage(List.of(), 0, false);
        }

        List<TaskDetail> allTasks = fetchAllTasks(ftm, sns);
        Map<String, Double> taskDistances = fetchTaskDistancesForPageable(ftm, allTasks);
        List<FlightTaskDto> matching = allTasks.stream()
                .map(detail -> toDto(detail, taskDistances.getOrDefault(detail.getUuid(), 0.0)))
                .filter(dto -> status == null || dto.status().equals(status))
                .toList();

        return paginate(matching, page, pageSize);
    }

    @Override
    public boolean existsTaskWithName(String name, String excludeTaskUuid) {
        if (name == null || name.isBlank()) {
            return false;
        }

        FlightTaskModule ftm = requireFlightTaskModule();
        if (ftm == null) {
            return false;
        }

        List<String> sns = getAirportSns();
        if (sns.isEmpty()) {
            // 无机场设备即无任务，此时返回值语义为「不冲突」，不应阻断创建/编辑
            LOG.info("无机场设备，任务名称唯一性校验直接放行：name={}", name);
            return false;
        }

        return fetchAllTasks(ftm, sns).stream()
                .anyMatch(detail -> name.equals(detail.getName())
                        && !isSameTask(detail, excludeTaskUuid));
    }

    /**
     * 判断任务是否是指定的待排除任务（编辑场景排除自身）。
     */
    private static boolean isSameTask(TaskDetail detail, String excludeTaskUuid) {
        return excludeTaskUuid != null && excludeTaskUuid.equals(detail.getUuid());
    }

    /**
     * 并发拉取分页列表所需的任务轨迹距离。
     *
     * <p>与 {@link #fetchTaskDistances} 共用并发扇出逻辑，但只针对 SUCCESS 任务且复用
     * 已完成的全量任务列表，避免在 {@code toDto} 中逐任务串行查询轨迹（每任务一次
     * 同步等待，几百个任务会让列表接口长时间阻塞）。
     *
     * @param ftm   FlightTaskModule 实例
     * @param tasks 已完成的全量任务列表（含各种状态）
     * @return 按任务 UUID 映射的飞行距离（米），非 SUCCESS 任务无轨迹不查询、默认为 0
     */
    private Map<String, Double> fetchTaskDistancesForPageable(FlightTaskModule ftm, List<TaskDetail> tasks) {
        List<String> successIds = tasks.stream()
                .filter(d -> "SUCCESS".equals(STATUS_MAP.getOrDefault(d.getStatus(), "UNKNOWN")))
                .map(TaskDetail::getUuid)
                .filter(uuid -> uuid != null && !uuid.isBlank())
                .toList();
        return fetchTaskDistances(ftm, successIds);
    }

    /**
     * 客户端分页。
     *
     * <p>任务需跨 task_type 合并后才完整，SDK 侧分页对合并结果不成立，
     * 且 SDK 也不支持服务端 status 过滤，因此分页统一在客户端完成。
     *
     * @param all      全量匹配结果（已排序）
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @return 分页结果
     */
    private FlightTaskPage paginate(List<FlightTaskDto> all, int page, int pageSize) {
        int totalCount = all.size();
        int startIndex = (page - 1) * pageSize;
        if (startIndex >= totalCount) {
            return new FlightTaskPage(List.of(), totalCount, false);
        }

        int endIndex = Math.min(startIndex + pageSize, totalCount);
        return new FlightTaskPage(
                List.copyOf(all.subList(startIndex, endIndex)), totalCount, endIndex < totalCount);
    }

    /**
     * 将 SDK 任务详情映射为列表 DTO。
     *
     * <p>{@code minBatteryCapacity} 需一并带出：平台会把连续执行任务也记为
     * {@code taskType=1}，前端只有拿到该阈值才能与"立即"任务区分，
     * 判定规则与详情接口的 {@code toDetailDto()} 保持一致。
     *
     * @param detail         SDK 任务详情
     * @param distanceMeters 飞行距离（米）
     * @return 列表 DTO
     */
    private FlightTaskDto toDto(TaskDetail detail, double distanceMeters) {
        String statusStr = STATUS_MAP.getOrDefault(detail.getStatus(), "UNKNOWN");
        long createdAtEpoch = DateTimeUtils.toEpochSeconds(detail.getCreatedAt());
        return new FlightTaskDto(
                detail.getUuid(),
                detail.getName(),
                detail.getSn(),
                statusStr,
                detail.getTaskType(),
                detail.getWaylineUuid(),
                createdAtEpoch,
                distanceMeters,
                detail.getMinBatteryCapacity(),
                detail.isBreakPointResume()
        );
    }

    /**
     * 查询单个任务详情（编辑预填用）。
     *
     * <p>详情查询与列表查询不同：返回空列表语义无法表达「任务不存在」，
     * 因此 module 不可用或任务缺失时直接抛 {@link SdkBridge.SdkQueryException}。
     *
     * @param taskUuid 任务 UUID
     * @return 任务详情 DTO
     */
    @Override
    public FlightTaskDetailDto getTaskDetail(String taskUuid) {
        FlightTaskModule ftm = requireFlightTaskModule();
        if (ftm == null) {
            throw new SdkBridge.SdkQueryException("FlightTaskModule 不可用，SDK 未初始化");
        }

        CompletableFuture<TaskDetail> future = new CompletableFuture<>();
        ftm.getFlightTaskDetail(taskUuid, (error, detail) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "查询任务详情失败: " + error.getServerMessage()));
                return;
            }
            future.complete(detail);
        });

        TaskDetail detail = SdkBridge.await(future);
        if (detail == null) {
            throw new SdkBridge.SdkQueryException("查询任务详情失败: 服务端返回空详情");
        }
        return toDetailDto(detail);
    }

    /**
     * 查询指定设备上全部任务的时间窗（时间冲突校验用）。
     *
     * <p>不复用 {@link #listTasksPaged}：分页接口只需当前页数据，而冲突校验必须覆盖该设备的
     * 完整时间窗集合，跨 task_type 合并后一次性返回。module 不可用时返回空列表，
     * 交由调用方按「无冲突」处理（拦截能力依赖 SDK 可用，不应因查询失败而阻断任务创建）。
     *
     * @param sn 机场 SN
     * @return 该设备的任务时间窗列表，无数据时返回空列表
     */
    @Override
    public List<FlightTaskWindow> listTaskWindows(String sn) {
        if (sn == null || sn.isBlank()) {
            return List.of();
        }
        return listTaskWindows(List.of(sn));
    }

    /**
     * 批量查询多个设备上全部任务的时间窗。
     *
     * <p>与单设备版本共用一次 {@link #fetchAllTasks}：SDK 的 {@code FlightTaskListParams.setSn}
     * 本身接受 SN 列表，多设备合并查询不会放大 SDK 调用次数，反而把「列表页逐台设备各拉一次全量」
     * 收敛为一次。返回结果跨设备混合，调用方需按 {@code sn} 分组后再做冲突判定。
     *
     * @param sns 机场 SN 集合
     * @return 全部设备的任务时间窗列表，无数据时返回空列表
     */
    @Override
    public List<FlightTaskWindow> listTaskWindows(Collection<String> sns) {
        List<String> validSns = filterValidSns(sns);
        if (validSns.isEmpty()) {
            return List.of();
        }
        FlightTaskModule ftm = requireFlightTaskModule();
        if (ftm == null) {
            LOG.warn("FlightTaskModule 不可用，跳过任务时间窗查询");
            return List.of();
        }

        return fetchAllTasks(ftm, validSns).stream()
                .map(SdkFlightTaskQueryService::toWindow)
                .toList();
    }

    /**
     * 过滤掉空白 SN 并去重，保持传入顺序。
     *
     * @param sns 机场 SN 集合，可为 null
     * @return 去重后的有效 SN 列表
     */
    private static List<String> filterValidSns(Collection<String> sns) {
        if (sns == null || sns.isEmpty()) {
            return List.of();
        }
        return sns.stream()
                .filter(sn -> sn != null && !sn.isBlank())
                .distinct()
                .toList();
    }

    /**
     * 将 SDK 任务详情映射为冲突比对用的时间窗。
     *
     * @param detail SDK 任务详情
     * @return 任务时间窗
     */
    private static FlightTaskWindow toWindow(TaskDetail detail) {
        return new FlightTaskWindow(
                detail.getUuid(),
                detail.getName(),
                detail.getSn(),
                STATUS_MAP.getOrDefault(detail.getStatus(), "UNKNOWN"),
                detail.getTaskType(),
                DateTimeUtils.toEpochSeconds(detail.getBeginAt()),
                DateTimeUtils.toEpochSeconds(detail.getEndAt()),
                detail.getWaylineDuration()
        );
    }

    /**
     * 将 SDK 任务详情映射为详情 DTO。
     *
     * <p>时间字段由 {@link DateTimeUtils#toEpochSeconds(Object)} 统一解析为 Unix 秒。
     * SDK 详情只返回 wayline_uuid 和 sn，不含名称，故航线名与设备名由本地反查补齐
     * （见 {@link #resolveWaylineName(String)} / {@link #resolveDeviceName(String)}）；
     * planType / repeatType / vlmEnabled SDK 详情不返回，DTO 不包含这些字段。
     *
     * @param detail SDK 任务详情
     * @return 详情 DTO
     */
    private FlightTaskDetailDto toDetailDto(TaskDetail detail) {
        String statusStr = STATUS_MAP.getOrDefault(detail.getStatus(), "UNKNOWN");
        return new FlightTaskDetailDto(
                detail.getUuid(),
                detail.getName(),
                detail.getSn(),
                resolveDeviceName(detail.getSn()),
                statusStr,
                detail.getTaskType(),
                detail.getWaylineUuid(),
                resolveWaylineName(detail.getWaylineUuid()),
                DateTimeUtils.toEpochSeconds(detail.getBeginAt()),
                DateTimeUtils.toEpochSeconds(detail.getEndAt()),
                detail.getRthAltitude(),
                detail.getRthMode(),
                detail.getOutOfControlActionInFlight(),
                detail.getWaylinePrecisionType(),
                detail.getMinBatteryCapacity(),
                detail.isBreakPointResume()
        );
    }

    /**
     * 由设备 SN 反查设备名称。
     *
     * <p>回显缺名不应导致整个详情接口失败，故设备列表查询异常时降级为 null，
     * 由前端回退展示 SN。
     *
     * @param sn 机场 SN
     * @return 设备名称；查不到或查询失败时为 null
     */
    private String resolveDeviceName(String sn) {
        if (sn == null || sn.isEmpty()) {
            return null;
        }
        try {
            return deviceQueryService.listDevices().stream()
                    .filter(d -> sn.equals(d.sn()))
                    .map(DeviceSnapshot::deviceName)
                    .filter(name -> name != null && !name.isEmpty())
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOG.warn("设备名反查失败，详情回显将缺失设备名：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 由航线 UUID 反查航线名称。
     *
     * <p>同理，航线名缺失不应导致详情接口失败；SPI 的 default 实现直接返回空 Map，
     * 未实现该方法的 ISV 会走降级路径。
     *
     * @param waylineUuid 航线 UUID
     * @return 航线名称；查不到或查询失败时为 null
     */
    private String resolveWaylineName(String waylineUuid) {
        if (waylineUuid == null || waylineUuid.isEmpty()) {
            return null;
        }
        try {
            return waylineQueryService.getWaylineNames(List.of(waylineUuid)).get(waylineUuid);
        } catch (Exception e) {
            LOG.warn("航线名反查失败，详情回显将缺失航线名：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 带时间戳的在线机场 SN 缓存条目。
     */
    private record CachedSns(List<String> sns, long timestampMs) {

        boolean isExpired() {
            return System.currentTimeMillis() - timestampMs > AIRPORT_SN_CACHE_TTL_MS;
        }
    }
}
