// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <unordered_map>
#include <mutex>
#include <atomic>
#include <functional>
#include <optional>

template<typename T, bool UseCache = true>
class CallbackContainer {
public:
    CallbackContainer() = default;
    ~CallbackContainer() {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        // 清空所有回调，避免在析构后调用
        for (auto& callback : callbacks_) {
            callback.second = nullptr;
        }
        callbacks_.clear();
    }

    int addCallback(const std::function<void(const T&)> callback) {
        if (UseCache) {
            auto cache_data = getCacheData();
            if (cache_data.has_value()) {
                callback(cache_data.value());
            }
        }
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        int id = generateId();
        callbacks_[id] = callback;
        return id;
    }

    void removeCallback(int id) {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        auto it = callbacks_.find(id);
        if (it != callbacks_.end()) {
            it->second = nullptr;  // 先清空函数对象
            callbacks_.erase(it);
        }
    }

    void clear() {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        // 先置空所有回调函数，防止外部还持有拷贝时调用
        for (auto& callback : callbacks_) {
            callback.second = nullptr;
        }
        callbacks_.clear();
    }

    void notifyCallback(const T& data) {
        // 先复制回调列表，避免在回调中死锁
        decltype(callbacks_) callbacks_temp;
        {
            std::lock_guard<std::mutex> lock(callbacks_mutex_);
            callbacks_temp = callbacks_;
        }
        
        // 在锁外调用回调函数
        for (auto& callback : callbacks_temp) {
            if (callback.second) {  // 添加空指针检查
                callback.second(data);
            }
        }
        
        if (UseCache) {
            setCacheData(data);
        }
    }

    void notifyCallbackIfChanged(const T& data) {
        if (UseCache) {
            auto cache_data = getCacheData();
            if (cache_data.has_value() && cache_data.value() == data) {
                return;
            }
        }
        
        // 先复制回调列表，避免在回调中死锁
        decltype(callbacks_) callbacks_temp;
        {
            std::lock_guard<std::mutex> lock(callbacks_mutex_);
            callbacks_temp = callbacks_;
        }
        
        // 在锁外调用回调函数
        for (auto& callback : callbacks_temp) {
            if (callback.second) {  // 添加空指针检查
                callback.second(data);
            }
        }
        
        if (UseCache) {
            setCacheData(data);
        }
    }

private:
    void setCacheData(const T& data) {
        std::lock_guard<std::mutex> lock(cache_data_mutex_);
        cache_data_ = data;
    }

    std::optional<T> getCacheData() {
        std::lock_guard<std::mutex> lock(cache_data_mutex_);
        return cache_data_;
    }

    std::unordered_map<int, std::function<void(const T&)>> getCallbackList() {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        return callbacks_;
    }

    int generateId() {
        static std::atomic<int> id_counter(0);
        return id_counter++;
    }

    std::optional<T> cache_data_;
    std::unordered_map<int, std::function<void(const T&)>> callbacks_;
    std::mutex cache_data_mutex_;
    std::mutex callbacks_mutex_;
};

template<typename T, typename U, bool UseCache = true>
class CallbackContainerTwoParams {
public:
    CallbackContainerTwoParams() = default;
    ~CallbackContainerTwoParams() {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        // 清空所有回调，避免在析构后调用
        for (auto& callback : callbacks_) {
            callback.second = nullptr;
        }
        callbacks_.clear();
    }

    int addCallback(const std::function<void(const T&, const U&)> callback) {
        if (UseCache) {
            auto cache_data = getCacheData();
            if (cache_data.has_value()) {
                callback(cache_data.value().first, cache_data.value().second);
            }
        }
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        int id = generateId();
        callbacks_[id] = callback;
        return id;
    }

    void removeCallback(int id) {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        auto it = callbacks_.find(id);
        if (it != callbacks_.end()) {
            it->second = nullptr;  // 先清空函数对象
            callbacks_.erase(it);
        }
    }

    void clear() {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        // 先置空所有回调函数，防止外部还持有拷贝时调用
        for (auto& callback : callbacks_) {
            callback.second = nullptr;
        }
        callbacks_.clear();
    }

    void notifyCallback(const T& data, const U& data2) {
        // 先复制回调列表，避免在回调中死锁
        decltype(callbacks_) callbacks_temp;
        {
            std::lock_guard<std::mutex> lock(callbacks_mutex_);
            callbacks_temp = callbacks_;
        }
        
        // 在锁外调用回调函数
        for (auto& callback : callbacks_temp) {
            if (callback.second) {  // 添加空指针检查
                callback.second(data, data2);
            }
        }
        
        if (UseCache) {
            setCacheData(data, data2);
        }
    }

    void notifyCallbackIfChanged(const T& data, const U& data2) {
        if (UseCache) {
            auto cache_data = getCacheData();
            if (cache_data.has_value() && cache_data.value().first == data && cache_data.value().second == data2) {
                return;
            }
        }
        
        // 先复制回调列表，避免在回调中死锁
        decltype(callbacks_) callbacks_temp;
        {
            std::lock_guard<std::mutex> lock(callbacks_mutex_);
            callbacks_temp = callbacks_;
        }
        
        // 在锁外调用回调函数
        for (auto& callback : callbacks_temp) {
            if (callback.second) {  // 添加空指针检查
                callback.second(data, data2);
            }
        }
        
        if (UseCache) {
            setCacheData(data, data2);
        }
    }

private:
    void setCacheData(const T& data, const U& data2) {
        std::lock_guard<std::mutex> lock(cache_data_mutex_);
        cache_data_ = std::make_pair(data, data2);
    }

    std::optional<std::pair<T, U>> getCacheData() {
        std::lock_guard<std::mutex> lock(cache_data_mutex_);
        return cache_data_;
    }

    std::unordered_map<int, std::function<void(const T&, const U&)>> getCallbackList() {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        return callbacks_;
    }

    int generateId() {
        static std::atomic<int> id_counter(0);
        return id_counter++;
    }

    std::optional<std::pair<T, U>> cache_data_;
    std::unordered_map<int, std::function<void(const T&, const U&)>> callbacks_;
    std::mutex cache_data_mutex_;
    std::mutex callbacks_mutex_;
};
