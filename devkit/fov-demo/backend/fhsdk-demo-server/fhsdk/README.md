# FHSDK Java 依赖

将 **DJI CloudAPI2 SDK** 发行包中的 `flight-hub-sdk.jar` 放到本目录：

```text
fhsdk/
├── README.md              ← 本文件
└── flight-hub-sdk.jar     ← 自行放置
```

- JAR 文件名须与 `pom.xml` 中 `systemPath` 一致：`flight-hub-sdk.jar`
- 版本需与目标 DJI CloudAPI2 平台匹配（见 SDK 发行说明）
- `token` / `binding_uuid` 由 SDK 内置 `ConfigReader` 提供，Demo **无需**额外配置文件
