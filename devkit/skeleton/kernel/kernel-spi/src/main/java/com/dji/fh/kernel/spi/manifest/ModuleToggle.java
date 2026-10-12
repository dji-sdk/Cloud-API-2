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

/**
 * Module 启用开关。对应 D4 「可禁用」设计原则。
 *
 * @param property     启用开关对应的配置 key（如 "fh.skeleton.feature.device.enabled"）
 * @param defaultValue 默认是否启用
 * @author DJI DevKit Team
 */
public record ModuleToggle(String property, boolean defaultValue) {
}
