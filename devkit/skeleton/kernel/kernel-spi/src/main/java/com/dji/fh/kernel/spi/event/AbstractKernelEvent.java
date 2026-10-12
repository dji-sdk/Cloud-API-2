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
import java.util.Objects;
import java.util.UUID;

/**
 * 推荐基类：自动填充 eventId / occurredAt / source 元数据。
 * 也可直接以 record 实现 KernelEvent，不强制继承。
 *
 * @author DJI DevKit Team
 */
public abstract class AbstractKernelEvent implements KernelEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final String source;

    /**
     * @param source 触发来源标识
     */
    protected AbstractKernelEvent(String source) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.source = Objects.requireNonNull(source, "source 不能为空");
    }

    @Override
    public final String eventId() {
        return eventId;
    }

    @Override
    public final Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public final String source() {
        return source;
    }
}
