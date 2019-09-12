package com.pinyougou.mapper;

import com.pinyougou.pojo.Goods;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * GoodsMapper 数据访问接口
 * @date 2019-08-15 15:23:56
 * @version 1.0
 */
public interface GoodsMapper extends Mapper<Goods>{

    /** 多条件查询商品 */
    List<Map> findAll(Goods goods);

    /** 修改商品状态码 */
    void updateStatus(String columnName, Long[] ids, String status);
}