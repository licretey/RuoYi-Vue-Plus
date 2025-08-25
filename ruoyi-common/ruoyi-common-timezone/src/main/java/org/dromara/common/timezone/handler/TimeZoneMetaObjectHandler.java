package org.dromara.common.timezone.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.timezone.core.TimeZoneAwareEntity;
import org.dromara.common.timezone.utils.TimeZoneUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 时区感知的字段自动填充处理器
 * 继承原有的字段填充功能，增加时区处理
 *
 * @author YourName
 */
@Slf4j
@Component
public class TimeZoneMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (metaObject.getOriginalObject() instanceof TimeZoneAwareEntity entity) {
                // 获取当前UTC时间
                Date currentUtc = Date.from(TimeZoneUtils.nowUtc());

                // 填充创建时间和更新时间（UTC）
                if (entity.getCreateTime() == null) {
                    entity.setCreateTime(currentUtc);
                }
                entity.setUpdateTime(currentUtc);

                // 填充创建人和更新人
                try {
                    Long userId = LoginHelper.getUserId();
                    if (entity.getCreateBy() == null) {
                        entity.setCreateBy(userId);
                    }
                    entity.setUpdateBy(userId);
                } catch (Exception e) {
                    log.warn("获取当前用户ID失败: {}", e.getMessage());
                }

                // 填充创建部门
                try {
                    Long deptId = LoginHelper.getDeptId();
                    if (entity.getCreateDept() == null) {
                        entity.setCreateDept(deptId);
                    }
                } catch (Exception e) {
                    log.warn("获取当前部门ID失败: {}", e.getMessage());
                }

                log.debug("插入时自动填充时间字段，UTC时间: {}, 用户时区: {}",
                    currentUtc, entity.getCurrentTimeZoneInfo());
            }
        } catch (Exception e) {
            log.error("插入时自动填充字段失败", e);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (metaObject.getOriginalObject() instanceof TimeZoneAwareEntity entity) {
                // 填充更新时间（UTC）
                Date currentUtc = Date.from(TimeZoneUtils.nowUtc());
                entity.setUpdateTime(currentUtc);

                // 填充更新人
                try {
                    Long userId = LoginHelper.getUserId();
                    entity.setUpdateBy(userId);
                } catch (Exception e) {
                    log.warn("获取当前用户ID失败: {}", e.getMessage());
                }

                log.debug("更新时自动填充时间字段，UTC时间: {}, 用户时区: {}",
                    currentUtc, entity.getCurrentTimeZoneInfo());
            }
        } catch (Exception e) {
            log.error("更新时自动填充字段失败", e);
        }
    }
}
