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

package com.dji.fh.feature.dashboard;

import com.dji.fh.feature.dashboard.dto.DashboardStatsDto;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 看板数据 REST API。
 *
 * <p>安全发布专项修复：看板聚合统计含业务运营数据，强制登录。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequireAuthenticated
public class DashboardRestController {

    private final DashboardService service;

    public DashboardRestController(DashboardService service) {
        this.service = service;
    }

    /**
     * 获取看板聚合统计数据。
     *
     * @param range 时间范围（week / recent_month / half_year / year），默认 month
     * @return 聚合统计 DTO
     */
    @GetMapping("/stats")
    public DashboardStatsDto getStats(@RequestParam(defaultValue = "month") String range) {
        return service.getStats(range);
    }
}
