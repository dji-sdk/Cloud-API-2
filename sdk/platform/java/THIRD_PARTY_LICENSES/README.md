Flight Hub SDK — Third-Party Notices
=====================================

This SDK includes the following third-party open source components, redistributed
as native binary shared libraries under jar://lib/linux-x86_64/.
Each component's full license text is in the correspondingly named file next to
this NOTICE.

Component               Version    License          License file
---------               -------    -------          ------------
OpenSSL                 1.1.1f     OpenSSL/SSLeay   OpenSSL-1.1.1-LICENSE
                        (Linux)    dual
OpenSSL                 3.x        Apache 2.0       OpenSSL-LICENSE
                        (macOS 本机构建,            (Homebrew libssl.3 /
                        非官方交付)                 libcrypto.3)
libwebsockets           v2.4.2     LGPL-2.1*        libwebsockets-LICENSE
                        (SONAME 12) (warmcat
                                    exceptions)
paho-mqtt-c             1.3.14     EPL 2.0 or       paho-mqtt-c-LICENSE
                        (async SSL) EDL 1.0 (dual)

Additional notes
----------------
- OpenSSL 1.1.1f (Linux `.so.1.1`) is under the historical OpenSSL/SSLeay
  dual license; full text is in OpenSSL-1.1.1-LICENSE and
  `third_party/openssl_1_1_1/LICENSE`. OpenSSL 3.x (`libssl.3.dylib` /
  `libcrypto.3.dylib`, copied from Homebrew when building on macOS — a local
  development build, not part of the official Linux x86_64 delivery) is
  Apache 2.0 only; see OpenSSL-LICENSE.
- libwebsockets **v2.4.2** is LGPL-2.1 with warmcat's additional
  exceptions (static-linking exception and documentation note). Full text
  is in libwebsockets-LICENSE. This SDK dynamically links
  `libwebsockets.so.12` / `libwebsockets.12.dylib`. Source:
  https://libwebsockets.org/ / https://github.com/warmcat/libwebsockets/tree/v2.4.2
  Later upstream releases relicensed to MIT; that is not what this SDK ships.
- paho-mqtt-c is dual-licensed under EPL 2.0 and EDL 1.0. See
  paho-mqtt-c-LICENSE for the upstream notice plus both full texts; you may
  pick either.
- Volc RTC (VolcEngineRTC / ByteRTC series) and Agora RTC — **removed from
  this build** (C-01, ES320 v3.6 second-round audit). FHSDK realtime paths
  converged to WebRTC and DRTC. Related native libraries (`libVolcEngineRTC`,
  `libbytertc_*`, `libbytenn`, `libagora_rtc_sdk`, `libagora-fdkaac`,
  `libaosl`, and the transitively bundled `libRTCFFmpeg`) are no longer
  packaged. The `LGPL-2.1-LICENSE` file is the FSF LGPL 2.1 text. Shipped
  libwebsockets v2.4.2 is covered by `libwebsockets-LICENSE` (LGPL 2.1 plus
  warmcat exceptions), not by a restored Volc/Agora binary.

DJI Proprietary Native Components
----------------------------------

The following native libraries are DJI proprietary components, redistributed
as part of this SDK per DJI's own terms (not subject to third-party licensing):

- `libdrtc_device_sdk` (Linux) — DJI DRTC device SDK
- `libdrpc` (Linux) — DJI DRTC RPC

Redistribution of these components requires no third-party consent; DJI
retains all rights.

Statically linked / code-embedded components
--------------------------------------------

The following components are statically linked or code-embedded inside the JNI
library and their licenses are provided here for full transparency, even though
they do not produce separate .so/.dylib files:

- AIGCJson — MIT (Yaronzz); DJI fork in `third_party/AIGCJson/`
- RapidJSON — MIT (Tencent); headers under `third_party/AIGCJson/include/rapidjson/`
- json11 — MIT (Dropbox); source in `third_party/json11/`
- h264_analyze — DJI 自研（`deps/h264_analyze/`），不是三方
- plog (header-only logger) — **MIT License** (Copyright (c) Sergey Podobry;
  see plog-LICENSE for full text). Note: some older audit references list
  plog as MPL 2.0, which is out-of-date — the upstream project has been
  MIT-licensed for several releases. MIT imposes no copyleft or source-
  disclosure obligation; retaining the copyright notice is sufficient.
- aws-cpp-sdk-s3 — Apache 2.0（1.11.642；全文见 `third_party/aws-cpp-sdk-s3/LICENSE` 与本目录 `aws-cpp-sdk-s3-LICENSE`）
- libcurl — curl license（MIT/X 衍生；8.15.0-DEV；全文见 `third_party/libcurl/COPYING` 与本目录 `libcurl-LICENSE`）
- libwebrtc — BSD 3-clause（branch-heads/6477；全文见 `third_party/libwebrtc/LICENSE` 与本目录 `libwebrtc-LICENSE`）

For any question regarding the third-party redistribution please contact the
Flight Hub SDK team.
