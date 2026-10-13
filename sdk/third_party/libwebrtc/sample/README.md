# libwebrtc sample（WHEP 原始码流）

只依赖本目录上一级的 `include/` + `lib/<本机>/libwebrtc.a`，外加 OpenSSL 和**系统** libcurl。

仓内 curl 头是扁平的 `curl.h`，这个 demo 写的是 `<curl/curl.h>`，所以 HTTP 走系统 curl，不链 `third_party/libcurl`。

## 系统依赖

**Linux x86_64**（示例 Ubuntu/Debian）：

```bash
sudo apt-get install -y \
  libcurl4-openssl-dev \
  libasound2-dev \
  libjpeg-dev \
  libx11-dev
```

**macOS**：`brew install curl openssl@3`（以及 Xcode CLT）。

## 编译

```bash
cd third_party/libwebrtc/sample
mkdir -p build && cd build
cmake ..
cmake --build . -j
```

mac：Homebrew `openssl@3`。Linux：仓内 `third_party/openssl_1_1_1`。两边都要能 `find_package(CURL)`。

链接时若系统 curl 走 OpenSSL 3、仓内 WebRTC 走 OpenSSL 1.1.1，可能出现 `libssl.so.3` / `libssl.so.1.1` 的 ld warning；sample 编过即可，生产链路仍以仓内 OpenSSL 1.1.1 为准。

## 运行

```bash
./whep_pull_origin_stream '<whep_url>'
```

WHEP 地址由调用方提供（本机 SRS / 自建 WHEP 均可）。未传参数会打印用法后退出。外网或 URL 不通只说明运行环境，不算 sample 编译失败。
