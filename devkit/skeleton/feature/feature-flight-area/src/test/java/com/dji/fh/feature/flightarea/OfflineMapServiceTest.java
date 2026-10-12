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

import com.dji.fh.feature.flightarea.model.OfflineMapUpdateState;
import com.dji.fh.feature.flightarea.model.OfflineMapUpdateStatus;
import com.dji.fh.feature.flightarea.service.OfflineMapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 地形障碍物更新服务单元测试。
 *
 * @author DJI DevKit Team
 */
class OfflineMapServiceTest {

    private OfflineMapService service;

    @BeforeEach
    void setUp() {
        service = new OfflineMapService();
    }

    @Test
    void getModelInfo_shouldReturnDefault() {
        var info = service.getModelInfo();
        assertThat(info.disabled()).isFalse();
        assertThat(info.version()).isEqualTo("1.0.0");
        assertThat(info.dataDescription()).contains("高程地图");
    }

    @Test
    void setEnabled_shouldToggleDisabledFlag() {
        service.setEnabled(false);
        assertThat(service.getModelInfo().disabled()).isTrue();

        service.setEnabled(true);
        assertThat(service.getModelInfo().disabled()).isFalse();
    }

    @Test
    void getUpdateState_shouldReturnIdleInitially() {
        OfflineMapUpdateState state = service.getUpdateState();
        assertThat(state.status()).isEqualTo(OfflineMapUpdateStatus.IDLE);
        assertThat(state.progress()).isZero();
    }

    @Test
    void triggerUpdate_shouldReturnTrueAndStartProcessing() {
        boolean result = service.triggerUpdate();
        assertThat(result).isTrue();

        OfflineMapUpdateState state = service.getUpdateState();
        assertThat(state.status()).isIn(OfflineMapUpdateStatus.WAITING, OfflineMapUpdateStatus.PROCESSING);
    }

    @Test
    void triggerUpdate_shouldRejectDuplicateCall() {
        service.triggerUpdate();
        boolean second = service.triggerUpdate();
        assertThat(second).isFalse();
    }

    @Test
    void triggerUpdate_shouldEventuallyFinish() {
        service.triggerUpdate();

        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.FINISH);

        assertThat(service.getUpdateState().progress()).isEqualTo(100);
        assertThat(service.getModelInfo().version()).isNotEqualTo("1.0.0");
    }

    @Test
    void cancelUpdate_shouldStopProcessing() {
        service.triggerUpdate();

        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.PROCESSING);

        boolean cancelled = service.cancelUpdate();
        assertThat(cancelled).isTrue();
        assertThat(service.getUpdateState().status()).isEqualTo(OfflineMapUpdateStatus.CANCEL);
    }

    @Test
    void cancelUpdate_shouldReturnFalseWhenIdle() {
        boolean result = service.cancelUpdate();
        assertThat(result).isFalse();
    }

    @Test
    void resetUpdateState_shouldReturnToIdle() {
        service.triggerUpdate();

        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.FINISH);

        service.resetUpdateState();
        assertThat(service.getUpdateState().status()).isEqualTo(OfflineMapUpdateStatus.IDLE);
        assertThat(service.getUpdateState().progress()).isZero();
    }

    @Test
    void triggerUpdate_shouldAllowRetriggerAfterFinish() {
        service.triggerUpdate();

        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.FINISH);

        boolean retrigger = service.triggerUpdate();
        assertThat(retrigger).isTrue();
    }

    @Test
    void triggerUpdate_shouldBeConcurrencySafe() throws InterruptedException {
        // 两个线程同时触发，只有一个应该成功
        var results = new boolean[2];
        Thread t1 = new Thread(() -> results[0] = service.triggerUpdate());
        Thread t2 = new Thread(() -> results[1] = service.triggerUpdate());

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // 恰好一个 true 一个 false
        assertThat(results[0] ^ results[1]).isTrue();
    }

    @Test
    void shutdown_shouldNotThrow() {
        service.shutdown();
    }
}
