package org.dromara.common.timezone.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 时区配置属性
 * 
 * @author YourName
 */
@Data
@ConfigurationProperties(prefix = "timezone")
public class TimeZoneProperties {
    
    /**
     * 是否启用时区功能
     */
    private boolean enabled = true;
    
    /**
     * 默认时区，如果未指定则使用UTC
     */
    private String defaultZone = "UTC";
    
    /**
     * 拦截器顺序
     */
    private int order = -100;
    
    /**
     * 包含的路径模式
     */
    private String[] includePatterns = {"/**"};
    
    /**
     * 排除的路径模式
     */
    private String[] excludePatterns = {
        "/static/**",
        "/public/**",
        "/webjars/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/error"
    };
    
    /**
     * 是否在响应头中返回服务器时区信息
     */
    private boolean includeServerTimeZone = true;
    
    /**
     * 是否启用时区转换日志
     */
    private boolean enableLogging = false;
}
