package com.pinyougou.mapper;

import com.pinyougou.pojo.Item;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * ItemMapper 数据访问接口
 * @date 2019-08-15 15:23:56
 * @version 1.0
 */
public interface ItemMapper extends Mapper<Item>{

    /** 根据goods_id查询SKU商品数据 */
    @Select("SELECT id, title, price, spec FROM tb_item WHERE goods_id = #{goodsId} ORDER BY is_default DESC")
    List<Map<String,Object>> findItemByGoodsId(Long goodsId);
}