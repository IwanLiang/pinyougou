package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Brand;
import com.pinyougou.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 品牌控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-14<p>
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    // 引用远程的服务
    @Reference(timeout = 10000) // alt + enter
    private BrandService brandService;

    /** 查询全部品牌 */
    @GetMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /** 分页查询品牌 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Brand brand, Integer page,
                                 @RequestParam(defaultValue = "10")Integer rows){
        // {pages : 100, rows : [{},{}]}
        // {} Map|实体类
        return brandService.findByPage(brand, page, rows);
    }

    /** 添加品牌 */
    @PostMapping("/save")
    public boolean save(@RequestBody Brand brand){
        try {
            brandService.save(brand);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 修改品牌 */
    @PostMapping("/update")
    public boolean update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 删除品牌 */
    @GetMapping("/delete")
    public boolean delete(Long[] ids){
        try {
            brandService.deleteAll(ids);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询全部品牌(id与name) */
    @GetMapping("/findBrandList")
    public List<Map<String,Object>> findBrandList(){
        // [{id:1, text : '华为'},{id:1, text : '小米'},{id:1, text : '红米'}]
        return brandService.findAllByIdAndName();

    }



}
