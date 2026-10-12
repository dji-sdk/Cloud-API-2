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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 航线查询 SPI，供看板聚合使用。
 *
 * <p>默认提供 mock 实现；ISV 接入真实 SDK 时通过声明自有 bean 覆盖。
 *
 * @author DJI DevKit Team
 */
public interface WaylineQueryService {

    /**
     * 查询工作空间航线总数。
     *
     * @return 航线数量
     */
    int countWaylines();

    /**
     * 按 UUID 批量查询航线名称。
     *
     * <p>供任务详情回显使用：SDK 的任务详情接口只返回 wayline_uuid，不含航线名，
     * 需反查补齐。以 default 方法提供空实现，ISV 已有的自定义实现无需改动即可编译通过；
     * 未覆盖时返回空 Map，调用方需容忍航线名缺失。
     *
     * @param waylineUuids 航线 UUID 列表，允许为空
     * @return UUID → 航线名称；查不到的 UUID 不会出现在结果中
     */
    default Map<String, String> getWaylineNames(List<String> waylineUuids) {
        return Collections.emptyMap();
    }
}
