// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/wayline_manage_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class WaylineManageModuleImpl : public IWaylineManageModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit WaylineManageModuleImpl();

    void getWaylineList(const GetWaylineListParam& param, std::function<void(FlightHubError err, GetWaylineListResult result)> callback) override;
    void getWaylineDetail(const std::string& wayline_uuid, std::function<void(FlightHubError err, GetWaylineDetailResult result)> callback) override;
    void batchDeleteWaylines(const std::vector<std::string>& wayline_uuids,
        std::function<void(FlightHubError err, BatchDeleteWaylinesResult result)> callback) override;
    void updateWayline(const std::string& wayline_uuid, const std::string& wayline_name, std::function<void(FlightHubError err, UpdateWaylineResult result)> callback) override;
    void submitWaylineInfo(const SubmitWaylineInfoParam& param, std::function<void(FlightHubError err)> callback) override;
    void batchDownloadWaylines(const BatchDownloadWaylinesParam& param, std::function<void(FlightHubError err, BatchDownloadWaylinesResult result)> callback) override;
    void duplicateWayline(const DuplicateWaylineParam& param, std::function<void(FlightHubError err, DuplicateWaylineResult result)> callback) override;
    void copyWayline(const CopyWaylineParam& param, std::function<void(FlightHubError err, CopyWaylineResult result)> callback) override;
    void concatWayline(const ConcatWaylineParam& param, std::function<void(FlightHubError err, ConcatWaylineResult result)> callback) override;
    void importKmz(const std::vector<std::string>& file_paths, std::function<void(FlightHubError err, ImportKmzResult result)> callback) override;
    int listenImportWaylineEvent(const std::function<void(const ImportWaylineEventResult& result)>& callback) override;
    void cancelListenImportWaylineEvent(int listen_id) override;

    void getWaylineEditorSts(
        const GetWaylineEditorStsParams& params,
        std::function<void(FlightHubError err, const GetWaylineEditorStsResult& result)> callback) override;

private:
};

}  // namespace flight_hub_sdk
