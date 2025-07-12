package org.dromara.common.timezone.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.dromara.common.timezone.annotation.TimeZoneConvert;
import org.dromara.common.timezone.core.TimeZoneContext;
import org.dromara.common.timezone.utils.TimeZoneUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * 时区转换切面
 * 自动处理方法参数和返回值的时区转换
 * 
 * @author YourName
 */
@Slf4j
@Aspect
@Component
@Order(-50) // 确保在其他切面之前执行
public class TimeZoneAspect {
    
    @Around("@annotation(timeZoneConvert) || @within(timeZoneConvert)")
    public Object around(ProceedingJoinPoint joinPoint, TimeZoneConvert timeZoneConvert) throws Throwable {
        if (timeZoneConvert == null || !timeZoneConvert.enabled()) {
            return joinPoint.proceed();
        }
        
        try {
            // 处理输入参数的时区转换
            Object[] args = joinPoint.getArgs();
            processInputArgs(args, timeZoneConvert);
            
            // 执行目标方法
            Object result = joinPoint.proceed(args);
            
            // 处理返回值的时区转换
            return processOutputResult(result, timeZoneConvert);
            
        } catch (Exception e) {
            log.error("时区转换处理失败: {}", e.getMessage(), e);
            // 如果时区转换失败，继续执行原方法
            return joinPoint.proceed();
        }
    }
    
    /**
     * 处理输入参数的时区转换
     */
    private void processInputArgs(Object[] args, TimeZoneConvert annotation) {
        if (args == null || args.length == 0) {
            return;
        }
        
        for (Object arg : args) {
            if (arg != null) {
                convertObjectTimeFields(arg, annotation.inputMode(), true);
            }
        }
    }
    
    /**
     * 处理返回值的时区转换
     */
    private Object processOutputResult(Object result, TimeZoneConvert annotation) {
        if (result == null) {
            return result;
        }
        
        convertObjectTimeFields(result, annotation.outputMode(), false);
        return result;
    }
    
    /**
     * 转换对象中的时间字段
     */
    private void convertObjectTimeFields(Object obj, Object mode, boolean isInput) {
        if (obj == null) {
            return;
        }
        
        Class<?> clazz = obj.getClass();
        
        // 处理集合类型
        if (obj instanceof Collection<?> collection) {
            for (Object item : collection) {
                convertObjectTimeFields(item, mode, isInput);
            }
            return;
        }
        
        // 处理Map类型
        if (obj instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                convertObjectTimeFields(value, mode, isInput);
            }
            return;
        }
        
        // 处理基本类型和包装类型
        if (clazz.isPrimitive() || clazz.getPackage().getName().startsWith("java.lang")) {
            return;
        }
        
        // 处理自定义对象
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                
                if (fieldValue instanceof Date date) {
                    Date convertedDate = convertDate(date, mode, isInput);
                    field.set(obj, convertedDate);
                } else if (fieldValue instanceof Instant instant) {
                    Instant convertedInstant = convertInstant(instant, mode, isInput);
                    field.set(obj, convertedInstant);
                } else if (fieldValue instanceof ZonedDateTime zonedDateTime) {
                    ZonedDateTime convertedZdt = convertZonedDateTime(zonedDateTime, mode, isInput);
                    field.set(obj, convertedZdt);
                } else if (fieldValue != null) {
                    // 递归处理嵌套对象
                    convertObjectTimeFields(fieldValue, mode, isInput);
                }
            } catch (Exception e) {
                log.debug("转换字段 {} 失败: {}", field.getName(), e.getMessage());
            }
        }
    }
    
    /**
     * 转换Date类型
     */
    private Date convertDate(Date date, Object mode, boolean isInput) {
        if (date == null) {
            return null;
        }
        
        if (isInput) {
            // 输入转换：假设输入为用户时区，转换为UTC存储
            if (mode instanceof TimeZoneConvert.InputMode inputMode) {
                switch (inputMode) {
                    case USER_ZONE:
                        // 假设Date表示用户时区时间，需要转换为UTC
                        Instant userInstant = TimeZoneUtils.parseFromUserZone(
                            TimeZoneUtils.formatDateInUserZone(date));
                        return Date.from(userInstant);
                    case UTC:
                    case AUTO:
                    default:
                        return date; // 保持不变
                }
            }
        } else {
            // 输出转换：从UTC转换为目标时区
            if (mode instanceof TimeZoneConvert.OutputMode outputMode) {
                switch (outputMode) {
                    case USER_ZONE:
                        // 保持Date不变，通过虚拟字段提供用户时区显示
                        return date;
                    case UTC:
                    case ORIGINAL:
                    default:
                        return date;
                }
            }
        }
        
        return date;
    }
    
    /**
     * 转换Instant类型
     */
    private Instant convertInstant(Instant instant, Object mode, boolean isInput) {
        // Instant本身就是UTC时间，通常不需要转换
        return instant;
    }
    
    /**
     * 转换ZonedDateTime类型
     */
    private ZonedDateTime convertZonedDateTime(ZonedDateTime zdt, Object mode, boolean isInput) {
        if (zdt == null) {
            return null;
        }
        
        if (!isInput && mode instanceof TimeZoneConvert.OutputMode outputMode) {
            switch (outputMode) {
                case USER_ZONE:
                    return zdt.withZoneSameInstant(TimeZoneContext.getTimeZone());
                case UTC:
                    return zdt.withZoneSameInstant(TimeZoneContext.DEFAULT_ZONE);
                case SYSTEM:
                    return zdt.withZoneSameInstant(TimeZoneContext.SYSTEM_ZONE);
                default:
                    return zdt;
            }
        }
        
        return zdt;
    }
}
