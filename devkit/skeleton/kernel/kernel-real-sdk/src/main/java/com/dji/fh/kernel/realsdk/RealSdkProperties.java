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

package com.dji.fh.kernel.realsdk;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 真 SDK 启动 / 运行参数。
 *
 * <p>对应 {@code application.yml} 中的 {@code fh.skeleton.sdk.real.*}。
 * 与 mock SDK 的 {@code fh.skeleton.mock.*} 对称设计。
 *
 * <p>4 项必填（{@code token} / {@code address} / {@code port} / {@code notificationServerAddress}），
 * 其余字段在构造器内做 null/blank 兜底。字段名 1:1 对应 SDK {@code FlightHubSdkConfig} setter
 * （javap 实查后校准）。
 *
 * @author DJI DevKit Team
 */
@ConfigurationProperties("fh.skeleton.sdk.real")
public record RealSdkProperties(
        /** SDK workspace token（必填，生产建议通过 env / vault 注入） */
        String token,
        /** HTTP API 服务端 host，不带 scheme/port（必填，如 {@code api.example.com}） */
        String address,
        /** HTTP API 服务端 port（必填，如 {@code 443}） */
        String port,
        /** 通知 / WebSocket 服务端 URL（必填，如 {@code wss://api.example.com/ws}） */
        String notificationServerAddress,
        /** SDK 内部日志级别（默认 info） */
        String logLevel,
        /** 启动时拉初始设备清单的超时（默认 10s） */
        Duration deviceListTimeout,
        /**
         * OSD 合成 Tick 间隔（毫秒，默认 1000，即 1Hz，与 mock 对齐）。
         *
         * <p><strong>类型说明</strong>：本字段类型为 {@code long}（毫秒），与其他 {@code Duration}
         * 字段不一致。原因：{@link OsdAggregator#tick()} 使用 Spring
         * {@code @Scheduled(fixedRateString = "${fh.skeleton.sdk.real.osd-interval-millis:1000}")}，
         * 该 SpEL 表达式要求整数字面量，无法直接解析 {@code Duration} 字符串（如 "1s"）。
         */
        long osdIntervalMillis,
        /** OSD 单 source 快照保留时长（默认 5s，超时视为陈旧不发） */
        Duration osdSnapshotTtl,
        /** Dock location listen 注册失败后重试延迟（默认 20s，需等 SDK 通知 WebSocket 就绪） */
        Duration dockListenRetryDelay,
        /** Dock location listen 最大重试次数（默认 3） */
        int dockListenMaxRetries,
        /** HTTP 请求鉴权 header key（如 {@code user_id}，为空则不设置） */
        String httpAuthHeaderKey,
        /** HTTP 请求鉴权 header value（如 {@code your-auth-value}，为空则不设置；值为占位示例，需按平台实际配置） */
        String httpAuthHeaderValue,
        /**
         * 设备状态周期调和间隔（默认 2 分钟）。
         *
         * <p>SDK 的 {@code listenDroneOnlineStatus} / {@code listenDockOnlineStatus} 是事件驱动的
         * （仅在状态变化时回调），不提供周期性心跳。{@code DeviceStatusService} 的存活超时
         * （{@code liveness-timeout}）需要周期性信号来刷新 {@code lastSeenAt}，否则稳定在线的
         * 设备会在超时后被误判为离线。
         *
         * <p>本调和任务周期性调用 {@code getDeviceList()} 并为在线设备发布
         * {@link com.dji.fh.kernel.spi.event.DeviceOnlineEvent}，充当心跳替代。
         * 间隔应小于 {@code liveness-timeout}（默认 5 分钟），设为 {@link Duration#ZERO} 可关闭。
         */
        Duration reconcileInterval
) {

    /** 缺省值：日志级别 */
    private static final String DEFAULT_LOG_LEVEL = "info";

    /** 缺省值：设备清单超时 */
    private static final Duration DEFAULT_DEVICE_LIST_TIMEOUT = Duration.ofSeconds(10);

    /** 缺省值：OSD Tick 间隔（毫秒） */
    private static final long DEFAULT_OSD_INTERVAL_MILLIS = 1000L;

    /** 缺省值：OSD 快照 TTL */
    private static final Duration DEFAULT_OSD_SNAPSHOT_TTL = Duration.ofSeconds(5);

    /** 缺省值：Dock listen 重试延迟 */
    private static final Duration DEFAULT_DOCK_LISTEN_RETRY_DELAY = Duration.ofSeconds(20);

    /** 缺省值：Dock listen 最大重试次数 */
    private static final int DEFAULT_DOCK_LISTEN_MAX_RETRIES = 3;

    /** 缺省值：设备状态调和间隔 */
    private static final Duration DEFAULT_RECONCILE_INTERVAL = Duration.ofMinutes(2);

    /**
     * 紧凑构造器：对非必填字段做 null/blank/非正值兜底。
     */
    public RealSdkProperties {
        if (logLevel == null || logLevel.isBlank()) {
            logLevel = DEFAULT_LOG_LEVEL;
        }
        if (deviceListTimeout == null) {
            deviceListTimeout = DEFAULT_DEVICE_LIST_TIMEOUT;
        }
        if (osdIntervalMillis <= 0) {
            osdIntervalMillis = DEFAULT_OSD_INTERVAL_MILLIS;
        }
        if (osdSnapshotTtl == null) {
            osdSnapshotTtl = DEFAULT_OSD_SNAPSHOT_TTL;
        }
        if (dockListenRetryDelay == null) {
            dockListenRetryDelay = DEFAULT_DOCK_LISTEN_RETRY_DELAY;
        }
        if (dockListenMaxRetries <= 0) {
            dockListenMaxRetries = DEFAULT_DOCK_LISTEN_MAX_RETRIES;
        }
        if (reconcileInterval == null || reconcileInterval.isNegative()) {
            reconcileInterval = DEFAULT_RECONCILE_INTERVAL;
        }
    }
}
