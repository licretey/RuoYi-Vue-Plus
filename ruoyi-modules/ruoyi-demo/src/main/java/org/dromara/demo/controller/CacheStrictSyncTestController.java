package org.dromara.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.redis.annotation.CacheStrictSync;
import org.dromara.common.redis.manager.StrictCacheSyncManager;
import org.dromara.demo.domain.vo.TestUserVo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 缓存严格同步注解测试控制器
 * 演示@CacheStrictSync注解的使用方法
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/cache-strict-sync")
public class CacheStrictSyncTestController {

    private final StrictCacheSyncManager strictCacheSyncManager;

    /**
     * 测试严格同步缓存读取
     * 使用异步严格同步模式
     */
    @CacheStrictSync(syncType = CacheStrictSync.SyncType.ASYNC)
    @Cacheable(cacheNames = "strict:user#300s", key = "#userId")
    @GetMapping("/user/{userId}")
    public R<TestUserVo> getUser(@PathVariable Long userId) {
        // 模拟从数据库加载用户数据
        TestUserVo user = new TestUserVo();
        user.setId(userId);
        user.setName("用户" + userId);
        user.setEmail("user" + userId + "@example.com");
        user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        log.info("从数据库加载用户数据: {}", user);
        return R.ok("获取成功", user);
    }

    /**
     * 测试严格同步缓存更新
     * 使用立即同步模式，确保关键数据的强一致性
     */
    @CacheStrictSync(
        syncType = CacheStrictSync.SyncType.IMMEDIATE,
        priority = 1,
        condition = "#user.id != null"
    )
    @CachePut(cacheNames = "strict:user#300s", key = "#user.id")
    @PutMapping("/user")
    public R<TestUserVo> updateUser(@RequestBody TestUserVo user) {
        // 模拟更新用户数据
        user.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        log.info("更新用户数据: {}", user);
        return R.ok("更新成功", user);
    }

    /**
     * 测试严格同步缓存删除
     * 使用同步等待模式，确保删除操作的可靠性
     */
    @CacheStrictSync(
        syncType = CacheStrictSync.SyncType.SYNC,
        timeout = 5000L,
        priority = 2
    )
    @CacheEvict(cacheNames = "strict:user#300s", key = "#userId")
    @DeleteMapping("/user/{userId}")
    public R<Void> deleteUser(@PathVariable Long userId) {
        log.info("删除用户: {}", userId);
        return R.ok("删除成功");
    }

    /**
     * 测试严格同步清空所有缓存
     * 使用立即同步模式
     */
    @CacheStrictSync(syncType = CacheStrictSync.SyncType.IMMEDIATE)
    @CacheEvict(cacheNames = "strict:user#300s", allEntries = true)
    @DeleteMapping("/users/clear")
    public R<Void> clearAllUsers() {
        log.info("清空所有用户缓存");
        return R.ok("清空成功");
    }

    /**
     * 测试条件严格同步
     * 只有当用户ID大于100时才启用严格同步
     */
    @CacheStrictSync(
        syncType = CacheStrictSync.SyncType.ASYNC,
        condition = "#userId > 100",
        cacheNames = {"strict:user"}
    )
    @Cacheable(cacheNames = "strict:user#300s", key = "#userId")
    @GetMapping("/user/conditional/{userId}")
    public R<TestUserVo> getUserConditional(@PathVariable Long userId) {
        TestUserVo user = new TestUserVo();
        user.setId(userId);
        user.setName("条件用户" + userId);
        user.setEmail("conditional" + userId + "@example.com");
        user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        log.info("条件加载用户数据: {}", user);
        return R.ok("获取成功", user);
    }

    /**
     * 批量操作测试
     * 演示在批量操作中使用严格同步
     */
    @CacheStrictSync(
        syncType = CacheStrictSync.SyncType.ASYNC,
        priority = 0
    )
    @PostMapping("/users/batch-update")
    public R<String> batchUpdateUsers(@RequestBody Long[] userIds) {
        for (Long userId : userIds) {
            // 模拟批量更新，每个操作都会触发严格同步
            TestUserVo user = new TestUserVo();
            user.setId(userId);
            user.setName("批量更新用户" + userId);
            user.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 这里应该调用带有@CachePut的方法，为了演示简化处理
            log.info("批量更新用户: {}", user);
        }
        
        return R.ok("批量更新成功，共处理 " + userIds.length + " 个用户");
    }

    /**
     * 获取严格同步管理器状态
     */
    @GetMapping("/status")
    public R<Object> getStrictSyncStatus() {
        return R.ok("严格同步状态", new Object() {
            public String nodeId = strictCacheSyncManager.getNodeId();
            public boolean strictSyncEnabled = strictCacheSyncManager.isStrictSyncEnabled();
            public String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        });
    }

    /**
     * 性能对比测试
     * 比较普通缓存和严格同步缓存的性能差异
     */
    @GetMapping("/performance-test")
    public R<String> performanceTest(@RequestParam(defaultValue = "100") int iterations) {
        long startTime = System.currentTimeMillis();
        
        // 执行多次缓存操作
        for (int i = 0; i < iterations; i++) {
            getUser((long) (i % 10 + 1));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        String result = String.format("执行 %d 次缓存操作，耗时: %d ms，平均: %.2f ms/次", 
            iterations, duration, (double) duration / iterations);
        
        log.info("性能测试结果: {}", result);
        return R.ok(result);
    }

    /**
     * 测试不同同步类型的响应时间
     */
    @GetMapping("/sync-type-test/{syncType}")
    public R<String> syncTypeTest(@PathVariable String syncType) {
        long startTime = System.currentTimeMillis();
        
        try {
            switch (syncType.toUpperCase()) {
                case "ASYNC":
                    testAsyncSync();
                    break;
                case "SYNC":
                    testSyncSync();
                    break;
                case "IMMEDIATE":
                    testImmediateSync();
                    break;
                default:
                    return R.fail("不支持的同步类型: " + syncType);
            }
        } catch (Exception e) {
            log.error("同步类型测试失败", e);
            return R.fail("测试失败: " + e.getMessage());
        }
        
        long duration = System.currentTimeMillis() - startTime;
        String result = String.format("%s 同步模式测试完成，耗时: %d ms", syncType, duration);
        
        log.info("同步类型测试结果: {}", result);
        return R.ok(result);
    }

    @CacheStrictSync(syncType = CacheStrictSync.SyncType.ASYNC)
    @CachePut(cacheNames = "test:async#60s", key = "'async-test'")
    private String testAsyncSync() {
        return "异步同步测试 - " + LocalDateTime.now();
    }

    @CacheStrictSync(syncType = CacheStrictSync.SyncType.SYNC, timeout = 2000L)
    @CachePut(cacheNames = "test:sync#60s", key = "'sync-test'")
    private String testSyncSync() {
        return "同步等待测试 - " + LocalDateTime.now();
    }

    @CacheStrictSync(syncType = CacheStrictSync.SyncType.IMMEDIATE)
    @CachePut(cacheNames = "test:immediate#60s", key = "'immediate-test'")
    private String testImmediateSync() {
        return "立即同步测试 - " + LocalDateTime.now();
    }
}
