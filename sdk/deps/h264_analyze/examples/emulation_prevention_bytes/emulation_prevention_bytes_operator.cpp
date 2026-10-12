// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <vector>
#include <cstdint>
#include <string>
#include <sstream>
#include <algorithm>

/**
 * H.264 防竞争码操作类
 * 实现符合H.264标准的防竞争码添加和移除功能
 */
class EmulationPreventionBytesOperator {
public:
    /**
     * 添加防竞争码
     * 在数据中检测到 0x000000、0x000001、0x000002、0x000003 序列时，在倒数第二位插入0x03
     * 符合H.264标准：防止NALU载荷中出现与起始码相同的序列
     * 
     * @param data 原始数据
     * @return 添加防竞争码后的数据
     */
    static std::vector<uint8_t> addEmulationPreventionBytes(const std::vector<uint8_t>& data) {
        std::vector<uint8_t> result;
        result.reserve(data.size() + data.size() / 4); // 预分配空间，最多增加25%

        size_t consecutive_zero_count = 0; // 已写入结果中的连续 0x00 个数

        for (size_t i = 0; i < data.size(); i++) {
            uint8_t current_byte = data[i];

            // 若结果中已连续写入两个 0x00，且当前字节在 [0x00..0x03]，则在其前插入 0x03
            if (consecutive_zero_count >= 2 &&
                (current_byte == 0x00 || current_byte == 0x01 || current_byte == 0x02 || current_byte == 0x03)) {
                result.push_back(0x03);
                consecutive_zero_count = 0; // 插入防竞争码后重置计数
            }

            // 写入当前字节
            result.push_back(current_byte);

            // 更新连续 0x00 计数
            if (current_byte == 0x00) {
                consecutive_zero_count++;
            } else {
                consecutive_zero_count = 0;
            }
        }
        
        return result;
    }
    
    /**
     * 移除防竞争码
     * 检测并移除数据中的防竞争码序列 0x00000300、0x00000301、0x00000302、0x00000303
     * 
     * @param data 包含防竞争码的数据
     * @return 移除防竞争码后的原始数据
     */
    static std::vector<uint8_t> removeEmulationPreventionBytes(const std::vector<uint8_t>& data) {
        std::vector<uint8_t> result;
        result.reserve(data.size()); // 预分配空间
        
        for (size_t i = 0; i < data.size(); i++) {
            // 检查是否遇到防竞争码序列（0x00 0x00 0x03 0x00/01/02/03），若是则跳过 0x03
            if (i >= 2 && (i + 1) < data.size() &&
                data[i] == 0x03 &&
                data[i - 1] == 0x00 &&
                data[i - 2] == 0x00 &&
                (data[i + 1] == 0x00 || data[i + 1] == 0x01 || data[i + 1] == 0x02 || data[i + 1] == 0x03)) {
                continue;
            }
            // 普通字节，直接添加
            result.push_back(data[i]);
        }
        
        return result;
    }
    
    /**
     * 打印十六进制数据
     * @param data 要打印的数据
     * @param title 标题
     * @param maxBytes 最大打印字节数（0表示打印全部）
     */
    static void printHexData(const std::vector<uint8_t>& data, 
                           const std::string& title, 
                           size_t maxBytes = 0) {
        std::cout << title << " (" << data.size() << " 字节):" << std::endl;
        
        size_t printCount = (maxBytes == 0) ? data.size() : std::min(data.size(), maxBytes);
        
        for (size_t i = 0; i < printCount; i++) {
            printf("%02X ", data[i]);
            if ((i + 1) % 16 == 0) {
                std::cout << std::endl;
            }
        }
        
        if (printCount % 16 != 0) {
            std::cout << std::endl;
        }
        
        if (maxBytes > 0 && data.size() > maxBytes) {
            std::cout << "... (还有 " << (data.size() - maxBytes) << " 字节)" << std::endl;
        }
        std::cout << std::endl;
    }
    
    /**
     * 验证数据是否相等
     * @param data1 数据1
     * @param data2 数据2
     * @return 是否相等
     */
    static bool compareData(const std::vector<uint8_t>& data1, const std::vector<uint8_t>& data2) {
        if (data1.size() != data2.size()) {
            return false;
        }
        
        for (size_t i = 0; i < data1.size(); i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        
        return true;
    }
};

/**
 * 测试函数
 */
void runTests() {
    std::cout << "=== H.264 防竞争码操作测试 ===" << std::endl;
    
    // 测试用例1：包含 0x000000 序列
    std::cout << "\n--- 测试用例1：包含 0x000000 序列 ---" << std::endl;
    std::vector<uint8_t> test1 = {0x00, 0x00, 0x00, 0x01, 0x02, 0x03};
    EmulationPreventionBytesOperator::printHexData(test1, "原始数据");
    
    auto result1_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test1);
    EmulationPreventionBytesOperator::printHexData(result1_add, "添加防竞争码后");
    
    auto result1_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result1_add);
    EmulationPreventionBytesOperator::printHexData(result1_remove, "移除防竞争码后");
    
    bool test1_passed = EmulationPreventionBytesOperator::compareData(test1, result1_remove);
    std::cout << "测试用例1结果: " << (test1_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例2：包含 0x000001 序列
    std::cout << "\n--- 测试用例2：包含 0x000001 序列 ---" << std::endl;
    std::vector<uint8_t> test2 = {0x00, 0x00, 0x01, 0x01, 0x02, 0x03};
    EmulationPreventionBytesOperator::printHexData(test2, "原始数据");
    
    auto result2_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test2);
    EmulationPreventionBytesOperator::printHexData(result2_add, "添加防竞争码后");
    
    auto result2_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result2_add);
    EmulationPreventionBytesOperator::printHexData(result2_remove, "移除防竞争码后");
    
    bool test2_passed = EmulationPreventionBytesOperator::compareData(test2, result2_remove);
    std::cout << "测试用例2结果: " << (test2_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例3：包含 0x000002 序列
    std::cout << "\n--- 测试用例3：包含 0x000002 序列 ---" << std::endl;
    std::vector<uint8_t> test3 = {0x00, 0x00, 0x02, 0x01, 0x02, 0x03};
    EmulationPreventionBytesOperator::printHexData(test3, "原始数据");
    
    auto result3_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test3);
    EmulationPreventionBytesOperator::printHexData(result3_add, "添加防竞争码后");
    
    auto result3_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result3_add);
    EmulationPreventionBytesOperator::printHexData(result3_remove, "移除防竞争码后");
    
    bool test3_passed = EmulationPreventionBytesOperator::compareData(test3, result3_remove);
    std::cout << "测试用例3结果: " << (test3_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例4：包含 0x000003 序列
    std::cout << "\n--- 测试用例4：包含 0x000003 序列 ---" << std::endl;
    std::vector<uint8_t> test4 = {0x00, 0x00, 0x03, 0x01, 0x02, 0x03};
    EmulationPreventionBytesOperator::printHexData(test4, "原始数据");
    
    auto result4_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test4);
    EmulationPreventionBytesOperator::printHexData(result4_add, "添加防竞争码后");
    
    auto result4_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result4_add);
    EmulationPreventionBytesOperator::printHexData(result4_remove, "移除防竞争码后");
    
    bool test4_passed = EmulationPreventionBytesOperator::compareData(test4, result4_remove);
    std::cout << "测试用例4结果: " << (test4_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例5：包含起始码的情况（不应添加防竞争码）
    std::cout << "\n--- 测试用例5：包含起始码 0x00000001 ---" << std::endl;
    std::vector<uint8_t> test5 = {0x00, 0x00, 0x00, 0x01, 0x65, 0x00, 0x00, 0x03, 0x01};
    EmulationPreventionBytesOperator::printHexData(test5, "原始数据");
    
    auto result5_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test5);
    EmulationPreventionBytesOperator::printHexData(result5_add, "添加防竞争码后");
    
    auto result5_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result5_add);
    EmulationPreventionBytesOperator::printHexData(result5_remove, "移除防竞争码后");
    
    bool test5_passed = EmulationPreventionBytesOperator::compareData(test5, result5_remove);
    std::cout << "测试用例5结果: " << (test5_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例6：包含多个需要防竞争码的序列
    std::cout << "\n--- 测试用例6：多个防竞争码序列 ---" << std::endl;
    std::vector<uint8_t> test6 = {0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x01, 0x03, 0x00, 0x00, 0x02, 0x04, 0x00, 0x00, 0x03, 0x05};
    EmulationPreventionBytesOperator::printHexData(test6, "原始数据");
    
    auto result6_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test6);
    EmulationPreventionBytesOperator::printHexData(result6_add, "添加防竞争码后");
    
    auto result6_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result6_add);
    EmulationPreventionBytesOperator::printHexData(result6_remove, "移除防竞争码后");
    
    bool test6_passed = EmulationPreventionBytesOperator::compareData(test6, result6_remove);
    std::cout << "测试用例6结果: " << (test6_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例7：不包含需要防竞争码的序列
    std::cout << "\n--- 测试用例7：不包含防竞争码序列 ---" << std::endl;
    std::vector<uint8_t> test7 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
    EmulationPreventionBytesOperator::printHexData(test7, "原始数据");
    
    auto result7_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test7);
    EmulationPreventionBytesOperator::printHexData(result7_add, "添加防竞争码后");
    
    auto result7_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result7_add);
    EmulationPreventionBytesOperator::printHexData(result7_remove, "移除防竞争码后");
    
    bool test7_passed = EmulationPreventionBytesOperator::compareData(test7, result7_remove);
    std::cout << "测试用例7结果: " << (test7_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 测试用例8：空数据
    std::cout << "\n--- 测试用例8：空数据 ---" << std::endl;
    std::vector<uint8_t> test8 = {};
    EmulationPreventionBytesOperator::printHexData(test8, "原始数据");
    
    auto result8_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(test8);
    EmulationPreventionBytesOperator::printHexData(result8_add, "添加防竞争码后");
    
    auto result8_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result8_add);
    EmulationPreventionBytesOperator::printHexData(result8_remove, "移除防竞争码后");
    
    bool test8_passed = EmulationPreventionBytesOperator::compareData(test8, result8_remove);
    std::cout << "测试用例8结果: " << (test8_passed ? "✓ 通过" : "✗ 失败") << std::endl;
    
    // 总结
    std::cout << "\n=== 测试总结 ===" << std::endl;
    int total_tests = 8;
    int passed_tests = (test1_passed ? 1 : 0) + (test2_passed ? 1 : 0) + 
                      (test3_passed ? 1 : 0) + (test4_passed ? 1 : 0) + 
                      (test5_passed ? 1 : 0) + (test6_passed ? 1 : 0) + 
                      (test7_passed ? 1 : 0) + (test8_passed ? 1 : 0);
    
    std::cout << "总测试数: " << total_tests << std::endl;
    std::cout << "通过测试: " << passed_tests << std::endl;
    std::cout << "失败测试: " << (total_tests - passed_tests) << std::endl;
    std::cout << "成功率: " << (passed_tests * 100 / total_tests) << "%" << std::endl;
    
    if (passed_tests == total_tests) {
        std::cout << "🎉 所有测试通过！防竞争码操作函数工作正常。" << std::endl;
    } else {
        std::cout << "❌ 部分测试失败，请检查实现。" << std::endl;
    }
}

int main() {
    std::cout << "H.264 防竞争码操作器演示程序" << std::endl;
    std::cout << "================================" << std::endl;
    
    // 运行测试
    runTests();
    
    // 交互式测试
    std::cout << "\n=== 交互式测试 ===" << std::endl;
    std::cout << "请输入要测试的十六进制数据（用空格分隔，输入空行结束）:" << std::endl;
    
    std::string line;
    while (std::getline(std::cin, line) && !line.empty()) {
        std::vector<uint8_t> input_data;
        std::istringstream iss(line);
        std::string hex_byte;
        
        while (iss >> hex_byte) {
            try {
                uint8_t byte = static_cast<uint8_t>(std::stoul(hex_byte, nullptr, 16));
                input_data.push_back(byte);
            } catch (const std::exception& e) {
                std::cout << "无效的十六进制数据: " << hex_byte << std::endl;
                break;
            }
        }
        
        if (!input_data.empty()) {
            std::cout << "\n--- 处理数据 ---" << std::endl;
            EmulationPreventionBytesOperator::printHexData(input_data, "输入数据");
            
            auto result_add = EmulationPreventionBytesOperator::addEmulationPreventionBytes(input_data);
            EmulationPreventionBytesOperator::printHexData(result_add, "添加防竞争码后");
            
            auto result_remove = EmulationPreventionBytesOperator::removeEmulationPreventionBytes(result_add);
            EmulationPreventionBytesOperator::printHexData(result_remove, "移除防竞争码后");
            
            bool roundtrip_success = EmulationPreventionBytesOperator::compareData(input_data, result_remove);
            std::cout << "往返测试结果: " << (roundtrip_success ? "✓ 成功" : "✗ 失败") << std::endl;
        }
        
        std::cout << "\n请输入下一组数据（空行结束）:" << std::endl;
    }
    
    std::cout << "\n程序结束。" << std::endl;
    return 0;
}
