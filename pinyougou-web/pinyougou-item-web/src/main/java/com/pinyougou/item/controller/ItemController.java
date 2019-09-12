package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.GoodsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-30<p>
 */
@Controller
public class ItemController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;

    /**
     * 根据goodsId查询商品详情信息
     * http://item.pinyougou.com/1028292.html
     * */
    @GetMapping("/{goodsId}")
    public String getGoods(@PathVariable("goodsId")Long goodsId, Model model){
        System.out.println("goodsId = " + goodsId);
        // model: 数据模型 (它是根据视图解析器来决定数据存放在哪里)
        Map<String,Object> dataModel = goodsService.getGoods(goodsId);
        model.addAllAttributes(dataModel);

        return "item";
    }

}
