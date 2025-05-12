package org.sl.base.controller;


import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 地址管理
 *
 * @author Michelle.Chung
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/oms/order")
public class OmsOrderController {

    /**
     * 查询地址
     *
     * @param code      地址编码
     */
    @GetMapping("/test")
    public R<Void> sendSimpleMessage(String code) {
        return R.ok();
    }

}
