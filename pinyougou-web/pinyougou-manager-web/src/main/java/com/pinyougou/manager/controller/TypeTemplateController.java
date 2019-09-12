package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 类型模板控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-17<p>
 */
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    private TypeTemplateService typeTemplateService;

    /** 多条件分页类型模板 */
    @GetMapping("/findByPage")
    public PageResult findByPage(TypeTemplate typeTemplate, Integer page,
                                 @RequestParam(defaultValue = "10") Integer rows){
        return typeTemplateService.findByPage(typeTemplate, page, rows);
    }

    /** 添加类型模板 */
    @PostMapping("/save")
    public boolean save(@RequestBody TypeTemplate typeTemplate){
        try{
            typeTemplateService.save(typeTemplate);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 修改类型模板 */
    @PostMapping("/update")
    public boolean update(@RequestBody TypeTemplate typeTemplate){
        try{
            typeTemplateService.update(typeTemplate);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 删除类型模板 */
    @GetMapping("/delete")
    public boolean delete(Long[] ids){
        try{
            typeTemplateService.deleteAll(ids);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询类型模板(id与name) */
    @GetMapping("/findTypeTemplateList")
    public List<Map<String, Object>> findTypeTemplateList(){
        return typeTemplateService.findAllByIdAndName();
    }

}
