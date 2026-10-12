# json11

| | |
|--|--|
| 上游 | https://github.com/dropbox/json11 |
| 版本 | 仓内快照（Dropbox, Inc. · MIT） |
| 许可证 | MIT，见 [LICENSE](./LICENSE) 与源码头 |
| 类型 | 源码一起编 |
| SDK 目标 | `cloud_api2::json11` |
| 如何替换 | 换 `include/json11.hpp` + `src/json11.cpp`，保持 target 名 |

`#include <json11.hpp>`。由 `cloud_api2::base` `PUBLIC` 链接，ME 等下游不必再写一遍。
