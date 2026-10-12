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

package com.dji.flighthub.mcp.security;

/**
 * MCP tool 操作的安全等级。
 * 等级越高，授权要求越严格。
 */
public enum SecurityLevel {

    /** L0：只读操作（查询、快照、列表），无需确认。 */
    READ_ONLY(0, "L0"),

    /** L1：可逆操作（机场舱盖、暂停任务），需要 confirm=true。 */
    REVERSIBLE(1, "L1"),

    /** L2：飞行操作（起飞、飞向目标点），需要 confirm + 飞行控制权检查。 */
    FLIGHT(2, "L2"),

    /** L3：关键操作（紧急停止），需要 operator_token + confirm。 */
    CRITICAL(3, "L3");

    private final int level;
    private final String label;

    SecurityLevel(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 根据 int 等级值返回对应 label（如 "L0"、"L1"）。
     * 未匹配时返回 "L" + level（兜底格式）。
     *
     * @param level int 等级值
     * @return 对应的 label 字符串
     */
    public static String labelFor(int level) {
        for (SecurityLevel sl : values()) {
            if (sl.level == level) {
                return sl.label;
            }
        }
        return "L" + level;
    }

    /**
     * 检查此安全等级是否在 session 允许的最大等级范围内。
     */
    public boolean isAllowedBy(int sessionMaxLevel) {
        return this.level <= sessionMaxLevel;
    }
}
