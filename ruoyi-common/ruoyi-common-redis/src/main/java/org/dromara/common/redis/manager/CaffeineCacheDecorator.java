package org.dromara.common.redis.manager;

import org.dromara.common.core.utils.SpringUtils;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Cache 装饰器模式(用于扩展 Caffeine 一级缓存)
 *
 * @author LionLi
 */
public class CaffeineCacheDecorator implements Cache {

    private static final com.github.benmanes.caffeine.cache.Cache<Object, Object>
        CAFFEINE = SpringUtils.getBean("caffeine");

    private final String name;
    private final Cache cache;

    /**
     * 缓存同步管理器（延迟获取，避免循环依赖）
     */
    private CacheSyncManager cacheSyncManager;

    public CaffeineCacheDecorator(String name, Cache cache) {
        this.name = name;
        this.cache = cache;
    }

    /**
     * 获取缓存同步管理器（延迟获取，避免循环依赖）
     */
    private CacheSyncManager getCacheSyncManager() {
        if (cacheSyncManager == null) {
            try {
                cacheSyncManager = SpringUtils.getBean(CacheSyncManager.class);
                // 注册当前缓存实例
                cacheSyncManager.registerCache(name, this);
            } catch (Exception e) {
                // 如果获取不到，说明可能没有启用缓存同步功能，忽略即可
            }
        }
        return cacheSyncManager;
    }

    /**
     * 检查是否应该发布同步消息
     * 只有在启用普通同步且未启用严格同步时才发布
     */
    private boolean shouldPublishSyncMessage() {
        try {
            // 检查是否启用普通同步
            Boolean syncEnabled = SpringUtils.getProperty("cache.sync.enabled", Boolean.class, true);
            if (!syncEnabled) {
                return false;
            }

            // 检查是否启用严格同步（如果启用严格同步，则由AOP处理，这里不处理）
            Boolean strictSyncEnabled = SpringUtils.getProperty("cache.sync.strict.enabled", Boolean.class, false);
            return !strictSyncEnabled;
        } catch (Exception e) {
            // 配置获取失败时，默认启用普通同步
            return false;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    public String getUniqueKey(Object key) {
        return name + ":" + key;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key));
        return (ValueWrapper) o;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key, type));
        return (T) o;
    }

    @Override
    public void put(Object key, Object value) {
        CAFFEINE.invalidate(getUniqueKey(key));
        cache.put(key, value);

        // 只在普通同步模式下发布同步消息（严格同步由AOP处理）
        if (shouldPublishSyncMessage()) {
            CacheSyncManager syncManager = getCacheSyncManager();
            if (syncManager != null) {
                syncManager.publishPut(name, key);
            }
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        CAFFEINE.invalidate(getUniqueKey(key));
        ValueWrapper result = cache.putIfAbsent(key, value);

        // 如果成功插入，在普通同步模式下发布缓存更新同步消息
        if (result == null && shouldPublishSyncMessage()) {
            CacheSyncManager syncManager = getCacheSyncManager();
            if (syncManager != null) {
                syncManager.publishPut(name, key);
            }
        }
        return result;
    }

    @Override
    public void evict(Object key) {
        evictIfPresent(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean b = cache.evictIfPresent(key);
        if (b) {
            CAFFEINE.invalidate(getUniqueKey(key));

            // 在普通同步模式下发布缓存清除同步消息
            if (shouldPublishSyncMessage()) {
                CacheSyncManager syncManager = getCacheSyncManager();
                if (syncManager != null) {
                    syncManager.publishEvict(name, key);
                }
            }
        }
        return b;
    }

    @Override
    public void clear() {
        CAFFEINE.invalidateAll();
        cache.clear();

        // 在普通同步模式下发布缓存清空同步消息
        if (shouldPublishSyncMessage()) {
            CacheSyncManager syncManager = getCacheSyncManager();
            if (syncManager != null) {
                syncManager.publishClear(name);
            }
        }
    }

    @Override
    public boolean invalidate() {
        return cache.invalidate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key, valueLoader));
        return (T) o;
    }

}
