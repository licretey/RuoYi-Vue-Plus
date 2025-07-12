package org.dromara.common.timezone;

import org.dromara.common.timezone.core.TimeZoneContext;
import org.dromara.common.timezone.utils.TimeZoneUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 时区工具类测试
 *
 * @author YourName
 */
class TimeZoneUtilsTest {

    private final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private final ZoneId NEW_YORK_ZONE = ZoneId.of("America/New_York");
    private final ZoneId UTC_ZONE = ZoneOffset.UTC;

    @BeforeEach
    void setUp() {
        // 设置测试时区为北京时间
        TimeZoneContext.setTimeZone(BEIJING_ZONE);
    }

    @AfterEach
    void tearDown() {
        // 清理时区设置
        TimeZoneContext.clear();
    }

    @Test
    void testNowUtc() {
        Instant now = TimeZoneUtils.nowUtc();
        assertNotNull(now);

        // 验证返回的是UTC时间
        Instant systemNow = Instant.now();
        assertTrue(Math.abs(now.toEpochMilli() - systemNow.toEpochMilli()) < 1000);
    }

    @Test
    void testNowInUserZone() {
        ZonedDateTime userNow = TimeZoneUtils.nowInUserZone();
        assertNotNull(userNow);
        assertEquals(BEIJING_ZONE, userNow.getZone());
    }

    @Test
    void testTimeZoneConversion() {
        // 创建一个UTC时间：2023-12-25 12:00:00 UTC
        Instant utcTime = Instant.parse("2023-12-25T12:00:00Z");

        // 转换为北京时间（UTC+8）
        ZonedDateTime beijingTime = TimeZoneUtils.toUserZone(utcTime);
        assertNotNull(beijingTime);
        assertEquals(BEIJING_ZONE, beijingTime.getZone());
        assertEquals(20, beijingTime.getHour()); // 12 + 8 = 20

        // 转换回UTC
        Instant backToUtc = TimeZoneUtils.toUtc(beijingTime);
        assertEquals(utcTime, backToUtc);
    }

    @Test
    void testDateConversion() {
        // 创建一个Date对象
        Date date = Date.from(Instant.parse("2023-12-25T12:00:00Z"));

        // 转换为用户时区
        ZonedDateTime userZoneTime = TimeZoneUtils.dateToUserZone(date);
        assertNotNull(userZoneTime);
        assertEquals(BEIJING_ZONE, userZoneTime.getZone());
        assertEquals(20, userZoneTime.getHour());

        // 转换回Date
        Date backToDate = TimeZoneUtils.zonedDateTimeToDate(userZoneTime);
        assertEquals(date, backToDate);
    }

    @Test
    void testFormatting() {
        Instant utcTime = Instant.parse("2023-12-25T12:00:00Z");

        // 格式化为用户时区字符串
        String formatted = TimeZoneUtils.formatInUserZone(utcTime);
        assertEquals("2023-12-25 20:00:00", formatted);

        // 使用自定义格式
        String customFormatted = TimeZoneUtils.formatInUserZone(utcTime, "yyyy/MM/dd HH:mm");
        assertEquals("2023/12/25 20:00", customFormatted);
    }

    @Test
    void testParsing() {
        // 解析用户时区时间字符串
        String timeStr = "2023-12-25 20:00:00";
        Instant parsed = TimeZoneUtils.parseFromUserZone(timeStr);

        // 应该解析为UTC时间 12:00:00
        assertEquals(Instant.parse("2023-12-25T12:00:00Z"), parsed);
    }

    @Test
    void testSameDayCheck() {
        // 创建两个在UTC时间不同日期但在北京时间同一天的时间
        Instant time1 = Instant.parse("2023-12-25T16:00:00Z"); // 北京时间 2023-12-26 00:00:00
        Instant time2 = Instant.parse("2023-12-25T23:59:59Z"); // 北京时间 2023-12-26 07:59:59

        assertTrue(TimeZoneUtils.isSameDayInUserZone(time1, time2));

        // 测试不同天
        Instant time3 = Instant.parse("2023-12-24T16:00:00Z"); // 北京时间 2023-12-25 00:00:00
        assertFalse(TimeZoneUtils.isSameDayInUserZone(time1, time3));
    }

    @Test
    void testTodayRange() {
        Instant todayStart = TimeZoneUtils.getTodayStartInUserZone();
        Instant todayEnd = TimeZoneUtils.getTodayEndInUserZone();

        assertNotNull(todayStart);
        assertNotNull(todayEnd);
        assertTrue(todayStart.isBefore(todayEnd));

        // 验证今天开始时间是00:00:00
        ZonedDateTime startInUserZone = TimeZoneUtils.toUserZone(todayStart);
        assertEquals(0, startInUserZone.getHour());
        assertEquals(0, startInUserZone.getMinute());
        assertEquals(0, startInUserZone.getSecond());

        // 验证今天结束时间是23:59:59
        ZonedDateTime endInUserZone = TimeZoneUtils.toUserZone(todayEnd);
        assertEquals(23, endInUserZone.getHour());
        assertEquals(59, endInUserZone.getMinute());
        assertEquals(59, endInUserZone.getSecond());
    }

    @Test
    void testDaysBetween() {
        Instant start = Instant.parse("2023-12-25T12:00:00Z");
        Instant end = Instant.parse("2023-12-27T12:00:00Z");

        long days = TimeZoneUtils.daysBetweenInUserZone(start, end);
        assertEquals(2, days);
    }

    @Test
    void testZoneOffset() {
        String offset = TimeZoneUtils.getCurrentUserZoneOffset();
        assertEquals("+08:00", offset); // 北京时间偏移量

        String nyOffset = TimeZoneUtils.getZoneOffset(NEW_YORK_ZONE);
        assertTrue(nyOffset.equals("-05:00") || nyOffset.equals("-04:00")); // 取决于夏令时
    }

    @Test
    void testCompatibilityMethods() {
        Date date = new Date();

        // 测试兼容性格式化方法
        String formatted = TimeZoneUtils.formatDateInUserZone(date);
        assertNotNull(formatted);
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        // 测试兼容性解析方法
        Date parsed = TimeZoneUtils.parseToDate(formatted);
        assertNotNull(parsed);

        // 由于时区转换，解析后的时间可能与原时间有差异，但应该在合理范围内
        long diff = Math.abs(date.getTime() - parsed.getTime());
        assertTrue(diff < 24 * 60 * 60 * 1000); // 差异应小于24小时
    }

    @Test
    void testDifferentTimeZones() {
        // 测试纽约时区
        TimeZoneContext.setTimeZone(NEW_YORK_ZONE);

        Instant utcTime = Instant.parse("2023-12-25T12:00:00Z");
        ZonedDateTime nyTime = TimeZoneUtils.nowInUserZone();

        assertEquals(NEW_YORK_ZONE, nyTime.getZone());

        // 验证时区转换
        ZonedDateTime convertedTime = TimeZoneUtils.toUserZone(utcTime);
        assertEquals(NEW_YORK_ZONE, convertedTime.getZone());
    }
}
