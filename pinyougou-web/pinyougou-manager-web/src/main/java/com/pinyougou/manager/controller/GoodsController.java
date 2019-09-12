package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-24<p>
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;

    /** 多条件分页查询待审核的商品 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Goods goods, Integer page,
                                 @RequestParam(defaultValue = "10")Integer rows){
        //设置查询的状态码：未审核
        goods.setAuditStatus("0");
        return goodsService.findByPage(goods, page, rows);
    }

    /** 商品审核 */
    @GetMapping("/updateStatus")
    public boolean updateStatus(Long[] ids, String status){
        try{
            goodsService.updateStatus("audit_status", ids, status);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 商品删除(修改删除状态) */
    @GetMapping("/delete")
    public boolean delete(Long[] ids){
        try{
            goodsService.updateStatus("is_delete", ids, "1");
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}
