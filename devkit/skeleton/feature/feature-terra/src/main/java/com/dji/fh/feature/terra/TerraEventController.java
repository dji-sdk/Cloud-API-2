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

package com.dji.fh.feature.terra;

import com.dji.fh.feature.terra.dto.TaskStatusDto;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 端点 -- 推送重建任务状态变更.
 *
 * <p>Mock 模式：定时器每 3 秒推送模拟进度递增.
 * Real 模式：桥接 SDK listenReconstructionStatusChanged 回调.
 *
 * <p>安全发布专项修复：任务状态流含业务数据，强制登录。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/terra/tasks")
@RequireAuthenticated
public class TerraEventController {

    /** 日志实例. */
    private static final Logger LOG =
            LoggerFactory.getLogger(TerraEventController.class);

    /** SSE 超时时间（毫秒）：30 分钟. */
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /** Mock 进度递增步长. */
    private static final int PROGRESS_STEP = 5;

    /** Mock 进度取模值（超过 100 重置）. */
    private static final int PROGRESS_MODULO = 105;

    /** 进度满值. */
    private static final int PROGRESS_COMPLETE = 100;

    /** Mock 调度间隔（秒）. */
    private static final int SCHEDULE_INTERVAL_SECONDS = 3;

    /** 活跃的 SSE 连接列表. */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /** Terra 配置属性. */
    private final TerraProperties properties;

    /** Mock 模式调度器. */
    private ScheduledExecutorService mockScheduler;

    /**
     * 构造函数.
     *
     * @param terraProperties Terra 配置属性
     */
    public TerraEventController(final TerraProperties terraProperties) {
        this.properties = terraProperties;
        if (terraProperties.mock()) {
            startMockScheduler();
        }
    }

    /**
     * 订阅任务状态 SSE 流.
     *
     * @return SSE emitter
     */
    @GetMapping("/events")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    /**
     * 向所有订阅者推送任务状态事件.
     *
     * <p>供 RealTerraService 的 SDK 回调调用.
     *
     * @param status 任务状态 DTO
     */
    public void broadcast(final TaskStatusDto status) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(status));
            } catch (IOException e) {
                LOG.warn("[TerraSSE] 推送失败，移除断开的 emitter: {}", e.getMessage());
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }

    /**
     * 销毁时关闭 mock 调度线程.
     */
    @PreDestroy
    void shutdown() {
        if (mockScheduler != null) {
            mockScheduler.shutdownNow();
            LOG.info("[TerraSSE] Mock 调度器已关闭");
        }
    }

    private void startMockScheduler() {
        mockScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "terra-mock-sse");
            t.setDaemon(true);
            return t;
        });
        final int[] progress = {0};
        mockScheduler.scheduleAtFixedRate(() -> {
            if (emitters.isEmpty()) {
                return;
            }
            progress[0] =
                    (progress[0] + PROGRESS_STEP) % PROGRESS_MODULO;
            String state = progress[0] >= PROGRESS_COMPLETE
                    ? "success" : "processing";
            int p = Math.min(progress[0], PROGRESS_COMPLETE);
            broadcast(new TaskStatusDto(
                    "mock-task-001", state, p,
                    "/mock/input", "/mock/sfm"));
        }, SCHEDULE_INTERVAL_SECONDS,
                SCHEDULE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 用于测试的活跃连接数.
     *
     * @return 当前活跃 emitter 数量
     */
    int getActiveEmitterCount() {
        return emitters.size();
    }
}
