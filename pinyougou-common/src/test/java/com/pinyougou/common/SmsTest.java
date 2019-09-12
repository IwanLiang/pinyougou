package com.pinyougou.common;

import com.pinyougou.common.util.HttpClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信接口调用测试
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-02<p>
 */
public class SmsTest {

    public static void main(String[] args){
        // 创建HttpClientUtils对象
        HttpClientUtils httpClientUtils = new HttpClientUtils(false);
        // 定义Map集合封装请求参数
        Map<String,String> params = new HashMap<>();
        // 设置请求参数
        params.put("phone", "13138859773");
        params.put("signName", "五子连珠");
        params.put("templateCode", "SMS_11480310");
        params.put("templateParam", "{'number' : '8666'}");
        // 调用接口
        String jsonStr = httpClientUtils.sendPost("http://sms.pinyougou.com/sms/sendSms", params);
        System.out.println("jsonStr = " + jsonStr);
    }

}
