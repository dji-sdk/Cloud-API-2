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

package com.dji.fh.feature.terra.dto;

/**
 * 瓦片存储信息，供代理端点拼接 MinIO 对象 URL.
 *
 * @param storageBaseUrl 存储基础 URL（含协议、主机、端口、桶名），如 {@code http://10.x:30802/bucket/}
 * @param filePrefix     瓦片文件目录前缀（不含 tileset.json 文件名），如 {@code project/xxx/}
 */
public record TileStorageInfo(
        String storageBaseUrl,
        String filePrefix
) { }
