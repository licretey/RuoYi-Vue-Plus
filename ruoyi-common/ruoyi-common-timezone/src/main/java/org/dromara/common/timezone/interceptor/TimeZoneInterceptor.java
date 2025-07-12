package org.dromara.common.timezone.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.timezone.core.TimeZoneContext;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.ZoneId;

/**
 * 时区拦截器
 * 自动解析HTTP请求中的时区信息并设置到当前线程上下文
 * 
 * @author YourName
 */
@Slf4j
public class TimeZoneInterceptor implements HandlerInterceptor {
    
    /**
     * 时区请求头名称
     */
    public static final String TIMEZONE_HEADER = "X-Timezone";
    
    /**
     * 时区请求参数名称
     */
    public static final String TIMEZONE_PARAM = "timezone";
    
    /**
     * 时区偏移量请求头名称（如 +08:00）
     */
    public static final String TIMEZONE_OFFSET_HEADER = "X-Timezone-Offset";
    
    /**
     * 默认时区（当无法解析用户时区时使用）
     */
    private final ZoneId defaultZone;
    
    public TimeZoneInterceptor() {
        this.defaultZone = TimeZoneContext.DEFAULT_ZONE;
    }
    
    public TimeZoneInterceptor(ZoneId defaultZone) {
        this.defaultZone = defaultZone != null ? defaultZone : TimeZoneContext.DEFAULT_ZONE;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String timeZone = extractTimeZone(request);
        
        if (StringUtils.isNotBlank(timeZone)) {
            try {
                ZoneId zoneId = parseTimeZone(timeZone);
                TimeZoneContext.setTimeZone(zoneId);
                log.debug("设置请求时区: {} for URI: {}", zoneId, request.getRequestURI());
            } catch (Exception e) {
                log.warn("解析时区失败: {}, 使用默认时区: {}, URI: {}", timeZone, defaultZone, request.getRequestURI());
                TimeZoneContext.setTimeZone(defaultZone);
            }
        } else {
            // 如果没有提供时区信息，使用默认时区
            TimeZoneContext.setTimeZone(defaultZone);
            log.debug("未提供时区信息，使用默认时区: {} for URI: {}", defaultZone, request.getRequestURI());
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理线程本地变量，防止内存泄漏
        TimeZoneContext.clear();
    }
    
    /**
     * 从请求中提取时区信息
     * 优先级：请求头 > 请求参数 > 偏移量请求头
     * 
     * @param request HTTP请求
     * @return 时区字符串
     */
    private String extractTimeZone(HttpServletRequest request) {
        // 1. 优先从请求头获取时区ID（如 Asia/Shanghai）
        String timeZone = request.getHeader(TIMEZONE_HEADER);
        if (StringUtils.isNotBlank(timeZone)) {
            return timeZone.trim();
        }
        
        // 2. 从请求参数获取时区ID
        timeZone = request.getParameter(TIMEZONE_PARAM);
        if (StringUtils.isNotBlank(timeZone)) {
            return timeZone.trim();
        }
        
        // 3. 从偏移量请求头获取（如 +08:00）
        String offset = request.getHeader(TIMEZONE_OFFSET_HEADER);
        if (StringUtils.isNotBlank(offset)) {
            return offset.trim();
        }
        
        return null;
    }
    
    /**
     * 解析时区字符串为ZoneId
     * 
     * @param timeZoneStr 时区字符串
     * @return ZoneId对象
     */
    private ZoneId parseTimeZone(String timeZoneStr) {
        try {
            // 尝试解析为时区ID（如 Asia/Shanghai, UTC, GMT+8）
            return ZoneId.of(timeZoneStr);
        } catch (Exception e) {
            // 如果解析失败，尝试解析为偏移量（如 +08:00, -05:00）
            try {
                if (timeZoneStr.matches("[+-]\\d{2}:?\\d{2}")) {
                    // 标准化偏移量格式
                    if (!timeZoneStr.contains(":")) {
                        // 将 +0800 转换为 +08:00
                        timeZoneStr = timeZoneStr.substring(0, 3) + ":" + timeZoneStr.substring(3);
                    }
                    return ZoneId.of(timeZoneStr);
                }
            } catch (Exception ex) {
                // 忽略解析错误
            }
            
            // 如果都解析失败，抛出异常
            throw new IllegalArgumentException("无法解析时区: " + timeZoneStr);
        }
    }
}
