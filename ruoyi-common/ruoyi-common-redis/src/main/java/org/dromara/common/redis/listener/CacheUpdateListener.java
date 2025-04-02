package org.dromara.common.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.redis.utils.CacheUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis缓存更新监听器
 * 用于监听Redis中的数据变更，并更新本地Caffeine缓存
 */
@Slf4j
@Component
public class CacheUpdateListener implements MessageListener {


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            // 只处理缓存更新频道的消息
            if (!CacheNames.CACHE_UPDATE_CHANNEL.contains(channel.substring(0,CacheNames.CACHE_UPDATE_CHANNEL.length() - 1))) {
                return;
            }
            String key = new String(message.getBody());
            log.info("Received cache update notification for key: {} on channel: {}", key, channel);
            // 使本地Caffeine缓存失效 todo 
            CacheUtils.evict("", key);
        } catch (Exception e) {
            log.error("Error processing cache update notification", e);
        }
    }
}
