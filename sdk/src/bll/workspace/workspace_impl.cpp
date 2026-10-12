// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "workspace_impl.h"
#include <base/common/logger.h>
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

namespace flight_hub_sdk {

WorkspaceImpl::WorkspaceImpl()
{
    LOG_INFO("WorkspaceImpl: created");
}

WorkspaceImpl::~WorkspaceImpl() = default;

std::vector<DeviceTopo> WorkspaceImpl::getDeviceList() {
    auto module = std::static_pointer_cast<DeviceManageModuleImpl>(getDeviceManageModule());
    return module->getCachedDeviceList();
}

std::shared_ptr<IDeviceManageModule> WorkspaceImpl::getDeviceManageModule() {
    std::lock_guard<std::mutex> lock_gd(device_manage_module_mutex_);
    if (!device_manage_module_) {
        device_manage_module_ = std::make_shared<DeviceManageModuleImpl>();
    }
    return device_manage_module_;
}

std::shared_ptr<IFirmwareManageModule> WorkspaceImpl::getFirmwareManageModule() {
    std::lock_guard<std::mutex> lock_gd(firmware_manage_module_mutex_);
    if (!firmware_manage_module_) {
        firmware_manage_module_ = std::make_shared<FirmwareManageModuleImpl>();
    }
    return firmware_manage_module_;
}

std::shared_ptr<IWaylineManageModule> WorkspaceImpl::getWaylineManageModule() {
    std::lock_guard<std::mutex> lock_gd(wayline_manage_module_mutex_);
    if (!wayline_manage_module_) {
        wayline_manage_module_ = std::make_shared<WaylineManageModuleImpl>();
    }
    return wayline_manage_module_;
}

std::shared_ptr<IPsdkResourceModule> WorkspaceImpl::getPsdkResourceModule() {
    std::lock_guard<std::mutex> lock_gd(psdk_resource_module_mutex_);
    if (!psdk_resource_module_) {
        psdk_resource_module_ = std::make_shared<PsdkResourceModuleImpl>();
    }
    return psdk_resource_module_;
}

std::shared_ptr<IFlashLedModule> WorkspaceImpl::getFlashLedModule() {
    std::lock_guard<std::mutex> lock_gd(flash_led_module_mutex_);
    if (!flash_led_module_) {
        flash_led_module_ = std::make_shared<FlashLedModuleImpl>();
    }
    return flash_led_module_;
}

std::shared_ptr<IMediaServiceModule> WorkspaceImpl::getMediaServiceModule() {
    std::lock_guard<std::mutex> lock_gd(media_service_module_mutex_);

    if (!media_service_module_) {
        media_service_module_ = std::make_shared<MediaServiceModuleImpl>();
    }

    return media_service_module_;
}

std::shared_ptr<ISafetyServiceModule> WorkspaceImpl::getSafetyServiceModule() {
    std::lock_guard<std::mutex> lock_gd(safety_service_module_mutex_);

    if (!safety_service_module_) {
        safety_service_module_ = std::make_shared<SafetyServiceModuleImpl>();
    }

    return safety_service_module_;
}

std::shared_ptr<IFlightTaskRecordModule> WorkspaceImpl::getFlightTaskRecordModule() {
    std::lock_guard<std::mutex> lock_gd(flight_task_record_module_mutex_);

    if (!flight_task_record_module_) {
        flight_task_record_module_ = std::make_shared<FlightTaskRecordModuleImpl>();
    }

    return flight_task_record_module_;
}

std::shared_ptr<ILogManagementModule> WorkspaceImpl::getLogManagementModule() {
    if (!log_management_module_) {
        log_management_module_ = std::make_shared<LogManagementModuleImpl>();
    }
    return log_management_module_;
}

std::shared_ptr<IHMSModule> WorkspaceImpl::getHMSModule() {
    if (!hms_module_) {
        hms_module_ = std::make_shared<HMSModuleImpl>();
    }
    return hms_module_;
}

std::shared_ptr<IFlightTaskModule> WorkspaceImpl::getFlightTaskModule() {
    if (!flight_task_module_) {
        flight_task_module_ = std::make_shared<FlightTaskModuleImpl>();
    }
    return flight_task_module_;
}

std::shared_ptr<ITerraBuildModule> WorkspaceImpl::getTerraBuildModule() {
    std::lock_guard<std::mutex> lock_gd(terra_build_module_mutex_);
    if (!terra_build_module_) {
        terra_build_module_ = std::make_shared<TerraBuildModuleImpl>();
    }
    return terra_build_module_;
}

std::shared_ptr<IDiagnosisManagementModule> WorkspaceImpl::getDiagnosisManagementModule() {
    std::lock_guard<std::mutex> lock_gd(diagnosis_management_module_mutex_);
    if (!diagnosis_management_module_) {
        diagnosis_management_module_ = std::make_shared<DiagnosisManagementModuleImpl>();
    }

    return diagnosis_management_module_;
}

std::shared_ptr<IMaintenanceManagementModule> WorkspaceImpl::getMaintenanceManagementModule() {
    std::lock_guard<std::mutex> lock_gd(maintenance_management_module_mutex_);
    if (!maintenance_management_module_) {
        maintenance_management_module_ = std::make_shared<MaintenanceManagementModuleImpl>();
    }

    return maintenance_management_module_;
}

std::shared_ptr<IAIInsideManageModule> WorkspaceImpl::getAIInsideManageModule() {
    std::lock_guard<std::mutex> lock_gd(ai_inside_manage_module_mutex_);
    if (!ai_inside_manage_module_) {
        ai_inside_manage_module_ = std::make_shared<AIInsideManageModuleImpl>();
    }
    return ai_inside_manage_module_;
}

std::shared_ptr<IElementModule> WorkspaceImpl::getElementModule() {
    std::lock_guard<std::mutex> lock_gd(element_module_mutex_);
    if (!element_module_) {
        element_module_ = std::make_shared<ElementModuleImpl>();
    }
    return element_module_;
}

std::shared_ptr<IAIAlertModule> WorkspaceImpl::getAIAlertModule() {
    std::lock_guard<std::mutex> lock_gd(ai_alert_module_mutex_);
    if (!ai_alert_module_) {
        ai_alert_module_ = std::make_shared<AIAlertModuleImpl>();
    }
    return ai_alert_module_;
}

std::shared_ptr<ILiveSyncModule> WorkspaceImpl::getLiveSyncModule() {
    std::lock_guard<std::mutex> lock_gd(live_sync_module_mutex_);
    if (!live_sync_module_) {
        live_sync_module_ = std::make_shared<LiveSyncModuleImpl>();
    }
    return live_sync_module_;
}

}  // namespace flight_hub_sdk
