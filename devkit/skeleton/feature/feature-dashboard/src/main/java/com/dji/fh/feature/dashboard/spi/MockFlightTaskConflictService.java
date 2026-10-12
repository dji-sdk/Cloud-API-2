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

import java.util.List;

/**
 * 飞行任务时间冲突校验 mock 实现。
 *
 * <p>Mock 模式不拦截任何创建/编辑请求：mock 任务数据本身是随机生成的，
 * 若参与冲突比对会产生与真实排期无关的误拦截，干扰 Prompt 调试与功能演示。
 * 真实拦截能力见 {@code SdkFlightTaskConflictService}。
 *
 * @author DJI DevKit Team
 */
public class MockFlightTaskConflictService implements FlightTaskConflictService {

    /**
     * 始终返回无冲突。
     *
     * @param candidate 待校验的任务时间窗
     * @return 空列表
     */
    @Override
    public List<ConflictingTask> findConflicts(TaskWindow candidate) {
        return List.of();
    }
}
