// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drtc_user_engine_manager.h"

#include <base/common/logger.h>

using namespace message_engine;

DrtcUserEngineManager& DrtcUserEngineManager::GetInstance() {
    static DrtcUserEngineManager instance;
    return instance;
}

std::shared_ptr<DrtcUserEngineImpl> DrtcUserEngineManager::AcquireEngine(
    const std::string &room_id,
    const std::string &owner_id,
    const std::string &config_path,
    const std::shared_ptr<drc_sdk::Executor> &executor) {

    LOG_INFO("[DrtcUserEngineManager] AcquireEngine room_id: %s, owner_id: %s",
             room_id.c_str(), owner_id.c_str());

    std::lock_guard<std::mutex> lock(engine_map_mutex_);

    auto it = engine_map_.find(room_id);
    if (it != engine_map_.end()) {
        // Engine already exists, add owner reference
        it->second.owners.insert(owner_id);
        LOG_INFO("[DrtcUserEngineManager] Engine exists for room %s, added owner %s (total owners: %zu)",
                 room_id.c_str(), owner_id.c_str(), it->second.owners.size());
        return it->second.engine;
    }

    // Create new engine
    auto engine = std::make_shared<DrtcUserEngineImpl>(config_path, executor);
    DrtcUserEngineEntry entry;
    entry.engine = engine;
    entry.owners.insert(owner_id);
    engine_map_[room_id] = entry;

    LOG_INFO("[DrtcUserEngineManager] Created new engine for room %s, owner %s",
             room_id.c_str(), owner_id.c_str());
    return engine;
}

void DrtcUserEngineManager::ReleaseEngine(const std::string &room_id, const std::string &owner_id) {
    LOG_INFO("[DrtcUserEngineManager] ReleaseEngine room_id: %s, owner_id: %s",
             room_id.c_str(), owner_id.c_str());

    std::shared_ptr<DrtcUserEngineImpl> engine_to_cleanup;

    {
        std::lock_guard<std::mutex> lock(engine_map_mutex_);

        auto it = engine_map_.find(room_id);
        if (it == engine_map_.end()) {
            LOG_WARN("[DrtcUserEngineManager] ReleaseEngine: room %s not found", room_id.c_str());
            return;
        }

        it->second.owners.erase(owner_id);
        LOG_INFO("[DrtcUserEngineManager] Removed owner %s, remaining owners: %zu",
                 owner_id.c_str(), it->second.owners.size());

        if (it->second.owners.empty()) {
            // Last owner released, cleanup engine
            engine_to_cleanup = it->second.engine;
            engine_map_.erase(it);
            LOG_INFO("[DrtcUserEngineManager] Last owner released, cleaning up engine for room %s",
                     room_id.c_str());
        }
    }

    // Perform cleanup outside the lock to avoid deadlock
    if (engine_to_cleanup) {
        engine_to_cleanup->LeaveRoom();
        engine_to_cleanup->DeInit();
    }
}

bool DrtcUserEngineManager::HasEngine(const std::string &room_id) const {
    std::lock_guard<std::mutex> lock(engine_map_mutex_);
    return engine_map_.contains(room_id);
}
