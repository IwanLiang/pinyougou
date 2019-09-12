package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-21<p>
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SellerMapper sellerMapper;


    @Override
    public void save(Goods goods) {
        try{
            // 1. 往tb_goods表插入数据
            // 设置商品的审核状态：未审核
            goods.setAuditStatus("0");
            goodsMapper.insertSelective(goods);

            // 2. 往tb_goods_desc表插入数据
            // 设置主键id
            goods.getGoodsDesc().setGoodsId(goods.getId());
            goodsDescMapper.insertSelective(goods.getGoodsDesc());

            // 3. 往tb_item表插入数据
            // 判断是否启用规格
            if ("1".equals(goods.getIsEnableSpec())) { // 启用规格

                for (Item item : goods.getItems()) {
                    // item: { "spec": { "网络": "移动4G", "机身内存": "128G" },
                    //        "price": "2000", "num": "100", "status": 1, "isDefault": 1 }

                    // Apple iPhone 8 (A1863) 64GB 金色 移动联通电信4G手机
                    // SPU的名称 + 规格选项
                    StringBuilder title = new StringBuilder(goods.getGoodsName());
                    //  { "网络": "移动4G", "机身内存": "128G" }
                    String spec = item.getSpec();
                    Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                    for (String value : specMap.values()) {
                        title.append(" " + value);
                    }
                    // 设置SKU商品的标题
                    item.setTitle(title.toString());


                    /** 设置SKU商品其它属性 */
                    setItemInfo(item, goods);

                    itemMapper.insertSelective(item);
                }
            }else {  // 没有启用规格(SPU就是SKU)
                // item: { "spec": { "网络": "移动4G", "机身内存": "128G" },
                //        "price": "2000", "num": "100", "status": 1, "isDefault": 1 }
                /** 创建SKU具体商品对象 */
                Item item = new Item();
                /** 设置SKU商品的标题 */
                item.setTitle(goods.getGoodsName());
                /** 设置SKU商品的价格 */
                item.setPrice(goods.getPrice());
                /** 设置SKU商品库存数据 */
                item.setNum(9999);
                /** 设置SKU商品启用状态 */
                item.setStatus("1");
                /** 设置是否默认*/
                item.setIsDefault("1");
                /** 设置规格选项 */
                item.setSpec("{}");

                /** 设置SKU商品其它属性 */
                setItemInfo(item, goods);

                itemMapper.insertSelective(item);

            }

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    //  设置SKU商品其它属性
    private void setItemInfo(Item item, Goods goods) {
        // 设置SKU商品的图片
        /**
         * [{"color":"金色","url":"http://image.pinyougou.com/jd/wKgMg1qtKEOATL9nAAFti6upbx4132.jpg"},
         * {"color":"深空灰色","url":"http://image.pinyougou.com/jd/wKgMg1qtKHmAFxj7AAFZsBqChgk725.jpg"},
         * {"color":"银色","url":"http://image.pinyougou.com/jd/wKgMg1qtKJyAHQ9sAAFuOBobu-A759.jpg"}]
         */
        String itemImages = goods.getGoodsDesc().getItemImages();
        List<Map> imageList = JSON.parseArray(itemImages, Map.class);
        if (imageList != null && imageList.size() > 0) {
            item.setImage(imageList.get(0).get("url").toString());
        }
        // 设置SKU商品的三级分类id
        item.setCategoryid(goods.getCategory3Id());
        // 设置SKU商品的创建时间
        item.setCreateTime(new Date());
        // 设置SKU商品的修改时间
        item.setUpdateTime(item.getCreateTime());
        // 设置SKU商品关联的SPU的id
        item.setGoodsId(goods.getId());
        // 设置SKU商品的商家id
        item.setSellerId(goods.getSellerId());

        // 设置SKU商品的三级分类名称
        ItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
        item.setCategory(itemCat != null ? itemCat.getName() : "");

        // 设置SKU商品的品牌名称
        Brand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());
        item.setBrand(brand != null ? brand.getName() : "");

        // 设置SKU商品的店铺名称
        Seller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());
        item.setSeller(seller != null ? seller.getNickName() : "");
    }

    @Override
    public void update(Goods goods) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Goods findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Goods> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Goods goods, int page, int rows) {
        try{
            PageInfo<Map> pageInfo = PageHelper.startPage(page, rows)
                    .doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    goodsMapper.findAll(goods);
                }
            });

            // 获取分页数据
            List<Map> goodsList = pageInfo.getList();
            // 迭代分页数据
            for (Map map : goodsList) {
                // 获取三级分类id
                Long category3Id = (Long)map.get("category3Id");
                if (category3Id != null){
                    // 查询一级分类的名称
                    String category1Name = itemCatMapper.selectByPrimaryKey(map.get("category1Id")).getName();
                    map.put("category1Name", category1Name);

                    // 查询二级分类的名称
                    String category2Name = itemCatMapper.selectByPrimaryKey(map.get("category2Id")).getName();
                    map.put("category2Name", category2Name);

                    // 查询三级分类的名称
                    String category3Name = itemCatMapper.selectByPrimaryKey(map.get("category3Id")).getName();
                    map.put("category3Name", category3Name);
                }
            }

            return new PageResult(pageInfo.getPages(), goodsList);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 修改商品状态码 */
    public void updateStatus(String columnName, Long[] ids, String status){
        try{
            // UPDATE tb_goods SET audit_status = ? WHERE id IN (?,?,?)
            goodsMapper.updateStatus(columnName, ids, status);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据goodsId查询商品详情的数据 */
    public Map<String,Object> getGoods(Long goodsId){
        try{
            Map<String,Object> dataModel = new HashMap<>();

            // 1. 查询tb_goods表中的数据
            Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);

            // 2. 查询tb_goods_desc表中的数据
            GoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);

            // 3. 查询tb_item表中的数据
            // SELECT id, title, price, spec FROM tb_item WHERE goods_id = 149187842867973 ORDER BY is_default DESC
            List<Map<String,Object>> itemList = itemMapper.findItemByGoodsId(goodsId);
            // 把List集合转化成json字符串
            dataModel.put("itemList", JSON.toJSONString(itemList));

            // 4. 查询商品的分类
            if (goods.getCategory3Id() != null){
                // 查询商品的一级分类名称
                String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
                dataModel.put("itemCat1", itemCat1);

                // 查询商品的二级分类名称
                String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
                dataModel.put("itemCat2", itemCat2);

                // 查询商品的三级分类名称
                String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
                dataModel.put("itemCat3", itemCat3);
            }

            return dataModel;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据goodsId查询SKU商品 */
    public List<Item> findItemByGoodsId(List<Long> goodsIds){
        try{
            // SELECT * FROM tb_item WHERE goods_id IN (?,?)
            Example example = new Example(Item.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("goodsId", goodsIds);
            return itemMapper.selectByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
