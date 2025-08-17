package org.dromara.common.redis.manager;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheConstants;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.redis.dto.CacheSyncMessage;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 分布式缓存同步管理器
 * 负责处理多级缓存在分布式环境下的数据一致性
 *
 * @author Lion Li
 */
@Slf4j
@Component
public class CacheSyncManager {

    /**
     * 缓存同步主题
     */
    public static final String CACHE_SYNC_TOPIC = CacheConstants.CACHE_SYNC_TOPIC;

    /**
     * 当前节点ID（用于避免处理自己发送的消息）
     */
    private final String nodeId;

    /**
     * 本地Caffeine缓存实例
     */
    private static final com.github.benmanes.caffeine.cache.Cache<Object, Object> CAFFEINE = SpringUtils
            .getBean("caffeine");

    /**
     * 缓存实例映射（用于获取具体的缓存实例）
     */
    private final ConcurrentHashMap<String, org.springframework.cache.Cache> cacheInstanceMap = new ConcurrentHashMap<>();

    /**
     * 是否启用缓存同步
     */
    @Value("${cache.sync.enabled:true}")
    private boolean syncEnabled;

    public CacheSyncManager() {
        this.nodeId = IdUtil.fastSimpleUUID();
        log.info("缓存同步管理器初始化，节点ID: {}", nodeId);
    }

    /**
     * 注册缓存实例
     */
    public void registerCache(String cacheName, org.springframework.cache.Cache cache) {
        cacheInstanceMap.put(cacheName, cache);
        log.debug("注册缓存实例: {}", cacheName);
    }

    /**
     * 发布缓存清除消息
     */
    public void publishEvict(String cacheName, Object key) {
        if (!syncEnabled) {
            return;
        }

        CacheSyncMessage message = CacheSyncMessage.evict(cacheName, key, nodeId);
        publishMessage(message);
        log.debug("发布缓存清除消息: cacheName={}, key={}", cacheName, key);
    }

    /**
     * 发布缓存更新消息
     */
    public void publishPut(String cacheName, Object key) {
        if (!syncEnabled) {
            return;
        }

        CacheSyncMessage message = CacheSyncMessage.put(cacheName, key, nodeId);
        publishMessage(message);
        log.debug("发布缓存更新消息: cacheName={}, key={}", cacheName, key);
    }

    /**
     * 发布缓存清空消息
     */
    public void publishClear(String cacheName) {
        if (!syncEnabled) {
            return;
        }

        CacheSyncMessage message = CacheSyncMessage.clear(cacheName, nodeId);
        publishMessage(message);
        log.debug("发布缓存清空消息: cacheName={}", cacheName);
    }

    /**
     * 发布同步消息
     */
    private void publishMessage(CacheSyncMessage message) {
        try {
            RedisUtils.publish(CACHE_SYNC_TOPIC, message);
        } catch (Exception e) {
            log.error("发布缓存同步消息失败", e);
        }
    }

    /**
     * 处理接收到的缓存同步消息
     */
    public void handleSyncMessage(CacheSyncMessage message) {
        // 忽略自己发送的消息
        if (nodeId.equals(message.getNodeId())) {
            return;
        }

        try {
            String cacheName = message.getCacheName();
            Object key = message.getKey();

            switch (message.getOperation()) {
                case EVICT:
                    handleEvict(cacheName, key);
                    break;
                case PUT:
                    handlePut(cacheName, key);
                    break;
                case CLEAR:
                    handleClear(cacheName);
                    break;
                default:
                    log.warn("未知的缓存操作类型: {}", message.getOperation());
            }

            log.debug("处理缓存同步消息: operation={}, cacheName={}, key={}",
                    message.getOperation(), cacheName, key);

        } catch (Exception e) {
            log.error("处理缓存同步消息失败", e);
        }
    }

    /**
     * 处理缓存清除
     */
    private void handleEvict(String cacheName, Object key) {
        // 清除本地Caffeine缓存
        String uniqueKey = cacheName + ":" + key;
        CAFFEINE.invalidate(uniqueKey);
    }

    /**
     * 处理缓存更新（实际是清除，让下次访问时重新加载）
     */
    private void handlePut(String cacheName, Object key) {
        // 对于PUT操作，我们也是清除本地缓存，让下次访问时从Redis重新加载
        handleEvict(cacheName, key);
    }

    /**
     * 处理缓存清空
     */
    private void handleClear(String cacheName) {
        // 清除所有相关的本地缓存
        CAFFEINE.asMap().keySet().removeIf(key -> key.toString().startsWith(cacheName + ":"));
    }

    /**
     * 订阅缓存同步消息
     */
    public void subscribeCacheSync() {
        if (!syncEnabled) {
            log.info("缓存同步功能已禁用");
            return;
        }

        Consumer<CacheSyncMessage> messageHandler = this::handleSyncMessage;
        RedisUtils.subscribe(CACHE_SYNC_TOPIC, CacheSyncMessage.class, messageHandler);
        log.info("开始订阅缓存同步消息，主题: {}", CACHE_SYNC_TOPIC);
    }

    /**
     * 获取当前节点ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 检查是否启用同步
     */
    public boolean isSyncEnabled() {
        return syncEnabled;
    }
}
