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

import com.dji.fh.feature.user.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户数据访问接口。
 *
 * @author DJI DevKit Team
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 按用户名查找。
     *
     * @param username 用户名
     * @return 用户实体（如存在）
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 检查用户名是否已存在。
     *
     * @param username 用户名
     * @return 已存在返回 true
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已被注册。
     *
     * @param email 邮箱
     * @return 已存在返回 true
     */
    boolean existsByEmail(String email);

    /**
     * 统计指定角色的用户数量。
     *
     * @param role 角色名
     * @return 该角色用户数
     */
    long countByRole(String role);
}
