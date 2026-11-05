#ifndef DRTC_DEVICE_SDK_FACTORY_H
#define DRTC_DEVICE_SDK_FACTORY_H

#include "drtc_device_sdk.h"

#include <memory>


namespace drtc
{
    class DrtcDeviceSdkFactory {
        public:
            // 创建一个独立的 SDK 实例。调用方持有返回的 shared_ptr，实例在最后
            // 一个引用释放时析构（先 DrtcDeinit()，它负责关闭子系统并销毁 loop）。
            //
            // 单进程内可以并存多个实例。约束：
            //  - modem_manager.enable = true（DRPC 模式）只支持单实例；
            //    多实例场景请配 enable = false。
            //  - SetLogLevel 等日志接口是进程级全局副作用，最后调用者生效。
            static std::shared_ptr<DrtcDeviceSdk> Create(const char *config_path);

        private:
            // 隐藏构造函数和析构函数
            DrtcDeviceSdkFactory() = default;
            ~DrtcDeviceSdkFactory()= default;
            // 隐藏拷贝构造函数和拷贝赋值运算符
            DrtcDeviceSdkFactory(const DrtcDeviceSdkFactory&) = delete;
            DrtcDeviceSdkFactory& operator=(const DrtcDeviceSdkFactory&) = delete;

    };
}



#endif // DRTC_DEVICE_SDK_FACTORY_H