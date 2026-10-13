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

package com.dji.fh.kernel.impl.event;

import com.dji.fh.kernel.spi.bridge.BridgeErrorHandler;
import com.dji.fh.kernel.spi.bridge.EventBridge;
import com.dji.fh.kernel.spi.event.AbstractKernelEvent;
import com.dji.fh.kernel.spi.event.KernelEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BridgeEventDispatcherTest {

    @Test
    void dispatch_shouldNoOp_whenNoBridges() {
        BridgeEventDispatcher dispatcher = new BridgeEventDispatcher(List.of(), null);

        // 不抛即可
        dispatcher.dispatch(new TestEvent("src"));
    }

    @Test
    void dispatch_shouldSendToInterestedBridge_whenEventMatches() {
        EventBridge bridge = mock(EventBridge.class);
        when(bridge.id()).thenReturn("test");
        when(bridge.outboundInterest()).thenReturn(Set.of(TestEvent.class));
        BridgeEventDispatcher dispatcher = new BridgeEventDispatcher(List.of(bridge), null);
        TestEvent event = new TestEvent("src");

        dispatcher.dispatch(event);

        verify(bridge).send(same(event));
    }

    @Test
    void dispatch_shouldSkipBridge_whenInterestDoesNotMatch() {
        EventBridge bridge = mock(EventBridge.class);
        when(bridge.id()).thenReturn("test");
        when(bridge.outboundInterest()).thenReturn(Set.of(OtherEvent.class));
        BridgeEventDispatcher dispatcher = new BridgeEventDispatcher(List.of(bridge), null);

        dispatcher.dispatch(new TestEvent("src"));

        verify(bridge, never()).send(any());
    }

    @Test
    void dispatch_shouldCallErrorHandler_whenBridgeThrows() {
        EventBridge bridge = mock(EventBridge.class);
        when(bridge.id()).thenReturn("failing");
        when(bridge.outboundInterest()).thenReturn(Set.of(TestEvent.class));
        RuntimeException boom = new RuntimeException("broker down");
        doThrow(boom).when(bridge).send(any());
        BridgeErrorHandler errorHandler = mock(BridgeErrorHandler.class);
        BridgeEventDispatcher dispatcher = new BridgeEventDispatcher(
                List.of(bridge), errorHandler);
        TestEvent event = new TestEvent("src");

        dispatcher.dispatch(event);

        verify(errorHandler).onSendFailure(eq("failing"), same(event), same(boom));
    }

    @Test
    void dispatch_shouldNotPropagateException_whenNoErrorHandler() {
        EventBridge bridge = mock(EventBridge.class);
        when(bridge.id()).thenReturn("failing");
        when(bridge.outboundInterest()).thenReturn(Set.of(TestEvent.class));
        doThrow(new RuntimeException("broker down")).when(bridge).send(any());
        BridgeEventDispatcher dispatcher = new BridgeEventDispatcher(List.of(bridge), null);

        // 异常被吞 + log warn；不抛
        dispatcher.dispatch(new TestEvent("src"));
    }

    @Test
    void dispatch_shouldFanOutToMultipleInterestedBridges_whenMultiplePresent() {
        EventBridge b1 = mock(EventBridge.class);
        EventBridge b2 = mock(EventBridge.class);
        when(b1.id()).thenReturn("b1");
        when(b2.id()).thenReturn("b2");
        when(b1.outboundInterest()).thenReturn(Set.of(TestEvent.class));
        when(b2.outboundInterest()).thenReturn(Set.of(TestEvent.class));
        BridgeEventDispatcher dispatcher = new BridgeEventDispatcher(List.of(b1, b2), null);
        TestEvent event = new TestEvent("src");

        dispatcher.dispatch(event);

        ArgumentCaptor<KernelEvent> cap1 = ArgumentCaptor.forClass(KernelEvent.class);
        ArgumentCaptor<KernelEvent> cap2 = ArgumentCaptor.forClass(KernelEvent.class);
        verify(b1).send(cap1.capture());
        verify(b2).send(cap2.capture());
        assertThat(cap1.getValue()).isSameAs(event);
        assertThat(cap2.getValue()).isSameAs(event);
    }

    private static final class TestEvent extends AbstractKernelEvent {
        TestEvent(String source) {
            super(source);
        }
    }

    private static final class OtherEvent extends AbstractKernelEvent {
        OtherEvent(String source) {
            super(source);
        }
    }
}
