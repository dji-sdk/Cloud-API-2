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

package com.dji.fh.feature.flightarea.entity;

import com.dji.fh.feature.flightarea.model.FlightAreaStatus;
import com.dji.fh.feature.flightarea.model.FlightAreaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * 飞行区域持久化实体。
 *
 * @author DJI DevKit Team
 */
@Entity
@Table(name = "t_flight_area")
public class FlightAreaEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FlightAreaType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FlightAreaStatus status;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(nullable = false)
    private long createdTime;

    @Column(nullable = false, length = 64)
    private String createdBy;

    @Column(nullable = false)
    private long updatedTime;

    @Column(nullable = false, length = 64)
    private String updatedBy;

    /** JPA 要求的无参构造。 */
    public FlightAreaEntity() {
    }

    /**
     * 业务构造函数。
     *
     * @param id          区域 ID
     * @param name        名称
     * @param type        区域类型
     * @param status      启用状态
     * @param contentJson GeoJSON 内容（序列化为 JSON 字符串）
     * @param createdTime 创建时间（Unix 秒）
     * @param createdBy   创建人
     * @param updatedTime 更新时间（Unix 秒）
     * @param updatedBy   更新人
     */
    public FlightAreaEntity(String id, String name, FlightAreaType type, FlightAreaStatus status,
                            String contentJson, long createdTime, String createdBy,
                            long updatedTime, String updatedBy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.contentJson = contentJson;
        this.createdTime = createdTime;
        this.createdBy = createdBy;
        this.updatedTime = updatedTime;
        this.updatedBy = updatedBy;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlightAreaType getType() {
        return type;
    }

    public FlightAreaStatus getStatus() {
        return status;
    }

    public void setStatus(FlightAreaStatus status) {
        this.status = status;
    }

    public String getContentJson() {
        return contentJson;
    }

    public void setContentJson(String contentJson) {
        this.contentJson = contentJson;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
