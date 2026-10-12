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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Terra 模块配置.
 *
 * @param enabled 是否启用（默认 true）
 * @param mock    是否使用 Mock 实现（默认 false）
 * @author DJI DevKit Team
 */
@ConfigurationProperties("fh.skeleton.feature.terra")
public record TerraProperties(boolean enabled, boolean mock) {

    /**
     * record 规范构造器.
     */
    public TerraProperties {
        // 无额外逻辑
    }
}
