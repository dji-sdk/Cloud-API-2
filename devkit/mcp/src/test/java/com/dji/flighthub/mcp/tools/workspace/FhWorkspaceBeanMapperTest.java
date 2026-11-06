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

package com.dji.flighthub.mcp.tools.workspace;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.WaylineDetailExtData;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineDetailSurveyAreaScope;
import com.dji.flighthubsdk.bean.WaylineDetailSurveyAreaScopePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhWorkspaceTool 的 SDK bean → 结构化 Map 映射单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class FhWorkspaceBeanMapperTest {

    private FhWorkspaceTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhWorkspaceTool(sessionManager, new SdkService(), new ListenChannelSupport(new SubscriptionBuffer()));
    }

    // ==================== mapWaylineDetail ====================

    @Test
    void mapWaylineDetail_shouldMapAllFieldsInOrder() {
        GetWaylineDetailResult result = new GetWaylineDetailResult();
        result.setWaylineUuid("wl-uuid-1");
        result.setWaylineName("巡检航线");
        result.setMd5("md5-1");
        result.setKmzObjectKey("wayline/wl-uuid-1.kmz");
        result.setUrl("https://example.com/wl.kmz");
        result.setSuffix("kmz");
        result.setDeletedAt(0L);
        result.setUpdatedAt(1_700_000_000_000L);
        result.setKmzWithoutResUrl("https://example.com/wl-no-res.kmz");
        result.setTargetAreaFileUrl("https://example.com/target.kml");
        result.setBusinessFileUrl("https://example.com/business.json");
        result.setEditorWarnings(Map.of("k1", "v1"));
        result.setExecuteWarnings(Map.of("k2", "v2"));

        Map<String, Object> row = tool.mapWaylineDetail(result);

        assertEquals(
                List.of("wayline_uuid", "wayline_name", "md5", "kmz_object_key", "url", "suffix",
                        "deleted_at", "updated_at", "kmz_without_res_url", "target_area_file_url",
                        "business_file_url", "kmz_data", "ext_data", "editor_warnings", "execute_warnings"),
                List.copyOf(row.keySet()));
        assertEquals("wl-uuid-1", row.get("wayline_uuid"));
        assertEquals("巡检航线", row.get("wayline_name"));
        assertEquals("md5-1", row.get("md5"));
        assertEquals("wayline/wl-uuid-1.kmz", row.get("kmz_object_key"));
        assertEquals("https://example.com/wl.kmz", row.get("url"));
        assertEquals("kmz", row.get("suffix"));
        assertEquals(0L, row.get("deleted_at"));
        assertEquals(1_700_000_000_000L, row.get("updated_at"));
        assertEquals("https://example.com/wl-no-res.kmz", row.get("kmz_without_res_url"));
        assertEquals("https://example.com/target.kml", row.get("target_area_file_url"));
        assertEquals("https://example.com/business.json", row.get("business_file_url"));
        assertEquals(Map.of("k1", "v1"), row.get("editor_warnings"));
        assertEquals(Map.of("k2", "v2"), row.get("execute_warnings"));
    }

    @Test
    void mapWaylineDetail_shouldReturnNullNestedFields_whenSubBeansMissing() {
        Map<String, Object> row = tool.mapWaylineDetail(new GetWaylineDetailResult());

        assertTrue(row.containsKey("kmz_data"));
        assertNull(row.get("kmz_data"));
        assertTrue(row.containsKey("ext_data"));
        assertNull(row.get("ext_data"));
    }

    // ==================== mapKmzData ====================

    @Test
    void mapKmzData_shouldReturnNull_whenInputNull() {
        assertNull(tool.mapKmzData(null));
    }

    @Test
    void mapKmzData_shouldMapAllFieldsInOrder() {
        WaylineDetailSurveyAreaScopePoint point = new WaylineDetailSurveyAreaScopePoint();
        point.setLng(113.9);
        point.setLat(22.5);
        point.setAlt(100.0);

        WaylineDetailSurveyAreaScope scope = new WaylineDetailSurveyAreaScope();
        scope.setPoints(List.of(point));

        WaylineDetailKmzData kmzData = new WaylineDetailKmzData();
        kmzData.setWaylineType(1);
        kmzData.setWaylinePointCount(42);
        kmzData.setDroneType("M3D");
        kmzData.setPayloadTypes(List.of("wide"));
        kmzData.setPayloadTypesWithIndex(List.of("0-wide"));
        kmzData.setDistance(1500.5);
        kmzData.setDuration(600.0);
        kmzData.setSurveyArea(20000.0);
        kmzData.setStartLatitude(22.5);
        kmzData.setStartLongitude(113.9);
        kmzData.setStartAltitude(100.0);
        kmzData.setEstimatedPhotoCount(120);
        kmzData.setWaylineValidityCheck(true);
        kmzData.setWaylineValidityCheckCodes(List.of(0, 1));
        kmzData.setElevationFileNames(List.of("elevation.tif"));
        kmzData.setSurveyAreaScope(scope);

        Map<String, Object> row = tool.mapKmzData(kmzData);

        assertEquals(
                List.of("wayline_type", "wayline_point_count", "drone_type", "payload_types",
                        "payload_types_with_index", "distance", "duration", "survey_area",
                        "start_latitude", "start_longitude", "start_altitude", "estimated_photo_count",
                        "wayline_validity_check", "wayline_validity_check_codes",
                        "elevation_file_names", "survey_area_scope"),
                List.copyOf(row.keySet()));
        assertEquals(1, row.get("wayline_type"));
        assertEquals(42, row.get("wayline_point_count"));
        assertEquals("M3D", row.get("drone_type"));
        assertEquals(List.of("wide"), row.get("payload_types"));
        assertEquals(List.of("0-wide"), row.get("payload_types_with_index"));
        assertEquals(1500.5, row.get("distance"));
        assertEquals(600.0, row.get("duration"));
        assertEquals(20000.0, row.get("survey_area"));
        assertEquals(22.5, row.get("start_latitude"));
        assertEquals(113.9, row.get("start_longitude"));
        assertEquals(100.0, row.get("start_altitude"));
        assertEquals(120, row.get("estimated_photo_count"));
        assertEquals(true, row.get("wayline_validity_check"));
        assertEquals(List.of(0, 1), row.get("wayline_validity_check_codes"));
        assertEquals(List.of("elevation.tif"), row.get("elevation_file_names"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedScope = (Map<String, Object>) row.get("survey_area_scope");
        assertNotNull(mappedScope);
    }

    @Test
    void mapKmzData_shouldReturnNullScope_whenScopeMissing() {
        WaylineDetailKmzData kmzData = new WaylineDetailKmzData();

        assertNull(tool.mapKmzData(kmzData).get("survey_area_scope"));
    }

    // ==================== mapSurveyAreaScope ====================

    @Test
    void mapSurveyAreaScope_shouldReturnNull_whenInputNull() {
        assertNull(tool.mapSurveyAreaScope(null));
    }

    @Test
    void mapSurveyAreaScope_shouldReturnEmptyPoints_whenPointsNull() {
        Map<String, Object> row = tool.mapSurveyAreaScope(new WaylineDetailSurveyAreaScope());

        assertEquals(List.of("points"), List.copyOf(row.keySet()));
        assertEquals(List.of(), row.get("points"));
    }

    @Test
    void mapSurveyAreaScope_shouldMapPoints() {
        WaylineDetailSurveyAreaScopePoint first = new WaylineDetailSurveyAreaScopePoint();
        first.setLng(113.9);
        first.setLat(22.5);
        first.setAlt(100.0);

        WaylineDetailSurveyAreaScopePoint second = new WaylineDetailSurveyAreaScopePoint();
        second.setLng(114.0);
        second.setLat(22.6);
        second.setAlt(120.0);

        WaylineDetailSurveyAreaScope scope = new WaylineDetailSurveyAreaScope();
        scope.setPoints(List.of(first, second));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> points = (List<Map<String, Object>>) tool.mapSurveyAreaScope(scope).get("points");

        assertEquals(2, points.size());
        assertEquals(113.9, points.get(0).get("lng"));
        assertEquals(22.5, points.get(0).get("lat"));
        assertEquals(100.0, points.get(0).get("alt"));
        assertEquals(114.0, points.get(1).get("lng"));
        assertEquals(22.6, points.get(1).get("lat"));
        assertEquals(120.0, points.get(1).get("alt"));
    }

    // ==================== mapExtData ====================

    @Test
    void mapExtData_shouldReturnNull_whenInputNull() {
        assertNull(tool.mapExtData(null));
    }

    @Test
    void mapExtData_shouldMapAllFieldsInOrder() {
        WaylineDetailExtData extData = new WaylineDetailExtData();
        extData.setKmzWithoutResObjectKey("wayline/no-res.kmz");
        extData.setKmzWithoutResMd5("md5-no-res");
        extData.setTargetAreaFileObjectKey("wayline/target.kml");
        extData.setBusinessFileObjectKey("wayline/business.json");
        extData.setModelFileObjectKeys(List.of("wayline/model.obj"));
        extData.setElevationFileObjectKeys(List.of("wayline/elevation.tif"));
        extData.setContentLength(2048L);

        Map<String, Object> row = tool.mapExtData(extData);

        assertEquals(
                List.of("kmz_without_res_object_key", "kmz_without_res_md5",
                        "target_area_file_object_key", "business_file_object_key",
                        "model_file_object_keys", "elevation_file_object_keys", "content_length"),
                List.copyOf(row.keySet()));
        assertEquals("wayline/no-res.kmz", row.get("kmz_without_res_object_key"));
        assertEquals("md5-no-res", row.get("kmz_without_res_md5"));
        assertEquals("wayline/target.kml", row.get("target_area_file_object_key"));
        assertEquals("wayline/business.json", row.get("business_file_object_key"));
        assertEquals(List.of("wayline/model.obj"), row.get("model_file_object_keys"));
        assertEquals(List.of("wayline/elevation.tif"), row.get("elevation_file_object_keys"));
        assertEquals(2048L, row.get("content_length"));
    }
}
