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

package com.dji.fh.feature.media;

import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 媒体文件管理 REST API。
 *
 * <ul>
 *   <li>{@code GET /api/v1/media} — 媒体文件列表（分页+过滤）</li>
 *   <li>{@code GET /api/v1/media/{fileId}/download-url} — 获取下载链接</li>
 *   <li>{@code POST /api/v1/media/batch-delete} — 批量删除</li>
 * </ul>
 *
 * <p>安全发布专项修复：媒体文件含飞行影像数据，批量删除为破坏性操作，全部接口强制登录。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/media")
@RequireAuthenticated
public class MediaRestController {

    private final MediaService service;

    public MediaRestController(MediaService service) {
        this.service = service;
    }

    /**
     * 媒体文件列表。
     *
     * @param deviceSn   设备 SN（可选）
     * @param fileType   文件类型，默认 all
     * @param startTime  起始时间（可选）
     * @param endTime    结束时间（可选）
     * @param startIndex 分页游标，默认 0
     * @param pageSize   每页大小，默认 50
     * @return 分页响应
     */
    @GetMapping
    public MediaListResponse listMedia(
            @RequestParam(name = "device_sn", required = false) String deviceSn,
            @RequestParam(name = "file_type", defaultValue = "all") String fileType,
            @RequestParam(name = "start_time", required = false) String startTime,
            @RequestParam(name = "end_time", required = false) String endTime,
            @RequestParam(name = "start_index", defaultValue = "0") int startIndex,
            @RequestParam(name = "page_size", defaultValue = "50") int pageSize) {
        return service.listFiles(deviceSn, fileType, startTime, endTime, startIndex, pageSize);
    }

    /**
     * 获取媒体文件下载 URL。
     *
     * @param fileId 文件 ID
     * @return 下载 URL 响应
     */
    @GetMapping("/{fileId}/download-url")
    public DownloadUrlResponse getDownloadUrl(@PathVariable("fileId") long fileId) {
        return service.getDownloadUrl(fileId);
    }

    /**
     * 批量删除媒体文件。
     *
     * @param request 包含 fileIds 的请求体
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<BatchDeleteResponse> batchDelete(@RequestBody BatchDeleteRequest request) {
        if (request.fileIds() == null || request.fileIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        BatchDeleteResponse resp = service.batchDelete(request.fileIds());
        return ResponseEntity.ok(resp);
    }
}
