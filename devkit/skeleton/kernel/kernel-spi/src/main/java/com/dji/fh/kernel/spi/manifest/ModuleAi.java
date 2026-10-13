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
 * AI 元数据 section —— 专为 skill / RAG 设计的自然语言提示。
 *
 * @param capability               模块核心能力短语
 * @param typicalExtensionPoints   典型扩展点描述列表
 * @param examples                 扩展示例列表
 * @author DJI DevKit Team
 */
public record ModuleAi(
        String capability,
        List<String> typicalExtensionPoints,
        List<Example> examples
) {
    public ModuleAi {
        typicalExtensionPoints = typicalExtensionPoints == null ? List.of() : List.copyOf(typicalExtensionPoints);
        examples = examples == null ? List.of() : List.copyOf(examples);
    }

    /**
     * 扩展示例。
     *
     * @param description 场景描述
     * @param mechanism   推荐机制
     */
    public record Example(String description, String mechanism) {
    }
}
