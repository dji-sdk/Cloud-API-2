#include <atomic>
#include <chrono>
#include <future>
#include <iostream>
#include <thread>
#include <common/executors/executors.h>

// 模拟网络线程在关闭过程中返回并投递回调；不需要真实底座。
int main() {
    using drc_sdk::common::Executors;
    Executors::Init();
    std::promise<void> io_started, release_io, shutdown_started;
    auto release = release_io.get_future().share();
    std::atomic<bool> posted{false};
    Executors::IO()->Post([&] {
        io_started.set_value();
        release.wait();
        auto main = Executors::Main();
        if (main) posted = main->Post([] {}) >= 0;
    });
    io_started.get_future().wait();
    std::thread shutdown([&] { shutdown_started.set_value(); Executors::Shutdown(); });
    shutdown_started.get_future().wait();
    // 让关闭线程进入 Stop，重现旧顺序先释放 Main 的窗口。
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    release_io.set_value();
    shutdown.join();
    if (!posted) {
        std::cerr << "Main executor stopped before an in-flight IO callback returned\n";
        return 1;
    }
    return 0;
}
