// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/psdk_resource_module.h"
#include "cloud_storage_uploader/cloud_storage_interface.h"
#include "common/util/async_capture_protect.h"

#include <memory>
#include <string>

namespace flight_hub_sdk {

class PsdkResourceModuleImpl : public IPsdkResourceModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    PsdkResourceModuleImpl();

    void getPsdkSpeakerResourceList(std::function<void(FlightHubError err, const PsdkSpeakerResourceList& psdk_speaker_resource_list)> callback) override;
    void createPsdkSpeakerResource(const CreatePsdkSpeakerResourceParams& params, std::function<void(FlightHubError err, const PsdkSpeakerResource& psdk_speaker_resource)> callback) override;
    void editPsdkSpeakerResource(const EditPsdkSpeakerResourceParams& params, std::function<void(FlightHubError err, const PsdkSpeakerResource& psdk_speaker_resource)> callback) override;
    void deletePsdkSpeakerResource(int resource_id, std::function<void(FlightHubError err)> callback) override;
    void getSTS(std::function<void(FlightHubError err, const STSInfo& sts_response)> callback) override;
    void getPsdkWidgetResources(const GetPsdkWidgetResourceParams& params, std::function<void(FlightHubError err, const PsdkWidgetResourceList& psdk_widget_resource_list)> callback) override;
    void uploadSpeakerAudioFile(const std::string& file_path, const STSInfo& sts_info, std::function<void(FlightHubError err)> callback) override;

private:
    std::shared_ptr<ICloudStorageUploader> getCloudStorageUploader(const std::string& provider);

};

}  // namespace flight_hub_sdk
