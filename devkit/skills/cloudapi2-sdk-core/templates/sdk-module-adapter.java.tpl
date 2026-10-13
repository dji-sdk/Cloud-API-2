package com.example.dji.sdk.modules;

import com.dji.flighthub.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * ${MODULE_NAME} SDK 适配器。
 * 封装 SDK 的 ${MODULE_NAME} 相关调用，提供同步/异步接口给 Controller 层使用。
 *
 * @author ${AUTHOR}
 */
@Component
@Slf4j
public class ${ModuleName}Adapter {

    /**
     * 示例：异步操作模板。
     * 将 SDK 回调模式转为 CompletableFuture，方便上层使用。
     *
     * @param sn 设备序列号
     * @return 操作结果
     */
    public CompletableFuture<${ResultType}> ${methodName}(String sn) {
        CompletableFuture<${ResultType}> future = new CompletableFuture<>();

        // ✅ 规则 5：null 检查
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            future.completeExceptionally(
                new DeviceNotFoundException("Drone not found: " + sn));
            return future;
        }

        // 调用 SDK
        drone.${sdkMethod}((error, result) -> {
            // ✅ 规则 2：检查 errorCode
            if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log.error("[{}] {} 失败: sdk={}, server={}, msg={}",
                    sn, "${methodName}",
                    error.getErrorCode(), error.getServerCode(), error.getServerMessage());
                future.completeExceptionally(
                    new SdkOperationException("${methodName} failed", error));
                return;
            }
            future.complete(result);
        });

        return future;
    }

    /**
     * 示例：监听模板。
     * 返回 listenId 和取消函数，由调用方管理生命周期。
     *
     * @param sn 设备序列号
     * @param handler 数据处理回调
     * @return listenId (>=0 成功, <0 失败)
     */
    public int ${listenMethodName}(String sn, ${CallbackType} handler) {
        // ✅ 规则 5：null 检查
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            log.warn("[{}] Drone 实例为 null，无法订阅", sn);
            return -1;
        }

        // ✅ 规则 3：返回 listenId 供调用方保存和 cancel
        int listenId = drone.${sdkListenMethod}(data -> {
            handler.onData(data);
        });

        if (listenId < 0) {
            log.error("[{}] 订阅 {} 失败, listenId={}", sn, "${sdkListenMethod}", listenId);
        }
        return listenId;
    }
}
