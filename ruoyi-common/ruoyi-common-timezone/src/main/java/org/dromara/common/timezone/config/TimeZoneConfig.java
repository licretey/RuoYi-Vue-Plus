package org.dromara.common.timezone.config;

import org.dromara.common.timezone.interceptor.TimeZoneInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.ZoneId;

/**
 * 时区自动配置类
 * 
 * @author YourName
 */
@AutoConfiguration
@EnableConfigurationProperties(TimeZoneProperties.class)
public class TimeZoneConfig implements WebMvcConfigurer {
    
    private final TimeZoneProperties timeZoneProperties;
    
    public TimeZoneConfig(TimeZoneProperties timeZoneProperties) {
        this.timeZoneProperties = timeZoneProperties;
    }
    
    @Bean
    public TimeZoneInterceptor timeZoneInterceptor() {
        ZoneId defaultZone = null;
        if (timeZoneProperties.getDefaultZone() != null) {
            try {
                defaultZone = ZoneId.of(timeZoneProperties.getDefaultZone());
            } catch (Exception e) {
                // 使用系统默认时区
            }
        }
        return new TimeZoneInterceptor(defaultZone);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (timeZoneProperties.isEnabled()) {
            registry.addInterceptor(timeZoneInterceptor())
                .addPathPatterns(timeZoneProperties.getIncludePatterns())
                .excludePathPatterns(timeZoneProperties.getExcludePatterns())
                .order(timeZoneProperties.getOrder());
        }
    }
}
