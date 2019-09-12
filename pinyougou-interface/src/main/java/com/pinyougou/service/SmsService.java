package com.pinyougou.service;

/**
 * SmsService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-02<p>
 */
public interface SmsService {

    /**
     * 通用的短信发送方法
     * @param phone 手机号码
     * @param signName 短信签名
     * @param templateCode 模板代号
     * @param templateParam 模板中的参数,要求json格式字符串
     * @return true: 发送成功、false: 发送失败
     */
    boolean sendSms(String phone, String signName,
                    String templateCode, String templateParam);
}
