package org.dromara.common.redis.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 缓存同步消息
 * 用于分布式环境下的缓存同步通知
 *
 * @author Lion Li
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheSyncMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存键
     */
    private Object key;

    /**
     * 操作类型
     */
    private CacheOperation operation;

    /**
     * 操作时间戳
     */
    private Long timestamp;

    /**
     * 发送节点标识（避免自己处理自己发送的消息）
     */
    private String nodeId;

    /**
     * 缓存操作类型枚举
     */
    public enum CacheOperation {
        /**
         * 清除单个缓存项
         */
        EVICT,
        /**
         * 清除所有缓存
         */
        CLEAR,
        /**
         * 更新缓存（实际是先清除再让其他节点重新加载）
         */
        PUT
    }

    /**
     * 创建清除缓存消息
     */
    public static CacheSyncMessage evict(String cacheName, Object key, String nodeId) {
        return new CacheSyncMessage(cacheName, key, CacheOperation.EVICT, System.currentTimeMillis(), nodeId);
    }

    /**
     * 创建清除所有缓存消息
     */
    public static CacheSyncMessage clear(String cacheName, String nodeId) {
        return new CacheSyncMessage(cacheName, null, CacheOperation.CLEAR, System.currentTimeMillis(), nodeId);
    }

    /**
     * 创建更新缓存消息
     */
    public static CacheSyncMessage put(String cacheName, Object key, String nodeId) {
        return new CacheSyncMessage(cacheName, key, CacheOperation.PUT, System.currentTimeMillis(), nodeId);
    }
}
