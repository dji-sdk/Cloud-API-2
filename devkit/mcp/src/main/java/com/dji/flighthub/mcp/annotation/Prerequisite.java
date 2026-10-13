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

/**
 * MCP Tool 前置条件枚举。
 * 在 @McpTool 注解的 prerequisites 中声明，框架按声明顺序执行。
 */
public enum Prerequisite {
    /** 需要有效 session */
    SESSION,
    /** 需要安全等级许可 */
    SECURITY_LEVEL,
    /** 需要飞行控制权（含 session + SN 校验） */
    CONTROL_AUTHORITY,
    /** 需要 DRC 低延迟链路（含 session + 控制权 + DRC 校验） */
    DRC_LINK
}
