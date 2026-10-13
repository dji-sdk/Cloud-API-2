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

package com.dji.fh.feature.xxx;

import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * [模块名] 事件监听 Service。
 *
 * <p>通过 {@code @EventListener} 消费 kernel-spi 定义的 KernelEvent 子类。
 * 所有状态存储在 ConcurrentHashMap 中，volatile + synchronized 保证线程安全。
 *
 * @author TODO
 */
public class XxxService {

    private static final Logger LOG = LoggerFactory.getLogger(XxxService.class);

    private final ConcurrentHashMap<String, Object> state = new ConcurrentHashMap<>();
    private final XxxProperties props;

    public XxxService(XxxProperties props) {
        this.props = props;
    }

    // ---------- 事件消费 ----------

    /**
     * 设备上线事件处理。
     */
    @EventListener
    public void onDeviceOnline(DeviceOnlineEvent event) {
        LOG.debug("设备上线：sn={}, type={}", event.deviceSn(), event.deviceType());
        // TODO: 业务逻辑
    }

    /**
     * 设备下线事件处理。
     */
    @EventListener
    public void onDeviceOffline(DeviceOfflineEvent event) {
        LOG.debug("设备下线：sn={}", event.deviceSn());
        // TODO: 业务逻辑
    }

    /**
     * OSD 数据更新处理。
     * 注意：此方法调用频率高（1-5Hz），实现必须轻量。
     */
    @EventListener
    public void onOsdUpdate(OsdUpdateEvent event) {
        // TODO: 业务逻辑（注意性能，不可阻塞）
    }

    /**
     * HMS 告警处理。
     */
    @EventListener
    public void onHmsAlert(HmsAlertEvent event) {
        LOG.info("HMS 告警：sn={}, code={}, level={}",
                event.deviceSn(), event.alertCode(), event.level());
        // TODO: 分级处理逻辑
    }

    // ---------- 查询方法 ----------

    // TODO: 添加面向 Controller 的查询方法
}
