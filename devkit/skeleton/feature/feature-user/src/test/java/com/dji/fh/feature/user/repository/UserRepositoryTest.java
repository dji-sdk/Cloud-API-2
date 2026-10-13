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

package com.dji.fh.feature.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.user.entity.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

/**
 * UserRepository 集成测试（H2 内存库）。
 *
 * @author DJI DevKit Team
 */
@DataJpaTest
@ContextConfiguration(classes = UserRepositoryTest.Config.class)
class UserRepositoryTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.dji.fh.feature.user.entity")
    @EnableJpaRepositories(basePackages = "com.dji.fh.feature.user.repository")
    static class Config {
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_shouldReturnUser_whenExists() {
        UserEntity user = new UserEntity("testuser", "$2a$10$hash", "Test", null, "user");
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        Optional<UserEntity> found = userRepository.findByUsername("noone");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenExists() {
        userRepository.save(new UserEntity("admin", "$2a$10$hash", "Admin", null, "admin"));
        assertThat(userRepository.existsByUsername("admin")).isTrue();
    }

    @Test
    void count_shouldReturnZero_whenEmpty() {
        assertThat(userRepository.count()).isZero();
    }
}
