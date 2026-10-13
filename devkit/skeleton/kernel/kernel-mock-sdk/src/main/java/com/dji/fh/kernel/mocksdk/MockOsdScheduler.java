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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 按固定周期向所有在线设备推送 OSD 快照。
 *
 * <p>调度通过 Spring {@link Scheduled @Scheduled(fixedRateString = "${fh.skeleton.mock.osd.interval-millis:1000}")}。
 * 单次 {@link #tick()} 遍历 {@link MockDeviceRegistry#findAllOnline()}，对每个设备调
 * {@link MockFlightHubSdk#fireOsd(String, Map)}。
 *
 * <p>OSD 数据为伪造的合理范围随机值（battery_percent / altitude / mode / longitude / latitude），
 * 供 ISV demo 看起来像真设备。
 *
 * <p>每个设备根据 SN 哈希分配一个稳定的"基站坐标"（从中国主要城市池选取），
 * 后续 Tick 在基站附近小幅偏移，模拟真实飞行或静止状态。
 *
 * @author DJI DevKit Team
 */
public class MockOsdScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(MockOsdScheduler.class);

    /**
     * 中国主要城市坐标池，用于给 mock 设备分配分散的基站位置。
     * 格式：{纬度, 经度}
     */
    private static final double[][] CITY_COORDS = {
            {22.54, 113.94},  // 深圳
            {23.13, 113.26},  // 广州
            {39.91, 116.40},  // 北京
            {31.23, 121.47},  // 上海
            {30.57, 104.07},  // 成都
            {29.56, 106.55},  // 重庆
            {30.29, 120.15},  // 杭州
            {32.06, 118.80},  // 南京
            {34.26, 108.94},  // 西安
            {28.23, 112.94},  // 长沙
            {36.07, 120.38},  // 青岛
            {26.08, 119.30},  // 福州
            {25.04, 102.71},  // 昆明
            {45.75, 126.65},  // 哈尔滨
            {43.88, 125.32},  // 长春
            {41.80, 123.43},  // 沈阳
            {38.05, 114.49},  // 石家庄
            {36.65, 117.00},  // 济南
            {27.99, 120.70},  // 温州
            {24.48, 118.09},  // 厦门
    };

    private final MockFlightHubSdk sdk;
    private final MockDeviceRegistry registry;

    /** SN → 该设备对应的城市基站坐标索引（首次 tick 时惰性分配） */
    private final ConcurrentHashMap<String, Integer> snCityIndex = new ConcurrentHashMap<>();

    /**
     * @param sdk      Mock SDK
     * @param registry 设备注册表
     */
    public MockOsdScheduler(MockFlightHubSdk sdk, MockDeviceRegistry registry) {
        this.sdk = Objects.requireNonNull(sdk, "sdk 不能为空");
        this.registry = Objects.requireNonNull(registry, "registry 不能为空");
    }

    /**
     * 周期 tick 入口。默认 1Hz，可通过 {@code fh.skeleton.mock.osd.interval-millis} 调整。
     */
    @Scheduled(fixedRateString = "${fh.skeleton.mock.osd.interval-millis:1000}")
    public void tick() {
        List<MockDeviceRegistry.Device> online = registry.findAllOnline();
        if (online.isEmpty()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("mock OSD tick：向 {} 个在线设备推送（{}）", online.size(),
                    online.stream().map(MockDeviceRegistry.Device::sn).toList());
        }
        for (MockDeviceRegistry.Device device : online) {
            sdk.fireOsd(device.sn(), generateOsd(device.sn(), device.type()));
        }
    }

    /**
     * 根据 SN 哈希值为设备分配一个稳定的城市坐标索引。
     */
    private int getCityIndex(String sn) {
        return snCityIndex.computeIfAbsent(sn,
                k -> (k.hashCode() & 0x7FFF_FFFF) % CITY_COORDS.length);
    }

    private Map<String, Object> generateOsd(String sn, DeviceType type) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Map<String, Object> osd = new HashMap<>();
        osd.put("battery_percent", 50 + rng.nextInt(50));
        osd.put("mode", "MANUAL");

        int cityIdx = getCityIndex(sn);
        double baseLat = CITY_COORDS[cityIdx][0];
        double baseLng = CITY_COORDS[cityIdx][1];

        if (type == DeviceType.DRONE) {
            osd.put("altitude", 10.0 + rng.nextDouble() * 50);
            osd.put("lat", baseLat + rng.nextDouble() * 0.01);
            osd.put("lng", baseLng + rng.nextDouble() * 0.01);
            osd.put("speed", rng.nextDouble() * 15);
        } else {
            osd.put("lat", baseLat + 0.005);
            osd.put("lng", baseLng + 0.005);
            osd.put("cover_state", "CLOSED");
            osd.put("power_state", "AC");
            osd.put("wind_speed", rng.nextDouble() * 5);
        }
        return osd;
    }
}
