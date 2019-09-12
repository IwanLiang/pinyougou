package com.pinyougou.service;

import java.util.Map;

/**
 * 微信支付服务接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-08<p>
 */
public interface WeixinPayService {

    /**
     * 调用微信支付系统的“统一下单接口”
     * 获取code_url，生成二维码
     * @return Map集合
     */
    Map<String,Object> genPayCode(String outTradeNo, String totalFee);

    /**
     * 调用微信支付系统的“查询订单接口”
     * 获取trade_state，判断支付状态
     * @return Map集合
     */
    Map<String,String> queryPayStatus(String outTradeNo);

    /**
     * 调用微信支付系统的“关闭订单接口”
     * 获取result_code，判断关单状态
     * @return Map集合
     */
    Map<String,String> closePayTimeout(String outTradeNo);
}
