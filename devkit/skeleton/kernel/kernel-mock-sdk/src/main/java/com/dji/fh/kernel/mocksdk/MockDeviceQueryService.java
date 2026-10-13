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

import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mock 实现的设备列表查询服务。
 *
 * <p>直接从 {@link MockDeviceRegistry} 内存注册表返回全量设备快照。
 * dock 的 subDevices 包含所有 drone，与真 SDK 返回的拓扑结构一致。
 *
 * @author DJI DevKit Team
 */
public class MockDeviceQueryService implements DeviceQueryService {

    private final MockDeviceRegistry registry;

    public MockDeviceQueryService(MockDeviceRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry 不能为空");
    }

    @Override
    public List<DeviceSnapshot> listDevices() {
        List<MockDeviceRegistry.Device> all = registry.findAll();

        List<DeviceSnapshot> droneSnapshots = new ArrayList<>();
        List<DeviceSnapshot> dockSnapshots = new ArrayList<>();

        for (MockDeviceRegistry.Device d : all) {
            DeviceSnapshot snap = new DeviceSnapshot(
                    d.sn(), d.type().name(), d.model(), d.sn(), null, d.online());
            if (d.type() == DeviceType.DRONE) {
                droneSnapshots.add(snap);
            } else {
                dockSnapshots.add(snap);
            }
        }

        List<DeviceSnapshot> result = new ArrayList<>();
        for (DeviceSnapshot dock : dockSnapshots) {
            result.add(new DeviceSnapshot(
                    dock.sn(), dock.deviceClass(), dock.modelKey(),
                    dock.deviceName(), dock.nickname(), dock.online(),
                    List.copyOf(droneSnapshots)));
        }
        result.addAll(droneSnapshots);
        return result;
    }
}
