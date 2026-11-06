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

package com.dji.flighthub.mcp.tools.hms;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.HistoryHmsDataResult;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultArgs;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultItem;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultPagination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhHmsTool 的 SDK bean → 结构化 Map 映射单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class FhHmsBeanMapperTest {

    private FhHmsTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhHmsTool(sessionManager, new SdkService());
    }

    // ==================== buildStructuredHistory ====================

    @Test
    void buildStructuredHistory_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildStructuredHistory(null));
    }

    @Test
    void buildStructuredHistory_shouldKeepPaginationNullAndListEmpty_whenSubFieldsMissing() {
        HistoryHmsDataResult result = new HistoryHmsDataResult();

        Map<String, Object> structured = tool.buildStructuredHistory(result);

        assertEquals(List.of("pagination", "list"), List.copyOf(structured.keySet()));
        assertNull(structured.get("pagination"));
        assertEquals(List.of(), structured.get("list"));
    }

    @Test
    void buildStructuredHistory_shouldMapPaginationAndList() {
        HistoryHmsDataResultPagination pagination = new HistoryHmsDataResultPagination();
        pagination.setPageNum(2);
        pagination.setPageSize(50);
        pagination.setTotal(120);

        HistoryHmsDataResult result = new HistoryHmsDataResult();
        result.setPagination(pagination);
        result.setList(List.of(new HistoryHmsDataResultItem()));

        Map<String, Object> structured = tool.buildStructuredHistory(result);

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedPagination = (Map<String, Object>) structured.get("pagination");
        assertEquals(List.of("page_num", "page_size", "total"), List.copyOf(mappedPagination.keySet()));
        assertEquals(2, mappedPagination.get("page_num"));
        assertEquals(50, mappedPagination.get("page_size"));
        assertEquals(120, mappedPagination.get("total"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mappedList = (List<Map<String, Object>>) structured.get("list");
        assertEquals(1, mappedList.size());
    }

    // ==================== buildPagination ====================

    @Test
    void buildPagination_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildPagination(null));
    }

    @Test
    void buildPagination_shouldMapAllFields() {
        HistoryHmsDataResultPagination pagination = new HistoryHmsDataResultPagination();
        pagination.setPageNum(1);
        pagination.setPageSize(20);
        pagination.setTotal(3);

        Map<String, Object> row = tool.buildPagination(pagination);

        assertEquals(List.of("page_num", "page_size", "total"), List.copyOf(row.keySet()));
        assertEquals(1, row.get("page_num"));
        assertEquals(20, row.get("page_size"));
        assertEquals(3, row.get("total"));
    }

    // ==================== buildAlertItems ====================

    @Test
    void buildAlertItems_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildAlertItems(null).isEmpty());
    }

    @Test
    void buildAlertItems_shouldSkipNullElements() {
        List<HistoryHmsDataResultItem> items = new ArrayList<>(Arrays.asList(new HistoryHmsDataResultItem(), null));

        assertEquals(1, tool.buildAlertItems(items).size());
    }

    @Test
    void buildAlertItems_shouldMapAllFieldsInOrder() {
        HistoryHmsDataResultArgs args = new HistoryHmsDataResultArgs();
        args.setAlarmInfo(7);
        args.setComponentIndex(1);
        args.setSensorIndex(2);
        args.setDeviceId("DEV-1");
        args.setDisplayScene(3);
        args.setIgnore(0);
        args.setSenderHostId("HOST-1");
        args.setCreateTimestamp(1_700_000_000_000L);

        HistoryHmsDataResultItem item = new HistoryHmsDataResultItem();
        item.setMongoId("mongo-1");
        item.setSn("DOCK-001");
        item.setLevel(3);
        item.setModule(4);
        item.setCode("CODE-1");
        item.setMessage("电机过温");
        item.setStatus(1);
        item.setDeviceType("dock");
        item.setDomainType("domain");
        item.setInTheSky(0);
        item.setImminent(1);
        item.setDeviceDataCreateTime(1_690_000_000_000L);
        item.setDeviceDataUpdateTime(1_690_000_001_000L);
        item.setCreateTime(1_700_000_000_000L);
        item.setUpdateTime(1_700_000_001_000L);
        item.setArgs(args);

        Map<String, Object> row = tool.buildAlertItems(List.of(item)).get(0);

        assertEquals(
                List.of("alert_id", "sn", "level", "module", "code", "message", "status",
                        "device_type", "domain_type", "in_the_sky", "imminent",
                        "device_create_time", "device_update_time", "create_time", "update_time", "args"),
                List.copyOf(row.keySet()));
        assertEquals("mongo-1", row.get("alert_id"));
        assertEquals("DOCK-001", row.get("sn"));
        assertEquals(3, row.get("level"));
        assertEquals(4, row.get("module"));
        assertEquals("CODE-1", row.get("code"));
        assertEquals("电机过温", row.get("message"));
        assertEquals(1, row.get("status"));
        assertEquals("dock", row.get("device_type"));
        assertEquals("domain", row.get("domain_type"));
        assertEquals(0, row.get("in_the_sky"));
        assertEquals(1, row.get("imminent"));
        assertEquals(1_690_000_000_000L, row.get("device_create_time"));
        assertEquals(1_690_000_001_000L, row.get("device_update_time"));
        assertEquals(1_700_000_000_000L, row.get("create_time"));
        assertEquals(1_700_000_001_000L, row.get("update_time"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedArgs = (Map<String, Object>) row.get("args");
        assertEquals(
                List.of("alarm_info", "component_index", "sensor_index", "device_id",
                        "display_scene", "ignore", "sender_host_id", "create_timestamp"),
                List.copyOf(mappedArgs.keySet()));
        assertEquals(7, mappedArgs.get("alarm_info"));
        assertEquals(1, mappedArgs.get("component_index"));
        assertEquals(2, mappedArgs.get("sensor_index"));
        assertEquals("DEV-1", mappedArgs.get("device_id"));
        assertEquals(3, mappedArgs.get("display_scene"));
        assertEquals(0, mappedArgs.get("ignore"));
        assertEquals("HOST-1", mappedArgs.get("sender_host_id"));
        assertEquals(1_700_000_000_000L, mappedArgs.get("create_timestamp"));
    }

    // ==================== buildAlertArgs ====================

    @Test
    void buildAlertArgs_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildAlertArgs(null));
    }

    @Test
    void buildAlertArgs_shouldMapAllFieldsInOrder() {
        HistoryHmsDataResultArgs args = new HistoryHmsDataResultArgs();
        args.setAlarmInfo(1);
        args.setComponentIndex(2);
        args.setSensorIndex(3);
        args.setDeviceId("DEV-2");
        args.setDisplayScene(4);
        args.setIgnore(1);
        args.setSenderHostId("HOST-2");
        args.setCreateTimestamp(1_700_000_000_000L);

        Map<String, Object> row = tool.buildAlertArgs(args);

        assertEquals(
                List.of("alarm_info", "component_index", "sensor_index", "device_id",
                        "display_scene", "ignore", "sender_host_id", "create_timestamp"),
                List.copyOf(row.keySet()));
        assertEquals(1, row.get("alarm_info"));
        assertEquals(2, row.get("component_index"));
        assertEquals(3, row.get("sensor_index"));
        assertEquals("DEV-2", row.get("device_id"));
        assertEquals(4, row.get("display_scene"));
        assertEquals(1, row.get("ignore"));
        assertEquals("HOST-2", row.get("sender_host_id"));
        assertEquals(1_700_000_000_000L, row.get("create_timestamp"));
    }
}
