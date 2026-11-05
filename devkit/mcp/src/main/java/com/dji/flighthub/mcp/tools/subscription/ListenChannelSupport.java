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

package com.dji.flighthub.mcp.tools.subscription;

import com.dji.flighthub.mcp.session.SessionContext;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 从 {@code FhOsdListenTool} 抽取的 listen/cancel/events/stop 共性逻辑。
 *
 * <p>Batch C 新增的订阅类 action（{@code fh_psdk_subscribe}、
 * {@code fh_deploy_subscribe} 等新 Tool，以及现有 Tool 新增的
 * {@code listen_*}/{@code events}/{@code stop} 三元组）统一通过本类编排：
 * 调用方只需提供 channel 的 registry key、label、mock 数据供应器与真实
 * SDK listen/cancel 闭包，不需要重复编写订阅注册、缓冲区写入、游标读取、
 * 批量取消等样板代码。
 *
 * <p>与 {@code FhOsdListenTool}/{@code FhAiSubscribeTool}/{@code FhLiveSeiTool}
 * 的既有实现无耦合——本类只服务 Batch C 新代码，不回头重构那三个已上线 Tool。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class ListenChannelSupport {

    private static final AtomicInteger MOCK_ID_GEN = new AtomicInteger(29000);

    private final SubscriptionBuffer buffer;

    public ListenChannelSupport(SubscriptionBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * 组装订阅注册表 key：{@code namespace::scopeId::channel}。
     *
     * @param namespace 命名空间前缀（如 "psdk_subscribe"，建议与 Tool 名对应）
     * @param scopeId   作用域标识（设备 SN / 机场 SN 等）
     * @param channel   channel 名（如 "device_list"）
     * @return 注册表 key
     */
    public static String registryKey(String namespace, String scopeId, String channel) {
        return namespace + "::" + scopeId + "::" + channel;
    }

    /**
     * 静默取消订阅，取消失败只记日志，不向上抛异常（用于 start 失败回滚等场景）。
     */
    public void cancelQuietly(SessionContext session, String key, Logger log) {
        try {
            session.cancelListen(key);
        } catch (RuntimeException e) {
            log.warn("Failed to cancel listen: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 提供一条 mock 事件数据。
     */
    @FunctionalInterface
    public interface MockEventSupplier {
        Map<String, Object> get();
    }

    /**
     * 真实 SDK listen 调用：接收事件回调，返回 SDK listenId（失败返回负数）。
     */
    @FunctionalInterface
    public interface RealListenFn {
        int listen(Consumer<Map<String, Object>> onEvent);
    }

    /**
     * 真实 SDK cancel 调用。
     */
    @FunctionalInterface
    public interface RealCancelFn {
        void cancel(int listenId);
    }

    /**
     * 启动一个订阅 channel：mock 模式下直接写一条 mock 数据并分配 mock id；
     * 真实模式下调用 SDK listen，把回调数据写入 buffer；成功后在 session
     * 注册表登记取消动作（取消时调用 SDK cancel 并清空 buffer）。
     *
     * @return SDK/mock listenId；SDK 拒绝订阅时返回负数，调用方需自行处理回滚
     */
    public int startChannel(SessionContext session, String registryKey, String label,
                             String scope, String streamKey, boolean mock,
                             MockEventSupplier mockSupplier, RealListenFn realListenFn, RealCancelFn realCancelFn) {
        int listenId;
        if (mock) {
            buffer.append(scope, streamKey, withTimestamp(mockSupplier.get()));
            listenId = MOCK_ID_GEN.incrementAndGet();
        } else {
            listenId = realListenFn.listen(payload -> buffer.append(scope, streamKey, withTimestamp(payload)));
        }
        if (listenId < 0) {
            return listenId;
        }
        session.registerListen(registryKey, label, () -> {
            try {
                if (!mock) {
                    realCancelFn.cancel(listenId);
                }
            } finally {
                buffer.remove(scope, streamKey);
            }
        });
        return listenId;
    }

    /**
     * 读取一个 channel 的增量事件，组装成 events action 的单条 stream 结果。
     *
     * @param registryKeyForActive 该 channel 的订阅注册表 key，用于判断 active
     */
    public Map<String, Object> readChannel(SessionContext session, String registryKeyForActive,
                                            String scope, String streamKey, long sinceSeq, int limit) {
        SubscriptionBuffer.ReadResult result = buffer.read(scope, streamKey, sinceSeq, limit);
        Map<String, Object> stream = new LinkedHashMap<>();
        stream.put("active", session.getListenRegistry().containsKey(registryKeyForActive));
        stream.put("events", result.events());
        stream.put("count", result.events().size());
        stream.put("dropped", result.dropped());
        stream.put("truncated", result.truncated());
        if (!result.events().isEmpty()) {
            stream.put("last_seq", result.events().get(result.events().size() - 1).get("_seq"));
        }
        return stream;
    }

    /**
     * 批量取消订阅，汇总成功/未激活/失败三类结果（对应 stop action 的标准响应形态）。
     *
     * @param channels channel 名列表
     * @param keyFn    channel 名 → 注册表 key 的映射
     */
    public StopResult stopChannels(SessionContext session, List<String> channels,
                                    java.util.function.Function<String, String> keyFn, Logger log) {
        List<String> stopped = new ArrayList<>();
        List<String> notActive = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String channel : channels) {
            String key = keyFn.apply(channel);
            try {
                if (session.cancelListen(key)) {
                    stopped.add(channel);
                } else {
                    notActive.add(channel);
                }
            } catch (RuntimeException e) {
                log.warn("Failed to cancel listen: key={}, error={}", key, e.getMessage());
                failed.add(channel);
            }
        }
        return new StopResult(stopped, notActive, failed);
    }

    /**
     * 在 session 的订阅注册表里查找指定前缀下仍活跃的 channel 名（按字典序去重排序）。
     *
     * @param prefix 形如 {@code namespace + "::" + scopeId + "::"}
     */
    public List<String> activeChannels(SessionContext session, String prefix) {
        Set<String> names = new TreeSet<>();
        for (String key : session.getListenRegistry().keySet()) {
            if (key.startsWith(prefix)) {
                names.add(key.substring(prefix.length()));
            }
        }
        return new ArrayList<>(names);
    }

    private static Map<String, Object> withTimestamp(Map<String, Object> event) {
        Map<String, Object> copy = new LinkedHashMap<>(event);
        copy.put("_ts", System.currentTimeMillis());
        return copy;
    }

    /**
     * {@link #stopChannels} 的结果：成功取消 / 本就未激活 / 取消失败的 channel 名列表。
     */
    public record StopResult(List<String> stopped, List<String> notActive, List<String> failed) {
    }
}
