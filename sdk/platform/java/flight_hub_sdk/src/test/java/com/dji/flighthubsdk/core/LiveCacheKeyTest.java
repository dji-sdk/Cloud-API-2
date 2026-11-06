// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.bean.LiveInfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LiveCacheKeyTest {

    @Test
    public void samePayloadAndVideoEquals() {
        LiveInfo a = new LiveInfo("sn", "122-0-0", "normal-0", null);
        LiveInfo b = new LiveInfo("sn", "122-0-0", "normal-0", null);
        assertEquals(LiveCacheKey.of(a), LiveCacheKey.of(b));
    }

    @Test
    public void samePayloadDifferentVideoNotEquals() {
        LiveInfo normal = new LiveInfo("sn", "122-0-0", "normal-0", null);
        LiveInfo zoom = new LiveInfo("sn", "122-0-0", "zoom-0", null);
        assertNotEquals(LiveCacheKey.of(normal), LiveCacheKey.of(zoom));
    }

    @Test
    public void differentPayloadNotEquals() {
        LiveInfo a = new LiveInfo("sn", "122-0-0", "normal-0", null);
        LiveInfo b = new LiveInfo("sn", "176-0-0", "normal-0", null);
        assertNotEquals(LiveCacheKey.of(a), LiveCacheKey.of(b));
    }

    @Test
    public void nullVideoIdUsesEmptyString() {
        LiveInfo info = new LiveInfo("sn", "122-0-0", null, null);
        assertEquals("122-0-0:", LiveCacheKey.of(info));
    }

    @Test
    public void nullParamsUsesEmptyString() {
        assertEquals("", LiveCacheKey.of(null));
    }
}
