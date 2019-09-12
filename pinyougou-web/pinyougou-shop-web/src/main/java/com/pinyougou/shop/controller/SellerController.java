package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.web.bind.annotation.*;

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

    /** 商家申请入驻 */
    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller){
        try{
            // 设置密码加密、加盐、加迭代次数
            // String algorithmName: MD5、SHA-256、SHA-384、SHA-512
            // Object source,  明文
            // Object salt,  盐
            // int hashIterations 加密次数
            String password = new SimpleHash("MD5", seller.getPassword(),
                    seller.getSellerId(), 5).toHex();
            seller.setPassword(password);
            sellerService.save(seller);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    /**
     * 回显商家数据
     */
    @GetMapping("/show")
    public Seller show(String id){
        try {
            return sellerService.show(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存商家数据
     */
    @PostMapping("/update")
    public boolean update(@RequestBody Seller seller){
        try {
            //更新保存商家数据
            return sellerService.update(seller);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
