// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "CppWebSocket.h"
#include "base/common/logger.h"
#include "base/utils/base64_codec.h"
#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"

using namespace notification_center;


#ifdef ENABLE_LIBWEBSOCKETS

#include <atomic>
#include <libwebsockets.h>
#include <openssl/x509v3.h>
#include <thread>
// #include <queue>
#include <chrono>
#include <cstdlib>
#include <deque>
#include <mutex>
#include <algorithm>
#include <sstream>
#include <vector>
#include <fstream>
// TCP_NODELAY 优化所需头文件
#include <netinet/tcp.h>
#include <sys/socket.h>
// P0-01 修复: 决策 5 主机名/IP 自检所需
#include <arpa/inet.h>

// ============================================================================
// WebSocket 配置常量
// ============================================================================

// libwebsockets 协议层缓冲区配置
namespace {
// libwebsockets 接收缓冲区大小(rx_buffer_size)
// 回退: 从 1MB 恢复为 4KB。
// 1MB 大缓冲区使 SSL_read() 每次读取更多数据, 延长 SSL 内循环单次迭代耗时,
// 减少 LWS_CALLBACK_CLIENT_RECEIVE 触发频率, 加剧 POLLOUT 饥饿(lws issue 1700)。
constexpr size_t NCSDK_LWS_RX_BUFFER_SIZE = 4 * 1024;   // 4KB

// TCP Socket 缓冲区配置
constexpr int NCSDK_TCP_SEND_BUFFER_SIZE = 256 * 1024;  // 256KB 发送缓冲区
constexpr int NCSDK_TCP_RECV_BUFFER_SIZE = 256 * 1024;  // 256KB 接收缓冲区
constexpr int NCSDK_TCP_NODELAY_FLAG = 1;               // 启用 TCP_NODELAY(禁用 Nagle 算法)

// TCP Keep-Alive 配置
constexpr int NCSDK_TCP_KEEPALIVE_TIME = 30;            // 30秒后开始发送 TCP Keep-Alive
constexpr int NCSDK_TCP_KEEPALIVE_INTERVAL = 10;        // 每10秒重试一次
constexpr int NCSDK_TCP_KEEPALIVE_PROBES = 3;           // 最多重试3次

// 连接超时配置
constexpr int NCSDK_CONNECTION_TIMEOUT_SECS = 10;       // 10秒连接超时

// 消息大小限制
constexpr size_t NCSDK_MAX_MESSAGE_SIZE = 64 * 1024 * 1024;  // 64MB 最大消息大小(防止恶意服务器)

// 队列监控配置
constexpr size_t NCSDK_QUEUE_SIZE_LOG_INTERVAL = 1000;  // 每1000条消息打印一次队列大小

// WRITEABLE 批量发送配置
// 每次 WRITEABLE 回调最多连续发送的消息条数, 受 lws_send_pipe_choked()
// 动态约束。
//
// 设计依据(对 ~150B 的 ACK 类消息): 
//   - 真正的吞吐瓶颈是网络带宽,
//   而非批次大小；调大批次不改变最终清空队列的耗时。
//   - 批次大小主要影响 CPU 效率: 批次越大, 事件循环迭代次数越少,
//   系统调用开销越低。
//   - 过大(如 10000+)会让单次 WRITEABLE 回调占用 IO 线程过长, 延迟同轮次 RX
//   处理；
//     对 150B 消息实际影响极小(64 × 150B = 9.6KB 的 memcpy 是微秒级),
//     但仍保留上限以防消息变大。
//   - lws_send_pipe_choked() 是发送侧的真正流控手段, 当 lws
//   内部有未完成的待发缓冲时才返回 1。
//     注意: 在正常 lws_write() 完整写入内核路径下, choked 通常不触发,
//     批次上限才是实际止点。
// 64 条 × ~150B/条 ≈ 9.6KB/批；相比 32, 事件循环迭代次数减半, CPU 节省约
// 15ms(235K 场景)。
constexpr int NCSDK_WRITEABLE_BATCH_SIZE = 64;

// ACK 消息 TTL(生存时间)
// 队列堆积时, 超过此时间的 ACK 已无发送价值: 
//   - 服务端典型重发间隔为 5~10 秒, 30 秒内通常已重发 3~6 次
//   - 但偏旧的 ACK(5~30s)到达后仍能抑制服务端后续重发, 有一定价值
//   - 超过 30 秒的 ACK 几乎无意义, 继续发送只浪费带宽并延迟新消息
// 选择 30 秒作为丢弃阈值, 在"减少无效流量"和"保留有效 ACK"之间取平衡。
constexpr int64_t NCSDK_MSG_TTL_MS = 30000;  // 30 秒

// 超时和重试配置
constexpr int NCSDK_WRITEABLE_PENDING_TIMEOUT_MS = 50;   // macOS: writeablePending 超时时间(50ms)
constexpr int NCSDK_QUEUE_HEALTH_CHECK_INTERVAL_MS = 50; // 非 macOS: 队列健康检查间隔(50ms)
// 连续 N 次健康检查无 WRITEABLE 进展时判定 TCP 拥塞并断开重连
// 50ms × 150 = 7.5 秒
constexpr int NCSDK_CONGESTION_STUCK_THRESHOLD = 150;
// 修复: 健康检查每次淘汰的最大消息数, 限制单次持锁时间。
// 5000 条 × ~100ns/条 ≈ 0.5ms, 262,000 条需 53 个周期(约 10.6s)清完, 可接受。
constexpr size_t NCSDK_HEALTH_CHECK_EVICT_BATCH = 5000;

// SSL_pending() 读节流: 每收 N 条消息调用一次 lws_cancel_service()
// 打断 SSL 读内循环, 让 poll() 得以调度 POLLOUT(lws issue 1700 workaround)。
// N=8: 稳态(24 msgs/s)约 3 次/s, storm 阶段(1000+ msgs/s)约 125 次/s。
// 过小(N=1): cancel_service 开销增大；过大(N=32): WRITEABLE 恢复延迟增大。
constexpr int NCSDK_RECV_THROTTLE_N = 8;

// 默认端口
constexpr int NCSDK_DEFAULT_HTTP_PORT = 80;    // HTTP 默认端口
constexpr int NCSDK_DEFAULT_HTTPS_PORT = 443;  // HTTPS 默认端口

// libwebsockets 日志桥接: 统一转发到项目日志系统
void LwsLogEmitter(int level, const char *line) {
    if (!line) {
        return;
    }
    std::string message(line);
    while (!message.empty() && (message.back() == '\n' || message.back() == '\r')) {
        message.pop_back();
    }
    if (message.empty()) {
        return;
    }
    switch (level) {
    case LLL_ERR:
        LOG_ERROR("[lws] %s", message.c_str());
        break;
    case LLL_WARN:
        LOG_WARN("[lws] %s", message.c_str());
        break;
    case LLL_NOTICE:
    case LLL_INFO:
    case LLL_CLIENT:
    case LLL_LATENCY:
        LOG_INFO("[lws] %s", message.c_str());
        break;
    default:
        LOG_DEBUG("[lws] %s", message.c_str());
        break;
    }
}

// 环境变量 NCSDK_LWS_LOG_LEVEL 可覆盖默认日志级别(十进制或十六进制)
int ResolveLwsLogLevel() {
    constexpr int kDefaultLevel = (1 << LLL_COUNT) - 1;  // 默认开启所有 lws 日志级别
    const char *env_level = std::getenv("NCSDK_LWS_LOG_LEVEL");
    if (!env_level || env_level[0] == '\0') {
        return kDefaultLevel;
    }

    char *endptr = nullptr;
    long parsed = std::strtol(env_level, &endptr, 0);
    if (endptr == env_level || *endptr != '\0' || parsed < 0) {
        LOG_WARN("CppWebSocket: NCSDK_LWS_LOG_LEVEL 非法: %s, 使用默认值: %d", env_level, kDefaultLevel);
        return kDefaultLevel;
    }

    return static_cast<int>(parsed);
}

void InitLwsLogBridgeOnce() {
    static std::once_flag s_lws_log_once;
    std::call_once(s_lws_log_once, []() {
        int level = ResolveLwsLogLevel();
        lws_set_log_level(level, LwsLogEmitter);
        LOG_INFO("CppWebSocket: 已启用 libwebsockets 日志桥接, 级别掩码: %d", level);
    });
}

std::string BuildAuthPayload(const std::map<std::string, std::string> &headers) {
    std::vector<std::pair<std::string, std::string>> kvs;
    kvs.reserve(headers.size());
    for (const auto &item : headers) {
        if (item.first.empty() || item.second.empty()) {
            continue;
        }
        kvs.emplace_back(item.first, item.second);
    }
    if (kvs.empty()) {
        return "";
    }

    std::sort(kvs.begin(), kvs.end(),
              [](const std::pair<std::string, std::string> &lhs,
                 const std::pair<std::string, std::string> &rhs) {
                  return lhs.first < rhs.first;
              });

    std::ostringstream oss;
    for (size_t i = 0; i < kvs.size(); ++i) {
        if (i > 0) {
            oss << ";";
        }
        oss << kvs[i].first << "=" << kvs[i].second;
    }
    return oss.str();
}

std::string BuildSubProtocolHeader(const std::map<std::string, std::string> &headers) {
    const std::string auth_payload = BuildAuthPayload(headers);
    if (auth_payload.empty()) {
        return "v1.notify";
    }
    // 标准 Base64 → Base64URL：替换 URL 不安全字符（+→- /→_）并去掉 = padding
    std::string encoded = drc_sdk::base64_encode(auth_payload);
    for (char &ch : encoded) {
        if (ch == '+') ch = '-';
        else if (ch == '/') ch = '_';
    }
    while (!encoded.empty() && encoded.back() == '=') {
        encoded.pop_back();
    }
    return "v1.notify,auth." + encoded;
}

} // namespace

/**
  * WebSocket 连接状态枚举
  *
  * 状态流转图: 
  *
  *     DISCONNECTED (初始状态)
  *          |
  *          | connect()
  *          v
  *     CONNECTING
  *          |
  *          | 连接成功 (LWS_CALLBACK_CLIENT_ESTABLISHED)
  *          v
  *     CONNECTED
  *          |
  *          | close() 或连接错误/关闭 (LWS_CALLBACK_CLOSED/ERROR)
  *          v
  *     CLOSING
  *          |
  *          | 关闭完成 (close() 完成)
  *          v
  *     CLOSED
  *          |
  *          | connect() (重新连接, 会先清理残留资源)
  *          v
  *     CONNECTING
  *
  * 状态说明: 
  * - DISCONNECTED: 未连接状态, 可以开始连接
  * - CONNECTING: 正在连接中, 不允许重复连接(通过原子状态转换保证)
  * - CONNECTED: 已连接, 可以发送消息
  * - CLOSING: 正在关闭, 不允许新操作(等待线程退出, 清理资源)
  * - CLOSED: 已关闭, 可以重新连接(会先清理残留资源再连接)
  *
  * 线程安全保证: 
  * - 所有状态转换使用 compare_exchange_strong 原子操作
  * - 同一时间只能有一个状态值
  * - 状态转换失败时会记录日志并忽略操作
  */
enum class WebSocketState {
    DISCONNECTED, // 未连接(初始状态)
    CONNECTING,   // 连接中
    CONNECTED,    // 已连接
    CLOSING,      // 关闭中
    CLOSED        // 已关闭
};

// 带入队时间戳的消息, 用于 TTL 淘汰过期 ACK
struct QueuedMessage {
    std::string data;
    std::chrono::steady_clock::time_point enqueue_time;
};

// WSS TLS 验证配置。
// 经 lws_context_creation_info.user 挂载到 context, 证书验证回调中经
// lws_context_user(lws_get_context(wsi)) 取回 —— 证书验证回调触发于连接早期,
// 此时 lws_wsi_user(wsi) 尚不可用(协议未协商), 不能走 Impl 指针的常规路径。
// 生命周期: 由 Impl 持有, 必须覆盖 lws_context 生命周期(info.user 悬垂风险)。
struct WssTlsConfig {
    std::mutex mutex;          // protect: setTlsConfig(外部线程) vs 回调(IO 线程)并发访问
    bool verify_ssl = true;    // 证书链 + 主机名/IP 校验开关(安全默认开启; 私有化自签名须配 ssl_ca_path)
    std::string ssl_ca_path;   // 私有 CA 文件路径(PEM); 为空时使用系统 CA 信任库
    std::string connect_host;  // 本次连接目标地址(已去端口), 用于叶子证书主机名/IP 自检
};

class CppWebSocket::Impl : public drc_sdk::common::AsyncCaptureProtect {
public:
    Impl(CppWebSocket *parent)
        : parent_(parent), context(nullptr), wsi(nullptr),
        state_(WebSocketState::DISCONNECTED), ws_subprotocol_header_("v1.notify") {}

    ~Impl() {
        close();
        // 兜底: 服务端主动断开 / force_close 时, state 已被 serviceLoop 推至 CLOSED,
        // close() 检测到 CLOSING/CLOSED 直接返回, 不执行 join。
        // 此时 serviceLoop 已退出(线程函数已返回)但 service_thread 仍 joinable,
        // ~thread() 对 joinable 线程调用 std::terminate()。
        // 此处补充 join 确保线程资源在析构时总能被正确回收。
        if (service_thread.joinable()) {
            service_thread.join();
        }
    }

private:
    // 状态转换辅助函数
    bool tryTransition(WebSocketState expected, WebSocketState desired) {
        WebSocketState exp = expected;
        bool success = state_.compare_exchange_strong(exp, desired);
        if (success) {
            LOG_DEBUG("CppWebSocket: 状态转换 %d -> %d", static_cast<int>(expected), static_cast<int>(desired));
        } else {
            LOG_DEBUG("CppWebSocket: 状态转换失败, 期望 %d, 实际 %d", static_cast<int>(expected), static_cast<int>(exp));
        }
        return success;
    }

    WebSocketState getState() const {
        return state_.load();
    }

    bool isState(WebSocketState s) const {
        return state_.load() == s;
    }

    // 状态检查辅助函数
    bool canConnect() const {
        WebSocketState s = state_.load();
        return s == WebSocketState::DISCONNECTED || s == WebSocketState::CLOSED;
    }

    bool canSend() const { return state_.load() == WebSocketState::CONNECTED; }

    bool isClosing() const {
        WebSocketState s = state_.load();
        return s == WebSocketState::CLOSING || s == WebSocketState::CLOSED;
    }

    bool isConnected() const {
        return state_.load() == WebSocketState::CONNECTED;
    }

public:
    void setAuthHeaders(const std::map<std::string, std::string> &headers) {
        std::lock_guard<std::mutex> lock(auth_headers_mutex_);
        auth_headers_ = headers;
        ws_subprotocol_header_ = BuildSubProtocolHeader(auth_headers_);
        LOG_INFO("CppWebSocket: setAuthHeaders updated, keys=%zu, subprotocol=%s",
                 auth_headers_.size(),
                 ws_subprotocol_header_.c_str());
    }

    // 设置 TLS 证书验证配置(仅在下一次 connect() 生效, 与 setAuthHeaders 同语义)
    void setTlsConfig(bool verify_ssl, const std::string &ca_path) {
        std::lock_guard<std::mutex> lock(tls_config_.mutex);
        tls_config_.verify_ssl = verify_ssl;
        tls_config_.ssl_ca_path = ca_path;
        LOG_INFO("CppWebSocket: setTlsConfig, verify_ssl=%s, ca_path=%s",
                 verify_ssl ? "true" : "false(不安全, 仅限受控测试环境)",
                 ca_path.empty() ? "(系统CA)" : ca_path.c_str());
    }

    // 连接 WebSocket
    void connect(const std::string &url) {
        // 检查是否可以连接(只能从 DISCONNECTED 或 CLOSED 状态转换到 CONNECTING)
        WebSocketState current = getState();
        if (current != WebSocketState::DISCONNECTED && current != WebSocketState::CLOSED) {
            LOG_WARN("CppWebSocket: 当前状态 %d, 无法连接, 忽略重复请求", static_cast<int>(current));
            return;
        }

        // 尝试转换到 CONNECTING 状态
        if (!tryTransition(current, WebSocketState::CONNECTING)) {
            LOG_WARN("CppWebSocket: 状态转换失败, 连接已在进行中, 忽略重复请求");
            return;
        }

        this->url = url;

        // 如果之前有连接, 先清理残留资源(从 CLOSED 状态重新连接时)
        // 注意: 此时状态已经是 CONNECTING, 需要安全清理残留资源
        if (current == WebSocketState::CLOSED) {
            // 关键: join() 必须在访问 context/wsi 之前执行。
            //
            // force_close 路径的清理顺序(问题1修复后): 
            //   ① sendMutex 下 wsi=nullptr
            //   ② context.store(nullptr)
            //   ③ lws_context_destroy(ctx)
            // join() 提供 happens-before 保证: IO 线程上述三步均先于 join() 返回可见,
            // 故 join() 后 context 必为 nullptr, wsi 必为 nullptr。
            // 防御性检查: 正常路径下 context 应已为 nullptr, 此处兜底处理异常情况。
            if (service_thread.joinable()) {
                service_thread.join();
            }
            // join() 后 IO 线程已退出: context 已由 IO 线程置 nullptr, wsi 同理。
            // 无需调用 lws_cancel_service (IO 线程已退出, 无需唤醒)。
            // 防御性检查: 正常路径下 context 应已为 nullptr, 此处兜底处理异常情况。
            {
                auto ctx = context.load();
                if (ctx) {
                    lws_context_destroy(ctx);
                    context.store(nullptr);
                }
            }
            wsi = nullptr;
        }

        // 重置每连接状态, 确保新连接从干净初态开始。
        // 时机: service_thread.join() 已完成(旧 IO 线程已退出), 新线程尚未启动,
        // 不存在并发访问, 无需加锁。
        //
        // 核心问题(不重置时的后果): 
        //   force_close 触发时
        //   consecutiveStuckChecks_=150、writeableCallbackCount_==lastWriteableCount_。
        //   重连后 lastQueueHealthCheck_
        //   仍是旧连接最后一次健康检查的时刻(force_close 前约 50ms),
        //   新连接建立后第一次健康检查的 check_elapsed 已超过 50ms, 立即触发。
        //   若新连接同样遭遇 TCP 拥塞(WRITEABLE 未触发), consecutiveStuckChecks_ 由
        //   150 变 151,
        //   >= 150 → 立即再次 force_close, 形成几乎无间隔的重连风暴。
        pendingWriteRequest_.store(false, std::memory_order_relaxed);
        close_notified_.store(false, std::memory_order_relaxed); // 新连接重置通知标志
        recvCount_ = 0; // 重置读节流计数器: 新连接重新计数(旧计数可能残留 storm 峰值)
#ifdef __APPLE__
        writeablePending.store(false, std::memory_order_relaxed);
        // lastWriteableRequest 无需重置: writeablePending=false 后不会被读取
#else
        lastQueueHealthCheck_ = std::chrono::steady_clock::now(); // 给新连接完整的预热期
        writeableCallbackCount_ = 0;          // 新连接重新计数
        lastWriteableCount_ = 0; // 与 writeableCallbackCount_ 对齐, 首次健康检查基准归零
        consecutiveStuckChecks_ = 0; // 新连接获得完整的拥塞判定窗口
#endif

        // 清空接收缓冲区和发送队列(新连接时清理旧数据)
        {
            std::lock_guard<std::mutex> lock(receiveMutex);
            receiveBuffer.clear();
        }
        {
            std::lock_guard<std::mutex> lock(sendMutex);
            sendQueue.clear();
        }

        LOG_INFO("CppWebSocket: 开始连接 %s", url.c_str());

        // 启动事件循环线程
        WeakDummy(weak_ptr);
        service_thread = std::thread([this, weak_ptr]() {
            WeakDummyReturn(weak_ptr);
            this->serviceLoop();
        });
    }

    // 发送消息
    void send(const std::string &message) {
        if (!canSend()) {
            LOG_WARN("CppWebSocket: 当前状态 %d, 连接未建立, 无法发送消息", static_cast<int>(getState()));
            return;
        }

        // 不限制队列大小, 允许无限缓存
        // 通过超时重试机制和队列健康检查确保消息能够及时发送
        bool shouldTriggerWrite = false;
        size_t queue_size = 0;
        {
            std::lock_guard<std::mutex> lock(sendMutex);
            shouldTriggerWrite = sendQueue.empty(); // 只有队列为空时才触发
            sendQueue.push_back({message, std::chrono::steady_clock::now()});
            queue_size = sendQueue.size();

            // 监控队列大小(用于压测和问题诊断)
            if (queue_size % NCSDK_QUEUE_SIZE_LOG_INTERVAL == 0) {
                LOG_DEBUG("CppWebSocket: 发送队列大小: %zu", queue_size);
            }
        }

        // 只有当队列之前为空时才通知 IO 线程触发 WRITEABLE, 避免重复触发。
        //
        // 方案A: 主线程只设 pendingWriteRequest_ 原子标志, 不调用任何 lws API。
        // 根本原因: lws_cancel_service(ctx) 与 lws_context_destroy(ctx) 存在 UAF 竞争——
        //   主线程: isConnected()→true, context.load()→非null
        //   IO线程: CAS→CLOSED, context.store(null), lws_context_destroy(ctx)  ← ctx 已释放
        //   主线程: lws_cancel_service(ctx)  ← UAF
        // lws_wsi_user(current_wsi) 同理: wsi 可能在 lws_context_destroy 后已被释放。
        //
        // IO 线程在 lws_service() 超时返回后(≤1ms)检测 pendingWriteRequest_ 标志,
        // 在 IO 线程上下文安全调用 lws_callback_on_writable, 彻底消除跨线程 lws
        // API 调用的 UAF 风险。最大额外延迟 ≤1ms, 对 ACK 场景完全可接受。
        if (shouldTriggerWrite && isConnected()) {
            LOG_DEBUG("CppWebSocket: 触发写事件, 队列大小: %zu", queue_size);
#ifdef __APPLE__
            {
                // lastWriteableRequest 是非原子 time_point, serviceLoop(IO 线程)在
                // sendMutex 下读取它, 主线程写入也必须在同一把锁下进行。
                // 先写时间戳再设 writeablePending, 确保 IO 线程看到 true 时时间戳已是最新值。
                std::lock_guard<std::mutex> lk(sendMutex);
                lastWriteableRequest = std::chrono::steady_clock::now();
            }
            writeablePending = true;
#endif
            pendingWriteRequest_.store(true, std::memory_order_release);
        } else {
            LOG_DEBUG("CppWebSocket: 消息已加入队列, 队列大小: %zu", queue_size);
        }
    }

    // 关闭连接
    void close() {
        LOG_INFO("CppWebSocket: 关闭连接");

        // 用 CAS 重试循环确保成功转换到 CLOSING 状态。
        // 问题: 单次 CAS 在 CONNECTING→CONNECTED 竞争时会静默失败: 
        //   主线程读到 CONNECTING → IO 线程推至 CONNECTED → CAS(CONNECTING→CLOSING) 失败 → return
        //   连接实际未关闭, 但 close() 已返回, service_thread 持续运行, 资源泄漏至析构。
        // 循环: 重读状态后对新的源状态(如 CONNECTED)重试 CAS, 必然成功。
        // 终止保证: 状态机是有限单向图, IO 线程最多做 CONNECTING→CONNECTED 和
        //   CONNECTED→CLOSED 两次转换；最坏情况循环 2 次即可成功或观察到 CLOSING/CLOSED。
        while (true) {
            WebSocketState current = getState();
            if (current == WebSocketState::CLOSING || current == WebSocketState::CLOSED) {
                LOG_DEBUG("CppWebSocket: 已经在关闭状态, 无需重复关闭");
                return;
            }
            if (tryTransition(current, WebSocketState::CLOSING)) {
                break; // CAS 成功, 继续清理
            }
            // CAS 失败: state 刚被其他线程修改(如 CONNECTING→CONNECTED), 重读后重试
        }

        // 在锁内清空 wsi, 防止 send() 使用
        {
            std::lock_guard<std::mutex> lock(sendMutex);
            wsi = nullptr; // 先清空 wsi
        }

        // 清空接收缓冲区
        {
            std::lock_guard<std::mutex> lock(receiveMutex);
            receiveBuffer.clear();
        }

        // 取消服务, 这会停止 lws_service 循环
        // 快照 context: 此时 IO 线程可能已退出并将其置 nullptr(context 已是
        // atomic)
        {
            auto ctx = context.load();
            if (ctx) {
                lws_cancel_service(ctx);
            }
        }

        // 等待服务线程退出
        // 注意: 必须等待线程退出后再销毁 context, 否则可能还有回调在使用 context
        if (service_thread.joinable()) {
            service_thread.join();
        }

        // 清理资源(此时所有回调都应该已经完成)
        // join() 提供 happens-before: IO 线程对 context 的写入此处完全可见
        {
            auto ctx = context.load();
            if (ctx) {
                lws_context_destroy(ctx);
                context.store(nullptr);
            }
        }

        // 确保 wsi 被清空(双重保险)
        wsi = nullptr;

        // 转换到 CLOSED 状态
        tryTransition(WebSocketState::CLOSING, WebSocketState::CLOSED);
    }

    // 是否已连接
    bool isOpen() const {
        return isConnected();
    }

private:
    // 事件循环线程, IO线程
    void serviceLoop() {
        LOG_INFO("CppWebSocket: 服务线程启动");
        // 注册日志函数;
        InitLwsLogBridgeOnce();

        // 协议定义（回调协议名固定，握手发送的子协议字符串通过 ccinfo.protocol 单独设置）
        static struct lws_protocols protocols[] = {
            {"v1.notify", &CppWebSocket::Impl::lwsCallback, sizeof(void *), NCSDK_LWS_RX_BUFFER_SIZE, 0, nullptr, 0},
            {nullptr, nullptr, 0, 0, 0, nullptr, 0}
        };

        std::string protocol_header_value;
        {
            std::lock_guard<std::mutex> lock(auth_headers_mutex_);
            protocol_header_value = ws_subprotocol_header_;
        }
        if (protocol_header_value.empty()) {
            protocol_header_value = "v1.notify";
        }
        LOG_INFO("CppWebSocket: 握手子协议配置: %s",
                 protocol_header_value.c_str());

        struct lws_context_creation_info info;
        memset(&info, 0, sizeof(info));
        info.port = CONTEXT_PORT_NO_LISTEN;
        info.protocols = protocols;
        info.gid = -1;
        info.uid = -1;

        // 配置 TCP Keep-Alive 心跳机制
        info.ka_time = NCSDK_TCP_KEEPALIVE_TIME;
        info.ka_interval = NCSDK_TCP_KEEPALIVE_INTERVAL;
        info.ka_probes = NCSDK_TCP_KEEPALIVE_PROBES;

        // 配置连接超时
        info.timeout_secs = NCSDK_CONNECTION_TIMEOUT_SECS;

        // SSL 配置 - 启用 SSL(证书验证: 链验证由 OpenSSL/预校验完成,
        // 主机名/IP 校验在回调中自检, 见 LWS_CALLBACK_OPENSSL_PERFORM_SERVER_CERT_VERIFICATION)
        info.options = LWS_SERVER_OPTION_DO_SSL_GLOBAL_INIT;

        // TLS 配置挂载与私有 CA 接线。
        // info.user -> context->user_space -> 回调内 lws_context_user() 取回。
        // ca_path 非空时经 client_ssl_ca_filepath 提供给 lws(SSL_CTX_load_verify_locations);
        // 为空时依赖 lws 构建默认开启的 LWS_SSL_CLIENT_USE_OS_CA_CERTS(系统 CA)。
        {
            std::lock_guard<std::mutex> lock(tls_config_.mutex);
            if (!tls_config_.ssl_ca_path.empty()) {
                // 启动期检查: lws 对 load_verify_locations 失败仅打日志不中断,
                // 配置 typo 会静默回退为"找不到颁发者"的连接失败, 此处提前给出明确报错
                std::ifstream ca_file(tls_config_.ssl_ca_path);
                if (!ca_file.good()) {
                    LOG_ERROR("CppWebSocket: ssl_ca_path 文件不可读: %s, 拒绝连接"
                              "(请检查路径与文件权限)", tls_config_.ssl_ca_path.c_str());
                    tryTransition(WebSocketState::CONNECTING, WebSocketState::CLOSED);
                    WeakDummy(weak_ptr);
                    auto executor = drc_sdk::common::Executors::Main();
                    if (executor) {
                        executor->Post([this, weak_ptr]() -> void {
                            WeakDummyReturn(weak_ptr);
                            parent_->callOnError("ssl_ca_path 文件不可读: 无法建立 WSS 连接");
                        });
                    }
                    return;
                }
                info.client_ssl_ca_filepath = tls_config_.ssl_ca_path.c_str();
                LOG_INFO("CppWebSocket: 使用私有 CA 文件: %s", tls_config_.ssl_ca_path.c_str());
            }
            LOG_INFO("CppWebSocket: WSS 证书验证 %s(verify_ssl=%s)",
                     tls_config_.verify_ssl ? "开启" : "关闭(不安全)",
                     tls_config_.verify_ssl ? "true" : "false");
        }
        info.user = &tls_config_;

        lws_context *ctx = lws_create_context(&info);
        context.store(ctx);
        if (!ctx) {
            LOG_ERROR("CppWebSocket: 创建 lws_context 失败");
            // 修复: 同步推进状态, 消除 serviceLoop 已退出但 state 仍为 CONNECTING 的窗口期。
            // 窗口期内 canConnect() 返回 false, 外部 connect() 调用会被误拒。
            tryTransition(WebSocketState::CONNECTING, WebSocketState::CLOSED);
            WeakDummy(weak_ptr);
            auto executor = drc_sdk::common::Executors::Main();
            if (!executor) {
                LOG_WARN("CppWebSocket: Main executor 未初始化, 跳过 callOnError(lws_context create failed)");
            } else {
                executor->Post([this, weak_ptr]() -> void {
                    WeakDummyReturn(weak_ptr);
                    parent_->callOnError("lws_context create failed");
                });
            }
            return;
        }

        // 解析 ws://host:port/path
        std::string address, path;
        int port = 80;
        bool ssl = false;
        if (!parseUrl(url, address, port, path, ssl)) {
            LOG_ERROR("CppWebSocket: URL 解析失败: %s - 请检查URL格式是否正确 (支持 "
                "ws://host:port/path 或 wss://host:port/path)",
                url.c_str());
            // 修复: 同步推进状态, 消除窗口期。先销毁资源, 再投递通知(Post 仅做通知, 不改状态)。
            tryTransition(WebSocketState::CONNECTING, WebSocketState::CLOSED);
            context.store(nullptr);
            lws_context_destroy(ctx);
            ctx = nullptr;
            WeakDummy(weak_ptr);
            auto executor = drc_sdk::common::Executors::Main();
            if (!executor) {
                LOG_WARN("CppWebSocket: Main executor 未初始化, 跳过 callOnError(URL解析失败)");
            } else {
                executor->Post([this, weak_ptr]() -> void {
                    WeakDummyReturn(weak_ptr);
                    parent_->callOnError("URL解析失败: " + url);
                });
            }
            return;
        }

        // 添加网络诊断信息
        LOG_INFO("CppWebSocket: 网络诊断 - 目标地址: %s, 端口: %d, 协议: %s", address.c_str(), port, ssl ? "WSS" : "WS");

        // 记录本次连接目标地址(去端口), 供证书验证回调做主机名/IP 自检
        {
            std::lock_guard<std::mutex> lock(tls_config_.mutex);
            tls_config_.connect_host = address;
        }

        struct lws_client_connect_info ccinfo = {0};
        ccinfo.context = ctx;
        ccinfo.address = address.c_str();
        ccinfo.port = port;
        ccinfo.path = path.c_str();
        ccinfo.host = address.c_str();
        ccinfo.origin = address.c_str();
        ccinfo.protocol = protocol_header_value.c_str();
        // SSL 连接配置。
        // LCCSCF_SKIP_SERVER_CERT_HOSTNAME_CHECK 跳过 lws 内置
        // set1_host 主机名校验 —— 该实现不支持 IP 字面量(demo 默认 IP 直连会被
        // 误判 hostname mismatch), 主机名/IP 校验统一由证书验证回调自检完成。
        ccinfo.ssl_connection = ssl ? (LCCSCF_USE_SSL | LCCSCF_SKIP_SERVER_CERT_HOSTNAME_CHECK) : 0;
        ccinfo.pwsi = &wsi;
        ccinfo.userdata = this;

        // 修复: 持 sendMutex 赋值 wsi, 消除与 close()(在 sendMutex 下置 null)的 data race。
        // lws_client_connect_via_info 是同步调用, 不触发任何回调, 持锁期间安全。
        {
            std::lock_guard<std::mutex> lock(sendMutex);
            wsi = lws_client_connect_via_info(&ccinfo);
        }
        if (!wsi) {
            LOG_ERROR("CppWebSocket: 连接失败 - 目标: %s:%d%s, SSL: %s", address.c_str(), port, path.c_str(), ssl ? "是" : "否");
            // 修复: 同步推进状态, 消除窗口期。先销毁资源, 再投递通知。
            tryTransition(WebSocketState::CONNECTING, WebSocketState::CLOSED);
            context.store(nullptr);
            lws_context_destroy(ctx);
            ctx = nullptr;
            WeakDummy(weak_ptr);
            auto executor = drc_sdk::common::Executors::Main();
            if (!executor) {
                LOG_WARN("CppWebSocket: Main executor 未初始化, 跳过 callOnError(连接初始化失败)");
            } else {
                executor->Post([this, weak_ptr]() -> void {
                    WeakDummyReturn(weak_ptr);
                    parent_->callOnError("连接初始化失败 - 请检查网络连接和服务器状态");
                });
            }
            return;
        }

        LOG_INFO("CppWebSocket: 连接请求已发送, 等待连接建立...");

        // 事件循环
        while (!isClosing()) {
            // 使用 lws_service_adjust_timeout 检查是否有待处理的工作(如 pending
            // writable) 有待处理工作时使用 0ms 超时(立即返回), 否则使用
            // 1ms(等待新事件)
            int has_pending = lws_service_adjust_timeout(ctx, 1, 0);
            int timeout = (has_pending == 0) ? 0 : 1;
            int ret = lws_service(ctx, timeout);
            if (ret < 0) {
                LOG_ERROR("CppWebSocket: lws_service 返回错误: %d", ret);
                break;
            }

            // 修复: 消费主线程 send() 留下的写请求。
            // 此处已在 IO 线程(serviceLoop), 可安全调用 lws_callback_on_writable。
            // send() 仅设 pendingWriteRequest_ 标志(不调用任何 lws API), 
            // lws_service() 超时返回(≤1ms)后本轮即检测并处理, 最大延迟 1ms。
            if (pendingWriteRequest_.load(std::memory_order_acquire)) {
                pendingWriteRequest_.store(false, std::memory_order_relaxed);
                // Bug: 在 sendMutex 保护下读取 wsi 快照, 消除与 close() 的 data
                // race。 close() 先设 CLOSING 状态, 再持 sendMutex 将 wsi 置为 nullptr,
                // 最后 join 等待本线程退出。 因此 isClosing() 为 true 时快照仍可能非空,
                // 但后续 isClosing() 检查会正确跳过调用。 snap_wsi 在本线程退出前不会被
                // lws_context_destroy 释放(join 尚未完成), 指针始终有效。
                struct lws *snap_wsi = nullptr;
                {
                    std::lock_guard<std::mutex> lock(sendMutex);
                    snap_wsi = wsi;
                }
                if (snap_wsi && isConnected() && !isClosing()) {
                    lws_callback_on_writable(snap_wsi);
                }
            }

            // 注意: 此处不可再调用 lws_service_tsi()。
            // lws_service 已完整处理所有就绪事件, 额外调用 lws_service_tsi 属于重入,
            // 会损坏 lws 内部的 writable 调度状态, 导致 WRITEABLE 回调永久丢失。

            // 通用的 WRITEABLE 事件超时重试机制(适用于所有平台)
            // 如果队列中有数据但长时间没有收到 WRITEABLE 事件, 主动触发
            //
            // 线程安全说明(外层条件无需持 sendMutex): 
            //   isConnected() = state_.load(seq_cst), 纯原子读, 无数据竞争。
            //   close() 的严格顺序: ① CAS state→CLOSING(seq_cst)→ ② sendMutex 下
            //   wsi=nullptr。 C++ seq_cst 全序保证: 若本线程观测到 CONNECTED, 则
            //   CLOSING 尚未写入, 因此 wsi=nullptr 也尚未执行 —— 外层无需读 wsi, 无
            //   data race。 内层 macOS / 非 macOS 块均在各自的 sendMutex 下重新读取
            //   wsi, 可正确处理 close() 与健康检查的并发边界。
            if (isConnected() && !isClosing()) {
#ifdef __APPLE__
                // macOS 特定: 使用 writeablePending 标志
                if (writeablePending.load()) {
                    auto now = std::chrono::steady_clock::now();
                    // 修复: elapsed 计算需读取 lastWriteableRequest(非原子 time_point),
                    // send()(主线程)写入它时已持 sendMutex, 此处读取也须持同一把锁,
                    // 消除 data race。同时将 elapsed 检查与后续操作合并到同一把锁内,
                    // 减少一次加锁/解锁, 比原来的「先解锁再重新加锁」更高效。
                    std::lock_guard<std::mutex> lock(sendMutex);
                    auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(
                                                          now - lastWriteableRequest)
                                                          .count();
                    if (elapsed > NCSDK_WRITEABLE_PENDING_TIMEOUT_MS) {
                        // 验证 wsi 仍然有效
                        if (!sendQueue.empty() && wsi && !isClosing() && ctx) {
                            void *wsi_user = lws_wsi_user(wsi);
                            if (wsi_user == this) {
                                LOG_WARN("CppWebSocket: WRITEABLE事件超时(%lld ms), 重新触发, "
                                         "队列大小: %zu",
                                         (long long)elapsed, sendQueue.size());
                                lastWriteableRequest = now;
                                lws_callback_on_writable(wsi);
                            } else {
                                LOG_WARN("CppWebSocket: wsi 已失效, 清除 pending 标志");
                                writeablePending = false;
                            }
                        } else {
                            writeablePending = false;
                        }
                    }
                }
#else
                // 非 macOS 平台: 定期检查队列健康状态
                // 如果队列中有数据但 WRITEABLE 链条已断裂, 主动触发恢复。
                auto now = std::chrono::steady_clock::now();
                auto check_elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(now - lastQueueHealthCheck_).count();

                if (check_elapsed > NCSDK_QUEUE_HEALTH_CHECK_INTERVAL_MS) {
                    lastQueueHealthCheck_ = now;
                    bool force_close = false;
                    {
                        std::lock_guard<std::mutex> lock(sendMutex);
                        // 修复: 在健康检查中直接淘汰 TTL 过期消息, 不再依赖 WRITEABLE
                        // 回调。 TCP 拥塞期间 WRITEABLE 无法触发,
                        // 旧消息仍能被及时清理以释放内存。修复: 复用外层 now,
                        // 避免在锁内重复调用 steady_clock::now()。
                        size_t hc_stale_dropped = 0;
                        while (!sendQueue.empty() &&
                               hc_stale_dropped < NCSDK_HEALTH_CHECK_EVICT_BATCH) {
                            auto age_ms = std::chrono::duration_cast<std::chrono::milliseconds>(
                                    now - sendQueue.front().enqueue_time).count();
                            if (age_ms <= NCSDK_MSG_TTL_MS)
                                break;
                            sendQueue.pop_front();
                            hc_stale_dropped++;
                        }
                        if (hc_stale_dropped > 0) {
                            LOG_WARN("CppWebSocket: 健康检查丢弃 %zu 条过期消息(TTL=%lldms), 剩余: %zu",
                                hc_stale_dropped, (long long)NCSDK_MSG_TTL_MS, sendQueue.size());
                        }

                        if (!sendQueue.empty() && wsi && !isClosing() && ctx) {
                            void *wsi_user = lws_wsi_user(wsi);
                            if (wsi_user == this) {
                                // 修复: 检测 WRITEABLE 是否有实际进展。
                                // writeableCallbackCount_ 每次 WRITEABLE 回调递增(IO 线程独占,
                                // 无锁)。 若本周期计数未变, 说明 socket 不可写(TCP 拥塞)。
                                if (writeableCallbackCount_ == lastWriteableCount_) {
                                    consecutiveStuckChecks_++;
                                    if (consecutiveStuckChecks_ >= NCSDK_CONGESTION_STUCK_THRESHOLD) {
                                        LOG_ERROR("CppWebSocket: 健康检查: %d 次(约%dms)无 WRITEABLE 响应, "
                                            "队列大小: %zu, 判定为 TCP 拥塞, 主动断开重连",
                                            consecutiveStuckChecks_,
                                            consecutiveStuckChecks_ * NCSDK_QUEUE_HEALTH_CHECK_INTERVAL_MS,
                                            sendQueue.size());
                                        force_close = true;
                                    } else if (consecutiveStuckChecks_ % 25 == 0) {
                                        // 每 25 × 50ms = 1.25 秒打印一次拥塞预警
                                        LOG_WARN("CppWebSocket: 健康检查: 已 %d 次无 WRITEABLE 响应, 队列大小: %zu",
                                            consecutiveStuckChecks_, sendQueue.size());
                                    }
                                } else {
                                    consecutiveStuckChecks_ = 0;
                                    lastWriteableCount_ = writeableCallbackCount_;
                                }

                                if (!force_close) {
                                    LOG_DEBUG("CppWebSocket: 队列健康检查, 队列大小: %zu, 主动触发 WRITEABLE 事件", sendQueue.size());
                                    lws_callback_on_writable(wsi);
                                }
                            }
                        } else {
                            // 队列已空, 重置拥塞计数
                            consecutiveStuckChecks_ = 0;
                            lastWriteableCount_ = writeableCallbackCount_;
                        }
                    }
                    // force_close: 锁已释放, 退出服务循环。
                    // 不在此处转换状态——lws_context_destroy 会触发 LWS_CALLBACK_CLOSED,
                    // 该回调负责将 CONNECTED → CLOSED 并投递 callOnClose() 通知上层重连。
                    // 若并发 close() 先抢占了 CLOSING 状态, callOnClose()
                    // 被抑制是正确行为 (上层主动调用 close(), 不需要触发重连)。
                    if (force_close) {
                        break;
                    }
                }
#endif
            }
        }

        LOG_INFO("CppWebSocket: 服务线程退出");

        // force_close / lws_service 错误导致的非正常退出: 推状态到 CLOSED 并记录标志。
        // 正常退出路径(isClosing()==true): state 已是 CLOSING 或 CLOSED, CAS 静默失败。
        // CAS 成功 → involuntary_exit=true → 后续补投 callOnClose()(若 LWS_CALLBACK_CLOSED 未触发)。
        bool involuntary_exit = false;
        {
            WebSocketState exp = WebSocketState::CONNECTED;
            if (state_.compare_exchange_strong(exp, WebSocketState::CLOSED)) {
                LOG_DEBUG("CppWebSocket: 服务线程退出前推状态 CONNECTED -> CLOSED");
                involuntary_exit = true;
            }
        }

        // 清理资源: 三步顺序固定, 构成 send() UAF 的完整防护(见 Issue #1 修复注释)。
        //   ① sendMutex 下 wsi=nullptr
        //   ② context.store(nullptr)
        //   ③ lws_context_destroy(ctx)  ← 可能同步触发 LWS_CALLBACK_CLOSED
        {
            std::lock_guard<std::mutex> lock(sendMutex);
            wsi = nullptr; // ①
        }
        if (ctx) {
            context.store(nullptr);   // ②
            lws_context_destroy(ctx); // ③ 可能同步触发 LWS_CALLBACK_CLOSED
            ctx = nullptr;
        }

        // 修复: force_close 路径不依赖 lws_context_destroy 触发 LWS_CALLBACK_CLOSED。
        // lws v2.4.2 不保证在 context 销毁时对已建立连接同步触发 CLOSED 回调。
        // 使用 close_notified_ 原子标志去重: 
        //   - 若 LWS_CALLBACK_CLOSED 已在 lws_context_destroy 中触发并抢先 exchange(), 
        //     此处 exchange() 返回 true, 跳过, 防止双重调用。
        //   - 若未触发, 此处 exchange() 返回 false, 补投 callOnClose(), 防止漏调用。
        if (involuntary_exit) {
            // 修复: 先检查 executor 是否可用, 再消费 close_notified_ 标志。
            // 原来的写法 (!close_notified_.exchange(true)) 在 if 条件中直接消费标志,
            // 若 executor 为 null(初始化竞态或关闭竞态), 标志已消费但通知未投递,
            // LWS_CALLBACK_CLOSED 路径随后也会看到 exchange() 返回 true 而跳过,
            // 导致 callOnClose() 永远不会送达上层。
            // 新写法: executor 可用时才消费标志并投递, 保留重试机会。
            auto executor = drc_sdk::common::Executors::Main();
            if (!executor) {
                LOG_WARN("CppWebSocket: Main executor 未初始化, 跳过 callOnClose (involuntary exit)");
            } else if (!close_notified_.exchange(true)) {
                WeakDummy(weak_ptr);
                executor->Post([this, weak_ptr]() -> void {
                    WeakDummyReturn(weak_ptr);
                    parent_->callOnClose();
                });
            }
        }

        // 如果还在 CONNECTING 状态, 转换到 CLOSED(连接失败)
        tryTransition(WebSocketState::CONNECTING, WebSocketState::CLOSED);
    }

    // lws 回调函数(静态)
    static int lwsCallback(struct lws *wsi, enum lws_callback_reasons reason,
                           void *user, void *in, size_t len) {
        // 处理 SSL 证书验证回调(此时 self 可能为空)
        // (lws 2.4.2 实测, 见 lws OpenSSL_client_verify_callback):
        //   - 回调返回 0 = 接受, 非 0 = 拒绝(returning 1 will fail the cert in any case)
        //   - 接受失败证书必须同时 set_error(X509_V_OK) + return 0, 否则 lws 内部二次拒绝
        if (reason == LWS_CALLBACK_OPENSSL_PERFORM_SERVER_CERT_VERIFICATION) {
            X509_STORE_CTX *x509_ctx = (X509_STORE_CTX *)user;
            int preverify_ok = (int)len;

            // 配置经 context user 取回(连接早期 lws_wsi_user 不可用, 见 WssTlsConfig 注释)
            auto *tls_cfg = static_cast<WssTlsConfig *>(lws_context_user(lws_get_context(wsi)));
            bool verify_ssl = false;
            std::string connect_host;
            if (tls_cfg) {
                std::lock_guard<std::mutex> lock(tls_cfg->mutex);
                verify_ssl = tls_cfg->verify_ssl;
                connect_host = tls_cfg->connect_host;
            }

            // 显式逃生舱: verify_ssl=false 仅限受控测试环境, 必须留下 WARN 痕迹
            if (!verify_ssl) {
                LOG_WARN("CppWebSocket: verify_ssl=false, 跳过 WSS 证书验证(不安全, 仅限受控测试环境)");
                X509_STORE_CTX_set_error(x509_ctx, X509_V_OK);
                return 0;
            }

            if (!preverify_ok) {
                int err = X509_STORE_CTX_get_error(x509_ctx);
                int depth = X509_STORE_CTX_get_error_depth(x509_ctx);
                LOG_ERROR("CppWebSocket: WSS 证书验证失败: %s (err=%d, depth=%d, preverify=%d), 拒绝连接。"
                          "私有化自签名部署请在初始化配置中设置 ssl_ca_path 指向 CA 证书文件",
                          X509_verify_cert_error_string(err), err, depth, preverify_ok);
                return 1; // 非 0 = 拒绝握手
            }

            // 叶子证书(depth==0)主机名/IP 自检。
            // lws 内置 set1_host 校验已被 LCCSCF_SKIP_SERVER_CERT_HOSTNAME_CHECK 跳过
            // (不支持 IP 字面量), 此处统一自检: IP 地址用 X509_check_ip_asc,
            // 域名用 X509_check_host, 语义与浏览器一致, 防止"有效 CA 签发的他人证书"通过。
            if (X509_STORE_CTX_get_error_depth(x509_ctx) == 0 && !connect_host.empty()) {
                X509 *cert = X509_STORE_CTX_get_current_cert(x509_ctx);
                struct in_addr addr4;
                struct in6_addr addr6;
                bool is_ip = (inet_pton(AF_INET, connect_host.c_str(), &addr4) == 1 ||
                              inet_pton(AF_INET6, connect_host.c_str(), &addr6) == 1);
                bool name_ok = is_ip
                        ? (X509_check_ip_asc(cert, connect_host.c_str(), 0) == 1)
                        : (X509_check_host(cert, connect_host.c_str(), connect_host.size(),
                                           X509_CHECK_FLAG_NO_PARTIAL_WILDCARDS, nullptr) == 1);
                if (!name_ok) {
                    LOG_ERROR("CppWebSocket: WSS 证书主机名与连接地址不匹配 (addr=%s), 拒绝连接",
                              connect_host.c_str());
                    return 1;
                }
                LOG_INFO("CppWebSocket: WSS 证书验证通过(链校验+主机名/IP校验, addr=%s)",
                         connect_host.c_str());
            }
            return 0; // preverify 通过(链验证) → 接受
        }

        Impl *self = reinterpret_cast<Impl *>(lws_wsi_user(wsi));
        if (!self) {
            if (reason == LWS_CALLBACK_CLIENT_ESTABLISHED ||
                    reason == LWS_CALLBACK_CLIENT_RECEIVE ||
                    reason == LWS_CALLBACK_CLIENT_WRITEABLE ||
                    reason == LWS_CALLBACK_CLOSED ||
                    reason == LWS_CALLBACK_CLIENT_CONNECTION_ERROR) {
                self = reinterpret_cast<Impl *>(user);
            }
        }
        if (!self)
            return 0;

        // 在所有回调中, 先检查是否正在关闭
        // 这样可以避免在关闭过程中处理回调
        if (self->isClosing()) {
            // 对于某些关键回调, 即使正在关闭也需要处理
            if (reason != LWS_CALLBACK_CLOSED &&
                    reason != LWS_CALLBACK_CLIENT_CONNECTION_ERROR) {
                LOG_DEBUG("CppWebSocket: 回调时检测到关闭状态, 忽略回调 reason=%d", reason);
                return 0;
            }
        }

        switch (reason) {
        case LWS_CALLBACK_CLIENT_ESTABLISHED: {
            // 再次检查是否正在关闭(可能在回调期间状态改变)
            if (self->isClosing()) {
                LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 连接建立时检测到关闭状态, 忽略");
                return 0;
            }

            // 优化方案3: 设置 TCP_NODELAY, 禁用 Nagle 算法
            // 原理: Nagle 算法会延迟小包的发送, 禁用后可以立即发送小包, 减少 TCP
            // 层延迟 预期效果: 可能进一步降低 WRITEABLE 事件间隔 10-20%
            int fd = lws_get_socket_fd(wsi);
            if (fd >= 0) {
                // 1. 设置 TCP_NODELAY(禁用 Nagle 算法)
                int flag = NCSDK_TCP_NODELAY_FLAG;
                if (setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, (char *)&flag, sizeof(flag)) < 0) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 设置 TCP_NODELAY 失败");
                } else {
                    LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 已启用 TCP_NODELAY(禁用 Nagle 算法)");
                }

                // 2. 设置 TCP 发送缓冲区(客户端主要发送, 这个更重要)
                int sndbuf = NCSDK_TCP_SEND_BUFFER_SIZE;
                if (setsockopt(fd, SOL_SOCKET, SO_SNDBUF, (char *)&sndbuf, sizeof(sndbuf)) < 0) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 设置 SO_SNDBUF 失败");
                } else {
                    LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 已设置发送缓冲区: %d KB", sndbuf / 1024);
                }

                // 3. 设置 TCP 接收缓冲区
                int rcvbuf = NCSDK_TCP_RECV_BUFFER_SIZE;
                if (setsockopt(fd, SOL_SOCKET, SO_RCVBUF, (char *)&rcvbuf, sizeof(rcvbuf)) < 0) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 设置 SO_RCVBUF 失败");
                } else {
                    LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 已设置接收缓冲区: %d KB", rcvbuf / 1024);
                }
            } else {
                LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 无法获取 socket 文件描述符, 无法设置 TCP 选项");
            }

            // 转换到 CONNECTED 状态
            // 修复: callOnOpen() 仅在状态转换成功时调用。
            // 若转换失败(如 close() 已将 state 推至 CLOSING), 上层不应收到"连接成功"通知, 
            // 否则上层认为连接可用, 但实际上正在关闭, 逻辑矛盾。
            if (self->tryTransition(WebSocketState::CONNECTING, WebSocketState::CONNECTED)) {
                LOG_INFO("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 连接已建立");
                auto weak_dummy = std::weak_ptr<drc_sdk::common::Dummy>(self->dummy_ptr_);
                auto executor = drc_sdk::common::Executors::Main();
                if (!executor) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: Main executor 未初始化, 跳过 callOnOpen");
                } else {
                    executor->Post([self, weak_dummy]() -> void {
                        if (!weak_dummy.lock()) return;
                        self->parent_->callOnOpen();
                    });
                }
            } else {
                LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_ESTABLISHED]: 连接建立但状态转换失败"
                    "(可能正在关闭), 不通知上层。当前状态: %d",
                    static_cast<int>(self->getState()));
            }
            break;
        }

        case LWS_CALLBACK_CLIENT_RECEIVE:
            // 检查是否正在关闭
            if (self->isClosing()) {
                LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE]: 接收消息时检测到关闭状态, 忽略");
                return 0;
            }
            if (in && len > 0) {
                // 检查是否是最后一个片段(FIN标志)
                // 注意: lws_is_final_fragment 需要有效的 wsi
                bool is_final = lws_is_final_fragment(wsi);

                // 修复: 将 Post() 移出 receiveMutex 锁范围。
                // 原来在持锁期间调用 Post(), 若 Post() 内部有锁且主线程持该锁时
                // 访问 receiveMutex, 可能死锁；即便无死锁也不必要地延长了持锁时间。
                // 方案: 锁内只做数据操作, 锁外再投递回调。
                bool msg_overflow = false;
                std::string completeMsg;
                {
                    std::lock_guard<std::mutex> lock(self->receiveMutex);

                    // 防止恶意服务器发送超大消息导致内存耗尽
                    if (self->receiveBuffer.size() + len > NCSDK_MAX_MESSAGE_SIZE) {
                        LOG_ERROR("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE]: "
                            "消息大小超过限制 (%zu + %zu > %zu), 丢弃消息",
                            self->receiveBuffer.size(), len, NCSDK_MAX_MESSAGE_SIZE);
                        self->receiveBuffer.clear();
                        msg_overflow = true;
                        // 不 break: oversized 错误路径同样需要执行后续 Fix 1+2。
                        // SSL 内循环在错误路径下仍在运行, POLLOUT 饥饿修复不可跳过。
                    } else {
                        // 累积当前片段的数据
                        self->receiveBuffer.append((const char *)in, len);

                        LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE]: 收到消息片段, "
                            "长度: %zu, 是否最后片段: %s, 累积长度: %zu",
                            len, is_final ? "是" : "否", self->receiveBuffer.size());

                        // 如果是最后一个片段, 将完整消息移出(锁内), 锁外再 Post
                        if (is_final) {
                            completeMsg = std::move(self->receiveBuffer);
                            // move 后 receiveBuffer 已是合法空串, 无需再 clear()
                        }
                    }
                }
                // receiveMutex 已释放, 再投递回调
                if (msg_overflow) {
                    auto executor = drc_sdk::common::Executors::Main();
                    if (!executor) {
                        LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE]: Main executor 未初始化, 跳过 callOnError(消息大小超过限制)");
                    } else {
                        executor->Post(
                            [self, weak_dummy = std::weak_ptr<drc_sdk::common::Dummy>(self->dummy_ptr_)]() -> void {
                                if (!weak_dummy.lock()) return;
                                self->parent_->callOnError("消息大小超过限制");
                        });
                    }
                } else if (!completeMsg.empty()) {
                    LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE]: 收到完整消息, 总长度: %zu, 内容: %s",
                        completeMsg.size(), completeMsg.c_str());

                    // 移动捕获, 避免对大消息(最大 64MB)做完整堆内存拷贝
                    auto executor = drc_sdk::common::Executors::Main();
                    if (!executor) {
                        LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE]: Main executor 未初始化, 跳过 callOnMessage");
                    } else {
                        executor->Post(
                        [self,
                            weak_dummy = std::weak_ptr<drc_sdk::common::Dummy>(self->dummy_ptr_),
                            completeMsg = std::move(completeMsg)]() -> void {
                            if (!weak_dummy.lock()) return;
                            self->parent_->callOnMessage(completeMsg);
                        });
                    }
                }

                // Fix: 对抗 SSL_pending() 导致的 POLLOUT 饥饿 (lws issue #1700)
                //
                // 背景: lws 2.4.2 SSL 场景下, SSL_pending() 非零时 lws_service() 内部
                // 反复 SSL_read 而不返回 poll(), 主线程 send() 写入的 cancel pipe 字节
                // 持续积累却永不被消费, WRITEABLE 链路永久断裂, ACK 积压形成重传风暴。
                //
                // Fix 1(lws_callback_on_writable): 每条消息到达时重置 POLLOUT 标志。
                //   lws 下次回到 poll() 时 POLLOUT 已就绪, WRITEABLE 立即触发。
                //   sendMutex 保证读 sendQueue 的线程安全(主线程持锁写, IO
                //   线程持锁读)。
                //
                // Fix 2(lws_cancel_service): 每 NCSDK_RECV_THROTTLE_N 条消息强制打断
                //   SSL 读内循环, 使 lws 回到 poll() 顶层, Fix 1 的 POLLOUT
                //   才能被调度。 从 IO 线程调用安全: lws_cancel_service 仅向 cancel
                //   pipe 写 1 字节。
                //
                // 两者配合: Fix 2 让 lws 有机会回到 poll(), Fix 1 确保 POLLOUT 已就绪。
                //
                // 结构说明: Fix 1+2 置于 if (in && len > 0) 内部: 
                //   ① 仅在收到实际数据时执行, 避免空 RECEIVE 产生无效操作(Bug 2 修复)
                //   ② oversized 错误路径同样执行(去除 break, 改用 else 分支)(Bug 1
                //   修复)
                {
                    std::lock_guard<std::mutex> lock(self->sendMutex);
                    if (!self->sendQueue.empty()) {
                        lws_callback_on_writable(wsi); // Fix 1
                    }
                }
                if ((++self->recvCount_ % NCSDK_RECV_THROTTLE_N) == 0) {
                    auto ctx_snap = self->context.load(); // Fix 2
                    if (ctx_snap) {
                        lws_cancel_service(ctx_snap);
                    }
                }
            }
            break;

        case LWS_CALLBACK_CLIENT_WRITEABLE: {
            // 验证 wsi 的有效性: 检查 lws_wsi_user 是否返回正确的 self
            // 如果 wsi 已经被释放, lws_wsi_user 可能返回 NULL 或无效值
            void *wsi_user = lws_wsi_user(wsi);
            if (wsi_user != self) {
                LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: wsi 已失效(userdata 不匹配), 忽略写回调");
                return 0;
            }

            // 再次检查 context 是否有效
            if (!self->context.load()) {
                LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: context 已销毁, 忽略写回调");
                return 0;
            }

            // 检查是否正在关闭(在验证 wsi 有效性之后)
            if (self->isClosing()) {
                LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 回调时检测到关闭状态, 忽略写回调");
                return 0;
            }

            // lws_validity_confirmed 在 v2.4.2 中不存在, 跳过

#ifdef __APPLE__
            // 清除pending标志
            self->writeablePending = false;
#else
            // 记录 WRITEABLE 实际触发次数, 供健康检查拥塞检测使用(IO 线程独占)
            self->writeableCallbackCount_++;
#endif
            // 批量发送: 每次 WRITEABLE 最多发送 NCSDK_WRITEABLE_BATCH_SIZE 条,
            // 每条发送后通过 lws_send_pipe_choked() 检查 TCP/SSL 缓冲区,
            // 缓冲区满则立即停止, 等待下次 WRITEABLE, 避免数据丢失。

            // TTL 淘汰: 队列堆积时, 队首的过期 ACK 已无发送意义(服务端早已超时),
            // 丢弃过期消息, 避免浪费带宽和延迟新消息投递。
            // 因为队列是 FIFO, 时间戳单调递增, 一旦遇到未过期消息即可停止。
            // 修复: 与健康检查对齐, 加 NCSDK_HEALTH_CHECK_EVICT_BATCH 上限,
            // 防止 TCP 拥塞恢复时一次性淘汰大量消息导致 sendMutex 持锁过长。
            auto ttl_now = std::chrono::steady_clock::now();
            size_t stale_dropped = 0;
            size_t queue_size_at_entry = 0;
            {
                std::lock_guard<std::mutex> lock(self->sendMutex);
                while (!self->sendQueue.empty() &&
                       stale_dropped < NCSDK_HEALTH_CHECK_EVICT_BATCH) {
                    auto age_ms = std::chrono::duration_cast<std::chrono::milliseconds>(
                            ttl_now - self->sendQueue.front().enqueue_time).count();
                    if (age_ms <= NCSDK_MSG_TTL_MS)
                        break;
                    self->sendQueue.pop_front();
                    stale_dropped++;
                }
                queue_size_at_entry = self->sendQueue.size();
            }
            if (stale_dropped > 0) {
                LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 丢弃 %zu "
                    "条过期消息(TTL=%lldms), 剩余队列: %zu",
                    stale_dropped, (long long)NCSDK_MSG_TTL_MS,
                    queue_size_at_entry);
            }
            LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: WRITEABLE "
                "事件触发, 队列大小: %zu",
                queue_size_at_entry);

            int sent_count = 0;
            bool write_error = false;

            while (sent_count < NCSDK_WRITEABLE_BATCH_SIZE) {
                // 每轮迭代前检查关闭状态
                if (self->isClosing() || !self->context.load())
                    break;

                std::string msg;
                std::chrono::steady_clock::time_point
                        msg_enqueue_time; // 保留原始入队时间
                {
                    std::lock_guard<std::mutex> lock(self->sendMutex);
                    if (self->sendQueue.empty())
                        break;
                    msg_enqueue_time = self->sendQueue.front().enqueue_time; // 保存原始时间
                    msg = std::move(self->sendQueue.front().data);
                    self->sendQueue.pop_front();
                }

                // 构造 WebSocket 帧并发送(锁已释放, 不阻塞入队)
                std::vector<unsigned char> buf(LWS_PRE + msg.size());
                memcpy(buf.data() + LWS_PRE, msg.data(), msg.size());
                int n = lws_write(wsi, buf.data() + LWS_PRE, msg.size(), LWS_WRITE_TEXT);

                if (n < 0) {
                    LOG_ERROR("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 发送失败, 返回值: %d, 消息长度: %zu", n, msg.size());
                    // 发送失败时, 将消息重新放回队列前端(用当前时间重置 TTL, 允许重试)
                    {
                        std::lock_guard<std::mutex> lock(self->sendMutex);
                        self->sendQueue.push_front({std::move(msg), std::chrono::steady_clock::now()});
                    }
                    auto executor = drc_sdk::common::Executors::Main();
                    if (!executor) {
                        LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: Main executor 未初始化, 跳过 callOnError(send failed)");
                    } else {
                        executor->Post(
                            [self, weak_dummy = std::weak_ptr<drc_sdk::common::Dummy>(self->dummy_ptr_)]() -> void {
                                if (!weak_dummy.lock())
                                    return;
                                self->parent_->callOnError("send failed");
                        });
                    }
                    write_error = true;
                    break;
                }

                if (n != (int)msg.size()) {
                    // 修复: lws_write 部分发送(0 < n < msg.size())是致命协议错误。
                    // lws_write 调用时已向对端发出帧头, 声明 payload 长度为 msg.size()；
                    // 但内核缓冲区仅接收了前 n 字节, lws 不在内部缓存剩余字节。
                    // 若将 msg.substr(n) 作为新的 WebSocket 帧重发, 对端将收到: 
                    //   [帧头(M字节payload) + n字节 payload][新帧头 + remaining]
                    // 这将导致对端 WebSocket 协议解析器报错, 连接被强制关闭。
                    // 正确做法: 将完整原始消息放回队列头, 触发重连, 重连后完整重发。
                    LOG_ERROR("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: "
                        "lws_write 部分发送(expected=%zu, actual=%d), "
                        "WebSocket 帧完整性已破坏, 放回完整消息并触发重连",
                        msg.size(), n);
                    {
                        std::lock_guard<std::mutex> lock(self->sendMutex);
                        // 放回完整原始消息(非 remaining), 保留原始入队时间
                        self->sendQueue.push_front({std::move(msg), msg_enqueue_time});
                    }
                    write_error = true;
                    break;
                }

                sent_count++;
                LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 已发送: %s", msg.c_str());

                // 关键安全检查: lws_send_pipe_choked 检测 TCP/SSL 发送管道是否拥塞。
                // 拥塞时继续 lws_write 会导致数据丢失或未定义行为, 必须停止。
                if (lws_send_pipe_choked(wsi)) {
                    LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 发送管道拥塞, 本次已发送 %d 条, 等待下次 WRITEABLE",
                        sent_count);
                    break;
                }
            }

            if (sent_count > 1) {
                LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 批量发送完成, 本次发送 %d 条, 剩余队列约 %zu 条",
                    sent_count,
                    queue_size_at_entry > (size_t)sent_count ? queue_size_at_entry - sent_count: 0);
            }

            // 队列中还有数据时, 请求下一次 WRITEABLE 回调
            if (!write_error) {
                std::lock_guard<std::mutex> lock(self->sendMutex);
                if (!self->sendQueue.empty() && !self->isClosing() && self->context.load()) {
                    void *wsi_user_check = lws_wsi_user(wsi);
                    if (wsi_user_check == self) {
                        LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: 队列中还有 %zu 条消息, 继续触发写事件", self->sendQueue.size());
                        lws_callback_on_writable(wsi);
#ifdef __APPLE__
                        // 标记链式请求, 让 serviceLoop 超时检测能发现链条断裂
                        self->writeablePending = true;
                        self->lastWriteableRequest = std::chrono::steady_clock::now();
#endif
                        // 注意: 此处不可调用 lws_cancel_service / lws_service_tsi,
                        // 当前已处于 lws 回调上下文中, 重入事件循环会损坏 lws 内部 writable
                        // 状态。
                    } else {
                        LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_WRITEABLE]: wsi 已失效, 无法继续触发写事件");
                    }
                }
            }
            break;
        }

        case LWS_CALLBACK_CLOSED: {
            // 先检查是否正在关闭(CLOSED 回调即使正在关闭也需要处理)
            // 转换到 CLOSED 状态(如果当前是 CONNECTED 或 CONNECTING)
            WebSocketState current = self->getState();
            if (current == WebSocketState::CONNECTED ||
                    current == WebSocketState::CONNECTING) {
                self->tryTransition(current, WebSocketState::CLOSED);
            }

            // 清空接收缓冲区(连接关闭时丢弃未完成的消息)
            {
                std::lock_guard<std::mutex> lock(self->receiveMutex);
                if (!self->receiveBuffer.empty()) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLOSED]: 连接关闭时丢弃未完成的消息片段, 长度: %zu", self->receiveBuffer.size());
                    self->receiveBuffer.clear();
                }
            }

            // 获取关闭原因
            std::string close_reason = "正常关闭";
            if (in && len > 0) {
                close_reason = std::string((const char *)in, len);
            }

            LOG_WARN("CppWebSocket[LWS_CALLBACK_CLOSED]: 连接已关闭 - 原因: %s, URL: %s",
                close_reason.c_str(), self->url.c_str());

            // 修复: 通过 close_notified_ 原子标志去重, 与 serviceLoop 补投路径互斥。
            // 修复2: 先检查 executor 是否可用, 再消费 close_notified_ 标志。
            // 原来的写法 (!close_notified_.exchange(true)) 在 if 条件中直接消费标志,
            // 若 executor 为 null(初始化竞态或关闭竞态), 标志已消费但通知未投递,
            // serviceLoop 补投路径随后也会看到 exchange() 返回 true 而跳过,
            // 导致 callOnClose() 永远不会送达上层。
            // 新写法: executor 可用时才消费标志并投递, 保留重试机会。
            const bool user_initiated_close = (current == WebSocketState::CLOSING);
            if (!user_initiated_close) {
                auto executor = drc_sdk::common::Executors::Main();
                if (!executor) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLOSED]: Main executor 未初始化, 跳过 callOnClose");
                } else if (!self->close_notified_.exchange(true)) {
                    executor->Post(
                        [self, weak_dummy = std::weak_ptr<drc_sdk::common::Dummy>(self->dummy_ptr_)]() -> void {
                            if (!weak_dummy.lock())
                                return;
                            self->parent_->callOnClose();
                        });
                }
            }
            break;
        }

        case LWS_CALLBACK_CLIENT_RECEIVE_PONG:
            LOG_DEBUG("CppWebSocket[LWS_CALLBACK_CLIENT_RECEIVE_PONG]: 收到pong");
            // lws_validity_confirmed 在 v2.4.2 中不存在, 跳过
            break;

        case LWS_CALLBACK_CLIENT_CONNECTION_ERROR: {
            // 连接错误时转换到 CLOSED 状态(如果当前是 CONNECTING 或 CONNECTED)
            WebSocketState current = self->getState();
            if (current == WebSocketState::CONNECTING ||
                    current == WebSocketState::CONNECTED) {
                self->tryTransition(current, WebSocketState::CLOSED);
            }

            // 清空接收缓冲区(连接错误时丢弃未完成的消息)
            {
                std::lock_guard<std::mutex> lock(self->receiveMutex);
                if (!self->receiveBuffer.empty()) {
                    LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_CONNECTION_ERROR]: "
                        "连接错误时丢弃未完成的消息片段, 长度: %zu",
                        self->receiveBuffer.size());
                    self->receiveBuffer.clear();
                }
            }

            // 获取详细的错误信息
            std::string error_details = "connection error";
            if (in && len > 0) {
                error_details = std::string((const char *)in, len);
            }
            const unsigned int http_code = wsi ? lws_http_client_http_response(wsi) : 0;

            // 获取连接信息用于日志
            std::string peer_info = "unknown";
            if (wsi) {
                int fd = lws_get_socket_fd(wsi);
                if (fd >= 0) {
                    // lws_get_peer_addresses: 第3参数(peer_hostname)填入对端 hostname,
                    //                         第5参数(peer_rip)填入对端 IP 地址。
                    // 注意: 该 API 不返回端口号。端口信息可从 self->url 中获取。
                    char peer_hostname[128] = {0};
                    char peer_rip[64] = {0};
                    lws_get_peer_addresses(wsi, fd, peer_hostname, sizeof(peer_hostname), peer_rip, sizeof(peer_rip));
                    if (peer_rip[0] != '\0') {
                        peer_info = peer_rip;
                    } else if (peer_hostname[0] != '\0') {
                        peer_info = peer_hostname;
                    }
                }
            }

            LOG_ERROR("CppWebSocket[LWS_CALLBACK_CLIENT_CONNECTION_ERROR]: 连接出错 "
                "- HTTP状态: %u, 详细信息: %s, 目标地址: %s, URL: %s",
                http_code, error_details.c_str(), peer_info.c_str(), self->url.c_str());
            if (http_code == 401 || http_code == 403) {
                LOG_ERROR("CppWebSocket[LWS_CALLBACK_CLIENT_CONNECTION_ERROR]: 握手鉴权失败, "
                          "请检查 websocket auth headers 与 token 有效性");
            }

            auto executor = drc_sdk::common::Executors::Main();
            if (!executor) {
                LOG_WARN("CppWebSocket[LWS_CALLBACK_CLIENT_CONNECTION_ERROR]: Main executor 未初始化, 跳过 callOnError");
            } else {
                executor->Post(
                    [self, weak_dummy = std::weak_ptr<drc_sdk::common::Dummy>(self->dummy_ptr_), error_details, http_code]() -> void {
                        if (!weak_dummy.lock())
                            return;
                        // 连接错误回调允许触发(即使正在关闭)
                        if (http_code == 401 || http_code == 403) {
                            self->parent_->callOnError("auth_failed:http_" + std::to_string(http_code) + ":" + error_details);
                        } else {
                            self->parent_->callOnError(error_details);
                        }
                    });
            }
            break;
        }

        default:
            break;
        }
        return 0;
    }

    // URL 解析工具
    static bool parseUrl(const std::string &url, std::string &address, int &port, std::string &path, bool &ssl) {
        // 支持 ws://host:port/path 或 wss://host:port/path
        ssl = false;
        size_t pos = 0;
        if (url.find("ws://") == 0) {
            pos = 5;
            ssl = false;
        } else if (url.find("wss://") == 0) {
            pos = 6;
            ssl = true;
        } else {
            return false;
        }
        size_t slash = url.find('/', pos);
        size_t colon = url.find(':', pos);
        if (colon != std::string::npos &&
                (slash == std::string::npos || colon < slash)) {
            address = url.substr(pos, colon - pos);
            size_t port_start = colon + 1;
            size_t port_end = (slash == std::string::npos) ? url.size() : slash;

            // 手动解析端口号, 不使用异常
            std::string port_str = url.substr(port_start, port_end - port_start);
            if (port_str.empty()) {
                return false; // 端口号不能为空
            }

            // 检查是否全部是数字
            bool is_valid_port = true;
            for (size_t i = 0; i < port_str.size(); ++i) {
                if (port_str[i] < '0' || port_str[i] > '9') {
                    is_valid_port = false;
                    break;
                }
            }

            if (!is_valid_port) {
                return false; // 端口号包含非数字字符
            }

            // 手动转换为整数(不抛异常)
            port = 0;
            for (size_t i = 0; i < port_str.size(); ++i) {
                int digit = port_str[i] - '0';
                // 检查溢出: port * 10 + digit <= 65535
                if (port > 6553 || (port == 6553 && digit > 5)) {
                    return false; // 端口号超出范围
                }
                port = port * 10 + digit;
            }

            // 验证端口范围
            if (port < 1 || port > 65535) {
                return false;
            }
        } else {
            address = url.substr(pos, (slash == std::string::npos) ? url.size() - pos : slash - pos);
            port = ssl ? NCSDK_DEFAULT_HTTPS_PORT : NCSDK_DEFAULT_HTTP_PORT;
        }
        if (slash != std::string::npos) {
            path = url.substr(slash);
        } else {
            path = "/";
        }
        return true;
    }

    CppWebSocket *parent_;
    std::atomic<struct lws_context *> context;
    struct lws *wsi;
    std::atomic<WebSocketState> state_; // WebSocket 连接状态
    std::string url;
    std::map<std::string, std::string> auth_headers_;
    std::string ws_subprotocol_header_;
    std::mutex auth_headers_mutex_;
    WssTlsConfig tls_config_; // TLS 验证配置(挂载到 lws context user)
    std::deque<QueuedMessage> sendQueue;
    std::mutex sendMutex;
    std::thread service_thread; // 服务线程
    std::string receiveBuffer;  // 用于累积多帧消息的缓冲区
    std::mutex receiveMutex;    // 保护接收缓冲区的互斥锁
#ifdef __APPLE__
    std::atomic<bool> writeablePending{
            false}; // macOS: 是否有待处理的writeable事件
    std::chrono::steady_clock::time_point lastWriteableRequest;
#else
    std::chrono::steady_clock::time_point
            lastQueueHealthCheck_; // 非 macOS: 上次队列健康检查时间
    uint64_t writeableCallbackCount_{
            0}; // WRITEABLE 实际触发次数(仅 IO 线程访问, 无需同步)
    uint64_t lastWriteableCount_{0}; // 上次健康检查时的 WRITEABLE 计数快照
    int consecutiveStuckChecks_{0};  // 连续无 WRITEABLE 进展的健康检查次数
#endif
    // 修复: 跨线程写请求标志(全平台)。
    // send()(主线程)通过此标志通知 serviceLoop(IO 线程)调用
    // lws_callback_on_writable, 避免主线程直接调用导致的 lws 内部 pollfd data
    // race。
    std::atomic<bool> pendingWriteRequest_{false};
    // force_close / 非正常退出时 callOnClose() 投递去重标志。
    // 问题: lws v2.4.2 不保证 lws_context_destroy 对已建立连接同步触发
    //   LWS_CALLBACK_CLOSED；若不触发, 上层永远收不到重连信号。
    // 方案: serviceLoop 清理段与 LWS_CALLBACK_CLOSED 均通过 exchange() 原子抢占, 
    //   仅首个抢到的一方投递 callOnClose(), 彻底消除双重调用和漏调用。
    // connect() 时重置, 确保每次新连接从干净状态开始。
    std::atomic<bool> close_notified_{false};
    // SSL_pending() 读节流计数器(IO 线程独占, 无需同步)。
    // 每收 NCSDK_RECV_THROTTLE_N 条消息调用一次 lws_cancel_service(),
    // 强制打断 SSL 读内循环, 使 lws 回到 poll() 顶层, 让已置位的 POLLOUT
    // 得以调度。
    uint64_t recvCount_{0};
};

CppWebSocket::CppWebSocket() : pImpl(new Impl(this)) {}

CppWebSocket::~CppWebSocket() {
    if (pImpl) {
        LOG_INFO("CppWebSocket: 析构函数被调用");
        pImpl->close();
        pImpl.reset();
    }
}

void CppWebSocket::connect(const std::string &url) {
    LOG_DEBUG("CppWebSocket: 连接 %s", url.c_str());
    pImpl->connect(url);
}

void CppWebSocket::setAuthHeaders(const std::map<std::string, std::string> &headers) {
    LOG_INFO("CppWebSocket: setAuthHeaders, size=%zu", headers.size());
    pImpl->setAuthHeaders(headers);
}

void CppWebSocket::setTlsConfig(bool verify_ssl, const std::string &ca_path) {
    pImpl->setTlsConfig(verify_ssl, ca_path);
}

void CppWebSocket::send(const std::string &message) {
    LOG_DEBUG("CppWebSocket: 发送 %s", message.c_str());
    pImpl->send(message);
}

void CppWebSocket::close() {
    LOG_DEBUG("CppWebSocket: 关闭连接");
    pImpl->close();
}

bool CppWebSocket::isOpen() const { return pImpl->isOpen(); }

#else // !ENABLE_LIBWEBSOCKETS

CppWebSocket::CppWebSocket() : pImpl(nullptr) {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
}

CppWebSocket::~CppWebSocket() {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
    pImpl = nullptr;
}

void CppWebSocket::connect(const std::string &) {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
}

void CppWebSocket::setAuthHeaders(const std::map<std::string, std::string> &) {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
}

void CppWebSocket::setTlsConfig(bool, const std::string &) {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
}

void CppWebSocket::send(const std::string &) {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
}

void CppWebSocket::close() {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
}

bool CppWebSocket::isOpen() const {
    LOG_ERROR("CppWebSocket: ENABLE_LIBWEBSOCKETS == false");
    return false;
}

#endif // ENABLE_LIBWEBSOCKETS
