// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "workspace.h"
#include "common/util/async_capture_protect.h"
#include "bll/module/flight_task_module/flight_task_module_impl.h"
#include "bll/module/media_service_module/media_service_module_impl.h"
#include "bll/module/safety_service_module/safety_service_module_impl.h"
#include "bll/module/flight_task_record_module/flight_task_record_module_impl.h"
#include "bll/module/log_management_module/log_management_module_impl.h"
#include "bll/module/hms_module/hms_module_impl.h"
#include "bll/module/terra_build_module/terra_build_module_impl.h"
#include "bll/module/diagnose_management_module/diagnose_management_module_impl.h"
#include "bll/module/maintenance_management_module/maintenance_management_module_impl.h"
#include "bll/module/device_manage_module/device_manage_module_impl.h"
#include "bll/module/firmware_manage_module/firmware_manage_module_impl.h"
#include "bll/module/psdk_resource_module/psdk_resource_module_impl.h"
#include "bll/module/flash_led_module/flash_led_module_impl.h"
#include "bll/module/wayline_manage_module/wayline_manage_module_impl.h"
#include "bll/module/ai_inside_manage_module/ai_inside_manage_module_impl.h"
#include "bll/module/element_module/element_module_impl.h"
#include "bll/module/ai_alert_module/ai_alert_module_impl.h"
#include "bll/module/live_sync_module/live_sync_module_impl.h"
#include <memory>
#include <mutex>

namespace flight_hub_sdk {

class WorkspaceImpl : public IWorkspace, public drc_sdk::common::AsyncCaptureProtect {
public:
    WorkspaceImpl();
    ~WorkspaceImpl() override;

    // 获取缓存的设备列表
    std::vector<DeviceTopo> getDeviceList();

    std::shared_ptr<IMediaServiceModule> getMediaServiceModule() override;
    std::shared_ptr<ISafetyServiceModule> getSafetyServiceModule() override;
    std::shared_ptr<IFlightTaskRecordModule> getFlightTaskRecordModule() override;
    std::shared_ptr<ILogManagementModule> getLogManagementModule() override;
    std::shared_ptr<IHMSModule> getHMSModule() override;
    std::shared_ptr<IFlightTaskModule> getFlightTaskModule() override;

    std::shared_ptr<IDeviceManageModule> getDeviceManageModule() override;
    std::shared_ptr<IFirmwareManageModule> getFirmwareManageModule() override;
    std::shared_ptr<IWaylineManageModule> getWaylineManageModule() override;
    std::shared_ptr<IPsdkResourceModule> getPsdkResourceModule() override;
    std::shared_ptr<IFlashLedModule> getFlashLedModule() override;

    std::shared_ptr<ITerraBuildModule> getTerraBuildModule() override;
    std::shared_ptr<IDiagnosisManagementModule> getDiagnosisManagementModule() override;
    std::shared_ptr<IMaintenanceManagementModule> getMaintenanceManagementModule() override;

    std::shared_ptr<IAIInsideManageModule> getAIInsideManageModule() override;

    std::shared_ptr<IElementModule> getElementModule() override;
    std::shared_ptr<IAIAlertModule> getAIAlertModule() override;
    std::shared_ptr<ILiveSyncModule> getLiveSyncModule() override;

private:
    std::mutex device_manage_module_mutex_;
    std::shared_ptr<DeviceManageModuleImpl> device_manage_module_ = nullptr;

    std::mutex firmware_manage_module_mutex_;
    std::shared_ptr<FirmwareManageModuleImpl> firmware_manage_module_ = nullptr;

    std::mutex psdk_resource_module_mutex_;
    std::shared_ptr<PsdkResourceModuleImpl> psdk_resource_module_ = nullptr;

    std::mutex flash_led_module_mutex_;
    std::shared_ptr<FlashLedModuleImpl> flash_led_module_ = nullptr;

    std::mutex wayline_manage_module_mutex_;
    std::shared_ptr<WaylineManageModuleImpl> wayline_manage_module_ = nullptr;

    std::shared_ptr<FlightTaskModuleImpl> flight_task_module_ = nullptr;

    std::mutex media_service_module_mutex_;
    std::shared_ptr<MediaServiceModuleImpl> media_service_module_ = nullptr;

    std::mutex safety_service_module_mutex_;
    std::shared_ptr<SafetyServiceModuleImpl> safety_service_module_ = nullptr;

    std::mutex flight_task_record_module_mutex_;
    std::shared_ptr<FlightTaskRecordModuleImpl> flight_task_record_module_ = nullptr;

    std::shared_ptr<LogManagementModuleImpl> log_management_module_ = nullptr;
    std::shared_ptr<HMSModuleImpl> hms_module_ = nullptr;

    std::mutex terra_build_module_mutex_;
    std::shared_ptr<TerraBuildModuleImpl> terra_build_module_ = nullptr;

    std::mutex diagnosis_management_module_mutex_;
    std::shared_ptr<DiagnosisManagementModuleImpl> diagnosis_management_module_ = nullptr;

    std::mutex maintenance_management_module_mutex_;
    std::shared_ptr<MaintenanceManagementModuleImpl> maintenance_management_module_ = nullptr;

    std::mutex ai_inside_manage_module_mutex_;
    std::shared_ptr<AIInsideManageModuleImpl> ai_inside_manage_module_ = nullptr;

    std::mutex element_module_mutex_;
    std::shared_ptr<ElementModuleImpl> element_module_ = nullptr;
    std::mutex ai_alert_module_mutex_;
    std::shared_ptr<AIAlertModuleImpl> ai_alert_module_ = nullptr;

    std::mutex live_sync_module_mutex_;
    std::shared_ptr<LiveSyncModuleImpl> live_sync_module_ = nullptr;
};

}
