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

import com.dji.fh.feature.dashboard.spi.MediaCountService;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.core.Workspace;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 媒体文件计数真实 SDK 实现。
 *
 * <p>通过 {@code MediaServiceModule.getMediaFilesList()} 查询（pageSize=1），
 * 利用返回结果中的 total 获取文件总数。
 *
 * @author DJI DevKit Team
 */
public class SdkMediaCountService implements MediaCountService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkMediaCountService.class);

    @Override
    public int countMediaFiles() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            LOG.warn("Workspace 不可用，SDK 未初始化");
            return 0;
        }

        MediaServiceModule msm = ws.getMediaServiceModule();
        if (msm == null) {
            LOG.warn("MediaServiceModule 不可用");
            return 0;
        }

        GetMediaFilesListParams req = new GetMediaFilesListParams();
        req.setStartIndex(0);
        req.setPageSize(1);

        CompletableFuture<Integer> future = new CompletableFuture<>();
        msm.getMediaFilesList(req, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getMediaFilesList 失败: " + error.getErrorCode()));
                return;
            }
            future.complete(results.getTotal());
        });

        int total = SdkBridge.await(future);
        LOG.info("SDK 媒体文件总数查询完成：total={}", total);
        return total;
    }
}
