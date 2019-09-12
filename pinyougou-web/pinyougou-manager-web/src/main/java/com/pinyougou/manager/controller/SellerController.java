package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-20<p>
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference(timeout = 10000)
    private SellerService sellerService;

    /** 多条件分页查询商家 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Seller seller, Integer page,
                                 @RequestParam(defaultValue = "10")Integer rows){
        return sellerService.findByPage(seller, page, rows);
    }

    /** 修改商家的审核状态 */
    @GetMapping("/updateStatus")
    public boolean updateStatus(String sellerId, String status){
        try {
            sellerService.updateStatus(sellerId, status);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return  false;
    }
}
