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

package com.dji.fh.feature.flightarea.spi.sdk;

import com.dji.fh.feature.flightarea.model.OfflineMapModelInfo;
import com.dji.fh.feature.flightarea.model.OfflineMapUpdateStatus;
import com.dji.fh.feature.flightarea.service.OfflineMapService;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults;
import com.dji.flighthubsdk.bean.SetOfflineMapsParams;
import com.dji.flighthubsdk.bean.ModelItemInfoData;
import com.dji.flighthubsdk.bean.UpdateOfflineMapsParams;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import com.dji.flighthubsdk.core.Workspace;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 地形障碍物离线地图真实 SDK 实现。
 *
 * <p>通过 {@link SafetyServiceModule} 调用 FlightHub 平台 API，
 * 替代 {@link OfflineMapService} 中的默认 Mock 实现。
 * 仅在 {@code fh.skeleton.sdk.mode=sdk} 且 SDK JAR 存在时激活。
 *
 * <p>数据更新流程：
 * <ol>
 *   <li>{@link #triggerUpdate()} 调用 SDK {@code updateOfflineMapsModelInfo()}</li>
 *   <li>启动定时轮询（每 2s）调用 {@code getOfflineMapsModelInfo()} 获取更新进度</li>
 *   <li>根据 SDK 返回的 status/percent 字段更新状态机，直到终态</li>
 * </ol>
 *
 * @author DJI DevKit Team
 */
public class SdkOfflineMapService extends OfflineMapService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkOfflineMapService.class);

    /** 轮询 SDK 更新进度的间隔（秒） */
    private static final long POLL_INTERVAL_SECONDS = 2;

    /** 更新超时时间（秒），超过则自动标记 TIMEOUT */
    private static final long UPDATE_TIMEOUT_SECONDS = 300;

    private final ScheduledExecutorService pollScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sdk-offline-map-poll");
                t.setDaemon(true);
                return t;
            });

    private volatile ScheduledFuture<?> pollTask;

    private volatile long updateStartTime;

    /**
     * 关闭轮询调度器，防止上下文重启时线程泄露。
     */
    @Override
    @PreDestroy
    public void shutdown() {
        super.shutdown();
        pollScheduler.shutdownNow();
    }

    /**
     * 查询地形模型信息（通过 SDK 获取真实数据）。
     *
     * @return 模型信息
     */
    @Override
    public OfflineMapModelInfo getModelInfo() {
        try {
            SafetyServiceModule ssm = requireSafetyModule();
            CompletableFuture<GetOfflineMapsModelInfoResults> future = new CompletableFuture<>();

            ssm.getOfflineMapsModelInfo((error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "getOfflineMapsModelInfo 失败: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetOfflineMapsModelInfoResults result = SdkBridge.await(future);
            OfflineMapModelInfo info = mapToModelInfo(result);
            modelInfo.set(info);
            return info;
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 查询地形模型信息失败", e);
            return modelInfo.get();
        }
    }

    /**
     * 开关地形障碍物功能（通过 SDK 设置）。
     *
     * @param enabled true=启用，false=禁用
     */
    @Override
    public void setEnabled(boolean enabled) {
        try {
            SafetyServiceModule ssm = requireSafetyModule();
            SetOfflineMapsParams params = new SetOfflineMapsParams();
            params.setOfflineMapEnable(enabled);

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.setOfflineMaps(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "setOfflineMaps 失败: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkBridge.await(future);

            OfflineMapModelInfo current = modelInfo.get();
            modelInfo.set(new OfflineMapModelInfo(
                    !enabled, current.version(), current.updateTime(), current.dataDescription()));
            LOG.info("SDK 地形障碍物功能设置: enabled={}", enabled);
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 设置地形障碍物开关失败", e);
            throw e;
        }
    }

    /**
     * 触发数据更新（调用 SDK 并启动进度轮询）。
     * 使用父类 CAS 方法保证并发安全。
     *
     * @return true=触发成功，false=正在更新中
     */
    @Override
    public boolean triggerUpdate() {
        if (!tryTransitionToWaiting()) {
            LOG.warn("地形数据更新已在进行中，忽略重复触发");
            return false;
        }

        updateProgress.set(0);
        failureReason.set("");

        try {
            SafetyServiceModule ssm = requireSafetyModule();

            GetOfflineMapsModelInfoResults currentInfo = queryModelInfo(ssm);
            if (!hasAvailableModels(currentInfo)) {
                LOG.warn("当前环境无可用地形模型数据，无法触发更新");
                updateStatus.set(OfflineMapUpdateStatus.IDLE);
                throw new SdkBridge.SdkQueryException("当前环境无可用地形模型数据");
            }

            UpdateOfflineMapsParams params = buildUpdateParams(currentInfo);

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.updateOfflineMapsModelInfo(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "updateOfflineMapsModelInfo 失败: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkBridge.await(future);
            LOG.info("SDK 触发地形数据更新成功，启动进度轮询");

            updateStartTime = System.currentTimeMillis();
            startProgressPolling();
            return true;
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 触发地形数据更新失败", e);
            updateStatus.set(OfflineMapUpdateStatus.FAIL);
            failureReason.set(e.getMessage());
            return true;
        }
    }

    /**
     * 取消更新（调用 SDK 取消）。
     * 使用 CAS 保证不会覆盖已完成的 FINISH 状态。
     *
     * @return true=取消成功，false=无进行中的更新
     */
    @Override
    public boolean cancelUpdate() {
        boolean cancelled = updateStatus.compareAndSet(OfflineMapUpdateStatus.PROCESSING,
                OfflineMapUpdateStatus.CANCEL)
                || updateStatus.compareAndSet(OfflineMapUpdateStatus.WAITING,
                OfflineMapUpdateStatus.CANCEL);

        if (!cancelled) {
            return false;
        }

        try {
            SafetyServiceModule ssm = requireSafetyModule();
            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.cancelOfflineMapsModelInfo(error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "cancelOfflineMapsModelInfo 失败: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkBridge.await(future);
            LOG.info("SDK 取消地形数据更新成功");
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 取消地形数据更新失败（状态已置为 CANCEL）", e);
        }

        stopPolling();
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 启动进度轮询任务：定期调用 SDK 获取最新更新状态。
     */
    private void startProgressPolling() {
        stopPolling();

        pollTask = pollScheduler.scheduleAtFixedRate(() -> {
            try {
                if (isUpdateTimeout()) {
                    LOG.warn("地形数据更新超时（{}s），标记为 TIMEOUT", UPDATE_TIMEOUT_SECONDS);
                    updateStatus.set(OfflineMapUpdateStatus.TIMEOUT);
                    stopPolling();
                    return;
                }

                SafetyServiceModule ssm = requireSafetyModule();
                CompletableFuture<GetOfflineMapsModelInfoResults> future = new CompletableFuture<>();
                ssm.getOfflineMapsModelInfo((error, result) -> {
                    if (!error.isSuccess()) {
                        future.completeExceptionally(new SdkBridge.SdkQueryException(
                                "getOfflineMapsModelInfo 失败: " + error.getServerMessage()));
                        return;
                    }
                    future.complete(result);
                });

                GetOfflineMapsModelInfoResults result = SdkBridge.await(future);
                applyUpdateProgress(result);
            } catch (SdkBridge.SdkQueryException e) {
                LOG.warn("轮询更新进度失败: {}", e.getMessage());
            } catch (Exception e) {
                LOG.error("轮询更新进度异常", e);
                updateStatus.set(OfflineMapUpdateStatus.FAIL);
                failureReason.set(e.getMessage());
                stopPolling();
            }
        }, POLL_INTERVAL_SECONDS, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /** SDK status 字段已知值 */
    private static final String SDK_STATUS_FINISH = "finish";
    private static final String SDK_STATUS_UPDATING = "updating";
    private static final String SDK_STATUS_FAIL = "fail";

    /**
     * 根据 SDK 返回结果更新内部状态机。
     *
     * <p>SDK {@code OfflineMapsModelData} 字段：
     * <ul>
     *   <li>status: String 枚举 — "finish"=完成, "updating"=更新中, "fail"=失败, 空/null=空闲</li>
     *   <li>percent: int 0~100 表示进度百分比</li>
     *   <li>result: int 结果码（0=成功）</li>
     * </ul>
     *
     * @param result SDK 查询结果
     */
    private void applyUpdateProgress(GetOfflineMapsModelInfoResults result) {
        if (result == null || result.getOfflineMap() == null) {
            return;
        }

        String sdkStatus = result.getOfflineMap().getStatus();
        int percent = result.getOfflineMap().getPercent();

        if (SDK_STATUS_FINISH.equals(sdkStatus)) {
            updateStatus.set(OfflineMapUpdateStatus.FINISH);
            updateProgress.set(100);
            modelInfo.set(mapToModelInfo(result));
            stopPolling();
            LOG.info("SDK 地形数据更新完成");
        } else if (SDK_STATUS_UPDATING.equals(sdkStatus)) {
            updateStatus.set(OfflineMapUpdateStatus.PROCESSING);
            updateProgress.set(Math.min(percent, 99));
            LOG.debug("SDK 更新进度: {}%", percent);
        } else if (SDK_STATUS_FAIL.equals(sdkStatus)) {
            updateStatus.set(OfflineMapUpdateStatus.FAIL);
            int resultCode = result.getOfflineMap().getResult();
            failureReason.set("SDK result=" + resultCode);
            stopPolling();
            LOG.warn("SDK 地形数据更新失败, result={}", resultCode);
        } else if (sdkStatus == null || sdkStatus.isEmpty()) {
            // 空闲状态 — 保持当前状态不变
            LOG.debug("SDK 状态为空闲，当前状态: {}", updateStatus.get());
        } else {
            LOG.warn("SDK 返回未知更新状态: {}", sdkStatus);
        }
    }

    /**
     * 判断更新是否超时。
     */
    private boolean isUpdateTimeout() {
        return (System.currentTimeMillis() - updateStartTime) > UPDATE_TIMEOUT_SECONDS * 1000;
    }

    private void stopPolling() {
        ScheduledFuture<?> task = pollTask;
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
        pollTask = null;
    }

    /**
     * 查询当前地形模型信息。
     */
    private GetOfflineMapsModelInfoResults queryModelInfo(SafetyServiceModule ssm) {
        CompletableFuture<GetOfflineMapsModelInfoResults> future = new CompletableFuture<>();
        ssm.getOfflineMapsModelInfo((error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getOfflineMapsModelInfo 失败: " + error.getServerMessage()));
                return;
            }
            future.complete(result);
        });
        return SdkBridge.await(future);
    }

    /**
     * 判断是否有可用的地形模型数据。
     */
    private boolean hasAvailableModels(GetOfflineMapsModelInfoResults info) {
        if (info == null || info.getOfflineMap() == null) {
            return false;
        }
        var models = info.getOfflineMap().getModels();
        return models != null && !models.isEmpty();
    }

    /**
     * 从查询结果构建更新参数，将已有模型 UUID 填入 params。
     *
     * <p>注意：SDK OfflineMapsModelInfoData 只暴露 uuid，无 type/fileType 字段，
     * 无法区分 2D 与 3D 模型。当前平台地形障碍物仅使用 3D 全球高程数据集（DEM），
     * 故统一填入 model3dItems。若平台后续支持 2D 模型，需后端提供分类字段后再适配。
     */
    private UpdateOfflineMapsParams buildUpdateParams(GetOfflineMapsModelInfoResults info) {
        UpdateOfflineMapsParams params = new UpdateOfflineMapsParams();

        List<ModelItemInfoData> modelItems = new ArrayList<>();
        for (var model : info.getOfflineMap().getModels()) {
            ModelItemInfoData item = new ModelItemInfoData();
            item.setUuid(model.getUuid());
            modelItems.add(item);
        }

        params.setModel3dItems(modelItems);
        params.setModel2dItems(modelItems);
        return params;
    }

    /**
     * 将 SDK 结果映射为 {@link OfflineMapModelInfo} DTO。
     *
     * <p>SDK {@code OfflineMapsModelData} 不直接提供 version/dataDescription，
     * 通过 models 列表中的首个 UUID 作为版本标识，updatedTime 作为更新时间。
     *
     * @param result SDK 返回结果
     * @return 模型信息 DTO
     */
    private OfflineMapModelInfo mapToModelInfo(GetOfflineMapsModelInfoResults result) {
        boolean disabled = result.isDisable();
        String version = "unknown";
        long updateTime = Instant.now().getEpochSecond();
        String description = "DJI 上云API 2 全球高程地图";

        if (result.getOfflineMap() != null) {
            var offlineMap = result.getOfflineMap();
            if (offlineMap.getUpdatedTime() > 0) {
                updateTime = normalizeToEpochSeconds(offlineMap.getUpdatedTime());
            }
            if (offlineMap.getModels() != null && !offlineMap.getModels().isEmpty()) {
                String uuid = offlineMap.getModels().get(0).getUuid();
                if (uuid != null && !uuid.isEmpty()) {
                    // UUID 前 8 位作为版本显示
                    version = uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
                }
            }
        }
        return new OfflineMapModelInfo(disabled, version, updateTime, description);
    }

    /**
     * 将时间戳统一归一化为秒级 epoch。
     * SDK 可能返回秒或毫秒级时间戳，通过阈值判断：
     * 大于 10^12 视为毫秒，除以 1000 转为秒。
     */
    private static long normalizeToEpochSeconds(long timestamp) {
        if (timestamp > 1_000_000_000_000L) {
            return timestamp / 1000;
        }
        return timestamp;
    }

    /**
     * 获取 SafetyServiceModule，不可用时抛出异常。
     *
     * @return SafetyServiceModule 实例
     * @throws IllegalStateException SDK 未初始化或模块不可用
     */
    private SafetyServiceModule requireSafetyModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 不可用，SDK 未初始化");
        }
        SafetyServiceModule ssm = ws.getSafetyServiceModule();
        if (ssm == null) {
            throw new IllegalStateException("SafetyServiceModule 不可用");
        }
        return ssm;
    }
}
