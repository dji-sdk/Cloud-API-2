#include <plog/Log.h>
#include <plog/Initializers/RollingFileInitializer.h>
#include <iostream>

int main() {
    // 初始化 plog，输出到控制台和文件
    plog::init(plog::debug, "plog_test.log", 1000000, 5);
    
    // 测试不同级别的日志
    PLOGD << "这是一条调试日志";
    PLOGI << "这是一条信息日志";
    PLOGW << "这是一条警告日志";
    PLOGE << "这是一条错误日志";
    
    // 测试带参数的日志
    int value = 42;
    PLOGI << "测试值: " << value;
    
    // 测试字符串拼接
    std::string message = "测试字符串拼接";
    PLOGI << message << " - 成功！";
    
    std::cout << "plog 接口库测试完成！请查看 plog_test.log 文件。" << std::endl;
    
    return 0;
}
