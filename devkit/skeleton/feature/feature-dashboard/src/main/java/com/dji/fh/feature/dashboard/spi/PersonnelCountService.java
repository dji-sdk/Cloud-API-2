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

package com.dji.fh.feature.dashboard.spi;

/**
 * 人员计数 SPI，供看板统计使用。
 *
 * <p>默认 mock 实现返回 0；ISV 可通过声明自有 bean 覆盖，
 * 例如基于 {@code UserRepository.count()} 返回真实注册用户数。
 *
 * @author DJI DevKit Team
 */
@FunctionalInterface
public interface PersonnelCountService {

    /**
     * 查询人员总数。
     *
     * @return 人员数量
     */
    int countPersonnel();
}
