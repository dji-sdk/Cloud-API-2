#!/usr/bin/env python3
"""Sync C++ src/api/*.h Doxygen comments to Java core/*.java Javadoc."""

from __future__ import annotations

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
API_DIR = REPO_ROOT / "src" / "api"
JAVA_CORE = REPO_ROOT / "platform" / "java" / "flight_hub_sdk" / "src" / "main" / "java" / "com" / "dji" / "flighthubsdk" / "core"

HEADER_TO_JAVA = {
    "workspace.h": "Workspace.java",
    "camera_control_module.h": "CameraControlModule.java",
    "fly_to_point.h": "FlyToPointModule.java",
    "poi_module.h": "PoiModule.java",
    "takeoff_module.h": "TakeoffModule.java",
    "flight_control_module.h": "FlightControlModule.java",
    "control_auth_module.h": "ControlAuthModule.java",
    "drc_module.h": "DrcModule.java",
    "flight_settings_module.h": "FlightSettingsModule.java",
    "test_flight_module.h": "TestFlightModule.java",
    "onboard_ai_module.h": "OnboardAiModule.java",
    "psdk_control_module.h": "PsdkControlModule.java",
    "drone_osd_module.h": "DroneOsdModule.java",
    "dock_osd_module.h": "DockOsdModule.java",
    "flight_task_control.h": "FlightTaskControlModule.java",
    "device_state_module.h": "DeviceStateModule.java",
    "device_hms_module.h": "DeviceHMSModule.java",
    "device_manage_module.h": "DeviceManageModule.java",
    "live_device_module.h": "LiveDeviceModule.java",
    "deploy_module.h": "DeployModule.java",
    "dock_control_module.h": "DockControlModule.java",
    "device_debug_module.h": "DeviceDebugModule.java",
    "network_test_module.h": "NetworkTestModule.java",
    "rtk_calibration_module.h": "RtkCalibrationModule.java",
    "relay_control_module.h": "RelayControlModule.java",
    "flight_task_module.h": "FlightTaskModule.java",
    "terra_build.h": "TerraBuildModule.java",
    "safety_service_module.h": "SafetyServiceModule.java",
    "pull_stream.h": "PullStreamModule.java",
    "media_service_module.h": "MediaServiceModule.java",
    "maintenance_management_module.h": "MaintenanceManagementModule.java",
    "log_management_module.h": "LogManagementModule.java",
    "live_stream.h": "LiveModule.java",
    "live_record_module.h": "LiveRecordModule.java",
    "hms_module.h": "HMSModule.java",
    "flight_task_record_module.h": "FlightTaskRecordModule.java",
    "diagnose_management_module.h": "DiagnosisManagementModule.java",
    "custom_sei.h": "CustomSEIModule.java",
    "ai_box_module.h": "AIBoxModule.java",
    "firmware_manage_module.h": "FirmwareManageModule.java",
    "wayline_manage_module.h": "WaylineManageModule.java",
    "psdk_resource_module.h": "PsdkResourceModule.java",
    "drone.h": "Drone.java",
    "dock.h": "Dock.java",
    "base_station.h": "BaseStation.java",
    "relay.h": "Relay.java",
    "remote_controller.h": "RemoteController.java",
}

# Java 对象封装类在 C++ 类注释基础上追加的获取方式说明
OBJECT_USAGE_NOTE: dict[str, str] = {
    "Workspace.java": "<p>通过 {@code FlightHubSDK.getWorkspaceInstance()} 获取实例。</p>",
    "Drone.java": "<p>通过 {@code FlightHubSDK.getDroneInstance()} 获取实例。</p>",
    "Dock.java": "<p>通过 {@code FlightHubSDK.getDockInstance()} 获取实例。</p>",
    "Relay.java": "<p>通过 {@code FlightHubSdkManager.getRelayInstance()} 获取实例。</p>",
}

PARAM_RENAMES = {
    "payload_index": "payloadIndex",
    "listen_id": "listenId",
    "fly_to_id": "flyToId",
    "reset_mode": "resetMode",
    "psdk_index": "psdkIndex",
    "video_quality": "videoQuality",
}


def parse_cpp_header(path: Path) -> tuple[str | None, dict[str, str]]:
    """Return (class_brief, method_name -> javadoc block)."""
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()

    class_brief: str | None = None
    methods: dict[str, str] = {}
    pending_doc: list[str] = []

    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        if stripped.startswith("///"):
            pending_doc.append(stripped[3:].lstrip())
            i += 1
            continue

        class_match = re.search(r"class\s+I(\w+)", line)
        if class_match and pending_doc and not methods:
            brief_parts: list[str] = []
            for d in pending_doc:
                if not d:
                    continue
                if d.startswith("@brief "):
                    brief_parts.append(d[7:].strip())
                elif d.startswith("@brief"):
                    brief_parts.append(d[6:].strip())
                elif not d.startswith("@"):
                    brief_parts.append(d)
            if brief_parts:
                class_brief = brief_parts[0]
            pending_doc = []
            i += 1
            continue

        if "virtual" in line:
            decl = line
            while "= 0" not in decl and "= default" not in decl and i + 1 < len(lines):
                i += 1
                decl += " " + lines[i].strip()
            virt_match = re.search(
                r"virtual\s+(?:std::shared_ptr<[^>]+>|[\w:<>,\s*&]+?)\s+(\w+)\s*\(",
                decl,
            )
            if virt_match and "= 0" in decl:
                name = virt_match.group(1)
                if name not in ("IDrone", "IDock") and pending_doc:
                    methods[name] = cpp_doc_to_javadoc(pending_doc)
            pending_doc = []
            i += 1
            continue

        if stripped and not stripped.startswith("//") and not stripped.startswith("#"):
            if not stripped.startswith("namespace") and stripped not in ("{", "}", "public:", "private:"):
                if not stripped.startswith("class ") and not stripped.startswith("virtual"):
                    pending_doc = []
        i += 1

    return class_brief, methods


def cpp_doc_to_javadoc(doc_lines: list[str]) -> str:
    out: list[str] = ["    /**"]
    impl_note: str | None = None
    in_internal = False

    for raw in doc_lines:
        line = raw.strip()
        if not line:
            continue
        if line == "@internal":
            in_internal = True
            continue
        if line == "@endinternal":
            in_internal = False
            continue
        if in_internal:
            if line.startswith("链路:") or "链路:" in line:
                impl_note = line if line.startswith("链路:") else line[line.index("链路:") :]
            continue

        if line.startswith("@brief "):
            out.append(f"     * {line[7:].strip()}")
        elif line.startswith("@param "):
            rest = line[7:].strip()
            parts = rest.split(None, 1)
            if len(parts) == 2:
                pname, pdesc = parts
                pname = PARAM_RENAMES.get(pname, pname)
                out.append(f"     * @param {pname} {pdesc}")
            else:
                out.append(f"     * @param {rest}")
        elif line.startswith("@return "):
            out.append(f"     * @return {line[8:].strip()}")
        elif line.startswith("@brief"):
            out.append(f"     * {line[6:].strip()}")
        elif not line.startswith("@"):
            out.append(f"     * {line}")

    if impl_note:
        out.append(f"     * @implNote {impl_note.strip()}")

    out.append("     */")
    return "\n".join(out)


def clean_orphan_javadocs(content: str) -> str:
    lines = content.splitlines(keepends=True)
    out: list[str] = []
    i = 0
    while i < len(lines):
        if not re.match(r"    /\*\*", lines[i]):
            out.append(lines[i])
            i += 1
            continue

        block_start = i
        while i < len(lines) and not re.match(r"^\s*\*/", lines[i]):
            i += 1
        if i < len(lines):
            i += 1
        block_end = i

        j = block_end
        while j < len(lines) and lines[j].strip() == "":
            j += 1

        if j < len(lines) and re.match(r"    /\*\*", lines[j]):
            i = block_end
            continue

        if j < len(lines) and re.match(r"^    public ", lines[j]):
            out.extend(lines[block_start:block_end])
            continue

        i = block_end

    return "".join(out)


def build_method_lookup(methods: dict[str, str]) -> dict[str, str]:
    """Map Java camelCase names to C++ docs (handles PascalCase API methods)."""
    lookup: dict[str, str] = {}
    for name, doc in methods.items():
        lookup[name] = doc
        if name:
            lookup[name[0].lower() + name[1:]] = doc
    return lookup


JAVA_TO_CPP_ALIASES: dict[str, list[str]] = {}


def resolve_java_method_doc(java_name: str, lookup: dict[str, str]) -> str | None:
    candidates = [java_name]
    if java_name in JAVA_TO_CPP_ALIASES:
        candidates.extend(JAVA_TO_CPP_ALIASES[java_name])
    if java_name.startswith("unSubscribe"):
        candidates.append("unsubscribe" + java_name[len("unSubscribe") :])
    if java_name.startswith("cancelListen"):
        rest = java_name[len("cancelListen") :]
        candidates.append("unsubscribe" + rest)
    for name in candidates:
        if name in lookup:
            return lookup[name]
    return None


JAVA_METHOD_PATTERN = re.compile(
    r"((?:    /\*\*(?:[^*]|\*(?!/)|\n)*?\*/\s*\n)*)"
    r"(^    public (?:synchronized )?(?:[\w.]+(?:<[^>]+>)?)\s+(\w+)\([^;]*\)(?:\s+throws\s+[\w.]+)?\s*\{)",
    re.MULTILINE,
)


def build_class_javadoc(class_brief: str, java_file: str) -> str:
    lines = ["/**", f" * {class_brief}"]
    usage = OBJECT_USAGE_NOTE.get(java_file)
    if usage:
        lines.append(f" * {usage}")
    lines.append(" */")
    return "\n".join(lines) + "\n"


def apply_class_javadoc(content: str, class_name: str, class_brief: str, java_file: str) -> str:
    brief_block = build_class_javadoc(class_brief, java_file)
    existing = re.compile(
        r"/\*\*\n(?: \*.*\n)*? \*/\n(public class " + re.escape(class_name) + r")",
        re.MULTILINE,
    )
    if existing.search(content):
        return existing.sub(brief_block + r"\1", content, count=1)

    insert = re.compile(r"(public class " + re.escape(class_name) + r")")
    return insert.sub(brief_block + r"\1", content, count=1)


def apply_javadoc(java_path: Path, methods: dict[str, str], class_brief: str | None) -> tuple[int, int]:
    content = java_path.read_text(encoding="utf-8")
    if java_path.name == "Drone.java":
        content = clean_orphan_javadocs(content)

    class_name = java_path.stem
    lookup = build_method_lookup(methods)
    updated = 0
    missing_cpp = 0

    def repl_method(match: re.Match[str]) -> str:
        nonlocal updated, missing_cpp
        method_name = match.group(3)
        if method_name == class_name:
            return match.group(0)
        # Skip constructors: return type equals class name (e.g. public FooModule(IFooModule m))
        sig = match.group(2)
        if re.match(rf"^    public (?:synchronized )?{re.escape(class_name)}\s*\(", sig):
            return match.group(0)
        doc = resolve_java_method_doc(method_name, lookup)
        if doc is None:
            missing_cpp += 1
            return match.group(0)

        javadoc = doc + "\n"
        updated += 1
        return javadoc + match.group(2)

    new_content = JAVA_METHOD_PATTERN.sub(repl_method, content)

    if class_brief:
        new_content = apply_class_javadoc(new_content, class_name, class_brief, java_path.name)

    if new_content != content:
        java_path.write_text(new_content, encoding="utf-8")

    return updated, missing_cpp


def main() -> int:
    total_updated = 0
    reports: list[str] = []

    for header, java_file in sorted(HEADER_TO_JAVA.items()):
        hpath = API_DIR / header
        jpath = JAVA_CORE / java_file
        if not hpath.exists():
            reports.append(f"SKIP header missing: {header}")
            continue
        if not jpath.exists():
            reports.append(f"SKIP java missing: {java_file}")
            continue

        class_brief, methods = parse_cpp_header(hpath)
        updated, missing = apply_javadoc(jpath, methods, class_brief)
        total_updated += updated
        class_note = f", class: {class_brief[:40]}..." if class_brief and len(class_brief) > 40 else (
            f", class: {class_brief}" if class_brief else ", class: (none)"
        )
        reports.append(
            f"{java_file}: synced {updated} methods from {header} "
            f"({len(methods)} in C++, missing_java={missing}{class_note})"
        )

    print("\n".join(reports))
    print(f"\nTotal method javadocs applied/updated: {total_updated}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
