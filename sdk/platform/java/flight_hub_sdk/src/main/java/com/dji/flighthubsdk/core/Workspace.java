// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.util.Logger;

/**
 * 工作空间模块接口，提供设备管理、媒体服务、航线/固件/任务管理、安全服务、Terra 重建等全局 module getter。
 * <p>通过 {@code FlightHubSDK.getWorkspaceInstance()} 获取实例。</p>
 */
public class Workspace {

    private static final String TAG = "Workspace";

    private final IWorkspace workspace;

    private FlightTaskModule flightTaskModuleRef = null;
    private MediaServiceModule mediaServiceModuleRef = null;
    private TerraBuildModule terraBuildModuleRef = null;
    private SafetyServiceModule safetyServiceModuleRef = null;
    private FlightTaskRecordModule flightTaskRecordModuleRef = null;
    private HMSModule hmsModuleRef = null;
    private LogManagementModule logManagementModuleRef = null;
    private MaintenanceManagementModule maintenanceManagementModuleRef = null;
    private DiagnosisManagementModule diagnosisManagementModuleRef = null;
    private DeviceManageModule deviceManageModuleRef = null;
    private FirmwareManageModule firmwareManageModuleRef = null;
    private PsdkResourceModule psdkResourceModuleRef = null;
    private FlashLedModule flashLedModuleRef = null;
    private WaylineManageModule waylineManageModuleRef = null;
    private AIInsideManageModule aiInsideManageModuleRef = null;
    private ElementModule elementModuleRef = null;
    private AiAlertModule aiAlertModuleRef = null;
    private LiveSyncModule liveSyncModuleRef = null;

    public Workspace(IWorkspace workspace) {
        this.workspace = workspace;
        Logger.info(TAG, "Workspace instance created");
    }

    /**
     * 获取航线任务模块（分页列表、创建/编辑/删除/执行、状态更新、设备推荐、TCA 等）
     */
    public synchronized FlightTaskModule getFlightTaskModule() {
        if (flightTaskModuleRef != null) {
            return flightTaskModuleRef;
        }
        IFlightTaskModule raw = workspace.getFlightTaskModule();
        if (raw == null) {
            Logger.error(TAG, "getFlightTaskModule error: module is null");
            return null;
        }
        flightTaskModuleRef = new FlightTaskModule(raw);
        return flightTaskModuleRef;
    }

    /**
     * 获取媒体服务模块
     */
    public synchronized MediaServiceModule getMediaServiceModule() {
        if (mediaServiceModuleRef != null) {
            return mediaServiceModuleRef;
        }
        IMediaServiceModule raw = workspace.getMediaServiceModule();
        if (raw == null) {
            Logger.error(TAG, "getMediaServiceModule error: module is null");
            return null;
        }
        mediaServiceModuleRef = new MediaServiceModule(raw);
        return mediaServiceModuleRef;
    }

    /**
     * 获取 Terra 重建模块
     */
    public synchronized TerraBuildModule getTerraBuildModule() {
        if (terraBuildModuleRef != null) {
            return terraBuildModuleRef;
        }
        ITerraBuildModule raw = workspace.getTerraBuildModule();
        if (raw == null) {
            Logger.error(TAG, "getTerraBuildModule error: module is null");
            return null;
        }
        terraBuildModuleRef = new TerraBuildModule(raw);
        return terraBuildModuleRef;
    }

    /**
     * 获取安全服务模块
     */
    public synchronized SafetyServiceModule getSafetyServiceModule() {
        if (safetyServiceModuleRef != null) {
            return safetyServiceModuleRef;
        }
        ISafetyServiceModule raw = workspace.getSafetyServiceModule();
        if (raw == null) {
            Logger.error(TAG, "getSafetyServiceModule error: module is null");
            return null;
        }
        safetyServiceModuleRef = new SafetyServiceModule(raw);
        return safetyServiceModuleRef;
    }

    /**
     * 获取飞行任务记录模块
     */
    public synchronized FlightTaskRecordModule getFlightTaskRecordModule() {
        if (flightTaskRecordModuleRef != null) {
            return flightTaskRecordModuleRef;
        }
        IFlightTaskRecordModule raw = workspace.getFlightTaskRecordModule();
        if (raw == null) {
            Logger.error(TAG, "getFlightTaskRecordModule error: module is null");
            return null;
        }
        flightTaskRecordModuleRef = new FlightTaskRecordModule(raw);
        return flightTaskRecordModuleRef;
    }

    /**
     * 获取健康管理模块
     */
    public synchronized HMSModule getHMSModule() {
        if (hmsModuleRef != null) {
            return hmsModuleRef;
        }
        IHMSModule raw = workspace.getHMSModule();
        if (raw == null) {
            Logger.error(TAG, "getHMSModule error: module is null");
            return null;
        }
        hmsModuleRef = new HMSModule(raw);
        return hmsModuleRef;
    }

    /**
     * 获取设备日志管理模块
     */
    public synchronized LogManagementModule getLogManagementModule() {
        if (logManagementModuleRef != null) {
            return logManagementModuleRef;
        }
        ILogManagementModule raw = workspace.getLogManagementModule();
        if (raw == null) {
            Logger.error(TAG, "getLogManagementModule error: module is null");
            return null;
        }
        logManagementModuleRef = new LogManagementModule(raw);
        return logManagementModuleRef;
    }

    /**
     * 获取维护管理模块
     */
    public synchronized MaintenanceManagementModule getMaintenanceManagementModule() {
        if (maintenanceManagementModuleRef != null) {
            return maintenanceManagementModuleRef;
        }
        IMaintenanceManagementModule raw = workspace.getMaintenanceManagementModule();
        if (raw == null) {
            Logger.error(TAG, "getMaintenanceManagementModule error: module is null");
            return null;
        }
        maintenanceManagementModuleRef = new MaintenanceManagementModule(raw);
        Logger.info(TAG, "getMaintenanceManagementModule: module created");
        return maintenanceManagementModuleRef;
    }

    /**
     * 获取诊断管理模块
     */
    public synchronized DiagnosisManagementModule getDiagnosisManagementModule() {
        if (diagnosisManagementModuleRef != null) {
            return diagnosisManagementModuleRef;
        }
        IDiagnosisManagementModule raw = workspace.getDiagnosisManagementModule();
        if (raw == null) {
            Logger.error(TAG, "getDiagnosisManagementModule error: module is null");
            return null;
        }
        diagnosisManagementModuleRef = new DiagnosisManagementModule(raw);
        Logger.info(TAG, "getDiagnosisManagementModule: module created");
        return diagnosisManagementModuleRef;
    }

    /**
     * 获取设备管理模块
     */
    public synchronized DeviceManageModule getDeviceManageModule() {
        if (deviceManageModuleRef != null) {
            return deviceManageModuleRef;
        }
        IDeviceManageModule raw = workspace.getDeviceManageModule();
        if (raw == null) {
            Logger.error(TAG, "getDeviceManageModule error: module is null");
            return null;
        }
        deviceManageModuleRef = new DeviceManageModule(raw);
        return deviceManageModuleRef;
    }

    /**
     * 获取固件管理模块
     */
    public synchronized FirmwareManageModule getFirmwareManageModule() {
        if (firmwareManageModuleRef != null) {
            return firmwareManageModuleRef;
        }
        IFirmwareManageModule raw = workspace.getFirmwareManageModule();
        if (raw == null) {
            Logger.error(TAG, "getFirmwareManageModule error: module is null");
            return null;
        }
        firmwareManageModuleRef = new FirmwareManageModule(raw);
        return firmwareManageModuleRef;
    }

    /**
     * 获取 PSDK 资源模块
     */
    public synchronized PsdkResourceModule getPsdkResourceModule() {
        if (psdkResourceModuleRef != null) {
            return psdkResourceModuleRef;
        }
        IPsdkResourceModule raw = workspace.getPsdkResourceModule();
        if (raw == null) {
            Logger.error(TAG, "getPsdkResourceModule error: module is null");
            return null;
        }
        psdkResourceModuleRef = new PsdkResourceModule(raw);
        return psdkResourceModuleRef;
    }

    /**
     * 获取爆闪灯模板库模块（HTTP flash-configs）
     */
    public synchronized FlashLedModule getFlashLedModule() {
        if (flashLedModuleRef != null) {
            return flashLedModuleRef;
        }
        IFlashLedModule raw = workspace.getFlashLedModule();
        if (raw == null) {
            Logger.error(TAG, "getFlashLedModule error: module is null");
            return null;
        }
        flashLedModuleRef = new FlashLedModule(raw);
        return flashLedModuleRef;
    }

    /**
     * 获取航线管理模块
     */
    public synchronized WaylineManageModule getWaylineManageModule() {
        if (waylineManageModuleRef != null) {
            return waylineManageModuleRef;
        }
        IWaylineManageModule raw = workspace.getWaylineManageModule();
        if (raw == null) {
            Logger.error(TAG, "getWaylineManageModule error: module is null");
            return null;
        }
        waylineManageModuleRef = new WaylineManageModule(raw);
        return waylineManageModuleRef;
    }

    /**
     * 获取 AI Inside 机载算法管理模块（PAAS 纯血场景）
     */
    public synchronized AIInsideManageModule getAIInsideManageModule() {
        if (aiInsideManageModuleRef != null) { return aiInsideManageModuleRef; }
        IAIInsideManageModule raw = workspace.getAIInsideManageModule();
        if (raw == null) {
            Logger.error(TAG, "getAIInsideManageModule error: module is null");
            return null;
        }
        aiInsideManageModuleRef = new AIInsideManageModule(raw);
        return aiInsideManageModuleRef;
    }

    /** 获取元素管理模块（ElementService） */
    public synchronized ElementModule getElementModule() {
        if (elementModuleRef != null) { return elementModuleRef; }
        com.dji.flighthubsdk.generated.IElementModule raw = workspace.getElementModule();
        if (raw == null) {
            Logger.error(TAG, "getElementModule error: module is null");
            return null;
        }
        elementModuleRef = new ElementModule(raw);
        return elementModuleRef;
    }

    /**
     * 获取 AI 告警管理模块（PAAS 纯血场景）
     */
    public synchronized AiAlertModule getAIAlertModule() {
        if (aiAlertModuleRef != null) { return aiAlertModuleRef; }
        IAIAlertModule raw = workspace.getAIAlertModule();
        if (raw == null) {
            Logger.error(TAG, "getAIAlertModule error: module is null");
            return null;
        }
        aiAlertModuleRef = new AiAlertModule(raw);
        return aiAlertModuleRef;
    }

    /**
     * 获取直播转发模块（旁路转码器 CRUD）
     */
    public synchronized LiveSyncModule getLiveSyncModule() {
        if (liveSyncModuleRef != null) { return liveSyncModuleRef; }
        ILiveSyncModule raw = workspace.getLiveSyncModule();
        if (raw == null) {
            Logger.error(TAG, "getLiveSyncModule error: module is null");
            return null;
        }
        liveSyncModuleRef = new LiveSyncModule(raw);
        return liveSyncModuleRef;
    }
}
