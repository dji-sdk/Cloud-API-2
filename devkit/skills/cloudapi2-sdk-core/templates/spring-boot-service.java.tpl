package com.example.dji.service;

import com.example.dji.sdk.modules.${ModuleName}Adapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ${SERVICE_NAME} 业务服务。
 * 管理设备订阅的生命周期，提供业务接口给 Controller。
 *
 * @author ${AUTHOR}
 */
@Service
@Slf4j
public class ${ServiceName}Service {

    private final ${ModuleName}Adapter adapter;

    /** 按设备 SN 管理订阅清理函数 */
    private final Map<String, List<Runnable>> cleanups = new ConcurrentHashMap<>();

    public ${ServiceName}Service(${ModuleName}Adapter adapter) {
        this.adapter = adapter;
    }

    /**
     * 订阅指定设备。
     * ✅ 规则 3：保存 listenId，注册清理函数。
     *
     * @param sn 设备序列号
     */
    public void subscribe(String sn) {
        int listenId = adapter.${listenMethodName}(sn, data -> {
            // 业务处理逻辑
            processData(sn, data);
        });

        if (listenId >= 0) {
            track(sn, () -> {
                // ✅ 规则 3：cancel 时调用对应的取消方法（通过 adapter 保持一致）
                adapter.cancelListen(sn, listenId);
                log.info("[{}] 已取消订阅, listenId={}", sn, listenId);
            });
            log.info("[{}] 订阅成功, listenId={}", sn, listenId);
        }
    }

    /**
     * 取消指定设备的所有订阅。
     */
    public void unsubscribe(String sn) {
        List<Runnable> fns = cleanups.remove(sn);
        if (fns != null) {
            fns.forEach(Runnable::run);
            log.info("[{}] 已清理 {} 个订阅", sn, fns.size());
        }
    }

    /**
     * 应用关闭时清理所有订阅。
     * ✅ 规则 3：服务关闭时必须 cancel 所有 listen。
     */
    @PreDestroy
    public void shutdown() {
        cleanups.forEach((sn, fns) -> fns.forEach(Runnable::run));
        cleanups.clear();
        log.info("所有设备订阅已清理");
    }

    // --- 私有方法 ---

    private void track(String sn, Runnable cancelAction) {
        cleanups.computeIfAbsent(sn, k -> new CopyOnWriteArrayList<>()).add(cancelAction);
    }

    private void processData(String sn, Object data) {
        // TODO: 实现具体业务逻辑
        log.debug("[{}] 收到数据: {}", sn, data);
    }
}
