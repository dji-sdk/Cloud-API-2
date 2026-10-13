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

package com.dji.fh.feature.device;

import com.dji.fh.kernel.spi.event.HmsAlertLevel;
import java.time.Instant;

/**
 * 单条 HMS 告警（不可变）。
 *
 * @param alertCode 告警码
 * @param level     告警级别
 * @param message   告警消息（可能 null：SDK v0.1 不暴露翻译文本）
 * @param timestamp 收到时间
 * @author DJI DevKit Team
 */
public record HmsAlert(
        String alertCode,
        HmsAlertLevel level,
        String message,
        Instant timestamp
) {}
