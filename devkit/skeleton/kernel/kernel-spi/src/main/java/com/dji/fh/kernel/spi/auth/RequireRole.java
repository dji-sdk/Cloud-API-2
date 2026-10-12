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

package com.dji.fh.kernel.spi.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色鉴权注解。可注解方法或类。
 *
 * <p>由 Kernel 提供的 {@code AuthorizationAspect}（Plan A2）处理。
 * ISV 也可在自家代码内手动 {@code SecurityContextHolder.current().hasRole(...)} 判断。
 *
 * @author DJI DevKit Team
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * @return 所需角色列表
     */
    String[] value();

    /**
     * @return true 表示必须全部角色都拥有；false（默认）任一角色满足即可
     */
    boolean requireAll() default false;
}
