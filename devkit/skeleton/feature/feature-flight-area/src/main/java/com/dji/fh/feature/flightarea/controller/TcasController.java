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

package com.dji.fh.feature.flightarea.controller;

import com.dji.fh.feature.flightarea.model.TcasConfig;
import com.dji.fh.feature.flightarea.service.TcasService;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TCAS 空域告警配置 REST API。
 *
 * <p>安全发布专项修复：TCAS 控制无人机防撞逻辑，参数被篡改可导致碰撞事故，全部接口强制登录。
 */
@RestController
@RequestMapping("/api/v1/tcas")
@RequireAuthenticated
public class TcasController {

    private final TcasService service;

    public TcasController(TcasService service) {
        this.service = service;
    }

    @GetMapping
    public TcasConfig getConfig() {
        return service.getConfig();
    }

    @PutMapping
    public ResponseEntity<Void> updateConfig(@RequestBody TcasConfig config) {
        service.updateConfig(config);
        return ResponseEntity.ok().build();
    }
}
