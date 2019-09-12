package com.pinyougou.seckill.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 秒杀订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-10<p>
 */
@Service
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }

    /** 秒杀下单 */
    public void saveSeckillOrderToRedis(String userId, Long id){
        try{
            // 获取Redis中的分布式锁，如果返回true就代表获取锁成功
            boolean lock = (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    JedisCommands jedisCommands = (JedisCommands) connection
                            .getNativeConnection();
                    return "OK".equals(jedisCommands.set("kill_" + id,
                            "true", "nx", "ex", 20));
                }
            });
            if (lock){
                try{
                    // 1. 扣减库存
                    // 1.1 从Redis数据库中获取秒杀商品
                    SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                            .boundHashOps("seckillGoodsList").get(id);
                    if (seckillGoods != null && seckillGoods.getStockCount() > 0){
                        // 1.2 减库存(线程安全)
                        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
                        // 1.3 判断是否已秒光
                        if (seckillGoods.getStockCount() == 0){
                            // 把秒杀商品同步到数据库
                            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                            // 从Redis中删除该秒杀商品
                            redisTemplate.boundHashOps("seckillGoodsList").delete(id);
                        }else{
                            // 同步秒杀商品到Redis
                            redisTemplate.boundHashOps("seckillGoodsList").put(id, seckillGoods);
                        }

                        // 2. 产生秒杀订单
                        SeckillOrder seckillOrder = new SeckillOrder();
                        seckillOrder.setId(idWorker.nextId());
                        seckillOrder.setSeckillId(seckillGoods.getId());
                        seckillOrder.setMoney(seckillGoods.getCostPrice());
                        seckillOrder.setUserId(userId);
                        seckillOrder.setSellerId(seckillGoods.getSellerId());
                        seckillOrder.setCreateTime(new Date());
                        seckillOrder.setStatus("0");

                        // 3. 存入Redis
                        redisTemplate.boundHashOps("seckillOrderList")
                                .put(userId, seckillOrder);
                    }

                }finally {
                    // 释放锁
                    redisTemplate.execute(new RedisCallback<Boolean>() {
                        @Override
                        public Boolean doInRedis(RedisConnection connection)
                                throws DataAccessException {
                            return ((JedisCommands)connection.getNativeConnection())
                                    .del("kill_" + id) == 1;
                        }
                    });
                }
            }else {
                throw new RuntimeException("秒杀商品已锁！");
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 获取秒杀订单 */
    public SeckillOrder findSeckillFromRedis(String userId){
        try{
            return (SeckillOrder)redisTemplate
                    .boundHashOps("seckillOrderList").get(userId);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 保存订单 */
    public void saveOrder(String userId, String transactionId){
        try{
            // 从Redis获取秒杀订单
            SeckillOrder seckillOrder = findSeckillFromRedis(userId);

            // 保存到数据库
            seckillOrder.setTransactionId(transactionId);
            seckillOrder.setPayTime(new Date());
            seckillOrder.setStatus("1");
            seckillOrderMapper.insertSelective(seckillOrder);

            // 从Redis中删除秒杀订单
            redisTemplate.boundHashOps("seckillOrderList").delete(userId);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 查询超时未支付的秒杀订单 */
    public List<SeckillOrder> findOrderByTimeout(){
        try{
            List<SeckillOrder> seckillOrders = new ArrayList<>();

            // 1. 从Redis中查询全部未支付的秒杀订单
            List<SeckillOrder> seckillOrderList = redisTemplate
                    .boundHashOps("seckillOrderList").values();

            // 2. 迭代判断哪些秒杀订单超出了5分钟，还未支付，添加到新的集合中
            seckillOrderList.forEach( seckillOrder -> {
                // 获取当前时间的毫秒数 - 5分钟的毫秒数
                long date = new Date().getTime() - 5 * 60 * 1000;
                // 用秒杀订单的创建时间与当前时比较
                if (seckillOrder.getCreateTime().getTime() < date){
                    seckillOrders.add(seckillOrder);
                }
            });
            // 3. 返回超时未支付的秒杀订单集合
            return seckillOrders;

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 删除超时未支付的订单，恢复库存 */
    public void deleteOrderFromRedis(SeckillOrder seckillOrder){
        try{
            // 1. 恢复秒杀商品的库存
            // 1.1. 从Redis获取秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods)redisTemplate
                    .boundHashOps("seckillGoodsList")
                    .get(seckillOrder.getSeckillId());
            if (seckillGoods != null){
                // 增加库存
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            }else {
                // 查询秒杀商品
                seckillGoods = seckillGoodsMapper
                        .selectByPrimaryKey(seckillOrder.getSeckillId());
                seckillGoods.setStockCount(1);
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            }
            // 1.2 把秒杀商品重新存储到Redis
            redisTemplate.boundHashOps("seckillGoodsList")
                    .put(seckillGoods.getId(), seckillGoods);

            // 2. 从Redis中删除超时未支付的订单
            redisTemplate.boundHashOps("seckillOrderList")
                    .delete(seckillOrder.getUserId());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
