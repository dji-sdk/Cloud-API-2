package com.example.dji.sdk;

import com.dji.flighthub.sdk.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SDK 回调处理工具类。
 * 提供回调转 Future、超时控制、统一错误处理等通用能力。
 *
 * @author ${AUTHOR}
 */
@Slf4j
public class SdkCallbackUtils {

    /** 默认超时时间（秒） */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private SdkCallbackUtils() {}

    /**
     * 将 SDK 一次性回调转为 CompletableFuture（带超时）。
     * ✅ 规则 2：内部自动检查 errorCode。
     *
     * 使用示例：
     * <pre>
     * CompletableFuture<List<DeviceTopo>> future = SdkCallbackUtils.toFuture(
     *     callback -> workspace.getDeviceManageModule().getDeviceList(callback),
     *     "getDeviceList"
     * );
     * List<DeviceTopo> devices = future.get(10, TimeUnit.SECONDS);
     * </pre>
     *
     * @param action SDK 调用（接收 callback 参数）
     * @param operationName 操作名称（用于日志）
     * @param <T> 返回数据类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> toFuture(
            SdkAction<T> action, String operationName) {
        return toFuture(action, operationName, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 带自定义超时的版本。
     */
    public static <T> CompletableFuture<T> toFuture(
            SdkAction<T> action, String operationName, int timeoutSeconds) {

        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            action.execute((error, result) -> {
                // ✅ 规则 2：统一检查 errorCode
                if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                    log.error("[{}] SDK 错误: errorCode={}, httpCode={}, serverCode={}, msg={}",
                        operationName,
                        error.getErrorCode(), error.getHttpCode(),
                        error.getServerCode(), error.getServerMessage());
                    future.completeExceptionally(
                        new SdkOperationException(operationName + " failed", error));
                    return;
                }
                future.complete(result);
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        // 超时保护
        return future.orTimeout(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 将无返回值的 SDK 操作转为 Future。
     * 适用于 acquireFlightControlAuthority 等仅回调 error 的操作。
     */
    public static CompletableFuture<Void> toVoidFuture(
            VoidSdkAction action, String operationName) {

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            action.execute(error -> {
                if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                    log.error("[{}] 失败: {}", operationName, error.getErrorCode());
                    future.completeExceptionally(
                        new SdkOperationException(operationName + " failed", error));
                    return;
                }
                future.complete(null);
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future.orTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    // 函数式接口 ---

    @FunctionalInterface
    public interface SdkAction<T> {
        void execute(SdkResultCallback<T> callback);
    }

    @FunctionalInterface
    public interface VoidSdkAction {
        void execute(ResultCallbackWithError callback);
    }

    @FunctionalInterface
    public interface SdkResultCallback<T> {
        void onResult(FlightHubError error, T result);
    }
}

/**
 * SDK 操作失败时抛出的运行时异常。
 * 封装 FlightHubError 以便在 Future/异步链中传递错误信息。
 *
 * 建议将此类单独放置在 com.example.dji.sdk.exception 包下：
 *   src/main/java/com/example/dji/sdk/exception/SdkOperationException.java
 *
 * @author ${AUTHOR}
 */
class SdkOperationException extends RuntimeException {

    private final transient FlightHubError sdkError;

    /**
     * @param message   操作名称及失败描述，如 "getDeviceList failed"
     * @param sdkError  原始 SDK 错误对象（含 errorCode/httpCode/serverCode）
     */
    public SdkOperationException(String message, FlightHubError sdkError) {
        super(message + " [errorCode=" + sdkError.getErrorCode()
              + ", httpCode=" + sdkError.getHttpCode()
              + ", serverCode=" + sdkError.getServerCode()
              + ", msg=" + sdkError.getServerMessage() + "]");
        this.sdkError = sdkError;
    }

    /**
     * 获取原始 SDK 错误对象，供上层精确处理错误码。
     *
     * @return FlightHubError
     */
    public FlightHubError getSdkError() {
        return sdkError;
    }
}
