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

package com.dji.fh.feature.flightarea.service;

import com.dji.fh.feature.flightarea.model.OfflineMapModelInfo;
import com.dji.fh.feature.flightarea.model.OfflineMapUpdateState;
import com.dji.fh.feature.flightarea.model.OfflineMapUpdateStatus;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 地形障碍物（离线地图）管理服务。
 *
 * <p>实现异步数据更新流程，对标 DJI 上云API 2 TerrainObstacle 模块的状态机：
 * IDLE → WAITING → PROCESSING(0~100%) → FINISH/FAIL/CANCEL/TIMEOUT
 *
 * <p>本类为 Mock 实现（异步模拟），生产环境使用 {@code SdkOfflineMapService} 子类
 * 调用真实 SDK {@code SafetyServiceModule}。
 *
 * @author DJI DevKit Team
 */
public class OfflineMapService {

    private static final Logger LOG = LoggerFactory.getLogger(OfflineMapService.class);

    /** Mock 模式下每次进度增量（百分比） */
    private static final int MOCK_PROGRESS_STEP = 10;

    /** Mock 模式下进度更新间隔（毫秒） */
    private static final long MOCK_PROGRESS_INTERVAL_MS = 800;

    /** Mock 模式下初始等待时间（毫秒） */
    private static final long MOCK_WAITING_DELAY_MS = 1000;

    protected final AtomicReference<OfflineMapModelInfo> modelInfo = new AtomicReference<>(
            new OfflineMapModelInfo(false, "1.0.0", Instant.now().getEpochSecond(), "DJI 上云API 2 全球高程地图")
    );

    protected final AtomicReference<OfflineMapUpdateStatus> updateStatus =
            new AtomicReference<>(OfflineMapUpdateStatus.IDLE);

    protected final AtomicInteger updateProgress = new AtomicInteger(0);

    /** 失败原因（仅 FAIL/TIMEOUT 状态时有值） */
    protected final AtomicReference<String> failureReason = new AtomicReference<>("");

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "offline-map-update");
                t.setDaemon(true);
                return t;
            });

    private volatile ScheduledFuture<?> progressTask;

    /**
     * 查询地形模型信息。
     */
    public OfflineMapModelInfo getModelInfo() {
        return modelInfo.get();
    }

    /**
     * 查询当前更新状态快照。
     *
     * @return 包含状态、进度、描述信息的快照
     */
    public OfflineMapUpdateState getUpdateState() {
        OfflineMapUpdateStatus status = updateStatus.get();
        int progress = updateProgress.get();
        String message = buildStatusMessage(status, progress, failureReason.get());
        return new OfflineMapUpdateState(status, progress, message);
    }

    /**
     * 开关地形障碍物功能。
     *
     * @param enabled true=启用，false=禁用
     */
    public void setEnabled(boolean enabled) {
        OfflineMapModelInfo current = modelInfo.get();
        modelInfo.set(new OfflineMapModelInfo(
                !enabled, current.version(), current.updateTime(), current.dataDescription()));
        LOG.info("地形障碍物功能: enabled={}", enabled);
    }

    /**
     * 触发数据更新（异步）。
     *
     * <p>使用 CAS 保证并发安全：仅当状态为 IDLE/终态时才能触发。
     * Mock 模式下通过定时任务模拟进度推进（WAITING → PROCESSING → FINISH）；
     * 生产模式下子类覆写此方法调用 SDK。
     *
     * @return true=触发成功，false=正在更新中无法重复触发
     */
    public boolean triggerUpdate() {
        if (!tryTransitionToWaiting()) {
            LOG.warn("地形数据更新已在进行中，忽略重复触发");
            return false;
        }

        updateProgress.set(0);
        failureReason.set("");
        LOG.info("触发地形数据更新（异步）");
        startMockProgressSimulation();
        return true;
    }

    /**
     * 取消更新。
     *
     * <p>使用 CAS 保证不会覆盖已完成的 FINISH 状态。
     *
     * @return true=取消成功，false=当前无进行中的更新
     */
    public boolean cancelUpdate() {
        boolean cancelled = updateStatus.compareAndSet(OfflineMapUpdateStatus.PROCESSING,
                OfflineMapUpdateStatus.CANCEL)
                || updateStatus.compareAndSet(OfflineMapUpdateStatus.WAITING,
                OfflineMapUpdateStatus.CANCEL);

        if (!cancelled) {
            return false;
        }

        stopProgressTask();
        LOG.info("取消地形数据更新");
        return true;
    }

    /**
     * 重置更新状态为 IDLE（用于前端确认终态后清理）。
     */
    public void resetUpdateState() {
        OfflineMapUpdateStatus current = updateStatus.get();
        if (current == OfflineMapUpdateStatus.FINISH
                || current == OfflineMapUpdateStatus.FAIL
                || current == OfflineMapUpdateStatus.CANCEL
                || current == OfflineMapUpdateStatus.TIMEOUT) {
            updateStatus.compareAndSet(current, OfflineMapUpdateStatus.IDLE);
            updateProgress.set(0);
            failureReason.set("");
        }
    }

    /**
     * 关闭后台调度器，防止上下文重启时线程泄露。
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // ==================== protected 方法 ====================

    /**
     * 尝试将状态从非活跃态 CAS 为 WAITING。
     *
     * @return true=CAS 成功，false=当前处于 WAITING/PROCESSING 无法触发
     */
    protected boolean tryTransitionToWaiting() {
        while (true) {
            OfflineMapUpdateStatus current = updateStatus.get();
            if (current == OfflineMapUpdateStatus.PROCESSING
                    || current == OfflineMapUpdateStatus.WAITING) {
                return false;
            }
            if (updateStatus.compareAndSet(current, OfflineMapUpdateStatus.WAITING)) {
                return true;
            }
        }
    }

    protected static String buildStatusMessage(OfflineMapUpdateStatus status, int progress,
                                                String reason) {
        return switch (status) {
            case IDLE -> "就绪";
            case WAITING -> "服务准备中...";
            case PROCESSING -> "正在更新 " + progress + "%";
            case FINISH -> "更新完成";
            case FAIL -> reason != null && !reason.isEmpty()
                    ? "更新失败: " + reason : "更新失败";
            case CANCEL -> "已取消";
            case TIMEOUT -> reason != null && !reason.isEmpty()
                    ? "更新超时: " + reason : "更新超时";
        };
    }

    // ==================== 私有方法 ====================

    /**
     * Mock 模式：启动异步进度模拟。
     *
     * <p>所有状态变更和 progressTask 赋值都在 scheduler 单线程内执行，
     * 消除 WAITING→PROCESSING 切换期间的竞态。
     */
    private void startMockProgressSimulation() {
        stopProgressTask();

        progressTask = scheduler.schedule(() -> {
            // 在 scheduler 线程内检查状态是否仍为 WAITING（可能已被 cancel）
            if (updateStatus.get() != OfflineMapUpdateStatus.WAITING) {
                return;
            }
            updateStatus.set(OfflineMapUpdateStatus.PROCESSING);
            LOG.debug("地形数据更新进入 PROCESSING 阶段");

            progressTask = scheduler.scheduleAtFixedRate(this::advanceMockProgress,
                    0, MOCK_PROGRESS_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }, MOCK_WAITING_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Mock 进度推进单步。
     * 每次执行前检查状态是否仍为 PROCESSING，若已被取消则不推进。
     */
    private void advanceMockProgress() {
        if (updateStatus.get() != OfflineMapUpdateStatus.PROCESSING) {
            stopProgressTask();
            return;
        }

        int newProgress = updateProgress.addAndGet(MOCK_PROGRESS_STEP);
        LOG.debug("地形数据更新进度: {}%", newProgress);

        if (newProgress >= 100) {
            updateProgress.set(100);
            updateStatus.set(OfflineMapUpdateStatus.FINISH);
            updateModelInfoOnFinish();
            stopProgressTask();
        }
    }

    /**
     * 更新完成后刷新模型信息（版本号递增、更新时间刷新）。
     */
    private void updateModelInfoOnFinish() {
        OfflineMapModelInfo current = modelInfo.get();
        String newVersion = incrementVersion(current.version());
        long now = Instant.now().getEpochSecond();
        modelInfo.set(new OfflineMapModelInfo(
                current.disabled(), newVersion, now, current.dataDescription()));
        LOG.info("地形数据更新完成，版本: {} → {}", current.version(), newVersion);
    }

    private void stopProgressTask() {
        ScheduledFuture<?> task = progressTask;
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
        progressTask = null;
    }

    /**
     * 版本号递增：1.0.0 → 1.0.1。
     * 非数字 patch 段安全回退为 version + ".1"。
     */
    private static String incrementVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length == 3) {
            try {
                int patch = Integer.parseInt(parts[2]) + 1;
                return parts[0] + "." + parts[1] + "." + patch;
            } catch (NumberFormatException e) {
                return version + ".1";
            }
        }
        return version + ".1";
    }
}
