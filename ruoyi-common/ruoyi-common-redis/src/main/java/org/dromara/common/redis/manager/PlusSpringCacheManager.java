/**
 * Copyright (c) 2013-2021 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dromara.common.redis.manager;

import org.dromara.common.redis.utils.RedisUtils;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link org.springframework.cache.CacheManager} implementation
 * backed by Redisson instance.
 * <p>
 * 修改 RedissonSpringCacheManager 源码
 * 重写 cacheName 处理方法 支持多参数
 *
 * @author Nikita Koksharov
 *
 */
@SuppressWarnings("unchecked")
public class PlusSpringCacheManager implements CacheManager {

    private boolean dynamic = true;

    // 防止缓存穿透
    private boolean allowNullValues = true;

    private boolean transactionAware = true;

    // 缓存组 配置集合（缓存分组名：）
    Map<String, CacheConfig> configMap = new ConcurrentHashMap<>();
    // 缓存组 实例集合（一个实例多个kv缓存）
    ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();

    /**
     * Creates CacheManager supplied by Redisson instance
     */
    public PlusSpringCacheManager() {
    }


    /**
     * Defines possibility of storing {@code null} values.
     * <p>
     * Default is <code>true</code>
     *
     * @param allowNullValues stores if <code>true</code>
     */
    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    /**
     * Defines if cache aware of Spring-managed transactions.
     * If {@code true} put/evict operations are executed only for successful transaction in after-commit phase.
     * <p>
     * Default is <code>false</code>
     *
     * @param transactionAware cache is transaction aware if <code>true</code>
     */
    public void setTransactionAware(boolean transactionAware) {
        this.transactionAware = transactionAware;
    }

    /**
     * Defines 'fixed' cache names.
     * A new cache instance will not be created in dynamic for non-defined names.
     * <p>
     * `null` parameter setups dynamic mode
     *
     * @param names of caches
     */
    public void setCacheNames(Collection<String> names) {
        if (names != null) {
            for (String name : names) {
                getCache(name);
            }
            // 不动态使用缓存分组名称
            dynamic = false;
        } else {
            dynamic = true;
        }
    }

    /**
     * Set cache config mapped by cache name
     *
     * @param config object
     */
    public void setConfig(Map<String, ? extends CacheConfig> config) {
        this.configMap = (Map<String, CacheConfig>) config;
    }

    protected CacheConfig createDefaultConfig() {
        return new CacheConfig();
    }

    /**
     *
     * @param name 缓存分组名称
     *       由4部分组成：名称、过期时间、最大空闲时间、条数
     *             如test#0#0#0 表示不会过期、最大空闲时间无限、不限条数
     * @return
     */
    @Override
    public Cache getCache(String name) {
        // 重写 cacheName 支持多参数
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];

        Cache cache = instanceMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!dynamic) {
            return cache;
        }

        CacheConfig config = configMap.get(name);
        if (config == null) {
            // 0#0#0 表示不会过期、最大空闲时间无限、不限条数
            config = createDefaultConfig();
            configMap.put(name, config);
        }

        if (array.length > 1) {
            config.setTTL(DurationStyle.detectAndParse(array[1]).toMillis());
        }
        if (array.length > 2) {
            config.setMaxIdleTime(DurationStyle.detectAndParse(array[2]).toMillis());
        }
        if (array.length > 3) {
            config.setMaxSize(Integer.parseInt(array[3]));
        }
        int local = 1;
        if (array.length > 4) {
            local = Integer.parseInt(array[4]);
        }

        // 创建分组
        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {
            return createMap(name, config, local);
        }

        return createMapCache(name, config, local);
    }

    /**
     * 创建cache分组，无限制
     * @param name
     * @param config
     * @return
     */
    private Cache createMap(String name, CacheConfig config, int local) {
        // 从redis中读取缓存组或新建一个
        RMap<Object, Object> map = RedisUtils.getClient().getMap(name);

        Cache cache = new RedissonCache(map, allowNullValues);
        if (local == 1) {
            // 没有限制
            cache = new CaffeineCacheDecorator(name, cache);
        }
        if (transactionAware) {
            // 会考虑事务
            cache = new TransactionAwareCacheDecorator(cache);
        }
        // 有历史的分组就直接使用历史分组，放弃新建（有时返回旧值，无时返回null）
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        // 返回name对应的实例
        return cache;
    }

    /**
     * 创建cache分组，根据配置设置，会有最大长度限制
     * @param name
     * @param config
     * @return
     */
    private Cache createMapCache(String name, CacheConfig config, int local) {
        RMapCache<Object, Object> map = RedisUtils.getClient().getMapCache(name);

        // 将config配置设置到redis缓存中
        Cache cache = new RedissonCache(map, config, allowNullValues);
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            // todo ? 为什么要重新设置一次
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    /**
     * 获取缓存分组名称集合
     * @return
     */
    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(configMap.keySet());
    }


}
