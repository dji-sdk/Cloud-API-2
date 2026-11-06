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

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthub.mcp.session.SessionContext;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订阅推送数据的环形缓冲区。
 *
 * <p>按 {@code (scope, stream_key)} 分桶，每桶独立的 {@link ArrayDeque} 环形缓冲。
 * scope 由 {@link #scope(SessionContext, String)} 生成（session + 设备），避免多 session 间数据串读。
 * 容量满时丢弃最旧并累加 {@code dropped} 计数，同时记录最后被淘汰的 {@code _seq}，
 * 读取时据此判断调用方的 {@code since_seq} 之后是否有数据丢失（{@code truncated}）。
 *
 * <p>每条记录带单调递增 {@code _seq}。读取使用 {@code since_seq} 续读，
 * 不清空缓冲，调用方可反复查看。
 *
 * <p>线程安全：外层 ConcurrentHashMap 保证桶级并发，桶内操作 synchronized。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class AiSubscriptionBuffer {

    private static final int DEFAULT_CAPACITY = 200;

    private final int capacity;
    private final AtomicLong globalSeq = new AtomicLong(0);
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public AiSubscriptionBuffer() {
        this(DEFAULT_CAPACITY);
    }

    public AiSubscriptionBuffer(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 生成 session + 设备维度的缓冲作用域。
     *
     * @param session 当前会话
     * @param sn      设备序列号
     * @return 作用域字符串
     */
    public static String scope(SessionContext session, String sn) {
        return session.getSessionId() + "::" + sn;
    }

    public void append(String scope, String streamKey, Map<String, Object> data) {
        String key = bucketKey(scope, streamKey);
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity));
        Map<String, Object> entry = new LinkedHashMap<>(data);
        entry.put("_seq", globalSeq.incrementAndGet());
        bucket.add(Collections.unmodifiableMap(entry));
    }

    public ReadResult read(String scope, String streamKey, long sinceSeq, int limit) {
        String key = bucketKey(scope, streamKey);
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            return new ReadResult(Collections.emptyList(), 0, false);
        }
        return bucket.read(sinceSeq, limit);
    }

    public void remove(String scope, String streamKey) {
        buckets.remove(bucketKey(scope, streamKey));
    }

    private static String bucketKey(String scope, String streamKey) {
        return scope + "::" + streamKey;
    }

    /**
     * 读取结果。
     *
     * @param events    本次读取到的事件（只读）
     * @param dropped   该桶自创建以来累计被淘汰的条数
     * @param truncated since_seq 之后的事件中是否已有被淘汰（调用方漏读了数据）
     */
    public record ReadResult(List<Map<String, Object>> events, long dropped, boolean truncated) {
    }

    private static class Bucket {

        private final int capacity;
        private final Deque<Map<String, Object>> deque;
        private long dropped;
        private long lastEvictedSeq;

        Bucket(int capacity) {
            this.capacity = capacity;
            this.deque = new ArrayDeque<>(capacity);
        }

        synchronized void add(Map<String, Object> entry) {
            if (deque.size() >= capacity) {
                Map<String, Object> evicted = deque.pollFirst();
                lastEvictedSeq = (long) evicted.get("_seq");
                dropped++;
            }
            deque.addLast(entry);
        }

        synchronized ReadResult read(long sinceSeq, int limit) {
            List<Map<String, Object>> result = new ArrayList<>();
            int count = 0;
            for (Map<String, Object> entry : deque) {
                long seq = (long) entry.get("_seq");
                if (seq > sinceSeq) {
                    result.add(entry);
                    count++;
                    if (count >= limit) {
                        break;
                    }
                }
            }
            return new ReadResult(Collections.unmodifiableList(result), dropped, lastEvictedSeq > sinceSeq);
        }
    }
}
