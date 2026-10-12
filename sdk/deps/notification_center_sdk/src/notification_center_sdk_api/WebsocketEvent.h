// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <string>
#include "APIExport.h"

namespace notification_center {
    class NOTIFICATION_CENTER_API WebsocketEvent {
    public:
        virtual ~WebsocketEvent() = default;

        virtual void OnOpen() = 0;

        virtual void OnClose() = 0;

        virtual void OnError(const std::string &error) = 0;
    };
}
