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

/**
 * 模型文件计数 SPI，供看板统计使用。
 *
 * <p>默认 mock 实现返回固定值；SDK 模式下由 {@code SdkModelCountService} 实现，
 * 调用 {@code TerraBuildModule.listModelFiles()} 获取真实总数。
 *
 * @author DJI DevKit Team
 */
@FunctionalInterface
public interface ModelCountService {

    /**
     * 查询模型文件总数。
     *
     * @return 模型文件数量
     */
    int countModels();
}
