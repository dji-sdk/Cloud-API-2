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

package com.dji.fh.feature.flightarea;

import com.dji.fh.feature.flightarea.model.FlightAreaStatus;
import com.dji.fh.feature.flightarea.model.FlightAreaType;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 FlightAreaType / FlightAreaStatus 的 String→Enum 转换器。
 *
 * <p>Spring MVC 的 {@code @RequestParam} 不使用 Jackson 的 {@code @JsonCreator}，
 * 默认 {@code Enum.valueOf()} 按枚举名匹配（大写），而前端传小写值（"nfz"/"enable"）。
 * 此配置让 query string 中的小写值正确转换为对应枚举实例。
 */
@Configuration
public class FlightAreaWebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, FlightAreaType.class, FlightAreaType::fromValue);
        registry.addConverter(String.class, FlightAreaStatus.class, FlightAreaStatus::fromValue);
    }
}
