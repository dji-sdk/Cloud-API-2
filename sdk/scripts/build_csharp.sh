#!/usr/bin/env bash
# .NET 8 / Linux x64 SDK; run from any working directory.
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
configuration=Release
version="$(tr -d '[:space:]' < "$ROOT_DIR/VERSION")"
jobs="${FHSDK_JOBS:-${FHSDK_MAX_JOBS:-$(nproc 2>/dev/null || echo 4)}}"
for arg in "$@"; do
    case "$arg" in
        -h|--help)
            echo 'Usage: bash scripts/build_csharp.sh [-r|--release|-d|--debug] [-v=X.Y.Z] [-j=N|--jobs=N]'
            echo 'Output: build/csharp/dist/flight_hub_sdk.<version>.nupkg + flight_hub_sdk.dll'
            exit 0 ;;
        -d|--debug) configuration=Debug ;;
        -r|--release) configuration=Release ;;
        -v=*) version="${arg#-v=}"; version="${version#v}" ;;
        -j=*|--jobs=*) jobs="${arg#*=}" ;;
        *) echo "Unknown argument: $arg" >&2; exit 1 ;;
    esac
done
[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo 'Version must be X.Y.Z' >&2; exit 1; }
[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || { echo 'Jobs must be a positive integer' >&2; exit 1; }
[[ "$(uname -s)/$(uname -m)" == Linux/x86_64 ]] || { echo 'Build on Linux x64 (glibc), not Windows or Alpine.' >&2; exit 1; }
for tool in cmake swig dotnet python3 patchelf ldd readelf; do
    command -v "$tool" >/dev/null || { echo "Missing build tool: $tool" >&2; exit 1; }
done
# Native getVersion() also reads VERSION; refuse an inconsistent override.
source_version="$(tr -d '[:space:]' < "$ROOT_DIR/VERSION")"
[[ "$version" == "$source_version" ]] || { echo "Set VERSION to $version first so native and NuGet versions agree." >&2; exit 1; }
BUILD_DIR="$ROOT_DIR/build/csharp"
debug_symbols=OFF
[[ "$configuration" != Debug ]] || debug_symbols=ON
cmake -S "$ROOT_DIR" -B "$BUILD_DIR" -DENABLE_JAVA=OFF -DENABLE_CSHARP=ON \
    -DFH_SDK_DEBUG_SYMBOLS="$debug_symbols" -DCMAKE_BUILD_TYPE="$configuration"
cmake --build "$BUILD_DIR" --target flight_hub_sdk_native --parallel "$jobs"
# Same runtime libraries as Java; copy from source prebuilts.
for spec in \
    openssl_1_1_1/lib/linux/x86_64/libcrypto.so.1.1 \
    openssl_1_1_1/lib/linux/x86_64/libssl.so.1.1 \
    websockets/lib/linux/x86_64/libwebsockets.so.12 \
    paho_mqtt_c/lib/linux/x86_64/libpaho-mqtt3as.so.1 \
    libdrtc/lib/linux/x86_64/libdrpc.so \
    libdrtc/lib/linux/x86_64/libdrtc_device_sdk.so; do
    cp -L "$ROOT_DIR/third_party/$spec" "$BUILD_DIR/native/"
done
for lib in "$BUILD_DIR"/native/*.so*; do
    patchelf --set-rpath '$ORIGIN' "$lib"
    readelf -h "$lib" | grep -q 'Advanced Micro Devices X86-64'
    dependencies="$(LD_LIBRARY_PATH="$BUILD_DIR/native${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" ldd "$lib")"
    if [[ "$dependencies" == *'not found'* ]]; then
        echo "Unresolved dependencies in $lib: $dependencies" >&2
        exit 1
    fi
done
# Isolated reference generation never overwrites Java's generated sources.
mkdir -p "$BUILD_DIR/java-reference" "$BUILD_DIR/dist"
swig -c++ -java -package com.dji.flighthubsdk.generated \
    -I"$ROOT_DIR/src/api" -I"$ROOT_DIR/deps/message_engine/src/api" \
    -outdir "$BUILD_DIR/java-reference" -o "$BUILD_DIR/java-reference/wrap.cxx" \
    "$ROOT_DIR/platform/java/swig/flight_hub_sdk.i"
python3 "$ROOT_DIR/platform/csharp/verify_api_parity.py" \
    "$BUILD_DIR/java-reference" "$BUILD_DIR/generated" "$BUILD_DIR/dist/api-parity.json"
dotnet pack "$ROOT_DIR/platform/csharp/flight_hub_sdk/flight_hub_sdk.csproj" \
    -c "$configuration" -p:Version="$version" -o "$BUILD_DIR/dist"
cp "$ROOT_DIR/platform/csharp/flight_hub_sdk/bin/$configuration/net8.0/flight_hub_sdk.dll" "$BUILD_DIR/dist/"
# Consume the real package and call native code without contacting a DJI server.
smoke_packages="$(mktemp -d "$BUILD_DIR/smoke-packages.XXXXXX")"
python3 - "$BUILD_DIR" <<'PY'
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
root = Path(sys.argv[1])
config = ET.Element('configuration')
sources = ET.SubElement(config, 'packageSources')
ET.SubElement(sources, 'clear')
ET.SubElement(sources, 'add', key='sdk-local', value=str(root / 'dist'))
ET.SubElement(sources, 'add', key='dotnet-official', value='https://api.nuget.org/v3/index.json')
mapping = ET.SubElement(config, 'packageSourceMapping')
local = ET.SubElement(mapping, 'packageSource', key='sdk-local')
ET.SubElement(local, 'package', pattern='flight_hub_sdk')
official = ET.SubElement(mapping, 'packageSource', key='dotnet-official')
for name in ('Microsoft.NETCore.App.Ref', 'Microsoft.AspNetCore.App.Ref',
             'Microsoft.NETCore.App.Host.linux-x64'):
    ET.SubElement(official, 'package', pattern=name)
ET.ElementTree(config).write(root / 'smoke.nuget.config', encoding='utf-8', xml_declaration=True)
PY
dotnet restore "$ROOT_DIR/platform/csharp/smoke/Smoke.csproj" \
    --configfile "$BUILD_DIR/smoke.nuget.config" --packages "$smoke_packages" -p:SdkVersion="$version" --force
dotnet run --project "$ROOT_DIR/platform/csharp/smoke/Smoke.csproj" \
    -c "$configuration" --no-restore -p:SdkVersion="$version"
echo "Built and smoke-tested: $BUILD_DIR/dist/flight_hub_sdk.$version.nupkg"
