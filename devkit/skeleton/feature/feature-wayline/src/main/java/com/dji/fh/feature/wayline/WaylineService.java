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

package com.dji.fh.feature.wayline;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * 航线库管理 service（默认空实现）。
 *
 * <p>默认返回空列表。ISV 接入真实 SDK 时由 {@code SdkWaylineService} 覆盖
 * （{@link WaylineAutoConfiguration} 用 {@code @ConditionalOnMissingBean}）。
 *
 * @author DJI DevKit Team
 */
public class WaylineService {

    private static final Logger LOG = LoggerFactory.getLogger(WaylineService.class);

    private final WaylineProperties props;

    /**
     * 构造函数。
     *
     * @param props 航线配置属性
     */
    public WaylineService(WaylineProperties props) {
        this.props = props;
    }

    /**
     * 查询航线列表（默认返回空）。
     *
     * @param page        页码（从 1 开始）
     * @param pageSize    每页大小
     * @param droneType   机型过滤（null 表示不过滤）
     * @param payloadType 负载过滤（null 表示不过滤）
     * @param keyword     名称关键字过滤（null 表示不过滤，默认实现忽略）
     * @return 分页响应
     */
    public WaylineListResponse listWaylines(int page, int pageSize,
                                            String droneType, String payloadType,
                                            String keyword) {
        int effectivePage = Math.max(page, 1);
        int effectivePageSize = clampPageSize(pageSize);
        LOG.debug("航线列表查询（默认空实现）：page={}, pageSize={}, droneType={}, payloadType={}, keyword={}",
                effectivePage, effectivePageSize, droneType, payloadType, keyword);
        return new WaylineListResponse(List.of(), 0, effectivePage, effectivePageSize);
    }

    /**
     * 查询航线关联的飞行执行列表（默认返回 mock 数据）。
     *
     * <p>ISV 接入真实 SDK 时由 {@code SdkWaylineService} 覆盖，
     * 通过飞行任务和媒体模块查询实际执行记录。
     *
     * @param waylineUuid 航线 UUID
     * @return 该航线关联的飞行执行列表
     */
    public WaylineExecutionsResponse listExecutions(String waylineUuid) {
        LOG.debug("航线执行列表查询（默认 mock 实现）：waylineUuid={}", waylineUuid);
        List<WaylineExecutionInfo> executions = MOCK_EXECUTIONS.getOrDefault(waylineUuid, List.of());
        String waylineName = MOCK_WAYLINE_NAMES.getOrDefault(waylineUuid, "未知航线");
        return new WaylineExecutionsResponse(waylineUuid, waylineName, executions);
    }

    /**
     * 批量删除航线（默认空实现）。
     *
     * <p>ISV 接入真实 SDK 时由 {@code SdkWaylineService} 覆盖。
     *
     * @param uuids 待删除的航线 UUID 列表
     */
    public void batchDeleteWaylines(List<String> uuids) {
        LOG.warn("航线批量删除（默认空实现，未接入 SDK）：uuids={}", uuids);
    }

    /**
     * 批量下载航线（默认空实现）。
     *
     * <p>ISV 接入真实 SDK 时由 {@code SdkWaylineService} 覆盖。
     *
     * @param uuids 待下载的航线 UUID 列表
     * @return 空的下载响应
     */
    public BatchDownloadWaylinesResponse batchDownloadWaylines(List<String> uuids) {
        LOG.warn("航线批量下载（默认空实现，未接入 SDK）：uuids={}", uuids);
        return new BatchDownloadWaylinesResponse(List.of());
    }

    /**
     * 检查指定 UUID 的航线是否存在（默认返回 true）。
     *
     * <p>ISV 接入真实 SDK 时由 {@code SdkWaylineService} 覆盖，
     * 通过查询航线列表匹配 UUID 判断航线是否仍存在。
     *
     * @param uuid 航线 UUID
     * @return 航线存在返回 true，否则 false
     */
    public boolean waylineExists(String uuid) {
        LOG.debug("航线存在性检查（默认实现，始终返回 true）：uuid={}", uuid);
        return true;
    }

    /**
     * 导入航线（默认空实现）。
     *
     * <p>ISV 接入真实 SDK 时由 {@code SdkWaylineService} 覆盖。
     *
     * @param file        KMZ 文件
     * @param waylineName 航线名称
     * @return 包含生成的 UUID 的响应
     */
    public ImportWaylineResponse importWayline(MultipartFile file, String waylineName) {
        LOG.warn("航线导入（默认空实现，未接入 SDK）：fileName={}, waylineName={}",
                file.getOriginalFilename(), waylineName);
        return new ImportWaylineResponse("mock-" + System.currentTimeMillis());
    }

    /**
     * 将 pageSize 限制在合法范围内。
     *
     * @param pageSize 请求的页大小
     * @return 有效的页大小
     */
    protected int clampPageSize(int pageSize) {
        if (pageSize <= 0) {
            return props.defaultPageSize();
        }
        return Math.min(pageSize, props.maxPageSize());
    }

    /** Mock 航线名称映射 */
    private static final Map<String, String> MOCK_WAYLINE_NAMES = Map.of(
            "wl-001", "新建面状航线(50)",
            "wl-002", "25号机场-面状航线-执行日志",
            "wl-003", "corvin面状航线-M30",
            "wl-004", "新建面状航线(110)",
            "wl-005", "corvin几何体航线-M3D",
            "wl-006", "corvi斜面航线-M3D-1",
            "wl-007", "corvin斜面航线-M3D",
            "wl-008", "corvin面状航线-M3D"
    );

    /** Mock 执行数据 */
    @SuppressWarnings("all")
    private static final Map<String, List<WaylineExecutionInfo>> MOCK_EXECUTIONS = Map.of(
            "wl-001", List.of(
                    new WaylineExecutionInfo("ex-001-1", "面状航线执行 2026-04-17 19:00", 50, 1776423615L, null)
            ),
            "wl-002", List.of(
                    new WaylineExecutionInfo("ex-002-1", "面状航线执行 2026-03-02 21:43", 120, 1772458980L, null)
            ),
            "wl-003", List.of(
                    new WaylineExecutionInfo("ex-003-1", "corvin面状航线执行 2026-01-09 09:15", 85, 1767921300L, null),
                    new WaylineExecutionInfo("ex-003-2", "corvin面状航线执行 2026-01-09 11:02", 92, 1767927774L, null)
            ),
            "wl-004", List.of(
                    new WaylineExecutionInfo("ex-004-1", "面状航线执行 2026-01-07 18:19", 110, 1767781156L, null)
            ),
            "wl-005", List.of(
                    new WaylineExecutionInfo("ex-005-1", "几何体航线执行 2026-01-07 12:17", 200, 1767759425L, null)
            ),
            "wl-006", List.of(
                    new WaylineExecutionInfo("ex-006-1", "斜面航线执行 2026-01-07 12:07", 156, 1767758879L, null),
                    new WaylineExecutionInfo("ex-006-2", "斜面航线执行 2026-01-09 14:30", 163, 1767940200L, null)
            ),
            "wl-007", List.of(
                    new WaylineExecutionInfo("ex-007-1", "斜面航线执行 2026-01-07 12:04", 178, 1767758658L, null)
            ),
            "wl-008", List.of(
                    new WaylineExecutionInfo("ex-008-1", "面状航线执行 2026-01-07 11:58", 95, 1767758297L, null)
            )
    );
}
