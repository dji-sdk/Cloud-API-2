// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#ifdef _WIN32
    #ifdef NOTIFICATION_CENTER_SDK_EXPORTS
        #define NOTIFICATION_CENTER_API __declspec(dllexport)
    #else
        #define NOTIFICATION_CENTER_API __declspec(dllimport)
    #endif
#else
    #ifdef NOTIFICATION_CENTER_SDK_EXPORTS
        #define NOTIFICATION_CENTER_API __attribute__((visibility("default")))
    #else
        #define NOTIFICATION_CENTER_API __attribute__((visibility("default")))
    #endif
#endif
