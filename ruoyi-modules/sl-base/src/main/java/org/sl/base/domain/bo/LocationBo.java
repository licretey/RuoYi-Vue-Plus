package org.sl.base.domain.bo;

import org.sl.base.domain.Location;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 地址业务对象 es_tms_location
 *
 * @author Sen
 * @date 2025-05-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Location.class, reverseConvertGenerate = false)
public class LocationBo extends BaseEntity {

    /**
     * ID
     */
    private Long id;

    /**
     * 类型
     */
    private String locationType;

    /**
     * 子类型
     */
    private String category;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 城市
     */
    private String city;

    /**
     * 城市id
     */
    private Long cityId;

    /**
     * 省
     */
    private String province;

    /**
     * 省id
     */
    private Long provinceId;

    /**
     * 省编码
     */
    private String provinceCode;

    /**
     * 邮编
     */
    private String postCode;

    /**
     * 分邮编
     */
    private String postCode2;

    /**
     * 地址1
     */
    private String address1;

    /**
     * 地址2
     */
    private String address2;

    /**
     * 地址3
     */
    private String address3;

    /**
     * 国家代码
     */
    private Long country;

    /**
     * 国家代码
     */
    private String countryName;

    /**
     * 国家代码
     */
    private String countryCode;

    /**
     * 国家id
     */
    private String countryId;

    /**
     * 区域1
     */
    private String zone1;

    /**
     * 区域2
     */
    private String zone2;

    /**
     * 区域3
     */
    private String zone3;

    /**
     * 区域4
     */
    private String zone4;

    /**
     * 时区
     */
    private String timeZone;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lon;

    /**
     * 备注
     */
    private String remark;

    /**
     * Domain_name域名
     */
    private String domainName;

    /**
     * 公司
     */
    private String company;

    /**
     * 联系人电话
     */
    private String tel;

    /**
     * 审核状态
     */
    private String judgeStatus;

    /**
     * 是否坏掉
     */
    private Boolean isBad;

    /**
     * 仓库类型
     */
    private String warehouseType;

    /**
     * 仓库面积
     */
    private BigDecimal area;

    /**
     * 库位数
     */
    private Long warehouseQty;

    /**
     * 装车月台数
     */
    private Long loadPlatformQty;

    /**
     * 卸货月台数
     */
    private Long unloadPlatformQty;

    /**
     * 客户
     */
    private Long client;

    /**
     * 辅助审核信息
     */
    private String vatNumber;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * noFBA辅助审核文件
     */
    private String noFbaFile;

    /**
     * 是否对wms下发
     */
    private String isSend;

    /**
     * 是否es保税仓库
     */
    private String isBonded;

    /**
     * 结算周期
     */
    private String dateType;

    /**
     * 结算周期
     */
    private String startDate;

    /**
     * 是否可选，默认可选
     */
    private String viewable;

    /**
     * 送货单尾
     */
    private String singleTail;

    /**
     * $column.columnComment
     */
    private String email;

    /**
     * 创建部门
     */
    private Long createDepart;


}
