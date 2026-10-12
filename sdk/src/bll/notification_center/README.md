# NotificationCenter 设计文档

## 概述

`NotificationCenter` 是 FlightHub SDK 的通知中心管理类，负责管理 WebSocket 连接、处理连接状态变化，并提供自动重连机制。采用单例模式，确保全局只有一个实例。

## 核心功能

### 1. 单例模式

```cpp
class NotificationCenter : public drc_sdk::common::AsyncCaptureProtect {
public:
    static NotificationCenter* getInstance();
    
private:
    NotificationCenter();
    ~NotificationCenter();
    NotificationCenter(const NotificationCenter&) = delete;
    NotificationCenter& operator=(const NotificationCenter&) = delete;
};
```

- 使用静态局部变量实现线程安全的单例
- 禁止拷贝和赋值，确保唯一性

### 2. WebSocket 连接管理

#### 初始化流程

```cpp
bool initialize(const std::string& log_path, int log_level, bool log_to_console, const std::string& websocket_url) {
    // 1. 检查是否已初始化
    if (is_initialized_.load()) {
        return false;
    }
    
    // 2. 初始化底层 SDK
    sdk_manager_->init(init_info);
    
    // 3. 创建并设置 WebSocket 事件监听器
    websocket_listener_ = std::make_shared<InternalWebsocketEventListener>(this);
    sdk_manager_->setWebsocketEventListener(websocket_listener_);
    
    // 4. 连接到 WebSocket 服务器
    sdk_manager_->connect(websocket_url_);
    
    is_initialized_.store(true);
    return true;
}
```

#### 内部事件监听器

`InternalWebsocketEventListener` 监听 WebSocket 的三种事件：

```cpp
class NotificationCenter::InternalWebsocketEventListener 
    : public notification_center::WebsocketEvent, 
      public drc_sdk::common::AsyncCaptureProtect {
    
    void OnOpen() override {
        // 连接建立成功
        notification_center_->is_connected_.store(true);
        notification_center_->notifyConnectionCallbacks();
    }
    
    void OnClose() override {
        // 连接关闭
        notification_center_->is_connected_.store(false);
        notification_center_->notifyConnectionCallbacks();
        notification_center_->scheduleReconnect();  // 调度重连
    }
    
    void OnError(const std::string& error) override {
        // 连接错误
        notification_center_->is_connected_.store(false);
        notification_center_->notifyConnectionCallbacks();
        notification_center_->scheduleReconnect();  // 调度重连
    }
};
```

## 核心机制：防重复重连

### 问题背景

在之前的实现中，当网络频繁断开时会出现以下问题：

1. **一次连接失败可能触发多个错误回调**（`OnError` 和 `OnClose` 可能同时触发）
2. **连接失败很快**（几毫秒内），在 1 秒延迟任务执行前，新的连接尝试又失败了
3. **没有防重复机制**，每次错误都会提交新的重连任务
4. **结果**：短时间内产生大量重连任务，填满线程池队列，导致崩溃

### 解决方案

引入**防重复重连机制**，确保同一时间只有一个重连任务在队列中。

#### 关键数据结构

```cpp
class NotificationCenter {
private:
    // 重连任务管理：防止重复提交重连任务
    std::atomic<bool> reconnect_task_pending_;  // 标记是否有重连任务在队列中
    mutable std::mutex reconnect_mutex_;          // 保护重连任务的提交
};
```

#### 核心方法：scheduleReconnect()

```cpp
void NotificationCenter::scheduleReconnect() {
    // 1. 使用锁保护，确保原子性检查
    std::lock_guard<std::mutex> lock(reconnect_mutex_);
    
    // 2. 如果已经有重连任务在队列中，不再提交新的任务
    if (reconnect_task_pending_.load()) {
        LOG_INFO("[NotificationCenter] 重连任务已在队列中，跳过本次重连请求");
        return;  // 直接返回，避免重复提交
    }
    
    // 3. 标记重连任务已提交
    reconnect_task_pending_.store(true);
    
    // 4. 使用 WeakDummy 保护异步回调中的 this 指针
    WeakDummy(weak_ptr);
    
    // 5. 提交延迟任务（1 秒后执行）
    drc_sdk::common::Executors::Main()->PostDelay([this, weak_ptr]()->void {
        WeakDummyReturn(weak_ptr);  // 检查对象是否还存在
        
        // 6. 清除重连任务标记（允许下次失败时提交新的重连任务）
        reconnect_task_pending_.store(false);
        
        // 7. 执行重连
        reconnect();
    }, std::chrono::milliseconds(kWebsocketRetryInterval));  // 1000ms
    
    LOG_INFO("[NotificationCenter] 已调度重连任务，将在 %d ms 后执行", kWebsocketRetryInterval);
}
```

#### 执行流程

```
时间线示例（连接频繁失败的情况）：

T0 (0ms):     连接失败
              ├─ OnError() 触发
              ├─ scheduleReconnect() 被调用
              ├─ reconnect_task_pending_ = false，标记为 true
              └─ 提交延迟任务（1秒后执行）
              
T0+10ms:      连接又失败（很快失败）
              ├─ OnError() 再次触发
              ├─ scheduleReconnect() 被调用
              ├─ reconnect_task_pending_ = true（已有任务在队列）
              └─ 直接返回，跳过本次请求 ✅
              
T0+20ms:      连接又失败
              ├─ OnError() 再次触发
              ├─ scheduleReconnect() 被调用
              ├─ reconnect_task_pending_ = true（仍有任务在队列）
              └─ 直接返回，跳过本次请求 ✅
              
T0+1000ms:    延迟任务执行
              ├─ reconnect_task_pending_ = false（清除标记）
              ├─ reconnect() 被调用
              └─ 尝试连接
              
T0+1010ms:    如果连接又失败
              ├─ OnError() 触发
              ├─ scheduleReconnect() 被调用
              ├─ reconnect_task_pending_ = false（已清除）
              ├─ 标记为 true，提交新的延迟任务
              └─ 循环继续...
```

### 关键保护机制

1. **原子变量 + 互斥锁**：确保检查与标记的原子性
   ```cpp
   std::lock_guard<std::mutex> lock(reconnect_mutex_);
   if (reconnect_task_pending_.load()) {
       return;  // 已有任务，跳过
   }
   reconnect_task_pending_.store(true);
   ```

2. **WeakDummy 保护**：防止对象销毁后还在使用
   ```cpp
   WeakDummy(weak_ptr);
   PostDelay([this, weak_ptr]()->void {
       WeakDummyReturn(weak_ptr);  // 如果对象已销毁，直接返回
       // ...
   });
   ```

3. **任务执行后立即清除标记**：允许下次失败时提交新的重连任务

## 连接状态回调管理

### 注册回调

```cpp
uint64_t setConnectionCallback(const std::function<void(bool connected)>& callback) {
    uint64_t callback_id;
    bool current_connected;
    
    {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        callback_id = next_callback_id_.fetch_add(1);
        connection_callbacks_[callback_id] = callback;
        current_connected = is_connected_.load();
    }
    
    // 在锁外立即回调当前连接状态
    callback(current_connected);
    
    return callback_id;
}
```

### 通知所有回调

```cpp
void notifyConnectionCallbacks() {
    // 1. 先复制回调列表，避免在持有锁时调用回调导致死锁
    std::unordered_map<uint64_t, std::function<void(bool)>> callbacks_copy;
    
    {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        callbacks_copy = connection_callbacks_;
    }
    
    // 2. 获取当前连接状态
    bool connected = is_connected_.load();
    
    // 3. 在锁外调用回调（避免死锁）
    for (const auto& [id, callback] : callbacks_copy) {
        callback(connected);
    }
}
```

**设计要点**：
- 在锁外调用回调，避免死锁
- 先复制回调列表，避免回调中修改列表导致的问题

## 完整状态转换图

```
初始化
  │
  ├─ initialize()
  │   ├─ 创建 InternalWebsocketEventListener
  │   ├─ 设置事件监听器
  │   └─ connect() 尝试连接
  │
  ├─ OnOpen() ──────────────┐
  │   ├─ is_connected_ = true │
  │   └─ notifyConnectionCallbacks() ──┐
  │                                     │
  ├─ OnError() ─────────────┐           │
  │   ├─ is_connected_ = false          │
  │   ├─ notifyConnectionCallbacks() ──┤
  │   └─ scheduleReconnect()            │
  │       ├─ 检查 reconnect_task_pending_│
  │       ├─ 如果已有任务，跳过          │
  │       └─ 否则提交延迟任务（1秒后）   │
  │                                     │
  ├─ OnClose() ────────────┐            │
  │   ├─ is_connected_ = false          │
  │   ├─ notifyConnectionCallbacks() ──┤
  │   └─ scheduleReconnect()            │
  │                                     │
  └─ 延迟任务执行 ──────────┐           │
      ├─ reconnect_task_pending_ = false│
      └─ reconnect() ───────────────────┘
          └─ connect() 再次尝试连接
```

## 关键配置

```cpp
constexpr auto kWebsocketRetryInterval = 1000; // websocket 断连重连间隔 ms
```

- **重连间隔**：1 秒（1000 毫秒）
- **防重复机制**：确保同一时间只有一个重连任务
- **线程安全**：使用原子变量和互斥锁保护

## 使用示例

```cpp
// 1. 获取单例
auto* notification_center = NotificationCenter::getInstance();

// 2. 初始化
notification_center->initialize(log_path, log_level, log_to_console, websocket_url);

// 3. 注册连接状态回调
uint64_t callback_id = notification_center->setConnectionCallback([](bool connected) {
    if (connected) {
        std::cout << "WebSocket 已连接" << std::endl;
    } else {
        std::cout << "WebSocket 已断开" << std::endl;
    }
});

// 4. 获取业务管理器
auto business_manager = notification_center->getBusinessManager();

// 5. 清理（可选）
notification_center->removeConnectionCallback(callback_id);
notification_center->cleanup();
```

## 总结

### 核心优势

1. **防重复重连**：即使频繁失败，也严格按照 1 秒间隔重连，不会产生大量任务
2. **线程安全**：使用原子变量和互斥锁，支持多线程环境
3. **资源保护**：使用 WeakDummy 防止对象销毁后还在使用
4. **避免死锁**：在锁外调用回调，避免死锁问题

### 解决的问题

- ✅ 防止线程池队列被填满导致崩溃
- ✅ 确保重连间隔严格为 1 秒
- ✅ 避免重复提交重连任务
- ✅ 线程安全的状态管理

