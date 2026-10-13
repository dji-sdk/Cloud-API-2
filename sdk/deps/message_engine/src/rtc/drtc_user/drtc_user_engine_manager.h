// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <mutex>
#include <unordered_map>
#include <string>
#include <set>

#include <base/common/macros.h>
#include <common/util/async_capture_protect.h>
#include <base/async/executor.h>

#include "drtc_user_engine_impl.h"

namespace message_engine {

// Engine instance with reference counting by owner
struct DrtcUserEngineEntry {
    std::shared_ptr<DrtcUserEngineImpl> engine;
    std::set<std::string> owners;  // owner_id set for reference counting
};

class DrtcUserEngineManager final : public drc_sdk::common::AsyncCaptureProtect {
public:
    static DrtcUserEngineManager& GetInstance();
    ~DrtcUserEngineManager() override = default;

    // Acquire engine for a given room_id with owner_id reference
    // If engine doesn't exist for room_id, creates a new one
    std::shared_ptr<DrtcUserEngineImpl> AcquireEngine(const std::string &room_id,
                                                       const std::string &owner_id,
                                                       const std::string &config_path,
                                                       const std::shared_ptr<drc_sdk::Executor> &executor);

    // Release engine reference for the given owner
    // If last owner, triggers LeaveRoom + DeInit and removes the engine
    void ReleaseEngine(const std::string &room_id, const std::string &owner_id);

    // Check if engine exists for room_id
    bool HasEngine(const std::string &room_id) const;

    DISALLOW_COPY_AND_ASSIGN(DrtcUserEngineManager);

private:
    DrtcUserEngineManager() = default;

    mutable std::mutex engine_map_mutex_;
    std::unordered_map<std::string, DrtcUserEngineEntry> engine_map_;  // room_id -> entry
};

}  // namespace message_engine
