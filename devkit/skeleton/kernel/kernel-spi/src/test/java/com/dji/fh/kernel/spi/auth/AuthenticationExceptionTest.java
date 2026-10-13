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

package com.dji.fh.kernel.spi.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationExceptionTest {

    @Test
    void constructor_withMessage_shouldStoreMessage() {
        AuthenticationException ex = new AuthenticationException("token expired");

        assertThat(ex.getMessage()).isEqualTo("token expired");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructor_withMessageAndCause_shouldStoreBoth() {
        RuntimeException cause = new RuntimeException("underlying error");
        AuthenticationException ex = new AuthenticationException("auth failed", cause);

        assertThat(ex.getMessage()).isEqualTo("auth failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
