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

package com.dji.fh.feature.dashboard.spi.sdk;

import com.dji.fh.feature.dashboard.spi.PersonnelCountService;
import com.dji.fh.feature.user.repository.UserRepository;

/**
 * SDK 模式下人员计数实现。
 *
 * <p>直接从 {@code t_user} 表查询已注册用户总数，
 * 反映 skeleton 系统内的真实人员规模。
 *
 * @author DJI DevKit Team
 */
public class SdkPersonnelCountService implements PersonnelCountService {

    private final UserRepository userRepository;

    public SdkPersonnelCountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public int countPersonnel() {
        return (int) userRepository.count();
    }
}
