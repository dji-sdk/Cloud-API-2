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

package com.dji.flighthub.mcp.tools.firmware;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityConfigItemResult;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityRulesResult;
import com.dji.flighthubsdk.bean.FirmwareItemResult;
import com.dji.flighthubsdk.bean.FirmwareReleaseNoteResult;
import com.dji.flighthubsdk.bean.FirmwareUpgradeAssociateDrone;
import com.dji.flighthubsdk.bean.FirmwareUpgradeAssociateRelay;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressOTA;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressStep;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResponseDeviceItemResult;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResult;
import com.dji.flighthubsdk.bean.UploadFileResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhFirmwareTool 的 SDK bean → 结构化 Map 映射单元测试。
 *
 * <p>这些 mapper 原先直接透传 {@code toString()}，改为结构化输出后需要锁定字段名与 null 语义，
 * 避免后续改动悄悄改变返回给 LLM 的响应结构。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFirmwareBeanMapperTest {

    private FhFirmwareTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhFirmwareTool(sessionManager, new SdkService(), "/app/firmware");
    }

    // ==================== mapFirmwareItems ====================

    @Test
    void mapFirmwareItems_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.mapFirmwareItems(null).isEmpty());
    }

    @Test
    void mapFirmwareItems_shouldSkipNullElements() {
        List<FirmwareItemResult> items = new ArrayList<>(Arrays.asList(null, null));

        assertTrue(tool.mapFirmwareItems(items).isEmpty());
    }

    @Test
    void mapFirmwareItems_shouldMapAllFields() {
        FirmwareItemResult item = new FirmwareItemResult();
        item.setId(7);
        item.setCreatedAt("2026-01-02T03:04:05Z");
        item.setDeviceModelKey("dock3");
        item.setStatus("released");
        item.setVersion("10.01.0101");
        item.setDesc("release note");

        List<Map<String, Object>> mapped = tool.mapFirmwareItems(List.of(item));

        assertEquals(1, mapped.size());
        Map<String, Object> row = mapped.get(0);
        assertEquals(7, row.get("file_id"));
        assertEquals("2026-01-02T03:04:05Z", row.get("created_at"));
        assertEquals("dock3", row.get("device_model_key"));
        assertEquals("released", row.get("status"));
        assertEquals("10.01.0101", row.get("version"));
        assertEquals("release note", row.get("desc"));
        assertEquals(
                List.of("file_id", "created_at", "device_model_key", "status", "version", "desc"),
                List.copyOf(row.keySet()));
    }

    // ==================== mapProgressItems ====================

    @Test
    void mapProgressItems_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.mapProgressItems(null).isEmpty());
    }

    @Test
    void mapProgressItems_shouldKeepNestedFieldsNull_whenSubBeansMissing() {
        FirmwareUpgradeProgressItem item = new FirmwareUpgradeProgressItem();
        item.setSn("SN-1");
        item.setDeviceCode(1);
        item.setFirmwareStatus(2);
        item.setWorkingStatus(3);

        List<Map<String, Object>> mapped = tool.mapProgressItems(List.of(item));

        assertEquals(1, mapped.size());
        Map<String, Object> row = mapped.get(0);
        assertEquals("SN-1", row.get("sn"));
        assertEquals(1, row.get("device_code"));
        assertEquals(2, row.get("firmware_status"));
        assertEquals(3, row.get("working_status"));
        assertTrue(row.containsKey("ota"));
        assertNull(row.get("ota"));
        assertNull(row.get("associate_drone"));
        assertNull(row.get("associate_relay"));
    }

    @Test
    void mapProgressItems_shouldMapNestedBeans() {
        FirmwareUpgradeProgressStep step = new FirmwareUpgradeProgressStep();
        step.setCurrentStep(3);
        step.setPercent(45);
        FirmwareUpgradeProgressOTA ota = new FirmwareUpgradeProgressOTA();
        ota.setStatus("downloading");
        ota.setProgress(step);

        FirmwareUpgradeAssociateDrone drone = new FirmwareUpgradeAssociateDrone();
        drone.setSn("DRONE-1");
        drone.setFirmwareStatus(1);
        drone.setWorkingStatus(2);

        FirmwareUpgradeAssociateRelay relay = new FirmwareUpgradeAssociateRelay();
        relay.setSn("RELAY-1");
        relay.setFirmwareStatus(4);

        FirmwareUpgradeProgressItem item = new FirmwareUpgradeProgressItem();
        item.setSn("SN-1");
        item.setOta(ota);
        item.setAssociateDrone(drone);
        item.setAssociateRelay(relay);

        Map<String, Object> row = tool.mapProgressItems(List.of(item)).get(0);

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedOta = (Map<String, Object>) row.get("ota");
        assertEquals("downloading", mappedOta.get("status"));
        assertEquals(3, mappedOta.get("current_step"));
        assertEquals(45, mappedOta.get("percent"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedDrone = (Map<String, Object>) row.get("associate_drone");
        assertEquals("DRONE-1", mappedDrone.get("sn"));
        assertEquals(1, mappedDrone.get("firmware_status"));
        assertEquals(2, mappedDrone.get("working_status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedRelay = (Map<String, Object>) row.get("associate_relay");
        assertEquals("RELAY-1", mappedRelay.get("sn"));
        assertEquals(4, mappedRelay.get("firmware_status"));
    }

    // ==================== buildProgressOta ====================

    @Test
    void buildProgressOta_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildProgressOta(null));
    }

    @Test
    void buildProgressOta_shouldOmitStepFields_whenProgressNull() {
        FirmwareUpgradeProgressOTA ota = new FirmwareUpgradeProgressOTA();
        ota.setStatus("idle");

        Map<String, Object> row = tool.buildProgressOta(ota);

        assertEquals("idle", row.get("status"));
        assertEquals(List.of("status"), List.copyOf(row.keySet()));
    }

    // ==================== buildAssociateDrone / buildAssociateRelay ====================

    @Test
    void buildAssociateDrone_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildAssociateDrone(null));
    }

    @Test
    void buildAssociateDrone_shouldMapFields() {
        FirmwareUpgradeAssociateDrone drone = new FirmwareUpgradeAssociateDrone();
        drone.setSn("DRONE-1");
        drone.setFirmwareStatus(5);
        drone.setWorkingStatus(6);

        Map<String, Object> row = tool.buildAssociateDrone(drone);

        assertEquals(List.of("sn", "firmware_status", "working_status"), List.copyOf(row.keySet()));
        assertEquals("DRONE-1", row.get("sn"));
        assertEquals(5, row.get("firmware_status"));
        assertEquals(6, row.get("working_status"));
    }

    @Test
    void buildAssociateRelay_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildAssociateRelay(null));
    }

    @Test
    void buildAssociateRelay_shouldMapFields() {
        FirmwareUpgradeAssociateRelay relay = new FirmwareUpgradeAssociateRelay();
        relay.setSn("RELAY-1");
        relay.setFirmwareStatus(7);

        Map<String, Object> row = tool.buildAssociateRelay(relay);

        assertEquals(List.of("sn", "firmware_status"), List.copyOf(row.keySet()));
        assertEquals("RELAY-1", row.get("sn"));
        assertEquals(7, row.get("firmware_status"));
    }

    // ==================== mapReleaseNotes ====================

    @Test
    void mapReleaseNotes_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.mapReleaseNotes(null).isEmpty());
    }

    @Test
    void mapReleaseNotes_shouldMapAllFields() {
        FirmwareReleaseNoteResult note = new FirmwareReleaseNoteResult();
        note.setDeviceModelKey("dock3");
        note.setLang("zh-CN");
        note.setProductVersion("10.01.0101");
        note.setReleaseNote("修复若干问题");
        note.setReleasedTime("2026-01-02");

        Map<String, Object> row = tool.mapReleaseNotes(List.of(note)).get(0);

        assertEquals(
                List.of("device_model_key", "lang", "product_version", "release_note", "released_time"),
                List.copyOf(row.keySet()));
        assertEquals("dock3", row.get("device_model_key"));
        assertEquals("zh-CN", row.get("lang"));
        assertEquals("10.01.0101", row.get("product_version"));
        assertEquals("修复若干问题", row.get("release_note"));
        assertEquals("2026-01-02", row.get("released_time"));
    }

    // ==================== mapCompatibilityConfigs ====================

    @Test
    void mapCompatibilityConfigs_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.mapCompatibilityConfigs(null).isEmpty());
    }

    @Test
    void mapCompatibilityConfigs_shouldOmitRules_whenRulesNull() {
        FirmwareCompatibilityConfigItemResult config = new FirmwareCompatibilityConfigItemResult();
        config.setDeviceModelKey("dock3");
        config.setCompatibleVersions(List.of("10.01.0101"));

        Map<String, Object> row = tool.mapCompatibilityConfigs(List.of(config)).get(0);

        assertEquals("dock3", row.get("device_model_key"));
        assertEquals(List.of("10.01.0101"), row.get("compatible_versions"));
        assertFalse(row.containsKey("rules"));
    }

    @Test
    void mapCompatibilityConfigs_shouldMapRules_whenRulesPresent() {
        FirmwareCompatibilityRulesResult rules = new FirmwareCompatibilityRulesResult();
        rules.setMinVersion("10.00.0000");
        rules.setMaxVersion("10.02.0000");
        FirmwareCompatibilityConfigItemResult config = new FirmwareCompatibilityConfigItemResult();
        config.setDeviceModelKey("dock3");
        config.setCompatibleVersions(List.of("10.01.0101"));
        config.setRules(rules);

        Map<String, Object> row = tool.mapCompatibilityConfigs(List.of(config)).get(0);

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedRules = (Map<String, Object>) row.get("rules");
        assertEquals(List.of("min_version", "max_version"), List.copyOf(mappedRules.keySet()));
        assertEquals("10.00.0000", mappedRules.get("min_version"));
        assertEquals("10.02.0000", mappedRules.get("max_version"));
    }

    // ==================== buildUpgradeResult ====================

    @Test
    void buildUpgradeResult_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildUpgradeResult(null));
    }

    @Test
    void buildUpgradeResult_shouldReturnEmptyDevices_whenDevicesNull() {
        FirmwareUpgradeResult result = new FirmwareUpgradeResult();
        result.setDeviceCode(2);
        result.setStatus("in_progress");

        Map<String, Object> row = tool.buildUpgradeResult(result);

        assertEquals(2, row.get("device_code"));
        assertEquals("in_progress", row.get("status"));
        assertEquals(List.of(), row.get("devices"));
    }

    @Test
    void buildUpgradeResult_shouldSkipNullDevices() {
        FirmwareUpgradeResponseDeviceItemResult device = new FirmwareUpgradeResponseDeviceItemResult();
        device.setSn("DRONE-1");
        device.setFirmwareUpgradeType(1);
        device.setProductVersion("10.01.0101");

        FirmwareUpgradeResult result = new FirmwareUpgradeResult();
        result.setDeviceCode(1);
        result.setStatus("ok");
        result.setDevices(Arrays.asList(device, null));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> devices = (List<Map<String, Object>>) tool.buildUpgradeResult(result).get("devices");

        assertEquals(1, devices.size());
        assertEquals(List.of("sn", "firmware_upgrade_type", "product_version"),
                List.copyOf(devices.get(0).keySet()));
        assertEquals("DRONE-1", devices.get(0).get("sn"));
        assertEquals(1, devices.get(0).get("firmware_upgrade_type"));
        assertEquals("10.01.0101", devices.get(0).get("product_version"));
    }

    // ==================== buildUploadFileResult ====================

    @Test
    void buildUploadFileResult_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildUploadFileResult(null));
    }

    @Test
    void buildUploadFileResult_shouldMapAllFields() {
        UploadFileResult result = new UploadFileResult();
        result.setObjectKey("firmware/dock3.bin");
        result.setObjectUrl("https://example.com/dock3.bin");
        result.setEtag("etag-1");

        Map<String, Object> row = tool.buildUploadFileResult(result);

        assertEquals(List.of("object_key", "object_url", "etag"), List.copyOf(row.keySet()));
        assertEquals("firmware/dock3.bin", row.get("object_key"));
        assertEquals("https://example.com/dock3.bin", row.get("object_url"));
        assertEquals("etag-1", row.get("etag"));
    }
}
