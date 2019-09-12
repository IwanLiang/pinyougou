package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-05<p>
 */
@RestController
@RequestMapping("/cart")
public class CartController {


    @Reference(timeout = 10000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /** 添加SKU商品到用户的购物车 */
    @GetMapping("/addCart")
    @CrossOrigin(origins = {"http://item.pinyougou.com"},
            allowCredentials = "true")
    public boolean addCart(Long itemId, Integer num){

        // 设置允许访问的域名(99%)
//        response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
//        // 设置允许访问Cookie(1%)
//        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            // 1. 获取用户原来的购物车
            List<Cart> carts = findCart();

            // 2. 把用户购买的商品添加到用户的购物车，返回修改后的购物车
            carts = cartService.addItemToCart(carts, itemId, num);


            // 获取登录用户名
            String userId = request.getRemoteUser();
            // 判断用户是否登录
            if (StringUtils.isNoneBlank(userId)){ // 已登录
                /** ####### 已登录的用户，把购物车数据存储到Redis中 ######## */
                cartService.saveCartRedis(userId, carts);

            }else { // 未登录
                /** ####### 未登录的用户，把购物车数据存储到用户浏览器的Cookie ######## */
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts),
                        60 * 60 * 24, true);
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }


    /** 获取用户的购物车 */
    @GetMapping("/findCart")
    public List<Cart> findCart(){
        // 获取登录用户名
        String userId = request.getRemoteUser();

        // 定义用户的购物车集合
        List<Cart> carts = null;

        // 判断用户是否登录
        if (StringUtils.isNoneBlank(userId)){ // 已登录
            /** ######## 已登录的用户从Redis数据库获取购物车数据 ######## */
            carts = cartService.findCartRedis(userId);


            /** ###### 把Cookie中购物车合并到Redis数据库 ####### */
            // 获取cookie中的购物车
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            // 判断是否为空(不是空)
            if (StringUtils.isNoneBlank(cartJsonStr)){
                // 把json数组字符串，转化成List集合
                List<Cart> cookieCarts = JSON.parseArray(cartJsonStr, Cart.class);
                if (cookieCarts != null && cookieCarts.size() > 0){
                    // 合并购物车，返回合并后的购物车
                    carts = cartService.mergeCart(cookieCarts, carts);
                    // 把合并后的购物车，存储到Redis
                    cartService.saveCartRedis(userId, carts);
                    // 删除用户浏览器的Cookie(购物车Cookie)
                    CookieUtils.deleteCookie(request, response,
                            CookieUtils.CookieName.PINYOUGOU_CART);
                }
            }
        }else { // 未登录
            /** ######## 未登录的用户从用户浏览器获取Cookie(购物车数据) ######## */
            // List<Cart> [{},{}]
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            System.out.println("cartJsonStr = " + cartJsonStr);

            // 判断是否为空
            if (StringUtils.isBlank(cartJsonStr)) {// 为空
                // 创建新的购物车
                cartJsonStr = "[]";
            }
            // 把json数组的字符串，转化成List<Cart>
            carts = JSON.parseArray(cartJsonStr, Cart.class);
        }
        return carts;
    }
}
