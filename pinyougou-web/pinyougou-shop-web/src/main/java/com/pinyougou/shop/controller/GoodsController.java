package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-21<p>
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Autowired
    private MQProducer mqProducer;

    /** 添加商品 */
    @PostMapping("/save")
    public boolean save(@RequestBody Goods goods){
        try {
            // 获取登录用户名(商家id)
            String sellerId = SecurityUtils.getSubject()
                    .getPrincipal().toString();
            // 设置商品关联的商家
            goods.setSellerId(sellerId);
            // 商品添加
            goodsService.save(goods);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 多条件分页查询当前登录商家的商品 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Goods goods, Integer page,
                                 @RequestParam(defaultValue = "10")Integer rows){
        // 获取登录用户名
        String sellerId = SecurityUtils.getSubject().getPrincipal().toString();
        // 设置商家id
        goods.setSellerId(sellerId);

        return goodsService.findByPage(goods, page, rows);
    }

    /** 商品上下架 */
    @GetMapping("/updateMarketable")
    public boolean updateMarketable(Long[] ids, String status){
        try {
            goodsService.updateStatus("is_marketable", ids, status);
            // 判断上下架状态
            if ("1".equals(status)){ // 上架
                // 发送消息到MQ，生成商品的索引数据
                mqProducer.send(new Message("ES_ITEM_TOPIC", "UPDATE",
                        JSON.toJSONString(ids).getBytes("UTF-8")));

                // 发送消息到MQ，生成商品的静态页面
                mqProducer.send(new Message("PAGE_ITEM_TOPIC", "CREATE",
                        JSON.toJSONString(ids).getBytes("UTF-8")));
            }else { // 下架

                // 发送消息到MQ，删除商品的索引数据
                mqProducer.send(new Message("ES_ITEM_TOPIC", "DELETE",
                        JSON.toJSONString(ids).getBytes("UTF-8")));

                // 发送消息到MQ，删除商品的静态页面
                mqProducer.send(new Message("PAGE_ITEM_TOPIC", "DELETE",
                        JSON.toJSONString(ids).getBytes("UTF-8")));
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

}
