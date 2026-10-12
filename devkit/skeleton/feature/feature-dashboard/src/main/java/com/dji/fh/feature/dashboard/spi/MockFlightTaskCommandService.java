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

import com.dji.fh.feature.dashboard.dto.CreateFlightTaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 飞行任务命令 Mock 实现。
 *
 * <p>不依赖 SDK，返回模拟数据，适用于无 SDK 环境的前端联调和功能演示。
 *
 * @author DJI DevKit Team
 */
public class MockFlightTaskCommandService implements FlightTaskCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(MockFlightTaskCommandService.class);

    @Override
    public CreateResult createTask(CreateFlightTaskDto dto) {
        String taskUuid = "task_mock_" + System.currentTimeMillis() % 100000;
        LOG.debug("[Mock] 创建飞行任务: name={}, taskUuid={}", dto.getName(), taskUuid);
        return new CreateResult(taskUuid);
    }

    @Override
    public void updateTask(String taskUuid, CreateFlightTaskDto dto) {
        LOG.info("[Mock] 编辑飞行任务: taskUuid={}, newName={}", taskUuid, dto.getName());
    }

    @Override
    public void deleteTask(String taskUuid) {
        LOG.info("[Mock] 删除飞行任务: taskUuid={}", taskUuid);
    }

    @Override
    public void executeTask(String taskUuid) {
        LOG.info("[Mock] 执行飞行任务: taskUuid={}", taskUuid);
    }

    @Override
    public void pauseTask(String taskUuid) {
        LOG.info("[Mock] 暂停飞行任务: taskUuid={}", taskUuid);
    }

    @Override
    public void resumeTask(String taskUuid) {
        LOG.info("[Mock] 恢复飞行任务: taskUuid={}", taskUuid);
    }

    @Override
    public void cancelTask(String taskUuid) {
        LOG.info("[Mock] 取消飞行任务: taskUuid={}", taskUuid);
    }
}
