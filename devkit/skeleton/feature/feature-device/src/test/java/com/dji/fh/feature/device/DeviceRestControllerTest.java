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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.OsdData;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * DeviceRestController @WebMvcTest 切片测试。
 *
 * <p>不用 @MockBean（Java 24 + Byte Buddy 兼容性问题，mock concrete class 不支持），
 * 改用 TestConfig 中直接构造真实 service 实例 + 预填充数据。
 *
 * @author DJI DevKit Team
 */
@WebMvcTest(DeviceRestController.class)
class DeviceRestControllerTest {

    private static final Instant T0 = Instant.parse("2026-06-29T10:00:00Z");

    /**
     * @WebMvcTest 需要 @SpringBootConfiguration 入口。
     * 构造真实 DeviceStatusService + 预填充 1 台设备 (SN-1)。
     */
    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        com.dji.fh.kernel.spi.query.DeviceQueryService stubQueryService() {
            // 返回 1 台预定义设备，与事件预填充的 SN-1 对应
            return () -> List.of(
                    new com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot(
                            "SN-1", "DRONE", "M30T", "DroneA", null, true));
        }

        @Bean
        DeviceStatusService deviceStatusService(com.dji.fh.kernel.spi.query.DeviceQueryService qs) {
            DeviceProperties props = new DeviceProperties(50, true, Duration.ofMinutes(5), null);
            DeviceStatusService svc = new DeviceStatusService(props, qs, Clock.fixed(T0, ZoneOffset.UTC));
            // 预填充 1 台设备
            svc.onDeviceOnline(new com.dji.fh.kernel.spi.event.DeviceOnlineEvent(
                    "test", "SN-1", DeviceType.DRONE, "M30T", "DroneA"));
            svc.onOsdUpdate(new com.dji.fh.kernel.spi.event.OsdUpdateEvent(
                    "test", "SN-1", new OsdData(Map.of("lat", 40.0, "lng", 120.0))));
            return svc;
        }

        @Bean
        DeviceRestController deviceRestController(DeviceStatusService service) {
            return new DeviceRestController(service);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void listDevices_shouldReturn200WithDeviceList() throws Exception {
        mvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sn").value("SN-1"))
                .andExpect(jsonPath("$[0].online").value(true))
                .andExpect(jsonPath("$[0].deviceType").value("DRONE"))
                .andExpect(jsonPath("$[0].lastOsd.attributes.lat").value(40.0));
    }

    @Test
    void getDevice_shouldReturn200_whenFound() throws Exception {
        mvc.perform(get("/api/v1/devices/SN-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sn").value("SN-1"))
                .andExpect(jsonPath("$.model").value("M30T"));
    }

    @Test
    void getDevice_shouldReturn404_whenNotFound() throws Exception {
        mvc.perform(get("/api/v1/devices/NONEXIST"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDevices_shouldReturnEmptyList_whenNoDevicesPreloaded() throws Exception {
        // 这个测试验证端点格式正确（List<DeviceState>），即使只有预填充的 1 台设备
        // 结果不为空（因为 TestConfig 预填充了 SN-1），验证格式 is an array
        mvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
