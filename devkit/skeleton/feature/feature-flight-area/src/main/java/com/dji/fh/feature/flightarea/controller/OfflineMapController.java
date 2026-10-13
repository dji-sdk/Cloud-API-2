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

import com.dji.fh.feature.flightarea.model.OfflineMapModelInfo;
import com.dji.fh.feature.flightarea.model.OfflineMapUpdateState;
import com.dji.fh.feature.flightarea.service.OfflineMapService;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 地形障碍物（离线地图）REST API。
 *
 * <p>API 设计对标 DJI 上云API 2 TerrainObstacle 模块，支持：
 * <ul>
 *   <li>查询模型信息</li>
 *   <li>开关功能</li>
 *   <li>触发异步数据更新</li>
 *   <li>轮询更新进度</li>
 *   <li>取消更新</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/offline-maps")
@RequireAuthenticated
public class OfflineMapController {

    private final OfflineMapService service;

    public OfflineMapController(OfflineMapService service) {
        this.service = service;
    }

    /**
     * 查询地形模型信息。
     */
    @GetMapping("/model-info")
    public OfflineMapModelInfo getModelInfo() {
        return service.getModelInfo();
    }

    /**
     * 设置地形障碍物功能开关。
     */
    @PutMapping("/enabled")
    public ResponseEntity<Void> setEnabled(@RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }
        service.setEnabled(enabled);
        return ResponseEntity.ok().build();
    }

    /**
     * 触发地形数据更新（异步）。
     *
     * @return 200=触发成功，409=已有更新在进行中，422=无可用模型数据
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> triggerUpdate() {
        try {
            boolean success = service.triggerUpdate();
            if (!success) {
                return ResponseEntity.status(409).body(Map.of("error", "更新正在进行中"));
            }
            return ResponseEntity.ok(Map.of("message", "更新已触发"));
        } catch (RuntimeException e) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 查询更新进度（前端轮询接口）。
     *
     * @return 当前更新状态、进度百分比、描述信息
     */
    @GetMapping("/update-status")
    public OfflineMapUpdateState getUpdateStatus() {
        return service.getUpdateState();
    }

    /**
     * 取消数据更新。
     *
     * @return 200=取消成功，409=当前无进行中的更新
     */
    @DeleteMapping("/update")
    public ResponseEntity<Void> cancelUpdate() {
        boolean success = service.cancelUpdate();
        if (!success) {
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 重置更新状态为 IDLE（前端确认终态后调用）。
     */
    @PostMapping("/update-reset")
    public ResponseEntity<Void> resetUpdateState() {
        service.resetUpdateState();
        return ResponseEntity.ok().build();
    }
}
