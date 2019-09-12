package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.ItemCat;
import com.pinyougou.service.ItemCatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-18<p>
 */
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference(timeout = 10000)
    private ItemCatService itemCatService;

    /** 根据父级id查询商品分类 */
    @GetMapping("/findItemCatByParentId")
    public List<ItemCat> findItemCatByParentId(Long parentId){
        return itemCatService.findItemCatByParentId(parentId);
    }

    /** 添加商品分类 */
    @PostMapping("/save")
    public boolean save(@RequestBody ItemCat itemCat){
        try {
            itemCatService.save(itemCat);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 修改商品分类 */
    @PostMapping("/update")
    public boolean update(@RequestBody ItemCat itemCat){
        try {
            itemCatService.update(itemCat);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 删除商品分类 */
    @GetMapping("/delete")
    public boolean delete(Long[] ids){
        try {
            itemCatService.deleteAll(ids);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

}
