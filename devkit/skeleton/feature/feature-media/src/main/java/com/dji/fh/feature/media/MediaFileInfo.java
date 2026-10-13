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

/**
 * 单个媒体文件信息 DTO。
 *
 * @param fileId      文件 ID
 * @param fileName    文件名
 * @param fileType    文件类型（photo / video）
 * @param fileSize    文件大小（字节）
 * @param deviceSn    拍摄设备 SN
 * @param flightId    所属飞行任务 ID
 * @param createTime  创建时间（Unix 秒）
 * @param payloadType 负载类型
 * @author DJI DevKit Team
 */
public record MediaFileInfo(
        long fileId,
        String fileName,
        String fileType,
        long fileSize,
        String deviceSn,
        String flightId,
        long createTime,
        String payloadType
) {}
