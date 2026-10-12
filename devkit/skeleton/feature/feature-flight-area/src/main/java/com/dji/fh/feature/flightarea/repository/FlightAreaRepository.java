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

package com.dji.fh.feature.flightarea.repository;

import com.dji.fh.feature.flightarea.entity.FlightAreaEntity;
import com.dji.fh.feature.flightarea.model.FlightAreaStatus;
import com.dji.fh.feature.flightarea.model.FlightAreaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 飞行区域数据访问接口。
 *
 * @author DJI DevKit Team
 */
public interface FlightAreaRepository extends JpaRepository<FlightAreaEntity, String> {

    /**
     * 按条件筛选飞行区域（支持类型、状态、名称模糊匹配，任一参数为 null 时不筛选）。
     *
     * @param type     区域类型（可选）
     * @param status   状态（可选）
     * @param name     名称关键字（可选，模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT a FROM FlightAreaEntity a WHERE "
            + "(:type IS NULL OR a.type = :type) "
            + "AND (:status IS NULL OR a.status = :status) "
            + "AND (:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')")
    Page<FlightAreaEntity> findByFilters(
            @Param("type") FlightAreaType type,
            @Param("status") FlightAreaStatus status,
            @Param("name") String name,
            Pageable pageable
    );

    /**
     * 原子删除指定 ID 的区域，返回受影响的行数。
     *
     * @param id 区域 ID
     * @return 删除的行数（0 或 1）
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FlightAreaEntity a WHERE a.id = :id")
    int deleteByIdReturningCount(@Param("id") String id);
}
