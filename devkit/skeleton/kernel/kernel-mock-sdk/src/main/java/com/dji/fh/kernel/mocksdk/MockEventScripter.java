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

package com.dji.fh.kernel.mocksdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 启动脚本回放器：按 {@link MockSdkProperties.Script#getEvents()} 配置的时间轴，
 * 在启动后逐个触发事件。
 *
 * <p>由 {@code MockSdkAutoConfiguration} 在 {@code ApplicationReadyEvent} 后调 {@link #play()}。
 *
 * <p>底层用单线程 {@link ScheduledExecutorService}，每个事件 {@link ScheduledExecutorService#schedule}
 * 到对应 atMillis；执行时根据 type 派发到 {@link MockEventTrigger} 对应方法。
 *
 * @author DJI DevKit Team
 */
public class MockEventScripter {

    private static final Logger LOG = LoggerFactory.getLogger(MockEventScripter.class);

    private static final String TYPE_DEVICE_ONLINE = "device-online";
    private static final String TYPE_DEVICE_OFFLINE = "device-offline";
    private static final String TYPE_FLIGHT_TASK = "flight-task-progress";
    private static final String TYPE_HMS = "hms-alert";

    private static final String DEVICE_KEY_DOCK = "dock";

    private final MockEventTrigger trigger;
    private final MockSdkProperties props;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "fh-mock-scripter");
                t.setDaemon(true);
                return t;
            });

    /**
     * @param trigger 触发 API
     * @param props   Mock 配置（含 device sn / dock sn / script events）
     */
    public MockEventScripter(MockEventTrigger trigger, MockSdkProperties props) {
        this.trigger = Objects.requireNonNull(trigger, "trigger 不能为空");
        this.props = Objects.requireNonNull(props, "props 不能为空");
    }

    /**
     * 启动脚本回放。若 script.enabled=false 则 no-op。
     */
    public void play() {
        if (!props.getScript().isEnabled()) {
            LOG.info("mock script 已禁用，跳过启动脚本回放");
            return;
        }
        List<MockSdkProperties.ScriptEvent> events = props.getScript().getEvents();
        if (events.isEmpty()) {
            LOG.info("mock script events 为空：启动后不会自动触发事件，需通过 POST /mock/devices/{sn}/online 等 REST 端点手动触发");
            return;
        }
        LOG.info("mock script 启动，回放 {} 条事件", events.size());
        for (MockSdkProperties.ScriptEvent ev : events) {
            scheduler.schedule(() -> dispatch(ev), ev.getAtMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 关闭 scheduler（spring shutdown 时调用 / 测试 teardown 调用）。
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void dispatch(MockSdkProperties.ScriptEvent ev) {
        try {
            switch (ev.getType()) {
                case TYPE_DEVICE_ONLINE -> trigger.fireDeviceOnline(resolveSn(ev.getDevice()));
                case TYPE_DEVICE_OFFLINE -> trigger.fireDeviceOffline(resolveSn(ev.getDevice()), "scripted");
                case TYPE_FLIGHT_TASK -> trigger.fireFlightTaskProgress(
                        resolveSn(ev.getDevice()), "scripted-task", ev.getStatus(), 0);
                case TYPE_HMS -> trigger.fireHmsAlert(
                        resolveSn(ev.getDevice()), "SCRIPT-HMS", "WARNING", "scripted hms alert");
                default -> LOG.warn("mock script 未知事件类型: {}", ev.getType());
            }
        } catch (RuntimeException e) {
            LOG.warn("mock script 派发事件 {} 失败: {}", ev.getType(), e.getMessage(), e);
        }
    }

    private String resolveSn(String deviceKey) {
        if (DEVICE_KEY_DOCK.equalsIgnoreCase(deviceKey)) {
            return props.getDock().getSn();
        }
        // 默认 drone（含 null / drone / 未知值）
        return props.getDevice().getSn();
    }
}
