package com.pinyougou.user.controller;

import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-04<p>
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    /** 获取登录用户名 */
    @GetMapping("/showName")
    public Map<String,String> showName(){
        Pac4jPrincipal principal =(Pac4jPrincipal) SecurityUtils
                .getSubject().getPrincipal();
        Map<String,String> data = new HashMap<>();
        data.put("loginName", principal.getName());
        return data;
    }

}
