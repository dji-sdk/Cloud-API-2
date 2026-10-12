# JNI 原生库目录

本目录用于存放 JNI 原生库文件（官方支持平台: Linux x86_64）。

## 目录结构

```
lib/
└── linux-x86_64/          # Linux x86_64
    └── libflighthubsdk_jni.so
```

## 说明

- 这些库文件由构建脚本 `bin/build.sh` 自动生成和复制
- Java SDK会根据当前运行平台自动加载对应的原生库
- 发布 SDK 时确保 `linux-x86_64/` 下的库文件完整

## 构建说明

运行以下命令构建原生库：

```bash
# 构建 Debug 与 Release 两个版本
./bin/build.sh --all

# 只构建 Release 版本
./bin/build.sh --release
```