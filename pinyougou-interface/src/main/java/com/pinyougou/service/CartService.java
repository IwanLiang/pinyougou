package com.pinyougou.service;

import com.pinyougou.cart.Cart;

import java.util.List; /**
 * CartService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-05<p>
 */
public interface CartService {

    /**
     * 把SKU商品加入用户的购物车
     * @param carts 用户的购物车
     * @param itemId SKU商品的id
     * @param num 购买数量
     * @return 修改后得购物车
     */
    List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num);

    /**
     * 把用户的购物车存储到Redis
     * @param userId 用户id
     * @param carts 购物车
     */
    void saveCartRedis(String userId, List<Cart> carts);

    /**
     * 从Redis数据库获取用户的购物车
     * @param userId 用户id
     * @return 购物车
     */
    List<Cart> findCartRedis(String userId);

    /**
     * 购物车合并
     * @param cookieCarts Cookie中的购物车
     * @param redisCarts Redis中的购物车
     * @return 合并后得购物车
     */
    List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> redisCarts);
}
