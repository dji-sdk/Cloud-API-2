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

import com.dji.fh.kernel.spi.event.AbstractKernelEvent;
import com.dji.fh.kernel.spi.event.KernelEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultSdkEventBusTest {

    @Test
    void publish_shouldDelegateToSpringPublisher_whenEventGiven() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DefaultSdkEventBus bus = new DefaultSdkEventBus(publisher);
        KernelEvent event = new TestEvent("kernel-test");

        bus.publish(event);

        verify(publisher).publishEvent(same(event));
    }

    @Test
    void publish_shouldThrowNPE_whenEventIsNull() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        DefaultSdkEventBus bus = new DefaultSdkEventBus(publisher);

        assertThatNullPointerException()
                .isThrownBy(() -> bus.publish(null))
                .withMessageContaining("event");
    }

    @Test
    void constructor_shouldThrowNPE_whenPublisherIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DefaultSdkEventBus(null))
                .withMessageContaining("publisher");
    }

    private static final class TestEvent extends AbstractKernelEvent {
        TestEvent(String source) {
            super(source);
        }
    }
}
