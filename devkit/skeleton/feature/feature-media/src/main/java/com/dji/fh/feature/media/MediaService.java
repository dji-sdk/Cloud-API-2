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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 媒体文件管理 service（默认实现）。
 *
 * <p>默认返回空数据。ISV 接入真实 SDK 时由 {@code SdkMediaService} 覆盖。
 *
 * @author DJI DevKit Team
 */
public class MediaService {

    private static final Logger LOG = LoggerFactory.getLogger(MediaService.class);

    private final MediaProperties props;

    /**
     * 构造函数。
     *
     * @param props 媒体配置属性
     */
    public MediaService(MediaProperties props) {
        this.props = props;
    }

    /**
     * 查询媒体文件列表。
     *
     * @param deviceSn   设备 SN（null 表示不过滤）
     * @param fileType   文件类型：photo / video / all
     * @param startTime  起始时间 ISO 8601（null 不限）
     * @param endTime    结束时间 ISO 8601（null 不限）
     * @param startIndex 分页游标
     * @param pageSize   每页大小
     * @return 分页响应
     */
    public MediaListResponse listFiles(String deviceSn, String fileType,
                                       String startTime, String endTime,
                                       int startIndex, int pageSize) {
        int effectivePageSize = clampPageSize(pageSize);
        LOG.debug("媒体列表查询：deviceSn={}, fileType={}, startIndex={}, pageSize={}",
                deviceSn, fileType, startIndex, effectivePageSize);
        return new MediaListResponse(List.of(), 0, startIndex, effectivePageSize, false);
    }

    /**
     * 获取文件下载 URL。
     *
     * @param fileId 文件 ID
     * @return 下载 URL 响应
     */
    public DownloadUrlResponse getDownloadUrl(long fileId) {
        LOG.warn("默认 MediaService 不支持下载，需启用 SDK 模式");
        return new DownloadUrlResponse(fileId, "", 0);
    }

    /**
     * 批量删除媒体文件。
     *
     * @param fileIds 文件 ID 列表
     * @return 删除结果
     */
    public BatchDeleteResponse batchDelete(List<Long> fileIds) {
        LOG.warn("默认 MediaService 不支持删除，需启用 SDK 模式");
        return new BatchDeleteResponse(0);
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
}
