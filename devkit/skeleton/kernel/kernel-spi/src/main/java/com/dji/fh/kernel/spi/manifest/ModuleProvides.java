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
 * Module 对外暴露的能力声明。
 *
 * @param spi            实现的 SPI 接口列表
 * @param events         发布的事件类型列表
 * @param consumes       监听的事件类型列表（声明用，便于 AI 理解模块协作）
 * @param rest           暴露的 REST endpoint 列表
 * @param mcpTools       暴露的 MCP Tool 列表
 * @param uiSlots        暴露的 UI Slot 列表
 * @param configurations 暴露的配置项列表
 * @author DJI DevKit Team
 */
public record ModuleProvides(
        List<SpiImpl> spi,
        List<EventDecl> events,
        List<EventDecl> consumes,
        List<RestEndpoint> rest,
        List<McpToolDecl> mcpTools,
        List<UiSlotDecl> uiSlots,
        List<ConfigurationDecl> configurations
) {

    public ModuleProvides {
        spi = spi == null ? List.of() : List.copyOf(spi);
        events = events == null ? List.of() : List.copyOf(events);
        consumes = consumes == null ? List.of() : List.copyOf(consumes);
        rest = rest == null ? List.of() : List.copyOf(rest);
        mcpTools = mcpTools == null ? List.of() : List.copyOf(mcpTools);
        uiSlots = uiSlots == null ? List.of() : List.copyOf(uiSlots);
        configurations = configurations == null ? List.of() : List.copyOf(configurations);
    }

    /**
     * @return 各列表均为空列表（非 null）的实例，便于 ISV 不声明任何能力时直接使用
     */
    public static ModuleProvides empty() {
        return new ModuleProvides(null, null, null, null, null, null, null);
    }

    /**
     * SPI 实现声明。
     *
     * @param interfaceName SPI 接口 FQN
     * @param type          泛型 T 的具体类型（可空）
     * @param bean          Spring bean 名（用于 ISV override）
     */
    public record SpiImpl(String interfaceName, String type, String bean) {
    }

    /**
     * 事件类型声明（events 与 consumes 共用）。
     *
     * @param className   事件类 FQN
     * @param description 事件描述
     * @param scope       事件作用域：device / system / task
     */
    public record EventDecl(String className, String description, String scope) {
    }

    /**
     * REST endpoint 声明。
     *
     * @param method      HTTP method
     * @param path        路径
     * @param summary     简介
     * @param operationId 操作 id（与 OpenAPI 对齐）
     */
    public record RestEndpoint(String method, String path, String summary, String operationId) {
    }

    /**
     * MCP Tool 声明。
     *
     * @param name        Tool 名
     * @param description Tool 描述（英文，给 LLM 看）
     * @param mapsTo      映射目标（如 "restOperation:listDevices"）
     */
    public record McpToolDecl(String name, String description, String mapsTo) {
    }

    /**
     * UI Slot 声明。
     *
     * @param name        Slot 名
     * @param location    所在位置（视图路径）
     * @param description Slot 用途说明
     */
    public record UiSlotDecl(String name, String location, String description) {
    }

    /**
     * 配置项声明。
     *
     * @param key          配置 key（application.yaml 的属性名）
     * @param type         类型名（如 "Duration" / "String" / "int"）
     * @param defaultValue 默认值字面量
     * @param description  说明
     */
    public record ConfigurationDecl(String key, String type, String defaultValue, String description) {
    }
}
