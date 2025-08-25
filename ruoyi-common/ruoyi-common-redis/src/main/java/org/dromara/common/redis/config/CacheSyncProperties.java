package org.dromara.common.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 缓存同步配置属性
 *
 * @author Lion Li
 */
@Data
@Component
@ConfigurationProperties(prefix = "cache.sync")
public class CacheSyncProperties {

    /**
     * 是否启用分布式缓存同步
     */
    private boolean enabled = false;

    /**
     * 严格同步配置
     */
    private Strict strict = new Strict();

    /**
     * 严格同步配置类
     */
    @Data
    public static class Strict {

        /**
         * 是否启用严格缓存同步
         */
        private boolean enabled = false;

        /**
         * 严格同步默认超时时间（毫秒）
         */
        private long timeout = 3000L;

        /**
         * 严格同步重试次数
         */
        private int retryCount = 3;

        /**
         * 严格同步批处理大小
         */
        private int batchSize = 100;

        /**
         * 是否启用同步确认机制
         */
        private boolean confirmationEnabled = true;

        /**
         * 确认超时时间（毫秒）
         */
        private long confirmationTimeout = 1000L;

        /**
         * 是否启用优先级队列
         */
        private boolean priorityQueueEnabled = false;

        /**
         * 最大并发同步数
         */
        private int maxConcurrentSync = 10;
    }
}
