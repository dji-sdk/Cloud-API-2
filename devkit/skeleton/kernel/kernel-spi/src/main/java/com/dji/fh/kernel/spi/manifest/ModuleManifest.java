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
 * module.yaml 顶层结构。
 *
 * <p>本类是反序列化目标 POJO；实际 YAML 解析与 Kernel 启动期校验逻辑
 * 由 Plan A2 中的 {@code ModuleManifestLoader} 承担。
 *
 * <p>必填字段：schemaVersion / id / version / name / description /
 * metadata / requires / toggle。
 *
 * @param schemaVersion module.yaml schema 自身版本（Kernel 据此选 parser）
 * @param id            模块 id（kebab-case，全局唯一）
 * @param version       模块版本（SemVer）
 * @param name          人类可读名
 * @param description   功能描述（强制必填，给 AI / llms.txt 看）
 * @param metadata      元数据
 * @param requires      兼容性声明
 * @param provides      对外暴露的能力（可为 {@link ModuleProvides#empty()}）
 * @param ai            AI 元数据（可空，ISV module 可省略）
 * @param toggle        启用开关
 * @author DJI DevKit Team
 */
public record ModuleManifest(
        String schemaVersion,
        String id,
        String version,
        String name,
        String description,
        ModuleMetadata metadata,
        ModuleRequires requires,
        ModuleProvides provides,
        ModuleAi ai,
        ModuleToggle toggle
) {
}
