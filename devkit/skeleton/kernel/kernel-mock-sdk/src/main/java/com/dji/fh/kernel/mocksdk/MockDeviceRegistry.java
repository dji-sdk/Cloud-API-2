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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Mock 虚拟设备状态注册表。
 *
 * <p>线程安全：内部 {@link ConcurrentHashMap} + 每个 device 自带 {@link AtomicBoolean} online 标志。
 * 适合 OSD scheduler / REST controller / scripter 并发读写。
 *
 * @author DJI DevKit Team
 */
public class MockDeviceRegistry {

    private final ConcurrentMap<String, MutableDevice> devices = new ConcurrentHashMap<>();

    /**
     * 注册新虚拟设备（默认离线）。已存在则覆盖。
     *
     * @param sn    设备 SN
     * @param type  设备类型
     * @param model 设备型号
     */
    public void register(String sn, DeviceType type, String model) {
        Objects.requireNonNull(sn, "sn 不能为空");
        Objects.requireNonNull(type, "type 不能为空");
        Objects.requireNonNull(model, "model 不能为空");
        devices.put(sn, new MutableDevice(sn, type, model));
    }

    /**
     * 标记设备上线。设备不存在则静默跳过。
     *
     * @param sn 设备 SN
     */
    public void markOnline(String sn) {
        Objects.requireNonNull(sn, "sn 不能为空");
        MutableDevice d = devices.get(sn);
        if (d != null) {
            d.online.set(true);
        }
    }

    /**
     * 标记设备下线。设备不存在则静默跳过。
     *
     * @param sn 设备 SN
     */
    public void markOffline(String sn) {
        Objects.requireNonNull(sn, "sn 不能为空");
        MutableDevice d = devices.get(sn);
        if (d != null) {
            d.online.set(false);
        }
    }

    /**
     * 按 SN 查询设备。
     *
     * @param sn 设备 SN
     * @return 存在则返回不可变快照，不存在返回 empty
     */
    public Optional<Device> find(String sn) {
        Objects.requireNonNull(sn, "sn 不能为空");
        MutableDevice d = devices.get(sn);
        return d == null ? Optional.empty() : Optional.of(d.snapshot());
    }

    /**
     * 返回全部在线设备快照。
     *
     * @return 在线设备列表（顺序由 ConcurrentHashMap 决定）
     */
    public List<Device> findAllOnline() {
        return devices.values().stream()
                .filter(d -> d.online.get())
                .map(MutableDevice::snapshot)
                .collect(Collectors.toList());
    }

    /**
     * 返回全部已注册设备快照（含离线设备）。
     *
     * @return 全量设备列表
     */
    public List<Device> findAll() {
        return devices.values().stream()
                .map(MutableDevice::snapshot)
                .collect(Collectors.toList());
    }

    /**
     * 设备不可变快照。供外部读取。
     *
     * @param sn     设备 SN
     * @param type   设备类型
     * @param model  设备型号
     * @param online 是否在线
     * @author DJI DevKit Team
     */
    public record Device(String sn, DeviceType type, String model, boolean online) {
    }

    /**
     * 内部可变设备状态。
     */
    private static final class MutableDevice {

        private final String sn;
        private final DeviceType type;
        private final String model;
        private final AtomicBoolean online = new AtomicBoolean(false);

        MutableDevice(String sn, DeviceType type, String model) {
            this.sn = sn;
            this.type = type;
            this.model = model;
        }

        Device snapshot() {
            return new Device(sn, type, model, online.get());
        }
    }
}
