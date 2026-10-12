// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "logger_plog.h"

#define LOG_DEBUG(...) PLOGD << drc_sdk::PlogLogger::FormatMessage(__VA_ARGS__)
#define LOG_INFO(...)  PLOGI << drc_sdk::PlogLogger::FormatMessage(__VA_ARGS__)
#define LOG_WARN(...)  PLOGW << drc_sdk::PlogLogger::FormatMessage(__VA_ARGS__)
#define LOG_ERROR(...) PLOGE << drc_sdk::PlogLogger::FormatMessage(__VA_ARGS__)
