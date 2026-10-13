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

import com.dji.fh.kernel.spi.repository.KeyValueStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryKeyValueStoreTest {

    private KeyValueStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryKeyValueStore(new ObjectMapper());
    }

    @Test
    void get_shouldReturnEmpty_whenKeyAbsent() {
        assertThat(store.get("missing", String.class)).isEmpty();
    }

    @Test
    void put_shouldRoundTripValue_whenSimpleString() {
        store.put("greeting", "hello");

        Optional<String> got = store.get("greeting", String.class);

        assertThat(got).contains("hello");
    }

    @Test
    void put_shouldRoundTripValue_whenComplexRecord() {
        record User(String name, int age) {}
        User alice = new User("alice", 30);

        store.put("user:1", alice);

        Optional<User> got = store.get("user:1", User.class);
        assertThat(got).contains(alice);
    }

    @Test
    void put_shouldOverwrite_whenKeyExists() {
        store.put("k", "v1");
        store.put("k", "v2");

        assertThat(store.get("k", String.class)).contains("v2");
    }

    @Test
    void exists_shouldReturnFalse_whenKeyAbsent() {
        assertThat(store.exists("missing")).isFalse();
    }

    @Test
    void exists_shouldReturnTrue_whenKeyPresent() {
        store.put("k", "v");

        assertThat(store.exists("k")).isTrue();
    }

    @Test
    void delete_shouldRemoveKey_whenPresent() {
        store.put("k", "v");
        store.delete("k");

        assertThat(store.exists("k")).isFalse();
    }

    @Test
    void delete_shouldBeNoOp_whenKeyAbsent() {
        // 不抛即可
        store.delete("missing");

        assertThat(store.exists("missing")).isFalse();
    }

    @Test
    void keys_shouldReturnMatchingPrefix_whenScanned() {
        store.put("user:1", "alice");
        store.put("user:2", "bob");
        store.put("device:1", "drone-a");

        List<String> userKeys = store.keys("user:").toList();

        assertThat(userKeys).containsExactlyInAnyOrder("user:1", "user:2");
    }

    @Test
    void keys_shouldReturnAll_whenEmptyPrefix() {
        store.put("a", "1");
        store.put("b", "2");

        assertThat(store.keys("").toList()).hasSize(2);
    }
}
