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

package com.dji.fh.kernel.spi.repository;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * 仅追加日志 SPI（事件流 / 审计 / 时序数据）。
 * 不寻址、按时间顺序读。
 * Retention / 归档策略由实现层 + 配置决定。
 *
 * <p><b>资源管理</b>：{@link #readSince(java.time.Instant)} 和
 * {@link #readBetween(java.time.Instant, java.time.Instant)} 返回的 {@link Stream}
 * 实现 {@link AutoCloseable}。当底层为 JDBC ResultSet / 文件句柄时，
 * 调用方必须在消费完成后 {@link Stream#close()}（推荐 try-with-resources），
 * 否则可能泄漏底层 I/O 资源。
 *
 * @param <T> 日志条目类型
 * @author DJI DevKit Team
 */
public interface AppendLog<T> {

    /**
     * 追加一条日志。时间戳由实现层加。
     *
     * @param entry 日志条目
     */
    void append(T entry);

    /**
     * 读取自指定时间起的全部条目（按时间升序）。
     *
     * @param from 起始时间（含）
     * @return 条目流（caller-owned；必须 close）
     */
    Stream<T> readSince(Instant from);

    /**
     * 读取指定时间区间的条目（按时间升序）。
     *
     * @param from 起始时间（含）
     * @param to   终止时间（不含）
     * @return 条目流（caller-owned；必须 close）
     */
    Stream<T> readBetween(Instant from, Instant to);

    /**
     * @return 全部条目数量
     */
    long count();
}
