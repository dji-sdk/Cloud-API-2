#ifndef DRTC_DEVICE_SDK_FACTORY_H
#define DRTC_DEVICE_SDK_FACTORY_H

#include "drtc_device_sdk.h"


namespace drtc
{
    class DrtcDeviceSdkFactory {
        public:
            // 获取单例实例的静态方法
            static DrtcDeviceSdk *getInstance(const char *config_path);

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