package com.pinyougou.manager.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-20<p>
 */
@Controller
@RequestMapping("/user")
public class LoginController {

    /** 用户登录 */
    @PostMapping("/login")
    public String login(String username, String password){
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        try {
            // 1. 获取认证的主体
            Subject subject = SecurityUtils.getSubject();

            // 2. 创建用户名与密码的令牌
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);

            // 3. 用户身份认证
            subject.login(token);

            // 4. 判断用户是否认证成功
            if (subject.isAuthenticated()){
                // 重定向到后台主页
                return "redirect:/admin/index.html";
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        // 重定向到登录页面
        return "redirect:/login.html";
    }

    /** 获取登录用户名 */
    @GetMapping("/findLoginName")
    @ResponseBody
    public Map<String,String> findLoginName(){
        // 获取登录用户名
        String loginName = SecurityUtils.getSubject().getPrincipal().toString();
        Map<String,String> data = new HashMap<>();
        data.put("loginName", loginName);
        return data;

    }
}
