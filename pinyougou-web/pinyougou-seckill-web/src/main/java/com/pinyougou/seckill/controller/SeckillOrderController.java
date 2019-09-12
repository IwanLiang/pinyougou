package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-10<p>
 */
@RestController
@RequestMapping("/order")
public class SeckillOrderController {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /** 秒杀下单 */
    @GetMapping("/submitOrder")
    public boolean submitOrder(Long id, HttpServletRequest request){
        try{
            // 获取登录用户名
            String userId = request.getRemoteUser();
            seckillOrderService.saveSeckillOrderToRedis(userId, id);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 生成微信支付二维码 */
    @GetMapping("/genPayCode")
    public Map<String,Object> genPayCode(HttpServletRequest request){
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 从Redis获取支付的秒杀订单
        SeckillOrder seckillOrder = seckillOrderService.findSeckillFromRedis(userId);
        long totalFee  = (long)(seckillOrder.getMoney().doubleValue() * 100);
        return weixinPayService.genPayCode(seckillOrder.getId().toString(),
                String.valueOf(totalFee));
    }


    /** 检测支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String,Integer> queryPayStatus(String outTradeNo, HttpServletRequest request){
        Map<String,Integer> data = new HashMap<>();
        data.put("status", 3);

        try{
            Map<String,String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            if (resMap != null && resMap.size() > 0){
                // 判断支付状态: SUCCESS—支付成功
                if ("SUCCESS".equals(resMap.get("trade_state"))){
                    String userId = request.getRemoteUser();
                    // 业务处理
                    seckillOrderService.saveOrder(userId, resMap.get("transaction_id"));

                    data.put("status", 1);
                }
                // NOTPAY: 未支付
                if ("NOTPAY".equals(resMap.get("trade_state"))){
                    data.put("status", 2);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }

}
