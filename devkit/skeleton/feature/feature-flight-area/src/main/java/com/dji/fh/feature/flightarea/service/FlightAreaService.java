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

package com.dji.fh.feature.flightarea.service;

import com.dji.fh.feature.flightarea.entity.FlightAreaEntity;
import com.dji.fh.feature.flightarea.model.CreateFlightAreaRequest;
import com.dji.fh.feature.flightarea.model.FlightAreaItem;
import com.dji.fh.feature.flightarea.model.FlightAreaListRequest;
import com.dji.fh.feature.flightarea.model.FlightAreaStatus;
import com.dji.fh.feature.flightarea.model.UpdateFlightAreaRequest;
import com.dji.fh.feature.flightarea.repository.FlightAreaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 飞行区域 CRUD 服务。
 *
 * <p>使用 JPA 持久化，数据在应用重启后不会丢失。
 */
public class FlightAreaService {

    private static final Logger LOG = LoggerFactory.getLogger(FlightAreaService.class);
    private static final String ID_PREFIX = "fa_";
    private static final int ID_RANDOM_LENGTH = 12;
    private static final String CREATOR_SYSTEM = "system";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private final FlightAreaRepository repository;

    public FlightAreaService(FlightAreaRepository repository) {
        this.repository = repository;
    }

    /**
     * 查询飞行区域列表。
     */
    public List<FlightAreaItem> list(FlightAreaListRequest req) {
        Pageable pageable = PageRequest.of(
                req.safePageIndex() - 1,
                req.safePageSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );

        String nameFilter = (req.name() != null && !req.name().isBlank())
                ? escapeLikeWildcards(req.name()) : null;

        return repository.findByFilters(req.type(), req.status(), nameFilter, pageable)
                .map(this::toItem)
                .getContent();
    }

    /**
     * 创建飞行区域。
     */
    public FlightAreaItem create(CreateFlightAreaRequest req) {
        Objects.requireNonNull(req.name(), "name 不能为空");
        Objects.requireNonNull(req.type(), "type 不能为空");
        Objects.requireNonNull(req.content(), "content 不能为空");

        String id = ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, ID_RANDOM_LENGTH);
        long now = Instant.now().getEpochSecond();

        FlightAreaEntity entity = new FlightAreaEntity(
                id, req.name(), req.type(), FlightAreaStatus.ENABLE,
                serializeContent(req.content()), now, CREATOR_SYSTEM, now, CREATOR_SYSTEM
        );
        repository.save(entity);
        LOG.info("创建飞行区域: id={}, name={}, type={}", id, req.name(), req.type());
        return toItem(entity);
    }

    /**
     * 更新飞行区域。
     */
    @Transactional
    public Optional<FlightAreaItem> update(String id, UpdateFlightAreaRequest req) {
        return repository.findById(id).map(entity -> {
            if (req.name() != null && !req.name().isBlank()) {
                entity.setName(req.name());
            }
            if (req.content() != null) {
                entity.setContentJson(serializeContent(req.content()));
            }
            long now = Instant.now().getEpochSecond();
            entity.setUpdatedTime(now);
            entity.setUpdatedBy(CREATOR_SYSTEM);
            repository.save(entity);
            return toItem(entity);
        });
    }

    /**
     * 删除飞行区域。
     */
    @Transactional
    public boolean delete(String id) {
        return repository.deleteByIdReturningCount(id) > 0;
    }

    private FlightAreaItem toItem(FlightAreaEntity entity) {
        return new FlightAreaItem(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getStatus(),
                deserializeContent(entity.getContentJson()),
                entity.getCreatedTime(),
                entity.getCreatedBy(),
                entity.getUpdatedTime(),
                entity.getUpdatedBy()
        );
    }

    private String serializeContent(Map<String, Object> content) {
        try {
            return MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("content 序列化失败", e);
        }
    }

    private Map<String, Object> deserializeContent(String json) {
        try {
            return MAPPER.readValue(json, MAP_TYPE_REF);
        } catch (JsonProcessingException e) {
            LOG.warn("content 反序列化失败, 返回空 Map: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 转义 LIKE 通配符，确保用户输入作为字面量匹配。
     */
    private static String escapeLikeWildcards(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
