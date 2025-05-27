package org.sl.base.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.sl.base.domain.vo.LocationVo;
import org.sl.base.domain.bo.LocationBo;
import org.sl.base.service.ILocationService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 地址
 *
 * @author Sen
 * @date 2025-05-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/baseLocation")
public class LocationController extends BaseController {

    private final ILocationService locationService;

    /**
     * 查询地址列表
     */
    @SaCheckPermission("base:baseLocation:list")
    @GetMapping("/list")
    public TableDataInfo<LocationVo> list(LocationBo bo, PageQuery pageQuery) {
        return locationService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出地址列表
     */
    @SaCheckPermission("base:baseLocation:export")
    @Log(title = "地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(LocationBo bo, HttpServletResponse response) {
        List<LocationVo> list = locationService.queryList(bo);
        ExcelUtil.exportExcel(list, "地址", LocationVo.class, response);
    }

    /**
     * 获取地址详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("base:baseLocation:query")
    @GetMapping("/{id}")
    public R<LocationVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(locationService.queryById(id));
    }

    /**
     * 新增地址
     */
    @SaCheckPermission("base:baseLocation:add")
    @Log(title = "地址", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody LocationBo bo) {
        return toAjax(locationService.insertByBo(bo));
    }

    /**
     * 修改地址
     */
    @SaCheckPermission("base:baseLocation:edit")
    @Log(title = "地址", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody LocationBo bo) {
        return toAjax(locationService.updateByBo(bo));
    }

    /**
     * 删除地址
     *
     * @param ids 主键串
     */
    @SaCheckPermission("base:baseLocation:remove")
    @Log(title = "地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(locationService.deleteWithValidByIds(List.of(ids), true));
    }
}
