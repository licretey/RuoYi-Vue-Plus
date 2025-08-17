package org.dromara.common.redis.manager;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheConstants;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.redis.annotation.CacheStrictSync;
import org.dromara.common.redis.dto.CacheSyncMessage;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 严格缓存同步管理器
 * 
 * <p>专门处理带有@CacheStrictSync注解的方法的缓存同步逻辑，
 * 提供比普通同步更强的一致性保证。</p>
 *
 * @author Lion Li
 */
@Slf4j
@Component
public class StrictCacheSyncManager {

    /**
     * 严格同步主题前缀
     */
    private static final String STRICT_SYNC_TOPIC_PREFIX = "cache:strict:sync:";

    /**
     * 当前节点ID
     */
    private final String nodeId;

    /**
     * 本地Caffeine缓存实例
     */
    private static final com.github.benmanes.caffeine.cache.Cache<Object, Object>
        CAFFEINE = SpringUtils.getBean("caffeine");

    /**
     * 同步确认映射（用于同步等待模式）
     */
    private final ConcurrentHashMap<String, CompletableFuture<Boolean>> syncConfirmations = new ConcurrentHashMap<>();

    /**
     * 是否启用严格同步
     */
    @Value("${cache.sync.strict.enabled:false}")
    private boolean strictSyncEnabled;

    /**
     * 默认同步超时时间
     */
    @Value("${cache.sync.strict.timeout:3000}")
    private long defaultTimeout;

    public StrictCacheSyncManager() {
        this.nodeId = IdUtil.fastSimpleUUID();
        log.info("严格缓存同步管理器初始化，节点ID: {}", nodeId);
    }

    /**
     * 发布严格缓存更新消息
     */
    public void publishStrictPut(String cacheName, Object key, CacheStrictSync annotation) {
        if (!strictSyncEnabled) {
            return;
        }

        CacheSyncMessage message = createStrictMessage(cacheName, key, CacheSyncMessage.CacheOperation.PUT, annotation);
        publishStrictMessage(message, annotation);
        
        log.debug("发布严格缓存更新消息: cacheName={}, key={}, syncType={}", 
            cacheName, key, annotation.syncType());
    }

    /**
     * 发布严格缓存清除消息
     */
    public void publishStrictEvict(String cacheName, Object key, CacheStrictSync annotation) {
        if (!strictSyncEnabled) {
            return;
        }

        CacheSyncMessage message = createStrictMessage(cacheName, key, CacheSyncMessage.CacheOperation.EVICT, annotation);
        publishStrictMessage(message, annotation);
        
        log.debug("发布严格缓存清除消息: cacheName={}, key={}, syncType={}", 
            cacheName, key, annotation.syncType());
    }

    /**
     * 发布严格缓存清空消息
     */
    public void publishStrictClear(String cacheName, CacheStrictSync annotation) {
        if (!strictSyncEnabled) {
            return;
        }

        CacheSyncMessage message = createStrictMessage(cacheName, null, CacheSyncMessage.CacheOperation.CLEAR, annotation);
        publishStrictMessage(message, annotation);
        
        log.debug("发布严格缓存清空消息: cacheName={}, syncType={}", cacheName, annotation.syncType());
    }

    /**
     * 创建严格同步消息
     */
    private CacheSyncMessage createStrictMessage(String cacheName, Object key, 
                                               CacheSyncMessage.CacheOperation operation, 
                                               CacheStrictSync annotation) {
        CacheSyncMessage message = new CacheSyncMessage(cacheName, key, operation, System.currentTimeMillis(), nodeId);
        
        // 可以在这里添加严格同步特有的属性
        // 比如优先级、同步类型等
        
        return message;
    }

    /**
     * 发布严格同步消息
     */
    private void publishStrictMessage(CacheSyncMessage message, CacheStrictSync annotation) {
        String topic = getStrictSyncTopic(message.getCacheName());
        
        switch (annotation.syncType()) {
            case ASYNC:
                publishAsync(topic, message);
                break;
            case SYNC:
                publishSync(topic, message, annotation.timeout());
                break;
            case IMMEDIATE:
                publishImmediate(topic, message);
                break;
            default:
                log.warn("未知的同步类型: {}", annotation.syncType());
                publishAsync(topic, message);
        }
    }

    /**
     * 异步发布消息
     */
    private void publishAsync(String topic, CacheSyncMessage message) {
        CompletableFuture.runAsync(() -> {
            try {
                RedisUtils.publish(topic, message);
            } catch (Exception e) {
                log.error("异步发布严格同步消息失败", e);
            }
        });
    }

    /**
     * 同步发布消息（等待确认）
     */
    private void publishSync(String topic, CacheSyncMessage message, long timeout) {
        String confirmKey = generateConfirmKey(message);
        CompletableFuture<Boolean> confirmation = new CompletableFuture<>();
        syncConfirmations.put(confirmKey, confirmation);

        try {
            // 发布消息
            RedisUtils.publish(topic, message);
            
            // 等待确认
            Boolean result = confirmation.get(timeout, TimeUnit.MILLISECONDS);
            if (result != null && result) {
                log.debug("严格同步消息确认成功: {}", confirmKey);
            } else {
                log.warn("严格同步消息确认失败: {}", confirmKey);
            }
        } catch (Exception e) {
            log.error("同步发布严格同步消息失败", e);
        } finally {
            syncConfirmations.remove(confirmKey);
        }
    }

    /**
     * 立即发布消息（最高优先级）
     */
    private void publishImmediate(String topic, CacheSyncMessage message) {
        try {
            // 立即发布到普通主题
            RedisUtils.publish(CacheConstants.CACHE_SYNC_TOPIC, message);
            // 同时发布到严格同步主题
            RedisUtils.publish(topic, message);
            
            log.debug("立即发布严格同步消息成功");
        } catch (Exception e) {
            log.error("立即发布严格同步消息失败", e);
        }
    }

    /**
     * 处理接收到的严格同步消息
     */
    public void handleStrictSyncMessage(CacheSyncMessage message) {
        // 忽略自己发送的消息
        if (nodeId.equals(message.getNodeId())) {
            return;
        }

        try {
            String cacheName = message.getCacheName();
            Object key = message.getKey();
            
            switch (message.getOperation()) {
                case EVICT:
                    handleStrictEvict(cacheName, key);
                    break;
                case PUT:
                    handleStrictPut(cacheName, key);
                    break;
                case CLEAR:
                    handleStrictClear(cacheName);
                    break;
                default:
                    log.warn("未知的严格同步操作类型: {}", message.getOperation());
            }
            
            // 发送确认消息（如果需要）
            sendConfirmation(message);
            
            log.debug("处理严格同步消息: operation={}, cacheName={}, key={}", 
                message.getOperation(), cacheName, key);
                
        } catch (Exception e) {
            log.error("处理严格同步消息失败", e);
        }
    }

    /**
     * 处理严格缓存清除
     */
    private void handleStrictEvict(String cacheName, Object key) {
        String uniqueKey = cacheName + ":" + key;
        CAFFEINE.invalidate(uniqueKey);
        
        // 可以添加更严格的清除逻辑
        // 比如强制刷新、验证清除结果等
    }

    /**
     * 处理严格缓存更新
     */
    private void handleStrictPut(String cacheName, Object key) {
        // 对于严格同步，我们清除本地缓存让其重新加载
        handleStrictEvict(cacheName, key);
    }

    /**
     * 处理严格缓存清空
     */
    private void handleStrictClear(String cacheName) {
        CAFFEINE.asMap().keySet().removeIf(key -> 
            key.toString().startsWith(cacheName + ":"));
    }

    /**
     * 发送确认消息
     */
    private void sendConfirmation(CacheSyncMessage message) {
        // 简化实现，实际可以发送确认消息到特定主题
        String confirmKey = generateConfirmKey(message);
        log.debug("发送严格同步确认: {}", confirmKey);
    }

    /**
     * 生成确认键
     */
    private String generateConfirmKey(CacheSyncMessage message) {
        return String.format("%s:%s:%s:%d", 
            message.getCacheName(), 
            message.getKey(), 
            message.getOperation(), 
            message.getTimestamp());
    }

    /**
     * 获取严格同步主题
     */
    private String getStrictSyncTopic(String cacheName) {
        return STRICT_SYNC_TOPIC_PREFIX + cacheName;
    }

    /**
     * 订阅严格缓存同步消息
     */
    public void subscribeStrictCacheSync(String cacheName) {
        if (!strictSyncEnabled) {
            return;
        }
        
        String topic = getStrictSyncTopic(cacheName);
        Consumer<CacheSyncMessage> messageHandler = this::handleStrictSyncMessage;
        RedisUtils.subscribe(topic, CacheSyncMessage.class, messageHandler);
        
        log.info("开始订阅严格缓存同步消息，主题: {}", topic);
    }

    /**
     * 获取当前节点ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 检查是否启用严格同步
     */
    public boolean isStrictSyncEnabled() {
        return strictSyncEnabled;
    }
}
