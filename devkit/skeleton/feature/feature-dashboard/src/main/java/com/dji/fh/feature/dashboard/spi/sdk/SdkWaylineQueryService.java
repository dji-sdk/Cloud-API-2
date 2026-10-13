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

package com.dji.fh.feature.dashboard.spi.sdk;

import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.core.WaylineManageModule;
import com.dji.flighthubsdk.core.Workspace;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 航线查询真实 SDK 实现。
 *
 * <p>通过 {@code WaylineManageModule.getWaylineList()} 查询航线：
 * <ul>
 *   <li>{@code countWaylines()} 取第一页（pageSize=1），用分页 total 拿总数</li>
 *   <li>{@code getWaylineNames()} 用 {@code setWaylineUuids} 精确过滤，取回航线名</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public class SdkWaylineQueryService implements WaylineQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkWaylineQueryService.class);

    /** 总数查询只需分页信息，取最小页大小 */
    private static final int COUNT_PAGE_SIZE = 1;

    @Override
    public int countWaylines() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            LOG.warn("Workspace 不可用，SDK 未初始化");
            return 0;
        }

        WaylineManageModule wmm = ws.getWaylineManageModule();
        if (wmm == null) {
            LOG.warn("WaylineManageModule 不可用");
            return 0;
        }

        GetWaylineListParam param = new GetWaylineListParam();
        param.setPage(1);
        param.setPageSize(COUNT_PAGE_SIZE);

        CompletableFuture<Integer> future = new CompletableFuture<>();
        wmm.getWaylineList(param, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getWaylineList 失败: " + error.getServerMessage()));
                return;
            }
            int total = (result != null && result.getPagination() != null)
                    ? result.getPagination().getTotal() : 0;
            future.complete(total);
        });

        int total = SdkBridge.await(future);
        LOG.info("SDK 航线总数查询完成：total={}", total);
        return total;
    }

    /**
     * 按 UUID 精确查航线名。
     *
     * <p>任务详情回显依赖此结果，但航线名缺失不应导致整个详情接口失败，
     * 因此这里对 SDK 异常做降级处理：仅记录日志并返回已查到的部分。
     *
     * @param waylineUuids 航线 UUID 列表
     * @return UUID → 航线名称，查不到或被降级时可能缺少部分键
     */
    @Override
    public Map<String, String> getWaylineNames(List<String> waylineUuids) {
        if (waylineUuids == null || waylineUuids.isEmpty()) {
            return Collections.emptyMap();
        }

        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            LOG.warn("Workspace 不可用，SDK 未初始化，跳过航线名查询");
            return Collections.emptyMap();
        }

        WaylineManageModule wmm = ws.getWaylineManageModule();
        if (wmm == null) {
            LOG.warn("WaylineManageModule 不可用，跳过航线名查询");
            return Collections.emptyMap();
        }

        GetWaylineListParam param = new GetWaylineListParam();
        param.setPage(1);
        param.setPageSize(waylineUuids.size());
        param.setWaylineUuids(waylineUuids);

        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
        wmm.getWaylineList(param, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getWaylineList 失败: " + error.getServerMessage()));
                return;
            }
            future.complete(toNameMap(result));
        });

        try {
            Map<String, String> names = SdkBridge.await(future);
            LOG.info("SDK 航线名查询完成：请求 {} 条，命中 {} 条", waylineUuids.size(), names.size());
            return names;
        } catch (SdkBridge.SdkQueryException e) {
            LOG.warn("航线名查询失败，详情回显将缺失航线名：{}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 将 SDK 返回的航线列表转为 UUID → 名称映射。
     *
     * @param result SDK 返回结果，允许为 null
     * @return 航线名映射，UUID 或名称为空时跳过该条
     */
    private Map<String, String> toNameMap(GetWaylineListResult result) {
        if (result == null || result.getItems() == null) {
            return Collections.emptyMap();
        }
        Map<String, String> names = new LinkedHashMap<>();
        for (WaylineListItemResult item : result.getItems()) {
            if (item == null || item.getWaylineUuid() == null) {
                continue;
            }
            names.put(item.getWaylineUuid(), item.getWaylineName());
        }
        return names;
    }
}
