package org.sl.base.service;

import org.sl.base.domain.vo.LocationVo;
import org.sl.base.domain.bo.LocationBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 地址Service接口
 *
 * @author Sen
 * @date 2025-05-27
 */
public interface ILocationService {

    /**
     * 查询地址
     *
     * @param id 主键
     * @return 地址
     */
    LocationVo queryById(Long id);

    /**
     * 分页查询地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 地址分页列表
     */
    TableDataInfo<LocationVo> queryPageList(LocationBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的地址列表
     *
     * @param bo 查询条件
     * @return 地址列表
     */
    List<LocationVo> queryList(LocationBo bo);

    /**
     * 新增地址
     *
     * @param bo 地址
     * @return 是否新增成功
     */
    Boolean insertByBo(LocationBo bo);

    /**
     * 修改地址
     *
     * @param bo 地址
     * @return 是否修改成功
     */
    Boolean updateByBo(LocationBo bo);

    /**
     * 校验并批量删除地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
