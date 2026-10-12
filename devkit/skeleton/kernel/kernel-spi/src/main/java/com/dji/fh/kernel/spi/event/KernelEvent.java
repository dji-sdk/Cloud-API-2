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

package com.dji.fh.kernel.spi.event;

import java.time.Instant;

/**
 * 所有 Kernel + ISV 事件的顶层标记接口。
 * 提供事件追踪 / 幂等所需的最小元数据。
 *
 * <p>事件 schema 进严格 SemVer 契约 —— 一旦发布破坏即 major。
 *
 * @author DJI DevKit Team
 */
public interface KernelEvent {

    /**
     * @return 事件唯一标识（UUID，构造时生成）
     */
    String eventId();

    /**
     * @return 事件发生时间（不是 publish 时间）
     */
    Instant occurredAt();

    /**
     * @return 触发来源（例如 "kernel-sdk" / "feature-device" / "isv.acme.inspection"）
     */
    String source();
}
