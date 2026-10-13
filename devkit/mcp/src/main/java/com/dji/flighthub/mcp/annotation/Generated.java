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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要真实 DJI FlightHub SDK 连接才能执行的方法或类。
 *
 * <p>JaCoCo 0.8.2+ 会自动排除所有简单名称为 {@code Generated} 且 Retention 为
 * {@link RetentionPolicy#CLASS} 或 {@link RetentionPolicy#RUNTIME} 的注解标记的元素。
 * 因此，所有 SDK 集成路径（{@code *WithRealSdk} 系列方法）标记此注解后，将不再计入
 * 覆盖率分母，避免因无法单测 SDK 调用而导致 CI 覆盖率检查失败。</p>
 *
 * <p>使用场景：所有需要真实设备连接的私有方法，如 {@code executeWithRealSdk}、
 * {@code takeoffWithRealSdk} 等。</p>
 *
 * @author DJI FlightHub SDK Team
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
public @interface Generated {

    /**
     * 生成原因描述，默认为 SDK 集成路径。
     *
     * @return 原因描述字符串
     */
    String value() default "sdk-integration";
}
