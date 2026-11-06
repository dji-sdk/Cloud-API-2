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

package com.dji.flighthub.mcp.tools.osd;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftWindSpeed;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.DroneOsdModule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OsdFieldCatalog 单元测试。字段全集钉住 SDK 3.3.7，升级 SDK 后列表变化时本测试会失败，提示同步文档与调用方。
 *
 * @author DJI FlightHub SDK Team
 */
class OsdFieldCatalogTest {

    private static final List<String> EXPECTED_DRONE_FIELDS = List.of(
            "activation_time", "attitude", "battery_info", "battery_warning_thresholds", "compliance_info",
            "control_code", "control_rights_cloud_control_status", "control_rights_device_control_status",
            "control_source", "current_rth_mode", "drone_dongle_infos", "drone_flysafe_database_version",
            "drone_mode_code_reason", "drone_wpmz_version", "firmware_status", "flight_tasks_exception",
            "flight_tasks_media_file_display_available", "gear", "home_distance", "home_geographical_info",
            "hsi_info", "limits_info", "location", "maintain_status_info", "mode_code", "night_lights_state",
            "obstacle_avoidance_state", "payloads_camera_liveview_world_region", "payloads_cameras_list",
            "payloads_gimbal_attitude", "payloads_list", "payloads_osd_info", "position_state",
            "rc_lost_action", "rth_info", "speed", "storage_info", "total_flight_info", "track_id",
            "wind_speed");

    private static final List<String> EXPECTED_DOCK_FIELDS = List.of(
            "accumulated_running_time", "activation_time", "air_conditioner_mode", "alarm_state",
            "alternate_land_point", "backup_battery_info", "battery_store_mode", "cover_state", "dongle_infos",
            "drc_state", "drone_battery_maintenance_info", "drone_charge_state", "drone_in_dock",
            "emergency_stop_state", "firmware_info", "first_power_on_time", "flight_task_step_code",
            "job_number", "location", "maintain_status_info", "media_file_detail", "meteorological_info",
            "mode_code", "network_state", "position_state", "power_state", "putter_state",
            "relative_alternate_land_point", "self_converge_coordinate", "silent_mode", "storage_state",
            "sub_device", "user_experience_improvement", "wireless_link");

    @Test
    void drone_shouldExposePinnedFieldSet() {
        assertEquals(EXPECTED_DRONE_FIELDS, OsdFieldCatalog.DRONE.names());
    }

    @Test
    void dock_shouldExposePinnedFieldSet() {
        assertEquals(EXPECTED_DOCK_FIELDS, OsdFieldCatalog.DOCK.names());
    }

    @Test
    void catalogs_shouldCoverEveryListenMethodOfTheModules() {
        assertEquals(countListenMethods(DroneOsdModule.class), OsdFieldCatalog.DRONE.all().size());
        assertEquals(countListenMethods(DockOsdModule.class), OsdFieldCatalog.DOCK.all().size());
    }

    @Test
    void everyField_shouldPairListenWithMatchingCancel() {
        for (OsdFieldCatalog catalog : List.of(OsdFieldCatalog.DRONE, OsdFieldCatalog.DOCK)) {
            for (OsdFieldCatalog.Field field : catalog.all().values()) {
                assertEquals(field.listen().getName().replace("listen", "cancelListen"),
                        field.cancel().getName(), field.name());
                assertNotEquals(Object.class, field.payloadType(), field.name());
            }
        }
    }

    @Test
    void sameNameInBothTables_shouldNotClash() {
        assertNotSame(OsdFieldCatalog.DRONE.get("location"), OsdFieldCatalog.DOCK.get("location"));
        assertEquals("listenAircraftLocation", OsdFieldCatalog.DRONE.get("location").listen().getName());
        assertEquals("listenDockLocation", OsdFieldCatalog.DOCK.get("location").listen().getName());
    }

    @Test
    void toFieldName_shouldStripDevicePrefixAndSnakeCase() {
        assertEquals("wind_speed", OsdFieldCatalog.toFieldName("AircraftWindSpeed", "Aircraft"));
        assertEquals("location", OsdFieldCatalog.toFieldName("DockLocation", "Dock"));
        assertEquals("drone_in_dock", OsdFieldCatalog.toFieldName("DroneInDock", "Dock"));
        assertEquals("hsi_info", OsdFieldCatalog.toFieldName("HsiInfo", "Aircraft"));
        assertEquals("aircraft", OsdFieldCatalog.toFieldName("Aircraft", "Aircraft"));
    }

    @Test
    void forDeviceType_shouldSelectTable() {
        assertSame(OsdFieldCatalog.DOCK, OsdFieldCatalog.forDeviceType("dock"));
        assertSame(OsdFieldCatalog.DRONE, OsdFieldCatalog.forDeviceType("drone"));
    }

    @Test
    void get_shouldReturnNull_whenFieldUnknown() {
        assertNull(OsdFieldCatalog.DRONE.get("no_such_field"));
    }

    @Test
    void build_shouldFail_whenListenHasNoPairedCancel() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> OsdFieldCatalog.build(UnpairedModule.class, "Aircraft"));
        assertTrue(e.getMessage().contains("cancelListenFoo"));
    }

    @Test
    void build_shouldFail_whenTwoMethodsMapToSameName() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> OsdFieldCatalog.build(DuplicateModule.class, "Aircraft"));
        assertTrue(e.getMessage().contains("foo"));
    }

    @Test
    void fieldListen_shouldDeliverPayloadThroughProxyCallback() {
        DroneOsdModule module = mock(DroneOsdModule.class);
        AtomicReference<FlightHubCallback.AircraftWindSpeedCallback> captured = new AtomicReference<>();
        when(module.listenAircraftWindSpeed(any())).thenAnswer(inv -> {
            captured.set(inv.getArgument(0));
            return 5;
        });
        AtomicReference<Object> received = new AtomicReference<>();
        OsdFieldCatalog.Field field = OsdFieldCatalog.DRONE.get("wind_speed");

        int id = field.listen(module, received::set);
        captured.get().onResult(new AircraftWindSpeed(90, 3.5));

        assertEquals(5, id);
        assertInstanceOf(AircraftWindSpeed.class, received.get());
        FlightHubCallback.AircraftWindSpeedCallback callback = captured.get();
        assertTrue(callback.toString().contains("wind_speed"));
        assertEquals(callback, callback);
        assertNotEquals(callback, new Object());
        assertEquals(System.identityHashCode(callback), callback.hashCode());
    }

    @Test
    void fieldCancel_shouldInvokeSdkCancel() {
        DroneOsdModule module = mock(DroneOsdModule.class);
        OsdFieldCatalog.DRONE.get("wind_speed").cancel(module, 7);
        verify(module).cancelListenAircraftWindSpeed(7);
    }

    @Test
    void fieldListen_shouldRethrowRuntimeExceptionFromSdk() {
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenThrow(new IllegalStateException("device offline"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> OsdFieldCatalog.DRONE.get("wind_speed").listen(module, p -> { }));
        assertEquals("device offline", e.getMessage());
    }

    @Test
    void fieldListen_shouldWrapCheckedExceptionFromSdk() {
        OsdFieldCatalog catalog = OsdFieldCatalog.build(ThrowingModule.class, "Aircraft");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> catalog.get("foo").listen(new ThrowingModule(), p -> { }));
        assertInstanceOf(Exception.class, e.getCause());
        assertEquals("checked", e.getCause().getMessage());
    }

    @Test
    void newDefaultPayload_shouldConstructBean_orReturnNullWhenNoDefaultConstructor() throws Exception {
        assertInstanceOf(AircraftWindSpeed.class, OsdFieldCatalog.DRONE.get("wind_speed").newDefaultPayload());

        Method any = Object.class.getMethod("toString");
        OsdFieldCatalog.Field unconstructible =
                new OsdFieldCatalog.Field("x", any, any, Runnable.class, Integer.class);
        assertNull(unconstructible.newDefaultPayload());
    }

    private static long countListenMethods(Class<?> moduleClass) {
        return java.util.Arrays.stream(moduleClass.getMethods())
                .filter(m -> m.getName().startsWith("listen") && m.getParameterCount() == 1)
                .count();
    }

    public static class UnpairedModule {
        public int listenFoo(Runnable callback) {
            return 1;
        }
    }

    public static class DuplicateModule {
        public int listenAircraftFoo(Runnable callback) {
            return 1;
        }

        public void cancelListenAircraftFoo(int id) {
        }

        public int listenFoo(Runnable callback) {
            return 2;
        }

        public void cancelListenFoo(int id) {
        }
    }

    public static class ThrowingModule {
        public int listenFoo(Runnable callback) throws Exception {
            throw new Exception("checked");
        }

        public void cancelListenFoo(int id) {
        }
    }
}
