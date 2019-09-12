package com.pinyougou.sms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.pinyougou.service.SmsService;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 短信服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-02<p>
 */
@Service
public class SmsServiceImpl implements SmsService {

    // 产品域名
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";
    // 签名KEY
    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    // 签名密钥
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;

    /**
     * 通用的短信发送方法
     * @param phone 手机号码
     * @param signName 短信签名
     * @param templateCode 模板代号
     * @param templateParam 模板中的参数,要求json格式字符串
     * @return true: 发送成功、false: 发送失败
     */
    public boolean sendSms(String phone, String signName,
                    String templateCode, String templateParam){
        try {
            DefaultProfile profile = DefaultProfile.getProfile("default",
                    accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);

            // 请求对象
            CommonRequest request = new CommonRequest();
            // 公共的请求参数
            request.setMethod(MethodType.POST);
            request.setDomain(DOMAIN);
            request.setVersion("2017-05-25");
            request.setAction("SendSms");

            // 设置短信请求参数
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", templateCode);
            request.putQueryParameter("TemplateParam", templateParam);

            // 响应对象
            CommonResponse response = client.getCommonResponse(request);
            // {"Message":"OK","RequestId":"510684C8-F0B7-4B9D-9037-B9DF927F20B4",
            // "BizId":"558025167412899635^0","Code":"OK"}
            Map<String,String> resMap = JSON.parseObject(response.getData(), Map.class);
            System.out.println("resMap = " + resMap);

            return "OK".equals(resMap.get("Code"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
