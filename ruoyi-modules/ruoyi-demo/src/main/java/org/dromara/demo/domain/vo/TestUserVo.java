package org.dromara.demo.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 测试用户视图对象
 * 用于缓存严格同步功能测试
 *
 * @author Lion Li
 */
@Data
public class TestUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 备注
     */
    private String remark;
}
