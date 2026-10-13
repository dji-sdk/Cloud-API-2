# AIGCJson（DJI fork）

| | |
|--|--|
| 上游 | https://github.com/AIGMix/AIGCJson （亦见 yaronzz/AIGCJson） |
| 基线 | 头文件标注 1.0.3，Yaronzz · MIT |
| 许可证 | MIT，见 [LICENSE](./LICENSE) 与 `include/AIGCJson.hpp` 头 |
| 类型 | 头文件库（跟 SDK 一起用，不另编 `.a`） |
| SDK 目标 | `cloud_api2::aigcjson`（**不** `add_subdirectory` 本目录 CMake，避免默认开 test） |
| 本仓形态 | **fork**，不是干净上游快照 |

`#include <AIGCJson.hpp>`。SDK 写死 `-DAIGCJSON_FEATURE_OPTIONAL`（C++17）。

## DJI 相对上游的改动

- `std::optional<T>`：JSON `null` ↔ `nullopt`（`AIGCJSON_FEATURE_OPTIONAL`）
- `AIGC_JSON_HELPER_OPTIONAL_SKIP`：`nullopt` 的字段不写进 JSON；反序列化走同一套 OptionalSkip
- `AIGC_JSON_HELPER_ALL_OPTIONAL`：整结构体字段都可选（可单独用，不必再写 `AIGC_JSON_HELPER`）
- 定宽整数（`int8_t` / `uint8_t` 等）
- 本目录 `CMakeLists.txt`（feature 开关 + test）；上游没有

## 内嵌 RapidJSON

`include/rapidjson/` 是 [Tencent/rapidjson](https://github.com/Tencent/rapidjson) 头文件（MIT）。  
许可证见 [include/rapidjson/license.txt](./include/rapidjson/license.txt)。未带 `bin/jsonchecker/`（JSON License 那段）。

Notification Center SDK 的 `json_utils.h` 也直接 `#include` 这里的 RapidJSON。

## 如何替换

换 `include/AIGCJson.hpp`（及如需更新的 `include/rapidjson/`），保持 `cloud_api2::aigcjson` 目标名和 `AIGCJSON_FEATURE_OPTIONAL`。
