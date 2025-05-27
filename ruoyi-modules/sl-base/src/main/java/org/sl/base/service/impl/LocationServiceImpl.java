package org.sl.base.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.sl.base.domain.bo.LocationBo;
import org.sl.base.domain.vo.LocationVo;
import org.sl.base.domain.Location;
import org.sl.base.mapper.LocationMapper;
import org.sl.base.service.ILocationService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 地址Service业务层处理
 *
 * @author Sen
 * @date 2025-05-27
 */
@RequiredArgsConstructor
@Service
public class LocationServiceImpl implements ILocationService {

    private final LocationMapper baseMapper;

    /**
     * 查询地址
     *
     * @param id 主键
     * @return 地址
     */
    @Override
    public LocationVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 地址分页列表
     */
    @Override
    public TableDataInfo<LocationVo> queryPageList(LocationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Location> lqw = buildQueryWrapper(bo);
        Page<LocationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的地址列表
     *
     * @param bo 查询条件
     * @return 地址列表
     */
    @Override
    public List<LocationVo> queryList(LocationBo bo) {
        LambdaQueryWrapper<Location> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Location> buildQueryWrapper(LocationBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Location> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getLocationType()), Location::getLocationType, bo.getLocationType());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), Location::getCategory, bo.getCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getCode()), Location::getCode, bo.getCode());
        lqw.like(StringUtils.isNotBlank(bo.getName()), Location::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getContacts()), Location::getContacts, bo.getContacts());
        lqw.eq(StringUtils.isNotBlank(bo.getCity()), Location::getCity, bo.getCity());
        lqw.eq(bo.getCityId() != null, Location::getCityId, bo.getCityId());
        lqw.eq(StringUtils.isNotBlank(bo.getProvince()), Location::getProvince, bo.getProvince());
        lqw.eq(bo.getProvinceId() != null, Location::getProvinceId, bo.getProvinceId());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceCode()), Location::getProvinceCode, bo.getProvinceCode());
        lqw.eq(StringUtils.isNotBlank(bo.getPostCode()), Location::getPostCode, bo.getPostCode());
        lqw.eq(StringUtils.isNotBlank(bo.getPostCode2()), Location::getPostCode2, bo.getPostCode2());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress1()), Location::getAddress1, bo.getAddress1());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress2()), Location::getAddress2, bo.getAddress2());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress3()), Location::getAddress3, bo.getAddress3());
        lqw.eq(bo.getCountry() != null, Location::getCountry, bo.getCountry());
        lqw.like(StringUtils.isNotBlank(bo.getCountryName()), Location::getCountryName, bo.getCountryName());
        lqw.eq(StringUtils.isNotBlank(bo.getCountryCode()), Location::getCountryCode, bo.getCountryCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCountryId()), Location::getCountryId, bo.getCountryId());
        lqw.eq(StringUtils.isNotBlank(bo.getZone1()), Location::getZone1, bo.getZone1());
        lqw.eq(StringUtils.isNotBlank(bo.getZone2()), Location::getZone2, bo.getZone2());
        lqw.eq(StringUtils.isNotBlank(bo.getZone3()), Location::getZone3, bo.getZone3());
        lqw.eq(StringUtils.isNotBlank(bo.getZone4()), Location::getZone4, bo.getZone4());
        lqw.eq(StringUtils.isNotBlank(bo.getTimeZone()), Location::getTimeZone, bo.getTimeZone());
        lqw.eq(bo.getLat() != null, Location::getLat, bo.getLat());
        lqw.eq(bo.getLon() != null, Location::getLon, bo.getLon());
        lqw.like(StringUtils.isNotBlank(bo.getDomainName()), Location::getDomainName, bo.getDomainName());
        lqw.eq(StringUtils.isNotBlank(bo.getCompany()), Location::getCompany, bo.getCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getTel()), Location::getTel, bo.getTel());
        lqw.eq(StringUtils.isNotBlank(bo.getJudgeStatus()), Location::getJudgeStatus, bo.getJudgeStatus());
        lqw.eq(bo.getIsBad() != null, Location::getIsBad, bo.getIsBad());
        lqw.eq(StringUtils.isNotBlank(bo.getWarehouseType()), Location::getWarehouseType, bo.getWarehouseType());
        lqw.eq(bo.getArea() != null, Location::getArea, bo.getArea());
        lqw.eq(bo.getWarehouseQty() != null, Location::getWarehouseQty, bo.getWarehouseQty());
        lqw.eq(bo.getLoadPlatformQty() != null, Location::getLoadPlatformQty, bo.getLoadPlatformQty());
        lqw.eq(bo.getUnloadPlatformQty() != null, Location::getUnloadPlatformQty, bo.getUnloadPlatformQty());
        lqw.eq(bo.getClient() != null, Location::getClient, bo.getClient());
        lqw.eq(StringUtils.isNotBlank(bo.getVatNumber()), Location::getVatNumber, bo.getVatNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), Location::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getNoFbaFile()), Location::getNoFbaFile, bo.getNoFbaFile());
        lqw.eq(StringUtils.isNotBlank(bo.getIsSend()), Location::getIsSend, bo.getIsSend());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBonded()), Location::getIsBonded, bo.getIsBonded());
        lqw.eq(StringUtils.isNotBlank(bo.getDateType()), Location::getDateType, bo.getDateType());
        lqw.eq(StringUtils.isNotBlank(bo.getStartDate()), Location::getStartDate, bo.getStartDate());
        lqw.eq(StringUtils.isNotBlank(bo.getViewable()), Location::getViewable, bo.getViewable());
        lqw.eq(StringUtils.isNotBlank(bo.getSingleTail()), Location::getSingleTail, bo.getSingleTail());
        lqw.eq(StringUtils.isNotBlank(bo.getEmail()), Location::getEmail, bo.getEmail());
        lqw.eq(bo.getCreateDepart() != null, Location::getCreateDepart, bo.getCreateDepart());
        return lqw;
    }

    /**
     * 新增地址
     *
     * @param bo 地址
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(LocationBo bo) {
        Location add = MapstructUtils.convert(bo, Location.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改地址
     *
     * @param bo 地址
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(LocationBo bo) {
        Location update = MapstructUtils.convert(bo, Location.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Location entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
