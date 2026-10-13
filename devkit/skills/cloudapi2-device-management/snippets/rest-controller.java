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

package com.dji.fh.feature.xxx;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [模块名] REST API。
 *
 * <p>路径规范：{@code /api/v1/feature-xxx/...}
 *
 * @author TODO
 */
@RestController
@RequestMapping("/api/v1/feature-xxx")
public class XxxRestController {

    private final XxxService service;

    public XxxRestController(XxxService service) {
        this.service = service;
    }

    /**
     * 列表查询。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        // TODO: 调用 service 获取数据
        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "success",
            "data", List.of()
        ));
    }

    /**
     * 按 SN 查询详情。
     */
    @GetMapping("/{sn}")
    public ResponseEntity<Map<String, Object>> findBySn(@PathVariable String sn) {
        // TODO: 调用 service.findBySn(sn)
        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "success",
            "data", Map.of("sn", sn)
        ));
    }
}
