#include <iostream>
#include <cstring>
#include <cstdlib>
#include <thread>
#include <chrono>
#include "MQTTAsync.h"

// MQTT连接回调函数
void onConnect(void* context, MQTTAsync_successData* response) {
    (void)context; // 避免未使用参数警告
    std::cout << "✅ MQTT连接成功!" << std::endl;
    std::cout << "服务器URI: " << response->alt.connect.serverURI << std::endl;
    std::cout << "MQTT版本: " << response->alt.connect.MQTTVersion << std::endl;
    std::cout << "会话存在: " << (response->alt.connect.sessionPresent ? "是" : "否") << std::endl;
}

void onConnectFailure(void* context, MQTTAsync_failureData* response) {
    (void)context; // 避免未使用参数警告
    std::cout << "❌ MQTT连接失败!" << std::endl;
    std::cout << "错误代码: " << response->code << std::endl;
    if (response->message) {
        std::cout << "错误信息: " << response->message << std::endl;
    }
}

// 消息到达回调函数
int onMessageArrived(void* context, char* topicName, int topicLen, MQTTAsync_message* message) {
    (void)context; // 避免未使用参数警告
    std::cout << "📨 收到消息:" << std::endl;
    std::cout << "主题: " << std::string(topicName, topicLen) << std::endl;
    std::cout << "QoS: " << message->qos << std::endl;
    std::cout << "保留: " << (message->retained ? "是" : "否") << std::endl;
    std::cout << "内容: " << std::string((char*)message->payload, message->payloadlen) << std::endl;
    std::cout << "---" << std::endl;
    
    MQTTAsync_freeMessage(&message);
    return 1;
}

// 连接丢失回调函数
void onConnectionLost(void* context, char* cause) {
    (void)context; // 避免未使用参数警告
    std::cout << "⚠️  MQTT连接丢失!" << std::endl;
    if (cause) {
        std::cout << "原因: " << cause << std::endl;
    }
}

// 发布成功回调函数
void onPublishSuccess(void* context, MQTTAsync_successData* response) {
    (void)context; // 避免未使用参数警告
    (void)response; // 避免未使用参数警告
    std::cout << "✅ 消息发布成功!" << std::endl;
}

void onPublishFailure(void* context, MQTTAsync_failureData* response) {
    (void)context; // 避免未使用参数警告
    std::cout << "❌ 消息发布失败!" << std::endl;
    std::cout << "错误代码: " << response->code << std::endl;
    if (response->message) {
        std::cout << "错误信息: " << response->message << std::endl;
    }
}

int main(int argc, char* argv[]) {
    std::cout << "MQTT SSL Demo" << std::endl;
    std::cout << "===================" << std::endl;

    const char* serverURI = nullptr;
    if (argc >= 2 && argv[1] && argv[1][0] != '\0') {
        serverURI = argv[1];
    } else if (const char* from_env = std::getenv("MQTT_SERVER_URI")) {
        if (from_env[0] != '\0') {
            serverURI = from_env;
        }
    }
    if (!serverURI) {
        std::cerr << "用法: " << argv[0] << " <mqtt_uri>" << std::endl;
        std::cerr << "  或设置环境变量 MQTT_SERVER_URI" << std::endl;
        std::cerr << "示例: " << argv[0] << " tcp://127.0.0.1:1883" << std::endl;
        return 1;
    }

    const char* clientId = "paho_demo_client";
    const char* topic = "test/topic";
    const char* message = R"({"data": "Hello from Paho MQTT SSL Demo!"})";
    
    std::cout << "服务器地址: " << serverURI << std::endl;
    std::cout << "客户端ID: " << clientId << std::endl;
    std::cout << "测试主题: " << topic << std::endl;
    std::cout << "测试消息: " << message << std::endl;
    std::cout << "===================" << std::endl;
    
    MQTTAsync client;
    MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
    MQTTAsync_SSLOptions ssl_opts = MQTTAsync_SSLOptions_initializer;
    MQTTAsync_responseOptions pub_opts = MQTTAsync_responseOptions_initializer;
    MQTTAsync_responseOptions sub_opts = MQTTAsync_responseOptions_initializer;
    
    int rc;
    
    // 创建MQTT客户端
    rc = MQTTAsync_create(&client, serverURI, clientId, MQTTCLIENT_PERSISTENCE_NONE, NULL);
    if (rc != MQTTASYNC_SUCCESS) {
        std::cout << "❌ 创建MQTT客户端失败，错误代码: " << rc << std::endl;
        return 1;
    }
    std::cout << "✅ MQTT客户端创建成功" << std::endl;
    
    // 设置回调函数
    rc = MQTTAsync_setCallbacks(client, NULL, onConnectionLost, onMessageArrived, NULL);
    if (rc != MQTTASYNC_SUCCESS) {
        std::cout << "❌ 设置回调函数失败，错误代码: " << rc << std::endl;
        MQTTAsync_destroy(&client);
        return 1;
    }
    std::cout << "✅ 回调函数设置成功" << std::endl;
    
    // 配置SSL选项
    ssl_opts.struct_version = 1;
    ssl_opts.sslVersion = MQTT_SSL_VERSION_TLS_1_2;
    ssl_opts.verify = 0; // 跳过证书验证（仅用于测试）
    
    // 配置连接选项
    conn_opts.struct_version = 1;
    conn_opts.keepAliveInterval = 20;
    conn_opts.cleansession = 1;
    conn_opts.connectTimeout = 10;
    conn_opts.ssl = &ssl_opts;
    conn_opts.onSuccess = onConnect;
    conn_opts.onFailure = onConnectFailure;
    conn_opts.context = NULL;
    
    // 连接到MQTT服务器
    std::cout << "🔗 正在连接到MQTT服务器..." << std::endl;
    rc = MQTTAsync_connect(client, &conn_opts);
    if (rc != MQTTASYNC_SUCCESS) {
        std::cout << "❌ 连接请求失败，错误代码: " << rc << std::endl;
        MQTTAsync_destroy(&client);
        return 1;
    }
    
    // 等待连接完成
    std::this_thread::sleep_for(std::chrono::seconds(3));
    
    // 订阅主题
    std::cout << "📡 订阅主题: " << topic << std::endl;
    sub_opts.onSuccess = onPublishSuccess;
    sub_opts.onFailure = onPublishFailure;
    sub_opts.context = NULL;
    
    rc = MQTTAsync_subscribe(client, topic, 1, &sub_opts);
    if (rc != MQTTASYNC_SUCCESS) {
        std::cout << "❌ 订阅失败，错误代码: " << rc << std::endl;
    } else {
        std::cout << "✅ 订阅请求发送成功" << std::endl;
    }
    
    // 等待订阅完成
    std::this_thread::sleep_for(std::chrono::seconds(1));
    
    // 发布消息
    std::cout << "📤 发布消息到主题: " << topic << std::endl;
    pub_opts.onSuccess = onPublishSuccess;
    pub_opts.onFailure = onPublishFailure;
    pub_opts.context = NULL;
    
    rc = MQTTAsync_send(client, topic, strlen(message), message, 1, 0, &pub_opts);
    if (rc != MQTTASYNC_SUCCESS) {
        std::cout << "❌ 发布消息失败，错误代码: " << rc << std::endl;
    } else {
        std::cout << "✅ 消息发布请求发送成功" << std::endl;
    }
    
    // 等待消息处理
    std::this_thread::sleep_for(std::chrono::seconds(2));
    
    // 断开连接
    std::cout << "🔌 断开MQTT连接..." << std::endl;
    MQTTAsync_disconnectOptions disc_opts = MQTTAsync_disconnectOptions_initializer;
    disc_opts.timeout = 1000;
    
    rc = MQTTAsync_disconnect(client, &disc_opts);
    if (rc != MQTTASYNC_SUCCESS) {
        std::cout << "❌ 断开连接失败，错误代码: " << rc << std::endl;
    } else {
        std::cout << "✅ 断开连接请求发送成功" << std::endl;
    }
    
    // 等待断开完成
    std::this_thread::sleep_for(std::chrono::seconds(1));
    
    // 清理资源
    MQTTAsync_destroy(&client);
    
    std::cout << "🏁 Demo完成" << std::endl;
    return 0;
}
