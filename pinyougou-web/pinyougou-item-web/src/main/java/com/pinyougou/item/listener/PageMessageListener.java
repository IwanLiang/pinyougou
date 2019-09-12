package com.pinyougou.item.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.service.GoodsService;
import freemarker.template.Template;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * 消息监听器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-01<p>
 */
public class PageMessageListener implements MessageListenerConcurrently{

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Reference(timeout = 10000)
    private GoodsService goodsService;
    // 生成静态页面的存储路径
    @Value("${page.dir}")
    private String pageDir;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExts,
                                                    ConsumeConcurrentlyContext context) {
        try {
            System.out.println("========PageMessageListener=======");
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
            if ("CREATE".equals(tags)){
                // 6. 生成SKU商品的静态页面
                // 获取模板对象
                Template template = freeMarkerConfigurer
                        .getConfiguration().getTemplate("item.ftl");

                // 循环生成SKU的静态页面
                for (Long goodsId : goodsIds) {
                    // 获取数据模板
                    Map<String, Object> dataModel = goodsService.getGoods(goodsId);

                    // 定义输出流
                    OutputStreamWriter writer = new OutputStreamWriter(new
                            FileOutputStream(pageDir + goodsId + ".html"), "UTF-8");

                    // 模板填充数据模型，输出页面
                    template.process(dataModel, writer);
                    // 关闭
                    writer.close();
                }
            }

            if ("DELETE".equals(tags)){
                // 7. 删除SKU商品的静态页面
                for (Long goodsId : goodsIds) {
                    File file = new File(pageDir + goodsId + ".html");
                    if (file.exists()){
                        file.delete();
                    }
                }
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
