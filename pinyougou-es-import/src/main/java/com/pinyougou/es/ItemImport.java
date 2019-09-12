package com.pinyougou.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinyougou.es.dao.EsItemDao;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品数据导入
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-27<p>
 */
@Component
public class ItemImport {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private EsItemDao esItemDao;

    /** 把SKU商品数据导入索引库 */
    public void importData() throws Exception{

        // 查询SKU商品表中的数据
        Item item = new Item();
        // 设置状态码
        item.setStatus("1");
        // 条件查询
        List<Item> itemList = itemMapper.select(item);

        System.out.println("=====华丽分割线=====");
        List<EsItem> items = new ArrayList<>();
        for (Item item1 : itemList) {
            System.out.println(item1.getId() + "\t" + item1.getTitle());
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
            Map<String,String> specMap = new ObjectMapper().readValue(spec, Map.class);
            // 设置规格选项
            esItem.setSpec(specMap);

            items.add(esItem);
        }

        esItemDao.saveAll(items);
        System.out.println("=====华丽分割线=====");
    }

    public static void main(String[] args) throws Exception{
        // 创建Spring容器
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        // 获取java对象
        ItemImport itemImport = ac.getBean(ItemImport.class);

        itemImport.importData();
    }
}
