// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/element.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class ElementModuleImpl
    : public IElementModule
    , public drc_sdk::common::AsyncCaptureProtect {
public:
    ElementModuleImpl() = default;
    ~ElementModuleImpl() override = default;

    void createElement(
        const CreateElementParams& params,
        std::function<void(FlightHubError err, const CreateElementResult& result)> callback) override;

    void updateElement(
        const UpdateElementParams& params,
        std::function<void(FlightHubError err, const UpdateElementResult& result)> callback) override;

    void listElementHierarchy(
        const ListElementHierarchyParams& params,
        std::function<void(FlightHubError err, const ListElementHierarchyResult& result)> callback) override;

    void setElementVisibility(
        const SetElementVisibilityParams& params,
        std::function<void(FlightHubError err, const SetElementVisibilityResult& result)> callback) override;

    void deleteElement(
        const DeleteElementParams& params,
        std::function<void(FlightHubError err, const DeleteElementResult& result)> callback) override;

    int listenElementCreate(
        const std::function<void(const ElementCreateMsg& msg)>& callback) override;
    void cancelListenElementCreate(int listen_id) override;

    int listenElementUpdate(
        const std::function<void(const ElementUpdateMsg& msg)>& callback) override;
    void cancelListenElementUpdate(int listen_id) override;

    int listenElementDelete(
        const std::function<void(const ElementDeleteMsg& msg)>& callback) override;
    void cancelListenElementDelete(int listen_id) override;
};

}  // namespace flight_hub_sdk
