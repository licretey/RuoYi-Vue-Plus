package org.sl.base.domain.vo;

import java.math.BigDecimal;
import org.sl.base.domain.Location;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 地址视图对象 es_tms_location
 *
 * @author Sen
 * @date 2025-05-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Location.class)
public class LocationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 类型
     */
    @ExcelProperty(value = "类型")
    private String locationType;

    /**
     * 子类型
     */
    @ExcelProperty(value = "子类型")
    private String category;

    /**
     * 编码
     */
    @ExcelProperty(value = "编码")
    private String code;

    /**
     * 名称
     */
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 联系人
     */
    @ExcelProperty(value = "联系人")
    private String contacts;

    /**
     * 城市
     */
    @ExcelProperty(value = "城市")
    private String city;

    /**
     * 城市id
     */
    @ExcelProperty(value = "城市id")
    private Long cityId;

    /**
     * 省
     */
    @ExcelProperty(value = "省")
    private String province;

    /**
     * 省id
     */
    @ExcelProperty(value = "省id")
    private Long provinceId;

    /**
     * 省编码
     */
    @ExcelProperty(value = "省编码")
    private String provinceCode;

    /**
     * 邮编
     */
    @ExcelProperty(value = "邮编")
    private String postCode;

    /**
     * 分邮编
     */
    @ExcelProperty(value = "分邮编")
    private String postCode2;

    /**
     * 地址1
     */
    @ExcelProperty(value = "地址1")
    private String address1;

    /**
     * 地址2
     */
    @ExcelProperty(value = "地址2")
    private String address2;

    /**
     * 地址3
     */
    @ExcelProperty(value = "地址3")
    private String address3;

    /**
     * 国家代码
     */
    @ExcelProperty(value = "国家代码")
    private Long country;

    /**
     * 国家代码
     */
    @ExcelProperty(value = "国家代码")
    private String countryName;

    /**
     * 国家代码
     */
    @ExcelProperty(value = "国家代码")
    private String countryCode;

    /**
     * 国家id
     */
    @ExcelProperty(value = "国家id")
    private String countryId;

    /**
     * 区域1
     */
    @ExcelProperty(value = "区域1")
    private String zone1;

    /**
     * 区域2
     */
    @ExcelProperty(value = "区域2")
    private String zone2;

    /**
     * 区域3
     */
    @ExcelProperty(value = "区域3")
    private String zone3;

    /**
     * 区域4
     */
    @ExcelProperty(value = "区域4")
    private String zone4;

    /**
     * 时区
     */
    @ExcelProperty(value = "时区")
    private String timeZone;

    /**
     * 纬度
     */
    @ExcelProperty(value = "纬度")
    private BigDecimal lat;

    /**
     * 经度
     */
    @ExcelProperty(value = "经度")
    private BigDecimal lon;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * Domain_name域名
     */
    @ExcelProperty(value = "Domain_name域名")
    private String domainName;

    /**
     * 公司
     */
    @ExcelProperty(value = "公司")
    private String company;

    /**
     * 联系人电话
     */
    @ExcelProperty(value = "联系人电话")
    private String tel;

    /**
     * 审核状态
     */
    @ExcelProperty(value = "审核状态")
    private String judgeStatus;

    /**
     * 是否坏掉
     */
    @ExcelProperty(value = "是否坏掉")
    private Boolean isBad;

    /**
     * 仓库类型
     */
    @ExcelProperty(value = "仓库类型")
    private String warehouseType;

    /**
     * 仓库面积
     */
    @ExcelProperty(value = "仓库面积")
    private BigDecimal area;

    /**
     * 库位数
     */
    @ExcelProperty(value = "库位数")
    private Long warehouseQty;

    /**
     * 装车月台数
     */
    @ExcelProperty(value = "装车月台数")
    private Long loadPlatformQty;

    /**
     * 卸货月台数
     */
    @ExcelProperty(value = "卸货月台数")
    private Long unloadPlatformQty;

    /**
     * 客户
     */
    @ExcelProperty(value = "客户")
    private Long client;

    /**
     * 辅助审核信息
     */
    @ExcelProperty(value = "辅助审核信息")
    private String vatNumber;

    /**
     * 审核状态
     */
    @ExcelProperty(value = "审核状态")
    private String auditStatus;

    /**
     * noFBA辅助审核文件
     */
    @ExcelProperty(value = "noFBA辅助审核文件")
    private String noFbaFile;

    /**
     * 是否对wms下发
     */
    @ExcelProperty(value = "是否对wms下发")
    private String isSend;

    /**
     * 是否es保税仓库
     */
    @ExcelProperty(value = "是否es保税仓库")
    private String isBonded;

    /**
     * 结算周期
     */
    @ExcelProperty(value = "结算周期")
    private String dateType;

    /**
     * 结算周期
     */
    @ExcelProperty(value = "结算周期")
    private String startDate;

    /**
     * 是否可选，默认可选
     */
    @ExcelProperty(value = "是否可选，默认可选")
    private String viewable;

    /**
     * 送货单尾
     */
    @ExcelProperty(value = "送货单尾")
    private String singleTail;

    /**
     * $column.columnComment
     */
    @ExcelProperty(value = "${comment}", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "$column.readConverterExp()")
    private String email;

    /**
     * 创建部门
     */
    @ExcelProperty(value = "创建部门")
    private Long createDepart;


}
