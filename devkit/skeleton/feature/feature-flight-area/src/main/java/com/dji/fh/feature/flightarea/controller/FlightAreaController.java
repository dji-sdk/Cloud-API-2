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

import com.dji.fh.feature.flightarea.model.CreateFlightAreaRequest;
import com.dji.fh.feature.flightarea.model.FlightAreaItem;
import com.dji.fh.feature.flightarea.model.FlightAreaListRequest;
import com.dji.fh.feature.flightarea.model.FlightAreaStatus;
import com.dji.fh.feature.flightarea.model.FlightAreaType;
import com.dji.fh.feature.flightarea.model.UpdateFlightAreaRequest;
import com.dji.fh.feature.flightarea.service.FlightAreaService;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 自定义飞行区域 REST API。
 *
 * <p>安全发布专项修复：飞行禁区管理涉及飞行安全，全部接口强制登录。
 */
@RestController
@RequestMapping("/api/v1/flight-areas")
@RequireAuthenticated
public class FlightAreaController {

    private final FlightAreaService service;

    public FlightAreaController(FlightAreaService service) {
        this.service = service;
    }

    /**
     * 查询飞行区域列表（支持类型/状态/名称筛选 + 分页）。
     *
     * @param type      区域类型筛选
     * @param status    状态筛选
     * @param name      名称关键字
     * @param pageIndex 页码（从 1 开始）
     * @param pageSize  每页条数
     * @return 飞行区域列表
     */
    @GetMapping
    public List<FlightAreaItem> list(
            @RequestParam(required = false) FlightAreaType type,
            @RequestParam(required = false) FlightAreaStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageIndex,
            @RequestParam(required = false) Integer pageSize
    ) {
        return service.list(new FlightAreaListRequest(type, status, name, pageIndex, pageSize));
    }

    /**
     * 创建飞行区域。
     *
     * @param req 创建请求体
     * @return 201 + 创建的飞行区域
     */
    @PostMapping
    public ResponseEntity<FlightAreaItem> create(@RequestBody CreateFlightAreaRequest req) {
        FlightAreaItem created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新飞行区域。
     *
     * @param id  区域 ID
     * @param req 更新请求体
     * @return 200 + 更新后的区域；404 表示不存在
     */
    @PutMapping("/{id}")
    public ResponseEntity<FlightAreaItem> update(
            @PathVariable String id,
            @RequestBody UpdateFlightAreaRequest req
    ) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除飞行区域。
     *
     * @param id 区域 ID
     * @return 204 删除成功；404 不存在
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
