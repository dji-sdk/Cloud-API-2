// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import org.junit.Test;

public class SeiInfoEqualsTest {

    @Test
    public void eisSeiInfoDefaultConstructedInstancesAreEqual() {
        EisSeiInfo a = new EisSeiInfo();
        EisSeiInfo b = new EisSeiInfo();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void aiSeiInfoDefaultConstructedInstancesAreEqual() {
        AiSeiInfo a = new AiSeiInfo();
        AiSeiInfo b = new AiSeiInfo();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void eisSeiInfoEqualsHandlesMixedNullFields() {
        EisSeiInfo a = new EisSeiInfo();
        a.setVersion(1);
        a.setLeftTop(new CamCropPos(10, 20));

        EisSeiInfo b = new EisSeiInfo();
        b.setVersion(1);
        b.setLeftTop(new CamCropPos(10, 20));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void aiSeiInfoEqualsHandlesExplicitNullTargets() {
        AiSeiInfo a = new AiSeiInfo();
        a.setTargets(null);
        AiSeiInfo b = new AiSeiInfo();
        b.setTargets(null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equalsIsReflexiveAndRejectsNullAndOtherTypes() {
        EisSeiInfo eis = new EisSeiInfo();
        AiSeiInfo ai = new AiSeiInfo();

        assertTrue(eis.equals(eis));
        assertTrue(ai.equals(ai));
        assertFalse(eis.equals(null));
        assertFalse(ai.equals(null));
        assertFalse(eis.equals(ai));
        assertFalse(ai.equals(eis));
    }

    @Test
    public void equalsDistinguishesDifferentValues() {
        EisSeiInfo eisA = new EisSeiInfo();
        eisA.setVersion(1);
        EisSeiInfo eisB = new EisSeiInfo();
        eisB.setVersion(2);
        assertFalse(eisA.equals(eisB));

        AiSeiInfo aiA = new AiSeiInfo(Collections.singletonList(new AiDetectionTarget()));
        AiSeiInfo aiB = new AiSeiInfo(Collections.emptyList());
        assertFalse(aiA.equals(aiB));
    }
}
