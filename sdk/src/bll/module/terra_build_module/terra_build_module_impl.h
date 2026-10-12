// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/terra_build.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class TerraBuildModuleImpl : public ITerraBuildModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit TerraBuildModuleImpl();
    ~TerraBuildModuleImpl();

    void getReconstructionSettings(
        const std::string& file_uuid,
        std::function<void(FlightHubError err, const TerraBuildSettingsResult& result)> callback) override;

    void getQualityReport(
        const std::string& file_uuid,
        const std::string& language,
        const std::string& measure_unit_system,
        std::function<void(FlightHubError err, const TerraBuildQualityReportResult& result)> callback) override;

    void getModelFileList(
        int page,
        int page_size,
        std::function<void(FlightHubError err, const TerraBuildModelFileListResult& result)> callback) override;

    void getModelFileUrl(
        int file_id,
        int type,
        std::function<void(FlightHubError err, const TerraBuildModelFileUrlResult& result)> callback) override;

    void createReconstruction(
        const CreateReconstructionParams& params,
        std::function<void(FlightHubError err, const TerraBuildCreateReconstructionResult& result)> callback) override;

    void retryReconstruction(
        const std::string& file_uuid,
        std::function<void(FlightHubError err)> callback) override;

    void stopReconstruction(
        const std::string& file_uuid,
        std::function<void(FlightHubError err)> callback) override;

    void getSfmResult(
        const std::string& file_uuid,
        std::function<void(FlightHubError err, const TerraBuildSfmResult& result)> callback) override;

    void deleteModelFile(
        int file_id,
        std::function<void(FlightHubError err)> callback) override;

    void batchDeleteModelFiles(
        const std::vector<int>& file_ids,
        std::function<void(FlightHubError err)> callback) override;

    void createPanorama(
        const CreatePanoramaParams& params,
        std::function<void(FlightHubError err, const TerraBuildCreatePanoramaResult& result)> callback) override;

    void getReconstructionImages(
        const std::string& file_uuid,
        std::function<void(FlightHubError err, const TerraBuildReconstructionImagesResult& result)> callback) override;

    int listenReconstructionStatusChanged(
        const std::function<void(const TerraBuildReconstructionStatusChanged& msg)>& callback) override;

    void cancelListenReconstructionStatusChanged(int listen_id) override;

    void uploadModelCallback(
        const TerraUploadCallbackParams& params,
        std::function<void(FlightHubError err, const TerraUploadCallbackResult& result)> callback) override;

    void renameModel(
        int file_id,
        const TerraRenameModelParams& params,
        std::function<void(FlightHubError err)> callback) override;

    void showModelOnMap(
        const TerraShowOnMapParams& params,
        std::function<void(FlightHubError err, const TerraShowOnMapResult& result)> callback) override;

    int listenTerraUnzipProgress(
        const std::function<void(const TerraUnzipProgressMsg& msg)>& callback) override;

    void cancelListenTerraUnzipProgress(int listen_id) override;

    int listenTerraModelStatusChanged(
        const std::function<void(const TerraModelStatusChangedMsg& msg)>& callback) override;

    void cancelListenTerraModelStatusChanged(int listen_id) override;

    int listenTerraFileShowOnMap(
        const std::function<void(const TerraFileShowOnMapMsg& msg)>& callback) override;

    void cancelListenTerraFileShowOnMap(int listen_id) override;

    int listenTerraMediaFileHideOnMap(
        const std::function<void(const TerraMediaFileHideOnMapMsg& msg)>& callback) override;
    void cancelListenTerraMediaFileHideOnMap(int listen_id) override;

    int listenTerraMediaFileShowOnMap(
        const std::function<void(const TerraMediaFileShowOnMapMsg& msg)>& callback) override;
    void cancelListenTerraMediaFileShowOnMap(int listen_id) override;

    void getModelDetail(
        int file_id,
        std::function<void(FlightHubError err, const TerraModelDetailResult& result)> callback) override;

    void setLasWkt(
        const TerraSetLasWktParams& params,
        std::function<void(FlightHubError err)> callback) override;

private:
    int instance_id_ = 0;
    std::string log_tag_;  // 格式: "TerraBuild<instance_id>"
};

}  // namespace flight_hub_sdk
