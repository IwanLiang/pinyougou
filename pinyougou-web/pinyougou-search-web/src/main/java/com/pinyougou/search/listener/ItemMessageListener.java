package com.pinyougou.search.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.EsItem;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息监听器(同步SKU商品的索引数据)
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-01<p>
 */
public class ItemMessageListener implements MessageListenerConcurrently {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Reference(timeout = 10000)
    private ItemSearchService itemSearchService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExts,
                                                    ConsumeConcurrentlyContext context) {
        try {
            System.out.println("========ItemMessageListener=======");
            // 1. 获取消息对象
            MessageExt messageExt = messageExts.get(0);
            // 2. 获取消息内容 [111,222]
            String jsonStr = new String(messageExt.getBody(), "UTF-8");
            // 3. 转化成List集合
            List<Long> goodsIds = JSON.parseArray(jsonStr, Long.class);
            System.out.println("goodsIds = " + goodsIds);

            // 4. 获取标签
            String tags = messageExt.getTags();

            // 5. 判断标签
            if ("UPDATE".equals(tags)){
                // 6. 把SKU商品数据同步到索引库
                // 6.1 根据goodsId从tb_item表查询SKU商品
                List<Item> itemList = goodsService.findItemByGoodsId(goodsIds);

                List<EsItem> items = new ArrayList<>();
                for (Item item1 : itemList) {
                    // 把Item转化成EsItem
                    EsItem esItem = new EsItem();
                    esItem.setId(item1.getId());
                    esItem.setTitle(item1.getTitle());
                    esItem.setPrice(item1.getPrice().doubleValue());
                    esItem.setImage(item1.getImage());
                    esItem.setGoodsId(item1.getGoodsId());
                    esItem.setCategory(item1.getCategory());
                    esItem.setBrand(item1.getBrand());
                    esItem.setSeller(item1.getSeller());
                    esItem.setUpdateTime(item1.getUpdateTime());

                    // json字符串 {}
                    String spec = item1.getSpec();
                    Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                    // 设置规格选项
                    esItem.setSpec(specMap);

                    items.add(esItem);
                }
                // 6.2 把SKU商品数据保存到ES的pinyougou索引库
                itemSearchService.saveOrUpdate(items);
            }


            if ("DELETE".equals(tags)){
                // 从索引库中删除SKU商品的索引数据
                itemSearchService.delete(goodsIds);
            }

        }catch (Exception ex){
            ex.printStackTrace();
            // 稍后再重新消费(消费失败)
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        // 消费成功
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
