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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthubsdk.bean.HmsArgs;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsDataItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDeviceManageTool 的 HMS SDK bean → 结构化 Map 映射单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDeviceManageBeanMapperTest {

    private FhDeviceManageTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhDeviceManageTool(sessionManager, new SdkService(), new ListenChannelSupport(new SubscriptionBuffer()));
    }

    // ==================== buildHmsAlerts ====================

    @Test
    void buildHmsAlerts_shouldReturnEmptyList_whenDataNull() {
        assertTrue(tool.buildHmsAlerts(null).isEmpty());
    }

    @Test
    void buildHmsAlerts_shouldReturnEmptyList_whenItemsNull() {
        HmsData data = new HmsData();
        data.setHmsDataItems(null);

        assertTrue(tool.buildHmsAlerts(data).isEmpty());
    }

    @Test
    void buildHmsAlerts_shouldSkipNullItemsAndKeepSubItemsEmptyList() {
        HmsDataItem item = new HmsDataItem();
        List<HmsDataItem> items = new ArrayList<>(Arrays.asList(item, null));
        HmsData data = new HmsData();
        data.setHmsDataItems(items);

        List<Map<String, Object>> alerts = tool.buildHmsAlerts(data);

        assertEquals(1, alerts.size());
        assertEquals(List.of("sub_items"), List.copyOf(alerts.get(0).keySet()));
        assertEquals(List.of(), alerts.get(0).get("sub_items"));
    }

    @Test
    void buildHmsAlerts_shouldMapSubItems() {
        HmsDataBasics sub = new HmsDataBasics();
        sub.setCode("sub-code");
        sub.setLevel(2);

        HmsDataItem item = new HmsDataItem();
        item.setBasics(createBasics());
        item.setSubHmsData(List.of(sub));
        HmsData data = new HmsData();
        data.setHmsDataItems(List.of(item));

        Map<String, Object> alert = tool.buildHmsAlerts(data).get(0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> subItems = (List<Map<String, Object>>) alert.get("sub_items");
        assertEquals(1, subItems.size());
        assertEquals("sub-code", subItems.get(0).get("code"));
        assertEquals(2, subItems.get(0).get("level"));
    }

    @Test
    void buildHmsAlerts_shouldSkipNullSubItems() {
        List<HmsDataBasics> subs = new ArrayList<>(Arrays.asList(null, new HmsDataBasics()));
        HmsDataItem item = new HmsDataItem();
        item.setSubHmsData(subs);
        HmsData data = new HmsData();
        data.setHmsDataItems(List.of(item));

        assertEquals(1, tool.buildHmsBasicsList(subs).size());
        assertEquals(1, tool.buildHmsAlerts(data).size());
    }

    // ==================== buildHmsBasics ====================

    @Test
    void buildHmsBasics_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildHmsBasics(null));
    }

    @Test
    void buildHmsBasics_shouldMapAllFieldsAndArgs() {
        HmsArgs args = new HmsArgs();
        args.setComponentIndex(1);
        args.setSensorIndex(2);
        args.setDeviceId("DEV-1");
        args.setSenderHostId("HOST-1");
        args.setIgnore(0);
        args.setAlarmInfo(42);
        args.setCreateTimestamp(1_700_000_000_000L);
        args.setDisplayScene(3);

        HmsDataBasics basics = createBasics();
        basics.setArgs(args);

        Map<String, Object> row = tool.buildHmsBasics(basics);

        assertEquals(
                List.of("level", "module_id", "code", "device_type", "domain_type", "in_the_sky",
                        "imminent", "status_key", "joint_key", "id", "parent_id", "args"),
                List.copyOf(row.keySet()));
        assertEquals(1, row.get("level"));
        assertEquals(2, row.get("module_id"));
        assertEquals("CODE-1", row.get("code"));
        assertEquals("drone", row.get("device_type"));
        assertEquals("domain", row.get("domain_type"));
        assertEquals(1, row.get("in_the_sky"));
        assertEquals(1, row.get("imminent"));
        assertEquals("status-key", row.get("status_key"));
        assertEquals("joint-key", row.get("joint_key"));
        assertEquals(10, row.get("id"));
        assertEquals(0, row.get("parent_id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedArgs = (Map<String, Object>) row.get("args");
        assertEquals(
                List.of("component_index", "sensor_index", "device_id", "sender_host_id",
                        "ignore", "alarm_info", "create_timestamp", "display_scene"),
                List.copyOf(mappedArgs.keySet()));
        assertEquals(1, mappedArgs.get("component_index"));
        assertEquals(2, mappedArgs.get("sensor_index"));
        assertEquals("DEV-1", mappedArgs.get("device_id"));
        assertEquals("HOST-1", mappedArgs.get("sender_host_id"));
        assertEquals(0, mappedArgs.get("ignore"));
        assertEquals(42, mappedArgs.get("alarm_info"));
        assertEquals(1_700_000_000_000L, mappedArgs.get("create_timestamp"));
        assertEquals(3, mappedArgs.get("display_scene"));
    }

    @Test
    void buildHmsBasics_shouldReturnNullArgs_whenArgsNull() {
        Map<String, Object> row = tool.buildHmsBasics(new HmsDataBasics());

        assertTrue(row.containsKey("args"));
        assertNull(row.get("args"));
    }

    // ==================== buildHmsArgs ====================

    @Test
    void buildHmsArgs_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildHmsArgs(null));
    }

    /**
     * 构造字段齐全的主告警条目，供多个用例复用。
     *
     * @return 填充完毕的 HmsDataBasics
     */
    private static HmsDataBasics createBasics() {
        HmsDataBasics basics = new HmsDataBasics();
        basics.setLevel(1);
        basics.setModuleId(2);
        basics.setCode("CODE-1");
        basics.setDeviceType("drone");
        basics.setDomainType("domain");
        basics.setInTheSky(1);
        basics.setImminent(1);
        basics.setStatusKey("status-key");
        basics.setJointKey("joint-key");
        basics.setId(10);
        basics.setParentId(0);
        return basics;
    }
}
