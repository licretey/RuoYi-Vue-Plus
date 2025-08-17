package org.dromara.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.redis.manager.CacheSyncManager;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 缓存同步测试控制器
 * 用于验证分布式缓存同步功能
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/cache-sync")
public class CacheSyncTestController {

    private final CacheSyncManager cacheSyncManager;

    /**
     * 测试缓存读取
     * 使用多级缓存（Caffeine + Redis）
     */
    @Cacheable(cacheNames = "test:sync#60s", key = "#key")
    @GetMapping("/get")
    public R<String> getCache(String key) {
        String value = "数据来源于数据库 - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("从数据库获取数据: key={}, value={}", key, value);
        return R.ok("获取成功", value);
    }

    /**
     * 测试缓存更新
     * 会触发分布式缓存同步
     */
    @CachePut(cacheNames = "test:sync#60s", key = "#key")
    @GetMapping("/put")
    public R<String> putCache(String key, String value) {
        String newValue = value + " - 更新时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("更新缓存: key={}, value={}", key, newValue);
        return R.ok("更新成功", newValue);
    }

    /**
     * 测试缓存清除
     * 会触发分布式缓存同步
     */
    @CacheEvict(cacheNames = "test:sync#60s", key = "#key")
    @GetMapping("/evict")
    public R<Void> evictCache(String key) {
        log.info("清除缓存: key={}", key);
        return R.ok("清除成功");
    }

    /**
     * 测试清除所有缓存
     * 会触发分布式缓存同步
     */
    @CacheEvict(cacheNames = "test:sync#60s", allEntries = true)
    @GetMapping("/clear")
    public R<Void> clearCache() {
        log.info("清除所有缓存");
        return R.ok("清除成功");
    }

    /**
     * 获取当前节点信息
     */
    @GetMapping("/node-info")
    public R<Object> getNodeInfo() {
        return R.ok("节点信息", new Object() {
            public String nodeId = cacheSyncManager.getNodeId();
            public boolean syncEnabled = cacheSyncManager.isSyncEnabled();
            public String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        });
    }

    /**
     * 手动发布缓存同步消息（用于测试）
     */
    @GetMapping("/manual-sync")
    public R<Void> manualSync(String operation, String cacheName, String key) {
        try {
            switch (operation.toLowerCase()) {
                case "evict":
                    cacheSyncManager.publishEvict(cacheName, key);
                    break;
                case "put":
                    cacheSyncManager.publishPut(cacheName, key);
                    break;
                case "clear":
                    cacheSyncManager.publishClear(cacheName);
                    break;
                default:
                    return R.fail("不支持的操作类型: " + operation);
            }
            log.info("手动发布缓存同步消息: operation={}, cacheName={}, key={}", operation, cacheName, key);
            return R.ok("发布成功");
        } catch (Exception e) {
            log.error("发布缓存同步消息失败", e);
            return R.fail("发布失败: " + e.getMessage());
        }
    }

    /**
     * 检查Redis中的缓存数据
     */
    @GetMapping("/check-redis")
    public R<Object> checkRedisCache(String key) {
        String redisKey = "test:sync:" + key;
        Object value = RedisUtils.getCacheObject(redisKey);
        return R.ok("Redis缓存数据", new Object() {
            public String key = redisKey;
            public Object value = value;
            public boolean exists = value != null;
            public String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        });
    }

    /**
     * 模拟高并发缓存访问测试
     */
    @GetMapping("/concurrent-test")
    public R<String> concurrentTest(String key) {
        // 模拟并发访问
        for (int i = 0; i < 10; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    Thread.sleep(100); // 稍微延迟
                    String result = getCache(key + "_" + index).getData();
                    log.info("并发测试 - 线程{}: {}", index, result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        return R.ok("并发测试已启动，请查看日志");
    }
}
