package com.pinyougou.seckill.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 秒杀任务调度类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-11<p>
 */
@Component
public class SeckillOrderTask {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /**
     * 关闭超时未支付的秒杀订单
     * 任务调度方法
     * cron : 时间表达式(秒 分 小时 日  月 周)
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void closeOrderTask(){

        // 1. 查询超时未支付的秒杀订单
        List<SeckillOrder> seckillOrderList = seckillOrderService.findOrderByTimeout();

        // 2. 调用微信支付系统的关单接口
        if (seckillOrderList.size() > 0){
            seckillOrderList.forEach(seckillOrder -> {
                // 调用服务接口
                Map<String,String> resMap = weixinPayService
                        .closePayTimeout(seckillOrder.getId().toString());
                // 判断关单状态
                if ("SUCCESS".equals(resMap.get("result_code"))){  // 关单成功
                    System.out.println("删除的秒杀订单 ：" + seckillOrder.getId());
                    // 3. 删除超时未支付的订单，恢复库存
                    seckillOrderService.deleteOrderFromRedis(seckillOrder);
                }
            });
        }

    }
}
