package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-07<p>
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public void save(Order order) {
        try{
            // 从Redis数据库获取用户的购物车
            List<Cart> cartList = (List<Cart>) redisTemplate.
                    boundValueOps("cart_" + order.getUserId()).get();

            // 定义支付总金额
            double totalMoney = 0;
            // 定义StringBuilder拼接多个订单id
            StringBuilder orderIds = new StringBuilder();

            // 1. 往tb_order表插入数据(一个Cart产生一个订单(对应商家))
            for (Cart cart : cartList) {
                // 创建订单
                Order order1 = new Order();
                // 生成主键id
                long orderId = idWorker.nextId();
                // 设置订单主键id
                order1.setOrderId(orderId);
                // 设置订单的支付方式
                order1.setPaymentType(order.getPaymentType());
                // 设置订单的状态: 1、未付款
                order1.setStatus("1");
                // 设置订单的创建时间
                order1.setCreateTime(new Date());
                // 设置订单的修改时间
                order1.setUpdateTime(order1.getCreateTime());
                // 设置订单关联的用户id
                order1.setUserId(order.getUserId());
                // 设置订单的收件地址
                order1.setReceiverAreaName(order.getReceiverAreaName());
                // 设置订单的收件人电话
                order1.setReceiverMobile(order.getReceiverMobile());
                // 设置订单的收件人
                order1.setReceiver(order.getReceiver());
                // 设置订单的来源
                order1.setSourceType(order.getSourceType());
                // 设置订单的商家id
                order1.setSellerId(cart.getSellerId());
                

                // 定义订单总金额
                double money = 0;
                // 2. 往tb_order_item表插入数据
                for (OrderItem orderItem : cart.getOrderItems()) {
                    // 设置订单明细的主键id
                    orderItem.setId(idWorker.nextId());
                    // 设置订单明细关联的订单id
                    orderItem.setOrderId(orderId);
                    // 累计该订单的总金额
                    money += orderItem.getTotalFee().doubleValue();

                    // 往订单明细表插入数据
                    orderItemMapper.insertSelective(orderItem);
                }


                // 累计多个订单的总金额
                totalMoney += money;
                // 多个订单id
                orderIds.append(orderId + ",");

                // 设置订单总支付金额
                order1.setPayment(new BigDecimal(money));
                // 往tb_order表插入数据
                orderMapper.insertSelective(order1);
            }

            // 往tb_pay_log表中插入记录
            if ("1".equals(order.getPaymentType())){ // 在线支付
                PayLog payLog = new PayLog();
                // 主键id
                payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
                // 创建时间
                payLog.setCreateTime(new Date());
                // 支付总金额(分)
                payLog.setTotalFee((long)(totalMoney * 100));
                // 用户id
                payLog.setUserId(order.getUserId());
                // 交易状态码：未支付
                payLog.setTradeState("0");
                // 设置多个订单id
                payLog.setOrderList(orderIds.substring(0,
                        orderIds.toString().length() - 1));
                // 支付类型
                payLog.setPayType(order.getPaymentType());
                payLogMapper.insertSelective(payLog);

                // 把支付日志存储到Redis
                redisTemplate.boundValueOps("payLog_" + order.getUserId())
                        .set(payLog, 2 , TimeUnit.HOURS);
            }

            // 3. 从Redis中删除用户的购物车
            redisTemplate.delete("cart_" + order.getUserId());

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Order order) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Order findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public List<Order> findByPage(Order order, int page, int rows) {
        return null;
    }

    /** 从Redis获取支付的订单 */
    public PayLog findPayLogFromRedis(String userId){
        try{
            return (PayLog)redisTemplate.boundValueOps("payLog_" + userId).get();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 修改支付状态 */
    public void updateOrderStatus(String outTradeNo, String transactionId){
        try{
            // 1. 修改tb_pay_log
            PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
            // 设置交易状态：已支付
            payLog.setTradeState("1");
            // 设置支付时间
            payLog.setPayTime(new Date());
            // 设置微信订单号
            payLog.setTransactionId(transactionId);
            payLogMapper.updateByPrimaryKeySelective(payLog);

            // 2. 修改tb_order
            String[] orderIds = payLog.getOrderList().split(",");
            for (String orderId : orderIds) {
                Order order = new Order();
                order.setOrderId(Long.valueOf(orderId));
                // 2、已付款
                order.setStatus("2");
                order.setPaymentTime(payLog.getPayTime());
                orderMapper.updateByPrimaryKeySelective(order);
            }

            // 3. 删除Redis的支付日志
            redisTemplate.delete("payLog_" + payLog.getUserId());

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
