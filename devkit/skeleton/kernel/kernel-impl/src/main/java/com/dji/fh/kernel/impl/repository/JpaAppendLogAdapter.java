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

package com.dji.fh.kernel.impl.repository;

import com.dji.fh.kernel.spi.repository.AppendLog;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Kernel AppendLog SPI 的 JPA 默认实现基类。
 *
 * <p>要求实体含一个 {@link Instant} 类型的时间戳字段，字段名通过构造器传入。
 * ISV 在自家 module 中通过继承使用：
 * <pre>
 *   &#064;Repository
 *   public class AuditLogStore extends JpaAppendLogAdapter&lt;AuditEntry&gt; {
 *       public AuditLogStore(EntityManager em) {
 *           super(em, AuditEntry.class, "createdAt");
 *       }
 *   }
 * </pre>
 *
 * <p><b>资源管理</b>：{@link #readSince}/{@link #readBetween} 返回的 Stream 由 JPA
 * {@code getResultStream()} 直接返出，<b>调用方必须 close</b>（推荐 try-with-resources），
 * 否则会泄漏 JDBC ResultSet。
 *
 * @param <T> 日志条目实体类型
 * @author DJI DevKit Team
 */
public abstract class JpaAppendLogAdapter<T> implements AppendLog<T> {

    /**
     * 合法 Java 标识符的正则（用于校验 timestampFieldName，
     * 防止 ISV 误传非法字段名导致 JPQL 注入或解析错误）。
     */
    private static final java.util.regex.Pattern IDENTIFIER_PATTERN =
            java.util.regex.Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * JPA EntityManager。
     */
    protected final EntityManager entityManager;

    /**
     * 日志条目实体 Class。
     */
    protected final Class<T> entityClass;

    /**
     * 时间戳字段名，必须是实体的 {@link Instant} 类型字段。
     */
    protected final String timestampFieldName;

    /**
     * @param entityManager      JPA EntityManager
     * @param entityClass        实体类
     * @param timestampFieldName 实体中作为时间戳的 Instant 字段名（如 "createdAt" / "timestamp"），
     *                           必须是合法 Java 标识符（{@code [A-Za-z_][A-Za-z0-9_]*}）
     */
    protected JpaAppendLogAdapter(EntityManager entityManager, Class<T> entityClass,
                                  String timestampFieldName) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager 不能为空");
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass 不能为空");
        this.timestampFieldName = Objects.requireNonNull(
                timestampFieldName, "timestampFieldName 不能为空");
        if (timestampFieldName.isBlank()) {
            throw new IllegalArgumentException("timestampFieldName 不能为空白");
        }
        if (!IDENTIFIER_PATTERN.matcher(timestampFieldName).matches()) {
            throw new IllegalArgumentException(
                    "timestampFieldName 必须是合法 Java 标识符: '" + timestampFieldName + "'");
        }
    }

    @Override
    @Transactional
    public void append(T entry) {
        Objects.requireNonNull(entry, "entry 不能为空");
        entityManager.persist(entry);
    }

    @Override
    public Stream<T> readSince(Instant from) {
        Objects.requireNonNull(from, "from 不能为空");
        String jpql = "SELECT e FROM " + entityAlias() + " "
                + "WHERE e." + timestampFieldName + " >= :from "
                + orderByAsc();
        return entityManager.createQuery(jpql, entityClass)
                .setParameter("from", from)
                .getResultStream();
    }

    @Override
    public Stream<T> readBetween(Instant from, Instant to) {
        Objects.requireNonNull(from, "from 不能为空");
        Objects.requireNonNull(to, "to 不能为空");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "to 早于 from: from=" + from + " to=" + to);
        }
        String jpql = "SELECT e FROM " + entityAlias() + " "
                + "WHERE e." + timestampFieldName + " >= :from "
                + "AND e." + timestampFieldName + " < :to "
                + orderByAsc();
        return entityManager.createQuery(jpql, entityClass)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultStream();
    }

    @Override
    public long count() {
        String jpql = "SELECT COUNT(e) FROM " + entityAlias();
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    /**
     * 构造带别名的 entity 引用，例：{@code "LogEntry e"}。
     */
    private String entityAlias() {
        return entityClass.getSimpleName() + " e";
    }

    /**
     * 构造按时间戳升序排序子句，例：{@code "ORDER BY e.createdAt ASC"}。
     */
    private String orderByAsc() {
        return "ORDER BY e." + timestampFieldName + " ASC";
    }
}
