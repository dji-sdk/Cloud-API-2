// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "blocking_detector.h"
#include <unistd.h>
#include <dlfcn.h>
#include <cxxabi.h>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unwind.h>
#include <sstream>
#include <iomanip>
#include <fstream>
#include <errno.h>
#if defined(__linux__)
#include <dirent.h>
#include <sys/syscall.h>
#endif

namespace blocking_detector {

// 静态实例指针
BlockingDetector* BlockingDetector::instance_ = nullptr;

// 堆栈回溯状态
struct BacktraceState {
    void** current;
    void** end;
};

// Unwind 回调函数
static _Unwind_Reason_Code UnwindCallback(_Unwind_Context* context, void* arg) {
    BacktraceState* state = static_cast<BacktraceState*>(arg);
    uintptr_t pc = _Unwind_GetIP(context);
    
    if (pc) {
        if (state->current == state->end) {
            return _URC_END_OF_STACK;
        }
        *state->current++ = reinterpret_cast<void*>(pc);
    }
    
    return _URC_NO_REASON;
}

BlockingDetector::BlockingDetector(TaskStatistics* stats, const BlockingDetectorConfig& config)
    : stats_(stats), config_(config) {
    instance_ = this;
    stopped_ = true;  // 初始状态为停止
    last_check_time_ = std::chrono::steady_clock::now();
    memset(stack_buffer_, 0, sizeof(stack_buffer_));
    memset(frame_buffer_, 0, sizeof(frame_buffer_));
}

BlockingDetector::~BlockingDetector() {
    Stop();
    if (alt_stack_ != nullptr) {
        munmap(alt_stack_, SIGSTKSZ);
        alt_stack_ = nullptr;
    }
    if (instance_ == this) {
        instance_ = nullptr;
    }
}

bool BlockingDetector::Init() {
    fprintf(stderr, "[BlockingDetector] 开始初始化...\n");
    
    // 初始化信号栈
    if (!InitSignalStack()) {
        fprintf(stderr, "[BlockingDetector] 初始化信号栈失败\n");
        return false;
    }
    fprintf(stderr, "[BlockingDetector] 信号栈初始化成功\n");
    
    // 注册信号处理
    if (!RegisterSignalHandler()) {
        fprintf(stderr, "[BlockingDetector] 注册信号处理失败\n");
        return false;
    }
    fprintf(stderr, "[BlockingDetector] 信号处理注册成功\n");
    
    fprintf(stderr, "[BlockingDetector] 初始化完成\n");
    return true;
}

void BlockingDetector::Start() {
    if (!stopped_.load()) {
        fprintf(stderr, "[BlockingDetector] 警告: 检测器已经在运行\n");
        return;  // 已经启动
    }
    
    fprintf(stderr, "[BlockingDetector] 启动监控线程...\n");
    stopped_ = false;
    monitor_thread_ = std::thread(&BlockingDetector::CheckLoop, this);
    fprintf(stderr, "[BlockingDetector] 监控线程已启动\n");
}

void BlockingDetector::Stop() {
    if (stopped_.exchange(true)) {
        return;  // 已经停止
    }
    
    if (monitor_thread_.joinable()) {
        monitor_thread_.join();
    }
}

void BlockingDetector::TriggerStackCapture() {
    SendStackCaptureSignal();
}

void BlockingDetector::CheckLoop() {
    fprintf(stderr, "[BlockingDetector] CheckLoop 开始运行，检测间隔: %d ms\n", config_.check_interval_ms);
    
    while (!stopped_.load()) {
        CheckBlocking();
        
        // 等待检测间隔
        std::this_thread::sleep_for(std::chrono::milliseconds(config_.check_interval_ms));
    }
    
    fprintf(stderr, "[BlockingDetector] CheckLoop 退出\n");
}

void BlockingDetector::CheckBlocking() {
    auto now = std::chrono::steady_clock::now();
    auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(
        now - last_check_time_);
    
    if (elapsed.count() < config_.check_interval_ms) {
        return;
    }
    
    last_check_time_ = now;
    
    int post_count = stats_->GetPostedTaskCount();
    int exec_count = stats_->GetExecutedTaskCount();
    int wait_size = post_count - exec_count;
    int post_speed = post_count - last_post_count_;
    int exec_speed = exec_count - last_exec_count_;
    
    // 调试日志：每次检查都输出状态
    fprintf(stderr, "[BlockingDetector] 检查状态: 待执行=%d, 已提交=%d, 已执行=%d, 提交速度=%d, 执行速度=%d\n",
            wait_size, post_count, exec_count, post_speed, exec_speed);
    
    bool abnormal = false;
    
    // 情况1：待执行任务数量超过阈值
    if (wait_size > config_.max_wait_count) {
        fprintf(stderr, "[BlockingDetector] 触发条件1: 待执行任务数(%d) > 最大阈值(%d)\n", 
                wait_size, config_.max_wait_count);
        abnormal = true;
    }
    
    // 情况2：上一个任务超过检测间隔未完成，且待执行任务数量超过阻塞阈值
    if (exec_speed == 0 && wait_size > config_.block_wait_count) {
        fprintf(stderr, "[BlockingDetector] 触发条件2: 执行速度=0 且 待执行任务数(%d) > 阻塞阈值(%d)\n", 
                wait_size, config_.block_wait_count);
        abnormal = true;
    }
    
    if (abnormal) {
        OnBlockingDetected(wait_size, post_speed, exec_speed);
    } else {
        // 未检测到阻塞，清零堆栈指纹，下次同一处或别处阻塞会重新上报
        last_reported_stack_hash_.store(0, std::memory_order_relaxed);
    }
    
    last_post_count_ = post_count;
    last_exec_count_ = exec_count;
}

void BlockingDetector::OnBlockingDetected(int wait_size, int post_speed, int exec_speed) {
    fprintf(stderr, "[BlockingDetector] 检测到线程阻塞！\n");
    fprintf(stderr, "  待执行任务数: %d\n", wait_size);
    fprintf(stderr, "  任务提交速度: %d\n", post_speed);
    fprintf(stderr, "  任务执行速度: %d\n", exec_speed);
    
#if defined(__linux__)
    if (config_.dump_all_threads) {
        DumpAllThreadsToFile(wait_size, post_speed, exec_speed);
    } else
#endif
    {
        SendStackCaptureSignal();
    }
    
    if (config_.abort_on_blocking) {
        // 等待一小段时间让堆栈信息保存完成
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        abort();
    }
}

void BlockingDetector::SendStackCaptureSignal() {
    pthread_t tid = stats_->GetTargetThreadId();
    fprintf(stderr, "[BlockingDetector] 准备发送 SIGUSR1 信号，目标线程 ID: %lu\n", (unsigned long)tid);
    
    if (tid != 0) {
        int ret = pthread_kill(tid, SIGUSR1);
        if (ret == 0) {
            fprintf(stderr, "[BlockingDetector] SIGUSR1 信号已发送成功\n");
        } else {
            fprintf(stderr, "[BlockingDetector] 发送 SIGUSR1 信号失败，错误码: %d (%s)\n", 
                    ret, strerror(ret));
        }
    } else {
        fprintf(stderr, "[BlockingDetector] 目标线程 ID 为 0，无法发送信号\n");
    }
}

#if defined(__linux__)
void BlockingDetector::DumpAllThreadsToFile(int wait_size, int post_speed, int exec_speed) {
    const std::string& crash_dir = config_.crash_dir;
    struct stat st;
    if (stat(crash_dir.c_str(), &st) != 0) {
        mkdir(crash_dir.c_str(), 0755);
    }
    char filename[512];
    time_t now = time(nullptr);
    int n = snprintf(filename, sizeof(filename), "%s/stack-trace-all-%ld.log", crash_dir.c_str(), (long)now);
    if (n <= 0 || n >= (int)sizeof(filename)) {
        fprintf(stderr, "[BlockingDetector] DumpAllThreadsToFile: 无法生成文件名\n");
        return;
    }
    int fd = open(filename, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) {
        fprintf(stderr, "[BlockingDetector] DumpAllThreadsToFile: 无法创建文件 %s\n", filename);
        return;
    }
    char header[640];
    snprintf(header, sizeof(header),
             "线程阻塞检测报告（全线程转储）\n时间: %ld\n待执行: %d 提交速度: %d 执行速度: %d\n"
             "说明: 下方带 [Monitored/Main] 的线程即触发本次检测的被监控线程（Main 执行器）。\n\n",
             (long)now, wait_size, post_speed, exec_speed);
    (void)write(fd, header, strlen(header));
    multi_dump_fd_ = fd;

    DIR* dir = opendir("/proc/self/task");
    if (!dir) {
        fprintf(stderr, "[BlockingDetector] DumpAllThreadsToFile: 无法打开 /proc/self/task\n");
        multi_dump_fd_ = -1;
        close(fd);
        return;
    }
    struct dirent* ent;
    while ((ent = readdir(dir)) != nullptr) {
        if (ent->d_name[0] == '.') continue;
        char* end = nullptr;
        long tid = strtol(ent->d_name, &end, 10);
        if (end == ent->d_name || *end != '\0' || tid <= 0) continue;
        int r = static_cast<int>(syscall(SYS_tgkill, getpid(), tid, SIGUSR1));
        if (r == 0) {
            std::this_thread::sleep_for(std::chrono::milliseconds(200));
        }
    }
    closedir(dir);
    {
        const char* tip = "\n提示: 使用 addr2line -e <可执行文件> -f -C -p <地址> 解析各线程堆栈。\n";
        size_t len = 0;
        while (tip[len] != '\0') len++;
        (void)write(fd, tip, len);
    }
    multi_dump_fd_ = -1;
    fsync(fd);
    close(fd);
    fprintf(stderr, "[BlockingDetector] 全线程堆栈已写入: %s\n", filename);
}
#endif

// 辅助函数：使用 write() 写入字符串（异步信号安全）
static void safe_write(int fd, const char* str) {
    if (fd >= 0 && str != nullptr) {
        size_t len = 0;
        while (str[len] != '\0') len++;
        write(fd, str, len);
    }
}

// 辅助函数：从路径提取文件名（异步信号安全）
static const char* safe_get_filename(const char* path) {
    if (path == nullptr) return "";
    const char* filename = strrchr(path, '/');
    return filename ? (filename + 1) : path;
}

// 辅助函数：格式化并写入堆栈帧详细信息（异步信号安全）
static void safe_write_stack_frame(int fd, int index, void* addr) {
    char buffer[512];
    Dl_info info;
    
    // 使用 dladdr 获取符号信息（异步信号安全）
    if (dladdr(addr, &info) != 0) {
        const char* lib_name = safe_get_filename(info.dli_fname ? info.dli_fname : "");
        uintptr_t offset = (uintptr_t)addr - (uintptr_t)info.dli_fbase;
        
        if (info.dli_sname != nullptr) {
            // 有符号名
            uintptr_t symbol_offset = 0;
            if (info.dli_saddr != nullptr) {
                symbol_offset = (uintptr_t)addr - (uintptr_t)info.dli_saddr;
            }
            
            int n = snprintf(buffer, sizeof(buffer), 
                           "  #%02d: 0x%016lx <- %s [%s+0x%lx]\n",
                           index, (uintptr_t)addr, lib_name, info.dli_sname, symbol_offset);
            if (n > 0 && n < (int)sizeof(buffer)) {
                write(fd, buffer, n);
            }
        } else {
            // 无符号名，只显示库和偏移
            int n = snprintf(buffer, sizeof(buffer),
                           "  #%02d: 0x%016lx <- %s+0x%lx [无符号信息]\n",
                           index, (uintptr_t)addr, lib_name, offset);
            if (n > 0 && n < (int)sizeof(buffer)) {
                write(fd, buffer, n);
            }
        }
    } else {
        // dladdr 失败，只输出地址
        int n = snprintf(buffer, sizeof(buffer), "  #%02d: %p [无法解析]\n", index, addr);
        if (n > 0 && n < (int)sizeof(buffer)) {
            write(fd, buffer, n);
        }
    }
}

void BlockingDetector::StackDumpHandler(int sig) {
    if (instance_ == nullptr) return;
    const int errfd = STDERR_FILENO;

#if defined(__linux__)
    // 全线程转储模式：仅追加当前线程堆栈到已打开的文件，不重复打印、不创建新文件
    int mfd = instance_->multi_dump_fd_;
    if (mfd >= 0) {
        long tid = syscall(SYS_gettid);
        pthread_t self = pthread_self();
        pthread_t monitored = instance_->stats_ ? instance_->stats_->GetTargetThreadId() : static_cast<pthread_t>(0);
        int is_main = pthread_equal(self, monitored) ? 1 : 0;
        char line[160];
        int n = snprintf(line, sizeof(line), "\n=== Thread (tid=%ld)%s ===\n",
                        tid, is_main ? " [Monitored/Main]" : "");
        if (n > 0 && n < (int)sizeof(line)) (void)write(mfd, line, (size_t)n);
        int depth = instance_->CaptureStack(instance_->stack_buffer_, instance_->config_.stack_depth);
        for (int i = 0; i < depth; i++) {
            safe_write_stack_frame(mfd, i, instance_->stack_buffer_[i]);
        }
        return;
    }
#endif

    {
        char logbuf[128];
        int m = snprintf(logbuf, sizeof(logbuf), "[BlockingDetector] StackDumpHandler 被调用，信号: %d\n", sig);
        if (m > 0 && m < (int)sizeof(logbuf)) (void)write(errfd, logbuf, (size_t)m);
    }
    safe_write(errfd, "[BlockingDetector] 开始捕获堆栈...\n");
    int depth = instance_->CaptureStack(instance_->stack_buffer_, instance_->config_.stack_depth);
    {
        char logbuf[128];
        int m = snprintf(logbuf, sizeof(logbuf), "[BlockingDetector] 堆栈捕获完成，深度: %d\n", depth);
        if (m > 0 && m < (int)sizeof(logbuf)) (void)write(errfd, logbuf, (size_t)m);
    }
    
    if (depth == 0) {
        safe_write(errfd, "[BlockingDetector] 警告: 堆栈深度为 0，无法继续处理\n");
        return;
    }
    
    // 计算堆栈指纹（仅用 PC，异步信号安全）
    uint64_t hash = 0;
    for (int i = 0; i < depth; i++) {
        hash = hash * 31 + static_cast<uint64_t>(reinterpret_cast<uintptr_t>(instance_->stack_buffer_[i]));
    }
    if (hash == 0) hash = 1;  // 避免与“未上报”的 0 冲突
    uint64_t prev = instance_->last_reported_stack_hash_.load(std::memory_order_relaxed);
    if (prev == hash) {
        safe_write(errfd, "[BlockingDetector] 与上次堆栈相同，跳过重复输出\n");
        return;
    }
    
    // 输出到 stderr（用于调试，包含符号信息，输出全部捕获帧）
    safe_write(errfd, "[BlockingDetector] ========================>>> 线程阻塞堆栈信息 <<<========================\n");
    for (int i = 0; i < depth; i++) {
        safe_write_stack_frame(errfd, i, instance_->stack_buffer_[i]);
    }
    safe_write(errfd, "[BlockingDetector] ==================================================================\n");
    
    // 保存到文件（使用异步信号安全的系统调用）
    const char* crash_dir = instance_->config_.crash_dir.c_str();
    
    // 创建目录（如果不存在）
    struct stat st;
    if (stat(crash_dir, &st) != 0) {
        // mkdir 不是异步信号安全的，但通常可以工作
        // 如果失败，我们继续尝试写入文件
        mkdir(crash_dir, 0755);
    }
    
    // 创建文件名
    char filename[512];
    time_t now = time(nullptr);  // time() 是异步信号安全的
    int n = snprintf(filename, sizeof(filename), "%s/stack-trace-%ld.log", 
                     crash_dir, now);
    if (n <= 0 || n >= (int)sizeof(filename)) {
        safe_write(errfd, "[BlockingDetector] 无法生成文件名\n");
        return;
    }
    
    // 打开文件（open() 是异步信号安全的）
    int fd = open(filename, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) {
        safe_write(errfd, "[BlockingDetector] 无法创建文件\n");
        return;
    }
    
    // 写入文件头（使用 write() 系统调用，异步信号安全）
    safe_write(fd, "线程阻塞检测报告\n");
    char time_str[64];
    snprintf(time_str, sizeof(time_str), "时间: %ld\n", now);
    safe_write(fd, time_str);
    
    // 写入任务统计信息
    if (instance_->stats_ != nullptr) {
        int post_count = instance_->stats_->GetPostedTaskCount();
        int exec_count = instance_->stats_->GetExecutedTaskCount();
        int wait_count = post_count - exec_count;
        
        char stats_buf[256];
        snprintf(stats_buf, sizeof(stats_buf), 
                "待执行任务数: %d\n已提交任务数: %d\n已执行任务数: %d\n",
                wait_count, post_count, exec_count);
        safe_write(fd, stats_buf);
    }
    
    safe_write(fd, "\n");
    safe_write(fd, "========================>>> 线程阻塞堆栈信息 <<<========================\n");
    safe_write(fd, "堆栈详细信息（包含函数名和库信息）:\n");
    
    // 写入堆栈详细信息（全部捕获帧，不截断）
    for (int i = 0; i < depth; i++) {
        safe_write_stack_frame(fd, i, instance_->stack_buffer_[i]);
    }
    
    safe_write(fd, "==================================================================\n");
    safe_write(fd, "\n提示: 使用以下命令可以解析堆栈地址获取源码位置:\n");
    safe_write(fd, "  addr2line -e <可执行文件路径> -f -C -p <地址>\n");
    safe_write(fd, "\n示例:\n");
    safe_write(fd, "  addr2line -e ./blocking_detector_demo -f -C -p 0x4094e4\n");
    safe_write(fd, "\n注意: 如果显示 \"无符号信息\"，说明该地址没有导出符号。\n");
    safe_write(fd, "      可以使用 addr2line 解析地址获取源码位置（需要 -g 编译选项）。\n");
    safe_write(fd, "      若堆栈在 libjvm.so / JVM_Sleep 附近结束，说明阻塞在 Java 层（如 Thread.sleep），\n");
    safe_write(fd, "      Native 层帧数有限，可配合 jstack <pid> 查看该线程的完整 Java 堆栈。\n");
    
    // 同步并关闭文件
    fsync(fd);  // fsync() 是异步信号安全的
    close(fd);  // close() 是异步信号安全的
    
    instance_->last_reported_stack_hash_.store(hash, std::memory_order_relaxed);
    safe_write(errfd, "[BlockingDetector] 堆栈信息已保存到文件\n");
}

int BlockingDetector::CaptureStack(void** buffer, int max_depth) {
    BacktraceState state = {buffer, buffer + max_depth};
    _Unwind_Backtrace(UnwindCallback, &state);
    return state.current - buffer;
}

int BlockingDetector::ResolveSymbols(void** pc_ptrs, int count, StackFrame* frames) {
    int frame_count = 0;
    
    for (int i = 0; i < count && frame_count < kMaxStackDepth; i++) {
        Dl_info info;
        if (dladdr(pc_ptrs[i], &info) != 0) {
            frames[frame_count].pc = pc_ptrs[i];
            frames[frame_count].offset = (uintptr_t)pc_ptrs[i] - (uintptr_t)info.dli_fbase;
            frames[frame_count].library = info.dli_fname ? std::string(info.dli_fname) : "";
            
            if (info.dli_sname) {
                frames[frame_count].symbol = std::string(info.dli_sname);
                frames[frame_count].demangled_name = DemangleSymbol(info.dli_sname);
            } else {
                frames[frame_count].symbol = "";
                frames[frame_count].demangled_name = "";
            }
            
            frame_count++;
        }
    }
    
    return frame_count;
}

int BlockingDetector::FilterKernelFrames(StackFrame* frames, int count) {
    for (int i = 0; i < count; i++) {
        if (frames[i].symbol == "__kernel_rt_sigreturn" || 
            frames[i].symbol == "StackDumpHandler") {
            return i + 1;  // 返回真实堆栈的起始位置
        }
    }
    return 0;
}

std::string BlockingDetector::FormatStackTrace(StackFrame* frames, int count) {
    std::ostringstream oss;
    oss << "========================>>> 线程阻塞堆栈信息 <<<========================\n";
    oss << "时间: " << std::chrono::duration_cast<std::chrono::seconds>(
        std::chrono::system_clock::now().time_since_epoch()).count() << "\n\n";
    
    for (int i = 0; i < count; i++) {
        const StackFrame& frame = frames[i];
        std::string lib_name = GetFileName(frame.library.c_str());
        
        oss << "#" << std::setw(2) << std::setfill('0') << i << " ";
        oss << "0x" << std::hex << std::setw(16) << std::setfill('0') 
            << (uintptr_t)frame.pc << std::dec << " ";
        oss << "<- " << lib_name;
        
        if (!frame.demangled_name.empty()) {
            oss << " [" << frame.demangled_name;
            if (frame.offset > 0) {
                oss << "+0x" << std::hex << frame.offset << std::dec;
            }
            oss << "]";
        } else if (!frame.symbol.empty()) {
            oss << " [" << frame.symbol;
            if (frame.offset > 0) {
                oss << "+0x" << std::hex << frame.offset << std::dec;
            }
            oss << "]";
        } else {
            // 尝试使用 addr2line 解析
            std::string exe_path = GetExecutablePath();
            if (exe_path.empty()) {
                exe_path = frame.library;
            }
            std::string addr2line_info = ResolveAddressWithAddr2line(frame.pc, exe_path.c_str());
            if (!addr2line_info.empty() && addr2line_info.find("??") == std::string::npos) {
                oss << " [" << addr2line_info << "]";
            } else {
                oss << " [无符号信息+0x" << std::hex << frame.offset << std::dec << "]";
            }
        }
        
        oss << "\n";
    }
    
    oss << "==================================================================\n";
    return oss.str();
}

void BlockingDetector::SaveStackInfo(const std::string& stack_info) {
    fprintf(stderr, "[BlockingDetector] SaveStackInfo 被调用，保存目录: %s\n", config_.crash_dir.c_str());
    
    // 创建目录
    struct stat st;
    if (stat(config_.crash_dir.c_str(), &st) != 0) {
        fprintf(stderr, "[BlockingDetector] 目录不存在，尝试创建: %s\n", config_.crash_dir.c_str());
        if (mkdir(config_.crash_dir.c_str(), 0755) != 0) {
            fprintf(stderr, "[BlockingDetector] 无法创建目录: %s, 错误: %s\n", 
                    config_.crash_dir.c_str(), strerror(errno));
            // 输出到 stderr 作为备用
            fprintf(stderr, "[BlockingDetector] 堆栈信息（输出到 stderr）:\n%s", stack_info.c_str());
            return;
        }
        fprintf(stderr, "[BlockingDetector] 目录创建成功\n");
    } else {
        fprintf(stderr, "[BlockingDetector] 目录已存在\n");
    }
    
    // 创建文件名
    char filename[512];
    time_t now = time(nullptr);
    snprintf(filename, sizeof(filename), "%s/stack-trace-%ld.log", 
             config_.crash_dir.c_str(), now);
    fprintf(stderr, "[BlockingDetector] 目标文件名: %s\n", filename);
    
    // 添加元数据
    std::ostringstream full_info;
    full_info << "线程阻塞检测报告\n";
    full_info << "时间: " << ctime(&now);
    full_info << "待执行任务数: " << (stats_->GetPostedTaskCount() - stats_->GetExecutedTaskCount()) << "\n";
    full_info << "已提交任务数: " << stats_->GetPostedTaskCount() << "\n";
    full_info << "已执行任务数: " << stats_->GetExecutedTaskCount() << "\n";
    full_info << "\n";
    full_info << stack_info;
    
    std::string full_content = full_info.str();
    size_t content_size = full_content.size();
    
    // 使用 mmap 写入文件
    fprintf(stderr, "[BlockingDetector] 打开文件: %s\n", filename);
    int fd = open(filename, O_RDWR | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) {
        fprintf(stderr, "[BlockingDetector] 无法创建文件: %s, 错误: %s\n", 
                filename, strerror(errno));
        fprintf(stderr, "[BlockingDetector] 堆栈信息（输出到 stderr）:\n%s", full_content.c_str());
        return;
    }
    fprintf(stderr, "[BlockingDetector] 文件打开成功，fd=%d\n", fd);
    
    // 扩展文件大小
    fprintf(stderr, "[BlockingDetector] 扩展文件大小到: %zu 字节\n", content_size);
    if (ftruncate(fd, content_size) != 0) {
        fprintf(stderr, "[BlockingDetector] 无法扩展文件大小，错误: %s\n", strerror(errno));
        close(fd);
        fprintf(stderr, "[BlockingDetector] 堆栈信息（输出到 stderr）:\n%s", full_content.c_str());
        return;
    }
    fprintf(stderr, "[BlockingDetector] 文件大小扩展成功\n");
    
    // 使用 mmap 映射
    fprintf(stderr, "[BlockingDetector] 映射文件到内存...\n");
    void* addr = mmap(nullptr, content_size, PROT_WRITE | PROT_READ, 
                      MAP_SHARED, fd, 0);
    if (addr == MAP_FAILED) {
        fprintf(stderr, "[BlockingDetector] mmap 失败，错误: %s\n", strerror(errno));
        close(fd);
        fprintf(stderr, "[BlockingDetector] 堆栈信息（输出到 stderr）:\n%s", full_content.c_str());
        return;
    }
    fprintf(stderr, "[BlockingDetector] mmap 成功，地址: %p\n", addr);
    
    // 复制数据并同步
    fprintf(stderr, "[BlockingDetector] 复制数据到内存映射...\n");
    memcpy(addr, full_content.c_str(), content_size);
    fprintf(stderr, "[BlockingDetector] 同步数据到磁盘...\n");
    msync(addr, content_size, MS_SYNC);
    fprintf(stderr, "[BlockingDetector] 数据同步完成\n");
    
    // 清理
    munmap(addr, content_size);
    close(fd);
    
    fprintf(stderr, "[BlockingDetector] 堆栈信息已成功保存到: %s\n", filename);
    fprintf(stderr, "[BlockingDetector] 堆栈信息内容:\n%s", stack_info.c_str());
}

bool BlockingDetector::InitSignalStack() {
    // 分配信号栈内存（使用 mmap 分配，确保在栈溢出时也能使用）
    alt_stack_ = mmap(nullptr, SIGSTKSZ, PROT_READ | PROT_WRITE, 
                      MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (alt_stack_ == MAP_FAILED) {
        fprintf(stderr, "[BlockingDetector] 无法创建信号栈空间\n");
        return false;
    }
    
    stack_t stack;
    stack.ss_sp = alt_stack_;
    stack.ss_size = SIGSTKSZ;
    stack.ss_flags = 0;
    
    if (sigaltstack(&stack, nullptr) != 0) {
        fprintf(stderr, "[BlockingDetector] 无法设置信号栈\n");
        munmap(alt_stack_, SIGSTKSZ);
        alt_stack_ = nullptr;
        return false;
    }
    
    return true;
}

bool BlockingDetector::RegisterSignalHandler() {
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sigemptyset(&sa.sa_mask);
    sa.sa_handler = StackDumpHandler;
    sa.sa_flags = SA_ONSTACK;  // 使用备用栈
    
    if (sigaction(SIGUSR1, &sa, nullptr) != 0) {
        fprintf(stderr, "[BlockingDetector] 无法注册信号处理器\n");
        return false;
    }
    
    // 取消信号屏蔽
    sigset_t set;
    sigemptyset(&set);
    sigaddset(&set, SIGUSR1);
    pthread_sigmask(SIG_UNBLOCK, &set, nullptr);
    
    return true;
}

std::string BlockingDetector::GetExecutablePath() {
    char exe_path[1024];
    ssize_t len = readlink("/proc/self/exe", exe_path, sizeof(exe_path) - 1);
    if (len > 0) {
        exe_path[len] = '\0';
        return std::string(exe_path);
    }
    return "";
}

std::string BlockingDetector::ResolveAddressWithAddr2line(void* addr, const char* executable_path) {
    if (executable_path == nullptr || executable_path[0] == '\0') {
        return "";
    }
    
    char cmd[512];
    snprintf(cmd, sizeof(cmd), "addr2line -e %s -f -C -p 0x%lx 2>/dev/null", 
             executable_path, (uintptr_t)addr);
    
    FILE* pipe = popen(cmd, "r");
    if (pipe == nullptr) {
        return "";
    }
    
    std::string result;
    char buffer[256];
    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        result += buffer;
    }
    
    pclose(pipe);
    
    // 移除末尾的换行符
    if (!result.empty() && result.back() == '\n') {
        result.pop_back();
    }
    
    return result;
}

std::string BlockingDetector::DemangleSymbol(const char* mangled_name) {
    if (mangled_name == nullptr) {
        return "";
    }
    
    int status = 0;
    char* demangled = abi::__cxa_demangle(mangled_name, nullptr, nullptr, &status);
    
    if (status == 0 && demangled != nullptr) {
        std::string result(demangled);
        free(demangled);
        return result;
    } else {
        return std::string(mangled_name);
    }
}

std::string BlockingDetector::GetFileName(const char* path) {
    if (path == nullptr) {
        return "";
    }
    const char* filename = strrchr(path, '/');
    return filename ? (filename + 1) : path;
}

} // namespace blocking_detector

