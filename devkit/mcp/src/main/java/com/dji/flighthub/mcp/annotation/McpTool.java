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

package com.dji.flighthub.mcp.annotation;

import com.dji.flighthub.mcp.security.SecurityLevel;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP Tool 元数据声明注解。
 * 标注在 AbstractMcpTool 子类上，声明 tool 的名称、描述、安全等级和前置条件。
 *
 * 示例用法：
 * {@code
 * @McpTool(
 *     name = "fh_flyto",
 *     description = "Fly drone to a target GPS coordinate",
 *     securityLevel = SecurityLevel.FLIGHT,
 *     prerequisites = {Prerequisite.SESSION, Prerequisite.SECURITY_LEVEL,
 *                      Prerequisite.CONTROL_AUTHORITY, Prerequisite.DRC_LINK},
 *     requiresConfirm = true,
 *     targetSnParam = "drone_sn"
 * )
 * }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {
    /** Tool 名称（MCP 协议中的 name 字段） */
    String name();

    /** Tool 描述（MCP 协议中的 description 字段） */
    String description();

    /** 安全等级，默认 L0（只读） */
    SecurityLevel securityLevel() default SecurityLevel.READ_ONLY;

    /** 前置条件列表，按声明顺序执行 */
    Prerequisite[] prerequisites() default {Prerequisite.SESSION};

    /** 是否需要人工确认（confirm=true） */
    boolean requiresConfirm() default false;

    /** 目标设备 SN 参数名（用于控制权/DRC 校验时从 args 中提取 SN） */
    String targetSnParam() default "drone_sn";
}
