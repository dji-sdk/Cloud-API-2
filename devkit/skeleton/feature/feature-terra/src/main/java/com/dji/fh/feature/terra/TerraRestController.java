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

package com.dji.fh.feature.terra;

import com.dji.fh.feature.terra.dto.BatchDeleteRequest;
import com.dji.fh.feature.terra.dto.CreatePanoramaRequest;
import com.dji.fh.feature.terra.dto.CreateReconstructionRequest;
import com.dji.fh.feature.terra.dto.FolderDto;
import com.dji.fh.feature.terra.dto.ImageDto;
import com.dji.fh.feature.terra.dto.ModelDetailDto;
import com.dji.fh.feature.terra.dto.ModelFileDto;
import com.dji.fh.feature.terra.dto.ModelUrlDto;
import com.dji.fh.feature.terra.dto.QualityReportDto;
import com.dji.fh.feature.terra.dto.ReconstructionTaskDto;
import com.dji.fh.feature.terra.dto.SfmResultDto;
import com.dji.fh.feature.terra.dto.TaskStatusDto;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Terra 模型管理 REST API.
 *
 * <p>提供模型浏览、删除、重建任务管理、质量报告等 13 个端点.
 *
 * <p>安全发布专项修复：三维重建模型删除/任务停止为破坏性操作，全部接口强制登录。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/terra")
@RequireAuthenticated
public class TerraRestController {

    /** 默认页码. */
    private static final int DEFAULT_PAGE = 1;

    /** 默认每页大小. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 每页最大大小. */
    private static final int MAX_PAGE_SIZE = 100;

    /** 业务服务. */
    private final TerraService terraService;

    /**
     * 构造 TerraRestController.
     *
     * @param service 业务服务实例
     */
    public TerraRestController(final TerraService service) {
        this.terraService = service;
    }

    // -- 模型浏览 --

    /**
     * 列出可用于重建的媒体文件夹.
     *
     * <p>聚合已有模型的重建设置中的 folderList，按 folderUuid 去重。
     * 平台无独立的文件夹列表端点，故从模型数据中间接获取。
     *
     * @return 文件夹列表
     */
    @GetMapping("/folders")
    public List<FolderDto> listFolders() {
        return terraService.listFolders();
    }

    /**
     * 分页查询模型文件列表.
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页大小，上限 100
     * @param type     可选重建类型过滤
     * @return 分页结果
     */
    @GetMapping("/models")
    public TerraService.PageResult<ModelFileDto> listModels(
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "20") final int pageSize,
            @RequestParam(required = false) final String type) {
        int clampedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        int clampedPage = Math.max(page, DEFAULT_PAGE);
        return terraService.listModels(clampedPage, clampedPageSize, type);
    }

    /**
     * 获取模型预签名下载链接.
     *
     * <p>返回 SDK 原始文件（ZIP）的预签名 URL。
     *
     * @param fileId 文件 ID
     * @return 预签名 URL 及过期时间
     */
    @GetMapping("/models/{fileId}/url")
    public ModelUrlDto getModelUrl(@PathVariable final long fileId) {
        return terraService.getModelUrl(fileId);
    }

    /**
     * 获取模型详情（含 3D Tiles URL、坐标和变换矩阵，用于 3D 查看器）.
     *
     * @param fileId 文件 ID
     * @return 模型详情
     */
    @GetMapping("/models/{fileId}/detail")
    public ModelDetailDto getModelDetail(@PathVariable final long fileId) {
        return terraService.getModelDetail(fileId);
    }

    // -- 模型删除 --

    /**
     * 删除单个模型文件.
     *
     * @param fileId 文件 ID
     */
    @DeleteMapping("/models/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(@PathVariable final long fileId) {
        terraService.deleteModel(fileId);
    }

    /**
     * 批量删除模型文件.
     *
     * @param request 包含待删除文件 ID 列表的请求体
     */
    @DeleteMapping("/models/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void batchDeleteModels(
            @RequestBody final BatchDeleteRequest request) {
        if (request.fileIds() == null || request.fileIds().isEmpty()) {
            return;
        }
        terraService.batchDeleteModels(request.fileIds());
    }

    // -- 重建任务 --

    /**
     * 创建重建任务（正射/三维网格/点云）.
     *
     * @param request 创建请求
     * @return 新建任务信息
     */
    @PostMapping("/tasks/reconstruction")
    public ResponseEntity<ReconstructionTaskDto> createReconstruction(
            @RequestBody
            final CreateReconstructionRequest request) {
        ReconstructionTaskDto result = terraService.createReconstruction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 创建全景拼接任务.
     *
     * @param request 创建请求
     * @return 新建任务信息
     */
    @PostMapping("/tasks/panorama")
    public ResponseEntity<ReconstructionTaskDto> createPanorama(
            @RequestBody final CreatePanoramaRequest request) {
        ReconstructionTaskDto result = terraService.createPanorama(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 查询重建任务状态.
     *
     * @param taskId 任务 ID
     * @return 任务状态
     */
    @GetMapping("/tasks/{taskId}/status")
    public TaskStatusDto getTaskStatus(@PathVariable final String taskId) {
        return terraService.getTaskStatus(taskId);
    }

    /**
     * 分页查询重建任务列表.
     *
     * <p>用于任务页初始加载，SSE 负责后续增量更新。
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页大小
     * @return 任务状态分页结果
     */
    @GetMapping("/tasks")
    public TerraService.PageResult<TaskStatusDto> listTasks(
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "20") final int pageSize) {
        int clampedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        int clampedPage = Math.max(page, DEFAULT_PAGE);
        return terraService.listTasks(clampedPage, clampedPageSize);
    }

    /**
     * 重试失败的重建任务.
     *
     * @param taskId 任务 ID
     * @return 空响应
     */
    @PostMapping("/tasks/{taskId}/retry")
    public ResponseEntity<Void> retryTask(@PathVariable final String taskId) {
        terraService.retryTask(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 停止进行中的重建任务.
     *
     * @param taskId 任务 ID
     * @return 空响应
     */
    @PostMapping("/tasks/{taskId}/stop")
    public ResponseEntity<Void> stopTask(@PathVariable final String taskId) {
        terraService.stopTask(taskId);
        return ResponseEntity.ok().build();
    }

    // -- 报告 & 影像 --

    /**
     * 获取质量报告.
     *
     * @param taskId   任务 ID
     * @param language 语言代码，默认 zh
     * @return 质量报告
     */
    @GetMapping("/tasks/{taskId}/report")
    public QualityReportDto getQualityReport(
            @PathVariable final String taskId,
            @RequestParam(defaultValue = "zh") final String language) {
        return terraService.getQualityReport(taskId, language);
    }

    /**
     * 获取 SfM 空三结果.
     *
     * @param taskId 任务 ID
     * @return SfM 结果
     */
    @GetMapping("/tasks/{taskId}/sfm")
    public SfmResultDto getSfmResult(@PathVariable final String taskId) {
        return terraService.getSfmResult(taskId);
    }

    /**
     * 获取重建使用的原始影像列表.
     *
     * @param taskId 任务 ID
     * @return 影像列表
     */
    @GetMapping("/tasks/{taskId}/images")
    public List<ImageDto> getReconstructionImages(
            @PathVariable final String taskId) {
        return terraService.getReconstructionImages(taskId);
    }
}
