package org.dromara.common.timezone.core;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * 时区上下文管理器
 * 
 * @author YourName
 */
public class TimeZoneContext {
    
    /**
     * 默认时区 - UTC
     */
    public static final ZoneId DEFAULT_ZONE = ZoneOffset.UTC;
    
    /**
     * 系统默认时区
     */
    public static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    
    /**
     * 当前线程时区
     */
    private static final ThreadLocal<ZoneId> CURRENT_ZONE = new ThreadLocal<>();
    
    /**
     * 设置当前线程时区
     * 
     * @param zoneId 时区ID
     */
    public static void setTimeZone(ZoneId zoneId) {
        CURRENT_ZONE.set(zoneId);
    }
    
    /**
     * 设置当前线程时区
     * 
     * @param zoneIdStr 时区ID字符串，如 "Asia/Shanghai", "UTC", "+08:00"
     */
    public static void setTimeZone(String zoneIdStr) {
        if (zoneIdStr == null || zoneIdStr.trim().isEmpty()) {
            return;
        }
        try {
            ZoneId zoneId = ZoneId.of(zoneIdStr);
            setTimeZone(zoneId);
        } catch (Exception e) {
            // 如果解析失败，尝试解析为偏移量
            try {
                ZoneOffset offset = ZoneOffset.of(zoneIdStr);
                setTimeZone(offset);
            } catch (Exception ex) {
                // 解析失败，忽略
            }
        }
    }
    
    /**
     * 获取当前线程时区
     * 
     * @return 时区ID，如果未设置则返回默认时区
     */
    public static ZoneId getTimeZone() {
        ZoneId zoneId = CURRENT_ZONE.get();
        return zoneId != null ? zoneId : DEFAULT_ZONE;
    }
    
    /**
     * 获取当前线程时区，如果未设置则返回系统时区
     * 
     * @return 时区ID
     */
    public static ZoneId getTimeZoneOrSystem() {
        ZoneId zoneId = CURRENT_ZONE.get();
        return zoneId != null ? zoneId : SYSTEM_ZONE;
    }
    
    /**
     * 清除当前线程时区
     */
    public static void clear() {
        CURRENT_ZONE.remove();
    }
    
    /**
     * 检查是否设置了时区
     * 
     * @return true-已设置，false-未设置
     */
    public static boolean hasTimeZone() {
        return CURRENT_ZONE.get() != null;
    }
    
    /**
     * 获取时区偏移量字符串
     * 
     * @return 如 "+08:00", "-05:00"
     */
    public static String getTimeZoneOffset() {
        return getTimeZone().getRules().getOffset(java.time.Instant.now()).toString();
    }
}
