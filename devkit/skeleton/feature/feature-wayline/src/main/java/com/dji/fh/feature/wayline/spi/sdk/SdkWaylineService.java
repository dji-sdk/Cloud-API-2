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

package com.dji.fh.feature.wayline.spi.sdk;

import com.dji.fh.feature.wayline.BatchDownloadWaylinesResponse;
import com.dji.fh.kernel.spi.util.DateTimeUtils;
import com.dji.fh.feature.wayline.ImportWaylineResponse;
import com.dji.fh.feature.wayline.WaylineExecutionInfo;
import com.dji.fh.feature.wayline.WaylineExecutionsResponse;
import com.dji.fh.feature.wayline.WaylineInfo;
import com.dji.fh.feature.wayline.WaylineListResponse;
import com.dji.fh.feature.wayline.WaylineProperties;
import com.dji.fh.feature.wayline.WaylineService;
import com.dji.fh.feature.wayline.WaylineTypeMapping;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.BatchDeleteWaylinesResult;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.ImportKmzItem;
import com.dji.flighthubsdk.bean.ImportKmzResult;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.bean.PaginationInfo;
import com.dji.flighthubsdk.bean.SubmitWaylineInfoParam;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.bean.WaylineListPaginationResult;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.core.WaylineManageModule;
import com.dji.flighthubsdk.core.Workspace;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * 航线库管理真实 SDK 实现。
 *
 * <p>通过 {@link WaylineManageModule#getWaylineList} 调用 FlightHub 平台 API，
 * 替代 {@link WaylineService} 中的默认空实现。
 * 仅在 {@code fh.skeleton.sdk.mode=sdk} 且 SDK JAR 存在时激活。
 *
 * <p>SDK 列表接口只返回航线的基础属性，航点数与距离需逐条补拉详情，
 * 见 {@link #enrichKmzStats}。
 * keyword 关键字过滤 SDK 列表接口不支持，仅记录日志后忽略。
 *
 * @author DJI DevKit Team
 */
public class SdkWaylineService extends WaylineService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SdkWaylineService.class);

    /** importKmz 上传阶段超时（秒）。 */
    private static final long IMPORT_UPLOAD_TIMEOUT_SECONDS = 30;

    /** 等待平台推送导入完成事件的超时（秒）。 */
    private static final long IMPORT_EVENT_TIMEOUT_SECONDS = 30;

    /** 导入完成事件的成功码。 */
    private static final int IMPORT_EVENT_CODE_SUCCESS = 0;

    /** 未收到导入事件时，按名称反查 UUID 的列表页大小。 */
    private static final int UUID_LOOKUP_PAGE_SIZE = 20;

    /** 按名称反查 UUID 的重试次数（平台落库可能存在短暂延迟）。 */
    private static final int UUID_LOOKUP_MAX_ATTEMPTS = 3;

    /** 按名称反查 UUID 的重试间隔（毫秒），与前端 verifyWaylinePersisted 策略对齐。 */
    private static final long UUID_LOOKUP_INTERVAL_MS = 800L;

    /** 未注册监听时的 listenId 哨兵值（SDK 约定负数为无效）。 */
    private static final int INVALID_LISTEN_ID = -1;

    /** 毫秒转秒的除数（平台 timestamp 字段为秒级）。 */
    private static final long MILLIS_PER_SECOND = 1000L;

    /** 飞行任务列表拉取每页大小。 */
    private static final int TASK_PAGE_SIZE = 100;

    /** 飞行任务列表最大拉取页数（防止无限循环）。 */
    private static final int MAX_TASK_PAGES = 10;

    /** 飞行任务状态：成功完成（SDK 枚举 5=Success，参照 Go SDK POITaskStatus）。 */
    private static final int TASK_STATUS_SUCCESS = 5;

    /** 媒体文件类型：照片。 */
    private static final String MEDIA_FILE_TYPE_PHOTO = "2";

    /** 设备照片聚合时每页媒体数量. */
    private static final int MEDIA_QUERY_PAGE_SIZE = 100;

    /** 设备照片聚合的最大扫描条数，避免设备照片过多时阻塞. */
    private static final int MAX_MEDIA_SCAN = 2000;

    /** 并发调用 SDK 阻塞接口的最大线程数。 */
    private static final int SDK_QUERY_MAX_THREADS = 8;

    /** SDK 查询线程池空闲线程存活时间（秒）。 */
    private static final long SDK_QUERY_KEEPALIVE_SECONDS = 60;

    /** 航点数 / 距离缓存的最大条目数，超出后整体清空，避免长期驻留。 */
    private static final int KMZ_STATS_CACHE_MAX_SIZE = 500;

    /** SDK 查询并发线程池（阻塞 I/O 隔离，避免污染 ForkJoinPool.commonPool）。 */
    private final ExecutorService sdkQueryExecutor;

    /**
     * 航线详情中的航点数 / 距离缓存，key 为 {@code uuid@updatedAt}。
     *
     * <p>航线被编辑后 updatedAt 变化，key 自然失效，无需显式过期。
     */
    private final Map<String, KmzStats> kmzStatsCache = new ConcurrentHashMap<>();

    /** 设备查询服务（用于获取机场 SN 列表，flight-tasks 接口必须传入 SN 才能返回数据）。 */
    private final DeviceQueryService deviceQueryService;

    /**
     * 构造函数。
     *
     * @param props              航线配置属性
     * @param deviceQueryService 设备查询服务
     */
    public SdkWaylineService(WaylineProperties props, DeviceQueryService deviceQueryService) {
        super(props);
        this.deviceQueryService = deviceQueryService;
        this.sdkQueryExecutor = createSdkQueryExecutor();
    }

    @Override
    public void close() {
        sdkQueryExecutor.shutdown();
        try {
            if (!sdkQueryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                sdkQueryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            sdkQueryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 创建 SDK 阻塞查询专用线程池。
     *
     * <p>使用有界线程池 + CallerRunsPolicy：最多 {@link #SDK_QUERY_MAX_THREADS} 个并发线程，
     * 超出时由调用线程直接执行（自然背压，不丢任务也不会 RejectedExecutionException）。
     *
     * @return SDK 查询线程池
     */
    private static ExecutorService createSdkQueryExecutor() {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "sdk-query-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        return new ThreadPoolExecutor(
                SDK_QUERY_MAX_THREADS, SDK_QUERY_MAX_THREADS,
                SDK_QUERY_KEEPALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 查询航线关联的飞行执行列表（SDK 实现）。
     *
     * <p>SDK 无直接按 waylineUuid 查询执行记录的接口，采用组合策略：
     * <ol>
     *   <li>{@link FlightTaskModule#getFlightTaskList} 拉取任务列表</li>
     *   <li>客户端过滤 {@code TaskDetail.waylineUuid} 匹配项（仅取已完成任务）</li>
     *   <li>并发调用 {@link MediaServiceModule#getMediaFilesList} 获取每次飞行照片总数
     *       （通过 deviceSn + 飞行时间范围关联）</li>
     * </ol>
     *
     * @param waylineUuid 航线 UUID
     * @return 该航线关联的飞行执行列表（含照片数）
     */
    @Override
    public WaylineExecutionsResponse listExecutions(String waylineUuid) {
        LOG.info("SDK 查询航线执行列表：waylineUuid={}", waylineUuid);

        FlightTaskModule ftm = requireFlightTaskModule();
        MediaServiceModule msm = requireMediaServiceModule();

        List<TaskDetail> matchedTasks = fetchTasksByWaylineUuid(ftm, waylineUuid);
        LOG.info("航线关联任务过滤完成：waylineUuid={}, matched={}", waylineUuid, matchedTasks.size());

        List<WaylineExecutionInfo> executions = queryExecutionsWithPhotoCount(msm, matchedTasks);

        LOG.info("SDK 航线执行列表查询完成：waylineUuid={}, executions={}",
                waylineUuid, executions.size());
        return new WaylineExecutionsResponse(waylineUuid, "", executions);
    }

    /**
     * 并发查询每个飞行任务的照片数，组装为 {@link WaylineExecutionInfo} 列表。
     *
     * <p>使用独立线程池 {@link #sdkQueryExecutor} 并发执行，
     * 避免阻塞 I/O 占用 {@code ForkJoinPool.commonPool()}。
     *
     * @param msm          MediaServiceModule 实例
     * @param matchedTasks 匹配的飞行任务列表
     * @return 执行信息列表（含照片数）
     */
    private List<WaylineExecutionInfo> queryExecutionsWithPhotoCount(
            MediaServiceModule msm, List<TaskDetail> matchedTasks) {
        if (matchedTasks.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<WaylineExecutionInfo>> futures = new ArrayList<>(matchedTasks.size());
        for (TaskDetail task : matchedTasks) {
            CompletableFuture<WaylineExecutionInfo> f = CompletableFuture.supplyAsync(() -> {
                MediaFolderRef ref = queryMediaFolder(msm, task);
                // id 保持任务 UUID（唯一，供前端 :key）；folderId 为媒体文件夹 id（数字，重建提交用）
                // 无媒体文件夹时 folderId 置 null，前端据此禁用不可重建的执行
                Integer folderId = ref != null ? ref.folderId() : null;
                int photoCount = ref != null ? ref.photoCount() : 0;
                return new WaylineExecutionInfo(
                        task.getUuid(),
                        task.getName(),
                        photoCount,
                        DateTimeUtils.toEpochSeconds(task.getCreatedAt()),
                        folderId);
            }, sdkQueryExecutor);
            futures.add(f);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<WaylineExecutionInfo> result = new ArrayList<>(futures.size());
        for (CompletableFuture<WaylineExecutionInfo> f : futures) {
            result.add(f.join());
        }
        return result;
    }

    /**
     * 查询飞行任务关联的媒体文件夹 id 与照片数.
     *
     * <p>媒体文件 {@code filePath} 即其所属文件夹 id。按任务时间范围 + 照片类型
     * 过滤 {@code getMediaFilesList}，取首条文件的 {@code filePath} 作为文件夹 id，
     * {@code total} 作为照片数。folderUuid 无法从媒体层获取，故重建改用 folder id。
     *
     * @param msm  MediaServiceModule 实例
     * @param task 飞行任务
     * @return 文件夹引用（id + 照片数）；无媒体数据时返回 null
     */
    private MediaFolderRef queryMediaFolder(MediaServiceModule msm, TaskDetail task) {
        try {
            long startTime = parseEpochSeconds(task.getRunAt());
            long endTime = parseEpochSeconds(task.getCompletedAt());
            // 时间范围不完整时直接返回 null（不可重建），避免退化为无界设备全量查询
            if (startTime <= 0 || endTime <= 0) {
                LOG.debug("飞行任务时间范围不完整，跳过媒体查询：taskUuid={}, runAt={}, completedAt={}",
                        task.getUuid(), task.getRunAt(), task.getCompletedAt());
                return null;
            }
            // 先按时间范围（runAt ~ completedAt）精确过滤
            MediaFolderRef strict = queryMediaFolderByParams(msm, task,
                    task.getSn(), startTime, endTime);
            if (strict != null) {
                return strict;
            }
            // 时间范围查不到则回退（启发式）：扫描该设备全部照片，取照片最集中的文件夹 id。
            // 这是一个明确的启发性归因——相机延迟上传/时钟偏移时，任务执行与照片拍摄时间可能不一致，
            // 可能把其他航次的照片归到本任务下。仅当精确查询确定无数据时回退，且结果仅供前端选择参考。
            LOG.debug("时间范围媒体查询为空，回退为设备全量照片统计（启发式归因）：taskUuid={}, sn={}",
                    task.getUuid(), task.getSn());
            return queryDevicePhotosAggregated(msm, task);
        } catch (Exception e) {
            LOG.warn("查询飞行任务媒体文件夹失败（非致命，返回 null）：taskUuid={}, deviceSn={}, error={}",
                    task.getUuid(), task.getSn(), e.getMessage());
            return null;
        }
    }

    /**
     * 扫描设备全部照片，按 file_path（文件夹 id）统计出现次数，返回照片最多的文件夹.
     *
     * @param msm  MediaServiceModule 实例
     * @param task 飞行任务（提供 deviceSn，仅用于日志）
     * @return 照片最多的文件夹引用；无有效文件夹时返回 null
     */
    private MediaFolderRef queryDevicePhotosAggregated(MediaServiceModule msm, TaskDetail task) {
        Map<String, Integer> counts = new HashMap<>();
        int startIndex = 0;
        int scanned = 0;
        while (scanned < MAX_MEDIA_SCAN) {
            GetMediaFilesListParams params = new GetMediaFilesListParams();
            params.setDeviceSn(task.getSn());
            params.setStartIndex(startIndex);
            params.setPageSize(MEDIA_QUERY_PAGE_SIZE);
            params.setFileTypes(List.of(MEDIA_FILE_TYPE_PHOTO));

            CompletableFuture<GetMediaFilesListResults> future = new CompletableFuture<>();
            msm.getMediaFilesList(params, (error, results) -> {
                if (error == null || !error.isSuccess()) {
                    String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "getMediaFilesList 失败: " + code));
                    return;
                }
                future.complete(results);
            });

            GetMediaFilesListResults results = SdkBridge.await(future);
            if (results == null) {
                break;
            }
            List<MediaFilesListItemInfoData> list = results.getList();
            if (list == null || list.isEmpty()) {
                break;
            }
            for (MediaFilesListItemInfoData item : list) {
                String filePath = item.getFilePath();
                if (filePath != null && filePath.matches(FOLDER_ID_PATTERN)) {
                    counts.merge(filePath, 1, Integer::sum);
                }
            }
            scanned += list.size();
            startIndex += list.size();
            if (list.size() < MEDIA_QUERY_PAGE_SIZE
                    || results.getTotal() <= 0
                    || scanned >= results.getTotal()) {
                break;
            }
        }

        if (counts.isEmpty()) {
            return null;
        }
        // 取照片最多的文件夹 id
        Map.Entry<String, Integer> best = null;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (best == null || e.getValue() > best.getValue()) {
                best = e;
            }
        }
        LOG.debug("设备照片文件夹统计完成：sn={}, folders={}, best={}({})",
                task.getSn(), counts, best.getKey(), best.getValue());
        return new MediaFolderRef(Integer.parseInt(best.getKey()), best.getValue());
    }

    /**
     * 按指定过滤条件查询媒体文件夹 id 与照片数.
     *
     * <p>媒体文件 {@code filePath} 即其所属文件夹 id。按 deviceSn + 照片类型
     * 过滤 {@code getMediaFilesList}，取首条文件的 {@code filePath} 作为文件夹 id，
     * {@code total} 作为照片数。startTime/endTime 为 0 时不限时间。
     *
     * @param msm       MediaServiceModule 实例
     * @param task      飞行任务（仅用于日志）
     * @param deviceSn  设备 SN
     * @param startTime 起始时间（epoch 秒，0 表示不限）
     * @param endTime   结束时间（epoch 秒，0 表示不限）
     * @return 文件夹引用（id + 照片数）；无媒体数据时返回 null
     */
    private MediaFolderRef queryMediaFolderByParams(MediaServiceModule msm, TaskDetail task,
                                                    String deviceSn, long startTime, long endTime) {
        GetMediaFilesListParams params = new GetMediaFilesListParams();
        params.setDeviceSn(deviceSn);
        params.setStartIndex(0);
        params.setPageSize(1);
        params.setFileTypes(List.of(MEDIA_FILE_TYPE_PHOTO));
        if (startTime > 0) {
            params.setStartCreateTime(startTime);
        }
        if (endTime > 0) {
            params.setEndCreateTime(endTime);
        }

        CompletableFuture<GetMediaFilesListResults> future = new CompletableFuture<>();
        msm.getMediaFilesList(params, (error, results) -> {
            if (error == null || !error.isSuccess()) {
                String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getMediaFilesList 失败: " + code));
                return;
            }
            future.complete(results);
        });

        GetMediaFilesListResults results = SdkBridge.await(future);
        if (results == null) {
            LOG.warn("getMediaFilesList 成功但返回 null 结果：taskUuid={}, deviceSn={}",
                    task.getUuid(), deviceSn);
            return null;
        }

        List<MediaFilesListItemInfoData> list = results.getList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        String filePath = list.get(0).getFilePath();
        if (filePath == null || !filePath.matches(FOLDER_ID_PATTERN)) {
            LOG.warn("媒体文件 filePath 非纯数字，跳过：taskUuid={}, filePath={}",
                    task.getUuid(), filePath);
            return null;
        }
        return new MediaFolderRef(Integer.parseInt(filePath), results.getTotal());
    }

    /** 媒体文件路径正则：纯数字才是文件夹 id（跳过 wayline-* / 多层路径 / 特殊值）. */
    private static final String FOLDER_ID_PATTERN = "\\d+";

    /** 媒体文件夹引用（id + 照片数）. */
    private record MediaFolderRef(int folderId, int photoCount) { }

    /**
     * 遍历飞行任务列表，过滤出匹配指定航线且已完成的任务。
     *
     * <p>SDK 不支持 waylineUuid 服务端过滤，客户端逐页拉取并筛选，
     * 最多拉取 {@link #MAX_TASK_PAGES} 页避免无限循环。
     *
     * @param ftm         FlightTaskModule 实例
     * @param waylineUuid 目标航线 UUID
     * @return 匹配且已完成的任务列表
     */
    private List<TaskDetail> fetchTasksByWaylineUuid(FlightTaskModule ftm, String waylineUuid) {
        List<TaskDetail> matched = new ArrayList<>();
        int page = 1;
        boolean truncated = false;

        List<String> deviceSns = getAllDeviceSns();
        if (deviceSns.isEmpty()) {
            LOG.warn("无可用设备 SN，flight-tasks 接口无法返回数据");
            return matched;
        }

        while (page <= MAX_TASK_PAGES) {
            FlightTaskListParams params = new FlightTaskListParams();
            params.setPage(page);
            params.setPageSize(TASK_PAGE_SIZE);
            params.setSn(deviceSns);

            CompletableFuture<FlightTaskListResult> future = new CompletableFuture<>();
            ftm.getFlightTaskList(params, (error, result) -> {
                if (error == null || !error.isSuccess()) {
                    String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "getFlightTaskList 失败: " + code));
                    return;
                }
                future.complete(result);
            });

            FlightTaskListResult result = SdkBridge.await(future);
            List<TaskDetail> tasks = result.getList();
            if (tasks == null || tasks.isEmpty()) {
                break;
            }

            for (TaskDetail task : tasks) {
                if (waylineUuid.equals(task.getWaylineUuid()) && task.getStatus() == TASK_STATUS_SUCCESS) {
                    matched.add(task);
                }
            }

            PaginationInfo pagination = result.getPagination();
            int total = pagination != null ? pagination.getTotal() : 0;

            if (total > 0) {
                if (page * TASK_PAGE_SIZE >= total) {
                    break;
                }
            } else {
                // SDK PaginationInfo.getTotal() 可能返回 0（反序列化 bug），
                // 此时按本页是否满页判断是否继续
                if (tasks.size() < TASK_PAGE_SIZE) {
                    break;
                }
            }
            if (page == MAX_TASK_PAGES) {
                truncated = true;
            }
            page++;
        }

        if (truncated) {
            LOG.warn("飞行任务列表已达扫描上限（{}页 × {}条），可能存在更早的执行记录未被包含",
                    MAX_TASK_PAGES, TASK_PAGE_SIZE);
        }

        return matched;
    }

    /** flight-tasks 接口绑定任务的网关设备类型（与 SdkFlightTaskQueryService 保持一致）。 */
    private static final Set<String> GATEWAY_DEVICE_CLASSES = Set.of("DOCK", "AIRPORT", "BASE_STATION");

    /**
     * 获取所有网关类设备（机场/基站）的 SN 列表。
     *
     * <p>FlightHub flight-tasks 接口要求传入 sn 参数才能返回数据，
     * 不传则返回空列表。仅传入网关设备 SN（不含无人机），因为：
     * <ul>
     *   <li>flight-tasks 按机场 SN 归属任务</li>
     *   <li>传入过多 SN 会导致 URL 超长，SDK 静默丢弃参数</li>
     * </ul>
     * 包含离线设备，确保能查到历史任务。
     *
     * @return 网关设备 SN 列表
     */
    private List<String> getAllDeviceSns() {
        List<DeviceSnapshot> devices = deviceQueryService.listDevices();
        List<String> sns = new ArrayList<>();
        for (DeviceSnapshot device : devices) {
            if (isGatewayDevice(device.deviceClass())) {
                sns.add(device.sn());
            }
        }
        return sns;
    }

    /**
     * 判断设备是否为网关类型（大小写不敏感）。
     */
    private static boolean isGatewayDevice(String deviceClass) {
        return deviceClass != null && GATEWAY_DEVICE_CLASSES.contains(deviceClass.toUpperCase());
    }

    /**
     * 将 ISO 8601 时间字符串解析为 epoch 秒数。
     *
     * @param isoTime ISO 时间字符串（如 "2026-04-01T10:00:00Z"），可能为 null
     * @return epoch 秒数，解析失败返回 0
     */
    private long parseEpochSeconds(String isoTime) {
        if (isoTime == null || isoTime.isBlank()) {
            return 0;
        }
        try {
            return Instant.parse(isoTime).getEpochSecond();
        } catch (DateTimeParseException e) {
            LOG.warn("时间格式解析失败：input={}", isoTime);
            return 0;
        }
    }

    /**
     * 批量删除航线（SDK 实现）。
     *
     * <p>调用 {@link WaylineManageModule#batchDeleteWaylines}，
     * 同时检查 callback error 和 {@link BatchDeleteWaylinesResult#getServerCode()}。
     *
     * @param uuids 待删除的航线 UUID 列表
     * @throws SdkBridge.SdkQueryException SDK 调用失败或服务端拒绝删除
     */
    @Override
    public void batchDeleteWaylines(List<String> uuids) {
        WaylineManageModule module = requireWaylineManageModule();
        LOG.info("SDK 批量删除航线：count={}, uuids={}", uuids.size(), uuids);

        CompletableFuture<BatchDeleteWaylinesResult> future = new CompletableFuture<>();
        module.batchDeleteWaylines(uuids, (error, result) -> {
            if (error == null || !error.isSuccess()) {
                String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "batchDeleteWaylines 失败: " + code));
                return;
            }
            if (result != null && result.getServerCode() != 0) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "服务端拒绝删除: serverCode=" + result.getServerCode()
                                + ", message=" + result.getServerMessage()));
                return;
            }
            future.complete(result);
        });

        try {
            SdkBridge.await(future);
            LOG.info("SDK 批量删除航线完成：count={}", uuids.size());
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 批量删除航线失败：count={}", uuids.size(), e);
            throw e;
        }
    }

    /**
     * 批量下载航线（SDK 实现）。
     *
     * <p>调用 {@link WaylineManageModule#batchDownloadWaylines}，
     * 将 SDK 返回的 {@link BatchDownloadWaylinesItemResult} 列表映射为前端契约 DTO。
     *
     * @param uuids 待下载的航线 UUID 列表
     * @return 包含下载 URL 的响应
     * @throws SdkBridge.SdkQueryException SDK 调用失败
     */
    @Override
    public BatchDownloadWaylinesResponse batchDownloadWaylines(List<String> uuids) {
        WaylineManageModule module = requireWaylineManageModule();
        LOG.info("SDK 批量下载航线：count={}, uuids={}", uuids.size(), uuids);

        BatchDownloadWaylinesParam param = new BatchDownloadWaylinesParam();
        param.setWaylineUuids(uuids);

        CompletableFuture<List<BatchDownloadWaylinesItemResult>> future = new CompletableFuture<>();
        module.batchDownloadWaylines(param, (error, result) -> {
            if (error == null || !error.isSuccess()) {
                String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "batchDownloadWaylines 失败: " + code));
                return;
            }
            future.complete(result != null ? result : List.of());
        });

        try {
            List<BatchDownloadWaylinesItemResult> items = SdkBridge.await(future);
            List<BatchDownloadWaylinesResponse.DownloadItem> downloads = mapToDownloadItems(items);
            LOG.info("SDK 批量下载航线完成：count={}, returned={}", uuids.size(), downloads.size());
            return new BatchDownloadWaylinesResponse(downloads);
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 批量下载航线失败：count={}", uuids.size(), e);
            throw e;
        }
    }

    /**
     * 导入航线（SDK 实现）。
     *
     * <p>平台有同步、异步两种导入模式，以 {@link WaylineManageModule#importKmz} 返回的
     * asyncRequestId 是否为空区分：
     * <ul>
     *   <li><b>同步模式</b>（asyncRequestId 为空）：importKmz 一步完成入库，
     *       直接按名称反查 UUID。此时平台的 import-callback 接口强制校验
     *       async_request_id 非空，再调 {@code submitWaylineInfo} 必然被拒。</li>
     *   <li><b>异步模式</b>（asyncRequestId 非空）：需用 kmzObjectKey 调
     *       {@link WaylineManageModule#submitWaylineInfo} 登记，结果由平台通过
     *       {@code listenImportWaylineEvent} 异步推送；必须先注册监听再登记，
     *       否则可能漏掉事件。</li>
     * </ul>
     *
     * @param file        KMZ 文件
     * @param waylineName 航线名称
     * @return 包含航线 UUID 的导入响应
     * @throws SdkBridge.SdkQueryException SDK 调用失败，或导入后未能确认航线 UUID
     */
    @Override
    public ImportWaylineResponse importWayline(MultipartFile file, String waylineName) {
        WaylineManageModule module = requireWaylineManageModule();
        LOG.info("SDK 导入航线：fileName={}, waylineName={}, size={}",
                file.getOriginalFilename(), waylineName, file.getSize());

        Path tempFile = null;
        int listenId = INVALID_LISTEN_ID;
        try {
            // 平台以文件名（不含扩展名）作为航线名称，因此用用户指定的 waylineName 命名临时文件
            String safeFileName = waylineName.replaceAll("[^\\w\\u4e00-\\u9fa5\\-_()（）]", "_");
            tempFile = Files.createTempFile(safeFileName + "-", ".kmz");
            file.transferTo(tempFile);

            ImportKmzItem uploadResult = uploadKmz(module, tempFile);

            String asyncRequestId = uploadResult.getAsyncRequestId();
            String uuid;
            if (asyncRequestId == null || asyncRequestId.isBlank()) {
                LOG.info("平台以同步模式完成导入（asyncRequestId 为空），跳过 submitWaylineInfo 登记");
                uuid = lookupUuidByNameWithRetry(module, uploadResult.getWaylineName());
            } else {
                // 必须先注册监听再登记，否则可能漏掉平台推送的导入完成事件
                CompletableFuture<ImportWaylineEventResult> eventFuture = new CompletableFuture<>();
                listenId = listenImportEvent(module, uploadResult.getKmzObjectKey(), eventFuture);

                submitWaylineInfo(module, uploadResult);

                uuid = awaitImportedUuid(module, eventFuture, uploadResult.getWaylineName());
            }
            if (uuid == null || uuid.isBlank()) {
                throw new SdkBridge.SdkQueryException(
                        "导入完成但未能确认航线 UUID：waylineName=" + uploadResult.getWaylineName());
            }
            LOG.info("SDK 导入航线完成：waylineName={}, waylineUuid={}",
                    uploadResult.getWaylineName(), uuid);
            return new ImportWaylineResponse(uuid);
        } catch (IOException e) {
            throw new SdkBridge.SdkQueryException("临时文件写入失败: " + e.getMessage());
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 导入航线失败：waylineName={}", waylineName, e);
            throw e;
        } finally {
            cancelImportEventListener(module, listenId);
            deleteTempFile(tempFile);
        }
    }

    /**
     * 注册导入完成事件监听，仅接收 kmzObjectKey 匹配的事件。
     *
     * @param module         WaylineManageModule 实例
     * @param kmzObjectKey   本次上传返回的对象存储 key
     * @param eventFuture    匹配到事件时完成的 future
     * @return listenId（负数表示注册失败）
     */
    private int listenImportEvent(WaylineManageModule module, String kmzObjectKey,
                                  CompletableFuture<ImportWaylineEventResult> eventFuture) {
        int listenId = module.listenImportWaylineEvent(event -> {
            if (event != null && Objects.equals(kmzObjectKey, event.getKmzObjectKey())) {
                eventFuture.complete(event);
            }
        });
        if (listenId < 0) {
            LOG.warn("导入完成事件监听注册失败：listenId={}, kmzObjectKey={}", listenId, kmzObjectKey);
        }
        return listenId;
    }

    /**
     * 向平台登记航线（导入两步协议的第二步）。
     *
     * @param module       WaylineManageModule 实例
     * @param uploadResult importKmz 上传结果
     * @throws SdkBridge.SdkQueryException 登记失败或超时
     */
    private void submitWaylineInfo(WaylineManageModule module, ImportKmzItem uploadResult) {
        SubmitWaylineInfoParam param = new SubmitWaylineInfoParam();
        param.setKmzObjectKey(uploadResult.getKmzObjectKey());
        param.setWaylineName(uploadResult.getWaylineName());
        param.setAsyncRequestId(uploadResult.getAsyncRequestId());
        param.setTimestamp(System.currentTimeMillis() / MILLIS_PER_SECOND);

        CompletableFuture<Void> future = new CompletableFuture<>();
        module.submitWaylineInfo(param, error -> {
            if (error == null || !error.isSuccess()) {
                String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "submitWaylineInfo 失败: " + code));
                return;
            }
            future.complete(null);
        });

        SdkBridge.await(future);
        LOG.info("SDK submitWaylineInfo 登记成功：waylineName={}, kmzObjectKey={}",
                uploadResult.getWaylineName(), uploadResult.getKmzObjectKey());
    }

    /**
     * 等待导入完成事件并取出航线 UUID，超时则降级按名称反查列表。
     *
     * @param module      WaylineManageModule 实例
     * @param eventFuture 导入完成事件 future
     * @param waylineName 航线名称（降级反查用）
     * @return 航线 UUID
     * @throws SdkBridge.SdkQueryException 平台明确返回导入失败，或降级反查耗尽重试仍未查到
     */
    private String awaitImportedUuid(WaylineManageModule module,
                                     CompletableFuture<ImportWaylineEventResult> eventFuture,
                                     String waylineName) {
        ImportWaylineEventResult event;
        try {
            event = SdkBridge.await(eventFuture, IMPORT_EVENT_TIMEOUT_SECONDS);
        } catch (SdkBridge.SdkQueryException e) {
            LOG.warn("未收到导入完成事件（{}），降级按名称反查 UUID：waylineName={}",
                    e.getMessage(), waylineName);
            return lookupUuidByNameWithRetry(module, waylineName);
        }
        if (event.getCode() != IMPORT_EVENT_CODE_SUCCESS) {
            throw new SdkBridge.SdkQueryException(
                    "平台导入航线失败: code=" + event.getCode() + ", " + event.getMessage());
        }
        return event.getWaylineUuid();
    }

    /**
     * 按航线名称在列表首页反查 UUID（导入事件缺失时的降级路径）。
     *
     * @param module      WaylineManageModule 实例
     * @param waylineName 航线名称
     * @return 匹配到的 UUID，未匹配到返回 null
     */
    private String lookupUuidByName(WaylineManageModule module, String waylineName) {
        try {
            GetWaylineListParam param = new GetWaylineListParam();
            param.setPage(1);
            param.setPageSize(UUID_LOOKUP_PAGE_SIZE);
            GetWaylineListResult result = queryWaylineList(module, param);
            if (result == null || result.getItems() == null) {
                return null;
            }
            for (WaylineListItemResult item : result.getItems()) {
                if (waylineName.equals(item.getWaylineName())) {
                    return item.getWaylineUuid();
                }
            }
            LOG.warn("列表首页未找到刚导入的航线：waylineName={}", waylineName);
            return null;
        } catch (SdkBridge.SdkQueryException e) {
            LOG.warn("按名称反查航线 UUID 失败：waylineName={}, error={}",
                    waylineName, e.getMessage());
            return null;
        }
    }

    /**
     * 按名称反查 UUID，未查到时按固定间隔重试。
     *
     * <p>同步导入模式下 importKmz 返回时平台落库可能尚未完成，单次查询会落空；
     * 重试耗尽仍未查到则抛异常，避免把「未确认落库」当作导入成功。
     *
     * @param module      WaylineManageModule 实例
     * @param waylineName 航线名称
     * @return 匹配到的 UUID
     * @throws SdkBridge.SdkQueryException 重试耗尽仍未查到，或等待被中断
     */
    private String lookupUuidByNameWithRetry(WaylineManageModule module, String waylineName) {
        for (int attempt = 1; attempt <= UUID_LOOKUP_MAX_ATTEMPTS; attempt++) {
            String uuid = lookupUuidByName(module, waylineName);
            if (uuid != null) {
                return uuid;
            }
            if (attempt < UUID_LOOKUP_MAX_ATTEMPTS) {
                sleepBeforeRetry(waylineName);
            }
        }
        throw new SdkBridge.SdkQueryException("同步导入后未在航线库查到该航线（已重试 "
                + UUID_LOOKUP_MAX_ATTEMPTS + " 次）：waylineName=" + waylineName);
    }

    /**
     * 反查重试前的等待，被中断时转为查询异常并恢复中断标志。
     *
     * @param waylineName 航线名称（异常信息用）
     * @throws SdkBridge.SdkQueryException 等待被中断
     */
    private void sleepBeforeRetry(String waylineName) {
        try {
            Thread.sleep(UUID_LOOKUP_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SdkBridge.SdkQueryException("反查航线 UUID 被中断：waylineName=" + waylineName);
        }
    }

    /**
     * 取消导入完成事件监听（listenId 无效时静默跳过）。
     *
     * @param module   WaylineManageModule 实例
     * @param listenId 监听 ID
     */
    private void cancelImportEventListener(WaylineManageModule module, int listenId) {
        if (listenId < 0) {
            return;
        }
        try {
            module.cancelListenImportWaylineEvent(listenId);
        } catch (RuntimeException e) {
            LOG.warn("取消导入事件监听失败：listenId={}, error={}", listenId, e.getMessage());
        }
    }

    /**
     * 上传 KMZ 文件到 SDK，返回完整的 {@link ImportKmzItem}。
     *
     * <p>包含 asyncRequestId（异步模式）或 waylineName（同步模式下用于反查 UUID）。
     */
    private ImportKmzItem uploadKmz(WaylineManageModule module, Path tempFile) {
        CompletableFuture<ImportKmzResult> future = new CompletableFuture<>();
        module.importKmz(List.of(tempFile.toString()), (error, result) -> {
            if (error == null || !error.isSuccess()) {
                String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "importKmz 失败: " + code));
                return;
            }
            future.complete(result);
        });

        ImportKmzResult result = SdkBridge.await(future, IMPORT_UPLOAD_TIMEOUT_SECONDS);
        if (result == null || result.getList() == null || result.getList().isEmpty()) {
            throw new SdkBridge.SdkQueryException("importKmz 返回结果为空");
        }
        ImportKmzItem first = result.getList().get(0);
        LOG.info("SDK importKmz 上传完成：asyncRequestId={}, waylineName={}",
                first.getAsyncRequestId(), first.getWaylineName());
        return first;
    }

    /**
     * 公共 helper：查询航线列表并等待结果。
     *
     * @param module WaylineManageModule 实例
     * @param param  查询参数
     * @return 查询结果
     * @throws SdkBridge.SdkQueryException SDK 回调失败或超时
     */
    private GetWaylineListResult queryWaylineList(WaylineManageModule module,
                                                  GetWaylineListParam param) {
        CompletableFuture<GetWaylineListResult> future = new CompletableFuture<>();
        module.getWaylineList(param, (error, result) -> {
            if (error == null || !error.isSuccess()) {
                String code = error != null ? String.valueOf(error.getErrorCode()) : "null";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getWaylineList 失败: " + code));
                return;
            }
            future.complete(result);
        });
        return SdkBridge.await(future);
    }

    @Override
    public WaylineListResponse listWaylines(int page, int pageSize,
                                            String droneType, String payloadType,
                                            String keyword) {
        WaylineManageModule module = requireWaylineManageModule();
        int effectivePage = Math.max(page, 1);
        int effectivePageSize = clampPageSize(pageSize);

        if (keyword != null && !keyword.isBlank()) {
            // SDK 列表接口不支持关键字过滤，记录日志后忽略
            LOG.warn("SDK 航线列表接口不支持 keyword 过滤，已忽略：keyword={}", keyword);
        }

        GetWaylineListParam req = new GetWaylineListParam();
        req.setPage(effectivePage);
        req.setPageSize(effectivePageSize);
        if (droneType != null && !droneType.isBlank()) {
            req.setDroneTypes(List.of(droneType));
        }
        if (payloadType != null && !payloadType.isBlank()) {
            req.setPayloadTypes(List.of(payloadType));
        }

        try {
            GetWaylineListResult result = queryWaylineList(module, req);
            List<WaylineInfo> waylines = mapToWaylineInfoList(module, result);
            int total = extractTotal(result);

            LOG.info("SDK 航线列表查询完成：page={}, pageSize={}, total={}, returned={}",
                    effectivePage, effectivePageSize, total, waylines.size());
            return new WaylineListResponse(waylines, total, effectivePage, effectivePageSize);
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 航线列表查询失败", e);
            throw e;
        }
    }

    /**
     * 检查指定 UUID 的航线是否仍然存在（SDK 实现）。
     *
     * <p>通过 {@link WaylineManageModule#getWaylineDetail(String,
     * com.dji.flighthubsdk.FlightHubCallback.GetWaylineDetailCallback)}
     * 单次查询航线详情。SDK 成功返回表示航线存在；返回错误表示不存在。
     *
     * <p>异常情况降级返回 true（不阻断回放加载）。
     *
     * @param uuid 航线 UUID
     * @return 航线存在返回 true，否则 false
     */
    @Override
    public boolean waylineExists(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        try {
            WaylineManageModule module = requireWaylineManageModule();
            CompletableFuture<GetWaylineDetailResult> future = new CompletableFuture<>();

            module.getWaylineDetail(uuid, (error, result) -> {
                if (error != null && error.isSuccess()) {
                    future.complete(result);
                } else {
                    future.complete(null);
                }
            });

            GetWaylineDetailResult result = SdkBridge.await(future);
            boolean exists = result != null
                    && result.getWaylineUuid() != null
                    && !result.getWaylineUuid().isBlank();
            LOG.debug("航线存在性检查完成：uuid={}, exists={}", uuid, exists);
            return exists;
        } catch (Exception e) {
            LOG.warn("航线存在性检查异常（降级返回 true，不阻断回放）：uuid={}, error={}",
                    uuid, e.getMessage());
            return true;
        }
    }

    /**
     * 静默删除临时文件。
     *
     * @param path 临时文件路径（可能为 null）
     */
    private void deleteTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                LOG.warn("临时文件删除失败：{}", path, e);
            }
        }
    }

    /**
     * 获取 {@link WaylineManageModule}，Workspace 或模块不可用时抛出异常。
     *
     * @return WaylineManageModule 实例（非 null）
     * @throws IllegalStateException SDK 未初始化或模块不可用
     */
    WaylineManageModule requireWaylineManageModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 不可用，SDK 未初始化");
        }
        WaylineManageModule module = ws.getWaylineManageModule();
        if (module == null) {
            throw new IllegalStateException("WaylineManageModule 不可用");
        }
        return module;
    }

    /**
     * 获取 {@link FlightTaskModule}，Workspace 或模块不可用时抛出异常。
     *
     * @return FlightTaskModule 实例（非 null）
     * @throws IllegalStateException SDK 未初始化或模块不可用
     */
    FlightTaskModule requireFlightTaskModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 不可用，SDK 未初始化");
        }
        FlightTaskModule module = ws.getFlightTaskModule();
        if (module == null) {
            throw new IllegalStateException("FlightTaskModule 不可用");
        }
        return module;
    }

    /**
     * 获取 {@link MediaServiceModule}，Workspace 或模块不可用时抛出异常。
     *
     * @return MediaServiceModule 实例（非 null）
     * @throws IllegalStateException SDK 未初始化或模块不可用
     */
    MediaServiceModule requireMediaServiceModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 不可用，SDK 未初始化");
        }
        MediaServiceModule module = ws.getMediaServiceModule();
        if (module == null) {
            throw new IllegalStateException("MediaServiceModule 不可用");
        }
        return module;
    }

    /**
     * 将 SDK 返回的航线列表映射为 DTO，并补齐航点数 / 距离。
     *
     * @param module SDK 航线管理模块，用于补拉详情
     * @param result SDK 查询结果
     * @return WaylineInfo 列表
     */
    private List<WaylineInfo> mapToWaylineInfoList(WaylineManageModule module,
                                                   GetWaylineListResult result) {
        if (result == null || result.getItems() == null) {
            return List.of();
        }
        List<WaylineListItemResult> items = result.getItems().stream()
                .filter(Objects::nonNull)
                .toList();
        Map<String, KmzStats> statsByKey = enrichKmzStats(module, items);

        List<WaylineInfo> waylines = new ArrayList<>(items.size());
        for (WaylineListItemResult item : items) {
            KmzStats stats = statsByKey.get(
                    kmzStatsCacheKey(item.getWaylineUuid(), item.getUpdatedAt()));
            waylines.add(new WaylineInfo(
                    item.getWaylineUuid(),
                    item.getWaylineName(),
                    item.getDroneType(),
                    firstOrEmpty(item.getPayloadTypes()),
                    WaylineTypeMapping.labelOf(firstWaylineType(item.getWaylineTypes())),
                    // SDK 返回毫秒时间戳，前端约定秒级
                    item.getUpdatedAt() / MILLIS_PER_SECOND,
                    stats != null ? stats.waypointCount() : null,
                    stats != null ? stats.distanceMeters() : null
            ));
        }
        // 按更新时间倒序排列（最新在前）
        waylines.sort((a, b) -> Long.compare(b.updatedAt() != null ? b.updatedAt() : 0,
                a.updatedAt() != null ? a.updatedAt() : 0));
        return waylines;
    }

    /**
     * 并发补拉当前页每条航线的航点数与距离。
     *
     * <p>SDK 列表接口不返回这两个字段，只能逐条查询详情。命中缓存的条目不再发起请求；
     * 单条失败只影响该行（对应字段降级为 null），不影响整页返回。
     *
     * @param module SDK 航线管理模块
     * @param items  当前页航线列表项
     * @return key 为 {@code uuid@updatedAt} 的统计信息，取不到的航线不会出现在结果中
     */
    private Map<String, KmzStats> enrichKmzStats(WaylineManageModule module,
                                                 List<WaylineListItemResult> items) {
        Map<String, KmzStats> statsByKey = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>(items.size());
        for (WaylineListItemResult item : items) {
            String uuid = item.getWaylineUuid();
            if (uuid == null || uuid.isBlank()) {
                continue;
            }
            String key = kmzStatsCacheKey(uuid, item.getUpdatedAt());
            KmzStats cached = kmzStatsCache.get(key);
            if (cached != null) {
                statsByKey.put(key, cached);
                continue;
            }
            futures.add(CompletableFuture.runAsync(() -> {
                KmzStats fetched = fetchKmzStats(module, uuid);
                if (fetched != null) {
                    statsByKey.put(key, fetched);
                    cacheKmzStats(key, fetched);
                }
            }, sdkQueryExecutor));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return statsByKey;
    }

    /**
     * 查询单条航线详情并提取航点数与距离。
     *
     * @param module SDK 航线管理模块
     * @param uuid   航线 UUID
     * @return 统计信息；查询失败或详情缺少 kmzData 时返回 null
     */
    private KmzStats fetchKmzStats(WaylineManageModule module, String uuid) {
        try {
            CompletableFuture<GetWaylineDetailResult> future = new CompletableFuture<>();
            module.getWaylineDetail(uuid, (error, detail) -> {
                if (error != null && error.isSuccess()) {
                    future.complete(detail);
                } else {
                    future.complete(null);
                }
            });

            GetWaylineDetailResult detail = SdkBridge.await(future);
            WaylineDetailKmzData kmz = detail != null ? detail.getKmzData() : null;
            if (kmz == null) {
                return null;
            }
            return new KmzStats(kmz.getWaylinePointCount(), Math.round(kmz.getDistance()));
        } catch (Exception e) {
            LOG.warn("补拉航线详情失败，航点数/距离降级为空：uuid={}, error={}", uuid, e.getMessage());
            return null;
        }
    }

    /**
     * 写入缓存；条目数超过上限时整体清空，避免长期驻留占用内存。
     */
    private void cacheKmzStats(String key, KmzStats stats) {
        if (kmzStatsCache.size() >= KMZ_STATS_CACHE_MAX_SIZE) {
            kmzStatsCache.clear();
        }
        kmzStatsCache.put(key, stats);
    }

    /**
     * 生成缓存 key：航线被编辑后 updatedAt 变化，旧 key 自然失效。
     */
    private static String kmzStatsCacheKey(String uuid, long updatedAt) {
        return uuid + "@" + updatedAt;
    }

    /**
     * 航线详情中的航点数与飞行距离。
     *
     * @param waypointCount  航点数量
     * @param distanceMeters 预计飞行距离（米，四舍五入到整数）
     */
    private record KmzStats(Integer waypointCount, Long distanceMeters) {}

    /**
     * 从 SDK 结果中提取总数。
     *
     * @param result SDK 查询结果
     * @return 总数，pagination 缺失时返回 0
     */
    private int extractTotal(GetWaylineListResult result) {
        if (result == null) {
            return 0;
        }
        WaylineListPaginationResult pagination = result.getPagination();
        return pagination != null ? pagination.getTotal() : 0;
    }

    /**
     * 取列表首个元素，空列表返回空串。
     *
     * @param list 字符串列表（可能为 null）
     * @return 首元素或空串
     */
    private String firstOrEmpty(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        String first = list.get(0);
        return first != null ? first : "";
    }

    /**
     * 取航线类型编码列表首个元素。
     *
     * @param types 类型编码列表（可能为 null）
     * @return 首个编码，空列表返回 null
     */
    private Integer firstWaylineType(List<Integer> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        return types.get(0);
    }

    /**
     * 将 SDK 批量下载结果映射为前端 DTO 列表。
     *
     * @param items SDK 返回的下载条目（可能为 null）
     * @return DownloadItem 列表
     */
    private List<BatchDownloadWaylinesResponse.DownloadItem> mapToDownloadItems(
            List<BatchDownloadWaylinesItemResult> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<BatchDownloadWaylinesResponse.DownloadItem> downloads = new ArrayList<>(items.size());
        for (BatchDownloadWaylinesItemResult item : items) {
            if (item == null) {
                continue;
            }
            downloads.add(new BatchDownloadWaylinesResponse.DownloadItem(
                    item.getWaylineUuid(),
                    item.getWaylineName(),
                    item.getUrl(),
                    item.getSize()));
        }
        return downloads;
    }
}
