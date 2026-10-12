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

package com.dji.fh.feature.wayline;

import java.util.Map;

/**
 * 航线类型整数编码 → 中文标签映射。
 *
 * <p>编码取自 {@code skills/cloudapi2-frontend-component/references/wayline-editor.md}
 * 中的 {@code WAYLINE_TYPE} 定义：
 * <ul>
 *   <li>0  → 航点航线</li>
 *   <li>1  → 面状航线</li>
 *   <li>2  → 斜面航线</li>
 *   <li>3  → 几何体航线</li>
 *   <li>4  → 带状航线</li>
 *   <li>7  → 贴近摄影航线</li>
 *   <li>8  → AI 巡逻航线</li>
 * </ul>
 * 未知编码统一返回"未知类型"。
 *
 * @author DJI DevKit Team
 */
public final class WaylineTypeMapping {

    /** 未知类型的兜底标签 */
    public static final String UNKNOWN_LABEL = "未知类型";

    private static final Map<Integer, String> WAYLINE_TYPE_LABELS = Map.ofEntries(
            Map.entry(0, "航点航线"),
            Map.entry(1, "面状航线"),
            Map.entry(2, "斜面航线"),
            Map.entry(3, "几何体航线"),
            Map.entry(4, "带状航线"),
            Map.entry(7, "贴近摄影航线"),
            Map.entry(8, "AI 巡逻航线")
    );

    private WaylineTypeMapping() {
    }

    /**
     * 将航线类型编码转为中文标签。
     *
     * @param type 航线类型编码（可能为 null）
     * @return 中文标签，null 或未知编码返回 {@link #UNKNOWN_LABEL}
     */
    public static String labelOf(Integer type) {
        if (type == null) {
            return UNKNOWN_LABEL;
        }
        return WAYLINE_TYPE_LABELS.getOrDefault(type, UNKNOWN_LABEL);
    }
}
