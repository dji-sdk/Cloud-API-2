// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>

namespace flight_hub_sdk {

    // Feature ID 汇总;
    // C++14 以后支持哈希容器存放 enum class 对象;
    // 将相近的 Feature 能力聚在一起;

    // todo: 不断抽象和完善特性列表, 形成一个一致的长期可用的特性列表;
    enum class FeatureId : uint32_t {
        DRC_LinkConnect,                    // 建立 DRC 链路连接;
        DRC_LinkDisconnect,                 // 断开 DRC 链路连接;
        DRC_RtcQualityTest,                 // RTC 质量测试;

        CA_FlightControl,                   // 控制权(CA: ControlAuthority), 飞行控制权;
        CA_PayloadControl,                  // 控制权(CA: ControlAuthority), 负载控制权;

        TakeOff,                            // 一键起飞;
        FlyToPoint,                         // 飞向目标点;
        POI,                                // POI 环绕飞行;

        LiveStart,                          // 开启直播;
        LiveStop,                           // 停止直播;
        LiveSetLiveQuality,                 // 设置直播画质;
        LiveRecordStart,                    // 直播录制 - 开启;
        LiveRecordStop,                     // 直播录制 - 停止;
        LiveGetDeviceLiveRecords,           // 获取设备直播录制;

        FC_EmergencyStop,                   // 飞行控制(FC: FlightControl): 急停;
        FC_EmergencyLanding,                // 飞行控制(FC: EmergencyLanding): 急降;
        FC_ForceLanding,                    // 飞行控制(FC: ForceLanding): 急降;
        FC_ReturnHome,                      // 飞行控制(FC: ReturnHome): 返航;
        FC_ReturnHomeCancel,                // 飞行控制(FC: ReturnHomeCancel): 取消返航;
        FC_StickControl,                    // 飞行控制(FC: StickControl): 摇杆操控(WASD);
        FC_SetDroneHover,
        FC_SetFollowMouseParameter,
        FC_SetNightLightsState,
        FC_SetStealthState,
        FC_OpenParachute,
        FC_SetFlashLed,
        FC_CameraLookAt,

        FS_SetFlightSmartPlanMode,         // 飞行设置(FS: FlightSetting);
        FS_SetOutOfControlAction,
        FS_SetFlightTaskHeight,
        FS_SetRthAltitude,
        FS_SetAirTransferEnable,

        CC_SwitchLiveLens,                  // 相机控制(CC: CameraControl): 切换直播镜头;
        CC_SwitchCameraMode,                // 相机控制(CC: CameraControl): 切换相机模式;
        CC_TakePhoto,                       // 相机控制(CC: CameraControl): 拍照;
        CC_SmartPhoto,
        CC_StartRecord,                     // 开启录像;
        CC_StopRecord,                      // 停止录像;
        CC_ZoomAtTarget,                    // 框选变焦;
        CC_SetFocalLength,                  // 变焦;
        CC_SetLinkageZoom,
        CC_ResetGimbal,                     // 重置云台;
        CC_LookAtTarget,                    // 看向目标;
        CC_AimAtTarget,                     // 瞄准目标;
        CC_DragCameraScreen,                // (相机)云台拖拽 drcSendCameraScreenDrag;
        CC_Panorama,                        // (相机)全景拍照;
        CC_SetPhotoSize,
        CC_PhotoStorageSetting,             // 照片存储设置;
        CC_VideoStorageSetting,             // 视频存储设置;
        CC_SetNightSceneMode,               // 夜景模式设置;
        CC_IntervalPhoto,                   // 定时拍照;
        CC_SetThermalGainMode,
        CC_SetCameraApertureValue,
        CC_SetCameraExposureMode,
        CC_SetCameraExposureValue,
        CC_SetCameraFocusMode,
        CC_SetCameraFocusValue,
        CC_SetCameraIso,
        CC_SetCameraMechanicalShutter,
        CC_CameraPointFocusAction,
        CC_SetCameraShutter,
        CC_SetCameraWatermark,
        CC_SetCameraDewarping,
        CC_SetCameraScreenSplit,
        CC_CleanFog,

        // 热成像 / 红外 (Thermal/IR);
        CC_SetThermalCurrentPalette,            // 热成像调色板;
        CC_SetThermalSuperResolutionEnable,     // 热成像超分辨率开关;
        CC_SetIrMeteringMode,                   // 红外测温模式;
        CC_SetIrMeteringPoint,                  // 红外点测温;
        CC_SetIrMeteringArea,                   // 红外区域测温;
        CC_SetIrDisplayMode,                    // 红外显示模式;
        CC_SetIrEnhanceMode,                    // 红外增强模式;
        CC_SetIrMixRate,                        // 红外混合比例;
        CC_SetIsothermEnable,                   // 等温线开关;
        CC_SetIsothermColorRenderingType,       // 等温线着色类型;
        CC_SetIsothermLowerUpperRange,          // 等温线上下限区间;
        CC_SetIsothermRenderingPolygon,         // 等温线多边形渲染;
        CC_SetIsothermRoiBasedRenderingEnable,  // 基于 ROI 的等温线渲染开关;
        CC_SetInfraredFillLightEnable,          // 红外补光灯开关;
        CC_SetCameraNightVisionEnable,          // 相机夜视开关;
        CC_ResetCameraSettings,                 // 重置相机设置;

        // 其它相机 DRC (Other Camera DRC);
        CC_SetCameraDenoiseLevel,           // 相机降噪等级;
        CC_SetVideoResolution,              // 视频分辨率;
        CC_SetCameraPhotoFormat,            // 照片格式;
        CC_SetPreRecordedStatus,            // 预录状态;
        CC_SetPreRecordedTime,              // 预录时长;
        CC_SetCountDownAction,              // 倒计时动作;
        CC_SetAssistantVideoMode,           // 辅助视频模式;
        CC_SetAssistantVideoSwitch,         // 辅助视频开关;
        CC_SetAlgorithmDehazeMode,          // 去雾算法模式;
        CC_SetAlgorithmDehazeLevel,         // 去雾算法等级;

        DC_ChangeDockCameraPosition,        // 机场控制(DC: DockControl), 切换摄像头;
        DC_SetDockSupplementLightState,
        DC_SetDockChargeState,              // 机场控制(DC: DockControl), 充电状态;
        DC_SetDockCoverState,               // 机场控制(DC: DockControl), 舱盖状态;
        DC_SetDockAirConditionerMode,       // 机场控制(DC: DockControl), 空调模式;

        RtkCalibration,                     // RTK 校正;

        AIBox,
        AIInsideManage,
        PSDK_Device,                        // PSDK 设备列表/设备状态;
        PSDK_Resource,                      // PSDK 资源管理;
        PSDK_Speaker,                       // PSDK 喊话器;
        PSDK_SearchLight,                   // PSDK 探照灯;

        AIControl,
        VisualScoutDeploy,                  // 踏勘部署模块;
        WaylineManage,                      // 航线管理模块;
        FlightTaskControl,                  // 飞行任务控制(航线任务控制)模块;
        FlightTask,                         // 飞行任务管理(航线任务管理)模块;
        FlightTaskRecord,                   // 飞行记录导出;
        MediaService,                       // 媒体服务模块;
        SafetyService,                      // 安全服务模块;
        DeviceDebug,                        // 设备调试模块;
        DeviceHMS,                          // Device HMS;
        DiagnosisManagement,                // 设备定损模块；
        FirmwareManage,                     // 固件管理模块;
        LogManagement,                      // 日志管理模块;
        MaintenanceManagement,              // (设备)报修管理模块;
        NetworkTest,                        // 网络测试模块;

        TerraBuild,                         // Terra 模块;
    };




    // Feature 状态定义;
    enum class FeatureStatus {
        NotFound = -1,               // model_key 没找到(或者 model_key 无效);
        NotConfigured = 0,           // model_key 对应 Feature 没有配置;
        Unsupported = 1              // model_key 对应 Feature 配置为"不支持";
    };

}  // end of namespace flight_hub_sdk