# plog（头文件库）

| | |
|--|--|
| 上游 | https://github.com/SergiusTheBest/plog |
| 版本 | **1.1.11** |
| 许可证 | MIT，见 [LICENSE](./LICENSE) |
| 类型 | 头文件库（跟 SDK 一起用，不另编 `.a`） |
| SDK 目标 | `cloud_api2::plog` |
| 平台 | header-only，与官方 native 相同 |

`#include <plog/Log.h>`。CMake 只加 `plog/include`，宏 `USE_PLOG_LOGGER`。

仓内是上游头文件快照，不是 submodule。上游 samples / CI / nuget 不进仓。示例见 `test/`。

## 仓内布局

```text
LICENSE
README.md
plog/include/plog/     # 头文件
plog/CMakeLists.txt    # 上游 INTERFACE 目标（test 会 add_subdirectory）
plog/LICENSE
plog/README.md         # 上游用法
test/                  # 本仓示例，可编可跑，用来验头文件、学用法
```

## 如何替换

换 `plog/include/`（及如需同步的 `plog/CMakeLists.txt`），保持 `cloud_api2::plog` 与 `USE_PLOG_LOGGER`。版本以 `plog/CMakeLists.txt` 里 `project(plog VERSION …)` 为准。
