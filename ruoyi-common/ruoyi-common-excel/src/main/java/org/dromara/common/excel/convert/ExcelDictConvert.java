package org.dromara.common.excel.convert;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 字典格式化转换处理
 *
 * @author Lion Li
 */
@Slf4j
public class ExcelDictConvert implements Converter<Object> {

    @Override
    public Class<Object> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    /**
     *
     * @param cellData 单元格类容
     * @param contentProperty 表格对应的model信息
     * @param globalConfiguration 全局配置信息
     * @return
     */
    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        // 获取字段上的解析注解
        ExcelDictFormat anno = getAnnotation(contentProperty.getField());
        String type = anno.dictType();
        String label = cellData.getStringValue();
        String value;
        if (StringUtils.isBlank(type)) {
            // 不提供字典类型时，使用内容转表达式的方式解析
            value = ExcelUtil.reverseByExp(label, anno.readConverterExp(), anno.separator());
        } else {
            // 提供字典类型直接使用字典解析表格内容
            value = SpringUtils.getBean(DictService.class).getDictValue(type, label, anno.separator());
        }
        // 转换为model指定的类型
        return Convert.convert(contentProperty.getField().getType(), value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNull(object)) {
            return new WriteCellData<>("");
        }
        // 获取字段上的解析注解
        ExcelDictFormat anno = getAnnotation(contentProperty.getField());
        String type = anno.dictType();
        String value = Convert.toStr(object);
        String label;
        if (StringUtils.isBlank(type)) {
            // 字典值转为字典标签
            label = ExcelUtil.convertByExp(value, anno.readConverterExp(), anno.separator());
        } else {
            label = SpringUtils.getBean(DictService.class).getDictLabel(type, value, anno.separator());
        }
        return new WriteCellData<>(label);
    }

    private ExcelDictFormat getAnnotation(Field field) {
        return AnnotationUtil.getAnnotation(field, ExcelDictFormat.class);
    }
}
