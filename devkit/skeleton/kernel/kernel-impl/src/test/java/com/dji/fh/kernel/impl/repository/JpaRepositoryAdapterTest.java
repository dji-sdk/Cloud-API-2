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
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * JpaRepositoryAdapter 用 H2 in-memory 跑的集成测试。
 *
 * @author DJI DevKit Team
 */
@DataJpaTest
@EntityScan(basePackageClasses = JpaRepositoryAdapterTest.SampleItem.class)
@Import(JpaRepositoryAdapterTest.TestConfig.class)
class JpaRepositoryAdapterTest {

    @Autowired
    private SampleItemRepository repo;

    @Test
    void save_shouldReturnPersistedEntity_whenNew() {
        SampleItem saved = repo.save(new SampleItem("foo"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("foo");
    }

    @Test
    void findById_shouldReturnEntity_whenExists() {
        SampleItem saved = repo.save(new SampleItem("bar"));

        Optional<SampleItem> got = repo.findById(saved.getId());

        assertThat(got).map(SampleItem::getName).contains("bar");
    }

    @Test
    void findById_shouldReturnEmpty_whenMissing() {
        assertThat(repo.findById(99999L)).isEmpty();
    }

    @Test
    void findAll_shouldReturnAll_whenMultiplePersisted() {
        repo.save(new SampleItem("a"));
        repo.save(new SampleItem("b"));
        repo.save(new SampleItem("c"));

        List<SampleItem> all = repo.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    void findAll_paged_shouldReturnPage_whenAcrossPages() {
        for (int i = 0; i < 7; i++) {
            repo.save(new SampleItem("x" + i));
        }

        Page<SampleItem> p0 = repo.findAll(0, 3);
        Page<SampleItem> p2 = repo.findAll(2, 3);

        assertThat(p0.content()).hasSize(3);
        assertThat(p0.totalElements()).isEqualTo(7L);
        assertThat(p2.content()).hasSize(1);
    }

    @Test
    void findAll_paged_shouldRejectNegativePage() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> repo.findAll(-1, 10));
    }

    @Test
    void findAll_paged_shouldRejectZeroSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> repo.findAll(0, 0));
    }

    @Test
    void deleteById_shouldRemove_whenExists() {
        SampleItem saved = repo.save(new SampleItem("gone"));

        repo.deleteById(saved.getId());

        assertThat(repo.existsById(saved.getId())).isFalse();
    }

    @Test
    void deleteById_shouldBeNoOp_whenMissing() {
        repo.deleteById(99999L);  // 不抛
    }

    @Test
    void count_shouldReturnZero_whenEmpty() {
        assertThat(repo.count()).isZero();
    }

    @Test
    void count_shouldReturnExact_whenPersisted() {
        repo.save(new SampleItem("a"));
        repo.save(new SampleItem("b"));

        assertThat(repo.count()).isEqualTo(2L);
    }

    /**
     * 测试用实体。
     */
    @Entity(name = "SampleItem")
    public static class SampleItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        public SampleItem() {
        }

        public SampleItem(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 测试 SampleItem 的 repository。
     */
    public static class SampleItemRepository extends JpaRepositoryAdapter<SampleItem, Long> {
        public SampleItemRepository(EntityManager em) {
            super(em, SampleItem.class);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        SampleItemRepository sampleItemRepository(EntityManager em) {
            return new SampleItemRepository(em);
        }
    }
}
