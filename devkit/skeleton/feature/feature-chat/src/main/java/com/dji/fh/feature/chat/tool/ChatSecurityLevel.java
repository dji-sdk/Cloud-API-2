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

package com.dji.fh.feature.chat.tool;

/**
 * Chat Tool 安全等级，与 MCP Server SecurityLevel 语义对齐。
 *
 * <ul>
 *   <li>L0 READ_ONLY：只读查询，Chat 直接执行</li>
 *   <li>L1 DEVICE_CONTROL：设备控制，Chat 需用户二次确认</li>
 *   <li>L2 FLIGHT_CONTROL：飞行控制，Chat 拒绝执行</li>
 *   <li>L3 EMERGENCY：紧急操作，Chat 拒绝执行</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public enum ChatSecurityLevel {

    /** L0：只读查询，Chat 直接执行 */
    READ_ONLY,

    /** L1：设备控制，Chat 需用户二次确认 */
    DEVICE_CONTROL,

    /** L2：飞行控制，Chat 拒绝执行 */
    FLIGHT_CONTROL,

    /** L3：紧急操作，Chat 拒绝执行 */
    EMERGENCY
}
