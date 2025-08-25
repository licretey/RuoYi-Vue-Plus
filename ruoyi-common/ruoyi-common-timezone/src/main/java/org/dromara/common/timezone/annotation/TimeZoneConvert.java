package org.dromara.common.timezone.annotation;

import java.lang.annotation.*;

/**
 * 时区转换注解
 * 用于标记需要进行时区转换的方法或类
 * 
 * @author YourName
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeZoneConvert {
    
    /**
     * 是否启用时区转换
     */
    boolean enabled() default true;
    
    /**
     * 输入时区转换模式
     * AUTO: 自动检测（默认）
     * USER_ZONE: 假设输入为用户时区
     * UTC: 假设输入为UTC
     * SYSTEM: 假设输入为系统时区
     */
    InputMode inputMode() default InputMode.AUTO;
    
    /**
     * 输出时区转换模式
     * USER_ZONE: 转换为用户时区（默认）
     * UTC: 转换为UTC
     * SYSTEM: 转换为系统时区
     * ORIGINAL: 保持原始格式
     */
    OutputMode outputMode() default OutputMode.USER_ZONE;
    
    /**
     * 需要转换的字段名称（为空则转换所有时间字段）
     */
    String[] fields() default {};
    
    /**
     * 排除的字段名称
     */
    String[] excludeFields() default {};
    
    /**
     * 输入时区模式
     */
    enum InputMode {
        AUTO,       // 自动检测
        USER_ZONE,  // 用户时区
        UTC,        // UTC时区
        SYSTEM      // 系统时区
    }
    
    /**
     * 输出时区模式
     */
    enum OutputMode {
        USER_ZONE,  // 用户时区
        UTC,        // UTC时区
        SYSTEM,     // 系统时区
        ORIGINAL    // 原始格式
    }
}
