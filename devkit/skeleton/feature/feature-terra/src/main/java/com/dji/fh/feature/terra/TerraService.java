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
import com.dji.fh.feature.terra.dto.TileStorageInfo;

import java.util.List;

/**
 * 模型管理业务接口 -- 抽象 SDK TerraBuildModule 调用.
 *
 * <p>Mock/Real 双实现通过配置切换.
 *
 * @author DJI DevKit Team
 */
public interface TerraService {

    /**
     * 分页查询模型文件列表.
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param type     重建类型过滤，为 null 时不过滤
     * @return 分页结果
     */
    PageResult<ModelFileDto> listModels(int page, int pageSize, String type);

    /**
     * 获取模型详情（含 3D Tiles URL、坐标和变换矩阵，用于 3D 查看器）.
     *
     * @param fileId 文件 ID
     * @return 模型详情
     */
    ModelDetailDto getModelDetail(long fileId);

    /**
     * 获取模型预签名下载链接.
     *
     * <p>返回 SDK 原始文件（ZIP）的预签名 URL。
     * 原 SDK {@code getModelFileUrl} 返回无效 key，改为从 {@code getModelDetail().url} 获取。
     *
     * @param fileId 文件 ID
     * @return 预签名 URL 及过期时间
     */
    ModelUrlDto getModelUrl(long fileId);

    /**
     * 获取瓦片存储信息（供瓦片代理端点使用）.
     *
     * @param fileId 文件 ID
     * @return 存储基础 URL 和文件前缀
     */
    TileStorageInfo getTileStorageInfo(long fileId);

    /**
     * 删除单个模型文件.
     *
     * @param fileId 文件 ID
     */
    void deleteModel(long fileId);

    /**
     * 批量删除模型文件.
     *
     * @param fileIds 待删除的文件 ID 列表
     */
    void batchDeleteModels(List<Long> fileIds);

    /**
     * 创建重建任务.
     *
     * @param request 创建请求
     * @return 重建任务信息
     */
    ReconstructionTaskDto createReconstruction(
            CreateReconstructionRequest request);

    /**
     * 创建全景任务.
     *
     * @param request 创建请求
     * @return 重建任务信息
     */
    ReconstructionTaskDto createPanorama(CreatePanoramaRequest request);

    /**
     * 查询重建任务状态.
     *
     * @param taskId 任务 ID
     * @return 任务状态
     */
    TaskStatusDto getTaskStatus(String taskId);

    /**
     * 重试失败的重建任务.
     *
     * @param taskId 任务 ID
     */
    void retryTask(String taskId);

    /**
     * 停止进行中的重建任务.
     *
     * @param taskId 任务 ID
     */
    void stopTask(String taskId);

    /**
     * 获取质量报告.
     *
     * @param taskId   任务 ID
     * @param language 语言代码
     * @return 质量报告
     */
    QualityReportDto getQualityReport(String taskId, String language);

    /**
     * 获取 SfM 空三结果.
     *
     * @param taskId 任务 ID
     * @return SfM 结果
     */
    SfmResultDto getSfmResult(String taskId);

    /**
     * 获取重建使用的原始影像列表.
     *
     * @param taskId 任务 ID
     * @return 影像列表
     */
    List<ImageDto> getReconstructionImages(String taskId);

    /**
     * 列出重建任务（按创建时间倒序）.
     *
     * <p>用于任务页初始加载。SDK 无独立的"任务列表"概念，任务即模型文件，
     * 故复用 {@link #listModels} 查询并映射为任务状态视图。SSE 推送负责增量更新。
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页大小
     * @return 任务状态分页结果
     */
    PageResult<TaskStatusDto> listTasks(int page, int pageSize);

    /**
     * 列出可用于重建的媒体文件夹.
     *
     * <p>通过遍历媒体文件（{@code getMediaFilesList}）按 {@code filePath}（文件夹 id）聚合，
     * 再经 {@code getMediaFileDetailById} 获取名称。folderUuid 无法从媒体层获取，可为 null。
     *
     * @return 文件夹列表（按 folderName 排序），无数据时返回空列表
     */
    List<FolderDto> listFolders();

    // ── 媒体代理 raw URL 获取（供 MediaProxyController 在缓存 miss 时 fallback） ──

    /**
     * 获取模型缩略图的原始 URL（内网 MinIO 预签名）.
     *
     * @param fileId 文件 ID
     * @return 原始缩略图 URL；不可用时返回 null
     */
    String getModelThumbnailUrl(long fileId);

    /**
     * 获取模型预览图的原始 URL（内网 MinIO 预签名）.
     *
     * @param fileId 文件 ID
     * @return 原始预览图 URL；不可用时返回 null
     */
    String getModelPreviewUrl(long fileId);

    /**
     * 获取重建影像的原始 URL（内网 MinIO 预签名）.
     *
     * @param taskId  任务 ID
     * @param imageId 影像 ID
     * @return 原始影像 URL；不可用时返回 null
     */
    String getReconstructionImageUrl(String taskId, String imageId);

    /**
     * 获取质量报告文件的原始 URL.
     *
     * @param taskId 任务 ID
     * @return 原始报告 URL；不可用时返回 null
     */
    String getQualityReportFileUrl(String taskId);

    /**
     * 分页结果包装.
     *
     * @param list     当前页数据
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      元素类型
     */
    record PageResult<T>(List<T> list, long total, int page, int pageSize) { }
}
