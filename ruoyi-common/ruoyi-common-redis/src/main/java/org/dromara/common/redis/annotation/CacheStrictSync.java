package org.dromara.common.redis.annotation;

import java.lang.annotation.*;

/**
 * 缓存严格同步注解
 * 
 * <p>用于标记需要严格缓存同步的方法。当启用严格同步模式时，
 * 被此注解标记的方法在执行缓存操作后会立即发布同步消息，
 * 确保分布式环境下的缓存一致性。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>关键业务数据的缓存操作</li>
 *   <li>需要强一致性保证的缓存</li>
 *   <li>用户权限、配置等敏感数据的缓存</li>
 * </ul>
 * 
 * <p>性能考虑：</p>
 * <ul>
 *   <li>只在必要时使用，避免过度使用影响性能</li>
 *   <li>可通过配置开关全局控制是否启用</li>
 *   <li>同步操作是异步执行，不会阻塞主流程</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * @CacheStrictSync
 * @CachePut(cacheNames = "user", key = "#user.id")
 * public User updateUser(User user) {
 *     // 更新用户信息
 *     return userService.update(user);
 * }
 * 
 * @CacheStrictSync(syncType = SyncType.IMMEDIATE)
 * @CacheEvict(cacheNames = "user", key = "#userId")
 * public void deleteUser(Long userId) {
 *     userService.delete(userId);
 * }
 * }</pre>
 *
 * @author Lion Li
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheStrictSync {

    /**
     * 同步类型
     * 
     * @return 同步类型，默认为异步同步
     */
    SyncType syncType() default SyncType.ASYNC;

    /**
     * 缓存名称
     * 
     * <p>如果不指定，将从Spring Cache注解中自动提取</p>
     * 
     * @return 缓存名称数组
     */
    String[] cacheNames() default {};

    /**
     * 同步超时时间（毫秒）
     * 
     * <p>仅在同步类型为SYNC时有效</p>
     * 
     * @return 超时时间，默认3000毫秒
     */
    long timeout() default 3000L;

    /**
     * 是否启用条件同步
     * 
     * <p>支持SpEL表达式，当表达式结果为true时才执行同步</p>
     * 
     * @return SpEL条件表达式
     */
    String condition() default "";

    /**
     * 同步优先级
     * 
     * <p>数值越小优先级越高，用于控制同步消息的处理顺序</p>
     * 
     * @return 优先级，默认为0
     */
    int priority() default 0;

    /**
     * 同步类型枚举
     */
    enum SyncType {
        /**
         * 异步同步（默认）
         * 发布同步消息后立即返回，不等待其他节点处理完成
         */
        ASYNC,

        /**
         * 同步等待
         * 发布同步消息后等待确认，确保至少有一个节点接收到消息
         * 注意：这会增加响应时间，谨慎使用
         */
        SYNC,

        /**
         * 立即同步
         * 优先级最高的同步方式，会立即发布消息并尝试快速传播
         */
        IMMEDIATE
    }
}
