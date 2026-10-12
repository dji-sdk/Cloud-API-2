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

package com.dji.flighthub.mcp.session;

/**
 * 无人机飞行阶段枚举，用于追踪当前飞行状态和防止冲突操作。
 *
 * @author DJI FlightHub SDK Team
 */
public enum FlightPhase {
    /** 无飞行活动 */
    IDLE,
    /** 起飞中 */
    TAKING_OFF,
    /** 悬停 */
    HOVERING,
    /** 指点飞行中 */
    FLYING_TO,
    /** 环绕飞行中 */
    ORBITING,
    /** 返航中 */
    RETURNING,
    /** 降落中 */
    LANDING,
    /** 紧急状态 */
    EMERGENCY,
    /** 航线任务执行中 */
    WAYLINE_EXECUTING
}
