# FHSDK Demo — Backend

后端包含两部分，**分开部署**：

| 目录 | 说明 |
|------|------|
| [`docker/oryx/`](docker/oryx/) | Oryx 流媒体（WHIP 收流 / WHEP 播放，Docker Compose） |
| [`fhsdk-demo-server/`](fhsdk-demo-server/) | Spring Boot + FHSDK BFF |

**依赖环境**（Java 17、FFmpeg、Docker/Oryx、FHSDK 平台等）见项目根目录 [`README.md`](../README.md) §依赖环境。

方案文档：[`docs/1_后端方案.md`](../docs/1_后端方案.md)
