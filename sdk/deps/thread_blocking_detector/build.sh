#!/bin/bash

# Copyright (c) 2026 SZ DJI Technology Co., Ltd.
# SPDX-License-Identifier: Apache-2.0

set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
cmake -S "$ROOT" -B "$ROOT/build" -DBUILD_BLOCKING_DETECTOR_DEMO=ON
cmake --build "$ROOT/build"
echo "Run: $ROOT/build/blocking_detector_demo"
