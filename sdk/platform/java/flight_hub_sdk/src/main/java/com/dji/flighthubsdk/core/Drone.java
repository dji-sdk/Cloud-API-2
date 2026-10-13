// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.generated.IDeviceStateModule;
import com.dji.flighthubsdk.generated.IDeviceHMSModule;
import com.dji.flighthubsdk.generated.IDeviceDebugModule;
import com.dji.flighthubsdk.generated.IAIBoxModule;
import com.dji.flighthubsdk.generated.ILiveDeviceModule;
import com.dji.flighthubsdk.generated.IDrone;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSPlayInfo;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.generated.IntVector;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.value.DroneLinkState;
import com.dji.flighthubsdk.value.MegaphonePlayMode;

/**
 * 飞行器模块接口，提供直播、负载控制、飞行控制、OSD 数据监听等功能。
 * <p>通过 {@code FlightHubSDK.getDroneInstance()} 获取实例。</p>
 */
public class Drone {

    private IDrone drone;
    private String sn; // 设备序列号
    private DeviceStateModule deviceStateModule;
    private DeviceHMSModule deviceHmsModule;
    private DeviceDebugModule deviceDebugModule;
    private AIBoxModule aiBoxModule;
    private LiveDeviceModule liveDeviceModule;
    private TakeoffModule takeoffModuleRef = null;
    private FlightControlModule flightControlModuleRef = null;
    private ControlAuthModule controlAuthModuleRef = null;
    private DrcModule drcModuleRef = null;
    private FlightSettingsModule flightSettingsModuleRef = null;
    private TestFlightModule testFlightModuleRef = null;
    private OnboardAiModule onboardAiModuleRef = null;
    private DroneOsdModule droneOsdModuleRef = null;
    private PsdkControlModule psdkControlModuleRef = null;
    private CameraControlModule cameraControlModuleRef = null;

    // 回调引用容器,防止回调被GC
    private class CallbackRefContainer {
        private Object callback_jni_ref;
        private Object callback_java_ref;

        public CallbackRefContainer(Object callback_jni_ref, Object callback_java_ref) {
            this.callback_jni_ref = callback_jni_ref;
            this.callback_java_ref = callback_java_ref;
        }

        public void release() {
            if (callback_jni_ref != null) {
                try {
                    callback_jni_ref.getClass().getMethod("delete").invoke(callback_jni_ref);
                } catch (Exception ignored) {
                }
                callback_jni_ref = null;
            }
        }
    }
    
    // Device Container
    private final java.util.concurrent.ConcurrentHashMap<String, LiveModule> liveModuleRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, LiveRecordModule> liveRecordModuleRef = new java.util.concurrent.ConcurrentHashMap<>();
    private FlyToPointModule flyToPointModuleRef = null;
    private PoiModule poiModuleRef = null;
    private InFlightWaylineModule inFlightWaylineModuleRef = null;
    private FlightTaskControlModule flightTaskControlModuleRef = null;

    // Action Callback

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    /**
     * 构造飞行器实例
     * @param drone 底层飞行器接口
     * @param sn 设备序列号
     */
    public Drone(IDrone drone, String sn) {
        this.drone = drone;
        this.sn = sn;
        Logger.info("Drone", "[" + sn + "] Drone instance created");
    }









    /**
     * 获取直播流模块
     * @param params 直播信息参数
     * @return 直播流模块实例
     */
    public synchronized LiveModule getLiveStream(LiveInfo params) {
        if (params == null) {
            Logger.warn("Drone", "getLiveStream: params is null");
            return null;
        }
        com.dji.flighthubsdk.generated.LiveInfo nativeInfo = null;
        try {
            nativeInfo = FlightHubSdkConvert.toNativeLiveInfo(params);
            if (liveModuleRef.containsKey(params.getPayloadIndex())) {
                return liveModuleRef.get(params.getPayloadIndex());
            }
            LiveModule liveModule = new LiveModule(this.drone.getLiveStream(nativeInfo), this.sn);
            if (liveModule != null) {
                Logger.info("Drone", "[" + sn + "] GetLiveStream: live module created, payload_index: " + params.getPayloadIndex());
                liveModuleRef.put(params.getPayloadIndex(), liveModule);
                return liveModule;
            }
            Logger.error("Drone", "[" + sn + "] GetLiveStream error: live module not found");
            return null;
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 获取直播录制模块
     * @param params 直播信息参数
     * @return 直播录制模块实例
     */
    public synchronized LiveRecordModule getLiveRecord(LiveInfo params) {
        if (params == null) {
            Logger.warn("Drone", "getLiveRecord: params is null");
            return null;
        }
        com.dji.flighthubsdk.generated.LiveInfo nativeInfo = null;
        try {
            nativeInfo = FlightHubSdkConvert.toNativeLiveInfo(params);
            if (liveRecordModuleRef.containsKey(params.getPayloadIndex())) {
                return liveRecordModuleRef.get(params.getPayloadIndex());
            }
            com.dji.flighthubsdk.generated.ILiveRecordModule raw = this.drone.getLiveRecord(nativeInfo);
            if (raw == null) {
                Logger.warn("Drone", "getLiveRecord: module is null, payload_index: " + params.getPayloadIndex());
                return null;
            }
            LiveRecordModule liveRecordModule = new LiveRecordModule(raw, this.sn);
            liveRecordModuleRef.put(params.getPayloadIndex(), liveRecordModule);
            Logger.info("Drone", "[" + sn + "] getLiveRecord: live record module created, payload_index: " + params.getPayloadIndex());
            return liveRecordModule;
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 获取设备级直播发现模块（可用直播源订阅）
     */
    public synchronized LiveDeviceModule getLiveDeviceModule() {
        if (liveDeviceModule == null) {
            ILiveDeviceModule module = drone.getLiveDeviceModule();
            if (module != null) {
                liveDeviceModule = new LiveDeviceModule(module, "Drone[" + sn + "]");
            }
        }
        return liveDeviceModule;
    }

    /**
     * 获取一键起飞模块
     */
    public synchronized TakeoffModule getTakeoffModule() {
        if (takeoffModuleRef != null) {
            return takeoffModuleRef;
        }
        try {
            takeoffModuleRef = new TakeoffModule(this.drone.getTakeoffModule());
            Logger.info("Drone", "[" + sn + "] GetTakeoffModule: TakeoffModule created");
            return takeoffModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetTakeoffModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取飞控指令模块（紧急停/降落、返航、摇杆）
     */
    public synchronized FlightControlModule getFlightControlModule() {
        if (flightControlModuleRef != null) {
            return flightControlModuleRef;
        }
        try {
            flightControlModuleRef = new FlightControlModule(this.drone.getFlightControlModule());
            Logger.info("Drone", "[" + sn + "] GetFlightControlModule: FlightControlModule created");
            return flightControlModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetFlightControlModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取飞行/负载控制权模块
     */
    public synchronized ControlAuthModule getControlAuthModule() {
        if (controlAuthModuleRef != null) {
            return controlAuthModuleRef;
        }
        try {
            controlAuthModuleRef = new ControlAuthModule(this.drone.getControlAuthModule());
            Logger.info("Drone", "[" + sn + "] GetControlAuthModule: ControlAuthModule created");
            return controlAuthModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetControlAuthModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取 DRC 链路模块
     */
    public synchronized DrcModule getDrcModule() {
        if (drcModuleRef != null) {
            return drcModuleRef;
        }
        try {
            drcModuleRef = new DrcModule(this.drone.getDrcModule());
            Logger.info("Drone", "[" + sn + "] GetDrcModule: DrcModule created");
            return drcModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetDrcModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取飞行设置模块
     */
    public synchronized FlightSettingsModule getFlightSettingsModule() {
        if (flightSettingsModuleRef != null) {
            return flightSettingsModuleRef;
        }
        try {
            flightSettingsModuleRef = new FlightSettingsModule(this.drone.getFlightSettingsModule(), sn);
            Logger.info("Drone", "[" + sn + "] GetFlightSettingsModule: FlightSettingsModule created");
            return flightSettingsModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetFlightSettingsModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取试飞任务模块
     */
    public synchronized TestFlightModule getTestFlightModule() {
        if (testFlightModuleRef != null) {
            return testFlightModuleRef;
        }
        try {
            testFlightModuleRef = new TestFlightModule(this.drone.getTestFlightModule());
            Logger.info("Drone", "[" + sn + "] GetTestFlightModule: TestFlightModule created");
            return testFlightModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetTestFlightModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取机载 AI 模块
     */
    public synchronized OnboardAiModule getOnboardAiModule() {
        if (onboardAiModuleRef != null) {
            return onboardAiModuleRef;
        }
        try {
            onboardAiModuleRef = new OnboardAiModule(this.drone.getOnboardAiModule());
            Logger.info("Drone", "[" + sn + "] GetOnboardAiModule: OnboardAiModule created");
            return onboardAiModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetOnboardAiModule error: " + e.getMessage(), e);
        }
        return null;
    }



    /**
     * 获取相机/云台控制模块
     */
    public synchronized CameraControlModule getCameraControlModule() {
        if (cameraControlModuleRef == null) {
            cameraControlModuleRef = new CameraControlModule(this.drone.getCameraControlModule(), sn);
        }
        return cameraControlModuleRef;
    }

    /**
     * 获取 PSDK 控制模块（探照灯/喊话器/Widget）
     */
    public synchronized PsdkControlModule getPsdkControlModule() {
        if (psdkControlModuleRef == null) {
            psdkControlModuleRef = new PsdkControlModule(this.drone.getPsdkControlModule(), sn);
        }
        return psdkControlModuleRef;
    }

    /**
     * 获取飞行器 OSD 数据订阅模块
     */
    public synchronized DroneOsdModule getDroneOsdModule() {
        if (droneOsdModuleRef != null) {
            return droneOsdModuleRef;
        }
        try {
            droneOsdModuleRef = new DroneOsdModule(this.drone.getDroneOsdModule(), sn);
            Logger.info("Drone", "[" + sn + "] GetDroneOsdModule: DroneOsdModule created");
            return droneOsdModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetDroneOsdModule error: " + e.getMessage(), e);
        }
        return null;
    }

































    /**
     * 获取指点飞行模块
     * @return 指点飞行模块实例
     */
    public synchronized FlyToPointModule getFlyToPointModule() {
        if (flyToPointModuleRef != null) {
            return flyToPointModuleRef;
        }
        flyToPointModuleRef = new FlyToPointModule(this.drone.getFlyToPointModule());
        if (flyToPointModuleRef != null) {
            Logger.info("Drone", "[" + sn + "] GetFlyToPointModule: fly to point module created");
            return flyToPointModuleRef;
        }
        Logger.error("Drone", "[" + sn + "] GetFlyToPointModule error: fly to point module not found");
        return null;
    }

    /**
     * 获取空中下发航线模块
     * @return 空中下发航线模块实例
     */
    public synchronized InFlightWaylineModule getInFlightWaylineModule() {
        if (inFlightWaylineModuleRef != null) {
            return inFlightWaylineModuleRef;
        }
        try {
            inFlightWaylineModuleRef = new InFlightWaylineModule(this.drone.getInFlightWaylineModule());
            Logger.info("Drone", "[" + sn + "] GetInFlightWaylineModule: module created");
            return inFlightWaylineModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetInFlightWaylineModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取环绕飞行 (POI) 模块
     * @return POI 模块实例
     */
    public synchronized PoiModule getPoiModule() {
        if (poiModuleRef != null) {
            return poiModuleRef;
        }
        try {
            poiModuleRef = new PoiModule(this.drone.getPoiModule());
            Logger.info("Drone", "[" + sn + "] GetPoiModule: POI module created");
            return poiModuleRef;
        } catch (Exception e) {
            Logger.error("Drone", "[" + sn + "] GetPoiModule error: POI module not found - " + e.getMessage(), e);
        }
        return null;
    }















    /**
     * 获取飞行任务控制模块
     * @return 飞行任务控制模块实例
     */
    public synchronized FlightTaskControlModule getFlightTaskControlModule() {
        if (flightTaskControlModuleRef == null) {
            try {
                flightTaskControlModuleRef = new FlightTaskControlModule(this.drone.getFlightTaskControlModule());
                Logger.info("Drone", "[" + sn + "] GetFlightTaskControlModule: flight task control module created");
            } catch (Exception e) {
                Logger.error("Drone", "[" + sn + "] GetFlightTaskControlModule error: " + e.getMessage(), e);
                return null;
            }
        }
        return flightTaskControlModuleRef;
    }

    // ==================== 红外增益模式相关接口 ====================









    /**
     * 获取设备状态模块（拓扑变更、在线状态）
     */
    public synchronized DeviceStateModule getDeviceStateModule() {
        if (deviceStateModule == null) {
            IDeviceStateModule module = drone.getDeviceStateModule();
            if (module != null) {
                deviceStateModule = new DeviceStateModule(module, "Drone[" + sn + "]");
            }
        }
        return deviceStateModule;
    }

    /**
     * 获取设备 HMS 实时推送模块
     */
    public synchronized DeviceHMSModule getDeviceHMSModule() {
        if (deviceHmsModule == null) {
            IDeviceHMSModule module = drone.getDeviceHMSModule();
            if (module != null) {
                deviceHmsModule = new DeviceHMSModule(module, "Drone[" + sn + "]");
            }
        }
        return deviceHmsModule;
    }

    /**
     * 获取设备调试模块
     */
    public synchronized DeviceDebugModule getDeviceDebugModule() {
        if (deviceDebugModule == null) {
            IDeviceDebugModule module = drone.getDeviceDebugModule();
            if (module != null) {
                deviceDebugModule = new DeviceDebugModule(module);
            }
        }
        return deviceDebugModule;
    }

    /**
     * 获取 AI Box 模块
     */
    public synchronized AIBoxModule getAIBoxModule() {
        if (aiBoxModule == null) {
            IAIBoxModule module = drone.getAIBoxModule();
            if (module != null) {
                aiBoxModule = new AIBoxModule(module);
            }
        }
        return aiBoxModule;
    }

}
