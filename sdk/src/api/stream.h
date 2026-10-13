// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include "custom_sei.h"
#include "pull_stream.h"

namespace flight_hub_sdk {

    /// Stream 模块入口，提供 CustomSEI 模块和拉流模块的获取。
    class IStream {
    public:
        virtual ~IStream() = default;

        /// @brief 获取自定义 SEI 编解码模块
        /// @return CustomSEI 模块实例
        virtual std::shared_ptr<ICustomSEIModule> getCustomSEIModule() = 0;

        /// @brief 获取拉流模块（按 stream_id 区分不同流）
        /// @param stream_id 流标识
        /// @return 拉流模块实例
        virtual std::shared_ptr<IPullStreamModule> getPullStreamModule(const std::string& stream_id) = 0;

    };

}  // namespace flight_hub_sdk