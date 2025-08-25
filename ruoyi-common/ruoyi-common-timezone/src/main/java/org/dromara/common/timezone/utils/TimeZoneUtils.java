package org.dromara.common.timezone.utils;

import org.dromara.common.timezone.core.TimeZoneContext;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时区感知的时间工具类
 * 提供多时区支持的时间转换和格式化功能
 * 
 * @author YourName
 */
public class TimeZoneUtils {
    
    /**
     * 标准时间格式
     */
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 获取当前UTC时间
     * 
     * @return UTC时间的Instant
     */
    public static Instant nowUtc() {
        return Instant.now();
    }
    
    /**
     * 获取当前用户时区时间
     * 
     * @return 用户时区的ZonedDateTime
     */
    public static ZonedDateTime nowInUserZone() {
        return ZonedDateTime.now(TimeZoneContext.getTimeZone());
    }
    
    /**
     * 获取当前系统时区时间
     * 
     * @return 系统时区的ZonedDateTime
     */
    public static ZonedDateTime nowInSystemZone() {
        return ZonedDateTime.now(TimeZoneContext.SYSTEM_ZONE);
    }
    
    /**
     * 将UTC时间转换为用户时区时间
     * 
     * @param utcInstant UTC时间
     * @return 用户时区时间
     */
    public static ZonedDateTime toUserZone(Instant utcInstant) {
        if (utcInstant == null) {
            return null;
        }
        return utcInstant.atZone(TimeZoneContext.getTimeZone());
    }
    
    /**
     * 将用户时区时间转换为UTC时间
     * 
     * @param userZoneTime 用户时区时间
     * @return UTC时间
     */
    public static Instant toUtc(ZonedDateTime userZoneTime) {
        if (userZoneTime == null) {
            return null;
        }
        return userZoneTime.toInstant();
    }
    
    /**
     * 将Date转换为用户时区的ZonedDateTime
     * 
     * @param date Date对象
     * @return 用户时区的ZonedDateTime
     */
    public static ZonedDateTime dateToUserZone(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(TimeZoneContext.getTimeZone());
    }
    
    /**
     * 将ZonedDateTime转换为Date
     * 
     * @param zonedDateTime ZonedDateTime对象
     * @return Date对象
     */
    public static Date zonedDateTimeToDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return Date.from(zonedDateTime.toInstant());
    }
    
    /**
     * 格式化时间为用户时区字符串
     * 
     * @param instant UTC时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatInUserZone(Instant instant, String pattern) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime userTime = toUserZone(instant);
        return userTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 格式化时间为用户时区字符串（使用标准格式）
     * 
     * @param instant UTC时间
     * @return 格式化后的字符串
     */
    public static String formatInUserZone(Instant instant) {
        return formatInUserZone(instant, STANDARD_FORMAT);
    }
    
    /**
     * 解析用户时区时间字符串为UTC Instant
     * 
     * @param timeStr 时间字符串
     * @param pattern 格式模式
     * @return UTC时间
     */
    public static Instant parseFromUserZone(String timeStr, String pattern) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
        ZonedDateTime zonedDateTime = localDateTime.atZone(TimeZoneContext.getTimeZone());
        return zonedDateTime.toInstant();
    }
    
    /**
     * 解析用户时区时间字符串为UTC Instant（使用标准格式）
     * 
     * @param timeStr 时间字符串
     * @return UTC时间
     */
    public static Instant parseFromUserZone(String timeStr) {
        return parseFromUserZone(timeStr, STANDARD_FORMAT);
    }
    
    /**
     * 时区转换：从源时区转换到目标时区
     * 
     * @param sourceTime 源时间
     * @param sourceZone 源时区
     * @param targetZone 目标时区
     * @return 目标时区时间
     */
    public static ZonedDateTime convertTimeZone(LocalDateTime sourceTime, ZoneId sourceZone, ZoneId targetZone) {
        if (sourceTime == null) {
            return null;
        }
        ZonedDateTime sourceZonedTime = sourceTime.atZone(sourceZone);
        return sourceZonedTime.withZoneSameInstant(targetZone);
    }
    
    /**
     * 获取时区偏移量字符串
     * 
     * @param zoneId 时区ID
     * @return 偏移量字符串，如 "+08:00"
     */
    public static String getZoneOffset(ZoneId zoneId) {
        if (zoneId == null) {
            return null;
        }
        return zoneId.getRules().getOffset(Instant.now()).toString();
    }
    
    /**
     * 获取当前用户时区偏移量
     * 
     * @return 偏移量字符串
     */
    public static String getCurrentUserZoneOffset() {
        return getZoneOffset(TimeZoneContext.getTimeZone());
    }
    
    /**
     * 判断是否为同一天（用户时区）
     * 
     * @param instant1 时间1
     * @param instant2 时间2
     * @return 是否为同一天
     */
    public static boolean isSameDayInUserZone(Instant instant1, Instant instant2) {
        if (instant1 == null || instant2 == null) {
            return false;
        }
        LocalDate date1 = toUserZone(instant1).toLocalDate();
        LocalDate date2 = toUserZone(instant2).toLocalDate();
        return date1.equals(date2);
    }
    
    /**
     * 获取用户时区的今天开始时间（00:00:00）
     * 
     * @return 今天开始的UTC时间
     */
    public static Instant getTodayStartInUserZone() {
        ZonedDateTime todayStart = ZonedDateTime.now(TimeZoneContext.getTimeZone())
            .toLocalDate()
            .atStartOfDay(TimeZoneContext.getTimeZone());
        return todayStart.toInstant();
    }
    
    /**
     * 获取用户时区的今天结束时间（23:59:59.999）
     * 
     * @return 今天结束的UTC时间
     */
    public static Instant getTodayEndInUserZone() {
        ZonedDateTime todayEnd = ZonedDateTime.now(TimeZoneContext.getTimeZone())
            .toLocalDate()
            .atTime(23, 59, 59, 999_999_999)
            .atZone(TimeZoneContext.getTimeZone());
        return todayEnd.toInstant();
    }
    
    /**
     * 计算两个时间之间的天数差（用户时区）
     * 
     * @param start 开始时间
     * @param end 结束时间
     * @return 天数差
     */
    public static long daysBetweenInUserZone(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        LocalDate startDate = toUserZone(start).toLocalDate();
        LocalDate endDate = toUserZone(end).toLocalDate();
        return Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
    }
    
    /**
     * 兼容性方法：将Date转换为用户时区格式化字符串
     * 
     * @param date Date对象
     * @return 格式化字符串
     */
    public static String formatDateInUserZone(Date date) {
        if (date == null) {
            return null;
        }
        return formatInUserZone(date.toInstant());
    }
    
    /**
     * 兼容性方法：解析时间字符串为Date（假设为用户时区）
     * 
     * @param timeStr 时间字符串
     * @return Date对象
     */
    public static Date parseToDate(String timeStr) {
        Instant instant = parseFromUserZone(timeStr);
        return instant != null ? Date.from(instant) : null;
    }
}
