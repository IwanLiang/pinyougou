package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规格控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-17<p>
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference(timeout = 10000)
    private SpecificationService specificationService;

    /** 多条件分页查询规格 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Specification specification, Integer page,
                                 @RequestParam(defaultValue = "10") Integer rows){
        return specificationService.findByPage(specification, page, rows);
    }

    /** 添加规格 */
    @PostMapping("/save")
    public boolean save(@RequestBody Specification specification){
        try {
            specificationService.save(specification);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 根据规格id查询规格选项 */
    @GetMapping("/findSpecOptions")
    public List<SpecificationOption> findSpecOptions(Long id){
        return specificationService.findSpecOptions(id);
    }

    /** 修改规格 */
    @PostMapping("/update")
    public boolean update(@RequestBody Specification specification){
        try {
            specificationService.update(specification);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 删除规格 */
    @GetMapping("/delete")
    public boolean delete(Long[] ids){
        try {
            specificationService.deleteAll(ids);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询全部规格 */
    @GetMapping("/findSpecList")
    public List<Map<String,Object>> findSpecList(){
        return specificationService.findAllByIdAndName();
    }


}
