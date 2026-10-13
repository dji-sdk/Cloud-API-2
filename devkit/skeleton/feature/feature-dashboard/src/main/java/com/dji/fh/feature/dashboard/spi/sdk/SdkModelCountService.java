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

import com.dji.fh.feature.dashboard.spi.ModelCountService;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.core.TerraBuildModule;
import com.dji.flighthubsdk.core.Workspace;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模型文件计数真实 SDK 实现。
 *
 * <p>通过 {@code TerraBuildModule.getModelFileList()} 查询（pageSize=1），
 * 利用返回结果中的 total 获取模型文件总数。
 *
 * @author DJI DevKit Team
 */
public class SdkModelCountService implements ModelCountService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkModelCountService.class);

    @Override
    public int countModels() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            LOG.warn("Workspace 不可用，SDK 未初始化");
            return 0;
        }

        TerraBuildModule tbm = ws.getTerraBuildModule();
        if (tbm == null) {
            LOG.warn("TerraBuildModule 不可用");
            return 0;
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        tbm.getModelFileList(1, 1, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getModelFileList 失败: " + error.getServerCode()));
                return;
            }
            future.complete((int) result.getTotal());
        });

        int total = SdkBridge.await(future);
        LOG.info("SDK 模型文件总数查询完成：total={}", total);
        return total;
    }
}
