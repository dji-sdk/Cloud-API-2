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

import com.dji.fh.kernel.spi.event.OsdData;
import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 3 个 SDK 子项（位置 / 电池 / 姿态）合并成统一 {@link OsdUpdateEvent}。
 *
 * <p>每收到一条 source callback 就更新 {@code snapshots.get(sn)} 对应字段，
 * 不立即发事件。{@link #tick()} 按 {@link RealSdkProperties#osdIntervalMillis()}
 * 频率（默认 1Hz）扫描 snapshots，对每个 sn 合成一条 OsdUpdateEvent 发出。
 *
 * <p>陈旧 snapshot（最近一次更新时间 &gt; {@code osdSnapshotTtl}，默认 5s）跳过本次 Tick，
 * 避免设备断连后还在推空数据。
 *
 * <p>单设备 publish 异常被 try-catch 隔离，不影响其他设备 Tick。
 *
 * @author DJI DevKit Team
 */
public class OsdAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(OsdAggregator.class);

    private final SdkEventBus bus;
    private final Clock clock;
    private final RealSdkProperties props;
    private final ConcurrentHashMap<String, OsdSnapshot> snapshots = new ConcurrentHashMap<>();

    public OsdAggregator(SdkEventBus bus, Clock clock, RealSdkProperties props) {
        this.bus = bus;
        this.clock = clock;
        this.props = props;
    }

    /** Starter 在收到 location callback 时调本方法。 */
    public void updateLocation(String sn, AircraftLocation loc) {
        if (sn == null || loc == null) {
            return;
        }
        snapshots.computeIfAbsent(sn, k -> new OsdSnapshot())
                .updateLocation(loc, clock.instant());
    }

    /** Starter 在收到 battery callback 时调本方法。 */
    public void updateBattery(String sn, AircraftBatteryInfo batt) {
        if (sn == null || batt == null) {
            return;
        }
        snapshots.computeIfAbsent(sn, k -> new OsdSnapshot())
                .updateBattery(batt, clock.instant());
    }

    /** Starter 在收到 attitude callback 时调本方法。 */
    public void updateAttitude(String sn, AircraftAttitude att) {
        if (sn == null || att == null) {
            return;
        }
        snapshots.computeIfAbsent(sn, k -> new OsdSnapshot())
                .updateAttitude(att, clock.instant());
    }

    /**
     * 1Hz Tick：扫描所有 snapshots，对每个未陈旧的 sn 合成发 OsdUpdateEvent。
     */
    @Scheduled(fixedRateString = "${fh.skeleton.sdk.real.osd-interval-millis:1000}")
    public void tick() {
        Instant now = clock.instant();
        Instant deadline = now.minus(props.osdSnapshotTtl());

        for (Map.Entry<String, OsdSnapshot> entry : snapshots.entrySet()) {
            String sn = entry.getKey();
            OsdSnapshot snap = entry.getValue();
            if (snap.lastUpdate().isBefore(deadline)) {
                continue;
            }
            try {
                bus.publish(new OsdUpdateEvent(RealSdkAdapter.SOURCE, sn,
                        new OsdData(snap.toMap(now))));
            } catch (Exception ex) {
                LOG.warn("OSD Tick 单设备 {} 发布失败：{}", sn, ex.getMessage());
                // 不影响其他 sn 的 Tick 发布
            }
        }
    }

    /** 测试 / shutdown 时清空 snapshots。 */
    void clear() {
        snapshots.clear();
    }

    /**
     * 单 sn 的 OSD 子项快照，可独立更新各字段。
     *
     * <p><strong>并发设计</strong>：
     * <ul>
     *   <li>{@code updateXxx} 方法 {@code synchronized}：保证 source 字段（location / battery /
     *       attitude）与 {@code lastUpdate} 时戳两字段的写 ordering——避免 JMM 重排序导致
     *       reader 看到新时戳但 source 还是旧值（cross-field 写一致性）；</li>
     *   <li>字段 {@code volatile}：让高频读路径 {@link #tick()} 无锁读取最近一次完成
     *       {@code synchronized} 写之后 happens-before 的值，避免每 Tick 对每个 sn 加锁
     *       的吞吐损耗；</li>
     *   <li><strong>read 不加锁的代价</strong>：reader 可能看到不同字段对应不同 update 时刻
     *       的值（如读 location 是 t0 写、读 battery 是 t1 写）。OSD 帧不严格要求 cross-source
     *       时刻一致（3 个 source 本来就异步推送），可接受。</li>
     * </ul>
     */
    static final class OsdSnapshot {

        private volatile AircraftLocation location;
        private volatile AircraftBatteryInfo battery;
        private volatile AircraftAttitude attitude;
        private volatile Instant lastUpdate = Instant.EPOCH;

        synchronized void updateLocation(AircraftLocation loc, Instant now) {
            this.location = loc;
            this.lastUpdate = now;
        }

        synchronized void updateBattery(AircraftBatteryInfo batt, Instant now) {
            this.battery = batt;
            this.lastUpdate = now;
        }

        synchronized void updateAttitude(AircraftAttitude att, Instant now) {
            this.attitude = att;
            this.lastUpdate = now;
        }

        Instant lastUpdate() {
            return lastUpdate;
        }

        /**
         * 构造 OsdData 用的 payload Map。缺字段不入 Map（避免 null 值污染）。
         */
        Map<String, Object> toMap(Instant now) {
            Map<String, Object> map = new HashMap<>();
            if (location != null) {
                map.put("lat", location.getLatitude());
                map.put("lng", location.getLongitude());
                map.put("height", location.getHeight());        // 相对起飞点高度
                map.put("elevation", location.getElevation());  // 海拔
            }
            if (battery != null) {
                map.put("battery_percent", battery.getCapacityPercent());
                map.put("battery_return_home_power", battery.getReturnHomePower());
                map.put("battery_landing_power", battery.getLandingPower());
                map.put("battery_remain_flight_time", battery.getRemainFlightTime());
            }
            if (attitude != null) {
                map.put("head", attitude.getAttitudeHead());     // yaw
                map.put("pitch", attitude.getAttitudePitch());
                map.put("roll", attitude.getAttitudeRoll());
            }
            map.put("timestamp", now.toEpochMilli());
            return map;
        }
    }
}
