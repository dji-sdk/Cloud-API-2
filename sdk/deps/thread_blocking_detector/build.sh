#!/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
cmake -S "$ROOT" -B "$ROOT/build" -DBUILD_BLOCKING_DETECTOR_DEMO=ON
cmake --build "$ROOT/build"
echo "Run: $ROOT/build/blocking_detector_demo"
