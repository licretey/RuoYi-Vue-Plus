package org.dromara.common.timezone.core;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.dromara.common.timezone.utils.TimeZoneUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 时区感知的实体基类
 * 提供时区感知的时间字段处理
 *
 * @author YourName
 */
@Data
public class TimeZoneAwareEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索值
     */
    @JsonIgnore
    @TableField(exist = false)
    private String searchValue;

    /**
     * 创建部门
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createDept;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间（数据库存储UTC时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间（数据库存储UTC时间）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

    // ========== 时区感知的虚拟字段 ==========

    /**
     * 用户时区的创建时间（仅用于JSON序列化）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public String getCreateTimeInUserZone() {
        return TimeZoneUtils.formatDateInUserZone(createTime);
    }

    /**
     * 用户时区的更新时间（仅用于JSON序列化）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public String getUpdateTimeInUserZone() {
        return TimeZoneUtils.formatDateInUserZone(updateTime);
    }

    /**
     * 获取创建时间的用户时区ZonedDateTime
     */
    @JsonIgnore
    public ZonedDateTime getCreateTimeZoned() {
        return TimeZoneUtils.dateToUserZone(createTime);
    }

    /**
     * 获取更新时间的用户时区ZonedDateTime
     */
    @JsonIgnore
    public ZonedDateTime getUpdateTimeZoned() {
        return TimeZoneUtils.dateToUserZone(updateTime);
    }

    /**
     * 获取创建时间的UTC Instant
     */
    @JsonIgnore
    public Instant getCreateTimeInstant() {
        return createTime != null ? createTime.toInstant() : null;
    }

    /**
     * 获取更新时间的UTC Instant
     */
    @JsonIgnore
    public Instant getUpdateTimeInstant() {
        return updateTime != null ? updateTime.toInstant() : null;
    }

    /**
     * 设置创建时间（从用户时区字符串）
     */
    @JsonIgnore
    public void setCreateTimeFromUserZone(String timeStr) {
        this.createTime = TimeZoneUtils.parseToDate(timeStr);
    }

    /**
     * 设置更新时间（从用户时区字符串）
     */
    @JsonIgnore
    public void setUpdateTimeFromUserZone(String timeStr) {
        this.updateTime = TimeZoneUtils.parseToDate(timeStr);
    }

    /**
     * 设置创建时间（从Instant）
     */
    @JsonIgnore
    public void setCreateTimeFromInstant(Instant instant) {
        this.createTime = instant != null ? Date.from(instant) : null;
    }

    /**
     * 设置更新时间（从Instant）
     */
    @JsonIgnore
    public void setUpdateTimeFromInstant(Instant instant) {
        this.updateTime = instant != null ? Date.from(instant) : null;
    }

    /**
     * 获取当前用户时区信息（用于调试）
     */
    @JsonIgnore
    public String getCurrentTimeZoneInfo() {
        return String.format("Zone: %s, Offset: %s",
            TimeZoneContext.getTimeZone().getId(),
            TimeZoneUtils.getCurrentUserZoneOffset());
    }
}
