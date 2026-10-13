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

import java.util.List;
import java.util.Optional;

/**
 * 通用 CRUD Repository SPI。
 * 复杂查询、原生 SQL、JPA Specification 等请在 ISV 自家 module 内直接用 JPA / MyBatis。
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型（推荐 String / Long / UUID）
 * @author DJI DevKit Team
 */
public interface Repository<T, ID> {

    /**
     * @param id 主键
     * @return 存在则返回实体；不存在返回 empty
     */
    Optional<T> findById(ID id);

    /**
     * 返回全部实体列表。
     *
     * <p><b>注意</b>：本方法无界 —— 大表场景（条数未知或可能上万）必须改用 {@link #findAll(int, int)}
     * 分页，否则可能 OOM 或长时间阻塞。仅在调用方明确知道实体规模可控（如系统级配置 / 字典表）
     * 时使用。
     *
     * @return 全部实体列表，顺序由实现层决定
     */
    List<T> findAll();

    /**
     * 分页查询。
     *
     * @param page 页号（0-based）
     * @param size 每页大小
     * @return 当前页结果
     */
    Page<T> findAll(int page, int size);

    /**
     * upsert：按 id 判断 create / update。
     *
     * @param entity 实体
     * @return 持久化后的实体
     */
    T save(T entity);

    /**
     * @param id 主键
     */
    void deleteById(ID id);

    /**
     * @param id 主键
     * @return 存在则 true
     */
    boolean existsById(ID id);

    /**
     * @return 全部元素数量
     */
    long count();
}
