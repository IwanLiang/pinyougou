package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-08<p>
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    // 微信公众账号或开放平台APP的唯一标识
    @Value("${appid}")
    private String appid;
    // 商户账号
    @Value("${partner}")
    private String partner;
    // 商户密钥 MD5
    @Value("${partnerkey}")
    private String partnerkey;

    // 统一下单接口URL
    @Value("${unifiedorder}")
    private String unifiedorder;
    // 查询订单接口URL
    @Value("${orderquery}")
    private String orderquery;
    // 关闭订单接口URL
    @Value("${closeorder}")
    private String closeorder;

    /**
     * 调用微信支付系统的“统一下单接口”
     * 获取code_url
     * @return Map集合
     */
    public Map<String,Object> genPayCode(String outTradeNo, String totalFee){
        try{
            // 1. 定义Map集合封装请求参数
            Map<String,String> params = new HashMap<>();
            // 公众账号ID 	appid
            params.put("appid", appid);
            // 商户号	mch_id
            params.put("mch_id", partner);
            // 随机字符串	nonce_str
            params.put("nonce_str", WXPayUtil.generateNonceStr());
            // 商品描述	body
            params.put("body", "品优购");
            // 商户订单号	out_trade_no
            params.put("out_trade_no", outTradeNo);
            // 标价金额	total_fee (订单总金额，单位为分)
            params.put("total_fee", totalFee);
            // 终端IP	spbill_create_ip
            params.put("spbill_create_ip", "127.0.0.1");
            // 通知地址	notify_url
            params.put("notify_url", "http://www.pinyougou.com");
            // 交易类型	trade_type (NATIVE -Native支付)
            params.put("trade_type", "NATIVE");

            // 生成带签名	sign的xml参数
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);


            // 2. 用HttpClient调用统一下单接口，得到响应数据
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String xmlData = httpClientUtils.sendPost(unifiedorder, xmlParam);
            System.out.println("响应数据：" + xmlData);


            // 3. 定义Map集合封装方法的返回值
            // 把xml转化成Map集合
            Map<String, String> resMap = WXPayUtil.xmlToMap(xmlData);
            // {outTradeNo : '', totalFee : 0, codeUrl : ''}
            Map<String,Object> data = new HashMap<>();
            data.put("outTradeNo", outTradeNo);
            data.put("totalFee", totalFee);
            // 二维码链接	code_url
            data.put("codeUrl", resMap.get("code_url"));
            return data;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 调用微信支付系统的“查询订单接口”
     * 获取trade_state，判断支付状态
     * @return Map集合
     */
    public Map<String,String> queryPayStatus(String outTradeNo){
        try{
            // 1. 定义Map集合封装请求参数
            Map<String,String> params = new HashMap<>();
            // 公众账号ID 	appid
            params.put("appid", appid);
            // 商户号	mch_id
            params.put("mch_id", partner);
            // 商户订单号	out_trade_no
            params.put("out_trade_no", outTradeNo);
            // 随机字符串	nonce_str
            params.put("nonce_str", WXPayUtil.generateNonceStr());

            // 生成带签名	sign的xml参数
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);


            // 2. 用HttpClient调用查询订单接口，得到响应数据
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String xmlData = httpClientUtils.sendPost(orderquery, xmlParam);
            System.out.println("响应数据：" + xmlData);


            // 3. 定义Map集合封装方法的返回值
            // 把xml转化成Map集合
            return WXPayUtil.xmlToMap(xmlData);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 调用微信支付系统的“关闭订单接口”
     * 获取result_code，判断关单状态
     * @return Map集合
     */
    public Map<String,String> closePayTimeout(String outTradeNo){
        try{
            // 1. 定义Map集合封装请求参数
            Map<String,String> params = new HashMap<>();
            // 公众账号ID 	appid
            params.put("appid", appid);
            // 商户号	mch_id
            params.put("mch_id", partner);
            // 商户订单号	out_trade_no
            params.put("out_trade_no", outTradeNo);
            // 随机字符串	nonce_str
            params.put("nonce_str", WXPayUtil.generateNonceStr());

            // 生成带签名	sign的xml参数
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);


            // 2. 用HttpClient调用查询订单接口，得到响应数据
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String xmlData = httpClientUtils.sendPost(closeorder, xmlParam);
            System.out.println("响应数据：" + xmlData);


            // 3. 定义Map集合封装方法的返回值
            // 把xml转化成Map集合
            return WXPayUtil.xmlToMap(xmlData);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
