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

package com.dji.fh.feature.device;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 设备状态管理 feature 配置。
 *
 * @param hmsMaxPerDevice        每设备 HMS 告警队列最大长度（默认 50）
 * @param enabled                是否启用本 feature（默认 true，设 false 则整个 AutoConfig 跳过）
 * @param livenessTimeout        存活超时：事件驱动在线态在此时长内未收到任何存活信号即判离线
 *                               （默认 5 分钟；设 {@link Duration#ZERO} 可关闭该兜底，退回纯事件驱动）
 * @param reconcileConfirmWindow 缓存自愈确认窗口：事件缓存判定 offline、但 SDK 实时快照连续
 *                               判定为 online 达到该时长后，纠正缓存为 online（默认 30 秒）
 * @author DJI DevKit Team
 */
@ConfigurationProperties("fh.skeleton.feature.device")
public record DeviceProperties(
        int hmsMaxPerDevice,
        boolean enabled,
        Duration livenessTimeout,
        Duration reconcileConfirmWindow
) {

    private static final int DEFAULT_HMS_MAX = 50;

    /**
     * 存活超时默认值。
     *
     * <p>取值需同时满足两侧约束：
     * <ul>
     *   <li><strong>下界</strong>：飞行器 OSD 由 kernel 的 {@code OsdAggregator} 以 1Hz 合成推送，
     *       机场仅有 {@code DockOsdUpdateEvent}（低频，且 listen 注册可能失败）——
     *       默认值必须远大于两者的推送间隔，否则真实在线的设备会被误判离线。</li>
     *   <li><strong>上界</strong>：设备真实离线但平台 device 表未回写、也未推送下线事件时，
     *       本兜底是唯一的收敛手段 —— 取值越小越快纠正，但误判风险越高。</li>
     * </ul>
     *
     * <p>5 分钟是在"机场心跳频率未知"前提下的保守选择。若现场确认机场位置回调的实际
     * 频率，可通过 {@code fh.skeleton.feature.device.liveness-timeout} 收紧。
     */
    private static final Duration DEFAULT_LIVENESS_TIMEOUT = Duration.ofMinutes(5);

    /**
     * 缓存自愈确认窗口默认值。
     *
     * <p>取值需要大于单次 SDK 设备列表查询缓存 TTL（{@code DeviceStatusService.DEVICE_LIST_CACHE_TTL}，
     * 5 秒），避免同一个查询周期内的抖动被误判为"持续在线"；同时不宜过长，否则现场发生本类
     * 缓存卡死问题时收敛太慢。30 秒是两者之间的保守折中。
     */
    private static final Duration DEFAULT_RECONCILE_CONFIRM_WINDOW = Duration.ofSeconds(30);

    public DeviceProperties {
        if (hmsMaxPerDevice <= 0) {
            hmsMaxPerDevice = DEFAULT_HMS_MAX;
        }
        if (livenessTimeout == null || livenessTimeout.isNegative()) {
            livenessTimeout = DEFAULT_LIVENESS_TIMEOUT;
        }
        if (reconcileConfirmWindow == null || reconcileConfirmWindow.isNegative()) {
            reconcileConfirmWindow = DEFAULT_RECONCILE_CONFIRM_WINDOW;
        }
    }
}
