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

package com.dji.fh.feature.flightarea;

import com.dji.fh.feature.flightarea.entity.FlightAreaEntity;
import com.dji.fh.feature.flightarea.model.*;
import com.dji.fh.feature.flightarea.repository.FlightAreaRepository;
import com.dji.fh.feature.flightarea.service.FlightAreaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(FlightAreaServiceTest.TestConfig.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class FlightAreaServiceTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(FlightAreaAutoConfiguration.class)
    static class TestConfig {
    }

    @Autowired
    private FlightAreaRepository repository;

    private FlightAreaService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        service = new FlightAreaService(repository);
    }

    @Test
    void create_shouldReturnItemWithGeneratedId() {
        CreateFlightAreaRequest req = new CreateFlightAreaRequest(
                "测试限飞区", FlightAreaType.NFZ, Map.of("type", "Feature")
        );

        FlightAreaItem item = service.create(req);

        assertThat(item.id()).startsWith("fa_");
        assertThat(item.name()).isEqualTo("测试限飞区");
        assertThat(item.type()).isEqualTo(FlightAreaType.NFZ);
        assertThat(item.status()).isEqualTo(FlightAreaStatus.ENABLE);
    }

    @Test
    void create_shouldPersistToDatabase() {
        CreateFlightAreaRequest req = new CreateFlightAreaRequest(
                "持久化测试", FlightAreaType.DFENCE, Map.of("type", "Feature")
        );

        FlightAreaItem item = service.create(req);

        Optional<FlightAreaEntity> entity = repository.findById(item.id());
        assertThat(entity).isPresent();
        assertThat(entity.get().getName()).isEqualTo("持久化测试");
    }

    @Test
    void create_shouldThrow_whenNameIsNull() {
        CreateFlightAreaRequest req = new CreateFlightAreaRequest(null, FlightAreaType.NFZ, Map.of());
        assertThatThrownBy(() -> service.create(req)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void list_shouldFilterByType() {
        service.create(new CreateFlightAreaRequest("A", FlightAreaType.NFZ, Map.of("t", "1")));
        service.create(new CreateFlightAreaRequest("B", FlightAreaType.DFENCE, Map.of("t", "2")));

        List<FlightAreaItem> result = service.list(
                new FlightAreaListRequest(FlightAreaType.NFZ, null, null, null, null)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("A");
    }

    @Test
    void list_shouldFilterByStatus() {
        service.create(new CreateFlightAreaRequest("启用的", FlightAreaType.NFZ, Map.of("t", "1")));

        FlightAreaItem disabledItem = service.create(new CreateFlightAreaRequest("禁用的", FlightAreaType.NFZ, Map.of("t", "2")));
        FlightAreaEntity disabledEntity = repository.findById(disabledItem.id()).orElseThrow();
        disabledEntity.setStatus(FlightAreaStatus.DISABLE);
        repository.save(disabledEntity);

        List<FlightAreaItem> enableResult = service.list(
                new FlightAreaListRequest(null, FlightAreaStatus.ENABLE, null, null, null)
        );
        assertThat(enableResult).hasSize(1);
        assertThat(enableResult.get(0).name()).isEqualTo("启用的");

        List<FlightAreaItem> disableResult = service.list(
                new FlightAreaListRequest(null, FlightAreaStatus.DISABLE, null, null, null)
        );
        assertThat(disableResult).hasSize(1);
        assertThat(disableResult.get(0).name()).isEqualTo("禁用的");
    }

    @Test
    void list_shouldFilterByName() {
        service.create(new CreateFlightAreaRequest("深圳作业区", FlightAreaType.DFENCE, Map.of("t", "1")));
        service.create(new CreateFlightAreaRequest("北京限飞区", FlightAreaType.NFZ, Map.of("t", "2")));

        List<FlightAreaItem> result = service.list(
                new FlightAreaListRequest(null, null, "深圳", null, null)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("深圳作业区");
    }

    @Test
    void update_shouldReturnEmpty_whenIdNotExists() {
        Optional<FlightAreaItem> result = service.update("nonexistent", new UpdateFlightAreaRequest("x", null));
        assertThat(result).isEmpty();
    }

    @Test
    void update_shouldModifyName() {
        FlightAreaItem created = service.create(new CreateFlightAreaRequest("旧名", FlightAreaType.NFZ, Map.of("t", "1")));

        Optional<FlightAreaItem> updated = service.update(created.id(), new UpdateFlightAreaRequest("新名", null));

        assertThat(updated).isPresent();
        assertThat(updated.get().name()).isEqualTo("新名");
    }

    @Test
    void delete_shouldReturnTrue_whenExists() {
        FlightAreaItem created = service.create(new CreateFlightAreaRequest("x", FlightAreaType.NFZ, Map.of("t", "1")));
        assertThat(service.delete(created.id())).isTrue();
    }

    @Test
    void delete_shouldReturnFalse_whenNotExists() {
        assertThat(service.delete("nonexistent")).isFalse();
    }
}
