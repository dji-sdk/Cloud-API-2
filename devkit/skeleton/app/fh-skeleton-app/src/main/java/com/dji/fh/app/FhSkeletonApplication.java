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

package com.dji.fh.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FH Skeleton v0.1 启动入口。
 *
 * <p>开发态：{@code mvn spring-boot:run}（默认 mock profile，启动后即可见事件流）。
 * <p>生产态：{@code java -jar fh-skeleton-app-0.1.0.jar --spring.profiles.active=sdk}
 * （需后续 plan 提供真 SDK adapter）。
 *
 * <p>ISV fork 时直接复制本类改包名 / 加 ComponentScan basePackages 即可。
 *
 * @author DJI DevKit Team
 */
@SpringBootApplication
public class FhSkeletonApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FhSkeletonApplication.class, args);
    }
}
