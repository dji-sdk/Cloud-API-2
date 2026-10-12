// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <string>
#include <vector>
#include <functional>
#include <memory>

#include "message.h"
#include "flight_task_module.h"
#include "media_service_module.h"
#include "safety_service_module.h"
#include "flight_task_record_module.h"
#include "diagnose_management_module.h"
#include "maintenance_management_module.h"
#include "log_management_module.h"
#include "hms_module.h"
#include "terra_build.h"
#include "device_manage_module.h"
#include "firmware_manage_module.h"
#include "psdk_resource_module.h"
#include "flash_led_module.h"
#include "wayline_manage_module.h"
#include "ai_inside_manage_module.h"
#include "element.h"
#include "ai_alert_module.h"
#include "live_sync_module.h"
namespace flight_hub_sdk {

/// 工作空间模块接口，提供设备管理、媒体服务、航线/固件/任务管理、安全服务、Terra 重建等全局 module getter。
class IWorkspace {
public:
    virtual ~IWorkspace() = default;

    /// @brief 获取设备日志管理模块
    virtual std::shared_ptr<ILogManagementModule> getLogManagementModule() = 0;

    /// @brief 获取健康管理模块
    virtual std::shared_ptr<IHMSModule> getHMSModule() = 0;

    /// @brief 获取媒体服务模块
    virtual std::shared_ptr<IMediaServiceModule> getMediaServiceModule() = 0;

    /// @brief 获取安全服务模块
    virtual std::shared_ptr<ISafetyServiceModule> getSafetyServiceModule() = 0;

    /// @brief 获取飞行任务记录模块
    virtual std::shared_ptr<IFlightTaskRecordModule> getFlightTaskRecordModule() = 0;

    /// @brief 获取诊断管理模块
    virtual std::shared_ptr<IDiagnosisManagementModule> getDiagnosisManagementModule() = 0;

    /// @brief 获取维护管理模块
    virtual std::shared_ptr<IMaintenanceManagementModule> getMaintenanceManagementModule() = 0;

    /// @brief 获取航线任务模块（分页列表、创建/编辑/删除/执行、状态更新、设备推荐、TCA 等）
    virtual std::shared_ptr<IFlightTaskModule> getFlightTaskModule() = 0;

    /// @brief 获取设备管理模块
    virtual std::shared_ptr<IDeviceManageModule> getDeviceManageModule() = 0;

    /// @brief 获取固件管理模块
    virtual std::shared_ptr<IFirmwareManageModule> getFirmwareManageModule() = 0;

    /// @brief 获取航线管理模块
    virtual std::shared_ptr<IWaylineManageModule> getWaylineManageModule() = 0;

    /// @brief 获取 PSDK 资源模块
    virtual std::shared_ptr<IPsdkResourceModule> getPsdkResourceModule() = 0;

    /// @brief 获取爆闪灯模板库模块（HTTP flash-configs）
    virtual std::shared_ptr<IFlashLedModule> getFlashLedModule() = 0;

    /// @brief 获取 Terra 重建模块
    virtual std::shared_ptr<ITerraBuildModule> getTerraBuildModule() = 0;

    /// @brief 获取 AI Inside 机载算法管理模块（PAAS 纯血场景）
    virtual std::shared_ptr<IAIInsideManageModule> getAIInsideManageModule() = 0;

    /// @brief 获取元素管理模块（ElementService）
    virtual std::shared_ptr<IElementModule> getElementModule() = 0;
    /// @brief 获取 AI 告警配置与查询模块（PaaS ai-alert，/drc/paas/v2/ai-alert）
    virtual std::shared_ptr<IAIAlertModule> getAIAlertModule() = 0;

    /// @brief 获取直播转发模块（旁路转码器 CRUD，/manage/api/v1/paas/c2c/live-sync）
    virtual std::shared_ptr<ILiveSyncModule> getLiveSyncModule() = 0;
};

} // namespace flight_hub_sdk
