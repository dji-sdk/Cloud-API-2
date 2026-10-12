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

import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 航线库管理 REST API。
 *
 * <ul>
 *   <li>{@code GET /api/v1/workspace/waylines} — 航线列表（分页 + 机型/负载过滤）</li>
 *   <li>{@code POST /api/v1/workspace/waylines/batch_delete} — 批量删除航线</li>
 *   <li>{@code POST /api/v1/workspace/waylines/batch_download} — 批量导出航线（返回下载 URL）</li>
 *   <li>{@code POST /api/v1/workspace/waylines/submit} — 导入航线（上传 KMZ）</li>
 * </ul>
 *
 * <p>对齐前端 {@code web/src/api/wayline.ts} 的请求契约：
 * query 参数 {@code page / page_size / drone_type / payload_type / keyword}，
 * 响应体 {@code waylines / total / page / pageSize}。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/workspace/waylines")
@RequireAuthenticated
public class WaylineRestController {

    private static final Logger LOG = LoggerFactory.getLogger(WaylineRestController.class);

    private final WaylineService service;

    public WaylineRestController(WaylineService service) {
        this.service = service;
    }

    /**
     * 航线列表。
     *
     * @param page        页码，默认 1
     * @param pageSize    每页大小，默认 20
     * @param droneType   机型过滤（可选）
     * @param payloadType 负载过滤（可选）
     * @param keyword     名称关键字（可选，SDK 列表接口不支持，仅透传到 service）
     * @return 分页响应
     */
    @GetMapping
    public ResponseEntity<?> listWaylines(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
            @RequestParam(name = "drone_type", required = false) String droneType,
            @RequestParam(name = "payload_type", required = false) String payloadType,
            @RequestParam(name = "keyword", required = false) String keyword) {
        try {
            return ResponseEntity.ok(service.listWaylines(page, pageSize, droneType, payloadType, keyword));
        } catch (RuntimeException e) {
            LOG.error("查询航线列表失败", e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 获取航线关联的飞行执行列表。
     *
     * <p>用于重建面板展开航线时加载关联的飞行执行（媒体文件夹）。
     *
     * @param uuid 航线 UUID
     * @return 飞行执行列表
     */
    @GetMapping("/{uuid}/executions")
    public WaylineExecutionsResponse listExecutions(@PathVariable("uuid") String uuid) {
        return service.listExecutions(uuid);
    }

    /**
     * 检查航线是否存在。
     *
     * <p>用于飞行回放页预判航线是否已被删除，避免 FlyDataReplay SDK 因航线缺失整页崩溃。
     * 查询失败时降级返回 {@code exists=true}，不阻断回放加载流程。
     *
     * @param uuid 航线 UUID
     * @return {@code {"exists": true/false}}
     */
    @GetMapping("/{uuid}/exists")
    public ResponseEntity<Map<String, Boolean>> waylineExists(@PathVariable("uuid") String uuid) {
        try {
            boolean exists = service.waylineExists(uuid);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (RuntimeException e) {
            LOG.error("检查航线存在性失败（降级返回 true）：uuid={}", uuid, e);
            return ResponseEntity.ok(Map.of("exists", true));
        }
    }

    /**
     * 批量删除航线。
     *
     * <p>对齐前端 {@code web/src/api/wayline.ts} 的请求契约：
     * {@code POST /batch_delete}，body 为 {@code { "wayline_uuids": [...] }}。
     *
     * @param request 包含待删除航线 UUID 列表的请求体
     * @return 200 OK（成功）/ 400 Bad Request（UUID 列表为空）
     */
    @PostMapping("/batch_delete")
    public ResponseEntity<?> batchDelete(@RequestBody BatchDeleteWaylinesRequest request) {
        List<String> uuids = filterBlank(request.waylineUuids());
        if (uuids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            service.batchDeleteWaylines(uuids);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            LOG.error("批量删除航线失败：uuids={}", uuids, e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 批量导出航线（返回下载 URL 列表）。
     *
     * <p>对齐前端 {@code web/src/api/wayline.ts} 的请求契约：
     * {@code POST /batch_download}，body 为 {@code { "wayline_uuids": [...] }}。
     *
     * @param request 包含待下载航线 UUID 列表的请求体
     * @return 200 OK + 下载 URL 列表 / 400 Bad Request
     */
    @PostMapping("/batch_download")
    public ResponseEntity<?> batchDownload(
            @RequestBody BatchDownloadWaylinesRequest request) {
        List<String> uuids = filterBlank(request.waylineUuids());
        if (uuids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            BatchDownloadWaylinesResponse resp = service.batchDownloadWaylines(uuids);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            LOG.error("批量导出航线失败：uuids={}", uuids, e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 导入航线（上传 KMZ 文件）。
     *
     * <p>对齐前端 {@code web/src/api/wayline.ts} 的请求契约：
     * {@code POST /submit}，multipart/form-data 包含 {@code file} 和 {@code wayline_name}。
     *
     * @param file        KMZ 文件
     * @param waylineName 航线名称
     * @return 200 OK + {@code { "wayline_uuid": "..." }} / 400 Bad Request
     */
    @PostMapping("/submit")
    public ResponseEntity<?> importWayline(
            @RequestPart("file") MultipartFile file,
            @RequestParam("wayline_name") String waylineName) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ImportWaylineResponse resp = service.importWayline(file, waylineName);
            return ResponseEntity.ok(resp);
        } catch (UnsupportedOperationException e) {
            LOG.warn("航线导入暂不支持：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            LOG.error("导入航线失败：waylineName={}", waylineName, e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 过滤列表中的 null 和空白元素。
     *
     * @param uuids 原始 UUID 列表（可能为 null）
     * @return 过滤后的列表（不含 null/空白元素）
     */
    private List<String> filterBlank(List<String> uuids) {
        if (uuids == null) {
            return List.of();
        }
        return uuids.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();
    }

    private ResponseEntity<Map<String, String>> serverError(String message) {
        String safeMessage = message != null ? message : "未知错误";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", safeMessage));
    }
}
