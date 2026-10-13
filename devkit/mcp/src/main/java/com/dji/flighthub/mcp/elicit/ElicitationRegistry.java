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

package com.dji.flighthub.mcp.elicit;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * elicitation 反向请求的关联登记表。
 *
 * <p>反向请求（server→client）与其响应（client→server）通过 JSON-RPC id 关联。
 * 发起端登记一个 {@link CompletableFuture} 并阻塞等待，reader 线程收到对应 id 的响应后
 * 完成该 future，唤醒发起端。本类只负责关联与唤醒，不涉及具体 transport 读写。
 *
 * @author DJI FlightHub SDK Team
 */
public class ElicitationRegistry {

    /** id → 等待中的 future */
    private final Map<String, CompletableFuture<ElicitResult>> pending = new ConcurrentHashMap<>();

    /**
     * 登记一个待响应的反向请求。
     *
     * @param requestId 反向请求的 JSON-RPC id
     * @return 与该请求关联的 future
     */
    public CompletableFuture<ElicitResult> register(String requestId) {
        CompletableFuture<ElicitResult> future = new CompletableFuture<>();
        pending.put(requestId, future);
        return future;
    }

    /**
     * 阻塞等待指定请求的响应。
     *
     * @param requestId     反向请求 id
     * @param future        register 返回的 future
     * @param timeoutMillis 超时（毫秒）
     * @return 真人的选择结果；超时或异常返回 {@link ElicitResult#timeout()}
     */
    public ElicitResult await(String requestId, CompletableFuture<ElicitResult> future, long timeoutMillis) {
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return ElicitResult.timeout();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ElicitResult.timeout();
        } catch (Exception e) {
            return ElicitResult.timeout();
        } finally {
            pending.remove(requestId);
        }
    }

    /**
     * 尝试用 client 响应完成对应的等待 future。
     *
     * @param requestId 响应对应的 JSON-RPC id
     * @param result    解析后的 elicitation 结果
     * @return true 表示命中并唤醒了等待者；false 表示无匹配（非本 registry 的响应）
     */
    public boolean complete(String requestId, ElicitResult result) {
        CompletableFuture<ElicitResult> future = pending.remove(requestId);
        if (future == null) {
            return false;
        }
        future.complete(result);
        return true;
    }

    /**
     * 是否存在指定 id 的待响应请求。
     *
     * @param requestId 反向请求 id
     * @return true 表示存在
     */
    public boolean isPending(String requestId) {
        return pending.containsKey(requestId);
    }
}
