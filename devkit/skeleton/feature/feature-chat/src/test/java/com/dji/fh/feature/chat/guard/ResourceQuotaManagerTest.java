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

package com.dji.fh.feature.chat.guard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link ResourceQuotaManager} 单元测试。
 *
 * @author DJI DevKit Team
 */
class ResourceQuotaManagerTest {

    // -------------------------------------------------------------------------
    // 频率限制
    // -------------------------------------------------------------------------

    @Test
    void tryAcquire_shouldPass_whenUnderLimit() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(5, 3600_000, 3, 0);
        GuardResult result = mgr.tryAcquire("user-1");
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void tryAcquire_shouldBlock_whenRateLimitExceeded() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(3, 3600_000, 10, 0);
        mgr.tryAcquire("user-1");
        mgr.tryAcquire("user-1");
        mgr.tryAcquire("user-1");
        GuardResult result = mgr.tryAcquire("user-1");
        assertThat(result.allowed()).isFalse();
        assertThat(result.errorCode()).isEqualTo(GuardResult.CODE_RATE_LIMITED);
    }

    @Test
    void tryAcquire_shouldIsolateUsers() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(2, 3600_000, 10, 0);
        mgr.tryAcquire("user-a");
        mgr.tryAcquire("user-a");
        GuardResult resultA = mgr.tryAcquire("user-a");
        GuardResult resultB = mgr.tryAcquire("user-b");
        assertThat(resultA.allowed()).isFalse();
        assertThat(resultB.allowed()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 并发限制（原子 CAS 模式）
    // -------------------------------------------------------------------------

    @Test
    void tryStreamStart_shouldBlock_whenConcurrentLimitReached() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 2, 0);
        assertThat(mgr.tryStreamStart("user-1").allowed()).isTrue();
        assertThat(mgr.tryStreamStart("user-1").allowed()).isTrue();
        GuardResult result = mgr.tryStreamStart("user-1");
        assertThat(result.allowed()).isFalse();
        assertThat(result.errorCode()).isEqualTo(GuardResult.CODE_CONCURRENT_LIMITED);
    }

    @Test
    void streamEnded_shouldDecrementConcurrency() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 1, 0);
        mgr.tryStreamStart("user-1");
        mgr.streamEnded("user-1", 100);
        GuardResult result = mgr.tryStreamStart("user-1");
        assertThat(result.allowed()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Token 额度
    // -------------------------------------------------------------------------

    @Test
    void checkTokenBudget_shouldPass_whenUnderLimit() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 3, 1000);
        mgr.tryAcquire("user-1");
        mgr.tryStreamStart("user-1");
        mgr.streamEnded("user-1", 500);
        GuardResult result = mgr.checkTokenBudget("user-1");
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void checkTokenBudget_shouldBlock_whenExceeded() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 3, 1000);
        mgr.tryAcquire("user-1");
        mgr.tryStreamStart("user-1");
        mgr.streamEnded("user-1", 1000);
        GuardResult result = mgr.checkTokenBudget("user-1");
        assertThat(result.allowed()).isFalse();
        assertThat(result.errorCode()).isEqualTo(GuardResult.CODE_TOKEN_BUDGET_EXCEEDED);
    }

    @Test
    void checkTokenBudget_shouldPass_whenLimitIsZero() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 3, 0);
        GuardResult result = mgr.checkTokenBudget("user-1");
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void checkTokenBudget_shouldAccumulateAcrossStreams() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 3, 500);
        mgr.tryAcquire("user-1");
        mgr.tryStreamStart("user-1");
        mgr.streamEnded("user-1", 300);
        mgr.tryStreamStart("user-1");
        mgr.streamEnded("user-1", 200);
        GuardResult result = mgr.checkTokenBudget("user-1");
        assertThat(result.allowed()).isFalse();
    }

    // -------------------------------------------------------------------------
    // 边界条件
    // -------------------------------------------------------------------------

    @Test
    void tryAcquire_shouldPass_whenPrincipalIsNullOrBlank() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(1, 3600_000, 1, 100);
        assertThat(mgr.tryAcquire(null).allowed()).isTrue();
        assertThat(mgr.tryAcquire("").allowed()).isTrue();
        assertThat(mgr.tryAcquire("   ").allowed()).isTrue();
    }

    @Test
    void tryStreamStart_shouldHandleNullPrincipal() {
        ResourceQuotaManager mgr = new ResourceQuotaManager(100, 3600_000, 3, 0);
        assertThat(mgr.tryStreamStart(null).allowed()).isTrue();
        mgr.streamEnded(null, 100);
    }
}
