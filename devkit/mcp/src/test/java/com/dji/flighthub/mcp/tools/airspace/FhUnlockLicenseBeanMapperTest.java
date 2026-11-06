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

package com.dji.flighthub.mcp.tools.airspace;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResult;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResultItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhUnlockLicenseTool 的 SDK bean → 结构化 Map 映射单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class FhUnlockLicenseBeanMapperTest {

    private FhUnlockLicenseTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhUnlockLicenseTool(sessionManager, new SdkService());
    }

    // ==================== buildStructuredCheckResult ====================

    @Test
    void buildStructuredCheckResult_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildStructuredCheckResult(null));
    }

    @Test
    void buildStructuredCheckResult_shouldReturnEmptyLists_whenSubFieldsNull() {
        Map<String, Object> structured = tool.buildStructuredCheckResult(new CheckUnlockLicenseResult());

        assertEquals(List.of("err_file_names", "err_cert_mismatch", "items"),
                List.copyOf(structured.keySet()));
        assertEquals(List.of(), structured.get("err_file_names"));
        assertEquals(List.of(), structured.get("err_cert_mismatch"));
        assertEquals(List.of(), structured.get("items"));
    }

    @Test
    void buildStructuredCheckResult_shouldMapAllFields() {
        CheckUnlockLicenseResultItem item = new CheckUnlockLicenseResultItem();
        item.setFileName("license.json");
        item.setName("测试证书");
        item.setType("area");
        item.setBeginTime(1_700_000_000_000L);
        item.setEndTime(1_800_000_000_000L);
        item.setUrl("https://example.com/license.json");

        CheckUnlockLicenseResult result = new CheckUnlockLicenseResult();
        result.setErrFileNames(List.of("bad-1.json", "bad-2.json"));
        result.setErrCertMismatch(List.of("mismatch-1"));
        result.setItems(List.of(item));

        Map<String, Object> structured = tool.buildStructuredCheckResult(result);

        assertEquals(List.of("bad-1.json", "bad-2.json"), structured.get("err_file_names"));
        assertEquals(List.of("mismatch-1"), structured.get("err_cert_mismatch"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mappedItems = (List<Map<String, Object>>) structured.get("items");
        assertEquals(1, mappedItems.size());
        assertEquals("license.json", mappedItems.get(0).get("file_name"));
        assertEquals("测试证书", mappedItems.get(0).get("name"));
        assertEquals("area", mappedItems.get(0).get("type"));
        assertEquals(1_700_000_000_000L, mappedItems.get(0).get("begin_time"));
        assertEquals(1_800_000_000_000L, mappedItems.get(0).get("end_time"));
        assertEquals("https://example.com/license.json", mappedItems.get(0).get("url"));
    }

    // ==================== buildCheckItems ====================

    @Test
    void buildCheckItems_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildCheckItems(null).isEmpty());
    }

    @Test
    void buildCheckItems_shouldSkipNullElements() {
        List<CheckUnlockLicenseResultItem> items =
                new ArrayList<>(Arrays.asList(new CheckUnlockLicenseResultItem(), null));

        assertEquals(1, tool.buildCheckItems(items).size());
    }

    @Test
    void buildCheckItems_shouldMapAllFieldsInOrder() {
        CheckUnlockLicenseResultItem item = new CheckUnlockLicenseResultItem();
        item.setFileName("license.json");
        item.setName("测试证书");
        item.setType("flight");
        item.setBeginTime(1_700_000_000_000L);
        item.setEndTime(1_800_000_000_000L);
        item.setUrl("https://example.com/license.json");

        Map<String, Object> row = tool.buildCheckItems(List.of(item)).get(0);

        assertEquals(List.of("file_name", "name", "type", "begin_time", "end_time", "url"),
                List.copyOf(row.keySet()));
        assertEquals("license.json", row.get("file_name"));
        assertEquals("测试证书", row.get("name"));
        assertEquals("flight", row.get("type"));
        assertEquals(1_700_000_000_000L, row.get("begin_time"));
        assertEquals(1_800_000_000_000L, row.get("end_time"));
        assertEquals("https://example.com/license.json", row.get("url"));
    }
}
