# 4 阶段 RCA 方法论

RCA（Root Cause Analysis）是排障的核心方法论。本文档详述每个阶段的具体操作。

## 原则

1. **不许跳步**：即使你"觉得"知道原因，也必须走完前两步确认
2. **证据驱动**：每个结论必须有日志/截图/代码位置支撑
3. **最小假设**：优先排除最简单的原因（配置错误 > 代码 bug > SDK bug）
4. **先取证再假设**：定不下来时加埋点跑一次，不要靠推理补空白

## Stage 1: Reproduce（复现）

### 必须收集的信息

| 类别 | 具体项 | 为什么重要 |
|------|--------|-----------|
| 环境 | 开发/测试/生产 | Mock 模式不会有真实 SDK 错误 |
| 版本 | SDK JAR / paas.js / skeleton | 版本不匹配可能是根因 |
| 操作 | 用户的完整操作步骤 | 复现是定位的前提 |
| 错误 | 完整错误信息 + 堆栈 | 模糊描述无法定位 |
| **变更** | **上次正常到现在改了什么** | **有变更就先查变更，范围从全栈缩到一个 diff** |
| 频率 | 必现/偶现/只出现一次 | 决定排查策略（见下） |

### 最近变更清单

"昨天还好好的"是最高频的报障开场白，而它同时也是最强的线索。逐项确认：

| 变更源 | 怎么查 |
|---|---|
| 自己的代码 | `git log --since="<上次正常的时间>" --oneline`、`git diff <上次正常的 commit>..HEAD` |
| 依赖 / SDK 版本 | SDK JAR 版本号、`paas.js` 引用地址、`package.json` / `pom.xml` 的 diff |
| 配置与环境变量 | host / 协议（HTTP↔HTTPS）、token、workspace uuid、`.env` 差异 |
| 部署与运行时 | 换了机器/容器/JDK/浏览器？Mock 模式被打开或关掉？ |
| 平台侧 | 平台或设备固件是否刚升级？同一份代码在别的环境还正常吗？ |

**只在一个环境复现 = 环境差异就是根因**，别去读业务代码。

### 频率 → 排查策略

| 频率 | 含义 | 该走的路 |
|---|---|---|
| **必现** | 静态问题：配置、参数、调用顺序 | 走分层排查 / 专项清单，能稳定验证 |
| **偶现** | **首查时序 / 竞态**；排除后再查资源耗尽、缓存失效、重试风暴、负载相关退化 | 见 Stage 3「时序类问题」；按必现的路子查会一无所获 |
| **只出现一次** | 环境抖动 / 网络闪断 / 设备临时状态 | **先别改代码**：补日志与埋点，等复现。为一次性现象改代码大概率是引入新 bug |

### 如果用户信息不足

必须提问，不可猜测。标准提问模板：

> 请提供以下信息帮助排查：
> 1. 运行环境（本地开发/测试服/生产？Mock 模式？）
> 2. 完整错误信息（错误码/控制台输出/堆栈）
> 3. 触发步骤（做了什么操作后出现？）
> 4. 以前是正常的吗？中间改过什么（代码/版本/配置/环境）？
> 5. 必现还是偶现？

日志采集可复用 `scripts/diag.sh collect`（末 200 行，脱敏安全）提升一致性。

## Stage 2: Locate（定位）

### 定位优先级

```
1. 错误码匹配（最快）
   → error-codes.yaml 中有直接对应 → 直接给方案
   
2. 经验库匹配
   → auto memory（~/.claude/projects/*/memory/）中有相同场景 → 复用历史方案

3. 专项清单（命中即优先于分层排查）
   → 平台 HTTP 响应体 → platform-error-response.md
   → listenXxx 收不到回调 → event-callback-checklist.md

4. 分层排查（需要更多信息）

5. 边界埋点取证（前 4 步定不下来时——停止猜测，去取证）

6. 反向溯源（已知坏值，但不知道它从哪来）
```

### 分层排查决策树

#### 前端问题

```
浏览器控制台有错误？
├── Mixed Content → 协议不一致（HTTP/HTTPS 混用）
├── CORS → 跨域配置问题
├── 404 paas.js → frontHost 地址错误
├── FH2 is undefined → paas.js 未加载完就调了 initConfig
├── Cannot read properties of null → DOM 容器不存在或无尺寸
└── SDK 错误码 → 查 error-codes.yaml
```

#### 后端问题

```
Spring 日志有异常？
├── NullPointerException → null guard 缺失
├── TimeoutException → 回调超时（网络/设备无响应）
├── IllegalStateException → SDK 未初始化就调用
├── SecurityException → 控制权未获取
└── 无异常但数据为空 → 设备不在线或 SN 错误
```

### 边界埋点取证

分层排查是**按症状查表**；当表里查不到、或多层都"看着没问题"时，改用**取证**：在每个组件边界各加一行日志，**跑一次**，直接看链路在哪一层断掉，再只查那一层。

> 逐层猜要跑 N 次；一次全链路取证只跑 1 次，而且结论是证据不是推测。

DJI CloudAPI2 ISV 的链路有 5 个边界：

| # | 边界 | 出入口各记什么 | 断在这里说明 |
|---|---|---|---|
| B1 | 浏览器 ↔ paas.js / 前端组件 | `paas.js` 请求是否 200；`initConfig` 入参（host、token 长度或前 8 位、容器 id）；容器实际宽高 | 资源没加载或配置没传进去（`FH2 is undefined`、容器无尺寸） |
| B2 | 前端 ↔ 你的 BFF | 请求 URL + 方法 + 参数；**响应体全文**（不是只看 HTTP 状态码） | 你的接口层问题。注意平台 ServiceError **HTTP 也是 200**，必须看 body 里的 `code` |
| B3 | BFF ↔ DJI CloudAPI2 SDK | 调用前：方法名 + SN / workspace / 关键参数；<br>回调里：`error.error_code`、`data` 是否为 null、`listenXxx` 的返回 `listen_id` | 参数在你侧就已经是坏值，或 SDK 未初始化就被调用（静默返回 `null`） |
| B4 | SDK ↔ 平台（HTTP / WS） | 平台响应体原文（含 `message` 里的内层 `err_code`）；WS 是否连上；订阅用的频道 `{DeviceSN}-{Topic}` | 参数/权限/平台内部问题 → 转 `platform-error-response.md`；频道对不上 → 转 `event-callback-checklist.md` |
| B5 | 平台 ↔ 设备 | `fh_device_list` 在线状态与设备树；`fh_osd_snapshot` 遥测；`fh_hms` 告警 | 设备侧状态问题，不是代码问题 → 转 Stage 2.5 定性 |

**埋点纪律**：

- **在危险/关键操作之前打，不要等它失败之后**——失败后往往拿不到入参了
- **打原始值**，不要打自己加工过的结论（`"参数正常"` 是零信息）
- **凭证必须遮蔽**：只打 token 长度或前 8 位，绝不打全量。诊断日志会被贴进工单
- 取完证据**及时移除临时埋点**，别留在生产代码里

### 反向溯源：修在源头，不修在报错点

报错点拿到的往往只是**坏值的终点**。在报错处加个 null 判断能让异常消失，但下一次它会从别的路径再冒出来。

追溯步骤：

```
1. 报错点拿到的坏值是什么？        （SN 为空 / token 过期 / host 是 http / 设备状态不对）
2. 谁把这个值传进来的？
3. 再上一层是谁传的？
4. 一直追到「这个值第一次被赋值」的地方  ← 这才是根因
5. 修在第 4 步，不修在第 1 步
```

DJI CloudAPI2 场景里最常见的几类坏值源头：

| 坏值 | 常见源头 |
|---|---|
| 设备 SN 为空 / 不存在 | 设备列表接口还没返回就取值；用了子设备 SN 而该场景要网关 SN；前端硬编码 |
| token 过期 | auth 回调写成了静态值而非动态取值，首次拿到后就不再刷新 |
| host / 协议不对 | 配置里写了 `http://` 而页面是 HTTPS（Mixed Content）；测试环境地址带到了生产 |
| 设备状态不符预期 | 上一个任务没结束（航线执行中固件会锁相机）；控制权在别的会话手上 |

追不动的时候：在坏值**被使用之前**打印一次调用栈（Java `Thread.currentThread().getStackTrace()` / JS `new Error().stack`），直接看是谁调进来的。

### 定位工具

| 工具 | 用途 | 命令/位置 |
|------|------|-----------|
| 浏览器 DevTools | 前端网络/控制台 | F12 |
| Spring 日志 | 后端异常堆栈 | stdout / logback 文件 |
| Whistle | HTTP/WS 代理抓包 | `w2 start` |
| SDK Debug 日志 | SDK 内部状态 | 设置 LOG_LEVEL=DEBUG |

## Stage 3: Fix（修复）

### 修复原则

1. **最小改动**：只修复根因，不顺手重构
2. **一次一个**：一次只改一处、只动一个变量。同时改多处 → 好了也不知道是哪条生效，坏了也不知道是哪条引入的
3. **遵守约束**：修复代码必须满足对应 Skill 的 constraints.yaml
4. **不引入新问题**：修复一个 bug 不能打破其他功能
5. **改完立刻验证**：不要在没验证的修复上叠加下一个修复

### 常见修复模式

| 根因 | 修复模式 |
|------|----------|
| null 未检查 | 添加 if (xxx == null) 提前返回 |
| token 过期 | auth 回调改为动态取值 |
| 加载顺序错 | 调整 script/await 顺序 |
| 协议不一致 | 统一为 HTTPS + WSS |
| 控制权未获取 | 添加 acquire/release 包裹 |
| 监听未取消 | 在 @PreDestroy/onUnmounted 中 cancel |

### 时序类问题（偶现）

**确认是时序问题之后**（排除了资源耗尽、缓存失效、负载退化），修复方向不是"改逻辑"，而是**把"等一会儿"换成"等到条件成立"**。固定 sleep 在快的机器上够、慢的机器上不够，是偶现的头号来源。

常见成因：

- SDK `initialize` 没 `await` 完就调了别的方法（静默返回 `null`）
- `listenXxx` 注册早于初始化完成
- 用固定 sleep 等回调 / 等设备状态流转
- 控制权被别的会话抢占
- 设备状态还没到位（上一个航线任务未结束、相机被固件锁住）

```java
// ❌ 靠猜时间：机器一慢就偶现
Thread.sleep(3000);
double[] pos = getPosition();

// ✅ 等条件成立，且必须带超时；注册要判负、监听必取消、中断要恢复标志
AtomicReference<double[]> posRef = new AtomicReference<>();
CountDownLatch latch = new CountDownLatch(1);

int listenId = osdModule.listenAircraftLocation(location -> {
    posRef.set(new double[]{location.getLatitude(), location.getLongitude()});
    latch.countDown();
});
if (listenId < 0) {
    return null;                         // 注册就失败了，不要进 await 空等一个超时
}

try {
    boolean received = latch.await(POSITION_FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    return received ? posRef.get() : null;
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // 恢复中断标志，不要吞掉
    return null;
} finally {
    osdModule.cancelListenAircraftLocation(listenId);  // 超时/异常路径也必须取消
}
```

四个容易漏掉的点：**结果容器要先声明**（回调里赋值，用 `AtomicReference` 跨线程可见）、**`listen_id < 0` 要提前返回**（注册失败还去 `await` 就是白等一个超时）、**`cancelListenXxx` 放 `finally`**（否则监听泄漏，见 `event-callback-checklist.md`）、**`InterruptedException` 要恢复中断标志**。

> 注意 `listenXxx` 的回调是**单参**（只有数据），与请求类接口的 `(data, error)` 双参回调不是一套。别把 `countDown()` 写进 `error_code == 0` 的守卫里 —— 出错时不 `countDown`，你等到的会是一个误导性的"超时"，而不是真实错误。

设备状态类的等待同理：轮询 `fh_wayline_status` / `fh_osd_snapshot` 直到目标状态出现（带超时上限），不要 sleep 固定秒数。

### 三振出局：修 3 次还不好就停手

**判定规则与三个升级问题以 SKILL.md「三振出局」段为准**，此处只补充执行细节。

判定的是**修复尝试次数**，不是耗时：连续 3 次针对同一问题的修复都没解决 → 停止第 4 次。计数在"换了根因假设"时归零，在"同一假设下换写法"时不归零 —— 后者恰恰是三振的典型形态。

升级材料的最小集（`diag.sh report` 产物之外还需人工补齐）：完整平台响应体原文（含内层 `err_code`）、`request_id`、设备 SN、发生时间点（带时区）、可复现步骤。缺 `request_id` 时平台侧无法定位，会被打回。

### 修复后加固（可选）

根因修好之后，如果**同一类坏值还能从别的路径进来**，在边界补一道守卫——这不算"顺手重构"，它属于根因修复的一部分。守卫写法遵守 `cloudapi2-sdk-core` 的约束（null guard、`error_code` 判断、控制权成对释放），不要自造一套。

判断标准：能说清"哪条路径还能传进坏值"才加；说不清就别加。

## Stage 4: Verify（验证）

### 验证清单

- [ ] 原始问题不再复现
- [ ] 代码编译/构建通过
- [ ] 相关测试通过（如有）
- [ ] 没有引入新的 lint/type 错误
- [ ] constraints.yaml critical 规则仍满足

### 验证命令

```bash
# 后端
cd skeleton && mvn compile
cd skeleton && mvn test -pl feature/feature-xxx

# 前端
cd skeleton/web && npm run build
cd skeleton/web && npm run lint
```

## Memory 回写

修复验证通过后，将经验写入 auto memory（`~/.claude/projects/*/memory/`，type: `feedback`）。

不需要记录的情况：
- 纯配置错误（IP 写错、token 粘贴不完整）
- 一次性的环境问题
- 已在 error-codes.yaml 中有完整记录的错误码
