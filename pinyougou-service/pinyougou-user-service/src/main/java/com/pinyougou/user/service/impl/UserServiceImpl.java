package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-02<p>
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void save(User user) {
        try{
            // 设置密码MD5加密 commons-codec.jar
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            // 设置注册时间
            user.setCreated(new Date());
            // 设置修改时间
            user.setUpdated(user.getCreated());

            userMapper.insertSelective(user);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public User findOne(Serializable id) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    /** 发送短信验证码 */
    public boolean sendSmsCode(String phone){
        try{
            // 1. 随机生成6位数字验证码
            // 3cf5500e-48ee-41c8-aec2-dc83752b9c32
            String code = UUID.randomUUID().toString().replaceAll("-", "")
                    .replaceAll("[a-z|A-Z]", "").substring(0,6);
            System.out.println("code: " + code);

            // 2. 调用短信接口，发送短信
            // 创建HttpClientUtils对象
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);
            // 定义Map集合封装请求参数
            Map<String,String> params = new HashMap<>();
            // 设置请求参数
            params.put("phone", phone);
            params.put("signName", signName);
            params.put("templateCode", templateCode);
            params.put("templateParam", "{'number' : '"+ code +"'}");
            // 调用接口
            String jsonStr = httpClientUtils.sendPost(smsUrl, params);
            System.out.println("jsonStr = " + jsonStr);

            // 3. 短信验证存储到Redis数据库
            Map<String,Object> resMap = JSON.parseObject(jsonStr, Map.class);
            boolean success = (boolean)resMap.get("success");
            if (success){
                redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            }
            return success;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 检验短信验证码是否正确 */
    public boolean checkSmsCode(String phone, String code){
        try{
            // 从Redis中获取短信验证码
            String oldCode = (String)redisTemplate.boundValueOps(phone).get();
            return  oldCode != null && oldCode.equals(code);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
