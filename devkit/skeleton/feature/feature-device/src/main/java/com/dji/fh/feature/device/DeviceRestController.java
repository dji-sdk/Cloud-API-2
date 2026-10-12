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

import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备状态 REST API。
 *
 * <ul>
 *   <li>{@code GET /api/v1/devices} — 设备列表（全量，v0.1 不分页）</li>
 *   <li>{@code GET /api/v1/devices/{sn}} — 单设备详情</li>
 * </ul>
 *
 * <p>ISV 可通过声明自家 {@code DeviceRestController} bean 覆盖
 * （{@link DeviceAutoConfiguration} 用 {@code @ConditionalOnMissingBean}）。
 *
 * <p>安全发布专项修复：设备 SN 为敏感资源，全部接口强制登录。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequireAuthenticated
public class DeviceRestController {

    private final DeviceStatusService service;

    public DeviceRestController(DeviceStatusService service) {
        this.service = service;
    }

    /**
     * 设备列表。
     */
    @GetMapping
    public List<DeviceState> listDevices() {
        return service.listAll();
    }

    /**
     * 单设备详情。
     *
     * @param sn 设备 SN
     * @return 200 + DeviceState JSON；或 404 empty body
     */
    @GetMapping("/{sn}")
    public ResponseEntity<DeviceState> getDevice(@PathVariable("sn") String sn) {
        return service.findBySn(sn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
