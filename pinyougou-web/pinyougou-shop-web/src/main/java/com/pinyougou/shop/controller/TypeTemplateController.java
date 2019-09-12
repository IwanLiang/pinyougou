package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 类型模板控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-21<p>
 */
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    private TypeTemplateService typeTemplateService;

    /** 根据主键id查询 */
    @GetMapping("/findOne")
    public TypeTemplate findOne(Long id){
        return typeTemplateService.findOne(id);
    }

    /** 根据主键id查询规格选项数据 */
    @GetMapping("/findSpecOptionsByTemplateId")
    public List<Map> findSpecOptionsByTemplateId(Long id){
        return typeTemplateService.findSpecOptionsByTemplateId(id);
    }

}
