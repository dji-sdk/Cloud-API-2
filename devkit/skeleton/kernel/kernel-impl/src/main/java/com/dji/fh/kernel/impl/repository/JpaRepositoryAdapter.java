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

import com.dji.fh.kernel.spi.repository.Page;
import com.dji.fh.kernel.spi.repository.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Kernel Repository SPI 的 JPA 默认实现基类。
 *
 * <p>ISV 在自家 module 中通过继承使用：
 * <pre>
 *   &#064;Repository
 *   public class DeviceRepository extends JpaRepositoryAdapter&lt;Device, Long&gt; {
 *       public DeviceRepository(EntityManager em) {
 *           super(em, Device.class);
 *       }
 *   }
 * </pre>
 *
 * <p>本类只实现 {@link Repository} 的 7 个方法，<b>不继承</b> Spring Data Repository hierarchy
 * （刻意保持轻量；ISV 想用 Spring Data 复杂特性可直接 {@code extends JpaRepository}，与本类共存）。
 *
 * <p>事务行为：write 方法（{@link #save}/{@link #deleteById}）标注 {@code @Transactional}；
 * read 方法不标，沿用调用方事务（或无事务读）。
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 * @author DJI DevKit Team
 */
public abstract class JpaRepositoryAdapter<T, ID> implements Repository<T, ID> {

    /**
     * JPA EntityManager。由子类构造器注入。
     */
    protected final EntityManager entityManager;

    /**
     * 实体 Class 对象，用于 find / TypedQuery。
     */
    protected final Class<T> entityClass;

    /**
     * @param entityManager JPA EntityManager（Spring 注入）
     * @param entityClass   实体类的 Class 对象
     */
    protected JpaRepositoryAdapter(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager 不能为空");
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass 不能为空");
    }

    @Override
    public Optional<T> findById(ID id) {
        Objects.requireNonNull(id, "id 不能为空");
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery(selectJpql(), entityClass).getResultList();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 当 {@code page < 0} 或 {@code size <= 0}
     */
    @Override
    public Page<T> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page 不可负: " + page);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size 必须为正: " + size);
        }
        TypedQuery<T> query = entityManager.createQuery(selectJpql(), entityClass);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<T> content = query.getResultList();
        return new Page<>(content, page, size, count());
    }

    /**
     * {@inheritDoc}
     *
     * <p>实现使用 {@link EntityManager#merge(Object)}，同时处理 create（id 为空时 INSERT）/
     * update（id 已存在时 UPDATE）两种 upsert 语义。
     */
    @Override
    @Transactional
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity 不能为空");
        // merge 同时处理 create（id 为空）/ update（id 已存在）两种场景 —— 与 SPI 的 upsert 语义一致
        return entityManager.merge(entity);
    }

    /**
     * {@inheritDoc}
     *
     * <p>id 不存在时静默跳过，不抛 {@code EntityNotFoundException}。
     */
    @Override
    @Transactional
    public void deleteById(ID id) {
        Objects.requireNonNull(id, "id 不能为空");
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public boolean existsById(ID id) {
        Objects.requireNonNull(id, "id 不能为空");
        return entityManager.find(entityClass, id) != null;
    }

    @Override
    public long count() {
        return entityManager.createQuery(countJpql(), Long.class).getSingleResult();
    }

    /**
     * 生成查询所有实体的 JPQL 语句。
     *
     * @return JPQL 查询字符串
     */
    private String selectJpql() {
        return "SELECT e FROM " + entityClass.getSimpleName() + " e";
    }

    /**
     * 生成统计实体数量的 JPQL 语句。
     *
     * @return JPQL 查询字符串
     */
    private String countJpql() {
        return "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
    }
}
