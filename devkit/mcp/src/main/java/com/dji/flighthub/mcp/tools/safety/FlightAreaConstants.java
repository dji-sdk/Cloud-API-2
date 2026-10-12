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

package com.dji.flighthub.mcp.tools.safety;

import java.util.List;
import java.util.Map;

/**
 * 自定义飞行区模块共享常量。
 * 被 FhFlightAreaListTool、FhFlightAreaManageTool 等工具共用，避免重复定义。
 *
 * @author DJI FlightHub SDK Team
 */
public final class FlightAreaConstants {

    private FlightAreaConstants() {
        // 工具类禁止实例化
    }

    /** 有效飞行区类型 */
    public static final List<String> VALID_TYPES = List.of("nfz", "dfence", "noland");

    /** 飞行区类型显示名映射 */
    public static final Map<String, String> AREA_TYPE_DISPLAY = Map.of(
            "nfz", "限飞区(No-Fly Zone)",
            "dfence", "作业区(Geofence)",
            "noland", "禁降区(No-Land Zone)"
    );
}
