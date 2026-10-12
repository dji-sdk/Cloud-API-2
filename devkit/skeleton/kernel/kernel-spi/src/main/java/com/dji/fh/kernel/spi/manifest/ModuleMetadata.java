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

package com.dji.fh.kernel.spi.manifest;

import java.util.List;

/**
 * Module 元数据 section。
 *
 * @param category 模块类别：kernel / feature / starter / extension
 * @param tier     档位：must / should / could（first-party 用，ISV module 可省略）
 * @param authors  作者列表
 * @param homepage 主页 URL
 * @param license  许可证标识（SPDX，如 "Apache-2.0"）
 * @author DJI DevKit Team
 */
public record ModuleMetadata(
        String category,
        String tier,
        List<String> authors,
        String homepage,
        String license
) {
    public ModuleMetadata {
        authors = authors == null ? List.of() : List.copyOf(authors);
    }
}
