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

package com.dji.fh.feature.dashboard.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 航线查询 mock 实现。
 *
 * @author DJI DevKit Team
 */
public class MockWaylineQueryService implements WaylineQueryService {

    private static final int MOCK_WAYLINE_COUNT = 128;

    /** mock 航线名模板，与 {@code MockFlightTaskQueryService} 生成的 wl-xxx UUID 对应 */
    private static final String MOCK_WAYLINE_NAME_PREFIX = "演示航线-";

    @Override
    public int countWaylines() {
        return MOCK_WAYLINE_COUNT;
    }

    /**
     * 按 UUID 生成确定性 mock 航线名。
     *
     * <p>UUID 形如 {@code wl-001}，对应名称 {@code 演示航线-001}，
     * 保证编辑页回显链路在 mock 模式下也可验证。
     *
     * @param waylineUuids 航线 UUID 列表
     * @return UUID → mock 航线名
     */
    @Override
    public Map<String, String> getWaylineNames(List<String> waylineUuids) {
        if (waylineUuids == null || waylineUuids.isEmpty()) {
            return Map.of();
        }
        Map<String, String> names = new LinkedHashMap<>();
        for (String uuid : waylineUuids) {
            if (uuid == null || uuid.isEmpty()) {
                continue;
            }
            names.put(uuid, MOCK_WAYLINE_NAME_PREFIX + uuid);
        }
        return names;
    }
}
