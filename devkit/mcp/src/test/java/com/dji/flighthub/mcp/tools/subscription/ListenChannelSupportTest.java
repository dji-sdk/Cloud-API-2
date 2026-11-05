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

package com.dji.flighthub.mcp.tools.subscription;

import com.dji.flighthub.mcp.session.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ListenChannelSupport 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class ListenChannelSupportTest {

    private static final Logger log = LoggerFactory.getLogger(ListenChannelSupportTest.class);

    private SubscriptionBuffer buffer;
    private ListenChannelSupport support;
    private SessionContext session;

    @BeforeEach
    void setUp() {
        buffer = new SubscriptionBuffer();
        support = new ListenChannelSupport(buffer);
        session = new SessionContext("sess_a", "u", "w", 3);
    }

    @Test
    void registryKey_shouldComposeNamespaceScopeChannel() {
        assertEquals("psdk_subscribe::DRONE-001::device_list",
                ListenChannelSupport.registryKey("psdk_subscribe", "DRONE-001", "device_list"));
    }

    @Test
    void startChannel_shouldAppendMockEventAndRegister_whenMock() {
        String key = ListenChannelSupport.registryKey("ns", "DRONE-001", "device_list");
        String scope = SubscriptionBuffer.scope(session, "DRONE-001");

        int listenId = support.startChannel(session, key, "device_list:DRONE-001", scope, "device_list", true,
                () -> Map.of("psdk_index", 1), onEvent -> -99, cancelId -> { });

        assertTrue(listenId > 0);
        assertTrue(session.getListenRegistry().containsKey(key));
        SubscriptionBuffer.ReadResult result = buffer.read(scope, "device_list", 0, 10);
        assertEquals(1, result.events().size());
        assertEquals(1, result.events().get(0).get("psdk_index"));
        assertTrue(result.events().get(0).containsKey("_ts"));
    }

    @Test
    void startChannel_shouldInvokeRealListenFnAndRegister_whenNotMock() {
        String key = ListenChannelSupport.registryKey("ns", "DRONE-001", "device_list");
        String scope = SubscriptionBuffer.scope(session, "DRONE-001");
        AtomicInteger cancelledWith = new AtomicInteger(-1);

        int listenId = support.startChannel(session, key, "device_list:DRONE-001", scope, "device_list", false,
                () -> Map.of("psdk_index", 1),
                onEvent -> {
                    onEvent.accept(Map.of("psdk_index", 7));
                    return 42;
                },
                cancelId -> cancelledWith.set(cancelId));

        assertEquals(42, listenId);
        SubscriptionBuffer.ReadResult result = buffer.read(scope, "device_list", 0, 10);
        assertEquals(1, result.events().size());
        assertEquals(7, result.events().get(0).get("psdk_index"));

        session.cancelListen(key);
        assertEquals(42, cancelledWith.get());
        assertTrue(buffer.read(scope, "device_list", 0, 10).events().isEmpty());
    }

    @Test
    void startChannel_shouldNotRegister_whenRealListenFnFails() {
        String key = ListenChannelSupport.registryKey("ns", "DRONE-001", "device_list");
        String scope = SubscriptionBuffer.scope(session, "DRONE-001");

        int listenId = support.startChannel(session, key, "device_list:DRONE-001", scope, "device_list", false,
                () -> Map.of(), onEvent -> -1, cancelId -> { });

        assertEquals(-1, listenId);
        assertFalse(session.getListenRegistry().containsKey(key));
    }

    @Test
    void readChannel_shouldReportActive_whenRegistered() {
        String key = ListenChannelSupport.registryKey("ns", "DRONE-001", "device_list");
        String scope = SubscriptionBuffer.scope(session, "DRONE-001");
        session.registerListen(key, "label", () -> { });
        buffer.append(scope, "device_list", Map.of("v", 1));

        Map<String, Object> stream = support.readChannel(session, key, scope, "device_list", 0, 10);

        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));
        assertEquals(0L, stream.get("dropped"));
        assertFalse((boolean) stream.get("truncated"));
    }

    @Test
    void readChannel_shouldReportInactive_whenNotRegistered() {
        String key = ListenChannelSupport.registryKey("ns", "DRONE-001", "device_list");
        String scope = SubscriptionBuffer.scope(session, "DRONE-001");

        Map<String, Object> stream = support.readChannel(session, key, scope, "device_list", 0, 10);

        assertEquals(false, stream.get("active"));
        assertEquals(0, stream.get("count"));
    }

    @Test
    void stopChannels_shouldPartitionStoppedNotActiveAndFailed() {
        String activeKey = ListenChannelSupport.registryKey("ns", "D", "a");
        String failingKey = ListenChannelSupport.registryKey("ns", "D", "b");
        session.registerListen(activeKey, "a", () -> { });
        session.registerListen(failingKey, "b", () -> {
            throw new IllegalStateException("cancel failed");
        });

        ListenChannelSupport.StopResult result = support.stopChannels(session, List.of("a", "b", "c"),
                channel -> ListenChannelSupport.registryKey("ns", "D", channel), log);

        assertEquals(List.of("a"), result.stopped());
        assertEquals(List.of("c"), result.notActive());
        assertEquals(List.of("b"), result.failed());
    }

    @Test
    void activeChannels_shouldFilterByPrefixAndStripIt() {
        session.registerListen(ListenChannelSupport.registryKey("ns", "D", "device_list"), "l1", () -> { });
        session.registerListen(ListenChannelSupport.registryKey("ns", "D", "state_summary"), "l2", () -> { });
        session.registerListen(ListenChannelSupport.registryKey("ns", "OTHER", "device_list"), "l3", () -> { });

        List<String> active = support.activeChannels(session, "ns::D::");

        assertEquals(List.of("device_list", "state_summary"), active);
    }

    @Test
    void cancelQuietly_shouldNotThrow_whenCancelActionFails() {
        String key = ListenChannelSupport.registryKey("ns", "D", "a");
        session.registerListen(key, "a", () -> {
            throw new IllegalStateException("boom");
        });

        support.cancelQuietly(session, key, log);

        assertFalse(session.getListenRegistry().containsKey(key));
    }
}
