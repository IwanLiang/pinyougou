package com.pinyougou.cart.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-05<p>
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 把SKU商品加入用户的购物车
     * @param carts 用户的购物车
     * @param itemId SKU商品的id
     * @param num 购买数量
     * @return 修改后得购物车
     */
    public List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num){
        try{
            // 1. 查询SKU商品的数据
            Item item = itemMapper.selectByPrimaryKey(itemId);
            // 2. 获取商家id
            String sellerId = item.getSellerId();
            
            // 3. 根据商家id从用户的购物车集合中搜索商家的购物车
            Cart cart = searchCartBySellerId(carts, sellerId);

            // 4. 判断商家的购物车是否为空
            if (cart == null){ // 代表该用户没有购买该商家的商品
                // 创建商家的购物车
                cart = new Cart();
                // 设置商家id
                cart.setSellerId(sellerId);
                // 设置商家店铺名称
                cart.setSellerName(item.getSeller());

                // 创建商家商品集合
                List<OrderItem> orderItems = new ArrayList<>();
                // 创建商家购物车中的商品对象
                OrderItem orderItem = createOrderItem(item, num);
                // 添加到商家商品集合中
                orderItems.add(orderItem);
                // 设置商品集合
                cart.setOrderItems(orderItems);

                // 添加商家的购物车到用户的购物车中
                carts.add(cart);

            }else { // 代表用户购买过该商家的商品
                // 获取该商家的购物车集合
                List<OrderItem> orderItems = cart.getOrderItems();
                // 根据itemId从商家的购物车集合中搜索商品
                OrderItem orderItem = searchOrderItemByItemId(orderItems, itemId);

                // 判断用户是否购买过该商家的同一个商品
                if (orderItem == null){// 不一样的商品
                    // 创建购物车的商品
                    orderItem = createOrderItem(item, num);
                    orderItems.add(orderItem);
                }else { // 一样的商品
                    // 设置商品数量相加
                    orderItem.setNum(orderItem.getNum() + num);
                    // 设置小计金额
                    orderItem.setTotalFee(new BigDecimal(orderItem.getNum()
                                * orderItem.getPrice().doubleValue()));

                    // 判断购买数量是否为零
                    if (orderItem.getNum() == 0){
                        // 从商家的购物车集合中删除该商品
                        orderItems.remove(orderItem);
                    }
                    // 判断商家的购物车集合大小
                    if (orderItems.size() == 0){
                        // 从用户的购物车中删除商家的购物车
                        carts.remove(cart);
                    }
                }
            }

            return carts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据itemId从商家的购物车集合中搜索商品 */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /** 创建购物车中的商品 */
    private OrderItem createOrderItem(Item item, Integer num) {
        OrderItem orderItem = new OrderItem();
        // 设置商品SKU的id
        orderItem.setItemId(item.getId());
        // 设置商品SPU的id
        orderItem.setGoodsId(item.getGoodsId());
        // 设置商品的标题
        orderItem.setTitle(item.getTitle());
        // 设置商品的价格
        orderItem.setPrice(item.getPrice());
        // 设置商品的购买数量
        orderItem.setNum(num);
        // 设置商品的小计金额
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        // 设置商品的图片
        orderItem.setPicPath(item.getImage());
        // 设置商家的id
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    /** 根据商家id从用户的购物车集合中搜索商家的购物车 */
    private Cart searchCartBySellerId(List<Cart> carts, String sellerId) {
        for (Cart cart : carts) {
            if (sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }

    /**
     * 把用户的购物车存储到Redis
     * @param userId 用户id
     * @param carts 购物车
     */
    public void saveCartRedis(String userId, List<Cart> carts){
        try{
            redisTemplate.boundValueOps("cart_" + userId).set(carts);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 从Redis数据库获取用户的购物车
     * @param userId 用户id
     * @return 购物车
     */
    public List<Cart> findCartRedis(String userId){
        try{
            List<Cart> carts = (List<Cart>)redisTemplate
                    .boundValueOps("cart_" + userId).get();
            if (carts == null){
                carts = new ArrayList<>();
            }
            return carts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 购物车合并
     * @param cookieCarts Cookie中的购物车
     * @param redisCarts Redis中的购物车
     * @return 合并后得购物车
     */
    public List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> redisCarts){
        try{
            // 迭代Cookie中的购物车数据
            for (Cart cookieCart : cookieCarts) {
                for (OrderItem orderItem : cookieCart.getOrderItems()) {
                    redisCarts = addItemToCart(redisCarts, orderItem.getItemId(), orderItem.getNum());
                }
            }
            return redisCarts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
