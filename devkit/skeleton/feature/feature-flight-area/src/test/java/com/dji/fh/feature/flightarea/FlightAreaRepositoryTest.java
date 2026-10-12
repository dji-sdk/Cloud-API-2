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
import com.dji.fh.feature.flightarea.model.FlightAreaStatus;
import com.dji.fh.feature.flightarea.model.FlightAreaType;
import com.dji.fh.feature.flightarea.repository.FlightAreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(FlightAreaRepositoryTest.TestConfig.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class FlightAreaRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(FlightAreaAutoConfiguration.class)
    static class TestConfig {
    }

    @Autowired
    private FlightAreaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        long now = Instant.now().getEpochSecond();
        repository.save(new FlightAreaEntity("fa_1", "深圳限飞区", FlightAreaType.NFZ, FlightAreaStatus.ENABLE, "{}", now, "system", now, "system"));
        repository.save(new FlightAreaEntity("fa_2", "北京作业区", FlightAreaType.DFENCE, FlightAreaStatus.ENABLE, "{}", now, "system", now, "system"));
        repository.save(new FlightAreaEntity("fa_3", "广州禁降区", FlightAreaType.NOLAND, FlightAreaStatus.DISABLE, "{}", now, "system", now, "system"));
    }

    @Test
    void findByFilters_shouldReturnAll_whenNoFilters() {
        Page<FlightAreaEntity> result = repository.findByFilters(null, null, null, PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void findByFilters_shouldFilterByType() {
        Page<FlightAreaEntity> result = repository.findByFilters(FlightAreaType.NFZ, null, null, PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("深圳限飞区");
    }

    @Test
    void findByFilters_shouldFilterByStatus() {
        Page<FlightAreaEntity> result = repository.findByFilters(null, FlightAreaStatus.DISABLE, null, PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("广州禁降区");
    }

    @Test
    void findByFilters_shouldFilterByName() {
        Page<FlightAreaEntity> result = repository.findByFilters(null, null, "深圳", PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("深圳限飞区");
    }

    @Test
    void findByFilters_shouldNotTreatWildcardsInName() {
        long now = Instant.now().getEpochSecond();
        repository.save(new FlightAreaEntity("fa_4", "50%区域", FlightAreaType.NFZ, FlightAreaStatus.ENABLE, "{}", now, "system", now, "system"));
        repository.save(new FlightAreaEntity("fa_5", "500区域", FlightAreaType.NFZ, FlightAreaStatus.ENABLE, "{}", now, "system", now, "system"));

        Page<FlightAreaEntity> result = repository.findByFilters(null, null, "50\\%", PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("50%区域");
    }

    @Test
    void findByFilters_shouldCombineFilters() {
        Page<FlightAreaEntity> result = repository.findByFilters(FlightAreaType.NFZ, FlightAreaStatus.ENABLE, "深圳", PageRequest.of(0, 50));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("fa_1");
    }

    @Test
    void findByFilters_shouldRespectPagination() {
        Page<FlightAreaEntity> page = repository.findByFilters(null, null, null,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id")));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void deleteByIdReturningCount_shouldReturn1_whenExists() {
        int count = repository.deleteByIdReturningCount("fa_1");
        assertThat(count).isEqualTo(1);
        assertThat(repository.findById("fa_1")).isEmpty();
    }

    @Test
    void deleteByIdReturningCount_shouldReturn0_whenNotExists() {
        int count = repository.deleteByIdReturningCount("nonexistent");
        assertThat(count).isZero();
    }
}
