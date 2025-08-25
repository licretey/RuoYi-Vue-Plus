package org.dromara.common.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.manager.CacheSyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 缓存同步主题监听器
 * 在应用启动时自动订阅缓存同步消息
 *
 * @author Lion Li
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "cache.sync.enabled", havingValue = "true", matchIfMissing = true)
public class CacheSyncTopicListener implements ApplicationRunner, Ordered {

    @Autowired
    private CacheSyncManager cacheSyncManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 订阅缓存同步消息
            cacheSyncManager.subscribeCacheSync();
            log.info("缓存同步监听器启动成功，节点ID: {}", cacheSyncManager.getNodeId());
        } catch (Exception e) {
            log.error("缓存同步监听器启动失败", e);
            throw e;
        }
    }

    @Override
    public int getOrder() {
        // 设置较低的优先级，确保在其他组件初始化完成后再启动
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
