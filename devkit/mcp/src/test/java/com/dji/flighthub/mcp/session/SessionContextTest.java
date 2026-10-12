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

package com.dji.flighthub.mcp.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionContext 单元测试。
 */
class SessionContextTest {

    @Test
    void constructor_shouldInitializeCorrectly() {
        SessionContext ctx = new SessionContext("sess_abc", "uuid-123", "MyWorkspace", 2);

        assertEquals("sess_abc", ctx.getSessionId());
        assertEquals("uuid-123", ctx.getWorkspaceUuid());
        assertEquals("MyWorkspace", ctx.getWorkspaceName());
        assertEquals(2, ctx.getMaxSecurityLevel());
        assertNotNull(ctx.getCreatedAt());
        assertNotNull(ctx.getLastActivityAt());
    }

    @Test
    void touch_shouldUpdateLastActivityAt() throws InterruptedException {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        var before = ctx.getLastActivityAt();
        Thread.sleep(10);
        ctx.touch();
        assertTrue(ctx.getLastActivityAt().isAfter(before));
    }

    @Test
    void isExpired_shouldReturnFalseWhenFresh() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        assertFalse(ctx.isExpired(1800));
    }

    @Test
    void isExpired_shouldReturnTrueWhenTimeout() throws InterruptedException {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        Thread.sleep(20);
        // 使用极短超时（0秒）模拟过期
        assertTrue(ctx.isExpired(0));
    }

    // ==================== Control Authority ====================

    @Test
    void controlAuthority_initiallyNone() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        assertFalse(ctx.hasControlAuthority());
        assertNull(ctx.getActiveControlDeviceSn());
    }

    @Test
    void controlAuthority_setAndClear() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        ctx.setActiveControlDevice("DRONE-001");

        assertTrue(ctx.hasControlAuthority());
        assertTrue(ctx.hasControlOf("DRONE-001"));
        assertFalse(ctx.hasControlOf("DRONE-002"));
        assertEquals("DRONE-001", ctx.getActiveControlDeviceSn());

        ctx.clearControlDevice();
        assertFalse(ctx.hasControlAuthority());
    }

    // ==================== DRC Link ====================

    @Test
    void drcLink_initiallyNone() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        assertFalse(ctx.hasDrcLink());
    }

    @Test
    void drcLink_setAndClear() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        ctx.setActiveDrcDevice("DOCK-001");

        assertTrue(ctx.hasDrcLink());
        assertTrue(ctx.hasDrcLinkTo("DOCK-001"));
        assertFalse(ctx.hasDrcLinkTo("DOCK-999"));

        ctx.clearDrcDevice();
        assertFalse(ctx.hasDrcLink());
    }

    // ==================== Listen IDs ====================

    @Test
    void listenIds_addAndRemove() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        assertEquals(0, ctx.getActiveListenIds().size());

        ctx.addListenId(1);
        ctx.addListenId(2);
        ctx.addListenId(3);
        assertEquals(3, ctx.getActiveListenIds().size());

        ctx.removeListenId(2);
        assertEquals(2, ctx.getActiveListenIds().size());
        assertFalse(ctx.getActiveListenIds().contains(2));
    }

    @Test
    void listenIds_shouldReturnUnmodifiableSet() {
        SessionContext ctx = new SessionContext("sess_1", "u", "n", 3);
        ctx.addListenId(1);

        assertThrows(UnsupportedOperationException.class, () -> {
            ctx.getActiveListenIds().add(99);
        });
    }

    // ==================== Flight Phase ====================

    @Test
    void flightPhase_shouldDefaultToIdle() {
        SessionContext session = new SessionContext("sess_1", "u", "n", 3);
        assertEquals(FlightPhase.IDLE, session.getFlightPhase());
    }

    @Test
    void setActiveControlDevice_shouldRecordTimestamp() {
        SessionContext session = new SessionContext("sess_1", "u", "n", 3);
        session.setActiveControlDevice("DRONE-001");
        assertNotNull(session.getControlAcquiredAt());
        assertTrue(session.hasControlOf("DRONE-001"));
    }

    @Test
    void clearControlDevice_shouldResetTimestamp() {
        SessionContext session = new SessionContext("sess_1", "u", "n", 3);
        session.setActiveControlDevice("DRONE-001");
        session.clearControlDevice();
        assertNull(session.getControlAcquiredAt());
        assertFalse(session.hasControlAuthority());
    }

    @Test
    void flightPhase_shouldTrackStateTransitions() {
        SessionContext session = new SessionContext("sess_1", "u", "n", 3);
        session.setFlightPhase(FlightPhase.TAKING_OFF);
        assertEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
        session.setFlightPhase(FlightPhase.HOVERING);
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }
}
